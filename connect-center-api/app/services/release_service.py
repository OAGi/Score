"""Service layer for Release operations in connectCenter.

Releases represent versions of business information standards within a library.
This service delegates query and retrieval operations to the repository while
documenting supported filters, pagination, sorting, and dependency traversal.
"""


from __future__ import annotations

import logging
from typing import Any

from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.repositories.contracts.release import ReleaseRepositoryContract
from app.repositories.models.app_user import AppUserRow
from app.security import AuthenticatedUser
from app.services import load_users_by_ids, to_user_summary
from app.services.models import WhoAndWhen
from app.services.models.release import ReleaseServiceResult
from app.services.utils.date import DateRange
from app.services.utils.pagination import PaginationParams, PaginationResponse
from app.types.identifiers import LibraryId, ReleaseId

logger = logging.getLogger("connectcenter.service.release")


class ReleaseService:
    """Service class for managing Release operations.

    This service provides a comprehensive interface for working with Releases,
    which represent versions of business information standards within a library.
    Releases organize components, code lists, and other entities into versioned
    collections. The service handles querying, filtering, pagination, and dependency
    management for releases.

    Key Features:
    - Query releases with advanced filtering capabilities
    - Retrieve individual releases by ID with related summaries
    - Get release dependency chains (recursive dependency resolution)
    - Support for pagination, sorting, and date range filtering
    - Automatic exclusion of "Working" releases (repository enforced)
    - Repository-managed loading of related entities (library, namespace, creator, last_updater)
    - SQL injection protection through column whitelisting

    Main Operations:
    - get(): Retrieve a single release by its ID, including library, namespace,
      creator, last_updater, and linked release summaries (as provided by the repository).
    - list(): Retrieve paginated lists of releases with optional filters for
      library_id, release_num, and state. Supports date range filtering and custom
      sorting. Automatically excludes releases with release_num="Working".
    - get_dependent_releases(): Recursively retrieve all release IDs that a given
      release depends on, either directly or indirectly. Includes protection against
      circular dependencies in repository implementations.

    Filtering and Sorting:
    - Supports filtering by library_id (exact match), release_num (partial match), and state (exact match)
    - Date range filtering for creation and last update timestamps
    - Multi-column sorting with ascending/descending order
    - Default sorting by creation timestamp (newest first)
    - Column whitelist prevents SQL injection attacks

    Relationship Loading:
    - The repository implementation loads library and namespace summaries.

    Example Usage:
        service = ReleaseService(repo)

        # Get a specific release
        release = await service.get(release_id=ReleaseId(1))

        # Get paginated releases with filters
        page = await service.list(
            limit=20,
            offset=0,
            sorts=[],
            library_id=LibraryId(1),
            state="Published",
        )

        # Get all dependencies for a release
        dependencies = await service.get_dependent_releases(release_id=ReleaseId(1))
    """

    # Allowed columns for ordering (defense-in-depth against SQL injection).
    _ORDER_BY_ALLOWED: set[str] = {
        "release_num",
        "state",
        "creation_timestamp",
        "last_update_timestamp",
    }

    def __init__(
        self,
        release_repository: ReleaseRepositoryContract,
        account_service_repo: AppUserRepositoryContract,
        requester: AuthenticatedUser,
    ):
        """Initialize service with release and account repositories.

        Args:
            release_repository: Release repository dependency.
            account_service_repo: Account repository used to resolve user summaries.
            requester: Requesting user context.
        """
        self._repo = release_repository
        self._account_service_repo = account_service_repo
        self._requester = requester

    async def list(
        self,
        *,
        limit: int,
        offset: int,
        order_by: str | None = None,
        library_id: int | None = None,
        release_num: str | None = None,
        state: str | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
    ) -> PaginationResponse[ReleaseServiceResult]:
        """Get releases with optional filtering and pagination.

        Args:
            limit: Maximum number of items to return.
            offset: Zero-based index of the first item in the page.
            order_by: Sort expression for the result set.

        Returns:
            Result of the operation.
        """
        logger.info("list releases limit=%d offset=%d", limit, offset)
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
            library_id=library_id,
            release_num=release_num,
            state=state,
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
        items = [self._to_release_result(row, users_by_id=users_by_id) for row in rows]
        logger.info("list releases → %d/%d", len(items), total)
        return PaginationResponse(items=items, total=total, limit=pagination.limit, offset=pagination.offset)

    async def get(self, release_id: ReleaseId) -> ReleaseServiceResult | None:
        """Get a release by ID.

        Args:
            release_id: Release identifier used to scope the query.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get(release_id)
        if row is None:
            logger.info("get release id=%d → not found", int(release_id))
            return None
        users_by_id = await load_users_by_ids(self._account_service_repo, [row.created_by, row.last_updated_by])
        result = self._to_release_result(row, users_by_id=users_by_id)
        logger.info("get release id=%d → found", int(release_id))
        return result

    async def get_by_library_id_and_release_num(
        self,
        *,
        library_id: LibraryId,
        release_num: str,
    ) -> ReleaseServiceResult | None:
        """Get a release by exact library and release number."""
        row = await self._repo.get_by_library_id_and_release_num(library_id, release_num)
        if row is None:
            logger.info(
                "get release library_id=%d release_num=%s → not found",
                int(library_id),
                release_num,
            )
            return None
        users_by_id = await load_users_by_ids(self._account_service_repo, [row.created_by, row.last_updated_by])
        result = self._to_release_result(row, users_by_id=users_by_id)
        logger.info(
            "get release library_id=%d release_num=%s → found id=%d",
            int(library_id),
            release_num,
            int(result.release_id),
        )
        return result

    async def get_dependent_releases(self, release_id: ReleaseId) -> list[ReleaseId]:
        """Get all releases that the given release depends on, recursively.

        Args:
            release_id: Release identifier used to scope the query.

        Returns:
            Result of the operation.
        """
        deps = await self._repo.get_dependent_releases(release_id)
        logger.info("get_dependent_releases release_id=%d → %d dependencies", int(release_id), len(deps))
        return deps

    async def get_release_dependency_ids(self, release_id: ReleaseId) -> list[ReleaseId]:
        """Get direct release dependency IDs from `release_dep`."""
        deps = await self._repo.get_release_dependency_ids(release_id)
        logger.info("get_release_dependency_ids release_id=%d → %d dependencies", int(release_id), len(deps))
        return deps

    def _to_release_result(
        self,
        row: Any,
        *,
        users_by_id: dict[int, AppUserRow],
    ) -> ReleaseServiceResult:
        """Map repository row to Release DTO.

        Args:
            row: Repository row model to convert into a DTO.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Result of the operation.
        """
        created = to_user_summary(int(row.created_by), users_by_id=users_by_id)
        updated = to_user_summary(int(row.last_updated_by), users_by_id=users_by_id)
        return ReleaseServiceResult(
            release_id=row.release_id,
            library=row.library,
            guid=row.guid,
            release_num=str(row.release_num or ""),
            release_note=row.release_note,
            release_license=row.release_license,
            namespace=row.namespace,
            state=row.state,
            created=WhoAndWhen(who=created, when=row.creation_timestamp),
            last_updated=WhoAndWhen(who=updated, when=row.last_update_timestamp),
        )
