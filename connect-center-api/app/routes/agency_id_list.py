"""Agency ID List API routes.

Provides read-only endpoints for listing agency ID lists by release and
retrieving individual agency ID lists by manifest ID.
"""


from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, Path, Query, status

from app.deps import get_agency_id_list_service
from app.routes.models.agency_id_list import (
    AgencyIdListEntry,
    GetAgencyIDListResponse,
    GetAgencyIdListByAgencyIdListManifestIdResponse,
)
from app.utils.date import parse_date_range
from app.services.agency_id_list_service import AgencyIdListService
from app.types.identifiers import AgencyIdListManifestId
from app.types.identifiers import ReleaseId

router = APIRouter(prefix="/agency-id-lists", tags=["agency-id-list"])


@router.get(
    "",
    summary="List agency ID lists",
    description="Retrieve a paginated list of agency ID lists.",
    response_model=GetAgencyIDListResponse,
)
async def get_agency_id_list_list(
    release_id: ReleaseId = Query(..., ge=1, description="Filter by release ID."),
    name: str | None = Query(default=None, description="Filter by name (partial match)."),
    list_id: str | None = Query(default=None, description="Filter by list ID (partial match)."),
    version_id: str | None = Query(default=None, description="Filter by version ID (partial match)."),
    owner: str | None = Query(
        default=None,
        description="Comma-separated owner login IDs to filter by exact match. Prefix a login ID with '!' to exclude it.",
    ),
    created_on: str | None = Query(default=None, description="Filter by creation date range '[before~after]'."),
    last_updated_on: str | None = Query(default=None, description="Filter by last update date range '[before~after]'."),
    order_by: str | None = Query(
        default=None,
        description=(
            "Comma-separated list of properties to order by. Prefix with '-' for descending, '+' for ascending. "
            "Allowed columns: name, list_id, version_id, definition, creation_timestamp, last_update_timestamp."
        ),
    ),
    offset: int = Query(default=0, ge=0, description="The offset from the beginning of the list."),
    limit: int = Query(default=10, ge=1, le=100, description="The maximum number of items to return."),
    agency_id_list_service: AgencyIdListService = Depends(get_agency_id_list_service),
) -> GetAgencyIDListResponse:
    """Return a paginated list of agency ID lists.

    Args:
        release_id: Release identifier used to scope the query.
        name: Optional name filter.
        list_id: Value for `list_id`.
        version_id: Value for `version_id`.
        created_on: Optional creation-time filter in ISO-8601 range form.
        last_updated_on: Optional last-update-time filter in ISO-8601 range form.
        order_by: Sort expression used for ordering the result set.
        offset: Zero-based index of the first item to include in the page.
        limit: Maximum number of items to return.
        agency_id_list_service: Agency-ID-list service dependency.

    Returns:
        Paginated response containing matching resources.
    """
    try:
        created_range = parse_date_range(created_on)
        updated_range = parse_date_range(last_updated_on)
        page = await agency_id_list_service.list(
            release_id=release_id,
            limit=limit,
            offset=offset,
            order_by=order_by,
            name=name,
            list_id=list_id,
            version_id=version_id,
            owner=owner,
            created_on=created_range,
            last_updated_on=updated_range,
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the parameters and try again.", "cause": str(e)},
        )

    return GetAgencyIDListResponse(
        items=[AgencyIdListEntry.model_validate(item, from_attributes=True) for item in page.items],
        total_items=page.total,
        limit=limit,
        offset=offset,
    )


@router.get(
    "/{agency_id_list_manifest_id}",
    summary="Retrieve an agency ID list",
    description="Retrieve an agency ID list by manifest ID.",
    response_model=GetAgencyIdListByAgencyIdListManifestIdResponse,
)
async def get_agency_id_list_by_agency_id_list_manifest_id(
    agency_id_list_manifest_id: AgencyIdListManifestId = Path(..., ge=1, description="Agency ID list manifest ID."),
    agency_id_list_service: AgencyIdListService = Depends(get_agency_id_list_service),
) -> GetAgencyIdListByAgencyIdListManifestIdResponse:
    """Return an agency ID list by manifest ID.

    Args:
        agency_id_list_manifest_id: Agency ID list manifest identifier.
        agency_id_list_service: Agency-ID-list service dependency.

    Returns:
        Response payload for the requested resource.
    """
    row = await agency_id_list_service.get(agency_id_list_manifest_id)
    if row is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "message": "The agency ID list was not found.",
                "cause": f"No agency ID list exists with manifest ID {int(agency_id_list_manifest_id)}.",
            },
        )
    return GetAgencyIdListByAgencyIdListManifestIdResponse.model_validate(row, from_attributes=True)
