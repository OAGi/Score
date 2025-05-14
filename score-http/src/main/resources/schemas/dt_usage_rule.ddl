CREATE TABLE `dt_usage_rule`
(
    `dt_usage_rule_id`       bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key of the table.',
    `assigned_usage_rule_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the USAGE_RULE table indicating the usage rule assigned to the DT content component or DT_SC.',
    `target_dt_id`           bigint(20) unsigned DEFAULT NULL COMMENT 'Foreing key to the DT_ID for assigning a usage rule to the corresponding DT content component.',
    `target_dt_sc_id`        bigint(20) unsigned DEFAULT NULL COMMENT 'Foreing key to the DT_SC_ID for assigning a usage rule to the corresponding DT_SC.',
    PRIMARY KEY (`dt_usage_rule_id`),
    KEY                      `dt_usage_rule_assigned_usage_rule_id_fk` (`assigned_usage_rule_id`),
    KEY                      `dt_usage_rule_target_dt_id_fk` (`target_dt_id`),
    KEY                      `dt_usage_rule_target_dt_sc_id_fk` (`target_dt_sc_id`),
    CONSTRAINT `dt_usage_rule_assigned_usage_rule_id_fk` FOREIGN KEY (`assigned_usage_rule_id`) REFERENCES `usage_rule` (`usage_rule_id`),
    CONSTRAINT `dt_usage_rule_target_dt_id_fk` FOREIGN KEY (`target_dt_id`) REFERENCES `dt` (`dt_id`),
    CONSTRAINT `dt_usage_rule_target_dt_sc_id_fk` FOREIGN KEY (`target_dt_sc_id`) REFERENCES `dt_sc` (`dt_sc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This is an intersection table. Per CCTS, a usage rule may be reused. This table allows m-m relationships between the usage rule and the DT content component and usage rules and DT supplementary component. In a particular record, either a TARGET_DT_ID or TARGET_DT_SC_ID must be present but not both.';