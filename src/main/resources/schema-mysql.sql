/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table abie
# ------------------------------------------------------------

DROP TABLE IF EXISTS `abie`;

CREATE TABLE `abie` (
  `abie_id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an ABIE.',
  `guid` varchar(41) NOT NULL COMMENT 'A globally unique identifier (GUID) of an ABIE. GUID of an ABIE is different from its based ACC. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `based_acc_id` int(11) unsigned NOT NULL COMMENT 'A foreign key to the ACC table refering to the ACC, on which the business context has been applied to derive this ABIE.',
  `is_top_level` tinyint(1) NOT NULL COMMENT 'Indicate whether the ABIE is a top-level ABIE. If false, it is a descendant of one of the top-level ABIEs. In the context of OAGIS, top-level ABE is used for flagging that an ABIE is a BOD. The condition or conditions for recognizing that an ABIE (or logically, its based ACC) is a top-level is documented in the design document. ',
  `biz_ctx_id` int(11) unsigned NOT NULL COMMENT 'A foreign key to the Business_Context table. This column stores the business context assigned to an ABIE.',
  `definition` text COMMENT 'Definition to override the ACC''s Definition. If Null, it means that the definition should be inherited from the based CC.',
  `created_by` int(11) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the ABIE. The creator of the ABIE is also its owner by default. ABIEs created as children of another ABIE have the same Created_By_User_ID.',
  `last_updated_by` int(11) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the ASBIE record. This may be the user who is in the same group as the creator.',
  `creation_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when the ABIE record was first created. ABIEs created as children of another ABIE have the same Creation_Timestamp.',
  `last_update_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The timestamp when the ABIE is last updated.',
  `state` int(11) DEFAULT NULL COMMENT '2 = Editing, 4 = Published. This column is only used with a top-level ABIE, because that is the only entry point for editing. The state value indicates the visibility of the top-level ABIE to users other than the owner. In the user group environment, a logic can apply that other users in the group can see the top-level ABIE only when it is in the ''Published'' state.',
  `client_id` int(11) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the Client table. The use case associated with this column is to indicate the organizational entity for which the profile BOD is created. For example, Boeing may generate a profile BOD for Boeing civilian or Boeing defense. It is more of the documentation purpose. Only an ABIE which is the top-level ABIE can use this column.',
  `version` varchar(45) DEFAULT NULL COMMENT 'This column hold a version number assigned by the user. This column is only used by the top-level ABIE. No format of version is enforced.',
  `status` varchar(45) DEFAULT NULL COMMENT 'This is different from State which is CRUD life cycle of an entity. The use case for this is to allow the user to indicate the usage status of a top-level ABIE (a profile BOD). An integration architect can use this column. Example values are ‘Prototype’, ‘Test’, and ‘Production’. Only the top-level ABIE can use this field.',
  `remark` varchar(225) DEFAULT NULL COMMENT 'This column allows the user to specify very context-specific usage of the BIE. It is different from the Definition column in that the Definition column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be "Type of BOM should be recognized in the BOM/typeCode."',
  `biz_term` varchar(225) DEFAULT NULL COMMENT 'To indicate what the BIE is called in a particular business context. With this current design, only one business term is allowed per business context.',
  PRIMARY KEY (`abie_id`),
  UNIQUE KEY `abie_uk1` (`guid`),
  KEY `abie_based_acc_id_fk` (`based_acc_id`),
  KEY `abie_biz_ctx_id_fk` (`biz_ctx_id`),
  KEY `abie_created_by_fk` (`created_by`),
  KEY `abie_last_updated_by_fk` (`last_updated_by`),
  KEY `abie_client_id_fk` (`client_id`),
  CONSTRAINT `abie_based_acc_id_fk` FOREIGN KEY (`based_acc_id`) REFERENCES `acc` (`acc_id`),
  CONSTRAINT `abie_biz_ctx_id_fk` FOREIGN KEY (`biz_ctx_id`) REFERENCES `biz_ctx` (`biz_ctx_id`),
  CONSTRAINT `abie_client_id_fk` FOREIGN KEY (`client_id`) REFERENCES `client` (`client_id`),
  CONSTRAINT `abie_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `abie_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='ABIE table stores information about an ABIE, which is a contextualized ACC. The context is represented by the Business_Context_ID column that refers to a business context. Each ABIE must have a business context and a based ACC.\n\nIt should be noted that, per design document, there is no corresponding ABIE created for an ACC which is designated as a "Semantic Group". \n\n';

DROP TABLE IF EXISTS `ABIE_ID_SEQ`;

CREATE TABLE `ABIE_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `ABIE_ID_SEQ` (`next_val`) VALUES (1);




# Dump of table acc
# ------------------------------------------------------------

DROP TABLE IF EXISTS `acc`;

CREATE TABLE `acc` (
  `acc_id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an ACC.',
  `guid` varchar(41) NOT NULL COMMENT 'A globally unique identifier (GUID) of an ACC. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `object_class_term` varchar(100) NOT NULL COMMENT 'Object class name of the ACC concept. For OAGIS, this is generally name of a type with the "Type" truncated from the end. Per CCS the name is space separated.',
  `den` varchar(200) NOT NULL COMMENT 'DEN (dictionary entry name) of the ACC. It can be derived as Object_Class_Term + ". Details".',
  `definition` text COMMENT 'This is a documentation or description of the ACC. Since ACC is business context independent, this is a business context independent description of the ACC concept.',
  `based_acc_id` int(11) unsigned DEFAULT NULL COMMENT 'Based_ACC_ID is a foreign key to the ACC table itself. It represents the ACC that is qualified by this ACC. In general CCS sense, a qualification can by a content extension or restriction, but the current scope supports only extension.\n\nFor history records of an ACC, this column always points to the current record of an ACC.',
  `object_class_qualifier` varchar(100),
  `oagis_component_type` int(11) DEFAULT NULL COMMENT 'The value can be 0 = Base, 1 = Semantics, 2 = Extension, 3 = Semantic Group, 4 = User Extension Group. Generally, Bsae is assigned when the Object_Class_Term contains "Base" at the end. Extension is assigned with the Object_Class_Term contains "Extension" at the end. Semantic Group is assigned when an ACC is imported from an XSD Group. Other cases are assigned Semantics.',
  `module` varchar(100) COMMENT 'This column stores the name of the physical schema module the ACC belongs to. Right now the schema file name is assigned. In the future, this needs to be updated to a file path from the base of the release directory.',
  `namespace_id` int(11) unsigned DEFAULT NULL COMMENT 'Foreign key to the Namespace table. This is the namespace to which the entity belongs. This namespace column is only used in the case the component is a user''s component.',
  `created_by` int(11) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the entity.\n\nThis column never change between the history and the current record. The history record should have the same value as that of its current record.',
  `owner_user_id` int(11) unsigned NOT NULL COMMENT 'This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership. ',
  `last_updated_by` int(11) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who updated the record. \n\nIn the history record, this should always be the user who is editing the entity.',
  `creation_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when the revision of the ACC was created. \n\nThis never change for a revision.',
  `last_update_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.',
  `state` int(11) NOT NULL COMMENT '1 = Editing, 2 = Candidate, 3 = Published. This the revision life cycle state of the ACC.\n\nState change can''t be undone. But the history record can still keep the records of when the state was changed.',
  `revision_num` int(11) NOT NULL DEFAULT '0' COMMENT 'Revision_Number is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).',
  `revision_tracking_num` int(11) NOT NULL DEFAULT '0' COMMENT 'Revision_Tracking_Number supports the ability to undo changes during a revision (life cycle of a revision is from the component''s Editing state to Published state). Once the component has transitioned into the Published state for its particular revision, all revision tracking records are deleted except the latest one. Revision_Tracking_Number can be 0, 1, 2, and so on. The zero value is assign to the record with Revision_Number = 0 as a default.',
  `revision_action` tinyint(11) DEFAULT '1' COMMENT 'This indicates the action associated with the record. The action can be 1 = insert, 2 = update, and 3 = delete. This column is null for the current record.',
  `release_id` int(11) unsigned DEFAULT NULL COMMENT 'Release_ID is an incremental integer. It is an unformatted counter part of the Release_Number in the Release table. Release_ID can be 1, 2, 3, and so on. Release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the Revision_Action column).\n\nNot all component revisions have an associated Release_ID because some revisions may never be released. User Extension Group component type is never part of a release.\n\nUnpublished components cannot be released.\n\nThis column is null for the current record.',
  `current_acc_id` int(11) unsigned DEFAULT NULL COMMENT 'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the the record whose Revision_Number is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.\n\nIt is noted that although this is a foreign key by definition, we don''t specify a foreign key in the data model. This is because when an entity is deleted the current record won''t exist anymore.\n\nThe value of this column for the current record should be left NULL.',
  `is_deprecated` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  `is_abstract` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`acc_id`),
  UNIQUE KEY `acc_uk1` (`guid`),
  KEY `acc_based_acc_id_fk` (`based_acc_id`),
  KEY `acc_namespace_id_fk` (`namespace_id`),
  KEY `acc_created_by_fk` (`created_by`),
  KEY `acc_owner_user_id_fk` (`owner_user_id`),
  KEY `acc_last_updated_by_fk` (`last_updated_by`),
  KEY `acc_release_id_fk` (`release_id`),
  KEY `acc_current_acc_id_fk` (`current_acc_id`),
  CONSTRAINT `acc_based_acc_id_fk` FOREIGN KEY (`based_acc_id`) REFERENCES `acc` (`acc_id`),
  CONSTRAINT `acc_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `acc_current_acc_id_fk` FOREIGN KEY (`current_acc_id`) REFERENCES `acc` (`acc_id`),
  CONSTRAINT `acc_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `acc_namespace_id_fk` FOREIGN KEY (`namespace_id`) REFERENCES `namespace` (`namespace_id`),
  CONSTRAINT `acc_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `acc_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The ACC table hold information about complex data structured concepts. For example, OAGIS''s Components, Nouns, and BODs are captured in the ACC table.\n\nNote that only Extension is supported when deriving ACC from another ACC. (So if there is a restriction needed, maybe that concept should placed higher in the derivation hierarchy rather than lower.)\n\nIn OAGIS, all XSD extensions will be treated as ACC qualification.';

DROP TABLE IF EXISTS `ACC_ID_SEQ`;

CREATE TABLE `ACC_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `ACC_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table agency_id_list
# ------------------------------------------------------------

DROP TABLE IF EXISTS `agency_id_list`;

CREATE TABLE `agency_id_list` (
  `agency_id_list_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `guid` varchar(41) CHARACTER SET utf8 NOT NULL,
  `enum_type_guid` varchar(41) CHARACTER SET utf8 NOT NULL,
  `name` varchar(100) CHARACTER SET utf8,
  `list_id` varchar(10) CHARACTER SET utf8,
  `agency_id` int(11) unsigned DEFAULT NULL,
  `version_id` varchar(10) CHARACTER SET utf8,
  `definition` text CHARACTER SET utf8,
  PRIMARY KEY (`agency_id_list_id`),
  UNIQUE KEY `agency_id_list_uk1` (`guid`),
  UNIQUE KEY `agency_id_list_uk2` (`enum_type_guid`),
  KEY `agency_id_list_agency_id_fk` (`agency_id`),
  CONSTRAINT `agency_id_list_agency_id_fk` FOREIGN KEY (`agency_id`) REFERENCES `agency_id_list_value` (`agency_id_list_value_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `AGENCY_ID_LIST_ID_SEQ`;

CREATE TABLE `AGENCY_ID_LIST_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `AGENCY_ID_LIST_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table agency_id_list_value
# ------------------------------------------------------------

DROP TABLE IF EXISTS `agency_id_list_value`;

CREATE TABLE `agency_id_list_value` (
  `agency_id_list_value_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `value` varchar(150) NOT NULL,
  `name` varchar(150),
  `definition` text,
  `owner_list_id` int(11) unsigned NOT NULL,
  PRIMARY KEY (`agency_id_list_value_id`),
  KEY `ailv_owner_list_id_fk` (`owner_list_id`),
  CONSTRAINT `ailv_owner_list_id_fk` FOREIGN KEY (`owner_list_id`) REFERENCES `agency_id_list` (`agency_id_list_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `AGENCY_ID_LIST_VALUE_ID_SEQ`;

CREATE TABLE `AGENCY_ID_LIST_VALUE_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `AGENCY_ID_LIST_VALUE_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table app_user
# ------------------------------------------------------------

DROP TABLE IF EXISTS `app_user`;

CREATE TABLE `app_user` (
  `app_user_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `login_id` varchar(45) NOT NULL,
  `password` varchar(45) NOT NULL,
  `name` varchar(100),
  `organization` varchar(100),
  `oagis_developer_indicator` tinyint(1) NOT NULL COMMENT 'This indicates whether the user can edit OAGIS Model content. Content created by the OAGIS developer is also considered OAGIS Model content.',
  PRIMARY KEY (`app_user_id`),
  UNIQUE KEY `app_user_uk1` (`login_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `APP_USER_ID_SEQ`;

CREATE TABLE `APP_USER_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `APP_USER_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table asbie
# ------------------------------------------------------------

DROP TABLE IF EXISTS `asbie`;

CREATE TABLE `asbie` (
  `asbie_id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an ASBIE.',
  `guid` varchar(41) NOT NULL COMMENT 'A globally unique identifier (GUID) of an ASBIE. GUID of an ASBIE is different from its based ASCC. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `from_abie_id` int(11) unsigned NOT NULL COMMENT 'Assoc_From_ABIE_ID is a foreign key pointing to the ABIE table. Assoc_From_ABIE_ID is basically  a parent data element (type) of the Assoc_To_ASBIEP_ID. Assoc_From_ABIE_ID must be based on the Assoc_From_ACC_ID in the Based_ASCC except when the Assoc_From_ACC_ID refers to an ACC Semantic Group.',
  `to_asbiep_id` int(11) unsigned NOT NULL COMMENT 'Assoc_To_ASBIEP_ID is a foreign key to the ASBIEP table. Assoc_To_ASBIEP_ID is basically a child data element of the Assoc_From_ABIE_ID. Assoc_To_ASBIEP_ID must be based on the Role_of_ACC_ID in the Based_ASCC.',
  `based_ascc` int(11) unsigned NOT NULL COMMENT 'The Based_ASCC column refers to the ASCC record, which this ASBIE contextualizes.',
  `definition` text CHARACTER SET utf8 COMMENT 'Definition to override the ASCC definition. If Null, it means that the definition should be derived from the based CC on the UI, expression generation, and any API.',
  `cardinality_min` int(11) NOT NULL COMMENT 'Minimum cardinality of the Assoc_To_ASBIEP_ID. The valid values are non-negative integer.',
  `cardinality_max` int(11) NOT NULL COMMENT 'Maximum cardinality of the Assoc_To_ASBIEP_ID. The valid values are integer -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.',
  `is_nillable` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicate whether the Assoc_To_ASBIEP is nillable.',
  `remark` varchar(225) DEFAULT NULL COMMENT 'This column allows the user to specify very context-specific usage of the BIE. It is different from the Definition column in that the Definition column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be "Type of BOM should be recognized in the BOM/typeCode."',
  `created_by` int(11) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the ASBIE. The creator of the ASBIE is also its owner by default. ASBIEs created as children of another ABIE have the same Created_By_User_ID.',
  `last_updated_by` int(11) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the ASBIE record. This may be the user who is in the same group as the creator.',
  `creation_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when the ASBIE record was first created. ASBIEs created as children of another ABIE have the same Creation_Timestamp.',
  `last_update_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The timestamp when the ASBIE is last updated.',
  `seq_key` int(11) unsigned NOT NULL COMMENT 'This indicates the order of the associations among other siblings. The Sequencing_Key for BIEs is decimal in order to accomodate the removal of inheritance hierarchy and group. For example, children of the most abstract ACC will have Sequencing_Key = 1.1, 1.2, 1.3, and so on; and Sequencing_Key of the next abstraction level ACC will have Sequencing_Key = 2.1, 2.2, 2.3 and so on so forth.',
  `is_used` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Flag to indicate whether the field/component is used in the content model. It indicates whether the field/component should be generated.',
  PRIMARY KEY (`asbie_id`),
  UNIQUE KEY `asbie_uk1` (`guid`, `from_abie_id`, `to_asbiep_id`),
  KEY `asbie_from_abie_id` (`from_abie_id`),
  KEY `asbie_to_asbiep_id_fk` (`to_asbiep_id`),
  KEY `asbie_based_ascc_id_fk` (`based_ascc`),
  KEY `asbie_created_by_fk` (`created_by`),
  KEY `asbie_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `asbie_based_ascc_id_fk` FOREIGN KEY (`based_ascc`) REFERENCES `ascc` (`ascc_id`),
  CONSTRAINT `asbie_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asbie_from_abie_id` FOREIGN KEY (`from_abie_id`) REFERENCES `abie` (`abie_id`),
  CONSTRAINT `asbie_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asbie_to_asbiep_id_fk` FOREIGN KEY (`to_asbiep_id`) REFERENCES `asbiep` (`asbiep_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='An ASBIE represents a relationship/association between two ABIEs through an ASBIEP. It is contextualization of an ASCC.';

DROP TABLE IF EXISTS `ASBIE_ID_SEQ`;

CREATE TABLE `ASBIE_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `ASBIE_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table asbiep
# ------------------------------------------------------------

DROP TABLE IF EXISTS `asbiep`;

CREATE TABLE `asbiep` (
  `asbiep_id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an ASBIEP.',
  `guid` varchar(41) NOT NULL COMMENT 'A globally unique identifier (GUID) of an ASBIEP. GUID of an ASBIEP is different from its based ASCCP. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `based_asccp_id` int(11) unsigned NOT NULL COMMENT 'A foreign key point to the ASCCP record. It is the ASCCP which the ASBIEP contextualizes.',
  `role_of_abie_id` int(11) unsigned NOT NULL COMMENT 'A foreign key pointing to the ABIE record. It is the ABIE which the property term in the based ASCCP qualifies. Note that the ABIE has to be derived from the ACC used by the based ASCCP.',
  `definition` text COMMENT 'Definition to override the ASCCP''s Definition. If Null, it means that the definition should be derived from the based CC on the UI, expression generation, and any API.',
  `remark` varchar(225) DEFAULT NULL COMMENT 'This column allows the user to specify very context-specific usage of the BIE. It is different from the Definition column in that the Definition column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be "Type of BOM should be recognized in the BOM/typeCode."',
  `biz_term` varchar(225) DEFAULT NULL COMMENT 'To indicate what the BIE is called in a particular business context. With this current design, only one business term is allowed per business context.',
  `created_by` int(11) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the ASBIEP. The creator of the ASBIEP is also its owner by default. ASBIEPs created as children of another ABIE have the same Created_By_User_ID.',
  `last_updated_by` int(11) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the ASBIEP record. This may be the user who is in the same group as the creator.',
  `creation_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when the ASBIEP record was first created. ASBIEPs created as children of another ABIE have the same Creation_Timestamp.',
  `last_update_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The timestamp when the ASBIEP is last updated.',
  PRIMARY KEY (`asbiep_id`),
  UNIQUE KEY `asbiep_uk1` (`guid`),
  KEY `asbiep_based_asccp_id` (`based_asccp_id`),
  KEY `asbiep_role_of_abie_id` (`role_of_abie_id`),
  KEY `asbiep_created_by_fk` (`created_by`),
  KEY `asbiep_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `asbiep_based_asccp_id` FOREIGN KEY (`based_asccp_id`) REFERENCES `asccp` (`asccp_id`),
  CONSTRAINT `asbiep_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asbiep_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asbiep_role_of_abie_id` FOREIGN KEY (`role_of_abie_id`) REFERENCES `abie` (`abie_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='ASBIEP represents a role in a usage of an ABIE. It is a contextualization of an ASCCP.';

DROP TABLE IF EXISTS `ASBIEP_ID_SEQ`;

CREATE TABLE `ASBIEP_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `ASBIEP_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table ascc
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ascc`;

CREATE TABLE `ascc` (
  `ascc_id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an ASCC.',
  `guid` varchar(41) NOT NULL COMMENT 'A globally unique identifier (GUID) of an ASCC. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `cardinality_min` int(11) NOT NULL COMMENT 'Minimum cardinality of the Assoc_To_ASCCP_ID. The valid values are non-negative integer.',
  `cardinality_max` int(11) NOT NULL COMMENT 'Maximum cardinality of the Assoc_To_ASCCP_ID. The valid values are integer -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.',
  `seq_key` int(11) unsigned NOT NULL COMMENT 'This indicates the order of the associations among other siblings. The valid values are positive integer. The Sequencing_Key at the CC side is localized. In other words, if an ACC is based on another ACC, Sequencing_Key of ASCCs or BCCs of the former ACC starts at 1 again. ',
  `from_acc_id` int(11) unsigned NOT NULL COMMENT 'Assoc_From_ACC_ID is a foreign key pointing to an ACC record. It is basically pointing to a parent data element (type) of the Assoc_To_ASCCP_ID. \n\nNote that for the ASCC history records, this column always points to the ACC_ID of the current record of an ACC.',
  `to_asccp_id` int(11) unsigned NOT NULL COMMENT 'Assoc_To_ASCCP_ID is a foreign key to an ASCCP table record. It is basically pointing to a child data element of the Assoc_From_ACC_ID. \n\nNote that for the ASCC history records, this column always points to the ASCCP_ID of the current record of an ASCCP.',
  `den` varchar(200) NOT NULL COMMENT 'DEN (dictionary entry name) of the ASCC. This column can be derived from Object_Class_Term of the Assoc_From_ACC_ID and DEN of the Assoc_To_ASCCP_ID as Object_Class_Term + ". " + DEN. ',
  `definition` text COMMENT 'This is a documentation or description of the ASCC. Since ASCC is business context independent, this is a business context independent description of the ASCC. Since there are Definitions also in the ASCCP (as referenced by Assoc_To_ASCCP_ID column) and the ACC under that ASCCP, Definition in the ASCC is a specific description about the relationship between the ACC (as in Assoc_From_ACC_ID) and the ASCCP.',
  `is_deprecated` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  `created_by` int(11) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the entity.\n\nThis column never change between the history and the current record. The history record should have the same value as that of its current record.',
  `owner_user_id` int(11) unsigned NOT NULL COMMENT 'This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership. ',
  `last_updated_by` int(11) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the record. \n\nIn the history record, this should always be the user who is editing the entity.',
  `creation_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when the revision of the ASCC was created. \n\nThis never change for a revision.',
  `last_update_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.',
  `state` int(11) NOT NULL COMMENT '1 = Editing, 2 = Candidate, 3 = Published. This the revision life cycle state of the entity.\n\nState change can''t be undone. But the history record can still keep the records of when the state was changed.',
  `revision_num` int(11) NOT NULL DEFAULT '0' COMMENT 'Revision_Number is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).',
  `revision_tracking_num` int(11) NOT NULL DEFAULT '0' COMMENT 'Revision_Tracking_Number supports the ability to undo changes during a revision (life cycle of a revision is from the component''s Editing state to Published state). Once the component has transitioned into the Published state for its particular revision, all revision tracking records are deleted except the latest one. Revision_Tracking_Number can be 0, 1, 2, and so on. The zero value is assign to the record with Revision_Number = 0 as a default.',
  `revision_action` tinyint(11) DEFAULT '1' COMMENT 'This indicates the action associated with the record. The action can be 1 = insert, 2 = update, and 3 = delete. This column is null for the current record.',
  `release_id` int(11) unsigned DEFAULT NULL COMMENT 'Release_ID is an incremental integer. It is an unformatted counter part of the Release_Number in the Release table. Release_ID can be 1, 2, 3, and so on. Release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the Revision_Action column).\n\nNot all component revisions have an associated Release_ID because some revisions may never be released.\n\nUnpublished components cannot be released.\n\nThis column is null for the current record.',
  `current_ascc_id` int(11) unsigned DEFAULT NULL COMMENT 'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the the record whose Revision_Number is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.\n\nIt is noted that although this is a foreign key by definition, we don''t specify a foreign key in the data model. This is because when an entity is deleted the current record won''t exist anymore.\n\nThe value of this column for the current record should be left NULL.',
  PRIMARY KEY (`ascc_id`),
  UNIQUE KEY `ascc_uk1` (`guid`, `from_acc_id`, `to_asccp_id`),
  KEY `ascc_from_acc_id_fk` (`from_acc_id`),
  KEY `ascc_to_asccp_id_fk` (`to_asccp_id`),
  KEY `ascc_created_by_fk` (`created_by`),
  KEY `ascc_owner_user_id_fk` (`owner_user_id`),
  KEY `ascc_last_updated_by_fk` (`last_updated_by`),
  KEY `ascc_release_id_fk` (`release_id`),
  KEY `ascc_current_ascc_id_fk` (`current_ascc_id`),
  CONSTRAINT `ascc_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `ascc_current_ascc_id_fk` FOREIGN KEY (`current_ascc_id`) REFERENCES `ascc` (`ascc_id`),
  CONSTRAINT `ascc_from_acc_id_fk` FOREIGN KEY (`from_acc_id`) REFERENCES `acc` (`acc_id`),
  CONSTRAINT `ascc_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `ascc_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `ascc_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
  CONSTRAINT `ascc_to_asccp_id_fk` FOREIGN KEY (`to_asccp_id`) REFERENCES `asccp` (`asccp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='An ASCC represents a relationship/association between two ACCs through an ASCCP.';

DROP TABLE IF EXISTS `ASCC_ID_SEQ`;

CREATE TABLE `ASCC_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `ASCC_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table asccp
# ------------------------------------------------------------

DROP TABLE IF EXISTS `asccp`;

CREATE TABLE `asccp` (
  `asccp_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `guid` varchar(41) NOT NULL,
  `property_term` varchar(60) COMMENT 'There must be only one ASCCP without a Property_Term for a particular ACC.',
  `definition` text COMMENT 'Generally Definition should not be empty but it is not forcing here. A warning should be given at the application level.',
  `role_of_acc_id` int(11) unsigned DEFAULT NULL COMMENT 'The ACC from which this ASCCP is created (ASCCP applies role to the ACC).',
  `den` varchar(200),
  `created_by` int(11) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the entity. \n\nThis column never change between the history and the current record. The history record should have the same value as that of its current record.',
  `owner_user_id` int(11) unsigned NOT NULL,
  `last_updated_by` int(11) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the record. \n\nIn the history record, this should always be the user who is editing the entity.',
  `creation_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when the revision of the ASCCP was created. \n\nThis never change for a revision.',
  `last_update_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.',
  `state` int(11) NOT NULL COMMENT '1 = Editing, 2 = Candidate, 3 = Published. This the revision life cycle state of the ACC.\n\nState change can''t be undone. But the history record can still keep the records of when the state was changed.',
  `module` text COMMENT 'This column stores the name of the physical schema module the ASCCP belongs to. Right now the schema file name is assigned. In the future, this needs to be updated to a file path from the base of the release directory.',
  `namespace_id` int(11) unsigned DEFAULT NULL COMMENT 'Foreign key to the Namespace table. This is the namespace, to which the entity belongs.',
  `reusable_indicator` tinyint(1) DEFAULT '1',
  `is_deprecated` tinyint(1) NOT NULL COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  `revision_num` int(11) NOT NULL DEFAULT '0' COMMENT 'Revision_Number is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).',
  `revision_tracking_num` int(11) NOT NULL DEFAULT '0' COMMENT 'Revision_Tracking_Number supports the ability to undo changes during a revision (life cycle of a revision is from the component''s Editing state to Published state). Once the component has transitioned into the Published state for its particular revision, all revision tracking records are deleted except the latest one. Revision_Tracking_Number can be 0, 1, 2, and so on. The zero value is assign to the record with Revision_Number = 0 as a default.',
  `revision_action` tinyint(11) DEFAULT '1' COMMENT 'This indicates the action associated with the record. The action can be 1 = insert, 2 = update, and 3 = delete. This column is null for the current record.',
  `release_id` int(11) unsigned DEFAULT NULL COMMENT 'Release_ID is an incremental integer. It is an unformatted counter part of the Release_Number in the Release table. Release_ID can be 1, 2, 3, and so on. Release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the Revision_Action column).\n\nNot all component revisions have an associated Release_ID because some revisions may never be released.\n\nUnpublished components cannot be released.\n\nThis column is null for the current record.',
  `current_asccp_id` int(11) unsigned DEFAULT NULL COMMENT 'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the the record whose Revision_Number is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.\n\nIt is noted that although this is a foreign key by definition, we don''t specify a foreign key in the data model. This is because when an entity is deleted the current record won''t exist anymore.\n\nThe value of this column for the current record should be left NULL.',
  PRIMARY KEY (`asccp_id`),
  UNIQUE KEY `asccp_uk1` (`guid`),
  KEY `asccp_role_of_acc` (`role_of_acc_id`),
  KEY `asccp_created_by_fk` (`created_by`),
  KEY `asccp_owner_user_id_fk` (`owner_user_id`),
  KEY `asccp_last_updated_by_fk` (`last_updated_by`),
  KEY `asccp_namespace_id_fk` (`namespace_id`),
  KEY `asccp_released_id_fk` (`release_id`),
  KEY `asccp_current_asccp_id_fk` (`current_asccp_id`),
  CONSTRAINT `asccp_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asccp_current_asccp_id_fk` FOREIGN KEY (`current_asccp_id`) REFERENCES `asccp` (`asccp_id`),
  CONSTRAINT `asccp_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asccp_namespace_id_fk` FOREIGN KEY (`namespace_id`) REFERENCES `namespace` (`namespace_id`),
  CONSTRAINT `asccp_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asccp_released_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
  CONSTRAINT `asccp_role_of_acc` FOREIGN KEY (`role_of_acc_id`) REFERENCES `acc` (`acc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `ASCCP_ID_SEQ`;

CREATE TABLE `ASCCP_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `ASCCP_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table bbie
# ------------------------------------------------------------

DROP TABLE IF EXISTS `bbie`;

CREATE TABLE `bbie` (
  `bbie_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `guid` varchar(41) NOT NULL,
  `based_bcc_id` int(11) unsigned NOT NULL,
  `from_abie_id` int(11) unsigned NOT NULL COMMENT 'Assoc_From_ABIE must be based on the Assoc_From_ACC in the Based_BCC.',
  `to_bbiep_id` int(11) unsigned NOT NULL,
  `bdt_pri_restri_id` int(11) unsigned DEFAULT NULL,
  `code_list_id` int(11) unsigned DEFAULT NULL,
  `cardinality_min` int(11) NOT NULL,
  `cardinality_max` int(11) DEFAULT NULL COMMENT 'Unspecified = unbounded',
  `default_value` text COMMENT 'Default and fixed value cannot be used at the same time.',
  `is_nillable` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicate whether the field can have a null value.',
  `fixed_value` text CHARACTER SET utf8,
  `is_null` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This column indicates whether the field is fixed to Null. isNull can be true only if the isNillable is true. If isNull is true then the Fixed_Value column cannot have a value.',
  `definition` text CHARACTER SET utf8 COMMENT 'Definition to override the BCC definition. If Null, it means that the definition should be inherited from the based CC.',
  `remark` varchar(225) DEFAULT NULL COMMENT 'This column allows the user to codify context specific usage of the BIE. It is different from the Definition column in that the Definition column is a descriptive text while this one is machine understandable. So the data type of this column is more like code.',
  `created_by` int(11) unsigned NOT NULL,
  `last_updated_by` int(11) unsigned NOT NULL,
  `creation_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_update_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `seq_key` int(11) unsigned NOT NULL,
  `is_used` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Flag to indicate whether the field/component is used in the content model. It indicates whether the field/component should be generated.',
  PRIMARY KEY (`bbie_id`),
  UNIQUE KEY `bbie_uk1` (`guid`, `from_abie_id`, `to_bbiep_id`),
  KEY `bbie_based_bcc_id_fk` (`based_bcc_id`),
  KEY `bbie_from_abie_id_fk` (`from_abie_id`),
  KEY `bbie_to_bbiep_id_fk` (`to_bbiep_id`),
  KEY `bbie_bdt_pri_restri_id_fk` (`bdt_pri_restri_id`),
  KEY `bbie_code_list_id_fk` (`code_list_id`),
  KEY `bbie_created_by_fk` (`created_by`),
  KEY `bbie_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `bbie_based_bcc_id_fk` FOREIGN KEY (`based_bcc_id`) REFERENCES `bcc` (`bcc_id`),
  CONSTRAINT `bbie_bdt_pri_restri_id_fk` FOREIGN KEY (`bdt_pri_restri_id`) REFERENCES `bdt_pri_restri` (`bdt_pri_restri_id`),
  CONSTRAINT `bbie_code_list_id_fk` FOREIGN KEY (`code_list_id`) REFERENCES `code_list` (`code_list_id`),
  CONSTRAINT `bbie_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bbie_from_abie_id_fk` FOREIGN KEY (`from_abie_id`) REFERENCES `abie` (`abie_id`),
  CONSTRAINT `bbie_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bbie_to_bbiep_id_fk` FOREIGN KEY (`to_bbiep_id`) REFERENCES `bbiep` (`bbiep_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `BBIE_ID_SEQ`;

CREATE TABLE `BBIE_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `BBIE_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table bbie_sc
# ------------------------------------------------------------

DROP TABLE IF EXISTS `bbie_sc`;

CREATE TABLE `bbie_sc` (
  `bbie_sc_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `bbie_id` int(11) unsigned NOT NULL,
  `dt_sc_id` int(11) unsigned NOT NULL COMMENT 'This should correspond to the DT_SC of the BDT of the based BCC and BCCP.',
  `dt_sc_pri_restri_id` int(11) unsigned DEFAULT NULL COMMENT 'This must be one of the allowed primitive/code list as specified in the corresponding SC of the based BCC of the BBIE.',
  `code_list_id` int(11) unsigned DEFAULT NULL,
  `agency_id_list_id` int(11) unsigned DEFAULT NULL,
  `min_cardinality` int(11) NOT NULL,
  `max_cardinality` int(11) DEFAULT NULL,
  `default_value` text,
  `fixed_value` text,
  `definition` text,
  `remark` varchar(225) DEFAULT NULL,
  `biz_term` varchar(225) DEFAULT NULL,
  `is_used` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Flag to indicate whether the field/component is used in the content model. It indicates whether the field/component should be generated.',
  PRIMARY KEY (`bbie_sc_id`),
  KEY `bbie_bbie_id_fk` (`bbie_id`),
  KEY `bbie_sc_dt_sc_id_fk` (`dt_sc_id`),
  KEY `bbie_sc_dt_sc_pri_restri_id_fk` (`dt_sc_pri_restri_id`),
  KEY `bbie_sc_code_list_id_fk` (`code_list_id`),
  KEY `bbie_sc_agency_id_list_id_fk` (`agency_id_list_id`),
  CONSTRAINT `bbie_bbie_id_fk` FOREIGN KEY (`bbie_id`) REFERENCES `bbie` (`bbie_id`),
  CONSTRAINT `bbie_sc_agency_id_list_id_fk` FOREIGN KEY (`agency_id_list_id`) REFERENCES `agency_id_list` (`agency_id_list_id`),
  CONSTRAINT `bbie_sc_code_list_id_fk` FOREIGN KEY (`code_list_id`) REFERENCES `code_list` (`code_list_id`),
  CONSTRAINT `bbie_sc_dt_sc_id_fk` FOREIGN KEY (`dt_sc_id`) REFERENCES `dt_sc` (`dt_sc_id`),
  CONSTRAINT `bbie_sc_dt_sc_pri_restri_id_fk` FOREIGN KEY (`dt_sc_pri_restri_id`) REFERENCES `bdt_sc_pri_restri` (`bdt_sc_pri_restri_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `BBIE_SC_ID_SEQ`;

CREATE TABLE `BBIE_SC_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `BBIE_SC_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table bbiep
# ------------------------------------------------------------

DROP TABLE IF EXISTS `bbiep`;

CREATE TABLE `bbiep` (
  `bbiep_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `guid` varchar(41) NOT NULL,
  `based_bccp_id` int(11) unsigned NOT NULL,
  `definition` text COMMENT 'Definition to override the BCCP''s Definition. If Null, it means that the definition should be inherited from the based CC.',
  `remark` varchar(225) DEFAULT NULL COMMENT 'This column allows the user to codify context specific usage of the BIE. It is different from the Definition column in that the Definition column is a descriptive text while this one is machine understandable. So the data type of this column is more like code.',
  `biz_term` varchar(225) DEFAULT NULL COMMENT 'To indicate what the BIE is called in a particular industry (a particular context in general). ',
  `created_by` int(11) unsigned NOT NULL,
  `last_updated_by` int(11) unsigned NOT NULL,
  `creation_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_update_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`bbiep_id`),
  UNIQUE KEY `bbiep_uk1` (`guid`),
  KEY `bbiep_based_bccp_id_fk` (`based_bccp_id`),
  KEY `bbiep_created_by_fk` (`created_by`),
  KEY `bbiep_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `bbiep_based_bccp_id_fk` FOREIGN KEY (`based_bccp_id`) REFERENCES `bccp` (`bccp_id`),
  CONSTRAINT `bbiep_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bbiep_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `BBIEP_ID_SEQ`;

CREATE TABLE `BBIEP_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `BBIEP_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table bcc
# ------------------------------------------------------------

DROP TABLE IF EXISTS `bcc`;

CREATE TABLE `bcc` (
  `bcc_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `guid` varchar(41) DEFAULT NULL,
  `cardinality_min` int(11) NOT NULL,
  `cardinality_max` int(11) DEFAULT NULL COMMENT '-1 means unbounded.',
  `to_bccp_id` int(11) unsigned NOT NULL,
  `from_acc_id` int(11) unsigned NOT NULL,
  `seq_key` int(11) unsigned DEFAULT NULL COMMENT 'This indicates the order of the associations among other siblings. The valid values are positive integer. The Sequencing_Key at the CC side is localized. In other words, if an ACC is based on another ACC, Sequencing_Key of ASCCs or BCCs of the former ACC starts at 1 again. The Sequencing_Key in the case of Entity_Type is attribute is always zero.',
  `entity_type` int(11) DEFAULT NULL COMMENT 'This is a code list: 0 = attribute and 1 = element. An expression generator may or may not use this information. This column is necessary because some of the BCCs are xsd:attribute and some are xsd:element in the legacy OAGIS. ',
  `den` varchar(200) NOT NULL,
  `definition` text,
  `created_by` int(11) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the entity.\n\nThis column never change between the history and the current record. The history record should have the same value as that of its current record.',
  `owner_user_id` int(11) unsigned NOT NULL,
  `last_updated_by` int(11) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the record. \n\nIn the history record, this should always be the user who is editing the entity.',
  `creation_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when the revision of the BCC was created. \n\nThis never change for a revision.',
  `last_update_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.',
  `state` int(11) NOT NULL COMMENT '1 = Editing, 2 = Candidate, 3 = Published. This the revision life cycle state of the ACC.\n\nState change can''t be undone. But the history record can still keep the records of when the state was changed.',
  `revision_num` int(11) NOT NULL DEFAULT '0' COMMENT 'Revision_Number is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).',
  `revision_tracking_num` int(11) NOT NULL DEFAULT '0' COMMENT 'Revision_Tracking_Number supports the ability to undo changes during a revision (life cycle of a revision is from the component''s Editing state to Published state). Once the component has transitioned into the Published state for its particular revision, all revision tracking records are deleted except the latest one. Revision_Tracking_Number can be 0, 1, 2, and so on. The zero value is assign to the record with Revision_Number = 0 as a default.',
  `revision_action` tinyint(1) DEFAULT '1' COMMENT 'This indicates the action associated with the record. The action can be 1 = insert, 2 = update, and 3 = delete. This column is null for the current record.',
  `release_id` int(11) unsigned DEFAULT NULL COMMENT 'Release_ID is an incremental integer. It is an unformatted counter part of the Release_Number in the Release table. Release_ID can be 1, 2, 3, and so on. Release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the Revision_Action column).\n\nNot all component revisions have an associated Release_ID because some revisions may never be released.\n\nUnpublished components cannot be released.\n\nThis column is null for the current record.',
  `current_bcc_id` int(11) unsigned DEFAULT NULL COMMENT 'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the the record whose Revision_Number is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.\n\nIt is noted that although this is a foreign key by definition, we don''t specify a foreign key in the data model. This is because when an entity is deleted the current record won''t exist anymore.\n\nThe value of this column for the current record should be left NULL.',
  `is_deprecated` tinyint(1) NOT NULL COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  PRIMARY KEY (`bcc_id`),
  UNIQUE KEY `bcc_uk1` (`guid`, `from_acc_id`, `to_bccp_id`),
  KEY `bcc_to_bccp_id_fk` (`to_bccp_id`),
  KEY `bcc_from_acc_id_fk` (`from_acc_id`),
  KEY `bcc_created_by_fk` (`created_by`),
  KEY `bcc_owner_user_id_fk` (`owner_user_id`),
  KEY `bcc_last_updated_by_fk` (`last_updated_by`),
  KEY `bcc_release_id_fk` (`release_id`),
  KEY `bcc_current_bcc_id_fk` (`current_bcc_id`),
  CONSTRAINT `bcc_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bcc_current_bcc_id_fk` FOREIGN KEY (`current_bcc_id`) REFERENCES `bcc` (`bcc_id`),
  CONSTRAINT `bcc_from_acc_id_fk` FOREIGN KEY (`from_acc_id`) REFERENCES `acc` (`acc_id`),
  CONSTRAINT `bcc_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bcc_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bcc_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
  CONSTRAINT `bcc_to_bccp_id_fk` FOREIGN KEY (`to_bccp_id`) REFERENCES `bccp` (`bccp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `BCC_ID_SEQ`;

CREATE TABLE `BCC_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `BCC_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table bccp
# ------------------------------------------------------------

DROP TABLE IF EXISTS `bccp`;

CREATE TABLE `bccp` (
  `bccp_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `guid` varchar(41) NOT NULL,
  `property_term` varchar(60) NOT NULL,
  `representation_term` varchar(20) NOT NULL COMMENT 'Note 1: BCCP''s Representation Term should be derived from its BDT as BDT''s Data_Type_Qualifier + CDT''s Data_Type_Term.',
  `bdt_id` int(11) unsigned NOT NULL COMMENT 'Only DT_ID which DT_Type is BDT can be used.',
  `den` varchar(200) NOT NULL,
  `definition` text,
  `module` text COMMENT 'This column stores the name of the physical schema module the ASCCP belongs to. Right now the schema file name is assigned. In the future, this needs to be updated to a file path from the base of the release directory.',
  `namespace_id` int(11) unsigned DEFAULT NULL COMMENT 'Foreign key to the Namespace table. This is the namespace, to which the entity belongs.',
  `is_deprecated` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  `created_by` int(11) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the entity.\n\nThis column never change between the history and the current record. The history record should have the same value as that of its current record.',
  `owner_user_id` int(11) unsigned NOT NULL,
  `last_updated_by` int(11) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the record. \n\nIn the history record, this should always be the user who is editing the entity.',
  `creation_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when the revision of the BCCP was created. \n\nThis never changefor a revision.',
  `last_update_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.',
  `state` int(11) NOT NULL COMMENT '1 = Editing, 2 = Candidate, 3 = Published. This the revision life cycle state of the ACC.\n\nState change can''t be undone. But the history record can still keep the records of when the state was changed.',
  `revision_num` int(11) NOT NULL DEFAULT '0' COMMENT 'Revision_Number is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).',
  `revision_tracking_num` int(11) NOT NULL DEFAULT '0' COMMENT 'Revision_Tracking_Number supports the ability to undo changes during a revision (life cycle of a revision is from the component''s Editing state to Published state). Once the component has transitioned into the Published state for its particular revision, all revision tracking records are deleted except the latest one. Revision_Tracking_Number can be 0, 1, 2, and so on. The zero value is assign to the record with Revision_Number = 0 as a default.',
  `revision_action` int(11) DEFAULT '1' COMMENT 'This indicates the action associated with the record. The action can be 1 = insert, 2 = update, and 3 = delete. This column is null for the current record.',
  `release_id` int(11) unsigned DEFAULT NULL COMMENT 'Release_ID is an incremental integer. It is an unformatted counter part of the Release_Number in the Release table. Release_ID can be 1, 2, 3, and so on. Release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the Revision_Action column).\n\nNot all component revisions have an associated Release_ID because some revisions may never be released.\n\nUnpublished components cannot be released.\n\nThis column is null for the current record.',
  `current_bccp_id` int(11) unsigned DEFAULT NULL COMMENT 'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the the record whose Revision_Number is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.\n\nIt is noted that although this is a foreign key by definition, we don''t specify a foreign key in the data model. This is because when an entity is deleted the current record won''t exist anymore.\n\nThe value of this column for the current record should be left NULL.',
  PRIMARY KEY (`bccp_id`),
  UNIQUE KEY `bccp_sc_uk1` (`guid`),
  UNIQUE KEY `bccp_sc_uk2` (`property_term`, `bdt_id`),
  KEY `bccp_bdt_id_fk` (`bdt_id`),
  KEY `bccp_namespace_id_fk` (`namespace_id`),
  KEY `bccp_created_by_fk` (`created_by`),
  KEY `bccp_owner_user_id_fk` (`owner_user_id`),
  KEY `bccp_last_updated_by_fk` (`last_updated_by`),
  KEY `bccp_release_id_fk` (`release_id`),
  KEY `bccp_current_bccp_id_fk` (`current_bccp_id`),
  CONSTRAINT `bccp_bdt_id_fk` FOREIGN KEY (`bdt_id`) REFERENCES `dt` (`dt_id`),
  CONSTRAINT `bccp_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bccp_current_bccp_id_fk` FOREIGN KEY (`current_bccp_id`) REFERENCES `bccp` (`bccp_id`),
  CONSTRAINT `bccp_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bccp_namespace_id_fk` FOREIGN KEY (`namespace_id`) REFERENCES `namespace` (`namespace_id`),
  CONSTRAINT `bccp_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bccp_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `BCCP_ID_SEQ`;

CREATE TABLE `BCCP_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `BCCP_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table bdt_pri_restri
# ------------------------------------------------------------

DROP TABLE IF EXISTS `bdt_pri_restri`;

CREATE TABLE `bdt_pri_restri` (
  `bdt_pri_restri_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `bdt_id` int(11) unsigned NOT NULL,
  `cdt_awd_pri_xps_type_map_id` int(11) unsigned DEFAULT NULL COMMENT 'Both CDT_Primitive_Expression_Type_Map_ID and Code_List_ID cannot be blank at the same time.',
  `code_list_id` int(11) unsigned DEFAULT NULL,
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This allow overriding the default in the CDT_Allowed_Primitive_Expression_Type_Map table. This field is used when generating an expression of the OAGIS model. In OAGIS 10, a bunch of BDTs are defined for a CDT, but OAGIS fields is bound to only one of the BDTs.',
  `agency_id_list_id` int(11) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the agency_id_list table. It is used in the case that the BDT content can be restricted to an agency ID list.',
  PRIMARY KEY (`bdt_pri_restri_id`),
  KEY `bpr_bdt_id_fk` (`bdt_id`),
  KEY `bpr_capxtm_id_fk` (`cdt_awd_pri_xps_type_map_id`),
  KEY `bpr_code_list_id_fk` (`code_list_id`),
  KEY `bpr_agency_id_list_id_fk` (`agency_id_list_id`),
  CONSTRAINT `bpr_agency_id_list_id_fk` FOREIGN KEY (`agency_id_list_id`) REFERENCES `agency_id_list` (`agency_id_list_id`),
  CONSTRAINT `bpr_bdt_id_fk` FOREIGN KEY (`bdt_id`) REFERENCES `dt` (`dt_id`),
  CONSTRAINT `bpr_capxtm_id_fk` FOREIGN KEY (`cdt_awd_pri_xps_type_map_id`) REFERENCES `cdt_awd_pri_xps_type_map` (`cdt_awd_pri_xps_type_map_id`),
  CONSTRAINT `bpr_code_list_id_fk` FOREIGN KEY (`code_list_id`) REFERENCES `code_list` (`code_list_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Business rules will ensure that the primitives for BDT are only subset of the CDT or BDT on which it is based.';

DROP TABLE IF EXISTS `BDT_PRI_RESTRI_ID_SEQ`;

CREATE TABLE `BDT_PRI_RESTRI_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `BDT_PRI_RESTRI_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table bdt_sc_pri_restri
# ------------------------------------------------------------

DROP TABLE IF EXISTS `bdt_sc_pri_restri`;

CREATE TABLE `bdt_sc_pri_restri` (
  `bdt_sc_pri_restri_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `bdt_sc_id` int(11) unsigned NOT NULL,
  `cdt_sc_awd_pri_xps_type_map_id` int(11) unsigned DEFAULT NULL COMMENT 'This column is used when the BDT is derived from the CDT.',
  `code_list_id` int(11) unsigned DEFAULT NULL COMMENT 'Foreign key to identify the code list.',
  `is_default` tinyint(1) NOT NULL,
  `agency_id_list_id` int(11) unsigned DEFAULT NULL,
  PRIMARY KEY (`bdt_sc_pri_restri_id`),
  KEY `bspr_bdt_sc_id_fk` (`bdt_sc_id`),
  KEY `bspr_csapxtm_id_fk` (`cdt_sc_awd_pri_xps_type_map_id`),
  KEY `bspr_code_list_id_fk` (`code_list_id`),
  KEY `bspr_agency_id_list_id_fk` (`agency_id_list_id`),
  CONSTRAINT `bspr_agency_id_list_id_fk` FOREIGN KEY (`agency_id_list_id`) REFERENCES `agency_id_list` (`agency_id_list_id`),
  CONSTRAINT `bspr_bdt_sc_id_fk` FOREIGN KEY (`bdt_sc_id`) REFERENCES `dt_sc` (`dt_sc_id`),
  CONSTRAINT `bspr_code_list_id_fk` FOREIGN KEY (`code_list_id`) REFERENCES `code_list` (`code_list_id`),
  CONSTRAINT `bspr_csapxtm_id_fk` FOREIGN KEY (`cdt_sc_awd_pri_xps_type_map_id`) REFERENCES `cdt_sc_awd_pri_xps_type_map` (`cdt_sc_awd_pri_xps_type_map_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='It should be noted that the table does not store the fact about primitive restriction hierarchical relationships. In other words, if a BDT SC is derived from another BDT SC and the derivative BDT SC applies some primitive restrictions, that relationship will not be explicitly stored. The derivative BDT SC points directly to the CDT_Primitive_Expression_Type_Map key rather than the BDT_SC_Primitive_Restriction key.';

DROP TABLE IF EXISTS `BDT_SC_PRI_RESTRI_ID_SEQ`;

CREATE TABLE `BDT_SC_PRI_RESTRI_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `BDT_SC_PRI_RESTRI_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table bie_user_ext_revision
# ------------------------------------------------------------

DROP TABLE IF EXISTS `bie_user_ext_revision`;

CREATE TABLE `bie_user_ext_revision` (
  `bie_user_ext_revision_id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto-generate primary key of the table.',
  `top_level_abie_id` int(11) unsigned NOT NULL COMMENT 'This points to an ABIE record which is a top-level ABIE. The record must have the isTop_Level flag true.',
  `ext_abie_id` int(11) unsigned DEFAULT NULL COMMENT 'This points to an ABIE record corresponding to the Extension_ACC_ID record. For example, this column can point to the ApplicationAreaExtension ABIE which is based on the ApplicationAreaExtension ACC (referred by the Extension_ACC_ID column). This column can be Null only when the extension is the AllExtension because there is no corresponding ABIE for the AllExtension ACC.',
  `ext_acc_id` int(11) unsigned NOT NULL COMMENT 'This points to an extension ACC on which the ABIE indicated by the Extension_ABIE_ID column is based. E.g. It may point to an ApplicationAreaExtension ACC, AllExtension ACC, ActualLedgerExtension ACC, etc. It should be noted that an ACC record pointed to must have the OAGIS_Component_Type = 2 (Extension).',
  `user_ext_acc_id` int(11) unsigned NOT NULL COMMENT 'This column points to the specific revision of a User Extension ACC (this is an ACC whose OAGIS_Component_Type = 4) currently used by the ABIE as indicated by the Extension_ABIE_ID or the by the Top_Level_ABIE_ID (in case of the AllExtension). ',
  `revised_indicator` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This column is a flag indicating to whether the User Extension ACC (as identified in the User_Extension_ACC_ID column) has been revised, i.e., there is a newer version of the user extension ACC than the one currently used by the Extension_ABIE_ID. 0 means the User_Extension_ACC_ID is current, 1 means it is not current.',
  PRIMARY KEY (`bie_user_ext_revision_id`),
  KEY `buer_top_level_abie_id_fk` (`top_level_abie_id`),
  KEY `buer_ext_abie_id_fk` (`ext_abie_id`),
  KEY `buer_ext_acc_id_fk` (`ext_acc_id`),
  KEY `buer_user_ext_acc_id_fk` (`user_ext_acc_id`),
  CONSTRAINT `buer_ext_abie_id_fk` FOREIGN KEY (`ext_abie_id`) REFERENCES `abie` (`abie_id`),
  CONSTRAINT `buer_ext_acc_id_fk` FOREIGN KEY (`ext_acc_id`) REFERENCES `acc` (`acc_id`),
  CONSTRAINT `buer_top_level_abie_id_fk` FOREIGN KEY (`top_level_abie_id`) REFERENCES `abie` (`abie_id`),
  CONSTRAINT `buer_user_ext_acc_id_fk` FOREIGN KEY (`user_ext_acc_id`) REFERENCES `acc` (`acc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='This table keeps track of the User Extension ACC (the specific revision) used by an Extension ABIE. This can be a named extension (such as ApplicationAreaExtension) or the AllExtension. The Revised_Indicator flag is designed such that a revision of a User Extension can notify by setting this flag to true. The Top_Level_ABIE_ID column makes it more efficient to when opening a top-level ABIE, the user can be notified of any new revision extension. A record in this table is created only when there is a user extension to the the OAGIS extension component/ACC.';

DROP TABLE IF EXISTS `BIE_USER_EXT_REVISION_ID_SEQ`;

CREATE TABLE `BIE_USER_EXT_REVISION_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `BIE_USER_EXT_REVISION_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table biz_ctx
# ------------------------------------------------------------

DROP TABLE IF EXISTS `biz_ctx`;

CREATE TABLE `biz_ctx` (
  `biz_ctx_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `guid` varchar(41) NOT NULL,
  `name` varchar(100) CHARACTER SET utf8 COMMENT 'Short, descriptive name of the business context.',
  `created_by` int(11) unsigned NOT NULL,
  `last_updated_by` int(11) unsigned NOT NULL,
  `creation_timestamp` datetime NOT NULL,
  `last_update_timestamp` datetime NOT NULL,
  PRIMARY KEY (`biz_ctx_id`),
  UNIQUE KEY `biz_ctx_uk1` (`guid`),
  KEY `biz_ctx_created_by_fk` (`created_by`),
  KEY `biz_ctx_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `biz_ctx_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `biz_ctx_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `BIZ_CTX_ID_SEQ`;

CREATE TABLE `BIZ_CTX_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `BIZ_CTX_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table biz_ctx_value
# ------------------------------------------------------------

DROP TABLE IF EXISTS `biz_ctx_value`;

CREATE TABLE `biz_ctx_value` (
  `biz_ctx_value_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `biz_ctx_id` int(11) unsigned NOT NULL,
  `ctx_scheme_value_id` int(11) unsigned NOT NULL,
  PRIMARY KEY (`biz_ctx_value_id`),
  KEY `biz_ctx_value_biz_ctx_id_fk` (`biz_ctx_id`),
  KEY `biz_ctx_value_csv_id_fk` (`ctx_scheme_value_id`),
  CONSTRAINT `biz_ctx_value_biz_ctx_id_fk` FOREIGN KEY (`biz_ctx_id`) REFERENCES `biz_ctx` (`biz_ctx_id`),
  CONSTRAINT `biz_ctx_value_csv_id_fk` FOREIGN KEY (`ctx_scheme_value_id`) REFERENCES `ctx_scheme_value` (`ctx_scheme_value_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `BIZ_CTX_VALUE_ID_SEQ`;

CREATE TABLE `BIZ_CTX_VALUE_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `BIZ_CTX_VALUE_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table blob_content
# ------------------------------------------------------------

DROP TABLE IF EXISTS `blob_content`;

CREATE TABLE `blob_content` (
  `blob_content_id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
  `content` mediumblob NOT NULL COMMENT 'The Blob content of the schema file.',
  `release_id` int(11) unsigned NOT NULL COMMENT 'The release to which this file belongs/published.',
  `module` varchar(100) NOT NULL COMMENT 'The is the subdirectory and filename of the blob. The format is Windows file path. The starting directory shall be the root folder of all the release content. For example, for OAGIS 10.1 Model, the root directory is Model. If the file shall be directly under the Model directory, then this column should be ''Model\\filename.xsd''. If the file is under, say, Model\\Platform\\2_1\\Common\\Components directory, then the value of this column shall be ''Model\\Platform\\2_1\\Common\\Components\\filename.xsd''.',
  PRIMARY KEY (`blob_content_id`),
  KEY `blob_content_release_id_fk` (`release_id`),
  CONSTRAINT `blob_content_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='This table stores schemas in Blob.';

DROP TABLE IF EXISTS `BLOB_CONTENT_ID_SEQ`;

CREATE TABLE `BLOB_CONTENT_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `BLOB_CONTENT_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table cdt_awd_pri
# ------------------------------------------------------------

DROP TABLE IF EXISTS `cdt_awd_pri`;

CREATE TABLE `cdt_awd_pri` (
  `cdt_awd_pri_id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
  `cdt_id` int(11) unsigned NOT NULL COMMENT 'Foreign key from the dt table corresponding to the CDT being recorded.',
  `cdt_pri_id` int(11) unsigned NOT NULL COMMENT 'Foreign key from the cdt_pri table corresponding to the Allowed Primitive column in each of the CDT Content Component section/table in CCTS DTC3',
  `is_default` tinyint(1) NOT NULL COMMENT 'Indicating a default primitive for the CDT’s Content Component. True for a default primitive; False otherwise.',
  PRIMARY KEY (`cdt_awd_pri_id`),
  KEY `cdt_awd_pri_cdt_id_fk` (`cdt_id`),
  KEY `cdt_awd_pri_cdt_pri_id_fk` (`cdt_pri_id`),
  CONSTRAINT `cdt_awd_pri_cdt_id_fk` FOREIGN KEY (`cdt_id`) REFERENCES `dt` (`dt_id`),
  CONSTRAINT `cdt_awd_pri_cdt_pri_id_fk` FOREIGN KEY (`cdt_pri_id`) REFERENCES `cdt_pri` (`cdt_pri_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='This table capture allowed primitives of the CDT’s Content Component.';

DROP TABLE IF EXISTS `CDT_AWD_PRI_ID_SEQ`;

CREATE TABLE `CDT_AWD_PRI_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `CDT_AWD_PRI_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table cdt_awd_pri_xps_type_map
# ------------------------------------------------------------

DROP TABLE IF EXISTS `cdt_awd_pri_xps_type_map`;

CREATE TABLE `cdt_awd_pri_xps_type_map` (
  `cdt_awd_pri_xps_type_map_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `cdt_awd_pri_id` int(11) unsigned NOT NULL,
  `xbt_id` int(11) unsigned NOT NULL,
  PRIMARY KEY (`cdt_awd_pri_xps_type_map_id`),
  KEY `capxtm_cdt_awd_pri_id_fk` (`cdt_awd_pri_id`),
  KEY `capxtm_xbt_id_fk` (`xbt_id`),
  CONSTRAINT `capxtm_cdt_awd_pri_id_fk` FOREIGN KEY (`cdt_awd_pri_id`) REFERENCES `cdt_awd_pri` (`cdt_awd_pri_id`),
  CONSTRAINT `capxtm_xbt_id_fk` FOREIGN KEY (`xbt_id`) REFERENCES `xbt` (`xbt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='This table allows for concrete mapping between the CDT Primitives and types in a particular expression such as XML Schema, JSON. At this point, it is not clear whether a separate table will be needed for each expression. The current table holds the map to XML Schema built-in types. \n\nFor each additional expression columns similar to the xbt_id will need to be added to this table for mapping to data types in another expression.\n\nIf we use a separate table for each expression, then we need binding all the way to BDT (or even BBIE) for every new expression. That would be almost like just store a BDT file. But using columns has no gaurantee that it will work with all kinds of expressions. If the typing in another expression is less finer grain than the XSD built-in types, I think the additional columns will work.';

DROP TABLE IF EXISTS `CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ`;

CREATE TABLE `CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table cdt_pri
# ------------------------------------------------------------

DROP TABLE IF EXISTS `cdt_pri`;

CREATE TABLE `cdt_pri` (
  `cdt_pri_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`cdt_pri_id`),
  UNIQUE KEY `cdt_pri_uk1` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `CDT_PRI_ID_SEQ`;

CREATE TABLE `CDT_PRI_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `CDT_PRI_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table cdt_sc_awd_pri
# ------------------------------------------------------------

DROP TABLE IF EXISTS `cdt_sc_awd_pri`;

CREATE TABLE `cdt_sc_awd_pri` (
  `cdt_sc_awd_pri_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `cdt_sc_id` int(11) unsigned NOT NULL,
  `cdt_pri_id` int(11) unsigned NOT NULL,
  `is_default` tinyint(1) NOT NULL COMMENT 'Indicating whether the primitive is the default primitive of the supplementary component.',
  PRIMARY KEY (`cdt_sc_awd_pri_id`),
  KEY `cdt_sc_id_fk` (`cdt_sc_id`),
  KEY `cdt_pri_id_fk` (`cdt_pri_id`),
  CONSTRAINT `cdt_pri_id_fk` FOREIGN KEY (`cdt_pri_id`) REFERENCES `cdt_pri` (`cdt_pri_id`),
  CONSTRAINT `cdt_sc_id_fk` FOREIGN KEY (`cdt_sc_id`) REFERENCES `dt_sc` (`dt_sc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='This table capture the CDT primitives allowed for a particular SC of CDTs. It also store the CDT primitives allowed for a SC of a BDT that extends its base.';

DROP TABLE IF EXISTS `CDT_SC_AWD_PRI_ID_SEQ`;

CREATE TABLE `CDT_SC_AWD_PRI_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `CDT_SC_AWD_PRI_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table cdt_sc_awd_pri_xps_type_map
# ------------------------------------------------------------

DROP TABLE IF EXISTS `cdt_sc_awd_pri_xps_type_map`;

CREATE TABLE `cdt_sc_awd_pri_xps_type_map` (
  `cdt_sc_awd_pri_xps_type_map_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `cdt_sc_awd_pri` int(11) unsigned NOT NULL,
  `xbt_id` int(11) unsigned NOT NULL,
  PRIMARY KEY (`cdt_sc_awd_pri_xps_type_map_id`),
  KEY `csapxtm_csap_fk` (`cdt_sc_awd_pri`),
  KEY `csapxtm_xbt_id_fk` (`xbt_id`),
  CONSTRAINT `csapxtm_csap_fk` FOREIGN KEY (`cdt_sc_awd_pri`) REFERENCES `cdt_sc_awd_pri` (`cdt_sc_awd_pri_id`),
  CONSTRAINT `csapxtm_xbt_id_fk` FOREIGN KEY (`xbt_id`) REFERENCES `xbt` (`xbt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ`;

CREATE TABLE `CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table classification_ctx_scheme
# ------------------------------------------------------------

DROP TABLE IF EXISTS `classification_ctx_scheme`;

CREATE TABLE `classification_ctx_scheme` (
  `classification_ctx_scheme_id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'External identification of the scheme.',
  `guid` varchar(41) NOT NULL,
  `scheme_id` varchar(45) CHARACTER SET utf8 NOT NULL COMMENT 'External identification of the scheme.',
  `scheme_name` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `description` text CHARACTER SET utf8,
  `scheme_agency_id` varchar(45) CHARACTER SET utf8 NOT NULL,
  `scheme_version_id` varchar(45) CHARACTER SET utf8 NOT NULL,
  `ctx_category_id` int(11) unsigned NOT NULL,
  `created_by` int(11) unsigned NOT NULL,
  `last_updated_by` int(11) unsigned NOT NULL,
  `creation_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_update_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`classification_ctx_scheme_id`),
  UNIQUE KEY `ctx_scheme_uk1` (`guid`),
  KEY `ctx_scheme_ctx_cat_id_fk` (`ctx_category_id`),
  KEY `ctx_scheme_created_by_fk` (`created_by`),
  KEY `ctx_scheme_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `ctx_scheme_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `ctx_scheme_ctx_cat_id_fk` FOREIGN KEY (`ctx_category_id`) REFERENCES `ctx_category` (`ctx_category_id`),
  CONSTRAINT `ctx_scheme_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `CTX_SCHEME_ID_SEQ`;

CREATE TABLE `CTX_SCHEME_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `CTX_SCHEME_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table client
# ------------------------------------------------------------

DROP TABLE IF EXISTS `client`;

CREATE TABLE `client` (
  `client_id` int(11) unsigned NOT NULL,
  `name` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `CLIENT_ID_SEQ`;

CREATE TABLE `CLIENT_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `CLIENT_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table code_list
# ------------------------------------------------------------

DROP TABLE IF EXISTS `code_list`;

CREATE TABLE `code_list` (
  `code_list_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `guid` varchar(41) CHARACTER SET utf8 NOT NULL,
  `enum_type_guid` varchar(41) CHARACTER SET utf8 DEFAULT NULL,
  `name` varchar(100) CHARACTER SET utf8,
  `list_id` varchar(100) CHARACTER SET utf8 NOT NULL COMMENT 'External identifier.',
  `agency_id` int(11) unsigned NOT NULL,
  `version_id` varchar(10) CHARACTER SET utf8 NOT NULL,
  `definition` text CHARACTER SET utf8,
  `remark` varchar(225) DEFAULT NULL COMMENT 'This column allows the user to codify context specific usage of the BIE. It is different from the Definition column in that the Definition column is a descriptive text while this one is machine understandable. So the data type of this column is more like code.',
  `definition_source` varchar(100) CHARACTER SET utf8,
  `based_code_list_id` int(11) unsigned DEFAULT NULL COMMENT 'This indicates that this code list is based on another code list - restriction and extension are allowed.',
  `extensible_indicator` tinyint(1) NOT NULL,
  `module` varchar(100) COMMENT 'The is the subdirectory and filename of the blob. The format is Windows file path. The starting directory shall be the root folder of all the release content. For example, for OAGIS 10.1 Model, the root directory is Model. If the file shall be directly under the Model directory, then this column should be ''Model\\filename.xsd''. If the file is under, say, Model\\Platform\\2_1\\Common\\Components directory, then the value of this column shall be ''Model\\Platform\\2_1\\Common\\Components\\filename.xsd''.',
  `created_by` int(11) unsigned NOT NULL,
  `last_updated_by` int(11) unsigned NOT NULL,
  `creation_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_update_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `state` varchar(10) NOT NULL COMMENT 'Life cycle state of the Code List. Possible values are Editing, Published, or Deleted.',
  PRIMARY KEY (`code_list_id`),
  UNIQUE KEY `code_list_uk1` (`guid`),
  UNIQUE KEY `code_list_uk2` (`enum_type_guid`),
  KEY `code_list_agency_id_fk` (`agency_id`),
  KEY `code_list_based_cl_id_fk` (`based_code_list_id`),
  KEY `code_list_created_by_fk` (`created_by`),
  KEY `code_list_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `code_list_agency_id_fk` FOREIGN KEY (`agency_id`) REFERENCES `agency_id_list_value` (`agency_id_list_value_id`),
  CONSTRAINT `code_list_based_cl_id_fk` FOREIGN KEY (`based_code_list_id`) REFERENCES `code_list` (`code_list_id`),
  CONSTRAINT `code_list_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `code_list_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='When a code list is derived, the whole set of code values belonging to that code list will be copied.';

DROP TABLE IF EXISTS `CODE_LIST_ID_SEQ`;

CREATE TABLE `CODE_LIST_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `CODE_LIST_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table code_list_value
# ------------------------------------------------------------

DROP TABLE IF EXISTS `code_list_value`;

CREATE TABLE `code_list_value` (
  `code_list_value_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `code_list_id` int(11) unsigned NOT NULL,
  `value` varchar(100) NOT NULL,
  `name` varchar(100),
  `definition` text,
  `definition_source` varchar(100),
  `used_indicator` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'This indicates whether the code value is allowed to be used or not in that code list context.',
  `locked_indicator` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This indicates whether the Used_Indicator can be changed from False to True. In other words, if the code value is derived from its base code value and the Used_Indicator of the base is False, then the Used_Indicator cannot be changed from False to True in the derivation if the Locked_Indicator is true.',
  `extension_Indicator` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`code_list_value_id`),
  KEY `clv_code_list_id_fk` (`code_list_id`),
  CONSTRAINT `clv_code_list_id_fk` FOREIGN KEY (`code_list_id`) REFERENCES `code_list` (`code_list_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `CODE_LIST_VALUE_ID_SEQ`;

CREATE TABLE `CODE_LIST_VALUE_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `CODE_LIST_VALUE_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table ctx_category
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ctx_category`;

CREATE TABLE `ctx_category` (
  `ctx_category_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `guid` varchar(41) NOT NULL,
  `name` varchar(45) DEFAULT NULL,
  `description` text,
  PRIMARY KEY (`ctx_category_id`),
  UNIQUE KEY `ctx_category_uk1` (`guid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `CTX_CATEGORY_ID_SEQ`;

CREATE TABLE `CTX_CATEGORY_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `CTX_CATEGORY_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table ctx_scheme_value
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ctx_scheme_value`;

CREATE TABLE `ctx_scheme_value` (
  `ctx_scheme_value_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `guid` varchar(41) NOT NULL,
  `value` varchar(45) NOT NULL,
  `meaning` text,
  `owner_ctx_scheme_id` int(11) unsigned DEFAULT NULL,
  PRIMARY KEY (`ctx_scheme_value_id`),
  UNIQUE KEY `ctx_scheme_value_uk1` (`guid`),
  KEY `csv_owner_ctx_scheme_id_fk` (`owner_ctx_scheme_id`),
  CONSTRAINT `csv_owner_ctx_scheme_id_fk` FOREIGN KEY (`owner_ctx_scheme_id`) REFERENCES `classification_ctx_scheme` (`classification_ctx_scheme_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `CTX_SCHEME_VALUE_ID_SEQ`;

CREATE TABLE `CTX_SCHEME_VALUE_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `CTX_SCHEME_VALUE_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table dt
# ------------------------------------------------------------

DROP TABLE IF EXISTS `dt`;

CREATE TABLE `dt` (
  `dt_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `guid` varchar(41) CHARACTER SET utf8 NOT NULL,
  `type` int(11) NOT NULL COMMENT 'List value: 0 = CDT, 1 = BDT.',
  `version_num` varchar(45) CHARACTER SET utf8 NOT NULL COMMENT 'Format X.Y.Z where all of them are integer with no leading zero allowed. X means major version number, Y means minor version number and Z means patch version number.',
  `previous_version_dt_id` int(11) unsigned DEFAULT NULL,
  `data_type_term` varchar(45) CHARACTER SET utf8 DEFAULT NULL COMMENT 'This column is derived from the Based_DT_ID when the column is not blank. ',
  `qualifier` varchar(100) CHARACTER SET utf8 COMMENT 'This column should be blank when the DT_Type is CDT. When the DT_Type is BDT, this is optional - if blank that the row is a unqualified BDT, if not blank it is a qualified BDT.',
  `based_dt_id` int(11) unsigned DEFAULT NULL COMMENT 'Foreign key pointing to itself. This column must be blank when the DT_Type is CDT. This column must not be blank when the DT_Type is BDT.',
  `den` varchar(200) CHARACTER SET utf8 NOT NULL COMMENT 'This column should be automatically derived.',
  `content_component_den` varchar(200) CHARACTER SET utf8 COMMENT 'When the DT_Type is CDT this column is automatically derived from Data_Type_Term as "<Data_Type_Term>. Content", where Content is called property term of the content component according to CCTS. When the DT_Type is BDT this column is automaticlaly derived from the Based_DT_ID.',
  `definition` text CHARACTER SET utf8,
  `content_component_definition` text CHARACTER SET utf8,
  `revision_doc` text CHARACTER SET utf8 COMMENT 'This is for documenting about the revision.',
  `module` varchar(100) COMMENT 'physical file where the DT shall belong to when generating it.',
  `state` int(11) DEFAULT NULL COMMENT '1 = Editing, 2 = Candidate, 3 = Published. This the revision life cycle state of the entity.\n\nState change can''t be undone. But the history record can still keep the records of when the state was changed.',
  `created_by` int(11) unsigned NOT NULL,
  `owner_user_id` int(11) unsigned NOT NULL COMMENT 'This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership. ',
  `last_updated_by` int(11) unsigned NOT NULL,
  `creation_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_update_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `revision_num` int(11) NOT NULL DEFAULT '0' COMMENT 'Revision_Number is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).',
  `revision_tracking_num` int(11) NOT NULL DEFAULT '0' COMMENT 'Revision_Tracking_Number supports the ability to undo changes during a revision (life cycle of a revision is from the component''s Editing state to Published state). Once the component has transitioned into the Published state for its particular revision, all revision tracking records are deleted except the latest one. Revision_Tracking_Number can be 0, 1, 2, and so on. The zero value is assign to the record with Revision_Number = 0 as a default.',
  `revision_action` tinyint(11) DEFAULT '1' COMMENT 'This indicates the action associated with the record. The action can be 1 = insert, 2 = update, and 3 = delete. This column is null for the current record.',
  `release_id` int(11) unsigned DEFAULT NULL COMMENT 'Release_ID is an incremental integer. It is an unformatted counter part of the Release_Number in the Release table. Release_ID can be 1, 2, 3, and so on. Release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the Revision_Action column).\n\nNot all component revisions have an associated Release_ID because some revisions may never be released.\n\nUnpublished components cannot be released.\n\nThis column is null for the current record.',
  `current_bdt_id` int(11) unsigned DEFAULT NULL COMMENT 'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the record whose Revision_Number is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.\n\nIt is noted that although this is a foreign key by definition, we don''t specify a foreign key in the data model. This is because when an entity is deleted the current record won''t exist anymore.\n\nThe value of this column for the current record should be left NULL.',
  `is_deprecated` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  PRIMARY KEY (`dt_id`),
  UNIQUE KEY `DT_UK1` (`guid`),
  KEY `dt_previous_version_dt_id_fk` (`previous_version_dt_id`),
  KEY `dt_based_dt_id_fk` (`based_dt_id`),
  KEY `dt_created_by_fk` (`created_by`),
  KEY `dt_owner_user_id_fk` (`owner_user_id`),
  KEY `dt_last_updated_by_fk` (`last_updated_by`),
  KEY `dt_release_id_fk` (`release_id`),
  KEY `dt_current_bdt_id_fk` (`current_bdt_id`),
  CONSTRAINT `dt_based_dt_id_fk` FOREIGN KEY (`based_dt_id`) REFERENCES `dt` (`dt_id`),
  CONSTRAINT `dt_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `dt_current_bdt_id_fk` FOREIGN KEY (`current_bdt_id`) REFERENCES `dt` (`dt_id`),
  CONSTRAINT `dt_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `dt_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `dt_previous_version_dt_id_fk` FOREIGN KEY (`previous_version_dt_id`) REFERENCES `dt` (`dt_id`),
  CONSTRAINT `dt_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `DT_ID_SEQ`;

CREATE TABLE `DT_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `DT_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table dt_sc
# ------------------------------------------------------------

DROP TABLE IF EXISTS `dt_sc`;

CREATE TABLE `dt_sc` (
  `dt_sc_id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Revision is not tracked at the supplementary component. It is considered intrinsic part of the DT. In other words, when a new revision of a DT is created a new set of supplementary components is created along with it. ',
  `guid` varchar(41) NOT NULL,
  `property_term` varchar(60) DEFAULT NULL,
  `representation_term` varchar(20) DEFAULT NULL,
  `definition` text,
  `owner_dt_id` int(11) unsigned NOT NULL COMMENT 'Foreigned key to the Core_Data_Type table indicating the data type to which this supplementary component belongs.',
  `min_cardinality` int(11) NOT NULL DEFAULT '0',
  `max_cardinality` int(11) DEFAULT NULL,
  `based_dt_sc_id` int(11) unsigned DEFAULT NULL,
  PRIMARY KEY (`dt_sc_id`),
  UNIQUE KEY `dt_sc_uk1` (`guid`),
  KEY `dt_sc_owner_dt_id_fk` (`owner_dt_id`),
  KEY `dt_sc_based_dt_sc_id_fk` (`based_dt_sc_id`),
  CONSTRAINT `dt_sc_based_dt_sc_id_fk` FOREIGN KEY (`based_dt_sc_id`) REFERENCES `dt_sc` (`dt_sc_id`),
  CONSTRAINT `dt_sc_owner_dt_id_fk` FOREIGN KEY (`owner_dt_id`) REFERENCES `dt` (`dt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `DT_SC_ID_SEQ`;

CREATE TABLE `DT_SC_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `DT_SC_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table namespace
# ------------------------------------------------------------

DROP TABLE IF EXISTS `namespace`;

CREATE TABLE `namespace` (
  `namespace_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `uri` varchar(100) NOT NULL COMMENT 'This is the URI of the namespace.',
  `prefix` varchar(45) DEFAULT NULL COMMENT 'This is a default short name to represent the URI. It may be override during the the expression generation. Null or empty means the same thing like the default prefix in an XML schema.',
  `description` varchar(200) COMMENT 'Description or explanation about the namespace or use of the namespace.',
  `is_std_nmsp` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This indicates whether the namespace is reserved for standard used (i.e., whether it is an OAGIS namespace). If it is true, then end users cannot user the namespace for the end user CCs.',
  `owner_user_id` int(11) unsigned NOT NULL COMMENT 'The user who can update or delete the record.',
  `created_by` int(11) unsigned NOT NULL COMMENT 'The user who created the namespace.',
  `last_updated_by` int(11) unsigned NOT NULL COMMENT 'The use who last updated the record.',
  `creation_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The time when the record was first created.',
  `last_updated_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The time the record was last updated.',
  PRIMARY KEY (`namespace_id`),
  KEY `namespace_owner_user_id_fk` (`owner_user_id`),
  KEY `namespace_created_by_fk` (`created_by`),
  KEY `namespace_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `namespace_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `namespace_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `namespace_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `NAMESPACE_ID_SEQ`;

CREATE TABLE `NAMESPACE_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `NAMESPACE_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table release
# ------------------------------------------------------------

DROP TABLE IF EXISTS `release`;

CREATE TABLE `release` (
  `release_id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Release_ID must be an incremental integer. Release_ID that is more than another Release_ID is interpreted to be released later than the other.',
  `release_num` varchar(45) NOT NULL COMMENT 'Release number such has 10.0, 10.1, etc. ',
  `release_note` longtext COMMENT 'Description or note associated with the release.',
  `namespace_id` int(11) unsigned NOT NULL,
  PRIMARY KEY (`release_id`),
  KEY `namespace_id_fk` (`namespace_id`),
  CONSTRAINT `namespace_id_fk` FOREIGN KEY (`namespace_id`) REFERENCES `namespace` (`namespace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `RELEASE_ID_SEQ`;

CREATE TABLE `RELEASE_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `RELEASE_ID_SEQ` (`next_val`) VALUES (1);



# Dump of table xbt
# ------------------------------------------------------------

DROP TABLE IF EXISTS `xbt`;

CREATE TABLE `xbt` (
  `xbt_id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Unique ID of XML Schema built-in types.',
  `name` varchar(45) DEFAULT NULL COMMENT 'Human readable name of the XML Schema built-in type.',
  `builtIn_type` varchar(45) DEFAULT NULL COMMENT 'XML Schema built-in type as it should appear in an XML schema.',
  `subtype_of_xbt_id` int(11) unsigned DEFAULT NULL,
  PRIMARY KEY (`xbt_id`),
  UNIQUE KEY `xbt_uk1` (`name`),
  UNIQUE KEY `xbt_uk2` (`builtIn_type`),
  KEY `xbt_subtype_of_xbt_id_fk` (`subtype_of_xbt_id`),
  CONSTRAINT `xbt_subtype_of_xbt_id_fk` FOREIGN KEY (`subtype_of_xbt_id`) REFERENCES `xbt` (`xbt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `XBT_ID_SEQ`;

CREATE TABLE `XBT_ID_SEQ` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `XBT_ID_SEQ` (`next_val`) VALUES (1);




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
