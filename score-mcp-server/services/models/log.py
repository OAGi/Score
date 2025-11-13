from datetime import datetime, timezone
from typing import Optional, List
from sqlmodel import SQLModel, Field, Relationship

from services.models.app_user import AppUser
from services.models.core_component import AccManifest, BccpManifest, AsccpManifest
from services.models.data_type import DtManifest, XbtManifest

class LogBase(SQLModel):
    """Base model for Log with common fields."""
    hash: str = Field(max_length=64, description="Hash of the log entry.")
    revision_num: int = Field(description="Revision number.")
    revision_tracking_num: int = Field(description="Revision tracking number.")
    log_action: str = Field(max_length=20, description="Action performed.")
    reference: Optional[str] = Field(default=None, description="Reference information.")
    snapshot: Optional[str] = Field(default=None, description="Snapshot data.")
    created_by: int = Field(foreign_key="app_user.app_user_id", description="Foreign key to the APP_USER table referring to the user who creates the log.")
    creation_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Timestamp when the log was created.")
class Log(LogBase, table=True):
    """Model for the log table."""
    __tablename__ = "log"
    
    log_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key for log.")
    
    # Relationships
    creator: AppUser = Relationship(sa_relationship_kwargs={"foreign_keys": "[Log.created_by]"})
    acc_manifests: List["AccManifest"] = Relationship(back_populates="log")
    bccp_manifests: List["BccpManifest"] = Relationship(back_populates="log")
    asccp_manifests: List["AsccpManifest"] = Relationship(back_populates="log")
    dt_manifests: List["DtManifest"] = Relationship(back_populates="log")
    xbt_manifests: List["XbtManifest"] = Relationship(back_populates="log")

class LogRead(LogBase):
    """Model for reading Log data."""
    log_id: int

