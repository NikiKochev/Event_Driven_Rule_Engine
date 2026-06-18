package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.enums.TransformOperation;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RemoveOperationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RemoveOperation removeOperation = new RemoveOperation();

    @Test
    void testRemove_SimpleField() throws Exception {
        String json = """
            {
                "name": "Alice",
                "age": 25
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("age", TransformOperation.REMOVE, null);
        removeOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertTrue(result.has("name"), "Field 'name' should still exist");
        assertFalse(result.has("age"), "Field 'age' should be removed");
        assertEquals("Alice", result.get("name").asText());
    }

    @Test
    void testRemove_NestedField() throws Exception {
        String json = """
            {
                "user": {
                    "name": "Alice",
                    "email": "alice@example.com",
                    "age": 25
                }
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("user.age", TransformOperation.REMOVE, null);
        removeOperation.execute(transformation, objectMapper.readTree("{}"), result);

        // Then: user.age should be removed, others remain
        assertTrue(result.has("user"));
        assertTrue(result.get("user").has("name"));
        assertTrue(result.get("user").has("email"));
        assertFalse(result.get("user").has("age"), "Field 'user.age' should be removed");
    }

    @Test
    void testRemove_WithDollarPrefix() throws Exception {
        String json = """
            {
                "status": "active",
                "count": 10
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.status", TransformOperation.REMOVE, null);
        removeOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertFalse(result.has("status"), "Field 'status' should be removed");
        assertTrue(result.has("count"), "Field 'count' should remain");
    }

    @Test
    void testRemove_MissingField_DoesNothing() throws Exception {
        String json = """
            {
                "name": "Alice",
                "age": 25
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("email", TransformOperation.REMOVE, null);

        assertDoesNotThrow(() -> removeOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(result.has("name"));
        assertTrue(result.has("age"));
        assertEquals(2, result.size());
    }

    @Test
    void testRemove_MissingNestedField_DoesNothing() throws Exception {
        String json = """
            {
                "user": {
                    "name": "Alice"
                }
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("user.email", TransformOperation.REMOVE, null);

        assertDoesNotThrow(() -> removeOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(result.has("user"));
        assertTrue(result.get("user").has("name"));
        assertEquals(1, result.get("user").size());
    }

    @Test
    void testRemove_MissingIntermediatePath_DoesNothing() throws Exception {
        String json = """
            {
                "name": "Alice"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("user.email", TransformOperation.REMOVE, null);

        assertDoesNotThrow(() -> removeOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(result.has("name"));
        assertFalse(result.has("user"));
        assertEquals(1, result.size());
    }

    @Test
    void testRemove_DeepNestedPath() throws Exception {
        String json = """
            {
                "data": {
                    "user": {
                        "profile": {
                            "contact": {
                                "phone": "123-456-7890",
                                "email": "user@example.com"
                            }
                        }
                    }
                }
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("data.user.profile.contact.phone", TransformOperation.REMOVE, null);
        removeOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertFalse(result.at("/data/user/profile/contact").has("phone"), "Phone should be removed");
        assertTrue(result.at("/data/user/profile/contact").has("email"), "Email should remain");
    }

    @Test
    void testRemove_MultipleFields() throws Exception {
        String json = """
            {
                "name": "Alice",
                "age": 25,
                "email": "alice@example.com",
                "phone": "123-456-7890"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        removeOperation.execute(
                new TransformationLoader.Transformation("age", TransformOperation.REMOVE, null),
                objectMapper.readTree("{}"),
                result
        );
        removeOperation.execute(
                new TransformationLoader.Transformation("phone", TransformOperation.REMOVE, null),
                objectMapper.readTree("{}"),
                result
        );

        assertTrue(result.has("name"));
        assertTrue(result.has("email"));
        assertFalse(result.has("age"));
        assertFalse(result.has("phone"));
        assertEquals(2, result.size());
    }

    @Test
    void testRemove_AllFieldsFromObject() throws Exception {
        String json = """
            {
                "user": {
                    "name": "Alice",
                    "age": 25
                }
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        removeOperation.execute(
                new TransformationLoader.Transformation("user.name", TransformOperation.REMOVE, null),
                objectMapper.readTree("{}"),
                result
        );
        removeOperation.execute(
                new TransformationLoader.Transformation("user.age", TransformOperation.REMOVE, null),
                objectMapper.readTree("{}"),
                result
        );

        assertTrue(result.has("user"));
        assertTrue(result.get("user").isObject());
        assertEquals(0, result.get("user").size());
    }

    @Test
    void testRemove_SameFieldTwice_NoError() throws Exception {
        String json = """
            {
                "name": "Alice",
                "age": 25
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("age", TransformOperation.REMOVE, null);

        removeOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertDoesNotThrow(() -> removeOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(result.has("name"));
        assertFalse(result.has("age"));
    }

    @Test
    void testSafe_NullPath_DoesNothing() throws Exception {
        String json = """
            {
                "name": "Alice"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation(null, TransformOperation.REMOVE, null);

        assertDoesNotThrow(() -> removeOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(result.has("name"));
        assertEquals("Alice", result.get("name").asText());
    }

    @Test
    void testSafe_EmptyPath_DoesNothing() throws Exception {
        String json = """
            {
                "name": "Alice"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("", TransformOperation.REMOVE, null);

        assertDoesNotThrow(() -> removeOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(result.has("name"));
        assertEquals("Alice", result.get("name").asText());
    }

    @Test
    void testSafe_NullResult_DoesNothing() {
        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("name", TransformOperation.REMOVE, null);

        assertDoesNotThrow(() -> removeOperation.execute(transformation, objectMapper.readTree("{}"), null));
    }

    @Test
    void testSafe_IntermediatePathNotObject_DoesNothing() throws Exception {
        String json = """
            {
                "user": "Alice",
                "age": 25
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("user.email", TransformOperation.REMOVE, null);

        assertDoesNotThrow(() -> removeOperation.execute(transformation, objectMapper.readTree("{}"), result));

        assertTrue(result.has("user"));
        assertTrue(result.has("age"));
        assertEquals("Alice", result.get("user").asText());
        assertEquals(25, result.get("age").asInt());
    }
}



