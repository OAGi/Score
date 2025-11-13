"""Models for XBT (XML Built-in Type) tools.

XBTs (XML Built-in Types) are fundamental XML Schema Definition (XSD) data types that serve as
the foundation for data type definitions in Core Components. They represent primitive data types
such as string, integer, decimal, date, boolean, and their specialized subtypes.

XBTs form a type hierarchy through subtype relationships. For example:
- anyType (root type)
  - anySimpleType (subtype of anyType)
    - string (subtype of anySimpleType)
      - normalizedString (subtype of string)
        - token (subtype of normalizedString)
          - language (subtype of token)
    - decimal (subtype of anySimpleType)
      - integer (subtype of decimal)
        - nonNegativeInteger (subtype of integer)
          - positiveInteger (subtype of nonNegativeInteger)
    - dateTime, date, time, duration, boolean, float, double, etc. (all subtypes of anySimpleType)

Each XBT includes mappings to other data representation formats (JSON Schema Draft 05, OpenAPI 3.0, Avro)
to support interoperability across different systems and standards.
"""
from pydantic import BaseModel

from tools.models.common import LibraryInfo, LogInfo, ReleaseInfo, UserInfo, WhoAndWhen


class SubtypeOfXbtInfo(BaseModel):
    """Subtype of XBT information object.
    
    Represents the parent XBT in the type hierarchy. XBTs can be subtypes of other XBTs,
    forming a specialization hierarchy. For example, 'normalizedString' is a subtype of 'string',
    and 'integer' is a subtype of 'decimal'. The root type is typically 'anyType', with 'anySimpleType'
    as its direct subtype.
    
    Attributes:
        xbt_manifest_id: Unique identifier for the subtype XBT manifest (release-specific version)
        xbt_id: Unique identifier for the subtype XBT (base entity ID, same across all releases)
        guid: Globally unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
        name: Human-readable name of the built-in type (e.g., "string", "date time", "boolean")
        builtIn_type: Built-in type as it should appear in XML schema with namespace prefix (e.g., "xsd:string", "xsd:dateTime")
        library: Library information where this XBT is stored
        release: Release information indicating which release this version belongs to
    """
    xbt_manifest_id: int  # Unique identifier for the subtype XBT manifest (release-specific version)
    xbt_id: int  # Unique identifier for the subtype XBT (base entity ID, same across all releases)
    guid: str  # Globally unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
    name: str | None  # Human-readable name of the built-in type (e.g., "string", "date time", "boolean")
    builtIn_type: str | None  # Built-in type as it should appear in XML schema with namespace prefix (e.g., "xsd:string", "xsd:dateTime")
    library: LibraryInfo  # Library information where this XBT is stored
    release: ReleaseInfo  # Release information indicating which release this version belongs to


class GetXbtResponse(BaseModel):
    """Response for get_xbt tool.
    
    Contains complete information about an XBT (XML Built-in Type), including its type hierarchy
    relationships, data format mappings, and metadata.
    
    Attributes:
        xbt_manifest_id: Unique identifier for the XBT manifest (release-specific version)
        xbt_id: Unique identifier for the XBT (base entity ID, same across all releases)
        guid: Globally unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
        name: Human-readable name of the built-in type (e.g., "string", "date time", "boolean", "normalized string")
        builtIn_type: Built-in type as it should appear in XML schema with namespace prefix (e.g., "xsd:string", "xsd:dateTime", "xsd:normalizedString")
        jbt_draft05_map: JSON Schema Draft 05 mapping as a JSON string (e.g., '{"type":"string"}', '{"type":"string", "format":"date-time"}')
        openapi30_map: OpenAPI 3.0 specification mapping as a JSON string (e.g., '{"type":"string", "format":"date-time"}')
        avro_map: Apache Avro schema mapping as a JSON string (e.g., '{"type":"string"}', '{"type":"int"}')
        subtype_of_xbt: Information about the parent XBT in the type hierarchy, if this XBT is a subtype of another (None for root types like anyType)
        schema_definition: XML Schema Definition (XSD) schema definition string, if custom schema is defined (typically None for built-in types)
        revision_doc: Revision documentation describing changes or updates to this XBT (typically None for standard built-in types)
        state: State of the XBT (e.g., 3 = Published). Indicates the lifecycle state of the XBT.
        is_deprecated: Whether the XBT is deprecated and should not be used in new implementations
        library: Library information where this XBT is stored
        release: Release information indicating which release this version belongs to
        log: Log information tracking revision history (if available)
        owner: User information about the owner of the XBT
        created: Information about who created the XBT and when
        last_updated: Information about who last updated the XBT and when
    """
    xbt_manifest_id: int  # Unique identifier for the XBT manifest (release-specific version)
    xbt_id: int  # Unique identifier for the XBT (base entity ID, same across all releases)
    guid: str  # Globally unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
    name: str | None  # Human-readable name of the built-in type (e.g., "string", "date time", "boolean", "normalized string")
    builtIn_type: str | None  # Built-in type as it should appear in XML schema with namespace prefix (e.g., "xsd:string", "xsd:dateTime", "xsd:normalizedString")
    jbt_draft05_map: str | None  # JSON Schema Draft 05 mapping as a JSON string (e.g., '{"type":"string"}', '{"type":"string", "format":"date-time"}')
    openapi30_map: str | None  # OpenAPI 3.0 specification mapping as a JSON string (e.g., '{"type":"string", "format":"date-time"}')
    avro_map: str | None  # Apache Avro schema mapping as a JSON string (e.g., '{"type":"string"}', '{"type":"int"}')
    subtype_of_xbt: SubtypeOfXbtInfo | None  # Information about the parent XBT in the type hierarchy, if this XBT is a subtype of another (None for root types like anyType)
    schema_definition: str | None  # XML Schema Definition (XSD) schema definition string, if custom schema is defined (typically None for built-in types)
    revision_doc: str | None  # Revision documentation describing changes or updates to this XBT (typically None for standard built-in types)
    state: int | None  # State of the XBT (e.g., 3 = Published). Indicates the lifecycle state of the XBT.
    is_deprecated: bool  # Whether the XBT is deprecated and should not be used in new implementations
    library: LibraryInfo  # Library information where this XBT is stored
    release: ReleaseInfo  # Release information indicating which release this version belongs to
    log: LogInfo | None  # Log information tracking revision history (if available)
    owner: UserInfo  # User information about the owner of the XBT
    created: WhoAndWhen  # Information about who created the XBT and when
    last_updated: WhoAndWhen  # Information about who last updated the XBT and when

