"""Models for Release MCP tools."""

from __future__ import annotations

from pydantic import BaseModel, ConfigDict, Field

from app.services.utils.string import Guid
from app.tools.models.shared import LibrarySummaryRecord
from app.tools.models.shared import NamespaceSummaryRecord
from app.tools.models.shared import WhoAndWhen


class ReleaseReference(BaseModel):
    """Adjacent release reference for MCP tool responses."""

    release_id: int
    release_num: str

    model_config = ConfigDict(frozen=True, from_attributes=True)


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
    is_latest: bool = Field(
        ...,
        description=(
            "True when this release's next_release points to the library's Working release; "
            "use this flag to identify the latest published release."
        ),
    )
    prev_release: "ReleaseReference | None" = Field(
        default=None,
        description="Previous release in the library release chain, with release_id and release_num.",
    )
    next_release: "ReleaseReference | None" = Field(
        default=None,
        description="Next release in the library release chain, with release_id and release_num. If this is Working, is_latest is true.",
    )

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
