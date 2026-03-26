"""Models for Namespace MCP tools."""

from __future__ import annotations

from app.routes.models.namespace import GetNamespaceByNamespaceIdResponse, GetNamespaceListResponse


class GetNamespaceResponse(GetNamespaceByNamespaceIdResponse):
    """Response for get_namespace tool."""


class GetNamespacePaginationResponse(GetNamespaceListResponse):
    """Response for get_namespaces tool."""
