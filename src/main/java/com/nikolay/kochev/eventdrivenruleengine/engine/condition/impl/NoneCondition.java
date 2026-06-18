package com.nikolay.kochev.eventdrivenruleengine.engine.condition.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.condition.BaseCondition;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.RulesLoader;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoneCondition extends BaseCondition {

    @Override
    public void execute(JsonNode context, List<RulesLoader.Rule> rules) {
    }
}

