package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.enums.TransformOperation;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import com.nikolay.kochev.eventdrivenruleengine.exception.TransformationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class DivideOperationExceptionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DivideOperation divideOperation = new DivideOperation();

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
                new TransformationLoader.Transformation(null, TransformOperation.DIVIDE, "2");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> divideOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Path cannot be null or empty"));
        assertTrue(exception.getMessage().contains("DIVIDE operation failed"));
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
                new TransformationLoader.Transformation("", TransformOperation.DIVIDE, "2");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> divideOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Path cannot be null or empty"));
        assertTrue(exception.getMessage().contains("DIVIDE operation failed"));
    }

    @Test
    void testException_NullValueToDivide() throws Exception {
        String json = """
                {
                    "price": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.DIVIDE, null);

        TransformationException exception = assertThrows(TransformationException.class,
                () -> divideOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Value to divide cannot be null or empty"));
        assertTrue(exception.getMessage().contains("DIVIDE operation failed"));
    }

    @Test
    void testException_EmptyValueToDivide() throws Exception {
        String json = """
                {
                    "price": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.DIVIDE, "");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> divideOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Value to divide cannot be null or empty"));
        assertTrue(exception.getMessage().contains("DIVIDE operation failed"));
    }

    @Test
    void testException_NullResult() throws Exception {
        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.DIVIDE, "2");
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> divideOperation.execute(transformation, sourceNode, null));

        assertTrue(exception.getMessage().contains("Result node cannot be null"));
        assertTrue(exception.getMessage().contains("DIVIDE operation failed"));
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
                new TransformationLoader.Transformation("price", TransformOperation.DIVIDE, "2");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> divideOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Field not found at path 'price'"));
        assertTrue(exception.getMessage().contains("DIVIDE operation failed"));
    }

    @Test
    void testException_FieldIsNotNumber() throws Exception {
        String json = """
                {
                    "name": "Alice"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("name", TransformOperation.DIVIDE, "2");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> divideOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("is not a number"));
        assertTrue(exception.getMessage().contains("name"));
        assertTrue(exception.getMessage().contains("DIVIDE operation failed"));
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
                new TransformationLoader.Transformation("price", TransformOperation.DIVIDE, "not-a-number");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> divideOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Cannot parse value to divide"));
        assertTrue(exception.getMessage().contains("not-a-number"));
        assertTrue(exception.getMessage().contains("DIVIDE operation failed"));
    }

    @Test
    void testException_DivisionByZero() throws Exception {
        String json = """
                {
                    "price": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.DIVIDE, "0");

        ArithmeticException exception = assertThrows(ArithmeticException.class,
                () -> divideOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Division by zero is not allowed"));
        assertTrue(exception.getMessage().contains("DIVIDE operation failed"));
    }

    @Test
    void testException_DivisionByNegativeZero() throws Exception {
        String json = """
                {
                    "price": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");
        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.DIVIDE, "-0.0");

        ArithmeticException exception = assertThrows(ArithmeticException.class,
                () -> divideOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Division by zero is not allowed"));
        assertTrue(exception.getMessage().contains("DIVIDE operation failed"));
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
                new TransformationLoader.Transformation("user.balance", TransformOperation.DIVIDE, "2");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> divideOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Field not found at path 'user.balance'"));
        assertTrue(exception.getMessage().contains("DIVIDE operation failed"));
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
                new TransformationLoader.Transformation("user.balance", TransformOperation.DIVIDE, "2");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> divideOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Field not found") ||
                exception.getMessage().contains("is not an object"));
        assertTrue(exception.getMessage().contains("DIVIDE operation failed"));
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
                new TransformationLoader.Transformation("price", TransformOperation.DIVIDE, "2");

        // Should not throw any exception
        assertDoesNotThrow(() -> divideOperation.execute(transformation, sourceNode, result));

        // Verify the operation was successful
        assertEquals(50.0, result.get("price").asDouble(), 0.001);
    }

    @Test
    void testNoException_DivisionBySmallNumber() throws Exception {
        String json = """
                {
                    "value": 10
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("value", TransformOperation.DIVIDE, "0.1");

        assertDoesNotThrow(() -> divideOperation.execute(transformation, sourceNode, result));

        assertEquals(100.0, result.get("value").asDouble(), 0.001);
    }
}

