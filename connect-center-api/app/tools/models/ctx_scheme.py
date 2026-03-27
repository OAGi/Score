"""Models for Context Scheme MCP tools."""

from __future__ import annotations

from pydantic import BaseModel, ConfigDict, Field

from app.services.utils.string import Guid
from app.tools.models.shared import WhoAndWhen


class CtxCategorySummaryResponse(BaseModel):
    """Context category summary embedded in context-scheme responses."""

    ctx_category_id: int
    name: str

    model_config = ConfigDict(frozen=True, from_attributes=True)


class CtxSchemeValueResponse(BaseModel):
    """Context-scheme value payload for MCP tools."""

    ctx_scheme_value_id: int
    guid: Guid
    value: str
    meaning: str | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class CtxSchemeListEntryResponse(BaseModel):
    """Context-scheme list payload for MCP tools."""

    ctx_scheme_id: int
    guid: Guid
    scheme_id: str
    scheme_name: str | None = None
    description: str | None = None
    scheme_agency_id: str
    scheme_version_id: str
    ctx_category: CtxCategorySummaryResponse
    values: list[CtxSchemeValueResponse] = Field(default_factory=list)
    created: WhoAndWhen
    last_updated: WhoAndWhen

    model_config = ConfigDict(frozen=True, from_attributes=True)


class CtxSchemeDetailResponse(BaseModel):
    """Context-scheme detail payload for MCP tools."""

    ctx_scheme_id: int
    guid: Guid
    scheme_id: str
    scheme_name: str | None = None
    description: str | None = None
    scheme_agency_id: str
    scheme_version_id: str
    ctx_category: CtxCategorySummaryResponse | None = None
    values: list[CtxSchemeValueResponse] = Field(default_factory=list)
    created: WhoAndWhen
    last_updated: WhoAndWhen

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetCtxSchemeResponse(CtxSchemeDetailResponse):
    """Response for get_context_scheme tool."""


class GetCtxSchemePaginationResponse(BaseModel):
    """Response for get_context_schemes tool."""

    total_items: int
    offset: int
    limit: int
    items: list[CtxSchemeListEntryResponse]

    model_config = ConfigDict(frozen=True)


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

    model_config = ConfigDict(frozen=True)


class DeleteCtxSchemeValueResponse(BaseModel):
    """Response for delete_context_scheme_value tool."""

    ctx_scheme_value_id: int | None = None
    message: str | None = None

    model_config = ConfigDict(frozen=True)
