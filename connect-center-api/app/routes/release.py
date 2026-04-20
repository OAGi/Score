"""Release API routes.

Provides read-only endpoints for listing releases, retrieving a single
release by ID, and retrieving a library's `Working` release. Supports
filtering, sorting, and date-range queries.

Key features:
- Pagination via `limit`/`offset`.
- Multi-column sorting via `order_by` with allowlisted columns.
- Filtering on release attributes (library_id, release_num, state).
- Retrieve a library's `Working` release by `library_id`.
- Date range filters for creation and last update timestamps.
- Standardized error responses for invalid query parameters and missing records.
"""


from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, Path, Query, status

from app.deps import get_release_service
from app.routes.models.release import GetReleaseByReleaseIdResponse, GetReleaseListResponse, ReleaseEntry
from app.utils.date import parse_date_range
from app.services.release_service import ReleaseService
from app.types.identifiers import LibraryId
from app.types.identifiers import ReleaseId

router = APIRouter(prefix="/releases", tags=["release"])


@router.get(
    "",
    summary="List releases",
    description="Retrieve a paginated list of releases.",
    response_model=GetReleaseListResponse,
)
async def get_release_list(
    library_id: LibraryId | None = Query(default=None, ge=1, description="Filter by library ID."),
    release_num: str | None = Query(default=None, description="Filter by release number (partial match)."),
    state: str | None = Query(default=None, description="Filter by release state."),
    created_on: str | None = Query(
        default=None,
        description="Filter by creation date using an inclusive range: '[before~after]'.",
    ),
    last_updated_on: str | None = Query(
        default=None,
        description="Filter by last update date using an inclusive range: '[before~after]'.",
    ),
    order_by: str | None = Query(
        default=None,
        description=(
            "Comma-separated list of properties to order results by. Prefix with '-' for descending, '+' for ascending. "
            "Allowed columns: release_num, state, creation_timestamp, last_update_timestamp."
        ),
    ),
    offset: int = Query(default=0, ge=0, description="The offset from the beginning of the list."),
    limit: int = Query(default=10, ge=1, le=100, description="The maximum number of items to return."),
    release_service: ReleaseService = Depends(get_release_service),
) -> GetReleaseListResponse:
    """Return a paginated list of releases.

    Args:
        library_id: Library identifier used to scope the query.
        release_num: Optional release number filter.
        state: Optional lifecycle state filter.
        created_on: Optional creation-time filter in ISO-8601 range form.
        last_updated_on: Optional last-update-time filter in ISO-8601 range form.
        order_by: Sort expression used for ordering the result set.
        offset: Zero-based index of the first item to include in the page.
        limit: Maximum number of items to return.
        release_service: Release service dependency.

    Returns:
        Paginated response containing matching resources.
    """
    try:
        created_range = parse_date_range(created_on)
        updated_range = parse_date_range(last_updated_on)
        page = await release_service.list(
            limit=limit,
            offset=offset,
            order_by=order_by,
            library_id=library_id,
            release_num=release_num,
            state=state,
            created_on=created_range,
            last_updated_on=updated_range,
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the parameters and try again.", "cause": str(e)},
        )

    return GetReleaseListResponse(
        items=[ReleaseEntry.model_validate(item, from_attributes=True) for item in page.items],
        total_items=page.total,
        limit=limit,
        offset=offset,
    )


@router.get(
    "/working",
    summary="Retrieve the Working release",
    description="Retrieve the `Working` release for a library by library ID.",
    response_model=GetReleaseByReleaseIdResponse,
)
async def get_working_release(
    library_id: LibraryId = Query(..., ge=1, description="ID of the library whose `Working` release to retrieve."),
    release_service: ReleaseService = Depends(get_release_service),
) -> GetReleaseByReleaseIdResponse:
    """Return the `Working` release for a library.

    Args:
        library_id: Library identifier used to scope the query.
        release_service: Release service dependency.

    Returns:
        Response payload for the requested resource.
    """
    row = await release_service.get_by_library_id_and_release_num(
        library_id=library_id,
        release_num="Working",
    )
    if row is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "message": "The Working release was not found.",
                "cause": f"No Working release exists for library ID {int(library_id)}.",
            },
        )
    return GetReleaseByReleaseIdResponse.model_validate(row, from_attributes=True)


@router.get(
    "/{release_id}",
    summary="Retrieve a release",
    description="Retrieve a release by ID.",
    response_model=GetReleaseByReleaseIdResponse,
)
async def get_release_by_release_id(
    release_id: ReleaseId = Path(..., description="ID of the release to retrieve."),
    release_service: ReleaseService = Depends(get_release_service),
) -> GetReleaseByReleaseIdResponse:
    """Return a release by ID.

    Args:
        release_id: Release identifier used to scope the query.
        release_service: Release service dependency.

    Returns:
        Response payload for the requested resource.
    """
    row = await release_service.get(release_id)
    if row is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "message": "The release was not found.",
                "cause": f"No release exists with ID {int(release_id)}.",
            },
        )
    return GetReleaseByReleaseIdResponse.model_validate(row, from_attributes=True)
