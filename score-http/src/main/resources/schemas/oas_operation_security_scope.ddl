CREATE TABLE `oas_operation_security_scope`
(
    `oas_operation_security_scope_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                            varchar(41) NOT NULL COMMENT 'The GUID of the record.',
    `oas_operation_security_id`       bigint(20) unsigned NOT NULL COMMENT 'The owning operation-level security entry.',
    `scope_name`                      varchar(200) NOT NULL COMMENT 'A scope name required by this entry (oauth2: from the scheme''s flows; openIdConnect: from the provider).',
    `created_by`                      bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`                 bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`              datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp`           datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_operation_security_scope_id`),
    UNIQUE KEY `oas_operation_security_scope_entry_name_uk` (`oas_operation_security_id`,`scope_name`),
    KEY                               `oas_operation_security_scope_fk` (`oas_operation_security_id`),
    KEY                               `oas_operation_security_scope_created_by_fk` (`created_by`),
    KEY                               `oas_operation_security_scope_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_operation_security_scope_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_operation_security_scope_fk` FOREIGN KEY (`oas_operation_security_id`) REFERENCES `oas_operation_security` (`oas_operation_security_id`),
    CONSTRAINT `oas_operation_security_scope_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci ROW_FORMAT=DYNAMIC COMMENT='Scope of an operation-level Security Requirement entry.';