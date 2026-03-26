"""Models for Context Scheme MCP tools."""

from __future__ import annotations

from pydantic import BaseModel

from app.routes.models.ctx_scheme import (
    GetCtxSchemeByCtxSchemeIdResponse,
    GetCtxSchemeListResponse,
)


class GetCtxSchemeResponse(GetCtxSchemeByCtxSchemeIdResponse):
    """Response for get_context_scheme tool."""


class GetCtxSchemePaginationResponse(GetCtxSchemeListResponse):
    """Response for get_context_schemes tool."""


class CreateCtxSchemeResponse(BaseModel):
    """Response for create_context_scheme tool."""

    ctx_scheme_id: int


class CreateCtxSchemeValueResponse(BaseModel):
    """Response for create_context_scheme_value tool."""

    ctx_scheme_value_id: int


class UpdateCtxSchemeResponse(BaseModel):
    """Response for update_context_scheme tool."""

    ctx_scheme_id: int
    updates: list[str]


class UpdateCtxSchemeValueResponse(BaseModel):
    """Response for update_context_scheme_value tool."""

    ctx_scheme_value_id: int
    updates: list[str]


class DeleteCtxSchemeResponse(BaseModel):
    """Response for delete_context_scheme tool."""

    ctx_scheme_id: int | None = None
    message: str | None = None


class DeleteCtxSchemeValueResponse(BaseModel):
    """Response for delete_context_scheme_value tool."""

    ctx_scheme_value_id: int | None = None
    message: str | None = None
