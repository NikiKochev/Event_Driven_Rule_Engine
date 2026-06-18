package com.nikolay.kochev.eventdrivenruleengine.engine.loader;

import com.nikolay.kochev.eventdrivenruleengine.engine.enums.BusinessConditionType;
import com.nikolay.kochev.eventdrivenruleengine.engine.enums.ConditionOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RulesLoader {

    private static Map<String, Rules> rules = new HashMap<>();

    public static Rules getRulesByType(String ruleType) {
        return rules.get(ruleType);
    }

    static void addRule(String ruleType, String path, String operation, String value, String businessConditionType) {
        if (!rules.containsKey(ruleType)) {
            rules.put(ruleType, new Rules(BusinessConditionType.valueOf(businessConditionType), new ArrayList<>()));
        }
        rules.get(ruleType).rules.add(new Rule(path, ConditionOperator.valueOf(operation), value));
    }

    public static void clearAll() {
        rules.clear();
    }

    public record Rule(String path, ConditionOperator operation, String value) {
    }

    public record Rules(BusinessConditionType businessConditionType, List<Rule> rules) {
    }
}
