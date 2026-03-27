"""Repository contract for Agency ID List persistence.

Defines the async interface for listing and retrieving agency ID lists and
related value manifests.
"""


from __future__ import annotations

from datetime import datetime
from typing import Protocol, Literal

from app.repositories.models.agency_id_list import AgencyIdListRow
from app.types.identifiers import AgencyIdListManifestId, ReleaseId


class AgencyIdListRepositoryContract(Protocol):
    """Protocol for agency ID list repository implementations."""

    async def list(
        self,
        release_id: ReleaseId,
        dependent_release_ids: list[ReleaseId],
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        name: str | None = None,
        list_id: str | None = None,
        version_id: str | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
    ) -> tuple[int, list[AgencyIdListRow]]:
        """Repository contract for list.

        Args:
            release_id: Release identifier used to scope the query.
            dependent_release_ids: Value for `dependent_release_ids`.
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            name: Optional name filter.
            list_id: Value for `list_id`.
            version_id: Value for `version_id`.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

        Returns:
            Result of the operation.
        """
        pass

    async def get(self, agency_id_list_manifest_id: AgencyIdListManifestId) -> AgencyIdListRow | None:
        """Repository contract for get.

        Args:
            agency_id_list_manifest_id: Agency ID list manifest identifier.

        Returns:
            Result of the operation.
        """
        pass
