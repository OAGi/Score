"""
MCP Tools for managing Tag operations in connectCenter.

This module provides Model Context Protocol (MCP) tools for querying and retrieving
Tags, which are classification labels used to categorize and organize core components
within connectCenter. Tags serve as a mechanism for grouping and
specifying sets of core components based on shared characteristics or functional
relationships. Common examples include semantic classifications such as 'Noun' and
'Verb', as well as domain-specific groupings like 'BOD' (Business Object Document)
used in connectSpec and other libraries.

Tags enable users to filter, search, and organize core components more effectively
by applying consistent categorization schemes across connectCenter. The tools provide
a standardized MCP interface, enabling clients to interact with Tag data programmatically.

Available Tools:
- get_tags: Retrieve paginated lists of tags with optional filters for name and
  description. Supports date range filtering for creation and last update timestamps,
  and custom sorting.

Key Features:
- Filter by tag name and description (case-insensitive partial matching)
- Date range filtering for creation and last update timestamps
- Support for pagination and flexible sorting
- Comprehensive error handling and validation
- Structured response models with creator/updater metadata and color information

The tools provide a clean, consistent interface for accessing Tag data through the MCP protocol.
All operations require proper authentication and authorization.
"""

from __future__ import annotations

import logging
from typing import Annotated, Any

from fastmcp import FastMCP
from fastmcp.dependencies import Depends
from pydantic import Field
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.vendor_plugins import get_vendor_plugin
from app.routes.models.tag import TagEntry
from app.routes.utils.date import parse_date_range
from app.security import AuthenticatedUser
from app.services.tag_service import TagService
from app.tools import _to_tool_error, get_tool_authenticated_user, tool_session
from app.tools.models.tag import GetTagPaginationResponse

logger = logging.getLogger("connectcenter.mcp.tag")

mcp = FastMCP("connectCenter MCP - Tag Tools")


async def get_tag_service(
    session: AsyncSession = Depends(tool_session),
) -> TagService:
    """Provide a requester-scoped tag service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    return _build_tag_service(session, requester)


@mcp.tool(
    name="get_tags",
    description="Get a paginated list of tags used to categorize and organize core components",
    output_schema={
        "type": "object",
        "description": "Response containing paginated list of tags. Tags are classification labels used to categorize and organize core components, enabling filtering and grouping of components by shared characteristics. Common examples include semantic classifications such as 'Noun' and 'Verb', as well as domain-specific groupings like 'BOD' (Business Object Document) in connectSpec.",
        "properties": {
            "total_items": {"type": "integer", "description": "Total number of tags available. Allowed values: non-negative integers (≥0).", "example": 25},
            "offset": {"type": "integer", "description": "Offset of the first item in this page. Allowed values: non-negative integers (≥0). Default value: 0.", "example": 0},
            "limit": {"type": "integer", "description": "Number of items returned in this page. Allowed values: integers between 1 and 100 (inclusive). Default value: 10.", "example": 10},
            "items": {
                "type": "array",
                "description": "List of tags on this page",
                "items": {
                    "type": "object",
                    "properties": {
                        "tag_id": {"type": "integer", "description": "Unique identifier for the tag", "example": 1},
                        "name": {"type": "string", "description": "Name of the tag used to categorize core components", "example": "BOD"},
                        "description": {"type": ["string", "null"], "description": "Description of the tag and its purpose in categorizing core components", "example": "Business Object Document"},
                        "color": {"type": ["string", "null"], "description": "Background color of the tag in hexadecimal format", "example": "#D1446B"},
                        "text_color": {"type": ["string", "null"], "description": "Text color of the tag in hexadecimal format", "example": "#FFFFFF"},
                        "created": {
                            "type": "object",
                            "description": "Information about the creation of the tag",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who created the tag",
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
                            "description": "Information about the last update of the tag",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who last updated the tag",
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
                    "required": ["tag_id", "name", "created", "last_updated"]
                }
            }
        },
        "required": ["total_items", "offset", "limit", "items"]
    }
)
async def get_tags(
    name: Annotated[str | None, Field(default=None, description="Filter by tag name using partial match (case-insensitive).")],
    description: Annotated[str | None, Field(default=None, description="Filter by tag description using partial match (case-insensitive).")],
    created_on: Annotated[str | None, Field(default=None, description="Filter by creation date using an inclusive range: '[before~after]'.")],
    last_updated_on: Annotated[str | None, Field(default=None, description="Filter by last update date using an inclusive range: '[before~after]'.")],
    order_by: Annotated[str | None, Field(default=None, description="Comma-separated list of properties to order results by. Allowed columns: name, description, creation_timestamp, last_update_timestamp.")],
    offset: Annotated[int, Field(default=0, ge=0, description="The offset from the beginning of the list.")],
    limit: Annotated[int, Field(default=10, ge=1, le=100, description="The maximum number of items to return.")],
    tag_service: TagService = Depends(get_tag_service),
) -> GetTagPaginationResponse:
    """
    Get a paginated list of tags used to categorize and organize core components.

    Tags are classification labels that serve as a mechanism for grouping and specifying
    sets of core components based on shared characteristics or functional relationships.
    They enable users to filter, search, and organize core components more effectively
    by applying consistent categorization schemes across connectCenter. Common examples
    include semantic classifications such as 'Noun' and 'Verb', as well as domain-specific
    groupings like 'BOD' (Business Object Document) used in connectSpec and other libraries.

    This function retrieves all available tags with support for pagination, filtering, and sorting.
    This tool is particularly useful for discovering available tag names that can be used
    as filter values in other tools like get_core_components.

    Args:
        name (str | None, optional): Filter by tag name using partial match (case-insensitive). Defaults to None.
        description (str | None, optional): Filter by tag description using partial match (case-insensitive). Defaults to None.
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
            Example: '-creation_timestamp,+name' translates to 'creation_timestamp DESC, name ASC'.
            Defaults to None.
        offset (int, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int, optional): The maximum number of items to return. Must be between 1 and 100 (inclusive). Defaults to 10.

    Returns:
        GetTagPaginationResponse: Response object containing:
            - total_items: Total number of tags available
            - offset: Offset of the first item in this page
            - limit: Number of items returned in this page
            - items: List of tags on this page with information including:
                - tag_id: Unique identifier for the tag
                - name: Tag name
                - description: Description of the tag
                - color: Color code for the tag (if any)
                - text_color: Text color code for the tag (if any)

    Raises:
        ToolError: If validation fails, parsing errors occur, or other errors happen.
            Common error scenarios include:
            - Invalid pagination parameters (negative offset, limit out of range)
            - Invalid date range format
            - Invalid order_by column names or format
            - Database connection issues
            - Authentication failures

    Examples:
        Get all tags:
        >>> result = get_tags(offset=0, limit=10)
        >>> print(f"Found {result.total_items} tags")

        Get tags with filtering:
        >>> result = get_tags(name="BOD", limit=5)
        >>> for tag in result.items:
        ...     print(f"Tag: {tag.name} - {tag.description}")

        Get all available tag names:
        >>> result = get_tags(limit=100)
        >>> tag_names = [tag.name for tag in result.items]
        >>> print(f"Available tags: {tag_names}")
    """
    try:
        page = await tag_service.list(
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
        raise _to_tool_error(exc, fallback="Unable to retrieve tags.") from exc


def _build_tag_service(
    session: AsyncSession,
    requester: AuthenticatedUser,
) -> TagService:
    """Construct the tag service for an MCP request."""
    plugin = get_vendor_plugin()
    return TagService(
        plugin.create_tag_repository(session),
        plugin.create_app_user_repository(session),
        requester=requester,
    )


def _to_list_response(*, items: list[Any], total: int, offset: int, limit: int) -> GetTagPaginationResponse:
    """Build the paginated MCP response model."""
    return GetTagPaginationResponse(
        total_items=total,
        offset=offset,
        limit=limit,
        items=[TagEntry.model_validate(item, from_attributes=True) for item in items],
    )
