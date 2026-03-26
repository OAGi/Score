"""Repository contract for Context Scheme persistence.

Defines the async interface for CRUD operations on schemes and scheme values,
plus lookup helpers used for deletion constraints.
"""

from __future__ import annotations

from datetime import datetime
from typing import Any, Literal
from typing import Protocol

from app.repositories.models.ctx_scheme import CtxSchemeRow, CtxSchemeValueRow
from app.types.identifiers import AppUserId, BizCtxId, ContextCategoryId, CtxSchemeId, CtxSchemeValueId


class CtxSchemeRepositoryContract(Protocol):
    """Protocol for context scheme repository implementations."""
    async def list(
        self,
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        scheme_id: str | None = None,
        scheme_name: str | None = None,
        description: str | None = None,
        scheme_agency_id: str | None = None,
        scheme_version_id: str | None = None,
        ctx_category_id: ContextCategoryId | None = None,
        ctx_category_name: str | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
    ) -> tuple[int, list[CtxSchemeRow]]:
        """Repository contract for list.

        Args:
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            scheme_id: Optional context scheme identifier filter.
            scheme_name: Optional context scheme name filter.
            description: Optional textual description filter or payload field.
            scheme_agency_id: Optional context scheme agency identifier filter.
            scheme_version_id: Optional context scheme version identifier filter.
            ctx_category_id: Optional context category identifier filter.
            ctx_category_name: Optional context category name filter.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

        Returns:
            Result of the operation.
        """
        pass

    async def get(self, ctx_scheme_id: CtxSchemeId) -> CtxSchemeRow | None:
        """Repository contract for get.

        Args:
            ctx_scheme_id: Context scheme identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def create(
        self,
        guid: str,
        scheme_id: str,
        scheme_name: str,
        description: str | None,
        scheme_agency_id: str,
        scheme_version_id: str,
        ctx_category_id: ContextCategoryId | None,
        created_by: AppUserId,
        last_updated_by: AppUserId,
        creation_timestamp: Any,
        last_update_timestamp: Any,
    ) -> CtxSchemeId:
        """Repository contract for create.

        Args:
            guid: Value for `guid`.
            scheme_id: Optional context scheme identifier filter.
            scheme_name: Optional context scheme name filter.
            description: Optional textual description filter or payload field.
            scheme_agency_id: Optional context scheme agency identifier filter.
            scheme_version_id: Optional context scheme version identifier filter.
            ctx_category_id: Optional context category identifier filter.
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
        ctx_scheme_id: CtxSchemeId,
        scheme_id: str | None,
        scheme_id_set: bool,
        scheme_name: str | None,
        scheme_name_set: bool,
        description: str | None,
        description_set: bool,
        scheme_agency_id: str | None,
        scheme_agency_id_set: bool,
        scheme_version_id: str | None,
        scheme_version_id_set: bool,
        ctx_category_id: ContextCategoryId | None,
        ctx_category_id_set: bool,
        last_updated_by: AppUserId,
        last_update_timestamp: Any,
    ) -> bool:
        """Repository contract for update.

        Args:
            ctx_scheme_id: Context scheme identifier.
            scheme_id: Optional context scheme identifier filter.
            scheme_id_set: Value for `scheme_id_set`.
            scheme_name: Optional context scheme name filter.
            scheme_name_set: Value for `scheme_name_set`.
            description: Optional textual description filter or payload field.
            description_set: Value for `description_set`.
            scheme_agency_id: Optional context scheme agency identifier filter.
            scheme_agency_id_set: Value for `scheme_agency_id_set`.
            scheme_version_id: Optional context scheme version identifier filter.
            scheme_version_id_set: Value for `scheme_version_id_set`.
            ctx_category_id: Optional context category identifier filter.
            ctx_category_id_set: Value for `ctx_category_id_set`.
            last_updated_by: Identifier of the user who last updated the record.
            last_update_timestamp: Value for `last_update_timestamp`.

        Returns:
            Result of the operation.
        """
        pass

    async def delete(self, ctx_scheme_id: CtxSchemeId) -> bool:
        """Repository contract for delete.

        Args:
            ctx_scheme_id: Context scheme identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def create_value(
        self,
        guid: str,
        owner_ctx_scheme_id: CtxSchemeId,
        value: str,
        meaning: str | None,
    ) -> CtxSchemeValueId:
        """Repository contract for create value.

        Args:
            guid: Value for `guid`.
            owner_ctx_scheme_id: Owner context scheme identifier.
            value: Context value string.
            meaning: Context value meaning/description.

        Returns:
            Result of the operation.
        """
        pass

    async def get_value(self, ctx_scheme_value_id: CtxSchemeValueId) -> CtxSchemeValueRow | None:
        """Repository contract for get value.

        Args:
            ctx_scheme_value_id: Context scheme value identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def update_value(
        self,
        ctx_scheme_value_id: CtxSchemeValueId,
        value: str | None,
        value_set: bool,
        meaning: str | None,
        meaning_set: bool,
    ) -> bool:
        """Repository contract for update value.

        Args:
            ctx_scheme_value_id: Context scheme value identifier.
            value: Context value string.
            value_set: Value for `value_set`.
            meaning: Context value meaning/description.
            meaning_set: Value for `meaning_set`.

        Returns:
            Result of the operation.
        """
        pass

    async def delete_value(self, ctx_scheme_value_id: CtxSchemeValueId) -> bool:
        """Repository contract for delete value.

        Args:
            ctx_scheme_value_id: Context scheme value identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def get_biz_ctx_ids_using_ctx_scheme_value(self, ctx_scheme_value_id: CtxSchemeValueId) -> list[BizCtxId]:
        """Repository contract for get biz ctx ids using ctx scheme value.

        Args:
            ctx_scheme_value_id: Context scheme value identifier.

        Returns:
            Result of the operation.
        """
        pass
