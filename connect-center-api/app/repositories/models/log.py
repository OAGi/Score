"""Repository row model for revision log summaries."""


from __future__ import annotations

from pydantic import BaseModel, ConfigDict


class LogSummaryRow(BaseModel):
    """Repository summary row for revision-log references."""

    log_id: int
    revision_num: int | None = None
    revision_tracking_num: int | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)

