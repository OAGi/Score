"""Service layer for Context Category operations."""


from __future__ import annotations

import logging
from datetime import datetime, timezone
from typing import Any

from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.repositories.contracts.ctx_category import ContextCategoryRepositoryContract
from app.repositories.models.app_user import AppUserRow
from app.security import AuthenticatedUser
from app.services import load_users_by_ids, to_user_summary
from app.services.models import WhoAndWhen
from app.services.models.ctx_category import ContextCategoryServiceResult
from app.services.utils.date import DateRange
from app.services.utils.owner import parse_login_id_filter
from app.services.utils.pagination import PaginationParams, PaginationResponse
from app.services.utils.string import Guid, new_guid
from app.types.identifiers import ContextCategoryId
from app.types.unset import UNSET, UnsetType

logger = logging.getLogger("connectcenter.service.ctx_category")


class ContextCategoryService:
    """Service for Context Category read and mutation operations."""

    _ORDER_BY_ALLOWED: set[str] = {"name", "description", "creation_timestamp", "last_update_timestamp"}

    def __init__(
        self,
        context_category_repository: ContextCategoryRepositoryContract,
        requester: AuthenticatedUser,
        account_service_repo: AppUserRepositoryContract | None = None,
    ):
        """Initialize service dependencies and requester context.

        Args:
            context_category_repository: Value for `context_category_repository`.
            requester: Requesting user used for authorization checks.
            account_service_repo: Account repository used to resolve user summaries.
        """
        self._repo = context_category_repository
        self._requester = requester
        self._account_service_repo = account_service_repo

    async def list(
        self,
        *,
        limit: int,
        offset: int,
        order_by: str | None = None,
        name: str | None = None,
        description: str | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
        updater: str | None = None,
    ) -> PaginationResponse[ContextCategoryServiceResult]:
        """List context categories with optional filters and pagination.

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
        logger.info("list ctx_categories limit=%d offset=%d", limit, offset)
        included_updater_login_ids, excluded_updater_login_ids = parse_login_id_filter(
            updater,
            filter_name="updater",
        )
        total, rows = await self._repo.list(
            name=name,
            description=description,
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
        items = [self._to_context_category_result(row, users_by_id=users_by_id) for row in rows]
        logger.info("list ctx_categories → %d/%d", len(items), total)
        return PaginationResponse(items=items, total=total, limit=pagination.limit, offset=pagination.offset)

    async def get(self, context_category_id: ContextCategoryId) -> ContextCategoryServiceResult | None:
        """Get context category detail by identifier.

        Args:
            context_category_id: Context category identifier.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get(context_category_id)
        if row is None:
            logger.info("get ctx_category id=%d → not found", int(context_category_id))
            return None
        users_by_id = await load_users_by_ids(self._account_service_repo, [row.created_by, row.last_updated_by])
        result = self._to_context_category_result(row, users_by_id=users_by_id)
        logger.info("get ctx_category id=%d → found", int(context_category_id))
        return result

    async def create(self, *, name: str, description: str | None) -> ContextCategoryId:
        """Create a context category and return its identifier.

        Args:
            name: Optional name filter.
            description: Optional textual description filter or payload field.

        Returns:
            Result of the operation.
        """
        logger.info("create ctx_category name=%r", name)
        if not name or not name.strip():
            raise ValueError("Name is required and cannot be empty. Please provide a non-empty name and try again.")
        normalized_name = name.strip()
        if len(normalized_name) > 45:
            raise ValueError("Name cannot exceed 45 characters. Please shorten the name and try again.")

        normalized_description = description.strip() if isinstance(description, str) else None
        if normalized_description and len(normalized_description) > 1000:
            raise ValueError("Description cannot exceed 1000 characters. Please shorten the description and try again.")

        now = datetime.now(timezone.utc).replace(tzinfo=None)
        guid: Guid = new_guid()
        user_id = self._requester.user.user_id
        ctx_category_id = await self._repo.create(
            guid=guid,
            name=normalized_name,
            description=normalized_description,
            created_by=user_id,
            last_updated_by=user_id,
            creation_timestamp=now,
            last_update_timestamp=now,
        )
        logger.info("create ctx_category name=%r → id=%d", normalized_name, int(ctx_category_id))
        return ctx_category_id

    async def update(
        self,
        *,
        context_category_id: ContextCategoryId,
        name: str | None | UnsetType = UNSET,
        description: str | None | UnsetType = UNSET,
    ) -> tuple[ContextCategoryId, list[str]] | None:
        """Update a context category and return changed fields.

        Args:
            context_category_id: Context category identifier.
            name: Optional name filter.
            description: Optional textual description filter or payload field.

        Returns:
            Result of the operation.
        """
        logger.info("update ctx_category id=%d", int(context_category_id))
        existing = await self.get(context_category_id)
        if existing is None:
            logger.warning("update ctx_category id=%d → not found", int(context_category_id))
            return None

        if name is not UNSET:
            if name is not None and not str(name).strip():
                raise ValueError("Name cannot be empty. Please provide a non-empty name and try again.")
            if name is not None and len(str(name).strip()) > 45:
                raise ValueError("Name cannot exceed 45 characters. Please shorten the name and try again.")

        if description is not UNSET and description is not None and len(str(description)) > 1000:
            raise ValueError("Description cannot exceed 1000 characters. Please shorten the description and try again.")

        updates: list[str] = []
        normalized_name = name.strip() if name is not UNSET and isinstance(name, str) else name
        normalized_description = description.strip() if description is not UNSET and isinstance(description, str) else description

        if normalized_name is not UNSET and normalized_name != existing.name:
            updates.append("name")
        if normalized_description is not UNSET and normalized_description != existing.description:
            updates.append("description")

        if not updates:
            logger.warning("update ctx_category id=%d → no changes", int(context_category_id))
            return (context_category_id, [])

        now = datetime.now(timezone.utc).replace(tzinfo=None)
        ok = await self._repo.update(
            context_category_id=context_category_id,
            name=None if normalized_name is UNSET else normalized_name,
            name_set=normalized_name is not UNSET,
            description=None if normalized_description is UNSET else normalized_description,
            description_set=normalized_description is not UNSET,
            last_updated_by=self._requester.user.user_id,
            last_update_timestamp=now,
        )
        if not ok:
            logger.warning("update ctx_category id=%d → repo returned not found", int(context_category_id))
            return None
        logger.info("update ctx_category id=%d → %s", int(context_category_id), sorted(updates))
        return (context_category_id, sorted(updates))

    async def delete(self, context_category_id: ContextCategoryId) -> bool:
        """Delete a context category by identifier.

        Args:
            context_category_id: Context category identifier.

        Returns:
            Result of the operation.
        """
        deleted = await self._repo.delete(context_category_id)
        logger.info("delete ctx_category id=%d → %s", int(context_category_id), "deleted" if deleted else "not found")
        return deleted

    def _to_context_category_result(
        self,
        row: Any,
        *,
        users_by_id: dict[int, AppUserRow],
    ) -> ContextCategoryServiceResult:
        """Map repository row to Context Category DTO.

        Args:
            row: Repository row model to convert into a DTO.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Result of the operation.
        """
        return ContextCategoryServiceResult(
            ctx_category_id=row.ctx_category_id,
            guid=row.guid,
            name=row.name,
            description=row.description,
            created=WhoAndWhen(
                who=to_user_summary(int(row.created_by), users_by_id=users_by_id),
                when=row.creation_timestamp,
            ),
            last_updated=WhoAndWhen(
                who=to_user_summary(int(row.last_updated_by), users_by_id=users_by_id),
                when=row.last_update_timestamp,
            ),
        )
