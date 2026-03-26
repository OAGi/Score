"""Pydantic response models for Application User endpoints.

Defines the serialized shapes returned by app user list/get routes.
"""


from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field

from app.routes.models.shared import Role


class AppUserEntry(BaseModel):
    """API representation of an application user record."""
    app_user_id: int = Field(..., ge=0, description="Primary key column.", examples=[1, 2])
    login_id: str = Field(..., max_length=45, description="User Id of the user.", examples=["oagis"])
    username: str = Field(..., max_length=100, description="Full name of the user.")
    roles: list[Role] = Field(
        ...,
        description="List of roles assigned to the user.",
        examples=[["Admin", "Developer"]],
    )
    # - Admin: Administrator that can control system configurations, data ownership management, and library/release creation
    # - Developer: Can create/edit core components for the library
    # - End-User: Can utilize core components for profiling business information entities
    organization: str | None = Field(default=None, max_length=100, description="The company the user represents.")
    email: str | None = Field(default=None, max_length=100, description="Email address.")
    email_verified: bool = Field(..., description="Whether the email is verified.")
    email_verified_timestamp: datetime | None = Field(
        default=None,
        description="Timestamp when the email address was verified.",
    )
    is_enabled: bool = Field(..., description="Whether the user is enabled.")

    model_config = ConfigDict(from_attributes=True)


class GetAppUserListResponse(BaseModel):
    """Paginated response envelope for application user listings."""
    total_items: int = Field(..., ge=0, description="Total number of accounts available.")
    offset: int = Field(..., ge=0, description="Offset used for this page.")
    limit: int = Field(..., ge=1, le=100, description="Limit used for this page.")
    items: list[AppUserEntry] = Field(..., description="Accounts.")


class GetAppUserByAppUserIdResponse(AppUserEntry):
    """Response payload for retrieving an account by ID."""

    model_config = ConfigDict(from_attributes=True)
