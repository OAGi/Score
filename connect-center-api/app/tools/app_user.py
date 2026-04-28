"""
MCP Tools for managing account operations in connectCenter.

This module provides Model Context Protocol (MCP) tools for querying and retrieving
accounts, which represent the individuals or systems that interact with connectCenter.
Accounts serve as the authentication and authorization mechanism for the platform,
enabling user management, role-based access control, and tracking of user activities.
Each account includes user identity information (login credentials, display name, email),
organizational affiliation, and role assignments (Admin, Developer, End-User) that
determine the user's permissions and capabilities within the system.

Accounts enable administrators to manage user access, track user contributions to
components and entities, and maintain audit trails of who created or modified various
artifacts in connectCenter. The tools provide a standardized MCP interface, enabling
clients to interact with user data programmatically.

Available Tools:
- get_users: Retrieve paginated lists of users with optional filters for login_id,
  username, organization, email, and role flags (is_admin, is_developer, is_enabled).
  Supports custom sorting and pagination.
- who_am_i: Retrieve the currently authenticated connectCenter account with its
  identity, role assignments, and current account status.

Key Features:
- Filter by user attributes (login ID, username, organization, email, roles)
- Filter by user status (admin, developer, enabled/disabled)
- Support for pagination and multi-column sorting
- Comprehensive error handling and validation
- Structured response models with user roles and metadata

The tools provide a clean, consistent interface for accessing user data through the MCP protocol.
All operations require proper authentication and authorization.
"""

from __future__ import annotations

import logging
from typing import Annotated

from fastmcp import FastMCP
from fastmcp.dependencies import Depends
from fastmcp.exceptions import ToolError
from pydantic import Field
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.vendor_plugins import get_vendor_plugin
from app.security import AuthenticatedUser
from app.services.app_user_service import AppUserService
from app.services.models.app_user import AppUserServiceResult, Role
from app.tools import _to_tool_error, get_tool_authenticated_user, str_to_bool, tool_session
from app.tools.models.app_user import GetCurrentUserResponse, GetUserListEntryResponse, GetUserPaginationResponse, GetUserResponse

logger = logging.getLogger("connectcenter.mcp.app_user")

mcp = FastMCP("connectCenter MCP - Account Tools")


async def get_app_user_service(
    session: AsyncSession = Depends(tool_session),
) -> AppUserService:
    """Provide a requester-scoped app-user service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    return _build_app_user_service(session, requester)


@mcp.tool(
    name="get_users",
    description="Get a paginated list of users.",
    output_schema={
        "type": "object",
        "description": "Response containing paginated list of users.",
        "properties": {
            "total_items": {"type": "integer",
                            "description": "Total number of users available. Allowed values: non-negative integers (≥0).",
                            "example": 25},
            "offset": {"type": "integer",
                       "description": "Offset of the first item in this page. Allowed values: non-negative integers (≥0). Default value: 0.",
                       "example": 0},
            "limit": {"type": "integer",
                      "description": "Number of items returned in this page. Allowed values: integers between 1 and 100 (inclusive). Default value: 10.",
                      "example": 10},
            "items": {
                "type": "array",
                "description": "List of users on this page",
                "items": {
                    "type": "object",
                    "properties": {
                        "user_id": {"type": "integer", "description": "Unique identifier for the user", "example": 1},
                        "login_id": {"type": "string", "description": "User's login identifier", "example": "admin"},
                        "username": {"type": ["string", "null"], "description": "Display name of the user",
                                     "example": "Administrator"},
                        "organization": {"type": ["string", "null"], "description": "The company the user represents",
                                         "example": "ACME Corp"},
                        "email": {"type": ["string", "null"], "description": "Email address of the user",
                                  "example": "admin@example.com"},
                        "roles": {"type": "array",
                                  "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                  "description": "List of roles assigned to the user", "example": ["Admin"]},
                        "is_enabled": {"type": "boolean", "description": "Whether the user account is enabled",
                                       "example": True}
                    },
                    "required": ["user_id", "login_id", "username", "roles", "is_enabled"]
                }
            }
        },
        "required": ["total_items", "offset", "limit", "items"]
    }
)
async def get_users(
    login_id: Annotated[str | None, Field(default=None, description="Filter by login ID using partial match (case-insensitive).")],
    username: Annotated[str | None, Field(default=None, description="Filter by username using partial match (case-insensitive).")],
    organization: Annotated[str | None, Field(default=None, description="Filter by organization using partial match (case-insensitive).")],
    email: Annotated[str | None, Field(default=None, description="Filter by email address using partial match (case-insensitive).")],
    is_admin: Annotated[bool | str | None, Field(default=None, description="Filter by admin status.")],
    is_developer: Annotated[bool | str | None, Field(default=None, description="Filter by developer status.")],
    is_enabled: Annotated[bool | str | None, Field(default=None, description="Filter by enabled status.")],
    order_by: Annotated[str | None, Field(default=None, description="Comma-separated list of properties to order results by. Allowed columns: login_id, username, organization, email, is_admin, is_developer, is_enabled.")],
    offset: Annotated[int, Field(default=0, ge=0, description="The offset from the beginning of the list.")],
    limit: Annotated[int, Field(default=10, ge=1, le=100, description="The maximum number of items to return.")],
    app_user_service: AppUserService = Depends(get_app_user_service),
) -> GetUserPaginationResponse:
    """
    Get a paginated list of users registered in connectCenter.

    This function retrieves users registered in connectCenter. This tool enables
    administrators and authorized users to query and manage user accounts, including filtering
    by login credentials, organizational affiliation, email addresses, and role assignments.
    It supports pagination, filtering, and sorting. Only authenticated users can access this endpoint.

    Args:
        login_id (str | None, optional): Filter by login ID using partial match (case-insensitive). Defaults to None.
        username (str | None, optional): Filter by username (display name) using partial match (case-insensitive). Defaults to None.
        organization (str | None, optional): Filter by organization using partial match (case-insensitive). Defaults to None.
        email (str | None, optional): Filter by email address using partial match (case-insensitive). Defaults to None.
        is_admin (bool | str | None, optional): Filter by admin status. Accepts bool, str ('True'/'true'/'1' for True, 'False'/'false'/'0' for False), or None. Defaults to None.
        is_developer (bool | str | None, optional): Filter by developer status. Accepts bool, str ('True'/'true'/'1' for True, 'False'/'false'/'0' for False), or None. Defaults to None.
        is_enabled (bool | str | None, optional): Filter by enabled status. Accepts bool, str ('True'/'true'/'1' for True, 'False'/'false'/'0' for False), or None. Defaults to None.
        order_by (str | None, optional): Comma-separated list of properties to order results by.
            Prefix with '-' for descending, '+' for ascending (default ascending).
            Allowed columns: login_id, username, organization, email, is_admin, is_developer, is_enabled.
            Example: '-login_id,+username' translates to 'login_id DESC, username ASC'.
            Defaults to None.
        offset (int, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int, optional): The maximum number of items to return. Must be between 1 and 100 (inclusive). Defaults to 10.

    Returns:
        GetUserPaginationResponse: Response object containing:
            - total_items: Total number of users available
            - offset: Offset of the first item in this page
            - limit: Number of items returned in this page
            - items: List of users on this page with detailed information including:
                - user_id: Unique identifier for the user
                - login_id: User's login identifier
                - username: Display name of the user
                - organization: The company the user represents
                - email: Email address of the user
                - roles: List of roles assigned to the user (Admin, Developer, End-User)
                - is_enabled: Whether the user account is enabled

    Raises:
        ToolError: If validation fails, parsing errors occur, or other errors happen.
            Common error scenarios include:
            - Invalid pagination parameters (negative offset, limit out of range)
            - Invalid date range format
            - Invalid order_by column names or format
            - Database connection issues
            - Authentication failures

    Examples:
        Basic listing:
        >>> result = get_users()
        >>> print(f"Found {result.total_items} users")

        Filtered by role:
        >>> result = get_users(is_admin=True, limit=5)
        >>> for user in result.items:
        ...     print(f"Admin: {user.username} ({user.login_id})")

        Filtered by organization:
        >>> result = get_users(organization="ACME", limit=10)
        >>> for user in result.items:
        ...     print(f"User: {user.username} from {user.organization}")

        Search by login ID:
        >>> result = get_users(login_id="admin", limit=5)
        >>> for user in result.items:
        ...     print(f"Found user: {user.username} ({user.email})")

        Filtered by enabled status:
        >>> result = get_users(is_enabled=True, limit=10)
        >>> print(f"Enabled users: {result.total_items}")
    """
    try:
        is_admin = str_to_bool(is_admin)
        is_developer = str_to_bool(is_developer)
        is_enabled = str_to_bool(is_enabled)
    except ToolError:
        raise
    except Exception as e:
        raise ToolError(
            f"Type conversion error: {str(e)}. Please check your parameter types and try again."
        ) from e

    try:
        page = await app_user_service.list(
            limit=limit,
            offset=offset,
            order_by=order_by,
            login_id=login_id,
            username=username,
            organization=organization,
            email=email,
            is_admin=is_admin,
            is_developer=is_developer,
            is_enabled=is_enabled,
        )
        return GetUserPaginationResponse(
            total_items=page.total,
            offset=page.offset,
            limit=page.limit,
            items=[_to_user_list_entry(item) for item in page.items],
        )
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to retrieve users.") from exc


@mcp.tool(
    name="who_am_i",
    description="Get information about the currently authenticated user.",
    output_schema={
        "type": "object",
        "description": "Response containing information about the current user",
        "properties": {
            "login_id": {"type": "string", "description": "User's login identifier", "example": "admin"},
            "username": {"type": ["string", "null"], "description": "Display name of the user",
                         "example": "Administrator"},
            "organization": {"type": ["string", "null"], "description": "The company the user represents",
                             "example": "ACME Corp"},
            "email": {"type": ["string", "null"], "description": "Email address of the user",
                      "example": "admin@example.com"},
            "roles": {"type": "array", "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                      "description": "List of roles assigned to the user", "example": ["Admin"]},
            "is_enabled": {"type": "boolean", "description": "Whether the user account is enabled", "example": True}
        },
        "required": ["login_id", "username", "organization", "email", "roles", "is_enabled"]
    }
)
async def who_am_i(
    app_user_service: AppUserService = Depends(get_app_user_service),
) -> GetCurrentUserResponse:
    """
    Get information about the currently authenticated user.

    This function returns detailed information about the user who is currently
    authenticated and making the request. It provides user identity, roles,
    and account status information.

    Returns:
        GetCurrentUserResponse: Response object containing:
            - login_id: User's login identifier
            - username: Display name of the user
            - organization: The company the user represents
            - email: Email address of the user
            - roles: List of roles assigned to the user (Admin, Developer, End-User)
            - is_enabled: Whether the user account is enabled

    Raises:
        ToolError: If authentication fails or other errors occur.
            Common error scenarios include:
            - Authentication token is invalid or expired
            - User account is disabled
            - Database connection issues

    Examples:
        Get current user information:
        >>> result = who_am_i()
        >>> print(f"Logged in as: {result.username} ({result.login_id})")
        >>> print(f"Roles: {', '.join(result.roles)}")
        >>> print(f"Organization: {result.organization}")

        Check if user has admin role:
        >>> result = who_am_i()
        >>> if "Admin" in result.roles:
        ...     print("User has admin privileges")
        >>> else:
        ...     print("User does not have admin privileges")

        Check user status:
        >>> result = who_am_i()
        >>> if result.is_enabled:
        ...     print("User account is active")
        >>> else:
        ...     print("User account is disabled")
    """
    try:
        user = await app_user_service.get_current_user()
        if user is None:
            raise ValueError("The current authenticated app user does not exist.")
        return _to_current_user_response(user)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to retrieve the current user.") from exc


def _build_app_user_service(
    session: AsyncSession,
    requester: AuthenticatedUser,
) -> AppUserService:
    """Construct the release service for an MCP request."""
    plugin = get_vendor_plugin()
    return AppUserService(
        plugin.create_app_user_repository(session),
        requester=requester,
    )


def _to_user_response(user: AppUserServiceResult) -> GetUserResponse:
    """Convert a service-layer app-user record into the MCP response model."""
    return GetUserResponse(
        user_id=int(user.app_user_id),
        login_id=user.login_id,
        username=user.username,
        roles=_build_roles(is_admin=user.is_admin, is_developer=user.is_developer),
        organization=user.organization,
        email=user.email,
        email_verified=user.email_verified,
        email_verified_timestamp=user.email_verified_timestamp,
        is_enabled=user.is_enabled,
    )


def _to_current_user_response(user: AppUserServiceResult) -> GetCurrentUserResponse:
    """Convert a service-layer app-user record into the MCP current-user response model."""
    return GetCurrentUserResponse(
        login_id=user.login_id,
        username=user.username,
        roles=_build_roles(is_admin=user.is_admin, is_developer=user.is_developer),
        organization=user.organization,
        email=user.email,
        email_verified=user.email_verified,
        email_verified_timestamp=user.email_verified_timestamp,
        is_enabled=user.is_enabled,
    )


def _to_user_list_entry(user: AppUserServiceResult) -> GetUserListEntryResponse:
    """Convert a service-layer app-user record into the MCP list model."""
    return GetUserListEntryResponse(
        user_id=int(user.app_user_id),
        login_id=user.login_id,
        username=user.username,
        organization=user.organization,
        email=user.email,
        roles=_build_roles(is_admin=user.is_admin, is_developer=user.is_developer),
        is_enabled=user.is_enabled,
    )


def _build_roles(*, is_admin: bool, is_developer: bool) -> list[Role]:
    """Build API roles from internal admin/developer flags."""
    roles: list[Role] = []
    if is_admin:
        roles.append("Admin")
    if is_developer:
        roles.append("Developer")
    else:
        roles.append("End-User")
    return roles
