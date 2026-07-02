CREATE TABLE `xbt_manifest`
(
    `xbt_manifest_id`      bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a XBT_MANIFEST record.',
    `release_id`           bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table.',
    `xbt_id`               bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the XBT table.',
    `cdt_pri_id`           bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key referencing the CDT_PRI table. Specifies how the current record maps to allowed primitives in CDT.',
    `conflict`             tinyint(1) NOT NULL DEFAULT 0 COMMENT 'This indicates that there is a conflict between self and relationship.',
    `log_id`               bigint(20) unsigned DEFAULT NULL COMMENT 'A foreign key pointed to a log for the current record.',
    `prev_xbt_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding XBT_MANIFEST record in the previous release (revision chain). NULL for the first revision.',
    `next_xbt_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding XBT_MANIFEST record in the next release (revision chain). NULL for the latest revision.',
    PRIMARY KEY (`xbt_manifest_id`),
    KEY                    `xbt_manifest_xbt_id_fk` (`xbt_id`),
    KEY                    `xbt_manifest_release_id_fk` (`release_id`),
    KEY                    `xbt_manifest_log_id_fk` (`log_id`),
    KEY                    `xbt_manifest_prev_xbt_manifest_id_fk` (`prev_xbt_manifest_id`),
    KEY                    `xbt_manifest_next_xbt_manifest_id_fk` (`next_xbt_manifest_id`),
    KEY                    `xbt_manifest_cdt_pri_id_fk` (`cdt_pri_id`),
    CONSTRAINT `xbt_manifest_cdt_pri_id_fk` FOREIGN KEY (`cdt_pri_id`) REFERENCES `cdt_pri` (`cdt_pri_id`),
    CONSTRAINT `xbt_manifest_log_id_fk` FOREIGN KEY (`log_id`) REFERENCES `log` (`log_id`),
    CONSTRAINT `xbt_manifest_next_xbt_manifest_id_fk` FOREIGN KEY (`next_xbt_manifest_id`) REFERENCES `xbt_manifest` (`xbt_manifest_id`),
    CONSTRAINT `xbt_manifest_prev_xbt_manifest_id_fk` FOREIGN KEY (`prev_xbt_manifest_id`) REFERENCES `xbt_manifest` (`xbt_manifest_id`),
    CONSTRAINT `xbt_manifest_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
    CONSTRAINT `xbt_manifest_xbt_id_fk` FOREIGN KEY (`xbt_id`) REFERENCES `xbt` (`xbt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='A release-specific handle to an XBT record, pinning an XML schema built-in type or OAGIS built-in type to a RELEASE and carrying the revision chain (PREV_XBT_MANIFEST_ID and NEXT_XBT_MANIFEST_ID) across releases. It also records how the built-in type maps to an allowed CDT primitive via CDT_PRI.';