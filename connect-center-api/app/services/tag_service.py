"""Service layer for Tag operations in connectCenter."""


from __future__ import annotations

import logging
from typing import Any

from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.repositories.contracts.tag import TagRepositoryContract
from app.repositories.models.app_user import AppUserRow
from app.security import AuthenticatedUser
from app.services import load_users_by_ids, to_user_summary
from app.services.models import WhoAndWhen
from app.services.models.tag import TagServiceResult
from app.services.utils.date import DateRange
from app.services.utils.pagination import PaginationParams, PaginationResponse

logger = logging.getLogger("connectcenter.service.tag")


class TagService:
    """Service for Tag query operations."""

    _ORDER_BY_ALLOWED: set[str] = {
        "name",
        "description",
        "creation_timestamp",
        "last_update_timestamp",
    }

    def __init__(
        self,
        tag_repository: TagRepositoryContract,
        account_service_repo: AppUserRepositoryContract,
        requester: AuthenticatedUser,
    ):
        """Initialize the service with repository dependencies.

        Args:
            tag_repository: Value for `tag_repository`.
            account_service_repo: Account repository used to resolve user summaries.
            requester: Requesting user context.
        """
        self._repo = tag_repository
        self._account_service_repo = account_service_repo
        self._requester = requester

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
    ) -> PaginationResponse[TagServiceResult]:
        """List tags with optional filters and pagination.

        Args:
            limit: Maximum number of items to return.
            offset: Zero-based index of the first item in the page.
            order_by: Sort expression for the result set.

        Returns:
            Result of the operation.
        """
        logger.info("list tags limit=%d offset=%d", limit, offset)
        pagination = PaginationParams.from_query(
            limit=limit,
            offset=offset,
            order_by=order_by,
            allowed_sort_columns=self._ORDER_BY_ALLOWED,
        )
        total, rows = await self._repo.list(
            limit=pagination.limit,
            offset=pagination.offset,
            sorts=[(s.column, s.direction.upper()) for s in pagination.sorts],
            name=name,
            description=description,
            creation_timestamp_before=created_on.before if created_on else None,
            creation_timestamp_after=created_on.after if created_on else None,
            last_update_timestamp_before=last_updated_on.before if last_updated_on else None,
            last_update_timestamp_after=last_updated_on.after if last_updated_on else None,
        )
        user_ids = sorted(
            {
                user_id
                for row in rows
                for user_id in (row.created_by, row.last_updated_by)
            },
            key=int,
        )
        users_by_id = await load_users_by_ids(self._account_service_repo, user_ids)
        items = [self._to_tag_result(row, users_by_id=users_by_id) for row in rows]
        logger.info("list tags → %d/%d", len(items), total)
        return PaginationResponse(items=items, total=total, limit=pagination.limit, offset=pagination.offset)

    def _to_tag_result(self, row: Any, *, users_by_id: dict[int, AppUserRow]) -> TagServiceResult:
        """Map repository row to Tag DTO.

        Args:
            row: Repository row model to convert into a DTO.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Result of the operation.
        """
        return TagServiceResult(
            tag_id=row.tag_id,
            name=row.name,
            description=row.description,
            color=row.color,
            text_color=row.text_color,
            created=WhoAndWhen(
                who=to_user_summary(int(row.created_by), users_by_id=users_by_id),
                when=row.creation_timestamp,
            ),
            last_updated=WhoAndWhen(
                who=to_user_summary(int(row.last_updated_by), users_by_id=users_by_id),
                when=row.last_update_timestamp,
            ),
        )
