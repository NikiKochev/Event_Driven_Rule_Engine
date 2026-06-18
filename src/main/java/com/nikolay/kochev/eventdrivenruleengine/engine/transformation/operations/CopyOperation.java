package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import com.nikolay.kochev.eventdrivenruleengine.engine.transformation.TransformationOperation;
import org.springframework.stereotype.Component;

import static com.nikolay.kochev.eventdrivenruleengine.engine.constant.Constants.PATH_DELIMITER;
import static com.nikolay.kochev.eventdrivenruleengine.engine.constant.Constants.ROOT_PREFIX;


@Component
public class CopyOperation implements TransformationOperation {

    @Override
    public void execute(TransformationLoader.Transformation transformation, JsonNode payload, JsonNode result) {
        String targetPath = transformation.path();
        String sourcePath = transformation.value();

        if (targetPath == null || targetPath.isEmpty() || sourcePath == null || sourcePath.isEmpty()) {
            return;
        }
        if (!(result instanceof ObjectNode)) {
            return;
        }

        String cleanSourcePath = sourcePath.startsWith(ROOT_PREFIX) ? sourcePath.substring(2) : sourcePath;
        String cleanTargetPath = targetPath.startsWith(ROOT_PREFIX) ? targetPath.substring(2) : targetPath;

        JsonNode sourceValue = getValueByPath(result, cleanSourcePath);
        if (sourceValue == null || sourceValue.isMissingNode()) {
            sourceValue = getValueByPath(payload, cleanSourcePath);
        }
        if (sourceValue == null || sourceValue.isMissingNode()) {
            return;
        }

        setValueAtPath((ObjectNode) result, cleanTargetPath, sourceValue);
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

    private void setValueAtPath(ObjectNode node, String path, JsonNode value) {
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
        current.set(finalKey, value);
    }

}

