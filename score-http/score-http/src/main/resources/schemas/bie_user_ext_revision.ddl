CREATE TABLE `bie_user_ext_revision`
(
    `bie_user_ext_revision_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
    `ext_abie_id`              bigint(20) unsigned          DEFAULT NULL COMMENT 'This points to an ABIE record corresponding to the EXTENSION_ACC_ID record. For example, this column can point to the ApplicationAreaExtension ABIE which is based on the ApplicationAreaExtension ACC (referred to by the EXT_ACC_ID column). This column can be NULL only when the extension is the AllExtension because there is no corresponding ABIE for the AllExtension ACC.',
    `ext_acc_id`               bigint(20) unsigned NOT NULL COMMENT 'This points to an extension ACC on which the ABIE indicated by the EXT_ABIE_ID column is based. E.g. It may point to an ApplicationAreaExtension ACC, AllExtension ACC, ActualLedgerExtension ACC, etc. It should be noted that an ACC record pointed to must have the OAGIS_COMPONENT_TYPE = 2 (Extension).',
    `user_ext_acc_id`          bigint(20) unsigned NOT NULL COMMENT 'This column points to the specific revision of a User Extension ACC (this is an ACC whose OAGIS_COMPONENT_TYPE = 4) currently used by the ABIE as indicated by the EXT_ABIE_ID or the by the TOP_LEVEL_ABIE_ID (in case of the AllExtension). ',
    `revised_indicator`        tinyint(1)          NOT NULL DEFAULT '0' COMMENT 'This column is a flag indicating to whether the User Extension ACC (as identified in the USER_EXT_ACC_ID column) has been revised, i.e., there is a newer version of the user extension ACC than the one currently used by the EXT_ABIE_ID. 0 means the USER_EXT_ACC_ID is current, 1 means it is not current.',
    `top_level_asbiep_id`      bigint(20) unsigned NOT NULL COMMENT 'This is a foreign key to the top-level ASBIEP.',
    PRIMARY KEY (`bie_user_ext_revision_id`),
    KEY `bie_user_ext_revision_ext_abie_id_fk` (`ext_abie_id`),
    KEY `bie_user_ext_revision_ext_acc_id_fk` (`ext_acc_id`),
    KEY `bie_user_ext_revision_user_ext_acc_id_fk` (`user_ext_acc_id`),
    KEY `bie_user_ext_revision_top_level_asbiep_id_fk` (`top_level_asbiep_id`),
    CONSTRAINT `bie_user_ext_revision_ext_abie_id_fk` FOREIGN KEY (`ext_abie_id`) REFERENCES `abie` (`abie_id`),
    CONSTRAINT `bie_user_ext_revision_ext_acc_id_fk` FOREIGN KEY (`ext_acc_id`) REFERENCES `acc` (`acc_id`),
    CONSTRAINT `bie_user_ext_revision_top_level_asbiep_id_fk` FOREIGN KEY (`top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`),
    CONSTRAINT `bie_user_ext_revision_user_ext_acc_id_fk` FOREIGN KEY (`user_ext_acc_id`) REFERENCES `acc` (`acc_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='This table is a log of events. It keeps track of the User Extension ACC (the specific revision) used by an Extension ABIE. This can be a named extension (such as ApplicationAreaExtension) or the AllExtension. The REVISED_INDICATOR flag is designed such that a revision of a User Extension can notify the user of a top-level ABIE by setting this flag to true. The TOP_LEVEL_ABIE_ID column makes it more efficient to when opening a top-level ABIE, the user can be notified of any new revision of the extension. A record in this table is created only when there is a user extension to the the OAGIS extension component/ACC.';