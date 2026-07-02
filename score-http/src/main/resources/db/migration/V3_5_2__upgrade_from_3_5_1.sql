-- ----------------------------------------------------
-- Migration script for Score v3.5.2                 --
--                                                   --
-- Author: Hakju Oh <hakju.oh@nist.gov>              --
-- ----------------------------------------------------

SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------------
-- Issue #1638 - Sibling sort order for model browsing & BIE editing
-- ------------------------------------------------------------------
--
-- A release-/lifecycle-decoupled, instance-level sort weight applied ONLY when
-- flattening the sibling list shown in the Model Browser and the BIE editor.
-- It is deliberately NOT used by SeqKeyHandler, the CC graph, or any generator
-- (XSD/JSON/OpenAPI/BIE expression), so generated output keeps the seq_key order.
--
-- The order is the same for all users (no app_user scoping), takes effect
-- immediately (no WIP/Published gate, no versioning/log), and is empty by default
-- (only an explicit reorder creates a row; an absent row falls back to seq_key).
--
-- `from_acc_manifest_id` is the VIEW parent (the expanded ACC node under which the
-- child appears as a flattened sibling); it MAY differ from seq_key.from_acc_manifest_id
-- because Group/Choice nodes are flattened on the frontend. Exactly one of
-- `ascc_manifest_id` / `bcc_manifest_id` is non-null (enforced in code).
CREATE TABLE `bie_view_order`
(
    `bie_view_order_id`     bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The primary key of the record.',
    `from_acc_manifest_id`  bigint(20) unsigned NOT NULL COMMENT 'View parent: the ACC manifest under which the child is shown as a flattened sibling. MAY differ from seq_key.from_acc_manifest_id due to group flattening.',
    `ascc_manifest_id`      bigint(20) unsigned DEFAULT NULL COMMENT 'The reordered ASCC child. Exactly one of ascc_manifest_id / bcc_manifest_id is non-null.',
    `bcc_manifest_id`       bigint(20) unsigned DEFAULT NULL COMMENT 'The reordered BCC child. Exactly one of ascc_manifest_id / bcc_manifest_id is non-null.',
    `weight`                int(11)             NOT NULL COMMENT 'Browse/BIE-edit sort weight, applied DESCENDING within the attributes-first partition. User-entered; need not be unique or sequential. NOT used for schema / BIE-expression output.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'The user who last updates the record.',
    `creation_timestamp`    datetime(6)         NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6)         NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`bie_view_order_id`),
    UNIQUE KEY `bie_view_order_ascc_uk` (`from_acc_manifest_id`, `ascc_manifest_id`),
    UNIQUE KEY `bie_view_order_bcc_uk` (`from_acc_manifest_id`, `bcc_manifest_id`),
    KEY `bie_view_order_from_acc_manifest_id` (`from_acc_manifest_id`),
    KEY `bie_view_order_ascc_manifest_id` (`ascc_manifest_id`),
    KEY `bie_view_order_bcc_manifest_id` (`bcc_manifest_id`),
    KEY `bie_view_order_created_by_fk` (`created_by`),
    KEY `bie_view_order_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `bie_view_order_from_acc_manifest_id_fk` FOREIGN KEY (`from_acc_manifest_id`) REFERENCES `acc_manifest` (`acc_manifest_id`),
    CONSTRAINT `bie_view_order_ascc_manifest_id_fk` FOREIGN KEY (`ascc_manifest_id`) REFERENCES `ascc_manifest` (`ascc_manifest_id`),
    CONSTRAINT `bie_view_order_bcc_manifest_id_fk` FOREIGN KEY (`bcc_manifest_id`) REFERENCES `bcc_manifest` (`bcc_manifest_id`),
    CONSTRAINT `bie_view_order_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `bie_view_order_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = DYNAMIC COMMENT ='Instance-level, release-decoupled sibling sort weights for model browsing and BIE editing (Issue #1638). NOT used for generated output.';

-- ------------------------------------------------------------------
-- Issue #1347 - OAS Doc default error responses + Error Response Body Type
-- ------------------------------------------------------------------
--
-- Per-operation selection of the body carried by the defaulted 4xx/5xx error
-- responses emitted into every generated operation. Stored on oas_operation (the
-- common parent of the Request and Response entries), so a single setting applies
-- even when both exist (and even for request-only / bodyless operations).
--
--   error_response_body_type = NONE            -> status code + description only (default; backward-compatible)
--                            = PROBLEM_DETAILS  -> application/problem+json -> a hardcoded RFC 9457 ProblemDetails
--                            = CONFIRM_MESSAGE   -> application/json -> the picked ConfirmMessage BIE schema
--
-- error_confirm_top_level_asbiep_id is the ConfirmMessage BIE, used only when
-- error_response_body_type = CONFIRM_MESSAGE (NULL otherwise).
ALTER TABLE `oas_operation`
    ADD COLUMN `error_response_body_type` varchar(20) NOT NULL DEFAULT 'NONE'
          COMMENT 'PROBLEM_DETAILS | CONFIRM_MESSAGE | NONE -- body for this operation''s defaulted 4xx/5xx error responses (issue #1347).'
          AFTER `security_overridden`,
    ADD COLUMN `error_confirm_top_level_asbiep_id` bigint(20) unsigned DEFAULT NULL
          COMMENT 'When error_response_body_type = CONFIRM_MESSAGE, the ConfirmMessage BIE to emit (issue #1347).'
          AFTER `error_response_body_type`,
    ADD CONSTRAINT `oas_operation_error_confirm_tla_fk`
          FOREIGN KEY (`error_confirm_top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`);

-- ------------------------------------------------------------------
-- Column comment corrections & fill-ins (schema re-sync)
-- ------------------------------------------------------------------
--
-- Comment-only synchronization surfaced while re-syncing resources/schemas/*.ddl
-- to the database. Grouped by kind of change; within each group statements are
-- ordered by table. Every statement is comment-only: column data type, nullability
-- and default are unchanged.

-- (1) Spelling & grammar typo corrections.
--     "refering"->"referring", "occurence"->"occurrence", "accomodate"->"accommodate",
--     "Foreing"->"Foreign", "nmenomic"->"mnemonic", "typye"->"type", stray punctuation, etc.
ALTER TABLE `abie` MODIFY COLUMN `based_acc_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key to the ACC_MANIFEST table referring to the ACC, on which the business context has been applied to derive this ABIE.';

ALTER TABLE `agency_id_list` MODIFY COLUMN `enum_type_guid` varchar(41) NOT NULL COMMENT 'This column stores the GUID of the type containing the enumerated values. In OAGIS, most code lists and agency ID lists are defined by an XyzCodeContentType (or XyzAgencyIdentificationContentType) and XyzCodeEnumerationType (or XyzAgencyIdentificationEnumerationContentType). However, some don''t have the enumeration type. When that is the case, this column is null.';

ALTER TABLE `app_user` MODIFY COLUMN `email_verified_timestamp` datetime(6) DEFAULT NULL COMMENT 'The timestamp when the email address has been verified.';

ALTER TABLE `asbie` MODIFY COLUMN `cardinality_min` int(11) NOT NULL COMMENT 'Minimum occurrence constraint of the TO_ASBIEP_ID. A valid value is a non-negative integer.';

ALTER TABLE `bbie` MODIFY COLUMN `cardinality_max` int(11) DEFAULT NULL COMMENT 'Maximum occurrence constraint of the TO_BBIEP_ID. A valid value is an integer from -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.';
ALTER TABLE `bbie` MODIFY COLUMN `is_nillable` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'Indicate whether the field can have a null value. This corresponds to the nillable flag in the XML schema.';
ALTER TABLE `bbie` MODIFY COLUMN `is_null` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'This column indicates whether the field is fixed to NULL. IS_NULL can be true only if the IS_NILLABLE is true. If IS_NULL is true then the FIX_VALUE and DEFAULT_VALUE columns cannot have a value.';
ALTER TABLE `bbie` MODIFY COLUMN `definition` text DEFAULT NULL COMMENT 'Description to override the BCC definition. If NULL, it means that the definition should be inherited from the based BCC.';

ALTER TABLE `bbie_sc` MODIFY COLUMN `cardinality_max` int(11) NOT NULL COMMENT 'Maximum occurrence constraint of the BBIE SC. A valid value is 0 or 1.';

ALTER TABLE `bbiep` MODIFY COLUMN `bbiep_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of a BBIEP.';
ALTER TABLE `bbiep` MODIFY COLUMN `definition` text DEFAULT NULL COMMENT 'Definition to override the BCCP''s Definition. If NULL, it means that the definition should be inherited from the based CC.';
ALTER TABLE `bbiep` MODIFY COLUMN `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the BBIEP. The creator of the BBIEP is also its owner by default. BBIEPs created as children of another ABIE have the same CREATED_BY.';
ALTER TABLE `bbiep` MODIFY COLUMN `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the BBIEP record was first created. BBIEPs created as children of another ABIE have the same CREATION_TIMESTAMP.';

ALTER TABLE `bcc` MODIFY COLUMN `cardinality_max` int(11) DEFAULT NULL COMMENT 'Maximum cardinality of the TO_BCCP_ID. The valid values are integer -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.';
ALTER TABLE `bcc` MODIFY COLUMN `to_bccp_id` bigint(20) unsigned NOT NULL COMMENT 'TO_BCCP_ID is a foreign key to an BCCP table record. It is basically pointing to a child data element of the FROM_ACC_ID. \n\nNote that for the BCC history records, this column always points to the BCCP_ID of the current record of a BCCP.';

ALTER TABLE `bccp` MODIFY COLUMN `bdt_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key pointing to the DT table indicating the data type or data format of the BCCP. Only DT_ID which DT_Type is BDT can be used.';

ALTER TABLE `business_term` MODIFY COLUMN `business_term_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a Business term.';

ALTER TABLE `code_list_value` MODIFY COLUMN `code_list_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the CODE_LIST table. It indicates the code list this code value belongs to.';
ALTER TABLE `code_list_value` MODIFY COLUMN `definition` text DEFAULT NULL COMMENT 'Long description or explanation of the code list value, e.g., ''EA is a discrete quantity for counting each unit of an item, such as, 2 shampoo bottles, 3 box of cereals''.';

ALTER TABLE `ctx_scheme` MODIFY COLUMN `ctx_category_id` bigint(20) unsigned NOT NULL COMMENT 'This is the foreign key to the CTX_CATEGORY table. It identifies the context category associated with this context scheme.';

ALTER TABLE `ctx_scheme_value` MODIFY COLUMN `meaning` text DEFAULT NULL COMMENT 'The description, explanation of the scheme value.';

ALTER TABLE `dt_sc` MODIFY COLUMN `owner_dt_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the DT table indicating the data type, to which this supplementary component belongs.';
ALTER TABLE `dt_sc` MODIFY COLUMN `cardinality_min` int(11) NOT NULL DEFAULT 0 COMMENT 'The minimum occurrence constraint associated with the supplementary component. The valid values are zero or one.';

ALTER TABLE `dt_usage_rule` MODIFY COLUMN `target_dt_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the DT_ID for assigning a usage rule to the corresponding DT content component.';
ALTER TABLE `dt_usage_rule` MODIFY COLUMN `target_dt_sc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the DT_SC_ID for assigning a usage rule to the corresponding DT_SC.';

ALTER TABLE `module` MODIFY COLUMN `type` varchar(45) NOT NULL COMMENT 'This is a type column that indicates whether the module is FILE or DIRECTORY.';
ALTER TABLE `module` MODIFY COLUMN `name` varchar(100) NOT NULL COMMENT 'This is the filename of the module. The reason to not including the extension is that the extension maybe dependent on the expression. For XML schema, ''.xsd'' maybe added; or for JSON, ''.json'' maybe added as the file extension.';

ALTER TABLE `namespace` MODIFY COLUMN `is_std_nmsp` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'This indicates whether the namespace is reserved for standard use (i.e., whether it is an OAGIS namespace). If it is true, then end users cannot use the namespace for the end user CCs.';

ALTER TABLE `release` MODIFY COLUMN `release_num` varchar(45) DEFAULT NULL COMMENT 'Release number such as 10.0, 10.1, etc. ';

ALTER TABLE `top_level_asbiep` MODIFY COLUMN `top_level_asbiep_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a TOP_LEVEL_ASBIEP record.';
ALTER TABLE `top_level_asbiep` MODIFY COLUMN `version` varchar(45) DEFAULT NULL COMMENT 'This column holds a version number assigned by the user. This column is only used by the top-level ASBIEP. No format of version is enforced.';
ALTER TABLE `top_level_asbiep` MODIFY COLUMN `status` varchar(45) DEFAULT NULL COMMENT 'This is different from the STATE column which is CRUD life cycle of an entity. The use case for this is to allow the user to indicate the usage status of a top-level ASBIEP (a profile BOD). An integration architect can use this column. Example values are ''Prototype'', ''Test'', and ''Production''. Only the top-level ASBIEP can use this field.';

ALTER TABLE `usage_rule` MODIFY COLUMN `name` text DEFAULT NULL COMMENT 'Short mnemonic name of the usage rule.';

ALTER TABLE `xbt` MODIFY COLUMN `builtIn_type` varchar(45) DEFAULT NULL COMMENT 'Built-in type as it should appear in the XML schema including the namespace prefix. Namespace prefix for the XML schema namespace is assumed to be ''xsd'' and a default prefix for the OAGIS built-in type.';

-- (2) Copy-pasted / factually wrong / mislabeled comment corrections.
--     e.g. columns that described a "code list" while belonging to a code list VALUE,
--     an agency ID list value, or a DT supplementary component; a min/max length mix-up;
--     join-table FK columns previously labeled "The primary key of the record"; the
--     inverted code_list.extensible_indicator; contentless "The meaning of ..." names.
ALTER TABLE `agency_id_list_value` MODIFY COLUMN `definition` text DEFAULT NULL COMMENT 'Description of the agency identification list value.';
ALTER TABLE `agency_id_list_value` MODIFY COLUMN `is_deprecated` tinyint(1) DEFAULT 0 COMMENT 'Indicates whether the agency id list value is deprecated and should not be reused (i.e., no new reference to this record should be allowed).';
ALTER TABLE `agency_id_list_value` MODIFY COLUMN `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created the agency ID list value.';
ALTER TABLE `agency_id_list_value` MODIFY COLUMN `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It identifies the user who last updated the agency ID list value.';
ALTER TABLE `agency_id_list_value` MODIFY COLUMN `creation_timestamp` datetime(6) NOT NULL DEFAULT current_timestamp(6) COMMENT 'Timestamp when the agency ID list value was created.';
ALTER TABLE `agency_id_list_value` MODIFY COLUMN `last_update_timestamp` datetime(6) NOT NULL DEFAULT current_timestamp(6) COMMENT 'Timestamp when the agency ID list value was last updated.';

ALTER TABLE `ascc` MODIFY COLUMN `state` varchar(20) DEFAULT NULL COMMENT 'Deleted, WIP, Draft, QA, Candidate, Production, Release Draft, Published. This the revision life cycle state of the ASCC.\n\nState change can''t be undone. But the history record can still keep the records of when the state was changed.';

ALTER TABLE `ascc_bizterm` MODIFY COLUMN `ascc_bizterm_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of an ascc_bizterm record.';

ALTER TABLE `bbie` MODIFY COLUMN `facet_max_length` bigint(20) unsigned DEFAULT NULL COMMENT 'Defines the maximum number of units of length.';
ALTER TABLE `bbie` MODIFY COLUMN `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who has last updated the BBIE record.';
ALTER TABLE `bbie` MODIFY COLUMN `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the BBIE was last updated.';

ALTER TABLE `bbie_bizterm` MODIFY COLUMN `bcc_bizterm_id` bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the bcc_bizterm record.';
ALTER TABLE `bbie_bizterm` MODIFY COLUMN `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the bbie_bizterm record. The creator of the bbie_bizterm is also its owner by default.';

ALTER TABLE `bbie_sc` MODIFY COLUMN `facet_max_length` bigint(20) unsigned DEFAULT NULL COMMENT 'Defines the maximum number of units of length.';

ALTER TABLE `bie_usage_rule` MODIFY COLUMN `target_bbiep_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the BBIEP table indicating the BBIEP, to which the usage rule is applied.';

ALTER TABLE `code_list` MODIFY COLUMN `extensible_indicator` tinyint(1) NOT NULL COMMENT 'This is a flag to indicate whether the code list is extensible, i.e., whether additional code values may be added or the code list may be further derived. When false, the code list is final.';

ALTER TABLE `code_list_value` MODIFY COLUMN `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created the code list value.';
ALTER TABLE `code_list_value` MODIFY COLUMN `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It identifies the user who last updated the code list value.';
ALTER TABLE `code_list_value` MODIFY COLUMN `creation_timestamp` datetime(6) NOT NULL DEFAULT current_timestamp(6) COMMENT 'Timestamp when the code list value was created.';
ALTER TABLE `code_list_value` MODIFY COLUMN `last_update_timestamp` datetime(6) NOT NULL DEFAULT current_timestamp(6) COMMENT 'Timestamp when the code list value was last updated.';

ALTER TABLE `dt_sc` MODIFY COLUMN `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created the supplementary component.';
ALTER TABLE `dt_sc` MODIFY COLUMN `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It identifies the user who last updated the supplementary component.';
ALTER TABLE `dt_sc` MODIFY COLUMN `creation_timestamp` datetime(6) NOT NULL DEFAULT current_timestamp(6) COMMENT 'Timestamp when the supplementary component was created.';
ALTER TABLE `dt_sc` MODIFY COLUMN `last_update_timestamp` datetime(6) NOT NULL DEFAULT current_timestamp(6) COMMENT 'Timestamp when the supplementary component was last updated.';

ALTER TABLE `message` MODIFY COLUMN `sender_id` bigint(20) unsigned NOT NULL COMMENT 'The user who sends this message.';
ALTER TABLE `message` MODIFY COLUMN `recipient_id` bigint(20) unsigned NOT NULL COMMENT 'The user who receives this message.';

ALTER TABLE `module_agency_id_list_manifest` MODIFY COLUMN `agency_id_list_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the agency id list manifest record.';

ALTER TABLE `release` MODIFY COLUMN `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table identifying the user who created the record.';

ALTER TABLE `tag` MODIFY COLUMN `name` varchar(100) NOT NULL COMMENT 'The name of the tag.';

ALTER TABLE `tenant` MODIFY COLUMN `name` varchar(100) DEFAULT NULL COMMENT 'The name of the tenant.';

ALTER TABLE `top_level_asbiep` MODIFY COLUMN `last_update_timestamp` datetime(6) NOT NULL DEFAULT current_timestamp(6) COMMENT 'The timestamp when any of the related BIE records was last updated.';
ALTER TABLE `top_level_asbiep` MODIFY COLUMN `release_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table. It identifies the release, for which this top-level ASBIEP is associated.';

-- (3) Newly added comments (columns that previously had no COMMENT).
ALTER TABLE `abie` MODIFY COLUMN `path` text CHARACTER SET ascii COLLATE ascii_general_ci DEFAULT NULL COMMENT 'The path of this node within the component graph; used together with HASH_PATH to locate the node.';

ALTER TABLE `acc_manifest` MODIFY COLUMN `acc_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a ACC_MANIFEST record.';
ALTER TABLE `acc_manifest` MODIFY COLUMN `release_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table.';
ALTER TABLE `acc_manifest` MODIFY COLUMN `acc_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the ACC table.';
ALTER TABLE `acc_manifest` MODIFY COLUMN `based_acc_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the ACC_MANIFEST record that this record is based on (its base/supertype from which it was derived).';
ALTER TABLE `acc_manifest` MODIFY COLUMN `prev_acc_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding ACC_MANIFEST record in the previous release (revision chain). NULL for the first revision.';
ALTER TABLE `acc_manifest` MODIFY COLUMN `next_acc_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding ACC_MANIFEST record in the next release (revision chain). NULL for the latest revision.';

ALTER TABLE `acc_manifest_tag` MODIFY COLUMN `acc_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the ACC_MANIFEST table.';
ALTER TABLE `acc_manifest_tag` MODIFY COLUMN `tag_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the TAG table.';

ALTER TABLE `agency_id_list_manifest` MODIFY COLUMN `agency_id_list_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a AGENCY_ID_LIST_MANIFEST record.';
ALTER TABLE `agency_id_list_manifest` MODIFY COLUMN `release_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table.';
ALTER TABLE `agency_id_list_manifest` MODIFY COLUMN `agency_id_list_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the AGENCY_ID_LIST table.';
ALTER TABLE `agency_id_list_manifest` MODIFY COLUMN `agency_id_list_value_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the AGENCY_ID_LIST_VALUE_MANIFEST table. It identifies the agency or organization that developed and/or maintains this agency ID list.';
ALTER TABLE `agency_id_list_manifest` MODIFY COLUMN `based_agency_id_list_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the AGENCY_ID_LIST_MANIFEST record that this record is based on (its base/supertype from which it was derived).';
ALTER TABLE `agency_id_list_manifest` MODIFY COLUMN `prev_agency_id_list_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding AGENCY_ID_LIST_MANIFEST record in the previous release (revision chain). NULL for the first revision.';
ALTER TABLE `agency_id_list_manifest` MODIFY COLUMN `next_agency_id_list_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding AGENCY_ID_LIST_MANIFEST record in the next release (revision chain). NULL for the latest revision.';

ALTER TABLE `agency_id_list_value_manifest` MODIFY COLUMN `agency_id_list_value_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a AGENCY_ID_LIST_VALUE_MANIFEST record.';
ALTER TABLE `agency_id_list_value_manifest` MODIFY COLUMN `release_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table.';
ALTER TABLE `agency_id_list_value_manifest` MODIFY COLUMN `agency_id_list_value_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the AGENCY_ID_LIST_VALUE table.';
ALTER TABLE `agency_id_list_value_manifest` MODIFY COLUMN `agency_id_list_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the AGENCY_ID_LIST_MANIFEST table.';
ALTER TABLE `agency_id_list_value_manifest` MODIFY COLUMN `based_agency_id_list_value_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the AGENCY_ID_LIST_VALUE_MANIFEST record that this record is based on (its base/supertype from which it was derived).';
ALTER TABLE `agency_id_list_value_manifest` MODIFY COLUMN `prev_agency_id_list_value_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding AGENCY_ID_LIST_VALUE_MANIFEST record in the previous release (revision chain). NULL for the first revision.';
ALTER TABLE `agency_id_list_value_manifest` MODIFY COLUMN `next_agency_id_list_value_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding AGENCY_ID_LIST_VALUE_MANIFEST record in the next release (revision chain). NULL for the latest revision.';

ALTER TABLE `app_user` MODIFY COLUMN `is_developer` tinyint(1) DEFAULT NULL COMMENT 'Indicator whether the user has a developer role or not.';
ALTER TABLE `app_user` MODIFY COLUMN `is_enabled` tinyint(1) DEFAULT 1 COMMENT 'Indicator whether the user account is enabled or not.';

ALTER TABLE `asbie` MODIFY COLUMN `path` text CHARACTER SET ascii COLLATE ascii_general_ci DEFAULT NULL COMMENT 'The path of this node within the component graph; used together with HASH_PATH to locate the node.';

ALTER TABLE `asbiep` MODIFY COLUMN `path` text CHARACTER SET ascii COLLATE ascii_general_ci DEFAULT NULL COMMENT 'The path of this node within the component graph; used together with HASH_PATH to locate the node.';

ALTER TABLE `ascc_manifest` MODIFY COLUMN `ascc_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of an ASCC_MANIFEST record.';
ALTER TABLE `ascc_manifest` MODIFY COLUMN `release_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the RELEASE table.';
ALTER TABLE `ascc_manifest` MODIFY COLUMN `ascc_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the ASCC table.';
ALTER TABLE `ascc_manifest` MODIFY COLUMN `seq_key_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the SEQ_KEY table, which records the order of this association among its siblings.';
ALTER TABLE `ascc_manifest` MODIFY COLUMN `from_acc_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the ACC_MANIFEST table pointing to the parent ACC (the FROM end of the association) of the TO_ASCCP_MANIFEST_ID.';
ALTER TABLE `ascc_manifest` MODIFY COLUMN `to_asccp_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the ASCCP_MANIFEST table pointing to the child ASCCP (the TO end of the association) of the FROM_ACC_MANIFEST_ID.';
ALTER TABLE `ascc_manifest` MODIFY COLUMN `prev_ascc_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding ASCC_MANIFEST record in the previous release (revision chain). NULL for the first revision.';
ALTER TABLE `ascc_manifest` MODIFY COLUMN `next_ascc_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding ASCC_MANIFEST record in the next release (revision chain). NULL for the latest revision.';

ALTER TABLE `asccp_manifest` MODIFY COLUMN `asccp_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a ASCCP_MANIFEST record.';
ALTER TABLE `asccp_manifest` MODIFY COLUMN `release_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table.';
ALTER TABLE `asccp_manifest` MODIFY COLUMN `asccp_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the ASCCP table.';
ALTER TABLE `asccp_manifest` MODIFY COLUMN `role_of_acc_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key to the ACC_MANIFEST record for the ACC from which this ASCCP is created (ASCCP applies role to the ACC).';
ALTER TABLE `asccp_manifest` MODIFY COLUMN `prev_asccp_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding ASCCP_MANIFEST record in the previous release (revision chain). NULL for the first revision.';
ALTER TABLE `asccp_manifest` MODIFY COLUMN `next_asccp_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding ASCCP_MANIFEST record in the next release (revision chain). NULL for the latest revision.';

ALTER TABLE `asccp_manifest_tag` MODIFY COLUMN `asccp_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the ASCCP_MANIFEST table.';
ALTER TABLE `asccp_manifest_tag` MODIFY COLUMN `tag_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the TAG table.';

ALTER TABLE `bbie` MODIFY COLUMN `path` text CHARACTER SET ascii COLLATE ascii_general_ci DEFAULT NULL COMMENT 'The path of this node within the component graph; used together with HASH_PATH to locate the node.';
ALTER TABLE `bbie` MODIFY COLUMN `example` text DEFAULT NULL COMMENT 'An example value for the BBIE.';

ALTER TABLE `bbie_sc` MODIFY COLUMN `path` text CHARACTER SET ascii COLLATE ascii_general_ci DEFAULT NULL COMMENT 'The path of this node within the component graph; used together with HASH_PATH to locate the node.';
ALTER TABLE `bbie_sc` MODIFY COLUMN `example` text DEFAULT NULL COMMENT 'An example value for the BBIE SC.';

ALTER TABLE `bbiep` MODIFY COLUMN `path` text CHARACTER SET ascii COLLATE ascii_general_ci DEFAULT NULL COMMENT 'The path of this node within the component graph; used together with HASH_PATH to locate the node.';

ALTER TABLE `bcc_manifest` MODIFY COLUMN `bcc_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a BCC_MANIFEST record.';
ALTER TABLE `bcc_manifest` MODIFY COLUMN `release_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the RELEASE table.';
ALTER TABLE `bcc_manifest` MODIFY COLUMN `bcc_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the BCC table.';
ALTER TABLE `bcc_manifest` MODIFY COLUMN `seq_key_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the SEQ_KEY table, which records the order of this association among its siblings.';
ALTER TABLE `bcc_manifest` MODIFY COLUMN `from_acc_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the ACC_MANIFEST table pointing to the parent ACC (the FROM end of the association) of the TO_BCCP_MANIFEST_ID.';
ALTER TABLE `bcc_manifest` MODIFY COLUMN `to_bccp_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the BCCP_MANIFEST table pointing to the child BCCP (the TO end of the association) of the FROM_ACC_MANIFEST_ID.';
ALTER TABLE `bcc_manifest` MODIFY COLUMN `prev_bcc_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding BCC_MANIFEST record in the previous release (revision chain). NULL for the first revision.';
ALTER TABLE `bcc_manifest` MODIFY COLUMN `next_bcc_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding BCC_MANIFEST record in the next release (revision chain). NULL for the latest revision.';

ALTER TABLE `bccp_manifest` MODIFY COLUMN `bccp_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a BCCP_MANIFEST record.';
ALTER TABLE `bccp_manifest` MODIFY COLUMN `release_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table.';
ALTER TABLE `bccp_manifest` MODIFY COLUMN `bccp_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the BCCP table.';
ALTER TABLE `bccp_manifest` MODIFY COLUMN `bdt_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the DT_MANIFEST table for the BDT (business data type) that specifies the data format of the BCCP. Only a DT whose type is BDT can be used.';
ALTER TABLE `bccp_manifest` MODIFY COLUMN `prev_bccp_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding BCCP_MANIFEST record in the previous release (revision chain). NULL for the first revision.';
ALTER TABLE `bccp_manifest` MODIFY COLUMN `next_bccp_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding BCCP_MANIFEST record in the next release (revision chain). NULL for the latest revision.';

ALTER TABLE `bccp_manifest_tag` MODIFY COLUMN `bccp_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the BCCP_MANIFEST table.';
ALTER TABLE `bccp_manifest_tag` MODIFY COLUMN `tag_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the TAG table.';

ALTER TABLE `biz_ctx_assignment` MODIFY COLUMN `biz_ctx_assignment_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a BIZ_CTX_ASSIGNMENT record.';
ALTER TABLE `biz_ctx_assignment` MODIFY COLUMN `biz_ctx_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the biz_ctx table.';

ALTER TABLE `blob_content_manifest` MODIFY COLUMN `blob_content_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a BLOB_CONTENT_MANIFEST record.';
ALTER TABLE `blob_content_manifest` MODIFY COLUMN `blob_content_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the BLOB_CONTENT table.';
ALTER TABLE `blob_content_manifest` MODIFY COLUMN `release_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table.';
ALTER TABLE `blob_content_manifest` MODIFY COLUMN `prev_blob_content_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding BLOB_CONTENT_MANIFEST record in the previous release (revision chain). NULL for the first revision.';
ALTER TABLE `blob_content_manifest` MODIFY COLUMN `next_blob_content_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding BLOB_CONTENT_MANIFEST record in the next release (revision chain). NULL for the latest revision.';

ALTER TABLE `code_list` MODIFY COLUMN `state` varchar(20) DEFAULT NULL COMMENT 'Deleted, WIP, Draft, QA, Candidate, Production, Release Draft, Published. This the revision life cycle state of the code list.\n\nState change can''t be undone. But the history record can still keep the records of when the state was changed.';

ALTER TABLE `code_list_manifest` MODIFY COLUMN `code_list_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a CODE_LIST_MANIFEST record.';
ALTER TABLE `code_list_manifest` MODIFY COLUMN `release_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table.';
ALTER TABLE `code_list_manifest` MODIFY COLUMN `code_list_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the CODE_LIST table.';
ALTER TABLE `code_list_manifest` MODIFY COLUMN `based_code_list_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the CODE_LIST_MANIFEST record that this record is based on (its base/supertype from which it was derived).';
ALTER TABLE `code_list_manifest` MODIFY COLUMN `agency_id_list_value_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the AGENCY_ID_LIST_VALUE_MANIFEST table. It identifies the agency or organization that developed and/or maintains this code list.';
ALTER TABLE `code_list_manifest` MODIFY COLUMN `prev_code_list_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding CODE_LIST_MANIFEST record in the previous release (revision chain). NULL for the first revision.';
ALTER TABLE `code_list_manifest` MODIFY COLUMN `next_code_list_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding CODE_LIST_MANIFEST record in the next release (revision chain). NULL for the latest revision.';

ALTER TABLE `code_list_value_manifest` MODIFY COLUMN `code_list_value_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a CODE_LIST_VALUE_MANIFEST record.';
ALTER TABLE `code_list_value_manifest` MODIFY COLUMN `release_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table.';
ALTER TABLE `code_list_value_manifest` MODIFY COLUMN `code_list_value_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the CODE_LIST_VALUE table.';
ALTER TABLE `code_list_value_manifest` MODIFY COLUMN `code_list_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the CODE_LIST_MANIFEST table.';
ALTER TABLE `code_list_value_manifest` MODIFY COLUMN `based_code_list_value_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the CODE_LIST_VALUE_MANIFEST record that this record is based on (its base/supertype from which it was derived).';
ALTER TABLE `code_list_value_manifest` MODIFY COLUMN `prev_code_list_value_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding CODE_LIST_VALUE_MANIFEST record in the previous release (revision chain). NULL for the first revision.';
ALTER TABLE `code_list_value_manifest` MODIFY COLUMN `next_code_list_value_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding CODE_LIST_VALUE_MANIFEST record in the next release (revision chain). NULL for the latest revision.';

ALTER TABLE `comment` MODIFY COLUMN `comment_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a COMMENT record.';
ALTER TABLE `comment` MODIFY COLUMN `reference` varchar(100) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL DEFAULT '' COMMENT 'The reference to the component the comment is associated with, in the form of the component type and its manifest ID (e.g., ''CODE_LIST-123'').';
ALTER TABLE `comment` MODIFY COLUMN `comment` text DEFAULT NULL COMMENT 'The text content of the comment.';
ALTER TABLE `comment` MODIFY COLUMN `is_hidden` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'A boolean flag indicating whether the comment is hidden. It is set instead of deletion when the comment has replies, so the reply chain is preserved.';
ALTER TABLE `comment` MODIFY COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'A boolean flag indicating whether the comment has been soft-deleted. Soft-deleted comments are excluded from queries.';
ALTER TABLE `comment` MODIFY COLUMN `prev_comment_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the previous COMMENT record in the chain.';
ALTER TABLE `comment` MODIFY COLUMN `created_by` bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.';
ALTER TABLE `comment` MODIFY COLUMN `creation_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.';
ALTER TABLE `comment` MODIFY COLUMN `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.';

ALTER TABLE `dt` MODIFY COLUMN `representation_term` varchar(100) DEFAULT NULL COMMENT 'This is the representation term assigned to the DT.';

ALTER TABLE `dt_manifest` MODIFY COLUMN `dt_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a DT_MANIFEST record.';
ALTER TABLE `dt_manifest` MODIFY COLUMN `release_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table.';
ALTER TABLE `dt_manifest` MODIFY COLUMN `dt_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the DT table.';
ALTER TABLE `dt_manifest` MODIFY COLUMN `based_dt_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the DT_MANIFEST record that this record is based on (its base/supertype from which it was derived).';
ALTER TABLE `dt_manifest` MODIFY COLUMN `prev_dt_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding DT_MANIFEST record in the previous release (revision chain). NULL for the first revision.';
ALTER TABLE `dt_manifest` MODIFY COLUMN `next_dt_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding DT_MANIFEST record in the next release (revision chain). NULL for the latest revision.';

ALTER TABLE `dt_manifest_tag` MODIFY COLUMN `dt_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the DT_MANIFEST table.';
ALTER TABLE `dt_manifest_tag` MODIFY COLUMN `tag_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the TAG table.';

ALTER TABLE `dt_sc_manifest` MODIFY COLUMN `dt_sc_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a DT_SC_MANIFEST record.';
ALTER TABLE `dt_sc_manifest` MODIFY COLUMN `release_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table.';
ALTER TABLE `dt_sc_manifest` MODIFY COLUMN `dt_sc_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the DT_SC table.';
ALTER TABLE `dt_sc_manifest` MODIFY COLUMN `owner_dt_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the DT_MANIFEST table representing the data type that owns this supplementary component.';
ALTER TABLE `dt_sc_manifest` MODIFY COLUMN `based_dt_sc_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the DT_SC_MANIFEST record that this record is based on (its base/supertype from which it was derived).';
ALTER TABLE `dt_sc_manifest` MODIFY COLUMN `prev_dt_sc_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding DT_SC_MANIFEST record in the previous release (revision chain). NULL for the first revision.';
ALTER TABLE `dt_sc_manifest` MODIFY COLUMN `next_dt_sc_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding DT_SC_MANIFEST record in the next release (revision chain). NULL for the latest revision.';

ALTER TABLE `log` MODIFY COLUMN `log_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a LOG record.';
ALTER TABLE `log` MODIFY COLUMN `reference` varchar(100) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL DEFAULT '' COMMENT 'A reference to the record for which this log is created. Because the LOG table stores logs for various component types, it cannot use a foreign key; instead it stores the GUID of the referenced component.';
ALTER TABLE `log` MODIFY COLUMN `prev_log_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the previous LOG record in the chain.';
ALTER TABLE `log` MODIFY COLUMN `next_log_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the next LOG record in the chain.';
ALTER TABLE `log` MODIFY COLUMN `created_by` bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.';
ALTER TABLE `log` MODIFY COLUMN `creation_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.';

ALTER TABLE `message` MODIFY COLUMN `message_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a MESSAGE record.';

ALTER TABLE `oauth2_app` MODIFY COLUMN `oauth2_app_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a OAUTH2_APP record.';
ALTER TABLE `oauth2_app` MODIFY COLUMN `provider_name` varchar(100) NOT NULL COMMENT 'The unique name identifying this OAuth 2.0 provider; used as the client registration ID.';
ALTER TABLE `oauth2_app` MODIFY COLUMN `issuer_uri` varchar(200) DEFAULT NULL COMMENT 'The OpenID Connect issuer URI; when set, the provider''s endpoints are discovered from its metadata.';
ALTER TABLE `oauth2_app` MODIFY COLUMN `authorization_uri` varchar(200) DEFAULT NULL COMMENT 'The authorization endpoint URI of the OAuth 2.0 provider.';
ALTER TABLE `oauth2_app` MODIFY COLUMN `token_uri` varchar(200) DEFAULT NULL COMMENT 'The token endpoint URI of the OAuth 2.0 provider.';
ALTER TABLE `oauth2_app` MODIFY COLUMN `user_info_uri` varchar(200) DEFAULT NULL COMMENT 'The UserInfo endpoint URI of the OAuth 2.0 provider.';
ALTER TABLE `oauth2_app` MODIFY COLUMN `jwk_set_uri` varchar(200) DEFAULT NULL COMMENT 'The JSON Web Key (JWK) Set URI of the OAuth 2.0 provider, used to validate tokens.';
ALTER TABLE `oauth2_app` MODIFY COLUMN `redirect_uri` varchar(200) NOT NULL COMMENT 'The redirect URI to which the OAuth 2.0 provider returns the end user after authorization.';
ALTER TABLE `oauth2_app` MODIFY COLUMN `end_session_endpoint` varchar(200) DEFAULT NULL COMMENT 'The end-session (logout) endpoint URI of the OAuth 2.0 provider.';
ALTER TABLE `oauth2_app` MODIFY COLUMN `client_id` varchar(200) NOT NULL COMMENT 'The client identifier issued to this application by the OAuth 2.0 provider.';
ALTER TABLE `oauth2_app` MODIFY COLUMN `client_secret` varchar(200) NOT NULL COMMENT 'The client secret issued to this application by the OAuth 2.0 provider.';
ALTER TABLE `oauth2_app` MODIFY COLUMN `client_authentication_method` varchar(50) NOT NULL COMMENT 'The client authentication method used with the provider (e.g. client_secret_basic, client_secret_post).';
ALTER TABLE `oauth2_app` MODIFY COLUMN `authorization_grant_type` varchar(50) NOT NULL COMMENT 'The OAuth 2.0 authorization grant type used with the provider (e.g. authorization_code).';
ALTER TABLE `oauth2_app` MODIFY COLUMN `prompt` varchar(20) DEFAULT NULL COMMENT 'The OpenID Connect ''prompt'' parameter sent to the provider''s authorization endpoint.';
ALTER TABLE `oauth2_app` MODIFY COLUMN `display_provider_name` varchar(100) DEFAULT NULL COMMENT 'The provider name displayed on the login button in the user interface.';
ALTER TABLE `oauth2_app` MODIFY COLUMN `background_color` varchar(50) DEFAULT NULL COMMENT 'The background color of the provider''s login button in the user interface.';
ALTER TABLE `oauth2_app` MODIFY COLUMN `font_color` varchar(50) DEFAULT NULL COMMENT 'The font color of the provider''s login button in the user interface.';
ALTER TABLE `oauth2_app` MODIFY COLUMN `display_order` int(11) DEFAULT 0 COMMENT 'The order in which this provider is displayed among the login options.';
ALTER TABLE `oauth2_app` MODIFY COLUMN `is_disabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'A flag indicating whether this OAuth 2.0 provider is disabled (1 = disabled, 0 = enabled).';

ALTER TABLE `oauth2_app_scope` MODIFY COLUMN `oauth2_app_scope_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of an OAUTH2_APP_SCOPE record.';
ALTER TABLE `oauth2_app_scope` MODIFY COLUMN `oauth2_app_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the OAUTH2_APP table.';
ALTER TABLE `oauth2_app_scope` MODIFY COLUMN `scope` varchar(100) NOT NULL COMMENT 'A single OAuth2 scope (e.g., openid, profile, email) requested for the associated OAUTH2_APP.';

ALTER TABLE `seq_key` MODIFY COLUMN `seq_key_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a SEQ_KEY record.';
ALTER TABLE `seq_key` MODIFY COLUMN `from_acc_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the ACC_MANIFEST table pointing to the ACC that owns the association (ASCC/BCC) ordered by this sequence key.';
ALTER TABLE `seq_key` MODIFY COLUMN `ascc_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the ASCC_MANIFEST table for the ASCC association ordered by this record. Exactly one of ASCC_MANIFEST_ID / BCC_MANIFEST_ID is non-null.';
ALTER TABLE `seq_key` MODIFY COLUMN `bcc_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the BCC_MANIFEST table for the BCC association ordered by this record. Exactly one of ASCC_MANIFEST_ID / BCC_MANIFEST_ID is non-null.';
ALTER TABLE `seq_key` MODIFY COLUMN `prev_seq_key_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the previous SEQ_KEY record in the ordering chain; NULL for the first association.';
ALTER TABLE `seq_key` MODIFY COLUMN `next_seq_key_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the next SEQ_KEY record in the ordering chain; NULL for the last association.';

ALTER TABLE `top_level_asbiep` MODIFY COLUMN `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership.';
ALTER TABLE `top_level_asbiep` MODIFY COLUMN `state` varchar(20) DEFAULT NULL COMMENT 'The life cycle state of the top-level ASBIEP. Possible values are Initiating, WIP, QA, and Production.';

ALTER TABLE `xbt_manifest` MODIFY COLUMN `xbt_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a XBT_MANIFEST record.';
ALTER TABLE `xbt_manifest` MODIFY COLUMN `release_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table.';
ALTER TABLE `xbt_manifest` MODIFY COLUMN `xbt_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the XBT table.';
ALTER TABLE `xbt_manifest` MODIFY COLUMN `prev_xbt_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding XBT_MANIFEST record in the previous release (revision chain). NULL for the first revision.';
ALTER TABLE `xbt_manifest` MODIFY COLUMN `next_xbt_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the corresponding XBT_MANIFEST record in the next release (revision chain). NULL for the latest revision.';

-- (4) Deprecation of asbie.seq_key / bbie.seq_key.
--     These columns are no longer used; sibling ordering is now carried by the
--     standalone `seq_key` table. The original description is retained after the notice.
ALTER TABLE `asbie` MODIFY COLUMN `seq_key` decimal(10, 2) NOT NULL COMMENT 'DEPRECATED. Instead, use `seq_key` table. This indicates the order of the associations among other siblings. The SEQ_KEY for BIEs is decimal in order to accommodate the removal of inheritance hierarchy and group. For example, children of the most abstract ACC will have SEQ_KEY = 1.1, 1.2, 1.3, and so on; and SEQ_KEY of the next abstraction level ACC will have SEQ_KEY = 2.1, 2.2, 2.3 and so on so forth.';

ALTER TABLE `bbie` MODIFY COLUMN `seq_key` decimal(10, 2) DEFAULT NULL COMMENT 'DEPRECATED. Instead, use `seq_key` table. This indicates the order of the associations among other siblings. The SEQ_KEY for BIEs is decimal in order to accommodate the removal of inheritance hierarchy and group. For example, children of the most abstract ACC will have SEQ_KEY = 1.1, 1.2, 1.3, and so on; and SEQ_KEY of the next abstraction level ACC will have SEQ_KEY = 2.1, 2.2, 2.3 and so on so forth.';

-- ------------------------------------------------------------------
-- Issue #1759 - Augment schema tables and comments
-- ------------------------------------------------------------------
--
-- Adds a table-level COMMENT to the tables that previously had none,
-- mirroring the per-table .ddl files. Comments only; no structural change.
ALTER TABLE `acc_manifest` COMMENT = 'The ACC_MANIFEST table is a release-specific handle to an ACC record, pinning a particular ACC (a complex data structured concept such as OAGIS''s Components, Nouns, and BODs) to a RELEASE. It carries the revision chain via the PREV_ACC_MANIFEST_ID and NEXT_ACC_MANIFEST_ID self-references across releases, and also records the based (supertype) and replacement manifests for the ACC in that release.';
ALTER TABLE `acc_manifest_tag` COMMENT = 'This is a many-to-many join table that assigns TAG rows to ACC_MANIFEST rows, associating tags with a release-specific ACC.';
ALTER TABLE `agency_id_list_manifest` COMMENT = 'The AGENCY_ID_LIST_MANIFEST table is a release-specific handle to an AGENCY_ID_LIST record; it pins the agency identification list to a RELEASE and carries the revision chain (prev/next manifest) across releases.';
ALTER TABLE `agency_id_list_value_manifest` COMMENT = 'A release-specific handle to an AGENCY_ID_LIST_VALUE record, pinning a value within an agency identification list to a RELEASE and to its owning AGENCY_ID_LIST_MANIFEST. It carries the revision chain, referencing the corresponding AGENCY_ID_LIST_VALUE_MANIFEST records in the previous and next releases.';
ALTER TABLE `app_oauth2_user` COMMENT = 'This table captures the OpenID Connect claims (such as the sub, name, and email) of a user authenticated through an external OAuth2 provider registered in OAUTH2_APP, linking that identity to a record in APP_USER. When the APP_USER reference is not set, the record is treated as a pending one that has not yet been associated with an application user.';
ALTER TABLE `ascc_manifest` COMMENT = 'A release-specific handle to an ASCC record, which represents a relationship/association between two ACCs through an ASCCP. It pins the ASCC to a RELEASE, resolves the association''s FROM_ACC_MANIFEST_ID and TO_ASCCP_MANIFEST_ID ends, and carries the revision chain (PREV/NEXT_ASCC_MANIFEST_ID) across releases.';
ALTER TABLE `asccp_manifest` COMMENT = 'A release-specific handle to an ASCCP, which specifies a role (or property) an ACC may play under another ACC; it pins the ASCCP to a RELEASE and carries the revision chain to the corresponding ASCCP_MANIFEST records in the previous and next releases.';
ALTER TABLE `asccp_manifest_tag` COMMENT = 'This is an intersection table that assigns TAG rows to ASCCP_MANIFEST rows, allowing a release-specific ASCCP handle to be tagged with one or more TAGs in a many-to-many relationship.';
ALTER TABLE `bcc_manifest` COMMENT = 'BCC_MANIFEST is a release-specific handle to a BCC, which represents a relationship/association between an ACC and a BCCP that creates a data element for an ACC. It pins the BCC to a RELEASE (linking the FROM_ACC_MANIFEST and TO_BCCP_MANIFEST ends within that release) and carries the revision chain to the corresponding records in the previous and next releases.';
ALTER TABLE `bccp_manifest` COMMENT = 'A BCCP_MANIFEST is a release-specific handle to a BCCP record, pinning it to a RELEASE and carrying the revision chain to the corresponding BCCP_MANIFEST in the previous and next releases. A BCCP specifies a property concept and the data type associated with it, which can then be added as a property of an ACC; the manifest also binds the BCCP to the BDT that specifies its data format through BDT_MANIFEST_ID (DT_MANIFEST).';
ALTER TABLE `bccp_manifest_tag` COMMENT = 'This is an intersection table that assigns TAG rows to BCCP_MANIFEST rows, allowing a many-to-many relationship between tags and BCCP manifests.';
ALTER TABLE `bie_package` COMMENT = 'The BIE_PACKAGE table stores information about a BIE package, which groups a set of top-level BIEs (TOP_LEVEL_ASBIEP records, associated through the BIE_PACKAGE_TOP_LEVEL_ASBIEP table) released together under a common package version within a LIBRARY. It carries the revision life cycle state and a chain to the previous version of the package, and may reference a source BIE_PACKAGE from which it was created by a Copy or Uplift action.';
ALTER TABLE `bie_package_top_level_asbiep` COMMENT = 'This is an intersection table that assigns TOP_LEVEL_ASBIEP records to a BIE_PACKAGE, capturing the top-level BIEs that make up the package. The referenced TOP_LEVEL_ASBIEP must belong to the same RELEASE as the BIE_PACKAGE, and the PREV_TOP_LEVEL_ASBIEP_ID column tracks the previous version of the Top-Level ASBIEP within the package.';
ALTER TABLE `biz_ctx_assignment` COMMENT = 'This is an intersection table that assigns business contexts to a top-level ASBIEP. It provides the many-to-many associations between the BIZ_CTX and TOP_LEVEL_ASBIEP tables.';
ALTER TABLE `blob_content_manifest` COMMENT = 'The BLOB_CONTENT_MANIFEST table is a release-specific handle to a BLOB_CONTENT record, which stores a schema whose content is only imported as a whole and is represented in Blob. It pins the BLOB_CONTENT to a RELEASE and carries the revision chain via its previous and next manifest self-references across releases.';
ALTER TABLE `code_list_manifest` COMMENT = 'The CODE_LIST_MANIFEST table is a release-specific handle to a CODE_LIST record, pinning that code list to a particular RELEASE and carrying the revision chain (the PREV/NEXT self-references linking the same code list across releases). When a code list is derived from another, the BASED_CODE_LIST_MANIFEST_ID self-reference records its base within the release.';
ALTER TABLE `code_list_value_manifest` COMMENT = 'A release-specific handle to a CODE_LIST_VALUE record, pinning a code list value of a code list to a RELEASE (and to its owning CODE_LIST_MANIFEST) and carrying the revision chain via the PREV/NEXT self-references across releases.';
ALTER TABLE `comment` COMMENT = 'This table stores user comments associated with a component, where the associated component is identified by the REFERENCE column in the form of the component type and its manifest ID (e.g., ''CODE_LIST-123''). Replies are captured through the self-referencing PREV_COMMENT_ID chain, and a comment with replies is hidden via IS_HIDDEN instead of being deleted so the reply chain is preserved, while IS_DELETED marks a comment as soft-deleted and excluded from queries rather than physically removing it.';
ALTER TABLE `dt_manifest` COMMENT = 'The DT_MANIFEST table is a release-specific handle to a DT record (both CDT and BDT, as stored in the DT table), pinning it to a particular RELEASE. It carries the revision chain across releases through the PREV_DT_MANIFEST_ID and NEXT_DT_MANIFEST_ID self-references, and records the DT''s base/supertype via BASED_DT_MANIFEST_ID.';
ALTER TABLE `dt_manifest_tag` COMMENT = 'A many-to-many join table that assigns TAG rows to DT_MANIFEST rows, attaching tags to the release-specific manifest of a data type (DT).';
ALTER TABLE `dt_sc_manifest` COMMENT = 'A release-specific handle to a DT_SC record, pinning the supplementary component (SC) of a data type to a RELEASE and linking it to the owning DT_MANIFEST. It carries the revision chain to the corresponding DT_SC_MANIFEST in the previous and next releases (PREV_DT_SC_MANIFEST_ID/NEXT_DT_SC_MANIFEST_ID), though as noted for DT_SC the supplementary component is an intrinsic part of the DT and is re-created with each new revision of the data type.';
ALTER TABLE `exception` COMMENT = 'This table logs exceptions raised by the application, capturing the exception message, its serialized stacktrace, and a searchable tag, along with the APP_USER who was working when the exception occurred and the time it was created.';
ALTER TABLE `library` COMMENT = 'This table stores information about a library, the top-level workspace that groups the components, code lists, and releases managed within it. It captures the library''s name, type, owning organization, description, application domain, and state, and flags whether the library is read-only or the default shown to users without a preference.';
ALTER TABLE `log` COMMENT = 'The LOG table records the revision history of component records, storing one entry per change with the component''s serialized state SNAPSHOT, its REVISION_NUM and REVISION_TRACKING_NUM, and the LOG_ACTION taken. Because it logs many different component types, it references the target component by its GUID (the REFERENCE column) rather than a foreign key, and the PREV_LOG_ID and NEXT_LOG_ID columns chain the entries into a per-component revision history.';
ALTER TABLE `message` COMMENT = 'This table stores messages exchanged between users, referencing APP_USER for both the sender and the recipient, along with each message''s subject, body, and read status.';
ALTER TABLE `module_acc_manifest` COMMENT = 'This intersection table assigns ACC_MANIFEST components to a MODULE within a MODULE_SET_RELEASE, indicating the physical file into which each ACC will be generated during the expression generation.';
ALTER TABLE `module_agency_id_list_manifest` COMMENT = 'This table assigns an AGENCY_ID_LIST_MANIFEST to a MODULE within a MODULE_SET_RELEASE, indicating the physical file into which the agency identification list will be generated during the expression generation.';
ALTER TABLE `module_asccp_manifest` COMMENT = 'This table assigns ASCCP_MANIFEST components to a MODULE within a MODULE_SET_RELEASE, indicating the physical file into which each ASCCP will be generated during the expression generation.';
ALTER TABLE `module_bccp_manifest` COMMENT = 'This is an intersection table that assigns a BCCP_MANIFEST to a MODULE within a MODULE_SET_RELEASE, indicating the physical file into which the BCCP will be generated during the expression generation.';
ALTER TABLE `module_blob_content_manifest` COMMENT = 'This table assigns BLOB_CONTENT_MANIFEST records to a MODULE within a MODULE_SET_RELEASE, associating each imported whole-schema blob with the physical file into which it is emitted during schema generation and export.';
ALTER TABLE `module_code_list_manifest` COMMENT = 'This table assigns a CODE_LIST_MANIFEST to a MODULE within a MODULE_SET_RELEASE, indicating the physical file into which the code list is generated during the expression generation.';
ALTER TABLE `module_dt_manifest` COMMENT = 'This intersection table assigns DT_MANIFEST components to a MODULE within a MODULE_SET_RELEASE, indicating the physical file into which each data type will be generated during expression generation.';
ALTER TABLE `module_set` COMMENT = 'This table stores information about a module set, which is a named, library-scoped collection of MODULE records that organizes CC components into physical schema files. A module set is pinned to a RELEASE through the MODULE_SET_RELEASE table for use in expression/schema generation.';
ALTER TABLE `module_set_release` COMMENT = 'This table pairs a MODULE_SET with a RELEASE, associating a set of MODULEs with a particular release for schema generation and export. Each pairing has its own name and description, and the IS_DEFAULT indicator marks the default module set to be used for a release.';
ALTER TABLE `module_xbt_manifest` COMMENT = 'This table assigns XBT_MANIFEST components (the XML Schema and OAGIS built-in types) to a MODULE within a MODULE_SET_RELEASE, so that they are generated into the appropriate physical schema file during expression generation.';
ALTER TABLE `oas_doc` COMMENT = 'The root of the OpenAPI Specification document object model that the other OAS_* tables hang off; each row is an OpenAPI Object holding the openapi version string together with the Info Object metadata (title, description, terms of service, version, and contact and license details).';
ALTER TABLE `oas_doc_tag` COMMENT = 'A many-to-many join assigning OAS_TAG rows to an OAS_DOC, populating the OpenAPI document''s root-level tags array.';
ALTER TABLE `oas_example` COMMENT = 'OpenAPI Example Object holding a single example, either an embedded literal in the value field or a reference to an external example via the ref (externalValue) field; the two are mutually exclusive.';
ALTER TABLE `oas_external_doc_doc` COMMENT = 'A many-to-many join that attaches OAS_EXTERNAL_DOC external documentation references to OAS_DOC OpenAPI documents.';
ALTER TABLE `oas_http_header` COMMENT = 'OpenAPI Header Object, defining an HTTP header by its name, description, and schema type reference ($ref); attached to an OAS_RESPONSE through the OAS_RESPONSE_HEADERS join and referenced by an OAS_PARAMETER when the parameter location is header.';
ALTER TABLE `oas_media_type` COMMENT = 'OpenAPI Media Type Object, which represents a media type (such as application/json) used within the content of a request body or response in an OpenAPI document.';
ALTER TABLE `oas_message_body` COMMENT = 'The OAS_MESSAGE_BODY table holds an OpenAPI message body whose schema is defined by the referenced TOP_LEVEL_ASBIEP. The OAS_REQUEST and OAS_RESPONSE tables both reference this table to bind that BIE as the content of an operation''s request or response body.';
ALTER TABLE `oas_operation` COMMENT = 'OpenAPI Operation Object; a single HTTP verb (get, put, post, delete, etc.) exposed on an OAS_RESOURCE path item, carrying its operationId, summary, description, deprecation flag, and defaulted error-response body settings, with any per-operation security overrides captured in OAS_OPERATION_SECURITY.';
ALTER TABLE `oas_parameter` COMMENT = 'OpenAPI Parameter Object describing a single operation parameter, identified by its name and location (in = query, header, path, or cookie) along with its required, schema type, and serialization settings; when in = header it may reference an OAS_HTTP_HEADER.';
ALTER TABLE `oas_parameter_link` COMMENT = 'OpenAPI Link Object parameter binding declared on an OAS_RESPONSE; each row ties an OAS_PARAMETER to a runtime EXPRESSION that supplies its value, optionally targeting the linked OAS_OPERATION.';
ALTER TABLE `oas_request` COMMENT = 'OpenAPI Request Body Object for an OAS_OPERATION; it defines the operation''s request payload from a BIE-based OAS_MESSAGE_BODY, along with whether the body is required and generation options such as array wrapping, root suppression, and optional meta-header and pagination TOP_LEVEL_ASBIEP references.';
ALTER TABLE `oas_request_parameter` COMMENT = 'A many-to-many join assigning OAS_PARAMETER entries (an operation''s query, header, path, or cookie parameters) to an OAS_REQUEST.';
ALTER TABLE `oas_resource` COMMENT = 'A resource (path) belonging to an OpenAPI document; each row is an entry of the OAS_DOC Paths Object, keyed by PATH (the OpenAPI path, defaulting to the BIE name) and optionally pointing to an externally defined Path Item Object via the REF ($ref) column.';
ALTER TABLE `oas_resource_tag` COMMENT = 'A many-to-many join assigning OAS_TAG entries to an OAS_OPERATION, representing the tags list of an OpenAPI Operation Object that groups the operation under those tags.';
ALTER TABLE `oas_response` COMMENT = 'OpenAPI Response Object owned by an OAS_OPERATION; one entry of the operation''s responses map keyed by HTTP status code, binding the response body to an OAS_MESSAGE_BODY (which references a BIE) with optional meta-header and pagination TOP_LEVEL_ASBIEP references.';
ALTER TABLE `oas_response_headers` COMMENT = 'The headers map of an OpenAPI Response Object; a many-to-many join assigning OAS_HTTP_HEADER definitions to an OAS_RESPONSE.';
ALTER TABLE `oas_server` COMMENT = 'OpenAPI Server Object; one entry of the servers array of an OAS_DOC, providing connectivity information to a target host via a URL (optionally with a description and URL-template variables).';
ALTER TABLE `oas_server_variable` COMMENT = 'OpenAPI Server Variable Object; one named entry of an OAS_SERVER''s variables map, holding the default value, optional enum, and description used for server URL template substitution.';
ALTER TABLE `oas_tag` COMMENT = 'OpenAPI Tag Object; adds metadata to a single tag (a name and an optional description) that is used to group the Operation Objects of an OpenAPI document.';
ALTER TABLE `oauth2_app` COMMENT = 'This table stores OAuth 2.0 / OpenID Connect provider registrations, including client credentials, endpoint URIs, and login-button display settings, used to authenticate end users.';
ALTER TABLE `oauth2_app_scope` COMMENT = 'This table captures the OAuth2 scopes requested for an OAUTH2_APP, storing one scope value per row.';
ALTER TABLE `seq_key` COMMENT = 'This table stores the ordering of the associations (ASCC and BCC) that belong to an ACC. Each record points, through FROM_ACC_MANIFEST_ID, to the owning ACC via ACC_MANIFEST and, through exactly one of ASCC_MANIFEST_ID or BCC_MANIFEST_ID, to the ordered association; the PREV_SEQ_KEY_ID and NEXT_SEQ_KEY_ID columns form a doubly-linked chain that defines the sequence of these associations within the ACC.';
ALTER TABLE `tag` COMMENT = 'The TAG table stores the tags (each with a name, description, and text and background color) that can be attached to core component and data type manifests through the intersection tables ACC_MANIFEST_TAG, ASCCP_MANIFEST_TAG, BCCP_MANIFEST_TAG, and DT_MANIFEST_TAG.';
ALTER TABLE `text_template` COMMENT = 'This table stores named text templates, each with a subject, content type, and body containing placeholders that are substituted at rendering time. Templates are looked up by name to compose output such as email messages.';
ALTER TABLE `xbt_manifest` COMMENT = 'A release-specific handle to an XBT record, pinning an XML schema built-in type or OAGIS built-in type to a RELEASE and carrying the revision chain (PREV_XBT_MANIFEST_ID and NEXT_XBT_MANIFEST_ID) across releases. It also records how the built-in type maps to an allowed CDT primitive via CDT_PRI.';

SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
