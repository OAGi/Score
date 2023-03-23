CREATE TABLE `cdt_pri`
(
    `cdt_pri_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
    `name`       varchar(45)         NOT NULL COMMENT 'Name of the CDT primitive per the CCTS datatype catalog, e.g., Decimal.',
    PRIMARY KEY (`cdt_pri_id`),
    UNIQUE KEY `cdt_pri_uk1` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='This table stores the CDT primitives.';