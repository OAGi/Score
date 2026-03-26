"""Models for Tag MCP tools."""

from __future__ import annotations

from app.routes.models.tag import GetTagListResponse


class GetTagPaginationResponse(GetTagListResponse):
    """Response for get_tags tool."""
