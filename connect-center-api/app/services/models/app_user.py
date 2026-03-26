"""Service models for app-user operations."""

from __future__ import annotations

from datetime import datetime
from typing import Literal

from dataclasses import dataclass

from app.types.identifiers import AppUserId


@dataclass(kw_only=True)
class AppUserServiceResult:
    """Application user information."""

    app_user_id: AppUserId
    login_id: str
    username: str
    organization: str | None
    email: str | None
    email_verified: bool
    email_verified_timestamp: datetime | None
    is_developer: bool
    is_admin: bool
    is_enabled: bool


@dataclass(kw_only=True)
class UserSummary:
    """Service-layer user summary."""

    user_id: AppUserId
    login_id: str
    username: str
    roles: list[Role]


Role = Literal["Admin", "Developer", "End-User"]
