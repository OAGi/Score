CREATE TABLE `message`
(
    `message_id`         bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a MESSAGE record.',
    `sender_id`          bigint(20) unsigned NOT NULL COMMENT 'The user who sends this message.',
    `recipient_id`       bigint(20) unsigned NOT NULL COMMENT 'The user who receives this message.',
    `subject`            text                 DEFAULT NULL COMMENT 'A subject of the message',
    `body`               mediumtext           DEFAULT NULL COMMENT 'A body of the message.',
    `body_content_type`  varchar(50) NOT NULL DEFAULT 'text/plain' COMMENT 'A content type of the body',
    `is_read`            tinyint(1) DEFAULT 0 COMMENT 'An indicator whether this record is read or not.',
    `creation_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.',
    PRIMARY KEY (`message_id`),
    KEY                  `message_sender_id_fk` (`sender_id`),
    KEY                  `message_recipient_id_fk` (`recipient_id`),
    CONSTRAINT `message_recipient_id_fk` FOREIGN KEY (`recipient_id`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `message_sender_id_fk` FOREIGN KEY (`sender_id`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='This table stores messages exchanged between users, referencing APP_USER for both the sender and the recipient, along with each message''s subject, body, and read status.';