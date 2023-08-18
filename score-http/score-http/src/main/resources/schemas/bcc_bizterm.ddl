CREATE TABLE `bcc_bizterm`
(
    `bcc_bizterm_id`        bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of an bcc_bizterm record.',
    `business_term_id`      bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the associated business term',
    `bcc_id`                bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the associated BCC',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the bcc_bizterm record. The creator of the bcc_bizterm is also its owner by default.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the bcc_bizterm record. This may be the user who is in the same group as the creator.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'Timestamp when the bcc_bizterm record was first created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the bcc_bizterm was last updated.',
    PRIMARY KEY (`bcc_bizterm_id`),
    KEY                     `bcc_bizterm_bcc_fk` (`bcc_id`),
    KEY                     `bcc_bizterm_business_term_fk` (`business_term_id`),
    CONSTRAINT `bcc_bizterm_bcc_fk` FOREIGN KEY (`bcc_id`) REFERENCES `bcc` (`bcc_id`),
    CONSTRAINT `bcc_bizterm_business_term_fk` FOREIGN KEY (`business_term_id`) REFERENCES `business_term` (`business_term_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='The bcc_bizterm table stores information about the aggregation between the business term and BCC. TODO: Placeholder, definition is missing.';