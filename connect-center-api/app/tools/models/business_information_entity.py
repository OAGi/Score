"""Models for Business Information Entity MCP tools."""

from __future__ import annotations

from typing import Annotated, Literal

from pydantic import BaseModel, ConfigDict, Field

from app.tools.models.shared import BizCtxSummaryRecord
from app.tools.models.shared import LibrarySummaryRecord
from app.tools.models.shared import NamespaceSummaryRecord
from app.tools.models.shared import ReleaseSummaryRecord
from app.tools.models.shared import UserSummary
from app.tools.models.shared import ValueConstraintRecord
from app.tools.models.shared import WhoAndWhen


class DataTypeSummaryRecord(BaseModel):
    """Manifest-scoped BDT summary."""

    dt_manifest_id: int = Field(..., ge=1, description="Data type manifest identifier.")
    dt_id: int = Field(..., ge=1, description="Data type identifier.")
    based_dt_manifest_id: int | None = Field(default=None, ge=1, description="Base data type manifest identifier.")
    guid: str = Field(..., description="Data type GUID.")
    den: str = Field(..., description="Data type DEN.")
    data_type_term: str | None = Field(default=None, description="Data type term.")
    qualifier: str | None = Field(default=None, description="Data type qualifier.")
    representation_term: str | None = Field(default=None, description="Data type representation term.")
    six_digit_id: str | None = Field(default=None, description="Data type six-digit identifier.")
    definition: str | None = Field(default=None, description="Data type definition.")
    definition_source: str | None = Field(default=None, description="Data type definition source.")
    content_component_definition: str | None = Field(default=None, description="Content component definition.")
    is_deprecated: bool = Field(..., description="Whether the data type is deprecated.")
    namespace: NamespaceSummaryRecord | None = Field(default=None, description="Namespace information.")
    library: LibrarySummaryRecord = Field(..., description="Library information.")
    release: ReleaseSummaryRecord = Field(..., description="Release information.")

    model_config = ConfigDict(frozen=True)


class DataTypeSupplementaryComponentRecord(BaseModel):
    """Manifest-scoped data type supplementary component."""

    dt_sc_manifest_id: int = Field(..., ge=1, description="Supplementary component manifest identifier.")
    dt_sc_id: int = Field(..., ge=1, description="Supplementary component identifier.")
    guid: str = Field(..., description="Supplementary component GUID.")
    object_class_term: str | None = Field(default=None, description="Object class term.")
    property_term: str | None = Field(default=None, description="Property term.")
    representation_term: str | None = Field(default=None, description="Representation term.")
    definition: str | None = Field(default=None, description="Definition.")
    definition_source: str | None = Field(default=None, description="Definition source.")
    cardinality_min: int = Field(..., description="Minimum cardinality.")
    cardinality_max: int | None = Field(default=None, description="Maximum cardinality.")
    is_deprecated: bool = Field(..., description="Whether deprecated.")

    model_config = ConfigDict(frozen=True)


class AccInfoRecord(BaseModel):
    """Manifest-scoped ACC information."""

    acc_manifest_id: int = Field(..., ge=1, description="ACC manifest identifier.")
    acc_id: int = Field(..., ge=1, description="ACC identifier.")
    guid: str = Field(..., description="ACC GUID.")
    den: str = Field(..., description="ACC DEN.")
    object_class_term: str = Field(..., description="ACC object class term.")
    definition: str | None = Field(default=None, description="ACC definition.")
    definition_source: str | None = Field(default=None, description="ACC definition source.")
    is_deprecated: bool = Field(..., description="Whether ACC is deprecated.")

    model_config = ConfigDict(frozen=True)


class AsccpInfoRecord(BaseModel):
    """Manifest-scoped ASCCP information."""

    asccp_manifest_id: int = Field(..., ge=1, description="ASCCP manifest identifier.")
    asccp_id: int = Field(..., ge=1, description="ASCCP identifier.")
    role_of_acc_manifest_id: int = Field(..., ge=1, description="Role-of ACC manifest identifier.")
    guid: str = Field(..., description="ASCCP GUID.")
    den: str | None = Field(default=None, description="ASCCP DEN.")
    property_term: str | None = Field(default=None, description="ASCCP property term.")
    definition: str | None = Field(default=None, description="ASCCP definition.")
    definition_source: str | None = Field(default=None, description="ASCCP definition source.")
    is_deprecated: bool = Field(..., description="Whether ASCCP is deprecated.")

    model_config = ConfigDict(frozen=True)


class AsccInfoRecord(BaseModel):
    """Manifest-scoped ASCC information."""

    ascc_manifest_id: int = Field(..., ge=1, description="ASCC manifest identifier.")
    ascc_id: int = Field(..., ge=1, description="ASCC identifier.")
    guid: str = Field(..., description="ASCC GUID.")
    den: str = Field(..., description="ASCC DEN.")
    cardinality_min: int = Field(..., description="Minimum cardinality.")
    cardinality_max: int = Field(..., description="Maximum cardinality.")
    is_deprecated: bool = Field(..., description="Whether ASCC is deprecated.")
    definition: str | None = Field(default=None, description="ASCC definition.")
    definition_source: str | None = Field(default=None, description="ASCC definition source.")
    from_acc_manifest_id: int = Field(..., ge=1, description="Source ACC manifest identifier.")
    to_asccp_manifest_id: int = Field(..., ge=1, description="Target ASCCP manifest identifier.")

    model_config = ConfigDict(frozen=True)


class BccpInfoRecord(BaseModel):
    """Manifest-scoped BCCP information."""

    bccp_manifest_id: int = Field(..., ge=1, description="BCCP manifest identifier.")
    bccp_id: int = Field(..., ge=1, description="BCCP identifier.")
    guid: str = Field(..., description="BCCP GUID.")
    den: str = Field(..., description="BCCP DEN.")
    property_term: str = Field(..., description="BCCP property term.")
    representation_term: str = Field(..., description="BCCP representation term.")
    definition: str | None = Field(default=None, description="BCCP definition.")
    definition_source: str | None = Field(default=None, description="BCCP definition source.")
    bdt_manifest: DataTypeSummaryRecord = Field(..., description="BDT summary associated with this BCCP.")
    is_deprecated: bool = Field(..., description="Whether BCCP is deprecated.")

    model_config = ConfigDict(frozen=True)


class BccInfoRecord(BaseModel):
    """Manifest-scoped BCC information."""

    bcc_manifest_id: int = Field(..., ge=1, description="BCC manifest identifier.")
    bcc_id: int = Field(..., ge=1, description="BCC identifier.")
    guid: str = Field(..., description="BCC GUID.")
    den: str = Field(..., description="BCC DEN.")
    cardinality_min: int = Field(..., description="Minimum cardinality.")
    cardinality_max: int = Field(..., description="Maximum cardinality.")
    entity_type: Literal["Attribute", "Element"] | None = Field(default=None, description="Entity type.")
    is_nillable: bool = Field(..., description="Whether BCC is nillable.")
    is_deprecated: bool = Field(..., description="Whether BCC is deprecated.")
    definition: str | None = Field(default=None, description="BCC definition.")
    definition_source: str | None = Field(default=None, description="BCC definition source.")
    from_acc_manifest_id: int = Field(..., ge=1, description="Source ACC manifest identifier.")
    to_bccp_manifest_id: int = Field(..., ge=1, description="Target BCCP manifest identifier.")

    model_config = ConfigDict(frozen=True)


class PrimitiveRestrictionRecord(BaseModel):
    """Primitive restriction summary."""

    xbtManifestId: int | None = None
    codeListManifestId: int | None = None
    agencyIdListManifestId: int | None = None

    model_config = ConfigDict(frozen=True)


class FacetRecord(BaseModel):
    """Facet restriction summary."""

    facet_min_length: int | None = None
    facet_max_length: int | None = None
    facet_pattern: str | None = None

    model_config = ConfigDict(frozen=True)


class TopLevelAsbiepInfoRecord(BaseModel):
    """Top-level ASBIEP metadata summary."""

    top_level_asbiep_id: int = Field(..., ge=1, description="Top-level ASBIEP identifier.")
    library: LibrarySummaryRecord = Field(..., description="Library information.")
    release: ReleaseSummaryRecord = Field(..., description="Release information.")
    version: str | None = Field(default=None, description="Version.")
    status: str | None = Field(default=None, description="Status.")
    state: str | None = Field(default=None, description="State.")
    is_deprecated: bool = Field(..., description="Deprecation flag.")
    deprecated_reason: str | None = Field(default=None, description="Deprecation reason.")
    deprecated_remark: str | None = Field(default=None, description="Deprecation remark.")
    owner: UserSummary = Field(..., description="Owner information.")
    created: WhoAndWhen | None = Field(default=None, description="Creation metadata.")
    last_updated: WhoAndWhen | None = Field(default=None, description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class AsbieRelationshipRecord(BaseModel):
    """ASBIE relationship summary."""

    component_type: Literal["ASBIE"] = Field(default="ASBIE", description="Relationship type discriminator.")
    asbie_id: int | None = Field(default=None, description="ASBIE identifier.")
    guid: str | None = Field(default=None, description="ASBIE GUID.")
    based_ascc: AsccInfoRecord = Field(..., description="Based ASCC information.")
    to_asbiep_id: int | None = Field(default=None, description="Target ASBIEP identifier.")
    is_used: bool = Field(..., description="Whether profiled or used.")
    path: str = Field(..., description="Relationship path.")
    hash_path: str = Field(..., description="Relationship hash path.")
    cardinality_min: int = Field(..., description="Minimum cardinality.")
    cardinality_max: int = Field(..., description="Maximum cardinality.")
    cardinality_display: str = Field(..., description="Human-readable cardinality display.")
    is_nillable: bool = Field(..., description="Nillable indicator.")
    remark: str | None = Field(default=None, description="Relationship remark.")

    model_config = ConfigDict(frozen=True)


class BbieRelationshipRecord(BaseModel):
    """BBIE relationship summary."""

    component_type: Literal["BBIE"] = Field(default="BBIE", description="Relationship type discriminator.")
    bbie_id: int | None = Field(default=None, description="BBIE identifier.")
    guid: str | None = Field(default=None, description="BBIE GUID.")
    based_bcc: BccInfoRecord = Field(..., description="Based BCC information.")
    to_bbiep_id: int | None = Field(default=None, description="Target BBIEP identifier.")
    is_used: bool = Field(..., description="Whether profiled or used.")
    path: str = Field(..., description="Relationship path.")
    hash_path: str = Field(..., description="Relationship hash path.")
    cardinality_min: int = Field(..., description="Minimum cardinality.")
    cardinality_max: int = Field(..., description="Maximum cardinality.")
    cardinality_display: str = Field(..., description="Human-readable cardinality display.")
    is_nillable: bool = Field(..., description="Nillable indicator.")
    remark: str | None = Field(default=None, description="Relationship remark.")
    primitiveRestriction: PrimitiveRestrictionRecord | None = Field(default=None, description="Primitive restriction.")
    valueConstraint: ValueConstraintRecord | None = Field(default=None, description="Value constraint.")
    facet: FacetRecord | None = Field(default=None, description="Facet restriction.")

    model_config = ConfigDict(frozen=True)


RelationshipRecord = Annotated[AsbieRelationshipRecord | BbieRelationshipRecord, Field(discriminator="component_type")]


class AbieInfoRecord(BaseModel):
    """ABIE information summary."""

    abie_id: int | None = Field(default=None, description="ABIE identifier.")
    guid: str | None = Field(default=None, description="ABIE GUID.")
    path: str | None = Field(default=None, description="ABIE path.")
    hash_path: str | None = Field(default=None, description="ABIE hash path.")
    based_acc_manifest: AccInfoRecord = Field(..., description="Based ACC information.")
    definition: str | None = Field(default=None, description="ABIE definition.")
    remark: str | None = Field(default=None, description="ABIE remark.")
    relationships: list[RelationshipRecord] = Field(default_factory=list, description="ASBIE/BBIE relationship list.")
    created: WhoAndWhen | None = Field(default=None, description="Creation metadata.")
    last_updated: WhoAndWhen | None = Field(default=None, description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class AsbiepInfoRecord(BaseModel):
    """ASBIEP information summary."""

    asbiep_id: int | None = Field(default=None, description="ASBIEP identifier.")
    guid: str | None = Field(default=None, description="ASBIEP GUID.")
    based_asccp_manifest: AsccpInfoRecord = Field(..., description="Based ASCCP information.")
    role_of_abie: AbieInfoRecord = Field(..., description="Role-of ABIE information.")
    path: str | None = Field(default=None, description="ASBIEP path.")
    hash_path: str | None = Field(default=None, description="ASBIEP hash path.")
    definition: str | None = Field(default=None, description="ASBIEP definition.")
    remark: str | None = Field(default=None, description="ASBIEP remark.")
    biz_term: str | None = Field(default=None, description="Business term.")
    display_name: str | None = Field(default=None, description="Display name.")
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord | None = Field(
        default=None,
        description="Owning top-level ASBIEP.",
    )
    created: WhoAndWhen | None = Field(default=None, description="Creation metadata.")
    last_updated: WhoAndWhen | None = Field(default=None, description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class TopLevelAsbiepRoleOfAbieRecord(AbieInfoRecord):
    """Role-of ABIE for top-level ASBIEP payloads."""

    abie_id: int = Field(..., ge=1, description="ABIE identifier.")
    guid: str = Field(..., description="ABIE GUID.")
    path: str = Field(..., description="ABIE path.")
    hash_path: str = Field(..., description="ABIE hash path.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class CreateAsbieRoleOfAbieRecord(AbieInfoRecord):
    """Role-of ABIE shape returned by create-ASBIE."""

    abie_id: int = Field(..., ge=1, description="ABIE identifier.")
    guid: str = Field(..., description="ABIE GUID.")
    path: str = Field(..., description="ABIE path.")
    hash_path: str = Field(..., description="ABIE hash path.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class TopLevelAsbiepAbieInfoRecord(BaseModel):
    """ABIE information under top-level ASBIEP payloads."""

    abie_id: int = Field(..., description="ABIE identifier.")
    guid: str = Field(..., description="ABIE GUID.")
    path: str = Field(..., description="ABIE path.")
    hash_path: str = Field(..., description="ABIE hash path.")
    based_acc_manifest: AccInfoRecord = Field(..., description="Based ACC information.")
    definition: str | None = Field(..., description="ABIE definition.")
    remark: str | None = Field(..., description="ABIE remark.")
    relationships: list[RelationshipRecord] = Field(default_factory=list, description="ASBIE/BBIE relationship list.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class TopLevelAsbiepInfoAsbiepRecord(BaseModel):
    """ASBIEP payload under get-top-level response."""

    asbiep_id: int = Field(..., description="ASBIEP identifier.")
    guid: str = Field(..., description="ASBIEP GUID.")
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord = Field(..., description="Owning top-level ASBIEP.")
    based_asccp_manifest: AsccpInfoRecord = Field(..., description="Based ASCCP information.")
    role_of_abie: TopLevelAsbiepAbieInfoRecord = Field(..., description="Role-of ABIE information.")
    path: str = Field(..., description="ASBIEP path.")
    hash_path: str = Field(..., description="ASBIEP hash path.")
    definition: str | None = Field(default=None, description="ASBIEP definition.")
    remark: str | None = Field(default=None, description="ASBIEP remark.")
    biz_term: str | None = Field(default=None, description="Business term.")
    display_name: str | None = Field(default=None, description="Display name.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class CreateAsbieAsbiepRecord(AsbiepInfoRecord):
    """ASBIEP shape returned by create-ASBIE."""

    asbiep_id: int = Field(..., ge=1, description="ASBIEP identifier.")
    role_of_abie: CreateAsbieRoleOfAbieRecord = Field(..., description="Role-of ABIE information.")
    path: str = Field(..., description="ASBIEP path.")
    hash_path: str = Field(..., description="ASBIEP hash path.")
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord = Field(..., description="Owning top-level ASBIEP.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class CreateTopLevelAsbiepAsbiepRecord(AsbiepInfoRecord):
    """ASBIEP shape returned by create-top-level."""

    asbiep_id: int = Field(..., ge=1, description="ASBIEP identifier.")
    guid: str = Field(..., description="ASBIEP GUID.")
    path: str = Field(..., description="ASBIEP path.")
    hash_path: str = Field(..., description="ASBIEP hash path.")
    role_of_abie: TopLevelAsbiepRoleOfAbieRecord = Field(..., description="Role-of ABIE information.")
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord = Field(..., description="Owning top-level ASBIEP.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class BbieScInfoRecord(BaseModel):
    """BBIE_SC information summary."""

    bbie_sc_id: int | None = Field(default=None, description="BBIE_SC identifier.")
    guid: str | None = Field(default=None, description="BBIE_SC GUID.")
    based_dt_sc: DataTypeSupplementaryComponentRecord = Field(..., description="Based DT_SC information.")
    path: str = Field(..., description="BBIE_SC path.")
    hash_path: str = Field(..., description="BBIE_SC hash path.")
    definition: str | None = Field(default=None, description="BBIE_SC definition.")
    biz_term: str | None = Field(default=None, description="Business term.")
    display_name: str | None = Field(default=None, description="Display name.")
    cardinality_min: int = Field(..., description="Minimum cardinality.")
    cardinality_max: int = Field(..., description="Maximum cardinality.")
    primitiveRestriction: PrimitiveRestrictionRecord | None = Field(default=None, description="Primitive restriction.")
    valueConstraint: ValueConstraintRecord | None = Field(default=None, description="Value constraint.")
    facet: FacetRecord | None = Field(default=None, description="Facet restriction.")
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord | None = Field(
        default=None,
        description="Owning top-level ASBIEP.",
    )

    model_config = ConfigDict(frozen=True)


class BbiepInfoRecord(BaseModel):
    """BBIEP information summary."""

    bbiep_id: int | None = Field(default=None, description="BBIEP identifier.")
    guid: str | None = Field(default=None, description="BBIEP GUID.")
    based_bccp: BccpInfoRecord = Field(..., description="Based BCCP information.")
    path: str | None = Field(default=None, description="BBIEP path.")
    hash_path: str | None = Field(default=None, description="BBIEP hash path.")
    definition: str | None = Field(default=None, description="BBIEP definition.")
    remark: str | None = Field(default=None, description="BBIEP remark.")
    biz_term: str | None = Field(default=None, description="Business term.")
    display_name: str | None = Field(default=None, description="Display name.")
    supplementary_components: list[BbieScInfoRecord] = Field(default_factory=list, description="Supplementary components.")
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord | None = Field(
        default=None,
        description="Owning top-level ASBIEP.",
    )
    created: WhoAndWhen | None = Field(default=None, description="Creation metadata.")
    last_updated: WhoAndWhen | None = Field(default=None, description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class CreateBbieBbiepRecord(BbiepInfoRecord):
    """BBIEP shape returned by create-BBIE."""

    bbiep_id: int = Field(..., ge=1, description="BBIEP identifier.")
    guid: str = Field(..., description="BBIEP GUID.")
    path: str = Field(..., description="BBIEP path.")
    hash_path: str = Field(..., description="BBIEP hash path.")
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord = Field(..., description="Owning top-level ASBIEP.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class TopLevelAsbiepListEntry(BaseModel):
    """Top-level ASBIEP list row."""

    top_level_asbiep_id: int = Field(..., ge=1, description="Top-level ASBIEP identifier.")
    asbiep_id: int = Field(..., ge=1, description="ASBIEP identifier.")
    guid: str = Field(..., description="ASBIEP GUID.")
    den: str | None = Field(default=None, description="Dictionary entry name.")
    property_term: str | None = Field(default=None, description="Property term from the underlying ASCCP.")
    display_name: str | None = Field(default=None, description="Display name.")
    version: str | None = Field(default=None, description="Version.")
    status: str | None = Field(default=None, description="Status.")
    biz_term: str | None = Field(default=None, description="Business term.")
    remark: str | None = Field(default=None, description="Remark.")
    business_contexts: list[BizCtxSummaryRecord] = Field(default_factory=list, description="Assigned business contexts.")
    state: str | None = Field(default=None, description="Lifecycle state.")
    is_deprecated: bool = Field(..., description="Deprecation flag.")
    deprecated_reason: str | None = Field(default=None, description="Deprecation reason.")
    deprecated_remark: str | None = Field(default=None, description="Deprecation remark.")
    library: LibrarySummaryRecord = Field(..., description="Library information.")
    release: ReleaseSummaryRecord = Field(..., description="Release information.")
    owner: UserSummary = Field(..., description="Owner information.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class GetTopLevelAsbiepListResponse(BaseModel):
    """Paginated response envelope for top-level ASBIEP list."""

    total_items: int = Field(..., ge=0, description="Total number of matching top-level ASBIEPs.")
    offset: int = Field(..., ge=0, description="Offset used for this page.")
    limit: int = Field(..., ge=1, le=100, description="Limit used for this page.")
    items: list[TopLevelAsbiepListEntry] = Field(..., description="Top-level ASBIEP entries.")

    model_config = ConfigDict(frozen=True)


class GetTopLevelAsbiepResponse(BaseModel):
    """Get-top-level ASBIEP response."""

    top_level_asbiep_id: int = Field(..., ge=1, description="Top-level ASBIEP identifier.")
    asbiep: TopLevelAsbiepInfoAsbiepRecord = Field(..., description="ASBIEP information.")
    version: str | None = Field(default=None, description="Version.")
    status: str | None = Field(default=None, description="Status.")
    business_contexts: list[BizCtxSummaryRecord] = Field(default_factory=list, description="Assigned business contexts.")
    state: str = Field(..., description="Lifecycle state.")
    is_deprecated: bool = Field(..., description="Deprecation flag.")
    deprecated_reason: str | None = Field(default=None, description="Deprecation reason.")
    deprecated_remark: str | None = Field(default=None, description="Deprecation remark.")
    owner: UserSummary = Field(..., description="Owner information.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class GetAsbieByAsbieIdResponse(BaseModel):
    """ASBIE detail response."""

    asbie_id: int = Field(..., description="ASBIE identifier.")
    guid: str = Field(..., description="ASBIE GUID.")
    based_ascc: AsccInfoRecord = Field(..., description="Based ASCC information.")
    to_asbiep: AsbiepInfoRecord = Field(..., description="Target ASBIEP.")
    is_used: bool = Field(..., description="Whether profiled or used.")
    cardinality_min: int = Field(..., description="Minimum cardinality.")
    cardinality_max: int = Field(..., description="Maximum cardinality.")
    is_nillable: bool = Field(..., description="Nillable indicator.")
    definition: str | None = Field(default=None, description="ASBIE definition.")
    remark: str | None = Field(default=None, description="ASBIE remark.")
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord = Field(..., description="Owning top-level ASBIEP.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class GetAsbieByBasedAsccManifestIdResponse(BaseModel):
    """ASBIE template detail response."""

    asbie_id: int | None = Field(default=None, description="ASBIE identifier.")
    guid: str | None = Field(default=None, description="ASBIE GUID.")
    based_ascc: AsccInfoRecord = Field(..., description="Based ASCC information.")
    to_asbiep: AsbiepInfoRecord = Field(..., description="Target ASBIEP.")
    is_used: bool = Field(..., description="Whether profiled or used.")
    cardinality_min: int = Field(..., description="Minimum cardinality.")
    cardinality_max: int = Field(..., description="Maximum cardinality.")
    is_nillable: bool = Field(..., description="Nillable indicator.")
    definition: str | None = Field(default=None, description="ASBIE definition.")
    remark: str | None = Field(default=None, description="ASBIE remark.")
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord = Field(..., description="Owning top-level ASBIEP.")
    created: WhoAndWhen | None = Field(default=None, description="Creation metadata.")
    last_updated: WhoAndWhen | None = Field(default=None, description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class GetBbieByBbieIdResponse(BaseModel):
    """BBIE detail response."""

    bbie_id: int = Field(..., description="BBIE identifier.")
    guid: str = Field(..., description="BBIE GUID.")
    based_bcc: BccInfoRecord = Field(..., description="Based BCC information.")
    to_bbiep: BbiepInfoRecord = Field(..., description="Target BBIEP.")
    is_used: bool = Field(..., description="Whether profiled or used.")
    cardinality_min: int = Field(..., description="Minimum cardinality.")
    cardinality_max: int = Field(..., description="Maximum cardinality.")
    is_nillable: bool = Field(..., description="Nillable indicator.")
    remark: str | None = Field(default=None, description="BBIE remark.")
    primitiveRestriction: PrimitiveRestrictionRecord = Field(..., description="Primitive restriction.")
    valueConstraint: ValueConstraintRecord | None = Field(default=None, description="Value constraint.")
    facet: FacetRecord | None = Field(default=None, description="Facet restriction.")
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord = Field(..., description="Owning top-level ASBIEP.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class GetBbieByBasedBccManifestIdResponse(BaseModel):
    """BBIE template detail response."""

    bbie_id: int | None = Field(default=None, description="BBIE identifier.")
    guid: str | None = Field(default=None, description="BBIE GUID.")
    based_bcc: BccInfoRecord = Field(..., description="Based BCC information.")
    to_bbiep: BbiepInfoRecord = Field(..., description="Target BBIEP.")
    is_used: bool = Field(..., description="Whether profiled or used.")
    cardinality_min: int = Field(..., description="Minimum cardinality.")
    cardinality_max: int = Field(..., description="Maximum cardinality.")
    is_nillable: bool = Field(..., description="Nillable indicator.")
    remark: str | None = Field(default=None, description="BBIE remark.")
    primitiveRestriction: PrimitiveRestrictionRecord | None = Field(default=None, description="Primitive restriction.")
    valueConstraint: ValueConstraintRecord | None = Field(default=None, description="Value constraint.")
    facet: FacetRecord | None = Field(default=None, description="Facet restriction.")
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord = Field(..., description="Owning top-level ASBIEP.")
    created: WhoAndWhen | None = Field(default=None, description="Creation metadata.")
    last_updated: WhoAndWhen | None = Field(default=None, description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class UpdateTopLevelASBIEPResponse(BaseModel):
    """Response payload for top-level ASBIEP update operations."""

    top_level_asbiep_id: int = Field(..., ge=1, description="Target top-level ASBIEP identifier.", examples=[1])
    updates: list[str] = Field(default_factory=list, description="Updated field names.", examples=[["definition"]])

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "top_level_asbiep_id": 1,
                "updates": ["definition"],
            }
        },
    )


class UpdateTopLevelAsbiepStateResponse(BaseModel):
    """Response payload for top-level ASBIEP state updates."""

    top_level_asbiep_id: int = Field(..., ge=1, description="Target top-level ASBIEP identifier.", examples=[1])
    updates: list[str] = Field(default_factory=list, description="Updated field names.", examples=[["state:Published"]])

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "top_level_asbiep_id": 1,
                "updates": ["state:Published"],
            }
        },
    )


class DeleteTopLevelASBIEPResponse(BaseModel):
    """Response payload for top-level ASBIEP deletion."""

    top_level_asbiep_id: int | None = Field(default=None, description="Deleted top-level ASBIEP identifier.")
    message: str | None = Field(default=None, description="Optional status message.")

    model_config = ConfigDict(frozen=True)


class UpdateASBIEResponse(BaseModel):
    """Response payload for updating an ASBIE."""

    asbie_id: int = Field(..., ge=1, description="Target ASBIE identifier.", examples=[1])
    updates: list[str] = Field(default_factory=list, description="Updated field names.", examples=[["definition"]])
    asbiep: AsbiepInfoRecord | None = Field(default=None, description="Resolved ASBIEP details.")

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "asbie_id": 1,
                "updates": ["definition"],
            }
        },
    )


class UpdateBBIEResponse(BaseModel):
    """Response payload for updating a BBIE."""

    bbie_id: int = Field(..., ge=1, description="Target BBIE identifier.", examples=[1])
    updates: list[str] = Field(default_factory=list, description="Updated field names.", examples=[["fixed_value"]])
    bbiep: BbiepInfoRecord | None = Field(default=None, description="Resolved BBIEP details.")

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "bbie_id": 1,
                "updates": ["fixed_value"],
            }
        },
    )


class CreateBBIESCResponse(BaseModel):
    """Response payload for creating a BBIE_SC."""

    bbie_sc_id: int = Field(..., ge=1, description="Created BBIE_SC identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.")

    model_config = ConfigDict(frozen=True)


class UpdateBBIESCResponse(BaseModel):
    """Response payload for updating a BBIE_SC."""

    bbie_sc_id: int = Field(..., ge=1, description="Target BBIE_SC identifier.", examples=[1])
    updates: list[str] = Field(default_factory=list, description="Updated field names.", examples=[["facet_pattern"]])

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "bbie_sc_id": 1,
                "updates": ["facet_pattern"],
            }
        },
    )


class GetTopLevelAsbiepPaginationResponse(GetTopLevelAsbiepListResponse):
    """Response for get_top_level_asbiep_list tool."""


class GetTopLevelAsbiepToolResponse(GetTopLevelAsbiepResponse):
    """Response for get_top_level_asbiep tool."""


class GetAsbieResponse(GetAsbieByAsbieIdResponse):
    """Response for ASBIE detail tools."""


class GetAsbieTemplateResponse(GetAsbieByBasedAsccManifestIdResponse):
    """Response for ASBIE template-detail tools."""


class GetBbieResponse(GetBbieByBbieIdResponse):
    """Response for BBIE detail tools."""


class GetBbieTemplateResponse(GetBbieByBasedBccManifestIdResponse):
    """Response for BBIE template-detail tools."""


class CreateTopLevelAsbiepResponse(BaseModel):
    """Response for create_top_level_asbiep tool."""

    top_level_asbiep_id: int
    asbiep: CreateTopLevelAsbiepAsbiepRecord | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class UpdateTopLevelAsbiepResponse(UpdateTopLevelASBIEPResponse):
    """Response for update_top_level_asbiep tool."""


class UpdateTopLevelAsbiepStateToolResponse(UpdateTopLevelAsbiepStateResponse):
    """Response for update_top_level_asbiep_state tool."""


class DeleteTopLevelAsbiepToolResponse(DeleteTopLevelASBIEPResponse):
    """Response for delete_top_level_asbiep tool."""


class TransferTopLevelAsbiepOwnershipResponse(BaseModel):
    """Response for transfer_top_level_asbiep_ownership tool."""

    top_level_asbiep_id: int
    updates: list[str]


class AssignBizCtxToTopLevelAsbiepToolResponse(BaseModel):
    """Response for assign_biz_ctx_to_top_level_asbiep tool."""

    top_level_asbiep_id: int
    updates: list[str]


class UnassignBizCtxFromTopLevelAsbiepToolResponse(BaseModel):
    """Response for unassign_biz_ctx_from_top_level_asbiep tool."""

    top_level_asbiep_id: int
    updates: list[str]


class CreateAsbieToolResponse(BaseModel):
    """Response for create_asbie tool."""

    asbie_id: int
    asbiep: CreateAsbieAsbiepRecord | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class UpdateAsbieToolResponse(UpdateASBIEResponse):
    """Response for update_asbie tool."""


class CreateBbieToolResponse(BaseModel):
    """Response for create_bbie tool."""

    bbie_id: int
    bbiep: CreateBbieBbiepRecord | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class UpdateBbieToolResponse(UpdateBBIEResponse):
    """Response for update_bbie tool."""


class CreateBbieScToolResponse(CreateBBIESCResponse):
    """Response for create_bbie_sc tool."""


class UpdateBbieScToolResponse(UpdateBBIESCResponse):
    """Response for update_bbie_sc tool."""


class ReuseTopLevelAsbiepToolResponse(BaseModel):
    """Response for reuse_top_level_asbiep tool."""

    asbie_id: int
    updates: list[str]
    asbiep: AsbiepInfoRecord | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class RemoveReusedTopLevelAsbiepToolResponse(BaseModel):
    """Response for remove_reused_top_level_asbiep tool."""

    asbie_id: int
    updates: list[str]
    asbiep: AsbiepInfoRecord | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)
