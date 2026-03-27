"""Data Type API routes.

Provides read-only endpoints for listing data types by release and retrieving
individual data types by manifest ID.
"""


from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, Path, Query, status

from app.deps import get_data_type_service
from app.routes.models.data_type import (
    DataTypeEntry,
    GetDataTypeByDataTypeManifestIdResponse,
    GetDataTypeListResponse,
)
from app.utils.date import parse_date_range
from app.services.data_type_service import DataTypeService
from app.types.identifiers import DataTypeManifestId
from app.types.identifiers import ReleaseId

router = APIRouter(prefix="/data-types", tags=["data-type"])


@router.get(
    "",
    summary="List data types",
    description="Retrieve a paginated list of data types.",
    response_model=GetDataTypeListResponse,
)
async def get_data_type_list(
    release_id: ReleaseId = Query(..., ge=1, description="Filter by release ID."),
    den: str | None = Query(default=None, description="Filter by DEN (partial match)."),
    representation_term: str | None = Query(default=None, description="Filter by representation term (partial match)."),
    created_on: str | None = Query(default=None, description="Filter by creation date range '[before~after]'."),
    last_updated_on: str | None = Query(default=None, description="Filter by last update date range '[before~after]'."),
    order_by: str | None = Query(
        default=None,
        description=(
            "Comma-separated list of properties to order by. Prefix with '-' for descending, '+' for ascending. "
            "Allowed columns: den, data_type_term, qualifier, representation_term, six_digit_id, definition, "
            "creation_timestamp, last_update_timestamp."
        ),
    ),
    offset: int = Query(default=0, ge=0, description="The offset from the beginning of the list."),
    limit: int = Query(default=10, ge=1, le=100, description="The maximum number of items to return."),
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> GetDataTypeListResponse:
    """Return a paginated list of data types.

    Args:
        release_id: Release identifier used to scope the query.
        den: Optional Dictionary Entry Name (DEN) filter.
        representation_term: Value for `representation_term`.
        created_on: Optional creation-time filter in ISO-8601 range form.
        last_updated_on: Optional last-update-time filter in ISO-8601 range form.
        order_by: Sort expression used for ordering the result set.
        offset: Zero-based index of the first item to include in the page.
        limit: Maximum number of items to return.
        data_type_service: Data-type service dependency.

    Returns:
        Paginated response containing matching resources.
    """
    try:
        created_range = parse_date_range(created_on)
        updated_range = parse_date_range(last_updated_on)
        page = await data_type_service.list(
            release_id=release_id,
            limit=limit,
            offset=offset,
            order_by=order_by,
            den=den,
            representation_term=representation_term,
            created_on=created_range,
            last_updated_on=updated_range,
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the parameters and try again.", "cause": str(e)},
        )

    return GetDataTypeListResponse(
        items=[DataTypeEntry.model_validate(item, from_attributes=True) for item in page.items],
        total_items=page.total,
        limit=limit,
        offset=offset,
    )


@router.get(
    "/{dt_manifest_id}",
    summary="Retrieve a data type",
    description="Retrieve a data type by manifest ID.",
    response_model=GetDataTypeByDataTypeManifestIdResponse,
)
async def get_data_type_by_data_type_manifest_id(
    dt_manifest_id: DataTypeManifestId = Path(..., ge=1, description="Data type manifest ID."),
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> GetDataTypeByDataTypeManifestIdResponse:
    """Return a data type by manifest ID.

    Args:
        dt_manifest_id: Data type manifest identifier.
        data_type_service: Data-type service dependency.

    Returns:
        Response payload for the requested resource.
    """
    row = await data_type_service.get(dt_manifest_id)
    if row is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "message": "The data type was not found.",
                "cause": f"No data type exists with manifest ID {int(dt_manifest_id)}.",
            },
        )
    return GetDataTypeByDataTypeManifestIdResponse.model_validate(row, from_attributes=True)
