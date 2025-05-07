CREATE TABLE `seq_key`
(
    `seq_key_id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `from_acc_manifest_id` bigint(20) unsigned NOT NULL,
    `ascc_manifest_id`     bigint(20) unsigned DEFAULT NULL,
    `bcc_manifest_id`      bigint(20) unsigned DEFAULT NULL,
    `prev_seq_key_id`      bigint(20) unsigned DEFAULT NULL,
    `next_seq_key_id`      bigint(20) unsigned DEFAULT NULL,
    PRIMARY KEY (`seq_key_id`),
    KEY                    `seq_key_from_acc_manifest_id` (`from_acc_manifest_id`),
    KEY                    `seq_key_ascc_manifest_id` (`ascc_manifest_id`),
    KEY                    `seq_key_bcc_manifest_id` (`bcc_manifest_id`),
    KEY                    `seq_key_prev_seq_key_id_fk` (`prev_seq_key_id`),
    KEY                    `seq_key_next_seq_key_id_fk` (`next_seq_key_id`),
    CONSTRAINT `seq_key_ascc_manifest_id_fk` FOREIGN KEY (`ascc_manifest_id`) REFERENCES `ascc_manifest` (`ascc_manifest_id`),
    CONSTRAINT `seq_key_bcc_manifest_id_fk` FOREIGN KEY (`bcc_manifest_id`) REFERENCES `bcc_manifest` (`bcc_manifest_id`),
    CONSTRAINT `seq_key_from_acc_manifest_id_fk` FOREIGN KEY (`from_acc_manifest_id`) REFERENCES `acc_manifest` (`acc_manifest_id`),
    CONSTRAINT `seq_key_next_seq_key_id_fk` FOREIGN KEY (`next_seq_key_id`) REFERENCES `seq_key` (`seq_key_id`),
    CONSTRAINT `seq_key_prev_seq_key_id_fk` FOREIGN KEY (`prev_seq_key_id`) REFERENCES `seq_key` (`seq_key_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;