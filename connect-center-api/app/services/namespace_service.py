"""Service layer for Namespace operations in connectCenter."""


from __future__ import annotations

import logging
from typing import Any

from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.repositories.contracts.namespace import NamespaceRepositoryContract
from app.repositories.models.app_user import AppUserRow
from app.security import AuthenticatedUser
from app.services import load_users_by_ids, to_user_summary
from app.services.models import WhoAndWhen
from app.services.models.namespace import (
    CreateNamespaceServiceResult,
    NamespaceServiceResult,
    TransferNamespaceOwnershipServiceResult,
    UpdateNamespaceServiceResult,
)
from app.services.utils.date import DateRange
from app.services.utils.owner import parse_login_id_filter, parse_owner_filter
from app.services.utils.pagination import PaginationParams, PaginationResponse
from app.types.identifiers import LibraryId, NamespaceId
from app.types.unset import UNSET, UnsetType

logger = logging.getLogger("connectcenter.service.namespace")


class NamespaceService:
    """Service class for namespace read and command operations."""

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
        owner: str | None = None,
        updater: str | None = None,
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
        included_owner_login_ids, excluded_owner_login_ids = parse_owner_filter(owner)
        included_updater_login_ids, excluded_updater_login_ids = parse_login_id_filter(
            updater,
            filter_name="updater",
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
            included_owner_login_ids=included_owner_login_ids,
            excluded_owner_login_ids=excluded_owner_login_ids,
            included_updater_login_ids=included_updater_login_ids,
            excluded_updater_login_ids=excluded_updater_login_ids,
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

    async def create_namespace(
        self,
        *,
        library_id: LibraryId,
        uri: str,
        prefix: str | None = None,
        description: str | None = None,
    ) -> CreateNamespaceServiceResult:
        """Create a namespace following score-http namespace rules."""
        normalized_uri = self._normalize_uri(uri)
        normalized_prefix = self._normalize_prefix(prefix)

        if not await self._repo.library_exists(library_id):
            raise LookupError(f"No library exists with ID {int(library_id)}. Please verify the identifier and try again.")
        if await self._repo.has_duplicate_uri(library_id=library_id, uri=normalized_uri):
            raise ValueError(f"Namespace URI '{normalized_uri}' already exists.")
        if await self._repo.has_duplicate_prefix(library_id=library_id, prefix=normalized_prefix):
            raise ValueError(f"Namespace Prefix '{normalized_prefix}' already exists.")

        namespace_id = await self._repo.create_namespace(
            library_id=library_id,
            uri=normalized_uri,
            prefix=normalized_prefix,
            description=description,
            requester_user_id=self._requester_user_id,
            requester_is_developer=self._requester_is_developer(),
        )
        return CreateNamespaceServiceResult(namespace_id=int(namespace_id))

    async def update_namespace(
        self,
        *,
        namespace_id: NamespaceId,
        uri: str | UnsetType = UNSET,
        prefix: str | None | UnsetType = UNSET,
        description: str | None | UnsetType = UNSET,
    ) -> UpdateNamespaceServiceResult:
        """Update a namespace owned by the requester."""
        namespace = await self.get(namespace_id)
        if namespace is None:
            raise LookupError(f"No namespace exists with ID {int(namespace_id)}. Please verify the identifier and try again.")
        self._assert_owner_only(namespace)

        normalized_uri = namespace.uri if uri is UNSET else self._normalize_uri(uri)
        normalized_prefix = (namespace.prefix or "") if prefix is UNSET else self._normalize_prefix(prefix)
        next_description = namespace.description if description is UNSET else description
        library_id = LibraryId(int(namespace.library.library_id))

        if uri is not UNSET and namespace.uri != normalized_uri:
            if await self._repo.has_duplicate_uri(
                library_id=library_id,
                uri=normalized_uri,
                exclude_namespace_id=namespace_id,
            ):
                raise ValueError(f"Namespace URI '{normalized_uri}' already exists.")
        if prefix is not UNSET and (namespace.prefix or "") != normalized_prefix:
            if await self._repo.has_duplicate_prefix(
                library_id=library_id,
                prefix=normalized_prefix,
                exclude_namespace_id=namespace_id,
            ):
                raise ValueError(f"Namespace Prefix '{normalized_prefix}' already exists.")

        updates: list[str] = []
        if uri is not UNSET and namespace.uri != normalized_uri:
            updates.append("uri")
        if prefix is not UNSET and (namespace.prefix or "") != normalized_prefix:
            updates.append("prefix")
        if description is not UNSET and namespace.description != next_description:
            updates.append("description")

        if not updates:
            return UpdateNamespaceServiceResult(namespace_id=int(namespace_id), updates=[])

        updated = await self._repo.update_namespace(
            namespace_id=namespace_id,
            uri=normalized_uri,
            prefix=normalized_prefix,
            description=next_description,
            requester_user_id=self._requester_user_id,
        )
        if not updated:
            raise PermissionError("Only the namespace owner can update the namespace.")
        return UpdateNamespaceServiceResult(namespace_id=int(namespace_id), updates=updates)

    async def discard_namespace(
        self,
        *,
        namespace_id: NamespaceId,
    ) -> None:
        """Discard a namespace when it is not in use."""
        namespace = await self.get(namespace_id)
        if namespace is None:
            raise LookupError(f"No namespace exists with ID {int(namespace_id)}. Please verify the identifier and try again.")
        self._assert_owner_only(namespace)

        if await self._repo.namespace_is_used(namespace_id=namespace_id):
            raise ValueError("The namespace in use cannot be discarded.")

        deleted = await self._repo.discard_namespace(namespace_id=namespace_id)
        if not deleted:
            raise LookupError(f"No namespace exists with ID {int(namespace_id)}. Please verify the identifier and try again.")

    async def transfer_namespace_ownership(
        self,
        *,
        namespace_id: NamespaceId,
        target_login_id: str,
    ) -> TransferNamespaceOwnershipServiceResult:
        """Transfer namespace ownership to another user by login ID."""
        namespace = await self.get(namespace_id)
        if namespace is None:
            raise LookupError(f"No namespace exists with ID {int(namespace_id)}. Please verify the identifier and try again.")
        self._assert_can_transfer(namespace)

        normalized_login_id = target_login_id.strip()
        if not normalized_login_id:
            raise ValueError("`target_login_id` is required.")

        target_user = await self._account_service_repo.get_auth_by_login_id(normalized_login_id)
        if target_user is None:
            raise LookupError(
                f"No user exists with login ID '{normalized_login_id}'. Please verify the identifier and try again."
            )

        target_is_developer = bool(target_user.is_developer)
        if bool(namespace.is_std_nmsp) != target_is_developer:
            raise ValueError(
                "Standard namespaces cannot be transferred to End Users."
                if namespace.is_std_nmsp
                else "Non-standard namespaces cannot be transferred to Developers."
            )

        if int(namespace.owner.user_id) == int(target_user.app_user_id):
            return TransferNamespaceOwnershipServiceResult(namespace_id=int(namespace_id), updates=[])

        transferred = await self._repo.transfer_namespace_ownership(
            namespace_id=namespace_id,
            requester_user_id=self._requester_user_id,
            target_user_id=int(target_user.app_user_id),
        )
        if not transferred:
            raise PermissionError("Ownership transfer failed due to insufficient permissions.")

        return TransferNamespaceOwnershipServiceResult(
            namespace_id=int(namespace_id),
            updates=["owner_user_id"],
        )

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

    @property
    def _requester_user_id(self) -> int:
        return int(self._requester.user.user_id)

    def _is_admin(self) -> bool:
        return "Admin" in self._requester.user.roles

    def _requester_is_developer(self) -> bool:
        return "Developer" in self._requester.user.roles

    @staticmethod
    def _normalize_uri(uri: str) -> str:
        normalized = uri.strip()
        if not normalized:
            raise ValueError("`uri` is required.")
        return normalized

    @staticmethod
    def _normalize_prefix(prefix: str | None) -> str:
        if prefix is None:
            return ""
        return prefix.strip()

    def _assert_owner_only(self, namespace: NamespaceServiceResult) -> None:
        if int(namespace.owner.user_id) != self._requester_user_id:
            raise PermissionError("Only the namespace owner can modify the namespace.")

    def _assert_can_transfer(self, namespace: NamespaceServiceResult) -> None:
        if not self._is_admin() and int(namespace.owner.user_id) != self._requester_user_id:
            raise PermissionError("Only the namespace owner can transfer ownership.")
