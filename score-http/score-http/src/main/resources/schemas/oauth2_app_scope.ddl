CREATE TABLE `oauth2_app_scope`
(
    `oauth2_app_scope_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `oauth2_app_id`       bigint(20) unsigned NOT NULL,
    `scope`               varchar(100)        NOT NULL,
    PRIMARY KEY (`oauth2_app_scope_id`),
    KEY `oauth2_app_scope_oauth2_app_id_fk` (`oauth2_app_id`),
    CONSTRAINT `oauth2_app_scope_oauth2_app_id_fk` FOREIGN KEY (`oauth2_app_id`) REFERENCES `oauth2_app` (`oauth2_app_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;