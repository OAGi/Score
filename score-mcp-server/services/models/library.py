from datetime import datetime, timezone
from typing import Optional, List
from sqlmodel import SQLModel, Field, Relationship

from services.models.app_user import AppUser

class LibraryBase(SQLModel):
    """Base model for Library with common fields."""
    name: Optional[str] = Field(default=None, max_length=100, description="A library name.")
    type: Optional[str] = Field(default=None, max_length=100, description="A type of the library.")
    organization: Optional[str] = Field(default=None, max_length=100, description="The name of the organization responsible for maintaining or managing the library.")
    description: Optional[str] = Field(default=None, description="A brief summary or overview of the library's purpose and functionality.")
    link: Optional[str] = Field(default=None, description="A URL directing to the library's homepage, documentation, or repository for further details.")
    domain: Optional[str] = Field(default=None, max_length=100, description="Specifies the area of focus or application domain of the library (e.g., agriculture, finance, or aerospace).")
    state: Optional[str] = Field(default=None, max_length=20, description="Current state of the library.")
    is_read_only: bool = Field(default=False, description="Indicates if the library is read-only (0 = False, 1 = True).")
    is_default: bool = Field(default=False, description="Indicates if the library is the default (0 = False, 1 = True). The default library is shown first if the user has no preference.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the user who creates the record.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the last user who updated the record.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the record was created.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the record was last updated.")

class Library(LibraryBase, table=True):
    """Model for the library table."""
    __tablename__ = "library"
    
    library_id: Optional[int] = Field(default=None, primary_key=True, description="Internal, primary database key.")
    
    # Relationships
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Library.created_by]"})
    last_updater: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Library.last_updated_by]"})
    releases: List["Release"] = Relationship(back_populates="library")
    namespaces: List["Namespace"] = Relationship(back_populates="library")

class LibraryRead(LibraryBase):
    """Model for reading Library data."""
    library_id: int
