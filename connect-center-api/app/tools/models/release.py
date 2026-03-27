"""Models for Release MCP tools."""

from __future__ import annotations

from pydantic import BaseModel, ConfigDict

from app.services.utils.string import Guid
from app.tools.models.shared import LibrarySummaryRecord
from app.tools.models.shared import NamespaceSummaryRecord
from app.tools.models.shared import WhoAndWhen


class ReleaseResponseEntry(BaseModel):
    """Release payload for MCP tools."""

    release_id: int
    library: LibrarySummaryRecord
    guid: Guid
    release_num: str
    release_note: str | None = None
    release_license: str | None = None
    namespace: NamespaceSummaryRecord | None = None
    state: str
    created: WhoAndWhen
    last_updated: WhoAndWhen

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetReleaseResponse(ReleaseResponseEntry):
    """Response for get_release tool."""


class GetReleasePaginationResponse(BaseModel):
    """Response for get_releases tool."""

    total_items: int
    offset: int
    limit: int
    items: list[ReleaseResponseEntry]

    model_config = ConfigDict(frozen=True)
