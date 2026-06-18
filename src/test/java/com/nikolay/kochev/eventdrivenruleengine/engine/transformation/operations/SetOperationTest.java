package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.enums.TransformOperation;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SetOperationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SetOperation setOperation = new SetOperation();

    @Test
    void testCreateNewField_Simple() throws Exception {
        String json = """
            {}
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("username", TransformOperation.SET, "john_doe");
        setOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertTrue(result.has("username"), "Field 'username' should exist");
        assertEquals("john_doe", result.get("username").asText());
    }

    @Test
    void testCreateNewField_Nested() throws Exception {
        String json = """
            {}
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("user.settings.theme", TransformOperation.SET, "dark");
        setOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertTrue(result.has("user"));
        assertTrue(result.get("user").has("settings"));
        assertTrue(result.get("user").get("settings").has("theme"));
        assertEquals("dark", result.at("/user/settings/theme").asText());
    }

    @Test
    void testReplaceExistingField_Simple() throws Exception {
        String json = """
            {
                "status": "pending"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        assertEquals("pending", result.get("status").asText());

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("status", TransformOperation.SET, "completed");
        setOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals("completed", result.get("status").asText());
    }

    @Test
    void testReplaceExistingField_Nested() throws Exception {
        String json = """
            {
                "user": {
                    "email": "old@example.com"
                }
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        assertEquals("old@example.com", result.at("/user/email").asText());

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("user.email", TransformOperation.SET, "new@example.com");
        setOperation.execute(transformation, objectMapper.readTree("{}"), result);


        assertEquals("new@example.com", result.at("/user/email").asText());
    }

    @Test
    void testReplaceExistingField_WithDifferentType() throws Exception {
        String json = """
            {
                "count": 42
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("count", TransformOperation.SET, "unlimited");
        setOperation.execute(transformation, objectMapper.readTree("{}"), result);

        // Then: Field should be replaced with string
        assertEquals("unlimited", result.get("count").asText());
    }

    @Test
    void testCreateAndReplace_Multiple() throws Exception {
        String json = """
            {
                "existing": "original_value"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        setOperation.execute(
                new TransformationLoader.Transformation("newField", TransformOperation.SET, "new_value"),
                objectMapper.readTree("{}"),
                result
        );

        setOperation.execute(
                new TransformationLoader.Transformation("existing", TransformOperation.SET, "updated_value"),
                objectMapper.readTree("{}"),
                result
        );

        assertEquals("new_value", result.get("newField").asText());
        assertEquals("updated_value", result.get("existing").asText());
    }
}

