CREATE TABLE `user_tenant`
(
    `user_tenant_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
    `tenant_id`      bigint(20) unsigned NOT NULL COMMENT 'Assigned tenant to the user.',
    `app_user_id`    bigint(20) unsigned NOT NULL COMMENT 'Application user.',
    PRIMARY KEY (`user_tenant_id`),
    UNIQUE KEY `user_tenant_pair` (`tenant_id`,`app_user_id`),
    KEY              `user_tenant_tenant_id_fk` (`tenant_id`),
    KEY              `user_tenant_tenant_id_app_user_id_fk` (`app_user_id`),
    CONSTRAINT `user_tenant_tenant_id_app_user_id_fk` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `user_tenant_tenant_id_fk` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This table captures the tenant roles of the user';