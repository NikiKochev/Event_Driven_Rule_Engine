package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import com.nikolay.kochev.eventdrivenruleengine.engine.transformation.TransformationOperation;
import org.springframework.stereotype.Component;

import static com.nikolay.kochev.eventdrivenruleengine.engine.constant.Constants.PATH_DELIMITER;
import static com.nikolay.kochev.eventdrivenruleengine.engine.constant.Constants.ROOT_PREFIX;

@Component
public class SetOperation implements TransformationOperation {

    @Override
    public void execute(TransformationLoader.Transformation transformation, JsonNode payload, JsonNode result) {
        String path = transformation.path();
        String value = transformation.value();

        if (path == null || path.isEmpty()) {
            return;
        }

        String cleanPath = path.startsWith(ROOT_PREFIX) ? path.substring(2) : path;

        String[] pathParts = cleanPath.split(PATH_DELIMITER);

        if (result instanceof ObjectNode) {
            setValueAtPath((ObjectNode) result, pathParts, value);
        }
    }

    private void setValueAtPath(ObjectNode node, String[] pathParts, String value) {
        if (pathParts.length == 0) {
            return;
        }

        ObjectNode current = node;
        for (int i = 0; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            JsonNode child = current.get(part);

            if (child == null || !child.isObject()) {
                ObjectNode newNode = current.objectNode();
                current.set(part, newNode);
                current = newNode;
            } else {
                current = (ObjectNode) child;
            }
        }

        // Set the value at the final path
        String finalKey = pathParts[pathParts.length - 1];
        current.set(finalKey, TextNode.valueOf(value));
    }

}

