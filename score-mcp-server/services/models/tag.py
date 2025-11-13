from datetime import datetime, timezone
from typing import Optional, List
from sqlmodel import SQLModel, Field, Relationship

from services.models import AppUser


class TagBase(SQLModel):
    """Base model for Tag with common fields."""
    name: str = Field(max_length=100, description="Name of the tag.")
    description: Optional[str] = Field(default=None, description="Description of the tag.")
    text_color: Optional[str] = Field(default=None, max_length=50, description="Text color for the tag.")
    background_color: Optional[str] = Field(default=None, max_length=50, description="Background color for the tag.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the user who creates the tag.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the last user who has updated the tag record.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the tag was created.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="The timestamp when the tag was last updated.")

class Tag(TagBase, table=True):
    """Model for the tag table."""
    __tablename__ = "tag"
    
    tag_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key for tag.")
    
    # Relationships
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Tag.created_by]"})
    last_updater: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Tag.last_updated_by]"})
    acc_manifest_tags: List["AccManifestTag"] = Relationship(back_populates="tag")
    bccp_manifest_tags: List["BccpManifestTag"] = Relationship(back_populates="tag")
    asccp_manifest_tags: List["AsccpManifestTag"] = Relationship(back_populates="tag")
    dt_manifest_tags: List["DtManifestTag"] = Relationship(back_populates="tag")

class TagRead(TagBase):
    """Model for reading Tag data."""
    tag_id: int

# Manifest Tag Models

class AccManifestTagBase(SQLModel):
    """Base model for AccManifestTag with common fields."""
    acc_manifest_id: int = Field(foreign_key="acc_manifest.acc_manifest_id", description="Foreign key to the acc_manifest table.")
    tag_id: int = Field(foreign_key="tag.tag_id", description="Foreign key to the tag table.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the user who creates the record.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the record was first created.")

class AccManifestTag(AccManifestTagBase, table=True):
    """Model for the acc_manifest_tag table."""
    __tablename__ = "acc_manifest_tag"
    
    # Composite primary key
    acc_manifest_id: int = Field(primary_key=True, foreign_key="acc_manifest.acc_manifest_id")
    tag_id: int = Field(primary_key=True, foreign_key="tag.tag_id")
    
    # Relationships
    acc_manifest: "AccManifest" = Relationship()
    tag: "Tag" = Relationship(back_populates="acc_manifest_tags")
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[AccManifestTag.created_by]"})

class AccManifestTagRead(AccManifestTagBase):
    """Model for reading AccManifestTag data."""
    pass

class BccpManifestTagBase(SQLModel):
    """Base model for BccpManifestTag with common fields."""
    bccp_manifest_id: int = Field(foreign_key="bccp_manifest.bccp_manifest_id", description="Foreign key to the bccp_manifest table.")
    tag_id: int = Field(foreign_key="tag.tag_id", description="Foreign key to the tag table.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the user who creates the record.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the record was first created.")

class BccpManifestTag(BccpManifestTagBase, table=True):
    """Model for the bccp_manifest_tag table."""
    __tablename__ = "bccp_manifest_tag"
    
    # Composite primary key
    bccp_manifest_id: int = Field(primary_key=True, foreign_key="bccp_manifest.bccp_manifest_id")
    tag_id: int = Field(primary_key=True, foreign_key="tag.tag_id")
    
    # Relationships
    bccp_manifest: "BccpManifest" = Relationship()
    tag: "Tag" = Relationship(back_populates="bccp_manifest_tags")
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[BccpManifestTag.created_by]"})

class BccpManifestTagRead(BccpManifestTagBase):
    """Model for reading BccpManifestTag data."""
    pass

class AsccpManifestTagBase(SQLModel):
    """Base model for AsccpManifestTag with common fields."""
    asccp_manifest_id: int = Field(foreign_key="asccp_manifest.asccp_manifest_id", description="Foreign key to the asccp_manifest table.")
    tag_id: int = Field(foreign_key="tag.tag_id", description="Foreign key to the tag table.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the user who creates the record.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the record was first created.")

class AsccpManifestTag(AsccpManifestTagBase, table=True):
    """Model for the asccp_manifest_tag table."""
    __tablename__ = "asccp_manifest_tag"
    
    # Composite primary key
    asccp_manifest_id: int = Field(primary_key=True, foreign_key="asccp_manifest.asccp_manifest_id")
    tag_id: int = Field(primary_key=True, foreign_key="tag.tag_id")
    
    # Relationships
    asccp_manifest: "AsccpManifest" = Relationship()
    tag: "Tag" = Relationship(back_populates="asccp_manifest_tags")
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[AsccpManifestTag.created_by]"})

class AsccpManifestTagRead(AsccpManifestTagBase):
    """Model for reading AsccpManifestTag data."""
    pass

class DtManifestTagBase(SQLModel):
    """Base model for DtManifestTag with common fields."""
    dt_manifest_id: int = Field(foreign_key="dt_manifest.dt_manifest_id", description="Foreign key to the dt_manifest table.")
    tag_id: int = Field(foreign_key="tag.tag_id", description="Foreign key to the tag table.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the user who creates the record.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the record was first created.")

class DtManifestTag(DtManifestTagBase, table=True):
    """Model for the dt_manifest_tag table."""
    __tablename__ = "dt_manifest_tag"
    
    # Composite primary key
    dt_manifest_id: int = Field(primary_key=True, foreign_key="dt_manifest.dt_manifest_id")
    tag_id: int = Field(primary_key=True, foreign_key="tag.tag_id")
    
    # Relationships
    dt_manifest: "DtManifest" = Relationship()
    tag: "Tag" = Relationship(back_populates="dt_manifest_tags")
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[DtManifestTag.created_by]"})

class DtManifestTagRead(DtManifestTagBase):
    """Model for reading DtManifestTag data."""
    pass

# Log Model (referenced by manifests)
