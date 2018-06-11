-- Add Columns on `xbt` table
ALTER TABLE `xbt` ADD `jbt_draft05_map` varchar (500);

UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 1;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 2;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string", "pattern":"^[-]?P(?!$)(?:\\d+Y)?(?:\\d+M)?(?:\\d+D)?(?:T(?!$)(?:\\d+H)?(?:\\d+M)?(?:\\d+(?:\\.\\d+)?S)?)?$"}' WHERE `xbt_id` = 3;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string", "format":"date-time"}' WHERE `xbt_id` = 4;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 5;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 6;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 7;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 8;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 9;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 10;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 11;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 12;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 13;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 14;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 15;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"boolean"}' WHERE `xbt_id` = 16;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 17;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 18;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"number"}' WHERE `xbt_id` = 19;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"number"}' WHERE `xbt_id` = 20;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"number", "multipleOf":1}' WHERE `xbt_id` = 21;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"integer", "minimum":0, "exclusiveMinimum":false}' WHERE `xbt_id` = 22;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"integer", "minimum":0, "exclusiveMinimum":true}' WHERE `xbt_id` = 23;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"number"}' WHERE `xbt_id` = 24;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string", "format":"uriref"}' WHERE `xbt_id` = 25;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"boolean"}' WHERE `xbt_id` = 26;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 27;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 28;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 29;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 30;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 31;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 32;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 33;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 34;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 35;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 36;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 37;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 38;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 39;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 40;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 41;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 42;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 43;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 44;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 45;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 46;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 47;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 48;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 49;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 50;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 51;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 52;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 53;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 54;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 55;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 56;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 57;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 58;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 59;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 60;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 61;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 62;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 63;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 64;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 65;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 66;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 67;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 68;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 69;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 70;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 71;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 72;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 73;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 74;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 75;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 76;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 77;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 78;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 79;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 80;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 81;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 82;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 83;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 84;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 85;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 86;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 87;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 88;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 89;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 90;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 91;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 92;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 93;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 94;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 95;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 96;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 97;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 98;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 99;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 100;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 101;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 102;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 103;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 104;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 105;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 106;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 107;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 108;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 109;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 110;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 111;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 112;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 113;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 114;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 115;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 116;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 117;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 118;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 119;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 120;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 121;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 122;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 123;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 124;
UPDATE `xbt` SET `jbt_draft05_map` = '{"type":"string"}' WHERE `xbt_id` = 125;

-- Add Columns on MODULE table
ALTER TABLE `module` ADD COLUMN (
  `created_by` bigint(20) unsigned COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this MODULE.',
  `last_updated_by` bigint(20) unsigned COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record. \n\nIn the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).',
  `owner_user_id` bigint(20) unsigned COMMENT 'Foreign key to the APP_USER table identifying the user who can update or delete the record.',
  `creation_timestamp` datetime(6) COMMENT 'The timestamp when the record was first created.',
  `last_update_timestamp` datetime(6) COMMENT 'The timestamp when the record was last updated.',
  KEY `module_owner_user_id_fk` (`owner_user_id`),
  KEY `module_created_by_fk` (`created_by`),
  KEY `module_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `module_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`)
);

-- Update data on MODULE table
UPDATE `module` SET `created_by` = (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'oagis'), `last_updated_by` = (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'oagis'), `owner_user_id` = (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'oagis'), `creation_timestamp` = CURRENT_TIMESTAMP(6), `last_update_timestamp` = CURRENT_TIMESTAMP(6);

ALTER TABLE `module` MODIFY COLUMN `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this MODULE.';
ALTER TABLE `module` MODIFY COLUMN `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record. \n\nIn the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).';
ALTER TABLE `module` MODIFY COLUMN `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table identifying the user who can update or delete the record.';
ALTER TABLE `module` MODIFY COLUMN `creation_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.';
ALTER TABLE `module` MODIFY COLUMN `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.';

-- Create indices for Core Component GUIDs
CREATE INDEX `acc_guid_idx` ON `acc` (`guid` ASC);
CREATE INDEX `ascc_guid_idx` ON `ascc` (`guid` ASC);
CREATE INDEX `asccp_guid_idx` ON `asccp` (`guid` ASC);
CREATE INDEX `bcc_guid_idx` ON `bcc` (`guid` ASC);
CREATE INDEX `bccp_guid_idx` ON `bccp` (`guid` ASC);

COMMIT;