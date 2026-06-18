package com.nikolay.kochev.eventdrivenruleengine.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GlobalExceptionHandler {

    public void handleProcessingException(ProcessingException ex, String messageId) {
        log.error("Processing error for message {}: {}", messageId, ex.getMessage(), ex);
    }

    public void handleConfigurationException(ConfigurationException ex) {
        log.error("Configuration error: {}", ex.getMessage(), ex);
    }

    public void handleUnexpectedException(Exception ex, String context) {
        log.error("Unexpected error in {}: {}", context, ex.getMessage(), ex);
    }
}
