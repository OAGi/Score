CREATE TABLE `cdt_ref_spec`
(
    `cdt_ref_spec_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `cdt_id`          bigint(20) unsigned NOT NULL,
    `ref_spec_id`     bigint(20) unsigned NOT NULL,
    PRIMARY KEY (`cdt_ref_spec_id`),
    KEY `cdt_ref_spec_cdt_id_fk` (`cdt_id`),
    KEY `cdt_ref_spec_ref_spec_id_fk` (`ref_spec_id`),
    CONSTRAINT `cdt_ref_spec_cdt_id_fk` FOREIGN KEY (`cdt_id`) REFERENCES `dt` (`dt_id`),
    CONSTRAINT `cdt_ref_spec_ref_spec_id_fk` FOREIGN KEY (`ref_spec_id`) REFERENCES `ref_spec` (`ref_spec_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;