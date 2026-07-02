CREATE TABLE `oas_doc_tag`
(
    `oas_doc_id`            bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the OAS_DOC table.',
    `oas_tag_id`            bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the OAS_TAG table.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_doc_id`,`oas_tag_id`),
    KEY                     `oas_doc_tag_oas_tag_id_fk` (`oas_tag_id`),
    KEY                     `oas_doc_tag_created_by_fk` (`created_by`),
    KEY                     `oas_doc_tag_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_doc_tag_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_doc_tag_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_doc_tag_oas_doc_id_fk` FOREIGN KEY (`oas_doc_id`) REFERENCES `oas_doc` (`oas_doc_id`),
    CONSTRAINT `oas_doc_tag_oas_tag_id_fk` FOREIGN KEY (`oas_tag_id`) REFERENCES `oas_tag` (`oas_tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='A many-to-many join assigning OAS_TAG rows to an OAS_DOC, populating the OpenAPI document''s root-level tags array.';