CREATE TABLE `bbie_bizterm`
(
    `bbie_bizterm_id`       bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of an bbie_bizterm record.',
    `bcc_bizterm_id`        bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the bbie_bizterm record.',
    `bbie_id`               bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the associated BBIE',
    `primary_indicator`     tinyint(1) NOT NULL DEFAULT 0 COMMENT 'The indicator shows if the business term is primary for the assigned BBIE.',
    `type_code`             char(30) DEFAULT NULL COMMENT 'The type code of the assignment.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the bbie_bizterm record. The creator of the asbie_bizterm is also its owner by default.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the bbie_bizterm record. This may be the user who is in the same group as the creator.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'Timestamp when the bbie_bizterm record was first created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the bbie_bizterm was last updated.',
    PRIMARY KEY (`bbie_bizterm_id`),
    KEY                     `bbie_bizterm_bcc_bizterm_fk` (`bcc_bizterm_id`),
    KEY                     `asbie_bizterm_asbie_fk` (`bbie_id`),
    CONSTRAINT `bbie_bizterm_bbie_fk` FOREIGN KEY (`bbie_id`) REFERENCES `bbie` (`bbie_id`),
    CONSTRAINT `bbie_bizterm_bcc_bizterm_fk` FOREIGN KEY (`bcc_bizterm_id`) REFERENCES `bcc_bizterm` (`bcc_bizterm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='The bbie_bizterm table stores information about the aggregation between the bbie_bizterm and BBIE. TODO: Placeholder, definition is missing.';