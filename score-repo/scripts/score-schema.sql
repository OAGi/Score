-- ------------------------------------------------------
-- Server version	5.7.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `abie`
--

DROP TABLE IF EXISTS `abie`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `abie` (
  `abie_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an ABIE.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `based_acc_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key to the ACC_MANIFEST table refering to the ACC, on which the business context has been applied to derive this ABIE.',
  `path` text CHARACTER SET ascii,
  `hash_path` varchar(64) CHARACTER SET ascii NOT NULL COMMENT 'hash_path generated from the path of the component graph using hash function, so that it is unique in the graph.',
  `biz_ctx_id` bigint(20) unsigned DEFAULT NULL COMMENT '(Deprecated) A foreign key to the BIZ_CTX table. This column stores the business context assigned to the ABIE.',
  `definition` text COMMENT 'Definition to override the ACC''s definition. If NULL, it means that the definition should be inherited from the based CC.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the ABIE. The creator of the ABIE is also its owner by default. ABIEs created as children of another ABIE have the same CREATED_BY as its parent.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the ABIE record. This may be the user who is in the same group as the creator.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the ABIE record was first created. ABIEs created as children of another ABIE have the same CREATION_TIMESTAMP.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the ABIE was last updated.',
  `state` int(11) DEFAULT NULL COMMENT '2 = EDITING, 4 = PUBLISHED. This column is only used with a top-level ABIE, because that is the only entry point for editing. The state value indicates the visibility of the top-level ABIE to users other than the owner. In the user group environment, a logic can apply that other users in the group can see the top-level ABIE only when it is in the ''Published'' state.',
  `remark` varchar(225) DEFAULT NULL COMMENT 'This column allows the user to specify very context-specific usage of the BIE. It is different from the DEFINITION column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be "Type of BOM should be recognized in the BOM/typeCode."',
  `biz_term` varchar(225) DEFAULT NULL COMMENT 'To indicate what the BIE is called in a particular business context. With this current design, only one business term is allowed per business context.',
  `owner_top_level_asbiep_id` bigint(20) unsigned NOT NULL COMMENT 'This is a foreign key to the top-level ASBIEP.',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The ABIE table stores information about an ABIE, which is a contextualized ACC. The context is represented by the BUSINESS_CTX_ID column that refers to a business context. Each ABIE must have a business context and a based ACC.\n\nIt should be noted that, per design document, there is no corresponding ABIE created for an ACC which will not show up in the instance document such as ACCs of OAGIS_COMPONENT_TYPE "SEMANTIC_GROUP", "USER_EXTENSION_GROUP", etc.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `acc`
--

DROP TABLE IF EXISTS `acc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `acc` (
  `acc_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an ACC.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `type` varchar(32) DEFAULT 'Default' COMMENT 'The Type of the ACC. List: Default, Extension, AllExtension.',
  `object_class_term` varchar(100) NOT NULL COMMENT 'Object class name of the ACC concept. For OAGIS, this is generally name of a type with the "Type" truncated from the end. Per CCS the name is space separated. "ID" is expanded to "Identifier".',
  `den` varchar(200) NOT NULL COMMENT 'DEN (dictionary entry name) of the ACC. It can be derived as OBJECT_CLASS_QUALIFIER + "_ " + OBJECT_CLASS_TERM + ". Details".',
  `definition` text COMMENT 'This is a documentation or description of the ACC. Since ACC is business context independent, this is a business context independent description of the ACC concept.',
  `definition_source` varchar(100) DEFAULT NULL COMMENT 'This is typically a URL identifying the source of the DEFINITION column.',
  `based_acc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'BASED_ACC_ID is a foreign key to the ACC table itself. It represents the ACC that is qualified by this ACC. In general CCS sense, a qualification can be a content extension or restriction, but the current scope supports only extension.',
  `object_class_qualifier` varchar(100) DEFAULT NULL COMMENT 'This column stores the qualifier of an ACC, particularly when it has a based ACC. ',
  `oagis_component_type` int(11) DEFAULT NULL COMMENT 'The value can be 0 = BASE, 1 = SEMANTICS, 2 = EXTENSION, 3 = SEMANTIC_GROUP, 4 = USER_EXTENSION_GROUP, 5 = EMBEDDED. Generally, BASE is assigned when the OBJECT_CLASS_TERM contains "Base" at the end. EXTENSION is assigned with the OBJECT_CLASS_TERM contains "Extension" at the end. SEMANTIC_GROUP is assigned when an ACC is imported from an XSD Group. USER_EXTENSION_GROUP is a wrapper ACC (a virtual ACC) for segregating user''s extension content. EMBEDDED is used for an ACC whose content is not explicitly defined in the database, for example, the Any Structured Content ACC that corresponds to the xsd:any.  Other cases are assigned SEMANTICS. ',
  `namespace_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the NAMESPACE table. This is the namespace to which the entity belongs. This namespace column is primarily used in the case the component is a user''s component because there is also a namespace assigned at the release level.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the user who creates the entity.\\n\\nThis column never change between the history and the current record for a given revision. The history record should have the same value as that of its current record.',
  `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\\n\\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership. ',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record. \\n\\nIn the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the revision of the ACC was created. \\n\\nThis never change for a revision.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.\\n\\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.',
  `state` varchar(20) DEFAULT NULL COMMENT 'Deleted, WIP, Draft, QA, Candidate, Production, Release Draft, Published. This the revision life cycle state of the ACC.\n\nState change can''t be undone. But the history record can still keep the records of when the state was changed.',
  `is_deprecated` tinyint(1) DEFAULT '0' COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be allowed).',
  `replacement_acc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement if the record is deprecated.',
  `is_abstract` tinyint(1) DEFAULT '0' COMMENT 'This is the XML Schema abstract flag. Default is false. If it is true, the abstract flag will be set to true when generating a corresponding xsd:complexType. So although this flag may not apply to some ACCs such as those that are xsd:group. It is still have a false value.',
  `prev_acc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the previous history record.',
  `next_acc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the next history record.',
  PRIMARY KEY (`acc_id`),
  KEY `acc_based_acc_id_fk` (`based_acc_id`),
  KEY `acc_created_by_fk` (`created_by`),
  KEY `acc_last_updated_by_fk` (`last_updated_by`),
  KEY `acc_namespace_id_fk` (`namespace_id`),
  KEY `acc_owner_user_id_fk` (`owner_user_id`),
  KEY `acc_prev_acc_id_fk` (`prev_acc_id`),
  KEY `acc_next_acc_id_fk` (`next_acc_id`),
  KEY `acc_guid_idx` (`guid`),
  KEY `acc_last_update_timestamp_desc_idx` (`last_update_timestamp`),
  KEY `acc_replacement_acc_id_fk` (`replacement_acc_id`),
  CONSTRAINT `acc_based_acc_id_fk` FOREIGN KEY (`based_acc_id`) REFERENCES `acc` (`acc_id`),
  CONSTRAINT `acc_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `acc_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `acc_namespace_id_fk` FOREIGN KEY (`namespace_id`) REFERENCES `namespace` (`namespace_id`),
  CONSTRAINT `acc_next_acc_id_fk` FOREIGN KEY (`next_acc_id`) REFERENCES `acc` (`acc_id`),
  CONSTRAINT `acc_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `acc_prev_acc_id_fk` FOREIGN KEY (`prev_acc_id`) REFERENCES `acc` (`acc_id`),
  CONSTRAINT `acc_replacement_acc_id_fk` FOREIGN KEY (`replacement_acc_id`) REFERENCES `acc` (`acc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The ACC table holds information about complex data structured concepts. For example, OAGIS''s Components, Nouns, and BODs are captured in the ACC table.\n\nNote that only Extension is supported when deriving ACC from another ACC. (So if there is a restriction needed, maybe that concept should placed higher in the derivation hierarchy rather than lower.)\n\nIn OAGIS, all XSD extensions will be treated as a qualification of an ACC.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `acc_manifest`
--

DROP TABLE IF EXISTS `acc_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `acc_manifest` (
  `acc_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `release_id` bigint(20) unsigned NOT NULL,
  `acc_id` bigint(20) unsigned NOT NULL,
  `based_acc_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `conflict` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This indicates that there is a conflict between self and relationship.',
  `log_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A foreign key pointed to a log for the current record.',
  `replacement_acc_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement manifest if the record is deprecated.',
  `prev_acc_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `next_acc_manifest_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`acc_manifest_id`),
  KEY `acc_manifest_acc_id_fk` (`acc_id`),
  KEY `acc_manifest_based_acc_manifest_id_fk` (`based_acc_manifest_id`),
  KEY `acc_manifest_release_id_fk` (`release_id`),
  KEY `acc_manifest_log_id_fk` (`log_id`),
  KEY `acc_manifest_prev_acc_manifest_id_fk` (`prev_acc_manifest_id`),
  KEY `acc_manifest_next_acc_manifest_id_fk` (`next_acc_manifest_id`),
  KEY `acc_replacement_acc_manifest_id_fk` (`replacement_acc_manifest_id`),
  CONSTRAINT `acc_manifest_acc_id_fk` FOREIGN KEY (`acc_id`) REFERENCES `acc` (`acc_id`),
  CONSTRAINT `acc_manifest_based_acc_manifest_id_fk` FOREIGN KEY (`based_acc_manifest_id`) REFERENCES `acc_manifest` (`acc_manifest_id`),
  CONSTRAINT `acc_manifest_log_id_fk` FOREIGN KEY (`log_id`) REFERENCES `log` (`log_id`),
  CONSTRAINT `acc_manifest_next_acc_manifest_id_fk` FOREIGN KEY (`next_acc_manifest_id`) REFERENCES `acc_manifest` (`acc_manifest_id`),
  CONSTRAINT `acc_manifest_prev_acc_manifest_id_fk` FOREIGN KEY (`prev_acc_manifest_id`) REFERENCES `acc_manifest` (`acc_manifest_id`),
  CONSTRAINT `acc_manifest_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
  CONSTRAINT `acc_replacement_acc_manifest_id_fk` FOREIGN KEY (`replacement_acc_manifest_id`) REFERENCES `acc_manifest` (`acc_manifest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `acc_manifest_tag`
--

DROP TABLE IF EXISTS `acc_manifest_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `acc_manifest_tag` (
  `acc_manifest_id` bigint(20) unsigned NOT NULL,
  `tag_id` bigint(20) unsigned NOT NULL,
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the record was first created.',
  PRIMARY KEY (`acc_manifest_id`,`tag_id`),
  KEY `acc_manifest_tag_acc_manifest_id_fk` (`acc_manifest_id`),
  KEY `acc_manifest_tag_tag_id_fk` (`tag_id`),
  KEY `acc_manifest_tag_created_by_fk` (`created_by`),
  CONSTRAINT `acc_manifest_tag_acc_manifest_id_fk` FOREIGN KEY (`acc_manifest_id`) REFERENCES `acc_manifest` (`acc_manifest_id`),
  CONSTRAINT `acc_manifest_tag_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `acc_manifest_tag_tag_id_fk` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `agency_id_list`
--

DROP TABLE IF EXISTS `agency_id_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `agency_id_list` (
  `agency_id_list_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `enum_type_guid` varchar(41) NOT NULL COMMENT 'This column stores the GUID of the type containing the enumerated values. In OAGIS, most code lists and agnecy ID lists are defined by an XyzCodeContentType (or XyzAgencyIdentificationContentType) and XyzCodeEnumerationType (or XyzAgencyIdentificationEnumerationContentType). However, some don''t have the enumeration type. When that is the case, this column is null.',
  `name` varchar(100) DEFAULT NULL COMMENT 'Name of the agency identification list.',
  `list_id` varchar(100) DEFAULT NULL COMMENT 'This is a business or standard identification assigned to the agency identification list.',
  `agency_id_list_value_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is the identification of the agency or organization which developed and/or maintains the list. Theoretically, this can be modeled as a self-reference foreign key, but it is not implemented at this point.',
  `version_id` varchar(100) DEFAULT NULL COMMENT 'Version number of the agency identification list (assigned by the agency).',
  `based_agency_id_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the AGENCY_ID_LIST table itself. This identifies the agency id list on which this agency id list is based, if any. The derivation may be restriction and/or extension.',
  `definition` text COMMENT 'Description of the agency identification list.',
  `definition_source` varchar(100) DEFAULT NULL COMMENT 'This is typically a URL which indicates the source of the agency id list DEFINITION.',
  `remark` varchar(225) DEFAULT NULL COMMENT 'Usage information about the agency id list.',
  `namespace_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the NAMESPACE table. This is the namespace to which the entity belongs. This namespace column is primarily used in the case the component is a user''s component because there is also a namespace assigned at the release level.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created the agency ID list.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It identifies the user who last updated the agency ID list.',
  `creation_timestamp` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Timestamp when the agency ID list was created.',
  `last_update_timestamp` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Timestamp when the agency ID list was last updated.',
  `state` varchar(20) DEFAULT NULL COMMENT 'Life cycle state of the agency ID list. Possible values are Editing, Published, or Deleted. Only the agency ID list in published state is available for derivation and for used by the CC and BIE. Once the agency ID list is published, it cannot go back to Editing. A new version would have to be created.',
  `is_deprecated` tinyint(1) DEFAULT '0' COMMENT 'Indicates whether the agency id list is deprecated and should not be reused (i.e., no new reference to this record should be allowed).',
  `replacement_agency_id_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement if the record is deprecated.',
  `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership.',
  `prev_agency_id_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the previous history record.',
  `next_agency_id_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the next history record.',
  PRIMARY KEY (`agency_id_list_id`),
  KEY `agency_id_list_agency_id_list_value_id_fk` (`agency_id_list_value_id`),
  KEY `agency_id_list_created_by_fk` (`created_by`),
  KEY `agency_id_list_last_updated_by_fk` (`last_updated_by`),
  KEY `agency_id_list_based_agency_id_list_id_fk` (`based_agency_id_list_id`),
  KEY `agency_id_list_owner_user_id_fk` (`owner_user_id`),
  KEY `agency_id_list_prev_agency_id_list_id_fk` (`prev_agency_id_list_id`),
  KEY `agency_id_list_next_agency_id_list_id_fk` (`next_agency_id_list_id`),
  KEY `agency_id_list_namespace_id_fk` (`namespace_id`),
  KEY `agency_id_list_replacement_agency_id_list_id_fk` (`replacement_agency_id_list_id`),
  CONSTRAINT `agency_id_list_agency_id_list_value_id_fk` FOREIGN KEY (`agency_id_list_value_id`) REFERENCES `agency_id_list_value` (`agency_id_list_value_id`),
  CONSTRAINT `agency_id_list_based_agency_id_list_id_fk` FOREIGN KEY (`based_agency_id_list_id`) REFERENCES `agency_id_list` (`agency_id_list_id`),
  CONSTRAINT `agency_id_list_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `agency_id_list_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `agency_id_list_namespace_id_fk` FOREIGN KEY (`namespace_id`) REFERENCES `namespace` (`namespace_id`),
  CONSTRAINT `agency_id_list_next_agency_id_list_id_fk` FOREIGN KEY (`next_agency_id_list_id`) REFERENCES `agency_id_list` (`agency_id_list_id`),
  CONSTRAINT `agency_id_list_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `agency_id_list_prev_agency_id_list_id_fk` FOREIGN KEY (`prev_agency_id_list_id`) REFERENCES `agency_id_list` (`agency_id_list_id`),
  CONSTRAINT `agency_id_list_replacement_agency_id_list_id_fk` FOREIGN KEY (`replacement_agency_id_list_id`) REFERENCES `agency_id_list` (`agency_id_list_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The AGENCY_ID_LIST table stores information about agency identification lists. The list''s values are however kept in the AGENCY_ID_LIST_VALUE.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `agency_id_list_manifest`
--

DROP TABLE IF EXISTS `agency_id_list_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `agency_id_list_manifest` (
  `agency_id_list_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `release_id` bigint(20) unsigned NOT NULL,
  `agency_id_list_id` bigint(20) unsigned NOT NULL,
  `agency_id_list_value_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `based_agency_id_list_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `conflict` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This indicates that there is a conflict between self and relationship.',
  `log_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A foreign key pointed to a log for the current record.',
  `replacement_agency_id_list_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement manifest if the record is deprecated.',
  `prev_agency_id_list_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `next_agency_id_list_manifest_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`agency_id_list_manifest_id`),
  KEY `agency_id_list_manifest_agency_id_list_id_fk` (`agency_id_list_id`),
  KEY `agency_id_list_manifest_based_agency_id_list_manifest_id_fk` (`based_agency_id_list_manifest_id`),
  KEY `agency_id_list_manifest_release_id_fk` (`release_id`),
  KEY `agency_id_list_manifest_log_id_fk` (`log_id`),
  KEY `agency_id_list_manifest_prev_agency_id_list_manifest_id_fk` (`prev_agency_id_list_manifest_id`),
  KEY `agency_id_list_manifest_next_agency_id_list_manifest_id_fk` (`next_agency_id_list_manifest_id`),
  KEY `agency_id_list_replacement_agency_id_list_manifest_id_fk` (`replacement_agency_id_list_manifest_id`),
  KEY `agency_id_list_value_manifest_id_fk` (`agency_id_list_value_manifest_id`),
  CONSTRAINT `agency_id_list_manifest_agency_id_list_id_fk` FOREIGN KEY (`agency_id_list_id`) REFERENCES `agency_id_list` (`agency_id_list_id`),
  CONSTRAINT `agency_id_list_manifest_based_agency_id_list_manifest_id_fk` FOREIGN KEY (`based_agency_id_list_manifest_id`) REFERENCES `agency_id_list_manifest` (`agency_id_list_manifest_id`),
  CONSTRAINT `agency_id_list_manifest_log_id_fk` FOREIGN KEY (`log_id`) REFERENCES `log` (`log_id`),
  CONSTRAINT `agency_id_list_manifest_next_agency_id_list_manifest_id_fk` FOREIGN KEY (`next_agency_id_list_manifest_id`) REFERENCES `agency_id_list_manifest` (`agency_id_list_manifest_id`),
  CONSTRAINT `agency_id_list_manifest_prev_agency_id_list_manifest_id_fk` FOREIGN KEY (`prev_agency_id_list_manifest_id`) REFERENCES `agency_id_list_manifest` (`agency_id_list_manifest_id`),
  CONSTRAINT `agency_id_list_manifest_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
  CONSTRAINT `agency_id_list_replacement_agency_id_list_manifest_id_fk` FOREIGN KEY (`replacement_agency_id_list_manifest_id`) REFERENCES `agency_id_list_manifest` (`agency_id_list_manifest_id`),
  CONSTRAINT `agency_id_list_value_manifest_id_fk` FOREIGN KEY (`agency_id_list_value_manifest_id`) REFERENCES `agency_id_list_value_manifest` (`agency_id_list_value_manifest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `agency_id_list_value`
--

DROP TABLE IF EXISTS `agency_id_list_value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `agency_id_list_value` (
  `agency_id_list_value_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `value` varchar(150) NOT NULL COMMENT 'A value in the agency identification list.',
  `name` varchar(150) DEFAULT NULL COMMENT 'Descriptive or short name of the value.',
  `definition` text COMMENT 'The meaning of the value.',
  `definition_source` varchar(100) DEFAULT NULL COMMENT 'This is typically a URL which indicates the source of the agency id list value DEFINITION.',
  `owner_list_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the agency identification list in the AGENCY_ID_LIST table this value belongs to.',
  `based_agency_id_list_value_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the AGENCY_ID_LIST_VALUE table itself. This column is used when the AGENCY_ID_LIST_VALUE is derived from the based AGENCY_ID_LIST_VALUE.',
  `is_deprecated` tinyint(1) DEFAULT '0' COMMENT 'Indicates whether the code list value is deprecated and should not be reused (i.e., no new reference to this record should be allowed).',
  `replacement_agency_id_list_value_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement if the record is deprecated.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created the code list.',
  `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It identifies the user who last updated the code list.',
  `creation_timestamp` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Timestamp when the code list was created.',
  `last_update_timestamp` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Timestamp when the code list was last updated.',
  `prev_agency_id_list_value_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the previous history record.',
  `next_agency_id_list_value_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the next history record.',
  PRIMARY KEY (`agency_id_list_value_id`),
  KEY `agency_id_list_value_owner_list_id_fk` (`owner_list_id`),
  KEY `agency_id_list_value_created_by_fk` (`created_by`),
  KEY `agency_id_list_value_owner_user_id_fk` (`owner_user_id`),
  KEY `agency_id_list_value_last_updated_by_fk` (`last_updated_by`),
  KEY `agency_id_list_value_prev_agency_id_list_value_id_fk` (`prev_agency_id_list_value_id`),
  KEY `agency_id_list_value_next_agency_id_list_value_id_fk` (`next_agency_id_list_value_id`),
  KEY `agency_id_list_value_replacement_agency_id_list_value_id_fk` (`replacement_agency_id_list_value_id`),
  KEY `agency_id_list_value_based_agency_id_list_value_id_fk` (`based_agency_id_list_value_id`),
  CONSTRAINT `agency_id_list_value_based_agency_id_list_value_id_fk` FOREIGN KEY (`based_agency_id_list_value_id`) REFERENCES `agency_id_list_value` (`agency_id_list_value_id`),
  CONSTRAINT `agency_id_list_value_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `agency_id_list_value_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `agency_id_list_value_next_agency_id_list_value_id_fk` FOREIGN KEY (`next_agency_id_list_value_id`) REFERENCES `agency_id_list_value` (`agency_id_list_value_id`),
  CONSTRAINT `agency_id_list_value_owner_list_id_fk` FOREIGN KEY (`owner_list_id`) REFERENCES `agency_id_list` (`agency_id_list_id`),
  CONSTRAINT `agency_id_list_value_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `agency_id_list_value_prev_agency_id_list_value_id_fk` FOREIGN KEY (`prev_agency_id_list_value_id`) REFERENCES `agency_id_list_value` (`agency_id_list_value_id`),
  CONSTRAINT `agency_id_list_value_replacement_agency_id_list_value_id_fk` FOREIGN KEY (`replacement_agency_id_list_value_id`) REFERENCES `agency_id_list_value` (`agency_id_list_value_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table captures the values within an agency identification list.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `agency_id_list_value_manifest`
--

DROP TABLE IF EXISTS `agency_id_list_value_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `agency_id_list_value_manifest` (
  `agency_id_list_value_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `release_id` bigint(20) unsigned NOT NULL,
  `agency_id_list_value_id` bigint(20) unsigned NOT NULL,
  `agency_id_list_manifest_id` bigint(20) unsigned NOT NULL,
  `based_agency_id_list_value_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `conflict` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This indicates that there is a conflict between self and relationship.',
  `replacement_agency_id_list_value_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement manifest if the record is deprecated.',
  `prev_agency_id_list_value_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `next_agency_id_list_value_manifest_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`agency_id_list_value_manifest_id`),
  KEY `agency_id_list_value_manifest_agency_id_list_value_id_fk` (`agency_id_list_value_id`),
  KEY `agency_id_list_value_manifest_release_id_fk` (`release_id`),
  KEY `agency_id_list_value_manifest_agency_id_list_manifest_id_fk` (`agency_id_list_manifest_id`),
  KEY `agency_id_list_value_manifest_prev_agency_id_list_value_manif_fk` (`prev_agency_id_list_value_manifest_id`),
  KEY `agency_id_list_value_manifest_next_agency_id_list_value_manif_fk` (`next_agency_id_list_value_manifest_id`),
  KEY `agency_id_list_value_replacement_agency_id_list_manif_fk` (`replacement_agency_id_list_value_manifest_id`),
  KEY `agency_id_list_value_manifest_based_agency_id_list_val_mnf_id_fk` (`based_agency_id_list_value_manifest_id`),
  CONSTRAINT `agency_id_list_value_manifest_agency_id_list_manifest_id_fk` FOREIGN KEY (`agency_id_list_manifest_id`) REFERENCES `agency_id_list_manifest` (`agency_id_list_manifest_id`),
  CONSTRAINT `agency_id_list_value_manifest_agency_id_list_value_id_fk` FOREIGN KEY (`agency_id_list_value_id`) REFERENCES `agency_id_list_value` (`agency_id_list_value_id`),
  CONSTRAINT `agency_id_list_value_manifest_based_agency_id_list_val_mnf_id_fk` FOREIGN KEY (`based_agency_id_list_value_manifest_id`) REFERENCES `agency_id_list_value_manifest` (`agency_id_list_value_manifest_id`),
  CONSTRAINT `agency_id_list_value_manifest_next_agency_id_list_value_manif_fk` FOREIGN KEY (`next_agency_id_list_value_manifest_id`) REFERENCES `agency_id_list_value_manifest` (`agency_id_list_value_manifest_id`),
  CONSTRAINT `agency_id_list_value_manifest_prev_agency_id_list_value_manif_fk` FOREIGN KEY (`prev_agency_id_list_value_manifest_id`) REFERENCES `agency_id_list_value_manifest` (`agency_id_list_value_manifest_id`),
  CONSTRAINT `agency_id_list_value_manifest_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
  CONSTRAINT `agency_id_list_value_replacement_agency_id_list_manif_fk` FOREIGN KEY (`replacement_agency_id_list_value_manifest_id`) REFERENCES `agency_id_list_value_manifest` (`agency_id_list_value_manifest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `app_oauth2_user`
--

DROP TABLE IF EXISTS `app_oauth2_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_oauth2_user` (
  `app_oauth2_user_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
  `app_user_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A reference to the record in `app_user`. If it is not set, this is treated as a pending record.',
  `oauth2_app_id` bigint(20) unsigned NOT NULL COMMENT 'A reference to the record in `oauth2_app`.',
  `sub` varchar(100) NOT NULL COMMENT '`sub` claim defined in OIDC spec. This is a unique identifier of the subject in the provider.',
  `name` varchar(200) DEFAULT NULL COMMENT '`name` claim defined in OIDC spec.',
  `email` varchar(200) DEFAULT NULL COMMENT '`email` claim defined in OIDC spec.',
  `nickname` varchar(200) DEFAULT NULL COMMENT '`nickname` claim defined in OIDC spec.',
  `preferred_username` varchar(200) DEFAULT NULL COMMENT '`preferred_username` claim defined in OIDC spec.',
  `phone_number` varchar(200) DEFAULT NULL COMMENT '`phone_number` claim defined in OIDC spec.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when this record is created.',
  PRIMARY KEY (`app_oauth2_user_id`),
  UNIQUE KEY `app_oauth2_user_uk1` (`oauth2_app_id`,`sub`),
  KEY `app_oauth2_user_app_user_id_fk` (`app_user_id`),
  CONSTRAINT `app_oauth2_user_app_user_id_fk` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `app_oauth2_user_oauth2_app_id_fk` FOREIGN KEY (`oauth2_app_id`) REFERENCES `oauth2_app` (`oauth2_app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `app_user`
--

DROP TABLE IF EXISTS `app_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_user` (
  `app_user_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
  `login_id` varchar(45) NOT NULL COMMENT 'User Id of the user.',
  `password` varchar(100) DEFAULT NULL COMMENT 'Password to authenticate the user.',
  `name` varchar(100) DEFAULT NULL COMMENT 'Full name of the user.',
  `organization` varchar(100) DEFAULT NULL COMMENT 'The company the user represents.',
  `is_developer` tinyint(1) DEFAULT NULL,
  `is_admin` tinyint(1) DEFAULT '0' COMMENT 'Indicator whether the user has an admin role or not.',
  `is_enabled` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`app_user_id`),
  UNIQUE KEY `app_user_uk1` (`login_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table captures the user information for authentication and authorization purposes.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `asbie`
--

DROP TABLE IF EXISTS `asbie`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `asbie` (
  `asbie_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an ASBIE.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `based_ascc_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'The BASED_ASCC_MANIFEST_ID column refers to the ASCC_MANIFEST record, which this ASBIE contextualizes.',
  `path` text CHARACTER SET ascii,
  `hash_path` varchar(64) CHARACTER SET ascii NOT NULL COMMENT 'hash_path generated from the path of the component graph using hash function, so that it is unique in the graph.',
  `from_abie_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointing to the ABIE table. FROM_ABIE_ID is basically  a parent data element (type) of the TO_ASBIEP_ID. FROM_ABIE_ID must be based on the FROM_ACC_ID in the BASED_ASCC_ID except when the FROM_ACC_ID refers to an SEMANTIC_GROUP ACC or USER_EXTENSION_GROUP ACC.',
  `to_asbiep_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A foreign key to the ASBIEP table. TO_ASBIEP_ID is basically a child data element of the FROM_ABIE_ID. The TO_ASBIEP_ID must be based on the TO_ASCCP_ID in the BASED_ASCC_ID. the ASBIEP is reused with the OWNER_TOP_LEVEL_ASBIEP is different after joining ASBIE and ASBIEP tables',
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
  `owner_top_level_asbiep_id` bigint(20) unsigned NOT NULL COMMENT 'This is a foreign key to the top-level ASBIEP.',
  PRIMARY KEY (`asbie_id`),
  KEY `asbie_created_by_fk` (`created_by`),
  KEY `asbie_from_abie_id_fk` (`from_abie_id`),
  KEY `asbie_last_updated_by_fk` (`last_updated_by`),
  KEY `asbie_to_asbiep_id_fk` (`to_asbiep_id`),
  KEY `asbie_owner_top_level_asbiep_id_fk` (`owner_top_level_asbiep_id`),
  KEY `asbie_based_ascc_manifest_id_fk` (`based_ascc_manifest_id`),
  KEY `asbie_path_k` (`path`(3072)),
  KEY `asbie_hash_path_k` (`hash_path`),
  CONSTRAINT `asbie_based_ascc_manifest_id_fk` FOREIGN KEY (`based_ascc_manifest_id`) REFERENCES `ascc_manifest` (`ascc_manifest_id`),
  CONSTRAINT `asbie_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asbie_from_abie_id_fk` FOREIGN KEY (`from_abie_id`) REFERENCES `abie` (`abie_id`),
  CONSTRAINT `asbie_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asbie_owner_top_level_asbiep_id_fk` FOREIGN KEY (`owner_top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`),
  CONSTRAINT `asbie_to_asbiep_id_fk` FOREIGN KEY (`to_asbiep_id`) REFERENCES `asbiep` (`asbiep_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='An ASBIE represents a relationship/association between two ABIEs through an ASBIEP. It is a contextualization of an ASCC.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `asbie_bizterm`
--

DROP TABLE IF EXISTS `asbie_bizterm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `asbie_bizterm` (
  `asbie_bizterm_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of an asbie_bizterm record.',
  `ascc_bizterm_id` bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the ascc_business_term record.',
  `asbie_id` bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the associated ASBIE',
  `primary_indicator` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'The indicator shows if the business term is primary for the assigned ASBIE.',
  `type_code` char(30) DEFAULT NULL COMMENT 'The type code of the assignment.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the asbie_bizterm record. The creator of the asbie_bizterm is also its owner by default.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the asbie_bizterm record. This may be the user who is in the same group as the creator.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the asbie_bizterm record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the asbie_bizterm was last updated.',
  PRIMARY KEY (`asbie_bizterm_id`),
  KEY `asbie_bizterm_ascc_bizterm_fk` (`ascc_bizterm_id`),
  KEY `asbie_bizterm_asbie_fk` (`asbie_id`),
  CONSTRAINT `asbie_bizterm_asbie_fk` FOREIGN KEY (`asbie_id`) REFERENCES `asbie` (`asbie_id`),
  CONSTRAINT `asbie_bizterm_ascc_bizterm_fk` FOREIGN KEY (`ascc_bizterm_id`) REFERENCES `ascc_bizterm` (`ascc_bizterm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The asbie_bizterm table stores information about the aggregation between the ascc_bizterm and ASBIE. TODO: Placeholder, definition is missing.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `asbiep`
--

DROP TABLE IF EXISTS `asbiep`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `asbiep` (
  `asbiep_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an ASBIEP.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `based_asccp_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointing to the ASCCP_MANIFEST record. It is the ASCCP, on which the ASBIEP contextualizes.',
  `path` text CHARACTER SET ascii,
  `hash_path` varchar(64) CHARACTER SET ascii NOT NULL COMMENT 'hash_path generated from the path of the component graph using hash function, so that it is unique in the graph.',
  `role_of_abie_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointing to the ABIE record. It is the ABIE, which the property term in the based ASCCP qualifies. Note that the ABIE has to be derived from the ACC used by the based ASCCP.',
  `definition` text COMMENT 'A definition to override the ASCCP''s definition. If NULL, it means that the definition should be derived from the based ASCCP on the UI, expression generation, and any API.',
  `remark` varchar(225) DEFAULT NULL COMMENT 'This column allows the user to specify a context-specific usage of the BIE. It is different from the DEFINITION column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ASBIEP can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ASBIEP. A remark about that ASBIEP may be "Type of BOM should be recognized in the BOM/typeCode."',
  `biz_term` varchar(225) DEFAULT NULL COMMENT 'This column represents a business term to indicate what the BIE is called in a particular business context. With this current design, only one business term is allowed per business context.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the ASBIEP. The creator of the ASBIEP is also its owner by default. ASBIEPs created as children of another ABIE have the same CREATED_BY.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the ASBIEP record. ',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the ASBIEP record was first created. ASBIEPs created as children of another ABIE have the same CREATION_TIMESTAMP.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the ASBIEP was last updated.',
  `owner_top_level_asbiep_id` bigint(20) unsigned NOT NULL COMMENT 'This is a foreign key to the top-level ASBIEP.',
  PRIMARY KEY (`asbiep_id`),
  KEY `asbiep_role_of_abie_id_fk` (`role_of_abie_id`),
  KEY `asbiep_created_by_fk` (`created_by`),
  KEY `asbiep_last_updated_by_fk` (`last_updated_by`),
  KEY `asbiep_owner_top_level_asbiep_id_fk` (`owner_top_level_asbiep_id`),
  KEY `asbiep_based_asccp_manifest_id_fk` (`based_asccp_manifest_id`),
  KEY `asbiep_path_k` (`path`(3072)),
  KEY `asbiep_hash_path_k` (`hash_path`),
  CONSTRAINT `asbiep_based_asccp_manifest_id_fk` FOREIGN KEY (`based_asccp_manifest_id`) REFERENCES `asccp_manifest` (`asccp_manifest_id`),
  CONSTRAINT `asbiep_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asbiep_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asbiep_owner_top_level_asbiep_id_fk` FOREIGN KEY (`owner_top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`),
  CONSTRAINT `asbiep_role_of_abie_id_fk` FOREIGN KEY (`role_of_abie_id`) REFERENCES `abie` (`abie_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='ASBIEP represents a role in a usage of an ABIE. It is a contextualization of an ASCCP.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ascc`
--

DROP TABLE IF EXISTS `ascc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ascc` (
  `ascc_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of an ASCC.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `cardinality_min` int(11) NOT NULL COMMENT 'Minimum occurrence of the TO_ASCCP_ID. The valid values are non-negative integer.',
  `cardinality_max` int(11) NOT NULL COMMENT 'Maximum cardinality of the TO_ASCCP_ID. A valid value is integer -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.',
  `seq_key` int(11) DEFAULT NULL COMMENT '@deprecated since 2.0.0. This indicates the order of the associations among other siblings. A valid value is positive integer. The SEQ_KEY at the CC side is localized. In other words, if an ACC is based on another ACC, SEQ_KEY of ASCCs or BCCs of the former ACC starts at 1 again.',
  `from_acc_id` bigint(20) unsigned NOT NULL COMMENT 'FROM_ACC_ID is a foreign key pointing to an ACC record. It is basically pointing to a parent data element (type) of the TO_ASCCP_ID.',
  `to_asccp_id` bigint(20) unsigned NOT NULL COMMENT 'TO_ASCCP_ID is a foreign key to an ASCCP table record. It is basically pointing to a child data element of the FROM_ACC_ID. ',
  `den` varchar(200) NOT NULL COMMENT 'DEN (dictionary entry name) of the ASCC. This column can be derived from Qualifier and OBJECT_CLASS_TERM of the FROM_ACC_ID and DEN of the TO_ASCCP_ID as Qualifier + "_ " + OBJECT_CLASS_TERM + ". " + DEN. ',
  `definition` text COMMENT 'This is a documentation or description of the ASCC. Since ASCC is business context independent, this is a business context independent description of the ASCC. Since there are definitions also in the ASCCP (as referenced by the TO_ASCCP_ID column) and the ACC under that ASCCP, definition in the ASCC is a specific description about the relationship between the ACC (as in FROM_ACC_ID) and the ASCCP.',
  `definition_source` varchar(100) DEFAULT NULL COMMENT 'This is typically a URL identifying the source of the DEFINITION column.',
  `is_deprecated` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  `replacement_ascc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement if the record is deprecated.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key to the APP_USER table referring to the user who creates the entity.\n\nThis column never change between the history and the current record for a given revision. The history record should have the same value as that of its current record.',
  `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership. ',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key to the APP_USER table referring to the last user who has updated the record. \n\nIn the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the revision of the ASCC was created. \n\nThis never change for a revision.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the change has occurred.',
  `state` varchar(20) DEFAULT NULL COMMENT 'Deleted, WIP, Draft, QA, Candidate, Production, Release Draft, Published. This the revision life cycle state of the BCC.\n\nState change can''t be undone. But the history record can still keep the records of when the state was changed.',
  `prev_ascc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the previous history record.',
  `next_ascc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the next history record.',
  PRIMARY KEY (`ascc_id`),
  KEY `ascc_from_acc_id_fk` (`from_acc_id`),
  KEY `ascc_to_asccp_id_fk` (`to_asccp_id`),
  KEY `ascc_created_by_fk` (`created_by`),
  KEY `ascc_owner_user_id_fk` (`owner_user_id`),
  KEY `ascc_last_updated_by_fk` (`last_updated_by`),
  KEY `ascc_prev_ascc_id_fk` (`prev_ascc_id`),
  KEY `ascc_next_ascc_id_fk` (`next_ascc_id`),
  KEY `ascc_guid_idx` (`guid`),
  KEY `ascc_last_update_timestamp_desc_idx` (`last_update_timestamp`),
  KEY `ascc_replacement_ascc_id_fk` (`replacement_ascc_id`),
  CONSTRAINT `ascc_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `ascc_from_acc_id_fk` FOREIGN KEY (`from_acc_id`) REFERENCES `acc` (`acc_id`),
  CONSTRAINT `ascc_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `ascc_next_ascc_id_fk` FOREIGN KEY (`next_ascc_id`) REFERENCES `ascc` (`ascc_id`),
  CONSTRAINT `ascc_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `ascc_prev_ascc_id_fk` FOREIGN KEY (`prev_ascc_id`) REFERENCES `ascc` (`ascc_id`),
  CONSTRAINT `ascc_replacement_ascc_id_fk` FOREIGN KEY (`replacement_ascc_id`) REFERENCES `ascc` (`ascc_id`),
  CONSTRAINT `ascc_to_asccp_id_fk` FOREIGN KEY (`to_asccp_id`) REFERENCES `asccp` (`asccp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='An ASCC represents a relationship/association between two ACCs through an ASCCP. ';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ascc_bizterm`
--

DROP TABLE IF EXISTS `ascc_bizterm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ascc_bizterm` (
  `ascc_bizterm_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of an Business term.',
  `business_term_id` bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the associated business term',
  `ascc_id` bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the associated ASCC',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the ascc_bizterm record. The creator of the ascc_bizterm is also its owner by default.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the ascc_bizterm record. This may be the user who is in the same group as the creator.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the ascc_bizterm record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the ascc_bizterm was last updated.',
  PRIMARY KEY (`ascc_bizterm_id`),
  KEY `ascc_bizterm_ascc_fk` (`ascc_id`),
  KEY `ascc_bizterm_business_term_fk` (`business_term_id`),
  CONSTRAINT `ascc_bizterm_ascc_fk` FOREIGN KEY (`ascc_id`) REFERENCES `ascc` (`ascc_id`),
  CONSTRAINT `ascc_bizterm_business_term_fk` FOREIGN KEY (`business_term_id`) REFERENCES `business_term` (`business_term_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The ascc_bizterm table stores information about the aggregation between the business term and ASCC. TODO: Placeholder, definition is missing.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ascc_manifest`
--

DROP TABLE IF EXISTS `ascc_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ascc_manifest` (
  `ascc_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `release_id` bigint(20) unsigned DEFAULT NULL,
  `ascc_id` bigint(20) unsigned NOT NULL,
  `seq_key_id` bigint(20) unsigned DEFAULT NULL,
  `from_acc_manifest_id` bigint(20) unsigned NOT NULL,
  `to_asccp_manifest_id` bigint(20) unsigned NOT NULL,
  `conflict` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This indicates that there is a conflict between self and relationship.',
  `replacement_ascc_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement manifest if the record is deprecated.',
  `prev_ascc_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `next_ascc_manifest_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`ascc_manifest_id`),
  KEY `ascc_manifest_ascc_id_fk` (`ascc_id`),
  KEY `ascc_manifest_release_id_fk` (`release_id`),
  KEY `ascc_manifest_from_acc_manifest_id_fk` (`from_acc_manifest_id`),
  KEY `ascc_manifest_to_asccp_manifest_id_fk` (`to_asccp_manifest_id`),
  KEY `ascc_manifest_prev_ascc_manifest_id_fk` (`prev_ascc_manifest_id`),
  KEY `ascc_manifest_next_ascc_manifest_id_fk` (`next_ascc_manifest_id`),
  KEY `ascc_manifest_seq_key_id_fk` (`seq_key_id`),
  KEY `ascc_replacement_ascc_manifest_id_fk` (`replacement_ascc_manifest_id`),
  CONSTRAINT `ascc_manifest_ascc_id_fk` FOREIGN KEY (`ascc_id`) REFERENCES `ascc` (`ascc_id`),
  CONSTRAINT `ascc_manifest_from_acc_manifest_id_fk` FOREIGN KEY (`from_acc_manifest_id`) REFERENCES `acc_manifest` (`acc_manifest_id`),
  CONSTRAINT `ascc_manifest_next_ascc_manifest_id_fk` FOREIGN KEY (`next_ascc_manifest_id`) REFERENCES `ascc_manifest` (`ascc_manifest_id`),
  CONSTRAINT `ascc_manifest_prev_ascc_manifest_id_fk` FOREIGN KEY (`prev_ascc_manifest_id`) REFERENCES `ascc_manifest` (`ascc_manifest_id`),
  CONSTRAINT `ascc_manifest_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
  CONSTRAINT `ascc_manifest_seq_key_id_fk` FOREIGN KEY (`seq_key_id`) REFERENCES `seq_key` (`seq_key_id`),
  CONSTRAINT `ascc_manifest_to_asccp_manifest_id_fk` FOREIGN KEY (`to_asccp_manifest_id`) REFERENCES `asccp_manifest` (`asccp_manifest_id`),
  CONSTRAINT `ascc_replacement_ascc_manifest_id_fk` FOREIGN KEY (`replacement_ascc_manifest_id`) REFERENCES `ascc_manifest` (`ascc_manifest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `asccp`
--

DROP TABLE IF EXISTS `asccp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `asccp` (
  `asccp_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of an ASCCP.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `type` varchar(32) DEFAULT 'Default' COMMENT 'The Type of the ASCCP. List: Default, Extension ',
  `property_term` varchar(100) DEFAULT NULL COMMENT 'The role (or property) the ACC as referred to by the Role_Of_ACC_ID play when the ASCCP is used by another ACC. There must be only one ASCCP without a Property_Term for a particular ACC.',
  `definition` text COMMENT 'Description of the ASCCP.',
  `definition_source` varchar(100) DEFAULT NULL COMMENT 'This is typically a URL identifying the source of the DEFINITION column.',
  `role_of_acc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'The ACC from which this ASCCP is created (ASCCP applies role to the ACC).',
  `den` varchar(200) DEFAULT NULL COMMENT 'The dictionary entry name of the ASCCP.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the user who creates the entity. \n\nThis column never change between the history and the current record for a given revision. The history record should have the same value as that of its current record.',
  `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership. ',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who has updated the record. \n\nIn the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the revision of the ASCCP was created. \n\nThis never change for a revision.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.',
  `state` varchar(20) DEFAULT NULL COMMENT 'Deleted, WIP, Draft, QA, Candidate, Production, Release Draft, Published. This the revision life cycle state of the ASCCP.\n\nState change can''t be undone. But the history record can still keep the records of when the state was changed.',
  `namespace_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the Namespace table. This is the namespace to which the entity belongs. This namespace column is primarily used in the case the component is a user''s component because there is also a namespace assigned at the release level.',
  `reusable_indicator` tinyint(1) DEFAULT '1' COMMENT 'This indicates whether the ASCCP can be used by more than one ASCC. This maps directly to the XML schema local element declaration.',
  `is_deprecated` tinyint(1) NOT NULL COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  `replacement_asccp_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement if the record is deprecated.',
  `is_nillable` tinyint(1) DEFAULT NULL COMMENT 'This is corresponding to the XML schema nillable flag. Although the nillable may not apply in certain cases of the ASCCP (e.g., when it corresponds to an XSD group), the value is default to false for simplification.',
  `prev_asccp_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the previous history record.',
  `next_asccp_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the next history record.',
  PRIMARY KEY (`asccp_id`),
  KEY `asccp_role_of_acc_id_fk` (`role_of_acc_id`),
  KEY `asccp_created_by_fk` (`created_by`),
  KEY `asccp_owner_user_id_fk` (`owner_user_id`),
  KEY `asccp_last_updated_by_fk` (`last_updated_by`),
  KEY `asccp_namespace_id_fk` (`namespace_id`),
  KEY `asccp_prev_asccp_id_fk` (`prev_asccp_id`),
  KEY `asccp_next_asccp_id_fk` (`next_asccp_id`),
  KEY `asccp_guid_idx` (`guid`),
  KEY `asccp_last_update_timestamp_desc_idx` (`last_update_timestamp`),
  KEY `asccp_replacement_asccp_id_fk` (`replacement_asccp_id`),
  CONSTRAINT `asccp_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asccp_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asccp_namespace_id_fk` FOREIGN KEY (`namespace_id`) REFERENCES `namespace` (`namespace_id`),
  CONSTRAINT `asccp_next_asccp_id_fk` FOREIGN KEY (`next_asccp_id`) REFERENCES `asccp` (`asccp_id`),
  CONSTRAINT `asccp_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asccp_prev_asccp_id_fk` FOREIGN KEY (`prev_asccp_id`) REFERENCES `asccp` (`asccp_id`),
  CONSTRAINT `asccp_replacement_asccp_id_fk` FOREIGN KEY (`replacement_asccp_id`) REFERENCES `asccp` (`asccp_id`),
  CONSTRAINT `asccp_role_of_acc_id_fk` FOREIGN KEY (`role_of_acc_id`) REFERENCES `acc` (`acc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='An ASCCP specifies a role (or property) an ACC may play under another ACC.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `asccp_manifest`
--

DROP TABLE IF EXISTS `asccp_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `asccp_manifest` (
  `asccp_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `release_id` bigint(20) unsigned NOT NULL,
  `asccp_id` bigint(20) unsigned NOT NULL,
  `role_of_acc_manifest_id` bigint(20) unsigned NOT NULL,
  `conflict` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This indicates that there is a conflict between self and relationship.',
  `log_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A foreign key pointed to a log for the current record.',
  `replacement_asccp_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement manifest if the record is deprecated.',
  `prev_asccp_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `next_asccp_manifest_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`asccp_manifest_id`),
  KEY `asccp_manifest_asccp_id_fk` (`asccp_id`),
  KEY `asccp_manifest_role_of_acc_manifest_id_fk` (`role_of_acc_manifest_id`),
  KEY `asccp_manifest_release_id_fk` (`release_id`),
  KEY `asccp_manifest_log_id_fk` (`log_id`),
  KEY `asccp_manifest_prev_asccp_manifest_id_fk` (`prev_asccp_manifest_id`),
  KEY `asccp_manifest_next_asccp_manifest_id_fk` (`next_asccp_manifest_id`),
  KEY `asccp_replacement_asccp_manifest_id_fk` (`replacement_asccp_manifest_id`),
  CONSTRAINT `asccp_manifest_asccp_id_fk` FOREIGN KEY (`asccp_id`) REFERENCES `asccp` (`asccp_id`),
  CONSTRAINT `asccp_manifest_log_id_fk` FOREIGN KEY (`log_id`) REFERENCES `log` (`log_id`),
  CONSTRAINT `asccp_manifest_next_asccp_manifest_id_fk` FOREIGN KEY (`next_asccp_manifest_id`) REFERENCES `asccp_manifest` (`asccp_manifest_id`),
  CONSTRAINT `asccp_manifest_prev_asccp_manifest_id_fk` FOREIGN KEY (`prev_asccp_manifest_id`) REFERENCES `asccp_manifest` (`asccp_manifest_id`),
  CONSTRAINT `asccp_manifest_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
  CONSTRAINT `asccp_manifest_role_of_acc_manifest_id_fk` FOREIGN KEY (`role_of_acc_manifest_id`) REFERENCES `acc_manifest` (`acc_manifest_id`),
  CONSTRAINT `asccp_replacement_asccp_manifest_id_fk` FOREIGN KEY (`replacement_asccp_manifest_id`) REFERENCES `asccp_manifest` (`asccp_manifest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `asccp_manifest_tag`
--

DROP TABLE IF EXISTS `asccp_manifest_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `asccp_manifest_tag` (
  `asccp_manifest_id` bigint(20) unsigned NOT NULL,
  `tag_id` bigint(20) unsigned NOT NULL,
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the record was first created.',
  PRIMARY KEY (`asccp_manifest_id`,`tag_id`),
  KEY `asccp_manifest_tag_asccp_manifest_id_fk` (`asccp_manifest_id`),
  KEY `asccp_manifest_tag_tag_id_fk` (`tag_id`),
  KEY `asccp_manifest_tag_created_by_fk` (`created_by`),
  CONSTRAINT `asccp_manifest_tag_asccp_manifest_id_fk` FOREIGN KEY (`asccp_manifest_id`) REFERENCES `asccp_manifest` (`asccp_manifest_id`),
  CONSTRAINT `asccp_manifest_tag_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `asccp_manifest_tag_tag_id_fk` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbie`
--

DROP TABLE IF EXISTS `bbie`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbie` (
  `bbie_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of a BBIE.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `based_bcc_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'The BASED_BCC_MANIFEST_ID column refers to the BCC_MANIFEST record, which this BBIE contextualizes.',
  `path` text CHARACTER SET ascii,
  `hash_path` varchar(64) CHARACTER SET ascii NOT NULL COMMENT 'hash_path generated from the path of the component graph using hash function, so that it is unique in the graph.',
  `from_abie_id` bigint(20) unsigned NOT NULL COMMENT 'FROM_ABIE_ID must be based on the FROM_ACC_ID in the BASED_BCC_ID.',
  `to_bbiep_id` bigint(20) unsigned NOT NULL COMMENT 'TO_BBIEP_ID is a foreign key to the BBIEP table. TO_BBIEP_ID basically refers to a child data element of the FROM_ABIE_ID. TO_BBIEP_ID must be based on the TO_BCCP_ID in the based BCC.',
  `bdt_pri_restri_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is the foreign key to the BDT_PRI_RESTRI table. It indicates the primitive assigned to the BBIE (or also can be viewed as assigned to the BBIEP for this specific association). This is assigned by the user who authors the BIE. The assignment would override the default from the CC side.',
  `code_list_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the CODE_LIST_MANIFEST table. If a code list is assigned to the BBIE (or also can be viewed as assigned to the BBIEP for this association), then this column stores the assigned code list. It should be noted that one of the possible primitives assignable to the BDT_PRI_RESTRI_ID column may also be a code list. So this column is typically used when the user wants to assign another code list different from the one permissible by the CC model.',
  `agency_id_list_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the AGENCY_ID_LIST_MANIFEST table. It is used in the case that the BDT content can be restricted to an agency identification.',
  `cardinality_min` int(11) NOT NULL COMMENT 'The minimum occurrence constraint for the BBIE. A valid value is a non-negative integer.',
  `cardinality_max` int(11) DEFAULT NULL COMMENT 'Maximum occurence constraint of the TO_BBIEP_ID. A valid value is an integer from -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.',
  `facet_min_length` bigint(20) unsigned DEFAULT NULL COMMENT 'Defines the minimum number of units of length.',
  `facet_max_length` bigint(20) unsigned DEFAULT NULL COMMENT 'Defines the minimum number of units of length.',
  `facet_pattern` text COMMENT 'Defines a constraint on the lexical space of a datatype to literals in a specific pattern.',
  `default_value` text COMMENT 'This column specifies the default value constraint. Default and fixed value constraints cannot be used at the same time.',
  `is_nillable` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicate whether the field can have a null  This is corresponding to the nillable flag in the XML schema.',
  `fixed_value` text COMMENT 'This column captures the fixed value constraint. Default and fixed value constraints cannot be used at the same time.',
  `is_null` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This column indicates whether the field is fixed to NULL. IS_NULLl can be true only if the IS_NILLABLE is true. If IS_NULL is true then the FIX_VALUE and DEFAULT_VALUE columns cannot have a value.',
  `definition` text COMMENT 'Description to override the BCC definition. If NULLl, it means that the definition should be inherited from the based BCC.',
  `example` text,
  `remark` varchar(225) DEFAULT NULL COMMENT 'This column allows the user to specify very context-specific usage of the BIE. It is different from the DEFINITION column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be "Type of BOM should be recognized in the BOM/typeCode."',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the BBIE. The creator of the BBIE is also its owner by default. BBIEs created as children of another ABIE have the same CREATED_BY.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who has last updated the ASBIE record. ',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the BBIE record was first created. BBIEs created as children of another ABIE have the same CREATION_TIMESTAMP.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the ASBIE was last updated.',
  `seq_key` decimal(10,2) DEFAULT NULL COMMENT 'This indicates the order of the associations among other siblings. The SEQ_KEY for BIEs is decimal in order to accomodate the removal of inheritance hierarchy and group. For example, children of the most abstract ACC will have SEQ_KEY = 1.1, 1.2, 1.3, and so on; and SEQ_KEY of the next abstraction level ACC will have SEQ_KEY = 2.1, 2.2, 2.3 and so on so forth.',
  `is_used` tinyint(1) DEFAULT '0' COMMENT 'Flag to indicate whether the field/component is used in the content model. It indicates whether the field/component should be generated in the expression generation.',
  `owner_top_level_asbiep_id` bigint(20) unsigned NOT NULL COMMENT 'This is a foreign key to the top-level ASBIEP.',
  PRIMARY KEY (`bbie_id`),
  KEY `bbie_from_abie_id_fk` (`from_abie_id`),
  KEY `bbie_to_bbiep_id_fk` (`to_bbiep_id`),
  KEY `bbie_bdt_pri_restri_id_fk` (`bdt_pri_restri_id`),
  KEY `bbie_created_by_fk` (`created_by`),
  KEY `bbie_last_updated_by_fk` (`last_updated_by`),
  KEY `bbie_owner_top_level_asbiep_id_fk` (`owner_top_level_asbiep_id`),
  KEY `bbie_based_bcc_manifest_id_fk` (`based_bcc_manifest_id`),
  KEY `bbie_path_k` (`path`(3072)),
  KEY `bbie_hash_path_k` (`hash_path`),
  KEY `bbie_code_list_manifest_id_fk` (`code_list_manifest_id`),
  KEY `bbie_agency_id_list_manifest_id_fk` (`agency_id_list_manifest_id`),
  CONSTRAINT `bbie_agency_id_list_manifest_id_fk` FOREIGN KEY (`agency_id_list_manifest_id`) REFERENCES `agency_id_list_manifest` (`agency_id_list_manifest_id`),
  CONSTRAINT `bbie_based_bcc_manifest_id_fk` FOREIGN KEY (`based_bcc_manifest_id`) REFERENCES `bcc_manifest` (`bcc_manifest_id`),
  CONSTRAINT `bbie_bdt_pri_restri_id_fk` FOREIGN KEY (`bdt_pri_restri_id`) REFERENCES `bdt_pri_restri` (`bdt_pri_restri_id`),
  CONSTRAINT `bbie_code_list_manifest_id_fk` FOREIGN KEY (`code_list_manifest_id`) REFERENCES `code_list_manifest` (`code_list_manifest_id`),
  CONSTRAINT `bbie_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bbie_from_abie_id_fk` FOREIGN KEY (`from_abie_id`) REFERENCES `abie` (`abie_id`),
  CONSTRAINT `bbie_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bbie_owner_top_level_asbiep_id_fk` FOREIGN KEY (`owner_top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`),
  CONSTRAINT `bbie_to_bbiep_id_fk` FOREIGN KEY (`to_bbiep_id`) REFERENCES `bbiep` (`bbiep_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='A BBIE represents a relationship/association between an ABIE and a BBIEP. It is a contextualization of a BCC. The BBIE table also stores some information about the specific constraints related to the BDT associated with the BBIEP. In particular, the three columns including the BDT_PRI_RESTRI_ID, CODE_LIST_ID, and AGENCY_ID_LIST_ID allows for capturing of the specific primitive to be used in the context. Only one column among the three can have a value in a particular record.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbie_bizterm`
--

DROP TABLE IF EXISTS `bbie_bizterm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbie_bizterm` (
  `bbie_bizterm_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of an bbie_bizterm record.',
  `bcc_bizterm_id` bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the bbie_bizterm record.',
  `bbie_id` bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the associated BBIE',
  `primary_indicator` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'The indicator shows if the business term is primary for the assigned BBIE.',
  `type_code` char(30) DEFAULT NULL COMMENT 'The type code of the assignment.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the bbie_bizterm record. The creator of the asbie_bizterm is also its owner by default.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the bbie_bizterm record. This may be the user who is in the same group as the creator.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the bbie_bizterm record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the bbie_bizterm was last updated.',
  PRIMARY KEY (`bbie_bizterm_id`),
  KEY `bbie_bizterm_bcc_bizterm_fk` (`bcc_bizterm_id`),
  KEY `asbie_bizterm_asbie_fk` (`bbie_id`),
  CONSTRAINT `bbie_bizterm_bbie_fk` FOREIGN KEY (`bbie_id`) REFERENCES `bbie` (`bbie_id`),
  CONSTRAINT `bbie_bizterm_bcc_bizterm_fk` FOREIGN KEY (`bcc_bizterm_id`) REFERENCES `bcc_bizterm` (`bcc_bizterm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The bbie_bizterm table stores information about the aggregation between the bbie_bizterm and BBIE. TODO: Placeholder, definition is missing.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbie_sc`
--

DROP TABLE IF EXISTS `bbie_sc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbie_sc` (
  `bbie_sc_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of a BBIE_SC.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `based_dt_sc_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the DT_SC_MANIFEST table. This should correspond to the DT_SC of the BDT of the based BCC and BCCP.',
  `path` text CHARACTER SET ascii,
  `hash_path` varchar(64) CHARACTER SET ascii NOT NULL COMMENT 'hash_path generated from the path of the component graph using hash function, so that it is unique in the graph.',
  `bbie_id` bigint(20) unsigned NOT NULL COMMENT 'The BBIE this BBIE_SC applies to.',
  `dt_sc_pri_restri_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This must be one of the allowed primitive/code list as specified in the corresponding SC of the based BCC of the BBIE (referred to by the BBIE_ID column).\n\nIt is the foreign key to the BDT_SC_PRI_RESTRI table. It indicates the primitive assigned to the BBIE (or also can be viewed as assigned to the BBIEP for this specific association). This is assigned by the user who authors the BIE. The assignment would override the default from the CC side.\n\nThis column, the CODE_LIST_ID column, and AGENCY_ID_LIST_ID column cannot have a value at the same time.',
  `code_list_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the CODE_LIST_MANIFEST table. If a code list is assigned to the BBIE SC (or also can be viewed as assigned to the BBIEP SC for this association), then this column stores the assigned code list. It should be noted that one of the possible primitives assignable to the DT_SC_PRI_RESTRI_ID column may also be a code list. So this column is typically used when the user wants to assign another code list different from the one permissible by the CC model.\n\nThis column is, the DT_SC_PRI_RESTRI_ID column, and AGENCY_ID_LIST_ID column cannot have a value at the same time.',
  `agency_id_list_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the AGENCY_ID_LIST_MANIFEST table. If a agency ID list is assigned to the BBIE SC (or also can be viewed as assigned to the BBIEP SC for this association), then this column stores the assigned Agency ID list. It should be noted that one of the possible primitives assignable to the DT_SC_PRI_RESTRI_ID column may also be an Agency ID list. So this column is typically used only when the user wants to assign another Agency ID list different from the one permissible by the CC model.\n\nThis column, the DT_SC_PRI_RESTRI_ID column, and CODE_LIST_ID column cannot have a value at the same time.',
  `cardinality_min` int(11) NOT NULL COMMENT 'The minimum occurrence constraint for the BBIE SC. A valid value is 0 or 1.',
  `cardinality_max` int(11) NOT NULL COMMENT 'Maximum occurence constraint of the BBIE SC. A valid value is 0 or 1.',
  `facet_min_length` bigint(20) unsigned DEFAULT NULL COMMENT 'Defines the minimum number of units of length.',
  `facet_max_length` bigint(20) unsigned DEFAULT NULL COMMENT 'Defines the minimum number of units of length.',
  `facet_pattern` text COMMENT 'Defines a constraint on the lexical space of a datatype to literals in a specific pattern.',
  `default_value` text COMMENT 'This column specifies the default value constraint. Default and fixed value constraints cannot be used at the same time.',
  `fixed_value` text COMMENT 'This column captures the fixed value constraint. Default and fixed value constraints cannot be used at the same time.',
  `definition` text COMMENT 'Description to override the BDT SC definition. If NULL, it means that the definition should be inherited from the based BDT SC.',
  `example` text,
  `remark` varchar(225) DEFAULT NULL COMMENT 'This column allows the user to specify a very context-specific usage of the BBIE SC. It is different from the Definition column in that the Definition column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. ',
  `biz_term` varchar(225) DEFAULT NULL COMMENT 'Business term to indicate what the BBIE SC is called in a particular business context. With this current design, only one business term is allowed per business context.',
  `is_used` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Flag to indicate whether the field/component is used in the content model. It indicates whether the field/component should be generated.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the BBIE_SC. The creator of the BBIE_SC is also its owner by default.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who has last updated the BBIE_SC record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the BBIE_SC record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the BBIE_SC was last updated.',
  `owner_top_level_asbiep_id` bigint(20) unsigned NOT NULL COMMENT 'This is a foreign key to the top-level ASBIEP.',
  PRIMARY KEY (`bbie_sc_id`),
  KEY `bbie_sc_bbie_id_fk` (`bbie_id`),
  KEY `bbie_sc_dt_sc_pri_restri_id_fk` (`dt_sc_pri_restri_id`),
  KEY `bbie_sc_owner_top_level_asbiep_id_fk` (`owner_top_level_asbiep_id`),
  KEY `bbie_sc_based_dt_sc_manifest_id_fk` (`based_dt_sc_manifest_id`),
  KEY `bbie_sc_path_k` (`path`(3072)),
  KEY `bbie_sc_hash_path_k` (`hash_path`),
  KEY `bbie_sc_created_by_fk` (`created_by`),
  KEY `bbie_sc_last_updated_by_fk` (`last_updated_by`),
  KEY `bbie_sc_code_list_manifest_id_fk` (`code_list_manifest_id`),
  KEY `bbie_sc_agency_id_list_manifest_id_fk` (`agency_id_list_manifest_id`),
  CONSTRAINT `bbie_sc_agency_id_list_manifest_id_fk` FOREIGN KEY (`agency_id_list_manifest_id`) REFERENCES `agency_id_list_manifest` (`agency_id_list_manifest_id`),
  CONSTRAINT `bbie_sc_based_dt_sc_manifest_id_fk` FOREIGN KEY (`based_dt_sc_manifest_id`) REFERENCES `dt_sc_manifest` (`dt_sc_manifest_id`),
  CONSTRAINT `bbie_sc_bbie_id_fk` FOREIGN KEY (`bbie_id`) REFERENCES `bbie` (`bbie_id`),
  CONSTRAINT `bbie_sc_code_list_manifest_id_fk` FOREIGN KEY (`code_list_manifest_id`) REFERENCES `code_list_manifest` (`code_list_manifest_id`),
  CONSTRAINT `bbie_sc_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bbie_sc_dt_sc_pri_restri_id_fk` FOREIGN KEY (`dt_sc_pri_restri_id`) REFERENCES `bdt_sc_pri_restri` (`bdt_sc_pri_restri_id`),
  CONSTRAINT `bbie_sc_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bbie_sc_owner_top_level_asbiep_id_fk` FOREIGN KEY (`owner_top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Because there is no single table that is a contextualized counterpart of the DT table (which stores both CDT and BDT), The context specific constraints associated with the DT are stored in the BBIE table, while this table stores the constraints associated with the DT''s SCs. ';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbiep`
--

DROP TABLE IF EXISTS `bbiep`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbiep` (
  `bbiep_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an BBIEP.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `based_bccp_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key pointing to the BCCP_MANIFEST record. It is the BCCP, which the BBIEP contextualizes.',
  `path` text CHARACTER SET ascii,
  `hash_path` varchar(64) CHARACTER SET ascii NOT NULL COMMENT 'hash_path generated from the path of the component graph using hash function, so that it is unique in the graph.',
  `definition` text COMMENT 'Definition to override the BCCP''s Definition. If NULLl, it means that the definition should be inherited from the based CC.',
  `remark` varchar(225) DEFAULT NULL COMMENT 'This column allows the user to specify very context-specific usage of the BIE. It is different from the Definition column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be "Type of BOM should be recognized in the BOM/typeCode.',
  `biz_term` varchar(225) DEFAULT NULL COMMENT 'Business term to indicate what the BIE is called in a particular business context such as in an industry.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the BBIEP. The creator of the BBIEP is also its owner by default. BBIEPs created as children of another ABIE have the same CREATED_BY'',',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the BBIEP record. ',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the BBIEP record was first created. BBIEPs created as children of another ABIE have the same CREATION_TIMESTAMP,',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the BBIEP was last updated.',
  `owner_top_level_asbiep_id` bigint(20) unsigned NOT NULL COMMENT 'This is a foreign key to the top-level ASBIEP.',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='BBIEP represents the usage of basic property in a specific business context. It is a contextualization of a BCCP.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bcc`
--

DROP TABLE IF EXISTS `bcc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bcc` (
  `bcc_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an BCC.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `cardinality_min` int(11) NOT NULL COMMENT 'Minimum cardinality of the TO_BCCP_ID. The valid values are non-negative integer.',
  `cardinality_max` int(11) DEFAULT NULL COMMENT 'Maximum cardinality of the TO_BCCP_ID. The valid values are integer -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.'',',
  `to_bccp_id` bigint(20) unsigned NOT NULL COMMENT 'TO_BCCP_ID is a foreign key to an BCCP table record. It is basically pointing to a child data element of the FROM_ACC_ID. \n\nNote that for the BCC history records, this column always points to the BCCP_ID of the current record of a BCCP.'',',
  `from_acc_id` bigint(20) unsigned NOT NULL COMMENT 'FROM_ACC_ID is a foreign key pointing to an ACC record. It is basically pointing to a parent data element (type) of the TO_BCCP_ID. \n\nNote that for the BCC history records, this column always points to the ACC_ID of the current record of an ACC.',
  `seq_key` int(11) DEFAULT NULL COMMENT '@deprecated since 2.0.0. This indicates the order of the associations among other siblings. A valid value is positive integer. The SEQ_KEY at the CC side is localized. In other words, if an ACC is based on another ACC, SEQ_KEY of ASCCs or BCCs of the former ACC starts at 1 again.',
  `entity_type` int(11) DEFAULT NULL COMMENT 'This is a code list: 0 = ATTRIBUTE and 1 = ELEMENT. An expression generator may or may not use this information. This column is necessary because some of the BCCs are xsd:attribute and some are xsd:element in the OAGIS 10.x. ',
  `den` varchar(200) NOT NULL COMMENT 'DEN (dictionary entry name) of the BCC. This column can be derived from QUALIFIER and OBJECT_CLASS_TERM of the FROM_ACC_ID and DEN of the TO_BCCP_ID as QUALIFIER + "_ " + OBJECT_CLASS_TERM + ". " + DEN. ',
  `definition` text COMMENT 'This is a documentation or description of the BCC. Since BCC is business context independent, this is a business context independent description of the BCC. Since there are definitions also in the BCCP (as referenced by TO_BCCP_ID column) and the BDT under that BCCP, the definition in the BCC is a specific description about the relationship between the ACC (as in FROM_ACC_ID) and the BCCP.',
  `definition_source` varchar(100) DEFAULT NULL COMMENT 'This is typically a URL identifying the source of the DEFINITION column.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the user who creates the entity.\n\nThis column never change between the history and the current record. The history record should have the same value as that of its current record.',
  `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who has updated the record. \n\nIn the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the revision of the BCC was created. \n\nThis never change for a revision.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the change has occurred.',
  `state` varchar(20) DEFAULT NULL COMMENT 'Deleted, WIP, Draft, QA, Candidate, Production, Release Draft, Published. This the revision life cycle state of the BCC.\n\nState change can''t be undone. But the history record can still keep the records of when the state was changed.',
  `is_deprecated` tinyint(1) NOT NULL COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  `replacement_bcc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement if the record is deprecated.',
  `is_nillable` tinyint(1) NOT NULL DEFAULT '0' COMMENT '@deprecated since 2.0.0 in favor of impossibility of nillable association (element reference) in XML schema.\n\nIndicate whether the field can have a NULL This is corresponding to the nillable flag in the XML schema.',
  `default_value` text COMMENT 'This set the default value at the association level. ',
  `fixed_value` text COMMENT 'This column captures the fixed value constraint. Default and fixed value constraints cannot be used at the same time.',
  `prev_bcc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the previous history record.',
  `next_bcc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the next history record.',
  PRIMARY KEY (`bcc_id`),
  KEY `bcc_to_bccp_id_fk` (`to_bccp_id`),
  KEY `bcc_from_acc_id_fk` (`from_acc_id`),
  KEY `bcc_created_by_fk` (`created_by`),
  KEY `bcc_owner_user_id_fk` (`owner_user_id`),
  KEY `bcc_last_updated_by_fk` (`last_updated_by`),
  KEY `bcc_prev_bcc_id_fk` (`prev_bcc_id`),
  KEY `bcc_next_bcc_id_fk` (`next_bcc_id`),
  KEY `bcc_guid_idx` (`guid`),
  KEY `bcc_last_update_timestamp_desc_idx` (`last_update_timestamp`),
  KEY `bcc_replacement_bcc_id_fk` (`replacement_bcc_id`),
  CONSTRAINT `bcc_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bcc_from_acc_id_fk` FOREIGN KEY (`from_acc_id`) REFERENCES `acc` (`acc_id`),
  CONSTRAINT `bcc_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bcc_next_bcc_id_fk` FOREIGN KEY (`next_bcc_id`) REFERENCES `bcc` (`bcc_id`),
  CONSTRAINT `bcc_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bcc_prev_bcc_id_fk` FOREIGN KEY (`prev_bcc_id`) REFERENCES `bcc` (`bcc_id`),
  CONSTRAINT `bcc_replacement_bcc_id_fk` FOREIGN KEY (`replacement_bcc_id`) REFERENCES `bcc` (`bcc_id`),
  CONSTRAINT `bcc_to_bccp_id_fk` FOREIGN KEY (`to_bccp_id`) REFERENCES `bccp` (`bccp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='A BCC represents a relationship/association between an ACC and a BCCP. It creates a data element for an ACC. ';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bcc_bizterm`
--

DROP TABLE IF EXISTS `bcc_bizterm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bcc_bizterm` (
  `bcc_bizterm_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of an bcc_bizterm record.',
  `business_term_id` bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the associated business term',
  `bcc_id` bigint(20) unsigned NOT NULL COMMENT 'An internal ID of the associated BCC',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the bcc_bizterm record. The creator of the bcc_bizterm is also its owner by default.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the bcc_bizterm record. This may be the user who is in the same group as the creator.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the bcc_bizterm record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the bcc_bizterm was last updated.',
  PRIMARY KEY (`bcc_bizterm_id`),
  KEY `bcc_bizterm_bcc_fk` (`bcc_id`),
  KEY `bcc_bizterm_business_term_fk` (`business_term_id`),
  CONSTRAINT `bcc_bizterm_bcc_fk` FOREIGN KEY (`bcc_id`) REFERENCES `bcc` (`bcc_id`),
  CONSTRAINT `bcc_bizterm_business_term_fk` FOREIGN KEY (`business_term_id`) REFERENCES `business_term` (`business_term_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The bcc_bizterm table stores information about the aggregation between the business term and BCC. TODO: Placeholder, definition is missing.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bcc_manifest`
--

DROP TABLE IF EXISTS `bcc_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bcc_manifest` (
  `bcc_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `release_id` bigint(20) unsigned DEFAULT NULL,
  `bcc_id` bigint(20) unsigned NOT NULL,
  `seq_key_id` bigint(20) unsigned DEFAULT NULL,
  `from_acc_manifest_id` bigint(20) unsigned NOT NULL,
  `to_bccp_manifest_id` bigint(20) unsigned NOT NULL,
  `conflict` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This indicates that there is a conflict between self and relationship.',
  `replacement_bcc_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement manifest if the record is deprecated.',
  `prev_bcc_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `next_bcc_manifest_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`bcc_manifest_id`),
  KEY `bcc_manifest_bcc_id_fk` (`bcc_id`),
  KEY `bcc_manifest_release_id_fk` (`release_id`),
  KEY `bcc_manifest_from_acc_manifest_id_fk` (`from_acc_manifest_id`),
  KEY `bcc_manifest_to_bccp_manifest_id_fk` (`to_bccp_manifest_id`),
  KEY `bcc_manifest_prev_bcc_manifest_id_fk` (`prev_bcc_manifest_id`),
  KEY `bcc_manifest_next_bcc_manifest_id_fk` (`next_bcc_manifest_id`),
  KEY `bcc_manifest_seq_key_id_fk` (`seq_key_id`),
  KEY `bcc_replacement_bcc_manifest_id_fk` (`replacement_bcc_manifest_id`),
  CONSTRAINT `bcc_manifest_bcc_id_fk` FOREIGN KEY (`bcc_id`) REFERENCES `bcc` (`bcc_id`),
  CONSTRAINT `bcc_manifest_from_acc_manifest_id_fk` FOREIGN KEY (`from_acc_manifest_id`) REFERENCES `acc_manifest` (`acc_manifest_id`),
  CONSTRAINT `bcc_manifest_next_bcc_manifest_id_fk` FOREIGN KEY (`next_bcc_manifest_id`) REFERENCES `bcc_manifest` (`bcc_manifest_id`),
  CONSTRAINT `bcc_manifest_prev_bcc_manifest_id_fk` FOREIGN KEY (`prev_bcc_manifest_id`) REFERENCES `bcc_manifest` (`bcc_manifest_id`),
  CONSTRAINT `bcc_manifest_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
  CONSTRAINT `bcc_manifest_seq_key_id_fk` FOREIGN KEY (`seq_key_id`) REFERENCES `seq_key` (`seq_key_id`),
  CONSTRAINT `bcc_manifest_to_bccp_manifest_id_fk` FOREIGN KEY (`to_bccp_manifest_id`) REFERENCES `bccp_manifest` (`bccp_manifest_id`),
  CONSTRAINT `bcc_replacement_bcc_manifest_id_fk` FOREIGN KEY (`replacement_bcc_manifest_id`) REFERENCES `bcc_manifest` (`bcc_manifest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bccp`
--

DROP TABLE IF EXISTS `bccp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bccp` (
  `bccp_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `property_term` varchar(100) NOT NULL COMMENT 'The property concept that the BCCP models.',
  `representation_term` varchar(20) NOT NULL COMMENT 'The representation term convey the format of the data the BCCP can take. The value is derived from the DT.DATA_TYPE_TERM of the associated BDT as referred to by the BDT_ID column.',
  `bdt_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key pointing to the DT table indicating the data typye or data format of the BCCP. Only DT_ID which DT_Type is BDT can be used.',
  `den` varchar(200) NOT NULL COMMENT 'The dictionary entry name of the BCCP. It is derived by PROPERTY_TERM + ". " + REPRESENTATION_TERM.',
  `definition` text COMMENT 'Description of the BCCP.',
  `definition_source` varchar(100) DEFAULT NULL COMMENT 'This is typically a URL identifying the source of the DEFINITION column.',
  `namespace_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the NAMESPACE table. This is the namespace to which the entity belongs. This namespace column is primarily used in the case the component is a user''s component because there is also a namespace assigned at the release level.',
  `is_deprecated` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  `replacement_bccp_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement if the record is deprecated.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the user who creates the entity. \n\nThis column never change between the history and the current record for a given revision. The history record should have the same value as that of its current record.',
  `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who has updated the record. \n\nIn the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the revision of the BCCP was created. \n\nThis never change for a revision.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.',
  `state` varchar(20) DEFAULT NULL COMMENT 'Deleted, WIP, Draft, QA, Candidate, Production, Release Draft, Published. This the revision life cycle state of the BCCP.\n\nState change can''t be undone. But the history record can still keep the records of when the state was changed.',
  `is_nillable` tinyint(1) NOT NULL COMMENT 'This is corresponding to the XML Schema nillable flag. Although the nillable may not apply to certain cases of the BCCP (e.g., when it is only used as XSD attribute), the value is default to false for simplification. ',
  `default_value` text COMMENT 'This column specifies the default value constraint. Default and fixed value constraints cannot be used at the same time.',
  `fixed_value` text COMMENT 'This column captures the fixed value constraint. Default and fixed value constraints cannot be used at the same time.',
  `prev_bccp_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the previous history record.',
  `next_bccp_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the next history record.',
  PRIMARY KEY (`bccp_id`),
  KEY `bccp_bdt_id_fk` (`bdt_id`),
  KEY `bccp_namespace_id_fk` (`namespace_id`),
  KEY `bccp_created_by_fk` (`created_by`),
  KEY `bccp_owner_user_id_fk` (`owner_user_id`),
  KEY `bccp_last_updated_by_fk` (`last_updated_by`),
  KEY `bccp_prev_bccp_id_fk` (`prev_bccp_id`),
  KEY `bccp_next_bccp_id_fk` (`next_bccp_id`),
  KEY `bccp_guid_idx` (`guid`),
  KEY `bccp_last_update_timestamp_desc_idx` (`last_update_timestamp`),
  KEY `bccp_replacement_bccp_id_fk` (`replacement_bccp_id`),
  CONSTRAINT `bccp_bdt_id_fk` FOREIGN KEY (`bdt_id`) REFERENCES `dt` (`dt_id`),
  CONSTRAINT `bccp_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bccp_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bccp_namespace_id_fk` FOREIGN KEY (`namespace_id`) REFERENCES `namespace` (`namespace_id`),
  CONSTRAINT `bccp_next_bccp_id_fk` FOREIGN KEY (`next_bccp_id`) REFERENCES `bccp` (`bccp_id`),
  CONSTRAINT `bccp_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bccp_prev_bccp_id_fk` FOREIGN KEY (`prev_bccp_id`) REFERENCES `bccp` (`bccp_id`),
  CONSTRAINT `bccp_replacement_bccp_id_fk` FOREIGN KEY (`replacement_bccp_id`) REFERENCES `bccp` (`bccp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='An BCCP specifies a property concept and data type associated with it. A BCCP can be then added as a property of an ACC.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bccp_manifest`
--

DROP TABLE IF EXISTS `bccp_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bccp_manifest` (
  `bccp_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `release_id` bigint(20) unsigned NOT NULL,
  `bccp_id` bigint(20) unsigned NOT NULL,
  `bdt_manifest_id` bigint(20) unsigned NOT NULL,
  `conflict` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This indicates that there is a conflict between self and relationship.',
  `log_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A foreign key pointed to a log for the current record.',
  `replacement_bccp_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement manifest if the record is deprecated.',
  `prev_bccp_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `next_bccp_manifest_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`bccp_manifest_id`),
  KEY `bccp_manifest_bccp_id_fk` (`bccp_id`),
  KEY `bccp_manifest_bdt_manifest_id_fk` (`bdt_manifest_id`),
  KEY `bccp_manifest_release_id_fk` (`release_id`),
  KEY `bccp_manifest_log_id_fk` (`log_id`),
  KEY `bccp_manifest_prev_bccp_manifest_id_fk` (`prev_bccp_manifest_id`),
  KEY `bccp_manifest_next_bccp_manifest_id_fk` (`next_bccp_manifest_id`),
  KEY `bccp_replacement_bccp_manifest_id_fk` (`replacement_bccp_manifest_id`),
  CONSTRAINT `bccp_manifest_bccp_id_fk` FOREIGN KEY (`bccp_id`) REFERENCES `bccp` (`bccp_id`),
  CONSTRAINT `bccp_manifest_bdt_manifest_id_fk` FOREIGN KEY (`bdt_manifest_id`) REFERENCES `dt_manifest` (`dt_manifest_id`),
  CONSTRAINT `bccp_manifest_log_id_fk` FOREIGN KEY (`log_id`) REFERENCES `log` (`log_id`),
  CONSTRAINT `bccp_manifest_next_bccp_manifest_id_fk` FOREIGN KEY (`next_bccp_manifest_id`) REFERENCES `bccp_manifest` (`bccp_manifest_id`),
  CONSTRAINT `bccp_manifest_prev_bccp_manifest_id_fk` FOREIGN KEY (`prev_bccp_manifest_id`) REFERENCES `bccp_manifest` (`bccp_manifest_id`),
  CONSTRAINT `bccp_manifest_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
  CONSTRAINT `bccp_replacement_bccp_manifest_id_fk` FOREIGN KEY (`replacement_bccp_manifest_id`) REFERENCES `bccp_manifest` (`bccp_manifest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bccp_manifest_tag`
--

DROP TABLE IF EXISTS `bccp_manifest_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bccp_manifest_tag` (
  `bccp_manifest_id` bigint(20) unsigned NOT NULL,
  `tag_id` bigint(20) unsigned NOT NULL,
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the record was first created.',
  PRIMARY KEY (`bccp_manifest_id`,`tag_id`),
  KEY `bccp_manifest_tag_bccp_manifest_id_fk` (`bccp_manifest_id`),
  KEY `bccp_manifest_tag_tag_id_fk` (`tag_id`),
  KEY `bccp_manifest_tag_created_by_fk` (`created_by`),
  CONSTRAINT `bccp_manifest_tag_bccp_manifest_id_fk` FOREIGN KEY (`bccp_manifest_id`) REFERENCES `bccp_manifest` (`bccp_manifest_id`),
  CONSTRAINT `bccp_manifest_tag_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `bccp_manifest_tag_tag_id_fk` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bdt_pri_restri`
--

DROP TABLE IF EXISTS `bdt_pri_restri`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bdt_pri_restri` (
  `bdt_pri_restri_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `bdt_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the DT_MANIFEST table. It shall point to only DT that is a BDT (not a CDT).',
  `cdt_awd_pri_xps_type_map_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the CDT_AWD_PRI_XPS_TYPE_MAP table.  It allows for a primitive restriction based on a built-in type of schema expressions.',
  `code_list_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the CODE_LIST_MANIFEST table.',
  `agency_id_list_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the AGENCY_ID_LIST_MANIFEST table. It is used in the case that the BDT content can be restricted to an agency identification.',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This allows overriding the default primitive assigned in the CDT_AWD_PRI_XPS_TYPE_MAP table. It typically indicates the most generic primtive for the data type.',
  PRIMARY KEY (`bdt_pri_restri_id`),
  KEY `bdt_pri_restri_cdt_awd_pri_xps_type_map_id_fk` (`cdt_awd_pri_xps_type_map_id`),
  KEY `bdt_pri_restri_bdt_manifest_id_fk` (`bdt_manifest_id`),
  KEY `bdt_pri_restri_code_list_manifest_id_fk` (`code_list_manifest_id`),
  KEY `bdt_pri_restri_agency_id_list_manifest_id_fk` (`agency_id_list_manifest_id`),
  CONSTRAINT `bdt_pri_restri_agency_id_list_manifest_id_fk` FOREIGN KEY (`agency_id_list_manifest_id`) REFERENCES `agency_id_list_manifest` (`agency_id_list_manifest_id`),
  CONSTRAINT `bdt_pri_restri_bdt_manifest_id_fk` FOREIGN KEY (`bdt_manifest_id`) REFERENCES `dt_manifest` (`dt_manifest_id`),
  CONSTRAINT `bdt_pri_restri_cdt_awd_pri_xps_type_map_id_fk` FOREIGN KEY (`cdt_awd_pri_xps_type_map_id`) REFERENCES `cdt_awd_pri_xps_type_map` (`cdt_awd_pri_xps_type_map_id`),
  CONSTRAINT `bdt_pri_restri_code_list_manifest_id_fk` FOREIGN KEY (`code_list_manifest_id`) REFERENCES `code_list_manifest` (`code_list_manifest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table captures the allowed primitives for a BDT. The allowed primitives are captured by three columns the CDT_AWD_PRI_XPS_TYPE_MAP_ID, CODE_LIST_ID, and AGENCY_ID_LIST_ID. The first column specifies the primitive by the built-in type of an expression language such as the XML Schema built-in type. The second specifies the primitive, which is a code list, while the last one specifies the primitive which is an agency identification list. Only one column among the three can have a value in a particular record.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bdt_sc_pri_restri`
--

DROP TABLE IF EXISTS `bdt_sc_pri_restri`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bdt_sc_pri_restri` (
  `bdt_sc_pri_restri_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `bdt_sc_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the DT_SC_MANIFEST table. It shall point to only DT that is a BDT (not a CDT).',
  `cdt_sc_awd_pri_xps_type_map_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This column is a forieng key to the CDT_SC_AWD_PRI_XPS_TYPE_MAP table. It allows for a primitive restriction based on a built-in type of schema expressions.',
  `code_list_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the CODE_LIST_MANIFEST table.',
  `agency_id_list_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the AGENCY_ID_LIST_MANIFEST table. It is used in the case that the BDT content can be restricted to an agency identification.',
  `is_default` tinyint(1) NOT NULL COMMENT 'This column specifies the default primitive for a BDT. It is typically the most generic primitive allowed for the BDT.',
  PRIMARY KEY (`bdt_sc_pri_restri_id`),
  KEY `bdt_sc_pri_restri_cdt_sc_awd_pri_xps_type_map_id_fk` (`cdt_sc_awd_pri_xps_type_map_id`),
  KEY `bdt_sc_pri_restri_bdt_manifest_id_fk` (`bdt_sc_manifest_id`),
  KEY `bdt_sc_pri_restri_code_list_manifest_id_fk` (`code_list_manifest_id`),
  KEY `bdt_sc_pri_restri_agency_id_list_manifest_id_fk` (`agency_id_list_manifest_id`),
  CONSTRAINT `bdt_sc_pri_restri_agency_id_list_manifest_id_fk` FOREIGN KEY (`agency_id_list_manifest_id`) REFERENCES `agency_id_list_manifest` (`agency_id_list_manifest_id`),
  CONSTRAINT `bdt_sc_pri_restri_bdt_manifest_id_fk` FOREIGN KEY (`bdt_sc_manifest_id`) REFERENCES `dt_sc_manifest` (`dt_sc_manifest_id`),
  CONSTRAINT `bdt_sc_pri_restri_cdt_sc_awd_pri_xps_type_map_id_fk` FOREIGN KEY (`cdt_sc_awd_pri_xps_type_map_id`) REFERENCES `cdt_sc_awd_pri_xps_type_map` (`cdt_sc_awd_pri_xps_type_map_id`),
  CONSTRAINT `bdt_sc_pri_restri_code_list_manifest_id_fk` FOREIGN KEY (`code_list_manifest_id`) REFERENCES `code_list_manifest` (`code_list_manifest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table is similar to the BDT_PRI_RESTRI table but it is for the BDT SC. The allowed primitives are captured by three columns the CDT_SC_AWD_PRI_XPS_TYPE_MAP, CODE_LIST_ID, and AGENCY_ID_LIST_ID. The first column specifies the primitive by the built-in type of an expression language such as the XML Schema built-in type. The second specifies the primitive, which is a code list, while the last one specifies the primitive which is an agency identification list. Only one column among the three can have a value in a particular record.\n\nIt should be noted that the table does not store the fact about primitive restriction hierarchical relationships. In other words, if a BDT SC is derived from another BDT SC and the derivative BDT SC applies some primitive restrictions, that relationship will not be explicitly stored. The derivative BDT SC points directly to the CDT_AWD_PRI_XPS_TYPE_MAP key rather than the BDT_SC_PRI_RESTRI key.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bie_usage_rule`
--

DROP TABLE IF EXISTS `bie_usage_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bie_user_ext_revision`
--

DROP TABLE IF EXISTS `bie_user_ext_revision`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bie_user_ext_revision` (
  `bie_user_ext_revision_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `ext_abie_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This points to an ABIE record corresponding to the EXTENSION_ACC_ID record. For example, this column can point to the ApplicationAreaExtension ABIE which is based on the ApplicationAreaExtension ACC (referred to by the EXT_ACC_ID column). This column can be NULL only when the extension is the AllExtension because there is no corresponding ABIE for the AllExtension ACC.',
  `ext_acc_id` bigint(20) unsigned NOT NULL COMMENT 'This points to an extension ACC on which the ABIE indicated by the EXT_ABIE_ID column is based. E.g. It may point to an ApplicationAreaExtension ACC, AllExtension ACC, ActualLedgerExtension ACC, etc. It should be noted that an ACC record pointed to must have the OAGIS_COMPONENT_TYPE = 2 (Extension).',
  `user_ext_acc_id` bigint(20) unsigned NOT NULL COMMENT 'This column points to the specific revision of a User Extension ACC (this is an ACC whose OAGIS_COMPONENT_TYPE = 4) currently used by the ABIE as indicated by the EXT_ABIE_ID or the by the TOP_LEVEL_ABIE_ID (in case of the AllExtension). ',
  `revised_indicator` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This column is a flag indicating to whether the User Extension ACC (as identified in the USER_EXT_ACC_ID column) has been revised, i.e., there is a newer version of the user extension ACC than the one currently used by the EXT_ABIE_ID. 0 means the USER_EXT_ACC_ID is current, 1 means it is not current.',
  `top_level_asbiep_id` bigint(20) unsigned NOT NULL COMMENT 'This is a foreign key to the top-level ASBIEP.',
  PRIMARY KEY (`bie_user_ext_revision_id`),
  KEY `bie_user_ext_revision_ext_abie_id_fk` (`ext_abie_id`),
  KEY `bie_user_ext_revision_ext_acc_id_fk` (`ext_acc_id`),
  KEY `bie_user_ext_revision_user_ext_acc_id_fk` (`user_ext_acc_id`),
  KEY `bie_user_ext_revision_top_level_asbiep_id_fk` (`top_level_asbiep_id`),
  CONSTRAINT `bie_user_ext_revision_ext_abie_id_fk` FOREIGN KEY (`ext_abie_id`) REFERENCES `abie` (`abie_id`),
  CONSTRAINT `bie_user_ext_revision_ext_acc_id_fk` FOREIGN KEY (`ext_acc_id`) REFERENCES `acc` (`acc_id`),
  CONSTRAINT `bie_user_ext_revision_top_level_asbiep_id_fk` FOREIGN KEY (`top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`),
  CONSTRAINT `bie_user_ext_revision_user_ext_acc_id_fk` FOREIGN KEY (`user_ext_acc_id`) REFERENCES `acc` (`acc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table is a log of events. It keeps track of the User Extension ACC (the specific revision) used by an Extension ABIE. This can be a named extension (such as ApplicationAreaExtension) or the AllExtension. The REVISED_INDICATOR flag is designed such that a revision of a User Extension can notify the user of a top-level ABIE by setting this flag to true. The TOP_LEVEL_ABIE_ID column makes it more efficient to when opening a top-level ABIE, the user can be notified of any new revision of the extension. A record in this table is created only when there is a user extension to the the OAGIS extension component/ACC.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `biz_ctx`
--

DROP TABLE IF EXISTS `biz_ctx`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_ctx` (
  `biz_ctx_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `biz_ctx_assignment`
--

DROP TABLE IF EXISTS `biz_ctx_assignment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_ctx_assignment` (
  `biz_ctx_assignment_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `biz_ctx_id` bigint(20) unsigned NOT NULL,
  `top_level_asbiep_id` bigint(20) unsigned NOT NULL COMMENT 'This is a foreign key to the top-level ASBIEP.',
  PRIMARY KEY (`biz_ctx_assignment_id`),
  UNIQUE KEY `biz_ctx_assignment_uk` (`biz_ctx_id`,`top_level_asbiep_id`),
  KEY `biz_ctx_id` (`biz_ctx_id`),
  KEY `biz_ctx_assignment_top_level_asbiep_id_fk` (`top_level_asbiep_id`),
  CONSTRAINT `biz_ctx_assignment_biz_ctx_id_fk` FOREIGN KEY (`biz_ctx_id`) REFERENCES `biz_ctx` (`biz_ctx_id`),
  CONSTRAINT `biz_ctx_assignment_top_level_asbiep_id_fk` FOREIGN KEY (`top_level_asbiep_id`) REFERENCES `top_level_asbiep` (`top_level_asbiep_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `biz_ctx_value`
--

DROP TABLE IF EXISTS `biz_ctx_value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `blob_content`
--

DROP TABLE IF EXISTS `blob_content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `blob_content` (
  `blob_content_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `content` mediumblob NOT NULL COMMENT 'The Blob content of the schema file.',
  PRIMARY KEY (`blob_content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table stores schemas whose content is only imported as a whole and is represented in Blob.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `blob_content_manifest`
--

DROP TABLE IF EXISTS `blob_content_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `blob_content_manifest` (
  `blob_content_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `blob_content_id` bigint(20) unsigned NOT NULL,
  `release_id` bigint(20) unsigned NOT NULL,
  `conflict` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This indicates that there is a conflict between self and relationship.',
  `prev_blob_content_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `next_blob_content_manifest_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`blob_content_manifest_id`),
  KEY `blob_content_manifest_release_id_fk` (`release_id`),
  KEY `blob_content_manifest_blob_content_id_fk` (`blob_content_id`),
  KEY `blob_content_manifest_prev_blob_content_manifest_id_fk` (`prev_blob_content_manifest_id`),
  KEY `blob_content_manifest_next_blob_content_manifest_id_fk` (`next_blob_content_manifest_id`),
  CONSTRAINT `blob_content_manifest_blob_content_id_fk` FOREIGN KEY (`blob_content_id`) REFERENCES `blob_content` (`blob_content_id`),
  CONSTRAINT `blob_content_manifest_next_blob_content_manifest_id_fk` FOREIGN KEY (`next_blob_content_manifest_id`) REFERENCES `blob_content_manifest` (`blob_content_manifest_id`),
  CONSTRAINT `blob_content_manifest_prev_blob_content_manifest_id_fk` FOREIGN KEY (`prev_blob_content_manifest_id`) REFERENCES `blob_content_manifest` (`blob_content_manifest_id`),
  CONSTRAINT `blob_content_manifest_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `business_term`
--

DROP TABLE IF EXISTS `business_term`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `business_term` (
  `business_term_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an Business term.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `business_term` varchar(255) NOT NULL COMMENT 'A main name of the business term',
  `definition` text COMMENT 'Definition of the business term.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the business term. The creator of the business term is also its owner by default.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the business term record. This may be the user who is in the same group as the creator.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the business term record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the business term was last updated.',
  `external_ref_uri` text NOT NULL COMMENT 'TODO: Definition is missing.',
  `external_ref_id` varchar(100) DEFAULT NULL COMMENT 'TODO: Definition is missing.',
  `comment` text COMMENT 'Comment of the business term.',
  PRIMARY KEY (`business_term_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The Business Term table stores information about the business term, which is usually associated to BIE or CC. TODO: Placeeholder, definition is missing.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cdt_awd_pri`
--

DROP TABLE IF EXISTS `cdt_awd_pri`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cdt_awd_pri_xps_type_map`
--

DROP TABLE IF EXISTS `cdt_awd_pri_xps_type_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cdt_awd_pri_xps_type_map` (
  `cdt_awd_pri_xps_type_map_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
  `cdt_awd_pri_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the CDT_AWD_PRI table.',
  `xbt_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key and to the XBT table. It identifies the XML schema built-in types that can be mapped to the CDT primivite identified in the CDT_AWD_PRI_ID column. The CDT primitives are typically broad and hence it usually maps to more than one XML schema built-in types.',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicating a default value domain mapping.',
  PRIMARY KEY (`cdt_awd_pri_xps_type_map_id`),
  KEY `cdt_awd_pri_xps_type_map_cdt_awd_pri_id_fk` (`cdt_awd_pri_id`),
  KEY `cdt_awd_pri_xps_type_map_xbt_id_fk` (`xbt_id`),
  CONSTRAINT `cdt_awd_pri_xps_type_map_cdt_awd_pri_id_fk` FOREIGN KEY (`cdt_awd_pri_id`) REFERENCES `cdt_awd_pri` (`cdt_awd_pri_id`),
  CONSTRAINT `cdt_awd_pri_xps_type_map_xbt_id_fk` FOREIGN KEY (`xbt_id`) REFERENCES `xbt` (`xbt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table allows for concrete mapping between the CDT Primitives and types in a particular expression such as XML Schema, JSON. At this point, it is not clear whether a separate table will be needed for each expression. The current table holds the map to XML Schema built-in types. \n\nFor each additional expression, a column similar to the XBT_ID column will need to be added to this table for mapping to data types in another expression.\n\nIf we use a separate table for each expression, then we need binding all the way to BDT (or even BBIE) for every new expression. That would be almost like just store a BDT file. But using a column may not work with all kinds of expressions, particulary if it does not map well to the XML schema data types. ';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cdt_pri`
--

DROP TABLE IF EXISTS `cdt_pri`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cdt_pri` (
  `cdt_pri_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
  `name` varchar(45) NOT NULL COMMENT 'Name of the CDT primitive per the CCTS datatype catalog, e.g., Decimal.',
  PRIMARY KEY (`cdt_pri_id`),
  UNIQUE KEY `cdt_pri_uk1` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table stores the CDT primitives.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cdt_ref_spec`
--

DROP TABLE IF EXISTS `cdt_ref_spec`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cdt_ref_spec` (
  `cdt_ref_spec_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `cdt_id` bigint(20) unsigned NOT NULL,
  `ref_spec_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`cdt_ref_spec_id`),
  KEY `cdt_ref_spec_cdt_id_fk` (`cdt_id`),
  KEY `cdt_ref_spec_ref_spec_id_fk` (`ref_spec_id`),
  CONSTRAINT `cdt_ref_spec_cdt_id_fk` FOREIGN KEY (`cdt_id`) REFERENCES `dt` (`dt_id`),
  CONSTRAINT `cdt_ref_spec_ref_spec_id_fk` FOREIGN KEY (`ref_spec_id`) REFERENCES `ref_spec` (`ref_spec_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cdt_sc_awd_pri`
--

DROP TABLE IF EXISTS `cdt_sc_awd_pri`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cdt_sc_awd_pri_xps_type_map`
--

DROP TABLE IF EXISTS `cdt_sc_awd_pri_xps_type_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cdt_sc_awd_pri_xps_type_map` (
  `cdt_sc_awd_pri_xps_type_map_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
  `cdt_sc_awd_pri_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the CDT_SC_AWD_PRI table.',
  `xbt_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the Xbt table. It identifies an XML schema built-in type that maps to the CDT SC Allowed Primitive identified in the CDT_SC_AWD_PRI column.',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicating a default value domain mapping.',
  PRIMARY KEY (`cdt_sc_awd_pri_xps_type_map_id`),
  KEY `cdt_sc_awd_pri_xps_type_map_cdt_sc_awd_pri_id_fk` (`cdt_sc_awd_pri_id`),
  KEY `cdt_sc_awd_pri_xps_type_map_xbt_id_fk` (`xbt_id`),
  CONSTRAINT `cdt_sc_awd_pri_xps_type_map_cdt_sc_awd_pri_id_fk` FOREIGN KEY (`cdt_sc_awd_pri_id`) REFERENCES `cdt_sc_awd_pri` (`cdt_sc_awd_pri_id`),
  CONSTRAINT `cdt_sc_awd_pri_xps_type_map_xbt_id_fk` FOREIGN KEY (`xbt_id`) REFERENCES `xbt` (`xbt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The purpose of this table is the same as that of the CDT_AWD_PRI_XPS_TYPE_MAP, but it is for the supplementary component (SC). It allows for the concrete mapping between the CDT Primitives and types in a particular expression such as XML Schema, JSON. ';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cdt_sc_ref_spec`
--

DROP TABLE IF EXISTS `cdt_sc_ref_spec`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cdt_sc_ref_spec` (
  `cdt_sc_ref_spec_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `cdt_sc_id` bigint(20) unsigned NOT NULL,
  `ref_spec_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`cdt_sc_ref_spec_id`),
  KEY `cdt_sc_ref_spec_cdt_sc_id_fk` (`cdt_sc_id`),
  KEY `cdt_sc_ref_spec_ref_spec_id_fk` (`ref_spec_id`),
  CONSTRAINT `cdt_sc_ref_spec_cdt_sc_id_fk` FOREIGN KEY (`cdt_sc_id`) REFERENCES `dt_sc` (`dt_sc_id`),
  CONSTRAINT `cdt_sc_ref_spec_ref_spec_id_fk` FOREIGN KEY (`ref_spec_id`) REFERENCES `ref_spec` (`ref_spec_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `code_list`
--

DROP TABLE IF EXISTS `code_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `code_list` (
  `code_list_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `enum_type_guid` varchar(41) DEFAULT NULL COMMENT 'In the OAGIS Model XML schema, a type, which keeps all the enumerated values, is  defined separately from the type that represents a code list. This only applies to some code lists. When that is the case, this column stores the GUID of that enumeration type.',
  `name` varchar(100) DEFAULT NULL COMMENT 'Name of the code list.',
  `list_id` varchar(100) NOT NULL COMMENT 'External identifier.',
  `version_id` varchar(100) NOT NULL COMMENT 'Code list version number.',
  `definition` text COMMENT 'Description of the code list.',
  `remark` varchar(225) DEFAULT NULL COMMENT 'Usage information about the code list.',
  `definition_source` varchar(100) DEFAULT NULL COMMENT 'This is typically a URL which indicates the source of the code list''s DEFINITION.',
  `namespace_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the NAMESPACE table. This is the namespace to which the entity belongs. This namespace column is primarily used in the case the component is a user''s component because there is also a namespace assigned at the release level.',
  `based_code_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is a foreign key to the CODE_LIST table itself. This identifies the code list on which this code list is based, if any. The derivation may be restriction and/or extension.',
  `extensible_indicator` tinyint(1) NOT NULL COMMENT 'This is a flag to indicate whether the code list is final and shall not be further derived.',
  `is_deprecated` tinyint(1) DEFAULT '0' COMMENT 'Indicates whether the code list is deprecated and should not be reused (i.e., no new reference to this record should be allowed).',
  `replacement_code_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement if the record is deprecated.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created the code list.',
  `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It identifies the user who last updated the code list.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the code list was created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the code list was last updated.',
  `state` varchar(20) DEFAULT NULL,
  `prev_code_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the previous history record.',
  `next_code_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the next history record.',
  PRIMARY KEY (`code_list_id`),
  KEY `code_list_based_code_list_id_fk` (`based_code_list_id`),
  KEY `code_list_created_by_fk` (`created_by`),
  KEY `code_list_last_updated_by_fk` (`last_updated_by`),
  KEY `code_list_owner_user_id_fk` (`owner_user_id`),
  KEY `code_list_prev_code_list_id_fk` (`prev_code_list_id`),
  KEY `code_list_next_code_list_id_fk` (`next_code_list_id`),
  KEY `code_list_namespace_id_fk` (`namespace_id`),
  KEY `code_list_replacement_code_list_id_fk` (`replacement_code_list_id`),
  CONSTRAINT `code_list_based_code_list_id_fk` FOREIGN KEY (`based_code_list_id`) REFERENCES `code_list` (`code_list_id`),
  CONSTRAINT `code_list_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `code_list_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `code_list_namespace_id_fk` FOREIGN KEY (`namespace_id`) REFERENCES `namespace` (`namespace_id`),
  CONSTRAINT `code_list_next_code_list_id_fk` FOREIGN KEY (`next_code_list_id`) REFERENCES `code_list` (`code_list_id`),
  CONSTRAINT `code_list_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `code_list_prev_code_list_id_fk` FOREIGN KEY (`prev_code_list_id`) REFERENCES `code_list` (`code_list_id`),
  CONSTRAINT `code_list_replacement_code_list_id_fk` FOREIGN KEY (`replacement_code_list_id`) REFERENCES `code_list` (`code_list_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table stores information about a code list. When a code list is derived from another code list, the whole set of code values belonging to the based code list will be copied.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `code_list_manifest`
--

DROP TABLE IF EXISTS `code_list_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `code_list_manifest` (
  `code_list_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `release_id` bigint(20) unsigned NOT NULL,
  `code_list_id` bigint(20) unsigned NOT NULL,
  `based_code_list_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `agency_id_list_value_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `conflict` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This indicates that there is a conflict between self and relationship.',
  `log_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A foreign key pointed to a log for the current record.',
  `replacement_code_list_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement manifest if the record is deprecated.',
  `prev_code_list_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `next_code_list_manifest_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`code_list_manifest_id`),
  KEY `code_list_manifest_code_list_id_fk` (`code_list_id`),
  KEY `code_list_manifest_based_code_list_manifest_id_fk` (`based_code_list_manifest_id`),
  KEY `code_list_manifest_release_id_fk` (`release_id`),
  KEY `code_list_manifest_log_id_fk` (`log_id`),
  KEY `code_list_manifest_prev_code_list_manifest_id_fk` (`prev_code_list_manifest_id`),
  KEY `code_list_manifest_next_code_list_manifest_id_fk` (`next_code_list_manifest_id`),
  KEY `code_list_replacement_code_list_manifest_id_fk` (`replacement_code_list_manifest_id`),
  KEY `code_list_agency_id_list_value_manifest_id_fk` (`agency_id_list_value_manifest_id`),
  CONSTRAINT `code_list_agency_id_list_value_manifest_id_fk` FOREIGN KEY (`agency_id_list_value_manifest_id`) REFERENCES `agency_id_list_value_manifest` (`agency_id_list_value_manifest_id`),
  CONSTRAINT `code_list_manifest_based_code_list_manifest_id_fk` FOREIGN KEY (`based_code_list_manifest_id`) REFERENCES `code_list_manifest` (`code_list_manifest_id`),
  CONSTRAINT `code_list_manifest_code_list_id_fk` FOREIGN KEY (`code_list_id`) REFERENCES `code_list` (`code_list_id`),
  CONSTRAINT `code_list_manifest_log_id_fk` FOREIGN KEY (`log_id`) REFERENCES `log` (`log_id`),
  CONSTRAINT `code_list_manifest_next_code_list_manifest_id_fk` FOREIGN KEY (`next_code_list_manifest_id`) REFERENCES `code_list_manifest` (`code_list_manifest_id`),
  CONSTRAINT `code_list_manifest_prev_code_list_manifest_id_fk` FOREIGN KEY (`prev_code_list_manifest_id`) REFERENCES `code_list_manifest` (`code_list_manifest_id`),
  CONSTRAINT `code_list_manifest_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
  CONSTRAINT `code_list_replacement_code_list_manifest_id_fk` FOREIGN KEY (`replacement_code_list_manifest_id`) REFERENCES `code_list_manifest` (`code_list_manifest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `code_list_value`
--

DROP TABLE IF EXISTS `code_list_value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `code_list_value` (
  `code_list_value_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `code_list_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the CODE_LIST table. It indicates the code list this code value belonging to.',
  `based_code_list_value_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the CODE_LIST_VALUE table itself. This column is used when the CODE_LIST is derived from the based CODE_LIST.',
  `value` tinytext NOT NULL COMMENT 'The code list value used in the instance data, e.g., EA, US-EN.',
  `meaning` varchar(100) DEFAULT NULL COMMENT 'The description or explanation of the code list value, e.g., ''Each'' for EA, ''English'' for EN.',
  `definition` text COMMENT 'Long description or explannation of the code list value, e.g., ''EA is a discrete quantity for counting each unit of an item, such as, 2 shampoo bottles, 3 box of cereals''.',
  `definition_source` varchar(100) DEFAULT NULL COMMENT 'This is typically a URL identifying the source of the DEFINITION column.',
  `is_deprecated` tinyint(1) DEFAULT '0' COMMENT 'Indicates whether the code list value is deprecated and should not be reused (i.e., no new reference to this record should be allowed).',
  `replacement_code_list_value_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement if the record is deprecated.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created the code list.',
  `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It identifies the user who last updated the code list.',
  `creation_timestamp` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Timestamp when the code list was created.',
  `last_update_timestamp` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Timestamp when the code list was last updated.',
  `prev_code_list_value_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the previous history record.',
  `next_code_list_value_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the next history record.',
  PRIMARY KEY (`code_list_value_id`),
  KEY `code_list_value_code_list_id_fk` (`code_list_id`),
  KEY `code_list_value_created_by_fk` (`created_by`),
  KEY `code_list_value_owner_user_id_fk` (`owner_user_id`),
  KEY `code_list_value_last_updated_by_fk` (`last_updated_by`),
  KEY `code_list_value_prev_code_list_value_id_fk` (`prev_code_list_value_id`),
  KEY `code_list_value_next_code_list_value_id_fk` (`next_code_list_value_id`),
  KEY `code_list_value_replacement_code_list_value_id_fk` (`replacement_code_list_value_id`),
  KEY `code_list_value_based_code_list_value_id_fk` (`based_code_list_value_id`),
  CONSTRAINT `code_list_value_based_code_list_value_id_fk` FOREIGN KEY (`based_code_list_value_id`) REFERENCES `code_list_value` (`code_list_value_id`),
  CONSTRAINT `code_list_value_code_list_id_fk` FOREIGN KEY (`code_list_id`) REFERENCES `code_list` (`code_list_id`),
  CONSTRAINT `code_list_value_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `code_list_value_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `code_list_value_next_code_list_value_id_fk` FOREIGN KEY (`next_code_list_value_id`) REFERENCES `code_list_value` (`code_list_value_id`),
  CONSTRAINT `code_list_value_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `code_list_value_prev_code_list_value_id_fk` FOREIGN KEY (`prev_code_list_value_id`) REFERENCES `code_list_value` (`code_list_value_id`),
  CONSTRAINT `code_list_value_replacement_code_list_value_id_fk` FOREIGN KEY (`replacement_code_list_value_id`) REFERENCES `code_list_value` (`code_list_value_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Each record in this table stores a code list value of a code list. A code list value may be inherited from another code list on which it is based. However, inherited value may be restricted (i.e., disabled and cannot be used) in this code list, i.e., the USED_INDICATOR = false. If the value cannot be used since the based code list, then the LOCKED_INDICATOR = TRUE, because the USED_INDICATOR of such code list value is FALSE by default and can no longer be changed.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `code_list_value_manifest`
--

DROP TABLE IF EXISTS `code_list_value_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `code_list_value_manifest` (
  `code_list_value_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `release_id` bigint(20) unsigned NOT NULL,
  `code_list_value_id` bigint(20) unsigned NOT NULL,
  `code_list_manifest_id` bigint(20) unsigned NOT NULL,
  `based_code_list_value_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `conflict` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This indicates that there is a conflict between self and relationship.',
  `replacement_code_list_value_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement manifest if the record is deprecated.',
  `prev_code_list_value_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `next_code_list_value_manifest_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`code_list_value_manifest_id`),
  KEY `code_list_value_manifest_code_list_value_id_fk` (`code_list_value_id`),
  KEY `code_list_value_manifest_release_id_fk` (`release_id`),
  KEY `code_list_value_manifest_code_list_manifest_id_fk` (`code_list_manifest_id`),
  KEY `code_list_value_manifest_prev_code_list_value_manifest_id_fk` (`prev_code_list_value_manifest_id`),
  KEY `code_list_value_manifest_next_code_list_value_manifest_id_fk` (`next_code_list_value_manifest_id`),
  KEY `code_list_value_replacement_code_list_value_manifest_id_fk` (`replacement_code_list_value_manifest_id`),
  KEY `code_list_value_manifest_based_code_list_value_manifest_id_fk` (`based_code_list_value_manifest_id`),
  CONSTRAINT `code_list_value_manifest_based_code_list_value_manifest_id_fk` FOREIGN KEY (`based_code_list_value_manifest_id`) REFERENCES `code_list_value_manifest` (`code_list_value_manifest_id`),
  CONSTRAINT `code_list_value_manifest_code_list_manifest_id_fk` FOREIGN KEY (`code_list_manifest_id`) REFERENCES `code_list_manifest` (`code_list_manifest_id`),
  CONSTRAINT `code_list_value_manifest_code_list_value_id_fk` FOREIGN KEY (`code_list_value_id`) REFERENCES `code_list_value` (`code_list_value_id`),
  CONSTRAINT `code_list_value_manifest_next_code_list_value_manifest_id_fk` FOREIGN KEY (`next_code_list_value_manifest_id`) REFERENCES `code_list_value_manifest` (`code_list_value_manifest_id`),
  CONSTRAINT `code_list_value_manifest_prev_code_list_value_manifest_id_fk` FOREIGN KEY (`prev_code_list_value_manifest_id`) REFERENCES `code_list_value_manifest` (`code_list_value_manifest_id`),
  CONSTRAINT `code_list_value_manifest_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
  CONSTRAINT `code_list_value_replacement_code_list_value_manifest_id_fk` FOREIGN KEY (`replacement_code_list_value_manifest_id`) REFERENCES `code_list_value_manifest` (`code_list_value_manifest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `comment_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `reference` varchar(100) CHARACTER SET ascii NOT NULL DEFAULT '',
  `comment` text,
  `is_hidden` tinyint(1) NOT NULL DEFAULT '0',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `prev_comment_id` bigint(20) unsigned DEFAULT NULL,
  `created_by` bigint(20) unsigned NOT NULL,
  `creation_timestamp` datetime(6) NOT NULL,
  `last_update_timestamp` datetime(6) NOT NULL,
  PRIMARY KEY (`comment_id`),
  KEY `reference` (`reference`),
  KEY `comment_created_by_fk` (`created_by`),
  KEY `comment_prev_comment_id_fk` (`prev_comment_id`),
  CONSTRAINT `comment_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `comment_prev_comment_id_fk` FOREIGN KEY (`prev_comment_id`) REFERENCES `comment` (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `configuration`
--

DROP TABLE IF EXISTS `configuration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `configuration` (
  `configuration_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
  `name` varchar(100) DEFAULT NULL COMMENT 'The name of configuration property.',
  `type` varchar(100) DEFAULT NULL COMMENT 'The type of configuration property.',
  `value` varchar(100) DEFAULT NULL COMMENT 'The value of configuration property.',
  PRIMARY KEY (`configuration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The table stores configuration properties of the application.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ctx_category`
--

DROP TABLE IF EXISTS `ctx_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ctx_category` (
  `ctx_category_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary, database key.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `name` varchar(45) DEFAULT NULL COMMENT 'Short name of the context category.',
  `description` text COMMENT 'Explanation of what the context category is.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created the context category.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It identifies the user who last updated the context category.',
  `creation_timestamp` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Timestamp when the context category was created.',
  `last_update_timestamp` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Timestamp when the context category was last updated.',
  PRIMARY KEY (`ctx_category_id`),
  UNIQUE KEY `ctx_category_uk1` (`guid`),
  KEY `ctx_category_created_by_fk` (`created_by`),
  KEY `ctx_category_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `ctx_category_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `ctx_category_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table captures the context category. Examples of context categories as described in the CCTS are business process, industry, etc.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ctx_scheme`
--

DROP TABLE IF EXISTS `ctx_scheme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ctx_scheme` (
  `ctx_scheme_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary, database key.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `scheme_id` varchar(45) NOT NULL COMMENT 'External identification of the scheme. ',
  `scheme_name` varchar(255) DEFAULT NULL COMMENT 'Pretty print name of the context scheme.',
  `description` text COMMENT 'Description of the context scheme.',
  `scheme_agency_id` varchar(45) NOT NULL COMMENT 'Identification of the agency maintaining the scheme. This column currently does not use the AGENCY_ID_LIST table. It is just a free form text at this point.',
  `scheme_version_id` varchar(45) NOT NULL COMMENT 'Version number of the context scheme.',
  `ctx_category_id` bigint(20) unsigned NOT NULL COMMENT 'This the foreign key to the CTX_CATEGORY table. It identifies the context category associated with this context scheme.',
  `code_list_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This is the foreign key to the CODE_LIST table. It identifies the code list associated with this context scheme.',
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ctx_scheme_value`
--

DROP TABLE IF EXISTS `ctx_scheme_value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ctx_scheme_value` (
  `ctx_scheme_value_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `value` varchar(100) NOT NULL DEFAULT '' COMMENT 'A short value for the scheme value similar to the code list value.',
  `meaning` text COMMENT 'The description, explanatiion of the scheme value.',
  `owner_ctx_scheme_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the CTX_SCHEME table. It identifies the context scheme, to which this scheme value belongs.',
  PRIMARY KEY (`ctx_scheme_value_id`),
  UNIQUE KEY `ctx_scheme_value_uk1` (`guid`),
  KEY `ctx_scheme_value_owner_ctx_scheme_id_fk` (`owner_ctx_scheme_id`),
  CONSTRAINT `ctx_scheme_value_owner_ctx_scheme_id_fk` FOREIGN KEY (`owner_ctx_scheme_id`) REFERENCES `ctx_scheme` (`ctx_scheme_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table stores the context scheme values for a particular context scheme in the CTX_SCHEME table.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dt`
--

DROP TABLE IF EXISTS `dt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dt` (
  `dt_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `data_type_term` varchar(45) DEFAULT NULL COMMENT 'This is the data type term assigned to the DT. The allowed set of data type terms are defined in the DTC specification. This column is derived from the Based_DT_ID when the column is not blank. ',
  `qualifier` varchar(100) DEFAULT NULL COMMENT 'This column shall be blank when the DT_TYPE is CDT. When the DT_TYPE is BDT, this is optional. If the column is not blank it is a qualified BDT. If blank then the row may be a default BDT or an unqualified BDT. Default BDT is OAGIS concrete implementation of the CDT, these are the DT with numbers in the name, e.g., CodeType_1E7368 (DEN is ''Code_1E7368. Type''). Default BDTs are almost like permutation of the CDT options into concrete data types. Unqualified BDT is a BDT that OAGIS model schema generally used for its canonical. A handful of default BDTs were selected; and each of them is wrapped with another type definition that has a simpler name such as CodeType and NormalizedString type - we call these "unqualified BDTs". ',
  `representation_term` varchar(100) DEFAULT NULL,
  `six_digit_id` varchar(45) DEFAULT NULL COMMENT 'The six number suffix comes from the UN/CEFACT XML Schema NDR.',
  `based_dt_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key pointing to the DT table itself. This column must be blank when the DT_TYPE is CDT. This column must not be blank when the DT_TYPE is BDT.',
  `den` varchar(200) NOT NULL COMMENT 'Dictionary Entry Name of the data type. ',
  `definition` text COMMENT 'Description of the data type.',
  `definition_source` varchar(200) DEFAULT NULL COMMENT 'This is typically a URL identifying the source of the DEFINITION column.',
  `namespace_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the NAMESPACE table. This is the namespace to which the entity belongs. This namespace column is primarily used in the case the component is a user''s component because there is also a namespace assigned at the release level.',
  `content_component_definition` text COMMENT 'Description of the content component of the data type.',
  `state` varchar(20) DEFAULT NULL COMMENT 'Deleted, WIP, Draft, QA, Candidate, Production, Release Draft, Published. This the revision life cycle state of the DT.\n\nState change can''t be undone. But the history record can still keep the records of when the state was changed.',
  `commonly_used` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This is a flag to indicate commonly used DT(s) by BCCPs.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this DT.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record. \n\nIn the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).',
  `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\\n\\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership. ',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the revision of the DT was created. \n\nThis never change for a revision.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.',
  `is_deprecated` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  `replacement_dt_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement if the record is deprecated.',
  `prev_dt_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the previous history record.',
  `next_dt_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the next history record.',
  PRIMARY KEY (`dt_id`),
  KEY `dt_based_dt_id_fk` (`based_dt_id`),
  KEY `dt_created_by_fk` (`created_by`),
  KEY `dt_last_updated_by_fk` (`last_updated_by`),
  KEY `dt_owner_user_id_fk` (`owner_user_id`),
  KEY `dt_namespace_id_fk` (`namespace_id`),
  KEY `dt_prev_dt_id_fk` (`prev_dt_id`),
  KEY `dt_next_dt_id_fk` (`next_dt_id`),
  KEY `dt_guid_idx` (`guid`),
  KEY `dt_last_update_timestamp_desc_idx` (`last_update_timestamp`),
  KEY `dt_replacement_dt_id_fk` (`replacement_dt_id`),
  CONSTRAINT `dt_based_dt_id_fk` FOREIGN KEY (`based_dt_id`) REFERENCES `dt` (`dt_id`),
  CONSTRAINT `dt_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `dt_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `dt_namespace_id_fk` FOREIGN KEY (`namespace_id`) REFERENCES `namespace` (`namespace_id`),
  CONSTRAINT `dt_next_dt_id_fk` FOREIGN KEY (`next_dt_id`) REFERENCES `dt` (`dt_id`),
  CONSTRAINT `dt_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `dt_prev_dt_id_fk` FOREIGN KEY (`prev_dt_id`) REFERENCES `dt` (`dt_id`),
  CONSTRAINT `dt_replacement_dt_id_fk` FOREIGN KEY (`replacement_dt_id`) REFERENCES `dt` (`dt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The DT table stores both CDT and BDT. The two types of DTs are differentiated by the TYPE column.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dt_manifest`
--

DROP TABLE IF EXISTS `dt_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dt_manifest` (
  `dt_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `release_id` bigint(20) unsigned NOT NULL,
  `dt_id` bigint(20) unsigned NOT NULL,
  `based_dt_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `conflict` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This indicates that there is a conflict between self and relationship.',
  `log_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A foreign key pointed to a log for the current record.',
  `replacement_dt_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement manifest if the record is deprecated.',
  `prev_dt_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `next_dt_manifest_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`dt_manifest_id`),
  KEY `dt_manifest_dt_id_fk` (`dt_id`),
  KEY `dt_manifest_release_id_fk` (`release_id`),
  KEY `dt_manifest_log_id_fk` (`log_id`),
  KEY `dt_manifest_prev_dt_manifest_id_fk` (`prev_dt_manifest_id`),
  KEY `dt_manifest_next_dt_manifest_id_fk` (`next_dt_manifest_id`),
  KEY `dt_replacement_dt_manifest_id_fk` (`replacement_dt_manifest_id`),
  KEY `dt_manifest_based_dt_manifest_id_fk` (`based_dt_manifest_id`),
  CONSTRAINT `dt_manifest_based_dt_manifest_id_fk` FOREIGN KEY (`based_dt_manifest_id`) REFERENCES `dt_manifest` (`dt_manifest_id`),
  CONSTRAINT `dt_manifest_dt_id_fk` FOREIGN KEY (`dt_id`) REFERENCES `dt` (`dt_id`),
  CONSTRAINT `dt_manifest_log_id_fk` FOREIGN KEY (`log_id`) REFERENCES `log` (`log_id`),
  CONSTRAINT `dt_manifest_next_dt_manifest_id_fk` FOREIGN KEY (`next_dt_manifest_id`) REFERENCES `dt_manifest` (`dt_manifest_id`),
  CONSTRAINT `dt_manifest_prev_dt_manifest_id_fk` FOREIGN KEY (`prev_dt_manifest_id`) REFERENCES `dt_manifest` (`dt_manifest_id`),
  CONSTRAINT `dt_manifest_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
  CONSTRAINT `dt_replacement_dt_manifest_id_fk` FOREIGN KEY (`replacement_dt_manifest_id`) REFERENCES `dt_manifest` (`dt_manifest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dt_manifest_tag`
--

DROP TABLE IF EXISTS `dt_manifest_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dt_manifest_tag` (
  `dt_manifest_id` bigint(20) unsigned NOT NULL,
  `tag_id` bigint(20) unsigned NOT NULL,
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the record was first created.',
  PRIMARY KEY (`dt_manifest_id`,`tag_id`),
  KEY `dt_manifest_tag_dt_manifest_id_fk` (`dt_manifest_id`),
  KEY `dt_manifest_tag_tag_id_fk` (`tag_id`),
  KEY `dt_manifest_tag_created_by_fk` (`created_by`),
  CONSTRAINT `dt_manifest_tag_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `dt_manifest_tag_dt_manifest_id_fk` FOREIGN KEY (`dt_manifest_id`) REFERENCES `dt_manifest` (`dt_manifest_id`),
  CONSTRAINT `dt_manifest_tag_tag_id_fk` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dt_sc`
--

DROP TABLE IF EXISTS `dt_sc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dt_sc` (
  `dt_sc_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `object_class_term` varchar(60) DEFAULT NULL COMMENT 'Object class term of the SC.',
  `property_term` varchar(60) DEFAULT NULL COMMENT 'Property term of the SC.',
  `representation_term` varchar(20) DEFAULT NULL COMMENT 'Representation of the supplementary component.',
  `definition` text COMMENT 'Description of the supplementary component.',
  `definition_source` varchar(200) DEFAULT NULL COMMENT 'This is typically a URL identifying the source of the DEFINITION column.',
  `owner_dt_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreigned key to the DT table indicating the data type, to which this supplementary component belongs.',
  `cardinality_min` int(11) NOT NULL DEFAULT '0' COMMENT 'The minimum occurrence constraint associated with the supplementary component. The valid values zero or one.',
  `cardinality_max` int(11) DEFAULT NULL COMMENT 'The maximum occurrence constraint associated with the supplementary component. The valid values are zero or one. Zero is used when the SC is restricted from an instantiation in the data type.',
  `based_dt_sc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the DT_SC table itself. This column is used when the SC is derived from the based DT.',
  `default_value` text COMMENT 'This column specifies the default value constraint. Default and fixed value constraints cannot be used at the same time.',
  `fixed_value` text COMMENT 'This column captures the fixed value constraint. Default and fixed value constraints cannot be used at the same time.',
  `is_deprecated` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Indicates whether this is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  `replacement_dt_sc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement if the record is deprecated.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created the code list.',
  `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn''t rollback the ownership.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It identifies the user who last updated the code list.',
  `creation_timestamp` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Timestamp when the code list was created.',
  `last_update_timestamp` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Timestamp when the code list was last updated.',
  `prev_dt_sc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the previous history record.',
  `next_dt_sc_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A self-foreign key to indicate the next history record.',
  PRIMARY KEY (`dt_sc_id`),
  KEY `dt_sc_owner_dt_id_fk` (`owner_dt_id`),
  KEY `dt_sc_based_dt_sc_id_fk` (`based_dt_sc_id`),
  KEY `dt_sc_guid_idx` (`guid`),
  KEY `dt_sc_replacement_dt_sc_id_fk` (`replacement_dt_sc_id`),
  KEY `dt_sc_created_by_fk` (`created_by`),
  KEY `dt_sc_owner_user_id_fk` (`owner_user_id`),
  KEY `dt_sc_last_updated_by_fk` (`last_updated_by`),
  KEY `dt_sc_prev_dt_sc_id_fk` (`prev_dt_sc_id`),
  KEY `dt_sc_next_dt_sc_id_fk` (`next_dt_sc_id`),
  CONSTRAINT `dt_sc_based_dt_sc_id_fk` FOREIGN KEY (`based_dt_sc_id`) REFERENCES `dt_sc` (`dt_sc_id`),
  CONSTRAINT `dt_sc_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `dt_sc_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `dt_sc_next_dt_sc_id_fk` FOREIGN KEY (`next_dt_sc_id`) REFERENCES `dt_sc` (`dt_sc_id`),
  CONSTRAINT `dt_sc_owner_dt_id_fk` FOREIGN KEY (`owner_dt_id`) REFERENCES `dt` (`dt_id`),
  CONSTRAINT `dt_sc_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `dt_sc_prev_dt_sc_id_fk` FOREIGN KEY (`prev_dt_sc_id`) REFERENCES `dt_sc` (`dt_sc_id`),
  CONSTRAINT `dt_sc_replacement_dt_sc_id_fk` FOREIGN KEY (`replacement_dt_sc_id`) REFERENCES `dt_sc` (`dt_sc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table represents the supplementary component (SC) of a DT. Revision is not tracked at the supplementary component. It is considered intrinsic part of the DT. In other words, when a new revision of a DT is created a new set of supplementary components is created along with it. ';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dt_sc_manifest`
--

DROP TABLE IF EXISTS `dt_sc_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dt_sc_manifest` (
  `dt_sc_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `release_id` bigint(20) unsigned NOT NULL,
  `dt_sc_id` bigint(20) unsigned NOT NULL,
  `owner_dt_manifest_id` bigint(20) unsigned NOT NULL,
  `based_dt_sc_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `conflict` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This indicates that there is a conflict between self and relationship.',
  `replacement_dt_sc_manifest_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This refers to a replacement manifest if the record is deprecated.',
  `prev_dt_sc_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `next_dt_sc_manifest_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`dt_sc_manifest_id`),
  KEY `dt_sc_manifest_dt_sc_id_fk` (`dt_sc_id`),
  KEY `dt_sc_manifest_release_id_fk` (`release_id`),
  KEY `dt_sc_manifest_owner_dt_manifest_id_fk` (`owner_dt_manifest_id`),
  KEY `dt_sc_prev_dt_sc_manifest_id_fk` (`prev_dt_sc_manifest_id`),
  KEY `dt_sc_next_dt_sc_manifest_id_fk` (`next_dt_sc_manifest_id`),
  KEY `dt_sc_replacement_dt_sc_manifest_id_fk` (`replacement_dt_sc_manifest_id`),
  KEY `based_dt_sc_manifest_id_fk` (`based_dt_sc_manifest_id`),
  CONSTRAINT `based_dt_sc_manifest_id_fk` FOREIGN KEY (`based_dt_sc_manifest_id`) REFERENCES `dt_sc_manifest` (`dt_sc_manifest_id`),
  CONSTRAINT `dt_sc_manifest_dt_sc_id_fk` FOREIGN KEY (`dt_sc_id`) REFERENCES `dt_sc` (`dt_sc_id`),
  CONSTRAINT `dt_sc_manifest_owner_dt_manifest_id_fk` FOREIGN KEY (`owner_dt_manifest_id`) REFERENCES `dt_manifest` (`dt_manifest_id`),
  CONSTRAINT `dt_sc_manifest_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
  CONSTRAINT `dt_sc_next_dt_sc_manifest_id_fk` FOREIGN KEY (`next_dt_sc_manifest_id`) REFERENCES `dt_sc_manifest` (`dt_sc_manifest_id`),
  CONSTRAINT `dt_sc_prev_dt_sc_manifest_id_fk` FOREIGN KEY (`prev_dt_sc_manifest_id`) REFERENCES `dt_sc_manifest` (`dt_sc_manifest_id`),
  CONSTRAINT `dt_sc_replacement_dt_sc_manifest_id_fk` FOREIGN KEY (`replacement_dt_sc_manifest_id`) REFERENCES `dt_sc_manifest` (`dt_sc_manifest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dt_usage_rule`
--

DROP TABLE IF EXISTS `dt_usage_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `exception`
--

DROP TABLE IF EXISTS `exception`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exception` (
  `exception_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Internal, primary database key.',
  `tag` varchar(50) DEFAULT NULL COMMENT 'A tag of the exception for the purpose of the searching facilitation',
  `message` text COMMENT 'The exception message.',
  `stacktrace` mediumblob COMMENT 'The serialized stacktrace object.',
  `created_by` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who is working on when the exception occurs.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the exception was created.',
  PRIMARY KEY (`exception_id`),
  KEY `exception_created_by_fk` (`created_by`),
  KEY `exception_tag_idx` (`tag`),
  CONSTRAINT `exception_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `log`
--

DROP TABLE IF EXISTS `log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `log` (
  `log_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `hash` char(40) CHARACTER SET ascii NOT NULL COMMENT 'The unique hash to identify the log.',
  `revision_num` int(10) unsigned NOT NULL DEFAULT '1' COMMENT 'This is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 1, 2, and so on.',
  `revision_tracking_num` int(10) unsigned NOT NULL DEFAULT '1' COMMENT 'This supports the ability to undo changes during a revision (life cycle of a revision is from the component''s WIP state to PUBLISHED state). REVISION_TRACKING_NUM can be 1, 2, and so on.',
  `log_action` varchar(20) DEFAULT NULL COMMENT 'This indicates the action associated with the record.',
  `reference` varchar(100) CHARACTER SET ascii NOT NULL DEFAULT '',
  `snapshot` json DEFAULT NULL,
  `prev_log_id` bigint(20) unsigned DEFAULT NULL,
  `next_log_id` bigint(20) unsigned DEFAULT NULL,
  `created_by` bigint(20) unsigned NOT NULL,
  `creation_timestamp` datetime(6) NOT NULL,
  PRIMARY KEY (`log_id`),
  KEY `reference` (`reference`),
  KEY `log_created_by_fk` (`created_by`),
  KEY `log_prev_log_id_fk` (`prev_log_id`),
  KEY `log_next_log_id_fk` (`next_log_id`),
  CONSTRAINT `log_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `log_next_log_id_fk` FOREIGN KEY (`next_log_id`) REFERENCES `log` (`log_id`),
  CONSTRAINT `log_prev_log_id_fk` FOREIGN KEY (`prev_log_id`) REFERENCES `log` (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `message`
--

DROP TABLE IF EXISTS `message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message` (
  `message_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `sender_id` bigint(20) unsigned NOT NULL COMMENT 'The user who created this record.',
  `recipient_id` bigint(20) unsigned NOT NULL COMMENT 'The user who is a target to possess this record.',
  `subject` text COMMENT 'A subject of the message',
  `body` mediumtext COMMENT 'A body of the message.',
  `body_content_type` varchar(50) NOT NULL DEFAULT 'text/plain' COMMENT 'A content type of the body',
  `is_read` tinyint(1) DEFAULT '0' COMMENT 'An indicator whether this record is read or not.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.',
  PRIMARY KEY (`message_id`),
  KEY `message_sender_id_fk` (`sender_id`),
  KEY `message_recipient_id_fk` (`recipient_id`),
  CONSTRAINT `message_recipient_id_fk` FOREIGN KEY (`recipient_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `message_sender_id_fk` FOREIGN KEY (`sender_id`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `module`
--

DROP TABLE IF EXISTS `module`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `module` (
  `module_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `module_set_id` bigint(20) unsigned NOT NULL COMMENT 'This indicates a module set.',
  `parent_module_id` bigint(20) unsigned DEFAULT NULL COMMENT 'This indicates a parent module id. root module will be NULL.',
  `type` varchar(45) NOT NULL COMMENT 'This is a type column for indicates module is FILE or DIRECTORY.',
  `path` text NOT NULL COMMENT 'Absolute path to the module.',
  `name` varchar(100) NOT NULL COMMENT 'The is the filename of the module. The reason to not including the extension is that the extension maybe dependent on the expression. For XML schema, ''.xsd'' maybe added; or for JSON, ''.json'' maybe added as the file extension.',
  `namespace_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Note that a release record has a namespace associated. The NAMESPACE_ID, if specified here, overrides the release''s namespace. However, the NAMESPACE_ID associated with the component takes the highest precedence.',
  `version_num` varchar(45) DEFAULT NULL COMMENT 'This is the version number to be assigned to the schema module.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this MODULE.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record. \n\nIn the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).',
  `owner_user_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table identifying the user who can update or delete the record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.',
  PRIMARY KEY (`module_id`),
  KEY `module_namespace_id_fk` (`namespace_id`),
  KEY `module_owner_user_id_fk` (`owner_user_id`),
  KEY `module_created_by_fk` (`created_by`),
  KEY `module_last_updated_by_fk` (`last_updated_by`),
  KEY `module_module_set_id_fk` (`module_set_id`),
  KEY `module_parent_module_id_fk` (`parent_module_id`),
  CONSTRAINT `module_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_module_set_id_fk` FOREIGN KEY (`module_set_id`) REFERENCES `module_set` (`module_set_id`),
  CONSTRAINT `module_namespace_id_fk` FOREIGN KEY (`namespace_id`) REFERENCES `namespace` (`namespace_id`),
  CONSTRAINT `module_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_parent_module_id_fk` FOREIGN KEY (`parent_module_id`) REFERENCES `module` (`module_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The module table stores information about a physical file, into which CC components will be generated during the expression generation.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `module_acc_manifest`
--

DROP TABLE IF EXISTS `module_acc_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `module_acc_manifest` (
  `module_acc_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
  `module_set_release_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the module set release record.',
  `acc_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the acc manifest record.',
  `module_id` bigint(20) unsigned NOT NULL COMMENT 'This indicates a module.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this record.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.',
  PRIMARY KEY (`module_acc_manifest_id`),
  KEY `module_acc_manifest_created_by_fk` (`created_by`),
  KEY `module_acc_manifest_last_updated_by_fk` (`last_updated_by`),
  KEY `module_acc_manifest_module_set_release_id_fk` (`module_set_release_id`),
  KEY `module_acc_manifest_acc_manifest_id_fk` (`acc_manifest_id`),
  KEY `module_acc_manifest_module_id_fk` (`module_id`),
  CONSTRAINT `module_acc_manifest_acc_manifest_id_fk` FOREIGN KEY (`acc_manifest_id`) REFERENCES `acc_manifest` (`acc_manifest_id`),
  CONSTRAINT `module_acc_manifest_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_acc_manifest_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_acc_manifest_module_id_fk` FOREIGN KEY (`module_id`) REFERENCES `module` (`module_id`),
  CONSTRAINT `module_acc_manifest_module_set_release_id_fk` FOREIGN KEY (`module_set_release_id`) REFERENCES `module_set_release` (`module_set_release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `module_agency_id_list_manifest`
--

DROP TABLE IF EXISTS `module_agency_id_list_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `module_agency_id_list_manifest` (
  `module_agency_id_list_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
  `module_set_release_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the module set release record.',
  `agency_id_list_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the code list manifest record.',
  `module_id` bigint(20) unsigned NOT NULL COMMENT 'This indicates a module.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this record.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.',
  PRIMARY KEY (`module_agency_id_list_manifest_id`),
  KEY `module_agency_id_list_manifest_created_by_fk` (`created_by`),
  KEY `module_agency_id_list_manifest_last_updated_by_fk` (`last_updated_by`),
  KEY `module_agency_id_list_manifest_module_set_release_id_fk` (`module_set_release_id`),
  KEY `module_agency_id_list_manifest_agency_id_list_manifest_id_fk` (`agency_id_list_manifest_id`),
  KEY `module_agency_id_list_manifest_module_id_fk` (`module_id`),
  CONSTRAINT `module_agency_id_list_manifest_agency_id_list_manifest_id_fk` FOREIGN KEY (`agency_id_list_manifest_id`) REFERENCES `agency_id_list_manifest` (`agency_id_list_manifest_id`),
  CONSTRAINT `module_agency_id_list_manifest_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_agency_id_list_manifest_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_agency_id_list_manifest_module_id_fk` FOREIGN KEY (`module_id`) REFERENCES `module` (`module_id`),
  CONSTRAINT `module_agency_id_list_manifest_module_set_release_id_fk` FOREIGN KEY (`module_set_release_id`) REFERENCES `module_set_release` (`module_set_release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `module_asccp_manifest`
--

DROP TABLE IF EXISTS `module_asccp_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `module_asccp_manifest` (
  `module_asccp_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
  `module_set_release_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the module set release record.',
  `asccp_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the asccp manifest record.',
  `module_id` bigint(20) unsigned NOT NULL COMMENT 'This indicates a module.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this record.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.',
  PRIMARY KEY (`module_asccp_manifest_id`),
  KEY `module_asccp_manifest_created_by_fk` (`created_by`),
  KEY `module_asccp_manifest_last_updated_by_fk` (`last_updated_by`),
  KEY `module_asccp_manifest_module_set_release_id_fk` (`module_set_release_id`),
  KEY `module_asccp_manifest_asccp_manifest_id_fk` (`asccp_manifest_id`),
  KEY `module_asccp_manifest_module_id_fk` (`module_id`),
  CONSTRAINT `module_asccp_manifest_asccp_manifest_id_fk` FOREIGN KEY (`asccp_manifest_id`) REFERENCES `asccp_manifest` (`asccp_manifest_id`),
  CONSTRAINT `module_asccp_manifest_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_asccp_manifest_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_asccp_manifest_module_id_fk` FOREIGN KEY (`module_id`) REFERENCES `module` (`module_id`),
  CONSTRAINT `module_asccp_manifest_module_set_release_id_fk` FOREIGN KEY (`module_set_release_id`) REFERENCES `module_set_release` (`module_set_release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `module_bccp_manifest`
--

DROP TABLE IF EXISTS `module_bccp_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `module_bccp_manifest` (
  `module_bccp_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
  `module_set_release_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the module set release record.',
  `bccp_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the bccp manifest record.',
  `module_id` bigint(20) unsigned NOT NULL COMMENT 'This indicates a module.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this record.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.',
  PRIMARY KEY (`module_bccp_manifest_id`),
  KEY `module_bccp_manifest_created_by_fk` (`created_by`),
  KEY `module_bccp_manifest_last_updated_by_fk` (`last_updated_by`),
  KEY `module_bccp_manifest_module_set_release_id_fk` (`module_set_release_id`),
  KEY `module_bccp_manifest_bccp_manifest_id_fk` (`bccp_manifest_id`),
  KEY `module_bccp_manifest_module_id_fk` (`module_id`),
  CONSTRAINT `module_bccp_manifest_bccp_manifest_id_fk` FOREIGN KEY (`bccp_manifest_id`) REFERENCES `bccp_manifest` (`bccp_manifest_id`),
  CONSTRAINT `module_bccp_manifest_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_bccp_manifest_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_bccp_manifest_module_id_fk` FOREIGN KEY (`module_id`) REFERENCES `module` (`module_id`),
  CONSTRAINT `module_bccp_manifest_module_set_release_id_fk` FOREIGN KEY (`module_set_release_id`) REFERENCES `module_set_release` (`module_set_release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `module_blob_content_manifest`
--

DROP TABLE IF EXISTS `module_blob_content_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `module_blob_content_manifest` (
  `module_blob_content_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
  `module_set_release_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the module set release record.',
  `blob_content_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the blob content manifest record.',
  `module_id` bigint(20) unsigned NOT NULL COMMENT 'This indicates a module.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this record.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.',
  PRIMARY KEY (`module_blob_content_manifest_id`),
  KEY `module_blob_content_manifest_created_by_fk` (`created_by`),
  KEY `mmodule_blob_content_manifest_last_updated_by_fk` (`last_updated_by`),
  KEY `module_blob_content_manifest_module_set_release_id_fk` (`module_set_release_id`),
  KEY `module_blob_content_manifest_blob_content_manifest_id_fk` (`blob_content_manifest_id`),
  KEY `module_blob_content_manifest_module_id_fk` (`module_id`),
  CONSTRAINT `module_blob_content_manifest_acc_manifest_id_fk` FOREIGN KEY (`blob_content_manifest_id`) REFERENCES `blob_content_manifest` (`blob_content_manifest_id`),
  CONSTRAINT `module_blob_content_manifest_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_blob_content_manifest_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_blob_content_manifest_module_id_fk` FOREIGN KEY (`module_id`) REFERENCES `module` (`module_id`),
  CONSTRAINT `module_blob_content_manifest_module_set_release_id_fk` FOREIGN KEY (`module_set_release_id`) REFERENCES `module_set_release` (`module_set_release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `module_code_list_manifest`
--

DROP TABLE IF EXISTS `module_code_list_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `module_code_list_manifest` (
  `module_code_list_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
  `module_set_release_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the module set release record.',
  `code_list_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the code list manifest record.',
  `module_id` bigint(20) unsigned NOT NULL COMMENT 'This indicates a module.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this record.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.',
  PRIMARY KEY (`module_code_list_manifest_id`),
  KEY `module_code_list_manifest_created_by_fk` (`created_by`),
  KEY `module_code_list_manifest_last_updated_by_fk` (`last_updated_by`),
  KEY `module_code_list_manifest_module_set_release_id_fk` (`module_set_release_id`),
  KEY `module_code_list_manifest_code_list_manifest_id_fk` (`code_list_manifest_id`),
  KEY `module_code_list_manifest_module_id_fk` (`module_id`),
  CONSTRAINT `module_code_list_manifest_code_list_manifest_id_fk` FOREIGN KEY (`code_list_manifest_id`) REFERENCES `code_list_manifest` (`code_list_manifest_id`),
  CONSTRAINT `module_code_list_manifest_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_code_list_manifest_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_code_list_manifest_module_id_fk` FOREIGN KEY (`module_id`) REFERENCES `module` (`module_id`),
  CONSTRAINT `module_code_list_manifest_module_set_release_id_fk` FOREIGN KEY (`module_set_release_id`) REFERENCES `module_set_release` (`module_set_release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `module_dt_manifest`
--

DROP TABLE IF EXISTS `module_dt_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `module_dt_manifest` (
  `module_dt_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
  `module_set_release_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the module set release record.',
  `dt_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the dt manifest record.',
  `module_id` bigint(20) unsigned NOT NULL COMMENT 'This indicates a module.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this record.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.',
  PRIMARY KEY (`module_dt_manifest_id`),
  KEY `module_dt_manifest_created_by_fk` (`created_by`),
  KEY `module_dt_manifest_last_updated_by_fk` (`last_updated_by`),
  KEY `module_dt_manifest_module_set_release_id_fk` (`module_set_release_id`),
  KEY `module_dt_manifest_dt_manifest_id_fk` (`dt_manifest_id`),
  KEY `module_dt_manifest_module_id_fk` (`module_id`),
  CONSTRAINT `module_dt_manifest_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_dt_manifest_dt_manifest_id_fk` FOREIGN KEY (`dt_manifest_id`) REFERENCES `dt_manifest` (`dt_manifest_id`),
  CONSTRAINT `module_dt_manifest_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_dt_manifest_module_id_fk` FOREIGN KEY (`module_id`) REFERENCES `module` (`module_id`),
  CONSTRAINT `module_dt_manifest_module_set_release_id_fk` FOREIGN KEY (`module_set_release_id`) REFERENCES `module_set_release` (`module_set_release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `module_set`
--

DROP TABLE IF EXISTS `module_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `module_set` (
  `module_set_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `name` varchar(100) NOT NULL COMMENT 'This is the name of the module set.',
  `description` text COMMENT 'Description or explanation about the module set or use of the module set.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this MODULE_SET.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.',
  PRIMARY KEY (`module_set_id`),
  KEY `module_set_created_by_fk` (`created_by`),
  KEY `module_set_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `module_set_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_set_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `module_set_release`
--

DROP TABLE IF EXISTS `module_set_release`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `module_set_release` (
  `module_set_release_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
  `module_set_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the module set.',
  `release_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the release.',
  `name` varchar(100) NOT NULL COMMENT 'This is the name of the module set release.',
  `description` text COMMENT 'Description or explanation about the module set release.',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'It would be a default module set if this indicator is checked. Otherwise, it would be an optional.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this MODULE_SET_RELEASE.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.',
  PRIMARY KEY (`module_set_release_id`),
  KEY `module_set_release_module_set_id_fk` (`module_set_id`),
  KEY `module_set_release_release_id_fk` (`release_id`),
  KEY `module_set_release_assignment_created_by_fk` (`created_by`),
  KEY `module_set_release_assignment_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `module_set_release_assignment_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_set_release_assignment_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_set_release_module_set_id_fk` FOREIGN KEY (`module_set_id`) REFERENCES `module_set` (`module_set_id`),
  CONSTRAINT `module_set_release_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `module_xbt_manifest`
--

DROP TABLE IF EXISTS `module_xbt_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `module_xbt_manifest` (
  `module_xbt_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
  `module_set_release_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the module set release record.',
  `xbt_manifest_id` bigint(20) unsigned NOT NULL COMMENT 'A foreign key of the xbt manifest record.',
  `module_id` bigint(20) unsigned NOT NULL COMMENT 'This indicates a module.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table. It indicates the user who created this record.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table referring to the last user who updated the record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.',
  PRIMARY KEY (`module_xbt_manifest_id`),
  KEY `module_xbt_manifest_created_by_fk` (`created_by`),
  KEY `module_xbt_manifest_last_updated_by_fk` (`last_updated_by`),
  KEY `module_xbt_manifest_module_set_release_id_fk` (`module_set_release_id`),
  KEY `module_xbt_manifest_bccp_manifest_id_fk` (`xbt_manifest_id`),
  KEY `module_xbt_manifest_module_id_fk` (`module_id`),
  CONSTRAINT `module_xbt_manifest_bccp_manifest_id_fk` FOREIGN KEY (`xbt_manifest_id`) REFERENCES `xbt_manifest` (`xbt_manifest_id`),
  CONSTRAINT `module_xbt_manifest_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_xbt_manifest_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `module_xbt_manifest_module_id_fk` FOREIGN KEY (`module_id`) REFERENCES `module` (`module_id`),
  CONSTRAINT `module_xbt_manifest_module_set_release_id_fk` FOREIGN KEY (`module_set_release_id`) REFERENCES `module_set_release` (`module_set_release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `namespace`
--

DROP TABLE IF EXISTS `namespace`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
  UNIQUE KEY `namespace_uk1` (`uri`),
  KEY `namespace_owner_user_id_fk` (`owner_user_id`),
  KEY `namespace_created_by_fk` (`created_by`),
  KEY `namespace_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `namespace_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `namespace_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `namespace_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table stores information about a namespace. Namespace is the namespace as in the XML schema specification.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `oauth2_app`
--

DROP TABLE IF EXISTS `oauth2_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oauth2_app` (
  `oauth2_app_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `provider_name` varchar(100) NOT NULL,
  `issuer_uri` varchar(200) DEFAULT NULL,
  `authorization_uri` varchar(200) DEFAULT NULL,
  `token_uri` varchar(200) DEFAULT NULL,
  `user_info_uri` varchar(200) DEFAULT NULL,
  `jwk_set_uri` varchar(200) DEFAULT NULL,
  `redirect_uri` varchar(200) NOT NULL,
  `end_session_endpoint` varchar(200) DEFAULT NULL,
  `client_id` varchar(200) NOT NULL,
  `client_secret` varchar(200) NOT NULL,
  `client_authentication_method` varchar(50) NOT NULL,
  `authorization_grant_type` varchar(50) NOT NULL,
  `prompt` varchar(20) DEFAULT NULL,
  `display_provider_name` varchar(100) DEFAULT NULL,
  `background_color` varchar(50) DEFAULT NULL,
  `font_color` varchar(50) DEFAULT NULL,
  `display_order` int(11) DEFAULT '0',
  `is_disabled` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`oauth2_app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `oauth2_app_scope`
--

DROP TABLE IF EXISTS `oauth2_app_scope`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oauth2_app_scope` (
  `oauth2_app_scope_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `oauth2_app_id` bigint(20) unsigned NOT NULL,
  `scope` varchar(100) NOT NULL,
  PRIMARY KEY (`oauth2_app_scope_id`),
  KEY `oauth2_app_scope_oauth2_app_id_fk` (`oauth2_app_id`),
  CONSTRAINT `oauth2_app_scope_oauth2_app_id_fk` FOREIGN KEY (`oauth2_app_id`) REFERENCES `oauth2_app` (`oauth2_app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ref_spec`
--

DROP TABLE IF EXISTS `ref_spec`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ref_spec` (
  `ref_spec_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `spec` varchar(30) NOT NULL DEFAULT '',
  PRIMARY KEY (`ref_spec_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `release`
--

DROP TABLE IF EXISTS `release`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `release` (
  `release_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'RELEASE_ID must be an incremental integer. RELEASE_ID that is more than another RELEASE_ID is interpreted to be released later than the other.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `release_num` varchar(45) DEFAULT NULL COMMENT 'Release number such has 10.0, 10.1, etc. ',
  `release_note` longtext COMMENT 'Description or note associated with the release.',
  `release_license` longtext COMMENT 'License associated with the release.',
  `namespace_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the NAMESPACE table. It identifies the namespace used with the release. It is particularly useful for a library that uses a single namespace such like the OAGIS 10.x. A library that uses multiple namespace but has a main namespace may also use this column as a specific namespace can be override at the module level.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table identifying user who created the namespace.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the APP_USER table identifying the user who last updated the record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the record was last updated.',
  `state` varchar(20) DEFAULT 'Initialized' COMMENT 'This indicates the revision life cycle state of the Release.',
  PRIMARY KEY (`release_id`),
  KEY `release_namespace_id_fk` (`namespace_id`),
  KEY `release_created_by_fk` (`created_by`),
  KEY `release_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `release_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `release_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `release_namespace_id_fk` FOREIGN KEY (`namespace_id`) REFERENCES `namespace` (`namespace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The is table store the release information.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `seq_key`
--

DROP TABLE IF EXISTS `seq_key`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seq_key` (
  `seq_key_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `from_acc_manifest_id` bigint(20) unsigned NOT NULL,
  `ascc_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `bcc_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `prev_seq_key_id` bigint(20) unsigned DEFAULT NULL,
  `next_seq_key_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`seq_key_id`),
  KEY `seq_key_from_acc_manifest_id` (`from_acc_manifest_id`),
  KEY `seq_key_ascc_manifest_id` (`ascc_manifest_id`),
  KEY `seq_key_bcc_manifest_id` (`bcc_manifest_id`),
  KEY `seq_key_prev_seq_key_id_fk` (`prev_seq_key_id`),
  KEY `seq_key_next_seq_key_id_fk` (`next_seq_key_id`),
  CONSTRAINT `seq_key_ascc_manifest_id_fk` FOREIGN KEY (`ascc_manifest_id`) REFERENCES `ascc_manifest` (`ascc_manifest_id`),
  CONSTRAINT `seq_key_bcc_manifest_id_fk` FOREIGN KEY (`bcc_manifest_id`) REFERENCES `bcc_manifest` (`bcc_manifest_id`),
  CONSTRAINT `seq_key_from_acc_manifest_id_fk` FOREIGN KEY (`from_acc_manifest_id`) REFERENCES `acc_manifest` (`acc_manifest_id`),
  CONSTRAINT `seq_key_next_seq_key_id_fk` FOREIGN KEY (`next_seq_key_id`) REFERENCES `seq_key` (`seq_key_id`),
  CONSTRAINT `seq_key_prev_seq_key_id_fk` FOREIGN KEY (`prev_seq_key_id`) REFERENCES `seq_key` (`seq_key_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tag`
--

DROP TABLE IF EXISTS `tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tag` (
  `tag_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'An internal, primary database key of a tag record.',
  `name` varchar(100) NOT NULL COMMENT 'The name of the tag.',
  `description` text COMMENT 'The description of the tag.',
  `text_color` varchar(10) NOT NULL COMMENT 'The text color of the tag.',
  `background_color` varchar(10) NOT NULL COMMENT 'The background color of the tag.',
  `created_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the user who creates the tag record.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated the tag record.',
  `creation_timestamp` datetime(6) NOT NULL COMMENT 'Timestamp when the tag record was first created.',
  `last_update_timestamp` datetime(6) NOT NULL COMMENT 'The timestamp when the tag was last updated.',
  PRIMARY KEY (`tag_id`),
  KEY `tag_created_by_fk` (`created_by`),
  KEY `tag_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `tag_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `tag_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tenant`
--

DROP TABLE IF EXISTS `tenant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant` (
  `tenant_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
  `name` varchar(100) DEFAULT NULL COMMENT 'The name of the tenant.',
  PRIMARY KEY (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table about the user tenant role.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tenant_business_ctx`
--

DROP TABLE IF EXISTS `tenant_business_ctx`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_business_ctx` (
  `tenant_business_ctx_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
  `tenant_id` bigint(20) unsigned NOT NULL COMMENT 'Tenant role.',
  `biz_ctx_id` bigint(20) unsigned NOT NULL COMMENT 'Concrete business context for the company.',
  PRIMARY KEY (`tenant_business_ctx_id`),
  UNIQUE KEY `tenant_business_ctx_pair` (`tenant_id`,`biz_ctx_id`),
  KEY `tenant_business_ctx_tenant_id_fk` (`tenant_id`),
  KEY `organization_business_ctx_biz_ctx_id_fk` (`biz_ctx_id`),
  CONSTRAINT `organization_business_ctx_biz_ctx_id_fk` FOREIGN KEY (`biz_ctx_id`) REFERENCES `biz_ctx` (`biz_ctx_id`),
  CONSTRAINT `tenant_business_ctx_tenant_id_fk` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table captures the tenant role and theirs business contexts.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `top_level_asbiep`
--

DROP TABLE IF EXISTS `top_level_asbiep`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `top_level_asbiep` (
  `top_level_asbiep_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an top-level ASBIEP.',
  `asbiep_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the ASBIEP table pointing to a record which is a top-level ASBIEP.',
  `owner_user_id` bigint(20) unsigned NOT NULL,
  `last_update_timestamp` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'The timestamp when among all related bie records was last updated.',
  `last_updated_by` bigint(20) unsigned NOT NULL COMMENT 'A foreign key referring to the last user who has updated any related bie records.',
  `release_id` bigint(20) unsigned NOT NULL COMMENT 'Foreign key to the RELEASE table. It identifies the release, for which this module is associated.',
  `version` varchar(45) DEFAULT NULL COMMENT 'This column hold a version number assigned by the user. This column is only used by the top-level ASBIEP. No format of version is enforced.',
  `status` varchar(45) DEFAULT NULL COMMENT 'This is different from the STATE column which is CRUD life cycle of an entity. The use case for this is to allow the user to indicate the usage status of a top-level ASBIEP (a profile BOD). An integration architect can use this column. Example values are ?Prototype?, ?Test?, and ?Production?. Only the top-level ASBIEP can use this field.',
  `state` varchar(20) DEFAULT NULL,
  `inverse_mode` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'If this is true, all BIEs not edited by users under this TOP_LEVEL_ASBIEP will be treated as used BIEs.',
  PRIMARY KEY (`top_level_asbiep_id`),
  KEY `top_level_asbiep_asbiep_id_fk` (`asbiep_id`),
  KEY `top_level_asbiep_owner_user_id_fk` (`owner_user_id`),
  KEY `top_level_asbiep_release_id_fk` (`release_id`),
  KEY `top_level_asbiep_last_updated_by_fk` (`last_updated_by`),
  CONSTRAINT `top_level_asbiep_asbiep_id_fk` FOREIGN KEY (`asbiep_id`) REFERENCES `asbiep` (`asbiep_id`),
  CONSTRAINT `top_level_asbiep_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `top_level_asbiep_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `top_level_asbiep_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table indexes the ASBIEP which is a top-level ASBIEP. This table and the owner_top_level_asbiep_id column in all BIE tables allow all related BIEs to be retrieved all at once speeding up the profile BOD transactions.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usage_rule`
--

DROP TABLE IF EXISTS `usage_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usage_rule` (
  `usage_rule_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key of the usage rule.',
  `name` text COMMENT 'Short nmenomic name of the usage rule.',
  `condition_type` int(11) NOT NULL COMMENT 'Condition type according to the CC specification. It is a value list column. 0 = pre-condition, 1 = post-condition, 2 = invariant.',
  PRIMARY KEY (`usage_rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table captures a usage rule information. A usage rule may be expressed in multiple expressions. Each expression is captured in the USAGE_RULE_EXPRESSION table. To capture a description of a usage rule, create a usage rule expression with the unstructured constraint type.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usage_rule_expression`
--

DROP TABLE IF EXISTS `usage_rule_expression`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usage_rule_expression` (
  `usage_rule_expression_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key of the usage rule expression',
  `constraint_type` int(11) NOT NULL COMMENT 'Constraint type according to the CC spec. It represents the expression language (syntax) used in the CONSTRAINT column. It is a value list column. 0 = ''Unstructured'' which is basically a description of the rule, 1 = ''Schematron''.',
  `constraint_text` text NOT NULL COMMENT 'This column capture the constraint expressing the usage rule. In other words, this is the expression.',
  `represented_usage_rule_id` bigint(20) unsigned NOT NULL COMMENT 'The usage rule which the expression represents',
  PRIMARY KEY (`usage_rule_expression_id`),
  KEY `usage_rule_expression_represented_usage_rule_id_fk` (`represented_usage_rule_id`),
  CONSTRAINT `usage_rule_expression_represented_usage_rule_id_fk` FOREIGN KEY (`represented_usage_rule_id`) REFERENCES `usage_rule` (`usage_rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The USAGE_RULE_EXPRESSION provides a representation of a usage rule in a particular syntax indicated by the CONSTRAINT_TYPE column. One of the syntaxes can be unstructured, which works a description of the usage rule.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_tenant`
--

DROP TABLE IF EXISTS `user_tenant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_tenant` (
  `user_tenant_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary key column.',
  `tenant_id` bigint(20) unsigned NOT NULL COMMENT 'Assigned tenant to the user.',
  `app_user_id` bigint(20) unsigned NOT NULL COMMENT 'Application user.',
  PRIMARY KEY (`user_tenant_id`),
  UNIQUE KEY `user_tenant_pair` (`tenant_id`,`app_user_id`),
  KEY `user_tenant_tenant_id_fk` (`tenant_id`),
  KEY `user_tenant_tenant_id_app_user_id_fk` (`app_user_id`),
  CONSTRAINT `user_tenant_tenant_id_app_user_id_fk` FOREIGN KEY (`app_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `user_tenant_tenant_id_fk` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table captures the tenant roles of the user';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `xbt`
--

DROP TABLE IF EXISTS `xbt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xbt` (
  `xbt_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary, internal database key.',
  `guid` char(32) CHARACTER SET ascii NOT NULL COMMENT 'A globally unique identifier (GUID).',
  `name` varchar(45) DEFAULT NULL COMMENT 'Human understandable name of the built-in type.',
  `builtIn_type` varchar(45) DEFAULT NULL COMMENT 'Built-in type as it should appear in the XML schema including the namespace prefix. Namespace prefix for the XML schema namespace is assumed to be ''xsd'' and a default prefix for the OAGIS built-int type.',
  `jbt_draft05_map` varchar(500) DEFAULT NULL,
  `openapi30_map` varchar(500) DEFAULT NULL,
  `subtype_of_xbt_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Foreign key to the XBT table itself. It indicates a super type of this XSD built-in type.',
  `schema_definition` text,
  `revision_doc` text,
  `state` int(11) DEFAULT NULL,
  `created_by` bigint(20) unsigned NOT NULL,
  `owner_user_id` bigint(20) unsigned NOT NULL,
  `last_updated_by` bigint(20) unsigned NOT NULL,
  `creation_timestamp` datetime(6) NOT NULL,
  `last_update_timestamp` datetime(6) NOT NULL,
  `is_deprecated` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`xbt_id`),
  KEY `xbt_subtype_of_xbt_id_fk` (`subtype_of_xbt_id`),
  KEY `xbt_created_by_fk` (`created_by`),
  KEY `xbt_last_updated_by_fk` (`last_updated_by`),
  KEY `xbt_owner_user_id_fk` (`owner_user_id`),
  KEY `xbt_guid_idx` (`guid`),
  KEY `xbt_last_update_timestamp_desc_idx` (`last_update_timestamp`),
  CONSTRAINT `xbt_created_by_fk` FOREIGN KEY (`created_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `xbt_last_updated_by_fk` FOREIGN KEY (`last_updated_by`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `xbt_owner_user_id_fk` FOREIGN KEY (`owner_user_id`) REFERENCES `app_user` (`app_user_id`),
  CONSTRAINT `xbt_subtype_of_xbt_id_fk` FOREIGN KEY (`subtype_of_xbt_id`) REFERENCES `xbt` (`xbt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table stores XML schema built-in types and OAGIS built-in types. OAGIS built-in types are those types defined in the XMLSchemaBuiltinType and the XMLSchemaBuiltinType Patterns schemas.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `xbt_manifest`
--

DROP TABLE IF EXISTS `xbt_manifest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xbt_manifest` (
  `xbt_manifest_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `release_id` bigint(20) unsigned NOT NULL,
  `xbt_id` bigint(20) unsigned NOT NULL,
  `conflict` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'This indicates that there is a conflict between self and relationship.',
  `log_id` bigint(20) unsigned DEFAULT NULL COMMENT 'A foreign key pointed to a log for the current record.',
  `prev_xbt_manifest_id` bigint(20) unsigned DEFAULT NULL,
  `next_xbt_manifest_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`xbt_manifest_id`),
  KEY `xbt_manifest_xbt_id_fk` (`xbt_id`),
  KEY `xbt_manifest_release_id_fk` (`release_id`),
  KEY `xbt_manifest_log_id_fk` (`log_id`),
  KEY `xbt_manifest_prev_xbt_manifest_id_fk` (`prev_xbt_manifest_id`),
  KEY `xbt_manifest_next_xbt_manifest_id_fk` (`next_xbt_manifest_id`),
  CONSTRAINT `xbt_manifest_log_id_fk` FOREIGN KEY (`log_id`) REFERENCES `log` (`log_id`),
  CONSTRAINT `xbt_manifest_next_xbt_manifest_id_fk` FOREIGN KEY (`next_xbt_manifest_id`) REFERENCES `xbt_manifest` (`xbt_manifest_id`),
  CONSTRAINT `xbt_manifest_prev_xbt_manifest_id_fk` FOREIGN KEY (`prev_xbt_manifest_id`) REFERENCES `xbt_manifest` (`xbt_manifest_id`),
  CONSTRAINT `xbt_manifest_release_id_fk` FOREIGN KEY (`release_id`) REFERENCES `release` (`release_id`),
  CONSTRAINT `xbt_manifest_xbt_id_fk` FOREIGN KEY (`xbt_id`) REFERENCES `xbt` (`xbt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'oagi'
--
/*!50003 DROP FUNCTION IF EXISTS `levenshtein` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`oagi`@`%` FUNCTION `levenshtein`(s1 VARCHAR(255), s2 VARCHAR(255)) RETURNS int(11)
    DETERMINISTIC
BEGIN
    DECLARE s1_len, s2_len, i, j, c, c_temp, cost INT;
    DECLARE s1_char CHAR;
    DECLARE cv0, cv1 VARBINARY(256);
    SET s1_len = CHAR_LENGTH(s1), s2_len = CHAR_LENGTH(s2), cv1 = 0x00, j = 1, i = 1, c = 0;
    IF s1 = s2 THEN
        RETURN 0;
    ELSEIF s1_len = 0 THEN
        RETURN s2_len;
    ELSEIF s2_len = 0 THEN
        RETURN s1_len;
    ELSE
        WHILE j <= s2_len DO
            SET cv1 = CONCAT(cv1, UNHEX(HEX(j))), j = j + 1;
        END WHILE;
        WHILE i <= s1_len DO
            SET s1_char = SUBSTRING(s1, i, 1), c = i, cv0 = UNHEX(HEX(i)), j = 1;
            WHILE j <= s2_len DO
                SET c = c + 1;
                IF s1_char = SUBSTRING(s2, j, 1) THEN SET cost = 0; ELSE SET cost = 1; END IF;
                SET c_temp = CONV(HEX(SUBSTRING(cv1, j, 1)), 16, 10) + cost;
                IF c > c_temp THEN SET c = c_temp; END IF;
                SET c_temp = CONV(HEX(SUBSTRING(cv1, j+1, 1)), 16, 10) + 1;
                IF c > c_temp THEN SET c = c_temp; END IF;
                SET cv0 = CONCAT(cv0, UNHEX(HEX(c))), j = j + 1;
            END WHILE;
            SET cv1 = cv0, i = i + 1;
        END WHILE;
    END IF;
    RETURN c;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-03-13 17:09:17
