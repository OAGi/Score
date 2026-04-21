"""
MCP Tools for managing Namespace operations in connectCenter.

This module provides Model Context Protocol (MCP) tools for querying and retrieving
Namespaces, which provide unique identification and scoping for components and entities.
Namespaces are globally unique identifiers that work across systems, standards, and organizations.
They use Uniform Resource Identifiers (URIs)
and prefixes to create globally unique identifiers for components, preventing naming conflicts
and enabling interoperability across different systems, standards, and libraries.

Namespaces enable global component identification and disambiguation by providing a mechanism
to distinguish components from different sources, standards bodies, or organizations regardless
of the system or platform in which they are used. This allows components from different
standards organizations (such as UN/CEFACT, OAGi, or industry-specific standards) to coexist
and be referenced unambiguously across different systems and implementations. Libraries may
manage multiple namespaces, with the standard namespace serving as the primary namespace for
the library's standard components. Standard namespaces are reserved exclusively for standard
use (e.g., OAGIS namespace) and cannot be used by end users for their custom Core Components.
Custom namespaces can be created for organization-specific components. The tools provide a
standardized MCP interface, enabling clients to interact with Namespace data programmatically.

Available Tools:
- get_namespaces: Retrieve paginated lists of namespaces with optional filters for
  library_id, uri, prefix, and is_std_nmsp flag. Supports date range filtering and
  custom sorting.

- get_namespace: Retrieve a single namespace by its ID, including library, owner,
  creator, and last_updater information.

- create_namespace: Create a namespace in a target library.

- update_namespace: Update a namespace owned by the requester.

- transfer_namespace_ownership: Transfer a namespace to another user by login ID.

- discard_namespace: Permanently delete a namespace that is not in use.

Key Features:
- Full relationship loading (library, owner, creator, last_updater)
- Support for filtering, pagination, and sorting
- Filter by standard namespace flag
- Date range filtering for creation and last update timestamps
- Comprehensive error handling and validation
- Structured response models with detailed metadata

The tools provide a clean, consistent interface for accessing Namespace data through the MCP protocol.
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
from app.services.namespace_service import NamespaceService
from app.tools import _to_tool_error, get_tool_authenticated_user, str_to_bool, tool_session
from app.tools.models.namespace import (
    CreateNamespaceResponse,
    GetNamespacePaginationResponse,
    GetNamespaceResponse,
    NamespaceResponseEntry,
    TransferNamespaceOwnershipResponse,
    UpdateNamespaceResponse,
)

logger = logging.getLogger("connectcenter.mcp.namespace")

mcp = FastMCP("connectCenter MCP - Namespace Tools")

EMPTY_OUTPUT_SCHEMA = {
    "type": "object",
    "description": "Empty response body.",
    "properties": {},
    "additionalProperties": False,
}


async def get_namespace_service(
    session: AsyncSession = Depends(tool_session),
) -> NamespaceService:
    """Provide a requester-scoped namespace service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    return _build_namespace_service(session, requester)


@mcp.tool(
    name="get_namespaces",
    description="Get a paginated list of namespaces. Namespaces are globally unique identifiers that work across systems, standards, and organizations.",
    output_schema={
        "type": "object",
        "description": "Response containing paginated list of namespaces. Namespaces are globally unique identifiers that work across systems, standards, and organizations.",
        "properties": {
            "total_items": {"type": "integer",
                            "description": "Total number of namespaces available. Allowed values: non-negative integers (≥0).",
                            "example": 10},
            "offset": {"type": "integer",
                       "description": "Offset of the first item in this page. Allowed values: non-negative integers (≥0). Default value: 0.",
                       "example": 0},
            "limit": {"type": "integer",
                      "description": "Number of items returned in this page. Allowed values: integers between 1 and 100 (inclusive). Default value: 10.",
                      "example": 10},
            "items": {
                "type": "array",
                "description": "List of namespaces on this page",
                "items": {
                    "type": "object",
                    "properties": {
                        "namespace_id": {"type": "integer", "description": "Unique identifier for the namespace",
                                         "example": 1},
                        "library": {
                            "type": "object",
                            "description": "Library information",
                            "properties": {
                                "library_id": {"type": "integer", "description": "Unique identifier for the library",
                                               "example": 1},
                                "name": {"type": "string", "description": "Library name", "example": "connectSpec"}
                            },
                            "required": ["library_id", "name"]
                        },
                        "uri": {"type": "string",
                                "description": "Namespace URI (Uniform Resource Identifier) - globally unique identifier that works across systems, standards, and organizations",
                                "example": "http://www.openapplications.org/oagis/10"},
                        "prefix": {"type": ["string", "null"],
                                   "description": "Namespace prefix - short identifier used to reference the namespace URI",
                                   "example": "oagis"},
                        "description": {"type": ["string", "null"],
                                        "description": "Description of the namespace and its purpose",
                                        "example": "OAGIS namespace for business documents"},
                        "is_std_nmsp": {"type": "boolean",
                                        "description": "Whether this namespace is reserved for standard use (e.g., OAGIS namespace). If true, end users cannot use this namespace for their end user Core Components",
                                        "example": True},
                        "owner": {
                            "type": "object",
                            "description": "User information about the owner of the namespace",
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
                            "description": "Information about the creation of the namespace",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who created the namespace",
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
                            "description": "Information about the last update of the namespace",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who last updated the namespace",
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
                                         "example": "2024-01-20T14:45:00Z"}
                            },
                            "required": ["who", "when"]
                        }
                    },
                    "required": ["namespace_id", "library", "uri", "is_std_nmsp", "owner", "created", "last_updated"]
                }
            }
        },
        "required": ["total_items", "offset", "limit", "items"]
    }
)
async def get_namespaces(
    library_id: Annotated[int, Field(gt=0, description="Filter by library ID using exact match.")],
    uri: Annotated[str | None, Field(default=None, description="Filter by URI using partial match (case-insensitive).")],
    prefix: Annotated[str | None, Field(default=None, description="Filter by prefix using partial match (case-insensitive).")],
    is_std_nmsp: Annotated[bool | str | None, Field(default=None, description="Filter by standard namespace flag.")],
    created_on: Annotated[str | None, Field(default=None, description="Filter by creation date using an inclusive range: '[before~after]'.")],
    last_updated_on: Annotated[str | None, Field(default=None, description="Filter by last update date using an inclusive range: '[before~after]'.")],
    order_by: Annotated[str | None, Field(default=None, description="Comma-separated list of properties to order results by. Allowed columns: uri, prefix, is_std_nmsp, creation_timestamp, last_update_timestamp.")],
    offset: Annotated[int, Field(default=0, ge=0, description="The offset from the beginning of the list.")],
    limit: Annotated[int, Field(default=10, ge=1, le=100, description="The maximum number of items to return.")],
    namespace_service: NamespaceService = Depends(get_namespace_service),
) -> GetNamespacePaginationResponse:
    """
    Get a paginated list of namespaces.

    Namespaces are globally unique identifiers that work across systems, standards, and
    organizations. They use URIs and
    prefixes to create globally unique identifiers for components, preventing naming conflicts
    and enabling interoperability across different systems and implementations.

    This function retrieves namespaces with support for pagination, filtering, and sorting.
    It returns detailed information about each namespace including creation and update metadata,
    and namespace-specific attributes.

    Args:
        library_id (int): Filter by library ID using exact match (required).
        uri (str | None, optional): Filter by URI using partial match (case-insensitive). Defaults to None.
        prefix (str | None, optional): Filter by prefix using partial match (case-insensitive). Defaults to None.
        is_std_nmsp (bool | str | None, optional): Filter by standard namespace flag. Standard namespaces are reserved for standard use (e.g., OAGIS namespace) and end users cannot use them for their end user Core Components. Accepts bool, str ('True'/'true'/'1' for True, 'False'/'false'/'0' for False), or None. Defaults to None.
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
            Allowed columns: uri, prefix, is_std_nmsp, creation_timestamp, last_update_timestamp.
            Example: '-creation_timestamp,+uri' translates to 'creation_timestamp DESC, uri ASC'.
            Defaults to None.

    Returns:
        GetNamespacePaginationResponse: Response object containing:
            - total_items: Total number of namespaces available
            - offset: Offset of the first item in this page
            - limit: Number of items returned in this page
            - items: List of namespaces on this page with detailed information

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
        >>> result = get_namespaces(library_id=123, offset=0, limit=10)
        >>> print(f"Found {result.total_items} namespaces")

        Filtered search:
        >>> result = get_namespaces(
        ...     library_id=123,
        ...     is_std_nmsp=True,
        ...     limit=5
        ... )

        Date range filtering:
        >>> result = get_namespaces(
        ...     library_id=123,
        ...     created_on="[2024-01-01~2024-12-31]"
        ... )

        Custom ordering:
        >>> result = get_namespaces(
        ...     library_id=123,
        ...     order_by="-creation_timestamp,+uri"
        ... )
    """
    try:
        is_std_nmsp = str_to_bool(is_std_nmsp)
    except ToolError:
        raise
    except Exception as e:
        raise ToolError(
            f"Type conversion error: {str(e)}. Please check your parameter types and try again."
        ) from e

    try:
        page = await namespace_service.list(
            limit=limit,
            offset=offset,
            order_by=order_by,
            library_id=library_id,
            uri=uri,
            prefix=prefix,
            is_std_nmsp=is_std_nmsp,
            created_on=parse_date_range(created_on),
            last_updated_on=parse_date_range(last_updated_on),
        )
        return _to_list_response(items=page.items, total=page.total, offset=page.offset, limit=page.limit)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to retrieve namespaces.") from exc


@mcp.tool(
    name="get_namespace",
    description="Get a specific namespace by ID. Namespaces are globally unique identifiers that work across systems, standards, and organizations.",
    output_schema={
        "type": "object",
        "description": "Response containing namespace information. Namespaces are globally unique identifiers that work across systems, standards, and organizations.",
        "properties": {
            "namespace_id": {"type": "integer", "description": "Unique identifier for the namespace", "example": 1},
            "library": {
                "type": "object",
                "description": "Library information",
                "properties": {
                    "library_id": {"type": "integer", "description": "Unique identifier for the library", "example": 1},
                    "name": {"type": "string", "description": "Library name", "example": "connectSpec"}
                },
                "required": ["library_id", "name"]
            },
            "uri": {"type": "string",
                    "description": "Namespace URI (Uniform Resource Identifier) - globally unique identifier that works across systems, standards, and organizations",
                    "example": "http://www.openapplications.org/oagis/10"},
            "prefix": {"type": ["string", "null"],
                       "description": "Namespace prefix - short identifier used to reference the namespace URI",
                       "example": "oagis"},
            "description": {"type": ["string", "null"], "description": "Description of the namespace and its purpose",
                            "example": "OAGIS namespace for business documents"},
            "is_std_nmsp": {"type": "boolean",
                            "description": "Whether this namespace is reserved for standard use (e.g., OAGIS namespace). If true, end users cannot use this namespace for their end user Core Components",
                            "example": True},
            "owner": {
                "type": "object",
                "description": "User information about the owner of the namespace",
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
                "description": "Information about the creation of the namespace",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who created the namespace",
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
                "description": "Information about the last update of the namespace",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who last updated the namespace",
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
                             "example": "2024-01-20T14:45:00Z"}
                },
                "required": ["who", "when"]
            }
        },
        "required": ["namespace_id", "library", "uri", "is_std_nmsp", "owner", "created", "last_updated"]
    }
)
async def get_namespace(
    namespace_id: Annotated[int, Field(gt=0, description="Unique numeric identifier of the namespace to retrieve.")],
    namespace_service: NamespaceService = Depends(get_namespace_service),
) -> GetNamespaceResponse:
    """
    Get a specific namespace by ID.

    Namespaces are globally unique identifiers that work across systems, standards, and
    organizations. They use URIs and
    prefixes to create globally unique identifiers for components, preventing naming conflicts
    and enabling interoperability across different systems and implementations.

    This function retrieves a single namespace and returns detailed information including
    metadata about who created it and when it was last updated, along with namespace-specific
    attributes and library information.

    Args:
        namespace_id (int): The unique identifier of the namespace to fetch

    Returns:
        GetNamespaceResponse: Response object containing:
            - namespace_id: Unique identifier for the namespace
            - library: Library information object with library_id and name
            - uri: URI of the namespace
            - prefix: Default short name for the URI (if any)
            - description: Description of the namespace (if any)
            - is_std_nmsp: Whether the namespace is reserved for standard use (e.g., OAGIS namespace). If true, end users cannot use this namespace for their end user Core Components
            - owner: User information about the owner of the namespace
            - created: Information about the creation of the namespace
            - last_updated: Information about the last update of the namespace

    Raises:
        ToolError: If validation fails, the namespace is not found, or other errors occur.
            Common error scenarios include:
            - Namespace with the specified ID does not exist
            - Database connection issues
            - Authentication failures

    Examples:
        Get a specific namespace:
        >>> result = get_namespace(namespace_id=123)
        >>> print(f"Namespace URI: {result.uri}")
        >>> print(f"Prefix: {result.prefix}")
        >>> print(f"Owner: {result.owner.username}")

        Access namespace details:
        >>> result = get_namespace(namespace_id=123)
        >>> if result.description:
        ...     print(f"Description: {result.description}")
        >>> print(f"Is standard: {result.is_std_nmsp}")
    """
    try:
        row = await namespace_service.get(namespace_id)
        if row is None:
            raise ValueError(f"The namespace with ID {namespace_id} was not found. Please check the ID and try again.")
        return GetNamespaceResponse.model_validate(row, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to retrieve namespace {namespace_id}.") from exc


@mcp.tool(
    name="create_namespace",
    description="Create a namespace in a target library.",
    output_schema={
        "type": "object",
        "description": "Response containing the created namespace identifier.",
        "properties": {
            "namespace_id": {"type": "integer", "description": "Created namespace identifier.", "example": 101}
        },
        "required": ["namespace_id"],
    },
)
async def create_namespace(
    library_id: Annotated[int, Field(gt=0, description="Owning library identifier.")],
    uri: Annotated[str, Field(min_length=1, description="Namespace URI.")],
    prefix: Annotated[
        str | None,
        Field(default=None, description="Namespace prefix. Pass an empty string to store a blank prefix."),
    ] = None,
    description: Annotated[str | None, Field(default=None, description="Namespace description.")] = None,
    namespace_service: NamespaceService = Depends(get_namespace_service),
) -> CreateNamespaceResponse:
    """Create a namespace."""
    try:
        result = await namespace_service.create_namespace(
            library_id=library_id,
            uri=uri,
            prefix=prefix,
            description=description,
        )
        return CreateNamespaceResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to create the namespace.") from exc


@mcp.tool(
    name="update_namespace",
    description="Update a namespace owned by the requester.",
    output_schema={
        "type": "object",
        "description": "Response containing the target namespace identifier and changed fields.",
        "properties": {
            "namespace_id": {"type": "integer", "description": "Target namespace identifier.", "example": 101},
            "updates": {
                "type": "array",
                "description": "Updated field names.",
                "items": {"type": "string"},
                "example": ["uri", "prefix"],
            },
        },
        "required": ["namespace_id", "updates"],
    },
)
async def update_namespace(
    namespace_id: Annotated[int, Field(gt=0, description="Target namespace identifier.")],
    uri: Annotated[str, Field(min_length=1, description="Updated namespace URI.")],
    prefix: Annotated[
        str | None,
        Field(default=None, description="Updated namespace prefix. Pass an empty string to store a blank prefix."),
    ] = None,
    description: Annotated[str | None, Field(default=None, description="Updated namespace description.")] = None,
    namespace_service: NamespaceService = Depends(get_namespace_service),
) -> UpdateNamespaceResponse:
    """Update a namespace."""
    try:
        result = await namespace_service.update_namespace(
            namespace_id=namespace_id,
            uri=uri,
            prefix=prefix,
            description=description,
        )
        return UpdateNamespaceResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to update namespace {namespace_id}.") from exc


@mcp.tool(
    name="transfer_namespace_ownership",
    description="Transfer namespace ownership to another user by login ID.",
    output_schema={
        "type": "object",
        "description": "Response containing the target namespace identifier and changed fields.",
        "properties": {
            "namespace_id": {"type": "integer", "description": "Target namespace identifier.", "example": 101},
            "updates": {
                "type": "array",
                "description": "Updated field names.",
                "items": {"type": "string"},
                "example": ["owner_user_id"],
            },
        },
        "required": ["namespace_id", "updates"],
    },
)
async def transfer_namespace_ownership(
    namespace_id: Annotated[int, Field(gt=0, description="Target namespace identifier.")],
    target_login_id: Annotated[str, Field(min_length=1, description="Login ID of the new owner.")],
    ctx: Context,
    namespace_service: NamespaceService = Depends(get_namespace_service),
) -> TransferNamespaceOwnershipResponse:
    """Transfer namespace ownership after confirmation."""
    row = await namespace_service.get(namespace_id)
    if row is None:
        raise ToolError(
            f"The namespace with ID {namespace_id} was not found. Please check the ID and try again."
        )

    elicit_result = await ctx.elicit(
        message=(
            f"Are you sure you want to transfer ownership of namespace '{row.uri}' "
            f"to {target_login_id.strip()}?"
        ),
        response_type=None,
    )
    match elicit_result:
        case AcceptedElicitation():
            pass
        case DeclinedElicitation():
            raise ToolError("Namespace ownership transfer declined by user.")
        case CancelledElicitation():
            raise ToolError("Namespace ownership transfer cancelled by user.")

    try:
        payload = await namespace_service.transfer_namespace_ownership(
            namespace_id=namespace_id,
            target_login_id=target_login_id,
        )
        return TransferNamespaceOwnershipResponse.model_validate(payload, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to transfer ownership of namespace {namespace_id}.") from exc


@mcp.tool(
    name="discard_namespace",
    description="Discard a namespace permanently when it is not in use.",
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def discard_namespace(
    namespace_id: Annotated[int, Field(gt=0, description="Target namespace identifier.")],
    ctx: Context,
    namespace_service: NamespaceService = Depends(get_namespace_service),
) -> dict[str, object]:
    """Discard a namespace after confirmation."""
    row = await namespace_service.get(namespace_id)
    if row is None:
        raise ToolError(
            f"The namespace with ID {namespace_id} was not found. Please check the ID and try again."
        )

    elicit_result = await ctx.elicit(
        message=(
            f"Are you sure you want to discard namespace '{row.uri}' permanently?\n\n"
            "This removes the namespace record and cannot be undone."
        ),
        response_type=None,
    )
    match elicit_result:
        case AcceptedElicitation():
            pass
        case DeclinedElicitation():
            raise ToolError("Namespace discard was not confirmed.")
        case CancelledElicitation():
            raise ToolError("Namespace discard was cancelled.")

    try:
        await namespace_service.discard_namespace(namespace_id=namespace_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to discard namespace {namespace_id}.") from exc


def _build_namespace_service(
    session: AsyncSession,
    requester: AuthenticatedUser,
) -> NamespaceService:
    """Construct the namespace service for an MCP request."""
    plugin = get_vendor_plugin()
    return NamespaceService(
        plugin.create_namespace_repository(session),
        plugin.create_app_user_repository(session),
        requester=requester,
    )


def _to_list_response(*, items: list[Any], total: int, offset: int, limit: int) -> GetNamespacePaginationResponse:
    """Build the paginated MCP response model."""
    return GetNamespacePaginationResponse(
        total_items=total,
        offset=offset,
        limit=limit,
        items=[NamespaceResponseEntry.model_validate(item, from_attributes=True) for item in items],
    )
