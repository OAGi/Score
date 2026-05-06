"""Service layer for Agency ID List operations in connectCenter."""


from __future__ import annotations

import logging
from typing import Any

from app.repositories.contracts.agency_id_list import AgencyIdListRepositoryContract
from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.repositories.models.app_user import AppUserRow
from app.security import AuthenticatedUser
from app.services import load_users_by_ids, to_user_summary
from app.services.models import WhoAndWhen
from app.services.models.agency_id_list import AgencyIdListServiceResult
from app.services.release_service import ReleaseService
from app.services.utils.date import DateRange
from app.services.utils.owner import parse_owner_filter
from app.services.utils.pagination import PaginationParams, PaginationResponse
from app.types.identifiers import AgencyIdListManifestId, ReleaseId

logger = logging.getLogger("connectcenter.service.agency_id_list")


class AgencyIdListService:
    """Service for Agency ID List query and detail read operations."""

    _ORDER_BY_ALLOWED: set[str] = {
        "name",
        "list_id",
        "version_id",
        "definition",
        "creation_timestamp",
        "last_update_timestamp",
    }

    def __init__(
        self,
        agency_id_list_repository: AgencyIdListRepositoryContract,
        release_service: ReleaseService,
        account_service_repo: AppUserRepositoryContract,
        requester: AuthenticatedUser,
    ):
        """Initialize the service with repository and dependency services.

        Args:
            agency_id_list_repository: Value for `agency_id_list_repository`.
            release_service: Release service dependency.
            account_service_repo: Account repository used to resolve user summaries.
            requester: Requesting user context.
        """
        self._repo = agency_id_list_repository
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
        name: str | None = None,
        list_id: str | None = None,
        version_id: str | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
        owner: str | None = None,
    ) -> PaginationResponse[AgencyIdListServiceResult]:
        """List Agency ID Lists for a release scope.

        Args:
            release_id: Release identifier used to scope the query.
            limit: Maximum number of items to return.
            offset: Zero-based index of the first item in the page.
            order_by: Sort expression for the result set.

        Returns:
            Paginated agency ID list service results.
        """
        logger.info("list agency_id_lists release_id=%d limit=%d offset=%d", int(release_id), limit, offset)
        pagination = PaginationParams.from_query(
            limit=limit,
            offset=offset,
            order_by=order_by,
            allowed_sort_columns=self._ORDER_BY_ALLOWED,
        )
        dependent_release_ids = await self._release_service.get_dependent_releases(release_id)
        included_owner_login_ids, excluded_owner_login_ids = parse_owner_filter(owner)
        total, rows = await self._repo.list(
            release_id=release_id,
            dependent_release_ids=dependent_release_ids,
            limit=pagination.limit,
            offset=pagination.offset,
            sorts=[(s.column, s.direction.upper()) for s in pagination.sorts],
            name=name,
            list_id=list_id,
            version_id=version_id,
            creation_timestamp_before=created_on.before if created_on else None,
            creation_timestamp_after=created_on.after if created_on else None,
            last_update_timestamp_before=last_updated_on.before if last_updated_on else None,
            last_update_timestamp_after=last_updated_on.after if last_updated_on else None,
            included_owner_login_ids=included_owner_login_ids,
            excluded_owner_login_ids=excluded_owner_login_ids,
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
        items = [self._to_agency_id_list_result(row, users_by_id=users_by_id) for row in rows]
        logger.info("list agency_id_lists release_id=%d → %d/%d", int(release_id), len(items), total)
        return PaginationResponse(items=items, total=total, limit=pagination.limit, offset=pagination.offset)

    async def get(self, agency_id_list_manifest_id: AgencyIdListManifestId) -> AgencyIdListServiceResult | None:
        """Get Agency ID List detail by manifest ID.

        Args:
            agency_id_list_manifest_id: Agency ID list manifest identifier.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get(agency_id_list_manifest_id)
        if row is None:
            logger.info("get agency_id_list id=%d → not found", int(agency_id_list_manifest_id))
            return None
        users_by_id = await load_users_by_ids(self._account_service_repo, [row.owner_user_id, row.created_by, row.last_updated_by])
        result = self._to_agency_id_list_result(row, users_by_id=users_by_id)
        logger.info("get agency_id_list id=%d → found", int(agency_id_list_manifest_id))
        return result

    def _to_agency_id_list_result(
        self,
        row: Any,
        *,
        users_by_id: dict[int, AppUserRow],
    ) -> AgencyIdListServiceResult:
        """Map repository row to Agency ID List DTO.

        Args:
            row: Repository row model to convert into a DTO.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Agency ID list service result.
        """
        return AgencyIdListServiceResult(
            agency_id_list_manifest_id=row.agency_id_list_manifest_id,
            agency_id_list_id=row.agency_id_list_id,
            guid=row.guid,
            enum_type_guid=row.enum_type_guid,
            name=str(row.name or ""),
            list_id=str(row.list_id or ""),
            version_id=str(row.version_id or ""),
            definition=row.definition,
            remark=row.remark,
            definition_source=row.definition_source,
            is_deprecated=row.is_deprecated,
            state=row.state,
            values=row.values,
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
