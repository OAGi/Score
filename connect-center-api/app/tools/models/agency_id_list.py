"""Models for Agency ID List MCP tools."""

from __future__ import annotations

from app.routes.models.agency_id_list import (
    GetAgencyIDListResponse,
    GetAgencyIdListByAgencyIdListManifestIdResponse,
)


class GetAgencyIdListResponse(GetAgencyIdListByAgencyIdListManifestIdResponse):
    """Response for get_agency_id_list tool."""


class GetAgencyIdListPaginationResponse(GetAgencyIDListResponse):
    """Response for get_agency_id_lists tool."""
