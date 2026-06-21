"""
MCP Tools for managing Data Type operations in connectCenter.

This module provides Model Context Protocol (MCP) tools for querying and retrieving
Data Types, which define the structure and constraints for data elements in business
information exchanges. Data Types serve as the foundation for Basic Core Component
Properties (BCCPs), specifying the format, validation rules, and semantic meaning of
simple data values. They follow the UN/CEFACT Core Component Technical Specification
(CCTS) methodology, providing standardized representations for common data patterns such
as dates, amounts, identifiers, and text fields.

Data Types enable consistent data representation across different business contexts by
defining reusable type definitions that can be applied to multiple BCCPs. They include
supplementary components that add additional facets or constraints (such as minimum/maximum
values, patterns, or enumerations) to refine the data type's behavior. Data Types can also
be based on other Data Types, creating inheritance hierarchies that support specialization
and reuse. The tools provide a standardized MCP interface, enabling clients to interact
with Data Type data programmatically, including dependency-aware release queries.

Available Tools:
- get_data_types: Retrieve paginated lists of Data Types filtered by release. Automatically
  includes Data Types from all dependent releases (recursively). Supports optional filters
  for DEN (Dictionary Entry Name) and representation_term, with date range filtering and
  custom sorting.

- get_data_type: Retrieve a single Data Type by its manifest ID, including all related
  information such as namespace, release, library, log, based_dt_manifest, and associated
  supplementary component manifests.

Key Features:
- Full relationship loading (namespace, creator, owner, release, library, log, based_dt_manifest)
- Dependency-aware queries that include data types from dependent releases
- Support for filtering, pagination, and sorting
- Automatic retrieval of associated supplementary component manifests
- Comprehensive error handling and validation
- Structured response models with detailed metadata

The tools provide a clean, consistent interface for accessing Data Type data through the MCP protocol.
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
from pydantic import Field
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.vendor_plugins import get_vendor_plugin
from app.utils.date import parse_date_range
from app.security import AuthenticatedUser
from app.services.app_user_service import AppUserService
from app.services.data_type_service import DataTypeService
from app.services.models.data_type import DataTypePrimitiveServiceRecord
from app.services.release_service import ReleaseService
from app.tools import _to_tool_error, get_tool_authenticated_user, tool_session
from app.tools.models.data_type import (
    CreateDataTypeResponse,
    CreateDataTypeSupplementaryComponentResponse,
    DataTypeResponseEntry,
    DefaultPrimitiveSelectionInput,
    GetDataTypePaginationResponse,
    GetDataTypeResponse,
    PrimitiveMutationInput,
    TransferDataTypeOwnershipResponse,
    UpdateDataTypeResponse,
    UpdateDataTypeSupplementaryComponentResponse,
    ValueConstraintInput,
)
from app.types.unset import UNSET

logger = logging.getLogger("connectcenter.mcp.data_type")

mcp = FastMCP("connectCenter MCP - Data Type Tools")

EMPTY_OUTPUT_SCHEMA = {
    "type": "object",
    "description": "Empty response body.",
    "properties": {},
    "additionalProperties": False,
}


async def get_data_type_service(
    session: AsyncSession = Depends(tool_session),
) -> DataTypeService:
    """Provide a requester-scoped data-type service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    return _build_data_type_service(session, requester)


async def get_app_user_service(
    session: AsyncSession = Depends(tool_session),
) -> AppUserService:
    """Provide a requester-scoped app-user service for MCP tools."""
    requester = await get_tool_authenticated_user(session)
    vendor_plugin = get_vendor_plugin()
    app_user_repository = vendor_plugin.create_app_user_repository(session)
    return AppUserService(app_user_repository=app_user_repository, requester=requester)


@mcp.tool(
    name="get_data_types",
    description="Get a paginated list of data types associated with a specific release.",
    output_schema={
        "type": "object",
        "description": "Response containing paginated list of data types",
        "properties": {
            "total_items": {
                "type": "integer",
                "description": "Total number of data types available. Allowed values: non-negative integers (≥0).",
                "example": 50,
            },
            "offset": {
                "type": "integer",
                "description": "Offset of the first item in this page. Allowed values: non-negative integers (≥0). Default value: 0.",
                "example": 0,
            },
            "limit": {
                "type": "integer",
                "description": "Number of items returned in this page. Allowed values: integers between 1 and 100 (inclusive). Default value: 10.",
                "example": 10,
            },
            "items": {
                "type": "array",
                "description": "List of data types on this page",
                "items": {
                    "type": "object",
                    "properties": {
                        "dt_manifest_id": {
                            "type": "integer",
                            "description": "Unique identifier for the data type (DT) manifest",
                            "example": 12345,
                        },
                        "dt_id": {
                            "type": "integer",
                            "description": "Unique identifier for the data type (DT)",
                            "example": 6789,
                        },
                        "base_dt": {
                            "type": ["object", "null"],
                            "description": "Base data type information if this data type is based on another",
                            "properties": {
                                "dt_manifest_id": {
                                    "type": "integer",
                                    "description": "Unique identifier for the base data type manifest",
                                    "example": 12345,
                                },
                                "dt_id": {
                                    "type": "integer",
                                    "description": "Unique identifier for the base data type",
                                    "example": 6789,
                                },
                                "based_dt_manifest_id": {
                                    "type": ["integer", "null"],
                                    "description": "Unique identifier for the base data type manifest of the base data type",
                                    "example": 6789,
                                },
                                "guid": {
                                    "type": "string",
                                    "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)",
                                    "example": "a1b2c3d4e5f6789012345678901234ab",
                                },
                                "den": {
                                    "type": "string",
                                    "description": "Dictionary Entry Name (DEN) - the standardized name of the data type (DT) as defined by CCTS v3",
                                    "example": "Amount. Type",
                                },
                                "data_type_term": {
                                    "type": ["string", "null"],
                                    "description": "Data type (DT) term as specified in CCTS v3",
                                    "example": "Amount",
                                },
                                "qualifier": {
                                    "type": ["string", "null"],
                                    "description": "Qualifier for the data type (DT)",
                                    "example": "Price",
                                },
                                "representation_term": {
                                    "type": ["string", "null"],
                                    "description": "Representation term for the data type (DT)",
                                    "example": "Amount",
                                },
                                "six_digit_id": {
                                    "type": ["string", "null"],
                                    "description": "Six-character suffix for the BDT Type name from the UN/CEFACT XML Schema NDR. It must be unique within the namespace and use only letters and digits.",
                                    "example": "123456",
                                },
                                "definition": {
                                    "type": ["string", "null"],
                                    "description": "Definition of the data type (DT)",
                                    "example": "A number of monetary units specified in a currency where the unit of currency is explicit or implied",
                                },
                                "definition_source": {
                                    "type": ["string", "null"],
                                    "description": "URL indicating the source of the definition",
                                    "example": "https://unece.org/trade/uncefact/core-components-data-type-catalogue",
                                },
                                "content_component_definition": {
                                    "type": ["string", "null"],
                                    "description": "Content component definition",
                                    "example": "A numeric value determined by measuring an object along with the specified unit of measure",
                                },
                                "is_deprecated": {
                                    "type": "boolean",
                                    "description": "Whether the data type (DT) is deprecated",
                                    "example": False,
                                },
                                "namespace": {
                                    "type": ["object", "null"],
                                    "description": "Namespace information",
                                    "properties": {
                                        "namespace_id": {
                                            "type": "integer",
                                            "description": "Unique identifier for the namespace",
                                            "example": 1,
                                        },
                                        "uri": {
                                            "type": "string",
                                            "description": "Namespace URI (Uniform Resource Identifier)",
                                            "example": "http://www.openapplications.org/oagis/10",
                                        },
                                        "prefix": {
                                            "type": ["string", "null"],
                                            "description": "Namespace prefix",
                                            "example": "oagis",
                                        },
                                    },
                                    "required": ["namespace_id", "uri"],
                                },
                                "library": {
                                    "type": "object",
                                    "description": "Library information",
                                    "properties": {
                                        "library_id": {
                                            "type": "integer",
                                            "description": "Unique identifier for the library",
                                            "example": 1,
                                        },
                                        "name": {
                                            "type": "string",
                                            "description": "Library name",
                                            "example": "connectSpec",
                                        },
                                    },
                                    "required": ["library_id", "name"],
                                },
                                "release": {
                                    "type": "object",
                                    "description": "Release information",
                                    "properties": {
                                        "release_id": {
                                            "type": "integer",
                                            "description": "Unique identifier for the release",
                                            "example": 1,
                                        },
                                        "release_num": {
                                            "type": "string",
                                            "description": "Release number",
                                            "example": "10.6",
                                        },
                                        "state": {
                                            "type": "string",
                                            "description": "Release state",
                                            "example": "Published",
                                        },
                                    },
                                    "required": ["release_id", "release_num", "state"],
                                },
                            },
                            "required": ["dt_manifest_id", "dt_id", "guid", "den", "library", "release"],
                        },
                        "guid": {
                            "type": "string",
                            "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)",
                            "example": "a1b2c3d4e5f6789012345678901234ab",
                        },
                        "den": {
                            "type": "string",
                            "description": "Dictionary Entry Name (DEN) - the standardized name of the data type (DT) as defined by CCTS v3, uniquely identifying the data type within its namespace",
                            "example": "Price_ Amount. Type",
                        },
                        "data_type_term": {
                            "type": ["string", "null"],
                            "description": "Data type (DT) term as specified in CCTS v3",
                            "example": "Amount",
                        },
                        "qualifier": {
                            "type": ["string", "null"],
                            "description": "Qualifier for the data type (DT)",
                            "example": "Price",
                        },
                        "representation_term": {
                            "type": ["string", "null"],
                            "description": "Representation term for the data type (DT)",
                            "example": "Amount",
                        },
                        "six_digit_id": {
                            "type": ["string", "null"],
                            "description": "Six-character suffix for the BDT Type name from the UN/CEFACT XML Schema NDR. It must be unique within the namespace and use only letters and digits.",
                            "example": "123456",
                        },
                        "definition": {
                            "type": ["string", "null"],
                            "description": "Definition of the data type (DT)",
                            "example": "A number of monetary units specified in a currency where the unit of currency is explicit or implied",
                        },
                        "definition_source": {
                            "type": ["string", "null"],
                            "description": "URL indicating the source of the definition",
                            "example": "https://unece.org/trade/uncefact/core-components-data-type-catalogue",
                        },
                        "content_component_definition": {
                            "type": ["string", "null"],
                            "description": "Content component definition",
                            "example": "A numeric value determined by measuring an object along with the specified unit of measure",
                        },
                        "commonly_used": {
                            "type": "boolean",
                            "description": "Whether the data type (DT) is commonly used",
                            "example": False,
                        },
                        "is_deprecated": {
                            "type": "boolean",
                            "description": "Whether the data type (DT) is deprecated",
                            "example": False,
                        },
                        "state": {
                            "type": ["string", "null"],
                            "description": "State of the data type (DT)",
                            "example": "Published",
                        },
                        "supplementary_components": {
                            "type": "array",
                            "description": "List of supplementary components for the data type (DT)",
                            "items": {
                                "type": "object",
                                "properties": {
                                    "dt_sc_manifest_id": {
                                        "type": "integer",
                                        "description": "Unique identifier for the data type supplementary component manifest",
                                        "example": 12345,
                                    },
                                    "dt_sc_id": {
                                        "type": "integer",
                                        "description": "Unique identifier for the data type supplementary component",
                                        "example": 6789,
                                    },
                                    "guid": {
                                        "type": "string",
                                        "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)",
                                        "example": "a1b2c3d4e5f6789012345678901234ab",
                                    },
                                    "object_class_term": {
                                        "type": ["string", "null"],
                                        "description": "Object class term for the supplementary component",
                                        "example": "Amount",
                                    },
                                    "property_term": {
                                        "type": ["string", "null"],
                                        "description": "Property term for the supplementary component",
                                        "example": "Format",
                                    },
                                    "representation_term": {
                                        "type": ["string", "null"],
                                        "description": "Representation term for the supplementary component",
                                        "example": "Text",
                                    },
                                    "definition": {
                                        "type": ["string", "null"],
                                        "description": "Definition of the supplementary component",
                                        "example": "Whether the number is an integer, decimal, real number or percentage",
                                    },
                                    "definition_source": {
                                        "type": ["string", "null"],
                                        "description": "URL indicating the source of the definition",
                                        "example": "https://unece.org/trade/uncefact/core-components-data-type-catalogue",
                                    },
                                    "cardinality": {
                                        "type": "string",
                                        "description": "SCORE-style supplementary-component cardinality label",
                                        "enum": ["Prohibited", "Optional", "Required"],
                                        "example": "Optional",
                                    },
                                    "value_constraint": {
                                        "type": ["object", "null"],
                                        "description": "Value constraint (default_value or fixed_value) for the supplementary component. Exactly one of default_value or fixed_value must be set.",
                                        "properties": {
                                            "default_value": {
                                                "type": ["string", "null"],
                                                "description": "Default value for the supplementary component",
                                                "example": "decimal",
                                            },
                                            "fixed_value": {
                                                "type": ["string", "null"],
                                                "description": "Fixed value for the supplementary component",
                                                "example": "integer",
                                            },
                                        },
                                    },
                                    "is_deprecated": {
                                        "type": "boolean",
                                        "description": "Whether the supplementary component is deprecated",
                                        "example": False,
                                    },
                                },
                                "required": ["dt_sc_manifest_id", "dt_sc_id", "guid", "cardinality", "is_deprecated"],
                            },
                        },
                        "namespace": {
                            "type": ["object", "null"],
                            "description": "Namespace information",
                            "properties": {
                                "namespace_id": {
                                    "type": "integer",
                                    "description": "Unique identifier for the namespace",
                                    "example": 1,
                                },
                                "uri": {
                                    "type": "string",
                                    "description": "Namespace URI (Uniform Resource Identifier)",
                                    "example": "http://www.openapplications.org/oagis/10",
                                },
                                "prefix": {
                                    "type": ["string", "null"],
                                    "description": "Namespace prefix",
                                    "example": "oagis",
                                },
                            },
                            "required": ["namespace_id", "uri"],
                        },
                        "library": {
                            "type": "object",
                            "description": "Library information",
                            "properties": {
                                "library_id": {
                                    "type": "integer",
                                    "description": "Unique identifier for the library",
                                    "example": 1,
                                },
                                "name": {"type": "string", "description": "Library name", "example": "connectSpec"},
                            },
                            "required": ["library_id", "name"],
                        },
                        "release": {
                            "type": "object",
                            "description": "Release information",
                            "properties": {
                                "release_id": {
                                    "type": "integer",
                                    "description": "Unique identifier for the release",
                                    "example": 1,
                                },
                                "release_num": {"type": "string", "description": "Release number", "example": "10.6"},
                                "state": {"type": "string", "description": "Release state", "example": "Published"},
                            },
                            "required": ["release_id", "release_num", "state"],
                        },
                        "log": {
                            "type": ["object", "null"],
                            "description": "Log information",
                            "properties": {
                                "log_id": {
                                    "type": "integer",
                                    "description": "Unique identifier for the log",
                                    "example": 123,
                                },
                                "revision_num": {"type": "integer", "description": "Revision number", "example": 1},
                                "revision_tracking_num": {
                                    "type": "integer",
                                    "description": "Revision tracking number",
                                    "example": 1,
                                },
                            },
                            "required": ["log_id", "revision_num", "revision_tracking_num"],
                        },
                        "owner": {
                            "type": "object",
                            "description": "User information about the owner of the data type (DT)",
                            "properties": {
                                "user_id": {
                                    "type": "integer",
                                    "description": "Unique identifier for the user",
                                    "example": 1,
                                },
                                "login_id": {
                                    "type": "string",
                                    "description": "User's login identifier",
                                    "example": "admin",
                                },
                                "username": {
                                    "type": "string",
                                    "description": "Display name of the user",
                                    "example": "Administrator",
                                },
                                "roles": {
                                    "type": "array",
                                    "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                    "description": "List of roles assigned to the user",
                                    "example": ["Admin"],
                                },
                            },
                            "required": ["user_id", "login_id", "username", "roles"],
                        },
                        "created": {
                            "type": "object",
                            "description": "Information about the creation of the data type (DT)",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who created the data type (DT)",
                                    "properties": {
                                        "user_id": {
                                            "type": "integer",
                                            "description": "Unique identifier for the user",
                                            "example": 1,
                                        },
                                        "login_id": {
                                            "type": "string",
                                            "description": "User's login identifier",
                                            "example": "admin",
                                        },
                                        "username": {
                                            "type": "string",
                                            "description": "Display name of the user",
                                            "example": "Administrator",
                                        },
                                        "roles": {
                                            "type": "array",
                                            "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                            "description": "List of roles assigned to the user",
                                            "example": ["Admin"],
                                        },
                                    },
                                    "required": ["user_id", "login_id", "username", "roles"],
                                },
                                "when": {
                                    "type": "string",
                                    "format": "date-time",
                                    "description": "Creation timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)",
                                    "example": "2024-01-15T10:30:00Z",
                                },
                            },
                            "required": ["who", "when"],
                        },
                        "last_updated": {
                            "type": "object",
                            "description": "Information about the last update of the data type (DT)",
                            "properties": {
                                "who": {
                                    "type": "object",
                                    "description": "User who last updated the data type (DT)",
                                    "properties": {
                                        "user_id": {
                                            "type": "integer",
                                            "description": "Unique identifier for the user",
                                            "example": 1,
                                        },
                                        "login_id": {
                                            "type": "string",
                                            "description": "User's login identifier",
                                            "example": "admin",
                                        },
                                        "username": {
                                            "type": "string",
                                            "description": "Display name of the user",
                                            "example": "Administrator",
                                        },
                                        "roles": {
                                            "type": "array",
                                            "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                            "description": "List of roles assigned to the user",
                                            "example": ["Admin"],
                                        },
                                    },
                                    "required": ["user_id", "login_id", "username", "roles"],
                                },
                                "when": {
                                    "type": "string",
                                    "format": "date-time",
                                    "description": "Last update timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)",
                                    "example": "2024-01-20T14:45:00Z",
                                },
                            },
                            "required": ["who", "when"],
                        },
                    },
                    "required": [
                        "dt_manifest_id",
                        "dt_id",
                        "guid",
                        "den",
                        "library",
                        "release",
                        "commonly_used",
                        "is_deprecated",
                        "supplementary_components",
                        "owner",
                        "created",
                        "last_updated",
                    ],
                },
            },
        },
        "required": ["total_items", "offset", "limit", "items"],
    },
)
async def get_data_types(
    release_id: Annotated[int, Field(gt=0, description="Filter by release ID using exact match.")],
    den: Annotated[
        str | None, Field(description="Filter by data type DEN using partial match (case-insensitive).")
    ] = None,
    representation_term: Annotated[
        str | None, Field(description="Filter by representation term using partial match (case-insensitive).")
    ] = None,
    owner: Annotated[
        str | None,
        Field(
            description="Comma-separated owner login IDs to filter by exact match. Prefix a login ID with '!' to exclude it."
        ),
    ] = None,
    updater: Annotated[
        str | None,
        Field(
            description="Comma-separated updater login IDs to filter by exact match. Prefix a login ID with '!' to exclude it."
        ),
    ] = None,
    created_on: Annotated[
        str | None, Field(description="Filter by creation date using an inclusive range: '[before~after]'.")
    ] = None,
    last_updated_on: Annotated[
        str | None, Field(description="Filter by last update date using an inclusive range: '[before~after]'.")
    ] = None,
    order_by: Annotated[
        str | None,
        Field(
            description="Comma-separated list of properties to order results by. Allowed columns: den, data_type_term, qualifier, representation_term, six_digit_id, definition, creation_timestamp, last_update_timestamp."
        ),
    ] = None,
    offset: Annotated[int, Field(ge=0, description="The offset from the beginning of the list.")] = 0,
    limit: Annotated[int, Field(ge=1, le=100, description="The maximum number of items to return.")] = 10,
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> GetDataTypePaginationResponse:
    """
    Get a paginated list of data types associated with a specific release.

    This function retrieves data types that are associated with the given release_id. This tool enables
    users to query and manage data types within specific releases, including filtering by DEN (Dictionary
    Entry Name), representation term, and other attributes. It supports pagination, filtering, and sorting.
    The release_id filter is required to ensure you get data types from the correct release context.

    The DEN (Dictionary Entry Name) is computed as: ((qualifier) ? qualifier + "_ " : "") + data_type_term + ". Type"

    Args:
        release_id (int): Filter by release ID using exact match (required).
        den (str | None, optional): Filter by Dictionary Entry Name (DEN) using partial match (case-insensitive).
            DEN format: '((qualifier) ? qualifier + "_ " : "") + data_type_term + ". Type"'. Defaults to None.
        representation_term (str | None, optional): Filter by representation term using partial match (case-insensitive). Defaults to None.
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
            Allowed columns: den, data_type_term, qualifier, representation_term, six_digit_id, definition, creation_timestamp, last_update_timestamp.
            Example: '-creation_timestamp,+den' translates to 'creation_timestamp DESC, den ASC'.
            Defaults to None.
        offset (int, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int, optional): The maximum number of items to return. Must be between 1 and 100 (inclusive). Defaults to 10.

    Returns:
        GetDataTypePaginationResponse: Response object containing:
            - total_items: Total number of data types available
            - offset: Offset of the first item in this page
            - limit: Number of items returned in this page
            - items: List of data types on this page with detailed information including:
                - dt_manifest_id: Unique identifier for the data type manifest
                - dt_id: Unique identifier for the data type
                - guid: Unique identifier within the release
                - den: Dictionary Entry Name (computed from qualifier and data_type_term)
                - data_type_term: Data type term
                - qualifier: Qualifier (if any)
                - representation_term: Representation term
                - six_digit_id: Six digit ID
                - definition: Description of the data type
                - definition_source: URL indicating the source of the definition
                - content_component_definition: Description of the content component
                - namespace: Namespace information (if any)
                - library: Library information with library_id and name
                - release: Release information with release_id, release_num, and state
                - log: Log information with log_id, revision_num, and revision_tracking_num (if any)
                - commonly_used: Whether the data type is commonly used
                - is_deprecated: Whether the data type is deprecated
                - state: State of the data type
                - supplementary_components: List of supplementary components with their details
                - owner: User information about the owner of the data type
                - created: Information about the creation of the data type
                - last_updated: Information about the last update of the data type

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
        >>> result = get_data_types(release_id=123, offset=0, limit=10)
        >>> print(f"Found {result.total_items} data types")

        Filtered search:
        >>> result = get_data_types(release_id=123, den="Amount. Type", limit=5)
        >>> for data_type in result.items:
        ...     print(f"Data type: {data_type.den} (GUID: {data_type.guid})")

        Date range filtering:
        >>> result = get_data_types(release_id=123, created_on="[2024-01-01~2024-12-31]")
        >>> print(f"Data types created in 2024: {result.total_items}")
    """
    try:
        page = await data_type_service.list(
            release_id=release_id,
            limit=limit,
            offset=offset,
            order_by=order_by,
            den=den,
            representation_term=representation_term,
            owner=owner,
            updater=updater,
            created_on=parse_date_range(created_on),
            last_updated_on=parse_date_range(last_updated_on),
        )
        return _to_list_response(items=page.items, total=page.total, offset=page.offset, limit=page.limit)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to retrieve data types.") from exc


@mcp.tool(
    name="get_data_type",
    description="Get a specific data type by its manifest ID.",
    output_schema={
        "type": "object",
        "description": "Response containing data type information",
        "properties": {
            "dt_manifest_id": {
                "type": "integer",
                "description": "Unique identifier for the data type (DT) manifest",
                "example": 12345,
            },
            "dt_id": {"type": "integer", "description": "Unique identifier for the data type (DT)", "example": 6789},
            "base_dt": {
                "type": ["object", "null"],
                "description": "Base data type information if this data type is based on another",
                "properties": {
                    "dt_manifest_id": {
                        "type": "integer",
                        "description": "Unique identifier for the base data type manifest",
                        "example": 12345,
                    },
                    "dt_id": {
                        "type": "integer",
                        "description": "Unique identifier for the base data type",
                        "example": 6789,
                    },
                    "based_dt_manifest_id": {
                        "type": ["integer", "null"],
                        "description": "Unique identifier for the base data type manifest of the base data type",
                        "example": 6789,
                    },
                    "guid": {
                        "type": "string",
                        "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)",
                        "example": "a1b2c3d4e5f6789012345678901234ab",
                    },
                    "den": {
                        "type": "string",
                        "description": "Dictionary Entry Name (DEN) - the standardized name of the data type (DT) as defined by CCTS v3",
                        "example": "Amount. Type",
                    },
                    "data_type_term": {
                        "type": ["string", "null"],
                        "description": "Data type (DT) term as specified in CCTS v3",
                        "example": "Amount",
                    },
                    "qualifier": {
                        "type": ["string", "null"],
                        "description": "Qualifier for the data type (DT)",
                        "example": "Price",
                    },
                    "representation_term": {
                        "type": ["string", "null"],
                        "description": "Representation term for the data type (DT)",
                        "example": "Amount",
                    },
                    "six_digit_id": {
                        "type": ["string", "null"],
                        "description": "Six-digit identifier for the data type (DT)",
                        "example": "123456",
                    },
                    "definition": {
                        "type": ["string", "null"],
                        "description": "Definition of the data type (DT)",
                        "example": "A number of monetary units specified in a currency where the unit of currency is explicit or implied",
                    },
                    "definition_source": {
                        "type": ["string", "null"],
                        "description": "URL indicating the source of the definition",
                        "example": "https://unece.org/trade/uncefact/core-components-data-type-catalogue",
                    },
                    "content_component_definition": {
                        "type": ["string", "null"],
                        "description": "Content component definition",
                        "example": "A numeric value determined by measuring an object along with the specified unit of measure",
                    },
                    "is_deprecated": {
                        "type": "boolean",
                        "description": "Whether the data type (DT) is deprecated",
                        "example": False,
                    },
                    "namespace": {
                        "type": ["object", "null"],
                        "description": "Namespace information",
                        "properties": {
                            "namespace_id": {
                                "type": "integer",
                                "description": "Unique identifier for the namespace",
                                "example": 1,
                            },
                            "uri": {
                                "type": "string",
                                "description": "Namespace URI (Uniform Resource Identifier)",
                                "example": "http://www.openapplications.org/oagis/10",
                            },
                            "prefix": {
                                "type": ["string", "null"],
                                "description": "Namespace prefix",
                                "example": "oagis",
                            },
                        },
                        "required": ["namespace_id", "uri"],
                    },
                    "library": {
                        "type": "object",
                        "description": "Library information",
                        "properties": {
                            "library_id": {
                                "type": "integer",
                                "description": "Unique identifier for the library",
                                "example": 1,
                            },
                            "name": {"type": "string", "description": "Library name", "example": "connectSpec"},
                        },
                        "required": ["library_id", "name"],
                    },
                    "release": {
                        "type": "object",
                        "description": "Release information",
                        "properties": {
                            "release_id": {
                                "type": "integer",
                                "description": "Unique identifier for the release",
                                "example": 1,
                            },
                            "release_num": {"type": "string", "description": "Release number", "example": "10.6"},
                            "state": {"type": "string", "description": "Release state", "example": "Published"},
                        },
                        "required": ["release_id", "release_num", "state"],
                    },
                },
                "required": ["dt_manifest_id", "dt_id", "guid", "den", "library", "release"],
            },
            "guid": {
                "type": "string",
                "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)",
                "example": "a1b2c3d4e5f6789012345678901234ab",
            },
            "den": {
                "type": "string",
                "description": "Dictionary Entry Name (DEN) - the standardized name of the data type (DT) as defined by CCTS v3, uniquely identifying the data type within its namespace",
                "example": "Price_ Amount. Type",
            },
            "data_type_term": {
                "type": ["string", "null"],
                "description": "Data type (DT) term as specified in CCTS v3",
                "example": "Amount",
            },
            "qualifier": {
                "type": ["string", "null"],
                "description": "Qualifier for the data type (DT)",
                "example": "Price",
            },
            "representation_term": {
                "type": ["string", "null"],
                "description": "Representation term for the data type (DT)",
                "example": "Amount",
            },
            "six_digit_id": {
                "type": ["string", "null"],
                "description": "Six-character suffix for the BDT Type name from the UN/CEFACT XML Schema NDR. It must be unique within the namespace and use only letters and digits.",
                "example": "123456",
            },
            "definition": {
                "type": ["string", "null"],
                "description": "Definition of the data type (DT)",
                "example": "A number of monetary units specified in a currency where the unit of currency is explicit or implied",
            },
            "definition_source": {
                "type": ["string", "null"],
                "description": "URL indicating the source of the definition",
                "example": "https://unece.org/trade/uncefact/core-components-data-type-catalogue",
            },
            "content_component_definition": {
                "type": ["string", "null"],
                "description": "Content component definition",
                "example": "A numeric value determined by measuring an object along with the specified unit of measure",
            },
            "commonly_used": {
                "type": "boolean",
                "description": "Whether the data type (DT) is commonly used",
                "example": False,
            },
            "is_deprecated": {
                "type": "boolean",
                "description": "Whether the data type (DT) is deprecated",
                "example": False,
            },
            "state": {"type": ["string", "null"], "description": "State of the data type (DT)", "example": "Published"},
            "supplementary_components": {
                "type": "array",
                "description": "List of supplementary components for the data type (DT)",
                "items": {
                    "type": "object",
                    "properties": {
                        "dt_sc_manifest_id": {
                            "type": "integer",
                            "description": "Unique identifier for the data type supplementary component manifest",
                            "example": 12345,
                        },
                        "dt_sc_id": {
                            "type": "integer",
                            "description": "Unique identifier for the data type supplementary component",
                            "example": 6789,
                        },
                        "guid": {
                            "type": "string",
                            "description": "Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)",
                            "example": "a1b2c3d4e5f6789012345678901234ab",
                        },
                        "object_class_term": {
                            "type": ["string", "null"],
                            "description": "Object class term for the supplementary component",
                            "example": "Amount",
                        },
                        "property_term": {
                            "type": ["string", "null"],
                            "description": "Property term for the supplementary component",
                            "example": "Format",
                        },
                        "representation_term": {
                            "type": ["string", "null"],
                            "description": "Representation term for the supplementary component",
                            "example": "Text",
                        },
                        "definition": {
                            "type": ["string", "null"],
                            "description": "Definition of the supplementary component",
                            "example": "Whether the number is an integer, decimal, real number or percentage",
                        },
                        "definition_source": {
                            "type": ["string", "null"],
                            "description": "URL indicating the source of the definition",
                            "example": "https://unece.org/trade/uncefact/core-components-data-type-catalogue",
                        },
                        "cardinality": {
                            "type": "string",
                            "description": "SCORE-style supplementary-component cardinality label",
                            "enum": ["Prohibited", "Optional", "Required"],
                            "example": "Optional",
                        },
                        "value_constraint": {
                            "type": ["object", "null"],
                            "description": "Value constraint (default_value or fixed_value) for the supplementary component. Exactly one of default_value or fixed_value must be set.",
                            "properties": {
                                "default_value": {
                                    "type": ["string", "null"],
                                    "description": "Default value for the supplementary component",
                                    "example": "decimal",
                                },
                                "fixed_value": {
                                    "type": ["string", "null"],
                                    "description": "Fixed value for the supplementary component",
                                    "example": "integer",
                                },
                            },
                        },
                        "is_deprecated": {
                            "type": "boolean",
                            "description": "Whether the supplementary component is deprecated",
                            "example": False,
                        },
                    },
                    "required": ["dt_sc_manifest_id", "dt_sc_id", "guid", "cardinality", "is_deprecated"],
                },
            },
            "namespace": {
                "type": ["object", "null"],
                "description": "Namespace information",
                "properties": {
                    "namespace_id": {
                        "type": "integer",
                        "description": "Unique identifier for the namespace",
                        "example": 1,
                    },
                    "uri": {
                        "type": "string",
                        "description": "Namespace URI (Uniform Resource Identifier)",
                        "example": "http://www.openapplications.org/oagis/10",
                    },
                    "prefix": {"type": ["string", "null"], "description": "Namespace prefix", "example": "oagis"},
                },
                "required": ["namespace_id", "uri"],
            },
            "library": {
                "type": "object",
                "description": "Library information",
                "properties": {
                    "library_id": {"type": "integer", "description": "Unique identifier for the library", "example": 1},
                    "name": {"type": "string", "description": "Library name", "example": "connectSpec"},
                },
                "required": ["library_id", "name"],
            },
            "release": {
                "type": "object",
                "description": "Release information",
                "properties": {
                    "release_id": {"type": "integer", "description": "Unique identifier for the release", "example": 1},
                    "release_num": {"type": "string", "description": "Release number", "example": "10.6"},
                    "state": {"type": "string", "description": "Release state", "example": "Published"},
                },
                "required": ["release_id", "release_num", "state"],
            },
            "log": {
                "type": ["object", "null"],
                "description": "Log information",
                "properties": {
                    "log_id": {"type": "integer", "description": "Unique identifier for the log", "example": 123},
                    "revision_num": {"type": "integer", "description": "Revision number", "example": 1},
                    "revision_tracking_num": {
                        "type": "integer",
                        "description": "Revision tracking number",
                        "example": 1,
                    },
                },
                "required": ["log_id", "revision_num", "revision_tracking_num"],
            },
            "owner": {
                "type": "object",
                "description": "User information about the owner of the data type (DT)",
                "properties": {
                    "user_id": {"type": "integer", "description": "Unique identifier for the user", "example": 1},
                    "login_id": {"type": "string", "description": "User's login identifier", "example": "admin"},
                    "username": {
                        "type": "string",
                        "description": "Display name of the user",
                        "example": "Administrator",
                    },
                    "roles": {
                        "type": "array",
                        "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                        "description": "List of roles assigned to the user",
                        "example": ["Admin"],
                    },
                },
                "required": ["user_id", "login_id", "username", "roles"],
            },
            "created": {
                "type": "object",
                "description": "Information about the creation of the data type (DT)",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who created the data type (DT)",
                        "properties": {
                            "user_id": {
                                "type": "integer",
                                "description": "Unique identifier for the user",
                                "example": 1,
                            },
                            "login_id": {
                                "type": "string",
                                "description": "User's login identifier",
                                "example": "admin",
                            },
                            "username": {
                                "type": "string",
                                "description": "Display name of the user",
                                "example": "Administrator",
                            },
                            "roles": {
                                "type": "array",
                                "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                "description": "List of roles assigned to the user",
                                "example": ["Admin"],
                            },
                        },
                        "required": ["user_id", "login_id", "username", "roles"],
                    },
                    "when": {
                        "type": "string",
                        "format": "date-time",
                        "description": "Creation timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)",
                        "example": "2024-01-15T10:30:00Z",
                    },
                },
                "required": ["who", "when"],
            },
            "last_updated": {
                "type": "object",
                "description": "Information about the last update of the data type (DT)",
                "properties": {
                    "who": {
                        "type": "object",
                        "description": "User who last updated the data type (DT)",
                        "properties": {
                            "user_id": {
                                "type": "integer",
                                "description": "Unique identifier for the user",
                                "example": 1,
                            },
                            "login_id": {
                                "type": "string",
                                "description": "User's login identifier",
                                "example": "admin",
                            },
                            "username": {
                                "type": "string",
                                "description": "Display name of the user",
                                "example": "Administrator",
                            },
                            "roles": {
                                "type": "array",
                                "items": {"type": "string", "enum": ["Admin", "Developer", "End-User"]},
                                "description": "List of roles assigned to the user",
                                "example": ["Admin"],
                            },
                        },
                        "required": ["user_id", "login_id", "username", "roles"],
                    },
                    "when": {
                        "type": "string",
                        "format": "date-time",
                        "description": "Last update timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)",
                        "example": "2024-01-20T14:45:00Z",
                    },
                },
                "required": ["who", "when"],
            },
        },
        "required": [
            "dt_manifest_id",
            "dt_id",
            "guid",
            "den",
            "library",
            "release",
            "commonly_used",
            "is_deprecated",
            "supplementary_components",
            "owner",
            "created",
            "last_updated",
        ],
    },
)
async def get_data_type(
    dt_manifest_id: Annotated[
        int, Field(gt=0, description="Unique numeric identifier of the data type manifest to retrieve.")
    ],
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> GetDataTypeResponse:
    """
    Get a specific data type by its manifest ID.

    This function retrieves a single data type from the database and returns
    detailed information including metadata about who created it and when it was
    last updated, along with data type-specific attributes, namespace information,
    and associated supplementary components.

    Args:
        dt_manifest_id (int): The unique identifier of the data type manifest to fetch

    Returns:
        GetDataTypeResponse: Response object containing:
            - dt_manifest_id: Unique identifier for the data type manifest
            - dt_id: Unique identifier for the data type
            - guid: Unique identifier within the release
            - den: Dictionary Entry Name (computed from qualifier and data_type_term)
            - data_type_term: Data type term
            - qualifier: Qualifier (if any)
            - representation_term: Representation term
            - six_digit_id: Six digit ID
            - definition: Description of the data type
            - definition_source: URL indicating the source of the definition
            - content_component_definition: Description of the content component
            - namespace: Namespace information (if any)
            - library: Library information with library_id and name
            - release: Release information with release_id, release_num, and state
            - log: Log information with log_id, revision_num, and revision_tracking_num (if any)
            - commonly_used: Whether the data type is commonly used
            - is_deprecated: Whether the data type is deprecated
            - state: State of the data type
            - base_dt: Base data type information (if this data type is based on another)
            - owner: User information about the owner of the data type
            - supplementary_components: List of supplementary components with their details
            - created: Information about the creation of the data type
            - last_updated: Information about the last update of the data type

    Raises:
        ToolError: If validation fails, the data type manifest is not found, or other errors occur.
            Common error scenarios include:
            - Data type manifest with the specified ID does not exist
            - Database connection issues
            - Authentication failures

    Examples:
        Get a specific data type:
        >>> result = get_data_type(dt_manifest_id=123)
        >>> print(f"Data type: {result.data_type_term} (GUID: {result.guid})")
        >>> print(f"Owner: {result.owner.username}")
        >>> print(f"Supplementary components: {len(result.supplementary_components)}")

        Access supplementary components:
        >>> for sc in result.supplementary_components:
        ...     print(f"  {sc.property_term}: {sc.definition}")
    """
    try:
        row = await data_type_service.get(dt_manifest_id)
        if row is None:
            raise ValueError(
                f"The data type with manifest ID {dt_manifest_id} was not found. Please check the ID and try again."
            )
        return GetDataTypeResponse.model_validate(row, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to retrieve data type {dt_manifest_id}.") from exc


@mcp.tool(
    name="create_dt",
    description="Create a new DT (Data Type) from an existing base DT, with optional initial mutable-field overrides.",
    output_schema={
        "type": "object",
        "description": "Response containing the created DT manifest identifier.",
        "properties": {
            "dt_manifest_id": {"type": "integer", "description": "Created DT manifest identifier.", "example": 12345}
        },
        "required": ["dt_manifest_id"],
    },
)
async def create_dt(
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
    based_dt_manifest_id: Annotated[
        int,
        Field(
            gt=0,
            description=(
                "Base DT manifest identifier used to derive the new DT. "
                "If the base DT is in the same library, it must be from the target release. "
                "If it is in a different library, its release must be one of the target release dependencies."
            ),
        ),
    ],
    qualifier: Annotated[
        str | None,
        Field(
            description=(
                "Qualifier to start with. In CCTS, a BDT qualifier term is a word or words that help define and "
                "differentiate a qualified BDT from its higher-level BDT. Omit to use the value derived from the base DT."
            ),
        ),
    ] = None,
    six_digit_id: Annotated[
        str | None,
        Field(
            description=(
                "Initial six-character suffix for the BDT Type name from the UN/CEFACT XML Schema NDR. "
                "It must be unique within the namespace and use only letters and digits. "
                "Omit to start without a six-character suffix."
            ),
        ),
    ] = None,
    deprecated: Annotated[
        bool | None,
        Field(description="Initial deprecation flag override. Omit to keep the base DT value."),
    ] = None,
    namespace_id: Annotated[
        int | None,
        Field(
            ge=0,
            description=(
                "Namespace to use when creating this DT. In the original score-http create logic, omitting this "
                "uses the base DT namespace only when it is compatible with the requester's role. Use 0 to clear "
                "the namespace in this API."
            ),
        ),
    ] = None,
    content_component_definition: Annotated[
        str | None,
        Field(
            description=("Content component definition to start with. Omit to use the value derived from the base DT."),
        ),
    ] = None,
    definition: Annotated[
        str | None,
        Field(
            description=(
                "Definition text to start with. This is the explanatory text that describes what the DT means. "
                "Omit to use the value derived from the base DT."
            ),
        ),
    ] = None,
    definition_source: Annotated[
        str | None,
        Field(
            description=(
                "Definition source to start with. Use this to record where the definition came from, such as a "
                "specification, standard, or reference URL. Omit to use the value derived from the base DT."
            ),
        ),
    ] = None,
    tag_id: Annotated[
        list[int] | None,
        Field(
            description="Optional tag identifier list to attach. Use get_tags() to discover valid tag IDs.",
        ),
    ] = None,
    default_primitive: Annotated[
        DefaultPrimitiveSelectionInput | None,
        Field(
            description=(
                "Initial default primitive target. Provide exactly one of xbt_manifest_id, "
                "code_list_manifest_id, or agency_id_list_manifest_id."
            ),
        ),
    ] = None,
    add_primitives: Annotated[
        list[PrimitiveMutationInput] | None,
        Field(
            default=None,
            description=(
                "Primitive rows to add during creation. Each row uses the form {cdt_pri_name, xbt_manifest_id, "
                "code_list_manifest_id, agency_id_list_manifest_id}. Use default_primitive to choose which remaining "
                "primitive is the default."
            ),
        ),
    ] = None,
    remove_primitives: Annotated[
        list[PrimitiveMutationInput] | None,
        Field(
            default=None,
            description=(
                "Primitive rows to remove during creation. Each row uses the form {cdt_pri_name, xbt_manifest_id, "
                "code_list_manifest_id, agency_id_list_manifest_id}."
            ),
        ),
    ] = None,
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> CreateDataTypeResponse:
    """
    Create a DT in a role-appropriate release branch.

    Rules:
    - developers can target only the `Working` release
    - end-users can target only non-`Working` releases
    - the target release must already be `Published`
    - the new DT is derived from `based_dt_manifest_id` and starts in `WIP`
    - the new DT copies `dt_awd_pri` and DT supplementary components (with their `dt_sc_awd_pri`) from the selected base DT
    - optional mutable fields can be applied during creation
    """
    try:
        result = await data_type_service.create_dt(
            release_id=release_id,
            based_dt_manifest_id=based_dt_manifest_id,
            tag_id=tag_id,
            qualifier=UNSET if qualifier is None else qualifier,
            six_digit_id=UNSET if six_digit_id is None else six_digit_id,
            deprecated=UNSET if deprecated is None else deprecated,
            namespace_id=UNSET if namespace_id is None else (None if namespace_id == 0 else namespace_id),
            content_component_definition=UNSET
            if content_component_definition is None
            else content_component_definition,
            definition=UNSET if definition is None else definition,
            definition_source=UNSET if definition_source is None else definition_source,
            xbt_manifest_id=UNSET if default_primitive is None else default_primitive.xbt_manifest_id,
            code_list_manifest_id=UNSET if default_primitive is None else default_primitive.code_list_manifest_id,
            agency_id_list_manifest_id=(
                UNSET if default_primitive is None else default_primitive.agency_id_list_manifest_id
            ),
            add_primitives=(
                [
                    DataTypePrimitiveServiceRecord(**primitive.model_dump(), is_default=False)
                    for primitive in add_primitives
                ]
                if add_primitives is not None
                else UNSET
            ),
            remove_primitives=(
                [
                    DataTypePrimitiveServiceRecord(**primitive.model_dump(), is_default=False)
                    for primitive in remove_primitives
                ]
                if remove_primitives is not None
                else UNSET
            ),
        )
        return CreateDataTypeResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback="Unable to create the DT.") from exc


@mcp.tool(
    name="update_dt",
    description="Update mutable DT (Data Type) fields while the DT is in WIP.",
    output_schema={
        "type": "object",
        "description": "Response containing the updated DT manifest identifier and changed fields.",
        "properties": {
            "dt_manifest_id": {"type": "integer", "description": "Target DT manifest identifier.", "example": 12345},
            "updates": {
                "type": "array",
                "description": "Updated field names.",
                "items": {"type": "string"},
                "example": ["definition", "definition_source"],
            },
        },
        "required": ["dt_manifest_id", "updates"],
    },
)
async def update_dt(
    dt_manifest_id: Annotated[int, Field(gt=0, description="Target DT manifest identifier.")],
    based_dt_manifest_id: Annotated[
        int | None,
        Field(
            ge=0,
            description=(
                "Updated base DT manifest identifier. Omit to leave unchanged. Use 0 to clear the base DT link. "
                "If the base DT is in the same library, it must be from the target release. "
                "If it is in a different library, its release must be one of the target release dependencies."
            ),
        ),
    ] = None,
    qualifier: Annotated[
        str | None,
        Field(
            description=(
                "Qualifier. In CCTS, a BDT qualifier term is a word or words that help define and differentiate a "
                "qualified BDT from its higher-level BDT. Omit to leave unchanged. Pass an empty string to clear it."
            ),
        ),
    ] = None,
    six_digit_id: Annotated[
        str | None,
        Field(
            description=(
                "Updated six-character suffix for the BDT Type name from the UN/CEFACT XML Schema NDR. "
                "It must be unique within the namespace and use only letters and digits. "
                "Omit to leave unchanged. Pass an empty string to clear it."
            ),
        ),
    ] = None,
    deprecated: Annotated[
        bool | None,
        Field(description="Updated deprecation flag. Omit to leave unchanged."),
    ] = None,
    namespace_id: Annotated[
        int | None,
        Field(
            ge=0,
            description="Updated namespace identifier. Omit to leave unchanged. Use 0 to clear the namespace.",
        ),
    ] = None,
    content_component_definition: Annotated[
        str | None,
        Field(
            description="Updated content component definition. Omit to leave unchanged. Pass an empty string to clear it."
        ),
    ] = None,
    definition: Annotated[
        str | None,
        Field(description="Definition text. Omit to leave unchanged. Pass an empty string to clear it."),
    ] = None,
    definition_source: Annotated[
        str | None,
        Field(description="Definition source. Omit to leave unchanged. Pass an empty string to clear it."),
    ] = None,
    default_primitive: Annotated[
        DefaultPrimitiveSelectionInput | None,
        Field(
            description=(
                "Default primitive target. Provide exactly one of xbt_manifest_id, "
                "code_list_manifest_id, or agency_id_list_manifest_id."
            ),
        ),
    ] = None,
    add_primitives: Annotated[
        list[PrimitiveMutationInput] | None,
        Field(
            description=(
                "Primitive rows to add. Each row uses the form {cdt_pri_name, xbt_manifest_id, "
                "code_list_manifest_id, agency_id_list_manifest_id}. Use default_primitive to choose "
                "which remaining primitive is the default."
            ),
        ),
    ] = None,
    remove_primitives: Annotated[
        list[PrimitiveMutationInput] | None,
        Field(
            description=(
                "Primitive rows to remove. Each row uses the form {cdt_pri_name, xbt_manifest_id, "
                "code_list_manifest_id, agency_id_list_manifest_id}."
            ),
        ),
    ] = None,
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> UpdateDataTypeResponse:
    """
    Update mutable DT fields.

    Rules:
    - the DT must currently be in `WIP`
    - only the owner or an administrator can update it
    - omit a parameter to leave it unchanged
    - pass `0` for `based_dt_manifest_id` to clear the base DT link
    - pass `""` to clear string fields
    - pass `0` for `namespace_id` to clear the namespace
    """
    try:
        result = await data_type_service.update_dt(
            dt_manifest_id=dt_manifest_id,
            based_dt_manifest_id=UNSET
            if based_dt_manifest_id is None
            else (None if based_dt_manifest_id == 0 else based_dt_manifest_id),
            qualifier=UNSET if qualifier is None else qualifier,
            six_digit_id=UNSET if six_digit_id is None else six_digit_id,
            deprecated=UNSET if deprecated is None else deprecated,
            namespace_id=UNSET if namespace_id is None else (None if namespace_id == 0 else namespace_id),
            content_component_definition=UNSET
            if content_component_definition is None
            else content_component_definition,
            definition=UNSET if definition is None else definition,
            definition_source=UNSET if definition_source is None else definition_source,
            xbt_manifest_id=UNSET if default_primitive is None else default_primitive.xbt_manifest_id,
            code_list_manifest_id=UNSET if default_primitive is None else default_primitive.code_list_manifest_id,
            agency_id_list_manifest_id=(
                UNSET if default_primitive is None else default_primitive.agency_id_list_manifest_id
            ),
            add_primitives=(
                [
                    DataTypePrimitiveServiceRecord(**primitive.model_dump(), is_default=False)
                    for primitive in add_primitives
                ]
                if add_primitives is not None
                else UNSET
            ),
            remove_primitives=(
                [
                    DataTypePrimitiveServiceRecord(**primitive.model_dump(), is_default=False)
                    for primitive in remove_primitives
                ]
                if remove_primitives is not None
                else UNSET
            ),
        )
        return UpdateDataTypeResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to update DT {dt_manifest_id}.") from exc


@mcp.tool(
    name="transfer_dt_ownership",
    description="Transfer ownership of a DT (Data Type) to another user.",
    output_schema={
        "type": "object",
        "description": "Response containing the DT manifest identifier and changed fields.",
        "properties": {
            "dt_manifest_id": {"type": "integer", "description": "Target DT manifest identifier.", "example": 12345},
            "updates": {
                "type": "array",
                "description": "Updated field names.",
                "items": {"type": "string"},
                "example": ["owner_user_id"],
            },
        },
        "required": ["dt_manifest_id", "updates"],
    },
)
async def transfer_dt_ownership(
    dt_manifest_id: Annotated[int, Field(gt=0, description="Target DT manifest identifier.")],
    new_owner_user_id: Annotated[int, Field(gt=0, description="User ID of the new owner.")],
    ctx: Context,
    data_type_service: DataTypeService = Depends(get_data_type_service),
    app_user_service: AppUserService = Depends(get_app_user_service),
) -> TransferDataTypeOwnershipResponse:
    """Transfer DT ownership to another user after confirmation."""
    try:
        row = await data_type_service.get(dt_manifest_id)
        if row is None:
            raise ToolError(
                f"The data type with manifest ID {dt_manifest_id} was not found. Please check the ID and try again."
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
            message=(f"Are you sure you want to transfer ownership of '{row.den}' to {target_user_label}?"),
            response_type=None,
        )
        match elicit_result:
            case AcceptedElicitation():
                payload = await data_type_service.transfer_dt_ownership(
                    dt_manifest_id=dt_manifest_id,
                    target_user_id=new_owner_user_id,
                )
                updates = [] if int(new_owner_user_id) == int(row.owner.user_id) else ["owner_user_id"]
                return TransferDataTypeOwnershipResponse(
                    dt_manifest_id=payload.dt_manifest_id,
                    updates=updates,
                )
            case DeclinedElicitation():
                raise ToolError("DT ownership transfer declined by user.")
            case CancelledElicitation():
                raise ToolError("DT ownership transfer cancelled by user.")
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to transfer ownership of DT {dt_manifest_id}.") from exc


@mcp.tool(
    name="create_dt_sc",
    description="Create a new DT supplementary component under a DT and optionally apply the same mutable fields available in update_dt_sc.",
    output_schema={
        "type": "object",
        "description": "Response containing the created DT_SC manifest identifier.",
        "properties": {
            "dt_sc_manifest_id": {
                "type": "integer",
                "description": "Created DT_SC manifest identifier.",
                "example": 12345,
            }
        },
        "required": ["dt_sc_manifest_id"],
    },
)
async def create_dt_sc(
    dt_manifest_id: Annotated[int, Field(gt=0, description="Owning DT manifest identifier.")],
    property_term: Annotated[
        str,
        Field(
            ...,
            min_length=1,
            description=(
                "Property term to start with. In CCTS, a BDT supplementary component property term is a "
                "semantically meaningful name for a unique characteristic that can be used in a BDT."
            ),
        ),
    ],
    representation_term: Annotated[
        str,
        Field(
            ...,
            min_length=1,
            description=(
                "Representation term to start with. In CCTS, this is a semantically meaningful name that "
                "represents the value domain of the supplementary component. Choose an approved CDT data type "
                "term such as `Amount`, `Code`, or `Text`. When this changes, the DT_SC primitive rows are reset "
                "to the default primitive set for that term."
            ),
        ),
    ],
    cardinality: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Cardinality to start with. Use `Prohibited` for `0..0`, `Optional` for `0..1`, or `Required` "
                "for `1..1`. Omit to keep the generated initial cardinality."
            ),
        ),
    ] = None,
    deprecated: Annotated[
        bool | None,
        Field(
            default=None,
            description="Whether this supplementary component should start as deprecated. Omit to keep the generated initial value.",
        ),
    ] = None,
    definition: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Definition text to start with. This is the explanatory text that describes what the "
                "supplementary component means. Omit to keep the generated initial value. Pass an empty string to clear it."
            ),
        ),
    ] = None,
    definition_source: Annotated[
        str | None,
        Field(
            default=None,
            description=(
                "Definition source to start with. Use this to record where the definition came from, such as a "
                "specification, standard, or reference URL. Omit to keep the generated initial value. Pass an empty string to clear it."
            ),
        ),
    ] = None,
    value_constraint: Annotated[
        ValueConstraintInput | None,
        Field(
            default=None,
            description=(
                "Value constraint to start with. Provide default_value to set a default when the element is "
                "omitted, or fixed_value to require one exact value. Omit to keep the generated initial value."
            ),
        ),
    ] = None,
    default_primitive: Annotated[
        DefaultPrimitiveSelectionInput | None,
        Field(
            default=None,
            description=(
                "Default primitive to start with. Provide exactly one of xbt_manifest_id, code_list_manifest_id, "
                "or agency_id_list_manifest_id. Omit to keep the generated initial value."
            ),
        ),
    ] = None,
    add_primitives: Annotated[
        list[PrimitiveMutationInput] | None,
        Field(
            default=None,
            description=(
                "Primitive rows to add during creation. Each row uses the form {cdt_pri_name, xbt_manifest_id, "
                "code_list_manifest_id, agency_id_list_manifest_id}. Use default_primitive to choose which "
                "remaining primitive is the default. Omit to keep only the generated initial primitive rows."
            ),
        ),
    ] = None,
    remove_primitives: Annotated[
        list[PrimitiveMutationInput] | None,
        Field(
            default=None,
            description=(
                "Primitive rows to remove during creation. Each row uses the form {cdt_pri_name, xbt_manifest_id, "
                "code_list_manifest_id, agency_id_list_manifest_id}. Omit to keep the generated initial primitive rows."
            ),
        ),
    ] = None,
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> CreateDataTypeSupplementaryComponentResponse:
    """Create a DT_SC under a WIP DT."""
    try:
        result = await data_type_service.create_dt_sc(
            owner_dt_manifest_id=dt_manifest_id,
            property_term=property_term,
            representation_term=representation_term,
            cardinality=UNSET if cardinality is None else cardinality,
            deprecated=UNSET if deprecated is None else deprecated,
            default_value=UNSET if value_constraint is None else value_constraint.default_value,
            fixed_value=UNSET if value_constraint is None else value_constraint.fixed_value,
            definition=UNSET if definition is None else definition,
            definition_source=UNSET if definition_source is None else definition_source,
            xbt_manifest_id=UNSET if default_primitive is None else default_primitive.xbt_manifest_id,
            code_list_manifest_id=UNSET if default_primitive is None else default_primitive.code_list_manifest_id,
            agency_id_list_manifest_id=(
                UNSET if default_primitive is None else default_primitive.agency_id_list_manifest_id
            ),
            add_primitives=UNSET
            if add_primitives is None
            else [DataTypePrimitiveServiceRecord(**item.model_dump(), is_default=False) for item in add_primitives],
            remove_primitives=UNSET
            if remove_primitives is None
            else [DataTypePrimitiveServiceRecord(**item.model_dump(), is_default=False) for item in remove_primitives],
        )
        return CreateDataTypeSupplementaryComponentResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(
            exc, fallback=f"Unable to create a DT supplementary component for DT {dt_manifest_id}."
        ) from exc


@mcp.tool(
    name="update_dt_sc",
    description="Update mutable DT supplementary-component fields while the owner DT is in WIP.",
    output_schema={
        "type": "object",
        "description": "Response containing the updated DT_SC manifest identifier and changed fields.",
        "properties": {
            "dt_sc_manifest_id": {
                "type": "integer",
                "description": "Target DT_SC manifest identifier.",
                "example": 12345,
            },
            "updates": {"type": "array", "items": {"type": "string"}, "example": ["property_term", "definition"]},
        },
        "required": ["dt_sc_manifest_id", "updates"],
    },
)
async def update_dt_sc(
    dt_sc_manifest_id: Annotated[int, Field(gt=0, description="Target DT_SC manifest identifier.")],
    property_term: Annotated[
        str | None,
        Field(
            description=(
                "Property term. In CCTS, a BDT supplementary component property term is a semantically meaningful "
                "name for a unique characteristic that can be used in a BDT. Omit to leave unchanged. Pass an "
                "empty string to clear it."
            ),
        ),
    ] = None,
    representation_term: Annotated[
        str | None,
        Field(
            description=(
                "Representation term. In CCTS, this is a semantically meaningful name that represents the value "
                "domain of the supplementary component. Use an approved CDT data type term such as `Amount`, "
                "`Code`, or `Text`. When this changes, the DT_SC primitive rows are reset to the default primitive "
                "set for that term. Omit to leave unchanged."
            ),
        ),
    ] = None,
    cardinality: Annotated[
        str | None,
        Field(
            description=(
                "Updated DT_SC cardinality. Use `Prohibited` for `0..0`, `Optional` for `0..1`, "
                "or `Required` for `1..1`. Omit to leave unchanged."
            ),
        ),
    ] = None,
    deprecated: Annotated[bool | None, Field(description="Updated deprecation flag. Omit to leave unchanged.")] = None,
    definition: Annotated[
        str | None,
        Field(
            description=(
                "Definition text. This is the explanatory text that describes what the supplementary component means. "
                "Omit to leave unchanged. Pass an empty string to clear it."
            ),
        ),
    ] = None,
    definition_source: Annotated[
        str | None,
        Field(
            description=(
                "Definition source. Use this to record where the definition came from, such as a "
                "specification, standard, or reference URL. Omit to leave unchanged. Pass an empty string to clear it."
            ),
        ),
    ] = None,
    value_constraint: Annotated[
        ValueConstraintInput | None,
        Field(
            description=(
                "Updated value constraint. Provide default_value to set a default when the element is omitted, "
                "or fixed_value to require one exact value. Omit to leave unchanged."
            ),
        ),
    ] = None,
    default_primitive: Annotated[
        DefaultPrimitiveSelectionInput | None,
        Field(
            description=(
                "Default primitive target. Provide exactly one of xbt_manifest_id, "
                "code_list_manifest_id, or agency_id_list_manifest_id. Omit to leave unchanged."
            ),
        ),
    ] = None,
    add_primitives: Annotated[
        list[PrimitiveMutationInput] | None,
        Field(
            description=(
                "Primitive rows to add. Each row uses the form {cdt_pri_name, xbt_manifest_id, "
                "code_list_manifest_id, agency_id_list_manifest_id}. Use default_primitive to choose "
                "which remaining primitive is the default. Omit to leave unchanged."
            ),
        ),
    ] = None,
    remove_primitives: Annotated[
        list[PrimitiveMutationInput] | None,
        Field(
            description=(
                "Primitive rows to remove. Each row uses the form {cdt_pri_name, xbt_manifest_id, "
                "code_list_manifest_id, agency_id_list_manifest_id}. Omit to leave unchanged."
            ),
        ),
    ] = None,
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> UpdateDataTypeSupplementaryComponentResponse:
    """Update mutable DT_SC fields."""
    try:
        result = await data_type_service.update_dt_sc(
            dt_sc_manifest_id=dt_sc_manifest_id,
            property_term=UNSET if property_term is None else property_term,
            representation_term=UNSET if representation_term is None else representation_term,
            cardinality=UNSET if cardinality is None else cardinality,
            deprecated=UNSET if deprecated is None else deprecated,
            default_value=UNSET if value_constraint is None else value_constraint.default_value,
            fixed_value=UNSET if value_constraint is None else value_constraint.fixed_value,
            definition=UNSET if definition is None else definition,
            definition_source=UNSET if definition_source is None else definition_source,
            xbt_manifest_id=UNSET if default_primitive is None else default_primitive.xbt_manifest_id,
            code_list_manifest_id=UNSET if default_primitive is None else default_primitive.code_list_manifest_id,
            agency_id_list_manifest_id=(
                UNSET if default_primitive is None else default_primitive.agency_id_list_manifest_id
            ),
            add_primitives=(
                [
                    DataTypePrimitiveServiceRecord(**primitive.model_dump(), is_default=False)
                    for primitive in add_primitives
                ]
                if add_primitives is not None
                else UNSET
            ),
            remove_primitives=(
                [
                    DataTypePrimitiveServiceRecord(**primitive.model_dump(), is_default=False)
                    for primitive in remove_primitives
                ]
                if remove_primitives is not None
                else UNSET
            ),
        )
        return UpdateDataTypeSupplementaryComponentResponse.model_validate(result, from_attributes=True)
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to update DT_SC {dt_sc_manifest_id}.") from exc


@mcp.tool(
    name="delete_dt_sc",
    description="Delete a DT supplementary component while the owner DT is in WIP.",
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def delete_dt_sc(
    dt_sc_manifest_id: Annotated[int, Field(gt=0, description="Target DT_SC manifest identifier.")],
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> dict[str, object]:
    """Delete a DT_SC."""
    try:
        await data_type_service.delete_dt_sc(dt_sc_manifest_id=dt_sc_manifest_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to delete DT_SC {dt_sc_manifest_id}.") from exc


@mcp.tool(
    name="add_dt_tags",
    description="Attach one or more tags to a DT (Data Type).",
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def add_dt_tags(
    dt_manifest_id: Annotated[int, Field(gt=0, description="Target DT manifest identifier.")],
    tag_id: Annotated[
        list[int],
        Field(description="Tag identifier list to attach. Use get_tags() to discover valid tag IDs."),
    ],
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> dict[str, object]:
    """Add tags to a DT while it is in `WIP`."""
    try:
        await data_type_service.add_dt_tags(dt_manifest_id=dt_manifest_id, tag_id=tag_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to add tags to DT {dt_manifest_id}.") from exc


@mcp.tool(
    name="remove_dt_tags",
    description="Detach one or more tags from a DT (Data Type).",
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def remove_dt_tags(
    dt_manifest_id: Annotated[int, Field(gt=0, description="Target DT manifest identifier.")],
    tag_id: Annotated[
        list[int],
        Field(description="Tag identifier list to remove. Use get_tags() to discover valid tag IDs."),
    ],
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> dict[str, object]:
    """Remove tags from a DT while it is in `WIP`."""
    try:
        await data_type_service.remove_dt_tags(dt_manifest_id=dt_manifest_id, tag_id=tag_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to remove tags from DT {dt_manifest_id}.") from exc


@mcp.tool(
    name="change_dt_state",
    description="Change the lifecycle state of a DT (Data Type).",
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def change_dt_state(
    dt_manifest_id: Annotated[int, Field(gt=0, description="Target DT manifest identifier.")],
    state: Annotated[
        Literal["Deleted", "WIP", "Draft", "QA", "Candidate", "Production"],
        Field(description="Target lifecycle state."),
    ],
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> dict[str, object]:
    """
    Change a DT lifecycle state according to connectCenter rules.

    Valid transitions depend on the DT release branch:
    - `Working` release DTs: `Deleted -> WIP`, `WIP -> Deleted|Draft`, `Draft -> WIP|Candidate`, `Candidate -> WIP`
    - non-`Working` release DTs: `Deleted -> WIP`, `WIP -> Deleted|QA`, `QA -> WIP|Production`, `Production` is terminal
    """
    try:
        await data_type_service.change_dt_state(dt_manifest_id=dt_manifest_id, state=state)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to change the DT state for {dt_manifest_id}.") from exc


@mcp.tool(
    name="revise_or_amend_dt",
    description=(
        "Create a new editable DT (Data Type) revision from a stable DT revision. "
        "For end-user DTs, this is called an amendment."
    ),
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def revise_or_amend_dt(
    dt_manifest_id: Annotated[int, Field(gt=0, description="Target DT manifest identifier.")],
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> dict[str, object]:
    """
    Revise or amend a DT according to connectCenter rules.

    Rules:
    - developer-side DTs can be revised only from the `Published` state in the `Working` release
    - end-user DTs can be amended only from the `Production` state in a non-`Working` release
    - the requester and the DT owner must belong to the same role family
    """
    try:
        await data_type_service.revise_dt(dt_manifest_id=dt_manifest_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to revise or amend DT {dt_manifest_id}.") from exc


@mcp.tool(
    name="cancel_dt",
    description="Cancel the current DT (Data Type) revision and restore the previous stable revision.",
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def cancel_dt(
    dt_manifest_id: Annotated[int, Field(gt=0, description="Target DT manifest identifier.")],
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> dict[str, object]:
    """
    Cancel the current DT revision according to connectCenter rules.

    Rules:
    - only DTs in the `WIP` state can be cancelled
    - the requester and the DT owner must belong to the same role family
    - the DT must be on the requester's allowed branch (`Working` for developers, non-`Working` for end-users)
    """
    try:
        await data_type_service.cancel_dt(dt_manifest_id=dt_manifest_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to cancel DT {dt_manifest_id}.") from exc


@mcp.tool(
    name="discard_dt",
    description="Discard a Deleted DT (Data Type) and its direct DT-owned records permanently.",
    output_schema=EMPTY_OUTPUT_SCHEMA,
)
async def discard_dt(
    dt_manifest_id: Annotated[int, Field(gt=0, description="Target DT manifest identifier.")],
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> dict[str, object]:
    """
    Discard a Deleted DT from the database.

    Rules:
    - the DT must already be in the `Deleted` state
    - related BCCPs that still use the DT must be discarded first
    - derived DTs must be discarded first
    - this operation is irreversible
    """
    try:
        await data_type_service.discard_dt(dt_manifest_id=dt_manifest_id)
        return {}
    except Exception as exc:
        raise _to_tool_error(exc, fallback=f"Unable to discard DT {dt_manifest_id}.") from exc


def _build_data_type_service(
    session: AsyncSession,
    requester: AuthenticatedUser,
) -> DataTypeService:
    """Construct the data-type service for an MCP request."""
    plugin = get_vendor_plugin()
    release_service = ReleaseService(
        plugin.create_release_repository(session),
        plugin.create_app_user_repository(session),
        requester=requester,
    )
    return DataTypeService(
        plugin.create_data_type_repository(session),
        release_service,
        plugin.create_app_user_repository(session),
        requester=requester,
    )


def _to_list_response(*, items: list[Any], total: int, offset: int, limit: int) -> GetDataTypePaginationResponse:
    """Build the paginated MCP response model."""
    return GetDataTypePaginationResponse(
        total_items=total,
        offset=offset,
        limit=limit,
        items=[DataTypeResponseEntry.model_validate(item, from_attributes=True) for item in items],
    )
