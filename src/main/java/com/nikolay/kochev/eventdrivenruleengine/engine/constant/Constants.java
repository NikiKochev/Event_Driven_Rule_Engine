package com.nikolay.kochev.eventdrivenruleengine.engine.constant;

public class Constants {
    public static final String RULE_TYPE = "businessRuleType";
    public static final String CONDITION = "condition";
    public static final String TYPE = "type";
    public static final String RULE_SETS = "rules";
    public static final String TRANSFORMATIONS = "transformations";

    public static final String ROOT = "$";
    public static final String ROOT_PREFIX = ROOT + ".";
    public static final String PATH_SEPARATOR = "\\";
    public static final String PATH_DELIMITER = PATH_SEPARATOR + ".";


    public static final class CommonKeys {
        public static final String PATH = "path";
        public static final String VALUE = "value";

        private CommonKeys() {
        }
    }

}