"""
MCP Tools for managing Release operations in connectCenter.

This module provides Model Context Protocol (MCP) tools for querying and retrieving
Releases, which represent versions of business information standards within a library.
Releases serve as version control and publication mechanisms for business information
standards, enabling organizations to manage the lifecycle of components, data types, code
lists, and other artifacts. Each release represents a snapshot of a library's content at
a specific point in time, allowing users to work with stable, published versions while
developing new versions in parallel.

Releases enable version management by tracking the evolution of standards over time,
supporting scenarios where multiple versions of the same standard need to coexist (such
as supporting both current and legacy versions). They also support dependency management,
allowing releases to reference components from other releases, which is essential for
maintaining compatibility and managing cross-release relationships. Common examples include
numbered releases (e.g., "10.6", "10.7") that represent published versions of standards
libraries. The tools provide a standardized MCP interface, enabling clients to interact
with Release data programmatically, including dependency management.

Available Tools:
- get_releases: Retrieve paginated lists of releases with optional filters for
  library_id, release_num, and state. Supports date range filtering and custom
  sorting. Automatically excludes releases with release_num='Working'.

- get_release: Retrieve a single release by its ID, including library, namespace,
  creator, last_updater, and linked releases (prev_release, next_release).

- get_working_release: Retrieve the `Working` release for a library by `library_id`.

Key Features:
- Full relationship loading (library, namespace, creator, last_updater, prev_release, next_release)
- Support for filtering, pagination, and sorting
- Automatic exclusion of 'Working' releases from queries
- Comprehensive error handling and validation
- Structured response models with detailed metadata

The tools provide a clean, consistent interface for accessing Release data through the MCP protocol.
All operations require proper authentication and authorization.
"""

from __future__ import annotations

import logging
from typing import Annotated, Any

from fastmcp import FastMCP
from fastmcp.dependencies import Depends
from pydantic import Field
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.vendor_plugins import get_vendor_plugin
from app.utils.date import parse_date_range
from app.security import AuthenticatedUser
from app.services.release_service import ReleaseService
from app.tools import _to_tool_error, get_tool_authenticated_user, tool_session
from app.tools.models.release import GetReleasePaginationResponse, GetReleaseResponse, ReleaseResponseEntry

logger = logging.getLogger("connectcenter.mcp.release")

mcp = FastMCP("connectCenter MCP - Release Tools")

_RELEASE_OUTPUT_SCHEMA = {
    "type": "object",
    "description": "Response containing release information",
    "properties": {
        "release_id": {"type": "integer", "description": "Unique identifier for the release", "example": 1},
        "library": {
            "type": "object",
            "description": "Library information",
            "properties": {
                "library_id": {"type": "integer", "description": "Unique identifier for the library", "example": 1},
                "name": {"type": "string", "description": "Library name", "example": "connectSpec"}
            },
            "required": ["library_id", "name"]
        },
        "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
        "release_num": {"type": "string", "description": "Release number", "example": "10.6"},
        "release_note": {"type": ["string", "null"], "description": "Release notes", "example": "Major update with new features"},
        "release_license": {"type": ["string", "null"], "description": "Release license information", "example": "MIT License"},
        "namespace": {
            "type": ["object", "null"],
            "description": "Namespace information",
            "properties": {
                "namespace_id": {"type": "integer", "description": "Unique identifier for the namespace", "example": 1},
                "uri": {"type": "string", "description": "Namespace URI (Uniform Resource Identifier)", "example": "http://www.openapplications.org/oagis/10"},
                "prefix": {"type": ["string", "null"], "description": "Namespace prefix", "example": "oagis"}
            },
            "required": ["namespace_id", "uri"]
        },
        "state": {"type": "string", "enum": ["Processing", "Initialized", "Draft", "Published"], "description": "Release state", "example": "Published"},
        "created": {
            "type": "object",
            "description": "Information about the creation of the release",
            "properties": {
                "who": {
                    "type": "object",
                    "description": "User who created the release",
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
            "description": "Information about the last update of the release",
            "properties": {
                "who": {
                    "type": "object",
                    "description": "User who last updated the release",
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
    "required": ["release_id", "library", "guid", "release_num", "state", "created", "last_updated"]
}


async def get_release_service(
    session: AsyncSession = Depends(tool_session),
) -> ReleaseService:
    """Provide a requester-scoped release service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    return _build_release_service(session, requester)


@mcp.tool(
    name="get_releases",
    description="Get a paginated list of releases",
    output_schema={
        "type": "object",
        "description": "Response containing paginated list of releases",
        "properties": {
            "total_items": {"type": "integer", "description": "Total number of releases available. Allowed values: non-negative integers (≥0).", "example": 15},
            "offset": {"type": "integer", "description": "Offset of the first item in this page. Allowed values: non-negative integers (≥0). Default value: 0.", "example": 0},
            "limit": {"type": "integer", "description": "Number of items returned in this page. Allowed values: integers between 1 and 100 (inclusive). Default value: 10.", "example": 10},
            "items": {
                "type": "array",
                "description": "List of releases on this page",
                "items": {
                    "type": "object",
                    "properties": {
                        "release_id": {"type": "integer", "description": "Unique identifier for the release", "example": 1},
                        "library": {
                            "type": "object",
                            "description": "Library information",
                            "properties": {
                                "library_id": {"type": "integer", "description": "Unique identifier for the library", "example": 1},
                                "name": {"type": "string", "description": "Library name", "example": "connectSpec"}
                            },
                            "required": ["library_id", "name"]
                        },
                        "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                        "release_num": {"type": "string", "description": "Release number", "example": "10.6"},
                        "release_note": {"type": ["string", "null"], "description": "Release notes", "example": "Major update with new features"},
                        "release_license": {"type": ["string", "null"], "description": "Release license information", "example": "MIT License"},
                        "namespace": {
                            "type": ["object", "null"],
                            "description": "Namespace information",
                            "properties": {
                                "namespace_id": {"type": "integer", "description": "Unique identifier for the namespace", "example": 1},
                                "uri": {"type": "string", "description": "Namespace URI (Uniform Resource Identifier)", "example": "http://www.openapplications.org/oagis/10"},
                                "prefix": {"type": ["string", "null"], "description": "Namespace prefix", "example": "oagis"}
                            },
                            "required": ["namespace_id", "uri"]
                        },
                        "state": {"type": "string", "enum": ["Processing", "Initialized", "Draft", "Published"], "description": "Release state", "example": "Published"},
                        "created": {
                            "type": "object",
                            "description": "Information about the creation of the release",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who created the release",
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
                            "description": "Information about the last update of the release",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who last updated the release",
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
                    "required": ["release_id", "library", "guid", "release_num", "state", "created", "last_updated"]
                }
            }
        },
        "required": ["total_items", "offset", "limit", "items"]
    }
)
async def get_releases(
    library_id: Annotated[int, Field(gt=0, description="Filter by library ID using exact match.")],
    release_num: Annotated[str | None, Field(default=None, description="Filter by release number using partial match (case-insensitive).")],
    state: Annotated[str | None, Field(default=None, description="Filter by state using exact match.")],
    created_on: Annotated[str | None, Field(default=None, description="Filter by creation date using an inclusive range: '[before~after]'.")],
    last_updated_on: Annotated[str | None, Field(default=None, description="Filter by last update date using an inclusive range: '[before~after]'.")],
    order_by: Annotated[str | None, Field(default=None, description="Comma-separated list of properties to order results by. Allowed columns: release_num, state, creation_timestamp, last_update_timestamp.")],
    offset: Annotated[int, Field(default=0, ge=0, description="The offset from the beginning of the list.")],
    limit: Annotated[int, Field(default=10, ge=1, le=100, description="The maximum number of items to return.")],
    release_service: ReleaseService = Depends(get_release_service),
) -> GetReleasePaginationResponse:
    """
    Get a paginated list of releases.

    This function retrieves releases with support for pagination, filtering,
    and sorting. It returns detailed information about each release including creation
    and update metadata, and release-specific attributes.

    Args:
        library_id (int): Filter by library ID using exact match (required).
        release_num (str | None, optional): Filter by release number using partial match (case-insensitive). Defaults to None.
        state (str | None, optional): Filter by state using exact match (case-sensitive). Defaults to None.
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
            Allowed columns: release_num, state, creation_timestamp, last_update_timestamp.
            Example: '-creation_timestamp,+release_num' translates to 'creation_timestamp DESC, release_num ASC'.
            Defaults to None.
        offset (int, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int, optional): The maximum number of items to return. Must be between 1 and 100 (inclusive). Defaults to 10.

    Returns:
        GetReleasePaginationResponse: Response object containing:
            - total_items: Total number of releases available
            - offset: Offset of the first item in this page
            - limit: Number of items returned in this page
            - items: List of releases on this page with detailed information

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
        >>> result = get_releases(library_id=123)
        >>> print(f"Found {result.total_items} releases")

        Filtered search:
        >>> result = get_releases(
        ...     library_id=123,
        ...     state="Published",
        ...     limit=5
        ... )

        Date range filtering:
        >>> result = get_releases(
        ...     library_id=123,
        ...     created_on="[2024-01-01~2024-12-31]"
        ... )

        Custom ordering:
        >>> result = get_releases(
        ...     library_id=123,
        ...     order_by="-creation_timestamp,+release_num"
        ... )
    """
    try:
        page = await release_service.list(
            limit=limit,
            offset=offset,
            order_by=order_by,
            library_id=library_id,
            release_num=release_num,
            state=state,
            created_on=parse_date_range(created_on),
            last_updated_on=parse_date_range(last_updated_on),
        )
        return _to_list_response(items=page.items, total=page.total, offset=page.offset, limit=page.limit)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to retrieve releases.") from exc


@mcp.tool(
    name="get_release",
    description="Get a specific release by ID",
    output_schema=_RELEASE_OUTPUT_SCHEMA
)
async def get_release(
    release_id: Annotated[int, Field(gt=0, description="Unique numeric identifier of the release to retrieve.")],
    release_service: ReleaseService = Depends(get_release_service),
) -> GetReleaseResponse:
    """
    Get a specific release by ID.

    This function retrieves a single release from the database and returns
    detailed information including metadata about who created it and when it was
    last updated, along with release-specific attributes and library information.

    Args:
        release_id (int): The unique identifier of the release to fetch

    Returns:
        GetReleaseResponse: Response object containing:
            - release_id: Unique identifier for the release
            - library: Library information object with library_id and name
            - guid: Globally unique identifier for the release
            - release_num: Release number (e.g., "10.0", "10.1")
            - release_note: Description or note associated with the release
            - release_license: License associated with the release
            - namespace: Namespace information object with namespace_id, prefix, and uri (if any)
            - state: Current state of the release
            - created: Information about the creation of the release
            - last_updated: Information about the last update of the release

    Raises:
        ToolError: If validation fails, the release is not found, or other errors occur.
            Common error scenarios include:
            - Release with the specified ID does not exist
            - Database connection issues
            - Authentication failures

    Examples:
        Get a specific release:
        >>> result = get_release(release_id=123)
        >>> print(f"Release: {result.release_num} (State: {result.state})")
        >>> print(f"Created by: {result.created.who.username}")
        >>> print(f"Library: {result.library.name}")

        Access release details:
        >>> result = get_release(release_id=123)
        >>> if result.namespace:
        ...     print(f"Namespace: {result.namespace.uri}")
        >>> print(f"Release note: {result.release_note}")
    """
    try:
        row = await release_service.get(release_id)
        if row is None:
            raise ValueError(f"The release with ID {release_id} was not found. Please check the ID and try again.")
        return GetReleaseResponse.model_validate(row, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to retrieve release {release_id}.") from exc


@mcp.tool(
    name="get_working_release",
    description="Get the `Working` release for a specific library",
    output_schema=_RELEASE_OUTPUT_SCHEMA,
)
async def get_working_release(
    library_id: Annotated[int, Field(gt=0, description="Unique numeric identifier of the library whose `Working` release should be retrieved.")],
    release_service: ReleaseService = Depends(get_release_service),
) -> GetReleaseResponse:
    """
    Get the `Working` release for a specific library.

    This function retrieves the library's exact `Working` release, which is
    especially useful for Developer flows that need a valid editable release_id
    before calling other tools such as `create_acc`.

    Args:
        library_id (int): The unique identifier of the library to inspect

    Returns:
        GetReleaseResponse: Response object containing the library's `Working` release details.

    Raises:
        ToolError: If the library has no `Working` release, the library ID is invalid,
            or other retrieval errors occur.
    """
    try:
        row = await release_service.get_by_library_id_and_release_num(
            library_id=library_id,
            release_num="Working",
        )
        if row is None:
            raise ValueError(
                f"The `Working` release for library ID {library_id} was not found. Please check the library ID and try again."
            )
        return GetReleaseResponse.model_validate(row, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to retrieve the Working release for library {library_id}.") from exc


def _build_release_service(
    session: AsyncSession,
    requester: AuthenticatedUser,
) -> ReleaseService:
    """Construct the release service for an MCP request."""
    plugin = get_vendor_plugin()
    return ReleaseService(
        plugin.create_release_repository(session),
        plugin.create_app_user_repository(session),
        requester=requester,
    )


def _to_list_response(*, items: list[Any], total: int, offset: int, limit: int) -> GetReleasePaginationResponse:
    """Build the paginated MCP response model."""
    return GetReleasePaginationResponse(
        total_items=total,
        offset=offset,
        limit=limit,
        items=[ReleaseResponseEntry.model_validate(item, from_attributes=True) for item in items],
    )
