"""Code List API routes.

Provides read-only endpoints for listing code lists by release and retrieving
individual code lists by manifest ID.
"""


from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, Path, Query, status

from app.deps import get_code_list_service
from app.routes.models.code_list import (
    CodeListEntry,
    GetCodeListByCodeListManifestIdResponse,
    GetCodeListListResponse,
)
from app.routes.utils.date import parse_date_range
from app.services.code_list_service import CodeListService
from app.types.identifiers import CodeListManifestId
from app.types.identifiers import ReleaseId

router = APIRouter(prefix="/code-lists", tags=["code-list"])


@router.get(
    "",
    summary="List code lists",
    description="Retrieve a paginated list of code lists.",
    response_model=GetCodeListListResponse,
)
async def get_code_list_list(
    release_id: ReleaseId = Query(..., ge=1, description="Filter by release ID."),
    name: str | None = Query(default=None, description="Filter by name (partial match)."),
    list_id: str | None = Query(default=None, description="Filter by list ID (partial match)."),
    version_id: str | None = Query(default=None, description="Filter by version ID (partial match)."),
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
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> GetCodeListListResponse:
    """Return a paginated list of code lists.

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
        code_list_service: Code-list service dependency.

    Returns:
        Paginated response containing matching resources.
    """
    try:
        created_range = parse_date_range(created_on)
        updated_range = parse_date_range(last_updated_on)
        page = await code_list_service.list(
            release_id=release_id,
            limit=limit,
            offset=offset,
            order_by=order_by,
            name=name,
            list_id=list_id,
            version_id=version_id,
            created_on=created_range,
            last_updated_on=updated_range,
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the parameters and try again.", "cause": str(e)},
        )

    return GetCodeListListResponse(
        items=[CodeListEntry.model_validate(item, from_attributes=True) for item in page.items],
        total_items=page.total,
        limit=limit,
        offset=offset,
    )


@router.get(
    "/{code_list_manifest_id}",
    summary="Retrieve a code list",
    description="Retrieve a code list by manifest ID.",
    response_model=GetCodeListByCodeListManifestIdResponse,
)
async def get_code_list_by_code_list_manifest_id(
    code_list_manifest_id: CodeListManifestId = Path(..., ge=1, description="Code list manifest ID."),
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> GetCodeListByCodeListManifestIdResponse:
    """Return a code list by manifest ID.

    Args:
        code_list_manifest_id: Code list manifest identifier.
        code_list_service: Code-list service dependency.

    Returns:
        Response payload for the requested resource.
    """
    row = await code_list_service.get(code_list_manifest_id)
    if row is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "message": "The code list was not found.",
                "cause": f"No code list exists with manifest ID {int(code_list_manifest_id)}.",
            },
        )
    return GetCodeListByCodeListManifestIdResponse.model_validate(row, from_attributes=True)
