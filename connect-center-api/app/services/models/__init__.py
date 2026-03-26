"""Service-layer request/response models."""
from __future__ import annotations

from datetime import datetime

from dataclasses import dataclass

from app.services.models.app_user import UserSummary


@dataclass(kw_only=True)
class WhoAndWhen:
    """Who-and-when information."""

    who: UserSummary
    when: datetime
