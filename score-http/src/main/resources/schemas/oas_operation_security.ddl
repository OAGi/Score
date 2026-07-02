CREATE TABLE `oas_operation_security`
(
    `oas_operation_security_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                      varchar(41) NOT NULL COMMENT 'The GUID of the record.',
    `oas_operation_id`          bigint(20) unsigned NOT NULL COMMENT 'The owning operation; one entry of the operation''s security array (overrides the document-level security).',
    `requirement_group`         int(11) NOT NULL DEFAULT 0 COMMENT 'Index of the Security Requirement Object within the security array (OR). Rows sharing a group are ANDed into one requirement object.',
    `oas_security_scheme_id`    bigint(20) unsigned DEFAULT NULL COMMENT 'FK to oas_security_scheme. The scheme this entry references (its scheme_name is the components.securitySchemes key). NULL marks an empty requirement object {} (anonymous / optional).',
    `created_by`                bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`           bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`        datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp`     datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_operation_security_id`),
    UNIQUE KEY `oas_operation_security_op_group_scheme_uk` (`oas_operation_id`,`requirement_group`,`oas_security_scheme_id`),
    KEY                         `oas_operation_security_oas_operation_id_fk` (`oas_operation_id`),
    KEY                         `oas_operation_security_oas_security_scheme_id_fk` (`oas_security_scheme_id`),
    KEY                         `oas_operation_security_created_by_fk` (`created_by`),
    KEY                         `oas_operation_security_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_operation_security_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_operation_security_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_operation_security_oas_operation_id_fk` FOREIGN KEY (`oas_operation_id`) REFERENCES `oas_operation` (`oas_operation_id`),
    CONSTRAINT `oas_operation_security_oas_security_scheme_id_fk` FOREIGN KEY (`oas_security_scheme_id`) REFERENCES `oas_security_scheme` (`oas_security_scheme_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci ROW_FORMAT=DYNAMIC COMMENT='Operation-level Security Requirement entry (overrides document-level).';