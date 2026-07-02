CREATE TABLE `oas_example`
(
    `oas_example_id`        bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `summary`               text DEFAULT NULL COMMENT 'Short description for the example.',
    `description`           text DEFAULT NULL COMMENT 'Long description for the example. CommonMark syntax MAY be used for rich text representation.',
    `ref`                   varchar(250) DEFAULT NULL COMMENT 'A URL that points to the literal example. This provides the capability to reference examples that cannot easily be included in JSON or YAML documents. The value field and externalValue field are mutually exclusive.',
    `value`                 text DEFAULT NULL COMMENT 'Embedded literal example. The value field and externalValue field are mutually exclusive. To represent examples of media types that cannot naturally represented in JSON or YAML, use a string value to contain the example, escaping where necessary.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_example_id`),
    KEY                     `oas_example_created_by_fk` (`created_by`),
    KEY                     `oas_example_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_example_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_example_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='OpenAPI Example Object holding a single example, either an embedded literal in the value field or a reference to an external example via the ref (externalValue) field; the two are mutually exclusive.';