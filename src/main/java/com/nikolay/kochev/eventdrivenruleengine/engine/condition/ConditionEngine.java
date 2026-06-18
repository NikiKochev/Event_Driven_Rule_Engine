package com.nikolay.kochev.eventdrivenruleengine.engine.condition;

import com.nikolay.kochev.eventdrivenruleengine.service.model.EventDrivenMessage;

public interface ConditionEngine {

    boolean validateBusinessRules(EventDrivenMessage eventDrivenMessage);
}

