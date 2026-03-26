"""Models for XBT MCP tools."""

from __future__ import annotations

from app.routes.models.xbt import GetXbtByXbtManifestIdResponse


class GetXbtResponse(GetXbtByXbtManifestIdResponse):
    """Response for get_xbt tool."""
