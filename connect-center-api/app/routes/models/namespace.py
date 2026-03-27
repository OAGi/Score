"""Pydantic response models for Namespace endpoints.

Defines the serialized shapes returned by namespace list/get routes, including
library and owner summaries.
"""


from __future__ import annotations

from pydantic import BaseModel, ConfigDict, Field

from app.routes.models.shared import UserSummary
from app.routes.models.shared import WhoAndWhen


class LibrarySummary(BaseModel):
    """Minimal library summary embedded in namespace responses."""
    library_id: int = Field(..., ge=1, description="Unique identifier for the library.")
    name: str = Field(..., description="Library name.")

    model_config = ConfigDict(frozen=True)


class NamespaceEntry(BaseModel):
    """API representation of a namespace record."""
    namespace_id: int = Field(..., ge=1, description="Unique identifier for the namespace.")
    library: LibrarySummary = Field(..., description="Library information where this namespace belongs.")
    uri: str = Field(..., description="Namespace URI.")
    prefix: str | None = Field(default=None, description="Default short name for the URI.")
    description: str | None = Field(default=None, description="Description of the namespace.")
    is_std_nmsp: bool = Field(..., description="Whether this is a standard namespace.")
    owner: UserSummary = Field(..., description="User information about the owner of the namespace.")
    created: WhoAndWhen = Field(..., description="Information about who created the namespace and when.")
    last_updated: WhoAndWhen = Field(..., description="Information about who last updated the namespace and when.")

    model_config = ConfigDict(frozen=True)


class GetNamespaceListResponse(BaseModel):
    """Paginated response envelope for namespace listings."""
    total_items: int = Field(..., ge=0, description="Total number of matching namespaces.")
    offset: int = Field(..., ge=0, description="Offset used for this page.")
    limit: int = Field(..., ge=1, le=100, description="Limit used for this page.")
    items: list[NamespaceEntry] = Field(..., description="Namespaces.")


class GetNamespaceByNamespaceIdResponse(NamespaceEntry):
    """Response payload for retrieving a namespace by ID."""

    model_config = ConfigDict(frozen=True)

