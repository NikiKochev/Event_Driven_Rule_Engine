package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.enums.TransformOperation;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import com.nikolay.kochev.eventdrivenruleengine.exception.TransformationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtractOperationExceptionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SubtractOperation subtractOperation = new SubtractOperation();

    @Test
    void testException_NullPath() throws Exception {
        String json = """
                {
                    "price": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation(null, TransformOperation.SUBTRACT, "20");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> subtractOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(exception.getMessage().contains("Path cannot be null or empty"));
    }

    @Test
    void testException_EmptyPath() throws Exception {
        String json = """
                {
                    "price": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("", TransformOperation.SUBTRACT, "20");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> subtractOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(exception.getMessage().contains("Path cannot be null or empty"));
    }

    @Test
    void testException_NullValueToSubtract() throws Exception {
        String json = """
                {
                    "price": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.SUBTRACT, null);

        TransformationException exception = assertThrows(TransformationException.class,
                () -> subtractOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(exception.getMessage().contains("Value to subtract cannot be null or empty"));
    }

    @Test
    void testException_EmptyValueToSubtract() throws Exception {
        String json = """
                {
                    "price": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.SUBTRACT, "");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> subtractOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(exception.getMessage().contains("Value to subtract cannot be null or empty"));
    }

    @Test
    void testException_NullResult() {
        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.SUBTRACT, "20");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> subtractOperation.execute(transformation, objectMapper.readTree("{}"), null));

        assertTrue(exception.getMessage().contains("Result node cannot be null"));
    }

    @Test
    void testException_FieldNotFound() throws Exception {
        String json = """
                {
                    "other": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.SUBTRACT, "20");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> subtractOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(exception.getMessage().contains("Field not found at path 'price'"));
    }

    @Test
    void testException_FieldIsNotNumber() throws Exception {
        String json = """
                {
                    "name": "Alice"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("name", TransformOperation.SUBTRACT, "20");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> subtractOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(exception.getMessage().contains("is not a number"));
        assertTrue(exception.getMessage().contains("name"));
    }

    @Test
    void testException_InvalidNumberFormat() throws Exception {
        String json = """
                {
                    "price": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.SUBTRACT, "not-a-number");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> subtractOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(exception.getMessage().contains("Cannot parse value to subtract"));
        assertTrue(exception.getMessage().contains("not-a-number"));
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
                new TransformationLoader.Transformation("user.balance", TransformOperation.SUBTRACT, "20");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> subtractOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(exception.getMessage().contains("Field not found at path 'user.balance'"));
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
                new TransformationLoader.Transformation("user.balance", TransformOperation.SUBTRACT, "20");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> subtractOperation.execute(transformation, objectMapper.readTree("{}"), result));

        // This will fail at field lookup since "user" is a string, not an object
        assertTrue(exception.getMessage().contains("Field not found") ||
                exception.getMessage().contains("is not an object"));
    }

    @Test
    void testNoException_ValidOperation() throws Exception {
        String json = """
                {
                    "price": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.SUBTRACT, "20");

        // Should not throw any exception
        assertDoesNotThrow(() -> subtractOperation.execute(transformation, objectMapper.readTree("{}"), result));

        // Verify the operation was successful
        assertEquals(80.0, result.get("price").asDouble(), 0.001);
    }
}

