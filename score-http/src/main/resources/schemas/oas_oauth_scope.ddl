CREATE TABLE `oas_oauth_scope`
(
    `oas_oauth_scope_id`    bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                  varchar(41) NOT NULL COMMENT 'The GUID of the record.',
    `oas_oauth_flow_id`     bigint(20) unsigned NOT NULL COMMENT 'The owning OAuth flow; one entry of that flow''s scopes map.',
    `scope_name`            varchar(200) NOT NULL COMMENT 'REQUIRED. The scope name (the scopes map key). Unique within a flow.',
    `description`           text DEFAULT NULL COMMENT 'REQUIRED. A short description of the scope (the scopes map value).',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_oauth_scope_id`),
    UNIQUE KEY `oas_oauth_scope_flow_name_uk` (`oas_oauth_flow_id`,`scope_name`),
    KEY                     `oas_oauth_scope_oas_oauth_flow_id_fk` (`oas_oauth_flow_id`),
    KEY                     `oas_oauth_scope_created_by_fk` (`created_by`),
    KEY                     `oas_oauth_scope_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_oauth_scope_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_oauth_scope_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_oauth_scope_oas_oauth_flow_id_fk` FOREIGN KEY (`oas_oauth_flow_id`) REFERENCES `oas_oauth_flow` (`oas_oauth_flow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci ROW_FORMAT=DYNAMIC COMMENT='OpenAPI OAuth Flow scope (name -> description) for an oauth2 flow.';