"""Context Category API routes.

These endpoints provide CRUD operations for Context Categories, along with
filtering, pagination, and sorting support for list queries.

Key features:
- Create, update, delete, list, and retrieve context categories.
- Pagination and allowlisted sorting for list queries.
- Date range filtering on creation and last update timestamps.
- Standardized error responses for invalid parameters and missing records.
"""


from __future__ import annotations

from fastapi import APIRouter, Body, Depends, HTTPException, Path, Query, Response, status

from app.deps import get_ctx_category_service
from app.routes.models.ctx_category import (
    CreateContextCategoryRequest,
    CreateContextCategoryResponse,
    UpdateContextCategoryRequest,
    UpdateContextCategoryResponse,
    ContextCategoryEntry,
    GetContextCategoryByContextCategoryIdResponse,
    GetContextCategoryListResponse,
)
from app.utils.date import parse_date_range
from app.services.ctx_category_service import ContextCategoryService
from app.types.identifiers import ContextCategoryId
from app.types.unset import UNSET

router = APIRouter(prefix="/context-categories", tags=["context-category"])


@router.get(
    "",
    summary="List context categories",
    description="Retrieve a paginated list of context categories.",
    response_model=GetContextCategoryListResponse,
)
async def get_ctx_category_list(
    name: str | None = Query(default=None, description="Filter categories by name."),
    description: str | None = Query(default=None, description="Filter categories by description."),
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
            "Allowed columns: name, description, creation_timestamp, last_update_timestamp."
        ),
    ),
    offset: int = Query(default=0, ge=0, description="The offset from the beginning of the list."),
    limit: int = Query(default=10, ge=1, le=100, description="The maximum number of items to return."),
    context_category_service: ContextCategoryService = Depends(get_ctx_category_service),
) -> GetContextCategoryListResponse:
    """Return a paginated list of context categories.

    Args:
        name: Optional name filter.
        description: Optional textual description filter or payload field.
        created_on: Optional creation-time filter in ISO-8601 range form.
        last_updated_on: Optional last-update-time filter in ISO-8601 range form.
        order_by: Sort expression used for ordering the result set.
        offset: Zero-based index of the first item to include in the page.
        limit: Maximum number of items to return.
        context_category_service: Context category service dependency.

    Returns:
        Paginated response containing matching resources.
    """
    try:
        created_range = parse_date_range(created_on)
        updated_range = parse_date_range(last_updated_on)
        page = await context_category_service.list(
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

    return GetContextCategoryListResponse(
        items=[ContextCategoryEntry.model_validate(item, from_attributes=True) for item in page.items],
        total_items=page.total,
        limit=limit,
        offset=offset,
    )


@router.get(
    "/{context_category_id}",
    summary="Retrieve a context category",
    description="Retrieve a context category by ID.",
    response_model=GetContextCategoryByContextCategoryIdResponse,
)
async def get_ctx_category_by_context_category_id(
    context_category_id: ContextCategoryId = Path(..., description="ID of the context category to retrieve."),
    context_category_service: ContextCategoryService = Depends(get_ctx_category_service),
) -> GetContextCategoryByContextCategoryIdResponse:
    """Return a context category by ID.

    Args:
        context_category_id: Context category identifier.
        context_category_service: Context category service dependency.

    Returns:
        Response payload for the requested resource.
    """
    row = await context_category_service.get(context_category_id)

    if row is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "message": "The context category was not found.",
                "cause": f"No context category exists with ID {context_category_id}.",
            },
        )
    return GetContextCategoryByContextCategoryIdResponse.model_validate(row, from_attributes=True)


@router.post(
    "",
    status_code=status.HTTP_201_CREATED,
    summary="Create a context category",
    description="Creates a new context category in the connectCenter system.",
    response_model=CreateContextCategoryResponse,
)
async def create_ctx_category(
    payload: CreateContextCategoryRequest = Body(...),
    context_category_service: ContextCategoryService = Depends(get_ctx_category_service),
) -> CreateContextCategoryResponse:
    """Create a context category and return the new ID.

    Args:
        payload: Validated request payload.
        context_category_service: Context category service dependency.

    Returns:
        Response payload describing the newly created resource.
    """
    try:
        context_category_id = await context_category_service.create(
            name=payload.name,
            description=payload.description,
        )
        return CreateContextCategoryResponse(context_category_id=context_category_id)
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't create the context category.", "cause": str(e)},
        )


@router.post(
    "/{context_category_id}",
    summary="Update a context category by ID",
    description="Updates selected fields of an existing context category.",
    response_model=UpdateContextCategoryResponse,
)
async def update_ctx_category(
    context_category_id: ContextCategoryId = Path(..., description="ID of the context category to update."),
    payload: UpdateContextCategoryRequest = Body(...),
    context_category_service: ContextCategoryService = Depends(get_ctx_category_service),
) -> UpdateContextCategoryResponse:
    """Update an existing context category and return the updated fields.

    Args:
        context_category_id: Context category identifier.
        payload: Validated request payload.
        context_category_service: Context category service dependency.

    Returns:
        Response payload describing the updated resource and changed fields.
    """
    updates_payload = payload.model_dump(exclude_unset=True)
    try:
        result = await context_category_service.update(
            context_category_id=context_category_id,
            name=updates_payload.get("name", UNSET),
            description=updates_payload.get("description", UNSET),
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the parameters and try again.", "cause": str(e)},
        )

    if result is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "message": "The context category was not found.",
                "cause": f"No context category exists with ID {context_category_id}.",
            },
        )

    updated_id, updates = result
    return UpdateContextCategoryResponse(ctx_category_id=updated_id, updates=updates)


@router.delete(
    "/{context_category_id}",
    summary="Delete a context category by ID",
    description="Deletes a single context category by context_category_id.",
    status_code=status.HTTP_204_NO_CONTENT,
)
async def delete_ctx_category(
    context_category_id: ContextCategoryId = Path(..., description="ID of the context category to delete."),
    context_category_service: ContextCategoryService = Depends(get_ctx_category_service),
) -> Response:
    """Delete a context category by ID.

    Args:
        context_category_id: Context category identifier.
        context_category_service: Context category service dependency.

    Returns:
        Empty HTTP 204 response when deletion succeeds.
    """
    deleted = await context_category_service.delete(context_category_id)
    if not deleted:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "message": "The context category was not found.",
                "cause": f"No context category exists with ID {context_category_id}.",
            },
        )
    return Response(status_code=status.HTTP_204_NO_CONTENT)
