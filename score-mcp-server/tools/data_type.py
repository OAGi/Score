"""
MCP Tools for managing Data Type operations in connectCenter.

This module provides Model Context Protocol (MCP) tools for querying and retrieving
Data Types, which define the structure and constraints for data elements in business
information exchanges. Data Types serve as the foundation for Basic Core Component
Properties (BCCPs), specifying the format, validation rules, and semantic meaning of
simple data values. They follow the UN/CEFACT Core Component Technical Specification
(CCTS) methodology, providing standardized representations for common data patterns such
as dates, amounts, identifiers, and text fields.

Data Types enable consistent data representation across different business contexts by
defining reusable type definitions that can be applied to multiple BCCPs. They include
supplementary components that add additional facets or constraints (such as minimum/maximum
values, patterns, or enumerations) to refine the data type's behavior. Data Types can also
be based on other Data Types, creating inheritance hierarchies that support specialization
and reuse. The tools provide a standardized MCP interface, enabling clients to interact
with Data Type data programmatically, including dependency-aware release queries.

Available Tools:
- get_data_types: Retrieve paginated lists of Data Types filtered by release. Automatically
  includes Data Types from all dependent releases (recursively). Supports optional filters
  for DEN (Dictionary Entry Name) and representation_term, with date range filtering and
  custom sorting.

- get_data_type: Retrieve a single Data Type by its manifest ID, including all related
  information such as namespace, release, library, log, based_dt_manifest, and associated
  supplementary component manifests.

Key Features:
- Full relationship loading (namespace, creator, owner, release, library, log, based_dt_manifest)
- Dependency-aware queries that include data types from dependent releases
- Support for filtering, pagination, and sorting
- Automatic retrieval of associated supplementary component manifests
- Comprehensive error handling and validation
- Structured response models with detailed metadata

The tools provide a clean, consistent interface for accessing Data Type data through the MCP protocol.
All operations require proper authentication and authorization.
"""

import logging
from typing import Annotated

from fastapi import HTTPException
from fastmcp import FastMCP
from fastmcp.exceptions import ToolError
from pydantic import Field

from services import DataTypeService, DateRangeParams, PaginationParams
from tools import _validate_auth_and_db, parse_order_by_to_sorts, _create_user_info
from tools.models.data_type import (
    BaseDataTypeInfo,
    DataTypeSupplementaryComponentInfo,
    GetDataTypeResponse,
    GetDataTypesResponse,
)
from tools.models.common import LibraryInfo, LogInfo, NamespaceInfo, ReleaseInfo, WhoAndWhen
from tools.utils import parse_date_range, validate_and_create_value_constraint

# Configure logging
logger = logging.getLogger(__name__)

mcp = FastMCP("Score MCP Server - Data Type Tools")


@mcp.tool(
    name="get_data_types",
    description="Get a paginated list of data types associated with a specific release",
    output_schema={
        "type": "object",
        "description": "Response containing paginated list of data types",
        "properties": {
            "total_items": {"type": "integer", "description": "Total number of data types available. Allowed values: non-negative integers (≥0).", "example": 50},
            "offset": {"type": "integer", "description": "Offset of the first item in this page. Allowed values: non-negative integers (≥0). Default value: 0.", "example": 0},
            "limit": {"type": "integer", "description": "Number of items returned in this page. Allowed values: integers between 1 and 100 (inclusive). Default value: 10.", "example": 10},
            "items": {
                "type": "array",
                "description": "List of data types on this page",
                "items": {
                    "type": "object",
                    "properties": {
                        "dt_manifest_id": {"type": "integer", "description": "Unique identifier for the data type (DT) manifest", "example": 12345},
                        "dt_id": {"type": "integer", "description": "Unique identifier for the data type (DT)", "example": 6789},
                        "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                        "den": {"type": "string", "description": "Dictionary Entry Name (DEN) - the standardized name of the data type (DT) as defined by CCTS v3, uniquely identifying the data type within its namespace", "example": "Price_ Amount. Type"},
                        "data_type_term": {"type": ["string", "null"], "description": "Data type (DT) term as specified in CCTS v3", "example": "Amount"},
                        "qualifier": {"type": ["string", "null"], "description": "Qualifier for the data type (DT)", "example": "Price"},
                        "representation_term": {"type": ["string", "null"], "description": "Representation term for the data type (DT)", "example": "Amount"},
                        "six_digit_id": {"type": ["string", "null"], "description": "Six-digit identifier for the data type (DT)", "example": "123456"},
                        "definition": {"type": ["string", "null"], "description": "Definition of the data type (DT)", "example": "A number of monetary units specified in a currency where the unit of currency is explicit or implied"},
                        "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://unece.org/trade/uncefact/core-components-data-type-catalogue"},
                        "content_component_definition": {"type": ["string", "null"], "description": "Content component definition", "example": "A numeric value determined by measuring an object along with the specified unit of measure"},
                        "commonly_used": {"type": "boolean", "description": "Whether the data type (DT) is commonly used", "example": False},
                        "is_deprecated": {"type": "boolean", "description": "Whether the data type (DT) is deprecated", "example": False},
                        "state": {"type": ["string", "null"], "description": "State of the data type (DT)", "example": "Published"},
                        "base_dt": {
                            "type": ["object", "null"],
                            "description": "Base data type information if this data type is based on another",
                            "properties": {
                                "dt_manifest_id": {"type": "integer", "description": "Unique identifier for the base data type manifest", "example": 12345},
                                "dt_id": {"type": "integer", "description": "Unique identifier for the base data type", "example": 6789},
                                "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                                "den": {"type": "string", "description": "Dictionary Entry Name (DEN) - the standardized name of the data type (DT) as defined by CCTS v3", "example": "Amount. Type"},
                                "data_type_term": {"type": ["string", "null"], "description": "Data type (DT) term as specified in CCTS v3", "example": "Amount"},
                                "qualifier": {"type": ["string", "null"], "description": "Qualifier for the data type (DT)", "example": "Price"},
                                "representation_term": {"type": ["string", "null"], "description": "Representation term for the data type (DT)", "example": "Amount"},
                                "six_digit_id": {"type": ["string", "null"], "description": "Six-digit identifier for the data type (DT)", "example": "123456"},
                                "definition": {"type": ["string", "null"], "description": "Definition of the data type (DT)", "example": "A number of monetary units specified in a currency where the unit of currency is explicit or implied"},
                                "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://unece.org/trade/uncefact/core-components-data-type-catalogue"},
                                "content_component_definition": {"type": ["string", "null"], "description": "Content component definition", "example": "A numeric value determined by measuring an object along with the specified unit of measure"},
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
                                        "state": {"type": "string", "enum": ["Processing", "Initialized", "Draft", "Published"], "description": "Release state", "example": "Published"}
                                    },
                                    "required": ["release_id", "release_num", "state"]
                                }
                            },
                            "required": ["dt_manifest_id", "dt_id", "guid", "den", "library", "release"]
                        },
                        "supplementary_components": {
                            "type": "array",
                            "description": "List of supplementary components for the data type (DT)",
                            "items": {
                                "type": "object",
                                "properties": {
                                    "dt_sc_manifest_id": {"type": "integer", "description": "Unique identifier for the data type supplementary component manifest", "example": 12345},
                                    "dt_sc_id": {"type": "integer", "description": "Unique identifier for the data type supplementary component", "example": 6789},
                                    "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                                    "object_class_term": {"type": ["string", "null"], "description": "Object class term for the supplementary component", "example": "Amount"},
                                    "property_term": {"type": ["string", "null"], "description": "Property term for the supplementary component", "example": "Format"},
                                    "representation_term": {"type": ["string", "null"], "description": "Representation term for the supplementary component", "example": "Text"},
                                    "definition": {"type": ["string", "null"], "description": "Definition of the supplementary component", "example": "Whether the number is an integer, decimal, real number or percentage"},
                                    "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://unece.org/trade/uncefact/core-components-data-type-catalogue"},
                                    "cardinality_min": {"type": "integer", "description": "Minimum cardinality for the supplementary component", "example": 0},
                                    "cardinality_max": {"type": "integer", "description": "Maximum cardinality for the supplementary component", "example": 1},
                                    "value_constraint": {
                                        "type": ["object", "null"],
                                        "description": "Value constraint (default_value or fixed_value) for the supplementary component. Exactly one of default_value or fixed_value must be set.",
                                        "properties": {
                                            "default_value": {"type": ["string", "null"], "description": "Default value for the supplementary component", "example": "decimal"},
                                            "fixed_value": {"type": ["string", "null"], "description": "Fixed value for the supplementary component", "example": "integer"}
                                        }
                                    },
                                    "is_deprecated": {"type": "boolean", "description": "Whether the supplementary component is deprecated", "example": False}
                                },
                                "required": ["dt_sc_manifest_id", "dt_sc_id", "guid", "cardinality_min", "is_deprecated"]
                            }
                        },
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
                                "state": {"type": "string", "enum": ["Processing", "Initialized", "Draft", "Published"], "description": "Release state", "example": "Published"}
                            },
                            "required": ["release_id", "release_num", "state"]
                        },
                        "log": {
                            "type": ["object", "null"],
                            "description": "Log information",
                            "properties": {
                                "log_id": {"type": "integer", "description": "Unique identifier for the log", "example": 123},
                                "revision_num": {"type": "integer", "description": "Revision number", "example": 1},
                                "revision_tracking_num": {"type": "integer", "description": "Revision tracking number", "example": 1}
                            },
                            "required": ["log_id", "revision_num", "revision_tracking_num"]
                        },
                        "owner": {
                            "type": "object",
                            "description": "User information about the owner of the data type (DT)",
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
                            "description": "Information about the creation of the data type (DT)",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who created the data type (DT)",
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
                            "description": "Information about the last update of the data type (DT)",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who last updated the data type (DT)",
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
                    "required": ["dt_manifest_id", "dt_id", "guid", "den", "library", "release", "commonly_used", "is_deprecated", "supplementary_components", "owner", "created", "last_updated"]
                }
            }
        },
        "required": ["total_items", "offset", "limit", "items"]
    }
)
async def get_data_types(
    release_id: Annotated[int, Field(
        description="Filter by release ID using exact match.",
        examples=[123, 456, 789],
        gt=0,
        title="Release ID"
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
    den: Annotated[str | None, Field(
        description="Filter by Dictionary Entry Name (DEN) using partial match (case-insensitive). DEN format: '((qualifier) ? qualifier + \"_ \" : \"\") + data_type_term + \". Type\"'.",
        examples=["Amount. Type", "Text. Type", "Date. Type"],
        title="Dictionary Entry Name"
    )] = None,
    representation_term: Annotated[str | None, Field(
        description="Filter by representation term using partial match (case-insensitive).",
        examples=["Amount", "Text", "Date"],
        title="Representation Term"
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
        description="Comma-separated list of properties to order results by. Prefix with '-' for descending, '+' for ascending (default ascending). Allowed columns: data_type_term, qualifier, representation_term, six_digit_id, definition, creation_timestamp, last_update_timestamp. Example: '-creation_timestamp,+data_type_term' translates to 'creation_timestamp DESC, data_type_term ASC'.",
        examples=["-creation_timestamp,+data_type_term", "data_type_term", "-last_update_timestamp"],
        title="Order By"
    )] = None
) -> GetDataTypesResponse:
    """
    Get a paginated list of data types associated with a specific release.
    
    This function retrieves data types that are associated with the given release_id. This tool enables
    users to query and manage data types within specific releases, including filtering by DEN (Dictionary
    Entry Name), representation term, and other attributes. It supports pagination, filtering, and sorting.
    The release_id filter is required to ensure you get data types from the correct release context.
    
    The DEN (Dictionary Entry Name) is computed as: ((qualifier) ? qualifier + "_ " : "") + data_type_term + ". Type"
    
    Args:
        release_id (int): Filter by release ID using exact match.
        offset (int | None, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int | None, optional): The maximum number of items to return. Must be a non-negative number. Defaults to 10.
        den (str | None, optional): Filter by Dictionary Entry Name (DEN) using partial match (case-insensitive). 
            DEN format: '((qualifier) ? qualifier + "_ " : "") + data_type_term + ". Type"'. Defaults to None.
        representation_term (str | None, optional): Filter by representation term using partial match (case-insensitive). Defaults to None.
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
            Allowed columns: den, data_type_term, qualifier, representation_term, six_digit_id, definition, creation_timestamp, last_update_timestamp.
            Example: '-creation_timestamp,+den' translates to 'creation_timestamp DESC, den ASC'.
            Defaults to None.
    
    Returns:
        GetDataTypesResponse: Response object containing:
            - total_items: Total number of data types available
            - offset: Offset of the first item in this page
            - limit: Number of items returned in this page
            - items: List of data types on this page with detailed information including:
                - dt_manifest_id: Unique identifier for the data type manifest
                - dt_id: Unique identifier for the data type
                - guid: Unique identifier within the release
                - den: Dictionary Entry Name (computed from qualifier and data_type_term)
                - data_type_term: Data type term
                - qualifier: Qualifier (if any)
                - representation_term: Representation term
                - six_digit_id: Six digit ID
                - definition: Description of the data type
                - definition_source: URL indicating the source of the definition
                - content_component_definition: Description of the content component
                - namespace: Namespace information (if any)
                - library: Library information with library_id and name
                - release: Release information with release_id, release_num, and state
                - log: Log information with log_id, revision_num, and revision_tracking_num (if any)
                - commonly_used: Whether the data type is commonly used
                - is_deprecated: Whether the data type is deprecated
                - state: State of the data type
                - supplementary_components: List of supplementary components with their details
                - owner: User information about the owner of the data type
                - created: Information about the creation of the data type
                - last_updated: Information about the last update of the data type
    
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
        >>> result = get_data_types(release_id=123, offset=0, limit=10)
        >>> print(f"Found {result.total_items} data types")
        
        Filtered search:
        >>> result = get_data_types(release_id=123, den="Amount. Type", limit=5)
        >>> for data_type in result.items:
        ...     print(f"Data type: {data_type.den} (GUID: {data_type.guid})")
        
        Date range filtering:
        >>> result = get_data_types(release_id=123, created_on="[2024-01-01~2024-12-31]")
        >>> print(f"Data types created in 2024: {result.total_items}")
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Create service instance
    data_type_service = DataTypeService()

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
                f"Valid columns: {", ".join(data_type_service.allowed_columns_for_order_by)}") from e

    # Get data types
    try:
        page = data_type_service.get_data_types_by_release(
            release_id=release_id,
            den=den,
            representation_term=representation_term,
            created_on_params=created_on_params,
            last_updated_on_params=last_updated_on_params,
            pagination=pagination,
            sort_list=sort_list
        )

        return GetDataTypesResponse(
            total_items=page.total,
            offset=page.offset,
            limit=page.limit,
            items=[_create_data_type_result(manifest, data_type_service) for manifest in page.items]
        )
    except HTTPException as e:
        logger.error(f"HTTP error retrieving data types: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving data types: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the data types: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="get_data_type",
    description="Get a specific data type by its manifest ID",
    output_schema={
        "type": "object",
        "description": "Response containing data type information",
        "properties": {
            "dt_manifest_id": {"type": "integer", "description": "Unique identifier for the data type (DT) manifest", "example": 12345},
            "dt_id": {"type": "integer", "description": "Unique identifier for the data type (DT)", "example": 6789},
            "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
            "den": {"type": "string", "description": "Dictionary Entry Name (DEN) - the standardized name of the data type (DT) as defined by CCTS v3, uniquely identifying the data type within its namespace", "example": "Price_ Amount. Type"},
            "data_type_term": {"type": ["string", "null"], "description": "Data type (DT) term as specified in CCTS v3", "example": "Amount"},
            "qualifier": {"type": ["string", "null"], "description": "Qualifier for the data type (DT)", "example": "Price"},
            "representation_term": {"type": ["string", "null"], "description": "Representation term for the data type (DT)", "example": "Amount"},
            "six_digit_id": {"type": ["string", "null"], "description": "Six-digit identifier for the data type (DT)", "example": "123456"},
            "definition": {"type": ["string", "null"], "description": "Definition of the data type (DT)", "example": "A number of monetary units specified in a currency where the unit of currency is explicit or implied"},
            "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://unece.org/trade/uncefact/core-components-data-type-catalogue"},
            "content_component_definition": {"type": ["string", "null"], "description": "Content component definition", "example": "A numeric value determined by measuring an object along with the specified unit of measure"},
            "commonly_used": {"type": "boolean", "description": "Whether the data type (DT) is commonly used", "example": False},
            "is_deprecated": {"type": "boolean", "description": "Whether the data type (DT) is deprecated", "example": False},
            "state": {"type": ["string", "null"], "description": "State of the data type (DT)", "example": "Published"},
            "base_dt": {
                "type": ["object", "null"],
                "description": "Base data type information if this data type is based on another",
                "properties": {
                    "dt_manifest_id": {"type": "integer", "description": "Unique identifier for the base data type manifest", "example": 12345},
                    "dt_id": {"type": "integer", "description": "Unique identifier for the base data type", "example": 6789},
                    "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                    "den": {"type": "string", "description": "Dictionary Entry Name (DEN) - the standardized name of the data type (DT) as defined by CCTS v3", "example": "Amount. Type"},
                    "data_type_term": {"type": ["string", "null"], "description": "Data type (DT) term as specified in CCTS v3", "example": "Amount"},
                    "qualifier": {"type": ["string", "null"], "description": "Qualifier for the data type (DT)", "example": "Price"},
                    "representation_term": {"type": ["string", "null"], "description": "Representation term for the data type (DT)", "example": "Amount"},
                    "six_digit_id": {"type": ["string", "null"], "description": "Six-digit identifier for the data type (DT)", "example": "123456"},
                    "definition": {"type": ["string", "null"], "description": "Definition of the data type (DT)", "example": "A number of monetary units specified in a currency where the unit of currency is explicit or implied"},
                    "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://unece.org/trade/uncefact/core-components-data-type-catalogue"},
                    "content_component_definition": {"type": ["string", "null"], "description": "Content component definition", "example": "A numeric value determined by measuring an object along with the specified unit of measure"},
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
                            "state": {"type": "string", "enum": ["Processing", "Initialized", "Draft", "Published"], "description": "Release state", "example": "Published"}
                        },
                        "required": ["release_id", "release_num", "state"]
                    }
                },
                "required": ["dt_manifest_id", "dt_id", "guid", "den", "data_type_term", "qualifier", "representation_term", "six_digit_id", "definition", "definition_source", "content_component_definition", "namespace", "library", "release"]
            },
            "supplementary_components": {
                "type": "array",
                "description": "List of supplementary components for the data type (DT)",
                "items": {
                    "type": "object",
                    "properties": {
                        "dt_sc_manifest_id": {"type": "integer", "description": "Unique identifier for the data type supplementary component manifest", "example": 12345},
                        "dt_sc_id": {"type": "integer", "description": "Unique identifier for the data type supplementary component", "example": 6789},
                        "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                        "object_class_term": {"type": ["string", "null"], "description": "Object class term for the supplementary component", "example": "Amount"},
                        "property_term": {"type": ["string", "null"], "description": "Property term for the supplementary component", "example": "Format"},
                        "representation_term": {"type": ["string", "null"], "description": "Representation term for the supplementary component", "example": "Text"},
                        "definition": {"type": ["string", "null"], "description": "Definition of the supplementary component", "example": "Whether the number is an integer, decimal, real number or percentage"},
                        "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://unece.org/trade/uncefact/core-components-data-type-catalogue"},
                        "cardinality_min": {"type": "integer", "description": "Minimum cardinality for the supplementary component", "example": 0},
                        "cardinality_max": {"type": "integer", "description": "Maximum cardinality for the supplementary component", "example": 1},
                        "value_constraint": {
                            "type": ["object", "null"],
                            "description": "Value constraint (default_value or fixed_value) for the supplementary component. Exactly one of default_value or fixed_value must be set.",
                            "properties": {
                                "default_value": {"type": ["string", "null"], "description": "Default value for the supplementary component", "example": "decimal"},
                                "fixed_value": {"type": ["string", "null"], "description": "Fixed value for the supplementary component", "example": "integer"}
                            }
                        },
                        "is_deprecated": {"type": "boolean", "description": "Whether the supplementary component is deprecated", "example": False}
                    },
                    "required": ["dt_sc_manifest_id", "dt_sc_id", "guid", "object_class_term", "property_term", "representation_term", "definition", "cardinality_min", "cardinality_max", "is_deprecated"]
                }
            },
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
                    "state": {"type": "string", "enum": ["Processing", "Initialized", "Draft", "Published"], "description": "Release state", "example": "Published"}
                },
                "required": ["release_id", "release_num", "state"]
            },
            "log": {
                "type": ["object", "null"],
                "description": "Log information",
                "properties": {
                    "log_id": {"type": "integer", "description": "Unique identifier for the log", "example": 123},
                    "revision_num": {"type": "integer", "description": "Revision number", "example": 1},
                    "revision_tracking_num": {"type": "integer", "description": "Revision tracking number", "example": 1}
                },
                "required": ["log_id", "revision_num", "revision_tracking_num"]
            },
            "owner": {
                "type": "object",
                "description": "User information about the owner of the data type (DT)",
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
                "description": "Information about the creation of the data type (DT)",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who created the data type (DT)",
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
                "description": "Information about the last update of the data type (DT)",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who last updated the data type (DT)",
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
        "required": ["dt_manifest_id", "dt_id", "guid", "den", "data_type_term", "representation_term", "is_deprecated", "state", "library", "release", "owner", "created", "last_updated"]
    }
)
async def get_data_type(
    dt_manifest_id: Annotated[int, Field(
        description="Unique numeric identifier of the data type manifest to retrieve.",
        examples=[123, 456, 789],
        gt=0,
        title="Data Type Manifest ID"
    )]
) -> GetDataTypeResponse:
    """
    Get a specific data type by its manifest ID.
    
    This function retrieves a single data type from the database and returns
    detailed information including metadata about who created it and when it was
    last updated, along with data type-specific attributes, namespace information,
    and associated supplementary components.
    
    Args:
        dt_manifest_id (int): The unique identifier of the data type manifest to fetch
    
    Returns:
        GetDataTypeResponse: Response object containing:
            - dt_manifest_id: Unique identifier for the data type manifest
            - dt_id: Unique identifier for the data type
            - guid: Unique identifier within the release
            - den: Dictionary Entry Name (computed from qualifier and data_type_term)
            - data_type_term: Data type term
            - qualifier: Qualifier (if any)
            - representation_term: Representation term
            - six_digit_id: Six digit ID
            - definition: Description of the data type
            - definition_source: URL indicating the source of the definition
            - content_component_definition: Description of the content component
            - namespace: Namespace information (if any)
            - library: Library information with library_id and name
            - release: Release information with release_id, release_num, and state
            - log: Log information with log_id, revision_num, and revision_tracking_num (if any)
            - commonly_used: Whether the data type is commonly used
            - is_deprecated: Whether the data type is deprecated
            - state: State of the data type
            - base_dt: Base data type information (if this data type is based on another)
            - owner: User information about the owner of the data type
            - supplementary_components: List of supplementary components with their details
            - created: Information about the creation of the data type
            - last_updated: Information about the last update of the data type
    
    Raises:
        ToolError: If validation fails, the data type manifest is not found, or other errors occur.
            Common error scenarios include:
            - Data type manifest with the specified ID does not exist
            - Database connection issues
            - Authentication failures
    
    Examples:
        Get a specific data type:
        >>> result = get_data_type(dt_manifest_id=123)
        >>> print(f"Data type: {result.data_type_term} (GUID: {result.guid})")
        >>> print(f"Owner: {result.owner.username}")
        >>> print(f"Supplementary components: {len(result.supplementary_components)}")
        
        Access supplementary components:
        >>> for sc in result.supplementary_components:
        ...     print(f"  {sc.property_term}: {sc.definition}")
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Get data type
    try:
        service = DataTypeService()
        manifest, sc_manifests = service.get_data_type_by_manifest_id(dt_manifest_id)

        return _create_data_type_result(manifest, service)
    except HTTPException as e:
        logger.error(f"HTTP error retrieving data type: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The data type manifest with ID {dt_manifest_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving data type: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the data type: {str(e)}. Please contact your system administrator.") from e


# Helper functions (placed after their usage)

def _create_data_type_result(manifest, data_type_service) -> GetDataTypeResponse:
    """
    Create a data type result from a DtManifest model instance.
    
    Args:
        manifest: DtManifest model instance with dt relationship
        data_type_service: DataTypeService instance for retrieving related data
        
    Returns:
        GetDataTypeResponse: Formatted data type result
    """
    data_type = manifest.dt
    
    # Get supplementary components using the separate service function
    try:
        sc_manifests = data_type_service.get_supplementary_components_by_dt_manifest_id(manifest.dt_manifest_id)
    except Exception as e:
        logger.warning(f"Failed to retrieve supplementary components for DtManifest {manifest.dt_manifest_id}: {e}")
        sc_manifests = []  # Continue without supplementary components rather than failing completely
    
    # Create namespace info if available
    namespace_info = None
    if data_type.namespace:
        namespace_info = NamespaceInfo(
            namespace_id=data_type.namespace.namespace_id,
            prefix=data_type.namespace.prefix,
            uri=data_type.namespace.uri
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

    # Create supplementary components info from sc manifests
    supplementary_components_info = []
    for sc_manifest in sc_manifests:
        value_constraint = validate_and_create_value_constraint(
            default_value=sc_manifest.dt_sc.default_value,
            fixed_value=sc_manifest.dt_sc.fixed_value
        )
        supplementary_components_info.append(DataTypeSupplementaryComponentInfo(
            dt_sc_manifest_id=sc_manifest.dt_sc_manifest_id,
            dt_sc_id=sc_manifest.dt_sc_id,
            guid=sc_manifest.dt_sc.guid,
            object_class_term=sc_manifest.dt_sc.object_class_term,
            property_term=sc_manifest.dt_sc.property_term,
            representation_term=sc_manifest.dt_sc.representation_term,
            definition=sc_manifest.dt_sc.definition,
            definition_source=sc_manifest.dt_sc.definition_source,
            cardinality_min=sc_manifest.dt_sc.cardinality_min,
            cardinality_max=sc_manifest.dt_sc.cardinality_max,
            value_constraint=value_constraint,
            is_deprecated=sc_manifest.dt_sc.is_deprecated
        ))

    # Create base data type info if available
    base_dt_info = None
    if manifest.based_dt_manifest:
        base_manifest = manifest.based_dt_manifest
        base_data_type = base_manifest.dt
        
        # Create namespace info for base data type if available
        base_namespace_info = None
        if base_data_type.namespace:
            base_namespace_info = NamespaceInfo(
                namespace_id=base_data_type.namespace.namespace_id,
                prefix=base_data_type.namespace.prefix,
                uri=base_data_type.namespace.uri
            )

        # Create library info for base data type from its release
        base_library_info = LibraryInfo(
            library_id=base_manifest.release.library_id,
            name=base_manifest.release.library.name
        )

        # Create release info for base data type
        base_release_info = ReleaseInfo(
            release_id=base_manifest.release_id,
            release_num=base_manifest.release.release_num,
            state=base_manifest.release.state
        )

        base_dt_info = BaseDataTypeInfo(
            dt_manifest_id=base_manifest.dt_manifest_id,
            dt_id=base_data_type.dt_id,
            guid=base_data_type.guid,
            den=base_manifest.den,
            data_type_term=base_data_type.data_type_term,
            qualifier=base_data_type.qualifier,
            representation_term=base_data_type.representation_term,
            six_digit_id=base_data_type.six_digit_id,
            definition=base_data_type.definition,
            definition_source=base_data_type.definition_source,
            content_component_definition=base_data_type.content_component_definition,
            namespace=base_namespace_info,
            library=base_library_info,
            release=base_release_info
        )

    return GetDataTypeResponse(
        dt_manifest_id=manifest.dt_manifest_id,
        dt_id=data_type.dt_id,
        guid=data_type.guid,
        den=manifest.den,
        data_type_term=data_type.data_type_term,
        qualifier=data_type.qualifier,
        representation_term=data_type.representation_term,
        six_digit_id=data_type.six_digit_id,
        definition=data_type.definition,
        definition_source=data_type.definition_source,
        content_component_definition=data_type.content_component_definition,
        namespace=namespace_info,
        library=library_info,
        release=release_info,
        log=log_info,
        commonly_used=data_type.commonly_used,
        is_deprecated=data_type.is_deprecated,
        state=data_type.state,
        base_dt=base_dt_info,
        supplementary_components=supplementary_components_info,
        owner=_create_user_info(data_type.owner),
        created=WhoAndWhen(
            who=_create_user_info(data_type.creator),
            when=data_type.creation_timestamp
        ),
        last_updated=WhoAndWhen(
            who=_create_user_info(data_type.last_updater),
            when=data_type.last_update_timestamp
        )
    )
