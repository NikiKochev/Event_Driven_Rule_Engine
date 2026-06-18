package com.nikolay.kochev.eventdrivenruleengine.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.kochev.eventdrivenruleengine.exception.GlobalExceptionHandler;
import com.nikolay.kochev.eventdrivenruleengine.exception.ProcessingException;
import com.nikolay.kochev.eventdrivenruleengine.messaging.model.IncomingKafkaMessage;
import com.nikolay.kochev.eventdrivenruleengine.service.MessageProcessingService;
import com.nikolay.kochev.eventdrivenruleengine.service.model.EventDrivenMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageConsumer {

    private final GlobalExceptionHandler exceptionHandler;
    private final ObjectMapper objectMapper;
    private final MessageProcessingService messageProcessingService;

    @KafkaListener(topics = "${engine.kafka.input-topic}")
    public void consume(String message, Acknowledgment acknowledgment) {
        log.info("Received kafka message: {}", message);
        IncomingKafkaMessage incomingKafkaMessage;
        try {
            incomingKafkaMessage = objectMapper.readValue(message, IncomingKafkaMessage.class);
        } catch (ProcessingException e) {
            exceptionHandler.handleProcessingException(e, message);
            log.error("Failed to process message {}", message);
            // Acknowledge to prevent infinite retries on unrecoverable errors
            acknowledgment.acknowledge();
            return;
        } catch (Exception e) {
            exceptionHandler.handleUnexpectedException(e, "KafkaMessageConsumer.listen");
            // Acknowledge to prevent infinite retries on unrecoverable errors
            acknowledgment.acknowledge();
            log.error("Unrecoverable error processing message {}, message skipped", message);
            return;
        }

        var incomingMessage = EventDrivenMessage.builder()
                .messageId(incomingKafkaMessage.messageId() == null ?
                        UUID.randomUUID().toString() : incomingKafkaMessage.messageId())
                .timestamp(incomingKafkaMessage.timestamp() == null ?
                        OffsetDateTime.now() : incomingKafkaMessage.timestamp())
                .payload(incomingKafkaMessage.payload())
                .metadata(incomingKafkaMessage.metadata())
                .build();

        messageProcessingService.process(incomingMessage);
        acknowledgment.acknowledge();
        log.info("Message {} acknowledged!", incomingMessage.getMessageId());
    }
}
