"""Service layer for Library operations in connectCenter."""


from __future__ import annotations

import logging
from typing import Any

from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.repositories.contracts.library import LibraryRepositoryContract
from app.repositories.models.app_user import AppUserRow
from app.security import AuthenticatedUser
from app.services import load_users_by_ids, to_user_summary
from app.services.models import WhoAndWhen
from app.services.models.library import (
    CreateLibraryServiceResult,
    DiscardLibraryCheckServiceResult,
    LibraryReleaseDependencyServiceResult,
    LibraryServiceResult,
    ManageLibraryReleaseDependenciesServiceResult,
    UpdateLibraryServiceResult,
)
from app.services.utils.date import DateRange
from app.services.utils.pagination import PaginationParams, PaginationResponse
from app.types.identifiers import LibraryId, ReleaseId
from app.types.unset import UNSET, UnsetType

logger = logging.getLogger("connectcenter.service.library")

DEFAULT_LIBRARY_DEPENDENCY_NAME = "CCTS Data Type Catalogue v3"
DEFAULT_LIBRARY_DEPENDENCY_RELEASE_NUM = "3.1"


class LibraryService:
    """Service class for library queries and commands."""

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
        """Get libraries with optional filtering and pagination."""
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
        items = [self._to_library_result(row, users_by_id=users_by_id, release_dependencies=[]) for row in rows]
        logger.info("list libraries → %d/%d", len(items), total)
        return PaginationResponse(items=items, total=total, limit=pagination.limit, offset=pagination.offset)

    async def get(self, library_id: LibraryId) -> LibraryServiceResult | None:
        """Get a library by ID."""
        row = await self._repo.get(library_id)
        if row is None:
            logger.info("get library id=%d → not found", int(library_id))
            return None
        users_by_id = await load_users_by_ids(self._account_service_repo, [row.created_by, row.last_updated_by])
        release_dependencies = await self._get_library_release_dependencies(library_id)
        result = self._to_library_result(row, users_by_id=users_by_id, release_dependencies=release_dependencies)
        logger.info("get library id=%d → found", int(library_id))
        return result

    async def create_library(
        self,
        *,
        type: str | None,
        name: str,
        organization: str | None,
        description: str | None,
        link: str | None,
        domain: str | None,
        namespace_uri: str,
        namespace_prefix: str | None,
    ) -> CreateLibraryServiceResult:
        """Create a library and seed its working release."""
        self._assert_admin("Only administrators can create the library.")
        normalized_name = self._normalize_required_name(name)
        normalized_namespace_uri = self._normalize_required_namespace_uri(namespace_uri)
        normalized_namespace_prefix = self._normalize_namespace_prefix(namespace_prefix)
        if await self._repo.has_duplicate_name(name=normalized_name):
            raise ValueError(f"The library name '{normalized_name}' already exists.")

        library_id = await self._repo.create_library(
            type=self._normalize_optional_text(type),
            name=normalized_name,
            organization=self._normalize_optional_text(organization),
            description=self._normalize_optional_text(description),
            link=self._normalize_optional_text(link),
            domain=self._normalize_optional_text(domain),
            state=None,
            requester_user_id=self._requester_user_id,
        )
        namespace_id = await self._repo.create_namespace(
            library_id=library_id,
            uri=normalized_namespace_uri,
            prefix=normalized_namespace_prefix,
            description=None,
            requester_user_id=self._requester_user_id,
            is_std_nmsp=True,
        )
        working_release_id = await self._repo.create_working_release(
            library_id=library_id,
            namespace_id=namespace_id,
            requester_user_id=self._requester_user_id,
        )
        await self._repo.create_xbt_manifest_records(release_id=working_release_id)
        ccts_release_id = await self._repo.get_release_id_by_library_name_and_release_num(
            library_name=DEFAULT_LIBRARY_DEPENDENCY_NAME,
            release_num=DEFAULT_LIBRARY_DEPENDENCY_RELEASE_NUM,
        )
        if ccts_release_id is not None:
            await self._repo.replace_release_dependencies(
                release_id=working_release_id,
                dependency_release_ids=[ccts_release_id],
            )
        else:
            logger.warning(
                "Skipping default dependency for new library %d because %s %s was not found.",
                int(library_id),
                DEFAULT_LIBRARY_DEPENDENCY_NAME,
                DEFAULT_LIBRARY_DEPENDENCY_RELEASE_NUM,
            )
        return CreateLibraryServiceResult(library_id=library_id)

    async def update_library(
        self,
        *,
        library_id: LibraryId,
        type: str | None | UnsetType = UNSET,
        name: str | UnsetType = UNSET,
        organization: str | None | UnsetType = UNSET,
        description: str | None | UnsetType = UNSET,
        link: str | None | UnsetType = UNSET,
        domain: str | None | UnsetType = UNSET,
        state: str | None | UnsetType = UNSET,
        is_default: bool | None | UnsetType = UNSET,
    ) -> UpdateLibraryServiceResult:
        """Update a library."""
        self._assert_admin("Only administrators can update the library.")
        if not await self._repo.exists(library_id):
            raise LookupError(f"Library with ID '{int(library_id)}' does not exist.")

        current = await self.get(library_id)
        if current is None:
            raise LookupError(f"Library with ID '{int(library_id)}' does not exist.")

        normalized_name = current.name if name is UNSET else self._normalize_required_name(name)
        if name is not UNSET and normalized_name != current.name:
            if await self._repo.has_duplicate_name(name=normalized_name, exclude_library_id=library_id):
                raise ValueError(f"The library name '{normalized_name}' already exists.")

        normalized_type = current.type if type is UNSET else self._normalize_optional_text(type)
        normalized_organization = (
            current.organization if organization is UNSET else self._normalize_optional_text(organization)
        )
        normalized_description = (
            current.description if description is UNSET else self._normalize_optional_text(description)
        )
        normalized_link = current.link if link is UNSET else self._normalize_optional_text(link)
        normalized_domain = current.domain if domain is UNSET else self._normalize_optional_text(domain)
        normalized_state = current.state if state is UNSET else self._normalize_optional_text(state)

        updates: list[str] = []
        if type is not UNSET and current.type != normalized_type:
            updates.append("type")
        if name is not UNSET and current.name != normalized_name:
            updates.append("name")
        if organization is not UNSET and current.organization != normalized_organization:
            updates.append("organization")
        if description is not UNSET and current.description != normalized_description:
            updates.append("description")
        if link is not UNSET and current.link != normalized_link:
            updates.append("link")
        if domain is not UNSET and current.domain != normalized_domain:
            updates.append("domain")
        if state is not UNSET and current.state != normalized_state:
            updates.append("state")
        if is_default is not UNSET and current.is_default != bool(is_default):
            updates.append("is_default")

        if not updates:
            return UpdateLibraryServiceResult(library_id=library_id, updates=[])

        await self._repo.update_library(
            library_id=library_id,
            type=normalized_type,
            name=normalized_name,
            organization=normalized_organization,
            description=normalized_description,
            link=normalized_link,
            domain=normalized_domain,
            state=normalized_state,
            is_default=None if is_default is UNSET else bool(is_default),
            requester_user_id=self._requester_user_id,
        )
        return UpdateLibraryServiceResult(library_id=library_id, updates=updates)

    async def check_discard_library(
        self,
        *,
        library_id: LibraryId,
    ) -> DiscardLibraryCheckServiceResult:
        """Return whether a library can be discarded."""
        block_message = await self._get_discard_block_message(library_id)
        return DiscardLibraryCheckServiceResult(
            discardable=block_message is None,
            message="" if block_message is None else block_message,
        )

    async def add_library_release_dependency(
        self,
        *,
        library_id: LibraryId,
        release_id: ReleaseId,
    ) -> ManageLibraryReleaseDependenciesServiceResult:
        """Add a direct dependency to the library's working release."""
        working_release = await self._get_required_working_release(library_id)
        current_dependency_ids = {
            int(existing_release_id)
            for existing_release_id in await self._repo.get_release_dependency_ids(
                release_id=ReleaseId(int(working_release.release_id))
            )
        }
        await self._validate_selected_release_dependencies(
            library_id=library_id,
            working_release_id=ReleaseId(int(working_release.release_id)),
            selected_release_ids=[release_id],
        )
        target_dependency_ids = sorted(current_dependency_ids | {int(release_id)})
        await self._repo.replace_release_dependencies(
            release_id=ReleaseId(int(working_release.release_id)),
            dependency_release_ids=[ReleaseId(value) for value in target_dependency_ids],
        )
        return ManageLibraryReleaseDependenciesServiceResult(
            library_id=library_id,
            release_dependencies=await self._get_library_release_dependencies(library_id),
        )

    async def remove_library_release_dependency(
        self,
        *,
        library_id: LibraryId,
        release_id: ReleaseId,
    ) -> ManageLibraryReleaseDependenciesServiceResult:
        """Remove a direct dependency from the library's working release."""
        working_release = await self._get_required_working_release(library_id)
        current_dependency_ids = {
            int(existing_release_id)
            for existing_release_id in await self._repo.get_release_dependency_ids(
                release_id=ReleaseId(int(working_release.release_id))
            )
        }
        if int(release_id) not in current_dependency_ids:
            selected_releases = await self._repo.get_releases_by_ids(release_ids=[release_id])
            if len(selected_releases) != 1:
                raise LookupError(f"Release with ID '{int(release_id)}' does not exist.")
            raise ValueError("The selected release dependency is not currently assigned to the library.")

        target_dependency_ids = sorted(current_dependency_ids - {int(release_id)})
        await self._repo.replace_release_dependencies(
            release_id=ReleaseId(int(working_release.release_id)),
            dependency_release_ids=[ReleaseId(value) for value in target_dependency_ids],
        )
        return ManageLibraryReleaseDependenciesServiceResult(
            library_id=library_id,
            release_dependencies=await self._get_library_release_dependencies(library_id),
        )

    async def discard_library(
        self,
        *,
        library_id: LibraryId,
    ) -> None:
        """Discard a library once all discard checks pass."""
        block_message = await self._get_discard_block_message(library_id)
        if block_message is not None:
            raise ValueError(block_message)

        working_release = await self._repo.get_working_release(library_id=library_id)
        if working_release is not None:
            await self._repo.discard_working_release(release_id=ReleaseId(int(working_release.release_id)))

        deleted = await self._repo.discard_library(library_id=library_id)
        if not deleted:
            raise LookupError(f"Library with ID '{int(library_id)}' does not exist.")

    def _to_library_result(
        self,
        row: Any,
        *,
        users_by_id: dict[int, AppUserRow],
        release_dependencies: list[LibraryReleaseDependencyServiceResult],
    ) -> LibraryServiceResult:
        """Map a repository row to a service DTO."""
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
            release_dependencies=release_dependencies,
            created=WhoAndWhen(who=created, when=row.creation_timestamp),
            last_updated=WhoAndWhen(who=updated, when=row.last_update_timestamp),
        )

    @property
    def _requester_user_id(self) -> int:
        return int(self._requester.user.user_id)

    def _assert_admin(self, message: str) -> None:
        if "Admin" not in self._requester.user.roles:
            raise PermissionError(message)

    async def _get_library_release_dependencies(
        self,
        library_id: LibraryId,
    ) -> list[LibraryReleaseDependencyServiceResult]:
        working_release = await self._repo.get_working_release(library_id=library_id)
        if working_release is None:
            return []

        dependency_release_ids = await self._repo.get_release_dependency_ids(
            release_id=ReleaseId(int(working_release.release_id))
        )
        if not dependency_release_ids:
            return []

        releases = await self._repo.get_releases_by_ids(release_ids=dependency_release_ids)
        return [
            LibraryReleaseDependencyServiceResult(
                release_id=ReleaseId(int(release.release_id)),
                library_id=LibraryId(int(release.library_id)),
                library_name=release.library_name,
                release_num=release.release_num,
                state=release.state,
            )
            for release in releases
        ]

    async def _get_required_working_release(self, library_id: LibraryId) -> Any:
        self._assert_admin("Only administrators can update library release dependencies.")
        if not await self._repo.exists(library_id):
            raise LookupError(f"Library with ID '{int(library_id)}' does not exist.")

        working_release = await self._repo.get_working_release(library_id=library_id)
        if working_release is None:
            raise ValueError("The library does not have an editable release.")
        return working_release

    async def _validate_selected_release_dependencies(
        self,
        *,
        library_id: LibraryId,
        working_release_id: ReleaseId,
        selected_release_ids: list[ReleaseId],
    ) -> None:
        unique_release_ids = self._dedupe_release_ids(selected_release_ids)
        selected_releases = await self._repo.get_releases_by_ids(release_ids=unique_release_ids)
        if len(selected_releases) != len(unique_release_ids):
            raise ValueError("One or more selected release dependencies do not exist.")
        if len({release.library_id for release in selected_releases}) != len(selected_releases):
            raise ValueError("Only one release dependency can be selected from each library.")

        for release in selected_releases:
            if int(release.library_id) == int(library_id):
                raise ValueError("A library cannot depend on one of its own releases.")
            if release.state != "Published":
                raise ValueError("Only published releases can be assigned as dependencies.")
            transitive_dependencies = await self._repo.get_transitive_dependency_ids(
                release_id=ReleaseId(int(release.release_id))
            )
            if int(working_release_id) in {int(dep_release_id) for dep_release_id in transitive_dependencies}:
                raise ValueError("The selected dependencies would create a circular release dependency.")

    @staticmethod
    def _normalize_required_name(name: str) -> str:
        normalized = name.strip()
        if not normalized:
            raise ValueError("`name` is required.")
        return normalized

    @staticmethod
    def _normalize_optional_text(value: str | None) -> str | None:
        if value is None:
            return None
        normalized = value.strip()
        return normalized or None

    @staticmethod
    def _normalize_required_namespace_uri(namespace_uri: str) -> str:
        normalized = namespace_uri.strip()
        if not normalized:
            raise ValueError("`namespace_uri` is required.")
        return normalized

    @staticmethod
    def _normalize_namespace_prefix(namespace_prefix: str | None) -> str:
        if namespace_prefix is None:
            return ""
        return namespace_prefix.strip()

    @staticmethod
    def _dedupe_release_ids(release_ids: list[int]) -> list[ReleaseId]:
        seen: set[int] = set()
        ordered: list[ReleaseId] = []
        for release_id in release_ids:
            release_int = int(release_id)
            if release_int in seen:
                continue
            seen.add(release_int)
            ordered.append(ReleaseId(release_int))
        return ordered

    async def _get_discard_block_message(self, library_id: LibraryId) -> str | None:
        if "Admin" not in self._requester.user.roles:
            return "Only administrators can discard the library."
        if not await self._repo.exists(library_id):
            return f"Library with ID '{int(library_id)}' does not exist."

        releases = await self._repo.get_library_releases(library_id=library_id)
        published_non_working_releases = [
            release for release in releases if not release.is_working_release and release.state == "Published"
        ]
        if published_non_working_releases:
            return (
                "This library cannot be discarded because it has published releases. "
                "Please contact an administrator."
            )

        non_working_releases = [release for release in releases if not release.is_working_release]
        if non_working_releases:
            return (
                "This library cannot be discarded because it has releases other than the current editable branch. "
                "Remove those releases first."
            )

        working_release = next((release for release in releases if release.is_working_release), None)
        if working_release is None:
            return None

        depending_releases = await self._repo.get_releases_depending_on(
            release_id=ReleaseId(int(working_release.release_id))
        )
        external_depending_releases = [
            release for release in depending_releases if int(release.library_id) != int(library_id)
        ]
        if external_depending_releases:
            return (
                "This library cannot be discarded because other releases depend on it. "
                "Unlink the release dependencies first."
            )

        return None
