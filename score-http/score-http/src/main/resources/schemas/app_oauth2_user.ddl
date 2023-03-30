CREATE TABLE `app_oauth2_user`
(
    `app_oauth2_user_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
    `app_user_id`        bigint(20) unsigned DEFAULT NULL COMMENT 'A reference to the record in `app_user`. If it is not set, this is treated as a pending record.',
    `oauth2_app_id`      bigint(20) unsigned NOT NULL COMMENT 'A reference to the record in `oauth2_app`.',
    `sub`                varchar(100)        NOT NULL COMMENT '`sub` claim defined in OIDC spec. This is a unique identifier of the subject in the provider.',
    `name`               varchar(200)        DEFAULT NULL COMMENT '`name` claim defined in OIDC spec.',
    `email`              varchar(200)        DEFAULT NULL COMMENT '`email` claim defined in OIDC spec.',
    `nickname`           varchar(200)        DEFAULT NULL COMMENT '`nickname` claim defined in OIDC spec.',
    `preferred_username` varchar(200)        DEFAULT NULL COMMENT '`preferred_username` claim defined in OIDC spec.',
    `phone_number`       varchar(200)        DEFAULT NULL COMMENT '`phone_number` claim defined in OIDC spec.',
    `creation_timestamp` datetime(6)         NOT NULL COMMENT 'Timestamp when this record is created.',
    PRIMARY KEY (`app_oauth2_user_id`),
    UNIQUE KEY `app_oauth2_user_uk1` (`oauth2_app_id`, `sub`),
    KEY `app_oauth2_user_app_user_id_fk` (`app_user_id`),
    CONSTRAINT `app_oauth2_user_app_user_id_fk` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `app_oauth2_user_oauth2_app_id_fk` FOREIGN KEY (`oauth2_app_id`) REFERENCES `oauth2_app` (`oauth2_app_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;