package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.enums.TransformOperation;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RemoveOperation_ObjectFieldTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RemoveOperation removeOperation = new RemoveOperation();

    @Test
    void testRemove_ObjectWithSubfields_RemovesEntireObject() throws Exception {
        String json = """
            {
                "id": "123",
                "user": {
                    "name": "Alice",
                    "email": "alice@example.com",
                    "address": {
                        "city": "New York",
                        "country": "USA"
                    },
                    "age": 25
                },
                "status": "active"
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        assertTrue(result.has("user"));
        assertTrue(result.get("user").has("name"));
        assertTrue(result.get("user").has("email"));
        assertTrue(result.get("user").has("address"));
        assertTrue(result.get("user").has("age"));

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("user", TransformOperation.REMOVE, null);
        removeOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertFalse(result.has("user"), "User object should be completely removed");

        assertTrue(result.has("id"));
        assertTrue(result.has("status"));

        assertEquals("123", result.get("id").asText());
        assertEquals("active", result.get("status").asText());
        assertEquals(2, result.size()); // Only 2 fields remain
    }

    @Test
    void testRemove_NestedObjectWithSubfields_RemovesEntireNestedObject() throws Exception {
        String json = """
            {
                "user": {
                    "name": "Alice",
                    "address": {
                        "street": "123 Main St",
                        "city": "New York",
                        "country": "USA",
                        "postalCode": "10001"
                    },
                    "phone": "123-456-7890"
                }
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("user.address", TransformOperation.REMOVE, null);
        removeOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertFalse(result.get("user").has("address"), "Address object should be completely removed");

        assertTrue(result.get("user").has("name"));
        assertTrue(result.get("user").has("phone"));

        assertEquals(2, result.get("user").size()); // Only 2 fields remain in user
    }

    @Test
    void testRemove_DeepNestedObjectWithManySubfields() throws Exception {
        String json = """
            {
                "company": {
                    "name": "Acme Corp",
                    "employees": {
                        "developers": {
                            "count": 50,
                            "languages": ["Java", "Python", "JavaScript"],
                            "teams": {
                                "backend": 30,
                                "frontend": 20
                            }
                        },
                        "managers": 10
                    },
                    "revenue": 1000000
                }
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("company.employees", TransformOperation.REMOVE, null);
        removeOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertFalse(result.get("company").has("employees"), "Employees object should be completely removed");

        assertTrue(result.get("company").has("name"));
        assertTrue(result.get("company").has("revenue"));

        assertEquals(2, result.get("company").size());
    }

    @Test
    void testRemove_ArrayField_RemovesEntireArray() throws Exception {
        String json = """
            {
                "name": "Alice",
                "hobbies": ["reading", "coding", "gaming"],
                "age": 25
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("hobbies", TransformOperation.REMOVE, null);
        removeOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertFalse(result.has("hobbies"), "Hobbies array should be completely removed");

        assertEquals(2, result.size());
    }
}

