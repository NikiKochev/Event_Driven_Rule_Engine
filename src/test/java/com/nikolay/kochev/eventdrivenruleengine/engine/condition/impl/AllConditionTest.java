package com.nikolay.kochev.eventdrivenruleengine.engine.condition.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.kochev.eventdrivenruleengine.engine.enums.ConditionOperator;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.RulesLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AllConditionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AllCondition allCondition = new AllCondition();

    @Test
    void testSimpleRule_FirstName_Equals_A() throws Exception {
        String json = """
            {
                "firstName": "A",
                "lastName": "Smith"
            }
            """;
        JsonNode context = objectMapper.readTree(json);

        RulesLoader.Rule rule = new RulesLoader.Rule("$.firstName", ConditionOperator.EQUALS, "A");
        List<RulesLoader.Rule> rules = List.of(rule);

        assertDoesNotThrow(() -> allCondition.execute(context, rules));
    }

    @Test
    void testSimpleRule_FirstName_Equals_A_Fails() throws Exception {
        String json = """
            {
                "firstName": "B",
                "lastName": "Smith"
            }
            """;
        JsonNode context = objectMapper.readTree(json);

        RulesLoader.Rule rule = new RulesLoader.Rule("$.firstName", ConditionOperator.EQUALS, "A");
        List<RulesLoader.Rule> rules = List.of(rule);
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> allCondition.execute(context, rules));
        assertTrue(exception.getMessage().contains("Rule failed"));
    }

    @Test
    void testNestedPath() throws Exception {
        String json = """
            {
                "user": {
                    "name": {
                        "firstName": "Alice"
                    }
                }
            }
            """;
        JsonNode context = objectMapper.readTree(json);

        RulesLoader.Rule rule = new RulesLoader.Rule("$.user.name.firstName", ConditionOperator.EQUALS, "Alice");
        List<RulesLoader.Rule> rules = List.of(rule);


        assertDoesNotThrow(() -> allCondition.execute(context, rules));
    }

    @Test
    void testAllCondition_MultipleRules_AllPass() throws Exception {
        String json = """
            {
                "firstName": "A",
                "age": 25
            }
            """;
        JsonNode context = objectMapper.readTree(json);

        List<RulesLoader.Rule> rules = List.of(
            new RulesLoader.Rule("$.firstName", ConditionOperator.EQUALS, "A"),
            new RulesLoader.Rule("$.age", ConditionOperator.GREATER_THAN, "18")
        );
        assertDoesNotThrow(() -> allCondition.execute(context, rules));
    }

    @Test
    void testAllCondition_MultipleRules_OneFails() throws Exception {
        String json = """
            {
                "firstName": "A",
                "age": 15
            }
            """;
        JsonNode context = objectMapper.readTree(json);

        List<RulesLoader.Rule> rules = List.of(
            new RulesLoader.Rule("$.firstName", ConditionOperator.EQUALS, "A"),  // passes
            new RulesLoader.Rule("$.age", ConditionOperator.GREATER_THAN, "18")    // fails (15 < 18)
        );

        assertThrows(RuntimeException.class, () -> allCondition.execute(context, rules));
    }

    @Test
    void testAllOperators_WithComplexJson() throws Exception {
        String json = """
            {
                "user": {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "email": "alice@example.com",
                    "age": 25,
                    "score": 15,
                    "role": "admin",
                    "status": "ACTIVE",
                    "nickname": "ace",
                    "profile": {
                        "filename": "report.json",
                        "bio": "Hello World"
                    }
                }
            }
            """;
        JsonNode context = objectMapper.readTree(json);

        List<RulesLoader.Rule> rules = List.of(
            new RulesLoader.Rule("user.firstName",              ConditionOperator.EQUALS,              "Alice"),
            new RulesLoader.Rule("user.status",                 ConditionOperator.EQUALS_IGNORE_CASE,  "active"),
            new RulesLoader.Rule("user.age",                    ConditionOperator.GREATER_THAN,        "18"),
            new RulesLoader.Rule("user.score",                  ConditionOperator.LESS_THAN,           "18"),
            new RulesLoader.Rule("user.email",                  ConditionOperator.CONTAINS,            "@example.com"),
            new RulesLoader.Rule("user.profile.bio",            ConditionOperator.STARTS_WITH,         "Hello"),
            new RulesLoader.Rule("user.profile.filename",       ConditionOperator.ENDS_WITH,           ".json"),
            new RulesLoader.Rule("user.nickname",               ConditionOperator.EXISTS,              ""),
            new RulesLoader.Rule("user.nickname",               ConditionOperator.NOT_NULL,            ""),
            new RulesLoader.Rule("user.role",                   ConditionOperator.IN,                  "admin,editor,viewer"),
            new RulesLoader.Rule("user.role",                   ConditionOperator.NOT_IN,              "banned,suspended")
        );

        assertDoesNotThrow(() -> allCondition.execute(context, rules));
    }
}

