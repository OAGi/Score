CREATE TABLE `oas_server_variable`
(
    `oas_server_variable_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `oas_server_id`          bigint(20) unsigned NOT NULL COMMENT 'A reference of the server record.',
    `name`                   varchar(100) DEFAULT NULL COMMENT '"port", "username", "basePath" are the examples in the OpenAPI Specification.',
    `description`            text DEFAULT NULL COMMENT 'An optional description for the server variable. CommonMark syntax MAY be used for rich text representation.',
    `default`                text DEFAULT NULL COMMENT 'REQUIRED. The default value to use for substitution, which SHALL be sent if an alternate value is not supplied. Note this behavior is different than the Schema Object''s treatment of default values, because in those cases parameter values are optional. If the enum is defined, the value SHOULD exist in the enum''s values.',
    `enum`                   text DEFAULT NULL COMMENT 'An enumeration of string values to be used if the substitution options are from a limited set. The array SHOULD NOT be empty.',
    `created_by`             bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`        bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`     datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp`  datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_server_variable_id`),
    KEY                      `oas_server_variable_oas_server_id_fk` (`oas_server_id`),
    KEY                      `oas_server_variable_created_by_fk` (`created_by`),
    KEY                      `oas_server_variable_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_server_variable_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_server_variable_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_server_variable_oas_server_id_fk` FOREIGN KEY (`oas_server_id`) REFERENCES `oas_server` (`oas_server_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='OpenAPI Server Variable Object; one named entry of an OAS_SERVER''s variables map, holding the default value, optional enum, and description used for server URL template substitution.';