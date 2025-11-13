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

Key Features:
- Filter by user attributes (login ID, username, organization, email, roles)
- Filter by user status (admin, developer, enabled/disabled)
- Support for pagination and multi-column sorting
- Comprehensive error handling and validation
- Structured response models with user roles and metadata

The tools provide a clean, consistent interface for accessing user data through the MCP protocol.
All operations require proper authentication and authorization.
"""

import logging
from typing import Annotated

from fastapi import HTTPException
from fastmcp import FastMCP
from fastmcp.exceptions import ToolError
from pydantic import BaseModel, Field

from services import PaginationParams
from tools import _validate_auth_and_db, parse_order_by_to_sorts, _get_user_roles
from tools.utils import str_to_bool
from tools.models.app_user import GetUserResponse, GetUsersResponse

# Configure logging
logger = logging.getLogger(__name__)

mcp = FastMCP("Score MCP Server - App User Tools")


@mcp.tool(
    name="get_users",
    description="Get a paginated list of users. Boolean parameters accept both their native types and string representations (strings are automatically converted).",
    output_schema={
        "type": "object",
        "description": "Response containing paginated list of users. Boolean parameters accept both their native types and string representations (strings are automatically converted to the appropriate type).",
        "properties": {
            "total_items": {"type": "integer", "description": "Total number of users available. Allowed values: non-negative integers (≥0).", "example": 25},
            "offset": {"type": "integer", "description": "Offset of the first item in this page. Allowed values: non-negative integers (≥0). Default value: 0.", "example": 0},
            "limit": {"type": "integer", "description": "Number of items returned in this page. Allowed values: integers between 1 and 100 (inclusive). Default value: 10.", "example": 10},
            "items": {
                "type": "array",
                "description": "List of users on this page",
                "items": {
                    "type": "object",
                    "properties": {
                        "user_id": {"type": "integer", "description": "Unique identifier for the user", "example": 1},
                        "login_id": {"type": "string", "description": "User's login identifier", "example": "admin"},
                        "username": {"type": ["string", "null"], "description": "Display name of the user", "example": "Administrator"},
                        "organization": {"type": ["string", "null"], "description": "The company the user represents", "example": "ACME Corp"},
                        "email": {"type": ["string", "null"], "description": "Email address of the user", "example": "admin@example.com"},
                        "roles": {"type": "array", "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]}, "description": "List of roles assigned to the user", "example": ["Admin"]},
                        "is_enabled": {"type": "boolean", "description": "Whether the user account is enabled", "example": True}
                    },
                    "required": ["user_id", "login_id", "username", "organization", "email", "roles", "is_enabled"]
                }
            }
        },
        "required": ["total_items", "offset", "limit", "items"]
    }
)
async def get_users(
    offset: Annotated[int, Field(
        description="The offset from the beginning of the list. Must be a non-negative number.",
        examples=[0, 10, 20],
        ge=0,
        title="Offset"
    )] = 0,
    limit: Annotated[int, Field(
        description="The maximum number of items to return. Must be a non-negative number.",
        examples=[10, 25, 50],
        ge=1,
        le=100,
        title="Limit"
    )] = 10,
    login_id: Annotated[str | None, Field(
        description="Filter by login ID using partial match (case-insensitive).",
        examples=["admin", "developer", "user"],
        title="Login ID"
    )] = None,
    username: Annotated[str | None, Field(
        description="Filter by username (display name) using partial match (case-insensitive).",
        examples=["Administrator", "Developer", "User"],
        title="Username"
    )] = None,
    organization: Annotated[str | None, Field(
        description="Filter by organization using partial match (case-insensitive).",
        examples=["ACME Corp", "Tech Solutions", "Global Inc"],
        title="Organization"
    )] = None,
    email: Annotated[str | None, Field(
        description="Filter by email address using partial match (case-insensitive).",
        examples=["admin@example.com", "dev@company.com", "user@org.org"],
        title="Email"
    )] = None,
    is_admin: Annotated[bool | str | None, Field(
        description="Filter by admin status. Accepts bool, str, or None. String values are converted: 'True'/'true'/'1' -> True, 'False'/'false'/'0' -> False.",
        examples=[True, False, "1", "0", "true", "false", "True", "False"],
        title="Is Admin"
    )] = None,
    is_developer: Annotated[bool | str | None, Field(
        description="Filter by developer status. Accepts bool, str, or None. String values are converted: 'True'/'true'/'1' -> True, 'False'/'false'/'0' -> False.",
        examples=[True, False, "1", "0", "true", "false", "True", "False"],
        title="Is Developer"
    )] = None,
    is_enabled: Annotated[bool | str | None, Field(
        description="Filter by enabled status. Accepts bool, str, or None. String values are converted: 'True'/'true'/'1' -> True, 'False'/'false'/'0' -> False.",
        examples=[True, False, "1", "0", "true", "false", "True", "False"],
        title="Is Enabled"
    )] = None,
    order_by: Annotated[str | None, Field(
        description="Comma-separated list of properties to order results by. Prefix with '-' for descending, '+' for ascending (default ascending). Allowed columns: login_id, username, organization, email, is_admin, is_developer, is_enabled, email_verified. Example: '-login_id,+username' translates to 'login_id DESC, username ASC'.",
        examples=["-login_id,+username", "username", "-email", "-is_admin"],
        title="Order By"
    )] = None
) -> GetUsersResponse:
    """
    Get a paginated list of users registered in connectCenter.
    
    This function retrieves users registered in connectCenter. This tool enables
    administrators and authorized users to query and manage user accounts, including filtering
    by login credentials, organizational affiliation, email addresses, and role assignments.
    It supports pagination, filtering, and sorting. Only authenticated users can access this endpoint.
    
    Args:
        offset (int | None, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int | None, optional): The maximum number of items to return. Must be a non-negative number. Defaults to 10.
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
    
    Returns:
        GetUsersResponse: Response object containing:
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
        >>> result = get_users(offset=0, limit=10)
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
    logger.info(
        f"Retrieving users: offset={offset}, limit={limit}, login_id={login_id}, username={username}, "
        f"organization={organization}, email={email}, is_admin={is_admin}, is_developer={is_developer}, "
        f"is_enabled={is_enabled}, order_by={order_by}"
    )
    
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()
    logger.debug(f"User authenticated: {app_user.login_id} (ID: {app_user.app_user_id})")
    
    # Convert string parameters to their proper types
    try:
        logger.debug(f"Converting boolean parameters: is_admin={is_admin}, is_developer={is_developer}, is_enabled={is_enabled}")
        is_admin = str_to_bool(is_admin)
        is_developer = str_to_bool(is_developer)
        is_enabled = str_to_bool(is_enabled)
        logger.debug(f"Boolean values converted: is_admin={is_admin}, is_developer={is_developer}, is_enabled={is_enabled}")
    except ToolError:
        raise  # Re-raise ToolError as-is
    except Exception as e:
        logger.error(f"Type conversion error: {e}")
        raise ToolError(
            f"Type conversion error: {str(e)}. Please check your parameter types and try again."
        ) from e

    # Create service instance
    from services import AppUserService
    user_service = AppUserService()

    # Validate and create pagination parameters
    try:
        pagination = PaginationParams(offset=offset, limit=limit)
    except ValueError as e:
        raise ToolError(
            f"Pagination validation failed: {str(e)}. Please provide valid offset (≥0) and limit (1-100) values.") from e


    # Validate order_by parameter and create Sort objects
    sort_list = None
    if order_by:
        try:
            sort_list = parse_order_by_to_sorts(order_by)
            # Validate that all column names are in the allowed list
            for sort in sort_list:
                if sort.column not in user_service.allowed_columns_for_order_by:
                    raise ValueError(f"Invalid column name: '{sort.column}'. Allowed columns: {', '.join(user_service.allowed_columns_for_order_by)}")
        except ValueError as e:
            raise ToolError(
                f"Invalid order_by: {str(e)}. Please use format: '(-|+)?<column_name>(,(-|+)?<column_name>)*'. "
                f"Valid columns: {", ".join(user_service.allowed_columns_for_order_by)}") from e

    # Get users
    try:
        logger.debug("Querying users from database")
        page = user_service.get_users(
            login_id=login_id,
            username=username,
            organization=organization,
            email=email,
            is_admin=is_admin,
            is_developer=is_developer,
            is_enabled=is_enabled,
            pagination=pagination,
            sort_list=sort_list
        )
        logger.info(f"Found {len(page.items)} users (total available: {page.total})")

        result = GetUsersResponse(
            total_items=page.total,
            offset=page.offset,
            limit=page.limit,
            items=[_create_user_result(user) for user in page.items]
        )
        logger.debug(f"Response prepared with {len(result.items)} users")
        return result
    except HTTPException as e:
        logger.error(f"Failed to retrieve users: {e.detail} (status: {e.status_code})")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error while retrieving users: {str(e)}", exc_info=True)
        raise ToolError(
            f"An unexpected error occurred while retrieving the users: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="who_am_i",
    description="Get information about the currently authenticated user.",
    output_schema={
        "type": "object",
        "description": "Response containing information about the current user",
        "properties": {
            "user_id": {"type": "integer", "description": "Unique identifier for the user", "example": 1},
            "login_id": {"type": "string", "description": "User's login identifier", "example": "admin"},
            "username": {"type": ["string", "null"], "description": "Display name of the user", "example": "Administrator"},
            "organization": {"type": ["string", "null"], "description": "The company the user represents", "example": "ACME Corp"},
            "email": {"type": ["string", "null"], "description": "Email address of the user", "example": "admin@example.com"},
            "roles": {"type": "array", "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]}, "description": "List of roles assigned to the user", "example": ["Admin"]},
            "is_enabled": {"type": "boolean", "description": "Whether the user account is enabled", "example": True}
        },
        "required": ["user_id", "login_id", "username", "organization", "email", "roles", "is_enabled"]
    }
)
async def who_am_i() -> GetUserResponse:
    """
    Get information about the currently authenticated user.
    
    This function returns detailed information about the user who is currently
    authenticated and making the request. It provides user identity, roles,
    and account status information.
    
    Returns:
        GetUserResponse: Response object containing:
            - user_id: Unique identifier for the user
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
        >>> print(f"Email verified: {result.email_verified}")
        
        Check if user is admin:
        >>> result = who_am_i()
        >>> if result.is_admin:
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
    logger.info("Retrieving current user information")
    
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()
    logger.debug(f"User authenticated: {app_user.login_id} (ID: {app_user.app_user_id})")
    
    try:
        # Create response with current user information using existing helper
        logger.debug(f"Preparing user information for {app_user.login_id}")
        result = _create_user_result(app_user)
        logger.info(f"Retrieved user information for {app_user.login_id}")
        return result
    except Exception as e:
        logger.error(f"Failed to retrieve current user information: {str(e)}", exc_info=True)
        raise ToolError(
            f"An unexpected error occurred while retrieving your user information: {str(e)}. Please contact your system administrator.") from e


# Helper functions (placed after their usage)

def _create_user_result(user) -> GetUserResponse:
    """
    Create a user result from an AppUser model instance.
    
    Args:
        user: AppUser model instance
        
    Returns:
        GetUserResponse: Formatted user result
    """
    return GetUserResponse(
        user_id=user.app_user_id,
        login_id=user.login_id,
        username=user.name,
        organization=user.organization,
        email=user.email,
        roles=_get_user_roles(user),
        is_enabled=user.is_enabled
    )
