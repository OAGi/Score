"""Repository row model for namespace resources."""


from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, ConfigDict

from app.repositories.models.library import LibrarySummaryRow


class NamespaceRow(BaseModel):
    """Represent NamespaceRow."""
    namespace_id: int
    library: LibrarySummaryRow
    uri: str
    prefix: str | None = None
    description: str | None = None
    is_std_nmsp: bool
    owner_user_id: int
    created_by: int
    creation_timestamp: datetime
    last_updated_by: int
    last_update_timestamp: datetime

    model_config = ConfigDict(frozen=True, from_attributes=True)


class NamespaceSummaryRow(BaseModel):
    """Repository summary row for namespace references."""

    namespace_id: int
    uri: str
    prefix: str | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)
