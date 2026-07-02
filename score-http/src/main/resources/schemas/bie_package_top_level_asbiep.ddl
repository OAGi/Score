CREATE TABLE `bie_package_top_level_asbiep`
(
    `bie_package_top_level_asbiep_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the BIE package-Top-Level ASBIEP record.',
    `bie_package_id`                  bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the BIE package.',
    `top_level_asbiep_id`             bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the TOP_LEVEL_ASBIEP_ID which has linked to the BIE package. The release ID of this record must be the same to the BIE package''s release ID.',
    `prev_top_level_asbiep_id`        bigint(20) unsigned DEFAULT NULL COMMENT 'A foreign key referring to the previous version of the Top-Level ASBIEP record, if any. Used to track version history within the BIE package.',
    `created_by`                      bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who adds the record into the BIE package.',
    `creation_timestamp`              datetime(6) NOT NULL COMMENT 'Timestamp when this record was first created.',
    PRIMARY KEY (`bie_package_top_level_asbiep_id`),
    UNIQUE KEY `bie_package_top_level_asbiep_uk1` (`bie_package_id`,`top_level_asbiep_id`),
    KEY                               `bie_package_top_level_asbiep_bie_package_id_fk` (`bie_package_id`),
    KEY                               `bie_package_top_level_asbiep_top_level_asbiep_id_fk` (`top_level_asbiep_id`),
    KEY                               `bie_package_top_level_asbiep_prev_top_level_asbiep_id_fk` (`prev_top_level_asbiep_id`),
    CONSTRAINT `bie_package_top_level_asbiep_bie_package_id_fk` FOREIGN KEY (`bie_package_id`) REFERENCES `bie_package` (`bie_package_id`),
    CONSTRAINT `bie_package_top_level_asbiep_prev_top_level_asbiep_id_fk` FOREIGN KEY (`prev_top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`),
    CONSTRAINT `bie_package_top_level_asbiep_top_level_asbiep_id_fk` FOREIGN KEY (`top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='This is an intersection table that assigns TOP_LEVEL_ASBIEP records to a BIE_PACKAGE, capturing the top-level BIEs that make up the package. The referenced TOP_LEVEL_ASBIEP must belong to the same RELEASE as the BIE_PACKAGE, and the PREV_TOP_LEVEL_ASBIEP_ID column tracks the previous version of the Top-Level ASBIEP within the package.';