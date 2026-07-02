CREATE TABLE `oas_request_parameter`
(
    `oas_parameter_id`      bigint(20) unsigned NOT NULL COMMENT 'The primary key of the record.',
    `oas_request_id`        bigint(20) unsigned NOT NULL COMMENT 'The primary key of the record.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_parameter_id`,`oas_request_id`),
    KEY                     `oas_request_parameter_created_by_fk` (`created_by`),
    KEY                     `oas_request_parameter_last_updated_by_fk` (`last_updated_by`),
    KEY                     `oas_request_parameter_oas_request_id_fk` (`oas_request_id`),
    CONSTRAINT `oas_request_parameter_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_request_parameter_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_request_parameter_oas_parameter_id_fk` FOREIGN KEY (`oas_parameter_id`) REFERENCES `oas_parameter` (`oas_parameter_id`),
    CONSTRAINT `oas_request_parameter_oas_request_id_fk` FOREIGN KEY (`oas_request_id`) REFERENCES `oas_request` (`oas_request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='A many-to-many join assigning OAS_PARAMETER entries (an operation''s query, header, path, or cookie parameters) to an OAS_REQUEST.';