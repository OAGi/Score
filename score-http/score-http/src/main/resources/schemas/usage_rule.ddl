CREATE TABLE `usage_rule`
(
    `usage_rule_id`  bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key of the usage rule.',
    `name`           text COMMENT 'Short nmenomic name of the usage rule.',
    `condition_type` int(11)             NOT NULL COMMENT 'Condition type according to the CC specification. It is a value list column. 0 = pre-condition, 1 = post-condition, 2 = invariant.',
    PRIMARY KEY (`usage_rule_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='This table captures a usage rule information. A usage rule may be expressed in multiple expressions. Each expression is captured in the USAGE_RULE_EXPRESSION table. To capture a description of a usage rule, create a usage rule expression with the unstructured constraint type.';