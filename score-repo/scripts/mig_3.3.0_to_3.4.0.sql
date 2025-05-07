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

-- Increase the size of `code_list_value`.`meaning`
ALTER TABLE `code_list_value` MODIFY `meaning` TINYTEXT DEFAULT NULL COMMENT 'The description or explanation of the code list value, e.g., ''Each'' for EA, ''English'' for EN.';

-- Add `prev_release_id`, `next_release_id` into `release` table.
ALTER TABLE `release`
    ADD COLUMN `prev_release_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key referencing the previous release record.',
    ADD COLUMN `next_release_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key referencing the next release record.';

ALTER TABLE `release`
    ADD CONSTRAINT `release_prev_release_id_fk` FOREIGN KEY (`prev_release_id`) REFERENCES `release` (`release_id`),
    ADD CONSTRAINT `release_next_release_id_fk` FOREIGN KEY (`next_release_id`) REFERENCES `release` (`release_id`);

-- Fix `blob_content_manifest`
UPDATE `blob_content_manifest` SET `prev_blob_content_manifest_id` = NULL, `next_blob_content_manifest_id` = NULL;
DELETE FROM `module_blob_content_manifest` WHERE `module_set_release_id` > 1;
DELETE FROM `blob_content_manifest` WHERE `release_id` > 1;

-- Fix 'Common Time Reporting'
-- ‘Common Time Reporting’ was of the 'Group' type in 10.6, but it was populated in the database as the 'Semantics' type.
-- We will leave it as is; however, since the ASCCP and ACC share the same GUID, we will update the GUID of the ASCCP.
UPDATE `asccp` SET `guid` = '1d721f70dfc742e8b334063557cfa2eb' WHERE property_term = 'Common Time Reporting';

-- -------------------------
-- Support for Multi-Library
-- -------------------------
DROP TABLE IF EXISTS `library`;
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
    `is_read_only`          tinyint(1)   DEFAULT 0    COMMENT 'Indicates if the library is read-only (0 = False, 1 = True).',
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

INSERT INTO `library` (`library_id`, `name`, `type`, `organization`, `description`, `link`, `domain`, `state`, `is_read_only`,
                       `created_by`, `last_updated_by`, `creation_timestamp`, `last_update_timestamp`)
VALUES (1, 'CCTS Data Type Catalogue v3', 'Standard', 'UN/CEFACT',
        'The Core Components Data Type Catalogue defines standardized data types and formats to support electronic business transactions and trade facilitation.',
        'https://unece.org/trade/uncefact/core-components-data-type-catalogue', 'International Trade', NULL, 1,
        (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'sysadm'),
        (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'sysadm'),
        '2009-09-29 00:00:00.000000', '2009-09-29 00:00:00.000000'),
       (2, 'ISO 15000-5', 'Standard', 'International Organization for Standardization (ISO)',
        'ISO 15000-5:2014 describes and specifies the Core Component solution as a methodology for developing a common set of semantic building blocks that represent general types of business data, and provides for the creation of new business vocabularies and restructuring of existing business vocabularies.',
        'https://www.iso.org/standard/61433.html', 'International Trade', NULL, 1,
        (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'sysadm'),
        (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'sysadm'), '2014-04-15 00:00:00.000000',
        '2014-04-15 00:00:00.000000'),
       (3, 'connectSpec', 'Standard', 'OAGi',
        'connectSpec provides standards and guidelines to enhance interoperability across enterprise systems and business-to-business integrations.',
        'https://oagi.org/', 'Enterprise Interoperability', NULL, 0,
        (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'oagis'),
        (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'oagis'),
        '2019-10-02 15:27:09.521000',
        '2019-10-02 15:27:09.521000');

ALTER TABLE `release` ADD COLUMN `library_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointed to a library of the current record.' AFTER `release_id`;
UPDATE `release` SET `library_id` = 3;
ALTER TABLE `release` ADD CONSTRAINT `release_library_id_fk` FOREIGN KEY (`library_id`) REFERENCES `library` (`library_id`);

INSERT INTO `release` (`library_id`, `guid`, `release_num`, `namespace_id`,
                       `created_by`, `last_updated_by`, `creation_timestamp`, `last_update_timestamp`,
                       `state`)
VALUES (1, 'fae43c9bab384f95a36eb2dde4ce130b', 'Working', NULL,
        (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'sysadm'),
        (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'sysadm'),
        '2009-09-29 00:00:00.000000', '2009-09-29 00:00:00.000000',
        'Published'),
       (1, '49eb92922b284a1fbfd7dc770d73d38a', '3.0', NULL,
        (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'sysadm'),
        (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'sysadm'),
        '2009-09-29 00:00:00.000000', '2009-09-29 00:00:00.000000',
        'Published'),
       (1, 'f8b050e05b3c4043bbfd7d6bc824f3ce', '3.1', NULL,
        (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'sysadm'),
        (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'sysadm'),
        '2011-10-17 00:00:00.000000', '2011-10-17 00:00:00.000000',
        'Published'),
       (2, '6bd71e6c1e524dceb74f3897f675bd96', 'Working', NULL,
        (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'sysadm'),
        (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'sysadm'),
        '2014-04-15 00:00:00.000000', '2014-04-15 00:00:00.000000',
        'Published'),
       (2, '4dc2a995a1764100a23b046936a2027c', '2014', NULL,
        (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'sysadm'),
        (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'sysadm'),
        '2014-04-15 00:00:00.000000', '2014-04-15 00:00:00.000000',
        'Published');

-- Update `prev_release_id`, `next_release_id`

UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0') WHERE `library_id` = 1 AND `release_num` = '3.1';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1') WHERE `library_id` = 1 AND `release_num` = 'Working';

UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1') WHERE `library_id` = 1 AND `release_num` = '3.0';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working') WHERE `library_id` = 1 AND `release_num` = '3.1';

UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014') WHERE `library_id` = 2 AND `release_num` = 'Working';

UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working') WHERE `library_id` = 2 AND `release_num` = '2014';

UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.6') WHERE `library_id` = 3 AND `release_num` = '10.7.0.1';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.7.0.1') WHERE `library_id` = 3 AND `release_num` = '10.7.1';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.7.1') WHERE `library_id` = 3 AND `release_num` = '10.7.2';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.7.2') WHERE `library_id` = 3 AND `release_num` = '10.7.3';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.7.3') WHERE `library_id` = 3 AND `release_num` = '10.7.4';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.7.4') WHERE `library_id` = 3 AND `release_num` = '10.7.5';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.7.5') WHERE `library_id` = 3 AND `release_num` = '10.8';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.8') WHERE `library_id` = 3 AND `release_num` = '10.8.1';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.8.1') WHERE `library_id` = 3 AND `release_num` = '10.8.2';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.8.2') WHERE `library_id` = 3 AND `release_num` = '10.8.3';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.8.3') WHERE `library_id` = 3 AND `release_num` = '10.8.4';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.8.4') WHERE `library_id` = 3 AND `release_num` = '10.8.5';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.8.5') WHERE `library_id` = 3 AND `release_num` = '10.8.6';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.8.6') WHERE `library_id` = 3 AND `release_num` = '10.8.7.1';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.8.7.1') WHERE `library_id` = 3 AND `release_num` = '10.8.8';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.8.8') WHERE `library_id` = 3 AND `release_num` = '10.9';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.9') WHERE `library_id` = 3 AND `release_num` = '10.9.1';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.9.1') WHERE `library_id` = 3 AND `release_num` = '10.9.2';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.9.2') WHERE `library_id` = 3 AND `release_num` = '10.9.3';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.9.3') WHERE `library_id` = 3 AND `release_num` = '10.10';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.10') WHERE `library_id` = 3 AND `release_num` = '10.10.1';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.10.1') WHERE `library_id` = 3 AND `release_num` = '10.10.2';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.10.2') WHERE `library_id` = 3 AND `release_num` = '10.10.3';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.10.3') WHERE `library_id` = 3 AND `release_num` = '10.10.4';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.10.4') WHERE `library_id` = 3 AND `release_num` = '10.11';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.11') WHERE `library_id` = 3 AND `release_num` = '10.11.1';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.11.1') WHERE `library_id` = 3 AND `release_num` = '10.11.2';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.11.2') WHERE `library_id` = 3 AND `release_num` = '10.11.3';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.11.3') WHERE `library_id` = 3 AND `release_num` = '10.11.4';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.11.4') WHERE `library_id` = 3 AND `release_num` = '10.12';
UPDATE `release` SET `prev_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.12') WHERE `library_id` = 3 AND `release_num` = 'Working';

UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.7.0.1') WHERE `library_id` = 3 AND `release_num` = '10.6';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.7.1') WHERE `library_id` = 3 AND `release_num` = '10.7.0.1';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.7.2') WHERE `library_id` = 3 AND `release_num` = '10.7.1';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.7.3') WHERE `library_id` = 3 AND `release_num` = '10.7.2';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.7.4') WHERE `library_id` = 3 AND `release_num` = '10.7.3';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.7.5') WHERE `library_id` = 3 AND `release_num` = '10.7.4';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.8') WHERE `library_id` = 3 AND `release_num` = '10.7.5';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.8.1') WHERE `library_id` = 3 AND `release_num` = '10.8';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.8.2') WHERE `library_id` = 3 AND `release_num` = '10.8.1';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.8.3') WHERE `library_id` = 3 AND `release_num` = '10.8.2';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.8.4') WHERE `library_id` = 3 AND `release_num` = '10.8.3';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.8.5') WHERE `library_id` = 3 AND `release_num` = '10.8.4';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.8.6') WHERE `library_id` = 3 AND `release_num` = '10.8.5';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.8.7.1') WHERE `library_id` = 3 AND `release_num` = '10.8.6';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.8.8') WHERE `library_id` = 3 AND `release_num` = '10.8.7.1';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.9') WHERE `library_id` = 3 AND `release_num` = '10.8.8';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.9.1') WHERE `library_id` = 3 AND `release_num` = '10.9';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.9.2') WHERE `library_id` = 3 AND `release_num` = '10.9.1';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.9.3') WHERE `library_id` = 3 AND `release_num` = '10.9.2';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.10') WHERE `library_id` = 3 AND `release_num` = '10.9.3';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.10.1') WHERE `library_id` = 3 AND `release_num` = '10.10';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.10.2') WHERE `library_id` = 3 AND `release_num` = '10.10.1';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.10.3') WHERE `library_id` = 3 AND `release_num` = '10.10.2';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.10.4') WHERE `library_id` = 3 AND `release_num` = '10.10.3';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.11') WHERE `library_id` = 3 AND `release_num` = '10.10.4';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.11.1') WHERE `library_id` = 3 AND `release_num` = '10.11';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.11.2') WHERE `library_id` = 3 AND `release_num` = '10.11.1';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.11.3') WHERE `library_id` = 3 AND `release_num` = '10.11.2';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.11.4') WHERE `library_id` = 3 AND `release_num` = '10.11.3';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = '10.12') WHERE `library_id` = 3 AND `release_num` = '10.11.4';
UPDATE `release` SET `next_release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 3 AND `release_num` = 'Working') WHERE `library_id` = 3 AND `release_num` = '10.12';


ALTER TABLE `namespace` ADD COLUMN `library_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointed to a library of the current record.' AFTER `namespace_id`;
UPDATE `namespace` SET `library_id` = 3;
ALTER TABLE `namespace` ADD CONSTRAINT `namespace_library_id_fk` FOREIGN KEY (`library_id`) REFERENCES `library` (`library_id`);
ALTER TABLE `namespace` DROP KEY `namespace_uk1`;
ALTER TABLE `namespace` ADD UNIQUE KEY `namespace_uk1` (`library_id`, `uri`);

ALTER TABLE `module_set` ADD COLUMN `library_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointed to a library of the current record.' AFTER `module_set_id`;
UPDATE `module_set` SET `library_id` = 3;
ALTER TABLE `module_set` ADD CONSTRAINT `module_set_library_id_fk` FOREIGN KEY (`library_id`) REFERENCES `library` (`library_id`);

ALTER TABLE `bie_package` ADD COLUMN `library_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointed to a library of the current record.' AFTER `bie_package_id`;
UPDATE `bie_package` SET `library_id` = 3;
ALTER TABLE `bie_package` ADD CONSTRAINT `bie_package_library_id_fk` FOREIGN KEY (`library_id`) REFERENCES `library` (`library_id`);

-- ---------------------------
-- Add DT records for CDT v3.0
-- ---------------------------
ALTER TABLE `dt_manifest` AUTO_INCREMENT = 1;
ALTER TABLE `dt_sc_manifest` AUTO_INCREMENT = 1;

INSERT INTO `dt` (`dt_id`, `guid`, `data_type_term`, `qualifier`, `representation_term`, `six_digit_id`, `based_dt_id`, `definition`, `definition_source`, `namespace_id`, `content_component_definition`, `state`, `commonly_used`, `created_by`, `last_updated_by`, `owner_user_id`, `creation_timestamp`, `last_update_timestamp`, `is_deprecated`, `replacement_dt_id`, `prev_dt_id`, `next_dt_id`)
VALUES
    (602, 'b3bb32f239074f7b92dc588bc42d8867', 'Amount', NULL, 'Amount', NULL, NULL, 'CDT V3.0. An amount is a number of monetary units specified in a currency.', NULL, 1, 'A number of monetary units.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.159000', '2019-10-02 15:28:31.159000', 0, NULL, NULL, 1),
    (603, 'f5cc93bce35c474494e493d27438c1f9', 'Binary Object', NULL, 'Binary Object', NULL, NULL, 'CDT V3.0. A binary object is a sequence of binary digits (bits).', NULL, 1, 'A finite sequence of binary digits (bits).', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.167000', '2019-10-02 15:28:31.167000', 0, NULL, NULL, 2),
    (604, '333a69be7aab453f90ae221e6f9b1546', 'Code', NULL, 'Code', NULL, NULL, 'CDT V3.0. A code is a character string of letters, numbers, special characters (except escape sequences); and symbols. It represents a definitive value,\na method, or a property description in an abbreviated or language-independent form that is part of a finite list of allowed values.', NULL, 1, 'A character string (letters, figures or symbols) that for brevity and/or language independence may be used to represent or replace a definitive value or text of an attribute.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.173000', '2019-10-02 15:28:31.173000', 0, NULL, NULL, 3),
    (605, '5b80ef6938dc4a62a436810803e03af3', 'Date', NULL, 'Date', NULL, NULL, 'CDT V3.0. A date is a Gregorian calendar representation in various common resolutions: year, month, week, day.', NULL, 1, 'The particular point in the progression of date.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.180000', '2019-10-02 15:28:31.180000', 0, NULL, NULL, 4),
    (606, '0c80153fdaca4bca844e79d356c4fc6b', 'Date Time', NULL, 'Date Time', NULL, NULL, 'CDT V3.0. A date time identifies a date and time of day to various common resolutions: year, month, week, day, hour, minute, second, and fraction of\nsecond.', NULL, 1, 'The particular date and time point in the progression of time.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.185000', '2019-10-02 15:28:31.185000', 0, NULL, NULL, 5),
    (607, '9db16a2c1ebe42c6b537d1d2eec19390', 'Duration', NULL, 'Duration', NULL, NULL, 'CDT V3.0. A duration is the specification of a length of time without a fixed start or end time, expressed in Gregorian calendar time units (Year, Month,\nWeek, Day) and Hours, Minutes or Seconds.', NULL, 1, 'The particular representation of duration.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.191000', '2019-10-02 15:28:31.191000', 0, NULL, NULL, 6),
    (608, '41d5e4e513fa495bb292ff4a6ed2689a', 'Graphic', NULL, 'Graphic', NULL, NULL, 'CDT V3.0. A graphic is a diagram, a graph, mathematical curves, or similar vector based representation in binary notation (octets).', NULL, 1, 'A finite sequence of binary digits (bits) for graphics.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.196000', '2019-10-02 15:28:31.196000', 0, NULL, NULL, 7),
    (609, 'aedabcd96e0846e697a53e43de6fafa0', 'Identifier', NULL, 'Identifier', NULL, NULL, 'CDT V3.0. An identifier is a character string used to uniquely identify one instance of an object within an identification scheme that is managed by an\nagency.', NULL, 1, 'A character string used to uniquely identify one instance of an object within an identification scheme that is managed by an agency.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.202000', '2019-10-02 15:28:31.202000', 0, NULL, NULL, 8),
    (610, '2ab3b84a56784a5da3cfcbdb39052d9c', 'Indicator', NULL, 'Indicator', NULL, NULL, 'CDT V3.0. An indicator is a list of two mutually exclusive Boolean values that express the only possible states of a property.', NULL, 1, 'The value of the Indicator.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.208000', '2019-10-02 15:28:31.208000', 0, NULL, NULL, 9),
    (611, '83b36601fb9f447f8e654c50b1423584', 'Measure', NULL, 'Measure', NULL, NULL, 'CDT V3.0. A measure is a numeric value determined by measuring an object along with the specified unit of measure.', NULL, 1, 'The numeric value determined by measuring an object.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.214000', '2019-10-02 15:28:31.214000', 0, NULL, NULL, 10),
    (612, '91d14e8c0941492ea236bef40302ddef', 'Name', NULL, 'Name', NULL, NULL, 'CDT V3.0. A name is a word or phrase that constitutes the distinctive designation of a person, place, thing or concept.', NULL, 1, 'A word or phrase that represents a designation of a person, place, thing or concept.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.219000', '2019-10-02 15:28:31.219000', 0, NULL, NULL, 11),
    (613, 'e0f9e1d8be6c4131bdbf85842cc337d3', 'Ordinal', NULL, 'Ordinal', NULL, NULL, 'CDT V3.0. An ordinal number is an assigned mathematical number that represents order or sequence.', NULL, 1, 'An assigned mathematical number that represents order or sequence', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.228000', '2019-10-02 15:28:31.228000', 0, NULL, NULL, 13),
    (614, 'f909ad14772846f6a5e5fa9be3a64951', 'Percent', NULL, 'Percent', NULL, NULL, 'CDT V3.0. A percent is a value representing a fraction of one hundred, expressed as a quotient.', NULL, 1, 'Numeric information that is assigned or is determined by percent.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.234000', '2019-10-02 15:28:31.234000', 0, NULL, NULL, 14),
    (615, '951e876e45ed4acfb6b982fa95f17504', 'Picture', NULL, 'Picture', NULL, NULL, 'CDT V3.0. A picture is a visual representation of a person, object, or scene in binary notation (octets).', NULL, 1, 'A finite sequence of binary digits (bits) for pictures.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.239000', '2019-10-02 15:28:31.239000', 0, NULL, NULL, 15),
    (616, '4a30a11e700a4cf89052756509c104be', 'Quantity', NULL, 'Quantity', NULL, NULL, 'CDT V3.0. A quantity is a counted number of non-monetary units, possibly including fractions.', NULL, 1, 'A counted number of non-monetary units possibly including fractions.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.245000', '2019-10-02 15:28:31.245000', 0, NULL, NULL, 16),
    (617, '9f9ff24ae1e44f17860f45debe2dd9c0', 'Rate', NULL, 'Rate', NULL, NULL, 'CDT V3.0. A rate is a quantity, amount, frequency, or dimensionless factor, measured against an independent base unit, expressed as a quotient.', NULL, 1, 'The numerical value of the rate.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.250000', '2019-10-02 15:28:31.250000', 0, NULL, NULL, 17),
    (618, '9f47d00520284b6c93b59f3768c363f9', 'Ratio', NULL, 'Ratio', NULL, NULL, 'CDT V3.0. A ratio is a relation between two independent quantities, using the same unit of measure or currency. A ratio can be expressed as either a\nquotient showing the number of times one value contains or is contained within the other, or as a proportion.', NULL, 1, 'The quotient or proportion between two independent quantities of the same unit of measure or currency.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.256000', '2019-10-02 15:28:31.256000', 0, NULL, NULL, 18),
    (619, '615bc5352c5d417eb3e70d7399b858e4', 'Sound', NULL, 'Sound', NULL, NULL, 'CDT V3.0. A sound is any form of an audio file such as audio recordings in binary notation (octets).', NULL, 1, 'A finite sequence of binary digits (bits) for sounds.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.261000', '2019-10-02 15:28:31.261000', 0, NULL, NULL, 19),
    (620, 'e83117ab2ba84804aa766dab03da06ef', 'Text', NULL, 'Text', NULL, NULL, 'CDT V3.0. Text is a character string such as a finite set of characters generally in the form of words of a language.', NULL, 1, 'A character string generally in the form of words of a language.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.266000', '2019-10-02 15:28:31.266000', 0, NULL, NULL, 20),
    (621, '163ef5f6c73d4ee2b2099f178e26b8f5', 'Time', NULL, 'Time', NULL, NULL, 'CDT V3.0. Time is a time of day to various common resolutions ??hour, minute, second and fractions thereof.', NULL, 1, 'The particular point in the progression of time.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.272000', '2019-10-02 15:28:31.272000', 0, NULL, NULL, 21),
    (622, 'cf0c409297a14795ab34c3642b00bfcc', 'Value', NULL, 'Value', NULL, NULL, 'CDT V3.0. A value is the numerical amount denoted by an algebraic term; a magnitude, quantity, or number.', NULL, 1, 'Numeric information that is assigned or is determined by value.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.277000', '2019-10-02 15:28:31.277000', 0, NULL, NULL, 22),
    (623, '9ddfa9408e9246c5952f22e96de05689', 'Video', NULL, 'Video', NULL, NULL, 'CDT V3.0. A video is a recording, reproducing or broadcasting of visual images on magnetic tape or digitally in binary notation (octets).', NULL, 1, 'A finite sequence of binary digits (bits) for videos.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.283000', '2019-10-02 15:28:31.283000', 0, NULL, NULL, 23);

UPDATE `dt` SET `prev_dt_id` = 602 WHERE `dt_id` = 1;
UPDATE `dt` SET `prev_dt_id` = 603 WHERE `dt_id` = 2;
UPDATE `dt` SET `prev_dt_id` = 604 WHERE `dt_id` = 3;
UPDATE `dt` SET `prev_dt_id` = 605 WHERE `dt_id` = 4;
UPDATE `dt` SET `prev_dt_id` = 606 WHERE `dt_id` = 5;
UPDATE `dt` SET `prev_dt_id` = 607 WHERE `dt_id` = 6;
UPDATE `dt` SET `prev_dt_id` = 608 WHERE `dt_id` = 7;
UPDATE `dt` SET `prev_dt_id` = 609 WHERE `dt_id` = 8;
UPDATE `dt` SET `prev_dt_id` = 610 WHERE `dt_id` = 9;
UPDATE `dt` SET `prev_dt_id` = 611 WHERE `dt_id` = 10;
UPDATE `dt` SET `prev_dt_id` = 612 WHERE `dt_id` = 11;
UPDATE `dt` SET `prev_dt_id` = 613 WHERE `dt_id` = 13;
UPDATE `dt` SET `prev_dt_id` = 614 WHERE `dt_id` = 14;
UPDATE `dt` SET `prev_dt_id` = 615 WHERE `dt_id` = 15;
UPDATE `dt` SET `prev_dt_id` = 616 WHERE `dt_id` = 16;
UPDATE `dt` SET `prev_dt_id` = 617 WHERE `dt_id` = 17;
UPDATE `dt` SET `prev_dt_id` = 618 WHERE `dt_id` = 18;
UPDATE `dt` SET `prev_dt_id` = 619 WHERE `dt_id` = 19;
UPDATE `dt` SET `prev_dt_id` = 620 WHERE `dt_id` = 20;
UPDATE `dt` SET `prev_dt_id` = 621 WHERE `dt_id` = 21;
UPDATE `dt` SET `prev_dt_id` = 622 WHERE `dt_id` = 22;
UPDATE `dt` SET `prev_dt_id` = 623 WHERE `dt_id` = 23;

INSERT INTO `dt_manifest` (`release_id`, `dt_id`, `based_dt_manifest_id`, `den`, `conflict`, `log_id`,
                           `replacement_dt_manifest_id`, `prev_dt_manifest_id`, `next_dt_manifest_id`)
VALUES ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 602, NULL, 'Amount. Type', 0, 9843, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 603, NULL, 'Binary Object. Type', 0, 9844, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 604, NULL, 'Code. Type', 0, 9845, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 605, NULL, 'Date. Type', 0, 9846, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 606, NULL, 'Date Time. Type', 0, 9847, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 607, NULL, 'Duration. Type', 0, 9848, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 608, NULL, 'Graphic. Type', 0, 9849, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 609, NULL, 'Identifier. Type', 0, 9850, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 610, NULL, 'Indicator. Type', 0, 9851, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 611, NULL, 'Measure. Type', 0, 9852, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 612, NULL, 'Name. Type', 0, 9853, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 613, NULL, 'Ordinal. Type', 0, 9855, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 614, NULL, 'Percent. Type', 0, 9856, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 615, NULL, 'Picture. Type', 0, 9857, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 616, NULL, 'Quantity. Type', 0, 9858, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 617, NULL, 'Rate. Type', 0, 9859, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 618, NULL, 'Ratio. Type', 0, 9860, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 619, NULL, 'Sound. Type', 0, 9861, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 620, NULL, 'Text. Type', 0, 9862, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 621, NULL, 'Time. Type', 0, 9863, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 622, NULL, 'Value. Type', 0, 9864, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0'), 623, NULL, 'Video. Type', 0, 9865, NULL, NULL, NULL),

       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 1, NULL, 'Amount. Type', 0, 9843, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 2, NULL, 'Binary Object. Type', 0, 9844, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 3, NULL, 'Code. Type', 0, 9845, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 4, NULL, 'Date. Type', 0, 9846, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 5, NULL, 'Date Time. Type', 0, 9847, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 6, NULL, 'Duration. Type', 0, 9848, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 7, NULL, 'Graphic. Type', 0, 9849, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 8, NULL, 'Identifier. Type', 0, 9850, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 9, NULL, 'Indicator. Type', 0, 9851, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 10, NULL, 'Measure. Type', 0, 9852, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 11, NULL, 'Name. Type', 0, 9853, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 12, NULL, 'Number. Type', 0, 9854, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 13, NULL, 'Ordinal. Type', 0, 9855, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 14, NULL, 'Percent. Type', 0, 9856, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 15, NULL, 'Picture. Type', 0, 9857, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 16, NULL, 'Quantity. Type', 0, 9858, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 17, NULL, 'Rate. Type', 0, 9859, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 18, NULL, 'Ratio. Type', 0, 9860, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 19, NULL, 'Sound. Type', 0, 9861, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 20, NULL, 'Text. Type', 0, 9862, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 21, NULL, 'Time. Type', 0, 9863, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 22, NULL, 'Value. Type', 0, 9864, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1'), 23, NULL, 'Video. Type', 0, 9865, NULL, NULL, NULL),

       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 1, NULL, 'Amount. Type', 0, 9843, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 2, NULL, 'Binary Object. Type', 0, 9844, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 3, NULL, 'Code. Type', 0, 9845, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 4, NULL, 'Date. Type', 0, 9846, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 5, NULL, 'Date Time. Type', 0, 9847, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 6, NULL, 'Duration. Type', 0, 9848, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 7, NULL, 'Graphic. Type', 0, 9849, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 8, NULL, 'Identifier. Type', 0, 9850, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 9, NULL, 'Indicator. Type', 0, 9851, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 10, NULL, 'Measure. Type', 0, 9852, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 11, NULL, 'Name. Type', 0, 9853, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 12, NULL, 'Number. Type', 0, 9854, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 13, NULL, 'Ordinal. Type', 0, 9855, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 14, NULL, 'Percent. Type', 0, 9856, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 15, NULL, 'Picture. Type', 0, 9857, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 16, NULL, 'Quantity. Type', 0, 9858, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 17, NULL, 'Rate. Type', 0, 9859, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 18, NULL, 'Ratio. Type', 0, 9860, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 19, NULL, 'Sound. Type', 0, 9861, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 20, NULL, 'Text. Type', 0, 9862, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 21, NULL, 'Time. Type', 0, 9863, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 22, NULL, 'Value. Type', 0, 9864, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working'), 23, NULL, 'Video. Type', 0, 9865, NULL, NULL, NULL);

UPDATE `dt_manifest` AS cdt31, `dt_manifest` AS working
SET `working`.`prev_dt_manifest_id` = `cdt31`.`dt_manifest_id`,
    `cdt31`.`next_dt_manifest_id` = `working`.`dt_manifest_id`
WHERE
    `cdt31`.`release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1') AND
    `working`.`release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working') AND
    `cdt31`.`dt_id` = `working`.`dt_id`;

UPDATE `dt_manifest` AS cdt30, `dt_manifest` AS cdt31
SET `cdt31`.`prev_dt_manifest_id` = `cdt30`.`dt_manifest_id`,
    `cdt30`.`next_dt_manifest_id` = `cdt31`.`dt_manifest_id`
WHERE
    `cdt30`.`release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0') AND
    `cdt31`.`release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1') AND
    `cdt30`.`den` = `cdt31`.`den`;

-- ------------------------------
-- Add DT_SC records for CDT v3.0
-- ------------------------------
INSERT INTO `dt_sc` (`dt_sc_id`, `guid`, `object_class_term`, `property_term`, `representation_term`, `definition`, `definition_source`, `owner_dt_id`, `cardinality_min`, `cardinality_max`, `based_dt_sc_id`, `default_value`, `fixed_value`, `is_deprecated`, `replacement_dt_sc_id`, `created_by`, `owner_user_id`, `last_updated_by`, `creation_timestamp`, `last_update_timestamp`, `prev_dt_sc_id`, `next_dt_sc_id`)
VALUES
    (3596, 'c95ae17672494fa1b8d8d4e1053a197d', 'Amount', 'Currency', 'Code', 'The currency of the amount', NULL, 602, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.159000', '2019-10-02 15:28:31.159000', NULL, NULL),
    (3597, '62f2d8d21a8846adbc82a2560fd019d4', 'Binary Object', 'MIME', 'Code', 'The Multipurpose Internet Mail Extensions(MIME) media type of the binary object', NULL, 603, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.167000', '2019-10-02 15:28:31.167000', NULL, NULL),
    (3598, '239940b16350458dba6238f759d62acd', 'Binary Object', 'Character Set', 'Code', 'The character set of the binary object if the Multipurpose Internet Mail Extensions (MIME) type is text', NULL, 603, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.167000', '2019-10-02 15:28:31.167000', NULL, NULL),
    (3599, '750e2e3019d14f4aa5de2fa242446100', 'Binary Object', 'Filename', 'Name', 'The filename of the binary object', NULL, 603, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.167000', '2019-10-02 15:28:31.167000', NULL, NULL),
    (3600, '7f887c1d388a41cda75fc24a6d12ad97', 'Code', 'List', 'Identifier', 'The identification of a list of codes', NULL, 604, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.173000', '2019-10-02 15:28:31.173000', NULL, NULL),
    (3601, '10ef56a1365842dc856007ca9fcc9021', 'Code', 'List Agency', 'Identifier', 'The identification of the agency that manages the code list.', NULL, 604, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.173000', '2019-10-02 15:28:31.173000', NULL, NULL),
    (3602, 'f5ce51f5a0de428c8fd23e6b6d3b6c25', 'Code', 'List Version', 'Identifier', 'The identification of the version of the list of codes.', NULL, 604, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.173000', '2019-10-02 15:28:31.173000', NULL, NULL),
    (3603, '58b2e9a4469047b38c9198ddeb0139bb', 'Date Time', 'Time Zone', 'Code', 'The time zone to which the date time refers', NULL, 606, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.185000', '2019-10-02 15:28:31.185000', NULL, NULL),
    (3604, '104475ffe3104f179642ef7067f15687', 'Graphic', 'MIME', 'Code', 'The Multipurpose Internet Mail Extensions (MIME) media type of the graphic.', NULL, 608, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.196000', '2019-10-02 15:28:31.196000', NULL, NULL),
    (3605, 'a7ccc9633ab14d88b8e15591fa326937', 'Graphic', 'Character Set', 'Code', 'The character set of the graphic if the Multipurpose Internet Mail Extensions (MIME) type is text.', NULL, 608, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.196000', '2019-10-02 15:28:31.196000', NULL, NULL),
    (3606, 'b5938aa9c1db4d489f4e4f1ce82a4299', 'Graphic', 'Filename', 'Name', 'The filename of the graphic', NULL, 608, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.196000', '2019-10-02 15:28:31.196000', NULL, NULL),
    (3607, 'e95806b86f054ddfa6e2ba4de2ceadca', 'Identifier', 'Scheme', 'Identifier', 'The identification of the identifier scheme.', NULL, 609, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.202000', '2019-10-02 15:28:31.202000', NULL, NULL),
    (3608, '0edacb772212453eb5a2f3fe211fe264', 'Identifier', 'Scheme Version', 'Identifier', 'The identification of the version of the identifier scheme', NULL, 609, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.202000', '2019-10-02 15:28:31.202000', NULL, NULL),
    (3609, 'fb70a55f2f60476cb18219c3efc7fc2e', 'Identifier', 'Scheme Agency', 'Identifier', 'The identification of the agency that manages the identifier scheme', NULL, 609, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.202000', '2019-10-02 15:28:31.202000', NULL, NULL),
    (3610, '65e4c342a86c4b1fb40fcc436f12c2f0', 'Measure', 'Unit', 'Code', 'The unit of measure', NULL, 611, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.214000', '2019-10-02 15:28:31.214000', NULL, NULL),
    (3611, '06bb3103d9d743c68dd3e257f2a837a4', 'Name', 'Language', 'Code', 'The language used in the corresponding text string', NULL, 612, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.219000', '2019-10-02 15:28:31.219000', NULL, NULL),
    (3612, 'c33b44bafee84b02b33ab79d1e0615c6', 'Picture', 'MIME', 'Code', 'The Multipurpose Internet Mail Extensions(MIME) media type of the picture', NULL, 615, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.239000', '2019-10-02 15:28:31.239000', NULL, NULL),
    (3613, '748dac4fb74049ea908bb8a6aeb220f7', 'Picture', 'Character Set', 'Code', 'The character set of the binary object if the Multipurpose Internet Mail Extensions (MIME) type is text', NULL, 615, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.239000', '2019-10-02 15:28:31.239000', NULL, NULL),
    (3614, 'd58325f5683e4647ac472b6f8a74eff8', 'Picture', 'Filename', 'Name', 'The filename of the picture', NULL, 615, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.239000', '2019-10-02 15:28:31.239000', NULL, NULL),
    (3615, 'a2b14b3b13c14035be66706c9a76b16b', 'Quantity', 'Unit', 'Code', 'The unit of measure in which the quantity is expressed', NULL, 616, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.245000', '2019-10-02 15:28:31.245000', NULL, NULL),
    (3616, 'c36bd97e23e046bf8a4a2734029d78b6', 'Rate', 'Multiplier', 'Value', 'The multiplier of the Rate. Unit. Code or Rate. Currency. Code', NULL, 617, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.250000', '2019-10-02 15:28:31.250000', NULL, NULL),
    (3617, 'd640f41c41e046a28e74fc91fa34f5c5', 'Rate', 'Unit', 'Code', 'The unit of measure of the numerator', NULL, 617, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.250000', '2019-10-02 15:28:31.250000', NULL, NULL),
    (3618, '858a469bc1b14aeca2fd318a34f13295', 'Rate', 'Currency', 'Code', 'The currency of the numerator', NULL, 617, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.250000', '2019-10-02 15:28:31.250000', NULL, NULL),
    (3619, 'bb23ca31fed5471390c096d3e955e27a', 'Rate', 'Base Multiplier', 'Value', 'The multiplier of the Rate. Base Unit. Code or Rate. Base Currency. Code', NULL, 617, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.250000', '2019-10-02 15:28:31.250000', NULL, NULL),
    (3620, 'aa236cb529524784b0c034d105d4baed', 'Rate', 'Base Unit', 'Code', 'The unit of measure of the denominator', NULL, 617, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.250000', '2019-10-02 15:28:31.250000', NULL, NULL),
    (3621, '4f6d7dd28fab4083a987c00376f9b2f4', 'Rate', 'Base Currency', 'Code', 'The currency of the denominator', NULL, 617, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.250000', '2019-10-02 15:28:31.250000', NULL, NULL),
    (3622, '1204dda43fdf48289db283c71479eec5', 'Sound', 'MIME', 'Code', 'The Multipurpose Internet Mail Extensions(MIME) media type of the sound', NULL, 619, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.261000', '2019-10-02 15:28:31.261000', NULL, NULL),
    (3623, '317b944b80954a098933d160c86628a2', 'Sound', 'Character Set', 'Code', 'The character set of the binary object if the Multipurpose Internet Mail Extensions (MIME) type is text', NULL, 619, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.261000', '2019-10-02 15:28:31.261000', NULL, NULL),
    (3624, 'd3424fa8ce8f4594b0062528c6bb48b1', 'Sound', 'Filename', 'Name', 'The filename of the sound', NULL, 619, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.261000', '2019-10-02 15:28:31.261000', NULL, NULL),
    (3625, 'df786f3edd654d679925b39ab812c6ed', 'Text', 'Language', 'Code', 'The language used in the corresponding text string', NULL, 620, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.266000', '2019-10-02 15:28:31.266000', NULL, NULL),
    (3626, '2962b9367978474a8feca6b7b62455df', 'Video', 'MIME', 'Code', 'The Multipurpose Internet Mail Extensions(MIME) media type of the video', NULL, 623, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.283000', '2019-10-02 15:28:31.283000', NULL, NULL),
    (3627, 'ff760165456b483c8bcbbdd8f60f72dc', 'Video', 'Character Set', 'Code', 'The character set of the binary object if the Multipurpose Internet Mail Extensions (MIME) type is text', NULL, 623, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.283000', '2019-10-02 15:28:31.283000', NULL, NULL),
    (3628, '56be979f46cb418995f9198f97e345f3', 'Video', 'Filename', 'Name', 'The filename of the video', NULL, 623, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.283000', '2019-10-02 15:28:31.283000', NULL, NULL);

UPDATE `dt_sc` AS cdt_sc_30, `dt_sc` AS cdt_sc_31
SET `cdt_sc_31`.`prev_dt_sc_id` = `cdt_sc_30`.`dt_sc_id`,
    `cdt_sc_30`.`next_dt_sc_id` = `cdt_sc_31`.`dt_sc_id`
WHERE
    `cdt_sc_31`.`owner_dt_id` >= 1 AND `cdt_sc_31`.`owner_dt_id` <= 23 AND
    `cdt_sc_30`.`owner_dt_id` >= 602 AND `cdt_sc_31`.`owner_dt_id` <= 623 AND
    `cdt_sc_30`.`guid` = `cdt_sc_31`.`guid`;

-- Add DT_SC_MANIFESTs to v3.0
INSERT INTO `dt_sc_manifest` (`release_id`, `dt_sc_id`, `owner_dt_manifest_id`)
SELECT
    `release`.`release_id`, `dt_sc_id`, `dt_manifest`.`dt_manifest_id`
FROM
    `release`,
    `dt_sc` JOIN `dt` ON `dt_sc`.`owner_dt_id` = `dt`.`dt_id`
            JOIN `dt_manifest` ON `dt_sc`.`owner_dt_id` = `dt_manifest`.`dt_id`
WHERE
    `release`.`library_id` = 1 AND `release`.`release_num` = '3.0' AND
    `dt_sc`.`dt_sc_id` >= 3596 AND `dt_sc`.`dt_sc_id` <= 3628 AND `dt_manifest`.`release_id` = `release`.`release_id`;

-- Add DT_SC_MANIFESTs to v3.1
INSERT INTO `dt_sc_manifest` (`release_id`, `dt_sc_id`, `owner_dt_manifest_id`)
SELECT
    `release`.`release_id`, `dt_sc_id`, `dt_manifest`.`dt_manifest_id`
FROM
    `release`,
    `dt_sc` JOIN `dt` ON `dt_sc`.`owner_dt_id` = `dt`.`dt_id`
            JOIN `dt_manifest` ON `dt_sc`.`owner_dt_id` = `dt_manifest`.`dt_id`
WHERE
    `release`.`library_id` = 1 AND `release`.`release_num` = '3.1' AND
    `dt_sc`.`dt_sc_id` >= 1 AND `dt_sc`.`dt_sc_id` <= 34 AND `dt_manifest`.`release_id` = `release`.`release_id`;

-- Add DT_SC_MANIFESTs to Working
INSERT INTO `dt_sc_manifest` (`release_id`, `dt_sc_id`, `owner_dt_manifest_id`)
SELECT
    `release`.`release_id`, `dt_sc_id`, `dt_manifest`.`dt_manifest_id`
FROM
    `release`,
    `dt_sc` JOIN `dt` ON `dt_sc`.`owner_dt_id` = `dt`.`dt_id`
            JOIN `dt_manifest` ON `dt_sc`.`owner_dt_id` = `dt_manifest`.`dt_id`
WHERE
    `release`.`library_id` = 1 AND `release`.`release_num` = 'Working' AND
    `dt_sc`.`dt_sc_id` >= 1 AND `dt_sc`.`dt_sc_id` <= 34 AND `dt_manifest`.`release_id` = `release`.`release_id`;

UPDATE `dt_sc_manifest` AS cdt_sc_31, `dt_sc_manifest` AS working
SET `working`.`prev_dt_sc_manifest_id` = `cdt_sc_31`.`dt_sc_manifest_id`,
    `cdt_sc_31`.`next_dt_sc_manifest_id` = `working`.`dt_sc_manifest_id`
WHERE
    `cdt_sc_31`.`release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1') AND
    `working`.`release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working') AND
    `cdt_sc_31`.`dt_sc_id` = `working`.`dt_sc_id`;

UPDATE `dt_sc_manifest` AS cdt_sc_30, `dt_sc_manifest` AS cdt_sc_31,
       `dt_sc` AS sc_30, `dt_sc` AS sc_31
SET `cdt_sc_31`.`prev_dt_sc_manifest_id` = `cdt_sc_30`.`dt_sc_manifest_id`,
    `cdt_sc_30`.`next_dt_sc_manifest_id` = `cdt_sc_31`.`dt_sc_manifest_id`
WHERE
    `cdt_sc_30`.`release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0') AND
    `cdt_sc_31`.`release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1') AND
    `cdt_sc_30`.`dt_sc_id` = `sc_30`.`dt_sc_id` AND
    `cdt_sc_31`.`dt_sc_id` = `sc_31`.`dt_sc_id` AND
    `sc_30`.`guid` = `sc_31`.`guid`;

-- -----------------------------------
-- Add DT records for ISO 15000-5:2014
-- -----------------------------------
INSERT INTO `dt` (`dt_id`, `guid`, `data_type_term`, `qualifier`, `representation_term`, `six_digit_id`, `based_dt_id`, `definition`, `definition_source`, `namespace_id`, `content_component_definition`, `state`, `commonly_used`, `created_by`, `last_updated_by`, `owner_user_id`, `creation_timestamp`, `last_update_timestamp`, `is_deprecated`, `replacement_dt_id`, `prev_dt_id`, `next_dt_id`)
VALUES
    (624, '8fb269cf7b4c4747beb61657b7ce9667', 'Amount', NULL, 'Amount', NULL, NULL, 'A number of monetary units specified in a currency where the unit of currency is explicit or implied.', NULL, 1, 'A number of monetary units.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.159000', '2019-10-02 15:28:31.159000', 0, NULL, NULL, NULL),
    (625, '2523e8e70f2941d4b55196b0552999e1', 'Binary Object', NULL, 'Binary Object', NULL, NULL, 'A set of finite-length sequences of binary octets.', NULL, 1, 'A finite sequence of binary digits (bits).', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.167000', '2019-10-02 15:28:31.167000', 0, NULL, NULL, NULL),
    (626, 'ef4ceb88846a40acb31bdec130108e77', 'Code', NULL, 'Code', NULL, NULL, 'A character string (letters, figures or symbols) that for brevity and/or language independence may be used to represent or replace a definitive value or text of an attribute together with relevant supplementary information.', NULL, 1, 'A character string (letters, figures or symbols) that for brevity and/or language independence may be used to represent or replace a definitive value or text of an attribute.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.173000', '2019-10-02 15:28:31.173000', 0, NULL, NULL, NULL),
    (627, '79cb0782c764408a939edd051d9f6611', 'Date Time', NULL, 'Date Time', NULL, NULL, 'A particular point in the progression of time together with relevant supplementary information.', NULL, 1, 'The particular date and time point in the progression of time.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.185000', '2019-10-02 15:28:31.185000', 0, NULL, NULL, NULL),
    (628, 'e6ce0624e7be475e9874cd090a8195a9', 'Identifier', NULL, 'Identifier', NULL, NULL, 'A character string to identify and distinguish uniquely, one instance of an object in an identification scheme from all other objects in the same scheme together with relevant supplementary information.', NULL, 1, 'A character string used to uniquely identify one instance of an object within an identification scheme that is managed by an agency.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.202000', '2019-10-02 15:28:31.202000', 0, NULL, NULL, NULL),
    (629, 'b3bff626f5854d88a4f9911872e9b8ea', 'Indicator', NULL, 'Indicator', NULL, NULL, 'A list of two mutually exclusive Boolean values that express the only possible states of a Property.', NULL, 1, 'The value of the Indicator.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.208000', '2019-10-02 15:28:31.208000', 0, NULL, NULL, NULL),
    (630, '338741ddf4cb4715aa2b7b97bd6500a1', 'Measure', NULL, 'Measure', NULL, NULL, 'A numeric value determined by measuring an object along with the specified unit of measure.', NULL, 1, 'The numeric value determined by measuring an object.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.214000', '2019-10-02 15:28:31.214000', 0, NULL, NULL, NULL),
    -- 596 - Numeric
    (632, '194a084e9ba8427d8b7daf42c6083698', 'Quantity', NULL, 'Quantity', NULL, NULL, 'A counted number of non-monetary units possibly including fractions.', NULL, 1, 'A counted number of non-monetary units possibly including fractions.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.245000', '2019-10-02 15:28:31.245000', 0, NULL, NULL, NULL),
    (633, '53a8b3be1de742369ad0df4811606894', 'Text', NULL, 'Text', NULL, NULL, 'A character string (i.e. a finite set of characters) generally in the form of words of a language.', NULL, 1, 'A character string generally in the form of words of a language.', 'Published', 0, 0, 0, 1, '2019-10-02 15:28:31.266000', '2019-10-02 15:28:31.266000', 0, NULL, NULL, NULL);

INSERT INTO `dt_manifest` (`release_id`, `dt_id`, `based_dt_manifest_id`, `den`, `conflict`, `log_id`,
                           `replacement_dt_manifest_id`, `prev_dt_manifest_id`, `next_dt_manifest_id`)
VALUES ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 624, NULL, 'Amount. Type', 0, 9843, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 625, NULL, 'Binary Object. Type', 0, 9844, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 626, NULL, 'Code. Type', 0, 9845, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 627, NULL, 'Date Time. Type', 0, 9847, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 628, NULL, 'Identifier. Type', 0, 9850, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 629, NULL, 'Indicator. Type', 0, 9851, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 630, NULL, 'Measure. Type', 0, 9852, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 596, NULL, 'Numeric. Type', 0, 23345, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 632, NULL, 'Quantity. Type', 0, 9858, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 633, NULL, 'Text. Type', 0, 9862, NULL, NULL, NULL),

       ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 624, NULL, 'Amount. Type', 0, 9843, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 625, NULL, 'Binary Object. Type', 0, 9844, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 626, NULL, 'Code. Type', 0, 9845, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 627, NULL, 'Date Time. Type', 0, 9847, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 628, NULL, 'Identifier. Type', 0, 9850, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 629, NULL, 'Indicator. Type', 0, 9851, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 630, NULL, 'Measure. Type', 0, 9852, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 596, NULL, 'Numeric. Type', 0, 23345, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 632, NULL, 'Quantity. Type', 0, 9858, NULL, NULL, NULL),
       ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 633, NULL, 'Text. Type', 0, 9862, NULL, NULL, NULL);

UPDATE `dt_manifest` AS cdt2014, `dt_manifest` AS working
SET `working`.`prev_dt_manifest_id` = `cdt2014`.`dt_manifest_id`,
    `cdt2014`.`next_dt_manifest_id` = `working`.`dt_manifest_id`
WHERE
    `cdt2014`.`release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014') AND
    `working`.`release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working') AND
    `cdt2014`.`dt_id` = `working`.`dt_id`;

-- --------------------------------------
-- Add DT_SC records for ISO 15000-5:2014
-- --------------------------------------
UPDATE `dt_sc` SET `owner_dt_id` = 624 WHERE `owner_dt_id` = 1 AND `dt_sc_id` >= 1544 AND `dt_sc_id` <= 1577;
UPDATE `dt_sc` SET `owner_dt_id` = 625 WHERE `owner_dt_id` = 2 AND `dt_sc_id` >= 1544 AND `dt_sc_id` <= 1577;
UPDATE `dt_sc` SET `owner_dt_id` = 626 WHERE `owner_dt_id` = 3 AND `dt_sc_id` >= 1544 AND `dt_sc_id` <= 1577;
UPDATE `dt_sc` SET `owner_dt_id` = 627 WHERE `owner_dt_id` = 5 AND `dt_sc_id` >= 1544 AND `dt_sc_id` <= 1577;
UPDATE `dt_sc` SET `owner_dt_id` = 628 WHERE `owner_dt_id` = 8 AND `dt_sc_id` >= 1544 AND `dt_sc_id` <= 1577;
UPDATE `dt_sc` SET `owner_dt_id` = 629 WHERE `owner_dt_id` = 9 AND `dt_sc_id` >= 1544 AND `dt_sc_id` <= 1577;
UPDATE `dt_sc` SET `owner_dt_id` = 630 WHERE `owner_dt_id` = 10 AND `dt_sc_id` >= 1544 AND `dt_sc_id` <= 1577;
UPDATE `dt_sc` SET `owner_dt_id` = 632 WHERE `owner_dt_id` = 16 AND `dt_sc_id` >= 1544 AND `dt_sc_id` <= 1577;
UPDATE `dt_sc` SET `owner_dt_id` = 633 WHERE `owner_dt_id` = 20 AND `dt_sc_id` >= 1544 AND `dt_sc_id` <= 1577;

INSERT INTO `dt_sc` (`dt_sc_id`, `guid`, `object_class_term`, `property_term`, `representation_term`, `definition`, `definition_source`, `owner_dt_id`, `cardinality_min`, `cardinality_max`, `based_dt_sc_id`, `default_value`, `fixed_value`, `is_deprecated`, `replacement_dt_sc_id`, `created_by`, `owner_user_id`, `last_updated_by`, `creation_timestamp`, `last_update_timestamp`, `prev_dt_sc_id`, `next_dt_sc_id`)
VALUES
    (3629, 'b01d2dd2a8be4dcd9edaae1f65be691c', 'Quantity', 'Unit', 'Code', 'The unit of measure in which the quantity is expressed', NULL, 632, 0, 1, NULL, NULL, NULL, 0, NULL, 0, 1, 0, '2019-10-02 15:28:31.245000', '2019-10-02 15:28:31.245000', NULL, NULL);

INSERT INTO `dt_sc_manifest` (`release_id`, `dt_sc_id`, `owner_dt_manifest_id`, `based_dt_sc_manifest_id`, `conflict`, `replacement_dt_sc_manifest_id`, `prev_dt_sc_manifest_id`, `next_dt_sc_manifest_id`)
VALUES
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1544,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 624 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1545,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 624 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),

    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1546,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 625 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1547,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 625 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1548,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 625 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1549,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 625 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1550,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 625 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1551,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 625 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),

    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1552,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 626 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1553,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 626 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1554,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 626 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1555,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 626 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1556,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 626 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1557,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 626 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1558,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 626 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1559,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 626 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1560,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 626 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),

    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1561,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 627 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),

    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1562,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 628 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1563,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 628 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1564,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 628 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1565,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 628 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1566,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 628 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1567,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 628 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1568,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 628 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),

    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1569,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 629 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),

    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1570,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 630 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1571,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 630 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),

    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1572,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 596 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),

    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 3629,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 632 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1573,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 632 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1574,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 632 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1575,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 632 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),

    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1576,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 633 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014'), 1577,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 633 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014')),
     NULL, 0, NULL, NULL, NULL),

    -- Working

    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1544,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 624 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1545,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 624 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),

    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1546,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 625 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1547,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 625 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1548,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 625 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1549,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 625 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1550,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 625 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1551,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 625 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),

    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1552,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 626 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1553,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 626 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1554,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 626 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1555,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 626 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1556,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 626 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1557,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 626 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1558,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 626 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1559,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 626 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1560,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 626 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),

    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1561,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 627 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),

    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1562,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 628 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1563,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 628 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1564,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 628 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1565,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 628 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1566,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 628 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1567,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 628 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1568,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 628 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),

    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1569,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 629 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),

    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1570,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 630 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1571,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 630 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),

    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1572,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 596 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),

    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 3629,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 632 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1573,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 632 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1574,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 632 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1575,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 632 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),

    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1576,
     (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 633 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
     NULL, 0, NULL, NULL, NULL),
    ((SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working'), 1577,
        (SELECT `dt_manifest_id` FROM `dt_manifest` WHERE `dt_id` = 633 AND `release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working')),
        NULL, 0, NULL, NULL, NULL);

UPDATE `dt_sc_manifest` AS cdt_sc_31, `dt_sc_manifest` AS working
SET `working`.`prev_dt_sc_manifest_id` = `cdt_sc_31`.`dt_sc_manifest_id`,
    `cdt_sc_31`.`next_dt_sc_manifest_id` = `working`.`dt_sc_manifest_id`
WHERE
    `cdt_sc_31`.`release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014') AND
    `working`.`release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working') AND
    `cdt_sc_31`.`dt_sc_id` = `working`.`dt_sc_id`;

-- ------------------------------------
-- Update ownership of CDTs and CDT_SCs
-- ------------------------------------
UPDATE `dt`, (
    SELECT
    DISTINCT `dt_id`
    FROM `dt_manifest`
    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
    WHERE `release`.`library_id` IN (1, 2)
) t
SET `dt`.`owner_user_id` = 0
WHERE `dt`.`dt_id` = t.`dt_id`;

UPDATE `dt_sc`, (
    SELECT
    DISTINCT `dt_sc_id`
    FROM `dt_sc_manifest`
    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
    WHERE `release`.`library_id` IN (1, 2)
) t
SET `dt_sc`.`owner_user_id` = 0
WHERE `dt_sc`.`dt_sc_id` = t.`dt_sc_id`;

-- -------------------------------------------------------------
-- Delete the SCs owned by the CDTs that belonged to connectSpec
-- -------------------------------------------------------------
DELETE `dt_sc_manifest` FROM `dt_sc_manifest`, (
SELECT `dt_sc_manifest_id`
FROM `dt_sc_manifest`
JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
WHERE `release`.`library_id` NOT IN (1, 2) AND `dt_sc_manifest`.`dt_sc_id` IN (
SELECT DISTINCT `dt_sc_id`
FROM `dt_sc_manifest`
JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
WHERE `release`.`library_id` IN (1, 2)
)) t WHERE `dt_sc_manifest`.`dt_sc_manifest_id` = t.`dt_sc_manifest_id`;

-- ---------------
-- Add XBT records
-- ---------------
INSERT INTO `xbt` (`xbt_id`, `guid`, `name`, `builtIn_type`, `jbt_draft05_map`, `openapi30_map`, `avro_map`, `subtype_of_xbt_id`, `schema_definition`, `revision_doc`, `state`, `created_by`, `owner_user_id`, `last_updated_by`, `creation_timestamp`, `last_update_timestamp`, `is_deprecated`)
VALUES
    (126, 'cde3a6544ef046758f81fafd2412fbcd', 'non positive integer', 'xsd:nonPositiveInteger', '{\"type\":\"number\", \"multipleOf\":1, \"maximum\":0, \"exclusiveMaximum\":false}', '{\"type\":\"integer\", \"maximum\":0, \"exclusiveMaximum\":false}', '{\"type\":\"int\"}', 21, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0),
    (127, 'df01213dc7a2416cbb1cee9c36417bd4', 'negative integer', 'xsd:negativeInteger', '{\"type\":\"number\", \"multipleOf\":1, \"maximum\":0, \"exclusiveMaximum\":true}', '{\"type\":\"integer\", \"maximum\":0, \"exclusiveMaximum\":true}', '{\"type\":\"int\"}', 126, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0),
    (128, 'f03ffbb9cdf049a28d7d94a08be43398', 'long', 'xsd:long', '{\"type\":\"number\", \"multipleOf\":1}', '{\"type\":\"integer\"}', '{\"type\":\"int\"}', 21, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0),
    (129, 'be7c68a204514b89a85661dcb8eb9276', 'int', 'xsd:int', '{\"type\":\"number\", \"multipleOf\":1, \"minimum\":-2147483648, \"maximum\":2147483647}', '{\"type\":\"integer\", \"minimum\":-2147483648, \"maximum\":2147483647}', '{\"type\":\"int\"}', 128, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0),
    (130, '713c8f3829f04f8eb52977073bcd250d', 'short', 'xsd:short', '{\"type\":\"number\", \"multipleOf\":1, \"minimum\":-32768, \"maximum\":32767}', '{\"type\":\"integer\", \"minimum\":-32768, \"maximum\":32767}', '{\"type\":\"int\"}', 129, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0),
    (131, '37ddb910e2184816a5f335e7b5f631e2', 'byte', 'xsd:byte', '{\"type\":\"number\", \"multipleOf\":1, \"minimum\":-128, \"maximum\":127}', '{\"type\":\"integer\", \"minimum\":-128, \"maximum\":127}', '{\"type\":\"int\"}', 130, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0),
    (132, '55b409dd0f194e4caa29085f7170b088', 'unsigned long', 'xsd:unsignedLong', '{\"type\":\"number\", \"multipleOf\":1, \"minimum\":0}', '{\"type\":\"integer\", \"minimum\":0}', '{\"type\":\"int\"}', 22, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0),
    (133, 'fbe50d0ffd7a4ddfaca504312eab3eb5', 'unsigned int', 'xsd:unsignedInt', '{\"type\":\"number\", \"multipleOf\":1, \"minimum\":0, \"maximum\":4294967295}', '{\"type\":\"integer\", \"minimum\":0, \"maximum\":4294967295}', '{\"type\":\"int\"}', 132, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0),
    (134, 'cd16b9873aef4d6286f5f0215ad2974e', 'unsigned short', 'xsd:unsignedShort', '{\"type\":\"number\", \"multipleOf\":1, \"minimum\":0, \"maximum\":65535}', '{\"type\":\"integer\", \"minimum\":0, \"maximum\":65535}', '{\"type\":\"int\"}', 133, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0),
    (135, 'b240df3d59cd4c949c972eb2d41b64d3', 'unsigned byte', 'xsd:unsignedByte', '{\"type\":\"number\", \"multipleOf\":1, \"minimum\":0, \"maximum\":255}', '{\"type\":\"integer\", \"minimum\":0, \"maximum\":255}', '{\"type\":\"int\"}', 134, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0),
    (136, '3b2596f3c253432b9e5149a55d79d88c', 'qualified name', 'xsd:QName', '{\"type\":\"string\"}', '{\"type\":\"string\"}', '{\"type\":\"string\"}', 2, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0),
    (137, 'f8f4e42af2d0439c9905e746c3fd58a0', 'notation', 'xsd:NOTATION', '{\"type\":\"string\"}', '{\"type\":\"string\"}', '{\"type\":\"string\"}', 2, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0),
    (138, '34b854ba94644e6ca5b0a0fef4a1fa98', 'name', 'xsd:Name', '{\"type\":\"string\"}', '{\"type\":\"string\"}', '{\"type\":\"string\"}', 14, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0),
    (139, '11646467e9b044a7b010a815d7276a14', 'name token', 'xsd:NMTOKEN', '{\"type\":\"string\"}', '{\"type\":\"string\"}', '{\"type\":\"string\"}', 14, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0),
    (140, '63a692a8087344b2a4e4f3c3712bf71b', 'name tokens', 'xsd:NMTOKENS', '{\"type\":\"string\"}', '{\"type\":\"string\"}', '{\"type\":\"string\"}', 139, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0),
    (141, 'b91b66f3a5d446b89e3d59aa1947f80e', 'non colonized name', 'xsd:NCName', '{\"type\":\"string\"}', '{\"type\":\"string\"}', '{\"type\":\"string\"}', 138, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0),
    (142, '8097dc7dd40f43b593a9d4bb36551901', 'identifier', 'xsd:ID', '{\"type\":\"string\"}', '{\"type\":\"string\"}', '{\"type\":\"string\"}', 141, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0),
    (143, 'ac612108bfb04eafba1ad8f045783fdf', 'identifier reference', 'xsd:IDREF', '{\"type\":\"string\"}', '{\"type\":\"string\"}', '{\"type\":\"string\"}', 141, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0),
    (144, '16607035ba2942f5abaf6b420b5a49d7', 'identifier references', 'xsd:IDREFS', '{\"type\":\"string\"}', '{\"type\":\"string\"}', '{\"type\":\"string\"}', 143, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0),
    (145, 'd221e89d2224488ca30d3c4e2ee06f8b', 'entity', 'xsd:ENTITY', '{\"type\":\"string\"}', '{\"type\":\"string\"}', '{\"type\":\"string\"}', 141, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0),
    (146, 'ad620dcc21554ed995d861a1b3a1aa2d', 'entities', 'xsd:ENTITIES', '{\"type\":\"string\"}', '{\"type\":\"string\"}', '{\"type\":\"string\"}', 145, NULL, NULL, 3, 0, 1, 0, '2019-10-02 15:28:29.742000', '2019-10-02 15:28:29.742000', 0);

INSERT INTO `log` (`hash`, `revision_num`, `revision_tracking_num`, `log_action`, `reference`, `snapshot`, `prev_log_id`, `next_log_id`, `created_by`, `creation_timestamp`)
VALUES
    ('f9a47d7bc05d5af466bcf30f4779007f', 1, 1, 'Added', 'ad620dcc21554ed995d861a1b3a1aa2d', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A454E544954494553222C22737562547970654F66586274223A7B226E616D65223A22656E74697479222C2267756964223A226432323165383964323232343438386361333064336334653265653036663862227D2C226E616D65223A22656E746974696573222C2267756964223A226164363230646363323135353465643939356438363161316233613161613264222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A454E544954494553222C226176726F5F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227862745F6964223A3134362C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A22656E746974696573222C2267756964223A226164363230646363323135353465643939356438363161316233613161613264222C227374617465223A332C22737562747970655F6F665F7862745F6964223A3134357D2C227862744D616E6966657374223A7B227862745F6964223A3134362C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393930322C22707265765F7862745F6D616E69666573745F6964223A393939342C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000'),
    ('9b2adc50de2d8933269a63b2350d0f39', 1, 1, 'Added', 'd221e89d2224488ca30d3c4e2ee06f8b', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A454E54495459222C22737562547970654F66586274223A7B226E616D65223A226E6F6E20636F6C6F6E697A6564206E616D65222C2267756964223A226239316236366633613564343436623839653364353961613139343766383065227D2C226E616D65223A22656E74697479222C2267756964223A226432323165383964323232343438386361333064336334653265653036663862222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A454E54495459222C226176726F5F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227862745F6964223A3134352C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A22656E74697479222C2267756964223A226432323165383964323232343438386361333064336334653265653036663862222C227374617465223A332C22737562747970655F6F665F7862745F6964223A3134317D2C227862744D616E6966657374223A7B227862745F6964223A3134352C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393930312C22707265765F7862745F6D616E69666573745F6964223A393939332C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000'),
    ('58c01cf4f96e40f892d4a4b2d4f0e677', 1, 1, 'Added', '16607035ba2942f5abaf6b420b5a49d7', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A494452454653222C22737562547970654F66586274223A7B226E616D65223A226964656E746966696572207265666572656E6365222C2267756964223A226163363132313038626662303465616662613161643866303435373833666466227D2C226E616D65223A226964656E746966696572207265666572656E636573222C2267756964223A223136363037303335626132393432663561626166366234323062356134396437222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A494452454653222C226176726F5F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227862745F6964223A3134342C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A226964656E746966696572207265666572656E636573222C2267756964223A223136363037303335626132393432663561626166366234323062356134396437222C227374617465223A332C22737562747970655F6F665F7862745F6964223A3134337D2C227862744D616E6966657374223A7B227862745F6964223A3134342C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393930302C22707265765F7862745F6D616E69666573745F6964223A393939322C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000'),
    ('7b8e1f56189c352fbf749d032465fcd9', 1, 1, 'Added', 'ac612108bfb04eafba1ad8f045783fdf', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A4944524546222C22737562547970654F66586274223A7B226E616D65223A226E6F6E20636F6C6F6E697A6564206E616D65222C2267756964223A226239316236366633613564343436623839653364353961613139343766383065227D2C226E616D65223A226964656E746966696572207265666572656E6365222C2267756964223A226163363132313038626662303465616662613161643866303435373833666466222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A4944524546222C226176726F5F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227862745F6964223A3134332C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A226964656E746966696572207265666572656E6365222C2267756964223A226163363132313038626662303465616662613161643866303435373833666466222C227374617465223A332C22737562747970655F6F665F7862745F6964223A3134317D2C227862744D616E6966657374223A7B227862745F6964223A3134332C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393839392C22707265765F7862745F6D616E69666573745F6964223A393939312C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000'),
    ('1a754727dc50f3e75a8b09027af93b98', 1, 1, 'Added', '8097dc7dd40f43b593a9d4bb36551901', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A4944222C22737562547970654F66586274223A7B226E616D65223A226E6F6E20636F6C6F6E697A6564206E616D65222C2267756964223A226239316236366633613564343436623839653364353961613139343766383065227D2C226E616D65223A226964656E746966696572222C2267756964223A223830393764633764643430663433623539336139643462623336353531393031222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A4944222C226176726F5F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227862745F6964223A3134322C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A226964656E746966696572222C2267756964223A223830393764633764643430663433623539336139643462623336353531393031222C227374617465223A332C22737562747970655F6F665F7862745F6964223A3134317D2C227862744D616E6966657374223A7B227862745F6964223A3134322C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393839382C22707265765F7862745F6D616E69666573745F6964223A393939302C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000'),
    ('a50ad94bdd39cf58acd5935446959f30', 1, 1, 'Added', 'b91b66f3a5d446b89e3d59aa1947f80e', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A4E434E616D65222C22737562547970654F66586274223A7B226E616D65223A226E616D65222C2267756964223A223334623835346261393436343465366361356230613066656634613166613938227D2C226E616D65223A226E6F6E20636F6C6F6E697A6564206E616D65222C2267756964223A226239316236366633613564343436623839653364353961613139343766383065222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A4E434E616D65222C226176726F5F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227862745F6964223A3134312C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A226E6F6E20636F6C6F6E697A6564206E616D65222C2267756964223A226239316236366633613564343436623839653364353961613139343766383065222C227374617465223A332C22737562747970655F6F665F7862745F6964223A3133387D2C227862744D616E6966657374223A7B227862745F6964223A3134312C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393839372C22707265765F7862745F6D616E69666573745F6964223A393938392C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000'),
    ('3a780444803f878168824535e136aae5', 1, 1, 'Added', '63a692a8087344b2a4e4f3c3712bf71b', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A4E4D544F4B454E53222C22737562547970654F66586274223A7B226E616D65223A226E616D6520746F6B656E222C2267756964223A223131363436343637653962303434613762303130613831356437323736613134227D2C226E616D65223A226E616D6520746F6B656E73222C2267756964223A223633613639326138303837333434623261346534663363333731326266373162222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A4E4D544F4B454E53222C226176726F5F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227862745F6964223A3134302C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A226E616D6520746F6B656E73222C2267756964223A223633613639326138303837333434623261346534663363333731326266373162222C227374617465223A332C22737562747970655F6F665F7862745F6964223A3133397D2C227862744D616E6966657374223A7B227862745F6964223A3134302C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393839362C22707265765F7862745F6D616E69666573745F6964223A393938382C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000'),
    ('8b201a3377074770e611cc8163e40f50', 1, 1, 'Added', '11646467e9b044a7b010a815d7276a14', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A4E4D544F4B454E222C22737562547970654F66586274223A7B226E616D65223A22746F6B656E222C2267756964223A223039363364643264323230383462343839336666363966663330336535376439227D2C226E616D65223A226E616D6520746F6B656E222C2267756964223A223131363436343637653962303434613762303130613831356437323736613134222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A4E4D544F4B454E222C226176726F5F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227862745F6964223A3133392C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A226E616D6520746F6B656E222C2267756964223A223131363436343637653962303434613762303130613831356437323736613134222C227374617465223A332C22737562747970655F6F665F7862745F6964223A31347D2C227862744D616E6966657374223A7B227862745F6964223A3133392C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393839352C22707265765F7862745F6D616E69666573745F6964223A393938372C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000'),
    ('177603814a1f89fbff106bd7a7f2eb40', 1, 1, 'Added', '34b854ba94644e6ca5b0a0fef4a1fa98', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A4E616D65222C22737562547970654F66586274223A7B226E616D65223A22746F6B656E222C2267756964223A223039363364643264323230383462343839336666363966663330336535376439227D2C226E616D65223A226E616D65222C2267756964223A223334623835346261393436343465366361356230613066656634613166613938222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A4E616D65222C226176726F5F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227862745F6964223A3133382C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A226E616D65222C2267756964223A223334623835346261393436343465366361356230613066656634613166613938222C227374617465223A332C22737562747970655F6F665F7862745F6964223A31347D2C227862744D616E6966657374223A7B227862745F6964223A3133382C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393839342C22707265765F7862745F6D616E69666573745F6964223A393938362C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000'),
    ('12fe0ac5ce69f7ec77525c00731ad858', 1, 1, 'Added', 'f8f4e42af2d0439c9905e746c3fd58a0', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A4E4F544154494F4E222C22737562547970654F66586274223A7B226E616D65223A22616E792073696D706C652074797065222C2267756964223A223731313434343131393830353461643738643266636639626364616232636266227D2C226E616D65223A226E6F746174696F6E222C2267756964223A226638663465343261663264303433396339393035653734366333666435386130222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A4E4F544154494F4E222C226176726F5F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227862745F6964223A3133372C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A226E6F746174696F6E222C2267756964223A226638663465343261663264303433396339393035653734366333666435386130222C227374617465223A332C22737562747970655F6F665F7862745F6964223A327D2C227862744D616E6966657374223A7B227862745F6964223A3133372C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393839332C22707265765F7862745F6D616E69666573745F6964223A393938352C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000'),
    ('192b47453fd56629350b1048cd005b4a', 1, 1, 'Added', '3b2596f3c253432b9e5149a55d79d88c', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A514E616D65222C22737562547970654F66586274223A7B226E616D65223A22616E792073696D706C652074797065222C2267756964223A223731313434343131393830353461643738643266636639626364616232636266227D2C226E616D65223A227175616C6966696564206E616D65222C2267756964223A223362323539366633633235333433326239653531343961353564373964383863222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A514E616D65222C226176726F5F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C22737472696E675C227D222C227862745F6964223A3133362C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A227175616C6966696564206E616D65222C2267756964223A223362323539366633633235333433326239653531343961353564373964383863222C227374617465223A332C22737562747970655F6F665F7862745F6964223A327D2C227862744D616E6966657374223A7B227862745F6964223A3133362C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393839322C22707265765F7862745F6D616E69666573745F6964223A393938342C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000'),
    ('7c4e2e442ff7ea55f2bbd5fc3f471eea', 1, 1, 'Added', 'b240df3d59cd4c949c972eb2d41b64d3', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C226E756D6265725C222C205C226D756C7469706C654F665C223A312C205C226D696E696D756D5C223A302C205C226D6178696D756D5C223A3235357D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A756E7369676E656442797465222C22737562547970654F66586274223A7B226E616D65223A22756E7369676E65642073686F7274222C2267756964223A226364313662393837336165663464363238366635663032313561643239373465227D2C226E616D65223A22756E7369676E65642062797465222C2267756964223A226232343064663364353963643463393439633937326562326434316236346433222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22696E74656765725C222C205C226D696E696D756D5C223A302C205C226D6178696D756D5C223A3235357D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22696E74656765725C222C205C226D696E696D756D5C223A302C205C226D6178696D756D5C223A3235357D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A756E7369676E656442797465222C226176726F5F6D6170223A227B5C22747970655C223A5C22696E745C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C226E756D6265725C222C205C226D756C7469706C654F665C223A312C205C226D696E696D756D5C223A302C205C226D6178696D756D5C223A3235357D222C227862745F6964223A3133352C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A22756E7369676E65642062797465222C2267756964223A226232343064663364353963643463393439633937326562326434316236346433222C227374617465223A332C22737562747970655F6F665F7862745F6964223A3133347D2C227862744D616E6966657374223A7B227862745F6964223A3133352C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393839312C22707265765F7862745F6D616E69666573745F6964223A393938332C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000'),
    ('bf127a5d52ca6e9941dda27291d3eb0e', 1, 1, 'Added', 'cd16b9873aef4d6286f5f0215ad2974e', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C226E756D6265725C222C205C226D756C7469706C654F665C223A312C205C226D696E696D756D5C223A302C205C226D6178696D756D5C223A36353533357D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A756E7369676E656453686F7274222C22737562547970654F66586274223A7B226E616D65223A22756E7369676E656420696E74222C2267756964223A226662653530643066666437613464646661636135303433313265616233656235227D2C226E616D65223A22756E7369676E65642073686F7274222C2267756964223A226364313662393837336165663464363238366635663032313561643239373465222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22696E74656765725C222C205C226D696E696D756D5C223A302C205C226D6178696D756D5C223A36353533357D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22696E74656765725C222C205C226D696E696D756D5C223A302C205C226D6178696D756D5C223A36353533357D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A756E7369676E656453686F7274222C226176726F5F6D6170223A227B5C22747970655C223A5C22696E745C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C226E756D6265725C222C205C226D756C7469706C654F665C223A312C205C226D696E696D756D5C223A302C205C226D6178696D756D5C223A36353533357D222C227862745F6964223A3133342C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A22756E7369676E65642073686F7274222C2267756964223A226364313662393837336165663464363238366635663032313561643239373465222C227374617465223A332C22737562747970655F6F665F7862745F6964223A3133337D2C227862744D616E6966657374223A7B227862745F6964223A3133342C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393839302C22707265765F7862745F6D616E69666573745F6964223A393938322C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000'),
    ('e0cff44351bbc95dfe3ea0a04e2fa88f', 1, 1, 'Added', 'fbe50d0ffd7a4ddfaca504312eab3eb5', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C226E756D6265725C222C205C226D756C7469706C654F665C223A312C205C226D696E696D756D5C223A302C205C226D6178696D756D5C223A343239343936373239357D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A756E7369676E6564496E74222C22737562547970654F66586274223A7B226E616D65223A22756E7369676E6564206C6F6E67222C2267756964223A223535623430396464306631393465346361613239303835663731373062303838227D2C226E616D65223A22756E7369676E656420696E74222C2267756964223A226662653530643066666437613464646661636135303433313265616233656235222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22696E74656765725C222C205C226D696E696D756D5C223A302C205C226D6178696D756D5C223A343239343936373239357D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22696E74656765725C222C205C226D696E696D756D5C223A302C205C226D6178696D756D5C223A343239343936373239357D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A756E7369676E6564496E74222C226176726F5F6D6170223A227B5C22747970655C223A5C22696E745C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C226E756D6265725C222C205C226D756C7469706C654F665C223A312C205C226D696E696D756D5C223A302C205C226D6178696D756D5C223A343239343936373239357D222C227862745F6964223A3133332C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A22756E7369676E656420696E74222C2267756964223A226662653530643066666437613464646661636135303433313265616233656235222C227374617465223A332C22737562747970655F6F665F7862745F6964223A3133327D2C227862744D616E6966657374223A7B227862745F6964223A3133332C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393838392C22707265765F7862745F6D616E69666573745F6964223A393938312C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000'),
    ('edd2a97f2b4b3976086a5698af698a5e', 1, 1, 'Added', '55b409dd0f194e4caa29085f7170b088', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C226E756D6265725C222C205C226D756C7469706C654F665C223A312C205C226D696E696D756D5C223A307D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A756E7369676E65644C6F6E67222C22737562547970654F66586274223A7B226E616D65223A226E6F6E206E6567617469766520696E7465676572222C2267756964223A226461383261363265366266643464623838373130343432613637333536666639227D2C226E616D65223A22756E7369676E6564206C6F6E67222C2267756964223A223535623430396464306631393465346361613239303835663731373062303838222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22696E74656765725C222C205C226D696E696D756D5C223A307D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22696E74656765725C222C205C226D696E696D756D5C223A307D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A756E7369676E65644C6F6E67222C226176726F5F6D6170223A227B5C22747970655C223A5C22696E745C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C226E756D6265725C222C205C226D756C7469706C654F665C223A312C205C226D696E696D756D5C223A307D222C227862745F6964223A3133322C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A22756E7369676E6564206C6F6E67222C2267756964223A223535623430396464306631393465346361613239303835663731373062303838222C227374617465223A332C22737562747970655F6F665F7862745F6964223A32327D2C227862744D616E6966657374223A7B227862745F6964223A3133322C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393838382C22707265765F7862745F6D616E69666573745F6964223A393938302C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000'),
    ('4308d7faf838677791fa44d903d0f79d', 1, 1, 'Added', '37ddb910e2184816a5f335e7b5f631e2', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C226E756D6265725C222C205C226D756C7469706C654F665C223A312C205C226D696E696D756D5C223A2D3132382C205C226D6178696D756D5C223A3132377D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A62797465222C22737562547970654F66586274223A7B226E616D65223A2273686F7274222C2267756964223A223731336338663338323966303466386562353239373730373362636432353064227D2C226E616D65223A2262797465222C2267756964223A223337646462393130653231383438313661356633333565376235663633316532222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22696E74656765725C222C205C226D696E696D756D5C223A2D3132382C205C226D6178696D756D5C223A3132377D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22696E74656765725C222C205C226D696E696D756D5C223A2D3132382C205C226D6178696D756D5C223A3132377D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A62797465222C226176726F5F6D6170223A227B5C22747970655C223A5C22696E745C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C226E756D6265725C222C205C226D756C7469706C654F665C223A312C205C226D696E696D756D5C223A2D3132382C205C226D6178696D756D5C223A3132377D222C227862745F6964223A3133312C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A2262797465222C2267756964223A223337646462393130653231383438313661356633333565376235663633316532222C227374617465223A332C22737562747970655F6F665F7862745F6964223A3133307D2C227862744D616E6966657374223A7B227862745F6964223A3133312C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393838372C22707265765F7862745F6D616E69666573745F6964223A393937392C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000'),
    ('15e89f76acc39eef87fe3fc37757bff4', 1, 1, 'Added', '713c8f3829f04f8eb52977073bcd250d', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C226E756D6265725C222C205C226D756C7469706C654F665C223A312C205C226D696E696D756D5C223A2D33323736382C205C226D6178696D756D5C223A33323736377D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A73686F7274222C22737562547970654F66586274223A7B226E616D65223A22696E74222C2267756964223A226265376336386132303435313462383961383536363164636238656239323736227D2C226E616D65223A2273686F7274222C2267756964223A223731336338663338323966303466386562353239373730373362636432353064222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22696E74656765725C222C205C226D696E696D756D5C223A2D33323736382C205C226D6178696D756D5C223A33323736377D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22696E74656765725C222C205C226D696E696D756D5C223A2D33323736382C205C226D6178696D756D5C223A33323736377D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A73686F7274222C226176726F5F6D6170223A227B5C22747970655C223A5C22696E745C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C226E756D6265725C222C205C226D756C7469706C654F665C223A312C205C226D696E696D756D5C223A2D33323736382C205C226D6178696D756D5C223A33323736377D222C227862745F6964223A3133302C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A2273686F7274222C2267756964223A223731336338663338323966303466386562353239373730373362636432353064222C227374617465223A332C22737562747970655F6F665F7862745F6964223A3132397D2C227862744D616E6966657374223A7B227862745F6964223A3133302C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393838362C22707265765F7862745F6D616E69666573745F6964223A393937382C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000'),
    ('ce4c0ca1065abf6e41cbfe10a9f87059', 1, 1, 'Added', 'be7c68a204514b89a85661dcb8eb9276', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C226E756D6265725C222C205C226D756C7469706C654F665C223A312C205C226D696E696D756D5C223A2D323134373438333634382C205C226D6178696D756D5C223A323134373438333634377D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A696E74222C22737562547970654F66586274223A7B226E616D65223A226C6F6E67222C2267756964223A226630336666626239636466303439613238643764393461303862653433333938227D2C226E616D65223A22696E74222C2267756964223A226265376336386132303435313462383961383536363164636238656239323736222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22696E74656765725C222C205C226D696E696D756D5C223A2D323134373438333634382C205C226D6178696D756D5C223A323134373438333634377D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22696E74656765725C222C205C226D696E696D756D5C223A2D323134373438333634382C205C226D6178696D756D5C223A323134373438333634377D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A696E74222C226176726F5F6D6170223A227B5C22747970655C223A5C22696E745C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C226E756D6265725C222C205C226D756C7469706C654F665C223A312C205C226D696E696D756D5C223A2D323134373438333634382C205C226D6178696D756D5C223A323134373438333634377D222C227862745F6964223A3132392C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A22696E74222C2267756964223A226265376336386132303435313462383961383536363164636238656239323736222C227374617465223A332C22737562747970655F6F665F7862745F6964223A3132387D2C227862744D616E6966657374223A7B227862745F6964223A3132392C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393838352C22707265765F7862745F6D616E69666573745F6964223A393937372C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000'),
    ('b914512d2ccd26386c779273551ab1c3', 1, 1, 'Added', 'f03ffbb9cdf049a28d7d94a08be43398', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C226E756D6265725C222C205C226D756C7469706C654F665C223A317D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A6C6F6E67222C22737562547970654F66586274223A7B226E616D65223A22696E7465676572222C2267756964223A226633626336663363613434623437653961396136363939376663356433633262227D2C226E616D65223A226C6F6E67222C2267756964223A226630336666626239636466303439613238643764393461303862653433333938222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22696E74656765725C227D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22696E74656765725C227D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A6C6F6E67222C226176726F5F6D6170223A227B5C22747970655C223A5C22696E745C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C226E756D6265725C222C205C226D756C7469706C654F665C223A317D222C227862745F6964223A3132382C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A226C6F6E67222C2267756964223A226630336666626239636466303439613238643764393461303862653433333938222C227374617465223A332C22737562747970655F6F665F7862745F6964223A32317D2C227862744D616E6966657374223A7B227862745F6964223A3132382C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393838342C22707265765F7862745F6D616E69666573745F6964223A393937362C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000'),
    ('f80a3e45917304e094690130e6b1fb22', 1, 1, 'Added', 'df01213dc7a2416cbb1cee9c36417bd4', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C226E756D6265725C222C205C226D756C7469706C654F665C223A312C205C226D6178696D756D5C223A302C205C226578636C75736976654D6178696D756D5C223A747275657D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A6E65676174697665496E7465676572222C22737562547970654F66586274223A7B226E616D65223A226E6F6E20706F73697469766520696E7465676572222C2267756964223A226364653361363534346566303436373538663831666166643234313266626364227D2C226E616D65223A226E6567617469766520696E7465676572222C2267756964223A226466303132313364633761323431366362623163656539633336343137626434222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22696E74656765725C222C205C226D6178696D756D5C223A302C205C226578636C75736976654D6178696D756D5C223A747275657D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22696E74656765725C222C205C226D6178696D756D5C223A302C205C226578636C75736976654D6178696D756D5C223A747275657D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A6E65676174697665496E7465676572222C226176726F5F6D6170223A227B5C22747970655C223A5C22696E745C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C226E756D6265725C222C205C226D756C7469706C654F665C223A312C205C226D6178696D756D5C223A302C205C226578636C75736976654D6178696D756D5C223A747275657D222C227862745F6964223A3132372C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A226E6567617469766520696E7465676572222C2267756964223A226466303132313364633761323431366362623163656539633336343137626434222C227374617465223A332C22737562747970655F6F665F7862745F6964223A3132367D2C227862744D616E6966657374223A7B227862745F6964223A3132372C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393838332C22707265765F7862745F6D616E69666573745F6964223A393937352C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000'),
    ('c7bc0b52de9ccd551ec04d6174dafe41', 1, 1, 'Added', 'cde3a6544ef046758f81fafd2412fbcd', X'7B2264657072656361746564223A66616C73652C226A6274447261667430354D6170223A227B5C22747970655C223A5C226E756D6265725C222C205C226D756C7469706C654F665C223A312C205C226D6178696D756D5C223A302C205C226578636C75736976654D6178696D756D5C223A66616C73657D222C22636F6D706F6E656E74223A22786274222C226F776E657255736572223A7B22726F6C6573223A5B22646576656C6F706572225D2C22757365726E616D65223A226F61676973227D2C226275696C74496E54797065223A227873643A6E6F6E506F736974697665496E7465676572222C22737562547970654F66586274223A7B226E616D65223A22696E7465676572222C2267756964223A226633626336663363613434623437653961396136363939376663356433633262227D2C226E616D65223A226E6F6E20706F73697469766520696E7465676572222C2267756964223A226364653361363534346566303436373538663831666166643234313266626364222C226F70656E61706933304D6170223A227B5C22747970655C223A5C22696E74656765725C222C205C226D6178696D756D5C223A302C205C226578636C75736976654D6178696D756D5C223A66616C73657D222C227374617465223A332C225F6D65746164617461223A7B22786274223A7B226F70656E61706933305F6D6170223A227B5C22747970655C223A5C22696E74656765725C222C205C226D6178696D756D5C223A302C205C226578636C75736976654D6178696D756D5C223A66616C73657D222C226F776E65725F757365725F6964223A312C226372656174696F6E5F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C226275696C74496E5F74797065223A227873643A6E6F6E506F736974697665496E7465676572222C226176726F5F6D6170223A227B5C22747970655C223A5C22696E745C227D222C22637265617465645F6279223A302C226A62745F647261667430355F6D6170223A227B5C22747970655C223A5C226E756D6265725C222C205C226D756C7469706C654F665C223A312C205C226D6178696D756D5C223A302C205C226578636C75736976654D6178696D756D5C223A66616C73657D222C227862745F6964223A3132362C226C6173745F757064617465645F6279223A302C226C6173745F7570646174655F74696D657374616D70223A22323031392D31302D30325431353A32383A32392E373432222C2269735F64657072656361746564223A66616C73652C226E616D65223A226E6F6E20706F73697469766520696E7465676572222C2267756964223A226364653361363534346566303436373538663831666166643234313266626364222C227374617465223A332C22737562747970655F6F665F7862745F6964223A32317D2C227862744D616E6966657374223A7B227862745F6964223A3132362C2272656C656173655F6964223A34362C227862745F6D616E69666573745F6964223A393838322C22707265765F7862745F6D616E69666573745F6964223A393937342C22636F6E666C696374223A66616C73657D7D7D', NULL, NULL, 0, '2019-10-02 15:28:29.742000');

INSERT INTO `xbt_manifest` (`release_id`, `xbt_id`, `conflict`, `log_id`, `prev_xbt_manifest_id`,
                            `next_xbt_manifest_id`)
SELECT `release`.`release_id`,
       `xbt`.`xbt_id`,
       0,
       IF(`xbt_id` < 126, 10437 + `xbt_id`, NULL),
       NULL,
       NULL
FROM `release`,
     `xbt`
WHERE `release`.`library_id` IN (1, 2)
  AND `xbt`.`builtin_type` LIKE 'xsd%'
ORDER BY `release`.`release_id`, `xbt`.`xbt_id`;

UPDATE `xbt_manifest`, `xbt`, `log`
SET `xbt_manifest`.`log_id` = `log`.`log_id`
WHERE `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id` AND `xbt_manifest`.`xbt_id` >= 126 AND `log`.`reference` = `xbt`.`guid`;

UPDATE `xbt_manifest` AS xbt_31, `xbt_manifest` AS working
SET `working`.`prev_xbt_manifest_id` = `xbt_31`.`xbt_manifest_id`,
    `xbt_31`.`next_xbt_manifest_id` = `working`.`xbt_manifest_id`
WHERE
    `xbt_31`.`release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1') AND
    `working`.`release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = 'Working') AND
    `xbt_31`.`xbt_id` = `working`.`xbt_id`;

UPDATE `xbt_manifest` AS xbt_30, `xbt_manifest` AS xbt_31
SET `xbt_31`.`prev_xbt_manifest_id` = `xbt_30`.`xbt_manifest_id`,
    `xbt_30`.`next_xbt_manifest_id` = `xbt_31`.`xbt_manifest_id`
WHERE
    `xbt_30`.`release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.0') AND
    `xbt_31`.`release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 1 AND `release_num` = '3.1') AND
    `xbt_30`.`xbt_id` = `xbt_31`.`xbt_id`;

UPDATE `xbt_manifest` AS xbt_2014, `xbt_manifest` AS working
SET `working`.`prev_xbt_manifest_id` = `xbt_2014`.`xbt_manifest_id`,
    `xbt_2014`.`next_xbt_manifest_id` = `working`.`xbt_manifest_id`
WHERE
    `xbt_2014`.`release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = '2014') AND
    `working`.`release_id` = (SELECT `release_id` FROM `release` WHERE `library_id` = 2 AND `release_num` = 'Working') AND
    `xbt_2014`.`xbt_id` = `working`.`xbt_id`;

ALTER TABLE `xbt_manifest` ADD COLUMN `cdt_pri_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key referencing the CDT_PRI table. Specifies how the current record maps to allowed primitives in CDT.' AFTER `xbt_id`;
ALTER TABLE `xbt_manifest` ADD CONSTRAINT `xbt_manifest_cdt_pri_id_fk` FOREIGN KEY (`cdt_pri_id`) REFERENCES `cdt_pri` (`cdt_pri_id`);

UPDATE `xbt_manifest`, `xbt`, `cdt_pri`
SET `xbt_manifest`.`cdt_pri_id` = `cdt_pri`.`cdt_pri_id`
WHERE `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
  AND `xbt`.`name` IN ('base64 binary', 'hex binary')
  AND `cdt_pri`.`name` = 'Binary';

UPDATE `xbt_manifest`, `xbt`, `cdt_pri`
SET `xbt_manifest`.`cdt_pri_id` = `cdt_pri`.`cdt_pri_id`
WHERE `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
  AND `xbt`.`name` IN ('boolean', 'xbt boolean')
  AND `cdt_pri`.`name` = 'Boolean';

UPDATE `xbt_manifest`, `xbt`, `cdt_pri`
SET `xbt_manifest`.`cdt_pri_id` = `cdt_pri`.`cdt_pri_id`
WHERE `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
  AND `xbt`.`name` IN ('decimal')
  AND `cdt_pri`.`name` = 'Decimal';

UPDATE `xbt_manifest`, `xbt`, `cdt_pri`
SET `xbt_manifest`.`cdt_pri_id` = `cdt_pri`.`cdt_pri_id`
WHERE `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
  AND `xbt`.`name` IN ('double')
  AND `cdt_pri`.`name` = 'Double';

UPDATE `xbt_manifest`, `xbt`, `cdt_pri`
SET `xbt_manifest`.`cdt_pri_id` = `cdt_pri`.`cdt_pri_id`
WHERE `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
  AND `xbt`.`name` IN ('float')
  AND `cdt_pri`.`name` = 'Float';

UPDATE `xbt_manifest`, `xbt`, `cdt_pri`
SET `xbt_manifest`.`cdt_pri_id` = `cdt_pri`.`cdt_pri_id`
WHERE `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
    'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
  AND `cdt_pri`.`name` = 'Integer';

UPDATE `xbt_manifest`, `xbt`, `cdt_pri`
SET `xbt_manifest`.`cdt_pri_id` = `cdt_pri`.`cdt_pri_id`
WHERE `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
  AND `xbt`.`name` IN ('normalized string')
  AND `cdt_pri`.`name` = 'NormalizedString';

UPDATE `xbt_manifest`, `xbt`, `cdt_pri`
SET `xbt_manifest`.`cdt_pri_id` = `cdt_pri`.`cdt_pri_id`
WHERE `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
  AND `xbt`.`name` IN ('string', 'any URI', 'qualified name', 'notation')
  AND `cdt_pri`.`name` = 'String';

UPDATE `xbt_manifest`, `xbt`, `cdt_pri`
SET `xbt_manifest`.`cdt_pri_id` = `cdt_pri`.`cdt_pri_id`
WHERE `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
  AND `xbt`.`name` IN ('duration', 'xbt week duration')
  AND `cdt_pri`.`name` = 'TimeDuration';

UPDATE `xbt_manifest`, `xbt`, `cdt_pri`
SET `xbt_manifest`.`cdt_pri_id` = `cdt_pri`.`cdt_pri_id`
WHERE `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
  AND `xbt`.`name` IN ('date time', 'time', 'date', 'gregorian year month', 'gregorian year', 'gregorian month day',
    'gregorian day', 'gregorian month',
    'xbt century', 'xbt date', 'xbt day of week', 'xbt day of year', 'xbt day', 'xbt month day', 'xbt month',
    'xbt week', 'xbt week day', 'xbt year day', 'xbt year month', 'xbt year', 'xbt year week', 'xbt year week day',
    'xbt hour minute', 'xbt hour minute UTC', 'xbt hour minute UTC offset', 'xbt hour', 'xbt hour UTC',
    'xbt hour UTC offset', 'xbt minute', 'xbt minute second', 'xbt second', 'xbt time', 'xbt time UTC',
    'xbt time UTC offset', 'xbt date hour minute', 'xbt date hour minute UTC', 'xbt date hour minute UTC offset',
    'xbt date hour', 'xbt date hour UTC', 'xbt date hour UTC offset', 'xbt date time', 'xbt date time UTC',
    'xbt date time UTC offset', 'xbt day hour minute', 'xbt day hour minute UTC', 'xbt day hour minute UTC offset',
    'xbt day hour', 'xbt day hour UTC', 'xbt day hour UTC offset', 'xbt day of week hour minute',
    'xbt day of week hour minute UTC', 'xbt day of week hour minute UTC offset', 'xbt day of week hour',
    'xbt day of week hour UTC', 'xbt day of week hour UTC offset', 'xbt day of week time', 'xbt day of week time UTC',
    'xbt day of week time UTC offset', 'xbt day of year hour minute', 'xbt day of year hour minute UTC',
    'xbt day of year hour minute UTC offset', 'xbt day of year hour', 'xbt day of year hour UTC',
    'xbt day of year hour UTC offset', 'xbt day of year time', 'xbt day of year time UTC',
    'xbt day of year time UTC offset', 'xbt day time', 'xbt day time UTC', 'xbt day time UTC offset',
    'xbt month day hour minute', 'xbt month day hour minute UTC', 'xbt month day hour minute UTC offset',
    'xbt month day hour', 'xbt month day hour UTC', 'xbt month day hour UTC offset', 'xbt month day time',
    'xbt month day time UTC', 'xbt month day time UTC offset', 'xbt week day hour minute',
    'xbt week day hour minute UTC', 'xbt week day hour minute UTC offset', 'xbt week day hour',
    'xbt week day hour UTC', 'xbt week day hour UTC offset', 'xbt week day time', 'xbt week day time UTC',
    'xbt week day time UTC offset', 'xbt year day hour minute', 'xbt year day hour minute UTC',
    'xbt year day hour minute UTC offset', 'xbt year day hour', 'xbt year day hour UTC',
    'xbt year day hour UTC offset', 'xbt year day time', 'xbt year day time UTC', 'xbt year day time UTC offset',
    'xbt year week day hour minute', 'xbt year week day hour minute UTC', 'xbt year week day hour minute UTC offset',
    'xbt year week day hour', 'xbt year week day hour UTC', 'xbt year week day hour UTC offset',
    'xbt year week day time', 'xbt year week day time UTC', 'xbt year week day time UTC offset')
  AND `cdt_pri`.`name` = 'TimePoint';

UPDATE `xbt_manifest`, `xbt`, `cdt_pri`
SET `xbt_manifest`.`cdt_pri_id` = `cdt_pri`.`cdt_pri_id`
WHERE `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
  AND `xbt`.`name` IN ('token', 'language', 'name', 'name token', 'name tokens', 'non colonized name',
    'identifier', 'identifier reference', 'identifier references', 'entity', 'entities')
  AND `cdt_pri`.`name` = 'Token';

-- ---------------------------------------
-- Add DT_AWD_PRI and DT_SC_AWD_PRI tables
-- ---------------------------------------
CREATE TABLE `dt_awd_pri`
(
    `dt_awd_pri_id`              bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
    `release_id`                 bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table.',
    `dt_id`                      bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the DT table.',
    `xbt_manifest_id`            bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the XBT_MANIFEST table.',
    `code_list_manifest_id`      bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the CODE_LIST_MANIFEST table.',
    `agency_id_list_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the AGENCY_ID_LIST_MANIFEST table.',
    `is_default`                 tinyint(1) NOT NULL DEFAULT 0 COMMENT 'It indicates the most generic primitive for the data type.',
    PRIMARY KEY (`dt_awd_pri_id`),
    KEY                          `dt_awd_pri_release_id_fk` (`release_id`),
    KEY                          `dt_awd_pri_dt_id_fk` (`dt_id`),
    KEY                          `dt_awd_pri_xbt_manifest_id_fk` (`xbt_manifest_id`),
    KEY                          `dt_awd_pri_code_list_manifest_id_fk` (`code_list_manifest_id`),
    KEY                          `dt_awd_pri_agency_id_list_manifest_id_fk` (`agency_id_list_manifest_id`),
    CONSTRAINT `dt_awd_pri_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
    CONSTRAINT `dt_awd_pri_dt_id_fk` FOREIGN KEY (`dt_id`) REFERENCES `dt` (`dt_id`),
    CONSTRAINT `dt_awd_pri_xbt_manifest_id_fk` FOREIGN KEY (`xbt_manifest_id`) REFERENCES `xbt_manifest` (`xbt_manifest_id`),
    CONSTRAINT `dt_awd_pri_code_list_manifest_id_fk` FOREIGN KEY (`code_list_manifest_id`) REFERENCES `code_list_manifest` (`code_list_manifest_id`),
    CONSTRAINT `dt_awd_pri_agency_id_list_manifest_id_fk` FOREIGN KEY (`agency_id_list_manifest_id`) REFERENCES `agency_id_list_manifest` (`agency_id_list_manifest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This table captures the allowed primitives for a DT. The allowed primitives are captured by three columns the XBT_MANIFEST_ID, CODE_LIST_MANIFEST_ID, and AGENCY_ID_LIST_MANIFEST_ID. The first column specifies the primitive by the built-in type of an expression language such as the XML Schema built-in type. The second specifies the primitive, which is a code list, while the last one specifies the primitive which is an agency identification list. Only one column among the three can have a value in a particular record.';

-- CCTS Data Type Catalogue v3

-- Amount. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
         JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
         JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
         JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
         JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Amount'
  AND `cdt_pri`.`name` = 'Decimal'
  AND `xbt`.`name` IN ('decimal')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Amount'
  AND `cdt_pri`.`name` = 'Double'
  AND `xbt`.`name` IN ('double')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Amount'
  AND `cdt_pri`.`name` = 'Float'
  AND `xbt`.`name` IN ('float')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Amount'
  AND `cdt_pri`.`name` = 'Integer'
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
                       'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Binary Object. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'base64 binary', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
         JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
         JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
         JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
         JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Binary Object'
  AND `cdt_pri`.`name` = 'Binary'
  AND `xbt`.`name` IN ('base64 binary', 'hex binary')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Code. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
         JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
         JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
         JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
         JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Code'
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Code'
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string', 'any URI', 'qualified name', 'notation')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Code'
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token', 'language', 'name', 'non colonized name', 'name token', 'name tokens',
                       'identifier', 'identifier reference', 'identifier references', 'entity', 'entities')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Date. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'date', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
         JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
         JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
         JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
         JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Date'
  AND `cdt_pri`.`name` = 'TimePoint'
  AND `xbt`.`name` IN ('date time', 'time', 'date', 'gregorian year month', 'gregorian year',
                       'gregorian month day', 'gregorian day', 'gregorian month')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'date', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Date'
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'date', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Date'
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'date', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Date'
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Date Time. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'date time', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
         JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
         JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
         JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
         JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Date Time'
  AND `cdt_pri`.`name` = 'TimePoint'
  AND `xbt`.`name` IN ('date time', 'time', 'date', 'gregorian year month', 'gregorian year',
                       'gregorian month day', 'gregorian day', 'gregorian month')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'date time', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Date Time'
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'date time', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Date Time'
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'date time', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Date Time'
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Duration. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'duration', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
         JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
         JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
         JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
         JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Duration'
  AND `cdt_pri`.`name` = 'TimeDuration'
  AND `xbt`.`name` IN ('duration')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'duration', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Duration'
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'duration', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Duration'
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'duration', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Duration'
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Graphic. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'base64 binary', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
         JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
         JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
         JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
         JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Graphic'
  AND `cdt_pri`.`name` = 'Binary'
  AND `xbt`.`name` IN ('base64 binary', 'hex binary')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Identifier. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
         JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
         JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
         JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
         JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Identifier'
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Identifier'
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string', 'any URI', 'qualified name', 'notation')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Identifier'
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token', 'language', 'name', 'non colonized name', 'name token', 'name tokens',
                       'identifier', 'identifier reference', 'identifier references', 'entity', 'entities')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Identifier'
  AND `cdt_pri`.`name` = 'Integer'
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
                       'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Indicator. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'boolean', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
         JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
         JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
         JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
         JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Indicator'
  AND `cdt_pri`.`name` = 'Boolean'
  AND `xbt`.`name` IN ('boolean')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'boolean', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Indicator'
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'boolean', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Indicator'
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'boolean', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Indicator'
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'boolean', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Indicator'
  AND `cdt_pri`.`name` = 'Integer'
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
                       'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Measure. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Measure'
  AND `cdt_pri`.`name` = 'Decimal'
  AND `xbt`.`name` IN ('decimal')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Measure'
  AND `cdt_pri`.`name` = 'Double'
  AND `xbt`.`name` IN ('double')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Measure'
  AND `cdt_pri`.`name` = 'Float'
  AND `xbt`.`name` IN ('float')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Measure'
  AND `cdt_pri`.`name` = 'Integer'
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
                       'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Name. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Name'
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Name'
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string', 'any URI', 'qualified name', 'notation')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Name'
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token', 'language', 'name', 'non colonized name', 'name token', 'name tokens',
                       'identifier', 'identifier reference', 'identifier references', 'entity', 'entities')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Number. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.1', 'Working')
  AND `dt`.`data_type_term` = 'Number'
  AND `cdt_pri`.`name` = 'Decimal'
  AND `xbt`.`name` IN ('decimal')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.1', 'Working')
  AND `dt`.`data_type_term` = 'Number'
  AND `cdt_pri`.`name` = 'Double'
  AND `xbt`.`name` IN ('double')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.1', 'Working')
  AND `dt`.`data_type_term` = 'Number'
  AND `cdt_pri`.`name` = 'Float'
  AND `xbt`.`name` IN ('float')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.1', 'Working')
  AND `dt`.`data_type_term` = 'Number'
  AND `cdt_pri`.`name` = 'Integer'
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
                       'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Ordinal. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'integer', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Ordinal'
  AND `cdt_pri`.`name` = 'Integer'
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
                       'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Percent. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Percent'
  AND `cdt_pri`.`name` = 'Decimal'
  AND `xbt`.`name` IN ('decimal')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Percent'
  AND `cdt_pri`.`name` = 'Double'
  AND `xbt`.`name` IN ('double')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Percent'
  AND `cdt_pri`.`name` = 'Float'
  AND `xbt`.`name` IN ('float')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Percent'
  AND `cdt_pri`.`name` = 'Integer'
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
                       'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Picture. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'base64 binary', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
         JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
         JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
         JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
         JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Picture'
  AND `cdt_pri`.`name` = 'Binary'
  AND `xbt`.`name` IN ('base64 binary', 'hex binary')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Quantity. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Quantity'
  AND `cdt_pri`.`name` = 'Decimal'
  AND `xbt`.`name` IN ('decimal')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Quantity'
  AND `cdt_pri`.`name` = 'Double'
  AND `xbt`.`name` IN ('double')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Quantity'
  AND `cdt_pri`.`name` = 'Float'
  AND `xbt`.`name` IN ('float')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Quantity'
  AND `cdt_pri`.`name` = 'Integer'
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
                       'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Rate. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Rate'
  AND `cdt_pri`.`name` = 'Decimal'
  AND `xbt`.`name` IN ('decimal')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Rate'
  AND `cdt_pri`.`name` = 'Double'
  AND `xbt`.`name` IN ('double')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Rate'
  AND `cdt_pri`.`name` = 'Float'
  AND `xbt`.`name` IN ('float')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Rate'
  AND `cdt_pri`.`name` = 'Integer'
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
                       'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Ratio. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Ratio'
  AND `cdt_pri`.`name` = 'Decimal'
  AND `xbt`.`name` IN ('decimal')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Ratio'
  AND `cdt_pri`.`name` = 'Double'
  AND `xbt`.`name` IN ('double')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Ratio'
  AND `cdt_pri`.`name` = 'Float'
  AND `xbt`.`name` IN ('float')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Ratio'
  AND `cdt_pri`.`name` = 'Integer'
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
                       'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Ratio'
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Ratio'
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Ratio'
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Sound. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'base64 binary', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
         JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
         JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
         JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
         JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Sound'
  AND `cdt_pri`.`name` = 'Binary'
  AND `xbt`.`name` IN ('base64 binary', 'hex binary')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Text. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'string', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Text'
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'string', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Text'
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string', 'any URI', 'qualified name', 'notation')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'string', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Text'
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token', 'language', 'name', 'non colonized name', 'name token', 'name tokens',
                       'identifier', 'identifier reference', 'identifier references', 'entity', 'entities')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Time. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'time', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Time'
  AND `cdt_pri`.`name` = 'TimePoint'
  AND `xbt`.`name` IN ('date time', 'time', 'date', 'gregorian year month', 'gregorian year',
                       'gregorian month day', 'gregorian day', 'gregorian month')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'time', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Time'
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'time', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Time'
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'time', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Time'
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Value. Type

-- 3.0

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` = '3.0'
  AND `dt`.`data_type_term` = 'Value'
  AND `cdt_pri`.`name` = 'Decimal'
  AND `xbt`.`name` IN ('decimal')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` = '3.0'
  AND `dt`.`data_type_term` = 'Value'
  AND `cdt_pri`.`name` = 'Double'
  AND `xbt`.`name` IN ('double')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` = '3.0'
  AND `dt`.`data_type_term` = 'Value'
  AND `cdt_pri`.`name` = 'Float'
  AND `xbt`.`name` IN ('float')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` = '3.0'
  AND `dt`.`data_type_term` = 'Value'
  AND `cdt_pri`.`name` = 'Integer'
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
                       'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- 3.1

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.1', 'Working')
  AND `dt`.`data_type_term` = 'Value'
  AND `cdt_pri`.`name` = 'Decimal'
  AND `xbt`.`name` IN ('decimal')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.1', 'Working')
  AND `dt`.`data_type_term` = 'Value'
  AND `cdt_pri`.`name` = 'Double'
  AND `xbt`.`name` IN ('double')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.1', 'Working')
  AND `dt`.`data_type_term` = 'Value'
  AND `cdt_pri`.`name` = 'Float'
  AND `xbt`.`name` IN ('float')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.1', 'Working')
  AND `dt`.`data_type_term` = 'Value'
  AND `cdt_pri`.`name` = 'Integer'
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
                       'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Value'
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Value'
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string', 'any URI', 'qualified name', 'notation')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Value'
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token', 'language', 'name', 'non colonized name', 'name token', 'name tokens',
                       'identifier', 'identifier reference', 'identifier references', 'entity', 'entities')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Video. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'base64 binary', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
         JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
         JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
         JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
         JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt`.`data_type_term` = 'Video'
  AND `cdt_pri`.`name` = 'Binary'
  AND `xbt`.`name` IN ('base64 binary', 'hex binary')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- ISO 15000-5

-- Amount. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Amount'
  AND `cdt_pri`.`name` = 'Decimal'
  AND `xbt`.`name` IN ('decimal')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Amount'
  AND `cdt_pri`.`name` = 'Double'
  AND `xbt`.`name` IN ('double')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Amount'
  AND `cdt_pri`.`name` = 'Float'
  AND `xbt`.`name` IN ('float')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Amount'
  AND `cdt_pri`.`name` = 'Integer'
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
                       'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Binary Object. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'base64 binary', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
         JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
         JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
         JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
         JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Binary Object'
  AND `cdt_pri`.`name` = 'Binary'
  AND `xbt`.`name` IN ('base64 binary', 'hex binary')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Code. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Code'
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Code'
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string', 'any URI', 'qualified name', 'notation')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Code'
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token', 'language', 'name', 'non colonized name', 'name token', 'name tokens',
                       'identifier', 'identifier reference', 'identifier references', 'entity', 'entities')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Date Time. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'date time', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Date Time'
  AND `cdt_pri`.`name` = 'TimePoint'
  AND `xbt`.`name` IN ('date time', 'time', 'date', 'gregorian year month', 'gregorian year',
                       'gregorian month day', 'gregorian day', 'gregorian month')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'date time', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Date Time'
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'date time', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Date Time'
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'date time', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Date Time'
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Identifier. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Identifier'
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Identifier'
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string', 'any URI', 'qualified name', 'notation')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Identifier'
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token', 'language', 'name', 'non colonized name', 'name token', 'name tokens',
                       'identifier', 'identifier reference', 'identifier references', 'entity', 'entities')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Identifier'
  AND `cdt_pri`.`name` = 'Integer'
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
                       'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Indicator. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'boolean', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Indicator'
  AND `cdt_pri`.`name` = 'Boolean'
  AND `xbt`.`name` IN ('boolean')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'boolean', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Indicator'
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'boolean', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Indicator'
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'boolean', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Indicator'
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'boolean', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Indicator'
  AND `cdt_pri`.`name` = 'Integer'
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
                       'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Measure. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Measure'
  AND `cdt_pri`.`name` = 'Decimal'
  AND `xbt`.`name` IN ('decimal')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Measure'
  AND `cdt_pri`.`name` = 'Double'
  AND `xbt`.`name` IN ('double')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Measure'
  AND `cdt_pri`.`name` = 'Float'
  AND `xbt`.`name` IN ('float')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Measure'
  AND `cdt_pri`.`name` = 'Integer'
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
                       'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Numeric. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Numeric'
  AND `cdt_pri`.`name` = 'Decimal'
  AND `xbt`.`name` IN ('decimal')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Numeric'
  AND `cdt_pri`.`name` = 'Double'
  AND `xbt`.`name` IN ('double')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Numeric'
  AND `cdt_pri`.`name` = 'Float'
  AND `xbt`.`name` IN ('float')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Numeric'
  AND `cdt_pri`.`name` = 'Integer'
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
                       'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Quantity. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Quantity'
  AND `cdt_pri`.`name` = 'Decimal'
  AND `xbt`.`name` IN ('decimal')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Quantity'
  AND `cdt_pri`.`name` = 'Double'
  AND `xbt`.`name` IN ('double')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Quantity'
  AND `cdt_pri`.`name` = 'Float'
  AND `xbt`.`name` IN ('float')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Quantity'
  AND `cdt_pri`.`name` = 'Integer'
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
                       'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- Text. Type

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'string', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Text'
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'string', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Text'
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string', 'any URI', 'qualified name', 'notation')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'string', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_manifest`
                    JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
                    JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `dt`.`data_type_term` = 'Text'
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token', 'language', 'name', 'non colonized name', 'name token', 'name tokens',
                       'identifier', 'identifier reference', 'identifier references', 'entity', 'entities')
ORDER BY `release`.`release_id`, `dt`.`dt_id`, `xbt_manifest`.`xbt_manifest_id`;

-- DT_SC_AWD_PRI

CREATE TABLE `dt_sc_awd_pri`
(
    `dt_sc_awd_pri_id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
    `release_id`                 bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table.',
    `dt_sc_id`                   bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the DT_SC table.',
    `xbt_manifest_id`            bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the XBT_MANIFEST table.',
    `code_list_manifest_id`      bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the CODE_LIST_MANIFEST table.',
    `agency_id_list_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the AGENCY_ID_LIST_MANIFEST table.',
    `is_default`                 tinyint(1) NOT NULL DEFAULT 0 COMMENT 'It indicates the most generic primitive for the data type.',
    PRIMARY KEY (`dt_sc_awd_pri_id`),
    KEY                          `dt_sc_awd_pri_release_id_fk` (`release_id`),
    KEY                          `dt_sc_awd_pri_dt_id_fk` (`dt_sc_id`),
    KEY                          `dt_sc_awd_pri_xbt_manifest_id_fk` (`xbt_manifest_id`),
    KEY                          `dt_sc_awd_pri_code_list_manifest_id_fk` (`code_list_manifest_id`),
    KEY                          `dt_sc_awd_pri_agency_id_list_manifest_id_fk` (`agency_id_list_manifest_id`),
    CONSTRAINT `dt_sc_awd_pri_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
    CONSTRAINT `dt_sc_awd_pri_dt_id_fk` FOREIGN KEY (`dt_sc_id`) REFERENCES `dt_sc` (`dt_sc_id`),
    CONSTRAINT `dt_sc_awd_pri_xbt_manifest_id_fk` FOREIGN KEY (`xbt_manifest_id`) REFERENCES `xbt_manifest` (`xbt_manifest_id`),
    CONSTRAINT `dt_sc_awd_pri_code_list_manifest_id_fk` FOREIGN KEY (`code_list_manifest_id`) REFERENCES `code_list_manifest` (`code_list_manifest_id`),
    CONSTRAINT `dt_sc_awd_pri_agency_id_list_manifest_id_fk` FOREIGN KEY (`agency_id_list_manifest_id`) REFERENCES `agency_id_list_manifest` (`agency_id_list_manifest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This table captures the allowed primitives for a DT_SC. The allowed primitives are captured by three columns the XBT_MANIFEST_ID, CODE_LIST_MANIFEST_ID, and AGENCY_ID_LIST_MANIFEST_ID. The first column specifies the primitive by the built-in type of an expression language such as the XML Schema built-in type. The second specifies the primitive, which is a code list, while the last one specifies the primitive which is an agency identification list. Only one column among the three can have a value in a particular record.';

-- CCTS Data Type Catalogue v3

-- DT_SCs in Amount. Type, Binary Object. Type, and Code. Type

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt_sc`.`object_class_term` IN ('Amount', 'Binary Object', 'Code')
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt_sc`.`object_class_term` IN ('Amount', 'Binary Object', 'Code')
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string', 'any URI', 'qualified name', 'notation')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt_sc`.`object_class_term` IN ('Amount', 'Binary Object', 'Code')
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token', 'language', 'name', 'non colonized name', 'name token', 'name tokens',
                       'identifier', 'identifier reference', 'identifier references', 'entity', 'entities')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

-- DT_SCs in Date Time. Type

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt_sc`.`object_class_term` = 'Date Time'
  AND `dt_sc`.`property_term` = 'Time Zone'
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt_sc`.`object_class_term` = 'Date Time'
  AND `dt_sc`.`property_term` = 'Time Zone'
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string', 'any URI', 'qualified name', 'notation')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt_sc`.`object_class_term` = 'Date Time'
  AND `dt_sc`.`property_term` = 'Time Zone'
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token', 'language', 'name', 'non colonized name', 'name token', 'name tokens',
                       'identifier', 'identifier reference', 'identifier references', 'entity', 'entities')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'boolean', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.1', 'Working')
  AND `dt_sc`.`object_class_term` = 'Date Time'
  AND `dt_sc`.`property_term` = 'Daylight Saving'
  AND `cdt_pri`.`name` = 'Boolean'
  AND `xbt`.`name` IN ('boolean')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'boolean', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.1', 'Working')
  AND `dt_sc`.`object_class_term` = 'Date Time'
  AND `dt_sc`.`property_term` = 'Daylight Saving'
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'boolean', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.1', 'Working')
  AND `dt_sc`.`object_class_term` = 'Date Time'
  AND `dt_sc`.`property_term` = 'Daylight Saving'
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'boolean', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.1', 'Working')
  AND `dt_sc`.`object_class_term` = 'Date Time'
  AND `dt_sc`.`property_term` = 'Daylight Saving'
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'boolean', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.1', 'Working')
  AND `dt_sc`.`object_class_term` = 'Date Time'
  AND `dt_sc`.`property_term` = 'Daylight Saving'
  AND `cdt_pri`.`name` = 'Integer'
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
                       'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

-- DT_SCs in Graphic. Type, Identifier. Type, Measure. Type, Name. Type, Picture. Type, and Quantity. Type

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt_sc`.`object_class_term` IN ('Graphic', 'Identifier', 'Measure', 'Name', 'Picture', 'Quantity')
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt_sc`.`object_class_term` IN ('Graphic', 'Identifier', 'Measure', 'Name', 'Picture', 'Quantity')
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string', 'any URI', 'qualified name', 'notation')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt_sc`.`object_class_term` IN ('Graphic', 'Identifier', 'Measure', 'Name', 'Picture', 'Quantity')
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token', 'language', 'name', 'non colonized name', 'name token', 'name tokens',
                       'identifier', 'identifier reference', 'identifier references', 'entity', 'entities')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

-- DT_SCs in Rate. Type

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt_sc`.`object_class_term` = 'Rate'
  AND `dt_sc`.`property_term` IN ('Multiplier', 'Base Multiplier')
  AND `cdt_pri`.`name` = 'Decimal'
  AND `xbt`.`name` IN ('decimal')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt_sc`.`object_class_term` = 'Rate'
  AND `dt_sc`.`property_term` IN ('Multiplier', 'Base Multiplier')
  AND `cdt_pri`.`name` = 'Double'
  AND `xbt`.`name` IN ('double')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt_sc`.`object_class_term` = 'Rate'
  AND `dt_sc`.`property_term` IN ('Multiplier', 'Base Multiplier')
  AND `cdt_pri`.`name` = 'Float'
  AND `xbt`.`name` IN ('float')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'decimal', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt_sc`.`object_class_term` = 'Rate'
  AND `dt_sc`.`property_term` IN ('Multiplier', 'Base Multiplier')
  AND `cdt_pri`.`name` = 'Integer'
  AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
                       'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt_sc`.`object_class_term` = 'Rate'
  AND `dt_sc`.`property_term` IN ('Unit', 'Currency', 'Base Unit', 'Base Currency')
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt_sc`.`object_class_term` = 'Rate'
  AND `dt_sc`.`property_term` IN ('Unit', 'Currency', 'Base Unit', 'Base Currency')
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string', 'any URI', 'qualified name', 'notation')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt_sc`.`object_class_term` = 'Rate'
  AND `dt_sc`.`property_term` IN ('Unit', 'Currency', 'Base Unit', 'Base Currency')
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token', 'language', 'name', 'non colonized name', 'name token', 'name tokens',
                       'identifier', 'identifier reference', 'identifier references', 'entity', 'entities')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

-- DT_SCs in Sound. Type, Text. Type, and Video. Type

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt_sc`.`object_class_term` IN ('Sound', 'Text', 'Video')
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt_sc`.`object_class_term` IN ('Sound', 'Text', 'Video')
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string', 'any URI', 'qualified name', 'notation')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'token', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 1
  AND `release`.`release_num` IN ('3.0', '3.1', 'Working')
  AND `dt_sc`.`object_class_term` IN ('Sound', 'Text', 'Video')
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token', 'language', 'name', 'non colonized name', 'name token', 'name tokens',
                       'identifier', 'identifier reference', 'identifier references', 'entity', 'entities')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

-- ISO 15000-5

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'string', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `cdt_pri`.`name` = 'NormalizedString'
  AND `xbt`.`name` IN ('normalized string')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'string', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `cdt_pri`.`name` = 'String'
  AND `xbt`.`name` IN ('string', 'any URI', 'qualified name', 'notation')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `is_default`)
SELECT DISTINCT `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`, IF(`xbt`.`name` = 'string', 1, 0) AS `is_default`
FROM `cdt_pri`, `dt_sc_manifest`
                    JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
                    JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
                    JOIN `xbt_manifest` ON `release`.`release_id` = `xbt_manifest`.`release_id`
                    JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
WHERE `release`.`library_id` = 2
  AND `release`.`release_num` IN ('2014', 'Working')
  AND `cdt_pri`.`name` = 'Token'
  AND `xbt`.`name` IN ('token', 'language', 'name', 'non colonized name', 'name token', 'name tokens',
                       'identifier', 'identifier reference', 'identifier references', 'entity', 'entities')
ORDER BY `release`.`release_id`, `dt_sc`.`dt_sc_id`, `xbt_manifest`.`xbt_manifest_id`;

-- ----------------------
-- Add Release Dependency
-- ----------------------
DELETE FROM `dt_manifest`
WHERE `dt_manifest_id` IN (
    SELECT
        `dt_manifest`.`dt_manifest_id`
    FROM `dt_manifest`
             JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
    WHERE `release`.`library_id` = 3 AND ((`dt_id` >= 1 AND `dt_id` <= 23) OR `dt_id` = 596)
);

CREATE TABLE `release_dep`
(
    `release_dep_id`       bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
    `release_id`           bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointing to a release record.',
    `depend_on_release_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointing to dependent release records of the release specified in release_id.',
    PRIMARY KEY (`release_dep_id`),
    KEY                    `release_dep_release_id_fk` (`release_id`),
    KEY                    `release_dep_depend_on_release_id_fk` (`depend_on_release_id`),
    CONSTRAINT `release_dep_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
    CONSTRAINT `release_dep_depend_on_release_id_fk` FOREIGN KEY (`depend_on_release_id`) REFERENCES `release` (`release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This table stores release dependency information.';

-- Include CCTS Data Type Catalogue v3:3.1 to connectSpec
INSERT INTO `release_dep` (`release_id`, `depend_on_release_id`)
SELECT `release`.`release_id`, `depend_on_release`.`release_id`
FROM `release`, `release` AS `depend_on_release`
WHERE `release`.`library_id` = 3 AND `depend_on_release`.`library_id` = 1 AND `depend_on_release`.`release_num` = '3.1';

-- Include ISO 15000-5:2014 to connectSpec
-- INSERT INTO `release_dep` (`release_id`, `depend_on_release_id`)
-- SELECT `release`.`release_id`, `depend_on_release`.`release_id`
-- FROM `release`, `release` AS `depend_on_release`
-- WHERE `release`.`library_id` = 3 AND `depend_on_release`.`library_id` = 2 AND `depend_on_release`.`release_num` = '2014';

-- ----------------------------------------------------
-- ADD `xbt_manifest_id` references to BBIE and BBIE_SC
-- ----------------------------------------------------
ALTER TABLE `bbie` ADD COLUMN `xbt_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is the foreign key to the XBT_MANIFEST table. It indicates the primitive assigned to the BBIE (or also can be viewed as assigned to the BBIEP for this specific association). This is assigned by the user who authors the BIE. The assignment would override the default from the DT_AWD_PRI side.' AFTER `bdt_pri_restri_id`;
ALTER TABLE `bbie` ADD CONSTRAINT `bbie_xbt_manifest_id_fk` FOREIGN KEY (`xbt_manifest_id`) REFERENCES `xbt_manifest` (`xbt_manifest_id`);

UPDATE `bbie`, (
    SELECT
    `bbie`.`bbie_id`, `xbt_manifest`.`xbt_manifest_id`
    FROM `bbie`
    JOIN `top_level_asbiep` ON `bbie`.`owner_top_level_asbiep_id` = `top_level_asbiep`.`top_level_asbiep_id`
    JOIN `release` ON `top_level_asbiep`.`release_id` = `release`.`release_id`
    JOIN `bdt_pri_restri` ON `bbie`.`bdt_pri_restri_id` = `bdt_pri_restri`.`bdt_pri_restri_id`
    JOIN `cdt_awd_pri_xps_type_map` ON `bdt_pri_restri`.`cdt_awd_pri_xps_type_map_id` = `cdt_awd_pri_xps_type_map`.`cdt_awd_pri_xps_type_map_id`
    JOIN `xbt` ON `cdt_awd_pri_xps_type_map`.`xbt_id` = `xbt`.`xbt_id`
    JOIN `xbt_manifest` ON `xbt`.`xbt_id` = `xbt_manifest`.`xbt_id` AND `xbt_manifest`.`release_id` = `release`.`release_id`) t
SET `bbie`.`xbt_manifest_id` = t.`xbt_manifest_id`
WHERE `bbie`.`bbie_id` = t.`bbie_id`;

ALTER TABLE `bbie_sc` ADD COLUMN `xbt_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This must be one of the allowed primitive as specified in the corresponding SC of the based BCC of the BBIE (referred to by the BBIE_ID column).\n\nIt is the foreign key to the XBT_MANIFEST table. This is assigned by the user who authors the BIE. The assignment would override the default from the CC side.\n\nThis column, the CODE_LIST_ID column, and AGENCY_ID_LIST_ID column cannot have a value at the same time.' AFTER `dt_sc_pri_restri_id`;
ALTER TABLE `bbie_sc` ADD CONSTRAINT `bbie_sc_xbt_manifest_id_fk` FOREIGN KEY (`xbt_manifest_id`) REFERENCES `xbt_manifest` (`xbt_manifest_id`);

UPDATE `bbie_sc`, (
    SELECT
    `bbie_sc`.`bbie_sc_id`, `xbt_manifest`.`xbt_manifest_id`
    FROM `bbie_sc`
    JOIN `top_level_asbiep` ON `bbie_sc`.`owner_top_level_asbiep_id` = `top_level_asbiep`.`top_level_asbiep_id`
    JOIN `release` ON `top_level_asbiep`.`release_id` = `release`.`release_id`
    JOIN `bdt_sc_pri_restri` ON `bbie_sc`.`dt_sc_pri_restri_id` = `bdt_sc_pri_restri`.`bdt_sc_pri_restri_id`
    JOIN `cdt_sc_awd_pri_xps_type_map` ON `bdt_sc_pri_restri`.`cdt_sc_awd_pri_xps_type_map_id` = `cdt_sc_awd_pri_xps_type_map`.`cdt_sc_awd_pri_xps_type_map_id`
    JOIN `xbt` ON `cdt_sc_awd_pri_xps_type_map`.`xbt_id` = `xbt`.`xbt_id`
    JOIN `xbt_manifest` ON `xbt`.`xbt_id` = `xbt_manifest`.`xbt_id` AND `xbt_manifest`.`release_id` = `release`.`release_id`) t
SET `bbie_sc`.`xbt_manifest_id` = t.`xbt_manifest_id`
WHERE `bbie_sc`.`bbie_sc_id` = t.`bbie_sc_id`;

-- Clean Data
-- Delete merged ISO 15000-5 DT_SCs in connectSpec DTs.

DELETE
`bdt_sc_pri_restri` FROM `bdt_sc_pri_restri`, (
SELECT
`dt_sc_manifest`.`dt_sc_manifest_id`, `dt_sc`.*
FROM
`dt_sc` JOIN `dt_sc_manifest` ON `dt_sc`.`dt_sc_id` = `dt_sc_manifest`.`dt_sc_id`
JOIN (
SELECT
`dt_sc`.*
FROM
`dt_sc_manifest`
JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
WHERE
`release`.`library_id` = 2 AND `release`.`release_num` = '2014'
) t ON `dt_sc`.`object_class_term` = t.`object_class_term`
AND `dt_sc`.`property_term` = t.`property_term`
AND `dt_sc`.`representation_term` = t.`representation_term`
WHERE
`dt_sc`.`cardinality_max` = 0
) x
WHERE
`bdt_sc_pri_restri`.`bdt_sc_manifest_id` = x.`dt_sc_manifest_id`;

DELETE
`dt_sc_manifest` FROM `dt_sc_manifest`, (
SELECT
`dt_sc_manifest`.`dt_sc_manifest_id`, `dt_sc`.*
FROM
`dt_sc` JOIN `dt_sc_manifest` ON `dt_sc`.`dt_sc_id` = `dt_sc_manifest`.`dt_sc_id`
JOIN (
SELECT
`dt_sc`.*
FROM
`dt_sc_manifest`
JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
WHERE
`release`.`library_id` = 2 AND `release`.`release_num` = '2014'
) t ON `dt_sc`.`object_class_term` = t.`object_class_term`
AND `dt_sc`.`property_term` = t.`property_term`
AND `dt_sc`.`representation_term` = t.`representation_term`
WHERE
`dt_sc`.`cardinality_max` = 0
) x
WHERE
`dt_sc_manifest`.`dt_sc_manifest_id` = x.`dt_sc_manifest_id`;

DELETE
`dt_sc` FROM `dt_sc`, (
SELECT
`dt_sc`.*
FROM
`dt_sc`
JOIN (
SELECT
`dt_sc`.*
FROM
`dt_sc_manifest`
JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
WHERE
`release`.`library_id` = 2 AND `release`.`release_num` = '2014'
) t ON `dt_sc`.`object_class_term` = t.`object_class_term`
AND `dt_sc`.`property_term` = t.`property_term`
AND `dt_sc`.`representation_term` = t.`representation_term`
WHERE
`dt_sc`.`cardinality_max` = 0
) x
WHERE
`dt_sc`.`dt_sc_id` = x.`dt_sc_id`;

DELETE
`cdt_sc_awd_pri` FROM `cdt_sc_awd_pri`, (
SELECT
DISTINCT `cdt_sc_awd_pri`.*
FROM
`cdt_sc_awd_pri`
LEFT JOIN
`dt_sc` ON `cdt_sc_awd_pri`.`cdt_sc_id` = `dt_sc`.`dt_sc_id`
WHERE
`dt_sc`.`dt_sc_id` IS NULL) x
WHERE `cdt_sc_awd_pri`.`cdt_sc_awd_pri_id` = x.`cdt_sc_awd_pri_id`;

DELETE
`cdt_sc_awd_pri_xps_type_map` FROM `cdt_sc_awd_pri_xps_type_map`, (
SELECT
DISTINCT `cdt_sc_awd_pri_xps_type_map`.*
FROM
`cdt_sc_awd_pri_xps_type_map`
LEFT JOIN
`cdt_sc_awd_pri` ON `cdt_sc_awd_pri_xps_type_map`.`cdt_sc_awd_pri_id` = `cdt_sc_awd_pri`.`cdt_sc_awd_pri_id`
WHERE
`cdt_sc_awd_pri`.`cdt_sc_awd_pri_id` IS NULL) x
WHERE `cdt_sc_awd_pri_xps_type_map`.`cdt_sc_awd_pri_xps_type_map_id` = x.`cdt_sc_awd_pri_xps_type_map_id`;

UPDATE `dt_manifest`, (
    SELECT
    `connect_spec_dt_manifest`.`dt_manifest_id` AS `connect_spec_dt_manifest_id`, `ccts_cdt_manifest`.`dt_manifest_id` AS `ccts_cdt_manifest_id`
    FROM
    `dt_manifest` AS `connect_spec_dt_manifest`
    JOIN `dt` AS `connect_spec_dt` ON `connect_spec_dt_manifest`.`dt_id` = `connect_spec_dt`.`dt_id`
    JOIN `dt` AS `ccts_cdt` ON `connect_spec_dt`.`based_dt_id` = `ccts_cdt`.`dt_id`
    JOIN `dt_manifest` AS `ccts_cdt_manifest` ON `ccts_cdt`.`dt_id` = `ccts_cdt_manifest`.`dt_id`
    JOIN `release` AS `ccts_release` ON `ccts_cdt_manifest`.`release_id` = `ccts_release`.`release_id`
    WHERE
    `ccts_release`.`library_id` = 1 AND `ccts_release`.`release_num` = '3.1'
    AND `connect_spec_dt_manifest`.`based_dt_manifest_id` != `ccts_cdt_manifest`.`dt_manifest_id`
    ) x
SET `dt_manifest`.`based_dt_manifest_id` = x.`ccts_cdt_manifest_id`
WHERE `dt_manifest`.`dt_manifest_id` = x.`connect_spec_dt_manifest_id`;

UPDATE `dt_sc_manifest`, (
    SELECT
    `connect_spec_dt_sc_manifest`.`dt_sc_manifest_id` AS `connect_spec_dt_sc_manifest_id`, `ccts_cdt_sc_manifest`.`dt_sc_manifest_id` AS `ccts_cdt_sc_manifest_id`
    FROM
    `dt_sc_manifest` AS `connect_spec_dt_sc_manifest`
    JOIN `dt_sc` AS `connect_spec_dt_sc` ON `connect_spec_dt_sc_manifest`.`dt_sc_id` = `connect_spec_dt_sc`.`dt_sc_id`
    JOIN `dt_sc` AS `ccts_cdt_sc` ON `connect_spec_dt_sc`.`based_dt_sc_id` = `ccts_cdt_sc`.`dt_sc_id`
    JOIN `dt_sc_manifest` AS `ccts_cdt_sc_manifest` ON `ccts_cdt_sc`.`dt_sc_id` = `ccts_cdt_sc_manifest`.`dt_sc_id`
    JOIN `release` AS `ccts_release` ON `ccts_cdt_sc_manifest`.`release_id` = `ccts_release`.`release_id`
    WHERE
    `ccts_release`.`library_id` = 1 AND `ccts_release`.`release_num` = '3.1'
    AND `connect_spec_dt_sc_manifest`.`based_dt_sc_manifest_id` != `ccts_cdt_sc_manifest`.`dt_sc_manifest_id`
    ) x
SET `dt_sc_manifest`.`based_dt_sc_manifest_id` = x.`ccts_cdt_sc_manifest_id`
WHERE `dt_sc_manifest`.`dt_sc_manifest_id` = x.`connect_spec_dt_sc_manifest_id`;

-- Insert into `dt_awd_pri` for connectSpec BDTs
INSERT INTO `dt_awd_pri` (`release_id`, `dt_id`, `xbt_manifest_id`, `code_list_manifest_id`, `agency_id_list_manifest_id`, `is_default`)
SELECT DISTINCT `release_id`,
                `dt_id`,
                `xbt_manifest_id`,
                `code_list_manifest_id`,
                `agency_id_list_manifest_id`,
                `is_default`
FROM (SELECT `release`.`release_id`,
             `dt`.`dt_id`,
             `xbt_manifest`.`xbt_manifest_id`,
             NULL AS `code_list_manifest_id`,
             NULL AS `agency_id_list_manifest_id`,
             `bdt_pri_restri`.`is_default`
      FROM `dt_manifest`
               JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
               JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
               JOIN `bdt_pri_restri` ON `dt_manifest`.`dt_manifest_id` = `bdt_pri_restri`.`bdt_manifest_id`
               JOIN `cdt_awd_pri_xps_type_map` ON `bdt_pri_restri`.`cdt_awd_pri_xps_type_map_id` =
                                                  `cdt_awd_pri_xps_type_map`.`cdt_awd_pri_xps_type_map_id`
               JOIN `xbt` ON `cdt_awd_pri_xps_type_map`.`xbt_id` = `xbt`.`xbt_id`
               JOIN `xbt_manifest` ON `xbt`.`xbt_id` = `xbt_manifest`.`xbt_id` AND
                                      `xbt_manifest`.`release_id` = `dt_manifest`.`release_id`
               LEFT JOIN `dt_awd_pri` ON `dt_awd_pri`.`dt_id` = `dt_manifest`.`dt_id`
      WHERE `release`.`library_id` = 3
        AND `dt_awd_pri`.`dt_id` IS NULL

      UNION

      SELECT `release`.`release_id`,
             `dt`.`dt_id`,
             NULL AS `xbt_manifest_id`,
             `code_list_manifest`.`code_list_manifest_id`,
             NULL AS `agency_id_list_manifest_id`,
             `bdt_pri_restri`.`is_default`
      FROM `dt_manifest`
               JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
               JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
               JOIN `bdt_pri_restri` ON `dt_manifest`.`dt_manifest_id` = `bdt_pri_restri`.`bdt_manifest_id`
               JOIN `code_list_manifest`
                    ON `bdt_pri_restri`.`code_list_manifest_id` = `code_list_manifest`.`code_list_manifest_id` AND
                       `code_list_manifest`.`release_id` = `dt_manifest`.`release_id`
               JOIN `code_list` ON `code_list_manifest`.`code_list_id` = `code_list`.`code_list_id`
               LEFT JOIN `dt_awd_pri` ON `dt_awd_pri`.`dt_id` = `dt_manifest`.`dt_id`
      WHERE `release`.`library_id` = 3
        AND `dt_awd_pri`.`dt_id` IS NULL

      UNION

      SELECT `release`.`release_id`,
             `dt`.`dt_id`,
             NULL AS `xbt_manifest_id`,
             NULL AS `code_list_manifest_id`,
             `agency_id_list_manifest`.`agency_id_list_manifest_id`,
             `bdt_pri_restri`.`is_default`
      FROM `dt_manifest`
               JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id`
               JOIN `release` ON `dt_manifest`.`release_id` = `release`.`release_id`
               JOIN `bdt_pri_restri` ON `dt_manifest`.`dt_manifest_id` = `bdt_pri_restri`.`bdt_manifest_id`
               JOIN `agency_id_list_manifest` ON `bdt_pri_restri`.`agency_id_list_manifest_id` =
                                                 `agency_id_list_manifest`.`agency_id_list_manifest_id` AND
                                                 `agency_id_list_manifest`.`release_id` = `dt_manifest`.`release_id`
               JOIN `agency_id_list`
                    ON `agency_id_list_manifest`.`agency_id_list_id` = `agency_id_list`.`agency_id_list_id`
               LEFT JOIN `dt_awd_pri` ON `dt_awd_pri`.`dt_id` = `dt_manifest`.`dt_id`
      WHERE `release`.`library_id` = 3
        AND `dt_awd_pri`.`dt_id` IS NULL) t
ORDER BY `release_id`, `dt_id`, `xbt_manifest_id`, `code_list_manifest_id`, `agency_id_list_manifest_id`;

-- Update `cdt_pri_id` for connectSpec `dt_awd_pri`

-- UPDATE `dt_awd_pri`, (
--     SELECT `dt_awd_pri`.`dt_awd_pri_id`, `xbt`.`name`
--     FROM `dt_awd_pri`
--     JOIN `xbt_manifest` ON `dt_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('normalized string')) t
-- SET `dt_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'NormalizedString')
-- WHERE `dt_awd_pri`.`dt_awd_pri_id` = t.`dt_awd_pri_id`;
--
-- UPDATE `dt_awd_pri`, (
--     SELECT `dt_awd_pri`.`dt_awd_pri_id`, `xbt`.`name`
--     FROM `dt_awd_pri`
--     JOIN `xbt_manifest` ON `dt_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('string', 'any URI', 'qualified name', 'notation')) t
-- SET `dt_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'String')
-- WHERE `dt_awd_pri`.`dt_awd_pri_id` = t.`dt_awd_pri_id`;
--
-- UPDATE `dt_awd_pri`, (
--     SELECT `dt_awd_pri`.`dt_awd_pri_id`, `xbt`.`name`
--     FROM `dt_awd_pri`
--     JOIN `xbt_manifest` ON `dt_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('token', 'language', 'name', 'non colonized name', 'name token', 'name tokens',
--     'identifier', 'identifier reference', 'identifier references', 'entity', 'entities')) t
-- SET `dt_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'Token')
-- WHERE `dt_awd_pri`.`dt_awd_pri_id` = t.`dt_awd_pri_id`;
--
-- UPDATE `dt_awd_pri`, (
--     SELECT `dt_awd_pri`.`dt_awd_pri_id`, `xbt`.`name`
--     FROM `dt_awd_pri`
--     JOIN `xbt_manifest` ON `dt_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('decimal')) t
-- SET `dt_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'Decimal')
-- WHERE `dt_awd_pri`.`dt_awd_pri_id` = t.`dt_awd_pri_id`;
--
-- UPDATE `dt_awd_pri`, (
--     SELECT `dt_awd_pri`.`dt_awd_pri_id`, `xbt`.`name`
--     FROM `dt_awd_pri`
--     JOIN `xbt_manifest` ON `dt_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('double')) t
-- SET `dt_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'Double')
-- WHERE `dt_awd_pri`.`dt_awd_pri_id` = t.`dt_awd_pri_id`;
--
-- UPDATE `dt_awd_pri`, (
--     SELECT `dt_awd_pri`.`dt_awd_pri_id`, `xbt`.`name`
--     FROM `dt_awd_pri`
--     JOIN `xbt_manifest` ON `dt_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('float')) t
-- SET `dt_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'Float')
-- WHERE `dt_awd_pri`.`dt_awd_pri_id` = t.`dt_awd_pri_id`;
--
-- UPDATE `dt_awd_pri`, (
--     SELECT `dt_awd_pri`.`dt_awd_pri_id`, `xbt`.`name`
--     FROM `dt_awd_pri`
--     JOIN `xbt_manifest` ON `dt_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
--     'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')) t
-- SET `dt_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'Integer')
-- WHERE `dt_awd_pri`.`dt_awd_pri_id` = t.`dt_awd_pri_id`;
--
-- UPDATE `dt_awd_pri`, (
--     SELECT `dt_awd_pri`.`dt_awd_pri_id`, `xbt`.`name`
--     FROM `dt_awd_pri`
--     JOIN `xbt_manifest` ON `dt_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('base64 binary', 'hex binary')) t
-- SET `dt_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'Binary')
-- WHERE `dt_awd_pri`.`dt_awd_pri_id` = t.`dt_awd_pri_id`;
--
-- UPDATE `dt_awd_pri`, (
--     SELECT `dt_awd_pri`.`dt_awd_pri_id`, `xbt`.`name`
--     FROM `dt_awd_pri`
--     JOIN `xbt_manifest` ON `dt_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('date time', 'time', 'date', 'gregorian year month', 'gregorian year',
--     'gregorian month day', 'gregorian day', 'gregorian month',
--     'xbt century', 'xbt date', 'xbt day of week', 'xbt day of year', 'xbt day', 'xbt month day', 'xbt month',
--     'xbt week', 'xbt week day', 'xbt year day', 'xbt year month', 'xbt year', 'xbt year week', 'xbt year week day',
--     'xbt hour minute', 'xbt hour minute UTC', 'xbt hour minute UTC offset', 'xbt hour',
--     'xbt hour UTC', 'xbt hour UTC offset', 'xbt minute', 'xbt minute second', 'xbt second',
--     'xbt time', 'xbt time UTC', 'xbt time UTC offset', 'xbt date hour minute',
--     'xbt date hour minute UTC', 'xbt date hour minute UTC offset',
--     'xbt date hour', 'xbt date hour UTC', 'xbt date hour UTC offset',
--     'xbt date time', 'xbt date time UTC', 'xbt date time UTC offset',
--     'xbt day hour minute', 'xbt day hour minute UTC', 'xbt day hour minute UTC offset',
--     'xbt day hour', 'xbt day hour UTC', 'xbt day hour UTC offset',
--     'xbt day of week hour minute', 'xbt day of week hour minute UTC', 'xbt day of week hour minute UTC offset',
--     'xbt day of week hour', 'xbt day of week hour UTC', 'xbt day of week hour UTC offset',
--     'xbt day of week time', 'xbt day of week time UTC', 'xbt day of week time UTC offset',
--     'xbt day of year hour minute', 'xbt day of year hour minute UTC', 'xbt day of year hour minute UTC offset',
--     'xbt day of year hour', 'xbt day of year hour UTC', 'xbt day of year hour UTC offset',
--     'xbt day of year time', 'xbt day of year time UTC', 'xbt day of year time UTC offset',
--     'xbt day time', 'xbt day time UTC', 'xbt day time UTC offset',
--     'xbt month day hour minute', 'xbt month day hour minute UTC', 'xbt month day hour minute UTC offset',
--     'xbt month day hour', 'xbt month day hour UTC', 'xbt month day hour UTC offset',
--     'xbt month day time', 'xbt month day time UTC', 'xbt month day time UTC offset',
--     'xbt week day hour minute', 'xbt week day hour minute UTC', 'xbt week day hour minute UTC offset',
--     'xbt week day hour', 'xbt week day hour UTC', 'xbt week day hour UTC offset',
--     'xbt week day time', 'xbt week day time UTC', 'xbt week day time UTC offset',
--     'xbt year day hour minute', 'xbt year day hour minute UTC', 'xbt year day hour minute UTC offset',
--     'xbt year day hour', 'xbt year day hour UTC', 'xbt year day hour UTC offset',
--     'xbt year day time', 'xbt year day time UTC', 'xbt year day time UTC offset',
--     'xbt year week day hour minute', 'xbt year week day hour minute UTC', 'xbt year week day hour minute UTC offset',
--     'xbt year week day hour', 'xbt year week day hour UTC', 'xbt year week day hour UTC offset',
--     'xbt year week day time', 'xbt year week day time UTC', 'xbt year week day time UTC offset')) t
-- SET `dt_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'TimePoint')
-- WHERE `dt_awd_pri`.`dt_awd_pri_id` = t.`dt_awd_pri_id`;
--
-- UPDATE `dt_awd_pri`, (
--     SELECT `dt_awd_pri`.`dt_awd_pri_id`, `xbt`.`name`
--     FROM `dt_awd_pri`
--     JOIN `xbt_manifest` ON `dt_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('boolean', 'xbt boolean')) t
-- SET `dt_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'Boolean')
-- WHERE `dt_awd_pri`.`dt_awd_pri_id` = t.`dt_awd_pri_id`;
--
-- UPDATE `dt_awd_pri`, (
--     SELECT `dt_awd_pri`.`dt_awd_pri_id`, `xbt`.`name`
--     FROM `dt_awd_pri`
--     JOIN `xbt_manifest` ON `dt_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('duration', 'xbt week duration')) t
-- SET `dt_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'TimeDuration')
-- WHERE `dt_awd_pri`.`dt_awd_pri_id` = t.`dt_awd_pri_id`;

-- Insert into `dt_awd_pri` for connectSpec BDT_SCs
INSERT INTO `dt_sc_awd_pri` (`release_id`, `dt_sc_id`, `xbt_manifest_id`, `code_list_manifest_id`, `agency_id_list_manifest_id`, `is_default`)
SELECT DISTINCT `release_id`, `dt_sc_id`, `xbt_manifest_id`, `code_list_manifest_id`, `agency_id_list_manifest_id`, `is_default`
FROM (SELECT `release`.`release_id`,
             `dt_sc`.`dt_sc_id`,
             `xbt_manifest`.`xbt_manifest_id`,
             NULL AS `code_list_manifest_id`,
             NULL AS `agency_id_list_manifest_id`,
             `bdt_sc_pri_restri`.`is_default`
      FROM `dt_sc_manifest`
               JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
               JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
               JOIN `bdt_sc_pri_restri`
                    ON `dt_sc_manifest`.`dt_sc_manifest_id` = `bdt_sc_pri_restri`.`bdt_sc_manifest_id`
               JOIN `cdt_sc_awd_pri_xps_type_map` ON `bdt_sc_pri_restri`.`cdt_sc_awd_pri_xps_type_map_id` =
                                                     `cdt_sc_awd_pri_xps_type_map`.`cdt_sc_awd_pri_xps_type_map_id`
               JOIN `xbt` ON `cdt_sc_awd_pri_xps_type_map`.`xbt_id` = `xbt`.`xbt_id`
               JOIN `xbt_manifest` ON `xbt`.`xbt_id` = `xbt_manifest`.`xbt_id` AND
                                      `xbt_manifest`.`release_id` = `dt_sc_manifest`.`release_id`
               LEFT JOIN `dt_sc_awd_pri` ON `dt_sc_awd_pri`.`dt_sc_id` = `dt_sc_manifest`.`dt_sc_id`
      WHERE `release`.`library_id` = 3
        AND `dt_sc_awd_pri`.`dt_sc_id` IS NULL

      UNION

      SELECT `release`.`release_id`,
             `dt_sc`.`dt_sc_id`,
             NULL AS `xbt_manifest_id`,
             `code_list_manifest`.`code_list_manifest_id`,
             NULL AS `agency_id_list_manifest_id`,
             `bdt_sc_pri_restri`.`is_default`
      FROM `dt_sc_manifest`
               JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
               JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
               JOIN `bdt_sc_pri_restri`
                    ON `dt_sc_manifest`.`dt_sc_manifest_id` = `bdt_sc_pri_restri`.`bdt_sc_manifest_id`
               JOIN `code_list_manifest`
                    ON `bdt_sc_pri_restri`.`code_list_manifest_id` = `code_list_manifest`.`code_list_manifest_id` AND
                       `code_list_manifest`.`release_id` = `dt_sc_manifest`.`release_id`
               JOIN `code_list` ON `code_list_manifest`.`code_list_id` = `code_list`.`code_list_id`
               LEFT JOIN `dt_sc_awd_pri` ON `dt_sc_awd_pri`.`dt_sc_id` = `dt_sc_manifest`.`dt_sc_id`
      WHERE `release`.`library_id` = 3
        AND `dt_sc_awd_pri`.`dt_sc_id` IS NULL

      UNION

      SELECT `release`.`release_id`,
             `dt_sc`.`dt_sc_id`,
             NULL AS `xbt_manifest_id`,
             NULL AS `code_list_manifest_id`,
             `agency_id_list_manifest`.`agency_id_list_manifest_id`,
             `bdt_sc_pri_restri`.`is_default`
      FROM `dt_sc_manifest`
               JOIN `dt_sc` ON `dt_sc_manifest`.`dt_sc_id` = `dt_sc`.`dt_sc_id`
               JOIN `release` ON `dt_sc_manifest`.`release_id` = `release`.`release_id`
               JOIN `bdt_sc_pri_restri`
                    ON `dt_sc_manifest`.`dt_sc_manifest_id` = `bdt_sc_pri_restri`.`bdt_sc_manifest_id`
               JOIN `agency_id_list_manifest` ON `bdt_sc_pri_restri`.`agency_id_list_manifest_id` =
                                                 `agency_id_list_manifest`.`agency_id_list_manifest_id` AND
                                                 `agency_id_list_manifest`.`release_id` = `dt_sc_manifest`.`release_id`
               JOIN `agency_id_list`
                    ON `agency_id_list_manifest`.`agency_id_list_id` = `agency_id_list`.`agency_id_list_id`
               LEFT JOIN `dt_sc_awd_pri` ON `dt_sc_awd_pri`.`dt_sc_id` = `dt_sc_manifest`.`dt_sc_id`
      WHERE `release`.`library_id` = 3
        AND `dt_sc_awd_pri`.`dt_sc_id` IS NULL) t
ORDER BY `release_id`, `dt_sc_id`, `xbt_manifest_id`, `code_list_manifest_id`, `agency_id_list_manifest_id`;

-- Update `cdt_pri_id` for connectSpec `dt_sc_awd_pri`

-- UPDATE `dt_sc_awd_pri`, (
--     SELECT `dt_sc_awd_pri`.`dt_sc_awd_pri_id`, `xbt`.`name`
--     FROM `dt_sc_awd_pri`
--     JOIN `xbt_manifest` ON `dt_sc_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_sc_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_sc_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('normalized string')) t
-- SET `dt_sc_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'NormalizedString')
-- WHERE `dt_sc_awd_pri`.`dt_sc_awd_pri_id` = t.`dt_sc_awd_pri_id`;
--
-- UPDATE `dt_sc_awd_pri`, (
--     SELECT `dt_sc_awd_pri`.`dt_sc_awd_pri_id`, `xbt`.`name`
--     FROM `dt_sc_awd_pri`
--     JOIN `xbt_manifest` ON `dt_sc_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_sc_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_sc_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('string', 'any URI', 'qualified name', 'notation')) t
-- SET `dt_sc_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'String')
-- WHERE `dt_sc_awd_pri`.`dt_sc_awd_pri_id` = t.`dt_sc_awd_pri_id`;
--
-- UPDATE `dt_sc_awd_pri`, (
--     SELECT `dt_sc_awd_pri`.`dt_sc_awd_pri_id`, `xbt`.`name`
--     FROM `dt_sc_awd_pri`
--     JOIN `xbt_manifest` ON `dt_sc_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_sc_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_sc_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('token', 'language', 'name', 'non colonized name', 'name token', 'name tokens',
--     'identifier', 'identifier reference', 'identifier references', 'entity', 'entities')) t
-- SET `dt_sc_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'Token')
-- WHERE `dt_sc_awd_pri`.`dt_sc_awd_pri_id` = t.`dt_sc_awd_pri_id`;
--
-- UPDATE `dt_sc_awd_pri`, (
--     SELECT `dt_sc_awd_pri`.`dt_sc_awd_pri_id`, `xbt`.`name`
--     FROM `dt_sc_awd_pri`
--     JOIN `xbt_manifest` ON `dt_sc_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_sc_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_sc_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('decimal')) t
-- SET `dt_sc_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'Decimal')
-- WHERE `dt_sc_awd_pri`.`dt_sc_awd_pri_id` = t.`dt_sc_awd_pri_id`;
--
-- UPDATE `dt_sc_awd_pri`, (
--     SELECT `dt_sc_awd_pri`.`dt_sc_awd_pri_id`, `xbt`.`name`
--     FROM `dt_sc_awd_pri`
--     JOIN `xbt_manifest` ON `dt_sc_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_sc_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_sc_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('double')) t
-- SET `dt_sc_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'Double')
-- WHERE `dt_sc_awd_pri`.`dt_sc_awd_pri_id` = t.`dt_sc_awd_pri_id`;
--
-- UPDATE `dt_sc_awd_pri`, (
--     SELECT `dt_sc_awd_pri`.`dt_sc_awd_pri_id`, `xbt`.`name`
--     FROM `dt_sc_awd_pri`
--     JOIN `xbt_manifest` ON `dt_sc_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_sc_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_sc_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('float')) t
-- SET `dt_sc_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'Float')
-- WHERE `dt_sc_awd_pri`.`dt_sc_awd_pri_id` = t.`dt_sc_awd_pri_id`;
--
-- UPDATE `dt_sc_awd_pri`, (
--     SELECT `dt_sc_awd_pri`.`dt_sc_awd_pri_id`, `xbt`.`name`
--     FROM `dt_sc_awd_pri`
--     JOIN `xbt_manifest` ON `dt_sc_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_sc_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_sc_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('integer', 'non positive integer', 'negative integer', 'long', 'int', 'short', 'byte',
--     'non negative integer', 'unsigned long', 'unsigned int', 'unsigned short', 'unsigned byte', 'positive integer')) t
-- SET `dt_sc_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'Integer')
-- WHERE `dt_sc_awd_pri`.`dt_sc_awd_pri_id` = t.`dt_sc_awd_pri_id`;
--
-- UPDATE `dt_sc_awd_pri`, (
--     SELECT `dt_sc_awd_pri`.`dt_sc_awd_pri_id`, `xbt`.`name`
--     FROM `dt_sc_awd_pri`
--     JOIN `xbt_manifest` ON `dt_sc_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_sc_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_sc_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('base64 binary', 'hex binary')) t
-- SET `dt_sc_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'Binary')
-- WHERE `dt_sc_awd_pri`.`dt_sc_awd_pri_id` = t.`dt_sc_awd_pri_id`;
--
-- UPDATE `dt_sc_awd_pri`, (
--     SELECT `dt_sc_awd_pri`.`dt_sc_awd_pri_id`, `xbt`.`name`
--     FROM `dt_sc_awd_pri`
--     JOIN `xbt_manifest` ON `dt_sc_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_sc_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_sc_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('date time', 'time', 'date', 'gregorian year month', 'gregorian year',
--     'gregorian month day', 'gregorian day', 'gregorian month',
--     'xbt century', 'xbt date', 'xbt day of week', 'xbt day of year', 'xbt day', 'xbt month day', 'xbt month',
--     'xbt week', 'xbt week day', 'xbt year day', 'xbt year month', 'xbt year', 'xbt year week', 'xbt year week day',
--     'xbt hour minute', 'xbt hour minute UTC', 'xbt hour minute UTC offset', 'xbt hour',
--     'xbt hour UTC', 'xbt hour UTC offset', 'xbt minute', 'xbt minute second', 'xbt second',
--     'xbt time', 'xbt time UTC', 'xbt time UTC offset', 'xbt date hour minute',
--     'xbt date hour minute UTC', 'xbt date hour minute UTC offset',
--     'xbt date hour', 'xbt date hour UTC', 'xbt date hour UTC offset',
--     'xbt date time', 'xbt date time UTC', 'xbt date time UTC offset',
--     'xbt day hour minute', 'xbt day hour minute UTC', 'xbt day hour minute UTC offset',
--     'xbt day hour', 'xbt day hour UTC', 'xbt day hour UTC offset',
--     'xbt day of week hour minute', 'xbt day of week hour minute UTC', 'xbt day of week hour minute UTC offset',
--     'xbt day of week hour', 'xbt day of week hour UTC', 'xbt day of week hour UTC offset',
--     'xbt day of week time', 'xbt day of week time UTC', 'xbt day of week time UTC offset',
--     'xbt day of year hour minute', 'xbt day of year hour minute UTC', 'xbt day of year hour minute UTC offset',
--     'xbt day of year hour', 'xbt day of year hour UTC', 'xbt day of year hour UTC offset',
--     'xbt day of year time', 'xbt day of year time UTC', 'xbt day of year time UTC offset',
--     'xbt day time', 'xbt day time UTC', 'xbt day time UTC offset',
--     'xbt month day hour minute', 'xbt month day hour minute UTC', 'xbt month day hour minute UTC offset',
--     'xbt month day hour', 'xbt month day hour UTC', 'xbt month day hour UTC offset',
--     'xbt month day time', 'xbt month day time UTC', 'xbt month day time UTC offset',
--     'xbt week day hour minute', 'xbt week day hour minute UTC', 'xbt week day hour minute UTC offset',
--     'xbt week day hour', 'xbt week day hour UTC', 'xbt week day hour UTC offset',
--     'xbt week day time', 'xbt week day time UTC', 'xbt week day time UTC offset',
--     'xbt year day hour minute', 'xbt year day hour minute UTC', 'xbt year day hour minute UTC offset',
--     'xbt year day hour', 'xbt year day hour UTC', 'xbt year day hour UTC offset',
--     'xbt year day time', 'xbt year day time UTC', 'xbt year day time UTC offset',
--     'xbt year week day hour minute', 'xbt year week day hour minute UTC', 'xbt year week day hour minute UTC offset',
--     'xbt year week day hour', 'xbt year week day hour UTC', 'xbt year week day hour UTC offset',
--     'xbt year week day time', 'xbt year week day time UTC', 'xbt year week day time UTC offset')) t
-- SET `dt_sc_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'TimePoint')
-- WHERE `dt_sc_awd_pri`.`dt_sc_awd_pri_id` = t.`dt_sc_awd_pri_id`;
--
-- UPDATE `dt_sc_awd_pri`, (
--     SELECT `dt_sc_awd_pri`.`dt_sc_awd_pri_id`, `xbt`.`name`
--     FROM `dt_sc_awd_pri`
--     JOIN `xbt_manifest` ON `dt_sc_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_sc_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_sc_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('boolean', 'xbt boolean')) t
-- SET `dt_sc_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'Boolean')
-- WHERE `dt_sc_awd_pri`.`dt_sc_awd_pri_id` = t.`dt_sc_awd_pri_id`;
--
-- UPDATE `dt_sc_awd_pri`, (
--     SELECT `dt_sc_awd_pri`.`dt_sc_awd_pri_id`, `xbt`.`name`
--     FROM `dt_sc_awd_pri`
--     JOIN `xbt_manifest` ON `dt_sc_awd_pri`.`xbt_manifest_id` = `xbt_manifest`.`xbt_manifest_id`
--     JOIN `xbt` ON `xbt_manifest`.`xbt_id` = `xbt`.`xbt_id`
--     WHERE `dt_sc_awd_pri`.`xbt_manifest_id` IS NOT NULL AND `dt_sc_awd_pri`.`cdt_pri_id` IS NULL
--     AND `xbt`.`name` IN ('duration', 'xbt week duration')) t
-- SET `dt_sc_awd_pri`.`cdt_pri_id` = (SELECT `cdt_pri_id` FROM `cdt_pri` WHERE `name` = 'TimeDuration')
-- WHERE `dt_sc_awd_pri`.`dt_sc_awd_pri_id` = t.`dt_sc_awd_pri_id`;

-- ----------------------------------------------------------------
-- Drop unused tables and constraints related to CDT/BDT primitives
-- ----------------------------------------------------------------
ALTER TABLE `bbie_sc` DROP CONSTRAINT `bbie_sc_dt_sc_pri_restri_id_fk`, DROP COLUMN `dt_sc_pri_restri_id`;
ALTER TABLE `bbie` DROP CONSTRAINT `bbie_bdt_pri_restri_id_fk`, DROP COLUMN `bdt_pri_restri_id`;
DROP TABLE `bdt_sc_pri_restri`;
DROP TABLE `bdt_pri_restri`;
DROP TABLE `cdt_sc_ref_spec`;
DROP TABLE `cdt_ref_spec`;
DROP TABLE `ref_spec`;
DROP TABLE `cdt_sc_awd_pri_xps_type_map`;
DROP TABLE `cdt_awd_pri_xps_type_map`;
DROP TABLE `cdt_sc_awd_pri`;
DROP TABLE `cdt_awd_pri`;