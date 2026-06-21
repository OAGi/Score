"""Service models for data-type operations."""

from __future__ import annotations

from typing import Literal

from dataclasses import dataclass, field

from app.services.models import WhoAndWhen
from app.services.models.app_user import UserSummary
from app.services.models.library import LibrarySummaryServiceRecord
from app.services.models.log import LogSummaryServiceRecord
from app.services.models.namespace import NamespaceSummaryServiceRecord
from app.services.models.release import ReleaseSummaryServiceRecord
from app.services.models.tag import TagSummaryServiceRecord
from app.services.utils.date import DateRange
from app.services.utils.pagination import PaginationParams
from app.services.utils.string import Guid
from app.types.identifiers import DataTypeId, DataTypeManifestId, DataTypeSupplementaryComponentId
from app.types.identifiers import DataTypeSupplementaryComponentManifestId
from app.types.identifiers import ReleaseId


class DataTypeServiceParams:
    """Parameters for listing data types."""

    def __init__(
        self,
        *,
        release_id: ReleaseId,
        pagination: PaginationParams,
        den: str | None = None,
        representation_term: str | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
    ) -> None:
        self.release_id = release_id
        self.pagination = pagination
        self.den = den
        self.representation_term = representation_term
        self.created_on = created_on
        self.last_updated_on = last_updated_on


@dataclass(kw_only=True)
class DataTypeServiceResult:
    """Data type information with supplementary components."""

    dt_manifest_id: DataTypeManifestId
    dt_id: DataTypeId
    base_dt: DataTypeBaseSummaryServiceRecord | None = None
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
    state: Literal[
        "Deleted",
        "WIP",
        "Draft",
        "QA",
        "Candidate",
        "Production",
        "ReleaseDraft",
        "Published",
    ]
    primitives: list[DataTypePrimitiveServiceRecord] = field(default_factory=list)
    supplementary_components: list[DataTypeSupplementaryComponentServiceRecord] = field(default_factory=list)
    tags: list[TagSummaryServiceRecord] = field(default_factory=list)
    namespace: NamespaceSummaryServiceRecord | None = None
    library: LibrarySummaryServiceRecord
    release: ReleaseSummaryServiceRecord
    log: LogSummaryServiceRecord | None = None
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen


@dataclass(kw_only=True)
class DataTypeBaseSummaryServiceRecord:
    """Summary information for a base data type."""

    dt_manifest_id: DataTypeManifestId
    dt_id: DataTypeId
    based_dt_manifest_id: DataTypeManifestId | None = None
    guid: Guid
    den: str
    data_type_term: str | None = None
    qualifier: str | None = None
    representation_term: str | None = None
    six_digit_id: str | None = None
    definition: str | None = None
    definition_source: str | None = None
    content_component_definition: str | None = None
    is_deprecated: bool
    namespace: NamespaceSummaryServiceRecord | None = None
    library: LibrarySummaryServiceRecord
    release: ReleaseSummaryServiceRecord


@dataclass(kw_only=True)
class DataTypeValueConstraintServiceRecord:
    """Value constraint for a supplementary component."""

    default_value: str | None = None
    fixed_value: str | None = None


@dataclass(kw_only=True)
class DataTypePrimitiveServiceRecord:
    """Primitive selection for a DT or DT_SC."""

    cdt_pri_name: str | None = None
    xbt_manifest_id: int | None = None
    code_list_manifest_id: int | None = None
    agency_id_list_manifest_id: int | None = None
    is_default: bool


@dataclass(kw_only=True)
class DataTypeSupplementaryComponentServiceRecord:
    """Data type supplementary component information."""

    dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId
    dt_sc_id: DataTypeSupplementaryComponentId
    guid: Guid
    object_class_term: str | None = None
    property_term: str | None = None
    representation_term: str | None = None
    definition: str | None = None
    definition_source: str | None = None
    cardinality_min: int
    cardinality_max: int
    value_constraint: DataTypeValueConstraintServiceRecord | None = None
    is_deprecated: bool
    primitives: list[DataTypePrimitiveServiceRecord] = field(default_factory=list)

    @property
    def cardinality(self) -> Literal["Prohibited", "Optional", "Required"]:
        """Return the score-web style DT_SC cardinality label."""
        if self.cardinality_min == 0 and self.cardinality_max == 0:
            return "Prohibited"
        if self.cardinality_min == 0 and self.cardinality_max == 1:
            return "Optional"
        return "Required"


@dataclass(kw_only=True)
class DataTypeSummaryServiceRecord:
    """Data type summary used in BCCP relationship payloads."""

    dt_manifest_id: DataTypeManifestId
    dt_id: DataTypeId
    based_dt_manifest_id: DataTypeManifestId | None = None
    guid: Guid
    den: str
    data_type_term: str | None = None
    qualifier: str | None = None
    representation_term: str | None = None
    six_digit_id: str | None = None
    definition: str | None = None
    definition_source: str | None = None
    content_component_definition: str | None = None
    is_deprecated: bool
    namespace: NamespaceSummaryServiceRecord | None = None
    library: LibrarySummaryServiceRecord
    release: ReleaseSummaryServiceRecord


@dataclass(kw_only=True)
class CreateDataTypeServiceResult:
    """Data-type create response model."""

    dt_manifest_id: DataTypeManifestId


@dataclass(kw_only=True)
class UpdateDataTypeServiceResult:
    """Data-type update response model."""

    dt_manifest_id: DataTypeManifestId
    updates: list[str] = field(default_factory=list)


@dataclass(kw_only=True)
class TransferDataTypeOwnershipServiceResult:
    """Data-type ownership-transfer response model."""

    dt_manifest_id: DataTypeManifestId
    updates: list[str] = field(default_factory=list)


@dataclass(kw_only=True)
class CreateDataTypeSupplementaryComponentServiceResult:
    """Data-type supplementary-component create response model."""

    dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId


@dataclass(kw_only=True)
class UpdateDataTypeSupplementaryComponentServiceResult:
    """Data-type supplementary-component update response model."""

    dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId
    updates: list[str] = field(default_factory=list)


@dataclass(kw_only=True)
class DiscardDataTypeServiceResult:
    """Data-type discard response model."""

    dt_manifest_id: DataTypeManifestId
    discarded: bool


@dataclass(kw_only=True)
class ReviseDataTypeServiceResult:
    """Data-type revise response model."""

    dt_manifest_id: DataTypeManifestId
    revised: bool


@dataclass(kw_only=True)
class CancelDataTypeServiceResult:
    """Data-type cancel response model."""

    dt_manifest_id: DataTypeManifestId
    cancelled: bool
