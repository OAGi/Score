CREATE TABLE `exception`
(
    `exception_id`       bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
    `tag`                varchar(50) DEFAULT NULL COMMENT 'A tag of the exception for the purpose of the searching facilitation',
    `message`            text        DEFAULT NULL COMMENT 'The exception message.',
    `stacktrace`         mediumblob  DEFAULT NULL COMMENT 'The serialized stacktrace object.',
    `created_by`         bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who is working on when the exception occurs.',
    `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the exception was created.',
    PRIMARY KEY (`exception_id`),
    KEY                  `exception_created_by_fk` (`created_by`),
    KEY                  `exception_tag_idx` (`tag`),
    CONSTRAINT `exception_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;