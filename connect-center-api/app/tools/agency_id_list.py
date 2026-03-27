"""
MCP Tools for managing Agency ID List operations in connectCenter.

This module provides Model Context Protocol (MCP) tools for querying and retrieving
Agency ID Lists, which are standardized lists used to identify agencies or organizations
in business information exchanges. Agency ID Lists serve as controlled vocabularies that
define the authoritative sources or maintainers of various standards, code lists, and
other business information artifacts. They provide a standardized way to identify which
organization is responsible for creating, maintaining, or governing specific components
or standards.

Agency ID Lists enable provenance tracking and standards governance by clearly identifying
the authoritative source for each artifact. They are essential for understanding the
ownership and maintenance responsibility of standards, code lists, and other components.
Common examples include identifiers for standards organizations such as ISO (International
Organization for Standardization), UN/CEFACT (United Nations Centre for Trade Facilitation
and Electronic Business), and OAGi (Open Applications Group). Each agency ID list contains
multiple agency identifiers, where each identifier represents a specific organization or
standards body. The tools provide a standardized MCP interface, enabling clients to interact
with Agency ID List data programmatically.

Available Tools:
- get_agency_id_lists: Retrieve paginated lists of Agency ID Lists filtered by release
  with optional filters for name, list_id, version_id, and date ranges. Supports custom
  sorting and pagination.

- get_agency_id_list: Retrieve a single Agency ID List by its manifest ID, including
  all related information such as namespace, release, library, log, and associated
  value manifests.

Key Features:
- Full relationship loading (namespace, creator, owner, release, library, log)
- Support for filtering, pagination, and sorting
- Automatic retrieval of associated value manifests
- Comprehensive error handling and validation
- Structured response models with detailed metadata

The tools provide a clean, consistent interface for accessing Agency ID List data through the MCP protocol.
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
from app.services.agency_id_list_service import AgencyIdListService
from app.services.release_service import ReleaseService
from app.tools import _to_tool_error, get_tool_authenticated_user, tool_session
from app.tools.models.agency_id_list import (
    AgencyIdListResponseEntry,
    GetAgencyIdListPaginationResponse,
    GetAgencyIdListResponse,
)

logger = logging.getLogger("connectcenter.mcp.agency_id_list")

mcp = FastMCP("connectCenter MCP - Agency ID List Tools")


async def get_agency_id_list_service(
    session: AsyncSession = Depends(tool_session),
) -> AgencyIdListService:
    """Provide a requester-scoped agency ID list service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    return _build_agency_id_list_service(session, requester)


@mcp.tool(
    name="get_agency_id_lists",
    description="Get a paginated list of agency ID lists associated with a specific release.",
    output_schema={
        "type": "object",
        "description": "Response containing paginated list of agency ID lists",
        "properties": {
            "total_items": {"type": "integer", "description": "Total number of agency ID lists available. Allowed values: non-negative integers (≥0).", "example": 25},
            "offset": {"type": "integer", "description": "Offset of the first item in this page. Allowed values: non-negative integers (≥0). Default value: 0.", "example": 0},
            "limit": {"type": "integer", "description": "Number of items returned in this page. Allowed values: integers between 1 and 100 (inclusive). Default value: 10.", "example": 10},
            "items": {
                "type": "array",
                "description": "List of agency ID lists on this page",
                "items": {
                    "type": "object",
                    "properties": {
                        "agency_id_list_manifest_id": {"type": "integer", "description": "Unique identifier for the agency ID list manifest", "example": 12345},
                        "agency_id_list_id": {"type": "integer", "description": "Unique identifier for the agency ID list", "example": 6789},
                        "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                        "enum_type_guid": {"type": ["string", "null"], "description": "Enum type GUID for the agency ID list", "example": "a1b2c3d4e5f6789012345678901234ab"},
                        "name": {"type": "string", "description": "Name of the agency ID list", "example": "Country Code"},
                        "list_id": {"type": "string", "description": "List identifier", "example": "ISO3166-1"},
                        "version_id": {"type": "string", "description": "Version identifier", "example": "1.0"},
                        "definition": {"type": ["string", "null"], "description": "Definition of the agency ID list", "example": "Standard country codes"},
                        "remark": {"type": ["string", "null"], "description": "Remarks about the agency ID list", "example": "Based on ISO 3166-1"},
                        "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://www.iso.org/iso-3166-country-codes.html"},
                        "is_deprecated": {"type": "boolean", "description": "Whether the agency ID list is deprecated", "example": False},
                        "state": {"type": ["string", "null"], "description": "Current state of the agency ID list", "example": "Published"},
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
                            "description": "List of agency ID list values",
                            "items": {
                                "type": "object",
                                "properties": {
                                    "agency_id_list_value_manifest_id": {"type": "integer", "description": "Unique identifier for the agency ID list value manifest", "example": 12345},
                                    "agency_id_list_value_id": {"type": "integer", "description": "Unique identifier for the agency ID list value", "example": 6789},
                                    "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                                    "value": {"type": "string", "description": "Value of the agency ID list", "example": "US"},
                                    "name": {"type": ["string", "null"], "description": "Name of the agency ID list value", "example": "United States"},
                                    "definition": {"type": ["string", "null"], "description": "Definition of the agency ID list value", "example": "United States of America"},
                                    "is_deprecated": {"type": "boolean", "description": "Whether the agency ID list value is deprecated", "example": False},
                                    "is_developer_default": {"type": "boolean", "description": "Whether this is the developer default value", "example": False},
                                    "is_user_default": {"type": "boolean", "description": "Whether this is the user default value", "example": False}
                                },
                                "required": ["agency_id_list_value_manifest_id", "agency_id_list_value_id", "guid", "value", "is_deprecated"]
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
                            "description": "Information about the creation of the agency ID list",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who created the agency ID list",
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
                            "description": "Information about the last update of the agency ID list",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who last updated the agency ID list",
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
                    "required": ["agency_id_list_manifest_id", "agency_id_list_id", "guid", "name", "list_id", "version_id", "is_deprecated", "library", "release", "owner", "created", "last_updated"]
                }
            }
        },
        "required": ["total_items", "offset", "limit", "items"]
    }
)
async def get_agency_id_lists(
    release_id: Annotated[int, Field(gt=0, description="Filter by release ID using exact match.")],
    name: Annotated[str | None, Field(default=None, description="Filter by agency ID list name using partial match (case-insensitive).")],
    list_id: Annotated[str | None, Field(default=None, description="Filter by list ID using partial match (case-insensitive).")],
    version_id: Annotated[str | None, Field(default=None, description="Filter by version ID using partial match (case-insensitive).")],
    created_on: Annotated[str | None, Field(default=None, description="Filter by creation date using an inclusive range: '[before~after]'.")],
    last_updated_on: Annotated[str | None, Field(default=None, description="Filter by last update date using an inclusive range: '[before~after]'.")],
    order_by: Annotated[str | None, Field(default=None, description="Comma-separated list of properties to order results by. Allowed columns: name, list_id, version_id, definition, creation_timestamp, last_update_timestamp.")],
    offset: Annotated[int, Field(default=0, ge=0, description="The offset from the beginning of the list.")],
    limit: Annotated[int, Field(default=10, ge=1, le=100, description="The maximum number of items to return.")],
    agency_id_list_service: AgencyIdListService = Depends(get_agency_id_list_service),
) -> GetAgencyIdListPaginationResponse:
    """
    Get a paginated list of agency ID lists associated with a specific release.

    This function retrieves agency ID lists that are associated with the given release_id. This tool enables
    users to query and manage agency ID lists within specific releases, including filtering by name, list_id,
    version_id, and other attributes. It supports pagination, filtering, and sorting. The release_id
    filter is required to ensure you get agency ID lists from the correct release context.

    Args:
        release_id (int): Filter by release ID using exact match (required).
        name (str | None, optional): Filter by agency ID list name using partial match (case-insensitive). Defaults to None.
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
        GetAgencyIdListPaginationResponse: Response object containing:
            - total_items: Total number of agency ID lists available
            - offset: Offset of the first item in this page
            - limit: Number of items returned in this page
            - items: List of agency ID lists on this page with detailed information including:
                - agency_id_list_manifest_id: Unique identifier for the agency ID list manifest
                - agency_id_list_id: Unique identifier for the agency ID list
                - guid: Unique identifier within the release
                - enum_type_guid: Enum type GUID (if any)
                - name: Name of the agency ID list
                - list_id: External identifier
                - version_id: Agency ID list version number
                - definition: Description of the agency ID list
                - remark: Usage information about the agency ID list
                - definition_source: URL indicating the source of the definition
                - namespace: Namespace information (if any)
                - library: Library information with library_id and name
                - release: Release information with release_id, release_num, and state
                - log: Log information with log_id, revision_num, and revision_tracking_num (if any)
                - is_deprecated: Whether the agency ID list is deprecated
                - state: State of the agency ID list
                - values: List of agency ID list values with their details
                - owner: User information about the owner of the agency ID list
                - created: Information about the creation of the agency ID list
                - last_updated: Information about the last update of the agency ID list

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
        >>> result = get_agency_id_lists(release_id=123, offset=0, limit=10)
        >>> print(f"Found {result.total_items} agency ID lists")

        Filtered search:
        >>> result = get_agency_id_lists(release_id=123, name="government", limit=5)
        >>> for agency_id_list in result.items:
        ...     print(f"Agency ID list: {agency_id_list.name} (ID: {agency_id_list.list_id})")

        Date range filtering:
        >>> result = get_agency_id_lists(release_id=123, created_on="[2024-01-01~2024-12-31]")
        >>> print(f"Agency ID lists created in 2024: {result.total_items}")
    """
    try:
        page = await agency_id_list_service.list(
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
        raise _to_tool_error(exc, fallback="Unable to retrieve agency ID lists.") from exc


@mcp.tool(
    name="get_agency_id_list",
    description="Get a specific agency ID list by its manifest ID.",
    output_schema={
        "type": "object",
        "description": "Response containing agency ID list information",
        "properties": {
            "agency_id_list_manifest_id": {"type": "integer", "description": "Unique identifier for the agency ID list manifest", "example": 12345},
            "agency_id_list_id": {"type": "integer", "description": "Unique identifier for the agency ID list", "example": 6789},
            "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
            "enum_type_guid": {"type": ["string", "null"], "description": "Enum type GUID for the agency ID list", "example": "a1b2c3d4e5f6789012345678901234ab"},
            "name": {"type": "string", "description": "Name of the agency ID list", "example": "Country Code"},
            "list_id": {"type": "string", "description": "List identifier", "example": "ISO3166-1"},
            "version_id": {"type": "string", "description": "Version identifier", "example": "1.0"},
            "definition": {"type": ["string", "null"], "description": "Definition of the agency ID list", "example": "Standard country codes"},
            "remark": {"type": ["string", "null"], "description": "Remarks about the agency ID list", "example": "Based on ISO 3166-1"},
            "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://www.iso.org/iso-3166-country-codes.html"},
            "is_deprecated": {"type": "boolean", "description": "Whether the agency ID list is deprecated", "example": False},
            "state": {"type": ["string", "null"], "description": "Current state of the agency ID list", "example": "Published"},
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
                "description": "List of agency ID list values",
                "items": {
                    "type": "object",
                    "properties": {
                        "agency_id_list_value_manifest_id": {"type": "integer", "description": "Unique identifier for the agency ID list value manifest", "example": 12345},
                        "agency_id_list_value_id": {"type": "integer", "description": "Unique identifier for the agency ID list value", "example": 6789},
                        "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                        "value": {"type": "string", "description": "Value of the agency ID list", "example": "US"},
                        "name": {"type": ["string", "null"], "description": "Name of the agency ID list value", "example": "United States"},
                        "definition": {"type": ["string", "null"], "description": "Definition of the agency ID list value", "example": "United States of America"},
                        "is_deprecated": {"type": "boolean", "description": "Whether the agency ID list value is deprecated", "example": False},
                        "is_developer_default": {"type": "boolean", "description": "Whether this is the developer default value", "example": False},
                        "is_user_default": {"type": "boolean", "description": "Whether this is the user default value", "example": False}
                    },
                    "required": ["agency_id_list_value_manifest_id", "agency_id_list_value_id", "guid", "value", "is_deprecated"]
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
                "description": "Information about the creation of the agency ID list",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who created the agency ID list",
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
                "description": "Information about the last update of the agency ID list",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who last updated the agency ID list",
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
        "required": ["agency_id_list_manifest_id", "agency_id_list_id", "guid", "name", "list_id", "version_id", "library", "release", "owner", "created", "last_updated"]
    }
)
async def get_agency_id_list(
    agency_id_list_manifest_id: Annotated[int, Field(gt=0, description="Unique numeric identifier of the agency ID list manifest to retrieve.")],
    agency_id_list_service: AgencyIdListService = Depends(get_agency_id_list_service),
) -> GetAgencyIdListResponse:
    """
    Get a specific agency ID list by its manifest ID.

    This function retrieves a single agency ID list from the database and returns
    detailed information including metadata about who created it and when it was
    last updated, along with agency ID list-specific attributes, namespace information,
    and associated agency ID list values.

    Args:
        agency_id_list_manifest_id (int): The unique identifier of the agency ID list manifest to fetch

    Returns:
        GetAgencyIdListResponse: Response object containing:
            - agency_id_list_manifest_id: Unique identifier for the agency ID list manifest
            - agency_id_list_id: Unique identifier for the agency ID list
            - guid: Unique identifier within the release
            - enum_type_guid: Enum type GUID (if any)
            - name: Name of the agency ID list
            - list_id: External identifier
            - version_id: Agency ID list version number
            - definition: Description of the agency ID list
            - remark: Usage information about the agency ID list
            - definition_source: URL indicating the source of the definition
            - namespace: Namespace information (if any)
            - library: Library information with library_id and name
            - release: Release information with release_id, release_num, and state
            - log: Log information with log_id, revision_num, and revision_tracking_num (if any)
            - is_deprecated: Whether the agency ID list is deprecated
            - state: State of the agency ID list
            - values: List of agency ID list values with their details
            - owner: User information about the owner of the agency ID list
            - created: Information about the creation of the agency ID list
            - last_updated: Information about the last update of the agency ID list

    Raises:
        ToolError: If validation fails, the agency ID list manifest is not found, or other errors occur.
            Common error scenarios include:
            - Agency ID list manifest with the specified ID does not exist
            - Database connection issues
            - Authentication failures

    Examples:
        Get a specific agency ID list:
        >>> result = get_agency_id_list(agency_id_list_manifest_id=123)
        >>> print(f"Agency ID list: {result.name} (GUID: {result.guid})")
        >>> print(f"Owner: {result.owner.username}")
        >>> print(f"Values: {len(result.values)}")

        Access agency ID list values:
        >>> for value in result.values:
        ...     print(f"  {value.value}: {value.name}")
    """
    try:
        row = await agency_id_list_service.get(agency_id_list_manifest_id)
        if row is None:
            raise ValueError(
                f"The agency ID list with manifest ID {agency_id_list_manifest_id} was not found. Please check the ID and try again."
            )
        return GetAgencyIdListResponse.model_validate(row, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to retrieve agency ID list {agency_id_list_manifest_id}.") from exc


def _build_agency_id_list_service(
    session: AsyncSession,
    requester: AuthenticatedUser,
) -> AgencyIdListService:
    """Construct the agency ID list service for an MCP request."""
    plugin = get_vendor_plugin()
    release_service = ReleaseService(
        plugin.create_release_repository(session),
        plugin.create_app_user_repository(session),
        requester=requester,
    )
    return AgencyIdListService(
        plugin.create_agency_id_list_repository(session),
        release_service,
        plugin.create_app_user_repository(session),
        requester=requester,
    )


def _to_list_response(*, items: list[Any], total: int, offset: int, limit: int) -> GetAgencyIdListPaginationResponse:
    """Build the paginated MCP response model."""
    return GetAgencyIdListPaginationResponse(
        total_items=total,
        offset=offset,
        limit=limit,
        items=[AgencyIdListResponseEntry.model_validate(item, from_attributes=True) for item in items],
    )
