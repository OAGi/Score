"""Pydantic request/response models for Business Context endpoints.

Includes request payloads for create/update and response envelopes for list/get,
along with embedded context scheme value summaries.
"""


from __future__ import annotations

from pydantic import BaseModel, ConfigDict, Field

from app.routes.models.shared import WhoAndWhen
from app.services.utils.string import Guid


class CtxSchemeValueSummary(BaseModel):
    """Minimal context scheme value summary embedded in business context responses."""
    ctx_scheme_value_id: int = Field(..., ge=1, description="Unique identifier for the context scheme value.", examples=[1])
    value: str = Field(..., description="Value of the context scheme.", examples=["US"])

    model_config = ConfigDict(frozen=True)


class BizCtxValueEntry(BaseModel):
    """API representation of a business context value."""
    biz_ctx_value_id: int = Field(..., ge=1, description="Unique identifier for the business context value.", examples=[1])
    ctx_scheme_value: CtxSchemeValueSummary = Field(..., description="Context scheme value information.")

    model_config = ConfigDict(frozen=True)


class BizCtxEntry(BaseModel):
    """API representation of a business context record (including values)."""
    biz_ctx_id: int = Field(..., ge=1, description="Internal, primary, database key.")
    guid: Guid = Field(..., description="A globally unique identifier (GUID), 32 characters.")
    name: str | None = Field(default=None, max_length=100, description="Short, descriptive name of the business context.")
    values: list[BizCtxValueEntry] = Field(default_factory=list, description="List of business context values.")
    created: WhoAndWhen = Field(..., description="Information about who created the business context and when.")
    last_updated: WhoAndWhen = Field(..., description="Information about who last updated the business context and when.")

    model_config = ConfigDict(frozen=True)


class GetBizCtxListResponse(BaseModel):
    """Paginated response envelope for business context listings."""
    total_items: int = Field(..., ge=0, description="Total number of business contexts available.")
    offset: int = Field(..., ge=0, description="Offset used for this page.")
    limit: int = Field(..., ge=1, le=100, description="Limit used for this page.")
    items: list[BizCtxEntry] = Field(..., description="Business contexts.")


class GetBizCtxByBizCtxIdResponse(BizCtxEntry):
    """Response body for retrieving a business context by ID."""

    model_config = ConfigDict(frozen=True)


class GetBizCtxValueByBizCtxValueIdResponse(BaseModel):
    """Response body for retrieving a business context value by ID."""

    biz_ctx_value_id: int = Field(..., ge=1, description="Unique identifier for the business context value.")
    biz_ctx_id: int = Field(..., ge=1, description="Unique identifier for the owning business context.")
    ctx_scheme_value_id: int = Field(..., ge=1, description="Linked context scheme value identifier.")

    model_config = ConfigDict(frozen=True)


class CreateBizCtxRequest(BaseModel):
    """Request body for creating a business context."""
    name: str = Field(..., max_length=100, description="Short, descriptive name of the business context.", examples=["Manufacturing BOM Profile"])

    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "name": "Manufacturing BOM Profile",
            }
        }
    )


class CreateBizCtxResponse(BaseModel):
    """Response body for successful business context creation."""
    biz_ctx_id: int = Field(..., ge=1, description="Unique identifier of the created business context.")


class UpdateBizCtxRequest(BaseModel):
    """Request body for partially updating a business context."""
    name: str | None = Field(default=None, max_length=100, description="Short, descriptive name of the business context. Omit to leave unchanged.")

    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "name": "Manufacturing BOM Profile",
            }
        }
    )


class UpdateBizCtxResponse(BaseModel):
    """Response body for business context updates."""
    biz_ctx_id: int = Field(
        ...,
        ge=1,
        description="Unique identifier of the updated business context.",
        examples=[1],
    )
    updates: list[str] = Field(
        default_factory=list,
        description="List of fields that were updated.",
        examples=[["name"]],
    )

    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "biz_ctx_id": 1,
                "updates": ["name"],
            }
        }
    )


class CreateBizCtxValueRequest(BaseModel):
    """Request body for creating a business context value."""
    ctx_scheme_value_id: int = Field(..., ge=1, description="Context scheme value ID to link to this business context.")


class CreateBizCtxValueResponse(BaseModel):
    """Response body for successful business context value creation."""
    biz_ctx_value_id: int = Field(..., ge=1, description="Unique identifier of the created business context value.")


class UpdateBizCtxValueRequest(BaseModel):
    """Request body for updating a business context value."""
    ctx_scheme_value_id: int = Field(
        ...,
        ge=1,
        description="Context scheme value ID to link to this business context value.",
        examples=[1],
    )


class UpdateBizCtxValueResponse(BaseModel):
    """Response body for business context value updates."""
    biz_ctx_value_id: int = Field(
        ...,
        ge=1,
        description="Unique identifier of the updated business context value.",
        examples=[1],
    )
    updates: list[str] = Field(
        default_factory=list,
        description="List of fields that were updated.",
        examples=[["ctx_scheme_value_id"]],
    )

    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "biz_ctx_value_id": 1,
                "updates": ["ctx_scheme_value_id"],
            }
        }
    )
