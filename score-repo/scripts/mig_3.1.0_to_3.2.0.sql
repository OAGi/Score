-- ----------------------------------------------------
-- Migration script for Score v3.2.0                 --
--                                                   --
-- Author: Hakju Oh <hakju.oh@nist.gov>              --
-- ----------------------------------------------------

-- Change the data type of `configuration`.`value`
ALTER TABLE `configuration` MODIFY COLUMN `value` LONGTEXT DEFAULT NULL COMMENT 'The value of configuration property.';

-- Move `acc/ascc/bcc/asccp/bccp/dt`.`den` TO its manifest
ALTER TABLE `acc_manifest` ADD COLUMN `den` varchar(200) NOT NULL COMMENT 'DEN (dictionary entry name) of the ACC. It can be derived as OBJECT_CLASS_QUALIFIER + "_ " + OBJECT_CLASS_TERM + ". Details".' AFTER `based_acc_manifest_id`;
UPDATE `acc_manifest`, `acc` SET `acc_manifest`.`den` = `acc`.`den` WHERE `acc_manifest`.`acc_id` = `acc`.`acc_id`;

ALTER TABLE `ascc_manifest` ADD COLUMN `den` varchar(200) NOT NULL COMMENT 'DEN (dictionary entry name) of the ASCC. This column can be derived from Qualifier and OBJECT_CLASS_TERM of the FROM_ACC_ID and DEN of the TO_ASCCP_ID as Qualifier + "_ " + OBJECT_CLASS_TERM + ". " + DEN.' AFTER `to_asccp_manifest_id`;
UPDATE `ascc_manifest`, `ascc` SET `ascc_manifest`.`den` = `ascc`.`den` WHERE `ascc_manifest`.`ascc_id` = `ascc`.`ascc_id`;

ALTER TABLE `bcc_manifest` ADD COLUMN `den` varchar(200) NOT NULL COMMENT 'DEN (dictionary entry name) of the BCC. This column can be derived from QUALIFIER and OBJECT_CLASS_TERM of the FROM_ACC_ID and DEN of the TO_BCCP_ID as QUALIFIER + "_ " + OBJECT_CLASS_TERM + ". " + DEN.' AFTER `to_bccp_manifest_id`;
UPDATE `bcc_manifest`, `bcc` SET `bcc_manifest`.`den` = `bcc`.`den` WHERE `bcc_manifest`.`bcc_id` = `bcc`.`bcc_id`;

ALTER TABLE `asccp_manifest` ADD COLUMN `den` varchar(200) DEFAULT NULL COMMENT 'The dictionary entry name of the ASCCP.' AFTER `role_of_acc_manifest_id`;
UPDATE `asccp_manifest`, `asccp` SET `asccp_manifest`.`den` = `asccp`.`den` WHERE `asccp_manifest`.`asccp_id` = `asccp`.`asccp_id`;

ALTER TABLE `bccp_manifest` ADD COLUMN `den` varchar(200) NOT NULL COMMENT 'The dictionary entry name of the BCCP. It is derived by PROPERTY_TERM + ". " + REPRESENTATION_TERM.' AFTER `bdt_manifest_id`;
UPDATE `bccp_manifest`, `bccp` SET `bccp_manifest`.`den` = `bccp`.`den` WHERE `bccp_manifest`.`bccp_id` = `bccp`.`bccp_id`;

ALTER TABLE `dt_manifest` ADD COLUMN `den` varchar(200) NOT NULL COMMENT 'Dictionary Entry Name of the data type.' AFTER `based_dt_manifest_id`;
UPDATE `dt_manifest`, `dt` SET `dt_manifest`.`den` = `dt`.`den` WHERE `dt_manifest`.`dt_id` = `dt`.`dt_id`;

ALTER TABLE `acc` DROP COLUMN `den`;
ALTER TABLE `ascc` DROP COLUMN `den`;
ALTER TABLE `bcc` DROP COLUMN `den`;
ALTER TABLE `asccp` DROP COLUMN `den`;
ALTER TABLE `bccp` DROP COLUMN `den`;
ALTER TABLE `dt` DROP COLUMN `den`;