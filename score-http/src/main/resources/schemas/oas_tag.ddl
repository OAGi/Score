CREATE TABLE `oas_tag`
(
    `oas_tag_id`            bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                  varchar(41) NOT NULL COMMENT 'The GUID of the record.',
    `name`                  varchar(200) NOT NULL COMMENT 'REQUIRED. The name of the tag.',
    `description`           text DEFAULT NULL COMMENT 'A short description for the tag. CommonMark syntax MAY be used for rich text representation.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_tag_id`),
    KEY                     `oas_tag_created_by_fk` (`created_by`),
    KEY                     `oas_tag_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_tag_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_tag_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='OpenAPI Tag Object; adds metadata to a single tag (a name and an optional description) that is used to group the Operation Objects of an OpenAPI document.';