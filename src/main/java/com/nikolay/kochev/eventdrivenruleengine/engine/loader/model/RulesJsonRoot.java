package com.nikolay.kochev.eventdrivenruleengine.engine.loader.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RulesJsonRoot {
    @JsonProperty("ruleSets")
    private List<RuleSetConfig> ruleSets;

    @Data
    public static class RuleSetConfig {
        @JsonProperty("businessRuleType")
        private String businessRuleType;

        @JsonProperty("conditionType")
        private String conditionType;

        @JsonProperty("conditions")
        private List<SimpleConfig> conditions;

        @JsonProperty("transformations")
        private List<SimpleConfig> transformations;
    }


    @Data
    public static class SimpleConfig {
        @JsonProperty("path")
        private String path;

        @JsonProperty("operator")
        private String operator;

        @JsonProperty("value")
        private Object value; // Can be String, Number, Boolean, etc.
    }
}

