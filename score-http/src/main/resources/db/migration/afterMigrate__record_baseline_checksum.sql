-- Databases created before V3_4_0__baseline.sql existed were baselined at 3.4.0, so their history row is a
-- checksum-less '<< Flyway Baseline >>' marker. Turn that marker into the same record a fresh install has for
-- V3_4_0__baseline.sql, so the baseline is checksum-tracked on every database.
--
-- Safe to run repeatedly: it only touches a 3.4.0 row of type BASELINE and does nothing on fresh installs,
-- on databases without a history table yet, and once converted.
--
-- -246808805 is Flyway's checksum of V3_4_0__baseline.sql. That file must never be edited; if it ever is,
-- verify-flyway-baseline.sh fails until this value is updated too.
SET @sync_baseline = IF(
    (SELECT COUNT(*) FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'flyway_schema_history') > 0,
    "UPDATE `flyway_schema_history`
        SET `type` = 'SQL', `description` = 'baseline', `script` = 'V3_4_0__baseline.sql', `checksum` = -246808805
      WHERE `version` = '3.4.0' AND `type` = 'BASELINE'",
    'DO 0');
PREPARE sync_baseline FROM @sync_baseline;
EXECUTE sync_baseline;
DEALLOCATE PREPARE sync_baseline;
