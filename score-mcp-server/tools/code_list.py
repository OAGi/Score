"""
MCP Tools for managing Code List operations in connectCenter.

This module provides Model Context Protocol (MCP) tools for querying and retrieving
Code Lists, which are standardized lists of values used to represent enumerated data
in business information exchanges. Code Lists serve as controlled vocabularies that
define the allowed values for specific data elements, ensuring consistency and
interoperability in business communications. They provide a standardized way to represent
categorical data where only a predefined set of values is acceptable.

Code Lists enable data validation and semantic consistency by restricting data values
to a well-defined set of options, each with a specific meaning. They are commonly used
for representing status codes, country codes, currency codes, measurement units, and
other standardized enumerations. Each code list contains multiple values, where each
value has a code (the actual value used in data exchange), a meaning (the semantic
interpretation), and optionally a definition. Code Lists can be extensible (allowing
additional values) or fixed (restricted to predefined values only). The tools provide
a standardized MCP interface, enabling clients to interact with Code List data programmatically.

Available Tools:
- get_code_lists: Retrieve paginated lists of Code Lists filtered by release with
  optional filters for name, list_id, version_id, and date ranges. Supports custom
  sorting and pagination.

- get_code_list: Retrieve a single Code List by its manifest ID, including all
  related information such as namespace, release, library, log, and associated
  value manifests.

Key Features:
- Full relationship loading (namespace, creator, owner, release, library, log)
- Support for filtering, pagination, and sorting
- Automatic retrieval of associated value manifests
- Comprehensive error handling and validation
- Structured response models with detailed metadata

The tools provide a clean, consistent interface for accessing Code List data through the MCP protocol.
"""

import logging
from typing import Annotated

from fastapi import HTTPException
from fastmcp import FastMCP
from fastmcp.exceptions import ToolError
from pydantic import Field

from services import CodeListService, DateRangeParams, PaginationParams
from tools import _validate_auth_and_db, parse_order_by_to_sorts, _create_user_info
from tools.models.code_list import (
    CodeListValueInfo,
    GetCodeListResponse,
    GetCodeListsResponse,
)
from tools.models.common import LibraryInfo, LogInfo, NamespaceInfo, ReleaseInfo, WhoAndWhen
from tools.utils import parse_date_range

# Configure logging
logger = logging.getLogger(__name__)

mcp = FastMCP("Score MCP Server - Code List Tools")


@mcp.tool(
    name="get_code_lists",
    description="Get a paginated list of code lists associated with a specific release",
    output_schema={
        "type": "object",
        "description": "Response containing paginated list of code lists",
        "properties": {
            "total_items": {"type": "integer", "description": "Total number of code lists available. Allowed values: non-negative integers (≥0).", "example": 25},
            "offset": {"type": "integer", "description": "Offset of the first item in this page. Allowed values: non-negative integers (≥0). Default value: 0.", "example": 0},
            "limit": {"type": "integer", "description": "Number of items returned in this page. Allowed values: integers between 1 and 100 (inclusive). Default value: 10.", "example": 10},
            "items": {
                "type": "array",
                "description": "List of code lists on this page",
                "items": {
                    "type": "object",
                    "properties": {
                        "code_list_manifest_id": {"type": "integer", "description": "Unique identifier for the code list manifest", "example": 12345},
                        "code_list_id": {"type": "integer", "description": "Unique identifier for the code list", "example": 6789},
                        "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                        "enum_type_guid": {"type": ["string", "null"], "description": "Enum type GUID for the code list", "example": "a1b2c3d4e5f6789012345678901234ab"},
                        "name": {"type": "string", "description": "Name of the code list", "example": "Currency Code"},
                        "list_id": {"type": "string", "description": "List identifier", "example": "ISO4217"},
                        "version_id": {"type": "string", "description": "Version identifier", "example": "1.0"},
                        "definition": {"type": ["string", "null"], "description": "Definition of the code list", "example": "Standard currency codes"},
                        "remark": {"type": ["string", "null"], "description": "Remarks about the code list", "example": "Based on ISO 4217"},
                        "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://www.iso.org/iso-4217-currency-codes.html"},
                        "extensible_indicator": {"type": "boolean", "description": "Whether the code list is extensible", "example": False},
                        "is_deprecated": {"type": "boolean", "description": "Whether the code list is deprecated", "example": False},
                        "state": {"type": ["string", "null"], "description": "Current state of the code list", "example": "Published"},
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
                        "library": {
                            "type": "object",
                            "description": "Library information",
                            "properties": {
                                "library_id": {"type": "integer", "description": "Unique identifier for the library", "example": 1},
                                "name": {"type": "string", "description": "Library name", "example": "connectSpec"}
                            },
                            "required": ["library_id", "name"]
                        },
                        "release": {
                            "type": "object",
                            "description": "Release information",
                            "properties": {
                                "release_id": {"type": "integer", "description": "Unique identifier for the release", "example": 1},
                                "release_num": {"type": "string", "description": "Release number", "example": "10.6"},
                                "state": {"type": "string", "description": "Release state", "example": "Published"}
                            },
                            "required": ["release_id", "release_num", "state"]
                        },
                        "log": {
                            "type": ["object", "null"],
                            "description": "Log information",
                            "properties": {
                                "log_id": {"type": "integer", "description": "Unique identifier for the log", "example": 1},
                                "revision_num": {"type": "integer", "description": "Revision number", "example": 1},
                                "revision_tracking_num": {"type": "integer", "description": "Revision tracking number", "example": 1}
                            },
                            "required": ["log_id", "revision_num", "revision_tracking_num"]
                        },
                        "values": {
                            "type": "array",
                            "description": "List of code list values",
                            "items": {
                                "type": "object",
                                "properties": {
                                    "code_list_value_manifest_id": {"type": "integer", "description": "Unique identifier for the code list value manifest", "example": 12345},
                                    "code_list_value_id": {"type": "integer", "description": "Unique identifier for the code list value", "example": 6789},
                                    "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                                    "value": {"type": "string", "description": "Value of the code list", "example": "USD"},
                                    "meaning": {"type": ["string", "null"], "description": "Meaning of the code list value", "example": "US Dollar"},
                                    "definition": {"type": ["string", "null"], "description": "Definition of the code list value", "example": "United States Dollar"},
                                    "is_deprecated": {"type": "boolean", "description": "Whether the code list value is deprecated", "example": False}
                                },
                                "required": ["code_list_value_manifest_id", "code_list_value_id", "guid", "value", "is_deprecated"]
                            }
                        },
                        "owner": {
                            "type": "object",
                            "description": "Owner information",
                            "properties": {
                                "user_id": {"type": "integer", "description": "Unique identifier for the user", "example": 1},
                                "login_id": {"type": "string", "description": "User's login identifier", "example": "admin"},
                                "username": {"type": "string", "description": "Display name of the user", "example": "Administrator"},
                                "roles": {"type": "array", "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]}, "description": "List of roles assigned to the user", "example": ["Admin"]}
                            },
                            "required": ["user_id", "login_id", "username", "roles"]
                        },
                        "created": {
                            "type": "object",
                            "description": "Information about the creation of the code list",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who created the code list",
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
                            "description": "Information about the last update of the code list",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who last updated the code list",
                                    "properties": {
                                        "user_id": {"type": "integer", "description": "Unique identifier for the user", "example": 1},
                                        "login_id": {"type": "string", "description": "User's login identifier", "example": "admin"},
                                        "username": {"type": "string", "description": "Display name of the user", "example": "Administrator"},
                                        "roles": {"type": "array", "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]}, "description": "List of roles assigned to the user", "example": ["Admin"]}
                                    },
                                    "required": ["user_id", "login_id", "username", "roles"]
                                },
                                "when": {"type": "string", "format": "date-time", "description": "Last update timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)", "example": "2024-01-15T10:30:00Z"}
                            },
                            "required": ["who", "when"]
                        }
                    },
                    "required": ["code_list_manifest_id", "code_list_id", "guid", "name", "list_id", "version_id", "library", "release", "owner", "created", "last_updated"]
                }
            }
        },
        "required": ["total_items", "offset", "limit", "items"]
    }
)
async def get_code_lists(
    release_id: Annotated[int, "Filter by release ID using exact match."],
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
    name: Annotated[str | None, "Filter by code list name using partial match (case-insensitive)."] = None,
    list_id: Annotated[str | None, "Filter by list ID using partial match (case-insensitive)."] = None,
    version_id: Annotated[str | None, "Filter by version ID using partial match (case-insensitive)."] = None,
    created_on: Annotated[
        str | None, "Filter by creation date using an inclusive range: '[before~after]'. 'before' and 'after' are date-time strings. Default date format: YYYY-MM-DD. Examples: '[2025-01-01~2025-02-01]'. Either 'before' or 'after' can be omitted, e.g., '[~2025-02-01]' or '[2025-01-01~]'."] = None,
    last_updated_on: Annotated[
        str | None, "Filter by last update date using an inclusive range: '[before~after]'. 'before' and 'after' are date-time strings. Default date format: YYYY-MM-DD. Examples: '[2025-01-01~2025-02-01]'. Either 'before' or 'after' can be omitted, e.g., '[~2025-02-01]' or '[2025-01-01~]'."] = None,
    order_by: Annotated[
        str | None, "Comma-separated list of properties to order results by. Prefix with '-' for descending, '+' for ascending (default ascending). Allowed columns: name, list_id, version_id, definition, creation_timestamp, last_update_timestamp. Example: '-creation_timestamp,+name' translates to 'creation_timestamp DESC, name ASC'."] = None
) -> GetCodeListsResponse:
    """
    Get a paginated list of code lists associated with a specific release.
    
    This function retrieves code lists that are associated with the given release_id. This tool enables
    users to query and manage code lists within specific releases, including filtering by name, list_id,
    version_id, and other attributes. It supports pagination, filtering, and sorting. The release_id 
    filter is required to ensure you get code lists from the correct release context.
    
    Args:
        release_id (int): Filter by release ID using exact match.
        offset (int | None, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int | None, optional): The maximum number of items to return. Must be a non-negative number. Defaults to 10.
        name (str | None, optional): Filter by code list name using partial match (case-insensitive). Defaults to None.
        list_id (str | None, optional): Filter by list ID using partial match (case-insensitive). Defaults to None.
        version_id (str | None, optional): Filter by version ID using partial match (case-insensitive). Defaults to None.
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
            Allowed columns: name, list_id, version_id, definition, creation_timestamp, last_update_timestamp.
            Example: '-creation_timestamp,+name' translates to 'creation_timestamp DESC, name ASC'.
            Defaults to None.
    
    Returns:
        GetCodeListsResponse: Response object containing:
            - total_items: Total number of code lists available
            - offset: Offset of the first item in this page
            - limit: Number of items returned in this page
            - items: List of code lists on this page with detailed information including:
                - code_list_manifest_id: Unique identifier for the code list manifest
                - code_list_id: Unique identifier for the code list
                - guid: Unique identifier within the release
                - enum_type_guid: Enum type GUID (if any)
                - name: Name of the code list
                - list_id: External identifier
                - version_id: Code list version number
                - definition: Description of the code list
                - remark: Usage information about the code list
                - definition_source: URL indicating the source of the definition
                - namespace: Namespace information (if any)
                - library: Library information with library_id and name
                - release: Release information with release_id, release_num, and state
                - log: Log information with log_id, revision_num, and revision_tracking_num (if any)
                - extensible_indicator: Whether the code list is extensible
                - is_deprecated: Whether the code list is deprecated
                - state: State of the code list
                - values: List of code list values with their details
                - owner: User information about the owner of the code list
                - created: Information about the creation of the code list
                - last_updated: Information about the last update of the code list
    
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
        >>> result = get_code_lists(release_id=123, offset=0, limit=10)
        >>> print(f"Found {result.total_items} code lists")
        
        Filtered search:
        >>> result = get_code_lists(release_id=123, name="currency", limit=5)
        >>> for code_list in result.items:
        ...     print(f"Code list: {code_list.name} (ID: {code_list.list_id})")
        
        Date range filtering:
        >>> result = get_code_lists(release_id=123, created_on="[2024-01-01~2024-12-31]")
        >>> print(f"Code lists created in 2024: {result.total_items}")
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Create service instance
    code_list_service = CodeListService()

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
                f"Valid columns: {", ".join(code_list_service.allowed_columns_for_order_by)}") from e

    # Get code lists
    try:
        page = code_list_service.get_code_lists_by_release(
            release_id=release_id,
            name=name,
            list_id=list_id,
            version_id=version_id,
            created_on_params=created_on_params,
            last_updated_on_params=last_updated_on_params,
            pagination=pagination,
            sort_list=sort_list
        )

        return GetCodeListsResponse(
            total_items=page.total,
            offset=page.offset,
            limit=page.limit,
            items=[_create_code_list_result(manifest, code_list_service) for manifest in page.items]
        )
    except HTTPException as e:
        logger.error(f"HTTP error retrieving code lists: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving code lists: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the code lists: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="get_code_list",
    description="Get a specific code list by its manifest ID",
    output_schema={
        "type": "object",
        "description": "Response containing code list information",
        "properties": {
            "code_list_manifest_id": {"type": "integer", "description": "Unique identifier for the code list manifest", "example": 12345},
            "code_list_id": {"type": "integer", "description": "Unique identifier for the code list", "example": 6789},
            "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
            "enum_type_guid": {"type": ["string", "null"], "description": "Enum type GUID for the code list", "example": "a1b2c3d4e5f6789012345678901234ab"},
            "name": {"type": "string", "description": "Name of the code list", "example": "Currency Code"},
            "list_id": {"type": "string", "description": "List identifier", "example": "ISO4217"},
            "version_id": {"type": "string", "description": "Version identifier", "example": "1.0"},
            "definition": {"type": ["string", "null"], "description": "Definition of the code list", "example": "Standard currency codes"},
            "remark": {"type": ["string", "null"], "description": "Remarks about the code list", "example": "Based on ISO 4217"},
            "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://www.iso.org/iso-4217-currency-codes.html"},
            "extensible_indicator": {"type": "boolean", "description": "Whether the code list is extensible", "example": False},
            "is_deprecated": {"type": "boolean", "description": "Whether the code list is deprecated", "example": False},
            "state": {"type": ["string", "null"], "description": "Current state of the code list", "example": "Published"},
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
            "library": {
                "type": "object",
                "description": "Library information",
                "properties": {
                    "library_id": {"type": "integer", "description": "Unique identifier for the library", "example": 1},
                    "name": {"type": "string", "description": "Library name", "example": "connectSpec"}
                },
                "required": ["library_id", "name"]
            },
            "release": {
                "type": "object",
                "description": "Release information",
                "properties": {
                    "release_id": {"type": "integer", "description": "Unique identifier for the release", "example": 1},
                    "release_num": {"type": "string", "description": "Release number", "example": "10.6"},
                    "state": {"type": "string", "description": "Release state", "example": "Published"}
                },
                "required": ["release_id", "release_num", "state"]
            },
            "log": {
                "type": ["object", "null"],
                "description": "Log information",
                "properties": {
                    "log_id": {"type": "integer", "description": "Unique identifier for the log", "example": 1},
                    "revision_num": {"type": "integer", "description": "Revision number", "example": 1},
                    "revision_tracking_num": {"type": "integer", "description": "Revision tracking number", "example": 1}
                },
                "required": ["log_id", "revision_num", "revision_tracking_num"]
            },
            "values": {
                "type": "array",
                "description": "List of code list values",
                "items": {
                    "type": "object",
                    "properties": {
                        "code_list_value_manifest_id": {"type": "integer", "description": "Unique identifier for the code list value manifest", "example": 12345},
                        "code_list_value_id": {"type": "integer", "description": "Unique identifier for the code list value", "example": 6789},
                        "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                        "value": {"type": "string", "description": "Value of the code list", "example": "USD"},
                        "meaning": {"type": ["string", "null"], "description": "Meaning of the code list value", "example": "US Dollar"},
                        "definition": {"type": ["string", "null"], "description": "Definition of the code list value", "example": "United States Dollar"},
                        "is_deprecated": {"type": "boolean", "description": "Whether the code list value is deprecated", "example": False}
                    },
                    "required": ["code_list_value_manifest_id", "code_list_value_id", "guid", "value", "is_deprecated"]
                }
            },
            "owner": {
                "type": "object",
                "description": "Owner information",
                "properties": {
                    "user_id": {"type": "integer", "description": "Unique identifier for the user", "example": 1},
                    "login_id": {"type": "string", "description": "User's login identifier", "example": "admin"},
                    "username": {"type": "string", "description": "Display name of the user", "example": "Administrator"},
                    "roles": {"type": "array", "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]}, "description": "List of roles assigned to the user", "example": ["Admin"]}
                },
                "required": ["user_id", "login_id", "username", "roles"]
            },
            "created": {
                "type": "object",
                "description": "Information about the creation of the code list",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who created the code list",
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
                "description": "Information about the last update of the code list",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who last updated the code list",
                        "properties": {
                            "user_id": {"type": "integer", "description": "Unique identifier for the user", "example": 1},
                            "login_id": {"type": "string", "description": "User's login identifier", "example": "admin"},
                            "username": {"type": "string", "description": "Display name of the user", "example": "Administrator"},
                            "roles": {"type": "array", "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]}, "description": "List of roles assigned to the user", "example": ["Admin"]}
                        },
                        "required": ["user_id", "login_id", "username", "roles"]
                    },
                    "when": {"type": "string", "format": "date-time", "description": "Last update timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)", "example": "2024-01-15T10:30:00Z"}
                },
                "required": ["who", "when"]
            }
        },
        "required": ["code_list_manifest_id", "code_list_id", "guid", "name", "list_id", "version_id", "library", "release", "owner", "created", "last_updated"]
    }
)
async def get_code_list(
    code_list_manifest_id: Annotated[int, Field(
        description="Unique numeric identifier of the code list manifest to retrieve.",
        examples=[123, 456, 789],
        gt=0,
        title="Code List Manifest ID"
    )]
) -> GetCodeListResponse:
    """
    Get a specific code list by its manifest ID.
    
    This function retrieves a single code list from the database and returns
    detailed information including metadata about who created it and when it was
    last updated, along with code list-specific attributes, namespace information,
    and associated code list values.
    
    Args:
        code_list_manifest_id (int): The unique identifier of the code list manifest to fetch
    
    Returns:
        GetCodeListResponse: Response object containing:
            - code_list_manifest_id: Unique identifier for the code list manifest
            - code_list_id: Unique identifier for the code list
            - guid: Unique identifier within the release
            - enum_type_guid: Enum type GUID (if any)
            - name: Name of the code list
            - list_id: External identifier
            - version_id: Code list version number
            - definition: Description of the code list
            - remark: Usage information about the code list
            - definition_source: URL indicating the source of the definition
            - namespace: Namespace information (if any)
            - library: Library information with library_id and name
            - release: Release information with release_id, release_num, and state
            - log: Log information with log_id, revision_num, and revision_tracking_num (if any)
            - extensible_indicator: Whether the code list is extensible
            - is_deprecated: Whether the code list is deprecated
            - state: State of the code list
            - values: List of code list values with their details
            - owner: User information about the owner of the code list
            - created: Information about the creation of the code list
            - last_updated: Information about the last update of the code list
    
    Raises:
        ToolError: If validation fails, the code list manifest is not found, or other errors occur.
            Common error scenarios include:
            - Code list manifest with the specified ID does not exist
            - Database connection issues
            - Authentication failures
    
    Examples:
        Get a specific code list:
        >>> result = get_code_list(code_list_manifest_id=123)
        >>> print(f"Code list: {result.name} (GUID: {result.guid})")
        >>> print(f"Owner: {result.owner.username}")
        >>> print(f"Values: {len(result.values)}")
        
        Access code list values:
        >>> for value in result.values:
        ...     print(f"  {value.value}: {value.meaning}")
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Get code list
    try:
        service = CodeListService()
        manifest = service.get_code_list_by_manifest_id(code_list_manifest_id)

        return _create_code_list_result(manifest, service)
    except HTTPException as e:
        logger.error(f"HTTP error retrieving code list: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The code list manifest with ID {code_list_manifest_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving code list: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the code list: {str(e)}. Please contact your system administrator.") from e


# Helper functions (placed after their usage)

def _create_code_list_result(manifest, code_list_service) -> GetCodeListResponse:
    """
    Create a code list result from a CodeListManifest model instance.
    
    Args:
        manifest: CodeListManifest model instance with code_list relationship
        code_list_service: CodeListService instance for retrieving related data
        
    Returns:
        GetCodeListResponse: Formatted code list result
    """
    code_list = manifest.code_list
    
    # Get value manifests using the separate service function
    try:
        value_manifests = code_list_service.get_code_list_value_manifests_by_manifest_id(manifest.code_list_manifest_id)
    except Exception as e:
        logger.warning(f"Failed to retrieve value manifests for CodeListManifest {manifest.code_list_manifest_id}: {e}")
        value_manifests = []  # Continue without value manifests rather than failing completely
    
    # Create namespace info if available
    namespace_info = None
    if code_list.namespace:
        namespace_info = NamespaceInfo(
            namespace_id=code_list.namespace.namespace_id,
            prefix=code_list.namespace.prefix,
            uri=code_list.namespace.uri
        )

    # Create library info from release
    library_info = LibraryInfo(
        library_id=manifest.release.library_id,
        name=manifest.release.library.name
    )

    # Create release info from manifest
    # Since release_id is required and release relationship is loaded, release should always be available
    release_info = ReleaseInfo(
        release_id=manifest.release_id,
        release_num=manifest.release.release_num,
        state=manifest.release.state
    )

    # Create log info from manifest
    log_info = None
    if manifest.log:
        log_info = LogInfo(
            log_id=manifest.log.log_id,
            revision_num=manifest.log.revision_num,
            revision_tracking_num=manifest.log.revision_tracking_num
        )

    # Create code list values info from value manifests
    code_list_values_info = []
    for value_manifest in value_manifests:
        code_list_values_info.append(CodeListValueInfo(
            code_list_value_manifest_id=value_manifest.code_list_value_manifest_id,
            code_list_value_id=value_manifest.code_list_value_id,
            guid=value_manifest.code_list_value.guid,
            value=value_manifest.code_list_value.value,
            meaning=value_manifest.code_list_value.meaning,
            definition=value_manifest.code_list_value.definition,
            is_deprecated=value_manifest.code_list_value.is_deprecated
        ))

    return GetCodeListResponse(
        code_list_manifest_id=manifest.code_list_manifest_id,
        code_list_id=code_list.code_list_id,
        guid=code_list.guid,
        enum_type_guid=code_list.enum_type_guid,
        name=code_list.name,
        list_id=code_list.list_id,
        version_id=code_list.version_id,
        definition=code_list.definition,
        remark=code_list.remark,
        definition_source=code_list.definition_source,
        namespace=namespace_info,
        library=library_info,
        release=release_info,
        log=log_info,
        extensible_indicator=code_list.extensible_indicator,
        is_deprecated=code_list.is_deprecated,
        state=code_list.state,
        owner=_create_user_info(code_list.owner),
        values=code_list_values_info,
        created=WhoAndWhen(
            who=_create_user_info(code_list.creator),
            when=code_list.creation_timestamp
        ),
        last_updated=WhoAndWhen(
            who=_create_user_info(code_list.last_updater),
            when=code_list.last_update_timestamp
        )
    )
