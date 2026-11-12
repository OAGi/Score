-- ----------------------------------------------------
-- Migration script for Score v3.6.1                 --
--                                                   --
-- Author: Hakju Oh <hakju.oh@nist.gov>              --
-- ----------------------------------------------------

SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

SET @OLD_SQL_NOTES = @@SQL_NOTES;
SET SQL_NOTES = 0;

-- ------------------------------------------------------------------
-- HTTP headers of an OpenAPI document (#1791)
-- ------------------------------------------------------------------
--
-- A header such as traceparent is a request parameter (in: header) and a response header of the operations of a
-- document. The definitions are kept in OAS_PARAMETER and OAS_HTTP_HEADER; the new table binds them to a document, or to
-- one operation of it. OAS_REQUEST_PARAMETER and OAS_RESPONSE_HEADERS are not used for this, because they hang on
-- OAS_REQUEST and OAS_RESPONSE, which exist only for an operation that carries a BIE.
--
-- A header is not always taken from a list of agency ID values. The schema of a header and of a parameter is always a JSON
-- object, a Schema Object.
ALTER TABLE `oas_http_header` MODIFY COLUMN `agency_id_list_value_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A reference of the agency id list value. NULL when the header is not taken from a list of agency ID values.';
ALTER TABLE `oas_http_header` MODIFY COLUMN `schema_type_reference` text NOT NULL COMMENT 'REQUIRED. The schema defining the type used for the header, as a JSON object: a Schema Object (OpenAPI 3.0 also allows a Reference Object such as {"$ref": "#/components/schemas/X"}).';
ALTER TABLE `oas_parameter` MODIFY COLUMN `schema_type_reference` text NOT NULL COMMENT 'REQUIRED. The schema defining the type used for the parameter, as a JSON object: a Schema Object (OpenAPI 3.0 also allows a Reference Object such as {"$ref": "#/components/schemas/X"}).';

-- The example of a parameter or of a header (the example of the Parameter Object and of the Header Object, not the one of
-- its Schema Object, which is a part of SCHEMA_TYPE_REFERENCE). It is a JSON literal, so that a string, a number and an
-- object can be told apart.
ALTER TABLE `oas_parameter` ADD COLUMN IF NOT EXISTS `example` text DEFAULT NULL COMMENT 'Example of the parameter''s potential value, as a JSON literal. It overrides any example in the schema. NULL when there is none.' AFTER `schema_type_reference`;
ALTER TABLE `oas_http_header` ADD COLUMN IF NOT EXISTS `example` text DEFAULT NULL COMMENT 'Example of the header''s potential value, as a JSON literal. It overrides any example in the schema. NULL when there is none.' AFTER `schema_type_reference`;

-- The Header Object of OpenAPI has a `required` field (since 3.0.0), like the Parameter Object, whose OAS_PARAMETER.REQUIRED the
-- table already has. It belongs to the definition of the header, not to the binding of it to a document.
ALTER TABLE `oas_http_header` ADD COLUMN IF NOT EXISTS `required` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'Determines whether this header is mandatory. The default value is false.' AFTER `example`;

-- How the value of a parameter or of a header is serialized (the fixed fields of the Parameter Object and of the Header Object):
-- either with a schema (and STYLE, EXPLODE and, for a query parameter, ALLOW_RESERVED), or with `content`, a Media Type Object
-- keyed by CONTENT_MEDIA_TYPE, which the specification recommends for a cookie. The two exclude each other. NULL means the
-- default of the specification, which depends on the location of the parameter.
ALTER TABLE `oas_parameter` ADD COLUMN IF NOT EXISTS `style` varchar(20) DEFAULT NULL COMMENT 'Describes how the parameter value will be serialized depending on the type of the parameter value (matrix, label, simple, form, spaceDelimited, pipeDelimited or deepObject). NULL for the default of the location: form for query and cookie, simple for path and header. Not used with CONTENT_MEDIA_TYPE.' AFTER `deprecated`;
ALTER TABLE `oas_parameter` ADD COLUMN IF NOT EXISTS `explode` tinyint(1) DEFAULT NULL COMMENT 'When true, parameter values of type array or object generate separate parameters for each value of the array or key-value pair of the map. NULL for the default of the style: true for form, false for all other styles. Not used with CONTENT_MEDIA_TYPE.' AFTER `style`;
ALTER TABLE `oas_parameter` ADD COLUMN IF NOT EXISTS `content_media_type` varchar(255) DEFAULT NULL COMMENT 'The media type of the content (for example text/plain) that serializes the parameter instead of a style: the schema and the example then belong to this Media Type Object. NULL when the parameter is serialized with a schema.' AFTER `explode`;
ALTER TABLE `oas_http_header` ADD COLUMN IF NOT EXISTS `style` varchar(20) DEFAULT NULL COMMENT 'Describes how the header value will be serialized. The only style of a header is simple, which is the default; NULL for it. Not used with CONTENT_MEDIA_TYPE.' AFTER `required`;
ALTER TABLE `oas_http_header` ADD COLUMN IF NOT EXISTS `explode` tinyint(1) DEFAULT NULL COMMENT 'When true, header values of type array or object generate separate values for each value of the array or key-value pair of the map. NULL for the default, false. Not used with CONTENT_MEDIA_TYPE.' AFTER `style`;
ALTER TABLE `oas_http_header` ADD COLUMN IF NOT EXISTS `content_media_type` varchar(255) DEFAULT NULL COMMENT 'The media type of the content (for example text/plain) that serializes the header instead of a style: the schema and the example then belong to this Media Type Object. NULL when the header is serialized with a schema.' AFTER `explode`;

DROP TABLE IF EXISTS `oas_doc_http_header`;

CREATE TABLE `oas_doc_http_header`
(
    `oas_doc_http_header_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `oas_doc_id`            bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the OAS_DOC table; the document whose operations carry the header.',
    `oas_operation_id`      bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the OAS_OPERATION table. NULL when the header applies to every operation of the document.',
    `oas_parameter_id`      bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the OAS_PARAMETER table; the header is generated as a request parameter (in: header). NULL when the header is a response header.',
    `oas_http_header_id`    bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the OAS_HTTP_HEADER table; the header is generated in every response. NULL when the header is a request parameter.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6)         NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6)         NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`oas_doc_http_header_id`),
    KEY `oas_doc_http_header_oas_doc_id_fk` (`oas_doc_id`),
    KEY `oas_doc_http_header_oas_operation_id_fk` (`oas_operation_id`),
    KEY `oas_doc_http_header_oas_parameter_id_fk` (`oas_parameter_id`),
    KEY `oas_doc_http_header_oas_http_header_id_fk` (`oas_http_header_id`),
    KEY `oas_doc_http_header_created_by_fk` (`created_by`),
    KEY `oas_doc_http_header_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `oas_doc_http_header_oas_doc_id_fk` FOREIGN KEY (`oas_doc_id`) REFERENCES `oas_doc` (`oas_doc_id`),
    CONSTRAINT `oas_doc_http_header_oas_operation_id_fk` FOREIGN KEY (`oas_operation_id`) REFERENCES `oas_operation` (`oas_operation_id`),
    CONSTRAINT `oas_doc_http_header_oas_parameter_id_fk` FOREIGN KEY (`oas_parameter_id`) REFERENCES `oas_parameter` (`oas_parameter_id`),
    CONSTRAINT `oas_doc_http_header_oas_http_header_id_fk` FOREIGN KEY (`oas_http_header_id`) REFERENCES `oas_http_header` (`oas_http_header_id`),
    CONSTRAINT `oas_doc_http_header_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `oas_doc_http_header_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3
  COLLATE = utf8mb3_general_ci
  ROW_FORMAT = DYNAMIC COMMENT ='Binds an HTTP header of an OpenAPI document to the document, or to one of its operations: as a request parameter (OAS_PARAMETER) or as a header of every response (OAS_HTTP_HEADER).';

-- Issue #1791: register, for the operations that exist, the request parameters that the generator makes by itself
-- (sinceLastDateTime of a GET that returns an array, one path parameter per {name} of the path), as entries of the
-- operation. Not run twice for the same operation: what the operation (or the document) has already is skipped.

DROP TEMPORARY TABLE IF EXISTS tmp_numbers;
CREATE TEMPORARY TABLE tmp_numbers (n int PRIMARY KEY);
INSERT INTO tmp_numbers (n) VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(14),(15),(16),(17),(18),(19),(20);

-- every candidate: (operation, location, name), the path as the generated document has it
DROP TEMPORARY TABLE IF EXISTS tmp_candidate;
CREATE TEMPORARY TABLE tmp_candidate
(
    oas_doc_id       bigint(20) unsigned NOT NULL,
    oas_operation_id bigint(20) unsigned NOT NULL,
    created_by       bigint(20) unsigned NOT NULL,
    `in`             varchar(100)        NOT NULL,
    name             varchar(200)        NOT NULL,
    seq              int                 NOT NULL,
    guid             varchar(41)         NOT NULL
) DEFAULT CHARSET = utf8mb3 COLLATE = utf8mb3_general_ci;

INSERT INTO tmp_candidate (oas_doc_id, oas_operation_id, created_by, `in`, name, seq, guid)
SELECT DISTINCT r.oas_doc_id, o.oas_operation_id, o.created_by, 'query', 'sinceLastDateTime', 0, REPLACE(UUID(), '-', '')
FROM oas_operation o
         JOIN oas_resource r ON r.oas_resource_id = o.oas_resource_id
WHERE LOWER(o.verb) = 'get'
  AND (EXISTS(SELECT 1 FROM oas_request q WHERE q.oas_operation_id = o.oas_operation_id AND q.make_array_indicator = 1)
    OR EXISTS(SELECT 1 FROM oas_response p WHERE p.oas_operation_id = o.oas_operation_id AND p.make_array_indicator = 1));

-- the path parameters, in the order their names appear in the path
INSERT INTO tmp_candidate (oas_doc_id, oas_operation_id, created_by, `in`, name, seq, guid)
SELECT x.oas_doc_id, x.oas_operation_id, x.created_by, 'path', x.name, MIN(x.n), REPLACE(UUID(), '-', '')
FROM (SELECT r.oas_doc_id,
             o.oas_operation_id,
             o.created_by,
             n.n,
             TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(SUBSTRING_INDEX(
                     IF(IFNULL(d.version, '') <> '', REPLACE(r.path, '{version}', d.version), r.path),
                     '{', n.n + 1), '{', -1), '}', 1)) AS name
      FROM oas_operation o
               JOIN oas_resource r ON r.oas_resource_id = o.oas_resource_id
               JOIN oas_doc d ON d.oas_doc_id = r.oas_doc_id
               JOIN tmp_numbers n
      -- the n-th '{' has a '}' after it
      WHERE SUBSTRING_INDEX(SUBSTRING_INDEX(
                    IF(IFNULL(d.version, '') <> '', REPLACE(r.path, '{version}', d.version), r.path),
                    '{', n.n + 1), '{', -1) LIKE '%}%') x
WHERE x.name <> '' AND x.name NOT LIKE '%{%'
GROUP BY x.oas_doc_id, x.oas_operation_id, x.created_by, x.name;

-- leave out what the operation or the document has already
DELETE c
FROM tmp_candidate c
WHERE EXISTS(SELECT 1
             FROM oas_doc_http_header b
                      JOIN oas_parameter p ON p.oas_parameter_id = b.oas_parameter_id
             WHERE b.oas_doc_id = c.oas_doc_id
               AND (b.oas_operation_id IS NULL OR b.oas_operation_id = c.oas_operation_id)
               AND p.`in` = c.`in`
               AND p.name = c.name);

INSERT INTO oas_parameter (guid, name, `in`, required, description, schema_type_reference,
                           created_by, last_updated_by, creation_timestamp, last_update_timestamp)
SELECT c.guid, c.name, c.`in`, IF(c.`in` = 'path', 1, 0),
       IF(c.`in` = 'path', '', 'Returns resources that have been updated since the last time the endpoint has been called'),
       IF(c.`in` = 'path', '{"type":"string"}', '{"type":"string","format":"date-time"}'),
       c.created_by, c.created_by, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
FROM tmp_candidate c
ORDER BY c.oas_operation_id, c.seq;

INSERT INTO oas_doc_http_header (oas_doc_id, oas_operation_id, oas_parameter_id,
                                 created_by, last_updated_by, creation_timestamp, last_update_timestamp)
SELECT c.oas_doc_id, c.oas_operation_id, p.oas_parameter_id,
       c.created_by, c.created_by, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
FROM tmp_candidate c
         JOIN oas_parameter p ON p.guid = c.guid
ORDER BY c.oas_operation_id, c.seq;

DROP TEMPORARY TABLE tmp_candidate;
DROP TEMPORARY TABLE tmp_numbers;

-- ------------------------------------------------------------------
-- Names of an OpenAPI document are case-sensitive (#1891)
-- ------------------------------------------------------------------
--
-- The OpenAPI Specification (3.0.4 and 3.1.2, "Case Sensitivity" and "Format") makes every field name and every map key
-- case-sensitive, so Auth and auth are two names. The OAS_* tables are case-insensitive (utf8mb3_general_ci), which made
-- such names collide in the unique keys and in the lookups. The columns below hold a map key or an identifier of the
-- specification: the key of components.securitySchemes, the scope names, the operationId and the path of the Paths Object.
-- The unique keys over them are rebuilt by the change and only become less strict.
--
-- The name of a parameter is case-sensitive as well ("Parameter names are case sensitive"). A header parameter is the
-- exception, because the names of the HTTP headers are case-insensitive (RFC 7230); no lookup by the name of a header
-- parameter exists, and the code that generates a document already folds the case of a header name.
--
-- Not changed: OAS_HTTP_HEADER.HEADER (header names, RFC 7230) and the scheme of an http security scheme
-- (case-insensitive, RFC 7235).
--
-- Character sets:
-- - The operationId, the path and the name of a parameter keep their character set (utf8mb3): the specification and its
--   JSON Schema do not limit their characters (a plain string), and the path is made from names that are not limited to ASCII.
-- - The key of components.securitySchemes is limited to ^[a-zA-Z0-9\.\-_]+$ by the Components Object, and a scope is an
--   OAuth 2.0 scope token, printable ASCII without a space, a double quote and a backslash (RFC 6749, 3.3), so those columns
--   are ASCII. The service rejects any other name. A row that holds another character fails this upgrade; fix such a name
--   before upgrading.
--
-- A _bin collation still ignores trailing spaces (PAD SPACE): of the lookups, only that of the Operation IDs keeps the exact
-- matches.
ALTER TABLE `oas_security_scheme` MODIFY COLUMN `scheme_name` varchar(100) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'REQUIRED. The components.securitySchemes map key (e.g. OAuth2, ApiKeyAuth, BearerAuth); also the schemeName used in Security Requirement Objects. Unique within an oas_doc; case-sensitive, so Auth and auth are two names. Only letters, digits, ''.'', ''-'' and ''_'' (the Components Object).';
ALTER TABLE `oas_oauth_scope` MODIFY COLUMN `scope_name` varchar(200) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'REQUIRED. The scope name (the scopes map key). Unique within a flow; case-sensitive. An OAuth 2.0 scope token: printable ASCII without a space, a double quote and a backslash (RFC 6749, 3.3).';
ALTER TABLE `oas_doc_security_scope` MODIFY COLUMN `scope_name` varchar(200) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'A scope name required by this entry (oauth2: from the scheme''s flows; openIdConnect: from the provider). Case-sensitive. An OAuth 2.0 scope token: printable ASCII without a space, a double quote and a backslash (RFC 6749, 3.3).';
ALTER TABLE `oas_operation_security_scope` MODIFY COLUMN `scope_name` varchar(200) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'A scope name required by this entry (oauth2: from the scheme''s flows; openIdConnect: from the provider). Case-sensitive. An OAuth 2.0 scope token: printable ASCII without a space, a double quote and a backslash (RFC 6749, 3.3).';
ALTER TABLE `oas_operation` MODIFY COLUMN `operation_id` varchar(1024) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'Unique string used to identify the operation. The id MUST be unique among all operations described in the API. The operationId value is case-sensitive. Tools and libraries MAY use the operationId to uniquely identify an operation, therefore, it is RECOMMENDED to follow common programming naming conventions.';
ALTER TABLE `oas_parameter` MODIFY COLUMN `name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'REQUIRED. The name of the parameter. Parameter names are case sensitive.\nIf in is "path", the name field MUST correspond to a template expression occurring within the path field in the Paths Object. See Path Templating for further information.\nIf in is "header" and the name field is "Accept", "Content-Type" or "Authorization", the parameter definition SHALL be ignored.\nFor all other cases, the name corresponds to the parameter name used by the in property.';
ALTER TABLE `oas_resource` MODIFY COLUMN `path` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'The OpenAPI path (resource name) for this resource; by default made from the business context and the BIE name. Case-sensitive: /Pets and /pets are two paths.';

-- ------------------------------------------------------------------
-- The value and the meaning of a code list value
-- ------------------------------------------------------------------
--
-- Both columns were tinytext, which holds 255 BYTES. The dialog counts characters, so a text of 3-byte characters (Korean,
-- Chinese, ...) that fits the input overflowed the column and the save ended as a generic conflict.
--
-- - VALUE is varchar(255) (255 characters, the limit of the input) with a binary collation: a code is an enumeration value
--   of the generated XML Schema and JSON Schema, which compare it case-sensitively, so 'EA' and 'ea' are two codes.
-- - MEANING is text: no generated schema carries it and nothing needs it bounded, so the column does not restate the limit
--   that the input already has. It keeps the case-insensitive collation of the table, as a meaning is a description.
--
-- A _bin collation still ignores trailing spaces (PAD SPACE); the service trims a code before saving it.
ALTER TABLE `code_list_value` MODIFY COLUMN `value` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'The code list value used in the instance data, e.g., EA, US-EN. Case sensitive: EA and ea are different values.';
ALTER TABLE `code_list_value` MODIFY COLUMN `meaning` text DEFAULT NULL COMMENT 'The description or explanation of the code list value, e.g., ''Each'' for EA, ''English'' for EN.';

SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
SET SQL_NOTES = @OLD_SQL_NOTES;
