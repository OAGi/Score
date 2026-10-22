-- ----------------------------------------------------
-- Migration script for Score v3.6.0                 --
--                                                   --
-- Author: Hakju Oh <hakju.oh@nist.gov>              --
-- ----------------------------------------------------

SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

-- The DROP ... IF EXISTS statements below make the script safe to run again. On a database that never had these objects
-- each one raises a note ("Unknown table", "Trigger does not exist"), which Flyway logs as a warning, so notes are off.
SET @OLD_SQL_NOTES = @@SQL_NOTES;
SET SQL_NOTES = 0;

-- ------------------------------------------------------------------
-- Passkey (WebAuthn) sign-in
-- ------------------------------------------------------------------
--
-- Passkeys let a local account sign in with a fingerprint, face or screen lock instead of a password.
--
-- `app_webauthn_user` is the WebAuthn identity (user handle) of an `app_user`; `webauthn_credential` holds the
-- passkeys, many per user. The user handle is random and opaque, never derived from the login ID or email.
--
-- These tables may already exist on a database that ran a development build, with rows in them. They are dropped
-- and created again, so any passkey registered by such a build is lost and has to be registered again. The
-- credential table goes first because it refers to the user table.
DROP TABLE IF EXISTS `webauthn_credential`;
DROP TABLE IF EXISTS `app_webauthn_user`;

CREATE TABLE `app_webauthn_user`
(
    `app_webauthn_user_id`  bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
    `app_user_id`           bigint(20) unsigned NOT NULL COMMENT 'A reference to the record in `app_user`.',
    `user_handle`           varbinary(64)       NOT NULL COMMENT 'Opaque random WebAuthn user handle (max 64 bytes per the spec). It is the only user identifier the authenticator stores, so it must not be derived from login_id or email.',
    `creation_timestamp`    datetime(6)         NOT NULL COMMENT 'Timestamp when this record is created.',
    `last_update_timestamp` datetime(6)         NOT NULL COMMENT 'Timestamp when this record is last updated.',
    PRIMARY KEY (`app_webauthn_user_id`),
    UNIQUE KEY `app_webauthn_user_uk1` (`user_handle`),
    KEY `app_webauthn_user_app_user_id_fk` (`app_user_id`),
    CONSTRAINT `app_webauthn_user_app_user_id_fk` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = DYNAMIC COMMENT ='WebAuthn identity (user handle) of an app_user; created on the first passkey registration.';

CREATE TABLE `webauthn_credential`
(
    `webauthn_credential_id`       bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
    `app_webauthn_user_id`         bigint(20) unsigned NOT NULL COMMENT 'A reference to the record in `app_webauthn_user`; the owner of the passkey.',
    `credential_id`                varbinary(1023)     NOT NULL COMMENT 'The authenticator-assigned credential ID (max 1023 bytes per the spec). Globally unique.',
    `rp_id`                        varchar(255)        NOT NULL COMMENT 'The relying party ID (domain) this passkey was registered for. A passkey only works while the configured RP ID matches; used to find and clean up passkeys after a domain change.',
    `public_key`                   blob                NOT NULL COMMENT 'The credential public key (COSE-encoded).',
    `signature_count`              bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT 'Last seen authenticator signature counter, for information only. Sign-in compares the counter in attestation_object, not this value, so cloned authenticators are not detected. Synced passkeys commonly keep it at 0.',
    `uv_initialized`               tinyint(1)          NOT NULL DEFAULT 0 COMMENT 'Whether user verification (PIN/biometric) was performed at registration.',
    `backup_eligible`              tinyint(1)          NOT NULL DEFAULT 0 COMMENT 'WebAuthn BE flag: the credential may be synced/backed up (a multi-device passkey).',
    `backup_state`                 tinyint(1)          NOT NULL DEFAULT 0 COMMENT 'WebAuthn BS flag: the credential is currently backed up.',
    `transports`                   varchar(255)                 DEFAULT NULL COMMENT 'Comma-separated authenticator transports (usb, nfc, ble, hybrid, internal), used as a sign-in hint.',
    `credential_type`              varchar(32)         NOT NULL DEFAULT 'public-key' COMMENT 'The PublicKeyCredential type.',
    `attestation_object`           blob                         DEFAULT NULL COMMENT 'The attestation object returned at registration. Required for sign-in: the credential public key and the previous counter are read from it when an assertion is verified.',
    `attestation_client_data_json` blob                         DEFAULT NULL COMMENT 'The clientDataJSON returned at registration, kept for auditing.',
    `label`                        varchar(100)                 DEFAULT NULL COMMENT 'User-given name of the passkey, e.g. "MacBook Touch ID".',
    `last_used_timestamp`          datetime(6)                  DEFAULT NULL COMMENT 'Timestamp when this passkey was last used to sign in; NULL until it is used.',
    `last_used_user_agent`         varchar(512)                 DEFAULT NULL COMMENT 'The User-Agent header of the request that last used this passkey.',
    `creation_timestamp`           datetime(6)         NOT NULL COMMENT 'Timestamp when this passkey is registered.',
    `last_update_timestamp`        datetime(6)         NOT NULL COMMENT 'Timestamp when this record is last updated (rename, or authenticator data refreshed after a sign-in).',
    PRIMARY KEY (`webauthn_credential_id`),
    UNIQUE KEY `webauthn_credential_uk1` (`credential_id`),
    KEY `webauthn_credential_app_webauthn_user_id_fk` (`app_webauthn_user_id`),
    CONSTRAINT `webauthn_credential_app_webauthn_user_id_fk` FOREIGN KEY (`app_webauthn_user_id`) REFERENCES `app_webauthn_user` (`app_webauthn_user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = DYNAMIC COMMENT ='Registered passkeys (WebAuthn credentials) of local accounts.';

-- ------------------------------------------------------------------
-- Query optimization: default primitive lookup indexes
-- ------------------------------------------------------------------

ALTER TABLE `dt_awd_pri`
    ADD INDEX `dt_awd_pri_dt_id_release_id_is_default_idx` (`dt_id`, `release_id`, `is_default`);

ALTER TABLE `dt_sc_awd_pri`
    ADD INDEX `dt_sc_awd_pri_dt_sc_id_release_id_is_default_idx` (`dt_sc_id`, `release_id`, `is_default`);

-- ------------------------------------------------------------------
-- Query optimization: faster `levenshtein` function
-- ------------------------------------------------------------------

-- `levenshtein` scores the fuzzy search of the CC list (the `score` column is computed for every matching row), and
-- the cell-by-cell loop of the previous version cost about 0.8 ms per call. The result is unchanged for every input;
-- only the way the distance is computed differs. The first branch that applies is used:
--
--   1. Equal strings, or an empty string: the distance is 0, or the length of the other string.
--   2. One string contains the other: the distance is exactly the difference of the lengths (delete the surplus
--      characters), so no calculation is needed. This is the usual case, since the search itself is a
--      "contains" match on the same text.
--   3. Both strings are ASCII (LENGTH = CHAR_LENGTH) and the shorter one has at most 62 characters: the
--      Myers/Hyyro bit-parallel algorithm. The shorter string is the pattern with one bit per character, the other
--      string is read once, and the distance is kept in two 64-bit vertical-delta vectors, so the work is
--      proportional to the length of the longer string instead of the product of both lengths. The bit mask of each
--      pattern character is kept in a 128 x 8 byte table (VARBINARY) indexed by the character code. Both strings are
--      lower-cased first, which keeps the comparison case-insensitive as before. 62 characters keep the addition
--      in the algorithm below 2^64.
--   4. Anything else (non-ASCII text, or a pattern longer than 62 characters): the dynamic-programming loop of the
--      previous version, with the work of one cell done in a single SET (LEAST of the three neighbours, with ORD()
--      and CHAR() instead of CONV(HEX(...)) and UNHEX(HEX(...))).
--
-- Measured with the same inputs against the previous version: about 6 times faster for a typo-like query (3,000
-- calls: 2.3 s -> 0.38 s), about 5 times faster for the page query of a one-word search ("Identifier": 1.1 s ->
-- 0.2 s), and about 2 times faster on the fallback path. Equal results were checked on 22,860 pairs of real names
-- and edge cases (empty, 62/63 characters, repeated characters, mixed case, reversed arguments).
--
-- The function is created under the same session settings as in the baseline, because the SQL mode and the client
-- character sets are stored with the routine.

SET @OLD_SQL_MODE_LEVENSHTEIN = @@SQL_MODE,
    @OLD_CHARACTER_SET_CLIENT_LEVENSHTEIN = @@character_set_client,
    @OLD_CHARACTER_SET_RESULTS_LEVENSHTEIN = @@character_set_results,
    @OLD_COLLATION_CONNECTION_LEVENSHTEIN = @@collation_connection;
SET SQL_MODE = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION';
SET character_set_client = utf8mb4;
SET character_set_results = utf8mb4;
SET collation_connection = utf8mb4_general_ci;

DROP FUNCTION IF EXISTS `levenshtein`;

DELIMITER ;;
CREATE FUNCTION `levenshtein`(s1 VARCHAR(255), s2 VARCHAR(255)) RETURNS int(11)
    DETERMINISTIC
BEGIN
    DECLARE s1_len, s2_len, i, j, c, score, m, n, pos INT;
    DECLARE s1_char CHAR;
    DECLARE cv0, cv1 VARBINARY(256);
    DECLARE pat, txt VARBINARY(255);
    DECLARE tbl VARBINARY(1024);
    DECLARE eq, pv, mv, xv, xh, ph, mh, msk, hb BIGINT UNSIGNED;
    SET s1_len = CHAR_LENGTH(s1), s2_len = CHAR_LENGTH(s2), cv1 = 0x00, j = 1, i = 1, c = 0;
    IF s1 = s2 THEN
        RETURN 0;
    ELSEIF s1_len = 0 THEN
        RETURN s2_len;
    ELSEIF s2_len = 0 THEN
        RETURN s1_len;
    ELSEIF s1_len >= s2_len AND INSTR(s1, s2) > 0 THEN
        RETURN s1_len - s2_len;
    ELSEIF s2_len > s1_len AND INSTR(s2, s1) > 0 THEN
        RETURN s2_len - s1_len;
    ELSEIF LEAST(s1_len, s2_len) <= 62 AND LENGTH(s1) = s1_len AND LENGTH(s2) = s2_len THEN
        -- Myers/Hyyro bit-parallel edit distance: the shorter string is the pattern (one bit per character).
        IF s1_len <= s2_len THEN
            SET pat = CAST(LOWER(s1) AS BINARY), txt = CAST(LOWER(s2) AS BINARY), m = s1_len, n = s2_len;
        ELSE
            SET pat = CAST(LOWER(s2) AS BINARY), txt = CAST(LOWER(s1) AS BINARY), m = s2_len, n = s1_len;
        END IF;
        -- tbl: 8-byte bit mask of the pattern positions of each character code
        SET tbl = REPEAT(CHAR(0), 1024);
        WHILE j <= m DO
            SET pos = ORD(SUBSTRING(pat, j, 1)) * 8 + 1,
                tbl = INSERT(tbl, pos, 8, UNHEX(LPAD(HEX(
                    CAST(CONV(HEX(SUBSTRING(tbl, pos, 8)), 16, 10) AS UNSIGNED) | (1 << (j - 1))), 16, '0'))),
                j = j + 1;
        END WHILE;
        SET msk = (1 << m) - 1, hb = 1 << (m - 1), pv = msk, mv = 0, score = m;
        WHILE i <= n DO
            SET eq = CAST(CONV(HEX(SUBSTRING(tbl, ORD(SUBSTRING(txt, i, 1)) * 8 + 1, 8)), 16, 10) AS UNSIGNED),
                xv = eq | mv,
                xh = (((eq & pv) + pv) ^ pv) | eq,
                ph = mv | ~(xh | pv),
                mh = pv & xh,
                score = score + ((ph & hb) <> 0) - ((mh & hb) <> 0),
                ph = ((ph << 1) | 1) & msk,
                mh = (mh << 1) & msk,
                pv = (mh | ~(xv | ph)) & msk,
                mv = ph & xv,
                i = i + 1;
        END WHILE;
        RETURN score;
    ELSE
        -- Dynamic programming over the two strings, one row at a time (cv1: previous row, cv0: current row).
        WHILE j <= s2_len DO
            SET cv1 = CONCAT(cv1, CHAR(j)), j = j + 1;
        END WHILE;
        WHILE i <= s1_len DO
            SET s1_char = SUBSTRING(s1, i, 1), c = i, cv0 = CHAR(i), j = 1;
            WHILE j <= s2_len DO
                SET c = LEAST(c + 1,
                              ORD(SUBSTRING(cv1, j, 1)) + (s1_char <> SUBSTRING(s2, j, 1)),
                              ORD(SUBSTRING(cv1, j + 1, 1)) + 1),
                    cv0 = CONCAT(cv0, CHAR(c)), j = j + 1;
            END WHILE;
            SET cv1 = cv0, i = i + 1;
        END WHILE;
        RETURN c;
    END IF;
END ;;
DELIMITER ;

SET SQL_MODE = @OLD_SQL_MODE_LEVENSHTEIN,
    character_set_client = @OLD_CHARACTER_SET_CLIENT_LEVENSHTEIN,
    character_set_results = @OLD_CHARACTER_SET_RESULTS_LEVENSHTEIN,
    collation_connection = @OLD_COLLATION_CONNECTION_LEVENSHTEIN;

-- ------------------------------------------------------------------
-- cc_change_log: what changed in the data core component records are made of
-- ------------------------------------------------------------------
--
-- The cache of core component records (see CcChangeFeed in score-http) must forget a record when the data it was made of
-- changes. The triggers below (named `<table>_cc_change_<event>_trg`) write one row here, in the transaction of the change,
-- for every row that changes in a table those records are made of, whoever makes the change (the application, a script, a
-- person with a SQL client). Every instance reads the rows and deletes the old ones; nothing else uses them.
--
-- This is TEMPORARY data. The application deletes the rows older than two hours (retention policy, checked every ten
-- minutes), and when the table has stayed empty for ten minutes it starts the numbering of CC_CHANGE_LOG_ID again from 1
-- (`ALTER TABLE ... AUTO_INCREMENT = 1`), so that the numbers stay small. If change data capture (CDC) of the binary log is
-- introduced to tell the cache what changed, this table and its triggers can be dropped.
--
-- Which triggers write rows (KIND is what changed; TARGET_ID the manifest id, or the id of the release, library, user or tag):
--   1. acc_manifest, ascc_manifest, bcc_manifest, asccp_manifest, bccp_manifest, dt_manifest, dt_sc_manifest:
--      INSERT, UPDATE, DELETE. One row for the manifest (kind ACC, ASCC, BCC, ASCCP, BCCP, DT, DT_SC). PARENT_MANIFEST_ID
--      is the ACC of an ASCC or a BCC and the DT of a DT_SC, whose list of children changes with it.
--   2. acc, ascc, bcc, asccp, bccp, dt, dt_sc: UPDATE, DELETE. One row for every manifest, in every release, that shows the
--      changed row (none for a row that no manifest shows).
--   3. seq_key (the order of the associations of an ACC): INSERT, UPDATE, DELETE. One row of kind ACC for the ACC.
--   4. acc_manifest_tag, asccp_manifest_tag, bccp_manifest_tag, dt_manifest_tag: INSERT, DELETE. One row for the manifest.
--   5. tag, release, library: UPDATE, DELETE. One row of kind TAG, RELEASE, LIBRARY (everything that shows it is forgotten).
--   6. release_dep: INSERT, UPDATE, DELETE. One row of kind RELEASE_DEP (the releases a document answers for).
--   7. app_user: UPDATE, only when LOGIN_ID or IS_DEVELOPER changes. One row of kind USER (the records show the owner).
--
-- Creating a trigger needs the TRIGGER privilege, and, when the binary log is on, also SUPER or
-- log_bin_trust_function_creators=1.

DROP TABLE IF EXISTS `cc_change_log`;

CREATE TABLE `cc_change_log`
(
    `cc_change_log_id`    bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key. Given when a row is written, but rows become visible in the order of their transactions'' commits, not of this number. Deleting rows does not lower it; the application starts the numbering again from 1 when the table has stayed empty for 10 minutes.',
    `kind`                varchar(16)         NOT NULL COMMENT 'What changed: ACC, ASCC, BCC, ASCCP, BCCP, DT or DT_SC (a manifest of that kind; written by the triggers on the manifest tables, on the component tables, on seq_key (always ACC) and on the manifest tag tables (ACC, ASCCP, BCCP, DT)); RELEASE (trigger on release), RELEASE_DEP (release_dep), LIBRARY (library), USER (app_user, when login_id or is_developer changes) or TAG (tag).',
    `target_id`            bigint(20) unsigned NOT NULL COMMENT 'The manifest id of the changed record (kind ACC to DT_SC), or the id of the release (RELEASE; for RELEASE_DEP the release that depends on others), the library (LIBRARY), the user (USER) or the tag (TAG) that changed.',
    `parent_manifest_id`  bigint(20) unsigned          DEFAULT NULL COMMENT 'The ACC_MANIFEST_ID that owns the changed ASCC or BCC, or the DT_MANIFEST_ID that owns the changed DT_SC; NULL for the other kinds.',
    `release_id`          bigint(20) unsigned          DEFAULT NULL COMMENT 'The release of the changed record, when it is known. No foreign key: a release may be deleted while its rows are still here.',
    `creation_timestamp`  datetime(6)         NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Timestamp when this record is created. Rows older than two hours are deleted by the application.',
    PRIMARY KEY (`cc_change_log_id`),
    KEY `cc_change_log_creation_timestamp_idx` (`creation_timestamp`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = DYNAMIC COMMENT ='Temporary log of the changes to the data core component records are made of, read by the CC cache (CcChangeFeed) to forget what changed; can be dropped with its triggers if CDC is introduced. Rows are deleted by the application after 2 hours (retention policy, checked every 10 minutes) and the numbering restarts from 1 once the table has stayed empty for 10 minutes. Rows are written only by AFTER INSERT/UPDATE/DELETE triggers named <table>_cc_change_<insert|update|delete>_trg, in the transaction of the change (a rollback removes them), whoever makes the change: (1) acc_manifest, ascc_manifest, bcc_manifest, asccp_manifest, bccp_manifest, dt_manifest, dt_sc_manifest on INSERT, UPDATE and DELETE: one row per changed manifest; (2) acc, ascc, bcc, asccp, bccp, dt, dt_sc on UPDATE and DELETE: one row for every manifest, in every release, that shows the changed row; (3) seq_key on INSERT, UPDATE, DELETE: one row of kind ACC for the ACC that owns the order; (4) acc_manifest_tag, asccp_manifest_tag, bccp_manifest_tag, dt_manifest_tag on INSERT and DELETE: one row for the tagged manifest; (5) tag, release, library on UPDATE and DELETE: one row of kind TAG, RELEASE, LIBRARY; (6) release_dep on INSERT, UPDATE, DELETE: one row of kind RELEASE_DEP; (7) app_user on UPDATE when login_id or is_developer changes: one row of kind USER.';

DROP TRIGGER IF EXISTS `acc_manifest_cc_change_insert_trg`;
CREATE TRIGGER `acc_manifest_cc_change_insert_trg` AFTER INSERT ON `acc_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('ACC', NEW.`acc_manifest_id`, NULL, NEW.`release_id`);

DROP TRIGGER IF EXISTS `acc_manifest_cc_change_update_trg`;
CREATE TRIGGER `acc_manifest_cc_change_update_trg` AFTER UPDATE ON `acc_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('ACC', NEW.`acc_manifest_id`, NULL, NEW.`release_id`);

DROP TRIGGER IF EXISTS `acc_manifest_cc_change_delete_trg`;
CREATE TRIGGER `acc_manifest_cc_change_delete_trg` AFTER DELETE ON `acc_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('ACC', OLD.`acc_manifest_id`, NULL, OLD.`release_id`);

DROP TRIGGER IF EXISTS `asccp_manifest_cc_change_insert_trg`;
CREATE TRIGGER `asccp_manifest_cc_change_insert_trg` AFTER INSERT ON `asccp_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('ASCCP', NEW.`asccp_manifest_id`, NULL, NEW.`release_id`);

DROP TRIGGER IF EXISTS `asccp_manifest_cc_change_update_trg`;
CREATE TRIGGER `asccp_manifest_cc_change_update_trg` AFTER UPDATE ON `asccp_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('ASCCP', NEW.`asccp_manifest_id`, NULL, NEW.`release_id`);

DROP TRIGGER IF EXISTS `asccp_manifest_cc_change_delete_trg`;
CREATE TRIGGER `asccp_manifest_cc_change_delete_trg` AFTER DELETE ON `asccp_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('ASCCP', OLD.`asccp_manifest_id`, NULL, OLD.`release_id`);

DROP TRIGGER IF EXISTS `bccp_manifest_cc_change_insert_trg`;
CREATE TRIGGER `bccp_manifest_cc_change_insert_trg` AFTER INSERT ON `bccp_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('BCCP', NEW.`bccp_manifest_id`, NULL, NEW.`release_id`);

DROP TRIGGER IF EXISTS `bccp_manifest_cc_change_update_trg`;
CREATE TRIGGER `bccp_manifest_cc_change_update_trg` AFTER UPDATE ON `bccp_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('BCCP', NEW.`bccp_manifest_id`, NULL, NEW.`release_id`);

DROP TRIGGER IF EXISTS `bccp_manifest_cc_change_delete_trg`;
CREATE TRIGGER `bccp_manifest_cc_change_delete_trg` AFTER DELETE ON `bccp_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('BCCP', OLD.`bccp_manifest_id`, NULL, OLD.`release_id`);

DROP TRIGGER IF EXISTS `dt_manifest_cc_change_insert_trg`;
CREATE TRIGGER `dt_manifest_cc_change_insert_trg` AFTER INSERT ON `dt_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('DT', NEW.`dt_manifest_id`, NULL, NEW.`release_id`);

DROP TRIGGER IF EXISTS `dt_manifest_cc_change_update_trg`;
CREATE TRIGGER `dt_manifest_cc_change_update_trg` AFTER UPDATE ON `dt_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('DT', NEW.`dt_manifest_id`, NULL, NEW.`release_id`);

DROP TRIGGER IF EXISTS `dt_manifest_cc_change_delete_trg`;
CREATE TRIGGER `dt_manifest_cc_change_delete_trg` AFTER DELETE ON `dt_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('DT', OLD.`dt_manifest_id`, NULL, OLD.`release_id`);

DROP TRIGGER IF EXISTS `ascc_manifest_cc_change_insert_trg`;
CREATE TRIGGER `ascc_manifest_cc_change_insert_trg` AFTER INSERT ON `ascc_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('ASCC', NEW.`ascc_manifest_id`, NEW.`from_acc_manifest_id`, NEW.`release_id`);

DROP TRIGGER IF EXISTS `ascc_manifest_cc_change_update_trg`;
CREATE TRIGGER `ascc_manifest_cc_change_update_trg` AFTER UPDATE ON `ascc_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('ASCC', NEW.`ascc_manifest_id`, NEW.`from_acc_manifest_id`, NEW.`release_id`);

DROP TRIGGER IF EXISTS `ascc_manifest_cc_change_delete_trg`;
CREATE TRIGGER `ascc_manifest_cc_change_delete_trg` AFTER DELETE ON `ascc_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('ASCC', OLD.`ascc_manifest_id`, OLD.`from_acc_manifest_id`, OLD.`release_id`);

DROP TRIGGER IF EXISTS `bcc_manifest_cc_change_insert_trg`;
CREATE TRIGGER `bcc_manifest_cc_change_insert_trg` AFTER INSERT ON `bcc_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('BCC', NEW.`bcc_manifest_id`, NEW.`from_acc_manifest_id`, NEW.`release_id`);

DROP TRIGGER IF EXISTS `bcc_manifest_cc_change_update_trg`;
CREATE TRIGGER `bcc_manifest_cc_change_update_trg` AFTER UPDATE ON `bcc_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('BCC', NEW.`bcc_manifest_id`, NEW.`from_acc_manifest_id`, NEW.`release_id`);

DROP TRIGGER IF EXISTS `bcc_manifest_cc_change_delete_trg`;
CREATE TRIGGER `bcc_manifest_cc_change_delete_trg` AFTER DELETE ON `bcc_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('BCC', OLD.`bcc_manifest_id`, OLD.`from_acc_manifest_id`, OLD.`release_id`);

DROP TRIGGER IF EXISTS `dt_sc_manifest_cc_change_insert_trg`;
CREATE TRIGGER `dt_sc_manifest_cc_change_insert_trg` AFTER INSERT ON `dt_sc_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('DT_SC', NEW.`dt_sc_manifest_id`, NEW.`owner_dt_manifest_id`, NEW.`release_id`);

DROP TRIGGER IF EXISTS `dt_sc_manifest_cc_change_update_trg`;
CREATE TRIGGER `dt_sc_manifest_cc_change_update_trg` AFTER UPDATE ON `dt_sc_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('DT_SC', NEW.`dt_sc_manifest_id`, NEW.`owner_dt_manifest_id`, NEW.`release_id`);

DROP TRIGGER IF EXISTS `dt_sc_manifest_cc_change_delete_trg`;
CREATE TRIGGER `dt_sc_manifest_cc_change_delete_trg` AFTER DELETE ON `dt_sc_manifest` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('DT_SC', OLD.`dt_sc_manifest_id`, OLD.`owner_dt_manifest_id`, OLD.`release_id`);

DROP TRIGGER IF EXISTS `acc_cc_change_update_trg`;
CREATE TRIGGER `acc_cc_change_update_trg` AFTER UPDATE ON `acc` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`)
    SELECT 'ACC', `acc_manifest_id`, NULL, `release_id` FROM `acc_manifest` WHERE `acc_id` = NEW.`acc_id`;

DROP TRIGGER IF EXISTS `acc_cc_change_delete_trg`;
CREATE TRIGGER `acc_cc_change_delete_trg` AFTER DELETE ON `acc` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`)
    SELECT 'ACC', `acc_manifest_id`, NULL, `release_id` FROM `acc_manifest` WHERE `acc_id` = OLD.`acc_id`;

DROP TRIGGER IF EXISTS `asccp_cc_change_update_trg`;
CREATE TRIGGER `asccp_cc_change_update_trg` AFTER UPDATE ON `asccp` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`)
    SELECT 'ASCCP', `asccp_manifest_id`, NULL, `release_id` FROM `asccp_manifest` WHERE `asccp_id` = NEW.`asccp_id`;

DROP TRIGGER IF EXISTS `asccp_cc_change_delete_trg`;
CREATE TRIGGER `asccp_cc_change_delete_trg` AFTER DELETE ON `asccp` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`)
    SELECT 'ASCCP', `asccp_manifest_id`, NULL, `release_id` FROM `asccp_manifest` WHERE `asccp_id` = OLD.`asccp_id`;

DROP TRIGGER IF EXISTS `bccp_cc_change_update_trg`;
CREATE TRIGGER `bccp_cc_change_update_trg` AFTER UPDATE ON `bccp` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`)
    SELECT 'BCCP', `bccp_manifest_id`, NULL, `release_id` FROM `bccp_manifest` WHERE `bccp_id` = NEW.`bccp_id`;

DROP TRIGGER IF EXISTS `bccp_cc_change_delete_trg`;
CREATE TRIGGER `bccp_cc_change_delete_trg` AFTER DELETE ON `bccp` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`)
    SELECT 'BCCP', `bccp_manifest_id`, NULL, `release_id` FROM `bccp_manifest` WHERE `bccp_id` = OLD.`bccp_id`;

DROP TRIGGER IF EXISTS `dt_cc_change_update_trg`;
CREATE TRIGGER `dt_cc_change_update_trg` AFTER UPDATE ON `dt` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`)
    SELECT 'DT', `dt_manifest_id`, NULL, `release_id` FROM `dt_manifest` WHERE `dt_id` = NEW.`dt_id`;

DROP TRIGGER IF EXISTS `dt_cc_change_delete_trg`;
CREATE TRIGGER `dt_cc_change_delete_trg` AFTER DELETE ON `dt` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`)
    SELECT 'DT', `dt_manifest_id`, NULL, `release_id` FROM `dt_manifest` WHERE `dt_id` = OLD.`dt_id`;

DROP TRIGGER IF EXISTS `ascc_cc_change_update_trg`;
CREATE TRIGGER `ascc_cc_change_update_trg` AFTER UPDATE ON `ascc` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`)
    SELECT 'ASCC', `ascc_manifest_id`, `from_acc_manifest_id`, `release_id` FROM `ascc_manifest` WHERE `ascc_id` = NEW.`ascc_id`;

DROP TRIGGER IF EXISTS `ascc_cc_change_delete_trg`;
CREATE TRIGGER `ascc_cc_change_delete_trg` AFTER DELETE ON `ascc` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`)
    SELECT 'ASCC', `ascc_manifest_id`, `from_acc_manifest_id`, `release_id` FROM `ascc_manifest` WHERE `ascc_id` = OLD.`ascc_id`;

DROP TRIGGER IF EXISTS `bcc_cc_change_update_trg`;
CREATE TRIGGER `bcc_cc_change_update_trg` AFTER UPDATE ON `bcc` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`)
    SELECT 'BCC', `bcc_manifest_id`, `from_acc_manifest_id`, `release_id` FROM `bcc_manifest` WHERE `bcc_id` = NEW.`bcc_id`;

DROP TRIGGER IF EXISTS `bcc_cc_change_delete_trg`;
CREATE TRIGGER `bcc_cc_change_delete_trg` AFTER DELETE ON `bcc` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`)
    SELECT 'BCC', `bcc_manifest_id`, `from_acc_manifest_id`, `release_id` FROM `bcc_manifest` WHERE `bcc_id` = OLD.`bcc_id`;

DROP TRIGGER IF EXISTS `dt_sc_cc_change_update_trg`;
CREATE TRIGGER `dt_sc_cc_change_update_trg` AFTER UPDATE ON `dt_sc` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`)
    SELECT 'DT_SC', `dt_sc_manifest_id`, `owner_dt_manifest_id`, `release_id` FROM `dt_sc_manifest` WHERE `dt_sc_id` = NEW.`dt_sc_id`;

DROP TRIGGER IF EXISTS `dt_sc_cc_change_delete_trg`;
CREATE TRIGGER `dt_sc_cc_change_delete_trg` AFTER DELETE ON `dt_sc` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`)
    SELECT 'DT_SC', `dt_sc_manifest_id`, `owner_dt_manifest_id`, `release_id` FROM `dt_sc_manifest` WHERE `dt_sc_id` = OLD.`dt_sc_id`;

DROP TRIGGER IF EXISTS `seq_key_cc_change_insert_trg`;
CREATE TRIGGER `seq_key_cc_change_insert_trg` AFTER INSERT ON `seq_key` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('ACC', NEW.`from_acc_manifest_id`, NULL, NULL);

DROP TRIGGER IF EXISTS `seq_key_cc_change_update_trg`;
CREATE TRIGGER `seq_key_cc_change_update_trg` AFTER UPDATE ON `seq_key` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('ACC', NEW.`from_acc_manifest_id`, NULL, NULL);

DROP TRIGGER IF EXISTS `seq_key_cc_change_delete_trg`;
CREATE TRIGGER `seq_key_cc_change_delete_trg` AFTER DELETE ON `seq_key` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('ACC', OLD.`from_acc_manifest_id`, NULL, NULL);

DROP TRIGGER IF EXISTS `acc_manifest_tag_cc_change_insert_trg`;
CREATE TRIGGER `acc_manifest_tag_cc_change_insert_trg` AFTER INSERT ON `acc_manifest_tag` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('ACC', NEW.`acc_manifest_id`, NULL, NULL);

DROP TRIGGER IF EXISTS `acc_manifest_tag_cc_change_delete_trg`;
CREATE TRIGGER `acc_manifest_tag_cc_change_delete_trg` AFTER DELETE ON `acc_manifest_tag` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('ACC', OLD.`acc_manifest_id`, NULL, NULL);

DROP TRIGGER IF EXISTS `asccp_manifest_tag_cc_change_insert_trg`;
CREATE TRIGGER `asccp_manifest_tag_cc_change_insert_trg` AFTER INSERT ON `asccp_manifest_tag` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('ASCCP', NEW.`asccp_manifest_id`, NULL, NULL);

DROP TRIGGER IF EXISTS `asccp_manifest_tag_cc_change_delete_trg`;
CREATE TRIGGER `asccp_manifest_tag_cc_change_delete_trg` AFTER DELETE ON `asccp_manifest_tag` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('ASCCP', OLD.`asccp_manifest_id`, NULL, NULL);

DROP TRIGGER IF EXISTS `bccp_manifest_tag_cc_change_insert_trg`;
CREATE TRIGGER `bccp_manifest_tag_cc_change_insert_trg` AFTER INSERT ON `bccp_manifest_tag` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('BCCP', NEW.`bccp_manifest_id`, NULL, NULL);

DROP TRIGGER IF EXISTS `bccp_manifest_tag_cc_change_delete_trg`;
CREATE TRIGGER `bccp_manifest_tag_cc_change_delete_trg` AFTER DELETE ON `bccp_manifest_tag` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('BCCP', OLD.`bccp_manifest_id`, NULL, NULL);

DROP TRIGGER IF EXISTS `dt_manifest_tag_cc_change_insert_trg`;
CREATE TRIGGER `dt_manifest_tag_cc_change_insert_trg` AFTER INSERT ON `dt_manifest_tag` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('DT', NEW.`dt_manifest_id`, NULL, NULL);

DROP TRIGGER IF EXISTS `dt_manifest_tag_cc_change_delete_trg`;
CREATE TRIGGER `dt_manifest_tag_cc_change_delete_trg` AFTER DELETE ON `dt_manifest_tag` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('DT', OLD.`dt_manifest_id`, NULL, NULL);

DROP TRIGGER IF EXISTS `tag_cc_change_update_trg`;
CREATE TRIGGER `tag_cc_change_update_trg` AFTER UPDATE ON `tag` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('TAG', NEW.`tag_id`, NULL, NULL);

DROP TRIGGER IF EXISTS `release_cc_change_update_trg`;
CREATE TRIGGER `release_cc_change_update_trg` AFTER UPDATE ON `release` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('RELEASE', NEW.`release_id`, NULL, NEW.`release_id`);

DROP TRIGGER IF EXISTS `library_cc_change_update_trg`;
CREATE TRIGGER `library_cc_change_update_trg` AFTER UPDATE ON `library` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('LIBRARY', NEW.`library_id`, NULL, NULL);

DROP TRIGGER IF EXISTS `tag_cc_change_delete_trg`;
CREATE TRIGGER `tag_cc_change_delete_trg` AFTER DELETE ON `tag` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('TAG', OLD.`tag_id`, NULL, NULL);

DROP TRIGGER IF EXISTS `release_cc_change_delete_trg`;
CREATE TRIGGER `release_cc_change_delete_trg` AFTER DELETE ON `release` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('RELEASE', OLD.`release_id`, NULL, OLD.`release_id`);

DROP TRIGGER IF EXISTS `library_cc_change_delete_trg`;
CREATE TRIGGER `library_cc_change_delete_trg` AFTER DELETE ON `library` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('LIBRARY', OLD.`library_id`, NULL, NULL);

DROP TRIGGER IF EXISTS `release_dep_cc_change_insert_trg`;
CREATE TRIGGER `release_dep_cc_change_insert_trg` AFTER INSERT ON `release_dep` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('RELEASE_DEP', NEW.`release_id`, NULL, NEW.`release_id`);

DROP TRIGGER IF EXISTS `release_dep_cc_change_update_trg`;
CREATE TRIGGER `release_dep_cc_change_update_trg` AFTER UPDATE ON `release_dep` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('RELEASE_DEP', NEW.`release_id`, NULL, NEW.`release_id`);

DROP TRIGGER IF EXISTS `release_dep_cc_change_delete_trg`;
CREATE TRIGGER `release_dep_cc_change_delete_trg` AFTER DELETE ON `release_dep` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`) VALUES ('RELEASE_DEP', OLD.`release_id`, NULL, OLD.`release_id`);

DROP TRIGGER IF EXISTS `app_user_cc_change_update_trg`;
CREATE TRIGGER `app_user_cc_change_update_trg` AFTER UPDATE ON `app_user` FOR EACH ROW
    INSERT INTO `cc_change_log` (`kind`, `target_id`, `parent_manifest_id`, `release_id`)
    SELECT 'USER', NEW.`app_user_id`, NULL, NULL FROM DUAL
    WHERE NOT (OLD.`login_id` <=> NEW.`login_id` AND OLD.`is_developer` <=> NEW.`is_developer`);


-- When only a part of this script is run, the variable saved at the top of the script is not set (NULL).
SET @OLD_FOREIGN_KEY_CHECKS = IFNULL(CAST(@OLD_FOREIGN_KEY_CHECKS AS SIGNED), 1);
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;

SET @OLD_SQL_NOTES = IFNULL(@OLD_SQL_NOTES, 1);
SET SQL_NOTES = @OLD_SQL_NOTES;
