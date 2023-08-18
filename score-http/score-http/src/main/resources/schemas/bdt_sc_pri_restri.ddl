CREATE TABLE `bdt_sc_pri_restri`
(
    `bdt_sc_pri_restri_id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
    `bdt_sc_manifest_id`             bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the DT_SC_MANIFEST table. It shall point to only DT that is a BDT (not a CDT).',
    `cdt_sc_awd_pri_xps_type_map_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This column is a forieng key to the CDT_SC_AWD_PRI_XPS_TYPE_MAP table. It allows for a primitive restriction based on a built-in type of schema expressions.',
    `code_list_manifest_id`          bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the CODE_LIST_MANIFEST table.',
    `agency_id_list_manifest_id`     bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the AGENCY_ID_LIST_MANIFEST table. It is used in the case that the BDT content can be restricted to an agency identification.',
    `is_default`                     tinyint(1) NOT NULL COMMENT 'This column specifies the default primitive for a BDT. It is typically the most generic primitive allowed for the BDT.',
    PRIMARY KEY (`bdt_sc_pri_restri_id`),
    KEY                              `bdt_sc_pri_restri_cdt_sc_awd_pri_xps_type_map_id_fk` (`cdt_sc_awd_pri_xps_type_map_id`),
    KEY                              `bdt_sc_pri_restri_bdt_manifest_id_fk` (`bdt_sc_manifest_id`),
    KEY                              `bdt_sc_pri_restri_code_list_manifest_id_fk` (`code_list_manifest_id`),
    KEY                              `bdt_sc_pri_restri_agency_id_list_manifest_id_fk` (`agency_id_list_manifest_id`),
    CONSTRAINT `bdt_sc_pri_restri_agency_id_list_manifest_id_fk` FOREIGN KEY (`agency_id_list_manifest_id`) REFERENCES `agency_id_list_manifest` (`agency_id_list_manifest_id`),
    CONSTRAINT `bdt_sc_pri_restri_bdt_manifest_id_fk` FOREIGN KEY (`bdt_sc_manifest_id`) REFERENCES `dt_sc_manifest` (`dt_sc_manifest_id`),
    CONSTRAINT `bdt_sc_pri_restri_cdt_sc_awd_pri_xps_type_map_id_fk` FOREIGN KEY (`cdt_sc_awd_pri_xps_type_map_id`) REFERENCES `cdt_sc_awd_pri_xps_type_map` (`cdt_sc_awd_pri_xps_type_map_id`),
    CONSTRAINT `bdt_sc_pri_restri_code_list_manifest_id_fk` FOREIGN KEY (`code_list_manifest_id`) REFERENCES `code_list_manifest` (`code_list_manifest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This table is similar to the BDT_PRI_RESTRI table but it is for the BDT SC. The allowed primitives are captured by three columns the CDT_SC_AWD_PRI_XPS_TYPE_MAP, CODE_LIST_ID, and AGENCY_ID_LIST_ID. The first column specifies the primitive by the built-in type of an expression language such as the XML Schema built-in type. The second specifies the primitive, which is a code list, while the last one specifies the primitive which is an agency identification list. Only one column among the three can have a value in a particular record.\n\nIt should be noted that the table does not store the fact about primitive restriction hierarchical relationships. In other words, if a BDT SC is derived from another BDT SC and the derivative BDT SC applies some primitive restrictions, that relationship will not be explicitly stored. The derivative BDT SC points directly to the CDT_AWD_PRI_XPS_TYPE_MAP key rather than the BDT_SC_PRI_RESTRI key.';