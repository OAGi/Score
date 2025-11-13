from datetime import datetime, timezone
from typing import Optional, List
from sqlmodel import SQLModel, Field, Relationship

from services.models.app_user import AppUser
from services.models.library import Library

class NamespaceBase(SQLModel):
    """Base model for Namespace with common fields."""
    library_id: int = Field(foreign_key="library.library_id", description="A foreign key pointed to a library of the current record.")
    uri: str = Field(max_length=100, description="This is the URI of the namespace.")
    prefix: Optional[str] = Field(default=None, max_length=45, description="This is a default short name to represent the URI. It may be overridden during the expression generation.")
    description: Optional[str] = Field(default=None, description="Description or explanation about the namespace or use of the namespace.")
    is_std_nmsp: bool = Field(default=False, description="This indicates whether the namespace is reserved for standard used (i.e., whether it is an OAGIS namespace).")
    owner_user_id: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table identifying the user who can update or delete the record.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table identifying user who created the namespace.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table identifying the user who last updated the record.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="The timestamp when the record was first created.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="The timestamp when the record was last updated.")

class Namespace(NamespaceBase, table=True):
    """Model for the namespace table."""
    __tablename__ = "namespace"
    
    namespace_id: Optional[int] = Field(default=None, primary_key=True, description="Primary, internal database key.")
    
    # Relationships
    library: "Library" = Relationship(back_populates="namespaces")
    owner: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Namespace.owner_user_id]"})
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Namespace.created_by]"})
    last_updater: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Namespace.last_updated_by]"})
    releases: List["Release"] = Relationship(back_populates="namespace")

class NamespaceRead(NamespaceBase):
    """Model for reading Namespace data."""
    namespace_id: int

# ACC (Aggregate Core Component) Models
