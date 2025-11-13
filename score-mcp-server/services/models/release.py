from datetime import datetime, timezone
from typing import Optional, List
from sqlmodel import SQLModel, Field, Relationship
from pydantic import field_validator
from services.utils import validate_guid

from services.models.app_user import AppUser
from services.models.library import Library

class ReleaseBase(SQLModel):
    """Base model for Release with common fields."""
    library_id: int = Field(foreign_key="library.library_id", description="A foreign key pointed to a library of the current record.")
    guid: str = Field(max_length=32, description="A globally unique identifier (GUID).")
    release_num: Optional[str] = Field(default=None, max_length=45, description="Release number such has 10.0, 10.1, etc.")
    release_note: Optional[str] = Field(default=None, description="Description or note associated with the release.")
    release_license: Optional[str] = Field(default=None, description="License associated with the release.")
    namespace_id: Optional[int] = Field(default=None, foreign_key="namespace.namespace_id", description="Foreign key to the NAMESPACE table. It identifies the namespace used with the release.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table identifying user who created the namespace.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table identifying the user who last updated the record.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="The timestamp when the record was first created.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="The timestamp when the record was last updated.")
    state: str = Field(default="Initialized", max_length=20, description="This indicates the revision life cycle state of the Release.")
    prev_release_id: Optional[int] = Field(default=None, foreign_key="release.release_id", description="Foreign key referencing the previous release record.")
    next_release_id: Optional[int] = Field(default=None, foreign_key="release.release_id", description="Foreign key referencing the next release record.")
    
    @field_validator('guid')
    @classmethod
    def validate_guid(cls, v):
        if not validate_guid(v):
            raise ValueError("Guid must be a 32-character hexadecimal string (lowercase).")
        return v

class Release(ReleaseBase, table=True):
    """Model for the release table."""
    __tablename__ = "release"
    
    release_id: Optional[int] = Field(default=None, primary_key=True, description="RELEASE_ID must be an incremental integer.")
    
    # Relationships
    library: "Library" = Relationship(back_populates="releases")
    namespace: Optional["Namespace"] = Relationship(back_populates="releases")
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Release.created_by]"})
    last_updater: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Release.last_updated_by]"})
    prev_release: Optional["Release"] = Relationship(back_populates="next_release", sa_relationship_kwargs={"foreign_keys": "[Release.prev_release_id]", "remote_side": "[Release.release_id]"})
    next_release: Optional["Release"] = Relationship(back_populates="prev_release", sa_relationship_kwargs={"foreign_keys": "[Release.next_release_id]"})
    release_deps: List["ReleaseDep"] = Relationship(back_populates="release", sa_relationship_kwargs={"foreign_keys": "[ReleaseDep.release_id]"})
    depend_on_release_deps: List["ReleaseDep"] = Relationship(back_populates="depend_on_release", sa_relationship_kwargs={"foreign_keys": "[ReleaseDep.depend_on_release_id]"})

class ReleaseRead(ReleaseBase):
    """Model for reading Release data."""
    release_id: int

class ReleaseDepBase(SQLModel):
    """Base model for ReleaseDep with common fields."""
    release_id: int = Field(foreign_key="release.release_id", description="A foreign key pointing to a release record.")
    depend_on_release_id: int = Field(foreign_key="release.release_id", description="A foreign key pointing to dependent release records of the release specified in release_id.")

class ReleaseDep(ReleaseDepBase, table=True):
    """Model for the release_dep table."""
    __tablename__ = "release_dep"
    
    release_dep_id: Optional[int] = Field(default=None, primary_key=True, description="Primary, internal database key.")
    
    # Relationships
    release: "Release" = Relationship(sa_relationship_kwargs={"foreign_keys": "[ReleaseDep.release_id]"})
    depend_on_release: "Release" = Relationship(sa_relationship_kwargs={"foreign_keys": "[ReleaseDep.depend_on_release_id]"})

class ReleaseDepRead(ReleaseDepBase):
    """Model for reading ReleaseDep data."""
    release_dep_id: int
