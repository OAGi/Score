"""Repository row model for XBT resources."""


from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, ConfigDict

from app.repositories.models.library import LibrarySummaryRow
from app.repositories.models.log import LogSummaryRow
from app.repositories.models.release import ReleaseSummaryRow


class XbtRow(BaseModel):
    """Represent XbtRow."""
    xbt_manifest_id: int
    xbt_id: int
    guid: str
    name: str | None = None
    builtIn_type: str | None = None
    jbt_draft05_map: str | None = None
    openapi30_map: str | None = None
    avro_map: str | None = None
    subtype_of_xbt: XbtSummaryRow | None = None
    schema_definition: str | None = None
    revision_doc: str | None = None
    state: int | None = None
    is_deprecated: bool
    library: LibrarySummaryRow
    release: ReleaseSummaryRow
    log: LogSummaryRow | None = None
    owner_user_id: int
    created_by: int
    last_updated_by: int
    creation_timestamp: datetime
    last_update_timestamp: datetime

    model_config = ConfigDict(frozen=True, from_attributes=True)


class XbtSummaryRow(BaseModel):
    """Repository summary row for subtype-of XBT references."""

    xbt_manifest_id: int

    model_config = ConfigDict(frozen=True, from_attributes=True)
