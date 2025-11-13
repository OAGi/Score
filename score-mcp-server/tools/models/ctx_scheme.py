"""Models for Context Scheme tools."""
from pydantic import BaseModel

from tools.models.common import WhoAndWhen


class CreateCtxSchemeResponse(BaseModel):
    """Response for create_ctx_scheme tool."""
    ctx_scheme_id: int  # Unique identifier of the newly created context scheme


class CreateCtxSchemeValueResponse(BaseModel):
    """Response for create_ctx_scheme_value tool."""
    ctx_scheme_value_id: int  # Unique identifier of the newly created context scheme value


class UpdateCtxSchemeValueResponse(BaseModel):
    """Response for update_context_scheme_value tool."""
    ctx_scheme_value_id: int  # Unique identifier of the updated context scheme value
    updates: list[str]  # A list of field names that were updated (e.g., ["value", "meaning"])


class DeleteCtxSchemeValueResponse(BaseModel):
    """Response for delete_context_scheme_value tool."""
    ctx_scheme_value_id: int  # Unique identifier of the deleted context scheme value


class UpdateCtxSchemeResponse(BaseModel):
    """Response for update_context_scheme tool."""
    ctx_scheme_id: int  # Unique identifier of the updated context scheme
    updates: list[str]  # A list of field names that were updated (e.g., ["scheme_name", "description"])


class DeleteCtxSchemeResponse(BaseModel):
    """Response for delete_ctx_scheme tool."""
    ctx_scheme_id: int  # Unique identifier of the deleted context scheme


class CtxCategoryInfo(BaseModel):
    """Context category information."""
    ctx_category_id: int  # Unique identifier for the context category
    name: str  # Name of the context category (e.g., "Geography", "Industry")


class CtxSchemeValueInfo(BaseModel):
    """Context scheme value information."""
    ctx_scheme_value_id: int  # Unique identifier for the context scheme value
    guid: str  # Globally unique identifier for the context scheme value
    value: str  # The actual value string (e.g., "US", "EUR", "Production")
    meaning: str | None = None  # Human-readable meaning or description of what this value represents


class GetCtxSchemeResponse(BaseModel):
    """Response for get_ctx_schemes tool."""
    ctx_scheme_id: int  # Unique identifier for the context scheme
    guid: str  # Globally unique identifier for the context scheme
    scheme_id: str  # Scheme identifier that uniquely identifies the context scheme standard (e.g., "ISO3166-1")
    scheme_name: str | None  # Human-readable name of the context scheme (e.g., "Country Code")
    description: str | None  # Description of what the context scheme represents
    scheme_agency_id: str  # Agency identifier that maintains the scheme (e.g., "ISO", "UN")
    scheme_version_id: str  # Version identifier of the context scheme (e.g., "1.0", "2.1")
    ctx_category: CtxCategoryInfo | None  # Context category this scheme belongs to (if any)
    values: list[CtxSchemeValueInfo]  # List of values contained in this context scheme
    created: WhoAndWhen  # Information about who created the context scheme and when
    last_updated: WhoAndWhen  # Information about who last updated the context scheme and when


class GetCtxSchemesResponse(BaseModel):
    """Response for get_ctx_schemes tool."""
    total_items: int  # Total number of context schemes available
    offset: int  # Offset of the first item in this page
    limit: int  # Number of items returned in this page
    items: list[GetCtxSchemeResponse]  # List of context schemes on this page

