package com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.enums.TransformOperation;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SUBTRACT operation
 */
class SubtractOperationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SubtractOperation subtractOperation = new SubtractOperation();

    @Test
    void testSubtract_SimpleField() throws Exception {
        String json = """
            {
                "price": 100
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("price", TransformOperation.SUBTRACT, "20");
        subtractOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(80.0, result.get("price").asDouble(), 0.001);
    }

    @Test
    void testSubtract_NestedField() throws Exception {
        String json = """
            {
                "order": {
                    "total": 500.50
                }
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("order.total", TransformOperation.SUBTRACT, "100.50");
        subtractOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(400.0, result.at("/order/total").asDouble(), 0.001);
    }

    @Test
    void testSubtract_DecimalValues() throws Exception {
        String json = """
            {
                "balance": 1234.56
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("balance", TransformOperation.SUBTRACT, "234.56");
        subtractOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(1000.0, result.get("balance").asDouble(), 0.001);
    }

    @Test
    void testSubtract_ResultCanBeNegative() throws Exception {
        String json = """
            {
                "amount": 10
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("amount", TransformOperation.SUBTRACT, "50");
        subtractOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(-40.0, result.get("amount").asDouble(), 0.001);
    }

    @Test
    void testSubtract_WithDollarPrefix() throws Exception {
        String json = """
            {
                "score": 100
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("$.score", TransformOperation.SUBTRACT, "15");
        subtractOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(85.0, result.get("score").asDouble(), 0.001);
    }

    @Test
    void testSubtract_MultipleOperations() throws Exception {
        String json = """
            {
                "counter": 1000
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        subtractOperation.execute(
                new TransformationLoader.Transformation("counter", TransformOperation.SUBTRACT, "100"),
                objectMapper.readTree("{}"),
                result
        );
        subtractOperation.execute(
                new TransformationLoader.Transformation("counter", TransformOperation.SUBTRACT, "50"),
                objectMapper.readTree("{}"),
                result
        );
        subtractOperation.execute(
                new TransformationLoader.Transformation("counter", TransformOperation.SUBTRACT, "25"),
                objectMapper.readTree("{}"),
                result
        );

        assertEquals(825.0, result.get("counter").asDouble(), 0.001);
    }

    @Test
    void testSubtract_ZeroValue() throws Exception {
        String json = """
            {
                "value": 42
            }
            """;
        ObjectNode result = (ObjectNode) objectMapper.readTree(json);

        TransformationLoader.Transformation transformation =
                new TransformationLoader.Transformation("value", TransformOperation.SUBTRACT, "0");
        subtractOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(42.0, result.get("value").asDouble(), 0.001);
    }

    @Test
    void testSubtract_DeepNestedPath() throws Exception {
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
                new TransformationLoader.Transformation("data.metrics.performance.score", TransformOperation.SUBTRACT, "10.5");
        subtractOperation.execute(transformation, objectMapper.readTree("{}"), result);

        assertEquals(85.0, result.at("/data/metrics/performance/score").asDouble(), 0.001);
    }
}

