"""Repository row model for data type resources."""


from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field

from app.repositories.models.library import LibrarySummaryRow
from app.repositories.models.log import LogSummaryRow
from app.repositories.models.namespace import NamespaceSummaryRow
from app.repositories.models.release import ReleaseSummaryRow
from app.repositories.models.tag import TagSummaryRow


class DataTypeRow(BaseModel):
    """Represent DataTypeRow."""
    dt_manifest_id: int
    dt_id: int
    base_dt: DataTypeBaseSummaryRow | None = None
    guid: str
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
    supplementary_components: list[DataTypeSupplementaryComponentRow] = Field(default_factory=list)
    tags: list[TagSummaryRow] = Field(default_factory=list)
    namespace: NamespaceSummaryRow | None = None
    library: LibrarySummaryRow
    release: ReleaseSummaryRow
    log: LogSummaryRow | None = None
    owner_user_id: int
    created_by: int
    last_updated_by: int
    creation_timestamp: datetime
    last_update_timestamp: datetime

    model_config = ConfigDict(frozen=True, from_attributes=True)


class DataTypeBaseSummaryRow(BaseModel):
    """Repository summary row for base data type references."""

    dt_manifest_id: int
    dt_id: int
    based_dt_manifest_id: int | None = None
    guid: str
    den: str
    data_type_term: str | None = None
    qualifier: str | None = None
    representation_term: str | None = None
    six_digit_id: str | None = None
    definition: str | None = None
    definition_source: str | None = None
    content_component_definition: str | None = None
    is_deprecated: bool
    namespace: NamespaceSummaryRow | None = None
    library: LibrarySummaryRow
    release: ReleaseSummaryRow

    model_config = ConfigDict(frozen=True, from_attributes=True)


class DataTypeSupplementaryComponentRow(BaseModel):
    """Repository row for supplementary data-type components."""

    dt_sc_manifest_id: int
    dt_sc_id: int
    guid: str
    object_class_term: str | None = None
    property_term: str | None = None
    representation_term: str | None = None
    based_dt_sc_manifest_id: int | None = None
    dt_sc_value: str | None = None
    cardinality_min: int
    cardinality_max: int
    definition: str | None = None
    definition_source: str | None = None
    based_default_xbt_manifest_id: int | None = None
    based_default_code_list_manifest_id: int | None = None
    based_default_agency_id_list_manifest_id: int | None = None
    default_value: str | None = None
    fixed_value: str | None = None
    is_deprecated: bool

    model_config = ConfigDict(frozen=True, from_attributes=True)
