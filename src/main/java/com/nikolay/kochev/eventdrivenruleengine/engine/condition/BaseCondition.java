package com.nikolay.kochev.eventdrivenruleengine.engine.condition;

import com.fasterxml.jackson.databind.JsonNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.enums.ConditionOperator;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.RulesLoader;

public abstract class BaseCondition implements ConditionEvaluator {

    protected boolean evaluateRule(JsonNode context, RulesLoader.Rule rule) {
        JsonNode actualValue = extractValueByPath(context, rule.path());
        if (actualValue == null || actualValue.isMissingNode() || actualValue.isNull()) {
            return false;
        }
        return applyOperator(actualValue, rule.operation(), rule.value());
    }

    protected JsonNode extractValueByPath(JsonNode context, String path) {
        String cleanPath = path.startsWith("$.") ? path.substring(2) : path;

        String[] pathParts = cleanPath.split("\\.");

        JsonNode current = context;
        for (String part : pathParts) {
            if (current == null || current.isMissingNode()) {
                return null;
            }
            current = current.get(part);
        }

        return current;
    }

    protected boolean applyOperator(JsonNode actualValue, ConditionOperator operator, String expectedValue) {
        String actualText = actualValue.asText();

        return switch (operator) {
            case EQUALS -> actualText.equals(expectedValue);
            case EQUALS_IGNORE_CASE -> actualText.equalsIgnoreCase(expectedValue);
            case CONTAINS -> actualText.contains(expectedValue);
            case STARTS_WITH -> actualText.startsWith(expectedValue);
            case ENDS_WITH -> actualText.endsWith(expectedValue);
            case GREATER_THAN -> Double.parseDouble(actualText) > Double.parseDouble(expectedValue);
            case LESS_THAN -> Double.parseDouble(actualText) < Double.parseDouble(expectedValue);
            case EXISTS -> !actualValue.isMissingNode();
            case NOT_NULL -> !actualValue.isNull();
            case IN -> isValueInList(actualText, expectedValue);
            case NOT_IN -> !isValueInList(actualText, expectedValue);
        };
    }

    protected boolean isValueInList(String actualValue, String commaSeparatedList) {
        String[] values = commaSeparatedList.split(",");
        for (String value : values) {
            if (value.trim().equals(actualValue)) {
                return true;
            }
        }
        return false;
    }
}

