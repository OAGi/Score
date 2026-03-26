"""Models for Release MCP tools."""

from __future__ import annotations

from app.routes.models.release import GetReleaseByReleaseIdResponse, GetReleaseListResponse


class GetReleaseResponse(GetReleaseByReleaseIdResponse):
    """Response for get_release tool."""


class GetReleasePaginationResponse(GetReleaseListResponse):
    """Response for get_releases tool."""
