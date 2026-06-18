package com.nikolay.kochev.eventdrivenruleengine.engine.transformation;

import com.fasterxml.jackson.databind.JsonNode;
import com.nikolay.kochev.eventdrivenruleengine.service.model.EventDrivenMessage;

import java.util.Optional;

public interface TransformationEngine {

    Optional<JsonNode> transformMessage(EventDrivenMessage payload);
}

