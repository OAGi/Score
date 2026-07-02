CREATE TABLE `oas_http_header`
(
    `oas_http_header_id`      bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                    varchar(41) NOT NULL COMMENT 'The GUID of the record.',
    `header`                  varchar(200) DEFAULT NULL COMMENT 'REQUIRED. The name of the header. Header names are case sensitive. ',
    `description`             text DEFAULT NULL COMMENT 'A brief description of the header. This could contain examples of use. CommonMark syntax MAY be used for rich text representation.',
    `agency_id_list_value_id` bigint(20) unsigned NOT NULL COMMENT 'A reference of the agency id list value',
    `schema_type_reference`   text NOT NULL COMMENT 'REQUIRED. The schema defining the type used for the header using the reference string, $ref.',
    `owner_user_id`           bigint(20) unsigned NOT NULL COMMENT 'The user who owns the record.',
    `created_by`              bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`         bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`      datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp`   datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_http_header_id`),
    KEY                       `oas_http_header_agency_id_list_value_id_fk` (`agency_id_list_value_id`),
    KEY                       `oas_http_header_created_by_fk` (`created_by`),
    KEY                       `oas_http_header_last_updated_by_fk` (`last_updated_by`),
    KEY                       `oas_http_header_owner_user_id_fk` (`owner_user_id`),
    CONSTRAINT `oas_http_header_agency_id_list_value_id_fk` FOREIGN KEY (`agency_id_list_value_id`) REFERENCES `agency_id_list_value` (`agency_id_list_value_id`),
    CONSTRAINT `oas_http_header_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_http_header_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_http_header_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='OpenAPI Header Object, defining an HTTP header by its name, description, and schema type reference ($ref); attached to an OAS_RESPONSE through the OAS_RESPONSE_HEADERS join and referenced by an OAS_PARAMETER when the parameter location is header.';