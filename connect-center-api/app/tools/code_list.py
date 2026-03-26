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

from __future__ import annotations

import logging
from typing import Annotated, Any

from fastmcp import FastMCP
from fastmcp.dependencies import Depends
from pydantic import Field
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.vendor_plugins import get_vendor_plugin
from app.routes.models.code_list import CodeListEntry
from app.routes.utils.date import parse_date_range
from app.security import AuthenticatedUser
from app.services.code_list_service import CodeListService
from app.services.release_service import ReleaseService
from app.tools import _to_tool_error, get_tool_authenticated_user, tool_session
from app.tools.models.code_list import GetCodeListPaginationResponse, GetCodeListResponse

logger = logging.getLogger("connectcenter.mcp.code_list")

mcp = FastMCP("connectCenter MCP - Code List Tools")


async def get_code_list_service(
    session: AsyncSession = Depends(tool_session),
) -> CodeListService:
    """Provide a requester-scoped code list service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    return _build_code_list_service(session, requester)


@mcp.tool(
    name="get_code_lists",
    description="Get a paginated list of code lists associated with a specific release.",
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
                    "required": ["code_list_manifest_id", "code_list_id", "guid", "name", "list_id", "version_id", "is_deprecated", "library", "release", "owner", "created", "last_updated"]
                }
            }
        },
        "required": ["total_items", "offset", "limit", "items"]
    }
)
async def get_code_lists(
    release_id: Annotated[int, Field(gt=0, description="Filter by release ID using exact match.")],
    name: Annotated[str | None, Field(default=None, description="Filter by code list name using partial match (case-insensitive).")],
    list_id: Annotated[str | None, Field(default=None, description="Filter by list ID using partial match (case-insensitive).")],
    version_id: Annotated[str | None, Field(default=None, description="Filter by version ID using partial match (case-insensitive).")],
    created_on: Annotated[str | None, Field(default=None, description="Filter by creation date using an inclusive range: '[before~after]'.")],
    last_updated_on: Annotated[str | None, Field(default=None, description="Filter by last update date using an inclusive range: '[before~after]'.")],
    order_by: Annotated[str | None, Field(default=None, description="Comma-separated list of properties to order results by. Allowed columns: name, list_id, version_id, definition, creation_timestamp, last_update_timestamp.")],
    offset: Annotated[int, Field(default=0, ge=0, description="The offset from the beginning of the list.")],
    limit: Annotated[int, Field(default=10, ge=1, le=100, description="The maximum number of items to return.")],
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> GetCodeListPaginationResponse:
    """
    Get a paginated list of code lists associated with a specific release.

    This function retrieves code lists that are associated with the given release_id. This tool enables
    users to query and manage code lists within specific releases, including filtering by name, list_id,
    version_id, and other attributes. It supports pagination, filtering, and sorting. The release_id
    filter is required to ensure you get code lists from the correct release context.

    Args:
        release_id (int): Filter by release ID using exact match (required).
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
        offset (int, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int, optional): The maximum number of items to return. Must be between 1 and 100 (inclusive). Defaults to 10.

    Returns:
        GetCodeListPaginationResponse: Response object containing:
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
    try:
        page = await code_list_service.list(
            release_id=release_id,
            limit=limit,
            offset=offset,
            order_by=order_by,
            name=name,
            list_id=list_id,
            version_id=version_id,
            created_on=parse_date_range(created_on),
            last_updated_on=parse_date_range(last_updated_on),
        )
        return _to_list_response(items=page.items, total=page.total, offset=page.offset, limit=page.limit)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to retrieve code lists.") from exc


@mcp.tool(
    name="get_code_list",
    description="Get a specific code list by its manifest ID.",
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
    code_list_manifest_id: Annotated[int, Field(gt=0, description="Unique numeric identifier of the code list manifest to retrieve.")],
    code_list_service: CodeListService = Depends(get_code_list_service),
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
    try:
        row = await code_list_service.get(code_list_manifest_id)
        if row is None:
            raise ValueError(
                f"The code list with manifest ID {code_list_manifest_id} was not found. Please check the ID and try again."
            )
        return GetCodeListResponse.model_validate(row, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to retrieve code list {code_list_manifest_id}.") from exc


def _build_code_list_service(
    session: AsyncSession,
    requester: AuthenticatedUser,
) -> CodeListService:
    """Construct the code list service for an MCP request."""
    plugin = get_vendor_plugin()
    release_service = ReleaseService(
        plugin.create_release_repository(session),
        plugin.create_app_user_repository(session),
        requester=requester,
    )
    return CodeListService(
        plugin.create_code_list_repository(session),
        release_service,
        plugin.create_app_user_repository(session),
        requester=requester,
    )


def _to_list_response(*, items: list[Any], total: int, offset: int, limit: int) -> GetCodeListPaginationResponse:
    """Build the paginated MCP response model."""
    return GetCodeListPaginationResponse(
        total_items=total,
        offset=offset,
        limit=limit,
        items=[CodeListEntry.model_validate(item, from_attributes=True) for item in items],
    )
