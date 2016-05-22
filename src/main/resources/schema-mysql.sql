-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema oagsrt_revision
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema oagsrt_revision
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `oagsrt_revision` DEFAULT CHARACTER SET latin1 ;
USE `oagsrt_revision` ;

-- -----------------------------------------------------
-- Table `oagsrt_revision`.`dt`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`dt` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`dt` (
  `dt_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `guid` VARCHAR(41) CHARACTER SET 'utf8' NOT NULL,
  `type` INT(11) NOT NULL COMMENT 'List value: 0 = CDT, 1 = BDT.',
  `version_num` VARCHAR(45) CHARACTER SET 'utf8' NOT NULL COMMENT 'Format X.Y.Z where all of them are integer with no leading zero allowed. X means major version number, Y means minor version number and Z means patch version number.',
  `previous_version_dt_id` INT(11) UNSIGNED NULL,
  `data_type_term` VARCHAR(45) CHARACTER SET 'utf8' NULL COMMENT 'This column is derived from the Based_DT_ID when the column is not blank. ',
  `qualifier` TEXT CHARACTER SET 'utf8' NULL COMMENT 'This column should be blank when the DT_Type is CDT. When the DT_Type is BDT, this is optional - if blank that the row is a unqualified BDT, if not blank it is a qualified BDT.',
  `based_dt_id` INT(11) UNSIGNED NULL COMMENT 'Foreign key pointing to itself. This column must be blank when the DT_Type is CDT. This column must not be blank when the DT_Type is BDT.',
  `den` TEXT CHARACTER SET 'utf8' NOT NULL COMMENT 'This column should be automatically derived.',
  `content_component_den` TEXT CHARACTER SET 'utf8' NULL COMMENT 'When the DT_Type is CDT this column is automatically derived from Data_Type_Term as \"<Data_Type_Term>. Content\", where Content is called property term of the content component according to CCTS. When the DT_Type is BDT this column is automaticlaly derived from the Based_DT_ID.',
  `definition` TEXT CHARACTER SET 'utf8' NULL,
  `content_component_definition` TEXT CHARACTER SET 'utf8' NULL,
  `revision_doc` TEXT CHARACTER SET 'utf8' NULL COMMENT 'This is for documenting about the revision.',
  `module` TEXT(100) NULL COMMENT 'physical file where the DT shall belong to when generating it.',
  `state` INT(11) NULL COMMENT '1 = Editing, 2 = Candidate, 3 = Published. This the revision life cycle state of the entity.\n\nState change can\'t be undone. But the history record can still keep the records of when the state was changed.',
  `created_by` INT(11) NOT NULL,
  `owner_user_id` INT(11) UNSIGNED NOT NULL COMMENT 'This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn\'t rollback the ownership. ',
  `last_updated_by` INT(11) NOT NULL,
  `creation_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_update_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `revision_num` INT(11) NOT NULL DEFAULT 0 COMMENT 'Revision_Number is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).',
  `revision_tracking_num` INT(11) NOT NULL DEFAULT 0 COMMENT 'Revision_Tracking_Number supports the ability to undo changes during a revision (life cycle of a revision is from the component\'s Editing state to Published state). Once the component has transitioned into the Published state for its particular revision, all revision tracking records are deleted except the latest one. Revision_Tracking_Number can be 0, 1, 2, and so on. The zero value is assign to the record with Revision_Number = 0 as a default.',
  `revision_action` TINYINT(11) NULL DEFAULT 1 COMMENT 'This indicates the action associated with the record. The action can be 1 = insert, 2 = update, and 3 = delete. This column is null for the current record.',
  `release_id` INT(11) UNSIGNED NULL COMMENT 'Release_ID is an incremental integer. It is an unformatted counter part of the Release_Number in the Release table. Release_ID can be 1, 2, 3, and so on. Release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the Revision_Action column).\n\nNot all component revisions have an associated Release_ID because some revisions may never be released.\n\nUnpublished components cannot be released.\n\nThis column is null for the current record.',
  `current_bdt_id` INT(11) UNSIGNED NULL COMMENT 'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the record whose Revision_Number is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.\n\nIt is noted that although this is a foreign key by definition, we don\'t specify a foreign key in the data model. This is because when an entity is deleted the current record won\'t exist anymore.\n\nThe value of this column for the current record should be left NULL.',
  `is_deprecated` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  PRIMARY KEY (`dt_id`),
  CONSTRAINT `Based_DT`
  FOREIGN KEY (`based_dt_id`)
  REFERENCES `oagsrt_revision`.`dt` (`dt_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;

CREATE UNIQUE INDEX `Core_Data_Type_GUID_UNIQUE` ON `oagsrt_revision`.`dt` (`guid` ASC);


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`xbt`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`xbt` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`xbt` (
  `xbt_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Unique ID of XML Schema built-in types.',
  `name` VARCHAR(45) NULL COMMENT 'Human readable name of the XML Schema built-in type.',
  `builtIn_type` VARCHAR(45) NULL COMMENT 'XML Schema built-in type as it should appear in an XML schema.',
  `subtype_of_xbt_id` INT(11) UNSIGNED NULL DEFAULT NULL,
  PRIMARY KEY (`xbt_id`),
  CONSTRAINT `subtype_of_xbt_id_fk`
  FOREIGN KEY (`subtype_of_xbt_id`)
  REFERENCES `oagsrt_revision`.`xbt` (`xbt_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8;

CREATE INDEX `FK_Subtype_Of_XSD_BuiltIn_Type_ID_idx` ON `oagsrt_revision`.`xbt` (`subtype_of_xbt_id` ASC);


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`cdt_pri`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`cdt_pri` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`cdt_pri` (
  `cdt_pri_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`cdt_pri_id`))
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`cdt_awd_pri`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`cdt_awd_pri` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`cdt_awd_pri` (
  `cdt_awd_pri_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
  `cdt_id` INT(11) UNSIGNED NOT NULL COMMENT 'Foreign key from the dt table corresponding to the CDT being recorded.',
  `cdt_pri_id` INT(11) UNSIGNED NOT NULL COMMENT 'Foreign key from the cdt_pri table corresponding to the Allowed Primitive column in each of the CDT Content Component section/table in CCTS DTC3',
  `is_default` TINYINT(1) NOT NULL COMMENT 'Indicating a default primitive for the CDT’s Content Component. True for a default primitive; False otherwise.',
  PRIMARY KEY (`cdt_awd_pri_id`),
  CONSTRAINT `CDT`
  FOREIGN KEY (`cdt_id`)
  REFERENCES `oagsrt_revision`.`dt` (`dt_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `CDT_Primitive`
  FOREIGN KEY (`cdt_pri_id`)
  REFERENCES `oagsrt_revision`.`cdt_pri` (`cdt_pri_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8
  COMMENT = 'This table capture allowed primitives of the CDT’s Content Component. ';


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`cdt_awd_pri_xps_type_map`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`cdt_awd_pri_xps_type_map` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`cdt_awd_pri_xps_type_map` (
  `cdt_awd_pri_xps_type_map_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `cdt_awd_pri_id` INT(11) UNSIGNED NOT NULL,
  `xbt_id` INT(11) UNSIGNED NOT NULL,
  PRIMARY KEY (`cdt_awd_pri_xps_type_map_id`),
  CONSTRAINT `CDT_Allowed_Primitive1`
  FOREIGN KEY (`cdt_awd_pri_id`)
  REFERENCES `oagsrt_revision`.`cdt_awd_pri` (`cdt_awd_pri_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `XSD_BuiltIn_Type1`
  FOREIGN KEY (`xbt_id`)
  REFERENCES `oagsrt_revision`.`xbt` (`xbt_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8
  COMMENT = 'This table allows for concrete mapping between the CDT Primitives and types in a particular expression such as XML Schema, JSON. At this point, it is not clear whether a separate table will be needed for each expression. The current table holds the map to XML Schema built-in types. \n\nFor each additional expression columns similar to the xbt_id will need to be added to this table for mapping to data types in another expression.\n\nIf we use a separate table for each expression, then we need binding all the way to BDT (or even BBIE) for every new expression. That would be almost like just store a BDT file. But using columns has no gaurantee that it will work with all kinds of expressions. If the typing in another expression is less finer grain than the XSD built-in types, I think the additional columns will work.';

CREATE INDEX `XSD_BuiltIn_Type1` ON `oagsrt_revision`.`cdt_awd_pri_xps_type_map` (`xbt_id` ASC);


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`dt_sc`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`dt_sc` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`dt_sc` (
  `dt_sc_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Revision is not tracked at the supplementary component. It is considered intrinsic part of the DT. In other words, when a new revision of a DT is created a new set of supplementary components is created along with it. ',
  `guid` VARCHAR(41) NOT NULL,
  `property_term` VARCHAR(45) NULL,
  `representation_term` VARCHAR(45) NULL,
  `definition` TEXT NULL,
  `owner_dt_id` INT(11) UNSIGNED NOT NULL COMMENT 'Foreigned key to the Core_Data_Type table indicating the data type to which this supplementary component belongs.',
  `min_cardinality` INT(11) NOT NULL DEFAULT 0,
  `max_cardinality` INT(11) NULL,
  `based_dt_sc_id` INT(11) UNSIGNED NULL,
  PRIMARY KEY (`dt_sc_id`),
  CONSTRAINT `Based_DT_SC`
  FOREIGN KEY (`based_dt_sc_id`)
  REFERENCES `oagsrt_revision`.`dt_sc` (`dt_sc_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `DT`
  FOREIGN KEY (`owner_dt_id`)
  REFERENCES `oagsrt_revision`.`dt` (`dt_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`cdt_sc_awd_pri`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`cdt_sc_awd_pri` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`cdt_sc_awd_pri` (
  `cdt_sc_awd_pri_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `cdt_sc_id` INT(11) UNSIGNED NOT NULL,
  `cdt_pri_id` INT(11) UNSIGNED NOT NULL,
  `is_default` TINYINT(1) NOT NULL COMMENT 'Indicating whether the primitive is the default primitive of the supplementary component.',
  PRIMARY KEY (`cdt_sc_awd_pri_id`),
  CONSTRAINT `SC`
  FOREIGN KEY (`cdt_sc_id`)
  REFERENCES `oagsrt_revision`.`dt_sc` (`dt_sc_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `CP`
  FOREIGN KEY (`cdt_pri_id`)
  REFERENCES `oagsrt_revision`.`cdt_pri` (`cdt_pri_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  COMMENT = 'This table capture the CDT primitives allowed for a particular SC of CDTs. It also store the CDT primitives allowed for a SC of a BDT that extends its base.';


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`namespace`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`namespace` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`namespace` (
  `namespace_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `uri` TEXT NOT NULL COMMENT 'This is the URI of the namespace.',
  `prefix` VARCHAR(45) NULL COMMENT 'This is a default short name to represent the URI. It may be override during the the expression generation. Null or empty means the same thing like the default prefix in an XML schema.',
  `description` TEXT NULL COMMENT 'Description or explanation about the namespace or use of the namespace.',
  `is_std_nmsp` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'This indicates whether the namespace is reserved for standard used (i.e., whether it is an OAGIS namespace). If it is true, then end users cannot user the namespace for the end user CCs.',
  `owner_user_id` INT(11) UNSIGNED NOT NULL COMMENT 'The user who can update or delete the record.',
  `created_by` INT(11) NOT NULL COMMENT 'The user who created the namespace.',
  `last_updated_by` INT(11) NOT NULL COMMENT 'The use who last updated the record.',
  `creation_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The time when the record was first created.',
  `last_updated_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The time the record was last updated.',
  PRIMARY KEY (`namespace_id`))
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`bccp`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`bccp` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`bccp` (
  `bccp_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `guid` VARCHAR(41) NOT NULL,
  `property_term` TEXT NOT NULL,
  `representation_term` TEXT NOT NULL COMMENT 'Note 1: BCCP\'s Representation Term should be derived from its BDT as BDT\'s Data_Type_Qualifier + CDT\'s Data_Type_Term.',
  `bdt_id` INT(11) UNSIGNED NOT NULL COMMENT 'Only DT_ID which DT_Type is BDT can be used.',
  `den` TEXT NOT NULL,
  `definition` TEXT NULL,
  `module` TEXT(100) NULL COMMENT 'This column stores the name of the physical schema module the ASCCP belongs to. Right now the schema file name is assigned. In the future, this needs to be updated to a file path from the base of the release directory.',
  `namespace_id` INT(11) UNSIGNED NULL COMMENT 'Foreign key to the Namespace table. This is the namespace, to which the entity belongs.',
  `is_deprecated` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  `created_by` INT(11) NOT NULL COMMENT 'A foreign key referring to the user who creates the entity.\n\nThis column never change between the history and the current record. The history record should have the same value as that of its current record.',
  `owner_user_id` INT(11) UNSIGNED NOT NULL,
  `last_updated_by` INT(11) NOT NULL COMMENT 'A foreign key referring to the last user who has updated the record. \n\nIn the history record, this should always be the user who is editing the entity.',
  `creation_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when the revision of the BCCP was created. \n\nThis never changefor a revision.',
  `last_update_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.',
  `state` INT(11) NOT NULL COMMENT '1 = Editing, 2 = Candidate, 3 = Published. This the revision life cycle state of the ACC.\n\nState change can\'t be undone. But the history record can still keep the records of when the state was changed.',
  `revision_num` INT(11) NOT NULL DEFAULT 0 COMMENT 'Revision_Number is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).',
  `revision_tracking_num` INT(11) NOT NULL DEFAULT 0 COMMENT 'Revision_Tracking_Number supports the ability to undo changes during a revision (life cycle of a revision is from the component\'s Editing state to Published state). Once the component has transitioned into the Published state for its particular revision, all revision tracking records are deleted except the latest one. Revision_Tracking_Number can be 0, 1, 2, and so on. The zero value is assign to the record with Revision_Number = 0 as a default.',
  `revision_action` INT(11) NULL DEFAULT 1 COMMENT 'This indicates the action associated with the record. The action can be 1 = insert, 2 = update, and 3 = delete. This column is null for the current record.',
  `release_id` INT(11) UNSIGNED NULL COMMENT 'Release_ID is an incremental integer. It is an unformatted counter part of the Release_Number in the Release table. Release_ID can be 1, 2, 3, and so on. Release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the Revision_Action column).\n\nNot all component revisions have an associated Release_ID because some revisions may never be released.\n\nUnpublished components cannot be released.\n\nThis column is null for the current record.',
  `current_bccp_id` INT(11) UNSIGNED NULL COMMENT 'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the the record whose Revision_Number is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.\n\nIt is noted that although this is a foreign key by definition, we don\'t specify a foreign key in the data model. This is because when an entity is deleted the current record won\'t exist anymore.\n\nThe value of this column for the current record should be left NULL.',
  PRIMARY KEY (`bccp_id`),
  CONSTRAINT `BDT`
  FOREIGN KEY (`bdt_id`)
  REFERENCES `oagsrt_revision`.`dt` (`dt_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `BCCP_Target_Namespace`
  FOREIGN KEY (`namespace_id`)
  REFERENCES `oagsrt_revision`.`namespace` (`namespace_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`release`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`release` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`release` (
  `release_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Release_ID must be an incremental integer. Release_ID that is more than another Release_ID is interpreted to be released later than the other.',
  `release_num` VARCHAR(45) NOT NULL COMMENT 'Release number such has 10.0, 10.1, etc. ',
  `release_note` LONGTEXT NULL COMMENT 'Description or note associated with the release.',
  `namespace_id` INT(11) UNSIGNED NOT NULL,
  PRIMARY KEY (`release_id`),
  CONSTRAINT `Namespace`
  FOREIGN KEY (`namespace_id`)
  REFERENCES `oagsrt_revision`.`namespace` (`namespace_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`acc`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`acc` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`acc` (
  `acc_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an ACC.',
  `guid` VARCHAR(41) NOT NULL COMMENT 'A globally unique identifier (GUID) of an ACC. Per OAGIS, a GUID is of the form \"oagis-id-\" followed by a 32 Hex character sequence.',
  `object_class_term` TEXT NOT NULL COMMENT 'Object class name of the ACC concept. For OAGIS, this is generally name of a type with the \"Type\" truncated from the end. Per CCS the name is space separated.',
  `den` TEXT NOT NULL COMMENT 'DEN (dictionary entry name) of the ACC. It can be derived as Object_Class_Term + \". Details\".',
  `definition` TEXT NULL COMMENT 'This is a documentation or description of the ACC. Since ACC is business context independent, this is a business context independent description of the ACC concept.',
  `based_acc_id` INT(11) UNSIGNED NULL COMMENT 'Based_ACC_ID is a foreign key to the ACC table itself. It represents the ACC that is qualified by this ACC. In general CCS sense, a qualification can by a content extension or restriction, but the current scope supports only extension.\n\nFor history records of an ACC, this column always points to the current record of an ACC.',
  `object_class_qualifier` TEXT NULL,
  `oagis_component_type` INT(11) NULL COMMENT 'The value can be 0 = Base, 1 = Semantics, 2 = Extension, 3 = Semantic Group, 4 = User Extension Group. Generally, Bsae is assigned when the Object_Class_Term contains \"Base\" at the end. Extension is assigned with the Object_Class_Term contains \"Extension\" at the end. Semantic Group is assigned when an ACC is imported from an XSD Group. Other cases are assigned Semantics.',
  `module` TEXT(100) NULL COMMENT 'This column stores the name of the physical schema module the ACC belongs to. Right now the schema file name is assigned. In the future, this needs to be updated to a file path from the base of the release directory.',
  `namespace_id` INT(11) UNSIGNED NULL COMMENT 'Foreign key to the Namespace table. This is the namespace to which the entity belongs. This namespace column is only used in the case the component is a user\'s component.',
  `created_by` INT(11) NOT NULL COMMENT 'A foreign key referring to the user who creates the entity.\n\nThis column never change between the history and the current record. The history record should have the same value as that of its current record.',
  `owner_user_id` INT(11) UNSIGNED NOT NULL COMMENT 'This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn\'t rollback the ownership. ',
  `last_updated_by` INT(11) NOT NULL COMMENT 'A foreign key referring to the last user who updated the record. \n\nIn the history record, this should always be the user who is editing the entity.',
  `creation_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when the revision of the ACC was created. \n\nThis never change for a revision.',
  `last_update_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.',
  `state` INT(11) NOT NULL COMMENT '1 = Editing, 2 = Candidate, 3 = Published. This the revision life cycle state of the ACC.\n\nState change can\'t be undone. But the history record can still keep the records of when the state was changed.',
  `revision_num` INT(11) NOT NULL DEFAULT 0 COMMENT 'Revision_Number is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).',
  `revision_tracking_num` INT(11) NOT NULL DEFAULT 0 COMMENT 'Revision_Tracking_Number supports the ability to undo changes during a revision (life cycle of a revision is from the component\'s Editing state to Published state). Once the component has transitioned into the Published state for its particular revision, all revision tracking records are deleted except the latest one. Revision_Tracking_Number can be 0, 1, 2, and so on. The zero value is assign to the record with Revision_Number = 0 as a default.',
  `revision_action` TINYINT(11) NULL DEFAULT 1 COMMENT 'This indicates the action associated with the record. The action can be 1 = insert, 2 = update, and 3 = delete. This column is null for the current record.',
  `release_id` INT(11) UNSIGNED NULL COMMENT 'Release_ID is an incremental integer. It is an unformatted counter part of the Release_Number in the Release table. Release_ID can be 1, 2, 3, and so on. Release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the Revision_Action column).\n\nNot all component revisions have an associated Release_ID because some revisions may never be released. User Extension Group component type is never part of a release.\n\nUnpublished components cannot be released.\n\nThis column is null for the current record.',
  `current_acc_id` INT(11) UNSIGNED NULL COMMENT 'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the the record whose Revision_Number is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.\n\nIt is noted that although this is a foreign key by definition, we don\'t specify a foreign key in the data model. This is because when an entity is deleted the current record won\'t exist anymore.\n\nThe value of this column for the current record should be left NULL.',
  `is_deprecated` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  PRIMARY KEY (`acc_id`),
  CONSTRAINT `Base_ACC`
  FOREIGN KEY (`based_acc_id`)
  REFERENCES `oagsrt_revision`.`acc` (`acc_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `ACC_Released_Since`
  FOREIGN KEY (`release_id`)
  REFERENCES `oagsrt_revision`.`release` (`release_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `ACC_Target_Namespace`
  FOREIGN KEY (`namespace_id`)
  REFERENCES `oagsrt_revision`.`namespace` (`namespace_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8
  COMMENT = 'The ACC table hold information about complex data structured concepts. For example, OAGIS\'s Components, Nouns, and BODs are captured in the ACC table.\n\nNote that only Extension is supported when deriving ACC from another ACC. (So if there is a restriction needed, maybe that concept should placed higher in the derivation hierarchy rather than lower.)\n\nIn OAGIS, all XSD extensions will be treated as ACC qualification.';


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`bcc`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`bcc` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`bcc` (
  `bcc_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `guid` VARCHAR(41) NULL,
  `cardinality_min` INT(11) NOT NULL,
  `cardinality_max` INT(11) NULL COMMENT '-1 means unbounded.',
  `to_bccp_id` INT(11) UNSIGNED NOT NULL,
  `from_acc_id` INT(11) UNSIGNED NOT NULL,
  `seq_key` INT(11) NULL COMMENT 'This indicates the order of the associations among other siblings. The valid values are positive integer. The Sequencing_Key at the CC side is localized. In other words, if an ACC is based on another ACC, Sequencing_Key of ASCCs or BCCs of the former ACC starts at 1 again. The Sequencing_Key in the case of Entity_Type is attribute is always zero.',
  `entity_type` INT(11) NULL COMMENT 'This is a code list: 0 = attribute and 1 = element. An expression generator may or may not use this information. This column is necessary because some of the BCCs are xsd:attribute and some are xsd:element in the legacy OAGIS. ',
  `den` TEXT NOT NULL,
  `definition` TEXT NULL,
  `created_by` INT(11) NOT NULL COMMENT 'A foreign key referring to the user who creates the entity.\n\nThis column never change between the history and the current record. The history record should have the same value as that of its current record.',
  `owner_user_id` INT(11) UNSIGNED NOT NULL,
  `last_updated_by` INT(11) NOT NULL COMMENT 'A foreign key referring to the last user who has updated the record. \n\nIn the history record, this should always be the user who is editing the entity.',
  `creation_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when the revision of the BCC was created. \n\nThis never change for a revision.',
  `last_update_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.',
  `state` INT(11) NOT NULL COMMENT '1 = Editing, 2 = Candidate, 3 = Published. This the revision life cycle state of the ACC.\n\nState change can\'t be undone. But the history record can still keep the records of when the state was changed.',
  `revision_num` INT(11) NOT NULL DEFAULT 0 COMMENT 'Revision_Number is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).',
  `revision_tracking_num` INT(11) NOT NULL DEFAULT 0 COMMENT 'Revision_Tracking_Number supports the ability to undo changes during a revision (life cycle of a revision is from the component\'s Editing state to Published state). Once the component has transitioned into the Published state for its particular revision, all revision tracking records are deleted except the latest one. Revision_Tracking_Number can be 0, 1, 2, and so on. The zero value is assign to the record with Revision_Number = 0 as a default.',
  `revision_action` TINYINT(1) NULL DEFAULT 1 COMMENT 'This indicates the action associated with the record. The action can be 1 = insert, 2 = update, and 3 = delete. This column is null for the current record.',
  `release_id` INT(11) UNSIGNED NULL COMMENT 'Release_ID is an incremental integer. It is an unformatted counter part of the Release_Number in the Release table. Release_ID can be 1, 2, 3, and so on. Release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the Revision_Action column).\n\nNot all component revisions have an associated Release_ID because some revisions may never be released.\n\nUnpublished components cannot be released.\n\nThis column is null for the current record.',
  `current_bcc_id` INT(11) UNSIGNED NULL COMMENT 'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the the record whose Revision_Number is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.\n\nIt is noted that although this is a foreign key by definition, we don\'t specify a foreign key in the data model. This is because when an entity is deleted the current record won\'t exist anymore.\n\nThe value of this column for the current record should be left NULL.',
  `is_deprecated` TINYINT(1) NOT NULL COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  PRIMARY KEY (`bcc_id`),
  CONSTRAINT `BCC_BCCP`
  FOREIGN KEY (`to_bccp_id`)
  REFERENCES `oagsrt_revision`.`bccp` (`bccp_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `BCC_Owner_ACC`
  FOREIGN KEY (`from_acc_id`)
  REFERENCES `oagsrt_revision`.`acc` (`acc_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `BCC_Released_Since`
  FOREIGN KEY (`release_id`)
  REFERENCES `oagsrt_revision`.`release` (`release_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`biz_ctx`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`biz_ctx` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`biz_ctx` (
  `biz_ctx_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `guid` VARCHAR(41) NOT NULL,
  `name` TEXT CHARACTER SET 'utf8' NULL COMMENT 'Short, descriptive name of the business context.',
  `created_by` INT(11) NOT NULL,
  `last_updated_by` INT(11) NOT NULL,
  `creation_timestamp` DATETIME NOT NULL,
  `last_update_timestamp` DATETIME NOT NULL,
  PRIMARY KEY (`biz_ctx_id`))
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`client`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`client` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`client` (
  `client_id` INT(11) UNSIGNED NOT NULL,
  `name` VARCHAR(200) NULL,
  PRIMARY KEY (`client_id`))
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`abie`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`abie` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`abie` (
  `abie_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an ABIE.',
  `guid` VARCHAR(41) NOT NULL COMMENT 'A globally unique identifier (GUID) of an ABIE. GUID of an ABIE is different from its based ACC. Per OAGIS, a GUID is of the form \"oagis-id-\" followed by a 32 Hex character sequence.',
  `based_acc_id` INT(11) UNSIGNED NOT NULL COMMENT 'A foreign key to the ACC table refering to the ACC, on which the business context has been applied to derive this ABIE.',
  `is_top_level` TINYINT(1) NOT NULL COMMENT 'Indicate whether the ABIE is a top-level ABIE. If false, it is a descendant of one of the top-level ABIEs. In the context of OAGIS, top-level ABE is used for flagging that an ABIE is a BOD. The condition or conditions for recognizing that an ABIE (or logically, its based ACC) is a top-level is documented in the design document. ',
  `biz_ctx_id` INT(11) UNSIGNED NOT NULL COMMENT 'A foreign key to the Business_Context table. This column stores the business context assigned to an ABIE.',
  `definition` TEXT NULL COMMENT 'Definition to override the ACC\'s Definition. If Null, it means that the definition should be inherited from the based CC.',
  `created_by` INT(11) NOT NULL COMMENT 'A foreign key referring to the user who creates the ABIE. The creator of the ABIE is also its owner by default. ABIEs created as children of another ABIE have the same Created_By_User_ID.',
  `last_updated_by` INT(11) NOT NULL COMMENT 'A foreign key referring to the last user who has updated the ASBIE record. This may be the user who is in the same group as the creator.',
  `creation_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when the ABIE record was first created. ABIEs created as children of another ABIE have the same Creation_Timestamp.',
  `last_update_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The timestamp when the ABIE is last updated.',
  `state` INT(11) NULL COMMENT '2 = Editing, 4 = Published. This column is only used with a top-level ABIE, because that is the only entry point for editing. The state value indicates the visibility of the top-level ABIE to users other than the owner. In the user group environment, a logic can apply that other users in the group can see the top-level ABIE only when it is in the \'Published\' state.',
  `client_id` INT(11) UNSIGNED NULL COMMENT 'This is a foreign key to the Client table. The use case associated with this column is to indicate the organizational entity for which the profile BOD is created. For example, Boeing may generate a profile BOD for Boeing civilian or Boeing defense. It is more of the documentation purpose. Only an ABIE which is the top-level ABIE can use this column.',
  `version` VARCHAR(45) NULL COMMENT 'This column hold a version number assigned by the user. This column is only used by the top-level ABIE. No format of version is enforced.',
  `status` VARCHAR(45) NULL COMMENT 'This is different from State which is CRUD life cycle of an entity. The use case for this is to allow the user to indicate the usage status of a top-level ABIE (a profile BOD). An integration architect can use this column. Example values are ‘Prototype’, ‘Test’, and ‘Production’. Only the top-level ABIE can use this field.',
  `remark` VARCHAR(225) NULL COMMENT 'This column allows the user to specify very context-specific usage of the BIE. It is different from the Definition column in that the Definition column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be \"Type of BOM should be recognized in the BOM/typeCode.\"',
  `biz_term` VARCHAR(225) NULL COMMENT 'To indicate what the BIE is called in a particular business context. With this current design, only one business term is allowed per business context.',
  PRIMARY KEY (`abie_id`),
  CONSTRAINT `Based_ACC`
  FOREIGN KEY (`based_acc_id`)
  REFERENCES `oagsrt_revision`.`acc` (`acc_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `Business_Context`
  FOREIGN KEY (`biz_ctx_id`)
  REFERENCES `oagsrt_revision`.`biz_ctx` (`biz_ctx_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `Client`
  FOREIGN KEY (`client_id`)
  REFERENCES `oagsrt_revision`.`client` (`client_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  COMMENT = 'ABIE table stores information about an ABIE, which is a contextualized ACC. The context is represented by the Business_Context_ID column that refers to a business context. Each ABIE must have a business context and a based ACC.\n\nIt should be noted that, per design document, there is no corresponding ABIE created for an ACC which is designated as a \"Semantic Group\". \n\n';


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`bbiep`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`bbiep` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`bbiep` (
  `bbiep_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `guid` VARCHAR(41) NOT NULL,
  `based_bccp_id` INT(11) UNSIGNED NOT NULL,
  `definition` TEXT NULL COMMENT 'Definition to override the BCCP\'s Definition. If Null, it means that the definition should be inherited from the based CC.',
  `remark` VARCHAR(225) NULL COMMENT 'This column allows the user to codify context specific usage of the BIE. It is different from the Definition column in that the Definition column is a descriptive text while this one is machine understandable. So the data type of this column is more like code.',
  `biz_term` VARCHAR(225) NULL COMMENT 'To indicate what the BIE is called in a particular industry (a particular context in general). ',
  `created_by` INT(11) NOT NULL,
  `last_updated_by` INT(11) NOT NULL,
  `creation_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_update_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`bbiep_id`),
  CONSTRAINT `Based_BCCP`
  FOREIGN KEY (`based_bccp_id`)
  REFERENCES `oagsrt_revision`.`bccp` (`bccp_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`code_list`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`code_list` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`code_list` (
  `code_list_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `guid` VARCHAR(41) CHARACTER SET 'utf8' NOT NULL,
  `enum_type_guid` VARCHAR(41) CHARACTER SET 'utf8' NULL,
  `name` TEXT CHARACTER SET 'utf8' NULL,
  `list_id` TEXT CHARACTER SET 'utf8' NOT NULL COMMENT 'External identifier.',
  `agency_id` INT(11) UNSIGNED NOT NULL,
  `version_id` TEXT CHARACTER SET 'utf8' NOT NULL,
  `definition` TEXT CHARACTER SET 'utf8' NULL,
  `remark` VARCHAR(225) NULL COMMENT 'This column allows the user to codify context specific usage of the BIE. It is different from the Definition column in that the Definition column is a descriptive text while this one is machine understandable. So the data type of this column is more like code.',
  `definition_source` TEXT CHARACTER SET 'utf8' NULL,
  `based_code_list_id` INT(11) UNSIGNED NULL COMMENT 'This indicates that this code list is based on another code list - restriction and extension are allowed.',
  `extensible_indicator` TINYINT(1) NOT NULL,
  `module` TEXT(100) NULL COMMENT 'The is the subdirectory and filename of the blob. The format is Windows file path. The starting directory shall be the root folder of all the release content. For example, for OAGIS 10.1 Model, the root directory is Model. If the file shall be directly under the Model directory, then this column should be \'Model\\filename.xsd\'. If the file is under, say, Model\\Platform\\2_1\\Common\\Components directory, then the value of this column shall be \'Model\\Platform\\2_1\\Common\\Components\\filename.xsd\'.',
  `created_by` INT(11) NOT NULL,
  `last_updated_by` INT(11) NOT NULL,
  `creation_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_update_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `state` VARCHAR(255) NOT NULL COMMENT 'Life cycle state of the Code List. Possible values are Editing, Published, or Deleted.',
  PRIMARY KEY (`code_list_id`),
  CONSTRAINT `code_list_agency_id_fk`
  FOREIGN KEY (`agency_id`)
  REFERENCES `agency_id_list_value` (`agency_id_list_value_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `based_code_list_id_fk`
  FOREIGN KEY (`based_code_list_id`)
  REFERENCES `code_list` (`code_list_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  COMMENT = 'When a code list is derived, the whole set of code values belonging to that code list will be copied.';

CREATE INDEX `Based_Code_List` ON `oagsrt_revision`.`code_list` (`based_code_list_id` ASC);

CREATE INDEX `Scheme Agency` ON `oagsrt_revision`.`code_list` (`agency_id` ASC);


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`agency_id_list_value`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`agency_id_list_value` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`agency_id_list_value` (
  `agency_id_list_value_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `value` TEXT NOT NULL,
  `name` TEXT NULL DEFAULT NULL,
  `definition` TEXT NULL,
  `owner_list_id` INT(11) UNSIGNED NOT NULL,
  PRIMARY KEY (`agency_id_list_value_id`),
  CONSTRAINT `agency_id_list_fk`
  FOREIGN KEY (`owner_list_id`)
  REFERENCES `oagsrt_revision`.`agency_id_list` (`agency_id_list_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`agency_id_list`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`agency_id_list` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`agency_id_list` (
  `agency_id_list_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `guid` VARCHAR(41) CHARACTER SET 'utf8' NOT NULL,
  `enum_type_guid` VARCHAR(41) CHARACTER SET 'utf8' NOT NULL,
  `name` TEXT CHARACTER SET 'utf8' NULL,
  `list_id` TEXT CHARACTER SET 'utf8' NULL,
  `agency_id` INT(11) UNSIGNED NULL,
  `version_id` TEXT CHARACTER SET 'utf8' NULL,
  `definition` TEXT CHARACTER SET 'utf8' NULL,
  PRIMARY KEY (`agency_id_list_id`),
  CONSTRAINT `agency_id_list_agency_id_fk`
  FOREIGN KEY (`agency_id`)
  REFERENCES `oagsrt_revision`.`agency_id_list_value` (`agency_id_list_value_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;

CREATE INDEX `Agency_ID_List_Value` ON `oagsrt_revision`.`agency_id_list` (`agency_id` ASC);


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`bdt_pri_restri`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`bdt_pri_restri` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`bdt_pri_restri` (
  `bdt_pri_restri_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `bdt_id` INT(11) UNSIGNED NOT NULL,
  `cdt_awd_pri_xps_type_map_id` INT(11) UNSIGNED NULL COMMENT 'Both CDT_Primitive_Expression_Type_Map_ID and Code_List_ID cannot be blank at the same time.',
  `code_list_id` INT(11) UNSIGNED NULL,
  `is_default` TINYINT(1) NOT NULL DEFAULT '0' COMMENT 'This allow overriding the default in the CDT_Allowed_Primitive_Expression_Type_Map table. This field is used when generating an expression of the OAGIS model. In OAGIS 10, a bunch of BDTs are defined for a CDT, but OAGIS fields is bound to only one of the BDTs.',
  `agency_id_list_id` INT(11) UNSIGNED NULL COMMENT 'This is a foreign key to the agency_id_list table. It is used in the case that the BDT content can be restricted to an agency ID list.',
  PRIMARY KEY (`bdt_pri_restri_id`),
  CONSTRAINT `BDT1`
  FOREIGN KEY (`bdt_id`)
  REFERENCES `oagsrt_revision`.`dt` (`dt_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `CDT_Primitive_Expression_Type_Map1`
  FOREIGN KEY (`cdt_awd_pri_xps_type_map_id`)
  REFERENCES `oagsrt_revision`.`cdt_awd_pri_xps_type_map` (`cdt_awd_pri_xps_type_map_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `Code_List1`
  FOREIGN KEY (`code_list_id`)
  REFERENCES `oagsrt_revision`.`code_list` (`code_list_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `agency_id_list`
  FOREIGN KEY (`agency_id_list_id`)
  REFERENCES `oagsrt_revision`.`agency_id_list` (`agency_id_list_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8
  COMMENT = 'Business rules will ensure that the primitives for BDT are only subset of the CDT or BDT on which it is based.';

CREATE INDEX `Code_List1` ON `oagsrt_revision`.`bdt_pri_restri` (`code_list_id` ASC);


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`bbie`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`bbie` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`bbie` (
  `bbie_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `guid` VARCHAR(41) NOT NULL,
  `based_bcc_id` INT(11) UNSIGNED NOT NULL,
  `from_abie_id` INT(11) UNSIGNED NOT NULL COMMENT 'Assoc_From_ABIE must be based on the Assoc_From_ACC in the Based_BCC.',
  `to_bbiep_id` INT(11) UNSIGNED NOT NULL,
  `bdt_pri_restri_id` INT(11) UNSIGNED NULL,
  `code_list_id` INT(11) UNSIGNED NULL,
  `cardinality_min` INT(11) NOT NULL,
  `cardinality_max` INT(11) NULL COMMENT 'Unspecified = unbounded',
  `default_value` TEXT NULL COMMENT 'Default and fixed value cannot be used at the same time.',
  `is_nillable` TINYINT(1) NOT NULL DEFAULT '0' COMMENT 'Indicate whether the field can have a null value.',
  `fixed_value` TEXT CHARACTER SET 'utf8' NULL,
  `is_null` TINYINT(1) NOT NULL DEFAULT '0' COMMENT 'This column indicates whether the field is fixed to Null. isNull can be true only if the isNillable is true. If isNull is true then the Fixed_Value column cannot have a value.',
  `definition` TEXT CHARACTER SET 'utf8' NULL COMMENT 'Definition to override the BCC definition. If Null, it means that the definition should be inherited from the based CC.',
  `remark` VARCHAR(225) NULL COMMENT 'This column allows the user to codify context specific usage of the BIE. It is different from the Definition column in that the Definition column is a descriptive text while this one is machine understandable. So the data type of this column is more like code.',
  `created_by` INT(11) NOT NULL,
  `last_updated_by` INT(11) NOT NULL,
  `creation_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_update_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `seq_key` DECIMAL NOT NULL,
  `is_used` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Flag to indicate whether the field/component is used in the content model. It indicates whether the field/component should be generated.',
  PRIMARY KEY (`bbie_id`),
  CONSTRAINT `Assoc_From_ABIE`
  FOREIGN KEY (`from_abie_id`)
  REFERENCES `oagsrt_revision`.`abie` (`abie_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `Based_BCC`
  FOREIGN KEY (`based_bcc_id`)
  REFERENCES `oagsrt_revision`.`bcc` (`bcc_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `BBIEP_ID`
  FOREIGN KEY (`to_bbiep_id`)
  REFERENCES `oagsrt_revision`.`bbiep` (`bbiep_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `Primitive_Restriction`
  FOREIGN KEY (`bdt_pri_restri_id`)
  REFERENCES `oagsrt_revision`.`bdt_pri_restri` (`bdt_pri_restri_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `Code_List_Primitive_Restriction`
  FOREIGN KEY (`code_list_id`)
  REFERENCES `oagsrt_revision`.`code_list` (`code_list_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`cdt_sc_awd_pri_xps_type_map`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`cdt_sc_awd_pri_xps_type_map` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`cdt_sc_awd_pri_xps_type_map` (
  `cdt_sc_awd_pri_xps_type_map_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `cdt_sc_awd_pri` INT(11) UNSIGNED NOT NULL,
  `xbt_id` INT(11) UNSIGNED NOT NULL,
  PRIMARY KEY (`cdt_sc_awd_pri_xps_type_map_id`),
  CONSTRAINT `CDT_SC_Allowed_Primitive`
  FOREIGN KEY (`cdt_sc_awd_pri`)
  REFERENCES `oagsrt_revision`.`cdt_sc_awd_pri` (`cdt_sc_awd_pri_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `XSD_Builtin-Type_ID_1`
  FOREIGN KEY (`xbt_id`)
  REFERENCES `oagsrt_revision`.`xbt` (`xbt_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8;

CREATE INDEX `XSD_BuiltIn_Type` ON `oagsrt_revision`.`cdt_sc_awd_pri_xps_type_map` (`xbt_id` ASC);


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`bdt_sc_pri_restri`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`bdt_sc_pri_restri` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`bdt_sc_pri_restri` (
  `bdt_sc_pri_restri_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `bdt_sc_id` INT(11) UNSIGNED NOT NULL,
  `cdt_sc_awd_pri_xps_type_map_id` INT(11) UNSIGNED NULL COMMENT 'This column is used when the BDT is derived from the CDT.',
  `code_list_id` INT(11) UNSIGNED NULL COMMENT 'Foreign key to identify the code list.',
  `is_default` TINYINT(1) NOT NULL,
  `agency_id_list_id` INT(11) UNSIGNED NULL,
  PRIMARY KEY (`bdt_sc_pri_restri_id`),
  CONSTRAINT `Agency_ID_List_ID`
  FOREIGN KEY (`agency_id_list_id`)
  REFERENCES `oagsrt_revision`.`agency_id_list` (`agency_id_list_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `BDT_SC`
  FOREIGN KEY (`bdt_sc_id`)
  REFERENCES `oagsrt_revision`.`dt_sc` (`dt_sc_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `CDT_SC_Allowed_Primitive_Expression_Type_Map`
  FOREIGN KEY (`cdt_sc_awd_pri_xps_type_map_id`)
  REFERENCES `oagsrt_revision`.`cdt_sc_awd_pri_xps_type_map` (`cdt_sc_awd_pri_xps_type_map_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `Code_List`
  FOREIGN KEY (`code_list_id`)
  REFERENCES `oagsrt_revision`.`code_list` (`code_list_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8
  COMMENT = 'It should be noted that the table does not store the fact about primitive restriction hierarchical relationships. In other words, if a BDT SC is derived from another BDT SC and the derivative BDT SC applies some primitive restrictions, that relationship will not be explicitly stored. The derivative BDT SC points directly to the CDT_Primitive_Expression_Type_Map key rather than the BDT_SC_Primitive_Restriction key.';

CREATE INDEX `Code_List` ON `oagsrt_revision`.`bdt_sc_pri_restri` (`code_list_id` ASC);

CREATE INDEX `Agency_ID_List_ID_idx` ON `oagsrt_revision`.`bdt_sc_pri_restri` (`agency_id_list_id` ASC);


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`asccp`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`asccp` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`asccp` (
  `asccp_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `guid` VARCHAR(41) NOT NULL,
  `property_term` TEXT NULL COMMENT 'There must be only one ASCCP without a Property_Term for a particular ACC.',
  `definition` TEXT NULL COMMENT 'Generally Definition should not be empty but it is not forcing here. A warning should be given at the application level.',
  `role_of_acc_id` INT(11) UNSIGNED NULL COMMENT 'The ACC from which this ASCCP is created (ASCCP applies role to the ACC).',
  `den` TEXT NULL,
  `created_by` INT(11) NOT NULL COMMENT 'A foreign key referring to the user who creates the entity. \n\nThis column never change between the history and the current record. The history record should have the same value as that of its current record.',
  `owner_user_id` INT(11) UNSIGNED NOT NULL,
  `last_updated_by` INT(11) NOT NULL COMMENT 'A foreign key referring to the last user who has updated the record. \n\nIn the history record, this should always be the user who is editing the entity.',
  `creation_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when the revision of the ASCCP was created. \n\nThis never change for a revision.',
  `last_update_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.',
  `state` INT(11) NOT NULL COMMENT '1 = Editing, 2 = Candidate, 3 = Published. This the revision life cycle state of the ACC.\n\nState change can\'t be undone. But the history record can still keep the records of when the state was changed.',
  `module` TEXT(100) NULL COMMENT 'This column stores the name of the physical schema module the ASCCP belongs to. Right now the schema file name is assigned. In the future, this needs to be updated to a file path from the base of the release directory.',
  `namespace_id` INT(11) UNSIGNED NULL COMMENT 'Foreign key to the Namespace table. This is the namespace, to which the entity belongs.',
  `reusable_indicator` TINYINT(1) NULL DEFAULT 1,
  `is_deprecated` TINYINT(1) NOT NULL COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  `revision_num` INT(11) NOT NULL DEFAULT 0 COMMENT 'Revision_Number is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).',
  `revision_tracking_num` INT(11) NOT NULL DEFAULT 0 COMMENT 'Revision_Tracking_Number supports the ability to undo changes during a revision (life cycle of a revision is from the component\'s Editing state to Published state). Once the component has transitioned into the Published state for its particular revision, all revision tracking records are deleted except the latest one. Revision_Tracking_Number can be 0, 1, 2, and so on. The zero value is assign to the record with Revision_Number = 0 as a default.',
  `revision_action` TINYINT(11) NULL DEFAULT 1 COMMENT 'This indicates the action associated with the record. The action can be 1 = insert, 2 = update, and 3 = delete. This column is null for the current record.',
  `release_id` INT(11) UNSIGNED NULL COMMENT 'Release_ID is an incremental integer. It is an unformatted counter part of the Release_Number in the Release table. Release_ID can be 1, 2, 3, and so on. Release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the Revision_Action column).\n\nNot all component revisions have an associated Release_ID because some revisions may never be released.\n\nUnpublished components cannot be released.\n\nThis column is null for the current record.',
  `current_asccp_id` INT(11) UNSIGNED NULL COMMENT 'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the the record whose Revision_Number is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.\n\nIt is noted that although this is a foreign key by definition, we don\'t specify a foreign key in the data model. This is because when an entity is deleted the current record won\'t exist anymore.\n\nThe value of this column for the current record should be left NULL.',
  PRIMARY KEY (`asccp_id`),
  CONSTRAINT `ASCCP_Role_of_ACC`
  FOREIGN KEY (`role_of_acc_id`)
  REFERENCES `oagsrt_revision`.`acc` (`acc_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `ASCCP_Released_Since`
  FOREIGN KEY (`release_id`)
  REFERENCES `oagsrt_revision`.`release` (`release_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `ASCCP_Target_Namespace`
  FOREIGN KEY (`namespace_id`)
  REFERENCES `oagsrt_revision`.`namespace` (`namespace_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8;

CREATE UNIQUE INDEX `ASCCP_GUID_UNIQUE` ON `oagsrt_revision`.`asccp` (`guid` ASC);


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`asbiep`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`asbiep` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`asbiep` (
  `asbiep_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an ASBIEP.',
  `guid` VARCHAR(41) NOT NULL COMMENT 'A globally unique identifier (GUID) of an ASBIEP. GUID of an ASBIEP is different from its based ASCCP. Per OAGIS, a GUID is of the form \"oagis-id-\" followed by a 32 Hex character sequence.',
  `based_asccp_id` INT(11) UNSIGNED NOT NULL COMMENT 'A foreign key point to the ASCCP record. It is the ASCCP which the ASBIEP contextualizes.',
  `role_of_abie_id` INT(11) UNSIGNED NOT NULL COMMENT 'A foreign key pointing to the ABIE record. It is the ABIE which the property term in the based ASCCP qualifies. Note that the ABIE has to be derived from the ACC used by the based ASCCP.',
  `definition` TEXT NULL COMMENT 'Definition to override the ASCCP\'s Definition. If Null, it means that the definition should be derived from the based CC on the UI, expression generation, and any API.',
  `remark` VARCHAR(225) NULL COMMENT 'This column allows the user to specify very context-specific usage of the BIE. It is different from the Definition column in that the Definition column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be \"Type of BOM should be recognized in the BOM/typeCode.\"',
  `biz_term` VARCHAR(225) NULL COMMENT 'To indicate what the BIE is called in a particular business context. With this current design, only one business term is allowed per business context.',
  `created_by` INT(11) NOT NULL COMMENT 'A foreign key referring to the user who creates the ASBIEP. The creator of the ASBIEP is also its owner by default. ASBIEPs created as children of another ABIE have the same Created_By_User_ID.',
  `last_updated_by` INT(11) NOT NULL COMMENT 'A foreign key referring to the last user who has updated the ASBIEP record. This may be the user who is in the same group as the creator.',
  `creation_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when the ASBIEP record was first created. ASBIEPs created as children of another ABIE have the same Creation_Timestamp.',
  `last_update_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The timestamp when the ASBIEP is last updated.',
  PRIMARY KEY (`asbiep_id`),
  CONSTRAINT `Based_ASCCP`
  FOREIGN KEY (`based_asccp_id`)
  REFERENCES `oagsrt_revision`.`asccp` (`asccp_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `Role_Of_ABIE`
  FOREIGN KEY (`role_of_abie_id`)
  REFERENCES `oagsrt_revision`.`abie` (`abie_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  COMMENT = 'ASBIEP represents a role in a usage of an ABIE. It is a contextualization of an ASCCP.';


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`ascc`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`ascc` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`ascc` (
  `ascc_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an ASCC.',
  `guid` TEXT(41) NOT NULL COMMENT 'A globally unique identifier (GUID) of an ASCC. Per OAGIS, a GUID is of the form \"oagis-id-\" followed by a 32 Hex character sequence.',
  `cardinality_min` INT(11) NOT NULL COMMENT 'Minimum cardinality of the Assoc_To_ASCCP_ID. The valid values are non-negative integer.',
  `cardinality_max` INT(11) NOT NULL COMMENT 'Maximum cardinality of the Assoc_To_ASCCP_ID. The valid values are integer -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.',
  `seq_key` INT(11) NOT NULL COMMENT 'This indicates the order of the associations among other siblings. The valid values are positive integer. The Sequencing_Key at the CC side is localized. In other words, if an ACC is based on another ACC, Sequencing_Key of ASCCs or BCCs of the former ACC starts at 1 again. ',
  `from_acc_id` INT(11) UNSIGNED NOT NULL COMMENT 'Assoc_From_ACC_ID is a foreign key pointing to an ACC record. It is basically pointing to a parent data element (type) of the Assoc_To_ASCCP_ID. \n\nNote that for the ASCC history records, this column always points to the ACC_ID of the current record of an ACC.',
  `to_asccp_id` INT(11) UNSIGNED NOT NULL COMMENT 'Assoc_To_ASCCP_ID is a foreign key to an ASCCP table record. It is basically pointing to a child data element of the Assoc_From_ACC_ID. \n\nNote that for the ASCC history records, this column always points to the ASCCP_ID of the current record of an ASCCP.',
  `den` TEXT NOT NULL COMMENT 'DEN (dictionary entry name) of the ASCC. This column can be derived from Object_Class_Term of the Assoc_From_ACC_ID and DEN of the Assoc_To_ASCCP_ID as Object_Class_Term + \". \" + DEN. ',
  `definition` TEXT NULL COMMENT 'This is a documentation or description of the ASCC. Since ASCC is business context independent, this is a business context independent description of the ASCC. Since there are Definitions also in the ASCCP (as referenced by Assoc_To_ASCCP_ID column) and the ACC under that ASCCP, Definition in the ASCC is a specific description about the relationship between the ACC (as in Assoc_From_ACC_ID) and the ASCCP.',
  `is_deprecated` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).',
  `created_by` INT(11) NOT NULL COMMENT 'A foreign key referring to the user who creates the entity.\n\nThis column never change between the history and the current record. The history record should have the same value as that of its current record.',
  `owner_user_id` INT(11) UNSIGNED NOT NULL COMMENT 'This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.\n\nThe ownership can change throughout the history, but undoing shouldn\'t rollback the ownership. ',
  `last_updated_by` INT(11) NOT NULL COMMENT 'A foreign key referring to the last user who has updated the record. \n\nIn the history record, this should always be the user who is editing the entity.',
  `creation_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when the revision of the ASCC was created. \n\nThis never change for a revision.',
  `last_update_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The timestamp when the record was last updated.\n\nThe value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.',
  `state` INT(11) NOT NULL COMMENT '1 = Editing, 2 = Candidate, 3 = Published. This the revision life cycle state of the entity.\n\nState change can\'t be undone. But the history record can still keep the records of when the state was changed.',
  `revision_num` INT(11) NOT NULL DEFAULT 0 COMMENT 'Revision_Number is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).',
  `revision_tracking_num` INT(11) NOT NULL DEFAULT 0 COMMENT 'Revision_Tracking_Number supports the ability to undo changes during a revision (life cycle of a revision is from the component\'s Editing state to Published state). Once the component has transitioned into the Published state for its particular revision, all revision tracking records are deleted except the latest one. Revision_Tracking_Number can be 0, 1, 2, and so on. The zero value is assign to the record with Revision_Number = 0 as a default.',
  `revision_action` TINYINT(11) NULL DEFAULT 1 COMMENT 'This indicates the action associated with the record. The action can be 1 = insert, 2 = update, and 3 = delete. This column is null for the current record.',
  `release_id` INT(11) UNSIGNED NULL COMMENT 'Release_ID is an incremental integer. It is an unformatted counter part of the Release_Number in the Release table. Release_ID can be 1, 2, 3, and so on. Release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the Revision_Action column).\n\nNot all component revisions have an associated Release_ID because some revisions may never be released.\n\nUnpublished components cannot be released.\n\nThis column is null for the current record.',
  `current_ascc_id` INT(11) UNSIGNED NULL COMMENT 'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the the record whose Revision_Number is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.\n\nIt is noted that although this is a foreign key by definition, we don\'t specify a foreign key in the data model. This is because when an entity is deleted the current record won\'t exist anymore.\n\nThe value of this column for the current record should be left NULL.',
  PRIMARY KEY (`ascc_id`),
  CONSTRAINT `Assoc_From_ACC`
  FOREIGN KEY (`from_acc_id`)
  REFERENCES `oagsrt_revision`.`acc` (`acc_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `Assoc_To_ACC`
  FOREIGN KEY (`to_asccp_id`)
  REFERENCES `oagsrt_revision`.`asccp` (`asccp_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `ASCC_Released_Since`
  FOREIGN KEY (`release_id`)
  REFERENCES `oagsrt_revision`.`release` (`release_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8
  COMMENT = 'An ASCC represents a relationship/association between two ACCs through an ASCCP. ';


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`asbie`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`asbie` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`asbie` (
  `asbie_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'A internal, primary database key of an ASBIE.',
  `guid` VARCHAR(41) NOT NULL COMMENT 'A globally unique identifier (GUID) of an ASBIE. GUID of an ASBIE is different from its based ASCC. Per OAGIS, a GUID is of the form \"oagis-id-\" followed by a 32 Hex character sequence.',
  `from_abie_id` INT(11) UNSIGNED NOT NULL COMMENT 'Assoc_From_ABIE_ID is a foreign key pointing to the ABIE table. Assoc_From_ABIE_ID is basically  a parent data element (type) of the Assoc_To_ASBIEP_ID. Assoc_From_ABIE_ID must be based on the Assoc_From_ACC_ID in the Based_ASCC except when the Assoc_From_ACC_ID refers to an ACC Semantic Group.',
  `to_asbiep_id` INT(11) UNSIGNED NOT NULL COMMENT 'Assoc_To_ASBIEP_ID is a foreign key to the ASBIEP table. Assoc_To_ASBIEP_ID is basically a child data element of the Assoc_From_ABIE_ID. Assoc_To_ASBIEP_ID must be based on the Role_of_ACC_ID in the Based_ASCC.',
  `based_ascc` INT(11) UNSIGNED NOT NULL COMMENT 'The Based_ASCC column refers to the ASCC record, which this ASBIE contextualizes.',
  `definition` TEXT CHARACTER SET 'utf8' NULL COMMENT 'Definition to override the ASCC definition. If Null, it means that the definition should be derived from the based CC on the UI, expression generation, and any API.',
  `cardinality_min` INT(11) NOT NULL COMMENT 'Minimum cardinality of the Assoc_To_ASBIEP_ID. The valid values are non-negative integer.',
  `cardinality_max` INT(11) NOT NULL COMMENT 'Maximum cardinality of the Assoc_To_ASBIEP_ID. The valid values are integer -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.',
  `is_nillable` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Indicate whether the Assoc_To_ASBIEP is nillable.',
  `remark` VARCHAR(225) NULL COMMENT 'This column allows the user to specify very context-specific usage of the BIE. It is different from the Definition column in that the Definition column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be \"Type of BOM should be recognized in the BOM/typeCode.\"',
  `created_by` INT(11) NOT NULL COMMENT 'A foreign key referring to the user who creates the ASBIE. The creator of the ASBIE is also its owner by default. ASBIEs created as children of another ABIE have the same Created_By_User_ID.',
  `last_updated_by` INT(11) NOT NULL COMMENT 'A foreign key referring to the last user who has updated the ASBIE record. This may be the user who is in the same group as the creator.',
  `creation_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when the ASBIE record was first created. ASBIEs created as children of another ABIE have the same Creation_Timestamp.',
  `last_update_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'The timestamp when the ASBIE is last updated.',
  `seq_key` DECIMAL(10,2) NOT NULL COMMENT 'This indicates the order of the associations among other siblings. The Sequencing_Key for BIEs is decimal in order to accomodate the removal of inheritance hierarchy and group. For example, children of the most abstract ACC will have Sequencing_Key = 1.1, 1.2, 1.3, and so on; and Sequencing_Key of the next abstraction level ACC will have Sequencing_Key = 2.1, 2.2, 2.3 and so on so forth.',
  `is_used` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Flag to indicate whether the field/component is used in the content model. It indicates whether the field/component should be generated.',
  PRIMARY KEY (`asbie_id`),
  CONSTRAINT `Assoc_From_ABIE1`
  FOREIGN KEY (`from_abie_id`)
  REFERENCES `oagsrt_revision`.`abie` (`abie_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `Assoc_To_ASBIEP`
  FOREIGN KEY (`to_asbiep_id`)
  REFERENCES `oagsrt_revision`.`asbiep` (`asbiep_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `Based_ASCC`
  FOREIGN KEY (`based_ascc`)
  REFERENCES `oagsrt_revision`.`ascc` (`ascc_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  COMMENT = 'An ASBIE represents a relationship/association between two ABIEs through an ASBIEP. It is contextualization of an ASCC.';


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`code_list_value`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`code_list_value` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`code_list_value` (
  `code_list_value_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `code_list_id` INT(11) UNSIGNED NOT NULL,
  `value` TINYTEXT NOT NULL,
  `name` TEXT NULL,
  `definition` TEXT NULL,
  `definition_source` TEXT NULL,
  `used_indicator` TINYINT(1) NOT NULL DEFAULT '1' COMMENT 'This indicates whether the code value is allowed to be used or not in that code list context.',
  `locked_indicator` TINYINT(1) NOT NULL DEFAULT '0' COMMENT 'This indicates whether the Used_Indicator can be changed from False to True. In other words, if the code value is derived from its base code value and the Used_Indicator of the base is False, then the Used_Indicator cannot be changed from False to True in the derivation if the Locked_Indicator is true.',
  `extension_Indicator` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`code_list_value_id`),
  CONSTRAINT `Owner_Code_List_ID`
  FOREIGN KEY (`code_list_id`)
  REFERENCES `oagsrt_revision`.`code_list` (`code_list_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`ctx_category`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`ctx_category` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`ctx_category` (
  `ctx_category_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `guid` VARCHAR(41) NOT NULL,
  `name` VARCHAR(45) NULL,
  `description` TEXT NULL,
  PRIMARY KEY (`ctx_category_id`))
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`classification_ctx_scheme`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`classification_ctx_scheme` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`classification_ctx_scheme` (
  `classification_ctx_scheme_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'External identification of the scheme.',
  `guid` VARCHAR(41) NOT NULL,
  `scheme_id` VARCHAR(45) CHARACTER SET 'utf8' NOT NULL COMMENT 'External identification of the scheme.',
  `scheme_name` VARCHAR(255) CHARACTER SET 'utf8' NULL,
  `description` TEXT CHARACTER SET 'utf8' NULL,
  `scheme_agency_id` VARCHAR(45) CHARACTER SET 'utf8' NOT NULL,
  `scheme_version_id` VARCHAR(45) CHARACTER SET 'utf8' NOT NULL,
  `ctx_category_id` INT(11) UNSIGNED NOT NULL,
  `created_by` INT(11) NOT NULL,
  `last_updated_by` INT(11) NOT NULL,
  `creation_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_update_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`classification_ctx_scheme_id`),
  CONSTRAINT `Context_Category`
  FOREIGN KEY (`ctx_category_id`)
  REFERENCES `oagsrt_revision`.`ctx_category` (`ctx_category_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`ctx_scheme_value`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`ctx_scheme_value` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`ctx_scheme_value` (
  `ctx_scheme_value_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `guid` VARCHAR(41) NOT NULL,
  `value` VARCHAR(45) NOT NULL,
  `meaning` TEXT NULL,
  `owner_ctx_scheme_id` INT(11) UNSIGNED NULL,
  PRIMARY KEY (`ctx_scheme_value_id`),
  CONSTRAINT `Owner_Context_Scheme`
  FOREIGN KEY (`owner_ctx_scheme_id`)
  REFERENCES `oagsrt_revision`.`classification_ctx_scheme` (`classification_ctx_scheme_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`biz_ctx_value`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`biz_ctx_value` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`biz_ctx_value` (
  `biz_ctx_value_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `biz_ctx_id` INT(11) UNSIGNED NOT NULL,
  `ctx_scheme_value_id` INT(11) UNSIGNED NOT NULL,
  PRIMARY KEY (`biz_ctx_value_id`),
  CONSTRAINT `Business_Context1`
  FOREIGN KEY (`biz_ctx_id`)
  REFERENCES `oagsrt_revision`.`biz_ctx` (`biz_ctx_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `Context_Scheme_Value1`
  FOREIGN KEY (`ctx_scheme_value_id`)
  REFERENCES `oagsrt_revision`.`ctx_scheme_value` (`ctx_scheme_value_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`bbie_sc`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`bbie_sc` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`bbie_sc` (
  `bbie_sc_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `bbie_id` INT(11) UNSIGNED NOT NULL,
  `dt_sc_id` INT(11) UNSIGNED NOT NULL COMMENT 'This should correspond to the DT_SC of the BDT of the based BCC and BCCP.',
  `dt_sc_pri_restri_id` INT(11) UNSIGNED NULL COMMENT 'This must be one of the allowed primitive/code list as specified in the corresponding SC of the based BCC of the BBIE.',
  `code_list_id` INT(11) UNSIGNED NULL,
  `agency_id_list_id` INT(11) UNSIGNED NULL,
  `min_cardinality` INT(11) NOT NULL,
  `max_cardinality` INT(11) NULL,
  `default_value` TEXT NULL,
  `fixed_value` TEXT NULL,
  `definition` TEXT NULL,
  `remark` VARCHAR(225) NULL,
  `biz_term` VARCHAR(225) NULL,
  `is_used` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Flag to indicate whether the field/component is used in the content model. It indicates whether the field/component should be generated.',
  PRIMARY KEY (`bbie_sc_id`),
  CONSTRAINT `BBIE_SC`
  FOREIGN KEY (`dt_sc_id`)
  REFERENCES `oagsrt_revision`.`dt_sc` (`dt_sc_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `BBIE_SC_Primitive`
  FOREIGN KEY (`dt_sc_pri_restri_id`)
  REFERENCES `oagsrt_revision`.`bdt_sc_pri_restri` (`bdt_sc_pri_restri_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `Owner_BBIE`
  FOREIGN KEY (`bbie_id`)
  REFERENCES `oagsrt_revision`.`bbie` (`bbie_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `BBIE_SC_Code_List_Primitive`
  FOREIGN KEY (`code_list_id`)
  REFERENCES `oagsrt_revision`.`code_list` (`code_list_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `BBIE_SC_Agency_ID_List_Primitive`
  FOREIGN KEY (`agency_id_list_id`)
  REFERENCES `oagsrt_revision`.`agency_id_list` (`agency_id_list_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`app_user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`app_user` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`app_user` (
  `app_user_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `login_id` VARCHAR(45) NOT NULL,
  `password` VARCHAR(45) NOT NULL,
  `name` TEXT NULL,
  `organization` TEXT NULL,
  `oagis_developer_indicator` TINYINT(1) NOT NULL COMMENT 'This indicates whether the user can edit OAGIS Model content. Content created by the OAGIS developer is also considered OAGIS Model content.',
  PRIMARY KEY (`app_user_id`))
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`bie_user_ext_revision`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`bie_user_ext_revision` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`bie_user_ext_revision` (
  `bie_user_ext_revision_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Auto-generate primary key of the table.',
  `top_level_abie_id` INT(11) UNSIGNED NOT NULL COMMENT 'This points to an ABIE record which is a top-level ABIE. The record must have the isTop_Level flag true.',
  `ext_abie_id` INT(11) UNSIGNED NULL COMMENT 'This points to an ABIE record corresponding to the Extension_ACC_ID record. For example, this column can point to the ApplicationAreaExtension ABIE which is based on the ApplicationAreaExtension ACC (referred by the Extension_ACC_ID column). This column can be Null only when the extension is the AllExtension because there is no corresponding ABIE for the AllExtension ACC.',
  `ext_acc_id` INT(11) UNSIGNED NOT NULL COMMENT 'This points to an extension ACC on which the ABIE indicated by the Extension_ABIE_ID column is based. E.g. It may point to an ApplicationAreaExtension ACC, AllExtension ACC, ActualLedgerExtension ACC, etc. It should be noted that an ACC record pointed to must have the OAGIS_Component_Type = 2 (Extension).',
  `user_ext_acc_id` INT(11) UNSIGNED NOT NULL COMMENT 'This column points to the specific revision of a User Extension ACC (this is an ACC whose OAGIS_Component_Type = 4) currently used by the ABIE as indicated by the Extension_ABIE_ID or the by the Top_Level_ABIE_ID (in case of the AllExtension). ',
  `revised_indicator` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'This column is a flag indicating to whether the User Extension ACC (as identified in the User_Extension_ACC_ID column) has been revised, i.e., there is a newer version of the user extension ACC than the one currently used by the Extension_ABIE_ID. 0 means the User_Extension_ACC_ID is current, 1 means it is not current.',
  PRIMARY KEY (`bie_user_ext_revision_id`),
  CONSTRAINT `Top_Level_ABIE`
  FOREIGN KEY (`top_level_abie_id`)
  REFERENCES `oagsrt_revision`.`abie` (`abie_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `Extension_ABIE`
  FOREIGN KEY (`ext_abie_id`)
  REFERENCES `oagsrt_revision`.`abie` (`abie_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `Extension_ACC`
  FOREIGN KEY (`ext_acc_id`)
  REFERENCES `oagsrt_revision`.`acc` (`acc_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `User_Extension_ACC`
  FOREIGN KEY (`user_ext_acc_id`)
  REFERENCES `oagsrt_revision`.`acc` (`acc_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  COMMENT = 'This table keeps track of the User Extension ACC (the specific revision) used by an Extension ABIE. This can be a named extension (such as ApplicationAreaExtension) or the AllExtension. The Revised_Indicator flag is designed such that a revision of a User Extension can notify by setting this flag to true. The Top_Level_ABIE_ID column makes it more efficient to when opening a top-level ABIE, the user can be notified of any new revision extension. A record in this table is created only when there is a user extension to the the OAGIS extension component/ACC.';


-- -----------------------------------------------------
-- Table `oagsrt_revision`.`blob_content`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `oagsrt_revision`.`blob_content` ;

CREATE TABLE IF NOT EXISTS `oagsrt_revision`.`blob_content` (
  `blob_content_id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key.',
  `content` MEDIUMBLOB NOT NULL COMMENT 'The Blob content of the schema file.',
  `release_id` INT(11) UNSIGNED NOT NULL COMMENT 'The release to which this file belongs/published.',
  `module` TEXT(100) NOT NULL COMMENT 'The is the subdirectory and filename of the blob. The format is Windows file path. The starting directory shall be the root folder of all the release content. For example, for OAGIS 10.1 Model, the root directory is Model. If the file shall be directly under the Model directory, then this column should be \'Model\\filename.xsd\'. If the file is under, say, Model\\Platform\\2_1\\Common\\Components directory, then the value of this column shall be \'Model\\Platform\\2_1\\Common\\Components\\filename.xsd\'.',
  PRIMARY KEY (`blob_content_id`),
  CONSTRAINT `release`
  FOREIGN KEY (`release_id`)
  REFERENCES `oagsrt_revision`.`release` (`release_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  COMMENT = 'This table stores schemas in Blob.';


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
