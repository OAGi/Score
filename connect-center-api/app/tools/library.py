"""
MCP Tools for managing Library operations in connectCenter.

This module provides Model Context Protocol (MCP) tools for querying and retrieving
Libraries, which are organizational containers that group related releases and components
in connectCenter. Libraries serve as logical partitions that organize business information
standards, Core Components, and related artifacts by domain, organization, or purpose.
Each library can represent a specific standards body (such as UN/CEFACT or OAGi), an
industry domain (such as healthcare or finance), or an organization's internal component
repository.

Libraries enable users to manage multiple standards and component sets within a single
connectCenter instance, supporting scenarios where different standards need to be maintained
separately while still allowing cross-library references and dependencies. Common examples
include standard libraries like connectSpec (OAGi's standard library) and custom libraries
created by organizations for their specific business needs. The tools provide a standardized
MCP interface, enabling clients to interact with Library data programmatically.

Available Tools:
- get_libraries: Retrieve paginated lists of libraries with optional filters for name,
  type, organization, domain, state, description, and is_default flag. Supports date
  range filtering and custom sorting.

- get_library: Retrieve a single library by its ID, including creator and last_updater
  information.

Key Features:
- Filter by library attributes (name, type, organization, domain, state, description)
- Filter by default library flag
- Date range filtering for creation and last update timestamps
- Support for pagination and multi-column sorting
- Comprehensive error handling and validation
- Structured response models with creator/updater metadata

The tools provide a clean, consistent interface for accessing Library data through the MCP protocol.
All operations require proper authentication and authorization.
"""

from __future__ import annotations

import logging
from typing import Annotated, Any

from fastmcp import FastMCP
from fastmcp.dependencies import Depends
from fastmcp.exceptions import ToolError
from pydantic import Field
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.vendor_plugins import get_vendor_plugin
from app.utils.date import parse_date_range
from app.security import AuthenticatedUser
from app.services.library_service import LibraryService
from app.tools import _to_tool_error, get_tool_authenticated_user, str_to_bool, tool_session
from app.tools.models.library import GetLibraryPaginationResponse, GetLibraryResponse, LibraryResponseEntry

logger = logging.getLogger("connectcenter.mcp.library")

mcp = FastMCP("connectCenter MCP - Library Tools")


async def get_library_service(
    session: AsyncSession = Depends(tool_session),
) -> LibraryService:
    """Provide a requester-scoped library service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    return _build_library_service(session, requester)


@mcp.tool(
    name="get_libraries",
    description="Get a paginated list of libraries.",
    output_schema={
        "type": "object",
        "description": "Response containing paginated list of libraries.",
        "properties": {
            "total_items": {"type": "integer", "description": "Total number of libraries available. Allowed values: non-negative integers (≥0).", "example": 5},
            "offset": {"type": "integer", "description": "Offset of the first item in this page. Allowed values: non-negative integers (≥0). Default value: 0.", "example": 0},
            "limit": {"type": "integer", "description": "Number of items returned in this page. Allowed values: integers between 1 and 100 (inclusive). Default value: 10.", "example": 10},
            "items": {
                "type": "array",
                "description": "List of libraries on this page",
                "items": {
                    "type": "object",
                    "properties": {
                        "library_id": {"type": "integer", "description": "Unique identifier for the library", "example": 1},
                        "name": {"type": "string", "description": "Name of the library", "example": "connectSpec"},
                        "type": {"type": ["string", "null"], "description": "Type of the library", "example": "Standard"},
                        "organization": {"type": ["string", "null"], "description": "Organization that owns the library", "example": "OAGI"},
                        "description": {"type": ["string", "null"], "description": "Description of the library", "example": "Core specification library"},
                        "link": {"type": ["string", "null"], "description": "URL link to the library", "example": "https://oagi.org"},
                        "domain": {"type": ["string", "null"], "description": "Domain of the library", "example": "Enterprise Interoperability"},
                        "state": {"type": ["string", "null"], "description": "Current state of the library", "example": "Published"},
                        "is_read_only": {"type": "boolean", "description": "Whether the library is read-only", "example": False},
                        "is_default": {"type": "boolean", "description": "Whether this is the default library", "example": True},
                        "created": {
                            "type": "object",
                            "description": "Information about the creation of the library",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who created the library",
                                    "properties": {
                                        "user_id": {"type": "integer", "description": "Unique identifier for the user", "example": 1},
                                        "login_id": {"type": "string", "description": "User's login identifier", "example": "admin"},
                                        "username": {"type": "string", "description": "Display name of the user", "example": "Administrator"},
                                        "roles": {"type": "array", "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]}, "description": "List of roles assigned to the user", "example": ["Admin"]}
                                    },
                                    "required": ["user_id", "login_id", "username", "roles"]
                                },
                                "when": {"type": "string", "format": "date-time", "description": "Creation timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)", "example": "2024-01-15T10:30:00Z"}
                            },
                            "required": ["who", "when"]
                        },
                        "last_updated": {
                            "type": "object",
                            "description": "Information about the last update of the library",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who last updated the library",
                                    "properties": {
                                        "user_id": {"type": "integer", "description": "Unique identifier for the user", "example": 1},
                                        "login_id": {"type": "string", "description": "User's login identifier", "example": "admin"},
                                        "username": {"type": "string", "description": "Display name of the user", "example": "Administrator"},
                                        "roles": {"type": "array", "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]}, "description": "List of roles assigned to the user", "example": ["Admin"]}
                                    },
                                    "required": ["user_id", "login_id", "username", "roles"]
                                },
                                "when": {"type": "string", "format": "date-time", "description": "Last update timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)", "example": "2024-01-20T14:45:00Z"}
                            },
                            "required": ["who", "when"]
                        }
                    },
                    "required": ["library_id", "name", "is_read_only", "is_default", "created", "last_updated"]
                }
            }
        },
        "required": ["total_items", "offset", "limit", "items"]
    }
)
async def get_libraries(
    name: Annotated[str | None, Field(default=None, description="Filter by library name using partial match (case-insensitive).")],
    type: Annotated[str | None, Field(default=None, description="Filter by library type using partial match (case-insensitive).")],
    organization: Annotated[str | None, Field(default=None, description="Filter by organization using partial match (case-insensitive).")],
    domain: Annotated[str | None, Field(default=None, description="Filter by domain using partial match (case-insensitive).")],
    state: Annotated[str | None, Field(default=None, description="Filter by state using partial match (case-insensitive).")],
    description: Annotated[str | None, Field(default=None, description="Filter by description using partial match (case-insensitive).")],
    is_default: Annotated[bool | str | None, Field(default=None, description="Filter by default library flag.")],
    created_on: Annotated[str | None, Field(default=None, description="Filter by creation date using an inclusive range: '[before~after]'.")],
    last_updated_on: Annotated[str | None, Field(default=None, description="Filter by last update date using an inclusive range: '[before~after]'.")],
    order_by: Annotated[str | None, Field(default=None, description="Comma-separated list of properties to order results by. Allowed columns: name, type, organization, domain, state, description, is_default, creation_timestamp, last_update_timestamp.")],
    offset: Annotated[int, Field(default=0, ge=0, description="The offset from the beginning of the list.")],
    limit: Annotated[int, Field(default=10, ge=1, le=100, description="The maximum number of items to return.")],
    library_service: LibraryService = Depends(get_library_service),
) -> GetLibraryPaginationResponse:
    """
    Get a paginated list of libraries.

    This function retrieves libraries with support for pagination, filtering,
    and sorting. It returns detailed information about each library including creation
    and update metadata, and library-specific attributes.

    Args:
        name (str | None, optional): Filter by library name using partial match (case-insensitive). Defaults to None.
        type (str | None, optional): Filter by library type using partial match (case-insensitive). Defaults to None.
        organization (str | None, optional): Filter by organization using partial match (case-insensitive). Defaults to None.
        domain (str | None, optional): Filter by domain using partial match (case-insensitive). Defaults to None.
        state (str | None, optional): Filter by state using exact match (case-sensitive). Defaults to None.
        description (str | None, optional): Filter by description using partial match (case-insensitive). Defaults to None.
        is_default (bool | str | None, optional): Filter by default library flag using exact match. Accepts bool, str ('True'/'true'/'1' for True, 'False'/'false'/'0' for False), or None. Defaults to None.
        created_on (str | None, optional): Filter by creation date using an inclusive range: '[before~after]'.
            'before' and 'after' are date-time strings. Default date format: YYYY-MM-DD.
            Examples: '[2025-01-01~2025-02-01]'. Either 'before' or 'after' can be omitted,
            e.g., '[~2025-02-01]' or '[2025-01-01~]'. Defaults to None.
        last_updated_on (str | None, optional): Filter by last update date using an inclusive range: '[before~after]'.
            'before' and 'after' are date-time strings. Default date format: YYYY-MM-DD.
            Examples: '[2025-01-01~2025-02-01]'. Either 'before' or 'after' can be omitted,
            e.g., '[~2025-02-01]' or '[2025-01-01~]'. Defaults to None.
        order_by (str | None, optional): Comma-separated list of properties to order results by.
            Prefix with '-' for descending, '+' for ascending (default ascending).
            Allowed columns: name, type, organization, domain, state, description, is_default, creation_timestamp, last_update_timestamp.
            Example: '-creation_timestamp,+name' translates to 'creation_timestamp DESC, name ASC'.
            Defaults to None.
        offset (int, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int, optional): The maximum number of items to return. Must be between 1 and 100 (inclusive). Defaults to 10.

    Returns:
        GetLibraryPaginationResponse: Response object containing:
            - total_items: Total number of libraries available
            - offset: Offset of the first item in this page
            - limit: Number of items returned in this page
            - items: List of libraries on this page with detailed information

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
        >>> result = get_libraries()
        >>> print(f"Found {result.total_items} libraries")

        Filtered search:
        >>> result = get_libraries(
        ...     name="OAGIS",
        ...     type="Standard",
        ...     limit=5
        ... )

        Date range filtering:
        >>> result = get_libraries(
        ...     created_on="[2024-01-01~2024-12-31]"
        ... )

        Custom ordering:
        >>> result = get_libraries(
        ...     order_by="-creation_timestamp,+name"
        ... )
    """
    try:
        is_default = str_to_bool(is_default)
    except ToolError:
        raise
    except Exception as e:
        raise ToolError(
            f"Type conversion error: {str(e)}. Please check your parameter types and try again."
        ) from e

    try:
        page = await library_service.list(
            limit=limit,
            offset=offset,
            order_by=order_by,
            name=name,
            type=type,
            organization=organization,
            domain=domain,
            state=state,
            description=description,
            is_default=is_default,
            created_on=parse_date_range(created_on),
            last_updated_on=parse_date_range(last_updated_on),
        )
        return _to_list_response(items=page.items, total=page.total, offset=page.offset, limit=page.limit)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to retrieve libraries.") from exc


@mcp.tool(
    name="get_library",
    description="Get a specific library by ID",
    output_schema={
        "type": "object",
        "description": "Response containing library information",
        "properties": {
            "library_id": {"type": "integer", "description": "Unique identifier for the library", "example": 1},
            "name": {"type": "string", "description": "Name of the library", "example": "connectSpec"},
            "type": {"type": ["string", "null"], "description": "Type of the library", "example": "Standard"},
            "organization": {"type": ["string", "null"], "description": "Organization that owns the library", "example": "OAGI"},
            "description": {"type": ["string", "null"], "description": "Description of the library", "example": "Core specification library"},
            "link": {"type": ["string", "null"], "description": "URL link to the library", "example": "https://oagi.org"},
            "domain": {"type": ["string", "null"], "description": "Domain of the library", "example": "Enterprise Interoperability"},
            "state": {"type": ["string", "null"], "description": "Current state of the library", "example": "Published"},
            "is_read_only": {"type": "boolean", "description": "Whether the library is read-only", "example": False},
            "is_default": {"type": "boolean", "description": "Whether this is the default library", "example": True},
            "created": {
                "type": "object",
                "description": "Information about the creation of the library",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who created the library",
                        "properties": {
                            "user_id": {"type": "integer", "description": "Unique identifier for the user", "example": 1},
                            "login_id": {"type": "string", "description": "User's login identifier", "example": "admin"},
                            "username": {"type": "string", "description": "Display name of the user", "example": "Administrator"},
                            "roles": {"type": "array", "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]}, "description": "List of roles assigned to the user", "example": ["Admin"]}
                        },
                        "required": ["user_id", "login_id", "username", "roles"]
                    },
                    "when": {"type": "string", "format": "date-time", "description": "Creation timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)", "example": "2024-01-15T10:30:00Z"}
                },
                "required": ["who", "when"]
            },
            "last_updated": {
                "type": "object",
                "description": "Information about the last update of the library",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who last updated the library",
                        "properties": {
                            "user_id": {"type": "integer", "description": "Unique identifier for the user", "example": 1},
                            "login_id": {"type": "string", "description": "User's login identifier", "example": "admin"},
                            "username": {"type": "string", "description": "Display name of the user", "example": "Administrator"},
                            "roles": {"type": "array", "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]}, "description": "List of roles assigned to the user", "example": ["Admin"]}
                        },
                        "required": ["user_id", "login_id", "username", "roles"]
                    },
                    "when": {"type": "string", "format": "date-time", "description": "Last update timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)", "example": "2024-01-20T14:45:00Z"}
                },
                "required": ["who", "when"]
            }
        },
        "required": ["library_id", "name", "is_read_only", "is_default", "created", "last_updated"]
    }
)
async def get_library(
    library_id: Annotated[int, Field(gt=0, description="Unique numeric identifier of the library to retrieve.")],
    library_service: LibraryService = Depends(get_library_service),
) -> GetLibraryResponse:
    """
    Get a specific library by ID.

    This function retrieves a single library from the database and returns
    detailed information including metadata about who created it and when it was
    last updated, along with library-specific attributes.

    Args:
        library_id (int): The unique identifier of the library to fetch

    Returns:
        GetLibraryResponse: Response object containing:
            - library_id: Unique identifier for the library
            - name: Library name
            - type: Type of the library
            - organization: Organization responsible for the library
            - description: Brief summary of the library's purpose
            - link: URL to library homepage or documentation
            - domain: Application domain of the library
            - state: Current state of the library
            - is_read_only: Whether the library is read-only
            - is_default: Whether the library is the default
            - created: Information about the creation of the library
            - last_updated: Information about the last update of the library

    Raises:
        ToolError: If validation fails, the library is not found, or other errors occur.
            Common error scenarios include:
            - Library with the specified ID does not exist
            - Database connection issues
            - Authentication failures

    Examples:
        Get a specific library:
        >>> result = get_library(library_id=123)
        >>> print(f"Library: {result.name} (Type: {result.type})")
        >>> print(f"Organization: {result.organization}")
        >>> print(f"State: {result.state}")

        Access library details:
        >>> result = get_library(library_id=123)
        >>> if result.link:
        ...     print(f"Link: {result.link}")
        >>> print(f"Is default: {result.is_default}")
    """
    try:
        row = await library_service.get(library_id)
        if row is None:
            raise ValueError(f"The library with ID {library_id} was not found. Please check the ID and try again.")
        return GetLibraryResponse.model_validate(row, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to retrieve library {library_id}.") from exc


def _build_library_service(
    session: AsyncSession,
    requester: AuthenticatedUser,
) -> LibraryService:
    """Construct the library service for an MCP request."""
    plugin = get_vendor_plugin()
    return LibraryService(
        plugin.create_library_repository(session),
        plugin.create_app_user_repository(session),
        requester=requester,
    )


def _to_list_response(*, items: list[Any], total: int, offset: int, limit: int) -> GetLibraryPaginationResponse:
    """Build the paginated MCP response model."""
    return GetLibraryPaginationResponse(
        total_items=total,
        offset=offset,
        limit=limit,
        items=[LibraryResponseEntry.model_validate(item, from_attributes=True) for item in items],
    )
