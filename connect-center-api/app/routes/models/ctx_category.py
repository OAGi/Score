"""Pydantic request/response models for Context Category endpoints.

Includes request payloads for create/update and response envelopes for list/get.
"""


from __future__ import annotations

from pydantic import BaseModel, ConfigDict, Field

from app.routes.models.shared import WhoAndWhen
from app.services.utils.string import Guid


class ContextCategoryBase(BaseModel):
    """Shared fields for context category responses."""
    guid: Guid = Field(
        ...,
        description="A globally unique identifier (GUID), 32 characters.",
        examples=["0123456789abcdef0123456789abcdef"],
    )
    name: str = Field(..., max_length=45, description="Short name of the context category.", examples=["Industry"])
    description: str | None = Field(
        default=None,
        description="Explanation of what the context category is.",
        examples=["Categorizes contexts by industry segment."],
    )


class CreateContextCategoryRequest(BaseModel):
    """Request body for creating a context category."""
    name: str = Field(..., max_length=45, description="Name of the context category.", examples=["Geographic"])
    description: str | None = Field(
        default=None,
        description="Description of the context category.",
        examples=["Geographic context for location-based data"],
    )

    model_config = ConfigDict(
        json_schema_extra={
            "examples": [
                {
                    "name": "Industry",
                    "description": "Categorizes contexts by industry segment.",
                }
            ]
        }
    )


class UpdateContextCategoryRequest(BaseModel):
    """Request body for partially updating a context category."""
    name: str | None = Field(
        default=None,
        max_length=45,
        description="Short name of the context category. Omit to leave unchanged.",
        examples=["Business Process"],
    )
    description: str | None = Field(
        default=None,
        description="Explanation of what the context category is. Omit to leave unchanged.",
        examples=["Categorizes contexts by business process."],
    )

    model_config = ConfigDict(
        json_schema_extra={
            "examples": [
                {
                    "name": "Industry",
                }
            ]
        }
    )


class ContextCategoryEntry(BaseModel):
    """API representation of a context category record."""
    ctx_category_id: int = Field(
        ...,
        ge=1,
        description="Internal, primary, database key.",
        examples=[13],
    )
    guid: Guid = Field(
        ...,
        description="A globally unique identifier (GUID), 32 characters.",
        examples=["0123456789abcdef0123456789abcdef"],
    )
    name: str = Field(..., max_length=45, description="Short name of the context category.", examples=["Industry"])
    description: str | None = Field(
        default=None,
        description="Explanation of what the context category is.",
        examples=["Categorizes contexts by industry segment."],
    )
    created: WhoAndWhen = Field(..., description="Information about who created the context category and when.")
    last_updated: WhoAndWhen = Field(..., description="Information about who last updated the context category and when.")

    model_config = ConfigDict(frozen=True)


class GetContextCategoryListResponse(BaseModel):
    """Paginated response envelope for context category listings."""
    total_items: int = Field(..., ge=0, description="Total number of context categories available.")
    offset: int = Field(..., ge=0, description="Offset used for this page.")
    limit: int = Field(..., ge=1, le=100, description="Limit used for this page.")
    items: list[ContextCategoryEntry] = Field(..., description="Context categories.")


class GetContextCategoryByContextCategoryIdResponse(ContextCategoryEntry):
    """Response body for retrieving a context category by ID."""

    model_config = ConfigDict(frozen=True)


class CreateContextCategoryResponse(BaseModel):
    """Response body for successful context category creation."""
    context_category_id: int = Field(..., ge=1, description="Unique identifier of the created context category.")


class UpdateContextCategoryResponse(BaseModel):
    """Response body for context category updates."""

    ctx_category_id: int = Field(
        ...,
        ge=1,
        description="Unique identifier of the updated context category",
        examples=[1],
    )
    updates: list[str] = Field(
        default_factory=list,
        description='A list of field names that were updated (e.g., ["name", "description"])',
        examples=[["name"]],
    )

    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "ctx_category_id": 1,
                "updates": ["name"],
            }
        }
    )
