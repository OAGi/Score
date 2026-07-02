CREATE TABLE `dt_manifest_tag`
(
    `dt_manifest_id`     bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the DT_MANIFEST table.',
    `tag_id`             bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the TAG table.',
    `created_by`         bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the record.',
    `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the record was first created.',
    PRIMARY KEY (`dt_manifest_id`, `tag_id`),
    KEY                  `dt_manifest_tag_dt_manifest_id_fk` (`dt_manifest_id`),
    KEY                  `dt_manifest_tag_tag_id_fk` (`tag_id`),
    KEY                  `dt_manifest_tag_created_by_fk` (`created_by`),
    CONSTRAINT `dt_manifest_tag_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `dt_manifest_tag_dt_manifest_id_fk` FOREIGN KEY (`dt_manifest_id`) REFERENCES `dt_manifest` (`dt_manifest_id`),
    CONSTRAINT `dt_manifest_tag_tag_id_fk` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='A many-to-many join table that assigns TAG rows to DT_MANIFEST rows, attaching tags to the release-specific manifest of a data type (DT).';