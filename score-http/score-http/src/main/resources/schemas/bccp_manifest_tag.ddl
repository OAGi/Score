CREATE TABLE `bccp_manifest_tag`
(
    `bccp_manifest_id`   bigint(20) unsigned NOT NULL,
    `tag_id`             bigint(20) unsigned NOT NULL,
    `created_by`         bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the record.',
    `creation_timestamp` datetime(6)         NOT NULL COMMENT 'Timestamp when the record was first created.',
    PRIMARY KEY (`bccp_manifest_id`, `tag_id`),
    KEY `bccp_manifest_tag_bccp_manifest_id_fk` (`bccp_manifest_id`),
    KEY `bccp_manifest_tag_tag_id_fk` (`tag_id`),
    KEY `bccp_manifest_tag_created_by_fk` (`created_by`),
    CONSTRAINT `bccp_manifest_tag_bccp_manifest_id_fk` FOREIGN KEY (`bccp_manifest_id`) REFERENCES `bccp_manifest` (`bccp_manifest_id`),
    CONSTRAINT `bccp_manifest_tag_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `bccp_manifest_tag_tag_id_fk` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`tag_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;