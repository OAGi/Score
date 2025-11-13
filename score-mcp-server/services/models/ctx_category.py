from datetime import datetime, timezone
from typing import Optional, List
from sqlmodel import SQLModel, Field, Relationship
from pydantic import field_validator
from services.utils import validate_guid

from services.models.app_user import AppUser

class CtxCategoryBase(SQLModel):
    """Base model for CtxCategory with common fields."""
    guid: str = Field(max_length=32, description="A globally unique identifier (GUID).")
    name: Optional[str] = Field(default=None, max_length=45, description="Short name of the context category.")
    description: Optional[str] = Field(default=None, description="Explanation of what the context category is.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. It indicates the user who created the context category.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table. It identifies the user who last updated the context category.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the context category was created.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the context category was last updated.")
    
    @field_validator('guid')
    @classmethod
    def validate_guid(cls, v):
        if not validate_guid(v):
            raise ValueError("Guid must be a 32-character hexadecimal string (lowercase).")
        return v

class CtxCategory(CtxCategoryBase, table=True):
    """Model for the ctx_category table."""
    __tablename__ = "ctx_category"
    
    ctx_category_id: Optional[int] = Field(default=None, primary_key=True, description="Internal, primary, database key.")

    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[CtxCategory.created_by]"})
    last_updater: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[CtxCategory.last_updated_by]"})
    ctx_schemes: List["CtxScheme"] = Relationship(back_populates="ctx_category")

class CtxCategoryRead(CtxCategoryBase):
    """Model for reading CtxCategory data."""
    ctx_category_id: int
