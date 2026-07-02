CREATE TABLE `seq_key`
(
    `seq_key_id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a SEQ_KEY record.',
    `from_acc_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the ACC_MANIFEST table pointing to the ACC that owns the association (ASCC/BCC) ordered by this sequence key.',
    `ascc_manifest_id`     bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the ASCC_MANIFEST table for the ASCC association ordered by this record. Exactly one of ASCC_MANIFEST_ID / BCC_MANIFEST_ID is non-null.',
    `bcc_manifest_id`      bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the BCC_MANIFEST table for the BCC association ordered by this record. Exactly one of ASCC_MANIFEST_ID / BCC_MANIFEST_ID is non-null.',
    `prev_seq_key_id`      bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the previous SEQ_KEY record in the ordering chain; NULL for the first association.',
    `next_seq_key_id`      bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the next SEQ_KEY record in the ordering chain; NULL for the last association.',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='This table stores the ordering of the associations (ASCC and BCC) that belong to an ACC. Each record points, through FROM_ACC_MANIFEST_ID, to the owning ACC via ACC_MANIFEST and, through exactly one of ASCC_MANIFEST_ID or BCC_MANIFEST_ID, to the ordered association; the PREV_SEQ_KEY_ID and NEXT_SEQ_KEY_ID columns form a doubly-linked chain that defines the sequence of these associations within the ACC.';