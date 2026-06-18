package com.nikolay.kochev.eventdrivenruleengine.engine.condition.impl;

import com.nikolay.kochev.eventdrivenruleengine.engine.condition.ConditionEngine;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.RulesLoader;
import com.nikolay.kochev.eventdrivenruleengine.service.model.EventDrivenMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.nikolay.kochev.eventdrivenruleengine.engine.constant.Constants.RULE_TYPE;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConditionEngineImpl implements ConditionEngine {

    private final AllCondition allCondition;
    private final AnyCondition anyCondition;
    private final NoneCondition noneCondition;

    @Override
    public boolean validateBusinessRules(EventDrivenMessage eventDrivenMessage) {
        try {
            var rulesData = RulesLoader.getRulesByType(eventDrivenMessage.getMetadata().getOrDefault(RULE_TYPE, null));

            if (rulesData == null) {
                log.warn("No rules found for type: {}", eventDrivenMessage.getMetadata().getOrDefault(RULE_TYPE, null));
                return false;
            }

            var type = rulesData.businessConditionType();
            var rules = rulesData.rules();

            switch (type) {
                case ALL -> allCondition.execute(eventDrivenMessage.getPayload(), rules);
                case ANY -> anyCondition.execute(eventDrivenMessage.getPayload(), rules);
                case NONE -> noneCondition.execute(eventDrivenMessage.getPayload(), rules);
                default -> throw new IllegalArgumentException("Unknown business condition type: " + type);
            }
            return true;
        } catch (Exception e) {
            log.error("Error validating business rules: {}", e.getMessage(), e);
            return false;
        }
    }
}
