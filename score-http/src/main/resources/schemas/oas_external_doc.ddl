CREATE TABLE `oas_external_doc`
(
    `oas_external_doc_id`   bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `url`                   varchar(250) NOT NULL COMMENT 'REQUIRED. The URL for the target documentation. Value MUST be in the format of a URL.',
    `description`           text DEFAULT NULL COMMENT 'A short description of the target documentation. CommonMark syntax MAY be used for rich text representation.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_external_doc_id`),
    KEY                     `oas_external_doc_created_by_fk` (`created_by`),
    KEY                     `oas_external_doc_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_external_doc_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_external_doc_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='Allows referencing an external resource for extended documentation.';