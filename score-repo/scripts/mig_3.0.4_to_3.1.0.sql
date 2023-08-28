-- ----------------------------------------------------
-- Migration script for Score v3.1.0                 --
--                                                   --
-- Author: Hakju Oh <hakju.oh@nist.gov>              --
-- ----------------------------------------------------

-- Add `xbt`.`avro_map` column for the issue #1500
ALTER TABLE `xbt` ADD COLUMN `avro_map` varchar(500) DEFAULT NULL AFTER `openapi30_map`;

UPDATE `xbt` SET `avro_map` = '{"type":"record"}' WHERE `xbt_id` = 1;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 2;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 3;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 4;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 5;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 6;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 7;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 8;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 9;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 10;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 11;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 12;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 13;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 14;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 15;
UPDATE `xbt` SET `avro_map` = '{"type":"boolean"}' WHERE `xbt_id` = 16;
UPDATE `xbt` SET `avro_map` = '{"type":"bytes"}' WHERE `xbt_id` = 17;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 18;
UPDATE `xbt` SET `avro_map` = '{"type":"float"}' WHERE `xbt_id` = 19;
UPDATE `xbt` SET `avro_map` = '{"type":"double"}' WHERE `xbt_id` = 20;
UPDATE `xbt` SET `avro_map` = '{"type":"integer"}' WHERE `xbt_id` = 21;
UPDATE `xbt` SET `avro_map` = '{"type":"integer"}' WHERE `xbt_id` = 22;
UPDATE `xbt` SET `avro_map` = '{"type":"integer"}' WHERE `xbt_id` = 23;
UPDATE `xbt` SET `avro_map` = '{"type":"double"}' WHERE `xbt_id` = 24;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 25;
UPDATE `xbt` SET `avro_map` = '{"type":"boolean"}' WHERE `xbt_id` = 26;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 27;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 28;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 29;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 30;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 31;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 32;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 33;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 34;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 35;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 36;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 37;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 38;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 39;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 40;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 41;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 42;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 43;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 44;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 45;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 46;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 47;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 48;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 49;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 50;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 51;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 52;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 53;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 54;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 55;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 56;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 57;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 58;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 59;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 60;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 61;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 62;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 63;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 64;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 65;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 66;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 67;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 68;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 69;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 70;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 71;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 72;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 73;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 74;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 75;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 76;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 77;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 78;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 79;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 80;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 81;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 82;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 83;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 84;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 85;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 86;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 87;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 88;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 89;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 90;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 91;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 92;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 93;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 94;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 95;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 96;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 97;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 98;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 99;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 100;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 101;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 102;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 103;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 104;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 105;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 106;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 107;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 108;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 109;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 110;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 111;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 112;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 113;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 114;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 115;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 116;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 117;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 118;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 119;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 120;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 121;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 122;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 123;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 124;
UPDATE `xbt` SET `avro_map` = '{"type":"string"}' WHERE `xbt_id` = 125;

-- Add `top_level_asbiep`.`source_top_level_asbiep_id`, `top_level_asbiep`.`source_source_action`, and `top_level_asbiep`.`source_timestamp`
ALTER TABLE `top_level_asbiep`
    ADD COLUMN `source_top_level_asbiep_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A foreign key referring to the source TOP_LEVEL_ASBIEP_ID which has linked to this record.',
    ADD COLUMN `source_action` varchar(20) DEFAULT NULL COMMENT 'An action that had used to create a reference from the source (e.g., ''Copy'' or ''Uplift''.)',
    ADD COLUMN `source_timestamp` datetime(6) DEFAULT NULL COMMENT 'A timestamp when a source reference had been made.',
    ADD CONSTRAINT `top_level_asbiep_source_top_level_asbiep_id_fk` FOREIGN KEY (`source_top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`);