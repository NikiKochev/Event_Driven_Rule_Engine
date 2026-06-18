package com.nikolay.kochev.eventdrivenruleengine.messaging.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OutgoingKafkaMessage(
        String messageId,
        OffsetDateTime timestamp,
        JsonNode payload,
        Map<String, String> metadata) {
}

