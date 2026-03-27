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


class GetLibraryByLibraryIdResponse(LibraryEntry):
    """Response payload for retrieving a library by ID."""

    model_config = ConfigDict(frozen=True)

