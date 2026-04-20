"""Service models for core-component operations."""

from __future__ import annotations

from typing import Literal

from dataclasses import dataclass, field

from app.services.models import WhoAndWhen
from app.services.models.app_user import UserSummary
from app.services.models.data_type import DataTypeSummaryServiceRecord
from app.services.models.library import LibrarySummaryServiceRecord
from app.services.models.log import LogSummaryServiceRecord
from app.services.models.namespace import NamespaceSummaryServiceRecord
from app.services.models.release import ReleaseSummaryServiceRecord
from app.services.models.tag import TagSummaryServiceRecord
from app.services.utils.date import DateRange
from app.services.utils.pagination import PaginationParams
from app.services.utils.string import Guid
from app.types.identifiers import AccId, AccManifestId, AsccId, AsccManifestId, AsccpId, AsccpManifestId, BccId
from app.types.identifiers import BccManifestId, BccpId, BccpManifestId
from app.types.identifiers import ReleaseId


class CoreComponentServiceParams:
    """Parameters for listing core components."""

    def __init__(
        self,
        *,
        release_id: ReleaseId,
        types: list[str],
        pagination: PaginationParams,
        den: str | None = None,
        tag: str | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
    ) -> None:
        self.release_id = release_id
        self.types = types
        self.pagination = pagination
        self.den = den
        self.tag = tag
        self.created_on = created_on
        self.last_updated_on = last_updated_on


CoreComponentType = Literal["ACC", "ASCCP", "BCCP"]
CoreComponentState = Literal[
    "Deleted",
    "WIP",
    "Draft",
    "QA",
    "Candidate",
    "Production",
    "ReleaseDraft",
    "Published",
]
OagisComponentType = Literal[
    "Base",
    "Semantics",
    "Extension",
    "SemanticGroup",
    "UserExtensionGroup",
    "Embedded",
    "OAGIS10Nouns",
    "OAGIS10BODs",
    "BOD",
    "Verb",
    "Noun",
    "Choice",
    "AttributeGroup",
]


@dataclass(kw_only=True)
class CoreComponentServiceResult:
    """Unified list record for core components."""

    component_type: CoreComponentType
    manifest_id: int
    component_id: int
    guid: Guid
    den: str
    name: str | None = None
    definition: str | None = None
    definition_source: str | None = None
    is_deprecated: bool
    state: str | None = None
    namespace: NamespaceSummaryServiceRecord | None = None
    library: LibrarySummaryServiceRecord
    release: ReleaseSummaryServiceRecord
    log: LogSummaryServiceRecord | None = None
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen
    tag: str | None = None


@dataclass(kw_only=True)
class DataTypeSummaryServiceRecord:
    """Manifest-scoped BDT summary."""

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
    namespace: NamespaceSummaryServiceRecord | None = None
    library: LibrarySummaryServiceRecord
    release: ReleaseSummaryServiceRecord


@dataclass(kw_only=True)
class ValueConstraintServiceRecord:
    """Value constraint information."""

    default_value: str | None = None
    fixed_value: str | None = None


@dataclass(kw_only=True)
class AccSummaryServiceRecord:
    """Manifest-scoped ACC information."""

    acc_manifest_id: int
    acc_id: int
    guid: str
    den: str
    object_class_term: str
    definition: str | None = None
    definition_source: str | None = None
    is_deprecated: bool


@dataclass(kw_only=True)
class BaseAccSummaryServiceRecord:
    """Manifest-scoped base ACC information."""

    acc_manifest_id: int
    acc_id: int
    guid: str
    den: str
    object_class_term: str
    type: str | None = None
    definition: str | None = None
    definition_source: str | None = None
    namespace: NamespaceSummaryServiceRecord | None = None
    library: LibrarySummaryServiceRecord
    release: ReleaseSummaryServiceRecord


@dataclass(kw_only=True)
class AsccpSummaryServiceRecord:
    """Manifest-scoped ASCCP information."""

    asccp_manifest_id: int
    asccp_id: int
    role_of_acc_manifest_id: int
    guid: str
    den: str | None = None
    property_term: str | None = None
    definition: str | None = None
    definition_source: str | None = None
    is_deprecated: bool


@dataclass(kw_only=True)
class AsccRelationshipServiceRecord:
    """ASCC relationship entry inside ACC detail."""

    component_type: Literal["ASCC"] = 'ASCC'
    ascc_manifest_id: int
    ascc_id: int
    guid: str
    den: str
    cardinality_min: int
    cardinality_max: int
    is_deprecated: bool
    definition: str | None = None
    definition_source: str | None = None
    from_acc: AccSummaryServiceRecord
    to_asccp: AsccpSummaryServiceRecord
    manifest_id: int = 0
    cardinality_display: str = ""


@dataclass(kw_only=True)
class BccpSummaryServiceRecord:
    """Manifest-scoped BCCP information."""

    bccp_manifest_id: int
    bccp_id: int
    guid: str
    den: str
    property_term: str
    representation_term: str
    definition: str | None = None
    definition_source: str | None = None
    bdt_manifest: DataTypeSummaryServiceRecord
    is_deprecated: bool


@dataclass(kw_only=True)
class BccSummaryServiceRecord:
    """Manifest-scoped BCC information."""

    bcc_manifest_id: int
    bcc_id: int
    guid: str
    den: str
    cardinality_min: int
    cardinality_max: int
    entity_type: Literal["Attribute", "Element"] | None = None
    is_nillable: bool
    is_deprecated: bool
    definition: str | None = None
    definition_source: str | None = None
    from_acc_manifest_id: int
    to_bccp_manifest_id: int


@dataclass(kw_only=True)
class BccRelationshipServiceRecord:
    """BCC relationship entry inside ACC detail."""

    component_type: Literal["BCC"] = 'BCC'
    bcc_manifest_id: int
    bcc_id: int
    guid: str
    den: str
    cardinality_min: int
    cardinality_max: int
    entity_type: Literal["Attribute", "Element"] | None = None
    is_nillable: bool
    value_constraint: ValueConstraintServiceRecord | None = None
    is_deprecated: bool
    definition: str | None = None
    definition_source: str | None = None
    from_acc: AccSummaryServiceRecord
    to_bccp: BccpSummaryServiceRecord
    manifest_id: int = 0
    cardinality_display: str = ""


@dataclass(kw_only=True)
class GetAccServiceResult:
    """ACC detail response model."""

    acc_manifest_id: int
    acc_id: int
    base_acc: BaseAccSummaryServiceRecord | None = None
    relationships: list[AsccRelationshipServiceRecord | BccRelationshipServiceRecord] = field(default_factory=list)
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
    namespace: NamespaceSummaryServiceRecord | None = None
    library: LibrarySummaryServiceRecord
    release: ReleaseSummaryServiceRecord
    tags: list[TagSummaryServiceRecord] = field(default_factory=list)
    log: LogSummaryServiceRecord | None = None
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen


@dataclass(kw_only=True)
class GetAsccpServiceResult:
    """ASCCP detail response model."""

    asccp_manifest_id: int
    asccp_id: int
    role_of_acc: BaseAccSummaryServiceRecord | None = None
    guid: str
    den: str | None = None
    property_term: str | None = None
    definition: str | None = None
    definition_source: str | None = None
    reusable_indicator: bool
    is_nillable: bool | None = None
    is_deprecated: bool
    state: str | None = None
    namespace: NamespaceSummaryServiceRecord | None = None
    library: LibrarySummaryServiceRecord
    release: ReleaseSummaryServiceRecord
    tags: list[TagSummaryServiceRecord] = field(default_factory=list)
    log: LogSummaryServiceRecord | None = None
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen


@dataclass(kw_only=True)
class GetBccpServiceResult:
    """BCCP detail response model."""

    bccp_manifest_id: int
    bccp_id: int
    bdt: DataTypeSummaryServiceRecord
    guid: str
    den: str
    property_term: str
    representation_term: str
    definition: str | None = None
    definition_source: str | None = None
    is_nillable: bool
    value_constraint: ValueConstraintServiceRecord | None = None
    is_deprecated: bool
    state: str | None = None
    namespace: NamespaceSummaryServiceRecord | None = None
    library: LibrarySummaryServiceRecord
    release: ReleaseSummaryServiceRecord
    tags: list[TagSummaryServiceRecord] = field(default_factory=list)
    log: LogSummaryServiceRecord | None = None
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen


@dataclass(kw_only=True)
class CreateAccServiceResult:
    """ACC create response model."""

    acc_manifest_id: int


@dataclass(kw_only=True)
class CreateAsccpServiceResult:
    """ASCCP create response model."""

    asccp_manifest_id: int


@dataclass(kw_only=True)
class CreateBccpServiceResult:
    """BCCP create response model."""

    bccp_manifest_id: int


@dataclass(kw_only=True)
class CreateAsccServiceResult:
    """ASCC place response model."""

    ascc_manifest_id: int


@dataclass(kw_only=True)
class CreateBccServiceResult:
    """BCC place response model."""

    bcc_manifest_id: int


@dataclass(kw_only=True)
class MoveAsccServiceResult:
    """ASCC move response model."""

    ascc_manifest_id: int
    updates: list[str] = field(default_factory=list)


@dataclass(kw_only=True)
class MoveBccServiceResult:
    """BCC move response model."""

    bcc_manifest_id: int
    updates: list[str] = field(default_factory=list)


@dataclass(kw_only=True)
class UpdateAccServiceResult:
    """ACC update/state-update response model."""

    acc_manifest_id: int
    updates: list[str] = field(default_factory=list)


@dataclass(kw_only=True)
class UpdateAsccpServiceResult:
    """ASCCP update response model."""

    asccp_manifest_id: int
    updates: list[str] = field(default_factory=list)


@dataclass(kw_only=True)
class UpdateBccpServiceResult:
    """BCCP update response model."""

    bccp_manifest_id: int
    updates: list[str] = field(default_factory=list)


@dataclass(kw_only=True)
class UpdateAccBaseServiceResult:
    """ACC base update response model."""

    acc_manifest_id: int
    updates: list[str] = field(default_factory=list)


@dataclass(kw_only=True)
class UpdateAccTagsServiceResult:
    """ACC tag update response model."""

    acc_manifest_id: int
    updates: list[str] = field(default_factory=list)


@dataclass(kw_only=True)
class DiscardAccServiceResult:
    """ACC discard response model."""

    acc_manifest_id: int
    discarded: bool


@dataclass(kw_only=True)
class DiscardAsccpServiceResult:
    """ASCCP discard response model."""

    asccp_manifest_id: int
    discarded: bool


@dataclass(kw_only=True)
class DiscardBccpServiceResult:
    """BCCP discard response model."""

    bccp_manifest_id: int
    discarded: bool


@dataclass(kw_only=True)
class ReviseAccServiceResult:
    """ACC revise response model."""

    acc_manifest_id: int
    revised: bool


@dataclass(kw_only=True)
class ReviseAsccpServiceResult:
    """ASCCP revise response model."""

    asccp_manifest_id: int
    revised: bool


@dataclass(kw_only=True)
class ReviseBccpServiceResult:
    """BCCP revise response model."""

    bccp_manifest_id: int
    revised: bool


@dataclass(kw_only=True)
class CancelAccServiceResult:
    """ACC cancel response model."""

    acc_manifest_id: int
    cancelled: bool


@dataclass(kw_only=True)
class CancelAsccpServiceResult:
    """ASCCP cancel response model."""

    asccp_manifest_id: int
    cancelled: bool


@dataclass(kw_only=True)
class CancelBccpServiceResult:
    """BCCP cancel response model."""

    bccp_manifest_id: int
    cancelled: bool


@dataclass(kw_only=True)
class AsccInfoRecord:
    """ASCC detail record used in BIE relationship payloads."""

    ascc_manifest_id: AsccManifestId
    ascc_id: AsccId
    guid: Guid
    den: str
    cardinality_min: int
    cardinality_max: int
    is_deprecated: bool
    definition: str | None = None
    definition_source: str | None = None
    from_acc_manifest_id: AccManifestId
    to_asccp_manifest_id: AsccpManifestId


@dataclass(kw_only=True)
class BccInfoRecord:
    """BCC detail record used in BIE relationship payloads."""

    bcc_manifest_id: BccManifestId
    bcc_id: BccId
    guid: Guid
    den: str
    cardinality_min: int
    cardinality_max: int
    entity_type: Literal["Attribute", "Element"] | None = None
    is_nillable: bool
    is_deprecated: bool
    definition: str | None = None
    definition_source: str | None = None
    from_acc_manifest_id: AccManifestId
    to_bccp_manifest_id: BccpManifestId


@dataclass(kw_only=True)
class AsccpInfoRecord:
    """ASCCP details used in ACC relationships."""

    asccp_manifest_id: AsccpManifestId
    asccp_id: AsccpId
    role_of_acc_manifest_id: AccManifestId
    guid: Guid
    den: str
    property_term: str
    definition: str | None = None
    definition_source: str | None = None
    is_deprecated: bool


@dataclass(kw_only=True)
class BccpInfoRecord:
    """BCCP details used in ACC relationships."""

    bccp_manifest_id: BccpManifestId
    bccp_id: BccpId
    guid: Guid
    den: str
    property_term: str
    representation_term: str
    definition: str | None = None
    definition_source: str | None = None
    bdt_manifest: DataTypeSummaryServiceRecord
    is_deprecated: bool


@dataclass(kw_only=True)
class AccInfoRecord:
    """ACC information used in relationship payloads."""

    acc_manifest_id: AccManifestId
    acc_id: AccId
    guid: Guid
    den: str
    object_class_term: str
    definition: str | None = None
    definition_source: str | None = None
    is_deprecated: bool
