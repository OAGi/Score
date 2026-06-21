"""Service layer for Context Scheme operations."""


from __future__ import annotations

import logging
from datetime import datetime, timezone
from typing import Any

from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.repositories.contracts.ctx_scheme import CtxSchemeRepositoryContract
from app.repositories.models.app_user import AppUserRow
from app.security import AuthenticatedUser
from app.services import load_users_by_ids, to_user_summary
from app.services.models import WhoAndWhen
from app.services.models.ctx_category import ContextCategorySummaryServiceRecord
from app.services.models.ctx_scheme import (
    CtxSchemeServiceResult,
    CtxSchemeValueDetailServiceRecord,
    CtxSchemeValueServiceRecord,
)
from app.services.models.mapper import to_dataclass
from app.services.utils.date import DateRange
from app.services.utils.owner import parse_login_id_filter
from app.services.utils.pagination import PaginationParams, PaginationResponse
from app.services.utils.string import Guid, new_guid
from app.types.identifiers import CtxSchemeId, CtxSchemeValueId
from app.types.unset import UNSET, UnsetType

logger = logging.getLogger("connectcenter.service.ctx_scheme")


class CtxSchemeService:
    """Service for Context Scheme read and mutation operations."""

    _ORDER_BY_ALLOWED: set[str] = {
        "scheme_id",
        "scheme_name",
        "description",
        "scheme_agency_id",
        "scheme_version_id",
        "creation_timestamp",
        "last_update_timestamp",
    }

    def __init__(
        self,
        ctx_scheme_repository: CtxSchemeRepositoryContract,
        requester: AuthenticatedUser,
        account_service_repo: AppUserRepositoryContract | None = None,
    ):
        """Initialize service dependencies and requester context.

        Args:
            ctx_scheme_repository: Context-scheme repository dependency.
            requester: Requesting user used for authorization checks.
            account_service_repo: Account repository used to resolve user summaries.
        """
        self._repo = ctx_scheme_repository
        self._requester = requester
        self._account_service_repo = account_service_repo

    async def list(
        self,
        *,
        limit: int,
        offset: int,
        order_by: str | None = None,
        scheme_id: str | None = None,
        scheme_name: str | None = None,
        description: str | None = None,
        scheme_agency_id: str | None = None,
        scheme_version_id: str | None = None,
        ctx_category_id: int | None = None,
        ctx_category_name: str | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
        updater: str | None = None,
    ) -> PaginationResponse[CtxSchemeServiceResult]:
        """List context schemes with optional filters and pagination.

        Args:
            limit: Maximum number of items to return.
            offset: Zero-based index of the first item in the page.
            order_by: Sort expression for the result set.

        Returns:
            Result of the operation.
        """
        pagination = PaginationParams.from_query(
            limit=limit,
            offset=offset,
            order_by=order_by,
            allowed_sort_columns=self._ORDER_BY_ALLOWED,
        )
        logger.info("list ctx_schemes limit=%d offset=%d", limit, offset)
        included_updater_login_ids, excluded_updater_login_ids = parse_login_id_filter(
            updater,
            filter_name="updater",
        )
        total, rows = await self._repo.list(
            scheme_id=scheme_id,
            scheme_name=scheme_name,
            description=description,
            scheme_agency_id=scheme_agency_id,
            scheme_version_id=scheme_version_id,
            ctx_category_id=ctx_category_id,
            ctx_category_name=ctx_category_name,
            creation_timestamp_before=created_on.before if created_on else None,
            creation_timestamp_after=created_on.after if created_on else None,
            last_update_timestamp_before=last_updated_on.before if last_updated_on else None,
            last_update_timestamp_after=last_updated_on.after if last_updated_on else None,
            included_updater_login_ids=included_updater_login_ids,
            excluded_updater_login_ids=excluded_updater_login_ids,
            limit=pagination.limit,
            offset=pagination.offset,
            sorts=[(s.column, s.direction.upper()) for s in pagination.sorts],
        )
        user_ids = sorted(
            {user_id for row in rows for user_id in (row.created_by, row.last_updated_by)},
            key=int,
        )
        users_by_id = await load_users_by_ids(self._account_service_repo, user_ids)
        items = [self._to_ctx_scheme_result(row, users_by_id=users_by_id) for row in rows]
        logger.info("list ctx_schemes → %d/%d", len(items), total)
        return PaginationResponse(items=items, total=total, limit=pagination.limit, offset=pagination.offset)

    async def get(self, ctx_scheme_id: CtxSchemeId) -> CtxSchemeServiceResult | None:
        """Get context scheme detail by identifier.

        Args:
            ctx_scheme_id: Context scheme identifier.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get(ctx_scheme_id)
        if row is None:
            logger.info("get ctx_scheme id=%d → not found", int(ctx_scheme_id))
            return None
        users_by_id = await load_users_by_ids(self._account_service_repo, [row.created_by, row.last_updated_by])
        result = self._to_ctx_scheme_result(row, users_by_id=users_by_id)
        logger.info("get ctx_scheme id=%d → found", int(ctx_scheme_id))
        return result

    async def create(
        self,
        *,
        scheme_id: str,
        scheme_name: str,
        description: str | None = None,
        scheme_agency_id: str | None = None,
        scheme_version_id: str | None = None,
        ctx_category_id: int | None = None,
    ) -> CtxSchemeId:
        """Create a context scheme and return its identifier.

        Args:
            scheme_id: Optional context scheme identifier filter.
            scheme_name: Optional context scheme name filter.
            description: Optional textual description filter or payload field.
            scheme_agency_id: Optional context scheme agency identifier filter.
            scheme_version_id: Optional context scheme version identifier filter.
            ctx_category_id: Optional context category identifier filter.

        Returns:
            Result of the operation.
        """
        logger.info("create ctx_scheme scheme_id=%r scheme_name=%r", scheme_id, scheme_name)
        if not scheme_id or not scheme_id.strip():
            raise ValueError("Scheme ID is required and cannot be empty. Please provide a non-empty scheme ID and try again.")
        normalized_scheme_id = scheme_id.strip()
        if len(normalized_scheme_id) > 45:
            raise ValueError("Scheme ID cannot exceed 45 characters. Please shorten the scheme ID and try again.")

        if not scheme_name or not scheme_name.strip():
            raise ValueError("Scheme name is required and cannot be empty. Please provide a non-empty scheme name and try again.")
        normalized_scheme_name = scheme_name.strip()
        if len(normalized_scheme_name) > 255:
            raise ValueError("Scheme name cannot exceed 255 characters. Please shorten the scheme name and try again.")

        normalized_description = description.strip() if isinstance(description, str) else None
        if normalized_description and len(normalized_description) > 1000:
            raise ValueError("Description cannot exceed 1000 characters. Please shorten the description and try again.")

        normalized_agency_id = scheme_agency_id.strip() if isinstance(scheme_agency_id, str) else ""
        if normalized_agency_id and len(normalized_agency_id) > 45:
            raise ValueError("Scheme agency ID cannot exceed 45 characters. Please shorten the scheme agency ID and try again.")

        normalized_version_id = scheme_version_id.strip() if isinstance(scheme_version_id, str) else ""
        if normalized_version_id and len(normalized_version_id) > 45:
            raise ValueError("Scheme version ID cannot exceed 45 characters. Please shorten the scheme version ID and try again.")

        if ctx_category_id is not None and int(ctx_category_id) <= 0:
            raise ValueError("Context category ID must be a positive integer.")

        now = datetime.now(timezone.utc).replace(tzinfo=None)
        guid: Guid = new_guid()
        user_id = self._requester.user.user_id
        new_id = await self._repo.create(
            guid=guid,
            scheme_id=normalized_scheme_id,
            scheme_name=normalized_scheme_name,
            description=normalized_description,
            scheme_agency_id=normalized_agency_id,
            scheme_version_id=normalized_version_id,
            ctx_category_id=ctx_category_id,
            created_by=user_id,
            last_updated_by=user_id,
            creation_timestamp=now,
            last_update_timestamp=now,
        )
        logger.info("create ctx_scheme scheme_id=%r → id=%d", normalized_scheme_id, int(new_id))
        return new_id

    async def update(
        self,
        *,
        ctx_scheme_id: CtxSchemeId,
        scheme_id: str | UnsetType = UNSET,
        scheme_name: str | UnsetType = UNSET,
        description: str | None | UnsetType = UNSET,
        scheme_agency_id: str | UnsetType = UNSET,
        scheme_version_id: str | UnsetType = UNSET,
        ctx_category_id: int | None | UnsetType = UNSET,
    ) -> tuple[CtxSchemeId, list[str]] | None:
        """Update a context scheme and return changed fields.

        Args:
            ctx_scheme_id: Context scheme identifier.
            scheme_id: Optional context scheme identifier filter.
            scheme_name: Optional context scheme name filter.
            description: Optional textual description filter or payload field.
            scheme_agency_id: Optional context scheme agency identifier filter.
            scheme_version_id: Optional context scheme version identifier filter.
            ctx_category_id: Optional context category identifier filter.

        Returns:
            Result of the operation.
        """
        logger.info("update ctx_scheme id=%d", int(ctx_scheme_id))
        existing = await self.get(ctx_scheme_id)
        if existing is None:
            logger.warning("update ctx_scheme id=%d → not found", int(ctx_scheme_id))
            return None

        if scheme_id is not UNSET:
            if scheme_id is None or not str(scheme_id).strip():
                raise ValueError("Scheme ID cannot be empty. Please provide a non-empty scheme ID and try again.")
            if len(str(scheme_id).strip()) > 45:
                raise ValueError("Scheme ID cannot exceed 45 characters. Please shorten the scheme ID and try again.")

        if scheme_name is not UNSET:
            if scheme_name is None or not str(scheme_name).strip():
                raise ValueError("Scheme name cannot be empty. Please provide a non-empty scheme name and try again.")
            if len(str(scheme_name).strip()) > 255:
                raise ValueError("Scheme name cannot exceed 255 characters. Please shorten the scheme name and try again.")

        if description is not UNSET and description is not None and len(str(description)) > 1000:
            raise ValueError("Description cannot exceed 1000 characters. Please shorten the description and try again.")

        if scheme_agency_id is not UNSET and scheme_agency_id is not None and len(str(scheme_agency_id).strip()) > 45:
            raise ValueError("Scheme agency ID cannot exceed 45 characters. Please shorten the scheme agency ID and try again.")

        if scheme_version_id is not UNSET and scheme_version_id is not None and len(str(scheme_version_id).strip()) > 45:
            raise ValueError("Scheme version ID cannot exceed 45 characters. Please shorten the scheme version ID and try again.")

        if ctx_category_id is not UNSET and ctx_category_id is not None and int(ctx_category_id) <= 0:
            raise ValueError("Context category ID must be a positive integer.")

        normalized_scheme_id = scheme_id.strip() if isinstance(scheme_id, str) else scheme_id
        normalized_scheme_name = scheme_name.strip() if isinstance(scheme_name, str) else scheme_name
        normalized_description = description.strip() if isinstance(description, str) else description
        normalized_agency_id = scheme_agency_id.strip() if isinstance(scheme_agency_id, str) else scheme_agency_id
        normalized_version_id = scheme_version_id.strip() if isinstance(scheme_version_id, str) else scheme_version_id

        current_category_id = int(existing.ctx_category.ctx_category_id) if existing.ctx_category is not None else None

        updates: list[str] = []
        if normalized_scheme_id is not UNSET and normalized_scheme_id != existing.scheme_id:
            updates.append("scheme_id")
        if normalized_scheme_name is not UNSET and normalized_scheme_name != existing.scheme_name:
            updates.append("scheme_name")
        if normalized_description is not UNSET and normalized_description != existing.description:
            updates.append("description")
        if normalized_agency_id is not UNSET and str(normalized_agency_id or "") != existing.scheme_agency_id:
            updates.append("scheme_agency_id")
        if normalized_version_id is not UNSET and str(normalized_version_id or "") != existing.scheme_version_id:
            updates.append("scheme_version_id")
        if ctx_category_id is not UNSET and (None if ctx_category_id is None else int(ctx_category_id)) != current_category_id:
            updates.append("ctx_category_id")

        if not updates:
            logger.warning("update ctx_scheme id=%d → no changes", int(ctx_scheme_id))
            return (ctx_scheme_id, [])

        now = datetime.now(timezone.utc).replace(tzinfo=None)
        ok = await self._repo.update(
            ctx_scheme_id=ctx_scheme_id,
            scheme_id=None if normalized_scheme_id is UNSET else str(normalized_scheme_id),
            scheme_id_set=normalized_scheme_id is not UNSET,
            scheme_name=None if normalized_scheme_name is UNSET else str(normalized_scheme_name),
            scheme_name_set=normalized_scheme_name is not UNSET,
            description=None if normalized_description is UNSET else normalized_description,
            description_set=normalized_description is not UNSET,
            scheme_agency_id=None if normalized_agency_id is UNSET else str(normalized_agency_id or ""),
            scheme_agency_id_set=normalized_agency_id is not UNSET,
            scheme_version_id=None if normalized_version_id is UNSET else str(normalized_version_id or ""),
            scheme_version_id_set=normalized_version_id is not UNSET,
            ctx_category_id=None if ctx_category_id is UNSET else ctx_category_id,
            ctx_category_id_set=ctx_category_id is not UNSET,
            last_updated_by=self._requester.user.user_id,
            last_update_timestamp=now,
        )
        if not ok:
            logger.warning("update ctx_scheme id=%d → repo returned not found", int(ctx_scheme_id))
            return None
        logger.info("update ctx_scheme id=%d → %s", int(ctx_scheme_id), sorted(updates))
        return (ctx_scheme_id, sorted(updates))

    async def delete(self, ctx_scheme_id: CtxSchemeId) -> bool:
        """Delete a context scheme by identifier.

        Args:
            ctx_scheme_id: Context scheme identifier.

        Returns:
            Result of the operation.
        """
        logger.info("delete ctx_scheme id=%d", int(ctx_scheme_id))
        existing = await self.get(ctx_scheme_id)
        if existing is None:
            logger.info("delete ctx_scheme id=%d → not found", int(ctx_scheme_id))
            return False
        if existing.ctx_category is not None:
            raise ValueError(
                "Cannot delete context scheme because it is still linked to context category "
                f"ID {int(existing.ctx_category.ctx_category_id)} ({existing.ctx_category.name}). "
                "Remove the context category reference first."
            )
        linked_biz_ctx_ids: set[int] = set()
        for value in existing.values:
            linked = await self._repo.get_biz_ctx_ids_using_ctx_scheme_value(value.ctx_scheme_value_id)
            linked_biz_ctx_ids.update(int(x) for x in linked)
        if linked_biz_ctx_ids:
            joined = ", ".join(str(x) for x in sorted(linked_biz_ctx_ids))
            raise ValueError(
                "Cannot delete context scheme because one or more context scheme values are referenced by business contexts: "
                f"{joined}. Remove those references first."
            )
        deleted = await self._repo.delete(ctx_scheme_id)
        logger.info("delete ctx_scheme id=%d → deleted", int(ctx_scheme_id))
        return deleted

    async def create_value(
        self,
        *,
        owner_ctx_scheme_id: CtxSchemeId,
        value: str,
        meaning: str | None = None,
    ) -> CtxSchemeValueId:
        """Create a context scheme value and return its identifier.

        Args:
            owner_ctx_scheme_id: Owner context scheme identifier.
            value: Context value string.
            meaning: Context value meaning/description.

        Returns:
            Result of the operation.
        """
        logger.info("create ctx_scheme_value owner_ctx_scheme_id=%d value=%r", int(owner_ctx_scheme_id), value)
        scheme = await self._repo.get(owner_ctx_scheme_id)
        if scheme is None:
            raise ValueError(f"No context scheme exists with ID {int(owner_ctx_scheme_id)}.")

        if not value or not value.strip():
            raise ValueError("Value is required and cannot be empty.")
        normalized_value = value.strip()
        if len(normalized_value) > 100:
            raise ValueError("Value cannot exceed 100 characters.")

        normalized_meaning = meaning.strip() if isinstance(meaning, str) else None
        if normalized_meaning and len(normalized_meaning) > 1000:
            raise ValueError("Meaning cannot exceed 1000 characters.")

        guid: Guid = new_guid()
        new_id = await self._repo.create_value(
            guid=guid,
            owner_ctx_scheme_id=owner_ctx_scheme_id,
            value=normalized_value,
            meaning=normalized_meaning,
        )
        logger.info("create ctx_scheme_value owner_ctx_scheme_id=%d → id=%d", int(owner_ctx_scheme_id), int(new_id))
        return new_id

    async def get_value(self, ctx_scheme_value_id: CtxSchemeValueId) -> CtxSchemeValueDetailServiceRecord | None:
        """Get context scheme value detail by identifier."""

        row = await self._repo.get_value(ctx_scheme_value_id)
        if row is None:
            logger.info("get ctx_scheme_value id=%d → not found", int(ctx_scheme_value_id))
            return None
        logger.info("get ctx_scheme_value id=%d → found", int(ctx_scheme_value_id))
        return CtxSchemeValueDetailServiceRecord(
            ctx_scheme_value_id=row.ctx_scheme_value_id,
            guid=row.guid,
            owner_ctx_scheme_id=row.owner_ctx_scheme_id,
            value=row.value,
            meaning=row.meaning,
        )

    async def update_value(
        self,
        *,
        ctx_scheme_id: CtxSchemeId,
        ctx_scheme_value_id: CtxSchemeValueId,
        value: str | UnsetType = UNSET,
        meaning: str | None | UnsetType = UNSET,
    ) -> tuple[CtxSchemeValueId, list[str]] | None:
        """Update a context scheme value and return changed fields.

        Args:
            ctx_scheme_id: Context scheme identifier from URL.
            ctx_scheme_value_id: Context scheme value identifier.
            value: Context value string.
            meaning: Context value meaning/description.

        Returns:
            Result of the operation.
        """
        logger.info("update ctx_scheme_value id=%d ctx_scheme_id=%d", int(ctx_scheme_value_id), int(ctx_scheme_id))
        existing = await self._repo.get_value(ctx_scheme_value_id)
        if existing is None:
            logger.warning("update ctx_scheme_value id=%d → not found", int(ctx_scheme_value_id))
            return None
        if int(existing.owner_ctx_scheme_id) != int(ctx_scheme_id):
            raise ValueError(
                "The context scheme value does not belong to the specified context scheme. "
                f"owner_ctx_scheme_id={int(existing.owner_ctx_scheme_id)}, ctx_scheme_id={int(ctx_scheme_id)}."
            )

        if value is not UNSET:
            if value is None or not str(value).strip():
                raise ValueError("Value cannot be empty.")
            if len(str(value).strip()) > 100:
                raise ValueError("Value cannot exceed 100 characters.")

        if meaning is not UNSET and meaning is not None and len(str(meaning)) > 1000:
            raise ValueError("Meaning cannot exceed 1000 characters.")

        normalized_value = value.strip() if isinstance(value, str) else value
        normalized_meaning = meaning.strip() if isinstance(meaning, str) else meaning

        updates: list[str] = []
        if normalized_value is not UNSET and str(normalized_value) != str(existing.value):
            updates.append("value")
        if normalized_meaning is not UNSET and normalized_meaning != existing.meaning:
            updates.append("meaning")

        if not updates:
            logger.warning("update ctx_scheme_value id=%d → no changes", int(ctx_scheme_value_id))
            return (ctx_scheme_value_id, [])

        ok = await self._repo.update_value(
            ctx_scheme_value_id=ctx_scheme_value_id,
            value=None if normalized_value is UNSET else str(normalized_value),
            value_set=normalized_value is not UNSET,
            meaning=None if normalized_meaning is UNSET else normalized_meaning,
            meaning_set=normalized_meaning is not UNSET,
        )
        if not ok:
            logger.warning("update ctx_scheme_value id=%d → repo returned not found", int(ctx_scheme_value_id))
            return None
        logger.info("update ctx_scheme_value id=%d → %s", int(ctx_scheme_value_id), sorted(updates))
        return (ctx_scheme_value_id, sorted(updates))

    async def delete_value(self, ctx_scheme_id: CtxSchemeId, ctx_scheme_value_id: CtxSchemeValueId) -> bool:
        """Delete a context scheme value, guarding against linked business contexts.

        Args:
            ctx_scheme_id: Context scheme identifier from URL.
            ctx_scheme_value_id: Context scheme value identifier.

        Returns:
            Result of the operation.
        """
        logger.info("delete ctx_scheme_value id=%d ctx_scheme_id=%d", int(ctx_scheme_value_id), int(ctx_scheme_id))
        existing = await self._repo.get_value(ctx_scheme_value_id)
        if existing is None:
            logger.info("delete ctx_scheme_value id=%d → not found", int(ctx_scheme_value_id))
            return False
        if int(existing.owner_ctx_scheme_id) != int(ctx_scheme_id):
            raise ValueError(
                "The context scheme value does not belong to the specified context scheme. "
                f"owner_ctx_scheme_id={int(existing.owner_ctx_scheme_id)}, ctx_scheme_id={int(ctx_scheme_id)}."
            )

        linked = await self._repo.get_biz_ctx_ids_using_ctx_scheme_value(ctx_scheme_value_id)
        if linked:
            joined = ", ".join(str(int(x)) for x in linked)
            raise ValueError(
                "Cannot delete context scheme value because it is referenced by business contexts: "
                f"{joined}. Remove those references first."
            )

        deleted = await self._repo.delete_value(ctx_scheme_value_id)
        logger.info("delete ctx_scheme_value id=%d → deleted", int(ctx_scheme_value_id))
        return deleted

    async def update_value_by_id(
        self,
        *,
        ctx_scheme_value_id: CtxSchemeValueId,
        value: str | UnsetType = UNSET,
        meaning: str | None | UnsetType = UNSET,
    ) -> tuple[CtxSchemeValueId, list[str]] | None:
        """Update a context scheme value by value identifier only."""

        existing = await self.get_value(ctx_scheme_value_id)
        if existing is None:
            return None
        return await self.update_value(
            ctx_scheme_id=existing.owner_ctx_scheme_id,
            ctx_scheme_value_id=ctx_scheme_value_id,
            value=value,
            meaning=meaning,
        )

    async def delete_value_by_id(self, ctx_scheme_value_id: CtxSchemeValueId) -> bool:
        """Delete a context scheme value by value identifier only."""

        existing = await self.get_value(ctx_scheme_value_id)
        if existing is None:
            return False
        return await self.delete_value(existing.owner_ctx_scheme_id, ctx_scheme_value_id)

    def _to_ctx_scheme_result(
        self,
        row: Any,
        *,
        users_by_id: dict[int, AppUserRow],
    ) -> CtxSchemeServiceResult:
        """Map repository row to Context Scheme DTO.

        Args:
            row: Repository row model to convert into a DTO.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Result of the operation.
        """
        return CtxSchemeServiceResult(
            ctx_scheme_id=row.ctx_scheme_id,
            guid=row.guid,
            scheme_id=row.scheme_id,
            scheme_name=row.scheme_name,
            description=row.description,
            scheme_agency_id=row.scheme_agency_id,
            scheme_version_id=row.scheme_version_id,
            ctx_category=(
                to_dataclass(ContextCategorySummaryServiceRecord, row.ctx_category)
                if row.ctx_category is not None
                else None
            ),
            values=[to_dataclass(CtxSchemeValueServiceRecord, item) for item in row.values],
            created=WhoAndWhen(
                who=to_user_summary(int(row.created_by), users_by_id=users_by_id),
                when=row.creation_timestamp,
            ),
            last_updated=WhoAndWhen(
                who=to_user_summary(int(row.last_updated_by), users_by_id=users_by_id),
                when=row.last_update_timestamp,
            ),
        )
