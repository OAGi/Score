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

import logging
from typing import Annotated

from fastapi import HTTPException
from fastmcp import FastMCP
from fastmcp.exceptions import ToolError
from pydantic import Field

from services import AgencyIdListService, DateRangeParams, PaginationParams
from tools import _validate_auth_and_db, parse_order_by_to_sorts, _create_user_info
from tools.models.agency_id_list import (
    AgencyIdListValueInfo,
    GetAgencyIdListResponse,
    GetAgencyIdListsResponse,
)
from tools.models.common import LibraryInfo, LogInfo, NamespaceInfo, ReleaseInfo, WhoAndWhen
from tools.utils import parse_date_range

# Configure logging
logger = logging.getLogger(__name__)

mcp = FastMCP("Score MCP Server - Agency ID List Tools")


@mcp.tool(
    name="get_agency_id_lists",
    description="Get a paginated list of agency ID lists associated with a specific release",
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
                    "required": ["agency_id_list_manifest_id", "agency_id_list_id", "guid", "name", "list_id", "version_id", "library", "release", "owner", "created", "last_updated"]
                }
            }
        },
        "required": ["total_items", "offset", "limit", "items"]
    }
)
async def get_agency_id_lists(
    release_id: Annotated[int, Field(
        description="Filter by release ID using exact match.",
        examples=[123, 456, 789],
        gt=0,
        title="Release ID"
    )],
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
    name: Annotated[str | None, Field(
        description="Filter by agency ID list name using partial match (case-insensitive).",
        examples=["ISO 3166", "UN/LOCODE", "Country Codes"],
        title="Agency ID List Name"
    )] = None,
    list_id: Annotated[str | None, Field(
        description="Filter by list ID using partial match (case-insensitive).",
        examples=["ISO3166-1", "UNLOCODE", "Country"],
        title="List ID"
    )] = None,
    version_id: Annotated[str | None, Field(
        description="Filter by version ID using partial match (case-insensitive).",
        examples=["1.0", "2.1", "3.0"],
        title="Version ID"
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
        description="Comma-separated list of properties to order results by. Prefix with '-' for descending, '+' for ascending (default ascending). Allowed columns: name, list_id, version_id, definition, creation_timestamp, last_update_timestamp. Example: '-creation_timestamp,+name' translates to 'creation_timestamp DESC, name ASC'.",
        examples=["-creation_timestamp,+name", "name", "-last_update_timestamp"],
        title="Order By"
    )] = None
) -> GetAgencyIdListsResponse:
    """
    Get a paginated list of agency ID lists associated with a specific release.
    
    This function retrieves agency ID lists that are associated with the given release_id. This tool enables
    users to query and manage agency ID lists within specific releases, including filtering by name, list_id,
    version_id, and other attributes. It supports pagination, filtering, and sorting. The release_id 
    filter is required to ensure you get agency ID lists from the correct release context.
    
    Args:
        release_id (int): Filter by release ID using exact match.
        offset (int | None, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int | None, optional): The maximum number of items to return. Must be a non-negative number. Defaults to 10.
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
    
    Returns:
        GetAgencyIdListsResponse: Response object containing:
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
    logger.info(
        f"Retrieving agency ID lists: release_id={release_id}, offset={offset}, limit={limit}, "
        f"name={name}, list_id={list_id}, version_id={version_id}, created_on={created_on}, "
        f"last_updated_on={last_updated_on}, order_by={order_by}"
    )
    
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()
    logger.debug(f"User authenticated: {app_user.login_id} (ID: {app_user.app_user_id})")

    # Create service instance
    agency_id_list_service = AgencyIdListService()
    logger.debug("Service instance initialized")

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
                f"Valid columns: {", ".join(agency_id_list_service.allowed_columns_for_order_by)}") from e

    # Get agency ID lists
    try:
        logger.debug(f"Querying agency ID lists for release {release_id}")
        page = agency_id_list_service.get_agency_id_lists_by_release(
            release_id=release_id,
            name=name,
            list_id=list_id,
            version_id=version_id,
            created_on_params=created_on_params,
            last_updated_on_params=last_updated_on_params,
            pagination=pagination,
            sort_list=sort_list
        )
        logger.info(f"Found {len(page.items)} agency ID lists (total available: {page.total})")

        result = GetAgencyIdListsResponse(
            total_items=page.total,
            offset=page.offset,
            limit=page.limit,
            items=[_create_agency_id_list_result(manifest, agency_id_list_service) for manifest in page.items]
        )
        logger.debug(f"Response prepared with {len(result.items)} items")
        return result
    except HTTPException as e:
        logger.error(f"Failed to retrieve agency ID lists: {e.detail} (status: {e.status_code})")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error while retrieving agency ID lists: {str(e)}", exc_info=True)
        raise ToolError(
            f"An unexpected error occurred while retrieving the agency ID lists: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="get_agency_id_list",
    description="Get a specific agency ID list by its manifest ID",
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
    agency_id_list_manifest_id: Annotated[int, Field(
        description="Unique numeric identifier of the agency ID list manifest to retrieve.",
        examples=[123, 456, 789],
        gt=0,
        title="Agency ID List Manifest ID"
    )]
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
    logger.info(f"Retrieving agency ID list: manifest_id={agency_id_list_manifest_id}")
    
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()
    logger.debug(f"User authenticated: {app_user.login_id} (ID: {app_user.app_user_id})")

    # Get agency ID list
    try:
        service = AgencyIdListService()
        logger.debug(f"Querying agency ID list manifest {agency_id_list_manifest_id}")
        manifest = service.get_agency_id_list_by_manifest_id(agency_id_list_manifest_id)
        logger.info(f"Retrieved agency ID list: '{manifest.agency_id_list.name if manifest.agency_id_list else 'N/A'}' (manifest_id: {manifest.agency_id_list_manifest_id})")

        result = _create_agency_id_list_result(manifest, service)
        logger.debug(f"Response prepared for agency ID list manifest {agency_id_list_manifest_id}")
        return result
    except HTTPException as e:
        logger.error(f"Failed to retrieve agency ID list: {e.detail} (status: {e.status_code})")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The agency ID list manifest with ID {agency_id_list_manifest_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error while retrieving agency ID list: {str(e)}", exc_info=True)
        raise ToolError(
            f"An unexpected error occurred while retrieving the agency ID list: {str(e)}. Please contact your system administrator.") from e


def _create_agency_id_list_result(manifest, agency_id_list_service) -> GetAgencyIdListResponse:
    """
    Create an agency ID list result from an AgencyIdListManifest model instance.
    
    Args:
        manifest: AgencyIdListManifest model instance with agency_id_list relationship
        agency_id_list_service: AgencyIdListService instance for retrieving related data
        
    Returns:
        GetAgencyIdListResponse: Formatted agency ID list result
    """
    logger.debug(f"Building response for agency ID list manifest {manifest.agency_id_list_manifest_id}")
    agency_id_list = manifest.agency_id_list
    
    # Get value manifests using the separate service function
    try:
        logger.debug(f"Retrieving value manifests for manifest {manifest.agency_id_list_manifest_id}")
        value_manifests = agency_id_list_service.get_agency_id_list_value_manifests_by_manifest_id(manifest.agency_id_list_manifest_id)
        logger.debug(f"Found {len(value_manifests)} value manifests")
    except Exception as e:
        logger.warning(f"Could not retrieve value manifests for manifest {manifest.agency_id_list_manifest_id}: {str(e)}")
        value_manifests = []  # Continue without value manifests rather than failing completely
    
    # Create namespace info if available
    namespace_info = None
    if agency_id_list.namespace:
        namespace_info = NamespaceInfo(
            namespace_id=agency_id_list.namespace.namespace_id,
            prefix=agency_id_list.namespace.prefix,
            uri=agency_id_list.namespace.uri
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

    # Create agency ID list values info from value manifests
    agency_id_list_values_info = []
    for value_manifest in value_manifests:
        agency_id_list_values_info.append(AgencyIdListValueInfo(
            agency_id_list_value_manifest_id=value_manifest.agency_id_list_value_manifest_id,
            agency_id_list_value_id=value_manifest.agency_id_list_value_id,
            guid=value_manifest.agency_id_list_value.guid,
            value=value_manifest.agency_id_list_value.value,
            name=value_manifest.agency_id_list_value.name,
            definition=value_manifest.agency_id_list_value.definition,
            is_deprecated=value_manifest.agency_id_list_value.is_deprecated,
            is_developer_default=value_manifest.agency_id_list_value.is_developer_default,
            is_user_default=value_manifest.agency_id_list_value.is_user_default
        ))

    return GetAgencyIdListResponse(
        agency_id_list_manifest_id=manifest.agency_id_list_manifest_id,
        agency_id_list_id=agency_id_list.agency_id_list_id,
        guid=agency_id_list.guid,
        enum_type_guid=agency_id_list.enum_type_guid,
        name=agency_id_list.name,
        list_id=agency_id_list.list_id,
        version_id=agency_id_list.version_id,
        definition=agency_id_list.definition,
        remark=agency_id_list.remark,
        definition_source=agency_id_list.definition_source,
        namespace=namespace_info,
        library=library_info,
        release=release_info,
        log=log_info,
        is_deprecated=agency_id_list.is_deprecated,
        state=agency_id_list.state,
        owner=_create_user_info(agency_id_list.owner),
        values=agency_id_list_values_info,
        created=WhoAndWhen(
            who=_create_user_info(agency_id_list.creator),
            when=agency_id_list.creation_timestamp
        ),
        last_updated=WhoAndWhen(
            who=_create_user_info(agency_id_list.last_updater),
            when=agency_id_list.last_update_timestamp
        )
    )
