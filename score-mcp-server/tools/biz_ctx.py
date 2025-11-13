"""
MCP Tools for managing Business Context operations in connectCenter.

This module provides Model Context Protocol (MCP) tools for creating, updating, deleting,
and querying Business Contexts, which represent specific contextual information that can
be associated with Business Information Entities. Business Contexts serve as containers
for contextual information that specifies the business circumstances under which a Business
Information Entity (BIE) is used. They combine multiple dimensions of context (such as
geographic location, industry sector, business process, or regulatory environment) to create
a complete contextual specification.

Business Contexts enable the contextualization of Core Components by associating specific
contextual values with BIEs, allowing the same Core Component structure to be used in
different business scenarios with appropriate contextual adaptations. Each Business Context
contains multiple Business Context Values, where each value links to a specific Context
Scheme Value (such as "Country: US" or "Industry: Healthcare"). These contexts are then
assigned to Top-Level ASBIEPs (Business Information Entities) to create context-specific
business documents and messages. For example, a purchase order BIE might be associated
with a business context specifying "Country: US, Industry: Retail" to create a US retail
industry-specific purchase order. The tools also manage Business Context Values, which
link Business Contexts to Context Scheme Values. The tools provide a standardized MCP
interface, enabling clients to interact with Business Context data programmatically.

Available Tools:
Business Context Management:
- get_business_contexts: Retrieve paginated lists of Business Contexts with optional
  filters for name and date ranges. Supports custom sorting. All results include full
  relationship loading.

- get_business_context: Retrieve a single Business Context by ID with all relationships
  and values loaded, including nested Context Scheme Value information.

- create_business_context: Create a new Business Context with optional name. Automatically
  generates a GUID and sets creation/update timestamps.

- create_business_context_value: Create a new Business Context Value linking a Business
  Context to a Context Scheme Value. Validates that both the business context and context
  scheme value exist.

- update_business_context: Update an existing Business Context's name. Returns both the
  updated context and the original values for audit purposes.

- update_business_context_value: Update an existing Business Context Value's associated
  Context Scheme Value. Validates that the new context scheme value exists. Returns both
  updated value and original values.

- delete_business_context: Delete a Business Context and all associated Business Context
  Values. Prevents deletion if the context has linked BizCtxAssignment records, providing
  detailed error messages with assignment IDs.

- delete_business_context_value: Delete a Business Context Value. No additional validation
  required as values can be freely removed.

Key Features:
- Full CRUD operations for Business Contexts and Values
- Full relationship loading (creator, last_updater, values with scheme values)
- Support for filtering, pagination, and sorting
- Validation and constraint checking (prevents deletion if linked to assignments)
- Comprehensive error handling and validation
- Structured response models with detailed metadata

The tools provide a clean, consistent interface for accessing Business Context data through the MCP protocol.
All operations require proper authentication and authorization.
"""

import logging
from typing import Annotated

from fastapi import HTTPException
from fastmcp import FastMCP
from fastmcp.exceptions import ToolError
from pydantic import Field

from services import BizCtxService, DateRangeParams, PaginationParams
from tools import _validate_auth_and_db, parse_order_by_to_sorts, _create_user_info
from tools.models.biz_ctx import (
    BizCtxValueInfo,
    CreateBizCtxResponse,
    CreateBizCtxValueResponse,
    CtxSchemeValueInfo,
    DeleteBizCtxResponse,
    DeleteBizCtxValueResponse,
    GetBizCtxResponse,
    GetBizCtxsResponse,
    UpdateBizCtxResponse,
    UpdateBizCtxValueResponse,
)
from tools.models.common import WhoAndWhen
from tools.utils import parse_date_range

# Configure logging
logger = logging.getLogger(__name__)

mcp = FastMCP("Score MCP Server - Business Context Tools")


@mcp.tool(
    name="get_business_contexts",
    description="Get a paginated list of business contexts",
    output_schema={
        "type": "object",
        "description": "Response containing paginated list of business contexts",
        "properties": {
            "total_items": {"type": "integer", "description": "Total number of business contexts available. Allowed values: non-negative integers (≥0).", "example": 25},
            "offset": {"type": "integer", "description": "Offset of the first item in this page. Allowed values: non-negative integers (≥0). Default value: 0.", "example": 0},
            "limit": {"type": "integer", "description": "Number of items returned in this page. Allowed values: integers between 1 and 100 (inclusive). Default value: 10.", "example": 10},
            "items": {
                "type": "array",
                "description": "List of business contexts on this page",
                "items": {
                    "type": "object",
                    "properties": {
                        "biz_ctx_id": {"type": "integer", "description": "Unique identifier for the business context", "example": 123},
                        "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                        "name": {"type": "string", "description": "Short, descriptive name of the business context", "example": "Production Environment"},
                        "values": {
                            "type": "array",
                            "description": "List of business context values",
                            "items": {
                                "type": "object",
                                "properties": {
                                    "biz_ctx_value_id": {"type": "integer", "description": "Unique identifier for the business context value", "example": 1},
                                    "ctx_scheme_value": {
                                        "type": "object",
                                        "description": "Context scheme value information",
                                        "properties": {
                                            "ctx_scheme_value_id": {"type": "integer", "description": "Unique identifier for the context scheme value", "example": 1},
                                            "value": {"type": "string", "description": "Value of the context scheme", "example": "US"}
                                        },
                                        "required": ["ctx_scheme_value_id", "value"]
                                    }
                                },
                                "required": ["biz_ctx_value_id", "ctx_scheme_value"]
                            }
                        },
                        "created": {
                            "type": "object",
                            "description": "Information about the creation of the business context",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who created the business context",
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
                            "description": "Information about the last update of the business context",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who last updated the business context",
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
                    "required": ["biz_ctx_id", "guid", "name", "created", "last_updated"]
                }
            }
        },
        "required": ["total_items", "offset", "limit", "items"]
    }
)
async def get_business_contexts(
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
        name: Annotated[str | None, "Filter by the name of the business context."] = None,
        created_on: Annotated[
            str | None, "Filter by creation date using an inclusive range: '[before~after]'. 'before' and 'after' are date-time strings. Default date format: YYYY-MM-DD. Examples: '[2025-01-01~2025-02-01]'. Either 'before' or 'after' can be omitted, e.g., '[~2025-02-01]' or '[2025-01-01~]'."] = None,
        last_updated_on: Annotated[
            str | None, "Filter by last update date using an inclusive range: '[before~after]'. 'before' and 'after' are date-time strings. Default date format: YYYY-MM-DD. Examples: '[2025-01-01~2025-02-01]'. Either 'before' or 'after' can be omitted, e.g., '[~2025-02-01]' or '[2025-01-01~]'."] = None,
        order_by: Annotated[
            str | None, "Comma-separated list of properties to order results by. Prefix with '-' for descending, '+' for ascending (default ascending). Allowed columns: name, creation_timestamp, last_update_timestamp. Example: '-last_update_timestamp,+name' translates to 'last_update_timestamp DESC, name ASC'."] = None
) -> GetBizCtxsResponse:
    """
    Get a paginated list of business contexts.
    
    This function retrieves business contexts with support for pagination, filtering,
    and sorting. It returns detailed information about each business context including creation
    and update metadata, and associated business context values.
    
    Args:
        offset (int | None, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int | None, optional): The maximum number of items to return. Must be a non-negative number. Defaults to 10.
        name (str | None, optional): Filter by the business context name. Defaults to None.
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
            Allowed columns: name, creation_timestamp, last_update_timestamp. 
            Example: '-last_update_timestamp,+name' translates to 'last_update_timestamp DESC, name ASC'.
            Defaults to None.
    
    Returns:
        GetBizCtxsResponse: Response object containing:
            - total_items: Total number of business contexts available
            - offset: Offset of the first item in this page
            - limit: Number of items returned in this page
            - items: List of business contexts on this page with associated values
    
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
        >>> result = await get_business_contexts(offset=0, limit=10)
        >>> print(f"Found {result.total_items} business contexts")
        
        Filtered search:
        >>> result = await get_business_contexts(
        ...     name="Production",
        ...     created_on="[2023-01-01~2023-12-31]",
        ...     order_by="-last_update_timestamp"
        ... )
        
        Pagination:
        >>> result = await get_business_contexts(offset=20, limit=5)
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Create service instance
    biz_ctx_service = BizCtxService(requester=app_user)

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
                f"Valid columns: {", ".join(biz_ctx_service.allowed_columns_for_order_by)}") from e

    # Get business contexts
    try:
        page = biz_ctx_service.get_biz_ctxs(
            name, created_on_params, last_updated_on_params,
            pagination, sort_list
        )

        return GetBizCtxsResponse(
            total_items=page.total,
            offset=page.offset,
            limit=page.limit,
            items=[_create_biz_ctx_result(biz_ctx) for biz_ctx in page.items]
        )
    except HTTPException as e:
        logger.error(f"HTTP error retrieving business contexts: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving business contexts: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the business contexts: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="get_business_context",
    description="Get a specific business context by ID",
    output_schema={
        "type": "object",
        "description": "Response containing business context information",
        "properties": {
            "biz_ctx_id": {"type": "integer", "description": "Unique identifier for the business context", "example": 123},
            "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
            "name": {"type": "string", "description": "Short, descriptive name of the business context", "example": "Production Environment"},
            "values": {
                "type": "array",
                "description": "List of business context values",
                "items": {
                    "type": "object",
                    "properties": {
                        "biz_ctx_value_id": {"type": "integer", "description": "Unique identifier for the business context value", "example": 1},
                        "ctx_scheme_value": {
                            "type": "object",
                            "description": "Context scheme value information",
                            "properties": {
                                "ctx_scheme_value_id": {"type": "integer", "description": "Unique identifier for the context scheme value", "example": 1},
                                "value": {"type": "string", "description": "Value of the context scheme", "example": "US"}
                            },
                            "required": ["ctx_scheme_value_id", "value"]
                        }
                    },
                    "required": ["biz_ctx_value_id", "ctx_scheme_value"]
                }
            },
            "created": {
                "type": "object",
                "description": "Information about the creation of the business context",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who created the business context",
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
                "description": "Information about the last update of the business context",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who last updated the business context",
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
        "required": ["biz_ctx_id", "guid", "name", "created", "last_updated"]
    }
)
async def get_business_context(
        biz_ctx_id: Annotated[int, Field(
            description="Unique numeric identifier of the business context to retrieve.",
            examples=[123, 456, 789],
            gt=0,
            title="Business Context ID"
        )]
) -> GetBizCtxResponse:
    """
    Get a specific business context by ID.
    
    This function retrieves a single business context from the database and returns
    detailed information including metadata about who created it and when it was
    last updated, along with associated business context values.
    
    Args:
        biz_ctx_id (int): The unique identifier of the business context to fetch
    
    Returns:
        GetBizCtxResponse: Response object containing:
            - biz_ctx_id: Unique identifier for the business context
            - guid: A globally unique identifier (GUID) for the business context
            - name: Short, descriptive name of the business context
            - values: List of associated business context values
            - created: Information about the creation of the business context
            - last_updated: Information about the most recent update to the business context
    
    Raises:
        ToolError: If validation fails, the business context is not found, or other errors occur.
            Common error scenarios include:
            - Invalid business context ID
            - Business context not found (404 error)
            - Database connection issues
            - Authentication failures
    
    Example:
        >>> result = await get_business_context(biz_ctx_id=123)
        >>> print(f"Business Context: {result.name}")
        >>> print(f"Created by: {result.created.who.username}")
        >>> print(f"Values: {len(result.values)}")
        Business Context: Production Environment
        Created by: john_doe
        Values: 3
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Get business context
    try:
        service = BizCtxService(requester=app_user)
        biz_ctx = service.get_biz_ctx(biz_ctx_id)

        return _create_biz_ctx_result(biz_ctx)
    except HTTPException as e:
        logger.error(f"HTTP error retrieving business context: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The business context with ID {biz_ctx_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving business context: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the business context: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="create_business_context",
    description="Create a new business context with a name and optional description",
    output_schema={
        "type": "object",
        "description": "Response containing the created business context ID",
        "properties": {
            "biz_ctx_id": {"type": "integer", "description": "Unique identifier for the created business context", "example": 123}
        },
        "required": ["biz_ctx_id"]
    }
)
async def create_business_context(
        name: Annotated[str, Field(
            description="Human-readable name of the business context to create.",
            examples=["Production Environment", "Test Environment", "Development Environment"],
            min_length=1,
            max_length=100,
            title="Business Context Name"
        )]
) -> CreateBizCtxResponse:
    """
    Create a new business context.
    
    This function creates a new business context with the specified name.
    The business context will be associated with the authenticated user as both
    creator and last updater.
    
    Args:
        name (str): Human-readable name of the business context to create (required).
    
    Returns:
        CreateBizCtxResponse: Response object containing:
            - biz_ctx_id: Unique numeric identifier generated for the newly created business context
    
    Raises:
        ToolError: If validation fails, database errors occur, or unexpected errors happen.
            Common error scenarios include:
            - Invalid input parameters (name too long, etc.)
            - Database connection issues
            - Authentication failures
    
    Example:
        >>> result = await create_business_context(name="Production Environment")
        >>> print(result.biz_ctx_id)
        123
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    logger.info(f"Creating business context with name: {app_user}")

    # Create business context
    try:
        service = BizCtxService(requester=app_user)
        biz_ctx = service.create_biz_ctx(name)

        return CreateBizCtxResponse(biz_ctx_id=biz_ctx.biz_ctx_id)
    except HTTPException as e:
        logger.error(f"HTTP error creating business context: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error creating business context: {e}")
        raise ToolError(
            f"An unexpected error occurred while creating the business context: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="create_business_context_value",
    description="Create a new value for a specific business context, linking it to a context scheme value. Duplicate records (same biz_ctx_id and ctx_scheme_value_id combination) are not allowed.",
    output_schema={
        "type": "object",
        "description": "Response containing the created business context value ID",
        "properties": {
            "biz_ctx_value_id": {"type": "integer", "description": "Unique identifier for the created business context value", "example": 123}
        },
        "required": ["biz_ctx_value_id"]
    }
)
async def create_business_context_value(
        biz_ctx_id: Annotated[int, "Identifier of the business context to which this value will be added."],
        ctx_scheme_value_id: Annotated[int, "Identifier of the context scheme value to associate with this business context."]
) -> CreateBizCtxValueResponse:
    """
    Create a new value for a specific business context, linking it to a context scheme value.
    
    This function creates a new business context value entry under an existing
    business context. The value will be associated with the specified business context
    and context scheme value. Duplicate records (same biz_ctx_id and ctx_scheme_value_id
    combination) are not allowed and will be rejected.
    
    Args:
        biz_ctx_id (int): Identifier of the business context to which this value will be added (required).
        ctx_scheme_value_id (int): Identifier of the context scheme value to associate with this business context (required).
    
    Returns:
        CreateBizCtxValueResponse: Response object containing:
            - biz_ctx_value_id: Unique numeric identifier generated for the newly created business context value
    
    Raises:
        ToolError: If validation fails, duplicate record exists, the business context or context scheme value is not found, or other errors occur.
            Common error scenarios include:
            - Invalid input parameters (invalid IDs, etc.)
            - Duplicate record: A business context value with the same biz_ctx_id and ctx_scheme_value_id already exists (400 error)
            - Business context not found (404 error)
            - Context scheme value not found (404 error)
            - Database connection issues
            - Authentication failures
    
    Example:
        >>> result = await create_business_context_value(
        ...     biz_ctx_id=123,
        ...     ctx_scheme_value_id=456
        ... )
        >>> print(result.biz_ctx_value_id)
        789
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Create business context value
    try:
        service = BizCtxService(requester=app_user)
        biz_ctx_value = service.create_biz_ctx_value(
            biz_ctx_id, ctx_scheme_value_id
        )

        return CreateBizCtxValueResponse(biz_ctx_value_id=biz_ctx_value.biz_ctx_value_id)
    except HTTPException as e:
        logger.error(f"HTTP error creating business context value: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The business context with ID {biz_ctx_id} or context scheme value with ID {ctx_scheme_value_id} was not found. Please check the IDs and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error creating business context value: {e}")
        raise ToolError(
            f"An unexpected error occurred while creating the business context value: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="update_business_context",
    description="Update the name of an existing business context",
    output_schema={
        "type": "object",
        "description": "Response containing the updated business context ID and list of updated fields",
        "properties": {
            "biz_ctx_id": {"type": "integer", "description": "Unique identifier of the updated business context", "example": 123},
            "updates": {"type": "array", "items": {"type": "string"}, "description": "A list of updated fields, each represented by its name", "example": ["name"]}
        },
        "required": ["biz_ctx_id", "updates"]
    }
)
async def update_business_context(
        biz_ctx_id: Annotated[int, "Unique identifier of the business context to update."],
        name: Annotated[str, "New name to assign to the business context."]
) -> UpdateBizCtxResponse:
    """
    Update the name of an existing business context.
    
    This function allows you to modify the name of an existing business context.
    The function tracks what changes were made and returns a summary of the updates.
    
    Args:
        biz_ctx_id (int): Unique identifier of the business context to update (required)
        name (str): New name to assign to the business context (required)
    
    Returns:
        UpdateBizCtxResponse: Response object containing:
            - biz_ctx_id: Unique identifier of the updated business context
            - updates: A list of updated fields, each represented by its name
    
    Raises:
        ToolError: If validation fails, the business context is not found, or other errors occur.
            Common error scenarios include:
            - Invalid input parameters (name too long, etc.)
            - Business context not found (404 error)
            - Database connection issues
            - Authentication failures
    
    Example:
        >>> result = await update_business_context(
        ...     biz_ctx_id=123,
        ...     name="Updated Production Environment"
        ... )
        >>> print(result.updates)
        ['name']
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Update business context
    try:
        service = BizCtxService(requester=app_user)
        biz_ctx, original_values = service.update_biz_ctx(
            biz_ctx_id, name
        )

        # Create update summary
        updates = []
        if original_values["name"] != name:
            updates.append("name")

        return UpdateBizCtxResponse(biz_ctx_id=biz_ctx.biz_ctx_id, updates=updates)
    except HTTPException as e:
        logger.error(f"HTTP error updating business context: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The business context with ID {biz_ctx_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error updating business context: {e}")
        raise ToolError(
            f"An unexpected error occurred while updating the business context: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="update_business_context_value",
    description="Update the linked context scheme value for an existing business context value. Duplicate records (same biz_ctx_id and ctx_scheme_value_id combination) are not allowed.",
    output_schema={
        "type": "object",
        "description": "Response containing the updated business context value ID and list of updated fields",
        "properties": {
            "biz_ctx_value_id": {"type": "integer", "description": "Unique identifier of the updated business context value", "example": 123},
            "updates": {"type": "array", "items": {"type": "string"}, "description": "A list of updated fields, each represented by its name", "example": ["ctx_scheme_value_id"]}
        },
        "required": ["biz_ctx_value_id", "updates"]
    }
)
async def update_business_context_value(
        biz_ctx_value_id: Annotated[int, "Unique identifier of the business context value to update."],
        ctx_scheme_value_id: Annotated[int, "Identifier of the new context scheme value to associate with this business context value."]
) -> UpdateBizCtxValueResponse:
    """
    Update the linked context scheme value for an existing business context value.
    
    This function allows you to modify the linked context scheme value of an existing business context value.
    The function tracks what changes were made and returns a summary of the updates. Duplicate records
    (same biz_ctx_id and ctx_scheme_value_id combination) are not allowed and will be rejected.
    
    Args:
        biz_ctx_value_id (int): Unique identifier of the business context value to update (required)
        ctx_scheme_value_id (int): Identifier of the new context scheme value to associate with this business context value (required)
    
    Returns:
        UpdateBizCtxValueResponse: Response object containing:
            - biz_ctx_value_id: Unique identifier of the updated business context value
            - updates: A list of updated fields, each represented by its name
    
    Raises:
        ToolError: If validation fails, duplicate record exists, the business context value is not found, or other errors occur.
            Common error scenarios include:
            - Invalid input parameters (invalid IDs, etc.)
            - Duplicate record: A business context value with the same biz_ctx_id and ctx_scheme_value_id already exists (400 error)
            - Business context value not found (404 error)
            - Context scheme value not found (404 error)
            - Database connection issues
            - Authentication failures
    
    Example:
        >>> result = await update_business_context_value(
        ...     biz_ctx_value_id=789,
        ...     ctx_scheme_value_id=999
        ... )
        >>> print(result.updates)
        ['ctx_scheme_value_id']
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Update business context value
    try:
        service = BizCtxService(requester=app_user)
        biz_ctx_value, original_values = service.update_biz_ctx_value(
            biz_ctx_value_id, ctx_scheme_value_id
        )

        # Create update summary
        updates = []
        if original_values["ctx_scheme_value_id"] != ctx_scheme_value_id:
            updates.append("ctx_scheme_value_id")

        return UpdateBizCtxValueResponse(biz_ctx_value_id=biz_ctx_value.biz_ctx_value_id, updates=updates)
    except HTTPException as e:
        logger.error(f"HTTP error updating business context value: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The business context value with ID {biz_ctx_value_id} or context scheme value with ID {ctx_scheme_value_id} was not found. Please check the IDs and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error updating business context value: {e}")
        raise ToolError(
            f"An unexpected error occurred while updating the business context value: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="delete_business_context",
    description="Delete a business context and all associated business context values",
    output_schema={
        "type": "object",
        "description": "Response containing the deleted business context ID",
        "properties": {
            "biz_ctx_id": {"type": "integer", "description": "Unique identifier of the deleted business context", "example": 123}
        },
        "required": ["biz_ctx_id"]
    }
)
async def delete_business_context(
        biz_ctx_id: Annotated[int, "Unique identifier of the business context to delete."]
) -> DeleteBizCtxResponse:
    """
    Delete a business context and all associated business context values.
    
    This function permanently removes a business context from the database along with
    all its associated business context values. This action is irreversible and should
    be used with caution. The function will return the ID of the deleted business
    context for confirmation.
    
    Note: This function will check for linked biz_ctx_assignment records before deletion. If any
    biz_ctx_assignment records are linked to this business context, the deletion will be blocked with a
    detailed error message explaining how to resolve the conflict.
    
    Args:
        biz_ctx_id (int): Unique identifier of the business context to delete.
    
    Returns:
        DeleteBizCtxResponse: Response object containing:
            - biz_ctx_id: Unique identifier of the deleted business context
    
    Raises:
        ToolError: If validation fails, the business context is not found, has linked
            biz_ctx_assignment records, or other errors occur. Common error scenarios include:
            - Invalid business context ID
            - Business context not found (404 error)
            - Business context has linked biz_ctx_assignment records (409 conflict error)
            - Database connection issues
            - Authentication failures
    
    Example:
        >>> result = await delete_business_context(biz_ctx_id=123)
        >>> print(f"Deleted business context with ID: {result.biz_ctx_id}")
        Deleted business context with ID: 123
    
    Warning:
        This operation is permanent and cannot be undone. All associated business
        context values will also be permanently deleted. If the business context has
        linked biz_ctx_assignment records, you must delete or update those assignments first.
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Delete business context
    try:
        service = BizCtxService(requester=app_user)
        service.delete_biz_ctx(biz_ctx_id)

        return DeleteBizCtxResponse(biz_ctx_id=biz_ctx_id)
    except HTTPException as e:
        logger.error(f"HTTP error deleting business context: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The business context with ID {biz_ctx_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 409:
            raise ToolError(f"Conflict: {e.detail}") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error deleting business context: {e}")
        raise ToolError(
            f"An unexpected error occurred while deleting the business context: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="delete_business_context_value",
    description="Delete a specific business context value",
    output_schema={
        "type": "object",
        "description": "Response containing the deleted business context value ID",
        "properties": {
            "biz_ctx_value_id": {"type": "integer", "description": "Unique identifier of the deleted business context value", "example": 123}
        },
        "required": ["biz_ctx_value_id"]
    }
)
async def delete_business_context_value(
        biz_ctx_value_id: Annotated[int, "Unique identifier of the business context value to delete."]
) -> DeleteBizCtxValueResponse:
    """
    Delete a specific business context value.
    
    This function permanently removes a business context value from the database. This action
    is irreversible and should be used with caution. The function will return the ID
    of the deleted business context value for confirmation.
    
    Args:
        biz_ctx_value_id (int): Unique identifier of the business context value to delete
    
    Returns:
        DeleteBizCtxValueResponse: Response object containing:
            - biz_ctx_value_id: Unique identifier of the deleted business context value
    
    Raises:
        ToolError: If validation fails, the business context value is not found, or other errors occur.
            Common error scenarios include:
            - Invalid business context value ID
            - Business context value not found (404 error)
            - Database connection issues
            - Authentication failures
    
    Example:
        >>> result = await delete_business_context_value(biz_ctx_value_id=789)
        >>> print(f"Deleted business context value with ID: {result.biz_ctx_value_id}")
        Deleted business context value with ID: 789
    
    Warning:
        This operation is permanent and cannot be undone. Ensure that the business
        context value is not referenced by other entities before deletion.
    """
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Delete business context value
    try:
        service = BizCtxService(requester=app_user)
        service.delete_biz_ctx_value(biz_ctx_value_id)

        return DeleteBizCtxValueResponse(biz_ctx_value_id=biz_ctx_value_id)
    except HTTPException as e:
        logger.error(f"HTTP error deleting business context value: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The business context value with ID {biz_ctx_value_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error deleting business context value: {e}")
        raise ToolError(
            f"An unexpected error occurred while deleting the business context value: {str(e)}. Please contact your system administrator.") from e


# Helper functions (placed after their usage)

def _create_biz_ctx_result(biz_ctx) -> GetBizCtxResponse:
    """
    Create a formatted response object for business context data.

    This helper function transforms a BizCtx database model into a standardized
    response format that includes related entities.
    
    Args:
        biz_ctx: The BizCtx database model instance to format
        
    Returns:
        GetBizCtxResponse: A formatted response object containing:
            - biz_ctx_id: The unique identifier of the business context
            - guid: The globally unique identifier
            - name: Short, descriptive name of the business context
            - values: List of associated business context values
            - created: WhoAndWhen object with creator info and creation timestamp
            - last_updated: WhoAndWhen object with updater info and update timestamp
    """
    # Create business context values info
    values_info = []
    if hasattr(biz_ctx, 'biz_ctx_values') and biz_ctx.biz_ctx_values:
        for biz_ctx_value in biz_ctx.biz_ctx_values:
            values_info.append(BizCtxValueInfo(
                biz_ctx_value_id=biz_ctx_value.biz_ctx_value_id,
                ctx_scheme_value=CtxSchemeValueInfo(
                    ctx_scheme_value_id=biz_ctx_value.ctx_scheme_value.ctx_scheme_value_id,
                    value=biz_ctx_value.ctx_scheme_value.value)
            ))

    
    return GetBizCtxResponse(
        biz_ctx_id=biz_ctx.biz_ctx_id,
        guid=biz_ctx.guid,
        name=biz_ctx.name,
        values=values_info,
        created=WhoAndWhen(
            who=_create_user_info(biz_ctx.creator),
            when=biz_ctx.creation_timestamp
        ),
        last_updated=WhoAndWhen(
            who=_create_user_info(biz_ctx.last_updater),
            when=biz_ctx.last_update_timestamp
        )
    )
