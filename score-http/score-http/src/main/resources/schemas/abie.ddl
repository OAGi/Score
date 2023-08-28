CREATE TABLE `abie`
(
    `abie_id`                   bigint(20) unsigned                                      NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an ABIE.',
    `guid`                      char(32) CHARACTER SET ascii COLLATE ascii_general_ci    NOT NULL COMMENT 'A globally unique identifier (GUID).',
    `based_acc_manifest_id`     bigint(20) unsigned                                      NOT NULL COMMENT 'A foreign key to the ACC_MANIFEST table refering to the ACC, on which the business context has been applied to derive this ABIE.',
    `path`                      text CHARACTER SET ascii COLLATE ascii_general_ci DEFAULT NULL,
    `hash_path`                 varchar(64) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL COMMENT 'hash_path generated from the path of the component graph using hash function, so that it is unique in the graph.',
    `biz_ctx_id`                bigint(20) unsigned                               DEFAULT NULL COMMENT '(Deprecated) A foreign key to the BIZ_CTX table. This column stores the business context assigned to the ABIE.',
    `definition`                text                                              DEFAULT NULL COMMENT 'Definition to override the ACC''s definition. If NULL, it means that the definition should be inherited from the based CC.',
    `created_by`                bigint(20) unsigned                                      NOT NULL COMMENT 'A foreign key referring to the user who creates the ABIE. The creator of the ABIE is also its owner by default. ABIEs created as children of another ABIE have the same CREATED_BY as its parent.',
    `last_updated_by`           bigint(20) unsigned                                      NOT NULL COMMENT 'A foreign key referring to the last user who has updated the ABIE record. This may be the user who is in the same group as the creator.',
    `creation_timestamp`        datetime(6)                                              NOT NULL COMMENT 'Timestamp when the ABIE record was first created. ABIEs created as children of another ABIE have the same CREATION_TIMESTAMP.',
    `last_update_timestamp`     datetime(6)                                              NOT NULL COMMENT 'The timestamp when the ABIE was last updated.',
    `state`                     int(11)                                           DEFAULT NULL COMMENT '2 = EDITING, 4 = PUBLISHED. This column is only used with a top-level ABIE, because that is the only entry point for editing. The state value indicates the visibility of the top-level ABIE to users other than the owner. In the user group environment, a logic can apply that other users in the group can see the top-level ABIE only when it is in the ''Published'' state.',
    `remark`                    varchar(225)                                      DEFAULT NULL COMMENT 'This column allows the user to specify very context-specific usage of the BIE. It is different from the DEFINITION column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be "Type of BOM should be recognized in the BOM/typeCode."',
    `biz_term`                  varchar(225)                                      DEFAULT NULL COMMENT 'To indicate what the BIE is called in a particular business context. With this current design, only one business term is allowed per business context.',
    `owner_top_level_asbiep_id` bigint(20) unsigned                                      NOT NULL COMMENT 'This is a foreign key to the top-level ASBIEP.',
    PRIMARY KEY (`abie_id`),
    KEY `abie_biz_ctx_id_fk` (`biz_ctx_id`),
    KEY `abie_created_by_fk` (`created_by`),
    KEY `abie_last_updated_by_fk` (`last_updated_by`),
    KEY `abie_owner_top_level_asbiep_id_fk` (`owner_top_level_asbiep_id`),
    KEY `abie_based_acc_manifest_id_fk` (`based_acc_manifest_id`),
    KEY `abie_path_k` (`path`(3072)),
    KEY `abie_hash_path_k` (`hash_path`),
    CONSTRAINT `abie_based_acc_manifest_id_fk` FOREIGN KEY (`based_acc_manifest_id`) REFERENCES `acc_manifest` (`acc_manifest_id`),
    CONSTRAINT `abie_biz_ctx_id_fk` FOREIGN KEY (`biz_ctx_id`) REFERENCES `biz_ctx` (`biz_ctx_id`),
    CONSTRAINT `abie_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `abie_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `abie_owner_top_level_asbiep_id_fk` FOREIGN KEY (`owner_top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3
  COLLATE = utf8mb3_general_ci COMMENT ='The ABIE table stores information about an ABIE, which is a contextualized ACC. The context is represented by the BUSINESS_CTX_ID column that refers to a business context. Each ABIE must have a business context and a based ACC.\n\nIt should be noted that, per design document, there is no corresponding ABIE created for an ACC which will not show up in the instance document such as ACCs of OAGIS_COMPONENT_TYPE "SEMANTIC_GROUP", "USER_EXTENSION_GROUP", etc.';