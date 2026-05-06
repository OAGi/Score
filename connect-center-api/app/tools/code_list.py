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

- create_code_list_value: Create a new code list value under a WIP Code List.

- update_code_list_value: Update one existing code list value under a WIP Code List.

- delete_code_list_value: Delete one existing code list value under a WIP Code List.

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

from fastmcp import Context, FastMCP
from fastmcp.dependencies import Depends
from fastmcp.exceptions import ToolError
from fastmcp.server.elicitation import (
    AcceptedElicitation,
    CancelledElicitation,
    DeclinedElicitation,
)
from pydantic import Field
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.vendor_plugins import get_vendor_plugin
from app.utils.date import parse_date_range
from app.security import AuthenticatedUser
from app.services.app_user_service import AppUserService
from app.services.code_list_service import CodeListService
from app.services.release_service import ReleaseService
from app.tools import _to_tool_error, get_tool_authenticated_user, tool_session
from app.tools.models.code_list import (
    CodeListLifecycleState,
    CodeListResponseEntry,
    CreateCodeListResponse,
    CreateCodeListValueResponse,
    GetCodeListPaginationResponse,
    GetCodeListResponse,
    TransferCodeListOwnershipResponse,
    UpdateCodeListResponse,
    UpdateCodeListValueResponse,
)
from app.types.unset import UNSET

logger = logging.getLogger("connectcenter.mcp.code_list")

mcp = FastMCP("connectCenter MCP - Code List Tools")

EMPTY_OUTPUT_SCHEMA = {
    "type": "object",
    "description": "Empty response body.",
    "properties": {},
    "additionalProperties": False,
}


async def get_code_list_service(
    session: AsyncSession = Depends(tool_session),
) -> CodeListService:
    """Provide a requester-scoped code list service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    return _build_code_list_service(session, requester)


async def get_app_user_service(
    session: AsyncSession = Depends(tool_session),
) -> AppUserService:
    """Provide a requester-scoped app-user service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    vendor_plugin = get_vendor_plugin()
    app_user_repository = vendor_plugin.create_app_user_repository(session)
    return AppUserService(app_user_repository=app_user_repository, requester=requester)


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
    owner: Annotated[str | None, Field(default=None, description="Comma-separated owner login IDs to filter by exact match. Prefix a login ID with '!' to exclude it.")],
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
        owner (str | None, optional): Comma-separated owner login IDs using exact match.
            Prefix a login ID with '!' to exclude it. Login IDs cannot contain '!' or ','. Defaults to None.
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
            owner=owner,
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


@mcp.tool(
    name="create_code_list",
    description="Create a new code list in a role-appropriate release branch.",
    output_schema={
        "type": "object",
        "description": "Response containing the created code list manifest identifier.",
        "properties": {
            "code_list_manifest_id": {
                "type": "integer",
                "description": "Created code list manifest identifier.",
                "example": 12345,
            }
        },
        "required": ["code_list_manifest_id"],
    },
)
async def create_code_list(
    release_id: Annotated[
        int,
        Field(
            gt=0,
            description=(
                "Target release identifier. Developers must use the `Working` release, "
                "and end-users must use a non-`Working` release."
            ),
        ),
    ],
    name: Annotated[str, Field(min_length=1, description="Name to save for this code list.")],
    based_code_list_manifest_id: Annotated[
        int | None,
        Field(
            default=None,
            gt=0,
            description="Optional base code list manifest identifier used to derive the new code list.",
        ),
    ],
    version_id: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Version identifier to save for this code list. If omitted, the base code list's "
                "version identifier is used when creating from a base code list; otherwise `1` is used."
            ),
        ),
    ],
    list_id: Annotated[
        str | None,
        Field(
            default=None,
            description="External list identifier to save for this code list. If omitted, a generated list identifier is used.",
        ),
    ],
    agency_id_list_value_manifest_id: Annotated[
        int | None,
        Field(default=None, gt=0, description="Agency ID list value to use for this code list."),
    ],
    definition: Annotated[
        str | None,
        Field(
            default=None,
            description="Definition text to save for this code list. This is the explanatory text that describes what the code list means.",
        ),
    ],
    definition_source: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Definition source to save for this code list. Use this to record where the definition came from, "
                "such as a specification, standard, or reference URL."
            ),
        ),
    ],
    remark: Annotated[
        str | None,
        Field(default=None, description="Remark to save for this code list."),
    ],
    namespace_id: Annotated[
        int | None,
        Field(default=None, gt=0, description="Namespace identifier to use for this code list."),
    ],
    deprecated: Annotated[
        bool | None,
        Field(default=None, description="Whether this code list should start as deprecated."),
    ],
    extensible_indicator: Annotated[
        bool | None,
        Field(default=None, description="Whether this code list should be extensible."),
    ],
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> CreateCodeListResponse:
    """Create a code list, optionally derived from a base code list."""
    try:
        result = await code_list_service.create_code_list(
            release_id=release_id,
            based_code_list_manifest_id=based_code_list_manifest_id,
            name=name,
            version_id=version_id,
            list_id=list_id,
            agency_id_list_value_manifest_id=agency_id_list_value_manifest_id,
            definition=definition,
            definition_source=definition_source,
            remark=remark,
            namespace_id=namespace_id,
            deprecated=deprecated,
            extensible_indicator=extensible_indicator,
        )
        return CreateCodeListResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to create the code list.") from exc


@mcp.tool(
    name="update_code_list",
    description="Update mutable code list fields while the code list is in WIP.",
    output_schema={
        "type": "object",
        "description": "Response containing the updated code list manifest identifier and changed fields.",
        "properties": {
            "code_list_manifest_id": {
                "type": "integer",
                "description": "Target code list manifest identifier.",
                "example": 12345,
            },
            "updates": {
                "type": "array",
                "description": "Updated field names.",
                "items": {"type": "string"},
                "example": ["definition", "remark"],
            },
        },
        "required": ["code_list_manifest_id", "updates"],
    },
)
async def update_code_list(
    code_list_manifest_id: Annotated[int, Field(gt=0, description="Target code list manifest identifier.")],
    name: Annotated[str | None, Field(default=None, description="Updated name. Omit to leave unchanged.")],
    version_id: Annotated[str | None, Field(default=None, description="Updated version identifier. Omit to leave unchanged.")],
    list_id: Annotated[str | None, Field(default=None, description="Updated external list identifier. Omit to leave unchanged.")],
    agency_id_list_value_manifest_id: Annotated[
        int | None,
        Field(
            default=None,
            ge=0,
            description=(
                "Updated agency ID list value manifest identifier. Omit to leave unchanged. "
                "Use `0` to clear it."
            ),
        ),
    ],
    definition: Annotated[
        str | None,
        Field(
            default=None,
            description="Definition text. This is the explanatory text that describes what the code list means. Omit to leave unchanged.",
        ),
    ],
    definition_source: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Definition source. Use this to record where the definition came from, such as a "
                "specification, standard, or reference URL. Omit to leave unchanged."
            ),
        ),
    ],
    remark: Annotated[str | None, Field(default=None, description="Updated remark. Omit to leave unchanged.")],
    namespace_id: Annotated[
        int | None,
        Field(
            default=None,
            ge=0,
            description="Updated namespace identifier. Omit to leave unchanged. Use `0` to clear the namespace.",
        ),
    ],
    deprecated: Annotated[bool | None, Field(default=None, description="Updated deprecation flag. Omit to leave unchanged.")],
    extensible_indicator: Annotated[
        bool | None,
        Field(default=None, description="Updated extensibility flag. Omit to leave unchanged."),
    ],
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> UpdateCodeListResponse:
    """Update mutable code list fields."""
    try:
        result = await code_list_service.update_code_list(
            code_list_manifest_id=code_list_manifest_id,
            name=UNSET if name is None else name,
            version_id=UNSET if version_id is None else version_id,
            list_id=UNSET if list_id is None else list_id,
            agency_id_list_value_manifest_id=(
                UNSET
                if agency_id_list_value_manifest_id is None
                else (None if agency_id_list_value_manifest_id == 0 else agency_id_list_value_manifest_id)
            ),
            definition=UNSET if definition is None else definition,
            definition_source=UNSET if definition_source is None else definition_source,
            remark=UNSET if remark is None else remark,
            namespace_id=UNSET if namespace_id is None else (None if namespace_id == 0 else namespace_id),
            deprecated=UNSET if deprecated is None else deprecated,
            extensible_indicator=UNSET if extensible_indicator is None else extensible_indicator,
        )
        return UpdateCodeListResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to update code list {code_list_manifest_id}.") from exc


@mcp.tool(
    name="create_code_list_value",
    description="Create a new code list value while the owner code list is in WIP.",
    output_schema={
        "type": "object",
        "description": "Response containing the created code list value manifest identifier.",
        "properties": {
            "code_list_value_manifest_id": {
                "type": "integer",
                "description": "Created code list value manifest identifier.",
                "example": 12345,
            }
        },
        "required": ["code_list_value_manifest_id"],
    },
)
async def create_code_list_value(
    code_list_manifest_id: Annotated[int, Field(gt=0, description="Owner code list manifest identifier.")],
    value: Annotated[str, Field(min_length=1, description="Value for the new code list entry.")],
    meaning: Annotated[str | None, Field(default=None, description="Meaning of the new code list value.")],
    definition: Annotated[str | None, Field(default=None, description="Definition of the new code list value.")],
    definition_source: Annotated[
        str | None,
        Field(default=None, description="Definition source URL for the new code list value."),
    ],
    deprecated: Annotated[bool, Field(default=False, description="Whether the new code list value is deprecated.")],
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> CreateCodeListValueResponse:
    """Create one code list value under the target code list."""
    try:
        result = await code_list_service.create_code_list_value(
            code_list_manifest_id=code_list_manifest_id,
            value=value,
            meaning=meaning,
            definition=definition,
            definition_source=definition_source,
            deprecated=deprecated,
        )
        return CreateCodeListValueResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(
            exc,
            fallback=f"Unable to create a code list value for code list {code_list_manifest_id}.",
        ) from exc


@mcp.tool(
    name="update_code_list_value",
    description="Update selected fields of an existing code list value while the owner code list is in WIP.",
    output_schema={
        "type": "object",
        "description": "Response containing the updated code list value manifest identifier and changed fields.",
        "properties": {
            "code_list_value_manifest_id": {
                "type": "integer",
                "description": "Target code list value manifest identifier.",
                "example": 12345,
            },
            "updates": {
                "type": "array",
                "description": "Updated field names.",
                "items": {"type": "string"},
                "example": ["meaning", "definition"],
            },
        },
        "required": ["code_list_value_manifest_id", "updates"],
    },
)
async def update_code_list_value(
    code_list_manifest_id: Annotated[int, Field(gt=0, description="Owner code list manifest identifier.")],
    code_list_value_manifest_id: Annotated[int, Field(gt=0, description="Target code list value manifest identifier.")],
    value: Annotated[str | None, Field(default=None, description="Updated value. Omit to leave unchanged.")],
    meaning: Annotated[str | None, Field(default=None, description="Updated meaning. Omit to leave unchanged.")],
    definition: Annotated[str | None, Field(default=None, description="Updated definition. Omit to leave unchanged.")],
    definition_source: Annotated[
        str | None,
        Field(default=None, description="Updated definition source URL. Omit to leave unchanged."),
    ],
    deprecated: Annotated[bool | None, Field(default=None, description="Updated deprecation flag. Omit to leave unchanged.")],
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> UpdateCodeListValueResponse:
    """Update one code list value under the target code list."""
    try:
        result = await code_list_service.update_code_list_value(
            code_list_manifest_id=code_list_manifest_id,
            code_list_value_manifest_id=code_list_value_manifest_id,
            value=UNSET if value is None else value,
            meaning=UNSET if meaning is None else meaning,
            definition=UNSET if definition is None else definition,
            definition_source=UNSET if definition_source is None else definition_source,
            deprecated=UNSET if deprecated is None else deprecated,
        )
        return UpdateCodeListValueResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(
            exc,
            fallback=f"Unable to update code list value {code_list_value_manifest_id}.",
        ) from exc


@mcp.tool(
    name="delete_code_list_value",
    description="Delete an existing code list value while the owner code list is in WIP.",
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def delete_code_list_value(
    code_list_manifest_id: Annotated[int, Field(gt=0, description="Owner code list manifest identifier.")],
    code_list_value_manifest_id: Annotated[int, Field(gt=0, description="Target code list value manifest identifier.")],
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> dict[str, object]:
    """Delete one code list value under the target code list."""
    try:
        await code_list_service.delete_code_list_value(
            code_list_manifest_id=code_list_manifest_id,
            code_list_value_manifest_id=code_list_value_manifest_id,
        )
        return {}
    except Exception as exc:
        raise _to_tool_error(
            exc,
            fallback=f"Unable to delete code list value {code_list_value_manifest_id}.",
        ) from exc


@mcp.tool(
    name="transfer_code_list_ownership",
    description="Transfer ownership of a code list to another user.",
    output_schema={
        "type": "object",
        "description": "Response containing the code list manifest identifier and changed fields.",
        "properties": {
            "code_list_manifest_id": {
                "type": "integer",
                "description": "Target code list manifest identifier.",
                "example": 12345,
            },
            "updates": {
                "type": "array",
                "description": "Updated field names.",
                "items": {"type": "string"},
                "example": ["owner_user_id"],
            },
        },
        "required": ["code_list_manifest_id", "updates"],
    },
)
async def transfer_code_list_ownership(
    code_list_manifest_id: Annotated[int, Field(gt=0, description="Target code list manifest identifier.")],
    new_owner_user_id: Annotated[int, Field(gt=0, description="User ID of the new owner.")],
    ctx: Context,
    code_list_service: CodeListService = Depends(get_code_list_service),
    app_user_service: AppUserService = Depends(get_app_user_service),
) -> TransferCodeListOwnershipResponse:
    """Transfer code list ownership to another user after confirmation."""
    try:
        row = await code_list_service.get(code_list_manifest_id)
        if row is None:
            raise ToolError(
                f"The code list with manifest ID {code_list_manifest_id} was not found. Please check the ID and try again."
            )
        target_user = await app_user_service.get(new_owner_user_id)
        if target_user is None:
            raise ToolError(
                f"The target user with ID {new_owner_user_id} was not found. Please check the ID and try again."
            )

        target_user_label = target_user.login_id
        if target_user.username and target_user.username != target_user.login_id:
            target_user_label = f"{target_user.login_id} ({target_user.username})"
        code_list_label = row.name or row.list_id or f"Code list {code_list_manifest_id}"

        elicit_result = await ctx.elicit(
            message=(
                f"Are you sure you want to transfer ownership of '{code_list_label}' "
                f"to {target_user_label}?"
            ),
            response_type=None,
        )
        match elicit_result:
            case AcceptedElicitation():
                payload = await code_list_service.transfer_code_list_ownership(
                    code_list_manifest_id=code_list_manifest_id,
                    target_user_id=new_owner_user_id,
                )
                updates = [] if int(new_owner_user_id) == int(row.owner.user_id) else ["owner_user_id"]
                return TransferCodeListOwnershipResponse(
                    code_list_manifest_id=payload.code_list_manifest_id,
                    updates=updates,
                )
            case DeclinedElicitation():
                raise ToolError("Code list ownership transfer declined by user.")
            case CancelledElicitation():
                raise ToolError("Code list ownership transfer cancelled by user.")
    except Exception as exc:
        raise _to_tool_error(
            exc,
            fallback=f"Unable to transfer ownership of code list {code_list_manifest_id}.",
        ) from exc


@mcp.tool(
    name="change_code_list_state",
    description="Change the lifecycle state of a code list.",
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def change_code_list_state(
    code_list_manifest_id: Annotated[int, Field(gt=0, description="Target code list manifest identifier.")],
    state: Annotated[CodeListLifecycleState, Field(description="Target lifecycle state.")],
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> dict[str, object]:
    """
    Change a code list lifecycle state according to connectCenter rules.

    Valid transitions depend on the release branch:
    - `Working` release code lists: `Deleted -> WIP`, `WIP -> Deleted|Draft`, `Draft -> WIP|Candidate`, `Candidate -> WIP`
    - non-`Working` release code lists: `Deleted -> WIP`, `WIP -> Deleted|QA`, `QA -> WIP|Production`, `Production` is terminal
    """
    try:
        await code_list_service.change_code_list_state(
            code_list_manifest_id=code_list_manifest_id,
            state=state,
        )
        return {}
    except Exception as exc:
        raise _to_tool_error(
            exc,
            fallback=f"Unable to change the code list state for {code_list_manifest_id}.",
        ) from exc


@mcp.tool(
    name="revise_or_amend_code_list",
    description=(
        "Create a new editable code list revision from a stable code list revision. "
        "For end-user code lists, this is called an amendment."
    ),
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def revise_or_amend_code_list(
    code_list_manifest_id: Annotated[int, Field(gt=0, description="Target code list manifest identifier.")],
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> dict[str, object]:
    """Revise or amend a code list according to connectCenter rules."""
    try:
        await code_list_service.revise_code_list(code_list_manifest_id=code_list_manifest_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to revise or amend code list {code_list_manifest_id}.") from exc


@mcp.tool(
    name="cancel_code_list",
    description="Cancel the current code list revision and restore the previous stable revision.",
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def cancel_code_list(
    code_list_manifest_id: Annotated[int, Field(gt=0, description="Target code list manifest identifier.")],
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> dict[str, object]:
    """Cancel the current code list revision according to connectCenter rules."""
    try:
        await code_list_service.cancel_code_list(code_list_manifest_id=code_list_manifest_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to cancel code list {code_list_manifest_id}.") from exc


@mcp.tool(
    name="discard_code_list",
    description="Discard a Deleted code list and its direct records permanently.",
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def discard_code_list(
    code_list_manifest_id: Annotated[int, Field(gt=0, description="Target code list manifest identifier.")],
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> dict[str, object]:
    """Discard a Deleted code list from the database."""
    try:
        await code_list_service.discard_code_list(code_list_manifest_id=code_list_manifest_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to discard code list {code_list_manifest_id}.") from exc


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
        items=[CodeListResponseEntry.model_validate(item, from_attributes=True) for item in items],
    )
