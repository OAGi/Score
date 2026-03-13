-- ----------------------------------------------------
-- Migration script for Score v3.5.0                 --
--                                                   --
-- Author: Hakju Oh <hakju.oh@nist.gov>              --
-- ----------------------------------------------------

SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

-- Issue #1700: Add 'DataArea' type to ASCCP to hide 'Data Area' records in 'Browse Standards' mode.
UPDATE `asccp` SET `type` = 'DataArea' WHERE `property_term` = 'Data Area';

ALTER TABLE `configuration` AUTO_INCREMENT = 1;

INSERT IGNORE INTO `configuration` (`name`, `type`, `value`)
VALUES ('score.browse-standard-mode.enabled',
        'Boolean',
        'false');

-- Issue #1711: Add default filename expressions.
INSERT IGNORE INTO `configuration` (`name`, `type`, `value`)
VALUES ('score.bie.schema-filename-expression',
        'String',
        '{BIE Property Term:separator(\'-\')}(-{Business Context Name[0]?includeBusinessContext})(-{BIE Version?includeVersion:replace(\'\\\\.\', \'_\')})');

INSERT IGNORE INTO `configuration` (`name`, `type`, `value`)
VALUES ('score.bie.schema-filename-duplicate-handler-expression',
        'String',
        '-{Incremental}');

INSERT IGNORE INTO `configuration` (`name`, `type`, `value`)
VALUES ('score.bie.package-schema-filename-expression',
        'String',
        '{BIE Package Name}-{BIE Package Version ID}_{Business Context Names:replace(\'\\\\s+\', \'\'):separator(\'+\')}_{BIE Property Term}([{BIE Display Name}])(-{BIE Version})');

INSERT IGNORE INTO `configuration` (`name`, `type`, `value`)
VALUES ('score.bie.package-schema-filename-duplicate-handler-expression',
        'String',
        '~{BIE ID}');

-- Issue #1703: Support JSON Schema 2020-12 version.
ALTER TABLE `xbt`
    MODIFY COLUMN `jbt_draft05_map` VARCHAR(500) DEFAULT NULL
    COMMENT 'Mapping from XML built-in datatype to JSON Schema Draft-05 type definition (JSON text).',
    MODIFY COLUMN `openapi30_map` VARCHAR(500) DEFAULT NULL
    COMMENT 'Mapping from XML built-in datatype to OpenAPI 3.0.3 schema type definition (JSON text).',
    MODIFY COLUMN `avro_map` VARCHAR(500) DEFAULT NULL
    COMMENT 'Mapping from XML built-in datatype to Apache Avro schema type definition (JSON text).',
    MODIFY COLUMN `schema_definition` TEXT DEFAULT NULL
    COMMENT 'Schema definition text for the built-in type when applicable.',
    MODIFY COLUMN `revision_doc` TEXT DEFAULT NULL
    COMMENT 'Revision notes or documentation for this built-in type mapping.',
    MODIFY COLUMN `state` INT(11) DEFAULT NULL
    COMMENT 'Record lifecycle state code.',
    MODIFY COLUMN `is_deprecated` TINYINT(1) DEFAULT 0
    COMMENT 'Deprecation flag (1 deprecated, 0 active).',
    MODIFY COLUMN `created_by` BIGINT(20) UNSIGNED NOT NULL
    COMMENT 'User ID that created this record (FK to app_user.app_user_id).',
    MODIFY COLUMN `owner_user_id` BIGINT(20) UNSIGNED NOT NULL
    COMMENT 'Owner user ID for this record (FK to app_user.app_user_id).',
    MODIFY COLUMN `last_updated_by` BIGINT(20) UNSIGNED NOT NULL
    COMMENT 'User ID that last updated this record (FK to app_user.app_user_id).',
    MODIFY COLUMN `creation_timestamp` DATETIME(6) NOT NULL
    COMMENT 'Timestamp when this record was created.',
    MODIFY COLUMN `last_update_timestamp` DATETIME(6) NOT NULL
    COMMENT 'Timestamp when this record was last updated.';

-- ----------------------------------------------
-- Add JSON Schema 2020-12 version mapping values
-- ----------------------------------------------
ALTER TABLE `xbt`
    ADD COLUMN IF NOT EXISTS `jbt_202012_map` VARCHAR(500) DEFAULT NULL
    COMMENT 'Mapping from XML built-in datatype to JSON Schema 2020-12 type definition (JSON text).'
    AFTER `jbt_draft05_map`;

UPDATE `xbt` SET `jbt_draft05_map` = '{}' WHERE `builtIn_type` = 'xsd:anyType';

-- 1) Start from draft-05 mappings
UPDATE `xbt` SET `jbt_202012_map` = `jbt_draft05_map` WHERE `jbt_202012_map` IS NULL;

-- 2) Normalize integer family to 2020-12 style
UPDATE `xbt` SET `jbt_202012_map` = '{"type":"string","format":"uri-reference"}' WHERE `builtIn_type` = 'xsd:anyURI';

UPDATE `xbt` SET `jbt_202012_map` = '{"type":"integer","minimum":0}' WHERE `builtIn_type` = 'xsd:nonNegativeInteger';
UPDATE `xbt` SET `jbt_202012_map` = '{"type":"integer","minimum":1}' WHERE `builtIn_type` = 'xsd:positiveInteger';
UPDATE `xbt` SET `jbt_202012_map` = '{"type":"integer","maximum":0}' WHERE `builtIn_type` = 'xsd:nonPositiveInteger';
UPDATE `xbt` SET `jbt_202012_map` = '{"type":"integer","maximum":-1}' WHERE `builtIn_type` = 'xsd:negativeInteger';

UPDATE `xbt` SET `jbt_202012_map` = '{"type":"integer"}' WHERE `builtIn_type` = 'xsd:integer';
UPDATE `xbt` SET `jbt_202012_map` = '{"type":"integer","minimum":-9223372036854775808,"maximum":9223372036854775807}' WHERE `builtIn_type` = 'xsd:long';
UPDATE `xbt` SET `jbt_202012_map` = '{"type":"integer","minimum":-2147483648,"maximum":2147483647}' WHERE `builtIn_type` = 'xsd:int';
UPDATE `xbt` SET `jbt_202012_map` = '{"type":"integer","minimum":-32768,"maximum":32767}' WHERE `builtIn_type` = 'xsd:short';
UPDATE `xbt` SET `jbt_202012_map` = '{"type":"integer","minimum":-128,"maximum":127}' WHERE `builtIn_type` = 'xsd:byte';

UPDATE `xbt` SET `jbt_202012_map` = '{"type":"integer","minimum":0,"maximum":18446744073709551615}' WHERE `builtIn_type` = 'xsd:unsignedLong';
UPDATE `xbt` SET `jbt_202012_map` = '{"type":"integer","minimum":0,"maximum":4294967295}' WHERE `builtIn_type` = 'xsd:unsignedInt';
UPDATE `xbt` SET `jbt_202012_map` = '{"type":"integer","minimum":0,"maximum":65535}' WHERE `builtIn_type` = 'xsd:unsignedShort';
UPDATE `xbt` SET `jbt_202012_map` = '{"type":"integer","minimum":0,"maximum":255}' WHERE `builtIn_type` = 'xsd:unsignedByte';

UPDATE `xbt` SET `jbt_202012_map` = '{"type":"string","format":"date"}' WHERE `builtIn_type` = 'xsd:date';
UPDATE `xbt` SET `jbt_202012_map` = '{"type":"string","format":"time"}' WHERE `builtIn_type` = 'xsd:time';
UPDATE `xbt` SET `jbt_202012_map` = '{"type":"string","contentEncoding":"base64"}' WHERE `builtIn_type` = 'xsd:base64Binary';
UPDATE `xbt` SET `jbt_202012_map` = '{"type":"string","pattern":"^[0-9A-Fa-f]*$"}' WHERE `builtIn_type` = 'xsd:hexBinary';

-- -----------------------------------
-- Fix malformed OpenAPI 3.0.3 mapping
-- -----------------------------------
UPDATE `xbt` SET `openapi30_map` = '{}' WHERE `builtIn_type` = 'xsd:anyType';

UPDATE `xbt` SET `openapi30_map` = '{"type":"string", "format":"date-time"}'
WHERE `builtIn_type` IN ('xbt_DateHourMinuteType', 'xbt_DateHourMinuteUTCType', 'xbt_DateHourMinuteUTCOffsetType');

UPDATE `xbt` SET `openapi30_map` = '{"type":"string", "pattern":"^[0-9]{4}-W(0[1-9]|[1-4][0-9]|5[0123])-[1-7]T((([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9](|(\\.[0-9]+)))|(24:00:00))$"}'
WHERE `builtIn_type` IN ('xbt_YearWeekDayTimeType', 'xbt_YearWeekDayTimeUTCType', 'xbt_YearWeekDayTimeUTCOffsetType');

-- --------------------------------
-- Add OpenAPI 3.1.1 mapping values
-- --------------------------------
ALTER TABLE `xbt`
    ADD COLUMN IF NOT EXISTS `openapi31_map` VARCHAR(500) DEFAULT NULL
    COMMENT 'Mapping from XML built-in datatype to OpenAPI 3.1.1 schema type definition (JSON text).'
    AFTER `openapi30_map`;

-- 1) Initialize from existing OpenAPI 3.0 mappings
UPDATE `xbt` SET `openapi31_map` = `openapi30_map` WHERE `openapi31_map` IS NULL;

-- 2) OpenAPI 3.1 / JSON Schema 2020-12 compatibility fixes
UPDATE `xbt` SET `openapi31_map` = '{"type":"integer","minimum":0}' WHERE `builtIn_type` = 'xsd:nonNegativeInteger';
UPDATE `xbt` SET `openapi31_map` = '{"type":"integer","minimum":1}' WHERE `builtIn_type` = 'xsd:positiveInteger';
UPDATE `xbt` SET `openapi31_map` = '{"type":"integer","maximum":0}' WHERE `builtIn_type` = 'xsd:nonPositiveInteger';
UPDATE `xbt` SET `openapi31_map` = '{"type":"integer","maximum":-1}' WHERE `builtIn_type` = 'xsd:negativeInteger';

UPDATE `xbt` SET `openapi31_map` = '{"type":"integer","minimum":-9223372036854775808,"maximum":9223372036854775807}' WHERE `builtIn_type` = 'xsd:long';
UPDATE `xbt` SET `openapi31_map` = '{"type":"integer","minimum":0,"maximum":18446744073709551615}' WHERE `builtIn_type` = 'xsd:unsignedLong';

UPDATE `xbt` SET `openapi31_map` = '{"type":"string","contentEncoding":"base64"}' WHERE `builtIn_type` = 'xsd:base64Binary';
UPDATE `xbt` SET `openapi31_map` = '{"type":"string","pattern":"^[0-9A-Fa-f]*$"}' WHERE `builtIn_type` = 'xsd:hexBinary';

UPDATE `xbt` SET `openapi31_map` = '{"type":"string","pattern":"^(?:[01]\\\\d|2[0-3]):(?:[0-5]\\\\d):(?:[0-5]\\\\d)(?:\\\\.\\\\d+)?(?:Z|[+\\\\-](?:0\\\\d|1[0-4]):[0-5]\\\\d)?$"}' WHERE `builtIn_type` = 'xsd:time';

SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;