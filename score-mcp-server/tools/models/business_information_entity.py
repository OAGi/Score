"""Models for Business Information Entity tools."""

from __future__ import annotations

from typing import Literal, List, Union

from pydantic import BaseModel, computed_field, model_validator

from tools.models.biz_ctx import BusinessContextInfo
from tools.models.core_component import AccInfo, AsccInfo, AsccpInfo, BccInfo, BccpInfo, DtScInfo, ValueConstraint
from tools.models.common import LibraryInfo, ReleaseInfo, UserInfo, WhoAndWhen


# Response classes for each MCP tool
class TopLevelAsbiepInfo(BaseModel):
    """Top-Level ASBIEP information object."""
    top_level_asbiep_id: int  # Unique identifier for the top-level ASBIEP
    library: LibraryInfo  # Library information where this top-level ASBIEP is stored
    release: ReleaseInfo  # Release information indicating which release this version belongs to
    version: str | None  # Version string of the top-level ASBIEP (e.g., "1.0", "2.1")
    status: str | None  # Status of the top-level ASBIEP (e.g., "Production", "Draft")
    state: str  # Current state of the top-level ASBIEP (e.g., "WIP", "QA", "Production", "Published")
    is_deprecated: bool  # Whether the top-level ASBIEP is deprecated and should not be used
    deprecated_reason: str | None  # Reason why the top-level ASBIEP was deprecated
    deprecated_remark: str | None  # Additional remarks about the deprecation
    owner: UserInfo  # User information about the owner of the top-level ASBIEP


class AsbiepInfo(BaseModel):
    """ASBIEP (Association Business Information Entity Property) information object."""
    asbiep_id: int | None  # Unique identifier for the ASBIEP (base entity ID, None if not yet created)
    owner_top_level_asbiep: TopLevelAsbiepInfo | None  # Information about the top-level ASBIEP that owns this ASBIEP
    based_asccp_manifest: AsccpInfo  # Information about the ASCCP that this ASBIEP is based on
    path: str | None  # Hierarchical path string indicating the position of this ASBIEP within the BIE structure
    hash_path: str | None  # Hashed version of the path for efficient lookups
    role_of_abie: "AbieInfo"  # Information about the ABIE that this ASBIEP plays the role of
    definition: str | None  # Definition or description of the ASBIEP
    remark: str | None  # Additional remarks or notes about the ASBIEP
    biz_term: str | None  # Business term that represents this ASBIEP in business language
    display_name: str | None  # Display name intended for user interface presentation
    created: WhoAndWhen | None  # Information about who created the ASBIEP and when (if available)
    last_updated: WhoAndWhen | None  # Information about who last updated the ASBIEP and when (if available)


class GetTopLevelAsbiepResponse(BaseModel):
    """Response for get_top_level_asbiep tool."""
    top_level_asbiep_id: int  # Unique identifier for the top-level ASBIEP
    asbiep: AsbiepInfo  # Detailed information about the ASBIEP associated with this top-level ASBIEP
    version: str | None  # Version string of the top-level ASBIEP (e.g., "1.0", "2.1")
    status: str | None  # Status of the top-level ASBIEP (e.g., "Production", "Draft")
    business_contexts: list[BusinessContextInfo]  # List of business contexts associated with this top-level ASBIEP
    state: str  # Current state of the top-level ASBIEP (e.g., "WIP", "QA", "Production", "Published")
    is_deprecated: bool  # Whether the top-level ASBIEP is deprecated and should not be used
    deprecated_reason: str | None  # Reason why the top-level ASBIEP was deprecated
    deprecated_remark: str | None  # Additional remarks about the deprecation
    owner: UserInfo  # User information about the owner of the top-level ASBIEP
    created: WhoAndWhen  # Information about who created the top-level ASBIEP and when
    last_updated: WhoAndWhen  # Information about who last updated the top-level ASBIEP and when


class GetTopLevelAsbiepListResponseEntry(BaseModel):
    """Response for get_top_level_asbiep tool."""
    top_level_asbiep_id: int  # Unique identifier for the top-level ASBIEP
    asbiep_id: int  # Unique identifier for the ASBIEP (base entity ID)
    guid: str  # Globally unique identifier for the ASBIEP
    den: str  # Dictionary Entry Name (DEN) - the standardized name as defined by CCTS v3
    property_term: str  # Property term from the underlying ASCCP
    display_name: str | None  # Display name intended for user interface presentation
    version: str | None  # Version string of the top-level ASBIEP (e.g., "1.0", "2.1")
    status: str | None  # Status of the top-level ASBIEP (e.g., "Production", "Draft")
    biz_term: str | None  # Business term that represents this top-level ASBIEP in business language
    remark: str | None  # Additional remarks or notes about the top-level ASBIEP
    business_contexts: list[BusinessContextInfo]  # List of business contexts associated with this top-level ASBIEP
    state: str  # Current state of the top-level ASBIEP (e.g., "WIP", "QA", "Production", "Published")
    is_deprecated: bool  # Whether the top-level ASBIEP is deprecated and should not be used
    deprecated_reason: str | None  # Reason why the top-level ASBIEP was deprecated
    deprecated_remark: str | None  # Additional remarks about the deprecation
    owner: UserInfo  # User information about the owner of the top-level ASBIEP
    created: WhoAndWhen  # Information about who created the top-level ASBIEP and when
    last_updated: WhoAndWhen  # Information about who last updated the top-level ASBIEP and when


class GetTopLevelAsbiepListResponse(BaseModel):
    """Response for get_top_level_asbiep_list tool."""
    total_items: int  # Total number of Top-Level ASBIEPs available
    offset: int  # Offset of the first item in this page
    limit: int  # Number of items returned in this page
    items: list[GetTopLevelAsbiepListResponseEntry]  # List of Top-Level ASBIEPs on this page


class CreateTopLevelAsbiepResponse(BaseModel):
    """Response for create_top_level_asbiep tool."""
    top_level_asbiep_id: int  # Unique identifier of the newly created top-level ASBIEP
    asbiep: "CreateAsbiepRelationshipDetail | None" = None  # ASBIEP structure with role_of_abie (excludes remark, is_nillable from relationships)


class DeleteTopLevelAsbiepResponse(BaseModel):
    """Response for delete_top_level_asbiep tool."""
    top_level_asbiep_id: int  # Unique identifier of the deleted top-level ASBIEP


class TransferTopLevelAsbiepOwnershipResponse(BaseModel):
    """Response for transfer_top_level_asbiep_ownership tool."""
    top_level_asbiep_id: int  # Unique identifier of the top-level ASBIEP whose ownership was transferred
    updates: list[str]  # A list of field names that were updated (e.g., ["owner"])


class UpdateTopLevelAsbiepResponse(BaseModel):
    """Response for update_top_level_asbiep tool."""
    top_level_asbiep_id: int  # Unique identifier of the updated top-level ASBIEP
    updates: list[str]  # A list of field names that were updated (e.g., ["version", "status", "remark"])


# Recursive models for mandatory relationships response
class BbieRelationshipDetail(BaseModel):
    """BBIE relationship detail in recursive structure."""
    bbie_id: int
    guid: str | None = None
    path: str
    cardinality_min: int
    cardinality_max: int
    is_nillable: bool
    remark: str | None = None
    based_bcc: BccInfo


class AsbieRelationshipDetail(BaseModel):
    """ASBIE relationship detail in recursive structure."""
    asbie_id: int
    guid: str | None = None
    path: str
    cardinality_min: int
    cardinality_max: int
    is_nillable: bool
    remark: str | None = None
    based_ascc: AsccInfo
    asbiep: "AsbiepRelationshipDetail | None" = None  # Recursive structure


# Simplified models for create responses (exclude fields that won't have values for newly generated records)
class CreateBbieScInfo(BaseModel):
    """BBIE SC info for create_bbie response (excludes definition, remark, default_value, fixed_value, facets)."""
    bbie_sc_id: int | None  # Unique identifier for the BBIE SC (None if not yet created)
    guid: str | None  # Globally unique identifier for the BBIE SC (if available)
    based_dt_sc: DtScInfo  # Information about the data type supplementary component
    path: str  # Hierarchical path string
    hash_path: str  # Hashed version of the path
    cardinality_min: int  # Minimum cardinality
    cardinality_max: int  # Maximum cardinality


class CreateBbiepInfo(BaseModel):
    """BBIEP info for create_bbie response (excludes definition, remark, biz_term, display_name)."""
    bbiep_id: int | None  # Unique identifier for the BBIEP
    guid: str | None  # Globally unique identifier for the BBIEP
    based_bccp: BccpInfo  # Information about the BCCP that this BBIEP is based on
    path: str  # Hierarchical path string
    hash_path: str  # Hashed version of the path
    supplementary_components: list[CreateBbieScInfo]  # List of supplementary components


class CreateAsbieRelationshipDetail(BaseModel):
    """ASBIE relationship detail for create_asbie response (excludes remark, is_nillable)."""
    asbie_id: int
    guid: str | None = None
    path: str
    cardinality_min: int
    cardinality_max: int
    based_ascc: AsccInfo
    asbiep: "CreateAsbiepRelationshipDetail | None" = None  # Recursive structure


class CreateBbieRelationshipDetail(BaseModel):
    """BBIE relationship detail for create_asbie/create_top_level_asbiep response (excludes remark)."""
    bbie_id: int
    guid: str | None = None
    path: str
    cardinality_min: int
    cardinality_max: int
    is_nillable: bool
    based_bcc: BccInfo


class CreateAsbiepRelationshipDetail(BaseModel):
    """ASBIEP relationship detail for create_asbie response."""
    asbiep_id: int
    role_of_abie: "CreateRoleOfAbieDetail | None" = None  # Recursive structure


class CreateRoleOfAbieDetail(BaseModel):
    """Role of ABIE detail for create_asbie response."""
    abie_id: int
    relationships: list["CreateRelationshipDetail"] = []  # Recursive structure


class CreateRelationshipDetail(BaseModel):
    """Relationship detail for create_asbie/create_top_level_asbiep response (can contain either ASBIE or BBIE)."""
    asbie: CreateAsbieRelationshipDetail | None = None
    bbie: CreateBbieRelationshipDetail | None = None  # BBIE relationships exclude remark in create responses


class AsbiepRelationshipDetail(BaseModel):
    """ASBIEP relationship detail in recursive structure."""
    asbiep_id: int
    role_of_abie: "RoleOfAbieDetail | None" = None  # Recursive structure


class RoleOfAbieDetail(BaseModel):
    """Role of ABIE detail in recursive structure."""
    abie_id: int
    relationships: list["RelationshipDetail"] = []  # Recursive structure


class RelationshipDetail(BaseModel):
    """Relationship detail that can contain either ASBIE or BBIE."""
    asbie: AsbieRelationshipDetail | None = None
    bbie: BbieRelationshipDetail | None = None


class CreateAsbieResponse(BaseModel):
    """Response for create_asbie tool."""
    asbie_id: int  # Unique identifier of the newly created ASBIE
    asbiep: "CreateAsbiepRelationshipDetail | None" = None  # Recursive structure containing all created/enabled relationships (excludes remark, is_nillable)


class UpdateAsbieRelationshipDetail(BaseModel):
    """ASBIE relationship detail with updates for update_asbie response."""
    asbie_id: int
    updates: list[str]  # List of fields that were updated on this ASBIE
    guid: str | None = None
    path: str
    cardinality_min: int
    cardinality_max: int
    based_ascc: AsccInfo
    asbiep: "UpdateAsbiepRelationshipDetail | None" = None  # Recursive structure


class UpdateBbieRelationshipDetail(BaseModel):
    """BBIE relationship detail with updates for update_asbie response."""
    bbie_id: int
    updates: list[str]  # List of fields that were updated on this BBIE
    guid: str | None = None
    path: str
    cardinality_min: int
    cardinality_max: int
    is_nillable: bool
    based_bcc: BccInfo


class UpdateRelationshipDetail(BaseModel):
    """Relationship detail with updates for update_asbie response."""
    asbie: UpdateAsbieRelationshipDetail | None = None
    bbie: UpdateBbieRelationshipDetail | None = None


class UpdateRoleOfAbieDetail(BaseModel):
    """Role of ABIE detail with updates for update_asbie response."""
    abie_id: int
    updates: list[str]  # List of fields that were updated on this ABIE (typically empty, as ABIE itself is rarely updated)
    relationships: list[UpdateRelationshipDetail] = []  # List of relationships with their updates


class UpdateAsbiepRelationshipDetail(BaseModel):
    """ASBIEP relationship detail with updates for update_asbie response."""
    asbiep_id: int
    updates: list[str]  # List of fields that were updated on this ASBIEP (typically empty, as ASBIEP itself is rarely updated)
    role_of_abie: UpdateRoleOfAbieDetail | None = None  # Recursive structure


class UpdateAsbieResponse(BaseModel):
    """Response for update_asbie tool."""
    asbie_id: int  # Unique identifier of the updated ASBIE
    updates: list[str]  # A list of field names that were updated on the ASBIE itself (e.g., ["definition", "cardinality_min", "remark"])
    asbiep: UpdateAsbiepRelationshipDetail | None = None  # Nested structure showing all updated relationships and their updates


class CreateBbieResponse(BaseModel):
    """Response for create_bbie tool."""
    bbie_id: int  # Unique identifier of the newly created BBIE
    bbiep: "CreateBbiepInfo | None" = None  # BBIEP information including all created/enabled supplementary components (excludes definition, remark, biz_term, display_name, default_value, fixed_value, facets)


class UpdateBbieScDetail(BaseModel):
    """BBIE SC detail with updates for update_bbie response."""
    bbie_sc_id: int | None
    updates: list[str]  # List of fields that were updated on this BBIE SC
    guid: str | None
    based_dt_sc: DtScInfo
    path: str
    hash_path: str
    cardinality_min: int
    cardinality_max: int


class UpdateBbiepDetail(BaseModel):
    """BBIEP detail with updates for update_bbie response."""
    bbiep_id: int | None
    updates: list[str]  # List of fields that were updated on this BBIEP (typically empty, as BBIEP itself is rarely updated)
    guid: str | None
    based_bccp: BccpInfo
    path: str
    hash_path: str
    supplementary_components: list[UpdateBbieScDetail]  # List of supplementary components with their updates


class UpdateBbieResponse(BaseModel):
    """Response for update_bbie tool."""
    bbie_id: int  # Unique identifier of the updated BBIE
    updates: list[str]  # A list of field names that were updated on the BBIE itself (e.g., ["definition", "cardinality_min", "default_value", "facet_min_length"])
    bbiep: UpdateBbiepDetail | None = None  # Nested structure showing all updated supplementary components and their updates


class CreateBbieScResponse(BaseModel):
    """Response for create_bbie_sc tool."""
    bbie_sc_id: int  # Unique identifier of the newly created BBIE_SC
    updates: list[str]  # A list of field names that were set during creation (e.g., ["bbie_sc_id", "is_used", "definition", "cardinality_min", "default_value"])


class UpdateBbieScResponse(BaseModel):
    """Response for update_bbie_sc tool."""
    bbie_sc_id: int  # Unique identifier of the updated BBIE_SC
    updates: list[str]  # A list of field names that were updated (e.g., ["definition", "cardinality_min", "default_value", "facet_min_length"])


class GetAsbieResponse(BaseModel):
    """Response for get_asbie tool."""
    asbie_id: int | None  # Unique identifier for the ASBIE (base entity ID, None if not yet created)
    owner_top_level_asbiep: TopLevelAsbiepInfo  # Information about the top-level ASBIEP that owns this ASBIE
    guid: str | None  # Globally unique identifier for the ASBIE (if available)
    based_ascc: AsccInfo  # Information about the ASCC that this ASBIE is based on
    to_asbiep: AsbiepInfo  # Information about the target ASBIEP that this ASBIE connects to
    is_used: bool  # Whether this ASBIE is currently being used (profiled) in the BIE
    path: str  # Hierarchical path string indicating the position of this ASBIE within the BIE structure
    hash_path: str  # Hashed version of the path for efficient lookups
    cardinality_min: int  # Minimum cardinality (minimum number of occurrences required, typically 0 or 1)
    cardinality_max: int  # Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)
    is_nillable: bool  # Whether the ASBIE can have a nil/null value
    remark: str | None  # Additional remarks or notes about the ASBIE

    @computed_field
    @property
    def cardinality_display(self) -> str:
        """Display cardinality with 'unbounded' for -1 values."""
        if self.cardinality_max == -1:
            return f"{self.cardinality_min}..unbounded"
        else:
            return f"{self.cardinality_min}..{self.cardinality_max}"


class BbieScInfo(BaseModel):
    """BBIE SC (Business Information Entity Supplementary Component) information object."""
    bbie_sc_id: int | None  # Unique identifier for the BBIE SC (base entity ID, None if not yet created)
    guid: str | None  # Globally unique identifier for the BBIE SC (if available)
    based_dt_sc: DtScInfo  # Information about the data type supplementary component that this BBIE SC is based on
    path: str  # Hierarchical path string indicating the position of this BBIE SC within the BIE structure
    hash_path: str  # Hashed version of the path for efficient lookups
    definition: str | None  # Definition or description of the BBIE SC
    cardinality_min: int  # Minimum cardinality (minimum number of occurrences required, typically 0 or 1)
    cardinality_max: int  # Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)
    primitiveRestriction: PrimitiveRestriction  # Primitive restriction information (xbtManifestId, codeListManifestId, agencyIdListManifestId). Required field that must not be None. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set (not multiple, not none).
    valueConstraint: ValueConstraint | None  # Value constraint information (default_value, fixed_value)
    facet: Facet | None  # Facet restriction information (min_length, max_length, pattern)
    owner_top_level_asbiep: TopLevelAsbiepInfo  # Information about the top-level ASBIEP that owns this BBIE SC


class BbiepInfo(BaseModel):
    """BBIEP (Basic Business Information Entity Property) information object."""
    bbiep_id: int | None  # Unique identifier for the BBIEP (base entity ID, None if not yet created)
    guid: str | None  # Globally unique identifier for the BBIEP (if available)
    based_bccp: BccpInfo  # Information about the BCCP that this BBIEP is based on
    path: str  # Hierarchical path string indicating the position of this BBIEP within the BIE structure
    hash_path: str  # Hashed version of the path for efficient lookups
    definition: str | None  # Definition or description of the BBIEP
    remark: str | None  # Additional remarks or notes about the BBIEP
    biz_term: str | None  # Business term that represents this BBIEP in business language
    display_name: str | None  # Display name intended for user interface presentation
    supplementary_components: list[BbieScInfo]  # List of supplementary components associated with this BBIEP
    owner_top_level_asbiep: TopLevelAsbiepInfo  # Information about the top-level ASBIEP that owns this BBIEP


class BbieInfo(BaseModel):
    """BBIE (Basic Business Information Entity) information object."""
    bbie_id: int  # Unique identifier for the BBIE (base entity ID)
    guid: str  # Globally unique identifier for the BBIE
    based_bcc: BccInfo  # Information about the BCC that this BBIE is based on
    cardinality_min: int  # Minimum cardinality (minimum number of occurrences required, typically 0 or 1)
    cardinality_max: int  # Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)
    is_nillable: bool  # Whether the BBIE can have a nil/null value
    remark: str | None  # Additional remarks or notes about the BBIE
    primitiveRestriction: PrimitiveRestriction  # Primitive restriction information (xbtManifestId, codeListManifestId, agencyIdListManifestId). Required field that must not be None. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set (not multiple, not none).
    valueConstraint: ValueConstraint | None  # Value constraint information (default_value, fixed_value)
    facet: Facet | None  # Facet restriction information (min_length, max_length, pattern)
    bbiep: BbiepInfo  # Information about the BBIEP associated with this BBIE
    owner_top_level_asbiep: TopLevelAsbiepInfo  # Information about the top-level ASBIEP that owns this BBIE


class GetBbieResponse(BaseModel):
    """Response for get_bbie tool."""
    bbie_id: int | None  # Unique identifier for the BBIE (base entity ID, None if not yet created)
    owner_top_level_asbiep: TopLevelAsbiepInfo  # Information about the top-level ASBIEP that owns this BBIE
    guid: str | None  # Globally unique identifier for the BBIE (if available)
    based_bcc: BccInfo  # Information about the BCC that this BBIE is based on
    to_bbiep: BbiepInfo  # Information about the target BBIEP that this BBIE connects to
    is_used: bool  # Whether this BBIE is currently being used (profiled) in the BIE
    path: str  # Hierarchical path string indicating the position of this BBIE within the BIE structure
    hash_path: str  # Hashed version of the path for efficient lookups
    cardinality_min: int  # Minimum cardinality (minimum number of occurrences required, typically 0 or 1)
    cardinality_max: int  # Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)
    is_nillable: bool  # Whether the BBIE can have a nil/null value
    remark: str | None  # Additional remarks or notes about the BBIE
    primitiveRestriction: PrimitiveRestriction  # Primitive restriction information (xbtManifestId, codeListManifestId, agencyIdListManifestId). Required field that must not be None. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set (not multiple, not none).
    valueConstraint: ValueConstraint | None  # Value constraint information (default_value, fixed_value)
    facet: Facet | None  # Facet restriction information (min_length, max_length, pattern)

    @computed_field
    @property
    def cardinality_display(self) -> str:
        if self.cardinality_max == -1:
            return f"{self.cardinality_min}..unbounded"
        return f"{self.cardinality_min}..{self.cardinality_max}"


class AsbieInfo(BaseModel):
    """ASBIE (Association Business Information Entity) information object."""
    asbie_id: int  # Unique identifier for the ASBIE (base entity ID)
    guid: str  # Globally unique identifier for the ASBIE
    based_ascc: AsccInfo  # Information about the ASCC that this ASBIE is based on
    cardinality_min: int  # Minimum cardinality (minimum number of occurrences required, typically 0 or 1)
    cardinality_max: int  # Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)
    is_nillable: bool  # Whether the ASBIE can have a nil/null value
    remark: str | None  # Additional remarks or notes about the ASBIE
    asbiep: AsbiepInfo  # Information about the ASBIEP associated with this ASBIE
    owner_top_level_asbiep: TopLevelAsbiepInfo  # Information about the top-level ASBIEP that owns this ASBIE


# Relationship models following the same pattern as core_component.py
class AbieRelationshipInfo(BaseModel):
    """Base ABIE relationship information object.
    
    The is_used property indicates profiling status:
    - is_used=True: Relationship is profiled for practical use (asbie_id/bbie_id is presented)
    - is_used=False: Relationship is available for profiling but not currently used
    """
    component_type: Literal["ASBIE", "BBIE"]  # Type of relationship: "ASBIE" (Association) or "BBIE" (Basic)
    is_used: bool  # Whether this component is currently being used (profiled) in the BIE (True means asbie_id/bbie_id exists)
    path: str  # Hierarchical path string indicating the position of this component within the BIE structure
    hash_path: str  # Hashed version of the path for efficient lookups
    cardinality_min: int  # Minimum cardinality (minimum number of occurrences required, typically 0 or 1)
    cardinality_max: int  # Maximum cardinality (maximum number of occurrences allowed, -1 means unbounded)
    is_nillable: bool  # Whether the component can have a nil/null value
    remark: str | None  # Additional remarks or notes about the component

    @computed_field
    @property
    def cardinality_display(self) -> str:
        """Display cardinality with 'unbounded' for -1 values."""
        if self.cardinality_max == -1:
            return f"{self.cardinality_min}..unbounded"
        else:
            return f"{self.cardinality_min}..{self.cardinality_max}"


class AsbieRelationshipInfo(AbieRelationshipInfo):
    """ASBIE relationship information object.
    
    When is_used=True, asbie_id will be created for practical use.
    When is_used=False, the relationship is available for profiling.
    """
    component_type: Literal["ASBIE"] = "ASBIE"  # Type of relationship, always "ASBIE" for this class
    asbie_id: int | None  # Unique identifier for the ASBIE (base entity ID, None if is_used=False)
    guid: str | None  # Globally unique identifier for the ASBIE (if available)
    based_ascc: AsccInfo  # Information about the ASCC that this ASBIE component is based on
    to_asbiep_id: int | None  # Unique identifier for the target ASBIEP that this ASBIE connects to (if available)

    @property
    def based_ascc_manifest_id(self) -> int:
        """Get the ASCC manifest ID from the based ASCC."""
        return self.based_ascc.ascc_manifest_id

    @property
    def based_manifest_id(self) -> int:
        return self.based_ascc.ascc_manifest_id


class Facet(BaseModel):
    """Facet restriction information for string values."""
    facet_min_length: int | None  # Minimum length constraint for string values (facet restriction)
    facet_max_length: int | None  # Maximum length constraint for string values (facet restriction)
    facet_pattern: str | None  # Pattern constraint (regular expression) for string values (facet restriction)


class PrimitiveRestriction(BaseModel):
    """Primitive restriction information for BBIE.
    
    Validation rules:
    - Should NOT be None (enforced at usage sites)
    - Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set (not multiple, not none)
    """
    xbtManifestId: int | None  # XBT (eXtended Built-in Type) manifest ID
    codeListManifestId: int | None  # Code list manifest ID
    agencyIdListManifestId: int | None  # Agency ID list manifest ID
    
    @model_validator(mode='after')
    def validate_primitive_restriction(self):
        """Validate that exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId is set."""
        has_xbt = self.xbtManifestId is not None
        has_code_list = self.codeListManifestId is not None
        has_agency_id_list = self.agencyIdListManifestId is not None
        
        count = sum([has_xbt, has_code_list, has_agency_id_list])
        
        if count == 0:
            raise ValueError(
                "PrimitiveRestriction: Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set. "
                "All cannot be None."
            )
        if count > 1:
            raise ValueError(
                "PrimitiveRestriction: Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set. "
                f"Found {count} values set: "
                f"{'xbtManifestId' if has_xbt else ''} "
                f"{'codeListManifestId' if has_code_list else ''} "
                f"{'agencyIdListManifestId' if has_agency_id_list else ''}".strip()
            )
        
        return self


class BbieRelationshipInfo(AbieRelationshipInfo):
    """BBIE relationship information object.
    
    When is_used=True, bbie_id will be created for practical use.
    When is_used=False, the relationship is available for profiling.
    """
    component_type: Literal["BBIE"] = "BBIE"  # Type of relationship, always "BBIE" for this class
    bbie_id: int | None  # Unique identifier for the BBIE (base entity ID, None if is_used=False)
    guid: str | None  # Globally unique identifier for the BBIE (if available)
    based_bcc: BccInfo  # Information about the BCC that this BBIE component is based on
    primitiveRestriction: PrimitiveRestriction  # Primitive restriction information (xbtManifestId, codeListManifestId, agencyIdListManifestId). Required field that must not be None. Exactly one of xbtManifestId, codeListManifestId, or agencyIdListManifestId must be set (not multiple, not none).
    valueConstraint: ValueConstraint | None  # Value constraint information (default_value, fixed_value)
    facet: Facet | None  # Facet restriction information (min_length, max_length, pattern)
    to_bbiep_id: int | None  # Unique identifier for the target BBIEP that this BBIE connects to (if available)

    @property
    def based_bcc_manifest_id(self) -> int:
        """Get the BCC manifest ID from the based BCC."""
        return self.based_bcc.bcc_manifest_id

    @property
    def based_manifest_id(self) -> int:
        return self.based_bcc.bcc_manifest_id


class AbieInfo(BaseModel):
    """ABIE (Aggregation Business Information Entity) information object."""
    abie_id: int | None  # Unique identifier for the ABIE (base entity ID, None if not yet created)
    guid: str | None  # Globally unique identifier for the ABIE (if available)
    based_acc_manifest: AccInfo  # Information about the ACC that this ABIE is based on
    definition: str | None  # Definition or description of the ABIE
    remark: str | None  # Additional remarks or notes about the ABIE
    relationships: List[Union[AsbieRelationshipInfo, BbieRelationshipInfo]]  # List of relationships (ASBIEs and BBIEs) contained in the ABIE
    created: WhoAndWhen | None  # Information about who created the ABIE and when (if available)
    last_updated: WhoAndWhen | None  # Information about who last updated the ABIE and when (if available)


class GetBbiepResponse(BaseModel):
    """Response for get_bbiep tool."""
    bbiep_id: int  # Unique identifier for the BBIEP (base entity ID)
    guid: str  # Globally unique identifier for the BBIEP
    based_bccp: BccpInfo  # Information about the BCCP that this BBIEP is based on
    definition: str | None  # Definition or description of the BBIEP
    remark: str | None  # Additional remarks or notes about the BBIEP
    biz_term: str | None  # Business term that represents this BBIEP in business language
    display_name: str | None  # Display name intended for user interface presentation
    supplementary_components: list[BbieScInfo]  # List of supplementary components associated with this BBIEP
    owner_top_level_asbiep: TopLevelAsbiepInfo  # Information about the top-level ASBIEP that owns this BBIEP
    created: WhoAndWhen  # Information about who created the BBIEP and when
    last_updated: WhoAndWhen  # Information about who last updated the BBIEP and when


# Update forward references - must be called after all class definitions
AsbieRelationshipDetail.model_rebuild()
AsbiepRelationshipDetail.model_rebuild()
RoleOfAbieDetail.model_rebuild()
CreateAsbieRelationshipDetail.model_rebuild()
CreateBbieRelationshipDetail.model_rebuild()
CreateAsbiepRelationshipDetail.model_rebuild()
CreateRoleOfAbieDetail.model_rebuild()
CreateRelationshipDetail.model_rebuild()
CreateTopLevelAsbiepResponse.model_rebuild()
CreateAsbieResponse.model_rebuild()
CreateBbieResponse.model_rebuild()
UpdateAsbieRelationshipDetail.model_rebuild()
UpdateBbieRelationshipDetail.model_rebuild()
UpdateRelationshipDetail.model_rebuild()
UpdateRoleOfAbieDetail.model_rebuild()
UpdateAsbiepRelationshipDetail.model_rebuild()
UpdateAsbieResponse.model_rebuild()
UpdateBbieScDetail.model_rebuild()
UpdateBbiepDetail.model_rebuild()
UpdateBbieResponse.model_rebuild()

