CREATE TABLE `usage_rule_expression`
(
    `usage_rule_expression_id`  bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key of the usage rule expression',
    `constraint_type`           int(11) NOT NULL COMMENT 'Constraint type according to the CC spec. It represents the expression language (syntax) used in the CONSTRAINT column. It is a value list column. 0 = ''Unstructured'' which is basically a description of the rule, 1 = ''Schematron''.',
    `constraint_text`           text NOT NULL COMMENT 'This column capture the constraint expressing the usage rule. In other words, this is the expression.',
    `represented_usage_rule_id` bigint(20) unsigned NOT NULL COMMENT 'The usage rule which the expression represents',
    PRIMARY KEY (`usage_rule_expression_id`),
    KEY                         `usage_rule_expression_represented_usage_rule_id_fk` (`represented_usage_rule_id`),
    CONSTRAINT `usage_rule_expression_represented_usage_rule_id_fk` FOREIGN KEY (`represented_usage_rule_id`) REFERENCES `usage_rule` (`usage_rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='The USAGE_RULE_EXPRESSION provides a representation of a usage rule in a particular syntax indicated by the CONSTRAINT_TYPE column. One of the syntaxes can be unstructured, which works a description of the usage rule.';