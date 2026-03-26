"""Repository contract for XBT persistence."""


from __future__ import annotations

from typing import Protocol

from app.repositories.models.xbt import XbtRow
from app.types.identifiers import XbtManifestId


class XbtRepositoryContract(Protocol):
    """Protocol for XBT repository implementations."""

    async def get(self, xbt_manifest_id: XbtManifestId) -> XbtRow | None:
        """Repository contract for get.

        Args:
            xbt_manifest_id: XBT manifest identifier.

        Returns:
            Result of the operation.
        """
        pass
