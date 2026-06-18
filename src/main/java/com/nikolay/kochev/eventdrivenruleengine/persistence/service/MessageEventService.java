package com.nikolay.kochev.eventdrivenruleengine.persistence.service;

import com.nikolay.kochev.eventdrivenruleengine.service.model.EventDrivenMessage;
import com.nikolay.kochev.eventdrivenruleengine.persistence.entity.MessageEvent;
import com.nikolay.kochev.eventdrivenruleengine.persistence.enums.MessageEventStatus;
import com.nikolay.kochev.eventdrivenruleengine.persistence.repository.MessageEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageEventService {

    private final MessageEventRepository repository;

    public MessageEvent createInitialRecord(EventDrivenMessage eventDrivenMessage, String messageType) {
        log.debug("Creating initial record for message: {}", eventDrivenMessage.getMessageId());

        MessageEvent messageEvent = MessageEvent.builder()
                .messageId(eventDrivenMessage.getMessageId())
                .messageType(messageType)
                .status(MessageEventStatus.RECEIVED)
                .build();

        return repository.save(messageEvent);
    }

    public MessageEvent createRejectRecord(EventDrivenMessage eventDrivenMessage) {
        log.debug("Creating initial rejected record for message: {}", eventDrivenMessage.getMessageId());
        MessageEvent messageEvent = MessageEvent.builder()
                .messageId(eventDrivenMessage.getMessageId())
                .status(MessageEventStatus.RECEIVED)
                .errorMessage("Message rejected due to invalid format or missing required fields")
                .build();

        return repository.save(messageEvent);
    }

    public void updateToCompleted(UUID entityId) {
        log.debug("Updating status to COMPLETED for message: {}", entityId);

        repository.findById(entityId).ifPresent(messageEvent -> {
            messageEvent.setStatus(MessageEventStatus.COMPLETED);
            repository.save(messageEvent);
        });
    }

    public void updateToValidationFailed(UUID entityId) {
        updateStatusWithError(entityId, MessageEventStatus.VALIDATION_FAILED, "Message did not pass business rule validation");
    }

    public void updateToTransformationFailed(UUID entityId, String errorMessage) {
        updateStatusWithError(entityId, MessageEventStatus.TRANSFORMATION_FAILED, errorMessage);
    }

    public void updateToSendingFailed(UUID entityId, String errorMessage) {
        log.debug("Updating status to SENDING_FAILED for message: {}", entityId);
        updateStatusWithError(entityId, MessageEventStatus.SENDING_FAILED, errorMessage);
    }

    public void updateToSystemError(UUID entityId, String errorMessage) {
        log.error("Updating status to SYSTEM_ERROR for message: {}", entityId);
        updateStatusWithError(entityId, MessageEventStatus.SYSTEM_ERROR, errorMessage);
    }

    public boolean existsByMessageId(String messageId) {
        return repository.existsByMessageId(messageId);
    }

    private void updateStatusWithError(UUID entityId, MessageEventStatus status,
                                       String errorMessage) {
        repository.findById(entityId).ifPresent(messageEvent -> {
            messageEvent.setStatus(status);
            messageEvent.setErrorMessage(errorMessage);
            repository.save(messageEvent);
        });
    }


}

