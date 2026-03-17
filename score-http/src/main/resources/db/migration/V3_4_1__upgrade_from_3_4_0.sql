-- ----------------------------------------------------
-- Migration script for Score v3.4.1                 --
--                                                   --
-- Author: Hakju Oh <hakju.oh@nist.gov>              --
-- ----------------------------------------------------

SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

-- Issue #1668
ALTER TABLE `library`
    ADD COLUMN IF NOT EXISTS `is_default` TINYINT(1) DEFAULT 0 COMMENT 'Indicates if the library is the default (0 = False, 1 = True). The default library is shown first if the user has no preference.' AFTER `is_read_only`;

UPDATE `library` SET `is_default` = 1 WHERE `name` = 'connectSpec';
UPDATE `library` SET `is_default` = 0 WHERE `name` <> 'connectSpec';

-- Issue #1677
ALTER TABLE `bie_package`
    ADD COLUMN IF NOT EXISTS `name` VARCHAR(200) NOT NULL
    COMMENT 'A text field used for containing the package name.'
        AFTER `library_id`;

UPDATE `bie_package` SET `name` = `version_name` WHERE `name` IS NULL OR `name` = '';

ALTER TABLE `bie_package` ADD UNIQUE KEY IF NOT EXISTS `bie_package_name_version_uk` (`name`, `version_id`, `version_name`);

SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;