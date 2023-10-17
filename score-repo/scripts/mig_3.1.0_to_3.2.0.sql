-- ----------------------------------------------------
-- Migration script for Score v3.2.0                 --
--                                                   --
-- Author: Hakju Oh <hakju.oh@nist.gov>              --
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