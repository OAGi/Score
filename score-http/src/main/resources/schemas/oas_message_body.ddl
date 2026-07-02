CREATE TABLE `oas_message_body`
(
    `oas_message_body_id`   bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `top_level_asbiep_id`   bigint(20) unsigned DEFAULT NULL COMMENT 'A reference of the ASBIEP record.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_message_body_id`),
    KEY                     `oas_message_body_oas_asbiep_id_fk` (`top_level_asbiep_id`),
    KEY                     `oas_message_body_created_by_fk` (`created_by`),
    KEY                     `oas_message_body_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_message_body_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_message_body_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_message_body_top_level_asbiep_id_fk` FOREIGN KEY (`top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='The OAS_MESSAGE_BODY table holds an OpenAPI message body whose schema is defined by the referenced TOP_LEVEL_ASBIEP. The OAS_REQUEST and OAS_RESPONSE tables both reference this table to bind that BIE as the content of an operation''s request or response body.';