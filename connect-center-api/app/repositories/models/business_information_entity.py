"""Repository row models for Business Information Entity resources."""


from __future__ import annotations

from datetime import datetime
from typing import Annotated, Literal

from pydantic import BaseModel, ConfigDict, Field

from app.repositories.models.core_component import (
    AccInfoRow,
    AsccInfoRow,
    AsccpInfoRow,
    BccInfoRow,
    BccpInfoRow,
    ValueConstraintRow,
)
from app.repositories.models.data_type import DataTypeSupplementaryComponentRow
from app.repositories.models.library import LibrarySummaryRow
from app.repositories.models.release import ReleaseSummaryRow


class BizCtxSummaryRow(BaseModel):
    """Minimal business-context summary for BIE list rows."""

    biz_ctx_id: int
    guid: str
    name: str

    model_config = ConfigDict(frozen=True, from_attributes=True)


class PrimitiveRestrictionRow(BaseModel):
    """Primitive restriction selection."""

    xbt_manifest_id: int | None = None
    code_list_manifest_id: int | None = None
    agency_id_list_manifest_id: int | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class FacetRow(BaseModel):
    """Facet restrictions for string-like values."""

    facet_min_length: int | None = None
    facet_max_length: int | None = None
    facet_pattern: str | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class TopLevelAsbiepInfoRow(BaseModel):
    """Top-level ASBIEP summary used by detail responses."""

    top_level_asbiep_id: int
    library: LibrarySummaryRow
    release: ReleaseSummaryRow
    version: str | None = None
    status: str | None = None
    state: str | None = None
    is_deprecated: bool
    deprecated_reason: str | None = None
    deprecated_remark: str | None = None
    owner_user_id: int
    created_by: int | None = None
    creation_timestamp: datetime | None = None
    last_updated_by: int | None = None
    last_update_timestamp: datetime | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class AsbieRelationshipRow(BaseModel):
    """ASBIE relationship in ABIE relationship list."""

    component_type: Literal["ASBIE"] = "ASBIE"
    asbie_id: int | None = None
    guid: str | None = None
    based_ascc: AsccInfoRow
    to_asbiep_id: int | None = None
    is_used: bool
    path: str
    hash_path: str
    cardinality_min: int
    cardinality_max: int
    is_nillable: bool
    remark: str | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class BbieRelationshipRow(BaseModel):
    """BBIE relationship in ABIE relationship list."""

    component_type: Literal["BBIE"] = "BBIE"
    bbie_id: int | None = None
    guid: str | None = None
    based_bcc: BccInfoRow
    to_bbiep_id: int | None = None
    is_used: bool
    path: str
    hash_path: str
    cardinality_min: int
    cardinality_max: int
    is_nillable: bool
    remark: str | None = None
    primitive_restriction: PrimitiveRestrictionRow | None = None
    value_constraint: ValueConstraintRow | None = None
    facet: FacetRow | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


RelationshipRow = Annotated[AsbieRelationshipRow | BbieRelationshipRow, Field(discriminator="component_type")]


class AbieInfoRow(BaseModel):
    """ABIE information with flattened relationships."""

    abie_id: int | None = None
    guid: str | None = None
    path: str | None = None
    hash_path: str | None = None
    based_acc_manifest: AccInfoRow
    definition: str | None = None
    remark: str | None = None
    relationships: list[RelationshipRow] = Field(default_factory=list)
    created_by: int | None = None
    creation_timestamp: datetime | None = None
    last_updated_by: int | None = None
    last_update_timestamp: datetime | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class AsbiepInfoRow(BaseModel):
    """ASBIEP detail with role-of ABIE and relationship list."""

    asbiep_id: int | None = None
    guid: str | None = None
    owner_top_level_asbiep: TopLevelAsbiepInfoRow | None = None
    based_asccp_manifest: AsccpInfoRow
    path: str | None = None
    hash_path: str | None = None
    role_of_abie: AbieInfoRow
    definition: str | None = None
    remark: str | None = None
    biz_term: str | None = None
    display_name: str | None = None
    created_by: int | None = None
    creation_timestamp: datetime | None = None
    last_updated_by: int | None = None
    last_update_timestamp: datetime | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class BbieScInfoRow(BaseModel):
    """BBIE_SC details inside BBIEP."""

    bbie_sc_id: int | None = None
    guid: str | None = None
    based_dt_sc: DataTypeSupplementaryComponentRow
    path: str
    hash_path: str
    definition: str | None = None
    biz_term: str | None = None
    display_name: str | None = None
    cardinality_min: int
    cardinality_max: int
    primitive_restriction: PrimitiveRestrictionRow | None = None
    value_constraint: ValueConstraintRow | None = None
    facet: FacetRow | None = None
    owner_top_level_asbiep: TopLevelAsbiepInfoRow | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class BbiepInfoRow(BaseModel):
    """BBIEP details for BBIE response."""

    bbiep_id: int | None = None
    guid: str | None = None
    based_bccp: BccpInfoRow
    path: str
    hash_path: str
    definition: str | None = None
    remark: str | None = None
    biz_term: str | None = None
    display_name: str | None = None
    supplementary_components: list[BbieScInfoRow] = Field(default_factory=list)
    owner_top_level_asbiep: TopLevelAsbiepInfoRow | None = None
    created_by: int | None = None
    creation_timestamp: datetime | None = None
    last_updated_by: int | None = None
    last_update_timestamp: datetime | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class TopLevelAsbiepListRow(BaseModel):
    """Repository row model for top-level ASBIEP list results."""

    top_level_asbiep_id: int
    asbiep_id: int
    guid: str
    den: str | None = None
    property_term: str | None = None
    display_name: str | None = None
    version: str | None = None
    status: str | None = None
    biz_term: str | None = None
    remark: str | None = None
    business_contexts: list[BizCtxSummaryRow] = Field(default_factory=list)
    state: str | None = None
    is_deprecated: bool
    deprecated_reason: str | None = None
    deprecated_remark: str | None = None
    library: LibrarySummaryRow
    release: ReleaseSummaryRow
    owner_user_id: int
    created_by: int
    last_updated_by: int
    creation_timestamp: datetime
    last_update_timestamp: datetime

    model_config = ConfigDict(frozen=True, from_attributes=True)


class TopLevelAsbiepDetailRow(BaseModel):
    """Repository row model for top-level ASBIEP detail."""

    top_level_asbiep_id: int
    asbiep: AsbiepInfoRow
    version: str | None = None
    status: str | None = None
    business_contexts: list[BizCtxSummaryRow] = Field(default_factory=list)
    state: str | None = None
    is_deprecated: bool
    deprecated_reason: str | None = None
    deprecated_remark: str | None = None
    owner_user_id: int
    created_by: int
    creation_timestamp: datetime
    last_updated_by: int
    last_update_timestamp: datetime

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetAsbieRow(BaseModel):
    """Repository row model for get-asbie methods."""

    asbie_id: int | None = None
    owner_top_level_asbiep: TopLevelAsbiepInfoRow
    guid: str | None = None
    based_ascc: AsccInfoRow
    to_asbiep: AsbiepInfoRow
    is_used: bool
    cardinality_min: int
    cardinality_max: int
    is_nillable: bool
    definition: str | None = None
    remark: str | None = None
    created_by: int | None = None
    creation_timestamp: datetime | None = None
    last_updated_by: int | None = None
    last_update_timestamp: datetime | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetBbieRow(BaseModel):
    """Repository row model for get-bbie methods."""

    bbie_id: int | None = None
    owner_top_level_asbiep: TopLevelAsbiepInfoRow
    guid: str | None = None
    based_bcc: BccInfoRow
    to_bbiep: BbiepInfoRow
    is_used: bool
    cardinality_min: int
    cardinality_max: int
    is_nillable: bool
    remark: str | None = None
    primitive_restriction: PrimitiveRestrictionRow | None = None
    value_constraint: ValueConstraintRow | None = None
    facet: FacetRow | None = None
    created_by: int | None = None
    creation_timestamp: datetime | None = None
    last_updated_by: int | None = None
    last_update_timestamp: datetime | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class AsbieCreatePlanRow(BaseModel):
    """Plan row for creating/enabling ASBIE."""

    top_level_asbiep_id: int
    top_level_owner_user_id: int
    top_level_state: str
    from_abie_based_acc_manifest_id: int
    ascc_manifest_from_acc_manifest_id: int
    asccp_manifest_id: int
    role_of_acc_manifest_id: int
    ascc_cardinality_min: int
    ascc_cardinality_max: int
    asbie_path: str
    asbie_hash_path: str
    existing_asbie_id: int | None = None
    existing_to_asbiep_id: int | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class AsbiepRolePlanRow(BaseModel):
    """Compatibility metadata for existing ASBIEP target checks."""

    based_asccp_manifest_id: int
    role_based_acc_manifest_id: int

    model_config = ConfigDict(frozen=True, from_attributes=True)


class BbieCreatePlanRow(BaseModel):
    """Plan row for creating/enabling BBIE."""

    top_level_asbiep_id: int
    top_level_owner_user_id: int
    top_level_state: str
    from_abie_based_acc_manifest_id: int
    bcc_manifest_from_acc_manifest_id: int
    bccp_manifest_id: int
    bcc_cardinality_min: int
    bcc_cardinality_max: int
    bcc_is_nillable: bool
    bcc_default_value: str | None = None
    bcc_fixed_value: str | None = None
    primitive_xbt_manifest_id: int | None = None
    primitive_code_list_manifest_id: int | None = None
    primitive_agency_id_list_manifest_id: int | None = None
    bbie_path: str
    bbie_hash_path: str
    existing_bbie_id: int | None = None
    existing_to_bbiep_id: int | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class BbiepPlanRow(BaseModel):
    """Compatibility metadata for existing BBIEP target checks."""

    based_bccp_manifest_id: int

    model_config = ConfigDict(frozen=True, from_attributes=True)


AbieInfoRow.model_rebuild()
AsbiepInfoRow.model_rebuild()
TopLevelAsbiepDetailRow.model_rebuild()
GetAsbieRow.model_rebuild()
GetBbieRow.model_rebuild()
