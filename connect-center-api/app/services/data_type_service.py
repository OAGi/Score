"""Service layer for Data Type operations in connectCenter."""


from __future__ import annotations

import logging
from typing import Any

from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.repositories.contracts.data_type import DataTypeRepositoryContract
from app.repositories.models.app_user import AppUserRow
from app.security import AuthenticatedUser
from app.services import load_users_by_ids, to_user_summary
from app.services.models import WhoAndWhen
from app.services.models.data_type import DataTypeServiceResult
from app.services.release_service import ReleaseService
from app.services.utils.date import DateRange
from app.services.utils.pagination import PaginationParams, PaginationResponse
from app.types.identifiers import DataTypeManifestId, ReleaseId

logger = logging.getLogger("connectcenter.service.data_type")


class DataTypeService:
    """Service for Data Type query and detail read operations."""

    _ORDER_BY_ALLOWED: set[str] = {
        "den",
        "data_type_term",
        "qualifier",
        "representation_term",
        "six_digit_id",
        "definition",
        "creation_timestamp",
        "last_update_timestamp",
    }

    def __init__(
        self,
        data_type_repository: DataTypeRepositoryContract,
        release_service: ReleaseService,
        account_service_repo: AppUserRepositoryContract,
        requester: AuthenticatedUser,
    ):
        """Initialize the service with repository and dependency services.

        Args:
            data_type_repository: Data-type repository dependency.
            release_service: Release service dependency.
            account_service_repo: Account repository used to resolve user summaries.
            requester: Requesting user context.
        """
        self._repo = data_type_repository
        self._release_service = release_service
        self._account_service_repo = account_service_repo
        self._requester = requester

    async def list(
        self,
        *,
        release_id: ReleaseId,
        limit: int,
        offset: int,
        order_by: str | None = None,
        den: str | None = None,
        representation_term: str | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
    ) -> PaginationResponse[DataTypeServiceResult]:
        """List Data Types for a release scope with filters and pagination.

        Args:
            release_id: Release identifier used to scope the query.
            limit: Maximum number of items to return.
            offset: Zero-based index of the first item in the page.
            order_by: Sort expression for the result set.

        Returns:
            Result of the operation.
        """
        logger.info("list data_types release_id=%d limit=%d offset=%d", int(release_id), limit, offset)
        pagination = PaginationParams.from_query(
            limit=limit,
            offset=offset,
            order_by=order_by,
            allowed_sort_columns=self._ORDER_BY_ALLOWED,
        )
        dependent_release_ids = await self._release_service.get_dependent_releases(release_id)
        total, rows = await self._repo.list(
            release_id=release_id,
            dependent_release_ids=dependent_release_ids,
            limit=pagination.limit,
            offset=pagination.offset,
            sorts=[(s.column, s.direction.upper()) for s in pagination.sorts],
            den=den,
            representation_term=representation_term,
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
        items = [self._to_data_type_result(row, users_by_id=users_by_id) for row in rows]
        logger.info("list data_types release_id=%d → %d/%d", int(release_id), len(items), total)
        return PaginationResponse(items=items, total=total, limit=pagination.limit, offset=pagination.offset)

    async def get(self, dt_manifest_id: DataTypeManifestId) -> DataTypeServiceResult | None:
        """Get Data Type detail by manifest ID.

        Args:
            dt_manifest_id: Data type manifest identifier.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get(dt_manifest_id)
        if row is None:
            logger.info("get data_type id=%d → not found", int(dt_manifest_id))
            return None
        users_by_id = await load_users_by_ids(self._account_service_repo, [row.owner_user_id, row.created_by, row.last_updated_by])
        result = self._to_data_type_result(row, users_by_id=users_by_id)
        logger.info("get data_type id=%d → found", int(dt_manifest_id))
        return result

    def _to_data_type_result(self, row: Any, *, users_by_id: dict[int, AppUserRow]) -> DataTypeServiceResult:
        """Map repository row to Data Type DTO.

        Args:
            row: Repository row model to convert into a DTO.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Result of the operation.
        """
        return DataTypeServiceResult(
            dt_manifest_id=row.dt_manifest_id,
            dt_id=row.dt_id,
            base_dt=row.base_dt,
            guid=row.guid,
            den=row.den,
            data_type_term=row.data_type_term,
            qualifier=row.qualifier,
            representation_term=row.representation_term,
            six_digit_id=row.six_digit_id,
            definition=row.definition,
            definition_source=row.definition_source,
            content_component_definition=row.content_component_definition,
            commonly_used=row.commonly_used,
            is_deprecated=row.is_deprecated,
            state=row.state,
            supplementary_components=row.supplementary_components,
            namespace=row.namespace,
            library=row.library,
            release=row.release,
            log=row.log,
            owner=to_user_summary(int(row.owner_user_id), users_by_id=users_by_id),
            created=WhoAndWhen(
                who=to_user_summary(int(row.created_by), users_by_id=users_by_id),
                when=row.creation_timestamp,
            ),
            last_updated=WhoAndWhen(
                who=to_user_summary(int(row.last_updated_by), users_by_id=users_by_id),
                when=row.last_update_timestamp,
            ),
        )
