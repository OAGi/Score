"""
MCP Tools for managing Context Category operations in connectCenter.

This module provides Model Context Protocol (MCP) tools for creating, updating, deleting,
and querying Context Categories, which provide classification and organization for Context
Schemes. Context Categories serve as a top-level organizational mechanism that groups
related Context Schemes by their domain or purpose. They enable users to organize and
categorize business contexts into logical groupings, making it easier to discover and
manage related context schemes.

Context Categories enable better organization and discovery of business contexts by
providing a hierarchical structure where categories contain multiple context schemes.
Common examples include categories such as "Geographic" (for location-based contexts
like country, region, or city), "Temporal" (for time-based contexts like fiscal year
or reporting period), and "Industry" (for industry-specific contexts like healthcare
or finance). Each category can contain multiple context schemes, and each scheme can
have multiple values that represent specific contextual information. The tools provide
a standardized MCP interface, enabling clients to interact with Context Category data
programmatically.

Available Tools:
- get_context_categories: Retrieve paginated lists of Context Categories with optional
  filters for name, description, and date ranges. Supports custom sorting.

- get_context_category: Retrieve a single Context Category by ID with all relationships
  loaded (creator, last_updater).

- create_context_category: Create a new Context Category with name and optional description.
  Automatically generates a GUID and sets creation/update timestamps. Validates input
  length constraints.

- update_context_category: Update an existing Context Category's name and/or description.
  Returns both the updated category and the original values for audit purposes.

- delete_context_category: Delete a Context Category. Prevents deletion if the category
  has linked Context Schemes, providing detailed error messages with scheme IDs.

Key Features:
- Full CRUD operations (Create, Read, Update, Delete)
- Full relationship loading (creator, last_updater)
- Support for filtering, pagination, and sorting
- Validation and constraint checking (prevents deletion if linked to schemes)
- Comprehensive error handling and validation
- Structured response models with detailed metadata

The tools provide a clean, consistent interface for accessing Context Category data through the MCP protocol.
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
from app.services.ctx_category_service import ContextCategoryService
from app.tools import _to_tool_error, get_tool_authenticated_user, tool_session
from app.tools.models.ctx_category import (
    CtxCategoryEntryResponse,
    CreateCtxCategoryResponse,
    DeleteCtxCategoryResponse,
    GetCtxCategoryPaginationResponse,
    GetCtxCategoryResponse,
    UpdateCtxCategoryResponse,
)
from app.types.unset import UNSET

logger = logging.getLogger("connectcenter.mcp.ctx_category")

mcp = FastMCP("connectCenter MCP - Context Category Tools")


async def get_ctx_category_service(
    session: AsyncSession = Depends(tool_session),
) -> ContextCategoryService:
    """Provide a requester-scoped context category service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    return _build_ctx_category_service(session, requester)


@mcp.tool(
    name="get_context_categories",
    description="Get a paginated list of context categories",
    output_schema={
        "type": "object",
        "description": "Response containing paginated list of context categories",
        "properties": {
            "total_items": {"type": "integer",
                            "description": "Total number of context categories available. Allowed values: non-negative integers (≥0).",
                            "example": 25},
            "offset": {"type": "integer",
                       "description": "Offset of the first item in this page. Allowed values: non-negative integers (≥0). Default value: 0.",
                       "example": 0},
            "limit": {"type": "integer",
                      "description": "Number of items returned in this page. Allowed values: integers between 1 and 100 (inclusive). Default value: 10.",
                      "example": 10},
            "items": {
                "type": "array",
                "description": "List of context categories on this page",
                "items": {
                    "type": "object",
                    "properties": {
                        "ctx_category_id": {"type": "integer",
                                            "description": "Unique identifier for the context category",
                                            "example": 123},
                        "guid": {"type": "string",
                                 "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)",
                                 "example": "a1b2c3d4e5f6789012345678901234ab"},
                        "name": {"type": "string", "description": "Name of the context category",
                                 "example": "Geographic"},
                        "description": {"type": ["string", "null"],
                                        "description": "Description of the context category",
                                        "example": "Geographic context for location-based data"},
                        "created": {
                            "type": "object",
                            "description": "Information about the creation of the context category",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who created the context category",
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
                            "description": "Information about the last update of the context category",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who last updated the context category",
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
                    "required": ["ctx_category_id", "guid", "name", "created", "last_updated"]
                }
            }
        },
        "required": ["total_items", "offset", "limit", "items"]
    }
)
async def get_context_categories(
    name: Annotated[str | None, Field(default=None, description="Filter categories by name.")],
    description: Annotated[str | None, Field(default=None, description="Filter categories by description.")],
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
            description="Comma-separated list of properties to order results by. Prefix with '-' for descending, '+' for ascending (default ascending). Allowed columns: name, description, creation_timestamp, last_update_timestamp. Example: '-last_update_timestamp,+name,description' translates to 'last_update_timestamp DESC, name ASC, description ASC'.",
        ),
    ],
    offset: Annotated[
        int,
        Field(
            default=0,
            ge=0,
            description="The offset from the beginning of the list. Must be a non-negative number.",
        ),
    ],
    limit: Annotated[
        int,
        Field(
            default=10,
            ge=1,
            le=100,
            description="The maximum number of items to return. Must be between 1 and 100 (inclusive).",
        ),
    ],
    context_category_service: ContextCategoryService = Depends(get_ctx_category_service),
) -> GetCtxCategoryPaginationResponse:
    """
    Get a paginated list of context categories.

    This function retrieves context categories with support for pagination, filtering,
    and sorting. It returns detailed information about each category including creation
    and update metadata.

    Args:
        name (str | None, optional): Filter categories by name. Defaults to None.
        description (str | None, optional): Filter categories by description. Defaults to None.
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
            Allowed columns: name, description, creation_timestamp, last_update_timestamp.
            Example: '-last_update_timestamp,+name,description' translates to 'last_update_timestamp DESC, name ASC, description ASC'.
            Defaults to None.
        offset (int, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int, optional): The maximum number of items to return. Must be between 1 and 100 (inclusive). Defaults to 10.

    Returns:
        GetCtxCategoryPaginationResponse: Response object containing:
            - total_items: Total number of context categories available
            - offset: Offset of the first item in this page
            - limit: Number of items returned in this page
            - items: List of context categories on this page

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
        >>> result = await get_context_categories()
        >>> print(f"Found {result.total_items} categories")

        Filtered search:
        >>> result = await get_context_categories(
        ...     name="business",
        ...     created_on="[2023-01-01~2023-12-31]",
        ...     order_by="-last_update_timestamp"
        ... )

        Pagination:
        >>> result = await get_context_categories(offset=20, limit=5)
    """
    try:
        page = await context_category_service.list(
            limit=limit,
            offset=offset,
            order_by=order_by,
            name=name,
            description=description,
            created_on=parse_date_range(created_on),
            last_updated_on=parse_date_range(last_updated_on),
        )
        return _to_list_response(items=page.items, total=page.total, offset=page.offset, limit=page.limit)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to retrieve context categories.") from exc


@mcp.tool(
    name="get_context_category",
    description="Get a specific context category by ID",
    output_schema={
        "type": "object",
        "description": "Response containing context category information",
        "properties": {
            "ctx_category_id": {"type": "integer", "description": "Unique identifier for the context category", "example": 123},
            "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
            "name": {"type": "string", "description": "Name of the context category", "example": "Geographic"},
            "description": {"type": ["string", "null"], "description": "Description of the context category", "example": "Geographic context for location-based data"},
            "created": {
                "type": "object",
                "description": "Information about the creation of the context category",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who created the context category",
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
                "description": "Information about the last update of the context category",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who last updated the context category",
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
        "required": ["ctx_category_id", "guid", "name", "created", "last_updated"]
    }
)
async def get_context_category(
    ctx_category_id: Annotated[int, Field(description="Unique numeric identifier of the context category to retrieve.", gt=0)],
    context_category_service: ContextCategoryService = Depends(get_ctx_category_service),
) -> GetCtxCategoryResponse:
    """
    Get a specific context category by ID.

    This function retrieves a single context category from the database and returns
    detailed information including metadata about who created it and when it was
    last updated.

    Args:
        ctx_category_id (int): The unique identifier of the context category to fetch

    Returns:
        GetCtxCategoryResponse: Response object containing:
            - ctx_category_id: Unique identifier for the context category
            - guid: A globally unique identifier (GUID) for the context category
            - name: The human-readable name of the context category
            - description: A detailed description or purpose of the context category
            - created: Information about the creation of the context category
            - last_updated: Information about the most recent update to the context category

    Raises:
        ToolError: If validation fails, the context category is not found, or other errors occur.
            Common error scenarios include:
            - Invalid context category ID
            - Context category not found (404 error)
            - Database connection issues
            - Authentication failures

    Example:
        >>> result = await get_context_category(ctx_category_id=123)
        >>> print(f"Category: {result.name}")
        >>> print(f"Created by: {result.created.who.username}")
        Category: Business Context
        Created by: john_doe
    """
    try:
        row = await context_category_service.get(ctx_category_id)
        if row is None:
            raise ToolError(f"The context category with ID {ctx_category_id} was not found. Please check the ID and try again.")
        return GetCtxCategoryResponse.model_validate(row, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to retrieve context category {ctx_category_id}.") from exc


@mcp.tool(
    name="create_context_category",
    description="Create a new context category with a name and optional description",
    output_schema={
        "type": "object",
        "description": "Response containing the created context category ID",
        "properties": {
            "ctx_category_id": {"type": "integer", "description": "Unique identifier for the created context category", "example": 123}
        },
        "required": ["ctx_category_id"]
    }
)
async def create_context_category(
    name: Annotated[str, Field(description="Human-readable name of the context category to create.", min_length=1, max_length=100)],
    description: Annotated[str | None, Field(default=None, description="A detailed description or purpose of the new context category.")],
    context_category_service: ContextCategoryService = Depends(get_ctx_category_service),
) -> CreateCtxCategoryResponse:
    """
    Create a new context category with a name and optional description.

    This function creates a new context category with the specified name and optional
    description. The context category will be associated with the authenticated user
    as both creator and last updater.

    Args:
        name (str): The human-readable name of the new context category.
        description (str | None, optional): A detailed description or purpose of the new context category.
            Defaults to None.

    Returns:
        CreateCtxCategoryResponse: Response object containing:
            - ctx_category_id: Unique identifier assigned to the newly created context category

    Raises:
        ToolError: If validation fails, database errors occur, or unexpected errors happen.
            Common error scenarios include:
            - Invalid input parameters (name too long, empty name, etc.)
            - Database connection issues
            - Authentication failures

    Example:
        >>> result = await create_context_category(
        ...     name="Business Context",
        ...     description="Categories for business-related contexts"
        ... )
        >>> print(result.ctx_category_id)
        123
    """
    try:
        created_id = await context_category_service.create(name=name, description=description)
        return CreateCtxCategoryResponse(ctx_category_id=created_id)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to create the context category.") from exc


@mcp.tool(
    name="update_context_category",
    description="Update an existing context category's name or description",
    output_schema={
        "type": "object",
        "description": "Response containing the updated context category ID and list of updated fields",
        "properties": {
            "ctx_category_id": {"type": "integer", "description": "Unique identifier of the updated context category", "example": 123},
            "updates": {"type": "array", "items": {"type": "string"}, "description": "A list of updated fields, each represented by its name", "example": ["name", "description"]}
        },
        "required": ["ctx_category_id", "updates"]
    }
)
async def update_context_category(
    ctx_category_id: Annotated[int, Field(description="Unique identifier of the context category to update.", gt=0)],
    name: Annotated[str | None, Field(default=None, description="The new name for the context category.")],
    description: Annotated[str | None, Field(default=None, description="The new description or purpose for the context category.")],
    context_category_service: ContextCategoryService = Depends(get_ctx_category_service),
) -> UpdateCtxCategoryResponse:
    """
    Update an existing context category's name or description.

    This function allows you to modify the name and/or description of an existing
    context category. At least one of name or description must be provided. The function
    tracks what changes were made and returns a summary of the updates.

    Args:
        ctx_category_id (int): Unique identifier of the context category to update
        name (str | None, optional): The new name for the context category. If not provided,
            the name will not be updated.
        description (str | None, optional): The new description or purpose for the context category.
            If not provided, the description will not be updated.

    Returns:
        UpdateCtxCategoryResponse: Response object containing:
            - ctx_category_id: Unique identifier of the updated context category
            - updates: A list of updated fields, each represented by its name

    Raises:
        ToolError: If validation fails, the context category is not found, or other errors occur.
            Common error scenarios include:
            - Invalid input parameters (name too long, empty name, etc.)
            - Neither name nor description provided
            - Context category not found (404 error)
            - Database connection issues
            - Authentication failures

    Example:
        >>> result = await update_context_category(
        ...     ctx_category_id=123,
        ...     name="Updated Business Context",
        ...     description="Updated description"
        ... )
        >>> print(result.updates)
        ['name', 'description']

        >>> result = await update_context_category(
        ...     ctx_category_id=123,
        ...     name="Updated Name Only"
        ... )
        >>> print(result.updates)
        ['name']
    """
    try:
        result = await context_category_service.update(
            context_category_id=ctx_category_id,
            name=UNSET if name is None else name,
            description=UNSET if description is None else description,
        )
        if result is None:
            raise ToolError(f"The context category with ID {ctx_category_id} was not found. Please check the ID and try again.")
        updated_id, updates = result
        return UpdateCtxCategoryResponse(ctx_category_id=updated_id, updates=updates)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to update context category {ctx_category_id}.") from exc


@mcp.tool(
    name="delete_context_category",
    description="Delete an existing context category by ID",
    output_schema={
        "type": "object",
        "description": "Response containing the deleted context category ID or cancellation message",
        "properties": {
            "ctx_category_id": {"type": ["integer", "null"], "description": "Unique identifier of the deleted context category (null if deletion was cancelled)", "example": 123},
            "message": {"type": ["string", "null"], "description": "Optional message indicating the status of the deletion operation", "example": "Deletion cancelled by user"}
        },
        "required": []
    }
)
async def delete_context_category(
    ctx_category_id: Annotated[int, Field(description="Unique identifier of the context category to delete.", gt=0)],
    ctx: Context,
    context_category_service: ContextCategoryService = Depends(get_ctx_category_service),
) -> DeleteCtxCategoryResponse:
    """
    Delete an existing context category by ID.

    This function permanently removes a context category from the database. This action
    is irreversible and should be used with caution. The function will return the ID
    of the deleted context category for confirmation.

    Note: This function will check for linked context schemes before deletion. If any
    context schemes are linked to this category, the deletion will be blocked with a
    detailed error message explaining how to resolve the conflict.

    Args:
        ctx_category_id (int): Unique identifier of the context category to delete

    Returns:
        DeleteCtxCategoryResponse: Response object containing:
            - ctx_category_id: Unique identifier of the deleted context category

    Raises:
        ToolError: If validation fails, the context category is not found, has linked
            context schemes, or other errors occur. Common error scenarios include:
            - Invalid context category ID
            - Context category not found (404 error)
            - Context category has linked context schemes (409 conflict error)
            - Database connection issues
            - Authentication failures

    Example:
        >>> result = await delete_context_category(ctx_category_id=123)
        >>> print(f"Deleted context category with ID: {result.ctx_category_id}")
        Deleted context category with ID: 123

    Warning:
        This operation is permanent and cannot be undone. If the context category has
        linked context schemes, you must delete or update those schemes first.
    """
    try:
        row = await context_category_service.get(ctx_category_id)
        if row is None:
            raise ToolError(f"The context category with ID {ctx_category_id} was not found. Please check the ID and try again.")

        elicit_result = await ctx.elicit(
            message=(
                f"Are you sure you want to discard '{row.name}' context category?\n\n"
                "It will be permanently removed.\n"
            ),
            response_type=None,
        )

        match elicit_result:
            case AcceptedElicitation():
                deleted = await context_category_service.delete(ctx_category_id)
                if not deleted:
                    raise ToolError(f"The context category with ID {ctx_category_id} was not found. Please check the ID and try again.")
                return DeleteCtxCategoryResponse(ctx_category_id=ctx_category_id)
            case DeclinedElicitation():
                return DeleteCtxCategoryResponse(ctx_category_id=None, message="Deletion declined by user")
            case CancelledElicitation():
                return DeleteCtxCategoryResponse(ctx_category_id=None, message="Deletion cancelled by user")

        raise ToolError("Deletion could not be completed. Please try again or contact your system administrator.")
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to delete context category {ctx_category_id}.") from exc


def _build_ctx_category_service(
    session: AsyncSession,
    requester: AuthenticatedUser,
) -> ContextCategoryService:
    """Construct the context-category service for an MCP request."""
    plugin = get_vendor_plugin()
    return ContextCategoryService(
        plugin.create_ctx_category_repository(session),
        requester=requester,
        account_service_repo=plugin.create_app_user_repository(session),
    )


def _to_list_response(*, items: list[Any], total: int, offset: int, limit: int) -> GetCtxCategoryPaginationResponse:
    """Build the paginated MCP response model."""
    return GetCtxCategoryPaginationResponse(
        total_items=total,
        offset=offset,
        limit=limit,
        items=[CtxCategoryEntryResponse.model_validate(item, from_attributes=True) for item in items],
    )
