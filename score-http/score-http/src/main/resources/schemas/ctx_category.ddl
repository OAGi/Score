CREATE TABLE `ctx_category`
(
    `ctx_category_id`       bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary, database key.',
    `guid`                  char(32) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL COMMENT 'A globally unique identifier (GUID).',
    `name`                  varchar(45) DEFAULT NULL COMMENT 'Short name of the context category.',
    `description`           text        DEFAULT NULL COMMENT 'Explanation of what the context category is.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created the context category.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It identifies the user who last updated the context category.',
    `creation_timestamp`    datetime(6) NOT NULL DEFAULT current_timestamp (6) COMMENT 'Timestamp when the context category was created.',
    `last_update_timestamp` datetime(6) NOT NULL DEFAULT current_timestamp (6) COMMENT 'Timestamp when the context category was last updated.',
    PRIMARY KEY (`ctx_category_id`),
    UNIQUE KEY `ctx_category_uk1` (`guid`),
    KEY                     `ctx_category_created_by_fk` (`created_by`),
    KEY                     `ctx_category_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `ctx_category_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `ctx_category_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This table captures the context category. Examples of context categories as described in the CCTS are business process, industry, etc.';