"""Models for Core Component tools."""

from typing import Literal, List, Optional, Union

from pydantic import BaseModel, computed_field, model_validator

from tools.models.common import LibraryInfo, LogInfo, NamespaceInfo, ReleaseInfo, UserInfo, WhoAndWhen


class ValueConstraint(BaseModel):
    """Value constraint information for components.
    
    Validation rules:
    - Can be None (optional)
    - If not None, exactly one of default_value or fixed_value must be set (not both, not neither)
    """
    default_value: str | None  # Default value for the component
    fixed_value: str | None  # Fixed value for the component
    
    @model_validator(mode='after')
    def validate_value_constraint(self):
        """Validate that exactly one of default_value or fixed_value is set."""
        has_default = self.default_value is not None
        has_fixed = self.fixed_value is not None
        
        if has_default and has_fixed:
            raise ValueError(
                "ValueConstraint: Both default_value and fixed_value cannot be set. "
                "Exactly one must be set, the other must be None."
            )
        if not has_default and not has_fixed:
            raise ValueError(
                "ValueConstraint: Either default_value or fixed_value must be set. "
                "Both cannot be None."
            )
        
        return self


class AsccpInfo(BaseModel):
    """ASCCP (Association Core Component Property) information object."""
    asccp_manifest_id: int  # Unique identifier for the ASCCP manifest (release-specific version)
    asccp_id: int  # Unique identifier for the ASCCP (base entity ID)
    role_of_acc_manifest_id: int  # Unique identifier for the ACC manifest that this ASCCP plays the role of
    guid: str  # Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
    den: str  # Dictionary Entry Name (DEN) - the standardized name as defined by CCTS v3
    property_term: str  # Property term as specified in CCTS v3, part of the component name
    definition: str | None  # Definition or description of the ASCCP
    definition_source: str | None  # URL indicating the source of the definition
    is_deprecated: bool  # Whether the ASCCP is deprecated and should not be used


class BccpInfo(BaseModel):
    """BCCP (Basic Core Component Property) information object."""
    bccp_manifest_id: int  # Unique identifier for the BCCP manifest (release-specific version)
    bccp_id: int  # Unique identifier for the BCCP (base entity ID)
    guid: str  # Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
    den: str  # Dictionary Entry Name (DEN) - the standardized name as defined by CCTS v3
    property_term: str  # Property term as specified in CCTS v3, part of the component name
    representation_term: str  # Representation term as specified in CCTS v3, indicates the data format
    definition: str | None  # Definition or description of the BCCP
    definition_source: str | None  # URL indicating the source of the definition
    bdt_manifest: "DtInfo"  # Basic Data Type (BDT) information associated with this BCCP
    is_deprecated: bool  # Whether the BCCP is deprecated and should not be used


class DtInfo(BaseModel):
    """DT (Data Type) information object."""
    dt_manifest_id: int  # Unique identifier for the data type manifest (release-specific version)
    dt_id: int  # Unique identifier for the data type (base entity ID)
    guid: str  # Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
    den: str  # Dictionary Entry Name (DEN) - the standardized name as defined by CCTS v3
    data_type_term: str  # Data type term as specified in CCTS v3
    qualifier: str | None  # Qualifier that modifies the data type term (if any)
    representation_term: str | None  # Representation term as specified in CCTS v3, indicates the data format
    six_digit_id: str | None  # Six-digit identifier used in some data type catalogues for classification
    based_dt_manifest_id: int  # Unique identifier for the base data type manifest this DT is derived from (if any)
    definition: str | None  # Definition or description of the data type
    definition_source: str | None  # URL indicating the source of the definition
    is_deprecated: bool  # Whether the data type is deprecated and should not be used


class AccInfo(BaseModel):
    """ACC (Aggregation Core Component) information object."""
    acc_manifest_id: int  # Unique identifier for the ACC manifest (release-specific version)
    acc_id: int  # Unique identifier for the ACC (base entity ID)
    guid: str  # Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
    den: str  # Dictionary Entry Name (DEN) - the standardized name as defined by CCTS v3
    object_class_term: str  # Object class term as specified in CCTS v3, part of the component name
    definition: str | None  # Definition or description of the ACC
    definition_source: str | None  # URL indicating the source of the definition
    is_deprecated: bool  # Whether the ACC is deprecated and should not be used


class AsccInfo(BaseModel):
    """ASCC (Association Core Component) information object."""
    ascc_manifest_id: int  # Unique identifier for the ASCC manifest (release-specific version)
    ascc_id: int  # Unique identifier for the ASCC (base entity ID)
    guid: str  # Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
    den: str  # Dictionary Entry Name (DEN) - the standardized name as defined by CCTS v3
    cardinality_min: int  # Minimum cardinality (minimum number of occurrences required, typically 0 or 1)
    cardinality_max: int  # Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)
    is_deprecated: bool  # Whether the ASCC is deprecated and should not be used
    definition: str | None  # Definition or description of the ASCC
    definition_source: str | None  # URL indicating the source of the definition
    from_acc_manifest_id: int  # Unique identifier for the source ACC manifest (the ACC that contains this ASCC)
    to_asccp_manifest_id: int  # Unique identifier for the target ASCCP manifest (the ASCCP this ASCC connects to)


class BccInfo(BaseModel):
    """BCC (Basic Core Component) information object."""
    bcc_manifest_id: int  # Unique identifier for the BCC manifest (release-specific version)
    bcc_id: int  # Unique identifier for the BCC (base entity ID)
    guid: str  # Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
    den: str  # Dictionary Entry Name (DEN) - the standardized name as defined by CCTS v3
    cardinality_min: int  # Minimum cardinality (minimum number of occurrences required, typically 0 or 1)
    cardinality_max: int  # Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)
    entity_type: str | None  # Entity type: "Attribute" (XML attribute) or "Element" (XML element)
    is_nillable: bool  # Whether the BCC can have a nil/null value
    is_deprecated: bool  # Whether the BCC is deprecated and should not be used
    definition: str | None  # Definition or description of the BCC
    definition_source: str | None  # URL indicating the source of the definition
    from_acc_manifest_id: int  # Unique identifier for the source ACC manifest (the ACC that contains this BCC)
    to_bccp_manifest_id: int  # Unique identifier for the target BCCP manifest (the BCCP this BCC connects to)


class DtScInfo(BaseModel):
    """DT_SC (Data Type Supplementary Component) information object."""
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
    default_value: str | None  # Default value for the supplementary component
    fixed_value: str | None  # Fixed value for the supplementary component
    is_deprecated: bool  # Whether the supplementary component is deprecated and should not be used


class CoreComponentRelationshipInfo(BaseModel):
    """Base related component information object."""
    component_type: Literal["ASCC", "BCC"]  # Type of related component: "ASCC" (Association) or "BCC" (Basic)
    guid: str  # Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
    den: str  # Dictionary Entry Name (DEN) - the standardized name as defined by CCTS v3
    cardinality_min: int  # Minimum cardinality (minimum number of occurrences required, typically 0 or 1)
    cardinality_max: int  # Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)
    is_deprecated: bool  # Whether the component is deprecated and should not be used
    definition: str | None  # Definition or description of the component
    definition_source: str | None  # URL indicating the source of the definition
    from_acc: AccInfo  # Information about the ACC that contains this related component

    @computed_field
    @property
    def cardinality_display(self) -> str:
        """Display cardinality with 'unbounded' for -1 values."""
        if self.cardinality_max == -1:
            return f"{self.cardinality_min}..unbounded"
        else:
            return f"{self.cardinality_min}..{self.cardinality_max}"


class AsccRelationshipInfo(CoreComponentRelationshipInfo):
    """ASCC related component information object."""
    component_type: Literal["ASCC"] = "ASCC"  # Type of related component, always "ASCC" for this class
    ascc_manifest_id: int  # Unique identifier for the ASCC manifest (release-specific version)
    ascc_id: int  # Unique identifier for the ASCC (base entity ID)
    to_asccp: AsccpInfo  # Information about the ASCCP that this ASCC connects to

    @computed_field
    @property
    def manifest_id(self) -> int:
        return self.ascc_manifest_id


class BccRelationshipInfo(CoreComponentRelationshipInfo):
    """BCC related component information object."""
    component_type: Literal["BCC"] = "BCC"  # Type of related component, always "BCC" for this class
    bcc_manifest_id: int  # Unique identifier for the BCC manifest (release-specific version)
    bcc_id: int  # Unique identifier for the BCC (base entity ID)
    entity_type: Optional[Literal["Attribute", "Element"]] = None  # Entity type: "Attribute" (XML attribute) or "Element" (XML element)
    is_nillable: bool  # Whether the BCC can have a nil/null value
    value_constraint: ValueConstraint | None  # Value constraint (default_value or fixed_value) for the BCC
    to_bccp: BccpInfo  # Information about the BCCP that this BCC connects to

    @computed_field
    @property
    def manifest_id(self) -> int:
        return self.bcc_manifest_id


class GetRelatedComponentsResponse(BaseModel):
    """Response for get_related_components tool."""
    acc_manifest_id: int  # Unique identifier for the ACC manifest whose related components are returned
    related_components: List[Union[AsccRelationshipInfo, BccRelationshipInfo]]  # List of related components (ASCCs and BCCs) contained in the ACC
    total_count: int  # Total count of related components for the ACC


class BaseAccInfo(BaseModel):
    """Base ACC information object."""
    acc_manifest_id: int  # Unique identifier for the base ACC manifest (release-specific version)
    acc_id: int  # Unique identifier for the base ACC (base entity ID)
    guid: str  # Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
    den: str  # Dictionary Entry Name (DEN) - the standardized name as defined by CCTS v3
    object_class_term: str  # Object class term as specified in CCTS v3, part of the component name
    type: str  # Type of ACC (e.g., "Default", "Extension", "SemanticGroup")
    definition: str | None  # Definition or description of the ACC
    definition_source: str | None  # URL indicating the source of the definition
    namespace: NamespaceInfo | None  # Namespace information if the ACC belongs to a specific namespace
    library: LibraryInfo  # Library information where this ACC is stored
    release: ReleaseInfo  # Release information indicating which release this version belongs to


class BaseAsccpInfo(BaseModel):
    """Base ASCCP information object."""
    asccp_manifest_id: int  # Unique identifier for the base ASCCP manifest (release-specific version)
    asccp_id: int  # Unique identifier for the base ASCCP (base entity ID)
    guid: str  # Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
    den: str | None  # Dictionary Entry Name (DEN) - the standardized name as defined by CCTS v3
    property_term: str | None  # Property term as specified in CCTS v3, part of the component name
    type: str  # Type of ASCCP (e.g., "Default", "Extension")
    definition: str | None  # Definition or description of the ASCCP
    definition_source: str | None  # URL indicating the source of the definition
    namespace: NamespaceInfo | None  # Namespace information if the ASCCP belongs to a specific namespace
    library: LibraryInfo  # Library information where this ASCCP is stored
    release: ReleaseInfo  # Release information indicating which release this version belongs to


class BaseBccpInfo(BaseModel):
    """Base BCCP information object."""
    bccp_manifest_id: int  # Unique identifier for the base BCCP manifest (release-specific version)
    bccp_id: int  # Unique identifier for the base BCCP (base entity ID)
    guid: str  # Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
    den: str  # Dictionary Entry Name (DEN) - the standardized name as defined by CCTS v3
    property_term: str  # Property term as specified in CCTS v3, part of the component name
    representation_term: str  # Representation term as specified in CCTS v3, indicates the data format
    definition: str | None  # Definition or description of the BCCP
    definition_source: str | None  # URL indicating the source of the definition
    namespace: NamespaceInfo | None  # Namespace information if the BCCP belongs to a specific namespace
    library: LibraryInfo  # Library information where this BCCP is stored
    release: ReleaseInfo  # Release information indicating which release this version belongs to


class BaseDtInfo(BaseModel):
    """Base Data Type information object."""
    dt_manifest_id: int # Unique identifier for the base data type manifest (release-specific version)
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


# ACC Response Models
class GetAccResponse(BaseModel):
    """Response for get_acc tool."""
    acc_manifest_id: int  # Unique identifier for the ACC manifest (release-specific version)
    acc_id: int  # Unique identifier for the ACC (base entity ID)
    base_acc: BaseAccInfo | None  # Base ACC information if this ACC is derived from another
    relationships: List[Union[AsccRelationshipInfo, BccRelationshipInfo]]  # List of related components (ASCCs and BCCs) contained in the ACC
    guid: str  # Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
    den: str  # Dictionary Entry Name (DEN) - following rule: acc.object_class_term + ". Details"
    object_class_term: str  # Object class term as specified in CCTS v3, part of the component name
    definition: str | None  # Definition or description of the ACC
    definition_source: str | None  # URL indicating the source of the definition
    object_class_qualifier: str | None  # Qualifier that modifies the object class term (if any)
    component_type: int | None  # Numeric component type identifier (e.g., 0=Default, 3=SemanticGroup, 4=UserExtensionGroup)
    is_abstract: bool  # Whether the ACC is abstract (cannot be instantiated directly)
    is_deprecated: bool  # Whether the ACC is deprecated and should not be used
    state: str | None  # Current state of the ACC (e.g., "Published", "Draft", "WIP", "QA", "Candidate", "Production")
    namespace: NamespaceInfo | None  # Namespace information if the ACC belongs to a specific namespace
    library: LibraryInfo  # Library information where this ACC is stored
    release: ReleaseInfo  # Release information indicating which release this version belongs to
    log: LogInfo | None  # Log information tracking revision history (if available)
    owner: UserInfo  # User information about the owner of the ACC
    created: WhoAndWhen  # Information about who created the ACC and when
    last_updated: WhoAndWhen  # Information about who last updated the ACC and when


# ASCCP Response Models
class GetAsccpResponse(BaseModel):
    """Response for get_asccp tool."""
    asccp_manifest_id: int  # Unique identifier for the ASCCP manifest (release-specific version)
    asccp_id: int  # Unique identifier for the ASCCP (base entity ID)
    role_of_acc: BaseAccInfo  # Information about the ACC that this ASCCP plays the role of (required)
    guid: str  # Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
    den: str | None  # Dictionary Entry Name (DEN) - following rule: asccp.property_term + ". " + asccp.role_of_acc.object_class_term
    property_term: str | None  # Property term as specified in CCTS v3, part of the component name
    definition: str | None  # Definition or description of the ASCCP
    definition_source: str | None  # URL indicating the source of the definition
    reusable_indicator: bool  # Whether the ASCCP can be reused in multiple contexts
    is_nillable: bool | None  # Whether the ASCCP can have a nil/null value
    is_deprecated: bool  # Whether the ASCCP is deprecated and should not be used
    state: str | None  # Current state of the ASCCP (e.g., "Published", "Draft", "WIP")
    namespace: NamespaceInfo | None  # Namespace information if the ASCCP belongs to a specific namespace
    library: LibraryInfo  # Library information where this ASCCP is stored
    release: ReleaseInfo  # Release information indicating which release this version belongs to
    log: LogInfo | None  # Log information tracking revision history (if available)
    owner: UserInfo  # User information about the owner of the ASCCP
    created: WhoAndWhen  # Information about who created the ASCCP and when
    last_updated: WhoAndWhen  # Information about who last updated the ASCCP and when


# BCCP Response Models
class GetBccpResponse(BaseModel):
    """Response for get_bccp tool."""
    bccp_manifest_id: int  # Unique identifier for the BCCP manifest (release-specific version)
    bccp_id: int  # Unique identifier for the BCCP (base entity ID)
    bdt: BaseDtInfo  # Basic Data Type (BDT) information associated with this BCCP (required)
    guid: str  # Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
    den: str  # Dictionary Entry Name (DEN) - following rule: bccp.property_term + ". " + dt.den.replace(". Type", "")
    property_term: str  # Property term as specified in CCTS v3, part of the component name
    representation_term: str  # Representation term as specified in CCTS v3, indicates the data format
    definition: str | None  # Definition or description of the BCCP
    definition_source: str | None  # URL indicating the source of the definition
    is_nillable: bool  # Whether the BCCP can have a nil/null value
    value_constraint: ValueConstraint | None  # Value constraint (default_value or fixed_value) for the BCCP
    is_deprecated: bool  # Whether the BCCP is deprecated and should not be used
    state: str | None  # Current state of the BCCP (e.g., "Published", "Draft", "WIP")
    namespace: NamespaceInfo | None  # Namespace information if the BCCP belongs to a specific namespace
    library: LibraryInfo  # Library information where this BCCP is stored
    release: ReleaseInfo  # Release information indicating which release this version belongs to
    log: LogInfo | None  # Log information tracking revision history (if available)
    owner: UserInfo  # User information about the owner of the BCCP
    created: WhoAndWhen  # Information about who created the BCCP and when
    last_updated: WhoAndWhen  # Information about who last updated the BCCP and when


# Unified Core Component Response Models
class CoreComponentInfo(BaseModel):
    """Unified core component information object."""
    component_type: Literal["ACC", "ASCCP", "BCCP"]  # Type of component: "ACC" (Aggregation), "ASCCP" (Association Property), or "BCCP" (Basic Property)
    manifest_id: int  # Unique identifier for the component manifest (release-specific version)
    component_id: int  # Unique identifier for the component (base entity ID)
    guid: str  # Unique identifier within the release. 32-character hexadecimal identifier (lowercase, no hyphens)
    den: str | None  # Dictionary Entry Name (DEN) - the standardized name as defined by CCTS v3
    name: str | None  # Component name: object_class_term for ACC, property_term for ASCCP/BCCP
    definition: str | None  # Definition or description of the component
    definition_source: str | None  # URL indicating the source of the definition
    is_deprecated: bool  # Whether the component is deprecated and should not be used
    state: str | None  # Current state of the component (e.g., "Published", "Draft", "WIP", "QA", "Candidate", "Production")
    tag: str | None  # Tag name associated with the component (e.g., "BOD" for Business Object Document)
    namespace: NamespaceInfo | None  # Namespace information if the component belongs to a specific namespace
    library: LibraryInfo  # Library information where this component is stored
    release: ReleaseInfo  # Release information indicating which release this version belongs to
    log: LogInfo | None  # Log information tracking revision history (if available)
    owner: UserInfo  # User information about the owner of the component
    created: WhoAndWhen  # Information about who created the component and when
    last_updated: WhoAndWhen  # Information about who last updated the component and when


class GetCoreComponentsResponse(BaseModel):
    """Response for get_core_components tool."""
    total_items: int  # Total number of core components available
    offset: int  # Offset of the first item in this page
    limit: int  # Number of items returned in this page
    items: list[CoreComponentInfo]  # List of core components on this page

