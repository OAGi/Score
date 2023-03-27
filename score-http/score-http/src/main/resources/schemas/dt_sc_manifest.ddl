CREATE TABLE `dt_sc_manifest`
(
    `dt_sc_manifest_id`             bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `release_id`                    bigint(20) unsigned NOT NULL,
    `dt_sc_id`                      bigint(20) unsigned NOT NULL,
    `owner_dt_manifest_id`          bigint(20) unsigned NOT NULL,
    `based_dt_sc_manifest_id`       bigint(20) unsigned          DEFAULT NULL,
    `conflict`                      tinyint(1)          NOT NULL DEFAULT '0' COMMENT 'This indicates that there is a conflict between self and relationship.',
    `replacement_dt_sc_manifest_id` bigint(20) unsigned          DEFAULT NULL COMMENT 'This refers to a replacement manifest if the record is deprecated.',
    `prev_dt_sc_manifest_id`        bigint(20) unsigned          DEFAULT NULL,
    `next_dt_sc_manifest_id`        bigint(20) unsigned          DEFAULT NULL,
    PRIMARY KEY (`dt_sc_manifest_id`),
    KEY `dt_sc_manifest_dt_sc_id_fk` (`dt_sc_id`),
    KEY `dt_sc_manifest_release_id_fk` (`release_id`),
    KEY `dt_sc_manifest_owner_dt_manifest_id_fk` (`owner_dt_manifest_id`),
    KEY `dt_sc_prev_dt_sc_manifest_id_fk` (`prev_dt_sc_manifest_id`),
    KEY `dt_sc_next_dt_sc_manifest_id_fk` (`next_dt_sc_manifest_id`),
    KEY `dt_sc_replacement_dt_sc_manifest_id_fk` (`replacement_dt_sc_manifest_id`),
    KEY `based_dt_sc_manifest_id_fk` (`based_dt_sc_manifest_id`),
    CONSTRAINT `based_dt_sc_manifest_id_fk` FOREIGN KEY (`based_dt_sc_manifest_id`) REFERENCES `dt_sc_manifest` (`dt_sc_manifest_id`),
    CONSTRAINT `dt_sc_manifest_dt_sc_id_fk` FOREIGN KEY (`dt_sc_id`) REFERENCES `dt_sc` (`dt_sc_id`),
    CONSTRAINT `dt_sc_manifest_owner_dt_manifest_id_fk` FOREIGN KEY (`owner_dt_manifest_id`) REFERENCES `dt_manifest` (`dt_manifest_id`),
    CONSTRAINT `dt_sc_manifest_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
    CONSTRAINT `dt_sc_next_dt_sc_manifest_id_fk` FOREIGN KEY (`next_dt_sc_manifest_id`) REFERENCES `dt_sc_manifest` (`dt_sc_manifest_id`),
    CONSTRAINT `dt_sc_prev_dt_sc_manifest_id_fk` FOREIGN KEY (`prev_dt_sc_manifest_id`) REFERENCES `dt_sc_manifest` (`dt_sc_manifest_id`),
    CONSTRAINT `dt_sc_replacement_dt_sc_manifest_id_fk` FOREIGN KEY (`replacement_dt_sc_manifest_id`) REFERENCES `dt_sc_manifest` (`dt_sc_manifest_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;