CREATE TABLE `biz_ctx`
(
    `biz_ctx_id`            bigint(20) unsigned          NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
    `guid`                  char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
    `name`                  varchar(100) DEFAULT NULL COMMENT 'Short, descriptive name of the business context.',
    `created_by`            bigint(20) unsigned          NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the user who creates the entity. ',
    `last_updated_by`       bigint(20) unsigned          NOT NULL COMMENT 'Foreign key to the APP_USER table  referring to the last user who has updated the business context.',
    `creation_timestamp`    datetime(6)                  NOT NULL COMMENT 'Timestamp when the business context record was first created. ',
    `last_update_timestamp` datetime(6)                  NOT NULL COMMENT 'The timestamp when the business context was last updated.',
    PRIMARY KEY (`biz_ctx_id`),
    UNIQUE KEY `biz_ctx_uk1` (`guid`),
    KEY `biz_ctx_created_by_fk` (`created_by`),
    KEY `biz_ctx_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `biz_ctx_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `biz_ctx_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='This table represents a business context. A business context is a combination of one or more business context values.';