package com.nikolay.kochev.eventdrivenruleengine.engine.condition.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.kochev.eventdrivenruleengine.engine.enums.ConditionOperator;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.RulesLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnyConditionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AnyCondition anyCondition = new AnyCondition();

    @Test
    void testAnyCondition_FirstRulePasses() throws Exception {
        String json = """
            {
                "status": "active",
                "type": "basic"
            }
            """;
        JsonNode context = objectMapper.readTree(json);

        List<RulesLoader.Rule> rules = List.of(
            new RulesLoader.Rule("status", ConditionOperator.EQUALS, "active"),  // PASSES
            new RulesLoader.Rule("type", ConditionOperator.EQUALS, "premium")     // FAILS
        );

        assertDoesNotThrow(() -> anyCondition.execute(context, rules));
    }

    @Test
    void testAnyCondition_LastRulePasses() throws Exception {
        String json = """
            {
                "status": "pending",
                "type": "premium"
            }
            """;
        JsonNode context = objectMapper.readTree(json);

        List<RulesLoader.Rule> rules = List.of(
            new RulesLoader.Rule("status", ConditionOperator.EQUALS, "active"),   // FAILS
            new RulesLoader.Rule("type", ConditionOperator.EQUALS, "basic"),      // FAILS
            new RulesLoader.Rule("type", ConditionOperator.EQUALS, "premium")     // PASSES
        );

        assertDoesNotThrow(() -> anyCondition.execute(context, rules));
    }

    @Test
    void testAnyCondition_AllRulesFail() throws Exception {
        String json = """
            {
                "status": "inactive",
                "type": "basic"
            }
            """;
        JsonNode context = objectMapper.readTree(json);

        List<RulesLoader.Rule> rules = List.of(
            new RulesLoader.Rule("status", ConditionOperator.EQUALS, "active"),
            new RulesLoader.Rule("type", ConditionOperator.EQUALS, "premium"),
            new RulesLoader.Rule("type", ConditionOperator.EQUALS, "gold")
        );

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> anyCondition.execute(context, rules));
        assertTrue(exception.getMessage().contains("All rules failed"));
    }

    @Test
    void testAnyCondition_OneOfMany() throws Exception {
        String json = """
            {
                "email": "user@example.com",
                "domain": "other.com"
            }
            """;
        JsonNode context = objectMapper.readTree(json);

        List<RulesLoader.Rule> rules = List.of(
            new RulesLoader.Rule("email", ConditionOperator.CONTAINS, "@gmail.com"),
            new RulesLoader.Rule("email", ConditionOperator.CONTAINS, "@example.com"),  // PASSES
            new RulesLoader.Rule("email", ConditionOperator.CONTAINS, "@yahoo.com")
        );

        assertDoesNotThrow(() -> anyCondition.execute(context, rules));
    }

    @Test
    void testAnyCondition_NestedObject() throws Exception {
        String json = """
            {
                "user": {
                    "address": {
                        "city": "London"
                    },
                    "subscription": "basic"
                }
            }
            """;
        JsonNode context = objectMapper.readTree(json);

        List<RulesLoader.Rule> rules = List.of(
            new RulesLoader.Rule("user.subscription", ConditionOperator.EQUALS, "premium"),  // FAILS
            new RulesLoader.Rule("user.address.city", ConditionOperator.EQUALS, "London")    // PASSES
        );

        assertDoesNotThrow(() -> anyCondition.execute(context, rules));
    }

    @Test
    void testAnyCondition_DifferentOperators() throws Exception {
        String json = """
            {
                "age": 16,
                "status": "verified"
            }
            """;
        JsonNode context = objectMapper.readTree(json);

        List<RulesLoader.Rule> rules = List.of(
            new RulesLoader.Rule("age", ConditionOperator.GREATER_THAN, "18"),      // FAILS (16 < 18)
            new RulesLoader.Rule("status", ConditionOperator.EQUALS, "verified")     // PASSES
        );

        assertDoesNotThrow(() -> anyCondition.execute(context, rules));
    }
}

