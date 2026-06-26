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

SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
