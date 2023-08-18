CREATE TABLE `cdt_awd_pri`
(
    `cdt_awd_pri_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
    `cdt_id`         bigint(20) unsigned NOT NULL COMMENT 'Foreign key pointing to a CDT in the DT table.',
    `cdt_pri_id`     bigint(20) unsigned NOT NULL COMMENT 'Foreign key from the CDT_PRI table. It indicates the primative allowed for the CDT identified in the CDT_ID column. ',
    `is_default`     tinyint(1) NOT NULL COMMENT 'Indicating a default primitive for the CDT?s Content Component. True for a default primitive; False otherwise.',
    PRIMARY KEY (`cdt_awd_pri_id`),
    KEY              `cdt_awd_pri_cdt_id_fk` (`cdt_id`),
    KEY              `cdt_awd_pri_cdt_pri_id_fk` (`cdt_pri_id`),
    CONSTRAINT `cdt_awd_pri_cdt_id_fk` FOREIGN KEY (`cdt_id`) REFERENCES `dt` (`dt_id`),
    CONSTRAINT `cdt_awd_pri_cdt_pri_id_fk` FOREIGN KEY (`cdt_pri_id`) REFERENCES `cdt_pri` (`cdt_pri_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This table capture allowed primitives of the CDT?s Content Component.  The information in this table is captured from the Allowed Primitive column in each of the CDT Content Component section/table in CCTS DTC3.';