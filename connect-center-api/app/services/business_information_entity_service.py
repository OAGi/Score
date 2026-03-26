"""Service layer for Business Information Entity operations in connectCenter."""


from __future__ import annotations

import hashlib
import logging
from datetime import datetime, timezone
from typing import Any

from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.repositories.contracts.business_information_entity import BusinessInformationEntityRepositoryContract
from app.repositories.contracts.code_list import CodeListRepositoryContract
from app.repositories.models.business_information_entity import (
    AbieInfoRow,
    AsbieRelationshipRow,
    AsbiepInfoRow,
    BbiepInfoRow,
    GetAsbieRow,
    GetBbieRow,
    TopLevelAsbiepDetailRow,
    TopLevelAsbiepInfoRow,
)
from app.security import AuthenticatedUser
from app.services import load_users_by_ids, to_user_summary
from app.services.bie_state_transition_service import (
    BieStateTransitionService,
    CodeListDependencyDetail,
    TopLevelAsbiepDependencyBlockedError,
    TopLevelAsbiepDependencyDetail,
)
from app.services.models import WhoAndWhen
from app.services.models.biz_ctx import BizCtxSummaryServiceRecord
from app.services.models.business_information_entity import (
    AssignBizCtxToTopLevelAsbiepServiceResult,
    CreateAsbieServiceResult,
    CreateBbieScServiceResult,
    CreateBbieServiceResult,
    CreateTopLevelAsbiepServiceResult,
    DeleteTopLevelAsbiepServiceResult,
    GetAsbieServiceResult,
    GetBbieServiceResult,
    GetTopLevelAsbiepServiceResult,
    RemoveReusedTopLevelAsbiepServiceResult,
    ReuseTopLevelAsbiepServiceResult,
    TopLevelAsbiepListServiceResult,
    TransferTopLevelAsbiepOwnershipServiceResult,
    UnassignBizCtxFromTopLevelAsbiepServiceResult,
    UpdateAsbieServiceResult,
    UpdateBbieScServiceResult,
    UpdateBbieServiceResult,
    UpdateTopLevelAsbiepServiceResult, PrimitiveRestrictionServiceRecord, FacetServiceRecord,
    AsbieRelationshipServiceRecord,
    BbieRelationshipServiceRecord, TopLevelAsbiepInfoRecord, AbieInfoRecord, AsbiepInfoRecord, BbieScInfoRecord,
    BbiepInfoRecord,
)
from app.services.models.core_component import (
    AccInfoRecord,
    AsccInfoRecord,
    AsccpInfoRecord,
    BccInfoRecord,
    BccpInfoRecord,
    ValueConstraintServiceRecord,
)
from app.services.models.data_type import DataTypeSupplementaryComponentServiceRecord
from app.services.models.library import LibrarySummaryServiceRecord
from app.services.models.mapper import to_dataclass
from app.services.models.release import ReleaseSummaryServiceRecord
from app.services.utils.date import DateRange
from app.services.utils.pagination import PaginationParams, PaginationResponse

logger = logging.getLogger("connectcenter.service.business_information_entity")


class BusinessInformationEntityService:
    """Application service for Business Information Entity read operations.

    This service orchestrates repository queries, user-summary enrichment, and
    conversion from repository rows to typed DTO records used by route layers.
    """

    _ORDER_BY_ALLOWED: set[str] = {
        "den",
        "version",
        "status",
        "state",
        "creation_timestamp",
        "last_update_timestamp",
    }

    def __init__(
        self,
        business_information_entity_repository: BusinessInformationEntityRepositoryContract,
        requester: AuthenticatedUser,
        account_service_repo: AppUserRepositoryContract,
        code_list_repo: CodeListRepositoryContract | None = None,
    ):
        """Initialize service with BIE and account repositories.

        Args:
            business_information_entity_repository: Value for `business_information_entity_repository`.
            requester: Requesting user used for authorization checks.
            account_service_repo: Account repository used to resolve user summaries.
            code_list_repo: Code-list repository used for transition dependency validation.
        """
        self._repo = business_information_entity_repository
        self._requester = requester
        self._account_service_repo = account_service_repo
        self._code_list_repo = code_list_repo

    async def list_top_level_asbieps(
        self,
        *,
        limit: int,
        offset: int,
        order_by: str | None = None,
        library_id: int | None = None,
        release_ids: list[int] | None = None,
        den: str | None = None,
        version: str | None = None,
        status: str | None = None,
        state: str | None = None,
        is_deprecated: bool | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
    ) -> PaginationResponse[TopLevelAsbiepListServiceResult]:
        """List top-level ASBIEPs with filters, sorting, and pagination.

        Args:
            limit: Maximum number of items to return.
            offset: Zero-based index of the first item in the page.
            order_by: Sort expression for the result set.
            library_id: Library identifier used to scope the query.
            release_ids: Release identifiers used to scope the query.
            den: Optional Dictionary Entry Name (DEN) filter.
            version: Optional version filter.
            status: Optional status filter.
            state: Optional lifecycle state filter.
            is_deprecated: Optional deprecation flag filter.
            created_on: Optional creation-time filter in ISO-8601 range form.
            last_updated_on: Optional last-update-time filter in ISO-8601 range form.

        Returns:
            Result of the operation.
        """
        logger.info("list top_level_asbieps limit=%d offset=%d", limit, offset)
        pagination = PaginationParams.from_query(
            limit=limit,
            offset=offset,
            order_by=order_by,
            allowed_sort_columns=self._ORDER_BY_ALLOWED,
        )
        total, rows = await self._repo.list_top_level_asbieps(
            limit=pagination.limit,
            offset=pagination.offset,
            sorts=[(s.column, s.direction.upper()) for s in pagination.sorts],
            library_id=library_id,
            release_ids=release_ids,
            den=den,
            version=version,
            status=status,
            state=state,
            is_deprecated=is_deprecated,
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
        users_by_id = await load_users_by_ids(self._account_service_repo, sorted(user_ids))
        items = [self._to_top_level_asbiep_list_result(row, users_by_id=users_by_id) for row in rows]
        logger.info("list top_level_asbieps → %d/%d", len(items), total)
        return PaginationResponse(items=items, total=total, limit=pagination.limit, offset=pagination.offset)

    async def get_top_level_asbiep(self, *, top_level_asbiep_id: int) -> GetTopLevelAsbiepServiceResult | None:
        """Get top-level ASBIEP detail by identifier.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get_top_level_asbiep(top_level_asbiep_id=top_level_asbiep_id)
        if row is None:
            logger.info("get top_level_asbiep id=%d → not found", top_level_asbiep_id)
            return None
        user_ids: set[int] = {int(row.owner_user_id), int(row.created_by), int(row.last_updated_by)}
        asbiep = row.asbiep
        if asbiep.owner_top_level_asbiep is not None:
            top_level = asbiep.owner_top_level_asbiep
            user_ids.add(int(top_level.owner_user_id))
            if top_level.created_by is not None:
                user_ids.add(int(top_level.created_by))
            if top_level.last_updated_by is not None:
                user_ids.add(int(top_level.last_updated_by))
        if asbiep.created_by is not None:
            user_ids.add(int(asbiep.created_by))
        if asbiep.last_updated_by is not None:
            user_ids.add(int(asbiep.last_updated_by))
        if asbiep.role_of_abie.created_by is not None:
            user_ids.add(int(asbiep.role_of_abie.created_by))
        if asbiep.role_of_abie.last_updated_by is not None:
            user_ids.add(int(asbiep.role_of_abie.last_updated_by))
        users_by_id = await load_users_by_ids(self._account_service_repo, sorted(user_ids))
        logger.info("get top_level_asbiep id=%d → found", top_level_asbiep_id)
        return self._to_top_level_detail_dto(row, users_by_id=users_by_id)

    async def get_asbie_by_asbie_id(self, *, asbie_id: int) -> GetAsbieServiceResult | None:
        """Get ASBIE detail by ASBIE identifier.

        Args:
            asbie_id: ASBIE identifier.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get_asbie_by_asbie_id(asbie_id=asbie_id)
        if row is None:
            logger.info("get asbie id=%d → not found", asbie_id)
            return None
        user_ids: set[int] = {int(row.owner_top_level_asbiep.owner_user_id)}
        asbiep = row.to_asbiep
        if asbiep.owner_top_level_asbiep is not None:
            top_level = asbiep.owner_top_level_asbiep
            user_ids.add(int(top_level.owner_user_id))
            if top_level.created_by is not None:
                user_ids.add(int(top_level.created_by))
            if top_level.last_updated_by is not None:
                user_ids.add(int(top_level.last_updated_by))
        if asbiep.created_by is not None:
            user_ids.add(int(asbiep.created_by))
        if asbiep.last_updated_by is not None:
            user_ids.add(int(asbiep.last_updated_by))
        if asbiep.role_of_abie.created_by is not None:
            user_ids.add(int(asbiep.role_of_abie.created_by))
        if asbiep.role_of_abie.last_updated_by is not None:
            user_ids.add(int(asbiep.role_of_abie.last_updated_by))
        if row.created_by is not None:
            user_ids.add(int(row.created_by))
        if row.last_updated_by is not None:
            user_ids.add(int(row.last_updated_by))
        users_by_id = await load_users_by_ids(self._account_service_repo, sorted(user_ids))
        logger.info("get asbie id=%d → found", asbie_id)
        return self._to_asbie_dto(row, users_by_id=users_by_id)

    async def get_asbie_by_based_ascc_manifest_id(
        self,
        *,
        top_level_asbiep_id: int,
        based_ascc_manifest_id: int,
    ) -> GetAsbieServiceResult | None:
        """Get ASBIE detail by top-level ASBIEP and based ASCC manifest identifiers.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            based_ascc_manifest_id: ASCC manifest identifier for relationship lookup.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get_asbie_by_based_ascc_manifest_id(
            top_level_asbiep_id=top_level_asbiep_id,
            based_ascc_manifest_id=based_ascc_manifest_id,
        )
        if row is None:
            logger.info("get asbie top_level_asbiep_id=%d based_ascc_manifest_id=%d → not found", top_level_asbiep_id, based_ascc_manifest_id)
            return None
        user_ids: set[int] = {int(row.owner_top_level_asbiep.owner_user_id)}
        asbiep = row.to_asbiep
        if asbiep.owner_top_level_asbiep is not None:
            top_level = asbiep.owner_top_level_asbiep
            user_ids.add(int(top_level.owner_user_id))
            if top_level.created_by is not None:
                user_ids.add(int(top_level.created_by))
            if top_level.last_updated_by is not None:
                user_ids.add(int(top_level.last_updated_by))
        if asbiep.created_by is not None:
            user_ids.add(int(asbiep.created_by))
        if asbiep.last_updated_by is not None:
            user_ids.add(int(asbiep.last_updated_by))
        if asbiep.role_of_abie.created_by is not None:
            user_ids.add(int(asbiep.role_of_abie.created_by))
        if asbiep.role_of_abie.last_updated_by is not None:
            user_ids.add(int(asbiep.role_of_abie.last_updated_by))
        if row.created_by is not None:
            user_ids.add(int(row.created_by))
        if row.last_updated_by is not None:
            user_ids.add(int(row.last_updated_by))
        users_by_id = await load_users_by_ids(self._account_service_repo, sorted(user_ids))
        logger.info("get asbie top_level_asbiep_id=%d based_ascc_manifest_id=%d → found", top_level_asbiep_id, based_ascc_manifest_id)
        return self._to_asbie_dto(row, users_by_id=users_by_id)

    async def get_bbie_by_bbie_id(self, *, bbie_id: int) -> GetBbieServiceResult | None:
        """Get BBIE detail by BBIE identifier.

        Args:
            bbie_id: BBIE identifier.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get_bbie_by_bbie_id(bbie_id=bbie_id)
        if row is None:
            logger.info("get bbie id=%d → not found", bbie_id)
            return None
        user_ids: set[int] = {int(row.owner_top_level_asbiep.owner_user_id)}
        if row.to_bbiep.owner_top_level_asbiep is not None:
            top_level = row.to_bbiep.owner_top_level_asbiep
            user_ids.add(int(top_level.owner_user_id))
            if top_level.created_by is not None:
                user_ids.add(int(top_level.created_by))
            if top_level.last_updated_by is not None:
                user_ids.add(int(top_level.last_updated_by))
        for sc in row.to_bbiep.supplementary_components:
            if sc.owner_top_level_asbiep is None:
                continue
            top_level = sc.owner_top_level_asbiep
            user_ids.add(int(top_level.owner_user_id))
            if top_level.created_by is not None:
                user_ids.add(int(top_level.created_by))
            if top_level.last_updated_by is not None:
                user_ids.add(int(top_level.last_updated_by))
        if row.created_by is not None:
            user_ids.add(int(row.created_by))
        if row.last_updated_by is not None:
            user_ids.add(int(row.last_updated_by))
        users_by_id = await load_users_by_ids(self._account_service_repo, sorted(user_ids))
        logger.info("get bbie id=%d → found", bbie_id)
        return self._to_bbie_dto(row, users_by_id=users_by_id)

    async def get_bbie_by_based_bcc_manifest_id(
        self,
        *,
        top_level_asbiep_id: int,
        based_bcc_manifest_id: int,
    ) -> GetBbieServiceResult | None:
        """Get BBIE detail by top-level ASBIEP and based BCC manifest identifiers.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            based_bcc_manifest_id: BCC manifest identifier for relationship lookup.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get_bbie_by_based_bcc_manifest_id(
            top_level_asbiep_id=top_level_asbiep_id,
            based_bcc_manifest_id=based_bcc_manifest_id,
        )
        if row is None:
            logger.info("get bbie top_level_asbiep_id=%d based_bcc_manifest_id=%d → not found", top_level_asbiep_id, based_bcc_manifest_id)
            return None
        user_ids: set[int] = {int(row.owner_top_level_asbiep.owner_user_id)}
        if row.to_bbiep.owner_top_level_asbiep is not None:
            top_level = row.to_bbiep.owner_top_level_asbiep
            user_ids.add(int(top_level.owner_user_id))
            if top_level.created_by is not None:
                user_ids.add(int(top_level.created_by))
            if top_level.last_updated_by is not None:
                user_ids.add(int(top_level.last_updated_by))
        for sc in row.to_bbiep.supplementary_components:
            if sc.owner_top_level_asbiep is None:
                continue
            top_level = sc.owner_top_level_asbiep
            user_ids.add(int(top_level.owner_user_id))
            if top_level.created_by is not None:
                user_ids.add(int(top_level.created_by))
            if top_level.last_updated_by is not None:
                user_ids.add(int(top_level.last_updated_by))
        if row.created_by is not None:
            user_ids.add(int(row.created_by))
        if row.last_updated_by is not None:
            user_ids.add(int(row.last_updated_by))
        users_by_id = await load_users_by_ids(self._account_service_repo, user_ids)
        logger.info("get bbie top_level_asbiep_id=%d based_bcc_manifest_id=%d → found", top_level_asbiep_id, based_bcc_manifest_id)
        return self._to_bbie_dto(row, users_by_id=users_by_id)

    async def create_top_level_asbiep(
        self,
        *,
        asccp_manifest_id: int,
        biz_ctx_list: list[int],
    ) -> CreateTopLevelAsbiepServiceResult:
        """Create a top-level ASBIEP and return created structure.

        Args:
            asccp_manifest_id: ASCCP manifest identifier.
            biz_ctx_list: Business context identifiers assigned to the new BIE.

        Returns:
            Result of the operation.
        """
        logger.info("create top_level_asbiep asccp_manifest_id=%d biz_ctx_list=%s", asccp_manifest_id, biz_ctx_list)
        top_level_asbiep_id = await self._repo.create_top_level_asbiep(
            asccp_manifest_id=asccp_manifest_id,
            biz_ctx_ids=biz_ctx_list,
            requester_user_id=int(self._requester.user.user_id),
        )
        detail = await self.get_top_level_asbiep(top_level_asbiep_id=top_level_asbiep_id)
        if detail is None:
            raise LookupError(
                f"No top-level ASBIEP exists with ID {int(top_level_asbiep_id)}. "
                "Please verify the identifier and try again."
            )
        logger.info("create top_level_asbiep → id=%d", int(top_level_asbiep_id))
        return CreateTopLevelAsbiepServiceResult(
            top_level_asbiep_id=top_level_asbiep_id,
            asbiep=detail.asbiep
        )

    async def update_top_level_asbiep(
        self,
        *,
        top_level_asbiep_id: int,
        version: str | None = None,
        status: str | None = None,
        display_name: str | None = None,
        biz_term: str | None = None,
        definition: str | None = None,
        remark: str | None = None,
        is_deprecated: bool | None = None,
        deprecated_reason: str | None = None,
        deprecated_remark: str | None = None,
    ) -> UpdateTopLevelAsbiepServiceResult:
        """Update mutable top-level ASBIEP fields.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            version: Version value.
            status: Status value.
            display_name: ASBIEP display name.
            biz_term: ASBIEP business term.
            definition: ASBIEP definition.
            remark: ASBIEP remark.
            is_deprecated: Deprecation flag.
            deprecated_reason: Deprecation reason.
            deprecated_remark: Deprecation remark.

        Returns:
            Result of the operation.
        """
        logger.info("update top_level_asbiep id=%d", top_level_asbiep_id)
        top_level = await self._repo.get_top_level_asbiep(top_level_asbiep_id=top_level_asbiep_id)
        if top_level is None:
            raise LookupError(
                f"No top-level ASBIEP exists with ID {int(top_level_asbiep_id)}. "
                "Please verify the identifier and try again."
            )
        try:
            self._assert_can_mutate_top_level_owner_only(
                owner_user_id=int(top_level.owner_user_id),
                state=str(top_level.state),
            )
        except PermissionError as exc:
            raise PermissionError(
                "Only the owner can update the top-level ASBIEP. "
                "Please sign in as the owner of this BIE and try again."
            ) from exc
        except ValueError as exc:
            raise ValueError(
                "You can update the top-level ASBIEP only when it is in WIP state. "
                "Please move the BIE back to WIP and try again."
            ) from exc

        updates = await self._repo.update_top_level_asbiep(
            top_level_asbiep_id=top_level_asbiep_id,
            requester_user_id=int(self._requester.user.user_id),
            is_admin=self._is_admin,
            version=version,
            status=status,
            display_name=display_name,
            biz_term=biz_term,
            definition=definition,
            remark=remark,
            is_deprecated=is_deprecated,
            deprecated_reason=deprecated_reason,
            deprecated_remark=deprecated_remark,
        )
        logger.info("update top_level_asbiep id=%d → %s", top_level_asbiep_id, updates)
        return UpdateTopLevelAsbiepServiceResult(top_level_asbiep_id=top_level_asbiep_id, updates=updates)

    async def update_top_level_asbiep_state(
        self,
        *,
        top_level_asbiep_id: int,
        state: str,
    ) -> UpdateTopLevelAsbiepServiceResult:
        """Transition top-level ASBIEP lifecycle state.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            state: Target state.

        Returns:
            Result of the operation.
        """
        logger.info("update top_level_asbiep_state id=%d state=%r", top_level_asbiep_id, state)
        top_level = await self._repo.get_top_level_asbiep(top_level_asbiep_id=top_level_asbiep_id)
        if top_level is None:
            raise LookupError(
                f"No top-level ASBIEP exists with ID {int(top_level_asbiep_id)}. "
                "Please verify the identifier and try again."
            )
        self._assert_can_update_top_level_state(
            owner_user_id=int(top_level.owner_user_id),
            state=str(top_level.state or ""),
        )

        transition_service = BieStateTransitionService(
            bie_repo=self._repo,
            code_list_repo=self._code_list_repo,
            app_user_repo=self._account_service_repo,
            top_level_asbiep_id=top_level_asbiep_id,
        )
        await transition_service.ensure_state_transition_allowed(
            top_level_asbiep=top_level,
            target_state=state,
        )

        _prev_state, new_state = await self._repo.update_top_level_asbiep_state(
            top_level_asbiep_id=top_level_asbiep_id,
            requester_user_id=self._requester_user_id,
            is_admin=self._is_admin,
            new_state=state,
        )
        logger.info("update top_level_asbiep_state id=%d → %s", top_level_asbiep_id, new_state)
        return UpdateTopLevelAsbiepServiceResult(top_level_asbiep_id=top_level_asbiep_id, updates=[f"state:{new_state}"])

    async def delete_top_level_asbiep(self, *, top_level_asbiep_id: int) -> DeleteTopLevelAsbiepServiceResult:
        """Delete a top-level ASBIEP and descendants.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.

        Returns:
            Result of the operation.
        """
        logger.info("delete top_level_asbiep id=%d", top_level_asbiep_id)
        await self.ensure_delete_top_level_asbiep_allowed(top_level_asbiep_id=top_level_asbiep_id)
        return await self.execute_delete_top_level_asbiep(top_level_asbiep_id=top_level_asbiep_id)

    async def execute_delete_top_level_asbiep(self, *, top_level_asbiep_id: int) -> DeleteTopLevelAsbiepServiceResult:
        """Execute deletion after the caller has already validated permission and dependencies."""

        await self._repo.delete_top_level_asbiep(
            top_level_asbiep_id=top_level_asbiep_id,
            requester_user_id=self._requester_user_id,
            is_admin=self._is_admin,
        )
        logger.info("delete top_level_asbiep id=%d → deleted", top_level_asbiep_id)
        return DeleteTopLevelAsbiepServiceResult(top_level_asbiep_id=top_level_asbiep_id, message=None)

    async def ensure_delete_top_level_asbiep_allowed(self, *, top_level_asbiep_id: int) -> None:
        """Validate whether the requester may delete the target top-level ASBIEP."""
        top_level = await self._repo.get_top_level_asbiep(top_level_asbiep_id=top_level_asbiep_id)
        if top_level is None:
            raise LookupError(
                f"No top-level ASBIEP exists with ID {int(top_level_asbiep_id)}. "
                "Please verify the identifier and try again."
            )
        self._assert_can_delete_top_level(
            owner_user_id=int(top_level.owner_user_id),
            state=str(top_level.state),
        )
        if await self._repo.has_top_level_asbiep_openapi_reference(top_level_asbiep_id=top_level_asbiep_id):
            raise PermissionError(
                f"Cannot delete the BIE '{int(top_level_asbiep_id)}'. "
                "Please remove the BIE from the OpenAPI document first and then try again."
            )
        transition_service = BieStateTransitionService(
            bie_repo=self._repo,
            code_list_repo=self._code_list_repo,
            app_user_repo=self._account_service_repo,
            top_level_asbiep_id=top_level_asbiep_id,
        )
        await transition_service.ensure_delete_allowed(top_level_asbiep=top_level)

    async def transfer_top_level_asbiep_ownership(
        self,
        *,
        top_level_asbiep_id: int,
        target_user_id: int,
    ) -> TransferTopLevelAsbiepOwnershipServiceResult:
        """Transfer ownership of a top-level ASBIEP.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            target_user_id: Target owner user identifier.

        Returns:
            Result of the operation.
        """
        logger.info("transfer top_level_asbiep_ownership id=%d → user_id=%d", top_level_asbiep_id, target_user_id)
        top_level = await self._repo.get_top_level_asbiep(top_level_asbiep_id=top_level_asbiep_id)
        if top_level is None:
            raise LookupError(
                f"No top-level ASBIEP exists with ID {int(top_level_asbiep_id)}. "
                "Please verify the identifier and try again."
            )
        self._assert_can_transfer_top_level_asbiep_ownership(owner_user_id=int(top_level.owner_user_id))
        if target_user_id == int(top_level.owner_user_id):
            logger.info("transfer top_level_asbiep_ownership id=%d → no-op (same owner)", top_level_asbiep_id)
            return TransferTopLevelAsbiepOwnershipServiceResult(
                top_level_asbiep_id=top_level_asbiep_id,
            )

        await self._repo.transfer_top_level_asbiep_ownership(
            top_level_asbiep_id=top_level_asbiep_id,
            requester_user_id=self._requester_user_id,
            is_admin=self._is_admin,
            target_user_id=target_user_id,
        )
        logger.info("transfer top_level_asbiep_ownership id=%d → transferred to user_id=%d", top_level_asbiep_id, target_user_id)
        return TransferTopLevelAsbiepOwnershipServiceResult(
            top_level_asbiep_id=top_level_asbiep_id,
        )

    async def assign_biz_ctx_to_top_level_asbiep(
        self,
        *,
        top_level_asbiep_id: int,
        biz_ctx_id: int,
    ) -> AssignBizCtxToTopLevelAsbiepServiceResult | None:
        """Assign business context to top-level ASBIEP.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            biz_ctx_id: Business context identifier.

        Returns:
            Result of the operation.
        """
        logger.info("assign biz_ctx top_level_asbiep_id=%d biz_ctx_id=%d", top_level_asbiep_id, biz_ctx_id)
        top_level = await self._repo.get_top_level_asbiep(top_level_asbiep_id=top_level_asbiep_id)
        if top_level is None:
            raise LookupError(
                f"No top-level ASBIEP exists with ID {int(top_level_asbiep_id)}. "
                "Please verify the identifier and try again."
            )
        try:
            self._assert_can_mutate_top_level_owner_only(
                owner_user_id=int(top_level.owner_user_id),
                state=str(top_level.state),
            )
        except PermissionError as exc:
            raise PermissionError(
                "Only the owner can assign a business context to the top-level ASBIEP. "
                "Please sign in as the owner of this BIE and try again."
            ) from exc
        except ValueError as exc:
            raise ValueError(
                "You can assign a business context only when the top-level ASBIEP is in WIP state. "
                "Please move the BIE back to WIP and try again."
            ) from exc

        assigned = await self._repo.assign_biz_ctx_to_top_level_asbiep(
            top_level_asbiep_id=top_level_asbiep_id,
            biz_ctx_id=biz_ctx_id,
            requester_user_id=self._requester_user_id,
            is_admin=self._is_admin,
        )
        if not assigned:
            logger.info("assign biz_ctx top_level_asbiep_id=%d biz_ctx_id=%d → already assigned", top_level_asbiep_id, biz_ctx_id)
            return None
        logger.info("assign biz_ctx top_level_asbiep_id=%d biz_ctx_id=%d → assigned", top_level_asbiep_id, biz_ctx_id)
        return AssignBizCtxToTopLevelAsbiepServiceResult(top_level_asbiep_id=top_level_asbiep_id)

    async def unassign_biz_ctx_from_top_level_asbiep(
        self,
        *,
        top_level_asbiep_id: int,
        biz_ctx_id: int,
    ) -> UnassignBizCtxFromTopLevelAsbiepServiceResult:
        """Unassign business context from top-level ASBIEP.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            biz_ctx_id: Business context identifier.

        Returns:
            Result of the operation.
        """
        logger.info("unassign biz_ctx top_level_asbiep_id=%d biz_ctx_id=%d", top_level_asbiep_id, biz_ctx_id)
        top_level = await self._repo.get_top_level_asbiep(top_level_asbiep_id=top_level_asbiep_id)
        if top_level is None:
            raise LookupError(
                f"No top-level ASBIEP exists with ID {int(top_level_asbiep_id)}. "
                "Please verify the identifier and try again."
            )
        try:
            self._assert_can_mutate_top_level_owner_only(
                owner_user_id=int(top_level.owner_user_id),
                state=str(top_level.state),
            )
        except PermissionError as exc:
            raise PermissionError(
                "Only the owner can remove a business context from the top-level ASBIEP. "
                "Please sign in as the owner of this BIE and try again."
            ) from exc
        except ValueError as exc:
            raise ValueError(
                "You can remove a business context only when the top-level ASBIEP is in WIP state. "
                "Please move the BIE back to WIP and try again."
            ) from exc

        assigned_biz_ctx_ids = [int(item.biz_ctx_id) for item in top_level.business_contexts]
        if int(biz_ctx_id) in assigned_biz_ctx_ids and len(assigned_biz_ctx_ids) <= 1:
            raise ValueError(
                "Top-level ASBIEP must have at least one assigned business context. "
                "Please assign another business context before removing the last one."
            )

        await self._repo.unassign_biz_ctx_from_top_level_asbiep(
            top_level_asbiep_id=top_level_asbiep_id,
            biz_ctx_id=biz_ctx_id,
            requester_user_id=self._requester_user_id,
            is_admin=self._is_admin,
        )
        logger.info("unassign biz_ctx top_level_asbiep_id=%d biz_ctx_id=%d → unassigned", top_level_asbiep_id, biz_ctx_id)
        return UnassignBizCtxFromTopLevelAsbiepServiceResult(top_level_asbiep_id=top_level_asbiep_id)

    async def create_asbie(
        self,
        *,
        from_abie_id: int,
        based_ascc_manifest_id: int,
    ) -> CreateAsbieServiceResult:
        """Create or enable ASBIE.

        Args:
            from_abie_id: Parent ABIE identifier.
            based_ascc_manifest_id: ASCC manifest identifier.

        Returns:
            Result of the operation.
        """
        logger.info("create asbie from_abie_id=%d based_ascc_manifest_id=%d", from_abie_id, based_ascc_manifest_id)
        plan = await self._repo.get_asbie_create_plan(
            from_abie_id=from_abie_id,
            based_ascc_manifest_id=based_ascc_manifest_id,
        )
        try:
            self._assert_can_mutate_top_level_owner_only(
                owner_user_id=int(plan.top_level_owner_user_id),
                state=str(plan.top_level_state),
            )
        except PermissionError as exc:
            raise PermissionError(
                "Only the owner can create an ASBIE. Please sign in as the owner of this BIE and try again."
            ) from exc
        except ValueError as exc:
            raise ValueError(
                "You can create an ASBIE only when the top-level ASBIEP is in WIP state. "
                "Please move the BIE back to WIP and try again."
            ) from exc
        now = datetime.now(timezone.utc).replace(tzinfo=None)
        asbie_id: int
        existing_asbie_id = plan.existing_asbie_id
        if existing_asbie_id is not None:
            to_asbiep_id = plan.existing_to_asbiep_id
            recreate_target = to_asbiep_id is None
            if to_asbiep_id is not None:
                target = await self._repo.get_asbiep_role_plan(asbiep_id=int(to_asbiep_id))
                recreate_target = (
                    target is None
                    or int(target.based_asccp_manifest_id) != int(plan.asccp_manifest_id)
                    or int(target.role_based_acc_manifest_id) != int(plan.role_of_acc_manifest_id)
                )
            if recreate_target:
                role_of_abie_path = (
                    f"{plan.asbie_path}>ASCCP-{int(plan.asccp_manifest_id)}>ACC-{int(plan.role_of_acc_manifest_id)}"
                )
                role_of_abie_id = await self._repo.create_abie(
                    based_acc_manifest_id=int(plan.role_of_acc_manifest_id),
                    path=role_of_abie_path,
                    hash_path=hashlib.sha256(role_of_abie_path.encode()).hexdigest(),
                    owner_top_level_asbiep_id=int(plan.top_level_asbiep_id),
                    requester_user_id=int(self._requester.user.user_id),
                    now=now,
                )
                to_asbiep_path = f"{plan.asbie_path}>ASCCP-{int(plan.asccp_manifest_id)}"
                to_asbiep_id = await self._repo.create_asbiep(
                    based_asccp_manifest_id=int(plan.asccp_manifest_id),
                    role_of_abie_id=int(role_of_abie_id),
                    path=to_asbiep_path,
                    hash_path=hashlib.sha256(to_asbiep_path.encode()).hexdigest(),
                    owner_top_level_asbiep_id=int(plan.top_level_asbiep_id),
                    requester_user_id=int(self._requester.user.user_id),
                    now=now,
                )
            await self._repo.activate_existing_asbie(
                asbie_id=int(existing_asbie_id),
                requester_user_id=int(self._requester.user.user_id),
                now=now,
                to_asbiep_id=int(to_asbiep_id) if to_asbiep_id is not None else None,
            )
            asbie_id = int(existing_asbie_id)
        else:
            role_of_abie_path = (
                f"{plan.asbie_path}>ASCCP-{int(plan.asccp_manifest_id)}>ACC-{int(plan.role_of_acc_manifest_id)}"
            )
            role_of_abie_id = await self._repo.create_abie(
                based_acc_manifest_id=int(plan.role_of_acc_manifest_id),
                path=role_of_abie_path,
                hash_path=hashlib.sha256(role_of_abie_path.encode()).hexdigest(),
                owner_top_level_asbiep_id=int(plan.top_level_asbiep_id),
                requester_user_id=int(self._requester.user.user_id),
                now=now,
            )
            to_asbiep_path = f"{plan.asbie_path}>ASCCP-{int(plan.asccp_manifest_id)}"
            to_asbiep_id = await self._repo.create_asbiep(
                based_asccp_manifest_id=int(plan.asccp_manifest_id),
                role_of_abie_id=int(role_of_abie_id),
                path=to_asbiep_path,
                hash_path=hashlib.sha256(to_asbiep_path.encode()).hexdigest(),
                owner_top_level_asbiep_id=int(plan.top_level_asbiep_id),
                requester_user_id=int(self._requester.user.user_id),
                now=now,
            )
            asbie_id = await self._repo.create_asbie_record(
                based_ascc_manifest_id=based_ascc_manifest_id,
                path=str(plan.asbie_path),
                hash_path=str(plan.asbie_hash_path),
                from_abie_id=from_abie_id,
                to_asbiep_id=int(to_asbiep_id),
                cardinality_min=int(plan.ascc_cardinality_min),
                cardinality_max=int(plan.ascc_cardinality_max),
                owner_top_level_asbiep_id=int(plan.top_level_asbiep_id),
                requester_user_id=int(self._requester.user.user_id),
                now=now,
            )
        detail = await self.get_asbie_by_asbie_id(asbie_id=asbie_id)
        if detail is None:
            raise LookupError(
                f"Failed to load created ASBIE with ID {asbie_id}. "
                "Please retry the operation. If the problem persists, contact your system administrator."
            )
        logger.info("create asbie → id=%d", asbie_id)
        return CreateAsbieServiceResult(asbie_id=asbie_id, asbiep=detail.to_asbiep)

    async def update_asbie(
        self,
        *,
        asbie_id: int,
        is_used: bool | None = None,
        is_nillable: bool | None = None,
        definition: str | None = None,
        cardinality_min: int | None = None,
        cardinality_max: int | None = None,
        remark: str | None = None,
    ) -> UpdateAsbieServiceResult:
        """Update ASBIE fields.

        Args:
            asbie_id: ASBIE identifier.
            is_used: Profile flag.
            is_nillable: Nillable flag.
            definition: Definition text for this ASBIE.
            cardinality_min: Minimum cardinality.
            cardinality_max: Maximum cardinality.
            remark: Remark text for the ASBIE property.

        Returns:
            Result of the operation.
        """
        logger.info("update asbie id=%d", asbie_id)
        asbie = await self._repo.get_asbie_by_asbie_id(asbie_id=asbie_id)
        if asbie is None:
            raise LookupError(f"No ASBIE exists with ID {asbie_id}. Please verify the identifier and try again.")
        self._assert_reused_asbiep_remark_update_allowed(asbie=asbie, remark=remark)
        try:
            self._assert_can_mutate_top_level_owner_only(
                owner_user_id=int(asbie.owner_top_level_asbiep.owner_user_id),
                state=str(asbie.owner_top_level_asbiep.state),
            )
        except PermissionError as exc:
            raise PermissionError(
                "Only the owner can update the ASBIE. Please sign in as the owner of this BIE and try again."
            ) from exc
        except ValueError as exc:
            raise ValueError(
                "You can update the ASBIE only when the top-level ASBIEP is in WIP state. "
                "Please move the BIE back to WIP and try again."
            ) from exc

        updates = await self._repo.update_asbie(
            asbie_id=asbie_id,
            requester_user_id=self._requester_user_id,
            is_admin=self._is_admin,
            is_used=is_used,
            is_nillable=is_nillable,
            definition=definition,
            cardinality_min=cardinality_min,
            cardinality_max=cardinality_max,
            remark=remark,
        )
        detail = await self.get_asbie_by_asbie_id(asbie_id=asbie_id)
        logger.info("update asbie id=%d → %s", asbie_id, updates)
        return UpdateAsbieServiceResult(asbie_id=asbie_id, updates=updates, asbiep=detail.to_asbiep if detail is not None else None)

    async def create_bbie(
        self,
        *,
        from_abie_id: int,
        based_bcc_manifest_id: int,
    ) -> CreateBbieServiceResult:
        """Create or enable BBIE.

        Args:
            from_abie_id: Parent ABIE identifier.
            based_bcc_manifest_id: BCC manifest identifier.

        Returns:
            Result of the operation.
        """
        logger.info("create bbie from_abie_id=%d based_bcc_manifest_id=%d", from_abie_id, based_bcc_manifest_id)
        plan = await self._repo.get_bbie_create_plan(
            from_abie_id=from_abie_id,
            based_bcc_manifest_id=based_bcc_manifest_id,
        )
        try:
            self._assert_can_mutate_top_level_owner_only(
                owner_user_id=int(plan.top_level_owner_user_id),
                state=str(plan.top_level_state),
            )
        except PermissionError as exc:
            raise PermissionError(
                "Only the owner can create a BBIE. Please sign in as the owner of this BIE and try again."
            ) from exc
        except ValueError as exc:
            raise ValueError(
                "You can create a BBIE only when the top-level ASBIEP is in WIP state. "
                "Please move the BIE back to WIP and try again."
            ) from exc
        now = datetime.now(timezone.utc).replace(tzinfo=None)
        bbie_id: int
        existing_bbie_id = plan.existing_bbie_id
        if existing_bbie_id is not None:
            to_bbiep_id = plan.existing_to_bbiep_id
            recreate_target = to_bbiep_id is None
            if to_bbiep_id is not None:
                target = await self._repo.get_bbiep_plan(bbiep_id=int(to_bbiep_id))
                recreate_target = target is None or int(target.based_bccp_manifest_id) != int(plan.bccp_manifest_id)
            if recreate_target:
                to_bbiep_path = f"{plan.bbie_path}>BCCP-{int(plan.bccp_manifest_id)}"
                to_bbiep_id = await self._repo.create_bbiep(
                    based_bccp_manifest_id=int(plan.bccp_manifest_id),
                    path=to_bbiep_path,
                    hash_path=hashlib.sha256(to_bbiep_path.encode()).hexdigest(),
                    owner_top_level_asbiep_id=int(plan.top_level_asbiep_id),
                    requester_user_id=int(self._requester.user.user_id),
                    now=now,
                )
            await self._repo.activate_existing_bbie(
                bbie_id=int(existing_bbie_id),
                requester_user_id=int(self._requester.user.user_id),
                now=now,
                to_bbiep_id=int(to_bbiep_id) if to_bbiep_id is not None else None,
            )
            bbie_id = int(existing_bbie_id)
        else:
            to_bbiep_path = f"{plan.bbie_path}>BCCP-{int(plan.bccp_manifest_id)}"
            to_bbiep_id = await self._repo.create_bbiep(
                based_bccp_manifest_id=int(plan.bccp_manifest_id),
                path=to_bbiep_path,
                hash_path=hashlib.sha256(to_bbiep_path.encode()).hexdigest(),
                owner_top_level_asbiep_id=int(plan.top_level_asbiep_id),
                requester_user_id=int(self._requester.user.user_id),
                now=now,
            )
            bbie_id = await self._repo.create_bbie_record(
                based_bcc_manifest_id=based_bcc_manifest_id,
                path=str(plan.bbie_path),
                hash_path=str(plan.bbie_hash_path),
                from_abie_id=from_abie_id,
                to_bbiep_id=int(to_bbiep_id),
                cardinality_min=int(plan.bcc_cardinality_min),
                cardinality_max=int(plan.bcc_cardinality_max),
                is_nillable=bool(plan.bcc_is_nillable),
                default_value=plan.bcc_default_value,
                fixed_value=plan.bcc_fixed_value,
                xbt_manifest_id=plan.primitive_xbt_manifest_id,
                code_list_manifest_id=plan.primitive_code_list_manifest_id,
                agency_id_list_manifest_id=plan.primitive_agency_id_list_manifest_id,
                owner_top_level_asbiep_id=int(plan.top_level_asbiep_id),
                requester_user_id=int(self._requester.user.user_id),
                now=now,
            )
        detail = await self.get_bbie_by_bbie_id(bbie_id=bbie_id)
        if detail is None:
            raise LookupError(
                f"Failed to load created BBIE with ID {bbie_id}. "
                "Please retry the operation. If the problem persists, contact your system administrator."
            )
        logger.info("create bbie → id=%d", bbie_id)
        return CreateBbieServiceResult(bbie_id=bbie_id, bbiep=detail.to_bbiep)

    async def update_bbie(
        self,
        *,
        bbie_id: int,
        is_used: bool | None = None,
        is_nillable: bool | None = None,
        cardinality_min: int | None = None,
        cardinality_max: int | None = None,
        definition: str | None = None,
        example: str | None = None,
        remark: str | None = None,
        default_value: str | None = None,
        fixed_value: str | None = None,
        facet_min_length: int | None = None,
        facet_max_length: int | None = None,
        facet_pattern: str | None = None,
        xbt_manifest_id: int | None = None,
        code_list_manifest_id: int | None = None,
        agency_id_list_manifest_id: int | None = None,
    ) -> UpdateBbieServiceResult:
        """Update BBIE fields.

        Args:
            bbie_id: BBIE identifier.
            is_used: Profile flag.
            is_nillable: Nillable flag.
            cardinality_min: Minimum cardinality.
            cardinality_max: Maximum cardinality.
            definition: Definition text for this BBIE.
            example: Illustrative example value or content for this BBIE.
            remark: Remark text for the BBIE property.
            default_value: Default value.
            fixed_value: Fixed value.
            facet_min_length: Facet minimum length.
            facet_max_length: Facet maximum length.
            facet_pattern: Facet regular expression pattern.
            xbt_manifest_id: XBT manifest identifier to use as the primitive restriction for this BBIE.
            code_list_manifest_id: Code-list manifest identifier to use as the primitive restriction for this BBIE.
            agency_id_list_manifest_id: Agency-ID-list manifest identifier to use as the primitive restriction for this BBIE.

        Returns:
            Result of the operation.
        """
        logger.info("update bbie id=%d", bbie_id)
        bbie = await self._repo.get_bbie_by_bbie_id(bbie_id=bbie_id)
        if bbie is None:
            raise LookupError(f"No BBIE exists with ID {bbie_id}. Please verify the identifier and try again.")
        try:
            self._assert_can_mutate_top_level_owner_only(
                owner_user_id=int(bbie.owner_top_level_asbiep.owner_user_id),
                state=str(bbie.owner_top_level_asbiep.state),
            )
        except PermissionError as exc:
            raise PermissionError(
                "Only the owner can update the BBIE. Please sign in as the owner of this BIE and try again."
            ) from exc
        except ValueError as exc:
            raise ValueError(
                "You can update the BBIE only when the top-level ASBIEP is in WIP state. "
                "Please move the BIE back to WIP and try again."
            ) from exc

        updates = await self._repo.update_bbie(
            bbie_id=bbie_id,
            requester_user_id=self._requester_user_id,
            is_admin=self._is_admin,
            is_used=is_used,
            is_nillable=is_nillable,
            cardinality_min=cardinality_min,
            cardinality_max=cardinality_max,
            definition=definition,
            example=example,
            remark=remark,
            default_value=default_value,
            fixed_value=fixed_value,
            facet_min_length=facet_min_length,
            facet_max_length=facet_max_length,
            facet_pattern=facet_pattern,
            xbt_manifest_id=xbt_manifest_id,
            code_list_manifest_id=code_list_manifest_id,
            agency_id_list_manifest_id=agency_id_list_manifest_id,
        )
        detail = await self.get_bbie_by_bbie_id(bbie_id=bbie_id)
        logger.info("update bbie id=%d → %s", bbie_id, updates)
        return UpdateBbieServiceResult(bbie_id=bbie_id, updates=updates, bbiep=detail.to_bbiep if detail is not None else None)

    async def create_bbie_sc(
        self,
        *,
        bbie_id: int,
        based_dt_sc_manifest_id: int,
    ) -> CreateBbieScServiceResult:
        """Create or enable BBIE_SC.

        Args:
            bbie_id: Parent BBIE identifier.
            based_dt_sc_manifest_id: DT_SC manifest identifier.

        Returns:
            Result of the operation.
        """
        logger.info("create bbie_sc bbie_id=%d based_dt_sc_manifest_id=%d", bbie_id, based_dt_sc_manifest_id)
        bbie = await self._repo.get_bbie_by_bbie_id(bbie_id=bbie_id)
        if bbie is None:
            raise LookupError(f"No BBIE exists with ID {bbie_id}. Please verify the identifier and try again.")
        try:
            self._assert_can_mutate_top_level_owner_only(
                owner_user_id=int(bbie.owner_top_level_asbiep.owner_user_id),
                state=str(bbie.owner_top_level_asbiep.state),
            )
        except PermissionError as exc:
            raise PermissionError(
                "Only the owner can create a BBIE_SC. Please sign in as the owner of this BIE and try again."
            ) from exc
        except ValueError as exc:
            raise ValueError(
                "You can create a BBIE_SC only when the top-level ASBIEP is in WIP state. "
                "Please move the BIE back to WIP and try again."
            ) from exc

        bbie_sc_id = await self._repo.create_bbie_sc(
            bbie_id=bbie_id,
            based_dt_sc_manifest_id=based_dt_sc_manifest_id,
            requester_user_id=self._requester_user_id,
            is_admin=self._is_admin,
        )
        logger.info("create bbie_sc bbie_id=%d → id=%d", bbie_id, bbie_sc_id)
        return CreateBbieScServiceResult(bbie_sc_id=bbie_sc_id, updates=["bbie_sc_id"])

    async def update_bbie_sc(
        self,
        *,
        bbie_sc_id: int,
        is_used: bool | None = None,
        cardinality_min: int | None = None,
        cardinality_max: int | None = None,
        definition: str | None = None,
        example: str | None = None,
        remark: str | None = None,
        biz_term: str | None = None,
        display_name: str | None = None,
        default_value: str | None = None,
        fixed_value: str | None = None,
        facet_min_length: int | None = None,
        facet_max_length: int | None = None,
        facet_pattern: str | None = None,
        xbt_manifest_id: int | None = None,
        code_list_manifest_id: int | None = None,
        agency_id_list_manifest_id: int | None = None,
    ) -> UpdateBbieScServiceResult:
        """Update BBIE_SC fields.

        Args:
            bbie_sc_id: BBIE_SC identifier.
            is_used: Profile flag.
            cardinality_min: Minimum cardinality.
            cardinality_max: Maximum cardinality.
            definition: Definition text for this BBIE supplementary component.
            example: Illustrative example value or content for this BBIE supplementary component.
            remark: Remark text for this BBIE supplementary component.
            biz_term: Business term override.
            display_name: Display name override.
            default_value: Default value.
            fixed_value: Fixed value.
            facet_min_length: Facet minimum length.
            facet_max_length: Facet maximum length.
            facet_pattern: Facet regular expression pattern.
            xbt_manifest_id: XBT manifest identifier to use as the primitive restriction for this BBIE supplementary component.
            code_list_manifest_id: Code-list manifest identifier to use as the primitive restriction for this BBIE supplementary component.
            agency_id_list_manifest_id: Agency-ID-list manifest identifier to use as the primitive restriction for this BBIE supplementary component.

        Returns:
            Result of the operation.
        """
        logger.info("update bbie_sc id=%d", bbie_sc_id)
        owner_top_level = await self._repo.get_bbie_sc_owner_top_level(bbie_sc_id=bbie_sc_id)
        if owner_top_level is None:
            raise LookupError(f"No BBIE_SC exists with ID {bbie_sc_id}. Please verify the identifier and try again.")
        try:
            self._assert_can_mutate_top_level_owner_only(
                owner_user_id=int(owner_top_level.owner_user_id),
                state=str(owner_top_level.state),
            )
        except PermissionError as exc:
            raise PermissionError(
                "Only the owner can update the BBIE_SC. Please sign in as the owner of this BIE and try again."
            ) from exc
        except ValueError as exc:
            raise ValueError(
                "You can update the BBIE_SC only when the top-level ASBIEP is in WIP state. "
                "Please move the BIE back to WIP and try again."
            ) from exc

        updates = await self._repo.update_bbie_sc(
            bbie_sc_id=bbie_sc_id,
            requester_user_id=self._requester_user_id,
            is_admin=self._is_admin,
            is_used=is_used,
            cardinality_min=cardinality_min,
            cardinality_max=cardinality_max,
            definition=definition,
            example=example,
            remark=remark,
            biz_term=biz_term,
            display_name=display_name,
            default_value=default_value,
            fixed_value=fixed_value,
            facet_min_length=facet_min_length,
            facet_max_length=facet_max_length,
            facet_pattern=facet_pattern,
            xbt_manifest_id=xbt_manifest_id,
            code_list_manifest_id=code_list_manifest_id,
            agency_id_list_manifest_id=agency_id_list_manifest_id,
        )
        logger.info("update bbie_sc id=%d → %s", bbie_sc_id, updates)
        return UpdateBbieScServiceResult(bbie_sc_id=bbie_sc_id, updates=updates)

    async def get_bbie_sc_owner_top_level_asbiep_id(self, *, bbie_sc_id: int) -> int | None:
        """Resolve the owning top-level ASBIEP identifier for a BBIE_SC."""

        owner_top_level = await self._repo.get_bbie_sc_owner_top_level(bbie_sc_id=bbie_sc_id)
        if owner_top_level is None:
            return None
        return int(owner_top_level.top_level_asbiep_id)

    async def reuse_top_level_asbiep(
        self,
        *,
        asbie_id: int,
        reuse_top_level_asbiep_id: int,
    ) -> ReuseTopLevelAsbiepServiceResult:
        """Reuse another top-level ASBIEP's ASBIEP in an ASBIE.

        Args:
            asbie_id: ASBIE identifier.
            reuse_top_level_asbiep_id: Top-level ASBIEP identifier to reuse.

        Returns:
            Result of the operation.
        """
        logger.info("reuse top_level_asbiep asbie_id=%d reuse_top_level_asbiep_id=%d", asbie_id, reuse_top_level_asbiep_id)
        asbie = await self._repo.get_asbie_by_asbie_id(asbie_id=asbie_id)
        if asbie is None:
            raise LookupError(f"No ASBIE exists with ID {asbie_id}. Please verify the identifier and try again.")
        try:
            self._assert_can_mutate_top_level_owner_only(
                owner_user_id=int(asbie.owner_top_level_asbiep.owner_user_id),
                state=str(asbie.owner_top_level_asbiep.state),
            )
        except PermissionError as exc:
            raise PermissionError(
                "Only the owner can reuse a top-level ASBIEP. "
                "Please sign in as the owner of this BIE and try again."
            ) from exc
        except ValueError as exc:
            raise ValueError(
                "You can reuse a top-level ASBIEP only when the owner top-level ASBIEP is in WIP state. "
                "Please move the owner BIE back to WIP and try again."
            ) from exc

        reused_asbiep_id = await self._repo.reuse_top_level_asbiep(
            asbie_id=asbie_id,
            reuse_top_level_asbiep_id=reuse_top_level_asbiep_id,
            requester_user_id=self._requester_user_id,
            is_admin=self._is_admin,
        )
        logger.info("reuse top_level_asbiep asbie_id=%d → reused_asbiep_id=%d", asbie_id, reused_asbiep_id)
        return ReuseTopLevelAsbiepServiceResult(
            asbie_id=asbie_id,
            reused_asbiep_id=reused_asbiep_id,
            updates=["to_asbiep_id"],
        )

    async def remove_reused_top_level_asbiep(self, *, asbie_id: int) -> RemoveReusedTopLevelAsbiepServiceResult:
        """Remove reused ASBIEP by restoring an owner-local ASBIEP.

        Args:
            asbie_id: ASBIE identifier.

        Returns:
            Result of the operation.
        """
        logger.info("remove reused top_level_asbiep asbie_id=%d", asbie_id)
        asbie = await self._repo.get_asbie_by_asbie_id(asbie_id=asbie_id)
        if asbie is None:
            raise LookupError(f"No ASBIE exists with ID {asbie_id}. Please verify the identifier and try again.")
        try:
            self._assert_can_mutate_top_level_owner_only(
                owner_user_id=int(asbie.owner_top_level_asbiep.owner_user_id),
                state=str(asbie.owner_top_level_asbiep.state),
            )
        except PermissionError as exc:
            raise PermissionError(
                "Only the owner can remove a reused top-level ASBIEP. "
                "Please sign in as the owner of this BIE and try again."
            ) from exc
        except ValueError as exc:
            raise ValueError(
                "You can remove a reused top-level ASBIEP only when the owner top-level ASBIEP is in WIP state. "
                "Please move the owner BIE back to WIP and try again."
            ) from exc

        await self._repo.remove_reused_top_level_asbiep(
            asbie_id=asbie_id,
            requester_user_id=self._requester_user_id,
            is_admin=self._is_admin,
        )
        logger.info("remove reused top_level_asbiep asbie_id=%d → removed", asbie_id)
        return RemoveReusedTopLevelAsbiepServiceResult(asbie_id=asbie_id, updates=["to_asbiep_id"])

    @property
    def _requester_user_id(self) -> int:
        """Return requester user identifier."""
        return int(self._requester.user.user_id)

    @property
    def _is_admin(self) -> bool:
        """Return whether the requester has Admin role.

        Returns:
            Result of the operation.
        """
        return "Admin" in self._requester.user.roles

    def _assert_can_transfer_top_level_asbiep_ownership(self, *, owner_user_id: int) -> None:
        """Validate ownership/admin for ownership transfer."""
        if not self._is_admin and self._requester_user_id != int(owner_user_id):
            raise PermissionError(
                "Only the owner or an admin can transfer top-level ASBIEP ownership. "
                "Please sign in as the current owner or an administrator and try again."
            )

    def _assert_owner_or_admin_for_top_level(self, *, owner_user_id: int) -> None:
        """Validate ownership/admin for non-transfer top-level operations."""
        if not self._is_admin and self._requester_user_id != int(owner_user_id):
            raise PermissionError(
                "Only the owner or an admin can modify this top-level ASBIEP. "
                "Please sign in as the owner or an administrator and try again."
            )

    def _assert_owner_only(self, *, owner_user_id: int) -> None:
        """Validate requester ownership for owner-only operations."""
        if self._requester_user_id != int(owner_user_id):
            raise PermissionError(
                "Owner access is required. Please sign in as the owner of this top-level ASBIEP and try again."
            )

    def _assert_can_mutate_top_level_owner_only(self, *, owner_user_id: int, state: str) -> None:
        """Validate owner-only and WIP state for mutating operations.

        Args:
            owner_user_id: Owner user identifier of the top-level BIE.
            state: Lifecycle state of the top-level BIE.

        Raises:
            PermissionError: If requester is not owner.
            ValueError: If lifecycle state is not WIP.
        """
        self._assert_owner_only(owner_user_id=owner_user_id)
        if state != "WIP":
            raise ValueError("WIP state is required. Please move the top-level ASBIEP to WIP and try again.")

    def _assert_can_update_top_level_state(self, *, owner_user_id: int, state: str) -> None:
        """Validate whether the requester can change the top-level ASBIEP state."""
        if state == "Production":
            if not self._is_admin:
                raise PermissionError(
                    "Top-level ASBIEP in Production state cannot be changed."
                )
            return
        if self._requester_user_id != int(owner_user_id):
            raise PermissionError(
                "Only the owner can update the top-level ASBIEP state. "
                "Please sign in as the owner of this BIE and try again."
            )

    def _assert_can_delete_top_level(self, *, owner_user_id: int, state: str) -> None:
        """Validate ownership/admin and production-discard rule."""
        if state == "Production":
            if not self._is_admin:
                raise PermissionError(
                    "Top-level ASBIEP in Production state cannot be discarded unless the current user is Administrator. "
                    "Please ask an administrator for assistance before trying again."
                )
            return
        self._assert_owner_or_admin_for_top_level(owner_user_id=owner_user_id)

    def _assert_reused_asbiep_remark_update_allowed(self, *, asbie: Any, remark: str | None) -> None:
        """Reject remark updates that would mutate a reused target ASBIEP through a reusing BIE."""
        if remark is None:
            return
        owner_top_level = getattr(asbie, "owner_top_level_asbiep", None)
        target_asbiep = getattr(asbie, "to_asbiep", None)
        target_top_level = getattr(target_asbiep, "owner_top_level_asbiep", None)
        if owner_top_level is None or target_top_level is None:
            return
        owner_top_level_id = getattr(owner_top_level, "top_level_asbiep_id", None)
        target_top_level_id = getattr(target_top_level, "top_level_asbiep_id", None)
        if owner_top_level_id is None or target_top_level_id is None:
            return
        if int(owner_top_level_id) != int(target_top_level_id):
            raise ValueError(
                "Cannot update the reused ASBIEP remark through the reusing BIE. "
                "Update the reused top-level ASBIEP directly."
            )

    def _to_top_level_asbiep_list_result(
        self,
        row: Any,
        *,
        users_by_id: dict[int],
    ) -> TopLevelAsbiepListServiceResult:
        """Map a top-level ASBIEP list row into a DTO record.

        Args:
            row: Repository row model to convert into a DTO.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Result of the operation.
        """
        return TopLevelAsbiepListServiceResult(
            top_level_asbiep_id=row.top_level_asbiep_id,
            asbiep_id=row.asbiep_id,
            guid=row.guid,
            den=row.den,
            property_term=row.property_term,
            display_name=row.display_name,
            version=row.version,
            status=row.status,
            biz_term=row.biz_term,
            remark=row.remark,
            business_contexts=[to_dataclass(BizCtxSummaryServiceRecord, item) for item in row.business_contexts],
            state=row.state,
            is_deprecated=row.is_deprecated,
            deprecated_reason=row.deprecated_reason,
            deprecated_remark=row.deprecated_remark,
            library=to_dataclass(LibrarySummaryServiceRecord, row.library),
            release=to_dataclass(ReleaseSummaryServiceRecord, row.release),
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

    def _to_top_level_info_dto(
        self,
        row: TopLevelAsbiepInfoRow | None,
        *,
        users_by_id: dict[int],
    ) -> TopLevelAsbiepInfoRecord | None:
        """Map top-level ASBIEP summary row into a DTO, preserving null metadata.

        Args:
            row: Repository row model to convert into a DTO.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Result of the operation.
        """
        if row is None:
            return None
        return TopLevelAsbiepInfoRecord(
            top_level_asbiep_id=row.top_level_asbiep_id,
            library=to_dataclass(LibrarySummaryServiceRecord, row.library),
            release=to_dataclass(ReleaseSummaryServiceRecord, row.release),
            version=row.version,
            status=row.status,
            state=row.state,
            is_deprecated=row.is_deprecated,
            deprecated_reason=row.deprecated_reason,
            deprecated_remark=row.deprecated_remark,
            owner=to_user_summary(int(row.owner_user_id), users_by_id=users_by_id),
            created=(
                WhoAndWhen(
                    who=to_user_summary(int(row.created_by), users_by_id=users_by_id),
                    when=row.creation_timestamp,
                )
                if row.created_by is not None and row.creation_timestamp is not None
                else None
            ),
            last_updated=(
                WhoAndWhen(
                    who=to_user_summary(int(row.last_updated_by), users_by_id=users_by_id),
                    when=row.last_update_timestamp,
                )
                if row.last_updated_by is not None and row.last_update_timestamp is not None
                else None
            ),
        )

    def _to_abie_dto(self, row: AbieInfoRow, *, users_by_id: dict[int]) -> AbieInfoRecord:
        """Map ABIE row and mixed relationship rows into an ABIE DTO.

        Args:
            row: Repository row model to convert into a DTO.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Result of the operation.
        """
        relationships: list[AsbieRelationshipServiceRecord | BbieRelationshipServiceRecord] = []
        for rel in row.relationships:
            if isinstance(rel, AsbieRelationshipRow):
                relationships.append(
                    AsbieRelationshipServiceRecord(
                        component_type=rel.component_type,
                        asbie_id=rel.asbie_id,
                        guid=rel.guid,
                        based_ascc=to_dataclass(AsccInfoRecord, rel.based_ascc),
                        to_asbiep_id=rel.to_asbiep_id,
                        is_used=rel.is_used,
                        path=rel.path,
                        hash_path=rel.hash_path,
                        cardinality_min=rel.cardinality_min,
                        cardinality_max=rel.cardinality_max,
                        is_nillable=rel.is_nillable,
                        remark=rel.remark,
                    )
                )
            else:
                relationships.append(
                    BbieRelationshipServiceRecord(
                        component_type=rel.component_type,
                        bbie_id=rel.bbie_id,
                        guid=rel.guid,
                        based_bcc=to_dataclass(BccInfoRecord, rel.based_bcc),
                        to_bbiep_id=rel.to_bbiep_id,
                        is_used=rel.is_used,
                        path=rel.path,
                        hash_path=rel.hash_path,
                        cardinality_min=rel.cardinality_min,
                        cardinality_max=rel.cardinality_max,
                        is_nillable=rel.is_nillable,
                        remark=rel.remark,
                        primitiveRestriction=(
                            self._to_primitive_restriction_dto(rel.primitive_restriction)
                            if rel.primitive_restriction
                            else None
                        ),
                        valueConstraint=to_dataclass(ValueConstraintServiceRecord, rel.value_constraint)
                        if rel.value_constraint
                        else None,
                        facet=to_dataclass(FacetServiceRecord, rel.facet) if rel.facet else None,
                    )
                )
        return AbieInfoRecord(
            abie_id=row.abie_id,
            guid=row.guid,
            path=row.path,
            hash_path=row.hash_path,
            based_acc_manifest=to_dataclass(AccInfoRecord, row.based_acc_manifest),
            definition=row.definition,
            remark=row.remark,
            relationships=relationships,
            created=(
                WhoAndWhen(
                    who=to_user_summary(int(row.created_by), users_by_id=users_by_id),
                    when=row.creation_timestamp,
                )
                if row.created_by is not None and row.creation_timestamp is not None
                else None
            ),
            last_updated=(
                WhoAndWhen(
                    who=to_user_summary(int(row.last_updated_by), users_by_id=users_by_id),
                    when=row.last_update_timestamp,
                )
                if row.last_updated_by is not None and row.last_update_timestamp is not None
                else None
            ),
        )

    def _to_asbiep_dto(self, row: AsbiepInfoRow, *, users_by_id: dict[int]) -> AsbiepInfoRecord:
        """Map ASBIEP row into DTO including role-of ABIE projection.

        Args:
            row: Repository row model to convert into a DTO.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Result of the operation.
        """
        return AsbiepInfoRecord(
            asbiep_id=row.asbiep_id,
            guid=row.guid,
            owner_top_level_asbiep=self._to_top_level_info_dto(row.owner_top_level_asbiep, users_by_id=users_by_id),
            based_asccp_manifest=to_dataclass(AsccpInfoRecord, row.based_asccp_manifest),
            path=row.path,
            hash_path=row.hash_path,
            role_of_abie=self._to_abie_dto(row.role_of_abie, users_by_id=users_by_id),
            definition=row.definition,
            remark=row.remark,
            biz_term=row.biz_term,
            display_name=row.display_name,
            created=(
                WhoAndWhen(
                    who=to_user_summary(int(row.created_by), users_by_id=users_by_id),
                    when=row.creation_timestamp,
                )
                if row.created_by is not None and row.creation_timestamp is not None
                else None
            ),
            last_updated=(
                WhoAndWhen(
                    who=to_user_summary(int(row.last_updated_by), users_by_id=users_by_id),
                    when=row.last_update_timestamp,
                )
                if row.last_updated_by is not None and row.last_update_timestamp is not None
                else None
            ),
        )

    def _to_top_level_detail_dto(
        self,
        row: TopLevelAsbiepDetailRow,
        *,
        users_by_id: dict[int],
    ) -> GetTopLevelAsbiepServiceResult:
        """Map top-level ASBIEP detail row into API-facing DTO.

        Args:
            row: Repository row model to convert into a DTO.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Result of the operation.
        """
        return GetTopLevelAsbiepServiceResult(
            top_level_asbiep_id=row.top_level_asbiep_id,
            asbiep=self._to_asbiep_dto(row.asbiep, users_by_id=users_by_id),
            version=row.version,
            status=row.status,
            business_contexts=[to_dataclass(BizCtxSummaryServiceRecord, item) for item in row.business_contexts],
            state=row.state,
            is_deprecated=row.is_deprecated,
            deprecated_reason=row.deprecated_reason,
            deprecated_remark=row.deprecated_remark,
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

    def _to_asbie_dto(self, row: GetAsbieRow, *, users_by_id: dict[int]) -> GetAsbieServiceResult:
        """Map ASBIE detail row into a DTO.

        Args:
            row: Repository row model to convert into a DTO.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Result of the operation.
        """
        return GetAsbieServiceResult(
            asbie_id=row.asbie_id,
            owner_top_level_asbiep=self._to_top_level_info_dto(row.owner_top_level_asbiep, users_by_id=users_by_id),
            guid=row.guid,
            based_ascc=to_dataclass(AsccInfoRecord, row.based_ascc),
            to_asbiep=self._to_asbiep_dto(row.to_asbiep, users_by_id=users_by_id),
            is_used=row.is_used,
            cardinality_min=row.cardinality_min,
            cardinality_max=row.cardinality_max,
            is_nillable=row.is_nillable,
            definition=row.definition,
            remark=row.remark,
            created=(
                WhoAndWhen(
                    who=to_user_summary(int(row.created_by), users_by_id=users_by_id),
                    when=row.creation_timestamp,
                )
                if row.created_by is not None and row.creation_timestamp is not None
                else None
            ),
            last_updated=(
                WhoAndWhen(
                    who=to_user_summary(int(row.last_updated_by), users_by_id=users_by_id),
                    when=row.last_update_timestamp,
                )
                if row.last_updated_by is not None and row.last_update_timestamp is not None
                else None
            ),
        )

    def _to_bbiep_dto(self, row: BbiepInfoRow, *, users_by_id: dict[int]) -> BbiepInfoRecord:
        """Map BBIEP row (including supplementary components) into a DTO.

        Args:
            row: Repository row model to convert into a DTO.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Result of the operation.
        """
        supplementary_components = [
            BbieScInfoRecord(
                bbie_sc_id=sc.bbie_sc_id,
                guid=sc.guid,
                based_dt_sc=to_dataclass(DataTypeSupplementaryComponentServiceRecord, sc.based_dt_sc),
                path=sc.path,
                hash_path=sc.hash_path,
                definition=sc.definition,
                biz_term=sc.biz_term,
                display_name=sc.display_name,
                cardinality_min=sc.cardinality_min,
                cardinality_max=sc.cardinality_max,
                primitiveRestriction=(
                    self._to_primitive_restriction_dto(sc.primitive_restriction)
                    if sc.primitive_restriction
                    else None
                ),
                valueConstraint=to_dataclass(ValueConstraintServiceRecord, sc.value_constraint)
                if sc.value_constraint
                else None,
                facet=to_dataclass(FacetServiceRecord, sc.facet) if sc.facet else None,
                owner_top_level_asbiep=self._to_top_level_info_dto(sc.owner_top_level_asbiep, users_by_id=users_by_id),
            )
            for sc in row.supplementary_components
        ]
        return BbiepInfoRecord(
            bbiep_id=row.bbiep_id,
            guid=row.guid,
            based_bccp=to_dataclass(BccpInfoRecord, row.based_bccp),
            path=row.path,
            hash_path=row.hash_path,
            definition=row.definition,
            remark=row.remark,
            biz_term=row.biz_term,
            display_name=row.display_name,
            supplementary_components=supplementary_components,
            owner_top_level_asbiep=self._to_top_level_info_dto(row.owner_top_level_asbiep, users_by_id=users_by_id),
            created=(
                WhoAndWhen(
                    who=to_user_summary(int(row.created_by), users_by_id=users_by_id),
                    when=row.creation_timestamp,
                )
                if row.created_by is not None and row.creation_timestamp is not None
                else None
            ),
            last_updated=(
                WhoAndWhen(
                    who=to_user_summary(int(row.last_updated_by), users_by_id=users_by_id),
                    when=row.last_update_timestamp,
                )
                if row.last_updated_by is not None and row.last_update_timestamp is not None
                else None
            ),
        )

    def _to_bbie_dto(self, row: GetBbieRow, *, users_by_id: dict[int]) -> GetBbieServiceResult:
        """Map BBIE detail row into a DTO.

        Args:
            row: Repository row model to convert into a DTO.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Result of the operation.
        """
        return GetBbieServiceResult(
            bbie_id=row.bbie_id,
            owner_top_level_asbiep=self._to_top_level_info_dto(row.owner_top_level_asbiep, users_by_id=users_by_id),
            guid=row.guid,
            based_bcc=to_dataclass(BccInfoRecord, row.based_bcc),
            to_bbiep=self._to_bbiep_dto(row.to_bbiep, users_by_id=users_by_id),
            is_used=row.is_used,
            cardinality_min=row.cardinality_min,
            cardinality_max=row.cardinality_max,
            is_nillable=row.is_nillable,
            remark=row.remark,
            primitiveRestriction=(
                self._to_primitive_restriction_dto(row.primitive_restriction)
                if row.primitive_restriction
                else None
            ),
            valueConstraint=to_dataclass(ValueConstraintServiceRecord, row.value_constraint)
            if row.value_constraint
            else None,
            facet=to_dataclass(FacetServiceRecord, row.facet) if row.facet else None,
            created=(
                WhoAndWhen(
                    who=to_user_summary(int(row.created_by), users_by_id=users_by_id),
                    when=row.creation_timestamp,
                )
                if row.created_by is not None and row.creation_timestamp is not None
                else None
            ),
            last_updated=(
                WhoAndWhen(
                    who=to_user_summary(int(row.last_updated_by), users_by_id=users_by_id),
                    when=row.last_update_timestamp,
                )
                if row.last_updated_by is not None and row.last_update_timestamp is not None
                else None
            ),
        )

    @staticmethod
    def _to_primitive_restriction_dto(row: Any) -> PrimitiveRestrictionServiceRecord:
        """Map repository primitive restriction row into the DTO shape.

        Args:
            row: Repository row model to convert into a DTO.

        Returns:
            Result of the operation.
        """
        return PrimitiveRestrictionServiceRecord(
            xbtManifestId=row.xbt_manifest_id,
            codeListManifestId=row.code_list_manifest_id,
            agencyIdListManifestId=row.agency_id_list_manifest_id,
        )
