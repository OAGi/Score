CREATE TABLE `asbiep_support_doc`
(
    `asbiep_support_doc_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key. Unique identifier for each supporting documentation.',
    `asbiep_id`             bigint(20) unsigned NOT NULL COMMENT 'Foreign key. References the related ASBIEP record.',
    `content`               text DEFAULT NULL COMMENT 'The main body or text content of the supporting documentation.',
    `description`           text DEFAULT NULL COMMENT 'Optional description, summary, or metadata about the supporting documentation.',
    PRIMARY KEY (`asbiep_support_doc_id`),
    KEY                     `asbiep_support_doc_asbiep_id_fk` (`asbiep_id`),
    CONSTRAINT `asbiep_support_doc_asbiep_id_fk` FOREIGN KEY (`asbiep_id`) REFERENCES `asbiep` (`asbiep_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Table storing supporting documentations linked to ASBIEP records.';