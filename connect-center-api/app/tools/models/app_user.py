"""Models for App User MCP tools."""

from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel

from app.routes.models.shared import Role


class GetUserResponse(BaseModel):
    """Response for app-user tools."""

    user_id: int
    login_id: str
    username: str
    roles: list[Role]
    organization: str | None = None
    email: str | None = None
    email_verified: bool
    email_verified_timestamp: datetime | None = None
    is_enabled: bool


class GetUserPaginationResponse(BaseModel):
    """Response for get_users tool."""

    total_items: int
    offset: int
    limit: int
    items: list[GetUserResponse]
