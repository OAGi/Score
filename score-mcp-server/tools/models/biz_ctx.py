"""Models for Business Context tools."""
from pydantic import BaseModel

from tools.models.common import WhoAndWhen


class BusinessContextInfo(BaseModel):
    """Business context information object."""
    biz_ctx_id: int  # Unique identifier for the business context
    guid: str  # Globally unique identifier for the business context
    name: str | None  # Human-readable name of the business context (e.g., "Production Environment", "Test Environment")


class CreateBizCtxResponse(BaseModel):
    """Response for create_business_context tool."""
    biz_ctx_id: int  # Unique identifier of the newly created business context


class CreateBizCtxValueResponse(BaseModel):
    """Response for create_business_context_value tool."""
    biz_ctx_value_id: int  # Unique identifier of the newly created business context value


class UpdateBizCtxValueResponse(BaseModel):
    """Response for update_business_context_value tool."""
    biz_ctx_value_id: int  # Unique identifier of the updated business context value
    updates: list[str]  # A list of field names that were updated (e.g., ["value"])


class DeleteBizCtxValueResponse(BaseModel):
    """Response for delete_business_context_value tool."""
    biz_ctx_value_id: int  # Unique identifier of the deleted business context value


class UpdateBizCtxResponse(BaseModel):
    """Response for update_business_context tool."""
    biz_ctx_id: int  # Unique identifier of the updated business context
    updates: list[str]  # A list of field names that were updated (e.g., ["name"])


class DeleteBizCtxResponse(BaseModel):
    """Response for delete_business_context tool."""
    biz_ctx_id: int  # Unique identifier of the deleted business context


class CtxSchemeValueInfo(BaseModel):
    """Context scheme value information."""
    ctx_scheme_value_id: int  # Unique identifier for the context scheme value
    value: str  # The actual value string of the context scheme entry (e.g., "US", "EUR")


class BizCtxValueInfo(BaseModel):
    """Business context value information."""
    biz_ctx_value_id: int  # Unique identifier for the business context value
    ctx_scheme_value: CtxSchemeValueInfo  # The context scheme value associated with this business context value


class GetBizCtxResponse(BaseModel):
    """Response for get_business_context tool."""
    biz_ctx_id: int  # Unique identifier for the business context
    guid: str  # Globally unique identifier for the business context
    name: str | None  # Human-readable name of the business context (e.g., "Production Environment", "Test Environment")
    values: list[BizCtxValueInfo]  # List of context scheme values assigned to this business context
    created: WhoAndWhen  # Information about who created the business context and when
    last_updated: WhoAndWhen  # Information about who last updated the business context and when


class GetBizCtxsResponse(BaseModel):
    """Response for get_business_contexts tool."""
    total_items: int  # Total number of business contexts available
    offset: int  # Offset of the first item in this page
    limit: int  # Number of items returned in this page
    items: list[GetBizCtxResponse]  # List of business contexts on this page

