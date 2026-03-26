"""Repository contract for Namespace persistence.

Defines the async interface for listing and retrieving namespaces across
vendor implementations (SQLite, MariaDB).
"""


from __future__ import annotations

from datetime import datetime
from typing import Protocol, Literal

from app.repositories.models.namespace import NamespaceRow
from app.types.identifiers import LibraryId, NamespaceId


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
