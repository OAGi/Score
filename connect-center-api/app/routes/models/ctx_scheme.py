"""Pydantic request/response models for Context Scheme endpoints.

Includes request payloads for create/update and response envelopes for list/get,
along with embedded scheme value summaries.
"""


from __future__ import annotations

from pydantic import BaseModel, ConfigDict, Field

from app.routes.models.shared import WhoAndWhen
from app.services.utils.string import Guid


class CtxCategorySummary(BaseModel):
    """Minimal context category summary embedded in scheme responses."""
    ctx_category_id: int = Field(..., ge=1, description="Unique identifier for the context category.", examples=[1])
    name: str = Field(..., description="Name of the context category.", examples=["Geographic"])

    model_config = ConfigDict(frozen=True)


class CtxSchemeValueEntry(BaseModel):
    """API representation of a context scheme value."""
    ctx_scheme_value_id: int = Field(..., ge=1, description="Unique identifier for the context scheme value.", examples=[1])
    guid: Guid = Field(..., description="A globally unique identifier (GUID), 32 characters.")
    value: str = Field(..., max_length=100, description="Short value for the scheme value.", examples=["US"])
    meaning: str | None = Field(default=None, description="Description or explanation of the scheme value.", examples=["United States"])

    model_config = ConfigDict(frozen=True)


class CtxSchemeEntry(BaseModel):
    """API representation of a context scheme record (including values)."""
    ctx_scheme_id: int = Field(..., ge=1, description="Internal, primary, database key.")
    guid: Guid = Field(..., description="A globally unique identifier (GUID), 32 characters.")
    scheme_id: str = Field(..., max_length=45, description="External identification of the scheme.", examples=["Country"])
    scheme_name: str = Field(..., max_length=255, description="Pretty print name of the context scheme.", examples=["Country Code"])
    description: str | None = Field(default=None, description="Description of the context scheme.")
    scheme_agency_id: str = Field(
        ...,
        max_length=45,
        description="Identification of the agency maintaining the scheme.",
        examples=["ISO"],
    )
    scheme_version_id: str = Field(
        ...,
        max_length=45,
        description="Version number of the context scheme.",
        examples=["1.0"],
    )
    ctx_category: CtxCategorySummary | None = Field(
        default=None,
        description="Associated context category.",
    )
    values: list[CtxSchemeValueEntry] = Field(default_factory=list, description="List of context scheme values.")
    created: WhoAndWhen = Field(..., description="Information about who created the context scheme and when.")
    last_updated: WhoAndWhen = Field(..., description="Information about who last updated the context scheme and when.")

    model_config = ConfigDict(frozen=True)


class GetCtxSchemeListResponse(BaseModel):
    """Paginated response envelope for context scheme listings."""
    total_items: int = Field(..., ge=0, description="Total number of context schemes available.")
    offset: int = Field(..., ge=0, description="Offset used for this page.")
    limit: int = Field(..., ge=1, le=100, description="Limit used for this page.")
    items: list[CtxSchemeEntry] = Field(..., description="Context schemes.")


class GetCtxSchemeByCtxSchemeIdResponse(CtxSchemeEntry):
    """Response body for retrieving a context scheme by ID."""

    model_config = ConfigDict(frozen=True)


class GetCtxSchemeValueByCtxSchemeValueIdResponse(BaseModel):
    """Response body for retrieving a context scheme value by ID."""

    ctx_scheme_value_id: int = Field(..., ge=1, description="Unique identifier for the context scheme value.")
    owner_ctx_scheme_id: int = Field(..., ge=1, description="Unique identifier for the owning context scheme.")
    guid: Guid = Field(..., description="A globally unique identifier (GUID), 32 characters.")
    value: str = Field(..., max_length=100, description="Short value for the scheme value.", examples=["US"])
    meaning: str | None = Field(default=None, description="Description or explanation of the scheme value.")

    model_config = ConfigDict(frozen=True)


class CreateCtxSchemeRequest(BaseModel):
    """Request body for creating a context scheme."""
    scheme_id: str = Field(..., max_length=45, description="External identification of the scheme.", examples=["Country"])
    scheme_name: str = Field(..., max_length=255, description="Pretty print name of the context scheme.", examples=["Country Code"])
    description: str | None = Field(default=None, description="Description of the context scheme.")
    scheme_agency_id: str | None = Field(
        default=None,
        max_length=45,
        description="Identification of the agency maintaining the scheme.",
        examples=["ISO"],
    )
    scheme_version_id: str | None = Field(
        default=None,
        max_length=45,
        description="Version number of the context scheme.",
        examples=["1.0"],
    )
    ctx_category_id: int | None = Field(
        default=None,
        ge=1,
        description="Associated context category ID.",
        examples=[1],
    )


class CreateCtxSchemeResponse(BaseModel):
    """Response body for successful context scheme creation."""
    ctx_scheme_id: int = Field(..., ge=1, description="Unique identifier of the created context scheme.")


class UpdateCtxSchemeRequest(BaseModel):
    """Request body for partially updating a context scheme."""
    scheme_id: str | None = Field(default=None, max_length=45, description="External identification of the scheme.")
    scheme_name: str | None = Field(default=None, max_length=255, description="Pretty print name of the context scheme.")
    description: str | None = Field(default=None, description="Description of the context scheme.")
    scheme_agency_id: str | None = Field(default=None, max_length=45, description="Identification of the agency maintaining the scheme.")
    scheme_version_id: str | None = Field(default=None, max_length=45, description="Version number of the context scheme.")
    ctx_category_id: int | None = Field(default=None, ge=1, description="Associated context category ID.")

    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "scheme_name": "Country Code",
            }
        }
    )


class UpdateCtxSchemeResponse(BaseModel):
    """Response body for context scheme updates."""
    ctx_scheme_id: int = Field(
        ...,
        ge=1,
        description="Unique identifier of the updated context scheme.",
        examples=[1],
    )
    updates: list[str] = Field(
        default_factory=list,
        description="List of fields that were updated.",
        examples=[["scheme_name"]],
    )

    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "ctx_scheme_id": 1,
                "updates": ["scheme_name"],
            }
        }
    )


class CreateCtxSchemeValueRequest(BaseModel):
    """Request body for creating a context scheme value."""
    value: str = Field(..., max_length=100, description="Value of the context scheme.", examples=["US"])
    meaning: str | None = Field(default=None, description="Meaning of the context scheme value.")


class CreateCtxSchemeValueResponse(BaseModel):
    """Response body for successful context scheme value creation."""
    ctx_scheme_value_id: int = Field(..., ge=1, description="Unique identifier of the created context scheme value.")


class UpdateCtxSchemeValueRequest(BaseModel):
    """Request body for updating a context scheme value."""
    value: str | None = Field(default=None, max_length=100, description="Value of the context scheme.")
    meaning: str | None = Field(default=None, description="Meaning of the context scheme value.")

    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "value": "US",
            }
        }
    )


class UpdateCtxSchemeValueResponse(BaseModel):
    """Response body for context scheme value updates."""
    ctx_scheme_value_id: int = Field(
        ...,
        ge=1,
        description="Unique identifier of the updated context scheme value.",
        examples=[1],
    )
    updates: list[str] = Field(
        default_factory=list,
        description="List of fields that were updated.",
        examples=[["value"]],
    )

    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "ctx_scheme_value_id": 1,
                "updates": ["value"],
            }
        }
    )
