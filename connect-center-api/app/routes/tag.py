"""Tag API routes."""


from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, Query, status

from app.deps import get_tag_service
from app.routes.models.tag import GetTagListResponse, TagEntry
from app.utils.date import parse_date_range
from app.services.tag_service import TagService

router = APIRouter(prefix="/tags", tags=["tag"])


@router.get(
    "",
    summary="List tags",
    description="Retrieve a paginated list of tags.",
    response_model=GetTagListResponse,
)
async def get_tag_list(
    name: str | None = Query(default=None, description="Filter by tag name (partial match)."),
    description: str | None = Query(default=None, description="Filter by tag description (partial match)."),
    created_on: str | None = Query(default=None, description="Filter by creation date range '[before~after]'."),
    last_updated_on: str | None = Query(default=None, description="Filter by last update date range '[before~after]'."),
    order_by: str | None = Query(
        default=None,
        description=(
            "Comma-separated list of properties to order by. Prefix with '-' for descending, '+' for ascending. "
            "Allowed columns: name, description, creation_timestamp, last_update_timestamp."
        ),
    ),
    offset: int = Query(default=0, ge=0, description="The offset from the beginning of the list."),
    limit: int = Query(default=10, ge=1, le=100, description="The maximum number of items to return."),
    tag_service: TagService = Depends(get_tag_service),
) -> GetTagListResponse:
    """Return a paginated list of tags.

    Args:
        name: Optional name filter.
        description: Optional textual description filter or payload field.
        created_on: Optional creation-time filter in ISO-8601 range form.
        last_updated_on: Optional last-update-time filter in ISO-8601 range form.
        order_by: Sort expression used for ordering the result set.
        offset: Zero-based index of the first item to include in the page.
        limit: Maximum number of items to return.
        tag_service: Tag service dependency.

    Returns:
        Paginated response containing matching resources.
    """
    try:
        created_range = parse_date_range(created_on)
        updated_range = parse_date_range(last_updated_on)
        page = await tag_service.list(
            limit=limit,
            offset=offset,
            order_by=order_by,
            name=name,
            description=description,
            created_on=created_range,
            last_updated_on=updated_range,
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the parameters and try again.", "cause": str(e)},
        )

    return GetTagListResponse(
        items=[TagEntry.model_validate(item, from_attributes=True) for item in page.items],
        total_items=page.total,
        limit=limit,
        offset=offset,
    )
