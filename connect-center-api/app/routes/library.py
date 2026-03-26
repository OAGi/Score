"""Library API routes.

Provides read-only endpoints for listing libraries and retrieving a single
library by ID. Supports filtering, sorting, and date-range queries.

Key features:
- Pagination via `limit`/`offset`.
- Multi-column sorting via `order_by` with allowlisted columns.
- Filtering on library attributes (name, type, organization, domain, state, description, is_default).
- Date range filters for creation and last update timestamps.
- Standardized error responses for invalid query parameters and missing records.
"""


from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, Path, Query, status

from app.deps import get_library_service
from app.routes.models.library import GetLibraryByLibraryIdResponse, GetLibraryListResponse, LibraryEntry
from app.routes.utils.date import parse_date_range
from app.services.library_service import LibraryService
from app.types.identifiers import LibraryId

router = APIRouter(prefix="/libraries", tags=["library"])


@router.get(
    "",
    summary="List libraries",
    description="Retrieve a paginated list of libraries.",
    response_model=GetLibraryListResponse,
)
async def get_library_list(
    name: str | None = Query(default=None, description="Filter by library name (partial match)."),
    type: str | None = Query(default=None, description="Filter by library type (partial match)."),
    organization: str | None = Query(default=None, description="Filter by organization (partial match)."),
    domain: str | None = Query(default=None, description="Filter by domain (partial match)."),
    state: str | None = Query(default=None, description="Filter by state (partial match)."),
    description: str | None = Query(default=None, description="Filter by description (partial match)."),
    is_default: bool | None = Query(default=None, description="Filter by default library flag."),
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
            "Allowed columns: name, type, organization, domain, state, description, is_default, creation_timestamp, last_update_timestamp."
        ),
    ),
    offset: int = Query(default=0, ge=0, description="The offset from the beginning of the list."),
    limit: int = Query(default=10, ge=1, le=100, description="The maximum number of items to return."),
    library_service: LibraryService = Depends(get_library_service),
) -> GetLibraryListResponse:
    """Return a paginated list of libraries.

    Args:
        name: Optional name filter.
        type: Optional type filter.
        organization: Optional organization filter.
        domain: Value for `domain`.
        state: Optional lifecycle state filter.
        description: Optional textual description filter or payload field.
        is_default: Optional default-library flag filter.
        created_on: Optional creation-time filter in ISO-8601 range form.
        last_updated_on: Optional last-update-time filter in ISO-8601 range form.
        order_by: Sort expression used for ordering the result set.
        offset: Zero-based index of the first item to include in the page.
        limit: Maximum number of items to return.
        library_service: Library service dependency.

    Returns:
        Paginated response containing matching resources.
    """
    try:
        created_range = parse_date_range(created_on)
        updated_range = parse_date_range(last_updated_on)
        page = await library_service.list(
            limit=limit,
            offset=offset,
            order_by=order_by,
            name=name,
            type=type,
            organization=organization,
            domain=domain,
            state=state,
            description=description,
            is_default=is_default,
            created_on=created_range,
            last_updated_on=updated_range,
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the parameters and try again.", "cause": str(e)},
        )

    return GetLibraryListResponse(
        items=[LibraryEntry.model_validate(item, from_attributes=True) for item in page.items],
        total_items=page.total,
        limit=limit,
        offset=offset,
    )


@router.get(
    "/{library_id}",
    summary="Retrieve a library",
    description="Retrieve a library by ID.",
    response_model=GetLibraryByLibraryIdResponse,
)
async def get_library_by_library_id(
    library_id: LibraryId = Path(..., description="ID of the library to retrieve."),
    library_service: LibraryService = Depends(get_library_service),
) -> GetLibraryByLibraryIdResponse:
    """Return a library by ID.

    Args:
        library_id: Library identifier used to scope the query.
        library_service: Library service dependency.

    Returns:
        Response payload for the requested resource.
    """
    row = await library_service.get(library_id)
    if row is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "message": "The library was not found.",
                "cause": f"No library exists with ID {int(library_id)}.",
            },
        )
    return GetLibraryByLibraryIdResponse.model_validate(row, from_attributes=True)
