"""Models for Business Context MCP tools."""

from __future__ import annotations

from pydantic import BaseModel, ConfigDict, Field

from app.services.utils.string import Guid
from app.tools.models.shared import WhoAndWhen


class CtxSchemeValueSummaryResponse(BaseModel):
    """Context scheme value summary embedded in business-context responses."""

    ctx_scheme_value_id: int
    value: str

    model_config = ConfigDict(frozen=True, from_attributes=True)


class BizCtxValueResponse(BaseModel):
    """Business-context value payload for MCP tools."""

    biz_ctx_value_id: int
    ctx_scheme_value: CtxSchemeValueSummaryResponse

    model_config = ConfigDict(frozen=True, from_attributes=True)


class BizCtxResponseEntry(BaseModel):
    """Business-context payload for MCP tools."""

    biz_ctx_id: int
    guid: Guid
    name: str
    values: list[BizCtxValueResponse] = Field(default_factory=list)
    created: WhoAndWhen
    last_updated: WhoAndWhen

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetBizCtxResponse(BizCtxResponseEntry):
    """Response for get_business_context tool."""


class GetBizCtxPaginationResponse(BaseModel):
    """Response for get_business_contexts tool."""

    total_items: int
    offset: int
    limit: int
    items: list[BizCtxResponseEntry]

    model_config = ConfigDict(frozen=True)


class CreateBizCtxResponse(BaseModel):
    """Response for create_business_context tool."""

    biz_ctx_id: int


class CreateBizCtxValueResponse(BaseModel):
    """Response for create_business_context_value tool."""

    biz_ctx_value_id: int


class UpdateBizCtxResponse(BaseModel):
    """Response for update_business_context tool."""

    biz_ctx_id: int
    updates: list[str]


class UpdateBizCtxValueResponse(BaseModel):
    """Response for update_business_context_value tool."""

    biz_ctx_value_id: int
    updates: list[str]


class DeleteBizCtxResponse(BaseModel):
    """Response for delete_business_context tool."""

    biz_ctx_id: int | None = None
    message: str | None = None

    model_config = ConfigDict(frozen=True)


class DeleteBizCtxValueResponse(BaseModel):
    """Response for delete_business_context_value tool."""

    biz_ctx_value_id: int | None = None
    message: str | None = None

    model_config = ConfigDict(frozen=True)
