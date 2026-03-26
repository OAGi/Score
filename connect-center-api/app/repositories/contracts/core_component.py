"""Repository contract for Core Component persistence.

Defines the async interface for listing and retrieving ACC, ASCCP, and BCCP
records and their relationships.
"""


from __future__ import annotations

from datetime import datetime
from typing import Protocol, Literal

from app.repositories.models.core_component import (
    AsccRelationshipInfoRow,
    BccRelationshipInfoRow,
    CoreComponentListRow,
    GetAccRow,
    GetAsccpRow,
    GetBccpRow,
)
from app.types.identifiers import AccManifestId, AsccpManifestId, BccpManifestId, ReleaseId


class CoreComponentRepositoryContract(Protocol):
    """Protocol for core component repository implementations."""

    async def list(
        self,
        release_id: ReleaseId,
        dependent_release_ids: list[ReleaseId],
        types: list[str],
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        den: str | None = None,
        tag: str | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
    ) -> tuple[int, list[CoreComponentListRow]]:
        """Repository contract for list.

        Args:
            release_id: Release identifier used to scope the query.
            dependent_release_ids: Value for `dependent_release_ids`.
            types: Optional component type filter list.
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            den: Optional Dictionary Entry Name (DEN) filter.
            tag: Optional tag filter.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

        Returns:
            Result of the operation.
        """
        pass

    async def get_acc(self, acc_manifest_id: AccManifestId) -> GetAccRow | None:
        """Repository contract for get acc.

        Args:
            acc_manifest_id: ACC manifest identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def get_acc_relationships(
        self,
        acc_manifest_id: AccManifestId,
    ) -> list[AsccRelationshipInfoRow | BccRelationshipInfoRow]:
        """Repository contract for get acc relationships.

        Args:
            acc_manifest_id: ACC manifest identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def get_asccp(self, asccp_manifest_id: AsccpManifestId) -> GetAsccpRow | None:
        """Repository contract for get asccp.

        Args:
            asccp_manifest_id: ASCCP manifest identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def get_bccp(self, bccp_manifest_id: BccpManifestId) -> GetBccpRow | None:
        """Repository contract for get bccp.

        Args:
            bccp_manifest_id: BCCP manifest identifier.

        Returns:
            Result of the operation.
        """
        pass
