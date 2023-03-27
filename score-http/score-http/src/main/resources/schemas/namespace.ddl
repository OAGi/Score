CREATE TABLE `namespace`
(
    `namespace_id`          bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
    `uri`                   varchar(100)        NOT NULL COMMENT 'This is the URI of the namespace.',
    `prefix`                varchar(45)                  DEFAULT NULL COMMENT 'This is a default short name to represent the URI. It may be overridden during the expression generation. Null or empty means the same thing like the default prefix in an XML schema.',
    `description`           text COMMENT 'Description or explanation about the namespace or use of the namespace.',
    `is_std_nmsp`           tinyint(1)          NOT NULL DEFAULT '0' COMMENT 'This indicates whether the namespace is reserved for standard used (i.e., whether it is an OAGIS namespace). If it is true, then end users cannot user the namespace for the end user CCs.',
    `owner_user_id`         bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table identifying the user who can update or delete the record.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table identifying user who created the namespace.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table identifying the user who last updated the record.',
    `creation_timestamp`    datetime(6)         NOT NULL COMMENT 'The timestamp when the record was first created.',
    `last_update_timestamp` datetime(6)         NOT NULL COMMENT 'The timestamp when the record was last updated.',
    PRIMARY KEY (`namespace_id`),
    UNIQUE KEY `namespace_uk1` (`uri`),
    KEY `namespace_owner_user_id_fk` (`owner_user_id`),
    KEY `namespace_created_by_fk` (`created_by`),
    KEY `namespace_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `namespace_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `namespace_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `namespace_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='This table stores information about a namespace. Namespace is the namespace as in the XML schema specification.';