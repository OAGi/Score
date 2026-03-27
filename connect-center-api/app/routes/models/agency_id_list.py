"""Pydantic response models for Agency ID List endpoints."""


from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, ConfigDict, Field

from app.routes.models.shared import LibrarySummaryRecord
from app.routes.models.shared import LogSummaryRecord
from app.routes.models.shared import NamespaceSummaryRecord
from app.routes.models.shared import ReleaseSummaryRecord
from app.routes.models.shared import UserSummary
from app.routes.models.shared import WhoAndWhen
from app.services.utils.string import Guid


class AgencyIdListValueEntry(BaseModel):
    """API representation of one agency ID list value."""

    agency_id_list_value_manifest_id: int = Field(..., description="Unique identifier for the agency ID list value manifest.")
    agency_id_list_value_id: int = Field(..., description="Unique identifier for the agency ID list value.")
    guid: Guid = Field(..., description="Globally unique identifier.")
    value: str = Field(..., description="Agency ID list value.")
    name: str | None = Field(default=None, description="Name for the value.")
    definition: str | None = Field(default=None, description="Definition of the value.")
    is_deprecated: bool = Field(..., description="Whether this value is deprecated.")
    is_developer_default: bool = Field(..., description="Whether this value is developer default.")
    is_user_default: bool = Field(..., description="Whether this value is user default.")

    model_config = ConfigDict(frozen=True)


class AgencyIdListEntry(BaseModel):
    """API representation of an agency ID list record."""

    agency_id_list_manifest_id: int = Field(..., description="Unique identifier for the agency ID list manifest.")
    agency_id_list_id: int = Field(..., description="Unique identifier for the agency ID list.")
    guid: Guid = Field(..., description="Globally unique identifier.")
    enum_type_guid: str | None = Field(default=None, description="Enum type GUID.")
    name: str | None = Field(default=None, description="Name of the agency ID list.")
    list_id: str | None = Field(default=None, description="List identifier.")
    version_id: str | None = Field(default=None, description="Version identifier.")
    definition: str | None = Field(default=None, description="Definition.")
    remark: str | None = Field(default=None, description="Remark.")
    definition_source: str | None = Field(default=None, description="Definition source URL.")
    is_deprecated: bool = Field(..., description="Whether this agency ID list is deprecated.")
    state: Literal[
        "Deleted",
        "WIP",
        "Draft",
        "QA",
        "Candidate",
        "Production",
        "ReleaseDraft",
        "Published",
    ] = Field(..., description="Lifecycle state.")
    values: list[AgencyIdListValueEntry] = Field(default_factory=list, description="Agency ID list values.")
    namespace: NamespaceSummaryRecord | None = Field(default=None, description="Namespace information.")
    library: LibrarySummaryRecord = Field(..., description="Library information.")
    release: ReleaseSummaryRecord = Field(..., description="Release information.")
    log: LogSummaryRecord | None = Field(default=None, description="Revision log summary.")
    owner: UserSummary = Field(..., description="Owner information.")
    created: WhoAndWhen = Field(..., description="Creation information.")
    last_updated: WhoAndWhen = Field(..., description="Last update information.")

    model_config = ConfigDict(frozen=True)


class GetAgencyIDListResponse(BaseModel):
    """Paginated response envelope for agency ID list listings."""

    total_items: int = Field(..., ge=0, description="Total number of matching agency ID lists.")
    offset: int = Field(..., ge=0, description="Offset used for this page.")
    limit: int = Field(..., ge=1, le=100, description="Limit used for this page.")
    items: list[AgencyIdListEntry] = Field(..., description="Agency ID lists.")


class GetAgencyIdListByAgencyIdListManifestIdResponse(AgencyIdListEntry):
    """Response payload for retrieving one agency ID list by manifest ID."""

    model_config = ConfigDict(frozen=True)

