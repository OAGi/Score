CREATE TABLE `blob_content`
(
    `blob_content_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
    `content`         mediumblob NOT NULL COMMENT 'The Blob content of the schema file.',
    PRIMARY KEY (`blob_content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This table stores schemas whose content is only imported as a whole and is represented in Blob.';