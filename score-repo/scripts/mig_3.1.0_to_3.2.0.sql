-- ----------------------------------------------------
-- Migration script for Score v3.2.0                 --
--                                                   --
-- Author: Hakju Oh <hakju.oh@nist.gov>              --
--         Bingqi Cheng <bingqi.cheng@nist.gov>      --
-- ----------------------------------------------------

-- Change the data type of `configuration`.`value`
ALTER TABLE `configuration` MODIFY COLUMN `value` LONGTEXT DEFAULT NULL COMMENT 'The value of configuration property.';

-- Move `dt`.`den` TO `dt_manifest`
ALTER TABLE `dt_manifest` ADD COLUMN `den` varchar(200) NOT NULL COMMENT 'Dictionary Entry Name of the data type.' AFTER `based_dt_manifest_id`;

UPDATE `dt_manifest`, `dt`
SET `dt_manifest`.`den` = CONCAT(`dt`.`data_type_term`, '. Type')
WHERE `dt_manifest`.`dt_id` = `dt`.`dt_id` AND `dt`.`qualifier` IS NULL;

UPDATE `dt_manifest`, `dt`
SET `dt_manifest`.`den` = CONCAT(`dt`.`qualifier`, '_ ', `dt`.`data_type_term`, '. Type')
WHERE `dt_manifest`.`dt_id` = `dt`.`dt_id` AND `dt`.`qualifier` IS NOT NULL;

ALTER TABLE `dt` DROP COLUMN `den`;

-- Move `bccp`.`den` TO `bccp_manifest`
ALTER TABLE `bccp_manifest` ADD COLUMN `den` varchar(200) NOT NULL COMMENT 'The dictionary entry name of the BCCP. It is derived by PROPERTY_TERM + ". " + REPRESENTATION_TERM.' AFTER `bdt_manifest_id`;

UPDATE `bccp_manifest`, `bccp`, `dt_manifest`
SET `bccp_manifest`.`den` = CONCAT(`bccp`.`property_term`, '. ', LEFT(`dt_manifest`.`den`, LENGTH(`dt_manifest`.`den`) - 6))
WHERE `bccp_manifest`.`bccp_id` = `bccp`.`bccp_id` AND `bccp_manifest`.`bdt_manifest_id` = `dt_manifest`.`dt_manifest_id`;

ALTER TABLE `bccp` DROP COLUMN `den`;

-- Move `acc`.`den` TO `acc_manifest`
ALTER TABLE `acc_manifest` ADD COLUMN `den` varchar(200) NOT NULL COMMENT 'DEN (dictionary entry name) of the ACC. It can be derived as OBJECT_CLASS_QUALIFIER + "_ " + OBJECT_CLASS_TERM + ". Details".' AFTER `based_acc_manifest_id`;

UPDATE `acc_manifest`, `acc`
SET `acc_manifest`.`den` = CONCAT(`acc`.`object_class_term`, '. Details')
WHERE `acc_manifest`.`acc_id` = `acc`.`acc_id`;

ALTER TABLE `acc` DROP COLUMN `den`;

-- Move `asccp`.`den` TO `asccp_manifest`
ALTER TABLE `asccp_manifest` ADD COLUMN `den` varchar(200) DEFAULT NULL COMMENT 'The dictionary entry name of the ASCCP.' AFTER `role_of_acc_manifest_id`;

UPDATE `asccp_manifest`, `acc_manifest`, `asccp`, `acc`
SET `asccp_manifest`.`den` = CONCAT(`asccp`.`property_term`, '. ', `acc`.`object_class_term`)
WHERE `asccp_manifest`.`asccp_id` = `asccp`.`asccp_id`
  AND `asccp_manifest`.`role_of_acc_manifest_id` = `acc_manifest`.`acc_manifest_id`
  AND `acc_manifest`.`acc_id` = `acc`.`acc_id`;

ALTER TABLE `asccp` DROP COLUMN `den`;

-- Move `ascc`.`den` TO `ascc_manifest`
ALTER TABLE `ascc_manifest` ADD COLUMN `den` varchar(200) NOT NULL COMMENT 'DEN (dictionary entry name) of the ASCC. This column can be derived from Qualifier and OBJECT_CLASS_TERM of the FROM_ACC_ID and DEN of the TO_ASCCP_ID as Qualifier + "_ " + OBJECT_CLASS_TERM + ". " + DEN.' AFTER `to_asccp_manifest_id`;

UPDATE `ascc_manifest`, `acc_manifest`, `asccp_manifest`, `acc`
SET `ascc_manifest`.`den` = CONCAT(`acc`.`object_class_term`, '. ', `asccp_manifest`.`den`)
WHERE `ascc_manifest`.`from_acc_manifest_id` = `acc_manifest`.`acc_manifest_id`
  AND `acc_manifest`.`acc_id` = `acc`.`acc_id`
  AND `ascc_manifest`.`to_asccp_manifest_id` = `asccp_manifest`.`asccp_manifest_id`;

ALTER TABLE `ascc` DROP COLUMN `den`;

-- Move `bcc`.`den` TO `bcc_manifest`
ALTER TABLE `bcc_manifest` ADD COLUMN `den` varchar(200) NOT NULL COMMENT 'DEN (dictionary entry name) of the BCC. This column can be derived from QUALIFIER and OBJECT_CLASS_TERM of the FROM_ACC_ID and DEN of the TO_BCCP_ID as QUALIFIER + "_ " + OBJECT_CLASS_TERM + ". " + DEN.' AFTER `to_bccp_manifest_id`;

UPDATE `bcc_manifest`, `acc_manifest`, `bccp_manifest`, `acc`
SET `bcc_manifest`.`den` = CONCAT(`acc`.`object_class_term`, '. ', `bccp_manifest`.`den`)
WHERE `bcc_manifest`.`from_acc_manifest_id` = `acc_manifest`.`acc_manifest_id`
  AND `acc_manifest`.`acc_id` = `acc`.`acc_id`
  AND `bcc_manifest`.`to_bccp_manifest_id` = `bccp_manifest`.`bccp_manifest_id`;

ALTER TABLE `bcc` DROP COLUMN `den`;

-- Issue #1554
-- Add `facet_min_length`, `facet_max_length`, and `facet_pattern` to `dt` and `dt_sc` tables.
ALTER TABLE `dt`
    ADD COLUMN `facet_min_length` bigint(20) unsigned DEFAULT NULL COMMENT 'Defines the minimum number of units of length.' AFTER `based_dt_id`,
    ADD COLUMN `facet_max_length` bigint(20) unsigned DEFAULT NULL COMMENT 'Defines the minimum number of units of length.' AFTER `facet_min_length`,
    ADD COLUMN `facet_pattern`    text                DEFAULT NULL COMMENT 'Defines a constraint on the lexical space of a datatype to literals in a specific pattern.' AFTER `facet_max_length`;

ALTER TABLE `dt_sc`
    ADD COLUMN `facet_min_length` bigint(20) unsigned DEFAULT NULL COMMENT 'Defines the minimum number of units of length.' AFTER `fixed_value`,
    ADD COLUMN `facet_max_length` bigint(20) unsigned DEFAULT NULL COMMENT 'Defines the minimum number of units of length.' AFTER `facet_min_length`,
    ADD COLUMN `facet_pattern`    text                DEFAULT NULL COMMENT 'Defines a constraint on the lexical space of a datatype to literals in a specific pattern.' AFTER `facet_max_length`;

-- Issue #1558
-- Initialize color sets
INSERT INTO `configuration` (`name`, `type`, `value`)
VALUES ('score.pages.colors.cc-state.WIP.background', 'String', '#d32f2f'),
       ('score.pages.colors.cc-state.WIP.font', 'String', '#ffffff'),
       ('score.pages.colors.cc-state.QA.background', 'String', '#b961e1'),
       ('score.pages.colors.cc-state.QA.font', 'String', '#ffffff'),
       ('score.pages.colors.cc-state.Draft.background', 'String', '#b961e1'),
       ('score.pages.colors.cc-state.Draft.font', 'String', '#ffffff'),
       ('score.pages.colors.cc-state.Candidate.background', 'String', '#303f9f'),
       ('score.pages.colors.cc-state.Candidate.font', 'String', '#ffffff'),
       ('score.pages.colors.cc-state.Production.background', 'String', '#303f9f'),
       ('score.pages.colors.cc-state.Production.font', 'String', '#ffffff'),
       ('score.pages.colors.cc-state.ReleaseDraft.background', 'String', '#c55a11'),
       ('score.pages.colors.cc-state.ReleaseDraft.font', 'String', '#ffffff'),
       ('score.pages.colors.cc-state.Published.background', 'String', '#388e3c'),
       ('score.pages.colors.cc-state.Published.font', 'String', '#ffffff'),
       ('score.pages.colors.cc-state.Deleted.background', 'String', '#616161'),
       ('score.pages.colors.cc-state.Deleted.font', 'String', '#ffffff'),
       ('score.pages.colors.cc-state.Deprecated.background', 'String', '#455a64'),
       ('score.pages.colors.cc-state.Deprecated.font', 'String', '#ffffff'),
       ('score.pages.colors.release-state.Processing.background', 'String', '#bfdadc'),
       ('score.pages.colors.release-state.Processing.font', 'String', '#000000'),
       ('score.pages.colors.release-state.Initialized.background', 'String', '#d32f2f'),
       ('score.pages.colors.release-state.Initialized.font', 'String', '#ffffff'),
       ('score.pages.colors.release-state.Draft.background', 'String', '#7b1fa2'),
       ('score.pages.colors.release-state.Draft.font', 'String', '#ffffff'),
       ('score.pages.colors.release-state.Published.background', 'String', '#388e3c'),
       ('score.pages.colors.release-state.Published.font', 'String', '#ffffff'),
       ('score.pages.colors.user-role.Admin.background', 'String', '#ffe4e1'),
       ('score.pages.colors.user-role.Admin.font', 'String', '#212121'),
       ('score.pages.colors.user-role.Developer.background', 'String', '#fafad2'),
       ('score.pages.colors.user-role.Developer.font', 'String', '#212121'),
       ('score.pages.colors.user-role.End-User.background', 'String', '#f5f5f5'),
       ('score.pages.colors.user-role.End-User.font', 'String', '#212121');

-- Issue #1492
-- Add tables for OpenAPI documents
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `oas_doc`;
CREATE TABLE `oas_doc`
(
    `oas_doc_id`            bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                  varchar(41) NOT NULL COMMENT 'The GUID of the record.',
    `open_api_version`      varchar(20) NOT NULL COMMENT 'REQUIRED. This string MUST be the semantic version number of the OpenAPI Specification version that the OpenAPI document uses. The openapi field SHOULD be used by tooling specifications and clients to interpret the OpenAPI document. This is not related to the API info.version string.',
    `title`                 text        NOT NULL COMMENT 'The title of the API.',
    `description`           text        DEFAULT NULL COMMENT 'A short description of the API. CommonMark syntax MAY be used for rich text representation.',
    `terms_of_service`      varchar(250) DEFAULT NULL COMMENT 'A URL to the Terms of Service for the API. MUST be in the format of a URL.',
    `version`               varchar(20) NOT NULL COMMENT 'REQUIRED. The version of the OpenAPI document (which is distinct from the OpenAPI Specification version or the API implementation version).',
    `contact_name`          text COMMENT 'The identifying name of the contact person/organization.',
    `contact_url`           varchar(250) DEFAULT NULL COMMENT 'The URL pointing to the contact information. MUST be in the format of a URL.',
    `contact_email`         text COMMENT 'The email address of the contact person/organization. MUST be in the format of an email address.',
    `license_name`          varchar(100) DEFAULT NULL COMMENT 'REQUIRED if the license used for the API. The license name used for the API.',
    `license_url`           varchar(250) DEFAULT NULL COMMENT 'A URL to the license used for the API. MUST be in the format of a URL.',
    `owner_user_id`         bigint(20) unsigned NOT NULL COMMENT 'The user who owns the record.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_doc_id`),
    KEY                     `oas_doc_created_by_fk` (`created_by`),
    KEY                     `oas_doc_last_updated_by_fk` (`last_updated_by`),
    KEY                     `oas_doc_owner_user_id_fk` (`owner_user_id`),
    CONSTRAINT `oas_doc_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_doc_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_doc_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_tag`;
CREATE TABLE `oas_tag`
(
    `oas_tag_id`            bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                  varchar(41)  NOT NULL COMMENT 'The GUID of the record.',
    `name`                  varchar(200) NOT NULL COMMENT 'REQUIRED. The name of the tag.',
    `description`           text COMMENT 'A short description for the tag. CommonMark syntax MAY be used for rich text representation.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_tag_id`),
    KEY                     `oas_tag_created_by_fk` (`created_by`),
    KEY                     `oas_tag_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_tag_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_tag_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_doc_tag`;
CREATE TABLE `oas_doc_tag`
(
    `oas_doc_id`            bigint(20) unsigned NOT NULL COMMENT 'The primary key of the record.',
    `oas_tag_id`            bigint(20) unsigned NOT NULL COMMENT 'The primary key of the record.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_doc_id`, `oas_tag_id`),
    KEY                     `oas_doc_tag_oas_tag_id_fk` (`oas_tag_id`),
    CONSTRAINT `oas_doc_tag_oas_doc_id_fk` FOREIGN KEY (`oas_doc_id`) REFERENCES `oas_doc` (`oas_doc_id`),
    CONSTRAINT `oas_doc_tag_oas_tag_id_fk` FOREIGN KEY (`oas_tag_id`) REFERENCES `oas_tag` (`oas_tag_id`),
    CONSTRAINT `oas_doc_tag_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_doc_tag_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_example`;
CREATE TABLE `oas_example`
(
    `oas_example_id`        bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `summary`               text         DEFAULT NULL COMMENT 'Short description for the example.',
    `description`           text COMMENT 'Long description for the example. CommonMark syntax MAY be used for rich text representation.',
    `ref`                   varchar(250) DEFAULT NULL COMMENT 'A URL that points to the literal example. This provides the capability to reference examples that cannot easily be included in JSON or YAML documents. The value field and externalValue field are mutually exclusive.',
    `value`                 text COMMENT 'Embedded literal example. The value field and externalValue field are mutually exclusive. To represent examples of media types that cannot naturally represented in JSON or YAML, use a string value to contain the example, escaping where necessary.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_example_id`),
    KEY                     `oas_example_created_by_fk` (`created_by`),
    KEY                     `oas_example_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_example_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_example_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_external_doc`;
CREATE TABLE `oas_external_doc`
(
    `oas_external_doc_id`   bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `url`                   varchar(250) NOT NULL COMMENT 'REQUIRED. The URL for the target documentation. Value MUST be in the format of a URL.',
    `description`           text COMMENT 'A short description of the target documentation. CommonMark syntax MAY be used for rich text representation.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_external_doc_id`),
    KEY                     `oas_external_doc_created_by_fk` (`created_by`),
    KEY                     `oas_external_doc_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_external_doc_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_external_doc_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Allows referencing an external resource for extended documentation.';

DROP TABLE IF EXISTS `oas_external_doc_doc`;
CREATE TABLE `oas_external_doc_doc`
(
    `oas_external_doc_id`   bigint(20) unsigned NOT NULL COMMENT 'The primary key of the record.',
    `oas_doc_id`            bigint(20) unsigned NOT NULL COMMENT 'The primary key of the record.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_external_doc_id`, `oas_doc_id`),
    KEY                     `oas_external_doc_oas_doc_id_fk` (`oas_doc_id`),
    KEY                     `oas_external_doc_doc_created_by_fk` (`created_by`),
    KEY                     `oas_external_doc_doc_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_external_doc_doc_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_external_doc_doc_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_external_doc_oas_doc_id_fk` FOREIGN KEY (`oas_doc_id`) REFERENCES `oas_doc` (`oas_doc_id`),
    CONSTRAINT `oas_external_doc_oas_external_doc_id_fk` FOREIGN KEY (`oas_external_doc_id`) REFERENCES `oas_external_doc` (`oas_external_doc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_http_header`;
CREATE TABLE `oas_http_header`
(
    `oas_http_header_id`      bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                    varchar(41) NOT NULL COMMENT 'The GUID of the record.',
    `header`                  varchar(200) DEFAULT NULL COMMENT 'REQUIRED. The name of the header. Header names are case sensitive. ',
    `description`             text COMMENT 'A brief description of the header. This could contain examples of use. CommonMark syntax MAY be used for rich text representation.',
    `agency_id_list_value_id` bigint(20) unsigned NOT NULL COMMENT 'A reference of the agency id list value',
    `schema_type_reference`   text        NOT NULL COMMENT 'REQUIRED. The schema defining the type used for the header using the reference string, $ref.',
    `owner_user_id`           bigint(20) unsigned NOT NULL COMMENT 'The user who owns the record.',
    `created_by`              bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`         bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`      datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp`   datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_http_header_id`),
    KEY                       `oas_http_header_agency_id_list_value_id_fk` (`agency_id_list_value_id`),
    KEY                       `oas_http_header_created_by_fk` (`created_by`),
    KEY                       `oas_http_header_last_updated_by_fk` (`last_updated_by`),
    KEY                       `oas_http_header_owner_user_id_fk` (`owner_user_id`),
    CONSTRAINT `oas_http_header_agency_id_list_value_id_fk` FOREIGN KEY (`agency_id_list_value_id`) REFERENCES `agency_id_list_value` (`agency_id_list_value_id`),
    CONSTRAINT `oas_http_header_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_http_header_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_http_header_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_media_type`;
CREATE TABLE `oas_media_type`
(
    `oas_media_type_id`     bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                  varchar(41) NOT NULL COMMENT 'The GUID of the record.',
    `description`           text COMMENT 'On POST, PUT, and PATCH, $ref is present',
    `owner_user_id`         bigint(20) unsigned NOT NULL COMMENT 'The user who owns the record.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_media_type_id`),
    KEY                     `oas_media_type_created_by_fk` (`created_by`),
    KEY                     `oas_media_type_last_updated_by_fk` (`last_updated_by`),
    KEY                     `oas_media_type_owner_user_id_fk` (`owner_user_id`),
    CONSTRAINT `oas_media_type_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_media_type_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_media_type_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_message_body`;
CREATE TABLE `oas_message_body`
(
    `oas_message_body_id`   bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `top_level_asbiep_id`   bigint(20) unsigned DEFAULT NULL COMMENT 'A reference of the ASBIEP record.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_message_body_id`),
    KEY                     `oas_message_body_oas_asbiep_id_fk` (`top_level_asbiep_id`),
    KEY                     `oas_message_body_created_by_fk` (`created_by`),
    KEY                     `oas_message_body_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_message_body_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_message_body_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_message_body_top_level_asbiep_id_fk` FOREIGN KEY (`top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_resource`;
CREATE TABLE `oas_resource`
(
    `oas_resource_id`       bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `oas_doc_id`            bigint(20) unsigned DEFAULT NULL COMMENT 'A reference of the doc record.',
    `path`                  text NOT NULL COMMENT 'This will hold the BIE name by default.',
    `ref`                   text,
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_resource_id`),
    KEY                     `oas_resource_oas_doc_id_fk` (`oas_doc_id`),
    KEY                     `oas_resource_created_by_fk` (`created_by`),
    KEY                     `oas_resource_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_resource_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_resource_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_resource_oas_doc_id_fk` FOREIGN KEY (`oas_doc_id`) REFERENCES `oas_doc` (`oas_doc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_operation`;
CREATE TABLE `oas_operation`
(
    `oas_operation_id`      bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `oas_resource_id`       bigint(20) unsigned NOT NULL COMMENT 'A reference of the resource record.',
    `verb`                  varchar(30) NOT NULL COMMENT 'verbs, list of values droplist: get, put , post, delete, options, head, patch, trace;',
    `operation_id`          varchar(1024) NOT NULL COMMENT 'Unique string used to identify the operation. The id MUST be unique among all operations described in the API. The operationId value is case-sensitive. Tools and libraries MAY use the operationId to uniquely identify an operation, therefore, it is RECOMMENDED to follow common programming naming conventions.',
    `summary`               text DEFAULT NULL COMMENT 'A short summary of what the operation does.',
    `description`           text COMMENT 'A verbose explanation of the operation behavior. CommonMark syntax MAY be used for rich text representation.',
    `deprecated`            tinyint(1) DEFAULT '0' COMMENT 'Declares this operation to be deprecated. Consumers SHOULD refrain from usage of the declared operation. Default value is false.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_operation_id`),
    KEY                     `oas_operation_created_by_fk` (`created_by`),
    KEY                     `oas_operation_last_updated_by_fk` (`last_updated_by`),
    KEY                     `oas_operation_oas_resource_id_fk` (`oas_resource_id`),
    CONSTRAINT `oas_operation_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_operation_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_operation_oas_resource_id_fk` FOREIGN KEY (`oas_resource_id`) REFERENCES `oas_resource` (`oas_resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_parameter`;
CREATE TABLE `oas_parameter`
(
    `oas_parameter_id`      bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                  varchar(41)  NOT NULL COMMENT 'The GUID of the record.',
    `name`                  varchar(200) NOT NULL COMMENT 'REQUIRED. The name of the parameter. Parameter names are case sensitive.\nIf in is "path", the name field MUST correspond to a template expression occurring within the path field in the Paths Object. See Path Templating for further information.\nIf in is "header" and the name field is "Accept", "Content-Type" or "Authorization", the parameter definition SHALL be ignored.\nFor all other cases, the name corresponds to the parameter name used by the in property.',
    `in`                    varchar(100) NOT NULL COMMENT 'REQUIRED. The location of the parameter. Possible values are "query", "header", "path" or "cookie".',
    `required`              tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Determines whether this parameter is mandatory. If the parameter location is "path", this property is REQUIRED and its value MUST be true. Otherwise, the property MAY be included and its default value is false.',
    `description`           text COMMENT 'A brief description of the parameter. This could contain examples of use. CommonMark syntax MAY be used for rich text representation.',
    `schema_type_reference` text         NOT NULL COMMENT 'A reference of the schema defining the type used for the parameter.',
    `allow_reserved`        tinyint(1) DEFAULT '0' COMMENT 'Determines whether the parameter value SHOULD allow reserved characters, as defined by RFC3986 :/?#[]@!$&''()*+,;= to be included without percent-encoding. This property only applies to parameters with an in value of query. The default value is false.',
    `deprecated`            tinyint(1) DEFAULT '0' COMMENT 'Specifies that a parameter is deprecated and SHOULD be transitioned out of usage. Default value is false.',
    `oas_http_header_id`    bigint(20) unsigned DEFAULT NULL COMMENT 'IF IN = Header, Then select form OAS_HTTP_HEADER table',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_parameter_id`),
    KEY                     `oas_parameter_oas_http_header_id_fk` (`oas_http_header_id`),
    KEY                     `oas_parameter_created_by_fk` (`created_by`),
    KEY                     `oas_parameter_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_parameter_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_parameter_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_parameter_oas_http_header_id_fk` FOREIGN KEY (`oas_http_header_id`) REFERENCES `oas_http_header` (`oas_http_header_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_request`;
CREATE TABLE `oas_request`
(
    `oas_request_id`                  bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `oas_operation_id`                bigint(20) unsigned NOT NULL,
    `description`                     text DEFAULT NULL COMMENT 'A brief description of the request body. This could contain examples of use. CommonMark syntax MAY be used for rich text representation.',
    `required`                        tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Determines if the request body is required in the request. Defaults to false.',
    `oas_message_body_id`             bigint(20) unsigned NOT NULL,
    `make_array_indicator`            tinyint(1) DEFAULT '0',
    `suppress_root_indicator`         tinyint(1) DEFAULT '0',
    `meta_header_top_level_asbiep_id` bigint(20) unsigned DEFAULT NULL,
    `pagination_top_level_asbiep_id`  bigint(20) unsigned DEFAULT NULL,
    `is_callback`                     tinyint(1) DEFAULT '0' COMMENT 'If is_callback == true, oas_callback table has URL rows in it, with eventType=Success or Failed values to allow different endpoints to be called when a successful request is processed, or failure endpoint when an exception occurs.',
    `created_by`                      bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`                 bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`              datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp`           datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_request_id`),
    KEY                               `oas_request_oas_operation_id_fk` (`oas_operation_id`),
    KEY                               `oas_request_oas_message_body_id_fk` (`oas_message_body_id`),
    KEY                               `oas_request_created_by_fk` (`created_by`),
    KEY                               `oas_request_last_updated_by_fk` (`last_updated_by`),
    KEY                               `oas_request_meta_header_top_level_asbiep_id_fk` (`meta_header_top_level_asbiep_id`),
    KEY                               `oas_request_pagination_top_level_asbiep_id_fk` (`pagination_top_level_asbiep_id`),
    CONSTRAINT `oas_request_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_request_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_request_oas_message_body_id_fk` FOREIGN KEY (`oas_message_body_id`) REFERENCES `oas_message_body` (`oas_message_body_id`),
    CONSTRAINT `oas_request_oas_operation_id_fk` FOREIGN KEY (`oas_operation_id`) REFERENCES `oas_operation` (`oas_operation_id`),
    CONSTRAINT `oas_request_meta_header_top_level_asbiep_id_fk` FOREIGN KEY (`meta_header_top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`),
    CONSTRAINT `oas_request_pagination_top_level_asbiep_id_fk` FOREIGN KEY (`pagination_top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_response`;
CREATE TABLE `oas_response`
(
    `oas_response_id`                 bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `oas_operation_id`                bigint(20) unsigned NOT NULL,
    `http_status_code`                int(11) DEFAULT NULL,
    `description`                     text DEFAULT NULL COMMENT 'A brief description of the response body. This could contain examples of use. CommonMark syntax MAY be used for rich text representation.',
    `oas_message_body_id`             bigint(20) unsigned NOT NULL,
    `make_array_indicator`            tinyint(1) DEFAULT '0',
    `suppress_root_indicator`         tinyint(1) DEFAULT '0',
    `meta_header_top_level_asbiep_id` bigint(20) unsigned DEFAULT NULL,
    `pagination_top_level_asbiep_id`  bigint(20) unsigned DEFAULT NULL,
    `include_confirm_indicator`       tinyint(1) DEFAULT '0',
    `created_by`                      bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`                 bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`              datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp`           datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_response_id`),
    KEY                               `oas_response_created_by_fk` (`created_by`),
    KEY                               `oas_response_last_updated_by_fk` (`last_updated_by`),
    KEY                               `oas_response_oas_message_body_id_fk` (`oas_message_body_id`),
    KEY                               `oas_response_oas_operation_id_fk` (`oas_operation_id`),
    KEY                               `oas_response_meta_header_top_level_asbiep_id_fk` (`meta_header_top_level_asbiep_id`),
    KEY                               `oas_response_pagination_top_level_asbiep_id_fk` (`pagination_top_level_asbiep_id`),
    CONSTRAINT `oas_response_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_response_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_response_oas_message_body_id_fk` FOREIGN KEY (`oas_message_body_id`) REFERENCES `oas_message_body` (`oas_message_body_id`),
    CONSTRAINT `oas_response_oas_operation_id_fk` FOREIGN KEY (`oas_operation_id`) REFERENCES `oas_operation` (`oas_operation_id`),
    CONSTRAINT `oas_response_meta_header_top_level_asbiep_id_fk` FOREIGN KEY (`meta_header_top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`),
    CONSTRAINT `oas_response_pagination_top_level_asbiep_id_fk` FOREIGN KEY (`pagination_top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_server`;
CREATE TABLE `oas_server`
(
    `oas_server_id`         bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                  varchar(41)  NOT NULL COMMENT 'The GUID of the record.',
    `oas_doc_id`            bigint(20) unsigned NOT NULL COMMENT 'A reference of the doc record.',
    `description`           text COMMENT 'An optional string describing the host designated by the URL. CommonMark syntax MAY be used for rich text representation.',
    `url`                   varchar(250) NOT NULL COMMENT 'REQUIRED. A URL to the target host. This URL supports Server Variables and MAY be relative, to indicate that the host location is relative to the location where the OpenAPI document is being served. Variable substitutions will be made when a variable is named in {brackets}.',
    `variables`             text COMMENT 'A map between a variable name and its value. The value is used for substitution in the server''s URL template.',
    `owner_user_id`         bigint(20) unsigned NOT NULL COMMENT 'The user who owns the record.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_server_id`),
    KEY                     `oas_server_oas_doc_id_fk` (`oas_doc_id`),
    KEY                     `oas_server_created_by_fk` (`created_by`),
    KEY                     `oas_server_last_updated_by_fk` (`last_updated_by`),
    KEY                     `oas_server_owner_user_id_fk` (`owner_user_id`),
    CONSTRAINT `oas_server_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_server_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_server_oas_doc_id_fk` FOREIGN KEY (`oas_doc_id`) REFERENCES `oas_doc` (`oas_doc_id`),
    CONSTRAINT `oas_server_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_server_variable`;
CREATE TABLE `oas_server_variable`
(
    `oas_server_variable_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `oas_server_id`          bigint(20) unsigned NOT NULL COMMENT 'A reference of the server record.',
    `name`                   varchar(100) COMMENT '"port", "username", "basePath" are the examples in the OpenAPI Specification.',
    `description`            text COMMENT 'An optional description for the server variable. CommonMark syntax MAY be used for rich text representation.',
    `default`                text DEFAULT NULL COMMENT 'REQUIRED. The default value to use for substitution, which SHALL be sent if an alternate value is not supplied. Note this behavior is different than the Schema Object''s treatment of default values, because in those cases parameter values are optional. If the enum is defined, the value SHOULD exist in the enum''s values.',
    `enum`                   text COMMENT 'An enumeration of string values to be used if the substitution options are from a limited set. The array SHOULD NOT be empty.',
    `created_by`             bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`        bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`     datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp`  datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_server_variable_id`),
    KEY                      `oas_server_variable_oas_server_id_fk` (`oas_server_id`),
    KEY                      `oas_server_variable_created_by_fk` (`created_by`),
    KEY                      `oas_server_variable_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_server_variable_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_server_variable_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_server_variable_oas_server_id_fk` FOREIGN KEY (`oas_server_id`) REFERENCES `oas_server` (`oas_server_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_parameter_link`;
CREATE TABLE `oas_parameter_link`
(
    `oas_parameter_link_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `oas_response_id`       bigint(20) unsigned NOT NULL,
    `oas_parameter_id`      bigint(20) unsigned NOT NULL,
    `oas_operation_id`      bigint(20) unsigned DEFAULT NULL,
    `expression`            text COMMENT 'jsonPathSnip for example ''$response.body#/purchaseOrderHeader.identifier''',
    `description`           text,
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_parameter_link_id`),
    KEY                     `oas_parameter_link_oas_response_id_fk` (`oas_response_id`),
    KEY                     `oas_parameter_link_oas_parameter_id_fk` (`oas_parameter_id`),
    KEY                     `oas_parameter_link_oas_operation_id_fk` (`oas_operation_id`),
    KEY                     `oas_parameter_link_created_by_fk` (`created_by`),
    KEY                     `oas_parameter_link_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_parameter_link_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_parameter_link_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_parameter_link_oas_operation_id_fk` FOREIGN KEY (`oas_operation_id`) REFERENCES `oas_operation` (`oas_operation_id`),
    CONSTRAINT `oas_parameter_link_oas_parameter_id_fk` FOREIGN KEY (`oas_parameter_id`) REFERENCES `oas_parameter` (`oas_parameter_id`),
    CONSTRAINT `oas_parameter_link_oas_response_id_fk` FOREIGN KEY (`oas_response_id`) REFERENCES `oas_response` (`oas_response_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_request_parameter`;
CREATE TABLE `oas_request_parameter`
(
    `oas_parameter_id`      bigint(20) unsigned NOT NULL COMMENT 'The primary key of the record.',
    `oas_request_id`        bigint(20) unsigned NOT NULL COMMENT 'The primary key of the record.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_parameter_id`, `oas_request_id`),
    KEY                     `oas_request_parameter_created_by_fk` (`created_by`),
    KEY                     `oas_request_parameter_last_updated_by_fk` (`last_updated_by`),
    KEY                     `oas_request_parameter_oas_request_id_fk` (`oas_request_id`),
    CONSTRAINT `oas_request_parameter_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_request_parameter_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_request_parameter_oas_parameter_id_fk` FOREIGN KEY (`oas_parameter_id`) REFERENCES `oas_parameter` (`oas_parameter_id`),
    CONSTRAINT `oas_request_parameter_oas_request_id_fk` FOREIGN KEY (`oas_request_id`) REFERENCES `oas_request` (`oas_request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_response_headers`;
CREATE TABLE `oas_response_headers`
(
    `oas_response_id`       bigint(20) unsigned NOT NULL COMMENT 'The primary key of the record.',
    `oas_http_header_id`    bigint(20) unsigned NOT NULL COMMENT 'The primary key of the record.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_response_id`, `oas_http_header_id`),
    KEY                     `oas_response_headers_oas_http_header_id_fk` (`oas_http_header_id`),
    KEY                     `oas_response_headers_created_by_fk` (`created_by`),
    KEY                     `oas_response_headers_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_response_headers_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_response_headers_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_response_headers_oas_http_header_id_fk` FOREIGN KEY (`oas_http_header_id`) REFERENCES `oas_http_header` (`oas_http_header_id`),
    CONSTRAINT `oas_response_headers_oas_response_id_fk` FOREIGN KEY (`oas_response_id`) REFERENCES `oas_response` (`oas_response_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_resource_tag`;
CREATE TABLE `oas_resource_tag`
(
    `oas_operation_id`      bigint(20) unsigned NOT NULL COMMENT 'The primary key of the record.',
    `oas_tag_id`            bigint(20) unsigned NOT NULL COMMENT 'The primary key of the record.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_operation_id`, `oas_tag_id`),
    KEY                     `oas_resource_tag_oas_tag_id_fk` (`oas_tag_id`),
    CONSTRAINT `oas_resource_tag_oas_operation_id_fk` FOREIGN KEY (`oas_operation_id`) REFERENCES `oas_operation` (`oas_operation_id`),
    CONSTRAINT `oas_resource_tag_oas_tag_id_fk` FOREIGN KEY (`oas_tag_id`) REFERENCES `oas_tag` (`oas_tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;