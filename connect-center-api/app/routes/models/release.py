"""Pydantic response models for Release endpoints.

Defines the serialized shapes returned by release list/get routes, including
library and namespace summaries.
"""


from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, ConfigDict, Field

from app.routes.models.shared import WhoAndWhen
from app.services.utils.string import Guid


class LibrarySummary(BaseModel):
    """Minimal library summary embedded in release responses."""
    library_id: int = Field(..., ge=1, description="Unique identifier for the library.")
    name: str = Field(..., description="Library name.")

    model_config = ConfigDict(frozen=True)


class NamespaceSummary(BaseModel):
    """Minimal namespace summary embedded in release responses."""
    namespace_id: int = Field(..., ge=1, description="Unique identifier for the namespace.")
    prefix: str | None = Field(default=None, description="Default short name for the URI.")
    uri: str = Field(..., description="Namespace URI.")

    model_config = ConfigDict(frozen=True)


class ReleaseReference(BaseModel):
    """Adjacent release reference used to identify release-chain position."""

    release_id: int = Field(..., ge=1, description="Adjacent release identifier.")
    release_num: str = Field(..., description="Adjacent release number.")

    model_config = ConfigDict(frozen=True, from_attributes=True)


class ReleaseEntry(BaseModel):
    """API representation of a release record."""
    release_id: int = Field(..., ge=1, description="Unique identifier for the release.")
    library: LibrarySummary = Field(..., description="Library information that this release belongs to.")
    guid: Guid = Field(..., description="Globally unique identifier (GUID), 32 characters.")
    release_num: str = Field(..., description="Release number (e.g., '10.0').")
    release_note: str | None = Field(default=None, description="Release notes.")
    release_license: str | None = Field(default=None, description="Release license information.")
    namespace: NamespaceSummary | None = Field(default=None, description="Default namespace information (if any).")
    state: Literal["Processing", "Initialized", "Draft", "Published"] = Field(..., description="Current state of the release.")
    created: WhoAndWhen = Field(..., description="Information about who created the release and when.")
    last_updated: WhoAndWhen = Field(..., description="Information about who last updated the release and when.")
    is_latest: bool = Field(
        ...,
        description=(
            "True when this release's next_release points to the library's Working release; "
            "use this flag to identify the latest published release."
        ),
    )
    prev_release: ReleaseReference | None = Field(
        default=None,
        description="Previous release in the library release chain, with release_id and release_num.",
    )
    next_release: ReleaseReference | None = Field(
        default=None,
        description="Next release in the library release chain, with release_id and release_num. If this is Working, is_latest is true.",
    )

    model_config = ConfigDict(frozen=True)


class GetReleaseListResponse(BaseModel):
    """Paginated response envelope for release listings."""
    total_items: int = Field(..., ge=0, description="Total number of matching releases.")
    offset: int = Field(..., ge=0, description="Offset used for this page.")
    limit: int = Field(..., ge=1, le=100, description="Limit used for this page.")
    items: list[ReleaseEntry] = Field(..., description="Releases.")


class GetReleaseByReleaseIdResponse(ReleaseEntry):
    """Response payload for retrieving a release by ID."""

    model_config = ConfigDict(frozen=True)
