"""Repository contract for Library persistence.

Defines the async interface for listing and retrieving libraries across
vendor implementations (SQLite, MariaDB).
"""


from __future__ import annotations

from datetime import datetime
from typing import Protocol, Literal

from app.repositories.models.library import LibraryRow
from app.types.identifiers import LibraryId


class LibraryRepositoryContract(Protocol):
    """Protocol for library repository implementations."""
    async def list(
        self,
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        name: str | None = None,
        type: str | None = None,
        organization: str | None = None,
        domain: str | None = None,
        state: str | None = None,
        description: str | None = None,
        is_default: bool | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
    ) -> tuple[int, list[LibraryRow]]:
        """Repository contract for list.

        Args:
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            name: Optional name filter.
            type: Optional type filter.
            organization: Optional organization filter.
            domain: Value for `domain`.
            state: Optional lifecycle state filter.
            description: Optional textual description filter or payload field.
            is_default: Optional default-library flag filter.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

        Returns:
            Result of the operation.
        """
        pass

    async def get(self, library_id: LibraryId) -> LibraryRow | None:
        """Repository contract for get.

        Args:
            library_id: Library identifier used to scope the query.

        Returns:
            Result of the operation.
        """
        pass
