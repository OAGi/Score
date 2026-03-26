"""Shared service-layer date types."""


from __future__ import annotations

from datetime import datetime

from dataclasses import dataclass


@dataclass(kw_only=True)
class DateRange:
    """Inclusive date range with optional lower/upper bounds."""
    before: datetime | None = None
    after: datetime | None = None
