"""Service layer for Namespace operations in connectCenter.

Namespaces provide unique identification and scoping for components and entities.
This service delegates query and retrieval operations to the repository while
documenting supported filters, pagination, and sorting.
"""


from __future__ import annotations

import logging
from typing import Any

from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.repositories.contracts.namespace import NamespaceRepositoryContract
from app.repositories.models.app_user import AppUserRow
from app.security import AuthenticatedUser
from app.services import load_users_by_ids, to_user_summary
from app.services.models import WhoAndWhen
from app.services.models.namespace import NamespaceServiceResult
from app.services.utils.date import DateRange
from app.services.utils.pagination import PaginationParams, PaginationResponse
from app.types.identifiers import NamespaceId

logger = logging.getLogger("connectcenter.service.namespace")


class NamespaceService:
    """Service class for managing Namespace operations.

    This service provides a comprehensive interface for working with Namespaces,
    which provide unique identification and scoping for components and entities
    in connectCenter. Namespaces help organize and distinguish components
    from different sources or standards.

    Key Features:
    - Query namespaces with advanced filtering capabilities
    - Retrieve individual namespaces by ID
    - Support for pagination, sorting, and date range filtering
    - Repository-managed loading of related entities (library, owner, creator, last_updater)
    - SQL injection protection through column whitelisting

    Main Operations:
    - get(): Retrieve a single namespace by its ID, including library, owner,
      creator, and last_updater information (as provided by the repository).
    - list(): Retrieve paginated lists of namespaces with optional filters for
      library_id, uri, prefix, and is_std_nmsp flag. Supports date range filtering
      and custom sorting.

    Filtering Capabilities:
    - Text fields (uri, prefix): case-insensitive partial matching
    - Boolean field (is_std_nmsp): exact matching for standard namespace flag
    - Library ID: exact matching
    - Date range filtering for creation and last update timestamps
    - All filters can be combined for complex queries

    Sorting:
    - Supports sorting by multiple columns (uri, prefix, is_std_nmsp, timestamps)
    - Multi-column sorting with ascending/descending order
    - Default sorting by creation timestamp (newest first)
    - Column whitelist prevents SQL injection attacks

    Relationship Loading:
    - The repository implementation loads library, owner, creator, and last_updater summaries.

    Example Usage:
        service = NamespaceService(repo)

        # Get a specific namespace
        namespace = await service.get(namespace_id=NamespaceId(1))

        # Get paginated namespaces with filters
        page = await service.list(
            limit=10,
            offset=0,
            sorts=[],
            uri="http://www.example.org",
            is_std_nmsp=True,
        )
    """

    # Allowed columns for ordering (defense-in-depth against SQL injection).
    _ORDER_BY_ALLOWED: set[str] = {
        "uri",
        "prefix",
        "is_std_nmsp",
        "creation_timestamp",
        "last_update_timestamp",
    }

    def __init__(
        self,
        namespace_repository: NamespaceRepositoryContract,
        account_service_repo: AppUserRepositoryContract,
        requester: AuthenticatedUser,
    ):
        """Initialize service with namespace and account repositories.

        Args:
            namespace_repository: Namespace repository dependency.
            account_service_repo: Account repository used to resolve user summaries.
            requester: Requesting user context.
        """
        self._repo = namespace_repository
        self._account_service_repo = account_service_repo
        self._requester = requester

    async def list(
        self,
        *,
        limit: int,
        offset: int,
        order_by: str | None = None,
        library_id: int | None = None,
        uri: str | None = None,
        prefix: str | None = None,
        is_std_nmsp: bool | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
    ) -> PaginationResponse[NamespaceServiceResult]:
        """Get namespaces with optional filtering and pagination.

        Args:
            limit: Maximum number of items to return.
            offset: Zero-based index of the first item in the page.
            order_by: Sort expression for the result set.

        Returns:
            Result of the operation.
        """
        logger.info("list namespaces limit=%d offset=%d", limit, offset)
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
            uri=uri,
            prefix=prefix,
            is_std_nmsp=is_std_nmsp,
            creation_timestamp_before=created_on.before if created_on else None,
            creation_timestamp_after=created_on.after if created_on else None,
            last_update_timestamp_before=last_updated_on.before if last_updated_on else None,
            last_update_timestamp_after=last_updated_on.after if last_updated_on else None,
        )
        user_ids = sorted(
            {
                user_id
                for row in rows
                for user_id in (row.owner_user_id, row.created_by, row.last_updated_by)
            },
            key=int,
        )
        users_by_id = await load_users_by_ids(self._account_service_repo, user_ids)
        items = [self._to_namespace_result(row, users_by_id=users_by_id) for row in rows]
        logger.info("list namespaces → %d/%d", len(items), total)
        return PaginationResponse(items=items, total=total, limit=pagination.limit, offset=pagination.offset)

    async def get(self, namespace_id: NamespaceId) -> NamespaceServiceResult | None:
        """Get a namespace by ID.

        Args:
            namespace_id: Namespace identifier.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get(namespace_id)
        if row is None:
            logger.info("get namespace id=%d → not found", int(namespace_id))
            return None
        users_by_id = await load_users_by_ids(self._account_service_repo, [row.owner_user_id, row.created_by, row.last_updated_by])
        result = self._to_namespace_result(row, users_by_id=users_by_id)
        logger.info("get namespace id=%d → found", int(namespace_id))
        return result

    def _to_namespace_result(
        self,
        row: Any,
        *,
        users_by_id: dict[int, AppUserRow],
    ) -> NamespaceServiceResult:
        """Map repository row to Namespace DTO.

        Args:
            row: Repository row model to convert into a DTO.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Result of the operation.
        """
        owner = to_user_summary(int(row.owner_user_id), users_by_id=users_by_id)
        created = to_user_summary(int(row.created_by), users_by_id=users_by_id)
        updated = to_user_summary(int(row.last_updated_by), users_by_id=users_by_id)
        return NamespaceServiceResult(
            namespace_id=row.namespace_id,
            library=row.library,
            uri=row.uri,
            prefix=row.prefix,
            description=row.description,
            is_std_nmsp=row.is_std_nmsp,
            owner=owner,
            created=WhoAndWhen(who=created, when=row.creation_timestamp),
            last_updated=WhoAndWhen(who=updated, when=row.last_update_timestamp),
        )
