CREATE TABLE `biz_ctx_value`
(
    `biz_ctx_value_id`    bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
    `biz_ctx_id`          bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the biz_ctx table.',
    `ctx_scheme_value_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the CTX_SCHEME_VALUE table.',
    PRIMARY KEY (`biz_ctx_value_id`),
    KEY                   `biz_ctx_value_biz_ctx_id_fk` (`biz_ctx_id`),
    KEY                   `biz_ctx_value_ctx_scheme_value_id_fk` (`ctx_scheme_value_id`),
    CONSTRAINT `biz_ctx_value_biz_ctx_id_fk` FOREIGN KEY (`biz_ctx_id`) REFERENCES `biz_ctx` (`biz_ctx_id`),
    CONSTRAINT `biz_ctx_value_ctx_scheme_value_id_fk` FOREIGN KEY (`ctx_scheme_value_id`) REFERENCES `ctx_scheme_value` (`ctx_scheme_value_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This table represents business context values for business contexts. It provides the associations between a business context and a context scheme value.';