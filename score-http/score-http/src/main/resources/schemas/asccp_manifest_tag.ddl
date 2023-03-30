CREATE TABLE `asccp_manifest_tag`
(
    `asccp_manifest_id`  bigint(20) unsigned NOT NULL,
    `tag_id`             bigint(20) unsigned NOT NULL,
    `created_by`         bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the record.',
    `creation_timestamp` datetime(6)         NOT NULL COMMENT 'Timestamp when the record was first created.',
    PRIMARY KEY (`asccp_manifest_id`, `tag_id`),
    KEY `asccp_manifest_tag_asccp_manifest_id_fk` (`asccp_manifest_id`),
    KEY `asccp_manifest_tag_tag_id_fk` (`tag_id`),
    KEY `asccp_manifest_tag_created_by_fk` (`created_by`),
    CONSTRAINT `asccp_manifest_tag_asccp_manifest_id_fk` FOREIGN KEY (`asccp_manifest_id`) REFERENCES `asccp_manifest` (`asccp_manifest_id`),
    CONSTRAINT `asccp_manifest_tag_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `asccp_manifest_tag_tag_id_fk` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`tag_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;