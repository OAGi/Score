CREATE TABLE `module_set_release`
(
    `module_set_release_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
    `module_set_id`         bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the module set.',
    `release_id`            bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the release.',
    `name`                  varchar(100)        NOT NULL COMMENT 'This is the name of the module set release.',
    `description`           text COMMENT 'Description or explanation about the module set release.',
    `is_default`            tinyint(1)          NOT NULL DEFAULT '0' COMMENT 'It would be a default module set if this indicator is checked. Otherwise, it would be an optional.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this MODULE_SET_RELEASE.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record.',
    `creation_timestamp`    datetime(6)         NOT NULL COMMENT 'The timestamp when the record was first created.',
    `last_update_timestamp` datetime(6)         NOT NULL COMMENT 'The timestamp when the record was last updated.',
    PRIMARY KEY (`module_set_release_id`),
    KEY `module_set_release_module_set_id_fk` (`module_set_id`),
    KEY `module_set_release_release_id_fk` (`release_id`),
    KEY `module_set_release_assignment_created_by_fk` (`created_by`),
    KEY `module_set_release_assignment_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `module_set_release_assignment_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `module_set_release_assignment_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `module_set_release_module_set_id_fk` FOREIGN KEY (`module_set_id`) REFERENCES `module_set` (`module_set_id`),
    CONSTRAINT `module_set_release_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;