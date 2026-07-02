CREATE TABLE `oas_server`
(
    `oas_server_id`         bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                  varchar(41) NOT NULL COMMENT 'The GUID of the record.',
    `oas_doc_id`            bigint(20) unsigned NOT NULL COMMENT 'A reference of the doc record.',
    `description`           text DEFAULT NULL COMMENT 'An optional string describing the host designated by the URL. CommonMark syntax MAY be used for rich text representation.',
    `url`                   varchar(250) NOT NULL COMMENT 'REQUIRED. A URL to the target host. This URL supports Server Variables and MAY be relative, to indicate that the host location is relative to the location where the OpenAPI document is being served. Variable substitutions will be made when a variable is named in {brackets}.',
    `variables`             text DEFAULT NULL COMMENT 'A map between a variable name and its value. The value is used for substitution in the server''s URL template.',
    `owner_user_id`         bigint(20) unsigned NOT NULL COMMENT 'The user who owns the record.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_server_id`),
    KEY                     `oas_server_oas_doc_id_fk` (`oas_doc_id`),
    KEY                     `oas_server_created_by_fk` (`created_by`),
    KEY                     `oas_server_last_updated_by_fk` (`last_updated_by`),
    KEY                     `oas_server_owner_user_id_fk` (`owner_user_id`),
    CONSTRAINT `oas_server_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_server_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_server_oas_doc_id_fk` FOREIGN KEY (`oas_doc_id`) REFERENCES `oas_doc` (`oas_doc_id`),
    CONSTRAINT `oas_server_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='OpenAPI Server Object; one entry of the servers array of an OAS_DOC, providing connectivity information to a target host via a URL (optionally with a description and URL-template variables).';