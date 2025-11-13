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

import logging
from typing import Annotated

from fastapi import HTTPException
from fastmcp import FastMCP
from fastmcp.exceptions import ToolError
from pydantic import Field

from services import NamespaceService, DateRangeParams, PaginationParams
from tools import _validate_auth_and_db, parse_order_by_to_sorts, _create_user_info
from tools.models.namespace import GetNamespaceResponse, GetNamespacesResponse
from tools.models.common import LibraryInfo, WhoAndWhen
from tools.utils import parse_date_range, str_to_bool

# Configure logging
logger = logging.getLogger(__name__)

mcp = FastMCP("Score MCP Server - Namespace Tools")


@mcp.tool(
    name="get_namespaces",
    description="Get a paginated list of namespaces. Namespaces are globally unique identifiers that work across systems, standards, and organizations. Boolean parameters accept both their native types and string representations (strings are automatically converted).",
    output_schema={
        "type": "object",
        "description": "Response containing paginated list of namespaces. Namespaces are globally unique identifiers that work across systems, standards, and organizations. Boolean parameters accept both their native types and string representations (strings are automatically converted to the appropriate type).",
        "properties": {
            "total_items": {"type": "integer", "description": "Total number of namespaces available. Allowed values: non-negative integers (≥0).", "example": 10},
            "offset": {"type": "integer", "description": "Offset of the first item in this page. Allowed values: non-negative integers (≥0). Default value: 0.", "example": 0},
            "limit": {"type": "integer", "description": "Number of items returned in this page. Allowed values: integers between 1 and 100 (inclusive). Default value: 10.", "example": 10},
            "items": {
                "type": "array",
                "description": "List of namespaces on this page",
                "items": {
                    "type": "object",
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
                        "uri": {"type": "string", "description": "Namespace URI (Uniform Resource Identifier) - globally unique identifier that works across systems, standards, and organizations", "example": "http://www.openapplications.org/oagis/10"},
                        "prefix": {"type": ["string", "null"], "description": "Namespace prefix - short identifier used to reference the namespace URI", "example": "oagis"},
                        "description": {"type": ["string", "null"], "description": "Description of the namespace and its purpose", "example": "OAGIS namespace for business documents"},
                        "is_std_nmsp": {"type": "boolean", "description": "Whether this namespace is reserved for standard use (e.g., OAGIS namespace). If true, end users cannot use this namespace for their end user Core Components", "example": True},
                        "owner": {
                            "type": "object",
                            "description": "User information about the owner of the namespace",
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
                            "description": "Information about the creation of the namespace",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who created the namespace",
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
                            "description": "Information about the last update of the namespace",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who last updated the namespace",
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
                    "required": ["namespace_id", "library", "uri", "is_std_nmsp", "owner", "created", "last_updated"]
                }
            }
        },
        "required": ["total_items", "offset", "limit", "items"]
    }
)
async def get_namespaces(
        library_id: Annotated[int, Field(
            description="Filter by library ID using exact match.",
            examples=[123, 456, 789],
            gt=0,
            title="Library ID"
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
        uri: Annotated[str | None, Field(
            description="Filter by URI using partial match (case-insensitive).",
            examples=["http://www.openapplications.org/oagis", "urn:oasis:names:specification", "http://www.w3.org"],
            title="URI"
        )] = None,
        prefix: Annotated[str | None, Field(
            description="Filter by prefix using partial match (case-insensitive).",
            examples=["oagis", "ubl", "iso"],
            title="Prefix"
        )] = None,
        is_std_nmsp: Annotated[bool | str | None, Field(
            description="Filter by standard namespace flag. Standard namespaces are reserved for standard use (e.g., OAGIS namespace) and end users cannot use them for their end user Core Components. Accepts bool, str, or None. String values are converted: 'True'/'true'/'1' -> True, 'False'/'false'/'0' -> False.",
            examples=[True, False, "1", "0", "true", "false", "True", "False"],
            title="Is Standard Namespace"
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
            description="Comma-separated list of properties to order results by. Prefix with '-' for descending, '+' for ascending (default ascending). Allowed columns: uri, prefix, is_std_nmsp, creation_timestamp, last_update_timestamp. Example: '-creation_timestamp,+uri' translates to 'creation_timestamp DESC, uri ASC'.",
            examples=["-creation_timestamp,+uri", "uri", "-last_update_timestamp"],
            title="Order By"
        )] = None
) -> GetNamespacesResponse:
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
        library_id (int): Filter by library ID using exact match.
        offset (int | None, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int | None, optional): The maximum number of items to return. Must be a non-negative number. Defaults to 10.
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
        GetNamespacesResponse: Response object containing:
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()
    
    # Convert string parameters to their proper types
    try:
        is_std_nmsp = str_to_bool(is_std_nmsp)
    except ToolError:
        raise  # Re-raise ToolError as-is
    except Exception as e:
        raise ToolError(
            f"Type conversion error: {str(e)}. Please check your parameter types and try again."
        ) from e

    # Create service instance
    namespace_service = NamespaceService()

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
                f"Valid columns: {", ".join(namespace_service.allowed_columns_for_order_by)}") from e

    # Get namespaces
    try:
        page = namespace_service.get_namespaces(
            library_id=library_id,
            uri=uri,
            prefix=prefix,
            is_std_nmsp=is_std_nmsp,
            created_on_params=created_on_params,
            last_updated_on_params=last_updated_on_params,
            pagination=pagination,
            sort_list=sort_list
        )

        return GetNamespacesResponse(
            total_items=page.total,
            offset=page.offset,
            limit=page.limit,
            items=[_create_namespace_result(namespace) for namespace in page.items]
        )
    except HTTPException as e:
        logger.error(f"HTTP error retrieving namespaces: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving namespaces: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the namespaces: {str(e)}. Please contact your system administrator.") from e


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
                        "uri": {"type": "string", "description": "Namespace URI (Uniform Resource Identifier) - globally unique identifier that works across systems, standards, and organizations", "example": "http://www.openapplications.org/oagis/10"},
                        "prefix": {"type": ["string", "null"], "description": "Namespace prefix - short identifier used to reference the namespace URI", "example": "oagis"},
                        "description": {"type": ["string", "null"], "description": "Description of the namespace and its purpose", "example": "OAGIS namespace for business documents"},
            "is_std_nmsp": {"type": "boolean", "description": "Whether this is a standard namespace", "example": True},
            "owner": {
                "type": "object",
                "description": "User information about the owner of the namespace",
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
                "description": "Information about the creation of the namespace",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who created the namespace",
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
                "description": "Information about the last update of the namespace",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who last updated the namespace",
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
        "required": ["namespace_id", "library", "uri", "is_std_nmsp", "owner", "created", "last_updated"]
    }
)
async def get_namespace(
        namespace_id: Annotated[int, Field(
            description="Unique numeric identifier of the namespace to retrieve.",
            examples=[123, 456, 789],
            gt=0,
            title="Namespace ID"
        )]
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Get namespace
    try:
        service = NamespaceService()
        namespace = service.get_namespace(namespace_id)

        return _create_namespace_result(namespace)
    except HTTPException as e:
        logger.error(f"HTTP error retrieving namespace: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The namespace with ID {namespace_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving namespace: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the namespace: {str(e)}. Please contact your system administrator.") from e


def _create_namespace_result(namespace) -> GetNamespaceResponse:
    """
    Create a namespace result from a Namespace model instance.
    
    Args:
        namespace: Namespace model instance with loaded relationships
        
    Returns:
        GetNamespaceResponse: Formatted namespace result
    """
    return GetNamespaceResponse(
        namespace_id=namespace.namespace_id,
        library=LibraryInfo(library_id=namespace.library.library_id, name=namespace.library.name),
        uri=namespace.uri,
        prefix=namespace.prefix,
        description=namespace.description,
        is_std_nmsp=namespace.is_std_nmsp,
        owner=_create_user_info(namespace.owner),
        created=WhoAndWhen(who=_create_user_info(namespace.creator), when=namespace.creation_timestamp),
        last_updated=WhoAndWhen(who=_create_user_info(namespace.last_updater), when=namespace.last_update_timestamp)
    )
