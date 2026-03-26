"""Service class for managing Core Component operations in connectCenter.

Core Components are the foundational building blocks of business information
standards:
- ACC (Aggregate Core Component): Complex aggregate structures.
- ASCCP (Association Core Component Property): Association properties to ACCs.
- BCCP (Basic Core Component Property): Basic scalar-like properties.

These abbreviations and semantics follow CCTS (UN/CEFACT Core Components
Technical Specification), standardized as ISO 15000-5.

This service coordinates repository-level reads and release dependency logic to
deliver SCORE-style behavior in a backend-friendly contract:
- Unified list retrieval across component types.
- Type-specific detail retrieval by manifest ID.
- Release dependency expansion (include dependent releases in list queries).
- Filter/pagination/sort handoff with defensive type validation.
"""


from __future__ import annotations

import logging

from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.repositories.contracts.core_component import CoreComponentRepositoryContract
from app.security import AuthenticatedUser
from app.services import load_users_by_ids, to_user_summary, UserSummary
from app.services.models import WhoAndWhen
from app.services.models.core_component import (
    AsccRelationshipServiceRecord,
    BccRelationshipServiceRecord,
    BaseAccSummaryServiceRecord,
    CoreComponentServiceResult,
    DataTypeSummaryServiceRecord,
    GetAccServiceResult,
    GetAsccpServiceResult,
    GetBccpServiceResult,
    ValueConstraintServiceRecord,
)
from app.services.models.library import LibrarySummaryServiceRecord
from app.services.models.log import LogSummaryServiceRecord
from app.services.models.mapper import to_dataclass
from app.services.models.namespace import NamespaceSummaryServiceRecord
from app.services.models.release import ReleaseSummaryServiceRecord
from app.services.release_service import ReleaseService
from app.services.utils.date import DateRange
from app.services.utils.pagination import PaginationParams, PaginationResponse
from app.types.identifiers import AccManifestId, AsccpManifestId, BccpManifestId
from app.types.identifiers import ReleaseId

logger = logging.getLogger("connectcenter.service.core_component")


class CoreComponentService:
    """Service façade for Core Component read operations.

    Responsibilities:
    - Validate requested component types against the supported type set.
    - Resolve dependent release IDs through ReleaseService before list queries.
    - Delegate data access and projection to repository implementations.

    Supported list sort keys:
    - `den`
    - `name`
    - `definition`
    - `creation_timestamp`
    - `last_update_timestamp`
    """

    _ORDER_BY_ALLOWED: set[str] = {
        "den",
        "name",
        "definition",
        "creation_timestamp",
        "last_update_timestamp",
    }

    def __init__(
        self,
        core_component_repository: CoreComponentRepositoryContract,
        release_service: ReleaseService,
        account_service_repo: AppUserRepositoryContract,
        requester: AuthenticatedUser,
    ):
        """Initialize service with component, release, and account dependencies.

        Args:
            core_component_repository: Core-component repository dependency.
            release_service: Release service dependency.
            account_service_repo: Account repository used to resolve user summaries.
            requester: Requesting user context.
        """
        self._repo = core_component_repository
        self._release_service = release_service
        self._account_service_repo = account_service_repo
        self._requester = requester

    async def list(
        self,
        *,
        release_id: ReleaseId,
        types: list[str],
        limit: int,
        offset: int,
        order_by: str | None = None,
        den: str | None = None,
        tag: str | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
    ) -> PaginationResponse[CoreComponentServiceResult]:
        """Get a unified list of core components for the target release scope.

        Args:
            release_id: Release identifier used to scope the query.
            types: Requested core-component types.
            limit: Maximum number of items to return.
            offset: Zero-based index of the first item in the page.
            order_by: Sort expression for the result set.

        Returns:
            Result of the operation.
        """
        logger.info("list core_components release_id=%d types=%s limit=%d offset=%d", int(release_id), types, limit, offset)
        pagination = PaginationParams.from_query(
            limit=limit,
            offset=offset,
            order_by=order_by,
            allowed_sort_columns=self._ORDER_BY_ALLOWED,
        )
        release = await self._release_service.get(release_id)
        if release is None:
            raise LookupError(f"No release exists with ID {int(release_id)}. Please verify the identifier and try again.")

        valid_types = {"ACC", "ASCCP", "BCCP"}
        invalid = [t for t in types if t not in valid_types]
        if invalid:
            raise ValueError(
                f"Invalid component types: {', '.join(invalid)}. Allowed values are: ACC, ASCCP, BCCP. "
                "Please choose only supported component types and try again."
            )

        dependent_release_ids = await self._release_service.get_dependent_releases(release_id)
        total, rows = await self._repo.list(
            release_id=release_id,
            dependent_release_ids=dependent_release_ids,
            types=types,
            limit=pagination.limit,
            offset=pagination.offset,
            sorts=[(s.column, s.direction.upper()) for s in pagination.sorts],
            den=den,
            tag=tag,
            creation_timestamp_before=created_on.before if created_on else None,
            creation_timestamp_after=created_on.after if created_on else None,
            last_update_timestamp_before=last_updated_on.before if last_updated_on else None,
            last_update_timestamp_after=last_updated_on.after if last_updated_on else None,
        )
        # Repository intentionally returns only AppUser IDs. We "join" AppUser at
        # service layer via one batched lookup so account data can be cached.
        user_ids = sorted(
            {
                user_id
                for raw in rows
                for user_id in (raw.owner_user_id, raw.created_by, raw.last_updated_by)
            },
            key=int,
        )
        users_by_id = await load_users_by_ids(self._account_service_repo, user_ids)
        items = []
        for raw in rows:
            owner, created_who, updated_who = await self._enrich_audit_users(
                owner_user_id=int(raw.owner_user_id),
                created_by=int(raw.created_by),
                last_updated_by=int(raw.last_updated_by),
                users_by_id=users_by_id,
            )
            items.append(
                CoreComponentServiceResult(
                    component_type=raw.component_type,
                    manifest_id=raw.manifest_id,
                    component_id=raw.component_id,
                    guid=raw.guid,
                    den=raw.den,
                    name=raw.name,
                    definition=raw.definition,
                    definition_source=raw.definition_source,
                    is_deprecated=raw.is_deprecated,
                    state=raw.state,
                    namespace=to_dataclass(NamespaceSummaryServiceRecord, raw.namespace),
                    library=to_dataclass(LibrarySummaryServiceRecord, raw.library),
                    release=to_dataclass(ReleaseSummaryServiceRecord, raw.release),
                    log=to_dataclass(LogSummaryServiceRecord, raw.log) if raw.log is not None else None,
                    owner=owner,
                    created=WhoAndWhen(who=created_who, when=raw.creation_timestamp),
                    last_updated=WhoAndWhen(who=updated_who, when=raw.last_update_timestamp),
                    tag=raw.tag,
                )
            )
        logger.info("list core_components release_id=%d → %d/%d", int(release_id), len(items), total)
        return PaginationResponse(items=items, total=total, limit=pagination.limit, offset=pagination.offset)

    async def get_acc(self, acc_manifest_id: AccManifestId) -> GetAccServiceResult | None:
        """Get ACC details by manifest ID.

        Args:
            acc_manifest_id: ACC manifest identifier.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get_acc(acc_manifest_id)
        if row is None:
            logger.info("get acc id=%d → not found", int(acc_manifest_id))
            return None
        owner, created_who, updated_who = await self._enrich_audit_users(
            owner_user_id=int(row.owner_user_id),
            created_by=int(row.created_by),
            last_updated_by=int(row.last_updated_by),
        )
        logger.info("get acc id=%d → found", int(acc_manifest_id))
        payload = GetAccServiceResult(
            acc_manifest_id=row.acc_manifest_id,
            acc_id=row.acc_id,
            base_acc=to_dataclass(BaseAccSummaryServiceRecord, row.base_acc) if row.base_acc is not None else None,
            relationships=[
                to_dataclass(AsccRelationshipServiceRecord, relationship)
                if getattr(relationship, "component_type", None) == "ASCC"
                else to_dataclass(BccRelationshipServiceRecord, relationship)
                for relationship in row.relationships
            ],
            guid=row.guid,
            den=row.den,
            object_class_term=row.object_class_term,
            definition=row.definition,
            definition_source=row.definition_source,
            object_class_qualifier=row.object_class_qualifier,
            component_type=row.component_type,
            is_abstract=row.is_abstract,
            is_deprecated=row.is_deprecated,
            state=row.state,
            namespace=to_dataclass(NamespaceSummaryServiceRecord, row.namespace),
            library=to_dataclass(LibrarySummaryServiceRecord, row.library),
            release=to_dataclass(ReleaseSummaryServiceRecord, row.release),
            log=to_dataclass(LogSummaryServiceRecord, row.log) if row.log is not None else None,
            owner=owner,
            created=WhoAndWhen(
                who=created_who,
                when=row.creation_timestamp,
            ),
            last_updated=WhoAndWhen(
                who=updated_who,
                when=row.last_update_timestamp,
            ),
        )
        return payload

    async def get_asccp(self, asccp_manifest_id: AsccpManifestId) -> GetAsccpServiceResult | None:
        """Get ASCCP details by manifest ID.

        Args:
            asccp_manifest_id: ASCCP manifest identifier.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get_asccp(asccp_manifest_id)
        if row is None:
            logger.info("get asccp id=%d → not found", int(asccp_manifest_id))
            return None
        owner, created_who, updated_who = await self._enrich_audit_users(
            owner_user_id=int(row.owner_user_id),
            created_by=int(row.created_by),
            last_updated_by=int(row.last_updated_by),
        )
        logger.info("get asccp id=%d → found", int(asccp_manifest_id))
        payload = GetAsccpServiceResult(
            asccp_manifest_id=row.asccp_manifest_id,
            asccp_id=row.asccp_id,
            role_of_acc=to_dataclass(BaseAccSummaryServiceRecord, row.role_of_acc),
            guid=row.guid,
            den=row.den,
            property_term=row.property_term,
            definition=row.definition,
            definition_source=row.definition_source,
            reusable_indicator=row.reusable_indicator,
            is_nillable=row.is_nillable,
            is_deprecated=row.is_deprecated,
            state=row.state,
            namespace=to_dataclass(NamespaceSummaryServiceRecord, row.namespace),
            library=to_dataclass(LibrarySummaryServiceRecord, row.library),
            release=to_dataclass(ReleaseSummaryServiceRecord, row.release),
            log=to_dataclass(LogSummaryServiceRecord, row.log) if row.log is not None else None,
            owner=owner,
            created=WhoAndWhen(
                who=created_who,
                when=row.creation_timestamp,
            ),
            last_updated=WhoAndWhen(
                who=updated_who,
                when=row.last_update_timestamp,
            ),
        )
        return payload

    async def get_bccp(self, bccp_manifest_id: BccpManifestId) -> GetBccpServiceResult | None:
        """Get BCCP details by manifest ID.

        Args:
            bccp_manifest_id: BCCP manifest identifier.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get_bccp(bccp_manifest_id)
        if row is None:
            logger.info("get bccp id=%d → not found", int(bccp_manifest_id))
            return None
        owner, created_who, updated_who = await self._enrich_audit_users(
            owner_user_id=int(row.owner_user_id),
            created_by=int(row.created_by),
            last_updated_by=int(row.last_updated_by),
        )
        logger.info("get bccp id=%d → found", int(bccp_manifest_id))
        payload = GetBccpServiceResult(
            bccp_manifest_id=row.bccp_manifest_id,
            bccp_id=row.bccp_id,
            bdt=to_dataclass(DataTypeSummaryServiceRecord, row.bdt),
            guid=row.guid,
            den=row.den,
            property_term=row.property_term,
            representation_term=row.representation_term,
            definition=row.definition,
            definition_source=row.definition_source,
            is_nillable=row.is_nillable,
            value_constraint=to_dataclass(ValueConstraintServiceRecord, row.value_constraint)
            if row.value_constraint is not None
            else None,
            is_deprecated=row.is_deprecated,
            state=row.state,
            namespace=to_dataclass(NamespaceSummaryServiceRecord, row.namespace),
            library=to_dataclass(LibrarySummaryServiceRecord, row.library),
            release=to_dataclass(ReleaseSummaryServiceRecord, row.release),
            log=to_dataclass(LogSummaryServiceRecord, row.log) if row.log is not None else None,
            owner=owner,
            created=WhoAndWhen(
                who=created_who,
                when=row.creation_timestamp,
            ),
            last_updated=WhoAndWhen(
                who=updated_who,
                when=row.last_update_timestamp,
            ),
        )
        return payload

    async def _enrich_audit_users(
        self,
        *,
        owner_user_id: int,
        created_by: int,
        last_updated_by: int,
        users_by_id: dict[int] | None = None,
    ) -> tuple[UserSummary, UserSummary, UserSummary]:
        """Resolve owner/created/updated users into summaries.

        Args:
            owner_user_id: Identifier of the owning user.
            created_by: Identifier of the user who created the record.
            last_updated_by: Identifier of the user who last updated the record.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Result of the operation.
        """
        owner_id = owner_user_id
        created_id = created_by
        updated_id = last_updated_by
        if users_by_id is None:
            user_ids = sorted(
                {
                    owner_id,
                    created_id,
                    updated_id,
                },
                key=int,
            )
            users_by_id = await load_users_by_ids(self._account_service_repo, user_ids)
        owner = to_user_summary(owner_id, users_by_id=users_by_id)
        created_who = to_user_summary(created_id, users_by_id=users_by_id)
        updated_who = to_user_summary(updated_id, users_by_id=users_by_id)
        return owner, created_who, updated_who
