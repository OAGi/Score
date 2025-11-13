from datetime import datetime, timezone
from typing import Optional
from sqlmodel import SQLModel, Field, Relationship
from pydantic import field_validator
from services.utils import validate_guid

from services.models.app_user import AppUser
from services.models.release import Release

class TopLevelAsbiepBase(SQLModel):
    """Base model for TopLevelAsbiep with common fields."""
    based_top_level_asbiep_id: Optional[int] = Field(default=None, foreign_key="top_level_asbiep.top_level_asbiep_id", description="Foreign key referencing the inherited base TOP_LEVEL_ASBIEP_ID.")
    asbiep_id: Optional[int] = Field(default=None, foreign_key="asbiep.asbiep_id", description="Foreign key to the ASBIEP table pointing to a record which is a top-level ASBIEP.")
    owner_user_id: int = Field(foreign_key="app_user.app_user_id", description="Owner user ID.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="The timestamp when among all related bie records was last updated.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="A foreign key referring to the last user who has updated any related bie records.")
    release_id: int = Field(foreign_key="release.release_id", description="Foreign key to the RELEASE table. It identifies the release, for which this module is associated.")
    version: Optional[str] = Field(default=None, max_length=45, description="This column hold a version number assigned by the user. This column is only used by the top-level ASBIEP. No format of version is enforced.")
    status: Optional[str] = Field(default=None, max_length=45, description="This is different from the STATE column which is CRUD life cycle of an entity. The use case for this is to allow the user to indicate the usage status of a top-level ASBIEP (a profile BOD). An integration architect can use this column. Example values are 'Prototype', 'Test', and 'Production'. Only the top-level ASBIEP can use this field.")
    state: Optional[str] = Field(default=None, max_length=20, description="State of the top-level ASBIEP.")
    inverse_mode: bool = Field(default=False, description="If this is true, all BIEs not edited by users under this TOP_LEVEL_ASBIEP will be treated as used BIEs.")
    is_deprecated: bool = Field(default=False, description="Indicates whether the TOP_LEVEL_ASBIEP is deprecated.")
    deprecated_reason: Optional[str] = Field(default=None, description="The reason for the deprecation of the TOP_LEVEL_ASBIEP.")
    deprecated_remark: Optional[str] = Field(default=None, description="The remark for the deprecation of the TOP_LEVEL_ASBIEP.")
    source_top_level_asbiep_id: Optional[int] = Field(default=None, foreign_key="top_level_asbiep.top_level_asbiep_id", description="A foreign key referring to the source TOP_LEVEL_ASBIEP_ID which has linked to this record.")
    source_action: Optional[str] = Field(default=None, max_length=20, description="An action that had used to create a reference from the source (e.g., 'Copy' or 'Uplift'.)")
    source_timestamp: Optional[datetime] = Field(default=None, description="A timestamp when a source reference had been made.")

class TopLevelAsbiep(TopLevelAsbiepBase, table=True):
    """Model for the top_level_asbiep table."""
    __tablename__ = "top_level_asbiep"
    
    top_level_asbiep_id: Optional[int] = Field(default=None, primary_key=True, description="A internal, primary database key of an top-level ASBIEP.")
    
    # Relationships
    based_top_level_asbiep: Optional["TopLevelAsbiep"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[TopLevelAsbiep.based_top_level_asbiep_id]", "remote_side": "[TopLevelAsbiep.top_level_asbiep_id]"})
    asbiep: Optional["Asbiep"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[TopLevelAsbiep.asbiep_id]"})
    owner_user: "AppUser" = Relationship(sa_relationship_kwargs={"foreign_keys": "[TopLevelAsbiep.owner_user_id]"})
    last_updated_by_user: "AppUser" = Relationship(sa_relationship_kwargs={"foreign_keys": "[TopLevelAsbiep.last_updated_by]"})
    release: "Release" = Relationship()
    source_top_level_asbiep: Optional["TopLevelAsbiep"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[TopLevelAsbiep.source_top_level_asbiep_id]", "remote_side": "[TopLevelAsbiep.top_level_asbiep_id]"})

class TopLevelAsbiepRead(TopLevelAsbiepBase):
    """Model for reading TopLevelAsbiep data."""
    top_level_asbiep_id: int

class AsbiepBase(SQLModel):
    """Base model for Asbiep with common fields."""
    guid: str = Field(max_length=32, description="A globally unique identifier (GUID).")
    based_asccp_manifest_id: int = Field(foreign_key="asccp_manifest.asccp_manifest_id", description="A foreign key pointing to the ASCCP_MANIFEST record. It is the ASCCP, on which the ASBIEP contextualizes.")
    path: Optional[str] = Field(default=None, description="Path of the component graph.")
    hash_path: str = Field(max_length=64, description="hash_path generated from the path of the component graph using hash function, so that it is unique in the graph.")
    role_of_abie_id: int = Field(foreign_key="abie.abie_id", description="A foreign key pointing to the ABIE record. It is the ABIE, which the property term in the based ASCCP qualifies. Note that the ABIE has to be derived from the ACC used by the based ASCCP.")
    definition: Optional[str] = Field(default=None, description="A definition to override the ASCCP's definition. If NULL, it means that the definition should be derived from the based ASCCP on the UI, expression generation, and any API.")
    remark: Optional[str] = Field(default=None, max_length=225, description="This column allows the user to specify a context-specific usage of the BIE. It is different from the DEFINITION column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ASBIEP can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ASBIEP. A remark about that ASBIEP may be \"Type of BOM should be recognized in the BOM/typeCode.\"")
    biz_term: Optional[str] = Field(default=None, max_length=225, description="This column represents a business term to indicate what the BIE is called in a particular business context. With this current design, only one business term is allowed per business context.")
    display_name: Optional[str] = Field(default=None, max_length=100, description="The display name of the ASBIEP")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="A foreign key referring to the user who creates the ASBIEP. The creator of the ASBIEP is also its owner by default. ASBIEPs created as children of another ABIE have the same CREATED_BY.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="A foreign key referring to the last user who has updated the ASBIEP record.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the ASBIEP record was first created. ASBIEPs created as children of another ABIE have the same CREATION_TIMESTAMP.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="The timestamp when the ASBIEP was last updated.")
    owner_top_level_asbiep_id: int = Field(foreign_key="top_level_asbiep.top_level_asbiep_id", description="This is a foreign key to the top-level ASBIEP.")

    @field_validator('guid')
    @classmethod
    def validate_guid_format(cls, v):
        return validate_guid(v)

class Asbiep(AsbiepBase, table=True):
    """Model for the asbiep table."""
    __tablename__ = "asbiep"
    
    asbiep_id: Optional[int] = Field(default=None, primary_key=True, description="A internal, primary database key of an ASBIEP.")
    
    # Relationships
    based_asccp_manifest: "AsccpManifest" = Relationship()
    role_of_abie: "Abie" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Asbiep.role_of_abie_id]"})
    created_by_user: "AppUser" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Asbiep.created_by]"})
    last_updated_by_user: "AppUser" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Asbiep.last_updated_by]"})
    owner_top_level_asbiep: "TopLevelAsbiep" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Asbiep.owner_top_level_asbiep_id]"})

class AsbiepRead(AsbiepBase):
    """Model for reading Asbiep data."""
    asbiep_id: int

class AsbiepSupportDocBase(SQLModel):
    """Base model for AsbiepSupportDoc with common fields."""
    asbiep_id: int = Field(foreign_key="asbiep.asbiep_id", description="Foreign key. References the related ASBIEP record.")
    content: Optional[str] = Field(default=None, description="The main body or text content of the supporting documentation.")
    description: Optional[str] = Field(default=None, description="Optional description, summary, or metadata about the supporting documentation.")

class AsbiepSupportDoc(AsbiepSupportDocBase, table=True):
    """Model for the asbiep_support_doc table."""
    __tablename__ = "asbiep_support_doc"
    
    asbiep_support_doc_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key. Unique identifier for each supporting documentation.")
    
    # Relationships
    asbiep: "Asbiep" = Relationship()

class AsbiepSupportDocRead(AsbiepSupportDocBase):
    """Model for reading AsbiepSupportDoc data."""
    asbiep_support_doc_id: int

class AbieBase(SQLModel):
    """Base model for Abie with common fields."""
    guid: str = Field(max_length=32, description="A globally unique identifier (GUID).")
    based_acc_manifest_id: int = Field(foreign_key="acc_manifest.acc_manifest_id", description="A foreign key to the ACC_MANIFEST table refering to the ACC, on which the business context has been applied to derive this ABIE.")
    path: Optional[str] = Field(default=None, description="Path of the component graph.")
    hash_path: str = Field(max_length=64, description="hash_path generated from the path of the component graph using hash function, so that it is unique in the graph.")
    biz_ctx_id: Optional[int] = Field(default=None, foreign_key="biz_ctx.biz_ctx_id", description="(Deprecated) A foreign key to the BIZ_CTX table. This column stores the business context assigned to the ABIE.")
    definition: Optional[str] = Field(default=None, description="Definition to override the ACC's definition. If NULL, it means that the definition should be inherited from the based CC.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="A foreign key referring to the user who creates the ABIE. The creator of the ABIE is also its owner by default. ABIEs created as children of another ABIE have the same CREATED_BY as its parent.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="A foreign key referring to the user who has updated the ABIE record. This may be the user who is in the same group as the creator.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the ABIE record was first created. ABIEs created as children of another ABIE have the same CREATION_TIMESTAMP.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="The timestamp when the ABIE was last updated.")
    state: Optional[int] = Field(default=None, description="2 = EDITING, 4 = PUBLISHED. This column is only used with a top-level ABIE, because that is the only entry point for editing. The state value indicates the visibility of the top-level ABIE to users other than the owner. In the user group environment, a logic can apply that other users in the group can see the top-level ABIE only when it is in the 'Published' state.")
    remark: Optional[str] = Field(default=None, max_length=225, description="This column allows the user to specify very context-specific usage of the BIE. It is different from the DEFINITION column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be \"Type of BOM should be recognized in the BOM/typeCode.\"")
    biz_term: Optional[str] = Field(default=None, max_length=225, description="To indicate what the BIE is called in a particular business context. With this current design, only one business term is allowed per business context.")
    owner_top_level_asbiep_id: int = Field(foreign_key="top_level_asbiep.top_level_asbiep_id", description="This is a foreign key to the top-level ASBIEP.")

    @field_validator('guid')
    @classmethod
    def validate_guid_format(cls, v):
        return validate_guid(v)

class Abie(AbieBase, table=True):
    """Model for the abie table."""
    __tablename__ = "abie"
    
    abie_id: Optional[int] = Field(default=None, primary_key=True, description="A internal, primary database key of an ABIE.")
    
    # Relationships
    based_acc_manifest: "AccManifest" = Relationship()
    biz_ctx: Optional["BizCtx"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Abie.biz_ctx_id]"})
    created_by_user: "AppUser" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Abie.created_by]"})
    last_updated_by_user: "AppUser" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Abie.last_updated_by]"})
    owner_top_level_asbiep: "TopLevelAsbiep" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Abie.owner_top_level_asbiep_id]"})

class AbieRead(AbieBase):
    """Model for reading Abie data."""
    abie_id: int

class AsbieBase(SQLModel):
    """Base model for Asbie with common fields."""
    guid: str = Field(max_length=32, description="A globally unique identifier (GUID).")
    based_ascc_manifest_id: int = Field(foreign_key="ascc_manifest.ascc_manifest_id", description="The BASED_ASCC_MANIFEST_ID column refers to the ASCC_MANIFEST record, which this ASBIE contextualizes.")
    path: Optional[str] = Field(default=None, description="Path of the component graph.")
    hash_path: str = Field(max_length=64, description="hash_path generated from the path of the component graph using hash function, so that it is unique in the graph.")
    from_abie_id: int = Field(foreign_key="abie.abie_id", description="A foreign key pointing to the ABIE table. FROM_ABIE_ID is basically a parent data element (type) of the TO_ASBIEP_ID. FROM_ABIE_ID must be based on the FROM_ACC_ID in the BASED_ASCC_ID except when the FROM_ACC_ID refers to an SEMANTIC_GROUP ACC or USER_EXTENSION_GROUP ACC.")
    to_asbiep_id: Optional[int] = Field(default=None, foreign_key="asbiep.asbiep_id", description="A foreign key to the ASBIEP table. TO_ASBIEP_ID is basically a child data element of the FROM_ABIE_ID. The TO_ASBIEP_ID must be based on the TO_ASCCP_ID in the BASED_ASCC_ID. the ASBIEP is reused with the OWNER_TOP_LEVEL_ASBIEP is different after joining ASBIE and ASBIEP tables")
    definition: Optional[str] = Field(default=None, description="Definition to override the ASCC definition. If NULL, it means that the definition should be derived from the based CC on the UI, expression generation, and any API.")
    cardinality_min: int = Field(description="Minimum occurence constraint of the TO_ASBIEP_ID. A valid value is a non-negative integer.")
    cardinality_max: int = Field(description="Maximum occurrence constraint of the TO_ASBIEP_ID. A valid value is an integer from -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.")
    is_nillable: bool = Field(default=False, description="Indicate whether the TO_ASBIEP_ID is allowed to be null.")
    remark: Optional[str] = Field(default=None, max_length=225, description="This column allows the user to specify very context-specific usage of the BIE. It is different from the DEFINITION column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be \"Type of BOM should be recognized in the BOM/typeCode.\"")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="A foreign key referring to the user who creates the ASBIE. The creator of the ASBIE is also its owner by default. ASBIEs created as children of another ABIE have the same CREATED_BY.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="A foreign key referring to the user who has last updated the ASBIE record.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the ASBIE record was first created. ASBIEs created as children of another ABIE have the same CREATION_TIMESTAMP.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="The timestamp when the ASBIE was last updated.")
    seq_key: float = Field(description="This indicates the order of the associations among other siblings. The SEQ_KEY for BIEs is decimal in order to accomodate the removal of inheritance hierarchy and group. For example, children of the most abstract ACC will have SEQ_KEY = 1.1, 1.2, 1.3, and so on; and SEQ_KEY of the next abstraction level ACC will have SEQ_KEY = 2.1, 2.2, 2.3 and so on so forth.")
    is_used: bool = Field(default=False, description="Flag to indicate whether the field/component is used in the content model. It signifies whether the field/component should be generated.")
    is_deprecated: bool = Field(default=False, description="Indicates whether the ASBIE is deprecated.")
    owner_top_level_asbiep_id: int = Field(foreign_key="top_level_asbiep.top_level_asbiep_id", description="This is a foreign key to the top-level ASBIEP.")

    @field_validator('guid')
    @classmethod
    def validate_guid_format(cls, v):
        return validate_guid(v)

class Asbie(AsbieBase, table=True):
    """Model for the asbie table."""
    __tablename__ = "asbie"
    
    asbie_id: Optional[int] = Field(default=None, primary_key=True, description="A internal, primary database key of an ASBIE.")
    
    # Relationships
    based_ascc_manifest: "AsccManifest" = Relationship()
    from_abie: "Abie" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Asbie.from_abie_id]"})
    to_asbiep: Optional["Asbiep"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Asbie.to_asbiep_id]"})
    created_by_user: "AppUser" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Asbie.created_by]"})
    last_updated_by_user: "AppUser" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Asbie.last_updated_by]"})
    owner_top_level_asbiep: "TopLevelAsbiep" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Asbie.owner_top_level_asbiep_id]"})

class AsbieRead(AsbieBase):
    """Model for reading Asbie data."""
    asbie_id: int

class BbieBase(SQLModel):
    """Base model for Bbie with common fields."""
    guid: str = Field(max_length=32, description="A globally unique identifier (GUID).")
    based_bcc_manifest_id: int = Field(foreign_key="bcc_manifest.bcc_manifest_id", description="The BASED_BCC_MANIFEST_ID column refers to the BCC_MANIFEST record, which this BBIE contextualizes.")
    path: Optional[str] = Field(default=None, description="Path of the component graph.")
    hash_path: str = Field(max_length=64, description="hash_path generated from the path of the component graph using hash function, so that it is unique in the graph.")
    from_abie_id: int = Field(foreign_key="abie.abie_id", description="FROM_ABIE_ID must be based on the FROM_ACC_ID in the BASED_BCC_ID.")
    to_bbiep_id: int = Field(foreign_key="bbiep.bbiep_id", description="TO_BBIEP_ID is a foreign key to the BBIEP table. TO_BBIEP_ID basically refers to a child data element of the FROM_ABIE_ID. TO_BBIEP_ID must be based on the TO_BCCP_ID in the based BCC.")
    xbt_manifest_id: Optional[int] = Field(default=None, foreign_key="xbt_manifest.xbt_manifest_id", description="This is the foreign key to the XBT_MANIFEST table. It indicates the primitive assigned to the BBIE (or also can be viewed as assigned to the BBIEP for this specific association). This is assigned by the user who authors the BIE. The assignment would override the default from the DT_AWD_PRI side.")
    code_list_manifest_id: Optional[int] = Field(default=None, foreign_key="code_list_manifest.code_list_manifest_id", description="This is a foreign key to the CODE_LIST_MANIFEST table. If a code list is assigned to the BBIE (or also can be viewed as assigned to the BBIEP for this association), then this column stores the assigned code list. It should be noted that one of the possible primitives assignable to the BDT_PRI_RESTRI_ID column may also be a code list. So this column is typically used when the user wants to assign another code list different from the one permissible by the CC model.")
    agency_id_list_manifest_id: Optional[int] = Field(default=None, foreign_key="agency_id_list_manifest.agency_id_list_manifest_id", description="This is a foreign key to the AGENCY_ID_LIST_MANIFEST table. It is used in the case that the BDT content can be restricted to an agency identification.")
    cardinality_min: int = Field(description="The minimum occurrence constraint for the BBIE. A valid value is a non-negative integer.")
    cardinality_max: Optional[int] = Field(default=None, description="Maximum occurence constraint of the TO_BBIEP_ID. A valid value is an integer from -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.")
    facet_min_length: Optional[int] = Field(default=None, description="Defines the minimum number of units of length.")
    facet_max_length: Optional[int] = Field(default=None, description="Defines the minimum number of units of length.")
    facet_pattern: Optional[str] = Field(default=None, description="Defines a constraint on the lexical space of a datatype to literals in a specific pattern.")
    default_value: Optional[str] = Field(default=None, description="This column specifies the default value constraint. Default and fixed value constraints cannot be used at the same time.")
    is_nillable: bool = Field(default=False, description="Indicate whether the field can have a null This is corresponding to the nillable flag in the XML schema.")
    fixed_value: Optional[str] = Field(default=None, description="This column captures the fixed value constraint. Default and fixed value constraints cannot be used at the same time.")
    is_null: bool = Field(default=False, description="This column indicates whether the field is fixed to NULL. IS_NULLl can be true only if the IS_NILLABLE is true. If IS_NULL is true then the FIX_VALUE and DEFAULT_VALUE columns cannot have a value.")
    definition: Optional[str] = Field(default=None, description="Description to override the BCC definition. If NULLl, it means that the definition should be inherited from the based BCC.")
    example: Optional[str] = Field(default=None, description="Example value.")
    remark: Optional[str] = Field(default=None, max_length=225, description="This column allows the user to specify very context-specific usage of the BIE. It is different from the DEFINITION column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be \"Type of BOM should be recognized in the BOM/typeCode.\"")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="A foreign key referring to the user who creates the BBIE. The creator of the BBIE is also its owner by default. BBIEs created as children of another ABIE have the same CREATED_BY.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="A foreign key referring to the user who has last updated the ASBIE record.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the BBIE record was first created. BBIEs created as children of another ABIE have the same CREATION_TIMESTAMP.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="The timestamp when the ASBIE was last updated.")
    seq_key: Optional[float] = Field(default=None, description="This indicates the order of the associations among other siblings. The SEQ_KEY for BIEs is decimal in order to accomodate the removal of inheritance hierarchy and group. For example, children of the most abstract ACC will have SEQ_KEY = 1.1, 1.2, 1.3, and so on; and SEQ_KEY of the next abstraction level ACC will have SEQ_KEY = 2.1, 2.2, 2.3 and so on so forth.")
    is_used: bool = Field(default=False, description="Flag to indicate whether the field/component is used in the content model. It indicates whether the field/component should be generated in the expression generation.")
    is_deprecated: bool = Field(default=False, description="Indicates whether the BBIE is deprecated.")
    owner_top_level_asbiep_id: int = Field(foreign_key="top_level_asbiep.top_level_asbiep_id", description="This is a foreign key to the top-level ASBIEP.")

    @field_validator('guid')
    @classmethod
    def validate_guid_format(cls, v):
        return validate_guid(v)

class Bbie(BbieBase, table=True):
    """Model for the bbie table."""
    __tablename__ = "bbie"
    
    bbie_id: Optional[int] = Field(default=None, primary_key=True, description="A internal, primary database key of a BBIE.")
    
    # Relationships
    based_bcc_manifest: "BccManifest" = Relationship()
    from_abie: "Abie" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bbie.from_abie_id]"})
    to_bbiep: "Bbiep" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bbie.to_bbiep_id]"})
    xbt_manifest: Optional["XbtManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bbie.xbt_manifest_id]"})
    code_list_manifest: Optional["CodeListManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bbie.code_list_manifest_id]"})
    agency_id_list_manifest: Optional["AgencyIdListManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bbie.agency_id_list_manifest_id]"})
    created_by_user: "AppUser" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bbie.created_by]"})
    last_updated_by_user: "AppUser" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bbie.last_updated_by]"})
    owner_top_level_asbiep: "TopLevelAsbiep" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bbie.owner_top_level_asbiep_id]"})

class BbieRead(BbieBase):
    """Model for reading Bbie data."""
    bbie_id: int

class BbiepBase(SQLModel):
    """Base model for Bbiep with common fields."""
    guid: str = Field(max_length=32, description="A globally unique identifier (GUID).")
    based_bccp_manifest_id: int = Field(foreign_key="bccp_manifest.bccp_manifest_id", description="A foreign key pointing to the BCCP_MANIFEST record. It is the BCCP, which the BBIEP contextualizes.")
    path: Optional[str] = Field(default=None, description="Path of the component graph.")
    hash_path: str = Field(max_length=64, description="hash_path generated from the path of the component graph using hash function, so that it is unique in the graph.")
    definition: Optional[str] = Field(default=None, description="Definition to override the BCCP's Definition. If NULLl, it means that the definition should be inherited from the based CC.")
    remark: Optional[str] = Field(default=None, max_length=225, description="This column allows the user to specify very context-specific usage of the BIE. It is different from the Definition column in that the DEFINITION column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others. For example, BOM BOD, as an ACC, is a generic BOM structure. In a particular context, a BOM ABIE can be a Super BOM. Explanation of the Super BOM concept should be captured in the Definition of the ABIE. A remark about that ABIE may be \"Type of BOM should be recognized in the BOM/typeCode.")
    biz_term: Optional[str] = Field(default=None, max_length=225, description="Business term to indicate what the BIE is called in a particular business context such as in an industry.")
    display_name: Optional[str] = Field(default=None, max_length=100, description="The display name of the BBIEP")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="A foreign key referring to the user who creates the BBIEP. The creator of the BBIEP is also its owner by default. BBIEPs created as children of another ABIE have the same CREATED_BY'',")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="A foreign key referring to the last user who has updated the BBIEP record.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the BBIEP record was first created. BBIEPs created as children of another ABIE have the same CREATION_TIMESTAMP,")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="The timestamp when the BBIEP was last updated.")
    owner_top_level_asbiep_id: int = Field(foreign_key="top_level_asbiep.top_level_asbiep_id", description="This is a foreign key to the top-level ASBIEP.")

    @field_validator('guid')
    @classmethod
    def validate_guid_format(cls, v):
        return validate_guid(v)

class Bbiep(BbiepBase, table=True):
    """Model for the bbiep table."""
    __tablename__ = "bbiep"
    
    bbiep_id: Optional[int] = Field(default=None, primary_key=True, description="A internal, primary database key of an BBIEP.")
    
    # Relationships
    based_bccp_manifest: "BccpManifest" = Relationship()
    created_by_user: "AppUser" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bbiep.created_by]"})
    last_updated_by_user: "AppUser" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bbiep.last_updated_by]"})
    owner_top_level_asbiep: "TopLevelAsbiep" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bbiep.owner_top_level_asbiep_id]"})

class BbiepRead(BbiepBase):
    """Model for reading Bbiep data."""
    bbiep_id: int

class BbieScBase(SQLModel):
    """Base model for BbieSc with common fields."""
    guid: str = Field(max_length=32, description="A globally unique identifier (GUID).")
    based_dt_sc_manifest_id: int = Field(foreign_key="dt_sc_manifest.dt_sc_manifest_id", description="Foreign key to the DT_SC_MANIFEST table. This should correspond to the DT_SC of the BDT of the based BCC and BCCP.")
    path: Optional[str] = Field(default=None, description="Path of the component graph.")
    hash_path: str = Field(max_length=64, description="hash_path generated from the path of the component graph using hash function, so that it is unique in the graph.")
    bbie_id: int = Field(foreign_key="bbie.bbie_id", description="The BBIE this BBIE_SC applies to.")
    xbt_manifest_id: Optional[int] = Field(default=None, foreign_key="xbt_manifest.xbt_manifest_id", description="This must be one of the allowed primitive as specified in the corresponding SC of the based BCC of the BBIE (referred to by the BBIE_ID column).\n\nIt is the foreign key to the XBT_MANIFEST table. This is assigned by the user who authors the BIE. The assignment would override the default from the CC side.\n\nThis column, the CODE_LIST_ID column, and AGENCY_ID_LIST_ID column cannot have a value at the same time.")
    code_list_manifest_id: Optional[int] = Field(default=None, foreign_key="code_list_manifest.code_list_manifest_id", description="This is a foreign key to the CODE_LIST_MANIFEST table. If a code list is assigned to the BBIE SC (or also can be viewed as assigned to the BBIEP SC for this association), then this column stores the assigned code list. It should be noted that one of the possible primitives assignable to the DT_SC_PRI_RESTRI_ID column may also be a code list. So this column is typically used when the user wants to assign another code list different from the one permissible by the CC model.\n\nThis column is, the DT_SC_PRI_RESTRI_ID column, and AGENCY_ID_LIST_ID column cannot have a value at the same time.")
    agency_id_list_manifest_id: Optional[int] = Field(default=None, foreign_key="agency_id_list_manifest.agency_id_list_manifest_id", description="This is a foreign key to the AGENCY_ID_LIST_MANIFEST table. If a agency ID list is assigned to the BBIE SC (or also can be viewed as assigned to the BBIEP SC for this association), then this column stores the assigned Agency ID list. It should be noted that one of the possible primitives assignable to the DT_SC_PRI_RESTRI_ID column may also be an Agency ID list. So this column is typically used only when the user wants to assign another Agency ID list different from the one permissible by the CC model.\n\nThis column, the DT_SC_PRI_RESTRI_ID column, and CODE_LIST_ID column cannot have a value at the same time.")
    cardinality_min: int = Field(description="The minimum occurrence constraint for the BBIE SC. A valid value is 0 or 1.")
    cardinality_max: int = Field(description="Maximum occurence constraint of the BBIE SC. A valid value is 0 or 1.")
    facet_min_length: Optional[int] = Field(default=None, description="Defines the minimum number of units of length.")
    facet_max_length: Optional[int] = Field(default=None, description="Defines the minimum number of units of length.")
    facet_pattern: Optional[str] = Field(default=None, description="Defines a constraint on the lexical space of a datatype to literals in a specific pattern.")
    default_value: Optional[str] = Field(default=None, description="This column specifies the default value constraint. Default and fixed value constraints cannot be used at the same time.")
    fixed_value: Optional[str] = Field(default=None, description="This column captures the fixed value constraint. Default and fixed value constraints cannot be used at the same time.")
    definition: Optional[str] = Field(default=None, description="Description to override the BDT SC definition. If NULL, it means that the definition should be inherited from the based BDT SC.")
    example: Optional[str] = Field(default=None, description="Example value.")
    remark: Optional[str] = Field(default=None, max_length=225, description="This column allows the user to specify a very context-specific usage of the BBIE SC. It is different from the Definition column in that the Definition column is a description conveying the meaning of the associated concept. Remarks may be a very implementation specific instruction or others.")
    biz_term: Optional[str] = Field(default=None, max_length=225, description="Business term to indicate what the BBIE SC is called in a particular business context. With this current design, only one business term is allowed per business context.")
    display_name: Optional[str] = Field(default=None, max_length=100, description="The display name of the BBIE_SC")
    is_used: bool = Field(default=False, description="Flag to indicate whether the field/component is used in the content model. It indicates whether the field/component should be generated.")
    is_deprecated: bool = Field(default=False, description="Indicates whether the BBIE_SC is deprecated.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="A foreign key referring to the user who creates the BBIE_SC. The creator of the BBIE_SC is also its owner by default.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="A foreign key referring to the user who has last updated the BBIE_SC record.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the BBIE_SC record was first created.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="The timestamp when the BBIE_SC was last updated.")
    owner_top_level_asbiep_id: int = Field(foreign_key="top_level_asbiep.top_level_asbiep_id", description="This is a foreign key to the top-level ASBIEP.")

    @field_validator('guid')
    @classmethod
    def validate_guid_format(cls, v):
        return validate_guid(v)

class BbieSc(BbieScBase, table=True):
    """Model for the bbie_sc table."""
    __tablename__ = "bbie_sc"
    
    bbie_sc_id: Optional[int] = Field(default=None, primary_key=True, description="A internal, primary database key of a BBIE_SC.")
    
    # Relationships
    based_dt_sc_manifest: "DtScManifest" = Relationship()
    bbie: "Bbie" = Relationship(sa_relationship_kwargs={"foreign_keys": "[BbieSc.bbie_id]"})
    xbt_manifest: Optional["XbtManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[BbieSc.xbt_manifest_id]"})
    code_list_manifest: Optional["CodeListManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[BbieSc.code_list_manifest_id]"})
    agency_id_list_manifest: Optional["AgencyIdListManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[BbieSc.agency_id_list_manifest_id]"})
    created_by_user: "AppUser" = Relationship(sa_relationship_kwargs={"foreign_keys": "[BbieSc.created_by]"})
    last_updated_by_user: "AppUser" = Relationship(sa_relationship_kwargs={"foreign_keys": "[BbieSc.last_updated_by]"})
    owner_top_level_asbiep: "TopLevelAsbiep" = Relationship(sa_relationship_kwargs={"foreign_keys": "[BbieSc.owner_top_level_asbiep_id]"})

class BbieScRead(BbieScBase):
    """Model for reading BbieSc data."""
    bbie_sc_id: int
