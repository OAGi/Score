"""Repository row model for business context resources."""


from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field

from app.repositories.models.ctx_scheme import CtxSchemeValueSummaryRow


class BizCtxRow(BaseModel):
    """Represent BizCtxRow."""
    biz_ctx_id: int
    guid: str
    name: str | None = None
    values: list[BizCtxValueRow] = Field(default_factory=list)
    created_by: int
    creation_timestamp: datetime
    last_updated_by: int
    last_update_timestamp: datetime

    model_config = ConfigDict(frozen=True, from_attributes=True)


class BizCtxValueRow(BaseModel):
    """Repository row for business-context values."""

    biz_ctx_value_id: int
    ctx_scheme_value: CtxSchemeValueSummaryRow

    model_config = ConfigDict(frozen=True, from_attributes=True)


class BizCtxValueDetailRow(BaseModel):
    """Repository row for business-context value detail lookups."""

    biz_ctx_value_id: int
    biz_ctx_id: int
    ctx_scheme_value_id: int

    model_config = ConfigDict(frozen=True, from_attributes=True)
