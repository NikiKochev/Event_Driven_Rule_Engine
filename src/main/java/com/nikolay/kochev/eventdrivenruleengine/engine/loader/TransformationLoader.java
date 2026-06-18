package com.nikolay.kochev.eventdrivenruleengine.engine.loader;

import com.nikolay.kochev.eventdrivenruleengine.engine.enums.TransformOperation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransformationLoader {
    private static final Map<String, List<Transformation>> transformations = new HashMap<>();

    public static List<Transformation> getTransformationByType(String ruleType) {
        return transformations.get(ruleType);
    }

    static void addTransformation(String ruleType, String path, String operation, String value) {
        if (!transformations.containsKey(ruleType)) {
            transformations.put(ruleType, new java.util.ArrayList<>());
        }
        transformations.get(ruleType).add(new Transformation(path, TransformOperation.valueOf(operation), value));

    }

    public static void clearAll() {
        transformations.clear();
    }

    public record Transformation(String path, TransformOperation operation, String value) {
    }
}
