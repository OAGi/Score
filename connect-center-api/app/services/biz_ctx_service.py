"""Service layer for Business Context operations."""


from __future__ import annotations

import logging
from datetime import datetime, timezone
from typing import Any

from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.repositories.contracts.biz_ctx import BizCtxRepositoryContract
from app.repositories.models.app_user import AppUserRow
from app.security import AuthenticatedUser
from app.services import load_users_by_ids, to_user_summary
from app.services.models import WhoAndWhen
from app.services.models.biz_ctx import BizCtxServiceResult, BizCtxValueDetailServiceRecord, BizCtxValueServiceRecord
from app.services.models.ctx_scheme import CtxSchemeValueSummaryServiceRecord
from app.services.models.mapper import to_dataclass
from app.services.utils.date import DateRange
from app.services.utils.owner import parse_login_id_filter
from app.services.utils.pagination import PaginationParams, PaginationResponse
from app.services.utils.string import Guid, new_guid
from app.types.identifiers import BizCtxId, BizCtxValueId
from app.types.identifiers import CtxSchemeValueId
from app.types.unset import UNSET, UnsetType

logger = logging.getLogger("connectcenter.service.biz_ctx")


class BizCtxService:
    """Service for Business Context read and mutation operations."""

    _ORDER_BY_ALLOWED: set[str] = {"name", "creation_timestamp", "last_update_timestamp"}

    def __init__(
        self,
        biz_ctx_repository: BizCtxRepositoryContract,
        requester: AuthenticatedUser,
        account_service_repo: AppUserRepositoryContract | None = None,
    ):
        """Initialize service dependencies and requester context.

        Args:
            biz_ctx_repository: Business-context repository dependency.
            requester: Requesting user used for authorization checks.
            account_service_repo: Account repository used to resolve user summaries.
        """
        self._repo = biz_ctx_repository
        self._requester = requester
        self._account_service_repo = account_service_repo

    async def list(
        self,
        *,
        limit: int,
        offset: int,
        order_by: str | None = None,
        name: str | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
        updater: str | None = None,
    ) -> PaginationResponse[BizCtxServiceResult]:
        """List business contexts with optional filters and pagination.

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
        logger.info("list biz_ctxs limit=%d offset=%d", limit, offset)
        included_updater_login_ids, excluded_updater_login_ids = parse_login_id_filter(
            updater,
            filter_name="updater",
        )
        total, rows = await self._repo.list(
            name=name,
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
        items = [self._to_biz_ctx_result(row, users_by_id=users_by_id) for row in rows]
        logger.info("list biz_ctxs → %d/%d", len(items), total)
        return PaginationResponse(items=items, total=total, limit=pagination.limit, offset=pagination.offset)

    async def get(self, biz_ctx_id: BizCtxId) -> BizCtxServiceResult | None:
        """Get business context detail by identifier.

        Args:
            biz_ctx_id: Business context identifier.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get(biz_ctx_id)
        if row is None:
            logger.info("get biz_ctx id=%d → not found", int(biz_ctx_id))
            return None
        users_by_id = await load_users_by_ids(self._account_service_repo, [row.created_by, row.last_updated_by])
        result = self._to_biz_ctx_result(row, users_by_id=users_by_id)
        logger.info("get biz_ctx id=%d → found", int(biz_ctx_id))
        return result

    async def create(self, *, name: str) -> BizCtxId:
        """Create a business context and return its identifier.

        Args:
            name: Name of the business context.

        Returns:
            Result of the operation.
        """
        logger.info("create biz_ctx name=%r", name)
        normalized_name = name.strip()
        if not normalized_name:
            raise ValueError(
                "Business context name is required and cannot be empty. Please provide a non-empty name and try again."
            )
        if len(normalized_name) > 100:
            raise ValueError("Business context name cannot exceed 100 characters. Please shorten the name and try again.")

        now = datetime.now(timezone.utc).replace(tzinfo=None)
        guid: Guid = new_guid()
        user_id = self._requester.user.user_id
        new_id = await self._repo.create(
            guid=guid,
            name=normalized_name,
            created_by=user_id,
            last_updated_by=user_id,
            creation_timestamp=now,
            last_update_timestamp=now,
        )
        logger.info("create biz_ctx name=%r → id=%d", normalized_name, int(new_id))
        return new_id

    async def update(
        self,
        *,
        biz_ctx_id: BizCtxId,
        name: str | None | UnsetType = UNSET,
    ) -> tuple[BizCtxId, list[str]] | None:
        """Update a business context and return changed fields.

        Args:
            biz_ctx_id: Business context identifier.
            name: Optional name filter.

        Returns:
            Result of the operation.
        """
        logger.info("update biz_ctx id=%d", int(biz_ctx_id))
        existing = await self.get(biz_ctx_id)
        if existing is None:
            logger.warning("update biz_ctx id=%d → not found", int(biz_ctx_id))
            return None

        if name is not UNSET and name is not None and len(str(name).strip()) > 100:
            raise ValueError("Business context name cannot exceed 100 characters. Please shorten the name and try again.")

        normalized_name = name
        if name is not UNSET and isinstance(name, str):
            normalized_name = name.strip() or None

        updates: list[str] = []
        if normalized_name is not UNSET and normalized_name != existing.name:
            updates.append("name")

        if not updates:
            logger.warning("update biz_ctx id=%d → no changes", int(biz_ctx_id))
            return (biz_ctx_id, [])

        now = datetime.now(timezone.utc).replace(tzinfo=None)
        ok = await self._repo.update(
            biz_ctx_id=biz_ctx_id,
            name=None if normalized_name is UNSET else normalized_name,
            name_set=normalized_name is not UNSET,
            last_updated_by=self._requester.user.user_id,
            last_update_timestamp=now,
        )
        if not ok:
            logger.warning("update biz_ctx id=%d → repo returned not found", int(biz_ctx_id))
            return None
        logger.info("update biz_ctx id=%d → %s", int(biz_ctx_id), sorted(updates))
        return (biz_ctx_id, sorted(updates))

    async def delete(self, biz_ctx_id: BizCtxId) -> bool:
        """Delete a business context by identifier.

        Args:
            biz_ctx_id: Business context identifier.

        Returns:
            Result of the operation.
        """
        deleted = await self._repo.delete(biz_ctx_id)
        logger.info("delete biz_ctx id=%d → %s", int(biz_ctx_id), "deleted" if deleted else "not found")
        return deleted

    async def create_value(self, *, biz_ctx_id: BizCtxId, ctx_scheme_value_id: CtxSchemeValueId) -> BizCtxValueId:
        """Create a business-context value reference.

        Args:
            biz_ctx_id: Business context identifier.
            ctx_scheme_value_id: Context scheme value identifier.

        Returns:
            Result of the operation.
        """
        logger.info("create biz_ctx_value biz_ctx_id=%d ctx_scheme_value_id=%d", int(biz_ctx_id), int(ctx_scheme_value_id))
        biz_ctx = await self._repo.get(biz_ctx_id)
        if biz_ctx is None:
            raise ValueError(
                f"No business context exists with ID {int(biz_ctx_id)}. Please verify the identifier and try again."
            )

        if int(ctx_scheme_value_id) <= 0:
            raise ValueError("Context scheme value ID must be a positive integer. Please provide a valid ID and try again.")

        if not await self._repo.ctx_scheme_value_exists(ctx_scheme_value_id):
            raise ValueError(
                f"No context scheme value exists with ID {int(ctx_scheme_value_id)}. Please verify the identifier and try again."
            )

        new_id = await self._repo.create_value(biz_ctx_id=biz_ctx_id, ctx_scheme_value_id=ctx_scheme_value_id)
        logger.info("create biz_ctx_value biz_ctx_id=%d → id=%d", int(biz_ctx_id), int(new_id))
        return new_id

    async def get_value(self, biz_ctx_value_id: BizCtxValueId) -> BizCtxValueDetailServiceRecord | None:
        """Get business-context value detail by identifier."""

        row = await self._repo.get_value(biz_ctx_value_id)
        if row is None:
            logger.info("get biz_ctx_value id=%d → not found", int(biz_ctx_value_id))
            return None
        logger.info("get biz_ctx_value id=%d → found", int(biz_ctx_value_id))
        return BizCtxValueDetailServiceRecord(
            biz_ctx_value_id=row.biz_ctx_value_id,
            biz_ctx_id=row.biz_ctx_id,
            ctx_scheme_value_id=row.ctx_scheme_value_id,
        )

    async def update_value(
        self,
        *,
        biz_ctx_id: BizCtxId,
        biz_ctx_value_id: BizCtxValueId,
        ctx_scheme_value_id: CtxSchemeValueId | UnsetType = UNSET,
    ) -> tuple[BizCtxValueId, list[str]] | None:
        """Update a business-context value reference.

        Args:
            biz_ctx_id: Business context identifier.
            biz_ctx_value_id: Business context value identifier.
            ctx_scheme_value_id: Context scheme value identifier.

        Returns:
            Result of the operation.
        """
        logger.info("update biz_ctx_value id=%d biz_ctx_id=%d", int(biz_ctx_value_id), int(biz_ctx_id))
        if ctx_scheme_value_id is UNSET:
            logger.warning("update biz_ctx_value id=%d → no changes", int(biz_ctx_value_id))
            return (biz_ctx_value_id, [])

        existing = await self._repo.get_value(biz_ctx_value_id)
        if existing is None:
            logger.warning("update biz_ctx_value id=%d → not found", int(biz_ctx_value_id))
            return None
        if int(existing.biz_ctx_id) != int(biz_ctx_id):
            raise ValueError(
                f"Business context value ID {int(biz_ctx_value_id)} does not belong to business context ID {int(biz_ctx_id)}. "
                "Please use the correct business context value for this business context and try again."
            )

        if int(ctx_scheme_value_id) <= 0:
            raise ValueError("Context scheme value ID must be a positive integer. Please provide a valid ID and try again.")

        if not await self._repo.ctx_scheme_value_exists(ctx_scheme_value_id):
            raise ValueError(
                f"No context scheme value exists with ID {int(ctx_scheme_value_id)}. Please verify the identifier and try again."
            )

        ok = await self._repo.update_value(
            biz_ctx_value_id=biz_ctx_value_id,
            ctx_scheme_value_id=ctx_scheme_value_id,
            ctx_scheme_value_id_set=True,
        )
        if not ok:
            logger.warning("update biz_ctx_value id=%d → repo returned not found", int(biz_ctx_value_id))
            return None
        logger.info("update biz_ctx_value id=%d → ctx_scheme_value_id", int(biz_ctx_value_id))
        return (biz_ctx_value_id, ["ctx_scheme_value_id"])

    async def delete_value(self, biz_ctx_id: BizCtxId, biz_ctx_value_id: BizCtxValueId) -> bool:
        """Delete a business-context value reference.

        Args:
            biz_ctx_id: Business context identifier.
            biz_ctx_value_id: Business context value identifier.

        Returns:
            Result of the operation.
        """
        logger.info("delete biz_ctx_value id=%d biz_ctx_id=%d", int(biz_ctx_value_id), int(biz_ctx_id))
        existing = await self._repo.get_value(biz_ctx_value_id)
        if existing is None:
            logger.info("delete biz_ctx_value id=%d → not found", int(biz_ctx_value_id))
            return False
        if int(existing.biz_ctx_id) != int(biz_ctx_id):
            raise ValueError(
                f"Business context value ID {int(biz_ctx_value_id)} does not belong to business context ID {int(biz_ctx_id)}. "
                "Please use the correct business context value for this business context and try again."
            )
        deleted = await self._repo.delete_value(biz_ctx_value_id)
        logger.info("delete biz_ctx_value id=%d → deleted", int(biz_ctx_value_id))
        return deleted

    async def update_value_by_id(
        self,
        *,
        biz_ctx_value_id: BizCtxValueId,
        ctx_scheme_value_id: CtxSchemeValueId | UnsetType = UNSET,
    ) -> tuple[BizCtxValueId, list[str]] | None:
        """Update a business-context value by value identifier only."""

        existing = await self.get_value(biz_ctx_value_id)
        if existing is None:
            return None
        return await self.update_value(
            biz_ctx_id=existing.biz_ctx_id,
            biz_ctx_value_id=biz_ctx_value_id,
            ctx_scheme_value_id=ctx_scheme_value_id,
        )

    async def delete_value_by_id(self, biz_ctx_value_id: BizCtxValueId) -> bool:
        """Delete a business-context value by value identifier only."""

        existing = await self.get_value(biz_ctx_value_id)
        if existing is None:
            return False
        return await self.delete_value(existing.biz_ctx_id, biz_ctx_value_id)

    def _to_biz_ctx_result(
        self,
        row: Any,
        *,
        users_by_id: dict[int, AppUserRow],
    ) -> BizCtxServiceResult:
        """Map repository row to Business Context DTO.

        Args:
            row: Repository row model to convert into a DTO.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Result of the operation.
        """
        return BizCtxServiceResult(
            biz_ctx_id=row.biz_ctx_id,
            guid=row.guid,
            name=str(row.name or ""),
            values=[
                BizCtxValueServiceRecord(
                    biz_ctx_value_id=item.biz_ctx_value_id,
                    ctx_scheme_value=to_dataclass(CtxSchemeValueSummaryServiceRecord, item.ctx_scheme_value),
                )
                for item in row.values
            ],
            created=WhoAndWhen(
                who=to_user_summary(int(row.created_by), users_by_id=users_by_id),
                when=row.creation_timestamp,
            ),
            last_updated=WhoAndWhen(
                who=to_user_summary(int(row.last_updated_by), users_by_id=users_by_id),
                when=row.last_update_timestamp,
            ),
        )
