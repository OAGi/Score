"""Repository contract for Business Context persistence.

Defines the async interface for CRUD operations on contexts and values,
plus validation helpers for scheme value existence.
"""

from __future__ import annotations

from datetime import datetime
from typing import Any, Literal
from typing import Protocol

from app.repositories.models.biz_ctx import BizCtxRow, BizCtxValueDetailRow
from app.types.identifiers import AppUserId, BizCtxId, BizCtxValueId, CtxSchemeValueId


class BizCtxRepositoryContract(Protocol):
    """Protocol for business context repository implementations."""
    async def list(
        self,
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        name: str | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
        included_updater_login_ids: list[str] | None = None,
        excluded_updater_login_ids: list[str] | None = None,
    ) -> tuple[int, list[BizCtxRow]]:
        """Repository contract for list.

        Args:
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            name: Optional name filter.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.
            included_updater_login_ids: Optional updater login IDs to include by exact match.
            excluded_updater_login_ids: Optional updater login IDs to exclude by exact match.

        Returns:
            Result of the operation.
        """
        pass

    async def get(self, biz_ctx_id: BizCtxId) -> BizCtxRow | None:
        """Repository contract for get.

        Args:
            biz_ctx_id: Business context identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def create(
        self,
        guid: str,
        name: str | None,
        created_by: AppUserId,
        last_updated_by: AppUserId,
        creation_timestamp: Any,
        last_update_timestamp: Any,
    ) -> BizCtxId:
        """Repository contract for create.

        Args:
            guid: Value for `guid`.
            name: Optional name filter.
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
        biz_ctx_id: BizCtxId,
        name: str | None,
        name_set: bool,
        last_updated_by: AppUserId,
        last_update_timestamp: Any,
    ) -> bool:
        """Repository contract for update.

        Args:
            biz_ctx_id: Business context identifier.
            name: Optional name filter.
            name_set: Value for `name_set`.
            last_updated_by: Identifier of the user who last updated the record.
            last_update_timestamp: Value for `last_update_timestamp`.

        Returns:
            Result of the operation.
        """
        pass

    async def delete(self, biz_ctx_id: BizCtxId) -> bool:
        """Repository contract for delete.

        Args:
            biz_ctx_id: Business context identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def create_value(
        self,
        biz_ctx_id: BizCtxId,
        ctx_scheme_value_id: CtxSchemeValueId,
    ) -> BizCtxValueId:
        """Repository contract for create value.

        Args:
            biz_ctx_id: Business context identifier.
            ctx_scheme_value_id: Context scheme value identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def update_value(
        self,
        biz_ctx_value_id: BizCtxValueId,
        ctx_scheme_value_id: CtxSchemeValueId,
        ctx_scheme_value_id_set: bool,
    ) -> bool:
        """Repository contract for update value.

        Args:
            biz_ctx_value_id: Business context value identifier.
            ctx_scheme_value_id: Context scheme value identifier.
            ctx_scheme_value_id_set: Value for `ctx_scheme_value_id_set`.

        Returns:
            Result of the operation.
        """
        pass

    async def get_value(self, biz_ctx_value_id: BizCtxValueId) -> BizCtxValueDetailRow | None:
        """Repository contract for get value.

        Args:
            biz_ctx_value_id: Business context value identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def delete_value(self, biz_ctx_value_id: BizCtxValueId) -> bool:
        """Repository contract for delete value.

        Args:
            biz_ctx_value_id: Business context value identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def ctx_scheme_value_exists(self, ctx_scheme_value_id: CtxSchemeValueId) -> bool:
        """Repository contract for ctx scheme value exists.

        Args:
            ctx_scheme_value_id: Context scheme value identifier.

        Returns:
            Result of the operation.
        """
        pass
