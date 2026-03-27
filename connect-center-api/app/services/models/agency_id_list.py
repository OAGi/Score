"""Service models for agency ID list operations."""

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
from app.types.identifiers import AgencyIdListId, AgencyIdListManifestId, AgencyIdListValueId
from app.types.identifiers import AgencyIdListValueManifestId
from app.types.identifiers import ReleaseId


class AgencyIdListServiceListParams:
    """Parameters for listing agency ID lists."""

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
class AgencyIdListValueServiceRecord:
    """Agency ID list value information."""

    agency_id_list_value_manifest_id: AgencyIdListValueManifestId
    agency_id_list_value_id: AgencyIdListValueId
    guid: Guid
    value: str
    name: str | None = None
    definition: str | None = None
    is_deprecated: bool
    is_developer_default: bool
    is_user_default: bool


@dataclass(kw_only=True)
class AgencyIdListServiceResult:
    """Agency ID list information with value manifests."""

    agency_id_list_manifest_id: AgencyIdListManifestId
    agency_id_list_id: AgencyIdListId
    guid: Guid
    enum_type_guid: str | None = None
    name: str = ""
    list_id: str = ""
    version_id: str = ""
    definition: str | None = None
    remark: str | None = None
    definition_source: str | None = None
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
    values: list[AgencyIdListValueServiceRecord] = field(default_factory=list)
    namespace: NamespaceSummaryServiceRecord | None = None
    library: LibrarySummaryServiceRecord
    release: ReleaseSummaryServiceRecord
    log: LogSummaryServiceRecord | None = None
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen
