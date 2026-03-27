"""Models for Agency ID List MCP tools."""

from __future__ import annotations

from pydantic import BaseModel, ConfigDict, Field

from app.services.utils.string import Guid
from app.tools.models.shared import LibrarySummaryRecord
from app.tools.models.shared import LogSummaryRecord
from app.tools.models.shared import NamespaceSummaryRecord
from app.tools.models.shared import ReleaseSummaryRecord
from app.tools.models.shared import UserSummary
from app.tools.models.shared import WhoAndWhen


class AgencyIdListValueResponse(BaseModel):
    """Agency ID list value payload for MCP tools."""

    agency_id_list_value_manifest_id: int
    agency_id_list_value_id: int
    guid: Guid
    value: str
    name: str | None = None
    definition: str | None = None
    is_deprecated: bool = False
    is_developer_default: bool = False
    is_user_default: bool = False

    model_config = ConfigDict(frozen=True, from_attributes=True)


class AgencyIdListResponseEntry(BaseModel):
    """Agency ID list payload for MCP tools."""

    agency_id_list_manifest_id: int
    agency_id_list_id: int
    guid: Guid
    enum_type_guid: str | None = None
    name: str
    list_id: str
    version_id: str
    definition: str | None = None
    remark: str | None = None
    definition_source: str | None = None
    is_deprecated: bool = False
    state: str | None = None
    values: list[AgencyIdListValueResponse] = Field(default_factory=list)
    namespace: NamespaceSummaryRecord | None = None
    library: LibrarySummaryRecord
    release: ReleaseSummaryRecord
    log: LogSummaryRecord | None = None
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetAgencyIdListResponse(AgencyIdListResponseEntry):
    """Response for get_agency_id_list tool."""


class GetAgencyIdListPaginationResponse(BaseModel):
    """Response for get_agency_id_lists tool."""

    total_items: int
    offset: int
    limit: int
    items: list[AgencyIdListResponseEntry]

    model_config = ConfigDict(frozen=True)
