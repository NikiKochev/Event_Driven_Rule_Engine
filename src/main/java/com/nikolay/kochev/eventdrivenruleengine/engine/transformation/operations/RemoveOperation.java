package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import com.nikolay.kochev.eventdrivenruleengine.engine.transformation.TransformationOperation;
import org.springframework.stereotype.Component;

import static com.nikolay.kochev.eventdrivenruleengine.engine.constant.Constants.PATH_DELIMITER;
import static com.nikolay.kochev.eventdrivenruleengine.engine.constant.Constants.ROOT_PREFIX;

@Component
public class RemoveOperation implements TransformationOperation {

    @Override
    public void execute(TransformationLoader.Transformation transformation, JsonNode payload, JsonNode result) {
        String path = transformation.path();
        if (path == null || path.isEmpty()) {
            return;
        }
        if (result == null) {
            return;
        }
        if (!(result instanceof ObjectNode)) {
            return;
        }

        String cleanPath = path.startsWith(ROOT_PREFIX) ? path.substring(2) : path;

        removeFieldAtPath((ObjectNode) result, cleanPath);
    }

    private void removeFieldAtPath(ObjectNode node, String path) {
        String[] pathParts = path.split(PATH_DELIMITER);

        ObjectNode current = node;
        for (int i = 0; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            JsonNode child = current.get(part);

            if (child == null || child.isMissingNode()) {
                return;
            }

            if (!child.isObject()) {
                return;
            }

            current = (ObjectNode) child;
        }

        String finalKey = pathParts[pathParts.length - 1];

        if (current.has(finalKey)) {
            current.remove(finalKey);
        }
    }

}

