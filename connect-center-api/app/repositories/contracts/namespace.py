"""Repository contract for Namespace persistence.

Defines the async interface for listing and retrieving namespaces across
vendor implementations (SQLite, MariaDB).
"""


from __future__ import annotations

from datetime import datetime
from typing import Protocol, Literal

from app.repositories.models.namespace import NamespaceRow
from app.types.identifiers import AppUserId, LibraryId, NamespaceId


class NamespaceRepositoryContract(Protocol):
    """Protocol for namespace repository implementations."""
    async def list(
        self,
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        library_id: LibraryId | None = None,
        uri: str | None = None,
        prefix: str | None = None,
        is_std_nmsp: bool | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
    ) -> tuple[int, list[NamespaceRow]]:
        """Repository contract for list.

        Args:
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            library_id: Library identifier used to scope the query.
            uri: Optional namespace URI filter.
            prefix: Optional namespace prefix filter.
            is_std_nmsp: Optional standard-namespace flag filter.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

        Returns:
            Result of the operation.
        """
        pass

    async def get(self, namespace_id: NamespaceId) -> NamespaceRow | None:
        """Repository contract for get.

        Args:
            namespace_id: Namespace identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def library_exists(self, library_id: LibraryId) -> bool:
        """Return whether the target library exists."""
        pass

    async def has_duplicate_uri(
        self,
        *,
        library_id: LibraryId,
        uri: str,
        exclude_namespace_id: NamespaceId | None = None,
    ) -> bool:
        """Return whether another namespace in the library already uses the URI."""
        pass

    async def has_duplicate_prefix(
        self,
        *,
        library_id: LibraryId,
        prefix: str,
        exclude_namespace_id: NamespaceId | None = None,
    ) -> bool:
        """Return whether another namespace in the library already uses the prefix."""
        pass

    async def create_namespace(
        self,
        *,
        library_id: LibraryId,
        uri: str,
        prefix: str,
        description: str | None,
        requester_user_id: AppUserId,
        requester_is_developer: bool,
    ) -> NamespaceId:
        """Create a namespace and return its identifier."""
        pass

    async def update_namespace(
        self,
        *,
        namespace_id: NamespaceId,
        uri: str,
        prefix: str,
        description: str | None,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update a namespace owned by the requester."""
        pass

    async def discard_namespace(self, *, namespace_id: NamespaceId) -> bool:
        """Delete a namespace row by identifier."""
        pass

    async def transfer_namespace_ownership(
        self,
        *,
        namespace_id: NamespaceId,
        requester_user_id: AppUserId,
        target_user_id: AppUserId,
    ) -> bool:
        """Transfer namespace ownership."""
        pass

    async def namespace_is_used(self, *, namespace_id: NamespaceId) -> bool:
        """Return whether any release or component still references the namespace."""
        pass
