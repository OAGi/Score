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
- get_library: Retrieve a single library by its ID, including creator and last_updater
  information.

- get_libraries: Retrieve paginated lists of libraries with optional filters for name,
  type, organization, domain, state, description, and is_default flag. Supports date
  range filtering and custom sorting.

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

import logging
from typing import Annotated

from fastapi import HTTPException
from fastmcp import FastMCP
from fastmcp.exceptions import ToolError
from pydantic import Field

from services import LibraryService, DateRangeParams, PaginationParams
from tools import _validate_auth_and_db, parse_order_by_to_sorts, _create_user_info
from tools.models.library import GetLibrariesResponse, GetLibraryResponse
from tools.models.common import WhoAndWhen
from tools.utils import parse_date_range, str_to_bool

# Configure logging
logger = logging.getLogger(__name__)

mcp = FastMCP("Score MCP Server - Library Tools")


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
        library_id: Annotated[int, Field(
            description="Unique numeric identifier of the library to retrieve.",
            examples=[123, 456, 789],
            gt=0,
            title="Library ID"
        )]
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Get library
    try:
        service = LibraryService()
        library = service.get_library(library_id)

        return _create_library_result(library)
    except HTTPException as e:
        logger.error(f"HTTP error retrieving library: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The library with ID {library_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving library: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the library: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="get_libraries",
    description="Get a paginated list of libraries. Boolean parameters accept both their native types and string representations (strings are automatically converted).",
    output_schema={
        "type": "object",
        "description": "Response containing paginated list of libraries. Boolean parameters accept both their native types and string representations (strings are automatically converted to the appropriate type).",
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
        name: Annotated[str | None, Field(
            description="Filter by library name using partial match (case-insensitive).",
            examples=["OAGIS", "UBL", "ISO"],
            title="Library Name"
        )] = None,
        type: Annotated[str | None, Field(
            description="Filter by library type using partial match (case-insensitive).",
            examples=["Standard", "Custom", "Extension"],
            title="Library Type"
        )] = None,
        organization: Annotated[str | None, Field(
            description="Filter by organization using partial match (case-insensitive).",
            examples=["OAGI", "OASIS", "ISO"],
            title="Organization"
        )] = None,
        domain: Annotated[str | None, Field(
            description="Filter by domain using partial match (case-insensitive).",
            examples=["Finance", "Healthcare", "Manufacturing"],
            title="Domain"
        )] = None,
        state: Annotated[str | None, Field(
            description="Filter by state using partial match (case-insensitive).",
            examples=["Published", "Draft", "Deprecated"],
            title="State"
        )] = None,
        description: Annotated[str | None, Field(
            description="Filter by description using partial match (case-insensitive).",
            examples=["Business", "Technical", "Standard"],
            title="Description"
        )] = None,
        is_default: Annotated[bool | str | None, Field(
            description="Filter by default library flag using exact match. Accepts bool, str, or None. String values are converted: 'True'/'true'/'1' -> True, 'False'/'false'/'0' -> False.",
            examples=[True, False, "1", "0", "true", "false", "True", "False"],
            title="Is Default"
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
            description="Comma-separated list of properties to order results by. Prefix with '-' for descending, '+' for ascending (default ascending). Allowed columns: name, type, organization, domain, state, creation_timestamp, last_update_timestamp. Example: '-creation_timestamp,+name' translates to 'creation_timestamp DESC, name ASC'.",
            examples=["-creation_timestamp,+name", "name", "-last_update_timestamp"],
            title="Order By"
        )] = None
) -> GetLibrariesResponse:
    """
    Get a paginated list of libraries.
    
    This function retrieves libraries with support for pagination, filtering,
    and sorting. It returns detailed information about each library including creation
    and update metadata, and library-specific attributes.
    
    Args:
        offset (int | None, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int | None, optional): The maximum number of items to return. Must be a non-negative number. Defaults to 10.
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
    
    Returns:
        GetLibrariesResponse: Response object containing:
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
        >>> result = get_libraries(offset=0, limit=10)
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
        ...     sort_list="-creation_timestamp,+name"
        ... )
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()
    
    # Convert string parameters to their proper types
    try:
        is_default = str_to_bool(is_default)
    except ToolError:
        raise  # Re-raise ToolError as-is
    except Exception as e:
        raise ToolError(
            f"Type conversion error: {str(e)}. Please check your parameter types and try again."
        ) from e

    # Create service instance
    library_service = LibraryService()

    # Validate and create pagination parameters
    try:
        pagination = PaginationParams(offset=offset, limit=limit)
    except ValueError as e:
        raise ToolError(
            f"Pagination validation failed: {str(e)}. Please provide valid offset (≥0) and limit (1-100) values.") from e

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

    # Validate order_by parameter and create Sort objects
    sort_list = None
    if order_by:
        try:
            sort_list = parse_order_by_to_sorts(order_by)
        except ValueError as e:
            raise ToolError(
                f"Invalid order_by format: {str(e)}. Please use format: '(-|+)?<column_name>(,(-|+)?<column_name>)*'. "
                f"Valid columns: {", ".join(library_service.allowed_columns_for_order_by)}") from e

    # Get libraries
    try:
        page = library_service.get_libraries(
            name=name,
            type=type,
            organization=organization,
            domain=domain,
            state=state,
            description=description,
            is_default=is_default,
            created_on_params=created_on_params,
            last_updated_on_params=last_updated_on_params,
            pagination=pagination,
            sort_list=sort_list
        )

        return GetLibrariesResponse(
            total_items=page.total,
            offset=page.offset,
            limit=page.limit,
            items=[_create_library_result(library) for library in page.items]
        )
    except HTTPException as e:
        logger.error(f"HTTP error retrieving libraries: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving libraries: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the libraries: {str(e)}. Please contact your system administrator.") from e


# Helper functions (placed after their usage)

def _create_library_result(library) -> GetLibraryResponse:
    """
    Create a library result from a Library model instance.
    
    Args:
        library: Library model instance
        
    Returns:
        GetLibraryResponse: Formatted library result
    """
    return GetLibraryResponse(
        library_id=library.library_id,
        name=library.name,
        type=library.type,
        organization=library.organization,
        description=library.description,
        link=library.link,
        domain=library.domain,
        state=library.state,
        is_read_only=library.is_read_only,
        is_default=library.is_default,
        created=WhoAndWhen(who=_create_user_info(library.creator), when=library.creation_timestamp),
        last_updated=WhoAndWhen(who=_create_user_info(library.last_updater), when=library.last_update_timestamp)
    )
