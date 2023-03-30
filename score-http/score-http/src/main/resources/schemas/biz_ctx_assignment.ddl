CREATE TABLE `biz_ctx_assignment`
(
    `biz_ctx_assignment_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `biz_ctx_id`            bigint(20) unsigned NOT NULL,
    `top_level_asbiep_id`   bigint(20) unsigned NOT NULL COMMENT 'This is a foreign key to the top-level ASBIEP.',
    PRIMARY KEY (`biz_ctx_assignment_id`),
    UNIQUE KEY `biz_ctx_assignment_uk` (`biz_ctx_id`, `top_level_asbiep_id`),
    KEY `biz_ctx_id` (`biz_ctx_id`),
    KEY `biz_ctx_assignment_top_level_asbiep_id_fk` (`top_level_asbiep_id`),
    CONSTRAINT `biz_ctx_assignment_biz_ctx_id_fk` FOREIGN KEY (`biz_ctx_id`) REFERENCES `biz_ctx` (`biz_ctx_id`),
    CONSTRAINT `biz_ctx_assignment_top_level_asbiep_id_fk` FOREIGN KEY (`top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;