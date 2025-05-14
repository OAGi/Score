CREATE TABLE `app_user`
(
    `app_user_id`              bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
    `login_id`                 varchar(45) NOT NULL COMMENT 'User Id of the user.',
    `password`                 varchar(100) DEFAULT NULL COMMENT 'Password to authenticate the user.',
    `name`                     varchar(100) DEFAULT NULL COMMENT 'Full name of the user.',
    `organization`             varchar(100) DEFAULT NULL COMMENT 'The company the user represents.',
    `email`                    varchar(100) DEFAULT NULL COMMENT 'Email address.',
    `email_verified`           tinyint(1) NOT NULL DEFAULT 0 COMMENT 'The fact whether the email value is verified or not.',
    `email_verified_timestamp` datetime(6) DEFAULT NULL COMMENT 'The timestamp when the email address has verified.',
    `is_developer`             tinyint(1) DEFAULT NULL,
    `is_admin`                 tinyint(1) DEFAULT 0 COMMENT 'Indicator whether the user has an admin role or not.',
    `is_enabled`               tinyint(1) DEFAULT 1,
    PRIMARY KEY (`app_user_id`),
    UNIQUE KEY `app_user_uk1` (`login_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This table captures the user information for authentication and authorization purposes.';