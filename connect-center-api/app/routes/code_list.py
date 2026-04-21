"""Code List API routes."""


from __future__ import annotations

from fastapi import APIRouter, Body, Depends, HTTPException, Path, Query, Response, status

from app.deps import get_code_list_service
from app.routes.models.code_list import (
    CodeListEntry,
    CreateCodeListRequest,
    CreateCodeListResponse,
    CreateCodeListValueRequest,
    CreateCodeListValueResponse,
    GetCodeListByCodeListManifestIdResponse,
    GetCodeListListResponse,
    GetCodeListValueByCodeListValueManifestIdResponse,
    TransferCodeListOwnershipRequest,
    TransferCodeListOwnershipResponse,
    UpdateCodeListRequest,
    UpdateCodeListValueRequest,
    UpdateCodeListValueResponse,
    UpdateCodeListResponse,
    UpdateCodeListStateRequest,
)
from app.utils.date import parse_date_range
from app.services.code_list_service import CodeListService
from app.types.unset import UNSET
from app.types.identifiers import CodeListManifestId, CodeListValueManifestId
from app.types.identifiers import ReleaseId

router = APIRouter(prefix="/code-lists", tags=["code list"])
value_router = APIRouter(prefix="/code-list-values", tags=["code list value"])


async def _get_code_list_value_or_404(
    *,
    code_list_service: CodeListService,
    code_list_value_manifest_id: CodeListValueManifestId,
    code_list_manifest_id: CodeListManifestId | None = None,
) -> GetCodeListValueByCodeListValueManifestIdResponse:
    """Retrieve one code list value via owner-scoped or value-scoped routing."""
    if code_list_manifest_id is None:
        row = await code_list_service.get_code_list_value_by_id(
            code_list_value_manifest_id=code_list_value_manifest_id,
        )
    else:
        row = await code_list_service.get_code_list_value(
            code_list_manifest_id=code_list_manifest_id,
            code_list_value_manifest_id=code_list_value_manifest_id,
        )

    if row is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "message": "The code list or code list value was not found.",
                "cause": f"No code list value exists with manifest ID {int(code_list_value_manifest_id)}.",
            },
        )
    return GetCodeListValueByCodeListValueManifestIdResponse.model_validate(row, from_attributes=True)


async def _update_code_list_value_or_404(
    *,
    payload: UpdateCodeListValueRequest,
    code_list_service: CodeListService,
    code_list_value_manifest_id: CodeListValueManifestId,
    code_list_manifest_id: CodeListManifestId | None = None,
) -> UpdateCodeListValueResponse:
    """Update one code list value via owner-scoped or value-scoped routing."""
    try:
        updates_payload = payload.model_dump(exclude_unset=True)
        if code_list_manifest_id is None:
            result = await code_list_service.update_code_list_value_by_id(
                code_list_value_manifest_id=code_list_value_manifest_id,
                value=updates_payload.get("value", UNSET),
                meaning=updates_payload.get("meaning", UNSET),
                definition=updates_payload.get("definition", UNSET),
                definition_source=updates_payload.get("definition_source", UNSET),
                deprecated=updates_payload.get("deprecated", UNSET),
            )
        else:
            result = await code_list_service.update_code_list_value(
                code_list_manifest_id=code_list_manifest_id,
                code_list_value_manifest_id=code_list_value_manifest_id,
                value=updates_payload.get("value", UNSET),
                meaning=updates_payload.get("meaning", UNSET),
                definition=updates_payload.get("definition", UNSET),
                definition_source=updates_payload.get("definition_source", UNSET),
                deprecated=updates_payload.get("deprecated", UNSET),
            )
        return UpdateCodeListValueResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The code list or code list value was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to update this code list.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't update the code list value.", "cause": str(e)},
        )


async def _delete_code_list_value_or_404(
    *,
    code_list_service: CodeListService,
    code_list_value_manifest_id: CodeListValueManifestId,
    code_list_manifest_id: CodeListManifestId | None = None,
) -> Response:
    """Delete one code list value via owner-scoped or value-scoped routing."""
    try:
        if code_list_manifest_id is None:
            await code_list_service.delete_code_list_value_by_id(
                code_list_value_manifest_id=code_list_value_manifest_id,
            )
        else:
            await code_list_service.delete_code_list_value(
                code_list_manifest_id=code_list_manifest_id,
                code_list_value_manifest_id=code_list_value_manifest_id,
            )
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The code list or code list value was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to update this code list.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the code list and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't delete the code list value.", "cause": str(e)},
        )


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
        code_list_service: Code list service dependency.

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


@router.post(
    "",
    status_code=status.HTTP_201_CREATED,
    summary="Create code list",
    description="Create a code list from an optional base code list in a release allowed for your role.",
    response_model=CreateCodeListResponse,
)
async def create_code_list(
    payload: CreateCodeListRequest = Body(...),
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> CreateCodeListResponse:
    """Create a code list and return its manifest ID."""
    try:
        result = await code_list_service.create_code_list(
            release_id=payload.release_id,
            based_code_list_manifest_id=payload.based_code_list_manifest_id,
        )
        return CreateCodeListResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "A referenced resource was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to create code lists.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't create the code list.", "cause": str(e)},
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
        code_list_service: Code list service dependency.

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


@router.post(
    "/{code_list_manifest_id}",
    summary="Update code list",
    description="Update mutable code list fields while the code list is in `WIP`.",
    response_model=UpdateCodeListResponse,
)
async def update_code_list(
    payload: UpdateCodeListRequest = Body(...),
    code_list_manifest_id: CodeListManifestId = Path(..., ge=1, description="Code list manifest ID."),
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> UpdateCodeListResponse:
    """Update mutable code list fields."""
    try:
        result = await code_list_service.update_code_list(
            code_list_manifest_id=code_list_manifest_id,
            name=payload.name if "name" in payload.model_fields_set else UNSET,
            version_id=payload.version_id if "version_id" in payload.model_fields_set else UNSET,
            list_id=payload.list_id if "list_id" in payload.model_fields_set else UNSET,
            agency_id_list_value_manifest_id=(
                payload.agency_id_list_value_manifest_id
                if "agency_id_list_value_manifest_id" in payload.model_fields_set
                else UNSET
            ),
            definition=payload.definition if "definition" in payload.model_fields_set else UNSET,
            definition_source=payload.definition_source if "definition_source" in payload.model_fields_set else UNSET,
            remark=payload.remark if "remark" in payload.model_fields_set else UNSET,
            namespace_id=payload.namespace_id if "namespace_id" in payload.model_fields_set else UNSET,
            deprecated=payload.deprecated if "deprecated" in payload.model_fields_set else UNSET,
            extensible_indicator=(
                payload.extensible_indicator if "extensible_indicator" in payload.model_fields_set else UNSET
            ),
        )
        return UpdateCodeListResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The code list or a referenced value was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to update this code list.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't update the code list.", "cause": str(e)},
        )


@router.post(
    "/{code_list_manifest_id}/values",
    status_code=status.HTTP_201_CREATED,
    summary="Create code list value",
    description="Create a new code list value while the owner code list is in `WIP`.",
    response_model=CreateCodeListValueResponse,
)
async def create_code_list_value(
    payload: CreateCodeListValueRequest = Body(...),
    code_list_manifest_id: CodeListManifestId = Path(..., ge=1, description="Code list manifest ID."),
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> CreateCodeListValueResponse:
    """Create one code list value under the target code list."""
    try:
        result = await code_list_service.create_code_list_value(
            code_list_manifest_id=code_list_manifest_id,
            value=payload.value,
            meaning=payload.meaning,
            definition=payload.definition,
            definition_source=payload.definition_source,
            deprecated=payload.deprecated,
        )
        return CreateCodeListValueResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The code list was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to update this code list.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't create the code list value.", "cause": str(e)},
        )


@router.post(
    "/{code_list_manifest_id}/values/{code_list_value_manifest_id}",
    summary="Update code list value",
    description="Update selected fields of an existing code list value while the owner code list is in `WIP`.",
    response_model=UpdateCodeListValueResponse,
)
async def update_code_list_value(
    payload: UpdateCodeListValueRequest = Body(...),
    code_list_manifest_id: CodeListManifestId = Path(..., ge=1, description="Code list manifest ID."),
    code_list_value_manifest_id: CodeListValueManifestId = Path(..., ge=1, description="Code list value manifest ID."),
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> UpdateCodeListValueResponse:
    """Update one code list value under the target code list."""
    return await _update_code_list_value_or_404(
        payload=payload,
        code_list_service=code_list_service,
        code_list_manifest_id=code_list_manifest_id,
        code_list_value_manifest_id=code_list_value_manifest_id,
    )


@router.get(
    "/{code_list_manifest_id}/values/{code_list_value_manifest_id}",
    summary="Retrieve a code list value",
    description="Retrieve one code list value under the specified code list manifest ID.",
    response_model=GetCodeListValueByCodeListValueManifestIdResponse,
)
async def get_code_list_value(
    code_list_manifest_id: CodeListManifestId = Path(..., ge=1, description="Code list manifest ID."),
    code_list_value_manifest_id: CodeListValueManifestId = Path(..., ge=1, description="Code list value manifest ID."),
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> GetCodeListValueByCodeListValueManifestIdResponse:
    """Retrieve one code list value under the target code list."""
    return await _get_code_list_value_or_404(
        code_list_service=code_list_service,
        code_list_manifest_id=code_list_manifest_id,
        code_list_value_manifest_id=code_list_value_manifest_id,
    )


@router.delete(
    "/{code_list_manifest_id}/values/{code_list_value_manifest_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Delete code list value",
    description="Delete an existing code list value while the owner code list is in `WIP`.",
)
async def delete_code_list_value(
    code_list_manifest_id: CodeListManifestId = Path(..., ge=1, description="Code list manifest ID."),
    code_list_value_manifest_id: CodeListValueManifestId = Path(..., ge=1, description="Code list value manifest ID."),
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> Response:
    """Delete one code list value under the target code list."""
    return await _delete_code_list_value_or_404(
        code_list_service=code_list_service,
        code_list_manifest_id=code_list_manifest_id,
        code_list_value_manifest_id=code_list_value_manifest_id,
    )


@value_router.post(
    "/{code_list_value_manifest_id}",
    summary="Update code list value by ID",
    description="Update selected fields of an existing code list value addressed directly by value ID.",
    response_model=UpdateCodeListValueResponse,
    openapi_extra={
        "x-alternative-endpoint-for": "/code-lists/{code_list_manifest_id}/values/{code_list_value_manifest_id}"
    },
)
async def update_code_list_value_by_code_list_value_manifest_id(
    payload: UpdateCodeListValueRequest = Body(...),
    code_list_value_manifest_id: CodeListValueManifestId = Path(..., ge=1, description="Code list value manifest ID."),
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> UpdateCodeListValueResponse:
    """Update one code list value by value identifier only."""
    return await _update_code_list_value_or_404(
        payload=payload,
        code_list_service=code_list_service,
        code_list_value_manifest_id=code_list_value_manifest_id,
    )


@value_router.get(
    "/{code_list_value_manifest_id}",
    summary="Retrieve a code list value by ID",
    description="Retrieve one code list value addressed directly by value ID.",
    response_model=GetCodeListValueByCodeListValueManifestIdResponse,
    openapi_extra={
        "x-alternative-endpoint-for": "/code-lists/{code_list_manifest_id}/values/{code_list_value_manifest_id}"
    },
)
async def get_code_list_value_by_code_list_value_manifest_id(
    code_list_value_manifest_id: CodeListValueManifestId = Path(..., ge=1, description="Code list value manifest ID."),
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> GetCodeListValueByCodeListValueManifestIdResponse:
    """Retrieve one code list value by value identifier only."""
    return await _get_code_list_value_or_404(
        code_list_service=code_list_service,
        code_list_value_manifest_id=code_list_value_manifest_id,
    )


@value_router.delete(
    "/{code_list_value_manifest_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Delete code list value by ID",
    description="Delete an existing code list value addressed directly by value ID.",
    openapi_extra={
        "x-alternative-endpoint-for": "/code-lists/{code_list_manifest_id}/values/{code_list_value_manifest_id}"
    },
)
async def delete_code_list_value_by_code_list_value_manifest_id(
    code_list_value_manifest_id: CodeListValueManifestId = Path(..., ge=1, description="Code list value manifest ID."),
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> Response:
    """Delete one code list value by value identifier only."""
    return await _delete_code_list_value_or_404(
        code_list_service=code_list_service,
        code_list_value_manifest_id=code_list_value_manifest_id,
    )


@router.post(
    "/{code_list_manifest_id}/ownership",
    summary="Transfer code list ownership",
    description="Transfer ownership of a code list to another user while the code list is in `WIP`.",
    response_model=TransferCodeListOwnershipResponse,
)
async def transfer_code_list_ownership(
    payload: TransferCodeListOwnershipRequest = Body(...),
    code_list_manifest_id: CodeListManifestId = Path(..., ge=1, description="Code list manifest ID."),
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> TransferCodeListOwnershipResponse:
    """Transfer code list ownership and return the changed field names."""
    try:
        result = await code_list_service.transfer_code_list_ownership(
            code_list_manifest_id=code_list_manifest_id,
            target_user_id=payload.target_user_id,
        )
        return TransferCodeListOwnershipResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The code list or target user was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to transfer this code list.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't transfer code list ownership.", "cause": str(e)},
        )


@router.post(
    "/{code_list_manifest_id}/state",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Change code list state",
    description=(
        "Change the lifecycle state of a code list. "
        "For `Working` releases, the allowed path is `Deleted <-> WIP <-> Draft <-> Candidate`. "
        "For non-`Working` releases, the allowed path is `Deleted <-> WIP <-> QA <-> Production`."
    ),
)
async def change_code_list_state(
    payload: UpdateCodeListStateRequest = Body(...),
    code_list_manifest_id: CodeListManifestId = Path(..., ge=1, description="Code list manifest ID."),
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> Response:
    """Change code list lifecycle state."""
    try:
        await code_list_service.change_code_list_state(
            code_list_manifest_id=code_list_manifest_id,
            state=payload.state,
        )
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The code list was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to update this code list state.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't change the code list state.", "cause": str(e)},
        )


@router.post(
    "/{code_list_manifest_id}/revise",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Revise code list",
    description=(
        "Create a new editable code list revision from a stable code list. "
        "Developer-side code lists can be revised only from `Published`, "
        "and end-user code lists only from `Production`."
    ),
)
async def revise_code_list(
    code_list_manifest_id: CodeListManifestId = Path(..., ge=1, description="Code list manifest ID."),
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> Response:
    """Create a revised code list working copy."""
    try:
        await code_list_service.revise_code_list(code_list_manifest_id=code_list_manifest_id)
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The code list was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to revise this code list.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the code list and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't revise the code list.", "cause": str(e)},
        )


@router.post(
    "/{code_list_manifest_id}/cancel",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Cancel code list",
    description="Cancel the current code list revision and restore the previous stable code list revision.",
)
async def cancel_code_list(
    code_list_manifest_id: CodeListManifestId = Path(..., ge=1, description="Code list manifest ID."),
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> Response:
    """Cancel the active code list revision."""
    try:
        await code_list_service.cancel_code_list(code_list_manifest_id=code_list_manifest_id)
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The code list was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to cancel this code list revision.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the code list and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't cancel the code list revision.", "cause": str(e)},
        )


@router.delete(
    "/{code_list_manifest_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Discard code list",
    description="Discard a code list and its direct records permanently. The code list must already be in the `Deleted` state.",
)
async def discard_code_list(
    code_list_manifest_id: CodeListManifestId = Path(..., ge=1, description="Code list manifest ID."),
    code_list_service: CodeListService = Depends(get_code_list_service),
) -> Response:
    """Discard a Deleted code list."""
    try:
        await code_list_service.discard_code_list(code_list_manifest_id=code_list_manifest_id)
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The code list was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to discard this code list.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the code list and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't discard the code list.", "cause": str(e)},
        )
