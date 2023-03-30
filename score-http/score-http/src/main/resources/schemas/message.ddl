CREATE TABLE `message`
(
    `message_id`         bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `sender_id`          bigint(20) unsigned NOT NULL COMMENT 'The user who created this record.',
    `recipient_id`       bigint(20) unsigned NOT NULL COMMENT 'The user who is a target to possess this record.',
    `subject`            text COMMENT 'A subject of the message',
    `body`               mediumtext COMMENT 'A body of the message.',
    `body_content_type`  varchar(50)         NOT NULL DEFAULT 'text/plain' COMMENT 'A content type of the body',
    `is_read`            tinyint(1)                   DEFAULT '0' COMMENT 'An indicator whether this record is read or not.',
    `creation_timestamp` datetime(6)         NOT NULL COMMENT 'The timestamp when the record was first created.',
    PRIMARY KEY (`message_id`),
    KEY `message_sender_id_fk` (`sender_id`),
    KEY `message_recipient_id_fk` (`recipient_id`),
    CONSTRAINT `message_recipient_id_fk` FOREIGN KEY (`recipient_id`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `message_sender_id_fk` FOREIGN KEY (`sender_id`) REFERENCES `app_user` (`app_user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;