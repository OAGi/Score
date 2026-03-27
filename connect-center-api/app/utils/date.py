"""Shared parsing utilities for date range query parameters."""

from __future__ import annotations

from datetime import datetime

from app.services.utils.date import DateRange


def parse_date_range(range_str: str | None) -> DateRange | None:
    """Parse an inclusive date range in the form: '[start~end]'."""
    if not range_str:
        return None
    s = range_str.strip()
    if not (s.startswith("[") and s.endswith("]") and "~" in s):
        raise ValueError("Invalid date range format. Expected '[before~after]'.")
    inner = s[1:-1]
    start_s, end_s = inner.split("~", 1)
    after = _parse_dt(start_s.strip()) if start_s.strip() else None
    before = _parse_dt(end_s.strip()) if end_s.strip() else None
    return DateRange(before=before, after=after)


def _parse_dt(value: str) -> datetime:
    """Parse ISO-8601 date/time strings with basic 'Z' handling."""
    v = value
    if v.endswith("Z"):
        v = v[:-1] + "+00:00"
    if len(v) == 10 and v[4] == "-" and v[7] == "-":
        v = v + "T00:00:00"
    return datetime.fromisoformat(v)
