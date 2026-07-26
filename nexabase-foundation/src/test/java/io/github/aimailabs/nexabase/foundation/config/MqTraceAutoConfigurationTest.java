package io.github.aimailabs.nexabase.foundation.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MqTraceAutoConfigurationTest {

    @Test
    public void testJsonMessageConverterBeanCreation() {
        MqTraceAutoConfiguration configuration = new MqTraceAutoConfiguration();
        MessageConverter converter = configuration.jsonMessageConverter();
        assertNotNull(converter);
        assertTrue(converter instanceof Jackson2JsonMessageConverter);
    }

    @Test
    public void testMessageDeserialization() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);

        String jsonPayload = "{\"id\":100,\"action\":\"CREATE\"}";
        Message message = new Message(jsonPayload.getBytes(StandardCharsets.UTF_8), properties);

        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) converter.fromMessage(message);

        assertNotNull(resultMap);
        assertEquals(100, resultMap.get("id"));
        assertEquals("CREATE", resultMap.get("action"));
    }
}
