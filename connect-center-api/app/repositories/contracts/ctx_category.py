"""Repository contract for Context Category persistence.

Defines the async interface for CRUD operations and listing categories
across vendor implementations.
"""

from __future__ import annotations

from datetime import datetime
from typing import Any, Literal
from typing import Protocol

from app.repositories.models.ctx_category import ContextCategoryRow
from app.types.identifiers import AppUserId, ContextCategoryId


class ContextCategoryRepositoryContract(Protocol):
    """Protocol for context category repository implementations."""
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
        included_updater_login_ids: list[str] | None = None,
        excluded_updater_login_ids: list[str] | None = None,
    ) -> tuple[int, list[ContextCategoryRow]]:
        """Repository contract for list.

        Args:
            limit: Maximum number of rows to return.
            offset: Number of rows to skip.
            sorts: Sort definitions.
            name: Optional name filter.
            description: Optional textual description filter or payload field.
            creation_timestamp_before: Include rows created on/before this timestamp.
            creation_timestamp_after: Include rows created on/after this timestamp.
            last_update_timestamp_before: Include rows updated on/before this timestamp.
            last_update_timestamp_after: Include rows updated on/after this timestamp.
            included_updater_login_ids: Optional updater login IDs to include by exact match.
            excluded_updater_login_ids: Optional updater login IDs to exclude by exact match.

        Returns:
            Result of the operation.
        """
        pass

    async def get(self, context_category_id: ContextCategoryId) -> ContextCategoryRow | None:
        """Repository contract for get.

        Args:
            context_category_id: Context category identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def create(
        self,
        guid: str,
        name: str,
        description: str | None,
        created_by: AppUserId,
        last_updated_by: AppUserId,
        creation_timestamp: Any,
        last_update_timestamp: Any,
    ) -> ContextCategoryId:
        """Repository contract for create.

        Args:
            guid: Value for `guid`.
            name: Category name.
            description: Optional category description.
            created_by: Identifier of the user who created the record.
            last_updated_by: Identifier of the user who last updated the record.
            creation_timestamp: Value for `creation_timestamp`.
            last_update_timestamp: Value for `last_update_timestamp`.

        Returns:
            Result of the operation.
        """
        pass

    async def update(
        self,
        context_category_id: ContextCategoryId,
        name: str | None,
        name_set: bool,
        description: str | None,
        description_set: bool,
        last_updated_by: AppUserId,
        last_update_timestamp: Any,
    ) -> bool:
        """Repository contract for update.

        Args:
            context_category_id: Context category identifier.
            name: Optional name filter.
            name_set: Value for `name_set`.
            description: Optional textual description filter or payload field.
            description_set: Value for `description_set`.
            last_updated_by: Identifier of the user who last updated the record.
            last_update_timestamp: Value for `last_update_timestamp`.

        Returns:
            Result of the operation.
        """
        pass

    async def delete(self, context_category_id: ContextCategoryId) -> bool:
        """Repository contract for delete.

        Args:
            context_category_id: Context category identifier.

        Returns:
            Result of the operation.
        """
        pass
