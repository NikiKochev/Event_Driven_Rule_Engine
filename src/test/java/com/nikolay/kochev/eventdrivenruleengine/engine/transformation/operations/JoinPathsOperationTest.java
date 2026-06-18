package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import org.junit.jupiter.api.Test;

import static com.nikolay.kochev.eventdrivenruleengine.engine.enums.TransformOperation.JOIN_PATHS;
import static org.junit.jupiter.api.Assertions.*;

class JoinPathsOperationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JoinPathsOperation joinPathsOperation = new JoinPathsOperation();

    @Test
    void testJoinPaths_TwoSimpleFields() throws Exception {
        String json = """
                {
                    "userId": "12345",
                    "action": "view"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.joinedFields", JOIN_PATHS, "$.userId$action");
        joinPathsOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals("12345view", result.get("joinedFields").asText());
    }

    @Test
    void testJoinPaths_ThreeFields() throws Exception {
        String json = """
                {
                    "userId": "12345",
                    "resource": "documents",
                    "action": "view"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.fullPath", JOIN_PATHS, "$.userId$resource$action");
        joinPathsOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals("12345documentsview", result.get("fullPath").asText());
    }

    @Test
    void testJoinPaths_WithNumbers() throws Exception {
        String json = """
                {
                    "id": 999,
                    "code": "ABC",
                    "version": 3
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.combined", JOIN_PATHS, "$.code$id$version");
        joinPathsOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals("ABC9993", result.get("combined").asText());
    }

    @Test
    void testJoinPaths_WithBoolean() throws Exception {
        String json = """
                {
                    "prefix": "status",
                    "active": true,
                    "suffix": "end"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.result", JOIN_PATHS, "$.prefix$active$suffix");
        joinPathsOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals("statustrueend", result.get("result").asText());
    }

    @Test
    void testJoinPaths_MissingFieldSkipped() throws Exception {
        String json = """
                {
                    "userId": "12345",
                    "action": "view"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        // resource is missing, should be skipped
        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.joined", JOIN_PATHS, "$.userId$resource$action");
        joinPathsOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals("12345view", result.get("joined").asText());
    }

    @Test
    void testJoinPaths_AllFieldsMissing() throws Exception {
        String json = """
                {
                    "other": "data"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.joined", JOIN_PATHS, "$.missing1$missing2$missing3");
        joinPathsOperation.execute(transformation, objectMapper.readTree("{}"), result);

        // Should create field with empty string
        assertTrue(result.has("joined"));
        assertEquals("", result.get("joined").asText());
    }

    @Test
    void testJoinPaths_NullFieldSkipped() throws Exception {
        String json = """
                {
                    "field1": "hello",
                    "field2": null,
                    "field3": "world"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.result", JOIN_PATHS, "$.field1$field2$field3");
        joinPathsOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals("helloworld", result.get("result").asText());
    }

    @Test
    void testJoinPaths_NestedFields() throws Exception {
        String json = """
                {
                    "user": {
                        "id": "123",
                        "name": "Alice"
                    },
                    "action": "login"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.log", JOIN_PATHS, "$.user.id$user.name$action");
        joinPathsOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals("123Alicelogin", result.get("log").asText());
    }

    @Test
    void testJoinPaths_ReplaceExistingField() throws Exception {
        String json = """
                {
                    "userId": "12345",
                    "action": "view",
                    "joined": "oldValue"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.joined", JOIN_PATHS, "$.userId$action");
        joinPathsOperation.execute(transformation, objectMapper.readTree("{}"), result);

        // Should replace old value
        assertEquals("12345view", result.get("joined").asText());
    }

    @Test
    void testJoinPaths_CreateNestedPath() throws Exception {
        String json = """
                {
                    "field1": "hello",
                    "field2": "world"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.output.result", JOIN_PATHS, "$.field1$field2");
        joinPathsOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals("helloworld", result.at("/output/result").asText());
    }

    @Test
    void testJoinPaths_WithDollarPrefix() throws Exception {
        String json = """
                {
                    "a": "foo",
                    "b": "bar"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.result", JOIN_PATHS, "$.a$b");
        joinPathsOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals("foobar", result.get("result").asText());
    }

    @Test
    void testJoinPaths_SingleField() throws Exception {
        String json = """
                {
                    "name": "Alice"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.output", JOIN_PATHS, "$.name");
        joinPathsOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals("Alice", result.get("output").asText());
    }

    @Test
    void testJoinPaths_FromPayload() throws Exception {
        String payload = """
                {
                    "requestId": "REQ-999",
                    "timestamp": "2024-01-01"
                }
                """;
        String result = """
                {
                    "status": "processing"
                }
                """;
        ObjectNode resultNode = (ObjectNode) objectMapper.readTree(result);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.logEntry", JOIN_PATHS, "$.requestId$timestamp$status");
        joinPathsOperation.execute(transformation, objectMapper.readTree(payload), resultNode);

        // Should get requestId and timestamp from payload, status from result
        assertEquals("REQ-9992024-01-01processing", resultNode.get("logEntry").asText());
    }

    @Test
    void testJoinPaths_MixedDecimals() throws Exception {
        String json = """
                {
                    "price": 99.99,
                    "tax": 5.5,
                    "total": 105.49
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.summary", JOIN_PATHS, "$.price$tax$total");
        joinPathsOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals("99.995.5105.49", result.get("summary").asText());
    }

    @Test
    void testJoinPaths_EmptyPartsString() throws Exception {
        String json = """
                {
                    "field": "value"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.output", JOIN_PATHS, "");
        joinPathsOperation.execute(transformation, objectMapper.readTree("{}"), result);

        // Should not create field (safe operation)
        assertFalse(result.has("output"));
    }

    @Test
    void testJoinPaths_NullPath() throws Exception {
        String json = """
                {
                    "field": "value"
                }
                """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation(null, JOIN_PATHS, "$.field");
        joinPathsOperation.execute(transformation, objectMapper.readTree("{}"), result);

        // Should not throw, safe operation
        assertDoesNotThrow(() -> joinPathsOperation.execute(transformation, objectMapper.readTree("{}"), result));
    }
}

