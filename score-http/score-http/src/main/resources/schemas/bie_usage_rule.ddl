CREATE TABLE `bie_usage_rule`
(
    `bie_usage_rule_id`      bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key of the table.',
    `assigned_usage_rule_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the USAGE_RULE table indicating the usage rule assigned to a BIE.',
    `target_abie_id`         bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the ABIE table indicating the ABIE, to which the usage rule is applied.',
    `target_asbie_id`        bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the ASBIE table indicating the ASBIE, to which the usage rule is applied.',
    `target_asbiep_id`       bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the ASBIEP table indicating the ASBIEP, to which the usage rule is applied.',
    `target_bbie_id`         bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the BBIE table indicating the BBIE, to which the usage rule is applied.',
    `target_bbiep_id`        bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the BBIEP table indicating the ABIEP, to which the usage rule is applied.',
    PRIMARY KEY (`bie_usage_rule_id`),
    KEY                      `bie_usage_rule_assigned_usage_rule_id_fk` (`assigned_usage_rule_id`),
    KEY                      `bie_usage_rule_target_abie_id_fk` (`target_abie_id`),
    KEY                      `bie_usage_rule_target_asbie_id_fk` (`target_asbie_id`),
    KEY                      `bie_usage_rule_target_asbiep_id_fk` (`target_asbiep_id`),
    KEY                      `bie_usage_rule_target_bbie_id_fk` (`target_bbie_id`),
    KEY                      `bie_usage_rule_target_bbiep_id_fk` (`target_bbiep_id`),
    CONSTRAINT `bie_usage_rule_assigned_usage_rule_id_fk` FOREIGN KEY (`assigned_usage_rule_id`) REFERENCES `usage_rule` (`usage_rule_id`),
    CONSTRAINT `bie_usage_rule_target_abie_id_fk` FOREIGN KEY (`target_abie_id`) REFERENCES `abie` (`abie_id`),
    CONSTRAINT `bie_usage_rule_target_asbie_id_fk` FOREIGN KEY (`target_asbie_id`) REFERENCES `asbie` (`asbie_id`),
    CONSTRAINT `bie_usage_rule_target_asbiep_id_fk` FOREIGN KEY (`target_asbiep_id`) REFERENCES `asbiep` (`asbiep_id`),
    CONSTRAINT `bie_usage_rule_target_bbie_id_fk` FOREIGN KEY (`target_bbie_id`) REFERENCES `bbie` (`bbie_id`),
    CONSTRAINT `bie_usage_rule_target_bbiep_id_fk` FOREIGN KEY (`target_bbiep_id`) REFERENCES `bbiep` (`bbiep_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This is an intersection table. Per CCTS, a usage rule may be reused. This table allows m-m relationships between the usage rule and all kinds of BIEs. In a particular record, either only one of the TARGET_ABIE_ID, TARGET_ASBIE_ID, TARGET_ASBIEP_ID, TARGET_BBIE_ID, or TARGET_BBIEP_ID.';