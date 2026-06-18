package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.enums.TransformOperation;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class HashOperationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HashOperation hashOperation = new HashOperation();

    @Test
    void testHash_SimplePassword() throws Exception {
        String json = """
            {
                "password": "mySecretPassword123"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashedPassword", TransformOperation.HASH, "$.password");
        hashOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertTrue(result.has("hashedPassword"));
        assertNotEquals("mySecretPassword123", result.get("hashedPassword").asText());

        assertEquals(64, result.get("hashedPassword").asText().length());

        assertEquals("mySecretPassword123", result.get("password").asText());
    }

    @Test
    void testHash_ReplaceOriginalField() throws Exception {
        String json = """
            {
                "password": "secret123"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.password", TransformOperation.HASH, "$.password");
        hashOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertNotEquals("secret123", result.get("password").asText());
        assertEquals(64, result.get("password").asText().length());
    }

    @Test
    void testHash_ConsistentHashing() throws Exception {
        String json = """
            {
                "value": "testValue"
            }
            """;
        ObjectNode result1 = (ObjectNode) objectMapper.readTree(json);
        ObjectNode result2 = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashed", TransformOperation.HASH, "$.value");

        hashOperation.execute(transformation, objectMapper.readTree("{}"), result1);
        hashOperation.execute(transformation, objectMapper.readTree("{}"), result2);

        assertEquals(result1.get("hashed").asText(), result2.get("hashed").asText());
    }

    @Test
    void testHash_DifferentValuesDifferentHashes() throws Exception {
        String json1 = """
            {
                "value": "password1"
            }
            """;
        String json2 = """
            {
                "value": "password2"
            }
            """;
        ObjectNode result1 = (ObjectNode) objectMapper.readTree(json1);
        ObjectNode result2 = (ObjectNode) objectMapper.readTree(json2);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashed", TransformOperation.HASH, "$.value");

        hashOperation.execute(transformation, objectMapper.readTree("{}"), result1);
        hashOperation.execute(transformation, objectMapper.readTree("{}"), result2);

        assertNotEquals(result1.get("hashed").asText(), result2.get("hashed").asText());
    }

    @Test
    void testHash_NestedField() throws Exception {
        String json = """
            {
                "user": {
                    "credentials": {
                        "password": "secret"
                    }
                }
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.user.hashedPassword", TransformOperation.HASH, "$.user.credentials.password");
        hashOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertTrue(result.at("/user/hashedPassword").isTextual());
        assertEquals(64, result.at("/user/hashedPassword").asText().length());
    }

    @Test
    void testHash_CreateNestedPath() throws Exception {
        String json = """
            {
                "password": "test123"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.security.hashedPassword", TransformOperation.HASH, "$.password");
        hashOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertTrue(result.has("security"));
        assertTrue(result.at("/security/hashedPassword").isTextual());
        assertEquals(64, result.at("/security/hashedPassword").asText().length());
    }

    @Test
    void testHash_WithDollarPrefix() throws Exception {
        String json = """
            {
                "apiKey": "key123456"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashedKey", TransformOperation.HASH, "$.apiKey");
        hashOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertTrue(result.has("hashedKey"));
        assertEquals(64, result.get("hashedKey").asText().length());
    }

    @Test
    void testHash_NumberValue() throws Exception {
        String json = """
            {
                "userId": 12345
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashedId", TransformOperation.HASH, "$.userId");
        hashOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertTrue(result.has("hashedId"));
        assertEquals(64, result.get("hashedId").asText().length());
    }

    @Test
    void testHash_BooleanValue() throws Exception {
        String json = """
            {
                "flag": true
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashedFlag", TransformOperation.HASH, "$.flag");
        hashOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertTrue(result.has("hashedFlag"));
        assertEquals(64, result.get("hashedFlag").asText().length());
    }

    @Test
    void testHash_FromPayload() throws Exception {
        String payload = """
            {
                "inputPassword": "userSecret"
            }
            """;
        ObjectNode resultNode = (ObjectNode) objectMapper.readTree(payload);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.password", TransformOperation.HASH, "$.inputPassword");
        hashOperation.execute(transformation, objectMapper.readTree(payload), resultNode);

        assertTrue(resultNode.has("password"));
        assertEquals(64, resultNode.get("password").asText().length());
    }

    @Test
    void testHash_KnownValue() throws Exception {
        String json = """
            {
                "value": "hello"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashed", TransformOperation.HASH, "$.value");
        hashOperation.execute(transformation, objectMapper.readTree("{}"), result);

        String expectedHash = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824";
        assertEquals(expectedHash, result.get("hashed").asText());
    }

    @Test
    void testHash_LongString() throws Exception {
        String json = """
            {
                "longText": "This is a very long text that should still be hashed properly even though it contains many characters and words."
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashed", TransformOperation.HASH, "$.longText");
        hashOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(64, result.get("hashed").asText().length());
    }

    @Test
    void testHash_SpecialCharacters() throws Exception {
        String json = """
            {
                "text": "P@ssw0rd!#$%^&*()"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.hashed", TransformOperation.HASH, "$.text");
        hashOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertTrue(result.has("hashed"));
        assertEquals(64, result.get("hashed").asText().length());
    }
}

