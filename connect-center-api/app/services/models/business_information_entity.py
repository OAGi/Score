"""Service models for Business Information Entity operations."""

from __future__ import annotations

from typing import Literal

from dataclasses import dataclass, field

from app.services.models import WhoAndWhen
from app.services.models.app_user import UserSummary
from app.services.models.biz_ctx import BizCtxSummaryServiceRecord
from app.services.models.core_component import AsccInfoRecord, BccInfoRecord, ValueConstraintServiceRecord, \
    AccInfoRecord, \
    AsccpInfoRecord, BccpInfoRecord
from app.services.models.data_type import DataTypeSupplementaryComponentServiceRecord
from app.services.models.library import LibrarySummaryServiceRecord
from app.services.models.release import ReleaseSummaryServiceRecord
from app.services.utils.string import Guid
from app.types.identifiers import AgencyIdListManifestId
from app.types.identifiers import CodeListManifestId
from app.types.identifiers import XbtManifestId


@dataclass(kw_only=True)
class TopLevelAsbiepListServiceResult:
    """Service result model for top-level ASBIEP list entries."""

    top_level_asbiep_id: int
    asbiep_id: int
    guid: Guid
    den: str | None = None
    property_term: str | None = None
    display_name: str | None = None
    version: str | None = None
    status: str | None = None
    biz_term: str | None = None
    remark: str | None = None
    business_contexts: list[BizCtxSummaryServiceRecord] = field(default_factory=list)
    state: str | None = None
    is_deprecated: bool
    deprecated_reason: str | None = None
    deprecated_remark: str | None = None
    library: LibrarySummaryServiceRecord
    release: ReleaseSummaryServiceRecord
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen


@dataclass(kw_only=True)
class GetTopLevelAsbiepServiceResult:
    """Service result model for top-level ASBIEP detail."""

    top_level_asbiep_id: int
    asbiep: AsbiepInfoRecord
    version: str | None = None
    status: str | None = None
    business_contexts: list[BizCtxSummaryServiceRecord] = field(default_factory=list)
    state: str
    is_deprecated: bool
    deprecated_reason: str | None = None
    deprecated_remark: str | None = None
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen


@dataclass(kw_only=True)
class GetAsbieServiceResult:
    """Service result model for ASBIE detail."""

    asbie_id: int | None = None
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord
    guid: Guid | None = None
    based_ascc: AsccInfoRecord
    to_asbiep: AsbiepInfoRecord
    is_used: bool
    cardinality_min: int
    cardinality_max: int
    is_nillable: bool
    definition: str | None = None
    remark: str | None = None
    created: WhoAndWhen | None = None
    last_updated: WhoAndWhen | None = None


@dataclass(kw_only=True)
class GetBbieServiceResult:
    """Service result model for BBIE detail."""

    bbie_id: int | None = None
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord
    guid: Guid | None = None
    based_bcc: BccInfoRecord
    to_bbiep: BbiepInfoRecord
    is_used: bool
    cardinality_min: int
    cardinality_max: int
    is_nillable: bool
    remark: str | None = None
    primitiveRestriction: PrimitiveRestrictionServiceRecord | None = None
    valueConstraint: ValueConstraintServiceRecord | None = None
    facet: FacetServiceRecord | None = None
    created: WhoAndWhen | None = None
    last_updated: WhoAndWhen | None = None


@dataclass(kw_only=True)
class CreateTopLevelAsbiepServiceResult:
    """Service result model for top-level ASBIEP creation."""

    top_level_asbiep_id: int
    asbiep: AsbiepInfoRecord | None = None


@dataclass(kw_only=True)
class UpdateTopLevelAsbiepServiceResult:
    """Service result model for top-level ASBIEP update/state update."""

    top_level_asbiep_id: int
    updates: list[str] = field(default_factory=list)


@dataclass(kw_only=True)
class DeleteTopLevelAsbiepServiceResult:
    """Service result model for top-level ASBIEP deletion."""

    top_level_asbiep_id: int | None = None
    message: str | None = None


@dataclass(kw_only=True)
class TransferTopLevelAsbiepOwnershipServiceResult:
    """Service result model for top-level ASBIEP ownership transfer."""

    top_level_asbiep_id: int


@dataclass(kw_only=True)
class AssignBizCtxToTopLevelAsbiepServiceResult:
    """Service result model for assigning business context to top-level ASBIEP."""

    top_level_asbiep_id: int


@dataclass(kw_only=True)
class UnassignBizCtxFromTopLevelAsbiepServiceResult:
    """Service result model for unassigning business context from top-level ASBIEP."""

    top_level_asbiep_id: int


@dataclass(kw_only=True)
class CreateAsbieServiceResult:
    """Service result model for ASBIE creation."""

    asbie_id: int
    asbiep: AsbiepInfoRecord


@dataclass(kw_only=True)
class UpdateAsbieServiceResult:
    """Service result model for ASBIE update."""

    asbie_id: int
    updates: list[str] = field(default_factory=list)
    asbiep: AsbiepInfoRecord | None = None


@dataclass(kw_only=True)
class CreateBbieServiceResult:
    """Service result model for BBIE creation."""

    bbie_id: int
    bbiep: BbiepInfoRecord


@dataclass(kw_only=True)
class UpdateBbieServiceResult:
    """Service result model for BBIE update."""

    bbie_id: int
    updates: list[str] = field(default_factory=list)
    bbiep: BbiepInfoRecord | None = None


@dataclass(kw_only=True)
class CreateBbieScServiceResult:
    """Service result model for BBIE supplementary-component creation."""

    bbie_sc_id: int
    updates: list[str] = field(default_factory=list)


@dataclass(kw_only=True)
class UpdateBbieScServiceResult:
    """Service result model for BBIE supplementary-component update."""

    bbie_sc_id: int
    updates: list[str] = field(default_factory=list)


@dataclass(kw_only=True)
class ReuseTopLevelAsbiepServiceResult:
    """Service result model for top-level ASBIEP reuse."""

    asbie_id: int
    reused_asbiep_id: int
    updates: list[str] = field(default_factory=list)


@dataclass(kw_only=True)
class RemoveReusedTopLevelAsbiepServiceResult:
    """Service result model for removing reused top-level ASBIEP."""

    asbie_id: int
    updates: list[str] = field(default_factory=list)


@dataclass(kw_only=True)
class PrimitiveRestrictionServiceRecord:
    """Primitive restriction selection for BIE payloads."""

    xbtManifestId: XbtManifestId | None = None
    codeListManifestId: CodeListManifestId | None = None
    agencyIdListManifestId: AgencyIdListManifestId | None = None


@dataclass(kw_only=True)
class FacetServiceRecord:
    """Facet restrictions for BIE payloads."""

    facet_min_length: int | None = None
    facet_max_length: int | None = None
    facet_pattern: str | None = None


@dataclass(kw_only=True)
class AsbieRelationshipServiceRecord:
    """ASBIE relationship in BIE role-of ABIE relationships."""

    component_type: Literal["ASBIE"] = "ASBIE"
    asbie_id: int | None = None
    guid: Guid | None = None
    based_ascc: AsccInfoRecord
    to_asbiep_id: int | None = None
    is_used: bool
    path: str
    hash_path: str
    cardinality_min: int
    cardinality_max: int
    cardinality_display: str = field(init=False)
    is_nillable: bool
    remark: str | None = None

    def __post_init__(self) -> None:
        upper = "unbounded" if self.cardinality_max == -1 else str(self.cardinality_max)
        self.cardinality_display = f"{self.cardinality_min}..{upper}"


@dataclass(kw_only=True)
class BbieRelationshipServiceRecord:
    """BBIE relationship in BIE role-of ABIE relationships."""

    component_type: Literal["BBIE"] = "BBIE"
    bbie_id: int | None = None
    guid: Guid | None = None
    based_bcc: BccInfoRecord
    to_bbiep_id: int | None = None
    is_used: bool
    path: str
    hash_path: str
    cardinality_min: int
    cardinality_max: int
    cardinality_display: str = field(init=False)
    is_nillable: bool
    remark: str | None = None
    primitiveRestriction: PrimitiveRestrictionServiceRecord | None = None
    valueConstraint: ValueConstraintServiceRecord | None = None
    facet: FacetServiceRecord | None = None

    def __post_init__(self) -> None:
        upper = "unbounded" if self.cardinality_max == -1 else str(self.cardinality_max)
        self.cardinality_display = f"{self.cardinality_min}..{upper}"


@dataclass(kw_only=True)
class TopLevelAsbiepInfoRecord:
    """Top-level ASBIEP summary used by BIE detail payloads."""

    top_level_asbiep_id: int
    library: LibrarySummaryServiceRecord
    release: ReleaseSummaryServiceRecord
    version: str | None = None
    status: str | None = None
    state: str | None = None
    is_deprecated: bool
    deprecated_reason: str | None = None
    deprecated_remark: str | None = None
    owner: UserSummary
    created: WhoAndWhen | None = None
    last_updated: WhoAndWhen | None = None


@dataclass(kw_only=True)
class AbieInfoRecord:
    """ABIE information with flattened ASBIE/BBIE relationships."""

    abie_id: int | None = None
    guid: Guid | None = None
    path: str | None = None
    hash_path: str | None = None
    based_acc_manifest: AccInfoRecord
    definition: str | None = None
    remark: str | None = None
    relationships: list[RelationshipRecord] = field(default_factory=list)
    created: WhoAndWhen | None = None
    last_updated: WhoAndWhen | None = None


@dataclass(kw_only=True)
class AsbiepInfoRecord:
    """ASBIEP details for BIE payloads."""

    asbiep_id: int | None = None
    guid: Guid | None = None
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord | None = None
    based_asccp_manifest: AsccpInfoRecord
    path: str | None = None
    hash_path: str | None = None
    role_of_abie: AbieInfoRecord
    definition: str | None = None
    remark: str | None = None
    biz_term: str | None = None
    display_name: str | None = None
    created: WhoAndWhen | None = None
    last_updated: WhoAndWhen | None = None


@dataclass(kw_only=True)
class BbieScInfoRecord:
    """BBIE supplementary component details."""

    bbie_sc_id: int | None = None
    guid: Guid | None = None
    based_dt_sc: DataTypeSupplementaryComponentServiceRecord
    path: str
    hash_path: str
    definition: str | None = None
    biz_term: str | None = None
    display_name: str | None = None
    cardinality_min: int
    cardinality_max: int
    primitiveRestriction: PrimitiveRestrictionServiceRecord | None = None
    valueConstraint: ValueConstraintServiceRecord | None = None
    facet: FacetServiceRecord | None = None
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord | None = None


@dataclass(kw_only=True)
class BbiepInfoRecord:
    """BBIEP details for BBIE payloads."""

    bbiep_id: int | None = None
    guid: Guid | None = None
    based_bccp: BccpInfoRecord
    path: str
    hash_path: str
    definition: str | None = None
    remark: str | None = None
    biz_term: str | None = None
    display_name: str | None = None
    supplementary_components: list[BbieScInfoRecord] = field(default_factory=list)
    owner_top_level_asbiep: TopLevelAsbiepInfoRecord | None = None
    created: WhoAndWhen | None = None
    last_updated: WhoAndWhen | None = None


RelationshipRecord = AsbieRelationshipServiceRecord | BbieRelationshipServiceRecord
