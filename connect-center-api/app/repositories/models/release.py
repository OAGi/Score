"""Repository row model for release resources."""


from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, ConfigDict

from app.repositories.models.library import LibrarySummaryRow
from app.repositories.models.namespace import NamespaceSummaryRow


class ReleaseSummaryRow(BaseModel):
    """Repository summary row for release references."""

    release_id: int
    release_num: str | None = None
    state: str | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class ReleaseRow(BaseModel):
    """Represent ReleaseRow."""
    release_id: int
    library: LibrarySummaryRow
    guid: str
    release_num: str | None = None
    release_note: str | None = None
    release_license: str | None = None
    namespace: NamespaceSummaryRow | None = None
    state: str
    created_by: int
    creation_timestamp: datetime
    last_updated_by: int
    last_update_timestamp: datetime
    is_latest: bool = False
    prev_release: ReleaseSummaryRow | None = None
    next_release: ReleaseSummaryRow | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)
