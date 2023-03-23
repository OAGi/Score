CREATE TABLE `blob_content_manifest`
(
    `blob_content_manifest_id`      bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `blob_content_id`               bigint(20) unsigned NOT NULL,
    `release_id`                    bigint(20) unsigned NOT NULL,
    `conflict`                      tinyint(1)          NOT NULL DEFAULT '0' COMMENT 'This indicates that there is a conflict between self and relationship.',
    `prev_blob_content_manifest_id` bigint(20) unsigned          DEFAULT NULL,
    `next_blob_content_manifest_id` bigint(20) unsigned          DEFAULT NULL,
    PRIMARY KEY (`blob_content_manifest_id`),
    KEY `blob_content_manifest_release_id_fk` (`release_id`),
    KEY `blob_content_manifest_blob_content_id_fk` (`blob_content_id`),
    KEY `blob_content_manifest_prev_blob_content_manifest_id_fk` (`prev_blob_content_manifest_id`),
    KEY `blob_content_manifest_next_blob_content_manifest_id_fk` (`next_blob_content_manifest_id`),
    CONSTRAINT `blob_content_manifest_blob_content_id_fk` FOREIGN KEY (`blob_content_id`) REFERENCES `blob_content` (`blob_content_id`),
    CONSTRAINT `blob_content_manifest_next_blob_content_manifest_id_fk` FOREIGN KEY (`next_blob_content_manifest_id`) REFERENCES `blob_content_manifest` (`blob_content_manifest_id`),
    CONSTRAINT `blob_content_manifest_prev_blob_content_manifest_id_fk` FOREIGN KEY (`prev_blob_content_manifest_id`) REFERENCES `blob_content_manifest` (`blob_content_manifest_id`),
    CONSTRAINT `blob_content_manifest_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;