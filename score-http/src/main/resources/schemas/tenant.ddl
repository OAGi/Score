CREATE TABLE `tenant`
(
    `tenant_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
    `name`      varchar(100) DEFAULT NULL COMMENT 'The meaning of the tenant.',
    PRIMARY KEY (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This table about the user tenant role.';