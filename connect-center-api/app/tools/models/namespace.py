"""Models for Namespace MCP tools."""

from __future__ import annotations

from pydantic import BaseModel, ConfigDict

from app.tools.models.shared import LibrarySummaryRecord
from app.tools.models.shared import UserSummary
from app.tools.models.shared import WhoAndWhen


class NamespaceResponseEntry(BaseModel):
    """Namespace payload for MCP tools."""

    namespace_id: int
    library: LibrarySummaryRecord
    uri: str
    prefix: str | None = None
    description: str | None = None
    is_std_nmsp: bool
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetNamespaceResponse(NamespaceResponseEntry):
    """Response for get_namespace tool."""


class GetNamespacePaginationResponse(BaseModel):
    """Response for get_namespaces tool."""

    total_items: int
    offset: int
    limit: int
    items: list[NamespaceResponseEntry]

    model_config = ConfigDict(frozen=True)


class CreateNamespaceResponse(BaseModel):
    """Response for create_namespace tool."""

    namespace_id: int

    model_config = ConfigDict(frozen=True)


class UpdateNamespaceResponse(BaseModel):
    """Response for update_namespace tool."""

    namespace_id: int
    updates: list[str]

    model_config = ConfigDict(frozen=True)


class TransferNamespaceOwnershipResponse(BaseModel):
    """Response for transfer_namespace_ownership tool."""

    namespace_id: int
    updates: list[str]

    model_config = ConfigDict(frozen=True)
