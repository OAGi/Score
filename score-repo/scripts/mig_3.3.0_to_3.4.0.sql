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
