"""XBT API routes."""


from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, Path, status

from app.deps import get_xbt_service
from app.routes.models.xbt import GetXbtByXbtManifestIdResponse
from app.services.xbt_service import XbtService
from app.types.identifiers import XbtManifestId

router = APIRouter(prefix="/xbts", tags=["xbt"])


@router.get(
    "/{xbt_manifest_id}",
    summary="Retrieve an XBT",
    description=(
        "Retrieve a XBT (XML Built-in Type) by ID. "
        "Note that XBTs originated in XML Schema and include mappings for JSON Schema, OpenAPI v3, and Avro Schema."
    ),
    response_model=GetXbtByXbtManifestIdResponse,
)
async def get_xbt_by_xbt_manifest_id(
    xbt_manifest_id: XbtManifestId = Path(..., ge=1, description="XBT manifest ID."),
    xbt_service: XbtService = Depends(get_xbt_service),
) -> GetXbtByXbtManifestIdResponse:
    """Return an XBT by manifest ID.

    Args:
        xbt_manifest_id: XBT manifest identifier.
        xbt_service: XBT service dependency.

    Returns:
        Response payload for the requested resource.
    """
    row = await xbt_service.get(xbt_manifest_id)
    if row is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "message": "The XBT was not found.",
                "cause": f"No XBT exists with manifest ID {int(xbt_manifest_id)}.",
            },
        )
    return GetXbtByXbtManifestIdResponse.model_validate(row, from_attributes=True)

