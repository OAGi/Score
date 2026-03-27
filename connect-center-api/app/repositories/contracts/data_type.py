"""Repository contract for Data Type persistence.

Defines the async interface for listing and retrieving data types and their
supplementary components.
"""


from __future__ import annotations

from datetime import datetime
from typing import Protocol, Literal

from app.repositories.models.data_type import DataTypeRow
from app.types.identifiers import DataTypeManifestId, ReleaseId


class DataTypeRepositoryContract(Protocol):
    """Protocol for data type repository implementations."""

    async def list(
        self,
        release_id: ReleaseId,
        dependent_release_ids: list[ReleaseId],
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        den: str | None = None,
        representation_term: str | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
    ) -> tuple[int, list[DataTypeRow]]:
        """Repository contract for list.

        Args:
            release_id: Release identifier used to scope the query.
            dependent_release_ids: Value for `dependent_release_ids`.
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            den: Optional Dictionary Entry Name (DEN) filter.
            representation_term: Value for `representation_term`.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

        Returns:
            Result of the operation.
        """
        pass

    async def get(self, dt_manifest_id: DataTypeManifestId) -> DataTypeRow | None:
        """Repository contract for get.

        Args:
            dt_manifest_id: Data type manifest identifier.

        Returns:
            Result of the operation.
        """
        pass
