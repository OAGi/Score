"""Models for App User MCP tools."""

from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, ConfigDict

from app.tools.models.shared import Role


class GetUserListEntryResponse(BaseModel):
    """User payload for list responses."""

    user_id: int
    login_id: str
    username: str | None
    organization: str | None = None
    email: str | None = None
    roles: list[Role]
    is_enabled: bool

    model_config = ConfigDict(frozen=True)


class GetUserResponse(BaseModel):
    """Response for the current-user tool."""

    user_id: int
    login_id: str
    username: str | None
    organization: str | None
    email: str | None
    roles: list[Role]
    email_verified: bool
    email_verified_timestamp: datetime | None = None
    is_enabled: bool

    model_config = ConfigDict(frozen=True)


class GetUserPaginationResponse(BaseModel):
    """Response for get_users tool."""

    total_items: int
    offset: int
    limit: int
    items: list[GetUserListEntryResponse]

    model_config = ConfigDict(frozen=True)
