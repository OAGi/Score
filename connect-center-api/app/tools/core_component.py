"""
MCP Tools for managing Core Component operations in connectCenter.

This module provides Model Context Protocol (MCP) tools for querying and retrieving
Core Components, which are the fundamental building blocks of business information
standards. Core Components serve as reusable, context-independent data structures that
can be assembled to create Business Information Entities (BIEs) for specific business
contexts. They follow the UN/CEFACT Core Component Technical Specification (CCTS) methodology,
providing a standardized approach to modeling business information.

Core Components include three main types: ACCs (Aggregate Core Components) which represent
complex data structures composed of other components, ASCCPs (Association Core Component
Properties) which define associations between aggregates, and BCCPs (Basic Core Component
Properties) which represent simple data elements with associated data types. These components
enable the creation of standardized, interoperable business documents and messages by
providing a library of reusable data structures that can be combined and contextualized
for specific business needs. The tools provide a standardized MCP interface, enabling
clients to interact with Core Component data programmatically.

Available Tools:
- get_core_components: Retrieve paginated lists of Core Components (ACCs, ASCCPs, or BCCPs)
  filtered by release with optional filters. Supports custom sorting and pagination.

- get_acc: Retrieve a single ACC (Aggregate Core Component) by its manifest ID, including
  all related entities (namespace, creator, owner, release, log, based_acc_manifest) and
  all associated ASCCs and BCCs with their relationships.

- get_asccp: Retrieve a single ASCCP (Association Core Component Property) by its manifest ID,
  including all related entities and the role_of_acc relationship.

- get_bccp: Retrieve a single BCCP (Basic Core Component Property) by its manifest ID, including
  all related entities and the associated data type.

Key Features:
- Full relationship loading including nested component hierarchies
- Support for complex relationship traversal (ASCCs, BCCs, based components)
- Support for filtering, pagination, and sorting
- Automatic loading of associated data types for BCCPs
- Comprehensive error handling and validation
- Structured response models with detailed metadata and component hierarchies

Component Hierarchy:
Core Components form a hierarchical structure:
- ACCs contain ASCCs (which reference ASCCPs) and BCCs (which reference BCCPs)
- ASCCPs reference ACCs (role_of_acc)
- BCCPs reference Data Types (dt_id)
- Components can be based on other components (based_* relationships)

The tools provide a clean, consistent interface for accessing Core Component data through the MCP protocol.
All operations require proper authentication and authorization.
"""

from __future__ import annotations

import logging
from typing import Annotated, Any, Literal

from fastmcp import FastMCP
from fastmcp.dependencies import Depends
from pydantic import Field
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.vendor_plugins import get_vendor_plugin
from app.routes.models.core_component import CoreComponentListEntry
from app.routes.utils.date import parse_date_range
from app.security import AuthenticatedUser
from app.services.core_component_service import CoreComponentService
from app.services.release_service import ReleaseService
from app.tools import _to_tool_error, get_tool_authenticated_user, tool_session
from app.tools.models.core_component import (
    GetAccResponse,
    GetAsccpResponse,
    GetBccpResponse,
    GetCoreComponentPaginationResponse,
)

logger = logging.getLogger("connectcenter.mcp.core_component")

mcp = FastMCP("connectCenter MCP - Core Component Tools")


async def get_core_component_service(
    session: AsyncSession = Depends(tool_session),
) -> CoreComponentService:
    """Provide a requester-scoped core-component service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    return _build_core_component_service(session, requester)


@mcp.tool(
    name="get_core_components",
    description="Get a paginated list of core components (ACC: Aggregation Core Component, ASCCP: Association Core Component Property, BCCP: Basic Core Component Property) with unified response format",
    output_schema={
        "type": "object",
        "description": "Response containing paginated list of core components with unified format",
        "properties": {
            "total_items": {"type": "integer", "description": "Total number of core components available. Allowed values: non-negative integers (≥0).", "example": 150},
            "offset": {"type": "integer", "description": "Offset of the first item in this page. Allowed values: non-negative integers (≥0). Default value: 0.", "example": 0},
            "limit": {"type": "integer", "description": "Number of items returned in this page. Allowed values: integers between 1 and 100 (inclusive). Default value: 10.", "example": 10},
            "items": {
                "type": "array",
                "description": "List of core components on this page",
                "items": {
                    "type": "object",
                    "properties": {
                        "component_type": {"type": "string", "enum": ["ACC", "ASCCP", "BCCP"], "description": "Type of component (ACC: Aggregation Core Component, ASCCP: Association Core Component Property, BCCP: Basic Core Component Property)", "example": "ASCCP"},
                        "manifest_id": {"type": "integer", "description": "Unique identifier for the component manifest", "example": 12345},
                        "component_id": {"type": "integer", "description": "Unique identifier for the component", "example": 6789},
                        "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                        "den": {"type": "string", "description": "Dictionary Entry Name (DEN) - the standardized name of the component as defined by CCTS v3, uniquely identifying the component within its namespace", "example": "Purchase Order. Details"},
                        "name": {"type": ["string", "null"], "description": "Component name derived from the object_class_term (for ACC) or property_term (for ASCCP/BCCP) as specified in CCTS v3, intended for user interface display and general communication", "example": "Purchase Order"},
                        "definition": {"type": ["string", "null"], "description": "Description of the component", "example": "A document used to request goods or services from a supplier"},
                        "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://www.oagis.org"},
                        "is_deprecated": {"type": "boolean", "description": "Whether the component is deprecated", "example": False},
                        "state": {"type": "string", "enum": ["Deleted", "WIP", "Draft", "QA", "Candidate", "Production", "ReleaseDraft", "Published"], "description": "Component lifecycle state. WIP: Work in Progress, owner can edit. Draft: Developer component awaiting review. QA: Quality Assurance, end-user component awaiting review. Candidate: Approved for new release consideration. Production: Final stable state for end-user components. ReleaseDraft: Prepared for publication in upcoming release. Published: Officially released in a new version", "example": "Published"},
                        "tag": {"type": ["string", "null"], "description": "Tag name associated with the component (e.g., BOD: Business Object Document)", "example": "BOD"},
                        "namespace": {
                            "type": ["object", "null"],
                            "description": "Namespace information",
                            "properties": {
                                "namespace_id": {"type": "integer", "description": "Unique identifier for the namespace", "example": 1},
                                "prefix": {"type": ["string", "null"], "description": "Namespace prefix", "example": "oagis"},
                                "uri": {"type": "string", "description": "Namespace URI (Uniform Resource Identifier)", "example": "http://www.openapplications.org/oagis/10"}
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
                                "release_num": {"type": ["string", "null"], "description": "Release number", "example": "10.6"},
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
                            "description": "User information about the owner of the component. The owner has full control over the component including editing, deleting, and transferring ownership. Administrators also have these permissions regardless of ownership",
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
                            "description": "Information about the creation of the component",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who created the component",
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
                            "description": "Information about the last update of the component",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who last updated the component",
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
                    "required": ["component_type", "manifest_id", "component_id", "guid", "den", "name", "state", "is_deprecated", "library", "release", "owner", "created", "last_updated"]
                }
            }
        },
        "required": ["total_items", "offset", "limit", "items"]
    }
)
async def get_core_components(
    release_id: Annotated[int, Field(gt=0, description="Filter by release ID using exact match.")],
    types: Annotated[list[Literal["ACC", "ASCCP", "BCCP"]], Field(description="Core component types to include.")],
    den: Annotated[str | None, Field(default=None, description="Filter by DEN using partial match (case-insensitive).")],
    tag: Annotated[str | None, Field(default=None, description="Filter by tag name using partial match (case-insensitive).")],
    created_on: Annotated[str | None, Field(default=None, description="Filter by creation date using an inclusive range: '[before~after]'.")],
    last_updated_on: Annotated[str | None, Field(default=None, description="Filter by last update date using an inclusive range: '[before~after]'.")],
    order_by: Annotated[str | None, Field(default=None, description="Comma-separated list of properties to order results by. Allowed columns: den, name, definition, creation_timestamp, last_update_timestamp.")],
    offset: Annotated[int, Field(default=0, ge=0, description="The offset from the beginning of the list.")],
    limit: Annotated[int, Field(default=10, ge=1, le=100, description="The maximum number of items to return.")],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> GetCoreComponentPaginationResponse:
    """
    Get a paginated list of core components (ACC, ASCCP, BCCP) with unified response format.

    Args:
        release_id (int): Filter by release ID using exact match (required).
        types (str | None, optional): Filter by core component types. Comma-separated list of allowed values: 'ACC', 'ASCCP', 'BCCP'.
            Examples: 'ASCCP', 'ACC,BCCP', 'ASCCP,ACC,BCCP'. Defaults to 'ASCCP' if not specified.
        den (str | None, optional): Filter by Dictionary Entry Name (DEN) using partial match (case-insensitive). Defaults to None.
        tag (str | None, optional): Filter by tag name using partial match (case-insensitive).
            To discover available tag names, use the get_tags() tool first. Defaults to None.
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
            Allowed columns: den, name, definition, creation_timestamp, last_update_timestamp.
            Example: '-creation_timestamp,+den' translates to 'creation_timestamp DESC, den ASC'.
            Defaults to None.
        offset (int, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int, optional): The maximum number of items to return. Must be between 1 and 100 (inclusive). Defaults to 10.

    Returns:
        GetCoreComponentPaginationResponse: Response object containing:
            - total_items: Total number of core components available
            - offset: Offset of the first item in this page
            - limit: Number of items returned in this page
            - items: List of core components on this page with unified information including:
                - component_type: Type of component ("ACC", "ASCCP", or "BCCP")
                - manifest_id: Unique identifier for the component manifest
                - component_id: Unique identifier for the component
                - guid: Unique identifier within the release
                - den: Dictionary Entry Name
                - name: Component name (object_class_term for ACC, property_term for ASCCP/BCCP)
                - definition: Description of the component
                - definition_source: URL indicating the source of the definition
                - is_deprecated: Whether the component is deprecated
                - state: State of the component
                - namespace: Namespace information (if any)
                - library: Library information with library_id and name
                - release: Release information with release_id, release_num, and state
                - log: Log information with log_id, revision_num, and revision_tracking_num (if any)
                - owner: User information about the owner of the component
                - created: Information about the creation of the component
                - last_updated: Information about the last update of the component
                - tag: Tag name associated with the component (if any)

    Raises:
        ToolError: If validation fails, parsing errors occur, or other errors happen.
            Common error scenarios include:
            - Invalid pagination parameters (negative offset, limit out of range)
            - Invalid date range format
            - Invalid order_by column names or format
            - Database connection issues
            - Authentication failures

    Examples:
        Get ASCCPs (default):
        >>> result = get_core_components(release_id=123, offset=0, limit=10)
        >>> print(f"Found {result.total_items} ASCCPs")

        Get ACCs:
        >>> result = get_core_components(release_id=123, types="ACC", offset=0, limit=10)
        >>> for component in result.items:
        ...     print(f"ACC: {component.den} (GUID: {component.guid})")

        Get multiple types:
        >>> result = get_core_components(release_id=123, types="ACC,BCCP", offset=0, limit=10)
        >>> print(f"Found {result.total_items} ACCs and BCCPs")

        Get BCCPs with filtering:
        >>> result = get_core_components(release_id=123, types="BCCP", den="Amount", offset=0, limit=10)
        >>> print(f"BCCPs with 'Amount' in den: {result.total_items}")
    """
    try:
        page = await core_component_service.list(
            release_id=release_id,
            types=list(types),
            limit=limit,
            offset=offset,
            order_by=order_by,
            den=den,
            tag=tag,
            created_on=parse_date_range(created_on),
            last_updated_on=parse_date_range(last_updated_on),
        )
        return _to_list_response(items=page.items, total=page.total, offset=page.offset, limit=page.limit)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to retrieve core components.") from exc


@mcp.tool(
    name="get_acc",
    description="Get a specific ACC by its manifest ID.",
    output_schema={
        "type": "object",
        "description": "Response containing ACC (Aggregation Core Component) information",
        "properties": {
            "acc_manifest_id": {"type": "integer", "description": "Unique identifier for the ACC manifest", "example": 12345},
            "acc_id": {"type": "integer", "description": "Unique identifier for the ACC", "example": 6789},
            "base_acc": {
                "type": ["object", "null"],
                "description": "Base ACC information",
                "properties": {
                    "acc_manifest_id": {"type": "integer", "description": "Unique identifier for the base ACC manifest", "example": 12345},
                    "acc_id": {"type": "integer", "description": "Unique identifier for the base ACC", "example": 6789},
                    "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                    "den": {"type": "string", "description": "Dictionary Entry Name (DEN) of the base ACC", "example": "Order. Details"},
                    "object_class_term": {"type": "string", "description": "Object class term of the base ACC", "example": "Order"},
                    "type": {"type": ["string", "null"], "description": "Type of the base ACC", "example": "ACC"},
                    "definition": {"type": ["string", "null"], "description": "Definition of the base ACC", "example": "A document used to request goods or services"},
                    "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://www.oagis.org"},
                    "namespace": {
                        "type": ["object", "null"],
                        "description": "Namespace information",
                        "properties": {
                            "namespace_id": {"type": "integer", "description": "Unique identifier for the namespace", "example": 1},
                            "prefix": {"type": ["string", "null"], "description": "Namespace prefix", "example": "oagis"},
                            "uri": {"type": "string", "description": "Namespace URI (Uniform Resource Identifier)", "example": "http://www.openapplications.org/oagis/10"}
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
                            "release_num": {"type": ["string", "null"], "description": "Release number", "example": "10.6"},
                            "state": {"type": "string", "description": "Release state", "example": "Published"}
                        },
                        "required": ["release_id", "release_num", "state"]
                    }
                },
                "required": ["acc_manifest_id", "acc_id", "guid", "den", "object_class_term", "type", "library", "release"]
            },
            "relationships": {
                "type": "array",
                "description": "List of relationships (ASCCs and BCCs) contained in this ACC. ASCCs relate to ASCCPs (which reference other ACCs via role_of_acc), and BCCs relate to BCCPs (which reference DTs for data types).",
                "items": {
                    "oneOf": [
                        {
                            "type": "object",
                            "description": "ASCC (Association Core Component) relationship",
                            "properties": {
                                "component_type": {"type": "string", "description": "Type of related component", "example": "ASCC"},
                                "manifest_id": {"type": "integer", "description": "Unique identifier for the ASCC manifest (computed from ascc_manifest_id)", "example": 12345},
                                "ascc_manifest_id": {"type": "integer", "description": "Unique identifier for the ASCC manifest", "example": 12345},
                                "ascc_id": {"type": "integer", "description": "Unique identifier for the ASCC", "example": 6789},
                                "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                                "den": {"type": "string", "description": "Dictionary Entry Name (DEN) of the related component", "example": "Purchase Order. Details"},
                                "cardinality_min": {"type": "integer", "description": "Minimum cardinality", "example": 0},
                                "cardinality_max": {"type": "integer", "description": "Maximum cardinality (-1 means unbounded)", "example": 1},
                                "cardinality_display": {"type": "string", "description": "Human-readable cardinality display (e.g., '0..1' or '1..unbounded')", "example": "0..1"},
                                "is_deprecated": {"type": "boolean", "description": "Whether the related component is deprecated", "example": False},
                                "definition": {"type": ["string", "null"], "description": "Definition of the related component", "example": "Details of the purchase order"},
                                "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://www.oagis.org"},
                                "from_acc": {
                                    "type": "object",
                                    "description": "Information about the ACC that contains this relationship",
                                    "properties": {
                                        "acc_manifest_id": {"type": "integer", "description": "Unique identifier for the ACC manifest", "example": 12345},
                                        "acc_id": {"type": "integer", "description": "Unique identifier for the ACC", "example": 6789},
                                        "guid": {"type": "string", "description": "Unique identifier within the release", "example": "a1b2c3d4e5f6789012345678901234ab"},
                                        "den": {"type": "string", "description": "Dictionary Entry Name (DEN) of the ACC", "example": "Purchase Order. Details"},
                                        "object_class_term": {"type": "string", "description": "Object class term of the ACC", "example": "Purchase Order"},
                                        "definition": {"type": ["string", "null"], "description": "Definition of the ACC", "example": "A document used to request goods or services"},
                                        "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://www.oagis.org"},
                                        "is_deprecated": {"type": "boolean", "description": "Whether the ACC is deprecated", "example": False}
                                    },
                                    "required": ["acc_manifest_id", "acc_id", "guid", "den", "object_class_term", "is_deprecated"]
                                },
                                "to_asccp": {
                                    "type": "object",
                                    "description": "Information about the ASCCP that this ASCC connects to",
                                    "properties": {
                                        "asccp_manifest_id": {"type": "integer", "description": "Unique identifier for the ASCCP manifest", "example": 12345},
                                        "asccp_id": {"type": "integer", "description": "Unique identifier for the ASCCP", "example": 6789},
                                        "role_of_acc_manifest_id": {"type": "integer", "description": "Unique identifier for the ACC manifest that this ASCCP plays the role of", "example": 12345},
                                        "guid": {"type": "string", "description": "Unique identifier within the release", "example": "a1b2c3d4e5f6789012345678901234ab"},
                                        "den": {"type": "string", "description": "Dictionary Entry Name (DEN) of the ASCCP", "example": "Purchase Order. Details"},
                                        "property_term": {"type": "string", "description": "Property term of the ASCCP", "example": "Details"},
                                        "definition": {"type": ["string", "null"], "description": "Definition of the ASCCP", "example": "Details of the purchase order"},
                                        "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://www.oagis.org"},
                                        "is_deprecated": {"type": "boolean", "description": "Whether the ASCCP is deprecated", "example": False}
                                    },
                                    "required": ["asccp_manifest_id", "asccp_id", "role_of_acc_manifest_id", "guid", "den", "property_term", "is_deprecated"]
                                }
                            },
                            "required": ["component_type", "manifest_id", "ascc_manifest_id", "ascc_id", "guid", "den", "cardinality_min", "cardinality_max", "cardinality_display", "is_deprecated", "from_acc", "to_asccp"]
                        },
                        {
                            "type": "object",
                            "description": "BCC (Basic Core Component) relationship",
                            "properties": {
                                "component_type": {"type": "string", "description": "Type of related component", "example": "BCC"},
                                "manifest_id": {"type": "integer", "description": "Unique identifier for the BCC manifest (computed from bcc_manifest_id)", "example": 12345},
                                "bcc_manifest_id": {"type": "integer", "description": "Unique identifier for the BCC manifest", "example": 12345},
                                "bcc_id": {"type": "integer", "description": "Unique identifier for the BCC", "example": 6789},
                                "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                                "den": {"type": "string", "description": "Dictionary Entry Name (DEN) of the related component", "example": "Purchase Order. Amount"},
                                "cardinality_min": {"type": "integer", "description": "Minimum cardinality", "example": 0},
                                "cardinality_max": {"type": "integer", "description": "Maximum cardinality (-1 means unbounded)", "example": 1},
                                "cardinality_display": {"type": "string", "description": "Human-readable cardinality display (e.g., '0..1' or '1..unbounded')", "example": "0..1"},
                                "entity_type": {"type": ["string", "null"], "enum": ["Attribute", "Element"], "description": "Entity type: 'Attribute' (XML attribute) or 'Element' (XML element)", "example": "Element"},
                                "is_nillable": {"type": "boolean", "description": "Whether the BCC can have a nil/null value", "example": False},
                                "value_constraint": {
                                    "type": ["object", "null"],
                                    "description": "Value constraint (default_value or fixed_value) for the BCC. Exactly one of default_value or fixed_value must be set.",
                                    "properties": {
                                        "default_value": {"type": ["string", "null"], "description": "Default value for the BCC if not specified", "example": "0.00"},
                                        "fixed_value": {"type": ["string", "null"], "description": "Fixed value that must always be used for this BCC", "example": "USD"}
                                    }
                                },
                                "is_deprecated": {"type": "boolean", "description": "Whether the related component is deprecated", "example": False},
                                "definition": {"type": ["string", "null"], "description": "Definition of the related component", "example": "The monetary amount of the purchase order"},
                                "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://www.oagis.org"},
                                "from_acc": {
                                    "type": "object",
                                    "description": "Information about the ACC that contains this relationship",
                                    "properties": {
                                        "acc_manifest_id": {"type": "integer", "description": "Unique identifier for the ACC manifest", "example": 12345},
                                        "acc_id": {"type": "integer", "description": "Unique identifier for the ACC", "example": 6789},
                                        "guid": {"type": "string", "description": "Unique identifier within the release", "example": "a1b2c3d4e5f6789012345678901234ab"},
                                        "den": {"type": "string", "description": "Dictionary Entry Name (DEN) of the ACC", "example": "Purchase Order. Details"},
                                        "object_class_term": {"type": "string", "description": "Object class term of the ACC", "example": "Purchase Order"},
                                        "definition": {"type": ["string", "null"], "description": "Definition of the ACC", "example": "A document used to request goods or services"},
                                        "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://www.oagis.org"},
                                        "is_deprecated": {"type": "boolean", "description": "Whether the ACC is deprecated", "example": False}
                                    },
                                    "required": ["acc_manifest_id", "acc_id", "guid", "den", "object_class_term", "is_deprecated"]
                                },
                                "to_bccp": {
                                    "type": "object",
                                    "description": "Information about the BCCP that this BCC connects to",
                                    "properties": {
                                        "bccp_manifest_id": {"type": "integer", "description": "Unique identifier for the BCCP manifest", "example": 12345},
                                        "bccp_id": {"type": "integer", "description": "Unique identifier for the BCCP", "example": 6789},
                                        "guid": {"type": "string", "description": "Unique identifier within the release", "example": "a1b2c3d4e5f6789012345678901234ab"},
                                        "den": {"type": "string", "description": "Dictionary Entry Name (DEN) of the BCCP", "example": "Purchase Order. Amount"},
                                        "property_term": {"type": "string", "description": "Property term of the BCCP", "example": "Amount"},
                                        "representation_term": {"type": "string", "description": "Representation term of the BCCP", "example": "Amount"},
                                        "definition": {"type": ["string", "null"], "description": "Definition of the BCCP", "example": "The monetary amount of the purchase order"},
                                        "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://www.oagis.org"},
                                        "is_deprecated": {"type": "boolean", "description": "Whether the BCCP is deprecated", "example": False},
                                        "bdt_manifest": {
                                            "type": "object",
                                            "description": "Basic Data Type (BDT) information associated with this BCCP",
                                            "properties": {
                                                "dt_manifest_id": {"type": "integer", "description": "Unique identifier for the data type manifest", "example": 12345},
                                                "dt_id": {"type": "integer", "description": "Unique identifier for the data type", "example": 6789},
                                                "guid": {"type": "string", "description": "Unique identifier within the release", "example": "a1b2c3d4e5f6789012345678901234ab"},
                                                "den": {"type": "string", "description": "Dictionary Entry Name (DEN) of the data type", "example": "Price_ Amount. Type"},
                                                "data_type_term": {"type": ["string", "null"], "description": "Data type term", "example": "Amount"},
                                                "qualifier": {"type": ["string", "null"], "description": "Qualifier of the data type", "example": "Price"},
                                                "representation_term": {"type": ["string", "null"], "description": "Representation term of the data type", "example": "Amount"},
                                                "six_digit_id": {"type": ["string", "null"], "description": "Six-digit identifier", "example": "123456"},
                                                "based_dt_manifest_id": {"type": ["integer", "null"], "description": "Unique identifier for the base data type manifest", "example": 12345},
                                                "definition": {"type": ["string", "null"], "description": "Definition of the data type", "example": "A number of monetary units"},
                                                "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://unece.org"},
                                                "is_deprecated": {"type": "boolean", "description": "Whether the data type is deprecated", "example": False}
                                            },
                                            "required": ["dt_manifest_id", "dt_id", "guid", "den", "is_deprecated"]
                                        }
                                    },
                                    "required": ["bccp_manifest_id", "bccp_id", "guid", "den", "property_term", "representation_term", "is_deprecated", "bdt_manifest"]
                                }
                            },
                            "required": ["component_type", "manifest_id", "bcc_manifest_id", "bcc_id", "guid", "den", "cardinality_min", "cardinality_max", "cardinality_display", "is_nillable", "is_deprecated", "from_acc", "to_bccp"]
                        }
                    ]
                }
            },
            "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
            "den": {"type": "string", "description": "Dictionary Entry Name (DEN) - the standardized name of the ACC as defined by CCTS v3, uniquely identifying the ACC within its namespace", "example": "Purchase Order. Details"},
            "object_class_term": {"type": "string", "description": "Object class term as specified in CCTS v3", "example": "Purchase Order"},
            "definition": {"type": ["string", "null"], "description": "Definition of the ACC", "example": "A document used to request goods or services from a supplier"},
            "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://www.oagis.org"},
            "object_class_qualifier": {"type": ["string", "null"], "description": "Object class qualifier for the ACC", "example": "Purchase"},
            "component_type": {"type": ["integer", "null"], "description": "Component type identifier", "example": 1},
            "is_abstract": {"type": "boolean", "description": "Whether the ACC is abstract", "example": False},
            "is_deprecated": {"type": "boolean", "description": "Whether the ACC is deprecated", "example": False},
            "state": {"type": ["string", "null"], "description": "Current state of the ACC", "example": "Published"},
            "namespace": {
                "type": ["object", "null"],
                "description": "Namespace information",
                "properties": {
                    "namespace_id": {"type": "integer", "description": "Unique identifier for the namespace", "example": 1},
                    "prefix": {"type": ["string", "null"], "description": "Namespace prefix", "example": "oagis"},
                    "uri": {"type": "string", "description": "Namespace URI (Uniform Resource Identifier)", "example": "http://www.openapplications.org/oagis/10"}
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
                    "release_num": {"type": ["string", "null"], "description": "Release number", "example": "10.6"},
                    "state": {"type": "string", "description": "Release state", "example": "Published"}
                },
                "required": ["release_id", "release_num", "state"]
            },
            "log": {
                "type": ["object", "null"],
                "description": "Log information",
                "properties": {
                    "log_id": {"type": "integer", "description": "Unique identifier for the log", "example": 1},
                    "revision_num": {"type": "integer", "description": "Revision number", "example": 1},
                    "revision_tracking_num": {"type": "integer", "description": "Revision tracking number", "example": 1}
                },
                "required": ["log_id", "revision_num", "revision_tracking_num"]
            },
            "owner": {
                "type": "object",
                "description": "Owner information",
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
                "description": "Information about the creation of the ACC",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who created the ACC",
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
                "description": "Information about the last update of the ACC",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who last updated the ACC",
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
        "required": ["acc_manifest_id", "acc_id", "guid", "den", "object_class_term", "is_abstract", "is_deprecated", "library", "release", "relationships", "owner", "created", "last_updated"]
    }
)
async def get_acc(
    acc_manifest_id: Annotated[int, Field(gt=0, description="Unique numeric identifier of the ACC manifest to retrieve.")],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> GetAccResponse:
    """
    Get a specific ACC by its manifest ID.

    This function retrieves a single ACC from the database and returns
    detailed information including metadata about who created it and when it was
    last updated, along with ACC-specific attributes, namespace information,
    and associated base ACC information.

    DEN Rule: acc.object_class_term + ". Details"

    Relationship Structure:
        An ACC (Aggregation Core Component) can contain relationships with:
        - ASCC (Association Core Component): Represents associations between ACCs
          - Each ASCC relates to an ASCCP (Association Core Component Property)
          - Each ASCCP relates back to an ACC (role_of_acc), forming a cycle
        - BCC (Basic Core Component): Represents properties of primitive types
          - Each BCC relates to a BCCP (Basic Core Component Property)
          - Each BCCP relates to a DT (Data Type), providing the actual data structure

    Component Hierarchy:
        ACC → [ASCC → ASCCP → ACC (role_of_acc)] | [BCC → BCCP → DT]

    Args:
        acc_manifest_id (int): The unique identifier of the ACC manifest to fetch

    Returns:
        GetAccResponse: Response object containing:
            - acc_manifest_id: Unique identifier for the ACC manifest
            - acc_id: Unique identifier for the ACC
            - base_acc: Base ACC information (if this ACC is based on another)
            - relationships: List of related components (ASCCs and BCCs) contained in this ACC.
                Each ASCC relates to an ASCCP (which has a role_of_acc pointing to another ACC),
                and each BCC relates to a BCCP (which has a BDT/DT providing the data type).
            - guid: Unique identifier within the release
            - den: Dictionary Entry Name
            - object_class_term: Object class term
            - definition: Description of the ACC
            - definition_source: URL indicating the source of the definition
            - object_class_qualifier: Qualifier of the ACC
            - component_type: OAGIS component type (0=Base, 1=Semantics, 2=Extension, 3=SemanticGroup, 4=UserExtensionGroup, 5=Embedded, 6=OAGIS10Nouns, 7=OAGIS10BODs, 8=BOD, 9=Verb, 10=Noun, 11=Choice, 12=AttributeGroup)
            - is_abstract: Whether the ACC is abstract
            - is_deprecated: Whether the ACC is deprecated
            - state: State of the ACC
            - namespace: Namespace information (if any)
            - library: Library information with library_id and name
            - release: Release information with release_id, release_num, and state
            - log: Log information with log_id, revision_num, and revision_tracking_num (if any)
            - owner: User information about the owner of the ACC
            - created: Information about the creation of the ACC
            - last_updated: Information about the last update of the ACC

    Raises:
        ToolError: If validation fails, the ACC manifest is not found, or other errors occur.
            Common error scenarios include:
            - ACC manifest with the specified ID does not exist
            - Database connection issues
            - Authentication failures

    Examples:
        Get a specific ACC:
        >>> result = get_acc(acc_manifest_id=123)
        >>> print(f"ACC: {result.object_class_term} (GUID: {result.guid})")
        >>> print(f"Owner: {result.owner.username}")
        >>> print(f"Base ACC: {result.base_acc.den if result.base_acc else 'None'}")
        >>> print(f"Relationships: {len(result.relationships)} ASCCs/BCCs")
    """
    try:
        row = await core_component_service.get_acc(acc_manifest_id)
        if row is None:
            raise ValueError(f"The ACC with manifest ID {acc_manifest_id} was not found. Please check the ID and try again.")
        return GetAccResponse.model_validate(row, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to retrieve ACC {acc_manifest_id}.") from exc


@mcp.tool(
    name="get_asccp",
    description="Get a specific ASCCP by its manifest ID.",
    output_schema={
        "type": "object",
        "description": "Response containing ASCCP (Association Core Component Property) information",
        "properties": {
            "asccp_manifest_id": {"type": "integer", "description": "Unique identifier for the ASCCP manifest", "example": 12345},
            "asccp_id": {"type": "integer", "description": "Unique identifier for the ASCCP", "example": 6789},
            "role_of_acc": {
                "type": "object",
                "description": "Role of ACC information",
                "properties": {
                    "acc_manifest_id": {"type": "integer", "description": "Unique identifier for the role of ACC manifest", "example": 12345},
                    "acc_id": {"type": "integer", "description": "Unique identifier for the role of ACC", "example": 6789},
                    "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                    "den": {"type": "string", "description": "Dictionary Entry Name (DEN) of the role of ACC", "example": "Purchase Order. Details"},
                    "object_class_term": {"type": "string", "description": "Object class term of the role of ACC", "example": "Purchase Order"},
                    "type": {"type": "string", "description": "Type of the role of ACC", "example": "ACC"},
                    "definition": {"type": ["string", "null"], "description": "Definition of the role of ACC", "example": "A document used to request goods or services from a supplier"},
                    "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://www.oagis.org"},
                    "namespace": {
                        "type": ["object", "null"],
                        "description": "Namespace information",
                        "properties": {
                            "namespace_id": {"type": "integer", "description": "Unique identifier for the namespace", "example": 1},
                            "prefix": {"type": ["string", "null"], "description": "Namespace prefix", "example": "oagis"},
                            "uri": {"type": "string", "description": "Namespace URI (Uniform Resource Identifier)", "example": "http://www.openapplications.org/oagis/10"}
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
                            "release_num": {"type": ["string", "null"], "description": "Release number", "example": "10.6"},
                            "state": {"type": "string", "description": "Release state", "example": "Published"}
                        },
                        "required": ["release_id", "release_num", "state"]
                    }
                },
                "required": ["acc_manifest_id", "acc_id", "guid", "den", "object_class_term", "type", "library", "release"]
            },
            "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
            "den": {"type": ["string", "null"], "description": "Dictionary Entry Name (DEN) - the standardized name of the ASCCP as defined by CCTS v3, uniquely identifying the ASCCP within its namespace", "example": "Purchase Order. Details"},
            "property_term": {"type": ["string", "null"], "description": "Property term as specified in CCTS v3", "example": "Details"},
            "definition": {"type": ["string", "null"], "description": "Definition of the ASCCP", "example": "Details of the purchase order"},
            "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://www.oagis.org"},
            "reusable_indicator": {"type": "boolean", "description": "Whether the ASCCP is reusable", "example": True},
            "is_nillable": {"type": ["boolean", "null"], "description": "Whether the ASCCP is nillable", "example": False},
            "is_deprecated": {"type": "boolean", "description": "Whether the ASCCP is deprecated", "example": False},
            "state": {"type": ["string", "null"], "description": "Current state of the ASCCP", "example": "Published"},
            "namespace": {
                "type": ["object", "null"],
                "description": "Namespace information",
                "properties": {
                    "namespace_id": {"type": "integer", "description": "Unique identifier for the namespace", "example": 1},
                    "prefix": {"type": ["string", "null"], "description": "Namespace prefix", "example": "oagis"},
                    "uri": {"type": "string", "description": "Namespace URI (Uniform Resource Identifier)", "example": "http://www.openapplications.org/oagis/10"}
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
                    "release_num": {"type": ["string", "null"], "description": "Release number", "example": "10.6"},
                    "state": {"type": "string", "description": "Release state", "example": "Published"}
                },
                "required": ["release_id", "release_num", "state"]
            },
            "log": {
                "type": ["object", "null"],
                "description": "Log information",
                "properties": {
                    "log_id": {"type": "integer", "description": "Unique identifier for the log", "example": 1},
                    "revision_num": {"type": "integer", "description": "Revision number", "example": 1},
                    "revision_tracking_num": {"type": "integer", "description": "Revision tracking number", "example": 1}
                },
                "required": ["log_id", "revision_num", "revision_tracking_num"]
            },
            "owner": {
                "type": "object",
                "description": "Owner information",
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
                "description": "Information about the creation of the ASCCP",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who created the ASCCP",
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
                "description": "Information about the last update of the ASCCP",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who last updated the ASCCP",
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
        "required": ["asccp_manifest_id", "asccp_id", "role_of_acc", "guid", "reusable_indicator", "is_deprecated", "library", "release", "owner", "created", "last_updated"]
    }
)
async def get_asccp(
    asccp_manifest_id: Annotated[int, Field(gt=0, description="Unique numeric identifier of the ASCCP manifest to retrieve.")],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> GetAsccpResponse:
    """
    Get a specific ASCCP by its manifest ID.

    This function retrieves a single ASCCP from the database and returns
    detailed information including metadata about who created it and when it was
    last updated, along with ASCCP-specific attributes, namespace information,
    and associated role of ACC information.

    DEN Rule: asccp.property_term + ". " + asccp.role_of_acc.object_class_term

    Args:
        asccp_manifest_id (int): The unique identifier of the ASCCP manifest to fetch

    Returns:
        GetAsccpResponse: Response object containing:
            - asccp_manifest_id: Unique identifier for the ASCCP manifest
            - asccp_id: Unique identifier for the ASCCP
            - role_of_acc: Role of ACC information (required)
            - guid: Unique identifier within the release
            - den: Dictionary Entry Name
            - property_term: Property term
            - definition: Description of the ASCCP
            - definition_source: URL indicating the source of the definition
            - reusable_indicator: Whether the ASCCP can be reused
            - is_nillable: Whether the ASCCP is nillable
            - is_deprecated: Whether the ASCCP is deprecated
            - state: State of the ASCCP
            - namespace: Namespace information (if any)
            - library: Library information with library_id and name
            - release: Release information with release_id, release_num, and state
            - log: Log information with log_id, revision_num, and revision_tracking_num (if any)
            - owner: User information about the owner of the ASCCP
            - created: Information about the creation of the ASCCP
            - last_updated: Information about the last update of the ASCCP

    Raises:
        ToolError: If validation fails, the ASCCP manifest is not found, or other errors occur.
            Common error scenarios include:
            - ASCCP manifest with the specified ID does not exist
            - Database connection issues
            - Authentication failures

    Examples:
        Get a specific ASCCP:
        >>> result = get_asccp(asccp_manifest_id=123)
        >>> print(f"ASCCP: {result.property_term} (GUID: {result.guid})")
        >>> print(f"Owner: {result.owner.username}")
        >>> print(f"Role of ACC: {result.role_of_acc.den}")
    """
    try:
        row = await core_component_service.get_asccp(asccp_manifest_id)
        if row is None:
            raise ValueError(
                f"The ASCCP with manifest ID {asccp_manifest_id} was not found. Please check the ID and try again."
            )
        return GetAsccpResponse.model_validate(row, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to retrieve ASCCP {asccp_manifest_id}.") from exc


@mcp.tool(
    name="get_bccp",
    description="Get a specific BCCP by its manifest ID.",
    output_schema={
        "type": "object",
        "description": "Response containing BCCP (Basic Core Component Property) information",
        "properties": {
            "bccp_manifest_id": {"type": "integer", "description": "Unique identifier for the BCCP manifest", "example": 12345},
            "bccp_id": {"type": "integer", "description": "Unique identifier for the BCCP", "example": 6789},
            "bdt": {
                "type": "object",
                "description": "Basic Data Type (BDT) information",
                "properties": {
                    "dt_manifest_id": {"type": "integer", "description": "Unique identifier for the BDT manifest", "example": 12345},
                    "dt_id": {"type": "integer", "description": "Unique identifier for the BDT", "example": 6789},
                    "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
                    "den": {"type": "string", "description": "Dictionary Entry Name (DEN) of the BDT", "example": "Price_ Amount. Type"},
                    "data_type_term": {"type": ["string", "null"], "description": "Data type term of the BDT", "example": "Amount"},
                    "qualifier": {"type": ["string", "null"], "description": "Qualifier of the BDT", "example": "Price"},
                    "representation_term": {"type": ["string", "null"], "description": "Representation term of the BDT", "example": "Amount"},
                    "six_digit_id": {"type": ["string", "null"], "description": "Six-digit identifier of the BDT", "example": "123456"},
                    "definition": {"type": ["string", "null"], "description": "Definition of the BDT", "example": "A number of monetary units specified in a currency where the unit of currency is explicit or implied"},
                    "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://unece.org/trade/uncefact/core-components-data-type-catalogue"},
                    "content_component_definition": {"type": ["string", "null"], "description": "Content component definition", "example": "A numeric value determined by measuring an object along with the specified unit of measure"},
                    "namespace": {
                        "type": ["object", "null"],
                        "description": "Namespace information",
                        "properties": {
                            "namespace_id": {"type": "integer", "description": "Unique identifier for the namespace", "example": 1},
                            "prefix": {"type": ["string", "null"], "description": "Namespace prefix", "example": "oagis"},
                            "uri": {"type": "string", "description": "Namespace URI (Uniform Resource Identifier)", "example": "http://www.openapplications.org/oagis/10"}
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
                            "release_num": {"type": ["string", "null"], "description": "Release number", "example": "10.6"},
                            "state": {"type": "string", "description": "Release state", "example": "Published"}
                        },
                        "required": ["release_id", "release_num", "state"]
                    }
                },
                "required": ["dt_manifest_id", "dt_id", "guid", "den", "library", "release"]
            },
            "guid": {"type": "string", "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)", "example": "a1b2c3d4e5f6789012345678901234ab"},
            "den": {"type": "string", "description": "Dictionary Entry Name (DEN) - the standardized name of the BCCP as defined by CCTS v3, uniquely identifying the BCCP within its namespace", "example": "Purchase Order. Amount"},
            "property_term": {"type": "string", "description": "Property term as specified in CCTS v3", "example": "Amount"},
            "representation_term": {"type": "string", "description": "Representation term as specified in CCTS v3", "example": "Amount"},
            "definition": {"type": ["string", "null"], "description": "Definition of the BCCP", "example": "The monetary amount of the purchase order"},
            "definition_source": {"type": ["string", "null"], "description": "URL indicating the source of the definition", "example": "https://www.oagis.org"},
            "is_nillable": {"type": "boolean", "description": "Whether the BCCP is nillable", "example": False},
            "value_constraint": {
                "type": ["object", "null"],
                "description": "Value constraint (default_value or fixed_value) for the BCCP. Exactly one of default_value or fixed_value must be set.",
                "properties": {
                    "default_value": {"type": ["string", "null"], "description": "Default value for the BCCP", "example": "0.00"},
                    "fixed_value": {"type": ["string", "null"], "description": "Fixed value for the BCCP", "example": "ISO"}
                }
            },
            "is_deprecated": {"type": "boolean", "description": "Whether the BCCP is deprecated", "example": False},
            "state": {"type": ["string", "null"], "description": "Current state of the BCCP", "example": "Published"},
            "namespace": {
                "type": ["object", "null"],
                "description": "Namespace information",
                "properties": {
                    "namespace_id": {"type": "integer", "description": "Unique identifier for the namespace", "example": 1},
                    "prefix": {"type": ["string", "null"], "description": "Namespace prefix", "example": "oagis"},
                    "uri": {"type": "string", "description": "Namespace URI (Uniform Resource Identifier)", "example": "http://www.openapplications.org/oagis/10"}
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
                    "release_num": {"type": ["string", "null"], "description": "Release number", "example": "10.6"},
                    "state": {"type": "string", "description": "Release state", "example": "Published"}
                },
                "required": ["release_id", "release_num", "state"]
            },
            "log": {
                "type": ["object", "null"],
                "description": "Log information",
                "properties": {
                    "log_id": {"type": "integer", "description": "Unique identifier for the log", "example": 1},
                    "revision_num": {"type": "integer", "description": "Revision number", "example": 1},
                    "revision_tracking_num": {"type": "integer", "description": "Revision tracking number", "example": 1}
                },
                "required": ["log_id", "revision_num", "revision_tracking_num"]
            },
            "owner": {
                "type": "object",
                "description": "Owner information",
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
                "description": "Information about the creation of the BCCP",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who created the BCCP",
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
                "description": "Information about the last update of the BCCP",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who last updated the BCCP",
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
        "required": ["bccp_manifest_id", "bccp_id", "bdt", "guid", "den", "property_term", "representation_term", "is_nillable", "is_deprecated", "library", "release", "owner", "created", "last_updated"]
    }
)
async def get_bccp(
    bccp_manifest_id: Annotated[int, Field(gt=0, description="Unique numeric identifier of the BCCP manifest to retrieve.")],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> GetBccpResponse:
    """
    Get a specific BCCP by its manifest ID.

    This function retrieves a single BCCP from the database and returns
    detailed information including metadata about who created it and when it was
    last updated, along with BCCP-specific attributes, namespace information,
    and associated BDT (Basic Data Type) information.

    DEN Rule: bccp.property_term + ". " + dt.den.replace(". Type", "")

    Args:
        bccp_manifest_id (int): The unique identifier of the BCCP manifest to fetch

    Returns:
        GetBccpResponse: Response object containing:
            - bccp_manifest_id: Unique identifier for the BCCP manifest
            - bccp_id: Unique identifier for the BCCP
            - bdt: BDT (Basic Data Type) information
            - guid: Unique identifier within the release
            - den: Dictionary Entry Name
            - property_term: Property term
            - representation_term: Representation term
            - definition: Description of the BCCP
            - definition_source: URL indicating the source of the definition
            - is_nillable: Whether the BCCP is nillable
            - default_value: Default value constraint
            - fixed_value: Fixed value constraint
            - is_deprecated: Whether the BCCP is deprecated
            - state: State of the BCCP
            - namespace: Namespace information (if any)
            - library: Library information with library_id and name
            - release: Release information with release_id, release_num, and state
            - log: Log information with log_id, revision_num, and revision_tracking_num (if any)
            - owner: User information about the owner of the BCCP
            - created: Information about the creation of the BCCP
            - last_updated: Information about the last update of the BCCP

    Raises:
        ToolError: If validation fails, the BCCP manifest is not found, or other errors occur.
            Common error scenarios include:
            - BCCP manifest with the specified ID does not exist
            - Database connection issues
            - Authentication failures

    Examples:
        Get a specific BCCP:
        >>> result = get_bccp(bccp_manifest_id=123)
        >>> print(f"BCCP: {result.property_term} (GUID: {result.guid})")
        >>> print(f"Owner: {result.owner.username}")
        >>> print(f"BDT: {result.bdt.den}")
    """
    try:
        row = await core_component_service.get_bccp(bccp_manifest_id)
        if row is None:
            raise ValueError(
                f"The BCCP with manifest ID {bccp_manifest_id} was not found. Please check the ID and try again."
            )
        return GetBccpResponse.model_validate(row, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to retrieve BCCP {bccp_manifest_id}.") from exc


def _build_core_component_service(
    session: AsyncSession,
    requester: AuthenticatedUser,
) -> CoreComponentService:
    """Construct the core-component service for an MCP request."""
    plugin = get_vendor_plugin()
    release_service = ReleaseService(
        plugin.create_release_repository(session),
        plugin.create_app_user_repository(session),
        requester=requester,
    )
    return CoreComponentService(
        plugin.create_core_component_repository(session),
        release_service,
        plugin.create_app_user_repository(session),
        requester=requester,
    )


def _to_list_response(*, items: list[Any], total: int, offset: int, limit: int) -> GetCoreComponentPaginationResponse:
    """Build the paginated MCP response model."""
    return GetCoreComponentPaginationResponse(
        total_items=total,
        offset=offset,
        limit=limit,
        items=[CoreComponentListEntry.model_validate(item, from_attributes=True) for item in items],
    )
