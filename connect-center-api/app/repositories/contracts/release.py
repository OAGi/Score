"""Repository contract for Release persistence.

Defines the async interface for listing, retrieving, and resolving release
dependencies across vendor implementations.
"""


from __future__ import annotations

from datetime import datetime
from typing import Protocol, Literal

from app.repositories.models.release import ReleaseRow
from app.types.identifiers import LibraryId, ReleaseId


class ReleaseRepositoryContract(Protocol):
    """Protocol for release repository implementations."""
    async def list(
        self,
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        library_id: LibraryId | None = None,
        release_num: str | None = None,
        state: str | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
    ) -> tuple[int, list[ReleaseRow]]:
        """Repository contract for list.

        Args:
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            library_id: Library identifier used to scope the query.
            release_num: Optional release number filter.
            state: Optional lifecycle state filter.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

        Returns:
            Result of the operation.
        """
        pass

    async def get(self, release_id: ReleaseId) -> ReleaseRow | None:
        """Repository contract for get.

        Args:
            release_id: Release identifier used to scope the query.

        Returns:
            Result of the operation.
        """
        pass

    async def get_by_library_id_and_release_num(
        self,
        library_id: LibraryId,
        release_num: str,
    ) -> ReleaseRow | None:
        """Repository contract for exact library/release-number lookup.

        Args:
            library_id: Library identifier used to scope the query.
            release_num: Exact release number to locate.

        Returns:
            Result of the operation.
        """
        pass

    async def get_dependent_releases(self, release_id: ReleaseId) -> list[ReleaseId]:
        """Repository contract for get dependent releases.

        Args:
            release_id: Release identifier used to scope the query.

        Returns:
            Result of the operation.
        """
        pass
