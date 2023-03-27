CREATE TABLE `agency_id_list_value_manifest`
(
    `agency_id_list_value_manifest_id`             bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `release_id`                                   bigint(20) unsigned NOT NULL,
    `agency_id_list_value_id`                      bigint(20) unsigned NOT NULL,
    `agency_id_list_manifest_id`                   bigint(20) unsigned NOT NULL,
    `based_agency_id_list_value_manifest_id`       bigint(20) unsigned          DEFAULT NULL,
    `conflict`                                     tinyint(1)          NOT NULL DEFAULT '0' COMMENT 'This indicates that there is a conflict between self and relationship.',
    `replacement_agency_id_list_value_manifest_id` bigint(20) unsigned          DEFAULT NULL COMMENT 'This refers to a replacement manifest if the record is deprecated.',
    `prev_agency_id_list_value_manifest_id`        bigint(20) unsigned          DEFAULT NULL,
    `next_agency_id_list_value_manifest_id`        bigint(20) unsigned          DEFAULT NULL,
    PRIMARY KEY (`agency_id_list_value_manifest_id`),
    KEY `agency_id_list_value_manifest_agency_id_list_value_id_fk` (`agency_id_list_value_id`),
    KEY `agency_id_list_value_manifest_release_id_fk` (`release_id`),
    KEY `agency_id_list_value_manifest_agency_id_list_manifest_id_fk` (`agency_id_list_manifest_id`),
    KEY `agency_id_list_value_manifest_prev_agency_id_list_value_manif_fk` (`prev_agency_id_list_value_manifest_id`),
    KEY `agency_id_list_value_manifest_next_agency_id_list_value_manif_fk` (`next_agency_id_list_value_manifest_id`),
    KEY `agency_id_list_value_replacement_agency_id_list_manif_fk` (`replacement_agency_id_list_value_manifest_id`),
    KEY `agency_id_list_value_manifest_based_agency_id_list_val_mnf_id_fk` (`based_agency_id_list_value_manifest_id`),
    CONSTRAINT `agency_id_list_value_manifest_agency_id_list_manifest_id_fk` FOREIGN KEY (`agency_id_list_manifest_id`) REFERENCES `agency_id_list_manifest` (`agency_id_list_manifest_id`),
    CONSTRAINT `agency_id_list_value_manifest_agency_id_list_value_id_fk` FOREIGN KEY (`agency_id_list_value_id`) REFERENCES `agency_id_list_value` (`agency_id_list_value_id`),
    CONSTRAINT `agency_id_list_value_manifest_based_agency_id_list_val_mnf_id_fk` FOREIGN KEY (`based_agency_id_list_value_manifest_id`) REFERENCES `agency_id_list_value_manifest` (`agency_id_list_value_manifest_id`),
    CONSTRAINT `agency_id_list_value_manifest_next_agency_id_list_value_manif_fk` FOREIGN KEY (`next_agency_id_list_value_manifest_id`) REFERENCES `agency_id_list_value_manifest` (`agency_id_list_value_manifest_id`),
    CONSTRAINT `agency_id_list_value_manifest_prev_agency_id_list_value_manif_fk` FOREIGN KEY (`prev_agency_id_list_value_manifest_id`) REFERENCES `agency_id_list_value_manifest` (`agency_id_list_value_manifest_id`),
    CONSTRAINT `agency_id_list_value_manifest_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
    CONSTRAINT `agency_id_list_value_replacement_agency_id_list_manif_fk` FOREIGN KEY (`replacement_agency_id_list_value_manifest_id`) REFERENCES `agency_id_list_value_manifest` (`agency_id_list_value_manifest_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;