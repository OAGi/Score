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

import logging
from typing import Annotated

from fastapi import HTTPException
from fastmcp import FastMCP
from fastmcp.exceptions import ToolError
from pydantic import Field

from services import CtxCategoryService, DateRangeParams, PaginationParams
from tools import _validate_auth_and_db, parse_order_by_to_sorts, _create_user_info
from tools.models.ctx_category import (
    CreateCtxCategoryResponse,
    DeleteCtxCategoryResponse,
    GetCtxCategoriesResponse,
    GetCtxCategoryResponse,
    UpdateCtxCategoryResponse,
)
from tools.models.common import WhoAndWhen
from tools.utils import parse_date_range

# Configure logging
logger = logging.getLogger(__name__)

mcp = FastMCP("Score MCP Server - Context Category Tools")


@mcp.tool(
    name="get_context_categories",
    description="Get a paginated list of context categories",
    output_schema={
        "type": "object",
        "description": "Response containing paginated list of context categories",
        "properties": {
            "total_items": {"type": "integer", "description": "Total number of context categories available. Allowed values: non-negative integers (≥0).", "example": 25},
            "offset": {"type": "integer", "description": "Offset of the first item in this page. Allowed values: non-negative integers (≥0). Default value: 0.", "example": 0},
            "limit": {"type": "integer", "description": "Number of items returned in this page. Allowed values: integers between 1 and 100 (inclusive). Default value: 10.", "example": 10},
            "items": {
                "type": "array",
                "description": "List of context categories on this page",
                "items": {
                    "type": "object",
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
            }
        },
        "required": ["total_items", "offset", "limit", "items"]
    }
)
async def get_context_categories(
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
        name: Annotated[str | None, "Filter categories by name."] = None,
        description: Annotated[str | None, "Filter categories by description."] = None,
        created_on: Annotated[
            str | None, "Filter by creation date using an inclusive range: '[before~after]'. 'before' and 'after' are date-time strings. Default date format: YYYY-MM-DD. Examples: '[2025-01-01~2025-02-01]'. Either 'before' or 'after' can be omitted, e.g., '[~2025-02-01]' or '[2025-01-01~]'."] = None,
        last_updated_on: Annotated[
            str | None, "Filter by last update date using an inclusive range: '[before~after]'. 'before' and 'after' are date-time strings. Default date format: YYYY-MM-DD. Examples: '[2025-01-01~2025-02-01]'. Either 'before' or 'after' can be omitted, e.g., '[~2025-02-01]' or '[2025-01-01~]'."] = None,
        order_by: Annotated[
            str | None, "Comma-separated list of properties to order results by. Prefix with '-' for descending, '+' for ascending (default ascending). Allowed columns: name, description, creation_timestamp, last_update_timestamp. Example: '-last_update_timestamp,+name,description' translates to 'last_update_timestamp DESC, name ASC, description ASC'."] = None
) -> GetCtxCategoriesResponse:
    """
    Get a paginated list of context categories.
    
    This function retrieves context categories with support for pagination, filtering,
    and sorting. It returns detailed information about each category including creation
    and update metadata.
    
    Args:
        offset (int | None, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int | None, optional): The maximum number of items to return. Must be a non-negative number. Defaults to 10.
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
    
    Returns:
        GetCtxCategoriesResponse: Response object containing:
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
        >>> result = await get_context_categories(offset=0, limit=10)
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Create service instance
    ctx_category_service = CtxCategoryService(requester=app_user)

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
                f"Valid columns: {", ".join(ctx_category_service.allowed_columns_for_order_by)}") from e

    # Get context categories
    try:
        page = ctx_category_service.get_ctx_categories(name, description, created_on_params, last_updated_on_params,
                                                       pagination, sort_list)

        return GetCtxCategoriesResponse(
            total_items=page.total,
            offset=page.offset,
            limit=page.limit,
            items=[_create_ctx_category_result(ctx_category) for ctx_category in page.items]
        )
    except HTTPException as e:
        logger.error(f"HTTP error retrieving context categories: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving context categories: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the context categories: {str(e)}. Please contact your system administrator.") from e


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
        ctx_category_id: Annotated[int, Field(
            description="The unique identifier of the context category to fetch.",
            examples=[123, 456, 789],
            gt=0,
            title="Context Category ID"
        )]
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Get context category
    try:
        service = CtxCategoryService(requester=app_user)
        ctx_category = service.get_ctx_category(ctx_category_id)

        return _create_ctx_category_result(ctx_category)
    except HTTPException as e:
        logger.error(f"HTTP error retrieving context category: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The context category with ID {ctx_category_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving context category: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the context category: {str(e)}. Please contact your system administrator.") from e


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
        name: Annotated[str, Field(
            description="The human-readable name of the new context category.",
            examples=["Geographic", "Temporal", "Industry"],
            min_length=1,
            max_length=100,
            title="Context Category Name"
        )],
        description: Annotated[str | None, Field(
            description="A detailed description or purpose of the new context category.",
            examples=["Geographic context for location-based data", "Temporal context for time-based data", "Industry context for business-specific data"],
            title="Description"
        )] = None
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Create context category
    try:
        service = CtxCategoryService(requester=app_user)
        ctx_category = service.create_ctx_category(name, description)

        return CreateCtxCategoryResponse(ctx_category_id=ctx_category.ctx_category_id)
    except HTTPException as e:
        logger.error(f"HTTP error creating context category: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error creating context category: {e}")
        raise ToolError(
            f"An unexpected error occurred while creating the context category: {str(e)}. Please contact your system administrator.") from e


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
        ctx_category_id: Annotated[int, "Unique identifier of the context category to update."],
        name: Annotated[str | None, "The new name for the context category."] = None,
        description: Annotated[str | None, "The new description or purpose for the context category."] = None
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Update context category
    try:
        service = CtxCategoryService(requester=app_user)
        ctx_category, original_values = service.update_ctx_category(ctx_category_id, name, description)

        # Create update summary
        updates = []
        if name is not None and original_values["name"] != name:
            updates.append("name")
        if description is not None and original_values["description"] != description:
            updates.append("description")

        return UpdateCtxCategoryResponse(ctx_category_id=ctx_category.ctx_category_id, updates=updates)
    except HTTPException as e:
        logger.error(f"HTTP error updating context category: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The context category with ID {ctx_category_id} was not found. Please check the ID and try again.")  from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error updating context category: {e}")
        raise ToolError(
            f"An unexpected error occurred while updating the context category: {str(e)}. Please contact your system administrator.") from e


@mcp.tool(
    name="delete_context_category",
    description="Delete an existing context category by ID",
    output_schema={
        "type": "object",
        "description": "Response containing the deleted context category ID",
        "properties": {
            "ctx_category_id": {"type": "integer", "description": "Unique identifier of the deleted context category", "example": 123}
        },
        "required": ["ctx_category_id"]
    }
)
async def delete_context_category(
        ctx_category_id: Annotated[int, "Unique identifier of the context category to delete."]
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Delete context category
    try:
        service = CtxCategoryService(requester=app_user)
        service.delete_ctx_category(ctx_category_id)

        return DeleteCtxCategoryResponse(ctx_category_id=ctx_category_id)
    except HTTPException as e:
        logger.error(f"HTTP error deleting context category: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The context category with ID {ctx_category_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 409:
            raise ToolError(f"Conflict: {e.detail}") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error deleting context category: {e}")
        raise ToolError(
            f"An unexpected error occurred while deleting the context category: {str(e)}. Please contact your system administrator.") from e


# Helper functions (placed after their usage)

def _create_ctx_category_result(ctx_category) -> GetCtxCategoryResponse:
    """
    Create a formatted response object for context category data.
    
    This helper function transforms a CtxCategory database model into a standardized
    response format that includes user information and timestamps.
    
    Args:
        ctx_category: The CtxCategory database model instance to format
        
    Returns:
        GetCtxCategoryResponse: A formatted response object containing:
            - ctx_category_id: The unique identifier of the context category
            - guid: The globally unique identifier
            - name: The name of the context category
            - description: The description (may be None)
            - created: WhoAndWhen object with creator info and creation timestamp
            - last_updated: WhoAndWhen object with updater info and update timestamp
    """
    return GetCtxCategoryResponse(
        ctx_category_id=ctx_category.ctx_category_id,
        guid=ctx_category.guid,
        name=ctx_category.name,
        description=ctx_category.description,
        created=WhoAndWhen(
            who=_create_user_info(ctx_category.creator),
            when=ctx_category.creation_timestamp
        ),
        last_updated=WhoAndWhen(
            who=_create_user_info(ctx_category.last_updater),
            when=ctx_category.last_update_timestamp
        )
    )
