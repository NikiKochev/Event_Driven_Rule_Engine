package com.nikolay.kochev.eventdrivenruleengine.service.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Map;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDrivenMessage {

    private String messageId;
    private OffsetDateTime timestamp;
    private JsonNode payload;
    private Map<String, String> metadata;

}
