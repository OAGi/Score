"""Models for Context Category MCP tools."""

from __future__ import annotations

from pydantic import BaseModel

from app.routes.models.ctx_category import (
    GetContextCategoryByContextCategoryIdResponse,
    GetContextCategoryListResponse,
)


class GetCtxCategoryResponse(GetContextCategoryByContextCategoryIdResponse):
    """Response for get_context_category tool."""


class GetCtxCategoryPaginationResponse(GetContextCategoryListResponse):
    """Response for get_context_categories tool."""


class CreateCtxCategoryResponse(BaseModel):
    """Response for create_context_category tool."""

    ctx_category_id: int


class UpdateCtxCategoryResponse(BaseModel):
    """Response for update_context_category tool."""

    ctx_category_id: int
    updates: list[str]


class DeleteCtxCategoryResponse(BaseModel):
    """Response for delete_context_category tool."""

    ctx_category_id: int | None = None
    message: str | None = None
