"""Data Type API routes.

Provides endpoints for listing and retrieving data types plus lifecycle and tag
commands for manifest-scoped DT authoring.
"""


from __future__ import annotations

from typing import Literal

from fastapi import APIRouter, Body, Depends, HTTPException, Path, Query, Response, status

from app.deps import get_data_type_service
from app.routes.models.data_type import (
    CreateDataTypeRequest,
    CreateDataTypeResponse,
    CreateDataTypeSupplementaryComponentRequest,
    CreateDataTypeSupplementaryComponentResponse,
    DataTypeEntry,
    GetDataTypeByDataTypeManifestIdResponse,
    GetDataTypeListResponse,
    TransferDataTypeOwnershipRequest,
    TransferDataTypeOwnershipResponse,
    UpdateDataTypeRequest,
    UpdateDataTypeResponse,
    UpdateDataTypeSupplementaryComponentRequest,
    UpdateDataTypeSupplementaryComponentResponse,
    UpdateDataTypeStateRequest,
)
from app.utils.date import parse_date_range
from app.services.data_type_service import DataTypeService
from app.services.models.data_type import DataTypePrimitiveServiceRecord
from app.types.unset import UNSET
from app.types.identifiers import DataTypeManifestId, DataTypeSupplementaryComponentManifestId
from app.types.identifiers import ReleaseId

router = APIRouter(prefix="/data-types", tags=["data-type"])


@router.get(
    "",
    summary="List data types",
    description="Retrieve a paginated list of data types.",
    response_model=GetDataTypeListResponse,
)
async def get_data_type_list(
    release_id: ReleaseId = Query(..., ge=1, description="Filter by release ID."),
    den: str | None = Query(default=None, description="Filter by DEN (partial match)."),
    representation_term: str | None = Query(default=None, description="Filter by representation term (partial match)."),
    created_on: str | None = Query(default=None, description="Filter by creation date range '[before~after]'."),
    last_updated_on: str | None = Query(default=None, description="Filter by last update date range '[before~after]'."),
    order_by: str | None = Query(
        default=None,
        description=(
            "Comma-separated list of properties to order by. Prefix with '-' for descending, '+' for ascending. "
            "Allowed columns: den, data_type_term, qualifier, representation_term, six_digit_id, definition, "
            "creation_timestamp, last_update_timestamp."
        ),
    ),
    offset: int = Query(default=0, ge=0, description="The offset from the beginning of the list."),
    limit: int = Query(default=10, ge=1, le=100, description="The maximum number of items to return."),
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> GetDataTypeListResponse:
    """Return a paginated list of data types.

    Args:
        release_id: Release identifier used to scope the query.
        den: Optional Dictionary Entry Name (DEN) filter.
        representation_term: Value for `representation_term`.
        created_on: Optional creation-time filter in ISO-8601 range form.
        last_updated_on: Optional last-update-time filter in ISO-8601 range form.
        order_by: Sort expression used for ordering the result set.
        offset: Zero-based index of the first item to include in the page.
        limit: Maximum number of items to return.
        data_type_service: Data-type service dependency.

    Returns:
        Paginated response containing matching resources.
    """
    try:
        created_range = parse_date_range(created_on)
        updated_range = parse_date_range(last_updated_on)
        page = await data_type_service.list(
            release_id=release_id,
            limit=limit,
            offset=offset,
            order_by=order_by,
            den=den,
            representation_term=representation_term,
            created_on=created_range,
            last_updated_on=updated_range,
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the parameters and try again.", "cause": str(e)},
        )

    return GetDataTypeListResponse(
        items=[DataTypeEntry.model_validate(item, from_attributes=True) for item in page.items],
        total_items=page.total,
        limit=limit,
        offset=offset,
    )


@router.post(
    "",
    status_code=status.HTTP_201_CREATED,
    summary="Create DT",
    description=(
        "Create a DT (Data Type) from an existing base DT in a release allowed for your role. "
        "Optional mutable DT fields can be supplied and applied during creation."
    ),
    response_model=CreateDataTypeResponse,
)
async def create_data_type(
    payload: CreateDataTypeRequest = Body(...),
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> CreateDataTypeResponse:
    """Create a DT and return its manifest ID."""
    try:
        result = await data_type_service.create_dt(
            release_id=payload.release_id,
            based_dt_manifest_id=payload.based_dt_manifest_id,
            tag_id=payload.tag_id,
            qualifier=payload.qualifier if "qualifier" in payload.model_fields_set else UNSET,
            six_digit_id=payload.six_digit_id if "six_digit_id" in payload.model_fields_set else UNSET,
            deprecated=payload.deprecated if "deprecated" in payload.model_fields_set else UNSET,
            namespace_id=payload.namespace_id if "namespace_id" in payload.model_fields_set else UNSET,
            content_component_definition=(
                payload.content_component_definition
                if "content_component_definition" in payload.model_fields_set
                else UNSET
            ),
            definition=payload.definition if "definition" in payload.model_fields_set else UNSET,
            definition_source=payload.definition_source if "definition_source" in payload.model_fields_set else UNSET,
            xbt_manifest_id=(
                payload.default_primitive.xbt_manifest_id
                if "default_primitive" in payload.model_fields_set and payload.default_primitive is not None
                else UNSET
            ),
            code_list_manifest_id=(
                payload.default_primitive.code_list_manifest_id
                if "default_primitive" in payload.model_fields_set and payload.default_primitive is not None
                else UNSET
            ),
            agency_id_list_manifest_id=(
                payload.default_primitive.agency_id_list_manifest_id
                if "default_primitive" in payload.model_fields_set and payload.default_primitive is not None
                else UNSET
            ),
            add_primitives=(
                [
                    DataTypePrimitiveServiceRecord(**primitive.model_dump(), is_default=False)
                    for primitive in payload.add_primitives
                ]
                if "add_primitives" in payload.model_fields_set and payload.add_primitives is not None
                else UNSET
            ),
            remove_primitives=(
                [
                    DataTypePrimitiveServiceRecord(**primitive.model_dump(), is_default=False)
                    for primitive in payload.remove_primitives
                ]
                if "remove_primitives" in payload.model_fields_set and payload.remove_primitives is not None
                else UNSET
            ),
        )
        return CreateDataTypeResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "A referenced resource was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to create DTs.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't create the DT.", "cause": str(e)},
        )


@router.get(
    "/{dt_manifest_id}",
    summary="Retrieve a data type",
    description="Retrieve a data type by manifest ID.",
    response_model=GetDataTypeByDataTypeManifestIdResponse,
)
async def get_data_type_by_data_type_manifest_id(
    dt_manifest_id: DataTypeManifestId = Path(..., ge=1, description="Data type manifest ID."),
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> GetDataTypeByDataTypeManifestIdResponse:
    """Return a data type by manifest ID.

    Args:
        dt_manifest_id: Data type manifest identifier.
        data_type_service: Data-type service dependency.

    Returns:
        Response payload for the requested resource.
    """
    row = await data_type_service.get(dt_manifest_id)
    if row is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "message": "The data type was not found.",
                "cause": f"No data type exists with manifest ID {int(dt_manifest_id)}.",
            },
        )
    return GetDataTypeByDataTypeManifestIdResponse.model_validate(row, from_attributes=True)


@router.post(
    "/{dt_manifest_id}",
    summary="Update DT",
    description="Update mutable DT fields while the DT is in `WIP`.",
    response_model=UpdateDataTypeResponse,
)
async def update_data_type(
    payload: UpdateDataTypeRequest = Body(...),
    dt_manifest_id: DataTypeManifestId = Path(..., ge=1, description="Data type manifest ID."),
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> UpdateDataTypeResponse:
    """Update mutable DT fields."""
    try:
        result = await data_type_service.update_dt(
            dt_manifest_id=dt_manifest_id,
            based_dt_manifest_id=payload.based_dt_manifest_id if "based_dt_manifest_id" in payload.model_fields_set else UNSET,
            qualifier=payload.qualifier if "qualifier" in payload.model_fields_set else UNSET,
            six_digit_id=payload.six_digit_id if "six_digit_id" in payload.model_fields_set else UNSET,
            deprecated=payload.deprecated if "deprecated" in payload.model_fields_set else UNSET,
            namespace_id=payload.namespace_id if "namespace_id" in payload.model_fields_set else UNSET,
            content_component_definition=(
                payload.content_component_definition
                if "content_component_definition" in payload.model_fields_set
                else UNSET
            ),
            definition=payload.definition if "definition" in payload.model_fields_set else UNSET,
            definition_source=payload.definition_source if "definition_source" in payload.model_fields_set else UNSET,
            xbt_manifest_id=(
                payload.default_primitive.xbt_manifest_id
                if "default_primitive" in payload.model_fields_set and payload.default_primitive is not None
                else UNSET
            ),
            code_list_manifest_id=(
                payload.default_primitive.code_list_manifest_id
                if "default_primitive" in payload.model_fields_set and payload.default_primitive is not None
                else UNSET
            ),
            agency_id_list_manifest_id=(
                payload.default_primitive.agency_id_list_manifest_id
                if "default_primitive" in payload.model_fields_set and payload.default_primitive is not None
                else UNSET
            ),
            add_primitives=(
                [
                    DataTypePrimitiveServiceRecord(**primitive.model_dump(), is_default=False)
                    for primitive in payload.add_primitives
                ]
                if "add_primitives" in payload.model_fields_set and payload.add_primitives is not None
                else UNSET
            ),
            remove_primitives=(
                [
                    DataTypePrimitiveServiceRecord(**primitive.model_dump(), is_default=False)
                    for primitive in payload.remove_primitives
                ]
                if "remove_primitives" in payload.model_fields_set and payload.remove_primitives is not None
                else UNSET
            ),
        )
        return UpdateDataTypeResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The data type was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to update this DT.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't update the DT.", "cause": str(e)},
        )


@router.post(
    "/{dt_manifest_id}/ownership",
    summary="Transfer DT ownership",
    description="Transfer ownership of a DT (Data Type) to another user while the DT is in `WIP`.",
    response_model=TransferDataTypeOwnershipResponse,
)
async def transfer_data_type_ownership(
    payload: TransferDataTypeOwnershipRequest = Body(...),
    dt_manifest_id: DataTypeManifestId = Path(..., ge=1, description="Data type manifest ID."),
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> TransferDataTypeOwnershipResponse:
    """Transfer DT ownership and return the changed field names."""
    try:
        result = await data_type_service.transfer_dt_ownership(
            dt_manifest_id=dt_manifest_id,
            target_user_id=payload.target_user_id,
        )
        return TransferDataTypeOwnershipResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The data type or target user was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to transfer this DT.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't transfer DT ownership.", "cause": str(e)},
        )


@router.post(
    "/{dt_manifest_id}/supplementary-components",
    status_code=status.HTTP_201_CREATED,
    summary="Create DT_SC",
    description="Create a new DT supplementary component under a `WIP` DT, optionally apply mutable fields, and propagate it to inherited DTs.",
    response_model=CreateDataTypeSupplementaryComponentResponse,
)
async def create_dt_sc(
    dt_manifest_id: DataTypeManifestId = Path(..., ge=1, description="Owning data type manifest ID."),
    payload: CreateDataTypeSupplementaryComponentRequest = Body(...),
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> CreateDataTypeSupplementaryComponentResponse:
    """Create a DT_SC under a DT."""
    try:
        result = await data_type_service.create_dt_sc(
            owner_dt_manifest_id=dt_manifest_id,
            property_term=payload.property_term,
            representation_term=payload.representation_term,
            cardinality=payload.cardinality if "cardinality" in payload.model_fields_set else UNSET,
            deprecated=payload.deprecated if "deprecated" in payload.model_fields_set else UNSET,
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
            definition=payload.definition if "definition" in payload.model_fields_set else UNSET,
            definition_source=payload.definition_source if "definition_source" in payload.model_fields_set else UNSET,
            xbt_manifest_id=(
                payload.default_primitive.xbt_manifest_id
                if "default_primitive" in payload.model_fields_set and payload.default_primitive is not None
                else UNSET
            ),
            code_list_manifest_id=(
                payload.default_primitive.code_list_manifest_id
                if "default_primitive" in payload.model_fields_set and payload.default_primitive is not None
                else UNSET
            ),
            agency_id_list_manifest_id=(
                payload.default_primitive.agency_id_list_manifest_id
                if "default_primitive" in payload.model_fields_set and payload.default_primitive is not None
                else UNSET
            ),
            add_primitives=payload.add_primitives if "add_primitives" in payload.model_fields_set else UNSET,
            remove_primitives=payload.remove_primitives if "remove_primitives" in payload.model_fields_set else UNSET,
        )
        return CreateDataTypeSupplementaryComponentResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The data type was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to add supplementary components to this DT.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the DT and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't create the DT supplementary component.", "cause": str(e)},
        )


@router.post(
    "/supplementary-components/{dt_sc_manifest_id}",
    summary="Update DT_SC",
    description="Update mutable DT supplementary-component fields while the owner DT is in `WIP`.",
    response_model=UpdateDataTypeSupplementaryComponentResponse,
)
async def update_dt_sc(
    payload: UpdateDataTypeSupplementaryComponentRequest = Body(...),
    dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId = Path(
        ...,
        ge=1,
        description="Data type supplementary-component manifest ID.",
    ),
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> UpdateDataTypeSupplementaryComponentResponse:
    """Update mutable DT_SC fields."""
    try:
        result = await data_type_service.update_dt_sc(
            dt_sc_manifest_id=dt_sc_manifest_id,
            property_term=payload.property_term if "property_term" in payload.model_fields_set else UNSET,
            representation_term=(
                payload.representation_term if "representation_term" in payload.model_fields_set else UNSET
            ),
            cardinality=payload.cardinality if "cardinality" in payload.model_fields_set else UNSET,
            deprecated=payload.deprecated if "deprecated" in payload.model_fields_set else UNSET,
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
            definition=payload.definition if "definition" in payload.model_fields_set else UNSET,
            definition_source=payload.definition_source if "definition_source" in payload.model_fields_set else UNSET,
            xbt_manifest_id=(
                payload.default_primitive.xbt_manifest_id
                if "default_primitive" in payload.model_fields_set and payload.default_primitive is not None
                else UNSET
            ),
            code_list_manifest_id=(
                payload.default_primitive.code_list_manifest_id
                if "default_primitive" in payload.model_fields_set and payload.default_primitive is not None
                else UNSET
            ),
            agency_id_list_manifest_id=(
                payload.default_primitive.agency_id_list_manifest_id
                if "default_primitive" in payload.model_fields_set and payload.default_primitive is not None
                else UNSET
            ),
            add_primitives=(
                [
                    DataTypePrimitiveServiceRecord(**primitive.model_dump(), is_default=False)
                    for primitive in payload.add_primitives
                ]
                if "add_primitives" in payload.model_fields_set and payload.add_primitives is not None
                else UNSET
            ),
            remove_primitives=(
                [
                    DataTypePrimitiveServiceRecord(**primitive.model_dump(), is_default=False)
                    for primitive in payload.remove_primitives
                ]
                if "remove_primitives" in payload.model_fields_set and payload.remove_primitives is not None
                else UNSET
            ),
        )
        return UpdateDataTypeSupplementaryComponentResponse.model_validate(result, from_attributes=True)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The DT supplementary component was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to update this DT supplementary component.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't update the DT supplementary component.", "cause": str(e)},
        )


@router.delete(
    "/supplementary-components/{dt_sc_manifest_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Delete DT_SC",
    description="Delete a DT supplementary component while the owner DT is in `WIP`.",
)
async def delete_dt_sc(
    dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId = Path(
        ...,
        ge=1,
        description="Data type supplementary-component manifest ID.",
    ),
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> Response:
    """Delete a DT_SC."""
    try:
        await data_type_service.delete_dt_sc(dt_sc_manifest_id=dt_sc_manifest_id)
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The DT supplementary component was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to delete this DT supplementary component.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the DT supplementary component and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't delete the DT supplementary component.", "cause": str(e)},
        )


@router.post(
    "/{dt_manifest_id}/tags",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Add tags to DT",
    description="Attach one or more tags to a DT while it is in `WIP`.",
)
async def add_dt_tags(
    dt_manifest_id: DataTypeManifestId = Path(..., ge=1, description="Data type manifest ID."),
    tag_id: list[int] | None = Query(
        default=None,
        description="Tag identifier query parameter. Repeat `tag_id` to pass multiple values.",
    ),
    tag_id_list: list[int] | None = Query(
        default=None,
        description="Alternative tag identifier query parameter. Repeat `tag_id_list` to pass multiple values.",
    ),
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> Response:
    """Attach tags to a WIP DT."""
    try:
        if tag_id is not None and tag_id_list is not None:
            raise ValueError("Use either `tag_id` or `tag_id_list`, but not both.")
        effective_tag_ids = tag_id if tag_id is not None else tag_id_list
        if not effective_tag_ids:
            raise ValueError("At least one tag identifier is required.")
        await data_type_service.add_dt_tags(dt_manifest_id=dt_manifest_id, tag_id=effective_tag_ids)
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The DT or a requested tag was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to update this DT.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the parameters and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't add DT tags.", "cause": str(e)},
        )


@router.delete(
    "/{dt_manifest_id}/tags",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Remove tags from DT",
    description="Detach one or more tags from a DT while it is in `WIP`.",
)
async def remove_dt_tags(
    dt_manifest_id: DataTypeManifestId = Path(..., ge=1, description="Data type manifest ID."),
    tag_id: list[int] | None = Query(
        default=None,
        description="Tag identifier query parameter. Repeat `tag_id` to pass multiple values.",
    ),
    tag_id_list: list[int] | None = Query(
        default=None,
        description="Alternative tag identifier query parameter. Repeat `tag_id_list` to pass multiple values.",
    ),
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> Response:
    """Detach tags from a WIP DT."""
    try:
        if tag_id is not None and tag_id_list is not None:
            raise ValueError("Use either `tag_id` or `tag_id_list`, but not both.")
        effective_tag_ids = tag_id if tag_id is not None else tag_id_list
        if not effective_tag_ids:
            raise ValueError("At least one tag identifier is required.")
        await data_type_service.remove_dt_tags(dt_manifest_id=dt_manifest_id, tag_id=effective_tag_ids)
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The DT or a requested tag was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to update this DT.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the parameters and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't remove DT tags.", "cause": str(e)},
        )


@router.post(
    "/{dt_manifest_id}/state",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Change DT state",
    description=(
        "Change the lifecycle state of a DT. "
        "For `Working` releases, the allowed moves are `Deleted -> WIP`, `WIP -> Deleted|Draft`, "
        "`Draft -> WIP|Candidate`, and `Candidate -> WIP`. "
        "For non-`Working` releases, the allowed moves are `Deleted -> WIP`, `WIP -> Deleted|QA`, "
        "`QA -> WIP|Production`, and `Production` is terminal."
    ),
)
async def change_dt_state(
    payload: UpdateDataTypeStateRequest = Body(...),
    dt_manifest_id: DataTypeManifestId = Path(..., ge=1, description="Data type manifest ID."),
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> Response:
    """Change DT lifecycle state."""
    try:
        await data_type_service.change_dt_state(dt_manifest_id=dt_manifest_id, state=payload.state)
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The data type was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to update this DT state.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the body and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't change the DT state.", "cause": str(e)},
        )


async def _revise_or_amend_dt(
    *,
    dt_manifest_id: DataTypeManifestId,
    requested_action: Literal["revise", "amend"],
    data_type_service: DataTypeService,
) -> Response:
    """Create a revised or amended DT working copy."""
    try:
        await data_type_service.revise_dt(
            dt_manifest_id=dt_manifest_id,
            requested_action=requested_action,
        )
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The data type was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to revise or amend this DT.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the DT and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't revise or amend the DT.", "cause": str(e)},
        )


@router.post(
    "/{dt_manifest_id}/revise",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Revise/Amend DT",
    description=(
        "Create a new editable DT revision from a stable DT. "
        "Use `/revise` for developer-side DTs in `Working` releases when the stable DT is in `Published`. "
        "End-user DTs should use `/amend` instead."
    ),
)
async def revise_dt(
    dt_manifest_id: DataTypeManifestId = Path(..., ge=1, description="Data type manifest ID."),
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> Response:
    """Create a revised DT working copy for developer-side DTs."""
    return await _revise_or_amend_dt(
        dt_manifest_id=dt_manifest_id,
        requested_action="revise",
        data_type_service=data_type_service,
    )


@router.post(
    "/{dt_manifest_id}/amend",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Revise/Amend DT",
    description=(
        "Create a new editable DT revision from a stable DT. "
        "Use `/amend` for end-user DTs in non-`Working` releases when the stable DT is in `Production`. "
        "Developer-side DTs should use `/revise` instead."
    ),
    openapi_extra={"x-alternative-endpoint-for": "/data-types/{dt_manifest_id}/revise"},
)
async def amend_dt(
    dt_manifest_id: DataTypeManifestId = Path(..., ge=1, description="Data type manifest ID."),
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> Response:
    """Create an amended DT working copy for end-user DTs."""
    return await _revise_or_amend_dt(
        dt_manifest_id=dt_manifest_id,
        requested_action="amend",
        data_type_service=data_type_service,
    )


@router.post(
    "/{dt_manifest_id}/cancel",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Cancel DT",
    description="Cancel the current DT revision and restore the previous stable DT revision.",
)
async def cancel_dt(
    dt_manifest_id: DataTypeManifestId = Path(..., ge=1, description="Data type manifest ID."),
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> Response:
    """Cancel the active DT revision."""
    try:
        await data_type_service.cancel_dt(dt_manifest_id=dt_manifest_id)
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The data type was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to cancel this DT revision.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the DT and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't cancel the DT revision.", "cause": str(e)},
        )


@router.delete(
    "/{dt_manifest_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Discard DT",
    description="Discard a DT and its direct records permanently. The DT must already be in the `Deleted` state.",
)
async def discard_dt(
    dt_manifest_id: DataTypeManifestId = Path(..., ge=1, description="Data type manifest ID."),
    data_type_service: DataTypeService = Depends(get_data_type_service),
) -> Response:
    """Discard a Deleted DT."""
    try:
        await data_type_service.discard_dt(dt_manifest_id=dt_manifest_id)
        return Response(status_code=status.HTTP_204_NO_CONTENT)
    except LookupError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"message": "The data type was not found.", "cause": str(e)},
        )
    except PermissionError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"message": "You do not have permission to discard this DT.", "cause": str(e)},
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the DT and try again.", "cause": str(e)},
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"message": "We couldn't discard the DT.", "cause": str(e)},
        )
