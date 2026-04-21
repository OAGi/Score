"""Models for Library MCP tools."""

from __future__ import annotations

from pydantic import BaseModel, ConfigDict

from app.tools.models.shared import WhoAndWhen


class LibraryResponseEntry(BaseModel):
    """Library payload for MCP tools."""

    library_id: int
    name: str
    type: str | None = None
    organization: str | None = None
    description: str | None = None
    link: str | None = None
    domain: str | None = None
    state: str | None = None
    is_read_only: bool
    is_default: bool
    created: WhoAndWhen
    last_updated: WhoAndWhen

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetLibraryResponse(LibraryResponseEntry):
    """Response for get_library tool."""


class GetLibraryPaginationResponse(BaseModel):
    """Response for get_libraries tool."""

    total_items: int
    offset: int
    limit: int
    items: list[LibraryResponseEntry]

    model_config = ConfigDict(frozen=True)


class CreateLibraryResponse(BaseModel):
    """Response for create_library tool."""

    library_id: int

    model_config = ConfigDict(frozen=True)


class UpdateLibraryResponse(BaseModel):
    """Response for update_library tool."""

    library_id: int
    updates: list[str]

    model_config = ConfigDict(frozen=True)


class UpdateLibraryReleaseDependenciesResponse(BaseModel):
    """Response for update_library_release_dependencies tool."""

    library_id: int
    release_ids: list[int]

    model_config = ConfigDict(frozen=True)
