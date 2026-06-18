package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.enums.TransformOperation;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import com.nikolay.kochev.eventdrivenruleengine.exception.TransformationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class AddOperationExceptionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AddOperation addOperation = new AddOperation();

    @Test
    void testException_NullPath() throws Exception {
        String json = """
                {
                    "value": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation(null, TransformOperation.ADD, "10");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> addOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Path cannot be null or empty"));
        assertTrue(exception.getMessage().contains("ADD operation failed"));
    }

    @Test
    void testException_EmptyPath() throws Exception {
        String json = """
                {
                    "value": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("", TransformOperation.ADD, "10");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> addOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Path cannot be null or empty"));
        assertTrue(exception.getMessage().contains("ADD operation failed"));
    }

    @Test
    void testException_NullValueToAdd() throws Exception {
        String json = """
                {
                    "value": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");
        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("value", TransformOperation.ADD, null);

        TransformationException exception = assertThrows(TransformationException.class,
                () -> addOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Value to add cannot be null or empty"));
        assertTrue(exception.getMessage().contains("ADD operation failed"));
    }

    @Test
    void testException_EmptyValueToAdd() throws Exception {
        String json = """
                {
                    "value": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("value", TransformOperation.ADD, "");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> addOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Value to add cannot be null or empty"));
        assertTrue(exception.getMessage().contains("ADD operation failed"));
    }

    @Test
    void testException_NullResult() throws Exception {
        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("value", TransformOperation.ADD, "10");
        JsonNode sourceNode = objectMapper.readTree("{}");
        TransformationException exception = assertThrows(TransformationException.class,
                () -> addOperation.execute(transformation, sourceNode, null));

        assertTrue(exception.getMessage().contains("Result node cannot be null"));
        assertTrue(exception.getMessage().contains("ADD operation failed"));
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
                new TransformationLoader.Transformation("value", TransformOperation.ADD, "10");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> addOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Field not found at path 'value'"));
        assertTrue(exception.getMessage().contains("ADD operation failed"));
    }

    @Test
    void testException_FieldIsNull() throws Exception {
        String json = """
                {
                    "value": null
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");
        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("value", TransformOperation.ADD, "10");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> addOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Field at path 'value' is null"));
        assertTrue(exception.getMessage().contains("ADD operation failed"));
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
                new TransformationLoader.Transformation("name", TransformOperation.ADD, "10");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> addOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("is not a number"));
        assertTrue(exception.getMessage().contains("name"));
        assertTrue(exception.getMessage().contains("ADD operation failed"));
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
                new TransformationLoader.Transformation("active", TransformOperation.ADD, "10");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> addOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("is not a number"));
        assertTrue(exception.getMessage().contains("active"));
        assertTrue(exception.getMessage().contains("ADD operation failed"));
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
                new TransformationLoader.Transformation("user", TransformOperation.ADD, "10");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> addOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("is not a number"));
        assertTrue(exception.getMessage().contains("user"));
        assertTrue(exception.getMessage().contains("ADD operation failed"));
    }

    @Test
    void testException_FieldIsNotNumber_Array() throws Exception {
        String json = """
                {
                    "tags": ["java", "spring"]
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");
        
        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("tags", TransformOperation.ADD, "10");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> addOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("is not a number"));
        assertTrue(exception.getMessage().contains("tags"));
        assertTrue(exception.getMessage().contains("ADD operation failed"));
    }

    @Test
    void testException_InvalidNumberFormat() throws Exception {
        String json = """
                {
                    "value": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");
        
        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("value", TransformOperation.ADD, "not-a-number");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> addOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Cannot parse value to add"));
        assertTrue(exception.getMessage().contains("not-a-number"));
        assertTrue(exception.getMessage().contains("ADD operation failed"));
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
                new TransformationLoader.Transformation("user.balance", TransformOperation.ADD, "10");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> addOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Field not found at path 'user.balance'"));
        assertTrue(exception.getMessage().contains("ADD operation failed"));
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
                new TransformationLoader.Transformation("user.balance", TransformOperation.ADD, "10");

        TransformationException exception = assertThrows(TransformationException.class,
                () -> addOperation.execute(transformation, sourceNode, result));

        assertTrue(exception.getMessage().contains("Field not found") ||
                exception.getMessage().contains("is not an object"));
        assertTrue(exception.getMessage().contains("ADD operation failed"));
    }

    @Test
    void testNoException_ValidOperation() throws Exception {
        String json = """
                {
                    "value": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("value", TransformOperation.ADD, "25");

        // Should not throw any exception
        assertDoesNotThrow(() -> addOperation.execute(transformation, sourceNode, result));

        // Verify the operation was successful
        assertEquals(125.0, result.get("value").asDouble(), 0.001);
    }

    @Test
    void testNoException_AddNegativeNumber() throws Exception {
        String json = """
                {
                    "value": 100
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("value", TransformOperation.ADD, "-30");

        // Should not throw any exception for negative numbers
        assertDoesNotThrow(() -> addOperation.execute(transformation, sourceNode, result));

        assertEquals(70.0, result.get("value").asDouble(), 0.001);
    }

    @Test
    void testNoException_AddZero() throws Exception {
        String json = """
                {
                    "value": 42
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);
        JsonNode sourceNode = objectMapper.readTree("{}");

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("value", TransformOperation.ADD, "0");

        // Should not throw any exception for zero
        assertDoesNotThrow(() -> addOperation.execute(transformation, sourceNode, result));

        assertEquals(42.0, result.get("value").asDouble(), 0.001);
    }
}

