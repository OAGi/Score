from datetime import datetime, timezone
from typing import Optional, List
from sqlmodel import SQLModel, Field, Relationship
from pydantic import field_validator
from services.utils import validate_guid

from services.models.app_user import AppUser
from services.models.namespace import Namespace
from services.models.release import Release

class AgencyIdListBase(SQLModel):
    """Base model for AgencyIdList with common fields."""
    guid: str = Field(max_length=32, description="A unique identifier within the release (GUID).")
    enum_type_guid: str = Field(max_length=41, description="This column stores the GUID of the type containing the enumerated values.")
    name: Optional[str] = Field(default=None, max_length=100, description="Name of the agency identification list.")
    list_id: Optional[str] = Field(default=None, max_length=100, description="Business or standard identification assigned to the agency identification list.")
    agency_id_list_value_id: Optional[int] = Field(default=None, foreign_key="agency_id_list_value.agency_id_list_value_id", description="Identification of the agency or organization which developed and/or maintains the list.")
    version_id: Optional[str] = Field(default=None, max_length=100, description="Version number of the agency identification list.")
    based_agency_id_list_id: Optional[int] = Field(default=None, foreign_key="agency_id_list.agency_id_list_id", description="Foreign key to the AGENCY_ID_LIST table itself.")
    definition: Optional[str] = Field(default=None, description="Description of the agency identification list.")
    definition_source: Optional[str] = Field(default=None, max_length=100, description="URL which indicates the source of the agency id list DEFINITION.")
    remark: Optional[str] = Field(default=None, max_length=225, description="Usage information about the agency id list.")
    namespace_id: Optional[int] = Field(default=None, foreign_key="namespace.namespace_id", description="Foreign key to the NAMESPACE table.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. It indicates the user who created the agency ID list.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. It identifies the user who last updated the agency ID list.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the agency ID list was created.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the agency ID list was last updated.")
    state: Optional[str] = Field(default=None, max_length=20, description="Life cycle state of the agency ID list.")
    is_deprecated: bool = Field(default=False, description="Indicates whether the agency id list is deprecated and should not be reused.")
    replacement_agency_id_list_id: Optional[int] = Field(default=None, foreign_key="agency_id_list.agency_id_list_id", description="This refers to a replacement if the record is deprecated.")
    owner_user_id: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. This is the user who owns the entity.")
    prev_agency_id_list_id: Optional[int] = Field(default=None, foreign_key="agency_id_list.agency_id_list_id", description="A self-foreign key to indicate the previous history record.")
    next_agency_id_list_id: Optional[int] = Field(default=None, foreign_key="agency_id_list.agency_id_list_id", description="A self-foreign key to indicate the next history record.")
    
    @field_validator('guid')
    @classmethod
    def validate_guid(cls, v):
        if not validate_guid(v):
            raise ValueError("Guid must be a 32-character hexadecimal string (lowercase).")
        return v

class AgencyIdList(AgencyIdListBase, table=True):
    """Model for the agency_id_list table."""
    __tablename__ = "agency_id_list"
    
    agency_id_list_id: Optional[int] = Field(default=None, primary_key=True, description="A internal, primary database key.")
    
    # Relationships
    namespace: Optional["Namespace"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdList.namespace_id]"})
    agency_id_list_value: Optional["AgencyIdListValue"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdList.agency_id_list_value_id]"})
    based_agency_id_list: Optional["AgencyIdList"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdList.based_agency_id_list_id]", "remote_side": "[AgencyIdList.agency_id_list_id]"})
    replacement_agency_id_list: Optional["AgencyIdList"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdList.replacement_agency_id_list_id]", "remote_side": "[AgencyIdList.agency_id_list_id]"})
    prev_agency_id_list: Optional["AgencyIdList"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdList.prev_agency_id_list_id]", "remote_side": "[AgencyIdList.agency_id_list_id]"})
    next_agency_id_list: Optional["AgencyIdList"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdList.next_agency_id_list_id]", "remote_side": "[AgencyIdList.agency_id_list_id]"})
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdList.created_by]"})
    owner: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdList.owner_user_id]"})
    last_updater: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdList.last_updated_by]"})
    agency_id_list_values: List["AgencyIdListValue"] = Relationship(back_populates="owner_list", sa_relationship_kwargs={"foreign_keys": "[AgencyIdListValue.owner_list_id]"})

class AgencyIdListRead(AgencyIdListBase):
    """Model for reading AgencyIdList data."""
    agency_id_list_id: int

class AgencyIdListManifestBase(SQLModel):
    """Base model for AgencyIdListManifest with common fields."""
    release_id: int = Field(foreign_key="release.release_id", description="Foreign key to the release table.")
    agency_id_list_id: int = Field(foreign_key="agency_id_list.agency_id_list_id", description="Foreign key to the agency_id_list table.")
    agency_id_list_value_manifest_id: Optional[int] = Field(default=None, foreign_key="agency_id_list_value_manifest.agency_id_list_value_manifest_id", description="Foreign key to the agency_id_list_value_manifest table.")
    based_agency_id_list_manifest_id: Optional[int] = Field(default=None, foreign_key="agency_id_list_manifest.agency_id_list_manifest_id", description="Foreign key to the agency_id_list_manifest table.")
    conflict: bool = Field(default=False, description="This indicates that there is a conflict between self and relationship.")
    log_id: Optional[int] = Field(default=None, foreign_key="log.log_id", description="A foreign key pointed to a log for the current record.")
    replacement_agency_id_list_manifest_id: Optional[int] = Field(default=None, foreign_key="agency_id_list_manifest.agency_id_list_manifest_id", description="This refers to a replacement manifest if the record is deprecated.")
    prev_agency_id_list_manifest_id: Optional[int] = Field(default=None, foreign_key="agency_id_list_manifest.agency_id_list_manifest_id", description="Previous manifest ID.")
    next_agency_id_list_manifest_id: Optional[int] = Field(default=None, foreign_key="agency_id_list_manifest.agency_id_list_manifest_id", description="Next manifest ID.")

class AgencyIdListManifest(AgencyIdListManifestBase, table=True):
    """Model for the agency_id_list_manifest table."""
    __tablename__ = "agency_id_list_manifest"
    
    agency_id_list_manifest_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key for agency_id_list_manifest.")
    
    # Relationships
    release: "Release" = Relationship()
    agency_id_list: "AgencyIdList" = Relationship()
    log: Optional["Log"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdListManifest.log_id]"})
    agency_id_list_value_manifest: Optional["AgencyIdListValueManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdListManifest.agency_id_list_value_manifest_id]"})
    agency_id_list_value_manifests: list["AgencyIdListValueManifest"] = Relationship(back_populates="agency_id_list_manifest", sa_relationship_kwargs={"foreign_keys": "[AgencyIdListValueManifest.agency_id_list_manifest_id]"})
    based_agency_id_list_manifest: Optional["AgencyIdListManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdListManifest.based_agency_id_list_manifest_id]", "remote_side": "[AgencyIdListManifest.agency_id_list_manifest_id]"})
    replacement_agency_id_list_manifest: Optional["AgencyIdListManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdListManifest.replacement_agency_id_list_manifest_id]", "remote_side": "[AgencyIdListManifest.agency_id_list_manifest_id]"})
    prev_agency_id_list_manifest: Optional["AgencyIdListManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdListManifest.prev_agency_id_list_manifest_id]", "remote_side": "[AgencyIdListManifest.agency_id_list_manifest_id]"})
    next_agency_id_list_manifest: Optional["AgencyIdListManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdListManifest.next_agency_id_list_manifest_id]", "remote_side": "[AgencyIdListManifest.agency_id_list_manifest_id]"})

class AgencyIdListManifestRead(AgencyIdListManifestBase):
    """Model for reading AgencyIdListManifest data."""
    agency_id_list_manifest_id: int

class AgencyIdListValueBase(SQLModel):
    """Base model for AgencyIdListValue with common fields."""
    guid: str = Field(max_length=32, description="A unique identifier within the release (GUID).")
    value: str = Field(max_length=150, description="A value in the agency identification list.")
    name: Optional[str] = Field(default=None, max_length=150, description="Descriptive or short name of the value.")
    definition: Optional[str] = Field(default=None, description="The meaning of the value.")
    definition_source: Optional[str] = Field(default=None, max_length=100, description="URL which indicates the source of the agency id list value DEFINITION.")
    owner_list_id: int = Field(foreign_key="agency_id_list.agency_id_list_id", description="Foreign key to the agency identification list in the AGENCY_ID_LIST table this value belongs to.")
    based_agency_id_list_value_id: Optional[int] = Field(default=None, foreign_key="agency_id_list_value.agency_id_list_value_id", description="Foreign key to the AGENCY_ID_LIST_VALUE table itself.")
    is_deprecated: bool = Field(default=False, description="Indicates whether the code list value is deprecated and should not be reused.")
    is_developer_default: bool = Field(default=False, description="Indicates whether this agency ID list value can be used as the default for components referenced by developers.")
    is_user_default: bool = Field(default=False, description="Indicates whether this agency ID list value can be used as the default for components referenced by users.")
    replacement_agency_id_list_value_id: Optional[int] = Field(default=None, foreign_key="agency_id_list_value.agency_id_list_value_id", description="This refers to a replacement if the record is deprecated.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. It indicates the user who created the code list.")
    owner_user_id: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. This is the user who owns the entity.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. It identifies the user who last updated the code list.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the code list was created.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the code list was last updated.")
    prev_agency_id_list_value_id: Optional[int] = Field(default=None, foreign_key="agency_id_list_value.agency_id_list_value_id", description="A self-foreign key to indicate the previous history record.")
    next_agency_id_list_value_id: Optional[int] = Field(default=None, foreign_key="agency_id_list_value.agency_id_list_value_id", description="A self-foreign key to indicate the next history record.")
    
    @field_validator('guid')
    @classmethod
    def validate_guid(cls, v):
        if not validate_guid(v):
            raise ValueError("Guid must be a 32-character hexadecimal string (lowercase).")
        return v

class AgencyIdListValue(AgencyIdListValueBase, table=True):
    """Model for the agency_id_list_value table."""
    __tablename__ = "agency_id_list_value"
    
    agency_id_list_value_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key column.")
    
    # Relationships
    owner_list: "AgencyIdList" = Relationship(back_populates="agency_id_list_values", sa_relationship_kwargs={"foreign_keys": "[AgencyIdListValue.owner_list_id]"})
    based_agency_id_list_value: Optional["AgencyIdListValue"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdListValue.based_agency_id_list_value_id]", "remote_side": "[AgencyIdListValue.agency_id_list_value_id]"})
    replacement_agency_id_list_value: Optional["AgencyIdListValue"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdListValue.replacement_agency_id_list_value_id]", "remote_side": "[AgencyIdListValue.agency_id_list_value_id]"})
    prev_agency_id_list_value: Optional["AgencyIdListValue"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdListValue.prev_agency_id_list_value_id]", "remote_side": "[AgencyIdListValue.agency_id_list_value_id]"})
    next_agency_id_list_value: Optional["AgencyIdListValue"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdListValue.next_agency_id_list_value_id]", "remote_side": "[AgencyIdListValue.agency_id_list_value_id]"})
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdListValue.created_by]"})
    owner: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdListValue.owner_user_id]"})
    last_updater: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdListValue.last_updated_by]"})

class AgencyIdListValueRead(AgencyIdListValueBase):
    """Model for reading AgencyIdListValue data."""
    agency_id_list_value_id: int

class AgencyIdListValueManifestBase(SQLModel):
    """Base model for AgencyIdListValueManifest with common fields."""
    release_id: int = Field(foreign_key="release.release_id", description="Foreign key to the release table.")
    agency_id_list_value_id: int = Field(foreign_key="agency_id_list_value.agency_id_list_value_id", description="Foreign key to the agency_id_list_value table.")
    agency_id_list_manifest_id: int = Field(foreign_key="agency_id_list_manifest.agency_id_list_manifest_id", description="Foreign key to the agency_id_list_manifest table.")
    based_agency_id_list_value_manifest_id: Optional[int] = Field(default=None, foreign_key="agency_id_list_value_manifest.agency_id_list_value_manifest_id", description="Foreign key to the agency_id_list_value_manifest table.")
    conflict: bool = Field(default=False, description="This indicates that there is a conflict between self and relationship.")
    replacement_agency_id_list_value_manifest_id: Optional[int] = Field(default=None, foreign_key="agency_id_list_value_manifest.agency_id_list_value_manifest_id", description="This refers to a replacement manifest if the record is deprecated.")
    prev_agency_id_list_value_manifest_id: Optional[int] = Field(default=None, foreign_key="agency_id_list_value_manifest.agency_id_list_value_manifest_id", description="Previous manifest ID.")
    next_agency_id_list_value_manifest_id: Optional[int] = Field(default=None, foreign_key="agency_id_list_value_manifest.agency_id_list_value_manifest_id", description="Next manifest ID.")

class AgencyIdListValueManifest(AgencyIdListValueManifestBase, table=True):
    """Model for the agency_id_list_value_manifest table."""
    __tablename__ = "agency_id_list_value_manifest"
    
    agency_id_list_value_manifest_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key for agency_id_list_value_manifest.")
    
    # Relationships
    release: "Release" = Relationship()
    agency_id_list_value: "AgencyIdListValue" = Relationship()
    agency_id_list_manifest: "AgencyIdListManifest" = Relationship(back_populates="agency_id_list_value_manifests", sa_relationship_kwargs={"foreign_keys": "[AgencyIdListValueManifest.agency_id_list_manifest_id]"})
    based_agency_id_list_value_manifest: Optional["AgencyIdListValueManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdListValueManifest.based_agency_id_list_value_manifest_id]", "remote_side": "[AgencyIdListValueManifest.agency_id_list_value_manifest_id]"})
    replacement_agency_id_list_value_manifest: Optional["AgencyIdListValueManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdListValueManifest.replacement_agency_id_list_value_manifest_id]", "remote_side": "[AgencyIdListValueManifest.agency_id_list_value_manifest_id]"})
    prev_agency_id_list_value_manifest: Optional["AgencyIdListValueManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdListValueManifest.prev_agency_id_list_value_manifest_id]", "remote_side": "[AgencyIdListValueManifest.agency_id_list_value_manifest_id]"})
    next_agency_id_list_value_manifest: Optional["AgencyIdListValueManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AgencyIdListValueManifest.next_agency_id_list_value_manifest_id]", "remote_side": "[AgencyIdListValueManifest.agency_id_list_value_manifest_id]"})

class AgencyIdListValueManifestRead(AgencyIdListValueManifestBase):
    """Model for reading AgencyIdListValueManifest data."""
    agency_id_list_value_manifest_id: int

# BIE (Business Information Entity) Models
