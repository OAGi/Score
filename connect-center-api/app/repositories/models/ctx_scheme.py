"""Repository row model for context scheme resources."""


from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field

from app.repositories.models.ctx_category import ContextCategorySummaryRow


class CtxSchemeRow(BaseModel):
    """Represent CtxSchemeRow."""
    ctx_scheme_id: int
    guid: str
    scheme_id: str
    scheme_name: str
    description: str | None = None
    scheme_agency_id: str
    scheme_version_id: str
    ctx_category: ContextCategorySummaryRow | None = None
    values: list[CtxSchemeValueRow] = Field(default_factory=list)
    created_by: int
    creation_timestamp: datetime
    last_updated_by: int
    last_update_timestamp: datetime

    model_config = ConfigDict(frozen=True, from_attributes=True)


class CtxSchemeValueRow(BaseModel):
    """Repository row for context-scheme values."""

    ctx_scheme_value_id: int
    guid: str
    owner_ctx_scheme_id: int
    value: str
    meaning: str | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class CtxSchemeValueSummaryRow(BaseModel):
    """Repository summary row for context-scheme values."""

    ctx_scheme_value_id: int
    value: str

    model_config = ConfigDict(frozen=True, from_attributes=True)
