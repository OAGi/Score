"""Models for Context Category MCP tools."""

from __future__ import annotations

from pydantic import BaseModel, ConfigDict

from app.services.utils.string import Guid
from app.tools.models.shared import WhoAndWhen


class CtxCategoryEntryResponse(BaseModel):
    """Context-category payload for MCP tools."""

    ctx_category_id: int
    guid: Guid
    name: str
    description: str | None = None
    created: WhoAndWhen
    last_updated: WhoAndWhen

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetCtxCategoryResponse(CtxCategoryEntryResponse):
    """Response for get_context_category tool."""


class GetCtxCategoryPaginationResponse(BaseModel):
    """Response for get_context_categories tool."""

    total_items: int
    offset: int
    limit: int
    items: list[CtxCategoryEntryResponse]

    model_config = ConfigDict(frozen=True)


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

    model_config = ConfigDict(frozen=True)
