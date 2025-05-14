CREATE TABLE `library`
(
    `library_id`            bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
    `name`                  varchar(100) DEFAULT NULL COMMENT 'A library name.',
    `type`                  varchar(100) DEFAULT NULL COMMENT 'A type of the library.',
    `organization`          varchar(100) DEFAULT NULL COMMENT 'The name of the organization responsible for maintaining or managing the library.',
    `description`           text         DEFAULT NULL COMMENT 'A brief summary or overview of the library''s purpose and functionality.',
    `link`                  text         DEFAULT NULL COMMENT 'A URL directing to the library''s homepage, documentation, or repository for further details.',
    `domain`                varchar(100) DEFAULT NULL COMMENT 'Specifies the area of focus or application domain of the library (e.g., agriculture, finance, or aerospace).',
    `state`                 varchar(20)  DEFAULT NULL COMMENT 'Current state of the library.',
    `is_read_only`          tinyint(1) DEFAULT 0 COMMENT 'Indicates if the library is read-only (0 = False, 1 = True).',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'Timestamp when the record was created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the record was last updated.',
    PRIMARY KEY (`library_id`),
    KEY                     `library_created_by_fk` (`created_by`),
    KEY                     `library_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `library_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `library_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;