package com.nikolay.kochev.eventdrivenruleengine.engine.transformation;

import com.fasterxml.jackson.databind.JsonNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;

public interface TransformationOperation {

    void execute(TransformationLoader.Transformation transformation, JsonNode payload, JsonNode result);
}

