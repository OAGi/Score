"""Business Information Entity API routes."""


from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, Path, Query, Response, status

from app.deps import get_business_information_entity_service
from app.routes.models.business_information_entity import (
    AssignBizCtxToTopLevelAsbiepResponse,
    AssignBizCtxToTopLevelAsbiepRequest,
    CreateASBIERequest,
    CreateASBIEResponse,
    CreateBBIERequest,
    CreateBBIEResponse,
    CreateBBIESCRequest,
    CreateBBIESCResponse,
    CreateTopLevelASBIEPRequest,
    CreateTopLevelASBIEPResponse,
    GetAsbieByBasedAsccManifestIdResponse,
    GetAsbieByAsbieIdResponse,
    GetBbieByBasedBccManifestIdResponse,
    GetBbieByBbieIdResponse,
    GetTopLevelAsbiepResponse,
    ReuseTopLevelASBIEPResponse,
    TopLevelAsbiepListEntry,
    GetTopLevelAsbiepListResponse,
    TransferTopLevelASBIEPOwnershipRequest,
    TransferTopLevelASBIEPOwnershipResponse,
    UnassignBizCtxRequest,
    UpdateASBIERequest,
    UpdateASBIEResponse,
    UpdateBBIERequest,
    UpdateBBIEResponse,
    UpdateBBIESCRequest,
    UpdateBBIESCResponse,
    UpdateTopLevelASBIEPRequest,
    UpdateTopLevelASBIEPResponse,
    UpdateTopLevelAsbiepStateResponse,
    UpdateTopLevelASBIEPStateRequest,
)
from app.routes.utils.date import parse_date_range
from app.services.business_information_entity_service import (
    BusinessInformationEntityService,
    TopLevelAsbiepDependencyBlockedError,
)

router = APIRouter(prefix="/business-information-entities", tags=["business-information-entity"])


def _raise_asbie_owner_not_found(*, top_level_asbiep_id: int, asbie_id: int) -> None:
    raise HTTPException(
        status_code=status.HTTP_404_NOT_FOUND,
        detail={
            "message": "The ASBIE was not found.",
            "cause": f"No ASBIE exists with ID {asbie_id} under top-level ASBIEP ID {top_level_asbiep_id}.",
        },
    )


def _raise_bbie_owner_not_found(*, top_level_asbiep_id: int, bbie_id: int) -> None:
    raise HTTPException(
        status_code=status.HTTP_404_NOT_FOUND,
        detail={
            "message": "The BBIE was not found.",
            "cause": f"No BBIE exists with ID {bbie_id} under top-level ASBIEP ID {top_level_asbiep_id}.",
        },
    )


def _raise_bbie_sc_owner_not_found(*, top_level_asbiep_id: int, bbie_sc_id: int) -> None:
    raise HTTPException(
        status_code=status.HTTP_404_NOT_FOUND,
        detail={
            "message": "The BBIE_SC was not found.",
            "cause": f"No BBIE_SC exists with ID {bbie_sc_id} under top-level ASBIEP ID {top_level_asbiep_id}.",
        },
    )


async def _get_asbie_payload_or_404(
    *,
    asbie_id: int,
    top_level_asbiep_id: int | None = None,
    business_information_entity_service: BusinessInformationEntityService,
) -> GetAsbieByAsbieIdResponse:
    payload = await business_information_entity_service.get_asbie_by_asbie_id(asbie_id=asbie_id)
    if payload is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The ASBIE was not found.", "cause": f"No ASBIE exists with ID {asbie_id}."},
        )
    response = GetAsbieByAsbieIdResponse.model_validate(payload, from_attributes=True)
    if top_level_asbiep_id is not None and int(response.owner_top_level_asbiep.top_level_asbiep_id) != int(top_level_asbiep_id):
        _raise_asbie_owner_not_found(top_level_asbiep_id=top_level_asbiep_id, asbie_id=asbie_id)
    return response


async def _get_bbie_payload_or_404(
    *,
    bbie_id: int,
    top_level_asbiep_id: int | None = None,
    business_information_entity_service: BusinessInformationEntityService,
) -> GetBbieByBbieIdResponse:
    payload = await business_information_entity_service.get_bbie_by_bbie_id(bbie_id=bbie_id)
    if payload is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The BBIE was not found.", "cause": f"No BBIE exists with ID {bbie_id}."},
        )
    response = GetBbieByBbieIdResponse.model_validate(payload, from_attributes=True)
    if top_level_asbiep_id is not None and int(response.owner_top_level_asbiep.top_level_asbiep_id) != int(top_level_asbiep_id):
        _raise_bbie_owner_not_found(top_level_asbiep_id=top_level_asbiep_id, bbie_id=bbie_id)
    return response


async def _ensure_bbie_sc_owner_or_404(
    *,
    bbie_sc_id: int,
    business_information_entity_service: BusinessInformationEntityService,
    top_level_asbiep_id: int | None = None,
) -> None:
    owner_top_level_asbiep_id = await business_information_entity_service.get_bbie_sc_owner_top_level_asbiep_id(
        bbie_sc_id=bbie_sc_id,
    )
    if owner_top_level_asbiep_id is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The BBIE_SC was not found.", "cause": f"No BBIE_SC exists with ID {bbie_sc_id}."},
        )
    if top_level_asbiep_id is not None and int(owner_top_level_asbiep_id) != int(top_level_asbiep_id):
        _raise_bbie_sc_owner_not_found(top_level_asbiep_id=top_level_asbiep_id, bbie_sc_id=bbie_sc_id)


async def _update_asbie_or_error(
    *,
    request: UpdateASBIERequest,
    asbie_id: int,
    business_information_entity_service: BusinessInformationEntityService,
    top_level_asbiep_id: int | None = None,
) -> UpdateASBIEResponse:
    await _get_asbie_payload_or_404(
        asbie_id=asbie_id,
        top_level_asbiep_id=top_level_asbiep_id,
        business_information_entity_service=business_information_entity_service,
    )
    try:
        payload = await business_information_entity_service.update_asbie(
            asbie_id=asbie_id,
            is_used=request.is_used,
            is_nillable=request.is_nillable,
            definition=request.definition,
            cardinality_min=request.cardinality_min,
            cardinality_max=request.cardinality_max,
            remark=request.remark,
        )
        return UpdateASBIEResponse.model_validate(payload, from_attributes=True)
    except Exception as err:
        raise _to_http_error(err)


async def _update_bbie_or_error(
    *,
    request: UpdateBBIERequest,
    bbie_id: int,
    business_information_entity_service: BusinessInformationEntityService,
    top_level_asbiep_id: int | None = None,
) -> UpdateBBIEResponse:
    await _get_bbie_payload_or_404(
        bbie_id=bbie_id,
        top_level_asbiep_id=top_level_asbiep_id,
        business_information_entity_service=business_information_entity_service,
    )
    try:
        payload = await business_information_entity_service.update_bbie(
            bbie_id=bbie_id,
            is_used=request.is_used,
            is_nillable=request.is_nillable,
            cardinality_min=request.cardinality_min,
            cardinality_max=request.cardinality_max,
            definition=request.definition,
            example=request.example,
            remark=request.remark,
            default_value=request.default_value,
            fixed_value=request.fixed_value,
            facet_min_length=request.facet_min_length,
            facet_max_length=request.facet_max_length,
            facet_pattern=request.facet_pattern,
            xbt_manifest_id=request.xbt_manifest_id,
            code_list_manifest_id=request.code_list_manifest_id,
            agency_id_list_manifest_id=request.agency_id_list_manifest_id,
        )
        return UpdateBBIEResponse.model_validate(payload, from_attributes=True)
    except Exception as err:
        raise _to_http_error(err)


async def _update_bbie_sc_or_error(
    *,
    request: UpdateBBIESCRequest,
    bbie_sc_id: int,
    business_information_entity_service: BusinessInformationEntityService,
    top_level_asbiep_id: int | None = None,
) -> UpdateBBIESCResponse:
    await _ensure_bbie_sc_owner_or_404(
        bbie_sc_id=bbie_sc_id,
        top_level_asbiep_id=top_level_asbiep_id,
        business_information_entity_service=business_information_entity_service,
    )
    try:
        payload = await business_information_entity_service.update_bbie_sc(
            bbie_sc_id=bbie_sc_id,
            is_used=request.is_used,
            cardinality_min=request.cardinality_min,
            cardinality_max=request.cardinality_max,
            definition=request.definition,
            example=request.example,
            remark=request.remark,
            biz_term=request.biz_term,
            display_name=request.display_name,
            default_value=request.default_value,
            fixed_value=request.fixed_value,
            facet_min_length=request.facet_min_length,
            facet_max_length=request.facet_max_length,
            facet_pattern=request.facet_pattern,
            xbt_manifest_id=request.xbt_manifest_id,
            code_list_manifest_id=request.code_list_manifest_id,
            agency_id_list_manifest_id=request.agency_id_list_manifest_id,
        )
        return UpdateBBIESCResponse.model_validate(payload, from_attributes=True)
    except Exception as err:
        raise _to_http_error(err)


async def _reuse_top_level_asbiep_or_error(
    *,
    asbie_id: int,
    reuse_top_level_asbiep_id: int,
    business_information_entity_service: BusinessInformationEntityService,
    top_level_asbiep_id: int | None = None,
) -> ReuseTopLevelASBIEPResponse:
    await _get_asbie_payload_or_404(
        asbie_id=asbie_id,
        top_level_asbiep_id=top_level_asbiep_id,
        business_information_entity_service=business_information_entity_service,
    )
    try:
        payload = await business_information_entity_service.reuse_top_level_asbiep(
            asbie_id=asbie_id,
            reuse_top_level_asbiep_id=reuse_top_level_asbiep_id,
        )
        return ReuseTopLevelASBIEPResponse.model_validate(payload, from_attributes=True)
    except Exception as err:
        raise _to_http_error(err)


async def _remove_reused_top_level_asbiep_or_error(
    *,
    asbie_id: int,
    business_information_entity_service: BusinessInformationEntityService,
    top_level_asbiep_id: int | None = None,
) -> Response:
    await _get_asbie_payload_or_404(
        asbie_id=asbie_id,
        top_level_asbiep_id=top_level_asbiep_id,
        business_information_entity_service=business_information_entity_service,
    )
    try:
        await business_information_entity_service.remove_reused_top_level_asbiep(asbie_id=asbie_id)
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except Exception as err:
        raise _to_http_error(err)


@router.get(
    "",
    summary="List top-level ASBIEPs",
    description="Retrieve a paginated list of top-level ASBIEPs.",
    response_model=GetTopLevelAsbiepListResponse,
)
async def get_top_level_asbiep_list(
    library_id: int | None = Query(default=None, ge=1, description="Filter by library ID."),
    release_id: list[int] | None = Query(default=None, description="Filter by release IDs."),
    den: str | None = Query(default=None, description="Filter by DEN or display name (partial match)."),
    version: str | None = Query(default=None, description="Filter by version (partial match)."),
    status_param: str | None = Query(default=None, alias="status", description="Filter by status (partial match)."),
    state: str | None = Query(default=None, description="Filter by state (partial match)."),
    is_deprecated: bool | None = Query(default=None, description="Filter by deprecation flag."),
    created_on: str | None = Query(default=None, description="Filter by creation date range '[before~after]'."),
    last_updated_on: str | None = Query(default=None, description="Filter by last update date range '[before~after]'."),
    order_by: str | None = Query(
        default=None,
        description=(
            "Comma-separated list of properties to order by. Prefix with '-' for descending, '+' for ascending. "
            "Allowed columns: den, version, status, state, creation_timestamp, last_update_timestamp."
        ),
    ),
    offset: int = Query(default=0, ge=0, description="The offset from the beginning of the list."),
    limit: int = Query(default=10, ge=1, le=100, description="The maximum number of items to return."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> GetTopLevelAsbiepListResponse:
    """Return top-level ASBIEP list with business-context summaries.

    Args:
        library_id: Library identifier used to scope the query.
        release_id: Release identifier used to scope the query.
        den: Optional Dictionary Entry Name (DEN) filter.
        version: Optional version filter.
        status_param: Optional status filter value from query parameters.
        state: Optional lifecycle state filter.
        is_deprecated: Optional deprecation flag filter.
        created_on: Optional creation-time filter in ISO-8601 range form.
        last_updated_on: Optional last-update-time filter in ISO-8601 range form.
        order_by: Sort expression used for ordering the result set.
        offset: Zero-based index of the first item to include in the page.
        limit: Maximum number of items to return.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    try:
        created_range = parse_date_range(created_on)
        updated_range = parse_date_range(last_updated_on)
        page = await business_information_entity_service.list_top_level_asbieps(
            limit=limit,
            offset=offset,
            order_by=order_by,
            library_id=library_id,
            release_ids=release_id,
            den=den,
            version=version,
            status=status_param,
            state=state,
            is_deprecated=is_deprecated,
            created_on=created_range,
            last_updated_on=updated_range,
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the parameters and try again.", "cause": str(e)},
        )

    return GetTopLevelAsbiepListResponse(
        items=[TopLevelAsbiepListEntry.model_validate(item, from_attributes=True) for item in page.items],
        total_items=page.total,
        limit=limit,
        offset=offset,
    )


@router.get(
    "/{top_level_asbiep_id}",
    summary="Retrieve top-level ASBIEP",
    description="Retrieve the Top-Level ASBIEP by ID.",
    response_model=GetTopLevelAsbiepResponse,
)
async def get_top_level_asbiep(
    top_level_asbiep_id: int = Path(..., ge=1, description="Top-level ASBIEP ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> GetTopLevelAsbiepResponse:
    """Handle get top level asbiep.

    Args:
        top_level_asbiep_id: Top-level ASBIEP identifier.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    payload = await business_information_entity_service.get_top_level_asbiep(top_level_asbiep_id=top_level_asbiep_id)
    if payload is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "message": "The top-level ASBIEP was not found.",
                "cause": f"No top-level ASBIEP exists with ID {top_level_asbiep_id}.",
            },
        )
    return GetTopLevelAsbiepResponse.model_validate(payload, from_attributes=True)


@router.get(
    "/{top_level_asbiep_id:int}/asbies/{asbie_id:int}",
    summary="Retrieve ASBIE",
    description="Retrieve an ASBIE by ID.",
    response_model=GetAsbieByAsbieIdResponse,
)
async def get_asbie_by_asbie_id(
    top_level_asbiep_id: int = Path(..., ge=1, description="Top-level ASBIEP ID."),
    asbie_id: int = Path(..., ge=1, description="ASBIE ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> GetAsbieByAsbieIdResponse:
    """Handle get asbie by asbie id.

    Args:
        top_level_asbiep_id: Top-level ASBIEP identifier in the URL.
        asbie_id: ASBIE identifier.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    return await _get_asbie_payload_or_404(
        asbie_id=asbie_id,
        top_level_asbiep_id=top_level_asbiep_id,
        business_information_entity_service=business_information_entity_service,
    )


@router.get(
    "/asbies/{asbie_id:int}",
    summary="Retrieve ASBIE",
    description="Retrieve an ASBIE by ID.",
    response_model=GetAsbieByAsbieIdResponse,
    openapi_extra={"x-alternative-endpoint-for": "/business-information-entities/{top_level_asbiep_id}/asbies/{asbie_id}"},
)
async def get_asbie_by_asbie_id_direct(
    asbie_id: int = Path(..., ge=1, description="ASBIE ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> GetAsbieByAsbieIdResponse:
    """Handle get asbie by asbie id without top-level ASBIEP path context."""

    return await _get_asbie_payload_or_404(
        asbie_id=asbie_id,
        business_information_entity_service=business_information_entity_service,
    )


@router.get(
    "/{top_level_asbiep_id}/asbies",
    summary="Retrieve ASBIE by based ASCC manifest ID",
    description="Retrieve an ASBIE structure by top-level ASBIEP ID and based ASCC manifest ID.",
    response_model=GetAsbieByBasedAsccManifestIdResponse,
)
async def get_asbie_by_based_ascc_manifest_id(
    top_level_asbiep_id: int = Path(..., ge=1, description="Top-level ASBIEP ID."),
    based_ascc_manifest_id: int = Query(..., ge=1, description="Based ASCC manifest ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> GetAsbieByBasedAsccManifestIdResponse:
    """Handle get asbie by based ascc manifest id.

    Args:
        top_level_asbiep_id: Top-level ASBIEP identifier.
        based_ascc_manifest_id: ASCC manifest identifier for relationship lookup.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    payload = await business_information_entity_service.get_asbie_by_based_ascc_manifest_id(
        top_level_asbiep_id=top_level_asbiep_id,
        based_ascc_manifest_id=based_ascc_manifest_id,
    )
    if payload is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "message": "The ASBIE template was not found.",
                "cause": (
                    "No ASBIE template exists for "
                    f"top_level_asbiep_id={top_level_asbiep_id}, "
                    f"based_ascc_manifest_id={based_ascc_manifest_id}."
                ),
            },
        )
    return GetAsbieByBasedAsccManifestIdResponse.model_validate(payload, from_attributes=True)


@router.get(
    "/{top_level_asbiep_id:int}/bbies/{bbie_id:int}",
    summary="Retrieve BBIE",
    description="Retrieve a BBIE by ID.",
    response_model=GetBbieByBbieIdResponse,
)
async def get_bbie_by_bbie_id(
    top_level_asbiep_id: int = Path(..., ge=1, description="Top-level ASBIEP ID."),
    bbie_id: int = Path(..., ge=1, description="BBIE ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> GetBbieByBbieIdResponse:
    """Handle get bbie by bbie id.

    Args:
        top_level_asbiep_id: Top-level ASBIEP identifier in the URL.
        bbie_id: BBIE identifier.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    return await _get_bbie_payload_or_404(
        bbie_id=bbie_id,
        top_level_asbiep_id=top_level_asbiep_id,
        business_information_entity_service=business_information_entity_service,
    )


@router.get(
    "/bbies/{bbie_id:int}",
    summary="Retrieve BBIE",
    description="Retrieve a BBIE by ID.",
    response_model=GetBbieByBbieIdResponse,
    openapi_extra={"x-alternative-endpoint-for": "/business-information-entities/{top_level_asbiep_id}/bbies/{bbie_id}"},
)
async def get_bbie_by_bbie_id_direct(
    bbie_id: int = Path(..., ge=1, description="BBIE ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> GetBbieByBbieIdResponse:
    """Handle get bbie by bbie id without top-level ASBIEP path context."""

    return await _get_bbie_payload_or_404(
        bbie_id=bbie_id,
        business_information_entity_service=business_information_entity_service,
    )


@router.get(
    "/{top_level_asbiep_id}/bbies",
    summary="Retrieve BBIE by based BCC manifest ID",
    description="Retrieve a BBIE structure by top-level ASBIEP ID and based BCC manifest ID.",
    response_model=GetBbieByBasedBccManifestIdResponse,
)
async def get_bbie_by_based_bcc_manifest_id(
    top_level_asbiep_id: int = Path(..., ge=1, description="Top-level ASBIEP ID."),
    based_bcc_manifest_id: int = Query(..., ge=1, description="Based BCC manifest ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> GetBbieByBasedBccManifestIdResponse:
    """Handle get bbie by based bcc manifest id.

    Args:
        top_level_asbiep_id: Top-level ASBIEP identifier.
        based_bcc_manifest_id: BCC manifest identifier for relationship lookup.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    payload = await business_information_entity_service.get_bbie_by_based_bcc_manifest_id(
        top_level_asbiep_id=top_level_asbiep_id,
        based_bcc_manifest_id=based_bcc_manifest_id,
    )
    if payload is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "message": "The BBIE template was not found.",
                "cause": (
                    "No BBIE template exists for "
                    f"top_level_asbiep_id={top_level_asbiep_id}, "
                    f"based_bcc_manifest_id={based_bcc_manifest_id}."
                ),
            },
        )
    return GetBbieByBasedBccManifestIdResponse.model_validate(payload, from_attributes=True)


@router.post(
    "",
    summary="Create top-level ASBIEP",
    description="Create a new top-level ASBIEP from an ASCCP manifest and assign business contexts.",
    response_model=CreateTopLevelASBIEPResponse,
    status_code=status.HTTP_201_CREATED,
)
async def create_top_level_asbiep(
    request: CreateTopLevelASBIEPRequest,
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> CreateTopLevelASBIEPResponse:
    """Create a top-level ASBIEP.

    Args:
        request: Create request body.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    try:
        payload = await business_information_entity_service.create_top_level_asbiep(
            asccp_manifest_id=request.asccp_manifest_id,
            biz_ctx_list=request.biz_ctx_list,
        )
        return CreateTopLevelASBIEPResponse.model_validate(payload, from_attributes=True)
    except Exception as err:
        raise _to_http_error(err)


@router.post(
    "/{top_level_asbiep_id:int}",
    summary="Update top-level ASBIEP",
    description="Update editable metadata fields on a top-level ASBIEP.",
    response_model=UpdateTopLevelASBIEPResponse,
)
async def update_top_level_asbiep(
    request: UpdateTopLevelASBIEPRequest,
    top_level_asbiep_id: int = Path(..., ge=1, description="Top-level ASBIEP ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> UpdateTopLevelASBIEPResponse:
    """Update mutable fields on a top-level ASBIEP.

    Args:
        request: Update request body.
        top_level_asbiep_id: Top-level ASBIEP identifier.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    try:
        payload = await business_information_entity_service.update_top_level_asbiep(
            top_level_asbiep_id=top_level_asbiep_id,
            version=request.version,
            status=request.status,
            display_name=request.display_name,
            biz_term=request.biz_term,
            definition=request.definition,
            remark=request.remark,
            is_deprecated=request.is_deprecated,
            deprecated_reason=request.deprecated_reason,
            deprecated_remark=request.deprecated_remark,
        )
        return UpdateTopLevelASBIEPResponse.model_validate(payload, from_attributes=True)
    except Exception as err:
        raise _to_http_error(err)


@router.post(
    "/{top_level_asbiep_id}/state",
    summary="Update top-level ASBIEP state",
    description="Change the lifecycle state of a top-level ASBIEP.",
    response_model=UpdateTopLevelAsbiepStateResponse,
)
async def update_top_level_asbiep_state(
    request: UpdateTopLevelASBIEPStateRequest,
    top_level_asbiep_id: int = Path(..., ge=1, description="Top-level ASBIEP ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> UpdateTopLevelAsbiepStateResponse:
    """Update top-level ASBIEP state.

    Args:
        request: State transition request body.
        top_level_asbiep_id: Top-level ASBIEP identifier.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    try:
        payload = await business_information_entity_service.update_top_level_asbiep_state(
            top_level_asbiep_id=top_level_asbiep_id,
            state=request.state,
        )
        return UpdateTopLevelAsbiepStateResponse.model_validate(payload, from_attributes=True)
    except Exception as err:
        raise _to_http_error(err)


@router.delete(
    "/{top_level_asbiep_id}",
    summary="Delete top-level ASBIEP",
    description="Delete a top-level ASBIEP and its owned BIE tree.",
    status_code=status.HTTP_204_NO_CONTENT,
)
async def delete_top_level_asbiep(
    top_level_asbiep_id: int = Path(..., ge=1, description="Top-level ASBIEP ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> Response:
    """Delete a top-level ASBIEP.

    Args:
        top_level_asbiep_id: Top-level ASBIEP identifier.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    try:
        await business_information_entity_service.delete_top_level_asbiep(top_level_asbiep_id=top_level_asbiep_id)
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except Exception as err:
        raise _to_http_error(err)


@router.post(
    "/{top_level_asbiep_id}/ownership",
    summary="Transfer top-level ASBIEP ownership",
    description="Transfer ownership of a top-level ASBIEP to another user.",
    response_model=TransferTopLevelASBIEPOwnershipResponse,
)
async def transfer_top_level_asbiep_ownership(
    request: TransferTopLevelASBIEPOwnershipRequest,
    top_level_asbiep_id: int = Path(..., ge=1, description="Top-level ASBIEP ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> TransferTopLevelASBIEPOwnershipResponse:
    """Transfer top-level ASBIEP ownership.

    Args:
        request: Ownership transfer request body.
        top_level_asbiep_id: Top-level ASBIEP identifier.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    try:
        payload = await business_information_entity_service.transfer_top_level_asbiep_ownership(
            top_level_asbiep_id=top_level_asbiep_id,
            target_user_id=request.target_user_id,
        )
        return TransferTopLevelASBIEPOwnershipResponse.model_validate(payload, from_attributes=True)
    except Exception as err:
        raise _to_http_error(err)


@router.post(
    "/{top_level_asbiep_id}/business-contexts",
    summary="Assign business context to top-level ASBIEP",
    description="Assign one business context to a top-level ASBIEP.",
    response_model=AssignBizCtxToTopLevelAsbiepResponse,
    responses={
        status.HTTP_204_NO_CONTENT: {
            "description": "The business context was already assigned; no content is returned.",
        }
    },
)
async def assign_biz_ctx_to_top_level_asbiep(
    request: AssignBizCtxToTopLevelAsbiepRequest,
    top_level_asbiep_id: int = Path(..., ge=1, description="Top-level ASBIEP ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> Response | AssignBizCtxToTopLevelAsbiepResponse:
    """Assign a business context to top-level ASBIEP.

    Args:
        request: Assignment request body.
        top_level_asbiep_id: Top-level ASBIEP identifier.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    try:
        payload = await business_information_entity_service.assign_biz_ctx_to_top_level_asbiep(
            top_level_asbiep_id=top_level_asbiep_id,
            biz_ctx_id=request.biz_ctx_id,
        )
        if payload is None:
            return Response(status_code=status.HTTP_204_NO_CONTENT)
        return AssignBizCtxToTopLevelAsbiepResponse.model_validate(payload, from_attributes=True)
    except Exception as err:
        raise _to_http_error(err)


@router.delete(
    "/{top_level_asbiep_id}/business-contexts",
    summary="Unassign business context from top-level ASBIEP",
    description="Remove one business context assignment from a top-level ASBIEP.",
    status_code=status.HTTP_204_NO_CONTENT,
)
async def unassign_biz_ctx_from_top_level_asbiep(
    request: UnassignBizCtxRequest,
    top_level_asbiep_id: int = Path(..., ge=1, description="Top-level ASBIEP ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> Response:
    """Unassign a business context from top-level ASBIEP.

    Args:
        request: Unassignment request body.
        top_level_asbiep_id: Top-level ASBIEP identifier.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    try:
        await business_information_entity_service.unassign_biz_ctx_from_top_level_asbiep(
            top_level_asbiep_id=top_level_asbiep_id,
            biz_ctx_id=request.biz_ctx_id,
        )
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except Exception as err:
        raise _to_http_error(err)


@router.post(
    "/{top_level_asbiep_id:int}/asbies",
    summary="Create ASBIE",
    description="Create an ASBIE under a parent ABIE from a based ASCC manifest.",
    response_model=CreateASBIEResponse,
    status_code=status.HTTP_201_CREATED,
)
async def create_asbie(
    request: CreateASBIERequest,
    top_level_asbiep_id: int = Path(..., ge=1, description="Top-level ASBIEP ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> CreateASBIEResponse:
    """Create an ASBIE.

    Args:
        request: Create request body.
        top_level_asbiep_id: Top-level ASBIEP identifier in the URL.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    _ = top_level_asbiep_id
    try:
        payload = await business_information_entity_service.create_asbie(
            from_abie_id=request.from_abie_id,
            based_ascc_manifest_id=request.based_ascc_manifest_id,
        )
        return CreateASBIEResponse.model_validate(payload, from_attributes=True)
    except Exception as err:
        raise _to_http_error(err)


@router.post(
    "/{top_level_asbiep_id:int}/asbies/{asbie_id:int}",
    summary="Update ASBIE",
    description="Update ASBIE usage, cardinality, nillable, and remark/definition fields.",
    response_model=UpdateASBIEResponse,
)
async def update_asbie(
    request: UpdateASBIERequest,
    top_level_asbiep_id: int = Path(..., ge=1, description="Top-level ASBIEP ID."),
    asbie_id: int = Path(..., ge=1, description="ASBIE ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> UpdateASBIEResponse:
    """Update an ASBIE.

    Args:
        request: Update request body.
        top_level_asbiep_id: Top-level ASBIEP identifier in the URL.
        asbie_id: ASBIE identifier.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    return await _update_asbie_or_error(
        request=request,
        asbie_id=asbie_id,
        top_level_asbiep_id=top_level_asbiep_id,
        business_information_entity_service=business_information_entity_service,
    )


@router.post(
    "/asbies/{asbie_id:int}",
    summary="Update ASBIE",
    description="Update ASBIE usage, cardinality, nillable, and remark/definition fields.",
    response_model=UpdateASBIEResponse,
    openapi_extra={"x-alternative-endpoint-for": "/business-information-entities/{top_level_asbiep_id}/asbies/{asbie_id}"},
)
async def update_asbie_direct(
    request: UpdateASBIERequest,
    asbie_id: int = Path(..., ge=1, description="ASBIE ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> UpdateASBIEResponse:
    """Update an ASBIE by child identifier only."""
    return await _update_asbie_or_error(
        request=request,
        asbie_id=asbie_id,
        business_information_entity_service=business_information_entity_service,
    )


@router.post(
    "/{top_level_asbiep_id:int}/bbies",
    summary="Create BBIE",
    description="Create a BBIE under a parent ABIE from a based BCC manifest.",
    response_model=CreateBBIEResponse,
    status_code=status.HTTP_201_CREATED,
)
async def create_bbie(
    request: CreateBBIERequest,
    top_level_asbiep_id: int = Path(..., ge=1, description="Top-level ASBIEP ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> CreateBBIEResponse:
    """Create a BBIE.

    Args:
        request: Create request body.
        top_level_asbiep_id: Top-level ASBIEP identifier in the URL.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    _ = top_level_asbiep_id
    try:
        payload = await business_information_entity_service.create_bbie(
            from_abie_id=request.from_abie_id,
            based_bcc_manifest_id=request.based_bcc_manifest_id,
        )
        return CreateBBIEResponse.model_validate(payload, from_attributes=True)
    except Exception as err:
        raise _to_http_error(err)


@router.post(
    "/{top_level_asbiep_id:int}/bbies/{bbie_id:int}",
    summary="Update BBIE",
    description="Update BBIE usage, cardinality, value constraints, and facets.",
    response_model=UpdateBBIEResponse,
)
async def update_bbie(
    request: UpdateBBIERequest,
    top_level_asbiep_id: int = Path(..., ge=1, description="Top-level ASBIEP ID."),
    bbie_id: int = Path(..., ge=1, description="BBIE ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> UpdateBBIEResponse:
    """Update a BBIE.

    Args:
        request: Update request body.
        top_level_asbiep_id: Top-level ASBIEP identifier in the URL.
        bbie_id: BBIE identifier.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    return await _update_bbie_or_error(
        request=request,
        bbie_id=bbie_id,
        top_level_asbiep_id=top_level_asbiep_id,
        business_information_entity_service=business_information_entity_service,
    )


@router.post(
    "/bbies/{bbie_id:int}",
    summary="Update BBIE",
    description="Update BBIE usage, cardinality, value constraints, and facets.",
    response_model=UpdateBBIEResponse,
    openapi_extra={"x-alternative-endpoint-for": "/business-information-entities/{top_level_asbiep_id}/bbies/{bbie_id}"},
)
async def update_bbie_direct(
    request: UpdateBBIERequest,
    bbie_id: int = Path(..., ge=1, description="BBIE ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> UpdateBBIEResponse:
    """Update a BBIE by child identifier only."""
    return await _update_bbie_or_error(
        request=request,
        bbie_id=bbie_id,
        business_information_entity_service=business_information_entity_service,
    )


@router.post(
    "/bbie-scs",
    summary="Create BBIE_SC",
    description="Create a supplementary BBIE_SC under a BBIE from a DT_SC manifest.",
    response_model=CreateBBIESCResponse,
    status_code=status.HTTP_201_CREATED,
)
async def create_bbie_sc(
    request: CreateBBIESCRequest,
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> CreateBBIESCResponse:
    """Create a BBIE_SC.

    Args:
        request: Create request body.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    try:
        payload = await business_information_entity_service.create_bbie_sc(
            bbie_id=request.bbie_id,
            based_dt_sc_manifest_id=request.based_dt_sc_manifest_id,
        )
        return CreateBBIESCResponse.model_validate(payload, from_attributes=True)
    except Exception as err:
        raise _to_http_error(err)


@router.post(
    "/bbie-scs/{bbie_sc_id:int}",
    summary="Update BBIE_SC",
    description="Update BBIE_SC usage, cardinality, value constraints, and facets.",
    response_model=UpdateBBIESCResponse,
    openapi_extra={"x-alternative-endpoint-for": "/business-information-entities/{top_level_asbiep_id}/bbie-scs/{bbie_sc_id}"},
)
async def update_bbie_sc(
    request: UpdateBBIESCRequest,
    bbie_sc_id: int = Path(..., ge=1, description="BBIE_SC ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> UpdateBBIESCResponse:
    """Update a BBIE_SC.

    Args:
        request: Update request body.
        bbie_sc_id: BBIE_SC identifier.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    return await _update_bbie_sc_or_error(
        request=request,
        bbie_sc_id=bbie_sc_id,
        business_information_entity_service=business_information_entity_service,
    )


@router.post(
    "/{top_level_asbiep_id:int}/bbie-scs/{bbie_sc_id:int}",
    summary="Update BBIE_SC",
    description="Update BBIE_SC usage, cardinality, value constraints, and facets.",
    response_model=UpdateBBIESCResponse,
)
async def update_bbie_sc_by_top_level_asbiep_id(
    request: UpdateBBIESCRequest,
    top_level_asbiep_id: int = Path(..., ge=1, description="Top-level ASBIEP ID."),
    bbie_sc_id: int = Path(..., ge=1, description="BBIE_SC ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> UpdateBBIESCResponse:
    """Update a BBIE_SC under a specific top-level ASBIEP."""
    return await _update_bbie_sc_or_error(
        request=request,
        bbie_sc_id=bbie_sc_id,
        top_level_asbiep_id=top_level_asbiep_id,
        business_information_entity_service=business_information_entity_service,
    )


@router.post(
    "/{top_level_asbiep_id:int}/asbies/{asbie_id:int}/reuse",
    summary="Reuse top-level ASBIEP",
    description=(
        "Reuse another top-level ASBIEP as the target of this ASBIE. "
        "The top-level ASBIEP identified by `reuse_top_level_asbiep_id` must be different from the ASBIE owner's top-level ASBIEP, "
        "must belong to the same release, and must point to an ASBIEP whose ASCCP matches the ASBIE's ASCC target. "
        "When successful, the ASBIE is marked as used."
    ),
    response_model=ReuseTopLevelASBIEPResponse,
)
async def reuse_top_level_asbiep(
    top_level_asbiep_id: int = Path(..., ge=1, description="Top-level ASBIEP ID."),
    asbie_id: int = Path(..., ge=1, description="ASBIE ID."),
    reuse_top_level_asbiep_id: int = Query(..., ge=1, description="Top-level ASBIEP ID to reuse."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> ReuseTopLevelASBIEPResponse:
    """Reuse a top-level ASBIEP in an ASBIE.

    Args:
        top_level_asbiep_id: Top-level ASBIEP ID.
        asbie_id: ASBIE ID.
        reuse_top_level_asbiep_id: Top-level ASBIEP ID to reuse.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    return await _reuse_top_level_asbiep_or_error(
        asbie_id=asbie_id,
        reuse_top_level_asbiep_id=reuse_top_level_asbiep_id,
        top_level_asbiep_id=top_level_asbiep_id,
        business_information_entity_service=business_information_entity_service,
    )


@router.post(
    "/asbies/{asbie_id:int}/reuse",
    summary="Reuse top-level ASBIEP",
    description=(
        "Reuse another top-level ASBIEP as the target of this ASBIE. "
        "The top-level ASBIEP identified by `reuse_top_level_asbiep_id` must be different from the ASBIE owner's top-level ASBIEP, "
        "must belong to the same release, and must point to an ASBIEP whose ASCCP matches the ASBIE's ASCC target. "
        "When successful, the ASBIE is marked as used."
    ),
    response_model=ReuseTopLevelASBIEPResponse,
    openapi_extra={"x-alternative-endpoint-for": "/business-information-entities/{top_level_asbiep_id}/asbies/{asbie_id}/reuse"},
)
async def reuse_top_level_asbiep_direct(
    asbie_id: int = Path(..., ge=1, description="ASBIE ID."),
    reuse_top_level_asbiep_id: int = Query(..., ge=1, description="Top-level ASBIEP ID to reuse."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> ReuseTopLevelASBIEPResponse:
    """Reuse a top-level ASBIEP in an ASBIE using only the ASBIE ID in the path."""
    return await _reuse_top_level_asbiep_or_error(
        asbie_id=asbie_id,
        reuse_top_level_asbiep_id=reuse_top_level_asbiep_id,
        business_information_entity_service=business_information_entity_service,
    )


@router.delete(
    "/{top_level_asbiep_id:int}/asbies/{asbie_id:int}/reuse",
    summary="Remove reused top-level ASBIEP",
    description=(
        "Remove the reused top-level ASBIEP from this ASBIE and switch back to the owner's local structure. "
        "If a matching owner-local ASBIEP already exists for the same path and owner top-level ASBIEP, it is reused. "
        "Otherwise, a new owner-local ASBIEP structure is created."
    ),
    status_code=status.HTTP_204_NO_CONTENT,
)
async def remove_reused_top_level_asbiep(
    top_level_asbiep_id: int = Path(..., ge=1, description="Top-level ASBIEP ID."),
    asbie_id: int = Path(..., ge=1, description="ASBIE ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> Response:
    """Remove a reused top-level ASBIEP from an ASBIE.

    Args:
        top_level_asbiep_id: Top-level ASBIEP ID.
        asbie_id: ASBIE ID.
        business_information_entity_service: Business-information-entity service dependency.

    Returns:
        Response payload for the requested resource.
    """
    return await _remove_reused_top_level_asbiep_or_error(
        asbie_id=asbie_id,
        top_level_asbiep_id=top_level_asbiep_id,
        business_information_entity_service=business_information_entity_service,
    )


@router.delete(
    "/asbies/{asbie_id:int}/reuse",
    summary="Remove reused top-level ASBIEP",
    description=(
        "Remove the reused top-level ASBIEP from this ASBIE and switch back to the owner's local structure. "
        "If a matching owner-local ASBIEP already exists for the same path and owner top-level ASBIEP, it is reused. "
        "Otherwise, a new owner-local ASBIEP structure is created."
    ),
    status_code=status.HTTP_204_NO_CONTENT,
    openapi_extra={"x-alternative-endpoint-for": "/business-information-entities/{top_level_asbiep_id}/asbies/{asbie_id}/reuse"},
)
async def remove_reused_top_level_asbiep_direct(
    asbie_id: int = Path(..., ge=1, description="ASBIE ID."),
    business_information_entity_service: BusinessInformationEntityService = Depends(get_business_information_entity_service),
) -> Response:
    """Remove a reused top-level ASBIEP using only the ASBIE ID in the path."""
    return await _remove_reused_top_level_asbiep_or_error(
        asbie_id=asbie_id,
        business_information_entity_service=business_information_entity_service,
    )


def _to_http_error(err: Exception) -> HTTPException:
    """Map service/repository exceptions into HTTPException.

    Args:
        err: Raised exception from service/repository layers.

    Returns:
        Result of the operation.
    """
    if isinstance(err, TopLevelAsbiepDependencyBlockedError):
        detail: dict[str, object] = {
            "message": (
                "The request is blocked by dependent top-level ASBIEPs."
                if err.dependents
                else "The request is blocked by dependencies."
            ),
            "cause": str(err),
            "dependents": [
                {
                    "top_level_asbiep_id": dependent.top_level_asbiep_id,
                    "state": dependent.state,
                    "display_name": dependent.display_name,
                    "den": dependent.den,
                }
                for dependent in err.dependents
            ],
        }
        if err.code_lists:
            detail["code_lists"] = [
                {
                    "code_list_manifest_id": code_list.code_list_manifest_id,
                    "state": code_list.state,
                    "name": code_list.name,
                    "list_id": code_list.list_id,
                    "version_id": code_list.version_id,
                }
                for code_list in err.code_lists
            ]
        return HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=detail,
        )
    if isinstance(err, LookupError):
        return HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The requested resource was not found.", "cause": str(err)},
        )
    if isinstance(err, PermissionError):
        return HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "The request is not allowed.", "cause": str(err)},
        )
    if isinstance(err, ValueError):
        return HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the parameters and try again.", "cause": str(err)},
        )
    return HTTPException(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        detail={"message": "The request failed due to an internal error.", "cause": str(err)},
    )
