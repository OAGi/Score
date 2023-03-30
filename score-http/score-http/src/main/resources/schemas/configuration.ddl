CREATE TABLE `configuration`
(
    `configuration_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
    `name`             varchar(100) DEFAULT NULL COMMENT 'The name of configuration property.',
    `type`             varchar(100) DEFAULT NULL COMMENT 'The type of configuration property.',
    `value`            varchar(100) DEFAULT NULL COMMENT 'The value of configuration property.',
    PRIMARY KEY (`configuration_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='The table stores configuration properties of the application.';