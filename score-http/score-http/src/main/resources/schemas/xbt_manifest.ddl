CREATE TABLE `xbt_manifest`
(
    `xbt_manifest_id`      bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `release_id`           bigint(20) unsigned NOT NULL,
    `xbt_id`               bigint(20) unsigned NOT NULL,
    `conflict`             tinyint(1) NOT NULL DEFAULT 0 COMMENT 'This indicates that there is a conflict between self and relationship.',
    `log_id`               bigint(20) unsigned DEFAULT NULL COMMENT 'A foreign key pointed to a log for the current record.',
    `prev_xbt_manifest_id` bigint(20) unsigned DEFAULT NULL,
    `next_xbt_manifest_id` bigint(20) unsigned DEFAULT NULL,
    PRIMARY KEY (`xbt_manifest_id`),
    KEY                    `xbt_manifest_xbt_id_fk` (`xbt_id`),
    KEY                    `xbt_manifest_release_id_fk` (`release_id`),
    KEY                    `xbt_manifest_log_id_fk` (`log_id`),
    KEY                    `xbt_manifest_prev_xbt_manifest_id_fk` (`prev_xbt_manifest_id`),
    KEY                    `xbt_manifest_next_xbt_manifest_id_fk` (`next_xbt_manifest_id`),
    CONSTRAINT `xbt_manifest_log_id_fk` FOREIGN KEY (`log_id`) REFERENCES `log` (`log_id`),
    CONSTRAINT `xbt_manifest_next_xbt_manifest_id_fk` FOREIGN KEY (`next_xbt_manifest_id`) REFERENCES `xbt_manifest` (`xbt_manifest_id`),
    CONSTRAINT `xbt_manifest_prev_xbt_manifest_id_fk` FOREIGN KEY (`prev_xbt_manifest_id`) REFERENCES `xbt_manifest` (`xbt_manifest_id`),
    CONSTRAINT `xbt_manifest_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
    CONSTRAINT `xbt_manifest_xbt_id_fk` FOREIGN KEY (`xbt_id`) REFERENCES `xbt` (`xbt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;