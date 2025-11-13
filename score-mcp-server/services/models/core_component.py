from datetime import datetime, timezone
from typing import Optional
from sqlmodel import SQLModel, Field, Relationship
from pydantic import field_validator
from services.utils import validate_guid

from services.models.app_user import AppUser
from services.models.namespace import Namespace
from services.models.release import Release

class AccBase(SQLModel):
    """Base model for ACC with common fields."""
    guid: str = Field(max_length=32, description="A globally unique identifier (GUID).")
    type: str = Field(default="Default", max_length=32, description="The Type of the ACC. List: Default, Extension, AllExtension.")
    object_class_term: str = Field(max_length=100, description="Object class name of the ACC concept.")
    definition: Optional[str] = Field(default=None, description="Documentation or description of the ACC.")
    definition_source: Optional[str] = Field(default=None, max_length=100, description="URL identifying the source of the DEFINITION column.")
    based_acc_id: Optional[int] = Field(default=None, foreign_key="acc.acc_id", description="Foreign key to the ACC table itself. It represents the ACC that is qualified by this ACC.")
    object_class_qualifier: Optional[str] = Field(default=None, max_length=100, description="Qualifier of an ACC, particularly when it has a based ACC.")
    oagis_component_type: Optional[int] = Field(default=None, description="The value can be 0 = BASE, 1 = SEMANTICS, 2 = EXTENSION, 3 = SEMANTIC_GROUP, 4 = USER_EXTENSION_GROUP, 5 = EMBEDDED.")
    namespace_id: Optional[int] = Field(default=None, foreign_key="namespace.namespace_id", description="Foreign key to the NAMESPACE table.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the user who creates the entity.")
    owner_user_id: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. This is the user who owns the entity.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the last user who updated the record.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the revision of the ACC was created.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="The timestamp when the record was last updated.")
    state: Optional[str] = Field(default=None, max_length=20, description="Deleted, WIP, Draft, QA, Candidate, Production, Release Draft, Published.")
    is_deprecated: bool = Field(default=False, description="Indicates whether the CC is deprecated and should not be reused.")
    replacement_acc_id: Optional[int] = Field(default=None, foreign_key="acc.acc_id", description="This refers to a replacement if the record is deprecated.")
    is_abstract: bool = Field(default=False, description="This is the XML Schema abstract flag.")
    prev_acc_id: Optional[int] = Field(default=None, foreign_key="acc.acc_id", description="A self-foreign key to indicate the previous history record.")
    next_acc_id: Optional[int] = Field(default=None, foreign_key="acc.acc_id", description="A self-foreign key to indicate the next history record.")
    
    @field_validator('guid')
    @classmethod
    def validate_guid(cls, v):
        if not validate_guid(v):
            raise ValueError("Guid must be a 32-character hexadecimal string (lowercase).")
        return v

class Acc(AccBase, table=True):
    """Model for the acc table."""
    __tablename__ = "acc"
    
    acc_id: Optional[int] = Field(default=None, primary_key=True, description="A internal, primary database key of an ACC.")
    
    # Relationships
    namespace: Optional["Namespace"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Acc.namespace_id]"})
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Acc.created_by]"})
    owner: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Acc.owner_user_id]"})
    last_updater: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Acc.last_updated_by]"})
    based_acc: Optional["Acc"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Acc.based_acc_id]", "remote_side": "[Acc.acc_id]"})
    replacement_acc: Optional["Acc"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Acc.replacement_acc_id]", "remote_side": "[Acc.acc_id]"})
    prev_acc: Optional["Acc"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Acc.prev_acc_id]", "remote_side": "[Acc.acc_id]"})
    next_acc: Optional["Acc"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Acc.next_acc_id]", "remote_side": "[Acc.acc_id]"})

class AccRead(AccBase):
    """Model for reading Acc data."""
    acc_id: int

class AccManifestBase(SQLModel):
    """Base model for AccManifest with common fields."""
    release_id: int = Field(foreign_key="release.release_id", description="Foreign key to the release table.")
    acc_id: int = Field(foreign_key="acc.acc_id", description="Foreign key to the acc table.")
    based_acc_manifest_id: Optional[int] = Field(default=None, foreign_key="acc_manifest.acc_manifest_id", description="Foreign key to the acc_manifest table.")
    den: str = Field(max_length=200, description="DEN (dictionary entry name) of the ACC.")
    conflict: bool = Field(default=False, description="This indicates that there is a conflict between self and relationship.")
    log_id: Optional[int] = Field(default=None, foreign_key="log.log_id", description="A foreign key pointed to a log for the current record.")
    replacement_acc_manifest_id: Optional[int] = Field(default=None, foreign_key="acc_manifest.acc_manifest_id", description="This refers to a replacement manifest if the record is deprecated.")
    prev_acc_manifest_id: Optional[int] = Field(default=None, foreign_key="acc_manifest.acc_manifest_id", description="Previous manifest ID.")
    next_acc_manifest_id: Optional[int] = Field(default=None, foreign_key="acc_manifest.acc_manifest_id", description="Next manifest ID.")

class AccManifest(AccManifestBase, table=True):
    """Model for the acc_manifest table."""
    __tablename__ = "acc_manifest"
    
    acc_manifest_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key for acc_manifest.")
    
    # Relationships
    release: "Release" = Relationship()
    acc: "Acc" = Relationship()
    log: Optional["Log"] = Relationship(back_populates="acc_manifests")
    based_acc_manifest: Optional["AccManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AccManifest.based_acc_manifest_id]", "remote_side": "[AccManifest.acc_manifest_id]"})
    replacement_acc_manifest: Optional["AccManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AccManifest.replacement_acc_manifest_id]", "remote_side": "[AccManifest.acc_manifest_id]"})
    prev_acc_manifest: Optional["AccManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AccManifest.prev_acc_manifest_id]", "remote_side": "[AccManifest.acc_manifest_id]"})
    next_acc_manifest: Optional["AccManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AccManifest.next_acc_manifest_id]", "remote_side": "[AccManifest.acc_manifest_id]"})

class AccManifestRead(AccManifestBase):
    """Model for reading AccManifest data."""
    acc_manifest_id: int

# ASCC (Association Core Component) Models

class AsccBase(SQLModel):
    """Base model for ASCC with common fields."""
    guid: str = Field(max_length=32, description="A globally unique identifier (GUID).")
    cardinality_min: int = Field(description="Minimum occurrence of the TO_ASCCP_ID. The valid values are non-negative integer.")
    cardinality_max: int = Field(description="Maximum cardinality of the TO_ASCCP_ID. A valid value is integer -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.")
    seq_key: Optional[int] = Field(default=None, description="@deprecated since 2.0.0. This indicates the order of the associations among other siblings.")
    from_acc_id: int = Field(foreign_key="acc.acc_id", description="FROM_ACC_ID is a foreign key pointing to an ACC record.")
    to_asccp_id: int = Field(foreign_key="asccp.asccp_id", description="TO_ASCCP_ID is a foreign key to an ASCCP table record.")
    definition: Optional[str] = Field(default=None, description="Documentation or description of the ASCC.")
    definition_source: Optional[str] = Field(default=None, max_length=100, description="URL identifying the source of the DEFINITION column.")
    is_deprecated: bool = Field(default=False, description="Indicates whether the CC is deprecated and should not be reused.")
    replacement_ascc_id: Optional[int] = Field(default=None, foreign_key="ascc.ascc_id", description="This refers to a replacement if the record is deprecated.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the user who creates the entity.")
    owner_user_id: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. This is the user who owns the entity.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the last user who has updated the record.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the revision of the ASCC was created.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="The timestamp when the record was last updated.")
    state: Optional[str] = Field(default=None, max_length=20, description="Deleted, WIP, Draft, QA, Candidate, Production, Release Draft, Published.")
    prev_ascc_id: Optional[int] = Field(default=None, foreign_key="ascc.ascc_id", description="A self-foreign key to indicate the previous history record.")
    next_ascc_id: Optional[int] = Field(default=None, foreign_key="ascc.ascc_id", description="A self-foreign key to indicate the next history record.")
    
    @field_validator('guid')
    @classmethod
    def validate_guid(cls, v):
        if not validate_guid(v):
            raise ValueError("Guid must be a 32-character hexadecimal string (lowercase).")
        return v

class Ascc(AsccBase, table=True):
    """Model for the ascc table."""
    __tablename__ = "ascc"
    
    ascc_id: Optional[int] = Field(default=None, primary_key=True, description="An internal, primary database key of an ASCC.")
    
    # Relationships
    from_acc: "Acc" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Ascc.from_acc_id]"})
    to_asccp: "Asccp" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Ascc.to_asccp_id]"})
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Ascc.created_by]"})
    owner: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Ascc.owner_user_id]"})
    last_updater: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Ascc.last_updated_by]"})
    replacement_ascc: Optional["Ascc"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Ascc.replacement_ascc_id]", "remote_side": "[Ascc.ascc_id]"})
    prev_ascc: Optional["Ascc"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Ascc.prev_ascc_id]", "remote_side": "[Ascc.ascc_id]"})
    next_ascc: Optional["Ascc"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Ascc.next_ascc_id]", "remote_side": "[Ascc.ascc_id]"})

class AsccRead(AsccBase):
    """Model for reading Ascc data."""
    ascc_id: int

class AsccManifestBase(SQLModel):
    """Base model for AsccManifest with common fields."""
    release_id: Optional[int] = Field(default=None, foreign_key="release.release_id", description="Foreign key to the release table.")
    ascc_id: int = Field(foreign_key="ascc.ascc_id", description="Foreign key to the ascc table.")
    seq_key_id: Optional[int] = Field(default=None, foreign_key="seq_key.seq_key_id", description="Foreign key to the seq_key table.")
    from_acc_manifest_id: int = Field(foreign_key="acc_manifest.acc_manifest_id", description="Foreign key to the acc_manifest table.")
    to_asccp_manifest_id: int = Field(foreign_key="asccp_manifest.asccp_manifest_id", description="Foreign key to the asccp_manifest table.")
    den: str = Field(max_length=304, description="DEN (dictionary entry name) of the ASCC.")
    conflict: bool = Field(default=False, description="This indicates that there is a conflict between self and relationship.")
    replacement_ascc_manifest_id: Optional[int] = Field(default=None, foreign_key="ascc_manifest.ascc_manifest_id", description="This refers to a replacement manifest if the record is deprecated.")
    prev_ascc_manifest_id: Optional[int] = Field(default=None, foreign_key="ascc_manifest.ascc_manifest_id", description="Previous manifest ID.")
    next_ascc_manifest_id: Optional[int] = Field(default=None, foreign_key="ascc_manifest.ascc_manifest_id", description="Next manifest ID.")

class AsccManifest(AsccManifestBase, table=True):
    """Model for the ascc_manifest table."""
    __tablename__ = "ascc_manifest"
    
    ascc_manifest_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key for ascc_manifest.")
    
    # Relationships
    release: Optional["Release"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AsccManifest.release_id]"})
    ascc: "Ascc" = Relationship()
    seq_key: Optional["SeqKey"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AsccManifest.seq_key_id]"})
    from_acc_manifest: "AccManifest" = Relationship(sa_relationship_kwargs={"foreign_keys": "[AsccManifest.from_acc_manifest_id]"})
    to_asccp_manifest: "AsccpManifest" = Relationship(sa_relationship_kwargs={"foreign_keys": "[AsccManifest.to_asccp_manifest_id]"})
    replacement_ascc_manifest: Optional["AsccManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AsccManifest.replacement_ascc_manifest_id]", "remote_side": "[AsccManifest.ascc_manifest_id]"})
    prev_ascc_manifest: Optional["AsccManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AsccManifest.prev_ascc_manifest_id]", "remote_side": "[AsccManifest.ascc_manifest_id]"})
    next_ascc_manifest: Optional["AsccManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AsccManifest.next_ascc_manifest_id]", "remote_side": "[AsccManifest.ascc_manifest_id]"})

class AsccManifestRead(AsccManifestBase):
    """Model for reading AsccManifest data."""
    ascc_manifest_id: int

# BCC (Basic Core Component) Models

class BccBase(SQLModel):
    """Base model for BCC with common fields."""
    guid: str = Field(max_length=32, description="A globally unique identifier (GUID).")
    cardinality_min: int = Field(description="Minimum cardinality of the TO_BCCP_ID. The valid values are non-negative integer.")
    cardinality_max: Optional[int] = Field(default=None, description="Maximum cardinality of the TO_BCCP_ID. The valid values are integer -1 and up. Specifically, -1 means unbounded. 0 means prohibited or not to use.")
    to_bccp_id: int = Field(foreign_key="bccp.bccp_id", description="TO_BCCP_ID is a foreign key to an BCCP table record.")
    from_acc_id: int = Field(foreign_key="acc.acc_id", description="FROM_ACC_ID is a foreign key pointing to an ACC record.")
    seq_key: Optional[int] = Field(default=None, description="@deprecated since 2.0.0. This indicates the order of the associations among other siblings.")
    entity_type: Optional[int] = Field(default=None, description="This is a code list: 0 = ATTRIBUTE and 1 = ELEMENT.")
    definition: Optional[str] = Field(default=None, description="Documentation or description of the BCC.")
    definition_source: Optional[str] = Field(default=None, max_length=100, description="URL identifying the source of the DEFINITION column.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the user who creates the entity.")
    owner_user_id: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. This is the user who owns the entity.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the last user who has updated the record.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the revision of the BCC was created.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="The timestamp when the record was last updated.")
    state: Optional[str] = Field(default=None, max_length=20, description="Deleted, WIP, Draft, QA, Candidate, Production, Release Draft, Published.")
    is_deprecated: bool = Field(default=False, description="Indicates whether the CC is deprecated and should not be reused.")
    replacement_bcc_id: Optional[int] = Field(default=None, foreign_key="bcc.bcc_id", description="This refers to a replacement if the record is deprecated.")
    is_nillable: bool = Field(default=False, description="@deprecated since 2.0.0 in favor of impossibility of nillable association (element reference) in XML schema.")
    default_value: Optional[str] = Field(default=None, description="This set the default value at the association level.")
    fixed_value: Optional[str] = Field(default=None, description="This column captures the fixed value constraint.")
    prev_bcc_id: Optional[int] = Field(default=None, foreign_key="bcc.bcc_id", description="A self-foreign key to indicate the previous history record.")
    next_bcc_id: Optional[int] = Field(default=None, foreign_key="bcc.bcc_id", description="A self-foreign key to indicate the next history record.")
    
    @field_validator('guid')
    @classmethod
    def validate_guid(cls, v):
        if not validate_guid(v):
            raise ValueError("Guid must be a 32-character hexadecimal string (lowercase).")
        return v

class Bcc(BccBase, table=True):
    """Model for the bcc table."""
    __tablename__ = "bcc"
    
    bcc_id: Optional[int] = Field(default=None, primary_key=True, description="A internal, primary database key of an BCC.")
    
    # Relationships
    to_bccp: "Bccp" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bcc.to_bccp_id]"})
    from_acc: "Acc" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bcc.from_acc_id]"})
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bcc.created_by]"})
    owner: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bcc.owner_user_id]"})
    last_updater: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bcc.last_updated_by]"})
    replacement_bcc: Optional["Bcc"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bcc.replacement_bcc_id]", "remote_side": "[Bcc.bcc_id]"})
    prev_bcc: Optional["Bcc"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bcc.prev_bcc_id]", "remote_side": "[Bcc.bcc_id]"})
    next_bcc: Optional["Bcc"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bcc.next_bcc_id]", "remote_side": "[Bcc.bcc_id]"})

class BccRead(BccBase):
    """Model for reading Bcc data."""
    bcc_id: int

class BccManifestBase(SQLModel):
    """Base model for BccManifest with common fields."""
    release_id: Optional[int] = Field(default=None, foreign_key="release.release_id", description="Foreign key to the release table.")
    bcc_id: int = Field(foreign_key="bcc.bcc_id", description="Foreign key to the bcc table.")
    seq_key_id: Optional[int] = Field(default=None, foreign_key="seq_key.seq_key_id", description="Foreign key to the seq_key table.")
    from_acc_manifest_id: int = Field(foreign_key="acc_manifest.acc_manifest_id", description="Foreign key to the acc_manifest table.")
    to_bccp_manifest_id: int = Field(foreign_key="bccp_manifest.bccp_manifest_id", description="Foreign key to the bccp_manifest table.")
    den: str = Field(max_length=351, description="DEN (dictionary entry name) of the BCC.")
    conflict: bool = Field(default=False, description="This indicates that there is a conflict between self and relationship.")
    replacement_bcc_manifest_id: Optional[int] = Field(default=None, foreign_key="bcc_manifest.bcc_manifest_id", description="This refers to a replacement manifest if the record is deprecated.")
    prev_bcc_manifest_id: Optional[int] = Field(default=None, foreign_key="bcc_manifest.bcc_manifest_id", description="Previous manifest ID.")
    next_bcc_manifest_id: Optional[int] = Field(default=None, foreign_key="bcc_manifest.bcc_manifest_id", description="Next manifest ID.")

class BccManifest(BccManifestBase, table=True):
    """Model for the bcc_manifest table."""
    __tablename__ = "bcc_manifest"
    
    bcc_manifest_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key for bcc_manifest.")
    
    # Relationships
    release: Optional["Release"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[BccManifest.release_id]"})
    bcc: "Bcc" = Relationship()
    seq_key: Optional["SeqKey"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[BccManifest.seq_key_id]"})
    from_acc_manifest: "AccManifest" = Relationship(sa_relationship_kwargs={"foreign_keys": "[BccManifest.from_acc_manifest_id]"})
    to_bccp_manifest: "BccpManifest" = Relationship(sa_relationship_kwargs={"foreign_keys": "[BccManifest.to_bccp_manifest_id]"})
    replacement_bcc_manifest: Optional["BccManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[BccManifest.replacement_bcc_manifest_id]", "remote_side": "[BccManifest.bcc_manifest_id]"})
    prev_bcc_manifest: Optional["BccManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[BccManifest.prev_bcc_manifest_id]", "remote_side": "[BccManifest.bcc_manifest_id]"})
    next_bcc_manifest: Optional["BccManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[BccManifest.next_bcc_manifest_id]", "remote_side": "[BccManifest.bcc_manifest_id]"})

class BccManifestRead(BccManifestBase):
    """Model for reading BccManifest data."""
    bcc_manifest_id: int

# BCCP (Basic Core Component Property) Models

class BccpBase(SQLModel):
    """Base model for BCCP with common fields."""
    guid: str = Field(max_length=32, description="A globally unique identifier (GUID).")
    property_term: str = Field(max_length=100, description="The property concept that the BCCP models.")
    representation_term: str = Field(max_length=20, description="The representation term convey the format of the data the BCCP can take.")
    bdt_id: int = Field(foreign_key="dt.dt_id", description="Foreign key pointing to the DT table indicating the data type or data format of the BCCP.")
    definition: Optional[str] = Field(default=None, description="Description of the BCCP.")
    definition_source: Optional[str] = Field(default=None, max_length=100, description="URL identifying the source of the DEFINITION column.")
    namespace_id: Optional[int] = Field(default=None, foreign_key="namespace.namespace_id", description="Foreign key to the NAMESPACE table.")
    is_deprecated: bool = Field(default=False, description="Indicates whether the CC is deprecated and should not be reused.")
    replacement_bccp_id: Optional[int] = Field(default=None, foreign_key="bccp.bccp_id", description="This refers to a replacement if the record is deprecated.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the user who creates the entity.")
    owner_user_id: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. This is the user who owns the entity.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the last user who has updated the record.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the revision of the BCCP was created.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="The timestamp when the record was last updated.")
    state: Optional[str] = Field(default=None, max_length=20, description="Deleted, WIP, Draft, QA, Candidate, Production, Release Draft, Published.")
    is_nillable: bool = Field(default=False, description="This is corresponding to the XML Schema nillable flag.")
    default_value: Optional[str] = Field(default=None, description="This column specifies the default value constraint.")
    fixed_value: Optional[str] = Field(default=None, description="This column captures the fixed value constraint.")
    prev_bccp_id: Optional[int] = Field(default=None, foreign_key="bccp.bccp_id", description="A self-foreign key to indicate the previous history record.")
    next_bccp_id: Optional[int] = Field(default=None, foreign_key="bccp.bccp_id", description="A self-foreign key to indicate the next history record.")
    
    @field_validator('guid')
    @classmethod
    def validate_guid(cls, v):
        if not validate_guid(v):
            raise ValueError("Guid must be a 32-character hexadecimal string (lowercase).")
        return v

class Bccp(BccpBase, table=True):
    """Model for the bccp table."""
    __tablename__ = "bccp"
    
    bccp_id: Optional[int] = Field(default=None, primary_key=True, description="An internal, primary database key.")
    
    # Relationships
    bdt: "Dt" = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bccp.bdt_id]"})
    namespace: Optional["Namespace"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bccp.namespace_id]"})
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bccp.created_by]"})
    owner: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bccp.owner_user_id]"})
    last_updater: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bccp.last_updated_by]"})
    replacement_bccp: Optional["Bccp"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bccp.replacement_bccp_id]", "remote_side": "[Bccp.bccp_id]"})
    prev_bccp: Optional["Bccp"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bccp.prev_bccp_id]", "remote_side": "[Bccp.bccp_id]"})
    next_bccp: Optional["Bccp"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Bccp.next_bccp_id]", "remote_side": "[Bccp.bccp_id]"})

class BccpRead(BccpBase):
    """Model for reading Bccp data."""
    bccp_id: int

class BccpManifestBase(SQLModel):
    """Base model for BccpManifest with common fields."""
    release_id: int = Field(foreign_key="release.release_id", description="Foreign key to the release table.")
    bccp_id: int = Field(foreign_key="bccp.bccp_id", description="Foreign key to the bccp table.")
    bdt_manifest_id: int = Field(foreign_key="dt_manifest.dt_manifest_id", description="Foreign key to the dt_manifest table.")
    den: str = Field(max_length=249, description="The dictionary entry name of the BCCP.")
    conflict: bool = Field(default=False, description="This indicates that there is a conflict between self and relationship.")
    log_id: Optional[int] = Field(default=None, foreign_key="log.log_id", description="A foreign key pointed to a log for the current record.")
    replacement_bccp_manifest_id: Optional[int] = Field(default=None, foreign_key="bccp_manifest.bccp_manifest_id", description="This refers to a replacement manifest if the record is deprecated.")
    prev_bccp_manifest_id: Optional[int] = Field(default=None, foreign_key="bccp_manifest.bccp_manifest_id", description="Previous manifest ID.")
    next_bccp_manifest_id: Optional[int] = Field(default=None, foreign_key="bccp_manifest.bccp_manifest_id", description="Next manifest ID.")

class BccpManifest(BccpManifestBase, table=True):
    """Model for the bccp_manifest table."""
    __tablename__ = "bccp_manifest"
    
    bccp_manifest_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key for bccp_manifest.")
    
    # Relationships
    release: "Release" = Relationship()
    bccp: "Bccp" = Relationship()
    log: Optional["Log"] = Relationship(back_populates="bccp_manifests")
    bdt_manifest: "DtManifest" = Relationship(sa_relationship_kwargs={"foreign_keys": "[BccpManifest.bdt_manifest_id]"})
    replacement_bccp_manifest: Optional["BccpManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[BccpManifest.replacement_bccp_manifest_id]", "remote_side": "[BccpManifest.bccp_manifest_id]"})
    prev_bccp_manifest: Optional["BccpManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[BccpManifest.prev_bccp_manifest_id]", "remote_side": "[BccpManifest.bccp_manifest_id]"})
    next_bccp_manifest: Optional["BccpManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[BccpManifest.next_bccp_manifest_id]", "remote_side": "[BccpManifest.bccp_manifest_id]"})

class BccpManifestRead(BccpManifestBase):
    """Model for reading BccpManifest data."""
    bccp_manifest_id: int

# ASCCP (Association Core Component Property) Models

class AsccpBase(SQLModel):
    """Base model for ASCCP with common fields."""
    guid: str = Field(max_length=32, description="A globally unique identifier (GUID).")
    type: str = Field(default="Default", max_length=32, description="The Type of the ASCCP. List: Default, Extension")
    property_term: Optional[str] = Field(default=None, max_length=100, description="The role (or property) the ACC as referred to by the Role_Of_ACC_ID play when the ASCCP is used by another ACC.")
    definition: Optional[str] = Field(default=None, description="Description of the ASCCP.")
    definition_source: Optional[str] = Field(default=None, max_length=100, description="URL identifying the source of the DEFINITION column.")
    role_of_acc_id: Optional[int] = Field(default=None, foreign_key="acc.acc_id", description="The ACC from which this ASCCP is created (ASCCP applies role to the ACC).")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the user who creates the entity.")
    owner_user_id: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. This is the user who owns the entity.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the last user who has updated the record.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the revision of the ASCCP was created.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="The timestamp when the record was last updated.")
    state: Optional[str] = Field(default=None, max_length=20, description="Deleted, WIP, Draft, QA, Candidate, Production, Release Draft, Published.")
    namespace_id: Optional[int] = Field(default=None, foreign_key="namespace.namespace_id", description="Foreign key to the Namespace table.")
    reusable_indicator: bool = Field(default=True, description="This indicates whether the ASCCP can be used by more than one ASCC.")
    is_deprecated: bool = Field(default=False, description="Indicates whether the CC is deprecated and should not be reused.")
    replacement_asccp_id: Optional[int] = Field(default=None, foreign_key="asccp.asccp_id", description="This refers to a replacement if the record is deprecated.")
    is_nillable: Optional[bool] = Field(default=None, description="This is corresponding to the XML schema nillable flag.")
    prev_asccp_id: Optional[int] = Field(default=None, foreign_key="asccp.asccp_id", description="A self-foreign key to indicate the previous history record.")
    next_asccp_id: Optional[int] = Field(default=None, foreign_key="asccp.asccp_id", description="A self-foreign key to indicate the next history record.")
    
    @field_validator('guid')
    @classmethod
    def validate_guid(cls, v):
        if not validate_guid(v):
            raise ValueError("Guid must be a 32-character hexadecimal string (lowercase).")
        return v

class Asccp(AsccpBase, table=True):
    """Model for the asccp table."""
    __tablename__ = "asccp"
    
    asccp_id: Optional[int] = Field(default=None, primary_key=True, description="An internal, primary database key of an ASCCP.")
    
    # Relationships
    role_of_acc: Optional["Acc"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Asccp.role_of_acc_id]"})
    namespace: Optional["Namespace"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Asccp.namespace_id]"})
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Asccp.created_by]"})
    owner: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Asccp.owner_user_id]"})
    last_updater: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Asccp.last_updated_by]"})
    replacement_asccp: Optional["Asccp"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Asccp.replacement_asccp_id]", "remote_side": "[Asccp.asccp_id]"})
    prev_asccp: Optional["Asccp"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Asccp.prev_asccp_id]", "remote_side": "[Asccp.asccp_id]"})
    next_asccp: Optional["Asccp"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[Asccp.next_asccp_id]", "remote_side": "[Asccp.asccp_id]"})

class AsccpRead(AsccpBase):
    """Model for reading Asccp data."""
    asccp_id: int

class AsccpManifestBase(SQLModel):
    """Base model for AsccpManifest with common fields."""
    release_id: int = Field(foreign_key="release.release_id", description="Foreign key to the release table.")
    asccp_id: int = Field(foreign_key="asccp.asccp_id", description="Foreign key to the asccp table.")
    role_of_acc_manifest_id: int = Field(foreign_key="acc_manifest.acc_manifest_id", description="Foreign key to the acc_manifest table.")
    den: Optional[str] = Field(default=None, max_length=202, description="The dictionary entry name of the ASCCP.")
    conflict: bool = Field(default=False, description="This indicates that there is a conflict between self and relationship.")
    log_id: Optional[int] = Field(default=None, foreign_key="log.log_id", description="A foreign key pointed to a log for the current record.")
    replacement_asccp_manifest_id: Optional[int] = Field(default=None, foreign_key="asccp_manifest.asccp_manifest_id", description="This refers to a replacement manifest if the record is deprecated.")
    prev_asccp_manifest_id: Optional[int] = Field(default=None, foreign_key="asccp_manifest.asccp_manifest_id", description="Previous manifest ID.")
    next_asccp_manifest_id: Optional[int] = Field(default=None, foreign_key="asccp_manifest.asccp_manifest_id", description="Next manifest ID.")

class AsccpManifest(AsccpManifestBase, table=True):
    """Model for the asccp_manifest table."""
    __tablename__ = "asccp_manifest"
    
    asccp_manifest_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key for asccp_manifest.")
    
    # Relationships
    release: "Release" = Relationship()
    asccp: "Asccp" = Relationship()
    log: Optional["Log"] = Relationship(back_populates="asccp_manifests")
    role_of_acc_manifest: "AccManifest" = Relationship(sa_relationship_kwargs={"foreign_keys": "[AsccpManifest.role_of_acc_manifest_id]"})
    replacement_asccp_manifest: Optional["AsccpManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AsccpManifest.replacement_asccp_manifest_id]", "remote_side": "[AsccpManifest.asccp_manifest_id]"})
    prev_asccp_manifest: Optional["AsccpManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AsccpManifest.prev_asccp_manifest_id]", "remote_side": "[AsccpManifest.asccp_manifest_id]"})
    next_asccp_manifest: Optional["AsccpManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[AsccpManifest.next_asccp_manifest_id]", "remote_side": "[AsccpManifest.asccp_manifest_id]"})

class AsccpManifestRead(AsccpManifestBase):
    """Model for reading AsccpManifest data."""
    asccp_manifest_id: int

# DT (Data Type) Models

class SeqKeyBase(SQLModel):
    """Base model for SeqKey with common fields."""
    from_acc_manifest_id: int = Field(foreign_key="acc_manifest.acc_manifest_id", description="Foreign key to the acc_manifest table.")
    ascc_manifest_id: Optional[int] = Field(default=None, foreign_key="ascc_manifest.ascc_manifest_id", description="Foreign key to the ascc_manifest table.")
    bcc_manifest_id: Optional[int] = Field(default=None, foreign_key="bcc_manifest.bcc_manifest_id", description="Foreign key to the bcc_manifest table.")
    prev_seq_key_id: Optional[int] = Field(default=None, foreign_key="seq_key.seq_key_id", description="Previous sequence key ID.")
    next_seq_key_id: Optional[int] = Field(default=None, foreign_key="seq_key.seq_key_id", description="Next sequence key ID.")

class SeqKey(SeqKeyBase, table=True):
    """Model for the seq_key table."""
    __tablename__ = "seq_key"
    
    seq_key_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key for seq_key.")
    
    # Relationships
    from_acc_manifest: "AccManifest" = Relationship(sa_relationship_kwargs={"foreign_keys": "[SeqKey.from_acc_manifest_id]"})
    ascc_manifest: Optional["AsccManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[SeqKey.ascc_manifest_id]"})
    bcc_manifest: Optional["BccManifest"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[SeqKey.bcc_manifest_id]"})
    prev_seq_key: Optional["SeqKey"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[SeqKey.prev_seq_key_id]", "remote_side": "[SeqKey.seq_key_id]"})
    next_seq_key: Optional["SeqKey"] = Relationship(sa_relationship_kwargs={"foreign_keys": "[SeqKey.next_seq_key_id]", "remote_side": "[SeqKey.seq_key_id]"})

class SeqKeyRead(SeqKeyBase):
    """Model for reading SeqKey data."""
    seq_key_id: int

# Tag Models (for manifest tagging)
