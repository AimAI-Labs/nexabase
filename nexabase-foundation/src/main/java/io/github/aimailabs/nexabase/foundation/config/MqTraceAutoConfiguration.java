package io.github.aimailabs.nexabase.foundation.config;

import io.github.aimailabs.nexabase.foundation.trace.MqTraceConsumerAdvice;
import io.github.aimailabs.nexabase.foundation.trace.MqTraceMessagePostProcessor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * MQ TraceID 传播与 JSON 消息转换自动配置。
 * <p>当 classpath 存在 AMQP 类（模块引入 spring-boot-starter-amqp）时激活：
 * <ul>
 *   <li>注册 {@link MessageConverter}（Jackson2JsonMessageConverter），确保消费端和发送端默认支持 JSON 序列化/反序列化</li>
 *   <li>注册 {@link MqTraceMessagePostProcessor}，供生产者 RabbitTemplate 发送时注入 TraceID</li>
 *   <li>注册 {@link BeanPostProcessor}，为消费者 {@link SimpleRabbitListenerContainerFactory} 注入 {@link MqTraceConsumerAdvice}</li>
 * </ul>
 * <p>采用 BeanPostProcessor 而非 {@code SimpleRabbitListenerContainerFactoryCustomizer}，
 * 避免依赖 Spring Boot 版本间差异的 autoconfigure customizer 类，更稳健。
 */
@AutoConfiguration
@ConditionalOnClass({Message.class, SimpleRabbitListenerContainerFactory.class})
public class MqTraceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public MqTraceMessagePostProcessor mqTraceMessagePostProcessor() {
        return new MqTraceMessagePostProcessor();
    }

    @Bean
    public static BeanPostProcessor mqTraceListenerFactoryPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof SimpleRabbitListenerContainerFactory factory) {
                    factory.setAdviceChain(new MqTraceConsumerAdvice());
                }
                return bean;
            }
        };
    }
}
