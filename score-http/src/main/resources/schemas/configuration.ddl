CREATE TABLE `configuration`
(
    `configuration_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
    `name`             varchar(100) DEFAULT NULL COMMENT 'The name of configuration property.',
    `type`             varchar(100) DEFAULT NULL COMMENT 'The type of configuration property.',
    `value`            longtext     DEFAULT NULL COMMENT 'The value of configuration property.',
    PRIMARY KEY (`configuration_id`),
    UNIQUE KEY `configuration_uk1` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='The table stores configuration properties of the application.';