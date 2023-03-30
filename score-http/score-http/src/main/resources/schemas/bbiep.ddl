CREATE TABLE `bbiep`
(
    `bbiep_id`                  bigint(20) unsigned             NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an BBIEP.',
    `guid`                      char(32) CHARACTER SET ascii    NOT NULL COMMENT 'A globally unique identifier (GUID).',
    `based_bccp_manifest_id`    bigint(20) unsigned             NOT NULL COMMENT 'A foreign key pointing to the BCCP_MANIFEST record. It is the BCCP, which the BBIEP contextualizes.',
    `path`                      text CHARACTER SET ascii,
    `hash_path`                 varchar(64) CHARACTER SET ascii NOT NULL COMMENT 'hash_path generated from the path of the component graph using hash function, so that it is unique in the graph.',
    `definition`                text COMMENT 'Definition to override the BCCP''s Definition. If NULLl, it means that the definition should be inherited from the based CC.',
    `remark`                    varchar(225) DEFAULT NULL COMMENT 'This column allows the user to specify very context-specific usage of the BIE. It is different from the Definition column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be "Type of BOM should be recognized in the BOM/typeCode.',
    `biz_term`                  varchar(225) DEFAULT NULL COMMENT 'Business term to indicate what the BIE is called in a particular business context such as in an industry.',
    `created_by`                bigint(20) unsigned             NOT NULL COMMENT 'A foreign key referring to the user who creates the BBIEP. The creator of the BBIEP is also its owner by default. BBIEPs created as children of another ABIE have the same CREATED_BY'',',
    `last_updated_by`           bigint(20) unsigned             NOT NULL COMMENT 'A foreign key referring to the last user who has updated the BBIEP record. ',
    `creation_timestamp`        datetime(6)                     NOT NULL COMMENT 'Timestamp when the BBIEP record was first created. BBIEPs created as children of another ABIE have the same CREATION_TIMESTAMP,',
    `last_update_timestamp`     datetime(6)                     NOT NULL COMMENT 'The timestamp when the BBIEP was last updated.',
    `owner_top_level_asbiep_id` bigint(20) unsigned             NOT NULL COMMENT 'This is a foreign key to the top-level ASBIEP.',
    PRIMARY KEY (`bbiep_id`),
    KEY `bbiep_created_by_fk` (`created_by`),
    KEY `bbiep_last_updated_by_fk` (`last_updated_by`),
    KEY `bbiep_owner_top_level_asbiep_id_fk` (`owner_top_level_asbiep_id`),
    KEY `bbiep_based_bccp_manifest_id_fk` (`based_bccp_manifest_id`),
    KEY `bbiep_path_k` (`path`(3072)),
    KEY `bbiep_hash_path_k` (`hash_path`),
    CONSTRAINT `bbiep_based_bccp_manifest_id_fk` FOREIGN KEY (`based_bccp_manifest_id`) REFERENCES `bccp_manifest` (`bccp_manifest_id`),
    CONSTRAINT `bbiep_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `bbiep_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
    CONSTRAINT `bbiep_owner_top_level_asbiep_id_fk` FOREIGN KEY (`owner_top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='BBIEP represents the usage of basic property in a specific business context. It is a contextualization of a BCCP.';