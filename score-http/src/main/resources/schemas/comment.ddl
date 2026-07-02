CREATE TABLE `comment`
(
    `comment_id`            bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a COMMENT record.',
    `reference`             varchar(100) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL DEFAULT '' COMMENT 'The reference to the component the comment is associated with, in the form of the component type and its manifest ID (e.g., ''CODE_LIST-123'').',
    `comment`               text                                                               DEFAULT NULL COMMENT 'The text content of the comment.',
    `is_hidden`             tinyint(1) NOT NULL DEFAULT 0 COMMENT 'A boolean flag indicating whether the comment is hidden. It is set instead of deletion when the comment has replies, so the reply chain is preserved.',
    `is_deleted`            tinyint(1) NOT NULL DEFAULT 0 COMMENT 'A boolean flag indicating whether the comment has been soft-deleted. Soft-deleted comments are excluded from queries.',
    `prev_comment_id`       bigint(20) unsigned DEFAULT NULL COMMENT 'A self-reference to the previous COMMENT record in the chain.',
    `created_by`            bigint(20) unsigned NOT NULL COMMENT 'The user who creates the record.',
    `creation_timestamp`    datetime(6) NOT NULL COMMENT 'The timestamp when the record is created.',
    `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record is last updated.',
    PRIMARY KEY (`comment_id`),
    KEY                     `reference` (`reference`),
    KEY                     `comment_created_by_fk` (`created_by`),
    KEY                     `comment_prev_comment_id_fk` (`prev_comment_id`),
    CONSTRAINT `comment_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `comment_prev_comment_id_fk` FOREIGN KEY (`prev_comment_id`) REFERENCES `comment` (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='This table stores user comments associated with a component, where the associated component is identified by the REFERENCE column in the form of the component type and its manifest ID (e.g., ''CODE_LIST-123''). Replies are captured through the self-referencing PREV_COMMENT_ID chain, and a comment with replies is hidden via IS_HIDDEN instead of being deleted so the reply chain is preserved, while IS_DELETED marks a comment as soft-deleted and excluded from queries rather than physically removing it.';