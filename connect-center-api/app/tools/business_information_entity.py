"""
MCP Tools for managing Business Information Entity (BIE) operations in connectCenter.

This module provides Model Context Protocol (MCP) tools for creating, updating, deleting,
and querying Business Information Entities, which are runtime instances of Core Components
configured for specific business contexts. BIEs represent the actual business documents and
messages used in information exchanges, such as purchase orders, invoices, or shipping notices.
They serve as the contextualized, business-ready representations of Core Components, where
generic, reusable component structures are adapted and configured for specific business needs.

BIEs enable the practical application of Core Components in real-world business scenarios
by providing context-specific instantiations of component structures. Each BIE is based
on an ASCCP (Association Core Component Property) and is associated with one or more
Business Contexts that specify the circumstances under which it is used. BIEs form
hierarchical structures (ASBIEs for associations, BBIEs for basic properties) that
mirror the Core Component hierarchy but are tailored for specific business contexts.
This allows organizations to create standardized, interoperable business documents while
maintaining the flexibility to adapt components for different industries, regions, or
business processes. The tools provide a standardized MCP interface, enabling clients to
interact with BIE data programmatically.

Available Tools:
Top-Level BIE Management:
- get_top_level_asbiep_list: Retrieve paginated lists of Top-Level ASBIEPs filtered by release
  with optional filters for library_id, release_id_list, den, version, status, state, owner,
  is_deprecated, and date ranges. Supports custom sorting. Note: Only returns BIEs that have at least one
  business context assigned (business rule requirement).

- get_top_level_asbiep: Retrieve a single Top-Level ASBIEP by ID with full relationship loading,
  including all nested ASBIEs, BBIEs, business contexts, and metadata.

- create_top_level_asbiep: Create a new Top-Level ASBIEP (Business Information Entity) based
  on an ASCCP Manifest and assign it to one or more Business Contexts. Automatically generates
  a GUID and creates the underlying ABIE structure.

- update_top_level_asbiep: Update an existing Top-Level ASBIEP's properties such as version,
  status, state, and business context assignments.

- delete_top_level_asbiep: Delete a Top-Level ASBIEP and all associated ASBIEs, BBIEs, and
  BBIE Supplementary Components. Cascades deletion through the entire BIE hierarchy.

BIE Relationship Management:
- create_asbie: Create a new ASBIE (Association Business Information Element) relationship
  between two BIEs, representing an association in the business information structure.

- create_bbie: Create a new BBIE (Basic Business Information Element) relationship, representing
  a simple data element in the business information structure.

- create_bbie_sc: Create a new BBIE Supplementary Component, representing additional facets
  or constraints on a basic business information element.

- Additional update and delete operations for managing BIE relationships are available.

Key Features:
- Full CRUD operations for Top-Level BIEs and their relationships
- Complex relationship traversal (ASBIE, BBIE hierarchies)
- Business context assignment management
- Support for supplementary components and data type facets
- Full relationship loading including nested hierarchies
- Support for filtering, pagination, and sorting
- Comprehensive error handling and validation
- Structured response models with detailed metadata

Business Rules:
- Every BIE must have at least one business context assigned
- Top-Level ASBIEPs without business contexts are excluded from query results
- BIEs are based on ASCCP Manifests, which in turn reference ACC Manifests
- BIE hierarchies mirror the Core Component structure

The tools provide a clean, consistent interface for accessing BIE data through the MCP protocol.
All operations require proper authentication and authorization.
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
from app.services.business_information_entity_service import BusinessInformationEntityService
from app.tools import (
    _to_tool_error,
    get_tool_authenticated_user,
    str_to_bool,
    tool_session,
)
from app.tools.models.business_information_entity import (
    AssignBizCtxToTopLevelAsbiepToolResponse,
    CreateAsbieToolResponse,
    CreateBbieScToolResponse,
    CreateBbieToolResponse,
    CreateTopLevelAsbiepResponse,
    DeleteTopLevelAsbiepToolResponse,
    GetAsbieResponse,
    GetAsbieTemplateResponse,
    GetBbieResponse,
    GetBbieTemplateResponse,
    GetTopLevelAsbiepPaginationResponse,
    GetTopLevelAsbiepToolResponse,
    RemoveReusedTopLevelAsbiepToolResponse,
    ReuseTopLevelAsbiepToolResponse,
    TransferTopLevelAsbiepOwnershipResponse,
    UnassignBizCtxFromTopLevelAsbiepToolResponse,
    UpdateAsbieToolResponse,
    UpdateBbieScToolResponse,
    UpdateBbieToolResponse,
    UpdateTopLevelAsbiepResponse,
    UpdateTopLevelAsbiepStateToolResponse,
)

logger = logging.getLogger("connectcenter.mcp.business_information_entity")

mcp = FastMCP("connectCenter MCP - Business Information Entity Tools")


async def get_business_information_entity_service(
    session: AsyncSession = Depends(tool_session),
) -> BusinessInformationEntityService:
    """Provide a requester-scoped BIE service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    return _build_business_information_entity_service(session, requester)


async def get_app_user_service(
    session: AsyncSession = Depends(tool_session),
) -> AppUserService:
    """Provide a requester-scoped app-user service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    vendor_plugin = get_vendor_plugin()
    app_user_repository = vendor_plugin.create_app_user_repository(session)
    return AppUserService(app_user_repository=app_user_repository, requester=requester)


@mcp.tool(
    name="get_top_level_asbiep_list",
    description="Get a paginated list of Top-Level ASBIEPs (Association Business Information Entity Properties).",
    output_schema={
        "type": "object",
        "description": "Response containing paginated list of Top-Level ASBIEPs (Association Business Information Entity Properties).",
        "properties": {
            "total_items": {"type": "integer",
                            "description": "Total number of Top-Level ASBIEPs available. Allowed values: non-negative integers (≥0).",
                            "example": 25},
            "offset": {"type": "integer",
                       "description": "Offset of the first item in this page. Allowed values: non-negative integers (≥0). Default value: 0.",
                       "example": 0},
            "limit": {"type": "integer",
                      "description": "Number of items returned in this page. Allowed values: integers between 1 and 100 (inclusive). Default value: 10.",
                      "example": 10},
            "items": {
                "type": "array",
                "description": "List of Top-Level ASBIEPs on this page",
                "items": {
                    "type": "object",
                    "properties": {
                        "top_level_asbiep_id": {"type": "integer",
                                                "description": "Unique identifier for the top-level ASBIEP (Association Business Information Entity Property)",
                                                "example": 12345},
                        "asbiep_id": {"type": "integer",
                                      "description": "Unique identifier for the ASBIEP (Association Business Information Entity Property)",
                                      "example": 12346},
                        "guid": {"type": "string",
                                 "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)",
                                 "example": "a1b2c3d4e5f6789012345678901234ab"},
                        "den": {"type": "string",
                                "description": "Data Element Name (DEN) following the rule: property_term + '. ' + object_class_term",
                                "example": "Purchase Order. Purchase Order"},
                        "property_term": {"type": "string",
                                          "description": "Property term from the based ASCCP (Association Core Component Property)",
                                          "example": "Purchase Order"},
                        "display_name": {"type": ["string", "null"],
                                         "description": "Display name of the ASBIEP (Association Business Information Entity Property)",
                                         "example": "Purchase Order BIE"},
                        "version": {"type": ["string", "null"], "description": "Version number assigned by the user",
                                    "example": "1.0"},
                        "status": {"type": ["string", "null"],
                                   "description": "Usage status of the top-level ASBIEP (Association Business Information Entity Property) (e.g., 'Prototype', 'Test', 'Production')",
                                   "example": "Production"},
                        "biz_term": {"type": ["string", "null"],
                                     "description": "Business term to indicate what the BIE is called in a particular business context",
                                     "example": "PO"},
                        "remark": {"type": ["string", "null"],
                                   "description": "Context-specific usage remarks about the BIE",
                                   "example": "Used for procurement processes"},
                        "business_contexts": {
                            "type": "array",
                            "description": "List of associated business contexts",
                            "items": {
                                "type": "object",
                                "properties": {
                                    "biz_ctx_id": {"type": "integer",
                                                   "description": "Unique identifier for the business context",
                                                   "example": 1},
                                    "guid": {"type": "string",
                                             "description": "Unique identifier for the business context",
                                             "example": "a1b2c3d4e5f6789012345678901234ab"},
                                    "name": {"type": ["string", "null"], "description": "Name of the business context",
                                             "example": "Production Environment"}
                                },
                                "required": ["biz_ctx_id", "guid"]
                            }
                        },
                        "state": {"type": "string",
                                  "description": "State of the top-level ASBIEP (Association Business Information Entity Property)",
                                  "example": "Published"},
                        "is_deprecated": {"type": "boolean",
                                          "description": "Whether the top-level ASBIEP (Association Business Information Entity Property) is deprecated",
                                          "example": False},
                        "deprecated_reason": {"type": ["string", "null"],
                                              "description": "The reason for the deprecation",
                                              "example": "Replaced by new version"},
                        "deprecated_remark": {"type": ["string", "null"],
                                              "description": "Additional remarks about the deprecation",
                                              "example": "Use version 2.0 instead"},
                        "owner": {
                            "type": "object",
                            "description": "Owner information",
                            "properties": {
                                "user_id": {"type": "integer", "description": "Unique identifier for the user",
                                            "example": 1},
                                "login_id": {"type": "string", "description": "User's login identifier",
                                             "example": "admin"},
                                "username": {"type": "string", "description": "Display name of the user",
                                             "example": "Administrator"},
                                "roles": {"type": "array",
                                          "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                          "description": "List of roles assigned to the user", "example": ["Admin"]}
                            },
                            "required": ["user_id", "login_id", "username", "roles"]
                        },
                        "created": {
                            "type": "object",
                            "description": "Information about the creation of the BIE (Business Information Entity)",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who created the BIE (Business Information Entity)",
                                    "properties": {
                                        "user_id": {"type": "integer", "description": "Unique identifier for the user",
                                                    "example": 1},
                                        "login_id": {"type": "string", "description": "User's login identifier",
                                                     "example": "admin"},
                                        "username": {"type": "string", "description": "Display name of the user",
                                                     "example": "Administrator"},
                                        "roles": {"type": "array", "items": {"type": "string",
                                                                             "enum": ["Admin", "Developer",
                                                                                      "End-User"]},
                                                  "description": "List of roles assigned to the user",
                                                  "example": ["Admin"]}
                                    },
                                    "required": ["user_id", "login_id", "username", "roles"]
                                },
                                "when": {"type": "string", "format": "date-time",
                                         "description": "Creation timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)",
                                         "example": "2024-01-15T10:30:00Z"}
                            },
                            "required": ["who", "when"]
                        },
                        "last_updated": {
                            "type": "object",
                            "description": "Information about the last update of the BIE (Business Information Entity)",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who last updated the BIE (Business Information Entity)",
                                    "properties": {
                                        "user_id": {"type": "integer", "description": "Unique identifier for the user",
                                                    "example": 1},
                                        "login_id": {"type": "string", "description": "User's login identifier",
                                                     "example": "admin"},
                                        "username": {"type": "string", "description": "Display name of the user",
                                                     "example": "Administrator"},
                                        "roles": {"type": "array", "items": {"type": "string",
                                                                             "enum": ["Admin", "Developer",
                                                                                      "End-User"]},
                                                  "description": "List of roles assigned to the user",
                                                  "example": ["Admin"]}
                                    },
                                    "required": ["user_id", "login_id", "username", "roles"]
                                },
                                "when": {"type": "string", "format": "date-time",
                                         "description": "Last update timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)",
                                         "example": "2024-01-15T10:30:00Z"}
                            },
                            "required": ["who", "when"]
                        }
                    },
                    "required": ["top_level_asbiep_id", "asbiep_id", "guid", "den", "property_term", "state",
                                 "business_contexts", "owner", "created", "last_updated"]
                }
            }
        },
        "required": ["total_items", "offset", "limit", "items"]
    }
)
async def get_top_level_asbiep_list(
    library_id: Annotated[int | str | None, Field(default=None, description="Filter by library ID. String values are converted to integers.")],
    release_id_list: Annotated[str | None, Field(default=None, description="Filter by release IDs. Use a comma-separated list like '1,2,3'.")],
    den: Annotated[str | None, Field(default=None, description="Filter by DEN or display name using partial match.")],
    version: Annotated[str | None, Field(default=None, description="Filter by version using partial match.")],
    status: Annotated[str | None, Field(default=None, description="Filter by status using partial match.")],
    state: Annotated[str | None, Field(default=None, description="Filter by lifecycle state using partial match.")],
    owner: Annotated[str | None, Field(default=None, description="Comma-separated owner login IDs to filter by exact match. Prefix a login ID with '!' to exclude it.")],
    is_deprecated: Annotated[bool | str | None, Field(default=None, description="Filter by deprecation flag. Accepts bool values or 'true'/'false' strings.")],
    created_on: Annotated[str | None, Field(default=None, description="Filter by creation date range using '[before~after]'.")],
    last_updated_on: Annotated[str | None, Field(default=None, description="Filter by last update date range using '[before~after]'.")],
    order_by: Annotated[str | None, Field(default=None, description="Comma-separated sort expression. Allowed columns: den, version, status, state, creation_timestamp, last_update_timestamp.")],
    offset: Annotated[int, Field(default=0, ge=0, description="Zero-based page offset.")],
    limit: Annotated[int, Field(default=10, ge=1, le=100, description="Maximum number of items to return.")],
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> GetTopLevelAsbiepPaginationResponse:
    """
    Get a paginated list of Top-Level ASBIEPs (Association Business Information Entity Properties).

    This function retrieves Top-Level ASBIEPs registered in connectCenter. Each Top-Level ASBIEP represents
    a complete Business Information Entity (BIE) that includes the top-level ASBIEP, its associated ASBIEP,
    and the related ABIE (Aggregation Business Information Entity). It supports pagination, filtering, and sorting.
    The release_id_list filter is optional - if not provided, searches across all releases.

    The function follows the relationship chain: top_level_asbiep -> asbiep -> based_asccp_manifest_id -> asccp_manifest_id -> asccp_id

    Args:
        library_id (int | str | None, optional): Filter by library ID using exact match. Accepts int, str (converted to int), or None. Defaults to None.
        release_id_list (str | None, optional): Filter by release IDs using exact match. Comma-separated list of release IDs. Examples: '123', '123,456', '123,456,789'. If not provided, searches across all releases. Defaults to None.
        den (str | None, optional): Filter by Data Element Name (DEN) or display name using partial match (case-insensitive). Matches either the DEN field or the display_name field. Defaults to None.
        version (str | None, optional): Filter by version using partial match (case-insensitive). Defaults to None.
        status (str | None, optional): Filter by status using partial match (case-insensitive). Defaults to None.
        state (str | None, optional): Filter by state using partial match (case-insensitive). Defaults to None.
        owner (str | None, optional): Comma-separated owner login IDs using exact match.
            Prefix a login ID with '!' to exclude it. Examples: 'john.doe', 'john.doe,jane.doe',
            '!john.doe', 'john.doe,!jane.doe'. Login IDs cannot contain '!' or ','. Defaults to None.
        is_deprecated (bool | str | None, optional): Filter by deprecation status. Accepts bool, str ('True'/'true'/'1' for True, 'False'/'false'/'0' for False), or None. Defaults to None.
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
            Allowed columns: den, version, status, state, creation_timestamp, last_update_timestamp.
            Example: '-creation_timestamp,+den' translates to 'creation_timestamp DESC, den ASC'.
            Defaults to None.
        offset (int, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int, optional): The maximum number of items to return. Must be between 1 and 100 (inclusive). Defaults to 10.

    Returns:
        GetBusinessInformationEntitiesResponse: Response object containing:
            - total_items: Total number of Top-Level ASBIEPs available
            - offset: Offset of the first item in this page
            - limit: Number of items returned in this page
            - items: List of Top-Level ASBIEPs on this page with detailed information including:
                - top_level_asbiep_id: Unique identifier for the top-level ASBIEP (Association Business Information Entity Property)
                - asbiep_id: Unique identifier for the ASBIEP (Association Business Information Entity Property)
                - guid: Unique identifier within the release
                - den: Data Element Name following the rule: property_term + '. ' + object_class_term
                - property_term: Property term from the based ASCCP (Association Core Component Property)
                - display_name: Display name of the ASBIEP (Association Business Information Entity Property)
                - version: Version number assigned by the user
                - status: Usage status of the top-level ASBIEP (Association Business Information Entity Property)
                - biz_term: Business term for the Top-Level ASBIEP
                - remark: Context-specific usage remarks
                - business_contexts: List of associated business contexts
                - state: State of the top-level ASBIEP (Association Business Information Entity Property)
                - is_deprecated: Whether the top-level ASBIEP (Association Business Information Entity Property) is deprecated
                - deprecated_reason: Reason for deprecation (if deprecated)
                - deprecated_remark: Additional deprecation remarks (if deprecated)
                - owner: User information about the owner
                - created: Information about the creation
                - last_updated: Information about the last update

    Raises:
        ToolError: If validation fails, parsing errors occur, or other errors happen.
            Common error scenarios include:
            - Invalid pagination parameters (negative offset, limit out of range)
            - Invalid date range format
            - Invalid order_by column names or format
            - Database connection issues
            - Authentication failures

    Examples:
        Basic listing (all releases):
        >>> result = get_top_level_asbiep_list()
        >>> print(f"Found {result.total_items} Top-Level ASBIEPs")

        Filtered by specific releases:
        >>> result = get_top_level_asbiep_list(release_id_list="123,456")
        >>> print(f"Found {result.total_items} Top-Level ASBIEPs")

        Filtered by library:
        >>> result = get_top_level_asbiep_list(library_id=1)
        >>> print(f"Found {result.total_items} Top-Level ASBIEPs")

        Filtered search:
        >>> result = get_top_level_asbiep_list(release_id_list="123", property_term="Purchase", limit=5)
        >>> for top_level_asbiep in result.items:
        ...     print(f"Top-Level ASBIEP: {top_level_asbiep.display_name} (Property: {top_level_asbiep.property_term})")

        DEN or display name filtering:
        >>> result = get_top_level_asbiep_list(release_id_list="123", den="Purchase Order", limit=5)
        >>> for top_level_asbiep in result.items:
        ...     print(f"Top-Level ASBIEP: {top_level_asbiep.display_name} (DEN: {top_level_asbiep.den})")

        Date range filtering:
        >>> result = get_top_level_asbiep_list(release_id_list="123", created_on="[2024-01-01~2024-12-31]")
        >>> print(f"Top-Level ASBIEPs created in 2024: {result.total_items}")
    """
    try:
        is_deprecated = str_to_bool(is_deprecated)
    except ToolError:
        raise
    except Exception as e:
        raise ToolError(
            f"Type conversion error: {str(e)}. Please check your parameter types and try again."
        ) from e

    try:
        page = await business_information_entity_service.list_top_level_asbieps(
            limit=limit,
            offset=offset,
            order_by=order_by,
            library_id=_to_optional_int(library_id, field_name="library_id"),
            release_ids=_parse_int_list(release_id_list, field_name="release_id_list"),
            den=den,
            version=version,
            status=status,
            state=state,
            owner=owner,
            is_deprecated=is_deprecated,
            created_on=parse_date_range(created_on),
            last_updated_on=parse_date_range(last_updated_on),
        )
        return GetTopLevelAsbiepPaginationResponse.model_validate(
            {
                "total_items": page.total,
                "offset": page.offset,
                "limit": page.limit,
                "items": page.items,
            },
            from_attributes=True,
        )
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to retrieve top-level ASBIEPs.") from exc


@mcp.tool(
    name="get_top_level_asbiep",
    description="Get a top-level ASBIEP (Association Business Information Entity Property) by its ID. The response displays the ASBIEP first, with its relationships (ASBIE/BBIE) shown as children under the role_of_abie section. The relationships array is an ordered sequence that preserves the original order from the ABIE structure. IMPORTANT: Groups (component_type 3=SemanticGroup or 4=UserExtensionGroup) are automatically skipped in BIE expressions, so the BIE structure may differ from the CC structure. To find the correct from_abie_id for create_asbie/create_bbie, traverse the actual BIE structure recursively using get_top_level_asbiep, get_asbie_*, and get_bbie_* to find the abie_id in the BIE structure. This tool is used for profiling core components - when is_used=True, the component is profiled for practical use (asbie_id/bbie_id will be created), when is_used=False, it shows all available components for profiling. To explore relationships further: (1) If a relationship has asbie_id/bbie_id (is_used=True), use get_asbie_by_asbie_id(asbie_id) or get_bbie_by_bbie_id(bbie_id) to get full details. (2) If a relationship has no asbie_id/bbie_id (is_used=False), use get_asbie_by_based_ascc_manifest_id(top_level_asbiep_id, parent_abie_path, based_ascc_manifest_id) or get_bbie_by_based_bcc_manifest_id(top_level_asbiep_id, parent_abie_path, based_bcc_manifest_id) to explore the component structure before profiling.",
    output_schema={
        "type": "object",
        "description": "Response containing a top-level ASBIEP (Association Business Information Entity Property) with basic information. The ASBIEP is displayed first, with its relationships (ASBIE/BBIE) shown as children under the role_of_abie section. The relationships array is an ordered sequence that preserves the original order from the ABIE structure. IMPORTANT: Groups (component_type 3=SemanticGroup or 4=UserExtensionGroup) are automatically skipped in BIE expressions, so the BIE structure may differ from the CC structure. To find the correct from_abie_id for create_asbie/create_bbie, traverse the actual BIE structure recursively. Each relationship has an 'is_used' property indicating whether it's profiled for practical use (is_used=True means asbie_id/bbie_id exists). To explore relationships: (1) If relationship has asbie_id/bbie_id (is_used=True), use get_asbie_by_asbie_id(asbie_id) or get_bbie_by_bbie_id(bbie_id). (2) If relationship has no asbie_id/bbie_id (is_used=False), use get_asbie_by_based_ascc_manifest_id(top_level_asbiep_id, parent_abie_path, based_ascc_manifest_id) or get_bbie_by_based_bcc_manifest_id(top_level_asbiep_id, parent_abie_path, based_bcc_manifest_id) - extract parent_abie_path from relationship.path and manifest_id from relationship.based_ascc/based_bcc.",
        "properties": {
            "top_level_asbiep_id": {"type": "integer", "description": "Unique identifier for the top-level ASBIEP",
                                    "example": 12345},
            "asbiep": {
                "type": "object",
                "description": "ASBIEP (Association Business Information Entity Property) information",
                "properties": {
                    "asbiep_id": {"type": "integer", "description": "Unique identifier for the ASBIEP",
                                  "example": 12346},
                    "owner_top_level_asbiep": {
                        "type": "object",
                        "description": "Top-Level ASBIEP information",
                        "properties": {
                            "top_level_asbiep_id": {"type": "integer",
                                                    "description": "Unique identifier for the top-level ASBIEP",
                                                    "example": 12345},
                            "library": {
                                "type": "object",
                                "description": "Library information",
                                "properties": {
                                    "library_id": {"type": "integer",
                                                   "description": "Unique identifier for the library", "example": 1},
                                    "name": {"type": "string", "description": "Library name", "example": "OAGIS"}
                                },
                                "required": ["library_id", "name"]
                            },
                            "release": {
                                "type": "object",
                                "description": "Release information",
                                "properties": {
                                    "release_id": {"type": "integer",
                                                   "description": "Unique identifier for the release", "example": 1},
                                    "release_num": {"type": "string", "description": "Release number",
                                                    "example": "10.6"},
                                    "state": {"type": "string", "description": "Release state", "example": "Published"}
                                },
                                "required": ["release_id", "release_num", "state"]
                            },
                            "version": {"type": ["string", "null"], "description": "Version number", "example": "1.0"},
                            "status": {"type": ["string", "null"], "description": "Status of the top-level ASBIEP",
                                       "example": "Production"},
                            "state": {"type": "string", "description": "State of the top-level ASBIEP",
                                      "example": "Published"},
                            "is_deprecated": {"type": "boolean",
                                              "description": "Whether the top-level ASBIEP is deprecated",
                                              "example": False},
                            "deprecated_reason": {"type": ["string", "null"], "description": "Reason for deprecation",
                                                  "example": "Replaced by newer version"},
                            "deprecated_remark": {"type": ["string", "null"],
                                                  "description": "Additional remarks about deprecation",
                                                  "example": "Use version 2.0 instead"},
                            "owner": {
                                "type": "object",
                                "description": "Owner information",
                                "properties": {
                                    "user_id": {"type": "integer", "description": "Unique identifier for the user",
                                                "example": 100000001},
                                    "login_id": {"type": "string", "description": "User's login identifier",
                                                 "example": "john.doe"},
                                    "username": {"type": "string", "description": "Display name of the user",
                                                 "example": "John Doe"},
                                    "roles": {"type": "array", "items": {"type": "string"},
                                              "description": "List of roles assigned to the user",
                                              "example": ["End-User"]}
                                },
                                "required": ["user_id", "login_id", "username", "roles"]
                            }
                        },
                        "required": ["top_level_asbiep_id", "library", "release", "state", "is_deprecated", "owner"]
                    },
                    "based_asccp_manifest": {
                        "type": "object",
                        "description": "Based ASCCP manifest information",
                        "properties": {
                            "asccp_manifest_id": {"type": "integer",
                                                  "description": "Unique identifier for the ASCCP manifest",
                                                  "example": 12347},
                            "asccp_id": {"type": "integer", "description": "Unique identifier for the ASCCP",
                                         "example": 12348},
                            "role_of_acc_manifest_id": {"type": "integer", "description": "Role of ACC manifest ID",
                                                        "example": 12349},
                            "guid": {"type": "string", "description": "Unique identifier for the ASCCP",
                                     "example": "a1b2c3d4e5f6789012345678901234ab"},
                            "den": {"type": ["string", "null"], "description": "Data Element Name",
                                    "example": "Purchase Order. Purchase Order"},
                            "property_term": {"type": "string", "description": "Property term",
                                              "example": "Purchase Order"},
                            "definition": {"type": ["string", "null"], "description": "Definition of the ASCCP",
                                           "example": "A purchase order document"},
                            "definition_source": {"type": ["string", "null"], "description": "Source of the definition",
                                                  "example": "https://example.com/spec"},
                            "is_deprecated": {"type": "boolean", "description": "Whether the ASCCP is deprecated",
                                              "example": False}
                        },
                        "required": ["asccp_manifest_id", "asccp_id", "role_of_acc_manifest_id", "guid", "den",
                                     "property_term", "is_deprecated"]
                    },
                    "role_of_abie": {
                        "type": "object",
                        "description": "Role of ABIE information - contains the ABIE details and its relationships as children",
                        "properties": {
                            "abie_id": {"type": ["integer", "null"],
                                        "description": "Unique identifier for the ABIE. This can be passed to create_asbie() and create_bbie() as the from_abie_id parameter.",
                                        "example": 12348},
                            "guid": {"type": ["string", "null"], "description": "Unique identifier for the ABIE",
                                     "example": "a1b2c3d4e5f6789012345678901234ab"},
                            "based_acc_manifest": {
                                "type": "object",
                                "description": "Based ACC manifest information",
                                "properties": {
                                    "acc_manifest_id": {"type": "integer",
                                                        "description": "Unique identifier for the ACC manifest",
                                                        "example": 12349},
                                    "acc_id": {"type": "integer", "description": "Unique identifier for the ACC",
                                               "example": 12350},
                                    "guid": {"type": "string", "description": "Unique identifier for the ACC",
                                             "example": "a1b2c3d4e5f6789012345678901234ab"},
                                    "den": {"type": "string", "description": "Data Element Name",
                                            "example": "Purchase Order. Purchase Order"},
                                    "object_class_term": {"type": "string", "description": "Object class term",
                                                          "example": "Purchase Order"},
                                    "definition": {"type": ["string", "null"], "description": "Definition of the ACC",
                                                   "example": "A purchase order aggregation"},
                                    "definition_source": {"type": ["string", "null"],
                                                          "description": "Source of the definition",
                                                          "example": "https://example.com/spec"},
                                    "is_deprecated": {"type": "boolean", "description": "Whether the ACC is deprecated",
                                                      "example": False}
                                },
                                "required": ["acc_manifest_id", "acc_id", "guid", "den", "object_class_term",
                                             "is_deprecated"]
                            },
                            "definition": {"type": ["string", "null"], "description": "Definition of the ABIE",
                                           "example": "A purchase order aggregation"},
                            "remark": {"type": ["string", "null"], "description": "Remarks about the ABIE",
                                       "example": "Used for procurement"},
                            "relationships": {
                                "type": "array",
                                "description": "Ordered sequence of relationships (ASBIE/BBIE) displayed as children under the ABIE. The order is preserved from the original ABIE structure. Each relationship has an 'is_used' property: when is_used=True, the relationship is profiled for practical use (asbie_id/bbie_id exists), when is_used=False, it shows available relationships for profiling. To explore relationships: (1) For ASBIE relationships with asbie_id (is_used=True): use get_asbie_by_asbie_id(asbie_id) to get full details. (2) For ASBIE relationships without asbie_id (is_used=False): use get_asbie_by_based_ascc_manifest_id(top_level_asbiep_id, based_ascc_manifest_id) with 'based_ascc.ascc_manifest_id' for based_ascc_manifest_id. (3) For BBIE relationships with bbie_id (is_used=True): use get_bbie_by_bbie_id(bbie_id) to get full details. (4) For BBIE relationships without bbie_id (is_used=False): use get_bbie_by_based_bcc_manifest_id(top_level_asbiep_id, based_bcc_manifest_id) with 'based_bcc.bcc_manifest_id' for based_bcc_manifest_id. This includes all nested relationships from the hierarchical CC (Core Component) structure, presented as a single flat ordered list for traversal.",
                                "items": {
                                    "oneOf": [
                                        {
                                            "type": "object",
                                            "description": "ASBIE (Association Business Information Entity) relationship",
                                            "properties": {
                                                "component_type": {"type": "string",
                                                                   "description": "Type of relationship, always 'ASBIE' for this type",
                                                                   "example": "ASBIE"},
                                                "is_used": {"type": "boolean",
                                                            "description": "Whether this component is currently being used (profiled) in the BIE (True means asbie_id exists)"},
                                                "cardinality_min": {"type": "integer",
                                                                    "description": "Minimum cardinality (minimum number of occurrences required, typically 0 or 1)"},
                                                "cardinality_max": {"type": "integer",
                                                                    "description": "Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)"},
                                                "is_nillable": {"type": "boolean",
                                                                "description": "Whether the component can have a nil/null value"},
                                                "remark": {"type": ["string", "null"],
                                                           "description": "Additional remarks or notes about the component"},
                                                "cardinality_display": {"type": "string",
                                                                        "description": "Display cardinality with 'unbounded' for -1 values"},
                                                "asbie_id": {"type": ["integer", "null"],
                                                             "description": "Unique identifier for the ASBIE (base entity ID, None if is_used=False)"},
                                                "guid": {"type": ["string", "null"],
                                                         "description": "Globally unique identifier for the ASBIE (if available)"},
                                                "based_ascc": {
                                                    "type": "object",
                                                    "description": "Information about the ASCC that this ASBIE component is based on",
                                                    "properties": {
                                                        "ascc_manifest_id": {"type": "integer",
                                                                             "description": "Unique identifier for the ASCC manifest. This can be passed to create_asbie() as the based_ascc_manifest_id parameter."},
                                                        "ascc_id": {"type": "integer",
                                                                    "description": "Unique identifier for the ASCC"},
                                                        "guid": {"type": "string",
                                                                 "description": "Unique identifier within the release"},
                                                        "den": {"type": "string",
                                                                "description": "Dictionary Entry Name (DEN)"},
                                                        "cardinality_min": {"type": "integer",
                                                                            "description": "Minimum cardinality"},
                                                        "cardinality_max": {"type": "integer",
                                                                            "description": "Maximum cardinality"},
                                                        "is_deprecated": {"type": "boolean",
                                                                          "description": "Whether the ASCC is deprecated"},
                                                        "definition": {"type": ["string", "null"],
                                                                       "description": "Definition or description of the ASCC"},
                                                        "definition_source": {"type": ["string", "null"],
                                                                              "description": "URL indicating the source of the definition"},
                                                        "from_acc_manifest_id": {"type": "integer",
                                                                                 "description": "Unique identifier for the source ACC manifest"},
                                                        "to_asccp_manifest_id": {"type": "integer",
                                                                                 "description": "Unique identifier for the target ASCCP manifest"}
                                                    },
                                                    "required": ["ascc_manifest_id", "ascc_id", "guid", "den",
                                                                 "cardinality_min", "cardinality_max", "is_deprecated",
                                                                 "from_acc_manifest_id", "to_asccp_manifest_id"]
                                                },
                                                "to_asbiep_id": {"type": ["integer", "null"],
                                                                 "description": "Unique identifier for the target ASBIEP that this ASBIE connects to (if available)"}
                                            },
                                            "required": ["component_type", "is_used",
                                                         "cardinality_min", "cardinality_max", "is_nillable", "remark",
                                                         "cardinality_display", "based_ascc"]
                                        },
                                        {
                                            "type": "object",
                                            "description": "BBIE (Basic Business Information Entity) relationship",
                                            "properties": {
                                                "component_type": {"type": "string",
                                                                   "description": "Type of relationship, always 'BBIE' for this type",
                                                                   "example": "BBIE"},
                                                "is_used": {"type": "boolean",
                                                            "description": "Whether this component is currently being used (profiled) in the BIE (True means bbie_id exists)"},
                                                "cardinality_min": {"type": "integer",
                                                                    "description": "Minimum cardinality (minimum number of occurrences required, typically 0 or 1)"},
                                                "cardinality_max": {"type": "integer",
                                                                    "description": "Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)"},
                                                "is_nillable": {"type": "boolean",
                                                                "description": "Whether the component can have a nil/null value"},
                                                "remark": {"type": ["string", "null"],
                                                           "description": "Additional remarks or notes about the component"},
                                                "cardinality_display": {"type": "string",
                                                                        "description": "Display cardinality with 'unbounded' for -1 values"},
                                                "bbie_id": {"type": ["integer", "null"],
                                                            "description": "Unique identifier for the BBIE (base entity ID, None if is_used=False)"},
                                                "guid": {"type": ["string", "null"],
                                                         "description": "Globally unique identifier for the BBIE (if available)"},
                                                "based_bcc": {
                                                    "type": "object",
                                                    "description": "Information about the BCC that this BBIE component is based on",
                                                    "properties": {
                                                        "bcc_manifest_id": {"type": "integer",
                                                                            "description": "Unique identifier for the BCC manifest. This can be passed to create_bbie() as the based_bcc_manifest_id parameter."},
                                                        "bcc_id": {"type": "integer",
                                                                   "description": "Unique identifier for the BCC"},
                                                        "guid": {"type": "string",
                                                                 "description": "Unique identifier within the release"},
                                                        "den": {"type": "string",
                                                                "description": "Dictionary Entry Name (DEN)"},
                                                        "cardinality_min": {"type": "integer",
                                                                            "description": "Minimum cardinality"},
                                                        "cardinality_max": {"type": "integer",
                                                                            "description": "Maximum cardinality"},
                                                        "entity_type": {"type": ["string", "null"],
                                                                        "description": "Entity type: 'Attribute' (XML attribute) or 'Element' (XML element)"},
                                                        "is_nillable": {"type": "boolean",
                                                                        "description": "Whether the BCC can have a nil/null value"},
                                                        "is_deprecated": {"type": "boolean",
                                                                          "description": "Whether the BCC is deprecated"},
                                                        "definition": {"type": ["string", "null"],
                                                                       "description": "Definition or description of the BCC"},
                                                        "definition_source": {"type": ["string", "null"],
                                                                              "description": "URL indicating the source of the definition"},
                                                        "from_acc_manifest_id": {"type": "integer",
                                                                                 "description": "Unique identifier for the source ACC manifest"},
                                                        "to_bccp_manifest_id": {"type": "integer",
                                                                                "description": "Unique identifier for the target BCCP manifest"}
                                                    },
                                                    "required": ["bcc_manifest_id", "bcc_id", "guid", "den",
                                                                 "cardinality_min", "cardinality_max", "is_nillable",
                                                                 "is_deprecated", "from_acc_manifest_id",
                                                                 "to_bccp_manifest_id"]
                                                },
                                                "primitiveRestriction": {
                                                    "type": "object",
                                                    "description": "Primitive restriction information for the BBIE. This field is required and must not be None. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set (not multiple, not none).",
                                                    "properties": {
                                                        "xbtManifestId": {"type": ["integer", "null"],
                                                                          "description": "XBT (eXtended Built-in Type) manifest ID. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set."},
                                                        "codeListManifestId": {"type": ["integer", "null"],
                                                                               "description": "Code list manifest ID. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set."},
                                                        "agencyIdListManifestId": {"type": ["integer", "null"],
                                                                                   "description": "Agency ID list manifest ID. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set."}
                                                    },
                                                    "required": ["xbtManifestId", "codeListManifestId",
                                                                 "agencyIdListManifestId"]
                                                },
                                                "valueConstraint": {
                                                    "type": ["object", "null"],
                                                    "description": "Value constraint information for the BBIE",
                                                    "properties": {
                                                        "default_value": {"type": ["string", "null"],
                                                                          "description": "Default value for the BBIE if not specified"},
                                                        "fixed_value": {"type": ["string", "null"],
                                                                        "description": "Fixed value that must always be used for this BBIE (cannot be changed)"}
                                                    },
                                                    "required": ["default_value", "fixed_value"]
                                                },
                                                "facet": {
                                                    "type": ["object", "null"],
                                                    "description": "Facet restriction information for string values",
                                                    "properties": {
                                                        "facet_min_length": {"type": ["integer", "null"],
                                                                             "description": "Minimum length constraint for string values (facet restriction)"},
                                                        "facet_max_length": {"type": ["integer", "null"],
                                                                             "description": "Maximum length constraint for string values (facet restriction)"},
                                                        "facet_pattern": {"type": ["string", "null"],
                                                                          "description": "Pattern constraint (regular expression) for string values (facet restriction)"}
                                                    },
                                                    "required": ["facet_min_length", "facet_max_length",
                                                                 "facet_pattern"]
                                                },
                                                "to_bbiep_id": {"type": ["integer", "null"],
                                                                "description": "Unique identifier for the target BBIEP that this BBIE connects to (if available)"}
                                            },
                                            "required": ["component_type", "is_used",
                                                         "cardinality_min", "cardinality_max", "is_nillable", "remark",
                                                         "cardinality_display", "based_bcc"]
                                        }
                                    ]
                                },
                                "example": []
                            },
                            "created": {
                                "type": "object",
                                "description": "Creation information",
                                "properties": {
                                    "who": {
                                        "type": "object",
                                        "description": "User who created the ABIE",
                                        "properties": {
                                            "user_id": {"type": "integer",
                                                        "description": "Unique identifier for the user",
                                                        "example": 100000001},
                                            "login_id": {"type": "string", "description": "User's login identifier",
                                                         "example": "john.doe"},
                                            "username": {"type": "string", "description": "Display name of the user",
                                                         "example": "John Doe"},
                                            "roles": {"type": "array", "items": {"type": "string"},
                                                      "description": "List of roles assigned to the user",
                                                      "example": ["End-User"]}
                                        },
                                        "required": ["user_id", "login_id", "username", "roles"]
                                    },
                                    "when": {"type": "string", "format": "date-time",
                                             "description": "Timestamp when the ABIE was created",
                                             "example": "2023-01-15T10:30:00Z"}
                                },
                                "required": ["who", "when"]
                            },
                            "last_updated": {
                                "type": "object",
                                "description": "Last update information",
                                "properties": {
                                    "who": {
                                        "type": "object",
                                        "description": "User who last updated the ABIE",
                                        "properties": {
                                            "user_id": {"type": "integer",
                                                        "description": "Unique identifier for the user",
                                                        "example": 100000001},
                                            "login_id": {"type": "string", "description": "User's login identifier",
                                                         "example": "john.doe"},
                                            "username": {"type": "string", "description": "Display name of the user",
                                                         "example": "John Doe"},
                                            "roles": {"type": "array", "items": {"type": "string"},
                                                      "description": "List of roles assigned to the user",
                                                      "example": ["End-User"]}
                                        },
                                        "required": ["user_id", "login_id", "username", "roles"]
                                    },
                                    "when": {"type": "string", "format": "date-time",
                                             "description": "Timestamp when the ABIE was last updated",
                                             "example": "2023-01-15T14:45:00Z"}
                                },
                                "required": ["who", "when"]
                            }
                        },
                        "required": ["based_acc_manifest", "created", "last_updated"]
                    },
                    "definition": {"type": ["string", "null"], "description": "Definition of the ASBIEP",
                                   "example": "A purchase order property"},
                    "remark": {"type": ["string", "null"], "description": "Remarks about the ASBIEP",
                               "example": "Used for procurement processes"},
                    "biz_term": {"type": ["string", "null"], "description": "Business term", "example": "PO"},
                    "display_name": {"type": ["string", "null"], "description": "Display name of the ASBIEP",
                                     "example": "Purchase Order Property"},
                    "created": {
                        "type": "object",
                        "description": "Creation information",
                        "properties": {
                            "who": {
                                "type": "object",
                                "description": "User who created the ASBIEP",
                                "properties": {
                                    "user_id": {"type": "integer", "description": "Unique identifier for the user",
                                                "example": 100000001},
                                    "login_id": {"type": "string", "description": "User's login identifier",
                                                 "example": "john.doe"},
                                    "username": {"type": "string", "description": "Display name of the user",
                                                 "example": "John Doe"},
                                    "roles": {"type": "array", "items": {"type": "string"},
                                              "description": "List of roles assigned to the user",
                                              "example": ["End-User"]}
                                },
                                "required": ["user_id", "login_id", "username", "roles"]
                            },
                            "when": {"type": "string", "format": "date-time",
                                     "description": "Timestamp when the ASBIEP was created",
                                     "example": "2023-01-15T10:30:00Z"}
                        },
                        "required": ["who", "when"]
                    },
                    "last_updated": {
                        "type": "object",
                        "description": "Last update information",
                        "properties": {
                            "who": {
                                "type": "object",
                                "description": "User who last updated the ASBIEP",
                                "properties": {
                                    "user_id": {"type": "integer", "description": "Unique identifier for the user",
                                                "example": 100000001},
                                    "login_id": {"type": "string", "description": "User's login identifier",
                                                 "example": "john.doe"},
                                    "username": {"type": "string", "description": "Display name of the user",
                                                 "example": "John Doe"},
                                    "roles": {"type": "array", "items": {"type": "string"},
                                              "description": "List of roles assigned to the user",
                                              "example": ["End-User"]}
                                },
                                "required": ["user_id", "login_id", "username", "roles"]
                            },
                            "when": {"type": "string", "format": "date-time",
                                     "description": "Timestamp when the ASBIEP was last updated",
                                     "example": "2023-01-15T14:45:00Z"}
                        },
                        "required": ["who", "when"]
                    }
                },
                "required": ["asbiep_id", "owner_top_level_asbiep", "based_asccp_manifest", "role_of_abie", "created",
                             "last_updated"]
            },
            "version": {"type": ["string", "null"], "description": "Version number assigned by the user",
                        "example": "1.0"},
            "status": {"type": ["string", "null"],
                       "description": "Usage status of the top-level ASBIEP (e.g., 'Prototype', 'Test', 'Production')",
                       "example": "Production"},
            "business_contexts": {
                "type": "array",
                "description": "List of associated business contexts",
                "items": {
                    "type": "object",
                    "properties": {
                        "biz_ctx_id": {"type": "integer", "description": "Unique identifier for the business context",
                                       "example": 1},
                        "guid": {"type": "string", "description": "Unique identifier for the business context",
                                 "example": "a1b2c3d4e5f6789012345678901234ab"},
                        "name": {"type": ["string", "null"], "description": "Name of the business context",
                                 "example": "Production Environment"}
                    },
                    "required": ["biz_ctx_id", "guid"]
                }
            },
            "state": {"type": "string", "description": "State of the top-level ASBIEP", "example": "Published"},
            "is_deprecated": {"type": "boolean", "description": "Whether the top-level ASBIEP is deprecated",
                              "example": False},
            "deprecated_reason": {"type": ["string", "null"], "description": "The reason for the deprecation",
                                  "example": "Replaced by new version"},
            "deprecated_remark": {"type": ["string", "null"], "description": "Additional remarks about the deprecation",
                                  "example": "Use version 2.0 instead"},
            "owner": {
                "type": "object",
                "description": "Owner information",
                "properties": {
                    "user_id": {"type": "integer", "description": "Unique identifier for the user", "example": 1},
                    "login_id": {"type": "string", "description": "User's login identifier", "example": "admin"},
                    "username": {"type": "string", "description": "Display name of the user",
                                 "example": "Administrator"},
                    "roles": {"type": "array", "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                              "description": "List of roles assigned to the user", "example": ["Admin"]}
                },
                "required": ["user_id", "login_id", "username", "roles"]
            },
            "created": {
                "type": "object",
                "description": "Information about the creation of the BIE (Business Information Entity)",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who created the BIE (Business Information Entity)",
                        "properties": {
                            "user_id": {"type": "integer", "description": "Unique identifier for the user",
                                        "example": 1},
                            "login_id": {"type": "string", "description": "User's login identifier",
                                         "example": "admin"},
                            "username": {"type": "string", "description": "Display name of the user",
                                         "example": "Administrator"},
                            "roles": {"type": "array",
                                      "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                      "description": "List of roles assigned to the user", "example": ["Admin"]}
                        },
                        "required": ["user_id", "login_id", "username", "roles"]
                    },
                    "when": {"type": "string", "format": "date-time",
                             "description": "Creation timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)",
                             "example": "2024-01-15T10:30:00Z"}
                },
                "required": ["who", "when"]
            },
            "last_updated": {
                "type": "object",
                "description": "Information about the last update of the BIE (Business Information Entity)",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who last updated the BIE (Business Information Entity)",
                        "properties": {
                            "user_id": {"type": "integer", "description": "Unique identifier for the user",
                                        "example": 1},
                            "login_id": {"type": "string", "description": "User's login identifier",
                                         "example": "admin"},
                            "username": {"type": "string", "description": "Display name of the user",
                                         "example": "Administrator"},
                            "roles": {"type": "array",
                                      "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                      "description": "List of roles assigned to the user", "example": ["Admin"]}
                        },
                        "required": ["user_id", "login_id", "username", "roles"]
                    },
                    "when": {"type": "string", "format": "date-time",
                             "description": "Last update timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)",
                             "example": "2024-01-15T10:30:00Z"}
                },
                "required": ["who", "when"]
            }
        },
        "required": ["top_level_asbiep_id", "asbiep", "business_contexts", "state",
                     "is_deprecated", "owner", "created", "last_updated"]
    }
)
async def get_top_level_asbiep(
    top_level_asbiep_id: Annotated[int, Field(gt=0, description="Unique identifier of the top-level ASBIEP to retrieve.")],
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> GetTopLevelAsbiepToolResponse:
    """
    Get a top-level ASBIEP (Association Business Information Entity Property) by its ID.

    This is the first step to access and traverse the BIE (Business Information Entity) structure.
    The response displays the ASBIEP first, with its relationships (ASBIE/BBIE) shown as children
    under the role_of_abie section. This function is used for profiling core components.

    Profiling Concept:
    - When is_used=True: The relationship is profiled for practical use (asbie_id/bbie_id exists)
    - When is_used=False: Shows all available relationships for profiling
    - To see only profiled BIEs: filter for is_used=True relationships
    - To profile a BIE: shows all relationships (both is_used=True and is_used=False) for selection

    Example:
    When profiling 'Address' ASCCP and wanting to use 'Identifier', 'Street Name', 'Floor',
    'Unit', 'Country Code', 'Postal Code', all these ASBIEs/BBIEs will be created with is_used=True.
    Other available relationships will show with is_used=False for potential profiling.

    Exploring Relationships:
    The relationships array is an ordered sequence containing ASBIE and BBIE components that preserves the original order from the ABIE structure. To explore these relationships further:

    **For ASBIE Relationships:**
    1. If the relationship has an asbie_id (is_used=True):
       - Use: get_asbie_by_asbie_id(asbie_id=relationship.asbie_id)
       - This retrieves the complete ASBIE information from the database
       - Example: If relationship.asbie_id = 12345, call get_asbie_by_asbie_id(asbie_id=12345)

    2. If the relationship has no asbie_id (is_used=False):
       - Use: get_asbie_by_based_ascc_manifest_id(
                 top_level_asbiep_id=top_level_asbiep_id,
                 parent_abie_path=relationship.path.split('>ASCC-')[0],
                 based_ascc_manifest_id=relationship.based_ascc.ascc_manifest_id
             )
       - This returns basic information about the ASBIE structure before it's profiled
       - Use relationship.path for constructing parent_abie_path (remove the '>ASCC-{id}' suffix)
       - Use relationship.based_ascc.ascc_manifest_id for based_ascc_manifest_id

    **For BBIE Relationships:**
    1. If the relationship has a bbie_id (is_used=True):
       - Use: get_bbie_by_bbie_id(bbie_id=relationship.bbie_id)
       - This retrieves the complete BBIE information from the database
       - Example: If relationship.bbie_id = 9876, call get_bbie_by_bbie_id(bbie_id=9876)
       - Note: The relationship.primitiveRestriction field is required and must not be None. It contains exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId.

    2. If the relationship has no bbie_id (is_used=False):
       - Use: get_bbie_by_based_bcc_manifest_id(
                 top_level_asbiep_id=top_level_asbiep_id,
                 parent_abie_path=relationship.path.split('>BCC-')[0],
                 based_bcc_manifest_id=relationship.based_bcc.bcc_manifest_id
             )
       - Note: The relationship.primitiveRestriction field is required and must not be None, even when no BBIE exists yet. It is fetched from DtAwdPri and contains exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId.
       - This returns basic information about the BBIE structure before it's profiled
       - Use relationship.path for constructing parent_abie_path (remove the '>BCC-{id}' suffix)
       - Use relationship.based_bcc.bcc_manifest_id for based_bcc_manifest_id

    BIE Structure Traversal:
    The BIE structure follows this hierarchy that can be traversed step by step:
    - top_level_asbiep -> ASBIEP (Association Business Information Entity Property) -> ABIE (Aggregation Business Information Entity) -> relationships (ASBIE/BBIE)
    - Each relationship can be explored using the appropriate get function based on whether it has an ID
    - ASBIE relationships lead to nested ASBIEP -> ABIE structures (recursively)
    - BBIE relationships lead to BBIEP -> BBIE_SC (Basic Business Information Entity Simple Content) structures

    Key Differences from CC (Core Component) Structure:
    - CC Structure: ACC (Aggregation Core Component) has base ACC recursively, each ACC has its own relationships
    - BIE Structure: ABIE (Aggregation Business Information Entity) has flat relationships, all combined in a single ordered list (flattened form)
    - The BIE structure provides a flattened view where all nested relationships from the CC hierarchy
      are presented as a single flat ordered list, preserving the original sequence, making it easier to work with in business contexts.

    Using with Create Tools:
    This tool provides all the information needed to use create_asbie, create_bbie, and create_bbie_sc tools.
    IMPORTANT: Groups (component_type 3=SemanticGroup or 4=UserExtensionGroup) can be skipped in BIE expressions.
    The actual BIE structure may differ from the CC structure because groups are flattened. To find the correct
    from_abie_id for create_asbie/create_bbie, you must traverse the actual BIE structure recursively using
    get_top_level_asbiep, get_asbie_*, and get_bbie_* to find the abie_id in the BIE structure (not the CC structure).

    **For create_asbie:**
    - from_abie_id: Use role_of_abie.abie_id from this response, or traverse recursively: if an ASBIE relationship
      exists (has asbie_id), use get_asbie_by_asbie_id() to get its to_asbiep.role_of_abie.abie_id. If no ASBIE
      exists yet, the parent ABIE's abie_id from the current level should be used. Groups are automatically skipped
      in the BIE structure, so the abie_id reflects the actual BIE hierarchy.
    - based_ascc_manifest_id: Use role_of_abie.relationships[].based_ascc.ascc_manifest_id from any ASBIE relationship
      in the relationships array. This value can be passed directly to create_asbie() as the based_ascc_manifest_id parameter.
      Note: The ASCC's from_acc may be a group in the CC structure, but in the BIE structure, the relationship appears
      directly under the parent ABIE (skipping the group).
    - Example: Use abie_id and based_ascc.ascc_manifest_id directly from this response:
      create_asbie(from_abie_id=result.asbiep.role_of_abie.abie_id, based_ascc_manifest_id=result.asbiep.role_of_abie.relationships[0].based_ascc.ascc_manifest_id)

    **For create_bbie:**
    - from_abie_id: Use role_of_abie.abie_id from this response, or traverse recursively: if an ASBIE relationship
      exists (has asbie_id), use get_asbie_by_asbie_id() to get its to_asbiep.role_of_abie.abie_id. If no ASBIE
      exists yet, the parent ABIE's abie_id from the current level should be used. Groups are automatically skipped
      in the BIE structure, so the abie_id reflects the actual BIE hierarchy.
    - based_bcc_manifest_id: Use role_of_abie.relationships[].based_bcc.bcc_manifest_id from any BBIE relationship
      in the relationships array. This value can be passed directly to create_bbie() as the based_bcc_manifest_id parameter.
      Note: The BCC's from_acc may be a group in the CC structure, but in the BIE structure, the relationship appears
      directly under the parent ABIE (skipping the group).
    - Example: Use abie_id and based_bcc.bcc_manifest_id directly from this response:
      create_bbie(from_abie_id=result.asbiep.role_of_abie.abie_id, based_bcc_manifest_id=result.asbiep.role_of_abie.relationships[0].based_bcc.bcc_manifest_id)

    **For create_bbie_sc:**
    - First use get_bbie_by_bbie_id() or get_bbie_by_based_bcc_manifest_id() to get BBIE details
    - bbie_id: Use the bbie_id from the BBIE response
    - based_dt_sc_manifest_id: Use to_bbiep.supplementary_components[].based_dt_sc.dt_sc_manifest_id from the BBIE response

    Args:
        top_level_asbiep_id (int): The unique identifier of the top-level ASBIEP (Association Business Information Entity Property).
            Must be a positive integer.

    Returns:
        GetTopLevelAsbiepResponse: Basic information about the ASBIEP including:
            - ASBIEP properties (ID, definition, remark, business term, display name)
            - Owner top-level ASBIEP information (library, release, version, status, state, deprecation info)
            - Based ASCCP (Association Core Component Property) manifest information
            - Role of ABIE (Aggregation Business Information Entity) information with relationships list for traversal
            - Each relationship includes:
              * is_used property indicating profiling status
              * asbie_id/bbie_id if profiled (is_used=True), None otherwise
              * based_ascc/based_bcc information with manifest IDs
            - Audit information (created by, last updated by, timestamps)

    Raises:
        ToolError: If the ASBIEP is not found, the ID is invalid, or a database error occurs.

    Examples:
        Get a top-level ASBIEP and explore its associations:
        >>> result = get_top_level_asbiep(top_level_asbiep_id=12345)
        >>> print(f"ASBIEP ID: {result.asbiep.asbiep_id}")
        >>> print(f"Owner: {result.asbiep.owner_top_level_asbiep.owner.username}")
        >>> print(f"Definition: {result.asbiep.definition}")
        >>> print(f"Relationships count: {len(result.asbiep.role_of_abie.relationships)}")

        # Explore a profiled ASBIE relationship (has asbie_id)
        >>> asbie_rel = [r for r in result.asbiep.role_of_abie.relationships
        ...              if hasattr(r, 'asbie_id') and r.asbie_id][0]
        >>> asbie_detail = get_asbie_by_asbie_id(asbie_id=asbie_rel.asbie_id)

        # Explore an unprofiled ASBIE relationship (no asbie_id)
        >>> asbie_rel_unprofiled = [r for r in result.asbiep.role_of_abie.relationships
        ...                         if hasattr(r, 'asbie_id') and not r.asbie_id][0]
        >>> parent_path = asbie_rel_unprofiled.path.split('>ASCC-')[0]
        >>> asbie_detail = get_asbie_by_based_ascc_manifest_id(
        ...     top_level_asbiep_id=12345,
        ...     parent_abie_path=parent_path,
        ...     based_ascc_manifest_id=asbie_rel_unprofiled.based_ascc.ascc_manifest_id
        ... )

        # Explore a profiled BBIE relationship (has bbie_id)
        >>> bbie_rel = [r for r in result.asbiep.role_of_abie.relationships
        ...             if hasattr(r, 'bbie_id') and r.bbie_id][0]
        >>> bbie_detail = get_bbie_by_bbie_id(bbie_id=bbie_rel.bbie_id)

        # Explore an unprofiled BBIE relationship (no bbie_id)
        >>> bbie_rel_unprofiled = [r for r in result.asbiep.role_of_abie.relationships
        ...                        if hasattr(r, 'bbie_id') and not r.bbie_id][0]
        >>> parent_path = bbie_rel_unprofiled.path.split('>BCC-')[0]
        >>> bbie_detail = get_bbie_by_based_bcc_manifest_id(
        ...     top_level_asbiep_id=12345,
        ...     parent_abie_path=parent_path,
        ...     based_bcc_manifest_id=bbie_rel_unprofiled.based_bcc.bcc_manifest_id
        ... )
    """
    try:
        payload = await business_information_entity_service.get_top_level_asbiep(
            top_level_asbiep_id=top_level_asbiep_id,
        )
        if payload is None:
            raise ToolError(
                f"The top-level ASBIEP with ID {top_level_asbiep_id} was not found. Please check the ID and try again."
            )
        return GetTopLevelAsbiepToolResponse.model_validate(payload, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to retrieve top-level ASBIEP {top_level_asbiep_id}.") from exc


@mcp.tool(
    name="get_asbie_by_asbie_id",
    description="Get an ASBIE (Association Business Information Entity) by its ASBIE ID. This function fetches the complete ASBIE information from the database when you have the asbie_id. IMPORTANT: Groups (component_type 3=SemanticGroup or 4=UserExtensionGroup) are automatically skipped in BIE expressions, so the BIE structure may differ from the CC structure. To find the correct from_abie_id for create_asbie, traverse the actual BIE structure recursively using get_top_level_asbiep, get_asbie_*, and get_bbie_* to find the abie_id in the BIE structure.",
    output_schema={
        "type": "object",
        "description": "Response containing ASBIE (Association Business Information Entity) information with its ASBIEP. Used for profiling core components – when is_used=True, the component is profiled for practical use (asbie_id will be created); when is_used=False, it shows all available components for profiling. IMPORTANT: Groups (component_type 3=SemanticGroup or 4=UserExtensionGroup) are automatically skipped in BIE expressions, so the BIE structure may differ from the CC structure. To find the correct from_abie_id for create_asbie, traverse the actual BIE structure recursively.",
        "properties": {
            "asbie_id": {"type": ["integer", "null"],
                         "description": "Unique identifier for the ASBIE (base entity ID, None if not yet created)",
                         "example": 12345},
            "owner_top_level_asbiep": {
                "type": "object",
                "description": "Top-Level ASBIEP information",
                "properties": {
                    "top_level_asbiep_id": {"type": "integer",
                                            "description": "Unique identifier for the top-level ASBIEP",
                                            "example": 12345},
                    "library": {
                        "type": "object",
                        "description": "Library information",
                        "properties": {
                            "library_id": {"type": "integer", "description": "Unique identifier for the library",
                                           "example": 1},
                            "name": {"type": "string", "description": "Library name", "example": "OAGIS"}
                        },
                        "required": ["library_id", "name"]
                    },
                    "release": {
                        "type": "object",
                        "description": "Release information",
                        "properties": {
                            "release_id": {"type": "integer", "description": "Unique identifier for the release",
                                           "example": 1},
                            "release_num": {"type": "string", "description": "Release number",
                                            "example": "10.6"},
                            "state": {"type": "string", "description": "Release state", "example": "Published"}
                        },
                        "required": ["release_id", "release_num", "state"]
                    },
                    "version": {"type": ["string", "null"], "description": "Version number", "example": "1.0"},
                    "status": {"type": ["string", "null"], "description": "Status of the top-level ASBIEP",
                               "example": "Production"},
                    "state": {"type": "string", "description": "State of the top-level ASBIEP", "example": "Published"},
                    "is_deprecated": {"type": "boolean", "description": "Whether the top-level ASBIEP is deprecated",
                                      "example": False},
                    "deprecated_reason": {"type": ["string", "null"], "description": "Reason for deprecation",
                                          "example": "Replaced by newer version"},
                    "deprecated_remark": {"type": ["string", "null"],
                                          "description": "Additional remarks about deprecation",
                                          "example": "Use version 2.0 instead"},
                    "owner": {
                        "type": "object",
                        "description": "Owner information",
                        "properties": {
                            "user_id": {"type": "integer", "description": "Unique identifier for the user",
                                        "example": 100000001},
                            "login_id": {"type": "string", "description": "User's login identifier",
                                         "example": "john.doe"},
                            "username": {"type": "string", "description": "Display name of the user",
                                         "example": "John Doe"},
                            "roles": {"type": "array",
                                      "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                      "description": "List of roles assigned to the user", "example": ["End-User"]}
                        },
                        "required": ["user_id", "login_id", "username", "roles"]
                    }
                },
                "required": ["top_level_asbiep_id", "library", "release", "state", "is_deprecated", "owner"]
            },
            "guid": {"type": ["string", "null"],
                     "description": "Globally unique identifier for the ASBIE (if available)",
                     "example": "a1b2c3d4e5f6789012345678901234ab"},
            "based_ascc": {
                "type": "object",
                "description": "ASCC (Association Core Component) information that this ASBIE is based on",
                "properties": {
                    "ascc_manifest_id": {"type": "integer",
                                         "description": "Unique identifier for the ASCC manifest. This can be passed to create_asbie() as the based_ascc_manifest_id parameter.",
                                         "example": 12345},
                    "ascc_id": {"type": "integer", "description": "Unique identifier for the ASCC", "example": 6789},
                    "guid": {"type": "string", "description": "Unique identifier within the release",
                             "example": "a1b2c3d4e5f6789012345678901234ab"},
                    "den": {"type": "string", "description": "Dictionary Entry Name (DEN)",
                            "example": "Purchase Order. Details"},
                    "cardinality_min": {"type": "integer", "description": "Minimum cardinality", "example": 0},
                    "cardinality_max": {"type": "integer", "description": "Maximum cardinality", "example": 1},
                    "is_deprecated": {"type": "boolean", "description": "Whether the ASCC is deprecated",
                                      "example": False},
                    "definition": {"type": ["string", "null"], "description": "Definition of the ASCC",
                                   "example": "Details of the purchase order"},
                    "definition_source": {"type": ["string", "null"], "description": "Source of the definition",
                                          "example": "https://example.com/spec"},
                    "from_acc_manifest_id": {"type": "integer", "description": "Source ACC manifest ID",
                                             "example": 12346},
                    "to_asccp_manifest_id": {"type": "integer", "description": "Target ASCCP manifest ID",
                                             "example": 12347}
                },
                "required": ["ascc_manifest_id", "ascc_id", "guid", "den", "cardinality_min", "cardinality_max",
                             "is_deprecated", "from_acc_manifest_id", "to_asccp_manifest_id"]
            },
            "to_asbiep": {
                "type": "object",
                "description": "ASBIEP (Association Business Information Entity Property) information",
                "properties": {
                    "asbiep_id": {"type": ["integer", "null"], "description": "Unique identifier for the ASBIEP",
                                  "example": 12348},
                    "owner_top_level_asbiep": {
                        "type": ["object", "null"],
                        "description": "Top-Level ASBIEP information",
                        "properties": {
                            "top_level_asbiep_id": {"type": "integer",
                                                    "description": "Unique identifier for the top-level ASBIEP",
                                                    "example": 12345},
                            "library": {
                                "type": "object",
                                "description": "Library information",
                                "properties": {
                                    "library_id": {"type": "integer",
                                                   "description": "Unique identifier for the library", "example": 1},
                                    "name": {"type": "string", "description": "Library name", "example": "OAGIS"}
                                },
                                "required": ["library_id", "name"]
                            },
                            "release": {
                                "type": "object",
                                "description": "Release information",
                                "properties": {
                                    "release_id": {"type": "integer",
                                                   "description": "Unique identifier for the release", "example": 1},
                                    "release_num": {"type": "string", "description": "Release number",
                                                    "example": "10.6"},
                                    "state": {"type": "string", "description": "Release state", "example": "Published"}
                                },
                                "required": ["release_id", "release_num", "state"]
                            },
                            "version": {"type": ["string", "null"], "description": "Version number", "example": "1.0"},
                            "status": {"type": ["string", "null"], "description": "Status", "example": "Production"},
                            "state": {"type": "string", "description": "State", "example": "Published"},
                            "is_deprecated": {"type": "boolean", "description": "Whether deprecated", "example": False},
                            "deprecated_reason": {"type": ["string", "null"], "description": "Reason for deprecation",
                                                  "example": None},
                            "deprecated_remark": {"type": ["string", "null"],
                                                  "description": "Remarks about deprecation", "example": None},
                            "owner": {
                                "type": "object",
                                "description": "Owner information",
                                "properties": {
                                    "user_id": {"type": "integer", "description": "Unique identifier for the user",
                                                "example": 100000001},
                                    "login_id": {"type": "string", "description": "User's login identifier",
                                                 "example": "john.doe"},
                                    "username": {"type": "string", "description": "Display name of the user",
                                                 "example": "John Doe"},
                                    "roles": {"type": "array",
                                              "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                              "description": "List of roles assigned to the user",
                                              "example": ["End-User"]}
                                },
                                "required": ["user_id", "login_id", "username", "roles"]
                            }
                        },
                        "required": ["top_level_asbiep_id", "library", "release", "state", "is_deprecated", "owner"]
                    },
                    "based_asccp_manifest": {
                        "type": "object",
                        "description": "ASCCP information that this ASBIEP is based on",
                        "properties": {
                            "asccp_manifest_id": {"type": "integer",
                                                  "description": "Unique identifier for the ASCCP manifest",
                                                  "example": 12347},
                            "asccp_id": {"type": "integer", "description": "Unique identifier for the ASCCP",
                                         "example": 6789},
                            "role_of_acc_manifest_id": {"type": "integer", "description": "Role of ACC manifest ID",
                                                        "example": 12349},
                            "guid": {"type": "string", "description": "Unique identifier within the release",
                                     "example": "a1b2c3d4e5f6789012345678901234ab"},
                            "den": {"type": ["string", "null"], "description": "Data Element Name",
                                    "example": "Purchase Order. Details"},
                            "property_term": {"type": ["string", "null"], "description": "Property term",
                                              "example": "Details"},
                            "definition": {"type": ["string", "null"], "description": "Definition of the ASCCP",
                                           "example": "Details of the purchase order"},
                            "definition_source": {"type": ["string", "null"], "description": "Source of the definition",
                                                  "example": "https://example.com/spec"},
                            "is_deprecated": {"type": "boolean", "description": "Whether the ASCCP is deprecated",
                                              "example": False}
                        },
                        "required": ["asccp_manifest_id", "asccp_id", "role_of_acc_manifest_id", "guid", "den",
                                     "property_term", "is_deprecated"]
                    },
                    "role_of_abie": {
                        "type": "object",
                        "description": "Role of ABIE information",
                        "properties": {
                            "abie_id": {"type": ["integer", "null"],
                                        "description": "Unique identifier for the ABIE. This can be passed to create_asbie() and create_bbie() as the from_abie_id parameter.",
                                        "example": 12350},
                            "guid": {"type": ["string", "null"], "description": "Unique identifier for the ABIE",
                                     "example": "a1b2c3d4e5f6789012345678901234ab"},
                            "based_acc_manifest": {
                                "type": "object",
                                "description": "Based ACC manifest information",
                                "properties": {
                                    "acc_manifest_id": {"type": "integer",
                                                        "description": "Unique identifier for the ACC manifest",
                                                        "example": 12351},
                                    "acc_id": {"type": "integer", "description": "Unique identifier for the ACC",
                                               "example": 7890},
                                    "guid": {"type": "string", "description": "Unique identifier for the ACC",
                                             "example": "a1b2c3d4e5f6789012345678901234ab"},
                                    "den": {"type": "string", "description": "Data Element Name",
                                            "example": "Purchase Order. Purchase Order"},
                                    "object_class_term": {"type": "string", "description": "Object class term",
                                                          "example": "Purchase Order"},
                                    "definition": {"type": ["string", "null"], "description": "Definition of the ACC",
                                                   "example": "A purchase order aggregation"},
                                    "definition_source": {"type": ["string", "null"],
                                                          "description": "Source of the definition",
                                                          "example": "https://example.com/spec"},
                                    "is_deprecated": {"type": "boolean", "description": "Whether the ACC is deprecated",
                                                      "example": False}
                                },
                                "required": ["acc_manifest_id", "acc_id", "guid", "den", "object_class_term",
                                             "is_deprecated"]
                            },
                            "definition": {"type": ["string", "null"], "description": "Definition of the ABIE",
                                           "example": "A purchase order aggregation"},
                            "remark": {"type": ["string", "null"], "description": "Remarks about the ABIE",
                                       "example": "Used for procurement"},
                            "relationships": {
                                "type": "array",
                                "description": "List of relationships (ASBIE/BBIE) displayed as children under the ABIE. Each relationship has an 'is_used' property: when is_used=True, the relationship is profiled for practical use (asbie_id/bbie_id will be created), when is_used=False, it shows available relationships for profiling. These relationships can be further explored using get_asbie_by_asbie_id(), get_asbie_by_based_ascc_manifest_id(), get_bbie_by_bbie_id(), and get_bbie_by_based_bcc_manifest_id() functions. This includes all nested relationships from the hierarchical CC (Core Component) structure, presented as a single flat list for traversal.",
                                "items": {
                                    "oneOf": [
                                        {
                                            "type": "object",
                                            "description": "ASBIE (Association Business Information Entity) relationship",
                                            "properties": {
                                                "component_type": {"type": "string",
                                                                   "description": "Type of relationship, always 'ASBIE' for this type",
                                                                   "example": "ASBIE"},
                                                "asbie_id": {"type": ["integer", "null"],
                                                             "description": "Unique identifier for the ASBIE (base entity ID, None if is_used=False)",
                                                             "example": 12345},
                                                "guid": {"type": ["string", "null"],
                                                         "description": "Globally unique identifier for the ASBIE (if available)",
                                                         "example": "a1b2c3d4e5f6789012345678901234ab"},
                                                "is_used": {"type": "boolean",
                                                            "description": "Whether this component is currently being used (profiled) in the BIE (True means asbie_id exists)",
                                                            "example": True},
                                                "cardinality_min": {"type": "integer",
                                                                    "description": "Minimum cardinality (minimum number of occurrences required, typically 0 or 1)",
                                                                    "example": 0},
                                                "cardinality_max": {"type": "integer",
                                                                    "description": "Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)",
                                                                    "example": 1},
                                                "is_nillable": {"type": "boolean",
                                                                "description": "Whether the component can have a nil/null value",
                                                                "example": False},
                                                "remark": {"type": ["string", "null"],
                                                           "description": "Additional remarks or notes about the component",
                                                           "example": "Used for purchase orders"},
                                                "based_ascc": {
                                                    "type": "object",
                                                    "description": "ASCC (Association Core Component) information that this ASBIE component is based on",
                                                    "properties": {
                                                        "ascc_manifest_id": {"type": "integer",
                                                                             "description": "Unique identifier for the ASCC manifest. This can be passed to create_asbie() as the based_ascc_manifest_id parameter.",
                                                                             "example": 12345},
                                                        "ascc_id": {"type": "integer",
                                                                    "description": "Unique identifier for the ASCC",
                                                                    "example": 6789},
                                                        "guid": {"type": "string",
                                                                 "description": "Unique identifier within the release",
                                                                 "example": "a1b2c3d4e5f6789012345678901234ab"},
                                                        "den": {"type": "string",
                                                                "description": "Dictionary Entry Name (DEN)",
                                                                "example": "Purchase Order. Details"},
                                                        "cardinality_min": {"type": "integer",
                                                                            "description": "Minimum cardinality",
                                                                            "example": 0},
                                                        "cardinality_max": {"type": "integer",
                                                                            "description": "Maximum cardinality",
                                                                            "example": 1},
                                                        "is_deprecated": {"type": "boolean",
                                                                          "description": "Whether the ASCC is deprecated",
                                                                          "example": False},
                                                        "definition": {"type": ["string", "null"],
                                                                       "description": "Definition of the ASCC",
                                                                       "example": "Details of the purchase order"},
                                                        "definition_source": {"type": ["string", "null"],
                                                                              "description": "Source of the definition",
                                                                              "example": "https://example.com/spec"},
                                                        "from_acc_manifest_id": {"type": "integer",
                                                                                 "description": "Source ACC manifest ID",
                                                                                 "example": 12346},
                                                        "to_asccp_manifest_id": {"type": "integer",
                                                                                 "description": "Target ASCCP manifest ID",
                                                                                 "example": 12347}
                                                    },
                                                    "required": ["ascc_manifest_id", "ascc_id", "guid", "den",
                                                                 "cardinality_min", "cardinality_max", "is_deprecated",
                                                                 "from_acc_manifest_id", "to_asccp_manifest_id"]
                                                },
                                                "to_asbiep_id": {"type": ["integer", "null"],
                                                                 "description": "Unique identifier for the target ASBIEP that this ASBIE connects to (if available)",
                                                                 "example": 12348}
                                            },
                                            "required": ["component_type", "is_used",
                                                         "cardinality_min", "cardinality_max", "is_nillable",
                                                         "based_ascc"]
                                        },
                                        {
                                            "type": "object",
                                            "description": "BBIE (Basic Business Information Entity) relationship",
                                            "properties": {
                                                "component_type": {"type": "string",
                                                                   "description": "Type of relationship, always 'BBIE' for this type",
                                                                   "example": "BBIE"},
                                                "bbie_id": {"type": ["integer", "null"],
                                                            "description": "Unique identifier for the BBIE (base entity ID, None if is_used=False)",
                                                            "example": 12345},
                                                "guid": {"type": ["string", "null"],
                                                         "description": "Globally unique identifier for the BBIE (if available)",
                                                         "example": "a1b2c3d4e5f6789012345678901234ab"},
                                                "is_used": {"type": "boolean",
                                                            "description": "Whether this component is currently being used (profiled) in the BIE (True means bbie_id exists)",
                                                            "example": True},
                                                "cardinality_min": {"type": "integer",
                                                                    "description": "Minimum cardinality (minimum number of occurrences required, typically 0 or 1)",
                                                                    "example": 0},
                                                "cardinality_max": {"type": "integer",
                                                                    "description": "Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)",
                                                                    "example": 1},
                                                "is_nillable": {"type": "boolean",
                                                                "description": "Whether the component can have a nil/null value",
                                                                "example": False},
                                                "remark": {"type": ["string", "null"],
                                                           "description": "Additional remarks or notes about the component",
                                                           "example": "Used for purchase orders"},
                                                "based_bcc": {
                                                    "type": "object",
                                                    "description": "BCC (Basic Core Component) information that this BBIE component is based on",
                                                    "properties": {
                                                        "bcc_manifest_id": {"type": "integer",
                                                                            "description": "Unique identifier for the BCC manifest",
                                                                            "example": 12345},
                                                        "bcc_id": {"type": "integer",
                                                                   "description": "Unique identifier for the BCC",
                                                                   "example": 6789},
                                                        "guid": {"type": "string",
                                                                 "description": "Unique identifier within the release",
                                                                 "example": "a1b2c3d4e5f6789012345678901234ab"},
                                                        "den": {"type": "string",
                                                                "description": "Dictionary Entry Name (DEN)",
                                                                "example": "Purchase Order. Amount"},
                                                        "cardinality_min": {"type": "integer",
                                                                            "description": "Minimum cardinality",
                                                                            "example": 0},
                                                        "cardinality_max": {"type": "integer",
                                                                            "description": "Maximum cardinality",
                                                                            "example": 1},
                                                        "entity_type": {"type": ["string", "null"],
                                                                        "description": "Entity type: 'Attribute' or 'Element'",
                                                                        "example": "Element"},
                                                        "is_nillable": {"type": "boolean",
                                                                        "description": "Whether the BCC is nillable",
                                                                        "example": False},
                                                        "is_deprecated": {"type": "boolean",
                                                                          "description": "Whether the BCC is deprecated",
                                                                          "example": False},
                                                        "definition": {"type": ["string", "null"],
                                                                       "description": "Definition of the BCC",
                                                                       "example": "A monetary amount"},
                                                        "definition_source": {"type": ["string", "null"],
                                                                              "description": "Source of the definition",
                                                                              "example": "https://example.com/spec"},
                                                        "from_acc_manifest_id": {"type": "integer",
                                                                                 "description": "Source ACC manifest ID",
                                                                                 "example": 12346},
                                                        "to_bccp_manifest_id": {"type": "integer",
                                                                                "description": "Target BCCP manifest ID",
                                                                                "example": 12347}
                                                    },
                                                    "required": ["bcc_manifest_id", "bcc_id", "guid", "den",
                                                                 "cardinality_min", "cardinality_max", "is_nillable",
                                                                 "is_deprecated", "from_acc_manifest_id",
                                                                 "to_bccp_manifest_id"]
                                                },
                                                "primitiveRestriction": {
                                                    "type": ["object", "null"],
                                                    "description": "Primitive restriction information for the BBIE",
                                                    "properties": {
                                                        "xbtManifestId": {"type": ["integer", "null"],
                                                                          "description": "XBT (eXtended Built-in Type) manifest ID",
                                                                          "example": None},
                                                        "codeListManifestId": {"type": ["integer", "null"],
                                                                               "description": "Code list manifest ID",
                                                                               "example": None},
                                                        "agencyIdListManifestId": {"type": ["integer", "null"],
                                                                                   "description": "Agency ID list manifest ID",
                                                                                   "example": None}
                                                    },
                                                    "required": ["xbtManifestId", "codeListManifestId",
                                                                 "agencyIdListManifestId"]
                                                },
                                                "valueConstraint": {
                                                    "type": ["object", "null"],
                                                    "description": "Value constraint information for the BBIE",
                                                    "properties": {
                                                        "default_value": {"type": ["string", "null"],
                                                                          "description": "Default value for the BBIE",
                                                                          "example": None},
                                                        "fixed_value": {"type": ["string", "null"],
                                                                        "description": "Fixed value for the BBIE",
                                                                        "example": None}
                                                    },
                                                    "required": ["default_value", "fixed_value"]
                                                },
                                                "facet": {
                                                    "type": ["object", "null"],
                                                    "description": "Facet restriction information for string values",
                                                    "properties": {
                                                        "facet_min_length": {"type": ["integer", "null"],
                                                                             "description": "Minimum length constraint for string values (facet restriction)",
                                                                             "example": None},
                                                        "facet_max_length": {"type": ["integer", "null"],
                                                                             "description": "Maximum length constraint for string values (facet restriction)",
                                                                             "example": None},
                                                        "facet_pattern": {"type": ["string", "null"],
                                                                          "description": "Pattern constraint (regular expression) for string values (facet restriction)",
                                                                          "example": None}
                                                    },
                                                    "required": ["facet_min_length", "facet_max_length",
                                                                 "facet_pattern"]
                                                },
                                                "to_bbiep_id": {"type": ["integer", "null"],
                                                                "description": "Unique identifier for the target BBIEP that this BBIE connects to (if available)",
                                                                "example": 12348}
                                            },
                                            "required": ["component_type", "is_used",
                                                         "cardinality_min", "cardinality_max", "is_nillable",
                                                         "based_bcc"]
                                        }
                                    ]
                                },
                                "example": []
                            },
                            "created": {
                                "type": ["object", "null"],
                                "description": "Creation information",
                                "properties": {
                                    "who": {
                                        "type": ["object", "null"],
                                        "description": "User who created the ABIE",
                                        "properties": {
                                            "user_id": {"type": "integer",
                                                        "description": "Unique identifier for the user",
                                                        "example": 100000001},
                                            "login_id": {"type": "string", "description": "User's login identifier",
                                                         "example": "john.doe"},
                                            "username": {"type": "string", "description": "Display name of the user",
                                                         "example": "John Doe"},
                                            "roles": {"type": "array", "items": {"type": "string",
                                                                                 "enum": ["Admin", "Developer",
                                                                                          "End-User"]},
                                                      "description": "List of roles assigned to the user",
                                                      "example": ["End-User"]}
                                        },
                                        "required": ["user_id", "login_id", "username", "roles"]
                                    },
                                    "when": {"type": ["string", "null"], "format": "date-time",
                                             "description": "Timestamp when the ABIE was created",
                                             "example": "2023-01-15T10:30:00Z"}
                                }
                            },
                            "last_updated": {
                                "type": ["object", "null"],
                                "description": "Last update information",
                                "properties": {
                                    "who": {
                                        "type": ["object", "null"],
                                        "description": "User who last updated the ABIE",
                                        "properties": {
                                            "user_id": {"type": "integer",
                                                        "description": "Unique identifier for the user",
                                                        "example": 100000001},
                                            "login_id": {"type": "string", "description": "User's login identifier",
                                                         "example": "john.doe"},
                                            "username": {"type": "string", "description": "Display name of the user",
                                                         "example": "John Doe"},
                                            "roles": {"type": "array", "items": {"type": "string",
                                                                                 "enum": ["Admin", "Developer",
                                                                                          "End-User"]},
                                                      "description": "List of roles assigned to the user",
                                                      "example": ["End-User"]}
                                        },
                                        "required": ["user_id", "login_id", "username", "roles"]
                                    },
                                    "when": {"type": ["string", "null"], "format": "date-time",
                                             "description": "Timestamp when the ABIE was last updated",
                                             "example": "2023-01-15T14:45:00Z"}
                                }
                            }
                        },
                        "required": ["based_acc_manifest"]
                    },
                    "definition": {"type": ["string", "null"], "description": "Definition of the ASBIEP",
                                   "example": "A purchase order property"},
                    "remark": {"type": ["string", "null"], "description": "Remarks about the ASBIEP",
                               "example": "Used for procurement processes"},
                    "biz_term": {"type": ["string", "null"], "description": "Business term", "example": "PO"},
                    "display_name": {"type": ["string", "null"], "description": "Display name of the ASBIEP",
                                     "example": "Purchase Order Property"},
                    "created": {
                        "type": ["object", "null"],
                        "description": "Creation information",
                        "properties": {
                            "who": {
                                "type": ["object", "null"],
                                "description": "User who created the ASBIEP",
                                "properties": {
                                    "user_id": {"type": "integer", "description": "Unique identifier for the user",
                                                "example": 100000001},
                                    "login_id": {"type": "string", "description": "User's login identifier",
                                                 "example": "john.doe"},
                                    "username": {"type": "string", "description": "Display name of the user",
                                                 "example": "John Doe"},
                                    "roles": {"type": "array",
                                              "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                              "description": "List of roles assigned to the user",
                                              "example": ["End-User"]}
                                },
                                "required": ["user_id", "login_id", "username", "roles"]
                            },
                            "when": {"type": ["string", "null"], "format": "date-time",
                                     "description": "Timestamp when the ASBIEP was created",
                                     "example": "2023-01-15T10:30:00Z"}
                        }
                    },
                    "last_updated": {
                        "type": ["object", "null"],
                        "description": "Last update information",
                        "properties": {
                            "who": {
                                "type": ["object", "null"],
                                "description": "User who last updated the ASBIEP",
                                "properties": {
                                    "user_id": {"type": "integer", "description": "Unique identifier for the user",
                                                "example": 100000001},
                                    "login_id": {"type": "string", "description": "User's login identifier",
                                                 "example": "john.doe"},
                                    "username": {"type": "string", "description": "Display name of the user",
                                                 "example": "John Doe"},
                                    "roles": {"type": "array",
                                              "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                              "description": "List of roles assigned to the user",
                                              "example": ["End-User"]}
                                },
                                "required": ["user_id", "login_id", "username", "roles"]
                            },
                            "when": {"type": ["string", "null"], "format": "date-time",
                                     "description": "Timestamp when the ASBIEP was last updated",
                                     "example": "2023-01-15T14:45:00Z"}
                        }
                    }
                },
                "required": ["based_asccp_manifest", "role_of_abie"]
            },
            "is_used": {"type": "boolean",
                        "description": "Whether this ASBIE is currently being used (profiled) in the BIE",
                        "example": True},
            "cardinality_min": {"type": "integer",
                                "description": "Minimum cardinality (minimum number of occurrences required, typically 0 or 1)",
                                "example": 0},
            "cardinality_max": {"type": "integer",
                                "description": "Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)",
                                "example": 1},
            "is_nillable": {"type": "boolean", "description": "Whether the ASBIE can have a nil/null value",
                            "example": False},
            "definition": {"type": ["string", "null"],
                           "description": "Definition to override the ASCC definition. If NULL, it means that the definition should be derived from the based CC",
                           "example": "A purchase order detail"},
            "remark": {"type": ["string", "null"], "description": "Additional remarks or notes about the ASBIE",
                       "example": "Used for purchase orders"}
        },
        "required": ["owner_top_level_asbiep", "based_ascc", "to_asbiep", "is_used",
                     "cardinality_min", "cardinality_max", "is_nillable"]
    }
)
async def get_asbie_by_asbie_id(
    asbie_id: Annotated[int, Field(gt=0, description="Unique identifier of the ASBIE to retrieve.")],
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> GetAsbieResponse:
    """
    Get an ASBIE (Association Business Information Entity) by its ASBIE ID.

    This tool fetches the complete ASBIE information from the database when you have the asbie_id.

    The response includes:
    - asbie_id: The ASBIE ID
    - guid: The ASBIE GUID
    - based_ascc: The ASCC information this ASBIE is based on
    - to_asbiep: The ASBIEP information this ASBIE points to
    - All other ASBIE properties (is_used, cardinality, etc.)

    Using with Create Tools:
    This tool provides all the information needed to use create_asbie.
    IMPORTANT: Groups (component_type 3=SemanticGroup or 4=UserExtensionGroup) are automatically skipped in BIE expressions.
    The actual BIE structure may differ from the CC structure because groups are flattened. To find the correct
    from_abie_id, you must traverse the actual BIE structure recursively using get_top_level_asbiep, get_asbie_*,
    and get_bbie_* to find the abie_id in the BIE structure (not the CC structure).
    - from_abie_id: Use to_asbiep.role_of_abie.abie_id from this response, or traverse recursively: if an ASBIE relationship
      exists (has asbie_id), use get_asbie_by_asbie_id() to get its to_asbiep.role_of_abie.abie_id. Groups are automatically
      skipped in the BIE structure, so the abie_id reflects the actual BIE hierarchy. This value can be passed directly to
      create_asbie() and create_bbie() as the from_abie_id parameter.
      Note: The ASCC's from_acc may be a group in the CC structure, but in the BIE structure, the relationship appears
      directly under the parent ABIE (skipping the group).
    - based_ascc_manifest_id: Use to_asbiep.role_of_abie.relationships[].based_ascc.ascc_manifest_id from any ASBIE
      relationship in the relationships array. This value can be passed directly to create_asbie() as the based_ascc_manifest_id parameter.
    - Example: Use abie_id and based_ascc.ascc_manifest_id directly from this response:
      create_asbie(from_abie_id=result.to_asbiep.role_of_abie.abie_id, based_ascc_manifest_id=result.to_asbiep.role_of_abie.relationships[0].based_ascc.ascc_manifest_id)

    Args:
        asbie_id (int): The unique identifier of the ASBIE

    Returns:
        GetAsbieResponse: ASBIE information including all properties

    Raises:
        ToolError: If validation fails, the ASBIE is not found, or a database error occurs.

    Examples:
        Get ASBIE by ID:
        >>> result = get_asbie_by_asbie_id(asbie_id=12345)
        >>> print(f"ASBIE ID: {result.asbie_id}")
        >>> print(f"GUID: {result.guid}")
    """
    try:
        payload = await business_information_entity_service.get_asbie_by_asbie_id(asbie_id=asbie_id)
        if payload is None:
            raise ToolError(f"The ASBIE with ID {asbie_id} was not found. Please check the ID and try again.")
        return GetAsbieResponse.model_validate(payload, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to retrieve ASBIE {asbie_id}.") from exc


@mcp.tool(
    name="get_asbie_by_based_ascc_manifest_id",
    description="Get an ASBIE (Association Business Information Entity) by its based ASCC manifest ID. This function returns basic information based on based_ascc_manifest_id when you don't have the asbie_id. This tool assumes there's no existing ASBIE and returns basic information for creating a new one. IMPORTANT: Groups (component_type 3=SemanticGroup or 4=UserExtensionGroup) are automatically skipped in BIE expressions, so the BIE structure may differ from the CC structure. To find the correct from_abie_id for create_asbie, traverse the actual BIE structure recursively using get_top_level_asbiep, get_asbie_*, and get_bbie_* to find the abie_id in the BIE structure.",
    output_schema={
        "type": "object",
        "description": "Response containing ASBIE (Association Business Information Entity) information with its ASBIEP. Used for profiling core components – when is_used=True, the component is profiled for practical use (asbie_id will be created); when is_used=False, it shows all available components for profiling. IMPORTANT: Groups (component_type 3=SemanticGroup or 4=UserExtensionGroup) are automatically skipped in BIE expressions, so the BIE structure may differ from the CC structure. To find the correct from_abie_id for create_asbie, traverse the actual BIE structure recursively.",
        "properties": {
            "asbie_id": {"type": ["integer", "null"],
                         "description": "Unique identifier for the ASBIE (base entity ID, None if not yet created)",
                         "example": 12345},
            "owner_top_level_asbiep": {
                "type": "object",
                "description": "Top-Level ASBIEP information",
                "properties": {
                    "top_level_asbiep_id": {"type": "integer",
                                            "description": "Unique identifier for the top-level ASBIEP",
                                            "example": 12345},
                    "library": {
                        "type": "object",
                        "description": "Library information",
                        "properties": {
                            "library_id": {"type": "integer", "description": "Unique identifier for the library",
                                           "example": 1},
                            "name": {"type": "string", "description": "Library name", "example": "OAGIS"}
                        },
                        "required": ["library_id", "name"]
                    },
                    "release": {
                        "type": "object",
                        "description": "Release information",
                        "properties": {
                            "release_id": {"type": "integer", "description": "Unique identifier for the release",
                                           "example": 1},
                            "release_num": {"type": "string", "description": "Release number",
                                            "example": "10.6"},
                            "state": {"type": "string", "description": "Release state", "example": "Published"}
                        },
                        "required": ["release_id", "release_num", "state"]
                    },
                    "version": {"type": ["string", "null"], "description": "Version number", "example": "1.0"},
                    "status": {"type": ["string", "null"], "description": "Status of the top-level ASBIEP",
                               "example": "Production"},
                    "state": {"type": "string", "description": "State of the top-level ASBIEP", "example": "Published"},
                    "is_deprecated": {"type": "boolean", "description": "Whether the top-level ASBIEP is deprecated",
                                      "example": False},
                    "deprecated_reason": {"type": ["string", "null"], "description": "Reason for deprecation",
                                          "example": "Replaced by newer version"},
                    "deprecated_remark": {"type": ["string", "null"],
                                          "description": "Additional remarks about deprecation",
                                          "example": "Use version 2.0 instead"},
                    "owner": {
                        "type": "object",
                        "description": "Owner information",
                        "properties": {
                            "user_id": {"type": "integer", "description": "Unique identifier for the user",
                                        "example": 100000001},
                            "login_id": {"type": "string", "description": "User's login identifier",
                                         "example": "john.doe"},
                            "username": {"type": "string", "description": "Display name of the user",
                                         "example": "John Doe"},
                            "roles": {"type": "array",
                                      "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                      "description": "List of roles assigned to the user", "example": ["End-User"]}
                        },
                        "required": ["user_id", "login_id", "username", "roles"]
                    }
                },
                "required": ["top_level_asbiep_id", "library", "release", "state", "is_deprecated", "owner"]
            },
            "guid": {"type": ["string", "null"],
                     "description": "Globally unique identifier for the ASBIE (if available)",
                     "example": "a1b2c3d4e5f6789012345678901234ab"},
            "based_ascc": {
                "type": "object",
                "description": "ASCC (Association Core Component) information that this ASBIE is based on",
                "properties": {
                    "ascc_manifest_id": {"type": "integer",
                                         "description": "Unique identifier for the ASCC manifest. This can be passed to create_asbie() as the based_ascc_manifest_id parameter.",
                                         "example": 12345},
                    "ascc_id": {"type": "integer", "description": "Unique identifier for the ASCC", "example": 6789},
                    "guid": {"type": "string", "description": "Unique identifier within the release",
                             "example": "a1b2c3d4e5f6789012345678901234ab"},
                    "den": {"type": "string", "description": "Dictionary Entry Name (DEN)",
                            "example": "Purchase Order. Details"},
                    "cardinality_min": {"type": "integer", "description": "Minimum cardinality", "example": 0},
                    "cardinality_max": {"type": "integer", "description": "Maximum cardinality", "example": 1},
                    "is_deprecated": {"type": "boolean", "description": "Whether the ASCC is deprecated",
                                      "example": False},
                    "definition": {"type": ["string", "null"], "description": "Definition of the ASCC",
                                   "example": "Details of the purchase order"},
                    "definition_source": {"type": ["string", "null"], "description": "Source of the definition",
                                          "example": "https://example.com/spec"},
                    "from_acc_manifest_id": {"type": "integer", "description": "Source ACC manifest ID",
                                             "example": 12346},
                    "to_asccp_manifest_id": {"type": "integer", "description": "Target ASCCP manifest ID",
                                             "example": 12347}
                },
                "required": ["ascc_manifest_id", "ascc_id", "guid", "den", "cardinality_min", "cardinality_max",
                             "is_deprecated", "from_acc_manifest_id", "to_asccp_manifest_id"]
            },
            "to_asbiep": {
                "type": "object",
                "description": "ASBIEP (Association Business Information Entity Property) information",
                "properties": {
                    "asbiep_id": {"type": ["integer", "null"], "description": "Unique identifier for the ASBIEP",
                                  "example": 12348},
                    "owner_top_level_asbiep": {
                        "type": ["object", "null"],
                        "description": "Top-Level ASBIEP information",
                        "properties": {
                            "top_level_asbiep_id": {"type": "integer",
                                                    "description": "Unique identifier for the top-level ASBIEP",
                                                    "example": 12345},
                            "library": {
                                "type": "object",
                                "description": "Library information",
                                "properties": {
                                    "library_id": {"type": "integer",
                                                   "description": "Unique identifier for the library", "example": 1},
                                    "name": {"type": "string", "description": "Library name", "example": "OAGIS"}
                                },
                                "required": ["library_id", "name"]
                            },
                            "release": {
                                "type": "object",
                                "description": "Release information",
                                "properties": {
                                    "release_id": {"type": "integer",
                                                   "description": "Unique identifier for the release", "example": 1},
                                    "release_num": {"type": "string", "description": "Release number",
                                                    "example": "10.6"},
                                    "state": {"type": "string", "description": "Release state", "example": "Published"}
                                },
                                "required": ["release_id", "release_num", "state"]
                            },
                            "version": {"type": ["string", "null"], "description": "Version number", "example": "1.0"},
                            "status": {"type": ["string", "null"], "description": "Status", "example": "Production"},
                            "state": {"type": "string", "description": "State", "example": "Published"},
                            "is_deprecated": {"type": "boolean", "description": "Whether deprecated", "example": False},
                            "deprecated_reason": {"type": ["string", "null"], "description": "Reason for deprecation",
                                                  "example": None},
                            "deprecated_remark": {"type": ["string", "null"],
                                                  "description": "Remarks about deprecation", "example": None},
                            "owner": {
                                "type": "object",
                                "description": "Owner information",
                                "properties": {
                                    "user_id": {"type": "integer", "description": "Unique identifier for the user",
                                                "example": 100000001},
                                    "login_id": {"type": "string", "description": "User's login identifier",
                                                 "example": "john.doe"},
                                    "username": {"type": "string", "description": "Display name of the user",
                                                 "example": "John Doe"},
                                    "roles": {"type": "array",
                                              "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                              "description": "List of roles assigned to the user",
                                              "example": ["End-User"]}
                                },
                                "required": ["user_id", "login_id", "username", "roles"]
                            }
                        },
                        "required": ["top_level_asbiep_id", "library", "release", "state", "is_deprecated", "owner"]
                    },
                    "based_asccp_manifest": {
                        "type": "object",
                        "description": "ASCCP information that this ASBIEP is based on",
                        "properties": {
                            "asccp_manifest_id": {"type": "integer",
                                                  "description": "Unique identifier for the ASCCP manifest",
                                                  "example": 12347},
                            "asccp_id": {"type": "integer", "description": "Unique identifier for the ASCCP",
                                         "example": 6789},
                            "role_of_acc_manifest_id": {"type": "integer", "description": "Role of ACC manifest ID",
                                                        "example": 12349},
                            "guid": {"type": "string", "description": "Unique identifier within the release",
                                     "example": "a1b2c3d4e5f6789012345678901234ab"},
                            "den": {"type": ["string", "null"], "description": "Data Element Name",
                                    "example": "Purchase Order. Details"},
                            "property_term": {"type": "string", "description": "Property term",
                                              "example": "Details"},
                            "definition": {"type": ["string", "null"], "description": "Definition of the ASCCP",
                                           "example": "Details of the purchase order"},
                            "definition_source": {"type": ["string", "null"], "description": "Source of the definition",
                                                  "example": "https://example.com/spec"},
                            "is_deprecated": {"type": "boolean", "description": "Whether the ASCCP is deprecated",
                                              "example": False}
                        },
                        "required": ["asccp_manifest_id", "asccp_id", "role_of_acc_manifest_id", "guid", "den",
                                     "property_term", "is_deprecated"]
                    },
                    "role_of_abie": {
                        "type": "object",
                        "description": "Role of ABIE information",
                        "properties": {
                            "abie_id": {"type": ["integer", "null"],
                                        "description": "Unique identifier for the ABIE. This can be passed to create_asbie() and create_bbie() as the from_abie_id parameter.",
                                        "example": 12350},
                            "guid": {"type": ["string", "null"], "description": "Unique identifier for the ABIE",
                                     "example": "a1b2c3d4e5f6789012345678901234ab"},
                            "based_acc_manifest": {
                                "type": "object",
                                "description": "Based ACC manifest information",
                                "properties": {
                                    "acc_manifest_id": {"type": "integer",
                                                        "description": "Unique identifier for the ACC manifest",
                                                        "example": 12351},
                                    "acc_id": {"type": "integer", "description": "Unique identifier for the ACC",
                                               "example": 7890},
                                    "guid": {"type": "string", "description": "Unique identifier for the ACC",
                                             "example": "a1b2c3d4e5f6789012345678901234ab"},
                                    "den": {"type": "string", "description": "Data Element Name",
                                            "example": "Purchase Order. Purchase Order"},
                                    "object_class_term": {"type": "string", "description": "Object class term",
                                                          "example": "Purchase Order"},
                                    "definition": {"type": ["string", "null"], "description": "Definition of the ACC",
                                                   "example": "A purchase order aggregation"},
                                    "definition_source": {"type": ["string", "null"],
                                                          "description": "Source of the definition",
                                                          "example": "https://example.com/spec"},
                                    "is_deprecated": {"type": "boolean", "description": "Whether the ACC is deprecated",
                                                      "example": False}
                                },
                                "required": ["acc_manifest_id", "acc_id", "guid", "den", "object_class_term",
                                             "is_deprecated"]
                            },
                            "definition": {"type": ["string", "null"], "description": "Definition of the ABIE",
                                           "example": "A purchase order aggregation"},
                            "remark": {"type": ["string", "null"], "description": "Remarks about the ABIE",
                                       "example": "Used for procurement"},
                            "relationships": {
                                "type": "array",
                                "description": "List of relationships (ASBIE/BBIE) displayed as children under the ABIE. Each relationship has an 'is_used' property: when is_used=True, the relationship is profiled for practical use (asbie_id/bbie_id will be created), when is_used=False, it shows available relationships for profiling. These relationships can be further explored using get_asbie_by_asbie_id(), get_asbie_by_based_ascc_manifest_id(), get_bbie_by_bbie_id(), and get_bbie_by_based_bcc_manifest_id() functions. This includes all nested relationships from the hierarchical CC (Core Component) structure, presented as a single flat list for traversal.",
                                "items": {
                                    "oneOf": [
                                        {
                                            "type": "object",
                                            "description": "ASBIE (Association Business Information Entity) relationship",
                                            "properties": {
                                                "component_type": {"type": "string",
                                                                   "description": "Type of relationship, always 'ASBIE' for this type",
                                                                   "example": "ASBIE"},
                                                "asbie_id": {"type": ["integer", "null"],
                                                             "description": "Unique identifier for the ASBIE (base entity ID, None if is_used=False)",
                                                             "example": 12345},
                                                "guid": {"type": ["string", "null"],
                                                         "description": "Globally unique identifier for the ASBIE (if available)",
                                                         "example": "a1b2c3d4e5f6789012345678901234ab"},
                                                "is_used": {"type": "boolean",
                                                            "description": "Whether this component is currently being used (profiled) in the BIE (True means asbie_id exists)",
                                                            "example": True},
                                                "cardinality_min": {"type": "integer",
                                                                    "description": "Minimum cardinality (minimum number of occurrences required, typically 0 or 1)",
                                                                    "example": 0},
                                                "cardinality_max": {"type": "integer",
                                                                    "description": "Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)",
                                                                    "example": 1},
                                                "is_nillable": {"type": "boolean",
                                                                "description": "Whether the component can have a nil/null value",
                                                                "example": False},
                                                "remark": {"type": ["string", "null"],
                                                           "description": "Additional remarks or notes about the component",
                                                           "example": "Used for purchase orders"},
                                                "based_ascc": {
                                                    "type": "object",
                                                    "description": "ASCC (Association Core Component) information that this ASBIE component is based on",
                                                    "properties": {
                                                        "ascc_manifest_id": {"type": "integer",
                                                                             "description": "Unique identifier for the ASCC manifest. This can be passed to create_asbie() as the based_ascc_manifest_id parameter.",
                                                                             "example": 12345},
                                                        "ascc_id": {"type": "integer",
                                                                    "description": "Unique identifier for the ASCC",
                                                                    "example": 6789},
                                                        "guid": {"type": "string",
                                                                 "description": "Unique identifier within the release",
                                                                 "example": "a1b2c3d4e5f6789012345678901234ab"},
                                                        "den": {"type": "string",
                                                                "description": "Dictionary Entry Name (DEN)",
                                                                "example": "Purchase Order. Details"},
                                                        "cardinality_min": {"type": "integer",
                                                                            "description": "Minimum cardinality",
                                                                            "example": 0},
                                                        "cardinality_max": {"type": "integer",
                                                                            "description": "Maximum cardinality",
                                                                            "example": 1},
                                                        "is_deprecated": {"type": "boolean",
                                                                          "description": "Whether the ASCC is deprecated",
                                                                          "example": False},
                                                        "definition": {"type": ["string", "null"],
                                                                       "description": "Definition of the ASCC",
                                                                       "example": "Details of the purchase order"},
                                                        "definition_source": {"type": ["string", "null"],
                                                                              "description": "Source of the definition",
                                                                              "example": "https://example.com/spec"},
                                                        "from_acc_manifest_id": {"type": "integer",
                                                                                 "description": "Source ACC manifest ID",
                                                                                 "example": 12346},
                                                        "to_asccp_manifest_id": {"type": "integer",
                                                                                 "description": "Target ASCCP manifest ID",
                                                                                 "example": 12347}
                                                    },
                                                    "required": ["ascc_manifest_id", "ascc_id", "guid", "den",
                                                                 "cardinality_min", "cardinality_max", "is_deprecated",
                                                                 "from_acc_manifest_id", "to_asccp_manifest_id"]
                                                },
                                                "to_asbiep_id": {"type": ["integer", "null"],
                                                                 "description": "Unique identifier for the target ASBIEP that this ASBIE connects to (if available)",
                                                                 "example": 12348}
                                            },
                                            "required": ["component_type", "is_used",
                                                         "cardinality_min", "cardinality_max", "is_nillable",
                                                         "based_ascc"]
                                        },
                                        {
                                            "type": "object",
                                            "description": "BBIE (Basic Business Information Entity) relationship",
                                            "properties": {
                                                "component_type": {"type": "string",
                                                                   "description": "Type of relationship, always 'BBIE' for this type",
                                                                   "example": "BBIE"},
                                                "bbie_id": {"type": ["integer", "null"],
                                                            "description": "Unique identifier for the BBIE (base entity ID, None if is_used=False)",
                                                            "example": 12345},
                                                "guid": {"type": ["string", "null"],
                                                         "description": "Globally unique identifier for the BBIE (if available)",
                                                         "example": "a1b2c3d4e5f6789012345678901234ab"},
                                                "is_used": {"type": "boolean",
                                                            "description": "Whether this component is currently being used (profiled) in the BIE (True means bbie_id exists)",
                                                            "example": True},
                                                "cardinality_min": {"type": "integer",
                                                                    "description": "Minimum cardinality (minimum number of occurrences required, typically 0 or 1)",
                                                                    "example": 0},
                                                "cardinality_max": {"type": "integer",
                                                                    "description": "Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)",
                                                                    "example": 1},
                                                "is_nillable": {"type": "boolean",
                                                                "description": "Whether the component can have a nil/null value",
                                                                "example": False},
                                                "remark": {"type": ["string", "null"],
                                                           "description": "Additional remarks or notes about the component",
                                                           "example": "Used for purchase orders"},
                                                "based_bcc": {
                                                    "type": "object",
                                                    "description": "BCC (Basic Core Component) information that this BBIE component is based on",
                                                    "properties": {
                                                        "bcc_manifest_id": {"type": "integer",
                                                                            "description": "Unique identifier for the BCC manifest",
                                                                            "example": 12345},
                                                        "bcc_id": {"type": "integer",
                                                                   "description": "Unique identifier for the BCC",
                                                                   "example": 6789},
                                                        "guid": {"type": "string",
                                                                 "description": "Unique identifier within the release",
                                                                 "example": "a1b2c3d4e5f6789012345678901234ab"},
                                                        "den": {"type": "string",
                                                                "description": "Dictionary Entry Name (DEN)",
                                                                "example": "Purchase Order. Amount"},
                                                        "cardinality_min": {"type": "integer",
                                                                            "description": "Minimum cardinality",
                                                                            "example": 0},
                                                        "cardinality_max": {"type": "integer",
                                                                            "description": "Maximum cardinality",
                                                                            "example": 1},
                                                        "entity_type": {"type": ["string", "null"],
                                                                        "description": "Entity type: 'Attribute' or 'Element'",
                                                                        "example": "Element"},
                                                        "is_nillable": {"type": "boolean",
                                                                        "description": "Whether the BCC is nillable",
                                                                        "example": False},
                                                        "is_deprecated": {"type": "boolean",
                                                                          "description": "Whether the BCC is deprecated",
                                                                          "example": False},
                                                        "definition": {"type": ["string", "null"],
                                                                       "description": "Definition of the BCC",
                                                                       "example": "A monetary amount"},
                                                        "definition_source": {"type": ["string", "null"],
                                                                              "description": "Source of the definition",
                                                                              "example": "https://example.com/spec"},
                                                        "from_acc_manifest_id": {"type": "integer",
                                                                                 "description": "Source ACC manifest ID",
                                                                                 "example": 12346},
                                                        "to_bccp_manifest_id": {"type": "integer",
                                                                                "description": "Target BCCP manifest ID",
                                                                                "example": 12347}
                                                    },
                                                    "required": ["bcc_manifest_id", "bcc_id", "guid", "den",
                                                                 "cardinality_min", "cardinality_max", "is_nillable",
                                                                 "is_deprecated", "from_acc_manifest_id",
                                                                 "to_bccp_manifest_id"]
                                                },
                                                "primitiveRestriction": {
                                                    "type": ["object", "null"],
                                                    "description": "Primitive restriction information for the BBIE",
                                                    "properties": {
                                                        "xbtManifestId": {"type": ["integer", "null"],
                                                                          "description": "XBT (eXtended Built-in Type) manifest ID",
                                                                          "example": None},
                                                        "codeListManifestId": {"type": ["integer", "null"],
                                                                               "description": "Code list manifest ID",
                                                                               "example": None},
                                                        "agencyIdListManifestId": {"type": ["integer", "null"],
                                                                                   "description": "Agency ID list manifest ID",
                                                                                   "example": None}
                                                    },
                                                    "required": ["xbtManifestId", "codeListManifestId",
                                                                 "agencyIdListManifestId"]
                                                },
                                                "valueConstraint": {
                                                    "type": ["object", "null"],
                                                    "description": "Value constraint information for the BBIE",
                                                    "properties": {
                                                        "default_value": {"type": ["string", "null"],
                                                                          "description": "Default value for the BBIE",
                                                                          "example": None},
                                                        "fixed_value": {"type": ["string", "null"],
                                                                        "description": "Fixed value for the BBIE",
                                                                        "example": None}
                                                    },
                                                    "required": ["default_value", "fixed_value"]
                                                },
                                                "facet": {
                                                    "type": ["object", "null"],
                                                    "description": "Facet restriction information for string values",
                                                    "properties": {
                                                        "facet_min_length": {"type": ["integer", "null"],
                                                                             "description": "Minimum length constraint for string values (facet restriction)",
                                                                             "example": None},
                                                        "facet_max_length": {"type": ["integer", "null"],
                                                                             "description": "Maximum length constraint for string values (facet restriction)",
                                                                             "example": None},
                                                        "facet_pattern": {"type": ["string", "null"],
                                                                          "description": "Pattern constraint (regular expression) for string values (facet restriction)",
                                                                          "example": None}
                                                    },
                                                    "required": ["facet_min_length", "facet_max_length",
                                                                 "facet_pattern"]
                                                },
                                                "to_bbiep_id": {"type": ["integer", "null"],
                                                                "description": "Unique identifier for the target BBIEP that this BBIE connects to (if available)",
                                                                "example": 12348}
                                            },
                                            "required": ["component_type", "is_used",
                                                         "cardinality_min", "cardinality_max", "is_nillable",
                                                         "based_bcc"]
                                        }
                                    ]
                                },
                                "example": []
                            },
                            "created": {
                                "type": ["object", "null"],
                                "description": "Creation information",
                                "properties": {
                                    "who": {
                                        "type": ["object", "null"],
                                        "description": "User who created the ABIE",
                                        "properties": {
                                            "user_id": {"type": "integer",
                                                        "description": "Unique identifier for the user",
                                                        "example": 100000001},
                                            "login_id": {"type": "string", "description": "User's login identifier",
                                                         "example": "john.doe"},
                                            "username": {"type": "string", "description": "Display name of the user",
                                                         "example": "John Doe"},
                                            "roles": {"type": "array", "items": {"type": "string",
                                                                                 "enum": ["Admin", "Developer",
                                                                                          "End-User"]},
                                                      "description": "List of roles assigned to the user",
                                                      "example": ["End-User"]}
                                        },
                                        "required": ["user_id", "login_id", "username", "roles"]
                                    },
                                    "when": {"type": ["string", "null"], "format": "date-time",
                                             "description": "Timestamp when the ABIE was created",
                                             "example": "2023-01-15T10:30:00Z"}
                                }
                            },
                            "last_updated": {
                                "type": ["object", "null"],
                                "description": "Last update information",
                                "properties": {
                                    "who": {
                                        "type": ["object", "null"],
                                        "description": "User who last updated the ABIE",
                                        "properties": {
                                            "user_id": {"type": "integer",
                                                        "description": "Unique identifier for the user",
                                                        "example": 100000001},
                                            "login_id": {"type": "string", "description": "User's login identifier",
                                                         "example": "john.doe"},
                                            "username": {"type": "string", "description": "Display name of the user",
                                                         "example": "John Doe"},
                                            "roles": {"type": "array", "items": {"type": "string",
                                                                                 "enum": ["Admin", "Developer",
                                                                                          "End-User"]},
                                                      "description": "List of roles assigned to the user",
                                                      "example": ["End-User"]}
                                        },
                                        "required": ["user_id", "login_id", "username", "roles"]
                                    },
                                    "when": {"type": ["string", "null"], "format": "date-time",
                                             "description": "Timestamp when the ABIE was last updated",
                                             "example": "2023-01-15T14:45:00Z"}
                                }
                            }
                        },
                        "required": ["based_acc_manifest"]
                    },
                    "definition": {"type": ["string", "null"], "description": "Definition of the ASBIEP",
                                   "example": "A purchase order property"},
                    "remark": {"type": ["string", "null"], "description": "Remarks about the ASBIEP",
                               "example": "Used for procurement processes"},
                    "biz_term": {"type": ["string", "null"], "description": "Business term", "example": "PO"},
                    "display_name": {"type": ["string", "null"], "description": "Display name of the ASBIEP",
                                     "example": "Purchase Order Property"},
                    "created": {
                        "type": ["object", "null"],
                        "description": "Creation information",
                        "properties": {
                            "who": {
                                "type": ["object", "null"],
                                "description": "User who created the ASBIEP",
                                "properties": {
                                    "user_id": {"type": "integer", "description": "Unique identifier for the user",
                                                "example": 100000001},
                                    "login_id": {"type": "string", "description": "User's login identifier",
                                                 "example": "john.doe"},
                                    "username": {"type": "string", "description": "Display name of the user",
                                                 "example": "John Doe"},
                                    "roles": {"type": "array",
                                              "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                              "description": "List of roles assigned to the user",
                                              "example": ["End-User"]}
                                },
                                "required": ["user_id", "login_id", "username", "roles"]
                            },
                            "when": {"type": ["string", "null"], "format": "date-time",
                                     "description": "Timestamp when the ASBIEP was created",
                                     "example": "2023-01-15T10:30:00Z"}
                        }
                    },
                    "last_updated": {
                        "type": ["object", "null"],
                        "description": "Last update information",
                        "properties": {
                            "who": {
                                "type": ["object", "null"],
                                "description": "User who last updated the ASBIEP",
                                "properties": {
                                    "user_id": {"type": "integer", "description": "Unique identifier for the user",
                                                "example": 100000001},
                                    "login_id": {"type": "string", "description": "User's login identifier",
                                                 "example": "john.doe"},
                                    "username": {"type": "string", "description": "Display name of the user",
                                                 "example": "John Doe"},
                                    "roles": {"type": "array",
                                              "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                              "description": "List of roles assigned to the user",
                                              "example": ["End-User"]}
                                },
                                "required": ["user_id", "login_id", "username", "roles"]
                            },
                            "when": {"type": ["string", "null"], "format": "date-time",
                                     "description": "Timestamp when the ASBIEP was last updated",
                                     "example": "2023-01-15T14:45:00Z"}
                        }
                    }
                },
                "required": ["based_asccp_manifest", "role_of_abie"]
            },
            "is_used": {"type": "boolean",
                        "description": "Whether this ASBIE is currently being used (profiled) in the BIE",
                        "example": True},
            "cardinality_min": {"type": "integer",
                                "description": "Minimum cardinality (minimum number of occurrences required, typically 0 or 1)",
                                "example": 0},
            "cardinality_max": {"type": "integer",
                                "description": "Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)",
                                "example": 1},
            "is_nillable": {"type": "boolean", "description": "Whether the ASBIE can have a nil/null value",
                            "example": False},
            "definition": {"type": ["string", "null"],
                           "description": "Definition to override the ASCC definition. If NULL, it means that the definition should be derived from the based CC",
                           "example": "A purchase order detail"},
            "remark": {"type": ["string", "null"], "description": "Additional remarks or notes about the ASBIE",
                       "example": "Used for purchase orders"}
        },
        "required": ["owner_top_level_asbiep", "based_ascc", "to_asbiep", "is_used",
                     "cardinality_min", "cardinality_max", "is_nillable"]
    }
)
async def get_asbie_by_based_ascc_manifest_id(
    top_level_asbiep_id: Annotated[int, Field(gt=0, description="Owning top-level ASBIEP identifier.")],
    based_ascc_manifest_id: Annotated[int, Field(gt=0, description="Based ASCC manifest identifier.")],
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> GetAsbieTemplateResponse:
    """
    Get an ASBIE (Association Business Information Entity) by its based ASCC manifest ID.

    This tool returns basic information based on based_ascc_manifest_id when you don't have the asbie_id.
    This tool assumes there's no existing ASBIE and returns basic information for creating a new one.

    The response includes:
    - asbie_id: The ASBIE ID (null if not yet created)
    - guid: The ASBIE GUID (null if not yet created)
    - based_ascc: The ASCC information this ASBIE is based on
    - to_asbiep: The ASBIEP information this ASBIE points to
    - All other ASBIE properties (is_used, cardinality, etc.)

    Using with Create Tools:
    This tool provides all the information needed to use create_asbie.
    IMPORTANT: Groups (component_type 3=SemanticGroup or 4=UserExtensionGroup) are automatically skipped in BIE expressions.
    The actual BIE structure may differ from the CC structure because groups are flattened. To find the correct
    from_abie_id, you must traverse the actual BIE structure recursively using get_top_level_asbiep, get_asbie_*,
    and get_bbie_* to find the abie_id in the BIE structure (not the CC structure).
    - from_abie_id: Use to_asbiep.role_of_abie.abie_id from this response, or traverse recursively: if an ASBIE relationship
      exists (has asbie_id), use get_asbie_by_asbie_id() to get its to_asbiep.role_of_abie.abie_id. Groups are automatically
      skipped in the BIE structure, so the abie_id reflects the actual BIE hierarchy. This value can be passed directly to
      create_asbie() and create_bbie() as the from_abie_id parameter.
      Note: The ASCC's from_acc may be a group in the CC structure, but in the BIE structure, the relationship appears
      directly under the parent ABIE (skipping the group).
    - based_ascc_manifest_id: Use to_asbiep.role_of_abie.relationships[].based_ascc.ascc_manifest_id from any ASBIE
      relationship in the relationships array. This value can be passed directly to create_asbie() as the based_ascc_manifest_id parameter.
    - Example: Use abie_id and based_ascc.ascc_manifest_id directly from this response:
      create_asbie(from_abie_id=result.to_asbiep.role_of_abie.abie_id, based_ascc_manifest_id=result.to_asbiep.role_of_abie.relationships[0].based_ascc.ascc_manifest_id)

    Args:
        top_level_asbiep_id (int): The top-level ASBIEP ID
        based_ascc_manifest_id (int): The ASCC manifest ID that this ASBIE is based on

    Returns:
        GetAsbieResponse: ASBIE information including all properties

    Raises:
        ToolError: If validation fails, the ASCC manifest is not found, or a database error occurs.

    Examples:
        Get basic ASBIE info by based ASCC manifest ID:
        >>> result = get_asbie_by_based_ascc_manifest_id(
        ...     top_level_asbiep_id=123,
        ...     based_ascc_manifest_id=67890
        ... )
        >>> print(f"Top-level ASBIEP ID: {result.owner_top_level_asbiep.top_level_asbiep_id}")
        >>> print(f"Based ASCC: {result.based_ascc.den}")
    """
    try:
        payload = await business_information_entity_service.get_asbie_by_based_ascc_manifest_id(
            top_level_asbiep_id=top_level_asbiep_id,
            based_ascc_manifest_id=based_ascc_manifest_id,
        )
        if payload is None:
            raise ToolError(
                "The ASBIE template was not found. "
                f"No ASBIE template exists for top_level_asbiep_id={top_level_asbiep_id}, "
                f"based_ascc_manifest_id={based_ascc_manifest_id}."
            )
        return GetAsbieTemplateResponse.model_validate(payload, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to retrieve the ASBIE template.") from exc


@mcp.tool(
    name="get_bbie_by_bbie_id",
    description="Get a BBIE (Basic Business Information Entity) by its BBIE ID. This function fetches the complete BBIE information from the database when you have the bbie_id. IMPORTANT: Groups (component_type 3=SemanticGroup or 4=UserExtensionGroup) are automatically skipped in BIE expressions, so the BIE structure may differ from the CC structure. To find the correct from_abie_id for create_bbie, traverse the actual BIE structure recursively using get_top_level_asbiep, get_asbie_*, and get_bbie_* to find the abie_id in the BIE structure. This tool provides all information needed for create_bbie_sc: use to_bbiep.supplementary_components[].based_dt_sc.dt_sc_manifest_id for create_bbie_sc's based_dt_sc_manifest_id parameter.",
    output_schema={
        "type": "object",
        "description": "Response containing BBIE (Basic Business Information Entity) information with its BBIEP. Used for profiling core components – when is_used=True, the component is profiled for practical use (bbie_id will be created); when is_used=False, it shows all available components for profiling. IMPORTANT: Groups (component_type 3=SemanticGroup or 4=UserExtensionGroup) are automatically skipped in BIE expressions, so the BIE structure may differ from the CC structure. To find the correct from_abie_id for create_bbie, traverse the actual BIE structure recursively.",
        "properties": {
            "bbie_id": {"type": ["integer", "null"],
                        "description": "Unique identifier for the BBIE (base entity ID, None if not yet created)",
                        "example": 12345},
            "owner_top_level_asbiep": {
                "type": "object",
                "description": "Top-Level ASBIEP information",
                "properties": {
                    "top_level_asbiep_id": {"type": "integer",
                                            "description": "Unique identifier for the top-level ASBIEP",
                                            "example": 12345},
                    "library": {
                        "type": "object",
                        "description": "Library information",
                        "properties": {
                            "library_id": {"type": "integer", "description": "Unique identifier for the library",
                                           "example": 1},
                            "name": {"type": "string", "description": "Library name", "example": "OAGIS"}
                        },
                        "required": ["library_id", "name"]
                    },
                    "release": {
                        "type": "object",
                        "description": "Release information",
                        "properties": {
                            "release_id": {"type": "integer", "description": "Unique identifier for the release",
                                           "example": 1},
                            "release_num": {"type": "string", "description": "Release number",
                                            "example": "10.6"},
                            "state": {"type": "string", "description": "Release state", "example": "Published"}
                        },
                        "required": ["release_id", "release_num", "state"]
                    },
                    "version": {"type": ["string", "null"], "description": "Version number", "example": "1.0"},
                    "status": {"type": ["string", "null"], "description": "Status of the top-level ASBIEP",
                               "example": "Production"},
                    "state": {"type": "string", "description": "State of the top-level ASBIEP", "example": "Published"},
                    "is_deprecated": {"type": "boolean", "description": "Whether the top-level ASBIEP is deprecated",
                                      "example": False},
                    "deprecated_reason": {"type": ["string", "null"], "description": "Reason for deprecation",
                                          "example": "Replaced by newer version"},
                    "deprecated_remark": {"type": ["string", "null"],
                                          "description": "Additional remarks about deprecation",
                                          "example": "Use version 2.0 instead"},
                    "owner": {
                        "type": "object",
                        "description": "Owner information",
                        "properties": {
                            "user_id": {"type": "integer", "description": "Unique identifier for the user",
                                        "example": 100000001},
                            "login_id": {"type": "string", "description": "User's login identifier",
                                         "example": "john.doe"},
                            "username": {"type": "string", "description": "Display name of the user",
                                         "example": "John Doe"},
                            "roles": {"type": "array",
                                      "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                      "description": "List of roles assigned to the user", "example": ["End-User"]}
                        },
                        "required": ["user_id", "login_id", "username", "roles"]
                    }
                },
                "required": ["top_level_asbiep_id", "library", "release", "state", "is_deprecated", "owner"]
            },
            "guid": {"type": ["string", "null"],
                     "description": "Globally unique identifier for the BBIE (if available)",
                     "example": "a1b2c3d4e5f6789012345678901234ab"},
            "based_bcc": {
                "type": "object",
                "description": "BCC (Basic Core Component) information that this BBIE is based on",
                "properties": {
                    "bcc_manifest_id": {"type": "integer",
                                        "description": "Unique identifier for the BCC manifest. This can be passed to create_bbie() as the based_bcc_manifest_id parameter.",
                                        "example": 12345},
                    "bcc_id": {"type": "integer", "description": "Unique identifier for the BCC", "example": 6789},
                    "guid": {"type": "string", "description": "Unique identifier within the release",
                             "example": "a1b2c3d4e5f6789012345678901234ab"},
                    "den": {"type": "string", "description": "Dictionary Entry Name (DEN)",
                            "example": "Purchase Order. Amount"},
                    "cardinality_min": {"type": "integer", "description": "Minimum cardinality", "example": 0},
                    "cardinality_max": {"type": "integer", "description": "Maximum cardinality", "example": 1},
                    "entity_type": {"type": ["string", "null"], "description": "Entity type: 'Attribute' or 'Element'",
                                    "example": "Element"},
                    "is_nillable": {"type": "boolean", "description": "Whether the BCC is nillable", "example": False},
                    "is_deprecated": {"type": "boolean", "description": "Whether the BCC is deprecated",
                                      "example": False},
                    "definition": {"type": ["string", "null"], "description": "Definition of the BCC",
                                   "example": "A monetary amount"},
                    "definition_source": {"type": ["string", "null"], "description": "Source of the definition",
                                          "example": "https://example.com/spec"},
                    "from_acc_manifest_id": {"type": "integer", "description": "Source ACC manifest ID",
                                             "example": 12346},
                    "to_bccp_manifest_id": {"type": "integer", "description": "Target BCCP manifest ID",
                                            "example": 12347}
                },
                "required": ["bcc_manifest_id", "bcc_id", "guid", "den", "cardinality_min", "cardinality_max",
                             "is_nillable", "is_deprecated", "from_acc_manifest_id", "to_bccp_manifest_id"]
            },
            "to_bbiep": {
                "type": "object",
                "description": "BBIEP (Basic Business Information Entity Property) information",
                "properties": {
                    "bbiep_id": {"type": ["integer", "null"], "description": "Unique identifier for the BBIEP",
                                 "example": 12348},
                    "guid": {"type": ["string", "null"], "description": "Globally unique identifier for the BBIEP",
                             "example": "a1b2c3d4e5f6789012345678901234ab"},
                    "based_bccp": {
                        "type": "object",
                        "description": "BCCP information that this BBIEP is based on",
                        "properties": {
                            "bccp_manifest_id": {"type": "integer",
                                                 "description": "Unique identifier for the BCCP manifest",
                                                 "example": 12347},
                            "bccp_id": {"type": "integer", "description": "Unique identifier for the BCCP",
                                        "example": 6789},
                            "guid": {"type": "string", "description": "Unique identifier within the release",
                                     "example": "a1b2c3d4e5f6789012345678901234ab"},
                            "den": {"type": "string", "description": "Dictionary Entry Name (DEN)",
                                    "example": "Purchase Order. Amount"},
                            "property_term": {"type": "string", "description": "Property term", "example": "Amount"},
                            "representation_term": {"type": "string", "description": "Representation term",
                                                    "example": "Amount"},
                            "definition": {"type": ["string", "null"], "description": "Definition of the BCCP",
                                           "example": "A monetary amount"},
                            "definition_source": {"type": ["string", "null"], "description": "Source of the definition",
                                                  "example": "https://example.com/spec"},
                            "bdt_manifest": {
                                "type": "object",
                                "description": "Basic Data Type (BDT) information",
                                "properties": {
                                    "dt_manifest_id": {"type": "integer",
                                                       "description": "Unique identifier for the data type manifest",
                                                       "example": 12349},
                                    "dt_id": {"type": "integer", "description": "Unique identifier for the data type",
                                              "example": 7890},
                                    "guid": {"type": "string", "description": "Unique identifier within the release",
                                             "example": "a1b2c3d4e5f6789012345678901234ab"},
                                    "den": {"type": "string", "description": "Dictionary Entry Name (DEN)",
                                            "example": "Amount. Type"},
                                    "data_type_term": {"type": ["string", "null"], "description": "Data type term",
                                                       "example": "Amount"},
                                    "qualifier": {"type": ["string", "null"], "description": "Qualifier",
                                                  "example": "Price"},
                                    "representation_term": {"type": ["string", "null"],
                                                            "description": "Representation term", "example": "Amount"},
                                    "six_digit_id": {"type": ["string", "null"], "description": "Six-digit identifier",
                                                     "example": "123456"},
                                    "definition": {"type": ["string", "null"],
                                                   "description": "Definition of the data type",
                                                   "example": "A number of monetary units"},
                                    "definition_source": {"type": ["string", "null"],
                                                          "description": "Source of the definition",
                                                          "example": "https://unece.org"},
                                    "is_deprecated": {"type": "boolean",
                                                      "description": "Whether the data type is deprecated",
                                                      "example": False},
                                    "based_dt_manifest_id": {"type": "integer",
                                                             "description": "Base data type manifest ID",
                                                             "example": 12350}
                                },
                                "required": ["dt_manifest_id", "dt_id", "guid", "den", "representation_term",
                                             "is_deprecated", "based_dt_manifest_id"]
                            },
                            "is_deprecated": {"type": "boolean", "description": "Whether the BCCP is deprecated",
                                              "example": False}
                        },
                        "required": ["bccp_manifest_id", "bccp_id", "guid", "den", "property_term",
                                     "representation_term", "bdt_manifest", "is_deprecated"]
                    },
                    "definition": {"type": ["string", "null"], "description": "Definition of the BBIEP",
                                   "example": "A monetary amount"},
                    "remark": {"type": ["string", "null"], "description": "Remarks about the BBIEP",
                               "example": "Used for purchase orders"},
                    "biz_term": {"type": ["string", "null"], "description": "Business term", "example": "PO Amount"},
                    "display_name": {"type": ["string", "null"], "description": "Display name",
                                     "example": "Purchase Order Amount"},
                    "supplementary_components": {
                        "type": "array",
                        "description": "List of supplementary components. Each component contains 'based_dt_sc.dt_sc_manifest_id' (for use with create_bbie_sc()) and 'bbie_sc_id' (for use with update_bbie_sc() if the component already exists). Use get_bbie_by_bbie_id() or get_bbie_by_based_bcc_manifest_id() to retrieve available supplementary components, then use create_bbie_sc() with the dt_sc_manifest_id to enable them, or update_bbie_sc() with the bbie_sc_id to modify existing ones.",
                        "items": {
                            "type": "object",
                            "properties": {
                                "bbie_sc_id": {"type": ["integer", "null"],
                                               "description": "Unique identifier for the BBIE SC", "example": 12351},
                                "guid": {"type": ["string", "null"],
                                         "description": "Globally unique identifier for the BBIE SC",
                                         "example": "a1b2c3d4e5f6789012345678901234ab"},
                                "based_dt_sc": {
                                    "type": "object",
                                    "description": "Data type supplementary component information",
                                    "properties": {
                                        "dt_sc_manifest_id": {"type": "integer",
                                                              "description": "Unique identifier for the DT_SC manifest",
                                                              "example": 12352},
                                        "dt_sc_id": {"type": "integer",
                                                     "description": "Unique identifier for the DT_SC", "example": 7891},
                                        "guid": {"type": "string",
                                                 "description": "Unique identifier within the release",
                                                 "example": "a1b2c3d4e5f6789012345678901234ab"},
                                        "object_class_term": {"type": ["string", "null"],
                                                              "description": "Object class term", "example": "Amount"},
                                        "property_term": {"type": ["string", "null"], "description": "Property term",
                                                          "example": "Currency"},
                                        "representation_term": {"type": ["string", "null"],
                                                                "description": "Representation term",
                                                                "example": "Code"},
                                        "definition": {"type": ["string", "null"], "description": "Definition",
                                                       "example": "Currency code"},
                                        "definition_source": {"type": ["string", "null"],
                                                              "description": "Source of the definition",
                                                              "example": "https://example.com"},
                                        "cardinality_min": {"type": "integer", "description": "Minimum cardinality",
                                                            "example": 0},
                                        "cardinality_max": {"type": ["integer", "null"],
                                                            "description": "Maximum cardinality", "example": 1},
                                        "default_value": {"type": ["string", "null"], "description": "Default value",
                                                          "example": "USD"},
                                        "fixed_value": {"type": ["string", "null"], "description": "Fixed value",
                                                        "example": None},
                                        "is_deprecated": {"type": "boolean",
                                                          "description": "Whether the DT_SC is deprecated",
                                                          "example": False}
                                    },
                                    "required": ["dt_sc_manifest_id", "dt_sc_id", "guid", "cardinality_min",
                                                 "is_deprecated"]
                                },
                                "definition": {"type": ["string", "null"], "description": "Definition of the BBIE SC",
                                               "example": "Currency code"},
                                "biz_term": {"type": ["string", "null"], "description": "Business term",
                                             "example": "Item Name Type"},
                                "display_name": {"type": ["string", "null"], "description": "Display name",
                                                 "example": "Item Name Type Code"},
                                "cardinality_min": {"type": "integer", "description": "Minimum cardinality",
                                                    "example": 0},
                                "cardinality_max": {"type": ["integer", "null"], "description": "Maximum cardinality",
                                                    "example": 1},
                                "primitiveRestriction": {
                                    "type": "object",
                                    "description": "Primitive restriction information for the BBIE SC. This field is required and must not be None. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set (not multiple, not none).",
                                    "properties": {
                                        "xbtManifestId": {"type": ["integer", "null"],
                                                          "description": "XBT (eXtended Built-in Type) manifest ID. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set.",
                                                          "example": None},
                                        "codeListManifestId": {"type": ["integer", "null"],
                                                               "description": "Code list manifest ID. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set.",
                                                               "example": None},
                                        "agencyIdListManifestId": {"type": ["integer", "null"],
                                                                   "description": "Agency ID list manifest ID. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set.",
                                                                   "example": None}
                                    },
                                    "required": ["xbtManifestId", "codeListManifestId", "agencyIdListManifestId"]
                                },
                                "valueConstraint": {
                                    "type": ["object", "null"],
                                    "description": "Value constraint information for the BBIE SC",
                                    "properties": {
                                        "default_value": {"type": ["string", "null"], "description": "Default value",
                                                          "example": "USD"},
                                        "fixed_value": {"type": ["string", "null"], "description": "Fixed value",
                                                        "example": None}
                                    },
                                    "required": ["default_value", "fixed_value"]
                                },
                                "facet": {
                                    "type": ["object", "null"],
                                    "description": "Facet restriction information for string values",
                                    "properties": {
                                        "facet_min_length": {"type": ["integer", "null"],
                                                             "description": "Minimum length constraint", "example": 3},
                                        "facet_max_length": {"type": ["integer", "null"],
                                                             "description": "Maximum length constraint", "example": 3},
                                        "facet_pattern": {"type": ["string", "null"],
                                                          "description": "Pattern constraint",
                                                          "example": "[A-Z]{3}"}
                                    },
                                    "required": ["facet_min_length", "facet_max_length", "facet_pattern"]
                                },
                                "owner_top_level_asbiep": {
                                    "type": "object",
                                    "description": "Top-Level ASBIEP information",
                                    "properties": {
                                        "top_level_asbiep_id": {"type": "integer",
                                                                "description": "Unique identifier for the top-level ASBIEP",
                                                                "example": 12345},
                                        "library": {
                                            "type": "object",
                                            "description": "Library information",
                                            "properties": {
                                                "library_id": {"type": "integer",
                                                               "description": "Unique identifier for the library",
                                                               "example": 1},
                                                "name": {"type": "string", "description": "Library name",
                                                         "example": "OAGIS"}
                                            },
                                            "required": ["library_id", "name"]
                                        },
                                        "release": {
                                            "type": "object",
                                            "description": "Release information",
                                            "properties": {
                                                "release_id": {"type": "integer",
                                                               "description": "Unique identifier for the release",
                                                               "example": 1},
                                                "release_num": {"type": "string",
                                                                "description": "Release number", "example": "10.6"},
                                                "state": {"type": "string", "description": "Release state",
                                                          "example": "Published"}
                                            },
                                            "required": ["release_id", "release_num", "state"]
                                        },
                                        "version": {"type": ["string", "null"], "description": "Version number",
                                                    "example": "1.0"},
                                        "status": {"type": ["string", "null"], "description": "Status",
                                                   "example": "Production"},
                                        "state": {"type": "string", "description": "State", "example": "Published"},
                                        "is_deprecated": {"type": "boolean", "description": "Whether deprecated",
                                                          "example": False},
                                        "deprecated_reason": {"type": ["string", "null"],
                                                              "description": "Reason for deprecation", "example": None},
                                        "deprecated_remark": {"type": ["string", "null"],
                                                              "description": "Remarks about deprecation",
                                                              "example": None},
                                        "owner": {
                                            "type": "object",
                                            "description": "Owner information",
                                            "properties": {
                                                "user_id": {"type": "integer",
                                                            "description": "Unique identifier for the user",
                                                            "example": 100000001},
                                                "login_id": {"type": "string", "description": "User's login identifier",
                                                             "example": "john.doe"},
                                                "username": {"type": "string",
                                                             "description": "Display name of the user",
                                                             "example": "John Doe"},
                                                "roles": {"type": "array", "items": {"type": "string",
                                                                                     "enum": ["Admin", "Developer",
                                                                                              "End-User"]},
                                                          "description": "List of roles assigned to the user",
                                                          "example": ["End-User"]}
                                            },
                                            "required": ["user_id", "login_id", "username", "roles"]
                                        }
                                    },
                                    "required": ["top_level_asbiep_id", "library", "release", "state", "is_deprecated",
                                                 "owner"]
                                }
                            },
                            "required": ["based_dt_sc", "cardinality_min", "cardinality_max",
                                         "owner_top_level_asbiep"]
                        }
                    },
                    "owner_top_level_asbiep": {
                        "type": "object",
                        "description": "Top-Level ASBIEP information",
                        "properties": {
                            "top_level_asbiep_id": {"type": "integer",
                                                    "description": "Unique identifier for the top-level ASBIEP",
                                                    "example": 12345},
                            "library": {
                                "type": "object",
                                "description": "Library information",
                                "properties": {
                                    "library_id": {"type": "integer",
                                                   "description": "Unique identifier for the library", "example": 1},
                                    "name": {"type": "string", "description": "Library name", "example": "OAGIS"}
                                },
                                "required": ["library_id", "name"]
                            },
                            "release": {
                                "type": "object",
                                "description": "Release information",
                                "properties": {
                                    "release_id": {"type": "integer",
                                                   "description": "Unique identifier for the release", "example": 1},
                                    "release_num": {"type": "string", "description": "Release number",
                                                    "example": "10.6"},
                                    "state": {"type": "string", "description": "Release state", "example": "Published"}
                                },
                                "required": ["release_id", "release_num", "state"]
                            },
                            "version": {"type": ["string", "null"], "description": "Version number", "example": "1.0"},
                            "status": {"type": ["string", "null"], "description": "Status", "example": "Production"},
                            "state": {"type": "string", "description": "State", "example": "Published"},
                            "is_deprecated": {"type": "boolean", "description": "Whether deprecated", "example": False},
                            "deprecated_reason": {"type": ["string", "null"], "description": "Reason for deprecation",
                                                  "example": None},
                            "deprecated_remark": {"type": ["string", "null"],
                                                  "description": "Remarks about deprecation", "example": None},
                            "owner": {
                                "type": "object",
                                "description": "Owner information",
                                "properties": {
                                    "user_id": {"type": "integer", "description": "Unique identifier for the user",
                                                "example": 100000001},
                                    "login_id": {"type": "string", "description": "User's login identifier",
                                                 "example": "john.doe"},
                                    "username": {"type": "string", "description": "Display name of the user",
                                                 "example": "John Doe"},
                                    "roles": {"type": "array",
                                              "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                              "description": "List of roles assigned to the user",
                                              "example": ["End-User"]}
                                },
                                "required": ["user_id", "login_id", "username", "roles"]
                            }
                        },
                        "required": ["top_level_asbiep_id", "library", "release", "state", "is_deprecated", "owner"]
                    }
                },
                "required": ["based_bccp", "supplementary_components", "owner_top_level_asbiep"]
            },
            "is_used": {"type": "boolean",
                        "description": "Whether this BBIE is currently being used (profiled) in the BIE",
                        "example": True},
            "cardinality_min": {"type": "integer",
                                "description": "Minimum cardinality (minimum number of occurrences required, typically 0 or 1)",
                                "example": 0},
            "cardinality_max": {"type": "integer",
                                "description": "Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)",
                                "example": 1},
            "is_nillable": {"type": "boolean", "description": "Whether the BBIE can have a nil/null value",
                            "example": False},
            "definition": {"type": ["string", "null"],
                           "description": "Definition to override the BCC definition. If NULL, it means that the definition should be inherited from the based BCC",
                           "example": "A monetary amount"},
            "remark": {"type": ["string", "null"], "description": "Additional remarks or notes about the BBIE",
                       "example": "Used for purchase orders"},
            "primitiveRestriction": {
                "type": "object",
                "description": "Primitive restriction information for the BBIE. This field is required and must not be None. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set (not multiple, not none).",
                "properties": {
                    "xbtManifestId": {"type": ["integer", "null"],
                                      "description": "XBT (eXtended Built-in Type) manifest ID. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set.",
                                      "example": None},
                    "codeListManifestId": {"type": ["integer", "null"],
                                           "description": "Code list manifest ID. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set.",
                                           "example": None},
                    "agencyIdListManifestId": {"type": ["integer", "null"],
                                               "description": "Agency ID list manifest ID. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set.",
                                               "example": None}
                },
                "required": ["xbtManifestId", "codeListManifestId", "agencyIdListManifestId"]
            },
            "valueConstraint": {
                "type": ["object", "null"],
                "description": "Value constraint information for the BBIE",
                "properties": {
                    "default_value": {"type": ["string", "null"], "description": "Default value for the BBIE",
                                      "example": None},
                    "fixed_value": {"type": ["string", "null"],
                                    "description": "Fixed value for the BBIE",
                                    "example": None}
                },
                "required": ["default_value", "fixed_value"]
            },
            "facet": {
                "type": ["object", "null"],
                "description": "Facet restriction information for string values",
                "properties": {
                    "facet_min_length": {"type": ["integer", "null"],
                                         "description": "Minimum length constraint for string values (facet restriction)",
                                         "example": None},
                    "facet_max_length": {"type": ["integer", "null"],
                                         "description": "Maximum length constraint for string values (facet restriction)",
                                         "example": None},
                    "facet_pattern": {"type": ["string", "null"],
                                      "description": "Pattern constraint (regular expression) for string values (facet restriction)",
                                      "example": None}
                },
                "required": ["facet_min_length", "facet_max_length", "facet_pattern"]
            }
        },
        "required": ["owner_top_level_asbiep", "based_bcc", "to_bbiep", "is_used",
                     "cardinality_min", "cardinality_max", "is_nillable"]
    }
)
async def get_bbie_by_bbie_id(
    bbie_id: Annotated[int, Field(gt=0, description="Unique identifier of the BBIE to retrieve.")],
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> GetBbieResponse:
    """
    Get a BBIE (Basic Business Information Entity) by its BBIE ID.

    This tool fetches the complete BBIE information from the database when you have the bbie_id.

    The response includes:
    - bbie_id: The BBIE ID
    - guid: The BBIE GUID
    - based_bcc: The BCC information this BBIE is based on
    - to_bbiep: The BBIEP information this BBIE connects to
    - All other BBIE properties (is_used, cardinality, facets, etc.)
    - primitiveRestriction: Required field that must not be None. Contains exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId (not multiple, not none).
    - valueConstraint: Optional field for default_value or fixed_value constraints
    - supplementary_components in to_bbiep for use with create_bbie_sc() and update_bbie_sc()

    Using with Create Tools:
    This tool provides all the information needed to use create_bbie and create_bbie_sc.
    IMPORTANT: Groups (component_type 3=SemanticGroup or 4=UserExtensionGroup) are automatically skipped in BIE expressions.
    The actual BIE structure may differ from the CC structure because groups are flattened. When creating BBIEs,
    traverse the actual BIE structure recursively to find the correct parent ABIE.
    - For create_bbie: Use based_bcc.bcc_manifest_id from this response. This value can be passed directly to create_bbie()
      as the based_bcc_manifest_id parameter. The from_abie_id should be obtained by traversing the BIE structure recursively.
    - For create_bbie_sc:
      - bbie_id: Use the bbie_id from this response
      - based_dt_sc_manifest_id: Use to_bbiep.supplementary_components[].based_dt_sc.dt_sc_manifest_id from the supplementary_components array.
        This value can be passed directly to create_bbie_sc() as the based_dt_sc_manifest_id parameter.
    - Example for create_bbie_sc: create_bbie_sc(bbie_id=result.bbie_id, based_dt_sc_manifest_id=result.to_bbiep.supplementary_components[0].based_dt_sc.dt_sc_manifest_id)

    Args:
        bbie_id (int): The unique identifier of the BBIE

    Returns:
        GetBbieResponse: BBIE information including all properties. The primitiveRestriction field is required and must not be None.

    Raises:
        ToolError: If validation fails, the BBIE is not found, or a database error occurs.

    Examples:
        Get BBIE by ID:
        >>> result = get_bbie_by_bbie_id(bbie_id=9876)
        >>> print(f"BBIE ID: {result.bbie_id}")
        >>> print(f"GUID: {result.guid}")
        >>> print(f"XBT Manifest ID: {result.primitiveRestriction.xbtManifestId}")
    """
    try:
        payload = await business_information_entity_service.get_bbie_by_bbie_id(bbie_id=bbie_id)
        if payload is None:
            raise ToolError(f"The BBIE with ID {bbie_id} was not found. Please check the ID and try again.")
        return GetBbieResponse.model_validate(payload, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to retrieve BBIE {bbie_id}.") from exc


@mcp.tool(
    name="get_bbie_by_based_bcc_manifest_id",
    description="Get a BBIE (Basic Business Information Entity) by its based BCC manifest ID. This function returns basic information based on based_bcc_manifest_id when you don't have the bbie_id. This tool assumes there's no existing BBIE and returns basic information for creating a new one. IMPORTANT: Groups (component_type 3=SemanticGroup or 4=UserExtensionGroup) are automatically skipped in BIE expressions, so the BIE structure may differ from the CC structure. To find the correct from_abie_id for create_bbie, traverse the actual BIE structure recursively using get_top_level_asbiep, get_asbie_*, and get_bbie_* to find the abie_id in the BIE structure. This tool provides all information needed for create_bbie_sc: use to_bbiep.supplementary_components[].based_dt_sc.dt_sc_manifest_id for create_bbie_sc's based_dt_sc_manifest_id parameter.",
    output_schema={
        "type": "object",
        "description": "Response containing BBIE (Basic Business Information Entity) information with its BBIEP. Used for profiling core components – when is_used=True, the component is profiled for practical use (bbie_id will be created); when is_used=False, it shows all available components for profiling. IMPORTANT: Groups (component_type 3=SemanticGroup or 4=UserExtensionGroup) are automatically skipped in BIE expressions, so the BIE structure may differ from the CC structure. To find the correct from_abie_id for create_bbie, traverse the actual BIE structure recursively.",
        "properties": {
            "bbie_id": {"type": ["integer", "null"],
                        "description": "Unique identifier for the BBIE (base entity ID, None if not yet created)",
                        "example": 12345},
            "owner_top_level_asbiep": {
                "type": "object",
                "description": "Top-Level ASBIEP information",
                "properties": {
                    "top_level_asbiep_id": {"type": "integer",
                                            "description": "Unique identifier for the top-level ASBIEP",
                                            "example": 12345},
                    "library": {
                        "type": "object",
                        "description": "Library information",
                        "properties": {
                            "library_id": {"type": "integer", "description": "Unique identifier for the library",
                                           "example": 1},
                            "name": {"type": "string", "description": "Library name", "example": "OAGIS"}
                        },
                        "required": ["library_id", "name"]
                    },
                    "release": {
                        "type": "object",
                        "description": "Release information",
                        "properties": {
                            "release_id": {"type": "integer", "description": "Unique identifier for the release",
                                           "example": 1},
                            "release_num": {"type": "string", "description": "Release number",
                                            "example": "10.6"},
                            "state": {"type": "string", "description": "Release state", "example": "Published"}
                        },
                        "required": ["release_id", "release_num", "state"]
                    },
                    "version": {"type": ["string", "null"], "description": "Version number", "example": "1.0"},
                    "status": {"type": ["string", "null"], "description": "Status of the top-level ASBIEP",
                               "example": "Production"},
                    "state": {"type": "string", "description": "State of the top-level ASBIEP", "example": "Published"},
                    "is_deprecated": {"type": "boolean", "description": "Whether the top-level ASBIEP is deprecated",
                                      "example": False},
                    "deprecated_reason": {"type": ["string", "null"], "description": "Reason for deprecation",
                                          "example": "Replaced by newer version"},
                    "deprecated_remark": {"type": ["string", "null"],
                                          "description": "Additional remarks about deprecation",
                                          "example": "Use version 2.0 instead"},
                    "owner": {
                        "type": "object",
                        "description": "Owner information",
                        "properties": {
                            "user_id": {"type": "integer", "description": "Unique identifier for the user",
                                        "example": 100000001},
                            "login_id": {"type": "string", "description": "User's login identifier",
                                         "example": "john.doe"},
                            "username": {"type": "string", "description": "Display name of the user",
                                         "example": "John Doe"},
                            "roles": {"type": "array",
                                      "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                      "description": "List of roles assigned to the user", "example": ["End-User"]}
                        },
                        "required": ["user_id", "login_id", "username", "roles"]
                    }
                },
                "required": ["top_level_asbiep_id", "library", "release", "state", "is_deprecated", "owner"]
            },
            "guid": {"type": ["string", "null"],
                     "description": "Globally unique identifier for the BBIE (if available)",
                     "example": "a1b2c3d4e5f6789012345678901234ab"},
            "based_bcc": {
                "type": "object",
                "description": "BCC (Basic Core Component) information that this BBIE is based on",
                "properties": {
                    "bcc_manifest_id": {"type": "integer",
                                        "description": "Unique identifier for the BCC manifest. This can be passed to create_bbie() as the based_bcc_manifest_id parameter.",
                                        "example": 12345},
                    "bcc_id": {"type": "integer", "description": "Unique identifier for the BCC", "example": 6789},
                    "guid": {"type": "string", "description": "Unique identifier within the release",
                             "example": "a1b2c3d4e5f6789012345678901234ab"},
                    "den": {"type": "string", "description": "Dictionary Entry Name (DEN)",
                            "example": "Purchase Order. Amount"},
                    "cardinality_min": {"type": "integer", "description": "Minimum cardinality", "example": 0},
                    "cardinality_max": {"type": "integer", "description": "Maximum cardinality", "example": 1},
                    "entity_type": {"type": ["string", "null"], "description": "Entity type: 'Attribute' or 'Element'",
                                    "example": "Element"},
                    "is_nillable": {"type": "boolean", "description": "Whether the BCC is nillable", "example": False},
                    "is_deprecated": {"type": "boolean", "description": "Whether the BCC is deprecated",
                                      "example": False},
                    "definition": {"type": ["string", "null"], "description": "Definition of the BCC",
                                   "example": "A monetary amount"},
                    "definition_source": {"type": ["string", "null"], "description": "Source of the definition",
                                          "example": "https://example.com/spec"},
                    "from_acc_manifest_id": {"type": "integer", "description": "Source ACC manifest ID",
                                             "example": 12346},
                    "to_bccp_manifest_id": {"type": "integer", "description": "Target BCCP manifest ID",
                                            "example": 12347}
                },
                "required": ["bcc_manifest_id", "bcc_id", "guid", "den", "cardinality_min", "cardinality_max",
                             "is_nillable", "is_deprecated", "from_acc_manifest_id", "to_bccp_manifest_id"]
            },
            "to_bbiep": {
                "type": "object",
                "description": "BBIEP (Basic Business Information Entity Property) information",
                "properties": {
                    "bbiep_id": {"type": ["integer", "null"], "description": "Unique identifier for the BBIEP",
                                 "example": 12348},
                    "guid": {"type": ["string", "null"], "description": "Globally unique identifier for the BBIEP",
                             "example": "a1b2c3d4e5f6789012345678901234ab"},
                    "based_bccp": {
                        "type": "object",
                        "description": "BCCP information that this BBIEP is based on",
                        "properties": {
                            "bccp_manifest_id": {"type": "integer",
                                                 "description": "Unique identifier for the BCCP manifest",
                                                 "example": 12347},
                            "bccp_id": {"type": "integer", "description": "Unique identifier for the BCCP",
                                        "example": 6789},
                            "guid": {"type": "string", "description": "Unique identifier within the release",
                                     "example": "a1b2c3d4e5f6789012345678901234ab"},
                            "den": {"type": "string", "description": "Dictionary Entry Name (DEN)",
                                    "example": "Purchase Order. Amount"},
                            "property_term": {"type": "string", "description": "Property term", "example": "Amount"},
                            "representation_term": {"type": "string", "description": "Representation term",
                                                    "example": "Amount"},
                            "definition": {"type": ["string", "null"], "description": "Definition of the BCCP",
                                           "example": "A monetary amount"},
                            "definition_source": {"type": ["string", "null"], "description": "Source of the definition",
                                                  "example": "https://example.com/spec"},
                            "bdt_manifest": {
                                "type": "object",
                                "description": "Basic Data Type (BDT) information",
                                "properties": {
                                    "dt_manifest_id": {"type": "integer",
                                                       "description": "Unique identifier for the data type manifest",
                                                       "example": 12349},
                                    "dt_id": {"type": "integer", "description": "Unique identifier for the data type",
                                              "example": 7890},
                                    "guid": {"type": "string", "description": "Unique identifier within the release",
                                             "example": "a1b2c3d4e5f6789012345678901234ab"},
                                    "den": {"type": "string", "description": "Dictionary Entry Name (DEN)",
                                            "example": "Amount. Type"},
                                    "data_type_term": {"type": ["string", "null"], "description": "Data type term",
                                                       "example": "Amount"},
                                    "qualifier": {"type": ["string", "null"], "description": "Qualifier",
                                                  "example": "Price"},
                                    "representation_term": {"type": ["string", "null"],
                                                            "description": "Representation term", "example": "Amount"},
                                    "six_digit_id": {"type": ["string", "null"], "description": "Six-digit identifier",
                                                     "example": "123456"},
                                    "definition": {"type": ["string", "null"],
                                                   "description": "Definition of the data type",
                                                   "example": "A number of monetary units"},
                                    "definition_source": {"type": ["string", "null"],
                                                          "description": "Source of the definition",
                                                          "example": "https://unece.org"},
                                    "is_deprecated": {"type": "boolean",
                                                      "description": "Whether the data type is deprecated",
                                                      "example": False},
                                    "based_dt_manifest_id": {"type": "integer",
                                                             "description": "Base data type manifest ID",
                                                             "example": 12350}
                                },
                                "required": ["dt_manifest_id", "dt_id", "guid", "den", "representation_term",
                                             "is_deprecated", "based_dt_manifest_id"]
                            },
                            "is_deprecated": {"type": "boolean", "description": "Whether the BCCP is deprecated",
                                              "example": False}
                        },
                        "required": ["bccp_manifest_id", "bccp_id", "guid", "den", "property_term",
                                     "representation_term", "bdt_manifest", "is_deprecated"]
                    },
                    "definition": {"type": ["string", "null"], "description": "Definition of the BBIEP",
                                   "example": "A monetary amount"},
                    "remark": {"type": ["string", "null"], "description": "Remarks about the BBIEP",
                               "example": "Used for purchase orders"},
                    "biz_term": {"type": ["string", "null"], "description": "Business term", "example": "PO Amount"},
                    "display_name": {"type": ["string", "null"], "description": "Display name",
                                     "example": "Purchase Order Amount"},
                    "supplementary_components": {
                        "type": "array",
                        "description": "List of supplementary components. Each component contains 'based_dt_sc.dt_sc_manifest_id' (for use with create_bbie_sc()) and 'bbie_sc_id' (for use with update_bbie_sc() if the component already exists). Use get_bbie_by_based_bcc_manifest_id() to retrieve available supplementary components, then use create_bbie_sc() with the dt_sc_manifest_id to enable them, or update_bbie_sc() with the bbie_sc_id to modify existing ones.",
                        "items": {
                            "type": "object",
                            "properties": {
                                "bbie_sc_id": {"type": ["integer", "null"],
                                               "description": "Unique identifier for the BBIE SC", "example": 12351},
                                "guid": {"type": ["string", "null"],
                                         "description": "Globally unique identifier for the BBIE SC",
                                         "example": "a1b2c3d4e5f6789012345678901234ab"},
                                "based_dt_sc": {
                                    "type": "object",
                                    "description": "Data type supplementary component information",
                                    "properties": {
                                        "dt_sc_manifest_id": {"type": "integer",
                                                              "description": "Unique identifier for the DT_SC manifest",
                                                              "example": 12352},
                                        "dt_sc_id": {"type": "integer",
                                                     "description": "Unique identifier for the DT_SC", "example": 7891},
                                        "guid": {"type": "string",
                                                 "description": "Unique identifier within the release",
                                                 "example": "a1b2c3d4e5f6789012345678901234ab"},
                                        "object_class_term": {"type": ["string", "null"],
                                                              "description": "Object class term", "example": "Amount"},
                                        "property_term": {"type": ["string", "null"], "description": "Property term",
                                                          "example": "Currency"},
                                        "representation_term": {"type": ["string", "null"],
                                                                "description": "Representation term",
                                                                "example": "Code"},
                                        "definition": {"type": ["string", "null"], "description": "Definition",
                                                       "example": "Currency code"},
                                        "definition_source": {"type": ["string", "null"],
                                                              "description": "Source of the definition",
                                                              "example": "https://example.com"},
                                        "cardinality_min": {"type": "integer", "description": "Minimum cardinality",
                                                            "example": 0},
                                        "cardinality_max": {"type": ["integer", "null"],
                                                            "description": "Maximum cardinality", "example": 1},
                                        "default_value": {"type": ["string", "null"], "description": "Default value",
                                                          "example": "USD"},
                                        "fixed_value": {"type": ["string", "null"], "description": "Fixed value",
                                                        "example": None},
                                        "is_deprecated": {"type": "boolean",
                                                          "description": "Whether the DT_SC is deprecated",
                                                          "example": False}
                                    },
                                    "required": ["dt_sc_manifest_id", "dt_sc_id", "guid", "cardinality_min",
                                                 "is_deprecated"]
                                },
                                "definition": {"type": ["string", "null"], "description": "Definition of the BBIE SC",
                                               "example": "Currency code"},
                                "biz_term": {"type": ["string", "null"], "description": "Business term",
                                             "example": "Item Name Type"},
                                "display_name": {"type": ["string", "null"], "description": "Display name",
                                                 "example": "Item Name Type Code"},
                                "cardinality_min": {"type": "integer", "description": "Minimum cardinality",
                                                    "example": 0},
                                "cardinality_max": {"type": ["integer", "null"], "description": "Maximum cardinality",
                                                    "example": 1},
                                "primitiveRestriction": {
                                    "type": "object",
                                    "description": "Primitive restriction information for the BBIE SC. This field is required and must not be None. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set (not multiple, not none).",
                                    "properties": {
                                        "xbtManifestId": {"type": ["integer", "null"],
                                                          "description": "XBT (eXtended Built-in Type) manifest ID. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set.",
                                                          "example": None},
                                        "codeListManifestId": {"type": ["integer", "null"],
                                                               "description": "Code list manifest ID. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set.",
                                                               "example": None},
                                        "agencyIdListManifestId": {"type": ["integer", "null"],
                                                                   "description": "Agency ID list manifest ID. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set.",
                                                                   "example": None}
                                    },
                                    "required": ["xbtManifestId", "codeListManifestId", "agencyIdListManifestId"]
                                },
                                "valueConstraint": {
                                    "type": ["object", "null"],
                                    "description": "Value constraint information for the BBIE SC",
                                    "properties": {
                                        "default_value": {"type": ["string", "null"], "description": "Default value",
                                                          "example": "USD"},
                                        "fixed_value": {"type": ["string", "null"], "description": "Fixed value",
                                                        "example": None}
                                    },
                                    "required": ["default_value", "fixed_value"]
                                },
                                "facet": {
                                    "type": ["object", "null"],
                                    "description": "Facet restriction information for string values",
                                    "properties": {
                                        "facet_min_length": {"type": ["integer", "null"],
                                                             "description": "Minimum length constraint", "example": 3},
                                        "facet_max_length": {"type": ["integer", "null"],
                                                             "description": "Maximum length constraint", "example": 3},
                                        "facet_pattern": {"type": ["string", "null"],
                                                          "description": "Pattern constraint",
                                                          "example": "[A-Z]{3}"}
                                    },
                                    "required": ["facet_min_length", "facet_max_length", "facet_pattern"]
                                },
                                "owner_top_level_asbiep": {
                                    "type": "object",
                                    "description": "Top-Level ASBIEP information",
                                    "properties": {
                                        "top_level_asbiep_id": {"type": "integer",
                                                                "description": "Unique identifier for the top-level ASBIEP",
                                                                "example": 12345},
                                        "library": {
                                            "type": "object",
                                            "description": "Library information",
                                            "properties": {
                                                "library_id": {"type": "integer",
                                                               "description": "Unique identifier for the library",
                                                               "example": 1},
                                                "name": {"type": "string", "description": "Library name",
                                                         "example": "OAGIS"}
                                            },
                                            "required": ["library_id", "name"]
                                        },
                                        "release": {
                                            "type": "object",
                                            "description": "Release information",
                                            "properties": {
                                                "release_id": {"type": "integer",
                                                               "description": "Unique identifier for the release",
                                                               "example": 1},
                                                "release_num": {"type": "string",
                                                                "description": "Release number", "example": "10.6"},
                                                "state": {"type": "string", "description": "Release state",
                                                          "example": "Published"}
                                            },
                                            "required": ["release_id", "release_num", "state"]
                                        },
                                        "version": {"type": ["string", "null"], "description": "Version number",
                                                    "example": "1.0"},
                                        "status": {"type": ["string", "null"], "description": "Status",
                                                   "example": "Production"},
                                        "state": {"type": "string", "description": "State", "example": "Published"},
                                        "is_deprecated": {"type": "boolean", "description": "Whether deprecated",
                                                          "example": False},
                                        "deprecated_reason": {"type": ["string", "null"],
                                                              "description": "Reason for deprecation", "example": None},
                                        "deprecated_remark": {"type": ["string", "null"],
                                                              "description": "Remarks about deprecation",
                                                              "example": None},
                                        "owner": {
                                            "type": "object",
                                            "description": "Owner information",
                                            "properties": {
                                                "user_id": {"type": "integer",
                                                            "description": "Unique identifier for the user",
                                                            "example": 100000001},
                                                "login_id": {"type": "string", "description": "User's login identifier",
                                                             "example": "john.doe"},
                                                "username": {"type": "string",
                                                             "description": "Display name of the user",
                                                             "example": "John Doe"},
                                                "roles": {"type": "array", "items": {"type": "string",
                                                                                     "enum": ["Admin", "Developer",
                                                                                              "End-User"]},
                                                          "description": "List of roles assigned to the user",
                                                          "example": ["End-User"]}
                                            },
                                            "required": ["user_id", "login_id", "username", "roles"]
                                        }
                                    },
                                    "required": ["top_level_asbiep_id", "library", "release", "state", "is_deprecated",
                                                 "owner"]
                                }
                            },
                            "required": ["based_dt_sc", "cardinality_min", "cardinality_max",
                                         "owner_top_level_asbiep"]
                        }
                    },
                    "owner_top_level_asbiep": {
                        "type": "object",
                        "description": "Top-Level ASBIEP information",
                        "properties": {
                            "top_level_asbiep_id": {"type": "integer",
                                                    "description": "Unique identifier for the top-level ASBIEP",
                                                    "example": 12345},
                            "library": {
                                "type": "object",
                                "description": "Library information",
                                "properties": {
                                    "library_id": {"type": "integer",
                                                   "description": "Unique identifier for the library", "example": 1},
                                    "name": {"type": "string", "description": "Library name", "example": "OAGIS"}
                                },
                                "required": ["library_id", "name"]
                            },
                            "release": {
                                "type": "object",
                                "description": "Release information",
                                "properties": {
                                    "release_id": {"type": "integer",
                                                   "description": "Unique identifier for the release", "example": 1},
                                    "release_num": {"type": "string", "description": "Release number",
                                                    "example": "10.6"},
                                    "state": {"type": "string", "description": "Release state", "example": "Published"}
                                },
                                "required": ["release_id", "release_num", "state"]
                            },
                            "version": {"type": ["string", "null"], "description": "Version number", "example": "1.0"},
                            "status": {"type": ["string", "null"], "description": "Status", "example": "Production"},
                            "state": {"type": "string", "description": "State", "example": "Published"},
                            "is_deprecated": {"type": "boolean", "description": "Whether deprecated", "example": False},
                            "deprecated_reason": {"type": ["string", "null"], "description": "Reason for deprecation",
                                                  "example": None},
                            "deprecated_remark": {"type": ["string", "null"],
                                                  "description": "Remarks about deprecation", "example": None},
                            "owner": {
                                "type": "object",
                                "description": "Owner information",
                                "properties": {
                                    "user_id": {"type": "integer", "description": "Unique identifier for the user",
                                                "example": 100000001},
                                    "login_id": {"type": "string", "description": "User's login identifier",
                                                 "example": "john.doe"},
                                    "username": {"type": "string", "description": "Display name of the user",
                                                 "example": "John Doe"},
                                    "roles": {"type": "array",
                                              "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                              "description": "List of roles assigned to the user",
                                              "example": ["End-User"]}
                                },
                                "required": ["user_id", "login_id", "username", "roles"]
                            }
                        },
                        "required": ["top_level_asbiep_id", "library", "release", "state", "is_deprecated", "owner"]
                    }
                },
                "required": ["based_bccp", "supplementary_components", "owner_top_level_asbiep"]
            },
            "is_used": {"type": "boolean",
                        "description": "Whether this BBIE is currently being used (profiled) in the BIE",
                        "example": True},
            "cardinality_min": {"type": "integer",
                                "description": "Minimum cardinality (minimum number of occurrences required, typically 0 or 1)",
                                "example": 0},
            "cardinality_max": {"type": "integer",
                                "description": "Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)",
                                "example": 1},
            "is_nillable": {"type": "boolean", "description": "Whether the BBIE can have a nil/null value",
                            "example": False},
            "definition": {"type": ["string", "null"],
                           "description": "Definition to override the BCC definition. If NULL, it means that the definition should be inherited from the based BCC",
                           "example": "A monetary amount"},
            "remark": {"type": ["string", "null"], "description": "Additional remarks or notes about the BBIE",
                       "example": "Used for purchase orders"},
            "primitiveRestriction": {
                "type": "object",
                "description": "Primitive restriction information for the BBIE. This field is required and must not be None. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set (not multiple, not none).",
                "properties": {
                    "xbtManifestId": {"type": ["integer", "null"],
                                      "description": "XBT (eXtended Built-in Type) manifest ID. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set.",
                                      "example": None},
                    "codeListManifestId": {"type": ["integer", "null"],
                                           "description": "Code list manifest ID. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set.",
                                           "example": None},
                    "agencyIdListManifestId": {"type": ["integer", "null"],
                                               "description": "Agency ID list manifest ID. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set.",
                                               "example": None}
                },
                "required": ["xbtManifestId", "codeListManifestId", "agencyIdListManifestId"]
            },
            "valueConstraint": {
                "type": ["object", "null"],
                "description": "Value constraint information for the BBIE",
                "properties": {
                    "default_value": {"type": ["string", "null"],
                                      "description": "Default value for the BBIE if not specified",
                                      "example": None},
                    "fixed_value": {"type": ["string", "null"],
                                    "description": "Fixed value that must always be used for this BBIE (cannot be changed)",
                                    "example": None}
                },
                "required": ["default_value", "fixed_value"]
            },
            "facet": {
                "type": ["object", "null"],
                "description": "Facet restriction information for string values",
                "properties": {
                    "facet_min_length": {"type": ["integer", "null"],
                                         "description": "Minimum length constraint for string values (facet restriction)",
                                         "example": None},
                    "facet_max_length": {"type": ["integer", "null"],
                                         "description": "Maximum length constraint for string values (facet restriction)",
                                         "example": None},
                    "facet_pattern": {"type": ["string", "null"],
                                      "description": "Pattern constraint (regular expression) for string values (facet restriction)",
                                      "example": None}
                },
                "required": ["facet_min_length", "facet_max_length", "facet_pattern"]
            }
        },
        "required": ["owner_top_level_asbiep", "based_bcc", "to_bbiep", "is_used",
                     "cardinality_min", "cardinality_max", "is_nillable", "primitiveRestriction"]
    }
)
async def get_bbie_by_based_bcc_manifest_id(
    top_level_asbiep_id: Annotated[int, Field(gt=0, description="Owning top-level ASBIEP identifier.")],
    based_bcc_manifest_id: Annotated[int, Field(gt=0, description="Based BCC manifest identifier.")],
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> GetBbieTemplateResponse:
    """
    Get a BBIE (Basic Business Information Entity) by its based BCC manifest ID.

    This tool returns basic information based on based_bcc_manifest_id when you don't have the bbie_id.
    This tool assumes there's no existing BBIE and returns basic information for creating a new one.

    The response includes:
    - bbie_id: The BBIE ID (null if not yet created)
    - guid: The BBIE GUID (null if not yet created)
    - based_bcc: The BCC information this BBIE is based on
    - to_bbiep: The BBIEP information this BBIE connects to
    - All other BBIE properties (is_used, cardinality, facets, etc.)
    - primitiveRestriction: Required field that must not be None. Contains exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId (not multiple, not none). Fetched from DtAwdPri if no BBIE exists yet.
    - valueConstraint: Optional field for default_value or fixed_value constraints (fetched from BCC if no BBIE exists yet)
    - supplementary_components in to_bbiep for use with create_bbie_sc() and update_bbie_sc()

    Using with Create Tools:
    This tool provides all the information needed to use create_bbie and create_bbie_sc.
    IMPORTANT: Groups (component_type 3=SemanticGroup or 4=UserExtensionGroup) are automatically skipped in BIE expressions.
    The actual BIE structure may differ from the CC structure because groups are flattened. When creating BBIEs,
    traverse the actual BIE structure recursively to find the correct parent ABIE. Note: The BCC's from_acc may
    be a group in the CC structure, but in the BIE structure, the relationship appears directly under the parent
    ABIE (skipping the group).
    - For create_bbie: Use based_bcc.bcc_manifest_id from this response. This value can be passed directly to create_bbie()
      as the based_bcc_manifest_id parameter. The from_abie_id should be obtained by traversing the BIE structure recursively.
    - For create_bbie_sc:
      - bbie_id: Use the bbie_id from this response (if it exists, otherwise create the BBIE first using create_bbie)
      - based_dt_sc_manifest_id: Use to_bbiep.supplementary_components[].based_dt_sc.dt_sc_manifest_id from the supplementary_components array.
        This value can be passed directly to create_bbie_sc() as the based_dt_sc_manifest_id parameter.
    - Example for create_bbie_sc: create_bbie_sc(bbie_id=result.bbie_id, based_dt_sc_manifest_id=result.to_bbiep.supplementary_components[0].based_dt_sc.dt_sc_manifest_id)

    Args:
        top_level_asbiep_id (int): The top-level ASBIEP ID
        based_bcc_manifest_id (int): The BCC manifest ID that this BBIE is based on

    Returns:
        GetBbieResponse: BBIE information including all properties. The primitiveRestriction field is required and must not be None, even when no BBIE exists yet (fetched from DtAwdPri).

    Raises:
        ToolError: If validation fails, the BCC manifest is not found, or a database error occurs.

    Examples:
        Get basic BBIE info by based BCC manifest ID:
        >>> result = get_bbie_by_based_bcc_manifest_id(
        ...     top_level_asbiep_id=123,
        ...     based_bcc_manifest_id=456
        ... )
        >>> print(f"Top-level ASBIEP ID: {result.owner_top_level_asbiep.top_level_asbiep_id}")
        >>> print(f"Based BCC: {result.based_bcc.den}")
    """
    try:
        payload = await business_information_entity_service.get_bbie_by_based_bcc_manifest_id(
            top_level_asbiep_id=top_level_asbiep_id,
            based_bcc_manifest_id=based_bcc_manifest_id,
        )
        if payload is None:
            raise ToolError(
                "The BBIE template was not found. "
                f"No BBIE template exists for top_level_asbiep_id={top_level_asbiep_id}, "
                f"based_bcc_manifest_id={based_bcc_manifest_id}."
            )
        return GetBbieTemplateResponse.model_validate(payload, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to retrieve the BBIE template.") from exc


@mcp.tool(
    name="create_top_level_asbiep",
    description="Create a new Top-Level ASBIEP (Association Business Information Entity Property) with the specified ASCCP (Association Core Component Property) manifest and business contexts",
    output_schema={
        "type": "object",
        "description": "Response containing the newly created top-level ASBIEP information. The 'asbiep' field contains ASBIEP structure with role_of_abie (excludes remark, is_nillable from relationships).",
        "properties": {
            "top_level_asbiep_id": {"type": "integer",
                                    "description": "ID of the created top-level ASBIEP (Association Business Information Entity Property)",
                                    "example": 12345},
            "asbiep": {"type": ["object", "null"],
                       "description": "ASBIEP structure with role_of_abie. Shows the hierarchical structure with asbiep_id and role_of_abie containing abie_id. Excludes remark and is_nillable fields from relationships.",
                       "properties": {
                           "asbiep_id": {"type": "integer", "description": "Unique identifier of the ASBIEP",
                                         "example": 12346},
                           "role_of_abie": {"type": ["object", "null"],
                                            "description": "The ABIE that this ASBIEP points to", "properties": {
                                   "abie_id": {"type": "integer", "description": "Unique identifier of the ABIE",
                                               "example": 12347},
                                   "relationships": {"type": "array",
                                                     "description": "List of relationships (ASBIEs and BBIEs) from this ABIE",
                                                     "items": {
                                                         "type": "object",
                                                         "description": "A relationship that can be either an ASBIE or BBIE",
                                                         "properties": {
                                                             "asbie": {"type": ["object", "null"],
                                                                       "description": "ASBIE relationship (if this is an ASBIE). Excludes is_nillable and remark fields.",
                                                                       "properties": {
                                                                           "asbie_id": {"type": "integer"},
                                                                           "guid": {"type": ["string", "null"]},
                                                                           "cardinality_min": {"type": "integer"},
                                                                           "cardinality_max": {"type": "integer"},
                                                                           "based_ascc": {"type": "object"},
                                                                           "asbiep": {"type": ["object", "null"],
                                                                                      "description": "Recursive structure - ASBIEP with its role_of_abie and relationships"}
                                                                       }},
                                                             "bbie": {"type": ["object", "null"],
                                                                      "description": "BBIE relationship (if this is a BBIE). Excludes remark field.",
                                                                      "properties": {
                                                                          "bbie_id": {"type": "integer"},
                                                                          "guid": {"type": ["string", "null"]},
                                                                          "cardinality_min": {"type": "integer"},
                                                                          "cardinality_max": {"type": "integer"},
                                                                          "is_nillable": {"type": "boolean"},
                                                                          "based_bcc": {"type": "object"}
                                                                      }}
                                                         }
                                                     }}
                               }}
                       }}
        },
        "required": ["top_level_asbiep_id"]
    }
)
async def create_top_level_asbiep(
    asccp_manifest_id: Annotated[int, Field(gt=0, description="ASCCP manifest identifier for the new top-level ASBIEP.")],
    biz_ctx_list: Annotated[list[int] | str, Field(description="Business context IDs to assign. Provide a JSON array or a comma-separated string like '1,2'.")],
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> CreateTopLevelAsbiepResponse:
    """
    Create a new BIE (Business Information Entity) with the specified ASCCP (Association Core Component Property) manifest and business contexts.

    When users request to create a BIE, it means creating a Top-Level ASBIEP (Association Business Information Entity Property)
    along with its related ASBIEP and ABIE (Aggregation Business Information Entity). This function creates the necessary
    database records in the correct order:
    1. Creates top_level_asbiep without asbiep_id initially
    2. Creates biz_ctx_assignment records for each business context
    3. Creates abie record
    4. Creates asbiep record
    5. Updates top_level_asbiep with the asbiep_id
    6. Recursively processes mandatory relationships (cardinality_min >= 1) for the created ABIE and all nested ABIEs,
       automatically creating/enabling required ASBIEs, BBIEs, and BBIE SCs

    Args:
        asccp_manifest_id (int): The ASCCP (Association Core Component Property) manifest ID to base the Top-Level ASBIEP on.
            Must be a positive integer. Use 'get_core_components' to find ASCCP manifest IDs.
        biz_ctx_list (str): Business context IDs to assign to the Top-Level ASBIEP. Comma-separated list of business context IDs.
            Examples: '1', '1,2', '1,2,3'. Must contain at least one positive integer. Use 'get_business_contexts' to find existing contexts
            or 'create_business_context' to create new ones.

    Returns:
        CreateTopLevelAsbiepResponse: Response object containing:
            - top_level_asbiep_id: ID of the newly created top-level ASBIEP
            - asbiep: ASBIEP structure with role_of_abie containing abie_id.
              Shows the hierarchical structure with asbiep_id and role_of_abie.
              Excludes remark and is_nillable fields from relationships.
              None if the created top-level ASBIEP doesn't have an ASBIEP or role_of_abie.

    Raises:
        ToolError: If validation fails, database errors occur, or the ASCCP (Association Core Component Property) manifest doesn't exist.
        Specifically raises 400 error if the ASCCP's role_of_acc is a group type (SemanticGroup or UserExtensionGroup).

    Examples:
        Create a simple Top-Level ASBIEP (BIE):
        >>> result = create_top_level_asbiep(asccp_manifest_id=123, biz_ctx_list="1,2")
        >>> print(f"Created Top-Level ASBIEP with top_level_asbiep_id: {result.top_level_asbiep_id}")

        Create a Top-Level ASBIEP (BIE) with multiple business contexts:
        >>> result = create_top_level_asbiep(asccp_manifest_id=456, biz_ctx_list="3,4,5")
        >>> print(f"Created Top-Level ASBIEP with top_level_asbiep_id: {result.top_level_asbiep_id}")

    Note:
        Before calling this function, you may need to:
        1. Find the ASCCP manifest ID: get_core_components(release_id=<id>, types=['ASCCP'], name='<search_term>')
        2. Find business context IDs: get_business_contexts(offset=0, limit=10)
        3. Create new business context: create_business_context(name='<context_name>')
    """
    try:
        payload = await business_information_entity_service.create_top_level_asbiep(
            asccp_manifest_id=asccp_manifest_id,
            biz_ctx_list=_parse_required_int_list(biz_ctx_list, field_name="biz_ctx_list"),
        )
        return CreateTopLevelAsbiepResponse.model_validate(payload, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to create the top-level ASBIEP.") from exc


@mcp.tool(
    name="update_top_level_asbiep",
    description="Update a Top-Level ASBIEP (Association Business Information Entity Property) with new version, status, or deprecation information. IMPORTANT: The 'version' parameter MUST be provided as a string and the exact string value must be preserved (e.g., '1.0' must NOT be converted to '1' or integer 1). The version format is user-defined and the exact string value must be stored as-is.",
    output_schema={
        "type": "object",
        "description": "Response containing the updated Top-Level ASBIEP information.",
        "properties": {
            "top_level_asbiep_id": {"type": "integer",
                                    "description": "ID of the updated Top-Level ASBIEP (Association Business Information Entity Property)"},
            "updates": {"type": "array", "items": {"type": "string"},
                        "description": "A list of updated fields, each represented by its name",
                        "example": ["version", "status", "is_deprecated"]}
        },
        "required": ["top_level_asbiep_id", "updates"]
    }
)
async def update_top_level_asbiep(
    top_level_asbiep_id: Annotated[int, Field(gt=0, description="Target top-level ASBIEP identifier.")],
    version: Annotated[str | None, Field(default=None, description="New version string to store as-is. Omit to leave unchanged.")],
    status: Annotated[str | None, Field(default=None, description="New status value. Omit to leave unchanged.")],
    display_name: Annotated[str | None, Field(default=None, description="Display name override. Omit to leave unchanged.")],
    biz_term: Annotated[str | None, Field(default=None, description="Business term override. Omit to leave unchanged.")],
    definition: Annotated[str | None, Field(default=None, description="ASBIEP definition override. Omit to leave unchanged.")],
    remark: Annotated[str | None, Field(default=None, description="ASBIEP remark override. Omit to leave unchanged.")],
    is_deprecated: Annotated[bool | str | None, Field(default=None, description="Deprecation flag. Accepts bool values or 'true'/'false' strings. Omit to leave unchanged.")],
    deprecated_reason: Annotated[str | None, Field(default=None, description="Deprecation reason. Omit to leave unchanged.")],
    deprecated_remark: Annotated[str | None, Field(default=None, description="Deprecation remark. Omit to leave unchanged.")],
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> UpdateTopLevelAsbiepResponse:
    """
    Update a Top-Level ASBIEP (Association Business Information Entity Property) with new version, status, deprecation information, and ASBIEP properties.

    This function allows updating specific fields of a Top-Level ASBIEP (which represents a complete BIE - Business Information Entity) including:
    - version: Version number assigned by the user. MUST be provided as a string and the exact string value must be preserved (e.g., '1.0' must NOT be converted to '1' or integer 1). The version format is user-defined and the exact string value must be stored as-is.
    - status: Usage status (e.g., 'Prototype', 'Test', 'Production')
    - is_deprecated: Whether the Top-Level ASBIEP is deprecated
    - deprecated_reason: Reason for deprecation (required if deprecating)
    - deprecated_remark: Additional deprecation remarks (optional)
    - biz_term: Business term to indicate what the BIE is called in a particular business context
    - definition: Definition of the ASBIEP
    - remark: Context-specific usage remarks about the BIE
    - display_name: Display name of the ASBIEP

    Permission Requirements:
    - The current user must be the owner of the Top-Level ASBIEP
    - The Top-Level ASBIEP state must be 'WIP' (Work In Progress)

    Args:
        top_level_asbiep_id (int): The top-level ASBIEP (Association Business Information Entity Property) ID of the Top-Level ASBIEP to update.
        version (str | None, optional): New version number. MUST be provided as a string and the exact string value must be preserved (e.g., '1.0' must NOT be converted to '1' or integer 1). The version format is user-defined and the exact string value must be stored as-is. If not provided, version will not be updated.
        status (str | None, optional): New status. If not provided, status will not be updated.
        is_deprecated (bool | str | None, optional): New deprecation status. Accepts bool, str ('True'/'true'/'1' for True, 'False'/'false'/'0' for False), or None. If not provided, deprecation status will not be updated.
        deprecated_reason (str | None, optional): Reason for deprecation. Required if is_deprecated is True.
        deprecated_remark (str | None, optional): Additional deprecation remarks.
        biz_term (str | None, optional): New business term. If not provided, biz_term will not be updated.
        definition (str | None, optional): New definition. If no provided, definition will not be updated.
        remark (str | None, optional): New remark. If not provided, remark will not be updated.
        display_name (str | None, optional): New display name. If not provided, display_name will not be updated.

    Returns:
        UpdateTopLevelAsbiepResponse: Response object containing:
            - top_level_asbiep_id: ID of the updated Top-Level ASBIEP (Association Business Information Entity Property)
            - updates: List of fields that were updated (e.g., ["version", "status", "biz_term", "remark"])

    Raises:
        ToolError: If validation fails, the Top-Level ASBIEP is not found, user lacks permission, Top-Level ASBIEP state is not 'WIP',
            or database errors occur. Common error scenarios include:
            - Invalid top-level ASBIEP (Association Business Information Entity Property) ID
            - Top-Level ASBIEP not found (404 error)
            - Access denied - not owner (403 error)
            - Top-Level ASBIEP state is not 'WIP' (400 error)
            - Deprecation reason required when deprecating (400 error)
            - No fields provided for update (400 error)
            - Database connection issues
            - Authentication failures

    Example:
        Update version and status (note: version MUST be a string, preserve exact format like "1.0", not 1):
        >>> result = await update_top_level_asbiep(
        ...     top_level_asbiep_id=123,
        ...     version="1.0",  # String "1.0", NOT integer 1 or string "1"
        ...     status="Production"
        ... )
        >>> print(f"Updated fields: {result.updates}")

        Update to version 2.0:
        >>> result = await update_top_level_asbiep(
        ...     top_level_asbiep_id=123,
        ...     version="2.0",  # String "2.0", NOT integer 2
        ...     status="Production"
        ... )
        >>> print(f"Updated fields: {result.updates}")

        Update ASBIEP properties:
        >>> result = await update_top_level_asbiep(
        ...     top_level_asbiep_id=123,
        ...     biz_term="Product Master",
        ...     remark="Used for manufacturing operations",
        ...     display_name="Item Master"
        ... )
        >>> print(f"Updated fields: {result.updates}")

        Deprecate a Top-Level ASBIEP:
        >>> result = await update_top_level_asbiep(
        ...     top_level_asbiep_id=123,
        ...     is_deprecated=True,
        ...     deprecated_reason="Replaced by new version"
        ... )
        >>> print(f"Updated fields: {result.updates}")

    Note:
        This operation only works when the Top-Level ASBIEP state is 'WIP'. For state transitions (WIP->QA, QA->WIP/Production),
        use the update_top_level_asbiep_state function.
    """
    try:
        is_deprecated = str_to_bool(is_deprecated)
    except ToolError:
        raise
    except Exception as e:
        raise ToolError(
            f"Type conversion error: {str(e)}. Please check your parameter types and try again."
        ) from e

    try:
        payload = await business_information_entity_service.update_top_level_asbiep(
            top_level_asbiep_id=top_level_asbiep_id,
            version=version,
            status=status,
            display_name=display_name,
            biz_term=biz_term,
            definition=definition,
            remark=remark,
            is_deprecated=is_deprecated,
            deprecated_reason=deprecated_reason,
            deprecated_remark=deprecated_remark,
        )
        return UpdateTopLevelAsbiepResponse.model_validate(payload, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to update top-level ASBIEP {top_level_asbiep_id}.") from exc


@mcp.tool(
    name="update_top_level_asbiep_state",
    description="Update the state of a Top-Level ASBIEP (Association Business Information Entity Property) following the state transition rules",
    output_schema={
        "type": "object",
        "properties": {
            "top_level_asbiep_id": {"type": "integer",
                                    "description": "ID of the Top-Level ASBIEP (Association Business Information Entity Property)"},
            "updates": {"type": "array", "items": {"type": "string"},
                        "description": "A list of updated fields, each represented by its name", "example": ["state"]}
        },
        "required": ["top_level_asbiep_id", "updates"]
    }
)
async def update_top_level_asbiep_state(
    top_level_asbiep_id: Annotated[int, Field(gt=0, description="Target top-level ASBIEP identifier.")],
    new_state: Annotated[str, Field(description="Target lifecycle state. Valid values are 'WIP', 'QA', and 'Production'.")],
    ctx: Context,
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> UpdateTopLevelAsbiepStateToolResponse:
    """
    Update the state of a BIE (Business Information Entity) following the state transition rules.

    This function allows updating the state of a BIE following these rules:
    - If BIE state is 'WIP', it can transition to 'QA'
    - If BIE state is 'QA', it can transition to either 'WIP' (back) or 'Production'
    - If BIE state is 'Production', it cannot be changed

    Permission Requirements:
    - The current user must be the owner of the BIE
    - The state transition must be valid according to the rules above

    Args:
        top_level_asbiep_id (int): The top-level ASBIEP (Association Business Information Entity Property) ID of the BIE to update state.
        new_state (str): The new state for the BIE. Valid values: 'QA', 'WIP', 'Production'

    Returns:
        UpdateTopLevelAsbiepResponse: Response object containing:
            - top_level_asbiep_id: ID of the BIE (Business Information Entity)
            - updates: List of fields that were updated (will contain "state")

    Raises:
        ToolError: If validation fails, the BIE is not found, user lacks permission, invalid state transition,
            or database errors occur. Common error scenarios include:
            - Invalid top-level ASBIEP (Association Business Information Entity Property) ID
            - Business information entity not found (404 error)
            - Access denied - not owner (403 error)
            - Invalid state transition (400 error)
            - BIE state is 'Production' and cannot be changed (400 error)
            - Database connection issues
            - Authentication failures

    Example:
        Transition from WIP to QA:
        >>> result = await update_bie_state(
        ...     top_level_asbiep_id=123,
        ...     new_state="QA"
        ... )
        >>> print(f"Updated fields: {result.updates}")

        Transition from QA to Production:
        >>> result = await update_bie_state(
        ...     top_level_asbiep_id=123,
        ...     new_state="Production"
        ... )
        >>> print(f"Updated fields: {result.updates}")

        Transition from QA back to WIP:
        >>> result = await update_bie_state(
        ...     top_level_asbiep_id=123,
        ...     new_state="WIP"
        ... )
        >>> print(f"Updated fields: {result.updates}")

    Note:
        This operation is separate from updating other BIE fields (version, status, etc.) which can only be done
        when the BIE state is 'WIP'. State transitions are independent of field updates and can be performed
        regardless of the current field values.
    """
    try:
        valid_states = {"WIP", "QA", "Production"}
        if new_state not in valid_states:
            raise ToolError(
                f"Invalid state '{new_state}'. Valid states are: {', '.join(sorted(valid_states))}."
            )

        row = await business_information_entity_service.get_top_level_asbiep(
            top_level_asbiep_id=top_level_asbiep_id,
        )
        if row is None:
            raise ToolError(
                f"The top-level ASBIEP with ID {top_level_asbiep_id} was not found. Please check the ID and try again."
            )

        if row.state == "QA" and new_state == "Production":
            elicit_result = await ctx.elicit(
                message=(
                    f"Are you sure you want to move '{_top_level_label(row)}' to the 'Production' state?\n\n"
                    "This action is permanent. Once in Production, the state cannot be changed."
                ),
                response_type=None,
            )
            match elicit_result:
                case AcceptedElicitation():
                    pass
                case DeclinedElicitation():
                    raise ToolError("State update declined by user.")
                case CancelledElicitation():
                    raise ToolError("State update cancelled by user.")

        payload = await business_information_entity_service.update_top_level_asbiep_state(
            top_level_asbiep_id=top_level_asbiep_id,
            state=new_state,
        )
        return UpdateTopLevelAsbiepStateToolResponse.model_validate(payload, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to update the state of top-level ASBIEP {top_level_asbiep_id}.") from exc


@mcp.tool(
    name="delete_top_level_asbiep",
    description="Delete a Top-Level ASBIEP (Association Business Information Entity Property) and all related records",
    output_schema={
        "type": "object",
        "description": "Response containing the deleted top-level ASBIEP ID or cancellation message",
        "properties": {
            "top_level_asbiep_id": {"type": ["integer", "null"],
                                    "description": "ID of the deleted top-level ASBIEP (Association Business Information Entity Property) (null if deletion was cancelled)"},
            "message": {"type": ["string", "null"],
                        "description": "Optional message indicating the status of the deletion operation",
                        "example": "Deletion cancelled by user"}
        },
        "required": []
    }
)
async def delete_top_level_asbiep(
    top_level_asbiep_id: Annotated[int, Field(gt=0, description="Target top-level ASBIEP identifier.")],
    ctx: Context,
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> DeleteTopLevelAsbiepToolResponse:
    """
    Delete a Top-Level ASBIEP (Association Business Information Entity Property) and all related records.

    This function permanently removes a Top-Level ASBIEP (which represents a complete BIE - Business Information Entity)
    from the database along with all its related records including the associated ASBIEP, ABIE, and all child entities.
    This action is irreversible and should be used with caution.

    The deletion process follows this order:
    1. Delete biz_ctx_assignment records
    2. Delete abie records (using owner_top_level_asbiep_id)
    3. Delete asbie records (using owner_top_level_asbiep_id)
    4. Delete bbie records (using owner_top_level_asbiep_id)
    5. Delete asbiep_support_doc records (for each asbiep)
    6. Delete asbiep records (using owner_top_level_asbiep_id)
    7. Delete bbiep records (using owner_top_level_asbiep_id)
    8. Delete bbie_sc records (using owner_top_level_asbiep_id)
    9. Delete top_level_asbiep record

    Foreign key checks are temporarily disabled during the deletion process to ensure
    proper cleanup of all related records.

    Permission Requirements:
    - If the current user is an admin, it can be discarded
    - If the current user is the owner and the state is not "Production", then it can be discarded
    - Otherwise, it cannot be discarded

    Args:
        top_level_asbiep_id (int): The top-level ASBIEP (Association Business Information Entity Property) ID of the Top-Level ASBIEP to delete.

    Returns:
        DeleteBusinessInformationEntityResponse: Response object containing:
            - top_level_asbiep_id: ID of the deleted top-level ASBIEP (Association Business Information Entity Property)

    Raises:
        ToolError: If validation fails, the Top-Level ASBIEP is not found, user lacks
            permission, or database errors occur. Common error scenarios include:
            - Invalid top-level ASBIEP (Association Business Information Entity Property) ID
            - Top-Level ASBIEP not found (404 error)
            - Access denied - not owner or admin (403 error)
            - Access denied - Production state discard requires admin (403 error)
            - Database connection issues
            - Authentication failures

    Example:
        >>> result = await delete_top_level_asbiep(top_level_asbiep_id=123)
        >>> print(f"Deleted Top-Level ASBIEP with top-level ASBIEP (Association Business Information Entity Property) ID: {result.top_level_asbiep_id}")
        Deleted Top-Level ASBIEP with top-level ASBIEP (Association Business Information Entity Property) ID: 123

    Warning:
        This operation is permanent and cannot be undone. All related records including
        biz_ctx_assignment, abie, asbie, bbie, asbiep_support_doc, asbiep, bbiep, and bbie_sc records will
        be permanently deleted.
    """
    try:
        row = await business_information_entity_service.get_top_level_asbiep(
            top_level_asbiep_id=top_level_asbiep_id,
        )
        if row is None:
            raise ToolError(
                f"The top-level ASBIEP with ID {top_level_asbiep_id} was not found. Please check the ID and try again."
            )

        await business_information_entity_service.ensure_delete_top_level_asbiep_allowed(
            top_level_asbiep_id=top_level_asbiep_id,
        )

        elicit_result = await ctx.elicit(
            message=(
                f"Are you sure you want to discard '{_top_level_label(row)}' top-level ASBIEP?\n\n"
                "It will be permanently removed along with its owned BIE tree."
            ),
            response_type=None,
        )

        match elicit_result:
            case AcceptedElicitation():
                await business_information_entity_service.execute_delete_top_level_asbiep(
                    top_level_asbiep_id=top_level_asbiep_id,
                )
                return DeleteTopLevelAsbiepToolResponse(top_level_asbiep_id=top_level_asbiep_id)
            case DeclinedElicitation():
                return DeleteTopLevelAsbiepToolResponse(
                    top_level_asbiep_id=None,
                    message="Deletion declined by user",
                )
            case CancelledElicitation():
                return DeleteTopLevelAsbiepToolResponse(
                    top_level_asbiep_id=None,
                    message="Deletion cancelled by user",
                )
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to delete top-level ASBIEP {top_level_asbiep_id}.") from exc


@mcp.tool(
    name="transfer_top_level_asbiep_ownership",
    description="Transfer ownership of a Top-Level ASBIEP (Association Business Information Entity Property) to another user",
    output_schema={
        "type": "object",
        "properties": {
            "top_level_asbiep_id": {"type": "integer",
                                    "description": "ID of the Top-Level ASBIEP (Association Business Information Entity Property)"},
            "updates": {"type": "array", "items": {"type": "string"},
                        "description": "A list of updated fields, each represented by its name",
                        "example": ["owner_user_id"]}
        },
        "required": ["top_level_asbiep_id", "updates"]
    }
)
async def transfer_top_level_asbiep_ownership(
    top_level_asbiep_id: Annotated[int, Field(gt=0, description="Target top-level ASBIEP identifier.")],
    new_owner_user_id: Annotated[int, Field(gt=0, description="User ID of the new owner.")],
    ctx: Context,
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
    app_user_service: AppUserService = Depends(get_app_user_service),
) -> TransferTopLevelAsbiepOwnershipResponse:
    """
    Transfer ownership of a Top-Level ASBIEP (Association Business Information Entity Property) to another user.

    This function transfers ownership of a Top-Level ASBIEP (which represents a complete BIE - Business Information Entity)
    from the current owner to a new owner. The transfer is subject to the following rules:

    1. Permission checks:
       - If the current user is an admin, they can transfer ownership
       - If the current user is the owner, they can transfer ownership
       - Otherwise, access is denied

    2. Role compatibility:
       - If current owner is 'End-User', new owner must be 'End-User'
       - If current owner is 'Developer', new owner must be 'Developer'
       - If current owner is 'Admin', new owner must be 'Admin'

    Args:
        top_level_asbiep_id (int): The top-level ASBIEP (Association Business Information Entity Property) ID of the Top-Level ASBIEP to transfer ownership.
        new_owner_user_id (int): The user ID of the new owner who will receive ownership of the Top-Level ASBIEP.

    Returns:
        TransferOwnershipResponse: Response object containing:
            - top_level_asbiep_id: ID of the Top-Level ASBIEP (Association Business Information Entity Property)
            - updates: List of fields that were updated (will contain "owner_user_id")

    Raises:
        ToolError: If validation fails, the Top-Level ASBIEP is not found, user lacks
            permission, role mismatch, or database errors occur. Common error scenarios include:
            - Invalid top-level ASBIEP (Association Business Information Entity Property) ID or new owner user ID
            - Top-Level ASBIEP not found (404 error)
            - Current or new owner not found (404 error)
            - Access denied - not owner or admin (403 error)
            - Role mismatch between current and new owner (400 error)
            - New owner is disabled (400 error)
            - Cannot transfer ownership to yourself (400 error)
            - Database connection issues
            - Authentication failures

    Example:
        >>> result = await transfer_top_level_asbiep_ownership(top_level_asbiep_id=123, new_owner_user_id=2)
        >>> print(f"Transferred ownership of Top-Level ASBIEP with ID: {result.top_level_asbiep_id}")
        Transferred ownership of Top-Level ASBIEP with ID: 123

    Note:
        This operation updates the ownership and last update timestamp of the Top-Level ASBIEP.
        The new owner will have full control over the Top-Level ASBIEP and all its related entities according to their role permissions.
    """
    try:
        row = await business_information_entity_service.get_top_level_asbiep(
            top_level_asbiep_id=top_level_asbiep_id,
        )
        if row is None:
            raise ToolError(
                f"The top-level ASBIEP with ID {top_level_asbiep_id} was not found. Please check the ID and try again."
            )

        target_user = await app_user_service.get(new_owner_user_id)
        if target_user is None:
            raise ToolError(
                f"The target user with ID {new_owner_user_id} was not found. Please check the ID and try again."
            )
        target_user_label = target_user.login_id
        if target_user.username and target_user.username != target_user.login_id:
            target_user_label = f"{target_user.login_id} ({target_user.username})"

        elicit_result = await ctx.elicit(
            message=(
                f"Are you sure you want to transfer ownership of '{_top_level_label(row)}' "
                f"to {target_user_label}?"
            ),
            response_type=None,
        )

        match elicit_result:
            case AcceptedElicitation():
                payload = await business_information_entity_service.transfer_top_level_asbiep_ownership(
                    top_level_asbiep_id=top_level_asbiep_id,
                    target_user_id=new_owner_user_id,
                )
                updates = [] if int(new_owner_user_id) == int(row.owner.user_id) else ["owner_user_id"]
                return TransferTopLevelAsbiepOwnershipResponse(
                    top_level_asbiep_id=payload.top_level_asbiep_id,
                    updates=updates,
                )
            case DeclinedElicitation():
                raise ToolError("Ownership transfer declined by user.")
            case CancelledElicitation():
                raise ToolError("Ownership transfer cancelled by user.")
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to transfer ownership of top-level ASBIEP {top_level_asbiep_id}.") from exc


@mcp.tool(
    name="assign_biz_ctx_to_top_level_asbiep",
    description="Assign a business context to a Top-Level ASBIEP (Association Business Information Entity Property)",
    output_schema={
        "type": "object",
        "properties": {
            "top_level_asbiep_id": {"type": "integer", "description": "ID of the BIE (Business Information Entity)"},
            "updates": {"type": "array", "items": {"type": "string"},
                        "description": "A list of updated fields, each represented by its name",
                        "example": ["business_contexts"]}
        },
        "required": ["top_level_asbiep_id", "updates"]
    }
)
async def assign_biz_ctx_to_top_level_asbiep(
    top_level_asbiep_id: Annotated[int, Field(gt=0, description="Target top-level ASBIEP identifier.")],
    biz_ctx_id: Annotated[int, Field(gt=0, description="Business context identifier to assign.")],
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> AssignBizCtxToTopLevelAsbiepToolResponse:
    """
    Assign a business context to a BIE (Business Information Entity).

    This function creates a new business context assignment for a BIE (Business Information Entity).
    If the assignment already exists, the request will be denied with an error.

    Permission Requirements:
    - The current user must be the owner of the BIE
    - The BIE state must be 'WIP' (Work In Progress)

    Args:
        top_level_asbiep_id (int): The top-level ASBIEP (Association Business Information Entity Property) ID of the BIE to assign the business context to.
        biz_ctx_id (int): The business context ID to assign to the BIE.

    Returns:
        UpdateTopLevelAsbiepResponse: Response object containing:
            - top_level_asbiep_id: ID of the BIE (Business Information Entity)
            - updates: List of fields that were updated (will contain "business_contexts")

    Raises:
        ToolError: If validation fails, the BIE or business context is not found, user lacks permission,
            BIE state is not 'WIP', duplicate assignment exists, or database errors occur. Common error scenarios include:
            - Invalid top-level ASBIEP (Association Business Information Entity Property) ID or business context ID
            - Business information entity not found (404 error)
            - Business context not found (404 error)
            - Access denied - not owner (403 error)
            - BIE state is not 'WIP' (400 error)
            - Duplicate assignment - business context already assigned (400 error)
            - Database connection issues
            - Authentication failures

    Example:
        Assign a business context to a BIE:
        >>> result = await assign_biz_ctx_to_bie(
        ...     top_level_asbiep_id=123,
        ...     biz_ctx_id=1
        ... )
        >>> print(f"Updated fields: {result.updates}")

    Note:
        This operation only works when the BIE state is 'WIP'. If the business context is already assigned
        to the Top-Level ASBIEP, the request will be denied with a 400 error.
    """
    try:
        payload = await business_information_entity_service.assign_biz_ctx_to_top_level_asbiep(
            top_level_asbiep_id=top_level_asbiep_id,
            biz_ctx_id=biz_ctx_id,
        )
        updates = ["business_contexts"] if payload is not None else []
        return AssignBizCtxToTopLevelAsbiepToolResponse(
            top_level_asbiep_id=top_level_asbiep_id,
            updates=updates,
        )
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to assign business context {biz_ctx_id}.") from exc


@mcp.tool(
    name="unassign_biz_ctx_from_top_level_asbiep",
    description="Unassign a business context from a Top-Level ASBIEP (Association Business Information Entity Property)",
    output_schema={
        "type": "object",
        "properties": {
            "top_level_asbiep_id": {"type": "integer", "description": "ID of the BIE (Business Information Entity)"},
            "updates": {"type": "array", "items": {"type": "string"},
                        "description": "A list of updated fields, each represented by its name",
                        "example": ["business_contexts"]}
        },
        "required": ["top_level_asbiep_id", "updates"]
    }
)
async def unassign_biz_ctx_from_top_level_asbiep(
    top_level_asbiep_id: Annotated[int, Field(gt=0, description="Target top-level ASBIEP identifier.")],
    biz_ctx_id: Annotated[int, Field(gt=0, description="Business context identifier to remove.")],
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> UnassignBizCtxFromTopLevelAsbiepToolResponse:
    """
    Unassign a business context from a BIE (Business Information Entity).

    This function removes a business context assignment from a BIE (Business Information Entity).
    If the assignment doesn't exist, it will return a success message without error.

    Permission Requirements:
    - The current user must be the owner of the BIE
    - The BIE state must be 'WIP' (Work In Progress)

    Args:
        top_level_asbiep_id (int): The top-level ASBIEP (Association Business Information Entity Property) ID of the BIE to unassign the business context from.
        biz_ctx_id (int): The business context ID to unassign from the BIE.

    Returns:
        UpdateTopLevelAsbiepResponse: Response object containing:
            - top_level_asbiep_id: ID of the BIE (Business Information Entity)
            - updates: List of fields that were updated (will contain "business_contexts")

    Raises:
        ToolError: If validation fails, the BIE or business context is not found, user lacks permission,
            BIE state is not 'WIP', or database errors occur. Common error scenarios include:
            - Invalid top-level ASBIEP (Association Business Information Entity Property) ID or business context ID
            - Business information entity not found (404 error)
            - Business context not found (404 error)
            - Access denied - not owner (403 error)
            - BIE state is not 'WIP' (400 error)
            - Database connection issues
            - Authentication failures

    Example:
        Unassign a business context from a BIE:
        >>> result = await unassign_biz_ctx_from_bie(
        ...     top_level_asbiep_id=123,
        ...     biz_ctx_id=1
        ... )
        >>> print(f"Updated fields: {result.updates}")

    Note:
        This operation only works when the BIE state is 'WIP'. If the assignment doesn't exist,
        the operation will succeed without error.
    """
    try:
        await business_information_entity_service.unassign_biz_ctx_from_top_level_asbiep(
            top_level_asbiep_id=top_level_asbiep_id,
            biz_ctx_id=biz_ctx_id,
        )
        return UnassignBizCtxFromTopLevelAsbiepToolResponse(
            top_level_asbiep_id=top_level_asbiep_id,
            updates=["business_contexts"],
        )
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to remove business context {biz_ctx_id}.") from exc


@mcp.tool(
    name="create_asbie",
    description="Enable (use) an ASBIE (Association Business Information Entity) during BIE profiling. Creates a new ASBIE with associated ASBIEP and ABIE records, enabling the component in the BIE. The ASBIE is automatically enabled (is_used=True) and profiled. IMPORTANT: The based_ascc_manifest_id must point to an ASCC that associates with a non-group ACC (component_type must not be 3=SemanticGroup or 4=UserExtensionGroup). Groups are automatically skipped in BIE expressions and cannot be created directly. Always use the exact ascc_manifest_id obtained from get_top_level_asbiep() or get_asbie_by_*() tools, which show only non-group relationships. Either from_abie_id or asbiep_id must be provided. If both are provided, the tool will validate that asbiep.role_of_abie_id == from_abie_id. If only asbiep_id is provided, from_abie_id will be automatically fetched from asbiep.role_of_abie_id. Either based_ascc_manifest_id or property_term must be provided. If property_term is provided, the tool will search through the ABIE's relationships to find a matching ASCCP by property_term. After creation, this tool automatically processes mandatory relationships (cardinality_min >= 1) recursively, creating or enabling required ASBIE and BBIE components to satisfy cardinality constraints.",
    output_schema={
        "type": "object",
        "description": "Response containing the newly created and enabled ASBIE information. The ASBIE is automatically enabled (is_used=True) for BIE profiling. IMPORTANT: The based_ascc_manifest_id must point to an ASCC that associates with a non-group ACC (component_type must not be 3=SemanticGroup or 4=UserExtensionGroup). Groups are automatically skipped in BIE expressions and cannot be created directly. Always use the exact ascc_manifest_id obtained from get_top_level_asbiep() or get_asbie_by_*() tools. All mandatory relationships (cardinality_min >= 1) are automatically created or enabled recursively. The 'asbiep' field contains a simplified recursive structure showing all created/enabled ASBIEs and BBIEs (excludes is_nillable, remark).",
        "properties": {
            "asbie_id": {"type": "integer", "description": "Unique identifier of the newly created and enabled ASBIE",
                         "example": 12345},
            "asbiep": {"type": ["object", "null"],
                       "description": "Simplified recursive structure containing ASBIEP and all created/enabled relationships. Shows the hierarchical structure of all ASBIEs and BBIEs that were automatically created or enabled during mandatory relationship processing. Excludes is_nillable and remark fields.",
                       "properties": {
                           "asbiep_id": {"type": "integer", "description": "Unique identifier of the ASBIEP",
                                         "example": 12346},
                           "role_of_abie": {"type": ["object", "null"],
                                            "description": "The ABIE that this ASBIEP points to, with its relationships",
                                            "properties": {
                                                "abie_id": {"type": "integer",
                                                            "description": "Unique identifier of the ABIE",
                                                            "example": 12347},
                                                "relationships": {"type": "array",
                                                                  "description": "List of relationships (ASBIEs and BBIEs) from this ABIE",
                                                                  "items": {
                                                                      "type": "object",
                                                                      "description": "A relationship that can be either an ASBIE or BBIE",
                                                                      "properties": {
                                                                          "asbie": {"type": ["object", "null"],
                                                                                    "description": "ASBIE relationship (if this is an ASBIE). Excludes is_nillable and remark fields.",
                                                                                    "properties": {
                                                                                        "asbie_id": {"type": "integer"},
                                                                                        "guid": {
                                                                                            "type": ["string", "null"]},
                                                                                        "cardinality_min": {
                                                                                            "type": "integer"},
                                                                                        "cardinality_max": {
                                                                                            "type": "integer"},
                                                                                        "based_ascc": {
                                                                                            "type": "object"},
                                                                                        "asbiep": {
                                                                                            "type": ["object", "null"],
                                                                                            "description": "Recursive structure - ASBIEP with its role_of_abie and relationships"}
                                                                                    }},
                                                                          "bbie": {"type": ["object", "null"],
                                                                                   "description": "BBIE relationship (if this is a BBIE). Excludes remark field.",
                                                                                   "properties": {
                                                                                       "bbie_id": {"type": "integer"},
                                                                                       "guid": {
                                                                                           "type": ["string", "null"]},
                                                                                       "cardinality_min": {
                                                                                           "type": "integer"},
                                                                                       "cardinality_max": {
                                                                                           "type": "integer"},
                                                                                       "is_nillable": {
                                                                                           "type": "boolean"},
                                                                                       "based_bcc": {"type": "object"}
                                                                                   }}
                                                                      }
                                                                  }}
                                            }}
                       }}
        },
        "required": ["asbie_id"]
    }
)
async def create_asbie(
    from_abie_id: Annotated[int, Field(gt=0, description="Parent ABIE identifier.")],
    based_ascc_manifest_id: Annotated[int, Field(gt=0, description="Based ASCC manifest identifier.")],
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> CreateAsbieToolResponse:
    """
    Enable (use) an ASBIE (Association Business Information Entity) during BIE profiling.

    This tool is used to enable (use) an ASBIE component during BIE profiling. It creates a new ASBIE
    with associated ASBIEP and ABIE records and automatically enables it (is_used=True) for use in the BIE.

    After creating the ASBIE, this tool automatically performs cardinality constraint analysis on the
    role_of_abie ABIE's relationships. For each mandatory relationship (cardinality_min >= 1), the tool:
    - If the relationship already exists (has asbie_id/bbie_id) but is disabled (is_used=False),
      it automatically enables it by setting is_used=True.
    - If the relationship doesn't exist, it automatically creates it.
    - After creating or enabling a relationship, it recursively processes the role_of_abie's
      relationships to ensure all mandatory cardinality constraints are satisfied.

    This recursive processing ensures that the BIE structure automatically satisfies all cardinality
    constraints defined in the Core Component specification without requiring manual follow-up operations.

    This is the primary tool for enabling/profiling ASBIE components. The created ASBIE is automatically
    enabled and ready for use in the BIE. Use update_asbie to toggle enable/disable (use/unuse) or
    modify other properties after creation.

    IMPORTANT - Group Validation:
    The based_ascc_manifest_id must point to an ASCC that associates with a non-group ACC.
    If the ASCC's to_asccp_manifest.role_of_acc_manifest.acc.component_type is 3 (SemanticGroup) or 4 (UserExtensionGroup),
    this tool will raise a ToolError. Groups are automatically skipped in BIE expressions and cannot be created directly.
    Always use the exact ascc_manifest_id obtained from get_top_level_asbiep() or get_asbie_by_*() tools,
    which automatically skip groups and show only non-group relationships in the BIE structure.

    Permission Requirements:
    - The current user must be the owner of the top-level ASBIEP
    - The top-level ASBIEP state must be 'WIP' (Work In Progress)

    Args:
        from_abie_id (int | str | None): The ABIE ID that this ASBIE originates from (parent ABIE).
            Either from_abie_id or asbiep_id must be provided. If both are provided, the tool will
            validate that asbiep.role_of_abie_id == from_abie_id. Accepts int, str, or None.
            String values are automatically converted to integers.
        asbiep_id (int | str | None): The ASBIEP ID to use for determining the parent ABIE.
            Either from_abie_id or asbiep_id must be provided. If both are provided, the tool will
            validate that asbiep.role_of_abie_id == from_abie_id. If only asbiep_id is provided,
            from_abie_id will be automatically fetched from asbiep.role_of_abie_id. Accepts int, str, or None.
            String values are automatically converted to integers.
        based_ascc_manifest_id (int | str | None): The ASCC manifest ID that this ASBIE is based on.
            Must point to an ASCC that associates with a non-group ACC (component_type must not be 3 or 4).
            Always use the exact ascc_manifest_id obtained from get_top_level_asbiep() or get_asbie_by_*() tools.
            If not provided, property_term must be provided instead.
        property_term (str | None): The property term of the ASCCP to search for in the relationships.
            If provided and based_ascc_manifest_id is None, this tool will search through the ABIE's relationships
            to find a matching ASCCP by property_term and extract the based_ascc_manifest_id from it.

    Returns:
        CreateAsbieResponse: Response object containing:
            - asbie_id: ID of the newly created and enabled ASBIE
            - asbiep: Simplified recursive structure containing ASBIEP and all created/enabled relationships.
              Shows the hierarchical structure of all ASBIEs and BBIEs that were automatically
              created or enabled during mandatory relationship processing. Excludes is_nillable and remark fields.
              The structure follows:
              asbiep -> role_of_abie -> relationships (list of ASBIE/BBIE) -> (for ASBIEs) asbiep -> ...
              None if the created ASBIE doesn't have an ASBIEP or role_of_abie.

    Raises:
        ToolError: If validation fails, resources are not found, user lacks permission,
            or database errors occur. Specifically raises an error if the ASCC points to a group ACC
            (component_type 3=SemanticGroup or 4=UserExtensionGroup), indicating that the exact
            ascc_manifest_id from get_top_level_asbiep() or get_asbie_by_*() tools should be used instead.

    Examples:
        Enable an ASBIE during BIE profiling using based_ascc_manifest_id (mandatory relationships are automatically created/enabled):
        >>> result = create_asbie(from_abie_id=123, based_ascc_manifest_id=456)
        >>> print(f"Created ASBIE ID: {result.asbie_id}")

        Enable an ASBIE using property_term to search for the relationship:
        >>> result = create_asbie(from_abie_id=123, property_term="Details")
        >>> print(f"Created ASBIE ID: {result.asbie_id}")

        Enable an ASBIE using asbiep_id to determine the parent ABIE:
        >>> result = create_asbie(asbiep_id=12346, based_ascc_manifest_id=456)
        >>> print(f"Created ASBIE ID: {result.asbie_id}")
    """
    try:
        payload = await business_information_entity_service.create_asbie(
            from_abie_id=from_abie_id,
            based_ascc_manifest_id=based_ascc_manifest_id,
        )
        return CreateAsbieToolResponse.model_validate(payload, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to create the ASBIE.") from exc


@mcp.tool(
    name="reuse_top_level_asbiep",
    description="Reuse an existing Top-Level ASBIEP (Association Business Information Entity Property) for an ASBIE (Association Business Information Entity). This tool sets the ASBIE's to_asbiep_id to point to the ASBIEP from the specified top-level ASBIEP. This works when: (1) the owner_top_level_asbiep_id of the ASBIE and the reuse_top_level_asbiep_id are different, (2) the ASBIE's based_ascc.to_asccp_manifest_id matches the reuse_top_level_asbiep's asbiep.based_asccp_manifest_id, and (3) both top-level ASBIEPs are in the same release (owner_top_level_asbiep.release.release_id must equal reuse_top_level_asbiep.release.release_id).",
    output_schema={
        "type": "object",
        "description": "Response containing the updated ASBIE information after reusing the top-level ASBIEP",
        "properties": {
            "asbie_id": {"type": "integer", "description": "Unique identifier of the ASBIE that was updated",
                         "example": 12345},
            "updates": {"type": "array", "items": {"type": "string"},
                        "description": "A list of field names that were updated on the ASBIE itself (e.g., ['to_asbiep_id'])",
                        "example": ["to_asbiep_id"]},
            "asbiep": {"type": ["object", "null"],
                       "description": "Nested structure showing ASBIEP and all updated relationships with their updates",
                       "properties": {
                           "asbiep_id": {"type": "integer", "description": "Unique identifier of the ASBIEP"},
                           "updates": {"type": "array", "items": {"type": "string"},
                                       "description": "List of fields updated on the ASBIEP (typically empty)"},
                           "role_of_abie": {"type": ["object", "null"],
                                            "description": "The ABIE that this ASBIEP points to, with its relationships",
                                            "properties": {
                                                "abie_id": {"type": "integer",
                                                            "description": "Unique identifier of the ABIE"},
                                                "updates": {"type": "array", "items": {"type": "string"},
                                                            "description": "List of fields updated on the ABIE (typically empty)"},
                                                "relationships": {"type": "array",
                                                                  "description": "List of relationships (ASBIEs and BBIEs) with their updates",
                                                                  "items": {
                                                                      "type": "object",
                                                                      "description": "A relationship that can be either an ASBIE or BBIE",
                                                                      "properties": {
                                                                          "asbie": {"type": ["object", "null"],
                                                                                    "description": "ASBIE relationship with updates"},
                                                                          "bbie": {"type": ["object", "null"],
                                                                                   "description": "BBIE relationship with updates"}
                                                                      }
                                                                  }}
                                            }}
                       }}
        },
        "required": ["asbie_id", "updates"]
    }
)
async def reuse_top_level_asbiep(
    asbie_id: Annotated[int, Field(gt=0, description="Target ASBIE identifier.")],
    reuse_top_level_asbiep_id: Annotated[int, Field(gt=0, description="Top-level ASBIEP identifier to reuse.")],
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> ReuseTopLevelAsbiepToolResponse:
    """
    Reuse an existing Top-Level ASBIEP (Association Business Information Entity Property) for an ASBIE.

    This tool allows reusing an existing top-level ASBIEP's ASBIEP for an ASBIE by setting the ASBIE's
    to_asbiep_id to point to the ASBIEP from the specified top-level ASBIEP.

    The operation works when:
    1. The owner_top_level_asbiep_id of the ASBIE (linked by asbie_id) and reuse_top_level_asbiep_id are different
    2. The ASBIE's based_ascc.to_asccp_manifest_id equals the reuse_top_level_asbiep's asbiep.based_asccp_manifest_id
       (In other words: ASBIE's to_asbiep.based_asccp_manifest_id = REUSE_TOP_LEVEL_ASBIEP.ASBIEP.based_asccp_manifest_id)
    3. Both top-level ASBIEPs are in the same release
       (asbie.owner_top_level_asbiep.release.release_id must equal reuse_top_level_asbiep.release.release_id)

    This tool should be used combined with 'create_asbie'. When called, it sets ASBIE.to_asbiep_id = reuse_top_level_asbiep.asbiep_id.

    Permission Requirements:
    - The current user must be the owner of the top-level ASBIEP that owns the ASBIE
    - The top-level ASBIEP state must be 'WIP' (Work In Progress)

    Args:
        asbie_id (int): The ASBIE ID to update with the reused top-level ASBIEP
        reuse_top_level_asbiep_id (int): The top-level ASBIEP ID to reuse

    Returns:
        UpdateAsbieResponse: Response object containing:
            - asbie_id: ID of the updated ASBIE
            - updates: List of updated fields (e.g., ['to_asbiep_id'])
            - asbiep: Nested structure showing ASBIEP and all updated relationships (None if not applicable)

    Raises:
        ToolError: If validation fails, resources are not found, user lacks permission,
            the owner_top_level_asbiep_ids are the same, the based_asccp_manifest_ids don't match,
            the release_ids don't match, or database errors occur.

    Examples:
        Reuse a top-level ASBIEP for an ASBIE:
        >>> result = reuse_top_level_asbiep(asbie_id=123, reuse_top_level_asbiep_id=67890)
        >>> print(f"Updated ASBIE ID: {result.asbie_id}")
        >>> print(f"Updates: {result.updates}")
    """
    try:
        payload = await business_information_entity_service.reuse_top_level_asbiep(
            asbie_id=asbie_id,
            reuse_top_level_asbiep_id=reuse_top_level_asbiep_id,
        )
        return ReuseTopLevelAsbiepToolResponse.model_validate(payload, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to reuse top-level ASBIEP {reuse_top_level_asbiep_id}.") from exc


@mcp.tool(
    name="remove_reused_top_level_asbiep",
    description="Remove a reused top-level ASBIEP by creating a new ASBIEP and ABIE. This tool checks if the ASBIE is using a reused top-level ASBIEP (i.e., asbie.owner_top_level_asbiep_id != asbie.to_asbiep.owner_top_level_asbiep_id). If it is reused, it creates a new ASBIEP and ABIE to revert the to_asbiep_id back to a non-reused state.",
    output_schema={
        "type": "object",
        "description": "Response containing the updated ASBIE information after removing the reused top-level ASBIEP",
        "properties": {
            "asbie_id": {"type": "integer", "description": "Unique identifier of the ASBIE that was updated",
                         "example": 12345},
            "updates": {"type": "array", "items": {"type": "string"},
                        "description": "A list of field names that were updated on the ASBIE itself (e.g., ['to_asbiep_id'])",
                        "example": ["to_asbiep_id"]},
            "asbiep": {"type": ["object", "null"],
                       "description": "Nested structure showing ASBIEP and all updated relationships with their updates",
                       "properties": {
                           "asbiep_id": {"type": "integer", "description": "Unique identifier of the ASBIEP"},
                           "updates": {"type": "array", "items": {"type": "string"},
                                       "description": "List of fields updated on the ASBIEP (typically empty)"},
                           "role_of_abie": {"type": ["object", "null"],
                                            "description": "The ABIE that this ASBIEP points to, with its relationships",
                                            "properties": {
                                                "abie_id": {"type": "integer",
                                                            "description": "Unique identifier of the ABIE"},
                                                "updates": {"type": "array", "items": {"type": "string"},
                                                            "description": "List of fields updated on the ABIE (typically empty)"},
                                                "relationships": {"type": "array",
                                                                  "description": "List of relationships (ASBIEs and BBIEs) with their updates",
                                                                  "items": {
                                                                      "type": "object",
                                                                      "description": "A relationship that can be either an ASBIE or BBIE",
                                                                      "properties": {
                                                                          "asbie": {"type": ["object", "null"],
                                                                                    "description": "ASBIE relationship with updates"},
                                                                          "bbie": {"type": ["object", "null"],
                                                                                   "description": "BBIE relationship with updates"}
                                                                      }
                                                                  }}
                                            }}
                       }}
        },
        "required": ["asbie_id", "updates"]
    }
)
async def remove_reused_top_level_asbiep(
    asbie_id: Annotated[int, Field(gt=0, description="Target ASBIE identifier.")],
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> RemoveReusedTopLevelAsbiepToolResponse:
    """
    Remove a reused top-level ASBIEP by creating a new ASBIEP and ABIE.

    This tool checks if the ASBIE is using a reused top-level ASBIEP (i.e.,
    asbie.owner_top_level_asbiep_id != asbie.to_asbiep.owner_top_level_asbiep_id).
    If it is reused, it creates a new ASBIEP and ABIE to revert the to_asbiep_id
    back to a non-reused state.

    Permission Requirements:
    - The current user must be the owner of the top-level ASBIEP that owns the ASBIE
    - The top-level ASBIEP state must be 'WIP' (Work In Progress)

    Args:
        asbie_id (int): The ASBIE ID to remove the reused top-level ASBIEP from

    Returns:
        UpdateAsbieResponse: Response object containing:
            - asbie_id: ID of the updated ASBIE
            - updates: List of updated fields (e.g., ['to_asbiep_id'])
            - asbiep: Nested structure showing ASBIEP and all updated relationships (None if not applicable)

    Raises:
        ToolError: If validation fails, resources are not found, user lacks permission,
            the ASBIE is not using a reused top-level ASBIEP, or database errors occur.

    Examples:
        Remove a reused top-level ASBIEP from an ASBIE:
        >>> result = remove_reused_top_level_asbiep(asbie_id=123)
        >>> print(f"Updated ASBIE ID: {result.asbie_id}")
        >>> print(f"Updates: {result.updates}")
    """
    try:
        payload = await business_information_entity_service.remove_reused_top_level_asbiep(
            asbie_id=asbie_id,
        )
        return RemoveReusedTopLevelAsbiepToolResponse(
            asbie_id=payload.asbie_id,
            updates=payload.updates,
        )
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to remove reused top-level ASBIEP from ASBIE {asbie_id}.") from exc


@mcp.tool(
    name="update_asbie",
    description="Update an existing ASBIE (Association Business Information Entity) during BIE profiling. Can be used to toggle enable/disable (use/unuse) the ASBIE, or modify its properties. Set is_used=True/1/'true'/'True' to enable/use or is_used=False/0/'false'/'False' to disable/unuse during profiling.",
    output_schema={
        "type": "object",
        "description": "Response containing the updated ASBIE information with nested structure showing all updated relationships. The updates list includes all fields that were updated on the ASBIE itself. The asbiep field contains nested structure with role_of_abie and relationships, each showing their updates. When is_used is set to True, mandatory relationships are automatically processed. When is_used is set to False, all underlying relationships are automatically disabled.",
        "properties": {
            "asbie_id": {"type": "integer", "description": "Unique identifier of the updated ASBIE", "example": 12345},
            "updates": {"type": "array", "items": {"type": "string"},
                        "description": "A list of field names that were updated on the ASBIE itself (includes 'is_used' when toggling enable/disable during profiling)",
                        "example": ["is_used", "definition", "cardinality_min", "remark"]},
            "asbiep": {"type": ["object", "null"],
                       "description": "Nested structure showing ASBIEP and all updated relationships with their updates",
                       "properties": {
                           "asbiep_id": {"type": "integer", "description": "Unique identifier of the ASBIEP"},
                           "updates": {"type": "array", "items": {"type": "string"},
                                       "description": "List of fields updated on the ASBIEP (typically empty)"},
                           "role_of_abie": {"type": ["object", "null"],
                                            "description": "The ABIE that this ASBIEP points to, with its relationships",
                                            "properties": {
                                                "abie_id": {"type": "integer",
                                                            "description": "Unique identifier of the ABIE"},
                                                "updates": {"type": "array", "items": {"type": "string"},
                                                            "description": "List of fields updated on the ABIE (typically empty)"},
                                                "relationships": {"type": "array",
                                                                  "description": "List of relationships (ASBIEs and BBIEs) with their updates",
                                                                  "items": {
                                                                      "type": "object",
                                                                      "description": "A relationship that can be either an ASBIE or BBIE",
                                                                      "properties": {
                                                                          "asbie": {"type": ["object", "null"],
                                                                                    "description": "ASBIE relationship with updates",
                                                                                    "properties": {
                                                                                        "asbie_id": {"type": "integer"},
                                                                                        "updates": {"type": "array",
                                                                                                    "items": {
                                                                                                        "type": "string"},
                                                                                                    "description": "List of fields updated on this ASBIE (e.g., ['is_used'])"},
                                                                                        "guid": {
                                                                                            "type": ["string", "null"]},
                                                                                        "cardinality_min": {
                                                                                            "type": "integer"},
                                                                                        "cardinality_max": {
                                                                                            "type": "integer"},
                                                                                        "based_ascc": {
                                                                                            "type": "object"},
                                                                                        "asbiep": {
                                                                                            "type": ["object", "null"],
                                                                                            "description": "Recursive structure - ASBIEP with its role_of_abie and relationships"}
                                                                                    }},
                                                                          "bbie": {"type": ["object", "null"],
                                                                                   "description": "BBIE relationship with updates",
                                                                                   "properties": {
                                                                                       "bbie_id": {"type": "integer"},
                                                                                       "updates": {"type": "array",
                                                                                                   "items": {
                                                                                                       "type": "string"},
                                                                                                   "description": "List of fields updated on this BBIE (e.g., ['is_used'])"},
                                                                                       "guid": {
                                                                                           "type": ["string", "null"]},
                                                                                       "cardinality_min": {
                                                                                           "type": "integer"},
                                                                                       "cardinality_max": {
                                                                                           "type": "integer"},
                                                                                       "is_nillable": {
                                                                                           "type": "boolean"},
                                                                                       "based_bcc": {"type": "object"}
                                                                                   }}
                                                                      }
                                                                  }}
                                            }}
                       }}
        },
        "required": ["asbie_id", "updates"]
    }
)
async def update_asbie(
    asbie_id: Annotated[int, Field(gt=0, description="Target ASBIE identifier.")],
    is_used: Annotated[bool | str | None, Field(default=None, description="Whether the ASBIE is profiled/used. Accepts bool values or 'true'/'false' strings. Omit to leave unchanged.")],
    is_nillable: Annotated[bool | str | None, Field(default=None, description="Whether the ASBIE is nillable. Accepts bool values or 'true'/'false' strings. Omit to leave unchanged.")],
    definition: Annotated[str | None, Field(default=None, description="Definition text for this ASBIE. Omit to leave unchanged.")],
    cardinality_min: Annotated[int | None, Field(default=None, ge=0, description="Minimum cardinality override. Omit to leave unchanged.")],
    cardinality_max: Annotated[int | None, Field(default=None, description="Maximum cardinality override. Use -1 for unbounded. Omit to leave unchanged.")],
    remark: Annotated[str | None, Field(default=None, description="Remark text for the ASBIE property. Omit to leave unchanged.")],
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> UpdateAsbieToolResponse:
    """
    Update an existing ASBIE (Association Business Information Entity) during BIE profiling.

    This tool is used to toggle enable/disable (use/unuse) an ASBIE during BIE profiling, or to
    modify its properties. At least one field must be provided. Set is_used=True/1/'true'/'True' to enable/use the ASBIE (making it active
    and profiled in the BIE), or is_used=False/0/'false'/'False' to disable/unuse it (removing it
    from the BIE profile).

    Automatic Relationship Processing:
    - When is_used is set to True: The tool automatically processes mandatory relationships
      (cardinality_min >= 1) recursively, creating or enabling required ASBIE and BBIE components
      to satisfy cardinality constraints. This ensures the BIE structure automatically satisfies
      all mandatory cardinality constraints.
    - When is_used is set to False: The tool automatically disables all existing underlying
      relationships (ASBIEs and BBIEs) recursively, ensuring that disabling an ASBIE also
      disables all its nested components. However, required relationships (cardinality_min >= 1)
      cannot be disabled and will be skipped during the disable process.

    Validation:
    - Required relationships (cardinality_min >= 1) cannot be disabled. If you attempt to disable
      an ASBIE that is required, the tool will raise an error. To remove a required relationship,
      you must first change its cardinality_min to 0.

    This is the primary tool for toggling ASBIE components during profiling. Use create_asbie to
    initially enable (use) an ASBIE component.

    Type Conversion:
    - Integer parameters (cardinality_min, cardinality_max):
      Accept int, str, or None. String values are automatically converted to integers.
    - Boolean parameters (is_used, is_nillable):
      Accept bool, str, or None. String values are converted as follows:
      - 'True'/'true'/'1' -> True
      - 'False'/'false'/'0' -> False

    Permission Requirements:
    - The current user must be the owner of the top-level ASBIEP
    - The top-level ASBIEP state must be 'WIP' (Work In Progress)

    Args:
        asbie_id (int): The ASBIE ID to update
        is_used (bool | str | None, optional): Toggle enable/disable (use/unuse) this ASBIE during profiling.
            Accepts bool, str ('True'/'true'/'1' for True, 'False'/'false'/'0' for False), or None.
            If not provided, is_used will not be updated.
        is_nillable (bool | str | None, optional): Whether the ASBIE can have a nil/null value.
            Accepts bool, str ('True'/'true'/'1' for True, 'False'/'false'/'0' for False), or None.
            If not provided, will not be updated.
        definition (str | None, optional): Definition text for this ASBIE.
        cardinality_min (int | str | None, optional): Minimum cardinality.
            Accepts int, str (converted to int), or None. If not provided, will not be updated.
        cardinality_max (int | str | None, optional): Maximum cardinality (-1 means unbounded).
            Accepts int, str (converted to int), or None. If not provided, will not be updated.
        remark (str | None, optional): Remark text for the ASBIE property.

    Returns:
        UpdateAsbieResponse: Response object containing:
            - asbie_id: ID of the updated ASBIE
            - updates: List of fields that were updated on the ASBIE itself (includes 'is_used', 'definition',
              'remark', 'cardinality_min', 'cardinality_max', etc.)
            - asbiep: Nested structure showing ASBIEP and all relationships with their updates:
              - asbiep_id: ID of the ASBIEP
              - updates: List of fields updated on the ASBIEP (typically empty)
              - role_of_abie: The ABIE that this ASBIEP points to:
                - abie_id: ID of the ABIE
                - updates: List of fields updated on the ABIE (typically empty)
                - relationships: List of relationships (ASBIEs and BBIEs), each with:
                  - asbie/bbie: Relationship details including:
                    - asbie_id/bbie_id: ID of the relationship
                    - updates: List of fields updated on this relationship (e.g., ['is_used'])
                    - Other relationship properties (guid, path, cardinality, etc.)
                    - For ASBIEs: recursive asbiep structure
              When is_used is set to True, mandatory relationships are automatically processed and their
              updates are tracked. When is_used is set to False, all underlying relationships are automatically
              disabled and their updates are tracked.

    Raises:
        ToolError: If validation fails, resources are not found, user lacks permission,
            type conversion fails, or database errors occur. Common error scenarios include:
            - No fields provided for update
            - Invalid input parameters
            - Resources not found (404 error)
            - Access denied (403 error)
            - Type conversion failures

    Examples:
        Toggle enable an ASBIE during profiling:
        >>> result = update_asbie(asbie_id=789, is_used=True)
        >>> result = update_asbie(asbie_id=789, is_used="1")
        >>> result = update_asbie(asbie_id=789, is_used="true")

        Toggle disable (unuse) an ASBIE during profiling:
        >>> result = update_asbie(asbie_id=789, is_used=False)
        >>> result = update_asbie(asbie_id=789, is_used="0")
        >>> result = update_asbie(asbie_id=789, is_used="false")

        Update definition and cardinality:
        >>> result = update_asbie(asbie_id=789, definition="Custom definition", cardinality_min=1)
        >>> result = update_asbie(asbie_id=789, cardinality_min="1", cardinality_max="3")
    """
    try:
        is_used = str_to_bool(is_used)
        is_nillable = str_to_bool(is_nillable)
    except ToolError:
        raise
    except Exception as e:
        raise ToolError(
            f"Type conversion error: {str(e)}. Please check your parameter types and try again."
        ) from e

    try:
        payload = await business_information_entity_service.update_asbie(
            asbie_id=asbie_id,
            is_used=is_used,
            is_nillable=is_nillable,
            definition=definition,
            cardinality_min=cardinality_min,
            cardinality_max=cardinality_max,
            remark=remark,
        )
        return UpdateAsbieToolResponse.model_validate(payload, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to update ASBIE {asbie_id}.") from exc


@mcp.tool(
    name="create_bbie",
    description="Enable (use) a BBIE (Basic Business Information Entity) during BIE profiling. Creates a new BBIE with associated BBIEP record, enabling the component in the BIE. The BBIE is automatically enabled (is_used=True) and profiled. Either from_abie_id or asbiep_id must be provided. If both are provided, the tool will validate that asbiep.role_of_abie_id == from_abie_id. If only asbiep_id is provided, from_abie_id will be automatically fetched from asbiep.role_of_abie_id. Either based_bcc_manifest_id or property_term must be provided. If property_term is provided, the tool will search through the ABIE's relationships to find a matching BCCP by property_term. After creation, this tool automatically processes mandatory BBIE SCs (supplementary components with cardinality_min >= 1), creating or enabling required BBIE_SC components to satisfy cardinality constraints.",
    output_schema={
        "type": "object",
        "description": "Response containing the newly created and enabled BBIE information. The BBIE is automatically enabled (is_used=True) for BIE profiling. All mandatory BBIE SCs (supplementary components with cardinality_min >= 1) are automatically created or enabled. The 'bbiep' field contains simplified BBIEP information including all created/enabled supplementary components.",
        "properties": {
            "bbie_id": {"type": "integer", "description": "Unique identifier of the newly created and enabled BBIE",
                        "example": 12345},
            "bbiep": {"type": ["object", "null"],
                      "description": "Simplified BBIEP information including all created/enabled supplementary components. Excludes definition, remark, biz_term, display_name, default_value, fixed_value, and facet fields that won't have values for newly generated records. Shows the BBIEP structure with supplementary_components array containing all BBIE SCs that were automatically created or enabled.",
                      "properties": {
                          "bbiep_id": {"type": ["integer", "null"], "description": "Unique identifier of the BBIEP",
                                       "example": 12346},
                          "guid": {"type": ["string", "null"], "description": "Globally unique identifier of the BBIEP",
                                   "example": "a1b2c3d4e5f6789012345678901234ab"},
                          "based_bccp": {"type": "object",
                                         "description": "Information about the BCCP that this BBIEP is based on"},
                          "supplementary_components": {"type": "array",
                                                       "description": "List of supplementary components. Each component contains 'bbie_sc_id' (if created), 'based_dt_sc.dt_sc_manifest_id', and cardinality information. Excludes definition, remark, default_value, fixed_value, and facet fields.",
                                                       "items": {
                                                           "type": "object",
                                                           "properties": {
                                                               "bbie_sc_id": {"type": ["integer", "null"],
                                                                              "description": "Unique identifier for the BBIE SC (if created)",
                                                                              "example": 12351},
                                                               "guid": {"type": ["string", "null"],
                                                                        "description": "Globally unique identifier for the BBIE SC"},
                                                               "based_dt_sc": {"type": "object",
                                                                               "description": "Data type supplementary component information"},
                                                               "cardinality_min": {"type": "integer",
                                                                                   "description": "Minimum cardinality",
                                                                                   "example": 0},
                                                               "cardinality_max": {"type": "integer",
                                                                                   "description": "Maximum cardinality",
                                                                                   "example": 1}
                                                           }
                                                       }}
                      }}
        },
        "required": ["bbie_id"]
    }
)
async def create_bbie(
    from_abie_id: Annotated[int, Field(gt=0, description="Parent ABIE identifier.")],
    based_bcc_manifest_id: Annotated[int, Field(gt=0, description="Based BCC manifest identifier.")],
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> CreateBbieToolResponse:
    """
    Enable (use) a BBIE (Basic Business Information Entity) during BIE profiling.

    This tool is used to enable (use) a BBIE component during BIE profiling. It creates a new BBIE
    with associated BBIEP record and automatically enables it (is_used=True) for use in the BIE.

    After creating the BBIE, this tool automatically processes mandatory BBIE SCs (supplementary
    components with cardinality_min >= 1). For each mandatory BBIE SC, the tool:
    - If the BBIE SC already exists (has bbie_sc_id) but is disabled (is_used=False),
      it automatically enables it by setting is_used=True.
    - If the BBIE SC doesn't exist, it automatically creates it.

    This automatic processing ensures that the BIE structure automatically satisfies all cardinality
    constraints defined in the Core Component specification for supplementary components without
    requiring manual follow-up operations.

    This is the primary tool for enabling/profiling BBIE components. The created BBIE is automatically
    enabled and ready for use in the BIE. Use update_bbie to toggle enable/disable (use/unuse) or
    modify other properties after creation.

    Permission Requirements:
    - The current user must be the owner of the top-level ASBIEP
    - The top-level ASBIEP state must be 'WIP' (Work In Progress)

    Args:
        from_abie_id (int | str | None): The ABIE ID that this BBIE originates from (parent ABIE).
            Either from_abie_id or asbiep_id must be provided. If both are provided, the tool will
            validate that asbiep.role_of_abie_id == from_abie_id. Accepts int, str, or None.
            String values are automatically converted to integers.
        asbiep_id (int | str | None): The ASBIEP ID to use for determining the parent ABIE.
            Either from_abie_id or asbiep_id must be provided. If both are provided, the tool will
            validate that asbiep.role_of_abie_id == from_abie_id. If only asbiep_id is provided,
            from_abie_id will be automatically fetched from asbiep.role_of_abie_id. Accepts int, str, or None.
            String values are automatically converted to integers.
        based_bcc_manifest_id (int | str | None): The BCC manifest ID that this BBIE is based on.
            Accepts int, str, or None. String values are automatically converted to integers.
            If not provided, property_term must be provided instead.
        property_term (str | None): The property term of the BCCP to search for in the relationships.
            If provided and based_bcc_manifest_id is None, this tool will search through the ABIE's relationships
            to find a matching BCCP by property_term and extract the based_bcc_manifest_id from it.

    Returns:
        CreateBbieResponse: Response object containing:
            - bbie_id: ID of the newly created and enabled BBIE
            - bbiep: Simplified BBIEP information including all created/enabled supplementary components.
              Shows the BBIEP structure with supplementary_components array containing all BBIE SCs
              that were automatically created or enabled during mandatory relationship processing.
              Excludes definition, remark, biz_term, display_name, default_value, fixed_value, and facet fields.
              None if the created BBIE doesn't have a BBIEP.
              Note: Each supplementary component's primitiveRestriction field is required and must not be None.
              It contains exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId.

    Raises:
        ToolError: If validation fails, resources are not found, user lacks permission,
            or database errors occur.

    Examples:
        Enable a BBIE during BIE profiling using based_bcc_manifest_id (mandatory BBIE SCs are automatically created/enabled):
        >>> result = create_bbie(from_abie_id=123, based_bcc_manifest_id=456)
        >>> print(f"Created BBIE ID: {result.bbie_id}")

        Enable a BBIE using property_term to search for the relationship:
        >>> result = create_bbie(from_abie_id=123, property_term="Amount")
        >>> print(f"Created BBIE ID: {result.bbie_id}")

        Enable a BBIE using asbiep_id to determine the parent ABIE:
        >>> result = create_bbie(asbiep_id=12346, based_bcc_manifest_id=456)
        >>> print(f"Created BBIE ID: {result.bbie_id}")
    """
    try:
        payload = await business_information_entity_service.create_bbie(
            from_abie_id=from_abie_id,
            based_bcc_manifest_id=based_bcc_manifest_id,
        )
        return CreateBbieToolResponse.model_validate(payload, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to create the BBIE.") from exc


@mcp.tool(
    name="update_bbie",
    description="Update an existing BBIE (Basic Business Information Entity) during BIE profiling. Can be used to toggle enable/disable (use/unuse) the BBIE, or modify its properties. Set is_used=True/1/'true'/'True' to enable/use or is_used=False/0/'false'/'False' to disable/unuse during profiling.",
    output_schema={
        "type": "object",
        "description": "Response containing the updated BBIE information with nested structure showing all updated supplementary components. The updates list includes all fields that were updated on the BBIE itself. The bbiep field contains nested structure with supplementary_components, each showing their updates. When is_used is set to True, mandatory supplementary components are automatically processed. When is_used is set to False, all supplementary components are automatically disabled.",
        "properties": {
            "bbie_id": {"type": "integer", "description": "Unique identifier of the updated BBIE", "example": 12345},
            "updates": {"type": "array", "items": {"type": "string"},
                        "description": "A list of field names that were updated on the BBIE itself (includes 'is_used' when toggling enable/disable during profiling)",
                        "example": ["is_used", "definition", "cardinality_min", "default_value", "facet_min_length"]},
            "bbiep": {"type": ["object", "null"],
                      "description": "Nested structure showing BBIEP and all updated supplementary components with their updates",
                      "properties": {
                          "bbiep_id": {"type": ["integer", "null"], "description": "Unique identifier of the BBIEP"},
                          "updates": {"type": "array", "items": {"type": "string"},
                                      "description": "List of fields updated on the BBIEP (typically empty)"},
                          "guid": {"type": ["string", "null"],
                                   "description": "Globally unique identifier of the BBIEP"},
                          "based_bccp": {"type": "object",
                                         "description": "Information about the BCCP that this BBIEP is based on"},
                          "supplementary_components": {"type": "array",
                                                       "description": "List of supplementary components with their updates",
                                                       "items": {
                                                           "type": "object",
                                                           "properties": {
                                                               "bbie_sc_id": {"type": ["integer", "null"],
                                                                              "description": "Unique identifier for the BBIE SC"},
                                                               "updates": {"type": "array", "items": {"type": "string"},
                                                                           "description": "List of fields updated on this BBIE SC (e.g., ['is_used'])"},
                                                               "guid": {"type": ["string", "null"],
                                                                        "description": "Globally unique identifier for the BBIE SC"},
                                                               "based_dt_sc": {"type": "object",
                                                                               "description": "Data type supplementary component information"},
                                                               "cardinality_min": {"type": "integer",
                                                                                   "description": "Minimum cardinality"},
                                                               "cardinality_max": {"type": "integer",
                                                                                   "description": "Maximum cardinality"}
                                                           }
                                                       }}
                      }}
        },
        "required": ["bbie_id", "updates"]
    }
)
async def update_bbie(
    bbie_id: Annotated[int, Field(gt=0, description="Target BBIE identifier.")],
    is_used: Annotated[bool | str | None, Field(default=None, description="Whether the BBIE is profiled/used. Accepts bool values or 'true'/'false' strings. Omit to leave unchanged.")],
    is_nillable: Annotated[bool | str | None, Field(default=None, description="Whether the BBIE is nillable. Accepts bool values or 'true'/'false' strings. Omit to leave unchanged.")],
    cardinality_min: Annotated[int | None, Field(default=None, ge=0, description="Minimum cardinality override. Omit to leave unchanged.")],
    cardinality_max: Annotated[int | None, Field(default=None, description="Maximum cardinality override. Use -1 for unbounded. Omit to leave unchanged.")],
    definition: Annotated[str | None, Field(default=None, description="Relationship definition override. Omit to leave unchanged.")],
    example: Annotated[str | None, Field(default=None, description="Illustrative example value or content for this BBIE. Omit to leave unchanged.")],
    remark: Annotated[str | None, Field(default=None, description="Relationship remark override. Omit to leave unchanged.")],
    default_value: Annotated[str | None, Field(default=None, description="Default value constraint. Omit to leave unchanged.")],
    fixed_value: Annotated[str | None, Field(default=None, description="Fixed value constraint. Omit to leave unchanged.")],
    facet_min_length: Annotated[int | None, Field(default=None, ge=0, description="Facet min-length restriction. Omit to leave unchanged.")],
    facet_max_length: Annotated[int | None, Field(default=None, ge=0, description="Facet max-length restriction. Omit to leave unchanged.")],
    facet_pattern: Annotated[str | None, Field(default=None, description="Facet pattern restriction. Omit to leave unchanged.")],
    xbt_manifest_id: Annotated[int | None, Field(default=None, gt=0, description="XBT manifest identifier to use as the primitive restriction for this BBIE. Omit to leave unchanged.")],
    code_list_manifest_id: Annotated[int | None, Field(default=None, gt=0, description="Code list manifest identifier to use as the primitive restriction for this BBIE. Omit to leave unchanged.")],
    agency_id_list_manifest_id: Annotated[int | None, Field(default=None, gt=0, description="Agency-ID-list manifest identifier to use as the primitive restriction for this BBIE. Omit to leave unchanged.")],
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> UpdateBbieToolResponse:
    """
    Update an existing BBIE (Basic Business Information Entity) during BIE profiling.

    This tool is used to toggle enable/disable (use/unuse) a BBIE during BIE profiling, or to
    modify its properties. At least one field must be provided. Set is_used=True/1/'true'/'True' to enable/use the BBIE (making it active
    and profiled in the BIE), or is_used=False/0/'false'/'False' to disable/unuse it (removing it
    from the BIE profile).

    Automatic Supplementary Component Processing:
    - When is_used is set to True: The tool automatically processes mandatory supplementary components
      (BBIE SCs with cardinality_min >= 1), creating or enabling required BBIE_SC components to
      satisfy cardinality constraints. This ensures the BIE structure automatically satisfies all
      mandatory cardinality constraints for supplementary components.
    - When is_used is set to False: The tool automatically disables all existing supplementary
      components (BBIE SCs), ensuring that disabling a BBIE also disables all its supplementary
      components. However, required supplementary components (cardinality_min >= 1) cannot be
      disabled and will be skipped during the disable process.

    Validation:
    - Required relationships (cardinality_min >= 1) cannot be disabled. If you attempt to disable
      a BBIE that is required, the tool will raise an error. To remove a required relationship,
      you must first change its cardinality_min to 0.

    This is the primary tool for toggling BBIE components during profiling. Use create_bbie to
    initially enable (use) a BBIE component.

    Type Conversion:
    - Integer parameters (cardinality_min, cardinality_max, facet_min_length, facet_max_length):
      Accept int, str, or None. String values are automatically converted to integers.
    - Boolean parameters (is_used, is_nillable):
      Accept bool, str, or None. String values are converted as follows:
      - 'True'/'true'/'1' -> True
      - 'False'/'false'/'0' -> False

    Permission Requirements:
    - The current user must be the owner of the top-level ASBIEP
    - The top-level ASBIEP state must be 'WIP' (Work In Progress)

    Args:
        bbie_id (int): The BBIE ID to update
        is_used (bool | str | None, optional): Toggle enable/disable (use/unuse) this BBIE during profiling.
            Accepts bool, str ('True'/'true'/'1' for True, 'False'/'false'/'0' for False), or None.
            If not provided, is_used will not be updated.
        is_nillable (bool | str | None, optional): Whether the BBIE can have a nil/null value.
            Accepts bool, str ('True'/'true'/'1' for True, 'False'/'false'/'0' for False), or None.
            If not provided, will not be updated.
        cardinality_min (int | str | None, optional): Minimum cardinality.
            Accepts int, str (converted to int), or None. If not provided, will not be updated.
        cardinality_max (int | str | None, optional): Maximum cardinality (-1 means unbounded).
            Accepts int, str (converted to int), or None. If not provided, will not be updated.
        definition (str | None, optional): Definition text for this BBIE
        example (str | None, optional): Illustrative example value or content for this BBIE
        remark (str | None, optional): Remark text for the BBIE property
        default_value (str | None, optional): Default value for the BBIE (mutually exclusive with fixed_value)
        fixed_value (str | None, optional): Fixed value for the BBIE (mutually exclusive with default_value)
        facet_min_length (int | str | None, optional): Minimum length facet for string types.
            Accepts int, str (converted to int), or None. If not provided, will not be updated.
        facet_max_length (int | str | None, optional): Maximum length facet for string types.
            Accepts int, str (converted to int), or None. If not provided, will not be updated.
        facet_pattern (str | None, optional): Pattern facet (regex) for string types
        xbt_manifest_id (int | None, optional): XBT manifest identifier to use as the primitive restriction for this BBIE. Mutually exclusive with code_list_manifest_id and agency_id_list_manifest_id.
        code_list_manifest_id (int | None, optional): Code list manifest identifier to use as the primitive restriction for this BBIE. Mutually exclusive with xbt_manifest_id and agency_id_list_manifest_id.
        agency_id_list_manifest_id (int | None, optional): Agency-ID-list manifest identifier to use as the primitive restriction for this BBIE. Mutually exclusive with xbt_manifest_id and code_list_manifest_id.

    Returns:
        UpdateBbieResponse: Response object containing:
            - bbie_id: ID of the updated BBIE
            - updates: List of fields that were updated on the BBIE itself (includes 'is_used', 'definition',
              'remark', 'cardinality_min', 'cardinality_max', 'default_value', 'fixed_value', facet fields, etc.)
            - bbiep: Nested structure showing BBIEP and all supplementary components with their updates:
              - bbiep_id: ID of the BBIEP
              - updates: List of fields updated on the BBIEP (typically empty)
              - guid, based_bccp: BBIEP properties
              - supplementary_components: List of supplementary components, each with:
                - bbie_sc_id: ID of the BBIE SC
                - updates: List of fields updated on this BBIE SC (e.g., ['is_used'])
                - primitiveRestriction: Required field that must not be None. Contains exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId.
                - Other component properties (guid, based_dt_sc, cardinality, etc.)
              When is_used is set to True, mandatory supplementary components are automatically processed
              and their updates are tracked. When is_used is set to False, all supplementary components
              are automatically disabled and their updates are tracked.

    Raises:
        ToolError: If validation fails, resources are not found, user lacks permission,
            type conversion fails, or database errors occur. Common error scenarios include:
            - No fields provided for update
            - Invalid input parameters
            - Resources not found (404 error)
            - Access denied (403 error)
            - Type conversion failures

    Examples:
        Toggle enable a BBIE during profiling:
        >>> result = update_bbie(bbie_id=789, is_used=True)
        >>> result = update_bbie(bbie_id=789, is_used="1")
        >>> result = update_bbie(bbie_id=789, is_used="true")

        Toggle disable (unuse) a BBIE during profiling:
        >>> result = update_bbie(bbie_id=789, is_used=False)
        >>> result = update_bbie(bbie_id=789, is_used="0")
        >>> result = update_bbie(bbie_id=789, is_used="false")

        Update definition, example, and facets:
        >>> result = update_bbie(bbie_id=789, definition="Custom definition", example="BOM-01", facet_min_length=5, facet_max_length=100)
        >>> result = update_bbie(bbie_id=789, facet_min_length="5", facet_max_length="100")

        Update cardinality:
        >>> result = update_bbie(bbie_id=789, cardinality_min=1, cardinality_max=3)
        >>> result = update_bbie(bbie_id=789, cardinality_min="1", cardinality_max="3")
    """
    try:
        is_used = str_to_bool(is_used)
        is_nillable = str_to_bool(is_nillable)
    except ToolError:
        raise
    except Exception as e:
        raise ToolError(
            f"Type conversion error: {str(e)}. Please check your parameter types and try again."
        ) from e

    try:
        payload = await business_information_entity_service.update_bbie(
            bbie_id=bbie_id,
            is_used=is_used,
            is_nillable=is_nillable,
            cardinality_min=cardinality_min,
            cardinality_max=cardinality_max,
            definition=definition,
            example=example,
            remark=remark,
            default_value=default_value,
            fixed_value=fixed_value,
            facet_min_length=facet_min_length,
            facet_max_length=facet_max_length,
            facet_pattern=facet_pattern,
            xbt_manifest_id=xbt_manifest_id,
            code_list_manifest_id=code_list_manifest_id,
            agency_id_list_manifest_id=agency_id_list_manifest_id,
        )
        return UpdateBbieToolResponse.model_validate(payload, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to update BBIE {bbie_id}.") from exc


@mcp.tool(
    name="create_bbie_sc",
    description="Enable (use) a BBIE_SC (Basic Business Information Entity Supplementary Component) during BIE profiling. Creates a new BBIE_SC record, enabling the supplementary component in the BIE. The BBIE_SC is automatically enabled (is_used=True) and profiled. Use get_bbie_by_bbie_id() or get_bbie_by_based_bcc_manifest_id() to retrieve available supplementary components, then use the 'based_dt_sc.dt_sc_manifest_id' from the supplementary_components array as the based_dt_sc_manifest_id parameter.",
    output_schema={
        "type": "object",
        "description": "Response containing the newly created and enabled BBIE_SC information. The BBIE_SC is automatically enabled (is_used=True) for BIE profiling.",
        "properties": {
            "bbie_sc_id": {"type": "integer",
                           "description": "Unique identifier of the newly created and enabled BBIE_SC",
                           "example": 12345},
            "updates": {"type": "array", "items": {"type": "string"},
                        "description": "A list of field names that were set during creation (includes 'is_used' as the BBIE_SC is enabled)",
                        "example": ["bbie_sc_id", "is_used", "cardinality_min", "cardinality_max"]}
        },
        "required": ["bbie_sc_id", "updates"]
    }
)
async def create_bbie_sc(
    bbie_id: Annotated[int, Field(gt=0, description="Parent BBIE identifier.")],
    based_dt_sc_manifest_id: Annotated[int, Field(gt=0, description="Based DT_SC manifest identifier.")],
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> CreateBbieScToolResponse:
    """
    Enable (use) a BBIE_SC (Basic Business Information Entity Supplementary Component) during BIE profiling.

    This tool is used to enable (use) a BBIE_SC component during BIE profiling. It creates a new BBIE_SC
    record and automatically enables it (is_used=True) for use in the BIE.

    This is the primary tool for enabling/profiling BBIE_SC components. The created BBIE_SC is automatically
    enabled and ready for use in the BIE. Use update_bbie_sc to toggle enable/disable (use/unuse) or
    modify other properties after creation.

    To find available supplementary components, use get_bbie_by_bbie_id() or get_bbie_by_based_bcc_manifest_id() which returns 'supplementary_components'
    in 'to_bbiep'. Each component in the array contains 'based_dt_sc.dt_sc_manifest_id' which should
    be used as the based_dt_sc_manifest_id parameter.

    Permission Requirements:
    - The current user must be the owner of the top-level ASBIEP
    - The top-level ASBIEP state must be 'WIP' (Work In Progress)

    Args:
        bbie_id (int): The BBIE ID that this BBIE_SC belongs to
        based_dt_sc_manifest_id (int): The DT_SC manifest ID that this BBIE_SC is based on.
            Obtain this from get_bbie_by_bbie_id() or get_bbie_by_based_bcc_manifest_id() response: 'to_bbiep.supplementary_components[].based_dt_sc.dt_sc_manifest_id'

    Returns:
        CreateBbieScResponse: Response object containing:
            - bbie_sc_id: ID of the newly created and enabled BBIE_SC
            - updates: List of fields that were set during creation (includes 'is_used' as enabled)
            - Note: The created BBIE_SC will have a primitiveRestriction field that is required and must not be None.
              It contains exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId (fetched from DtScAwdPri).

    Raises:
        ToolError: If validation fails, resources are not found, user lacks permission,
            or database errors occur.

    Examples:
        Enable a BBIE_SC during BIE profiling:
        >>> # First, get available supplementary components
        >>> bbie = get_bbie(top_level_asbiep_id=123, parent_abie_path="ASCCP-10>ACC-20", bcc_manifest_id=40)
        >>> # Then use the dt_sc_manifest_id from supplementary_components
        >>> result = create_bbie_sc(bbie_id=123, based_dt_sc_manifest_id=bbie.to_bbiep.supplementary_components[0].based_dt_sc.dt_sc_manifest_id)
    """
    try:
        payload = await business_information_entity_service.create_bbie_sc(
            bbie_id=bbie_id,
            based_dt_sc_manifest_id=based_dt_sc_manifest_id,
        )
        return CreateBbieScToolResponse.model_validate(payload, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to create the BBIE supplementary component.") from exc


@mcp.tool(
    name="update_bbie_sc",
    description="Update an existing BBIE_SC (Basic Business Information Entity Supplementary Component) during BIE profiling. Can be used to toggle enable/disable (use/unuse) the BBIE_SC, or modify its properties. Set is_used=True/1/'true'/'True' to enable/use or is_used=False/0/'false'/'False' to disable/unuse during profiling. Use get_bbie_by_bbie_id() or get_bbie_by_based_bcc_manifest_id() to retrieve existing supplementary components, then use the 'bbie_sc_id' from the supplementary_components array as the bbie_sc_id parameter.",
    output_schema={
        "type": "object",
        "description": "Response containing the updated BBIE_SC information. The updates list includes 'is_used' when the BBIE_SC is toggled between enabled/disabled (used/unused) during BIE profiling.",
        "properties": {
            "bbie_sc_id": {"type": "integer", "description": "Unique identifier of the updated BBIE_SC",
                           "example": 12345},
            "updates": {"type": "array", "items": {"type": "string"},
                        "description": "A list of field names that were updated (includes 'is_used' when toggling enable/disable during profiling)",
                        "example": ["is_used", "definition", "cardinality_min", "default_value", "facet_min_length"]}
        },
        "required": ["bbie_sc_id", "updates"]
    }
)
async def update_bbie_sc(
    bbie_sc_id: Annotated[int, Field(gt=0, description="Target BBIE supplementary-component identifier.")],
    is_used: Annotated[bool | str | None, Field(default=None, description="Whether the BBIE supplementary component is profiled/used. Accepts bool values or 'true'/'false' strings. Omit to leave unchanged.")],
    cardinality_min: Annotated[int | None, Field(default=None, ge=0, description="Minimum cardinality override. Omit to leave unchanged.")],
    cardinality_max: Annotated[int | None, Field(default=None, description="Maximum cardinality override. Use -1 for unbounded. Omit to leave unchanged.")],
    definition: Annotated[str | None, Field(default=None, description="Definition text for this BBIE supplementary component. Omit to leave unchanged.")],
    example: Annotated[str | None, Field(default=None, description="Illustrative example value or content for this BBIE supplementary component. Omit to leave unchanged.")],
    remark: Annotated[str | None, Field(default=None, description="Remark text for this BBIE supplementary component. Omit to leave unchanged.")],
    biz_term: Annotated[str | None, Field(default=None, description="Business term override. Omit to leave unchanged.")],
    display_name: Annotated[str | None, Field(default=None, description="Display name override. Omit to leave unchanged.")],
    default_value: Annotated[str | None, Field(default=None, description="Default value constraint. Omit to leave unchanged.")],
    fixed_value: Annotated[str | None, Field(default=None, description="Fixed value constraint. Omit to leave unchanged.")],
    facet_min_length: Annotated[int | None, Field(default=None, ge=0, description="Facet min-length restriction. Omit to leave unchanged.")],
    facet_max_length: Annotated[int | None, Field(default=None, ge=0, description="Facet max-length restriction. Omit to leave unchanged.")],
    facet_pattern: Annotated[str | None, Field(default=None, description="Facet pattern restriction. Omit to leave unchanged.")],
    xbt_manifest_id: Annotated[int | None, Field(default=None, gt=0, description="XBT manifest identifier to use as the primitive restriction for this BBIE supplementary component. Omit to leave unchanged.")],
    code_list_manifest_id: Annotated[int | None, Field(default=None, gt=0, description="Code list manifest identifier to use as the primitive restriction for this BBIE supplementary component. Omit to leave unchanged.")],
    agency_id_list_manifest_id: Annotated[int | None, Field(default=None, gt=0, description="Agency-ID-list manifest identifier to use as the primitive restriction for this BBIE supplementary component. Omit to leave unchanged.")],
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> UpdateBbieScToolResponse:
    """
    Update an existing BBIE_SC (Basic Business Information Entity Supplementary Component) during BIE profiling.

    This tool is used to toggle enable/disable (use/unuse) a BBIE_SC during BIE profiling, or to
    modify its properties. At least one field must be provided. Set is_used=True/1/'true'/'True' to enable/use the BBIE_SC (making it active
    and profiled in the BIE), or is_used=False/0/'false'/'False' to disable/unuse it (removing it
    from the BIE profile).

    This is the primary tool for toggling BBIE_SC components during profiling. Use create_bbie_sc to
    initially enable (use) a BBIE_SC component.

    Type Conversion:
    - Integer parameters (cardinality_min, cardinality_max, facet_min_length, facet_max_length):
      Accept int, str, or None. String values are automatically converted to integers.
    - Boolean parameters (is_used):
      Accept bool, str, or None. String values are converted as follows:
      - 'True'/'true'/'1' -> True
      - 'False'/'false'/'0' -> False

    To find existing supplementary components, use get_bbie_by_bbie_id() or get_bbie_by_based_bcc_manifest_id() which returns 'supplementary_components'
    in 'to_bbiep'. Each component in the array contains 'bbie_sc_id' (if the component already exists,
    i.e., bbie_sc_id is not null) which should be used as the bbie_sc_id parameter.

    Permission Requirements:
    - The current user must be the owner of the top-level ASBIEP
    - The top-level ASBIEP state must be 'WIP' (Work In Progress)

    Args:
        bbie_sc_id (int): The BBIE_SC ID to update. Obtain this from get_bbie_by_bbie_id() or get_bbie_by_based_bcc_manifest_id() response:
            'to_bbiep.supplementary_components[].bbie_sc_id' (only available if component exists)
        is_used (bool | str | None, optional): Toggle enable/disable (use/unuse) this BBIE_SC during profiling.
            Accepts bool, str ('True'/'true'/'1' for True, 'False'/'false'/'0' for False), or None.
            If not provided, is_used will not be updated.
        cardinality_min (int | str | None, optional): Minimum cardinality.
            Accepts int, str (converted to int), or None. If not provided, will not be updated.
        cardinality_max (int | str | None, optional): Maximum cardinality (-1 means unbounded).
            Accepts int, str (converted to int), or None. If not provided, will not be updated.
        definition (str | None, optional): Definition text for this BBIE supplementary component
        example (str | None, optional): Illustrative example value or content for this BBIE supplementary component
        remark (str | None, optional): Remark text for this BBIE supplementary component
        biz_term (str | None, optional): Business term override
        display_name (str | None, optional): Display name override
        default_value (str | None, optional): Default value for the BBIE_SC (mutually exclusive with fixed_value)
        fixed_value (str | None, optional): Fixed value for the BBIE_SC (mutually exclusive with default_value)
        facet_min_length (int | str | None, optional): Minimum length facet for string types.
            Accepts int, str (converted to int), or None. If not provided, will not be updated.
        facet_max_length (int | str | None, optional): Maximum length facet for string types.
            Accepts int, str (converted to int), or None. If not provided, will not be updated.
        facet_pattern (str | None, optional): Pattern facet (regex) for string types
        xbt_manifest_id (int | None, optional): XBT manifest identifier to use as the primitive restriction for this BBIE supplementary component. Mutually exclusive with code_list_manifest_id and agency_id_list_manifest_id.
        code_list_manifest_id (int | None, optional): Code list manifest identifier to use as the primitive restriction for this BBIE supplementary component. Mutually exclusive with xbt_manifest_id and agency_id_list_manifest_id.
        agency_id_list_manifest_id (int | None, optional): Agency-ID-list manifest identifier to use as the primitive restriction for this BBIE supplementary component. Mutually exclusive with xbt_manifest_id and code_list_manifest_id.

    Returns:
        UpdateBbieScResponse: Response object containing:
            - bbie_sc_id: ID of the updated BBIE_SC
            - updates: List of fields that were updated (includes 'is_used' when toggling enable/disable)
            - Note: The BBIE_SC's primitiveRestriction field is required and must not be None.
              It contains exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId.

    Raises:
        ToolError: If validation fails, resources are not found, user lacks permission,
            type conversion fails, or database errors occur. Common error scenarios include:
            - No fields provided for update
            - Invalid input parameters
            - Resources not found (404 error)
            - Access denied (403 error)
            - Type conversion failures

    Examples:
        Toggle enable a BBIE_SC during profiling:
        >>> # First, get existing supplementary components
        >>> bbie = get_bbie_by_bbie_id(bbie_id=999)
        >>> # Then use the bbie_sc_id from supplementary_components (if exists)
        >>> result = update_bbie_sc(bbie_sc_id=bbie.to_bbiep.supplementary_components[0].bbie_sc_id, is_used=True)
        >>> result = update_bbie_sc(bbie_sc_id=789, is_used="1")
        >>> result = update_bbie_sc(bbie_sc_id=789, is_used="true")

        Toggle disable (unuse) a BBIE_SC during profiling:
        >>> result = update_bbie_sc(bbie_sc_id=789, is_used=False)
        >>> result = update_bbie_sc(bbie_sc_id=789, is_used="0")

        Update definition, example, and facets:
        >>> result = update_bbie_sc(bbie_sc_id=789, definition="Custom definition", example="Approved", facet_min_length=5, facet_max_length=100)
        >>> result = update_bbie_sc(bbie_sc_id=789, facet_min_length="5", facet_max_length="100")
    """
    try:
        is_used = str_to_bool(is_used)
    except ToolError:
        raise
    except Exception as e:
        raise ToolError(
            f"Type conversion error: {str(e)}. Please check your parameter types and try again."
        ) from e

    try:
        payload = await business_information_entity_service.update_bbie_sc(
            bbie_sc_id=bbie_sc_id,
            is_used=is_used,
            cardinality_min=cardinality_min,
            cardinality_max=cardinality_max,
            definition=definition,
            example=example,
            remark=remark,
            biz_term=biz_term,
            display_name=display_name,
            default_value=default_value,
            fixed_value=fixed_value,
            facet_min_length=facet_min_length,
            facet_max_length=facet_max_length,
            facet_pattern=facet_pattern,
            xbt_manifest_id=xbt_manifest_id,
            code_list_manifest_id=code_list_manifest_id,
            agency_id_list_manifest_id=agency_id_list_manifest_id,
        )
        return UpdateBbieScToolResponse.model_validate(payload, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to update BBIE supplementary component {bbie_sc_id}.") from exc


def _build_business_information_entity_service(
    session: AsyncSession,
    requester: AuthenticatedUser,
) -> BusinessInformationEntityService:
    """Build the requester-scoped business-information-entity service."""
    vendor_plugin = get_vendor_plugin()
    core_component_repository = vendor_plugin.create_core_component_repository(session)
    business_information_entity_repository = vendor_plugin.create_business_information_entity_repository(
        session,
        core_component_repository,
    )
    app_user_repository = vendor_plugin.create_app_user_repository(session)
    code_list_repository = vendor_plugin.create_code_list_repository(session)
    return BusinessInformationEntityService(
        business_information_entity_repository=business_information_entity_repository,
        requester=requester,
        account_service_repo=app_user_repository,
        code_list_repo=code_list_repository,
    )


def _parse_int_list(value: str | None, *, field_name: str) -> list[int] | None:
    """Parse a comma-separated integer list, returning ``None`` for empty input."""
    if value is None:
        return None
    stripped = value.strip()
    if not stripped:
        return None
    parts = [part.strip() for part in stripped.split(",") if part.strip()]
    if not parts:
        return None
    try:
        parsed = [int(part) for part in parts]
    except ValueError as exc:
        raise ToolError(f"{field_name} must be a comma-separated list of integers.") from exc
    invalid = [item for item in parsed if item <= 0]
    if invalid:
        raise ToolError(f"{field_name} must contain only positive integers.")
    return parsed


def _parse_required_int_list(value: list[int] | str, *, field_name: str) -> list[int]:
    """Parse a required integer list from either an array or comma-separated string."""
    if isinstance(value, list):
        if not value:
            raise ToolError(f"{field_name} must contain at least one positive integer.")
        if any(int(item) <= 0 for item in value):
            raise ToolError(f"{field_name} must contain only positive integers.")
        return [int(item) for item in value]

    parsed = _parse_int_list(value, field_name=field_name)
    if not parsed:
        raise ToolError(f"{field_name} must contain at least one positive integer.")
    return parsed


def _to_optional_int(value: int | str | None, *, field_name: str) -> int | None:
    """Convert an optional integer-like input to ``int``."""
    if value is None:
        return None
    if isinstance(value, int):
        if value <= 0:
            raise ToolError(f"{field_name} must be greater than 0.")
        return value
    try:
        parsed = int(value.strip())
    except ValueError as exc:
        raise ToolError(f"{field_name} must be an integer.") from exc
    if parsed <= 0:
        raise ToolError(f"{field_name} must be greater than 0.")
    return parsed


def _top_level_label(row: Any) -> str:
    """Return a human-friendly label for confirmation prompts."""
    asbiep = getattr(row, "asbiep", None)
    based_asccp_manifest = getattr(asbiep, "based_asccp_manifest", None)
    den = getattr(based_asccp_manifest, "den", None)
    return str(den or f"Top-Level ASBIEP {int(row.top_level_asbiep_id)}")
