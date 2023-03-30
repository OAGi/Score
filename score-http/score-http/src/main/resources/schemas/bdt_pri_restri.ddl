CREATE TABLE `bdt_pri_restri`
(
    `bdt_pri_restri_id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
    `bdt_manifest_id`             bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the DT_MANIFEST table. It shall point to only DT that is a BDT (not a CDT).',
    `cdt_awd_pri_xps_type_map_id` bigint(20) unsigned          DEFAULT NULL COMMENT 'This is a foreign key to the CDT_AWD_PRI_XPS_TYPE_MAP table.  It allows for a primitive restriction based on a built-in type of schema expressions.',
    `code_list_manifest_id`       bigint(20) unsigned          DEFAULT NULL COMMENT 'Foreign key to the CODE_LIST_MANIFEST table.',
    `agency_id_list_manifest_id`  bigint(20) unsigned          DEFAULT NULL COMMENT 'This is a foreign key to the AGENCY_ID_LIST_MANIFEST table. It is used in the case that the BDT content can be restricted to an agency identification.',
    `is_default`                  tinyint(1)          NOT NULL DEFAULT '0' COMMENT 'This allows overriding the default primitive assigned in the CDT_AWD_PRI_XPS_TYPE_MAP table. It typically indicates the most generic primtive for the data type.',
    PRIMARY KEY (`bdt_pri_restri_id`),
    KEY `bdt_pri_restri_cdt_awd_pri_xps_type_map_id_fk` (`cdt_awd_pri_xps_type_map_id`),
    KEY `bdt_pri_restri_bdt_manifest_id_fk` (`bdt_manifest_id`),
    KEY `bdt_pri_restri_code_list_manifest_id_fk` (`code_list_manifest_id`),
    KEY `bdt_pri_restri_agency_id_list_manifest_id_fk` (`agency_id_list_manifest_id`),
    CONSTRAINT `bdt_pri_restri_agency_id_list_manifest_id_fk` FOREIGN KEY (`agency_id_list_manifest_id`) REFERENCES `agency_id_list_manifest` (`agency_id_list_manifest_id`),
    CONSTRAINT `bdt_pri_restri_bdt_manifest_id_fk` FOREIGN KEY (`bdt_manifest_id`) REFERENCES `dt_manifest` (`dt_manifest_id`),
    CONSTRAINT `bdt_pri_restri_cdt_awd_pri_xps_type_map_id_fk` FOREIGN KEY (`cdt_awd_pri_xps_type_map_id`) REFERENCES `cdt_awd_pri_xps_type_map` (`cdt_awd_pri_xps_type_map_id`),
    CONSTRAINT `bdt_pri_restri_code_list_manifest_id_fk` FOREIGN KEY (`code_list_manifest_id`) REFERENCES `code_list_manifest` (`code_list_manifest_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='This table captures the allowed primitives for a BDT. The allowed primitives are captured by three columns the CDT_AWD_PRI_XPS_TYPE_MAP_ID, CODE_LIST_ID, and AGENCY_ID_LIST_ID. The first column specifies the primitive by the built-in type of an expression language such as the XML Schema built-in type. The second specifies the primitive, which is a code list, while the last one specifies the primitive which is an agency identification list. Only one column among the three can have a value in a particular record.';