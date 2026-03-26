"""Models for Business Information Entity MCP tools."""

from __future__ import annotations

from pydantic import BaseModel

from app.routes.models.business_information_entity import (
    CreateASBIEResponse,
    CreateBBIEResponse,
    CreateBBIESCResponse,
    CreateTopLevelASBIEPResponse,
    GetAsbieByBasedAsccManifestIdResponse,
    DeleteTopLevelASBIEPResponse,
    GetAsbieByAsbieIdResponse,
    GetBbieByBasedBccManifestIdResponse,
    GetBbieByBbieIdResponse,
    GetTopLevelAsbiepListResponse,
    GetTopLevelAsbiepResponse,
    ReuseTopLevelASBIEPResponse,
    UpdateASBIEResponse,
    UpdateBBIEResponse,
    UpdateBBIESCResponse,
    UpdateTopLevelASBIEPResponse,
    UpdateTopLevelAsbiepStateResponse,
)


class GetTopLevelAsbiepPaginationResponse(GetTopLevelAsbiepListResponse):
    """Response for get_top_level_asbiep_list tool."""


class GetTopLevelAsbiepToolResponse(GetTopLevelAsbiepResponse):
    """Response for get_top_level_asbiep tool."""


class GetAsbieResponse(GetAsbieByAsbieIdResponse):
    """Response for ASBIE detail tools."""


class GetAsbieTemplateResponse(GetAsbieByBasedAsccManifestIdResponse):
    """Response for ASBIE template-detail tools."""


class GetBbieResponse(GetBbieByBbieIdResponse):
    """Response for BBIE detail tools."""


class GetBbieTemplateResponse(GetBbieByBasedBccManifestIdResponse):
    """Response for BBIE template-detail tools."""


class CreateTopLevelAsbiepResponse(CreateTopLevelASBIEPResponse):
    """Response for create_top_level_asbiep tool."""


class UpdateTopLevelAsbiepResponse(UpdateTopLevelASBIEPResponse):
    """Response for update_top_level_asbiep tool."""


class UpdateTopLevelAsbiepStateToolResponse(UpdateTopLevelAsbiepStateResponse):
    """Response for update_top_level_asbiep_state tool."""


class DeleteTopLevelAsbiepToolResponse(DeleteTopLevelASBIEPResponse):
    """Response for delete_top_level_asbiep tool."""


class TransferTopLevelAsbiepOwnershipResponse(BaseModel):
    """Response for transfer_top_level_asbiep_ownership tool."""

    top_level_asbiep_id: int
    updates: list[str]


class AssignBizCtxToTopLevelAsbiepToolResponse(BaseModel):
    """Response for assign_biz_ctx_to_top_level_asbiep tool."""

    top_level_asbiep_id: int
    updates: list[str]


class UnassignBizCtxFromTopLevelAsbiepToolResponse(BaseModel):
    """Response for unassign_biz_ctx_from_top_level_asbiep tool."""

    top_level_asbiep_id: int
    updates: list[str]


class CreateAsbieToolResponse(CreateASBIEResponse):
    """Response for create_asbie tool."""


class UpdateAsbieToolResponse(UpdateASBIEResponse):
    """Response for update_asbie tool."""


class CreateBbieToolResponse(CreateBBIEResponse):
    """Response for create_bbie tool."""


class UpdateBbieToolResponse(UpdateBBIEResponse):
    """Response for update_bbie tool."""


class CreateBbieScToolResponse(CreateBBIESCResponse):
    """Response for create_bbie_sc tool."""


class UpdateBbieScToolResponse(UpdateBBIESCResponse):
    """Response for update_bbie_sc tool."""


class ReuseTopLevelAsbiepToolResponse(ReuseTopLevelASBIEPResponse):
    """Response for reuse_top_level_asbiep tool."""


class RemoveReusedTopLevelAsbiepToolResponse(BaseModel):
    """Response for remove_reused_top_level_asbiep tool."""

    asbie_id: int
    updates: list[str]
