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
  filtered by release with optional filters including owner. Supports custom sorting and pagination.

- create_acc: Create a new ACC (Aggregate Core Component) by targeting an explicit
  `release_id` with optional base ACC, namespace, definition, and tag attachments.

- add_ascc_to_acc: Add an ASCC (Association Core Component) relationship to an ACC
  by referencing an existing ASCCP manifest.

- add_bcc_to_acc: Add a BCC (Basic Core Component) relationship to an ACC
  by referencing an existing BCCP manifest.

- remove_ascc: Remove an existing ASCC relationship from its owning ACC.

- remove_bcc: Remove an existing BCC relationship from its owning ACC.

- update_ascc: Update mutable ASCC relationship fields such as cardinality,
  definition, and deprecation state.

- update_bcc: Update mutable BCC relationship fields such as entity type,
  cardinality, value constraints, definition, and deprecation state.

- update_acc: Update mutable ACC fields such as object class term, component type,
  definition text, abstract flag, deprecation flag, namespace, or base ACC inheritance.

- add_tags_to_acc: Attach one or more tags to an ACC.

- remove_tags_from_acc: Detach one or more tags from an ACC.

- change_acc_state: Change an ACC to another lifecycle state following connectCenter's
  ACC state-transition rules.

- revise_or_amend_acc: Create a new editable ACC revision from a stable ACC revision.
- revise_or_amend_asccp: Create a new editable ASCCP revision from a stable ASCCP revision.
- revise_or_amend_bccp: Create a new editable BCCP revision from a stable BCCP revision.

- cancel_acc: Cancel the current ACC revision and restore the previous stable ACC revision.

- discard_acc: Discard a Deleted ACC and its direct ACC-owned records permanently.

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

from fastmcp import Context, FastMCP
from fastmcp.dependencies import Depends
from fastmcp.exceptions import ToolError
from fastmcp.server.elicitation import (
    AcceptedElicitation,
    CancelledElicitation,
    DeclinedElicitation,
)
from pydantic import BaseModel, Field
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.vendor_plugins import get_vendor_plugin
from app.utils.date import parse_date_range
from app.security import AuthenticatedUser
from app.services.app_user_service import AppUserService
from app.services.core_component_service import CoreComponentService
from app.services.data_type_service import DataTypeService
from app.services.release_service import ReleaseService
from app.tools import _to_tool_error, get_tool_authenticated_user, tool_session
from app.tools.models.core_component import (
    AddAsccToAccResponse,
    AddBccToAccResponse,
    BccEntityTypeUpdate,
    BccValueConstraintInput,
    CoreComponentListEntryResponse,
    CreateAccResponse,
    CreateAsccpResponse,
    CreateBccpResponse,
    GetAccResponse,
    GetAsccpResponse,
    GetBccpResponse,
    GetCoreComponentPaginationResponse,
    ReorderAsccInAccResponse,
    ReorderBccInAccResponse,
    TransferAccOwnershipResponse,
    TransferAsccpOwnershipResponse,
    TransferBccpOwnershipResponse,
    UpdateAsccResponse,
    UpdateAccResponse,
    UpdateBccResponse,
    UpdateAsccpResponse,
    UpdateBccpResponse,
)
from app.types.unset import UNSET
from app.types.identifiers import AccManifestId, DataTypeManifestId

logger = logging.getLogger("connectcenter.mcp.core_component")

mcp = FastMCP("connectCenter MCP - Core Component Tools")

EMPTY_OUTPUT_SCHEMA = {
    "type": "object",
    "description": "Empty response body.",
    "properties": {},
    "additionalProperties": False,
}


def _map_bcc_entity_type_update(entity_type: BccEntityTypeUpdate) -> Literal["Attribute", "Element"]:
    """Translate tool-facing BCC entity-type labels to stored service labels."""
    return "Attribute" if entity_type in {"Attribute", 0} else "Element"


class MissingAccDefinitionResponse(BaseModel):
    """Structured elicitation response for a missing ACC definition."""

    definition: str = Field(min_length=1, description="Definition to save before changing the ACC state.")


async def _elicit_on_acc_structure_warnings(
    *,
    ctx: Context,
    warnings: list[str],
    prompt: str,
    declined_message: str,
    cancelled_message: str,
) -> None:
    """Ask for confirmation when an ACC change may create repeated fields later."""
    if not warnings:
        return
    elicit_result = await ctx.elicit(
        message=(
            f"{prompt}\n\n"
            "Things to double-check:\n"
            + "\n".join(f"- {warning}" for warning in warnings)
            + "\n\nDo you still want to continue?"
        ),
        response_type=None,
    )
    match elicit_result:
        case AcceptedElicitation():
            return
        case DeclinedElicitation():
            raise ToolError(declined_message)
        case CancelledElicitation():
            raise ToolError(cancelled_message)


async def get_core_component_service(
    session: AsyncSession = Depends(tool_session),
) -> CoreComponentService:
    """Provide a requester-scoped core-component service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    return _build_core_component_service(session, requester)


async def get_app_user_service(
    session: AsyncSession = Depends(tool_session),
) -> AppUserService:
    """Provide a requester-scoped app-user service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    vendor_plugin = get_vendor_plugin()
    app_user_repository = vendor_plugin.create_app_user_repository(session)
    return AppUserService(app_user_repository=app_user_repository, requester=requester)


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
                                "release_num": {"type": "string", "description": "Release number", "example": "10.6"},
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
    tag: Annotated[str | None, Field(default=None, description="Comma-separated tag names to filter by exact match.")],
    owner: Annotated[str | None, Field(default=None, description="Comma-separated owner login IDs to filter by exact match. Prefix a login ID with '!' to exclude it.")],
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
        tag (str | None, optional): Comma-separated tag names using exact match.
            To discover available tag names, use the get_tags() tool first. Defaults to None.
        owner (str | None, optional): Comma-separated owner login IDs using exact match.
            Prefix a login ID with '!' to exclude it. Login IDs cannot contain '!' or ','. Defaults to None.
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
            owner=owner,
            created_on=parse_date_range(created_on),
            last_updated_on=parse_date_range(last_updated_on),
        )
        return _to_list_response(items=page.items, total=page.total, offset=page.offset, limit=page.limit)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to retrieve core components.") from exc


@mcp.tool(
    name="create_acc",
    description="Create a new ACC (Aggregate Core Component).",
    output_schema={
        "type": "object",
        "description": "Response containing the created ACC manifest identifier.",
        "properties": {
            "acc_manifest_id": {
                "type": "integer",
                "description": "Created ACC manifest identifier.",
                "example": 12345,
            }
        },
        "required": ["acc_manifest_id"],
    },
)
async def create_acc(
    release_id: Annotated[
        int,
        Field(
            gt=0,
            description=(
                "Target release identifier. Developers must use the `Working` release, "
                "and end-users must use a non-`Working` release."
            ),
        ),
    ],
    object_class_term: Annotated[
        str,
        Field(
            description=(
                "Object class term to start with. In CCTS, this is the term that represents the activity or "
                "object the ACC stands for. It serves as the basis for the ACC DEN and for the DENs of the ASCC "
                "and BCC properties under that ACC."
            ),
        ),
    ],
    based_acc_manifest_id: Annotated[
        int | None,
        Field(
            default=None,
            gt=0,
            description=(
                "Base ACC manifest identifier. If provided and the base ACC is in the same library as `release_id`, "
                "it must be from that release. If it is in a different library, its release must be one of the "
                "target release dependencies."
            ),
        ),
    ],
    component_type: Annotated[
        Literal[
            "Base",
            "Semantics",
            "Extension",
            "SemanticGroup",
            "UserExtensionGroup",
            "Embedded",
            "OAGIS10Nouns",
            "OAGIS10BODs",
            "BOD",
            "Verb",
            "Noun",
            "Choice",
            "AttributeGroup",
        ],
        Field(
            default="Semantics",
            description=(
                "OAGIS component type to start with. Use `Base` only when this ACC is intended to be the base ACC "
                "for other ACCs. Use `Extension` for a developer extension ACC that end-users may extend later "
                "in BIEs. Use `SemanticGroup` for a grouping ACC whose associations are flattened in BIEs. "
                "Otherwise use `Semantics`. If the user does not indicate a specific intent, keep the default "
                "value `Semantics`."
            ),
        ),
    ],
    definition: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Definition text to start with. This is the explanatory text that describes what the ACC means."
            ),
        ),
    ],
    definition_source: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Definition source to start with. Use this to record where the definition came from, such as a "
                "specification, standard, or reference URL."
            ),
        ),
    ],
    is_abstract: Annotated[
        bool | None,
        Field(
            default=None,
            description=(
                "Whether this ACC should be abstract. If `component_type` is `Base`, this is usually `true`, "
                "because the ACC is meant to serve as a base ACC for other ACCs."
            ),
        ),
    ],
    namespace_id: Annotated[
        int | None,
        Field(
            default=None,
            gt=0,
            description="Namespace identifier. If provided, it must belong to the same library as the target release.",
        ),
    ],
    tag_id: Annotated[
        list[int] | None,
        Field(
            default=None,
            description="Optional tag identifier list to attach. Use get_tags() to discover valid tag IDs.",
        ),
    ],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> CreateAccResponse:
    """
    Create an ACC in a role-appropriate release branch.

    This tool mirrors connectCenter's ACC creation defaults:
    - `component_type` defaults to `Semantics`
    - persisted ACC `type` is derived from `component_type`
    - developers can target only the `Working` release, while end-users can target only non-`Working` releases
    - use `get_working_release` when you need help finding the developer-valid `Working` `release_id`
    - choose `Base` only when the ACC is meant to be reused as a base ACC for other ACCs
    - choose `Extension` only for a developer extension ACC that end-users may extend later in BIEs
    - choose `SemanticGroup` only for a grouping ACC whose associations are flattened in BIEs
    - if the user does not clearly ask for one of those cases, leave `component_type` as `Semantics`

    If `tag_id` is provided, connectCenter will attach each referenced tag to the ACC.
    """
    return await _create_acc_response(
        core_component_service=core_component_service,
        release_id=release_id,
        based_acc_manifest_id=based_acc_manifest_id,
        object_class_term=object_class_term,
        component_type=component_type,
        definition=definition,
        definition_source=definition_source,
        is_abstract=is_abstract,
        namespace_id=namespace_id,
        tag_id=tag_id,
        fallback="Unable to create the ACC.",
    )


@mcp.tool(
    name="create_asccp",
    description="Create a new ASCCP (Association Core Component Property).",
    output_schema={
        "type": "object",
        "description": "Response containing the created ASCCP manifest identifier.",
        "properties": {
            "asccp_manifest_id": {"type": "integer", "description": "Created ASCCP manifest identifier.", "example": 12345}
        },
        "required": ["asccp_manifest_id"],
    },
)
async def create_asccp(
    release_id: Annotated[int, Field(gt=0, description="Target release identifier.")],
    role_of_acc_manifest_id: Annotated[
        int,
        Field(
            gt=0,
            description=(
                "Role ACC manifest identifier. This identifies the ACC that this ASCCP points to as the associated ACC in the relationship. "
                "If the role ACC is in the same library as `release_id`, it must be "
                "from that release. If it is in a different library, its release must be one of the target release "
                "dependencies."
            ),
        ),
    ],
    property_term: Annotated[
        str,
        Field(
            ...,
            min_length=1,
            description=(
                "Property term to use for this ASCCP. In CCTS, this is a semantically meaningful name for the "
                "characteristic that represents the nature of the association to the associated ACC."
            ),
        ),
    ],
    reusable_indicator: Annotated[bool, Field(default=True, description="Initial reusable indicator.")],
    namespace_id: Annotated[int | None, Field(default=None, gt=0, description="Optional namespace identifier.")],
    definition: Annotated[
        str | None,
        Field(
            default=None,
            description="Definition text to start with. This is the explanatory text that describes what the ASCCP means.",
        ),
    ],
    definition_source: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Definition source to start with. Use this to record where the definition came from, such as a "
                "specification, standard, or reference URL."
            ),
        ),
    ],
    tag_id: Annotated[list[int] | None, Field(default=None, description="Optional tag identifier list to attach.")] = None,
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> CreateAsccpResponse:
    """Create an ASCCP in a role-appropriate release branch."""
    try:
        result = await core_component_service.create_asccp(
            release_id=release_id,
            role_of_acc_manifest_id=role_of_acc_manifest_id,
            property_term=property_term,
            reusable_indicator=reusable_indicator,
            namespace_id=namespace_id,
            definition=definition,
            definition_source=definition_source,
            tag_id=tag_id,
        )
        return CreateAsccpResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to create the ASCCP.") from exc


@mcp.tool(
    name="create_bccp",
    description="Create a new BCCP (Basic Core Component Property).",
    output_schema={
        "type": "object",
        "description": "Response containing the created BCCP manifest identifier.",
        "properties": {
            "bccp_manifest_id": {"type": "integer", "description": "Created BCCP manifest identifier.", "example": 12345}
        },
        "required": ["bccp_manifest_id"],
    },
)
async def create_bccp(
    release_id: Annotated[int, Field(gt=0, description="Target release identifier.")],
    bdt_manifest_id: Annotated[
        int,
        Field(
            gt=0,
            description=(
                "Target BDT manifest identifier. The selected data type must already be a BDT, "
                "which means its base DT link is set. If the BDT is in the same library as `release_id`, it must be "
                "from that release. If it is in a different library, its release must be one of the target release "
                "dependencies."
            ),
        ),
    ],
    property_term: Annotated[
        str,
        Field(
            description=(
                "Property term for this BCCP. In CCTS, this is a semantically meaningful name for a unique "
                "characteristic that can be used in an ACC object class."
            ),
        ),
    ],
    definition: Annotated[
        str | None,
        Field(
            default=None,
            description="Definition text to start with. This is the explanatory text that describes what the BCCP means.",
        ),
    ] = None,
    definition_source: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Definition source to start with. Use this to record where the definition came from, such as a "
                "specification, standard, or reference URL."
            ),
        ),
    ] = None,
    deprecated: Annotated[bool | None, Field(default=None, description="Initial deprecation flag.")] = None,
    is_nillable: Annotated[bool | None, Field(default=None, description="Initial nillable flag.")] = None,
    namespace_id: Annotated[int | None, Field(default=None, gt=0, description="Optional namespace identifier.")] = None,
    value_constraint: Annotated[
        dict[str, str | None] | None,
        Field(
            default=None,
            description="Optional value constraint. Provide exactly one of `default_value` or `fixed_value`.",
        ),
    ] = None,
    tag_id: Annotated[list[int] | None, Field(default=None, description="Optional tag identifier list to attach.")] = None,
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> CreateBccpResponse:
    """Create a BCCP in a role-appropriate release branch."""
    try:
        default_value = None if value_constraint is None else value_constraint.get("default_value")
        fixed_value = None if value_constraint is None else value_constraint.get("fixed_value")
        result = await core_component_service.create_bccp(
            release_id=release_id,
            bdt_manifest_id=DataTypeManifestId(int(bdt_manifest_id)),
            property_term=property_term,
            definition=definition,
            definition_source=definition_source,
            deprecated=deprecated,
            is_nillable=is_nillable,
            namespace_id=namespace_id,
            default_value=default_value,
            fixed_value=fixed_value,
            tag_id=tag_id,
        )
        return CreateBccpResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to create the BCCP.") from exc


@mcp.tool(
    name="add_ascc_to_acc",
    description="Add an ASCC (Association Core Component) relationship to an ACC sequence, with optional relationship metadata applied immediately.",
    output_schema={
        "type": "object",
        "description": "Response containing the ASCC manifest identifier.",
        "properties": {
            "ascc_manifest_id": {
                "type": "integer",
                "description": "ASCC manifest identifier.",
                "example": 12345,
            },
        },
        "required": ["ascc_manifest_id"],
    },
)
async def add_ascc_to_acc(
    acc_manifest_id: Annotated[int, Field(gt=0, description="Source ACC manifest identifier.")],
    asccp_manifest_id: Annotated[int, Field(gt=0, description="Target ASCCP manifest identifier.")],
    ctx: Context,
    index: Annotated[
        int | None,
        Field(
            default=None,
            description=(
                "Optional zero-based insertion index. Use `0` to place the association first, `-1` to place it last, "
                "or another non-negative index to place the association at that position in the sequence. "
                "Mutually exclusive with all `after_*` and `before_*` options. If omitted together with all other "
                "placement options, the ASCC is added at the end."
            ),
        ),
    ] = None,
    after_ascc_manifest_id: Annotated[
        int | None,
        Field(default=None, gt=0, description="Place the ASCC after this ASCC manifest identifier. Mutually exclusive with `index` and all other `after_*`/`before_*` options."),
    ] = None,
    after_bcc_manifest_id: Annotated[
        int | None,
        Field(default=None, gt=0, description="Place the ASCC after this BCC manifest identifier. Mutually exclusive with `index` and all other `after_*`/`before_*` options."),
    ] = None,
    before_ascc_manifest_id: Annotated[
        int | None,
        Field(default=None, gt=0, description="Place the ASCC before this ASCC manifest identifier. Mutually exclusive with `index` and all other `after_*`/`before_*` options."),
    ] = None,
    before_bcc_manifest_id: Annotated[
        int | None,
        Field(default=None, gt=0, description="Place the ASCC before this BCC manifest identifier. Mutually exclusive with `index` and all other `after_*`/`before_*` options."),
    ] = None,
    cardinality_min: Annotated[
        int | None,
        Field(
            default=None,
            ge=0,
            description=(
                "Optional minimum cardinality to apply immediately after creation. Use `0` or a positive integer. "
                "If `cardinality_max` is also provided and is not `-1`, this value must be less than or equal to it."
            ),
        ),
    ] = None,
    cardinality_max: Annotated[
        int | None,
        Field(
            default=None,
            ge=-1,
            description=(
                "Optional maximum cardinality to apply immediately after creation. Use `-1` for unbounded, or "
                "`0` or a positive integer for a bounded maximum. If it is not `-1`, it must be greater than or "
                "equal to `cardinality_min`. If `entity_type` is `Attribute`, this value must be `0` or `1`; "
                "`-1` is not allowed."
            ),
        ),
    ] = None,
    definition: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Optional definition text to apply immediately after creation. This is the explanatory text "
                "that describes what the BCC means. Pass an empty string to clear it."
            ),
        ),
    ] = None,
    definition_source: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Optional definition source to apply immediately after creation. Use this to record where the "
                "definition came from, such as a specification, standard, or reference URL. Pass an empty "
                "string to clear it."
            ),
        ),
    ] = None,
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> AddAsccToAccResponse:
    """
    Add an ASCC to an ACC using an existing ASCCP manifest.

    connectCenter applies these rules:
    - if the ASCCP is in the same library as the ACC, it must be from the ACC release
    - if the ASCCP is in a different library, its release must be one of the ACC release dependencies
    - only the ACC owner or an administrator can modify relationships
    - the target ASCCP must be reusable
    - an ACC can have at most one `Extension` ASCCP
    - if the ASCCP is already associated to the ACC, use `update_ascc` with a sequence selector instead
    - if all placement inputs are omitted, the relationship is added at the end of the ACC's `seq_key` order
    - provide only one of `index`, `after_*`, or `before_*`
    - if the change could create repeated field names or sequence issues later, the tool asks for confirmation before continuing
    """
    try:
        warnings = await core_component_service.get_add_ascc_to_acc_warnings(
            acc_manifest_id=acc_manifest_id,
            asccp_manifest_id=asccp_manifest_id,
            index=index,
            after_ascc_manifest_id=None if after_ascc_manifest_id is None else after_ascc_manifest_id,
            after_bcc_manifest_id=None if after_bcc_manifest_id is None else after_bcc_manifest_id,
            before_ascc_manifest_id=None if before_ascc_manifest_id is None else before_ascc_manifest_id,
            before_bcc_manifest_id=None if before_bcc_manifest_id is None else before_bcc_manifest_id,
        )
        await _elicit_on_acc_structure_warnings(
            ctx=ctx,
            warnings=warnings,
            prompt="Adding this ASCC may create repeated field names or sequence issues later.",
            declined_message="Adding the ASCC to the ACC was not confirmed.",
            cancelled_message="Adding the ASCC to the ACC was cancelled.",
        )
        result = await core_component_service.add_ascc_to_acc(
            acc_manifest_id=acc_manifest_id,
            asccp_manifest_id=asccp_manifest_id,
            index=index,
            after_ascc_manifest_id=None if after_ascc_manifest_id is None else after_ascc_manifest_id,
            after_bcc_manifest_id=None if after_bcc_manifest_id is None else after_bcc_manifest_id,
            before_ascc_manifest_id=None if before_ascc_manifest_id is None else before_ascc_manifest_id,
            before_bcc_manifest_id=None if before_bcc_manifest_id is None else before_bcc_manifest_id,
            cardinality_min=cardinality_min,
            cardinality_max=cardinality_max,
            definition=definition,
            definition_source=definition_source,
        )
        return AddAsccToAccResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to add the ASCC to ACC {acc_manifest_id}.") from exc


@mcp.tool(
    name="add_bcc_to_acc",
    description="Add a BCC (Basic Core Component) relationship to an ACC sequence, with optional relationship metadata applied immediately.",
    output_schema={
        "type": "object",
        "description": "Response containing the BCC manifest identifier.",
        "properties": {
            "bcc_manifest_id": {
                "type": "integer",
                "description": "BCC manifest identifier.",
                "example": 12345,
            },
        },
        "required": ["bcc_manifest_id"],
    },
)
async def add_bcc_to_acc(
    acc_manifest_id: Annotated[int, Field(gt=0, description="Source ACC manifest identifier.")],
    bccp_manifest_id: Annotated[int, Field(gt=0, description="Target BCCP manifest identifier.")],
    ctx: Context,
    index: Annotated[
        int | None,
        Field(
            default=None,
            description=(
                "Optional zero-based insertion index. Use `0` to place the association first, `-1` to place it last, "
                "or another non-negative index to place the association at that position in the sequence. "
                "Mutually exclusive with all `after_*` and `before_*` options. If omitted together with all other "
                "placement options, the BCC is added at the end. For `Attribute` BCCs, positions outside the leading "
                "attribute block are adjusted to the end of that block."
            ),
        ),
    ] = None,
    after_ascc_manifest_id: Annotated[
        int | None,
        Field(default=None, gt=0, description="Place the BCC after this ASCC manifest identifier. Mutually exclusive with `index` and all other `after_*`/`before_*` options."),
    ] = None,
    after_bcc_manifest_id: Annotated[
        int | None,
        Field(default=None, gt=0, description="Place the BCC after this BCC manifest identifier. Mutually exclusive with `index` and all other `after_*`/`before_*` options."),
    ] = None,
    before_ascc_manifest_id: Annotated[
        int | None,
        Field(default=None, gt=0, description="Place the BCC before this ASCC manifest identifier. Mutually exclusive with `index` and all other `after_*`/`before_*` options."),
    ] = None,
    before_bcc_manifest_id: Annotated[
        int | None,
        Field(default=None, gt=0, description="Place the BCC before this BCC manifest identifier. Mutually exclusive with `index` and all other `after_*`/`before_*` options."),
    ] = None,
    cardinality_min: Annotated[
        int | None,
        Field(
            default=None,
            ge=0,
            description=(
                "Optional minimum cardinality to apply immediately after creation. Use `0` or a positive integer. "
                "If `cardinality_max` is also provided and is not `-1`, this value must be less than or equal to it."
            ),
        ),
    ] = None,
    cardinality_max: Annotated[
        int | None,
        Field(
            default=None,
            ge=-1,
            description=(
                "Optional maximum cardinality to apply immediately after creation. Use `-1` for unbounded, or "
                "`0` or a positive integer for a bounded maximum. If it is not `-1`, it must be greater than or "
                "equal to `cardinality_min`."
            ),
        ),
    ] = None,
    entity_type: Annotated[
        BccEntityTypeUpdate | None,
        Field(
            default=None,
            description="Optional entity type to apply immediately after creation. Use `Element` or `Attribute`. Integer aliases `1` and `0` are also accepted.",
        ),
    ] = None,
    is_nillable: Annotated[
        bool | None,
        Field(default=None, description="Optional nillable flag to apply immediately after creation."),
    ] = None,
    definition: Annotated[
        str | None,
        Field(default=None, description="Optional relationship definition to apply immediately after creation. Pass an empty string to clear it."),
    ] = None,
    definition_source: Annotated[
        str | None,
        Field(default=None, description="Optional relationship definition source to apply immediately after creation. Pass an empty string to clear it."),
    ] = None,
    value_constraint: Annotated[
        BccValueConstraintInput | None,
        Field(
            default=None,
            description=(
                "Optional value constraint to apply immediately after creation. Provide `default_value` to set a default when the element is omitted, "
                "or `fixed_value` to require one exact value."
            ),
        ),
    ] = None,
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> AddBccToAccResponse:
    """
    Add a BCC to an ACC using an existing BCCP manifest.

    connectCenter applies these rules:
    - if the BCCP is in the same library as the ACC, it must be from the ACC release
    - if the BCCP is in a different library, its release must be one of the ACC release dependencies
    - only the ACC owner or an administrator can modify relationships
    - if the BCCP is already associated to the ACC, use `update_bcc` with a sequence selector instead
    - if all placement inputs are omitted, the relationship is added at the end of the ACC's `seq_key` order
    - provide only one of `index`, `after_*`, or `before_*`
    - when adding an `Attribute` BCC, positions outside the leading attribute block are adjusted to the end of that block
    - if the change could create repeated field names or sequence issues later, the tool asks for confirmation before continuing
    """
    try:
        warnings = await core_component_service.get_add_bcc_to_acc_warnings(
            acc_manifest_id=acc_manifest_id,
            bccp_manifest_id=bccp_manifest_id,
            index=index,
            after_ascc_manifest_id=None if after_ascc_manifest_id is None else after_ascc_manifest_id,
            after_bcc_manifest_id=None if after_bcc_manifest_id is None else after_bcc_manifest_id,
            before_ascc_manifest_id=None if before_ascc_manifest_id is None else before_ascc_manifest_id,
            before_bcc_manifest_id=None if before_bcc_manifest_id is None else before_bcc_manifest_id,
            entity_type=None if entity_type is None else _map_bcc_entity_type_update(entity_type),
        )
        await _elicit_on_acc_structure_warnings(
            ctx=ctx,
            warnings=warnings,
            prompt="Adding this BCC may create repeated field names or sequence issues later.",
            declined_message="Adding the BCC to the ACC was not confirmed.",
            cancelled_message="Adding the BCC to the ACC was cancelled.",
        )
        result = await core_component_service.add_bcc_to_acc(
            acc_manifest_id=acc_manifest_id,
            bccp_manifest_id=bccp_manifest_id,
            index=index,
            after_ascc_manifest_id=None if after_ascc_manifest_id is None else after_ascc_manifest_id,
            after_bcc_manifest_id=None if after_bcc_manifest_id is None else after_bcc_manifest_id,
            before_ascc_manifest_id=None if before_ascc_manifest_id is None else before_ascc_manifest_id,
            before_bcc_manifest_id=None if before_bcc_manifest_id is None else before_bcc_manifest_id,
            entity_type=None if entity_type is None else _map_bcc_entity_type_update(entity_type),
            cardinality_min=cardinality_min,
            cardinality_max=cardinality_max,
            definition=definition,
            definition_source=definition_source,
            is_nillable=is_nillable,
            default_value=None if value_constraint is None else value_constraint.default_value,
            fixed_value=None if value_constraint is None else value_constraint.fixed_value,
        )
        return AddBccToAccResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to add the BCC to ACC {acc_manifest_id}.") from exc


@mcp.tool(
    name="remove_ascc",
    description="Remove an existing ASCC (Association Core Component) relationship from its owning ACC.",
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def remove_ascc(
    ascc_manifest_id: Annotated[int, Field(gt=0, description="ASCC manifest identifier to remove.")],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> dict[str, object]:
    """Remove an existing ASCC relationship from an ACC."""
    try:
        await core_component_service.remove_ascc(ascc_manifest_id=ascc_manifest_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to remove ASCC {ascc_manifest_id}.") from exc


@mcp.tool(
    name="remove_bcc",
    description="Remove an existing BCC (Basic Core Component) relationship from its owning ACC.",
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def remove_bcc(
    bcc_manifest_id: Annotated[int, Field(gt=0, description="BCC manifest identifier to remove.")],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> dict[str, object]:
    """Remove an existing BCC relationship from an ACC."""
    try:
        await core_component_service.remove_bcc(bcc_manifest_id=bcc_manifest_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to remove BCC {bcc_manifest_id}.") from exc


@mcp.tool(
    name="update_acc",
    description="Update mutable ACC (Aggregate Core Component) fields.",
    output_schema={
        "type": "object",
        "description": "Response containing the updated ACC manifest identifier and changed fields.",
        "properties": {
            "acc_manifest_id": {
                "type": "integer",
                "description": "Target ACC manifest identifier.",
                "example": 12345,
            },
            "updates": {
                "type": "array",
                "description": "List of updated field names.",
                "items": {"type": "string"},
                "example": ["definition", "namespace_id"],
            },
        },
        "required": ["acc_manifest_id", "updates"],
    },
)
async def update_acc(
    acc_manifest_id: Annotated[int, Field(gt=0, description="Target ACC manifest identifier.")],
    based_acc_manifest_id: Annotated[
        int | None,
        Field(
            default=None,
            ge=0,
            description=(
                "Updated base ACC manifest identifier. When set to a positive value, if the base ACC is in the same "
                "library as the target ACC it must be from the target ACC release; otherwise its release must be one "
                "of the target ACC release dependencies. Omit to leave unchanged. Use 0 to clear the current base ACC."
            ),
        ),
    ],
    object_class_term: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Updated object class term. In CCTS, this is the term that represents the activity or object the "
                "ACC stands for. It serves as the basis for the ACC DEN and for the DENs of the ASCC and BCC "
                "properties under that ACC. Omit to leave unchanged."
            ),
        ),
    ],
    component_type: Annotated[
        Literal[
            "Base",
            "Semantics",
            "Extension",
            "SemanticGroup",
            "UserExtensionGroup",
            "Embedded",
            "OAGIS10Nouns",
            "OAGIS10BODs",
            "BOD",
            "Verb",
            "Noun",
            "Choice",
            "AttributeGroup",
        ] | None,
        Field(
            default=None,
            description=(
                "OAGIS component type. Use `Base` only when this ACC is intended to be the "
                "base ACC for other ACCs. Use `Extension` for a developer extension ACC that end-users may extend "
                "later in BIEs. Use `SemanticGroup` for a grouping ACC whose associations are flattened in BIEs. "
                "Otherwise use `Semantics`. Omit to leave unchanged."
            ),
        ),
    ],
    definition: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Definition text. This is the explanatory text that describes what the ACC means. "
                "Omit to leave unchanged. Pass an empty string to clear it."
            ),
        ),
    ],
    definition_source: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Definition source. Use this to record where the definition came from, such as a "
                "specification, standard, or reference URL. Omit to leave unchanged. Pass an empty string to clear it."
            ),
        ),
    ],
    is_abstract: Annotated[
        bool | None,
        Field(
            default=None,
            description=(
                "Whether this ACC should be abstract. If `component_type` is `Base`, this is usually `true`, "
                "because the ACC is meant to serve as a base ACC for other ACCs. Omit to leave unchanged."
            ),
        ),
    ],
    deprecated: Annotated[
        bool | None,
        Field(default=None, description="Updated deprecation flag. Omit to leave unchanged."),
    ],
    namespace_id: Annotated[
        int | None,
        Field(
            default=None,
            ge=0,
            description=(
                "Updated namespace identifier. When set to a positive value, it must belong to the same library as the ACC release. "
                "Omit to leave unchanged. Use 0 to clear the namespace."
            ),
        ),
    ],
    ctx: Context,
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> UpdateAccResponse:
    """
    Update mutable ACC fields.

    ACC updates follow connectCenter's owner-edit rules:
    - the ACC must currently be in `WIP`
    - only the owner or an administrator can update it
    - changing the object class term also updates the ACC manifest DEN and dependent relationship DENs
    - changing the base ACC must follow the same-library or dependency-release rule and cannot introduce inherited ASCCP or BCCP conflicts

    Notes:
    - Omit a parameter to leave it unchanged
    - pass `""` for `definition` or `definition_source` to clear those values
    - pass `0` for `based_acc_manifest_id` to clear the current base ACC
    - pass `0` for `namespace_id` to clear the namespace
    """
    try:
        if based_acc_manifest_id is not None and based_acc_manifest_id > 0:
            warnings = await core_component_service.get_set_base_acc_warnings(
                acc_manifest_id=acc_manifest_id,
                based_acc_manifest_id=based_acc_manifest_id,
            )
            await _elicit_on_acc_structure_warnings(
                ctx=ctx,
                warnings=warnings,
                prompt="Changing the base ACC may create repeated field names or sequence issues later.",
                declined_message="Updating the ACC was not confirmed.",
                cancelled_message="Updating the ACC was cancelled.",
            )
        result = await core_component_service.update_acc(
            acc_manifest_id=acc_manifest_id,
            object_class_term=UNSET if object_class_term is None else object_class_term,
            component_type=UNSET if component_type is None else component_type,
            definition=UNSET if definition is None else definition,
            definition_source=UNSET if definition_source is None else definition_source,
            is_abstract=UNSET if is_abstract is None else is_abstract,
            deprecated=UNSET if deprecated is None else deprecated,
            based_acc_manifest_id=UNSET
            if based_acc_manifest_id is None
            else (None if based_acc_manifest_id == 0 else based_acc_manifest_id),
            namespace_id=UNSET if namespace_id is None else (None if namespace_id == 0 else namespace_id),
        )
        return UpdateAccResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to update ACC {acc_manifest_id}.") from exc


@mcp.tool(
    name="transfer_acc_ownership",
    description="Transfer ownership of an ACC (Aggregate Core Component) to another user.",
    output_schema={
        "type": "object",
        "description": "Response containing the ACC manifest identifier and changed fields.",
        "properties": {
            "acc_manifest_id": {"type": "integer"},
            "updates": {"type": "array", "items": {"type": "string"}},
        },
        "required": ["acc_manifest_id", "updates"],
    },
)
async def transfer_acc_ownership(
    acc_manifest_id: Annotated[int, Field(gt=0, description="Target ACC manifest identifier.")],
    new_owner_user_id: Annotated[int, Field(gt=0, description="User ID of the new owner.")],
    ctx: Context,
    core_component_service: CoreComponentService = Depends(get_core_component_service),
    app_user_service: AppUserService = Depends(get_app_user_service),
) -> TransferAccOwnershipResponse:
    """Transfer ACC ownership to another user after confirmation."""
    try:
        row = await core_component_service.get_acc(acc_manifest_id)
        if row is None:
            raise ToolError(
                f"The ACC with manifest ID {acc_manifest_id} was not found. Please check the ID and try again."
            )
        target_user = await app_user_service.get(new_owner_user_id)
        if target_user is None:
            raise ToolError(
                f"The target user with ID {new_owner_user_id} was not found. Please check the ID and try again."
            )
        target_user_label = target_user.login_id
        if target_user.username and target_user.username != target_user.login_id:
            target_user_label = f"{target_user.login_id} ({target_user.username})"

        elicit_result = await ctx.elicit(
            message=(
                f"Are you sure you want to transfer ownership of '{row.den}' "
                f"to {target_user_label}?"
            ),
            response_type=None,
        )
        match elicit_result:
            case AcceptedElicitation():
                payload = await core_component_service.transfer_acc_ownership(
                    acc_manifest_id=acc_manifest_id,
                    target_user_id=new_owner_user_id,
                )
                updates = [] if int(new_owner_user_id) == int(row.owner.user_id) else ["owner_user_id"]
                return TransferAccOwnershipResponse(
                    acc_manifest_id=payload.acc_manifest_id,
                    updates=updates,
                )
            case DeclinedElicitation():
                raise ToolError("ACC ownership transfer declined by user.")
            case CancelledElicitation():
                raise ToolError("ACC ownership transfer cancelled by user.")
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to transfer ownership of ACC {acc_manifest_id}.") from exc


@mcp.tool(
    name="update_ascc",
    description="Update mutable ASCC (Association Core Component) fields.",
    output_schema={
        "type": "object",
        "description": "Response containing the updated ASCC manifest identifier and changed fields.",
        "properties": {
            "ascc_manifest_id": {"type": "integer"},
            "updates": {"type": "array", "items": {"type": "string"}},
        },
        "required": ["ascc_manifest_id", "updates"],
    },
)
async def update_ascc(
    ascc_manifest_id: Annotated[int, Field(gt=0, description="Target ASCC manifest identifier.")],
    index: Annotated[
        int | None,
        Field(
            default=None,
            description=(
                "Optional zero-based insertion index. Use `0` to place the association first, `-1` to place it last, "
                "or another non-negative index to place it at that position in the sequence. "
                "Mutually exclusive with all `after_*` and `before_*` options."
            ),
        ),
    ] = None,
    after_ascc_manifest_id: Annotated[
        int | None,
        Field(default=None, gt=0, description="Place the association after this ASCC manifest identifier. Mutually exclusive with `index` and all other `after_*`/`before_*` options."),
    ] = None,
    after_bcc_manifest_id: Annotated[
        int | None,
        Field(default=None, gt=0, description="Place the association after this BCC manifest identifier. Mutually exclusive with `index` and all other `after_*`/`before_*` options."),
    ] = None,
    before_ascc_manifest_id: Annotated[
        int | None,
        Field(default=None, gt=0, description="Place the association before this ASCC manifest identifier. Mutually exclusive with `index` and all other `after_*`/`before_*` options."),
    ] = None,
    before_bcc_manifest_id: Annotated[
        int | None,
        Field(default=None, gt=0, description="Place the association before this BCC manifest identifier. Mutually exclusive with `index` and all other `after_*`/`before_*` options."),
    ] = None,
    cardinality_min: Annotated[
        int | None,
        Field(
            default=None,
            ge=0,
            description=(
                "Minimum cardinality. Use `0` or a positive integer. If `cardinality_max` is also "
                "provided and is not `-1`, this value must be less than or equal to it. Omit to leave unchanged."
            ),
        ),
    ] = None,
    cardinality_max: Annotated[
        int | None,
        Field(
            default=None,
            ge=-1,
            description=(
                "Maximum cardinality. Use `-1` for unbounded, or `0` or a positive integer for a bounded "
                "maximum. If it is not `-1`, it must be greater than or equal to `cardinality_min`. If the BCC is "
                "`Attribute`, this value must be `0` or `1`; `-1` is not allowed. Omit to leave unchanged."
            ),
        ),
    ] = None,
    definition: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Definition text. This is the explanatory text that describes what the BCC means. "
                "Omit to leave unchanged. Pass an empty string to clear it."
            ),
        ),
    ] = None,
    definition_source: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Definition source. Use this to record where the definition came from, such as a "
                "specification, standard, or reference URL. Omit to leave unchanged. Pass an empty string to "
                "clear it."
            ),
        ),
    ] = None,
    deprecated: Annotated[
        bool | None,
        Field(default=None, description="Updated deprecation flag. Omit to leave unchanged."),
    ] = None,
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> UpdateAsccResponse:
    """Update mutable ASCC relationship fields."""
    try:
        result = await core_component_service.update_ascc(
            ascc_manifest_id=ascc_manifest_id,
            index=UNSET if index is None else index,
            after_ascc_manifest_id=UNSET if after_ascc_manifest_id is None else after_ascc_manifest_id,
            after_bcc_manifest_id=UNSET if after_bcc_manifest_id is None else after_bcc_manifest_id,
            before_ascc_manifest_id=UNSET if before_ascc_manifest_id is None else before_ascc_manifest_id,
            before_bcc_manifest_id=UNSET if before_bcc_manifest_id is None else before_bcc_manifest_id,
            cardinality_min=UNSET if cardinality_min is None else cardinality_min,
            cardinality_max=UNSET if cardinality_max is None else cardinality_max,
            definition=UNSET if definition is None else definition,
            definition_source=UNSET if definition_source is None else definition_source,
            deprecated=UNSET if deprecated is None else deprecated,
        )
        return UpdateAsccResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to update ASCC {ascc_manifest_id}.") from exc


@mcp.tool(
    name="update_bcc",
    description="Update mutable BCC (Basic Core Component) fields.",
    output_schema={
        "type": "object",
        "description": "Response containing the updated BCC manifest identifier and changed fields.",
        "properties": {
            "bcc_manifest_id": {"type": "integer"},
            "updates": {"type": "array", "items": {"type": "string"}},
        },
        "required": ["bcc_manifest_id", "updates"],
    },
)
async def update_bcc(
    bcc_manifest_id: Annotated[int, Field(gt=0, description="Target BCC manifest identifier.")],
    index: Annotated[
        int | None,
        Field(
            default=None,
            description=(
                "Optional zero-based insertion index. Use `0` to place the association first, `-1` to place it last, "
                "or another non-negative index to place it at that position in the sequence. "
                "Mutually exclusive with all `after_*` and `before_*` options."
            ),
        ),
    ] = None,
    after_ascc_manifest_id: Annotated[
        int | None,
        Field(default=None, gt=0, description="Place the association after this ASCC manifest identifier. Mutually exclusive with `index` and all other `after_*`/`before_*` options."),
    ] = None,
    after_bcc_manifest_id: Annotated[
        int | None,
        Field(default=None, gt=0, description="Place the association after this BCC manifest identifier. Mutually exclusive with `index` and all other `after_*`/`before_*` options."),
    ] = None,
    before_ascc_manifest_id: Annotated[
        int | None,
        Field(default=None, gt=0, description="Place the association before this ASCC manifest identifier. Mutually exclusive with `index` and all other `after_*`/`before_*` options."),
    ] = None,
    before_bcc_manifest_id: Annotated[
        int | None,
        Field(default=None, gt=0, description="Place the association before this BCC manifest identifier. Mutually exclusive with `index` and all other `after_*`/`before_*` options."),
    ] = None,
    entity_type: Annotated[
        BccEntityTypeUpdate | None,
        Field(
            default=None,
            description="Updated entity type. Use `Element` or `Attribute`. Integer aliases `1` and `0` are also accepted. Omit to leave unchanged.",
        ),
    ] = None,
    cardinality_min: Annotated[
        int | None,
        Field(
            default=None,
            ge=0,
            description=(
                "Minimum cardinality. Use `0` or a positive integer. If `cardinality_max` is also "
                "provided and is not `-1`, this value must be less than or equal to it. Omit to leave unchanged."
            ),
        ),
    ] = None,
    cardinality_max: Annotated[
        int | None,
        Field(
            default=None,
            ge=-1,
            description=(
                "Maximum cardinality. Use `-1` for unbounded, or `0` or a positive integer for a bounded "
                "maximum. If it is not `-1`, it must be greater than or equal to `cardinality_min`. If `entity_type` "
                "changes from `Element` to `Attribute`, `-1` or a value greater than `1` is normalized to `1`. "
                "Omit to leave unchanged."
            ),
        ),
    ] = None,
    definition: Annotated[
        str | None,
        Field(default=None, description="Definition text. Omit to leave unchanged. Pass an empty string to clear it."),
    ] = None,
    definition_source: Annotated[
        str | None,
        Field(default=None, description="Definition source. Omit to leave unchanged. Pass an empty string to clear it."),
    ] = None,
    deprecated: Annotated[
        bool | None,
        Field(default=None, description="Updated deprecation flag. Omit to leave unchanged."),
    ] = None,
    is_nillable: Annotated[
        bool | None,
        Field(default=None, description="Updated nillable flag. Omit to leave unchanged."),
    ] = None,
    value_constraint: Annotated[
        BccValueConstraintInput | None,
        Field(
            default=None,
            description=(
                "Updated value constraint. Provide `default_value` to set a default when the element is omitted, "
                "or `fixed_value` to require one exact value. Omit to leave unchanged."
            ),
        ),
    ] = None,
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> UpdateBccResponse:
    """Update mutable BCC relationship fields."""
    try:
        result = await core_component_service.update_bcc(
            bcc_manifest_id=bcc_manifest_id,
            index=UNSET if index is None else index,
            after_ascc_manifest_id=UNSET if after_ascc_manifest_id is None else after_ascc_manifest_id,
            after_bcc_manifest_id=UNSET if after_bcc_manifest_id is None else after_bcc_manifest_id,
            before_ascc_manifest_id=UNSET if before_ascc_manifest_id is None else before_ascc_manifest_id,
            before_bcc_manifest_id=UNSET if before_bcc_manifest_id is None else before_bcc_manifest_id,
            entity_type=UNSET if entity_type is None else _map_bcc_entity_type_update(entity_type),
            cardinality_min=UNSET if cardinality_min is None else cardinality_min,
            cardinality_max=UNSET if cardinality_max is None else cardinality_max,
            definition=UNSET if definition is None else definition,
            definition_source=UNSET if definition_source is None else definition_source,
            deprecated=UNSET if deprecated is None else deprecated,
            is_nillable=UNSET if is_nillable is None else is_nillable,
            default_value=UNSET if value_constraint is None else value_constraint.default_value,
            fixed_value=UNSET if value_constraint is None else value_constraint.fixed_value,
        )
        return UpdateBccResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to update BCC {bcc_manifest_id}.") from exc


@mcp.tool(
    name="add_tags_to_acc",
    description="Attach one or more tags to an ACC (Aggregate Core Component).",
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def add_tags_to_acc(
    acc_manifest_id: Annotated[int, Field(gt=0, description="Target ACC manifest identifier.")],
    tag_id: Annotated[
        list[int],
        Field(description="Tag identifier list to attach. Use `List tags` to discover valid tag IDs."),
    ],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> dict[str, object]:
    """
    Add tags to an ACC.

    ACC tag updates follow connectCenter's owner-edit rules:
    - the ACC must currently be in `WIP`
    - only the owner or an administrator can update it
    - duplicate requested tags are ignored after normalization
    """
    try:
        await core_component_service.add_acc_tags(
            acc_manifest_id=acc_manifest_id,
            tag_id=tag_id,
        )
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to add tags to ACC {acc_manifest_id}.") from exc


@mcp.tool(
    name="remove_tags_from_acc",
    description="Detach one or more tags from an ACC (Aggregate Core Component).",
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def remove_tags_from_acc(
    acc_manifest_id: Annotated[int, Field(gt=0, description="Target ACC manifest identifier.")],
    tag_id: Annotated[
        list[int],
        Field(description="Tag identifier list to remove. Use `List tags` to discover valid tag IDs."),
    ],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> dict[str, object]:
    """
    Remove tags from an ACC.

    ACC tag updates follow connectCenter's owner-edit rules:
    - the ACC must currently be in `WIP`
    - only the owner or an administrator can update it
    - tags not currently attached are ignored after normalization
    """
    try:
        await core_component_service.remove_acc_tags(
            acc_manifest_id=acc_manifest_id,
            tag_id=tag_id,
        )
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to remove tags from ACC {acc_manifest_id}.") from exc


@mcp.tool(
    name="change_acc_state",
    description="Change the lifecycle state of an ACC (Aggregate Core Component).",
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def change_acc_state(
    acc_manifest_id: Annotated[int, Field(gt=0, description="Target ACC manifest identifier.")],
    state: Annotated[
        Literal["Deleted", "WIP", "Draft", "QA", "Candidate", "Production"],
        Field(description="Target lifecycle state."),
    ],
    ctx: Context,
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> dict[str, object]:
    """
    Change an ACC lifecycle state according to connectCenter rules.

    Valid transitions depend on the ACC release branch:
    - `Working` release ACCs: `Deleted -> WIP`, `WIP -> Deleted|Draft`, `Draft -> WIP|Candidate`, `Candidate -> WIP`
    - non-`Working` release ACCs: `Deleted -> WIP`, `WIP -> Deleted|QA`, `QA -> WIP|Production`, `Production` is terminal

    Additional rules:
    - moving from `WIP` to `Deleted` marks the ACC to be deleted later
    - only the owner or an administrator can change the ACC state, except when restoring `Deleted -> WIP`
    - the ACC must have a namespace before it can move to `QA`
    - if the ACC is missing a definition when moving to `QA`, the tool asks for one first
    - moving a non-`Working` ACC to `Production` requires explicit end-user confirmation
    - states such as `ReleaseDraft` and `Published` are not handled by this service
    """
    try:
        row = await core_component_service.get_acc(acc_manifest_id)
        if row is None:
            raise ToolError(
                f"The ACC with manifest ID {acc_manifest_id} was not found. Please check the ID and try again."
            )

        if state == "QA":
            if row.namespace is None:
                raise ToolError(
                    f"'{row.den}' needs a namespace before it can move to the 'QA' state."
                )
            if not str(row.definition or "").strip():
                elicit_result = await ctx.elicit(
                    message=(
                        f"'{row.den}' needs a definition before it can move to the 'QA' state.\n\n"
                        "Please enter a short definition for this ACC."
                    ),
                    response_type=MissingAccDefinitionResponse,
                )
                match elicit_result:
                    case AcceptedElicitation(data=data):
                        definition = str(data.definition).strip()
                        if not definition:
                            raise ToolError(
                                f"'{row.den}' still needs a definition before it can move to the 'QA' state."
                            )
                        await core_component_service.update_acc(
                            acc_manifest_id=acc_manifest_id,
                            definition=definition,
                        )
                    case DeclinedElicitation():
                        raise ToolError("Changing the ACC state was not confirmed.")
                    case CancelledElicitation():
                        raise ToolError("Changing the ACC state was cancelled.")

        if row.release.release_num != "Working" and row.state == "QA" and state == "Production":
            elicit_result = await ctx.elicit(
                message=(
                    f"Are you sure you want to change '{row.den}' to the 'Production' state?\n\n"
                    "This is the end-user production transition for a non-'Working' release ACC."
                ),
                response_type=None,
            )
            match elicit_result:
                case AcceptedElicitation():
                    pass
                case DeclinedElicitation():
                    raise ToolError("ACC state change was not confirmed.")
                case CancelledElicitation():
                    raise ToolError("ACC state change was cancelled.")

        await core_component_service.change_acc_state(
            acc_manifest_id=acc_manifest_id,
            state=state,
        )
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to change the ACC state for {acc_manifest_id}.") from exc


@mcp.tool(
    name="revise_or_amend_acc",
    description=(
        "Create a new editable ACC (Aggregate Core Component) revision from a stable ACC revision. "
        "This tool revises developer-side ACCs and amends end-user ACCs using the same operation."
    ),
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def revise_or_amend_acc(
    acc_manifest_id: Annotated[int, Field(gt=0, description="Target ACC manifest identifier.")],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> dict[str, object]:
    """
    Revise or amend an ACC according to connectCenter rules.

    Rules:
    - developer-side ACCs can be revised only from the `Published` state in the `Working` release
    - end-user ACCs can be amended only from the `Production` state in a non-`Working` release
    - the requester and the ACC owner must belong to the same role family
    - the operation creates a new editable `WIP` revision for the ACC
    """
    try:
        await core_component_service.revise_acc(acc_manifest_id=acc_manifest_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to revise or amend ACC {acc_manifest_id}.") from exc


@mcp.tool(
    name="cancel_acc",
    description="Cancel the current ACC (Aggregate Core Component) revision and restore the previous stable revision.",
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def cancel_acc(
    acc_manifest_id: Annotated[int, Field(gt=0, description="Target ACC manifest identifier.")],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> dict[str, object]:
    """
    Cancel the current ACC revision according to connectCenter rules.

    Rules:
    - only ACCs in the `WIP` state can be cancelled
    - the requester and the ACC owner must belong to the same role family
    - the ACC must be on the requester's allowed branch (`Working` for developers, non-`Working` for end-users)
    - cancelling restores the previous stable ACC revision
    """
    try:
        await core_component_service.cancel_acc(acc_manifest_id=acc_manifest_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to cancel ACC {acc_manifest_id}.") from exc


@mcp.tool(
    name="discard_acc",
    description="Discard a Deleted ACC (Aggregate Core Component) and its direct ACC-owned records permanently.",
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def discard_acc(
    acc_manifest_id: Annotated[int, Field(gt=0, description="Target ACC manifest identifier.")],
    ctx: Context,
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> dict[str, object]:
    """
    Discard a Deleted ACC from the database.

    Rules:
    - the ACC must already be in the `Deleted` state
    - related ASCCPs that still use the ACC as their role-of ACC must be discarded first
    - derived ACCs must be discarded first
    - this operation is irreversible
    """
    row = await core_component_service.get_acc(acc_manifest_id)
    if row is None:
        raise ToolError(
            f"The ACC with manifest ID {acc_manifest_id} was not found. Please check the ID and try again."
        )
    if row.state != "Deleted":
        raise ToolError(
            f"The ACC '{row.den}' is currently in the '{row.state}' state. "
            "Only ACCs in the 'Deleted' state can be discarded."
        )

    elicit_result = await ctx.elicit(
        message=(
            f"Are you sure you want to discard '{row.den}' permanently?\n\n"
            "This removes the ACC and its direct ACC-owned records from the database and cannot be undone."
        ),
        response_type=None,
    )
    match elicit_result:
        case AcceptedElicitation():
            pass
        case DeclinedElicitation():
            raise ToolError("ACC discard was not confirmed.")
        case CancelledElicitation():
            raise ToolError("ACC discard was cancelled.")

    try:
        await core_component_service.discard_acc(acc_manifest_id=acc_manifest_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to discard ACC {acc_manifest_id}.") from exc


@mcp.tool(name="update_asccp", description="Update selected mutable fields of an ASCCP (Association Core Component Property).", output_schema={"type": "object", "description": "Response containing the updated ASCCP manifest identifier and changed fields.", "properties": {"asccp_manifest_id": {"type": "integer"}, "updates": {"type": "array", "items": {"type": "string"}}}, "required": ["asccp_manifest_id", "updates"]})
async def update_asccp(
    asccp_manifest_id: Annotated[int, Field(gt=0, description="Target ASCCP manifest identifier.")],
    ctx: Context,
    role_of_acc_manifest_id: Annotated[
        int | None,
        Field(
            default=None,
            gt=0,
            description="Updated role ACC manifest identifier. This identifies the ACC that this ASCCP points to as the associated ACC in the relationship. "
                        "If the role ACC is in the same library as `release_id`, it must be from that release. If it is in a different library, "
                        "its release must be one of the target release dependencies. Omit to leave unchanged.",
        ),
    ] = None,
    property_term: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Updated property term. In CCTS, this is a semantically meaningful name for the characteristic "
                "that expresses the nature of the association to the associated ACC. Omit to leave unchanged."
            ),
        ),
    ] = None,
    definition: Annotated[
        str | None,
        Field(
            default=None,
            description="Definition text. This is the explanatory text that describes what the ASCCP means. Omit to leave unchanged.",
        ),
    ] = None,
    definition_source: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Definition source. Use this to record where the definition came from, such as a "
                "specification, standard, or reference URL. Omit to leave unchanged."
            ),
        ),
    ] = None,
    reusable_indicator: Annotated[bool | None, Field(default=None, description="Updated reusable indicator. Omit to leave unchanged.")] = None,
    deprecated: Annotated[bool | None, Field(default=None, description="Updated deprecation flag. Omit to leave unchanged.")] = None,
    is_nillable: Annotated[bool | None, Field(default=None, description="Updated nillable flag. Omit to leave unchanged.")] = None,
    namespace_id: Annotated[int | None, Field(default=None, gt=0, description="Updated namespace identifier. Omit to leave unchanged.")] = None,
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> UpdateAsccpResponse:
    """Update mutable ASCCP fields."""
    try:
        warnings = await core_component_service.get_update_asccp_warnings(
            asccp_manifest_id=asccp_manifest_id,
            role_of_acc_manifest_id=AccManifestId(int(role_of_acc_manifest_id)) if role_of_acc_manifest_id is not None else UNSET,
            property_term=property_term if property_term is not None else UNSET,
        )
        await _elicit_on_acc_structure_warnings(
            ctx=ctx,
            warnings=warnings,
            prompt="Updating this ASCCP may affect ACCs that already use it.",
            declined_message="Updating the ASCCP was not confirmed.",
            cancelled_message="Updating the ASCCP was cancelled.",
        )
        result = await core_component_service.update_asccp(
            asccp_manifest_id=asccp_manifest_id,
            role_of_acc_manifest_id=AccManifestId(int(role_of_acc_manifest_id)) if role_of_acc_manifest_id is not None else UNSET,
            property_term=property_term if property_term is not None else UNSET,
            definition=definition if definition is not None else UNSET,
            definition_source=definition_source if definition_source is not None else UNSET,
            reusable_indicator=reusable_indicator if reusable_indicator is not None else UNSET,
            deprecated=deprecated if deprecated is not None else UNSET,
            is_nillable=is_nillable if is_nillable is not None else UNSET,
            namespace_id=namespace_id if namespace_id is not None else UNSET,
            allow_warnings=True,
        )
        return UpdateAsccpResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to update ASCCP {asccp_manifest_id}.") from exc


@mcp.tool(
    name="transfer_asccp_ownership",
    description="Transfer ownership of an ASCCP (Association Core Component Property) to another user.",
    output_schema={
        "type": "object",
        "description": "Response containing the ASCCP manifest identifier and changed fields.",
        "properties": {
            "asccp_manifest_id": {"type": "integer"},
            "updates": {"type": "array", "items": {"type": "string"}},
        },
        "required": ["asccp_manifest_id", "updates"],
    },
)
async def transfer_asccp_ownership(
    asccp_manifest_id: Annotated[int, Field(gt=0, description="Target ASCCP manifest identifier.")],
    new_owner_user_id: Annotated[int, Field(gt=0, description="User ID of the new owner.")],
    ctx: Context,
    core_component_service: CoreComponentService = Depends(get_core_component_service),
    app_user_service: AppUserService = Depends(get_app_user_service),
) -> TransferAsccpOwnershipResponse:
    """Transfer ASCCP ownership to another user after confirmation."""
    try:
        row = await core_component_service.get_asccp(asccp_manifest_id)
        if row is None:
            raise ToolError(
                f"The ASCCP with manifest ID {asccp_manifest_id} was not found. Please check the ID and try again."
            )
        target_user = await app_user_service.get(new_owner_user_id)
        if target_user is None:
            raise ToolError(
                f"The target user with ID {new_owner_user_id} was not found. Please check the ID and try again."
            )
        target_user_label = target_user.login_id
        if target_user.username and target_user.username != target_user.login_id:
            target_user_label = f"{target_user.login_id} ({target_user.username})"

        elicit_result = await ctx.elicit(
            message=(
                f"Are you sure you want to transfer ownership of '{row.den}' "
                f"to {target_user_label}?"
            ),
            response_type=None,
        )
        match elicit_result:
            case AcceptedElicitation():
                payload = await core_component_service.transfer_asccp_ownership(
                    asccp_manifest_id=asccp_manifest_id,
                    target_user_id=new_owner_user_id,
                )
                updates = [] if int(new_owner_user_id) == int(row.owner.user_id) else ["owner_user_id"]
                return TransferAsccpOwnershipResponse(
                    asccp_manifest_id=payload.asccp_manifest_id,
                    updates=updates,
                )
            case DeclinedElicitation():
                raise ToolError("ASCCP ownership transfer declined by user.")
            case CancelledElicitation():
                raise ToolError("ASCCP ownership transfer cancelled by user.")
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to transfer ownership of ASCCP {asccp_manifest_id}.") from exc


@mcp.tool(name="update_bccp", description="Update selected mutable fields of a BCCP (Basic Core Component Property).", output_schema={"type": "object", "description": "Response containing the updated BCCP manifest identifier and changed fields.", "properties": {"bccp_manifest_id": {"type": "integer"}, "updates": {"type": "array", "items": {"type": "string"}}}, "required": ["bccp_manifest_id", "updates"]})
async def update_bccp(
    bccp_manifest_id: Annotated[int, Field(gt=0, description="Target BCCP manifest identifier.")],
    ctx: Context,
    bdt_manifest_id: Annotated[int | None, Field(default=None, gt=0, description="Target BDT manifest identifier. Omit to leave unchanged. The selected data type must already be a BDT, which means its base DT link is set. If the BDT is in the same library as the BCCP, it must be from the BCCP release. If it is in a different library, its release must be one of the BCCP release dependencies.")] = None,
    property_term: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Updated property term. In CCTS, this is a semantically meaningful name for a unique "
                "characteristic that can be used in an ACC object class. Omit to leave unchanged."
            ),
        ),
    ] = None,
    definition: Annotated[
        str | None,
        Field(
            default=None,
            description="Definition text. This is the explanatory text that describes what the BCCP means. Omit to leave unchanged.",
        ),
    ] = None,
    definition_source: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Definition source. Use this to record where the definition came from, such as a "
                "specification, standard, or reference URL. Omit to leave unchanged."
            ),
        ),
    ] = None,
    deprecated: Annotated[bool | None, Field(default=None, description="Updated deprecation flag. Omit to leave unchanged.")] = None,
    is_nillable: Annotated[bool | None, Field(default=None, description="Updated nillable flag. Omit to leave unchanged.")] = None,
    namespace_id: Annotated[int | None, Field(default=None, gt=0, description="Updated namespace identifier. Omit to leave unchanged.")] = None,
    default_value: Annotated[str | None, Field(default=None, description="Updated default value. Omit to leave unchanged.")] = None,
    fixed_value: Annotated[str | None, Field(default=None, description="Updated fixed value. Omit to leave unchanged.")] = None,
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> UpdateBccpResponse:
    """Update mutable BCCP fields."""
    try:
        warnings = await core_component_service.get_update_bccp_warnings(
            bccp_manifest_id=bccp_manifest_id,
            property_term=property_term if property_term is not None else UNSET,
        )
        await _elicit_on_acc_structure_warnings(
            ctx=ctx,
            warnings=warnings,
            prompt="Updating this BCCP may affect ACCs that already use it.",
            declined_message="Updating the BCCP was not confirmed.",
            cancelled_message="Updating the BCCP was cancelled.",
        )
        result = await core_component_service.update_bccp(
            bccp_manifest_id=bccp_manifest_id,
            bdt_manifest_id=DataTypeManifestId(int(bdt_manifest_id)) if bdt_manifest_id is not None else UNSET,
            property_term=property_term if property_term is not None else UNSET,
            definition=definition if definition is not None else UNSET,
            definition_source=definition_source if definition_source is not None else UNSET,
            deprecated=deprecated if deprecated is not None else UNSET,
            is_nillable=is_nillable if is_nillable is not None else UNSET,
            namespace_id=namespace_id if namespace_id is not None else UNSET,
            default_value=default_value if default_value is not None else UNSET,
            fixed_value=fixed_value if fixed_value is not None else UNSET,
            allow_warnings=True,
        )
        return UpdateBccpResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to update BCCP {bccp_manifest_id}.") from exc


@mcp.tool(
    name="transfer_bccp_ownership",
    description="Transfer ownership of a BCCP (Basic Core Component Property) to another user.",
    output_schema={
        "type": "object",
        "description": "Response containing the BCCP manifest identifier and changed fields.",
        "properties": {
            "bccp_manifest_id": {"type": "integer"},
            "updates": {"type": "array", "items": {"type": "string"}},
        },
        "required": ["bccp_manifest_id", "updates"],
    },
)
async def transfer_bccp_ownership(
    bccp_manifest_id: Annotated[int, Field(gt=0, description="Target BCCP manifest identifier.")],
    new_owner_user_id: Annotated[int, Field(gt=0, description="User ID of the new owner.")],
    ctx: Context,
    core_component_service: CoreComponentService = Depends(get_core_component_service),
    app_user_service: AppUserService = Depends(get_app_user_service),
) -> TransferBccpOwnershipResponse:
    """Transfer BCCP ownership to another user after confirmation."""
    try:
        row = await core_component_service.get_bccp(bccp_manifest_id)
        if row is None:
            raise ToolError(
                f"The BCCP with manifest ID {bccp_manifest_id} was not found. Please check the ID and try again."
            )
        target_user = await app_user_service.get(new_owner_user_id)
        if target_user is None:
            raise ToolError(
                f"The target user with ID {new_owner_user_id} was not found. Please check the ID and try again."
            )
        target_user_label = target_user.login_id
        if target_user.username and target_user.username != target_user.login_id:
            target_user_label = f"{target_user.login_id} ({target_user.username})"

        elicit_result = await ctx.elicit(
            message=(
                f"Are you sure you want to transfer ownership of '{row.den}' "
                f"to {target_user_label}?"
            ),
            response_type=None,
        )
        match elicit_result:
            case AcceptedElicitation():
                payload = await core_component_service.transfer_bccp_ownership(
                    bccp_manifest_id=bccp_manifest_id,
                    target_user_id=new_owner_user_id,
                )
                updates = [] if int(new_owner_user_id) == int(row.owner.user_id) else ["owner_user_id"]
                return TransferBccpOwnershipResponse(
                    bccp_manifest_id=payload.bccp_manifest_id,
                    updates=updates,
                )
            case DeclinedElicitation():
                raise ToolError("BCCP ownership transfer declined by user.")
            case CancelledElicitation():
                raise ToolError("BCCP ownership transfer cancelled by user.")
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to transfer ownership of BCCP {bccp_manifest_id}.") from exc


@mcp.tool(name="change_asccp_state", description="Change the lifecycle state of an ASCCP (Association Core Component Property).", output_schema=EMPTY_OUTPUT_SCHEMA)
async def change_asccp_state(
    asccp_manifest_id: Annotated[int, Field(gt=0, description="Target ASCCP manifest identifier.")],
    state: Annotated[Literal["Deleted", "WIP", "Draft", "QA", "Candidate", "Production"], Field(description="Target lifecycle state.")],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> dict[str, object]:
    """
    Change an ASCCP lifecycle state according to connectCenter rules.

    Valid transitions depend on the ASCCP release branch:
    - `Working` release ASCCPs: `Deleted -> WIP`, `WIP -> Deleted|Draft`, `Draft -> WIP|Candidate`, `Candidate -> WIP`
    - non-`Working` release ASCCPs: `Deleted -> WIP`, `WIP -> Deleted|QA`, `QA -> WIP|Production`, `Production` is terminal
    """
    try:
        await core_component_service.change_asccp_state(asccp_manifest_id=asccp_manifest_id, state=state)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to change the ASCCP state for {asccp_manifest_id}.") from exc


@mcp.tool(name="change_bccp_state", description="Change the lifecycle state of a BCCP (Basic Core Component Property).", output_schema=EMPTY_OUTPUT_SCHEMA)
async def change_bccp_state(
    bccp_manifest_id: Annotated[int, Field(gt=0, description="Target BCCP manifest identifier.")],
    state: Annotated[Literal["Deleted", "WIP", "Draft", "QA", "Candidate", "Production"], Field(description="Target lifecycle state.")],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> dict[str, object]:
    """
    Change a BCCP lifecycle state according to connectCenter rules.

    Valid transitions depend on the BCCP release branch:
    - `Working` release BCCPs: `Deleted -> WIP`, `WIP -> Deleted|Draft`, `Draft -> WIP|Candidate`, `Candidate -> WIP`
    - non-`Working` release BCCPs: `Deleted -> WIP`, `WIP -> Deleted|QA`, `QA -> WIP|Production`, `Production` is terminal
    """
    try:
        await core_component_service.change_bccp_state(bccp_manifest_id=bccp_manifest_id, state=state)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to change the BCCP state for {bccp_manifest_id}.") from exc


@mcp.tool(name="add_tags_to_asccp", description="Attach one or more tags to an ASCCP (Association Core Component Property).", output_schema=EMPTY_OUTPUT_SCHEMA)
async def add_tags_to_asccp(
    asccp_manifest_id: Annotated[int, Field(gt=0, description="Target ASCCP manifest identifier.")],
    tag_id: Annotated[list[int], Field(description="Tag identifier list to attach.")],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> dict[str, object]:
    """Attach tags to a WIP ASCCP."""
    try:
        await core_component_service.add_asccp_tags(asccp_manifest_id=asccp_manifest_id, tag_id=tag_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to add tags to ASCCP {asccp_manifest_id}.") from exc


@mcp.tool(name="remove_tags_from_asccp", description="Detach one or more tags from an ASCCP (Association Core Component Property).", output_schema=EMPTY_OUTPUT_SCHEMA)
async def remove_tags_from_asccp(
    asccp_manifest_id: Annotated[int, Field(gt=0, description="Target ASCCP manifest identifier.")],
    tag_id: Annotated[list[int], Field(description="Tag identifier list to remove.")],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> dict[str, object]:
    """Remove tags from a WIP ASCCP."""
    try:
        await core_component_service.remove_asccp_tags(asccp_manifest_id=asccp_manifest_id, tag_id=tag_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to remove tags from ASCCP {asccp_manifest_id}.") from exc


@mcp.tool(name="add_tags_to_bccp", description="Attach one or more tags to a BCCP (Basic Core Component Property).", output_schema=EMPTY_OUTPUT_SCHEMA)
async def add_tags_to_bccp(
    bccp_manifest_id: Annotated[int, Field(gt=0, description="Target BCCP manifest identifier.")],
    tag_id: Annotated[list[int], Field(description="Tag identifier list to attach.")],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> dict[str, object]:
    """Attach tags to a WIP BCCP."""
    try:
        await core_component_service.add_bccp_tags(bccp_manifest_id=bccp_manifest_id, tag_id=tag_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to add tags to BCCP {bccp_manifest_id}.") from exc


@mcp.tool(name="remove_tags_from_bccp", description="Detach one or more tags from a BCCP (Basic Core Component Property).", output_schema=EMPTY_OUTPUT_SCHEMA)
async def remove_tags_from_bccp(
    bccp_manifest_id: Annotated[int, Field(gt=0, description="Target BCCP manifest identifier.")],
    tag_id: Annotated[list[int], Field(description="Tag identifier list to remove.")],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> dict[str, object]:
    """Remove tags from a WIP BCCP."""
    try:
        await core_component_service.remove_bccp_tags(bccp_manifest_id=bccp_manifest_id, tag_id=tag_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to remove tags from BCCP {bccp_manifest_id}.") from exc


@mcp.tool(
    name="revise_or_amend_asccp",
    description=(
        "Create a new editable ASCCP revision from a stable ASCCP revision. "
        "For end-user ASCCPs, this is called an amendment."
    ),
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def revise_or_amend_asccp(
    asccp_manifest_id: Annotated[int, Field(gt=0, description="Target ASCCP manifest identifier.")],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> dict[str, object]:
    """Revise or amend an ASCCP."""
    try:
        await core_component_service.revise_asccp(asccp_manifest_id=asccp_manifest_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to revise or amend ASCCP {asccp_manifest_id}.") from exc


@mcp.tool(name="cancel_asccp", description="Cancel the current ASCCP revision and restore the previous stable revision.", output_schema=EMPTY_OUTPUT_SCHEMA)
async def cancel_asccp(
    asccp_manifest_id: Annotated[int, Field(gt=0, description="Target ASCCP manifest identifier.")],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> dict[str, object]:
    """Cancel the current ASCCP revision."""
    try:
        await core_component_service.cancel_asccp(asccp_manifest_id=asccp_manifest_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to cancel ASCCP {asccp_manifest_id}.") from exc


@mcp.tool(name="discard_asccp", description="Discard a Deleted ASCCP (Association Core Component Property) permanently.", output_schema=EMPTY_OUTPUT_SCHEMA)
async def discard_asccp(
    asccp_manifest_id: Annotated[int, Field(gt=0, description="Target ASCCP manifest identifier.")],
    ctx: Context,
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> dict[str, object]:
    """Discard a Deleted ASCCP permanently."""
    row = await core_component_service.get_asccp(asccp_manifest_id)
    if row is None:
        raise ToolError(f"The ASCCP with manifest ID {asccp_manifest_id} was not found. Please check the ID and try again.")
    if row.state != "Deleted":
        raise ToolError(f"The ASCCP '{row.den}' is currently in the '{row.state}' state. Only ASCCPs in the 'Deleted' state can be discarded.")
    elicit_result = await ctx.elicit(
        message=(f"Are you sure you want to discard '{row.den}' permanently?\n\nThis removes the ASCCP from the database and cannot be undone."),
        response_type=None,
    )
    match elicit_result:
        case AcceptedElicitation():
            pass
        case DeclinedElicitation():
            raise ToolError("ASCCP discard was not confirmed.")
        case CancelledElicitation():
            raise ToolError("ASCCP discard was cancelled.")
    try:
        await core_component_service.discard_asccp(asccp_manifest_id=asccp_manifest_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to discard ASCCP {asccp_manifest_id}.") from exc


@mcp.tool(
    name="revise_or_amend_bccp",
    description=(
        "Create a new editable BCCP revision from a stable BCCP revision. "
        "For end-user BCCPs, this is called an amendment."
    ),
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def revise_or_amend_bccp(
    bccp_manifest_id: Annotated[int, Field(gt=0, description="Target BCCP manifest identifier.")],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> dict[str, object]:
    """Revise or amend a BCCP."""
    try:
        await core_component_service.revise_bccp(bccp_manifest_id=bccp_manifest_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to revise or amend BCCP {bccp_manifest_id}.") from exc


@mcp.tool(name="cancel_bccp", description="Cancel the current BCCP revision and restore the previous stable revision.", output_schema=EMPTY_OUTPUT_SCHEMA)
async def cancel_bccp(
    bccp_manifest_id: Annotated[int, Field(gt=0, description="Target BCCP manifest identifier.")],
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> dict[str, object]:
    """Cancel the current BCCP revision."""
    try:
        await core_component_service.cancel_bccp(bccp_manifest_id=bccp_manifest_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to cancel BCCP {bccp_manifest_id}.") from exc


@mcp.tool(name="discard_bccp", description="Discard a Deleted BCCP (Basic Core Component Property) permanently.", output_schema=EMPTY_OUTPUT_SCHEMA)
async def discard_bccp(
    bccp_manifest_id: Annotated[int, Field(gt=0, description="Target BCCP manifest identifier.")],
    ctx: Context,
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> dict[str, object]:
    """Discard a Deleted BCCP permanently."""
    row = await core_component_service.get_bccp(bccp_manifest_id)
    if row is None:
        raise ToolError(f"The BCCP with manifest ID {bccp_manifest_id} was not found. Please check the ID and try again.")
    if row.state != "Deleted":
        raise ToolError(f"The BCCP '{row.den}' is currently in the '{row.state}' state. Only BCCPs in the 'Deleted' state can be discarded.")
    logger.warning("discard_bccp bccp_manifest_id=%d proceeding without elicitation", bccp_manifest_id)
    try:
        await core_component_service.discard_bccp(bccp_manifest_id=bccp_manifest_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to discard BCCP {bccp_manifest_id}.") from exc


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
                            "release_num": {"type": "string", "description": "Release number", "example": "10.6"},
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
                    "release_num": {"type": "string", "description": "Release number", "example": "10.6"},
                    "state": {"type": "string", "description": "Release state", "example": "Published"}
                },
                "required": ["release_id", "release_num", "state"]
            },
            "tags": {
                "type": "array",
                "description": "Tags attached to the ACC.",
                "items": {
                    "type": "object",
                    "properties": {
                        "tag_id": {"type": "integer", "description": "Unique identifier for the tag", "example": 1},
                        "name": {"type": "string", "description": "Tag name", "example": "Noun"}
                    },
                    "required": ["tag_id", "name"]
                }
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
                            "release_num": {"type": "string", "description": "Release number", "example": "10.6"},
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
                    "release_num": {"type": "string", "description": "Release number", "example": "10.6"},
                    "state": {"type": "string", "description": "Release state", "example": "Published"}
                },
                "required": ["release_id", "release_num", "state"]
            },
            "tags": {
                "type": "array",
                "description": "Tags attached to the ASCCP.",
                "items": {
                    "type": "object",
                    "properties": {
                        "tag_id": {"type": "integer", "description": "Unique identifier for the tag", "example": 1},
                        "name": {"type": "string", "description": "Tag name", "example": "Verb"}
                    },
                    "required": ["tag_id", "name"]
                }
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
                            "release_num": {"type": "string", "description": "Release number", "example": "10.6"},
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
                    "release_num": {"type": "string", "description": "Release number", "example": "10.6"},
                    "state": {"type": "string", "description": "Release state", "example": "Published"}
                },
                "required": ["release_id", "release_num", "state"]
            },
            "tags": {
                "type": "array",
                "description": "Tags attached to the BCCP.",
                "items": {
                    "type": "object",
                    "properties": {
                        "tag_id": {"type": "integer", "description": "Unique identifier for the tag", "example": 1},
                        "name": {"type": "string", "description": "Tag name", "example": "BOD"}
                    },
                    "required": ["tag_id", "name"]
                }
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
    app_user_repository = plugin.create_app_user_repository(session)
    release_service = ReleaseService(
        plugin.create_release_repository(session),
        app_user_repository,
        requester=requester,
    )
    data_type_service = DataTypeService(
        plugin.create_data_type_repository(session),
        release_service,
        app_user_repository,
        requester=requester,
    )
    return CoreComponentService(
        plugin.create_core_component_repository(session),
        release_service,
        data_type_service,
        app_user_repository,
        requester=requester,
    )


async def _create_acc_response(
    *,
    core_component_service: CoreComponentService,
    release_id: int,
    based_acc_manifest_id: int | None = None,
    object_class_term: str,
    component_type: Literal[
        "Base",
        "Semantics",
        "Extension",
        "SemanticGroup",
        "UserExtensionGroup",
        "Embedded",
        "OAGIS10Nouns",
        "OAGIS10BODs",
        "BOD",
        "Verb",
        "Noun",
        "Choice",
        "AttributeGroup",
    ] = "Semantics",
    definition: str | None = None,
    definition_source: str | None = None,
    is_abstract: bool | None = None,
    namespace_id: int | None = None,
    tag_id: list[int] | None = None,
    fallback: str,
) -> CreateAccResponse:
    """Create an ACC through the shared service and convert to MCP response."""
    try:
        result = await core_component_service.create_acc(
            release_id=release_id,
            based_acc_manifest_id=based_acc_manifest_id,
            object_class_term=object_class_term,
            component_type=component_type,
            definition=definition,
            definition_source=definition_source,
            is_abstract=is_abstract,
            namespace_id=namespace_id,
            tag_id=tag_id,
        )
        return CreateAccResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=fallback) from exc


def _to_list_response(*, items: list[Any], total: int, offset: int, limit: int) -> GetCoreComponentPaginationResponse:
    """Build the paginated MCP response model."""
    return GetCoreComponentPaginationResponse(
        total_items=total,
        offset=offset,
        limit=limit,
        items=[CoreComponentListEntryResponse.model_validate(item, from_attributes=True) for item in items],
    )
