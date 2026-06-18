package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import com.nikolay.kochev.eventdrivenruleengine.engine.transformation.TransformationOperation;
import com.nikolay.kochev.eventdrivenruleengine.exception.TransformationException;
import org.springframework.stereotype.Component;

import static com.nikolay.kochev.eventdrivenruleengine.engine.constant.Constants.PATH_DELIMITER;
import static com.nikolay.kochev.eventdrivenruleengine.engine.constant.Constants.ROOT_PREFIX;

@Component
public class SubtractOperation implements TransformationOperation {

    @Override
    public void execute(TransformationLoader.Transformation transformation, JsonNode payload, JsonNode result) {
        String path = transformation.path();
        String valueToSubtract = transformation.value();

        validateInputs(path, valueToSubtract, result);

        String cleanPath = path.startsWith(ROOT_PREFIX) ? path.substring(2) : path;

        JsonNode currentNode = getValueByPath(result, cleanPath);
        validateCurrentNode(currentNode, path);

        try {
            double subtractValue = Double.parseDouble(valueToSubtract);
            double currentValue = currentNode.asDouble();
            double resultValue = currentValue - subtractValue;
            setValueAtPath(result, cleanPath, resultValue);
        } catch (NumberFormatException e) {
            throw new TransformationException("SUBTRACT operation failed: Cannot parse value to subtract '" + valueToSubtract + "' as a number", e);
        } catch (Exception e) {
            throw new TransformationException("SUBTRACT operation failed: Cannot set value at path '" + path + "'", e);
        }
    }

    private void validateInputs(String path, String valueToSubtract, JsonNode result) {
        if (path == null || path.isEmpty()) {
            throw new TransformationException("SUBTRACT operation failed: Path cannot be null or empty");
        }

        if (valueToSubtract == null || valueToSubtract.isEmpty()) {
            throw new TransformationException("SUBTRACT operation failed: Value to subtract cannot be null or empty");
        }

        if (result == null) {
            throw new TransformationException("SUBTRACT operation failed: Result node cannot be null");
        }

        if (!(result instanceof ObjectNode)) {
            throw new TransformationException("SUBTRACT operation failed: Result node must be an ObjectNode, but was " + result.getClass().getSimpleName());
        }
    }

    private void validateCurrentNode(JsonNode currentNode, String path) {
        if (currentNode == null) {
            throw new TransformationException("SUBTRACT operation failed: Field not found at path '" + path + "'");
        }

        if (currentNode.isMissingNode()) {
            throw new TransformationException("SUBTRACT operation failed: Field at path '" + path + "' is missing");
        }

        if (!currentNode.isNumber()) {
            throw new TransformationException("SUBTRACT operation failed: Field at path '" + path + "' is not a number. Current type: " + currentNode.getNodeType() + ", value: " + currentNode);
        }
    }

    private JsonNode getValueByPath(JsonNode node, String path) {
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

    private void setValueAtPath(JsonNode node, String path, double value) {
        if (!(node instanceof ObjectNode)) {
            throw new TransformationException("Cannot set value: Node must be an ObjectNode, but was " + node.getClass().getSimpleName());
        }

        String[] pathParts = path.split(PATH_DELIMITER);
        ObjectNode current = (ObjectNode) node;

        for (int i = 0; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            JsonNode child = current.get(part);

            if (child == null) {
                throw new TransformationException("Cannot navigate to path '" + path + "': Field '" + part + "' does not exist at level " + i);
            }

            if (!child.isObject()) {
                throw new TransformationException("Cannot navigate to path '" + path + "': Field '" + part + "' at level " + i + " is not an object (type: " + child.getNodeType() + ")");
            }

            current = (ObjectNode) child;
        }

        String finalKey = pathParts[pathParts.length - 1];
        current.set(finalKey, DoubleNode.valueOf(value));
    }

}

