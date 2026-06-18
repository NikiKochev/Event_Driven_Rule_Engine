package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.enums.TransformOperation;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import com.nikolay.kochev.eventdrivenruleengine.exception.TransformationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashOperationExceptionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HashOperation hashOperation = new HashOperation();

    @Test
    void testException_NullTargetPath() throws Exception {
        String json = """
                {
                    "password": "secret"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation(null, TransformOperation.HASH, "$.password");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> hashOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(exception.getMessage().contains("Target path cannot be null or empty"));
        assertTrue(exception.getMessage().contains("HASH operation failed"));
    }

    @Test
    void testException_EmptyTargetPath() throws Exception {
        String json = """
                {
                    "password": "secret"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("", TransformOperation.HASH, "$.password");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> hashOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(exception.getMessage().contains("Target path cannot be null or empty"));
        assertTrue(exception.getMessage().contains("HASH operation failed"));
    }

    @Test
    void testException_NullSourcePath() throws Exception {
        String json = """
                {
                    "password": "secret"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashed", TransformOperation.HASH, null);

        TransformationException exception = assertThrows(TransformationException.class,
                () -> hashOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(exception.getMessage().contains("Source path cannot be null or empty"));
        assertTrue(exception.getMessage().contains("HASH operation failed"));
    }

    @Test
    void testException_EmptySourcePath() throws Exception {
        String json = """
                {
                    "password": "secret"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashed", TransformOperation.HASH, "");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> hashOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(exception.getMessage().contains("Source path cannot be null or empty"));
        assertTrue(exception.getMessage().contains("HASH operation failed"));
    }

    @Test
    void testException_NullResult() {
        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashed", TransformOperation.HASH, "$.password");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> hashOperation.execute(transformation, objectMapper.readTree("{}"), null));

        assertTrue(exception.getMessage().contains("Result node cannot be null"));
        assertTrue(exception.getMessage().contains("HASH operation failed"));
    }

    @Test
    void testException_SourceFieldNotFound() throws Exception {
        String json = """
                {
                    "other": "value"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashed", TransformOperation.HASH, "$.password");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> hashOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(exception.getMessage().contains("Source field not found at path '$.password'"));
        assertTrue(exception.getMessage().contains("HASH operation failed"));
    }

    @Test
    void testException_SourceFieldIsNull() throws Exception {
        String json = """
                {
                    "password": null
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashed", TransformOperation.HASH, "$.password");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> hashOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(exception.getMessage().contains("HASH operation failed"));
    }

    @Test
    void testException_SourceFieldIsEmpty() throws Exception {
        String json = """
                {
                    "password": ""
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashed", TransformOperation.HASH, "$.password");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> hashOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(exception.getMessage().contains("Source field at path '$.password' is empty"));
        assertTrue(exception.getMessage().contains("HASH operation failed"));
    }

    @Test
    void testException_NestedFieldNotFound() throws Exception {
        String json = """
                {
                    "user": {
                        "name": "Alice"
                    }
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashed", TransformOperation.HASH, "$.user.password");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> hashOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(exception.getMessage().contains("Source field not found at path '$.user.password'"));
        assertTrue(exception.getMessage().contains("HASH operation failed"));
    }

    @Test
    void testException_IntermediatePathNotObject() throws Exception {
        String json = """
                {
                    "user": "Alice"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashed", TransformOperation.HASH, "$.user.password");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> hashOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(exception.getMessage().contains("Source field not found") ||
                exception.getMessage().contains("is not an object"));
        assertTrue(exception.getMessage().contains("HASH operation failed"));
    }

    @Test
    void testException_SourceFieldMissingInBothPayloadAndResult() throws Exception {
        String payload = """
                {
                    "other": "value"
                }
                """;
        String result = """
                {
                    "data": "value"
                }
                """;
        ObjectNode resultNode = (ObjectNode) objectMapper.readTree(result);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashed", TransformOperation.HASH, "$.password");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> hashOperation.execute(transformation, objectMapper.readTree(payload), resultNode));

        assertTrue(exception.getMessage().contains("Source field not found at path '$.password'"));
        assertTrue(exception.getMessage().contains("HASH operation failed"));
    }

    @Test
    void testNoException_ValidOperation() throws Exception {
        String json = """
                {
                    "password": "secret123"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashed", TransformOperation.HASH, "$.password");

        // Should not throw any exception
        assertDoesNotThrow(() -> hashOperation.execute(transformation, objectMapper.readTree("{}"), result));

        // Verify the operation was successful
        assertTrue(result.has("hashed"));
        assertEquals(64, result.get("hashed").asText().length());
    }

    @Test
    void testNoException_ValidOperationWithNumbers() throws Exception {
        String json = """
                {
                    "id": 999
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashedId", TransformOperation.HASH, "$.id");

        // Should not throw any exception for numbers
        assertDoesNotThrow(() -> hashOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(result.has("hashedId"));
        assertEquals(64, result.get("hashedId").asText().length());
    }

    @Test
    void testNoException_ValidOperationWithBoolean() throws Exception {
        String json = """
                {
                    "flag": false
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashedFlag", TransformOperation.HASH, "$.flag");

        // Should not throw any exception for booleans
        assertDoesNotThrow(() -> hashOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(result.has("hashedFlag"));
        assertEquals(64, result.get("hashedFlag").asText().length());
    }
}

