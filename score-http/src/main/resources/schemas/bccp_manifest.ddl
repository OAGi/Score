CREATE TABLE `bccp_manifest`
(
    `bccp_manifest_id`             bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `release_id`                   bigint(20) unsigned NOT NULL,
    `bccp_id`                      bigint(20) unsigned NOT NULL,
    `bdt_manifest_id`              bigint(20) unsigned NOT NULL,
    `den`                          varchar(249) NOT NULL COMMENT 'The dictionary entry name of the BCCP. It is derived by PROPERTY_TERM + ". " + REPRESENTATION_TERM.',
    `conflict`                     tinyint(1) NOT NULL DEFAULT 0 COMMENT 'This indicates that there is a conflict between self and relationship.',
    `log_id`                       bigint(20) unsigned DEFAULT NULL COMMENT 'A foreign key pointed to a log for the current record.',
    `replacement_bccp_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement manifest if the record is deprecated.',
    `prev_bccp_manifest_id`        bigint(20) unsigned DEFAULT NULL,
    `next_bccp_manifest_id`        bigint(20) unsigned DEFAULT NULL,
    PRIMARY KEY (`bccp_manifest_id`),
    KEY                            `bccp_manifest_bccp_id_fk` (`bccp_id`),
    KEY                            `bccp_manifest_bdt_manifest_id_fk` (`bdt_manifest_id`),
    KEY                            `bccp_manifest_release_id_fk` (`release_id`),
    KEY                            `bccp_manifest_log_id_fk` (`log_id`),
    KEY                            `bccp_manifest_prev_bccp_manifest_id_fk` (`prev_bccp_manifest_id`),
    KEY                            `bccp_manifest_next_bccp_manifest_id_fk` (`next_bccp_manifest_id`),
    KEY                            `bccp_replacement_bccp_manifest_id_fk` (`replacement_bccp_manifest_id`),
    CONSTRAINT `bccp_manifest_bccp_id_fk` FOREIGN KEY (`bccp_id`) REFERENCES `bccp` (`bccp_id`),
    CONSTRAINT `bccp_manifest_bdt_manifest_id_fk` FOREIGN KEY (`bdt_manifest_id`) REFERENCES `dt_manifest` (`dt_manifest_id`),
    CONSTRAINT `bccp_manifest_log_id_fk` FOREIGN KEY (`log_id`) REFERENCES `log` (`log_id`),
    CONSTRAINT `bccp_manifest_next_bccp_manifest_id_fk` FOREIGN KEY (`next_bccp_manifest_id`) REFERENCES `bccp_manifest` (`bccp_manifest_id`),
    CONSTRAINT `bccp_manifest_prev_bccp_manifest_id_fk` FOREIGN KEY (`prev_bccp_manifest_id`) REFERENCES `bccp_manifest` (`bccp_manifest_id`),
    CONSTRAINT `bccp_manifest_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
    CONSTRAINT `bccp_replacement_bccp_manifest_id_fk` FOREIGN KEY (`replacement_bccp_manifest_id`) REFERENCES `bccp_manifest` (`bccp_manifest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;