CREATE TABLE `github_issue_bccp_manifest`
(
    `github_issue_bccp_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
    `bccp_manifest_id`              bigint(20) unsigned NOT NULL COMMENT 'Foreign key to bccp_manifest; the BCCP (in a given release) linked to a GitHub issue.',
    `github_issue_id`               bigint(20) unsigned NOT NULL COMMENT 'Foreign key to github_issue; the linked GitHub issue.',
    `created_by`                    bigint(20) unsigned NOT NULL COMMENT 'Foreign key to app_user; who created the link.',
    `last_updated_by`               bigint(20) unsigned NOT NULL COMMENT 'Foreign key to app_user; who last updated the link.',
    `creation_timestamp`            datetime(6) NOT NULL COMMENT 'Timestamp when this record was created.',
    `last_update_timestamp`         datetime(6) NOT NULL COMMENT 'Timestamp when this record was last updated.',
    PRIMARY KEY (`github_issue_bccp_manifest_id`),
    UNIQUE KEY `github_issue_bccp_manifest_uk1` (`bccp_manifest_id`,`github_issue_id`),
    KEY                             `github_issue_bccp_manifest_github_issue_id_fk` (`github_issue_id`),
    KEY                             `github_issue_bccp_manifest_created_by_fk` (`created_by`),
    KEY                             `github_issue_bccp_manifest_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `github_issue_bccp_manifest_bccp_manifest_id_fk` FOREIGN KEY (`bccp_manifest_id`) REFERENCES `bccp_manifest` (`bccp_manifest_id`),
    CONSTRAINT `github_issue_bccp_manifest_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `github_issue_bccp_manifest_github_issue_id_fk` FOREIGN KEY (`github_issue_id`) REFERENCES `github_issue` (`github_issue_id`),
    CONSTRAINT `github_issue_bccp_manifest_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='Links a BCCP manifest to a GitHub issue (many-to-many).';