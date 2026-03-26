"""Models for Library MCP tools."""

from __future__ import annotations

from app.routes.models.library import GetLibraryByLibraryIdResponse, GetLibraryListResponse


class GetLibraryResponse(GetLibraryByLibraryIdResponse):
    """Response for get_library tool."""


class GetLibraryPaginationResponse(GetLibraryListResponse):
    """Response for get_libraries tool."""
