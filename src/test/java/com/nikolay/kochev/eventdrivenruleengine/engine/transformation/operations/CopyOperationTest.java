package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.enums.TransformOperation;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class CopyOperationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CopyOperation copyOperation = new CopyOperation();

    @Test
    void testCopy_SimpleField() throws Exception {
        String json = """
            {
                "name": "Alice"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.nameCopy", TransformOperation.COPY, "$.name");
        copyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals("Alice", result.get("nameCopy").asText());
        assertEquals("Alice", result.get("name").asText()); // Original still exists
    }

    @Test
    void testCopy_ToExistingField() throws Exception {
        String json = """
            {
                "source": "newValue",
                "target": "oldValue"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.target", TransformOperation.COPY, "$.source");
        copyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        // Target should be replaced with source value
        assertEquals("newValue", result.get("target").asText());
        assertEquals("newValue", result.get("source").asText());
    }

    @Test
    void testCopy_NestedField() throws Exception {
        String json = """
            {
                "user": {
                    "email": "alice@example.com"
                }
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.contactEmail", TransformOperation.COPY, "$.user.email");
        copyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals("alice@example.com", result.get("contactEmail").asText());
    }

    @Test
    void testCopy_ToNestedPath() throws Exception {
        String json = """
            {
                "username": "alice123"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.backup.username", TransformOperation.COPY, "$.username");
        copyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals("alice123", result.at("/backup/username").asText());
    }

    @Test
    void testCopy_NumberValue() throws Exception {
        String json = """
            {
                "price": 99.99
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.originalPrice", TransformOperation.COPY, "$.price");
        copyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(99.99, result.get("originalPrice").asDouble(), 0.001);
    }

    @Test
    void testCopy_BooleanValue() throws Exception {
        String json = """
            {
                "active": true
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.status", TransformOperation.COPY, "$.active");
        copyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertTrue(result.get("status").asBoolean());
    }

    @Test
    void testCopy_NullValue() throws Exception {
        String json = """
            {
                "field": null,
                "target": "existingValue"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.target", TransformOperation.COPY, "$.field");
        copyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        // Should copy null and replace existing value
        assertTrue(result.has("target"));
        assertTrue(result.get("target").isNull());
    }

    @Test
    void testCopy_EmptyString() throws Exception {
        String json = """
            {
                "field": "",
                "target": "existingValue"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.target", TransformOperation.COPY, "$.field");
        copyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        // Should copy empty string and replace existing value
        assertEquals("", result.get("target").asText());
    }

    @Test
    void testCopy_ObjectValue() throws Exception {
        String json = """
            {
                "user": {
                    "name": "Alice",
                    "age": 30
                }
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.userBackup", TransformOperation.COPY, "$.user");
        copyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertTrue(result.get("userBackup").isObject());
        assertEquals("Alice", result.at("/userBackup/name").asText());
        assertEquals(30, result.at("/userBackup/age").asInt());
    }

    @Test
    void testCopy_ArrayValue() throws Exception {
        String json = """
            {
                "tags": ["java", "spring", "json"]
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.tagsCopy", TransformOperation.COPY, "$.tags");
        copyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertTrue(result.get("tagsCopy").isArray());
        assertEquals(3, result.get("tagsCopy").size());
    }

    @Test
    void testCopy_FromPayload() throws Exception {
        String payload = """
            {
                "requestId": "REQ-123"
            }
            """;
        String result = """
            {
                "status": "processing"
            }
            """;
        ObjectNode resultNode = (ObjectNode) objectMapper.readTree(result);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.id", TransformOperation.COPY, "$.requestId");
        copyOperation.execute(transformation, objectMapper.readTree(payload), resultNode);

        assertEquals("REQ-123", resultNode.get("id").asText());
    }

    @Test
    void testCopy_SourceMissing_DoesNothing() throws Exception {
        String json = """
            {
                "other": "value"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.target", TransformOperation.COPY, "$.missing");
        copyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        // Should not create target field (safe operation)
        assertFalse(result.has("target"));
    }

    @Test
    void testCopy_SourceMissing_PreservesExistingTarget() throws Exception {
        String json = """
            {
                "target": "existingValue"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.target", TransformOperation.COPY, "$.missing");
        copyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        // Should preserve existing value (safe operation)
        assertEquals("existingValue", result.get("target").asText());
    }

    @Test
    void testCopy_NullSourcePath_DoesNothing() throws Exception {
        String json = """
            {
                "field": "value"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.target", TransformOperation.COPY, null);
        copyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        // Should not throw, just do nothing (safe operation)
        assertFalse(result.has("target"));
    }

    @Test
    void testCopy_NullTargetPath_DoesNothing() throws Exception {
        String json = """
            {
                "field": "value"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation(null, TransformOperation.COPY, "$.field");
        copyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        // Should not throw, just do nothing (safe operation)
        assertDoesNotThrow(() -> copyOperation.execute(transformation, objectMapper.readTree("{}"), result));
    }

    @Test
    void testCopy_DeepNestedPath() throws Exception {
        String json = """
            {
                "data": {
                    "user": {
                        "profile": {
                            "email": "alice@example.com"
                        }
                    }
                }
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.backup.contact.email", TransformOperation.COPY, "$.data.user.profile.email");
        copyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals("alice@example.com", result.at("/backup/contact/email").asText());
    }

    @Test
    void testCopy_WithDollarPrefix() throws Exception {
        String json = """
            {
                "name": "Bob"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.fullName", TransformOperation.COPY, "$.name");
        copyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals("Bob", result.get("fullName").asText());
    }

    @Test
    void testCopy_SameField() throws Exception {
        String json = """
            {
                "field": "value"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        // Copy field to itself (should work, no change)
        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.field", TransformOperation.COPY, "$.field");
        copyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals("value", result.get("field").asText());
    }
}

