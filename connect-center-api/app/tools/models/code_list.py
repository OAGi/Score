"""Models for Code List MCP tools."""

from __future__ import annotations

from app.routes.models.code_list import GetCodeListByCodeListManifestIdResponse, GetCodeListListResponse


class GetCodeListResponse(GetCodeListByCodeListManifestIdResponse):
    """Response for get_code_list tool."""


class GetCodeListPaginationResponse(GetCodeListListResponse):
    """Response for get_code_lists tool."""
