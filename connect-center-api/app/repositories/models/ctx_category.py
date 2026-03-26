"""Repository row model for context category resources."""


from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, ConfigDict


class ContextCategoryRow(BaseModel):
    """Represent ContextCategoryRow."""
    ctx_category_id: int
    guid: str
    name: str
    description: str | None = None
    created_by: int
    creation_timestamp: datetime
    last_updated_by: int
    last_update_timestamp: datetime

    model_config = ConfigDict(frozen=True, from_attributes=True)


class ContextCategorySummaryRow(BaseModel):
    """Repository summary row for context-category references."""

    ctx_category_id: int
    name: str

    model_config = ConfigDict(frozen=True, from_attributes=True)
