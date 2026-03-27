"""Repository row model for tag resources."""


from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, ConfigDict


class TagRow(BaseModel):
    """Represent TagRow."""
    tag_id: int
    name: str
    description: str | None = None
    color: str | None = None
    text_color: str | None = None
    created_by: int
    last_updated_by: int
    creation_timestamp: datetime
    last_update_timestamp: datetime

    model_config = ConfigDict(frozen=True, from_attributes=True)
