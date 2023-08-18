CREATE TABLE `ctx_scheme_value`
(
    `ctx_scheme_value_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
    `guid`                char(32) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL COMMENT 'A globally unique identifier (GUID).',
    `value`               varchar(100)                                          NOT NULL DEFAULT '' COMMENT 'A short value for the scheme value similar to the code list value.',
    `meaning`             text                                                           DEFAULT NULL COMMENT 'The description, explanatiion of the scheme value.',
    `owner_ctx_scheme_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the CTX_SCHEME table. It identifies the context scheme, to which this scheme value belongs.',
    PRIMARY KEY (`ctx_scheme_value_id`),
    UNIQUE KEY `ctx_scheme_value_uk1` (`guid`),
    KEY                   `ctx_scheme_value_owner_ctx_scheme_id_fk` (`owner_ctx_scheme_id`),
    CONSTRAINT `ctx_scheme_value_owner_ctx_scheme_id_fk` FOREIGN KEY (`owner_ctx_scheme_id`) REFERENCES `ctx_scheme` (`ctx_scheme_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This table stores the context scheme values for a particular context scheme in the CTX_SCHEME table.';