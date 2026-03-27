"""Service layer for Library operations in connectCenter.

Libraries are organizational containers that group releases and related components.
This service delegates query and retrieval operations to the repository while
documenting supported filters, pagination, and sorting.
"""


from __future__ import annotations

import logging
from typing import Any

from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.repositories.contracts.library import LibraryRepositoryContract
from app.repositories.models.app_user import AppUserRow
from app.security import AuthenticatedUser
from app.services import load_users_by_ids, to_user_summary
from app.services.models import WhoAndWhen
from app.services.models.library import LibraryServiceResult
from app.services.utils.date import DateRange
from app.services.utils.pagination import PaginationParams, PaginationResponse
from app.types.identifiers import LibraryId

logger = logging.getLogger("connectcenter.service.library")


class LibraryService:
    """Service class for managing Library operations.

    This service provides a comprehensive interface for working with Libraries,
    which are organizational containers that group related releases, components,
    and other entities in connectCenter. Libraries help organize and manage
    collections of business information standards.

    Key Features:
    - Query libraries with advanced filtering capabilities
    - Retrieve individual libraries by ID
    - Support for pagination, sorting, and date range filtering
    - Repository-managed loading of related entities (creator, last_updater)
    - SQL injection protection through column whitelisting

    Main Operations:
    - get(): Retrieve a single library by its ID, including creator and last_updater
      information (as provided by the repository).
    - list(): Retrieve paginated lists of libraries with optional filters for name,
      type, organization, domain, state, description, and is_default flag. Supports
      date range filtering and custom sorting.

    Filtering Capabilities:
    - Text fields (name, type, organization, domain, state, description): case-insensitive partial matching
    - Boolean field (is_default): exact matching
    - Date range filtering for creation and last update timestamps
    - All filters can be combined for complex queries

    Sorting:
    - Supports sorting by multiple columns (name, type, organization, domain, state, etc.)
    - Multi-column sorting with ascending/descending order
    - Default sorting by creation timestamp (newest first)
    - Column whitelist prevents SQL injection attacks

    Relationship Loading:
    - The repository implementation loads creator and last_updater summaries.

    Example Usage:
        service = LibraryService(repo)

        # Get a specific library
        library = await service.get(library_id=LibraryId(1))

        # Get paginated libraries with filters
        page = await service.list(
            limit=10,
            offset=0,
            sorts=[],
            name="ISO",
            type="Standard",
        )
    """

    # Allowed columns for ordering (defense-in-depth against SQL injection).
    _ORDER_BY_ALLOWED: set[str] = {
        "name",
        "type",
        "organization",
        "domain",
        "state",
        "description",
        "is_default",
        "creation_timestamp",
        "last_update_timestamp",
    }

    def __init__(
        self,
        library_repository: LibraryRepositoryContract,
        account_service_repo: AppUserRepositoryContract,
        requester: AuthenticatedUser,
    ):
        """Initialize service with library and account repositories.

        Args:
            library_repository: Library repository dependency.
            account_service_repo: Account repository used to resolve user summaries.
            requester: Requesting user context.
        """
        self._repo = library_repository
        self._account_service_repo = account_service_repo
        self._requester = requester

    async def list(
        self,
        *,
        limit: int,
        offset: int,
        order_by: str | None = None,
        name: str | None = None,
        type: str | None = None,
        organization: str | None = None,
        domain: str | None = None,
        state: str | None = None,
        description: str | None = None,
        is_default: bool | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
    ) -> PaginationResponse[LibraryServiceResult]:
        """Get libraries with optional filtering and pagination.

        Args:
            limit: Maximum number of items to return.
            offset: Zero-based index of the first item in the page.
            order_by: Sort expression for the result set.

        Returns:
            Result of the operation.
        """
        logger.info("list libraries limit=%d offset=%d", limit, offset)
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
            type=type,
            organization=organization,
            domain=domain,
            state=state,
            description=description,
            is_default=is_default,
            creation_timestamp_before=created_on.before if created_on else None,
            creation_timestamp_after=created_on.after if created_on else None,
            last_update_timestamp_before=last_updated_on.before if last_updated_on else None,
            last_update_timestamp_after=last_updated_on.after if last_updated_on else None,
        )
        user_ids = sorted(
            {user_id for row in rows for user_id in (row.created_by, row.last_updated_by)},
            key=int,
        )
        users_by_id = await load_users_by_ids(self._account_service_repo, user_ids)
        items = [self._to_library_result(row, users_by_id=users_by_id) for row in rows]
        logger.info("list libraries → %d/%d", len(items), total)
        return PaginationResponse(items=items, total=total, limit=pagination.limit, offset=pagination.offset)

    async def get(self, library_id: LibraryId) -> LibraryServiceResult | None:
        """Get a library by ID.

        Args:
            library_id: Library identifier used to scope the query.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get(library_id)
        if row is None:
            logger.info("get library id=%d → not found", int(library_id))
            return None
        users_by_id = await load_users_by_ids(self._account_service_repo, [row.created_by, row.last_updated_by])
        result = self._to_library_result(row, users_by_id=users_by_id)
        logger.info("get library id=%d → found", int(library_id))
        return result

    def _to_library_result(
        self,
        row: Any,
        *,
        users_by_id: dict[int, AppUserRow],
    ) -> LibraryServiceResult:
        """Map repository row to Library DTO.

        Args:
            row: Repository row model to convert into a DTO.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Result of the operation.
        """
        created = to_user_summary(int(row.created_by), users_by_id=users_by_id)
        updated = to_user_summary(int(row.last_updated_by), users_by_id=users_by_id)
        return LibraryServiceResult(
            library_id=row.library_id,
            name=row.name,
            type=row.type,
            organization=row.organization,
            description=row.description,
            link=row.link,
            domain=row.domain,
            state=row.state,
            is_read_only=row.is_read_only,
            is_default=row.is_default,
            created=WhoAndWhen(who=created, when=row.creation_timestamp),
            last_updated=WhoAndWhen(who=updated, when=row.last_update_timestamp),
        )
