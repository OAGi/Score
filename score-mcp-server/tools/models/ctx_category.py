"""Models for Context Category tools."""
from pydantic import BaseModel

from tools.models.common import WhoAndWhen


class CreateCtxCategoryResponse(BaseModel):
    """Response for create_ctx_category tool."""
    ctx_category_id: int  # Unique identifier of the newly created context category


class GetCtxCategoryResponse(BaseModel):
    """Response for get_ctx_category tool."""
    ctx_category_id: int  # Unique identifier for the context category
    guid: str  # Globally unique identifier for the context category
    name: str  # Name of the context category (e.g., "Geography", "Industry", "Product")
    description: str | None  # Description of what the context category represents
    created: WhoAndWhen  # Information about who created the context category and when
    last_updated: WhoAndWhen  # Information about who last updated the context category and when


class UpdateCtxCategoryResponse(BaseModel):
    """Response for update_context_category tool."""
    ctx_category_id: int  # Unique identifier of the updated context category
    updates: list[str]  # A list of field names that were updated (e.g., ["name", "description"])


class DeleteCtxCategoryResponse(BaseModel):
    """Response for delete_ctx_category tool."""
    ctx_category_id: int  # Unique identifier of the deleted context category


class GetCtxCategoriesResponse(BaseModel):
    """Response for get_ctx_categories tool."""
    total_items: int  # Total number of context categories available
    offset: int  # Offset of the first item in this page
    limit: int  # Number of items returned in this page
    items: list[GetCtxCategoryResponse]  # List of context categories on this page

