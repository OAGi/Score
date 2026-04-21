"""Core Component API routes.

Provides endpoints for listing unified core components, creating/updating ACCs,
appending ASCC/BCC relationships, updating ACC state, and retrieving
ACC/ASCCP/BCCP detail payloads by manifest ID.

Terminology aligns with CCTS (UN/CEFACT Core Components Technical Specification,
published as ISO 15000-5):
- ACC: Aggregate Core Component
- ASCCP: Association Core Component Property
- BCCP: Basic Core Component Property
"""


from __future__ import annotations

from fastapi import APIRouter, Body, Depends, HTTPException, Path, Query, Response, status

from app.deps import get_core_component_service
from app.routes.models.core_component import (
    AddAsccToAccRequest,
    AddAsccToAccResponse,
    AddBccToAccRequest,
    AddBccToAccResponse,
    CoreComponentListEntry,
    CreateAccRequest,
    CreateAccResponse,
    CreateAsccpRequest,
    CreateAsccpResponse,
    CreateBccpRequest,
    CreateBccpResponse,
    GetAccByAccManifestIdResponse,
    GetAsccpByAsccpManifestIdResponse,
    GetBccpByBccpManifestIdResponse,
    GetCoreComponentListResponse,
    MoveAsccRequest,
    MoveBccRequest,
    UpdateAsccRequest,
    UpdateAsccResponse,
    UpdateAccRequest,
    UpdateAccResponse,
    UpdateAccStateRequest,
    TransferOwnershipRequest,
    TransferAccOwnershipResponse,
    TransferAsccpOwnershipResponse,
    TransferBccpOwnershipResponse,
    UpdateBccRequest,
    UpdateBccResponse,
    UpdateAsccpRequest,
    UpdateAsccpResponse,
    UpdateAsccpStateRequest,
    UpdateBccpRequest,
    UpdateBccpResponse,
    UpdateBccpStateRequest,
)
from app.utils.date import parse_date_range
from app.services.core_component_service import CoreComponentService
from app.types.unset import UNSET
from app.types.identifiers import (
    AccManifestId,
    AsccManifestId,
    AsccpManifestId,
    BccManifestId,
    BccpManifestId,
    DataTypeManifestId,
)
from app.types.identifiers import ReleaseId

router = APIRouter(prefix="/core-components", tags=["core-component"])


def _map_bcc_entity_type_update(entity_type: str | int) -> str:
    """Translate API-facing BCC entity-type labels to stored service labels."""
    return "Attribute" if entity_type in {"Attribute", 0} else "Element"


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


@router.post(
    "/acc",
    status_code=status.HTTP_201_CREATED,
    summary="Create ACC",
    description=(
        "Create an ACC (Aggregate Core Component). Unless the user explicitly indicates a base ACC, "
        "developer extension ACC, or semantic group ACC, the default component type should remain `Semantics`."
    ),
    response_model=CreateAccResponse,
)
async def create_acc(
    payload: CreateAccRequest = Body(...),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> CreateAccResponse:
    """Create an ACC and return its manifest ID."""
    try:
        result = await core_component_service.create_acc(
            release_id=payload.release_id,
            based_acc_manifest_id=payload.based_acc_manifest_id,
            initial_object_class_term=payload.initial_object_class_term,
            initial_component_type=payload.initial_component_type,
            initial_definition=payload.initial_definition,
            namespace_id=payload.namespace_id,
            tag_id=payload.tag_id,
        )
        return CreateAccResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "A referenced resource was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to create ACCs.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't create the ACC.", "cause": str(e)},
        )


@router.post(
    "/acc/{acc_manifest_id}/ascc/{asccp_manifest_id}",
    status_code=status.HTTP_201_CREATED,
    summary="Add ASCC to ACC",
    description="Add an ASCC (Association Core Component) to an ACC sequence. If the ASCCP is already present, use `Reorder ASCC in ACC` instead.",
    response_model=AddAsccToAccResponse,
)
async def add_ascc_to_acc(
    acc_manifest_id: AccManifestId = Path(..., ge=1, description="ACC (Aggregate Core Component) manifest ID."),
    asccp_manifest_id: AsccpManifestId = Path(
        ...,
        ge=1,
        description=(
            "ASCCP (Association Core Component Property) manifest ID to add. If the ASCCP is in the same library "
            "as the ACC, it must be from the ACC release. If it is in a different library, its release must be "
            "one of the ACC release dependencies."
        ),
    ),
    *,
    index: int = Query(
        default=-1,
        description="Zero-based insertion index. Defaults to -1, which places the ASCC at the end of the ACC sequence.",
    ),
    payload: AddAsccToAccRequest | None = Body(
        default=None,
        description="Optional relationship fields to set as part of the initial add operation.",
    ),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> AddAsccToAccResponse:
    """Add an ASCC relationship and return its manifest ID."""
    try:
        result = await core_component_service.add_ascc_to_acc(
            acc_manifest_id=acc_manifest_id,
            asccp_manifest_id=asccp_manifest_id,
            index=index,
            cardinality_min=None if payload is None else payload.cardinality_min,
            cardinality_max=None if payload is None else payload.cardinality_max,
            definition=None if payload is None else payload.definition,
            definition_source=None if payload is None else payload.definition_source,
        )
        return AddAsccToAccResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "A referenced resource was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to modify this ACC.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the parameters and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't add the ASCC to the ACC.", "cause": str(e)},
        )


@router.post(
    "/acc/{acc_manifest_id}/bcc/{bccp_manifest_id}",
    status_code=status.HTTP_201_CREATED,
    summary="Add BCC to ACC",
    description="Add a BCC (Basic Core Component) to an ACC sequence. If the BCCP is already present, use `Reorder BCC in ACC` instead.",
    response_model=AddBccToAccResponse,
)
async def add_bcc_to_acc(
    acc_manifest_id: AccManifestId = Path(..., ge=1, description="ACC (Aggregate Core Component) manifest ID."),
    bccp_manifest_id: BccpManifestId = Path(
        ...,
        ge=1,
        description=(
            "BCCP (Basic Core Component Property) manifest ID to add. If the BCCP is in the same library as the "
            "ACC, it must be from the ACC release. If it is in a different library, its release must be one of "
            "the ACC release dependencies."
        ),
    ),
    *,
    index: int = Query(
        default=-1,
        description="Zero-based insertion index. Defaults to -1, which places the BCC at the end of the ACC sequence.",
    ),
    payload: AddBccToAccRequest | None = Body(
        default=None,
        description="Optional relationship fields to set as part of the initial add operation.",
    ),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> AddBccToAccResponse:
    """Add a BCC relationship and return its manifest ID."""
    try:
        result = await core_component_service.add_bcc_to_acc(
            acc_manifest_id=acc_manifest_id,
            bccp_manifest_id=bccp_manifest_id,
            index=index,
            cardinality_min=None if payload is None else payload.cardinality_min,
            cardinality_max=None if payload is None else payload.cardinality_max,
            entity_type=None if payload is None or payload.entity_type is None else _map_bcc_entity_type_update(payload.entity_type),
            is_nillable=None if payload is None else payload.is_nillable,
            default_value=None if payload is None or payload.value_constraint is None else payload.value_constraint.default_value,
            fixed_value=None if payload is None or payload.value_constraint is None else payload.value_constraint.fixed_value,
            definition=None if payload is None else payload.definition,
            definition_source=None if payload is None else payload.definition_source,
        )
        return AddBccToAccResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "A referenced resource was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to modify this ACC.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the parameters and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't add the BCC to the ACC.", "cause": str(e)},
        )


@router.post(
    "/ascc/{ascc_manifest_id}/move",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Reorder ASCC in ACC",
    description="Reorder an existing ASCC (Association Core Component) within an ACC sequence. Exactly one positioning option may be provided: `index`, `after_ascc_manifest_id`, `after_bcc_manifest_id`, `before_ascc_manifest_id`, or `before_bcc_manifest_id`.",
)
async def reorder_ascc_in_acc(
    payload: MoveAsccRequest = Body(...),
    ascc_manifest_id: AsccManifestId = Path(..., ge=1, description="ASCC manifest ID to reorder."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Reorder an existing ASCC relationship."""
    try:
        await core_component_service.reorder_ascc_in_acc(
            ascc_manifest_id=ascc_manifest_id,
            index=payload.index,
            after_ascc_manifest_id=payload.after_ascc_manifest_id,
            after_bcc_manifest_id=payload.after_bcc_manifest_id,
            before_ascc_manifest_id=payload.before_ascc_manifest_id,
            before_bcc_manifest_id=payload.before_bcc_manifest_id,
        )
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "A referenced resource was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to modify this ACC.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't reorder the ASCC.", "cause": str(e)},
        )


@router.post(
    "/bcc/{bcc_manifest_id}/move",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Reorder BCC in ACC",
    description="Reorder an existing BCC (Basic Core Component) within an ACC sequence. Exactly one positioning option may be provided: `index`, `after_ascc_manifest_id`, `after_bcc_manifest_id`, `before_ascc_manifest_id`, or `before_bcc_manifest_id`.",
)
async def reorder_bcc_in_acc(
    payload: MoveBccRequest = Body(...),
    bcc_manifest_id: BccManifestId = Path(..., ge=1, description="BCC manifest ID to reorder."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Reorder an existing BCC relationship."""
    try:
        await core_component_service.reorder_bcc_in_acc(
            bcc_manifest_id=bcc_manifest_id,
            index=payload.index,
            after_ascc_manifest_id=payload.after_ascc_manifest_id,
            after_bcc_manifest_id=payload.after_bcc_manifest_id,
            before_ascc_manifest_id=payload.before_ascc_manifest_id,
            before_bcc_manifest_id=payload.before_bcc_manifest_id,
        )
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "A referenced resource was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to modify this ACC.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't reorder the BCC.", "cause": str(e)},
        )


@router.delete(
    "/ascc/{ascc_manifest_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Remove ASCC",
    description="Remove an existing ASCC (Association Core Component) relationship from its owning ACC.",
)
async def remove_ascc(
    ascc_manifest_id: AsccManifestId = Path(..., ge=1, description="ASCC manifest ID to remove."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Remove an existing ASCC relationship."""
    try:
        await core_component_service.remove_ascc(ascc_manifest_id=ascc_manifest_id)
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The ASCC was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to modify this ACC.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the ASCC and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't remove the ASCC.", "cause": str(e)},
        )


@router.delete(
    "/bcc/{bcc_manifest_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Remove BCC",
    description="Remove an existing BCC (Basic Core Component) relationship from its owning ACC.",
)
async def remove_bcc(
    bcc_manifest_id: BccManifestId = Path(..., ge=1, description="BCC manifest ID to remove."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Remove an existing BCC relationship."""
    try:
        await core_component_service.remove_bcc(bcc_manifest_id=bcc_manifest_id)
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The BCC was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to modify this ACC.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the BCC and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't remove the BCC.", "cause": str(e)},
        )


@router.post(
    "/ascc/{ascc_manifest_id}",
    summary="Update ASCC",
    description="Update selected mutable fields of an ASCC (Association Core Component).",
    response_model=UpdateAsccResponse,
)
async def update_ascc(
    payload: UpdateAsccRequest = Body(...),
    ascc_manifest_id: AsccManifestId = Path(..., ge=1, description="ASCC manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> UpdateAsccResponse:
    """Update mutable ASCC fields and return the changed field names."""
    updates_payload = payload.model_dump(exclude_unset=True)
    try:
        result = await core_component_service.update_ascc(
            ascc_manifest_id=ascc_manifest_id,
            cardinality_min=updates_payload.get("cardinality_min", UNSET),
            cardinality_max=updates_payload.get("cardinality_max", UNSET),
            definition=updates_payload.get("definition", UNSET),
            definition_source=updates_payload.get("definition_source", UNSET),
            deprecated=updates_payload.get("deprecated", UNSET),
        )
        return UpdateAsccResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The ASCC was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to update this ASCC.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't update the ASCC.", "cause": str(e)},
        )


@router.post(
    "/bcc/{bcc_manifest_id}",
    summary="Update BCC",
    description="Update selected mutable fields of a BCC (Basic Core Component).",
    response_model=UpdateBccResponse,
)
async def update_bcc(
    payload: UpdateBccRequest = Body(...),
    bcc_manifest_id: BccManifestId = Path(..., ge=1, description="BCC manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> UpdateBccResponse:
    """Update mutable BCC fields and return the changed field names."""
    updates_payload = payload.model_dump(exclude_unset=True)
    try:
        result = await core_component_service.update_bcc(
            bcc_manifest_id=bcc_manifest_id,
            entity_type=(
                _map_bcc_entity_type_update(payload.entity_type)
                if "entity_type" in payload.model_fields_set and payload.entity_type is not None
                else UNSET
            ),
            cardinality_min=updates_payload.get("cardinality_min", UNSET),
            cardinality_max=updates_payload.get("cardinality_max", UNSET),
            definition=updates_payload.get("definition", UNSET),
            definition_source=updates_payload.get("definition_source", UNSET),
            deprecated=updates_payload.get("deprecated", UNSET),
            is_nillable=updates_payload.get("is_nillable", UNSET),
            default_value=(
                None
                if "value_constraint" in payload.model_fields_set and payload.value_constraint is None
                else (
                    payload.value_constraint.default_value
                    if "value_constraint" in payload.model_fields_set and payload.value_constraint is not None
                    else UNSET
                )
            ),
            fixed_value=(
                None
                if "value_constraint" in payload.model_fields_set and payload.value_constraint is None
                else (
                    payload.value_constraint.fixed_value
                    if "value_constraint" in payload.model_fields_set and payload.value_constraint is not None
                    else UNSET
                )
            ),
        )
        return UpdateBccResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The BCC was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to update this BCC.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't update the BCC.", "cause": str(e)},
        )


@router.post(
    "/acc/{acc_manifest_id}",
    summary="Update ACC",
    description="Update selected mutable fields of an ACC (Aggregate Core Component).",
    response_model=UpdateAccResponse,
)
async def update_acc(
    payload: UpdateAccRequest = Body(...),
    acc_manifest_id: AccManifestId = Path(..., ge=1, description="ACC (Aggregate Core Component) manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> UpdateAccResponse:
    """Update mutable ACC fields and return the changed field names."""
    updates_payload = payload.model_dump(exclude_unset=True)
    try:
        result = await core_component_service.update_acc(
            acc_manifest_id=acc_manifest_id,
            object_class_term=updates_payload.get("object_class_term", UNSET),
            component_type=updates_payload.get("component_type", UNSET),
            definition=updates_payload.get("definition", UNSET),
            definition_source=updates_payload.get("definition_source", UNSET),
            is_abstract=updates_payload.get("is_abstract", UNSET),
            deprecated=updates_payload.get("deprecated", UNSET),
            based_acc_manifest_id=updates_payload.get("based_acc_manifest_id", UNSET),
            namespace_id=updates_payload.get("namespace_id", UNSET),
        )
        return UpdateAccResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The ACC was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to update this ACC.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't update the ACC.", "cause": str(e)},
        )


@router.post(
    "/acc/{acc_manifest_id}/ownership",
    summary="Transfer ACC ownership",
    description="Transfer ownership of an ACC (Aggregate Core Component) to another user while the ACC is in `WIP`.",
    response_model=TransferAccOwnershipResponse,
)
async def transfer_acc_ownership(
    payload: TransferOwnershipRequest = Body(...),
    acc_manifest_id: AccManifestId = Path(..., ge=1, description="ACC (Aggregate Core Component) manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> TransferAccOwnershipResponse:
    """Transfer ACC ownership and return the changed field names."""
    try:
        result = await core_component_service.transfer_acc_ownership(
            acc_manifest_id=acc_manifest_id,
            target_user_id=payload.target_user_id,
        )
        return TransferAccOwnershipResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The ACC or target user was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to transfer this ACC.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't transfer ACC ownership.", "cause": str(e)},
        )


@router.post(
    "/acc/{acc_manifest_id}/tags",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Add tags to ACC",
    description="Attach one or more tags to an ACC (Aggregate Core Component) while the ACC is in `WIP`.",
)
async def add_acc_tags(
    acc_manifest_id: AccManifestId = Path(..., ge=1, description="ACC (Aggregate Core Component) manifest ID."),
    tag_id: list[int] | None = Query(
        default=None,
        description="Tag identifier query parameter. Repeat `tag_id` to pass multiple values.",
    ),
    tag_id_list: list[int] | None = Query(
        default=None,
        description="Alternative tag identifier query parameter. Repeat `tag_id_list` to pass multiple values.",
    ),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Attach tags to a WIP ACC."""
    try:
        if tag_id is not None and tag_id_list is not None:
            raise ValueError("Use either `tag_id` or `tag_id_list`, but not both.")
        effective_tag_ids = tag_id if tag_id is not None else tag_id_list
        if not effective_tag_ids:
            raise ValueError("At least one tag identifier is required.")
        await core_component_service.add_acc_tags(
            acc_manifest_id=acc_manifest_id,
            tag_id=effective_tag_ids,
        )
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The ACC or a requested tag was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to update this ACC.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't add ACC tags.", "cause": str(e)},
        )


@router.delete(
    "/acc/{acc_manifest_id}/tags",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Remove tags from ACC",
    description="Detach one or more tags from an ACC (Aggregate Core Component) while the ACC is in `WIP`.",
)
async def remove_acc_tags(
    acc_manifest_id: AccManifestId = Path(..., ge=1, description="ACC (Aggregate Core Component) manifest ID."),
    tag_id: list[int] | None = Query(
        default=None,
        description="Tag identifier query parameter. Repeat `tag_id` to pass multiple values.",
    ),
    tag_id_list: list[int] | None = Query(
        default=None,
        description="Alternative tag identifier query parameter. Repeat `tag_id_list` to pass multiple values.",
    ),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Detach tags from a WIP ACC."""
    try:
        if tag_id is not None and tag_id_list is not None:
            raise ValueError("Use either `tag_id` or `tag_id_list`, but not both.")
        effective_tag_ids = tag_id if tag_id is not None else tag_id_list
        if not effective_tag_ids:
            raise ValueError("At least one tag identifier is required.")
        await core_component_service.remove_acc_tags(
            acc_manifest_id=acc_manifest_id,
            tag_id=effective_tag_ids,
        )
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The ACC or a requested tag was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to update this ACC.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't remove ACC tags.", "cause": str(e)},
        )


@router.post(
    "/acc/{acc_manifest_id}/state",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Change ACC state",
    description=(
        "Change the lifecycle state of an ACC (Aggregate Core Component). "
        "For `Working` releases, the allowed path is `Deleted <-> WIP <-> Draft <-> Candidate`. "
        "For non-`Working` releases, the allowed path is `Deleted <-> WIP <-> QA <-> Production`."
    ),
)
async def change_acc_state(
    payload: UpdateAccStateRequest = Body(...),
    acc_manifest_id: AccManifestId = Path(..., ge=1, description="ACC (Aggregate Core Component) manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Change ACC lifecycle state."""
    try:
        await core_component_service.change_acc_state(
            acc_manifest_id=acc_manifest_id,
            state=payload.state,
        )
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The ACC was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to update this ACC state.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't change the ACC state.", "cause": str(e)},
        )


@router.post(
    "/acc/{acc_manifest_id}/revise",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Revise ACC",
    description=(
        "Create a new editable ACC revision from a stable ACC. "
        "Developer-side ACCs can be revised only from `Published`, and end-user ACCs only from `Production`."
    ),
)
async def revise_acc(
    acc_manifest_id: AccManifestId = Path(..., ge=1, description="ACC (Aggregate Core Component) manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Create a revised ACC working copy."""
    try:
        await core_component_service.revise_acc(acc_manifest_id=acc_manifest_id)
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The ACC was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to revise this ACC.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the ACC and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't revise the ACC.", "cause": str(e)},
        )


@router.post(
    "/acc/{acc_manifest_id}/cancel",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Cancel ACC",
    description="Cancel the current ACC revision and restore the previous stable ACC revision.",
)
async def cancel_acc(
    acc_manifest_id: AccManifestId = Path(..., ge=1, description="ACC (Aggregate Core Component) manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Cancel the active ACC revision."""
    try:
        await core_component_service.cancel_acc(acc_manifest_id=acc_manifest_id)
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The ACC was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to cancel this ACC revision.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the ACC and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't cancel the ACC revision.", "cause": str(e)},
        )


@router.delete(
    "/acc/{acc_manifest_id}",
    summary="Discard ACC",
    description=(
        "Discard an ACC (Aggregate Core Component) and its direct ACC-owned records permanently. "
        "The ACC must already be in the `Deleted` state."
    ),
    status_code=status.HTTP_204_NO_CONTENT,
)
async def discard_acc(
    acc_manifest_id: AccManifestId = Path(..., ge=1, description="ACC (Aggregate Core Component) manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Discard a Deleted ACC and return no content."""
    try:
        await core_component_service.discard_acc(acc_manifest_id=acc_manifest_id)
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The ACC was not found.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the path and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't discard the ACC.", "cause": str(e)},
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


@router.post(
    "/asccp",
    status_code=status.HTTP_201_CREATED,
    summary="Create ASCCP",
    description="Create an ASCCP (Association Core Component Property).",
    response_model=CreateAsccpResponse,
)
async def create_asccp(
    payload: CreateAsccpRequest = Body(...),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> CreateAsccpResponse:
    """Create an ASCCP and return its manifest ID."""
    result = await core_component_service.create_asccp(**payload.model_dump())
    return CreateAsccpResponse.model_validate(result, from_attributes=True)


@router.post(
    "/asccp/{asccp_manifest_id}",
    summary="Update ASCCP",
    description="Update selected mutable fields of an ASCCP (Association Core Component Property).",
    response_model=UpdateAsccpResponse,
)
async def update_asccp(
    payload: UpdateAsccpRequest = Body(...),
    asccp_manifest_id: AsccpManifestId = Path(..., ge=1, description="ASCCP manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> UpdateAsccpResponse:
    """Update mutable ASCCP fields and return the changed field names."""
    result = await core_component_service.update_asccp(
        asccp_manifest_id=asccp_manifest_id,
        property_term=payload.model_dump(exclude_unset=True).get("property_term", UNSET),
        definition=payload.model_dump(exclude_unset=True).get("definition", UNSET),
        definition_source=payload.model_dump(exclude_unset=True).get("definition_source", UNSET),
        reusable_indicator=payload.model_dump(exclude_unset=True).get("reusable_indicator", UNSET),
        deprecated=payload.model_dump(exclude_unset=True).get("deprecated", UNSET),
        is_nillable=payload.model_dump(exclude_unset=True).get("is_nillable", UNSET),
        namespace_id=payload.model_dump(exclude_unset=True).get("namespace_id", UNSET),
    )
    return UpdateAsccpResponse.model_validate(result, from_attributes=True)


@router.post(
    "/asccp/{asccp_manifest_id}/ownership",
    summary="Transfer ASCCP ownership",
    description="Transfer ownership of an ASCCP (Association Core Component Property) to another user while the ASCCP is in `WIP`.",
    response_model=TransferAsccpOwnershipResponse,
)
async def transfer_asccp_ownership(
    payload: TransferOwnershipRequest = Body(...),
    asccp_manifest_id: AsccpManifestId = Path(..., ge=1, description="ASCCP manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> TransferAsccpOwnershipResponse:
    """Transfer ASCCP ownership and return the changed field names."""
    try:
        result = await core_component_service.transfer_asccp_ownership(
            asccp_manifest_id=asccp_manifest_id,
            target_user_id=payload.target_user_id,
        )
        return TransferAsccpOwnershipResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The ASCCP or target user was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to transfer this ASCCP.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't transfer ASCCP ownership.", "cause": str(e)},
        )


@router.post(
    "/asccp/{asccp_manifest_id}/state",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Change ASCCP state",
    description="Change the lifecycle state of an ASCCP (Association Core Component Property).",
)
async def change_asccp_state(
    payload: UpdateAsccpStateRequest = Body(...),
    asccp_manifest_id: AsccpManifestId = Path(..., ge=1, description="ASCCP manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Change ASCCP lifecycle state."""
    await core_component_service.change_asccp_state(asccp_manifest_id=asccp_manifest_id, state=payload.state)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.post(
    "/asccp/{asccp_manifest_id}/acc",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Change role ACC of ASCCP",
    description="Change the role ACC (Aggregate Core Component) of an ASCCP.",
)
async def change_asccp_role_of_acc(
    asccp_manifest_id: AsccpManifestId = Path(..., ge=1, description="ASCCP manifest ID."),
    acc_manifest_id: AccManifestId = Query(
        ...,
        ge=1,
        description=(
            "Role ACC manifest identifier. If the ACC is in the same library as the ASCCP, it must be from the "
            "ASCCP release. If it is in a different library, its release must be one of the ASCCP release "
            "dependencies."
        ),
    ),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Change the role ACC of an ASCCP."""
    await core_component_service.change_asccp_role_of_acc(
        asccp_manifest_id=asccp_manifest_id,
        role_of_acc_manifest_id=acc_manifest_id,
    )
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.post(
    "/asccp/{asccp_manifest_id}/tags",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Add tags to ASCCP",
    description="Attach one or more tags to an ASCCP while it is in `WIP`.",
)
async def add_asccp_tags(
    asccp_manifest_id: AsccpManifestId = Path(..., ge=1, description="ASCCP manifest ID."),
    tag_id: list[int] | None = Query(default=None, description="Repeat `tag_id` to pass multiple values."),
    tag_id_list: list[int] | None = Query(default=None, description="Alternative repeated tag query parameter."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Attach tags to a WIP ASCCP."""
    if tag_id is not None and tag_id_list is not None:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail={"message": "The request is invalid. Check the query and try again.", "cause": "Use either `tag_id` or `tag_id_list`, but not both."})
    effective_tag_ids = tag_id if tag_id is not None else tag_id_list
    if not effective_tag_ids:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail={"message": "The request is invalid. Check the query and try again.", "cause": "At least one tag identifier is required."})
    await core_component_service.add_asccp_tags(asccp_manifest_id=asccp_manifest_id, tag_id=effective_tag_ids)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.delete(
    "/asccp/{asccp_manifest_id}/tags",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Remove tags from ASCCP",
    description="Detach one or more tags from an ASCCP while it is in `WIP`.",
)
async def remove_asccp_tags(
    asccp_manifest_id: AsccpManifestId = Path(..., ge=1, description="ASCCP manifest ID."),
    tag_id: list[int] | None = Query(default=None, description="Repeat `tag_id` to pass multiple values."),
    tag_id_list: list[int] | None = Query(default=None, description="Alternative repeated tag query parameter."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Remove tags from a WIP ASCCP."""
    if tag_id is not None and tag_id_list is not None:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail={"message": "The request is invalid. Check the query and try again.", "cause": "Use either `tag_id` or `tag_id_list`, but not both."})
    effective_tag_ids = tag_id if tag_id is not None else tag_id_list
    if not effective_tag_ids:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail={"message": "The request is invalid. Check the query and try again.", "cause": "At least one tag identifier is required."})
    await core_component_service.remove_asccp_tags(asccp_manifest_id=asccp_manifest_id, tag_id=effective_tag_ids)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.post(
    "/asccp/{asccp_manifest_id}/revise",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Revise ASCCP",
    description="Create a new editable ASCCP revision from a stable ASCCP.",
)
async def revise_asccp(
    asccp_manifest_id: AsccpManifestId = Path(..., ge=1, description="ASCCP manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Create a revised ASCCP working copy."""
    await core_component_service.revise_asccp(asccp_manifest_id=asccp_manifest_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.post(
    "/asccp/{asccp_manifest_id}/cancel",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Cancel ASCCP",
    description="Cancel the current ASCCP revision and restore the previous stable ASCCP revision.",
)
async def cancel_asccp(
    asccp_manifest_id: AsccpManifestId = Path(..., ge=1, description="ASCCP manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Cancel the active ASCCP revision."""
    await core_component_service.cancel_asccp(asccp_manifest_id=asccp_manifest_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.delete(
    "/asccp/{asccp_manifest_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Discard ASCCP",
    description="Discard an ASCCP and its direct records permanently. The ASCCP must already be in the `Deleted` state.",
)
async def discard_asccp(
    asccp_manifest_id: AsccpManifestId = Path(..., ge=1, description="ASCCP manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Discard a Deleted ASCCP."""
    await core_component_service.discard_asccp(asccp_manifest_id=asccp_manifest_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.post(
    "/bccp",
    status_code=status.HTTP_201_CREATED,
    summary="Create BCCP",
    description="Create a BCCP (Basic Core Component Property).",
    response_model=CreateBccpResponse,
)
async def create_bccp(
    payload: CreateBccpRequest = Body(...),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> CreateBccpResponse:
    """Create a BCCP and return its manifest ID."""
    result = await core_component_service.create_bccp(**payload.model_dump())
    return CreateBccpResponse.model_validate(result, from_attributes=True)


@router.post(
    "/bccp/{bccp_manifest_id}",
    summary="Update BCCP",
    description="Update selected mutable fields of a BCCP (Basic Core Component Property).",
    response_model=UpdateBccpResponse,
)
async def update_bccp(
    payload: UpdateBccpRequest = Body(...),
    bccp_manifest_id: BccpManifestId = Path(..., ge=1, description="BCCP manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> UpdateBccpResponse:
    """Update mutable BCCP fields and return the changed field names."""
    updates_payload = payload.model_dump(exclude_unset=True)
    result = await core_component_service.update_bccp(
        bccp_manifest_id=bccp_manifest_id,
        property_term=updates_payload.get("property_term", UNSET),
        definition=updates_payload.get("definition", UNSET),
        definition_source=updates_payload.get("definition_source", UNSET),
        deprecated=updates_payload.get("deprecated", UNSET),
        is_nillable=updates_payload.get("is_nillable", UNSET),
        namespace_id=updates_payload.get("namespace_id", UNSET),
        default_value=updates_payload.get("default_value", UNSET),
        fixed_value=updates_payload.get("fixed_value", UNSET),
    )
    return UpdateBccpResponse.model_validate(result, from_attributes=True)


@router.post(
    "/bccp/{bccp_manifest_id}/ownership",
    summary="Transfer BCCP ownership",
    description="Transfer ownership of a BCCP (Basic Core Component Property) to another user while the BCCP is in `WIP`.",
    response_model=TransferBccpOwnershipResponse,
)
async def transfer_bccp_ownership(
    payload: TransferOwnershipRequest = Body(...),
    bccp_manifest_id: BccpManifestId = Path(..., ge=1, description="BCCP manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> TransferBccpOwnershipResponse:
    """Transfer BCCP ownership and return the changed field names."""
    try:
        result = await core_component_service.transfer_bccp_ownership(
            bccp_manifest_id=bccp_manifest_id,
            target_user_id=payload.target_user_id,
        )
        return TransferBccpOwnershipResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The BCCP or target user was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to transfer this BCCP.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't transfer BCCP ownership.", "cause": str(e)},
        )


@router.post(
    "/bccp/{bccp_manifest_id}/state",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Change BCCP state",
    description="Change the lifecycle state of a BCCP (Basic Core Component Property).",
)
async def change_bccp_state(
    payload: UpdateBccpStateRequest = Body(...),
    bccp_manifest_id: BccpManifestId = Path(..., ge=1, description="BCCP manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Change BCCP lifecycle state."""
    await core_component_service.change_bccp_state(bccp_manifest_id=bccp_manifest_id, state=payload.state)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.post(
    "/bccp/{bccp_manifest_id}/bdt",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Change BDT of BCCP",
    description="Change the BDT (Business Data Type) of a BCCP.",
)
async def change_bccp_bdt(
    bccp_manifest_id: BccpManifestId = Path(..., ge=1, description="BCCP manifest ID."),
    dt_manifest_id: DataTypeManifestId = Query(
        ...,
        ge=1,
        description=(
            "Target BDT manifest identifier. The selected data type must already be a BDT, "
            "which means its base DT link is set. If the BDT is in the same library as the BCCP, it must be from "
            "the BCCP release. If it is in a different library, its release must be one of the BCCP release "
            "dependencies."
        ),
    ),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Change the BDT of a BCCP."""
    await core_component_service.change_bccp_bdt(
        bccp_manifest_id=bccp_manifest_id,
        bdt_manifest_id=dt_manifest_id,
    )
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.post(
    "/bccp/{bccp_manifest_id}/tags",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Add tags to BCCP",
    description="Attach one or more tags to a BCCP while it is in `WIP`.",
)
async def add_bccp_tags(
    bccp_manifest_id: BccpManifestId = Path(..., ge=1, description="BCCP manifest ID."),
    tag_id: list[int] | None = Query(default=None, description="Repeat `tag_id` to pass multiple values."),
    tag_id_list: list[int] | None = Query(default=None, description="Alternative repeated tag query parameter."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Attach tags to a WIP BCCP."""
    if tag_id is not None and tag_id_list is not None:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail={"message": "The request is invalid. Check the query and try again.", "cause": "Use either `tag_id` or `tag_id_list`, but not both."})
    effective_tag_ids = tag_id if tag_id is not None else tag_id_list
    if not effective_tag_ids:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail={"message": "The request is invalid. Check the query and try again.", "cause": "At least one tag identifier is required."})
    await core_component_service.add_bccp_tags(bccp_manifest_id=bccp_manifest_id, tag_id=effective_tag_ids)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.delete(
    "/bccp/{bccp_manifest_id}/tags",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Remove tags from BCCP",
    description="Detach one or more tags from a BCCP while it is in `WIP`.",
)
async def remove_bccp_tags(
    bccp_manifest_id: BccpManifestId = Path(..., ge=1, description="BCCP manifest ID."),
    tag_id: list[int] | None = Query(default=None, description="Repeat `tag_id` to pass multiple values."),
    tag_id_list: list[int] | None = Query(default=None, description="Alternative repeated tag query parameter."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Remove tags from a WIP BCCP."""
    if tag_id is not None and tag_id_list is not None:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail={"message": "The request is invalid. Check the query and try again.", "cause": "Use either `tag_id` or `tag_id_list`, but not both."})
    effective_tag_ids = tag_id if tag_id is not None else tag_id_list
    if not effective_tag_ids:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail={"message": "The request is invalid. Check the query and try again.", "cause": "At least one tag identifier is required."})
    await core_component_service.remove_bccp_tags(bccp_manifest_id=bccp_manifest_id, tag_id=effective_tag_ids)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.post(
    "/bccp/{bccp_manifest_id}/revise",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Revise BCCP",
    description="Create a new editable BCCP revision from a stable BCCP.",
)
async def revise_bccp(
    bccp_manifest_id: BccpManifestId = Path(..., ge=1, description="BCCP manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Create a revised BCCP working copy."""
    await core_component_service.revise_bccp(bccp_manifest_id=bccp_manifest_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.post(
    "/bccp/{bccp_manifest_id}/cancel",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Cancel BCCP",
    description="Cancel the current BCCP revision and restore the previous stable BCCP revision.",
)
async def cancel_bccp(
    bccp_manifest_id: BccpManifestId = Path(..., ge=1, description="BCCP manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Cancel the active BCCP revision."""
    await core_component_service.cancel_bccp(bccp_manifest_id=bccp_manifest_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.delete(
    "/bccp/{bccp_manifest_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Discard BCCP",
    description="Discard a BCCP and its direct records permanently. The BCCP must already be in the `Deleted` state.",
)
async def discard_bccp(
    bccp_manifest_id: BccpManifestId = Path(..., ge=1, description="BCCP manifest ID."),
    core_component_service: CoreComponentService = Depends(get_core_component_service),
) -> Response:
    """Discard a Deleted BCCP."""
    await core_component_service.discard_bccp(bccp_manifest_id=bccp_manifest_id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


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
