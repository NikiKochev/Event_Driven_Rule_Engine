package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import com.nikolay.kochev.eventdrivenruleengine.engine.transformation.TransformationOperation;
import org.springframework.stereotype.Component;

import static com.nikolay.kochev.eventdrivenruleengine.engine.constant.Constants.*;

@Component
public class JoinPathsOperation implements TransformationOperation {

    @Override
    public void execute(TransformationLoader.Transformation transformation, JsonNode payload, JsonNode result) {
        String targetPath = transformation.path();
        String parts = transformation.value();
        if (targetPath == null || targetPath.isEmpty()) {
            return;
        }
        if (parts == null || parts.isEmpty()) {
            return;
        }

        String cleanTargetPath = targetPath.startsWith(ROOT_PREFIX) ? targetPath.substring(2) : targetPath;
        String[] pathParts = parts.split(PATH_SEPARATOR + ROOT);
        StringBuilder joinedValue = new StringBuilder();

        for (String pathPart : pathParts) {
            if (pathPart.isEmpty()) {
                continue;
            }
            String normalizedPath = pathPart.startsWith(".") ? ROOT + pathPart : ROOT_PREFIX + pathPart;
            String cleanPath = normalizedPath.substring(2); // Remove $.

            JsonNode value = getValueByPath(result, cleanPath);
            if (value == null || value.isNull() || value.isMissingNode()) {
                value = getValueByPath(payload, cleanPath);
            }
            if (value != null && !value.isNull() && !value.isMissingNode()) {
                String stringValue = convertToString(value);
                if (stringValue != null) {
                    joinedValue.append(stringValue);
                }
            }
        }
        if (result instanceof ObjectNode) {
            setValueAtPath((ObjectNode) result, cleanTargetPath, joinedValue.toString());
        }
    }

    private JsonNode getValueByPath(JsonNode node, String path) {
        if (node == null) {
            return null;
        }

        String[] pathParts = path.split(PATH_DELIMITER);
        JsonNode current = node;

        for (String part : pathParts) {
            if (current == null || current.isMissingNode()) {
                return null;
            }
            current = current.get(part);
        }

        return current;
    }

    private String convertToString(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isNumber()) {
            return node.asText();
        }
        if (node.isBoolean()) {
            return String.valueOf(node.asBoolean());
        }
        if (node.isObject() || node.isArray()) {
            return node.toString();
        }
        return node.asText();
    }

    private void setValueAtPath(ObjectNode node, String path, String value) {
        String[] pathParts = path.split(PATH_DELIMITER);
        ObjectNode current = node;

        for (int i = 0; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            JsonNode child = current.get(part);

            if (child == null || !child.isObject()) {
                current = current.putObject(part);
            } else {
                current = (ObjectNode) child;
            }
        }

        String finalKey = pathParts[pathParts.length - 1];
        current.set(finalKey, TextNode.valueOf(value));
    }

}

