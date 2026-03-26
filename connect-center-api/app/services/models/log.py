from __future__ import annotations

from dataclasses import dataclass


@dataclass(kw_only=True)
class LogSummaryServiceRecord:
    """Service-layer revision-log summary."""

    log_id: int
    revision_num: int
    revision_tracking_num: int
