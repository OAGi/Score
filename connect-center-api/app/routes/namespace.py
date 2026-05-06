"""Namespace API routes."""


from __future__ import annotations

from fastapi import APIRouter, Body, Depends, HTTPException, Path, Query, Response, status

from app.deps import get_namespace_service
from app.routes.models.namespace import (
    CreateNamespaceRequest,
    CreateNamespaceResponse,
    GetNamespaceByNamespaceIdResponse,
    GetNamespaceListResponse,
    NamespaceEntry,
    TransferNamespaceOwnershipRequest,
    TransferNamespaceOwnershipResponse,
    UpdateNamespaceRequest,
    UpdateNamespaceResponse,
)
from app.utils.date import parse_date_range
from app.services.namespace_service import NamespaceService
from app.types.identifiers import LibraryId
from app.types.identifiers import NamespaceId
from app.types.unset import UNSET

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
    owner: str | None = Query(
        default=None,
        description="Comma-separated owner login IDs to filter by exact match. Prefix a login ID with '!' to exclude it.",
    ),
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
            owner=owner,
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


@router.post(
    "",
    status_code=status.HTTP_201_CREATED,
    summary="Create namespace",
    description="Create a new namespace in a library.",
    response_model=CreateNamespaceResponse,
)
async def create_namespace(
    payload: CreateNamespaceRequest = Body(...),
    namespace_service: NamespaceService = Depends(get_namespace_service),
) -> CreateNamespaceResponse:
    """Create a namespace and return its identifier."""
    try:
        result = await namespace_service.create_namespace(
            library_id=payload.library_id,
            uri=payload.uri,
            prefix=payload.prefix,
            description=payload.description,
        )
        return CreateNamespaceResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "A referenced resource was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to create namespaces.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't create the namespace.", "cause": str(e)},
        )


@router.post(
    "/{namespace_id}",
    summary="Update namespace",
    description="Update an existing namespace owned by the requester.",
    response_model=UpdateNamespaceResponse,
)
async def update_namespace(
    namespace_id: NamespaceId = Path(..., ge=1, description="Namespace identifier."),
    payload: UpdateNamespaceRequest = Body(...),
    namespace_service: NamespaceService = Depends(get_namespace_service),
) -> UpdateNamespaceResponse:
    """Update a namespace and return the changed fields."""
    updates_payload = payload.model_dump(exclude_unset=True)
    try:
        result = await namespace_service.update_namespace(
            namespace_id=namespace_id,
            uri=updates_payload.get("uri", UNSET),
            prefix=updates_payload.get("prefix", UNSET),
            description=updates_payload.get("description", UNSET),
        )
        return UpdateNamespaceResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The namespace was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to update this namespace.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't update the namespace.", "cause": str(e)},
        )


@router.post(
    "/{namespace_id}/ownership",
    summary="Transfer namespace ownership",
    description="Transfer ownership of a namespace to another user by login ID.",
    response_model=TransferNamespaceOwnershipResponse,
)
async def transfer_namespace_ownership(
    namespace_id: NamespaceId = Path(..., ge=1, description="Namespace identifier."),
    payload: TransferNamespaceOwnershipRequest = Body(...),
    namespace_service: NamespaceService = Depends(get_namespace_service),
) -> TransferNamespaceOwnershipResponse:
    """Transfer namespace ownership."""
    try:
        result = await namespace_service.transfer_namespace_ownership(
            namespace_id=namespace_id,
            target_login_id=payload.target_login_id,
        )
        return TransferNamespaceOwnershipResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "A referenced resource was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to transfer this namespace.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't transfer namespace ownership.", "cause": str(e)},
        )


@router.delete(
    "/{namespace_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Discard namespace",
    description="Discard a namespace when it is not in use.",
)
async def discard_namespace(
    namespace_id: NamespaceId = Path(..., ge=1, description="Namespace identifier."),
    namespace_service: NamespaceService = Depends(get_namespace_service),
) -> Response:
    """Discard a namespace."""
    try:
        await namespace_service.discard_namespace(namespace_id=namespace_id)
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The namespace was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to discard this namespace.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the namespace and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't discard the namespace.", "cause": str(e)},
        )
