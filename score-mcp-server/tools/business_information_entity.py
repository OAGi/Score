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
  with optional filters for library_id, release_id_list, den, version, status, state, is_deprecated,
  and date ranges. Supports custom sorting. Note: Only returns BIEs that have at least one
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

import hashlib
import logging
from typing import Annotated

from fastapi import HTTPException
from fastmcp import FastMCP
from fastmcp.exceptions import ToolError
from pydantic import Field

from services import (
    BusinessInformationEntityService,
    CoreComponentService,
    DataTypeService,
    DateRangeParams,
    PaginationParams,
)
from tools import _validate_auth_and_db, parse_order_by_to_sorts, _create_user_info
from tools.utils import validate_and_create_value_constraint
from middleware import get_current_user
from tools.core_component import _get_relationships_for_acc
from tools.models.biz_ctx import BusinessContextInfo
from tools.models.business_information_entity import (
    AbieInfo,
    AsbiepInfo,
    BbieScInfo,
    BbiepInfo,
    CreateTopLevelAsbiepResponse,
    CreateAsbieResponse,
    CreateBbieResponse,
    CreateBbieScResponse,
    DeleteTopLevelAsbiepResponse,
    GetAsbieResponse,
    GetBbieResponse,
    GetTopLevelAsbiepListResponse,
    GetTopLevelAsbiepListResponseEntry,
    GetTopLevelAsbiepResponse,
    TopLevelAsbiepInfo,
    TransferTopLevelAsbiepOwnershipResponse,
    UpdateTopLevelAsbiepResponse,
    UpdateAsbieResponse,
    UpdateBbieResponse,
    UpdateBbieScResponse,
    UpdateAsbiepRelationshipDetail,
    UpdateRoleOfAbieDetail,
    UpdateRelationshipDetail,
    UpdateAsbieRelationshipDetail,
    UpdateBbieRelationshipDetail,
    UpdateBbiepDetail,
    UpdateBbieScDetail,
    AsbieRelationshipInfo,
    BbieRelationshipInfo,
    Facet,
    ValueConstraint,
    PrimitiveRestriction,
    AsbiepRelationshipDetail,
    RoleOfAbieDetail,
    RelationshipDetail,
    AsbieRelationshipDetail,
    BbieRelationshipDetail,
    CreateBbiepInfo,
    CreateBbieScInfo,
    CreateAsbieRelationshipDetail,
    CreateBbieRelationshipDetail,
    CreateAsbiepRelationshipDetail,
    CreateRoleOfAbieDetail,
    CreateRelationshipDetail,
)
from tools.models.core_component import (
    AccInfo,
    AsccInfo,
    AsccpInfo,
    AsccRelationshipInfo,
    BccInfo,
    BccpInfo,
    BccRelationshipInfo,
    DtInfo,
    DtScInfo,
)
from tools.models.common import LibraryInfo, ReleaseInfo, WhoAndWhen
from tools.utils import parse_date_range, str_to_bool, str_to_int

# Configure logging
logger = logging.getLogger(__name__)

mcp = FastMCP("Score MCP Server - Business Information Entity Tools")


@mcp.tool(
    name="get_top_level_asbiep_list",
    description="Get a paginated list of Top-Level ASBIEPs (Association Business Information Entity Properties). Integer and boolean parameters accept both their native types and string representations (strings are automatically converted).",
    output_schema={
        "type": "object",
        "description": "Response containing paginated list of Top-Level ASBIEPs (Association Business Information Entity Properties). Integer and boolean parameters accept both their native types and string representations (strings are automatically converted to the appropriate type).",
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
        library_id: Annotated[int | str | None, Field(
            description="Filter by library ID using exact match. Accepts int, str, or None. String values are automatically converted to integers.",
            examples=[1, 2, 3, "1", "2", "3"],
            gt=0,
            title="Library ID"
        )] = None,
        release_id_list: Annotated[str | None, Field(
            description="Filter by release IDs using exact match. Comma-separated list of release IDs. Examples: '123', '123,456', '123,456,789'. If not provided, searches across all releases.",
            examples=["123", "123,456", "123,456,789"],
            title="Release ID List"
        )] = None,
        den: Annotated[str | None, Field(
            description="Filter by Data Element Name (DEN) or display name using partial match (case-insensitive). Matches either the DEN field or the display_name field.",
            examples=["Purchase Order", "Invoice", "Payment"],
            title="Data Element Name (DEN) or Display Name"
        )] = None,
        version: Annotated[str | None, Field(
            description="Filter by version using partial match (case-insensitive).",
            examples=["1.0", "2.1", "3.0"],
            title="Version"
        )] = None,
        status: Annotated[str | None, Field(
            description="Filter by status using partial match (case-insensitive).",
            examples=["Production", "Test", "Prototype"],
            title="Status"
        )] = None,
        state: Annotated[str | None, Field(
            description="Filter by state using partial match (case-insensitive).",
            examples=["Published", "Editing", "Draft"],
            title="State"
        )] = None,
        is_deprecated: Annotated[bool | str | None, Field(
            description="Filter by deprecation status. Accepts bool, str, or None. String values are converted: 'True'/'true'/'1' -> True, 'False'/'false'/'0' -> False.",
            examples=[True, False, "1", "0", "true", "false", "True", "False"],
            title="Is Deprecated"
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
            description="Comma-separated list of properties to order results by. Prefix with '-' for descending, '+' for ascending (default ascending). Allowed columns: den, version, status, state, creation_timestamp, last_update_timestamp. Example: '-creation_timestamp,+den' translates to 'creation_timestamp DESC, den ASC'.",
            examples=["-creation_timestamp,+den", "display_name", "-den", "-last_update_timestamp"],
            title="Order By"
        )] = None
) -> GetTopLevelAsbiepListResponse:
    """
    Get a paginated list of Top-Level ASBIEPs (Association Business Information Entity Properties).

    This function retrieves Top-Level ASBIEPs registered in connectCenter. Each Top-Level ASBIEP represents
    a complete Business Information Entity (BIE) that includes the top-level ASBIEP, its associated ASBIEP,
    and the related ABIE (Aggregation Business Information Entity). It supports pagination, filtering, and sorting.
    The release_id_list filter is optional - if not provided, searches across all releases.

    The function follows the relationship chain: top_level_asbiep -> asbiep -> based_asccp_manifest_id -> asccp_manifest_id -> asccp_id

    Args:
        offset (int | None, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int | None, optional): The maximum number of items to return. Must be a non-negative number. Defaults to 10.
        library_id (int | str | None, optional): Filter by library ID using exact match. Accepts int, str (converted to int), or None. Defaults to None.
        release_id_list (str | None, optional): Filter by release IDs using exact match. Comma-separated list of release IDs. Examples: '123', '123,456', '123,456,789'. If not provided, searches across all releases. Defaults to None.
        den (str | None, optional): Filter by Data Element Name (DEN) or display name using partial match (case-insensitive). Matches either the DEN field or the display_name field. Defaults to None.
        version (str | None, optional): Filter by version using partial match (case-insensitive). Defaults to None.
        status (str | None, optional): Filter by status using partial match (case-insensitive). Defaults to None.
        state (str | None, optional): Filter by state using partial match (case-insensitive). Defaults to None.
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
        >>> result = get_top_level_asbiep_list(offset=0, limit=10)
        >>> print(f"Found {result.total_items} Top-Level ASBIEPs")

        Filtered by specific releases:
        >>> result = get_top_level_asbiep_list(release_id_list="123,456", offset=0, limit=10)
        >>> print(f"Found {result.total_items} Top-Level ASBIEPs")

        Filtered by library:
        >>> result = get_top_level_asbiep_list(library_id=1, offset=0, limit=10)
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()
    
    # Convert string parameters to their proper types
    try:
        library_id = str_to_int(library_id) if library_id is not None else None
        is_deprecated = str_to_bool(is_deprecated)
    except ToolError:
        raise  # Re-raise ToolError as-is
    except Exception as e:
        raise ToolError(
            f"Type conversion error: {str(e)}. Please check your parameter types and try again."
        ) from e

    # Create service instance
    bie_service = BusinessInformationEntityService(requester=app_user)

    # Parse release_id_list parameter
    release_id_list_parsed = None
    if release_id_list:
        try:
            # Parse comma-separated string into list of integers
            release_id_list_parsed = [int(rid.strip()) for rid in release_id_list.split(',') if rid.strip()]

            # Validate that all IDs are positive integers
            invalid_ids = [rid for rid in release_id_list_parsed if rid <= 0]
            if invalid_ids:
                raise ToolError(
                    f"Invalid release IDs: {', '.join(map(str, invalid_ids))}. "
                    f"Release IDs must be positive integers. "
                    f"Use comma-separated format like '123,456,789'."
                )
        except ValueError as e:
            raise ToolError(
                f"Invalid release_id_list format: {str(e)}. "
                f"Please use comma-separated integers like '123,456,789'."
            ) from e

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
                f"Valid columns: {", ".join(bie_service.allowed_columns_for_order_by)}") from e

    # Get BIEs (Business Information Entities)
    try:
        page = bie_service.get_top_level_asbiep_list_by_release(
            library_id=library_id,
            release_id_list=release_id_list_parsed,
            den=den,
            version=version,
            status=status,
            state=state,
            is_deprecated=is_deprecated,
            created_on_params=created_on_params,
            last_updated_on_params=last_updated_on_params,
            pagination=pagination,
            sort_list=sort_list
        )

        return GetTopLevelAsbiepListResponse(
            total_items=page.total,
            offset=page.offset,
            limit=page.limit,
            items=[_create_business_information_entity_result(top_level_asbiep, bie_service)
                   for top_level_asbiep in page.items]
        )
    except HTTPException as e:
        logger.error(f"HTTP error retrieving BIEs (Business Information Entities): {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving BIEs (Business Information Entities): {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the BIEs (Business Information Entities): {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="get_top_level_asbiep",
    description="Get a top-level ASBIEP (Association Business Information Entity Property) by its ID. The response displays the ASBIEP first, with its relationships (ASBIE/BBIE) shown as children under the role_of_abie section. The relationships array is an ordered sequence that preserves the original order from the ABIE structure. This tool is used for profiling core components - when is_used=True, the component is profiled for practical use (asbie_id/bbie_id will be created), when is_used=False, it shows all available components for profiling. To explore relationships further: (1) If a relationship has asbie_id/bbie_id (is_used=True), use get_asbie_by_asbie_id(asbie_id) or get_bbie_by_bbie_id(bbie_id) to get full details. (2) If a relationship has no asbie_id/bbie_id (is_used=False), use get_asbie_by_based_ascc_manifest_id(top_level_asbiep_id, parent_abie_path, based_ascc_manifest_id) or get_bbie_by_based_bcc_manifest_id(top_level_asbiep_id, parent_abie_path, based_bcc_manifest_id) to explore the component structure before profiling.",
    output_schema={
        "type": "object",
        "description": "Response containing a top-level ASBIEP (Association Business Information Entity Property) with basic information. The ASBIEP is displayed first, with its relationships (ASBIE/BBIE) shown as children under the role_of_abie section. The relationships array is an ordered sequence that preserves the original order from the ABIE structure. Each relationship has an 'is_used' property indicating whether it's profiled for practical use (is_used=True means asbie_id/bbie_id exists). To explore relationships: (1) If relationship has asbie_id/bbie_id (is_used=True), use get_asbie_by_asbie_id(asbie_id) or get_bbie_by_bbie_id(bbie_id). (2) If relationship has no asbie_id/bbie_id (is_used=False), use get_asbie_by_based_ascc_manifest_id(top_level_asbiep_id, parent_abie_path, based_ascc_manifest_id) or get_bbie_by_based_bcc_manifest_id(top_level_asbiep_id, parent_abie_path, based_bcc_manifest_id) - extract parent_abie_path from relationship.path and manifest_id from relationship.based_ascc/based_bcc.",
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
                                    "release_num": {"type": ["string", "null"], "description": "Release number",
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
                    "path": {"type": ["string", "null"],
                             "description": "[INTERNAL] Path of the ASBIEP - used internally for update_asbie() and update_bbie() operations",
                             "example": "/PurchaseOrder"},
                    "hash_path": {"type": ["string", "null"],
                                  "description": "[INTERNAL] SHA256 hashed value of the path field - used internally for efficient lookups",
                                  "example": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"},
                    "role_of_abie": {
                        "type": "object",
                        "description": "Role of ABIE information - contains the ABIE details and its relationships as children",
                        "properties": {
                            "abie_id": {"type": ["integer", "null"], "description": "Unique identifier for the ABIE",
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
                                "description": "Ordered sequence of relationships (ASBIE/BBIE) displayed as children under the ABIE. The order is preserved from the original ABIE structure. Each relationship has an 'is_used' property: when is_used=True, the relationship is profiled for practical use (asbie_id/bbie_id exists), when is_used=False, it shows available relationships for profiling. To explore relationships: (1) For ASBIE relationships with asbie_id (is_used=True): use get_asbie_by_asbie_id(asbie_id) to get full details. (2) For ASBIE relationships without asbie_id (is_used=False): use get_asbie_by_based_ascc_manifest_id(top_level_asbiep_id, parent_abie_path, based_ascc_manifest_id) - use the relationship's 'path' for parent_abie_path and 'based_ascc.ascc_manifest_id' for based_ascc_manifest_id. (3) For BBIE relationships with bbie_id (is_used=True): use get_bbie_by_bbie_id(bbie_id) to get full details. (4) For BBIE relationships without bbie_id (is_used=False): use get_bbie_by_based_bcc_manifest_id(top_level_asbiep_id, parent_abie_path, based_bcc_manifest_id) - use the relationship's 'path' for parent_abie_path and 'based_bcc.bcc_manifest_id' for based_bcc_manifest_id. This includes all nested relationships from the hierarchical CC (Core Component) structure, presented as a single flat ordered list for traversal.",
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
                                                "path": {"type": "string",
                                                         "description": "Hierarchical path string indicating the position of this component within the BIE structure"},
                                                "hash_path": {"type": "string",
                                                              "description": "Hashed version of the path for efficient lookups"},
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
                                                                             "description": "Unique identifier for the ASCC manifest"},
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
                                            "required": ["component_type", "is_used", "path", "hash_path",
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
                                                "path": {"type": "string",
                                                         "description": "Hierarchical path string indicating the position of this component within the BIE structure"},
                                                "hash_path": {"type": "string",
                                                              "description": "Hashed version of the path for efficient lookups"},
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
                                                                            "description": "Unique identifier for the BCC manifest"},
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
                                                    "required": ["xbtManifestId", "codeListManifestId", "agencyIdListManifestId"]
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
                                                    "required": ["facet_min_length", "facet_max_length", "facet_pattern"]
                                                },
                                                "to_bbiep_id": {"type": ["integer", "null"],
                                                                "description": "Unique identifier for the target BBIEP that this BBIE connects to (if available)"}
                                            },
                                            "required": ["component_type", "is_used", "path", "hash_path",
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
        "required": ["top_level_asbiep_id", "asbiep", "version", "status", "business_contexts", "state",
                     "is_deprecated", "deprecated_reason", "deprecated_remark", "owner", "created", "last_updated"]
    }
)
async def get_top_level_asbiep(top_level_asbiep_id: int) -> GetTopLevelAsbiepResponse:
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
              * path and hash_path for constructing parent paths
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Validate input parameters
    if top_level_asbiep_id <= 0:
        raise ToolError("top_level_asbiep_id must be a positive integer.")

    # Get ASBIEP
    try:
        bie_service = BusinessInformationEntityService(requester=app_user)

        # First get the top-level ASBIEP to get the asbiep_id
        top_level_asbiep = bie_service.get_top_level_asbiep_by_id(top_level_asbiep_id)
        if not top_level_asbiep:
            raise ToolError(f"Top-Level ASBIEP with top_level_asbiep_id {top_level_asbiep_id} not found.")

        # Get the ASBIEP using the asbiep_id
        asbiep = bie_service.get_asbiep(top_level_asbiep.asbiep_id)
        if not asbiep:
            raise ToolError(
                f"ASBIEP (Association Business Information Entity Property) with asbiep_id {top_level_asbiep.asbiep_id} not found.")

        return _create_asbiep_result(asbiep, bie_service)
    except HTTPException as e:
        logger.error(f"HTTP error retrieving ASBIEP (Association Business Information Entity Property): {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"ASBIEP (Association Business Information Entity Property) with top_level_asbiep_id {top_level_asbiep_id} not found.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving ASBIEP (Association Business Information Entity Property): {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the ASBIEP (Association Business Information Entity Property): {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="get_asbie_by_asbie_id",
    description="Get an ASBIE (Association Business Information Entity) by its ASBIE ID. This function fetches the complete ASBIE information from the database when you have the asbie_id.",
    output_schema={
        "type": "object",
        "description": "Response containing ASBIE (Association Business Information Entity) information with its ASBIEP. Used for profiling core components – when is_used=True, the component is profiled for practical use (asbie_id will be created); when is_used=False, it shows all available components for profiling.",
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
                            "release_num": {"type": ["string", "null"], "description": "Release number",
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
                    "ascc_manifest_id": {"type": "integer", "description": "Unique identifier for the ASCC manifest",
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
                                    "release_num": {"type": ["string", "null"], "description": "Release number",
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
                    "path": {"type": ["string", "null"],
                             "description": "[INTERNAL] Hierarchical path string - used internally for update_asbie() and update_bbie() operations",
                             "example": "/PurchaseOrder/Details"},
                    "hash_path": {"type": ["string", "null"],
                                  "description": "[INTERNAL] SHA256 hashed value of the path field - used internally for efficient lookups",
                                  "example": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"},
                    "role_of_abie": {
                        "type": "object",
                        "description": "Role of ABIE information",
                        "properties": {
                            "abie_id": {"type": ["integer", "null"], "description": "Unique identifier for the ABIE",
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
                                                "path": {"type": "string",
                                                         "description": "[INTERNAL] Hierarchical path string - used internally for update_asbie() and update_bbie() operations",
                                                         "example": "/PurchaseOrder/Details"},
                                                "hash_path": {"type": "string",
                                                              "description": "[INTERNAL] SHA256 hashed value of the path field - used internally for efficient lookups",
                                                              "example": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"},
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
                                                                             "description": "Unique identifier for the ASCC manifest",
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
                                            "required": ["component_type", "is_used", "path", "hash_path",
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
                                                "path": {"type": "string",
                                                         "description": "[INTERNAL] Hierarchical path string - used internally for update_asbie() and update_bbie() operations",
                                                         "example": "/PurchaseOrder/Amount"},
                                                "hash_path": {"type": "string",
                                                              "description": "[INTERNAL] SHA256 hashed value of the path field - used internally for efficient lookups",
                                                              "example": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"},
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
                                                    "required": ["xbtManifestId", "codeListManifestId", "agencyIdListManifestId"]
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
                                                    "required": ["facet_min_length", "facet_max_length", "facet_pattern"]
                                                },
                                                "to_bbiep_id": {"type": ["integer", "null"],
                                                                "description": "Unique identifier for the target BBIEP that this BBIE connects to (if available)",
                                                                "example": 12348}
                                            },
                                            "required": ["component_type", "is_used", "path", "hash_path",
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
            "path": {"type": "string",
                     "description": "[INTERNAL] Hierarchical path string - used internally for update_asbie() and update_bbie() operations",
                     "example": "/PurchaseOrder/Details"},
            "hash_path": {"type": "string",
                          "description": "[INTERNAL] SHA256 hashed value of the path field - used internally for efficient lookups",
                          "example": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"},
            "cardinality_min": {"type": "integer",
                                "description": "Minimum cardinality (minimum number of occurrences required, typically 0 or 1)",
                                "example": 0},
            "cardinality_max": {"type": "integer",
                                "description": "Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)",
                                "example": 1},
            "is_nillable": {"type": "boolean", "description": "Whether the ASBIE can have a nil/null value",
                            "example": False},
            "remark": {"type": ["string", "null"], "description": "Additional remarks or notes about the ASBIE",
                       "example": "Used for purchase orders"}
        },
        "required": ["owner_top_level_asbiep", "based_ascc", "to_asbiep", "is_used", "path", "hash_path",
                     "cardinality_min", "cardinality_max", "is_nillable"]
    }
)
async def get_asbie_by_asbie_id(
        asbie_id: Annotated[int, Field(
            description="The unique identifier of the ASBIE.",
            examples=[12345],
            title="ASBIE ID",
            gt=0
        )]
) -> GetAsbieResponse:
    """
    Get an ASBIE (Association Business Information Entity) by its ASBIE ID.

    This tool fetches the complete ASBIE information from the database when you have the asbie_id.

    The response includes:
    - asbie_id: The ASBIE ID
    - guid: The ASBIE GUID
    - based_ascc: The ASCC information this ASBIE is based on
    - to_asbiep: The ASBIEP information this ASBIE points to
    - All other ASBIE properties (is_used, path, hash_path, cardinality, etc.)

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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Validate input parameters
    if asbie_id <= 0:
        raise ToolError("asbie_id must be a positive integer.")

    try:
        bie_service = BusinessInformationEntityService(requester=app_user)
        cc_service = CoreComponentService()

        # Get ASBIE by ID from database
        asbie = bie_service.get_asbie_by_asbie_id(asbie_id)
        if not asbie:
            raise ToolError(f"ASBIE with asbie_id {asbie_id} not found.")

        # Get the top-level ASBIEP ID from the ASBIE
        top_level_asbiep_id = asbie.owner_top_level_asbiep_id

        # Get ASCC information
        if not asbie.based_ascc_manifest or not asbie.based_ascc_manifest.ascc:
            raise ToolError(f"ASBIE with asbie_id {asbie_id} has no associated ASCC manifest or ASCC.")

        ascc_manifest = asbie.based_ascc_manifest
        ascc = ascc_manifest.ascc
        based_ascc = AsccInfo(
            ascc_manifest_id=ascc_manifest.ascc_manifest_id,
            ascc_id=ascc.ascc_id,
            guid=ascc.guid,
            den=ascc_manifest.den,
            cardinality_min=ascc.cardinality_min,
            cardinality_max=ascc.cardinality_max,
            is_deprecated=ascc.is_deprecated,
            definition=ascc.definition,
            definition_source=ascc.definition_source,
            from_acc_manifest_id=ascc_manifest.from_acc_manifest_id,
            to_asccp_manifest_id=ascc_manifest.to_asccp_manifest_id
        )

        # Get ASCCP information for to_asbiep
        asccp_manifest = cc_service.get_asccp_by_manifest_id(ascc_manifest.to_asccp_manifest_id)
        if not asccp_manifest:
            raise ToolError(
                f"ASCC manifest with ID {ascc_manifest.ascc_manifest_id} has no associated ASCCP manifest or ASCCP.")

        # Get ASBIEP information
        if not asbie.to_asbiep_id:
            raise ToolError(f"ASBIE with asbie_id {asbie_id} has no associated ASBIEP.")

        asbiep = bie_service.get_asbiep(asbie.to_asbiep_id)
        if not asbiep:
            raise ToolError(f"ASBIEP with ID {asbie.to_asbiep_id} not found.")

        # Get the ABIE using the role_of_abie_id
        asbiep_info = _get_asbiep_info(asbiep.owner_top_level_asbiep_id,
                                       asbiep.asbiep_id, asccp_manifest.asccp_manifest_id, asbie.path)

        return GetAsbieResponse(
            asbie_id=asbie.asbie_id,
            owner_top_level_asbiep=asbiep_info.owner_top_level_asbiep,
            guid=asbie.guid,
            based_ascc=based_ascc,
            to_asbiep=asbiep_info,
            is_used=asbie.is_used,
            path=asbie.path,
            hash_path=asbie.hash_path,
            cardinality_min=asbie.cardinality_min,
            cardinality_max=asbie.cardinality_max,
            is_nillable=asbie.is_nillable,
            remark=asbie.remark
        )

    except HTTPException as e:
        logger.error(f"HTTP error retrieving ASBIE: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(f"ASBIE not found: {e.detail}") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving ASBIE: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the ASBIE: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="get_asbie_by_based_ascc_manifest_id",
    description="Get an ASBIE (Association Business Information Entity) by its based ASCC manifest ID. This function returns basic information based on based_ascc_manifest_id and parent_abie_path when you don't have the asbie_id. It can also find an existing ASBIE if one exists with the given based_ascc_manifest_id.",
    output_schema={
        "type": "object",
        "description": "Response containing ASBIE (Association Business Information Entity) information with its ASBIEP. Used for profiling core components – when is_used=True, the component is profiled for practical use (asbie_id will be created); when is_used=False, it shows all available components for profiling.",
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
                            "release_num": {"type": ["string", "null"], "description": "Release number",
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
                    "ascc_manifest_id": {"type": "integer", "description": "Unique identifier for the ASCC manifest",
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
                                    "release_num": {"type": ["string", "null"], "description": "Release number",
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
                    "path": {"type": ["string", "null"],
                             "description": "[INTERNAL] Hierarchical path string - used internally for update_asbie() and update_bbie() operations",
                             "example": "/PurchaseOrder/Details"},
                    "hash_path": {"type": ["string", "null"],
                                  "description": "[INTERNAL] SHA256 hashed value of the path field - used internally for efficient lookups",
                                  "example": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"},
                    "role_of_abie": {
                        "type": "object",
                        "description": "Role of ABIE information",
                        "properties": {
                            "abie_id": {"type": ["integer", "null"], "description": "Unique identifier for the ABIE",
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
                                                "path": {"type": "string",
                                                         "description": "[INTERNAL] Hierarchical path string - used internally for update_asbie() and update_bbie() operations",
                                                         "example": "/PurchaseOrder/Details"},
                                                "hash_path": {"type": "string",
                                                              "description": "[INTERNAL] SHA256 hashed value of the path field - used internally for efficient lookups",
                                                              "example": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"},
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
                                                                             "description": "Unique identifier for the ASCC manifest",
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
                                            "required": ["component_type", "is_used", "path", "hash_path",
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
                                                "path": {"type": "string",
                                                         "description": "[INTERNAL] Hierarchical path string - used internally for update_asbie() and update_bbie() operations",
                                                         "example": "/PurchaseOrder/Amount"},
                                                "hash_path": {"type": "string",
                                                              "description": "[INTERNAL] SHA256 hashed value of the path field - used internally for efficient lookups",
                                                              "example": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"},
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
                                                    "required": ["xbtManifestId", "codeListManifestId", "agencyIdListManifestId"]
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
                                                    "required": ["facet_min_length", "facet_max_length", "facet_pattern"]
                                                },
                                                "to_bbiep_id": {"type": ["integer", "null"],
                                                                "description": "Unique identifier for the target BBIEP that this BBIE connects to (if available)",
                                                                "example": 12348}
                                            },
                                            "required": ["component_type", "is_used", "path", "hash_path",
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
            "path": {"type": "string",
                     "description": "[INTERNAL] Hierarchical path string - used internally for update_asbie() and update_bbie() operations",
                     "example": "/PurchaseOrder/Details"},
            "hash_path": {"type": "string",
                          "description": "[INTERNAL] SHA256 hashed value of the path field - used internally for efficient lookups",
                          "example": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"},
            "cardinality_min": {"type": "integer",
                                "description": "Minimum cardinality (minimum number of occurrences required, typically 0 or 1)",
                                "example": 0},
            "cardinality_max": {"type": "integer",
                                "description": "Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)",
                                "example": 1},
            "is_nillable": {"type": "boolean", "description": "Whether the ASBIE can have a nil/null value",
                            "example": False},
            "remark": {"type": ["string", "null"], "description": "Additional remarks or notes about the ASBIE",
                       "example": "Used for purchase orders"}
        },
        "required": ["owner_top_level_asbiep", "based_ascc", "to_asbiep", "is_used", "path", "hash_path",
                     "cardinality_min", "cardinality_max", "is_nillable"]
    }
)
async def get_asbie_by_based_ascc_manifest_id(
        top_level_asbiep_id: Annotated[int, Field(
            description="The top-level ASBIEP (Association Business Information Entity Property) ID.",
            examples=[123],
            title="Top-level ASBIEP ID",
            gt=0
        )],
        parent_abie_path: Annotated[str, Field(
            description="The parent path for constructing the ASBIEP path.",
            examples=["ASCCP-123>ACC-456"],
            title="Parent Path"
        )],
        based_ascc_manifest_id: Annotated[int, Field(
            description="The ASCC manifest ID that this ASBIE is based on.",
            examples=[67890],
            title="Based ASCC Manifest ID",
            gt=0
        )]
) -> GetAsbieResponse:
    """
    Get an ASBIE (Association Business Information Entity) by its based ASCC manifest ID.

    This tool returns basic information based on based_ascc_manifest_id and parent_abie_path when you don't have the asbie_id.
    It will also check if an existing ASBIE exists with the given based_ascc_manifest_id and return it if found.

    The response includes:
    - asbie_id: The ASBIE ID (null if not yet created)
    - guid: The ASBIE GUID (null if not yet created)
    - based_ascc: The ASCC information this ASBIE is based on
    - to_asbiep: The ASBIEP information this ASBIE points to
    - All other ASBIE properties (is_used, path, hash_path, cardinality, etc.)

    Args:
        top_level_asbiep_id (int): The top-level ASBIEP ID
        parent_abie_path (str): The parent path for constructing the ASBIEP path
        based_ascc_manifest_id (int): The ASCC manifest ID that this ASBIE is based on

    Returns:
        GetAsbieResponse: ASBIE information including all properties

    Raises:
        ToolError: If validation fails, the ASCC manifest is not found, or a database error occurs.

    Examples:
        Get basic ASBIE info by based ASCC manifest ID:
        >>> result = get_asbie_by_based_ascc_manifest_id(
        ...     top_level_asbiep_id=123,
        ...     parent_abie_path="ASCCP-123>ACC-456",
        ...     based_ascc_manifest_id=67890
        ... )
        >>> print(f"Top-level ASBIEP ID: {result.owner_top_level_asbiep.top_level_asbiep_id}")
        >>> print(f"Based ASCC: {result.based_ascc.den}")
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Validate input parameters
    if based_ascc_manifest_id <= 0:
        raise ToolError("based_ascc_manifest_id must be a positive integer.")

    if not parent_abie_path or not parent_abie_path.strip():
        raise ToolError("parent_abie_path is required and cannot be empty.")

    try:
        bie_service = BusinessInformationEntityService(requester=app_user)
        cc_service = CoreComponentService()

        top_level_asbiep = bie_service.get_top_level_asbiep_by_id(top_level_asbiep_id)
        if top_level_asbiep is None:
            raise ToolError(f"Top-level ASBIEP with ID {top_level_asbiep_id} not found.")

        # Get ASCC information
        ascc_manifest = cc_service.get_ascc_manifest(based_ascc_manifest_id)
        if not ascc_manifest or not ascc_manifest.ascc:
            raise ToolError(f"ASCC manifest with ID {based_ascc_manifest_id} not found.")

        ascc = ascc_manifest.ascc
        based_ascc = AsccInfo(
            ascc_manifest_id=ascc_manifest.ascc_manifest_id,
            ascc_id=ascc.ascc_id,
            guid=ascc.guid,
            den=ascc_manifest.den,
            cardinality_min=ascc.cardinality_min,
            cardinality_max=ascc.cardinality_max,
            is_deprecated=ascc.is_deprecated,
            definition=ascc.definition,
            definition_source=ascc.definition_source,
            from_acc_manifest_id=ascc_manifest.from_acc_manifest_id,
            to_asccp_manifest_id=ascc_manifest.to_asccp_manifest_id
        )

        # Construct paths
        asbie_path = f"{parent_abie_path}>ASCC-{based_ascc_manifest_id}"
        hash_path = hashlib.sha256(asbie_path.encode()).hexdigest()

        # Try to find existing ASBIE by based_ascc_manifest_id and hash_path
        asbie = bie_service.get_asbie_by_based_ascc_manifest_id(
            based_ascc_manifest_id=based_ascc_manifest_id,
            owner_top_level_asbiep_id=top_level_asbiep_id,
            hash_path=hash_path
        )

        # Get ASCCP information for to_asbiep
        asccp_manifest = cc_service.get_asccp_by_manifest_id(ascc_manifest.to_asccp_manifest_id)
        if not asccp_manifest:
            raise ToolError(
                f"ASCC manifest with ID {based_ascc_manifest_id} has no associated ASCCP manifest or ASCCP.")

        if asbie:
            # Found existing ASBIE - return it with full details
            if not asbie.to_asbiep_id:
                raise ToolError(f"ASBIE with asbie_id {asbie.asbie_id} has no associated ASBIEP.")

            asbiep = bie_service.get_asbiep(asbie.to_asbiep_id)
            if not asbiep:
                raise ToolError(f"ASBIEP with ID {asbie.to_asbiep_id} not found.")

            asbiep_info = _get_asbiep_info(asbiep.owner_top_level_asbiep_id,
                                           asbiep.asbiep_id, asccp_manifest.asccp_manifest_id, asbie.path)

            return GetAsbieResponse(
                asbie_id=asbie.asbie_id,
                owner_top_level_asbiep=asbiep_info.owner_top_level_asbiep,
                guid=asbie.guid,
                based_ascc=based_ascc,
                to_asbiep=asbiep_info,
                is_used=asbie.is_used,
                path=asbie.path,
                hash_path=asbie.hash_path,
                cardinality_min=asbie.cardinality_min,
                cardinality_max=asbie.cardinality_max,
                is_nillable=asbie.is_nillable,
                remark=asbie.remark
            )
        else:
            # No existing ASBIE found - return basic info
            asbiep_path = f"{asbie_path}>ASCCP-{asccp_manifest.asccp_manifest_id}"

            # Get the ABIE using the role_of_abie_id
            asbiep_info = _get_asbiep_info(top_level_asbiep.top_level_asbiep_id,
                                           None, asccp_manifest.asccp_manifest_id, asbie_path)

            return GetAsbieResponse(
                asbie_id=None,
                owner_top_level_asbiep=asbiep_info.owner_top_level_asbiep,
                guid=None,
                based_ascc=based_ascc,
                to_asbiep=asbiep_info,
                is_used=False,  # Default for basic mode
                path=asbie_path,
                hash_path=hash_path,
                cardinality_min=ascc.cardinality_min,
                cardinality_max=ascc.cardinality_max,
                is_nillable=False,  # Default for basic mode
                remark=None
            )

    except HTTPException as e:
        logger.error(f"HTTP error retrieving ASBIE: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(f"ASBIE not found: {e.detail}") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving ASBIE: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the ASBIE: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="get_bbie_by_bbie_id",
    description="Get a BBIE (Basic Business Information Entity) by its BBIE ID. This function fetches the complete BBIE information from the database when you have the bbie_id.",
    output_schema={
        "type": "object",
        "description": "Response containing BBIE (Basic Business Information Entity) information with its BBIEP. Used for profiling core components – when is_used=True, the component is profiled for practical use (bbie_id will be created); when is_used=False, it shows all available components for profiling.",
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
                            "release_num": {"type": ["string", "null"], "description": "Release number",
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
                    "bcc_manifest_id": {"type": "integer", "description": "Unique identifier for the BCC manifest",
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
                    "path": {"type": "string",
                             "description": "[INTERNAL] Hierarchical path string - used internally for update_asbie() and update_bbie() operations",
                             "example": "/PurchaseOrder/Amount"},
                    "hash_path": {"type": "string",
                                  "description": "[INTERNAL] SHA256 hashed value of the path field - used internally for efficient lookups",
                                  "example": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"},
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
                                "path": {"type": "string",
                                         "description": "[INTERNAL] Hierarchical path string - used internally for update_asbie() and update_bbie() operations",
                                         "example": "/PurchaseOrder/Amount/CurrencyCode"},
                                "hash_path": {"type": "string",
                                              "description": "[INTERNAL] SHA256 hashed value of the path field - used internally for efficient lookups",
                                              "example": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"},
                                "definition": {"type": ["string", "null"], "description": "Definition of the BBIE SC",
                                               "example": "Currency code"},
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
                                        "facet_pattern": {"type": ["string", "null"], "description": "Pattern constraint",
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
                                                "release_num": {"type": ["string", "null"],
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
                            "required": ["based_dt_sc", "path", "hash_path", "cardinality_min", "cardinality_max",
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
                                    "release_num": {"type": ["string", "null"], "description": "Release number",
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
                "required": ["based_bccp", "path", "hash_path", "supplementary_components", "owner_top_level_asbiep"]
            },
            "is_used": {"type": "boolean",
                        "description": "Whether this BBIE is currently being used (profiled) in the BIE",
                        "example": True},
            "path": {"type": "string",
                     "description": "[INTERNAL] Hierarchical path string - used internally for update_asbie() and update_bbie() operations",
                     "example": "/PurchaseOrder/Amount"},
            "hash_path": {"type": "string",
                          "description": "[INTERNAL] SHA256 hashed value of the path field - used internally for efficient lookups",
                          "example": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"},
            "cardinality_min": {"type": "integer",
                                "description": "Minimum cardinality (minimum number of occurrences required, typically 0 or 1)",
                                "example": 0},
            "cardinality_max": {"type": "integer",
                                "description": "Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)",
                                "example": 1},
            "is_nillable": {"type": "boolean", "description": "Whether the BBIE can have a nil/null value",
                            "example": False},
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
        "required": ["owner_top_level_asbiep", "based_bcc", "to_bbiep", "is_used", "path", "hash_path",
                     "cardinality_min", "cardinality_max", "is_nillable"]
    }
)
async def get_bbie_by_bbie_id(
        bbie_id: Annotated[int, Field(
            description="The unique identifier of the BBIE.",
            examples=[9876],
            title="BBIE ID",
            gt=0
        )]
) -> GetBbieResponse:
    """
    Get a BBIE (Basic Business Information Entity) by its BBIE ID.

    This tool fetches the complete BBIE information from the database when you have the bbie_id.

    The response includes:
    - bbie_id: The BBIE ID
    - guid: The BBIE GUID
    - based_bcc: The BCC information this BBIE is based on
    - to_bbiep: The BBIEP information this BBIE connects to
    - All other BBIE properties (is_used, path, hash_path, cardinality, facets, etc.)
    - primitiveRestriction: Required field that must not be None. Contains exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId (not multiple, not none).
    - valueConstraint: Optional field for default_value or fixed_value constraints
    - supplementary_components in to_bbiep for use with create_bbie_sc() and update_bbie_sc()

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
    # Validate auth/DB
    app_user, engine = _validate_auth_and_db()

    if bbie_id <= 0:
        raise ToolError("bbie_id must be a positive integer.")

    bie_service = BusinessInformationEntityService(requester=app_user)
    cc_service = CoreComponentService()

    # Get BBIE by ID from database
    bbie = bie_service.get_bbie_by_bbie_id(bbie_id)
    if not bbie:
        raise ToolError(f"BBIE with bbie_id {bbie_id} not found.")

    # Get the top-level ASBIEP ID from the BBIE
    top_level_asbiep_id = bbie.owner_top_level_asbiep_id

    # Validate top_level_asbiep
    top_level_asbiep = bie_service.get_top_level_asbiep_by_id(top_level_asbiep_id)
    if not top_level_asbiep:
        raise ToolError(f"Top-level ASBIEP with ID {top_level_asbiep_id} not found.")

    owner_top_level_info = _create_top_level_asbiep_info(top_level_asbiep)

    # Get BCC information
    if not bbie.based_bcc_manifest or not bbie.based_bcc_manifest.bcc:
        raise ToolError(f"BBIE with bbie_id {bbie_id} has no associated BCC manifest or BCC.")

    bcc_manifest = bbie.based_bcc_manifest
    bcc = bcc_manifest.bcc
    based_bcc = BccInfo(
        bcc_manifest_id=bcc_manifest.bcc_manifest_id,
        bcc_id=bcc.bcc_id,
        guid=bcc.guid,
        den=bcc_manifest.den,
        cardinality_min=bcc.cardinality_min,
        cardinality_max=bcc.cardinality_max,
        entity_type='Attribute' if bcc.entity_type == 0 else 'Element',
        is_nillable=bcc.is_nillable,
        is_deprecated=bcc.is_deprecated,
        definition=bcc.definition,
        definition_source=bcc.definition_source,
        from_acc_manifest_id=bcc_manifest.from_acc_manifest_id,
        to_bccp_manifest_id=bcc_manifest.to_bccp_manifest_id
    )

    # to_bbiep
    bbiep_info = _get_bbiep_info(bbie.owner_top_level_asbiep_id, bbie_id, bbie.to_bbiep_id,
                                 bcc_manifest.to_bccp_manifest_id, bbie.path)

    # Create Facet object if any facet values exist
    facet = None
    if bbie.facet_min_length is not None or bbie.facet_max_length is not None or bbie.facet_pattern is not None:
        facet = Facet(
            facet_min_length=bbie.facet_min_length,
            facet_max_length=bbie.facet_max_length,
            facet_pattern=bbie.facet_pattern
        )
    
    # Create ValueConstraint object with validation
    value_constraint = validate_and_create_value_constraint(
        default_value=bbie.default_value,
        fixed_value=bbie.fixed_value
    )
    
    # Create PrimitiveRestriction object with validation
    primitive_restriction = _validate_and_create_primitive_restriction(
        xbt_manifest_id=bbie.xbt_manifest_id,
        code_list_manifest_id=bbie.code_list_manifest_id,
        agency_id_list_manifest_id=bbie.agency_id_list_manifest_id
    )
    
    return GetBbieResponse(
        bbie_id=bbie.bbie_id,
        owner_top_level_asbiep=owner_top_level_info,
        guid=bbie.guid,
        based_bcc=based_bcc,
        to_bbiep=bbiep_info,
        is_used=bbie.is_used,
        path=bbie.path,
        hash_path=bbie.hash_path,
        cardinality_min=bbie.cardinality_min,
        cardinality_max=bbie.cardinality_max,
        is_nillable=bbie.is_nillable,
        remark=bbie.remark,
        primitiveRestriction=primitive_restriction,
        valueConstraint=value_constraint,
        facet=facet
    )


@mcp.tool(
    name="get_bbie_by_based_bcc_manifest_id",
    description="Get a BBIE (Basic Business Information Entity) by its based BCC manifest ID. This function returns basic information based on based_bcc_manifest_id and parent_abie_path when you don't have the bbie_id. It can also find an existing BBIE if one exists with the given based_bcc_manifest_id.",
    output_schema={
        "type": "object",
        "description": "Response containing BBIE (Basic Business Information Entity) information with its BBIEP. Used for profiling core components – when is_used=True, the component is profiled for practical use (bbie_id will be created); when is_used=False, it shows all available components for profiling.",
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
                            "release_num": {"type": ["string", "null"], "description": "Release number",
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
                    "bcc_manifest_id": {"type": "integer", "description": "Unique identifier for the BCC manifest",
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
                    "path": {"type": "string",
                             "description": "[INTERNAL] Hierarchical path string - used internally for update_asbie() and update_bbie() operations",
                             "example": "/PurchaseOrder/Amount"},
                    "hash_path": {"type": "string",
                                  "description": "[INTERNAL] SHA256 hashed value of the path field - used internally for efficient lookups",
                                  "example": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"},
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
                                "path": {"type": "string",
                                         "description": "[INTERNAL] Hierarchical path string - used internally for update_asbie() and update_bbie() operations",
                                         "example": "/PurchaseOrder/Amount/CurrencyCode"},
                                "hash_path": {"type": "string",
                                              "description": "[INTERNAL] SHA256 hashed value of the path field - used internally for efficient lookups",
                                              "example": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"},
                                "definition": {"type": ["string", "null"], "description": "Definition of the BBIE SC",
                                               "example": "Currency code"},
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
                                        "facet_pattern": {"type": ["string", "null"], "description": "Pattern constraint",
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
                                                "release_num": {"type": ["string", "null"],
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
                            "required": ["based_dt_sc", "path", "hash_path", "cardinality_min", "cardinality_max",
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
                                    "release_num": {"type": ["string", "null"], "description": "Release number",
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
                "required": ["based_bccp", "path", "hash_path", "supplementary_components", "owner_top_level_asbiep"]
            },
            "is_used": {"type": "boolean",
                        "description": "Whether this BBIE is currently being used (profiled) in the BIE",
                        "example": True},
            "path": {"type": "string",
                     "description": "[INTERNAL] Hierarchical path string - used internally for update_asbie() and update_bbie() operations",
                     "example": "/PurchaseOrder/Amount"},
            "hash_path": {"type": "string",
                          "description": "[INTERNAL] SHA256 hashed value of the path field - used internally for efficient lookups",
                          "example": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"},
            "cardinality_min": {"type": "integer",
                                "description": "Minimum cardinality (minimum number of occurrences required, typically 0 or 1)",
                                "example": 0},
            "cardinality_max": {"type": "integer",
                                "description": "Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)",
                                "example": 1},
            "is_nillable": {"type": "boolean", "description": "Whether the BBIE can have a nil/null value",
                            "example": False},
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
        "required": ["owner_top_level_asbiep", "based_bcc", "to_bbiep", "is_used", "path", "hash_path",
                     "cardinality_min", "cardinality_max", "is_nillable", "primitiveRestriction"]
    }
)
async def get_bbie_by_based_bcc_manifest_id(
        top_level_asbiep_id: Annotated[int, Field(
            description="The top-level ASBIEP (Association Business Information Entity Property) ID.",
            examples=[123],
            title="Top-level ASBIEP ID",
            gt=0
        )],
        parent_abie_path: Annotated[str, Field(
            description="The parent path for constructing the BBIE path.",
            examples=["ASCCP-123>ACC-456"],
            title="Parent Path"
        )],
        based_bcc_manifest_id: Annotated[int, Field(
            description="The BCC manifest ID that this BBIE is based on.",
            examples=[456],
            title="Based BCC Manifest ID",
            gt=0
        )]
) -> GetBbieResponse:
    """
    Get a BBIE (Basic Business Information Entity) by its based BCC manifest ID.

    This tool returns basic information based on based_bcc_manifest_id and parent_abie_path when you don't have the bbie_id.
    It will also check if an existing BBIE exists with the given based_bcc_manifest_id and return it if found.

    The response includes:
    - bbie_id: The BBIE ID (null if not yet created)
    - guid: The BBIE GUID (null if not yet created)
    - based_bcc: The BCC information this BBIE is based on
    - to_bbiep: The BBIEP information this BBIE connects to
    - All other BBIE properties (is_used, path, hash_path, cardinality, facets, etc.)
    - primitiveRestriction: Required field that must not be None. Contains exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId (not multiple, not none). Fetched from DtAwdPri if no BBIE exists yet.
    - valueConstraint: Optional field for default_value or fixed_value constraints (fetched from BCC if no BBIE exists yet)
    - supplementary_components in to_bbiep for use with create_bbie_sc() and update_bbie_sc()

    Args:
        top_level_asbiep_id (int): The top-level ASBIEP ID
        parent_abie_path (str): The parent path for constructing the BBIE path
        based_bcc_manifest_id (int): The BCC manifest ID that this BBIE is based on

    Returns:
        GetBbieResponse: BBIE information including all properties. The primitiveRestriction field is required and must not be None, even when no BBIE exists yet (fetched from DtAwdPri).

    Raises:
        ToolError: If validation fails, the BCC manifest is not found, or a database error occurs.

    Examples:
        Get basic BBIE info by based BCC manifest ID:
        >>> result = get_bbie_by_based_bcc_manifest_id(
        ...     top_level_asbiep_id=123,
        ...     parent_abie_path="ASCCP-123>ACC-456",
        ...     based_bcc_manifest_id=456
        ... )
        >>> print(f"Top-level ASBIEP ID: {result.owner_top_level_asbiep.top_level_asbiep_id}")
        >>> print(f"Based BCC: {result.based_bcc.den}")
    """
    # Validate auth/DB
    app_user, engine = _validate_auth_and_db()

    if based_bcc_manifest_id <= 0:
        raise ToolError("based_bcc_manifest_id must be a positive integer.")

    if not parent_abie_path or not parent_abie_path.strip():
        raise ToolError("parent_abie_path is required and cannot be empty.")

    bie_service = BusinessInformationEntityService(requester=app_user)
    cc_service = CoreComponentService()

    # Validate top_level_asbiep
    top_level_asbiep = bie_service.get_top_level_asbiep_by_id(top_level_asbiep_id)
    if not top_level_asbiep:
        raise ToolError(f"Top-level ASBIEP with ID {top_level_asbiep_id} not found.")

    owner_top_level_info = _create_top_level_asbiep_info(top_level_asbiep)

    # Get BCC information
    bcc_manifest = cc_service.get_bcc_manifest(based_bcc_manifest_id)
    if not bcc_manifest or not bcc_manifest.bcc:
        raise ToolError(f"BCC manifest with ID {based_bcc_manifest_id} not found.")

    bcc = bcc_manifest.bcc
    based_bcc = BccInfo(
        bcc_manifest_id=bcc_manifest.bcc_manifest_id,
        bcc_id=bcc.bcc_id,
        guid=bcc.guid,
        den=bcc_manifest.den,
        cardinality_min=bcc.cardinality_min,
        cardinality_max=bcc.cardinality_max,
        entity_type='Attribute' if bcc.entity_type == 0 else 'Element',
        is_nillable=bcc.is_nillable,
        is_deprecated=bcc.is_deprecated,
        definition=bcc.definition,
        definition_source=bcc.definition_source,
        from_acc_manifest_id=bcc_manifest.from_acc_manifest_id,
        to_bccp_manifest_id=bcc_manifest.to_bccp_manifest_id
    )

    # Construct paths
    bbie_path = f"{parent_abie_path}>BCC-{based_bcc_manifest_id}"
    hash_path = hashlib.sha256(bbie_path.encode()).hexdigest()

    # Try to find existing BBIE by based_bcc_manifest_id and hash_path
    bbie = bie_service.get_bbie_by_based_bcc_manifest_id(
        based_bcc_manifest_id=based_bcc_manifest_id,
        owner_top_level_asbiep_id=top_level_asbiep_id,
        hash_path=hash_path
    )

    if bbie:
        # Found existing BBIE - return it with full details
        bbiep_info = _get_bbiep_info(bbie.owner_top_level_asbiep_id, bbie.bbie_id, bbie.to_bbiep_id,
                                     bcc_manifest.to_bccp_manifest_id, bbie.path)

        # Create Facet object if any facet values exist
        facet = None
        if bbie.facet_min_length is not None or bbie.facet_max_length is not None or bbie.facet_pattern is not None:
            facet = Facet(
                facet_min_length=bbie.facet_min_length,
                facet_max_length=bbie.facet_max_length,
                facet_pattern=bbie.facet_pattern
            )
        
        # Create ValueConstraint object with validation
        value_constraint = validate_and_create_value_constraint(
            default_value=bbie.default_value,
            fixed_value=bbie.fixed_value
        )
        
        # Create PrimitiveRestriction object with validation
        primitive_restriction = _validate_and_create_primitive_restriction(
            xbt_manifest_id=bbie.xbt_manifest_id,
            code_list_manifest_id=bbie.code_list_manifest_id,
            agency_id_list_manifest_id=bbie.agency_id_list_manifest_id
        )
        
        return GetBbieResponse(
            bbie_id=bbie.bbie_id,
            owner_top_level_asbiep=owner_top_level_info,
            guid=bbie.guid,
            based_bcc=based_bcc,
            to_bbiep=bbiep_info,
            is_used=bbie.is_used,
            path=bbie.path,
            hash_path=bbie.hash_path,
            cardinality_min=bbie.cardinality_min,
            cardinality_max=bbie.cardinality_max,
            is_nillable=bbie.is_nillable,
            remark=bbie.remark,
            primitiveRestriction=primitive_restriction,
            valueConstraint=value_constraint,
            facet=facet
        )
    else:
        # No existing BBIE found - return basic info
        bbiep_info = _get_bbiep_info(top_level_asbiep_id, None, None, bcc_manifest.to_bccp_manifest_id, bbie_path)

        # Create ValueConstraint object with validation
        value_constraint = validate_and_create_value_constraint(
            default_value=bcc.default_value,
            fixed_value=bcc.fixed_value
        )

        # Try to fetch default primitive restriction from DtAwdPri (is_default=1)
        primitive_restriction = None

        # Get default primitive restriction from DtAwdPri using BCCP manifest ID
        try:
            xbt_manifest_id, code_list_manifest_id, agency_id_list_manifest_id = bie_service.get_dt_awd_pri(
                bcc_manifest.to_bccp_manifest_id)
            # Create PrimitiveRestriction object with validation
            primitive_restriction = _validate_and_create_primitive_restriction(
                xbt_manifest_id=xbt_manifest_id,
                code_list_manifest_id=code_list_manifest_id,
                agency_id_list_manifest_id=agency_id_list_manifest_id
            )
        except ValueError as e:
            # Validation error - log and re-raise
            logger.error(f"PrimitiveRestriction validation failed for BCC manifest {bcc_manifest.bcc_manifest_id}: {e}")
            raise
        except Exception:
            # If we can't fetch the default primitive restriction, just leave it as None
            # This is not a critical error - the BBIE doesn't exist yet anyway
            pass

        return GetBbieResponse(
            bbie_id=None,
            owner_top_level_asbiep=owner_top_level_info,
            guid=None,
            based_bcc=based_bcc,
            to_bbiep=bbiep_info,
            is_used=False,
            path=bbie_path,
            hash_path=hash_path,
            cardinality_min=bcc_manifest.bcc.cardinality_min,
            cardinality_max=bcc_manifest.bcc.cardinality_max,
            is_nillable=bcc_manifest.bcc.is_nillable,
            remark=None,
            primitiveRestriction=primitive_restriction,  # Default from DtAwdPri if available
            valueConstraint=value_constraint,  # From BCC if available
            facet=None  # No BBIE-specific facet
        )


@mcp.tool(
    name="create_top_level_asbiep",
    description="Create a new Top-Level ASBIEP (Association Business Information Entity Property) with the specified ASCCP (Association Core Component Property) manifest and business contexts",
    output_schema={
        "type": "object",
        "description": "Response containing the newly created top-level ASBIEP information. The 'asbiep' field contains ASBIEP structure with role_of_abie (excludes remark, is_nillable from relationships).",
        "properties": {
            "top_level_asbiep_id": {"type": "integer",
                                    "description": "ID of the created top-level ASBIEP (Association Business Information Entity Property)", "example": 12345},
            "asbiep": {"type": ["object", "null"], "description": "ASBIEP structure with role_of_abie. Shows the hierarchical structure with asbiep_id and role_of_abie containing abie_id. Excludes remark and is_nillable fields from relationships.", "properties": {
                "asbiep_id": {"type": "integer", "description": "Unique identifier of the ASBIEP", "example": 12346},
                "role_of_abie": {"type": ["object", "null"], "description": "The ABIE that this ASBIEP points to", "properties": {
                    "abie_id": {"type": "integer", "description": "Unique identifier of the ABIE", "example": 12347},
                    "relationships": {"type": "array", "description": "List of relationships (ASBIEs and BBIEs) from this ABIE", "items": {
                        "type": "object",
                        "description": "A relationship that can be either an ASBIE or BBIE",
                        "properties": {
                            "asbie": {"type": ["object", "null"], "description": "ASBIE relationship (if this is an ASBIE). Excludes is_nillable and remark fields.", "properties": {
                                "asbie_id": {"type": "integer"},
                                "guid": {"type": ["string", "null"]},
                                "path": {"type": "string"},
                                "cardinality_min": {"type": "integer"},
                                "cardinality_max": {"type": "integer"},
                                "based_ascc": {"type": "object"},
                                "asbiep": {"type": ["object", "null"], "description": "Recursive structure - ASBIEP with its role_of_abie and relationships"}
                            }},
                            "bbie": {"type": ["object", "null"], "description": "BBIE relationship (if this is a BBIE). Excludes remark field.", "properties": {
                                "bbie_id": {"type": "integer"},
                                "guid": {"type": ["string", "null"]},
                                "path": {"type": "string"},
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
        asccp_manifest_id: Annotated[int, Field(
            description="The ASCCP (Association Core Component Property) manifest ID to base the Top-Level ASBIEP on.",
            examples=[123, 456, 789],
            gt=0,
            title="ASCCP (Association Core Component Property) Manifest ID"
        )],
        biz_ctx_list: Annotated[str, Field(
            description="Business context IDs to assign to the Top-Level ASBIEP. Comma-separated list of business context IDs. Examples: '1', '1,2', '1,2,3'. Must contain at least one positive integer.",
            examples=["1", "1,2", "1,2,3"],
            min_length=1,
            title="Business Context List"
        )]
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Enhanced parameter validation with helpful guidance
    if not asccp_manifest_id or asccp_manifest_id <= 0:
        raise ToolError(
            "ASCCP manifest ID is required and must be a positive integer. "
            "To find the correct ASCCP manifest ID, use the 'get_core_components' tool to search for ASCCPs by name or property term. "
            "For example: get_core_components(release_id=<release_id>, types=['ASCCP'], name='Item Master')"
        )

    # Parse biz_ctx_list parameter
    if not biz_ctx_list or not biz_ctx_list.strip():
        raise ToolError(
            "Business context list is required and must contain at least one business context ID. "
            "To find existing business contexts, use the 'get_business_contexts' tool. "
            "To create a new business context, use the 'create_business_context' tool. "
            "Example: get_business_contexts(offset=0, limit=10) to see available contexts, "
            "or create_business_context(name='Your Business Context Name') to create a new one."
        )

    try:
        # Parse comma-separated string into list of integers
        biz_ctx_list_parsed = [int(ctx_id.strip()) for ctx_id in biz_ctx_list.split(',') if ctx_id.strip()]

        # Validate that all IDs are positive integers
        invalid_ids = [ctx_id for ctx_id in biz_ctx_list_parsed if ctx_id <= 0]
        if invalid_ids:
            raise ToolError(
                f"Invalid business context IDs: {', '.join(map(str, invalid_ids))}. "
                f"Business context IDs must be positive integers. "
                f"Use comma-separated format like '1,2,3'."
            )

        if len(biz_ctx_list_parsed) == 0:
            raise ToolError(
                "Business context list must contain at least one valid business context ID. "
                "Use comma-separated format like '1,2,3'."
            )

    except ValueError as e:
        raise ToolError(
            f"Invalid biz_ctx_list format: {str(e)}. "
            f"Please use comma-separated integers like '1,2,3'. "
            f"Use 'get_business_contexts' to find valid business context IDs."
        ) from e

    # Create service instance
    bie_service = BusinessInformationEntityService(requester=app_user)

    # Create BIE (Business Information Entity)
    try:
        top_level_asbiep_id, asbiep_id, abie_id = bie_service.create_top_level_asbiep(
            asccp_manifest_id=asccp_manifest_id,
            biz_ctx_list=biz_ctx_list_parsed
        )

        # Recursively process mandatory relationships (cardinality_min >= 1)
        if abie_id:
            _process_mandatory_relationships_recursive(
                bie_service=bie_service,
                abie_id=abie_id,
                visited_abie_ids=set()
            )

        # Build asbiep structure with role_of_abie
        asbiep_detail = None
        if asbiep_id and abie_id:
            # Build the recursive structure starting from role_of_abie
            role_of_abie_detail = _build_create_role_of_abie_detail(
                bie_service=bie_service,
                abie_id=abie_id,
                visited_abie_ids=set()
            )
            if role_of_abie_detail:
                asbiep_detail = CreateAsbiepRelationshipDetail(
                    asbiep_id=asbiep_id,
                    role_of_abie=role_of_abie_detail
                )

        return CreateTopLevelAsbiepResponse(
            top_level_asbiep_id=top_level_asbiep_id,
            asbiep=asbiep_detail
        )
    except HTTPException as e:
        logger.error(f"HTTP error creating BIE (Business Information Entity): {e}")
        if e.status_code == 400:
            # Enhanced 400 error handling with specific guidance
            if "Group types are not allowed" in str(e.detail):
                raise ToolError(
                    f"Validation error: {e.detail}. "
                    "You cannot create a BIE from an ASCCP that is based on a SemanticGroup or UserExtensionGroup ACC. "
                    "Please choose a different ASCCP that is based on a regular ACC. "
                    "Use 'get_core_components' to find suitable ASCCPs."
                ) from e
            else:
                raise ToolError(
                    f"Validation error: {e.detail}. "
                    "Please check your input parameters and try again. "
                    "Ensure the ASCCP manifest ID exists and business context IDs are valid."
                ) from e
        elif e.status_code == 404:
            # Enhanced 404 error handling with specific guidance
            if "ASCCP manifest" in str(e.detail) or "ASCCP with ID" in str(e.detail):
                raise ToolError(
                    f"ASCCP not found: {e.detail}. "
                    "The specified ASCCP manifest ID does not exist. "
                    "Use 'get_core_components' to search for available ASCCPs: "
                    "get_core_components(release_id=<release_id>, types=['ASCCP'], name='<search_term>')"
                ) from e
            elif "Business contexts with IDs" in str(e.detail):
                missing_ids = str(e.detail).split("Business contexts with IDs ")[1].split(" not found")[0]
                raise ToolError(
                    f"Business contexts not found: {e.detail}. "
                    f"The following business context IDs are invalid: {missing_ids}. "
                    "Use 'get_business_contexts' to find valid business context IDs, "
                    "or 'create_business_context' to create new ones."
                ) from e
            else:
                raise ToolError(
                    f"Resource not found: {e.detail}. "
                    "Please verify the ASCCP manifest ID and business context IDs exist. "
                    "Use 'get_core_components' to find ASCCPs and 'get_business_contexts' to find business contexts."
                ) from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error creating BIE (Business Information Entity): {e}")
        raise ToolError(
            f"An unexpected error occurred while creating the BIE (Business Information Entity). Please try again later or contact your system administrator if the problem persists."
        ) from e


@mcp.tool(
    name="update_top_level_asbiep",
    description="Update a Top-Level ASBIEP (Association Business Information Entity Property) with new version, status, or deprecation information. IMPORTANT: The 'version' parameter MUST be provided as a string and the exact string value must be preserved (e.g., '1.0' must NOT be converted to '1' or integer 1). The version format is user-defined and the exact string value must be stored as-is. Boolean parameters accept both their native types and string representations (strings are automatically converted).",
    output_schema={
        "type": "object",
        "description": "Response containing the updated Top-Level ASBIEP information. Boolean parameters accept both their native types and string representations (strings are automatically converted to the appropriate type).",
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
        top_level_asbiep_id: Annotated[int, Field(
            description="The top-level ASBIEP (Association Business Information Entity Property) ID of the Top-Level ASBIEP to update.",
            examples=[123, 456, 789],
            gt=0,
            title="Top-Level ASBIEP (Association Business Information Entity Property) ID"
        )],
        version: Annotated[str | None, Field(
            description="New version number for the Top-Level ASBIEP. MUST be provided as a string and the exact string value must be preserved (e.g., '1.0' must NOT be converted to '1' or integer 1). The version format is user-defined and the exact string value must be stored as-is. If not provided, version will not be updated.",
            examples=["1.0", "1.1", "2.0", "2.1", "3.0", "v1.0", "2024.01"],
            title="Version"
        )] = None,
        status: Annotated[str | None, Field(
            description="New status for the Top-Level ASBIEP. If not provided, status will not be updated.",
            examples=["Prototype", "Test", "Production"],
            title="Status"
        )] = None,
        display_name: Annotated[str | None, Field(
            description="Display name of the ASBIEP. If not provided, display_name will not be updated.",
            examples=["Item Master", "Product Master", "Inventory Master"],
            title="Display Name"
        )] = None,
        biz_term: Annotated[str | None, Field(
            description="Business term to indicate what the BIE is called in a particular business context. If not provided, biz_term will not be updated.",
            examples=["Product Master", "Item Catalog", "Inventory Item"],
            title="Business Term"
        )] = None,
        remark: Annotated[str | None, Field(
            description="Context-specific usage remarks about the BIE. If not provided, remark will not be updated.",
            examples=["Used for manufacturing operations", "Primary item identification system",
                      "Core product data management"],
            title="Remark"
        )] = None,
        is_deprecated: Annotated[bool | str | None, Field(
            description="New deprecation status for the Top-Level ASBIEP. Accepts bool, str, or None. String values are converted: 'True'/'true'/'1' -> True, 'False'/'false'/'0' -> False. If not provided, deprecation status will not be updated.",
            examples=[True, False, "1", "0", "true", "false", "True", "False"],
            title="Is Deprecated"
        )] = None,
        deprecated_reason: Annotated[str | None, Field(
            description="Reason for deprecation. Required if is_deprecated is set to True. Ignored if is_deprecated is not provided or False.",
            examples=["Replaced by new version", "No longer used"],
            title="Deprecated Reason"
        )] = None,
        deprecated_remark: Annotated[str | None, Field(
            description="Additional remarks about the deprecation. Optional even when deprecating.",
            examples=["Use version 2.0 instead", "Contact admin for migration"],
            title="Deprecated Remark"
        )] = None
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()
    
    # Convert string parameters to their proper types
    try:
        is_deprecated = str_to_bool(is_deprecated)
    except ToolError:
        raise  # Re-raise ToolError as-is
    except Exception as e:
        raise ToolError(
            f"Type conversion error: {str(e)}. Please check your parameter types and try again."
        ) from e

    # Create service instance
    bie_service = BusinessInformationEntityService(requester=app_user)

    # Validate that at least one field is provided for update
    if all(field is None for field in [version, status, is_deprecated, biz_term, remark, display_name]):
        raise ToolError(
            "At least one field (version, status, is_deprecated, biz_term, remark, or display_name) must be provided for update.")

    # Validate deprecation reason if deprecating
    if is_deprecated is True and not deprecated_reason:
        raise ToolError("deprecated_reason is required when is_deprecated is set to True.")

    # Update BIE (Business Information Entity)
    try:
        updates = bie_service.update_top_level_asbiep(
            top_level_asbiep_id=top_level_asbiep_id,
            version=version,
            status=status,
            display_name=display_name,
            biz_term=biz_term,
            remark=remark,
            is_deprecated=is_deprecated,
            deprecated_reason=deprecated_reason,
            deprecated_remark=deprecated_remark
        )
        
        # Sync version to Version Identifier BBIE if version was updated
        # Only sync if version was actually updated (in the updates list)
        if version is not None and 'version' in updates:
            _sync_version_to_version_identifier_bbie(
                bie_service=bie_service,
                top_level_asbiep_id=top_level_asbiep_id,
                version=version
            )

        return UpdateTopLevelAsbiepResponse(
            top_level_asbiep_id=top_level_asbiep_id,
            updates=updates
        )
    except HTTPException as e:
        logger.error(f"HTTP error updating BIE (Business Information Entity): {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 403:
            raise ToolError(f"Access denied: {e.detail}") from e
        elif e.status_code == 404:
            raise ToolError(
                f"Resource not found: {e.detail}. Please verify the top-level ASBIEP (Association Business Information Entity Property) ID.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error updating BIE (Business Information Entity): {e}")
        raise ToolError(
            f"An unexpected error occurred while updating the BIE (Business Information Entity). Please try again later or contact your system administrator if the problem persists."
        ) from e


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
        top_level_asbiep_id: Annotated[int, Field(
            description="The top-level ASBIEP (Association Business Information Entity Property) ID of the Top-Level ASBIEP to update state.",
            examples=[123, 456, 789],
            gt=0,
            title="Top-Level ASBIEP (Association Business Information Entity Property) ID"
        )],
        new_state: Annotated[str, Field(
            description="The new state for the BIE (Business Information Entity). Valid transitions: WIP->QA, QA->WIP, QA->Production",
            examples=["QA", "WIP", "Production"],
            title="New State"
        )]
) -> UpdateTopLevelAsbiepResponse:
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Create service instance
    bie_service = BusinessInformationEntityService(requester=app_user)

    # Validate new_state
    valid_states = ['WIP', 'QA', 'Production']
    if new_state not in valid_states:
        raise ToolError(f"Invalid state '{new_state}'. Valid states are: {', '.join(valid_states)}")

    # Update BIE state
    try:
        previous_state, new_state_result = bie_service.update_top_level_asbiep_state(
            top_level_asbiep_id=top_level_asbiep_id,
            new_state=new_state
        )

        return UpdateTopLevelAsbiepResponse(
            top_level_asbiep_id=top_level_asbiep_id,
            updates=["state"]
        )
    except HTTPException as e:
        logger.error(f"HTTP error updating BIE (Business Information Entity) state: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 403:
            raise ToolError(f"Access denied: {e.detail}") from e
        elif e.status_code == 404:
            raise ToolError(
                f"Resource not found: {e.detail}. Please verify the top-level ASBIEP (Association Business Information Entity Property) ID.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error updating BIE (Business Information Entity) state: {e}")
        raise ToolError(
            f"An unexpected error occurred while updating the BIE (Business Information Entity) state. Please try again later or contact your system administrator if the problem persists."
        ) from e


@mcp.tool(
    name="delete_top_level_asbiep",
    description="Delete a Top-Level ASBIEP (Association Business Information Entity Property) and all related records",
    output_schema={
        "type": "object",
        "properties": {
            "top_level_asbiep_id": {"type": "integer",
                                    "description": "ID of the deleted top-level ASBIEP (Association Business Information Entity Property)"}
        },
        "required": ["top_level_asbiep_id"]
    }
)
async def delete_top_level_asbiep(
        top_level_asbiep_id: Annotated[int, Field(
            description="The top-level ASBIEP (Association Business Information Entity Property) ID of the Top-Level ASBIEP to delete.",
            examples=[123, 456, 789],
            gt=0,
            title="Top-Level ASBIEP (Association Business Information Entity Property) ID"
        )]
) -> DeleteTopLevelAsbiepResponse:
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
    - If the current user is an admin, it can be deleted
    - If the current user is the owner and the state is not "Production", then it can be deleted
    - Otherwise, it cannot be deleted
    
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
            - Access denied - Production state requires admin (403 error)
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Create service instance
    bie_service = BusinessInformationEntityService(requester=app_user)

    # Delete BIE (Business Information Entity)
    try:
        bie_service.delete_top_level_asbiep(top_level_asbiep_id)

        return DeleteTopLevelAsbiepResponse(
            top_level_asbiep_id=top_level_asbiep_id
        )
    except HTTPException as e:
        logger.error(f"HTTP error deleting BIE (Business Information Entity): {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 403:
            raise ToolError(f"Access denied: {e.detail}") from e
        elif e.status_code == 404:
            raise ToolError(
                f"Resource not found: {e.detail}. Please verify the top-level ASBIEP (Association Business Information Entity Property) ID.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error deleting BIE (Business Information Entity): {e}")
        raise ToolError(
            f"An unexpected error occurred while deleting the BIE (Business Information Entity). Please try again later or contact your system administrator if the problem persists."
        ) from e


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
        top_level_asbiep_id: Annotated[int, Field(
            description="The top-level ASBIEP (Association Business Information Entity Property) ID of the Top-Level ASBIEP to transfer ownership.",
            examples=[123, 456, 789],
            gt=0,
            title="Top-Level ASBIEP (Association Business Information Entity Property) ID"
        )],
        new_owner_user_id: Annotated[int, Field(
            description="The user ID of the new owner who will receive ownership of the Top-Level ASBIEP.",
            examples=[2, 3, 4],
            gt=0,
            title="New Owner User ID"
        )]
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Create service instance
    bie_service = BusinessInformationEntityService(requester=app_user)

    # Transfer ownership
    try:
        previous_owner, new_owner = bie_service.transfer_ownership(
            top_level_asbiep_id=top_level_asbiep_id,
            new_owner_user_id=new_owner_user_id
        )

        return TransferTopLevelAsbiepOwnershipResponse(
            top_level_asbiep_id=top_level_asbiep_id,
            updates=["owner_user_id"]
        )
    except HTTPException as e:
        logger.error(f"HTTP error transferring ownership: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 403:
            raise ToolError(f"Access denied: {e.detail}") from e
        elif e.status_code == 404:
            raise ToolError(
                f"Resource not found: {e.detail}. Please verify the top-level ASBIEP (Association Business Information Entity Property) ID and new owner user ID.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error transferring ownership: {e}")
        raise ToolError(
            f"An unexpected error occurred while transferring ownership. Please try again later or contact your system administrator if the problem persists."
        ) from e


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
        top_level_asbiep_id: Annotated[int, Field(
            description="The top-level ASBIEP (Association Business Information Entity Property) ID of the BIE (Business Information Entity) to assign the business context to.",
            examples=[123, 456, 789],
            gt=0,
            title="Top-Level ASBIEP (Association Business Information Entity Property) ID"
        )],
        biz_ctx_id: Annotated[int, Field(
            description="The business context ID to assign to the BIE (Business Information Entity).",
            examples=[1, 2, 3],
            gt=0,
            title="Business Context ID"
        )]
) -> UpdateTopLevelAsbiepResponse:
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Create service instance
    bie_service = BusinessInformationEntityService(requester=app_user)

    # Assign business context
    try:
        bie_service.assign_business_context(
            top_level_asbiep_id=top_level_asbiep_id,
            biz_ctx_id=biz_ctx_id
        )

        return UpdateTopLevelAsbiepResponse(
            top_level_asbiep_id=top_level_asbiep_id,
            updates=["business_contexts"]
        )
    except HTTPException as e:
        logger.error(f"HTTP error assigning business context: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 403:
            raise ToolError(f"Access denied: {e.detail}") from e
        elif e.status_code == 404:
            raise ToolError(
                f"Resource not found: {e.detail}. Please verify the top-level ASBIEP (Association Business Information Entity Property) ID and business context ID.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error assigning business context: {e}")
        raise ToolError(
            f"An unexpected error occurred while assigning the business context. Please try again later or contact your system administrator if the problem persists."
        ) from e


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
        top_level_asbiep_id: Annotated[int, Field(
            description="The top-level ASBIEP (Association Business Information Entity Property) ID of the BIE (Business Information Entity) to unassign the business context from.",
            examples=[123, 456, 789],
            gt=0,
            title="Top-Level ASBIEP (Association Business Information Entity Property) ID"
        )],
        biz_ctx_id: Annotated[int, Field(
            description="The business context ID to unassign from the BIE (Business Information Entity).",
            examples=[1, 2, 3],
            gt=0,
            title="Business Context ID"
        )]
) -> UpdateTopLevelAsbiepResponse:
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Create service instance
    bie_service = BusinessInformationEntityService(requester=app_user)

    # Unassign business context
    try:
        bie_service.unassign_business_context(
            top_level_asbiep_id=top_level_asbiep_id,
            biz_ctx_id=biz_ctx_id
        )

        return UpdateTopLevelAsbiepResponse(
            top_level_asbiep_id=top_level_asbiep_id,
            updates=["business_contexts"]
        )
    except HTTPException as e:
        logger.error(f"HTTP error unassigning business context: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 403:
            raise ToolError(f"Access denied: {e.detail}") from e
        elif e.status_code == 404:
            raise ToolError(
                f"Resource not found: {e.detail}. Please verify the top-level ASBIEP (Association Business Information Entity Property) ID and business context ID.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error unassigning business context: {e}")
        raise ToolError(
            f"An unexpected error occurred while unassigning the business context. Please try again later or contact your system administrator if the problem persists."
        ) from e


@mcp.tool(
    name="create_asbie",
    description="Enable (use) an ASBIE (Association Business Information Entity) during BIE profiling. Creates a new ASBIE with associated ASBIEP and ABIE records, enabling the component in the BIE. The ASBIE is automatically enabled (is_used=True) and profiled. After creation, this tool automatically processes mandatory relationships (cardinality_min >= 1) recursively, creating or enabling required ASBIE and BBIE components to satisfy cardinality constraints.",
    output_schema={
        "type": "object",
        "description": "Response containing the newly created and enabled ASBIE information. The ASBIE is automatically enabled (is_used=True) for BIE profiling. All mandatory relationships (cardinality_min >= 1) are automatically created or enabled recursively. The 'asbiep' field contains a simplified recursive structure showing all created/enabled ASBIEs and BBIEs (excludes is_nillable, remark).",
        "properties": {
            "asbie_id": {"type": "integer", "description": "Unique identifier of the newly created and enabled ASBIE", "example": 12345},
            "asbiep": {"type": ["object", "null"], "description": "Simplified recursive structure containing ASBIEP and all created/enabled relationships. Shows the hierarchical structure of all ASBIEs and BBIEs that were automatically created or enabled during mandatory relationship processing. Excludes is_nillable and remark fields.", "properties": {
                "asbiep_id": {"type": "integer", "description": "Unique identifier of the ASBIEP", "example": 12346},
                "role_of_abie": {"type": ["object", "null"], "description": "The ABIE that this ASBIEP points to, with its relationships", "properties": {
                    "abie_id": {"type": "integer", "description": "Unique identifier of the ABIE", "example": 12347},
                    "relationships": {"type": "array", "description": "List of relationships (ASBIEs and BBIEs) from this ABIE", "items": {
                        "type": "object",
                        "description": "A relationship that can be either an ASBIE or BBIE",
                        "properties": {
                            "asbie": {"type": ["object", "null"], "description": "ASBIE relationship (if this is an ASBIE). Excludes is_nillable and remark fields.", "properties": {
                                "asbie_id": {"type": "integer"},
                                "guid": {"type": ["string", "null"]},
                                "path": {"type": "string"},
                                "cardinality_min": {"type": "integer"},
                                "cardinality_max": {"type": "integer"},
                                "based_ascc": {"type": "object"},
                                "asbiep": {"type": ["object", "null"], "description": "Recursive structure - ASBIEP with its role_of_abie and relationships"}
                            }},
                            "bbie": {"type": ["object", "null"], "description": "BBIE relationship (if this is a BBIE). Excludes remark field.", "properties": {
                                "bbie_id": {"type": "integer"},
                                "guid": {"type": ["string", "null"]},
                                "path": {"type": "string"},
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
        "required": ["asbie_id"]
    }
)
async def create_asbie(
        from_abie_id: Annotated[int, Field(
            description="The ABIE ID that this ASBIE originates from (parent ABIE).",
            examples=[123, 456, 789],
            gt=0,
            title="From ABIE ID"
        )],
        based_ascc_manifest_id: Annotated[int, Field(
            description="The ASCC manifest ID that this ASBIE is based on.",
            examples=[12345, 67890],
            gt=0,
            title="Based ASCC Manifest ID"
        )]
) -> CreateAsbieResponse:
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
    
    Permission Requirements:
    - The current user must be the owner of the top-level ASBIEP
    - The top-level ASBIEP state must be 'WIP' (Work In Progress)
    
    Args:
        from_abie_id (int): The ABIE ID that this ASBIE originates from (parent ABIE)
        based_ascc_manifest_id (int): The ASCC manifest ID that this ASBIE is based on
    
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
            or database errors occur.
    
    Examples:
        Enable an ASBIE during BIE profiling (mandatory relationships are automatically created/enabled):
        >>> result = create_asbie(from_abie_id=123, based_ascc_manifest_id=456)
        >>> print(f"Created ASBIE ID: {result.asbie_id}")
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()
    
    # Create service instance
    bie_service = BusinessInformationEntityService(requester=app_user)
    
    try:
        # Calculate path from _get_abie_related_components
        # Get the ABIE to find its path
        from_abie = bie_service.get_abie(from_abie_id)
        if not from_abie:
            raise ToolError(f"ABIE with ID {from_abie_id} not found.")
        
        # Get relationships and find the matching ASBIE relationship
        relationships = _get_abie_related_components(
            from_abie.owner_top_level_asbiep_id, from_abie_id,
            from_abie.based_acc_manifest_id, from_abie.path
        )
        
        # Find the matching ASBIE relationship by ascc_manifest_id
        asbie_path = None
        for rel in relationships:
            if isinstance(rel, AsbieRelationshipInfo) and rel.based_ascc.ascc_manifest_id == based_ascc_manifest_id:
                asbie_path = rel.path
                break
        
        if not asbie_path:
            raise ToolError(
                f"Could not find ASBIE relationship for ASCC manifest ID {based_ascc_manifest_id} in the ABIE relationships. "
                f"This may indicate that the ASCC is not a valid relationship for this ABIE."
            )
        
        # Call the service method to create the ASBIE
        # Use default values for all optional parameters
        asbie_id_result, updates = bie_service.create_asbie(
            from_abie_id=from_abie_id,
            based_ascc_manifest_id=based_ascc_manifest_id,
            asbie_path=asbie_path
        )
        
        # Get the created ASBIE to access its relationships
        created_asbie = bie_service.get_asbie_by_asbie_id(asbie_id_result)
        if not created_asbie:
            raise ToolError(f"Created ASBIE with ID {asbie_id_result} not found.")
        
        # Get the role_of_abie_id for recursive processing
        role_of_abie_id = None
        asbiep_id = None
        if created_asbie.to_asbiep_id:
            asbiep_id = created_asbie.to_asbiep_id
            # Get the ASBIEP to access role_of_abie_id
            asbiep = bie_service.get_asbiep(asbiep_id)
            if asbiep and asbiep.role_of_abie_id:
                role_of_abie_id = asbiep.role_of_abie_id
        
        # Recursively process mandatory relationships
        if role_of_abie_id:
            _process_mandatory_relationships_recursive(
                bie_service=bie_service,
                abie_id=role_of_abie_id,
                visited_abie_ids=set()
            )
        
        # Build recursive structure for response
        asbiep_detail = None
        if asbiep_id and role_of_abie_id:
            # Build the recursive structure starting from role_of_abie
            role_of_abie_detail = _build_create_role_of_abie_detail(
                bie_service=bie_service,
                abie_id=role_of_abie_id,
                visited_abie_ids=set()
            )
            if role_of_abie_detail:
                asbiep_detail = CreateAsbiepRelationshipDetail(
                    asbiep_id=asbiep_id,
                    role_of_abie=role_of_abie_detail
                )
        
        return CreateAsbieResponse(
            asbie_id=asbie_id_result,
            asbiep=asbiep_detail
        )
    
    except HTTPException as e:
        logger.error(f"HTTP error creating ASBIE: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 403:
            raise ToolError(f"Access denied: {e.detail}") from e
        elif e.status_code == 404:
            raise ToolError(f"Resource not found: {e.detail}") from e
        elif e.status_code == 500:
            raise ToolError(f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error creating ASBIE: {e}")
        raise ToolError(
            f"An unexpected error occurred while creating the ASBIE: {str(e)}. Please contact your system administrator."
        ) from e


@mcp.tool(
    name="update_asbie",
    description="Update an existing ASBIE (Association Business Information Entity) during BIE profiling. Can be used to toggle enable/disable (use/unuse) the ASBIE, or modify its properties. Set is_used=True/1/'true'/'True' to enable/use or is_used=False/0/'false'/'False' to disable/unuse during profiling.",
    output_schema={
        "type": "object",
        "description": "Response containing the updated ASBIE information with nested structure showing all updated relationships. The updates list includes all fields that were updated on the ASBIE itself. The asbiep field contains nested structure with role_of_abie and relationships, each showing their updates. When is_used is set to True, mandatory relationships are automatically processed. When is_used is set to False, all underlying relationships are automatically disabled.",
        "properties": {
            "asbie_id": {"type": "integer", "description": "Unique identifier of the updated ASBIE", "example": 12345},
            "updates": {"type": "array", "items": {"type": "string"},
                        "description": "A list of field names that were updated on the ASBIE itself (includes 'is_used' when toggling enable/disable during profiling)", "example": ["is_used", "definition", "cardinality_min", "remark"]},
            "asbiep": {"type": ["object", "null"], "description": "Nested structure showing ASBIEP and all updated relationships with their updates", "properties": {
                "asbiep_id": {"type": "integer", "description": "Unique identifier of the ASBIEP"},
                "updates": {"type": "array", "items": {"type": "string"}, "description": "List of fields updated on the ASBIEP (typically empty)"},
                "role_of_abie": {"type": ["object", "null"], "description": "The ABIE that this ASBIEP points to, with its relationships", "properties": {
                    "abie_id": {"type": "integer", "description": "Unique identifier of the ABIE"},
                    "updates": {"type": "array", "items": {"type": "string"}, "description": "List of fields updated on the ABIE (typically empty)"},
                    "relationships": {"type": "array", "description": "List of relationships (ASBIEs and BBIEs) with their updates", "items": {
                        "type": "object",
                        "description": "A relationship that can be either an ASBIE or BBIE",
                        "properties": {
                            "asbie": {"type": ["object", "null"], "description": "ASBIE relationship with updates", "properties": {
                                "asbie_id": {"type": "integer"},
                                "updates": {"type": "array", "items": {"type": "string"}, "description": "List of fields updated on this ASBIE (e.g., ['is_used'])"},
                                "guid": {"type": ["string", "null"]},
                                "path": {"type": "string"},
                                "cardinality_min": {"type": "integer"},
                                "cardinality_max": {"type": "integer"},
                                "based_ascc": {"type": "object"},
                                "asbiep": {"type": ["object", "null"], "description": "Recursive structure - ASBIEP with its role_of_abie and relationships"}
                            }},
                            "bbie": {"type": ["object", "null"], "description": "BBIE relationship with updates", "properties": {
                                "bbie_id": {"type": "integer"},
                                "updates": {"type": "array", "items": {"type": "string"}, "description": "List of fields updated on this BBIE (e.g., ['is_used'])"},
                                "guid": {"type": ["string", "null"]},
                                "path": {"type": "string"},
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
        "required": ["asbie_id", "updates"]
    }
)
async def update_asbie(
        asbie_id: Annotated[int, Field(
            description="The ASBIE ID to update.",
            examples=[12345, 67890],
            gt=0,
            title="ASBIE ID"
        )],
        is_used: Annotated[bool | str | None, Field(
            description="Toggle enable/disable (use/unuse) this ASBIE during BIE profiling. Accepts bool, str, or None. String values are converted: 'True'/'true'/'1' -> True, 'False'/'false'/'0' -> False. Set to True/1/'true'/'True' to enable/use the ASBIE (profiled and active), or False/0/'false'/'False' to disable/unuse it (not used in the BIE). This is used to toggle the component's usage during profiling. If not provided, is_used will not be updated.",
            examples=[True, False, "1", "0", "true", "false", "True", "False"],
            title="Is Used (Enable/Disable Toggle)"
        )] = None,
        is_deprecated: Annotated[bool | str | None, Field(
            description="Whether the ASBIE is deprecated. Accepts bool, str, or None. String values are converted: 'True'/'true'/'1' -> True, 'False'/'false'/'0' -> False. If not provided, is_deprecated will not be updated.",
            examples=[True, False, "1", "0", "true", "false", "True", "False"],
            title="Is Deprecated"
        )] = None,
        is_nillable: Annotated[bool | str | None, Field(
            description="Whether the ASBIE can have a nil/null value. Accepts bool, str, or None. String values are converted: 'True'/'true'/'1' -> True, 'False'/'false'/'0' -> False. If not provided, is_nillable will not be updated.",
            examples=[True, False, "1", "0", "true", "false", "True", "False"],
            title="Is Nillable"
        )] = None,
        definition: Annotated[str | None, Field(
            description="Definition to override the ASCC definition. If not provided, definition will not be updated.",
            examples=["Custom definition for this ASBIE"],
            title="Definition"
        )] = None,
        cardinality_min: Annotated[int | str | None, Field(
            description="Minimum cardinality (minimum number of occurrences required, typically 0 or 1). Accepts int, str, or None. String values are automatically converted to integers. If not provided, cardinality_min will not be updated.",
            examples=[0, 1, "0", "1"],
            title="Cardinality Min"
        )] = None,
        cardinality_max: Annotated[int | str | None, Field(
            description="Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded). Accepts int, str, or None. String values are automatically converted to integers. If not provided, cardinality_max will not be updated.",
            examples=[1, -1, "1", "-1"],
            title="Cardinality Max"
        )] = None,
        remark: Annotated[str | None, Field(
            description="Additional remarks or notes about the ASBIEP. If not provided, remark will not be updated.",
            examples=["Used for purchase orders"],
            title="Remark"
        )] = None
) -> UpdateAsbieResponse:
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
    - Boolean parameters (is_used, is_deprecated, is_nillable):
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
        is_deprecated (bool | str | None, optional): Whether the ASBIE is deprecated. 
            Accepts bool, str ('True'/'true'/'1' for True, 'False'/'false'/'0' for False), or None.
            If not provided, will not be updated.
        is_nillable (bool | str | None, optional): Whether the ASBIE can have a nil/null value.
            Accepts bool, str ('True'/'true'/'1' for True, 'False'/'false'/'0' for False), or None.
            If not provided, will not be updated.
        definition (str | None, optional): Definition to override the ASCC definition
        cardinality_min (int | str | None, optional): Minimum cardinality. 
            Accepts int, str (converted to int), or None. If not provided, will not be updated.
        cardinality_max (int | str | None, optional): Maximum cardinality (-1 means unbounded). 
            Accepts int, str (converted to int), or None. If not provided, will not be updated.
        remark (str | None, optional): Additional remarks for the ASBIEP
    
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()
    
    # Convert string parameters to their proper types
    try:
        is_used = str_to_bool(is_used)
        is_deprecated = str_to_bool(is_deprecated)
        is_nillable = str_to_bool(is_nillable)
        cardinality_min = str_to_int(cardinality_min)
        cardinality_max = str_to_int(cardinality_max)
    except ToolError:
        raise  # Re-raise ToolError as-is
    except Exception as e:
        raise ToolError(
            f"Type conversion error: {str(e)}. Please check your parameter types and try again."
        ) from e
    
    # Create service instance
    bie_service = BusinessInformationEntityService(requester=app_user)
    
    # Validate that at least one field is provided for update
    if all(field is None for field in [is_used, is_deprecated, is_nillable, definition, cardinality_min, cardinality_max, remark]):
        raise ToolError(
            "At least one field (is_used, is_deprecated, is_nillable, definition, cardinality_min, cardinality_max, or remark) must be provided for update."
        )
    
    try:
        # Get the existing ASBIE to check current is_used status
        existing_asbie = bie_service.get_asbie_by_asbie_id(asbie_id)
        if not existing_asbie:
            raise ToolError(f"ASBIE with ID {asbie_id} not found.")
        
        previous_is_used = existing_asbie.is_used
        
        # Validate: Prevent disabling if cardinality_min >= 1 (required relationship)
        if is_used is not None and is_used == False and previous_is_used == True:
            # Check if this ASBIE is required (cardinality_min >= 1)
            # First check the ASBIE's own cardinality_min
            if existing_asbie.cardinality_min >= 1:
                raise ToolError(
                    f"Cannot disable ASBIE {asbie_id} because it is required (cardinality_min={existing_asbie.cardinality_min} >= 1). "
                    f"Required relationships cannot be disabled. If you need to remove this relationship, "
                    f"you must first change its cardinality_min to 0."
                )
            
            # Also check in the parent ABIE's relationships (as a double-check)
            if existing_asbie.from_abie_id:
                from_abie = bie_service.get_abie(existing_asbie.from_abie_id)
                if from_abie:
                    relationships = _get_abie_related_components(
                        from_abie.owner_top_level_asbiep_id,
                        from_abie.abie_id,
                        from_abie.based_acc_manifest_id,
                        from_abie.path
                    )
                    for rel in relationships:
                        if isinstance(rel, AsbieRelationshipInfo) and rel.asbie_id == asbie_id:
                            if rel.cardinality_min >= 1:
                                raise ToolError(
                                    f"Cannot disable ASBIE {asbie_id} because it is required (cardinality_min={rel.cardinality_min} >= 1). "
                                    f"Required relationships cannot be disabled. If you need to remove this relationship, "
                                    f"you must first change its cardinality_min to 0."
                                )
                            break
        
        # Call the service method to update the ASBIE
        # The service layer will retrieve the existing ASBIE and handle all validation
        asbie_id_result, updates = bie_service.update_asbie(
            asbie_id=asbie_id,
            is_used=is_used,
            is_deprecated=is_deprecated,
            is_nillable=is_nillable,
            definition=definition,
            cardinality_min=cardinality_min,
            cardinality_max=cardinality_max,
            remark=remark
        )
        
        # Handle is_used state changes and build nested response
        asbiep_detail = None
        if is_used is not None and is_used != previous_is_used:
            if is_used:
                # is_used changed from False to True: process mandatory relationships
                if existing_asbie.to_asbiep_id:
                    asbiep = bie_service.get_asbiep(existing_asbie.to_asbiep_id)
                    if asbiep and asbiep.role_of_abie_id:
                        # Track updated components
                        updated_components = {}
                        _process_mandatory_relationships_recursive(
                            bie_service=bie_service,
                            abie_id=asbiep.role_of_abie_id,
                            visited_abie_ids=set(),
                            updated_components=updated_components
                        )
                        # Build nested response structure
                        role_of_abie_detail = _build_update_role_of_abie_detail(
                            bie_service=bie_service,
                            abie_id=asbiep.role_of_abie_id,
                            visited_abie_ids=set(),
                            updated_components=updated_components
                        )
                        if role_of_abie_detail:
                            asbiep_detail = UpdateAsbiepRelationshipDetail(
                                asbiep_id=existing_asbie.to_asbiep_id,
                                updates=[],  # ASBIEP itself is rarely updated
                                role_of_abie=role_of_abie_detail
                            )
            else:
                # is_used changed from True to False: disable all relationships
                # Track updated components
                updated_components = {}
                _disable_all_relationships_for_asbie(
                    bie_service=bie_service,
                    asbie_id=asbie_id,
                    visited_asbie_ids=set(),
                    updated_components=updated_components
                )
                # Build nested response structure showing disabled relationships
                if existing_asbie.to_asbiep_id:
                    asbiep = bie_service.get_asbiep(existing_asbie.to_asbiep_id)
                    if asbiep and asbiep.role_of_abie_id:
                        role_of_abie_detail = _build_update_role_of_abie_detail(
                            bie_service=bie_service,
                            abie_id=asbiep.role_of_abie_id,
                            visited_abie_ids=set(),
                            updated_components=updated_components
                        )
                        if role_of_abie_detail:
                            asbiep_detail = UpdateAsbiepRelationshipDetail(
                                asbiep_id=existing_asbie.to_asbiep_id,
                                updates=[],
                                role_of_abie=role_of_abie_detail
                            )
        elif existing_asbie.to_asbiep_id:
            # Even if is_used didn't change, build the structure if relationships exist
            # This allows seeing the current state of relationships
            asbiep = bie_service.get_asbiep(existing_asbie.to_asbiep_id)
            if asbiep and asbiep.role_of_abie_id:
                role_of_abie_detail = _build_update_role_of_abie_detail(
                    bie_service=bie_service,
                    abie_id=asbiep.role_of_abie_id,
                    visited_abie_ids=set(),
                    updated_components=None  # No updates tracked if is_used didn't change
                )
                if role_of_abie_detail:
                    asbiep_detail = UpdateAsbiepRelationshipDetail(
                        asbiep_id=existing_asbie.to_asbiep_id,
                        updates=[],
                        role_of_abie=role_of_abie_detail
                    )
        
        return UpdateAsbieResponse(
            asbie_id=asbie_id_result,
            updates=updates,
            asbiep=asbiep_detail
        )
    
    except HTTPException as e:
        logger.error(f"HTTP error updating ASBIE: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 403:
            raise ToolError(f"Access denied: {e.detail}") from e
        elif e.status_code == 404:
            raise ToolError(f"Resource not found: {e.detail}") from e
        elif e.status_code == 500:
            raise ToolError(f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error updating ASBIE: {e}")
        raise ToolError(
            f"An unexpected error occurred while updating the ASBIE: {str(e)}. Please contact your system administrator."
        ) from e


@mcp.tool(
    name="create_bbie",
    description="Enable (use) a BBIE (Basic Business Information Entity) during BIE profiling. Creates a new BBIE with associated BBIEP record, enabling the component in the BIE. The BBIE is automatically enabled (is_used=True) and profiled. After creation, this tool automatically processes mandatory BBIE SCs (supplementary components with cardinality_min >= 1), creating or enabling required BBIE_SC components to satisfy cardinality constraints.",
    output_schema={
        "type": "object",
        "description": "Response containing the newly created and enabled BBIE information. The BBIE is automatically enabled (is_used=True) for BIE profiling. All mandatory BBIE SCs (supplementary components with cardinality_min >= 1) are automatically created or enabled. The 'bbiep' field contains simplified BBIEP information including all created/enabled supplementary components.",
        "properties": {
            "bbie_id": {"type": "integer", "description": "Unique identifier of the newly created and enabled BBIE", "example": 12345},
            "bbiep": {"type": ["object", "null"], "description": "Simplified BBIEP information including all created/enabled supplementary components. Excludes definition, remark, biz_term, display_name, default_value, fixed_value, and facet fields that won't have values for newly generated records. Shows the BBIEP structure with supplementary_components array containing all BBIE SCs that were automatically created or enabled.", "properties": {
                "bbiep_id": {"type": ["integer", "null"], "description": "Unique identifier of the BBIEP", "example": 12346},
                "guid": {"type": ["string", "null"], "description": "Globally unique identifier of the BBIEP", "example": "a1b2c3d4e5f6789012345678901234ab"},
                "based_bccp": {"type": "object", "description": "Information about the BCCP that this BBIEP is based on"},
                "path": {"type": "string", "description": "Hierarchical path string", "example": "/PurchaseOrder/Amount>BCCP-123"},
                "hash_path": {"type": "string", "description": "Hashed version of the path"},
                "supplementary_components": {"type": "array", "description": "List of supplementary components. Each component contains 'bbie_sc_id' (if created), 'based_dt_sc.dt_sc_manifest_id', path, hash_path, and cardinality information. Excludes definition, remark, default_value, fixed_value, and facet fields.", "items": {
                    "type": "object",
                    "properties": {
                        "bbie_sc_id": {"type": ["integer", "null"], "description": "Unique identifier for the BBIE SC (if created)", "example": 12351},
                        "guid": {"type": ["string", "null"], "description": "Globally unique identifier for the BBIE SC"},
                        "based_dt_sc": {"type": "object", "description": "Data type supplementary component information"},
                        "path": {"type": "string", "description": "Hierarchical path string"},
                        "hash_path": {"type": "string", "description": "Hashed version of the path"},
                        "cardinality_min": {"type": "integer", "description": "Minimum cardinality", "example": 0},
                        "cardinality_max": {"type": "integer", "description": "Maximum cardinality", "example": 1}
                    }
                }}
            }}
        },
        "required": ["bbie_id"]
    }
)
async def create_bbie(
        from_abie_id: Annotated[int, Field(
            description="The ABIE ID that this BBIE originates from (parent ABIE).",
            examples=[123, 456, 789],
            gt=0,
            title="From ABIE ID"
        )],
        based_bcc_manifest_id: Annotated[int, Field(
            description="The BCC manifest ID that this BBIE is based on.",
            examples=[12345, 67890],
            gt=0,
            title="Based BCC Manifest ID"
        )]
) -> CreateBbieResponse:
    """
    Enable (use) a BBIE (Basic Business Information Entity) during BIE profiling.
    
    This tool is used to enable (use) a BBIE component during BIE profiling. It creates a new BBIE 
    with associated BBIEP record and automatically enables it (is_used=True) for use in the BIE.
    The path and hash_path columns are automatically calculated for all created records.
    
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
        from_abie_id (int): The ABIE ID that this BBIE originates from (parent ABIE)
        based_bcc_manifest_id (int): The BCC manifest ID that this BBIE is based on
    
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
        Enable a BBIE during BIE profiling (mandatory BBIE SCs are automatically created/enabled):
        >>> result = create_bbie(from_abie_id=123, based_bcc_manifest_id=456)
        >>> print(f"Created BBIE ID: {result.bbie_id}")
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()
    
    # Create service instance
    bie_service = BusinessInformationEntityService(requester=app_user)
    
    try:
        # Calculate path from _get_abie_related_components
        # Get the ABIE to find its path
        from_abie = bie_service.get_abie(from_abie_id)
        if not from_abie:
            raise ToolError(f"ABIE with ID {from_abie_id} not found.")
        
        # Get relationships and find the matching BBIE relationship
        relationships = _get_abie_related_components(
            from_abie.owner_top_level_asbiep_id, from_abie_id,
            from_abie.based_acc_manifest_id, from_abie.path
        )
        
        # Find the matching BBIE relationship by bcc_manifest_id
        bbie_path = None
        for rel in relationships:
            if isinstance(rel, BbieRelationshipInfo) and rel.based_bcc.bcc_manifest_id == based_bcc_manifest_id:
                bbie_path = rel.path
                break
        
        if not bbie_path:
            raise ToolError(
                f"Could not find BBIE relationship for BCC manifest ID {based_bcc_manifest_id} in the ABIE relationships. "
                f"This may indicate that the BCC is not a valid relationship for this ABIE."
            )
        
        # Call the service method to create the BBIE
        # Use default values for all optional parameters
        bbie_id_result, updates = bie_service.create_bbie(
            from_abie_id=from_abie_id,
            based_bcc_manifest_id=based_bcc_manifest_id,
            bbie_path=bbie_path
        )
        
        # Process mandatory BBIE SCs (supplementary components with cardinality_min >= 1)
        _process_mandatory_bbie_scs(
            bie_service=bie_service,
            bbie_id=bbie_id_result
        )
        
        # Get the created BBIE to access its BBIEP
        created_bbie = bie_service.get_bbie_by_bbie_id(bbie_id_result)
        if not created_bbie:
            raise ToolError(f"Created BBIE with ID {bbie_id_result} not found.")
        
        # Build simplified BBIEP info structure with supplementary components
        bbiep_detail = None
        if created_bbie.to_bbiep_id:
            # Get BCCP manifest ID from the BCC manifest
            cc_service = CoreComponentService()
            bcc_manifest = cc_service.get_bcc_manifest(based_bcc_manifest_id)
            if bcc_manifest and bcc_manifest.to_bccp_manifest_id:
                # Get full BBIEP info first
                bbiep_info_full = _get_bbiep_info(
                    top_level_asbiep_id=created_bbie.owner_top_level_asbiep_id,
                    bbie_id=bbie_id_result,
                    bbiep_id=created_bbie.to_bbiep_id,
                    bccp_manifest_id=bcc_manifest.to_bccp_manifest_id,
                    parent_bbie_path=bbie_path
                )
                # Convert to create response version (exclude definition, remark, biz_term, display_name)
                bbiep_detail = _convert_bbiep_info_to_create(bbiep_info_full)
        
        return CreateBbieResponse(
            bbie_id=bbie_id_result,
            bbiep=bbiep_detail
        )
    
    except HTTPException as e:
        logger.error(f"HTTP error creating BBIE: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 403:
            raise ToolError(f"Access denied: {e.detail}") from e
        elif e.status_code == 404:
            raise ToolError(f"Resource not found: {e.detail}") from e
        elif e.status_code == 500:
            raise ToolError(f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error creating BBIE: {e}")
        raise ToolError(
            f"An unexpected error occurred while creating the BBIE: {str(e)}. Please contact your system administrator."
        ) from e


@mcp.tool(
    name="update_bbie",
    description="Update an existing BBIE (Basic Business Information Entity) during BIE profiling. Can be used to toggle enable/disable (use/unuse) the BBIE, or modify its properties. Set is_used=True/1/'true'/'True' to enable/use or is_used=False/0/'false'/'False' to disable/unuse during profiling.",
    output_schema={
        "type": "object",
        "description": "Response containing the updated BBIE information with nested structure showing all updated supplementary components. The updates list includes all fields that were updated on the BBIE itself. The bbiep field contains nested structure with supplementary_components, each showing their updates. When is_used is set to True, mandatory supplementary components are automatically processed. When is_used is set to False, all supplementary components are automatically disabled.",
        "properties": {
            "bbie_id": {"type": "integer", "description": "Unique identifier of the updated BBIE", "example": 12345},
            "updates": {"type": "array", "items": {"type": "string"},
                        "description": "A list of field names that were updated on the BBIE itself (includes 'is_used' when toggling enable/disable during profiling)", "example": ["is_used", "definition", "cardinality_min", "default_value", "facet_min_length"]},
            "bbiep": {"type": ["object", "null"], "description": "Nested structure showing BBIEP and all updated supplementary components with their updates", "properties": {
                "bbiep_id": {"type": ["integer", "null"], "description": "Unique identifier of the BBIEP"},
                "updates": {"type": "array", "items": {"type": "string"}, "description": "List of fields updated on the BBIEP (typically empty)"},
                "guid": {"type": ["string", "null"], "description": "Globally unique identifier of the BBIEP"},
                "based_bccp": {"type": "object", "description": "Information about the BCCP that this BBIEP is based on"},
                "path": {"type": "string", "description": "Hierarchical path string"},
                "hash_path": {"type": "string", "description": "Hashed version of the path"},
                "supplementary_components": {"type": "array", "description": "List of supplementary components with their updates", "items": {
                    "type": "object",
                    "properties": {
                        "bbie_sc_id": {"type": ["integer", "null"], "description": "Unique identifier for the BBIE SC"},
                        "updates": {"type": "array", "items": {"type": "string"}, "description": "List of fields updated on this BBIE SC (e.g., ['is_used'])"},
                        "guid": {"type": ["string", "null"], "description": "Globally unique identifier for the BBIE SC"},
                        "based_dt_sc": {"type": "object", "description": "Data type supplementary component information"},
                        "path": {"type": "string", "description": "Hierarchical path string"},
                        "hash_path": {"type": "string", "description": "Hashed version of the path"},
                        "cardinality_min": {"type": "integer", "description": "Minimum cardinality"},
                        "cardinality_max": {"type": "integer", "description": "Maximum cardinality"}
                    }
                }}
            }}
        },
        "required": ["bbie_id", "updates"]
    }
)
async def update_bbie(
        bbie_id: Annotated[int, Field(
            description="The BBIE ID to update.",
            examples=[12345, 67890],
            gt=0,
            title="BBIE ID"
        )],
        is_used: Annotated[bool | str | None, Field(
            description="Toggle enable/disable (use/unuse) this BBIE during BIE profiling. Accepts bool, str, or None. String values are converted: 'True'/'true'/'1' -> True, 'False'/'false'/'0' -> False. Set to True/1/'true'/'True' to enable/use the BBIE (profiled and active), or False/0/'false'/'False' to disable/unuse it (not used in the BIE). This is used to toggle the component's usage during profiling. If not provided, is_used will not be updated.",
            examples=[True, False, "1", "0", "true", "false", "True", "False"],
            title="Is Used (Enable/Disable Toggle)"
        )] = None,
        is_deprecated: Annotated[bool | str | None, Field(
            description="Whether the BBIE is deprecated. Accepts bool, str, or None. String values are converted: 'True'/'true'/'1' -> True, 'False'/'false'/'0' -> False. If not provided, is_deprecated will not be updated.",
            examples=[True, False, "1", "0", "true", "false", "True", "False"],
            title="Is Deprecated"
        )] = None,
        is_nillable: Annotated[bool | str | None, Field(
            description="Whether the BBIE can have a nil/null value. Accepts bool, str, or None. String values are converted: 'True'/'true'/'1' -> True, 'False'/'false'/'0' -> False. If not provided, is_nillable will not be updated.",
            examples=[True, False, "1", "0", "true", "false", "True", "False"],
            title="Is Nillable"
        )] = None,
        cardinality_min: Annotated[int | str | None, Field(
            description="Minimum cardinality (minimum number of occurrences required, typically 0 or 1). Accepts int, str, or None. String values are automatically converted to integers. If not provided, cardinality_min will not be updated.",
            examples=[0, 1, "0", "1"],
            title="Cardinality Min"
        )] = None,
        cardinality_max: Annotated[int | str | None, Field(
            description="Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded). Accepts int, str, or None. String values are automatically converted to integers. If not provided, cardinality_max will not be updated.",
            examples=[1, -1, "1", "-1"],
            title="Cardinality Max"
        )] = None,
        definition: Annotated[str | None, Field(
            description="Definition to override the BCC definition. If not provided, definition will not be updated.",
            examples=["Custom definition for this BBIE"],
            title="Definition"
        )] = None,
        remark: Annotated[str | None, Field(
            description="Additional remarks or notes about the BBIEP. If not provided, remark will not be updated.",
            examples=["Used for purchase orders"],
            title="Remark"
        )] = None,
        example: Annotated[str | None, Field(
            description="Example about the BBIE. If not provided, example will not be updated.",
            examples=["Example for this BBIE"],
            title="Example"
        )] = None,
        default_value: Annotated[str | None, Field(
            description="Default value for the BBIE (mutually exclusive with fixed_value). Represents the 'default' attribute in XML. If not provided, default_value will not be updated.",
            examples=["0", "N/A"],
            title="Default Value"
        )] = None,
        fixed_value: Annotated[str | None, Field(
            description="Fixed value for the BBIE (mutually exclusive with default_value). Represents the 'fixed' attribute in XML. If not provided, fixed_value will not be updated.",
            examples=["USD", "EN"],
            title="Fixed Value"
        )] = None,
        facet_min_length: Annotated[int | str | None, Field(
            description="Minimum length facet for string types (XML facet restriction). Accepts int, str, or None. String values are automatically converted to integers. If not provided, facet_min_length will not be updated.",
            examples=[1, 10, "1", "10"],
            title="Facet Min Length"
        )] = None,
        facet_max_length: Annotated[int | str | None, Field(
            description="Maximum length facet for string types (XML facet restriction). Accepts int, str, or None. String values are automatically converted to integers. If not provided, facet_max_length will not be updated.",
            examples=[50, 255, "50", "255"],
            title="Facet Max Length"
        )] = None,
        facet_pattern: Annotated[str | None, Field(
            description="Pattern facet (regular expression) for string types (XML facet restriction). If not provided, facet_pattern will not be updated.",
            examples=[r"[A-Z]{2}", r"\\d{4}-\\d{2}-\\d{2}"],
            title="Facet Pattern"
        )] = None
) -> UpdateBbieResponse:
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
    - Boolean parameters (is_used, is_deprecated, is_nillable):
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
        is_deprecated (bool | str | None, optional): Whether the BBIE is deprecated. 
            Accepts bool, str ('True'/'true'/'1' for True, 'False'/'false'/'0' for False), or None.
            If not provided, will not be updated.
        is_nillable (bool | str | None, optional): Whether the BBIE can have a nil/null value.
            Accepts bool, str ('True'/'true'/'1' for True, 'False'/'false'/'0' for False), or None.
            If not provided, will not be updated.
        cardinality_min (int | str | None, optional): Minimum cardinality. 
            Accepts int, str (converted to int), or None. If not provided, will not be updated.
        cardinality_max (int | str | None, optional): Maximum cardinality (-1 means unbounded). 
            Accepts int, str (converted to int), or None. If not provided, will not be updated.
        definition (str | None, optional): Definition to override the BCC definition
        remark (str | None, optional): Additional remarks for the BBIEP
        example (str | None, optional): Example for the BBIE
        default_value (str | None, optional): Default value for the BBIE (mutually exclusive with fixed_value)
        fixed_value (str | None, optional): Fixed value for the BBIE (mutually exclusive with default_value)
        facet_min_length (int | str | None, optional): Minimum length facet for string types.
            Accepts int, str (converted to int), or None. If not provided, will not be updated.
        facet_max_length (int | str | None, optional): Maximum length facet for string types.
            Accepts int, str (converted to int), or None. If not provided, will not be updated.
        facet_pattern (str | None, optional): Pattern facet (regex) for string types
    
    Returns:
        UpdateBbieResponse: Response object containing:
            - bbie_id: ID of the updated BBIE
            - updates: List of fields that were updated on the BBIE itself (includes 'is_used', 'definition', 
              'remark', 'cardinality_min', 'cardinality_max', 'default_value', 'fixed_value', facet fields, etc.)
            - bbiep: Nested structure showing BBIEP and all supplementary components with their updates:
              - bbiep_id: ID of the BBIEP
              - updates: List of fields updated on the BBIEP (typically empty)
              - guid, based_bccp, path, hash_path: BBIEP properties
              - supplementary_components: List of supplementary components, each with:
                - bbie_sc_id: ID of the BBIE SC
                - updates: List of fields updated on this BBIE SC (e.g., ['is_used'])
                - primitiveRestriction: Required field that must not be None. Contains exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId.
                - Other component properties (guid, based_dt_sc, path, hash_path, cardinality, etc.)
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
        
        Update definition and facets:
        >>> result = update_bbie(bbie_id=789, definition="Custom definition", facet_min_length=5, facet_max_length=100)
        >>> result = update_bbie(bbie_id=789, facet_min_length="5", facet_max_length="100")
        
        Update cardinality:
        >>> result = update_bbie(bbie_id=789, cardinality_min=1, cardinality_max=3)
        >>> result = update_bbie(bbie_id=789, cardinality_min="1", cardinality_max="3")
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()
    
    # Convert string parameters to their proper types
    try:
        is_used = str_to_bool(is_used)
        is_deprecated = str_to_bool(is_deprecated)
        is_nillable = str_to_bool(is_nillable)
        cardinality_min = str_to_int(cardinality_min)
        cardinality_max = str_to_int(cardinality_max)
        facet_min_length = str_to_int(facet_min_length)
        facet_max_length = str_to_int(facet_max_length)
    except ToolError:
        raise  # Re-raise ToolError as-is
    except Exception as e:
        raise ToolError(
            f"Type conversion error: {str(e)}. Please check your parameter types and try again."
        ) from e
    
    # Create service instance
    bie_service = BusinessInformationEntityService(requester=app_user)
    
    # Validate that at least one field is provided for update
    if all(field is None for field in [is_used, is_deprecated, is_nillable, cardinality_min, cardinality_max, definition, remark, example, default_value, fixed_value, facet_min_length, facet_max_length, facet_pattern]):
        raise ToolError(
            "At least one field must be provided for update."
        )
    
    try:
        # Get the existing BBIE to check current is_used status
        existing_bbie = bie_service.get_bbie_by_bbie_id(bbie_id)
        if not existing_bbie:
            raise ToolError(f"BBIE with ID {bbie_id} not found.")
        
        previous_is_used = existing_bbie.is_used
        
        # Validate: Prevent disabling if cardinality_min >= 1 (required relationship)
        if is_used is not None and is_used == False and previous_is_used == True:
            # Check if this BBIE is required (cardinality_min >= 1)
            # First check the BBIE's own cardinality_min
            if existing_bbie.cardinality_min >= 1:
                raise ToolError(
                    f"Cannot disable BBIE {bbie_id} because it is required (cardinality_min={existing_bbie.cardinality_min} >= 1). "
                    f"Required relationships cannot be disabled. If you need to remove this relationship, "
                    f"you must first change its cardinality_min to 0."
                )
            
            # Also check in the parent ABIE's relationships (as a double-check)
            from_abie = bie_service.get_abie(existing_bbie.from_abie_id)
            if from_abie:
                relationships = _get_abie_related_components(
                    from_abie.owner_top_level_asbiep_id,
                    from_abie.abie_id,
                    from_abie.based_acc_manifest_id,
                    from_abie.path
                )
                for rel in relationships:
                    if isinstance(rel, BbieRelationshipInfo) and rel.bbie_id == bbie_id:
                        if rel.cardinality_min >= 1:
                            raise ToolError(
                                f"Cannot disable BBIE {bbie_id} because it is required (cardinality_min={rel.cardinality_min} >= 1). "
                                f"Required relationships cannot be disabled. If you need to remove this relationship, "
                                f"you must first change its cardinality_min to 0."
                            )
                        break
        
        # Call the service method to update the BBIE
        # The service layer will retrieve the existing BBIE and handle all validation
        bbie_id_result, updates = bie_service.update_bbie(
            bbie_id=bbie_id,
            is_used=is_used,
            is_deprecated=is_deprecated,
            is_nillable=is_nillable,
            cardinality_min=cardinality_min,
            cardinality_max=cardinality_max,
            definition=definition,
            remark=remark,
            example=example,
            default_value=default_value,
            fixed_value=fixed_value,
            facet_min_length=facet_min_length,
            facet_max_length=facet_max_length,
            facet_pattern=facet_pattern
        )
        
        # Sync Version Identifier BBIE's fixed_value to top_level_asbiep.version if fixed_value was updated
        # Only sync if fixed_value was actually updated (in the updates list)
        if 'fixed_value' in updates:
            _sync_version_identifier_bbie_to_version(
                bie_service=bie_service,
                bbie_id=bbie_id,
                fixed_value=fixed_value
            )
        
        # Handle is_used state changes and build nested response
        bbiep_detail = None
        if is_used is not None and is_used != previous_is_used:
            # Track updated components
            updated_components = {}
            if is_used:
                # is_used changed from False to True: process mandatory supplementary components
                _process_mandatory_bbie_scs(
                    bie_service=bie_service,
                    bbie_id=bbie_id,
                    updated_components=updated_components
                )
            else:
                # is_used changed from True to False: disable all supplementary components
                _disable_all_bbie_scs_for_bbie(
                    bie_service=bie_service,
                    bbie_id=bbie_id,
                    updated_components=updated_components
                )
            
            # Build nested response structure showing supplementary components with updates
            bbiep_detail = _build_update_bbiep_detail(
                bie_service=bie_service,
                bbie_id=bbie_id,
                updated_components=updated_components
            )
        else:
            # Build nested response structure even if is_used didn't change
            bbiep_detail = _build_update_bbiep_detail(
                bie_service=bie_service,
                bbie_id=bbie_id,
                updated_components=None  # No updates tracked if is_used didn't change
            )
        
        return UpdateBbieResponse(
            bbie_id=bbie_id_result,
            updates=updates,
            bbiep=bbiep_detail
        )
    
    except HTTPException as e:
        logger.error(f"HTTP error updating BBIE: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 403:
            raise ToolError(f"Access denied: {e.detail}") from e
        elif e.status_code == 404:
            raise ToolError(f"Resource not found: {e.detail}") from e
        elif e.status_code == 500:
            raise ToolError(f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error updating BBIE: {e}")
        raise ToolError(
            f"An unexpected error occurred while updating the BBIE: {str(e)}. Please contact your system administrator."
        ) from e


@mcp.tool(
    name="create_bbie_sc",
    description="Enable (use) a BBIE_SC (Basic Business Information Entity Supplementary Component) during BIE profiling. Creates a new BBIE_SC record, enabling the supplementary component in the BIE. The BBIE_SC is automatically enabled (is_used=True) and profiled. The path and hash_path columns are automatically calculated for all created records. Use get_bbie_by_bbie_id() or get_bbie_by_based_bcc_manifest_id() to retrieve available supplementary components, then use the 'based_dt_sc.dt_sc_manifest_id' from the supplementary_components array as the based_dt_sc_manifest_id parameter.",
    output_schema={
        "type": "object",
        "description": "Response containing the newly created and enabled BBIE_SC information. The BBIE_SC is automatically enabled (is_used=True) for BIE profiling.",
        "properties": {
            "bbie_sc_id": {"type": "integer", "description": "Unique identifier of the newly created and enabled BBIE_SC", "example": 12345},
            "updates": {"type": "array", "items": {"type": "string"},
                        "description": "A list of field names that were set during creation (includes 'is_used' as the BBIE_SC is enabled)", "example": ["bbie_sc_id", "is_used", "cardinality_min", "cardinality_max"]}
        },
        "required": ["bbie_sc_id", "updates"]
    }
)
async def create_bbie_sc(
        bbie_id: Annotated[int, Field(
            description="The BBIE ID that this BBIE_SC belongs to.",
            examples=[123, 456, 789],
            gt=0,
            title="BBIE ID"
        )],
        based_dt_sc_manifest_id: Annotated[int, Field(
            description="The DT_SC manifest ID that this BBIE_SC is based on. This can be obtained from get_bbie_by_bbie_id() or get_bbie_by_based_bcc_manifest_id() response: use 'to_bbiep.supplementary_components[].based_dt_sc.dt_sc_manifest_id' from the supplementary_components array.",
            examples=[12345, 67890],
            gt=0,
            title="Based DT_SC Manifest ID"
        )]
) -> CreateBbieScResponse:
    """
    Enable (use) a BBIE_SC (Basic Business Information Entity Supplementary Component) during BIE profiling.
    
    This tool is used to enable (use) a BBIE_SC component during BIE profiling. It creates a new BBIE_SC 
    record and automatically enables it (is_used=True) for use in the BIE.
    The path and hash_path columns are automatically calculated for all created records.
    
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()
    
    # Create service instance
    bie_service = BusinessInformationEntityService(requester=app_user)
    
    try:
        # Get the BBIE to find its path and BCCP
        bbie = bie_service.get_bbie_by_bbie_id(bbie_id)
        if not bbie:
            raise ToolError(f"BBIE with ID {bbie_id} not found.")
        
        # Get BCCP manifest to get BDT manifest ID
        # Access the relationship through based_bcc_manifest
        bccp_manifest = bbie.based_bcc_manifest.to_bccp_manifest
        if not bccp_manifest:
            raise ToolError(f"BCCP manifest not found for BBIE {bbie_id}.")
        bdt_manifest_id = bccp_manifest.bdt_manifest_id
        
        # Calculate BBIE_SC path: {bbie_path}>BCCP-{bccp_manifest_id}>DT-{bdt_manifest_id}>DT_SC-{dt_sc_manifest_id}
        bbiep_path = f"{bbie.path}>BCCP-{bccp_manifest.bccp_manifest_id}"
        dt_path = f"{bbiep_path}>DT-{bdt_manifest_id}"
        bbie_sc_path = f"{dt_path}>DT_SC-{based_dt_sc_manifest_id}"
        
        # Call the service method to create the BBIE_SC
        bbie_sc_id_result, updates = bie_service.create_bbie_sc(
            bbie_id=bbie_id,
            based_dt_sc_manifest_id=based_dt_sc_manifest_id,
            bbie_sc_path=bbie_sc_path
        )
        
        return CreateBbieScResponse(
            bbie_sc_id=bbie_sc_id_result,
            updates=updates
        )
    
    except HTTPException as e:
        logger.error(f"HTTP error creating BBIE_SC: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 403:
            raise ToolError(f"Access denied: {e.detail}") from e
        elif e.status_code == 404:
            raise ToolError(f"Resource not found: {e.detail}") from e
        elif e.status_code == 500:
            raise ToolError(f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error creating BBIE_SC: {e}")
        raise ToolError(
            f"An unexpected error occurred while creating the BBIE_SC: {str(e)}. Please contact your system administrator."
        ) from e


@mcp.tool(
    name="update_bbie_sc",
    description="Update an existing BBIE_SC (Basic Business Information Entity Supplementary Component) during BIE profiling. Can be used to toggle enable/disable (use/unuse) the BBIE_SC, or modify its properties. Set is_used=True/1/'true'/'True' to enable/use or is_used=False/0/'false'/'False' to disable/unuse during profiling. Integer and boolean parameters accept both their native types and string representations (strings are automatically converted). Use get_bbie_by_bbie_id() or get_bbie_by_based_bcc_manifest_id() to retrieve existing supplementary components, then use the 'bbie_sc_id' from the supplementary_components array as the bbie_sc_id parameter.",
    output_schema={
        "type": "object",
        "description": "Response containing the updated BBIE_SC information. The updates list includes 'is_used' when the BBIE_SC is toggled between enabled/disabled (used/unused) during BIE profiling. Integer and boolean parameters accept both their native types and string representations (strings are automatically converted to the appropriate type).",
        "properties": {
            "bbie_sc_id": {"type": "integer", "description": "Unique identifier of the updated BBIE_SC", "example": 12345},
            "updates": {"type": "array", "items": {"type": "string"},
                        "description": "A list of field names that were updated (includes 'is_used' when toggling enable/disable during profiling)", "example": ["is_used", "definition", "cardinality_min", "default_value", "facet_min_length"]}
        },
        "required": ["bbie_sc_id", "updates"]
    }
)
async def update_bbie_sc(
        bbie_sc_id: Annotated[int, Field(
            description="The BBIE_SC ID to update. This can be obtained from get_bbie_by_bbie_id() or get_bbie_by_based_bcc_manifest_id() response: use 'to_bbiep.supplementary_components[].bbie_sc_id' from the supplementary_components array (only available if the component already exists, i.e., bbie_sc_id is not null).",
            examples=[12345, 67890],
            gt=0,
            title="BBIE_SC ID"
        )],
        is_used: Annotated[bool | str | None, Field(
            description="Toggle enable/disable (use/unuse) this BBIE_SC during BIE profiling. Accepts bool, str, or None. String values are converted: 'True'/'true'/'1' -> True, 'False'/'false'/'0' -> False. Set to True/1/'true'/'True' to enable/use the BBIE_SC (profiled and active), or False/0/'false'/'False' to disable/unuse it (not used in the BIE). This is used to toggle the component's usage during profiling. If not provided, is_used will not be updated.",
            examples=[True, False, "1", "0", "true", "false", "True", "False"],
            title="Is Used (Enable/Disable Toggle)"
        )] = None,
        is_deprecated: Annotated[bool | str | None, Field(
            description="Whether the BBIE_SC is deprecated. Accepts bool, str, or None. String values are converted: 'True'/'true'/'1' -> True, 'False'/'false'/'0' -> False. If not provided, is_deprecated will not be updated.",
            examples=[True, False, "1", "0", "true", "false", "True", "False"],
            title="Is Deprecated"
        )] = None,
        cardinality_min: Annotated[int | str | None, Field(
            description="Minimum cardinality (minimum number of occurrences required, typically 0 or 1). Accepts int, str, or None. String values are automatically converted to integers. If not provided, cardinality_min will not be updated.",
            examples=[0, 1, "0", "1"],
            title="Cardinality Min"
        )] = None,
        cardinality_max: Annotated[int | str | None, Field(
            description="Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded). Accepts int, str, or None. String values are automatically converted to integers. If not provided, cardinality_max will not be updated.",
            examples=[1, -1, "1", "-1"],
            title="Cardinality Max"
        )] = None,
        definition: Annotated[str | None, Field(
            description="Definition to override the DT_SC definition. If not provided, definition will not be updated.",
            examples=["Custom definition for this BBIE_SC"],
            title="Definition"
        )] = None,
        remark: Annotated[str | None, Field(
            description="Additional remarks or notes about the BBIE_SC. If not provided, remark will not be updated.",
            examples=["Used for purchase orders"],
            title="Remark"
        )] = None,
        example: Annotated[str | None, Field(
            description="Example about the BBIE_SC. If not provided, example will not be updated.",
            examples=["Example for this BBIE_SC"],
            title="Example"
        )] = None,
        default_value: Annotated[str | None, Field(
            description="Default value for the BBIE_SC (mutually exclusive with fixed_value). Represents the 'default' attribute in XML. If not provided, default_value will not be updated.",
            examples=["0", "N/A"],
            title="Default Value"
        )] = None,
        fixed_value: Annotated[str | None, Field(
            description="Fixed value for the BBIE_SC (mutually exclusive with default_value). Represents the 'fixed' attribute in XML. If not provided, fixed_value will not be updated.",
            examples=["USD", "EN"],
            title="Fixed Value"
        )] = None,
        facet_min_length: Annotated[int | str | None, Field(
            description="Minimum length facet for string types (XML facet restriction). Accepts int, str, or None. String values are automatically converted to integers. If not provided, facet_min_length will not be updated.",
            examples=[1, 10, "1", "10"],
            title="Facet Min Length"
        )] = None,
        facet_max_length: Annotated[int | str | None, Field(
            description="Maximum length facet for string types (XML facet restriction). Accepts int, str, or None. String values are automatically converted to integers. If not provided, facet_max_length will not be updated.",
            examples=[50, 255, "50", "255"],
            title="Facet Max Length"
        )] = None,
        facet_pattern: Annotated[str | None, Field(
            description="Pattern facet (regular expression) for string types (XML facet restriction). If not provided, facet_pattern will not be updated.",
            examples=[r"[A-Z]{2}", r"\\d{4}-\\d{2}-\\d{2}"],
            title="Facet Pattern"
        )] = None
) -> UpdateBbieScResponse:
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
    - Boolean parameters (is_used, is_deprecated):
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
        is_deprecated (bool | str | None, optional): Whether the BBIE_SC is deprecated. 
            Accepts bool, str ('True'/'true'/'1' for True, 'False'/'false'/'0' for False), or None.
            If not provided, will not be updated.
        cardinality_min (int | str | None, optional): Minimum cardinality. 
            Accepts int, str (converted to int), or None. If not provided, will not be updated.
        cardinality_max (int | str | None, optional): Maximum cardinality (-1 means unbounded). 
            Accepts int, str (converted to int), or None. If not provided, will not be updated.
        definition (str | None, optional): Definition to override the DT_SC definition
        remark (str | None, optional): Additional remarks
        example (str | None, optional): Example for the BBIE_SC
        default_value (str | None, optional): Default value for the BBIE_SC (mutually exclusive with fixed_value)
        fixed_value (str | None, optional): Fixed value for the BBIE_SC (mutually exclusive with default_value)
        facet_min_length (int | str | None, optional): Minimum length facet for string types.
            Accepts int, str (converted to int), or None. If not provided, will not be updated.
        facet_max_length (int | str | None, optional): Maximum length facet for string types.
            Accepts int, str (converted to int), or None. If not provided, will not be updated.
        facet_pattern (str | None, optional): Pattern facet (regex) for string types
    
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
        
        Update definition and facets:
        >>> result = update_bbie_sc(bbie_sc_id=789, definition="Custom definition", facet_min_length=5, facet_max_length=100)
        >>> result = update_bbie_sc(bbie_sc_id=789, facet_min_length="5", facet_max_length="100")
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()
    
    # Convert string parameters to their proper types
    try:
        is_used = str_to_bool(is_used)
        is_deprecated = str_to_bool(is_deprecated)
        cardinality_min = str_to_int(cardinality_min)
        cardinality_max = str_to_int(cardinality_max)
        facet_min_length = str_to_int(facet_min_length)
        facet_max_length = str_to_int(facet_max_length)
    except ToolError:
        raise  # Re-raise ToolError as-is
    except Exception as e:
        raise ToolError(
            f"Type conversion error: {str(e)}. Please check your parameter types and try again."
        ) from e
    
    # Create service instance
    bie_service = BusinessInformationEntityService(requester=app_user)
    
    # Validate that at least one field is provided for update
    if all(field is None for field in [is_used, is_deprecated, cardinality_min, cardinality_max, definition, remark, example, default_value, fixed_value, facet_min_length, facet_max_length, facet_pattern]):
        raise ToolError(
            "At least one field must be provided for update."
        )
    
    try:
        # Call the service method to update the BBIE_SC
        # The service layer will retrieve the existing BBIE_SC and handle all validation
        bbie_sc_id_result, updates = bie_service.update_bbie_sc(
            bbie_sc_id=bbie_sc_id,
            is_used=is_used,
            is_deprecated=is_deprecated,
            cardinality_min=cardinality_min,
            cardinality_max=cardinality_max,
            definition=definition,
            remark=remark,
            example=example,
            default_value=default_value,
            fixed_value=fixed_value,
            facet_min_length=facet_min_length,
            facet_max_length=facet_max_length,
            facet_pattern=facet_pattern
        )
        
        return UpdateBbieScResponse(
            bbie_sc_id=bbie_sc_id_result,
            updates=updates
        )
    
    except HTTPException as e:
        logger.error(f"HTTP error updating BBIE_SC: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 403:
            raise ToolError(f"Access denied: {e.detail}") from e
        elif e.status_code == 404:
            raise ToolError(f"Resource not found: {e.detail}") from e
        elif e.status_code == 500:
            raise ToolError(f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error updating BBIE_SC: {e}")
        raise ToolError(
            f"An unexpected error occurred while updating the BBIE_SC: {str(e)}. Please contact your system administrator."
        ) from e


# Helper functions (placed after their usage)



def _validate_and_create_primitive_restriction(
    xbt_manifest_id: int | None,
    code_list_manifest_id: int | None,
    agency_id_list_manifest_id: int | None
) -> PrimitiveRestriction | None:
    """
    Validate and create a PrimitiveRestriction object.
    
    Validation rules:
    - Can return None if all values are None
    - If not None, exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set
    
    Args:
        xbt_manifest_id: XBT manifest ID
        code_list_manifest_id: Code list manifest ID
        agency_id_list_manifest_id: Agency ID list manifest ID
        
    Returns:
        PrimitiveRestriction | None: PrimitiveRestriction object if valid, None if all are None
        
    Raises:
        ValueError: If multiple values are set or validation fails
    """
    if xbt_manifest_id is None and code_list_manifest_id is None and agency_id_list_manifest_id is None:
        return None
    
    # Validate: exactly one must be set
    has_xbt = xbt_manifest_id is not None
    has_code_list = code_list_manifest_id is not None
    has_agency_id_list = agency_id_list_manifest_id is not None
    
    count = sum([has_xbt, has_code_list, has_agency_id_list])
    
    if count == 0:
        raise ValueError(
            "PrimitiveRestriction validation failed: Exactly one of xbtManifestId, codeListManifestId, "
            "or agencyIdListManifestId must be set. All cannot be None."
        )
    
    if count > 1:
        set_values = []
        if has_xbt:
            set_values.append(f"xbtManifestId={xbt_manifest_id}")
        if has_code_list:
            set_values.append(f"codeListManifestId={code_list_manifest_id}")
        if has_agency_id_list:
            set_values.append(f"agencyIdListManifestId={agency_id_list_manifest_id}")
        
        raise ValueError(
            f"PrimitiveRestriction validation failed: Exactly one of xbtManifestId, codeListManifestId, "
            f"or agencyIdListManifestId must be set. Found {count} values set: {', '.join(set_values)}."
        )
    
    return PrimitiveRestriction(
        xbtManifestId=xbt_manifest_id,
        codeListManifestId=code_list_manifest_id,
        agencyIdListManifestId=agency_id_list_manifest_id
    )


def _get_business_contexts_info(top_level_asbiep_id: int, bie_service: BusinessInformationEntityService) -> list[
    BusinessContextInfo]:
    """
    Get business contexts information for a top-level ASBIEP.

    Args:
        top_level_asbiep: TopLevelAsbiep model instance

    Returns:
        list[BusinessContextInfo]: List of business contexts information
    """
    # Get business contexts using the separate service function
    business_contexts_info = []
    try:
        business_contexts = bie_service.get_business_contexts_by_top_level_asbiep_id(top_level_asbiep_id)
        for biz_ctx in business_contexts:
            business_contexts_info.append(BusinessContextInfo(
                biz_ctx_id=biz_ctx.biz_ctx_id,
                guid=biz_ctx.guid,
                name=biz_ctx.name
            ))
    except Exception as e:
        logger.warning(f"Failed to retrieve business contexts for TopLevelAsbiep {top_level_asbiep_id}: {e}")
        # Continue without business contexts rather than failing completely

    return business_contexts_info


def _create_business_information_entity_result(top_level_asbiep, bie_service) -> GetTopLevelAsbiepListResponseEntry:
    """
    Create a BIE (Business Information Entity) result from database models.

    Args:
        top_level_asbiep: TopLevelAsbiep model instance (contains asbiep relationship)
        bie_service: BusinessInformationEntityService instance for retrieving related data

    Returns:
        GetBusinessInformationEntityResponse: Formatted BIE (Business Information Entity) result
    """
    business_contexts_info = _get_business_contexts_info(top_level_asbiep.top_level_asbiep_id, bie_service)

    # Get asbiep from the relationship
    asbiep = top_level_asbiep.asbiep

    # Get property_term and den from asccp_manifest
    property_term = None
    den = None
    if asbiep.based_asccp_manifest:
        if asbiep.based_asccp_manifest.asccp:
            property_term = asbiep.based_asccp_manifest.asccp.property_term
        # Use the den field directly from asccp_manifest
        den = asbiep.based_asccp_manifest.den

    return GetTopLevelAsbiepListResponseEntry(
        top_level_asbiep_id=top_level_asbiep.top_level_asbiep_id,
        asbiep_id=asbiep.asbiep_id,
        guid=asbiep.guid,
        den=den,
        property_term=property_term,
        display_name=asbiep.display_name,
        version=top_level_asbiep.version,
        status=top_level_asbiep.status,
        biz_term=asbiep.biz_term,
        remark=asbiep.remark,
        business_contexts=business_contexts_info,
        state=top_level_asbiep.state,
        is_deprecated=top_level_asbiep.is_deprecated,
        deprecated_reason=top_level_asbiep.deprecated_reason,
        deprecated_remark=top_level_asbiep.deprecated_remark,
        owner=_create_user_info(top_level_asbiep.owner_user),
        created=WhoAndWhen(
            who=_create_user_info(asbiep.created_by_user),
            when=asbiep.creation_timestamp
        ),
        last_updated=WhoAndWhen(
            who=_create_user_info(top_level_asbiep.last_updated_by_user),
            when=top_level_asbiep.last_update_timestamp
        )
    )


def _create_top_level_asbiep_info(top_level_asbiep) -> TopLevelAsbiepInfo:
    # Create LibraryInfo and ReleaseInfo (required fields)
    if not top_level_asbiep.release:
        raise ValueError("Top-level ASBIEP must have an associated release")

    release_info = ReleaseInfo(
        release_id=top_level_asbiep.release.release_id,
        release_num=top_level_asbiep.release.release_num,
        state=top_level_asbiep.release.state
    )

    if not top_level_asbiep.release.library:
        raise ValueError("Release must have an associated library")

    library_info = LibraryInfo(
        library_id=top_level_asbiep.release.library.library_id,
        name=top_level_asbiep.release.library.name
    )

    return TopLevelAsbiepInfo(
        top_level_asbiep_id=top_level_asbiep.top_level_asbiep_id,
        library=library_info,
        release=release_info,
        version=top_level_asbiep.version,
        status=top_level_asbiep.status,
        state=top_level_asbiep.state,
        is_deprecated=top_level_asbiep.is_deprecated,
        deprecated_reason=top_level_asbiep.deprecated_reason,
        deprecated_remark=top_level_asbiep.deprecated_remark,
        owner=_create_user_info(top_level_asbiep.owner_user)
    )


def _get_asbiep_info(top_level_asbiep_id: int, asbiep_id: int | None, asccp_manifest_id: int,
                     parent_asbie_path: str | None) -> AsbiepInfo:
    app_user = get_current_user()
    if not app_user:
        raise ToolError("Authentication required to perform this operation.")
    bie_service = BusinessInformationEntityService(requester=app_user)
    cc_service = CoreComponentService()

    # Create TopLevelAsbiepInfo
    top_level_asbiep = bie_service.get_top_level_asbiep_by_id(top_level_asbiep_id)
    owner_top_level_asbiep = _create_top_level_asbiep_info(top_level_asbiep)

    asccp_manifest = cc_service.get_asccp_by_manifest_id(asccp_manifest_id)
    acc_manifest = cc_service.get_acc_by_manifest_id(asccp_manifest.role_of_acc_manifest_id)

    if parent_asbie_path:
        asbiep_path = f"{parent_asbie_path}>ASCCP-{asccp_manifest.asccp_manifest_id}"
    else:
        asbiep_path = f"ASCCP-{asccp_manifest.asccp_manifest_id}"

    abie_path = f"{asbiep_path}>ACC-{acc_manifest.acc_manifest_id}"

    asccp_info = AsccpInfo(
        asccp_manifest_id=asccp_manifest.asccp_manifest_id,
        asccp_id=asccp_manifest.asccp.asccp_id,
        role_of_acc_manifest_id=asccp_manifest.role_of_acc_manifest_id,
        guid=asccp_manifest.asccp.guid,
        den=asccp_manifest.den,
        property_term=asccp_manifest.asccp.property_term,
        definition=asccp_manifest.asccp.definition,
        definition_source=asccp_manifest.asccp.definition_source,
        is_deprecated=asccp_manifest.asccp.is_deprecated
    )

    acc_info = AccInfo(
        acc_manifest_id=acc_manifest.acc_manifest_id,
        acc_id=acc_manifest.acc.acc_id,
        guid=acc_manifest.acc.guid,
        den=acc_manifest.den,
        object_class_term=acc_manifest.acc.object_class_term,
        definition=acc_manifest.acc.definition,
        definition_source=acc_manifest.acc.definition_source,
        is_deprecated=acc_manifest.acc.is_deprecated
    )

    asbiep = bie_service.get_asbiep(asbiep_id)
    abie = None
    if asbiep:
        abie = bie_service.get_abie(asbiep.role_of_abie_id)

    # Get relationships for the ABIE
    if abie:
        relationships = _get_abie_related_components(top_level_asbiep_id,
                                                     abie.abie_id, abie.based_acc_manifest_id, abie.path)
        role_of_abie = AbieInfo(
            abie_id=abie.abie_id,
            guid=abie.guid,
            based_acc_manifest=acc_info,
            definition=abie.definition,
            remark=abie.remark,
            relationships=relationships,
            created=WhoAndWhen(
                who=_create_user_info(abie.created_by_user),
                when=abie.creation_timestamp
            ),
            last_updated=WhoAndWhen(
                who=_create_user_info(abie.last_updated_by_user),
                when=abie.last_update_timestamp
            )
        )
    else:
        relationships = _get_abie_related_components(top_level_asbiep_id,
                                                     None, acc_manifest.acc_manifest_id, abie_path)
        role_of_abie = AbieInfo(
            abie_id=None,
            guid=None,
            based_acc_manifest=acc_info,
            definition=None,
            remark=None,
            relationships=relationships,
            created=None,
            last_updated=None
        )

    # Create AsbiepInfo
    if asbiep:
        asbiep_info = AsbiepInfo(
            asbiep_id=asbiep.asbiep_id,
            owner_top_level_asbiep=owner_top_level_asbiep,
            based_asccp_manifest=asccp_info,
            path=asbiep.path,
            hash_path=asbiep.hash_path,
            role_of_abie=role_of_abie,
            definition=asbiep.definition,
            remark=asbiep.remark,
            biz_term=asbiep.biz_term,
            display_name=asbiep.display_name,
            created=WhoAndWhen(
                who=_create_user_info(asbiep.created_by_user),
                when=asbiep.creation_timestamp
            ),
            last_updated=WhoAndWhen(
                who=_create_user_info(asbiep.last_updated_by_user),
                when=asbiep.last_update_timestamp
            )
        )
    else:
        asbiep_info = AsbiepInfo(
            asbiep_id=None,
            owner_top_level_asbiep=owner_top_level_asbiep,
            based_asccp_manifest=asccp_info,
            path=asbiep_path,
            hash_path=hashlib.sha256(asbiep_path.encode()).hexdigest(),
            role_of_abie=role_of_abie,
            definition=None,
            remark=None,
            biz_term=None,
            display_name=None,
            created=None,
            last_updated=None
        )

    return asbiep_info


def _create_dt_sc_info_from_dt_sc_manifest(dt_sc_manifest) -> DtScInfo | None:
    """
    Create DtScInfo from a DtScManifest.

    Args:
        dt_sc_manifest: DtScManifest model instance with dt_sc relationship loaded

    Returns:
        DtScInfo | None: DtScInfo object if dt_sc exists, None otherwise
    """
    if not dt_sc_manifest:
        return None

    dt_sc = dt_sc_manifest.dt_sc if hasattr(dt_sc_manifest, 'dt_sc') else None
    if not dt_sc:
        return None

    return DtScInfo(
        dt_sc_manifest_id=dt_sc_manifest.dt_sc_manifest_id,
        dt_sc_id=dt_sc.dt_sc_id,
        guid=dt_sc.guid,
        object_class_term=dt_sc.object_class_term,
        property_term=dt_sc.property_term,
        representation_term=dt_sc.representation_term,
        definition=dt_sc.definition,
        definition_source=dt_sc.definition_source,
        cardinality_min=dt_sc.cardinality_min,
        cardinality_max=dt_sc.cardinality_max,
        default_value=dt_sc.default_value,
        fixed_value=dt_sc.fixed_value,
        is_deprecated=dt_sc.is_deprecated
    )


def _create_bbie_sc_info_from_bbie_sc(bbie_sc, owner_top_level_asbiep: TopLevelAsbiepInfo) -> BbieScInfo | None:
    """
    Create BbieScInfo from a BbieSc record.

    Args:
        bbie_sc: BbieSc model instance with based_dt_sc_manifest relationship loaded
        owner_top_level_asbiep: TopLevelAsbiepInfo for the owner

    Returns:
        BbieScInfo | None: BbieScInfo object if dt_sc exists, None otherwise
    """
    dt_sc_manifest = bbie_sc.based_dt_sc_manifest if hasattr(bbie_sc, 'based_dt_sc_manifest') else None
    dt_sc_info = _create_dt_sc_info_from_dt_sc_manifest(dt_sc_manifest)

    if not dt_sc_info:
        return None

    # Create Facet object if any facet values exist
    facet = None
    if bbie_sc.facet_min_length is not None or bbie_sc.facet_max_length is not None or bbie_sc.facet_pattern is not None:
        facet = Facet(
            facet_min_length=bbie_sc.facet_min_length,
            facet_max_length=bbie_sc.facet_max_length,
            facet_pattern=bbie_sc.facet_pattern
        )
    
    # Create ValueConstraint object with validation
    value_constraint = validate_and_create_value_constraint(
        default_value=bbie_sc.default_value,
        fixed_value=bbie_sc.fixed_value
    )
    
    # Create PrimitiveRestriction object with validation
    primitive_restriction = _validate_and_create_primitive_restriction(
        xbt_manifest_id=bbie_sc.xbt_manifest_id,
        code_list_manifest_id=bbie_sc.code_list_manifest_id,
        agency_id_list_manifest_id=bbie_sc.agency_id_list_manifest_id
    )
    
    return BbieScInfo(
        bbie_sc_id=bbie_sc.bbie_sc_id,
        guid=bbie_sc.guid,
        based_dt_sc=dt_sc_info,
        definition=bbie_sc.definition,
        cardinality_min=bbie_sc.cardinality_min,
        cardinality_max=bbie_sc.cardinality_max,
        primitiveRestriction=primitive_restriction,
        valueConstraint=value_constraint,
        facet=facet,
        owner_top_level_asbiep=owner_top_level_asbiep
    )


def _create_bbie_sc_info_from_dt_sc_manifest(dt_sc_manifest, parent_dt_path: str,
                                             owner_top_level_asbiep: TopLevelAsbiepInfo,
                                             bie_service: "BusinessInformationEntityService | None" = None) -> BbieScInfo | None:
    """
    Create BbieScInfo from a DtScManifest (when no actual BBIE_SC record exists).

    Args:
        dt_sc_manifest: DtScManifest model instance with dt_sc relationship loaded
        parent_dt_path: DT path
        owner_top_level_asbiep: TopLevelAsbiepInfo for the owner
        bie_service: BusinessInformationEntityService instance (optional, for fetching default primitive restriction)

    Returns:
        BbieScInfo | None: BbieScInfo object if dt_sc exists, None otherwise
    """
    dt_sc_info = _create_dt_sc_info_from_dt_sc_manifest(dt_sc_manifest)

    if not dt_sc_info:
        return None

    dt_sc = dt_sc_manifest.dt_sc if hasattr(dt_sc_manifest, 'dt_sc') else None
    if not dt_sc:
        return None

    bbie_sc_path = f"{parent_dt_path}>DT_SC-{dt_sc_manifest.dt_sc_manifest_id}"

    # Create BbieScInfo with default/None values since there's no actual BBIE_SC record
    # Create ValueConstraint object with validation
    value_constraint = validate_and_create_value_constraint(
        default_value=dt_sc.default_value,
        fixed_value=dt_sc.fixed_value
    )
    
    # Try to fetch default primitive restriction from DtScAwdPri (is_default=1)
    primitive_restriction = None
    if bie_service:
        # Get default primitive restriction from DtScAwdPri using DT_SC manifest ID
        try:
            xbt_manifest_id, code_list_manifest_id, agency_id_list_manifest_id = bie_service.get_dt_sc_awd_pri(
                dt_sc_manifest.dt_sc_manifest_id)
            # Create PrimitiveRestriction object with validation
            primitive_restriction = _validate_and_create_primitive_restriction(
                xbt_manifest_id=xbt_manifest_id,
                code_list_manifest_id=code_list_manifest_id,
                agency_id_list_manifest_id=agency_id_list_manifest_id
            )
        except ValueError as e:
            # Validation error - log and re-raise
            logger.error(f"PrimitiveRestriction validation failed for DT_SC manifest {dt_sc_manifest.dt_sc_manifest_id}: {e}")
            raise
        except Exception:
            # If we can't fetch the default primitive restriction, just leave it as None
            # This is not a critical error - the BBIE_SC doesn't exist yet anyway
            pass
    
    return BbieScInfo(
        bbie_sc_id=None,  # No actual BBIE_SC ID exists
        guid=None,  # No actual GUID exists
        based_dt_sc=dt_sc_info,
        path=bbie_sc_path,
        hash_path=hashlib.sha256(bbie_sc_path.encode()).hexdigest(),
        definition=None,  # Use DT_SC definition by default
        cardinality_min=dt_sc.cardinality_min,  # Use DT_SC cardinality
        cardinality_max=dt_sc.cardinality_max if dt_sc.cardinality_max is not None else -1,  # Use DT_SC cardinality
        primitiveRestriction=primitive_restriction,  # Default from DtScAwdPri if available
        valueConstraint=value_constraint,  # Use DT_SC value constraints
        facet=None,  # No BBIE-specific facet
        owner_top_level_asbiep=owner_top_level_asbiep
    )


def _get_supplementary_components(top_level_asbiep_id: int, bbie_id: int | None, bdt_manifest_id: int,
                                  parent_bbiep_path: str, owner_top_level_asbiep: TopLevelAsbiepInfo) -> list[
    BbieScInfo]:
    """
    Get supplementary components for a BBIE, either from actual BBIE_SC records or from DT supplementary components.

    Args:
        top_level_asbiep_id: Top-level ASBIEP ID
        bbie_id: BBIE ID (None if not yet created)
        bdt_manifest_id: BDT manifest ID to fetch DT supplementary components from
        parent_bbiep_path: Parent BBIE path
        owner_top_level_asbiep: TopLevelAsbiepInfo for the owner

    Returns:
        list[BbieScInfo]: List of supplementary component information objects
    """
    app_user = get_current_user()
    if not app_user:
        raise ToolError("Authentication required to perform this operation.")
    bie_service = BusinessInformationEntityService(requester=app_user)
    dt_service = DataTypeService()

    dt_path = f"{parent_bbiep_path}>DT-{bdt_manifest_id}"

    supplementary_components = []
    try:
        dt_sc_manifests = dt_service.get_supplementary_components_by_dt_manifest_id(bdt_manifest_id)

        for dt_sc_manifest in dt_sc_manifests:
            if dt_sc_manifest.dt_sc.cardinality_max > 0:
                bbie_sc_info = _create_bbie_sc_info_from_dt_sc_manifest(dt_sc_manifest, dt_path, owner_top_level_asbiep, bie_service)
                if bbie_sc_info:
                    supplementary_components.append(bbie_sc_info)
    except Exception as e:
        logger.warning(f"Failed to retrieve supplementary components for DT manifest {bdt_manifest_id}: {e}")
        # Continue without supplementary components rather than failing completely

    if bbie_id is not None:
        # When bbie_id is provided, fetch actual BBIE_SC records
        try:
            bbie_scs = bie_service.get_bbie_sc_list(bbie_id)
            for bbie_sc in bbie_scs:
                for supplementary_component in supplementary_components:
                    if bbie_sc.hash_path == supplementary_component.hash_path:
                        supplementary_component.bbie_sc_id = bbie_sc.bbie_sc_id
                        supplementary_component.guid = bbie_sc.guid
                        supplementary_component.path = bbie_sc.path
                        supplementary_component.hash_path = bbie_sc.hash_path
                        supplementary_component.definition = bbie_sc.definition
                        supplementary_component.cardinality_min = bbie_sc.cardinality_min
                        supplementary_component.cardinality_max = bbie_sc.cardinality_max
                        
                        # Update facet
                        if bbie_sc.facet_min_length is not None or bbie_sc.facet_max_length is not None or bbie_sc.facet_pattern is not None:
                            supplementary_component.facet = Facet(
                                facet_min_length=bbie_sc.facet_min_length,
                                facet_max_length=bbie_sc.facet_max_length,
                                facet_pattern=bbie_sc.facet_pattern
                            )
                        else:
                            supplementary_component.facet = None
                        
                        # Update valueConstraint with validation
                        supplementary_component.valueConstraint = validate_and_create_value_constraint(
                            default_value=bbie_sc.default_value,
                            fixed_value=bbie_sc.fixed_value
                        )
                        
                        # Update primitiveRestriction with validation
                        supplementary_component.primitiveRestriction = _validate_and_create_primitive_restriction(
                            xbt_manifest_id=bbie_sc.xbt_manifest_id,
                            code_list_manifest_id=bbie_sc.code_list_manifest_id,
                            agency_id_list_manifest_id=bbie_sc.agency_id_list_manifest_id
                        )
        except Exception as e:
            logger.warning(f"Failed to retrieve supplementary components for BBIE {bbie_id}: {e}")
            # Continue without supplementary components rather than failing completely

    return supplementary_components


def _get_bbiep_info(top_level_asbiep_id: int, bbie_id: int | None, bbiep_id: int | None, bccp_manifest_id: int,
                    parent_bbie_path: str) -> BbiepInfo:
    app_user = get_current_user()
    if not app_user:
        raise ToolError("Authentication required to perform this operation.")
    bie_service = BusinessInformationEntityService(requester=app_user)
    cc_service = CoreComponentService()
    dt_service = DataTypeService()

    # Create TopLevelAsbiepInfo
    top_level_asbiep = bie_service.get_top_level_asbiep_by_id(top_level_asbiep_id)
    owner_top_level_asbiep = _create_top_level_asbiep_info(top_level_asbiep)

    bccp_manifest = cc_service.get_bccp_by_manifest_id(bccp_manifest_id)
    bdt_manifest = dt_service.get_data_type_by_manifest_id(bccp_manifest.bdt_manifest_id)

    bbiep_path = f"{parent_bbie_path}>BCCP-{bccp_manifest.bccp_manifest_id}"

    bccp_info = BccpInfo(
        bccp_manifest_id=bccp_manifest.bccp_manifest_id,
        bccp_id=bccp_manifest.bccp_id,
        guid=bccp_manifest.bccp.guid,
        den=bccp_manifest.den,
        property_term=bccp_manifest.bccp.property_term,
        representation_term=bccp_manifest.bccp.representation_term,
        definition=bccp_manifest.bccp.definition,
        definition_source=bccp_manifest.bccp.definition_source,
        bdt_manifest=DtInfo(
            dt_manifest_id=bccp_manifest.bdt_manifest.dt_manifest_id,
            dt_id=bccp_manifest.bdt_manifest.dt_id,
            guid=bccp_manifest.bdt_manifest.dt.guid,
            den=bccp_manifest.bdt_manifest.den,
            data_type_term=bccp_manifest.bdt_manifest.dt.data_type_term,
            qualifier=bccp_manifest.bdt_manifest.dt.qualifier,
            representation_term=bccp_manifest.bdt_manifest.dt.representation_term,
            six_digit_id=bccp_manifest.bdt_manifest.dt.six_digit_id,
            based_dt_manifest_id=bccp_manifest.bdt_manifest.based_dt_manifest_id,
            definition=bccp_manifest.bdt_manifest.dt.definition,
            definition_source=bccp_manifest.bdt_manifest.dt.definition_source,
            is_deprecated=bccp_manifest.bdt_manifest.dt.is_deprecated
        ),
        is_deprecated=bccp_manifest.bccp.is_deprecated
    )

    bbiep = bie_service.get_bbiep(bbiep_id)

    # Fetch supplementary components (always populated, even when bbiep is None)
    supplementary_components = _get_supplementary_components(
        top_level_asbiep_id=top_level_asbiep_id,
        bbie_id=bbie_id,
        bdt_manifest_id=bccp_manifest.bdt_manifest.dt_manifest_id,
        parent_bbiep_path=bbiep_path,
        owner_top_level_asbiep=owner_top_level_asbiep
    )

    # Create BbiepInfo
    if bbiep:
        bbiep_info = BbiepInfo(
            bbiep_id=bbiep.bbiep_id,
            guid=bbiep.guid,
            based_bccp=bccp_info,
            path=bbiep.path,
            hash_path=bbiep.hash_path,
            definition=bbiep.definition,
            remark=bbiep.remark,
            biz_term=bbiep.biz_term,
            display_name=bbiep.display_name,
            supplementary_components=supplementary_components,
            owner_top_level_asbiep=owner_top_level_asbiep
        )
    else:
        bbiep_info = BbiepInfo(
            bbiep_id=None,
            guid=None,
            based_bccp=bccp_info,
            path=bbiep_path,
            hash_path=hashlib.sha256(bbiep_path.encode()).hexdigest(),
            definition=None,
            remark=None,
            biz_term=None,
            display_name=None,
            supplementary_components=supplementary_components,
            owner_top_level_asbiep=owner_top_level_asbiep
        )

    return bbiep_info


def _convert_bbiep_info_to_create(bbiep_info: BbiepInfo) -> CreateBbiepInfo:
    """
    Convert BbiepInfo to CreateBbiepInfo by excluding fields that won't have values for newly generated records.
    
    Args:
        bbiep_info: Full BbiepInfo object
        
    Returns:
        CreateBbiepInfo: BBIEP info for create_bbie response without definition, remark, biz_term, display_name
    """
    # Convert supplementary components to create response versions
    create_scs = []
    for sc in bbiep_info.supplementary_components:
        create_sc = CreateBbieScInfo(
            bbie_sc_id=sc.bbie_sc_id,
            guid=sc.guid,
            based_dt_sc=sc.based_dt_sc,
            path=sc.path,
            hash_path=sc.hash_path,
            cardinality_min=sc.cardinality_min,
            cardinality_max=sc.cardinality_max
        )
        create_scs.append(create_sc)
    
    return CreateBbiepInfo(
        bbiep_id=bbiep_info.bbiep_id,
        guid=bbiep_info.guid,
        based_bccp=bbiep_info.based_bccp,
        path=bbiep_info.path,
        hash_path=bbiep_info.hash_path,
        supplementary_components=create_scs
    )


def _create_asbiep_result(asbiep, bie_service) -> GetTopLevelAsbiepResponse:
    """
    Create an ASBIEP result from database models.

    Args:
        asbiep: Asbiep model instance
        bie_service: BusinessInformationEntityService instance for retrieving related data

    Returns:
        GetTopLevelAsbiepResponse: Formatted ASBIEP result
    """
    # Get the top-level ASBIEP from the relationship
    top_level_asbiep = asbiep.owner_top_level_asbiep

    # Create AsccpInfo for based_asccp_manifest
    if not asbiep.based_asccp_manifest:
        raise ValueError("ASBIEP must have an associated ASCCP manifest")

    asccp_manifest = asbiep.based_asccp_manifest
    asbiep_info = _get_asbiep_info(top_level_asbiep.top_level_asbiep_id,
                                   asbiep.asbiep_id, asccp_manifest.asccp_manifest_id, None)

    # Get business contexts for the top-level ASBIEP
    business_contexts = _get_business_contexts_info(top_level_asbiep.top_level_asbiep_id, bie_service)

    return GetTopLevelAsbiepResponse(
        top_level_asbiep_id=top_level_asbiep.top_level_asbiep_id,
        asbiep=asbiep_info,
        version=top_level_asbiep.version,
        status=top_level_asbiep.status,
        business_contexts=business_contexts,
        state=top_level_asbiep.state,
        is_deprecated=top_level_asbiep.is_deprecated,
        deprecated_reason=top_level_asbiep.deprecated_reason,
        deprecated_remark=top_level_asbiep.deprecated_remark,
        owner=_create_user_info(top_level_asbiep.owner_user),
        created=WhoAndWhen(
            who=_create_user_info(asbiep.created_by_user),
            when=asbiep.creation_timestamp
        ),
        last_updated=WhoAndWhen(
            who=_create_user_info(top_level_asbiep.last_updated_by_user),
            when=top_level_asbiep.last_update_timestamp
        )
    )


def _build_create_role_of_abie_detail(
    bie_service: BusinessInformationEntityService,
    abie_id: int,
    visited_abie_ids: set[int]
) -> CreateRoleOfAbieDetail | None:
    """
    Build a CreateRoleOfAbieDetail structure recursively for an ABIE (for create_asbie response).
    
    Args:
        bie_service: The BusinessInformationEntityService instance
        abie_id: The ABIE ID to build structure for
        visited_abie_ids: Set of ABIE IDs already visited (to prevent infinite recursion)
    
    Returns:
        CreateRoleOfAbieDetail or None if ABIE not found
    """
    # Prevent infinite recursion
    if abie_id in visited_abie_ids:
        return None
    visited_abie_ids.add(abie_id)
    
    # Get the ABIE
    abie = bie_service.get_abie(abie_id)
    if not abie:
        return None
    
    # Get relationships of the ABIE
    relationships = _get_abie_related_components(
        abie.owner_top_level_asbiep_id,
        abie_id,
        abie.based_acc_manifest_id,
        abie.path
    )
    
    relationship_details = []
    
    # Process each relationship
    for rel in relationships:
        if isinstance(rel, AsbieRelationshipInfo):
            # Only include if it's mandatory and was created/enabled
            if rel.cardinality_min >= 1 and rel.asbie_id is not None and rel.is_used:
                try:
                    # Get full ASBIE details
                    asbie = bie_service.get_asbie_by_asbie_id(rel.asbie_id)
                    if asbie:
                        # Build simplified ASBIE relationship detail (exclude is_nillable, remark)
                        asbie_detail = CreateAsbieRelationshipDetail(
                            asbie_id=rel.asbie_id,
                            guid=asbie.guid,
                            path=rel.path,
                            cardinality_min=rel.cardinality_min,
                            cardinality_max=rel.cardinality_max,
                            based_ascc=rel.based_ascc
                        )
                        
                        # Build recursive structure if ASBIEP exists
                        if asbie.to_asbiep_id:
                            asbiep = bie_service.get_asbiep(asbie.to_asbiep_id)
                            if asbiep and asbiep.role_of_abie_id:
                                role_of_abie_detail = _build_create_role_of_abie_detail(
                                    bie_service=bie_service,
                                    abie_id=asbiep.role_of_abie_id,
                                    visited_abie_ids=visited_abie_ids
                                )
                                if role_of_abie_detail:
                                    asbie_detail.asbiep = CreateAsbiepRelationshipDetail(
                                        asbiep_id=asbie.to_asbiep_id,
                                        role_of_abie=role_of_abie_detail
                                    )
                        
                        relationship_details.append(CreateRelationshipDetail(asbie=asbie_detail))
                except Exception as e:
                    logger.warning(f"Failed to build ASBIE detail for {rel.asbie_id}: {e}")
        
        elif isinstance(rel, BbieRelationshipInfo):
            # Only include if it's mandatory and was created/enabled
            if rel.cardinality_min >= 1 and rel.bbie_id is not None and rel.is_used:
                try:
                    # Get full BBIE details
                    bbie = bie_service.get_bbie_by_bbie_id(rel.bbie_id)
                    if bbie:
                        # Build simplified BBIE relationship detail (exclude remark)
                        bbie_detail = CreateBbieRelationshipDetail(
                            bbie_id=rel.bbie_id,
                            guid=bbie.guid,
                            path=rel.path,
                            cardinality_min=rel.cardinality_min,
                            cardinality_max=rel.cardinality_max,
                            is_nillable=rel.is_nillable,
                            based_bcc=rel.based_bcc
                        )
                        relationship_details.append(CreateRelationshipDetail(bbie=bbie_detail))
                except Exception as e:
                    logger.warning(f"Failed to build BBIE detail for {rel.bbie_id}: {e}")
    
    return CreateRoleOfAbieDetail(
        abie_id=abie_id,
        relationships=relationship_details
    )


def _build_role_of_abie_detail(
    bie_service: BusinessInformationEntityService,
    abie_id: int,
    visited_abie_ids: set[int]
) -> RoleOfAbieDetail | None:
    """
    Build a RoleOfAbieDetail structure recursively for an ABIE.
    
    Args:
        bie_service: The BusinessInformationEntityService instance
        abie_id: The ABIE ID to build structure for
        visited_abie_ids: Set of ABIE IDs already visited (to prevent infinite recursion)
    
    Returns:
        RoleOfAbieDetail or None if ABIE not found
    """
    # Prevent infinite recursion
    if abie_id in visited_abie_ids:
        return None
    visited_abie_ids.add(abie_id)
    
    # Get the ABIE
    abie = bie_service.get_abie(abie_id)
    if not abie:
        return None
    
    # Get relationships of the ABIE
    relationships = _get_abie_related_components(
        abie.owner_top_level_asbiep_id,
        abie_id,
        abie.based_acc_manifest_id,
        abie.path
    )
    
    relationship_details = []
    
    # Process each relationship
    for rel in relationships:
        if isinstance(rel, AsbieRelationshipInfo):
            # Only include if it's mandatory and was created/enabled
            if rel.cardinality_min >= 1 and rel.asbie_id is not None and rel.is_used:
                try:
                    # Get full ASBIE details
                    asbie = bie_service.get_asbie_by_asbie_id(rel.asbie_id)
                    if asbie:
                        # Build ASBIE relationship detail
                        asbie_detail = AsbieRelationshipDetail(
                            asbie_id=rel.asbie_id,
                            guid=asbie.guid,
                            path=rel.path,
                            cardinality_min=rel.cardinality_min,
                            cardinality_max=rel.cardinality_max,
                            is_nillable=rel.is_nillable,
                            remark=rel.remark,
                            based_ascc=rel.based_ascc
                        )
                        
                        # Build recursive structure if ASBIEP exists
                        if asbie.to_asbiep_id:
                            asbiep = bie_service.get_asbiep(asbie.to_asbiep_id)
                            if asbiep and asbiep.role_of_abie_id:
                                role_of_abie_detail = _build_role_of_abie_detail(
                                    bie_service=bie_service,
                                    abie_id=asbiep.role_of_abie_id,
                                    visited_abie_ids=visited_abie_ids
                                )
                                if role_of_abie_detail:
                                    asbie_detail.asbiep = AsbiepRelationshipDetail(
                                        asbiep_id=asbie.to_asbiep_id,
                                        role_of_abie=role_of_abie_detail
                                    )
                        
                        relationship_details.append(RelationshipDetail(asbie=asbie_detail))
                except Exception as e:
                    logger.warning(f"Failed to build ASBIE detail for {rel.asbie_id}: {e}")
        
        elif isinstance(rel, BbieRelationshipInfo):
            # Only include if it's mandatory and was created/enabled
            if rel.cardinality_min >= 1 and rel.bbie_id is not None and rel.is_used:
                try:
                    # Get full BBIE details
                    bbie = bie_service.get_bbie_by_bbie_id(rel.bbie_id)
                    if bbie:
                        # Build BBIE relationship detail
                        bbie_detail = BbieRelationshipDetail(
                            bbie_id=rel.bbie_id,
                            guid=bbie.guid,
                            path=rel.path,
                            cardinality_min=rel.cardinality_min,
                            cardinality_max=rel.cardinality_max,
                            is_nillable=rel.is_nillable,
                            remark=rel.remark,
                            based_bcc=rel.based_bcc
                        )
                        relationship_details.append(RelationshipDetail(bbie=bbie_detail))
                except Exception as e:
                    logger.warning(f"Failed to build BBIE detail for {rel.bbie_id}: {e}")
    
    return RoleOfAbieDetail(
        abie_id=abie_id,
        relationships=relationship_details
    )


def _process_mandatory_relationships_recursive(
    bie_service: BusinessInformationEntityService,
    abie_id: int,
    visited_abie_ids: set[int],
    updated_components: dict[str, list[str]] | None = None
) -> None:
    """
    Recursively process mandatory relationships (cardinality_min >= 1) for an ABIE.
    
    For each mandatory relationship:
    - If it already exists (has asbie_id/bbie_id) but is_used=False, update it to set is_used=True
    - If it doesn't exist, create it
    - After creating/updating, recursively process the role_of_abie's relationships
    
    Args:
        bie_service: The BusinessInformationEntityService instance
        abie_id: The ABIE ID to process relationships for
        visited_abie_ids: Set of ABIE IDs already visited (to prevent infinite recursion)
        updated_components: Optional dict to track updated components (key: "asbie_{id}" or "bbie_{id}", value: list of updated fields)
    """
    # Prevent infinite recursion
    if abie_id in visited_abie_ids:
        return
    visited_abie_ids.add(abie_id)
    
    # Get the ABIE
    abie = bie_service.get_abie(abie_id)
    if not abie:
        return
    
    # Get relationships of the ABIE
    relationships = _get_abie_related_components(
        abie.owner_top_level_asbiep_id,
        abie_id,
        abie.based_acc_manifest_id,
        abie.path
    )
    
    # Process each relationship
    for rel in relationships:
        if isinstance(rel, AsbieRelationshipInfo):
            # Check if this is a mandatory relationship
            if rel.cardinality_min >= 1:
                if rel.asbie_id is not None:
                    # Already exists - check if it needs to be enabled
                    if not rel.is_used:
                        try:
                            # Update the ASBIE to set is_used=True
                            _, rel_updates = bie_service.update_asbie(
                                asbie_id=rel.asbie_id,
                                is_used=True
                            )
                            # Track updates
                            if updated_components is not None:
                                updated_components[f"asbie_{rel.asbie_id}"] = rel_updates
                            # Get the updated ASBIE to find its role_of_abie_id
                            updated_asbie = bie_service.get_asbie_by_asbie_id(rel.asbie_id)
                            if updated_asbie and updated_asbie.to_asbiep_id:
                                asbiep = bie_service.get_asbiep(updated_asbie.to_asbiep_id)
                                if asbiep and asbiep.role_of_abie_id:
                                    # Recursively process the role_of_abie
                                    _process_mandatory_relationships_recursive(
                                        bie_service=bie_service,
                                        abie_id=asbiep.role_of_abie_id,
                                        visited_abie_ids=visited_abie_ids,
                                        updated_components=updated_components
                                    )
                        except Exception as e:
                            # Log error but continue processing other relationships
                            logger.warning(f"Failed to set is_used=True for ASBIE {rel.asbie_id}: {e}")
                else:
                    # Doesn't exist - create it
                    try:
                        # Create the ASBIE
                        new_asbie_id, _ = bie_service.create_asbie(
                            from_abie_id=abie_id,
                            based_ascc_manifest_id=rel.based_ascc.ascc_manifest_id,
                            asbie_path=rel.path
                        )
                        # Track creation (newly created ASBIEs have is_used=True by default)
                        if updated_components is not None:
                            updated_components[f"asbie_{new_asbie_id}"] = ["is_used"]  # Newly created, so is_used was set
                        # Get the created ASBIE to find its role_of_abie_id
                        new_asbie = bie_service.get_asbie_by_asbie_id(new_asbie_id)
                        if new_asbie and new_asbie.to_asbiep_id:
                            asbiep = bie_service.get_asbiep(new_asbie.to_asbiep_id)
                            if asbiep and asbiep.role_of_abie_id:
                                # Recursively process the role_of_abie
                                _process_mandatory_relationships_recursive(
                                    bie_service=bie_service,
                                    abie_id=asbiep.role_of_abie_id,
                                    visited_abie_ids=visited_abie_ids,
                                    updated_components=updated_components
                                )
                    except Exception as e:
                        # Log error but continue processing other relationships
                        logger.warning(f"Failed to create ASBIE for ASCC manifest ID {rel.based_ascc.ascc_manifest_id}: {e}")
        
        elif isinstance(rel, BbieRelationshipInfo):
            # Check if this is a mandatory relationship
            if rel.cardinality_min >= 1:
                logger.debug(f"Processing mandatory BBIE relationship: bcc_manifest_id={rel.based_bcc.bcc_manifest_id}, "
                           f"den={rel.based_bcc.den}, bbie_id={rel.bbie_id}, is_used={rel.is_used}, "
                           f"cardinality_min={rel.cardinality_min}")
                if rel.bbie_id is not None:
                    # Already exists - check if it needs to be enabled
                    if not rel.is_used:
                        try:
                            logger.debug(f"Enabling existing BBIE {rel.bbie_id} (mandatory relationship)")
                            # Update the BBIE to set is_used=True
                            _, rel_updates = bie_service.update_bbie(
                                bbie_id=rel.bbie_id,
                                is_used=True
                            )
                            # Track updates
                            if updated_components is not None:
                                updated_components[f"bbie_{rel.bbie_id}"] = rel_updates
                            logger.debug(f"Successfully enabled BBIE {rel.bbie_id}")
                            # Process mandatory BBIE SCs for the enabled BBIE
                            _process_mandatory_bbie_scs(
                                bie_service=bie_service,
                                bbie_id=rel.bbie_id,
                                updated_components=updated_components
                            )
                        except Exception as e:
                            # Log error but continue processing other relationships
                            logger.warning(f"Failed to set is_used=True for BBIE {rel.bbie_id}: {e}")
                    else:
                        logger.debug(f"BBIE {rel.bbie_id} is already enabled (is_used=True)")
                else:
                    # Doesn't exist - create it
                    try:
                        logger.debug(f"Creating new BBIE for mandatory relationship: bcc_manifest_id={rel.based_bcc.bcc_manifest_id}, "
                                   f"den={rel.based_bcc.den}")
                        # Create the BBIE
                        new_bbie_id, _ = bie_service.create_bbie(
                            from_abie_id=abie_id,
                            based_bcc_manifest_id=rel.based_bcc.bcc_manifest_id,
                            bbie_path=rel.path
                        )
                        logger.debug(f"Successfully created BBIE {new_bbie_id}")
                        # Track creation
                        if updated_components is not None:
                            updated_components[f"bbie_{new_bbie_id}"] = ["is_used"]  # Newly created, so is_used was set
                        # Process mandatory BBIE SCs for the newly created BBIE
                        _process_mandatory_bbie_scs(
                            bie_service=bie_service,
                            bbie_id=new_bbie_id,
                            updated_components=updated_components
                        )
                    except Exception as e:
                        # Log error but continue processing other relationships
                        logger.warning(f"Failed to create BBIE for BCC manifest ID {rel.based_bcc.bcc_manifest_id}: {e}")
            else:
                logger.debug(f"Skipping optional BBIE relationship: bcc_manifest_id={rel.based_bcc.bcc_manifest_id}, "
                           f"den={rel.based_bcc.den}, cardinality_min={rel.cardinality_min}")


def _process_mandatory_bbie_scs(
    bie_service: BusinessInformationEntityService,
    bbie_id: int,
    updated_components: dict[str, list[str]] | None = None
) -> None:
    """
    Process mandatory BBIE SCs (supplementary components with cardinality_min >= 1) for a BBIE.
    
    For each mandatory BBIE SC:
    - If it already exists (has bbie_sc_id) but is_used=False, update it to set is_used=True
    - If it doesn't exist (bbie_sc_id is None), create it
    
    Args:
        bie_service: The BusinessInformationEntityService instance
        bbie_id: The BBIE ID to process supplementary components for
        updated_components: Optional dict to track updated components (key: "bbie_sc_{id}", value: list of updated fields)
    """
    # Get the BBIE
    bbie = bie_service.get_bbie_by_bbie_id(bbie_id)
    if not bbie:
        return
    
    # Get the BBIEP to access BCCP manifest
    if not bbie.to_bbiep_id:
        return
    
    bbiep = bie_service.get_bbiep(bbie.to_bbiep_id)
    if not bbiep:
        return
    
    # Get BCCP manifest to find BDT manifest ID
    cc_service = CoreComponentService()
    bccp_manifest = cc_service.get_bccp_by_manifest_id(bbiep.based_bccp_manifest_id)
    if not bccp_manifest:
        return
    
    bdt_manifest_id = bccp_manifest.bdt_manifest_id
    
    # Get top-level ASBIEP info
    top_level_asbiep = bie_service.get_top_level_asbiep_by_id(bbie.owner_top_level_asbiep_id)
    if not top_level_asbiep:
        return
    
    owner_top_level_asbiep = _create_top_level_asbiep_info(top_level_asbiep)
    
    # Calculate BBIEP path for supplementary components
    # The BBIEP path is: {bbie.path}>BCCP-{bccp_manifest_id}
    bbiep_path = f"{bbie.path}>BCCP-{bccp_manifest.bccp_manifest_id}"
    
    # Get supplementary components
    supplementary_components = _get_supplementary_components(
        top_level_asbiep_id=bbie.owner_top_level_asbiep_id,
        bbie_id=bbie_id,
        bdt_manifest_id=bdt_manifest_id,
        parent_bbiep_path=bbiep_path,
        owner_top_level_asbiep=owner_top_level_asbiep
    )
    
    # Get existing BBIE_SC records to check is_used status
    existing_bbie_scs = {}
    try:
        bbie_sc_list = bie_service.get_bbie_sc_list(bbie_id)
        for bbie_sc in bbie_sc_list:
            existing_bbie_scs[bbie_sc.based_dt_sc_manifest_id] = bbie_sc
    except Exception as e:
        logger.warning(f"Failed to retrieve existing BBIE_SC records for BBIE {bbie_id}: {e}")
    
    # Process each mandatory supplementary component (cardinality_min >= 1)
    for sc_info in supplementary_components:
        if sc_info.cardinality_min >= 1:
            if sc_info.bbie_sc_id is not None:
                # Already exists - check if it needs to be enabled
                try:
                    # Get the actual BBIE_SC to check is_used
                    bbie_sc = existing_bbie_scs.get(sc_info.based_dt_sc.dt_sc_manifest_id)
                    if bbie_sc and not bbie_sc.is_used:
                        # Update the BBIE_SC to set is_used=True
                        _, sc_updates = bie_service.update_bbie_sc(
                            bbie_sc_id=sc_info.bbie_sc_id,
                            is_used=True
                        )
                        # Track updates
                        if updated_components is not None:
                            updated_components[f"bbie_sc_{sc_info.bbie_sc_id}"] = sc_updates
                except Exception as e:
                    # Log error but continue processing other components
                    logger.warning(f"Failed to set is_used=True for BBIE_SC {sc_info.bbie_sc_id}: {e}")
            else:
                # Doesn't exist - create it
                try:
                    # Calculate BBIE_SC path: {bbiep_path}>DT-{bdt_manifest_id}>DT_SC-{dt_sc_manifest_id}
                    dt_path = f"{bbiep_path}>DT-{bdt_manifest_id}"
                    bbie_sc_path = f"{dt_path}>DT_SC-{sc_info.based_dt_sc.dt_sc_manifest_id}"
                    
                    # Create the BBIE_SC
                    new_bbie_sc_id, _ = bie_service.create_bbie_sc(
                        bbie_id=bbie_id,
                        based_dt_sc_manifest_id=sc_info.based_dt_sc.dt_sc_manifest_id,
                        bbie_sc_path=bbie_sc_path
                    )
                    # Track creation
                    if updated_components is not None:
                        updated_components[f"bbie_sc_{new_bbie_sc_id}"] = ["is_used"]  # Newly created, so is_used was set
                except Exception as e:
                    # Log error but continue processing other components
                    logger.warning(f"Failed to create BBIE_SC for DT_SC manifest ID {sc_info.based_dt_sc.dt_sc_manifest_id}: {e}")


def _disable_all_relationships_for_asbie(
    bie_service: BusinessInformationEntityService,
    asbie_id: int,
    visited_asbie_ids: set[int],
    updated_components: dict[str, list[str]] | None = None
) -> None:
    """
    Recursively disable all relationships (ASBIEs and BBIEs) for an ASBIE when is_used=False.
    
    Args:
        bie_service: The BusinessInformationEntityService instance
        asbie_id: The ASBIE ID to disable relationships for
        visited_asbie_ids: Set of ASBIE IDs already visited (to prevent infinite recursion)
        updated_components: Optional dict to track updated components (key: "asbie_{id}" or "bbie_{id}", value: list of updated fields)
    """
    # Prevent infinite recursion
    if asbie_id in visited_asbie_ids:
        return
    visited_asbie_ids.add(asbie_id)
    
    # Get the ASBIE
    asbie = bie_service.get_asbie_by_asbie_id(asbie_id)
    if not asbie or not asbie.to_asbiep_id:
        return
    
    # Get the ASBIEP and its role_of_abie
    asbiep = bie_service.get_asbiep(asbie.to_asbiep_id)
    if not asbiep or not asbiep.role_of_abie_id:
        return
    
    # Get the ABIE
    abie = bie_service.get_abie(asbiep.role_of_abie_id)
    if not abie:
        return
    
    # Get relationships of the ABIE
    relationships = _get_abie_related_components(
        abie.owner_top_level_asbiep_id,
        abie.abie_id,
        abie.based_acc_manifest_id,
        abie.path
    )
    
    # Disable all relationships (skip mandatory ones with cardinality_min >= 1)
    for rel in relationships:
        if isinstance(rel, AsbieRelationshipInfo):
            # Skip mandatory relationships (cardinality_min >= 1)
            if rel.cardinality_min >= 1:
                logger.info(f"Skipping disable of ASBIE {rel.asbie_id} because it is required (cardinality_min={rel.cardinality_min} >= 1)")
                continue
            
            if rel.asbie_id is not None and rel.is_used:
                try:
                    # Disable the ASBIE
                    _, rel_updates = bie_service.update_asbie(
                        asbie_id=rel.asbie_id,
                        is_used=False
                    )
                    # Track updates
                    if updated_components is not None:
                        updated_components[f"asbie_{rel.asbie_id}"] = rel_updates
                    # Recursively disable its relationships
                    _disable_all_relationships_for_asbie(
                        bie_service=bie_service,
                        asbie_id=rel.asbie_id,
                        visited_asbie_ids=visited_asbie_ids,
                        updated_components=updated_components
                    )
                except Exception as e:
                    logger.warning(f"Failed to disable ASBIE {rel.asbie_id}: {e}")
        
        elif isinstance(rel, BbieRelationshipInfo):
            # Skip mandatory relationships (cardinality_min >= 1)
            if rel.cardinality_min >= 1:
                logger.info(f"Skipping disable of BBIE {rel.bbie_id} because it is required (cardinality_min={rel.cardinality_min} >= 1)")
                continue
            
            if rel.bbie_id is not None and rel.is_used:
                try:
                    # Disable the BBIE
                    _, rel_updates = bie_service.update_bbie(
                        bbie_id=rel.bbie_id,
                        is_used=False
                    )
                    # Track updates
                    if updated_components is not None:
                        updated_components[f"bbie_{rel.bbie_id}"] = rel_updates
                    # Disable its supplementary components
                    _disable_all_bbie_scs_for_bbie(
                        bie_service=bie_service,
                        bbie_id=rel.bbie_id,
                        updated_components=updated_components
                    )
                except Exception as e:
                    logger.warning(f"Failed to disable BBIE {rel.bbie_id}: {e}")


def _disable_all_bbie_scs_for_bbie(
    bie_service: BusinessInformationEntityService,
    bbie_id: int,
    updated_components: dict[str, list[str]] | None = None
) -> None:
    """
    Disable all supplementary components for a BBIE when is_used=False.
    
    Args:
        bie_service: The BusinessInformationEntityService instance
        bbie_id: The BBIE ID to disable supplementary components for
        updated_components: Optional dict to track updated components (key: "bbie_sc_{id}", value: list of updated fields)
    """
    try:
        # Get the BBIE to access its relationships
        bbie = bie_service.get_bbie_by_bbie_id(bbie_id)
        if not bbie or not bbie.to_bbiep_id:
            return
        
        # Get BBIEP and BCCP manifest to find BDT manifest ID
        bbiep = bie_service.get_bbiep(bbie.to_bbiep_id)
        if not bbiep:
            return
        
        cc_service = CoreComponentService()
        bccp_manifest = cc_service.get_bccp_by_manifest_id(bbiep.based_bccp_manifest_id)
        if not bccp_manifest:
            return
        
        bdt_manifest_id = bccp_manifest.bdt_manifest_id
        
        # Get top-level ASBIEP info
        top_level_asbiep = bie_service.get_top_level_asbiep_by_id(bbie.owner_top_level_asbiep_id)
        if not top_level_asbiep:
            return
        
        owner_top_level_asbiep = _create_top_level_asbiep_info(top_level_asbiep)
        
        # Calculate BBIEP path for supplementary components
        bbiep_path = f"{bbie.path}>BCCP-{bccp_manifest.bccp_manifest_id}"
        
        # Get supplementary components to check cardinality
        supplementary_components = _get_supplementary_components(
            top_level_asbiep_id=bbie.owner_top_level_asbiep_id,
            bbie_id=bbie_id,
            bdt_manifest_id=bdt_manifest_id,
            parent_bbiep_path=bbiep_path,
            owner_top_level_asbiep=owner_top_level_asbiep
        )
        
        # Create a map of dt_sc_manifest_id to cardinality_min
        sc_cardinality_map = {}
        for sc_info in supplementary_components:
            if sc_info.bbie_sc_id is not None:
                sc_cardinality_map[sc_info.based_dt_sc.dt_sc_manifest_id] = sc_info.cardinality_min
        
        # Get all BBIE_SC records for this BBIE
        bbie_sc_list = bie_service.get_bbie_sc_list(bbie_id)
        for bbie_sc in bbie_sc_list:
            if bbie_sc.is_used:
                # Check if this BBIE_SC is required (cardinality_min >= 1)
                cardinality_min = sc_cardinality_map.get(bbie_sc.based_dt_sc_manifest_id, 0)
                if cardinality_min >= 1:
                    logger.info(f"Skipping disable of BBIE_SC {bbie_sc.bbie_sc_id} because it is required (cardinality_min={cardinality_min} >= 1)")
                    continue
                
                try:
                    # Disable the BBIE_SC
                    _, sc_updates = bie_service.update_bbie_sc(
                        bbie_sc_id=bbie_sc.bbie_sc_id,
                        is_used=False
                    )
                    # Track updates
                    if updated_components is not None:
                        updated_components[f"bbie_sc_{bbie_sc.bbie_sc_id}"] = sc_updates
                except Exception as e:
                    logger.warning(f"Failed to disable BBIE_SC {bbie_sc.bbie_sc_id}: {e}")
    except Exception as e:
        logger.warning(f"Failed to retrieve BBIE_SC records for BBIE {bbie_id}: {e}")


def _build_update_role_of_abie_detail(
    bie_service: BusinessInformationEntityService,
    abie_id: int,
    visited_abie_ids: set[int],
    updated_components: dict[str, list[str]] | None = None
) -> UpdateRoleOfAbieDetail | None:
    """
    Build an UpdateRoleOfAbieDetail structure recursively for an ABIE (for update_asbie response).
    Includes all relationships that were updated (created/enabled/disabled).
    
    Args:
        bie_service: The BusinessInformationEntityService instance
        abie_id: The ABIE ID to build structure for
        visited_abie_ids: Set of ABIE IDs already visited (to prevent infinite recursion)
        updated_components: Optional dict to track updated components (key: "asbie_{id}" or "bbie_{id}", value: list of updated fields)
    
    Returns:
        UpdateRoleOfAbieDetail or None if ABIE not found
    """
    # Prevent infinite recursion
    if abie_id in visited_abie_ids:
        return None
    visited_abie_ids.add(abie_id)
    
    # Get the ABIE
    abie = bie_service.get_abie(abie_id)
    if not abie:
        return None
    
    # Get relationships of the ABIE
    relationships = _get_abie_related_components(
        abie.owner_top_level_asbiep_id,
        abie_id,
        abie.based_acc_manifest_id,
        abie.path
    )
    
    relationship_details = []
    
    # Process each relationship - include all that have asbie_id/bbie_id (were created/updated)
    for rel in relationships:
        if isinstance(rel, AsbieRelationshipInfo):
            if rel.asbie_id is not None:
                try:
                    # Get full ASBIE details
                    asbie = bie_service.get_asbie_by_asbie_id(rel.asbie_id)
                    if asbie:
                        # Get updates from tracking dict
                        updates = []
                        if updated_components is not None:
                            updates = updated_components.get(f"asbie_{rel.asbie_id}", [])
                        
                        # Build ASBIE relationship detail with updates
                        asbie_detail = UpdateAsbieRelationshipDetail(
                            asbie_id=rel.asbie_id,
                            updates=updates,
                            guid=asbie.guid,
                            path=rel.path,
                            cardinality_min=rel.cardinality_min,
                            cardinality_max=rel.cardinality_max,
                            based_ascc=rel.based_ascc
                        )
                        
                        # Build recursive structure if ASBIEP exists
                        if asbie.to_asbiep_id:
                            asbiep = bie_service.get_asbiep(asbie.to_asbiep_id)
                            if asbiep and asbiep.role_of_abie_id:
                                role_of_abie_detail = _build_update_role_of_abie_detail(
                                    bie_service=bie_service,
                                    abie_id=asbiep.role_of_abie_id,
                                    visited_abie_ids=visited_abie_ids,
                                    updated_components=updated_components
                                )
                                if role_of_abie_detail:
                                    asbie_detail.asbiep = UpdateAsbiepRelationshipDetail(
                                        asbiep_id=asbie.to_asbiep_id,
                                        updates=[],  # ASBIEP itself is rarely updated
                                        role_of_abie=role_of_abie_detail
                                    )
                        
                        relationship_details.append(UpdateRelationshipDetail(asbie=asbie_detail))
                except Exception as e:
                    logger.warning(f"Failed to build ASBIE detail for {rel.asbie_id}: {e}")
        
        elif isinstance(rel, BbieRelationshipInfo):
            if rel.bbie_id is not None:
                try:
                    # Get full BBIE details
                    bbie = bie_service.get_bbie_by_bbie_id(rel.bbie_id)
                    if bbie:
                        # Get updates from tracking dict
                        updates = []
                        if updated_components is not None:
                            updates = updated_components.get(f"bbie_{rel.bbie_id}", [])
                        
                        # Build BBIE relationship detail with updates
                        bbie_detail = UpdateBbieRelationshipDetail(
                            bbie_id=rel.bbie_id,
                            updates=updates,
                            guid=bbie.guid,
                            path=rel.path,
                            cardinality_min=rel.cardinality_min,
                            cardinality_max=rel.cardinality_max,
                            is_nillable=rel.is_nillable,
                            based_bcc=rel.based_bcc
                        )
                        relationship_details.append(UpdateRelationshipDetail(bbie=bbie_detail))
                except Exception as e:
                    logger.warning(f"Failed to build BBIE detail for {rel.bbie_id}: {e}")
    
    return UpdateRoleOfAbieDetail(
        abie_id=abie_id,
        updates=[],  # ABIE itself is rarely updated
        relationships=relationship_details
    )


def _build_update_bbiep_detail(
    bie_service: BusinessInformationEntityService,
    bbie_id: int,
    updated_components: dict[str, list[str]] | None = None
) -> UpdateBbiepDetail | None:
    """
    Build an UpdateBbiepDetail structure for a BBIE (for update_bbie response).
    Includes all supplementary components that were updated (created/enabled/disabled).
    
    Args:
        bie_service: The BusinessInformationEntityService instance
        bbie_id: The BBIE ID to build structure for
        updated_components: Optional dict to track updated components (key: "bbie_sc_{id}", value: list of updated fields)
    
    Returns:
        UpdateBbiepDetail or None if BBIE not found
    """
    # Get the BBIE
    bbie = bie_service.get_bbie_by_bbie_id(bbie_id)
    if not bbie or not bbie.to_bbiep_id:
        return None
    
    # Get the BBIEP
    bbiep = bie_service.get_bbiep(bbie.to_bbiep_id)
    if not bbiep:
        return None
    
    # Get BCCP manifest to find BDT manifest ID
    cc_service = CoreComponentService()
    bccp_manifest = cc_service.get_bccp_by_manifest_id(bbiep.based_bccp_manifest_id)
    if not bccp_manifest:
        return None
    
    bdt_manifest_id = bccp_manifest.bdt_manifest_id
    
    # Get top-level ASBIEP info
    top_level_asbiep = bie_service.get_top_level_asbiep_by_id(bbie.owner_top_level_asbiep_id)
    if not top_level_asbiep:
        return None
    
    owner_top_level_asbiep = _create_top_level_asbiep_info(top_level_asbiep)
    
    # Calculate BBIEP path for supplementary components
    bbiep_path = f"{bbie.path}>BCCP-{bccp_manifest.bccp_manifest_id}"
    
    # Get supplementary components
    supplementary_components = _get_supplementary_components(
        top_level_asbiep_id=bbie.owner_top_level_asbiep_id,
        bbie_id=bbie_id,
        bdt_manifest_id=bdt_manifest_id,
        parent_bbiep_path=bbiep_path,
        owner_top_level_asbiep=owner_top_level_asbiep
    )
    
    # Get existing BBIE_SC records
    existing_bbie_scs = {}
    try:
        bbie_sc_list = bie_service.get_bbie_sc_list(bbie_id)
        for bbie_sc in bbie_sc_list:
            existing_bbie_scs[bbie_sc.based_dt_sc_manifest_id] = bbie_sc
    except Exception as e:
        logger.warning(f"Failed to retrieve existing BBIE_SC records for BBIE {bbie_id}: {e}")
    
    # Build supplementary components list with updates
    sc_details = []
    for sc_info in supplementary_components:
        if sc_info.bbie_sc_id is not None:
            # Get the actual BBIE_SC
            bbie_sc = existing_bbie_scs.get(sc_info.based_dt_sc.dt_sc_manifest_id)
            if bbie_sc:
                # Get updates from tracking dict
                updates = []
                if updated_components is not None:
                    updates = updated_components.get(f"bbie_sc_{sc_info.bbie_sc_id}", [])
                
                sc_detail = UpdateBbieScDetail(
                    bbie_sc_id=sc_info.bbie_sc_id,
                    updates=updates,
                    guid=bbie_sc.guid,
                    based_dt_sc=sc_info.based_dt_sc,
                    path=sc_info.path,
                    hash_path=sc_info.hash_path,
                    cardinality_min=sc_info.cardinality_min,
                    cardinality_max=sc_info.cardinality_max
                )
                sc_details.append(sc_detail)
    
    # Get BCCP info
    cc_service = CoreComponentService()
    bccp_manifest = cc_service.get_bccp_by_manifest_id(bbiep.based_bccp_manifest_id)
    dt_service = DataTypeService()
    bdt_manifest, _ = dt_service.get_data_type_by_manifest_id(bccp_manifest.bdt_manifest_id)
    bccp_info = BccpInfo(
        bccp_manifest_id=bccp_manifest.bccp_manifest_id,
        bccp_id=bccp_manifest.bccp_id,
        guid=bccp_manifest.bccp.guid,
        den=bccp_manifest.den,
        property_term=bccp_manifest.bccp.property_term,
        representation_term=bccp_manifest.bccp.representation_term,
        definition=bccp_manifest.bccp.definition,
        definition_source=bccp_manifest.bccp.definition_source,
        bdt_manifest=DtInfo(
            dt_manifest_id=bdt_manifest.dt_manifest_id,
            dt_id=bdt_manifest.dt_id,
            guid=bdt_manifest.dt.guid,
            den=bdt_manifest.den,
            data_type_term=bdt_manifest.dt.data_type_term,
            qualifier=bdt_manifest.dt.qualifier,
            representation_term=bdt_manifest.dt.representation_term,
            six_digit_id=bdt_manifest.dt.six_digit_id,
            based_dt_manifest_id=bdt_manifest.based_dt_manifest_id,
            definition=bdt_manifest.dt.definition,
            definition_source=bdt_manifest.dt.definition_source,
            is_deprecated=bdt_manifest.dt.is_deprecated
        ),
        is_deprecated=bccp_manifest.bccp.is_deprecated
    )
    
    return UpdateBbiepDetail(
        bbiep_id=bbiep.bbiep_id,
        updates=[],  # BBIEP itself is rarely updated
        guid=bbiep.guid,
        based_bccp=bccp_info,
        path=bbiep.path or bbiep_path,
        hash_path=bbiep.hash_path or "",
        supplementary_components=sc_details
    )


def _get_abie_related_components(top_level_asbiep_id: int, abie_id: int | None, acc_manifest_id: int, abie_path: str) -> list:
    """
    Get relationships for an ABIE by combining CC associations and BIE associations.

    This function works in two steps:
    1. First, it fetches CC associations from acc_manifest_id recursively
    2. Second, it fetches all BIE associations from asbie/bbie from abie_id using from_abie_id column
    3. If BIE association exists, it's created based on the BIE association (ASBIE or BBIE)
    4. Otherwise, it's created with asbie_id=None or bbie_id=None

    Args:
        abie_id: The ABIE ID to get BIE associations for
        acc_manifest_id: The ACC manifest ID to get CC associations for

    Returns:
        list: List of relationship information objects
    """
    app_user = get_current_user()
    if not app_user:
        raise ToolError("Authentication required")

    cc_service = CoreComponentService()
    bie_service = BusinessInformationEntityService(requester=app_user)
    
    acc_manifest = cc_service.get_acc_by_manifest_id(acc_manifest_id)
    acc_manifest_queue = []
    while acc_manifest:
        acc_manifest_queue = [(abie_path, acc_manifest)] + acc_manifest_queue
        if acc_manifest.based_acc_manifest_id:
            acc_manifest = cc_service.get_acc_by_manifest_id(acc_manifest.based_acc_manifest_id)
            abie_path = f"{abie_path}>ACC-{acc_manifest.acc_manifest_id}"
        else:
            break

    bie_associations = []

    for abie_path, acc_manifest in acc_manifest_queue:
        associations = _get_relationships_for_acc(acc_manifest.acc_manifest_id)
        for cc_assoc in associations:
            if isinstance(cc_assoc, AsccRelationshipInfo):
                asbie_path = f"{abie_path}>ASCC-{cc_assoc.ascc_manifest_id}"

                role_of_acc_manifest = cc_service.get_acc_by_manifest_id(cc_assoc.to_asccp.role_of_acc_manifest_id)
                # Validate that the ACC is not a group type (SemanticGroup or UserExtensionGroup)
                if role_of_acc_manifest.acc.oagis_component_type in [3, 4]:  # 3 = SEMANTIC_GROUP, 4 = USER_EXTENSION_GROUP
                    asbiep_path = f"{asbie_path}>ASCCP-{cc_assoc.to_asccp.asccp_manifest_id}"
                    role_of_abie_path = f"{asbiep_path}>ACC-{cc_assoc.to_asccp.role_of_acc_manifest_id}"

                    group_abie_associations = _get_abie_related_components(
                        top_level_asbiep_id, abie_id, cc_assoc.to_asccp.role_of_acc_manifest_id, role_of_abie_path)

                    bie_associations.extend(group_abie_associations)
                else:
                    # Create AsccInfo from CC association
                    ascc_info = AsccInfo(
                        ascc_manifest_id=cc_assoc.ascc_manifest_id,
                        ascc_id=cc_assoc.ascc_id,
                        guid=cc_assoc.guid,
                        den=cc_assoc.den,
                        cardinality_min=cc_assoc.cardinality_min,
                        cardinality_max=cc_assoc.cardinality_max,
                        is_deprecated=cc_assoc.is_deprecated,
                        definition=cc_assoc.definition,
                        definition_source=cc_assoc.definition_source,
                        from_acc_manifest_id=cc_assoc.from_acc.acc_manifest_id,
                        to_asccp_manifest_id=cc_assoc.to_asccp.asccp_manifest_id
                    )

                    # Create ASBIE association info
                    asbie_assoc = AsbieRelationshipInfo(
                        asbie_id=None,
                        guid=None,
                        based_ascc=ascc_info,
                        to_asbiep_id=None,
                        is_used=False,
                        path=asbie_path,
                        hash_path=hashlib.sha256(asbie_path.encode()).hexdigest(),
                        cardinality_min=cc_assoc.cardinality_min,
                        cardinality_max=cc_assoc.cardinality_max,
                        is_nillable=False,
                        remark=None
                    )

                    bie_associations.append(asbie_assoc)

            elif isinstance(cc_assoc, BccRelationshipInfo):

                # Create BccInfo from CC association
                bcc_info = BccInfo(
                    bcc_manifest_id=cc_assoc.bcc_manifest_id,
                    bcc_id=cc_assoc.bcc_id,
                    guid=cc_assoc.guid,
                    den=cc_assoc.den,
                    cardinality_min=cc_assoc.cardinality_min,
                    cardinality_max=cc_assoc.cardinality_max,
                    entity_type=cc_assoc.entity_type,
                    is_nillable=cc_assoc.is_nillable,
                    is_deprecated=cc_assoc.is_deprecated,
                    definition=cc_assoc.definition,
                    definition_source=cc_assoc.definition_source,
                    from_acc_manifest_id=cc_assoc.from_acc.acc_manifest_id,
                    to_bccp_manifest_id=cc_assoc.to_bccp.bccp_manifest_id
                )

                bbie_path = f"{abie_path}>BCC-{cc_assoc.bcc_manifest_id}"
                # Create BBIE association info
                # Create Facet object if any facet values exist (all None in this case, so no facet)
                facet = None
                
                # Extract default_value and fixed_value from value_constraint if it exists
                default_value = None
                fixed_value = None
                if cc_assoc.value_constraint:
                    default_value = cc_assoc.value_constraint.default_value
                    fixed_value = cc_assoc.value_constraint.fixed_value
                
                # Create ValueConstraint object with validation
                value_constraint = validate_and_create_value_constraint(
                    default_value=default_value,
                    fixed_value=fixed_value
                )
                
                # Try to fetch default primitive restriction from DtAwdPri (is_default=1)
                primitive_restriction = None
                try:
                    xbt_manifest_id, code_list_manifest_id, agency_id_list_manifest_id = bie_service.get_dt_awd_pri(
                        cc_assoc.to_bccp.bccp_manifest_id)
                    # Create PrimitiveRestriction object with validation
                    primitive_restriction = _validate_and_create_primitive_restriction(
                        xbt_manifest_id=xbt_manifest_id,
                        code_list_manifest_id=code_list_manifest_id,
                        agency_id_list_manifest_id=agency_id_list_manifest_id
                    )
                except ValueError as e:
                    # Validation error - log and re-raise
                    logger.error(f"PrimitiveRestriction validation failed for BCC manifest {cc_assoc.bcc_manifest_id}: {e}")
                    raise
                except Exception:
                    # If we can't fetch the default primitive restriction, just leave it as None
                    # This is not a critical error - the BBIE doesn't exist yet anyway
                    pass
                
                # If primitive_restriction is still None, we need to create a minimal one
                # This should not happen in normal cases, but we need to satisfy the model requirement
                if primitive_restriction is None:
                    logger.warning(f"Could not fetch default primitive restriction for BCC manifest {cc_assoc.bcc_manifest_id}. This may indicate a data issue.")
                    # Note: The model requires primitiveRestriction, but we can't create a valid one without data
                    # This is a data integrity issue that should be addressed
                    raise ValueError(
                        f"PrimitiveRestriction is required for BBIE relationship but could not be fetched "
                        f"from DtAwdPri for BCCP manifest {cc_assoc.to_bccp.bccp_manifest_id}. "
                        f"This indicates a data integrity issue."
                    )
                
                bbie_assoc = BbieRelationshipInfo(
                    bbie_id=None,
                    guid=None,
                    based_bcc=bcc_info,
                    to_bbiep_id=None,
                    is_used=False,
                    path=bbie_path,
                    hash_path=hashlib.sha256(bbie_path.encode()).hexdigest(),
                    cardinality_min=cc_assoc.cardinality_min,
                    cardinality_max=cc_assoc.cardinality_max,
                    is_nillable=False,
                    remark=None,
                    primitiveRestriction=primitive_restriction,
                    valueConstraint=value_constraint,
                    facet=facet
                )

                bie_associations.append(bbie_assoc)

    # Only fetch BIE associations if abie_id is provided
    if abie_id is not None:
        asbie_associations = bie_service.get_asbie_list(abie_id)
        asbie_associations_map = {}
        for asbie_assoc in asbie_associations:
            asbie_associations_map[asbie_assoc.hash_path] = asbie_assoc
        bbie_associations = bie_service.get_bbie_list(abie_id)
        bbie_associations_map = {}
        for bbie_assoc in bbie_associations:
            bbie_associations_map[bbie_assoc.hash_path] = bbie_assoc
    else:
        asbie_associations_map = {}
        bbie_associations_map = {}

    bie_attributes = []
    bie_elements = []
    for bie_assoc in bie_associations:
        if isinstance(bie_assoc, AsbieRelationshipInfo):
            asbie = asbie_associations_map.get(bie_assoc.hash_path)
            if asbie:
                bie_assoc.asbie_id = asbie.asbie_id
                bie_assoc.guid = asbie.guid
                bie_assoc.is_used = asbie.is_used
                bie_assoc.cardinality_min = asbie.cardinality_min
                bie_assoc.cardinality_max = asbie.cardinality_max
                bie_assoc.is_nillable = asbie.is_nillable
                bie_assoc.remark = asbie.remark

            bie_elements.append(bie_assoc)

        elif isinstance(bie_assoc, BbieRelationshipInfo):
            bbie = bbie_associations_map.get(bie_assoc.hash_path)
            if bbie:
                bie_assoc.bbie_id = bbie.bbie_id
                bie_assoc.guid = bbie.guid
                bie_assoc.is_used = bbie.is_used
                bie_assoc.cardinality_min = bbie.cardinality_min
                bie_assoc.cardinality_max = bbie.cardinality_max
                bie_assoc.is_nillable = bbie.is_nillable
                bie_assoc.remark = bbie.remark
                
                # Update facet
                if bbie.facet_min_length is not None or bbie.facet_max_length is not None or bbie.facet_pattern is not None:
                    bie_assoc.facet = Facet(
                        facet_min_length=bbie.facet_min_length,
                        facet_max_length=bbie.facet_max_length,
                        facet_pattern=bbie.facet_pattern
                    )
                else:
                    bie_assoc.facet = None
                
                # Update valueConstraint
                # Update valueConstraint with validation
                bie_assoc.valueConstraint = validate_and_create_value_constraint(
                    default_value=bbie.default_value,
                    fixed_value=bbie.fixed_value
                )
                
                # Update primitiveRestriction with validation
                bie_assoc.primitiveRestriction = _validate_and_create_primitive_restriction(
                    xbt_manifest_id=bbie.xbt_manifest_id,
                    code_list_manifest_id=bbie.code_list_manifest_id,
                    agency_id_list_manifest_id=bbie.agency_id_list_manifest_id
                )

            if bie_assoc.based_bcc.entity_type == 'Attribute':
                bie_attributes.append(bie_assoc)
            else:
                bie_elements.append(bie_assoc)

    return bie_attributes + bie_elements


def _sync_version_to_version_identifier_bbie(
    bie_service: BusinessInformationEntityService,
    top_level_asbiep_id: int,
    version: str
) -> None:
    """
    Sync the top_level_asbiep version to the Version Identifier BBIE's fixed_value.
    
    Uses _get_abie_related_components to find the "Version Identifier" relationship.
    If BBIE_ID is None, creates the BBIE first, then syncs the version.
    
    Args:
        bie_service: The BusinessInformationEntityService instance
        top_level_asbiep_id: The top-level ASBIEP ID
        version: The version value to sync
    """
    from tools.core_component import CoreComponentService
    
    # Get the top-level ASBIEP using service method
    top_level_asbiep = bie_service.get_top_level_asbiep_by_id(top_level_asbiep_id)
    if not top_level_asbiep or not top_level_asbiep.asbiep_id:
        return
    
    # Get the ASBIEP and its role_of_abie
    asbiep = bie_service.get_asbiep(top_level_asbiep.asbiep_id)
    if not asbiep or not asbiep.role_of_abie_id:
        return
    
    # Get the ABIE
    abie = bie_service.get_abie(asbiep.role_of_abie_id)
    if not abie:
        return
    
    # Get relationships using _get_abie_related_components
    relationships = _get_abie_related_components(
        top_level_asbiep_id=top_level_asbiep_id,
        abie_id=abie.abie_id,
        acc_manifest_id=abie.based_acc_manifest_id,
        abie_path=abie.path
    )
    
    # Find the Version Identifier BBIE relationship
    version_identifier_rel = None
    cc_service = CoreComponentService()
    
    for rel in relationships:
        if isinstance(rel, BbieRelationshipInfo):
            # Get the BCCP to check property_term
            # based_bcc has to_bccp_manifest_id, so we need to get the BCCP manifest
            bccp_manifest = cc_service.get_bccp_by_manifest_id(rel.based_bcc.to_bccp_manifest_id)
            if bccp_manifest and bccp_manifest.bccp and bccp_manifest.bccp.property_term:
                if bccp_manifest.bccp.property_term.lower() == "version identifier":
                    version_identifier_rel = rel
                    # Get BCC manifest to check if it has fixed_value
                    try:
                        bcc_manifest = cc_service.get_bcc_manifest(rel.based_bcc.bcc_manifest_id)
                        if bcc_manifest and bcc_manifest.bcc:
                            logger.debug(f"Version Identifier BCC found: bcc_manifest_id={rel.based_bcc.bcc_manifest_id}, bcc_fixed_value={bcc_manifest.bcc.fixed_value}")
                    except Exception as e:
                        logger.debug(f"Could not get BCC manifest to check fixed_value: {e}")
                    break
    
    if not version_identifier_rel:
        # Version Identifier BCC doesn't exist in this BIE
        logger.debug(f"Version Identifier BCC not found in top_level_asbiep {top_level_asbiep_id}")
        return
    
    # Check if BBIE exists (bbie_id is not None)
    if version_identifier_rel.bbie_id is not None:
        # BBIE exists - check if fixed_value needs to be updated
        existing_bbie = bie_service.get_bbie_by_bbie_id(version_identifier_rel.bbie_id)
        # Bbie model has fixed_value as a direct attribute, not nested in valueConstraint
        current_fixed_value = existing_bbie.fixed_value if existing_bbie else None
        logger.debug(f"Version Identifier BBIE found: bbie_id={version_identifier_rel.bbie_id}, current_fixed_value={current_fixed_value}, target_version={version}")
        if existing_bbie and current_fixed_value != version:
            # Update its fixed_value using service method
            # Note: This will trigger _sync_version_identifier_bbie_to_version, but since we're syncing
            # from version to BBIE, and the version is already set, it should be fine (no infinite recursion)
            try:
                logger.debug(f"Updating Version Identifier BBIE (ID: {version_identifier_rel.bbie_id}) fixed_value from '{current_fixed_value}' to '{version}'")
                bbie_id_result, updates = bie_service.update_bbie(
                    bbie_id=version_identifier_rel.bbie_id,
                    fixed_value=version
                )
                logger.debug(f"Version Identifier BBIE update completed: bbie_id={bbie_id_result}, updates={updates}")
                if 'fixed_value' not in updates:
                    logger.warning(f"Version Identifier BBIE update did not include 'fixed_value' in updates list. Updates: {updates}")
            except Exception as e:
                logger.warning(f"Failed to update Version Identifier BBIE (ID: {version_identifier_rel.bbie_id}): {e}")
                import traceback
                logger.debug(traceback.format_exc())
        elif existing_bbie:
            logger.debug(f"Version Identifier BBIE fixed_value already matches version: {current_fixed_value} == {version}")
        else:
            logger.warning(f"Version Identifier BBIE (ID: {version_identifier_rel.bbie_id}) not found in database")
    else:
        # BBIE doesn't exist - create it first
        try:
            # Create the BBIE
            bbie_id, _ = bie_service.create_bbie(
                from_abie_id=abie.abie_id,
                based_bcc_manifest_id=version_identifier_rel.based_bcc.bcc_manifest_id,
                bbie_path=version_identifier_rel.path
            )
            
            # Now update its fixed_value to the version using service method
            # Note: This will trigger _sync_version_identifier_bbie_to_version, but since we're syncing
            # from version to BBIE, and the version is already set, it should be fine (no infinite recursion)
            # We pass fixed_value directly to update_bbie, which will update it and trigger the reverse sync
            # But the reverse sync will check if version needs updating, and since it's already set, it won't update again
            # Cache eviction is handled by the service methods (create_bbie and update_bbie)
            bie_service.update_bbie(
                bbie_id=bbie_id,
                fixed_value=version
            )
        except Exception as e:
            logger.warning(f"Failed to create Version Identifier BBIE for top_level_asbiep {top_level_asbiep_id}: {e}")
            import traceback
            logger.debug(traceback.format_exc())


def _sync_version_identifier_bbie_to_version(
    bie_service: BusinessInformationEntityService,
    bbie_id: int,
    fixed_value: str | None
) -> None:
    """
    Sync the Version Identifier BBIE's fixed_value to the top_level_asbiep version.
    
    Uses _get_abie_related_components to verify this is the Version Identifier BBIE.
    
    Args:
        bie_service: The BusinessInformationEntityService instance
        bbie_id: The BBIE ID that was updated
        fixed_value: The fixed_value that was set (or None if cleared)
    """
    from tools.core_component import CoreComponentService
    
    # Get the BBIE
    bbie = bie_service.get_bbie_by_bbie_id(bbie_id)
    if not bbie or not bbie.owner_top_level_asbiep_id:
        return
    
    # Get the ABIE to check if this is in the root ABIE
    from_abie = bie_service.get_abie(bbie.from_abie_id)
    if not from_abie:
        return
    
    # Get relationships to verify this is the Version Identifier BBIE
    relationships = _get_abie_related_components(
        top_level_asbiep_id=bbie.owner_top_level_asbiep_id,
        abie_id=from_abie.abie_id,
        acc_manifest_id=from_abie.based_acc_manifest_id,
        abie_path=from_abie.path
    )
    
    # Find the Version Identifier BBIE relationship and verify it matches
    cc_service = CoreComponentService()
    is_version_identifier = False
    
    for rel in relationships:
        if isinstance(rel, BbieRelationshipInfo) and rel.bbie_id == bbie_id:
            # Get the BCCP to check property_term
            bccp_manifest = cc_service.get_bccp_by_manifest_id(rel.based_bcc.to_bccp_manifest_id)
            if bccp_manifest and bccp_manifest.bccp and bccp_manifest.bccp.property_term:
                if bccp_manifest.bccp.property_term.lower() == "version identifier":
                    is_version_identifier = True
                    break
    
    if not is_version_identifier:
        # This is not the Version Identifier BBIE
        return
    
    # This is the Version Identifier BBIE - sync to top_level_asbiep.version using service method
    # Check if version needs to be updated to prevent infinite recursion
    top_level_asbiep = bie_service.get_top_level_asbiep_by_id(bbie.owner_top_level_asbiep_id)
    if top_level_asbiep and top_level_asbiep.version != fixed_value:
        # Note: This will trigger _sync_version_to_version_identifier_bbie, but since we're syncing
        # from BBIE to version, and the BBIE fixed_value is already set, it should be fine (no infinite recursion)
        bie_service.update_top_level_asbiep(
            top_level_asbiep_id=bbie.owner_top_level_asbiep_id,
            version=fixed_value
        )