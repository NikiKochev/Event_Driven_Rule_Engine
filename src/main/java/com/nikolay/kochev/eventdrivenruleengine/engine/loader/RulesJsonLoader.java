package com.nikolay.kochev.eventdrivenruleengine.engine.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.kochev.eventdrivenruleengine.engine.enums.BusinessConditionType;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.model.RulesJsonRoot;
import com.nikolay.kochev.eventdrivenruleengine.exception.ConfigurationException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
public class RulesJsonLoader {

    @Value("${engine.rules.file:classpath:rules.json}")
    private Resource rulesJsonResource;

    private final ObjectMapper objectMapper;

    public RulesJsonLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadRules() {
        log.info("Loading rules from rules.json...");

        try {
            RulesLoader.clearAll();
            TransformationLoader.clearAll();

            RulesJsonRoot rulesRoot = parseRulesJson();

            if (rulesRoot == null || rulesRoot.getRuleSets() == null || rulesRoot.getRuleSets().isEmpty()) {
                log.warn("No ruleSets found in rules.json");
                return;
            }

            int ruleSetCount = 0;
            for (RulesJsonRoot.RuleSetConfig ruleSet : rulesRoot.getRuleSets()) {
                processRuleSet(ruleSet);
                ruleSetCount++;
            }

            log.info("Successfully loaded {} rule set(s) from rules.json", ruleSetCount);

        } catch (Exception e) {
            log.error("Failed to load rules from rules.json: {}", e.getMessage(), e);
            throw new ConfigurationException("Failed to load rules configuration", e);
        }
    }

    private RulesJsonRoot parseRulesJson() throws IOException {
        if (!rulesJsonResource.exists()) {
            throw new ConfigurationException("rules.json file not found in classpath");
        }

        try (InputStream inputStream = rulesJsonResource.getInputStream()) {
            return objectMapper.readValue(inputStream, RulesJsonRoot.class);
        }
    }

    /**
     * Processes a single rule set configuration.
     */
    private void processRuleSet(RulesJsonRoot.RuleSetConfig ruleSet) {
        String businessRuleType = ruleSet.getBusinessRuleType();

        if (businessRuleType == null || businessRuleType.isBlank()) {
            log.warn("Skipping rule set with missing businessRuleType");
            return;
        }

        log.debug("Processing rule set: {}", businessRuleType);

        // Load condition rules
        if (ruleSet.getConditions() != null) {
            loadConditionRules(businessRuleType, ruleSet);
        }

        // Load transformations
        if (ruleSet.getTransformations() != null && !ruleSet.getTransformations().isEmpty()) {
            loadTransformations(businessRuleType, ruleSet);
        }
    }


    private void loadConditionRules(String businessRuleType, RulesJsonRoot.RuleSetConfig ruleSet) {
        var condition = ruleSet.getConditions();

        // Validate condition type
        String conditionType = ruleSet.getConditionType();
        if (conditionType == null || conditionType.isBlank()) {
            log.warn("Rule set {} has no condition type, defaulting to 'all'", businessRuleType);
            conditionType = "all";
        }

        try {
            BusinessConditionType.valueOf(conditionType);
        } catch (Exception e) {
            log.error("Invalid condition type '{}' for rule set {}", conditionType, businessRuleType);
            throw new ConfigurationException("Invalid condition type: " + conditionType);
        }

        // Load individual rules
        for (var rule : condition) {
            try {
                String path = rule.getPath();
                String operator = rule.getOperator();
                String value = String.valueOf(rule.getValue());

                if (path == null || operator == null) {
                    log.warn("Skipping invalid rule in {}: path or operator is null", businessRuleType);
                    continue;
                }

                RulesLoader.addRule(businessRuleType, path, operator.toUpperCase(), value, ruleSet.getConditionType());
                log.trace("Added rule: {} {} {} for {}", path, operator, value, businessRuleType);

            } catch (Exception e) {
                log.error("Failed to add rule for {}: {}", businessRuleType, e.getMessage());
                throw new ConfigurationException("Failed to process rule for " + businessRuleType, e);
            }
        }

        log.debug("Loaded {} condition rule(s) for {}", condition.size(), businessRuleType);
    }

    private void loadTransformations(String businessRuleType, RulesJsonRoot.RuleSetConfig ruleSet) {
        var transformations = ruleSet.getTransformations();

        for (var transformation : transformations) {
            try {
                String path = transformation.getPath();
                String operator = transformation.getOperator();
                Object value = transformation.getValue();

                if (path == null || operator == null) {
                    log.warn("Skipping invalid transformation in {}: path or operator is null", businessRuleType);
                    continue;
                }

                // Convert value to String (handle different types)
                String valueStr = convertValueToString(value);

                TransformationLoader.addTransformation(
                    businessRuleType,
                    path,
                    operator.toUpperCase(),
                    valueStr
                );

                log.trace("Added transformation: {} {} {} for {}", path, operator, valueStr, businessRuleType);

            } catch (Exception e) {
                log.error("Failed to add transformation for {}: {}", businessRuleType, e.getMessage());
                throw new ConfigurationException("Failed to process transformation for " + businessRuleType, e);
            }
        }

        log.debug("Loaded {} transformation(s) for {}", transformations.size(), businessRuleType);
    }

    private String convertValueToString(Object value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }
}

