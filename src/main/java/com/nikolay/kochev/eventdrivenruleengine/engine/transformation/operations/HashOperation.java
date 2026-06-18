package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import com.nikolay.kochev.eventdrivenruleengine.engine.transformation.TransformationOperation;
import com.nikolay.kochev.eventdrivenruleengine.exception.TransformationException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.nikolay.kochev.eventdrivenruleengine.engine.constant.Constants.PATH_DELIMITER;
import static com.nikolay.kochev.eventdrivenruleengine.engine.constant.Constants.ROOT_PREFIX;

@Component
public class HashOperation implements TransformationOperation {

    @Override
    public void execute(TransformationLoader.Transformation transformation, JsonNode payload, JsonNode result) {
        String targetPath = transformation.path();
        String sourcePath = transformation.value();

        validateInputs(targetPath, sourcePath, result);

        String cleanSourcePath = sourcePath.startsWith(ROOT_PREFIX) ? sourcePath.substring(2) : sourcePath;
        String cleanTargetPath = targetPath.startsWith(ROOT_PREFIX) ? targetPath.substring(2) : targetPath;

        JsonNode sourceNode = getValueByPath(result, cleanSourcePath);
        getAndValidateSourceNode(payload, result, cleanSourcePath, sourcePath);
        String valueToHash = extractStringValue(sourceNode);

        if (valueToHash == null || valueToHash.isEmpty()) {
            throw new TransformationException("HASH operation failed: Source field at path '" + sourcePath + "' is empty");
        }

        try {
            String hashedValue = hashWithSHA256(valueToHash);
            setValueAtPath((ObjectNode) result, cleanTargetPath, hashedValue);
        } catch (NoSuchAlgorithmException e) {
            throw new TransformationException("HASH operation failed: SHA-256 algorithm not available", e);
        } catch (Exception e) {
            throw new TransformationException("HASH operation failed: Cannot set value at path '" + targetPath + "'", e);
        }
    }

    private void validateInputs(String targetPath, String sourcePath, JsonNode result) {
        if (targetPath == null || targetPath.isEmpty()) {
            throw new TransformationException("HASH operation failed: Target path cannot be null or empty");
        }

        if (sourcePath == null || sourcePath.isEmpty()) {
            throw new TransformationException("HASH operation failed: Source path cannot be null or empty");
        }

        if (result == null) {
            throw new TransformationException("HASH operation failed: Result node cannot be null");
        }

        if (!(result instanceof ObjectNode)) {
            throw new TransformationException("HASH operation failed: Result node must be an ObjectNode, but was " + result.getClass().getSimpleName());
        }
    }

    private void getAndValidateSourceNode(JsonNode payload, JsonNode result, String cleanSourcePath, String sourcePath) {
        JsonNode sourceNode = getValueByPath(result, cleanSourcePath);
        if (sourceNode == null || sourceNode.isNull() || sourceNode.isMissingNode()) {
            sourceNode = getValueByPath(payload, cleanSourcePath);
        }

        if (sourceNode == null) {
            throw new TransformationException("HASH operation failed: Source field not found at path '" + sourcePath + "'");
        }

        if (sourceNode.isMissingNode()) {
            throw new TransformationException("HASH operation failed: Source field at path '" + sourcePath + "' is missing");
        }

        if (sourceNode.isNull()) {
            throw new TransformationException("HASH operation failed: Source field at path '" + sourcePath + "' is null");
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

    private String extractStringValue(JsonNode node) {
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

    private String hashWithSHA256(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    private void setValueAtPath(ObjectNode node, String path, String value) {
        String[] pathParts = path.split(PATH_DELIMITER);
        ObjectNode current = node;

        for (int i = 0; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            JsonNode child = current.get(part);

            if (child == null || !child.isObject()) {
                // Create intermediate object if missing or not an object
                current = current.putObject(part);
            } else {
                current = (ObjectNode) child;
            }
        }

        String finalKey = pathParts[pathParts.length - 1];
        current.set(finalKey, TextNode.valueOf(value));
    }

}

