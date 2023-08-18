CREATE TABLE `cdt_sc_ref_spec`
(
    `cdt_sc_ref_spec_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `cdt_sc_id`          bigint(20) unsigned NOT NULL,
    `ref_spec_id`        bigint(20) unsigned NOT NULL,
    PRIMARY KEY (`cdt_sc_ref_spec_id`),
    KEY                  `cdt_sc_ref_spec_cdt_sc_id_fk` (`cdt_sc_id`),
    KEY                  `cdt_sc_ref_spec_ref_spec_id_fk` (`ref_spec_id`),
    CONSTRAINT `cdt_sc_ref_spec_cdt_sc_id_fk` FOREIGN KEY (`cdt_sc_id`) REFERENCES `dt_sc` (`dt_sc_id`),
    CONSTRAINT `cdt_sc_ref_spec_ref_spec_id_fk` FOREIGN KEY (`ref_spec_id`) REFERENCES `ref_spec` (`ref_spec_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;