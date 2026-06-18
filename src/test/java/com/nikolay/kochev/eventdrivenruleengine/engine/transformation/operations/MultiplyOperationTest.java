package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.enums.TransformOperation;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiplyOperationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MultiplyOperation multiplyOperation = new MultiplyOperation();

    @Test
    void testMultiply_SimpleField() throws Exception {
        String json = """
            {
                "price": 100
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.MULTIPLY, "2");
        multiplyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(200.0, result.get("price").asDouble(), 0.001);
    }

    @Test
    void testMultiply_NestedField() throws Exception {
        String json = """
            {
                "order": {
                    "total": 50.50
                }
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("order.total", TransformOperation.MULTIPLY, "2");
        multiplyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(101.0, result.at("/order/total").asDouble(), 0.001);
    }

    @Test
    void testMultiply_DecimalValues() throws Exception {
        String json = """
            {
                "balance": 123.45
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("balance", TransformOperation.MULTIPLY, "2.5");
        multiplyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(308.625, result.get("balance").asDouble(), 0.001);
    }

    @Test
    void testMultiply_ByZero() throws Exception {
        String json = """
            {
                "amount": 999
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("amount", TransformOperation.MULTIPLY, "0");
        multiplyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(0.0, result.get("amount").asDouble(), 0.001);
    }

    @Test
    void testMultiply_ByNegativeNumber() throws Exception {
        String json = """
            {
                "value": 50
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("value", TransformOperation.MULTIPLY, "-2");
        multiplyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(-100.0, result.get("value").asDouble(), 0.001);
    }

    @Test
    void testMultiply_NegativeByNegative() throws Exception {
        String json = """
            {
                "score": -10
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("score", TransformOperation.MULTIPLY, "-3");
        multiplyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(30.0, result.get("score").asDouble(), 0.001);
    }

    @Test
    void testMultiply_WithDollarPrefix() throws Exception {
        String json = """
            {
                "score": 100
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.score", TransformOperation.MULTIPLY, "1.5");
        multiplyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(150.0, result.get("score").asDouble(), 0.001);
    }

    @Test
    void testMultiply_MultipleOperations() throws Exception {
        String json = """
            {
                "counter": 10
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        multiplyOperation.execute(
                new TransformationLoader.Transformation("counter", TransformOperation.MULTIPLY, "2"),
                objectMapper.readTree("{}"),
                result
        );
        multiplyOperation.execute(
                new TransformationLoader.Transformation("counter", TransformOperation.MULTIPLY, "3"),
                objectMapper.readTree("{}"),
                result
        );
        multiplyOperation.execute(
                new TransformationLoader.Transformation("counter", TransformOperation.MULTIPLY, "5"),
                objectMapper.readTree("{}"),
                result
        );

        assertEquals(300.0, result.get("counter").asDouble(), 0.001);
    }

    @Test
    void testMultiply_ByOne() throws Exception {
        String json = """
            {
                "value": 42
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("value", TransformOperation.MULTIPLY, "1");
        multiplyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(42.0, result.get("value").asDouble(), 0.001);
    }

    @Test
    void testMultiply_DeepNestedPath() throws Exception {
        String json = """
            {
                "data": {
                    "metrics": {
                        "performance": {
                            "score": 95.5
                        }
                    }
                }
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("data.metrics.performance.score", TransformOperation.MULTIPLY, "2");
        multiplyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(191.0, result.at("/data/metrics/performance/score").asDouble(), 0.001);
    }

    @Test
    void testMultiply_SmallDecimalMultiplier() throws Exception {
        String json = """
            {
                "price": 100
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.MULTIPLY, "0.75");
        multiplyOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(75.0, result.get("price").asDouble(), 0.001);
    }
}

