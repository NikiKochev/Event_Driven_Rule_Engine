package com.nikolay.kochev.eventdrivenruleengine.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.kochev.eventdrivenruleengine.service.MessageProcessingService;
import com.nikolay.kochev.eventdrivenruleengine.service.model.EventDrivenMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaMessageConsumerTest {

    @Mock
    private MessageProcessingService messageProcessingService;


    @Mock
    private Acknowledgment acknowledgment;

    private KafkaMessageConsumer kafkaMessageConsumer;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        kafkaMessageConsumer = new KafkaMessageConsumer(
                objectMapper,
                messageProcessingService
        );
    }


    @Test
    @DisplayName("Should successfully consume and process a valid order validation message")
    void shouldProcessValidOrderValidationMessage() {
        String kafkaMessage = """
                {
                    "messageId": "MSG-ORDER-12345",
                    "timestamp": "2026-06-18T10:30:00Z",
                    "metadata": {
                        "businessRuleType": "ORDER_VALIDATION",
                        "source": "order-service",
                        "region": "US-EAST"
                    },
                    "payload": {
                        "orderId": "ORD-001",
                        "orderType": "STANDARD",
                        "amount": 150.50,
                        "currency": "USD",
                        "customerId": "CUST-789",
                        "items": [
                            {"sku": "ITEM-001", "quantity": 2},
                            {"sku": "ITEM-002", "quantity": 1}
                        ]
                    }
                }
                """;

        kafkaMessageConsumer.consume(kafkaMessage, acknowledgment);
        ArgumentCaptor<EventDrivenMessage> eventCaptor = ArgumentCaptor.forClass(EventDrivenMessage.class);
        verify(messageProcessingService).process(eventCaptor.capture());

        EventDrivenMessage processedMessage = eventCaptor.getValue();
        assertThat(processedMessage.getMessageId()).isEqualTo("MSG-ORDER-12345");
        assertThat(processedMessage.getMetadata()).containsEntry("businessRuleType", "ORDER_VALIDATION");
        assertThat(processedMessage.getMetadata()).containsEntry("source", "order-service");
        assertThat(processedMessage.getPayload().get("orderId").asText()).isEqualTo("ORD-001");
        assertThat(processedMessage.getPayload().get("orderType").asText()).isEqualTo("STANDARD");
        assertThat(processedMessage.getPayload().get("amount").asDouble()).isEqualTo(150.50);
        assertThat(processedMessage.getPayload().get("customerId").asText()).isEqualTo("CUST-789");

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Should process payment message with all required fields")
    void shouldProcessPaymentMessage() {
        String kafkaMessage = """
                {
                    "messageId": "MSG-PAY-567",
                    "timestamp": "2026-06-18T11:45:00Z",
                    "metadata": {
                        "businessRuleType": "PAYMENT_PROCESSING",
                        "priority": "HIGH"
                    },
                    "payload": {
                        "transactionId": "TXN-999",
                        "paymentMethod": "CREDIT_CARD",
                        "amount": 250.75,
                        "verified": true,
                        "currency": "USD",
                        "cardLastFour": "4242"
                    }
                }
                """;

        kafkaMessageConsumer.consume(kafkaMessage, acknowledgment);
        ArgumentCaptor<EventDrivenMessage> eventCaptor = ArgumentCaptor.forClass(EventDrivenMessage.class);
        verify(messageProcessingService).process(eventCaptor.capture());

        EventDrivenMessage processedMessage = eventCaptor.getValue();
        assertThat(processedMessage.getMessageId()).isEqualTo("MSG-PAY-567");
        assertThat(processedMessage.getMetadata()).containsEntry("businessRuleType", "PAYMENT_PROCESSING");
        assertThat(processedMessage.getPayload().get("transactionId").asText()).isEqualTo("TXN-999");
        assertThat(processedMessage.getPayload().get("paymentMethod").asText()).isEqualTo("CREDIT_CARD");
        assertThat(processedMessage.getPayload().get("verified").asBoolean()).isTrue();

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Should generate messageId when not provided in incoming message")
    void shouldGenerateMessageIdWhenMissing() {
        String kafkaMessage = """
                {
                    "timestamp": "2026-06-18T12:00:00Z",
                    "metadata": {
                        "businessRuleType": "TEST_RULE"
                    },
                    "payload": {
                        "data": "test"
                    }
                }
                """;

        kafkaMessageConsumer.consume(kafkaMessage, acknowledgment);

        ArgumentCaptor<EventDrivenMessage> eventCaptor = ArgumentCaptor.forClass(EventDrivenMessage.class);
        verify(messageProcessingService).process(eventCaptor.capture());

        EventDrivenMessage processedMessage = eventCaptor.getValue();
        assertThat(processedMessage.getMessageId()).isNotNull();
        assertThat(processedMessage.getMessageId()).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Should handle message with minimal payload")
    void shouldHandleMinimalMessage() {
        String kafkaMessage = """
                {
                    "metadata": {
                        "businessRuleType": "SIMPLE_RULE"
                    },
                    "payload": {
                        "simpleField": "test"
                    }
                }
                """;

        kafkaMessageConsumer.consume(kafkaMessage, acknowledgment);

        ArgumentCaptor<EventDrivenMessage> eventCaptor = ArgumentCaptor.forClass(EventDrivenMessage.class);
        verify(messageProcessingService).process(eventCaptor.capture());

        EventDrivenMessage processedMessage = eventCaptor.getValue();
        assertThat(processedMessage.getMessageId()).isNotNull();
        assertThat(processedMessage.getTimestamp()).isNotNull();
        assertThat(processedMessage.getMetadata()).containsEntry("businessRuleType", "SIMPLE_RULE");
        assertThat(processedMessage.getPayload().get("simpleField").asText()).isEqualTo("test");

        verify(acknowledgment).acknowledge();
    }


    @Test
    @DisplayName("Should handle invalid JSON gracefully and acknowledge message")
    void shouldHandleInvalidJsonGracefully() {
        String invalidJson = "{ this is not valid json }";

        kafkaMessageConsumer.consume(invalidJson, acknowledgment);

        verify(messageProcessingService, never()).process(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Should handle empty JSON object")
    void shouldHandleEmptyJsonObject() {
        String emptyJson = "{}";

        kafkaMessageConsumer.consume(emptyJson, acknowledgment);

        ArgumentCaptor<EventDrivenMessage> eventCaptor = ArgumentCaptor.forClass(EventDrivenMessage.class);
        verify(messageProcessingService).process(eventCaptor.capture());

        EventDrivenMessage processedMessage = eventCaptor.getValue();

        assertThat(processedMessage.getMessageId()).isNotNull();
        assertThat(processedMessage.getTimestamp()).isNotNull();

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Should handle null message string")
    void shouldHandleNullMessage() {
        kafkaMessageConsumer.consume(null, acknowledgment);

        verify(messageProcessingService, never()).process(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Should handle message with invalid JSON structure")
    void shouldHandleInvalidJsonStructure() {
        String arrayJson = "[1, 2, 3]";

        kafkaMessageConsumer.consume(arrayJson, acknowledgment);

        verify(messageProcessingService, never()).process(any());
        verify(acknowledgment).acknowledge();
    }


    @Test
    @DisplayName("Should handle message with null payload")
    void shouldHandleNullPayload() {
        String kafkaMessage = """
                {
                    "messageId": "MSG-NULL-PAYLOAD",
                    "metadata": {
                        "businessRuleType": "TEST"
                    },
                    "payload": null
                }
                """;

        kafkaMessageConsumer.consume(kafkaMessage, acknowledgment);

        ArgumentCaptor<EventDrivenMessage> eventCaptor = ArgumentCaptor.forClass(EventDrivenMessage.class);
        verify(messageProcessingService).process(eventCaptor.capture());

        EventDrivenMessage processedMessage = eventCaptor.getValue();

        assertThat(processedMessage.getPayload() == null || processedMessage.getPayload().isNull()).isTrue();
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Should handle message with empty metadata")
    void shouldHandleEmptyMetadata() {
        String kafkaMessage = """
                {
                    "messageId": "MSG-EMPTY-META",
                    "metadata": {},
                    "payload": {
                        "test": "value"
                    }
                }
                """;

        kafkaMessageConsumer.consume(kafkaMessage, acknowledgment);

        ArgumentCaptor<EventDrivenMessage> eventCaptor = ArgumentCaptor.forClass(EventDrivenMessage.class);
        verify(messageProcessingService).process(eventCaptor.capture());

        EventDrivenMessage processedMessage = eventCaptor.getValue();
        assertThat(processedMessage.getMetadata()).isEmpty();

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Should handle very large payload")
    void shouldHandleLargePayload() {
        StringBuilder largePayloadBuilder = new StringBuilder("{");
        for (int i = 0; i < 100; i++) {
            if (i > 0) largePayloadBuilder.append(",");
            largePayloadBuilder.append("\"field").append(i).append("\": \"value").append(i).append("\"");
        }
        largePayloadBuilder.append("}");

        String kafkaMessage = String.format("""
                {
                    "messageId": "MSG-LARGE",
                    "metadata": {
                        "businessRuleType": "TEST"
                    },
                    "payload": %s
                }
                """, largePayloadBuilder);

        kafkaMessageConsumer.consume(kafkaMessage, acknowledgment);

        ArgumentCaptor<EventDrivenMessage> eventCaptor = ArgumentCaptor.forClass(EventDrivenMessage.class);
        verify(messageProcessingService).process(eventCaptor.capture());

        EventDrivenMessage processedMessage = eventCaptor.getValue();
        assertThat(processedMessage.getPayload().size()).isEqualTo(100);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Should handle special characters in payload")
    void shouldHandleSpecialCharacters() {
        String kafkaMessage = """
                {
                    "messageId": "MSG-SPECIAL-CHARS",
                    "metadata": {
                        "businessRuleType": "TEST",
                        "description": "Test with 'quotes' and \\"escapes\\""
                    },
                    "payload": {
                        "text": "Unicode: €£¥ Emoji: 😀",
                        "special": "Line\\nBreak\\tTab"
                    }
                }
                """;

        kafkaMessageConsumer.consume(kafkaMessage, acknowledgment);

        ArgumentCaptor<EventDrivenMessage> eventCaptor = ArgumentCaptor.forClass(EventDrivenMessage.class);
        verify(messageProcessingService).process(eventCaptor.capture());

        EventDrivenMessage processedMessage = eventCaptor.getValue();
        assertThat(processedMessage.getPayload().get("text").asText()).contains("€£¥");

        verify(acknowledgment).acknowledge();
    }

}

