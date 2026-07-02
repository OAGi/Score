CREATE TABLE `blob_content_manifest`
(
    `blob_content_manifest_id`      bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a BLOB_CONTENT_MANIFEST record.',
    `blob_content_id`               bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the BLOB_CONTENT table.',
    `release_id`                    bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table.',
    `conflict`                      tinyint(1) NOT NULL DEFAULT 0 COMMENT 'This indicates that there is a conflict between self and relationship.',
    `prev_blob_content_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding BLOB_CONTENT_MANIFEST record in the previous release (revision chain). NULL for the first revision.',
    `next_blob_content_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding BLOB_CONTENT_MANIFEST record in the next release (revision chain). NULL for the latest revision.',
    PRIMARY KEY (`blob_content_manifest_id`),
    KEY                             `blob_content_manifest_release_id_fk` (`release_id`),
    KEY                             `blob_content_manifest_blob_content_id_fk` (`blob_content_id`),
    KEY                             `blob_content_manifest_prev_blob_content_manifest_id_fk` (`prev_blob_content_manifest_id`),
    KEY                             `blob_content_manifest_next_blob_content_manifest_id_fk` (`next_blob_content_manifest_id`),
    CONSTRAINT `blob_content_manifest_blob_content_id_fk` FOREIGN KEY (`blob_content_id`) REFERENCES `blob_content` (`blob_content_id`),
    CONSTRAINT `blob_content_manifest_next_blob_content_manifest_id_fk` FOREIGN KEY (`next_blob_content_manifest_id`) REFERENCES `blob_content_manifest` (`blob_content_manifest_id`),
    CONSTRAINT `blob_content_manifest_prev_blob_content_manifest_id_fk` FOREIGN KEY (`prev_blob_content_manifest_id`) REFERENCES `blob_content_manifest` (`blob_content_manifest_id`),
    CONSTRAINT `blob_content_manifest_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='The BLOB_CONTENT_MANIFEST table is a release-specific handle to a BLOB_CONTENT record, which stores a schema whose content is only imported as a whole and is represented in Blob. It pins the BLOB_CONTENT to a RELEASE and carries the revision chain via its previous and next manifest self-references across releases.';