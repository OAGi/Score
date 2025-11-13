from datetime import datetime, timezone
from typing import Optional, List
from sqlmodel import SQLModel, Field, Relationship
from pydantic import field_validator
from services.utils import validate_guid

from services.models.app_user import AppUser

class CtxSchemeBase(SQLModel):
    """Base model for CtxScheme with common fields."""
    guid: str = Field(max_length=32, description="A globally unique identifier (GUID).")
    scheme_id: str = Field(max_length=45, description="External identification of the scheme.")
    scheme_name: Optional[str] = Field(default=None, max_length=255, description="Pretty print name of the context scheme.")
    description: Optional[str] = Field(default=None, description="Description of the context scheme.")
    scheme_agency_id: str = Field(max_length=45, description="Identification of the agency maintaining the scheme.")
    scheme_version_id: str = Field(max_length=45, description="Version number of the context scheme.")
    ctx_category_id: int = Field(foreign_key="ctx_category.ctx_category_id", description="Foreign key to the CTX_CATEGORY table.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. It indicates the user who created this context scheme.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. It identifies the user who last updated the context scheme.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the scheme was created.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the scheme was last updated.")
    
    @field_validator('guid')
    @classmethod
    def validate_guid(cls, v):
        if not validate_guid(v):
            raise ValueError("Guid must be a 32-character hexadecimal string (lowercase).")
        return v

class CtxScheme(CtxSchemeBase, table=True):
    """Model for the ctx_scheme table."""
    __tablename__ = "ctx_scheme"
    
    ctx_scheme_id: Optional[int] = Field(default=None, primary_key=True, description="Internal, primary, database key.")
    
    # Relationships
    ctx_category: "CtxCategory" = Relationship(back_populates="ctx_schemes")
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[CtxScheme.created_by]"})
    last_updater: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[CtxScheme.last_updated_by]"})
    ctx_scheme_values: List["CtxSchemeValue"] = Relationship(back_populates="owner_ctx_scheme")

class CtxSchemeRead(CtxSchemeBase):
    """Model for reading CtxScheme data."""
    ctx_scheme_id: int

class CtxSchemeValueBase(SQLModel):
    """Base model for CtxSchemeValue with common fields."""
    guid: str = Field(max_length=32, description="A globally unique identifier (GUID).")
    value: str = Field(max_length=100, default="", description="A short value for the scheme value similar to the code list value.")
    meaning: Optional[str] = Field(default=None, description="The description, explanation of the scheme value.")
    owner_ctx_scheme_id: int = Field(foreign_key="ctx_scheme.ctx_scheme_id", description="Foreign key to the CTX_SCHEME table.")
    
    @field_validator('guid')
    @classmethod
    def validate_guid(cls, v):
        if not validate_guid(v):
            raise ValueError("Guid must be a 32-character hexadecimal string (lowercase).")
        return v

class CtxSchemeValue(CtxSchemeValueBase, table=True):
    """Model for the ctx_scheme_value table."""
    __tablename__ = "ctx_scheme_value"
    
    ctx_scheme_value_id: Optional[int] = Field(default=None, primary_key=True, description="Primary, internal database key.")
    
    # Relationships
    owner_ctx_scheme: "CtxScheme" = Relationship(back_populates="ctx_scheme_values")

class CtxSchemeValueRead(CtxSchemeValueBase):
    """Model for reading CtxSchemeValue data."""
    ctx_scheme_value_id: int
