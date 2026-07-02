CREATE TABLE `oas_external_doc_doc`
(
    `oas_external_doc_id`   bigint(20) unsigned NOT NULL COMMENT 'The primary key of the record.',
    `oas_doc_id`            bigint(20) unsigned NOT NULL COMMENT 'The primary key of the record.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_external_doc_id`,`oas_doc_id`),
    KEY                     `oas_external_doc_oas_doc_id_fk` (`oas_doc_id`),
    KEY                     `oas_external_doc_doc_created_by_fk` (`created_by`),
    KEY                     `oas_external_doc_doc_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_external_doc_doc_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_external_doc_doc_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_external_doc_oas_doc_id_fk` FOREIGN KEY (`oas_doc_id`) REFERENCES `oas_doc` (`oas_doc_id`),
    CONSTRAINT `oas_external_doc_oas_external_doc_id_fk` FOREIGN KEY (`oas_external_doc_id`) REFERENCES `oas_external_doc` (`oas_external_doc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='A many-to-many join that attaches OAS_EXTERNAL_DOC external documentation references to OAS_DOC OpenAPI documents.';