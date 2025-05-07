CREATE TABLE `module_set`
(
    `module_set_id`         bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
    `library_id`            bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointed to a library of the current record.',
    `guid`                  char(32) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL COMMENT 'A globally unique identifier (GUID).',
    `name`                  varchar(100)                                          NOT NULL COMMENT 'This is the name of the module set.',
    `description`           text DEFAULT NULL COMMENT 'Description or explanation about the module set or use of the module set.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this MODULE_SET.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.',
    PRIMARY KEY (`module_set_id`),
    KEY                     `module_set_created_by_fk` (`created_by`),
    KEY                     `module_set_last_updated_by_fk` (`last_updated_by`),
    KEY                     `module_set_library_id_fk` (`library_id`),
    CONSTRAINT `module_set_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `module_set_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `module_set_library_id_fk` FOREIGN KEY (`library_id`) REFERENCES `library` (`library_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;