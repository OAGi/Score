SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `oas_doc`;
-- Create syntax for TABLE 'oas_doc'
CREATE TABLE `oas_doc`
(
    `oas_doc_id`            bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `title`                 text        NOT NULL COMMENT 'The title of the API.',
    `description`           text        NOT NULL COMMENT 'A short description of the API. CommonMark syntax MAY be used for rich text representation.',
    `version`               varchar(20) NOT NULL COMMENT 'REQUIRED. The version of the OpenAPI document (which is distinct from the OpenAPI Specification version or the API implementation version).',
    `owner_user_id`         bigint(20) unsigned NOT NULL COMMENT 'The user who owns the record.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_doc_id`),
    KEY                     `oas_doc_created_by_fk` (`created_by`),
    KEY                     `oas_doc_last_updated_by_fk` (`last_updated_by`),
    KEY                     `oas_doc_owner_user_id_fk` (`owner_user_id`),
    CONSTRAINT `oas_doc_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_doc_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_doc_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_doc_biz_ctx`;
-- Create syntax for TABLE 'oas_doc_biz_ctx'
CREATE TABLE `oas_doc_biz_ctx`
(
    `oas_doc_id`            bigint(20) unsigned NOT NULL COMMENT 'The primary key of the record.',
    `biz_ctx_id`            bigint(20) unsigned NOT NULL COMMENT 'The primary key of the record.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_doc_id`, `biz_ctx_id`),
    KEY                     `oas_doc_biz_ctx_biz_ctx_id_fk` (`biz_ctx_id`),
    KEY                     `oas_doc_biz_ctx_created_by_fk` (`created_by`),
    KEY                     `oas_doc_biz_ctx_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_doc_biz_ctx_biz_ctx_id_fk` FOREIGN KEY (`biz_ctx_id`) REFERENCES `biz_ctx` (`biz_ctx_id`),
    CONSTRAINT `oas_doc_biz_ctx_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_doc_biz_ctx_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_doc_biz_ctx_oas_doc_id_fk` FOREIGN KEY (`oas_doc_id`) REFERENCES `oas_doc` (`oas_doc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_tag`;
-- Create syntax for TABLE 'oas_tag'
CREATE TABLE `oas_tag`
(
    `oas_tag_id`            bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `name`                  varchar(200) NOT NULL COMMENT 'REQUIRED. The name of the tag.',
    `description`           text COMMENT 'A short description for the tag. CommonMark syntax MAY be used for rich text representation.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_tag_id`),
    KEY                     `oas_tag_created_by_fk` (`created_by`),
    KEY                     `oas_tag_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_tag_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_tag_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_message_body`;
-- Create syntax for TABLE 'oas_message_body'
CREATE TABLE `oas_message_body`
(
    `oas_message_body_id`   bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `top_level_asbiep_id`   bigint(20) unsigned DEFAULT NULL COMMENT 'A reference of the TOP_LEVEL_ASBIEP record.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_message_body_id`),
    KEY                     `oas_message_body_top_level_asbiep_id_fk` (`top_level_asbiep_id`),
    KEY                     `oas_message_body_created_by_fk` (`created_by`),
    KEY                     `oas_message_body_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_message_body_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_message_body_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_message_body_top_level_asbiep_id_fk` FOREIGN KEY (`top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_doc_message_body`;
-- Create syntax for TABLE 'oas_doc_message_body'
CREATE TABLE `oas_doc_message_body`
(
    `oas_doc_message_body_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `oas_doc_id`              bigint(20) unsigned NOT NULL COMMENT 'A reference of the oas_doc record.',
    `oas_message_body_id`     bigint(20) unsigned NOT NULL COMMENT 'A reference of the oas_message_body record.',
    `created_by`              bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`         bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`      datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp`   datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_doc_message_body_id`),
    KEY                       `oas_doc_message_body_oas_doc_id_fk` (`oas_doc_id`),
    KEY                       `oas_doc_message_body_oas_message_body_id_fk` (`oas_message_body_id`),
    KEY                       `oas_doc_message_body_created_by_fk` (`created_by`),
    KEY                       `oas_doc_message_body_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_doc_message_body_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_doc_message_body_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_doc_message_body_oas_doc_id_fk` FOREIGN KEY (`oas_doc_id`) REFERENCES `oas_doc` (`oas_doc_id`),
    CONSTRAINT `oas_doc_message_body_oas_message_body_id_fk` FOREIGN KEY (`oas_message_body_id`) REFERENCES `oas_message_body` (`oas_message_body_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_resource`;
-- Create syntax for TABLE 'oas_resource'
CREATE TABLE `oas_resource`
(
    `oas_resource_id`         bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `oas_doc_message_body_id` bigint(20) unsigned NOT NULL COMMENT 'A reference of the oas_doc_message_body record.',
    `path`                    text NOT NULL COMMENT 'This will hold the BIE name by default.',
    `created_by`              bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`         bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`      datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp`   datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_resource_id`),
    KEY                       `oas_resource_oas_doc_message_body_id_fk` (`oas_doc_message_body_id`),
    KEY                       `oas_resource_created_by_fk` (`created_by`),
    KEY                       `oas_resource_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_resource_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_resource_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_resource_oas_doc_message_body_id_fk` FOREIGN KEY (`oas_doc_message_body_id`) REFERENCES `oas_doc_message_body` (`oas_doc_message_body_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_operation`;
-- Create syntax for TABLE 'oas_operation'
CREATE TABLE `oas_operation`
(
    `oas_operation_id`      bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `oas_resource_id`       bigint(20) unsigned NOT NULL COMMENT 'A reference of the resource record.',
    `verb`                  varchar(10)  NOT NULL COMMENT 'verbs, list of values droplist: GET, PUT, POST, DELETE, OPTIONS, HEAD, PATCH, TRACE',
    `operation_id`          varchar(200) NOT NULL COMMENT 'Unique string used to identify the operation. The id MUST be unique among all operations described in the API. The operationId value is case-sensitive. Tools and libraries MAY use the operationId to uniquely identify an operation, therefore, it is RECOMMENDED to follow common programming naming conventions.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_operation_id`),
    KEY                     `oas_operation_created_by_fk` (`created_by`),
    KEY                     `oas_operation_last_updated_by_fk` (`last_updated_by`),
    KEY                     `oas_operation_oas_resource_id_fk` (`oas_resource_id`),
    CONSTRAINT `oas_operation_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_operation_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_operation_oas_resource_id_fk` FOREIGN KEY (`oas_resource_id`) REFERENCES `oas_resource` (`oas_resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_request`;
-- Create syntax for TABLE 'oas_request'
CREATE TABLE `oas_request`
(
    `oas_request_id`                  bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `oas_operation_id`                bigint(20) unsigned NOT NULL,
    `make_array_indicator`            tinyint(1) DEFAULT '0',
    `suppress_root_property`          tinyint(1) DEFAULT '1',
    `meta_header_top_level_asbiep_id` bigint(20) unsigned DEFAULT NULL,
    `created_by`                      bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`                 bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`              datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp`           datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_request_id`),
    KEY                               `oas_request_oas_operation_id_fk` (`oas_operation_id`),
    KEY                               `oas_request_meta_header_top_level_asbiep_id_fk` (`meta_header_top_level_asbiep_id`),
    KEY                               `oas_request_created_by_fk` (`created_by`),
    KEY                               `oas_request_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_request_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_request_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_request_meta_header_top_level_asbiep_id_fk` FOREIGN KEY (`meta_header_top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`),
    CONSTRAINT `oas_request_oas_operation_id_fk` FOREIGN KEY (`oas_operation_id`) REFERENCES `oas_operation` (`oas_operation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_response`;
-- Create syntax for TABLE 'oas_response'
CREATE TABLE `oas_response`
(
    `oas_response_id`                         bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `oas_operation_id`                        bigint(20) unsigned NOT NULL,
    `http_status_code`                        int(11) DEFAULT NULL,
    `make_array_indicator`                    tinyint(1) DEFAULT '0',
    `suppress_root_property`                  tinyint(1) DEFAULT '1',
    `meta_header_top_level_asbiep_id`         bigint(20) unsigned DEFAULT NULL,
    `pagination_response_top_level_asbiep_id` bigint(20) unsigned DEFAULT NULL,
    `created_by`                              bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`                         bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`                      datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp`                   datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_response_id`),
    KEY                                       `oas_response_created_by_fk` (`created_by`),
    KEY                                       `oas_response_last_updated_by_fk` (`last_updated_by`),
    KEY                                       `oas_response_oas_operation_id_fk` (`oas_operation_id`),
    KEY                                       `oas_response_meta_header_top_level_asbiep_id_fk` (`meta_header_top_level_asbiep_id`),
    KEY                                       `oas_response_pagination_response_top_level_asbiep_id_fk` (`pagination_response_top_level_asbiep_id`),
    CONSTRAINT `oas_response_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_response_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_response_meta_header_top_level_asbiep_id_fk` FOREIGN KEY (`meta_header_top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`),
    CONSTRAINT `oas_response_oas_operation_id_fk` FOREIGN KEY (`oas_operation_id`) REFERENCES `oas_operation` (`oas_operation_id`),
    CONSTRAINT `oas_response_pagination_response_top_level_asbiep_id_fk` FOREIGN KEY (`pagination_response_top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oas_operation_tag`;
-- Create syntax for TABLE 'oas_operation_tag'
CREATE TABLE `oas_operation_tag`
(
    `oas_operation_id`      bigint(20) unsigned NOT NULL COMMENT 'The primary key of the record.',
    `oas_tag_id`            bigint(20) unsigned NOT NULL COMMENT 'The primary key of the record.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_operation_id`, `oas_tag_id`),
    KEY                     `oas_operation_tag_oas_tag_id_fk` (`oas_tag_id`),
    CONSTRAINT `oas_operation_tag_oas_operation_id_fk` FOREIGN KEY (`oas_operation_id`) REFERENCES `oas_operation` (`oas_operation_id`),
    CONSTRAINT `oas_operation_tag_oas_tag_id_fk` FOREIGN KEY (`oas_tag_id`) REFERENCES `oas_tag` (`oas_tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;