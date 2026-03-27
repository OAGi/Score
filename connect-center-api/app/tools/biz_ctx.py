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
from app.services.biz_ctx_service import BizCtxService
from app.services.ctx_scheme_service import CtxSchemeService
from app.tools import _to_tool_error, get_tool_authenticated_user, tool_session
from app.tools.models.biz_ctx import (
    BizCtxResponseEntry,
    CreateBizCtxResponse,
    CreateBizCtxValueResponse,
    DeleteBizCtxResponse,
    DeleteBizCtxValueResponse,
    GetBizCtxPaginationResponse,
    GetBizCtxResponse,
    UpdateBizCtxResponse,
    UpdateBizCtxValueResponse,
)
from app.types.unset import UNSET

# Configure logging.
logger = logging.getLogger("connectcenter.mcp.biz_ctx")

mcp = FastMCP("connectCenter MCP - Business Context Tools")


async def get_biz_ctx_service(
    session: AsyncSession = Depends(tool_session),
) -> BizCtxService:
    """Provide a requester-scoped business-context service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    return _build_biz_ctx_service(session, requester)


async def get_ctx_scheme_service(
    session: AsyncSession = Depends(tool_session),
) -> CtxSchemeService:
    """Provide a requester-scoped context-scheme service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    return _build_ctx_scheme_service(session, requester)


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
    name: Annotated[str | None, Field(default=None, description="Filter by the name of the business context.")],
    created_on: Annotated[
        str | None,
        Field(
            default=None,
            description="Filter by creation date using an inclusive range: '[before~after]'. 'before' and 'after' are date-time strings. Default date format: YYYY-MM-DD. Examples: '[2025-01-01~2025-02-01]'. Either 'before' or 'after' can be omitted, e.g., '[~2025-02-01]' or '[2025-01-01~]'.",
        ),
    ],
    last_updated_on: Annotated[
        str | None,
        Field(
            default=None,
            description="Filter by last update date using an inclusive range: '[before~after]'. 'before' and 'after' are date-time strings. Default date format: YYYY-MM-DD. Examples: '[2025-01-01~2025-02-01]'. Either 'before' or 'after' can be omitted, e.g., '[~2025-02-01]' or '[2025-01-01~]'.",
        ),
    ],
    order_by: Annotated[
        str | None,
        Field(
            default=None,
            description="Comma-separated list of properties to order results by. Prefix with '-' for descending, '+' for ascending (default ascending). Allowed columns: name, creation_timestamp, last_update_timestamp. Example: '-last_update_timestamp,+name' translates to 'last_update_timestamp DESC, name ASC'.",
        ),
    ],
    offset: Annotated[int, Field(default=0, ge=0, description="The offset from the beginning of the list. Must be a non-negative number.")],
    limit: Annotated[int, Field(default=10, ge=1, le=100, description="The maximum number of items to return. Must be between 1 and 100 (inclusive).")],
    biz_ctx_service: BizCtxService = Depends(get_biz_ctx_service),
) -> GetBizCtxPaginationResponse:
    """
    Get a paginated list of business contexts.

    This function retrieves business contexts with support for pagination, filtering,
    and sorting. It returns detailed information about each business context including creation
    and update metadata, and associated business context values.

    Args:
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
        offset (int, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int, optional): The maximum number of items to return. Must be between 1 and 100 (inclusive). Defaults to 10.

    Returns:
        GetBizCtxPaginationResponse: Response object containing:
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
        >>> result = await get_business_contexts()
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
    try:
        page = await biz_ctx_service.list(
            limit=limit,
            offset=offset,
            order_by=order_by,
            name=name,
            created_on=parse_date_range(created_on),
            last_updated_on=parse_date_range(last_updated_on),
        )
        return _to_list_response(items=page.items, total=page.total, offset=page.offset, limit=page.limit)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to retrieve business contexts.") from exc


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
    biz_ctx_id: Annotated[int, Field(gt=0, description="Unique numeric identifier of the business context to retrieve.")],
    biz_ctx_service: BizCtxService = Depends(get_biz_ctx_service),
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
    try:
        row = await biz_ctx_service.get(biz_ctx_id)
        if row is None:
            raise ToolError(f"The business context with ID {biz_ctx_id} was not found. Please check the ID and try again.")
        return GetBizCtxResponse.model_validate(row, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to retrieve business context {biz_ctx_id}.") from exc


@mcp.tool(
    name="create_business_context",
    description="Create a new business context",
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
    name: Annotated[str, Field(min_length=1, max_length=100, description="Short, descriptive name of the business context.")],
    biz_ctx_service: BizCtxService = Depends(get_biz_ctx_service),
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
    try:
        created_id = await biz_ctx_service.create(name=name)
        return CreateBizCtxResponse(biz_ctx_id=created_id)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to create the business context.") from exc


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
    biz_ctx_id: Annotated[int, Field(gt=0, description="Identifier of the business context to which this value will be added.")],
    ctx_scheme_value_id: Annotated[int, Field(gt=0, description="Identifier of the context scheme value to associate with this business context.")],
    biz_ctx_service: BizCtxService = Depends(get_biz_ctx_service),
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
    try:
        created_id = await biz_ctx_service.create_value(
            biz_ctx_id=biz_ctx_id,
            ctx_scheme_value_id=ctx_scheme_value_id,
        )
        return CreateBizCtxValueResponse(biz_ctx_value_id=created_id)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to create the business context value.") from exc


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
    biz_ctx_id: Annotated[int, Field(gt=0, description="Unique identifier of the business context to update.")],
    name: Annotated[str | None, Field(default=None, description="New name to assign to the business context.")],
    biz_ctx_service: BizCtxService = Depends(get_biz_ctx_service),
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
    try:
        result = await biz_ctx_service.update(
            biz_ctx_id=biz_ctx_id,
            name=UNSET if name is None else name,
        )
        if result is None:
            raise ToolError(f"The business context with ID {biz_ctx_id} was not found. Please check the ID and try again.")
        updated_id, updates = result
        return UpdateBizCtxResponse(biz_ctx_id=updated_id, updates=updates)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to update business context {biz_ctx_id}.") from exc


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
    biz_ctx_value_id: Annotated[int, Field(gt=0, description="Unique identifier of the business context value to update.")],
    ctx_scheme_value_id: Annotated[int, Field(gt=0, description="Identifier of the new context scheme value to associate with this business context value.")],
    biz_ctx_service: BizCtxService = Depends(get_biz_ctx_service),
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
    try:
        result = await biz_ctx_service.update_value_by_id(
            biz_ctx_value_id=biz_ctx_value_id,
            ctx_scheme_value_id=ctx_scheme_value_id,
        )
        if result is None:
            raise ToolError(
                f"The business context value with ID {biz_ctx_value_id} was not found. Please check the ID and try again."
            )
        updated_id, updates = result
        return UpdateBizCtxValueResponse(biz_ctx_value_id=updated_id, updates=updates)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to update business context value {biz_ctx_value_id}.") from exc


@mcp.tool(
    name="delete_business_context",
    description="Delete a business context and its associated business context values",
    output_schema={
        "type": "object",
        "description": "Response containing the deleted business context ID or cancellation message",
        "properties": {
            "biz_ctx_id": {"type": ["integer", "null"], "description": "Unique identifier of the deleted business context (null if deletion was cancelled)", "example": 123},
            "message": {"type": ["string", "null"], "description": "Optional message indicating the status of the deletion operation", "example": "Deletion cancelled by user"}
        },
        "required": []
    }
)
async def delete_business_context(
    biz_ctx_id: Annotated[int, Field(gt=0, description="Unique identifier of the business context to delete.")],
    ctx: Context,
    biz_ctx_service: BizCtxService = Depends(get_biz_ctx_service),
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
    try:
        row = await biz_ctx_service.get(biz_ctx_id)
        if row is None:
            raise ToolError(f"The business context with ID {biz_ctx_id} was not found. Please check the ID and try again.")

        biz_ctx_name = row.name or f"business context {biz_ctx_id}"

        # Create confirmation message with business-context details.
        elicit_result = await ctx.elicit(
            message=(
                f"Are you sure you want to discard '{biz_ctx_name}' business context?\n\n"
                "It will be permanently removed.\n"
            ),
            response_type=None,
        )

        # Check whether the user confirmed the deletion.
        match elicit_result:
            case AcceptedElicitation():
                deleted = await biz_ctx_service.delete(biz_ctx_id)
                if not deleted:
                    raise ToolError(
                        f"The business context with ID {biz_ctx_id} was not found. Please check the ID and try again."
                    )
                return DeleteBizCtxResponse(biz_ctx_id=biz_ctx_id)
            case DeclinedElicitation():
                return DeleteBizCtxResponse(biz_ctx_id=None, message="Deletion declined by user")
            case CancelledElicitation():
                return DeleteBizCtxResponse(biz_ctx_id=None, message="Deletion cancelled by user")

        raise ToolError("Deletion could not be completed.")
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to delete business context {biz_ctx_id}.") from exc


@mcp.tool(
    name="delete_business_context_value",
    description="Delete a specific business context value",
    output_schema={
        "type": "object",
        "description": "Response containing the deleted business context value ID or cancellation message",
        "properties": {
            "biz_ctx_value_id": {"type": ["integer", "null"], "description": "Unique identifier of the deleted business context value (null if deletion was cancelled)", "example": 123},
            "message": {"type": ["string", "null"], "description": "Optional message indicating the status of the deletion operation", "example": "Deletion cancelled by user"}
        },
        "required": []
    }
)
async def delete_business_context_value(
    biz_ctx_value_id: Annotated[int, Field(gt=0, description="Unique identifier of the business context value to delete.")],
    ctx: Context,
    biz_ctx_service: BizCtxService = Depends(get_biz_ctx_service),
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
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
    try:
        value_row = await biz_ctx_service.get_value(biz_ctx_value_id)
        if value_row is None:
            raise ToolError(
                f"The business context value with ID {biz_ctx_value_id} was not found. Please check the ID and try again."
            )

        biz_ctx_row = await biz_ctx_service.get(value_row.biz_ctx_id)
        ctx_scheme_value_row = await ctx_scheme_service.get_value(value_row.ctx_scheme_value_id)

        biz_ctx_name = (
            biz_ctx_row.name
            if biz_ctx_row is not None and biz_ctx_row.name
            else f"business context {int(value_row.biz_ctx_id)}"
        )
        ctx_scheme_value_name = (
            ctx_scheme_value_row.value
            if ctx_scheme_value_row is not None
            else f"context scheme value {int(value_row.ctx_scheme_value_id)}"
        )

        # Create confirmation message with business-context value details.
        elicit_result = await ctx.elicit(
            message=(
                f"Are you sure you want to discard '{ctx_scheme_value_name}' from '{biz_ctx_name}' business context?\n\n"
                "It will be permanently removed.\n"
            ),
            response_type=None,
        )

        # Check whether the user confirmed the deletion.
        match elicit_result:
            case AcceptedElicitation():
                deleted = await biz_ctx_service.delete_value_by_id(biz_ctx_value_id)
                if not deleted:
                    raise ToolError(
                        f"The business context value with ID {biz_ctx_value_id} was not found. Please check the ID and try again."
                    )
                return DeleteBizCtxValueResponse(biz_ctx_value_id=biz_ctx_value_id)
            case DeclinedElicitation():
                return DeleteBizCtxValueResponse(biz_ctx_value_id=None, message="Deletion declined by user")
            case CancelledElicitation():
                return DeleteBizCtxValueResponse(biz_ctx_value_id=None, message="Deletion cancelled by user")

        raise ToolError("Deletion could not be completed.")
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to delete business context value {biz_ctx_value_id}.") from exc


def _build_biz_ctx_service(
    session: AsyncSession,
    requester: AuthenticatedUser,
) -> BizCtxService:
    """Construct the business-context service for an MCP request."""
    plugin = get_vendor_plugin()
    return BizCtxService(
        plugin.create_biz_ctx_repository(session),
        requester=requester,
        account_service_repo=plugin.create_app_user_repository(session),
    )


def _build_ctx_scheme_service(
    session: AsyncSession,
    requester: AuthenticatedUser,
) -> CtxSchemeService:
    """Construct the context-scheme service for related lookups."""
    plugin = get_vendor_plugin()
    return CtxSchemeService(
        plugin.create_ctx_scheme_repository(session),
        requester=requester,
        account_service_repo=plugin.create_app_user_repository(session),
    )


def _to_list_response(*, items: list[Any], total: int, offset: int, limit: int) -> GetBizCtxPaginationResponse:
    """Build the paginated MCP response model."""
    return GetBizCtxPaginationResponse(
        total_items=total,
        offset=offset,
        limit=limit,
        items=[BizCtxResponseEntry.model_validate(item, from_attributes=True) for item in items],
    )
