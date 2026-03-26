"""Core Component API routes.

Provides read-only endpoints for listing unified core components and retrieving
ACC/ASCCP/BCCP detail payloads by manifest ID.

Terminology aligns with CCTS (UN/CEFACT Core Components Technical Specification,
published as ISO 15000-5):
- ACC: Aggregate Core Component
- ASCCP: Association Core Component Property
- BCCP: Basic Core Component Property
"""


from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, Path, Query, status

from app.deps import get_core_component_service
from app.routes.models.core_component import (
    CoreComponentListEntry,
    GetAccByAccManifestIdResponse,
    GetAsccpByAsccpManifestIdResponse,
    GetBccpByBccpManifestIdResponse,
    GetCoreComponentListResponse,
)
from app.routes.utils.date import parse_date_range
from app.services.core_component_service import CoreComponentService
from app.types.identifiers import AccManifestId, AsccpManifestId, BccpManifestId
from app.types.identifiers import ReleaseId

router = APIRouter(prefix="/core-components", tags=["core-component"])


@router.get(
    "",
    summary="List core components",
    description=(
        "Retrieve a paginated list of core components: "
        "ACC (Aggregate Core Component), ASCCP (Association Core Component Property), "
        "and BCCP (Basic Core Component Property)."
    ),
    response_model=GetCoreComponentListResponse,
)
async def get_core_component_list(
    release_id: ReleaseId = Query(..., ge=1, description="Filter by release ID."),
    types: str | None = Query(
        default=None,
        json_schema_extra={"enum": ["ACC", "ASCCP", "BCCP"], "x-comma-separated": True},
        description=(
            "Comma-separated component types: ACC, ASCCP, BCCP. "
            "If omitted or empty, all component types are included."
        ),
    ),
    den: str | None = Query(default=None, description="Filter by DEN (partial match)."),
    tag: str | None = Query(default=None, description="Filter by tag name (partial match)."),
    created_on: str | None = Query(default=None, description="Filter by creation date range '[before~after]'."),
    last_updated_on: str | None = Query(default=None, description="Filter by last update date range '[before~after]'."),
    order_by: str | None = Query(
        default=None,
        description=(
            "Comma-separated list of properties to order by. Prefix with '-' for descending, '+' for ascending. "
            "Allowed columns: den, name, definition, creation_timestamp, last_update_timestamp."
        ),
    ),
    offset: int = Query(default=0, ge=0, description="The offset from the beginning of the list."),
    limit: int = Query(default=10, ge=1, le=100, description="The maximum number of items to return."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> GetCoreComponentListResponse:
    """Return a paginated list of core components.

    Args:
        release_id: Release identifier used to scope the query.
        types: Optional component type filter list.
        den: Optional Dictionary Entry Name (DEN) filter.
        tag: Optional tag filter.
        created_on: Optional creation-time filter in ISO-8601 range form.
        last_updated_on: Optional last-update-time filter in ISO-8601 range form.
        order_by: Sort expression used for ordering the result set.
        offset: Zero-based index of the first item to include in the page.
        limit: Maximum number of items to return.
        core_component_service: Core-component service dependency.

    Returns:
        Paginated response containing matching resources.
    """
    all_types = ["ACC", "ASCCP", "BCCP"]
    types_list = all_types
    if types is not None:
        parsed = [item.strip().upper() for item in types.split(",") if item.strip()]
        types_list = parsed if parsed else all_types
    invalid_types = [item for item in types_list if item not in {"ACC", "ASCCP", "BCCP"}]
    if invalid_types:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={
                "message": "The request is invalid. Check the parameters and try again.",
                "cause": f"Invalid component types: {', '.join(invalid_types)}.",
            },
        )

    try:
        created_range = parse_date_range(created_on)
        updated_range = parse_date_range(last_updated_on)
        page = await core_component_service.list(
            release_id=release_id,
            types=types_list,
            limit=limit,
            offset=offset,
            order_by=order_by,
            den=den,
            tag=tag,
            created_on=created_range,
            last_updated_on=updated_range,
        )
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The release was not found.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the parameters and try again.", "cause": str(e)},
        )

    return GetCoreComponentListResponse(
        items=[CoreComponentListEntry.model_validate(item, from_attributes=True) for item in page.items],
        total_items=page.total,
        limit=limit,
        offset=offset,
    )


@router.get(
    "/acc/{acc_manifest_id}",
    summary="Retrieve ACC",
    description="Returns ACC (Aggregate Core Component) details by acc_manifest_id.",
    response_model=GetAccByAccManifestIdResponse,
)
async def get_acc_by_acc_manifest_id(
    acc_manifest_id: AccManifestId = Path(..., ge=1, description="ACC (Aggregate Core Component) manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> GetAccByAccManifestIdResponse:
    """Return ACC details.

    Args:
        acc_manifest_id: ACC manifest identifier.
        core_component_service: Core-component service dependency.

    Returns:
        Response payload for the requested resource.
    """
    row = await core_component_service.get_acc(acc_manifest_id)
    if row is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The ACC was not found.", "cause": f"No ACC exists with manifest ID {int(acc_manifest_id)}."},
        )
    return GetAccByAccManifestIdResponse.model_validate(row, from_attributes=True)


@router.get(
    "/asccp/{asccp_manifest_id}",
    summary="Retrieve ASCCP",
    description=(
        "Returns ASCCP (Association Core Component Property) details by asccp_manifest_id."
    ),
    response_model=GetAsccpByAsccpManifestIdResponse,
)
async def get_asccp_by_asccp_manifest_id(
    asccp_manifest_id: AsccpManifestId = Path(
        ...,
        ge=1,
        description="ASCCP (Association Core Component Property) manifest ID.",
    ),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> GetAsccpByAsccpManifestIdResponse:
    """Return ASCCP details.

    Args:
        asccp_manifest_id: ASCCP manifest identifier.
        core_component_service: Core-component service dependency.

    Returns:
        Response payload for the requested resource.
    """
    row = await core_component_service.get_asccp(asccp_manifest_id)
    if row is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The ASCCP was not found.", "cause": f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}."},
        )
    return GetAsccpByAsccpManifestIdResponse.model_validate(row, from_attributes=True)


@router.get(
    "/bccp/{bccp_manifest_id}",
    summary="Retrieve BCCP",
    description=(
        "Returns BCCP (Basic Core Component Property) details by bccp_manifest_id."
    ),
    response_model=GetBccpByBccpManifestIdResponse,
)
async def get_bccp_by_bccp_manifest_id(
    bccp_manifest_id: BccpManifestId = Path(
        ...,
        ge=1,
        description="BCCP (Basic Core Component Property) manifest ID.",
    ),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> GetBccpByBccpManifestIdResponse:
    """Return BCCP details.

    Args:
        bccp_manifest_id: BCCP manifest identifier.
        core_component_service: Core-component service dependency.

    Returns:
        Response payload for the requested resource.
    """
    row = await core_component_service.get_bccp(bccp_manifest_id)
    if row is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The BCCP was not found.", "cause": f"No BCCP exists with manifest ID {int(bccp_manifest_id)}."},
    )
    return GetBccpByBccpManifestIdResponse.model_validate(row, from_attributes=True)
