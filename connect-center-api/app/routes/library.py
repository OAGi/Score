"""Library API routes."""


from __future__ import annotations

from fastapi import APIRouter, Body, Depends, HTTPException, Path, Query, Response, status

from app.deps import get_library_service
from app.routes.models.library import (
    CreateLibraryRequest,
    CreateLibraryResponse,
    GetLibraryByLibraryIdResponse,
    GetLibraryListResponse,
    LibraryEntry,
    UpdateLibraryReleaseDependenciesRequest,
    UpdateLibraryReleaseDependenciesResponse,
    UpdateLibraryRequest,
    UpdateLibraryResponse,
)
from app.services.library_service import LibraryService
from app.types.identifiers import LibraryId
from app.utils.date import parse_date_range

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
    """Return a paginated list of libraries."""
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


@router.post(
    "",
    status_code=status.HTTP_201_CREATED,
    summary="Create library",
    description="Create a new library and seed its working release.",
    response_model=CreateLibraryResponse,
)
async def create_library(
    payload: CreateLibraryRequest = Body(...),
    library_service: LibraryService = Depends(get_library_service),
) -> CreateLibraryResponse:
    """Create a library and return its identifier."""
    try:
        result = await library_service.create_library(
            type=payload.type,
            name=payload.name,
            organization=payload.organization,
            description=payload.description,
            link=payload.link,
            domain=payload.domain,
            namespace_uri=payload.namespace_uri,
            namespace_prefix=payload.namespace_prefix,
        )
        return CreateLibraryResponse.model_validate(result, from_attributes=True)
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to create libraries.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't create the library.", "cause": str(e)},
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
    """Return a library by ID."""
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


@router.post(
    "/{library_id}",
    summary="Update library",
    description="Update an existing library.",
    response_model=UpdateLibraryResponse,
)
async def update_library(
    library_id: LibraryId = Path(..., description="Target library identifier."),
    payload: UpdateLibraryRequest = Body(...),
    library_service: LibraryService = Depends(get_library_service),
) -> UpdateLibraryResponse:
    """Update a library and return the changed fields."""
    try:
        result = await library_service.update_library(
            library_id=library_id,
            type=payload.type,
            name=payload.name,
            organization=payload.organization,
            description=payload.description,
            link=payload.link,
            domain=payload.domain,
            state=payload.state,
            is_default=payload.is_default,
        )
        return UpdateLibraryResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The library was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to update libraries.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't update the library.", "cause": str(e)},
        )


@router.post(
    "/{library_id}/release-dependencies",
    summary="Update library release dependencies",
    description="Replace the dependency list for the library's working release.",
    response_model=UpdateLibraryReleaseDependenciesResponse,
)
async def update_library_release_dependencies(
    library_id: LibraryId = Path(..., description="Target library identifier."),
    payload: UpdateLibraryReleaseDependenciesRequest = Body(...),
    library_service: LibraryService = Depends(get_library_service),
) -> UpdateLibraryReleaseDependenciesResponse:
    """Replace working-release dependencies for a library."""
    try:
        result = await library_service.update_library_release_dependencies(
            library_id=library_id,
            release_ids=payload.release_ids,
        )
        return UpdateLibraryReleaseDependenciesResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "A referenced resource was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to update library release dependencies.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't update the library release dependencies.", "cause": str(e)},
        )


@router.delete(
    "/{library_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Discard library",
    description="Discard a library permanently when it passes the discard checks.",
)
async def discard_library(
    library_id: LibraryId = Path(..., description="Target library identifier."),
    library_service: LibraryService = Depends(get_library_service),
) -> Response:
    """Discard a library."""
    try:
        await library_service.discard_library(library_id=library_id)
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The library was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to discard libraries.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The library cannot be discarded.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't discard the library.", "cause": str(e)},
        )
