from datetime import datetime, timezone
from typing import Optional
from sqlmodel import SQLModel, Field, Relationship
from pydantic import field_validator
from services.utils import validate_guid

from services.models.app_user import AppUser
from services.models.namespace import Namespace
from services.models.release import Release

class DtBase(SQLModel):
    """Base model for DT with common fields."""
    guid: str = Field(max_length=32, description="A unique identifier within the release (GUID).")
    data_type_term: Optional[str] = Field(default=None, max_length=45, description="This is the data type term assigned to the DT.")
    qualifier: Optional[str] = Field(default=None, max_length=100, description="This column shall be blank when the DT_TYPE is CDT. When the DT_TYPE is BDT, this is optional.")
    representation_term: Optional[str] = Field(default=None, max_length=100, description="Representation term.")
    six_digit_id: Optional[str] = Field(default=None, max_length=45, description="The six number suffix comes from the UN/CEFACT XML Schema NDR.")
    based_dt_id: Optional[int] = Field(default=None, foreign_key="dt.dt_id", description="Foreign key pointing to the DT table itself.")
    definition: Optional[str] = Field(default=None, description="Description of the data type.")
    definition_source: Optional[str] = Field(default=None, max_length=200, description="URL identifying the source of the DEFINITION column.")
    namespace_id: Optional[int] = Field(default=None, foreign_key="namespace.namespace_id", description="Foreign key to the NAMESPACE table.")
    content_component_definition: Optional[str] = Field(default=None, description="Description of the content component of the data type.")
    state: Optional[str] = Field(default=None, max_length=20, description="Deleted, WIP, Draft, QA, Candidate, Production, Release Draft, Published.")
    commonly_used: bool = Field(default=False, description="This is a flag to indicate commonly used DT(s) by BCCPs.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. It indicates the user who created this DT.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the last user who updated the record.")
    owner_user_id: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. This is the user who owns the entity.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the revision of the DT was created.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the record was last updated.")
    is_deprecated: bool = Field(default=False, description="Indicates whether the CC is deprecated and should not be reused.")
    replacement_dt_id: Optional[int] = Field(default=None, foreign_key="dt.dt_id", description="This refers to a replacement if the record is deprecated.")
    prev_dt_id: Optional[int] = Field(default=None, foreign_key="dt.dt_id", description="A self-foreign key to indicate the previous history record.")
    next_dt_id: Optional[int] = Field(default=None, foreign_key="dt.dt_id", description="A self-foreign key to indicate the next history record.")
    
    @field_validator('guid')
    @classmethod
    def validate_guid(cls, v):
        if not validate_guid(v):
            raise ValueError("Guid must be a 32-character hexadecimal string (lowercase).")
        return v

class Dt(DtBase, table=True):
    """Model for the dt table."""
    __tablename__ = "dt"
    
    dt_id: Optional[int] = Field(default=None, primary_key=True, description="Internal, primary database key.")
    
    # Relationships
    based_dt: Optional["Dt"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Dt.based_dt_id]", "remote_side": "[Dt.dt_id]"})
    namespace: Optional["Namespace"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Dt.namespace_id]"})
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Dt.created_by]"})
    last_updater: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Dt.last_updated_by]"})
    owner: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Dt.owner_user_id]"})
    replacement_dt: Optional["Dt"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Dt.replacement_dt_id]", "remote_side": "[Dt.dt_id]"})
    prev_dt: Optional["Dt"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Dt.prev_dt_id]", "remote_side": "[Dt.dt_id]"})
    next_dt: Optional["Dt"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Dt.next_dt_id]", "remote_side": "[Dt.dt_id]"})

class DtRead(DtBase):
    """Model for reading Dt data."""
    dt_id: int

class DtManifestBase(SQLModel):
    """Base model for DtManifest with common fields."""
    release_id: int = Field(foreign_key="release.release_id", description="Foreign key to the release table.")
    dt_id: int = Field(foreign_key="dt.dt_id", description="Foreign key to the dt table.")
    based_dt_manifest_id: Optional[int] = Field(default=None, foreign_key="dt_manifest.dt_manifest_id", description="Foreign key to the dt_manifest table.")
    den: str = Field(max_length=200, description="Dictionary Entry Name of the data type.")
    conflict: bool = Field(default=False, description="This indicates that there is a conflict between self and relationship.")
    log_id: Optional[int] = Field(default=None, foreign_key="log.log_id", description="A foreign key pointed to a log for the current record.")
    replacement_dt_manifest_id: Optional[int] = Field(default=None, foreign_key="dt_manifest.dt_manifest_id", description="This refers to a replacement manifest if the record is deprecated.")
    prev_dt_manifest_id: Optional[int] = Field(default=None, foreign_key="dt_manifest.dt_manifest_id", description="Previous manifest ID.")
    next_dt_manifest_id: Optional[int] = Field(default=None, foreign_key="dt_manifest.dt_manifest_id", description="Next manifest ID.")

class DtManifest(DtManifestBase, table=True):
    """Model for the dt_manifest table."""
    __tablename__ = "dt_manifest"
    
    dt_manifest_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key for dt_manifest.")
    
    # Relationships
    release: "Release" = Relationship()
    dt: "Dt" = Relationship()
    log: Optional["Log"] = Relationship(back_populates="dt_manifests")
    based_dt_manifest: Optional["DtManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtManifest.based_dt_manifest_id]", "remote_side": "[DtManifest.dt_manifest_id]"})
    replacement_dt_manifest: Optional["DtManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtManifest.replacement_dt_manifest_id]", "remote_side": "[DtManifest.dt_manifest_id]"})
    prev_dt_manifest: Optional["DtManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtManifest.prev_dt_manifest_id]", "remote_side": "[DtManifest.dt_manifest_id]"})
    next_dt_manifest: Optional["DtManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtManifest.next_dt_manifest_id]", "remote_side": "[DtManifest.dt_manifest_id]"})

class DtManifestRead(DtManifestBase):
    """Model for reading DtManifest data."""
    dt_manifest_id: int

# DT_SC (Data Type Supplementary Component) Models

class DtScBase(SQLModel):
    """Base model for DT_SC with common fields."""
    guid: str = Field(max_length=32, description="A unique identifier within the release (GUID).")
    object_class_term: Optional[str] = Field(default=None, max_length=60, description="Object class term of the SC.")
    property_term: Optional[str] = Field(default=None, max_length=60, description="Property term of the SC.")
    representation_term: Optional[str] = Field(default=None, max_length=20, description="Representation of the supplementary component.")
    definition: Optional[str] = Field(default=None, description="Description of the supplementary component.")
    definition_source: Optional[str] = Field(default=None, max_length=200, description="URL identifying the source of the DEFINITION column.")
    owner_dt_id: Optional[int] = Field(default=None, foreign_key="dt.dt_id", description="Foreign key to the DT table indicating the data type, to which this supplementary component belongs.")
    cardinality_min: int = Field(default=0, description="The minimum occurrence constraint associated with the supplementary component.")
    cardinality_max: Optional[int] = Field(default=None, description="The maximum occurrence constraint associated with the supplementary component.")
    based_dt_sc_id: Optional[int] = Field(default=None, foreign_key="dt_sc.dt_sc_id", description="Foreign key to the DT_SC table itself.")
    default_value: Optional[str] = Field(default=None, description="This column specifies the default value constraint.")
    fixed_value: Optional[str] = Field(default=None, description="This column captures the fixed value constraint.")
    is_deprecated: bool = Field(default=False, description="Indicates whether this is deprecated and should not be reused.")
    replacement_dt_sc_id: Optional[int] = Field(default=None, foreign_key="dt_sc.dt_sc_id", description="This refers to a replacement if the record is deprecated.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. It indicates the user who created the code list.")
    owner_user_id: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. This is the user who owns the entity.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. It identifies the user who last updated the code list.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the code list was created.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the code list was last updated.")
    prev_dt_sc_id: Optional[int] = Field(default=None, foreign_key="dt_sc.dt_sc_id", description="A self-foreign key to indicate the previous history record.")
    next_dt_sc_id: Optional[int] = Field(default=None, foreign_key="dt_sc.dt_sc_id", description="A self-foreign key to indicate the next history record.")
    
    @field_validator('guid')
    @classmethod
    def validate_guid(cls, v):
        if not validate_guid(v):
            raise ValueError("Guid must be a 32-character hexadecimal string (lowercase).")
        return v

class DtSc(DtScBase, table=True):
    """Model for the dt_sc table."""
    __tablename__ = "dt_sc"
    
    dt_sc_id: Optional[int] = Field(default=None, primary_key=True, description="Internal, primary database key.")
    
    # Relationships
    owner_dt: Optional["Dt"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtSc.owner_dt_id]"})
    based_dt_sc: Optional["DtSc"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtSc.based_dt_sc_id]", "remote_side": "[DtSc.dt_sc_id]"})
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtSc.created_by]"})
    owner: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtSc.owner_user_id]"})
    last_updater: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtSc.last_updated_by]"})
    replacement_dt_sc: Optional["DtSc"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtSc.replacement_dt_sc_id]", "remote_side": "[DtSc.dt_sc_id]"})
    prev_dt_sc: Optional["DtSc"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtSc.prev_dt_sc_id]", "remote_side": "[DtSc.dt_sc_id]"})
    next_dt_sc: Optional["DtSc"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtSc.next_dt_sc_id]", "remote_side": "[DtSc.dt_sc_id]"})

class DtScRead(DtScBase):
    """Model for reading DtSc data."""
    dt_sc_id: int

class DtScManifestBase(SQLModel):
    """Base model for DtScManifest with common fields."""
    release_id: int = Field(foreign_key="release.release_id", description="Foreign key to the release table.")
    dt_sc_id: int = Field(foreign_key="dt_sc.dt_sc_id", description="Foreign key to the dt_sc table.")
    owner_dt_manifest_id: int = Field(foreign_key="dt_manifest.dt_manifest_id", description="Foreign key to the dt_manifest table.")
    based_dt_sc_manifest_id: Optional[int] = Field(default=None, foreign_key="dt_sc_manifest.dt_sc_manifest_id", description="Foreign key to the dt_sc_manifest table.")
    conflict: bool = Field(default=False, description="This indicates that there is a conflict between self and relationship.")
    replacement_dt_sc_manifest_id: Optional[int] = Field(default=None, foreign_key="dt_sc_manifest.dt_sc_manifest_id", description="This refers to a replacement manifest if the record is deprecated.")
    prev_dt_sc_manifest_id: Optional[int] = Field(default=None, foreign_key="dt_sc_manifest.dt_sc_manifest_id", description="Previous manifest ID.")
    next_dt_sc_manifest_id: Optional[int] = Field(default=None, foreign_key="dt_sc_manifest.dt_sc_manifest_id", description="Next manifest ID.")

class DtScManifest(DtScManifestBase, table=True):
    """Model for the dt_sc_manifest table."""
    __tablename__ = "dt_sc_manifest"
    
    dt_sc_manifest_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key for dt_sc_manifest.")
    
    # Relationships
    release: "Release" = Relationship()
    dt_sc: "DtSc" = Relationship()
    owner_dt_manifest: "DtManifest" = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtScManifest.owner_dt_manifest_id]"})
    based_dt_sc_manifest: Optional["DtScManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtScManifest.based_dt_sc_manifest_id]", "remote_side": "[DtScManifest.dt_sc_manifest_id]"})
    replacement_dt_sc_manifest: Optional["DtScManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtScManifest.replacement_dt_sc_manifest_id]", "remote_side": "[DtScManifest.dt_sc_manifest_id]"})
    prev_dt_sc_manifest: Optional["DtScManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtScManifest.prev_dt_sc_manifest_id]", "remote_side": "[DtScManifest.dt_sc_manifest_id]"})
    next_dt_sc_manifest: Optional["DtScManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtScManifest.next_dt_sc_manifest_id]", "remote_side": "[DtScManifest.dt_sc_manifest_id]"})

class DtScManifestRead(DtScManifestBase):
    """Model for reading DtScManifest data."""
    dt_sc_manifest_id: int

# CDT Primitive Model

class CdtPriBase(SQLModel):
    """Base model for CdtPri with common fields."""
    name: str = Field(max_length=45, description="Name of the CDT primitive per the CCTS datatype catalog.")

class CdtPri(CdtPriBase, table=True):
    """Model for the cdt_pri table."""
    __tablename__ = "cdt_pri"
    
    cdt_pri_id: Optional[int] = Field(default=None, primary_key=True, description="Internal, primary database key.")

class CdtPriRead(CdtPriBase):
    """Model for reading CdtPri data."""
    cdt_pri_id: int

# DT Allowed Primitives Models

class DtAwdPriBase(SQLModel):
    """Base model for DtAwdPri with common fields."""
    release_id: int = Field(foreign_key="release.release_id", description="Foreign key to the RELEASE table.")
    dt_id: int = Field(foreign_key="dt.dt_id", description="Foreign key to the DT table.")
    cdt_pri_id: int = Field(foreign_key="cdt_pri.cdt_pri_id", description="Foreign key to the CDT_PRI table.")
    xbt_manifest_id: Optional[int] = Field(default=None, foreign_key="xbt_manifest.xbt_manifest_id", description="Foreign key to the XBT_MANIFEST table.")
    code_list_manifest_id: Optional[int] = Field(default=None, foreign_key="code_list_manifest.code_list_manifest_id", description="Foreign key to the CODE_LIST_MANIFEST table.")
    agency_id_list_manifest_id: Optional[int] = Field(default=None, foreign_key="agency_id_list_manifest.agency_id_list_manifest_id", description="Foreign key to the AGENCY_ID_LIST_MANIFEST table.")
    is_default: bool = Field(default=False, description="It indicates the most generic primitive for the data type.")

class DtAwdPri(DtAwdPriBase, table=True):
    """Model for the dt_awd_pri table."""
    __tablename__ = "dt_awd_pri"
    
    dt_awd_pri_id: Optional[int] = Field(default=None, primary_key=True, description="Primary, internal database key.")
    
    # Relationships
    release: "Release" = Relationship()
    dt: "Dt" = Relationship()
    cdt_pri: "CdtPri" = Relationship()
    xbt_manifest: Optional["XbtManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtAwdPri.xbt_manifest_id]"})
    code_list_manifest: Optional["CodeListManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtAwdPri.code_list_manifest_id]"})
    agency_id_list_manifest: Optional["AgencyIdListManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtAwdPri.agency_id_list_manifest_id]"})

class DtAwdPriRead(DtAwdPriBase):
    """Model for reading DtAwdPri data."""
    dt_awd_pri_id: int

class DtScAwdPriBase(SQLModel):
    """Base model for DtScAwdPri with common fields."""
    release_id: int = Field(foreign_key="release.release_id", description="Foreign key to the RELEASE table.")
    dt_sc_id: int = Field(foreign_key="dt_sc.dt_sc_id", description="Foreign key to the DT_SC table.")
    cdt_pri_id: int = Field(foreign_key="cdt_pri.cdt_pri_id", description="Foreign key to the CDT_PRI table.")
    xbt_manifest_id: Optional[int] = Field(default=None, foreign_key="xbt_manifest.xbt_manifest_id", description="Foreign key to the XBT_MANIFEST table.")
    code_list_manifest_id: Optional[int] = Field(default=None, foreign_key="code_list_manifest.code_list_manifest_id", description="Foreign key to the CODE_LIST_MANIFEST table.")
    agency_id_list_manifest_id: Optional[int] = Field(default=None, foreign_key="agency_id_list_manifest.agency_id_list_manifest_id", description="Foreign key to the AGENCY_ID_LIST_MANIFEST table.")
    is_default: bool = Field(default=False, description="It indicates the most generic primitive for the data type.")

class DtScAwdPri(DtScAwdPriBase, table=True):
    """Model for the dt_sc_awd_pri table."""
    __tablename__ = "dt_sc_awd_pri"
    
    dt_sc_awd_pri_id: Optional[int] = Field(default=None, primary_key=True, description="Primary, internal database key.")
    
    # Relationships
    release: "Release" = Relationship()
    dt_sc: "DtSc" = Relationship()
    cdt_pri: "CdtPri" = Relationship()
    xbt_manifest: Optional["XbtManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtScAwdPri.xbt_manifest_id]"})
    code_list_manifest: Optional["CodeListManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtScAwdPri.code_list_manifest_id]"})
    agency_id_list_manifest: Optional["AgencyIdListManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtScAwdPri.agency_id_list_manifest_id]"})

class DtScAwdPriRead(DtScAwdPriBase):
    """Model for reading DtScAwdPri data."""
    dt_sc_awd_pri_id: int

# XBT (XML Built-in Type) Models

class XbtBase(SQLModel):
    """Base model for XBT with common fields."""
    guid: str = Field(max_length=32, description="A globally unique identifier (GUID).")
    name: Optional[str] = Field(default=None, max_length=45, description="Human understandable name of the built-in type.")
    builtIn_type: Optional[str] = Field(default=None, max_length=45, description="Built-in type as it should appear in the XML schema including the namespace prefix.")
    jbt_draft05_map: Optional[str] = Field(default=None, max_length=500, description="JBT Draft 05 mapping.")
    openapi30_map: Optional[str] = Field(default=None, max_length=500, description="OpenAPI 3.0 mapping.")
    avro_map: Optional[str] = Field(default=None, max_length=500, description="Avro mapping.")
    subtype_of_xbt_id: Optional[int] = Field(default=None, foreign_key="xbt.xbt_id", description="Foreign key to the XBT table itself. It indicates a super type of this XSD built-in type.")
    schema_definition: Optional[str] = Field(default=None, description="Schema definition.")
    revision_doc: Optional[str] = Field(default=None, description="Revision documentation.")
    state: Optional[int] = Field(default=None, description="State of the XBT.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table.")
    owner_user_id: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. This is the user who owns the entity.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the last user who updated the record.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the XBT was created.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the XBT was last updated.")
    is_deprecated: bool = Field(default=False, description="Indicates whether the XBT is deprecated.")
    
    @field_validator('guid')
    @classmethod
    def validate_guid(cls, v):
        if not validate_guid(v):
            raise ValueError("Guid must be a 32-character hexadecimal string (lowercase).")
        return v

class Xbt(XbtBase, table=True):
    """Model for the xbt table."""
    __tablename__ = "xbt"
    
    xbt_id: Optional[int] = Field(default=None, primary_key=True, description="Primary, internal database key.")
    
    # Relationships
    subtype_of_xbt: Optional["Xbt"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Xbt.subtype_of_xbt_id]", "remote_side": "[Xbt.xbt_id]"})
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Xbt.created_by]"})
    owner: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Xbt.owner_user_id]"})
    last_updater: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Xbt.last_updated_by]"})

class XbtRead(XbtBase):
    """Model for reading Xbt data."""
    xbt_id: int

class XbtManifestBase(SQLModel):
    """Base model for XbtManifest with common fields."""
    release_id: int = Field(foreign_key="release.release_id", description="Foreign key to the release table.")
    xbt_id: int = Field(foreign_key="xbt.xbt_id", description="Foreign key to the xbt table.")
    conflict: bool = Field(default=False, description="This indicates that there is a conflict between self and relationship.")
    log_id: Optional[int] = Field(default=None, foreign_key="log.log_id", description="A foreign key pointed to a log for the current record.")
    prev_xbt_manifest_id: Optional[int] = Field(default=None, foreign_key="xbt_manifest.xbt_manifest_id", description="Previous manifest ID.")
    next_xbt_manifest_id: Optional[int] = Field(default=None, foreign_key="xbt_manifest.xbt_manifest_id", description="Next manifest ID.")

class XbtManifest(XbtManifestBase, table=True):
    """Model for the xbt_manifest table."""
    __tablename__ = "xbt_manifest"
    
    xbt_manifest_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key for xbt_manifest.")
    
    # Relationships
    release: "Release" = Relationship()
    xbt: "Xbt" = Relationship()
    log: Optional["Log"] = Relationship(back_populates="xbt_manifests")
    prev_xbt_manifest: Optional["XbtManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[XbtManifest.prev_xbt_manifest_id]", "remote_side": "[XbtManifest.xbt_manifest_id]"})
    next_xbt_manifest: Optional["XbtManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[XbtManifest.next_xbt_manifest_id]", "remote_side": "[XbtManifest.xbt_manifest_id]"})

class XbtManifestRead(XbtManifestBase):
    """Model for reading XbtManifest data."""
    xbt_manifest_id: int

# SeqKey Models
