CREATE TABLE `oas_oauth_flow`
(
    `oas_oauth_flow_id`        bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                     varchar(41) NOT NULL COMMENT 'The GUID of the record.',
    `oas_security_scheme_id`   bigint(20) unsigned NOT NULL COMMENT 'The owning oauth2 security scheme; one entry of that scheme''s flows map.',
    `flow_type`                varchar(30) NOT NULL COMMENT 'REQUIRED. The OAuth Flows key: implicit | password | clientCredentials | authorizationCode | deviceAuthorization (deviceAuthorization requires OpenAPI 3.2+).',
    `authorization_url`        varchar(250) DEFAULT NULL COMMENT 'REQUIRED for the implicit and authorizationCode flows. MUST be a URL.',
    `token_url`                varchar(250) DEFAULT NULL COMMENT 'REQUIRED for the password, clientCredentials, authorizationCode and deviceAuthorization flows. MUST be a URL.',
    `refresh_url`              varchar(250) DEFAULT NULL COMMENT 'Optional. The OAuth refreshUrl for obtaining refresh tokens. MUST be a URL.',
    `device_authorization_url` varchar(250) DEFAULT NULL COMMENT 'OpenAPI 3.2+. REQUIRED for the deviceAuthorization flow (RFC 8628). MUST be a URL.',
    `created_by`               bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`          bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`       datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_oauth_flow_id`),
    UNIQUE KEY `oas_oauth_flow_scheme_type_uk` (`oas_security_scheme_id`,`flow_type`),
    KEY                        `oas_oauth_flow_oas_security_scheme_id_fk` (`oas_security_scheme_id`),
    KEY                        `oas_oauth_flow_created_by_fk` (`created_by`),
    KEY                        `oas_oauth_flow_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_oauth_flow_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_oauth_flow_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_oauth_flow_oas_security_scheme_id_fk` FOREIGN KEY (`oas_security_scheme_id`) REFERENCES `oas_security_scheme` (`oas_security_scheme_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci ROW_FORMAT=DYNAMIC COMMENT='OpenAPI OAuth Flow Object for an oauth2 security scheme (one entry of components.securitySchemes.<name>.flows).';