package io.github.aimailabs.nexabase.document.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置。
 * <p>
 * 定义文档变更事件的 Exchange、Queue 和 Binding：
 * <ul>
 *   <li>{@code document.exchange} — Topic Exchange，接收所有文档变更事件</li>
 *   <li>{@code document.queue.search} — 供 nexabase-search 消费，更新 ES 索引</li>
 *   <li>{@code document.queue.ai} — 供 nexabase-ai 消费，触发向量化处理</li>
 *   <li>{@code document.dlq} — 死信队列，处理消费失败的消息</li>
 * </ul>
 * RoutingKey 规范：{@code document.changed}（不区分事件类型，由消费方根据 action 字段判断）
 */
@Configuration
public class RabbitMqConfig {

    /** 文档变更 Topic Exchange */
    public static final String EXCHANGE_DOCUMENT = "document.exchange";

    /** 死信 Exchange */
    public static final String EXCHANGE_DLX = "document.dlx";

    /** Search 服务消费队列 */
    public static final String QUEUE_SEARCH = "document.queue.search";

    /** AI 服务消费队列（向量化） */
    public static final String QUEUE_AI = "document.queue.ai";

    /** 死信队列 */
    public static final String QUEUE_DLQ = "document.dlq";

    /** 路由键 */
    public static final String ROUTING_KEY_CHANGED = "document.changed";

    // ==================== Exchange ====================

    @Bean
    public TopicExchange documentExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_DOCUMENT)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE_DLX)
                .durable(true)
                .build();
    }

    // ==================== Queue ====================

    @Bean
    public Queue searchQueue() {
        return QueueBuilder.durable(QUEUE_SEARCH)
                // 绑定死信 Exchange，消费失败时转入死信队列
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", QUEUE_DLQ)
                .build();
    }

    @Bean
    public Queue aiQueue() {
        return QueueBuilder.durable(QUEUE_AI)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", QUEUE_DLQ)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(QUEUE_DLQ).build();
    }

    // ==================== Binding ====================

    @Bean
    public Binding searchBinding(Queue searchQueue, TopicExchange documentExchange) {
        return BindingBuilder.bind(searchQueue)
                .to(documentExchange)
                .with(ROUTING_KEY_CHANGED);
    }

    @Bean
    public Binding aiBinding(Queue aiQueue, TopicExchange documentExchange) {
        return BindingBuilder.bind(aiQueue)
                .to(documentExchange)
                .with(ROUTING_KEY_CHANGED);
    }

    @Bean
    public Binding dlqBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(QUEUE_DLQ);
    }

    // ==================== Converter ====================

    /**
     * 显式声明 RabbitAdmin，用于在应用启动时创建 Exchange、Queue 和 Binding。
     * <p>
     * 注意：Spring AMQP 3.x 中 RabbitAdmin 的 declare 动作挂在 ConnectionListener 回调上，
     * 只有连接首次建立才会触发。本模块为纯消息生产者（无 @RabbitListener），
     * CachingConnectionFactory 默认惰性连接，启动时不会建连，故需由下方
     * {@link #rabbitAdminInitializer} 主动触发。
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    /**
     * 应用就绪后主动调用 {@link RabbitAdmin#initialize()}，强制建立连接并完成
     * Exchange/Queue/Binding 的声明，确保纯生产者场景下远程 MQ 也能看到队列资源。
     */
    @Bean
    public ApplicationRunner rabbitAdminInitializer(RabbitAdmin rabbitAdmin) {
        return args -> rabbitAdmin.initialize();
    }

    /**
     * 使用 JSON 序列化消息，确保跨服务兼容。
     */
    @Bean
    @ConditionalOnMissingBean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置 RabbitTemplate 使用 JSON 消息转换器。
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                          MessageConverter jsonMessageConverter,
                                          io.github.aimailabs.nexabase.foundation.trace.MqTraceMessagePostProcessor mqTraceMessagePostProcessor) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        // 消息无法路由到队列时，回调通知（防止消息静默丢失）
        template.setMandatory(true);
        // 发送前注入 TraceID 到 AMQP header，实现 MQ 链路追踪
        template.setBeforePublishPostProcessors(mqTraceMessagePostProcessor);
        return template;
    }
}
