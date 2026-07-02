CREATE TABLE `oas_resource_tag`
(
    `oas_operation_id`      bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the OAS_OPERATION table.',
    `oas_tag_id`            bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the OAS_TAG table.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_operation_id`,`oas_tag_id`),
    KEY                     `oas_resource_tag_oas_tag_id_fk` (`oas_tag_id`),
    CONSTRAINT `oas_resource_tag_oas_operation_id_fk` FOREIGN KEY (`oas_operation_id`) REFERENCES `oas_operation` (`oas_operation_id`),
    CONSTRAINT `oas_resource_tag_oas_tag_id_fk` FOREIGN KEY (`oas_tag_id`) REFERENCES `oas_tag` (`oas_tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='A many-to-many join assigning OAS_TAG entries to an OAS_OPERATION, representing the tags list of an OpenAPI Operation Object that groups the operation under those tags.';