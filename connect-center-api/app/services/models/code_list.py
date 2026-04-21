"""Service models for code list operations."""

from __future__ import annotations

from typing import Literal

from dataclasses import dataclass, field

from app.services.models import WhoAndWhen
from app.services.models.app_user import UserSummary
from app.services.models.library import LibrarySummaryServiceRecord
from app.services.models.log import LogSummaryServiceRecord
from app.services.models.namespace import NamespaceSummaryServiceRecord
from app.services.models.release import ReleaseSummaryServiceRecord
from app.services.utils.date import DateRange
from app.services.utils.pagination import PaginationParams
from app.services.utils.string import Guid
from app.types.identifiers import CodeListId, CodeListManifestId, CodeListValueId, CodeListValueManifestId
from app.types.identifiers import ReleaseId


CodeListState = Literal[
    "Deleted",
    "WIP",
    "Draft",
    "QA",
    "Candidate",
    "Production",
    "ReleaseDraft",
    "Published",
]


class CodeListServiceParams:
    """Parameters for listing code lists."""

    def __init__(
        self,
        *,
        release_id: ReleaseId,
        pagination: PaginationParams,
        name: str | None = None,
        list_id: str | None = None,
        version_id: str | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
    ) -> None:
        self.release_id = release_id
        self.pagination = pagination
        self.name = name
        self.list_id = list_id
        self.version_id = version_id
        self.created_on = created_on
        self.last_updated_on = last_updated_on


@dataclass(kw_only=True)
class CodeListServiceResult:
    """Code list information with value manifests."""

    code_list_manifest_id: CodeListManifestId
    code_list_id: CodeListId
    guid: Guid
    enum_type_guid: str | None = None
    name: str = ""
    list_id: str
    version_id: str
    definition: str | None = None
    remark: str | None = None
    definition_source: str | None = None
    extensible_indicator: bool
    is_deprecated: bool
    state: CodeListState
    values: list[CodeListValueServiceRecord] = field(default_factory=list)
    namespace: NamespaceSummaryServiceRecord | None = None
    library: LibrarySummaryServiceRecord
    release: ReleaseSummaryServiceRecord
    log: LogSummaryServiceRecord | None = None
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen


@dataclass(kw_only=True)
class CodeListValueServiceRecord:
    """Code list value information."""

    code_list_value_manifest_id: CodeListValueManifestId
    code_list_value_id: CodeListValueId
    guid: Guid
    value: str
    meaning: str | None = None
    definition: str | None = None
    definition_source: str | None = None
    is_deprecated: bool


@dataclass(kw_only=True)
class CreateCodeListServiceResult:
    """Code list create response model."""

    code_list_manifest_id: CodeListManifestId


@dataclass(kw_only=True)
class CreateCodeListValueServiceResult:
    """Code list value create response model."""

    code_list_value_manifest_id: CodeListValueManifestId


@dataclass(kw_only=True)
class UpdateCodeListServiceResult:
    """Code list update response model."""

    code_list_manifest_id: CodeListManifestId
    updates: list[str] = field(default_factory=list)


@dataclass(kw_only=True)
class UpdateCodeListValueServiceResult:
    """Code list value update response model."""

    code_list_value_manifest_id: CodeListValueManifestId
    updates: list[str] = field(default_factory=list)


@dataclass(kw_only=True)
class TransferCodeListOwnershipServiceResult:
    """Code list ownership-transfer response model."""

    code_list_manifest_id: CodeListManifestId
    updates: list[str] = field(default_factory=list)
