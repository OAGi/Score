-- ----------------------------------------------------
-- Migration script for Score v3.4.0                 --
--                                                   --
-- Author: Hakju Oh <hakju.oh@nist.gov>              --
-- ----------------------------------------------------

SET FOREIGN_KEY_CHECKS = 0;

-- Issue #1643 (https://github.com/OAGi/Score/issues/1643)
ALTER TABLE `asccp_manifest` MODIFY COLUMN `den` VARCHAR (202) DEFAULT NULL COMMENT 'The dictionary entry name of the ASCCP.';
ALTER TABLE `ascc_manifest` MODIFY COLUMN `den` VARCHAR (304) NOT NULL COMMENT 'DEN (dictionary entry name) of the ASCC. This column can be derived from Qualifier and OBJECT_CLASS_TERM of the FROM_ACC_ID and DEN of the TO_ASCCP_ID as Qualifier + "_ " + OBJECT_CLASS_TERM + ". " + DEN.';
ALTER TABLE `bccp_manifest` MODIFY COLUMN `den` VARCHAR (249) NOT NULL COMMENT 'The dictionary entry name of the BCCP. It is derived by PROPERTY_TERM + ". " + REPRESENTATION_TERM.';
ALTER TABLE `bcc_manifest` MODIFY COLUMN `den` VARCHAR (351) NOT NULL COMMENT 'DEN (dictionary entry name) of the BCC. This column can be derived from QUALIFIER and OBJECT_CLASS_TERM of the FROM_ACC_ID and DEN of the TO_BCCP_ID as QUALIFIER + "_ " + OBJECT_CLASS_TERM + ". " + DEN.';

-- `display_name` for BIEs
ALTER TABLE `asbiep`
    ADD COLUMN `display_name` varchar(100) DEFAULT NULL COMMENT 'The display name of the ASBIEP' AFTER `biz_term`;
ALTER TABLE `bbiep`
    ADD COLUMN `display_name` varchar(100) DEFAULT NULL COMMENT 'The display name of the BBIEP' AFTER `biz_term`;
ALTER TABLE `bbie_sc`
    ADD COLUMN `display_name` varchar(100) DEFAULT NULL COMMENT 'The display name of the BBIE_SC' AFTER `biz_term`;

-- Issue #1635 (https://github.com/OAGi/Score/issues/1635)
ALTER TABLE `top_level_asbiep`
    ADD COLUMN `based_top_level_asbiep_id` BIGINT(20) UNSIGNED DEFAULT NULL COMMENT 'Foreign key referencing the inherited base TOP_LEVEL_ASBIEP_ID.' AFTER `top_level_asbiep_id`;

-- Issue #1647 (https://github.com/OAGi/Score/issues/1647)
ALTER TABLE `agency_id_list_value`
    ADD COLUMN `is_developer_default` TINYINT(1) DEFAULT 0 COMMENT 'Indicates whether this agency ID list value can be used as the default for components referenced by developers.' AFTER `is_deprecated`,
    ADD COLUMN `is_user_default` TINYINT(1) DEFAULT 0 COMMENT 'Indicates whether this agency ID list value can be used as the default for components referenced by users.' AFTER `is_developer_default`;

UPDATE `agency_id_list_value` SET `is_developer_default` = 1 WHERE `name` = 'OAGi (Open Applications Group, Incorporated)';
UPDATE `agency_id_list_value` SET `is_user_default` = 1 WHERE `name` = 'Mutually defined';

-- Support for Multi-Library
DROP TABLE IF EXISTS `library`;
CREATE TABLE `library`
(
    `library_id`            bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
    `name`                  varchar(100) DEFAULT NULL COMMENT 'A library name.',
    `organization`          varchar(100) DEFAULT NULL COMMENT 'The name of the organization responsible for maintaining or managing the library.',
    `description`           text         DEFAULT NULL COMMENT 'A brief summary or overview of the library''s purpose and functionality.',
    `link`                  text         DEFAULT NULL COMMENT 'A URL directing to the library''s homepage, documentation, or repository for further details.',
    `domain`                varchar(100) DEFAULT NULL COMMENT 'Specifies the area of focus or application domain of the library (e.g., agriculture, finance, or aerospace).',
    `state`                 varchar(20)  DEFAULT NULL COMMENT 'Indicates the current status of the library.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record.',
    `creation_timestamp`    datetime(6)         NOT NULL COMMENT 'Timestamp when the record was created.',
    `last_update_timestamp` datetime(6)         NOT NULL COMMENT 'Timestamp when the record was last updated.',
    PRIMARY KEY (`library_id`),
    CONSTRAINT `library_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `library_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

INSERT INTO `library` (`library_id`, `name`, `organization`, `description`, `link`, `domain`, `created_by`, `last_updated_by`, `creation_timestamp`, `last_update_timestamp`)
VALUES
    (1, 'connectSpec', 'OAGi',
     'OAGi is a US-based non-profit standards organization founded in 1995 to address interoperability challenges among ERP systems. Its scope later expanded to include interoperability across all enterprise systems and business-to-business interactions.', 'https://oagi.org/', 'Enterprise Interoperability',
     (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'oagis'),
     (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'oagis'),
     '2019-10-02 15:27:09.521000',
     '2019-10-02 15:27:09.521000');

ALTER TABLE `release` ADD COLUMN `library_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointed to a library of the current record.' AFTER `release_id`;
UPDATE `release` SET `library_id` = 1;
ALTER TABLE `release` ADD CONSTRAINT `release_library_id_fk` FOREIGN KEY (`library_id`) REFERENCES `library` (`library_id`);

ALTER TABLE `namespace` ADD COLUMN `library_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointed to a library of the current record.' AFTER `namespace_id`;
UPDATE `namespace` SET `library_id` = 1;
ALTER TABLE `namespace` ADD CONSTRAINT `namespace_library_id_fk` FOREIGN KEY (`library_id`) REFERENCES `library` (`library_id`);
ALTER TABLE `namespace` DROP KEY `namespace_uk1`;
ALTER TABLE `namespace` ADD UNIQUE KEY `namespace_uk1` (`library_id`, `uri`);

ALTER TABLE `module_set` ADD COLUMN `library_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointed to a library of the current record.' AFTER `module_set_id`;
UPDATE `module_set` SET `library_id` = 1;
ALTER TABLE `module_set` ADD CONSTRAINT `module_set_library_id_fk` FOREIGN KEY (`library_id`) REFERENCES `library` (`library_id`);
