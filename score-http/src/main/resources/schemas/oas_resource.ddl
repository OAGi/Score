CREATE TABLE `oas_resource`
(
    `oas_resource_id`       bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `oas_doc_id`            bigint(20) unsigned DEFAULT NULL COMMENT 'A reference of the doc record.',
    `path`                  text NOT NULL COMMENT 'The OpenAPI path (resource name) for this resource; defaults to the BIE name.',
    `ref`                   text DEFAULT NULL COMMENT 'An optional reference ($ref) to an externally defined Path Item Object for this resource.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_resource_id`),
    KEY                     `oas_resource_oas_doc_id_fk` (`oas_doc_id`),
    KEY                     `oas_resource_created_by_fk` (`created_by`),
    KEY                     `oas_resource_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_resource_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_resource_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_resource_oas_doc_id_fk` FOREIGN KEY (`oas_doc_id`) REFERENCES `oas_doc` (`oas_doc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='A resource (path) belonging to an OpenAPI document; each row is an entry of the OAS_DOC Paths Object, keyed by PATH (the OpenAPI path, defaulting to the BIE name) and optionally pointing to an externally defined Path Item Object via the REF ($ref) column.';