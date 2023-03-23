CREATE TABLE `ctx_scheme`
(
    `ctx_scheme_id`         bigint(20) unsigned          NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary, database key.',
    `guid`                  char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
    `scheme_id`             varchar(45)                  NOT NULL COMMENT 'External identification of the scheme. ',
    `scheme_name`           varchar(255)        DEFAULT NULL COMMENT 'Pretty print name of the context scheme.',
    `description`           text COMMENT 'Description of the context scheme.',
    `scheme_agency_id`      varchar(45)                  NOT NULL COMMENT 'Identification of the agency maintaining the scheme. This column currently does not use the AGENCY_ID_LIST table. It is just a free form text at this point.',
    `scheme_version_id`     varchar(45)                  NOT NULL COMMENT 'Version number of the context scheme.',
    `ctx_category_id`       bigint(20) unsigned          NOT NULL COMMENT 'This the foreign key to the CTX_CATEGORY table. It identifies the context category associated with this context scheme.',
    `code_list_id`          bigint(20) unsigned DEFAULT NULL COMMENT 'This is the foreign key to the CODE_LIST table. It identifies the code list associated with this context scheme.',
    `created_by`            bigint(20) unsigned          NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this context scheme.',
    `last_updated_by`       bigint(20) unsigned          NOT NULL COMMENT 'Foreign key to the APP_USER table. It identifies the user who last updated the context scheme.',
    `creation_timestamp`    datetime(6)                  NOT NULL COMMENT 'Timestamp when the scheme was created.',
    `last_update_timestamp` datetime(6)                  NOT NULL COMMENT 'Timestamp when the scheme was last updated.',
    PRIMARY KEY (`ctx_scheme_id`),
    UNIQUE KEY `ctx_scheme_uk1` (`guid`),
    KEY `ctx_scheme_ctx_category_id_fk` (`ctx_category_id`),
    KEY `ctx_scheme_created_by_fk` (`created_by`),
    KEY `ctx_scheme_last_updated_by_fk` (`last_updated_by`),
    KEY `ctx_scheme_code_list_id_fk` (`code_list_id`),
    CONSTRAINT `ctx_scheme_code_list_id_fk` FOREIGN KEY (`code_list_id`) REFERENCES `code_list` (`code_list_id`),
    CONSTRAINT `ctx_scheme_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `ctx_scheme_ctx_category_id_fk` FOREIGN KEY (`ctx_category_id`) REFERENCES `ctx_category` (`ctx_category_id`),
    CONSTRAINT `ctx_scheme_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='This table represents a context scheme (a classification scheme) for a context category.';