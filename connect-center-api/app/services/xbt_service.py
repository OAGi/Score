"""Service layer for XBT operations in connectCenter."""


from __future__ import annotations

import logging
from typing import Any

from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.repositories.contracts.xbt import XbtRepositoryContract
from app.repositories.models.app_user import AppUserRow
from app.security import AuthenticatedUser
from app.services import load_users_by_ids, to_user_summary
from app.services.models import WhoAndWhen
from app.services.models.library import LibrarySummaryServiceRecord
from app.services.models.log import LogSummaryServiceRecord
from app.services.models.mapper import to_dataclass
from app.services.models.release import ReleaseSummaryServiceRecord
from app.services.models.xbt import XbtServiceResult, XbtSummaryServiceRecord
from app.types.identifiers import XbtManifestId

logger = logging.getLogger("connectcenter.service.xbt")


class XbtService:
    """Service for XML Built-in Type detail retrieval."""

    def __init__(
        self,
        xbt_repository: XbtRepositoryContract,
        account_service_repo: AppUserRepositoryContract,
        requester: AuthenticatedUser,
    ):
        """Initialize the service with repository dependencies.

        Args:
            xbt_repository: Value for `xbt_repository`.
            account_service_repo: Account repository used to resolve user summaries.
            requester: Requesting user context.
        """
        self._repo = xbt_repository
        self._account_service_repo = account_service_repo
        self._requester = requester

    async def get(self, xbt_manifest_id: XbtManifestId) -> XbtServiceResult | None:
        """Get XBT detail by manifest ID.

        Args:
            xbt_manifest_id: XBT manifest identifier.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get(xbt_manifest_id)
        if row is None:
            logger.info("get xbt id=%d → not found", int(xbt_manifest_id))
            return None
        users_by_id = await load_users_by_ids(
            self._account_service_repo,
            [row.owner_user_id, row.created_by, row.last_updated_by],
        )
        result = self._to_xbt_dto(row, users_by_id=users_by_id)
        logger.info("get xbt id=%d → found", int(xbt_manifest_id))
        return result

    def _to_xbt_dto(self, row: Any, *, users_by_id: dict[int, AppUserRow]) -> XbtServiceResult:
        """Map repository row to XBT DTO.

        Args:
            row: Repository row model to convert into a DTO.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Result of the operation.
        """
        return XbtServiceResult(
            xbt_manifest_id=row.xbt_manifest_id,
            xbt_id=row.xbt_id,
            guid=row.guid,
            name=row.name,
            builtIn_type=row.builtIn_type,
            jbt_draft05_map=row.jbt_draft05_map,
            openapi30_map=row.openapi30_map,
            avro_map=row.avro_map,
            subtype_of_xbt=(
                to_dataclass(XbtSummaryServiceRecord, row.subtype_of_xbt)
                if row.subtype_of_xbt is not None
                else None
            ),
            schema_definition=row.schema_definition,
            revision_doc=row.revision_doc,
            state=row.state,
            is_deprecated=row.is_deprecated,
            library=to_dataclass(LibrarySummaryServiceRecord, row.library),
            release=to_dataclass(ReleaseSummaryServiceRecord, row.release),
            log=to_dataclass(LogSummaryServiceRecord, row.log) if row.log is not None else None,
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
