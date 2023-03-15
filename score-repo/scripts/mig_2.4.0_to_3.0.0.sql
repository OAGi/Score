-- ----------------------------------------------------
-- Migration script for Score v3.0.0                 --
--                                                   --
-- Author: Hakju Oh <hakju.oh@nist.gov>              --
-- ----------------------------------------------------

CREATE TABLE `business_term`
(
    `business_term_id`      bigint(20) unsigned          NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an Business term.',
    `guid`                  char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
    `business_term`         varchar(255)                 NOT NULL COMMENT 'A main name of the business term',
    `definition`            text COMMENT 'Definition of the business term.',
    `created_by`            bigint(20) unsigned          NOT NULL COMMENT 'A foreign key referring to the user who creates the business term. The creator of the business term is also its owner by default.',
    `last_updated_by`       bigint(20) unsigned          NOT NULL COMMENT 'A foreign key referring to the last user who has updated the business term record. This may be the user who is in the same group as the creator.',
    `creation_timestamp`    datetime(6)                  NOT NULL COMMENT 'Timestamp when the business term record was first created.',
    `last_update_timestamp` datetime(6)                  NOT NULL COMMENT 'The timestamp when the business term was last updated.',
    `external_ref_uri`      text                         NOT NULL COMMENT 'TODO: Definition is missing.',
    `external_ref_id`       varchar(100) DEFAULT NULL COMMENT 'TODO: Definition is missing.',
    `comment`               text COMMENT 'Comment of the business term.',
    PRIMARY KEY (`business_term_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='The Business Term table stores information about the business term, which is usually associated to BIE or CC. TODO: Placeeholder, definition is missing.';

CREATE TABLE `ascc_bizterm`
(
    `ascc_bizterm_id`       bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of an Business term.',
    `business_term_id`      bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the associated business term',
    `ascc_id`               bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the associated ASCC',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the ascc_bizterm record. The creator of the ascc_bizterm is also its owner by default.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the ascc_bizterm record. This may be the user who is in the same group as the creator.',
    `creation_timestamp`    datetime(6)         NOT NULL COMMENT 'Timestamp when the ascc_bizterm record was first created.',
    `last_update_timestamp` datetime(6)         NOT NULL COMMENT 'The timestamp when the ascc_bizterm was last updated.',
    PRIMARY KEY (`ascc_bizterm_id`),
    KEY `ascc_bizterm_ascc_fk` (`ascc_id`),
    KEY `ascc_bizterm_business_term_fk` (`business_term_id`),
    CONSTRAINT `ascc_bizterm_ascc_fk` FOREIGN KEY (`ascc_id`) REFERENCES `ascc` (`ascc_id`),
    CONSTRAINT `ascc_bizterm_business_term_fk` FOREIGN KEY (`business_term_id`) REFERENCES `business_term` (`business_term_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='The ascc_bizterm table stores information about the aggregation between the business term and ASCC. TODO: Placeholder, definition is missing.';

CREATE TABLE `asbie_bizterm`
(
    `asbie_bizterm_id`      bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of an asbie_bizterm record.',
    `ascc_bizterm_id`       bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the ascc_business_term record.',
    `asbie_id`              bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the associated ASBIE',
    `primary_indicator`     tinyint(1)          NOT NULL DEFAULT 0 COMMENT 'The indicator shows if the business term is primary for the assigned ASBIE.',
    `type_code`             char(30)                     DEFAULT NULL COMMENT 'The type code of the assignment.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the asbie_bizterm record. The creator of the asbie_bizterm is also its owner by default.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the asbie_bizterm record. This may be the user who is in the same group as the creator.',
    `creation_timestamp`    datetime(6)         NOT NULL COMMENT 'Timestamp when the asbie_bizterm record was first created.',
    `last_update_timestamp` datetime(6)         NOT NULL COMMENT 'The timestamp when the asbie_bizterm was last updated.',
    PRIMARY KEY (`asbie_bizterm_id`),
    KEY `asbie_bizterm_ascc_bizterm_fk` (`ascc_bizterm_id`),
    KEY `asbie_bizterm_asbie_fk` (`asbie_id`),
    CONSTRAINT `asbie_bizterm_asbie_fk` FOREIGN KEY (`asbie_id`) REFERENCES `asbie` (`asbie_id`),
    CONSTRAINT `asbie_bizterm_ascc_bizterm_fk` FOREIGN KEY (`ascc_bizterm_id`) REFERENCES `ascc_bizterm` (`ascc_bizterm_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='The asbie_bizterm table stores information about the aggregation between the ascc_bizterm and ASBIE. TODO: Placeholder, definition is missing.';

CREATE TABLE `bcc_bizterm`
(
    `bcc_bizterm_id`        bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of an bcc_bizterm record.',
    `business_term_id`      bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the associated business term',
    `bcc_id`                bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the associated BCC',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the bcc_bizterm record. The creator of the bcc_bizterm is also its owner by default.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the bcc_bizterm record. This may be the user who is in the same group as the creator.',
    `creation_timestamp`    datetime(6)         NOT NULL COMMENT 'Timestamp when the bcc_bizterm record was first created.',
    `last_update_timestamp` datetime(6)         NOT NULL COMMENT 'The timestamp when the bcc_bizterm was last updated.',
    PRIMARY KEY (`bcc_bizterm_id`),
    KEY `bcc_bizterm_bcc_fk` (`bcc_id`),
    KEY `bcc_bizterm_business_term_fk` (`business_term_id`),
    CONSTRAINT `bcc_bizterm_bcc_fk` FOREIGN KEY (`bcc_id`) REFERENCES `bcc` (`bcc_id`),
    CONSTRAINT `bcc_bizterm_business_term_fk` FOREIGN KEY (`business_term_id`) REFERENCES `business_term` (`business_term_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='The bcc_bizterm table stores information about the aggregation between the business term and BCC. TODO: Placeholder, definition is missing.';

CREATE TABLE `bbie_bizterm`
(
    `bbie_bizterm_id`       bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of an bbie_bizterm record.',
    `bcc_bizterm_id`        bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the bbie_bizterm record.',
    `bbie_id`               bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the associated BBIE',
    `primary_indicator`     tinyint(1)          NOT NULL DEFAULT 0 COMMENT 'The indicator shows if the business term is primary for the assigned BBIE.',
    `type_code`             char(30)                     DEFAULT NULL COMMENT 'The type code of the assignment.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the bbie_bizterm record. The creator of the asbie_bizterm is also its owner by default.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the bbie_bizterm record. This may be the user who is in the same group as the creator.',
    `creation_timestamp`    datetime(6)         NOT NULL COMMENT 'Timestamp when the bbie_bizterm record was first created.',
    `last_update_timestamp` datetime(6)         NOT NULL COMMENT 'The timestamp when the bbie_bizterm was last updated.',
    PRIMARY KEY (`bbie_bizterm_id`),
    KEY `bbie_bizterm_bcc_bizterm_fk` (`bcc_bizterm_id`),
    KEY `asbie_bizterm_asbie_fk` (`bbie_id`),
    CONSTRAINT `bbie_bizterm_bcc_bizterm_fk` FOREIGN KEY (`bcc_bizterm_id`) REFERENCES `bcc_bizterm` (`bcc_bizterm_id`),
    CONSTRAINT `bbie_bizterm_bbie_fk` FOREIGN KEY (`bbie_id`) REFERENCES `bbie` (`bbie_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='The bbie_bizterm table stores information about the aggregation between the bbie_bizterm and BBIE. TODO: Placeholder, definition is missing.';

-- oagi.tenant definition
CREATE TABLE `tenant`
(
    `tenant_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
    `name`      varchar(100) DEFAULT NULL COMMENT 'The name of the tenant.',
    PRIMARY KEY (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
    COMMENT ='This table about the user tenant role.';

-- oagi.user to tenant role
CREATE TABLE `user_tenant`
(
    `user_tenant_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
    `tenant_id`      bigint(20) unsigned NOT NULL COMMENT 'Assigned tenant to the user.',
    `app_user_id`    bigint(20) unsigned NOT NULL COMMENT 'Application user.',
    PRIMARY KEY (`user_tenant_id`),
    UNIQUE KEY `user_tenant_pair` (`tenant_id`, `app_user_id`),
    KEY `user_tenant_tenant_id_fk` (`tenant_id`),
    KEY `user_tenant_tenant_id_app_user_id_fk` (`app_user_id`),
    CONSTRAINT `user_tenant_tenant_id_fk` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`tenant_id`),
    CONSTRAINT `user_tenant_tenant_id_app_user_id_fk` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
    COMMENT ='This table captures the tenant roles of the user';

-- oagi.tenant to business context definition
CREATE TABLE `tenant_business_ctx`
(
    `tenant_business_ctx_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
    `tenant_id`              bigint(20) unsigned NOT NULL COMMENT 'Tenant role.',
    `biz_ctx_id`             bigint(20) unsigned NOT NULL COMMENT 'Concrete business context for the company.',
    PRIMARY KEY (`tenant_business_ctx_id`),
    UNIQUE KEY `tenant_business_ctx_pair` (`tenant_id`, `biz_ctx_id`),
    KEY `tenant_business_ctx_tenant_id_fk` (`tenant_id`),
    KEY `organization_business_ctx_biz_ctx_id_fk` (`biz_ctx_id`),
    CONSTRAINT `tenant_business_ctx_tenant_id_fk` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`tenant_id`),
    CONSTRAINT `organization_business_ctx_biz_ctx_id_fk` FOREIGN KEY (`biz_ctx_id`) REFERENCES `biz_ctx` (`biz_ctx_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
    COMMENT ='This table captures the tenant role and theirs business contexts.';

-- config table
-- oagi.configuration definition
CREATE TABLE `configuration`
(
    `configuration_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
    `name`             varchar(100) DEFAULT NULL COMMENT 'The name of configuration property.',
    `type`             varchar(100) DEFAULT NULL COMMENT 'The type of configuration property.',
    `value`            varchar(100) DEFAULT NULL COMMENT 'The value of configuration property.',
    PRIMARY KEY (`configuration_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
    COMMENT ='The table stores configuration properties of the application.';

-- Init data
INSERT INTO `configuration` (`name`, `type`, `value`)
VALUES ('score.tenant.enabled', 'Boolean', 'false'),
       ('score.business-term.enabled', 'Boolean', 'true'),
       ('score.bie.inverse-mode', 'Boolean', 'false');

-- Add 'levenshtein' function
-- Built by Felix Zandanel <felix@zandanel.me>, https://github.com/fza/mysql-doctrine-levenshtein-function
DELIMITER ;;;
CREATE FUNCTION `levenshtein`(s1 VARCHAR(255), s2 VARCHAR(255)) RETURNS int(11) DETERMINISTIC
BEGIN
    DECLARE s1_len, s2_len, i, j, c, c_temp, cost INT;
    DECLARE s1_char CHAR;
    DECLARE cv0, cv1 VARBINARY(256);
    SET s1_len = CHAR_LENGTH(s1), s2_len = CHAR_LENGTH(s2), cv1 = 0x00, j = 1, i = 1, c = 0;
    IF s1 = s2 THEN
        RETURN 0;
    ELSEIF s1_len = 0 THEN
        RETURN s2_len;
    ELSEIF s2_len = 0 THEN
        RETURN s1_len;
    ELSE
        WHILE j <= s2_len DO
            SET cv1 = CONCAT(cv1, UNHEX(HEX(j))), j = j + 1;
        END WHILE;
        WHILE i <= s1_len DO
            SET s1_char = SUBSTRING(s1, i, 1), c = i, cv0 = UNHEX(HEX(i)), j = 1;
            WHILE j <= s2_len DO
                SET c = c + 1;
                IF s1_char = SUBSTRING(s2, j, 1) THEN SET cost = 0; ELSE SET cost = 1; END IF;
                SET c_temp = CONV(HEX(SUBSTRING(cv1, j, 1)), 16, 10) + cost;
                IF c > c_temp THEN SET c = c_temp; END IF;
                SET c_temp = CONV(HEX(SUBSTRING(cv1, j+1, 1)), 16, 10) + 1;
                IF c > c_temp THEN SET c = c_temp; END IF;
                SET cv0 = CONCAT(cv0, UNHEX(HEX(c))), j = j + 1;
            END WHILE;
            SET cv1 = cv0, i = i + 1;
        END WHILE;
    END IF;
    RETURN c;
END;;;
DELIMITER ;

-- Add `tag` and relationship tables.
CREATE TABLE `tag`
(
    `tag_id`                bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a tag record.',
    `name`                  varchar(100) NOT NULL COMMENT 'The name of the tag.',
    `description`           text COMMENT 'The description of the tag.',
    `text_color`            varchar(10)  NOT NULL COMMENT 'The text color of the tag.',
    `background_color`      varchar(10)  NOT NULL COMMENT 'The background color of the tag.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the tag record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the tag record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'Timestamp when the tag record was first created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the tag was last updated.',
    PRIMARY KEY (`tag_id`),
    KEY                     `tag_created_by_fk` (`created_by`),
    KEY                     `tag_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `tag_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `tag_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `tag` (`name`, `description`, `text_color`, `background_color`, `created_by`, `last_updated_by`,
                   `creation_timestamp`, `last_update_timestamp`)
VALUES ('BOD', 'Business Document Object', '#FFFFFF', '#D1446B', 1, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));
INSERT INTO `tag` (`name`, `description`, `text_color`, `background_color`, `created_by`, `last_updated_by`,
                   `creation_timestamp`, `last_update_timestamp`)
VALUES ('Noun', NULL, '#FFFFFF', '#1C0F5C', 1, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));
INSERT INTO `tag` (`name`, `description`, `text_color`, `background_color`, `created_by`, `last_updated_by`,
                   `creation_timestamp`, `last_update_timestamp`)
VALUES ('Verb', NULL, '#FFFFFF', '#1A48A2', 1, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));

CREATE TABLE `acc_manifest_tag`
(
    `acc_manifest_id`    BIGINT(20) unsigned NOT NULL,
    `tag_id`             BIGINT(20) unsigned NOT NULL,
    `created_by`         bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the record.',
    `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the record was first created.',
    PRIMARY KEY (`acc_manifest_id`, `tag_id`),
    KEY                  `acc_manifest_tag_acc_manifest_id_fk` (`acc_manifest_id`),
    KEY                  `acc_manifest_tag_tag_id_fk` (`tag_id`),
    KEY                  `acc_manifest_tag_created_by_fk` (`created_by`),
    CONSTRAINT `acc_manifest_tag_acc_manifest_id_fk` FOREIGN KEY (`acc_manifest_id`) REFERENCES `acc_manifest` (`acc_manifest_id`),
    CONSTRAINT `acc_manifest_tag_tag_id_fk` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`tag_id`),
    CONSTRAINT `acc_manifest_tag_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `asccp_manifest_tag`
(
    `asccp_manifest_id`  BIGINT(20) unsigned NOT NULL,
    `tag_id`             BIGINT(20) unsigned NOT NULL,
    `created_by`         bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the record.',
    `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the record was first created.',
    PRIMARY KEY (`asccp_manifest_id`, `tag_id`),
    KEY                  `asccp_manifest_tag_asccp_manifest_id_fk` (`asccp_manifest_id`),
    KEY                  `asccp_manifest_tag_tag_id_fk` (`tag_id`),
    KEY                  `asccp_manifest_tag_created_by_fk` (`created_by`),
    CONSTRAINT `asccp_manifest_tag_asccp_manifest_id_fk` FOREIGN KEY (`asccp_manifest_id`) REFERENCES `asccp_manifest` (`asccp_manifest_id`),
    CONSTRAINT `asccp_manifest_tag_tag_id_fk` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`tag_id`),
    CONSTRAINT `asccp_manifest_tag_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `bccp_manifest_tag`
(
    `bccp_manifest_id`   BIGINT(20) unsigned NOT NULL,
    `tag_id`             BIGINT(20) unsigned NOT NULL,
    `created_by`         bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the record.',
    `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the record was first created.',
    PRIMARY KEY (`bccp_manifest_id`, `tag_id`),
    KEY                  `bccp_manifest_tag_bccp_manifest_id_fk` (`bccp_manifest_id`),
    KEY                  `bccp_manifest_tag_tag_id_fk` (`tag_id`),
    KEY                  `bccp_manifest_tag_created_by_fk` (`created_by`),
    CONSTRAINT `bccp_manifest_tag_bccp_manifest_id_fk` FOREIGN KEY (`bccp_manifest_id`) REFERENCES `bccp_manifest` (`bccp_manifest_id`),
    CONSTRAINT `bccp_manifest_tag_tag_id_fk` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`tag_id`),
    CONSTRAINT `bccp_manifest_tag_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `dt_manifest_tag`
(
    `dt_manifest_id`     BIGINT(20) unsigned NOT NULL,
    `tag_id`             BIGINT(20) unsigned NOT NULL,
    `created_by`         bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the record.',
    `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the record was first created.',
    PRIMARY KEY (`dt_manifest_id`, `tag_id`),
    KEY                  `dt_manifest_tag_dt_manifest_id_fk` (`dt_manifest_id`),
    KEY                  `dt_manifest_tag_tag_id_fk` (`tag_id`),
    KEY                  `dt_manifest_tag_created_by_fk` (`created_by`),
    CONSTRAINT `dt_manifest_tag_dt_manifest_id_fk` FOREIGN KEY (`dt_manifest_id`) REFERENCES `dt_manifest` (`dt_manifest_id`),
    CONSTRAINT `dt_manifest_tag_tag_id_fk` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`tag_id`),
    CONSTRAINT `dt_manifest_tag_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Add 'BOD' tag to ASCCPs
INSERT INTO `asccp_manifest_tag` (`asccp_manifest_id`, `tag_id`, `created_by`, `creation_timestamp`)
SELECT `asccp_manifest`.`asccp_manifest_id`,
       (SELECT `tag_id` FROM `tag` WHERE `name` = 'BOD'),
       (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'oagis'),
       CURRENT_TIMESTAMP(6)
FROM `acc_manifest` AS `base`
         JOIN `acc` ON `base`.`acc_id` = `acc`.`acc_id`
         JOIN `acc_manifest` ON `base`.`acc_manifest_id` = `acc_manifest`.`based_acc_manifest_id`
         JOIN `asccp_manifest` ON `acc_manifest`.`acc_manifest_id` = `asccp_manifest`.`role_of_acc_manifest_id`
WHERE `acc`.`object_class_term` = 'Business Object Document';

-- Add 'Verb' tag to ASCCPs
INSERT INTO `asccp_manifest_tag` (`asccp_manifest_id`, `tag_id`, `created_by`, `creation_timestamp`)
SELECT `asccp_manifest`.`asccp_manifest_id`,
       (SELECT `tag_id` FROM `tag` WHERE `name` = 'Verb'),
       (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'oagis'),
       CURRENT_TIMESTAMP(6)
FROM `asccp_manifest`
         JOIN `asccp` ON `asccp_manifest`.`asccp_id` = `asccp`.`asccp_id`
         JOIN `acc_manifest` ON `asccp_manifest`.`role_of_acc_manifest_id` = `acc_manifest`.`acc_manifest_id`
         JOIN `acc_manifest` AS `base` ON `acc_manifest`.`based_acc_manifest_id` = `base`.`acc_manifest_id`
         JOIN `acc` ON `base`.`acc_id` = `acc`.`acc_id`
WHERE `acc`.`object_class_term` IN
      ('Verb', 'Response Verb', 'Action Verb', 'Request Verb', 'Acknowledge', 'Show', 'Respond', 'Process', 'Sync',
       'Notify', 'Get');

-- Add 'Noun' tag to ASCCPs
INSERT INTO `asccp_manifest_tag` (`asccp_manifest_id`, `tag_id`, `created_by`, `creation_timestamp`)
SELECT `asccp_manifest`.`asccp_manifest_id`,
       (SELECT `tag_id` FROM `tag` WHERE `name` = 'Noun'),
       (SELECT `app_user_id` FROM `app_user` WHERE `login_id` = 'oagis'),
       CURRENT_TIMESTAMP(6)
FROM `asccp_manifest`
         JOIN (SELECT DISTINCT `asccp`.`asccp_id`
               FROM `asccp` AS `base`
                        JOIN `acc` ON `base`.`role_of_acc_id` = `acc`.`acc_id`
                        JOIN `ascc` ON `acc`.`acc_id` = `ascc`.`from_acc_id`
                        JOIN `asccp` ON `ascc`.`to_asccp_id` = `asccp`.`asccp_id`
               WHERE `base`.`property_term` = 'Data Area'
                 AND `asccp`.`property_term` NOT IN
                     ('Get', 'Acknowledge', 'Cancel', 'Sync', 'Process', 'Show', 'Change', 'Cancel Acknowledge',
                      'Notify', 'Change Acknowledge', 'Sync Response', 'Load', 'Load Response', 'Post Acknowledge',
                      'Post')) AS `t`
              ON `asccp_manifest`.`asccp_id` = `t`.`asccp_id`;

-- Add `replacement` data for 'Work Order Schedulde. Details'
UPDATE `acc_manifest`,
    (SELECT `acc_manifest`.`acc_manifest_id`, `replace_acc_manifest`.`acc_manifest_id` AS `replacement_acc_manifest_id` FROM `acc`
    JOIN `acc_manifest` ON `acc`.`acc_id` = `acc_manifest`.`acc_id`
    JOIN `acc_manifest` AS `replace_acc_manifest` ON `acc_manifest`.`release_id` = `replace_acc_manifest`.`release_id`
    JOIN `acc` AS `replace_acc` ON `replace_acc_manifest`.`acc_id` = `replace_acc`.`acc_id`
    WHERE `acc`.`object_class_term` = 'Work Order Schedulde' AND `acc`.`is_deprecated` = 1
    AND `replace_acc`.`object_class_term` = 'Work Order Schedule') AS `t`
SET `acc_manifest`.`replacement_acc_manifest_id` = `t`.`replacement_acc_manifest_id`
WHERE `acc_manifest`.`acc_manifest_id` = `t`.`acc_manifest_id`;

-- Migrate 'client_authentication_method' values
UPDATE `oauth2_app` SET `client_authentication_method` = 'client_secret_basic' WHERE `client_authentication_method` = 'basic';
UPDATE `oauth2_app` SET `client_authentication_method` = 'client_secret_post' WHERE `client_authentication_method` = 'post';
