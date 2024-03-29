CREATE TABLE `module`
(
    `module_id`             bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
    `module_set_id`         bigint(20) unsigned NOT NULL COMMENT 'This indicates a module set.',
    `parent_module_id`      bigint(20) unsigned DEFAULT NULL COMMENT 'This indicates a parent module id. root module will be NULL.',
    `type`                  varchar(45)  NOT NULL COMMENT 'This is a type column for indicates module is FILE or DIRECTORY.',
    `path`                  text         NOT NULL COMMENT 'Absolute path to the module.',
    `name`                  varchar(100) NOT NULL COMMENT 'The is the filename of the module. The reason to not including the extension is that the extension maybe dependent on the expression. For XML schema, ''.xsd'' maybe added; or for JSON, ''.json'' maybe added as the file extension.',
    `namespace_id`          bigint(20) unsigned DEFAULT NULL COMMENT 'Note that a release record has a namespace associated. The NAMESPACE_ID, if specified here, overrides the release''s namespace. However, the NAMESPACE_ID associated with the component takes the highest precedence.',
    `version_num`           varchar(45) DEFAULT NULL COMMENT 'This is the version number to be assigned to the schema module.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this MODULE.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record. \n\nIn the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).',
    `owner_user_id`         bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table identifying the user who can update or delete the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.',
    PRIMARY KEY (`module_id`),
    KEY                     `module_namespace_id_fk` (`namespace_id`),
    KEY                     `module_owner_user_id_fk` (`owner_user_id`),
    KEY                     `module_created_by_fk` (`created_by`),
    KEY                     `module_last_updated_by_fk` (`last_updated_by`),
    KEY                     `module_module_set_id_fk` (`module_set_id`),
    KEY                     `module_parent_module_id_fk` (`parent_module_id`),
    CONSTRAINT `module_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `module_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `module_module_set_id_fk` FOREIGN KEY (`module_set_id`) REFERENCES `module_set` (`module_set_id`),
    CONSTRAINT `module_namespace_id_fk` FOREIGN KEY (`namespace_id`) REFERENCES `namespace` (`namespace_id`),
    CONSTRAINT `module_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `module_parent_module_id_fk` FOREIGN KEY (`parent_module_id`) REFERENCES `module` (`module_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='The module table stores information about a physical file, into which CC components will be generated during the expression generation.';