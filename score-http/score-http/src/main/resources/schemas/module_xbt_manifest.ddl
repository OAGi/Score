CREATE TABLE `module_xbt_manifest`
(
    `module_xbt_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
    `module_set_release_id`  bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the module set release record.',
    `xbt_manifest_id`        bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the xbt manifest record.',
    `module_id`              bigint(20) unsigned NOT NULL COMMENT 'This indicates a module.',
    `created_by`             bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this record.',
    `last_updated_by`        bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record.',
    `creation_timestamp`     datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.',
    `last_update_timestamp`  datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.',
    PRIMARY KEY (`module_xbt_manifest_id`),
    KEY                      `module_xbt_manifest_created_by_fk` (`created_by`),
    KEY                      `module_xbt_manifest_last_updated_by_fk` (`last_updated_by`),
    KEY                      `module_xbt_manifest_module_set_release_id_fk` (`module_set_release_id`),
    KEY                      `module_xbt_manifest_bccp_manifest_id_fk` (`xbt_manifest_id`),
    KEY                      `module_xbt_manifest_module_id_fk` (`module_id`),
    CONSTRAINT `module_xbt_manifest_bccp_manifest_id_fk` FOREIGN KEY (`xbt_manifest_id`) REFERENCES `xbt_manifest` (`xbt_manifest_id`),
    CONSTRAINT `module_xbt_manifest_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `module_xbt_manifest_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `module_xbt_manifest_module_id_fk` FOREIGN KEY (`module_id`) REFERENCES `module` (`module_id`),
    CONSTRAINT `module_xbt_manifest_module_set_release_id_fk` FOREIGN KEY (`module_set_release_id`) REFERENCES `module_set_release` (`module_set_release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;