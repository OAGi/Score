"""Models for Core Component MCP tools."""

from __future__ import annotations

from app.routes.models.core_component import (
    GetAccByAccManifestIdResponse,
    GetAsccpByAsccpManifestIdResponse,
    GetBccpByBccpManifestIdResponse,
    GetCoreComponentListResponse,
)


class GetCoreComponentPaginationResponse(GetCoreComponentListResponse):
    """Response for get_core_components tool."""


class GetAccResponse(GetAccByAccManifestIdResponse):
    """Response for get_acc tool."""


class GetAsccpResponse(GetAsccpByAsccpManifestIdResponse):
    """Response for get_asccp tool."""


class GetBccpResponse(GetBccpByBccpManifestIdResponse):
    """Response for get_bccp tool."""
