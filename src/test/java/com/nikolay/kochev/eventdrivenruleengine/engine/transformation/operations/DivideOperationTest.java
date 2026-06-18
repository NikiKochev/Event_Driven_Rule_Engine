package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.enums.TransformOperation;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class DivideOperationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DivideOperation divideOperation = new DivideOperation();

    @Test
    void testDivide_SimpleField() throws Exception {
        String json = """
            {
                "price": 100
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.DIVIDE, "2");
        divideOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(50.0, result.get("price").asDouble(), 0.001);
    }

    @Test
    void testDivide_NestedField() throws Exception {
        String json = """
            {
                "order": {
                    "total": 500.50
                }
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("order.total", TransformOperation.DIVIDE, "2");
        divideOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(250.25, result.at("/order/total").asDouble(), 0.001);
    }

    @Test
    void testDivide_DecimalValues() throws Exception {
        String json = """
            {
                "balance": 1000.0
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("balance", TransformOperation.DIVIDE, "2.5");
        divideOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(400.0, result.get("balance").asDouble(), 0.001);
    }

    @Test
    void testDivide_ResultCanBeFractional() throws Exception {
        String json = """
            {
                "amount": 10
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("amount", TransformOperation.DIVIDE, "3");
        divideOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(3.333333, result.get("amount").asDouble(), 0.001);
    }

    @Test
    void testDivide_WithDollarPrefix() throws Exception {
        String json = """
            {
                "score": 100
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.score", TransformOperation.DIVIDE, "4");
        divideOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(25.0, result.get("score").asDouble(), 0.001);
    }

    @Test
    void testDivide_MultipleOperations() throws Exception {
        String json = """
            {
                "counter": 1000
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        divideOperation.execute(
                new TransformationLoader.Transformation("counter", TransformOperation.DIVIDE, "2"),
                objectMapper.readTree("{}"),
                result
        );
        divideOperation.execute(
                new TransformationLoader.Transformation("counter", TransformOperation.DIVIDE, "5"),
                objectMapper.readTree("{}"),
                result
        );
        divideOperation.execute(
                new TransformationLoader.Transformation("counter", TransformOperation.DIVIDE, "10"),
                objectMapper.readTree("{}"),
                result
        );

        assertEquals(10.0, result.get("counter").asDouble(), 0.001);
    }

    @Test
    void testDivide_ByOne() throws Exception {
        String json = """
            {
                "value": 42
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("value", TransformOperation.DIVIDE, "1");
        divideOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(42.0, result.get("value").asDouble(), 0.001);
    }

    @Test
    void testDivide_NegativeNumber() throws Exception {
        String json = """
            {
                "amount": -100
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("amount", TransformOperation.DIVIDE, "2");
        divideOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(-50.0, result.get("amount").asDouble(), 0.001);
    }

    @Test
    void testDivide_ByNegativeNumber() throws Exception {
        String json = """
            {
                "value": 100
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("value", TransformOperation.DIVIDE, "-4");
        divideOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(-25.0, result.get("value").asDouble(), 0.001);
    }

    @Test
    void testDivide_NegativeByNegative() throws Exception {
        String json = """
            {
                "score": -100
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("score", TransformOperation.DIVIDE, "-2");
        divideOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(50.0, result.get("score").asDouble(), 0.001);
    }

    @Test
    void testDivide_DeepNestedPath() throws Exception {
        String json = """
            {
                "data": {
                    "metrics": {
                        "performance": {
                            "score": 1000.0
                        }
                    }
                }
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("data.metrics.performance.score", TransformOperation.DIVIDE, "10");
        divideOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(100.0, result.at("/data/metrics/performance/score").asDouble(), 0.001);
    }

    @Test
    void testDivide_SmallDivisor() throws Exception {
        String json = """
            {
                "price": 100
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.DIVIDE, "0.5");
        divideOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(200.0, result.get("price").asDouble(), 0.001);
    }

    @Test
    void testDivide_VerySmallResult() throws Exception {
        String json = """
            {
                "value": 1
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("value", TransformOperation.DIVIDE, "1000");
        divideOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(0.001, result.get("value").asDouble(), 0.0001);
    }
}

