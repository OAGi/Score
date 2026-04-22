"""Pydantic response models for Library endpoints.

Defines the serialized shapes returned by library list/get routes.
"""


from __future__ import annotations

from pydantic import BaseModel, ConfigDict, Field

from app.routes.models.shared import WhoAndWhen


class LibraryEntry(BaseModel):
    """API representation of a library record."""
    library_id: int = Field(..., ge=1, description="Unique identifier for the library.")
    name: str = Field(..., description="Library name.")
    type: str | None = Field(default=None, description="Type of the library.")
    organization: str | None = Field(default=None, description="Organization that owns the library.")
    description: str | None = Field(default=None, description="Description of the library.")
    link: str | None = Field(default=None, description="URL link to the library.")
    domain: str | None = Field(default=None, description="Domain of the library.")
    state: str | None = Field(default=None, description="Current state of the library.")
    is_read_only: bool = Field(..., description="Whether the library is read-only.")
    is_default: bool = Field(..., description="Whether the library is default.")
    created: WhoAndWhen = Field(..., description="Information about creation.")
    last_updated: WhoAndWhen = Field(..., description="Information about the last update.")

    model_config = ConfigDict(frozen=True)


class GetLibraryListResponse(BaseModel):
    """Paginated response envelope for library listings."""
    total_items: int = Field(..., ge=0, description="Total number of matching libraries.")
    offset: int = Field(..., ge=0, description="Offset used for this page.")
    limit: int = Field(..., ge=1, le=100, description="Limit used for this page.")
    items: list[LibraryEntry] = Field(..., description="Libraries.")


class LibraryReleaseDependencyRecord(BaseModel):
    """Release dependency summary for a library's working release."""

    release_id: int = Field(..., ge=1, description="Release identifier.")
    library_id: int = Field(..., ge=1, description="Owning library identifier.")
    library_name: str = Field(..., description="Owning library name.")
    release_num: str = Field(..., description="Release number.")
    state: str = Field(..., description="Release lifecycle state.")

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetLibraryByLibraryIdResponse(LibraryEntry):
    """Response payload for retrieving a library by ID."""

    release_dependencies: list[LibraryReleaseDependencyRecord] = Field(
        default_factory=list,
        description="Direct dependencies of the library's working release.",
    )

    model_config = ConfigDict(frozen=True)


class CreateLibraryRequest(BaseModel):
    """Request payload for creating a library."""

    name: str = Field(..., min_length=1, description="Library name.")
    namespace_uri: str = Field(..., min_length=1, description="URI for the default standard namespace.")
    namespace_prefix: str | None = Field(default=None, description="Prefix for the default standard namespace.")
    type: str | None = Field(default=None, description="Type of the library.")
    organization: str | None = Field(default=None, description="Organization that owns the library.")
    link: str | None = Field(default=None, description="URL link to the library.")
    domain: str | None = Field(default=None, description="Domain of the library.")
    description: str | None = Field(default=None, description="Description of the library.")

    model_config = ConfigDict(frozen=True)


class CreateLibraryResponse(BaseModel):
    """Response payload for library creation."""

    library_id: int = Field(..., ge=1, description="Created library identifier.")

    model_config = ConfigDict(frozen=True)


class UpdateLibraryRequest(BaseModel):
    """Request payload for updating a library."""

    type: str | None = Field(default=None, description="Library type to save. Omit this field to leave it unchanged.")
    name: str | None = Field(default=None, min_length=1, description="Library name to save. Omit this field to leave it unchanged.")
    organization: str | None = Field(default=None, description="Organization to save. Omit this field to leave it unchanged.")
    link: str | None = Field(default=None, description="Library URL to save. Omit this field to leave it unchanged.")
    domain: str | None = Field(default=None, description="Domain to save. Omit this field to leave it unchanged.")
    description: str | None = Field(default=None, description="Description to save. Omit this field to leave it unchanged.")
    state: str | None = Field(default=None, description="Library state to save. Omit this field to leave it unchanged.")
    is_default: bool | None = Field(default=None, description="Whether this library should be the default. Omit this field to leave it unchanged.")

    model_config = ConfigDict(frozen=True)


class UpdateLibraryResponse(BaseModel):
    """Response payload for library update."""

    library_id: int = Field(..., ge=1, description="Target library identifier.")
    updates: list[str] = Field(..., description="Names of the fields that changed.")

    model_config = ConfigDict(frozen=True)


class ManageLibraryReleaseDependenciesResponse(BaseModel):
    """Response payload for adding or removing working-release dependencies."""

    library_id: int = Field(..., ge=1, description="Target library identifier.")
    release_dependencies: list[LibraryReleaseDependencyRecord] = Field(
        default_factory=list,
        description="Direct dependencies now assigned to the library's working release.",
    )

    model_config = ConfigDict(frozen=True)
