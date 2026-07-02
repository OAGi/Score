CREATE TABLE `oas_response_headers`
(
    `oas_response_id`       bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the OAS_RESPONSE table.',
    `oas_http_header_id`    bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the OAS_HTTP_HEADER table.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_response_id`,`oas_http_header_id`),
    KEY                     `oas_response_headers_oas_http_header_id_fk` (`oas_http_header_id`),
    KEY                     `oas_response_headers_created_by_fk` (`created_by`),
    KEY                     `oas_response_headers_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_response_headers_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_response_headers_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_response_headers_oas_http_header_id_fk` FOREIGN KEY (`oas_http_header_id`) REFERENCES `oas_http_header` (`oas_http_header_id`),
    CONSTRAINT `oas_response_headers_oas_response_id_fk` FOREIGN KEY (`oas_response_id`) REFERENCES `oas_response` (`oas_response_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='The headers map of an OpenAPI Response Object; a many-to-many join assigning OAS_HTTP_HEADER definitions to an OAS_RESPONSE.';