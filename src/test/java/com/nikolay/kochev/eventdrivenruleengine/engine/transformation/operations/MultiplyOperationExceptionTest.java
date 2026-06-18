package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.enums.TransformOperation;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import com.nikolay.kochev.eventdrivenruleengine.exception.TransformationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiplyOperationExceptionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MultiplyOperation multiplyOperation = new MultiplyOperation();

    @Test
    void testException_NullPath() throws Exception {
        String json = """
                {
                    "price": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");
        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation(null, TransformOperation.MULTIPLY, "2");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> multiplyOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Path cannot be null or empty"));
        assertTrue(exception.getMessage().contains("MULTIPLY operation failed"));
    }

    @Test
    void testException_EmptyPath() throws Exception {
        String json = """
                {
                    "price": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("", TransformOperation.MULTIPLY, "2");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> multiplyOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Path cannot be null or empty"));
        assertTrue(exception.getMessage().contains("MULTIPLY operation failed"));
    }

    @Test
    void testException_NullValueToMultiply() throws Exception {
        String json = """
                {
                    "price": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.MULTIPLY, null);

        TransformationException exception = assertThrows(TransformationException.class,
                () -> multiplyOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Value to multiply cannot be null or empty"));
        assertTrue(exception.getMessage().contains("MULTIPLY operation failed"));
    }

    @Test
    void testException_EmptyValueToMultiply() throws Exception {
        String json = """
                {
                    "price": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.MULTIPLY, "");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> multiplyOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Value to multiply cannot be null or empty"));
        assertTrue(exception.getMessage().contains("MULTIPLY operation failed"));
    }

    @Test
    void testException_NullResult() throws Exception{
        JsonNode sourceNode = objectMapper.readTree("{}");
        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.MULTIPLY, "2");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> multiplyOperation.execute(transformation, sourceNode, null));

        assertTrue(exception.getMessage().contains("Result node cannot be null"));
        assertTrue(exception.getMessage().contains("MULTIPLY operation failed"));
    }

    @Test
    void testException_FieldNotFound() throws Exception {
        String json = """
                {
                    "other": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.MULTIPLY, "2");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> multiplyOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Field not found at path 'price'"));
        assertTrue(exception.getMessage().contains("MULTIPLY operation failed"));
    }

    @Test
    void testException_FieldIsNull() throws Exception {
        String json = """
                {
                    "price": null
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.MULTIPLY, "2");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> multiplyOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("is not a number"));
        assertTrue(exception.getMessage().contains("price"));
        assertTrue(exception.getMessage().contains("MULTIPLY operation failed"));
    }

    @Test
    void testException_FieldIsNotNumber_String() throws Exception {
        String json = """
                {
                    "name": "Alice"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("name", TransformOperation.MULTIPLY, "2");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> multiplyOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("is not a number"));
        assertTrue(exception.getMessage().contains("name"));
        assertTrue(exception.getMessage().contains("MULTIPLY operation failed"));
    }

    @Test
    void testException_FieldIsNotNumber_Boolean() throws Exception {
        String json = """
                {
                    "active": true
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("active", TransformOperation.MULTIPLY, "2");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> multiplyOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("is not a number"));
        assertTrue(exception.getMessage().contains("active"));
        assertTrue(exception.getMessage().contains("MULTIPLY operation failed"));
    }

    @Test
    void testException_FieldIsNotNumber_Object() throws Exception {
        String json = """
                {
                    "user": {
                        "name": "Alice"
                    }
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("user", TransformOperation.MULTIPLY, "2");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> multiplyOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("is not a number"));
        assertTrue(exception.getMessage().contains("user"));
        assertTrue(exception.getMessage().contains("MULTIPLY operation failed"));
    }

    @Test
    void testException_InvalidNumberFormat() throws Exception {
        String json = """
                {
                    "price": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.MULTIPLY, "not-a-number");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> multiplyOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Cannot parse value to multiply"));
        assertTrue(exception.getMessage().contains("not-a-number"));
        assertTrue(exception.getMessage().contains("MULTIPLY operation failed"));
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
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("user.balance", TransformOperation.MULTIPLY, "2");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> multiplyOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Field not found at path 'user.balance'"));
        assertTrue(exception.getMessage().contains("MULTIPLY operation failed"));
    }

    @Test
    void testException_IntermediatePathNotObject() throws Exception {
        String json = """
                {
                    "user": "Alice"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("user.balance", TransformOperation.MULTIPLY, "2");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> multiplyOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Field not found") ||
                exception.getMessage().contains("is not an object"));
        assertTrue(exception.getMessage().contains("MULTIPLY operation failed"));
    }

    @Test
    void testNoException_ValidOperation() throws Exception {
        String json = """
                {
                    "price": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.MULTIPLY, "2");

        assertDoesNotThrow(() -> multiplyOperation.execute(transformation, sourceNode, result));

        assertEquals(200.0, result.get("price").asDouble(), 0.001);
    }
}

