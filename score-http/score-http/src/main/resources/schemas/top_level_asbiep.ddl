CREATE TABLE `top_level_asbiep`
(
    `top_level_asbiep_id`   bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an top-level ASBIEP.',
    `asbiep_id`             bigint(20) unsigned          DEFAULT NULL COMMENT 'Foreign key to the ASBIEP table pointing to a record which is a top-level ASBIEP.',
    `owner_user_id`         bigint(20) unsigned NOT NULL,
    `last_update_timestamp` datetime(6)         NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'The timestamp when among all related bie records was last updated.',
    `last_updated_by`       bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated any related bie records.',
    `release_id`            bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table. It identifies the release, for which this module is associated.',
    `version`               varchar(45)                  DEFAULT NULL COMMENT 'This column hold a version number assigned by the user. This column is only used by the top-level ASBIEP. No format of version is enforced.',
    `status`                varchar(45)                  DEFAULT NULL COMMENT 'This is different from the STATE column which is CRUD life cycle of an entity. The use case for this is to allow the user to indicate the usage status of a top-level ASBIEP (a profile BOD). An integration architect can use this column. Example values are ?Prototype?, ?Test?, and ?Production?. Only the top-level ASBIEP can use this field.',
    `state`                 varchar(20)                  DEFAULT NULL,
    `inverse_mode`          tinyint(1)          NOT NULL DEFAULT '0' COMMENT 'If this is true, all BIEs not edited by users under this TOP_LEVEL_ASBIEP will be treated as used BIEs.',
    PRIMARY KEY (`top_level_asbiep_id`),
    KEY `top_level_asbiep_asbiep_id_fk` (`asbiep_id`),
    KEY `top_level_asbiep_owner_user_id_fk` (`owner_user_id`),
    KEY `top_level_asbiep_release_id_fk` (`release_id`),
    KEY `top_level_asbiep_last_updated_by_fk` (`last_updated_by`),
    CONSTRAINT `top_level_asbiep_asbiep_id_fk` FOREIGN KEY (`asbiep_id`) REFERENCES `asbiep` (`asbiep_id`),
    CONSTRAINT `top_level_asbiep_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `top_level_asbiep_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `top_level_asbiep_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='This table indexes the ASBIEP which is a top-level ASBIEP. This table and the owner_top_level_asbiep_id column in all BIE tables allow all related BIEs to be retrieved all at once speeding up the profile BOD transactions.';