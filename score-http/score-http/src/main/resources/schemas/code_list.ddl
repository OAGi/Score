CREATE TABLE `code_list`
(
    `code_list_id`             bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
    `guid`                     char(32) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL COMMENT 'A globally unique identifier (GUID).',
    `enum_type_guid`           varchar(41)  DEFAULT NULL COMMENT 'In the OAGIS Model XML schema, a type, which keeps all the enumerated values, is  defined separately from the type that represents a code list. This only applies to some code lists. When that is the case, this column stores the GUID of that enumeration type.',
    `name`                     varchar(100) DEFAULT NULL COMMENT 'Name of the code list.',
    `list_id`                  varchar(100)                                          NOT NULL COMMENT 'External identifier.',
    `version_id`               varchar(100)                                          NOT NULL COMMENT 'Code list version number.',
    `definition`               text         DEFAULT NULL COMMENT 'Description of the code list.',
    `remark`                   varchar(225) DEFAULT NULL COMMENT 'Usage information about the code list.',
    `definition_source`        varchar(100) DEFAULT NULL COMMENT 'This is typically a URL which indicates the source of the code list''s DEFINITION.',
    `namespace_id`             bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the NAMESPACE table. This is the namespace to which the entity belongs. This namespace column is primarily used in the case the component is a user''s component because there is also a namespace assigned at the release level.',
    `based_code_list_id`       bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the CODE_LIST table itself. This identifies the code list on which this code list is based, if any. The derivation may be restriction and/or extension.',
    `extensible_indicator`     tinyint(1) NOT NULL COMMENT 'This is a flag to indicate whether the code list is final and shall not be further derived.',
    `is_deprecated`            tinyint(1) DEFAULT 0 COMMENT 'Indicates whether the code list is deprecated and should not be reused (i.e., no new reference to this record should be allowed).',
    `replacement_code_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement if the record is deprecated.',
    `created_by`               bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created the code list.',
    `owner_user_id`            bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership.',
    `last_updated_by`          bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It identifies the user who last updated the code list.',
    `creation_timestamp`       datetime(6) NOT NULL COMMENT 'Timestamp when the code list was created.',
    `last_update_timestamp`    datetime(6) NOT NULL COMMENT 'Timestamp when the code list was last updated.',
    `state`                    varchar(20)  DEFAULT NULL,
    `prev_code_list_id`        bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the previous history record.',
    `next_code_list_id`        bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the next history record.',
    PRIMARY KEY (`code_list_id`),
    KEY                        `code_list_based_code_list_id_fk` (`based_code_list_id`),
    KEY                        `code_list_created_by_fk` (`created_by`),
    KEY                        `code_list_last_updated_by_fk` (`last_updated_by`),
    KEY                        `code_list_owner_user_id_fk` (`owner_user_id`),
    KEY                        `code_list_prev_code_list_id_fk` (`prev_code_list_id`),
    KEY                        `code_list_next_code_list_id_fk` (`next_code_list_id`),
    KEY                        `code_list_namespace_id_fk` (`namespace_id`),
    KEY                        `code_list_replacement_code_list_id_fk` (`replacement_code_list_id`),
    CONSTRAINT `code_list_based_code_list_id_fk` FOREIGN KEY (`based_code_list_id`) REFERENCES `code_list` (`code_list_id`),
    CONSTRAINT `code_list_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `code_list_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `code_list_namespace_id_fk` FOREIGN KEY (`namespace_id`) REFERENCES `namespace` (`namespace_id`),
    CONSTRAINT `code_list_next_code_list_id_fk` FOREIGN KEY (`next_code_list_id`) REFERENCES `code_list` (`code_list_id`),
    CONSTRAINT `code_list_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `code_list_prev_code_list_id_fk` FOREIGN KEY (`prev_code_list_id`) REFERENCES `code_list` (`code_list_id`),
    CONSTRAINT `code_list_replacement_code_list_id_fk` FOREIGN KEY (`replacement_code_list_id`) REFERENCES `code_list` (`code_list_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='This table stores information about a code list. When a code list is derived from another code list, the whole set of code values belonging to the based code list will be copied.';