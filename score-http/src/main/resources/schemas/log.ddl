CREATE TABLE `log`
(
    `log_id`                bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a LOG record.',
    `hash`                  char(40) CHARACTER SET ascii COLLATE ascii_general_ci     NOT NULL COMMENT 'The unique hash to identify the log.',
    `revision_num`          int(10) unsigned NOT NULL DEFAULT 1 COMMENT 'This is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 1, 2, and so on.',
    `revision_tracking_num` int(10) unsigned NOT NULL DEFAULT 1 COMMENT 'This supports the ability to undo changes during a revision (life cycle of a revision is from the component''s WIP state to PUBLISHED state). REVISION_TRACKING_NUM can be 1, 2, and so on.',
    `log_action`            varchar(20)                                                        DEFAULT NULL COMMENT 'This indicates the action associated with the record.',
    `reference`             varchar(100) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL DEFAULT '' COMMENT 'A reference to the record for which this log is created. Because the LOG table stores logs for various component types, it cannot use a foreign key; instead it stores the GUID of the referenced component.',
    `snapshot`              longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'A JSON serialization capturing the snapshot of the component''s state at the time this log record is created.',
    `prev_log_id`           bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the previous LOG record in the chain.',
    `next_log_id`           bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the next LOG record in the chain.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    PRIMARY KEY (`log_id`),
    KEY                     `reference` (`reference`),
    KEY                     `log_created_by_fk` (`created_by`),
    KEY                     `log_prev_log_id_fk` (`prev_log_id`),
    KEY                     `log_next_log_id_fk` (`next_log_id`),
    CONSTRAINT `log_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `log_next_log_id_fk` FOREIGN KEY (`next_log_id`) REFERENCES `log` (`log_id`),
    CONSTRAINT `log_prev_log_id_fk` FOREIGN KEY (`prev_log_id`) REFERENCES `log` (`log_id`),
    CONSTRAINT `log_chk_1` CHECK (json_valid(`snapshot`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='The LOG table records the revision history of component records, storing one entry per change with the component''s serialized state SNAPSHOT, its REVISION_NUM and REVISION_TRACKING_NUM, and the LOG_ACTION taken. Because it logs many different component types, it references the target component by its GUID (the REFERENCE column) rather than a foreign key, and the PREV_LOG_ID and NEXT_LOG_ID columns chain the entries into a per-component revision history.';