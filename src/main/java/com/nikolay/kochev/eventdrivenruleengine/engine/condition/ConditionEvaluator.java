package com.nikolay.kochev.eventdrivenruleengine.engine.condition;

import com.fasterxml.jackson.databind.JsonNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.RulesLoader;

import java.util.List;

public interface ConditionEvaluator
{
    void execute(JsonNode context, List<RulesLoader.Rule> rules);
}

