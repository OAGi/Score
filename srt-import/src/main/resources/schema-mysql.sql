# ************************************************************
# Database: oagi
# Generation Time: 2018-05-29 16:18:03 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
SET NAMES utf8mb4;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table abie
# ------------------------------------------------------------

DROP TABLE IF EXISTS `abie`;

CREATE TABLE `abie` (
  `abie_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an ABIE.',
  `guid` varchar(41) NOT NULL COMMENT 'A globally unique identifier (GUID) of an ABIE. GUID of an ABIE is different from its based ACC. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `based_acc_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key to the ACC table refering to the ACC, on which the business context has been applied to derive this ABIE.',
  `biz_ctx_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key to the BIZ_CTX table. This column stores the business context assigned to the ABIE.',
  `definition` text COMMENT 'Definition to override the ACC''s definition. If NULL, it means that the definition should be inherited from the based CC.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the ABIE. The creator of the ABIE is also its owner by default. ABIEs created as children of another ABIE have the same CREATED_BY as its parent.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the ABIE record. This may be the user who is in the same group as the creator.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the ABIE record was first created. ABIEs created as children of another ABIE have the same CREATION_TIMESTAMP.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the ABIE was last updated.',
  `state` int(11) DEFAULT NULL COMMENT '2 = EDITING, 4 = PUBLISHED. This column is only used with a top-level ABIE, because that is the only entry point for editing. The state value indicates the visibility of the top-level ABIE to users other than the owner. In the user group environment, a logic can apply that other users in the group can see the top-level ABIE only when it is in the ''Published'' state.',
  `client_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the CLIENT table. The use case associated with this column is to indicate the organizational entity for which the profile BOD is created. For example, Boeing may generate a profile BOD for Boeing civilian or Boeing defense. It is more for the documentation purpose. Only an ABIE which is the top-level ABIE can use this column.',
  `version` varchar(45) DEFAULT NULL COMMENT 'This column hold a version number assigned by the user. This column is only used by the top-level ABIE. No format of version is enforced.',
  `status` varchar(45) DEFAULT NULL COMMENT 'This is different from the STATE column which is CRUD life cycle of an entity. The use case for this is to allow the user to indicate the usage status of a top-level ABIE (a profile BOD). An integration architect can use this column. Example values are ?Prototype?, ?Test?, and ?Production?. Only the top-level ABIE can use this field.',
  `remark` varchar(225) DEFAULT NULL COMMENT 'This column allows the user to specify very context-specific usage of the BIE. It is different from the DEFINITION column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be "Type of BOM should be recognized in the BOM/typeCode."',
  `biz_term` varchar(225) DEFAULT NULL COMMENT 'To indicate what the BIE is called in a particular business context. With this current design, only one business term is allowed per business context.',
  `owner_top_level_abie_id` bigint(20) unsigned NOT NULL COMMENT 'This is a foriegn key to the ABIE itself. It specifies the top-level ABIE which owns this ABIE record. For the ABIE that is a top-level ABIE itself, this column will have the same value as the ABIE_ID column. ',
  PRIMARY KEY (`abie_id`),
  UNIQUE KEY `abie_uk1` (`guid`),
  KEY `abie_based_acc_id_fk` (`based_acc_id`),
  KEY `abie_biz_ctx_id_fk` (`biz_ctx_id`),
  KEY `abie_client_id_fk` (`client_id`),
  KEY `abie_created_by_fk` (`created_by`),
  KEY `abie_last_updated_by_fk` (`last_updated_by`),
  KEY `abie_owner_top_level_abie_id_fk` (`owner_top_level_abie_id`),
  CONSTRAINT `abie_based_acc_id_fk` FOREIGN KEY (`based_acc_id`) REFERENCES `acc` (`acc_id`),
  CONSTRAINT `abie_biz_ctx_id_fk` FOREIGN KEY (`biz_ctx_id`) REFERENCES `biz_ctx` (`biz_ctx_id`),
  CONSTRAINT `abie_client_id_fk` FOREIGN KEY (`client_id`) REFERENCES `client` (`client_id`),
  CONSTRAINT `abie_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `abie_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `abie_owner_top_level_abie_id_fk` FOREIGN KEY (`owner_top_level_abie_id`) REFERENCES `top_level_abie` (`top_level_abie_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The ABIE table stores information about an ABIE, which is a contextualized ACC. The context is represented by the BUSINESS_CTX_ID column that refers to a business context. Each ABIE must have a business context and a based ACC.\n\nIt should be noted that, per design document, there is no corresponding ABIE created for an ACC which will not show up in the instance document such as ACCs of OAGIS_COMPONENT_TYPE "SEMANTIC_GROUP", "USER_EXTENSION_GROUP", etc.';



# Dump of table acc
# ------------------------------------------------------------

DROP TABLE IF EXISTS `acc`;

CREATE TABLE `acc` (
  `acc_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an ACC.',
  `guid` varchar(41) NOT NULL COMMENT 'A globally unique identifier (GUID) of an ACC. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `object_class_term` varchar(100) NOT NULL COMMENT 'Object class name of the ACC concept. For OAGIS, this is generally name of a type with the "Type" truncated from the end. Per CCS the name is space separated. "ID" is expanded to "Identifier".',
  `den` varchar(200) NOT NULL COMMENT 'DEN (dictionary entry name) of the ACC. It can be derived as OBJECT_CLASS_QUALIFIER + "_ " + OBJECT_CLASS_TERM + ". Details".',
  `definition` text COMMENT 'This is a documentation or description of the ACC. Since ACC is business context independent, this is a business context independent description of the ACC concept.',
  `definition_source` varchar(100) DEFAULT NULL COMMENT 'This is typically a URL identifying the source of the DEFINITION column.',
  `based_acc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'BASED_ACC_ID is a foreign key to the ACC table itself. It represents the ACC that is qualified by this ACC. In general CCS sense, a qualification can be a content extension or restriction, but the current scope supports only extension.',
  `object_class_qualifier` varchar(100) DEFAULT NULL COMMENT 'This column stores the qualifier of an ACC, particularly when it has a based ACC. ',
  `oagis_component_type` int(11) DEFAULT NULL COMMENT 'The value can be 0 = BASE, 1 = SEMANTICS, 2 = EXTENSION, 3 = SEMANTIC_GROUP, 4 = USER_EXTENSION_GROUP, 5 = EMBEDDED. Generally, BASE is assigned when the OBJECT_CLASS_TERM contains "Base" at the end. EXTENSION is assigned with the OBJECT_CLASS_TERM contains "Extension" at the end. SEMANTIC_GROUP is assigned when an ACC is imported from an XSD Group. USER_EXTENSION_GROUP is a wrapper ACC (a virtual ACC) for segregating user''s extension content. EMBEDDED is used for an ACC whose content is not explicitly defined in the database, for example, the Any Structured Content ACC that corresponds to the xsd:any.  Other cases are assigned SEMANTICS. ',
  `module_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the module table indicating the physical schema the ACC belongs to.',
  `namespace_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the NAMESPACE table. This is the namespace to which the entity belongs. This namespace column is primarily used in the case the component is a user''s component because there is also a namespace assigned at the release level.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the user who creates the entity.\\n\\nThis column never change between the history and the current record for a given revision. The history record should have the same value as that of its current record.',
  `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\\n\\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership. ',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record. \\n\\nIn the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the revision of the ACC was created. \\n\\nThis never change for a revision.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.\\n\\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.',
  `state` int(11) NOT NULL COMMENT '1 = EDITING, 2 = CANDIDATE, 3 = PUBLISHED. This the revision life cycle state of the ACC.\n\nState change can''t be undone. But the history record can still keep the records of when the state was changed.',
  `revision_num` int(11) NOT NULL DEFAULT '0' COMMENT 'REVISION_NUM is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).',
  `revision_tracking_num` int(11) NOT NULL DEFAULT '0' COMMENT 'REVISION_TRACKING_NUM supports the ability to undo changes during a revision (life cycle of a revision is from the component''s EDITING state to PUBLISHED state). Once the component has transitioned into the PUBLISHED state for its particular revision, all revision tracking records are deleted except the latest one. REVISION_TRACKING_NUMB can be 0, 1, 2, and so on. The zero value is assigned to the record with REVISION_NUM = 0 as a default.',
  `revision_action` tinyint(4) DEFAULT '1' COMMENT 'This indicates the action associated with the record. The action can be 1 = INSERT, 2 = UPDATE, and 3 = DELETE. This column is null for the current record.',
  `release_id` bigint(20) unsigned DEFAULT NULL COMMENT 'RELEASE_ID is an incremental integer. It is an unformatted counter part of the RELEASE_NUMBER in the RELEASE table. RELEASE_ID can be 1, 2, 3, and so on. A release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the REVISION_ACTION column).\\n\\nNot all component revisions have an associated RELEASE_ID because some revisions may never be released. USER_EXTENSION_GROUP component type is never part of a release.\\n\\nUnpublished components cannot be released.\\n\\nThis column is NULL for the current record.',
  `current_acc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the the record whose REVISION_NUM is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.\\n\\nIt is noted that although this is a foreign key by definition, we don''t specify a foreign key in the data model. This is because when an entity is deleted the current record won''t exist anymore.\\n\\nThe value of this column for the current record should be left NULL.',
  `is_deprecated` tinyint(1) DEFAULT '0' COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be allowed).',
  `is_abstract` tinyint(1) DEFAULT '0' COMMENT 'This is the XML Schema abstract flag. Default is false. If it is true, the abstract flag will be set to true when generating a corresponding xsd:complexType. So although this flag may not apply to some ACCs such as those that are xsd:group. It is still have a false value.',
  PRIMARY KEY (`acc_id`),
  KEY `acc_based_acc_id_fk` (`based_acc_id`),
  KEY `acc_created_by_fk` (`created_by`),
  KEY `acc_current_acc_id_fk` (`current_acc_id`),
  KEY `acc_last_updated_by_fk` (`last_updated_by`),
  KEY `acc_module_id_fk` (`module_id`),
  KEY `acc_namespace_id_fk` (`namespace_id`),
  KEY `acc_owner_user_id_fk` (`owner_user_id`),
  KEY `acc_release_id_fk` (`release_id`),
  CONSTRAINT `acc_based_acc_id_fk` FOREIGN KEY (`based_acc_id`) REFERENCES `acc` (`acc_id`),
  CONSTRAINT `acc_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `acc_current_acc_id_fk` FOREIGN KEY (`current_acc_id`) REFERENCES `acc` (`acc_id`),
  CONSTRAINT `acc_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `acc_module_id_fk` FOREIGN KEY (`module_id`) REFERENCES `module` (`module_id`),
  CONSTRAINT `acc_namespace_id_fk` FOREIGN KEY (`namespace_id`) REFERENCES `namespace` (`namespace_id`),
  CONSTRAINT `acc_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `acc_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The ACC table holds information about complex data structured concepts. For example, OAGIS''s Components, Nouns, and BODs are captured in the ACC table.\n\nNote that only Extension is supported when deriving ACC from another ACC. (So if there is a restriction needed, maybe that concept should placed higher in the derivation hierarchy rather than lower.)\n\nIn OAGIS, all XSD extensions will be treated as a qualification of an ACC.';



# Dump of table agency_id_list
# ------------------------------------------------------------

DROP TABLE IF EXISTS `agency_id_list`;

CREATE TABLE `agency_id_list` (
  `agency_id_list_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key.',
  `guid` varchar(41) DEFAULT NULL COMMENT 'A globally unique identifier (GUID) of an agency identifier scheme. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `enum_type_guid` varchar(41) NOT NULL COMMENT 'This column stores the GUID of the type containing the enumerated values. In OAGIS, most code lists and agnecy ID lists are defined by an XyzCodeContentType (or XyzAgencyIdentificationContentType) and XyzCodeEnumerationType (or XyzAgencyIdentificationEnumerationContentType). However, some don''t have the enumeration type. When that is the case, this column is null.',
  `name` varchar(100) DEFAULT NULL COMMENT 'Name of the agency identification list.',
  `list_id` varchar(10) DEFAULT NULL COMMENT 'This is a business or standard identification assigned to the agency identification list.',
  `agency_id_list_value_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is the identification of the agency or organization which developed and/or maintains the list. Theoretically, this can be modeled as a self-reference foreign key, but it is not implemented at this point.',
  `version_id` varchar(10) DEFAULT NULL COMMENT 'Version number of the agency identification list (assigned by the agency).',
  `module_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the module table indicating the physical schema the MODULE belongs to.',
  `definition` text COMMENT 'Description of the agency identification list.',
  PRIMARY KEY (`agency_id_list_id`),
  UNIQUE KEY `agency_id_list_uk2` (`enum_type_guid`),
  UNIQUE KEY `agency_id_list_uk1` (`guid`),
  KEY `agency_id_list_agency_id_list_value_id_fk` (`agency_id_list_value_id`),
  KEY `agency_id_list_module_id_fk` (`module_id`),
  CONSTRAINT `agency_id_list_agency_id_list_value_id_fk` FOREIGN KEY (`agency_id_list_value_id`) REFERENCES `agency_id_list_value` (`agency_id_list_value_id`),
  CONSTRAINT `agency_id_list_module_id_fk` FOREIGN KEY (`module_id`) REFERENCES `module` (`module_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The AGENCY_ID_LIST table stores information about agency identification lists. The list''s values are however kept in the AGENCY_ID_LIST_VALUE.';



# Dump of table agency_id_list_value
# ------------------------------------------------------------

DROP TABLE IF EXISTS `agency_id_list_value`;

CREATE TABLE `agency_id_list_value` (
  `agency_id_list_value_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
  `value` varchar(150) NOT NULL COMMENT 'A value in the agency identification list.',
  `name` varchar(150) DEFAULT NULL COMMENT 'Descriptive or short name of the value.',
  `definition` text COMMENT 'The meaning of the value.',
  `owner_list_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the agency identification list in the AGENCY_ID_LIST table this value belongs to.',
  PRIMARY KEY (`agency_id_list_value_id`),
  KEY `agency_id_list_value_owner_list_id_fk` (`owner_list_id`),
  CONSTRAINT `agency_id_list_value_owner_list_id_fk` FOREIGN KEY (`owner_list_id`) REFERENCES `agency_id_list` (`agency_id_list_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table captures the values within an agency identification list.';



# Dump of table app_user
# ------------------------------------------------------------

DROP TABLE IF EXISTS `app_user`;

CREATE TABLE `app_user` (
  `app_user_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
  `login_id` varchar(45) NOT NULL COMMENT 'User Id of the user.',
  `password` varchar(100) NOT NULL COMMENT 'Password to authenticate the user.',
  `name` varchar(100) DEFAULT NULL COMMENT 'Full name of the user.',
  `organization` varchar(100) DEFAULT NULL COMMENT 'The company the user represents.',
  `oagis_developer_indicator` tinyint(1) NOT NULL COMMENT 'This indicates whether the user can edit OAGIS Model content. Content created by the OAGIS developer is also considered OAGIS Model content.',
  PRIMARY KEY (`app_user_id`),
  UNIQUE KEY `app_user_uk1` (`login_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table captures the user information for authentication and authorization purposes.';



# Dump of table asbie
# ------------------------------------------------------------

DROP TABLE IF EXISTS `asbie`;

CREATE TABLE `asbie` (
  `asbie_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an ASBIE.',
  `guid` varchar(41) NOT NULL COMMENT 'A globally unique identifier (GUID) of an ASBIE. GUID of an ASBIE is different from its based ASCC. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `from_abie_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointing to the ABIE table. FROM_ABIE_ID is basically  a parent data element (type) of the TO_ASBIEP_ID. FROM_ABIE_ID must be based on the FROM_ACC_ID in the BASED_ASCC_ID except when the FROM_ACC_ID refers to an SEMANTIC_GROUP ACC or USER_EXTENSION_GROUP ACC.',
  `to_asbiep_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key to the ASBIEP table. TO_ASBIEP_ID is basically a child data element of the FROM_ABIE_ID. The TO_ASBIEP_ID must be based on the TO_ASCCP_ID in the BASED_ASCC_ID.',
  `based_ascc_id` bigint(20) unsigned NOT NULL COMMENT 'The BASED_ASCC_ID column refers to the ASCC record, which this ASBIE contextualizes.',
  `definition` text COMMENT 'Definition to override the ASCC definition. If NULL, it means that the definition should be derived from the based CC on the UI, expression generation, and any API.',
  `cardinality_min` int(11) NOT NULL COMMENT 'Minimum occurence constraint of the TO_ASBIEP_ID. A valid value is a non-negative integer.',
  `cardinality_max` int(11) NOT NULL COMMENT 'Maximum occurrence constraint of the TO_ASBIEP_ID. A valid value is an integer from -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.',
  `is_nillable` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicate whether the TO_ASBIEP_ID is allowed to be null.',
  `remark` varchar(225) DEFAULT NULL COMMENT 'This column allows the user to specify very context-specific usage of the BIE. It is different from the DEFINITION column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be "Type of BOM should be recognized in the BOM/typeCode."',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the ASBIE. The creator of the ASBIE is also its owner by default. ASBIEs created as children of another ABIE have the same CREATED_BY.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who has last updated the ASBIE record. ',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the ASBIE record was first created. ASBIEs created as children of another ABIE have the same CREATION_TIMESTAMP.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the ASBIE was last updated.',
  `seq_key` decimal(10,2) NOT NULL COMMENT 'This indicates the order of the associations among other siblings. The SEQ_KEY for BIEs is decimal in order to accomodate the removal of inheritance hierarchy and group. For example, children of the most abstract ACC will have SEQ_KEY = 1.1, 1.2, 1.3, and so on; and SEQ_KEY of the next abstraction level ACC will have SEQ_KEY = 2.1, 2.2, 2.3 and so on so forth.',
  `is_used` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Flag to indicate whether the field/component is used in the content model. It signifies whether the field/component should be generated.',
  `owner_top_level_abie_id` bigint(20) unsigned NOT NULL COMMENT 'This is a foriegn key to the ABIE table. It specifies the top-level ABIE which owns this ASBIE record.',
  PRIMARY KEY (`asbie_id`),
  KEY `asbie_based_ascc_id_fk` (`based_ascc_id`),
  KEY `asbie_created_by_fk` (`created_by`),
  KEY `asbie_from_abie_id_fk` (`from_abie_id`),
  KEY `asbie_last_updated_by_fk` (`last_updated_by`),
  KEY `asbie_owner_top_level_abie_id_fk` (`owner_top_level_abie_id`),
  KEY `asbie_to_asbiep_id_fk` (`to_asbiep_id`),
  CONSTRAINT `asbie_based_ascc_id_fk` FOREIGN KEY (`based_ascc_id`) REFERENCES `ascc` (`ascc_id`),
  CONSTRAINT `asbie_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asbie_from_abie_id_fk` FOREIGN KEY (`from_abie_id`) REFERENCES `abie` (`abie_id`),
  CONSTRAINT `asbie_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asbie_owner_top_level_abie_id_fk` FOREIGN KEY (`owner_top_level_abie_id`) REFERENCES `top_level_abie` (`top_level_abie_id`),
  CONSTRAINT `asbie_to_asbiep_id_fk` FOREIGN KEY (`to_asbiep_id`) REFERENCES `asbiep` (`asbiep_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='An ASBIE represents a relationship/association between two ABIEs through an ASBIEP. It is a contextualization of an ASCC.';



# Dump of table asbiep
# ------------------------------------------------------------

DROP TABLE IF EXISTS `asbiep`;

CREATE TABLE `asbiep` (
  `asbiep_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an ASBIEP.',
  `guid` varchar(41) NOT NULL COMMENT 'A globally unique identifier (GUID) of an ASBIEP. GUID of an ASBIEP is different from its based ASCCP. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `based_asccp_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointing to the ASCCP record. It is the ASCCP, on which the ASBIEP contextualizes.',
  `role_of_abie_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointing to the ABIE record. It is the ABIE, which the property term in the based ASCCP qualifies. Note that the ABIE has to be derived from the ACC used by the based ASCCP.',
  `definition` text COMMENT 'A definition to override the ASCCP''s definition. If NULL, it means that the definition should be derived from the based ASCCP on the UI, expression generation, and any API.',
  `remark` varchar(225) DEFAULT NULL COMMENT 'This column allows the user to specify a context-specific usage of the BIE. It is different from the DEFINITION column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ASBIEP can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ASBIEP. A remark about that ASBIEP may be "Type of BOM should be recognized in the BOM/typeCode."',
  `biz_term` varchar(225) DEFAULT NULL COMMENT 'This column represents a business term to indicate what the BIE is called in a particular business context. With this current design, only one business term is allowed per business context.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the ASBIEP. The creator of the ASBIEP is also its owner by default. ASBIEPs created as children of another ABIE have the same CREATED_BY.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the ASBIEP record. ',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the ASBIEP record was first created. ASBIEPs created as children of another ABIE have the same CREATION_TIMESTAMP.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the ASBIEP was last updated.',
  `owner_top_level_abie_id` bigint(20) unsigned NOT NULL COMMENT 'This is a foriegn key to the ABIE table. It specifies the top-level ABIE, which owns this ASBIEP record.',
  PRIMARY KEY (`asbiep_id`),
  KEY `asbiep_based_asccp_id_fk` (`based_asccp_id`),
  KEY `asbiep_role_of_abie_id_fk` (`role_of_abie_id`),
  KEY `asbiep_created_by_fk` (`created_by`),
  KEY `asbiep_last_updated_by_fk` (`last_updated_by`),
  KEY `asbiep_owner_top_level_abie_id_fk` (`owner_top_level_abie_id`),
  CONSTRAINT `asbiep_based_asccp_id_fk` FOREIGN KEY (`based_asccp_id`) REFERENCES `asccp` (`asccp_id`),
  CONSTRAINT `asbiep_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asbiep_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asbiep_owner_top_level_abie_id_fk` FOREIGN KEY (`owner_top_level_abie_id`) REFERENCES `top_level_abie` (`top_level_abie_id`),
  CONSTRAINT `asbiep_role_of_abie_id_fk` FOREIGN KEY (`role_of_abie_id`) REFERENCES `abie` (`abie_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='ASBIEP represents a role in a usage of an ABIE. It is a contextualization of an ASCCP.';



# Dump of table ascc
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ascc`;

CREATE TABLE `ascc` (
  `ascc_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of an ASCC.',
  `guid` varchar(41) NOT NULL COMMENT 'A globally unique identifier (GUID) of an ASCC. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `cardinality_min` int(11) NOT NULL COMMENT 'Minimum occurrence of the TO_ASCCP_ID. The valid values are non-negative integer.',
  `cardinality_max` int(11) NOT NULL COMMENT 'Maximum cardinality of the TO_ASCCP_ID. A valid value is integer -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.',
  `seq_key` int(11) NOT NULL COMMENT 'This indicates the order of the associations among other siblings. A valid value is positive integer. The SEQ_KEY at the CC side is localized. In other words, if an ACC is based on another ACC, SEQ_KEY of ASCCs or BCCs of the former ACC starts at 1 again. ',
  `from_acc_id` bigint(20) unsigned NOT NULL COMMENT 'FROM_ACC_ID is a foreign key pointing to an ACC record. It is basically pointing to a parent data element (type) of the TO_ASCCP_ID.',
  `to_asccp_id` bigint(20) unsigned NOT NULL COMMENT 'TO_ASCCP_ID is a foreign key to an ASCCP table record. It is basically pointing to a child data element of the FROM_ACC_ID. ',
  `den` varchar(200) NOT NULL COMMENT 'DEN (dictionary entry name) of the ASCC. This column can be derived from Qualifier and OBJECT_CLASS_TERM of the FROM_ACC_ID and DEN of the TO_ASCCP_ID as Qualifier + "_ " + OBJECT_CLASS_TERM + ". " + DEN. ',
  `definition` text COMMENT 'This is a documentation or description of the ASCC. Since ASCC is business context independent, this is a business context independent description of the ASCC. Since there are definitions also in the ASCCP (as referenced by the TO_ASCCP_ID column) and the ACC under that ASCCP, definition in the ASCC is a specific description about the relationship between the ACC (as in FROM_ACC_ID) and the ASCCP.',
  `definition_source` varchar(100) DEFAULT NULL COMMENT 'This is typically a URL identifying the source of the DEFINITION column.',
  `is_deprecated` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key to the APP_USER table referring to the user who creates the entity.\n\nThis column never change between the history and the current record for a given revision. The history record should have the same value as that of its current record.',
  `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership. ',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key to the APP_USER table referring to the last user who has updated the record. \n\nIn the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the revision of the ASCC was created. \n\nThis never change for a revision.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the change has occurred.',
  `state` int(11) NOT NULL COMMENT '1 = EDITING, 2 = CANDIDATE, 3 = PUBLISHED. This is the revision life cycle state of the entity.\n\nState change can''t be undone. But the history record can still keep the records of when the state was changed.',
  `revision_num` int(11) NOT NULL DEFAULT '0' COMMENT 'REVISION_NUM is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).',
  `revision_tracking_num` int(11) NOT NULL DEFAULT '0' COMMENT 'REVISION_TRACKING_NUM supports the ability to undo changes during a revision (life cycle of a revision is from the component''s EDITING state to PUBLISHED state). Once the component has transitioned into the PUBLISHED state for its particular revision, all revision tracking records are deleted except the latest one. REVISION_TRACKING_NUM can be 0, 1, 2, and so on. The zero value is assign to the record with REVISION_NUM = 0 as a default.',
  `revision_action` tinyint(4) DEFAULT '1' COMMENT 'This indicates the action associated with the record. The action can be 1 = INSERT, 2 = UPDATE, and 3 = DELETE. This column is null for the current record.',
  `release_id` bigint(20) unsigned DEFAULT NULL COMMENT 'RELEASE_ID is an incremental integer. It is an unformatted counterpart of the RELEASE_NUMBER in the RELEASE table. RELEASE_ID can be 1, 2, 3, and so on. RELEASE_ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the REVISION_ACTION column).\n\nNot all component revisions have an associated RELEASE_ID because some revisions may never be released.\n\nUnpublished components cannot be released.\n\nThis column is NULL for the current record.',
  `current_ascc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the the record whose REVISION_NUM is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.\n\nIt is noted that although this is a foreign key by definition, we don''t specify a foreign key in the data model. This is because when an entity is deleted the current record won''t exist anymore.\n\nThe value of this column for the current record should be left NULL.',
  PRIMARY KEY (`ascc_id`),
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='An ASCC represents a relationship/association between two ACCs through an ASCCP. ';



# Dump of table asccp
# ------------------------------------------------------------

DROP TABLE IF EXISTS `asccp`;

CREATE TABLE `asccp` (
  `asccp_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of an ASCCP.',
  `guid` varchar(41) NOT NULL COMMENT 'A globally unique identifier (GUID) of an ASCCP. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `property_term` varchar(60) DEFAULT NULL COMMENT 'The role (or property) the ACC as referred to by the Role_Of_ACC_ID play when the ASCCP is used by another ACC. \\n\\nThere must be only one ASCCP without a Property_Term for a particular ACC.',
  `definition` text COMMENT 'Description of the ASCCP.',
  `definition_source` varchar(100) DEFAULT NULL COMMENT 'This is typically a URL identifying the source of the DEFINITION column.',
  `role_of_acc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'The ACC from which this ASCCP is created (ASCCP applies role to the ACC).',
  `den` varchar(200) DEFAULT NULL COMMENT 'The dictionary entry name of the ASCCP.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the user who creates the entity. \n\nThis column never change between the history and the current record for a given revision. The history record should have the same value as that of its current record.',
  `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership. ',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who has updated the record. \n\nIn the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the revision of the ASCCP was created. \n\nThis never change for a revision.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.',
  `state` int(11) NOT NULL COMMENT '1 = EDITING, 2 = CANDIDATE, 3 = PUBLISHED. This the revision life cycle state of the ACC.\n\nState change can''t be undone. But the history record can still keep the records of when the state was changed.',
  `module_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This column stores the name of the physical schema module the ASCCP belongs to. Right now the schema file name is assigned. In the future, this needs to be updated to a file path from the base of the release directory.',
  `namespace_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the Namespace table. This is the namespace to which the entity belongs. This namespace column is primarily used in the case the component is a user''s component because there is also a namespace assigned at the release level.',
  `reusable_indicator` tinyint(1) DEFAULT '1' COMMENT 'This indicates whether the ASCCP can be used by more than one ASCC. This maps directly to the XML schema local element declaration.',
  `is_deprecated` tinyint(1) NOT NULL COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  `revision_num` int(11) NOT NULL DEFAULT '0' COMMENT 'REVISION_NUM is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).',
  `revision_tracking_num` int(11) NOT NULL DEFAULT '0' COMMENT 'REVISION_TRACKING_NUM supports the ability to undo changes during a revision (life cycle of a revision is from the component''s EDITING state to PUBLISHED state). Once the component has transitioned into the PUBLISHED state for its particular revision, all revision tracking records are deleted except the latest one. REVISION_TRACKING_NUMB can be 0, 1, 2, and so on. The zero value is assigned to the record with REVISION_NUM = 0 as a default.',
  `revision_action` tinyint(4) DEFAULT '1' COMMENT 'This indicates the action associated with the record. The action can be 1 = INSERT, 2 = UPDATE, and 3 = DELETE. This column is null for the current record.',
  `release_id` bigint(20) unsigned DEFAULT NULL COMMENT 'RELEASE_ID is an incremental integer. It is an unformatted counter part of the RELEASE_NUMBER in the RELEASE table. RELEASE_ID can be 1, 2, 3, and so on. A release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the REVISION_ACTION column).\n\nNot all component revisions have an associated RELEASE_ID because some revisions may never be released. USER_EXTENSION_GROUP component type is never part of a release.\n\nUnpublished components cannot be released.\n\nThis column is NULLl for the current record.',
  `current_asccp_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the the record whose REVISION_NUM is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.\n\nIt is noted that although this is a foreign key by definition, we don''t specify a foreign key in the data model. This is because when an entity is deleted the current record won''t exist anymore.\n\nThe value of this column for the current record should be left NULL.',
  `is_nillable` tinyint(1) DEFAULT NULL COMMENT 'This is corresponding to the XML schema nillable flag. Although the nillable may not apply in certain cases of the ASCCP (e.g., when it corresponds to an XSD group), the value is default to false for simplification.',
  PRIMARY KEY (`asccp_id`),
  KEY `asccp_role_of_acc_id_fk` (`role_of_acc_id`),
  KEY `asccp_created_by_fk` (`created_by`),
  KEY `asccp_owner_user_id_fk` (`owner_user_id`),
  KEY `asccp_last_updated_by_fk` (`last_updated_by`),
  KEY `asccp_module_id_fk` (`module_id`),
  KEY `asccp_namespace_id_fk` (`namespace_id`),
  KEY `asccp_release_id_fk` (`release_id`),
  KEY `asccp_current_asccp_id_fk` (`current_asccp_id`),
  CONSTRAINT `asccp_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asccp_current_asccp_id_fk` FOREIGN KEY (`current_asccp_id`) REFERENCES `asccp` (`asccp_id`),
  CONSTRAINT `asccp_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asccp_module_id_fk` FOREIGN KEY (`module_id`) REFERENCES `module` (`module_id`),
  CONSTRAINT `asccp_namespace_id_fk` FOREIGN KEY (`namespace_id`) REFERENCES `namespace` (`namespace_id`),
  CONSTRAINT `asccp_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asccp_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
  CONSTRAINT `asccp_role_of_acc_id_fk` FOREIGN KEY (`role_of_acc_id`) REFERENCES `acc` (`acc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='An ASCCP specifies a role (or property) an ACC may play under another ACC.';



# Dump of table bbie
# ------------------------------------------------------------

DROP TABLE IF EXISTS `bbie`;

CREATE TABLE `bbie` (
  `bbie_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of a BBIE.',
  `guid` varchar(41) NOT NULL COMMENT 'A globally unique identifier (GUID) of an SC. GUID of a BBIE''s SC  is different from the one in the DT_SC. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `based_bcc_id` bigint(20) unsigned NOT NULL COMMENT 'The BASED_BCC_ID column refers to the BCC record, which this BBIE contextualizes.',
  `from_abie_id` bigint(20) unsigned NOT NULL COMMENT 'FROM_ABIE_ID must be based on the FROM_ACC_ID in the BASED_BCC_ID.',
  `to_bbiep_id` bigint(20) unsigned NOT NULL COMMENT 'TO_BBIEP_ID is a foreign key to the BBIEP table. TO_BBIEP_ID basically refers to a child data element of the FROM_ABIE_ID. TO_BBIEP_ID must be based on the TO_BCCP_ID in the based BCC.',
  `bdt_pri_restri_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is the foreign key to the BDT_PRI_RESTRI table. It indicates the primitive assigned to the BBIE (or also can be viewed as assigned to the BBIEP for this specific association). This is assigned by the user who authors the BIE. The assignment would override the default from the CC side.',
  `code_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the CODE_LIST table. If a code list is assigned to the BBIE (or also can be viewed as assigned to the BBIEP for this association), then this column stores the assigned code list. It should be noted that one of the possible primitives assignable to the BDT_PRI_RESTRI_ID column may also be a code list. So this column is typically used when the user wants to assign another code list different from the one permissible by the CC model.',
  `agency_id_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the AGENCY_ID_LIST table. It is used in the case that the BDT content can be restricted to an agency identification.',
  `cardinality_min` int(11) NOT NULL COMMENT 'The minimum occurrence constraint for the BBIE. A valid value is a non-negative integer.',
  `cardinality_max` int(11) DEFAULT NULL COMMENT 'Maximum occurence constraint of the TO_BBIEP_ID. A valid value is an integer from -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.',
  `default_value` text COMMENT 'This column specifies the default value constraint. Default and fixed value constraints cannot be used at the same time.',
  `is_nillable` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicate whether the field can have a null  This is corresponding to the nillable flag in the XML schema.',
  `fixed_value` text COMMENT 'This column captures the fixed value constraint. Default and fixed value constraints cannot be used at the same time.',
  `is_null` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This column indicates whether the field is fixed to NULL. IS_NULLl can be true only if the IS_NILLABLE is true. If IS_NULL is true then the FIX_VALUE and DEFAULT_VALUE columns cannot have a value.',
  `definition` text COMMENT 'Description to override the BCC definition. If NULLl, it means that the definition should be inherited from the based BCC.',
  `remark` varchar(225) DEFAULT NULL COMMENT 'This column allows the user to specify very context-specific usage of the BIE. It is different from the DEFINITION column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be "Type of BOM should be recognized in the BOM/typeCode."',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the BBIE. The creator of the BBIE is also its owner by default. BBIEs created as children of another ABIE have the same CREATED_BY.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who has last updated the ASBIE record. ',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the BBIE record was first created. BBIEs created as children of another ABIE have the same CREATION_TIMESTAMP.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the ASBIE was last updated.',
  `seq_key` decimal(10,2) DEFAULT NULL COMMENT 'This indicates the order of the associations among other siblings. The SEQ_KEY for BIEs is decimal in order to accomodate the removal of inheritance hierarchy and group. For example, children of the most abstract ACC will have SEQ_KEY = 1.1, 1.2, 1.3, and so on; and SEQ_KEY of the next abstraction level ACC will have SEQ_KEY = 2.1, 2.2, 2.3 and so on so forth.',
  `is_used` tinyint(1) DEFAULT '0' COMMENT 'Flag to indicate whether the field/component is used in the content model. It indicates whether the field/component should be generated in the expression generation.',
  `owner_top_level_abie_id` bigint(20) unsigned NOT NULL COMMENT 'This is a foriegn key to the ABIE table. It specifies the top-level ABIE, which owns this BBIE record.',
  PRIMARY KEY (`bbie_id`),
  KEY `bbie_based_bcc_id_fk` (`based_bcc_id`),
  KEY `bbie_from_abie_id_fk` (`from_abie_id`),
  KEY `bbie_to_bbiep_id_fk` (`to_bbiep_id`),
  KEY `bbie_bdt_pri_restri_id_fk` (`bdt_pri_restri_id`),
  KEY `bbie_code_list_id_fk` (`code_list_id`),
  KEY `bbie_agency_id_list_id_fk` (`agency_id_list_id`),
  KEY `bbie_created_by_fk` (`created_by`),
  KEY `bbie_last_updated_by_fk` (`last_updated_by`),
  KEY `bbie_owner_top_level_abie_id_fk` (`owner_top_level_abie_id`),
  CONSTRAINT `bbie_agency_id_list_id_fk` FOREIGN KEY (`agency_id_list_id`) REFERENCES `agency_id_list` (`agency_id_list_id`),
  CONSTRAINT `bbie_based_bcc_id_fk` FOREIGN KEY (`based_bcc_id`) REFERENCES `bcc` (`bcc_id`),
  CONSTRAINT `bbie_bdt_pri_restri_id_fk` FOREIGN KEY (`bdt_pri_restri_id`) REFERENCES `bdt_pri_restri` (`bdt_pri_restri_id`),
  CONSTRAINT `bbie_code_list_id_fk` FOREIGN KEY (`code_list_id`) REFERENCES `code_list` (`code_list_id`),
  CONSTRAINT `bbie_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bbie_from_abie_id_fk` FOREIGN KEY (`from_abie_id`) REFERENCES `abie` (`abie_id`),
  CONSTRAINT `bbie_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bbie_owner_top_level_abie_id_fk` FOREIGN KEY (`owner_top_level_abie_id`) REFERENCES `top_level_abie` (`top_level_abie_id`),
  CONSTRAINT `bbie_to_bbiep_id_fk` FOREIGN KEY (`to_bbiep_id`) REFERENCES `bbiep` (`bbiep_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='A BBIE represents a relationship/association between an ABIE and a BBIEP. It is a contextualization of a BCC. The BBIE table also stores some information about the specific constraints related to the BDT associated with the BBIEP. In particular, the three columns including the BDT_PRI_RESTRI_ID, CODE_LIST_ID, and AGENCY_ID_LIST_ID allows for capturing of the specific primitive to be used in the context. Only one column among the three can have a value in a particular record.';



# Dump of table bbie_sc
# ------------------------------------------------------------

DROP TABLE IF EXISTS `bbie_sc`;

CREATE TABLE `bbie_sc` (
  `bbie_sc_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of a BBIE_SC.',
  `guid` varchar(41) NOT NULL COMMENT 'A globally unique identifier (GUID). It is different from the GUID fo the SC on the CC side. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `bbie_id` bigint(20) unsigned NOT NULL COMMENT 'The BBIE this BBIE_SC applies to.',
  `dt_sc_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the DT_SC table. This should correspond to the DT_SC of the BDT of the based BCC and BCCP.',
  `dt_sc_pri_restri_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This must be one of the allowed primitive/code list as specified in the corresponding SC of the based BCC of the BBIE (referred to by the BBIE_ID column).\n\nIt is the foreign key to the BDT_SC_PRI_RESTRI table. It indicates the primitive assigned to the BBIE (or also can be viewed as assigned to the BBIEP for this specific association). This is assigned by the user who authors the BIE. The assignment would override the default from the CC side.\n\nThis column, the CODE_LIST_ID column, and AGENCY_ID_LIST_ID column cannot have a value at the same time.',
  `code_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the CODE_LIST table. If a code list is assigned to the BBIE SC (or also can be viewed as assigned to the BBIEP SC for this association), then this column stores the assigned code list. It should be noted that one of the possible primitives assignable to the DT_SC_PRI_RESTRI_ID column may also be a code list. So this column is typically used when the user wants to assign another code list different from the one permissible by the CC model.\n\nThis column is, the DT_SC_PRI_RESTRI_ID column, and AGENCY_ID_LIST_ID column cannot have a value at the same time.',
  `agency_id_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the AGENCY_ID_LIST table. If a agency ID list is assigned to the BBIE SC (or also can be viewed as assigned to the BBIEP SC for this association), then this column stores the assigned Agency ID list. It should be noted that one of the possible primitives assignable to the DT_SC_PRI_RESTRI_ID column may also be an Agency ID list. So this column is typically used only when the user wants to assign another Agency ID list different from the one permissible by the CC model.\n\nThis column, the DT_SC_PRI_RESTRI_ID column, and CODE_LIST_ID column cannot have a value at the same time.',
  `cardinality_min` int(11) NOT NULL COMMENT 'The minimum occurrence constraint for the BBIE SC. A valid value is 0 or 1.',
  `cardinality_max` int(11) NOT NULL COMMENT 'Maximum occurence constraint of the BBIE SC. A valid value is 0 or 1.',
  `default_value` text COMMENT 'This column specifies the default value constraint. Default and fixed value constraints cannot be used at the same time.',
  `fixed_value` text COMMENT 'This column captures the fixed value constraint. Default and fixed value constraints cannot be used at the same time.',
  `definition` text COMMENT 'Description to override the BDT SC definition. If NULL, it means that the definition should be inherited from the based BDT SC.',
  `remark` varchar(225) DEFAULT NULL COMMENT 'This column allows the user to specify a very context-specific usage of the BBIE SC. It is different from the Definition column in that the Definition column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. ',
  `biz_term` varchar(225) DEFAULT NULL COMMENT 'Business term to indicate what the BBIE SC is called in a particular business context. With this current design, only one business term is allowed per business context.',
  `is_used` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Flag to indicate whether the field/component is used in the content model. It indicates whether the field/component should be generated.',
  `owner_top_level_abie_id` bigint(20) unsigned NOT NULL COMMENT 'This is a foriegn key to the ABIE. It specifies the top-level ABIE, which owns this BBIE_SC record.',
  PRIMARY KEY (`bbie_sc_id`),
  KEY `bbie_sc_bbie_id_fk` (`bbie_id`),
  KEY `bbie_sc_dt_sc_id_fk` (`dt_sc_id`),
  KEY `bbie_sc_dt_sc_pri_restri_id_fk` (`dt_sc_pri_restri_id`),
  KEY `bbie_sc_code_list_id_fk` (`code_list_id`),
  KEY `bbie_sc_agency_id_list_id_fk` (`agency_id_list_id`),
  KEY `bbie_sc_owner_top_level_abie_id_fk` (`owner_top_level_abie_id`),
  CONSTRAINT `bbie_sc_agency_id_list_id_fk` FOREIGN KEY (`agency_id_list_id`) REFERENCES `agency_id_list` (`agency_id_list_id`),
  CONSTRAINT `bbie_sc_bbie_id_fk` FOREIGN KEY (`bbie_id`) REFERENCES `bbie` (`bbie_id`),
  CONSTRAINT `bbie_sc_code_list_id_fk` FOREIGN KEY (`code_list_id`) REFERENCES `code_list` (`code_list_id`),
  CONSTRAINT `bbie_sc_dt_sc_id_fk` FOREIGN KEY (`dt_sc_id`) REFERENCES `dt_sc` (`dt_sc_id`),
  CONSTRAINT `bbie_sc_dt_sc_pri_restri_id_fk` FOREIGN KEY (`dt_sc_pri_restri_id`) REFERENCES `bdt_sc_pri_restri` (`bdt_sc_pri_restri_id`),
  CONSTRAINT `bbie_sc_owner_top_level_abie_id_fk` FOREIGN KEY (`owner_top_level_abie_id`) REFERENCES `top_level_abie` (`top_level_abie_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Because there is no single table that is a contextualized counterpart of the DT table (which stores both CDT and BDT), The context specific constraints associated with the DT are stored in the BBIE table, while this table stores the constraints associated with the DT''s SCs. ';



# Dump of table bbiep
# ------------------------------------------------------------

DROP TABLE IF EXISTS `bbiep`;

CREATE TABLE `bbiep` (
  `bbiep_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an BBIEP.',
  `guid` varchar(41) NOT NULL COMMENT 'A globally unique identifier (GUID) of an BBIEP. GUID of an BBIEP is different from its based BCCP. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `based_bccp_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointing to the BCCP record. It is the BCCP, which the BBIEP contextualizes.',
  `definition` text COMMENT 'Definition to override the BCCP''s Definition. If NULLl, it means that the definition should be inherited from the based CC.',
  `remark` varchar(225) DEFAULT NULL COMMENT 'This column allows the user to specify very context-specific usage of the BIE. It is different from the Definition column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be "Type of BOM should be recognized in the BOM/typeCode.',
  `biz_term` varchar(225) DEFAULT NULL COMMENT 'Business term to indicate what the BIE is called in a particular business context such as in an industry.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the BBIEP. The creator of the BBIEP is also its owner by default. BBIEPs created as children of another ABIE have the same CREATED_BY'',',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the BBIEP record. ',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the BBIEP record was first created. BBIEPs created as children of another ABIE have the same CREATION_TIMESTAMP,',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the BBIEP was last updated.',
  `owner_top_level_abie_id` bigint(20) unsigned NOT NULL COMMENT 'This is a foriegn key to the ABIE table. It specifies the top-level ABIE which owns this BBIEP record.',
  PRIMARY KEY (`bbiep_id`),
  KEY `bbiep_based_bccp_id_fk` (`based_bccp_id`),
  KEY `bbiep_created_by_fk` (`created_by`),
  KEY `bbiep_last_updated_by_fk` (`last_updated_by`),
  KEY `bbiep_owner_top_level_abie_id_fk` (`owner_top_level_abie_id`),
  CONSTRAINT `bbiep_based_bccp_id_fk` FOREIGN KEY (`based_bccp_id`) REFERENCES `bccp` (`bccp_id`),
  CONSTRAINT `bbiep_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bbiep_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bbiep_owner_top_level_abie_id_fk` FOREIGN KEY (`owner_top_level_abie_id`) REFERENCES `top_level_abie` (`top_level_abie_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='BBIEP represents the usage of basic property in a specific business context. It is a contextualization of a BCCP.';



# Dump of table bcc
# ------------------------------------------------------------

DROP TABLE IF EXISTS `bcc`;

CREATE TABLE `bcc` (
  `bcc_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an BCC.',
  `guid` varchar(41) NOT NULL COMMENT 'A globally unique identifier (GUID) of BCC. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.'',',
  `cardinality_min` int(11) NOT NULL COMMENT 'Minimum cardinality of the TO_BCCP_ID. The valid values are non-negative integer.',
  `cardinality_max` int(11) DEFAULT NULL COMMENT 'Maximum cardinality of the TO_BCCP_ID. The valid values are integer -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.'',',
  `to_bccp_id` bigint(20) unsigned NOT NULL COMMENT 'TO_BCCP_ID is a foreign key to an BCCP table record. It is basically pointing to a child data element of the FROM_ACC_ID. \n\nNote that for the BCC history records, this column always points to the BCCP_ID of the current record of a BCCP.'',',
  `from_acc_id` bigint(20) unsigned NOT NULL COMMENT 'FROM_ACC_ID is a foreign key pointing to an ACC record. It is basically pointing to a parent data element (type) of the TO_BCCP_ID. \n\nNote that for the BCC history records, this column always points to the ACC_ID of the current record of an ACC.',
  `seq_key` int(11) DEFAULT NULL COMMENT 'This indicates the order of the associations among other siblings. A valid value is positive integer. The SEQ_KEY at the CC side is localized. In other words, if an ACC is based on another ACC, SEQ_KEY of ASCCs or BCCs of the former ACC starts at 1 again. ',
  `entity_type` int(11) DEFAULT NULL COMMENT 'This is a code list: 0 = ATTRIBUTE and 1 = ELEMENT. An expression generator may or may not use this information. This column is necessary because some of the BCCs are xsd:attribute and some are xsd:element in the OAGIS 10.x. ',
  `den` varchar(200) NOT NULL COMMENT 'DEN (dictionary entry name) of the BCC. This column can be derived from QUALIFIER and OBJECT_CLASS_TERM of the FROM_ACC_ID and DEN of the TO_BCCP_ID as QUALIFIER + "_ " + OBJECT_CLASS_TERM + ". " + DEN. ',
  `definition` text COMMENT 'This is a documentation or description of the BCC. Since BCC is business context independent, this is a business context independent description of the BCC. Since there are definitions also in the BCCP (as referenced by TO_BCCP_ID column) and the BDT under that BCCP, the definition in the BCC is a specific description about the relationship between the ACC (as in FROM_ACC_ID) and the BCCP.',
  `definition_source` varchar(100) DEFAULT NULL COMMENT 'This is typically a URL identifying the source of the DEFINITION column.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the user who creates the entity.\n\nThis column never change between the history and the current record. The history record should have the same value as that of its current record.',
  `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who has updated the record. \n\nIn the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the revision of the BCC was created. \n\nThis never change for a revision.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the change has occurred.',
  `state` int(11) NOT NULL COMMENT '1 = EDITING, 2 = CANDIDATE, 3 = PUBLISHED. This is the revision life cycle state of the entity.\n\nState change can''t be undone. But the history record can still keep the records of when the state was changed.',
  `revision_num` int(11) NOT NULL DEFAULT '0' COMMENT 'REVISION_NUM is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).',
  `revision_tracking_num` int(11) NOT NULL DEFAULT '0' COMMENT 'REVISION_TRACKING_NUM supports the ability to undo changes during a revision (life cycle of a revision is from the component''s EDITING state to PUBLISHED state). Once the component has transitioned into the PUBLISHED state for its particular revision, all revision tracking records are deleted except the latest one. REVISION_TRACKING_NUM can be 0, 1, 2, and so on. The zero value is assign to the record with REVISION_NUM = 0 as a default.',
  `revision_action` tinyint(1) DEFAULT '1' COMMENT 'This indicates the action associated with the record. The action can be 1 = INSERT, 2 = UPDATE, and 3 = DELETE. This column is null for the current record.',
  `release_id` bigint(20) unsigned DEFAULT NULL COMMENT 'RELEASE_ID is an incremental integer. It is an unformatted counterpart of the RELEASE_NUMBER in the RELEASE table. RELEASE_ID can be 1, 2, 3, and so on. RELEASE_ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the REVISION_ACTION column).\n\nNot all component revisions have an associated RELEASE_ID because some revisions may never be released.\n\nUnpublished components cannot be released.\n\nThis column is NULLl for the current record.',
  `current_bcc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the record whose REVISION_NUM is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.\n\nIt is noted that although this is a foreign key by definition, we don''t specify a foreign key in the data model. This is because when an entity is deleted the current record won''t exist anymore.\n\nThe value of this column for the current record should be left NULL.',
  `is_deprecated` tinyint(1) NOT NULL COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  `is_nillable` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicate whether the field can have a NULL This is corresponding to the nillable flag in the XML schema.',
  `default_value` text COMMENT 'This set the default value at the association level. ',
  PRIMARY KEY (`bcc_id`),
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='A BCC represents a relationship/association between an ACC and a BCCP. It creates a data element for an ACC. ';



# Dump of table bccp
# ------------------------------------------------------------

DROP TABLE IF EXISTS `bccp`;

CREATE TABLE `bccp` (
  `bccp_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key.',
  `guid` varchar(41) NOT NULL COMMENT 'A globally unique identifier (GUID). Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.'',',
  `property_term` varchar(60) NOT NULL COMMENT 'The property concept that the BCCP models. ',
  `representation_term` varchar(20) NOT NULL COMMENT 'The representation term convey the format of the data the BCCP can take. The value is derived from the DT.DATA_TYPE_TERM of the associated BDT as referred to by the BDT_ID column.',
  `bdt_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key pointing to the DT table indicating the data typye or data format of the BCCP. Only DT_ID which DT_Type is BDT can be used.',
  `den` varchar(200) NOT NULL COMMENT 'The dictionary entry name of the BCCP. It is derived by PROPERTY_TERM + ". " + REPRESENTATION_TERM.',
  `definition` text COMMENT 'Description of the BCCP.',
  `definition_source` varchar(100) DEFAULT NULL COMMENT 'This is typically a URL identifying the source of the DEFINITION column.',
  `module_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the module table indicating physical schema module the BCCP belongs to.',
  `namespace_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the NAMESPACE table. This is the namespace to which the entity belongs. This namespace column is primarily used in the case the component is a user''s component because there is also a namespace assigned at the release level.',
  `is_deprecated` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the user who creates the entity. \n\nThis column never change between the history and the current record for a given revision. The history record should have the same value as that of its current record.',
  `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who has updated the record. \n\nIn the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the revision of the BCCP was created. \n\nThis never change for a revision.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.',
  `state` int(11) NOT NULL COMMENT '1 = EDITING, 2 = CANDIDATE, 3 = PUBLISHED. This the revision life cycle state of the ACC.\n\nState change can''t be undone. But the history record can still keep the records of when the state was changed.',
  `revision_num` int(11) NOT NULL DEFAULT '0' COMMENT 'REVISION_NUM is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).',
  `revision_tracking_num` int(11) NOT NULL DEFAULT '0' COMMENT 'REVISION_TRACKING_NUM supports the ability to undo changes during a revision (life cycle of a revision is from the component''s EDITING state to PUBLISHED state). Once the component has transitioned into the PUBLISHED state for its particular revision, all revision tracking records are deleted except the latest one. REVISION_TRACKING_NUMB can be 0, 1, 2, and so on. The zero value is assigned to the record with REVISION_NUM = 0 as a default.',
  `revision_action` int(11) DEFAULT '1' COMMENT 'This indicates the action associated with the record. The action can be 1 = INSERT, 2 = UPDATE, and 3 = DELETE. This column is null for the current record.',
  `release_id` bigint(20) unsigned DEFAULT NULL COMMENT 'RELEASE_ID is an incremental integer. It is an unformatted counter part of the RELEASE_NUMBER in the RELEASE table. RELEASE_ID can be 1, 2, 3, and so on. A release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the REVISION_ACTION column).\n\nNot all component revisions have an associated RELEASE_ID because some revisions may never be released. USER_EXTENSION_GROUP component type is never part of a release.\n\nUnpublished components cannot be released.\n\nThis column is NULLl for the current record.',
  `current_bccp_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the the record whose REVISION_NUM is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.\n\nIt is noted that although this is a foreign key by definition, we don''t specify a foreign key in the data model. This is because when an entity is deleted the current record won''t exist anymore.\n\nThe value of this column for the current record should be left NULL.',
  `is_nillable` tinyint(1) NOT NULL COMMENT 'This is corresponding to the XML Schema nillable flag. Although the nillable may not apply to certain cases of the BCCP (e.g., when it is only used as XSD attribute), the value is default to false for simplification. ',
  `default_value` text COMMENT 'This column specifies the default value constraint. Default and fixed value constraints cannot be used at the same time.',
  PRIMARY KEY (`bccp_id`),
  KEY `bccp_bdt_id_fk` (`bdt_id`),
  KEY `bccp_module_id_fk` (`module_id`),
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
  CONSTRAINT `bccp_module_id_fk` FOREIGN KEY (`module_id`) REFERENCES `module` (`module_id`),
  CONSTRAINT `bccp_namespace_id_fk` FOREIGN KEY (`namespace_id`) REFERENCES `namespace` (`namespace_id`),
  CONSTRAINT `bccp_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bccp_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='An BCCP specifies a property concept and data type associated with it. A BCCP can be then added as a property of an ACC.';



# Dump of table bdt_pri_restri
# ------------------------------------------------------------

DROP TABLE IF EXISTS `bdt_pri_restri`;

CREATE TABLE `bdt_pri_restri` (
  `bdt_pri_restri_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `bdt_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the DT table. It shall point to only DT that is a BDT (not a CDT).',
  `cdt_awd_pri_xps_type_map_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the CDT_AWD_PRI_XPS_TYPE_MAP table.  It allows for a primitive restriction based on a built-in type of schema expressions.',
  `code_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the CODE_LIST table.',
  `agency_id_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the AGENCY_ID_LIST table. It is used in the case that the BDT content can be restricted to an agency identification.',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This allows overriding the default primitive assigned in the CDT_AWD_PRI_XPS_TYPE_MAP table. It typically indicates the most generic primtive for the data type.',
  PRIMARY KEY (`bdt_pri_restri_id`),
  KEY `bdt_pri_restri_bdt_id_fk` (`bdt_id`),
  KEY `bdt_pri_restri_cdt_awd_pri_xps_type_map_id_fk` (`cdt_awd_pri_xps_type_map_id`),
  KEY `bdt_pri_restri_code_list_id_fk` (`code_list_id`),
  KEY `bdt_pri_restri_agency_id_list_id_fk` (`agency_id_list_id`),
  CONSTRAINT `bdt_pri_restri_agency_id_list_id_fk` FOREIGN KEY (`agency_id_list_id`) REFERENCES `agency_id_list` (`agency_id_list_id`),
  CONSTRAINT `bdt_pri_restri_bdt_id_fk` FOREIGN KEY (`bdt_id`) REFERENCES `dt` (`dt_id`),
  CONSTRAINT `bdt_pri_restri_cdt_awd_pri_xps_type_map_id_fk` FOREIGN KEY (`cdt_awd_pri_xps_type_map_id`) REFERENCES `cdt_awd_pri_xps_type_map` (`cdt_awd_pri_xps_type_map_id`),
  CONSTRAINT `bdt_pri_restri_code_list_id_fk` FOREIGN KEY (`code_list_id`) REFERENCES `code_list` (`code_list_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table captures the allowed primitives for a BDT. The allowed primitives are captured by three columns the CDT_AWD_PRI_XPS_TYPE_MAP_ID, CODE_LIST_ID, and AGENCY_ID_LIST_ID. The first column specifies the primitive by the built-in type of an expression language such as the XML Schema built-in type. The second specifies the primitive, which is a code list, while the last one specifies the primitive which is an agency identification list. Only one column among the three can have a value in a particular record.';



# Dump of table bdt_sc_pri_restri
# ------------------------------------------------------------

DROP TABLE IF EXISTS `bdt_sc_pri_restri`;

CREATE TABLE `bdt_sc_pri_restri` (
  `bdt_sc_pri_restri_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `bdt_sc_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the DT_SC table. This column should only refers to a DT_SC that belongs to a BDT (not CDT).',
  `cdt_sc_awd_pri_xps_type_map_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This column is a forieng key to the CDT_SC_AWD_PRI_XPS_TYPE_MAP table. It allows for a primitive restriction based on a built-in type of schema expressions.',
  `code_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to identify a code list. It allows for a primitive restriction based on a code list.',
  `agency_id_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to identify an agency identification list. It allows for a primitive restriction based on such list of values.',
  `is_default` tinyint(1) NOT NULL COMMENT 'This column specifies the default primitive for a BDT. It is typically the most generic primitive allowed for the BDT.',
  PRIMARY KEY (`bdt_sc_pri_restri_id`),
  KEY `bdt_sc_pri_restri_bdt_sc_id_fk` (`bdt_sc_id`),
  KEY `bdt_sc_pri_restri_cdt_sc_awd_pri_xps_type_map_id_fk` (`cdt_sc_awd_pri_xps_type_map_id`),
  KEY `bdt_sc_pri_restri_code_list_id_fk` (`code_list_id`),
  KEY `bdt_sc_pri_restri_agency_id_list_id_fk` (`agency_id_list_id`),
  CONSTRAINT `bdt_sc_pri_restri_agency_id_list_id_fk` FOREIGN KEY (`agency_id_list_id`) REFERENCES `agency_id_list` (`agency_id_list_id`),
  CONSTRAINT `bdt_sc_pri_restri_bdt_sc_id_fk` FOREIGN KEY (`bdt_sc_id`) REFERENCES `dt_sc` (`dt_sc_id`),
  CONSTRAINT `bdt_sc_pri_restri_cdt_sc_awd_pri_xps_type_map_id_fk` FOREIGN KEY (`cdt_sc_awd_pri_xps_type_map_id`) REFERENCES `cdt_sc_awd_pri_xps_type_map` (`cdt_sc_awd_pri_xps_type_map_id`),
  CONSTRAINT `bdt_sc_pri_restri_code_list_id_fk` FOREIGN KEY (`code_list_id`) REFERENCES `code_list` (`code_list_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table is similar to the BDT_PRI_RESTRI table but it is for the BDT SC. The allowed primitives are captured by three columns the CDT_SC_AWD_PRI_XPS_TYPE_MAP, CODE_LIST_ID, and AGENCY_ID_LIST_ID. The first column specifies the primitive by the built-in type of an expression language such as the XML Schema built-in type. The second specifies the primitive, which is a code list, while the last one specifies the primitive which is an agency identification list. Only one column among the three can have a value in a particular record.\n\nIt should be noted that the table does not store the fact about primitive restriction hierarchical relationships. In other words, if a BDT SC is derived from another BDT SC and the derivative BDT SC applies some primitive restrictions, that relationship will not be explicitly stored. The derivative BDT SC points directly to the CDT_AWD_PRI_XPS_TYPE_MAP key rather than the BDT_SC_PRI_RESTRI key.';



# Dump of table bie_usage_rule
# ------------------------------------------------------------

DROP TABLE IF EXISTS `bie_usage_rule`;

CREATE TABLE `bie_usage_rule` (
  `bie_usage_rule_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key of the table.',
  `assigned_usage_rule_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the USAGE_RULE table indicating the usage rule assigned to a BIE.',
  `target_abie_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the ABIE table indicating the ABIE, to which the usage rule is applied.',
  `target_asbie_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the ASBIE table indicating the ASBIE, to which the usage rule is applied.',
  `target_asbiep_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the ASBIEP table indicating the ASBIEP, to which the usage rule is applied.',
  `target_bbie_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the BBIE table indicating the BBIE, to which the usage rule is applied.',
  `target_bbiep_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the BBIEP table indicating the ABIEP, to which the usage rule is applied.',
  PRIMARY KEY (`bie_usage_rule_id`),
  KEY `bie_usage_rule_assigned_usage_rule_id_fk` (`assigned_usage_rule_id`),
  KEY `bie_usage_rule_target_abie_id_fk` (`target_abie_id`),
  KEY `bie_usage_rule_target_asbie_id_fk` (`target_asbie_id`),
  KEY `bie_usage_rule_target_asbiep_id_fk` (`target_asbiep_id`),
  KEY `bie_usage_rule_target_bbie_id_fk` (`target_bbie_id`),
  KEY `bie_usage_rule_target_bbiep_id_fk` (`target_bbiep_id`),
  CONSTRAINT `bie_usage_rule_assigned_usage_rule_id_fk` FOREIGN KEY (`assigned_usage_rule_id`) REFERENCES `usage_rule` (`usage_rule_id`),
  CONSTRAINT `bie_usage_rule_target_abie_id_fk` FOREIGN KEY (`target_abie_id`) REFERENCES `abie` (`abie_id`),
  CONSTRAINT `bie_usage_rule_target_asbie_id_fk` FOREIGN KEY (`target_asbie_id`) REFERENCES `asbie` (`asbie_id`),
  CONSTRAINT `bie_usage_rule_target_asbiep_id_fk` FOREIGN KEY (`target_asbiep_id`) REFERENCES `asbiep` (`asbiep_id`),
  CONSTRAINT `bie_usage_rule_target_bbie_id_fk` FOREIGN KEY (`target_bbie_id`) REFERENCES `bbie` (`bbie_id`),
  CONSTRAINT `bie_usage_rule_target_bbiep_id_fk` FOREIGN KEY (`target_bbiep_id`) REFERENCES `bbiep` (`bbiep_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This is an intersection table. Per CCTS, a usage rule may be reused. This table allows m-m relationships between the usage rule and all kinds of BIEs. In a particular record, either only one of the TARGET_ABIE_ID, TARGET_ASBIE_ID, TARGET_ASBIEP_ID, TARGET_BBIE_ID, or TARGET_BBIEP_ID.';



# Dump of table bie_user_ext_revision
# ------------------------------------------------------------

DROP TABLE IF EXISTS `bie_user_ext_revision`;

CREATE TABLE `bie_user_ext_revision` (
  `bie_user_ext_revision_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `top_level_abie_id` bigint(20) unsigned NOT NULL COMMENT 'This is a foreign key pointing to an ABIE record which is a top-level ABIE. ',
  `ext_abie_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This points to an ABIE record corresponding to the EXTENSION_ACC_ID record. For example, this column can point to the ApplicationAreaExtension ABIE which is based on the ApplicationAreaExtension ACC (referred to by the EXT_ACC_ID column). This column can be NULL only when the extension is the AllExtension because there is no corresponding ABIE for the AllExtension ACC.',
  `ext_acc_id` bigint(20) unsigned NOT NULL COMMENT 'This points to an extension ACC on which the ABIE indicated by the EXT_ABIE_ID column is based. E.g. It may point to an ApplicationAreaExtension ACC, AllExtension ACC, ActualLedgerExtension ACC, etc. It should be noted that an ACC record pointed to must have the OAGIS_COMPONENT_TYPE = 2 (Extension).',
  `user_ext_acc_id` bigint(20) unsigned NOT NULL COMMENT 'This column points to the specific revision of a User Extension ACC (this is an ACC whose OAGIS_COMPONENT_TYPE = 4) currently used by the ABIE as indicated by the EXT_ABIE_ID or the by the TOP_LEVEL_ABIE_ID (in case of the AllExtension). ',
  `revised_indicator` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This column is a flag indicating to whether the User Extension ACC (as identified in the USER_EXT_ACC_ID column) has been revised, i.e., there is a newer version of the user extension ACC than the one currently used by the EXT_ABIE_ID. 0 means the USER_EXT_ACC_ID is current, 1 means it is not current.',
  PRIMARY KEY (`bie_user_ext_revision_id`),
  KEY `bie_user_ext_revision_top_level_abie_id_fk` (`top_level_abie_id`),
  KEY `bie_user_ext_revision_ext_abie_id_fk` (`ext_abie_id`),
  KEY `bie_user_ext_revision_ext_acc_id_fk` (`ext_acc_id`),
  KEY `bie_user_ext_revision_user_ext_acc_id_fk` (`user_ext_acc_id`),
  CONSTRAINT `bie_user_ext_revision_ext_abie_id_fk` FOREIGN KEY (`ext_abie_id`) REFERENCES `abie` (`abie_id`),
  CONSTRAINT `bie_user_ext_revision_ext_acc_id_fk` FOREIGN KEY (`ext_acc_id`) REFERENCES `acc` (`acc_id`),
  CONSTRAINT `bie_user_ext_revision_top_level_abie_id_fk` FOREIGN KEY (`top_level_abie_id`) REFERENCES `top_level_abie` (`top_level_abie_id`),
  CONSTRAINT `bie_user_ext_revision_user_ext_acc_id_fk` FOREIGN KEY (`user_ext_acc_id`) REFERENCES `acc` (`acc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table is a log of events. It keeps track of the User Extension ACC (the specific revision) used by an Extension ABIE. This can be a named extension (such as ApplicationAreaExtension) or the AllExtension. The REVISED_INDICATOR flag is designed such that a revision of a User Extension can notify the user of a top-level ABIE by setting this flag to true. The TOP_LEVEL_ABIE_ID column makes it more efficient to when opening a top-level ABIE, the user can be notified of any new revision of the extension. A record in this table is created only when there is a user extension to the the OAGIS extension component/ACC.';



# Dump of table biz_ctx
# ------------------------------------------------------------

DROP TABLE IF EXISTS `biz_ctx`;

CREATE TABLE `biz_ctx` (
  `biz_ctx_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `guid` varchar(41) NOT NULL COMMENT 'A globally unique identifier (GUID). Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `name` varchar(100) DEFAULT NULL COMMENT 'Short, descriptive name of the business context.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the user who creates the entity. ',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table  referring to the last user who has updated the business context.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the business context record was first created. ',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the business context was last updated.',
  PRIMARY KEY (`biz_ctx_id`),
  UNIQUE KEY `biz_ctx_uk1` (`guid`),
  KEY `biz_ctx_created_by_fk` (`created_by`),
  KEY `biz_ctx_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `biz_ctx_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `biz_ctx_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table represents a business context. A business context is a combination of one or more business context values.';



# Dump of table biz_ctx_value
# ------------------------------------------------------------

DROP TABLE IF EXISTS `biz_ctx_value`;

CREATE TABLE `biz_ctx_value` (
  `biz_ctx_value_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `biz_ctx_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the biz_ctx table.',
  `ctx_scheme_value_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the CTX_SCHEME_VALUE table.',
  PRIMARY KEY (`biz_ctx_value_id`),
  KEY `biz_ctx_value_biz_ctx_id_fk` (`biz_ctx_id`),
  KEY `biz_ctx_value_ctx_scheme_value_id_fk` (`ctx_scheme_value_id`),
  CONSTRAINT `biz_ctx_value_biz_ctx_id_fk` FOREIGN KEY (`biz_ctx_id`) REFERENCES `biz_ctx` (`biz_ctx_id`),
  CONSTRAINT `biz_ctx_value_ctx_scheme_value_id_fk` FOREIGN KEY (`ctx_scheme_value_id`) REFERENCES `ctx_scheme_value` (`ctx_scheme_value_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table represents business context values for business contexts. It provides the associations between a business context and a context scheme value.';



# Dump of table blob_content
# ------------------------------------------------------------

DROP TABLE IF EXISTS `blob_content`;

CREATE TABLE `blob_content` (
  `blob_content_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `content` mediumblob NOT NULL COMMENT 'The Blob content of the schema file.',
  `release_id` bigint(20) unsigned NOT NULL COMMENT 'The release to which this file/content belongs/published.',
  `module_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the module table indicating the physical file the blob content should be output to when generating/serializing the content.',
  PRIMARY KEY (`blob_content_id`),
  KEY `blob_content_release_id_fk` (`release_id`),
  KEY `blob_content_module_id_fk` (`module_id`),
  CONSTRAINT `blob_content_module_id_fk` FOREIGN KEY (`module_id`) REFERENCES `module` (`module_id`),
  CONSTRAINT `blob_content_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table stores schemas whose content is only imported as a whole and is represented in Blob.';



# Dump of table cdt_awd_pri
# ------------------------------------------------------------

DROP TABLE IF EXISTS `cdt_awd_pri`;

CREATE TABLE `cdt_awd_pri` (
  `cdt_awd_pri_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `cdt_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key pointing to a CDT in the DT table.',
  `cdt_pri_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key from the CDT_PRI table. It indicates the primative allowed for the CDT identified in the CDT_ID column. ',
  `is_default` tinyint(1) NOT NULL COMMENT 'Indicating a default primitive for the CDT?s Content Component. True for a default primitive; False otherwise.',
  PRIMARY KEY (`cdt_awd_pri_id`),
  KEY `cdt_awd_pri_cdt_id_fk` (`cdt_id`),
  KEY `cdt_awd_pri_cdt_pri_id_fk` (`cdt_pri_id`),
  CONSTRAINT `cdt_awd_pri_cdt_id_fk` FOREIGN KEY (`cdt_id`) REFERENCES `dt` (`dt_id`),
  CONSTRAINT `cdt_awd_pri_cdt_pri_id_fk` FOREIGN KEY (`cdt_pri_id`) REFERENCES `cdt_pri` (`cdt_pri_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table capture allowed primitives of the CDT?s Content Component.  The information in this table is captured from the Allowed Primitive column in each of the CDT Content Component section/table in CCTS DTC3.';



# Dump of table cdt_awd_pri_xps_type_map
# ------------------------------------------------------------

DROP TABLE IF EXISTS `cdt_awd_pri_xps_type_map`;

CREATE TABLE `cdt_awd_pri_xps_type_map` (
  `cdt_awd_pri_xps_type_map_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
  `cdt_awd_pri_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the CDT_AWD_PRI table.',
  `xbt_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key and to the XBT table. It identifies the XML schema built-in types that can be mapped to the CDT primivite identified in the CDT_AWD_PRI_ID column. The CDT primitives are typically broad and hence it usually maps to more than one XML schema built-in types.',
  PRIMARY KEY (`cdt_awd_pri_xps_type_map_id`),
  KEY `cdt_awd_pri_xps_type_map_cdt_awd_pri_id_fk` (`cdt_awd_pri_id`),
  KEY `cdt_awd_pri_xps_type_map_xbt_id_fk` (`xbt_id`),
  CONSTRAINT `cdt_awd_pri_xps_type_map_cdt_awd_pri_id_fk` FOREIGN KEY (`cdt_awd_pri_id`) REFERENCES `cdt_awd_pri` (`cdt_awd_pri_id`),
  CONSTRAINT `cdt_awd_pri_xps_type_map_xbt_id_fk` FOREIGN KEY (`xbt_id`) REFERENCES `xbt` (`xbt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table allows for concrete mapping between the CDT Primitives and types in a particular expression such as XML Schema, JSON. At this point, it is not clear whether a separate table will be needed for each expression. The current table holds the map to XML Schema built-in types. \n\nFor each additional expression, a column similar to the XBT_ID column will need to be added to this table for mapping to data types in another expression.\n\nIf we use a separate table for each expression, then we need binding all the way to BDT (or even BBIE) for every new expression. That would be almost like just store a BDT file. But using a column may not work with all kinds of expressions, particulary if it does not map well to the XML schema data types. ';



# Dump of table cdt_pri
# ------------------------------------------------------------

DROP TABLE IF EXISTS `cdt_pri`;

CREATE TABLE `cdt_pri` (
  `cdt_pri_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
  `name` varchar(45) NOT NULL COMMENT 'Name of the CDT primitive per the CCTS datatype catalog, e.g., Decimal.',
  PRIMARY KEY (`cdt_pri_id`),
  UNIQUE KEY `cdt_pri_uk1` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table stores the CDT primitives.';



# Dump of table cdt_sc_awd_pri
# ------------------------------------------------------------

DROP TABLE IF EXISTS `cdt_sc_awd_pri`;

CREATE TABLE `cdt_sc_awd_pri` (
  `cdt_sc_awd_pri_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
  `cdt_sc_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key pointing to the supplementary component (SC).',
  `cdt_pri_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointing to the CDT_Pri table. It represents a CDT primitive allowed for the suppliement component identified in the CDT_SC_ID column.',
  `is_default` tinyint(1) NOT NULL COMMENT 'Indicating whether the primitive is the default primitive of the supplementary component.',
  PRIMARY KEY (`cdt_sc_awd_pri_id`),
  KEY `cdt_sc_awd_pri_cdt_sc_id_fk` (`cdt_sc_id`),
  KEY `cdt_sc_awd_pri_cdt_pri_id_fk` (`cdt_pri_id`),
  CONSTRAINT `cdt_sc_awd_pri_cdt_pri_id_fk` FOREIGN KEY (`cdt_pri_id`) REFERENCES `cdt_pri` (`cdt_pri_id`),
  CONSTRAINT `cdt_sc_awd_pri_cdt_sc_id_fk` FOREIGN KEY (`cdt_sc_id`) REFERENCES `dt_sc` (`dt_sc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table capture the CDT primitives allowed for a particular SC of a CDT. It also stores the CDT primitives allowed for a SC of a BDT that extends its base (such SC is not defined in the CCTS data type catalog specification).';



# Dump of table cdt_sc_awd_pri_xps_type_map
# ------------------------------------------------------------

DROP TABLE IF EXISTS `cdt_sc_awd_pri_xps_type_map`;

CREATE TABLE `cdt_sc_awd_pri_xps_type_map` (
  `cdt_sc_awd_pri_xps_type_map_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
  `cdt_sc_awd_pri_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the CDT_SC_AWD_PRI table.',
  `xbt_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the Xbt table. It identifies an XML schema built-in type that maps to the CDT SC Allowed Primitive identified in the CDT_SC_AWD_PRI column.',
  PRIMARY KEY (`cdt_sc_awd_pri_xps_type_map_id`),
  KEY `cdt_sc_awd_pri_xps_type_map_cdt_sc_awd_pri_id_fk` (`cdt_sc_awd_pri_id`),
  KEY `cdt_sc_awd_pri_xps_type_map_xbt_id_fk` (`xbt_id`),
  CONSTRAINT `cdt_sc_awd_pri_xps_type_map_cdt_sc_awd_pri_id_fk` FOREIGN KEY (`cdt_sc_awd_pri_id`) REFERENCES `cdt_sc_awd_pri` (`cdt_sc_awd_pri_id`),
  CONSTRAINT `cdt_sc_awd_pri_xps_type_map_xbt_id_fk` FOREIGN KEY (`xbt_id`) REFERENCES `xbt` (`xbt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The purpose of this table is the same as that of the CDT_AWD_PRI_XPS_TYPE_MAP, but it is for the supplementary component (SC). It allows for the concrete mapping between the CDT Primitives and types in a particular expression such as XML Schema, JSON. ';



# Dump of table client
# ------------------------------------------------------------

DROP TABLE IF EXISTS `client`;

CREATE TABLE `client` (
  `client_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `name` varchar(200) DEFAULT NULL COMMENT 'Pretty print name of the client.',
  PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table captures a client organization. It is used, for example, to indicate the customer, for which the BIE was generated.';



# Dump of table code_list
# ------------------------------------------------------------

DROP TABLE IF EXISTS `code_list`;

CREATE TABLE `code_list` (
  `code_list_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
  `guid` varchar(41) NOT NULL COMMENT 'GUID of the code list. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `enum_type_guid` varchar(41) DEFAULT NULL COMMENT 'In the OAGIS Model XML schema, a type, which keeps all the enumerated values, is  defined separately from the type that represents a code list. This only applies to some code lists. When that is the case, this column stores the GUID of that enumeration type.',
  `name` varchar(100) DEFAULT NULL COMMENT 'Name of the code list.',
  `list_id` varchar(100) NOT NULL COMMENT 'External identifier.',
  `agency_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the AGENCY_ID_LIST_VALUE table. It indicates the organization which maintains the code list.',
  `version_id` varchar(10) NOT NULL COMMENT 'Code list version number.',
  `definition` text COMMENT 'Description of the code list.',
  `remark` varchar(225) DEFAULT NULL COMMENT 'Usage information about the code list.',
  `definition_source` varchar(100) DEFAULT NULL COMMENT 'This is typically a URL which indicates the source of the code list''s DEFINITION.',
  `based_code_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the CODE_LIST table itself. This identifies the code list on which this code list is based, if any. The derivation may be restriction and/or extension.',
  `extensible_indicator` tinyint(1) NOT NULL COMMENT 'This is a flag to indicate whether the code list is final and shall not be further derived.',
  `module_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the module table indicating the physical file the code list belongs to when generating a physical model schema. ',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created the code list.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It identifies the user who last updated the code list.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the code list was created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the code list was last updated.',
  `state` varchar(10) DEFAULT NULL COMMENT 'Life cycle state of the code list. Possible values are Editing, Published, or Deleted. Only a code list in published state is available for derivation and for used by the CC and BIE. Once the code list is published, it cannot go back to Editing. A new version would have to be created.',
  PRIMARY KEY (`code_list_id`),
  UNIQUE KEY `code_list_uk1` (`guid`),
  UNIQUE KEY `code_list_uk2` (`enum_type_guid`),
  KEY `code_list_agency_id_fk` (`agency_id`),
  KEY `code_list_based_code_list_id_fk` (`based_code_list_id`),
  KEY `code_list_module_id_fk` (`module_id`),
  KEY `code_list_created_by_fk` (`created_by`),
  KEY `code_list_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `code_list_agency_id_fk` FOREIGN KEY (`agency_id`) REFERENCES `agency_id_list_value` (`agency_id_list_value_id`),
  CONSTRAINT `code_list_based_code_list_id_fk` FOREIGN KEY (`based_code_list_id`) REFERENCES `code_list` (`code_list_id`),
  CONSTRAINT `code_list_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `code_list_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `code_list_module_id_fk` FOREIGN KEY (`module_id`) REFERENCES `module` (`module_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table stores information about a code list. When a code list is derived from another code list, the whole set of code values belonging to the based code list will be copied.';



# Dump of table code_list_value
# ------------------------------------------------------------

DROP TABLE IF EXISTS `code_list_value`;

CREATE TABLE `code_list_value` (
  `code_list_value_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
  `code_list_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the CODE_LIST table. It indicates the code list this code value belonging to.',
  `value` tinytext NOT NULL COMMENT 'The code list value used in the instance data, e.g., EA, US-EN.',
  `name` varchar(100) DEFAULT NULL COMMENT 'Pretty print name of the code list value, e.g., ''Each'' for EA, ''English'' for EN.',
  `definition` text COMMENT 'Long description or explannation of the code list value, e.g., ''EA is a discrete quantity for counting each unit of an item, such as, 2 shampoo bottles, 3 box of cereals''.',
  `definition_source` varchar(100) DEFAULT NULL COMMENT 'This is typically a URL identifying the source of the DEFINITION column.',
  `used_indicator` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'This indicates whether the code value is allowed to be used or not in that code list context. In other words, this flag allows a user to enable or disable a code list value.',
  `locked_indicator` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This indicates whether the USED_INDICATOR can be changed from False to True. In other words, if the code value is derived from its base code list and the USED_INDICATOR of the code value in the base is False, then the USED_iNDICATOR cannot be changed from False to True for this code value; and this is indicated using this LOCKED_INDICATOR flag in the derived code list.',
  `extension_Indicator` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This indicates whether this code value has just been added in this code list. It is used particularly in the derived code list. If the code value has only been added to the derived code list, then it can be deleted; otherwise, it cannot be deleted.',
  PRIMARY KEY (`code_list_value_id`),
  KEY `code_list_value_code_list_id_fk` (`code_list_id`),
  CONSTRAINT `code_list_value_code_list_id_fk` FOREIGN KEY (`code_list_id`) REFERENCES `code_list` (`code_list_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Each record in this table stores a code list value of a code list. A code list value may be inherited from another code list on which it is based. However, inherited value may be restricted (i.e., disabled and cannot be used) in this code list, i.e., the USED_INDICATOR = false. If the value cannot be used since the based code list, then the LOCKED_INDICATOR = TRUE, because the USED_INDICATOR of such code list value is FALSE by default and can no longer be changed.';



# Dump of table ctx_category
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ctx_category`;

CREATE TABLE `ctx_category` (
  `ctx_category_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary, database key.',
  `guid` varchar(41) NOT NULL COMMENT 'GUID of the context category.  Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `name` varchar(45) DEFAULT NULL COMMENT 'Short name of the context category.',
  `description` text COMMENT 'Explanation of what the context category is.',
  PRIMARY KEY (`ctx_category_id`),
  UNIQUE KEY `ctx_category_uk1` (`guid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table captures the context category. Examples of context categories as described in the CCTS are business process, industry, etc.';



# Dump of table ctx_scheme
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ctx_scheme`;

CREATE TABLE `ctx_scheme` (
  `ctx_scheme_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary, database key.',
  `guid` varchar(41) NOT NULL COMMENT 'GUID of the classification scheme. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `scheme_id` varchar(45) NOT NULL COMMENT 'External identification of the scheme. ',
  `scheme_name` varchar(255) DEFAULT NULL COMMENT 'Pretty print name of the context scheme.',
  `description` text COMMENT 'Description of the context scheme.',
  `code_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the CODE_LIST table. If a code list is assigned to the CTX_SCHEME, then this column stores the assigned code list.',
  `scheme_agency_id` varchar(45) NOT NULL COMMENT 'Identification of the agency maintaining the scheme. This column currently does not use the AGENCY_ID_LIST table. It is just a free form text at this point.',
  `scheme_version_id` varchar(45) NOT NULL COMMENT 'Version number of the context scheme.',
  `ctx_category_id` bigint(20) unsigned NOT NULL COMMENT 'This the foreign key to the CTX_CATEGORY table. It identifies the context category associated with this context scheme.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this context scheme.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It identifies the user who last updated the context scheme.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the scheme was created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the scheme was last updated.',
  PRIMARY KEY (`ctx_scheme_id`),
  UNIQUE KEY `ctx_scheme_uk1` (`guid`),
  KEY `ctx_scheme_ctx_category_id_fk` (`ctx_category_id`),
  KEY `ctx_scheme_created_by_fk` (`created_by`),
  KEY `ctx_scheme_last_updated_by_fk` (`last_updated_by`),
  KEY `ctx_scheme_code_list_id_fk` (`code_list_id`),
  CONSTRAINT `ctx_scheme_code_list_id_fk` FOREIGN KEY (`code_list_id`) REFERENCES `code_list` (`code_list_id`),
  CONSTRAINT `ctx_scheme_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `ctx_scheme_ctx_category_id_fk` FOREIGN KEY (`ctx_category_id`) REFERENCES `ctx_category` (`ctx_category_id`),
  CONSTRAINT `ctx_scheme_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table represents a context scheme (a classification scheme) for a context category.';




# Dump of table ctx_scheme_value
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ctx_scheme_value`;

CREATE TABLE `ctx_scheme_value` (
  `ctx_scheme_value_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `guid` varchar(41) NOT NULL COMMENT 'GUID of the context scheme value. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `value` varchar(45) NOT NULL COMMENT 'A short value for the scheme value similar to the code list value.',
  `meaning` text COMMENT 'The description, explanatiion of the scheme value.',
  `owner_ctx_scheme_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the CTX_SCHEME table. It identifies the context scheme, to which this scheme value belongs.',
  PRIMARY KEY (`ctx_scheme_value_id`),
  UNIQUE KEY `ctx_scheme_value_uk1` (`guid`),
  KEY `ctx_scheme_value_owner_ctx_scheme_id_fk` (`owner_ctx_scheme_id`),
  CONSTRAINT `ctx_scheme_value_owner_ctx_scheme_id_fk` FOREIGN KEY (`owner_ctx_scheme_id`) REFERENCES `ctx_scheme` (`ctx_scheme_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table stores the context scheme values for a particular context scheme in the CTX_SCHEME table.';



# Dump of table dt
# ------------------------------------------------------------

DROP TABLE IF EXISTS `dt`;

CREATE TABLE `dt` (
  `dt_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
  `guid` varchar(41) NOT NULL COMMENT 'GUID of the data type. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.',
  `type` int(11) DEFAULT NULL COMMENT 'List value: 0 = CDT, 1 = BDT.',
  `version_num` varchar(45) NOT NULL COMMENT 'Format X.Y.Z where all of them are integer with no leading zero allowed. X means major version number, Y means minor version number and Z means patch version number. This column is different from the REVISION_NUM column in that the new version is only assigned to the release component while the REVISION_NUM is assigned every time editing life cycle.',
  `previous_version_dt_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foregin key to the DT table itself. It identifies the previous version.',
  `data_type_term` varchar(45) DEFAULT NULL COMMENT 'This is the data type term assigned to the DT. The allowed set of data type terms are defined in the DTC specification. This column is derived from the Based_DT_ID when the column is not blank. ',
  `qualifier` varchar(100) DEFAULT NULL COMMENT 'This column shall be blank when the DT_TYPE is CDT. When the DT_TYPE is BDT, this is optional. If the column is not blank it is a qualified BDT. If blank then the row may be a default BDT or an unqualified BDT. Default BDT is OAGIS concrete implementation of the CDT, these are the DT with numbers in the name, e.g., CodeType_1E7368 (DEN is ''Code_1E7368. Type''). Default BDTs are almost like permutation of the CDT options into concrete data types. Unqualified BDT is a BDT that OAGIS model schema generally used for its canonical. A handful of default BDTs were selected; and each of them is wrapped with another type definition that has a simpler name such as CodeType and NormalizedString type - we call these "unqualified BDTs". ',
  `based_dt_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key pointing to the DT table itself. This column must be blank when the DT_TYPE is CDT. This column must not be blank when the DT_TYPE is BDT.',
  `den` varchar(200) NOT NULL COMMENT 'Dictionary Entry Name of the data type. ',
  `content_component_den` varchar(200) DEFAULT NULL COMMENT 'When the DT_TYPE is CDT this column is automatically derived from DATA_TYPE_TERM as "<DATA_TYPE_TYPE>. Content", where ''Content'' is called property term of the content component according to CCTS. When the DT_TYPE is BDT this column has the same value as its BASED_DT_ID.',
  `definition` text COMMENT 'Description of the data type.',
  `definition_source` varchar(200) DEFAULT NULL COMMENT 'This is typically a URL identifying the source of the DEFINITION column.',
  `content_component_definition` text COMMENT 'Description of the content component of the data type.',
  `revision_doc` text COMMENT 'This is for documenting about the revision, e.g., how the newer version of the DT is different from the previous version.',
  `state` int(11) DEFAULT NULL COMMENT '1 = EDITING, 2 = CANDIDATE, 3 = PUBLISHED. This the revision life cycle state of the entity.\\n\\nState change can''t be undone. But the history record can still keep the records of when the state was changed.',
  `module_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the MODULE table indicating physical file where the DT shall belong to when it is generated in an expression. ',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this DT.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record. \n\nIn the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).',
  `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\\n\\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership. ',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the revision of the DT was created. \n\nThis never change for a revision.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.',
  `revision_num` int(11) NOT NULL DEFAULT '0' COMMENT 'REVISION_NUM is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).',
  `revision_tracking_num` int(11) NOT NULL DEFAULT '0' COMMENT 'REVISION_TRACKING_NUM supports the ability to undo changes during a revision (life cycle of a revision is from the component''s EDITING state to PUBLISHED state). Once the component has transitioned into the PUBLISHED state for its particular revision, all revision tracking records are deleted except the latest one. REVISION_TRACKING_NUM can be 0, 1, 2, and so on. The zero value is assign to the record with REVISION_NUM = 0 as a default.',
  `revision_action` tinyint(4) DEFAULT '1' COMMENT 'This indicates the action associated with the record. The action can be 1 = INSERT, 2 = UPDATE, and 3 = DELETE. This column is null for the current record.',
  `release_id` bigint(20) unsigned DEFAULT NULL COMMENT 'RELEASE_ID is an incremental integer. It is an unformatted counter part of the RELEASE_NUMBER in the RELEASE table. RELEASE_ID can be 1, 2, 3, and so on. A release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the REVISION_ACTION column).\n\nNot all component revisions have an associated RELEASE_ID because some revisions may never be released. USER_EXTENSION_GROUP component type is never part of a release.\n\nUnpublished components cannot be released.\n\nThis column is NULL for the current record.',
  `current_bdt_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the record whose REVISION_NUM is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.\n\nIt is noted that although this is a foreign key by definition, we don''t specify a foreign key in the data model. This is because when an entity is deleted the current record won''t exist anymore.\n\nThe value of this column for the current record should be left NULL.\n\nThe column name is specific to BDT because, the column does not apply to CDT.',
  `is_deprecated` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  PRIMARY KEY (`dt_id`),
  UNIQUE KEY `dt_uk1` (`guid`),
  KEY `dt_previous_version_dt_id_fk` (`previous_version_dt_id`),
  KEY `dt_based_dt_id_fk` (`based_dt_id`),
  KEY `dt_module_id_fk` (`module_id`),
  KEY `dt_created_by_fk` (`created_by`),
  KEY `dt_last_updated_by_fk` (`last_updated_by`),
  KEY `dt_owner_user_id_fk` (`owner_user_id`),
  KEY `dt_release_id_fk` (`release_id`),
  KEY `dt_current_bdt_id_fk` (`current_bdt_id`),
  CONSTRAINT `dt_based_dt_id_fk` FOREIGN KEY (`based_dt_id`) REFERENCES `dt` (`dt_id`),
  CONSTRAINT `dt_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `dt_current_bdt_id_fk` FOREIGN KEY (`current_bdt_id`) REFERENCES `dt` (`dt_id`),
  CONSTRAINT `dt_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `dt_module_id_fk` FOREIGN KEY (`module_id`) REFERENCES `module` (`module_id`),
  CONSTRAINT `dt_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `dt_previous_version_dt_id_fk` FOREIGN KEY (`previous_version_dt_id`) REFERENCES `dt` (`dt_id`),
  CONSTRAINT `dt_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The DT table stores both CDT and BDT. The two types of DTs are differentiated by the TYPE column.';



# Dump of table dt_sc
# ------------------------------------------------------------

DROP TABLE IF EXISTS `dt_sc`;

CREATE TABLE `dt_sc` (
  `dt_sc_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
  `guid` varchar(41) NOT NULL COMMENT 'A globally unique identifier (GUID) of an SC. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence. Note that each SC is considered intrinsic to each DT, so a SC has a different GUID from the based SC, i.e., SC inherited from the based DT has a new, different GUID.',
  `property_term` varchar(60) DEFAULT NULL COMMENT 'Property term of the SC.',
  `representation_term` varchar(20) DEFAULT NULL COMMENT 'Representation of the supplementary component.',
  `definition` text COMMENT 'Description of the supplementary component.',
  `definition_source` varchar(200) DEFAULT NULL COMMENT 'This is typically a URL identifying the source of the DEFINITION column.',
  `owner_dt_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreigned key to the DT table indicating the data type, to which this supplementary component belongs.',
  `cardinality_min` int(11) NOT NULL DEFAULT '0' COMMENT 'The minimum occurrence constraint associated with the supplementary component. The valid values zero or one.',
  `cardinality_max` int(11) DEFAULT NULL COMMENT 'The maximum occurrence constraint associated with the supplementary component. The valid values are zero or one. Zero is used when the SC is restricted from an instantiation in the data type.',
  `based_dt_sc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the DT_SC table itself. This column is used when the SC is derived from the based DT.',
  PRIMARY KEY (`dt_sc_id`),
  UNIQUE KEY `dt_sc_uk1` (`guid`),
  KEY `dt_sc_owner_dt_id_fk` (`owner_dt_id`),
  KEY `dt_sc_based_dt_sc_id_fk` (`based_dt_sc_id`),
  CONSTRAINT `dt_sc_based_dt_sc_id_fk` FOREIGN KEY (`based_dt_sc_id`) REFERENCES `dt_sc` (`dt_sc_id`),
  CONSTRAINT `dt_sc_owner_dt_id_fk` FOREIGN KEY (`owner_dt_id`) REFERENCES `dt` (`dt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table represents the supplementary component (SC) of a DT. Revision is not tracked at the supplementary component. It is considered intrinsic part of the DT. In other words, when a new revision of a DT is created a new set of supplementary components is created along with it. ';



# Dump of table dt_usage_rule
# ------------------------------------------------------------

DROP TABLE IF EXISTS `dt_usage_rule`;

CREATE TABLE `dt_usage_rule` (
  `dt_usage_rule_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key of the table.',
  `assigned_usage_rule_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the USAGE_RULE table indicating the usage rule assigned to the DT content component or DT_SC.',
  `target_dt_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreing key to the DT_ID for assigning a usage rule to the corresponding DT content component.',
  `target_dt_sc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreing key to the DT_SC_ID for assigning a usage rule to the corresponding DT_SC.',
  PRIMARY KEY (`dt_usage_rule_id`),
  KEY `dt_usage_rule_assigned_usage_rule_id_fk` (`assigned_usage_rule_id`),
  KEY `dt_usage_rule_target_dt_id_fk` (`target_dt_id`),
  KEY `dt_usage_rule_target_dt_sc_id_fk` (`target_dt_sc_id`),
  CONSTRAINT `dt_usage_rule_assigned_usage_rule_id_fk` FOREIGN KEY (`assigned_usage_rule_id`) REFERENCES `usage_rule` (`usage_rule_id`),
  CONSTRAINT `dt_usage_rule_target_dt_id_fk` FOREIGN KEY (`target_dt_id`) REFERENCES `dt` (`dt_id`),
  CONSTRAINT `dt_usage_rule_target_dt_sc_id_fk` FOREIGN KEY (`target_dt_sc_id`) REFERENCES `dt_sc` (`dt_sc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This is an intersection table. Per CCTS, a usage rule may be reused. This table allows m-m relationships between the usage rule and the DT content component and usage rules and DT supplementary component. In a particular record, either a TARGET_DT_ID or TARGET_DT_SC_ID must be present but not both.';



# Dump of table module
# ------------------------------------------------------------

DROP TABLE IF EXISTS `module`;

CREATE TABLE `module` (
  `module_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `module` varchar(100) NOT NULL COMMENT 'The is the subdirectory and filename. The format is Windows file path. The starting directory typically is the root folder of all the release content. For example, for OAGIS 10.1 Model, the root directory is Model. If the file shall be directly under the Model directory, then this column should be ''Model\\filename'' without the extension. If the file is under, say, Model\\Platform\\2_1\\Common\\Components directory, then the value of this column shall be ''Model\\Platform\\2_1\\Common\\Components\\filenam''. The reason to not including the extension is that the extension maybe dependent on the expression. For XML schema, ''.xsd'' maybe added; or for JSON, ''.json'' maybe added as the file extension.',
  `release_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table. It identifies the release, for which this module is associated.',
  `namespace_id` bigint(20) unsigned NOT NULL COMMENT 'Note that a release record has a namespace associated. The NAMESPACE_ID, if specified here, overrides the release''s namespace. However, the NAMESPACE_ID associated with the component takes the highest precedence.',
  `version_num` varchar(45) DEFAULT NULL COMMENT 'This is the version number to be assigned to the schema module.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this MODULE.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record. \n\nIn the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).',
  `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table identifying the user who can update or delete the record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.',
  PRIMARY KEY (`module_id`),
  KEY `module_release_id_fk` (`release_id`),
  KEY `module_namespace_id_fk` (`namespace_id`),
  KEY `module_owner_user_id_fk` (`owner_user_id`),
  KEY `module_created_by_fk` (`created_by`),
  KEY `module_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `module_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_namespace_id_fk` FOREIGN KEY (`namespace_id`) REFERENCES `namespace` (`namespace_id`),
  CONSTRAINT `module_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The module table stores information about a physical file, into which CC components will be generated during the expression generation.';



# Dump of table module_dep
# ------------------------------------------------------------

DROP TABLE IF EXISTS `module_dep`;

CREATE TABLE `module_dep` (
  `module_dep_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `dependency_type` int(11) NOT NULL COMMENT 'This is a code list. The value tells the expression generator what to do based on this dependency type. 0 = xsd:include, 1 = xsd:import. There could be other values supporting other expressions/syntaxes.',
  `depending_module_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the MODULE table. It identifies a depending module. For example, in XML schema if module A imports or includes module B, then module A is a depending module.',
  `depended_module_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the MODULE table. It identifies a depended module counterpart of the depending module. For example, in XML schema if module A imports or includes module B, then module B is a depended module.',
  PRIMARY KEY (`module_dep_id`),
  KEY `module_dep_depending_module_id_fk` (`depending_module_id`),
  KEY `module_dep_depended_module_id_fk` (`depended_module_id`),
  CONSTRAINT `module_dep_depended_module_id_fk` FOREIGN KEY (`depended_module_id`) REFERENCES `module` (`module_id`),
  CONSTRAINT `module_dep_depending_module_id_fk` FOREIGN KEY (`depending_module_id`) REFERENCES `module` (`module_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table carries the dependency between modules in the MODULE table.';



# Dump of table namespace
# ------------------------------------------------------------

DROP TABLE IF EXISTS `namespace`;

CREATE TABLE `namespace` (
  `namespace_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `uri` varchar(100) NOT NULL COMMENT 'This is the URI of the namespace.',
  `prefix` varchar(45) DEFAULT NULL COMMENT 'This is a default short name to represent the URI. It may be overridden during the expression generation. Null or empty means the same thing like the default prefix in an XML schema.',
  `description` text COMMENT 'Description or explanation about the namespace or use of the namespace.',
  `is_std_nmsp` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This indicates whether the namespace is reserved for standard used (i.e., whether it is an OAGIS namespace). If it is true, then end users cannot user the namespace for the end user CCs.',
  `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table identifying the user who can update or delete the record.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table identifying user who created the namespace.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table identifying the user who last updated the record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.',
  PRIMARY KEY (`namespace_id`),
  KEY `namespace_owner_user_id_fk` (`owner_user_id`),
  KEY `namespace_created_by_fk` (`created_by`),
  KEY `namespace_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `namespace_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `namespace_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `namespace_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table stores information about a namespace. Namespace is the namespace as in the XML schema specification.';



# Dump of table release
# ------------------------------------------------------------

DROP TABLE IF EXISTS `release`;

CREATE TABLE `release` (
  `release_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'RELEASE_ID must be an incremental integer. RELEASE_ID that is more than another RELEASE_ID is interpreted to be released later than the other.',
  `release_num` varchar(45) DEFAULT NULL COMMENT 'Release number such has 10.0, 10.1, etc. ',
  `release_note` longtext COMMENT 'Description or note associated with the release.',
  `namespace_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the NAMESPACE table. It identifies the namespace used with the release. It is particularly useful for a library that uses a single namespace such like the OAGIS 10.x. A library that uses multiple namespace but has a main namespace may also use this column as a specific namespace can be override at the module level.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table identifying user who created the namespace.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table identifying the user who last updated the record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.',
  `state` int(11) DEFAULT NULL COMMENT '1 = DRAFT, 2 = FINAL. This the revision life cycle state of the Release.',
  PRIMARY KEY (`release_id`),
  KEY `release_namespace_id_fk` (`namespace_id`),
  KEY `release_created_by_fk` (`created_by`),
  KEY `release_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `release_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `release_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `release_namespace_id_fk` FOREIGN KEY (`namespace_id`) REFERENCES `namespace` (`namespace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The is table store the release information.';



# Dump of table top_level_abie
# ------------------------------------------------------------

DROP TABLE IF EXISTS `top_level_abie`;

CREATE TABLE `top_level_abie` (
  `top_level_abie_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an ACC.',
  `abie_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the ABIE table pointing to a record which is a top-level ABIE.',
  `owner_user_id` bigint(20) unsigned NOT NULL,
  `release_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table. It identifies the release, for which this module is associated.',
  `state` int(11) DEFAULT NULL,
  PRIMARY KEY (`top_level_abie_id`),
  KEY `top_level_abie_abie_id_fk` (`abie_id`),
  KEY `top_level_abie_owner_user_id_fk` (`owner_user_id`),
  KEY `top_level_abie_release_id_fk` (`release_id`),
  CONSTRAINT `top_level_abie_abie_id_fk` FOREIGN KEY (`abie_id`) REFERENCES `abie` (`abie_id`),
  CONSTRAINT `top_level_abie_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `top_level_abie_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table indexes the ABIE which is a top-level ABIE. This table and the owner_top_level_abie_id column in all BIE tables allow all related BIEs to be retrieved all at once speeding up the profile BOD transactions.';



# Dump of table usage_rule
# ------------------------------------------------------------

DROP TABLE IF EXISTS `usage_rule`;

CREATE TABLE `usage_rule` (
  `usage_rule_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key of the usage rule.',
  `name` text COMMENT 'Short nmenomic name of the usage rule.',
  `condition_type` int(11) NOT NULL COMMENT 'Condition type according to the CC specification. It is a value list column. 0 = pre-condition, 1 = post-condition, 2 = invariant.',
  PRIMARY KEY (`usage_rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table captures a usage rule information. A usage rule may be expressed in multiple expressions. Each expression is captured in the USAGE_RULE_EXPRESSION table. To capture a description of a usage rule, create a usage rule expression with the unstructured constraint type.';



# Dump of table usage_rule_expression
# ------------------------------------------------------------

DROP TABLE IF EXISTS `usage_rule_expression`;

CREATE TABLE `usage_rule_expression` (
  `usage_rule_expression_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key of the usage rule expression',
  `constraint_type` int(11) NOT NULL COMMENT 'Constraint type according to the CC spec. It represents the expression language (syntax) used in the CONSTRAINT column. It is a value list column. 0 = ''Unstructured'' which is basically a description of the rule, 1 = ''Schematron''.',
  `constraint_text` text NOT NULL COMMENT 'This column capture the constraint expressing the usage rule. In other words, this is the expression.',
  `represented_usage_rule_id` bigint(20) unsigned NOT NULL COMMENT 'The usage rule which the expression represents',
  PRIMARY KEY (`usage_rule_expression_id`),
  KEY `usage_rule_expression_represented_usage_rule_id_fk` (`represented_usage_rule_id`),
  CONSTRAINT `usage_rule_expression_represented_usage_rule_id_fk` FOREIGN KEY (`represented_usage_rule_id`) REFERENCES `usage_rule` (`usage_rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The USAGE_RULE_EXPRESSION provides a representation of a usage rule in a particular syntax indicated by the CONSTRAINT_TYPE column. One of the syntaxes can be unstructured, which works a description of the usage rule.';



# Dump of table xbt
# ------------------------------------------------------------

DROP TABLE IF EXISTS `xbt`;

CREATE TABLE `xbt` (
  `xbt_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `name` varchar(45) DEFAULT NULL COMMENT 'Human understandable name of the built-in type.',
  `builtIn_type` varchar(45) DEFAULT NULL COMMENT 'Built-in type as it should appear in the XML schema including the namespace prefix. Namespace prefix for the XML schema namespace is assumed to be ''xsd'' and a default prefix for the OAGIS built-int type.',
  `jbt_draft05_map` varchar(500) DEFAULT NULL,
  `subtype_of_xbt_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the XBT table itself. It indicates a super type of this XSD built-in type.',
  `schema_definition` text,
  `module_id` bigint(20) unsigned DEFAULT NULL,
  `release_id` bigint(20) unsigned DEFAULT NULL,
  `revision_doc` text,
  `state` int(11) DEFAULT NULL,
  `created_by` bigint(20) unsigned NOT NULL,
  `owner_user_id` bigint(20) unsigned NOT NULL,
  `last_updated_by` bigint(20) unsigned NOT NULL,
  `creation_timestamp` datetime(6) NOT NULL,
  `last_update_timestamp` datetime(6) NOT NULL,
  `revision_num` int(11) NOT NULL DEFAULT '0',
  `revision_tracking_num` int(11) NOT NULL DEFAULT '0',
  `revision_action` tinyint(4) DEFAULT '1',
  `current_xbt_id` bigint(20) unsigned DEFAULT NULL,
  `is_deprecated` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`xbt_id`),
  KEY `xbt_subtype_of_xbt_id_fk` (`subtype_of_xbt_id`),
  KEY `xbt_module_id_fk` (`module_id`),
  KEY `xbt_release_id_fk` (`release_id`),
  KEY `xbt_created_by_fk` (`created_by`),
  KEY `xbt_last_updated_by_fk` (`last_updated_by`),
  KEY `xbt_owner_user_id_fk` (`owner_user_id`),
  KEY `xbt_current_xbt_id_fk` (`current_xbt_id`),
  CONSTRAINT `xbt_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `xbt_current_xbt_id_fk` FOREIGN KEY (`current_xbt_id`) REFERENCES `xbt` (`xbt_id`),
  CONSTRAINT `xbt_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `xbt_module_id_fk` FOREIGN KEY (`module_id`) REFERENCES `module` (`module_id`),
  CONSTRAINT `xbt_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `xbt_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
  CONSTRAINT `xbt_subtype_of_xbt_id_fk` FOREIGN KEY (`subtype_of_xbt_id`) REFERENCES `xbt` (`xbt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table stores XML schema built-in types and OAGIS built-in types. OAGIS built-in types are those types defined in the XMLSchemaBuiltinType and the XMLSchemaBuiltinType Patterns schemas.';




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
