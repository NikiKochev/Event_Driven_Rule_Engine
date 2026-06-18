package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.enums.TransformOperation;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddOperationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AddOperation addOperation = new AddOperation();

    @Test
    void testAdd_SimpleField() throws Exception {
        String json = """
            {
                "counter": 10
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("counter", TransformOperation.ADD, "5");
        addOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(15.0, result.get("counter").asDouble(), 0.001);
    }

    @Test
    void testAdd_NestedField() throws Exception {
        String json = """
            {
                "order": {
                    "total": 100.0
                }
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("order.total", TransformOperation.ADD, "50");
        addOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(150.0, result.at("/order/total").asDouble(), 0.001);
    }

    @Test
    void testAdd_DecimalValues() throws Exception {
        String json = """
            {
                "balance": 100.50
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("balance", TransformOperation.ADD, "25.25");
        addOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(125.75, result.get("balance").asDouble(), 0.001);
    }

    @Test
    void testAdd_NegativeNumber() throws Exception {
        String json = """
            {
                "value": 50
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("value", TransformOperation.ADD, "-20");
        addOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(30.0, result.get("value").asDouble(), 0.001);
    }

    @Test
    void testAdd_ToNegativeNumber() throws Exception {
        String json = """
            {
                "balance": -50
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("balance", TransformOperation.ADD, "30");
        addOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(-20.0, result.get("balance").asDouble(), 0.001);
    }

    @Test
    void testAdd_NegativeToNegative() throws Exception {
        String json = """
            {
                "debt": -100
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("debt", TransformOperation.ADD, "-50");
        addOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(-150.0, result.get("debt").asDouble(), 0.001);
    }

    @Test
    void testAdd_Zero() throws Exception {
        String json = """
            {
                "value": 42
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("value", TransformOperation.ADD, "0");
        addOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(42.0, result.get("value").asDouble(), 0.001);
    }

    @Test
    void testAdd_WithDollarPrefix() throws Exception {
        String json = """
            {
                "score": 100
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.score", TransformOperation.ADD, "50");
        addOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(150.0, result.get("score").asDouble(), 0.001);
    }

    @Test
    void testAdd_MultipleOperations() throws Exception {
        String json = """
            {
                "sum": 0
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        addOperation.execute(
                new TransformationLoader.Transformation("sum", TransformOperation.ADD, "10"),
                objectMapper.readTree("{}"),
                result
        );
        addOperation.execute(
                new TransformationLoader.Transformation("sum", TransformOperation.ADD, "20"),
                objectMapper.readTree("{}"),
                result
        );
        addOperation.execute(
                new TransformationLoader.Transformation("sum", TransformOperation.ADD, "30"),
                objectMapper.readTree("{}"),
                result
        );

        assertEquals(60.0, result.get("sum").asDouble(), 0.001);
    }

    @Test
    void testAdd_LargeNumbers() throws Exception {
        String json = """
            {
                "value": 999999
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("value", TransformOperation.ADD, "1");
        addOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(1000000.0, result.get("value").asDouble(), 0.001);
    }

    @Test
    void testAdd_VerySmallNumbers() throws Exception {
        String json = """
            {
                "value": 0.001
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("value", TransformOperation.ADD, "0.002");
        addOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(0.003, result.get("value").asDouble(), 0.0001);
    }

    @Test
    void testAdd_DeepNestedPath() throws Exception {
        String json = """
            {
                "data": {
                    "metrics": {
                        "performance": {
                            "score": 75.5
                        }
                    }
                }
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("data.metrics.performance.score", TransformOperation.ADD, "10.5");
        addOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(86.0, result.at("/data/metrics/performance/score").asDouble(), 0.001);
    }

    @Test
    void testAdd_ResultCanBePositiveFromNegative() throws Exception {
        String json = """
            {
                "balance": -20
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("balance", TransformOperation.ADD, "50");
        addOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(30.0, result.get("balance").asDouble(), 0.001);
    }
}

