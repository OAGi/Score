"""Repository contract for revision-log persistence."""


from __future__ import annotations

from datetime import datetime
from typing import Protocol

from app.repositories.models.core_component import AsccRelationshipInfoRow, BccRelationshipInfoRow
from app.types.identifiers import AppUserId, AccManifestId, AsccpManifestId, BccpManifestId


class LogRepositoryContract(Protocol):
    """Protocol for revision-log repository implementations."""

    async def append_acc_log(
        self,
        *,
        acc_manifest_id: AccManifestId,
        relationships: list[AsccRelationshipInfoRow | BccRelationshipInfoRow],
        requester_user_id: AppUserId,
        action: str,
        timestamp: datetime,
    ) -> int:
        """Append a new ACC log row and update the manifest head."""
        pass

    async def revert_acc_log_to_stable_state(
        self,
        *,
        reference: str,
        current_log_id: int | None,
    ) -> tuple[int, list[str]]:
        """Delete latest revised ACC logs and return the stable log ID plus association GUID order."""
        pass

    async def append_asccp_log(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        requester_user_id: AppUserId,
        action: str,
        timestamp: datetime,
    ) -> int:
        """Append a new ASCCP log row and update the manifest head."""
        pass

    async def append_bccp_log(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        requester_user_id: AppUserId,
        action: str,
        timestamp: datetime,
    ) -> int:
        """Append a new BCCP log row and update the manifest head."""
        pass

    async def append_component_log(
        self,
        *,
        reference: str,
        current_log_id: int | None,
        requester_user_id: AppUserId,
        action: str,
        timestamp: datetime,
        snapshot: str | None = None,
    ) -> int:
        """Append a non-ACC component log row and return the new log ID."""
        pass

    async def revert_component_log_to_stable_state(
        self,
        *,
        reference: str,
        current_log_id: int | None,
    ) -> int:
        """Delete latest revised non-ACC logs and return the stable log ID."""
        pass
