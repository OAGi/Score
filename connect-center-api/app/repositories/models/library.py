"""Repository row model for library resources."""


from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, ConfigDict


class LibraryRow(BaseModel):
    """Represent LibraryRow."""
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
    created_by: int
    creation_timestamp: datetime
    last_updated_by: int
    last_update_timestamp: datetime

    model_config = ConfigDict(frozen=True, from_attributes=True)


class LibrarySummaryRow(BaseModel):
    """Repository summary row for library references."""

    library_id: int
    name: str

    model_config = ConfigDict(frozen=True, from_attributes=True)
