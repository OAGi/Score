CREATE TABLE `cdt_sc_awd_pri_xps_type_map`
(
    `cdt_sc_awd_pri_xps_type_map_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
    `cdt_sc_awd_pri_id`              bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the CDT_SC_AWD_PRI table.',
    `xbt_id`                         bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the Xbt table. It identifies an XML schema built-in type that maps to the CDT SC Allowed Primitive identified in the CDT_SC_AWD_PRI column.',
    `is_default`                     tinyint(1) NOT NULL DEFAULT 0 COMMENT 'Indicating a default value domain mapping.',
    PRIMARY KEY (`cdt_sc_awd_pri_xps_type_map_id`),
    KEY                              `cdt_sc_awd_pri_xps_type_map_cdt_sc_awd_pri_id_fk` (`cdt_sc_awd_pri_id`),
    KEY                              `cdt_sc_awd_pri_xps_type_map_xbt_id_fk` (`xbt_id`),
    CONSTRAINT `cdt_sc_awd_pri_xps_type_map_cdt_sc_awd_pri_id_fk` FOREIGN KEY (`cdt_sc_awd_pri_id`) REFERENCES `cdt_sc_awd_pri` (`cdt_sc_awd_pri_id`),
    CONSTRAINT `cdt_sc_awd_pri_xps_type_map_xbt_id_fk` FOREIGN KEY (`xbt_id`) REFERENCES `xbt` (`xbt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='The purpose of this table is the same as that of the CDT_AWD_PRI_XPS_TYPE_MAP, but it is for the supplementary component (SC). It allows for the concrete mapping between the CDT Primitives and types in a particular expression such as XML Schema, JSON. ';