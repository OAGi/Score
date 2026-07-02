CREATE TABLE `biz_ctx_assignment`
(
    `biz_ctx_assignment_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a BIZ_CTX_ASSIGNMENT record.',
    `biz_ctx_id`            bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the biz_ctx table.',
    `top_level_asbiep_id`   bigint(20) unsigned NOT NULL COMMENT 'This is a foreign key to the top-level ASBIEP.',
    PRIMARY KEY (`biz_ctx_assignment_id`),
    UNIQUE KEY `biz_ctx_assignment_uk` (`biz_ctx_id`,`top_level_asbiep_id`),
    KEY                     `biz_ctx_id` (`biz_ctx_id`),
    KEY                     `biz_ctx_assignment_top_level_asbiep_id_fk` (`top_level_asbiep_id`),
    CONSTRAINT `biz_ctx_assignment_biz_ctx_id_fk` FOREIGN KEY (`biz_ctx_id`) REFERENCES `biz_ctx` (`biz_ctx_id`),
    CONSTRAINT `biz_ctx_assignment_top_level_asbiep_id_fk` FOREIGN KEY (`top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This is an intersection table that assigns business contexts to a top-level ASBIEP. It provides the many-to-many associations between the BIZ_CTX and TOP_LEVEL_ASBIEP tables.';