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
- get_acc: Retrieve a single ACC (Aggregate Core Component) by its manifest ID, including
  all related entities (namespace, creator, owner, release, log, based_acc_manifest) and
  all associated ASCCs and BCCs with their relationships.

- get_asccp: Retrieve a single ASCCP (Association Core Component Property) by its manifest ID,
  including all related entities and the role_of_acc relationship.

- get_bccp: Retrieve a single BCCP (Basic Core Component Property) by its manifest ID, including
  all related entities and the associated data type.

- get_core_components: Retrieve paginated lists of Core Components (ACCs, ASCCPs, or BCCPs)
  filtered by release with optional filters. Supports custom sorting and pagination.

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

import logging
from typing import Annotated, Literal, List, Optional, Union

from fastapi import HTTPException
from fastmcp import FastMCP
from fastmcp.exceptions import ToolError
from pydantic import Field

from services.models import SeqKey, AsccManifest, BccManifest, AccManifest, Bcc, AsccpManifest, BccpManifest
from services import CoreComponentService, DateRangeParams, PaginationParams
from tools import _validate_auth_and_db, parse_order_by_to_sorts, _create_user_info
from tools.models.core_component import (
    AccInfo,
    AsccpInfo,
    AsccRelationshipInfo,
    BaseAccInfo,
    BaseDtInfo,
    BccpInfo,
    BccRelationshipInfo,
    CoreComponentInfo,
    DtInfo,
    GetAccResponse,
    GetAsccpResponse,
    GetBccpResponse,
    GetCoreComponentsResponse,
)
from tools.models.common import LibraryInfo, LogInfo, NamespaceInfo, ReleaseInfo, WhoAndWhen
from tools.utils import parse_date_range, validate_and_create_value_constraint

# Configure logging
logger = logging.getLogger(__name__)

mcp = FastMCP("Score MCP Server - Core Component Tools")


@mcp.tool(
    name="get_acc",
    description="Get a specific ACC by its manifest ID",
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
    acc_manifest_id: Annotated[int, Field(
        description="Unique numeric identifier of the ACC manifest to retrieve.",
        examples=[123, 456, 789],
        gt=0,
        title="ACC Manifest ID"
    )]
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Get ACC
    try:
        service = CoreComponentService()
        manifest = service.get_acc_by_manifest_id(acc_manifest_id)

        acc_resp = _create_acc_result(manifest.acc, manifest, engine)
        logger.info(f"ACC response: {acc_resp}")
        return acc_resp
    except HTTPException as e:
        logger.error(f"HTTP error retrieving ACC: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The ACC manifest with ID {acc_manifest_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving ACC: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the ACC: {str(e)}. Please contact your system administrator.") from e


def _get_entity_type_from_bcc(bcc: Bcc) -> Optional[Literal["Attribute", "Element"]]:
    """Get entity type string from Bcc entity_type field."""
    return "Attribute" if bcc.entity_type == 0 else "Element"


def _create_relationship_info(seq_key: SeqKey, acc_info: AccInfo) -> Union[AsccRelationshipInfo, BccRelationshipInfo]:
    """
    Create AsccRelationshipInfo/BccRelationshipInfo from SeqKey with related manifest.
    
    Handles two relationship types:
    - ASCC → ASCCP → ACC (role_of_acc): Association relationships
    - BCC → BCCP → DT: Basic property relationships with data types
    """
    if seq_key.ascc_manifest_id:
        # ASCC relationship: ASCC → ASCCP → ACC (role_of_acc)
        # ASCC connects the source ACC to an ASCCP, which has a role_of_acc pointing to another ACC
        ascc_manifest = seq_key.ascc_manifest
        asccp_manifest = ascc_manifest.to_asccp_manifest
        
        # Create ASCCP info
        asccp_info = AsccpInfo(
            asccp_manifest_id=asccp_manifest.asccp_manifest_id,
            asccp_id=asccp_manifest.asccp_id,
            guid=asccp_manifest.asccp.guid,
            type=asccp_manifest.asccp.type,
            property_term=asccp_manifest.asccp.property_term,
            definition=asccp_manifest.asccp.definition,
            definition_source=asccp_manifest.asccp.definition_source,
            role_of_acc_manifest_id=asccp_manifest.role_of_acc_manifest_id,
            den=asccp_manifest.den,
            is_deprecated=asccp_manifest.asccp.is_deprecated
        )
        
        return AsccRelationshipInfo(
            component_type="ASCC",
            ascc_manifest_id=ascc_manifest.ascc_manifest_id,
            ascc_id=ascc_manifest.ascc_id,
            guid=ascc_manifest.ascc.guid,
            den=ascc_manifest.den,
            cardinality_min=ascc_manifest.ascc.cardinality_min,
            cardinality_max=ascc_manifest.ascc.cardinality_max,
            is_deprecated=ascc_manifest.ascc.is_deprecated,
            definition=ascc_manifest.ascc.definition,
            definition_source=ascc_manifest.ascc.definition_source,
            from_acc=acc_info,
            to_asccp=asccp_info
        )
    elif seq_key.bcc_manifest_id:
        # BCC relationship: BCC → BCCP → DT
        # BCC connects the source ACC to a BCCP, which has a BDT/DT providing the data type
        bcc_manifest = seq_key.bcc_manifest
        bccp_manifest = bcc_manifest.to_bccp_manifest
        
        # Create BCCP info
        bccp_info = BccpInfo(
            bccp_manifest_id=bccp_manifest.bccp_manifest_id,
            bccp_id=bccp_manifest.bccp_id,
            guid=bccp_manifest.bccp.guid,
            property_term=bccp_manifest.bccp.property_term,
            representation_term=bccp_manifest.bccp.representation_term,
            definition=bccp_manifest.bccp.definition,
            definition_source=bccp_manifest.bccp.definition_source,
            bdt_manifest=DtInfo(
                dt_manifest_id=bccp_manifest.bdt_manifest.dt_manifest_id,
                dt_id=bccp_manifest.bdt_manifest.dt_id,
                guid=bccp_manifest.bdt_manifest.dt.guid,
                den=bccp_manifest.bdt_manifest.den,
                data_type_term=bccp_manifest.bdt_manifest.dt.data_type_term,
                qualifier=bccp_manifest.bdt_manifest.dt.qualifier,
                representation_term=bccp_manifest.bdt_manifest.dt.representation_term,
                six_digit_id=bccp_manifest.bdt_manifest.dt.six_digit_id,
                based_dt_manifest_id=bccp_manifest.bdt_manifest.based_dt_manifest_id,
                definition=bccp_manifest.bdt_manifest.dt.definition,
                definition_source=bccp_manifest.bdt_manifest.dt.definition_source,
                is_deprecated=bccp_manifest.bdt_manifest.dt.is_deprecated
            ),
            den=bccp_manifest.den,
            is_deprecated=bccp_manifest.bccp.is_deprecated
        )
        
        value_constraint = validate_and_create_value_constraint(
            default_value=bcc_manifest.bcc.default_value,
            fixed_value=bcc_manifest.bcc.fixed_value
        )
        return BccRelationshipInfo(
            component_type="BCC",
            bcc_manifest_id=bcc_manifest.bcc_manifest_id,
            bcc_id=bcc_manifest.bcc_id,
            guid=bcc_manifest.bcc.guid,
            den=bcc_manifest.den,
            entity_type=_get_entity_type_from_bcc(bcc_manifest.bcc),
            cardinality_min=bcc_manifest.bcc.cardinality_min,
            cardinality_max=bcc_manifest.bcc.cardinality_max,
            is_deprecated=bcc_manifest.bcc.is_deprecated,
            is_nillable=bcc_manifest.bcc.is_nillable,
            definition=bcc_manifest.bcc.definition,
            definition_source=bcc_manifest.bcc.definition_source,
            from_acc=acc_info,
            value_constraint=value_constraint,
            to_bccp=bccp_info
        )
    else:
        raise ValueError("SeqKey must have either ascc_manifest_id or bcc_manifest_id")


def _sort_relationships(relationships: List[Union[AsccRelationshipInfo, BccRelationshipInfo]]) -> List[Union[AsccRelationshipInfo, BccRelationshipInfo]]:
    """Sort associations with Attributes first, then Elements, maintaining order within each type."""
    # Separate attributes and elements
    attributes = []
    elements = []
    
    for relationship in relationships:
        if relationship.component_type == "BCC" and hasattr(relationship, 'entity_type') and relationship.entity_type == "Attribute":
            attributes.append(relationship)
        else:
            # All ASCC related components and BCC Element related components go to elements
            elements.append(relationship)
    
    # Return attributes first, then elements
    return attributes + elements


def _get_relationships_for_acc(acc_manifest_id: int) -> List[Union[AsccRelationshipInfo, BccRelationshipInfo]]:
    """
    Get relationships for a specific ACC manifest.
    
    ACC has relationships with ASCC (which relates to ASCCP → ACC) and BCC (which relates to BCCP → DT).
    
    Args:
        acc_manifest_id: Unique identifier of the ACC manifest
        
    Returns:
        List of relationship info objects (ASCC or BCC relationships)
    """
    # Use service to get database query results
    cc_service = CoreComponentService()
    acc_manifest = cc_service.get_acc_by_manifest_id(acc_manifest_id)
    
    # Create AccInfo
    acc_info = AccInfo(
        acc_manifest_id=acc_manifest.acc_manifest_id,
        acc_id=acc_manifest.acc_id,
        guid=acc_manifest.acc.guid,
        object_class_term=acc_manifest.acc.object_class_term,
        definition=acc_manifest.acc.definition,
        definition_source=acc_manifest.acc.definition_source,
        den=acc_manifest.den,
        is_deprecated=acc_manifest.acc.is_deprecated
    )
    
    # Get SeqKeys from service
    seq_keys = cc_service.get_relationships_for_acc(acc_manifest_id)
    
    if not seq_keys:
        return []
    
    # Convert SeqKeys to relationship info objects
    relationships = []
    for seq_key in seq_keys:
        relationship = _create_relationship_info(seq_key, acc_info)
        relationships.append(relationship)
    
    return _sort_relationships(relationships)


def _create_acc_result(acc, manifest, engine=None) -> GetAccResponse:
    """
    Create an ACC result from an Acc model instance.
    
    Args:
        acc: Acc model instance
        manifest: AccManifest model instance (required)
        engine: Database engine (optional, for fetching associations)
        
    Returns:
        GetAccResponse: Formatted ACC result
    """
    # Create namespace info if available
    namespace_info = None
    if acc.namespace:
        namespace_info = NamespaceInfo(
            namespace_id=acc.namespace.namespace_id,
            prefix=acc.namespace.prefix,
            uri=acc.namespace.uri
        )

    # Create library info from release
    library_info = LibraryInfo(
        library_id=manifest.release.library_id,
        name=manifest.release.library.name
    )

    # Create release info from manifest
    release_info = ReleaseInfo(
        release_id=manifest.release_id,
        release_num=manifest.release.release_num,
        state=manifest.release.state
    )

    # Create log info from manifest
    log_info = None
    if manifest.log:
        log_info = LogInfo(
            log_id=manifest.log.log_id,
            revision_num=manifest.log.revision_num,
            revision_tracking_num=manifest.log.revision_tracking_num
        )

    # Create base ACC info if available
    base_acc_info = None
    if manifest.based_acc_manifest:
        base_manifest = manifest.based_acc_manifest
        base_acc = base_manifest.acc
        
        # Create namespace info for base ACC if available
        base_namespace_info = None
        if base_acc.namespace:
            base_namespace_info = NamespaceInfo(
                namespace_id=base_acc.namespace.namespace_id,
                prefix=base_acc.namespace.prefix,
                uri=base_acc.namespace.uri
            )

        # Create library info for base ACC from its release
        base_library_info = LibraryInfo(
            library_id=base_manifest.release.library_id,
            name=base_manifest.release.library.name
        )

        # Create release info for base ACC
        base_release_info = ReleaseInfo(
            release_id=base_manifest.release_id,
            release_num=base_manifest.release.release_num,
            state=base_manifest.release.state
        )

        base_acc_info = BaseAccInfo(
            acc_manifest_id=base_manifest.acc_manifest_id,
            acc_id=base_acc.acc_id,
            guid=base_acc.guid,
            den=base_manifest.den,
            object_class_term=base_acc.object_class_term,
            type=base_acc.type,
            definition=base_acc.definition,
            definition_source=base_acc.definition_source,
            namespace=base_namespace_info,
            library=base_library_info,
            release=base_release_info
        )

    # Get related components for this ACC if engine is provided
    # ACC has relationships with ASCC (which relates to ASCCP → ACC) and BCC (which relates to BCCP → DT)
    relationships = []
    if engine:
        relationships = _get_relationships_for_acc(manifest.acc_manifest_id)

    return GetAccResponse(
        acc_manifest_id=manifest.acc_manifest_id,
        acc_id=acc.acc_id,
        base_acc=base_acc_info,
        relationships=relationships,
        guid=acc.guid,
        den=manifest.den,
        object_class_term=acc.object_class_term,
        definition=acc.definition,
        definition_source=acc.definition_source,
        object_class_qualifier=acc.object_class_qualifier,
        component_type=acc.oagis_component_type,
        is_abstract=acc.is_abstract,
        is_deprecated=acc.is_deprecated,
        state=acc.state,
        namespace=namespace_info,
        library=library_info,
        release=release_info,
        log=log_info,
        owner=_create_user_info(acc.owner),
        created=WhoAndWhen(
            who=_create_user_info(acc.creator),
            when=acc.creation_timestamp
        ),
        last_updated=WhoAndWhen(
            who=_create_user_info(acc.last_updater),
            when=acc.last_update_timestamp
        )
    )


@mcp.tool(
    name="get_asccp",
    description="Get a specific ASCCP by its manifest ID",
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
    asccp_manifest_id: Annotated[int, Field(
        description="Unique numeric identifier of the ASCCP manifest to retrieve.",
        examples=[123, 456, 789],
        gt=0,
        title="ASCCP Manifest ID"
    )]
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Get ASCCP
    try:
        service = CoreComponentService()
        manifest = service.get_asccp_by_manifest_id(asccp_manifest_id)

        return _create_asccp_result(manifest.asccp, manifest)
    except HTTPException as e:
        logger.error(f"HTTP error retrieving ASCCP: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The ASCCP manifest with ID {asccp_manifest_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving ASCCP: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the ASCCP: {str(e)}. Please contact your system administrator.") from e


def _create_asccp_result(asccp, manifest) -> GetAsccpResponse:
    """
    Create an ASCCP result from an Asccp model instance.
    
    Args:
        asccp: Asccp model instance
        manifest: AsccpManifest model instance (required)
        
    Returns:
        GetAsccpResponse: Formatted ASCCP result
    """
    # Create namespace info if available
    namespace_info = None
    if asccp.namespace:
        namespace_info = NamespaceInfo(
            namespace_id=asccp.namespace.namespace_id,
            prefix=asccp.namespace.prefix,
            uri=asccp.namespace.uri
        )

    # Create library info from release
    library_info = LibraryInfo(
        library_id=manifest.release.library_id,
        name=manifest.release.library.name
    )

    # Create release info from manifest
    release_info = ReleaseInfo(
        release_id=manifest.release_id,
        release_num=manifest.release.release_num,
        state=manifest.release.state
    )

    # Create log info from manifest
    log_info = None
    if manifest.log:
        log_info = LogInfo(
            log_id=manifest.log.log_id,
            revision_num=manifest.log.revision_num,
            revision_tracking_num=manifest.log.revision_tracking_num
        )

    # Create role of ACC info (required)
    if not manifest.role_of_acc_manifest:
        raise ValueError("ASCCP must have an associated role_of_acc_manifest")
    
    role_manifest = manifest.role_of_acc_manifest
    role_acc = role_manifest.acc
    
    # Create namespace info for role ACC if available
    role_namespace_info = None
    if role_acc.namespace:
        role_namespace_info = NamespaceInfo(
            namespace_id=role_acc.namespace.namespace_id,
            prefix=role_acc.namespace.prefix,
            uri=role_acc.namespace.uri
        )

    # Create library info for role ACC from its release
    role_library_info = LibraryInfo(
        library_id=role_manifest.release.library_id,
        name=role_manifest.release.library.name
    )

    # Create release info for role ACC
    role_release_info = ReleaseInfo(
        release_id=role_manifest.release_id,
        release_num=role_manifest.release.release_num,
        state=role_manifest.release.state
    )

    role_of_acc_info = BaseAccInfo(
        acc_manifest_id=role_manifest.acc_manifest_id,
        acc_id=role_acc.acc_id,
        guid=role_acc.guid,
        den=role_manifest.den,
        object_class_term=role_acc.object_class_term,
        type=role_acc.type,
        definition=role_acc.definition,
        definition_source=role_acc.definition_source,
        namespace=role_namespace_info,
        library=role_library_info,
        release=role_release_info
    )

    return GetAsccpResponse(
        asccp_manifest_id=manifest.asccp_manifest_id,
        asccp_id=asccp.asccp_id,
        role_of_acc=role_of_acc_info,
        guid=asccp.guid,
        den=manifest.den,
        property_term=asccp.property_term,
        definition=asccp.definition,
        definition_source=asccp.definition_source,
        reusable_indicator=asccp.reusable_indicator,
        is_nillable=asccp.is_nillable,
        is_deprecated=asccp.is_deprecated,
        state=asccp.state,
        namespace=namespace_info,
        library=library_info,
        release=release_info,
        log=log_info,
        owner=_create_user_info(asccp.owner),
        created=WhoAndWhen(
            who=_create_user_info(asccp.creator),
            when=asccp.creation_timestamp
        ),
        last_updated=WhoAndWhen(
            who=_create_user_info(asccp.last_updater),
            when=asccp.last_update_timestamp
        )
    )


@mcp.tool(
    name="get_bccp",
    description="Get a specific BCCP by its manifest ID",
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
    bccp_manifest_id: Annotated[int, Field(
        description="Unique numeric identifier of the BCCP manifest to retrieve.",
        examples=[123, 456, 789],
        gt=0,
        title="BCCP Manifest ID"
    )]
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Get BCCP
    try:
        service = CoreComponentService()
        manifest = service.get_bccp_by_manifest_id(bccp_manifest_id)

        return _create_bccp_result(manifest.bccp, manifest)
    except HTTPException as e:
        logger.error(f"HTTP error retrieving BCCP: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 404:
            raise ToolError(
                f"The BCCP manifest with ID {bccp_manifest_id} was not found. Please check the ID and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving BCCP: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the BCCP: {str(e)}. Please contact your system administrator.") from e


def _create_bccp_result(bccp, manifest) -> GetBccpResponse:
    """
    Create a BCCP result from a Bccp model instance.
    
    Args:
        bccp: Bccp model instance
        manifest: BccpManifest model instance (required)
        
    Returns:
        GetBccpResponse: Formatted BCCP result
    """
    # Create namespace info if available
    namespace_info = None
    if bccp.namespace:
        namespace_info = NamespaceInfo(
            namespace_id=bccp.namespace.namespace_id,
            prefix=bccp.namespace.prefix,
            uri=bccp.namespace.uri
        )

    # Create library info from release
    library_info = LibraryInfo(
        library_id=manifest.release.library_id,
        name=manifest.release.library.name
    )

    # Create release info from manifest
    release_info = ReleaseInfo(
        release_id=manifest.release_id,
        release_num=manifest.release.release_num,
        state=manifest.release.state
    )

    # Create log info from manifest
    log_info = None
    if manifest.log:
        log_info = LogInfo(
            log_id=manifest.log.log_id,
            revision_num=manifest.log.revision_num,
            revision_tracking_num=manifest.log.revision_tracking_num
        )

    # Create BDT info (required)
    if not manifest.bdt_manifest:
        raise ValueError("BCCP must have an associated BDT manifest")
    
    bdt_manifest = manifest.bdt_manifest
    bdt = bdt_manifest.dt
    
    # Create namespace info for BDT if available
    bdt_namespace_info = None
    if bdt.namespace:
        bdt_namespace_info = NamespaceInfo(
            namespace_id=bdt.namespace.namespace_id,
            prefix=bdt.namespace.prefix,
            uri=bdt.namespace.uri
        )

    # Create library info for BDT from its release
    bdt_library_info = LibraryInfo(
        library_id=bdt_manifest.release.library_id,
        name=bdt_manifest.release.library.name
    )

    # Create release info for BDT
    bdt_release_info = ReleaseInfo(
        release_id=bdt_manifest.release_id,
        release_num=bdt_manifest.release.release_num,
        state=bdt_manifest.release.state
    )

    bdt_info = BaseDtInfo(
        dt_manifest_id=bdt_manifest.dt_manifest_id,
        dt_id=bdt.dt_id,
        guid=bdt.guid,
        den=bdt_manifest.den,
        data_type_term=bdt.data_type_term,
        qualifier=bdt.qualifier,
        representation_term=bdt.representation_term,
        six_digit_id=bdt.six_digit_id,
        definition=bdt.definition,
        definition_source=bdt.definition_source,
        content_component_definition=bdt.content_component_definition,
        namespace=bdt_namespace_info,
        library=bdt_library_info,
        release=bdt_release_info
    )

    return GetBccpResponse(
        bccp_manifest_id=manifest.bccp_manifest_id,
        bccp_id=bccp.bccp_id,
        bdt=bdt_info,
        guid=bccp.guid,
        den=manifest.den,
        property_term=bccp.property_term,
        representation_term=bccp.representation_term,
        definition=bccp.definition,
        definition_source=bccp.definition_source,
        is_nillable=bccp.is_nillable,
        value_constraint=validate_and_create_value_constraint(
            default_value=bccp.default_value,
            fixed_value=bccp.fixed_value
        ),
        is_deprecated=bccp.is_deprecated,
        state=bccp.state,
        namespace=namespace_info,
        library=library_info,
        release=release_info,
        log=log_info,
        owner=_create_user_info(bccp.owner),
        created=WhoAndWhen(
            who=_create_user_info(bccp.creator),
            when=bccp.creation_timestamp
        ),
        last_updated=WhoAndWhen(
            who=_create_user_info(bccp.last_updater),
            when=bccp.last_update_timestamp
        )
    )


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
    release_id: Annotated[int, Field(
        description="Filter by release ID using exact match. Use 'get_releases' to find a valid release ID.",
        examples=[123, 456, 789],
        gt=0,
        title="Release ID"
    )],
    types: Annotated[str | None, Field(
        description="Filter by core component types. Comma-separated list of allowed values: 'ACC' (Aggregation Core Component), 'ASCCP' (Association Core Component Property), 'BCCP' (Basic Core Component Property). Examples: 'ASCCP', 'ACC,BCCP', 'ASCCP,ACC,BCCP'. Defaults to 'ASCCP' if not specified.",
        examples=["ASCCP", "ACC,BCCP", "ASCCP,ACC,BCCP", ""],
        title="Component Types"
    )] = None,
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
    den: Annotated[str | None, Field(
        description="Filter by Dictionary Entry Name (DEN) using partial match (case-insensitive).",
        examples=["Amount", "Person", "Address"],
        title="Dictionary Entry Name"
    )] = None,
    tag: Annotated[str | None, Field(
        description="Filter by tag name using partial match (case-insensitive).",
        examples=["BOD", "Noun", "Verb"],
        title="Tag Name"
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
        description="Comma-separated list of properties to order results by. Prefix with '-' for descending, '+' for ascending (default ascending). Allowed columns: den, name, definition, creation_timestamp, last_update_timestamp. Example: '-creation_timestamp,+den' translates to 'creation_timestamp DESC, den ASC'.",
        examples=["-creation_timestamp,+den", "name", "-last_update_timestamp"],
        title="Order By"
    )] = None
) -> GetCoreComponentsResponse:
    """
    Get a paginated list of core components (ACC, ASCCP, BCCP) with unified response format.
    
    Args:
        release_id (int): Filter by release ID using exact match.
        types (str | None, optional): Filter by core component types. Comma-separated list of allowed values: 'ACC', 'ASCCP', 'BCCP'. 
            Examples: 'ASCCP', 'ACC,BCCP', 'ASCCP,ACC,BCCP'. Defaults to 'ASCCP' if not specified.
        offset (int | None, optional): The offset from the beginning of the list. Must be a non-negative number. Defaults to 0.
        limit (int | None, optional): The maximum number of items to return. Must be a non-negative number. Defaults to 10.
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
    
    Returns:
        GetCoreComponentsResponse: Response object containing:
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
    # Validate authentication and database connection
    app_user, engine = _validate_auth_and_db()

    # Create service instance
    core_component_service = CoreComponentService()

    # Parse types parameter and set default to ASCCP if not specified
    if types is None:
        types_list = ["ASCCP"]
    else:
        # Parse comma-separated string into list
        types_list = [t.strip().upper() for t in types.split(',') if t.strip()]
        
        # If empty string results in empty list, treat as all types
        if not types_list:
            types_list = ["ASCCP"]
        else:
            # Validate that all types are valid
            valid_types = {"ACC", "ASCCP", "BCCP"}
            invalid_types = set(types_list) - valid_types
            if invalid_types:
                raise ToolError(
                    f"Invalid component types: {', '.join(invalid_types)}. "
                    f"Valid types are: ACC, ASCCP, BCCP. "
                    f"Use comma-separated format like 'ASCCP,ACC,BCCP'."
                )

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
                f"Valid columns: den, name, definition, creation_timestamp, last_update_timestamp") from e

    # Get core components using UNION query
    try:
        page = core_component_service.get_core_components_by_release(
            release_id=release_id,
            types=types_list,
            den=den,
            tag=tag,
            created_on_params=created_on_params,
            last_updated_on_params=last_updated_on_params,
            pagination=pagination,
            sort_list=sort_list
        )

        # Convert dict items to CoreComponentInfo objects
        core_components = []
        for item in page.items:
            core_component = CoreComponentInfo(
                component_type=item["component_type"],
                manifest_id=item["manifest_id"],
                component_id=item["component_id"],
                guid=item["guid"],
                den=item["den"],
                name=item["name"],
                definition=item["definition"],
                definition_source=item["definition_source"],
                is_deprecated=item["is_deprecated"],
                state=item["state"],
                tag=item["tag"],
                namespace=item["namespace"],
                library=item["library"],
                release=item["release"],
                log=item["log"],
                owner=item["owner"],
                created=item["created"],
                last_updated=item["last_updated"]
            )
            core_components.append(core_component)

        return GetCoreComponentsResponse(
            total_items=page.total,
            offset=page.offset,
            limit=page.limit,
            items=core_components
        )
    except HTTPException as e:
        logger.error(f"HTTP error retrieving core components: {e}")
        if e.status_code == 400:
            raise ToolError(f"Validation error: {e.detail}. Please check your input and try again.") from e
        elif e.status_code == 500:
            raise ToolError(
                f"Database error: {e.detail}. Please try again later or contact your system administrator.") from e
        else:
            raise ToolError(f"Unexpected error: {e.detail}") from e
    except Exception as e:
        logger.error(f"Unexpected error retrieving core components: {e}")
        raise ToolError(
            f"An unexpected error occurred while retrieving the core components: {str(e)}. Please contact your system administrator.") from e


def _create_unified_core_component_result(component_data, component_type: str) -> CoreComponentInfo:
    """
    Create a unified core component result from any core component type.
    
    Args:
        component_data: The component data (ACC, ASCCP, or BCCP response)
        component_type: The type of component ("ACC", "ASCCP", or "BCCP")
        
    Returns:
        CoreComponentInfo: Unified core component result
    """
    # Extract common fields based on component type
    if component_type == "ACC":
        return CoreComponentInfo(
            component_type="ACC",
            manifest_id=component_data.acc_manifest_id,
            component_id=component_data.acc_id,
            guid=component_data.guid,
            den=component_data.den,
            name=component_data.object_class_term,
            definition=component_data.definition,
            definition_source=component_data.definition_source,
            is_deprecated=component_data.is_deprecated,
            state=component_data.state,
            namespace=component_data.namespace,
            library=component_data.library,
            release=component_data.release,
            log=component_data.log,
            owner=component_data.owner,
            created=component_data.created,
            last_updated=component_data.last_updated
        )
    elif component_type == "ASCCP":
        return CoreComponentInfo(
            component_type="ASCCP",
            manifest_id=component_data.asccp_manifest_id,
            component_id=component_data.asccp_id,
            guid=component_data.guid,
            den=component_data.den,
            name=component_data.property_term,
            definition=component_data.definition,
            definition_source=component_data.definition_source,
            is_deprecated=component_data.is_deprecated,
            state=component_data.state,
            namespace=component_data.namespace,
            library=component_data.library,
            release=component_data.release,
            log=component_data.log,
            owner=component_data.owner,
            created=component_data.created,
            last_updated=component_data.last_updated
        )
    else:  # BCCP
        return CoreComponentInfo(
            component_type="BCCP",
            manifest_id=component_data.bccp_manifest_id,
            component_id=component_data.bccp_id,
            guid=component_data.guid,
            den=component_data.den,
            name=component_data.property_term,
            definition=component_data.definition,
            definition_source=component_data.definition_source,
            is_deprecated=component_data.is_deprecated,
            state=component_data.state,
            namespace=component_data.namespace,
            library=component_data.library,
            release=component_data.release,
            log=component_data.log,
            owner=component_data.owner,
            created=component_data.created,
            last_updated=component_data.last_updated
        )
