package com.nikolay.kochev.eventdrivenruleengine.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.condition.ConditionEngine;
import com.nikolay.kochev.eventdrivenruleengine.engine.transformation.TransformationEngine;
import com.nikolay.kochev.eventdrivenruleengine.messaging.model.OutgoingKafkaMessage;
import com.nikolay.kochev.eventdrivenruleengine.messaging.producer.KafkaMessageProducer;
import com.nikolay.kochev.eventdrivenruleengine.service.model.EventDrivenMessage;
import com.nikolay.kochev.eventdrivenruleengine.persistence.service.MessageEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProcessingService {

    private final ConditionEngine conditionEngine;
    private final TransformationEngine transformationEngine;
    private final MessageEventService eventService;
    private final KafkaMessageProducer kafkaMessageProducer;

    public void process(EventDrivenMessage eventDrivenMessage) {
        String messageId = eventDrivenMessage.getMessageId();
        if (eventService.existsByMessageId(messageId)) {
            log.warn("Message {} has already been processed. Skipping.", messageId);
            return;
        }

        log.info("Processing message: {}", messageId);

        String messageType = eventDrivenMessage.getMetadata().get("businessRuleType");
        if (messageType == null || messageType.isEmpty()) {
            log.error("Message {} has no business rule type in metadata", messageId);
            eventService.createRejectRecord(eventDrivenMessage);
            return;
        }
        var messageEventId = eventService.createInitialRecord(eventDrivenMessage, messageType).getId();

        try {

            var isValid = conditionEngine.validateBusinessRules(eventDrivenMessage);
            if (!isValid) {
                log.warn("Message {} failed validation", messageId);
                eventService.updateToValidationFailed(messageEventId);
                return;
            }
            log.debug("Message {} passed validation", messageId);

            var transformedMessage = transformationEngine.transformMessage(eventDrivenMessage);
            if (transformedMessage.isEmpty()) {
                log.warn("Message {} failed transformation", messageId);
                eventService.updateToTransformationFailed(messageEventId,
                        "Transformation returned empty result");
                return;
            }

            log.debug("Message {} transformed successfully", messageId);

            var isSend = sendMessage(eventDrivenMessage, transformedMessage.get(), messageEventId);
            if(!isSend){
                return;
            }
            eventService.updateToCompleted(messageEventId);

            log.info("Message {} processed successfully.", messageId);

        } catch (Exception e) {
            log.error("System error processing message {}: {}", messageId, e.getMessage(), e);
            eventService.updateToSystemError(messageEventId, e.getMessage());
        }
    }

    private boolean sendMessage(EventDrivenMessage eventDrivenMessage, JsonNode transformedMessage, UUID messageEventId) {
        var outgoingKafkaMessage = new OutgoingKafkaMessage (
                eventDrivenMessage.getMessageId(),
                eventDrivenMessage.getTimestamp(),
                transformedMessage,
                eventDrivenMessage.getMetadata()
        );
        try{
            kafkaMessageProducer.sendMessage(outgoingKafkaMessage);
            return true;
        }catch (Exception e){
            log.error("Error sending message {} to Kafka: {}", eventDrivenMessage.getMessageId(), e.getMessage(), e);
            eventService.updateToSendingFailed(messageEventId, "Error sending message to Kafka: " + e.getMessage());
            return false;
        }
    }

}


