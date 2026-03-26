"""Service layer for Application User operations in connectCenter.

This service currently provides simple pagination over users and retrieval by ID,
delegating query details to repository implementations.
"""


from __future__ import annotations

import logging

from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.security import AuthenticatedUser
from app.services.models.app_user import AppUserServiceResult
from app.services.models.mapper import to_dataclass, to_plain_data
from app.services.utils.pagination import PaginationParams, PaginationResponse
from app.types.identifiers import AppUserId

logger = logging.getLogger("connectcenter.service.app_user")


class AppUserService:
    """Service class for managing Application User retrieval.

    This service provides a small, focused interface for paging through users
    and retrieving a single user by ID. Filtering is delegated to repository
    implementations and currently not exposed at the service layer.

    Key Features:
    - Paginated listing of users
    - Retrieval of a single user by ID

    Main Operations:
    - list(): Retrieve a paginated list of users.
    - get(): Retrieve a single user by ID.
    """

    _ORDER_BY_ALLOWED: set[str] = {
        "login_id",
        "username",
        "organization",
        "email",
        "is_admin",
        "is_developer",
        "is_enabled",
    }

    @staticmethod
    def _normalize_result(raw: object) -> AppUserServiceResult:
        record = to_plain_data(raw)
        if isinstance(record, dict):
            username_value = record.get("username")
            name_value = record.get("name")
            login_id_value = record.get("login_id")
            if username_value is None:
                username_value = name_value
            record["username"] = str(username_value) if username_value is not None else str(login_id_value or "")
            record.pop("name", None)
            record["is_developer"] = bool(record.get("is_developer"))
            record["is_enabled"] = bool(record.get("is_enabled"))
        return to_dataclass(AppUserServiceResult, record)

    def __init__(
        self,
        app_user_repository: AppUserRepositoryContract,
        requester: AuthenticatedUser,
    ):
        """Initialize service with an application-user repository.

        Args:
            app_user_repository: App-user repository dependency.
            requester: Authenticated requester.
        """
        self._repo = app_user_repository
        self._requester = requester

    async def list(
        self,
        *,
        limit: int,
        offset: int,
        order_by: str | None = None,
        login_id: str | None = None,
        username: str | None = None,
        organization: str | None = None,
        email: str | None = None,
        is_admin: bool | None = None,
        is_developer: bool | None = None,
        is_enabled: bool | None = None,
    ) -> PaginationResponse[AppUserServiceResult]:
        """Get a paginated list of users with optional filtering.

        Args:
            limit: Maximum number of items to return.
            offset: Zero-based index of the first item in the page.
            order_by: Sort expression for the result set.
            login_id: Optional login ID filter.
            username: Optional username filter.
            organization: Optional organization filter.
            email: Optional email filter.
            is_admin: Optional admin filter.
            is_developer: Optional developer filter.
            is_enabled: Optional enabled filter.

        Returns:
            Result of the operation.
        """
        pagination = PaginationParams.from_query(
            limit=limit,
            offset=offset,
            order_by=order_by,
            allowed_sort_columns=self._ORDER_BY_ALLOWED,
        )
        logger.info("list app_users limit=%d offset=%d", limit, offset)
        total, rows = await self._repo.list(
            limit=pagination.limit,
            offset=pagination.offset,
            sorts=[(s.column, s.direction.upper()) for s in pagination.sorts],
            login_id=login_id,
            username=username,
            organization=organization,
            email=email,
            is_admin=is_admin,
            is_developer=is_developer,
            is_enabled=is_enabled,
        )
        items = [self._normalize_result(item) for item in rows]
        logger.info("list app_users → %d/%d", len(items), total)
        return PaginationResponse(items=items, total=total, limit=pagination.limit, offset=pagination.offset)

    async def get(self, app_user_id: AppUserId) -> AppUserServiceResult | None:
        """Get a single user by ID.

        Args:
            app_user_id: Application user identifier.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get(app_user_id)
        if row is None:
            logger.info("get app_user id=%d → not found", int(app_user_id))
            return None
        result = self._normalize_result(row)
        logger.info("get app_user id=%d → found", int(app_user_id))
        return result

    async def get_current_user(self) -> AppUserServiceResult | None:
        """Return the requester account from the auth context."""
        logger.info("get_current_user user_id=%d", int(self._requester.user.user_id))
        return await self.get(self._requester.user.user_id)
