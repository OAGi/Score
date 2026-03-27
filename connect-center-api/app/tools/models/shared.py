"""Shared Pydantic models used by MCP tool response schemas."""

from __future__ import annotations

from datetime import datetime
from typing import Literal

from pydantic import BaseModel, ConfigDict, Field

Role = Literal["Admin", "Developer", "End-User"]


class UserSummary(BaseModel):
    """Minimal authenticated user summary for tool responses."""

    user_id: int = Field(..., ge=0, description="User identifier.")
    login_id: str = Field(..., description="Login identifier.")
    username: str = Field(..., description="Display name.")
    roles: list[Role] = Field(..., description="Assigned user roles.")

    model_config = ConfigDict(frozen=True, from_attributes=True)


class WhoAndWhen(BaseModel):
    """Audit information for create/update actions."""

    who: UserSummary = Field(..., description="User who performed the action.")
    when: datetime = Field(..., description="Timestamp when the action happened.")

    model_config = ConfigDict(frozen=True, from_attributes=True)


class LibrarySummaryRecord(BaseModel):
    """Library summary."""

    library_id: int = Field(..., ge=1, description="Library identifier.")
    name: str = Field(..., description="Library name.")

    model_config = ConfigDict(frozen=True, from_attributes=True)


class NamespaceSummaryRecord(BaseModel):
    """Namespace summary."""

    namespace_id: int = Field(..., ge=1, description="Namespace identifier.")
    prefix: str | None = Field(default=None, description="Namespace prefix.")
    uri: str = Field(..., description="Namespace URI.")

    model_config = ConfigDict(frozen=True, from_attributes=True)


class ReleaseSummaryRecord(BaseModel):
    """Release summary for tool responses."""

    release_id: int = Field(..., ge=1, description="Release identifier.")
    release_num: str = Field(..., description="Release number.")
    state: Literal["Processing", "Initialized", "Draft", "Published"] = Field(
        ...,
        description="Release lifecycle state.",
    )

    model_config = ConfigDict(frozen=True, from_attributes=True)


class LogSummaryRecord(BaseModel):
    """Revision log summary."""

    log_id: int = Field(..., ge=1, description="Revision log identifier.")
    revision_num: int = Field(..., ge=0, description="Revision number.")
    revision_tracking_num: int = Field(..., ge=0, description="Revision tracking number.")

    model_config = ConfigDict(frozen=True, from_attributes=True)


class ValueConstraintRecord(BaseModel):
    """Value constraint information."""

    default_value: str | None = Field(default=None, description="Default value.")
    fixed_value: str | None = Field(default=None, description="Fixed value.")

    model_config = ConfigDict(frozen=True, from_attributes=True)


class BizCtxSummaryRecord(BaseModel):
    """Business context summary."""

    biz_ctx_id: int = Field(..., ge=1, description="Business context identifier.")
    guid: str = Field(..., description="Business context GUID.")
    name: str | None = Field(default=None, description="Business context name.")

    model_config = ConfigDict(frozen=True, from_attributes=True)
