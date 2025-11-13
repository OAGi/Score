from datetime import datetime, timezone
from typing import Optional, List

from pydantic import field_validator
from sqlmodel import SQLModel, Field, Relationship

from services.models.app_user import AppUser
from services.models.namespace import Namespace
from services.models.release import Release
from services.utils import validate_guid

class CodeListBase(SQLModel):
    """Base model for CodeList with common fields."""
    guid: str = Field(max_length=32, description="A unique identifier within the release (GUID).")
    enum_type_guid: Optional[str] = Field(default=None, max_length=41, description="In the OAGIS Model XML schema, a type, which keeps all the enumerated values, is defined separately from the type that represents a code list.")
    name: Optional[str] = Field(default=None, max_length=100, description="Name of the code list.")
    list_id: str = Field(max_length=100, description="External identifier.")
    version_id: str = Field(max_length=100, description="Code list version number.")
    definition: Optional[str] = Field(default=None, description="Description of the code list.")
    remark: Optional[str] = Field(default=None, max_length=225, description="Usage information about the code list.")
    definition_source: Optional[str] = Field(default=None, max_length=100, description="URL which indicates the source of the code list's DEFINITION.")
    namespace_id: Optional[int] = Field(default=None, foreign_key="namespace.namespace_id", description="Foreign key to the NAMESPACE table.")
    based_code_list_id: Optional[int] = Field(default=None, foreign_key="code_list.code_list_id", description="Foreign key to the CODE_LIST table itself.")
    extensible_indicator: bool = Field(description="Flag to indicate whether the code list is final and shall not be further derived.")
    is_deprecated: bool = Field(default=False, description="Indicates whether the code list is deprecated and should not be reused.")
    replacement_code_list_id: Optional[int] = Field(default=None, foreign_key="code_list.code_list_id", description="This refers to a replacement if the record is deprecated.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. It indicates the user who created the code list.")
    owner_user_id: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. This is the user who owns the entity.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. It identifies the user who last updated the code list.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the code list was created.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the code list was last updated.")
    state: Optional[str] = Field(default=None, max_length=20, description="State of the code list.")
    prev_code_list_id: Optional[int] = Field(default=None, foreign_key="code_list.code_list_id", description="A self-foreign key to indicate the previous history record.")
    next_code_list_id: Optional[int] = Field(default=None, foreign_key="code_list.code_list_id", description="A self-foreign key to indicate the next history record.")
    
    @field_validator('guid')
    @classmethod
    def validate_guid(cls, v):
        if not validate_guid(v):
            raise ValueError("Guid must be a 32-character hexadecimal string (lowercase).")
        return v

class CodeList(CodeListBase, table=True):
    """Model for the code_list table."""
    __tablename__ = "code_list"
    
    code_list_id: Optional[int] = Field(default=None, primary_key=True, description="Internal, primary database key.")
    
    # Relationships
    namespace: Optional["Namespace"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeList.namespace_id]"})
    based_code_list: Optional["CodeList"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeList.based_code_list_id]", "remote_side": "[CodeList.code_list_id]"})
    replacement_code_list: Optional["CodeList"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeList.replacement_code_list_id]", "remote_side": "[CodeList.code_list_id]"})
    prev_code_list: Optional["CodeList"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeList.prev_code_list_id]", "remote_side": "[CodeList.code_list_id]"})
    next_code_list: Optional["CodeList"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeList.next_code_list_id]", "remote_side": "[CodeList.code_list_id]"})
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeList.created_by]"})
    owner: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeList.owner_user_id]"})
    last_updater: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeList.last_updated_by]"})
    code_list_values: List["CodeListValue"] = Relationship(back_populates="code_list")

class CodeListRead(CodeListBase):
    """Model for reading CodeList data."""
    code_list_id: int

class CodeListManifestBase(SQLModel):
    """Base model for CodeListManifest with common fields."""
    release_id: int = Field(foreign_key="release.release_id", description="Foreign key to the release table.")
    code_list_id: int = Field(foreign_key="code_list.code_list_id", description="Foreign key to the code_list table.")
    based_code_list_manifest_id: Optional[int] = Field(default=None, foreign_key="code_list_manifest.code_list_manifest_id", description="Foreign key to the code_list_manifest table.")
    agency_id_list_value_manifest_id: Optional[int] = Field(default=None, foreign_key="agency_id_list_value_manifest.agency_id_list_value_manifest_id", description="Foreign key to the agency_id_list_value_manifest table.")
    conflict: bool = Field(default=False, description="This indicates that there is a conflict between self and relationship.")
    log_id: Optional[int] = Field(default=None, foreign_key="log.log_id", description="A foreign key pointed to a log for the current record.")
    replacement_code_list_manifest_id: Optional[int] = Field(default=None, foreign_key="code_list_manifest.code_list_manifest_id", description="This refers to a replacement manifest if the record is deprecated.")
    prev_code_list_manifest_id: Optional[int] = Field(default=None, foreign_key="code_list_manifest.code_list_manifest_id", description="Previous manifest ID.")
    next_code_list_manifest_id: Optional[int] = Field(default=None, foreign_key="code_list_manifest.code_list_manifest_id", description="Next manifest ID.")

class CodeListManifest(CodeListManifestBase, table=True):
    """Model for the code_list_manifest table."""
    __tablename__ = "code_list_manifest"
    
    code_list_manifest_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key for code_list_manifest.")
    
    # Relationships
    release: "Release" = Relationship()
    code_list: "CodeList" = Relationship()
    log: Optional["Log"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeListManifest.log_id]"})
    based_code_list_manifest: Optional["CodeListManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeListManifest.based_code_list_manifest_id]", "remote_side": "[CodeListManifest.code_list_manifest_id]"})
    agency_id_list_value_manifest: Optional["AgencyIdListValueManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeListManifest.agency_id_list_value_manifest_id]"})
    replacement_code_list_manifest: Optional["CodeListManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeListManifest.replacement_code_list_manifest_id]", "remote_side": "[CodeListManifest.code_list_manifest_id]"})
    prev_code_list_manifest: Optional["CodeListManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeListManifest.prev_code_list_manifest_id]", "remote_side": "[CodeListManifest.code_list_manifest_id]"})
    next_code_list_manifest: Optional["CodeListManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeListManifest.next_code_list_manifest_id]", "remote_side": "[CodeListManifest.code_list_manifest_id]"})
    code_list_value_manifests: list["CodeListValueManifest"] = Relationship(back_populates="code_list_manifest")

class CodeListManifestRead(CodeListManifestBase):
    """Model for reading CodeListManifest data."""
    code_list_manifest_id: int

class CodeListValueBase(SQLModel):
    """Base model for CodeListValue with common fields."""
    guid: str = Field(max_length=32, description="A unique identifier within the release (GUID).")
    code_list_id: int = Field(foreign_key="code_list.code_list_id", description="Foreign key to the CODE_LIST table.")
    based_code_list_value_id: Optional[int] = Field(default=None, foreign_key="code_list_value.code_list_value_id", description="Foreign key to the CODE_LIST_VALUE table itself.")
    value: str = Field(description="The code list value used in the instance data.")
    meaning: Optional[str] = Field(default=None, description="The description or explanation of the code list value.")
    definition: Optional[str] = Field(default=None, description="Long description or explanation of the code list value.")
    definition_source: Optional[str] = Field(default=None, max_length=100, description="URL identifying the source of the DEFINITION column.")
    is_deprecated: bool = Field(default=False, description="Indicates whether the code list value is deprecated and should not be reused.")
    replacement_code_list_value_id: Optional[int] = Field(default=None, foreign_key="code_list_value.code_list_value_id", description="This refers to a replacement if the record is deprecated.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. It indicates the user who created the code list.")
    owner_user_id: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. This is the user who owns the entity.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. It identifies the user who last updated the code list.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the code list was created.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the code list was last updated.")
    prev_code_list_value_id: Optional[int] = Field(default=None, foreign_key="code_list_value.code_list_value_id", description="A self-foreign key to indicate the previous history record.")
    next_code_list_value_id: Optional[int] = Field(default=None, foreign_key="code_list_value.code_list_value_id", description="A self-foreign key to indicate the next history record.")
    
    @field_validator('guid')
    @classmethod
    def validate_guid(cls, v):
        if not validate_guid(v):
            raise ValueError("Guid must be a 32-character hexadecimal string (lowercase).")
        return v

class CodeListValue(CodeListValueBase, table=True):
    """Model for the code_list_value table."""
    __tablename__ = "code_list_value"
    
    code_list_value_id: Optional[int] = Field(default=None, primary_key=True, description="Internal, primary database key.")
    
    # Relationships
    code_list: "CodeList" = Relationship(back_populates="code_list_values")
    based_code_list_value: Optional["CodeListValue"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeListValue.based_code_list_value_id]", "remote_side": "[CodeListValue.code_list_value_id]"})
    replacement_code_list_value: Optional["CodeListValue"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeListValue.replacement_code_list_value_id]", "remote_side": "[CodeListValue.code_list_value_id]"})
    prev_code_list_value: Optional["CodeListValue"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeListValue.prev_code_list_value_id]", "remote_side": "[CodeListValue.code_list_value_id]"})
    next_code_list_value: Optional["CodeListValue"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeListValue.next_code_list_value_id]", "remote_side": "[CodeListValue.code_list_value_id]"})
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeListValue.created_by]"})
    owner: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeListValue.owner_user_id]"})
    last_updater: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeListValue.last_updated_by]"})

class CodeListValueRead(CodeListValueBase):
    """Model for reading CodeListValue data."""
    code_list_value_id: int

class CodeListValueManifestBase(SQLModel):
    """Base model for CodeListValueManifest with common fields."""
    release_id: int = Field(foreign_key="release.release_id", description="Foreign key to the release table.")
    code_list_value_id: int = Field(foreign_key="code_list_value.code_list_value_id", description="Foreign key to the code_list_value table.")
    code_list_manifest_id: int = Field(foreign_key="code_list_manifest.code_list_manifest_id", description="Foreign key to the code_list_manifest table.")
    based_code_list_value_manifest_id: Optional[int] = Field(default=None, foreign_key="code_list_value_manifest.code_list_value_manifest_id", description="Foreign key to the code_list_value_manifest table.")
    conflict: bool = Field(default=False, description="This indicates that there is a conflict between self and relationship.")
    replacement_code_list_value_manifest_id: Optional[int] = Field(default=None, foreign_key="code_list_value_manifest.code_list_value_manifest_id", description="This refers to a replacement manifest if the record is deprecated.")
    prev_code_list_value_manifest_id: Optional[int] = Field(default=None, foreign_key="code_list_value_manifest.code_list_value_manifest_id", description="Previous manifest ID.")
    next_code_list_value_manifest_id: Optional[int] = Field(default=None, foreign_key="code_list_value_manifest.code_list_value_manifest_id", description="Next manifest ID.")

class CodeListValueManifest(CodeListValueManifestBase, table=True):
    """Model for the code_list_value_manifest table."""
    __tablename__ = "code_list_value_manifest"
    
    code_list_value_manifest_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key for code_list_value_manifest.")
    
    # Relationships
    release: "Release" = Relationship()
    code_list_value: "CodeListValue" = Relationship()
    code_list_manifest: "CodeListManifest" = Relationship(back_populates="code_list_value_manifests")
    based_code_list_value_manifest: Optional["CodeListValueManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeListValueManifest.based_code_list_value_manifest_id]", "remote_side": "[CodeListValueManifest.code_list_value_manifest_id]"})
    replacement_code_list_value_manifest: Optional["CodeListValueManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeListValueManifest.replacement_code_list_value_manifest_id]", "remote_side": "[CodeListValueManifest.code_list_value_manifest_id]"})
    prev_code_list_value_manifest: Optional["CodeListValueManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeListValueManifest.prev_code_list_value_manifest_id]", "remote_side": "[CodeListValueManifest.code_list_value_manifest_id]"})
    next_code_list_value_manifest: Optional["CodeListValueManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[CodeListValueManifest.next_code_list_value_manifest_id]", "remote_side": "[CodeListValueManifest.code_list_value_manifest_id]"})

class CodeListValueManifestRead(CodeListValueManifestBase):
    """Model for reading CodeListValueManifest data."""
    code_list_value_manifest_id: int

# Agency ID List Models
