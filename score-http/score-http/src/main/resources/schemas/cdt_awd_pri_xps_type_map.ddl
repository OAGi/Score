CREATE TABLE `cdt_awd_pri_xps_type_map`
(
    `cdt_awd_pri_xps_type_map_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
    `cdt_awd_pri_id`              bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the CDT_AWD_PRI table.',
    `xbt_id`                      bigint(20) unsigned NOT NULL COMMENT 'Foreign key and to the XBT table. It identifies the XML schema built-in types that can be mapped to the CDT primivite identified in the CDT_AWD_PRI_ID column. The CDT primitives are typically broad and hence it usually maps to more than one XML schema built-in types.',
    `is_default`                  tinyint(1)          NOT NULL DEFAULT '0' COMMENT 'Indicating a default value domain mapping.',
    PRIMARY KEY (`cdt_awd_pri_xps_type_map_id`),
    KEY `cdt_awd_pri_xps_type_map_cdt_awd_pri_id_fk` (`cdt_awd_pri_id`),
    KEY `cdt_awd_pri_xps_type_map_xbt_id_fk` (`xbt_id`),
    CONSTRAINT `cdt_awd_pri_xps_type_map_cdt_awd_pri_id_fk` FOREIGN KEY (`cdt_awd_pri_id`) REFERENCES `cdt_awd_pri` (`cdt_awd_pri_id`),
    CONSTRAINT `cdt_awd_pri_xps_type_map_xbt_id_fk` FOREIGN KEY (`xbt_id`) REFERENCES `xbt` (`xbt_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='This table allows for concrete mapping between the CDT Primitives and types in a particular expression such as XML Schema, JSON. At this point, it is not clear whether a separate table will be needed for each expression. The current table holds the map to XML Schema built-in types. \n\nFor each additional expression, a column similar to the XBT_ID column will need to be added to this table for mapping to data types in another expression.\n\nIf we use a separate table for each expression, then we need binding all the way to BDT (or even BBIE) for every new expression. That would be almost like just store a BDT file. But using a column may not work with all kinds of expressions, particulary if it does not map well to the XML schema data types. ';