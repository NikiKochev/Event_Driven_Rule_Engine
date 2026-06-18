package com.nikolay.kochev.eventdrivenruleengine.engine.condition.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.condition.BaseCondition;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.RulesLoader;
import com.nikolay.kochev.eventdrivenruleengine.exception.ConditionException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AllCondition extends BaseCondition {

    @Override
    public void execute(JsonNode context, List<RulesLoader.Rule> rules) {
        for (RulesLoader.Rule rule : rules) {
            if (!evaluateRule(context, rule)) {
                throw new ConditionException("Rule failed: " + rule);
            }
        }
    }
}

