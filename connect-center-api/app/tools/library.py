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
  type, organization, domain, state, description, is_default flag, and updater. Supports date
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

from fastmcp import Context, FastMCP
from fastmcp.dependencies import Depends
from fastmcp.exceptions import ToolError
from fastmcp.server.elicitation import AcceptedElicitation, CancelledElicitation, DeclinedElicitation
from pydantic import Field
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.vendor_plugins import get_vendor_plugin
from app.security import AuthenticatedUser
from app.services.library_service import LibraryService
from app.tools import _to_tool_error, get_tool_authenticated_user, str_to_bool, tool_session
from app.tools.models.library import (
    CreateLibraryResponse,
    GetLibraryPaginationResponse,
    GetLibraryResponse,
    LibraryResponseEntry,
    ManageLibraryReleaseDependenciesResponse,
    UpdateLibraryResponse,
)
from app.types.unset import UNSET
from app.utils.date import parse_date_range

logger = logging.getLogger("connectcenter.mcp.library")

mcp = FastMCP("connectCenter MCP - Library Tools")

EMPTY_OUTPUT_SCHEMA = {
    "type": "object",
    "description": "Empty response body.",
    "properties": {},
    "additionalProperties": False,
}


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
            "total_items": {
                "type": "integer",
                "description": "Total number of libraries available. Allowed values: non-negative integers (≥0).",
                "example": 5,
            },
            "offset": {
                "type": "integer",
                "description": "Offset of the first item in this page. Allowed values: non-negative integers (≥0). Default value: 0.",
                "example": 0,
            },
            "limit": {
                "type": "integer",
                "description": "Number of items returned in this page. Allowed values: integers between 1 and 100 (inclusive). Default value: 10.",
                "example": 10,
            },
            "items": {
                "type": "array",
                "description": "List of libraries on this page",
                "items": {
                    "type": "object",
                    "properties": {
                        "library_id": {
                            "type": "integer",
                            "description": "Unique identifier for the library",
                            "example": 1,
                        },
                        "name": {"type": "string", "description": "Name of the library", "example": "connectSpec"},
                        "type": {
                            "type": ["string", "null"],
                            "description": "Type of the library",
                            "example": "Standard",
                        },
                        "organization": {
                            "type": ["string", "null"],
                            "description": "Organization that owns the library",
                            "example": "OAGI",
                        },
                        "description": {
                            "type": ["string", "null"],
                            "description": "Description of the library",
                            "example": "Core specification library",
                        },
                        "link": {
                            "type": ["string", "null"],
                            "description": "URL link to the library",
                            "example": "https://oagi.org",
                        },
                        "domain": {
                            "type": ["string", "null"],
                            "description": "Domain of the library",
                            "example": "Enterprise Interoperability",
                        },
                        "state": {
                            "type": ["string", "null"],
                            "description": "Current state of the library",
                            "example": "Published",
                        },
                        "is_read_only": {
                            "type": "boolean",
                            "description": "Whether the library is read-only",
                            "example": False,
                        },
                        "is_default": {
                            "type": "boolean",
                            "description": "Whether this is the default library",
                            "example": True,
                        },
                        "created": {
                            "type": "object",
                            "description": "Information about the creation of the library",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who created the library",
                                    "properties": {
                                        "user_id": {
                                            "type": "integer",
                                            "description": "Unique identifier for the user",
                                            "example": 1,
                                        },
                                        "login_id": {
                                            "type": "string",
                                            "description": "User's login identifier",
                                            "example": "admin",
                                        },
                                        "username": {
                                            "type": "string",
                                            "description": "Display name of the user",
                                            "example": "Administrator",
                                        },
                                        "roles": {
                                            "type": "array",
                                            "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                            "description": "List of roles assigned to the user",
                                            "example": ["Admin"],
                                        },
                                    },
                                    "required": ["user_id", "login_id", "username", "roles"],
                                },
                                "when": {
                                    "type": "string",
                                    "format": "date-time",
                                    "description": "Creation timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)",
                                    "example": "2024-01-15T10:30:00Z",
                                },
                            },
                            "required": ["who", "when"],
                        },
                        "last_updated": {
                            "type": "object",
                            "description": "Information about the last update of the library",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who last updated the library",
                                    "properties": {
                                        "user_id": {
                                            "type": "integer",
                                            "description": "Unique identifier for the user",
                                            "example": 1,
                                        },
                                        "login_id": {
                                            "type": "string",
                                            "description": "User's login identifier",
                                            "example": "admin",
                                        },
                                        "username": {
                                            "type": "string",
                                            "description": "Display name of the user",
                                            "example": "Administrator",
                                        },
                                        "roles": {
                                            "type": "array",
                                            "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                            "description": "List of roles assigned to the user",
                                            "example": ["Admin"],
                                        },
                                    },
                                    "required": ["user_id", "login_id", "username", "roles"],
                                },
                                "when": {
                                    "type": "string",
                                    "format": "date-time",
                                    "description": "Last update timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)",
                                    "example": "2024-01-20T14:45:00Z",
                                },
                            },
                            "required": ["who", "when"],
                        },
                    },
                    "required": ["library_id", "name", "is_read_only", "is_default", "created", "last_updated"],
                },
            },
        },
        "required": ["total_items", "offset", "limit", "items"],
    },
)
async def get_libraries(
    name: Annotated[
        str | None, Field(description="Filter by library name using partial match (case-insensitive).")
    ] = None,
    type: Annotated[
        str | None, Field(description="Filter by library type using partial match (case-insensitive).")
    ] = None,
    organization: Annotated[
        str | None, Field(description="Filter by organization using partial match (case-insensitive).")
    ] = None,
    domain: Annotated[str | None, Field(description="Filter by domain using partial match (case-insensitive).")] = None,
    state: Annotated[str | None, Field(description="Filter by state using partial match (case-insensitive).")] = None,
    description: Annotated[
        str | None, Field(description="Filter by description using partial match (case-insensitive).")
    ] = None,
    is_default: Annotated[bool | str | None, Field(description="Filter by default library flag.")] = None,
    created_on: Annotated[
        str | None, Field(description="Filter by creation date using an inclusive range: '[before~after]'.")
    ] = None,
    last_updated_on: Annotated[
        str | None, Field(description="Filter by last update date using an inclusive range: '[before~after]'.")
    ] = None,
    updater: Annotated[
        str | None,
        Field(
            description="Comma-separated updater login IDs to filter by exact match. Prefix a login ID with '!' to exclude it."
        ),
    ] = None,
    order_by: Annotated[
        str | None,
        Field(
            description="Comma-separated list of properties to order results by. Allowed columns: name, type, organization, domain, state, description, is_default, creation_timestamp, last_update_timestamp."
        ),
    ] = None,
    offset: Annotated[int, Field(ge=0, description="The offset from the beginning of the list.")] = 0,
    limit: Annotated[int, Field(ge=1, le=100, description="The maximum number of items to return.")] = 10,
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
        updater (str | None, optional): Comma-separated updater login IDs using exact match.
            Prefix a login ID with '!' to exclude it. Examples: 'john.doe', 'john.doe,jane.doe',
            '!john.doe', 'john.doe,!jane.doe'. Login IDs cannot contain '!' or ','. Defaults to None.
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
        raise ToolError(f"Type conversion error: {str(e)}. Please check your parameter types and try again.") from e

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
            updater=updater,
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
            "organization": {
                "type": ["string", "null"],
                "description": "Organization that owns the library",
                "example": "OAGI",
            },
            "description": {
                "type": ["string", "null"],
                "description": "Description of the library",
                "example": "Core specification library",
            },
            "link": {
                "type": ["string", "null"],
                "description": "URL link to the library",
                "example": "https://oagi.org",
            },
            "domain": {
                "type": ["string", "null"],
                "description": "Domain of the library",
                "example": "Enterprise Interoperability",
            },
            "state": {
                "type": ["string", "null"],
                "description": "Current state of the library",
                "example": "Published",
            },
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
                            "user_id": {
                                "type": "integer",
                                "description": "Unique identifier for the user",
                                "example": 1,
                            },
                            "login_id": {
                                "type": "string",
                                "description": "User's login identifier",
                                "example": "admin",
                            },
                            "username": {
                                "type": "string",
                                "description": "Display name of the user",
                                "example": "Administrator",
                            },
                            "roles": {
                                "type": "array",
                                "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                "description": "List of roles assigned to the user",
                                "example": ["Admin"],
                            },
                        },
                        "required": ["user_id", "login_id", "username", "roles"],
                    },
                    "when": {
                        "type": "string",
                        "format": "date-time",
                        "description": "Creation timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)",
                        "example": "2024-01-15T10:30:00Z",
                    },
                },
                "required": ["who", "when"],
            },
            "last_updated": {
                "type": "object",
                "description": "Information about the last update of the library",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who last updated the library",
                        "properties": {
                            "user_id": {
                                "type": "integer",
                                "description": "Unique identifier for the user",
                                "example": 1,
                            },
                            "login_id": {
                                "type": "string",
                                "description": "User's login identifier",
                                "example": "admin",
                            },
                            "username": {
                                "type": "string",
                                "description": "Display name of the user",
                                "example": "Administrator",
                            },
                            "roles": {
                                "type": "array",
                                "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                "description": "List of roles assigned to the user",
                                "example": ["Admin"],
                            },
                        },
                        "required": ["user_id", "login_id", "username", "roles"],
                    },
                    "when": {
                        "type": "string",
                        "format": "date-time",
                        "description": "Last update timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)",
                        "example": "2024-01-20T14:45:00Z",
                    },
                },
                "required": ["who", "when"],
            },
            "release_dependencies": {
                "type": "array",
                "description": "Direct dependencies of the library's working release.",
                "items": {
                    "type": "object",
                    "properties": {
                        "release_id": {"type": "integer", "description": "Release identifier", "example": 101},
                        "library_id": {"type": "integer", "description": "Owning library identifier", "example": 3},
                        "library_name": {
                            "type": "string",
                            "description": "Owning library name",
                            "example": "CCTS Data Type Catalogue v3",
                        },
                        "release_num": {"type": "string", "description": "Release number", "example": "3.1"},
                        "state": {"type": "string", "description": "Release lifecycle state", "example": "Published"},
                    },
                    "required": ["release_id", "library_id", "library_name", "release_num", "state"],
                },
            },
        },
        "required": [
            "library_id",
            "name",
            "is_read_only",
            "is_default",
            "created",
            "last_updated",
            "release_dependencies",
        ],
    },
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
            - release_dependencies: Direct dependencies of the library's working release

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


@mcp.tool(
    name="create_library",
    description="Create a library and seed its working release.",
    output_schema={
        "type": "object",
        "description": "Response containing the created library identifier.",
        "properties": {
            "library_id": {"type": "integer", "description": "Created library identifier.", "example": 12},
        },
        "required": ["library_id"],
    },
)
async def create_library(
    name: Annotated[str, Field(min_length=1, description="Library name.")],
    namespace_uri: Annotated[
        str,
        Field(min_length=1, description="URI for the default standard namespace."),
    ],
    namespace_prefix: Annotated[
        str | None,
        Field(default=None, description="Prefix for the default standard namespace."),
    ] = None,
    type: Annotated[str | None, Field(default=None, description="Type of the library.")] = None,
    organization: Annotated[str | None, Field(default=None, description="Owning organization.")] = None,
    link: Annotated[str | None, Field(default=None, description="Library URL.")] = None,
    domain: Annotated[str | None, Field(default=None, description="Library domain.")] = None,
    description: Annotated[str | None, Field(default=None, description="Library description.")] = None,
    library_service: LibraryService = Depends(get_library_service),
) -> CreateLibraryResponse:
    """Create a library."""
    try:
        result = await library_service.create_library(
            type=type,
            name=name,
            organization=organization,
            description=description,
            link=link,
            domain=domain,
            namespace_uri=namespace_uri,
            namespace_prefix=namespace_prefix,
        )
        return CreateLibraryResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to create the library.") from exc


@mcp.tool(
    name="update_library",
    description="Update an existing library.",
    output_schema={
        "type": "object",
        "description": "Response containing the target library identifier and changed fields.",
        "properties": {
            "library_id": {"type": "integer", "description": "Target library identifier.", "example": 12},
            "updates": {
                "type": "array",
                "description": "Updated field names.",
                "items": {"type": "string"},
                "example": ["name", "domain"],
            },
        },
        "required": ["library_id", "updates"],
    },
)
async def update_library(
    library_id: Annotated[int, Field(gt=0, description="Target library identifier.")],
    name: Annotated[
        str | None, Field(default=None, min_length=1, description="Library name to save. Omit to leave it unchanged.")
    ] = None,
    type: Annotated[
        str | None, Field(default=None, description="Library type to save. Omit to leave it unchanged.")
    ] = None,
    organization: Annotated[
        str | None, Field(default=None, description="Owning organization to save. Omit to leave it unchanged.")
    ] = None,
    link: Annotated[
        str | None, Field(default=None, description="Library URL to save. Omit to leave it unchanged.")
    ] = None,
    domain: Annotated[
        str | None, Field(default=None, description="Library domain to save. Omit to leave it unchanged.")
    ] = None,
    description: Annotated[
        str | None, Field(default=None, description="Library description to save. Omit to leave it unchanged.")
    ] = None,
    state: Annotated[
        str | None, Field(default=None, description="Library state to save. Omit to leave it unchanged.")
    ] = None,
    is_default: Annotated[
        bool | str | None,
        Field(default=None, description="Whether this library should be the default. Omit to leave it unchanged."),
    ] = None,
    library_service: LibraryService = Depends(get_library_service),
) -> UpdateLibraryResponse:
    """Update a library."""
    try:
        result = await library_service.update_library(
            library_id=library_id,
            type=UNSET if type is None else type,
            name=UNSET if name is None else name,
            organization=UNSET if organization is None else organization,
            description=UNSET if description is None else description,
            link=UNSET if link is None else link,
            domain=UNSET if domain is None else domain,
            state=UNSET if state is None else state,
            is_default=UNSET if is_default is None else str_to_bool(is_default),
        )
        return UpdateLibraryResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to update library {library_id}.") from exc


@mcp.tool(
    name="add_library_release_dependency",
    description="Add a direct dependency to a library's working release.",
    output_schema={
        "type": "object",
        "description": "Response containing the target library identifier and the direct dependencies now assigned.",
        "properties": {
            "library_id": {"type": "integer", "description": "Target library identifier.", "example": 12},
            "release_dependencies": {
                "type": "array",
                "description": "Direct dependencies now assigned to the library's working release.",
                "items": {
                    "type": "object",
                    "properties": {
                        "release_id": {"type": "integer", "description": "Release identifier.", "example": 101},
                        "library_id": {"type": "integer", "description": "Owning library identifier.", "example": 3},
                        "library_name": {
                            "type": "string",
                            "description": "Owning library name.",
                            "example": "CCTS Data Type Catalogue v3",
                        },
                        "release_num": {"type": "string", "description": "Release number.", "example": "3.1"},
                        "state": {"type": "string", "description": "Release lifecycle state.", "example": "Published"},
                    },
                    "required": ["release_id", "library_id", "library_name", "release_num", "state"],
                },
            },
        },
        "required": ["library_id", "release_dependencies"],
    },
)
async def add_library_release_dependency(
    library_id: Annotated[int, Field(gt=0, description="Target library identifier.")],
    release_id: Annotated[int, Field(gt=0, description="Release identifier to add as a dependency.")],
    library_service: LibraryService = Depends(get_library_service),
) -> ManageLibraryReleaseDependenciesResponse:
    """Add a direct dependency to a library's working release."""
    try:
        result = await library_service.add_library_release_dependency(
            library_id=library_id,
            release_id=release_id,
        )
        return ManageLibraryReleaseDependenciesResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(
            exc, fallback=f"Unable to add release dependency {release_id} to library {library_id}."
        ) from exc


@mcp.tool(
    name="remove_library_release_dependency",
    description="Remove a direct dependency from a library's working release.",
    output_schema={
        "type": "object",
        "description": "Response containing the target library identifier and the direct dependencies now assigned.",
        "properties": {
            "library_id": {"type": "integer", "description": "Target library identifier.", "example": 12},
            "release_dependencies": {
                "type": "array",
                "description": "Direct dependencies now assigned to the library's working release.",
                "items": {
                    "type": "object",
                    "properties": {
                        "release_id": {"type": "integer", "description": "Release identifier.", "example": 101},
                        "library_id": {"type": "integer", "description": "Owning library identifier.", "example": 3},
                        "library_name": {
                            "type": "string",
                            "description": "Owning library name.",
                            "example": "CCTS Data Type Catalogue v3",
                        },
                        "release_num": {"type": "string", "description": "Release number.", "example": "3.1"},
                        "state": {"type": "string", "description": "Release lifecycle state.", "example": "Published"},
                    },
                    "required": ["release_id", "library_id", "library_name", "release_num", "state"],
                },
            },
        },
        "required": ["library_id", "release_dependencies"],
    },
)
async def remove_library_release_dependency(
    library_id: Annotated[int, Field(gt=0, description="Target library identifier.")],
    release_id: Annotated[int, Field(gt=0, description="Release identifier to remove from dependencies.")],
    library_service: LibraryService = Depends(get_library_service),
) -> ManageLibraryReleaseDependenciesResponse:
    """Remove a direct dependency from a library's working release."""
    try:
        result = await library_service.remove_library_release_dependency(
            library_id=library_id,
            release_id=release_id,
        )
        return ManageLibraryReleaseDependenciesResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(
            exc, fallback=f"Unable to remove release dependency {release_id} from library {library_id}."
        ) from exc


@mcp.tool(
    name="discard_library",
    description="Discard a library permanently after explicit confirmation when it passes the discard checks.",
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def discard_library(
    library_id: Annotated[int, Field(gt=0, description="Target library identifier.")],
    ctx: Context,
    library_service: LibraryService = Depends(get_library_service),
) -> dict[str, object]:
    """Discard a library after confirmation."""
    row = await library_service.get(library_id)
    if row is None:
        raise ToolError(f"The library with ID {library_id} was not found. Please check the ID and try again.")

    elicit_result = await ctx.elicit(
        message=(
            f"Are you sure you want to discard library '{row.name}' permanently?\n\n"
            "This permanently deletes the library record, its current working release, "
            "its namespaces, and the current working-release authored data tied to that library. "
            "This cannot be undone."
        ),
        response_type=None,
    )
    match elicit_result:
        case AcceptedElicitation():
            pass
        case DeclinedElicitation():
            raise ToolError("Library discard was not confirmed.")
        case CancelledElicitation():
            raise ToolError("Library discard was cancelled.")

    try:
        await library_service.discard_library(library_id=library_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to discard library {library_id}.") from exc


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
