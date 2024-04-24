-- ----------------------------------------------------
-- Migration script for Score v3.3.0                 --
--                                                   --
-- Author: Hakju Oh <hakju.oh@nist.gov>              --
-- ----------------------------------------------------

SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO `configuration` (`name`, `type`, `value`)
VALUES ('score.mail.smtp.host', 'String', 'smtp.gmail.com'),
       ('score.mail.smtp.port', 'Integer', '587'),
       ('score.mail.smtp.auth', 'Boolean', 'true'),
       ('score.mail.smtp.ssl.enable', 'Boolean', 'false'),
       ('score.mail.smtp.starttls.enable', 'Boolean', 'true'),
       ('score.mail.smtp.auth.method', 'String', 'Password'),
       ('score.mail.smtp.auth.username', 'String', ''),
       ('score.mail.smtp.auth.password', 'String', ''),
       ('score.functions-requiring-email-transmission.enabled', 'Boolean', 'false');

ALTER TABLE `app_user`
    ADD COLUMN `email` varchar(100) DEFAULT NULL COMMENT 'Email address.' AFTER `organization`,
    ADD COLUMN `email_verified` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'The fact whether the email value is verified or not.' AFTER `email`,
    ADD COLUMN `email_verified_timestamp` datetime(6) DEFAULT NULL COMMENT 'The timestamp when the email address has verified.' AFTER `email_verified`;

UPDATE `app_user`, `app_oauth2_user` SET `app_user`.`email` = `app_oauth2_user`.`email`
WHERE `app_user`.`app_user_id` = `app_oauth2_user`.`app_user_id` AND `app_user`.`email` IS NULL;

DROP TABLE IF EXISTS `text_template`;
CREATE TABLE `text_template`
(
    `text_template_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `name`             varchar(100) NOT NULL DEFAULT '',
    `subject`          varchar(200)          DEFAULT NULL,
    `content_type`     varchar(100)          DEFAULT 'text/plain',
    `template`         longtext              DEFAULT NULL,
    PRIMARY KEY (`text_template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `text_template` (`name`, `subject`, `content_type`, `template`)
VALUES
    ('test', 'Test', 'text/html', '<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>Test</title>\n    <style>\n      body {\n          font-size: 14px;\n          font-family: Arial, Helvetica, sans-serif;\n      }\n    </style>\n</head>\n<body>\n    <p>Hello ${recipient},</p>\n    <p>This is a test message sent by <b>${sender}</b></p>\n</body>\n</html>'),
    ('email-validation', 'Email Validation', 'text/html', '<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>Email Validation</title>\n    <style>\n      body {\n          font-size: 14px;\n          font-family: Arial, Helvetica, sans-serif;\n      }\n      a {\n          text-decoration: none;\n      }\n      td {\n          border-radius: 10px;\n      }\n      td a {\n          padding: 8px 12px;\n          border: 0px solid;\n          font-family: Arial, Helvetica, sans-serif;\n          font-size: 14px;\n          color: black; \n          text-decoration: none;\n          font-weight: bold;\n          display: inline-block;  \n      }\n    </style>\n</head>\n<body>\n    <p>Hello ${recipient},</p>\n    <p>Click the link below to verify this was you. If this wasn\'t you, please change your password.</p>\n    <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n      <tr><td><table cellspacing=\"0\" cellpadding=\"0\"><tr>\n        <td bgcolor=\"#CADBE7\">\n          <a href=\"${email_validation_link}\" target=\"_blank\">\n          Verify Email Address\n          </a>\n        </td>\n      </tr></table></td></tr></table>\n</body>\n</html>'),
	('bie-ownership-transfer-request', 'Request for BIE Ownership Transfer', 'text/html', '<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>Request for BIE Ownership Transfer</title>\n    <style>\n      body {\n          font-size: 14px;\n          font-family: Arial, Helvetica, sans-serif;\n      }\n      a {\n          text-decoration: none;\n      }\n      td {\n          border-radius: 10px;\n      }\n      td a {\n          padding: 8px 12px;\n          border: 0px solid;\n          font-family: Arial, Helvetica, sans-serif;\n          font-size: 14px;\n          color: black; \n          text-decoration: none;\n          font-weight: bold;\n          display: inline-block;  \n      }\n    </style>\n</head>\n<body>\n    <p>Hello ${recipient},</p>\n    <p><b>${sender}</b> requests the ownership transfer of the BIE \'<a href=\"${bie_link}\" target=\"_blank\">${bie_name}</a>\'. Click the link below to accept this request.</p>\n    <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n      <tr><td><table cellspacing=\"0\" cellpadding=\"0\"><tr>\n        <td bgcolor=\"#CADBE7\">\n          <a href=\"${bie_link}/accept-ownership-transfer-request?topLevelAsbiepId=${topLevelAsbiepId}&targetLoginId=${targetLoginId}\" target=\"_blank\">\n          Accept the ownership transfer request\n          </a>\n        </td>\n      </tr></table></td></tr></table>\n</body>\n</html>'),
    ('bie-ownership-transfer-acceptance', 'BIE Ownership Transfer Acceptance', 'text/html', '<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>BIE Ownership Transfer Acceptance</title>\n    <style>\n      body {\n          font-size: 14px;\n          font-family: Arial, Helvetica, sans-serif;\n      }\n      a {\n          text-decoration: none;\n      }\n      td {\n          border-radius: 10px;\n      }\n      td a {\n          padding: 8px 12px;\n          border: 0px solid;\n          font-family: Arial, Helvetica, sans-serif;\n          font-size: 14px;\n          color: black; \n          text-decoration: none;\n          font-weight: bold;\n          display: inline-block;  \n      }\n    </style>\n</head>\n<body>\n    <p>Hello ${recipient},</p>\n    <p><b>${sender}</b> accept your ownership transfer request for the BIE \'<a href=\"${bie_link}\" target=\"_blank\">${bie_name}</a>\'.</p>\n</body>\n</html>'),
    ('bie-package-ownership-transfer-request', 'Request for BIE Package Ownership Transfer', 'text/html', '<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>Request for BIE Package Ownership Transfer</title>\n    <style>\n      body {\n          font-size: 14px;\n          font-family: Arial, Helvetica, sans-serif;\n      }\n      a {\n          text-decoration: none;\n      }\n      td {\n          border-radius: 10px;\n      }\n      td a {\n          padding: 8px 12px;\n          border: 0px solid;\n          font-family: Arial, Helvetica, sans-serif;\n          font-size: 14px;\n          color: black; \n          text-decoration: none;\n          font-weight: bold;\n          display: inline-block;  \n      }\n    </style>\n</head>\n<body>\n    <p>Hello ${recipient},</p>\n    <p><b>${sender}</b> requests the ownership transfer of the BIE package \'<a href=\"${bie_package_link}\" target=\"_blank\">${bie_package_version_name} - ${bie_package_version_id}</a>\'. Click the link below to accept this request.</p>\n    <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n      <tr><td><table cellspacing=\"0\" cellpadding=\"0\"><tr>\n        <td bgcolor=\"#CADBE7\">\n          <a href=\"${bie_package_link}/accept-ownership-transfer-request?biePackageId=${biePackageId}&targetLoginId=${targetLoginId}\" target=\"_blank\">\n          Accept the ownership transfer request\n          </a>\n        </td>\n      </tr></table></td></tr></table>\n</body>\n</html>'),
    ('bie-package-ownership-transfer-acceptance', 'BIE Package Ownership Transfer Acceptance', 'text/html', '<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>BIE Package Ownership Transfer Acceptance</title>\n    <style>\n      body {\n          font-size: 14px;\n          font-family: Arial, Helvetica, sans-serif;\n      }\n      a {\n          text-decoration: none;\n      }\n      td {\n          border-radius: 10px;\n      }\n      td a {\n          padding: 8px 12px;\n          border: 0px solid;\n          font-family: Arial, Helvetica, sans-serif;\n          font-size: 14px;\n          color: black; \n          text-decoration: none;\n          font-weight: bold;\n          display: inline-block;  \n      }\n    </style>\n</head>\n<body>\n    <p>Hello ${recipient},</p>\n    <p><b>${sender}</b> accept your ownership transfer request for the BIE Package \'<a href=\"${bie_package_link}\" target=\"_blank\">${bie_package_version_name} - ${bie_package_version_id}</a>\'.</p>\n</body>\n</html>');


DROP TABLE IF EXISTS `bie_package`;
CREATE TABLE `bie_package`
(
    `bie_package_id`        bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the BIE package record.',
    `version_id`            varchar(100) NOT NULL COMMENT 'A text field used for containing the release package version ID value (ex: CDM_1.1.0). All BIEs released as part of the same CDM package should have the same package version value.',
    `version_name`          varchar(200) NOT NULL COMMENT 'A text field used for containing the release package version name value (ex: 2024 Common Data Model Package Release). All BIEs released as part of the same CDM package should have the same package version value.',
    `description`           longtext    DEFAULT NULL COMMENT 'A text field used for containing a short description of the release package.  All BIEs released as part of the same CDM package should have the same package description value.',
    `state`                 varchar(20) DEFAULT 'WIP' COMMENT 'WIP, QA, Production. This the revision life cycle state of the BIE package.',
    `owner_user_id`         bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the BIE package. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the BIE package. The creator of the BIE package is also its owner by default.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the BIE package record. This may be the user who is in the same group as the creator.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'Timestamp when the BIE package record was first created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the BIE package was last updated.',
    `source_bie_package_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A foreign key referring to the source BIE_PACKAGE_ID which has linked to this record.',
    `source_action`         varchar(20) DEFAULT NULL COMMENT 'An action that had used to create a reference from the source (e.g., ''Copy'' or ''Uplift''.)',
    `source_timestamp`      datetime(6) DEFAULT NULL COMMENT 'A timestamp when a source reference had been made.',
    PRIMARY KEY (`bie_package_id`),
    KEY                     `bie_package_source_bie_package_id_fk` (`source_bie_package_id`),
    KEY                     `bie_package_owner_user_id_fk` (`owner_user_id`),
    KEY                     `bie_package_created_by_fk` (`created_by`),
    KEY                     `bie_package_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `bie_package_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `bie_package_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `bie_package_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `bie_package_source_bie_package_id_fk` FOREIGN KEY (`source_bie_package_id`) REFERENCES `bie_package` (`bie_package_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `bie_package_top_level_asbiep`;
CREATE TABLE `bie_package_top_level_asbiep`
(
    `bie_package_top_level_asbiep_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the BIE package-Top-Level ASBIEP record.',
    `bie_package_id`                  bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the BIE package.',
    `top_level_asbiep_id`             bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the TOP_LEVEL_ASBIEP_ID which has linked to the BIE package. The release ID of this record must be the same to the BIE package''s release ID.',
    `created_by`                      bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who adds the record into the BIE package.',
    `creation_timestamp`              datetime(6) NOT NULL COMMENT 'Timestamp when this record was first created.',
    PRIMARY KEY (`bie_package_top_level_asbiep_id`),
    KEY                               `bie_package_top_level_asbiep_bie_package_id_fk` (`bie_package_id`),
    KEY                               `bie_package_top_level_asbiep_top_level_asbiep_id_fk` (`top_level_asbiep_id`),
    CONSTRAINT `bie_package_top_level_asbiep_bie_package_id_fk` FOREIGN KEY (`bie_package_id`) REFERENCES `bie_package` (`bie_package_id`),
    CONSTRAINT `bie_package_top_level_asbiep_top_level_asbiep_id_fk` FOREIGN KEY (`top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;




