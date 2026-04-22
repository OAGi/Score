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
from app.services.ctx_scheme_service import CtxSchemeService
from app.tools import _to_tool_error, get_tool_authenticated_user, tool_session
from app.tools.models.ctx_scheme import (
    CtxSchemeListEntryResponse,
    CreateCtxSchemeResponse,
    CreateCtxSchemeValueResponse,
    DeleteCtxSchemeResponse,
    DeleteCtxSchemeValueResponse,
    GetCtxSchemePaginationResponse,
    GetCtxSchemeResponse,
    UpdateCtxSchemeResponse,
    UpdateCtxSchemeValueResponse,
)
from app.types.unset import UNSET

# Configure logging.
logger = logging.getLogger("connectcenter.mcp.ctx_scheme")

mcp = FastMCP("connectCenter MCP - Context Scheme Tools")


async def get_ctx_scheme_service(
    session: AsyncSession = Depends(tool_session),
) -> CtxSchemeService:
    """Provide a requester-scoped context-scheme service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    return _build_ctx_scheme_service(session, requester)


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
    scheme_id: Annotated[str | None, Field(default=None, description="Filter by the unique scheme identifier.")],
    scheme_name: Annotated[str | None, Field(default=None, description="Filter by the human-readable name of the scheme.")],
    scheme_agency_id: Annotated[str | None, Field(default=None, description="Filter by the agency identifier responsible for the scheme.")],
    scheme_version_id: Annotated[str | None, Field(default=None, description="Filter by the version identifier of the scheme.")],
    ctx_category_id: Annotated[int | None, Field(default=None, ge=1, description="Filter by the associated context category ID.")],
    ctx_category_name: Annotated[str | None, Field(default=None, description="Filter by the name of the associated context category.")],
    description: Annotated[str | None, Field(default=None, description="Filter by text contained in the scheme description.")],
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
            description="Comma-separated list of properties to order results by. Prefix with '-' for descending, '+' for ascending (default ascending). Allowed columns: scheme_id, scheme_name, description, scheme_agency_id, scheme_version_id, creation_timestamp, last_update_timestamp. Example: '-last_update_timestamp,+scheme_id,description' translates to 'last_update_timestamp DESC, scheme_id ASC, description ASC'.",
        ),
    ],
    offset: Annotated[int, Field(default=0, ge=0, description="The offset from the beginning of the list. Must be a non-negative number.")],
    limit: Annotated[int, Field(default=10, ge=1, le=100, description="The maximum number of items to return. Must be between 1 and 100 (inclusive).")],
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
) -> GetCtxSchemePaginationResponse:
    """
    Get a paginated list of context schemes.

    This function retrieves context schemes with support for pagination, filtering,
    and sorting. It returns detailed information about each scheme including creation
    and update metadata, associated context categories, and scheme values.

    Args:
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
        offset (int, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int, optional): The maximum number of items to return. Must be between 1 and 100 (inclusive). Defaults to 10.

    Returns:
        GetCtxSchemePaginationResponse: Response object containing:
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
        >>> result = await get_context_schemes()
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
    try:
        page = await ctx_scheme_service.list(
            limit=limit,
            offset=offset,
            order_by=order_by,
            scheme_id=scheme_id,
            scheme_name=scheme_name,
            description=description,
            scheme_agency_id=scheme_agency_id,
            scheme_version_id=scheme_version_id,
            ctx_category_id=ctx_category_id,
            ctx_category_name=ctx_category_name,
            created_on=parse_date_range(created_on),
            last_updated_on=parse_date_range(last_updated_on),
        )
        return _to_list_response(items=page.items, total=page.total, offset=page.offset, limit=page.limit)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to retrieve context schemes.") from exc


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
        "required": ["ctx_scheme_id", "guid", "scheme_id", "scheme_agency_id", "scheme_version_id", "ctx_category", "values", "created", "last_updated"]
    }
)
async def get_context_scheme(
    ctx_scheme_id: Annotated[int, Field(gt=0, description="Unique numeric identifier of the context scheme to retrieve.")],
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
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
            - ctx_category: Associated context category (if any)
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
        >>> if result.ctx_category:
        ...     print(f"Category: {result.ctx_category.name}")
        >>> print(f"Values: {len(result.values)}")
        Scheme: Business Context Scheme
        Created by: john_doe
        Category: Industry Classification
        Values: 3
    """
    try:
        row = await ctx_scheme_service.get(ctx_scheme_id)
        if row is None:
            raise ToolError(f"The context scheme with ID {ctx_scheme_id} was not found. Please check the ID and try again.")
        return GetCtxSchemeResponse.model_validate(row, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to retrieve context scheme {ctx_scheme_id}.") from exc


@mcp.tool(
    name="create_context_scheme",
    description="Create a new context scheme",
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
    scheme_id: Annotated[str, Field(min_length=1, max_length=45, description="External identification of the scheme.")],
    scheme_name: Annotated[str, Field(min_length=1, max_length=255, description="Pretty print name of the context scheme.")],
    description: Annotated[str | None, Field(default=None, description="Description of the context scheme.")],
    scheme_agency_id: Annotated[str | None, Field(default=None, description="Identification of the agency maintaining the scheme.")],
    scheme_version_id: Annotated[str | None, Field(default=None, description="Version number of the context scheme.")],
    ctx_category_id: Annotated[int | None, Field(default=None, ge=1, description="Associated context category ID.")],
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
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
    try:
        created_id = await ctx_scheme_service.create(
            scheme_id=scheme_id,
            scheme_name=scheme_name,
            description=description,
            scheme_agency_id=scheme_agency_id,
            scheme_version_id=scheme_version_id,
            ctx_category_id=ctx_category_id,
        )
        return CreateCtxSchemeResponse(ctx_scheme_id=created_id)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to create the context scheme.") from exc


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
    ctx_scheme_id: Annotated[int, Field(gt=0, description="Identifier of the context scheme to which this value belongs.")],
    value: Annotated[str, Field(min_length=1, max_length=100, description="The actual value string to be added to the context scheme.")],
    meaning: Annotated[str | None, Field(default=None, description="Optional descriptive meaning or explanation of the value.")],
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
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
    try:
        created_id = await ctx_scheme_service.create_value(
            owner_ctx_scheme_id=ctx_scheme_id,
            value=value,
            meaning=meaning,
        )
        return CreateCtxSchemeValueResponse(ctx_scheme_value_id=created_id)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to create the context scheme value.") from exc


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
    ctx_scheme_value_id: Annotated[int, Field(gt=0, description="Unique identifier of the context scheme value to update.")],
    value: Annotated[str | None, Field(default=None, description="Updated value string for this context scheme entry. Omit to leave unchanged.")],
    meaning: Annotated[str | None, Field(default=None, description="Updated human-readable meaning for the value. Omit to leave unchanged.")],
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
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
    try:
        result = await ctx_scheme_service.update_value_by_id(
            ctx_scheme_value_id=ctx_scheme_value_id,
            value=UNSET if value is None else value,
            meaning=UNSET if meaning is None else meaning,
        )
        if result is None:
            raise ToolError(
                f"The context scheme value with ID {ctx_scheme_value_id} was not found. Please check the ID and try again."
            )
        updated_id, updates = result
        return UpdateCtxSchemeValueResponse(ctx_scheme_value_id=updated_id, updates=updates)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to update context scheme value {ctx_scheme_value_id}.") from exc


@mcp.tool(
    name="delete_context_scheme_value",
    description="Delete a specific context scheme value",
    output_schema={
        "type": "object",
        "description": "Response containing the deleted context scheme value ID or cancellation message",
        "properties": {
            "ctx_scheme_value_id": {"type": ["integer", "null"], "description": "Unique identifier of the deleted context scheme value (null if deletion was cancelled)", "example": 123},
            "message": {"type": ["string", "null"], "description": "Optional message indicating the status of the deletion operation", "example": "Deletion cancelled by user"}
        },
        "required": []
    }
)
async def delete_context_scheme_value(
    ctx_scheme_value_id: Annotated[int, Field(gt=0, description="Unique identifier of the context scheme value to delete.")],
    ctx: Context,
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
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
    try:
        value_row = await ctx_scheme_service.get_value(ctx_scheme_value_id)
        if value_row is None:
            raise ToolError(
                f"The context scheme value with ID {ctx_scheme_value_id} was not found. Please check the ID and try again."
            )

        scheme_row = await ctx_scheme_service.get(value_row.owner_ctx_scheme_id)
        scheme_name = (
            scheme_row.scheme_name
            if scheme_row is not None and scheme_row.scheme_name
            else scheme_row.scheme_id
            if scheme_row is not None
            else f"context scheme {int(value_row.owner_ctx_scheme_id)}"
        )

        # Create confirmation message with context scheme value details.
        elicit_result = await ctx.elicit(
            message=(
                f"Are you sure you want to discard '{value_row.value}' from '{scheme_name}' context scheme?\n\n"
                "It will be permanently removed.\n"
            ),
            response_type=None,
        )

        # Check whether the user confirmed the deletion.
        match elicit_result:
            case AcceptedElicitation():
                deleted = await ctx_scheme_service.delete_value_by_id(ctx_scheme_value_id)
                if not deleted:
                    raise ToolError(
                        f"The context scheme value with ID {ctx_scheme_value_id} was not found. Please check the ID and try again."
                    )
                return DeleteCtxSchemeValueResponse(ctx_scheme_value_id=ctx_scheme_value_id)
            case DeclinedElicitation():
                return DeleteCtxSchemeValueResponse(ctx_scheme_value_id=None, message="Deletion declined by user")
            case CancelledElicitation():
                return DeleteCtxSchemeValueResponse(ctx_scheme_value_id=None, message="Deletion cancelled by user")

        raise ToolError("Deletion could not be completed.")
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to delete context scheme value {ctx_scheme_value_id}.") from exc


@mcp.tool(
    name="update_context_scheme",
    description="Update an existing context scheme",
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
    ctx_scheme_id: Annotated[int, Field(gt=0, description="Unique identifier of the context scheme to update.")],
    scheme_id: Annotated[str | None, Field(default=None, description="Updated external identification of the scheme. Omit to leave unchanged.")],
    scheme_name: Annotated[str | None, Field(default=None, description="Updated pretty print name of the context scheme. Omit to leave unchanged.")],
    description: Annotated[str | None, Field(default=None, description="Updated description of the context scheme. Omit to leave unchanged.")],
    scheme_agency_id: Annotated[str | None, Field(default=None, description="Updated agency identifier maintaining the scheme. Omit to leave unchanged.")],
    scheme_version_id: Annotated[str | None, Field(default=None, description="Updated version identifier of the scheme. Omit to leave unchanged.")],
    ctx_category_id: Annotated[int | None, Field(default=None, ge=1, description="Updated associated context category ID. Omit to leave unchanged.")],
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
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
    try:
        result = await ctx_scheme_service.update(
            ctx_scheme_id=ctx_scheme_id,
            scheme_id=UNSET if scheme_id is None else scheme_id,
            scheme_name=UNSET if scheme_name is None else scheme_name,
            description=UNSET if description is None else description,
            scheme_agency_id=UNSET if scheme_agency_id is None else scheme_agency_id,
            scheme_version_id=UNSET if scheme_version_id is None else scheme_version_id,
            ctx_category_id=UNSET if ctx_category_id is None else ctx_category_id,
        )
        if result is None:
            raise ToolError(f"The context scheme with ID {ctx_scheme_id} was not found. Please check the ID and try again.")
        updated_id, updates = result
        return UpdateCtxSchemeResponse(ctx_scheme_id=updated_id, updates=updates)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to update context scheme {ctx_scheme_id}.") from exc


@mcp.tool(
    name="delete_context_scheme",
    description="Delete a context scheme and its associated context scheme values",
    output_schema={
        "type": "object",
        "description": "Response containing the deleted context scheme ID or cancellation message",
        "properties": {
            "ctx_scheme_id": {"type": ["integer", "null"], "description": "Unique identifier of the deleted context scheme (null if deletion was cancelled)", "example": 123},
            "message": {"type": ["string", "null"], "description": "Optional message indicating the status of the deletion operation", "example": "Deletion cancelled by user"}
        },
        "required": []
    }
)
async def delete_context_scheme(
    ctx_scheme_id: Annotated[int, Field(gt=0, description="Unique identifier of the context scheme to delete.")],
    ctx: Context,
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
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
    try:
        row = await ctx_scheme_service.get(ctx_scheme_id)
        if row is None:
            raise ToolError(f"The context scheme with ID {ctx_scheme_id} was not found. Please check the ID and try again.")

        scheme_name = row.scheme_name or row.scheme_id

        # Create confirmation message with context scheme details.
        elicit_result = await ctx.elicit(
            message=(
                f"Are you sure you want to discard '{scheme_name}' context scheme?\n\n"
                "It will be permanently removed along with all associated context scheme values.\n"
            ),
            response_type=None,
        )

        # Check whether the user confirmed the deletion.
        match elicit_result:
            case AcceptedElicitation():
                deleted = await ctx_scheme_service.delete(ctx_scheme_id)
                if not deleted:
                    raise ToolError(f"The context scheme with ID {ctx_scheme_id} was not found. Please check the ID and try again.")
                return DeleteCtxSchemeResponse(ctx_scheme_id=ctx_scheme_id)
            case DeclinedElicitation():
                return DeleteCtxSchemeResponse(ctx_scheme_id=None, message="Deletion declined by user")
            case CancelledElicitation():
                return DeleteCtxSchemeResponse(ctx_scheme_id=None, message="Deletion cancelled by user")

        raise ToolError("Deletion could not be completed.")
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to delete context scheme {ctx_scheme_id}.") from exc


def _build_ctx_scheme_service(
    session: AsyncSession,
    requester: AuthenticatedUser,
) -> CtxSchemeService:
    """Construct the context-scheme service for an MCP request."""
    plugin = get_vendor_plugin()
    return CtxSchemeService(
        plugin.create_ctx_scheme_repository(session),
        requester=requester,
        account_service_repo=plugin.create_app_user_repository(session),
    )


def _to_list_response(*, items: list[Any], total: int, offset: int, limit: int) -> GetCtxSchemePaginationResponse:
    """Build the paginated MCP response model."""
    return GetCtxSchemePaginationResponse(
        total_items=total,
        offset=offset,
        limit=limit,
        items=[CtxSchemeListEntryResponse.model_validate(item, from_attributes=True) for item in items],
    )
