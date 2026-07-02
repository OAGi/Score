CREATE TABLE `oauth2_app_scope`
(
    `oauth2_app_scope_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of an OAUTH2_APP_SCOPE record.',
    `oauth2_app_id`       bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the OAUTH2_APP table.',
    `scope`               varchar(100) NOT NULL COMMENT 'A single OAuth2 scope (e.g., openid, profile, email) requested for the associated OAUTH2_APP.',
    PRIMARY KEY (`oauth2_app_scope_id`),
    KEY                   `oauth2_app_scope_oauth2_app_id_fk` (`oauth2_app_id`),
    CONSTRAINT `oauth2_app_scope_oauth2_app_id_fk` FOREIGN KEY (`oauth2_app_id`) REFERENCES `oauth2_app` (`oauth2_app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This table captures the OAuth2 scopes requested for an OAUTH2_APP, storing one scope value per row.';