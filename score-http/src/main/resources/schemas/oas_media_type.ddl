CREATE TABLE `oas_media_type`
(
    `oas_media_type_id`     bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                  varchar(41) NOT NULL COMMENT 'The GUID of the record.',
    `description`           text DEFAULT NULL COMMENT 'A short description of the media type. CommonMark syntax MAY be used for rich text representation.',
    `owner_user_id`         bigint(20) unsigned NOT NULL COMMENT 'The user who owns the record.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_media_type_id`),
    KEY                     `oas_media_type_created_by_fk` (`created_by`),
    KEY                     `oas_media_type_last_updated_by_fk` (`last_updated_by`),
    KEY                     `oas_media_type_owner_user_id_fk` (`owner_user_id`),
    CONSTRAINT `oas_media_type_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_media_type_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_media_type_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='OpenAPI Media Type Object, which represents a media type (such as application/json) used within the content of a request body or response in an OpenAPI document.';