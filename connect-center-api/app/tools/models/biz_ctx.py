"""Models for Business Context MCP tools."""

from __future__ import annotations

from pydantic import BaseModel

from app.routes.models.biz_ctx import GetBizCtxByBizCtxIdResponse, GetBizCtxListResponse


class GetBizCtxResponse(GetBizCtxByBizCtxIdResponse):
    """Response for get_business_context tool."""


class GetBizCtxPaginationResponse(GetBizCtxListResponse):
    """Response for get_business_contexts tool."""


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


class DeleteBizCtxValueResponse(BaseModel):
    """Response for delete_business_context_value tool."""

    biz_ctx_value_id: int | None = None
    message: str | None = None
