package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import com.nikolay.kochev.eventdrivenruleengine.engine.transformation.TransformationEngine;
import com.nikolay.kochev.eventdrivenruleengine.engine.transformation.TransformationExecutor;
import com.nikolay.kochev.eventdrivenruleengine.service.model.EventDrivenMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.nikolay.kochev.eventdrivenruleengine.engine.constant.Constants.RULE_TYPE;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransformationEngineImpl implements TransformationEngine {

    private final TransformationExecutor transformationExecutor;

    @Override
    public Optional<JsonNode> transformMessage(EventDrivenMessage payload) {
        try {
            var transformations = TransformationLoader.getTransformationByType(payload.getMetadata().getOrDefault(RULE_TYPE, null));
            var result = payload.getPayload();
            for (TransformationLoader.Transformation transformation : transformations) {
                transformationExecutor.executeTransformation(transformation, payload.getPayload(), result);

            }
            return Optional.of(result);
        }catch (Exception e) {
            log.error("Error during transformation: {}", e.getMessage(), e);
        }
        return Optional.empty();
    }
}
