-- ----------------------------------------------------
-- Migration script for Score v3.5.1                 --
--                                                   --
-- Author: Hakju Oh <hakju.oh@nist.gov>              --
-- ----------------------------------------------------

SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `oas_security_scheme`;
DROP TABLE IF EXISTS `oas_oauth_flow`;
DROP TABLE IF EXISTS `oas_oauth_scope`;
DROP TABLE IF EXISTS `oas_doc_security_scope`;
DROP TABLE IF EXISTS `oas_doc_security`;
DROP TABLE IF EXISTS `oas_operation_security_scope`;
DROP TABLE IF EXISTS `oas_operation_security`;

CREATE TABLE `oas_security_scheme`
(
    `oas_security_scheme_id`   bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                     varchar(41)         NOT NULL COMMENT 'The GUID of the record.',
    `oas_doc_id`               bigint(20) unsigned NOT NULL COMMENT 'Owning OpenAPI document; one entry of that doc components.securitySchemes map.',
    `scheme_name`              varchar(100)        NOT NULL COMMENT 'REQUIRED. The components.securitySchemes map key (e.g. OAuth2, ApiKeyAuth, BearerAuth); also the schemeName used in Security Requirement Objects. Unique within an oas_doc.',
    `type`                     varchar(20)         NOT NULL COMMENT 'REQUIRED. Security Scheme type: apiKey | http | oauth2 | openIdConnect | mutualTLS. mutualTLS requires OpenAPI 3.1+ output; enforced by the service layer per the doc open_api_version.',
    `description`              text                DEFAULT NULL COMMENT 'Optional (all types). Short description; CommonMark MAY be used.',
    `api_key_name`             varchar(200)        DEFAULT NULL COMMENT 'REQUIRED when type=apiKey: header/query/cookie parameter name. NULL otherwise.',
    `api_key_in`               varchar(10)         DEFAULT NULL COMMENT 'REQUIRED when type=apiKey: query | header | cookie (NOT path). NULL otherwise.',
    `http_scheme`              varchar(50)         DEFAULT NULL COMMENT 'REQUIRED when type=http: RFC7235 scheme name, e.g. basic, bearer. NULL otherwise.',
    `bearer_format`            varchar(50)         DEFAULT NULL COMMENT 'Optional; only when type=http AND http_scheme=bearer (e.g. JWT). NULL/omit for basic.',
    `open_id_connect_url`      varchar(250)        DEFAULT NULL COMMENT 'REQUIRED when type=openIdConnect: discovery URL. NULL otherwise.',
    `oauth2_flow_type`         varchar(30)         DEFAULT NULL COMMENT 'oauth2 only. The single OAuth Flows key emitted for this scheme: implicit | password | clientCredentials | authorizationCode | deviceAuthorization. deviceAuthorization requires OpenAPI 3.2+. NULL => authorizationCode (legacy default).',
    `authorization_url`        varchar(250)        DEFAULT NULL COMMENT 'oauth2; REQUIRED for implicit & authorizationCode flows. NULL => legacy default https://example.com/oauth/authorize.',
    `token_url`                varchar(250)        DEFAULT NULL COMMENT 'oauth2; REQUIRED for password, clientCredentials, authorizationCode, deviceAuthorization flows. NULL => legacy default https://example.com/oauth/token.',
    `device_authorization_url` varchar(250)        DEFAULT NULL COMMENT 'oauth2, OpenAPI 3.2+; REQUIRED for the deviceAuthorization flow (RFC 8628). NULL otherwise.',
    `refresh_url`              varchar(250)        DEFAULT NULL COMMENT 'oauth2; optional refreshUrl for the flow. Emitted only when present.',
    `oauth2_metadata_url`      varchar(250)        DEFAULT NULL COMMENT 'oauth2, OpenAPI 3.2+; scheme-level URL of the OAuth 2.0 Authorization Server Metadata (RFC 8414). NULL otherwise.',
    `deprecated`               tinyint(1)          NOT NULL DEFAULT 0 COMMENT 'OpenAPI 3.2+ Security Scheme deprecated flag. Emitted only for 3.2+ output.',
    `created_by`               bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`          bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`       datetime(6)         NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp`    datetime(6)         NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_security_scheme_id`),
    UNIQUE KEY `oas_security_scheme_doc_name_uk` (`oas_doc_id`, `scheme_name`),
    KEY `oas_security_scheme_oas_doc_id_fk` (`oas_doc_id`),
    KEY `oas_security_scheme_created_by_fk` (`created_by`),
    KEY `oas_security_scheme_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_security_scheme_oas_doc_id_fk` FOREIGN KEY (`oas_doc_id`) REFERENCES `oas_doc` (`oas_doc_id`),
    CONSTRAINT `oas_security_scheme_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_security_scheme_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb3
    COLLATE = utf8mb3_general_ci
    ROW_FORMAT = DYNAMIC COMMENT ='OpenAPI Security Scheme Object (OAS 3.0.3 through 3.2.0); one named entry of a document components.securitySchemes map.';

CREATE TABLE `oas_oauth_flow`
(
    `oas_oauth_flow_id`        bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                     varchar(41)         NOT NULL COMMENT 'The GUID of the record.',
    `oas_security_scheme_id`   bigint(20) unsigned NOT NULL COMMENT 'The owning oauth2 security scheme; one entry of that scheme''s flows map.',
    `flow_type`                varchar(30)         NOT NULL COMMENT 'REQUIRED. The OAuth Flows key: implicit | password | clientCredentials | authorizationCode | deviceAuthorization (deviceAuthorization requires OpenAPI 3.2+).',
    `authorization_url`        varchar(250)        DEFAULT NULL COMMENT 'REQUIRED for the implicit and authorizationCode flows. MUST be a URL.',
    `token_url`                varchar(250)        DEFAULT NULL COMMENT 'REQUIRED for the password, clientCredentials, authorizationCode and deviceAuthorization flows. MUST be a URL.',
    `refresh_url`              varchar(250)        DEFAULT NULL COMMENT 'Optional. The OAuth refreshUrl for obtaining refresh tokens. MUST be a URL.',
    `device_authorization_url` varchar(250)        DEFAULT NULL COMMENT 'OpenAPI 3.2+. REQUIRED for the deviceAuthorization flow (RFC 8628). MUST be a URL.',
    `created_by`               bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`          bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`       datetime(6)         NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp`    datetime(6)         NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_oauth_flow_id`),
    UNIQUE KEY `oas_oauth_flow_scheme_type_uk` (`oas_security_scheme_id`, `flow_type`),
    KEY `oas_oauth_flow_oas_security_scheme_id_fk` (`oas_security_scheme_id`),
    KEY `oas_oauth_flow_created_by_fk` (`created_by`),
    KEY `oas_oauth_flow_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_oauth_flow_oas_security_scheme_id_fk` FOREIGN KEY (`oas_security_scheme_id`) REFERENCES `oas_security_scheme` (`oas_security_scheme_id`),
    CONSTRAINT `oas_oauth_flow_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_oauth_flow_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3
  COLLATE = utf8mb3_general_ci
  ROW_FORMAT = DYNAMIC COMMENT ='OpenAPI OAuth Flow Object for an oauth2 security scheme (one entry of components.securitySchemes.<name>.flows).';

CREATE TABLE `oas_oauth_scope`
(
    `oas_oauth_scope_id`    bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                  varchar(41)         NOT NULL COMMENT 'The GUID of the record.',
    `oas_oauth_flow_id`     bigint(20) unsigned NOT NULL COMMENT 'The owning OAuth flow; one entry of that flow''s scopes map.',
    `scope_name`            varchar(200)        NOT NULL COMMENT 'REQUIRED. The scope name (the scopes map key). Unique within a flow.',
    `description`           text                DEFAULT NULL COMMENT 'REQUIRED. A short description of the scope (the scopes map value).',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6)         NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6)         NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_oauth_scope_id`),
    UNIQUE KEY `oas_oauth_scope_flow_name_uk` (`oas_oauth_flow_id`, `scope_name`),
    KEY `oas_oauth_scope_oas_oauth_flow_id_fk` (`oas_oauth_flow_id`),
    KEY `oas_oauth_scope_created_by_fk` (`created_by`),
    KEY `oas_oauth_scope_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_oauth_scope_oas_oauth_flow_id_fk` FOREIGN KEY (`oas_oauth_flow_id`) REFERENCES `oas_oauth_flow` (`oas_oauth_flow_id`),
    CONSTRAINT `oas_oauth_scope_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_oauth_scope_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3
  COLLATE = utf8mb3_general_ci
  ROW_FORMAT = DYNAMIC COMMENT ='OpenAPI OAuth Flow scope (name -> description) for an oauth2 flow.';

CREATE TABLE `oas_doc_security`
(
    `oas_doc_security_id`    bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                   varchar(41)         NOT NULL COMMENT 'The GUID of the record.',
    `oas_doc_id`             bigint(20) unsigned NOT NULL COMMENT 'The owning OpenAPI document; one entry of the document''s root-level security array.',
    `requirement_group`      int(11)             NOT NULL DEFAULT 0 COMMENT 'Index of the Security Requirement Object within the security array (OR). Rows sharing a group are ANDed into one requirement object.',
    `oas_security_scheme_id` bigint(20) unsigned DEFAULT NULL COMMENT 'FK to oas_security_scheme. The scheme this entry references (its scheme_name is the components.securitySchemes key). NULL marks an empty requirement object {} (anonymous / optional).',
    `created_by`             bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`        bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`     datetime(6)         NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp`  datetime(6)         NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_doc_security_id`),
    UNIQUE KEY `oas_doc_security_doc_group_scheme_uk` (`oas_doc_id`, `requirement_group`, `oas_security_scheme_id`),
    KEY `oas_doc_security_oas_doc_id_fk` (`oas_doc_id`),
    KEY `oas_doc_security_oas_security_scheme_id_fk` (`oas_security_scheme_id`),
    KEY `oas_doc_security_created_by_fk` (`created_by`),
    KEY `oas_doc_security_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_doc_security_oas_doc_id_fk` FOREIGN KEY (`oas_doc_id`) REFERENCES `oas_doc` (`oas_doc_id`),
    CONSTRAINT `oas_doc_security_oas_security_scheme_id_fk` FOREIGN KEY (`oas_security_scheme_id`) REFERENCES `oas_security_scheme` (`oas_security_scheme_id`),
    CONSTRAINT `oas_doc_security_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_doc_security_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb3
    COLLATE = utf8mb3_general_ci
    ROW_FORMAT = DYNAMIC COMMENT ='Root-level Security Requirement entry for an OpenAPI document.';

CREATE TABLE `oas_doc_security_scope`
(
    `oas_doc_security_scope_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                      varchar(41)         NOT NULL COMMENT 'The GUID of the record.',
    `oas_doc_security_id`       bigint(20) unsigned NOT NULL COMMENT 'The owning root-level security entry.',
    `scope_name`                varchar(200)        NOT NULL COMMENT 'A scope name required by this entry (oauth2: from the scheme''s flows; openIdConnect: from the provider).',
    `created_by`                bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`           bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`        datetime(6)         NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp`     datetime(6)         NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_doc_security_scope_id`),
    UNIQUE KEY `oas_doc_security_scope_entry_name_uk` (`oas_doc_security_id`, `scope_name`),
    KEY `oas_doc_security_scope_oas_doc_security_id_fk` (`oas_doc_security_id`),
    KEY `oas_doc_security_scope_created_by_fk` (`created_by`),
    KEY `oas_doc_security_scope_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_doc_security_scope_oas_doc_security_id_fk` FOREIGN KEY (`oas_doc_security_id`) REFERENCES `oas_doc_security` (`oas_doc_security_id`),
    CONSTRAINT `oas_doc_security_scope_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_doc_security_scope_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb3
    COLLATE = utf8mb3_general_ci
    ROW_FORMAT = DYNAMIC COMMENT ='Scope of a root-level Security Requirement entry.';

CREATE TABLE `oas_operation_security`
(
    `oas_operation_security_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                      varchar(41)         NOT NULL COMMENT 'The GUID of the record.',
    `oas_operation_id`          bigint(20) unsigned NOT NULL COMMENT 'The owning operation; one entry of the operation''s security array (overrides the document-level security).',
    `requirement_group`         int(11)             NOT NULL DEFAULT 0 COMMENT 'Index of the Security Requirement Object within the security array (OR). Rows sharing a group are ANDed into one requirement object.',
    `oas_security_scheme_id`    bigint(20) unsigned DEFAULT NULL COMMENT 'FK to oas_security_scheme. The scheme this entry references (its scheme_name is the components.securitySchemes key). NULL marks an empty requirement object {} (anonymous / optional).',
    `created_by`                bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`           bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`        datetime(6)         NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp`     datetime(6)         NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_operation_security_id`),
    UNIQUE KEY `oas_operation_security_op_group_scheme_uk` (`oas_operation_id`, `requirement_group`, `oas_security_scheme_id`),
    KEY `oas_operation_security_oas_operation_id_fk` (`oas_operation_id`),
    KEY `oas_operation_security_oas_security_scheme_id_fk` (`oas_security_scheme_id`),
    KEY `oas_operation_security_created_by_fk` (`created_by`),
    KEY `oas_operation_security_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_operation_security_oas_operation_id_fk` FOREIGN KEY (`oas_operation_id`) REFERENCES `oas_operation` (`oas_operation_id`),
    CONSTRAINT `oas_operation_security_oas_security_scheme_id_fk` FOREIGN KEY (`oas_security_scheme_id`) REFERENCES `oas_security_scheme` (`oas_security_scheme_id`),
    CONSTRAINT `oas_operation_security_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_operation_security_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb3
    COLLATE = utf8mb3_general_ci
    ROW_FORMAT = DYNAMIC COMMENT ='Operation-level Security Requirement entry (overrides document-level).';

CREATE TABLE `oas_operation_security_scope`
(
    `oas_operation_security_scope_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                            varchar(41)         NOT NULL COMMENT 'The GUID of the record.',
    `oas_operation_security_id`       bigint(20) unsigned NOT NULL COMMENT 'The owning operation-level security entry.',
    `scope_name`                      varchar(200)        NOT NULL COMMENT 'A scope name required by this entry (oauth2: from the scheme''s flows; openIdConnect: from the provider).',
    `created_by`                      bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`                 bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`              datetime(6)         NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp`           datetime(6)         NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_operation_security_scope_id`),
    UNIQUE KEY `oas_operation_security_scope_entry_name_uk` (`oas_operation_security_id`, `scope_name`),
    KEY `oas_operation_security_scope_fk` (`oas_operation_security_id`),
    KEY `oas_operation_security_scope_created_by_fk` (`created_by`),
    KEY `oas_operation_security_scope_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_operation_security_scope_fk` FOREIGN KEY (`oas_operation_security_id`) REFERENCES `oas_operation_security` (`oas_operation_security_id`),
    CONSTRAINT `oas_operation_security_scope_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_operation_security_scope_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb3
    COLLATE = utf8mb3_general_ci
    ROW_FORMAT = DYNAMIC COMMENT ='Scope of an operation-level Security Requirement entry.';

-- Operation security override mode:
--   security_overridden = 0  -> inherit the document-level (root) security (no operation `security` emitted)
--   security_overridden = 1, no oas_operation_security rows -> emit `security: []` (public)
--   security_overridden = 1, with rows                      -> emit the operation's security array
ALTER TABLE `oas_operation`
    ADD COLUMN `security_overridden` tinyint(1) NOT NULL DEFAULT 0
          COMMENT '1 = this operation overrides the document-level security (0 rows => security: []; rows => the operation array). 0 = inherit the root security.'
          AFTER `deprecated`;

SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
