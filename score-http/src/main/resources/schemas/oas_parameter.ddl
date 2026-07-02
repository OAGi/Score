CREATE TABLE `oas_parameter`
(
    `oas_parameter_id`      bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `guid`                  varchar(41) NOT NULL COMMENT 'The GUID of the record.',
    `name`                  varchar(200) NOT NULL COMMENT 'REQUIRED. The name of the parameter. Parameter names are case sensitive.\nIf in is "path", the name field MUST correspond to a template expression occurring within the path field in the Paths Object. See Path Templating for further information.\nIf in is "header" and the name field is "Accept", "Content-Type" or "Authorization", the parameter definition SHALL be ignored.\nFor all other cases, the name corresponds to the parameter name used by the in property.',
    `in`                    varchar(100) NOT NULL COMMENT 'REQUIRED. The location of the parameter. Possible values are "query", "header", "path" or "cookie".',
    `required`              tinyint(1) NOT NULL DEFAULT 0 COMMENT 'Determines whether this parameter is mandatory. If the parameter location is "path", this property is REQUIRED and its value MUST be true. Otherwise, the property MAY be included and its default value is false.',
    `description`           text DEFAULT NULL COMMENT 'A brief description of the parameter. This could contain examples of use. CommonMark syntax MAY be used for rich text representation.',
    `schema_type_reference` text NOT NULL COMMENT 'A reference of the schema defining the type used for the parameter.',
    `allow_reserved`        tinyint(1) DEFAULT 0 COMMENT 'Determines whether the parameter value SHOULD allow reserved characters, as defined by RFC3986 :/?#[]@!$&''()*+,;= to be included without percent-encoding. This property only applies to parameters with an in value of query. The default value is false.',
    `deprecated`            tinyint(1) DEFAULT 0 COMMENT 'Specifies that a parameter is deprecated and SHOULD be transitioned out of usage. Default value is false.',
    `oas_http_header_id`    bigint(20) unsigned DEFAULT NULL COMMENT 'IF IN = Header, Then select from OAS_HTTP_HEADER table',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_parameter_id`),
    KEY                     `oas_parameter_oas_http_header_id_fk` (`oas_http_header_id`),
    KEY                     `oas_parameter_created_by_fk` (`created_by`),
    KEY                     `oas_parameter_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_parameter_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_parameter_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_parameter_oas_http_header_id_fk` FOREIGN KEY (`oas_http_header_id`) REFERENCES `oas_http_header` (`oas_http_header_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='OpenAPI Parameter Object describing a single operation parameter, identified by its name and location (in = query, header, path, or cookie) along with its required, schema type, and serialization settings; when in = header it may reference an OAS_HTTP_HEADER.';