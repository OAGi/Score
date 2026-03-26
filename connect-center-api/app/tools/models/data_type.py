"""Models for Data Type MCP tools."""

from __future__ import annotations

from app.routes.models.data_type import GetDataTypeByDataTypeManifestIdResponse, GetDataTypeListResponse


class GetDataTypeResponse(GetDataTypeByDataTypeManifestIdResponse):
    """Response for get_data_type tool."""


class GetDataTypePaginationResponse(GetDataTypeListResponse):
    """Response for get_data_types tool."""
