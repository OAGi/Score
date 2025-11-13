"""Models for Data Type tools."""
from pydantic import BaseModel

from tools.models.common import LibraryInfo, LogInfo, NamespaceInfo, ReleaseInfo, UserInfo, WhoAndWhen
from tools.models.core_component import ValueConstraint


class DataTypeSupplementaryComponentInfo(BaseModel):
    """Data type supplementary component information object."""
    dt_sc_manifest_id: int  # Unique identifier for the data type supplementary component manifest (release-specific version)
    dt_sc_id: int  # Unique identifier for the data type supplementary component (base entity ID)
    guid: str  # Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
    object_class_term: str | None  # Object class term as specified in CCTS v3 (part of the supplementary component name)
    property_term: str | None  # Property term as specified in CCTS v3 (part of the supplementary component name)
    representation_term: str | None  # Representation term as specified in CCTS v3 (part of the supplementary component name)
    definition: str | None  # Definition or description of the supplementary component
    definition_source: str | None  # URL indicating the source of the definition
    cardinality_min: int  # Minimum cardinality (minimum number of occurrences required, typically 0 or 1)
    cardinality_max: int | None  # Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded, None indicates unbounded)
    value_constraint: ValueConstraint | None  # Value constraint (default_value or fixed_value) for the supplementary component
    is_deprecated: bool  # Whether the supplementary component is deprecated and should not be used


class BaseDataTypeInfo(BaseModel):
    """Base data type information object."""
    dt_manifest_id: int  # Unique identifier for the base data type manifest (release-specific version)
    dt_id: int  # Unique identifier for the base data type (base entity ID)
    guid: str  # Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
    den: str  # Dictionary Entry Name (DEN) - the standardized name of the data type as defined by CCTS v3
    data_type_term: str | None  # Data type term as specified in CCTS v3
    qualifier: str | None  # Qualifier that modifies the data type term (if any)
    representation_term: str | None  # Representation term as specified in CCTS v3
    six_digit_id: str | None  # Six-digit identifier used in some data type catalogues for classification
    definition: str | None  # Definition or description of the data type
    definition_source: str | None  # URL indicating the source of the definition
    content_component_definition: str | None  # Definition of the content component part of the data type
    namespace: NamespaceInfo | None  # Namespace information if the data type belongs to a specific namespace
    library: LibraryInfo  # Library information where this data type is stored
    release: ReleaseInfo  # Release information indicating which release this version belongs to


class GetDataTypeResponse(BaseModel):
    """Response for get_data_type tool."""
    dt_manifest_id: int  # Unique identifier for the data type manifest (release-specific version)
    dt_id: int  # Unique identifier for the data type (base entity ID)
    guid: str  # Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
    den: str  # Dictionary Entry Name (DEN) - the standardized name of the data type as defined by CCTS v3
    data_type_term: str | None  # Data type term as specified in CCTS v3
    qualifier: str | None  # Qualifier that modifies the data type term (if any)
    representation_term: str | None  # Representation term as specified in CCTS v3
    six_digit_id: str | None  # Six-digit identifier used in some data type catalogues for classification
    definition: str | None  # Definition or description of the data type
    definition_source: str | None  # URL indicating the source of the definition
    content_component_definition: str | None  # Definition of the content component part of the data type
    namespace: NamespaceInfo | None  # Namespace information if the data type belongs to a specific namespace
    library: LibraryInfo  # Library information where this data type is stored
    release: ReleaseInfo  # Release information indicating which release this version belongs to
    log: LogInfo | None  # Log information tracking revision history (if available)
    commonly_used: bool  # Whether this data type is commonly used across the system
    is_deprecated: bool  # Whether the data type is deprecated and should not be used
    state: str | None  # Current state of the data type (e.g., "Published", "Draft", "WIP")
    base_dt: BaseDataTypeInfo | None  # Base data type information if this data type is derived from another
    supplementary_components: list[DataTypeSupplementaryComponentInfo]  # List of supplementary components associated with this data type
    owner: UserInfo  # User information about the owner of the data type
    created: WhoAndWhen  # Information about who created the data type and when
    last_updated: WhoAndWhen  # Information about who last updated the data type and when


class GetDataTypesResponse(BaseModel):
    """Response for get_data_types tool."""
    total_items: int  # Total number of data types available
    offset: int  # Offset of the first item in this page
    limit: int  # Number of items returned in this page
    items: list[GetDataTypeResponse]  # List of data types on this page

