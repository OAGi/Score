CREATE TABLE `oas_doc_security`
(
    `oas_doc_security_id`    bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                   varchar(41) NOT NULL COMMENT 'The GUID of the record.',
    `oas_doc_id`             bigint(20) unsigned NOT NULL COMMENT 'The owning OpenAPI document; one entry of the document''s root-level security array.',
    `requirement_group`      int(11) NOT NULL DEFAULT 0 COMMENT 'Index of the Security Requirement Object within the security array (OR). Rows sharing a group are ANDed into one requirement object.',
    `oas_security_scheme_id` bigint(20) unsigned DEFAULT NULL COMMENT 'FK to oas_security_scheme. The scheme this entry references (its scheme_name is the components.securitySchemes key). NULL marks an empty requirement object {} (anonymous / optional).',
    `created_by`             bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`        bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`     datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp`  datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_doc_security_id`),
    UNIQUE KEY `oas_doc_security_doc_group_scheme_uk` (`oas_doc_id`,`requirement_group`,`oas_security_scheme_id`),
    KEY                      `oas_doc_security_oas_doc_id_fk` (`oas_doc_id`),
    KEY                      `oas_doc_security_oas_security_scheme_id_fk` (`oas_security_scheme_id`),
    KEY                      `oas_doc_security_created_by_fk` (`created_by`),
    KEY                      `oas_doc_security_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_doc_security_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_doc_security_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_doc_security_oas_doc_id_fk` FOREIGN KEY (`oas_doc_id`) REFERENCES `oas_doc` (`oas_doc_id`),
    CONSTRAINT `oas_doc_security_oas_security_scheme_id_fk` FOREIGN KEY (`oas_security_scheme_id`) REFERENCES `oas_security_scheme` (`oas_security_scheme_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci ROW_FORMAT=DYNAMIC COMMENT='Root-level Security Requirement entry for an OpenAPI document.';