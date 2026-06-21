"""Business Context API routes.

Business Contexts link Context Scheme Values together to define a complete
context definition (e.g., "Production, US, Manufacturing").

Key features:
- CRUD operations for business contexts.
- CRUD operations for business context values.
- Pagination, sorting, and filtering for list queries.
- Date range filters on creation and last update timestamps.
- Standardized error responses for invalid parameters and missing records.
"""


from __future__ import annotations

from fastapi import APIRouter, Body, Depends, HTTPException, Path, Query, Response, status

from app.deps import get_biz_ctx_service
from app.routes.models.biz_ctx import (
    BizCtxEntry,
    GetBizCtxByBizCtxIdResponse,
    GetBizCtxListResponse,
    GetBizCtxValueByBizCtxValueIdResponse,
    CreateBizCtxRequest,
    CreateBizCtxResponse,
    CreateBizCtxValueRequest,
    CreateBizCtxValueResponse,
    UpdateBizCtxRequest,
    UpdateBizCtxResponse,
    UpdateBizCtxValueRequest,
    UpdateBizCtxValueResponse,
)
from app.utils.date import parse_date_range
from app.services.biz_ctx_service import BizCtxService
from app.types.identifiers import BizCtxId, BizCtxValueId
from app.types.unset import UNSET

router = APIRouter(prefix="/business-contexts", tags=["business-context"])
value_router = APIRouter(prefix="/business-context-values", tags=["business-context-value"])


def _is_biz_ctx_value_owner_mismatch(error: ValueError) -> bool:
    return "does not belong to business context ID" in str(error)


def _raise_biz_ctx_value_not_found(*, biz_ctx_value_id: BizCtxValueId) -> None:
    raise HTTPException(
        status_code=status.HTTP_404_NOT_FOUND,
        detail={
            "message": "The business context value was not found.",
            "cause": f"No business context value exists with ID {int(biz_ctx_value_id)}.",
        },
    )


def _raise_biz_ctx_value_owner_not_found(
    *,
    biz_ctx_id: BizCtxId,
    biz_ctx_value_id: BizCtxValueId,
) -> None:
    raise HTTPException(
        status_code=status.HTTP_404_NOT_FOUND,
        detail={
            "message": "The business context value was not found.",
            "cause": (
                f"No business context value exists with ID {int(biz_ctx_value_id)} "
                f"under business context ID {int(biz_ctx_id)}."
            ),
        },
    )


async def _get_biz_ctx_value_or_404(
    *,
    biz_ctx_value_id: BizCtxValueId,
    biz_ctx_service: BizCtxService,
    biz_ctx_id: BizCtxId | None = None,
) -> GetBizCtxValueByBizCtxValueIdResponse:
    row = await biz_ctx_service.get_value(biz_ctx_value_id)
    if row is None:
        _raise_biz_ctx_value_not_found(biz_ctx_value_id=biz_ctx_value_id)
    if biz_ctx_id is not None and int(row.owner_biz_ctx_id) != int(biz_ctx_id):
        _raise_biz_ctx_value_owner_not_found(
            biz_ctx_id=biz_ctx_id,
            biz_ctx_value_id=biz_ctx_value_id,
        )
    return GetBizCtxValueByBizCtxValueIdResponse.model_validate(row, from_attributes=True)


async def _update_biz_ctx_value_or_404(
    *,
    biz_ctx_value_id: BizCtxValueId,
    payload: UpdateBizCtxValueRequest,
    biz_ctx_service: BizCtxService,
    biz_ctx_id: BizCtxId | None = None,
) -> UpdateBizCtxValueResponse:
    try:
        if biz_ctx_id is None:
            result = await biz_ctx_service.update_value_by_id(
                biz_ctx_value_id=biz_ctx_value_id,
                ctx_scheme_value_id=payload.ctx_scheme_value_id,
            )
        else:
            result = await biz_ctx_service.update_value(
                biz_ctx_id=biz_ctx_id,
                biz_ctx_value_id=biz_ctx_value_id,
                ctx_scheme_value_id=payload.ctx_scheme_value_id,
            )
    except ValueError as e:
        if biz_ctx_id is not None and _is_biz_ctx_value_owner_mismatch(e):
            _raise_biz_ctx_value_owner_not_found(
                biz_ctx_id=biz_ctx_id,
                biz_ctx_value_id=biz_ctx_value_id,
            )
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )

    if result is None:
        _raise_biz_ctx_value_not_found(biz_ctx_value_id=biz_ctx_value_id)
    updated_id, updates = result
    return UpdateBizCtxValueResponse(biz_ctx_value_id=updated_id, updates=updates)


async def _delete_biz_ctx_value_or_404(
    *,
    biz_ctx_value_id: BizCtxValueId,
    biz_ctx_service: BizCtxService,
    biz_ctx_id: BizCtxId | None = None,
) -> Response:
    try:
        if biz_ctx_id is None:
            deleted = await biz_ctx_service.delete_value_by_id(biz_ctx_value_id)
        else:
            deleted = await biz_ctx_service.delete_value(biz_ctx_id, biz_ctx_value_id)
    except ValueError as e:
        if biz_ctx_id is not None and _is_biz_ctx_value_owner_mismatch(e):
            _raise_biz_ctx_value_owner_not_found(
                biz_ctx_id=biz_ctx_id,
                biz_ctx_value_id=biz_ctx_value_id,
            )
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the parameters and try again.", "cause": str(e)},
        )
    if not deleted:
        _raise_biz_ctx_value_not_found(biz_ctx_value_id=biz_ctx_value_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.get(
    "",
    summary="List business contexts",
    description="Retrieve a paginated list of business contexts (including their values).",
    response_model=GetBizCtxListResponse,
)
async def get_business_context_list(
    name: str | None = Query(default=None, description="Filter by the name of the business context."),
    created_on: str | None = Query(
        default=None,
        description="Filter by creation date using an inclusive range: '[before~after]'.",
    ),
    last_updated_on: str | None = Query(
        default=None,
        description="Filter by last update date using an inclusive range: '[before~after]'.",
    ),
    updater: str | None = Query(
        default=None,
        description="Comma-separated updater login IDs to filter by exact match. Prefix a login ID with '!' to exclude it.",
    ),
    order_by: str | None = Query(
        default=None,
        description=(
            "Comma-separated list of properties to order results by. Prefix with '-' for descending, '+' for ascending. "
            "Allowed columns: name, creation_timestamp, last_update_timestamp."
        ),
    ),
    offset: int = Query(default=0, ge=0, description="The offset from the beginning of the list."),
    limit: int = Query(default=10, ge=1, le=100, description="The maximum number of items to return."),
    biz_ctx_service: BizCtxService = Depends(get_biz_ctx_service),
) -> GetBizCtxListResponse:
    """Return a paginated list of business contexts (including values).

    Args:
        name: Optional name filter.
        created_on: Optional creation-time filter in ISO-8601 range form.
        last_updated_on: Optional last-update-time filter in ISO-8601 range form.
        order_by: Sort expression used for ordering the result set.
        offset: Zero-based index of the first item to include in the page.
        limit: Maximum number of items to return.
        biz_ctx_service: Business context service dependency.

    Returns:
        Paginated business-context list response with nested values.
    """
    try:
        created_range = parse_date_range(created_on)
        updated_range = parse_date_range(last_updated_on)
        page = await biz_ctx_service.list(
            limit=limit,
            offset=offset,
            order_by=order_by,
            name=name,
            created_on=created_range,
            last_updated_on=updated_range,
            updater=updater,
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the parameters and try again.", "cause": str(e)},
        )

    return GetBizCtxListResponse(
        items=[BizCtxEntry.model_validate(item, from_attributes=True) for item in page.items],
        total_items=page.total,
        limit=limit,
        offset=offset,
    )


@router.get(
    "/{biz_ctx_id}",
    summary="Retrieve a business context",
    description="Retrieve a business context by ID (including its values).",
    response_model=GetBizCtxByBizCtxIdResponse,
)
async def get_business_context_by_biz_ctx_id(
    biz_ctx_id: BizCtxId = Path(..., description="ID of the business context to retrieve."),
    biz_ctx_service: BizCtxService = Depends(get_biz_ctx_service),
) -> GetBizCtxByBizCtxIdResponse:
    """Return a business context by ID.

    Args:
        biz_ctx_id: Business context identifier.
        biz_ctx_service: Business context service dependency.

    Returns:
        Business-context response for the requested identifier.
    """
    row = await biz_ctx_service.get(biz_ctx_id)
    if row is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The business context was not found.", "cause": f"No business context exists with ID {int(biz_ctx_id)}."},
        )
    return GetBizCtxByBizCtxIdResponse.model_validate(row, from_attributes=True)


@router.post(
    "",
    status_code=status.HTTP_201_CREATED,
    summary="Create a business context",
    description="Creates a new business context.",
    response_model=CreateBizCtxResponse,
)
async def create_business_context(
    payload: CreateBizCtxRequest = Body(...),
    biz_ctx_service: BizCtxService = Depends(get_biz_ctx_service),
) -> CreateBizCtxResponse:
    """Create a new business context and return the new ID.

    Args:
        payload: Validated request payload.
        biz_ctx_service: Business context service dependency.

    Returns:
        Identifier of the newly created business context.
    """
    try:
        biz_ctx_id = await biz_ctx_service.create(name=payload.name)
        return CreateBizCtxResponse(biz_ctx_id=biz_ctx_id)
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't create the business context.", "cause": str(e)},
        )


@router.post(
    "/{biz_ctx_id}",
    summary="Update a business context by ID",
    description="Updates selected fields of an existing business context.",
    response_model=UpdateBizCtxResponse,
)
async def update_business_context(
    biz_ctx_id: BizCtxId = Path(..., description="ID of the business context to update."),
    payload: UpdateBizCtxRequest = Body(...),
    biz_ctx_service: BizCtxService = Depends(get_biz_ctx_service),
) -> UpdateBizCtxResponse:
    """Update an existing business context and return the updated fields.

    Args:
        biz_ctx_id: Business context identifier.
        payload: Validated request payload.
        biz_ctx_service: Business context service dependency.

    Returns:
        Updated business-context identifier and changed fields.
    """
    updates_payload = payload.model_dump(exclude_unset=True)
    try:
        result = await biz_ctx_service.update(
            biz_ctx_id=biz_ctx_id,
            name=updates_payload.get("name", UNSET),
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )

    if result is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The business context was not found.", "cause": f"No business context exists with ID {int(biz_ctx_id)}."},
        )
    updated_id, updates = result
    return UpdateBizCtxResponse(biz_ctx_id=updated_id, updates=updates)


@router.delete(
    "/{biz_ctx_id}",
    summary="Delete a business context by ID",
    description="Deletes a single business context by biz_ctx_id (including its values).",
    status_code=status.HTTP_204_NO_CONTENT,
)
async def delete_business_context(
    biz_ctx_id: BizCtxId = Path(..., description="ID of the business context to delete."),
    biz_ctx_service: BizCtxService = Depends(get_biz_ctx_service),
) -> Response:
    """Delete a business context by ID (including its values).

    Args:
        biz_ctx_id: Business context identifier.
        biz_ctx_service: Business context service dependency.

    Returns:
        Empty HTTP 204 response when deletion succeeds.
    """
    deleted = await biz_ctx_service.delete(biz_ctx_id)
    if not deleted:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The business context was not found.", "cause": f"No business context exists with ID {int(biz_ctx_id)}."},
        )
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.post(
    "/{biz_ctx_id}/values",
    status_code=status.HTTP_201_CREATED,
    summary="Create a business context value",
    description="Creates a new value linking a business context to a context scheme value.",
    response_model=CreateBizCtxValueResponse,
)
async def create_business_context_value(
    biz_ctx_id: BizCtxId = Path(..., description="ID of the business context to add a value to."),
    payload: CreateBizCtxValueRequest = Body(...),
    biz_ctx_service: BizCtxService = Depends(get_biz_ctx_service),
) -> CreateBizCtxValueResponse:
    """Create a new business context value linking to a scheme value.

    Args:
        biz_ctx_id: Business context identifier.
        payload: Validated request payload.
        biz_ctx_service: Business context service dependency.

    Returns:
        Identifier of the newly created business-context value link.
    """
    try:
        value_id = await biz_ctx_service.create_value(biz_ctx_id=biz_ctx_id, ctx_scheme_value_id=payload.ctx_scheme_value_id)
        return CreateBizCtxValueResponse(biz_ctx_value_id=value_id)
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )


@router.post(
    "/{biz_ctx_id}/values/{biz_ctx_value_id}",
    summary="Update a business context value by ID",
    description="Updates the linked context scheme value for a business context value under a specific business context.",
    response_model=UpdateBizCtxValueResponse,
)
async def update_business_context_value(
    biz_ctx_id: BizCtxId = Path(..., description="ID of the business context that owns the value."),
    biz_ctx_value_id: BizCtxValueId = Path(..., description="ID of the business context value to update."),
    payload: UpdateBizCtxValueRequest = Body(...),
    biz_ctx_service: BizCtxService = Depends(get_biz_ctx_service),
) -> UpdateBizCtxValueResponse:
    """Update the linked scheme value for a business context value.

    Args:
        biz_ctx_id: Business context identifier.
        biz_ctx_value_id: Business context value identifier.
        payload: Validated request payload.
        biz_ctx_service: Business context service dependency.

    Returns:
        Updated value-link identifier and changed fields.
    """
    return await _update_biz_ctx_value_or_404(
        biz_ctx_id=biz_ctx_id,
        biz_ctx_value_id=biz_ctx_value_id,
        payload=payload,
        biz_ctx_service=biz_ctx_service,
    )


@router.get(
    "/{biz_ctx_id}/values/{biz_ctx_value_id}",
    summary="Retrieve a business context value",
    description="Retrieve a business context value by owner business context ID and value ID.",
    response_model=GetBizCtxValueByBizCtxValueIdResponse,
)
async def get_business_context_value(
    biz_ctx_id: BizCtxId = Path(..., description="ID of the business context that owns the value."),
    biz_ctx_value_id: BizCtxValueId = Path(..., description="ID of the business context value to retrieve."),
    biz_ctx_service: BizCtxService = Depends(get_biz_ctx_service),
) -> GetBizCtxValueByBizCtxValueIdResponse:
    """Return a business context value by owner business context ID and value ID."""
    return await _get_biz_ctx_value_or_404(
        biz_ctx_id=biz_ctx_id,
        biz_ctx_value_id=biz_ctx_value_id,
        biz_ctx_service=biz_ctx_service,
    )


@router.delete(
    "/{biz_ctx_id}/values/{biz_ctx_value_id}",
    summary="Delete a business context value by ID",
    description="Deletes a single business context value by biz_ctx_id and biz_ctx_value_id.",
    status_code=status.HTTP_204_NO_CONTENT,
)
async def delete_business_context_value(
    biz_ctx_id: BizCtxId = Path(..., description="ID of the business context that owns the value."),
    biz_ctx_value_id: BizCtxValueId = Path(..., description="ID of the business context value to delete."),
    biz_ctx_service: BizCtxService = Depends(get_biz_ctx_service),
) -> Response:
    """Delete a business context value by ID.

    Args:
        biz_ctx_id: Business context identifier.
        biz_ctx_value_id: Business context value identifier.
        biz_ctx_service: Business context service dependency.

    Returns:
        Empty HTTP 204 response when deletion succeeds.
    """
    return await _delete_biz_ctx_value_or_404(
        biz_ctx_id=biz_ctx_id,
        biz_ctx_value_id=biz_ctx_value_id,
        biz_ctx_service=biz_ctx_service,
    )


@value_router.get(
    "/{biz_ctx_value_id}",
    summary="Retrieve a business context value",
    description="Retrieve a business context value by ID.",
    response_model=GetBizCtxValueByBizCtxValueIdResponse,
    openapi_extra={"x-alternative-endpoint-for": "/business-contexts/{biz_ctx_id}/values/{biz_ctx_value_id}"},
)
async def get_business_context_value_by_biz_ctx_value_id(
    biz_ctx_value_id: BizCtxValueId = Path(..., description="ID of the business context value to retrieve."),
    biz_ctx_service: BizCtxService = Depends(get_biz_ctx_service),
) -> GetBizCtxValueByBizCtxValueIdResponse:
    """Return a business context value by ID."""
    return await _get_biz_ctx_value_or_404(
        biz_ctx_value_id=biz_ctx_value_id,
        biz_ctx_service=biz_ctx_service,
    )


@value_router.post(
    "/{biz_ctx_value_id}",
    summary="Update a business context value by ID",
    description="Updates the linked context scheme value for a business context value addressed directly by value ID.",
    response_model=UpdateBizCtxValueResponse,
    openapi_extra={"x-alternative-endpoint-for": "/business-contexts/{biz_ctx_id}/values/{biz_ctx_value_id}"},
)
async def update_business_context_value_by_biz_ctx_value_id(
    biz_ctx_value_id: BizCtxValueId = Path(..., description="ID of the business context value to update."),
    payload: UpdateBizCtxValueRequest = Body(...),
    biz_ctx_service: BizCtxService = Depends(get_biz_ctx_service),
) -> UpdateBizCtxValueResponse:
    """Update the linked scheme value for a business context value by value ID only."""
    return await _update_biz_ctx_value_or_404(
        biz_ctx_value_id=biz_ctx_value_id,
        payload=payload,
        biz_ctx_service=biz_ctx_service,
    )


@value_router.delete(
    "/{biz_ctx_value_id}",
    summary="Delete a business context value by ID",
    description="Deletes a single business context value addressed directly by value ID.",
    status_code=status.HTTP_204_NO_CONTENT,
    openapi_extra={"x-alternative-endpoint-for": "/business-contexts/{biz_ctx_id}/values/{biz_ctx_value_id}"},
)
async def delete_business_context_value_by_biz_ctx_value_id(
    biz_ctx_value_id: BizCtxValueId = Path(..., description="ID of the business context value to delete."),
    biz_ctx_service: BizCtxService = Depends(get_biz_ctx_service),
) -> Response:
    """Delete a business context value by value ID only."""
    return await _delete_biz_ctx_value_or_404(
        biz_ctx_value_id=biz_ctx_value_id,
        biz_ctx_service=biz_ctx_service,
    )
