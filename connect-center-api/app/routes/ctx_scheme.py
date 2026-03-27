"""Context Scheme API routes.

Context Schemes define controlled vocabularies (dimensions) for business contexts.
Each scheme can contain multiple scheme values.

Key features:
- CRUD operations for context schemes.
- CRUD operations for context scheme values.
- Pagination, sorting, and filtering for list queries.
- Date range filters on creation and last update timestamps.
- Standardized error responses for invalid parameters and missing records.
"""


from __future__ import annotations

from fastapi import APIRouter, Body, Depends, HTTPException, Path, Query, Response, status

from app.deps import get_ctx_scheme_service
from app.routes.models.ctx_scheme import (
    CreateCtxSchemeRequest,
    CreateCtxSchemeResponse,
    CreateCtxSchemeValueRequest,
    CreateCtxSchemeValueResponse,
    CtxSchemeEntry,
    GetCtxSchemeByCtxSchemeIdResponse,
    GetCtxSchemeListResponse,
    GetCtxSchemeValueByCtxSchemeValueIdResponse,
    UpdateCtxSchemeRequest,
    UpdateCtxSchemeResponse,
    UpdateCtxSchemeValueRequest,
    UpdateCtxSchemeValueResponse,
)
from app.utils.date import parse_date_range
from app.services.ctx_scheme_service import CtxSchemeService
from app.types.identifiers import CtxSchemeId, CtxSchemeValueId
from app.types.unset import UNSET

router = APIRouter(prefix="/context-schemes", tags=["context-scheme"])
value_router = APIRouter(prefix="/context-scheme-values", tags=["context-scheme-value"])


def _is_ctx_scheme_value_owner_mismatch(error: ValueError) -> bool:
    return "does not belong to the specified context scheme" in str(error)


def _raise_ctx_scheme_value_not_found(*, ctx_scheme_value_id: CtxSchemeValueId) -> None:
    raise HTTPException(
        status_code=status.HTTP_404_NOT_FOUND,
        detail={
            "message": "The context scheme value was not found.",
            "cause": f"No context scheme value exists with ID {int(ctx_scheme_value_id)}.",
        },
    )


def _raise_ctx_scheme_value_owner_not_found(
    *,
    ctx_scheme_id: CtxSchemeId,
    ctx_scheme_value_id: CtxSchemeValueId,
) -> None:
    raise HTTPException(
        status_code=status.HTTP_404_NOT_FOUND,
        detail={
            "message": "The context scheme value was not found.",
            "cause": (
                f"No context scheme value exists with ID {int(ctx_scheme_value_id)} "
                f"under context scheme ID {int(ctx_scheme_id)}."
            ),
        },
    )


async def _get_ctx_scheme_value_or_404(
    *,
    ctx_scheme_value_id: CtxSchemeValueId,
    ctx_scheme_service: CtxSchemeService,
    ctx_scheme_id: CtxSchemeId | None = None,
) -> GetCtxSchemeValueByCtxSchemeValueIdResponse:
    row = await ctx_scheme_service.get_value(ctx_scheme_value_id)
    if row is None:
        _raise_ctx_scheme_value_not_found(ctx_scheme_value_id=ctx_scheme_value_id)
    if ctx_scheme_id is not None and int(row.owner_ctx_scheme_id) != int(ctx_scheme_id):
        _raise_ctx_scheme_value_owner_not_found(
            ctx_scheme_id=ctx_scheme_id,
            ctx_scheme_value_id=ctx_scheme_value_id,
        )
    return GetCtxSchemeValueByCtxSchemeValueIdResponse.model_validate(row, from_attributes=True)


async def _update_ctx_scheme_value_or_404(
    *,
    ctx_scheme_value_id: CtxSchemeValueId,
    payload: UpdateCtxSchemeValueRequest,
    ctx_scheme_service: CtxSchemeService,
    ctx_scheme_id: CtxSchemeId | None = None,
) -> UpdateCtxSchemeValueResponse:
    updates_payload = payload.model_dump(exclude_unset=True)
    try:
        if ctx_scheme_id is None:
            result = await ctx_scheme_service.update_value_by_id(
                ctx_scheme_value_id=ctx_scheme_value_id,
                value=updates_payload.get("value", UNSET),
                meaning=updates_payload.get("meaning", UNSET),
            )
        else:
            result = await ctx_scheme_service.update_value(
                ctx_scheme_id=ctx_scheme_id,
                ctx_scheme_value_id=ctx_scheme_value_id,
                value=updates_payload.get("value", UNSET),
                meaning=updates_payload.get("meaning", UNSET),
            )
    except ValueError as e:
        if ctx_scheme_id is not None and _is_ctx_scheme_value_owner_mismatch(e):
            _raise_ctx_scheme_value_owner_not_found(
                ctx_scheme_id=ctx_scheme_id,
                ctx_scheme_value_id=ctx_scheme_value_id,
            )
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )

    if result is None:
        _raise_ctx_scheme_value_not_found(ctx_scheme_value_id=ctx_scheme_value_id)
    updated_id, updates = result
    return UpdateCtxSchemeValueResponse(ctx_scheme_value_id=updated_id, updates=updates)


async def _delete_ctx_scheme_value_or_404(
    *,
    ctx_scheme_value_id: CtxSchemeValueId,
    ctx_scheme_service: CtxSchemeService,
    ctx_scheme_id: CtxSchemeId | None = None,
) -> Response:
    try:
        if ctx_scheme_id is None:
            deleted = await ctx_scheme_service.delete_value_by_id(ctx_scheme_value_id)
        else:
            deleted = await ctx_scheme_service.delete_value(ctx_scheme_id, ctx_scheme_value_id)
    except ValueError as e:
        if ctx_scheme_id is not None and _is_ctx_scheme_value_owner_mismatch(e):
            _raise_ctx_scheme_value_owner_not_found(
                ctx_scheme_id=ctx_scheme_id,
                ctx_scheme_value_id=ctx_scheme_value_id,
            )
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={
                "message": "The context scheme value cannot be deleted.",
                "cause": str(e),
            },
        )
    if not deleted:
        _raise_ctx_scheme_value_not_found(ctx_scheme_value_id=ctx_scheme_value_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.get(
    "",
    summary="List context schemes",
    description="Retrieve a paginated list of context schemes (including their values).",
    response_model=GetCtxSchemeListResponse,
)
async def get_ctx_scheme_list(
    scheme_id: str | None = Query(default=None, description="Filter by the unique scheme identifier."),
    scheme_name: str | None = Query(default=None, description="Filter by the human-readable name of the scheme."),
    scheme_agency_id: str | None = Query(default=None, description="Filter by the agency identifier responsible for the scheme."),
    scheme_version_id: str | None = Query(default=None, description="Filter by the version identifier of the scheme."),
    ctx_category_id: int | None = Query(default=None, ge=1, description="Filter by the associated context category ID."),
    ctx_category_name: str | None = Query(default=None, description="Filter by the name of the associated context category."),
    description: str | None = Query(default=None, description="Filter by text contained in the scheme description."),
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
            "Allowed columns: scheme_id, scheme_name, description, scheme_agency_id, scheme_version_id, creation_timestamp, last_update_timestamp."
        ),
    ),
    offset: int = Query(default=0, ge=0, description="The offset from the beginning of the list."),
    limit: int = Query(default=10, ge=1, le=100, description="The maximum number of items to return."),
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
) -> GetCtxSchemeListResponse:
    """Return a paginated list of context schemes (including values).

    Args:
        scheme_id: Optional context scheme identifier filter.
        scheme_name: Optional context scheme name filter.
        scheme_agency_id: Optional context scheme agency identifier filter.
        scheme_version_id: Optional context scheme version identifier filter.
        ctx_category_id: Optional context category identifier filter.
        ctx_category_name: Optional context category name filter.
        description: Optional textual description filter or payload field.
        created_on: Optional creation-time filter in ISO-8601 range form.
        last_updated_on: Optional last-update-time filter in ISO-8601 range form.
        order_by: Sort expression used for ordering the result set.
        offset: Zero-based index of the first item to include in the page.
        limit: Maximum number of items to return.
        ctx_scheme_service: Context scheme service dependency.

    Returns:
        Paginated response containing matching resources.
    """
    try:
        created_range = parse_date_range(created_on)
        updated_range = parse_date_range(last_updated_on)
        page = await ctx_scheme_service.list(
            limit=limit,
            offset=offset,
            order_by=order_by,
            scheme_id=scheme_id,
            scheme_name=scheme_name,
            description=description,
            scheme_agency_id=scheme_agency_id,
            scheme_version_id=scheme_version_id,
            ctx_category_id=ctx_category_id,
            ctx_category_name=ctx_category_name,
            created_on=created_range,
            last_updated_on=updated_range,
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the parameters and try again.", "cause": str(e)},
        )

    return GetCtxSchemeListResponse(
        items=[CtxSchemeEntry.model_validate(item, from_attributes=True) for item in page.items],
        total_items=page.total,
        limit=limit,
        offset=offset,
    )


@router.get(
    "/{ctx_scheme_id}",
    summary="Retrieve a context scheme",
    description="Retrieve a context scheme by ID (including its values).",
    response_model=GetCtxSchemeByCtxSchemeIdResponse,
)
async def get_ctx_scheme_by_ctx_scheme_id(
    ctx_scheme_id: CtxSchemeId = Path(..., description="ID of the context scheme to retrieve."),
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
) -> GetCtxSchemeByCtxSchemeIdResponse:
    """Return a context scheme by ID.

    Args:
        ctx_scheme_id: Context scheme identifier.
        ctx_scheme_service: Context scheme service dependency.

    Returns:
        Response payload for the requested resource.
    """
    row = await ctx_scheme_service.get(ctx_scheme_id)
    if row is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "message": "The context scheme was not found.",
                "cause": f"No context scheme exists with ID {int(ctx_scheme_id)}.",
            },
        )
    return GetCtxSchemeByCtxSchemeIdResponse.model_validate(row, from_attributes=True)


@router.post(
    "",
    status_code=status.HTTP_201_CREATED,
    summary="Create a context scheme",
    description="Creates a new context scheme.",
    response_model=CreateCtxSchemeResponse,
)
async def create_ctx_scheme(
    payload: CreateCtxSchemeRequest = Body(...),
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
) -> CreateCtxSchemeResponse:
    """Create a new context scheme and return the new ID.

    Args:
        payload: Validated request payload.
        ctx_scheme_service: Context scheme service dependency.

    Returns:
        Response payload describing the newly created resource.
    """
    try:
        ctx_scheme_id = await ctx_scheme_service.create(
            scheme_id=payload.scheme_id,
            scheme_name=payload.scheme_name,
            description=payload.description,
            scheme_agency_id=payload.scheme_agency_id,
            scheme_version_id=payload.scheme_version_id,
            ctx_category_id=payload.ctx_category_id,
        )
        return CreateCtxSchemeResponse(ctx_scheme_id=ctx_scheme_id)
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't create the context scheme.", "cause": str(e)},
        )


@router.post(
    "/{ctx_scheme_id}",
    summary="Update a context scheme by ID",
    description="Updates selected fields of an existing context scheme.",
    response_model=UpdateCtxSchemeResponse,
)
async def update_ctx_scheme(
    ctx_scheme_id: CtxSchemeId = Path(..., description="ID of the context scheme to update."),
    payload: UpdateCtxSchemeRequest = Body(...),
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
) -> UpdateCtxSchemeResponse:
    """Update an existing context scheme and return the updated fields.

    Args:
        ctx_scheme_id: Context scheme identifier.
        payload: Validated request payload.
        ctx_scheme_service: Context scheme service dependency.

    Returns:
        Response payload describing the updated resource and changed fields.
    """
    updates_payload = payload.model_dump(exclude_unset=True)
    try:
        result = await ctx_scheme_service.update(
            ctx_scheme_id=ctx_scheme_id,
            scheme_id=updates_payload.get("scheme_id", UNSET),
            scheme_name=updates_payload.get("scheme_name", UNSET),
            description=updates_payload.get("description", UNSET),
            scheme_agency_id=updates_payload.get("scheme_agency_id", UNSET),
            scheme_version_id=updates_payload.get("scheme_version_id", UNSET),
            ctx_category_id=updates_payload.get("ctx_category_id", UNSET),
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )

    if result is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "message": "The context scheme was not found.",
                "cause": f"No context scheme exists with ID {int(ctx_scheme_id)}.",
            },
        )
    updated_id, updates = result
    return UpdateCtxSchemeResponse(ctx_scheme_id=updated_id, updates=updates)


@router.delete(
    "/{ctx_scheme_id}",
    summary="Delete a context scheme by ID",
    description="Deletes a single context scheme by ctx_scheme_id (including its values).",
    status_code=status.HTTP_204_NO_CONTENT,
)
async def delete_ctx_scheme(
    ctx_scheme_id: CtxSchemeId = Path(..., description="ID of the context scheme to delete."),
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
) -> Response:
    """Delete a context scheme by ID (including its values).

    Args:
        ctx_scheme_id: Context scheme identifier.
        ctx_scheme_service: Context scheme service dependency.

    Returns:
        Empty HTTP 204 response when deletion succeeds.
    """
    try:
        deleted = await ctx_scheme_service.delete(ctx_scheme_id)
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The context scheme cannot be deleted.", "cause": str(e)},
        )
    if not deleted:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The context scheme was not found.", "cause": f"No context scheme exists with ID {int(ctx_scheme_id)}."},
        )
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.post(
    "/{ctx_scheme_id}/values",
    status_code=status.HTTP_201_CREATED,
    summary="Create a context scheme value",
    description="Creates a new value for an existing context scheme.",
    response_model=CreateCtxSchemeValueResponse,
)
async def create_ctx_scheme_value(
    ctx_scheme_id: CtxSchemeId = Path(..., description="ID of the owner context scheme."),
    payload: CreateCtxSchemeValueRequest = Body(...),
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
) -> CreateCtxSchemeValueResponse:
    """Create a new context scheme value for an existing scheme.

    Args:
        ctx_scheme_id: Context scheme identifier.
        payload: Validated request payload.
        ctx_scheme_service: Context scheme service dependency.

    Returns:
        Response payload describing the newly created resource.
    """
    try:
        value_id = await ctx_scheme_service.create_value(
            owner_ctx_scheme_id=ctx_scheme_id,
            value=payload.value,
            meaning=payload.meaning,
        )
        return CreateCtxSchemeValueResponse(ctx_scheme_value_id=value_id)
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )


@router.get(
    "/{ctx_scheme_id}/values/{ctx_scheme_value_id}",
    summary="Retrieve a context scheme value",
    description="Retrieve a context scheme value by owner context scheme ID and value ID.",
    response_model=GetCtxSchemeValueByCtxSchemeValueIdResponse,
)
async def get_ctx_scheme_value(
    ctx_scheme_id: CtxSchemeId = Path(..., description="ID of the owner context scheme."),
    ctx_scheme_value_id: CtxSchemeValueId = Path(..., description="ID of the context scheme value to retrieve."),
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
) -> GetCtxSchemeValueByCtxSchemeValueIdResponse:
    """Return a context scheme value by owner context scheme ID and value ID."""
    return await _get_ctx_scheme_value_or_404(
        ctx_scheme_id=ctx_scheme_id,
        ctx_scheme_value_id=ctx_scheme_value_id,
        ctx_scheme_service=ctx_scheme_service,
    )


@router.post(
    "/{ctx_scheme_id}/values/{ctx_scheme_value_id}",
    summary="Update a context scheme value by ID",
    description="Updates selected fields of an existing context scheme value.",
    response_model=UpdateCtxSchemeValueResponse,
)
async def update_ctx_scheme_value(
    ctx_scheme_id: CtxSchemeId = Path(..., description="ID of the owner context scheme."),
    ctx_scheme_value_id: CtxSchemeValueId = Path(..., description="ID of the context scheme value to update."),
    payload: UpdateCtxSchemeValueRequest = Body(...),
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
) -> UpdateCtxSchemeValueResponse:
    """Update an existing context scheme value by ID.

    Args:
        ctx_scheme_id: Context scheme identifier.
        ctx_scheme_value_id: Context scheme value identifier.
        payload: Validated request payload.
        ctx_scheme_service: Context scheme service dependency.

    Returns:
        Response payload describing the updated resource and changed fields.
    """
    return await _update_ctx_scheme_value_or_404(
        ctx_scheme_id=ctx_scheme_id,
        ctx_scheme_value_id=ctx_scheme_value_id,
        payload=payload,
        ctx_scheme_service=ctx_scheme_service,
    )


@router.delete(
    "/{ctx_scheme_id}/values/{ctx_scheme_value_id}",
    summary="Delete a context scheme value by ID",
    description="Deletes a single context scheme value by ctx_scheme_id and ctx_scheme_value_id.",
    status_code=status.HTTP_204_NO_CONTENT,
)
async def delete_ctx_scheme_value(
    ctx_scheme_id: CtxSchemeId = Path(..., description="ID of the owner context scheme."),
    ctx_scheme_value_id: CtxSchemeValueId = Path(..., description="ID of the context scheme value to delete."),
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
) -> Response:
    """Delete a context scheme value by ID.

    Args:
        ctx_scheme_id: Context scheme identifier.
        ctx_scheme_value_id: Context scheme value identifier.
        ctx_scheme_service: Context scheme service dependency.

    Returns:
        Empty HTTP 204 response when deletion succeeds.
    """
    return await _delete_ctx_scheme_value_or_404(
        ctx_scheme_id=ctx_scheme_id,
        ctx_scheme_value_id=ctx_scheme_value_id,
        ctx_scheme_service=ctx_scheme_service,
    )


@value_router.get(
    "/{ctx_scheme_value_id}",
    summary="Retrieve a context scheme value",
    description="Retrieve a context scheme value by ID.",
    response_model=GetCtxSchemeValueByCtxSchemeValueIdResponse,
    openapi_extra={"x-alternative-endpoint-for": "/context-schemes/{ctx_scheme_id}/values/{ctx_scheme_value_id}"},
)
async def get_ctx_scheme_value_by_ctx_scheme_value_id(
    ctx_scheme_value_id: CtxSchemeValueId = Path(..., description="ID of the context scheme value to retrieve."),
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
) -> GetCtxSchemeValueByCtxSchemeValueIdResponse:
    """Return a context scheme value by ID."""
    return await _get_ctx_scheme_value_or_404(
        ctx_scheme_value_id=ctx_scheme_value_id,
        ctx_scheme_service=ctx_scheme_service,
    )


@value_router.post(
    "/{ctx_scheme_value_id}",
    summary="Update a context scheme value by ID",
    description="Updates selected fields of an existing context scheme value addressed directly by value ID.",
    response_model=UpdateCtxSchemeValueResponse,
    openapi_extra={"x-alternative-endpoint-for": "/context-schemes/{ctx_scheme_id}/values/{ctx_scheme_value_id}"},
)
async def update_ctx_scheme_value_by_ctx_scheme_value_id(
    ctx_scheme_value_id: CtxSchemeValueId = Path(..., description="ID of the context scheme value to update."),
    payload: UpdateCtxSchemeValueRequest = Body(...),
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
) -> UpdateCtxSchemeValueResponse:
    """Update an existing context scheme value by value ID only."""
    return await _update_ctx_scheme_value_or_404(
        ctx_scheme_value_id=ctx_scheme_value_id,
        payload=payload,
        ctx_scheme_service=ctx_scheme_service,
    )


@value_router.delete(
    "/{ctx_scheme_value_id}",
    summary="Delete a context scheme value by ID",
    description="Deletes a single context scheme value addressed directly by value ID.",
    status_code=status.HTTP_204_NO_CONTENT,
    openapi_extra={"x-alternative-endpoint-for": "/context-schemes/{ctx_scheme_id}/values/{ctx_scheme_value_id}"},
)
async def delete_ctx_scheme_value_by_ctx_scheme_value_id(
    ctx_scheme_value_id: CtxSchemeValueId = Path(..., description="ID of the context scheme value to delete."),
    ctx_scheme_service: CtxSchemeService = Depends(get_ctx_scheme_service),
) -> Response:
    """Delete a context scheme value by value ID only."""
    return await _delete_ctx_scheme_value_or_404(
        ctx_scheme_value_id=ctx_scheme_value_id,
        ctx_scheme_service=ctx_scheme_service,
    )
