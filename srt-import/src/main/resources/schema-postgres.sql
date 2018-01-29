DROP TABLE ABIE CASCADE ;

DROP TABLE ACC CASCADE ;

DROP TABLE AGENCY_ID_LIST CASCADE ;

DROP TABLE AGENCY_ID_LIST_VALUE CASCADE ;

DROP TABLE APP_USER CASCADE ;

DROP TABLE ASBIE CASCADE ;

DROP TABLE ASBIEP CASCADE ;

DROP TABLE ASCC CASCADE ;

DROP TABLE ASCCP CASCADE ;

DROP TABLE BBIE CASCADE ;

DROP TABLE BBIEP CASCADE ;

DROP TABLE BBIE_SC CASCADE ;

DROP TABLE BCC CASCADE ;

DROP TABLE BCCP CASCADE ;

DROP TABLE BDT_PRI_RESTRI CASCADE ;

DROP TABLE BDT_SC_PRI_RESTRI CASCADE ;

DROP TABLE BIE_USAGE_RULE CASCADE ;

DROP TABLE BIE_USER_EXT_REVISION CASCADE ;

DROP TABLE BIZ_CTX CASCADE ;

DROP TABLE BIZ_CTX_VALUE CASCADE ;

DROP TABLE BLOB_CONTENT CASCADE ;

DROP TABLE CDT_AWD_PRI CASCADE ;

DROP TABLE CDT_AWD_PRI_XPS_TYPE_MAP CASCADE ;

DROP TABLE CDT_PRI CASCADE ;

DROP TABLE CDT_SC_AWD_PRI CASCADE ;

DROP TABLE CDT_SC_AWD_PRI_XPS_TYPE_MAP CASCADE ;

DROP TABLE CLIENT CASCADE ;

DROP TABLE CODE_LIST CASCADE ;

DROP TABLE CODE_LIST_VALUE CASCADE ;

DROP TABLE CTX_CATEGORY CASCADE ;

DROP TABLE CTX_SCHEME CASCADE ;

DROP TABLE CTX_SCHEME_VALUE CASCADE ;

DROP TABLE DT CASCADE ;

DROP TABLE DT_SC CASCADE ;

DROP TABLE DT_USAGE_RULE CASCADE ;

DROP TABLE MODULE CASCADE ;

DROP TABLE MODULE_DEP CASCADE ;

DROP TABLE NAMESPACE CASCADE ;

DROP TABLE RELEASE CASCADE ;

DROP TABLE TOP_LEVEL_ABIE CASCADE ;

DROP TABLE USAGE_RULE CASCADE ;

DROP TABLE USAGE_RULE_EXPRESSION CASCADE ;

DROP TABLE XBT CASCADE ;

CREATE SEQUENCE ABIE_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE ACC_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE AGENCY_ID_LIST_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE AGENCY_ID_LIST_VALUE_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE APP_USER_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE ASBIEP_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE ASBIE_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE ASCCP_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE ASCC_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE BBIEP_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE BBIE_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE BBIE_SC_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE BCCP_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE BCC_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE BDT_PRI_RESTRI_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE BDT_SC_PRI_RESTRI_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE BIE_USAGE_RULE_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE BIE_USER_EXT_REVISION_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE BIZ_CTX_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE BIZ_CTX_VALUE_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE BLOB_CONTENT_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE CDT_AWD_PRI_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE CDT_PRI_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE CDT_SC_AWD_PRI_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE CLIENT_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE CODE_LIST_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE CODE_LIST_VALUE_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE CTX_CATEGORY_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE CTX_SCHEME_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE CTX_SCHEME_VALUE_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE DT_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE DT_SC_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE DT_USAGE_RULE_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE MODULE_DEP_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE MODULE_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE NAMESPACE_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE RELEASE_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE TOP_LEVEL_ABIE_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE USAGE_RULE_EXPRESSION_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE USAGE_RULE_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

CREATE SEQUENCE XBT_ID_SEQ START WITH 1 INCREMENT BY 1 NO CYCLE ;

--  The ABIE table stores information about an ABIE, which is a contextualized
--  ACC. The context is represented by the BUSINESS_CTX_ID column that refers
--  to a business context. Each ABIE must have a business context and a based
--  ACC.
--  It should be noted that, per design document, there is no corresponding
--  ABIE created for an ACC which will not show up in the instance document
--  such as ACCs of OAGIS_COMPONENT_TYPE "SEMANTIC_GROUP",
--  "USER_EXTENSION_GROUP", etc.
CREATE TABLE ABIE
  (
    ABIE_ID      DECIMAL (19) NOT NULL DEFAULT NEXTVAL('ABIE_ID_SEQ'),
    GUID         VARCHAR (41) NOT NULL ,
    BASED_ACC_ID DECIMAL (19) NOT NULL ,
    BIZ_CTX_ID   DECIMAL (19) NOT NULL ,
    DEFINITION TEXT ,
    CREATED_BY            DECIMAL (19) NOT NULL ,
    LAST_UPDATED_BY       DECIMAL (19) NOT NULL ,
    CREATION_TIMESTAMP    TIMESTAMP WITH TIME ZONE NOT NULL ,
    LAST_UPDATE_TIMESTAMP TIMESTAMP WITH TIME ZONE NOT NULL ,
    STATE                 DECIMAL (10) ,
    CLIENT_ID             DECIMAL (19) ,
    VERSION               VARCHAR (45) ,
    STATUS                VARCHAR (45) ,
    REMARK                  VARCHAR (225) ,
    BIZ_TERM                VARCHAR (225) ,
    OWNER_TOP_LEVEL_ABIE_ID DECIMAL (19) NOT NULL
  )
  ;
COMMENT ON TABLE ABIE
IS
  'The ABIE table stores information about an ABIE, which is a contextualized ACC. The context is represented by the BUSINESS_CTX_ID column that refers to a business context. Each ABIE must have a business context and a based ACC.

It should be noted that, per design document, there is no corresponding ABIE created for an ACC which will not show up in the instance document such as ACCs of OAGIS_COMPONENT_TYPE "SEMANTIC_GROUP", "USER_EXTENSION_GROUP", etc.' ;
  COMMENT ON COLUMN ABIE.ABIE_ID
IS
  'A internal, primary database key of an ABIE.' ;
  COMMENT ON COLUMN ABIE.GUID
IS
  'A globally unique identifier (GUID) of an ABIE. GUID of an ABIE is different from its based ACC. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.' ;
  COMMENT ON COLUMN ABIE.BASED_ACC_ID
IS
  'A foreign key to the ACC table refering to the ACC, on which the business context has been applied to derive this ABIE.' ;
  COMMENT ON COLUMN ABIE.BIZ_CTX_ID
IS
  'A foreign key to the BIZ_CTX table. This column stores the business context assigned to the ABIE.' ;
  COMMENT ON COLUMN ABIE.DEFINITION
IS
  'Definition to override the ACC''s definition. If NULL, it means that the definition should be inherited from the based CC.' ;
  COMMENT ON COLUMN ABIE.CREATED_BY
IS
  'A foreign key referring to the user who creates the ABIE. The creator of the ABIE is also its owner by default. ABIEs created as children of another ABIE have the same CREATED_BY as its parent.' ;
  COMMENT ON COLUMN ABIE.LAST_UPDATED_BY
IS
  'A foreign key referring to the last user who has updated the ABIE record. This may be the user who is in the same group as the creator.' ;
  COMMENT ON COLUMN ABIE.CREATION_TIMESTAMP
IS
  'Timestamp when the ABIE record was first created. ABIEs created as children of another ABIE have the same CREATION_TIMESTAMP.' ;
  COMMENT ON COLUMN ABIE.LAST_UPDATE_TIMESTAMP
IS
  'The timestamp when the ABIE was last updated.' ;
  COMMENT ON COLUMN ABIE.STATE
IS
  '2 = EDITING, 4 = PUBLISHED. This column is only used with a top-level ABIE, because that is the only entry point for editing. The state value indicates the visibility of the top-level ABIE to users other than the owner. In the user group environment, a logic can apply that other users in the group can see the top-level ABIE only when it is in the ''Published'' state.' ;
  COMMENT ON COLUMN ABIE.CLIENT_ID
IS
  'This is a foreign key to the CLIENT table. The use case associated with this column is to indicate the organizational entity for which the profile BOD is created. For example, Boeing may generate a profile BOD for Boeing civilian or Boeing defense. It is more for the documentation purpose. Only an ABIE which is the top-level ABIE can use this column.' ;
  COMMENT ON COLUMN ABIE.VERSION
IS
  'This column hold a version number assigned by the user. This column is only used by the top-level ABIE. No format of version is enforced.' ;
  COMMENT ON COLUMN ABIE.STATUS
IS
  'This is different from the STATE column which is CRUD life cycle of an entity. The use case for this is to allow the user to indicate the usage status of a top-level ABIE (a profile BOD). An integration architect can use this column. Example values are ‘Prototype’, ‘Test’, and ‘Production’. Only the top-level ABIE can use this field.' ;
  COMMENT ON COLUMN ABIE.REMARK
IS
  'This column allows the user to specify very context-specific usage of the BIE. It is different from the DEFINITION column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be "Type of BOM should be recognized in the BOM/typeCode."' ;
  COMMENT ON COLUMN ABIE.BIZ_TERM
IS
  'To indicate what the BIE is called in a particular business context. With this current design, only one business term is allowed per business context.' ;
  COMMENT ON COLUMN ABIE.OWNER_TOP_LEVEL_ABIE_ID
IS
  'This is a foriegn key to the ABIE itself. It specifies the top-level ABIE which owns this ABIE record. For the ABIE that is a top-level ABIE itself, this column will have the same value as the ABIE_ID column. ' ;
  CREATE INDEX ABIE_OWNER_TOP_LVL_ABIE_ID_IDX ON ABIE
    (
      OWNER_TOP_LEVEL_ABIE_ID ASC
    )
    ;
CREATE UNIQUE INDEX ABIE_ABIE_ID_IDX ON ABIE
  (
    ABIE_ID ASC
  )
  ;
  CREATE INDEX ABIE_BASED_ACC_ID_IDX ON ABIE
    ( BASED_ACC_ID ASC
    ) ;
  CREATE INDEX ABIE_CREATED_BY_IDX ON ABIE
    ( CREATED_BY ASC
    ) ;
  CREATE INDEX ABIE_LAST_UPDATED_BY_IDX ON ABIE
    ( LAST_UPDATED_BY ASC
    ) ;
  CREATE INDEX ABIE_CLIENT_ID_IDX ON ABIE
    ( CLIENT_ID ASC
    ) ;
  CREATE INDEX ABIE_BIZ_CTX_ID_IDX ON ABIE
    ( BIZ_CTX_ID ASC
    ) ;
ALTER TABLE ABIE ADD CONSTRAINT ABIE_PK PRIMARY KEY ( ABIE_ID ) ;


--  The ACC table holds information about complex data structured concepts. For
--  example, OAGIS's Components, Nouns, and BODs are captured in the ACC table.
--  Note that only Extension is supported when deriving ACC from another ACC.
--  (So if there is a restriction needed, maybe that concept should placed
--  higher in the derivation hierarchy rather than lower.)
--  In OAGIS, all XSD extensions will be treated as a qualification of an ACC.
CREATE TABLE ACC
  (
    ACC_ID            DECIMAL (19) NOT NULL DEFAULT NEXTVAL('ACC_ID_SEQ'),
    GUID              VARCHAR (41) NOT NULL ,
    OBJECT_CLASS_TERM VARCHAR (100) NOT NULL ,
    DEN               VARCHAR (200) NOT NULL ,
    DEFINITION TEXT ,
    DEFINITION_SOURCE      VARCHAR (100) ,
    BASED_ACC_ID           DECIMAL (19) ,
    OBJECT_CLASS_QUALIFIER VARCHAR (100) ,
    OAGIS_COMPONENT_TYPE   DECIMAL (10) ,
    MODULE_ID              DECIMAL (19) ,
    NAMESPACE_ID           DECIMAL (19) ,
    CREATED_BY             DECIMAL (19) NOT NULL ,
    OWNER_USER_ID          DECIMAL (19) NOT NULL ,
    LAST_UPDATED_BY        DECIMAL (19) NOT NULL ,
    CREATION_TIMESTAMP     TIMESTAMP WITH TIME ZONE NOT NULL ,
    LAST_UPDATE_TIMESTAMP  TIMESTAMP WITH TIME ZONE NOT NULL ,
    STATE                  DECIMAL (10) NOT NULL ,
    REVISION_NUM           DECIMAL (10) NOT NULL ,
    REVISION_TRACKING_NUM  DECIMAL (10) NOT NULL ,
    REVISION_ACTION        DECIMAL (3) ,
    RELEASE_ID             DECIMAL (19) ,
    CURRENT_ACC_ID         DECIMAL (19) ,
    IS_DEPRECATED          BOOLEAN ,
    IS_ABSTRACT            BOOLEAN
  )
  ;
COMMENT ON TABLE ACC
IS
  'The ACC table holds information about complex data structured concepts. For example, OAGIS''s Components, Nouns, and BODs are captured in the ACC table.

Note that only Extension is supported when deriving ACC from another ACC. (So if there is a restriction needed, maybe that concept should placed higher in the derivation hierarchy rather than lower.)

In OAGIS, all XSD extensions will be treated as a qualification of an ACC.' ;
  COMMENT ON COLUMN ACC.ACC_ID
IS
  'A internal, primary database key of an ACC.' ;
  COMMENT ON COLUMN ACC.GUID
IS
  'A globally unique identifier (GUID) of an ACC. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.' ;
  COMMENT ON COLUMN ACC.OBJECT_CLASS_TERM
IS
  'Object class name of the ACC concept. For OAGIS, this is generally name of a type with the "Type" truncated from the end. Per CCS the name is space separated. "ID" is expanded to "Identifier".' ;
  COMMENT ON COLUMN ACC.DEN
IS
  'DEN (dictionary entry name) of the ACC. It can be derived as OBJECT_CLASS_QUALIFIER + "_ " + OBJECT_CLASS_TERM + ". Details".' ;
  COMMENT ON COLUMN ACC.DEFINITION
IS
  'This is a documentation or description of the ACC. Since ACC is business context independent, this is a business context independent description of the ACC concept.' ;
  COMMENT ON COLUMN ACC.DEFINITION_SOURCE
IS
  'This is typically a URL identifying the source of the DEFINITION column.' ;
  COMMENT ON COLUMN ACC.BASED_ACC_ID
IS
  'BASED_ACC_ID is a foreign key to the ACC table itself. It represents the ACC that is qualified by this ACC. In general CCS sense, a qualification can be a content extension or restriction, but the current scope supports only extension.' ;
  COMMENT ON COLUMN ACC.OBJECT_CLASS_QUALIFIER
IS
  'This column stores the qualifier of an ACC, particularly when it has a based ACC. ' ;
  COMMENT ON COLUMN ACC.OAGIS_COMPONENT_TYPE
IS
  'The value can be 0 = BASE, 1 = SEMANTICS, 2 = EXTENSION, 3 = SEMANTIC_GROUP, 4 = USER_EXTENSION_GROUP, 5 = EMBEDDED. Generally, BASE is assigned when the OBJECT_CLASS_TERM contains "Base" at the end. EXTENSION is assigned with the OBJECT_CLASS_TERM contains "Extension" at the end. SEMANTIC_GROUP is assigned when an ACC is imported from an XSD Group. USER_EXTENSION_GROUP is a wrapper ACC (a virtual ACC) for segregating user''s extension content. EMBEDDED is used for an ACC whose content is not explicitly defined in the database, for example, the Any Structured Content ACC that corresponds to the xsd:any.  Other cases are assigned SEMANTICS. ' ;
  COMMENT ON COLUMN ACC.MODULE_ID
IS
  'Foreign key to the module table indicating the physical schema the ACC belongs to.' ;
  COMMENT ON COLUMN ACC.NAMESPACE_ID
IS
  'Foreign key to the Namespace table. This is the namespace to which the entity belongs. This namespace column is only used in the case the component is a user''s component.' ;
  COMMENT ON COLUMN ACC.CREATED_BY
IS
  'A foreign key referring to the user who creates the entity.

This column never change between the history and the current record. The history record should have the same value as that of its current record.' ;
  COMMENT ON COLUMN ACC.OWNER_USER_ID
IS
  'This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.

The ownership can change throughout the history, but undoing shouldn''t rollback the ownership. ' ;
  COMMENT ON COLUMN ACC.LAST_UPDATED_BY
IS
  'A foreign key referring to the last user who updated the record.

In the history record, this should always be the user who is editing the entity.' ;
  COMMENT ON COLUMN ACC.CREATION_TIMESTAMP
IS
  'Timestamp when the revision of the ACC was created.

This never change for a revision.' ;
  COMMENT ON COLUMN ACC.LAST_UPDATE_TIMESTAMP
IS
  'The timestamp when the record was last updated.

The value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.' ;
  COMMENT ON COLUMN ACC.STATE
IS
  '1 = EDITING, 2 = CANDIDATE, 3 = PUBLISHED. This the revision life cycle state of the ACC.

State change can''t be undone. But the history record can still keep the records of when the state was changed.' ;
  COMMENT ON COLUMN ACC.REVISION_NUM
IS
  'REVISION_NUM is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).' ;
  COMMENT ON COLUMN ACC.REVISION_TRACKING_NUM
IS
  'REVISION_TRACKING_NUM supports the ability to undo changes during a revision (life cycle of a revision is from the component''s EDITING state to PUBLISHED state). Once the component has transitioned into the PUBLISHED state for its particular revision, all revision tracking records are deleted except the latest one. REVISION_TRACKING_NUMB can be 0, 1, 2, and so on. The zero value is assigned to the record with REVISION_NUM = 0 as a default.' ;
  COMMENT ON COLUMN ACC.REVISION_ACTION
IS
  'This indicates the action associated with the record. The action can be 1 = INSERT, 2 = UPDATE, and 3 = DELETE. This column is null for the current record.' ;
  COMMENT ON COLUMN ACC.RELEASE_ID
IS
  'RELEASE_ID is an incremental integer. It is an unformatted counter part of the RELEASE_DECIMAL in the RELEASE table. RELEASE_ID can be 1, 2, 3, and so on. A release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the REVISION_ACTION column).

Not all component revisions have an associated RELEASE_ID because some revisions may never be released. USER_EXTENSION_GROUP component type is never part of a release.

Unpublished components cannot be released.' ;
  COMMENT ON COLUMN ACC.CURRENT_ACC_ID
IS
  'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the the record whose REVISION_NUM is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.

It is noted that although this is a foreign key by definition, we don''t specify a foreign key in the data model. This is because when an entity is deleted the current record won''t exist anymore.

The value of this column for the current record should be left NULL.' ;
  COMMENT ON COLUMN ACC.IS_DEPRECATED
IS
  'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be allowed).' ;
  COMMENT ON COLUMN ACC.IS_ABSTRACT
IS
  'This is the XML Schema abstract flag. Default is false. If it is true, the abstract flag will be set to true when generating a corresponding xsd:complexType. So although this flag may not apply to some ACCs such as those that are xsd:group. It is still have a false value.' ;
CREATE UNIQUE INDEX ACC_ACC_ID_IDX ON ACC
  (
    ACC_ID ASC
  )
  ;
  CREATE INDEX ACC_BASED_ACC_ID_IDX ON ACC
    ( BASED_ACC_ID ASC
    ) ;
  CREATE INDEX ACC_MODULE_ID_IDX ON ACC
    ( MODULE_ID ASC
    ) ;
  CREATE INDEX ACC_NAMESPACE_ID_IDX ON ACC
    ( NAMESPACE_ID ASC
    ) ;
  CREATE INDEX ACC_CREATED_BY_IDX ON ACC
    ( CREATED_BY ASC
    ) ;
  CREATE INDEX ACC_OWNER_USER_ID_IDX ON ACC
    ( OWNER_USER_ID ASC
    ) ;
  CREATE INDEX ACC_LAST_UPDATED_BY_IDX ON ACC
    ( LAST_UPDATED_BY ASC
    ) ;
  CREATE INDEX ACC_RELEASE_ID_IDX ON ACC
    ( RELEASE_ID ASC
    ) ;
  CREATE INDEX ACC_CURRENT_ACC_ID_IDX ON ACC
    ( CURRENT_ACC_ID ASC
    ) ;
ALTER TABLE ACC ADD CONSTRAINT ACC_PK PRIMARY KEY ( ACC_ID ) ;


--  The AGENCY_ID_LIST table stores information about agency identification
--  lists. The list's values are however kept in the AGENCY_ID_LIST_VALUE.
CREATE TABLE AGENCY_ID_LIST
  (
    AGENCY_ID_LIST_ID       DECIMAL (19) NOT NULL DEFAULT NEXTVAL('AGENCY_ID_LIST_ID_SEQ'),
    GUID                    VARCHAR (41) ,
    ENUM_TYPE_GUID          VARCHAR (41) NOT NULL ,
    NAME                    VARCHAR (100) ,
    LIST_ID                 VARCHAR (10) ,
    AGENCY_ID_LIST_VALUE_ID DECIMAL (19) ,
    VERSION_ID              VARCHAR (10) ,
    MODULE_ID               DECIMAL (19) ,
    DEFINITION TEXT
  )
  ;
COMMENT ON TABLE AGENCY_ID_LIST
IS
  'The AGENCY_ID_LIST table stores information about agency identification lists. The list''s values are however kept in the AGENCY_ID_LIST_VALUE.' ;
  COMMENT ON COLUMN AGENCY_ID_LIST.AGENCY_ID_LIST_ID
IS
  'A internal, primary database key.' ;
  COMMENT ON COLUMN AGENCY_ID_LIST.GUID
IS
  'A globally unique identifier (GUID) of an agency identifier scheme. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.' ;
  COMMENT ON COLUMN AGENCY_ID_LIST.ENUM_TYPE_GUID
IS
  'This column stores the GUID of the type containing the enumerated values. In OAGIS, most code lists and agnecy ID lists are defined by an XyzCodeContentType (or XyzAgencyIdentificationContentType) and XyzCodeEnumerationType (or XyzAgencyIdentificationEnumerationContentType). However, some don''t have the enumeration type. When that is the case, this column is null.' ;
  COMMENT ON COLUMN AGENCY_ID_LIST.NAME
IS
  'Name of the agency identification list.' ;
  COMMENT ON COLUMN AGENCY_ID_LIST.LIST_ID
IS
  'This is a business or standard identification assigned to the agency identification list.' ;
  COMMENT ON COLUMN AGENCY_ID_LIST.AGENCY_ID_LIST_VALUE_ID
IS
  'This is the identification of the agency or organization which developed and/or maintains the list. Theoretically, this can be modeled as a self-reference foreign key, but it is not implemented at this point.' ;
  COMMENT ON COLUMN AGENCY_ID_LIST.VERSION_ID
IS
  'Version number of the agency identification list (assigned by the agency).' ;
  COMMENT ON COLUMN AGENCY_ID_LIST.MODULE_ID
IS
  'Foreign key to the module table indicating the physical schema the MODULE belongs to.' ;
  COMMENT ON COLUMN AGENCY_ID_LIST.DEFINITION
IS
  'Description of the agency identification list.' ;
CREATE UNIQUE INDEX AGENCY_ID_LIST_PK_IDX ON AGENCY_ID_LIST
  (
    AGENCY_ID_LIST_ID ASC
  )
  ;
  CREATE INDEX AGENCY_ID_LIST_AILV_ID_IDX ON AGENCY_ID_LIST
    (
      AGENCY_ID_LIST_VALUE_ID ASC
    )
    ;
  CREATE INDEX AGENCY_ID_LIST_MODULE_ID_IDX ON AGENCY_ID_LIST
    (
      MODULE_ID ASC
    ) ;
ALTER TABLE AGENCY_ID_LIST ADD CONSTRAINT AGENCY_ID_LIST_PK PRIMARY KEY ( AGENCY_ID_LIST_ID ) ;
ALTER TABLE AGENCY_ID_LIST ADD CONSTRAINT AGENCY_ID_LIST_UK1 UNIQUE ( GUID ) ;
ALTER TABLE AGENCY_ID_LIST ADD CONSTRAINT AGENCY_ID_LIST_UK2 UNIQUE ( ENUM_TYPE_GUID ) ;


--  This table captures the values within an agency identification list.
CREATE TABLE AGENCY_ID_LIST_VALUE
  (
    AGENCY_ID_LIST_VALUE_ID DECIMAL (19) NOT NULL DEFAULT NEXTVAL('AGENCY_ID_LIST_VALUE_ID_SEQ'),
    VALUE                   VARCHAR (150) NOT NULL ,
    NAME                    VARCHAR (150) ,
    DEFINITION TEXT ,
    OWNER_LIST_ID DECIMAL (19) NOT NULL
  )
  ;
COMMENT ON TABLE AGENCY_ID_LIST_VALUE
IS
  'This table captures the values within an agency identification list.' ;
  COMMENT ON COLUMN AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID
IS
  'Primary key column.' ;
  COMMENT ON COLUMN AGENCY_ID_LIST_VALUE.VALUE
IS
  'A value in the agency identification list.' ;
  COMMENT ON COLUMN AGENCY_ID_LIST_VALUE.NAME
IS
  'Descriptive or short name of the value.' ;
  COMMENT ON COLUMN AGENCY_ID_LIST_VALUE.DEFINITION
IS
  'The meaning of the value.' ;
  COMMENT ON COLUMN AGENCY_ID_LIST_VALUE.OWNER_LIST_ID
IS
  'Foreign key to the agency identification list in the AGENCY_ID_LIST table this value belongs to.' ;
CREATE UNIQUE INDEX AILV_PK_IDX ON AGENCY_ID_LIST_VALUE
  (
    AGENCY_ID_LIST_VALUE_ID ASC
  )
  ;
  CREATE INDEX AILV_OWNER_LIST_ID_IDX ON AGENCY_ID_LIST_VALUE
    (
      OWNER_LIST_ID ASC
    )
    ;
ALTER TABLE AGENCY_ID_LIST_VALUE ADD CONSTRAINT AILV_PK PRIMARY KEY ( AGENCY_ID_LIST_VALUE_ID ) ;


--  This table captures the user information for authentication and
--  authorization purposes.
CREATE TABLE APP_USER
  (
    APP_USER_ID               DECIMAL (19) NOT NULL DEFAULT NEXTVAL('APP_USER_ID_SEQ'),
    LOGIN_ID                  VARCHAR (45) NOT NULL ,
    PASSWORD                  VARCHAR (100) NOT NULL ,
    NAME                      VARCHAR (100) ,
    ORGANIZATION              VARCHAR (100) ,
    OAGIS_DEVELOPER_INDICATOR BOOLEAN NOT NULL
  )
  ;
COMMENT ON TABLE APP_USER
IS
  'This table captures the user information for authentication and authorization purposes.' ;
  COMMENT ON COLUMN APP_USER.APP_USER_ID
IS
  'Primary key column.' ;
  COMMENT ON COLUMN APP_USER.LOGIN_ID
IS
  'User Id of the user.' ;
  COMMENT ON COLUMN APP_USER.PASSWORD
IS
  'Password to authenticate the user.' ;
  COMMENT ON COLUMN APP_USER.NAME
IS
  'Full name of the user.' ;
  COMMENT ON COLUMN APP_USER.ORGANIZATION
IS
  'The company the user represents.' ;
  COMMENT ON COLUMN APP_USER.OAGIS_DEVELOPER_INDICATOR
IS
  'This indicates whether the user can edit OAGIS Model content. Content created by the OAGIS developer is also considered OAGIS Model content.' ;
CREATE UNIQUE INDEX APP_USER_APP_USER_ID_IDX ON APP_USER
  (
    APP_USER_ID ASC
  )
  ;
ALTER TABLE APP_USER ADD CONSTRAINT APP_USER_PK PRIMARY KEY ( APP_USER_ID ) ;
ALTER TABLE APP_USER ADD CONSTRAINT APP_USER_UK1 UNIQUE ( LOGIN_ID ) ;


--  An ASBIE represents a relationship/association between two ABIEs through an
--  ASBIEP. It is a contextualization of an ASCC.
CREATE TABLE ASBIE
  (
    ASBIE_ID      DECIMAL (19) NOT NULL DEFAULT NEXTVAL('ASBIE_ID_SEQ'),
    GUID          VARCHAR (41) NOT NULL ,
    FROM_ABIE_ID  DECIMAL (19) NOT NULL ,
    TO_ASBIEP_ID  DECIMAL (19) NOT NULL ,
    BASED_ASCC_ID DECIMAL (19) NOT NULL ,
    DEFINITION TEXT ,
    CARDINALITY_MIN DECIMAL (10) NOT NULL ,
    CARDINALITY_MAX DECIMAL (10) NOT NULL ,
    IS_NILLABLE     BOOLEAN NOT NULL ,
    REMARK                  VARCHAR (225) ,
    CREATED_BY              DECIMAL (19) NOT NULL ,
    LAST_UPDATED_BY         DECIMAL (19) NOT NULL ,
    CREATION_TIMESTAMP      TIMESTAMP WITH TIME ZONE NOT NULL ,
    LAST_UPDATE_TIMESTAMP   TIMESTAMP WITH TIME ZONE NOT NULL ,
    SEQ_KEY                 DECIMAL (10,2) NOT NULL ,
    IS_USED                 BOOLEAN NOT NULL ,
    OWNER_TOP_LEVEL_ABIE_ID DECIMAL (19) NOT NULL
  )
  ;
COMMENT ON TABLE ASBIE
IS
  'An ASBIE represents a relationship/association between two ABIEs through an ASBIEP. It is a contextualization of an ASCC.' ;
  COMMENT ON COLUMN ASBIE.ASBIE_ID
IS
  'A internal, primary database key of an ASBIE.' ;
  COMMENT ON COLUMN ASBIE.GUID
IS
  'A globally unique identifier (GUID) of an ASBIE. GUID of an ASBIE is different from its based ASCC. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.' ;
  COMMENT ON COLUMN ASBIE.FROM_ABIE_ID
IS
  'A foreign key pointing to the ABIE table. FROM_ABIE_ID is basically  a parent data element (type) of the TO_ASBIEP_ID. FROM_ABIE_ID must be based on the FROM_ACC_ID in the BASED_ASCC except when the FROM_ACC_ID refers to an SEMANTIC_GROUP ACC or USER_EXTENSION_GROUP ACC.' ;
  COMMENT ON COLUMN ASBIE.TO_ASBIEP_ID
IS
  'A foreign key to the ASBIEP table. TO_ASBIEP_ID is basically a child data element of the FROM_ABIE_ID. TO_ASBIEP_ID must be based on the TO_ASCCP_ID in the BASED_ASCC.' ;
  COMMENT ON COLUMN ASBIE.BASED_ASCC_ID
IS
  'The BASED_ASCC_ID column refers to the ASCC record, which this ASBIE contextualizes.' ;
  COMMENT ON COLUMN ASBIE.DEFINITION
IS
  'Definition to override the ASCC definition. If NULL, it means that the definition should be derived from the based CC on the UI, expression generation, and any API.' ;
  COMMENT ON COLUMN ASBIE.CARDINALITY_MIN
IS
  'Minimum occurence constraint of the TO_ASBIEP_ID. A valid value is a non-negative integer.' ;
  COMMENT ON COLUMN ASBIE.CARDINALITY_MAX
IS
  'Maximum occurrence constraint of the TO_ASBIEP_ID. A valid value is an integer from -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.' ;
  COMMENT ON COLUMN ASBIE.IS_NILLABLE
IS
  'Indicate whether the TO_ASBIEP is allowed to be null.' ;
  COMMENT ON COLUMN ASBIE.REMARK
IS
  'This column allows the user to specify very context-specific usage of the BIE. It is different from the DEFINITION column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be "Type of BOM should be recognized in the BOM/typeCode."' ;
  COMMENT ON COLUMN ASBIE.CREATED_BY
IS
  'A foreign key referring to the user who creates the ASBIE. The creator of the ASBIE is also its owner by default. ASBIEs created as children of another ABIE have the same CREATED_BY.' ;
  COMMENT ON COLUMN ASBIE.LAST_UPDATED_BY
IS
  'A foreign key referring to the user who has last updated the ASBIE record. ' ;
  COMMENT ON COLUMN ASBIE.CREATION_TIMESTAMP
IS
  'Timestamp when the ASBIE record was first created. ASBIEs created as children of another ABIE have the same CREATION_TIMESTAMP.' ;
  COMMENT ON COLUMN ASBIE.LAST_UPDATE_TIMESTAMP
IS
  'The timestamp when the ASBIE was last updated.' ;
  COMMENT ON COLUMN ASBIE.SEQ_KEY
IS
  'This indicates the order of the associations among other siblings. The SEQ_KEY for BIEs is decimal in order to accomodate the removal of inheritance hierarchy and group. For example, children of the most abstract ACC will have SEQ_KEY = 1.1, 1.2, 1.3, and so on; and SEQ_KEY of the next abstraction level ACC will have SEQ_KEY = 2.1, 2.2, 2.3 and so on so forth.' ;
  COMMENT ON COLUMN ASBIE.IS_USED
IS
  'Flag to indicate whether the field/component is used in the content model. It signifies whether the field/component should be generated.' ;
  COMMENT ON COLUMN ASBIE.OWNER_TOP_LEVEL_ABIE_ID
IS
  'This is a foriegn key to the ABIE table. It specifies the top-level ABIE which owns this ASBIE record.' ;
  CREATE INDEX ASBIE_OWNER_TLVL_ABIE_ID_IDX ON ASBIE
    (
      OWNER_TOP_LEVEL_ABIE_ID ASC
    )
    ;
CREATE UNIQUE INDEX ASBIE_ASBIE_ID_IDX ON ASBIE
  (
    ASBIE_ID ASC
  )
  ;
  CREATE INDEX ASBIE_FROM_ABIE_ID_IDX ON ASBIE
    ( FROM_ABIE_ID ASC
    ) ;
  CREATE INDEX ASBIE_TO_ASBIEP_ID_IDX ON ASBIE
    ( TO_ASBIEP_ID ASC
    ) ;
  CREATE INDEX ASBIE_BASED_ASCC_ID_IDX ON ASBIE
    ( BASED_ASCC_ID ASC
    ) ;
  CREATE INDEX ASBIE_CREATED_BY_IDX ON ASBIE
    ( CREATED_BY ASC
    ) ;
  CREATE INDEX ASBIE_LAST_UPDATED_BY_IDX ON ASBIE
    ( LAST_UPDATED_BY ASC
    ) ;
ALTER TABLE ASBIE ADD CONSTRAINT ASBIE_PK PRIMARY KEY ( ASBIE_ID ) ;


--  ASBIEP represents a role in a usage of an ABIE. It is a contextualization
--  of an ASCCP.
CREATE TABLE ASBIEP
  (
    ASBIEP_ID       DECIMAL (19) NOT NULL DEFAULT NEXTVAL('ASBIEP_ID_SEQ'),
    GUID            VARCHAR (41) NOT NULL ,
    BASED_ASCCP_ID  DECIMAL (19) NOT NULL ,
    ROLE_OF_ABIE_ID DECIMAL (19) NOT NULL ,
    DEFINITION TEXT ,
    REMARK                  VARCHAR (225) ,
    BIZ_TERM                VARCHAR (225) ,
    CREATED_BY              DECIMAL (19) NOT NULL ,
    LAST_UPDATED_BY         DECIMAL (19) NOT NULL ,
    CREATION_TIMESTAMP      TIMESTAMP WITH TIME ZONE NOT NULL ,
    LAST_UPDATE_TIMESTAMP   TIMESTAMP WITH TIME ZONE NOT NULL ,
    OWNER_TOP_LEVEL_ABIE_ID DECIMAL (19) NOT NULL
  )
  ;
COMMENT ON TABLE ASBIEP
IS
  'ASBIEP represents a role in a usage of an ABIE. It is a contextualization of an ASCCP.' ;
  COMMENT ON COLUMN ASBIEP.ASBIEP_ID
IS
  'A internal, primary database key of an ASBIEP.' ;
  COMMENT ON COLUMN ASBIEP.GUID
IS
  'A globally unique identifier (GUID) of an ASBIEP. GUID of an ASBIEP is different from its based ASCCP. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.' ;
  COMMENT ON COLUMN ASBIEP.BASED_ASCCP_ID
IS
  'A foreign key point to the ASCCP record. It is the ASCCP which the ASBIEP contextualizes.' ;
  COMMENT ON COLUMN ASBIEP.ROLE_OF_ABIE_ID
IS
  'A foreign key pointing to the ABIE record. It is the ABIE which the property term in the based ASCCP qualifies. Note that the ABIE has to be derived from the ACC used by the based ASCCP.' ;
  COMMENT ON COLUMN ASBIEP.DEFINITION
IS
  'Definition to override the ASCCP''s Definition. If Null, it means that the definition should be derived from the based CC on the UI, expression generation, and any API.' ;
  COMMENT ON COLUMN ASBIEP.REMARK
IS
  'This column allows the user to specify very context-specific usage of the BIE. It is different from the Definition column in that the Definition column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be "Type of BOM should be recognized in the BOM/typeCode."' ;
  COMMENT ON COLUMN ASBIEP.BIZ_TERM
IS
  'This column represents a business term to indicate what the BIE is called in a particular business context. With this current design, only one business term is allowed per business context.' ;
  COMMENT ON COLUMN ASBIEP.CREATED_BY
IS
  'A foreign key referring to the user who creates the ASBIEP. The creator of the ASBIEP is also its owner by default. ASBIEPs created as children of another ABIE have the same CREATED_BY_USER_ID.' ;
  COMMENT ON COLUMN ASBIEP.LAST_UPDATED_BY
IS
  'A foreign key referring to the last user who has updated the ASBIEP record. ' ;
  COMMENT ON COLUMN ASBIEP.CREATION_TIMESTAMP
IS
  'Timestamp when the ASBIEP record was first created. ASBIEPs created as children of another ABIE have the same CREATION_TIMESTAMP.' ;
  COMMENT ON COLUMN ASBIEP.LAST_UPDATE_TIMESTAMP
IS
  'The timestamp when the ASBIEP was last updated.' ;
  COMMENT ON COLUMN ASBIEP.OWNER_TOP_LEVEL_ABIE_ID
IS
  'This is a foriegn key to the ABIE table. It specifies the top-level ABIE, which owns this ASBIEP record.' ;
  CREATE INDEX ASBIEP_OWNER_TLVL_ABIE_ID_IDX ON ASBIEP
    (
      OWNER_TOP_LEVEL_ABIE_ID ASC
    )
    ;
CREATE UNIQUE INDEX ASBIEP_ASBIEP_ID_IDX ON ASBIEP
  (
    ASBIEP_ID ASC
  )
  ;
  CREATE INDEX ASBIEP_BASED_ASCCP_ID_IDX ON ASBIEP
    ( BASED_ASCCP_ID ASC
    ) ;
  CREATE INDEX ASBIEP_ROLE_OF_ABIE_ID_IDX ON ASBIEP
    ( ROLE_OF_ABIE_ID ASC
    ) ;
  CREATE INDEX ASBIEP_CREATED_BY_IDX ON ASBIEP
    ( CREATED_BY ASC
    ) ;
  CREATE INDEX ASBIEP_LAST_UPDATED_BY_IDX ON ASBIEP
    ( LAST_UPDATED_BY ASC
    ) ;
ALTER TABLE ASBIEP ADD CONSTRAINT ASBIEP_PK PRIMARY KEY ( ASBIEP_ID ) ;


--  An ASCC represents a relationship/association between two ACCs through an
--  ASCCP.
CREATE TABLE ASCC
  (
    ASCC_ID         DECIMAL (19) NOT NULL DEFAULT NEXTVAL('ASCC_ID_SEQ'),
    GUID            VARCHAR (41) NOT NULL ,
    CARDINALITY_MIN DECIMAL (10) NOT NULL ,
    CARDINALITY_MAX DECIMAL (10) NOT NULL ,
    SEQ_KEY         DECIMAL (10) NOT NULL ,
    FROM_ACC_ID     DECIMAL (19) NOT NULL ,
    TO_ASCCP_ID     DECIMAL (19) NOT NULL ,
    DEN             VARCHAR (200) NOT NULL ,
    DEFINITION TEXT ,
    DEFINITION_SOURCE     VARCHAR (100) ,
    IS_DEPRECATED         BOOLEAN NOT NULL ,
    CREATED_BY            DECIMAL (19) NOT NULL ,
    OWNER_USER_ID         DECIMAL (19) NOT NULL ,
    LAST_UPDATED_BY       DECIMAL (19) NOT NULL ,
    CREATION_TIMESTAMP    TIMESTAMP WITH TIME ZONE NOT NULL ,
    LAST_UPDATE_TIMESTAMP TIMESTAMP WITH TIME ZONE NOT NULL ,
    STATE                 DECIMAL (10) NOT NULL ,
    REVISION_NUM          DECIMAL (10) NOT NULL ,
    REVISION_TRACKING_NUM DECIMAL (10) NOT NULL ,
    REVISION_ACTION       DECIMAL (3) ,
    RELEASE_ID            DECIMAL (19) ,
    CURRENT_ASCC_ID       DECIMAL (19)
  )
  ;
COMMENT ON TABLE ASCC
IS
  'An ASCC represents a relationship/association between two ACCs through an ASCCP. ' ;
  COMMENT ON COLUMN ASCC.ASCC_ID
IS
  'An internal, primary database key of an ASCC.' ;
  COMMENT ON COLUMN ASCC.GUID
IS
  'A globally unique identifier (GUID) of an ASCC. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.' ;
  COMMENT ON COLUMN ASCC.CARDINALITY_MIN
IS
  'Minimum occurrence of the TO_ASCCP_ID. The valid values are non-negative integer.' ;
  COMMENT ON COLUMN ASCC.CARDINALITY_MAX
IS
  'Maximum cardinality of the TO_ASCCP_ID. A valid value is integer -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.' ;
  COMMENT ON COLUMN ASCC.SEQ_KEY
IS
  'This indicates the order of the associations among other siblings. A valid value is positive integer. The SEQ_KEY at the CC side is localized. In other words, if an ACC is based on another ACC, SEQ_KEY of ASCCs or BCCs of the former ACC starts at 1 again. ' ;
  COMMENT ON COLUMN ASCC.FROM_ACC_ID
IS
  'FROM_ACC_ID is a foreign key pointing to an ACC record. It is basically pointing to a parent data element (type) of the TO_ASCCP_ID.' ;
  COMMENT ON COLUMN ASCC.TO_ASCCP_ID
IS
  'TO_ASCCP_ID is a foreign key to an ASCCP table record. It is basically pointing to a child data element of the FROM_ACC_ID. ' ;
  COMMENT ON COLUMN ASCC.DEN
IS
  'DEN (dictionary entry name) of the ASCC. This column can be derived from Qualifier and OBJECT_CLASS_TERM of the FROM_ACC_ID and DEN of the TO_ASCCP_ID as Qualifier + "_ " + OBJECT_CLASS_TERM + ". " + DEN. ' ;
  COMMENT ON COLUMN ASCC.DEFINITION
IS
  'This is a documentation or description of the ASCC. Since ASCC is business context independent, this is a business context independent description of the ASCC. Since there are definitions also in the ASCCP (as referenced by the TO_ASCCP_ID column) and the ACC under that ASCCP, definition in the ASCC is a specific description about the relationship between the ACC (as in FROM_ACC_ID) and the ASCCP.' ;
  COMMENT ON COLUMN ASCC.DEFINITION_SOURCE
IS
  'This is typically a URL identifying the source of the DEFINITION column.' ;
  COMMENT ON COLUMN ASCC.IS_DEPRECATED
IS
  'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).' ;
  COMMENT ON COLUMN ASCC.CREATED_BY
IS
  'A foreign key to the APP_USER table referring to the user who creates the entity.

This column never change between the history and the current record for a given revision. The history record should have the same value as that of its current record.' ;
  COMMENT ON COLUMN ASCC.OWNER_USER_ID
IS
  'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.

The ownership can change throughout the history, but undoing shouldn''t rollback the ownership. ' ;
  COMMENT ON COLUMN ASCC.LAST_UPDATED_BY
IS
  'A foreign key to the APP_USER table referring to the last user who has updated the record.

In the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).' ;
  COMMENT ON COLUMN ASCC.CREATION_TIMESTAMP
IS
  'Timestamp when the revision of the ASCC was created.

This never change for a revision.' ;
  COMMENT ON COLUMN ASCC.LAST_UPDATE_TIMESTAMP
IS
  'The timestamp when the record was last updated.

The value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the change has occurred.' ;
  COMMENT ON COLUMN ASCC.STATE
IS
  '1 = EDITING, 2 = CANDIDATE, 3 = PUBLISHED. This is the revision life cycle state of the entity.

State change can''t be undone. But the history record can still keep the records of when the state was changed.' ;
  COMMENT ON COLUMN ASCC.REVISION_NUM
IS
  'REVISION_NUM is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).' ;
  COMMENT ON COLUMN ASCC.REVISION_TRACKING_NUM
IS
  'REVISION_TRACKING_NUM supports the ability to undo changes during a revision (life cycle of a revision is from the component''s EDITING state to PUBLISHED state). Once the component has transitioned into the PUBLISHED state for its particular revision, all revision tracking records are deleted except the latest one. REVISION_TRACKING_NUM can be 0, 1, 2, and so on. The zero value is assign to the record with REVISION_NUM = 0 as a default.' ;
  COMMENT ON COLUMN ASCC.REVISION_ACTION
IS
  'This indicates the action associated with the record. The action can be 1 = INSERT, 2 = UPDATE, and 3 = DELETE. This column is null for the current record.' ;
  COMMENT ON COLUMN ASCC.RELEASE_ID
IS
  'RELEASE_ID is an incremental integer. It is an unformatted counterpart of the RELEASE_DECIMAL in the RELEASE table. RELEASE_ID can be 1, 2, 3, and so on. RELEASE_ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the REVISION_ACTION column).

Not all component revisions have an associated RELEASE_ID because some revisions may never be released.

Unpublished components cannot be released.' ;
  COMMENT ON COLUMN ASCC.CURRENT_ASCC_ID
IS
  'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the the record whose REVISION_NUM is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.

It is noted that although this is a foreign key by definition, we don''t specify a foreign key in the data model. This is because when an entity is deleted the current record won''t exist anymore.

The value of this column for the current record should be left NULL.' ;
CREATE UNIQUE INDEX ASCC_ASCC_ID_IDX ON ASCC
  (
    ASCC_ID ASC
  )
  ;
  CREATE INDEX ASCC_FROM_ACC_ID_IDX ON ASCC
    ( FROM_ACC_ID ASC
    ) ;
  CREATE INDEX ASCC_TO_ASCCP_ID_IDX ON ASCC
    ( TO_ASCCP_ID ASC
    ) ;
  CREATE INDEX ASCC_CREATED_BY_IDX ON ASCC
    ( CREATED_BY ASC
    ) ;
  CREATE INDEX ASCC_OWNER_USER_ID_IDX ON ASCC
    ( OWNER_USER_ID ASC
    ) ;
  CREATE INDEX ASCC_LAST_UPDATED_BY_IDX ON ASCC
    ( LAST_UPDATED_BY ASC
    ) ;
  CREATE INDEX ASCC_RELEASE_ID_IDX ON ASCC
    ( RELEASE_ID ASC
    ) ;
  CREATE INDEX ASCC_CURRENT_ASCC_ID_IDX ON ASCC
    ( CURRENT_ASCC_ID ASC
    ) ;
ALTER TABLE ASCC ADD CONSTRAINT ASCC_PK PRIMARY KEY ( ASCC_ID ) ;


--  An ASCCP specifies a role (or property) an ACC may play under another ACC.
CREATE TABLE ASCCP
  (
    ASCCP_ID      DECIMAL (19) NOT NULL DEFAULT NEXTVAL('ASCCP_ID_SEQ'),
    GUID          VARCHAR (41) NOT NULL ,
    PROPERTY_TERM VARCHAR (60) ,
    DEFINITION TEXT ,
    DEFINITION_SOURCE     VARCHAR (100) ,
    ROLE_OF_ACC_ID        DECIMAL (19) ,
    DEN                   VARCHAR (200) ,
    CREATED_BY            DECIMAL (19) NOT NULL ,
    OWNER_USER_ID         DECIMAL (19) NOT NULL ,
    LAST_UPDATED_BY       DECIMAL (19) NOT NULL ,
    CREATION_TIMESTAMP    TIMESTAMP WITH TIME ZONE NOT NULL ,
    LAST_UPDATE_TIMESTAMP TIMESTAMP WITH TIME ZONE NOT NULL ,
    STATE                 DECIMAL (10) NOT NULL ,
    MODULE_ID             DECIMAL (19) ,
    NAMESPACE_ID          DECIMAL (19) ,
    REUSABLE_INDICATOR    BOOLEAN ,
    IS_DEPRECATED         BOOLEAN NOT NULL ,
    REVISION_NUM          DECIMAL (10) NOT NULL ,
    REVISION_TRACKING_NUM DECIMAL (10) NOT NULL ,
    REVISION_ACTION       DECIMAL (3) ,
    RELEASE_ID            DECIMAL (19) ,
    CURRENT_ASCCP_ID      DECIMAL (19) ,
    IS_NILLABLE           BOOLEAN
  )
  ;
COMMENT ON TABLE ASCCP
IS
  'An ASCCP specifies a role (or property) an ACC may play under another ACC.' ;
  COMMENT ON COLUMN ASCCP.ASCCP_ID
IS
  'An internal, primary database key of an ASCCP.' ;
  COMMENT ON COLUMN ASCCP.GUID
IS
  'A globally unique identifier (GUID) of an ASCCP. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.' ;
  COMMENT ON COLUMN ASCCP.PROPERTY_TERM
IS
  'The role (or property) the ACC as referred to by the ROLE_Of_ACC_ID play when the ASCCP is used by another ACC.

There must be only one ASCCP without a PROPERTY_TERM for a particular ACC.' ;
  COMMENT ON COLUMN ASCCP.DEFINITION
IS
  'Description of the ASCCP.' ;
  COMMENT ON COLUMN ASCCP.DEFINITION_SOURCE
IS
  'This is typically a URL identifying the source of the DEFINITION column.' ;
  COMMENT ON COLUMN ASCCP.ROLE_OF_ACC_ID
IS
  'The ACC from which this ASCCP is created (ASCCP applies role to the ACC).' ;
  COMMENT ON COLUMN ASCCP.DEN
IS
  'The dictionary entry name of the ASCCP.' ;
  COMMENT ON COLUMN ASCCP.CREATED_BY
IS
  'Foreign key to the APP_USER table referring to the user who creates the entity.

This column never change between the history and the current record for a given revision. The history record should have the same value as that of its current record.' ;
  COMMENT ON COLUMN ASCCP.OWNER_USER_ID
IS
  'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.

The ownership can change throughout the history, but undoing shouldn''t rollback the ownership. ' ;
  COMMENT ON COLUMN ASCCP.LAST_UPDATED_BY
IS
  'Foreign key to the APP_USER table referring to the last user who has updated the record.

In the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).' ;
  COMMENT ON COLUMN ASCCP.CREATION_TIMESTAMP
IS
  'Timestamp when the revision of the ASCCP was created.

This never change for a revision.' ;
  COMMENT ON COLUMN ASCCP.LAST_UPDATE_TIMESTAMP
IS
  'The timestamp when the record was last updated.

The value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.' ;
  COMMENT ON COLUMN ASCCP.STATE
IS
  '1 = EDITING, 2 = CANDIDATE, 3 = PUBLISHED. This the revision life cycle state of the ACC.

State change can''t be undone. But the history record can still keep the records of when the state was changed.' ;
  COMMENT ON COLUMN ASCCP.MODULE_ID
IS
  'This column stores the name of the physical schema module the ASCCP belongs to. Right now the schema file name is assigned. In the future, this needs to be updated to a file path from the base of the release directory.' ;
  COMMENT ON COLUMN ASCCP.NAMESPACE_ID
IS
  'Foreign key to the NAMESPACE table. This is the namespace to which the entity belongs. This namespace column is primarily used in the case the component is a user''s component because there is also a namespace assigned at the release level.' ;
  COMMENT ON COLUMN ASCCP.REUSABLE_INDICATOR
IS
  'This indicates whether the ASCCP can be used by more than one ASCC. This maps directly to the XML schema local element declaration.' ;
  COMMENT ON COLUMN ASCCP.IS_DEPRECATED
IS
  'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).' ;
  COMMENT ON COLUMN ASCCP.REVISION_NUM
IS
  'REVISION_NUM is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).' ;
  COMMENT ON COLUMN ASCCP.REVISION_TRACKING_NUM
IS
  'REVISION_TRACKING_NUM supports the ability to undo changes during a revision (life cycle of a revision is from the component''s EDITING state to PUBLISHED state). Once the component has transitioned into the PUBLISHED state for its particular revision, all revision tracking records are deleted except the latest one. REVISION_TRACKING_NUMB can be 0, 1, 2, and so on. The zero value is assigned to the record with REVISION_NUM = 0 as a default.' ;
  COMMENT ON COLUMN ASCCP.REVISION_ACTION
IS
  'This indicates the action associated with the record. The action can be 1 = INSERT, 2 = UPDATE, and 3 = DELETE. This column is null for the current record.' ;
  COMMENT ON COLUMN ASCCP.RELEASE_ID
IS
  'RELEASE_ID is an incremental integer. It is an unformatted counter part of the RELEASE_DECIMAL in the RELEASE table. RELEASE_ID can be 1, 2, 3, and so on. A release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the REVISION_ACTION column).

Not all component revisions have an associated RELEASE_ID because some revisions may never be released. USER_EXTENSION_GROUP component type is never part of a release.

Unpublished components cannot be released.' ;
  COMMENT ON COLUMN ASCCP.CURRENT_ASCCP_ID
IS
  'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the the record whose REVISION_NUM is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.

It is noted that although this is a foreign key by definition, we don''t specify a foreign key in the data model. This is because when an entity is deleted the current record won''t exist anymore.

The value of this column for the current record should be left NULL.' ;
  COMMENT ON COLUMN ASCCP.IS_NILLABLE
IS
  'This is corresponding to the XML schema nillable flag. Although the nillable may not apply in certain cases of the ASCCP (e.g., when it corresponds to an XSD group), the value is default to false for simplification.' ;
CREATE UNIQUE INDEX ASCCP_ASCCP_ID_IDX ON ASCCP
  (
    ASCCP_ID ASC
  )
  ;
  CREATE INDEX ASCCP_ROLE_OF_ACC_ID_IDX ON ASCCP
    ( ROLE_OF_ACC_ID ASC
    ) ;
  CREATE INDEX ASCCP_CREATED_BY_IDX ON ASCCP
    ( CREATED_BY ASC
    ) ;
  CREATE INDEX ASCCP_OWNER_USER_ID_IDX ON ASCCP
    ( OWNER_USER_ID ASC
    ) ;
  CREATE INDEX ASCCP_LAST_UPDATED_BY_IDX ON ASCCP
    ( LAST_UPDATED_BY ASC
    ) ;
  CREATE INDEX ASCCP_MODULE_ID_IDX ON ASCCP
    ( MODULE_ID ASC
    ) ;
  CREATE INDEX ASCCP_NAMESPACE_ID_IDX ON ASCCP
    ( NAMESPACE_ID ASC
    ) ;
  CREATE INDEX ASCCP_RELEASE_ID_IDX ON ASCCP
    ( RELEASE_ID ASC
    ) ;
  CREATE INDEX ASCCP_CURRENT_ASCCP_ID_IDX ON ASCCP
    ( CURRENT_ASCCP_ID ASC
    ) ;
ALTER TABLE ASCCP ADD CONSTRAINT ASCCP_PK PRIMARY KEY ( ASCCP_ID ) ;


--  A BBIE represents a relationship/association between an ABIE and a BBIEP.
--  It is a contextualization of a BCC. The BBIE table also stores some
--  information about the specific constraints related to the BDT associated
--  with the BBIEP. In particular, the three columns including the
--  BDT_PRI_RESTRI_ID, CODE_LIST_ID, and AGENCY_ID_LIST_ID allows for capturing
--  of the specific primitive to be used in the context. Only one column among
--  the three can have a value in a particular record.
CREATE TABLE BBIE
  (
    BBIE_ID           DECIMAL (19) NOT NULL DEFAULT NEXTVAL('BBIE_ID_SEQ'),
    GUID              VARCHAR (41) NOT NULL ,
    BASED_BCC_ID      DECIMAL (19) NOT NULL ,
    FROM_ABIE_ID      DECIMAL (19) NOT NULL ,
    TO_BBIEP_ID       DECIMAL (19) NOT NULL ,
    BDT_PRI_RESTRI_ID DECIMAL (19) ,
    CODE_LIST_ID      DECIMAL (19) ,
    AGENCY_ID_LIST_ID DECIMAL (19) ,
    CARDINALITY_MIN   DECIMAL (10) NOT NULL ,
    CARDINALITY_MAX   DECIMAL (10) ,
    DEFAULT_VALUE TEXT ,
    IS_NILLABLE BOOLEAN NOT NULL ,
    FIXED_VALUE TEXT ,
    IS_NULL BOOLEAN NOT NULL ,
    DEFINITION TEXT ,
    REMARK                  VARCHAR (225) ,
    CREATED_BY              DECIMAL (19) NOT NULL ,
    LAST_UPDATED_BY         DECIMAL (19) NOT NULL ,
    CREATION_TIMESTAMP      TIMESTAMP WITH TIME ZONE NOT NULL ,
    LAST_UPDATE_TIMESTAMP   TIMESTAMP WITH TIME ZONE NOT NULL ,
    SEQ_KEY                 DECIMAL (10,2) ,
    IS_USED                 BOOLEAN ,
    OWNER_TOP_LEVEL_ABIE_ID DECIMAL (19) NOT NULL
  )
  ;
COMMENT ON TABLE BBIE
IS
  'A BBIE represents a relationship/association between an ABIE and a BBIEP. It is a contextualization of a BCC. The BBIE table also stores some information about the specific constraints related to the BDT associated with the BBIEP. In particular, the three columns including the BDT_PRI_RESTRI_ID, CODE_LIST_ID, and AGENCY_ID_LIST_ID allows for capturing of the specific primitive to be used in the context. Only one column among the three can have a value in a particular record.' ;
  COMMENT ON COLUMN BBIE.BBIE_ID
IS
  'A internal, primary database key of a BBIE.' ;
  COMMENT ON COLUMN BBIE.GUID
IS
  'A globally unique identifier (GUID) of an SC. GUID of a BBIE''s SC is different from the one in the DT_SC. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.' ;
  COMMENT ON COLUMN BBIE.BASED_BCC_ID
IS
  'The BASED_BCC_ID column refers to the BCC record, which this BBIE contextualizes.' ;
  COMMENT ON COLUMN BBIE.FROM_ABIE_ID
IS
  'FROM_ABIE_ID must be based on the FROM_ACC_ID in the BASED_BCC_ID.' ;
  COMMENT ON COLUMN BBIE.TO_BBIEP_ID
IS
  'TO_BBIEP_ID is a foreign key to the BBIEP table. TO_BBIEP_ID basically refers to a child data element of the FROM_ABIE_ID. TO_BBIEP_ID must be based on the TO_BCCP_ID in the based BCC.' ;
  COMMENT ON COLUMN BBIE.BDT_PRI_RESTRI_ID
IS
  'This is the foreign key to the BDT_PRI_RESTRI table. It indicates the primitive assigned to the BBIE (or also can be viewed as assigned to the BBIEP for this specific association). This is assigned by the user who authors the BIE. The assignment would override the default from the CC side.' ;
  COMMENT ON COLUMN BBIE.CODE_LIST_ID
IS
  'This is a foreign key to the CODE_LIST table. If a code list is assigned to the BBIE (or also can be viewed as assigned to the BBIEP for this association), then this column stores the assigned code list. It should be noted that one of the possible primitives assignable to the BDT_PRI_RESTRI_ID column may also be a code list. So this column is typically used when the user wants to assign another code list different from the one permissible by the CC model.' ;
  COMMENT ON COLUMN BBIE.AGENCY_ID_LIST_ID
IS
  'This is a foreign key to the AGENCY_ID_LIST table. It is used in the case that the BDT content can be restricted to an agency identification.' ;
  COMMENT ON COLUMN BBIE.CARDINALITY_MIN
IS
  'The minimum occurrence constraint for the BBIE. A valid value is a non-negative integer.' ;
  COMMENT ON COLUMN BBIE.CARDINALITY_MAX
IS
  'Maximum occurence constraint of the TO_BBIEP_ID. A valid value is an integer from -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.' ;
  COMMENT ON COLUMN BBIE.DEFAULT_VALUE
IS
  'This column specifies the default value constraint. Default and fixed value constraints cannot be used at the same time.' ;
  COMMENT ON COLUMN BBIE.IS_NILLABLE
IS
  'Indicate whether the field can have a null  This is corresponding to the nillable flag in the XML schema.' ;
  COMMENT ON COLUMN BBIE.FIXED_VALUE
IS
  'This column captures the fixed value constraint. Default and fixed value constraints cannot be used at the same time.' ;
  COMMENT ON COLUMN BBIE.IS_NULL
IS
  'This column indicates whether the field is fixed to NULL. IS_NULLl can be true only if the IS_NILLABLE is true. If IS_NULL is true then the FIX_VALUE and DEFAULT_VALUE columns cannot have a value.' ;
  COMMENT ON COLUMN BBIE.DEFINITION
IS
  'Description to override the BCC definition. If NULL, it means that the definition should be inherited from the based BCC.' ;
  COMMENT ON COLUMN BBIE.REMARK
IS
  'This column allows the user to specify very context-specific usage of the BIE. It is different from the DEFINITION column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be "Type of BOM should be recognized in the BOM/typeCode."' ;
  COMMENT ON COLUMN BBIE.CREATED_BY
IS
  'A foreign key referring to the user who creates the BBIE. The creator of the BBIE is also its owner by default. BBIEs created as children of another ABIE have the same CREATED_BY.' ;
  COMMENT ON COLUMN BBIE.LAST_UPDATED_BY
IS
  'A foreign key referring to the user who has last updated the ASBIE record. ' ;
  COMMENT ON COLUMN BBIE.CREATION_TIMESTAMP
IS
  'Timestamp when the BBIE record was first created. BBIEs created as children of another ABIE have the same CREATION_TIMESTAMP.' ;
  COMMENT ON COLUMN BBIE.LAST_UPDATE_TIMESTAMP
IS
  'The timestamp when the ASBIE was last updated.' ;
  COMMENT ON COLUMN BBIE.SEQ_KEY
IS
  'This indicates the order of the associations among other siblings. The SEQ_KEY for BIEs is decimal in order to accomodate the removal of inheritance hierarchy and group. For example, children of the most abstract ACC will have SEQ_KEY = 1.1, 1.2, 1.3, and so on; and SEQ_KEY of the next abstraction level ACC will have SEQ_KEY = 2.1, 2.2, 2.3 and so on so forth.' ;
  COMMENT ON COLUMN BBIE.IS_USED
IS
  'Flag to indicate whether the field/component is used in the content model. It indicates whether the field/component should be generated in the expression generation.' ;
  COMMENT ON COLUMN BBIE.OWNER_TOP_LEVEL_ABIE_ID
IS
  'This is a foriegn key to the ABIE table. It specifies the top-level ABIE, which owns this BBIE record.' ;
  CREATE INDEX BBIE_OWNER_TLVL_ABIE_ID_IDX ON BBIE
    (
      OWNER_TOP_LEVEL_ABIE_ID ASC
    )
    ;
CREATE UNIQUE INDEX BBIE_BBIE_ID_IDX ON BBIE
  (
    BBIE_ID ASC
  )
  ;
  CREATE INDEX BBIE_BASED_BCC_ID_IDX ON BBIE
    ( BASED_BCC_ID ASC
    ) ;
  CREATE INDEX BBIE_FROM_ABIE_ID_IDX ON BBIE
    ( FROM_ABIE_ID ASC
    ) ;
  CREATE INDEX BBIE_TO_BBIEP_ID_IDX ON BBIE
    ( TO_BBIEP_ID ASC
    ) ;
  CREATE INDEX BBIE_BDT_PRI_RESTRI_ID_IDX ON BBIE
    ( BDT_PRI_RESTRI_ID ASC
    ) ;
  CREATE INDEX BBIE_CODE_LIST_ID_IDX ON BBIE
    ( CODE_LIST_ID ASC
    ) ;
  CREATE INDEX BBIE_CREATED_BY_IDX ON BBIE
    ( CREATED_BY ASC
    ) ;
  CREATE INDEX BBIE_LAST_UPDATED_BY_IDX ON BBIE
    ( LAST_UPDATED_BY ASC
    ) ;
  CREATE INDEX BBIE_AGENCY_ID_LIST_ID_IDX ON BBIE
    ( AGENCY_ID_LIST_ID ASC
    ) ;
ALTER TABLE BBIE ADD CONSTRAINT BBIE_PK PRIMARY KEY ( BBIE_ID ) ;


--  BBIEP represents the usage of basic property in a specific business
--  context. It is a contextualization of a BCCP.
CREATE TABLE BBIEP
  (
    BBIEP_ID      DECIMAL (19) NOT NULL DEFAULT NEXTVAL('BBIEP_ID_SEQ'),
    GUID          VARCHAR (41) NOT NULL ,
    BASED_BCCP_ID DECIMAL (19) NOT NULL ,
    DEFINITION TEXT ,
    REMARK                  VARCHAR (225) ,
    BIZ_TERM                VARCHAR (225) ,
    CREATED_BY              DECIMAL (19) NOT NULL ,
    LAST_UPDATED_BY         DECIMAL (19) NOT NULL ,
    CREATION_TIMESTAMP      TIMESTAMP WITH TIME ZONE NOT NULL ,
    LAST_UPDATE_TIMESTAMP   TIMESTAMP WITH TIME ZONE NOT NULL ,
    OWNER_TOP_LEVEL_ABIE_ID DECIMAL (19) NOT NULL
  )
  ;
COMMENT ON TABLE BBIEP
IS
  'BBIEP represents the usage of basic property in a specific business context. It is a contextualization of a BCCP.' ;
  COMMENT ON COLUMN BBIEP.BBIEP_ID
IS
  'A internal, primary database key of an BBIEP.' ;
  COMMENT ON COLUMN BBIEP.GUID
IS
  'A globally unique identifier (GUID) of an BBIEP. GUID of an BBIEP is different from its based BCCP. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.' ;
  COMMENT ON COLUMN BBIEP.BASED_BCCP_ID
IS
  'A foreign key pointing to the BCCP record. It is the BCCP, which the BBIEP contextualizes.' ;
  COMMENT ON COLUMN BBIEP.DEFINITION
IS
  'Definition to override the BCCP''s Definition. If NULL, it means that the definition should be inherited from the based CC.' ;
  COMMENT ON COLUMN BBIEP.REMARK
IS
  'This column allows the user to specify very context-specific usage of the BIE. It is different from the Definition column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be "Type of BOM should be recognized in the BOM/typeCode.' ;
  COMMENT ON COLUMN BBIEP.BIZ_TERM
IS
  'Business term to indicate what the BIE is called in a particular business context such as in an industry.' ;
  COMMENT ON COLUMN BBIEP.CREATED_BY
IS
  'A foreign key referring to the user who creates the BBIEP. The creator of the BBIEP is also its owner by default. BBIEPs created as children of another ABIE have the same CREATED_BY'',' ;
  COMMENT ON COLUMN BBIEP.LAST_UPDATED_BY
IS
  'A foreign key referring to the last user who has updated the BBIEP record. ' ;
  COMMENT ON COLUMN BBIEP.CREATION_TIMESTAMP
IS
  'Timestamp when the BBIEP record was first created. BBIEPs created as children of another ABIE have the same CREATION_TIMESTAMP,' ;
  COMMENT ON COLUMN BBIEP.LAST_UPDATE_TIMESTAMP
IS
  'The timestamp when the BBIEP was last updated.' ;
  COMMENT ON COLUMN BBIEP.OWNER_TOP_LEVEL_ABIE_ID
IS
  'This is a foriegn key to the ABIE table. It specifies the top-level ABIE which owns this BBIEP record.' ;
  CREATE INDEX BBIEP_OWNER_TLVL_ABIE_ID_IDX ON BBIEP
    (
      OWNER_TOP_LEVEL_ABIE_ID ASC
    )
    ;
CREATE UNIQUE INDEX BBIEP_BBIEP_ID_IDX ON BBIEP
  (
    BBIEP_ID ASC
  )
  ;
  CREATE INDEX BBIEP_BASED_BCCP_ID_IDX ON BBIEP
    ( BASED_BCCP_ID ASC
    ) ;
  CREATE INDEX BBIEP_CREATED_BY_IDX ON BBIEP
    ( CREATED_BY ASC
    ) ;
  CREATE INDEX BBIEP_LAST_UPDATED_BY_IDX ON BBIEP
    ( LAST_UPDATED_BY ASC
    ) ;
ALTER TABLE BBIEP ADD CONSTRAINT BBIEP_PK PRIMARY KEY ( BBIEP_ID ) ;


--  Because there is no single table that is a contextualized counterpart of
--  the DT table (which stores both CDT and BDT), The context specific
--  constraints associated with the DT are stored in the BBIE table, while this
--  table stores the constraints associated with the DT's SCs.
CREATE TABLE BBIE_SC
  (
    BBIE_SC_ID          DECIMAL (19) NOT NULL DEFAULT NEXTVAL('BBIE_SC_ID_SEQ'),
    GUID                VARCHAR (41) NOT NULL ,
    BBIE_ID             DECIMAL (19) NOT NULL ,
    DT_SC_ID            DECIMAL (19) NOT NULL ,
    DT_SC_PRI_RESTRI_ID DECIMAL (19) ,
    CODE_LIST_ID        DECIMAL (19) ,
    AGENCY_ID_LIST_ID   DECIMAL (19) ,
    CARDINALITY_MIN     DECIMAL (10) NOT NULL ,
    CARDINALITY_MAX     DECIMAL (10) NOT NULL ,
    DEFAULT_VALUE TEXT ,
    FIXED_VALUE TEXT ,
    DEFINITION TEXT ,
    REMARK                  VARCHAR (225) ,
    BIZ_TERM                VARCHAR (225) ,
    IS_USED                 BOOLEAN NOT NULL ,
    OWNER_TOP_LEVEL_ABIE_ID DECIMAL (19) NOT NULL
  )
  ;
COMMENT ON TABLE BBIE_SC
IS
  'Because there is no single table that is a contextualized counterpart of the DT table (which stores both CDT and BDT), The context specific constraints associated with the DT are stored in the BBIE table, while this table stores the constraints associated with the DT''s SCs. ' ;
  COMMENT ON COLUMN BBIE_SC.BBIE_SC_ID
IS
  'A internal, primary database key of a BBIE_SC.' ;
  COMMENT ON COLUMN BBIE_SC.GUID
IS
  'A globally unique identifier (GUID). It is different from the GUID fo the SC on the CC side. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.' ;
  COMMENT ON COLUMN BBIE_SC.BBIE_ID
IS
  'The BBIE this BBIE_SC applies to.' ;
  COMMENT ON COLUMN BBIE_SC.DT_SC_ID
IS
  'Foreign key to the DT_SC table. This should correspond to the DT_SC of the BDT of the based BCC and BCCP.' ;
  COMMENT ON COLUMN BBIE_SC.DT_SC_PRI_RESTRI_ID
IS
  'This must be one of the allowed primitive/code list as specified in the corresponding SC of the based BCC of the BBIE (referred to by the BBIE_ID column).

It is the foreign key to the BDT_SC_PRI_RESTRI table. It indicates the primitive assigned to the BBIE (or also can be viewed as assigned to the BBIEP for this specific association). This is assigned by the user who authors the BIE. The assignment would override the default from the CC side.

This column, the CODE_LIST_ID column, and AGENCY_ID_LIST_ID column cannot have a value at the same time.' ;
  COMMENT ON COLUMN BBIE_SC.CODE_LIST_ID
IS
  'This is a foreign key to the CODE_LIST table. If a code list is assigned to the BBIE SC (or also can be viewed as assigned to the BBIEP SC for this association), then this column stores the assigned code list. It should be noted that one of the possible primitives assignable to the DT_SC_PRI_RESTRI_ID column may also be a code list. So this column is typically used when the user wants to assign another code list different from the one permissible by the CC model.

This column is, the DT_SC_PRI_RESTRI_ID column, and AGENCY_ID_LIST_ID column cannot have a value at the same time.' ;
  COMMENT ON COLUMN BBIE_SC.AGENCY_ID_LIST_ID
IS
  'This is a foreign key to the AGENCY_ID_LIST table. If a agency ID list is assigned to the BBIE SC (or also can be viewed as assigned to the BBIEP SC for this association), then this column stores the assigned Agency ID list. It should be noted that one of the possible primitives assignable to the DT_SC_PRI_RESTRI_ID column may also be an Agency ID list. So this column is typically used only when the user wants to assign another Agency ID list different from the one permissible by the CC model.

This column, the DT_SC_PRI_RESTRI_ID column, and CODE_LIST_ID column cannot have a value at the same time.' ;
  COMMENT ON COLUMN BBIE_SC.CARDINALITY_MIN
IS
  'The minimum occurrence constraint for the BBIE SC. A valid value is 0 or 1.' ;
  COMMENT ON COLUMN BBIE_SC.CARDINALITY_MAX
IS
  'Maximum occurence constraint of the BBIE SC. A valid value is 0 or 1.' ;
  COMMENT ON COLUMN BBIE_SC.DEFAULT_VALUE
IS
  'This column specifies the default value constraint. Default and fixed value constraints cannot be used at the same time.' ;
  COMMENT ON COLUMN BBIE_SC.FIXED_VALUE
IS
  'This column captures the fixed value constraint. Default and fixed value constraints cannot be used at the same time.' ;
  COMMENT ON COLUMN BBIE_SC.DEFINITION
IS
  'Description to override the BDT SC definition. If NULL, it means that the definition should be inherited from the based BDT SC.' ;
  COMMENT ON COLUMN BBIE_SC.REMARK
IS
  'This column allows the user to specify a very context-specific usage of the BBIE SC. It is different from the Definition column in that the Definition column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. ' ;
  COMMENT ON COLUMN BBIE_SC.BIZ_TERM
IS
  'Business term to indicate what the BBIE SC is called in a particular business context. With this current design, only one business term is allowed per business context.' ;
  COMMENT ON COLUMN BBIE_SC.IS_USED
IS
  'Flag to indicate whether the field/component is used in the content model. It indicates whether the field/component should be generated.' ;
  COMMENT ON COLUMN BBIE_SC.OWNER_TOP_LEVEL_ABIE_ID
IS
  'This is a foriegn key to the ABIE. It specifies the top-level ABIE, which owns this BBIE_SC record.' ;
  CREATE INDEX BBIE_SC_DT_SC_PRI_RESTR_ID_IDX ON BBIE_SC
    (
      DT_SC_PRI_RESTRI_ID ASC
    )
    ;
  CREATE INDEX BBIE_SC_OWNER_TLVL_ABIE_ID_IDX ON BBIE_SC
    (
      OWNER_TOP_LEVEL_ABIE_ID ASC
    )
    ;
CREATE UNIQUE INDEX BBIE_SC_BBIE_SC_ID_IDX ON BBIE_SC
  (
    BBIE_SC_ID ASC
  )
  ;
  CREATE INDEX BBIE_SC_BBIE_ID_IDX ON BBIE_SC
    ( BBIE_ID ASC
    ) ;
  CREATE INDEX BBIE_SC_DT_SC_ID_IDX ON BBIE_SC
    ( DT_SC_ID ASC
    ) ;
  CREATE INDEX BBIE_SC_CODE_LIST_ID_IDX ON BBIE_SC
    ( CODE_LIST_ID ASC
    ) ;
  CREATE INDEX BBIE_SC_AGENCY_ID_LIST_ID_IDX ON BBIE_SC
    (
      AGENCY_ID_LIST_ID ASC
    ) ;
ALTER TABLE BBIE_SC ADD CONSTRAINT BBIE_SC_PK PRIMARY KEY ( BBIE_SC_ID ) ;


--  A BCC represents a relationship/association between an ACC and a BCCP. It
--  creates a data element for an ACC.
CREATE TABLE BCC
  (
    BCC_ID          DECIMAL (19) NOT NULL DEFAULT NEXTVAL('BCC_ID_SEQ'),
    GUID            VARCHAR (41) NOT NULL ,
    CARDINALITY_MIN DECIMAL (10) NOT NULL ,
    CARDINALITY_MAX DECIMAL (10) ,
    TO_BCCP_ID      DECIMAL (19) NOT NULL ,
    FROM_ACC_ID     DECIMAL (19) NOT NULL ,
    SEQ_KEY         DECIMAL (10) ,
    ENTITY_TYPE     DECIMAL (10) ,
    DEN             VARCHAR (200) NOT NULL ,
    DEFINITION TEXT ,
    DEFINITION_SOURCE     VARCHAR (100) ,
    CREATED_BY            DECIMAL (19) NOT NULL ,
    OWNER_USER_ID         DECIMAL (19) NOT NULL ,
    LAST_UPDATED_BY       DECIMAL (19) NOT NULL ,
    CREATION_TIMESTAMP    TIMESTAMP WITH TIME ZONE NOT NULL ,
    LAST_UPDATE_TIMESTAMP TIMESTAMP WITH TIME ZONE NOT NULL ,
    STATE                 DECIMAL (10) NOT NULL ,
    REVISION_NUM          DECIMAL (10) NOT NULL ,
    REVISION_TRACKING_NUM DECIMAL (10) NOT NULL ,
    REVISION_ACTION       DECIMAL (3) ,
    RELEASE_ID            DECIMAL (19) ,
    CURRENT_BCC_ID        DECIMAL (19) ,
    IS_DEPRECATED         BOOLEAN NOT NULL ,
    IS_NILLABLE           BOOLEAN NOT NULL ,
    DEFAULT_VALUE TEXT
  )
  ;
COMMENT ON TABLE BCC
IS
  'A BCC represents a relationship/association between an ACC and a BCCP. It creates a data element for an ACC. ' ;
  COMMENT ON COLUMN BCC.BCC_ID
IS
  'A internal, primary database key of an BCC.' ;
  COMMENT ON COLUMN BCC.GUID
IS
  'A globally unique identifier (GUID) of BCC. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.'',' ;
  COMMENT ON COLUMN BCC.CARDINALITY_MIN
IS
  'Minimum cardinality of the TO_BCCP_ID. The valid values are non-negative integer.' ;
  COMMENT ON COLUMN BCC.CARDINALITY_MAX
IS
  'Maximum cardinality of the TO_BCCP_ID. The valid values are integer -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.'',' ;
  COMMENT ON COLUMN BCC.TO_BCCP_ID
IS
  'TO_BCCP_ID is a foreign key to an BCCP table record. It is basically pointing to a child data element of the FROM_ACC_ID.

Note that for the BCC history records, this column always points to the BCCP_ID of the current record of a BCCP.'',' ;
  COMMENT ON COLUMN BCC.FROM_ACC_ID
IS
  'FROM_ACC_ID is a foreign key pointing to an ACC record. It is basically pointing to a parent data element (type) of the TO_BCCP_ID.

Note that for the BCC history records, this column always points to the ACC_ID of the current record of an ACC.' ;
  COMMENT ON COLUMN BCC.SEQ_KEY
IS
  'This indicates the order of the associations among other siblings. A valid value is positive integer. The SEQ_KEY at the CC side is localized. In other words, if an ACC is based on another ACC, SEQ_KEY of ASCCs or BCCs of the former ACC starts at 1 again. ' ;
  COMMENT ON COLUMN BCC.ENTITY_TYPE
IS
  'This is a code list: 0 = ATTRIBUTE and 1 = ELEMENT. An expression generator may or may not use this information. This column is necessary because some of the BCCs are xsd:attribute and some are xsd:element in the OAGIS 10.x. ' ;
  COMMENT ON COLUMN BCC.DEN
IS
  'DEN (dictionary entry name) of the BCC. This column can be derived from QUALIFIER and OBJECT_CLASS_TERM of the FROM_ACC_ID and DEN of the TO_BCCP_ID as QUALIFIER + "_ " + OBJECT_CLASS_TERM + ". " + DEN. ' ;
  COMMENT ON COLUMN BCC.DEFINITION
IS
  'This is a documentation or description of the BCC. Since BCC is business context independent, this is a business context independent description of the BCC. Since there are definitions also in the BCCP (as referenced by TO_BCCP_ID column) and the BDT under that BCCP, the definition in the BCC is a specific description about the relationship between the ACC (as in FROM_ACC_ID) and the BCCP.' ;
  COMMENT ON COLUMN BCC.DEFINITION_SOURCE
IS
  'This is typically a URL identifying the source of the DEFINITION column.' ;
  COMMENT ON COLUMN BCC.CREATED_BY
IS
  'Foreign key to the APP_USER table referring to the user who creates the entity.

This column never change between the history and the current record. The history record should have the same value as that of its current record.' ;
  COMMENT ON COLUMN BCC.OWNER_USER_ID
IS
  'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.

The ownership can change throughout the history, but undoing shouldn''t rollback the ownership.' ;
  COMMENT ON COLUMN BCC.LAST_UPDATED_BY
IS
  'Foreign key to the APP_USER table referring to the last user who has updated the record.

In the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).' ;
  COMMENT ON COLUMN BCC.CREATION_TIMESTAMP
IS
  'Timestamp when the revision of the BCC was created.

This never change for a revision.' ;
  COMMENT ON COLUMN BCC.LAST_UPDATE_TIMESTAMP
IS
  'The timestamp when the record was last updated.

The value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the change has occurred.' ;
  COMMENT ON COLUMN BCC.STATE
IS
  '1 = EDITING, 2 = CANDIDATE, 3 = PUBLISHED. This is the revision life cycle state of the entity.

State change can''t be undone. But the history record can still keep the records of when the state was changed.' ;
  COMMENT ON COLUMN BCC.REVISION_NUM
IS
  'REVISION_NUM is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).' ;
  COMMENT ON COLUMN BCC.REVISION_TRACKING_NUM
IS
  'REVISION_TRACKING_NUM supports the ability to undo changes during a revision (life cycle of a revision is from the component''s EDITING state to PUBLISHED state). Once the component has transitioned into the PUBLISHED state for its particular revision, all revision tracking records are deleted except the latest one. REVISION_TRACKING_NUM can be 0, 1, 2, and so on. The zero value is assign to the record with REVISION_NUM = 0 as a default.' ;
  COMMENT ON COLUMN BCC.REVISION_ACTION
IS
  'This indicates the action associated with the record. The action can be 1 = INSERT, 2 = UPDATE, and 3 = DELETE. This column is null for the current record.' ;
  COMMENT ON COLUMN BCC.RELEASE_ID
IS
  'RELEASE_ID is an incremental integer. It is an unformatted counterpart of the RELEASE_DECIMAL in the RELEASE table. RELEASE_ID can be 1, 2, 3, and so on. RELEASE_ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the REVISION_ACTION column).

Not all component revisions have an associated RELEASE_ID because some revisions may never be released.

Unpublished components cannot be released.' ;
  COMMENT ON COLUMN BCC.CURRENT_BCC_ID
IS
  'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the record whose REVISION_NUM is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.

It is noted that although this is a foreign key by definition, we don''t specify a foreign key in the data model. This is because when an entity is deleted the current record won''t exist anymore.

The value of this column for the current record should be left NULL.' ;
  COMMENT ON COLUMN BCC.IS_DEPRECATED
IS
  'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).' ;
  COMMENT ON COLUMN BCC.IS_NILLABLE
IS
  'Indicate whether the field can have a NULL This is corresponding to the nillable flag in the XML schema.' ;
  COMMENT ON COLUMN BCC.DEFAULT_VALUE
IS
  'This set the default value at the association level. ' ;
CREATE UNIQUE INDEX BCC_BCC_ID_IDX ON BCC
  (
    BCC_ID ASC
  )
  ;
  CREATE INDEX BCC_TO_BCCP_ID_IDX ON BCC
    ( TO_BCCP_ID ASC
    ) ;
  CREATE INDEX BCC_FROM_ACC_ID_IDX ON BCC
    ( FROM_ACC_ID ASC
    ) ;
  CREATE INDEX BCC_CREATED_BY_IDX ON BCC
    ( CREATED_BY ASC
    ) ;
  CREATE INDEX BCC_OWNER_USER_ID_IDX ON BCC
    ( OWNER_USER_ID ASC
    ) ;
  CREATE INDEX BCC_LAST_UPDATED_BY_IDX ON BCC
    ( LAST_UPDATED_BY ASC
    ) ;
  CREATE INDEX BCC_RELEASE_ID_IDX ON BCC
    ( RELEASE_ID ASC
    ) ;
  CREATE INDEX BCC_CURRENT_BCC_ID_IDX ON BCC
    ( CURRENT_BCC_ID ASC
    ) ;
ALTER TABLE BCC ADD CONSTRAINT BCC_PK PRIMARY KEY ( BCC_ID ) ;


--  An BCCP specifies a property concept and data type associated with it. A
--  BCCP can be then added as a property of an ACC.
CREATE TABLE BCCP
  (
    BCCP_ID             DECIMAL (19) NOT NULL DEFAULT NEXTVAL('BCCP_ID_SEQ'),
    GUID                VARCHAR (41) NOT NULL ,
    PROPERTY_TERM       VARCHAR (60) NOT NULL ,
    REPRESENTATION_TERM VARCHAR (20) NOT NULL ,
    BDT_ID              DECIMAL (19) NOT NULL ,
    DEN                 VARCHAR (200) NOT NULL ,
    DEFINITION TEXT ,
    DEFINITION_SOURCE     VARCHAR (100) ,
    MODULE_ID             DECIMAL (19) ,
    NAMESPACE_ID          DECIMAL (19) ,
    IS_DEPRECATED         BOOLEAN NOT NULL ,
    CREATED_BY            DECIMAL (19) NOT NULL ,
    OWNER_USER_ID         DECIMAL (19) NOT NULL ,
    LAST_UPDATED_BY       DECIMAL (19) NOT NULL ,
    CREATION_TIMESTAMP    TIMESTAMP WITH TIME ZONE NOT NULL ,
    LAST_UPDATE_TIMESTAMP TIMESTAMP WITH TIME ZONE NOT NULL ,
    STATE                 DECIMAL (10) NOT NULL ,
    REVISION_NUM          DECIMAL (10) NOT NULL ,
    REVISION_TRACKING_NUM DECIMAL (10) NOT NULL ,
    REVISION_ACTION       DECIMAL (10) ,
    RELEASE_ID            DECIMAL (19) ,
    CURRENT_BCCP_ID       DECIMAL (19) ,
    IS_NILLABLE           BOOLEAN ,
    DEFAULT_VALUE TEXT
  )
  ;
COMMENT ON TABLE BCCP
IS
  'An BCCP specifies a property concept and data type associated with it. A BCCP can be then added as a property of an ACC.' ;
  COMMENT ON COLUMN BCCP.BCCP_ID
IS
  'An internal, primary database key.' ;
  COMMENT ON COLUMN BCCP.GUID
IS
  'A globally unique identifier (GUID). Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.'',' ;
  COMMENT ON COLUMN BCCP.PROPERTY_TERM
IS
  'The property concept that the BCCP models. ' ;
  COMMENT ON COLUMN BCCP.REPRESENTATION_TERM
IS
  'The representation term convey the format of the data the BCCP can take. The value is derived from the DT.DATA_TYPE_TERM of the associated BDT as referred to by the BDT_ID column.' ;
  COMMENT ON COLUMN BCCP.BDT_ID
IS
  'Foreign key pointing to the DT table indicating the data typye or data format of the BCCP. Only DT_ID which DT.TYPE is BDT can be used.' ;
  COMMENT ON COLUMN BCCP.DEN
IS
  'The dictionary entry name of the BCCP. It is derived by PROPERTY_TERM + ". " + REPRESENTATION_TERM.' ;
  COMMENT ON COLUMN BCCP.DEFINITION
IS
  'Description of the BCCP.' ;
  COMMENT ON COLUMN BCCP.DEFINITION_SOURCE
IS
  'This is typically a URL identifying the source of the DEFINITION column.' ;
  COMMENT ON COLUMN BCCP.MODULE_ID
IS
  'Foreign key to the module table indicating physical schema module the BCCP belongs to.' ;
  COMMENT ON COLUMN BCCP.NAMESPACE_ID
IS
  'Foreign key to the NAMESPACE table. This is the namespace to which the entity belongs. This namespace column is primarily used in the case the component is a user''s component because there is also a namespace assigned at the release level.' ;
  COMMENT ON COLUMN BCCP.IS_DEPRECATED
IS
  'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).' ;
  COMMENT ON COLUMN BCCP.CREATED_BY
IS
  'Foreign key to the APP_USER table referring to the user who creates the entity.

This column never change between the history and the current record for a given revision. The history record should have the same value as that of its current record.' ;
  COMMENT ON COLUMN BCCP.OWNER_USER_ID
IS
  'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.

The ownership can change throughout the history, but undoing shouldn''t rollback the ownership.' ;
  COMMENT ON COLUMN BCCP.LAST_UPDATED_BY
IS
  'Foreign key to the APP_USER table referring to the last user who has updated the record.

In the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).' ;
  COMMENT ON COLUMN BCCP.CREATION_TIMESTAMP
IS
  'Timestamp when the revision of the BCCP was created.

This never change for a revision.' ;
  COMMENT ON COLUMN BCCP.LAST_UPDATE_TIMESTAMP
IS
  'The timestamp when the record was last updated.

The value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.' ;
  COMMENT ON COLUMN BCCP.STATE
IS
  '1 = EDITING, 2 = CANDIDATE, 3 = PUBLISHED. This the revision life cycle state of the ACC.

State change can''t be undone. But the history record can still keep the records of when the state was changed.' ;
  COMMENT ON COLUMN BCCP.REVISION_NUM
IS
  'REVISION_NUM is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).' ;
  COMMENT ON COLUMN BCCP.REVISION_TRACKING_NUM
IS
  'REVISION_TRACKING_NUM supports the ability to undo changes during a revision (life cycle of a revision is from the component''s EDITING state to PUBLISHED state). Once the component has transitioned into the PUBLISHED state for its particular revision, all revision tracking records are deleted except the latest one. REVISION_TRACKING_NUMB can be 0, 1, 2, and so on. The zero value is assigned to the record with REVISION_NUM = 0 as a default.' ;
  COMMENT ON COLUMN BCCP.REVISION_ACTION
IS
  'This indicates the action associated with the record. The action can be 1 = INSERT, 2 = UPDATE, and 3 = DELETE. This column is null for the current record.' ;
  COMMENT ON COLUMN BCCP.RELEASE_ID
IS
  'RELEASE_ID is an incremental integer. It is an unformatted counter part of the RELEASE_DECIMAL in the RELEASE table. RELEASE_ID can be 1, 2, 3, and so on. A release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the REVISION_ACTION column).

Not all component revisions have an associated RELEASE_ID because some revisions may never be released. USER_EXTENSION_GROUP component type is never part of a release.

Unpublished components cannot be released.' ;
  COMMENT ON COLUMN BCCP.CURRENT_BCCP_ID
IS
  'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the the record whose REVISION_NUM is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.

It is noted that although this is a foreign key by definition, we don''t specify a foreign key in the data model. This is because when an entity is deleted the current record won''t exist anymore.

The value of this column for the current record should be left NULL.' ;
  COMMENT ON COLUMN BCCP.IS_NILLABLE
IS
  'This is corresponding to the XML Schema nillable flag. Although the nillable may not apply to certain cases of the BCCP (e.g., when it is only used as XSD attribute), the value is default to false for simplification. ' ;
  COMMENT ON COLUMN BCCP.DEFAULT_VALUE
IS
  'This column specifies the default value constraint. Default and fixed value constraints cannot be used at the same time.' ;
CREATE UNIQUE INDEX BCCP_BCCP_ID_IDX ON BCCP
  (
    BCCP_ID ASC
  )
  ;
  CREATE INDEX BCCP_BDT_ID_IDX ON BCCP
    ( BDT_ID ASC
    ) ;
  CREATE INDEX BCCP_MODULE_ID_IDX ON BCCP
    ( MODULE_ID ASC
    ) ;
  CREATE INDEX BCCP_NAMESPACE_ID_IDX ON BCCP
    ( NAMESPACE_ID ASC
    ) ;
  CREATE INDEX BCCP_CREATED_BY_IDX ON BCCP
    ( CREATED_BY ASC
    ) ;
  CREATE INDEX BCCP_OWNER_USER_ID_IDX ON BCCP
    ( OWNER_USER_ID ASC
    ) ;
  CREATE INDEX BCCP_LAST_UPDATED_BY_IDX ON BCCP
    ( LAST_UPDATED_BY ASC
    ) ;
  CREATE INDEX BCCP_RELEASE_ID_IDX ON BCCP
    ( RELEASE_ID ASC
    ) ;
  CREATE INDEX BCCP_CURRENT_BCCP_ID_IDX ON BCCP
    ( CURRENT_BCCP_ID ASC
    ) ;
ALTER TABLE BCCP ADD CONSTRAINT BCCP_PK PRIMARY KEY ( BCCP_ID ) ;


--  This table captures the allowed primitives for a BDT. The allowed
--  primitives are captured by three columns the CDT_AWD_PRI_XPS_TYPE_MAP_ID,
--  CODE_LIST_ID, and AGENCY_ID_LIST_ID. The first column specifies the
--  primitive by the built-in type of an expression language such as the XML
--  Schema built-in type. The second specifies the primitive, which is a code
--  list, while the last one specifies the primitive which is an agency
--  identification list. Only one column among the three can have a value in a
--  particular record.
CREATE TABLE BDT_PRI_RESTRI
  (
    BDT_PRI_RESTRI_ID           DECIMAL (19) NOT NULL DEFAULT NEXTVAL('BDT_PRI_RESTRI_ID_SEQ'),
    BDT_ID                      DECIMAL (19) NOT NULL ,
    CDT_AWD_PRI_XPS_TYPE_MAP_ID DECIMAL (19) ,
    CODE_LIST_ID                DECIMAL (19) ,
    AGENCY_ID_LIST_ID           DECIMAL (19) ,
    IS_DEFAULT                  BOOLEAN NOT NULL
  )
  ;
COMMENT ON TABLE BDT_PRI_RESTRI
IS
  'This table captures the allowed primitives for a BDT. The allowed primitives are captured by three columns the CDT_AWD_PRI_XPS_TYPE_MAP_ID, CODE_LIST_ID, and AGENCY_ID_LIST_ID. The first column specifies the primitive by the built-in type of an expression language such as the XML Schema built-in type. The second specifies the primitive, which is a code list, while the last one specifies the primitive which is an agency identification list. Only one column among the three can have a value in a particular record.' ;
  COMMENT ON COLUMN BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID
IS
  'Primary, internal database key.' ;
  COMMENT ON COLUMN BDT_PRI_RESTRI.BDT_ID
IS
  'Foreign key to the DT table. It shall point to only DT that is a BDT (not a CDT).' ;
  COMMENT ON COLUMN BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID
IS
  'This is a foreign key to the CDT_AWD_PRI_XPS_TYPE_MAP table.  It allows for a primitive restriction based on a built-in type of schema expressions.' ;
  COMMENT ON COLUMN BDT_PRI_RESTRI.CODE_LIST_ID
IS
  'Foreign key to the CODE_LIST table.' ;
  COMMENT ON COLUMN BDT_PRI_RESTRI.AGENCY_ID_LIST_ID
IS
  'This is a foreign key to the AGENCY_ID_LIST table. It is used in the case that the BDT content can be restricted to an agency identification.' ;
  COMMENT ON COLUMN BDT_PRI_RESTRI.IS_DEFAULT
IS
  'This allows overriding the default primitive assigned in the CDT_AWD_PRI_XPS_TYPE_MAP table. It typically indicates the most generic primtive for the data type.' ;
CREATE UNIQUE INDEX BDT_PR_PK_IDX ON BDT_PRI_RESTRI
  (
    BDT_PRI_RESTRI_ID ASC
  )
  ;
  CREATE INDEX BDT_PR_CAPXTM_ID_IDX ON BDT_PRI_RESTRI
    (
      CDT_AWD_PRI_XPS_TYPE_MAP_ID ASC
    )
    ;
  CREATE INDEX BDT_PR_CODE_LIST_ID_IDX ON BDT_PRI_RESTRI
    (
      CODE_LIST_ID ASC
    )
    ;
  CREATE INDEX BDT_PR_AGENCY_ID_LIST_ID_IDX ON BDT_PRI_RESTRI
    (
      AGENCY_ID_LIST_ID ASC
    )
    ;
  CREATE INDEX BDT_PRI_RESTRI_BDT_ID_IDX ON BDT_PRI_RESTRI
    ( BDT_ID ASC
    ) ;
ALTER TABLE BDT_PRI_RESTRI ADD CONSTRAINT BPR_PK PRIMARY KEY ( BDT_PRI_RESTRI_ID ) ;


--  This table is similar to the BDT_PRI_RESTRI table but it is for the BDT SC.
--  The allowed primitives are captured by three columns the
--  CDT_SC_AWD_PRI_XPS_TYPE_MAP, CODE_LIST_ID, and AGENCY_ID_LIST_ID. The first
--  column specifies the primitive by the built-in type of an expression
--  language such as the XML Schema built-in type. The second specifies the
--  primitive, which is a code list, while the last one specifies the primitive
--  which is an agency identification list. Only one column among the three can
--  have a value in a particular record.
--  It should be noted that the table does not store the fact about primitive
--  restriction hierarchical relationships. In other words, if a BDT SC is
--  derived from another BDT SC and the derivative BDT SC applies some
--  primitive restrictions, that relationship will not be explicitly stored.
--  The derivative BDT SC points directly to the CDT_AWD_PRI_XPS_TYPE_MAP key
--  rather than the BDT_SC_PRI_RESTRI key.
CREATE TABLE BDT_SC_PRI_RESTRI
  (
    BDT_SC_PRI_RESTRI_ID           DECIMAL (19) NOT NULL DEFAULT NEXTVAL('BDT_SC_PRI_RESTRI_ID_SEQ'),
    BDT_SC_ID                      DECIMAL (19) NOT NULL ,
    CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID DECIMAL (19) ,
    CODE_LIST_ID                   DECIMAL (19) ,
    AGENCY_ID_LIST_ID              DECIMAL (19) ,
    IS_DEFAULT                     BOOLEAN NOT NULL
  )
  ;
COMMENT ON TABLE BDT_SC_PRI_RESTRI
IS
  'This table is similar to the BDT_PRI_RESTRI table but it is for the BDT SC. The allowed primitives are captured by three columns the CDT_SC_AWD_PRI_XPS_TYPE_MAP, CODE_LIST_ID, and AGENCY_ID_LIST_ID. The first column specifies the primitive by the built-in type of an expression language such as the XML Schema built-in type. The second specifies the primitive, which is a code list, while the last one specifies the primitive which is an agency identification list. Only one column among the three can have a value in a particular record.

It should be noted that the table does not store the fact about primitive restriction hierarchical relationships. In other words, if a BDT SC is derived from another BDT SC and the derivative BDT SC applies some primitive restrictions, that relationship will not be explicitly stored. The derivative BDT SC points directly to the CDT_AWD_PRI_XPS_TYPE_MAP key rather than the BDT_SC_PRI_RESTRI key.' ;
  COMMENT ON COLUMN BDT_SC_PRI_RESTRI.BDT_SC_PRI_RESTRI_ID
IS
  'Primary, internal database key.' ;
  COMMENT ON COLUMN BDT_SC_PRI_RESTRI.BDT_SC_ID
IS
  'Foreign key to the DT_SC table. This column should only refers to a DT_SC that belongs to a BDT (not CDT).' ;
  COMMENT ON COLUMN BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID
IS
  'This column is a forieng key to the CDT_SC_AWD_PRI_XPS_TYPE_MAP table. It allows for a primitive restriction based on a built-in type of schema expressions.' ;
  COMMENT ON COLUMN BDT_SC_PRI_RESTRI.CODE_LIST_ID
IS
  'Foreign key to identify a code list. It allows for a primitive restriction based on a code list.' ;
  COMMENT ON COLUMN BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_ID
IS
  'Foreign key to identify an agency identification list. It allows for a primitive restriction based on such list of values.' ;
  COMMENT ON COLUMN BDT_SC_PRI_RESTRI.IS_DEFAULT
IS
  'This column specifies the default primitive for a BDT. It is typically the most generic primitive allowed for the BDT.' ;
CREATE UNIQUE INDEX BDT_SPR_PK_IDX ON BDT_SC_PRI_RESTRI
  (
    BDT_SC_PRI_RESTRI_ID ASC
  )
  ;
  CREATE INDEX BDT_SPR_BDT_SC_ID_IDX ON BDT_SC_PRI_RESTRI
    (
      BDT_SC_ID ASC
    )
    ;
  CREATE INDEX BDT_SPR_CSAPXTM_ID_IDX ON BDT_SC_PRI_RESTRI
    (
      CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID ASC
    )
    ;
  CREATE INDEX BDT_SPR_CODE_LIST_ID_IDX ON BDT_SC_PRI_RESTRI
    (
      CODE_LIST_ID ASC
    )
    ;
  CREATE INDEX BDT_SPR_AGENCY_ID_LIST_ID_IDX ON BDT_SC_PRI_RESTRI
    (
      AGENCY_ID_LIST_ID ASC
    )
    ;
ALTER TABLE BDT_SC_PRI_RESTRI ADD CONSTRAINT BSPR_PK PRIMARY KEY ( BDT_SC_PRI_RESTRI_ID ) ;


CREATE TABLE BIE_USAGE_RULE
  (
    BIE_USAGE_RULE_ID      DECIMAL (19) NOT NULL DEFAULT NEXTVAL('BIE_USAGE_RULE_ID_SEQ'),
    ASSIGNED_USAGE_RULE_ID DECIMAL (19) NOT NULL ,
    TARGET_ABIE_ID         DECIMAL (19) ,
    TARGET_ASBIE_ID        DECIMAL (19) ,
    TARGET_ASBIEP_ID       DECIMAL (19) ,
    TARGET_BBIE_ID         DECIMAL (19) ,
    TARGET_BBIEP_ID        DECIMAL (19)
  )
  ;
COMMENT ON TABLE BIE_USAGE_RULE
IS
  'This is an intersection table. Per CCTS, a usage rule may be reused. This table allows m-m relationships between the usage rule and all kinds of BIEs. In a particular record, either only one of the TARGET_ABIE_ID, TARGET_ASBIE_ID, TARGET_ASBIEP_ID, TARGET_BBIE_ID, or TARGET_BBIEP_ID.' ;
  COMMENT ON COLUMN BIE_USAGE_RULE.BIE_USAGE_RULE_ID
IS
  'Primary key of the table.' ;
  COMMENT ON COLUMN BIE_USAGE_RULE.ASSIGNED_USAGE_RULE_ID
IS
  'Foreign key to the USAGE_RULE table indicating the usage rule assigned to a BIE.' ;
  COMMENT ON COLUMN BIE_USAGE_RULE.TARGET_ABIE_ID
IS
  'Foreign key to the ABIE table indicating the ABIE, to which the usage rule is applied.' ;
  COMMENT ON COLUMN BIE_USAGE_RULE.TARGET_ASBIE_ID
IS
  'Foreign key to the ASBIE table indicating the ASBIE, to which the usage rule is applied.' ;
  COMMENT ON COLUMN BIE_USAGE_RULE.TARGET_ASBIEP_ID
IS
  'Foreign key to the ASBIEP table indicating the ASBIEP, to which the usage rule is applied.' ;
  COMMENT ON COLUMN BIE_USAGE_RULE.TARGET_BBIE_ID
IS
  'Foreign key to the BBIE table indicating the BBIE, to which the usage rule is applied.' ;
  COMMENT ON COLUMN BIE_USAGE_RULE.TARGET_BBIEP_ID
IS
  'Foreign key to the BBIEP table indicating the ABIEP, to which the usage rule is applied.' ;
CREATE UNIQUE INDEX BUR_BIE_USAGE_RULE_ID_IDX ON BIE_USAGE_RULE
  (
    BIE_USAGE_RULE_ID ASC
  )
  ;
  CREATE INDEX BUR_ASSIGNED_USAGE_RULE_ID_IDX ON BIE_USAGE_RULE
    (
      ASSIGNED_USAGE_RULE_ID ASC
    )
    ;
  CREATE INDEX BUR_TARGET_ABIE_ID_IDX ON BIE_USAGE_RULE
    (
      TARGET_ABIE_ID ASC
    )
    ;
  CREATE INDEX BUR_TARGET_ASBIE_ID_IDX ON BIE_USAGE_RULE
    (
      TARGET_ASBIE_ID ASC
    )
    ;
  CREATE INDEX BUR_TARGET_ASBIEP_ID_IDX ON BIE_USAGE_RULE
    (
      TARGET_ASBIEP_ID ASC
    )
    ;
  CREATE INDEX BUR_TARGET_BBIE_ID_IDX ON BIE_USAGE_RULE
    (
      TARGET_BBIE_ID ASC
    )
    ;
  CREATE INDEX BUR_TARGET_BBIEP_ID_IDX ON BIE_USAGE_RULE
    (
      TARGET_BBIEP_ID ASC
    )
    ;
ALTER TABLE BIE_USAGE_RULE ADD CONSTRAINT BIE_USAGE_RULE_PK PRIMARY KEY ( BIE_USAGE_RULE_ID ) ;


--  This table is a log of events. It keeps track of the User Extension ACC
--  (the specific revision) used by an Extension ABIE. This can be a named
--  extension (such as ApplicationAreaExtension) or the AllExtension. The
--  REVISED_INDICATOR flag is designed such that a revision of a User Extension
--  can notify the user of a top-level ABIE by setting this flag to true. The
--  TOP_LEVEL_ABIE_ID column makes it more efficient to when opening a
--  top-level ABIE, the user can be notified of any new revision of the
--  extension. A record in this table is created only when there is a user
--  extension to the the OAGIS extension component/ACC.
CREATE TABLE BIE_USER_EXT_REVISION
  (
    BIE_USER_EXT_REVISION_ID DECIMAL (19) NOT NULL DEFAULT NEXTVAL('BIE_USER_EXT_REVISION_ID_SEQ'),
    TOP_LEVEL_ABIE_ID        DECIMAL (19) NOT NULL ,
    EXT_ABIE_ID              DECIMAL (19) ,
    EXT_ACC_ID               DECIMAL (19) NOT NULL ,
    USER_EXT_ACC_ID          DECIMAL (19) NOT NULL ,
    REVISED_INDICATOR        BOOLEAN NOT NULL
  )
  ;
COMMENT ON TABLE BIE_USER_EXT_REVISION
IS
  'This table is a log of events. It keeps track of the User Extension ACC (the specific revision) used by an Extension ABIE. This can be a named extension (such as ApplicationAreaExtension) or the AllExtension. The REVISED_INDICATOR flag is designed such that a revision of a User Extension can notify the user of a top-level ABIE by setting this flag to true. The TOP_LEVEL_ABIE_ID column makes it more efficient to when opening a top-level ABIE, the user can be notified of any new revision of the extension. A record in this table is created only when there is a user extension to the the OAGIS extension component/ACC.' ;
  COMMENT ON COLUMN BIE_USER_EXT_REVISION.BIE_USER_EXT_REVISION_ID
IS
  'Primary, internal database key.' ;
  COMMENT ON COLUMN BIE_USER_EXT_REVISION.TOP_LEVEL_ABIE_ID
IS
  'This is a foreign key pointing to an ABIE record which is a top-level ABIE. ' ;
  COMMENT ON COLUMN BIE_USER_EXT_REVISION.EXT_ABIE_ID
IS
  'This points to an ABIE record corresponding to the EXTENSION_ACC_ID record. For example, this column can point to the ApplicationAreaExtension ABIE which is based on the ApplicationAreaExtension ACC (referred to by the EXT_ACC_ID column). This column can be NULL only when the extension is the AllExtension because there is no corresponding ABIE for the AllExtension ACC.' ;
  COMMENT ON COLUMN BIE_USER_EXT_REVISION.EXT_ACC_ID
IS
  'This points to an extension ACC on which the ABIE indicated by the EXT_ABIE_ID column is based. E.g. It may point to an ApplicationAreaExtension ACC, AllExtension ACC, ActualLedgerExtension ACC, etc. It should be noted that an ACC record pointed to must have the OAGIS_COMPONENT_TYPE = 2 (Extension).' ;
  COMMENT ON COLUMN BIE_USER_EXT_REVISION.USER_EXT_ACC_ID
IS
  'This column points to the specific revision of a User Extension ACC (this is an ACC whose OAGIS_COMPONENT_TYPE = 4) currently used by the ABIE as indicated by the EXT_ABIE_ID or the by the TOP_LEVEL_ABIE_ID (in case of the AllExtension). ' ;
  COMMENT ON COLUMN BIE_USER_EXT_REVISION.REVISED_INDICATOR
IS
  'This column is a flag indicating to whether the User Extension ACC (as identified in the USER_EXT_ACC_ID column) has been revised, i.e., there is a newer version of the user extension ACC than the one currently used by the EXT_ABIE_ID. 0 means the USER_EXT_ACC_ID is current, 1 means it is not current.' ;
CREATE UNIQUE INDEX BIE_UXR_PK_IDX ON BIE_USER_EXT_REVISION
  (
    BIE_USER_EXT_REVISION_ID ASC
  )
  ;
  CREATE INDEX BIE_UXR_TLVL_ABIE_ID_IDX ON BIE_USER_EXT_REVISION
    (
      TOP_LEVEL_ABIE_ID ASC
    )
    ;
  CREATE INDEX BIE_UXR_EXT_ABIE_ID_IDX ON BIE_USER_EXT_REVISION
    (
      EXT_ABIE_ID ASC
    )
    ;
  CREATE INDEX BIE_UXR_EXT_ACC_ID_IDX ON BIE_USER_EXT_REVISION
    (
      EXT_ACC_ID ASC
    )
    ;
  CREATE INDEX BIE_UXR_USR_EXT_ACC_ID_IDX ON BIE_USER_EXT_REVISION
    (
      USER_EXT_ACC_ID ASC
    )
    ;
ALTER TABLE BIE_USER_EXT_REVISION ADD CONSTRAINT BUER_PK PRIMARY KEY ( BIE_USER_EXT_REVISION_ID ) ;


--  This table represents a business context. A business context is a
--  combination of one or more business context values.
CREATE TABLE BIZ_CTX
  (
    BIZ_CTX_ID            DECIMAL (19) NOT NULL DEFAULT NEXTVAL('BIZ_CTX_ID_SEQ'),
    GUID                  VARCHAR (41) NOT NULL ,
    NAME                  VARCHAR (255) ,
    CREATED_BY            DECIMAL (19) NOT NULL ,
    LAST_UPDATED_BY       DECIMAL (19) NOT NULL ,
    CREATION_TIMESTAMP    TIMESTAMP WITH TIME ZONE NOT NULL ,
    LAST_UPDATE_TIMESTAMP TIMESTAMP WITH TIME ZONE NOT NULL
  )
  ;
COMMENT ON TABLE BIZ_CTX
IS
  'This table represents a business context. A business context is a combination of one or more business context values.' ;
  COMMENT ON COLUMN BIZ_CTX.BIZ_CTX_ID
IS
  'Primary, internal database key.' ;
  COMMENT ON COLUMN BIZ_CTX.GUID
IS
  'A globally unique identifier (GUID). Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.' ;
  COMMENT ON COLUMN BIZ_CTX.NAME
IS
  'Short, descriptive name of the business context.' ;
  COMMENT ON COLUMN BIZ_CTX.CREATED_BY
IS
  'Foreign key to the APP_USER table referring to the user who creates the entity. ' ;
  COMMENT ON COLUMN BIZ_CTX.LAST_UPDATED_BY
IS
  'Foreign key to the APP_USER table referring to the last user who has updated the business context.' ;
  COMMENT ON COLUMN BIZ_CTX.CREATION_TIMESTAMP
IS
  'Timestamp when the business context record was first created. ' ;
  COMMENT ON COLUMN BIZ_CTX.LAST_UPDATE_TIMESTAMP
IS
  'The timestamp when the business context was last updated.' ;
CREATE UNIQUE INDEX BIZ_CTX_BIZ_CTX_ID_IDX ON BIZ_CTX
  (
    BIZ_CTX_ID ASC
  )
  ;
  CREATE INDEX BIZ_CTX_CREATED_BY_IDX ON BIZ_CTX
    ( CREATED_BY ASC
    ) ;
  CREATE INDEX BIZ_CTX_LAST_UPDATED_BY_IDX ON BIZ_CTX
    ( LAST_UPDATED_BY ASC
    ) ;
ALTER TABLE BIZ_CTX ADD CONSTRAINT BIZ_CTX_PK PRIMARY KEY ( BIZ_CTX_ID ) ;
ALTER TABLE BIZ_CTX ADD CONSTRAINT BIZ_CTX_UK1 UNIQUE ( GUID ) ;


--  This table represents business context values for business contexts. It
--  provides the associations between a business context and a context scheme
--  value.
CREATE TABLE BIZ_CTX_VALUE
  (
    BIZ_CTX_VALUE_ID    DECIMAL (19) NOT NULL DEFAULT NEXTVAL('BIZ_CTX_VALUE_ID_SEQ'),
    BIZ_CTX_ID          DECIMAL (19) NOT NULL ,
    CTX_SCHEME_VALUE_ID DECIMAL (19) NOT NULL
  )
  ;
COMMENT ON TABLE BIZ_CTX_VALUE
IS
  'This table represents business context values for business contexts. It provides the associations between a business context and a context scheme value.' ;
  COMMENT ON COLUMN BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID
IS
  'Primary, internal database key.' ;
  COMMENT ON COLUMN BIZ_CTX_VALUE.BIZ_CTX_ID
IS
  'Foreign key to the biz_ctx table.' ;
  COMMENT ON COLUMN BIZ_CTX_VALUE.CTX_SCHEME_VALUE_ID
IS
  'Foreign key to the CTX_SCHEME_VALUE table.' ;
CREATE UNIQUE INDEX BIZ_CTX_VAL_PK_IDX ON BIZ_CTX_VALUE
  (
    BIZ_CTX_VALUE_ID ASC
  )
  ;
  CREATE INDEX BIZ_CTX_VAL_CTX_SCH_VAL_ID_IDX ON BIZ_CTX_VALUE
    (
      CTX_SCHEME_VALUE_ID ASC
    )
    ;
  CREATE INDEX BIZ_CTX_VALUE_BIZ_CTX_ID_IDX ON BIZ_CTX_VALUE
    (
      BIZ_CTX_ID ASC
    ) ;
ALTER TABLE BIZ_CTX_VALUE ADD CONSTRAINT BIZ_CTX_VALUE_PK PRIMARY KEY ( BIZ_CTX_VALUE_ID ) ;


--  This table stores schemas whose content is only imported as a whole and is
--  represented in Blob.
CREATE TABLE BLOB_CONTENT
  (
    BLOB_CONTENT_ID DECIMAL (19) NOT NULL DEFAULT NEXTVAL('BLOB_CONTENT_ID_SEQ'),
    CONTENT BYTEA NOT NULL ,
    RELEASE_ID DECIMAL (19) NOT NULL ,
    MODULE_ID  DECIMAL (19) NOT NULL
  )
  ;
COMMENT ON TABLE BLOB_CONTENT
IS
  'This table stores schemas whose content is only imported as a whole and is represented in Blob.' ;
  COMMENT ON COLUMN BLOB_CONTENT.BLOB_CONTENT_ID
IS
  'Primary, internal database key.' ;
  COMMENT ON COLUMN BLOB_CONTENT.CONTENT
IS
  'The Blob content of the schema file.' ;
  COMMENT ON COLUMN BLOB_CONTENT.RELEASE_ID
IS
  'The release to which this file/content belongs/published.' ;
  COMMENT ON COLUMN BLOB_CONTENT.MODULE_ID
IS
  'Foreign key to the module table indicating the physical file the blob content should be output to when generating/serializing the content.' ;
CREATE UNIQUE INDEX BLOB_CONTENT_PK_IDX ON BLOB_CONTENT
  (
    BLOB_CONTENT_ID ASC
  )
  ;
  CREATE INDEX BLOB_CONTENT_RELEASE_ID_IDX ON BLOB_CONTENT
    ( RELEASE_ID ASC
    ) ;
  CREATE INDEX BLOB_CONTENT_MODULE_ID_IDX ON BLOB_CONTENT
    ( MODULE_ID ASC
    ) ;
ALTER TABLE BLOB_CONTENT ADD CONSTRAINT BLOB_CONTENT_PK PRIMARY KEY ( BLOB_CONTENT_ID ) ;


--  This table capture allowed primitives of the CDT’s Content Component.  The
--  information in this table is captured from the Allowed Primitive column in
--  each of the CDT Content Component section/table in CCTS DTC3.
CREATE TABLE CDT_AWD_PRI
  (
    CDT_AWD_PRI_ID DECIMAL (19) NOT NULL DEFAULT NEXTVAL('CDT_AWD_PRI_ID_SEQ'),
    CDT_ID         DECIMAL (19) NOT NULL ,
    CDT_PRI_ID     DECIMAL (19) NOT NULL ,
    IS_DEFAULT     BOOLEAN NOT NULL
  )
  ;
COMMENT ON TABLE CDT_AWD_PRI
IS
  'This table capture allowed primitives of the CDT’s Content Component.  The information in this table is captured from the Allowed Primitive column in each of the CDT Content Component section/table in CCTS DTC3.' ;
  COMMENT ON COLUMN CDT_AWD_PRI.CDT_AWD_PRI_ID
IS
  'Primary, internal database key.' ;
  COMMENT ON COLUMN CDT_AWD_PRI.CDT_ID
IS
  'Foreign key pointing to a CDT in the DT table.' ;
  COMMENT ON COLUMN CDT_AWD_PRI.CDT_PRI_ID
IS
  'Foreign key from the CDT_PRI table. It indicates the primative allowed for the CDT identified in the CDT_ID column. ' ;
  COMMENT ON COLUMN CDT_AWD_PRI.IS_DEFAULT
IS
  'Indicating a default primitive for the CDT’s Content Component. True for a default primitive; False otherwise.' ;
CREATE UNIQUE INDEX CDT_AWD_PRI_CDT_AWD_PRI_ID_IDX ON CDT_AWD_PRI
  (
    CDT_AWD_PRI_ID ASC
  )
  ;
  CREATE INDEX CDT_AWD_PRI_CDT_ID_IDX ON CDT_AWD_PRI
    ( CDT_ID ASC
    ) ;
  CREATE INDEX CDT_AWD_PRI_CDT_PRI_ID_IDX ON CDT_AWD_PRI
    ( CDT_PRI_ID ASC
    ) ;
ALTER TABLE CDT_AWD_PRI ADD CONSTRAINT CDT_AWD_PRI_PK PRIMARY KEY ( CDT_AWD_PRI_ID ) ;


--  This table allows for concrete mapping between the CDT Primitives and types
--  in a particular expression such as XML Schema, JSON. At this point, it is
--  not clear whether a separate table will be needed for each expression. The
--  current table holds the map to XML Schema built-in types.
--  For each additional expression, a column similar to the XBT_ID column will
--  need to be added to this table for mapping to data types in another
--  expression.
--  If we use a separate table for each expression, then we need binding all
--  the way to BDT (or even BBIE) for every new expression. That would be
--  almost like just store a BDT file. But using a column may not work with all
--  kinds of expressions, particulary if it does not map well to the XML schema
--  data types.
CREATE TABLE CDT_AWD_PRI_XPS_TYPE_MAP
  (
    CDT_AWD_PRI_XPS_TYPE_MAP_ID DECIMAL (19) NOT NULL DEFAULT NEXTVAL('CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ'),
    CDT_AWD_PRI_ID              DECIMAL (19) NOT NULL ,
    XBT_ID                      DECIMAL (19) NOT NULL
  )
  ;
COMMENT ON TABLE CDT_AWD_PRI_XPS_TYPE_MAP
IS
  'This table allows for concrete mapping between the CDT Primitives and types in a particular expression such as XML Schema, JSON. At this point, it is not clear whether a separate table will be needed for each expression. The current table holds the map to XML Schema built-in types.

For each additional expression, a column similar to the XBT_ID column will need to be added to this table for mapping to data types in another expression.

If we use a separate table for each expression, then we need binding all the way to BDT (or even BBIE) for every new expression. That would be almost like just store a BDT file. But using a column may not work with all kinds of expressions, particulary if it does not map well to the XML schema data types. ' ;
  COMMENT ON COLUMN CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID
IS
  'Internal, primary database key.' ;
  COMMENT ON COLUMN CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_ID
IS
  'Foreign key to the CDT_AWD_PRI table.' ;
  COMMENT ON COLUMN CDT_AWD_PRI_XPS_TYPE_MAP.XBT_ID
IS
  'Foreign key and to the XBT table. It identifies the XML schema built-in types that can be mapped to the CDT primivite identified in the CDT_AWD_PRI_ID column. The CDT primitives are typically broad and hence it usually maps to more than one XML schema built-in types.' ;
CREATE UNIQUE INDEX CAPXTM_PK_IDX ON CDT_AWD_PRI_XPS_TYPE_MAP
  (
    CDT_AWD_PRI_XPS_TYPE_MAP_ID ASC
  )
  ;
  CREATE INDEX CAPXTM_CAP_ID_IDX ON CDT_AWD_PRI_XPS_TYPE_MAP
    (
      CDT_AWD_PRI_ID ASC
    )
    ;
  CREATE INDEX CAPXTM_XBT_ID_IDX ON CDT_AWD_PRI_XPS_TYPE_MAP
    (
      XBT_ID ASC
    )
    ;
ALTER TABLE CDT_AWD_PRI_XPS_TYPE_MAP ADD CONSTRAINT CAPXTM_PK PRIMARY KEY ( CDT_AWD_PRI_XPS_TYPE_MAP_ID ) ;


--  This table stores the CDT primitives.
CREATE TABLE CDT_PRI
  (
    CDT_PRI_ID DECIMAL (19) NOT NULL DEFAULT NEXTVAL('CDT_PRI_ID_SEQ'),
    NAME       VARCHAR (45) NOT NULL
  )
  ;
COMMENT ON TABLE CDT_PRI
IS
  'This table stores the CDT primitives.' ;
  COMMENT ON COLUMN CDT_PRI.CDT_PRI_ID
IS
  'Internal, primary database key.' ;
  COMMENT ON COLUMN CDT_PRI.NAME
IS
  'Name of the CDT primitive per the CCTS datatype catalog, e.g., Decimal.' ;
CREATE UNIQUE INDEX CDT_PRI_CDT_PRI_ID_IDX ON CDT_PRI
  (
    CDT_PRI_ID ASC
  )
  ;
ALTER TABLE CDT_PRI ADD CONSTRAINT CDT_PRI_PK PRIMARY KEY ( CDT_PRI_ID ) ;
ALTER TABLE CDT_PRI ADD CONSTRAINT CDT_PRI_UK1 UNIQUE ( NAME ) ;


--  This table capture the CDT primitives allowed for a particular SC of a CDT.
--  It also stores the CDT primitives allowed for a SC of a BDT that extends
--  its base (such SC is not defined in the CCTS data type catalog
--  specification).
CREATE TABLE CDT_SC_AWD_PRI
  (
    CDT_SC_AWD_PRI_ID DECIMAL (19) NOT NULL DEFAULT NEXTVAL('CDT_SC_AWD_PRI_ID_SEQ'),
    CDT_SC_ID         DECIMAL (19) NOT NULL ,
    CDT_PRI_ID        DECIMAL (19) NOT NULL ,
    IS_DEFAULT        BOOLEAN NOT NULL
  )
  ;
COMMENT ON TABLE CDT_SC_AWD_PRI
IS
  'This table capture the CDT primitives allowed for a particular SC of a CDT. It also stores the CDT primitives allowed for a SC of a BDT that extends its base (such SC is not defined in the CCTS data type catalog specification).' ;
  COMMENT ON COLUMN CDT_SC_AWD_PRI.CDT_SC_AWD_PRI_ID
IS
  'Internal, primary database key.' ;
  COMMENT ON COLUMN CDT_SC_AWD_PRI.CDT_SC_ID
IS
  'Foreign key pointing to the supplementary component (SC).' ;
  COMMENT ON COLUMN CDT_SC_AWD_PRI.CDT_PRI_ID
IS
  'A foreign key pointing to the CDT_Pri table. It represents a CDT primitive allowed for the suppliement component identified in the CDT_SC_ID column.' ;
  COMMENT ON COLUMN CDT_SC_AWD_PRI.IS_DEFAULT
IS
  'Indicating whether the primitive is the default primitive of the supplementary component.' ;
CREATE UNIQUE INDEX CDT_SC_AWD_PRI_PK_IDX ON CDT_SC_AWD_PRI
  (
    CDT_SC_AWD_PRI_ID ASC
  )
  ;
  CREATE INDEX CDT_SC_AWD_PRI_CDT_SC_ID_IDX ON CDT_SC_AWD_PRI
    (
      CDT_SC_ID ASC
    ) ;
  CREATE INDEX CDT_SC_AWD_PRI_CDT_PRI_ID_IDX ON CDT_SC_AWD_PRI
    (
      CDT_PRI_ID ASC
    ) ;
ALTER TABLE CDT_SC_AWD_PRI ADD CONSTRAINT CDT_SC_AWD_PRI_PK PRIMARY KEY ( CDT_SC_AWD_PRI_ID ) ;


--  The purpose of this table is the same as that of the
--  CDT_AWD_PRI_XPS_TYPE_MAP, but it is for the supplementary component (SC).
--  It allows for the concrete mapping between the CDT Primitives and types in
--  a particular expression such as XML Schema, JSON.
CREATE TABLE CDT_SC_AWD_PRI_XPS_TYPE_MAP
  (
    CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID DECIMAL (19) NOT NULL DEFAULT NEXTVAL('CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ'),
    CDT_SC_AWD_PRI_ID              DECIMAL (19) NOT NULL ,
    XBT_ID                         DECIMAL (19) NOT NULL
  )
  ;
COMMENT ON TABLE CDT_SC_AWD_PRI_XPS_TYPE_MAP
IS
  'The purpose of this table is the same as that of the CDT_AWD_PRI_XPS_TYPE_MAP, but it is for the supplementary component (SC). It allows for the concrete mapping between the CDT Primitives and types in a particular expression such as XML Schema, JSON. ' ;
  COMMENT ON COLUMN CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID
IS
  'Internal, primary database key.' ;
  COMMENT ON COLUMN CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_ID
IS
  'Foreign key to the CDT_SC_AWD_PRI table.' ;
  COMMENT ON COLUMN CDT_SC_AWD_PRI_XPS_TYPE_MAP.XBT_ID
IS
  'Foreign key to the XBT table. It identifies an XML schema built-in type that maps to the CDT SC Allowed Primitive identified in the CDT_SC_AWD_PRI_ID column.' ;
CREATE UNIQUE INDEX CSAPXTM_PK_IDX ON CDT_SC_AWD_PRI_XPS_TYPE_MAP
  (
    CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID ASC
  )
  ;
  CREATE INDEX CSAPXTM_CSAP_ID_IDX ON CDT_SC_AWD_PRI_XPS_TYPE_MAP
    (
      CDT_SC_AWD_PRI_ID ASC
    )
    ;
  CREATE INDEX CSAPXTM_XBT_ID_IDX ON CDT_SC_AWD_PRI_XPS_TYPE_MAP
    (
      XBT_ID ASC
    )
    ;
ALTER TABLE CDT_SC_AWD_PRI_XPS_TYPE_MAP ADD CONSTRAINT CSAPXTM_PK PRIMARY KEY ( CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID ) ;


--  This table captures a client organization. It is used, for example, to
--  indicate the customer, for which the BIE was generated.
CREATE TABLE CLIENT
  (
    CLIENT_ID DECIMAL (19) NOT NULL DEFAULT NEXTVAL('CLIENT_ID_SEQ'),
    NAME      VARCHAR (200)
  )
  ;
COMMENT ON TABLE CLIENT
IS
  'This table captures a client organization. It is used, for example, to indicate the customer, for which the BIE was generated.' ;
  COMMENT ON COLUMN CLIENT.CLIENT_ID
IS
  'Primary, internal database key.' ;
  COMMENT ON COLUMN CLIENT.NAME
IS
  'Pretty print name of the client.' ;
CREATE UNIQUE INDEX CLIENT_CLIENT_ID_IDX ON CLIENT
  (
    CLIENT_ID ASC
  )
  ;
ALTER TABLE CLIENT ADD CONSTRAINT CLIENT_PK PRIMARY KEY ( CLIENT_ID ) ;


--  This table stores information about a code list. When a code list is
--  derived from another code list, the whole set of code values belonging to
--  the based code list will be copied.
CREATE TABLE CODE_LIST
  (
    CODE_LIST_ID   DECIMAL (19) NOT NULL DEFAULT NEXTVAL('CODE_LIST_ID_SEQ'),
    GUID           VARCHAR (41) NOT NULL ,
    ENUM_TYPE_GUID VARCHAR (41) ,
    NAME           VARCHAR (100) ,
    LIST_ID        VARCHAR (100) NOT NULL ,
    AGENCY_ID      DECIMAL (19) ,
    VERSION_ID     VARCHAR (10) NOT NULL ,
    REMARK                VARCHAR (225) ,
    DEFINITION TEXT ,
    DEFINITION_SOURCE     VARCHAR (100) ,
    BASED_CODE_LIST_ID    DECIMAL (19) ,
    EXTENSIBLE_INDICATOR  BOOLEAN NOT NULL ,
    MODULE_ID             DECIMAL (19) ,
    CREATED_BY            DECIMAL (19) NOT NULL ,
    LAST_UPDATED_BY       DECIMAL (19) NOT NULL ,
    CREATION_TIMESTAMP    TIMESTAMP WITH TIME ZONE NOT NULL ,
    LAST_UPDATE_TIMESTAMP TIMESTAMP WITH TIME ZONE NOT NULL ,
    STATE                 VARCHAR (10)
  )
  ;
COMMENT ON TABLE CODE_LIST
IS
  'This table stores information about a code list. When a code list is derived from another code list, the whole set of code values belonging to the based code list will be copied.' ;
  COMMENT ON COLUMN CODE_LIST.CODE_LIST_ID
IS
  'Internal, primary database key.' ;
  COMMENT ON COLUMN CODE_LIST.GUID
IS
  'GUID of the code list. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.' ;
  COMMENT ON COLUMN CODE_LIST.ENUM_TYPE_GUID
IS
  'In the OAGIS Model XML schema, a type, which keeps all the enumerated values, is  defined separately from the type that represents a code list. This only applies to some code lists. When that is the case, this column stores the GUID of that enumeration type.' ;
  COMMENT ON COLUMN CODE_LIST.NAME
IS
  'Name of the code list.' ;
  COMMENT ON COLUMN CODE_LIST.LIST_ID
IS
  'External identifier.' ;
  COMMENT ON COLUMN CODE_LIST.AGENCY_ID
IS
  'Foreign key to the AGENCY_ID_LIST_VALUE table. It indicates the organization which maintains the code list.' ;
  COMMENT ON COLUMN CODE_LIST.VERSION_ID
IS
  'Code list version number.' ;
  COMMENT ON COLUMN CODE_LIST.REMARK
IS
  'Usage information about the code list.' ;
  COMMENT ON COLUMN CODE_LIST.DEFINITION
IS
  'Description of the code list.' ;
  COMMENT ON COLUMN CODE_LIST.DEFINITION_SOURCE
IS
  'This is typically a URL which indicates the source of the code list''s DEFINITION.' ;
  COMMENT ON COLUMN CODE_LIST.BASED_CODE_LIST_ID
IS
  'This is a foreign key to the CODE_LIST table itself. This identifies the code list on which this code list is based, if any. The derivation may be restriction and/or extension.' ;
  COMMENT ON COLUMN CODE_LIST.EXTENSIBLE_INDICATOR
IS
  'This is a flag to indicate whether the code list is final and shall not be further derived.' ;
  COMMENT ON COLUMN CODE_LIST.MODULE_ID
IS
  'Foreign key to the module table indicating the physical file the code list belongs to when generating a physical model schema. ' ;
  COMMENT ON COLUMN CODE_LIST.CREATED_BY
IS
  'Foreign key to the APP_USER table. It indicates the user who created the code list.' ;
  COMMENT ON COLUMN CODE_LIST.LAST_UPDATED_BY
IS
  'Foreign key to the APP_USER table. It identifies the user who last updated the code list.' ;
  COMMENT ON COLUMN CODE_LIST.CREATION_TIMESTAMP
IS
  'Timestamp when the code list was created.' ;
  COMMENT ON COLUMN CODE_LIST.LAST_UPDATE_TIMESTAMP
IS
  'Timestamp when the code list was last updated.' ;
  COMMENT ON COLUMN CODE_LIST.STATE
IS
  'Life cycle state of the code list. Possible values are Editing, Published, or Deleted. Only a code list in published state is available for derivation and for used by the CC and BIE. Once the code list is published, it cannot go back to Editing. A new version would have to be created.' ;
  CREATE INDEX CODE_LIST_BASED_CL_ID_IDX ON CODE_LIST
    (
      BASED_CODE_LIST_ID ASC
    )
    ;
CREATE UNIQUE INDEX CODE_LIST_CODE_LIST_ID_IDX ON CODE_LIST
  (
    CODE_LIST_ID ASC
  )
  ;
  CREATE INDEX CODE_LIST_AGENCY_ID_IDX ON CODE_LIST
    ( AGENCY_ID ASC
    ) ;
  CREATE INDEX CODE_LIST_MODULE_ID_IDX ON CODE_LIST
    ( MODULE_ID ASC
    ) ;
  CREATE INDEX CODE_LIST_CREATED_BY_IDX ON CODE_LIST
    ( CREATED_BY ASC
    ) ;
  CREATE INDEX CODE_LIST_LAST_UPDATED_BY_IDX ON CODE_LIST
    (
      LAST_UPDATED_BY ASC
    ) ;
ALTER TABLE CODE_LIST ADD CONSTRAINT CODE_LIST_PK PRIMARY KEY ( CODE_LIST_ID ) ;
ALTER TABLE CODE_LIST ADD CONSTRAINT CODE_LIST_UK1 UNIQUE ( GUID ) ;
ALTER TABLE CODE_LIST ADD CONSTRAINT CODE_LIST_UK2 UNIQUE ( ENUM_TYPE_GUID ) ;


--  Each record in this table stores a code list value of a code list. A code
--  list value may be inherited from another code list on which it is based.
--  However, inherited value may be restricted (i.e., disabled and cannot be
--  used) in this code list, i.e., the USED_INDICATOR = false. If the value
--  cannot be used since the based code list, then the LOCKED_INDICATOR = TRUE,
--  because the USED_INDICATOR of such code list value is FALSE by default and
--  can no longer be changed.
CREATE TABLE CODE_LIST_VALUE
  (
    CODE_LIST_VALUE_ID DECIMAL (19) NOT NULL DEFAULT NEXTVAL('CODE_LIST_VALUE_ID_SEQ'),
    CODE_LIST_ID       DECIMAL (19) NOT NULL ,
    VALUE              VARCHAR (100) NOT NULL ,
    NAME               VARCHAR (100) ,
    DEFINITION TEXT ,
    DEFINITION_SOURCE   VARCHAR (100) ,
    USED_INDICATOR      BOOLEAN NOT NULL ,
    LOCKED_INDICATOR    BOOLEAN NOT NULL ,
    EXTENSION_INDICATOR BOOLEAN NOT NULL
  )
  ;
COMMENT ON TABLE CODE_LIST_VALUE
IS
  'Each record in this table stores a code list value of a code list. A code list value may be inherited from another code list on which it is based. However, inherited value may be restricted (i.e., disabled and cannot be used) in this code list, i.e., the USED_INDICATOR = false. If the value cannot be used since the based code list, then the LOCKED_INDICATOR = TRUE, because the USED_INDICATOR of such code list value is FALSE by default and can no longer be changed.' ;
  COMMENT ON COLUMN CODE_LIST_VALUE.CODE_LIST_VALUE_ID
IS
  'Internal, primary database key.' ;
  COMMENT ON COLUMN CODE_LIST_VALUE.CODE_LIST_ID
IS
  'Foreign key to the CODE_LIST table. It indicates the code list this code value belonging to.' ;
  COMMENT ON COLUMN CODE_LIST_VALUE.VALUE
IS
  'The code list value used in the instance data, e.g., EA, US-EN.' ;
  COMMENT ON COLUMN CODE_LIST_VALUE.NAME
IS
  'Pretty print name of the code list value, e.g., ''Each'' for EA, ''English'' for EN.' ;
  COMMENT ON COLUMN CODE_LIST_VALUE.DEFINITION
IS
  'Long description or explannation of the code list value, e.g., ''EA is a discrete quantity for counting each unit of an item, such as, 2 shampoo bottles, 3 box of cereals''.' ;
  COMMENT ON COLUMN CODE_LIST_VALUE.DEFINITION_SOURCE
IS
  'This is typically a URL identifying the source of the DEFINITION column.' ;
  COMMENT ON COLUMN CODE_LIST_VALUE.USED_INDICATOR
IS
  'This indicates whether the code value is allowed to be used or not in that code list context. In other words, this flag allows a user to enable or disable a code list value.' ;
  COMMENT ON COLUMN CODE_LIST_VALUE.LOCKED_INDICATOR
IS
  'This indicates whether the USED_INDICATOR can be changed from False to True. In other words, if the code value is derived from its base code list and the USED_INDICATOR of the code value in the base is False, then the USED_iNDICATOR cannot be changed from False to True for this code value; and this is indicated using this LOCKED_INDICATOR flag in the derived code list.' ;
  COMMENT ON COLUMN CODE_LIST_VALUE.EXTENSION_INDICATOR
IS
  'This indicates whether this code value has just been added in this code list. It is used particularly in the derived code list. If the code value has only been added to the derived code list, then it can be deleted; otherwise, it cannot be deleted.' ;
CREATE UNIQUE INDEX CODE_LIST_VALUE_PK_IDX ON CODE_LIST_VALUE
  (
    CODE_LIST_VALUE_ID ASC
  )
  ;
  CREATE INDEX CODE_LIST_VALUE_CL_ID_IDX ON CODE_LIST_VALUE
    (
      CODE_LIST_ID ASC
    )
    ;
ALTER TABLE CODE_LIST_VALUE ADD CONSTRAINT CLV_PK PRIMARY KEY ( CODE_LIST_VALUE_ID ) ;


--  This table captures the context category. Examples of context categories as
--  described in the CCTS are business process, industry, etc.
CREATE TABLE CTX_CATEGORY
  (
    CTX_CATEGORY_ID DECIMAL (19) NOT NULL DEFAULT NEXTVAL('CTX_CATEGORY_ID_SEQ'),
    GUID            VARCHAR (41) NOT NULL ,
    NAME            VARCHAR (255) ,
    DESCRIPTION TEXT
  )
  ;
COMMENT ON TABLE CTX_CATEGORY
IS
  'This table captures the context category. Examples of context categories as described in the CCTS are business process, industry, etc.' ;
  COMMENT ON COLUMN CTX_CATEGORY.CTX_CATEGORY_ID
IS
  'Internal, primary, database key.' ;
  COMMENT ON COLUMN CTX_CATEGORY.GUID
IS
  'GUID of the context category.  Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.' ;
  COMMENT ON COLUMN CTX_CATEGORY.NAME
IS
  'Short name of the context category.' ;
  COMMENT ON COLUMN CTX_CATEGORY.DESCRIPTION
IS
  'Explanation of what the context category is.' ;
CREATE UNIQUE INDEX CTX_CATEGORY_PK_IDX ON CTX_CATEGORY
  (
    CTX_CATEGORY_ID ASC
  )
  ;
ALTER TABLE CTX_CATEGORY ADD CONSTRAINT CTX_CATEGORY_PK PRIMARY KEY ( CTX_CATEGORY_ID ) ;
ALTER TABLE CTX_CATEGORY ADD CONSTRAINT CTX_CATEGORY_UK1 UNIQUE ( GUID ) ;


--  This table represents a classification scheme for a context category.
CREATE TABLE CTX_SCHEME
  (
    CTX_SCHEME_ID DECIMAL (19) NOT NULL DEFAULT NEXTVAL('CTX_SCHEME_ID_SEQ'),
    GUID          VARCHAR (41) NOT NULL ,
    SCHEME_ID     VARCHAR (45) NOT NULL ,
    SCHEME_NAME   VARCHAR (255) ,
    DESCRIPTION TEXT ,
    SCHEME_AGENCY_ID      VARCHAR (45) NOT NULL ,
    SCHEME_VERSION_ID     VARCHAR (45) NOT NULL ,
    CTX_CATEGORY_ID       DECIMAL (19) NOT NULL ,
    CREATED_BY            DECIMAL (19) NOT NULL ,
    LAST_UPDATED_BY       DECIMAL (19) NOT NULL ,
    CREATION_TIMESTAMP    TIMESTAMP WITH TIME ZONE NOT NULL ,
    LAST_UPDATE_TIMESTAMP TIMESTAMP WITH TIME ZONE NOT NULL
  )
  ;
COMMENT ON TABLE CTX_SCHEME
IS
  'This table represents a classification scheme for a context category.' ;
  COMMENT ON COLUMN CTX_SCHEME.CTX_SCHEME_ID
IS
  'Internal, primary, database key.' ;
  COMMENT ON COLUMN CTX_SCHEME.GUID
IS
  'GUID of the classification scheme. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.' ;
  COMMENT ON COLUMN CTX_SCHEME.SCHEME_ID
IS
  'External identification of the scheme. ' ;
  COMMENT ON COLUMN CTX_SCHEME.SCHEME_NAME
IS
  'Pretty print name of the classification scheme.' ;
  COMMENT ON COLUMN CTX_SCHEME.DESCRIPTION
IS
  'Description of the classification scheme.' ;
  COMMENT ON COLUMN CTX_SCHEME.SCHEME_AGENCY_ID
IS
  'Identification of the agency maintaining the classification scheme. This column currently does not use the AGENCY_ID_LIST table. It is just a free form text at this point.' ;
  COMMENT ON COLUMN CTX_SCHEME.SCHEME_VERSION_ID
IS
  'Version number of the classification scheme.' ;
  COMMENT ON COLUMN CTX_SCHEME.CTX_CATEGORY_ID
IS
  'This the foreign key to the CTX_CATEGORY table. It identifies the context category associated with this classification scheme.' ;
  COMMENT ON COLUMN CTX_SCHEME.CREATED_BY
IS
  'Foreign key to the APP_USER table. It indicates the user who created this classification scheme.' ;
  COMMENT ON COLUMN CTX_SCHEME.LAST_UPDATED_BY
IS
  'Foreign key to the APP_USER table. It identifies the user who last updated the classification scheme.' ;
  COMMENT ON COLUMN CTX_SCHEME.CREATION_TIMESTAMP
IS
  'Timestamp when the classification scheme was created.' ;
  COMMENT ON COLUMN CTX_SCHEME.LAST_UPDATE_TIMESTAMP
IS
  'Timestamp when the classification scheme was last updated.' ;
CREATE UNIQUE INDEX CTX_SCH_PK_IDX ON CTX_SCHEME
  (
    CTX_SCHEME_ID ASC
  )
  ;
  CREATE INDEX CTX_SCH_CTX_CAT_ID_IDX ON CTX_SCHEME
    (
      CTX_CATEGORY_ID ASC
    )
    ;
  CREATE INDEX CTX_SCH_CREATED_BY_IDX ON CTX_SCHEME
    ( CREATED_BY ASC
    ) ;
  CREATE INDEX CTX_SCH_LAST_UPDATED_BY_IDX ON CTX_SCHEME
    (
      LAST_UPDATED_BY ASC
    )
    ;
ALTER TABLE CTX_SCHEME ADD CONSTRAINT CTX_SCHEME_PK PRIMARY KEY ( CTX_SCHEME_ID ) ;
ALTER TABLE CTX_SCHEME ADD CONSTRAINT CTX_SCHEME_UK1 UNIQUE ( GUID ) ;


--  This table stores the context scheme values for a particular context
--  classification scheme in the CTX_SCHEME table.
CREATE TABLE CTX_SCHEME_VALUE
  (
    CTX_SCHEME_VALUE_ID DECIMAL (19) NOT NULL DEFAULT NEXTVAL('CTX_SCHEME_VALUE_ID_SEQ'),
    GUID                VARCHAR (41) NOT NULL ,
    VALUE               VARCHAR (45) NOT NULL ,
    MEANING TEXT ,
    OWNER_CTX_SCHEME_ID DECIMAL (19)
  )
  ;
COMMENT ON TABLE CTX_SCHEME_VALUE
IS
  'This table stores the context scheme values for a particular context classification scheme in the CTX_SCHEME table.' ;
  COMMENT ON COLUMN CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID
IS
  'Primary, internal database key.' ;
  COMMENT ON COLUMN CTX_SCHEME_VALUE.GUID
IS
  'GUID of the classification scheme value. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.' ;
  COMMENT ON COLUMN CTX_SCHEME_VALUE.VALUE
IS
  'A short value for the scheme value similar to the code list value.' ;
  COMMENT ON COLUMN CTX_SCHEME_VALUE.MEANING
IS
  'The description, explanatiion of the scheme value.' ;
  COMMENT ON COLUMN CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID
IS
  'Foreign key to the CTX_SCHEME table. It identifies the classification scheme, to which this scheme value belongs.' ;
CREATE UNIQUE INDEX CTX_SCH_VAL_PK_IDX ON CTX_SCHEME_VALUE
  (
    CTX_SCHEME_VALUE_ID ASC
  )
  ;
  CREATE INDEX CTX_SCH_VAL_CTX_SCH_ID_IDX ON CTX_SCHEME_VALUE
    (
      OWNER_CTX_SCHEME_ID ASC
    )
    ;
ALTER TABLE CTX_SCHEME_VALUE ADD CONSTRAINT CTX_SCHEME_VALUE_PK PRIMARY KEY ( CTX_SCHEME_VALUE_ID ) ;
ALTER TABLE CTX_SCHEME_VALUE ADD CONSTRAINT CTX_SCHEME_VALUE_UK1 UNIQUE ( GUID ) ;


--  The DT table stores both CDT and BDT. The two types of DTs are
--  differentiated by the TYPE column.
CREATE TABLE DT
  (
    DT_ID                  DECIMAL (19) NOT NULL DEFAULT NEXTVAL('DT_ID_SEQ'),
    GUID                   VARCHAR (41) NOT NULL ,
    TYPE                   DECIMAL (10) ,
    VERSION_NUM            VARCHAR (45) NOT NULL ,
    PREVIOUS_VERSION_DT_ID DECIMAL (19) ,
    DATA_TYPE_TERM         VARCHAR (45) ,
    QUALIFIER              VARCHAR (100) ,
    BASED_DT_ID            DECIMAL (19) ,
    DEN                    VARCHAR (200) NOT NULL ,
    CONTENT_COMPONENT_DEN  VARCHAR (200) ,
    DEFINITION TEXT ,
    DEFINITION_SOURCE VARCHAR (200) ,
    CONTENT_COMPONENT_DEFINITION TEXT ,
    REVISION_DOC TEXT ,
    STATE                 DECIMAL (10) ,
    MODULE_ID             DECIMAL (19) ,
    CREATED_BY            DECIMAL (19) NOT NULL ,
    OWNER_USER_ID         DECIMAL (19) NOT NULL ,
    LAST_UPDATED_BY       DECIMAL (19) NOT NULL ,
    CREATION_TIMESTAMP    TIMESTAMP WITH TIME ZONE NOT NULL ,
    LAST_UPDATE_TIMESTAMP TIMESTAMP WITH TIME ZONE NOT NULL ,
    REVISION_NUM          DECIMAL (10) NOT NULL ,
    REVISION_TRACKING_NUM DECIMAL (10) NOT NULL ,
    REVISION_ACTION       DECIMAL (3) ,
    RELEASE_ID            DECIMAL (19) ,
    CURRENT_BDT_ID        DECIMAL (19) ,
    IS_DEPRECATED         BOOLEAN NOT NULL
  )
  ;
COMMENT ON TABLE DT
IS
  'The DT table stores both CDT and BDT. The two types of DTs are differentiated by the TYPE column.' ;
  COMMENT ON COLUMN DT.DT_ID
IS
  'Internal, primary database key.' ;
  COMMENT ON COLUMN DT.GUID
IS
  'GUID of the data type. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence.' ;
  COMMENT ON COLUMN DT.TYPE
IS
  'List value: 0 = CDT, 1 = BDT.' ;
  COMMENT ON COLUMN DT.VERSION_NUM
IS
  'Format X.Y.Z where all of them are integer with no leading zero allowed. X means major version number, Y means minor version number and Z means patch version number. This column is different from the REVISION_NUM column in that the new version is only assigned to the release component while the REVISION_NUM is assigned every time editing life cycle.' ;
  COMMENT ON COLUMN DT.PREVIOUS_VERSION_DT_ID
IS
  'Foregin key to the DT table itself. It identifies the previous version.' ;
  COMMENT ON COLUMN DT.DATA_TYPE_TERM
IS
  'This is the data type term assigned to the DT. The allowed set of data type terms are defined in the DTC specification. This column is derived from the Based_DT_ID when the column is not blank. ' ;
  COMMENT ON COLUMN DT.QUALIFIER
IS
  'This column shall be blank when the DT_TYPE is CDT. When the DT_TYPE is BDT, this is optional. If the column is not blank it is a qualified BDT. If blank then the row may be a default BDT or an unqualified BDT. Default BDT is OAGIS concrete implementation of the CDT, these are the DT with numbers in the name, e.g., CodeType_1E7368 (DEN is ''Code_1E7368. Type''). Default BDTs are almost like permutation of the CDT options into concrete data types. Unqualified BDT is a BDT that OAGIS model schema generally used for its canonical. A handful of default BDTs were selected; and each of them is wrapped with another type definition that has a simpler name such as CodeType and NormalizedString type - we call these "unqualified BDTs". ' ;
  COMMENT ON COLUMN DT.BASED_DT_ID
IS
  'Foreign key pointing to the DT table itself. This column must be blank when the DT_TYPE is CDT. This column must not be blank when the DT_TYPE is BDT.' ;
  COMMENT ON COLUMN DT.DEN
IS
  'Dictionary Entry Name of the data type. ' ;
  COMMENT ON COLUMN DT.CONTENT_COMPONENT_DEN
IS
  'When the DT_TYPE is CDT this column is automatically derived from DATA_TYPE_TERM as "<DATA_TYPE_TYPE>. Content", where ''Content'' is called property term of the content component according to CCTS. When the DT_TYPE is BDT this column has the same value as its BASED_DT_ID.' ;
  COMMENT ON COLUMN DT.DEFINITION
IS
  'Description of the data type.' ;
  COMMENT ON COLUMN DT.DEFINITION_SOURCE
IS
  'This is typically a URL identifying the source of the DEFINITION column.' ;
  COMMENT ON COLUMN DT.CONTENT_COMPONENT_DEFINITION
IS
  'Description of the content component of the data type.' ;
  COMMENT ON COLUMN DT.REVISION_DOC
IS
  'This is for documenting about the revision, e.g., how the newer version of the DT is different from the previous version.' ;
  COMMENT ON COLUMN DT.STATE
IS
  '1 = EDITING, 2 = CANDIDATE, 3 = PUBLISHED. This the revision life cycle state of the entity.

State change can''t be undone. But the history record can still keep the records of when the state was changed.' ;
  COMMENT ON COLUMN DT.MODULE_ID
IS
  'Foreign key to the MODULE table indicating physical file where the DT shall belong to when it is generated in an expression. ' ;
  COMMENT ON COLUMN DT.CREATED_BY
IS
  'Foreign key to the APP_USER table. It indicates the user who created this DT.' ;
  COMMENT ON COLUMN DT.OWNER_USER_ID
IS
  'Foreign key to the APP_USER table. This is the user who owns the entity, is allowed to edit the entity, and who can transfer the ownership to another user.

The ownership can change throughout the history, but undoing shouldn''t rollback the ownership. ' ;
  COMMENT ON COLUMN DT.LAST_UPDATED_BY
IS
  'Foreign key to the APP_USER table referring to the last user who updated the record.

In the history record, this should always be the user who is editing the entity (perhaps except when the ownership has just been changed).' ;
  COMMENT ON COLUMN DT.CREATION_TIMESTAMP
IS
  'Timestamp when the revision of the DT was created.

This never change for a revision.' ;
  COMMENT ON COLUMN DT.LAST_UPDATE_TIMESTAMP
IS
  'Timestamp when the record was last updated.

The value of this column in the latest history record should be the same as that of the current record. This column keeps the record of when the revision has occurred.' ;
  COMMENT ON COLUMN DT.REVISION_NUM
IS
  'REVISION_NUM is an incremental integer. It tracks changes in each component. If a change is made to a component after it has been published, the component receives a new revision number. Revision number can be 0, 1, 2, and so on. A record with zero revision number reflects the current record of the component (the identity of a component in this case is its GUID or the primary key).' ;
  COMMENT ON COLUMN DT.REVISION_TRACKING_NUM
IS
  'REVISION_TRACKING_NUM supports the ability to undo changes during a revision (life cycle of a revision is from the component''s EDITING state to PUBLISHED state). Once the component has transitioned into the PUBLISHED state for its particular revision, all revision tracking records are deleted except the latest one. REVISION_TRACKING_NUM can be 0, 1, 2, and so on. The zero value is assign to the record with REVISION_NUM = 0 as a default.' ;
  COMMENT ON COLUMN DT.REVISION_ACTION
IS
  'This indicates the action associated with the record. The action can be 1 = INSERT, 2 = UPDATE, and 3 = DELETE. This column is null for the current record.' ;
  COMMENT ON COLUMN DT.RELEASE_ID
IS
  'RELEASE_ID is an incremental integer. It is an unformatted counter part of the RELEASE_DECIMAL in the RELEASE table. RELEASE_ID can be 1, 2, 3, and so on. A release ID indicates the release point when a particular component revision is released. A component revision is only released once and assumed to be included in the subsequent releases unless it has been deleted (as indicated by the REVISION_ACTION column).

Not all component revisions have an associated RELEASE_ID because some revisions may never be released. USER_EXTENSION_GROUP component type is never part of a release.

Unpublished components cannot be released.

This column is NULL for the current record.' ;
  COMMENT ON COLUMN DT.CURRENT_BDT_ID
IS
  'This is a self-foreign-key. It points from a revised record to the current record. The current record is denoted by the record whose REVISION_NUM is 0. Revised records (a.k.a. history records) and their current record must have the same GUID.

It is noted that although this is a foreign key by definition, we don''t specify a foreign key in the data model. This is because when an entity is deleted the current record won''t exist anymore.

The value of this column for the current record should be left NULL.

The column name is specific to BDT because, the column does not apply to CDT.' ;
  COMMENT ON COLUMN DT.IS_DEPRECATED
IS
  'Indicates whether the CC is deprecated and should not be reused (i.e., no new reference to this record should be created).' ;
CREATE UNIQUE INDEX DT_DT_ID_IDX ON DT
  (
    DT_ID ASC
  )
  ;
  CREATE INDEX DT_PREVIOUS_VERSION_DT_ID_IDX ON DT
    (
      PREVIOUS_VERSION_DT_ID ASC
    ) ;
  CREATE INDEX DT_BASED_DT_ID_IDX ON DT
    ( BASED_DT_ID ASC
    ) ;
  CREATE INDEX DT_MODULE_ID_IDX ON DT
    ( MODULE_ID ASC
    ) ;
  CREATE INDEX DT_CREATED_BY_IDX ON DT
    ( CREATED_BY ASC
    ) ;
  CREATE INDEX DT_OWNER_USER_ID_IDX ON DT
    ( OWNER_USER_ID ASC
    ) ;
  CREATE INDEX DT_LAST_UPDATED_BY_IDX ON DT
    ( LAST_UPDATED_BY ASC
    ) ;
  CREATE INDEX DT_RELEASE_ID_IDX ON DT
    ( RELEASE_ID ASC
    ) ;
  CREATE INDEX DT_CURRENT_BDT_ID_IDX ON DT
    ( CURRENT_BDT_ID ASC
    ) ;
ALTER TABLE DT ADD CONSTRAINT DT_PK PRIMARY KEY ( DT_ID ) ;
ALTER TABLE DT ADD CONSTRAINT DT_UK1 UNIQUE ( GUID ) ;


--  This table represents the supplementary component (SC) of a DT. Revision is
--  not tracked at the supplementary component. It is considered intrinsic part
--  of the DT. In other words, when a new revision of a DT is created a new set
--  of supplementary components is created along with it.
CREATE TABLE DT_SC
  (
    DT_SC_ID            DECIMAL (19) NOT NULL DEFAULT NEXTVAL('DT_SC_ID_SEQ'),
    GUID                VARCHAR (41) NOT NULL ,
    PROPERTY_TERM       VARCHAR (60) ,
    REPRESENTATION_TERM VARCHAR (20) ,
    DEFINITION TEXT ,
    DEFINITION_SOURCE VARCHAR (200) ,
    OWNER_DT_ID       DECIMAL (19) ,
    CARDINALITY_MIN   DECIMAL (10) NOT NULL ,
    CARDINALITY_MAX   DECIMAL (10) ,
    BASED_DT_SC_ID    DECIMAL (19)
  )
  ;
COMMENT ON TABLE DT_SC
IS
  'This table represents the supplementary component (SC) of a DT. Revision is not tracked at the supplementary component. It is considered intrinsic part of the DT. In other words, when a new revision of a DT is created a new set of supplementary components is created along with it. ' ;
  COMMENT ON COLUMN DT_SC.DT_SC_ID
IS
  'Internal, primary database key.' ;
  COMMENT ON COLUMN DT_SC.GUID
IS
  'A globally unique identifier (GUID) of an SC. Per OAGIS, a GUID is of the form "oagis-id-" followed by a 32 Hex character sequence. Note that each SC is considered intrinsic to each DT, so a SC has a different GUID from the based SC, i.e., SC inherited from the based DT has a new, different GUID.' ;
  COMMENT ON COLUMN DT_SC.PROPERTY_TERM
IS
  'Property term of the SC.' ;
  COMMENT ON COLUMN DT_SC.REPRESENTATION_TERM
IS
  'Representation of the supplementary component.' ;
  COMMENT ON COLUMN DT_SC.DEFINITION
IS
  'Description of the supplementary component.' ;
  COMMENT ON COLUMN DT_SC.DEFINITION_SOURCE
IS
  'This is typically a URL identifying the source of the DEFINITION column.' ;
  COMMENT ON COLUMN DT_SC.OWNER_DT_ID
IS
  'Foreigned key to the DT table indicating the data type, to which this supplementary component belongs.' ;
  COMMENT ON COLUMN DT_SC.CARDINALITY_MIN
IS
  'The minimum occurrence constraint associated with the supplementary component. The valid values zero or one.' ;
  COMMENT ON COLUMN DT_SC.CARDINALITY_MAX
IS
  'The maximum occurrence constraint associated with the supplementary component. The valid values are zero or one. Zero is used when the SC is restricted from an instantiation in the data type.' ;
  COMMENT ON COLUMN DT_SC.BASED_DT_SC_ID
IS
  'Foreign key to the DT_SC table itself. This column is used when the SC is derived from the based DT.' ;
CREATE UNIQUE INDEX DT_SC_DT_SC_ID_IDX ON DT_SC
  (
    DT_SC_ID ASC
  )
  ;
  CREATE INDEX DT_SC_OWNER_DT_ID_IDX ON DT_SC
    ( OWNER_DT_ID ASC
    ) ;
  CREATE INDEX DT_SC_BASED_DT_SC_ID_IDX ON DT_SC
    ( BASED_DT_SC_ID ASC
    ) ;
ALTER TABLE DT_SC ADD CONSTRAINT DT_SC_PK PRIMARY KEY ( DT_SC_ID ) ;
ALTER TABLE DT_SC ADD CONSTRAINT DT_SC_UK1 UNIQUE ( GUID ) ;


CREATE TABLE DT_USAGE_RULE
  (
    DT_USAGE_RULE_ID       DECIMAL (19) NOT NULL DEFAULT NEXTVAL('DT_USAGE_RULE_ID_SEQ'),
    ASSIGNED_USAGE_RULE_ID DECIMAL (19) NOT NULL ,
    TARGET_DT_ID           DECIMAL (19) ,
    TARGET_DT_SC_ID        DECIMAL (19)
  )
  ;
COMMENT ON TABLE DT_USAGE_RULE
IS
  'This is an intersection table. Per CCTS, a usage rule may be reused. This table allows m-m relationships between the usage rule and the DT content component and usage rules and DT supplementary component. In a particular record, either a TARGET_DT_ID or TARGET_DT_SC_ID must be present but not both.' ;
  COMMENT ON COLUMN DT_USAGE_RULE.DT_USAGE_RULE_ID
IS
  'Primary key of the table.' ;
  COMMENT ON COLUMN DT_USAGE_RULE.ASSIGNED_USAGE_RULE_ID
IS
  'Foreign key to the USAGE_RULE table indicating the usage rule assigned to the DT content component or DT_SC.' ;
  COMMENT ON COLUMN DT_USAGE_RULE.TARGET_DT_ID
IS
  'Foreing key to the DT_ID for assigning a usage rule to the corresponding DT content component.' ;
  COMMENT ON COLUMN DT_USAGE_RULE.TARGET_DT_SC_ID
IS
  'Foreing key to the DT_SC_ID for assigning a usage rule to the corresponding DT_SC.' ;
CREATE UNIQUE INDEX DUR_DT_USAGE_RULE_ID_IDX ON DT_USAGE_RULE
  (
    DT_USAGE_RULE_ID ASC
  )
  ;
  CREATE INDEX DUR_TARGET_DT_ID_IDX ON DT_USAGE_RULE
    (
      TARGET_DT_ID ASC
    )
    ;
  CREATE INDEX DUR_TARGET_DT_SC_ID_IDX ON DT_USAGE_RULE
    (
      TARGET_DT_SC_ID ASC
    )
    ;
  CREATE INDEX DUR_ASSIGNED_USAGE_RULE_ID_IDX ON DT_USAGE_RULE
    (
      ASSIGNED_USAGE_RULE_ID ASC
    )
    ;
ALTER TABLE DT_USAGE_RULE ADD CONSTRAINT DT_USAGE_RULE_PK PRIMARY KEY ( DT_USAGE_RULE_ID ) ;


--  The module table stores information about a physical file, into which CC
--  components will be generated during the expression generation.
CREATE TABLE MODULE
  (
    MODULE_ID    DECIMAL (19) NOT NULL DEFAULT NEXTVAL('MODULE_ID_SEQ'),
    MODULE       VARCHAR (100) NOT NULL ,
    RELEASE_ID   DECIMAL (19) NOT NULL ,
    NAMESPACE_ID DECIMAL (19) NOT NULL ,
    VERSION_NUM  VARCHAR (45)
  )
  ;
COMMENT ON TABLE MODULE
IS
  'The module table stores information about a physical file, into which CC components will be generated during the expression generation.' ;
  COMMENT ON COLUMN MODULE.MODULE_ID
IS
  'Primary, internal database key.' ;
  COMMENT ON COLUMN MODULE.MODULE
IS
  'The is the subdirectory and filename. The format is Windows file path. The starting directory typically is the root folder of all the release content. For example, for OAGIS 10.1 Model, the root directory is Model. If the file shall be directly under the Model directory, then this column should be ''Model\filename'' without the extension. If the file is under, say, Model\Platform\2_1\Common\Components directory, then the value of this column shall be ''Model\Platform\2_1\Common\Components\filenam''. The reason to not including the extension is that the extension maybe dependent on the expression. For XML schema, ''.xsd'' maybe added; or for JSON, ''.json'' maybe added as the file extension.' ;
  COMMENT ON COLUMN MODULE.RELEASE_ID
IS
  'Foreign key to the RELEASE table. It identifies the release, for which this module is associated.' ;
  COMMENT ON COLUMN MODULE.NAMESPACE_ID
IS
  'Note that a release record has a namespace associated. The NAMESPACE_ID, if specified here, overrides the release''s namespace. However, the NAMESPACE_ID associated with the component takes the highest precedence.' ;
  COMMENT ON COLUMN MODULE.VERSION_NUM
IS
  'This is the version number to be assigned to the schema module.' ;
CREATE UNIQUE INDEX MODULE_MODULE_ID_IDX ON MODULE
  (
    MODULE_ID ASC
  )
  ;
  CREATE INDEX MODULE_RELEASE_ID_IDX ON MODULE
    ( RELEASE_ID ASC
    ) ;
  CREATE INDEX MODULE_NAMESPACE_ID_IDX ON MODULE
    ( NAMESPACE_ID ASC
    ) ;
ALTER TABLE MODULE ADD CONSTRAINT MODULE_PK PRIMARY KEY ( MODULE_ID ) ;


--  This table carries the dependency between modules in the MODULE table.
CREATE TABLE MODULE_DEP
  (
    MODULE_DEP_ID       DECIMAL (19) NOT NULL DEFAULT NEXTVAL('MODULE_DEP_ID_SEQ'),
    DEPENDENCY_TYPE     DECIMAL (10) NOT NULL ,
    DEPENDING_MODULE_ID DECIMAL (19) NOT NULL ,
    DEPENDED_MODULE_ID  DECIMAL (19) NOT NULL
  )
  ;
COMMENT ON TABLE MODULE_DEP
IS
  'This table carries the dependency between modules in the MODULE table.' ;
  COMMENT ON COLUMN MODULE_DEP.MODULE_DEP_ID
IS
  'Primary, internal database key.' ;
  COMMENT ON COLUMN MODULE_DEP.DEPENDENCY_TYPE
IS
  'This is a code list. The value tells the expression generator what to do based on this dependency type. 0 = xsd:include, 1 = xsd:import. There could be other values supporting other expressions/syntaxes.' ;
  COMMENT ON COLUMN MODULE_DEP.DEPENDING_MODULE_ID
IS
  'Foreign key to the MODULE table. It identifies a depending module. For example, in XML schema if module A imports or includes module B, then module A is a depending module.' ;
  COMMENT ON COLUMN MODULE_DEP.DEPENDED_MODULE_ID
IS
  'Foreign key to the MODULE table. It identifies a depended module counterpart of the depending module. For example, in XML schema if module A imports or includes module B, then module B is a depended module.' ;
  CREATE INDEX MODULE_DEP_DEPENDING_ID_IDX ON MODULE_DEP
    (
      DEPENDING_MODULE_ID ASC
    )
    ;
  CREATE INDEX MODULE_DEP_DEPENDED_ID_IDX ON MODULE_DEP
    (
      DEPENDED_MODULE_ID ASC
    )
    ;
CREATE UNIQUE INDEX MODULE_DEP_MODULE_DEP_ID_IDX ON MODULE_DEP
  (
    MODULE_DEP_ID ASC
  )
  ;
ALTER TABLE MODULE_DEP ADD CONSTRAINT MODULE_DEP_PK PRIMARY KEY ( MODULE_DEP_ID ) ;


--  This table stores information about a namespace. Namespace is the namespace
--  as in the XML schema specification.
CREATE TABLE NAMESPACE
  (
    NAMESPACE_ID DECIMAL (19) NOT NULL DEFAULT NEXTVAL('NAMESPACE_ID_SEQ'),
    URI          VARCHAR (100) NOT NULL ,
    PREFIX       VARCHAR (45) ,
    DESCRIPTION TEXT ,
    IS_STD_NMSP           BOOLEAN NOT NULL ,
    OWNER_USER_ID         DECIMAL (19) NOT NULL ,
    CREATED_BY            DECIMAL (19) NOT NULL ,
    LAST_UPDATED_BY       DECIMAL (19) NOT NULL ,
    CREATION_TIMESTAMP    TIMESTAMP WITH TIME ZONE NOT NULL ,
    LAST_UPDATE_TIMESTAMP TIMESTAMP WITH TIME ZONE NOT NULL
  )
  ;
COMMENT ON TABLE NAMESPACE
IS
  'This table stores information about a namespace. Namespace is the namespace as in the XML schema specification.' ;
  COMMENT ON COLUMN NAMESPACE.NAMESPACE_ID
IS
  'Primary, internal database key.' ;
  COMMENT ON COLUMN NAMESPACE.URI
IS
  'This is the URI of the namespace.' ;
  COMMENT ON COLUMN NAMESPACE.PREFIX
IS
  'This is a default short name to represent the URI. It may be overridden during the expression generation. Null or empty means the same thing like the default prefix in an XML schema.' ;
  COMMENT ON COLUMN NAMESPACE.DESCRIPTION
IS
  'Description or explanation about the namespace or use of the namespace.' ;
  COMMENT ON COLUMN NAMESPACE.IS_STD_NMSP
IS
  'This indicates whether the namespace is reserved for standard used (i.e., whether it is an OAGIS namespace). If it is true, then end users cannot user the namespace for the end user CCs.' ;
  COMMENT ON COLUMN NAMESPACE.OWNER_USER_ID
IS
  'Foreign key to the APP_USER table identifying the user who can update or delete the record.' ;
  COMMENT ON COLUMN NAMESPACE.CREATED_BY
IS
  'Foreign key to the APP_USER table identifying user who created the namespace.' ;
  COMMENT ON COLUMN NAMESPACE.LAST_UPDATED_BY
IS
  'Foreign key to the APP_USER table identifying the user who last updated the record.' ;
  COMMENT ON COLUMN NAMESPACE.CREATION_TIMESTAMP
IS
  'The timestamp when the record was first created.' ;
  COMMENT ON COLUMN NAMESPACE.LAST_UPDATE_TIMESTAMP
IS
  'The timestamp when the record was last updated.' ;
CREATE UNIQUE INDEX NAMESPACE_NAMESPACE_ID_IDX ON NAMESPACE
  (
    NAMESPACE_ID ASC
  )
  ;
  CREATE INDEX NAMESPACE_OWNER_USER_ID_IDX ON NAMESPACE
    ( OWNER_USER_ID ASC
    ) ;
  CREATE INDEX NAMESPACE_CREATED_BY_IDX ON NAMESPACE
    ( CREATED_BY ASC
    ) ;
  CREATE INDEX NAMESPACE_LAST_UPDATED_BY_IDX ON NAMESPACE
    (
      LAST_UPDATED_BY ASC
    ) ;
ALTER TABLE NAMESPACE ADD CONSTRAINT NAMESPACE_PK PRIMARY KEY ( NAMESPACE_ID ) ;


--  The is table store the release information.
CREATE TABLE RELEASE
  (
    RELEASE_ID  DECIMAL (19) NOT NULL DEFAULT NEXTVAL('RELEASE_ID_SEQ'),
    RELEASE_NUM VARCHAR (45) ,
    RELEASE_NOTE TEXT ,
    NAMESPACE_ID DECIMAL (19) NOT NULL
  )
  ;
COMMENT ON TABLE RELEASE
IS
  'The is table store the release information.' ;
  COMMENT ON COLUMN RELEASE.RELEASE_ID
IS
  'RELEASE_ID must be an incremental integer. RELEASE_ID that is more than another RELEASE_ID is interpreted to be released later than the other.' ;
  COMMENT ON COLUMN RELEASE.RELEASE_NUM
IS
  'Release number such has 10.0, 10.1, etc. ' ;
  COMMENT ON COLUMN RELEASE.RELEASE_NOTE
IS
  'Description or note associated with the release.' ;
  COMMENT ON COLUMN RELEASE.NAMESPACE_ID
IS
  'Foreign key to the NAMESPACE table. It identifies the namespace used with the release. It is particularly useful for a library that uses a single namespace such like the OAGIS 10.x. A library that uses multiple namespace but has a main namespace may also use this column as a specific namespace can be override at the module level.' ;
CREATE UNIQUE INDEX RELEASE_RELEASE_ID_IDX ON RELEASE
  (
    RELEASE_ID ASC
  )
  ;
  CREATE INDEX RELEASE_NAMESPACE_ID_IDX ON RELEASE
    ( NAMESPACE_ID ASC
    ) ;
ALTER TABLE RELEASE ADD CONSTRAINT RELEASE_PK PRIMARY KEY ( RELEASE_ID ) ;


--  This table indexes the ABIE which is a top-level ABIE. This table and the
--  owner_top_level_abie_id column in all BIE tables allow all related BIEs to
--  be retrieved all at once speeding up the profile BOD transactions.
CREATE TABLE TOP_LEVEL_ABIE
  (
    TOP_LEVEL_ABIE_ID DECIMAL (19) NOT NULL DEFAULT NEXTVAL('TOP_LEVEL_ABIE_ID_SEQ'),
    ABIE_ID           DECIMAL (19) ,
    OWNER_USER_ID     DECIMAL (19) NOT NULL ,
    STATE             DECIMAL (10)
  )
  ;
COMMENT ON TABLE TOP_LEVEL_ABIE
IS
  'This table indexes the ABIE which is a top-level ABIE. This table and the owner_top_level_abie_id column in all BIE tables allow all related BIEs to be retrieved all at once speeding up the profile BOD transactions.' ;
  COMMENT ON COLUMN TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID
IS
  'Primary, internal database key.' ;
  COMMENT ON COLUMN TOP_LEVEL_ABIE.ABIE_ID
IS
  'Foreign key to the ABIE table pointing to a record which is a top-level ABIE.' ;
CREATE UNIQUE INDEX TOP_LEVEL_ABIE_PK_IDX ON TOP_LEVEL_ABIE
  (
    TOP_LEVEL_ABIE_ID ASC
  )
  ;
  CREATE INDEX TOP_LEVEL_ABIE_APP_USER_FK ON TOP_LEVEL_ABIE
    (
      OWNER_USER_ID ASC
    )
    ;
  CREATE INDEX TOP_LEVEL_ABIE_ABIE_ID_IDX ON TOP_LEVEL_ABIE
    ( ABIE_ID ASC
    ) ;
ALTER TABLE TOP_LEVEL_ABIE ADD CONSTRAINT TOP_LEVEL_ABIE_PK PRIMARY KEY ( TOP_LEVEL_ABIE_ID ) ;


CREATE TABLE USAGE_RULE
  (
    USAGE_RULE_ID  DECIMAL (19) NOT NULL DEFAULT NEXTVAL('USAGE_RULE_ID_SEQ'),
    NAME           VARCHAR (4000) ,
    CONDITION_TYPE DECIMAL (5) NOT NULL
  )
  ;
COMMENT ON TABLE USAGE_RULE
IS
  'This table captures a usage rule information. A usage rule may be expressed in multiple expressions. Each expression is captured in the USAGE_RULE_EXPRESSION table. To capture a description of a usage rule, create a usage rule expression with the unstructured constraint type.' ;
  COMMENT ON COLUMN USAGE_RULE.USAGE_RULE_ID
IS
  'Primary key of the usage rule.' ;
  COMMENT ON COLUMN USAGE_RULE.NAME
IS
  'Short nmenomic name of the usage rule.' ;
  COMMENT ON COLUMN USAGE_RULE.CONDITION_TYPE
IS
  'Condition type according to the CC specification. It is a value list column. 0 = pre-condition, 1 = post-condition, 2 = invariant.' ;
CREATE UNIQUE INDEX USAGE_RULE_USAGE_RULE_ID_IDX ON USAGE_RULE
  (
    USAGE_RULE_ID ASC
  )
  ;
ALTER TABLE USAGE_RULE ADD CONSTRAINT USAGE_RULE_PK PRIMARY KEY ( USAGE_RULE_ID ) ;


CREATE TABLE USAGE_RULE_EXPRESSION
  (
    USAGE_RULE_EXPRESSION_ID DECIMAL (19) NOT NULL DEFAULT NEXTVAL('USAGE_RULE_EXPRESSION_ID_SEQ'),
    CONSTRAINT_TYPE          DECIMAL (5) NOT NULL ,
    CONSTRAINT_TEXT TEXT NOT NULL ,
    REPRESENTED_USAGE_RULE_ID DECIMAL (19) NOT NULL
  )
  ;
COMMENT ON TABLE USAGE_RULE_EXPRESSION
IS
  'The USAGE_RULE_EXPRESSION provides a representation of a usage rule in a particular syntax indicated by the CONSTRAINT_TYPE column. One of the syntaxes can be unstructured, which works a description of the usage rule.' ;
  COMMENT ON COLUMN USAGE_RULE_EXPRESSION.USAGE_RULE_EXPRESSION_ID
IS
  'Primary key of the usage rule expression' ;
  COMMENT ON COLUMN USAGE_RULE_EXPRESSION.CONSTRAINT_TYPE
IS
  'Constraint type according to the CC spec. It represents the expression language (syntax) used in the CONSTRAINT column. It is a value list column. 0 = ''Unstructured'' which is basically a description of the rule, 1 = ''Schematron''.' ;
  COMMENT ON COLUMN USAGE_RULE_EXPRESSION.CONSTRAINT_TEXT
IS
  'This column capture the constraint expressing the usage rule. In other words, this is the expression.' ;
  COMMENT ON COLUMN USAGE_RULE_EXPRESSION.REPRESENTED_USAGE_RULE_ID
IS
  'The usage rule which the expression represents' ;
CREATE UNIQUE INDEX URE_URXPS_ID_IDX ON USAGE_RULE_EXPRESSION
  (
    USAGE_RULE_EXPRESSION_ID ASC
  )
  ;
  CREATE INDEX URE_REP_USAGE_RULE_ID_IDX ON USAGE_RULE_EXPRESSION
    (
      REPRESENTED_USAGE_RULE_ID ASC
    )
    ;
ALTER TABLE USAGE_RULE_EXPRESSION ADD CONSTRAINT USAGE_RULE_EXPRESSION_PK PRIMARY KEY ( USAGE_RULE_EXPRESSION_ID ) ;


--  This table stores XML schema built-in types and OAGIS built-in types. OAGIS
--  built-in types are those types defined in the XMLSchemaBuiltinType and the
--  XMLSchemaBuiltinType Patterns schemas.
CREATE TABLE XBT
  (
    XBT_ID            DECIMAL (19) NOT NULL DEFAULT NEXTVAL('XBT_ID_SEQ'),
    NAME              VARCHAR (45) ,
    BUILTIN_TYPE      VARCHAR (45) ,
    SUBTYPE_OF_XBT_ID DECIMAL (19) ,
    SCHEMA_DEFINITION TEXT ,
    MODULE_ID  DECIMAL (19) ,
    RELEASE_ID DECIMAL (19) ,
    REVISION_DOC TEXT ,
    STATE                 DECIMAL (10) ,
    CREATED_BY            DECIMAL (19) NOT NULL ,
    OWNER_USER_ID         DECIMAL (19) NOT NULL ,
    LAST_UPDATED_BY       DECIMAL (19) NOT NULL ,
    CREATION_TIMESTAMP    TIMESTAMP WITH TIME ZONE NOT NULL ,
    LAST_UPDATE_TIMESTAMP TIMESTAMP WITH TIME ZONE NOT NULL ,
    REVISION_NUM          DECIMAL (10) NOT NULL ,
    REVISION_TRACKING_NUM DECIMAL (10) NOT NULL ,
    REVISION_ACTION       DECIMAL (3) ,
    CURRENT_XBT_ID        DECIMAL (19) ,
    IS_DEPRECATED         BOOLEAN NOT NULL
  )
  ;
COMMENT ON TABLE XBT
IS
  'This table stores XML schema built-in types and OAGIS built-in types. OAGIS built-in types are those types defined in the XMLSchemaBuiltinType and the XMLSchemaBuiltinType Patterns schemas.' ;
  COMMENT ON COLUMN XBT.XBT_ID
IS
  'Primary, internal database key.' ;
  COMMENT ON COLUMN XBT.NAME
IS
  'Human understandable name of the built-in type.' ;
  COMMENT ON COLUMN XBT.BUILTIN_TYPE
IS
  'Built-in type as it should appear in the XML schema including the namespace prefix. Namespace prefix for the XML schema namespace is assumed to be ''xsd'' and a default prefix for the OAGIS built-int type.' ;
  COMMENT ON COLUMN XBT.SUBTYPE_OF_XBT_ID
IS
  'Foreign key to the XBT table itself. It indicates a super type of this XSD built-in type.' ;
CREATE UNIQUE INDEX XBT_XBT_ID_IDX ON XBT
  (
    XBT_ID ASC
  )
  ;
  CREATE INDEX XBT_SUBTYPE_OF_XBT_ID_IDX ON XBT
    ( SUBTYPE_OF_XBT_ID ASC
    ) ;
  CREATE INDEX XBT_MODULE_ID_IDX ON XBT
    ( MODULE_ID ASC
    ) ;
  CREATE INDEX XBT_RELEASE_ID_IDX ON XBT
    ( RELEASE_ID ASC
    ) ;
  CREATE INDEX XBT_CREATED_BY_IDX ON XBT
    ( CREATED_BY ASC
    ) ;
  CREATE INDEX XBT_OWNER_USER_ID_IDX ON XBT
    ( OWNER_USER_ID ASC
    ) ;
  CREATE INDEX XBT_LAST_UPDATED_BY_IDX ON XBT
    ( LAST_UPDATED_BY ASC
    ) ;
ALTER TABLE XBT ADD CONSTRAINT XBT_PK PRIMARY KEY ( XBT_ID ) ;
ALTER TABLE XBT ADD CONSTRAINT XBT_UK1 UNIQUE ( NAME ) ;
ALTER TABLE XBT ADD CONSTRAINT XBT_UK2 UNIQUE ( BUILTIN_TYPE ) ;


ALTER TABLE ABIE ADD CONSTRAINT ABIE_BASED_ACC_ID_FK FOREIGN KEY ( BASED_ACC_ID ) REFERENCES ACC ( ACC_ID ) NOT DEFERRABLE ;

ALTER TABLE ABIE ADD CONSTRAINT ABIE_BIZ_CTX_ID_FK FOREIGN KEY ( BIZ_CTX_ID ) REFERENCES BIZ_CTX ( BIZ_CTX_ID ) NOT DEFERRABLE ;

ALTER TABLE ABIE ADD CONSTRAINT ABIE_CLIENT_ID_FK FOREIGN KEY ( CLIENT_ID ) REFERENCES CLIENT ( CLIENT_ID ) NOT DEFERRABLE ;

ALTER TABLE ABIE ADD CONSTRAINT ABIE_CREATED_BY_FK FOREIGN KEY ( CREATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE ABIE ADD CONSTRAINT ABIE_LAST_UPDATED_BY_FK FOREIGN KEY ( LAST_UPDATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE ABIE ADD CONSTRAINT ABIE_TOP_LEVEL_ABIE_ID_FK FOREIGN KEY ( OWNER_TOP_LEVEL_ABIE_ID ) REFERENCES TOP_LEVEL_ABIE ( TOP_LEVEL_ABIE_ID ) NOT DEFERRABLE ;

ALTER TABLE ACC ADD CONSTRAINT ACC_BASED_ACC_ID_FK FOREIGN KEY ( BASED_ACC_ID ) REFERENCES ACC ( ACC_ID ) NOT DEFERRABLE ;

ALTER TABLE ACC ADD CONSTRAINT ACC_CREATED_BY_FK FOREIGN KEY ( CREATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE ACC ADD CONSTRAINT ACC_CURRENT_ACC_ID_FK FOREIGN KEY ( CURRENT_ACC_ID ) REFERENCES ACC ( ACC_ID ) NOT DEFERRABLE ;

ALTER TABLE ACC ADD CONSTRAINT ACC_LAST_UPDATED_BY_FK FOREIGN KEY ( LAST_UPDATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE ACC ADD CONSTRAINT ACC_MODULE_ID_FK FOREIGN KEY ( MODULE_ID ) REFERENCES MODULE ( MODULE_ID ) NOT DEFERRABLE ;

ALTER TABLE ACC ADD CONSTRAINT ACC_NAMESPACE_ID_FK FOREIGN KEY ( NAMESPACE_ID ) REFERENCES NAMESPACE ( NAMESPACE_ID ) NOT DEFERRABLE ;

ALTER TABLE ACC ADD CONSTRAINT ACC_OWNER_USER_ID_FK FOREIGN KEY ( OWNER_USER_ID ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE ACC ADD CONSTRAINT ACC_RELEASE_ID_FK FOREIGN KEY ( RELEASE_ID ) REFERENCES RELEASE ( RELEASE_ID ) NOT DEFERRABLE ;

ALTER TABLE AGENCY_ID_LIST ADD CONSTRAINT AGENCY_ID_LIST_AILV_FK FOREIGN KEY ( AGENCY_ID_LIST_VALUE_ID ) REFERENCES AGENCY_ID_LIST_VALUE ( AGENCY_ID_LIST_VALUE_ID ) NOT DEFERRABLE ;

ALTER TABLE AGENCY_ID_LIST ADD CONSTRAINT AGENCY_ID_LIST_MODULE_ID_FK FOREIGN KEY ( MODULE_ID ) REFERENCES MODULE ( MODULE_ID ) NOT DEFERRABLE ;

ALTER TABLE AGENCY_ID_LIST_VALUE ADD CONSTRAINT AILV_AGENCY_ID_LIST_FK FOREIGN KEY ( OWNER_LIST_ID ) REFERENCES AGENCY_ID_LIST ( AGENCY_ID_LIST_ID ) NOT DEFERRABLE ;

ALTER TABLE ASBIEP ADD CONSTRAINT ASBIEP_BASED_ASCCP_ID_FK FOREIGN KEY ( BASED_ASCCP_ID ) REFERENCES ASCCP ( ASCCP_ID ) NOT DEFERRABLE ;

ALTER TABLE ASBIEP ADD CONSTRAINT ASBIEP_CREATED_BY_FK FOREIGN KEY ( CREATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE ASBIEP ADD CONSTRAINT ASBIEP_LAST_UPDATED_BY_FK FOREIGN KEY ( LAST_UPDATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE ASBIEP ADD CONSTRAINT ASBIEP_ROLE_OF_ABIE_ID FOREIGN KEY ( ROLE_OF_ABIE_ID ) REFERENCES ABIE ( ABIE_ID ) NOT DEFERRABLE ;

ALTER TABLE ASBIEP ADD CONSTRAINT ASBIEP_TOP_LEVEL_ABIE_ID_FK FOREIGN KEY ( OWNER_TOP_LEVEL_ABIE_ID ) REFERENCES TOP_LEVEL_ABIE ( TOP_LEVEL_ABIE_ID ) NOT DEFERRABLE ;

ALTER TABLE ASBIE ADD CONSTRAINT ASBIE_BASED_ASCC_ID_FK FOREIGN KEY ( BASED_ASCC_ID ) REFERENCES ASCC ( ASCC_ID ) NOT DEFERRABLE ;

ALTER TABLE ASBIE ADD CONSTRAINT ASBIE_CREATED_BY_FK FOREIGN KEY ( CREATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE ASBIE ADD CONSTRAINT ASBIE_FROM_ABIE_ID FOREIGN KEY ( FROM_ABIE_ID ) REFERENCES ABIE ( ABIE_ID ) NOT DEFERRABLE ;

ALTER TABLE ASBIE ADD CONSTRAINT ASBIE_LAST_UPDATED_BY_FK FOREIGN KEY ( LAST_UPDATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE ASBIE ADD CONSTRAINT ASBIE_TOP_LEVEL_ABIE_ID_FK FOREIGN KEY ( OWNER_TOP_LEVEL_ABIE_ID ) REFERENCES TOP_LEVEL_ABIE ( TOP_LEVEL_ABIE_ID ) NOT DEFERRABLE ;

ALTER TABLE ASBIE ADD CONSTRAINT ASBIE_TO_ASBIEP_ID_FK FOREIGN KEY ( TO_ASBIEP_ID ) REFERENCES ASBIEP ( ASBIEP_ID ) NOT DEFERRABLE ;

ALTER TABLE ASCCP ADD CONSTRAINT ASCCP_CREATED_BY_FK FOREIGN KEY ( CREATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE ASCCP ADD CONSTRAINT ASCCP_CURRENT_ASCCP_ID_FK FOREIGN KEY ( CURRENT_ASCCP_ID ) REFERENCES ASCCP ( ASCCP_ID ) NOT DEFERRABLE ;

ALTER TABLE ASCCP ADD CONSTRAINT ASCCP_LAST_UPDATED_BY_FK FOREIGN KEY ( LAST_UPDATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE ASCCP ADD CONSTRAINT ASCCP_MODULE_ID_FK FOREIGN KEY ( MODULE_ID ) REFERENCES MODULE ( MODULE_ID ) NOT DEFERRABLE ;

ALTER TABLE ASCCP ADD CONSTRAINT ASCCP_NAMESPACE_ID_FK FOREIGN KEY ( NAMESPACE_ID ) REFERENCES NAMESPACE ( NAMESPACE_ID ) NOT DEFERRABLE ;

ALTER TABLE ASCCP ADD CONSTRAINT ASCCP_OWNER_USER_ID_FK FOREIGN KEY ( OWNER_USER_ID ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE ASCCP ADD CONSTRAINT ASCCP_RELEASE_ID_FK FOREIGN KEY ( RELEASE_ID ) REFERENCES RELEASE ( RELEASE_ID ) NOT DEFERRABLE ;

ALTER TABLE ASCCP ADD CONSTRAINT ASCCP_ROLE_OF_ACC_ID_FK FOREIGN KEY ( ROLE_OF_ACC_ID ) REFERENCES ACC ( ACC_ID ) NOT DEFERRABLE ;

ALTER TABLE ASCC ADD CONSTRAINT ASCC_CREATED_BY_FK FOREIGN KEY ( CREATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE ASCC ADD CONSTRAINT ASCC_CURRENT_ASCC_ID_FK FOREIGN KEY ( CURRENT_ASCC_ID ) REFERENCES ASCC ( ASCC_ID ) NOT DEFERRABLE ;

ALTER TABLE ASCC ADD CONSTRAINT ASCC_FROM_ACC_ID_FK FOREIGN KEY ( FROM_ACC_ID ) REFERENCES ACC ( ACC_ID ) NOT DEFERRABLE ;

ALTER TABLE ASCC ADD CONSTRAINT ASCC_LAST_UPDATED_BY_FK FOREIGN KEY ( LAST_UPDATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE ASCC ADD CONSTRAINT ASCC_OWNER_USER_ID_FK FOREIGN KEY ( OWNER_USER_ID ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE ASCC ADD CONSTRAINT ASCC_RELEASE_ID_FK FOREIGN KEY ( RELEASE_ID ) REFERENCES RELEASE ( RELEASE_ID ) NOT DEFERRABLE ;

ALTER TABLE ASCC ADD CONSTRAINT ASCC_TO_ASCCP_ID_FK FOREIGN KEY ( TO_ASCCP_ID ) REFERENCES ASCCP ( ASCCP_ID ) NOT DEFERRABLE ;

ALTER TABLE BBIEP ADD CONSTRAINT BBIEP_BASED_BCCP_ID_FK FOREIGN KEY ( BASED_BCCP_ID ) REFERENCES BCCP ( BCCP_ID ) NOT DEFERRABLE ;

ALTER TABLE BBIEP ADD CONSTRAINT BBIEP_CREATED_BY_FK FOREIGN KEY ( CREATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE BBIEP ADD CONSTRAINT BBIEP_LAST_UPDATED_BY_FK FOREIGN KEY ( LAST_UPDATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE BBIEP ADD CONSTRAINT BBIEP_TOP_LEVEL_ABIE_ID_FK FOREIGN KEY ( OWNER_TOP_LEVEL_ABIE_ID ) REFERENCES TOP_LEVEL_ABIE ( TOP_LEVEL_ABIE_ID ) NOT DEFERRABLE ;

ALTER TABLE BBIE ADD CONSTRAINT BBIE_AGENCY_ID_LIST_ID_FK FOREIGN KEY ( AGENCY_ID_LIST_ID ) REFERENCES AGENCY_ID_LIST ( AGENCY_ID_LIST_ID ) NOT DEFERRABLE ;

ALTER TABLE BBIE ADD CONSTRAINT BBIE_BASED_BCC_ID_FK FOREIGN KEY ( BASED_BCC_ID ) REFERENCES BCC ( BCC_ID ) NOT DEFERRABLE ;

ALTER TABLE BBIE ADD CONSTRAINT BBIE_BDT_PRI_RESTRI_ID_FK FOREIGN KEY ( BDT_PRI_RESTRI_ID ) REFERENCES BDT_PRI_RESTRI ( BDT_PRI_RESTRI_ID ) NOT DEFERRABLE ;

ALTER TABLE BBIE ADD CONSTRAINT BBIE_CODE_LIST_ID_FK FOREIGN KEY ( CODE_LIST_ID ) REFERENCES CODE_LIST ( CODE_LIST_ID ) NOT DEFERRABLE ;

ALTER TABLE BBIE ADD CONSTRAINT BBIE_CREATED_BY_FK FOREIGN KEY ( CREATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE BBIE ADD CONSTRAINT BBIE_FROM_ABIE_ID_FK FOREIGN KEY ( FROM_ABIE_ID ) REFERENCES ABIE ( ABIE_ID ) NOT DEFERRABLE ;

ALTER TABLE BBIE ADD CONSTRAINT BBIE_LAST_UPDATED_BY_FK FOREIGN KEY ( LAST_UPDATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE BBIE_SC ADD CONSTRAINT BBIE_SC_AGENCY_ID_LIST_ID_FK FOREIGN KEY ( AGENCY_ID_LIST_ID ) REFERENCES AGENCY_ID_LIST ( AGENCY_ID_LIST_ID ) NOT DEFERRABLE ;

ALTER TABLE BBIE_SC ADD CONSTRAINT BBIE_SC_BBIE_ID_FK FOREIGN KEY ( BBIE_ID ) REFERENCES BBIE ( BBIE_ID ) NOT DEFERRABLE ;

ALTER TABLE BBIE_SC ADD CONSTRAINT BBIE_SC_CODE_LIST_ID_FK FOREIGN KEY ( CODE_LIST_ID ) REFERENCES CODE_LIST ( CODE_LIST_ID ) NOT DEFERRABLE ;

ALTER TABLE BBIE_SC ADD CONSTRAINT BBIE_SC_DT_SC_ID_FK FOREIGN KEY ( DT_SC_ID ) REFERENCES DT_SC ( DT_SC_ID ) NOT DEFERRABLE ;

ALTER TABLE BBIE_SC ADD CONSTRAINT BBIE_SC_DT_SC_PRI_RESTRI_ID_FK FOREIGN KEY ( DT_SC_PRI_RESTRI_ID ) REFERENCES BDT_SC_PRI_RESTRI ( BDT_SC_PRI_RESTRI_ID ) NOT DEFERRABLE ;

ALTER TABLE BBIE_SC ADD CONSTRAINT BBIE_SC_TOP_LEVEL_ABIE_ID_FK FOREIGN KEY ( OWNER_TOP_LEVEL_ABIE_ID ) REFERENCES TOP_LEVEL_ABIE ( TOP_LEVEL_ABIE_ID ) NOT DEFERRABLE ;

ALTER TABLE BBIE ADD CONSTRAINT BBIE_TOP_LEVEL_ABIE_ID_FK FOREIGN KEY ( OWNER_TOP_LEVEL_ABIE_ID ) REFERENCES TOP_LEVEL_ABIE ( TOP_LEVEL_ABIE_ID ) NOT DEFERRABLE ;

ALTER TABLE BBIE ADD CONSTRAINT BBIE_TO_BBIEP_ID_FK FOREIGN KEY ( TO_BBIEP_ID ) REFERENCES BBIEP ( BBIEP_ID ) NOT DEFERRABLE ;

ALTER TABLE BCCP ADD CONSTRAINT BCCP_BDT_ID_FK FOREIGN KEY ( BDT_ID ) REFERENCES DT ( DT_ID ) NOT DEFERRABLE ;

ALTER TABLE BCCP ADD CONSTRAINT BCCP_CREATED_BY_FK FOREIGN KEY ( CREATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE BCCP ADD CONSTRAINT BCCP_CURRENT_BCCP_ID_FK FOREIGN KEY ( CURRENT_BCCP_ID ) REFERENCES BCCP ( BCCP_ID ) NOT DEFERRABLE ;

ALTER TABLE BCCP ADD CONSTRAINT BCCP_LAST_UPDATED_BY_FK FOREIGN KEY ( LAST_UPDATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE BCCP ADD CONSTRAINT BCCP_MODULE_ID_FK FOREIGN KEY ( MODULE_ID ) REFERENCES MODULE ( MODULE_ID ) NOT DEFERRABLE ;

ALTER TABLE BCCP ADD CONSTRAINT BCCP_NAMESPACE_ID_FK FOREIGN KEY ( NAMESPACE_ID ) REFERENCES NAMESPACE ( NAMESPACE_ID ) NOT DEFERRABLE ;

ALTER TABLE BCCP ADD CONSTRAINT BCCP_OWNER_USER_ID_FK FOREIGN KEY ( OWNER_USER_ID ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE BCCP ADD CONSTRAINT BCCP_RELEASE_ID_FK FOREIGN KEY ( RELEASE_ID ) REFERENCES RELEASE ( RELEASE_ID ) NOT DEFERRABLE ;

ALTER TABLE BCC ADD CONSTRAINT BCC_CREATED_BY_FK FOREIGN KEY ( CREATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE BCC ADD CONSTRAINT BCC_CURRENT_BCC_ID_FK FOREIGN KEY ( CURRENT_BCC_ID ) REFERENCES BCC ( BCC_ID ) NOT DEFERRABLE ;

ALTER TABLE BCC ADD CONSTRAINT BCC_FROM_ACC_ID_FK FOREIGN KEY ( FROM_ACC_ID ) REFERENCES ACC ( ACC_ID ) NOT DEFERRABLE ;

ALTER TABLE BCC ADD CONSTRAINT BCC_LAST_UPDATED_BY_FK FOREIGN KEY ( LAST_UPDATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE BCC ADD CONSTRAINT BCC_OWNER_USER_ID_FK FOREIGN KEY ( OWNER_USER_ID ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE BCC ADD CONSTRAINT BCC_RELEASE_ID_FK FOREIGN KEY ( RELEASE_ID ) REFERENCES RELEASE ( RELEASE_ID ) NOT DEFERRABLE ;

ALTER TABLE BCC ADD CONSTRAINT BCC_TO_BCCP_ID_FK FOREIGN KEY ( TO_BCCP_ID ) REFERENCES BCCP ( BCCP_ID ) NOT DEFERRABLE ;

ALTER TABLE BIZ_CTX ADD CONSTRAINT BIZ_CTX_CREATED_BY_FK FOREIGN KEY ( CREATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE BIZ_CTX ADD CONSTRAINT BIZ_CTX_LAST_UPDATED_BY_FK FOREIGN KEY ( LAST_UPDATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE BIZ_CTX_VALUE ADD CONSTRAINT BIZ_CTX_VALUE_BIZ_CTX_ID_FK FOREIGN KEY ( BIZ_CTX_ID ) REFERENCES BIZ_CTX ( BIZ_CTX_ID ) NOT DEFERRABLE ;

ALTER TABLE BIZ_CTX_VALUE ADD CONSTRAINT BIZ_CTX_VALUE_CSV_ID_FK FOREIGN KEY ( CTX_SCHEME_VALUE_ID ) REFERENCES CTX_SCHEME_VALUE ( CTX_SCHEME_VALUE_ID ) NOT DEFERRABLE ;

ALTER TABLE BLOB_CONTENT ADD CONSTRAINT BLOB_CONTENT_MODULE_ID_FK FOREIGN KEY ( MODULE_ID ) REFERENCES MODULE ( MODULE_ID ) NOT DEFERRABLE ;

ALTER TABLE BLOB_CONTENT ADD CONSTRAINT BLOB_CONTENT_RELEASE_ID_FK FOREIGN KEY ( RELEASE_ID ) REFERENCES RELEASE ( RELEASE_ID ) NOT DEFERRABLE ;

ALTER TABLE BDT_PRI_RESTRI ADD CONSTRAINT BPR_AGENCY_ID_LIST_ID_FK FOREIGN KEY ( AGENCY_ID_LIST_ID ) REFERENCES AGENCY_ID_LIST ( AGENCY_ID_LIST_ID ) NOT DEFERRABLE ;

ALTER TABLE BDT_PRI_RESTRI ADD CONSTRAINT BPR_BDT_ID_FK FOREIGN KEY ( BDT_ID ) REFERENCES DT ( DT_ID ) NOT DEFERRABLE ;

ALTER TABLE BDT_PRI_RESTRI ADD CONSTRAINT BPR_CAPXTM_ID_FK FOREIGN KEY ( CDT_AWD_PRI_XPS_TYPE_MAP_ID ) REFERENCES CDT_AWD_PRI_XPS_TYPE_MAP ( CDT_AWD_PRI_XPS_TYPE_MAP_ID ) NOT DEFERRABLE ;

ALTER TABLE BDT_PRI_RESTRI ADD CONSTRAINT BPR_CODE_LIST_ID_FK FOREIGN KEY ( CODE_LIST_ID ) REFERENCES CODE_LIST ( CODE_LIST_ID ) NOT DEFERRABLE ;

ALTER TABLE BDT_SC_PRI_RESTRI ADD CONSTRAINT BSPR_AGENCY_ID_LIST_ID_FK FOREIGN KEY ( AGENCY_ID_LIST_ID ) REFERENCES AGENCY_ID_LIST ( AGENCY_ID_LIST_ID ) NOT DEFERRABLE ;

ALTER TABLE BDT_SC_PRI_RESTRI ADD CONSTRAINT BSPR_BDT_SC_ID_FK FOREIGN KEY ( BDT_SC_ID ) REFERENCES DT_SC ( DT_SC_ID ) NOT DEFERRABLE ;

ALTER TABLE BDT_SC_PRI_RESTRI ADD CONSTRAINT BSPR_CODE_LIST_ID_FK FOREIGN KEY ( CODE_LIST_ID ) REFERENCES CODE_LIST ( CODE_LIST_ID ) NOT DEFERRABLE ;

ALTER TABLE BDT_SC_PRI_RESTRI ADD CONSTRAINT BSPR_CSAPXTM_ID_FK FOREIGN KEY ( CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID ) REFERENCES CDT_SC_AWD_PRI_XPS_TYPE_MAP ( CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID ) NOT DEFERRABLE ;

ALTER TABLE BIE_USER_EXT_REVISION ADD CONSTRAINT BUER_EXT_ABIE_ID_FK FOREIGN KEY ( EXT_ABIE_ID ) REFERENCES ABIE ( ABIE_ID ) NOT DEFERRABLE ;

ALTER TABLE BIE_USER_EXT_REVISION ADD CONSTRAINT BUER_EXT_ACC_ID_FK FOREIGN KEY ( EXT_ACC_ID ) REFERENCES ACC ( ACC_ID ) NOT DEFERRABLE ;

ALTER TABLE BIE_USER_EXT_REVISION ADD CONSTRAINT BUER_TOP_LEVEL_ABIE_ID_FK FOREIGN KEY ( TOP_LEVEL_ABIE_ID ) REFERENCES TOP_LEVEL_ABIE ( TOP_LEVEL_ABIE_ID ) NOT DEFERRABLE ;

ALTER TABLE BIE_USER_EXT_REVISION ADD CONSTRAINT BUER_USER_EXT_ACC_ID_FK FOREIGN KEY ( USER_EXT_ACC_ID ) REFERENCES ACC ( ACC_ID ) NOT DEFERRABLE ;

ALTER TABLE BIE_USAGE_RULE ADD CONSTRAINT BUR_ABIE_ID_FK FOREIGN KEY ( TARGET_ABIE_ID ) REFERENCES ABIE ( ABIE_ID ) NOT DEFERRABLE ;

ALTER TABLE BIE_USAGE_RULE ADD CONSTRAINT BUR_ASBIEP_ID_FK FOREIGN KEY ( TARGET_ASBIEP_ID ) REFERENCES ASBIEP ( ASBIEP_ID ) NOT DEFERRABLE ;

ALTER TABLE BIE_USAGE_RULE ADD CONSTRAINT BUR_ASBIE_ID_FK FOREIGN KEY ( TARGET_ASBIE_ID ) REFERENCES ASBIE ( ASBIE_ID ) NOT DEFERRABLE ;

ALTER TABLE BIE_USAGE_RULE ADD CONSTRAINT BUR_BBIEP_ID_FK FOREIGN KEY ( TARGET_BBIEP_ID ) REFERENCES BBIEP ( BBIEP_ID ) NOT DEFERRABLE ;

ALTER TABLE BIE_USAGE_RULE ADD CONSTRAINT BUR_BBIE_ID_FK FOREIGN KEY ( TARGET_BBIE_ID ) REFERENCES BBIE ( BBIE_ID ) NOT DEFERRABLE ;

ALTER TABLE BIE_USAGE_RULE ADD CONSTRAINT BUR_USAGE_RULE_ID_FK FOREIGN KEY ( ASSIGNED_USAGE_RULE_ID ) REFERENCES USAGE_RULE ( USAGE_RULE_ID ) NOT DEFERRABLE ;

ALTER TABLE CDT_AWD_PRI_XPS_TYPE_MAP ADD CONSTRAINT CAPXTM_CDT_AWD_PRI_ID_FK FOREIGN KEY ( CDT_AWD_PRI_ID ) REFERENCES CDT_AWD_PRI ( CDT_AWD_PRI_ID ) NOT DEFERRABLE ;

ALTER TABLE CDT_AWD_PRI_XPS_TYPE_MAP ADD CONSTRAINT CAPXTM_XBT_ID_FK FOREIGN KEY ( XBT_ID ) REFERENCES XBT ( XBT_ID ) NOT DEFERRABLE ;

ALTER TABLE CDT_AWD_PRI ADD CONSTRAINT CDT_AWD_PRI_CDT_ID_FK FOREIGN KEY ( CDT_ID ) REFERENCES DT ( DT_ID ) NOT DEFERRABLE ;

ALTER TABLE CDT_AWD_PRI ADD CONSTRAINT CDT_AWD_PRI_CDT_PRI_ID_FK FOREIGN KEY ( CDT_PRI_ID ) REFERENCES CDT_PRI ( CDT_PRI_ID ) NOT DEFERRABLE ;

ALTER TABLE CDT_SC_AWD_PRI ADD CONSTRAINT CDT_SC_AWD_PRI_CDT_PRI_ID_FK FOREIGN KEY ( CDT_PRI_ID ) REFERENCES CDT_PRI ( CDT_PRI_ID ) NOT DEFERRABLE ;

ALTER TABLE CDT_SC_AWD_PRI ADD CONSTRAINT CDT_SC_AWD_PRI_CDT_SC_ID_FK FOREIGN KEY ( CDT_SC_ID ) REFERENCES DT_SC ( DT_SC_ID ) NOT DEFERRABLE ;

ALTER TABLE CODE_LIST_VALUE ADD CONSTRAINT CLV_CODE_LIST_ID_FK FOREIGN KEY ( CODE_LIST_ID ) REFERENCES CODE_LIST ( CODE_LIST_ID ) NOT DEFERRABLE ;

ALTER TABLE CODE_LIST ADD CONSTRAINT CODE_LIST_AILV_FK FOREIGN KEY ( AGENCY_ID ) REFERENCES AGENCY_ID_LIST_VALUE ( AGENCY_ID_LIST_VALUE_ID ) NOT DEFERRABLE ;

ALTER TABLE CODE_LIST ADD CONSTRAINT CODE_LIST_BASED_CL_ID_FK FOREIGN KEY ( BASED_CODE_LIST_ID ) REFERENCES CODE_LIST ( CODE_LIST_ID ) NOT DEFERRABLE ;

ALTER TABLE CODE_LIST ADD CONSTRAINT CODE_LIST_CREATED_BY_FK FOREIGN KEY ( CREATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE CODE_LIST ADD CONSTRAINT CODE_LIST_LAST_UPDATED_BY_FK FOREIGN KEY ( LAST_UPDATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE CODE_LIST ADD CONSTRAINT CODE_LIST_MODULE_ID_FK FOREIGN KEY ( MODULE_ID ) REFERENCES MODULE ( MODULE_ID ) NOT DEFERRABLE ;

ALTER TABLE CDT_SC_AWD_PRI_XPS_TYPE_MAP ADD CONSTRAINT CSAPXTM_CSAP_FK FOREIGN KEY ( CDT_SC_AWD_PRI_ID ) REFERENCES CDT_SC_AWD_PRI ( CDT_SC_AWD_PRI_ID ) NOT DEFERRABLE ;

ALTER TABLE CDT_SC_AWD_PRI_XPS_TYPE_MAP ADD CONSTRAINT CSAPXTM_XBT_ID_FK FOREIGN KEY ( XBT_ID ) REFERENCES XBT ( XBT_ID ) NOT DEFERRABLE ;

ALTER TABLE CTX_SCHEME_VALUE ADD CONSTRAINT CSV_OWNER_CTX_SCHEME_ID_FK FOREIGN KEY ( OWNER_CTX_SCHEME_ID ) REFERENCES CTX_SCHEME ( CTX_SCHEME_ID ) NOT DEFERRABLE ;

ALTER TABLE CTX_SCHEME ADD CONSTRAINT CTX_SCHEME_CREATED_BY_FK FOREIGN KEY ( CREATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE CTX_SCHEME ADD CONSTRAINT CTX_SCHEME_CTX_CAT_ID_FK FOREIGN KEY ( CTX_CATEGORY_ID ) REFERENCES CTX_CATEGORY ( CTX_CATEGORY_ID ) NOT DEFERRABLE ;

ALTER TABLE CTX_SCHEME ADD CONSTRAINT CTX_SCHEME_LAST_UPDATED_BY_FK FOREIGN KEY ( LAST_UPDATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE DT ADD CONSTRAINT DT_BASED_DT_ID_FK FOREIGN KEY ( BASED_DT_ID ) REFERENCES DT ( DT_ID ) NOT DEFERRABLE ;

ALTER TABLE DT ADD CONSTRAINT DT_CREATED_BY_FK FOREIGN KEY ( CREATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE DT ADD CONSTRAINT DT_CURRENT_BDT_ID_FK FOREIGN KEY ( CURRENT_BDT_ID ) REFERENCES DT ( DT_ID ) NOT DEFERRABLE ;

ALTER TABLE DT ADD CONSTRAINT DT_LAST_UPDATED_BY_FK FOREIGN KEY ( LAST_UPDATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE DT ADD CONSTRAINT DT_MODULE_ID_FK FOREIGN KEY ( MODULE_ID ) REFERENCES MODULE ( MODULE_ID ) NOT DEFERRABLE ;

ALTER TABLE DT ADD CONSTRAINT DT_OWNER_USER_ID_FK FOREIGN KEY ( OWNER_USER_ID ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE DT ADD CONSTRAINT DT_PREVIOUS_VERSION_DT_ID_FK FOREIGN KEY ( PREVIOUS_VERSION_DT_ID ) REFERENCES DT ( DT_ID ) NOT DEFERRABLE ;

ALTER TABLE DT ADD CONSTRAINT DT_RELEASE_ID_FK FOREIGN KEY ( RELEASE_ID ) REFERENCES RELEASE ( RELEASE_ID ) NOT DEFERRABLE ;

ALTER TABLE DT_SC ADD CONSTRAINT DT_SC_BASED_DT_SC_ID_FK FOREIGN KEY ( BASED_DT_SC_ID ) REFERENCES DT_SC ( DT_SC_ID ) NOT DEFERRABLE ;

ALTER TABLE DT_SC ADD CONSTRAINT DT_SC_OWNER_DT_ID_FK FOREIGN KEY ( OWNER_DT_ID ) REFERENCES DT ( DT_ID ) NOT DEFERRABLE ;

ALTER TABLE DT_USAGE_RULE ADD CONSTRAINT DT_USAGE_RULE_DT_ID_FK FOREIGN KEY ( TARGET_DT_ID ) REFERENCES DT ( DT_ID ) NOT DEFERRABLE ;

ALTER TABLE DT_USAGE_RULE ADD CONSTRAINT DT_USAGE_RULE_DT_SC_ID_FK FOREIGN KEY ( TARGET_DT_SC_ID ) REFERENCES DT_SC ( DT_SC_ID ) NOT DEFERRABLE ;

ALTER TABLE DT_USAGE_RULE ADD CONSTRAINT DT_USAGE_RULE_USAGE_RULE_ID_FK FOREIGN KEY ( ASSIGNED_USAGE_RULE_ID ) REFERENCES USAGE_RULE ( USAGE_RULE_ID ) NOT DEFERRABLE ;

ALTER TABLE MODULE_DEP ADD CONSTRAINT MODULE_DEPENDED_ID_FK FOREIGN KEY ( DEPENDED_MODULE_ID ) REFERENCES MODULE ( MODULE_ID ) NOT DEFERRABLE ;

ALTER TABLE MODULE_DEP ADD CONSTRAINT MODULE_DEPENDING_ID_FK FOREIGN KEY ( DEPENDING_MODULE_ID ) REFERENCES MODULE ( MODULE_ID ) NOT DEFERRABLE ;

ALTER TABLE MODULE ADD CONSTRAINT MODULE_NAMESPACE_ID_FK FOREIGN KEY ( NAMESPACE_ID ) REFERENCES NAMESPACE ( NAMESPACE_ID ) NOT DEFERRABLE ;

ALTER TABLE MODULE ADD CONSTRAINT MODULE_RELEASE_ID_FK FOREIGN KEY ( RELEASE_ID ) REFERENCES RELEASE ( RELEASE_ID ) NOT DEFERRABLE ;

ALTER TABLE NAMESPACE ADD CONSTRAINT NAMESPACE_CREATED_BY_FK FOREIGN KEY ( CREATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE NAMESPACE ADD CONSTRAINT NAMESPACE_LAST_UPDATED_BY_FK FOREIGN KEY ( LAST_UPDATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE NAMESPACE ADD CONSTRAINT NAMESPACE_OWNER_USER_ID_FK FOREIGN KEY ( OWNER_USER_ID ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE RELEASE ADD CONSTRAINT RELEASE_NAMESPACE_ID_FK FOREIGN KEY ( NAMESPACE_ID ) REFERENCES NAMESPACE ( NAMESPACE_ID ) NOT DEFERRABLE ;

ALTER TABLE TOP_LEVEL_ABIE ADD CONSTRAINT TOP_LEVEL_ABIE_ABIE_ID_FK FOREIGN KEY ( ABIE_ID ) REFERENCES ABIE ( ABIE_ID ) NOT DEFERRABLE ;

ALTER TABLE TOP_LEVEL_ABIE ADD CONSTRAINT TOP_LEVEL_ABIE_APP_USER_FK FOREIGN KEY ( OWNER_USER_ID ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE USAGE_RULE_EXPRESSION ADD CONSTRAINT USAGE_RULE_XPS_REPS_UR_ID_FK FOREIGN KEY ( REPRESENTED_USAGE_RULE_ID ) REFERENCES USAGE_RULE ( USAGE_RULE_ID ) NOT DEFERRABLE ;

ALTER TABLE XBT ADD CONSTRAINT XBT_CREATED_BY_FK FOREIGN KEY ( CREATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE XBT ADD CONSTRAINT XBT_LAST_UPDATED_BY_FK FOREIGN KEY ( LAST_UPDATED_BY ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE XBT ADD CONSTRAINT XBT_MODULE_FK FOREIGN KEY ( MODULE_ID ) REFERENCES MODULE ( MODULE_ID ) NOT DEFERRABLE ;

ALTER TABLE XBT ADD CONSTRAINT XBT_OWNER_USER_ID_FK FOREIGN KEY ( OWNER_USER_ID ) REFERENCES APP_USER ( APP_USER_ID ) NOT DEFERRABLE ;

ALTER TABLE XBT ADD CONSTRAINT XBT_RELEASE_FK FOREIGN KEY ( RELEASE_ID ) REFERENCES RELEASE ( RELEASE_ID ) NOT DEFERRABLE ;

ALTER TABLE XBT ADD CONSTRAINT XBT_SUBTYPE_OF_XBT_ID_FK FOREIGN KEY ( SUBTYPE_OF_XBT_ID ) REFERENCES XBT ( XBT_ID ) NOT DEFERRABLE ;


ALTER SEQUENCE ABIE_ID_SEQ OWNED BY ABIE.ABIE_ID ;

ALTER SEQUENCE ACC_ID_SEQ OWNED BY ACC.ACC_ID ;

ALTER SEQUENCE AGENCY_ID_LIST_ID_SEQ OWNED BY AGENCY_ID_LIST.AGENCY_ID_LIST_ID;

ALTER SEQUENCE AGENCY_ID_LIST_VALUE_ID_SEQ OWNED BY AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID ;

ALTER SEQUENCE APP_USER_ID_SEQ OWNED BY APP_USER.APP_USER_ID ;

ALTER SEQUENCE ASBIEP_ID_SEQ OWNED BY ASBIEP.ASBIEP_ID ;

ALTER SEQUENCE ASBIE_ID_SEQ OWNED BY ASBIE.ASBIE_ID ;

ALTER SEQUENCE ASCCP_ID_SEQ OWNED BY ASCCP.ASCCP_ID ;

ALTER SEQUENCE ASCC_ID_SEQ OWNED BY ASCC.ASCC_ID ;

ALTER SEQUENCE BBIEP_ID_SEQ OWNED BY BBIEP.BBIEP_ID ;

ALTER SEQUENCE BBIE_ID_SEQ OWNED BY BBIE.BBIE_ID ;

ALTER SEQUENCE BBIE_SC_ID_SEQ OWNED BY BBIE_SC.BBIE_SC_ID ;

ALTER SEQUENCE BCCP_ID_SEQ OWNED BY BCCP.BCCP_ID ;

ALTER SEQUENCE BCC_ID_SEQ OWNED BY BCC.BCC_ID ;

ALTER SEQUENCE BDT_PRI_RESTRI_ID_SEQ OWNED BY BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID ;

ALTER SEQUENCE BDT_SC_PRI_RESTRI_ID_SEQ OWNED BY BDT_SC_PRI_RESTRI.BDT_SC_PRI_RESTRI_ID ;

ALTER SEQUENCE BIE_USAGE_RULE_ID_SEQ OWNED BY BIE_USAGE_RULE.BIE_USAGE_RULE_ID ;

ALTER SEQUENCE BIE_USER_EXT_REVISION_ID_SEQ OWNED BY BIE_USER_EXT_REVISION.BIE_USER_EXT_REVISION_ID ;

ALTER SEQUENCE BIZ_CTX_ID_SEQ OWNED BY BIZ_CTX.BIZ_CTX_ID ;

ALTER SEQUENCE BIZ_CTX_VALUE_ID_SEQ OWNED BY BIZ_CTX_VALUE.BIZ_CTX_VALUE_ID ;

ALTER SEQUENCE BLOB_CONTENT_ID_SEQ OWNED BY BLOB_CONTENT.BLOB_CONTENT_ID ;

ALTER SEQUENCE CDT_AWD_PRI_ID_SEQ OWNED BY CDT_AWD_PRI.CDT_AWD_PRI_ID ;

ALTER SEQUENCE CDT_AWD_PRI_XPS_TYP_MAP_ID_SEQ OWNED BY CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID ;

ALTER SEQUENCE CDT_PRI_ID_SEQ OWNED BY CDT_PRI.CDT_PRI_ID ;

ALTER SEQUENCE CDT_SC_AWD_PRI_ID_SEQ OWNED BY CDT_SC_AWD_PRI.CDT_SC_AWD_PRI_ID ;

ALTER SEQUENCE CDT_SC_AW_PR_XPS_TYP_MP_ID_SEQ OWNED BY CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID ;

ALTER SEQUENCE CLIENT_ID_SEQ OWNED BY CLIENT.CLIENT_ID ;

ALTER SEQUENCE CODE_LIST_ID_SEQ OWNED BY CODE_LIST.CODE_LIST_ID ;

ALTER SEQUENCE CODE_LIST_VALUE_ID_SEQ OWNED BY CODE_LIST_VALUE.CODE_LIST_VALUE_ID ;

ALTER SEQUENCE CTX_CATEGORY_ID_SEQ OWNED BY CTX_CATEGORY.CTX_CATEGORY_ID ;

ALTER SEQUENCE CTX_SCHEME_ID_SEQ OWNED BY CTX_SCHEME.CTX_SCHEME_ID ;

ALTER SEQUENCE CTX_SCHEME_VALUE_ID_SEQ OWNED BY CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID ;

ALTER SEQUENCE DT_ID_SEQ OWNED BY DT.DT_ID ;

ALTER SEQUENCE DT_SC_ID_SEQ OWNED BY DT_SC.DT_SC_ID ;

ALTER SEQUENCE DT_USAGE_RULE_ID_SEQ OWNED BY DT_USAGE_RULE.DT_USAGE_RULE_ID ;

ALTER SEQUENCE MODULE_DEP_ID_SEQ OWNED BY MODULE_DEP.MODULE_DEP_ID ;

ALTER SEQUENCE MODULE_ID_SEQ OWNED BY MODULE.MODULE_ID ;

ALTER SEQUENCE NAMESPACE_ID_SEQ OWNED BY NAMESPACE.NAMESPACE_ID ;

ALTER SEQUENCE RELEASE_ID_SEQ OWNED BY RELEASE.RELEASE_ID ;

ALTER SEQUENCE TOP_LEVEL_ABIE_ID_SEQ OWNED BY TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID ;

ALTER SEQUENCE USAGE_RULE_EXPRESSION_ID_SEQ OWNED BY USAGE_RULE_EXPRESSION.USAGE_RULE_EXPRESSION_ID ;

ALTER SEQUENCE USAGE_RULE_ID_SEQ OWNED BY USAGE_RULE.USAGE_RULE_ID ;

ALTER SEQUENCE XBT_ID_SEQ OWNED BY XBT.XBT_ID ;
