CREATE TABLE `tag`
(
    `tag_id`                bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a tag record.',
    `name`                  varchar(100)        NOT NULL COMMENT 'The name of the tag.',
    `description`           text COMMENT 'The description of the tag.',
    `text_color`            varchar(10)         NOT NULL COMMENT 'The text color of the tag.',
    `background_color`      varchar(10)         NOT NULL COMMENT 'The background color of the tag.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the tag record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the tag record.',
    `creation_timestamp`    datetime(6)         NOT NULL COMMENT 'Timestamp when the tag record was first created.',
    `last_update_timestamp` datetime(6)         NOT NULL COMMENT 'The timestamp when the tag was last updated.',
    PRIMARY KEY (`tag_id`),
    KEY `tag_created_by_fk` (`created_by`),
    KEY `tag_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `tag_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `tag_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;