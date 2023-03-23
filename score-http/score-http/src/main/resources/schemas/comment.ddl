CREATE TABLE `comment`
(
    `comment_id`            bigint(20) unsigned              NOT NULL AUTO_INCREMENT,
    `reference`             varchar(100) CHARACTER SET ascii NOT NULL DEFAULT '',
    `comment`               text,
    `is_hidden`             tinyint(1)                       NOT NULL DEFAULT '0',
    `is_deleted`            tinyint(1)                       NOT NULL DEFAULT '0',
    `prev_comment_id`       bigint(20) unsigned                       DEFAULT NULL,
    `created_by`            bigint(20) unsigned              NOT NULL,
    `creation_timestamp`    datetime(6)                      NOT NULL,
    `last_update_timestamp` datetime(6)                      NOT NULL,
    PRIMARY KEY (`comment_id`),
    KEY `reference` (`reference`),
    KEY `comment_created_by_fk` (`created_by`),
    KEY `comment_prev_comment_id_fk` (`prev_comment_id`),
    CONSTRAINT `comment_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `comment_prev_comment_id_fk` FOREIGN KEY (`prev_comment_id`) REFERENCES `comment` (`comment_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;