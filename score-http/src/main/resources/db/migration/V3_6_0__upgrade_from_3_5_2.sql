-- ----------------------------------------------------
-- Migration script for Score v3.6.0                 --
--                                                   --
-- Author: Hakju Oh <hakju.oh@nist.gov>              --
-- ----------------------------------------------------

SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

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

SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
