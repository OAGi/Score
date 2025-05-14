CREATE TABLE `dt_awd_pri`
(
    `dt_awd_pri_id`              bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
    `release_id`                 bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table.',
    `dt_id`                      bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the DT table.',
    `xbt_manifest_id`            bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the XBT_MANIFEST table.',
    `code_list_manifest_id`      bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the CODE_LIST_MANIFEST table.',
    `agency_id_list_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the AGENCY_ID_LIST_MANIFEST table.',
    `is_default`                 tinyint(1) NOT NULL DEFAULT 0 COMMENT 'It indicates the most generic primitive for the data type.',
    PRIMARY KEY (`dt_awd_pri_id`),
    KEY                          `dt_awd_pri_release_id_fk` (`release_id`),
    KEY                          `dt_awd_pri_dt_id_fk` (`dt_id`),
    KEY                          `dt_awd_pri_xbt_manifest_id_fk` (`xbt_manifest_id`),
    KEY                          `dt_awd_pri_code_list_manifest_id_fk` (`code_list_manifest_id`),
    KEY                          `dt_awd_pri_agency_id_list_manifest_id_fk` (`agency_id_list_manifest_id`),
    CONSTRAINT `dt_awd_pri_agency_id_list_manifest_id_fk` FOREIGN KEY (`agency_id_list_manifest_id`) REFERENCES `agency_id_list_manifest` (`agency_id_list_manifest_id`),
    CONSTRAINT `dt_awd_pri_code_list_manifest_id_fk` FOREIGN KEY (`code_list_manifest_id`) REFERENCES `code_list_manifest` (`code_list_manifest_id`),
    CONSTRAINT `dt_awd_pri_dt_id_fk` FOREIGN KEY (`dt_id`) REFERENCES `dt` (`dt_id`),
    CONSTRAINT `dt_awd_pri_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
    CONSTRAINT `dt_awd_pri_xbt_manifest_id_fk` FOREIGN KEY (`xbt_manifest_id`) REFERENCES `xbt_manifest` (`xbt_manifest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This table captures the allowed primitives for a DT. The allowed primitives are captured by three columns the XBT_MANIFEST_ID, CODE_LIST_MANIFEST_ID, and AGENCY_ID_LIST_MANIFEST_ID. The first column specifies the primitive by the built-in type of an expression language such as the XML Schema built-in type. The second specifies the primitive, which is a code list, while the last one specifies the primitive which is an agency identification list. Only one column among the three can have a value in a particular record.';