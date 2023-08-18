CREATE TABLE `cdt_sc_awd_pri`
(
    `cdt_sc_awd_pri_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
    `cdt_sc_id`         bigint(20) unsigned NOT NULL COMMENT 'Foreign key pointing to the supplementary component (SC).',
    `cdt_pri_id`        bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointing to the CDT_Pri table. It represents a CDT primitive allowed for the suppliement component identified in the CDT_SC_ID column.',
    `is_default`        tinyint(1) NOT NULL COMMENT 'Indicating whether the primitive is the default primitive of the supplementary component.',
    PRIMARY KEY (`cdt_sc_awd_pri_id`),
    KEY                 `cdt_sc_awd_pri_cdt_sc_id_fk` (`cdt_sc_id`),
    KEY                 `cdt_sc_awd_pri_cdt_pri_id_fk` (`cdt_pri_id`),
    CONSTRAINT `cdt_sc_awd_pri_cdt_pri_id_fk` FOREIGN KEY (`cdt_pri_id`) REFERENCES `cdt_pri` (`cdt_pri_id`),
    CONSTRAINT `cdt_sc_awd_pri_cdt_sc_id_fk` FOREIGN KEY (`cdt_sc_id`) REFERENCES `dt_sc` (`dt_sc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This table capture the CDT primitives allowed for a particular SC of a CDT. It also stores the CDT primitives allowed for a SC of a BDT that extends its base (such SC is not defined in the CCTS data type catalog specification).';