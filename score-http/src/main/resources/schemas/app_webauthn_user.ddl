CREATE TABLE `app_webauthn_user`
(
    `app_webauthn_user_id`  bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
    `app_user_id`           bigint(20) unsigned NOT NULL COMMENT 'A reference to the record in `app_user`.',
    `user_handle`           varbinary(64) NOT NULL COMMENT 'Opaque random WebAuthn user handle (max 64 bytes per the spec). It is the only user identifier the authenticator stores, so it must not be derived from login_id or email.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'Timestamp when this record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when this record is last updated.',
    PRIMARY KEY (`app_webauthn_user_id`),
    UNIQUE KEY `app_webauthn_user_uk1` (`user_handle`),
    KEY                     `app_webauthn_user_app_user_id_fk` (`app_user_id`),
    CONSTRAINT `app_webauthn_user_app_user_id_fk` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='WebAuthn identity (user handle) of an app_user; created on the first passkey registration.';