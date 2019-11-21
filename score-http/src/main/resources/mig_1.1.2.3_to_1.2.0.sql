-- ----------------------------------------------------
-- Migration script for Score v1.2.0                 --
--                                                   --
-- Author: Hakju Oh <hakju.oh@nist.gov>              --
--         Kwanghoon Lee <kwanghoon.lee@nist.gov>    --
--         Sofian Chouder <sofian.chouder@nist.gov>    --
-- ----------------------------------------------------

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE `app_user` CHANGE COLUMN `oagis_developer_indicator` `is_developer` tinyint(1);

-- Add `biz_ctx_assignment` table.
DROP TABLE IF EXISTS `biz_ctx_assignment`;
CREATE TABLE `biz_ctx_assignment` (
  `biz_ctx_assignment_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `biz_ctx_id` bigint(20) unsigned NOT NULL,
  `top_level_abie_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`biz_ctx_assignment_id`),
  UNIQUE KEY `biz_ctx_assignment_uk` (`biz_ctx_id`,`top_level_abie_id`),
  KEY `biz_ctx_id` (`biz_ctx_id`),
  KEY `top_level_abie_id` (`top_level_abie_id`),
  CONSTRAINT `biz_ctx_rule_ibfk_1` FOREIGN KEY (`biz_ctx_id`) REFERENCES `biz_ctx` (`biz_ctx_id`),
  CONSTRAINT `biz_ctx_rule_ibfk_2` FOREIGN KEY (`top_level_abie_id`) REFERENCES `top_level_abie` (`top_level_abie_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `abie` MODIFY COLUMN `biz_ctx_id` bigint(20) unsigned DEFAULT NULL COMMENT '(Deprecated) A foreign key to the BIZ_CTX table. This column stores the business context assigned to the ABIE.';

INSERT INTO `biz_ctx_assignment` (`top_level_abie_id`, `biz_ctx_id`)
SELECT `top_level_abie_id`, `abie`.`biz_ctx_id`
FROM `top_level_abie`
JOIN `abie` ON `top_level_abie`.`abie_id` = `abie`.`abie_id`
WHERE `abie`.`biz_ctx_id` IS NOT NULL;

-- Add `code_list_id` column on `ctx_scheme` table.
ALTER TABLE `ctx_scheme` ADD COLUMN `code_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is the foreign key to the CODE_LIST table. It identifies the code list associated with this context scheme.' AFTER `ctx_category_id`;
ALTER TABLE `ctx_scheme` ADD CONSTRAINT `ctx_scheme_code_list_id_fk` FOREIGN KEY (`code_list_id`) REFERENCES `code_list` (`code_list_id`);

-- Add `last_update_timestamp` and `last_updated_by` columns on `top_level_abie` table.
ALTER TABLE `top_level_abie`
ADD COLUMN `last_update_timestamp` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'The timestamp when among all related bie records was last updated.' AFTER `owner_user_id`,
ADD COLUMN `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated any related bie records.' AFTER `last_update_timestamp`,
ADD KEY `top_level_abie_last_updated_by_fk` (`last_updated_by`),
ADD CONSTRAINT `top_level_abie_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`);

UPDATE `top_level_abie` SET `last_updated_by` = `owner_user_id`;
UPDATE `top_level_abie`, (SELECT `top_level_abie`.`top_level_abie_id`, `abie`.`last_update_timestamp` FROM `abie` JOIN `top_level_abie` ON `abie`.`abie_id` = `top_level_abie`.`abie_id`) AS t SET `top_level_abie`.`last_update_timestamp` = t.`last_update_timestamp` WHERE `top_level_abie`.`top_level_abie_id` = t.`top_level_abie_id`;

SET FOREIGN_KEY_CHECKS = 1;