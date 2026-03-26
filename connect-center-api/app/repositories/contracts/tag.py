"""Repository contract for Tag persistence."""


from __future__ import annotations

from datetime import datetime
from typing import Protocol, Literal

from app.repositories.models.tag import TagRow


class TagRepositoryContract(Protocol):
    """Protocol for tag repository implementations."""

    async def list(
        self,
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        name: str | None = None,
        description: str | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
    ) -> tuple[int, list[TagRow]]:
        """Repository contract for list.

        Args:
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            name: Optional name filter.
            description: Optional textual description filter or payload field.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

        Returns:
            Result of the operation.
        """
        pass
