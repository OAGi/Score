"""Models for Library MCP tools."""

from __future__ import annotations

from pydantic import BaseModel, ConfigDict, Field

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


class LibraryReleaseDependencyRecord(BaseModel):
    """Release dependency summary for a library's working release."""

    release_id: int
    library_id: int
    library_name: str
    release_num: str
    state: str

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetLibraryResponse(LibraryResponseEntry):
    """Response for get_library tool."""

    release_dependencies: list[LibraryReleaseDependencyRecord] = Field(default_factory=list)


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


class ManageLibraryReleaseDependenciesResponse(BaseModel):
    """Response for add/remove library release dependency tools."""

    library_id: int
    release_dependencies: list[LibraryReleaseDependencyRecord]

    model_config = ConfigDict(frozen=True)
