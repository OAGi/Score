"""Namespace API routes.

Provides read-only endpoints for listing namespaces and retrieving a single
namespace by ID. Supports filtering, sorting, and date-range queries.

Key features:
- Pagination via `limit`/`offset`.
- Multi-column sorting via `order_by` with allowlisted columns.
- Filtering on namespace attributes (library_id, uri, prefix, is_std_nmsp).
- Date range filters for creation and last update timestamps.
- Standardized error responses for invalid query parameters and missing records.
"""


from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, Path, Query, status

from app.deps import get_namespace_service
from app.routes.models.namespace import (
    GetNamespaceByNamespaceIdResponse,
    GetNamespaceListResponse,
    NamespaceEntry,
)
from app.routes.utils.date import parse_date_range
from app.services.namespace_service import NamespaceService
from app.types.identifiers import LibraryId
from app.types.identifiers import NamespaceId

router = APIRouter(prefix="/namespaces", tags=["namespace"])


@router.get(
    "",
    summary="List namespaces",
    description="Retrieve a paginated list of namespaces.",
    response_model=GetNamespaceListResponse,
)
async def get_namespace_list(
    library_id: LibraryId | None = Query(default=None, ge=1, description="Filter by library ID."),
    uri: str | None = Query(default=None, description="Filter by namespace URI (partial match)."),
    prefix: str | None = Query(default=None, description="Filter by namespace prefix (partial match)."),
    is_std_nmsp: bool | None = Query(default=None, description="Filter by standard namespace flag."),
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
            "Allowed columns: uri, prefix, is_std_nmsp, creation_timestamp, last_update_timestamp."
        ),
    ),
    offset: int = Query(default=0, ge=0, description="The offset from the beginning of the list."),
    limit: int = Query(default=10, ge=1, le=100, description="The maximum number of items to return."),
    namespace_service: NamespaceService = Depends(get_namespace_service),
) -> GetNamespaceListResponse:
    """Return a paginated list of namespaces.

    Args:
        library_id: Library identifier used to scope the query.
        uri: Optional namespace URI filter.
        prefix: Optional namespace prefix filter.
        is_std_nmsp: Optional standard-namespace flag filter.
        created_on: Optional creation-time filter in ISO-8601 range form.
        last_updated_on: Optional last-update-time filter in ISO-8601 range form.
        order_by: Sort expression used for ordering the result set.
        offset: Zero-based index of the first item to include in the page.
        limit: Maximum number of items to return.
        namespace_service: Namespace service dependency.

    Returns:
        Paginated response containing matching resources.
    """
    try:
        created_range = parse_date_range(created_on)
        updated_range = parse_date_range(last_updated_on)
        page = await namespace_service.list(
            limit=limit,
            offset=offset,
            order_by=order_by,
            library_id=library_id,
            uri=uri,
            prefix=prefix,
            is_std_nmsp=is_std_nmsp,
            created_on=created_range,
            last_updated_on=updated_range,
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the parameters and try again.", "cause": str(e)},
        )

    return GetNamespaceListResponse(
        items=[NamespaceEntry.model_validate(item, from_attributes=True) for item in page.items],
        total_items=page.total,
        limit=limit,
        offset=offset,
    )


@router.get(
    "/{namespace_id}",
    summary="Retrieve a namespace",
    description="Retrieve a namespace by ID.",
    response_model=GetNamespaceByNamespaceIdResponse,
)
async def get_namespace_by_namespace_id(
    namespace_id: NamespaceId = Path(..., description="ID of the namespace to retrieve."),
    namespace_service: NamespaceService = Depends(get_namespace_service),
) -> GetNamespaceByNamespaceIdResponse:
    """Return a namespace by ID.

    Args:
        namespace_id: Namespace identifier.
        namespace_service: Namespace service dependency.

    Returns:
        Response payload for the requested resource.
    """
    row = await namespace_service.get(namespace_id)
    if row is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "message": "The namespace was not found.",
                "cause": f"No namespace exists with ID {int(namespace_id)}.",
            },
        )
    return GetNamespaceByNamespaceIdResponse.model_validate(row, from_attributes=True)
