from datetime import datetime, timezone
from typing import Optional, List
from sqlmodel import SQLModel, Field, Relationship
from pydantic import field_validator
from services.utils import validate_guid

from services.models.app_user import AppUser

class BizCtxBase(SQLModel):
    """Base model for BizCtx with common fields."""
    guid: str = Field(max_length=32, description="A globally unique identifier (GUID).")
    name: Optional[str] = Field(default=None, max_length=100, description="Short, descriptive name of the business context.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the user who creates the entity.")
    last_updated_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the last user who has updated the business context.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the business context record was first created.")
    last_update_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="The timestamp when the business context was last updated.")
    
    @field_validator('guid')
    @classmethod
    def validate_guid(cls, v):
        if not validate_guid(v):
            raise ValueError("Guid must be a 32-character hexadecimal string (lowercase).")
        return v

class BizCtx(BizCtxBase, table=True):
    """Model for the biz_ctx table."""
    __tablename__ = "biz_ctx"
    
    biz_ctx_id: Optional[int] = Field(default=None, primary_key=True, description="Primary, internal database key.")
    
    # Relationships
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[BizCtx.created_by]"})
    last_updater: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[BizCtx.last_updated_by]"})
    biz_ctx_values: List["BizCtxValue"] = Relationship(back_populates="biz_ctx")

class BizCtxRead(BizCtxBase):
    """Model for reading BizCtx data."""
    biz_ctx_id: int

class BizCtxValueBase(SQLModel):
    """Base model for BizCtxValue with common fields."""
    biz_ctx_id: int = Field(foreign_key="biz_ctx.biz_ctx_id", description="Foreign key to the biz_ctx table.")
    ctx_scheme_value_id: int = Field(foreign_key="ctx_scheme_value.ctx_scheme_value_id", description="Foreign key to the CTX_SCHEME_VALUE table.")

class BizCtxValue(BizCtxValueBase, table=True):
    """Model for the biz_ctx_value table."""
    __tablename__ = "biz_ctx_value"
    
    biz_ctx_value_id: Optional[int] = Field(default=None, primary_key=True, description="Primary, internal database key.")
    
    # Relationships
    biz_ctx: "BizCtx" = Relationship(back_populates="biz_ctx_values")
    ctx_scheme_value: "CtxSchemeValue" = Relationship()

class BizCtxValueRead(BizCtxValueBase):
    """Model for reading BizCtxValue data."""
    biz_ctx_value_id: int

class BizCtxAssignmentBase(SQLModel):
    """Base model for BizCtxAssignment with common fields."""
    biz_ctx_id: int = Field(foreign_key="biz_ctx.biz_ctx_id", description="Business context ID.")
    top_level_asbiep_id: int = Field(foreign_key="top_level_asbiep.top_level_asbiep_id", description="This is a foreign key to the top-level ASBIEP.")

class BizCtxAssignment(BizCtxAssignmentBase, table=True):
    """Model for the biz_ctx_assignment table."""
    __tablename__ = "biz_ctx_assignment"
    
    biz_ctx_assignment_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key for business context assignment.")
    
    # Relationships
    biz_ctx: "BizCtx" = Relationship()
    top_level_asbiep: "TopLevelAsbiep" = Relationship()

class BizCtxAssignmentRead(BizCtxAssignmentBase):
    """Model for reading BizCtxAssignment data."""
    biz_ctx_assignment_id: int
