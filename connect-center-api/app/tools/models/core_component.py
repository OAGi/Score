"""Models for Core Component MCP tools."""

from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, ConfigDict, Field

from app.tools.models.shared import LibrarySummaryRecord
from app.tools.models.shared import LogSummaryRecord
from app.tools.models.shared import NamespaceSummaryRecord
from app.tools.models.shared import ReleaseSummaryRecord
from app.tools.models.shared import UserSummary
from app.tools.models.shared import ValueConstraintRecord
from app.tools.models.shared import WhoAndWhen

class CoreComponentListEntryResponse(BaseModel):
    """List payload for core-component MCP tools."""

    component_type: Literal["ACC", "ASCCP", "BCCP"]
    manifest_id: int
    component_id: int
    guid: str
    den: str
    name: str | None
    definition: str | None = None
    definition_source: str | None = None
    is_deprecated: bool
    state: str
    namespace: NamespaceSummaryRecord | None = None
    library: LibrarySummaryRecord
    release: ReleaseSummaryRecord
    log: LogSummaryRecord | None = None
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen
    tag: str | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class CoreComponentDataTypeManifestResponse(BaseModel):
    """BDT summary nested under ACC relationship payloads."""

    dt_manifest_id: int
    dt_id: int
    guid: str
    den: str
    data_type_term: str | None = None
    qualifier: str | None = None
    representation_term: str | None = None
    six_digit_id: str | None = None
    based_dt_manifest_id: int | None = None
    definition: str | None = None
    definition_source: str | None = None
    is_deprecated: bool

    model_config = ConfigDict(frozen=True, from_attributes=True)


class CoreComponentDataTypeDetailResponse(BaseModel):
    """BDT detail nested under BCCP responses."""

    dt_manifest_id: int
    dt_id: int
    guid: str
    den: str
    data_type_term: str | None = None
    qualifier: str | None = None
    representation_term: str | None = None
    six_digit_id: str | None = None
    based_dt_manifest_id: int | None = None
    definition: str | None = None
    definition_source: str | None = None
    content_component_definition: str | None = None
    is_deprecated: bool
    namespace: NamespaceSummaryRecord | None = None
    library: LibrarySummaryRecord
    release: ReleaseSummaryRecord

    model_config = ConfigDict(frozen=True, from_attributes=True)


class AccInfoResponse(BaseModel):
    """ACC summary nested under ACC relationship payloads."""

    acc_manifest_id: int
    acc_id: int
    guid: str
    den: str
    object_class_term: str
    definition: str | None = None
    definition_source: str | None = None
    is_deprecated: bool

    model_config = ConfigDict(frozen=True, from_attributes=True)


class BaseAccInfoResponse(BaseModel):
    """Base ACC payload for ACC detail responses."""

    acc_manifest_id: int
    acc_id: int
    guid: str
    den: str
    object_class_term: str
    type: str | None
    definition: str | None = None
    definition_source: str | None = None
    namespace: NamespaceSummaryRecord | None = None
    library: LibrarySummaryRecord
    release: ReleaseSummaryRecord

    model_config = ConfigDict(frozen=True, from_attributes=True)


class RoleOfAccResponse(BaseModel):
    """Role-of ACC payload for ASCCP detail responses."""

    acc_manifest_id: int
    acc_id: int
    guid: str
    den: str
    object_class_term: str
    type: str
    definition: str | None = None
    definition_source: str | None = None
    namespace: NamespaceSummaryRecord | None = None
    library: LibrarySummaryRecord
    release: ReleaseSummaryRecord

    model_config = ConfigDict(frozen=True, from_attributes=True)


class AsccpInfoResponse(BaseModel):
    """ASCCP summary nested under ACC relationship payloads."""

    asccp_manifest_id: int
    asccp_id: int
    role_of_acc_manifest_id: int
    guid: str
    den: str
    property_term: str
    definition: str | None = None
    definition_source: str | None = None
    is_deprecated: bool

    model_config = ConfigDict(frozen=True, from_attributes=True)


class AsccRelationshipResponse(BaseModel):
    """ASCC relationship nested under ACC detail responses."""

    component_type: Literal["ASCC"]
    manifest_id: int
    ascc_manifest_id: int
    ascc_id: int
    guid: str
    den: str
    cardinality_min: int
    cardinality_max: int
    cardinality_display: str
    is_deprecated: bool
    definition: str | None = None
    definition_source: str | None = None
    from_acc: AccInfoResponse
    to_asccp: AsccpInfoResponse

    model_config = ConfigDict(frozen=True, from_attributes=True)


class BccpInfoResponse(BaseModel):
    """BCCP summary nested under ACC relationship payloads."""

    bccp_manifest_id: int
    bccp_id: int
    guid: str
    den: str
    property_term: str
    representation_term: str
    definition: str | None = None
    definition_source: str | None = None
    is_deprecated: bool
    bdt_manifest: CoreComponentDataTypeManifestResponse

    model_config = ConfigDict(frozen=True, from_attributes=True)


class BccRelationshipResponse(BaseModel):
    """BCC relationship nested under ACC detail responses."""

    component_type: Literal["BCC"]
    manifest_id: int
    bcc_manifest_id: int
    bcc_id: int
    guid: str
    den: str
    cardinality_min: int
    cardinality_max: int
    cardinality_display: str
    entity_type: Literal["Attribute", "Element"] | None = None
    is_nillable: bool
    value_constraint: ValueConstraintRecord | None = None
    is_deprecated: bool
    definition: str | None = None
    definition_source: str | None = None
    from_acc: AccInfoResponse
    to_bccp: BccpInfoResponse

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetCoreComponentPaginationResponse(BaseModel):
    """Response for get_core_components tool."""

    total_items: int
    offset: int
    limit: int
    items: list[CoreComponentListEntryResponse]

    model_config = ConfigDict(frozen=True)


class GetAccResponse(BaseModel):
    """Response for get_acc tool."""

    acc_manifest_id: int
    acc_id: int
    base_acc: BaseAccInfoResponse | None = None
    relationships: list[AsccRelationshipResponse | BccRelationshipResponse]
    guid: str
    den: str
    object_class_term: str
    definition: str | None = None
    definition_source: str | None = None
    object_class_qualifier: str | None = None
    component_type: int | None = None
    is_abstract: bool
    is_deprecated: bool
    state: str | None = None
    namespace: NamespaceSummaryRecord | None = None
    library: LibrarySummaryRecord
    release: ReleaseSummaryRecord
    log: LogSummaryRecord | None = None
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetAsccpResponse(BaseModel):
    """Response for get_asccp tool."""

    asccp_manifest_id: int
    asccp_id: int
    role_of_acc: RoleOfAccResponse
    guid: str
    den: str | None = None
    property_term: str | None = None
    definition: str | None = None
    definition_source: str | None = None
    reusable_indicator: bool
    is_nillable: bool | None = None
    is_deprecated: bool
    state: str | None = None
    namespace: NamespaceSummaryRecord | None = None
    library: LibrarySummaryRecord
    release: ReleaseSummaryRecord
    log: LogSummaryRecord | None = None
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetBccpResponse(BaseModel):
    """Response for get_bccp tool."""

    bccp_manifest_id: int
    bccp_id: int
    bdt: CoreComponentDataTypeDetailResponse
    guid: str
    den: str
    property_term: str
    representation_term: str
    definition: str | None = None
    definition_source: str | None = None
    is_nillable: bool
    value_constraint: ValueConstraintRecord | None = None
    is_deprecated: bool
    state: str | None = None
    namespace: NamespaceSummaryRecord | None = None
    library: LibrarySummaryRecord
    release: ReleaseSummaryRecord
    log: LogSummaryRecord | None = None
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen

    model_config = ConfigDict(frozen=True, from_attributes=True)
