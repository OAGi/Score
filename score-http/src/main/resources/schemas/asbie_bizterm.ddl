CREATE TABLE `asbie_bizterm`
(
    `asbie_bizterm_id`      bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of an asbie_bizterm record.',
    `ascc_bizterm_id`       bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the ascc_business_term record.',
    `asbie_id`              bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the associated ASBIE',
    `primary_indicator`     tinyint(1) NOT NULL DEFAULT 0 COMMENT 'The indicator shows if the business term is primary for the assigned ASBIE.',
    `type_code`             char(30) DEFAULT NULL COMMENT 'The type code of the assignment.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the asbie_bizterm record. The creator of the asbie_bizterm is also its owner by default.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the asbie_bizterm record. This may be the user who is in the same group as the creator.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'Timestamp when the asbie_bizterm record was first created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the asbie_bizterm was last updated.',
    PRIMARY KEY (`asbie_bizterm_id`),
    KEY                     `asbie_bizterm_ascc_bizterm_fk` (`ascc_bizterm_id`),
    KEY                     `asbie_bizterm_asbie_fk` (`asbie_id`),
    CONSTRAINT `asbie_bizterm_asbie_fk` FOREIGN KEY (`asbie_id`) REFERENCES `asbie` (`asbie_id`),
    CONSTRAINT `asbie_bizterm_ascc_bizterm_fk` FOREIGN KEY (`ascc_bizterm_id`) REFERENCES `ascc_bizterm` (`ascc_bizterm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='The asbie_bizterm table stores information about the aggregation between the ascc_bizterm and ASBIE. TODO: Placeholder, definition is missing.';