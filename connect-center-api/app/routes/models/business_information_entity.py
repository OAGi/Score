"""Pydantic response models for Business Information Entity endpoints."""

from __future__ import annotations

from typing import Annotated, Literal

from pydantic import BaseModel, ConfigDict, Field

from app.routes.models.core_component import (
    AccInfoRecord,
    AsccInfoRecord,
    AsccpInfoRecord,
    BccInfoRecord,
    BccpInfoRecord,
    DataTypeSupplementaryComponentRecord,
)
from app.routes.models.shared import BizCtxSummaryRecord
from app.routes.models.shared import LibrarySummaryRecord
from app.routes.models.shared import ReleaseSummaryRecord
from app.routes.models.shared import UserSummary
from app.routes.models.shared import ValueConstraintRecord
from app.routes.models.shared import WhoAndWhen


class PrimitiveRestrictionRecord(BaseModel):
    """Represent PrimitiveRestrictionRecord."""
    xbtManifestId: int | None = None
    codeListManifestId: int | None = None
    agencyIdListManifestId: int | None = None

    model_config = ConfigDict(frozen=True)


class FacetRecord(BaseModel):
    """Represent FacetRecord."""
    facet_min_length: int | None = None
    facet_max_length: int | None = None
    facet_pattern: str | None = None

    model_config = ConfigDict(frozen=True)


class TopLevelAsbiepInfoRecord(BaseModel):
    """Represent TopLevelAsbiepInfoRecord."""
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
    """Represent AsbieRelationshipRecord."""
    component_type: Literal["ASBIE"] = Field(default="ASBIE", description="Relationship type discriminator.")
    asbie_id: int | None = Field(default=None, description="ASBIE identifier.")
    guid: str | None = Field(default=None, description="ASBIE GUID.")
    based_ascc: AsccInfoRecord = Field(..., description="Based ASCC information.")
    to_asbiep_id: int | None = Field(default=None, description="Target ASBIEP identifier.")
    is_used: bool = Field(..., description="Whether profiled/used.")
    path: str = Field(..., description="Relationship path.")
    hash_path: str = Field(..., description="Relationship hash path.")
    cardinality_min: int = Field(..., description="Minimum cardinality.")
    cardinality_max: int = Field(..., description="Maximum cardinality.")
    cardinality_display: str = Field(..., description="Human-readable cardinality display.")
    is_nillable: bool = Field(..., description="Nillable indicator.")
    remark: str | None = Field(default=None, description="Relationship remark.")

    model_config = ConfigDict(frozen=True)


class BbieRelationshipRecord(BaseModel):
    """Represent BbieRelationshipRecord."""
    component_type: Literal["BBIE"] = Field(default="BBIE", description="Relationship type discriminator.")
    bbie_id: int | None = Field(default=None, description="BBIE identifier.")
    guid: str | None = Field(default=None, description="BBIE GUID.")
    based_bcc: BccInfoRecord = Field(..., description="Based BCC information.")
    to_bbiep_id: int | None = Field(default=None, description="Target BBIEP identifier.")
    is_used: bool = Field(..., description="Whether profiled/used.")
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
    """Represent AbieInfoRecord."""
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
    """Represent AsbiepInfoRecord."""
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
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord | None = Field(default=None,
                                                                    description="Owning top-level ASBIEP.")
    created: WhoAndWhen | None = Field(default=None, description="Creation metadata.")
    last_updated: WhoAndWhen | None = Field(default=None, description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class TopLevelAsbiepRoleOfAbieRecord(AbieInfoRecord):
    """Represent TopLevelAsbiepRoleOfAbieRecord."""
    abie_id: int = Field(..., ge=1, description="ABIE identifier.")
    guid: str = Field(..., description="ABIE GUID.")
    path: str = Field(..., description="ABIE path.")
    hash_path: str = Field(..., description="ABIE hash path.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class CreateAsbieRoleOfAbieRecord(AbieInfoRecord):
    """Represent role-of ABIE shape returned by create-ASBIE endpoint."""

    abie_id: int = Field(..., ge=1, description="ABIE identifier.")
    guid: str = Field(..., description="ABIE GUID.")
    path: str = Field(..., description="ABIE path.")
    hash_path: str = Field(..., description="ABIE hash path.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class TopLevelAsbiepAbieInfoRecord(BaseModel):
    """Represent AbieInfoRecord."""
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
    """Represent TopLevelAsbiepInfoAsbiepRecord."""
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
    """Represent ASBIEP shape returned by create-ASBIE endpoint."""

    asbiep_id: int = Field(..., ge=1, description="ASBIEP identifier.")
    role_of_abie: CreateAsbieRoleOfAbieRecord = Field(..., description="Role-of ABIE information.")
    path: str = Field(..., description="ASBIEP path.")
    hash_path: str = Field(..., description="ASBIEP hash path.")
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord = Field(..., description="Owning top-level ASBIEP.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class CreateTopLevelAsbiepAsbiepRecord(AsbiepInfoRecord):
    """Represent ASBIEP shape returned by create-top-level endpoint."""

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
    """Represent BbieScInfoRecord."""
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
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord | None = Field(default=None,
                                                                    description="Owning top-level ASBIEP.")

    model_config = ConfigDict(frozen=True)


class BbiepInfoRecord(BaseModel):
    """Represent BbiepInfoRecord."""
    bbiep_id: int | None = Field(default=None, description="BBIEP identifier.")
    guid: str | None = Field(default=None, description="BBIEP GUID.")
    based_bccp: BccpInfoRecord = Field(..., description="Based BCCP information.")
    path: str | None = Field(default=None, description="BBIEP path.")
    hash_path: str | None = Field(default=None, description="BBIEP hash path.")
    definition: str | None = Field(default=None, description="BBIEP definition.")
    remark: str | None = Field(default=None, description="BBIEP remark.")
    biz_term: str | None = Field(default=None, description="Business term.")
    display_name: str | None = Field(default=None, description="Display name.")
    supplementary_components: list[BbieScInfoRecord] = Field(default_factory=list,
                                                             description="Supplementary components.")
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord | None = Field(default=None,
                                                                    description="Owning top-level ASBIEP.")
    created: WhoAndWhen | None = Field(default=None, description="Creation metadata.")
    last_updated: WhoAndWhen | None = Field(default=None, description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class CreateBbieBbiepRecord(BbiepInfoRecord):
    """Represent BBIEP shape returned by create-BBIE endpoint."""

    bbiep_id: int = Field(..., ge=1, description="BBIEP identifier.")
    guid: str = Field(..., description="BBIEP GUID.")
    path: str = Field(..., description="BBIEP path.")
    hash_path: str = Field(..., description="BBIEP hash path.")
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord = Field(..., description="Owning top-level ASBIEP.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class TopLevelAsbiepListEntry(BaseModel):
    """API representation of a top-level ASBIEP list row."""

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
    business_contexts: list[BizCtxSummaryRecord] = Field(default_factory=list,
                                                         description="Assigned business contexts.")
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
    """Represent GetTopLevelAsbiepResponse."""
    top_level_asbiep_id: int = Field(..., ge=1, description="Top-level ASBIEP identifier.")
    asbiep: TopLevelAsbiepInfoAsbiepRecord = Field(..., description="ASBIEP information.")
    version: str | None = Field(default=None, description="Version.")
    status: str | None = Field(default=None, description="Status.")
    business_contexts: list[BizCtxSummaryRecord] = Field(default_factory=list,
                                                         description="Assigned business contexts.")
    state: str = Field(..., description="Lifecycle state.")
    is_deprecated: bool = Field(..., description="Deprecation flag.")
    deprecated_reason: str | None = Field(default=None, description="Deprecation reason.")
    deprecated_remark: str | None = Field(default=None, description="Deprecation remark.")
    owner: UserSummary = Field(..., description="Owner information.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class GetAsbieByAsbieIdResponse(BaseModel):
    """Represent GetAsbieByAsbieIdResponse."""

    asbie_id: int = Field(..., description="ASBIE identifier.")
    guid: str = Field(..., description="ASBIE GUID.")
    based_ascc: AsccInfoRecord = Field(..., description="Based ASCC information.")
    to_asbiep: AsbiepInfoRecord = Field(..., description="Target ASBIEP.")
    is_used: bool = Field(..., description="Whether profiled/used.")
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
    """Represent GetAsbieByBasedAsccManifestIdResponse."""

    asbie_id: int | None = Field(default=None, description="ASBIE identifier.")
    guid: str | None = Field(default=None, description="ASBIE GUID.")
    based_ascc: AsccInfoRecord = Field(..., description="Based ASCC information.")
    to_asbiep: AsbiepInfoRecord = Field(..., description="Target ASBIEP.")
    is_used: bool = Field(..., description="Whether profiled/used.")
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
    """Represent GetBbieByBbieIdResponse."""

    bbie_id: int = Field(..., description="BBIE identifier.")
    guid: str = Field(..., description="BBIE GUID.")
    based_bcc: BccInfoRecord = Field(..., description="Based BCC information.")
    to_bbiep: BbiepInfoRecord = Field(..., description="Target BBIEP.")
    is_used: bool = Field(..., description="Whether profiled/used.")
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
    """Represent GetBbieByBasedBccManifestIdResponse."""

    bbie_id: int | None = Field(default=None, description="BBIE identifier.")
    guid: str | None = Field(default=None, description="BBIE GUID.")
    based_bcc: BccInfoRecord = Field(..., description="Based BCC information.")
    to_bbiep: BbiepInfoRecord = Field(..., description="Target BBIEP.")
    is_used: bool = Field(..., description="Whether profiled/used.")
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


class CreateTopLevelASBIEPRequest(BaseModel):
    """Request payload for creating a top-level ASBIEP."""

    asccp_manifest_id: int = Field(..., ge=1, description="ASCCP manifest identifier for the new top-level ASBIEP.")
    biz_ctx_list: list[int] = Field(..., min_length=1, description="Business context IDs to assign.")

    model_config = ConfigDict(frozen=True)


class CreateTopLevelASBIEPResponse(BaseModel):
    """Response payload for creating a top-level ASBIEP."""

    top_level_asbiep_id: int = Field(..., ge=1, description="Created top-level ASBIEP identifier.")
    asbiep: CreateTopLevelAsbiepAsbiepRecord = Field(..., description="Created ASBIEP structure.")

    model_config = ConfigDict(frozen=True)


class UpdateTopLevelASBIEPRequest(BaseModel):
    """Request payload for updating top-level ASBIEP fields."""

    version: str | None = Field(default=None, description="Version value.")
    status: str | None = Field(default=None, description="Status value.")
    display_name: str | None = Field(default=None, description="Display name override.")
    biz_term: str | None = Field(default=None, description="Business term override.")
    definition: str | None = Field(default=None, description="ASBIEP definition override.")
    remark: str | None = Field(default=None, description="ASBIEP remark override.")
    is_deprecated: bool | None = Field(default=None, description="Deprecation flag.")
    deprecated_reason: str | None = Field(default=None, description="Deprecation reason.")
    deprecated_remark: str | None = Field(default=None, description="Deprecation remark.")

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "definition": "Updated profile definition.",
            }
        },
    )


class UpdateTopLevelASBIEPStateRequest(BaseModel):
    """Request payload for top-level ASBIEP state transition."""

    state: str = Field(..., description="Target state.")

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "state": "Published",
            }
        },
    )


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


class DeleteTopLevelAsbiepRequest(BaseModel):
    """Request model for deleting a top-level ASBIEP."""

    top_level_asbiep_id: int = Field(..., ge=1, description="Top-level ASBIEP identifier.")

    model_config = ConfigDict(frozen=True)


class TransferTopLevelASBIEPOwnershipRequest(BaseModel):
    """Request payload for top-level ASBIEP ownership transfer."""

    target_user_id: int = Field(..., ge=1, description="Target owner user ID.")

    model_config = ConfigDict(frozen=True)


class TransferTopLevelASBIEPOwnershipResponse(BaseModel):
    """Response payload for top-level ASBIEP ownership transfer."""

    top_level_asbiep_id: int = Field(..., ge=1, description="Target top-level ASBIEP identifier.")

    model_config = ConfigDict(frozen=True)


class AssignBizCtxToTopLevelAsbiepRequest(BaseModel):
    """Request payload for assigning a business context."""

    biz_ctx_id: int = Field(..., ge=1, description="Business context ID.")

    model_config = ConfigDict(frozen=True)


class AssignBizCtxToTopLevelAsbiepResponse(BaseModel):
    """Response payload for assigning a business context."""

    top_level_asbiep_id: int = Field(..., ge=1, description="Target top-level ASBIEP identifier.")

    model_config = ConfigDict(frozen=True)


class UnassignBizCtxRequest(BaseModel):
    """Request payload for unassigning a business context."""

    biz_ctx_id: int = Field(..., ge=1, description="Business context ID.")

    model_config = ConfigDict(frozen=True)


class CreateASBIERequest(BaseModel):
    """Request payload for creating an ASBIE."""

    from_abie_id: int = Field(..., ge=1, description="Parent ABIE identifier.")
    based_ascc_manifest_id: int = Field(..., ge=1, description="Based ASCC manifest identifier.")

    model_config = ConfigDict(frozen=True)


class CreateASBIEResponse(BaseModel):
    """Response payload for creating an ASBIE."""

    asbie_id: int = Field(..., ge=1, description="Created ASBIE identifier.")
    asbiep: CreateAsbieAsbiepRecord = Field(..., description="Resolved ASBIEP details.")

    model_config = ConfigDict(frozen=True)


class UpdateASBIERequest(BaseModel):
    """Request payload for updating an ASBIE."""

    is_used: bool | None = Field(default=None, description="Whether this ASBIE is profiled.")
    is_nillable: bool | None = Field(default=None, description="Nillable indicator.")
    definition: str | None = Field(default=None, description="Definition text for this ASBIE.")
    cardinality_min: int | None = Field(default=None, ge=0, description="Minimum cardinality.")
    cardinality_max: int | None = Field(default=None, description="Maximum cardinality (`-1` for unbounded).")
    remark: str | None = Field(default=None, description="Remark text for the ASBIE property.")

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "definition": "Updated ASBIE definition.",
            }
        },
    )


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


class CreateBBIERequest(BaseModel):
    """Request payload for creating a BBIE."""

    from_abie_id: int = Field(..., ge=1, description="Parent ABIE identifier.")
    based_bcc_manifest_id: int = Field(..., ge=1, description="Based BCC manifest identifier.")

    model_config = ConfigDict(frozen=True)


class CreateBBIEResponse(BaseModel):
    """Response payload for creating a BBIE."""

    bbie_id: int = Field(..., ge=1, description="Created BBIE identifier.")
    bbiep: CreateBbieBbiepRecord = Field(..., description="Resolved BBIEP details.")

    model_config = ConfigDict(frozen=True)


class UpdateBBIERequest(BaseModel):
    """Request payload for updating a BBIE."""

    is_used: bool | None = Field(default=None, description="Whether this BBIE is profiled.")
    is_nillable: bool | None = Field(default=None, description="Nillable indicator.")
    cardinality_min: int | None = Field(default=None, ge=0, description="Minimum cardinality.")
    cardinality_max: int | None = Field(default=None, description="Maximum cardinality (`-1` for unbounded).")
    definition: str | None = Field(default=None, description="Definition text for this BBIE.")
    example: str | None = Field(default=None, description="Illustrative example value or content for this BBIE.")
    remark: str | None = Field(default=None, description="Remark text for the BBIE property.")
    default_value: str | None = Field(default=None, description="Default value.")
    fixed_value: str | None = Field(default=None, description="Fixed value.")
    facet_min_length: int | None = Field(default=None, ge=0, description="Facet min length.")
    facet_max_length: int | None = Field(default=None, ge=0, description="Facet max length.")
    facet_pattern: str | None = Field(default=None, description="Facet pattern (regular expression).")
    xbt_manifest_id: int | None = Field(default=None, ge=1, description="XBT manifest identifier to use as the primitive restriction for this BBIE.")
    code_list_manifest_id: int | None = Field(default=None, ge=1, description="Code-list manifest identifier to use as the primitive restriction for this BBIE.")
    agency_id_list_manifest_id: int | None = Field(default=None, ge=1, description="Agency-ID-list manifest identifier to use as the primitive restriction for this BBIE.")

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "fixed_value": "FIXED",
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


class CreateBBIESCRequest(BaseModel):
    """Request payload for creating a BBIE_SC."""

    bbie_id: int = Field(..., ge=1, description="Parent BBIE identifier.")
    based_dt_sc_manifest_id: int = Field(..., ge=1, description="Based DT_SC manifest identifier.")

    model_config = ConfigDict(frozen=True)


class CreateBBIESCResponse(BaseModel):
    """Response payload for creating a BBIE_SC."""

    bbie_sc_id: int = Field(..., ge=1, description="Created BBIE_SC identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.")

    model_config = ConfigDict(frozen=True)


class UpdateBBIESCRequest(BaseModel):
    """Request payload for updating a BBIE_SC."""

    is_used: bool | None = Field(default=None, description="Whether this BBIE_SC is profiled.")
    cardinality_min: int | None = Field(default=None, ge=0, description="Minimum cardinality.")
    cardinality_max: int | None = Field(default=None, description="Maximum cardinality (`-1` for unbounded).")
    definition: str | None = Field(default=None, description="Definition text for this BBIE supplementary component.")
    example: str | None = Field(default=None, description="Illustrative example value or content for this BBIE supplementary component.")
    remark: str | None = Field(default=None, description="Remark text for this BBIE supplementary component.")
    biz_term: str | None = Field(default=None, description="Business term override.")
    display_name: str | None = Field(default=None, description="Display name override.")
    default_value: str | None = Field(default=None, description="Default value.")
    fixed_value: str | None = Field(default=None, description="Fixed value.")
    facet_min_length: int | None = Field(default=None, ge=0, description="Facet min length.")
    facet_max_length: int | None = Field(default=None, ge=0, description="Facet max length.")
    facet_pattern: str | None = Field(default=None, description="Facet pattern (regular expression).")
    xbt_manifest_id: int | None = Field(default=None, ge=1, description="XBT manifest identifier to use as the primitive restriction for this BBIE supplementary component.")
    code_list_manifest_id: int | None = Field(default=None, ge=1, description="Code-list manifest identifier to use as the primitive restriction for this BBIE supplementary component.")
    agency_id_list_manifest_id: int | None = Field(default=None, ge=1, description="Agency-ID-list manifest identifier to use as the primitive restriction for this BBIE supplementary component.")

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "facet_pattern": "^[A-Z]+$",
            }
        },
    )


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


class ReuseTopLevelASBIEPRequest(BaseModel):
    """Request payload for reusing a top-level ASBIEP in an ASBIE."""

    top_level_asbiep_id: int | None = Field(default=None, ge=1, description="Top-level ASBIEP identifier in the URL.")
    asbie_id: int = Field(..., ge=1, description="ASBIE identifier.")
    reuse_top_level_asbiep_id: int = Field(..., ge=1, description="Top-level ASBIEP identifier to reuse.")

    model_config = ConfigDict(frozen=True)


class ReuseTopLevelASBIEPResponse(BaseModel):
    """Response payload for reusing a top-level ASBIEP in an ASBIE."""

    asbie_id: int = Field(..., ge=1, description="Target ASBIE identifier.")
    reused_asbiep_id: int = Field(..., ge=1, description="Reused ASBIEP identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.")

    model_config = ConfigDict(frozen=True)


class RemoveReusedTopLevelASBIEPRequest(BaseModel):
    """Request payload for removing a reused top-level ASBIEP from an ASBIE."""

    top_level_asbiep_id: int | None = Field(default=None, ge=1, description="Top-level ASBIEP identifier in the URL.")
    asbie_id: int = Field(..., ge=1, description="ASBIE identifier.")

    model_config = ConfigDict(frozen=True)


class RemoveReusedTopLevelASBIEPResponse(BaseModel):
    """Response payload for removing a reused top-level ASBIEP from an ASBIE."""

    asbie_id: int = Field(..., ge=1, description="Target ASBIE identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.")

    model_config = ConfigDict(frozen=True)
