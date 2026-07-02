CREATE TABLE `oas_parameter_link`
(
    `oas_parameter_link_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `oas_response_id`       bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the OAS_RESPONSE table.',
    `oas_parameter_id`      bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the OAS_PARAMETER table.',
    `oas_operation_id`      bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the OAS_OPERATION table.',
    `expression`            text DEFAULT NULL COMMENT 'A runtime expression that provides the parameter value, for example ''$response.body#/purchaseOrderHeader.identifier''.',
    `description`           text DEFAULT NULL COMMENT 'A brief description of the parameter link. CommonMark syntax MAY be used for rich text representation.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_parameter_link_id`),
    KEY                     `oas_parameter_link_oas_response_id_fk` (`oas_response_id`),
    KEY                     `oas_parameter_link_oas_parameter_id_fk` (`oas_parameter_id`),
    KEY                     `oas_parameter_link_oas_operation_id_fk` (`oas_operation_id`),
    KEY                     `oas_parameter_link_created_by_fk` (`created_by`),
    KEY                     `oas_parameter_link_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_parameter_link_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_parameter_link_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_parameter_link_oas_operation_id_fk` FOREIGN KEY (`oas_operation_id`) REFERENCES `oas_operation` (`oas_operation_id`),
    CONSTRAINT `oas_parameter_link_oas_parameter_id_fk` FOREIGN KEY (`oas_parameter_id`) REFERENCES `oas_parameter` (`oas_parameter_id`),
    CONSTRAINT `oas_parameter_link_oas_response_id_fk` FOREIGN KEY (`oas_response_id`) REFERENCES `oas_response` (`oas_response_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='OpenAPI Link Object parameter binding declared on an OAS_RESPONSE; each row ties an OAS_PARAMETER to a runtime EXPRESSION that supplies its value, optionally targeting the linked OAS_OPERATION.';