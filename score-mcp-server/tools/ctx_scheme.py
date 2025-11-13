"""
MCP Tools for managing Context Scheme operations in connectCenter.

This module provides Model Context Protocol (MCP) tools for creating, updating, deleting,
and querying Context Schemes, which define the structure and values for business contexts.
Context Schemes serve as controlled vocabularies that define the allowed values for specific
dimensions of business context, such as geographic location, industry sector, or business
process. They provide a standardized way to represent contextual information that can be
associated with Business Information Entities (BIEs) to create context-specific business
documents and messages.

Context Schemes enable contextualization of Core Components by defining the dimensions
along which business contexts can vary. Each scheme belongs to a Context Category (such
as "Geographic" or "Industry") and contains multiple Context Scheme Values that represent
the specific options available for that dimension. For example, a "Country Code" scheme
might contain values like "US", "CA", "MX" representing different countries. These schemes
and their values are then used in Business Contexts to specify the contextual information
for a particular BIE. The tools also manage Context Scheme Values, which represent the
actual values that can be used in business contexts. The tools provide a standardized
MCP interface, enabling clients to interact with Context Scheme data programmatically.

Available Tools:
Context Scheme Management:
- get_context_schemes: Retrieve paginated lists of Context Schemes with optional filters
  for scheme_id, scheme_name, description, agency_id, version_id, category_id, category_name,
  and date ranges. Supports custom sorting.

- get_context_scheme: Retrieve a single Context Scheme by ID with all relationships and
  values loaded.

- create_context_scheme: Create a new Context Scheme with scheme_id, scheme_name, and optional attributes
  (description, scheme_agency_id, scheme_version_id, ctx_category_id).
  Automatically generates a GUID.

- create_context_scheme_value: Create a new value for a Context Scheme with value and
  optional meaning. Automatically generates a GUID.

- update_context_scheme: Update an existing Context Scheme. All fields are optional,
  allowing partial updates. Returns both the updated scheme and original values.

- update_context_scheme_value: Update an existing Context Scheme Value's value and/or meaning.
  Returns both updated value and original values.

- delete_context_scheme: Delete a Context Scheme and all associated Context Scheme Values.
  Cascades deletion to all related values.

- delete_context_scheme_value: Delete a Context Scheme Value. Prevents deletion if the value
  is linked to Business Context Values, providing detailed error messages with business
  context IDs.

Key Features:
- Full CRUD operations for Context Schemes and Values
- Full relationship loading (creator, last_updater, category, values)
- Support for filtering, pagination, and sorting
- Validation and constraint checking (prevents deletion if linked to business contexts)
- Comprehensive error handling and validation
- Structured response models with detailed metadata

The tools provide a clean, consistent interface for accessing Context Scheme data through the MCP protocol.
All operations require proper authentication and authorization.
"""

import logging
from typing import Annotated

from fastapi import HTTPException
from fastmcp import FastMCP
from fastmcp.exceptions import ToolError
from pydantic import Field

from services import CtxSchemeService, DateRangeParams, PaginationParams
from tools import _validate_auth_and_db, parse_order_by_to_sorts, _create_user_info
from tools.models.ctx_scheme import (
    CtxCategoryInfo,
    CtxSchemeValueInfo,
    CreateCtxSchemeResponse,
    CreateCtxSchemeValueResponse,
    DeleteCtxSchemeResponse,
    DeleteCtxSchemeValueResponse,
    GetCtxSchemeResponse,
    GetCtxSchemesResponse,
    UpdateCtxSchemeResponse,
    UpdateCtxSchemeValueResponse,
)
from tools.models.common import WhoAndWhen
from tools.utils import parse_date_range

# Configure logging
logger = logging.getLogger(__name__)

mcp = FastMCP("Score MCP Server - Context Scheme Tools")


@mcp.tool(
    name="get_context_schemes",
    description="Get a paginated list of context schemes",
    output_schema={
        "type": "object",
        "description": "Response containing paginated list of context schemes",
        "properties": {
            "total_items": {"type": "integer", "description": "Total number of context schemes available. Allowed values: non-negative integers (≥0).", "example": 25},
            "offset": {"type": "integer", "description": "Offset of the first item in this page. Allowed values: non-negative integers (≥0). Default value: 0.", "example": 0},
            "limit": {"type": "integer", "description": "Number of items returned in this page. Allowed values: integers between 1 and 100 (inclusive). Default value: 10.", "example": 10},
            "items": {
                "type": "array",
                "description": "List of context schemes on this page",
                "items": {
                    "type": "object",
                    "properties": {
                        "ctx_scheme_id": {"type": "integer", "description": "Unique identifier for the context scheme", "example": 123},
                        "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                        "scheme_id": {"type": "string", "description": "External identification of the scheme", "example": "Country"},
                        "scheme_name": {"type": ["string", "null"], "description": "Pretty print name of the context scheme", "example": "Country Code"},
                        "description": {"type": ["string", "null"], "description": "Description of the context scheme", "example": "Standard country codes"},
                        "scheme_agency_id": {"type": "string", "description": "Agency identifier responsible for the scheme", "example": "ISO"},
                        "scheme_version_id": {"type": "string", "description": "Version identifier of the scheme", "example": "1.0"},
                        "ctx_category": {
                            "type": "object",
                            "description": "Associated context category",
                            "properties": {
                                "ctx_category_id": {"type": "integer", "description": "Unique identifier for the context category", "example": 1},
                                "name": {"type": "string", "description": "Name of the context category", "example": "Geographic"}
                            },
                            "required": ["ctx_category_id", "name"]
                        },
                        "values": {
                            "type": "array",
                            "description": "List of context scheme values",
                            "items": {
                                "type": "object",
                                "properties": {
                                    "ctx_scheme_value_id": {"type": "integer", "description": "Unique identifier for the context scheme value", "example": 1},
                                    "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                                    "value": {"type": "string", "description": "Value of the context scheme", "example": "US"},
                                    "meaning": {"type": ["string", "null"], "description": "Meaning of the context scheme value", "example": "United States"}
                                },
                                "required": ["ctx_scheme_value_id", "guid", "value"]
                            }
                        },
                        "created": {
                            "type": "object",
                            "description": "Information about the creation of the context scheme",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who created the context scheme",
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
                            "description": "Information about the last update of the context scheme",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who last updated the context scheme",
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
                    "required": ["ctx_scheme_id", "guid", "scheme_id", "scheme_agency_id", "scheme_version_id", "ctx_category", "created", "last_updated"]
                }
            }
        },
        "required": ["total_items", "offset", "limit", "items"]
    }
)
async def get_context_schemes(
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
        scheme_id: Annotated[str | None, "Filter by the unique scheme identifier."] = None,
        scheme_name: Annotated[str | None, "Filter by the human-readable name of the scheme."] = None,
        scheme_agency_id: Annotated[str | None, "Filter by the agency identifier responsible for the scheme."] = None,
        scheme_version_id: Annotated[str | None, "Filter by the version identifier of the scheme."] = None,
        ctx_category_name: Annotated[str | None, "Filter by the name of the associated context category."] = None,
        description: Annotated[str | None, "Filter by text contained in the scheme description."] = None,
        created_on: Annotated[
            str | None, "Filter by creation date using an inclusive range: '[before~after]'. 'before' and 'after' are date-time strings. Default date format: YYYY-MM-DD. Examples: '[2025-01-01~2025-02-01]'. Either 'before' or 'after' can be omitted, e.g., '[~2025-02-01]' or '[2025-01-01~]'."] = None,
        last_updated_on: Annotated[
            str | None, "Filter by last update date using an inclusive range: '[before~after]'. 'before' and 'after' are date-time strings. Default date format: YYYY-MM-DD. Examples: '[2025-01-01~2025-02-01]'. Either 'before' or 'after' can be omitted, e.g., '[~2025-02-01]' or '[2025-01-01~]'."] = None,
        order_by: Annotated[
            str | None, "Comma-separated list of properties to order results by. Prefix with '-' for descending, '+' for ascending (default ascending). Allowed columns: scheme_id, scheme_name, description, scheme_agency_id, scheme_version_id, creation_timestamp, last_update_timestamp. Example: '-last_update_timestamp,+scheme_id,description' translates to 'last_update_timestamp DESC, scheme_id ASC, description ASC'."] = None
) -> GetCtxSchemesResponse:
    """
    Get a paginated list of context schemes.
    
    This function retrieves context schemes with support for pagination, filtering,
    and sorting. It returns detailed information about each scheme including creation
    and update metadata, associated context categories, and scheme values.
    
    Args:
        offset (int | None, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int | None, optional): The maximum number of items to return. Must be a non-negative number. Defaults to 10.
        scheme_id (str | None, optional): Filter by the unique scheme identifier. Defaults to None.
        scheme_name (str | None, optional): Filter by the human-readable name of the scheme. Defaults to None.
        scheme_agency_id (str | None, optional): Filter by the agency identifier responsible for the scheme. Defaults to None.
        scheme_version_id (str | None, optional): Filter by the version identifier of the scheme. Defaults to None.
        ctx_category_name (str | None, optional): Filter by the name of the associated context category. Defaults to None.
        description (str | None, optional): Filter by text contained in the scheme description. Defaults to None.
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
            Allowed columns: scheme_id, scheme_name, description, scheme_agency_id, scheme_version_id, creation_timestamp, last_update_timestamp.
            Example: '-last_update_timestamp,+scheme_id,description' translates to 'last_update_timestamp DESC, scheme_id ASC, description ASC'.
            Defaults to None.
    
    Returns:
        GetCtxSchemesResponse: Response object containing:
            - total_items: Total number of context schemes available
            - offset: Offset of the first item in this page
            - limit: Number of items returned in this page
            - items: List of context schemes on this page with associated categories and values
    
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
        >>> result = await get_context_schemes(offset=0, limit=10)
        >>> print(f"Found {result.total_items} schemes")
        
        Filtered search:
        >>> result = await get_context_schemes(
        ...     scheme_id="SCHEME",
        ...     ctx_category_name="Business",
        ...     created_on="[2023-01-01~2023-12-31]",
        ...     order_by="-last_update_timestamp"
        ... )
        
        Pagination:
        >>> result = await get_context_schemes(offset=20, limit=5)
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Create service instance
    ctx_scheme_service = CtxSchemeService(requester=app_user)

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
                f"Valid columns: {", ".join(ctx_scheme_service.allowed_columns_for_order_by)}") from e

    # Get context schemes
    try:
        page = ctx_scheme_service.get_ctx_schemes(
            scheme_id, scheme_name, description, scheme_agency_id, scheme_version_id,
            None, created_on_params, last_updated_on_params,
            pagination, sort_list, ctx_category_name
        )

        return GetCtxSchemesResponse(
            total_items=page.total,
            offset=page.offset,
            limit=page.limit,
            items=[_create_ctx_scheme_result(ctx_scheme) for ctx_scheme in page.items]
        )
    except HTTPException as e:
        logger.error(f"HTTP error retrieving context schemes: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving context schemes: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the context schemes: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="get_context_scheme",
    description="Get a specific context scheme by ID",
    output_schema={
        "type": "object",
        "description": "Response containing context scheme information",
        "properties": {
            "ctx_scheme_id": {"type": "integer", "description": "Unique identifier for the context scheme", "example": 123},
            "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
            "scheme_id": {"type": "string", "description": "External identification of the scheme", "example": "Country"},
            "scheme_name": {"type": ["string", "null"], "description": "Pretty print name of the context scheme", "example": "Country Code"},
            "description": {"type": ["string", "null"], "description": "Description of the context scheme", "example": "Standard country codes"},
            "scheme_agency_id": {"type": "string", "description": "Agency identifier responsible for the scheme", "example": "ISO"},
            "scheme_version_id": {"type": "string", "description": "Version identifier of the scheme", "example": "1.0"},
            "ctx_category": {
                "type": ["object", "null"],
                "description": "Associated context category",
                "properties": {
                    "ctx_category_id": {"type": "integer", "description": "Unique identifier for the context category", "example": 1},
                    "name": {"type": "string", "description": "Name of the context category", "example": "Geographic"}
                },
                "required": ["ctx_category_id", "name"]
            },
            "values": {
                "type": "array",
                "description": "List of context scheme values",
                "items": {
                    "type": "object",
                    "properties": {
                        "ctx_scheme_value_id": {"type": "integer", "description": "Unique identifier for the context scheme value", "example": 1},
                        "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                        "value": {"type": "string", "description": "Value of the context scheme", "example": "US"},
                        "meaning": {"type": ["string", "null"], "description": "Meaning of the context scheme value", "example": "United States"}
                    },
                    "required": ["ctx_scheme_value_id", "guid", "value"]
                }
            },
            "created": {
                "type": "object",
                "description": "Information about the creation of the context scheme",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who created the context scheme",
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
                "description": "Information about the last update of the context scheme",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who last updated the context scheme",
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
        "required": ["ctx_scheme_id", "guid", "scheme_id", "scheme_agency_id", "scheme_version_id", "values", "created", "last_updated"]
    }
)
async def get_context_scheme(
        ctx_scheme_id: Annotated[int, Field(
            description="The unique identifier of the context scheme to fetch.",
            examples=[123, 456, 789],
            gt=0,
            title="Context Scheme ID"
        )]
) -> GetCtxSchemeResponse:
    """
    Get a specific context scheme by ID.
    
    This function retrieves a single context scheme from the database and returns
    detailed information including metadata about who created it and when it was
    last updated, along with associated context categories and scheme values.
    
    Args:
        ctx_scheme_id (int): The unique identifier of the context scheme to fetch
    
    Returns:
        GetCtxSchemeResponse: Response object containing:
            - ctx_scheme_id: Unique identifier for the context scheme
            - guid: A globally unique identifier (GUID) for the context scheme
            - scheme_id: External identification of the scheme
            - scheme_name: Pretty print name of the context scheme
            - description: Description of the context scheme
            - scheme_agency_id: Identification of the agency maintaining the scheme
            - scheme_version_id: Version number of the context scheme
            - ctx_category: List of associated context categories
            - values: List of associated context scheme values
            - created: Information about the creation of the context scheme
            - last_updated: Information about the most recent update to the context scheme
    
    Raises:
        ToolError: If validation fails, the context scheme is not found, or other errors occur.
            Common error scenarios include:
            - Invalid context scheme ID
            - Context scheme not found (404 error)
            - Database connection issues
            - Authentication failures
    
    Example:
        >>> result = await get_context_scheme(ctx_scheme_id=123)
        >>> print(f"Scheme: {result.scheme_name}")
        >>> print(f"Created by: {result.created.who.username}")
        >>> print(f"Categories: {len(result.ctx_category)}")
        >>> print(f"Values: {len(result.values)}")
        Scheme: Business Context Scheme
        Created by: john_doe
        Categories: 1
        Values: 3
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Get context scheme
    try:
        service = CtxSchemeService(requester=app_user)
        ctx_scheme = service.get_ctx_scheme(ctx_scheme_id)

        return _create_ctx_scheme_result(ctx_scheme)
    except HTTPException as e:
        logger.error(f"HTTP error retrieving context scheme: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The context scheme with ID {ctx_scheme_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving context scheme: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the context scheme: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="create_context_scheme",
    description="Create a new context scheme entry",
    output_schema={
        "type": "object",
        "description": "Response containing the created context scheme ID",
        "properties": {
            "ctx_scheme_id": {"type": "integer", "description": "Unique identifier for the created context scheme", "example": 123}
        },
        "required": ["ctx_scheme_id"]
    }
)
async def create_context_scheme(
        ctx_category_id: Annotated[int, Field(
            description="Identifier of the context category this scheme belongs to.",
            examples=[123, 456, 789],
            gt=0,
            title="Context Category ID"
        )],
        scheme_id: Annotated[str, Field(
            description="Identifier string for the new context scheme.",
            examples=["Country", "Currency", "Language"],
            min_length=1,
            max_length=100,
            title="Scheme ID"
        )],
        scheme_agency_id: Annotated[
            str, "Identifier string representing the issuing agency or organization responsible for the scheme."],
        scheme_version_id: Annotated[str, "Version identifier string to specify the version of the context scheme."],
        scheme_name: Annotated[str, Field(
            description="Human-readable display name for the new context scheme.",
            examples=["Country Code", "Currency Code", "Language Code"],
            min_length=1,
            max_length=255,
            title="Scheme Name"
        )],
        description: Annotated[str | None, "Optional text describing the purpose or usage of the scheme."] = None
) -> CreateCtxSchemeResponse:
    """
    Create a new context scheme entry.
    
    This function creates a new context scheme with the specified required parameters.
    The context scheme will be associated with the authenticated user as both
    creator and last updater.
    
    Args:
        ctx_category_id (int): Identifier of the context category this scheme belongs to (required).
        scheme_id (str): Identifier string for the new context scheme (required).
        scheme_name (str): Human-readable display name for the new context scheme (required).
        scheme_agency_id (str): Identifier string representing the issuing agency or organization responsible for the scheme (required).
        scheme_version_id (str): Version identifier string to specify the version of the context scheme (required).
        description (str | None, optional): Optional text describing the purpose or usage of the scheme.
    
    Returns:
        CreateCtxSchemeResponse: Response object containing:
            - ctx_scheme_id: Unique numeric identifier generated for the newly created context scheme
    
    Raises:
        ToolError: If validation fails, database errors occur, or unexpected errors happen.
            Common error scenarios include:
            - Invalid input parameters (scheme_id too long, empty scheme_id, etc.)
            - Database connection issues
            - Authentication failures
    
    Example:
        >>> result = await create_context_scheme(
        ...     ctx_category_id=1,
        ...     scheme_id="SCHEME001",
        ...     scheme_name="Business Context Scheme",
        ...     scheme_agency_id="AGENCY001",
        ...     scheme_version_id="1.0",
        ...     description="A scheme for business-related contexts"
        ... )
        >>> print(result.ctx_scheme_id)
        123
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Create context scheme
    try:
        service = CtxSchemeService(requester=app_user)
        ctx_scheme = service.create_ctx_scheme(
            scheme_id, scheme_name, description,
            scheme_agency_id, scheme_version_id, ctx_category_id
        )

        return CreateCtxSchemeResponse(ctx_scheme_id=ctx_scheme.ctx_scheme_id)
    except HTTPException as e:
        logger.error(f"HTTP error creating context scheme: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error creating context scheme: {e}")
        raise ToolError(
            f"An unexpected error occurred while creating the context scheme: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="create_context_scheme_value",
    description="Create a new value entry under an existing context scheme",
    output_schema={
        "type": "object",
        "description": "Response containing the created context scheme value ID",
        "properties": {
            "ctx_scheme_value_id": {"type": "integer", "description": "Unique identifier for the created context scheme value", "example": 123}
        },
        "required": ["ctx_scheme_value_id"]
    }
)
async def create_context_scheme_value(
        ctx_scheme_id: Annotated[int, "Identifier of the context scheme to which this value belongs."],
        value: Annotated[str, "The actual value string to be added to the context scheme."],
        meaning: Annotated[
            str | None, "Optional descriptive meaning or human-readable explanation of the value."] = None
) -> CreateCtxSchemeValueResponse:
    """
    Create a new value entry under an existing context scheme.
    
    This function creates a new context scheme value entry under an existing
    context scheme. The value will be associated with the specified context scheme.
    
    Args:
        ctx_scheme_id (int): Identifier of the context scheme to which this value belongs (required).
        value (str): The actual value string to be added to the context scheme (required).
        meaning (str | None, optional): Optional descriptive meaning or human-readable explanation of the value.
    
    Returns:
        CreateCtxSchemeValueResponse: Response object containing:
            - ctx_scheme_value_id: Unique numeric identifier generated for the newly created context scheme value
    
    Raises:
        ToolError: If validation fails, the context scheme is not found, or other errors occur.
            Common error scenarios include:
            - Invalid input parameters (value too long, empty value, etc.)
            - Context scheme not found (404 error)
            - Database connection issues
            - Authentication failures
    
    Example:
        >>> result = await create_context_scheme_value(
        ...     ctx_scheme_id=123,
        ...     value="ACTIVE",
        ...     meaning="Indicates an active status"
        ... )
        >>> print(result.ctx_scheme_value_id)
        456
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Create context scheme value
    try:
        service = CtxSchemeService(requester=app_user)
        ctx_scheme_value = service.create_ctx_scheme_value(
            ctx_scheme_id, value, meaning
        )

        return CreateCtxSchemeValueResponse(ctx_scheme_value_id=ctx_scheme_value.ctx_scheme_value_id)
    except HTTPException as e:
        logger.error(f"HTTP error creating context scheme value: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The context scheme with ID {ctx_scheme_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error creating context scheme value: {e}")
        raise ToolError(
            f"An unexpected error occurred while creating the context scheme value: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="update_context_scheme_value",
    description="Update properties of an existing context scheme value entry",
    output_schema={
        "type": "object",
        "description": "Response containing the updated context scheme value ID and list of updated fields",
        "properties": {
            "ctx_scheme_value_id": {"type": "integer", "description": "Unique identifier of the updated context scheme value", "example": 123},
            "updates": {"type": "array", "items": {"type": "string"}, "description": "A list of updated fields, each represented by its name", "example": ["value", "meaning"]}
        },
        "required": ["ctx_scheme_value_id", "updates"]
    }
)
async def update_context_scheme_value(
        ctx_scheme_value_id: Annotated[int, "Unique identifier of the context scheme value to update."],
        value: Annotated[str | None, "Optional updated value string for this context scheme entry."] = None,
        meaning: Annotated[
            str | None, "Optional updated human-readable meaning or description associated with the value."] = None
) -> UpdateCtxSchemeValueResponse:
    """
    Update properties of an existing context scheme value entry.
    
    This function allows you to modify the properties of an existing context scheme value.
    At least one of value or meaning must be provided. Only the fields you provide will be updated.
    The function tracks what changes were made and returns a summary of the updates.
    
    Args:
        ctx_scheme_value_id (int): Unique identifier of the context scheme value to update (required)
        value (str | None, optional): Optional updated value string for this context scheme entry
        meaning (str | None, optional): Optional updated human-readable meaning or description associated with the value
    
    Returns:
        UpdateCtxSchemeValueResponse: Response object containing:
            - ctx_scheme_value_id: Unique identifier of the updated context scheme value
            - updates: A list of updated fields, each represented by its name
    
    Raises:
        ToolError: If validation fails, the context scheme value is not found, or other errors occur.
            Common error scenarios include:
            - Invalid input parameters (value too long, empty value, etc.)
            - Neither value nor meaning provided
            - Context scheme value not found (404 error)
            - Database connection issues
            - Authentication failures
    
    Example:
        >>> result = await update_context_scheme_value(
        ...     ctx_scheme_value_id=456,
        ...     value="INACTIVE",
        ...     meaning="Indicates an inactive status"
        ... )
        >>> print(result.updates)
        ['value', 'meaning']
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Update context scheme value
    try:
        service = CtxSchemeService(requester=app_user)
        ctx_scheme_value, original_values = service.update_ctx_scheme_value(
            ctx_scheme_value_id, value, meaning
        )

        # Create update summary
        updates = []
        if value is not None and original_values["value"] != value:
            updates.append("value")
        if meaning is not None and original_values["meaning"] != meaning:
            updates.append("meaning")

        return UpdateCtxSchemeValueResponse(ctx_scheme_value_id=ctx_scheme_value.ctx_scheme_value_id, updates=updates)
    except HTTPException as e:
        logger.error(f"HTTP error updating context scheme value: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The context scheme value with ID {ctx_scheme_value_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error updating context scheme value: {e}")
        raise ToolError(
            f"An unexpected error occurred while updating the context scheme value: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="delete_context_scheme_value",
    description="Delete a specific context scheme value",
    output_schema={
        "type": "object",
        "description": "Response containing the deleted context scheme value ID",
        "properties": {
            "ctx_scheme_value_id": {"type": "integer", "description": "Unique identifier of the deleted context scheme value", "example": 123}
        },
        "required": ["ctx_scheme_value_id"]
    }
)
async def delete_context_scheme_value(
        ctx_scheme_value_id: Annotated[int, "Unique identifier of the context scheme value to delete."]
) -> DeleteCtxSchemeValueResponse:
    """
    Delete a specific context scheme value.
    
    This function permanently removes a context scheme value from the database. This action
    is irreversible and should be used with caution. The function will return the ID
    of the deleted context scheme value for confirmation.
    
    Args:
        ctx_scheme_value_id (int): Unique identifier of the context scheme value to delete
    
    Returns:
        DeleteCtxSchemeValueResponse: Response object containing:
            - ctx_scheme_value_id: Unique identifier of the deleted context scheme value
    
    Raises:
        ToolError: If validation fails, the context scheme value is not found, or other errors occur.
            Common error scenarios include:
            - Invalid context scheme value ID
            - Context scheme value not found (404 error)
            - Context scheme value is linked to business context values (409 error)
            - Database connection issues
            - Authentication failures
    
    Example:
        >>> result = await delete_context_scheme_value(ctx_scheme_value_id=456)
        >>> print(f"Deleted context scheme value with ID: {result.ctx_scheme_value_id}")
        Deleted context scheme value with ID: 456
    
    Warning:
        This operation is permanent and cannot be undone. Ensure that the context
        scheme value is not referenced by other entities before deletion.
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Delete context scheme value
    try:
        service = CtxSchemeService(requester=app_user)
        service.delete_ctx_scheme_value(ctx_scheme_value_id)

        return DeleteCtxSchemeValueResponse(ctx_scheme_value_id=ctx_scheme_value_id)
    except HTTPException as e:
        logger.error(f"HTTP error deleting context scheme value: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The context scheme value with ID {ctx_scheme_value_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 409:
            raise ToolError(f"Conflict error: {e.detail}") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error deleting context scheme value: {e}")
        raise ToolError(
            f"An unexpected error occurred while deleting the context scheme value: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="update_context_scheme",
    description="Update properties of an existing context scheme",
    output_schema={
        "type": "object",
        "description": "Response containing the updated context scheme ID and list of updated fields",
        "properties": {
            "ctx_scheme_id": {"type": "integer", "description": "Unique identifier of the updated context scheme", "example": 123},
            "updates": {"type": "array", "items": {"type": "string"}, "description": "A list of updated fields, each represented by its name", "example": ["scheme_id", "scheme_name", "description"]}
        },
        "required": ["ctx_scheme_id", "updates"]
    }
)
async def update_context_scheme(
        ctx_scheme_id: Annotated[int, "Unique identifier of the context scheme to update."],
        ctx_category_id: Annotated[
            int | None, "Optional updated identifier of the context category this scheme belongs to."] = None,
        scheme_id: Annotated[str | None, "Optional updated unique identifier string for the context scheme."] = None,
        scheme_name: Annotated[
            str | None, "Optional updated human-readable display name for the context scheme."] = None,
        scheme_agency_id: Annotated[
            str | None, "Optional updated identifier string for the issuing agency or organization."] = None,
        scheme_version_id: Annotated[
            str | None, "Optional updated version identifier string for the context scheme."] = None,
        description: Annotated[
            str | None, "Optional updated text describing the purpose or usage of the scheme."] = None
) -> UpdateCtxSchemeResponse:
    """
    Update properties of an existing context scheme.
    
    This function allows you to modify the properties of an existing context scheme.
    At least one field must be provided. Only the fields you provide will be updated.
    The function tracks what changes were made and returns a summary of the updates.
    
    Args:
        ctx_scheme_id (int): Unique identifier of the context scheme to update (required)
        ctx_category_id (int | None, optional): Optional updated identifier of the context category this scheme belongs to
        scheme_id (str | None, optional): Optional updated unique identifier string for the context scheme
        scheme_name (str | None, optional): Optional updated human-readable display name for the context scheme
        scheme_agency_id (str | None, optional): Optional updated identifier string for the issuing agency or organization
        scheme_version_id (str | None, optional): Optional updated version identifier string for the context scheme
        description (str | None, optional): Optional updated text describing the purpose or usage of the scheme
    
    Returns:
        UpdateCtxSchemeResponse: Response object containing:
            - ctx_scheme_id: Unique identifier of the updated context scheme
            - updates: A list of updated fields, each represented by its name
    
    Raises:
        ToolError: If validation fails, the context scheme is not found, or other errors occur.
            Common error scenarios include:
            - Invalid input parameters (scheme_id too long, empty scheme_id, etc.)
            - No fields provided for update
            - Context scheme not found (404 error)
            - Database connection issues
            - Authentication failures
    
    Example:
        >>> result = await update_context_scheme(
        ...     ctx_scheme_id=123,
        ...     scheme_name="Updated Business Context Scheme",
        ...     description="Updated description"
        ... )
        >>> print(result.updates)
        ['scheme_name', 'description']
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Update context scheme
    try:
        service = CtxSchemeService(requester=app_user)
        ctx_scheme, original_values = service.update_ctx_scheme(
            ctx_scheme_id, scheme_id, scheme_name, description,
            scheme_agency_id, scheme_version_id, ctx_category_id
        )

        # Create update summary
        updates = []
        if scheme_id is not None and original_values["scheme_id"] != scheme_id:
            updates.append("scheme_id")
        if scheme_name is not None and original_values["scheme_name"] != scheme_name:
            updates.append("scheme_name")
        if description is not None and original_values["description"] != description:
            updates.append("description")
        if scheme_agency_id is not None and original_values["scheme_agency_id"] != scheme_agency_id:
            updates.append("scheme_agency_id")
        if scheme_version_id is not None and original_values["scheme_version_id"] != scheme_version_id:
            updates.append("scheme_version_id")
        if ctx_category_id is not None and original_values["ctx_category_id"] != ctx_category_id:
            updates.append("ctx_category_id")

        return UpdateCtxSchemeResponse(ctx_scheme_id=ctx_scheme.ctx_scheme_id, updates=updates)
    except HTTPException as e:
        logger.error(f"HTTP error updating context scheme: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The context scheme with ID {ctx_scheme_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error updating context scheme: {e}")
        raise ToolError(
            f"An unexpected error occurred while updating the context scheme: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="delete_context_scheme",
    description="Delete a context scheme and all associated context scheme values",
    output_schema={
        "type": "object",
        "description": "Response containing the deleted context scheme ID",
        "properties": {
            "ctx_scheme_id": {"type": "integer", "description": "Unique identifier of the deleted context scheme", "example": 123}
        },
        "required": ["ctx_scheme_id"]
    }
)
async def delete_context_scheme(
        ctx_scheme_id: Annotated[int, "Unique identifier of the context scheme to delete."]
) -> DeleteCtxSchemeResponse:
    """
    Delete a context scheme and all associated context scheme values.
    
    This function permanently removes a context scheme from the database along with
    all its associated context scheme values. This action is irreversible and should
    be used with caution. The function will return the ID of the deleted context
    scheme for confirmation.
    
    Args:
        ctx_scheme_id (int): Unique identifier of the context scheme to delete.
    
    Returns:
        DeleteCtxSchemeResponse: Response object containing:
            - ctx_scheme_id: Unique identifier of the deleted context scheme
    
    Raises:
        ToolError: If validation fails, the context scheme is not found, or other errors occur.
            Common error scenarios include:
            - Invalid context scheme ID
            - Context scheme not found (404 error)
            - Database connection issues
            - Authentication failures
            - Referential integrity constraints (if the scheme is referenced elsewhere)
    
    Example:
        >>> result = await delete_context_scheme(ctx_scheme_id=123)
        >>> print(f"Deleted context scheme with ID: {result.ctx_scheme_id}")
        Deleted context scheme with ID: 123
    
    Warning:
        This operation is permanent and cannot be undone. All associated context
        scheme values will also be permanently deleted.
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Delete context scheme
    try:
        service = CtxSchemeService(requester=app_user)
        service.delete_ctx_scheme(ctx_scheme_id)

        return DeleteCtxSchemeResponse(ctx_scheme_id=ctx_scheme_id)
    except HTTPException as e:
        logger.error(f"HTTP error deleting context scheme: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The context scheme with ID {ctx_scheme_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error deleting context scheme: {e}")
        raise ToolError(
            f"An unexpected error occurred while deleting the context scheme: {str(e)}. Please contact your system administrator.") from e


# Helper functions (placed after their usage)

def _create_ctx_scheme_result(ctx_scheme) -> GetCtxSchemeResponse:
    """
    Create a formatted response object for context scheme data.

    This helper function transforms a CtxScheme database model into a standardized
    response format that includes related entities.
    
    Args:
        ctx_scheme: The CtxScheme database model instance to format
        
    Returns:
        GetCtxSchemeResponse: A formatted response object containing:
            - ctx_scheme_id: The unique identifier of the context scheme
            - guid: The globally unique identifier
            - scheme_id: External identification of the scheme
            - scheme_name: Pretty print name of the context scheme
            - description: Description of the context scheme
            - scheme_agency_id: Identification of the agency maintaining the scheme
            - scheme_version_id: Version number of the context scheme
            - ctx_category: Associated context category
            - values: List of associated context scheme values
            - created: WhoAndWhen object with creator info and creation timestamp
            - last_updated: WhoAndWhen object with updater info and update timestamp
    """
    # Create context category info
    ctx_category_info = None
    if ctx_scheme.ctx_category:
        ctx_category_info = CtxCategoryInfo(
            ctx_category_id=ctx_scheme.ctx_category.ctx_category_id,
            name=ctx_scheme.ctx_category.name
        )

    # Create context scheme values info
    values_info = []
    if hasattr(ctx_scheme, 'ctx_scheme_values') and ctx_scheme.ctx_scheme_values:
        for value in ctx_scheme.ctx_scheme_values:
            values_info.append(CtxSchemeValueInfo(
                ctx_scheme_value_id=value.ctx_scheme_value_id,
                guid=value.guid,
                value=value.value,
                meaning=value.meaning
            ))

    return GetCtxSchemeResponse(
        ctx_scheme_id=ctx_scheme.ctx_scheme_id,
        guid=ctx_scheme.guid,
        scheme_id=ctx_scheme.scheme_id,
        scheme_name=ctx_scheme.scheme_name,
        description=ctx_scheme.description,
        scheme_agency_id=ctx_scheme.scheme_agency_id,
        scheme_version_id=ctx_scheme.scheme_version_id,
        ctx_category=ctx_category_info,
        values=values_info,
        created=WhoAndWhen(
            who=_create_user_info(ctx_scheme.creator),
            when=ctx_scheme.creation_timestamp
        ),
        last_updated=WhoAndWhen(
            who=_create_user_info(ctx_scheme.last_updater),
            when=ctx_scheme.last_update_timestamp
        )
    )
