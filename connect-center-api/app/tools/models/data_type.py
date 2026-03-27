"""Models for Data Type MCP tools."""

from __future__ import annotations

from pydantic import BaseModel, ConfigDict, Field

from app.services.utils.string import Guid
from app.tools.models.shared import LibrarySummaryRecord
from app.tools.models.shared import LogSummaryRecord
from app.tools.models.shared import NamespaceSummaryRecord
from app.tools.models.shared import ReleaseSummaryRecord
from app.tools.models.shared import UserSummary
from app.tools.models.shared import ValueConstraintRecord
from app.tools.models.shared import WhoAndWhen


class DataTypeBaseResponse(BaseModel):
    """Base data-type payload for MCP tools."""

    dt_manifest_id: int
    dt_id: int
    based_dt_manifest_id: int | None = None
    guid: Guid
    den: str
    data_type_term: str | None = None
    qualifier: str | None = None
    representation_term: str | None = None
    six_digit_id: str | None = None
    definition: str | None = None
    definition_source: str | None = None
    content_component_definition: str | None = None
    is_deprecated: bool = False
    namespace: NamespaceSummaryRecord | None = None
    library: LibrarySummaryRecord
    release: ReleaseSummaryRecord

    model_config = ConfigDict(frozen=True, from_attributes=True)


class DataTypeSupplementaryComponentResponse(BaseModel):
    """Supplementary-component payload for MCP tools."""

    dt_sc_manifest_id: int
    dt_sc_id: int
    guid: Guid
    object_class_term: str | None = None
    property_term: str | None = None
    representation_term: str | None = None
    definition: str | None = None
    definition_source: str | None = None
    cardinality_min: int
    cardinality_max: int = 0
    value_constraint: ValueConstraintRecord | None = None
    is_deprecated: bool

    model_config = ConfigDict(frozen=True, from_attributes=True)


class DataTypeResponseEntry(BaseModel):
    """Data-type payload for MCP tools."""

    dt_manifest_id: int
    dt_id: int
    base_dt: DataTypeBaseResponse | None = None
    guid: Guid
    den: str
    data_type_term: str | None = None
    qualifier: str | None = None
    representation_term: str | None = None
    six_digit_id: str | None = None
    definition: str | None = None
    definition_source: str | None = None
    content_component_definition: str | None = None
    commonly_used: bool
    is_deprecated: bool
    state: str | None = None
    supplementary_components: list[DataTypeSupplementaryComponentResponse]
    namespace: NamespaceSummaryRecord | None = None
    library: LibrarySummaryRecord
    release: ReleaseSummaryRecord
    log: LogSummaryRecord | None = None
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetDataTypeResponse(DataTypeResponseEntry):
    """Response for get_data_type tool."""


class GetDataTypePaginationResponse(BaseModel):
    """Response for get_data_types tool."""

    total_items: int
    offset: int
    limit: int
    items: list[DataTypeResponseEntry]

    model_config = ConfigDict(frozen=True)
