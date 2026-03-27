"""
MCP Tools for managing XBT (XML Built-in Type) operations in connectCenter.

This module provides Model Context Protocol (MCP) tools for querying and retrieving
XBTs (XML Built-in Types), which are fundamental XML Schema Definition (XSD) data types
used in business information exchanges. XBTs represent the primitive data types
defined in XML Schema, such as string, integer, decimal, date, boolean, and their
specialized subtypes.

XBTs serve as the foundation for data type definitions in Core Components, enabling
consistent representation of simple data values across different business contexts.
They form a type hierarchy through subtype relationships, allowing specialization of
base types. For example:
- anyType (root type) → anySimpleType → string → normalizedString → token → language
- anySimpleType → decimal → integer → nonNegativeInteger → positiveInteger
- anySimpleType → dateTime, date, time, duration, boolean, float, double, etc.

Each XBT includes mappings to other data representation formats:
- JBT Draft 05: JSON Schema Draft 05 format (e.g., {"type":"string", "format":"date-time"})
- OpenAPI 3.0: OpenAPI 3.0 specification format (e.g., {"type":"string", "format":"date-time"})
- Avro: Apache Avro schema format (e.g., {"type":"string"} or {"type":"int"})

These mappings support interoperability across different systems and standards, enabling
the same XBT to be represented consistently in XML, JSON, OpenAPI, and Avro formats.

Available Tools:
- get_xbt: Retrieve a single XBT by its manifest ID, including all related
  information such as release, library, log, subtype_of_xbt (parent type in hierarchy),
  data format mappings, and metadata.

Key Features:
- Full relationship loading (release, library, log, subtype_of_xbt, owner, creator, last_updater)
- Support for type hierarchy navigation through subtype relationships
- Data format mappings (JSON Schema Draft 05, OpenAPI 3.0, Avro) for interoperability
- Comprehensive error handling and validation
- Structured response models with detailed metadata

The tools provide a clean, consistent interface for accessing XBT data through the MCP protocol.
All operations require proper authentication and authorization.
"""

from __future__ import annotations

import logging
from typing import Annotated

from fastmcp import FastMCP
from fastmcp.dependencies import Depends
from pydantic import Field
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.vendor_plugins import get_vendor_plugin
from app.security import AuthenticatedUser
from app.services.xbt_service import XbtService
from app.tools import _to_tool_error, get_tool_authenticated_user, tool_session
from app.tools.models.xbt import GetXbtResponse

logger = logging.getLogger("connectcenter.mcp.xbt")

mcp = FastMCP("connectCenter MCP - XBT Tools")


async def get_xbt_service(
    session: AsyncSession = Depends(tool_session),
) -> XbtService:
    """Provide a requester-scoped XBT service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    return _build_xbt_service(session, requester)


@mcp.tool(
    name="get_xbt",
    description="Get a specific XBT (XML Built-in Type) by its manifest ID. XBTs are fundamental XML Schema data types (e.g., string, integer, date, boolean) that form a type hierarchy and include mappings to JSON Schema, OpenAPI, and Avro formats.",
    output_schema={
        "type": "object",
        "description": "Response containing XBT (XML Built-in Type) information, including type hierarchy relationships, data format mappings, and metadata",
        "properties": {
            "xbt_manifest_id": {"type": "integer",
                                "description": "Unique identifier for the XBT manifest (release-specific version)",
                                "example": 12345},
            "xbt_id": {"type": "integer",
                       "description": "Unique identifier for the XBT (base entity ID, same across all releases)",
                       "example": 6789},
            "guid": {"type": "string",
                     "description": "Globally unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)",
                     "example": "a1b2c3d4e5f6789012345678901234ab"},
            "name": {"type": ["string", "null"],
                     "description": "Human-readable name of the built-in type (e.g., 'string', 'date time', 'boolean', 'normalized string')",
                     "example": "string"},
            "builtIn_type": {"type": ["string", "null"],
                             "description": "Built-in type as it should appear in XML schema with namespace prefix (e.g., 'xsd:string', 'xsd:dateTime', 'xsd:normalizedString')",
                             "example": "xsd:string"},
            "jbt_draft05_map": {"type": ["string", "null"],
                                "description": "JSON Schema Draft 05 mapping as a JSON string (e.g., '{\"type\":\"string\"}', '{\"type\":\"string\", \"format\":\"date-time\"}')",
                                "example": "{\"type\":\"string\"}"},
            "openapi30_map": {"type": ["string", "null"],
                              "description": "OpenAPI 3.0 specification mapping as a JSON string (e.g., '{\"type\":\"string\", \"format\":\"date-time\"}')",
                              "example": "{\"type\":\"string\", \"format\":\"date-time\"}"},
            "avro_map": {"type": ["string", "null"],
                         "description": "Apache Avro schema mapping as a JSON string (e.g., '{\"type\":\"string\"}', '{\"type\":\"int\"}')",
                         "example": "{\"type\":\"string\"}"},
            "subtype_of_xbt": {
                "type": ["object", "null"],
                "description": "Information about the parent XBT in the type hierarchy, if this XBT is a subtype of another. For example, 'normalizedString' has 'string' as its subtype_of_xbt, and 'integer' has 'decimal' as its subtype_of_xbt. Root types like 'anyType' have this field as null.",
                "properties": {
                    "xbt_manifest_id": {"type": "integer",
                                        "description": "Unique identifier for the parent XBT manifest (release-specific version)",
                                        "example": 12345},
                    "xbt_id": {"type": "integer",
                               "description": "Unique identifier for the parent XBT (base entity ID, same across all releases)",
                               "example": 6789},
                    "guid": {"type": "string",
                             "description": "Globally unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)",
                             "example": "a1b2c3d4e5f6789012345678901234ab"},
                    "name": {"type": ["string", "null"],
                             "description": "Human-readable name of the parent built-in type (e.g., 'string', 'decimal', 'anySimpleType')",
                             "example": "string"},
                    "builtIn_type": {"type": ["string", "null"],
                                     "description": "Parent built-in type as it should appear in XML schema with namespace prefix (e.g., 'xsd:string', 'xsd:decimal')",
                                     "example": "xsd:string"},
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
                    "release": {
                        "type": "object",
                        "description": "Release information",
                        "properties": {
                            "release_id": {"type": "integer", "description": "Unique identifier for the release",
                                           "example": 1},
                            "release_num": {"type": "string", "description": "Release number",
                                            "example": "10.6"},
                            "state": {"type": "string", "description": "Release state", "example": "Published"}
                        },
                        "required": ["release_id", "release_num", "state"]
                    }
                },
                "required": ["xbt_manifest_id", "xbt_id", "guid", "library", "release"]
            },
            "schema_definition": {"type": ["string", "null"],
                                  "description": "XML Schema Definition (XSD) schema definition string, if custom schema is defined. Typically None for standard built-in types.",
                                  "example": "<xs:simpleType name=\"string\">...</xs:simpleType>"},
            "revision_doc": {"type": ["string", "null"],
                             "description": "Revision documentation describing changes or updates to this XBT. Typically None for standard built-in types.",
                             "example": "Updated to support new schema version"},
            "state": {"type": ["integer", "null"],
                      "description": "State of the XBT indicating its lifecycle state (e.g., 3 = Published). Common states: 1=WIP, 2=QA, 3=Published.",
                      "example": 3},
            "is_deprecated": {"type": "boolean",
                              "description": "Whether the XBT is deprecated and should not be used in new implementations. Deprecated XBTs are retained for backward compatibility.",
                              "example": False},
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
            "log": {
                "type": ["object", "null"],
                "description": "Log information",
                "properties": {
                    "log_id": {"type": "integer", "description": "Unique identifier for the log", "example": 123},
                    "revision_num": {"type": "integer", "description": "Revision number", "example": 1},
                    "revision_tracking_num": {"type": "integer", "description": "Revision tracking number",
                                              "example": 1}
                },
                "required": ["log_id", "revision_num", "revision_tracking_num"]
            },
            "owner": {
                "type": "object",
                "description": "Owner information",
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
                "description": "Information about the creation of the XBT",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who created the XBT",
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
                "description": "Information about the last update of the XBT",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who last updated the XBT",
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
        "required": ["xbt_manifest_id", "xbt_id", "guid", "is_deprecated", "library", "release", "owner", "created",
                     "last_updated"]
    }
)
async def get_xbt(
    xbt_manifest_id: Annotated[int, Field(gt=0, description="Unique numeric identifier of the XBT manifest to retrieve.")],
    xbt_service: XbtService = Depends(get_xbt_service),
) -> GetXbtResponse:
    """
    Get a specific XBT (XML Built-in Type) by its manifest ID.

    XBTs are fundamental XML Schema Definition (XSD) data types that serve as the foundation
    for data type definitions in Core Components. They represent primitive data types such
    as string, integer, decimal, date, boolean, and their specialized subtypes.

    XBTs form a type hierarchy through subtype relationships. For example:
    - anyType (root) → anySimpleType → string → normalizedString → token → language
    - anySimpleType → decimal → integer → nonNegativeInteger → positiveInteger
    - anySimpleType → dateTime, date, time, duration, boolean, float, double, etc.

    Each XBT includes mappings to other data representation formats:
    - JBT Draft 05: JSON Schema Draft 05 format (JSON string)
    - OpenAPI 3.0: OpenAPI 3.0 specification format (JSON string)
    - Avro: Apache Avro schema format (JSON string)

    This function retrieves a single XBT from the database and returns detailed information
    including its type hierarchy relationships (subtype_of_xbt), data format mappings,
    metadata about who created it and when it was last updated, along with release and
    library information.

    Args:
        xbt_manifest_id (int): The unique identifier of the XBT manifest to fetch.
            The manifest ID is release-specific, while the xbt_id is the same across all releases.

    Returns:
        GetXbtResponse: Response object containing:
            - xbt_manifest_id: Unique identifier for the XBT manifest (release-specific)
            - xbt_id: Unique identifier for the XBT (base entity ID, same across releases)
            - guid: Globally unique identifier within the release (32-char hex, lowercase, no hyphens)
            - name: Human-readable name (e.g., "string", "date time", "boolean", "normalized string")
            - builtIn_type: XML schema type with namespace prefix (e.g., "xsd:string", "xsd:dateTime")
            - jbt_draft05_map: JSON Schema Draft 05 mapping as JSON string
            - openapi30_map: OpenAPI 3.0 specification mapping as JSON string
            - avro_map: Apache Avro schema mapping as JSON string
            - subtype_of_xbt: Parent XBT in type hierarchy (None for root types like anyType)
            - schema_definition: Custom XSD schema definition (typically None for built-in types)
            - revision_doc: Revision documentation (typically None for standard built-in types)
            - state: Lifecycle state (e.g., 3 = Published)
            - is_deprecated: Whether the XBT is deprecated
            - library: Library information (library_id, name)
            - release: Release information (release_id, release_num, state)
            - log: Log information tracking revision history (if available)
            - owner: User information about the owner
            - created: Information about creation (who, when)
            - last_updated: Information about last update (who, when)

    Raises:
        ToolError: If validation fails, the XBT manifest is not found, or other errors occur.
            Common error scenarios include:
            - XBT manifest with the specified ID does not exist
            - Database connection issues
            - Authentication failures

    Examples:
        Get a specific XBT and examine its type hierarchy:
        >>> result = get_xbt(xbt_manifest_id=123)
        >>> print(f"XBT: {result.name} (GUID: {result.guid})")
        >>> print(f"Built-in type: {result.builtIn_type}")
        >>> if result.subtype_of_xbt:
        ...     print(f"Subtype of: {result.subtype_of_xbt.name} ({result.subtype_of_xbt.builtIn_type})")
        >>> print(f"OpenAPI mapping: {result.openapi30_map}")
    """
    try:
        row = await xbt_service.get(xbt_manifest_id)
        if row is None:
            raise ValueError(f"The XBT with manifest ID {xbt_manifest_id} was not found. Please check the ID and try again.")
        return GetXbtResponse.model_validate(row, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to retrieve XBT {xbt_manifest_id}.") from exc


def _build_xbt_service(
    session: AsyncSession,
    requester: AuthenticatedUser,
) -> XbtService:
    """Construct the XBT service for an MCP request."""
    plugin = get_vendor_plugin()
    return XbtService(
        plugin.create_xbt_repository(session),
        plugin.create_app_user_repository(session),
        requester=requester,
    )
