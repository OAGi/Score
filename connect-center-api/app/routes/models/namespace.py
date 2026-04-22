"""Pydantic request and response models for Namespace endpoints.

Defines the serialized shapes returned by namespace routes, including
create/update/transfer payloads and list/get responses.
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


class CreateNamespaceRequest(BaseModel):
    """Request payload for creating a namespace."""

    library_id: int = Field(..., ge=1, description="Owning library identifier.")
    uri: str = Field(..., min_length=1, description="Namespace URI.")
    prefix: str | None = Field(
        default=None,
        description="Namespace prefix. Use an empty string to store a blank prefix.",
    )
    description: str | None = Field(default=None, description="Namespace description.")

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "library_id": 1,
                "uri": "http://www.example.com/acme/2026",
                "prefix": "acme",
                "description": "ACME custom namespace",
            }
        },
    )


class UpdateNamespaceRequest(BaseModel):
    """Request payload for updating a namespace."""

    uri: str | None = Field(default=None, min_length=1, description="Namespace URI to save. Omit this field to leave it unchanged.")
    prefix: str | None = Field(
        default=None,
        description="Namespace prefix to save. Omit this field to leave it unchanged. Use an empty string to store a blank prefix.",
    )
    description: str | None = Field(default=None, description="Namespace description to save. Omit this field to leave it unchanged.")

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "uri": "http://www.example.com/acme/2026",
                "prefix": "acme",
                "description": "Updated namespace description",
            }
        },
    )


class TransferNamespaceOwnershipRequest(BaseModel):
    """Request payload for namespace ownership transfer."""

    target_login_id: str = Field(..., min_length=1, description="Login ID of the new owner.")

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={"example": {"target_login_id": "developer1"}},
    )


class CreateNamespaceResponse(BaseModel):
    """Response payload for creating a namespace."""

    namespace_id: int = Field(..., ge=1, description="Created namespace identifier.")

    model_config = ConfigDict(frozen=True)


class UpdateNamespaceResponse(BaseModel):
    """Response payload for namespace updates."""

    namespace_id: int = Field(..., ge=1, description="Target namespace identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.")

    model_config = ConfigDict(frozen=True)


class TransferNamespaceOwnershipResponse(BaseModel):
    """Response payload for namespace ownership transfer."""

    namespace_id: int = Field(..., ge=1, description="Target namespace identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.")

    model_config = ConfigDict(frozen=True)
