-- ----------------------------------------------------
-- Migration script for Score v3.3.0                 --
--                                                   --
-- Author: Hakju Oh <hakju.oh@nist.gov>              --
-- ----------------------------------------------------

INSERT INTO `configuration` (`name`, `type`, `value`)
VALUES ('score.mail.smtp.host', 'String', 'smtp.gmail.com'),
       ('score.mail.smtp.port', 'Integer', '587'),
       ('score.mail.smtp.auth', 'Boolean', 'true'),
       ('score.mail.smtp.ssl.enable', 'Boolean', 'false'),
       ('score.mail.smtp.starttls.enable', 'Boolean', 'true'),
       ('score.mail.smtp.auth.method', 'String', 'Password'),
       ('score.mail.smtp.auth.username', 'String', ''),
       ('score.mail.smtp.auth.password', 'String', '');

ALTER TABLE `app_user`
    ADD COLUMN `email` varchar(100) DEFAULT NULL COMMENT 'Email address.' AFTER `organization`,
    ADD COLUMN `email_verified` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'The fact whether the email value is verified or not.' AFTER `email`,
    ADD COLUMN `email_verified_timestamp` datetime(6) DEFAULT NULL COMMENT 'The timestamp when the email address has verified.' AFTER `email_verified`;

UPDATE `app_user`, `app_oauth2_user` SET `app_user`.`email` = `app_oauth2_user`.`email`
WHERE `app_user`.`app_user_id` = `app_oauth2_user`.`app_user_id` AND `app_user`.`email` IS NULL;

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
    ('bie-ownership-transfer-request', 'Request for BIE Ownership Transfer', 'text/html', '<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>Request for BIE Ownership Transfer</title>\n    <style>\n      body {\n          font-size: 14px;\n          font-family: Arial, Helvetica, sans-serif;\n      }\n      a {\n          text-decoration: none;\n      }\n      td {\n          border-radius: 10px;\n      }\n      td a {\n          padding: 8px 12px;\n          border: 0px solid;\n          font-family: Arial, Helvetica, sans-serif;\n          font-size: 14px;\n          color: black; \n          text-decoration: none;\n          font-weight: bold;\n          display: inline-block;  \n      }\n    </style>\n</head>\n<body>\n    <p>Hello ${recipient},</p>\n    <p><b>${sender}</b> requests the ownership transfer of the BIE \'<a href=\"${bie_link}\" target=\"_blank\">${bie_name}</a>\'. Click the link below to accept this request.</p>\n    <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n      <tr><td><table cellspacing=\"0\" cellpadding=\"0\"><tr>\n        <td bgcolor=\"#CADBE7\">\n          <a href=\"${bie_link}/accept-ownership-transfer-request?topLevelAsbiepId=${topLevelAsbiepId}&targetLoginId=${targetLoginId}\" target=\"_blank\">\n          Accept the ownership transfer request\n          </a>\n        </td>\n      </tr></table></td></tr></table>\n</body>\n</html>'),
    ('email-validation', 'Email Validation', 'text/html', '<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>Email Validation</title>\n    <style>\n      body {\n          font-size: 14px;\n          font-family: Arial, Helvetica, sans-serif;\n      }\n      a {\n          text-decoration: none;\n      }\n      td {\n          border-radius: 10px;\n      }\n      td a {\n          padding: 8px 12px;\n          border: 0px solid;\n          font-family: Arial, Helvetica, sans-serif;\n          font-size: 14px;\n          color: black; \n          text-decoration: none;\n          font-weight: bold;\n          display: inline-block;  \n      }\n    </style>\n</head>\n<body>\n    <p>Hello ${recipient},</p>\n    <p>Click the link below to verify this was you. If this wasn\'t you, please change your password.</p>\n    <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n      <tr><td><table cellspacing=\"0\" cellpadding=\"0\"><tr>\n        <td bgcolor=\"#CADBE7\">\n          <a href=\"${email_validation_link}\" target=\"_blank\">\n          Verify Email Address\n          </a>\n        </td>\n      </tr></table></td></tr></table>\n</body>\n</html>');




