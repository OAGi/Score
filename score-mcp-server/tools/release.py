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
- get_release: Retrieve a single release by its ID, including library, namespace,
  creator, last_updater, and linked releases (prev_release, next_release).

- get_releases: Retrieve paginated lists of releases with optional filters for
  library_id, release_num, and state. Supports date range filtering and custom
  sorting. Automatically excludes releases with release_num='Working'.

Key Features:
- Full relationship loading (library, namespace, creator, last_updater, prev_release, next_release)
- Support for filtering, pagination, and sorting
- Automatic exclusion of 'Working' releases from queries
- Comprehensive error handling and validation
- Structured response models with detailed metadata

The tools provide a clean, consistent interface for accessing Release data through the MCP protocol.
All operations require proper authentication and authorization.
"""

import logging
from typing import Annotated

from fastapi import HTTPException
from fastmcp import FastMCP
from fastmcp.exceptions import ToolError
from pydantic import Field

from services import ReleaseService, DateRangeParams, PaginationParams
from tools import _validate_auth_and_db, parse_order_by_to_sorts, _create_user_info
from tools.models.release import (
    GetReleaseResponse,
    GetReleasesResponse,
)
from tools.models.common import LibraryInfo, NamespaceInfo, WhoAndWhen
from tools.utils import parse_date_range

# Configure logging
logger = logging.getLogger(__name__)

mcp = FastMCP("Score MCP Server - Release Tools")


@mcp.tool(
    name="get_release",
    description="Get a specific release by ID",
    output_schema={
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
)
async def get_release(
        release_id: Annotated[int, Field(
            description="Unique numeric identifier of the release to retrieve.",
            examples=[123, 456, 789],
            gt=0,
            title="Release ID"
        )]
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
        # Validate authentication and get database connection
        app_user, engine = _validate_auth_and_db()

        # Create service instance
        release_service = ReleaseService()

        # Get the release
        release = release_service.get_release(release_id)

        # Convert to response format
        result = _create_release_result(release)

        logger.info(f"Successfully retrieved release {release_id}")
        return result

    except ToolError:
        raise
    except HTTPException as e:
        logger.error(f"HTTP error retrieving release: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The release with ID {release_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving release: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the release: {str(e)}. Please contact your system administrator.") from e


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
        library_id: Annotated[int, Field(
            description="Filter by library ID using exact match.",
            examples=[123, 456, 789],
            gt=0,
            title="Library ID"
        )],
        offset: Annotated[int, Field(
            description="The offset from the beginning of the list. Allowed values: non-negative integers (≥0). Default value: 0.",
            examples=[0, 10, 20],
            ge=0,
            title="Offset"
        )] = 0,
        limit: Annotated[int, Field(
            description="The maximum number of items to return. Allowed values: integers between 1 and 100 (inclusive). Default value: 10.",
            examples=[10, 25, 50],
            ge=1,
            le=100,
            title="Limit"
        )] = 10,
        release_num: Annotated[str | None, Field(
            description="Filter by release number using partial match (case-insensitive).",
            examples=["10.6", "2.1", "1.0"],
            title="Release Number"
        )] = None,
        state: Annotated[str | None, Field(
            description="Filter by state using exact match (case-sensitive).",
            examples=["Published", "Draft", "Deprecated"],
            title="State"
        )] = None,
        created_on: Annotated[str | None, Field(
            description="Filter by creation date using an inclusive range: '[before~after]'. 'before' and 'after' are date-time strings. Default date format: YYYY-MM-DD. Examples: '[2025-01-01~2025-02-01]'. Either 'before' or 'after' can be omitted, e.g., '[~2025-02-01]' or '[2025-01-01~]'.",
            examples=["[2025-01-01~2025-02-01]", "[~2025-02-01]", "[2025-01-01~]"],
            title="Created On Date Range"
        )] = None,
        last_updated_on: Annotated[str | None, Field(
            description="Filter by last update date using an inclusive range: '[before~after]'. 'before' and 'after' are date-time strings. Default date format: YYYY-MM-DD. Examples: '[2025-01-01~2025-02-01]'. Either 'before' or 'after' can be omitted, e.g., '[~2025-02-01]' or '[2025-01-01~]'.",
            examples=["[2025-01-01~2025-02-01]", "[~2025-02-01]", "[2025-01-01~]"],
            title="Last Updated On Date Range"
        )] = None,
        order_by: Annotated[str | None, Field(
            description="Comma-separated list of properties to order results by. Prefix with '-' for descending, '+' for ascending (default ascending). Allowed columns: release_num, state, creation_timestamp, last_update_timestamp. Example: '-creation_timestamp,+release_num' translates to 'creation_timestamp DESC, release_num ASC'.",
            examples=["-creation_timestamp,+release_num", "release_num", "-last_update_timestamp"],
            title="Order By"
        )] = None
) -> GetReleasesResponse:
    """
    Get a paginated list of releases.
    
    This function retrieves releases with support for pagination, filtering,
    and sorting. It returns detailed information about each release including creation
    and update metadata, and release-specific attributes.
    
    Args:
        library_id (int): Filter by library ID using exact match.
        offset (int | None, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int | None, optional): The maximum number of items to return. Must be a non-negative number. Defaults to 10.
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
    
    Returns:
        GetReleasesResponse: Response object containing:
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
        # Validate authentication and get database connection
        app_user, engine = _validate_auth_and_db()

        # Create service instance
        release_service = ReleaseService()

        # Validate and create date range parameters
        created_on_params = None
        last_updated_on_params = None

        if created_on:
            try:
                before_date, after_date = parse_date_range(created_on)
                created_on_params = DateRangeParams(before=before_date, after=after_date)
            except ValueError as e:
                raise ToolError(
                    f"Created_on date range validation failed: {str(e)}. Please use format [before~after] or [~after] or [before~] with YYYY-MM-DD dates.") from e

        if last_updated_on:
            try:
                before_date, after_date = parse_date_range(last_updated_on)
                last_updated_on_params = DateRangeParams(before=before_date, after=after_date)
            except ValueError as e:
                raise ToolError(
                    f"Last_updated_on date range validation failed: {str(e)}. Please use format [before~after] or [~after] or [before~] with YYYY-MM-DD dates.") from e

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
            except ValueError as e:
                raise ToolError(
                    f"Invalid order_by format: {str(e)}. Please use format: '(-|+)?<column_name>(,(-|+)?<column_name>)*'. "
                    f"Valid columns: {", ".join(release_service.allowed_columns_for_order_by)}") from e

        # Get releases
        page = release_service.get_releases(
            library_id=library_id,
            release_num=release_num,
            state=state,
            created_on_params=created_on_params,
            last_updated_on_params=last_updated_on_params,
            pagination=pagination,
            sort_list=sort_list
        )

        # Convert to response format
        release_results = [_create_release_result(release) for release in page.items]

        result = GetReleasesResponse(
            total_items=page.total,
            offset=page.offset,
            limit=page.limit,
            items=release_results
        )

        logger.info(f"Successfully retrieved {len(page.items)} releases (total: {page.total})")
        return result

    except ToolError:
        raise
    except HTTPException as e:
        logger.error(f"HTTP error retrieving releases: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving releases: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the releases: {str(e)}. Please contact your system administrator.") from e


# Helper functions (placed after their usage)

def _create_release_result(release) -> GetReleaseResponse:
    """
    Create a release result from a Release model instance.
    
    Args:
        release: Release model instance
        
    Returns:
        GetReleaseResponse: Formatted release result
    """
    # Create library info
    library_info = LibraryInfo(
        library_id=release.library_id,
        name=release.library.name if release.library else None
    )
    
    # Create namespace info if available
    namespace_info = None
    if release.namespace:
        namespace_info = NamespaceInfo(
            namespace_id=release.namespace.namespace_id,
            prefix=release.namespace.prefix,
            uri=release.namespace.uri
        )
    
    # Create user info for creator and last_updater, handling None values
    creator_info = _create_user_info(release.creator) if release.creator else None
    last_updater_info = _create_user_info(release.last_updater) if release.last_updater else None
    
    return GetReleaseResponse(
        release_id=release.release_id,
        library=library_info,
        guid=release.guid,
        release_num=release.release_num,
        release_note=release.release_note,
        release_license=release.release_license,
        namespace=namespace_info,
        state=release.state,
        created=WhoAndWhen(who=creator_info, when=release.creation_timestamp),
        last_updated=WhoAndWhen(who=last_updater_info, when=release.last_update_timestamp)
    )
