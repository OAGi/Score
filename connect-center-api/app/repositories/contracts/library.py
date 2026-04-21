"""Repository contract for Library persistence.

Defines the async interface for listing and retrieving libraries across
vendor implementations (SQLite, MariaDB).
"""


from __future__ import annotations

from datetime import datetime
from typing import Protocol, Literal

from app.repositories.models.library import LibraryReleaseRow, LibraryRow
from app.types.identifiers import AppUserId, LibraryId, NamespaceId, ReleaseId


class LibraryRepositoryContract(Protocol):
    """Protocol for library repository implementations."""
    async def list(
        self,
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        name: str | None = None,
        type: str | None = None,
        organization: str | None = None,
        domain: str | None = None,
        state: str | None = None,
        description: str | None = None,
        is_default: bool | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
    ) -> tuple[int, list[LibraryRow]]:
        """Repository contract for list.

        Args:
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            name: Optional name filter.
            type: Optional type filter.
            organization: Optional organization filter.
            domain: Value for `domain`.
            state: Optional lifecycle state filter.
            description: Optional textual description filter or payload field.
            is_default: Optional default-library flag filter.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

        Returns:
            Result of the operation.
        """
        pass

    async def get(self, library_id: LibraryId) -> LibraryRow | None:
        """Repository contract for get.

        Args:
            library_id: Library identifier used to scope the query.

        Returns:
            Result of the operation.
        """
        pass

    async def exists(self, library_id: LibraryId) -> bool:
        """Return whether the target library exists."""
        pass

    async def has_duplicate_name(
        self,
        *,
        name: str,
        exclude_library_id: LibraryId | None = None,
    ) -> bool:
        """Return whether another library already uses the same name."""
        pass

    async def create_library(
        self,
        *,
        type: str | None,
        name: str,
        organization: str | None,
        description: str | None,
        link: str | None,
        domain: str | None,
        state: str | None,
        requester_user_id: AppUserId,
    ) -> LibraryId:
        """Create a library and return its identifier."""
        pass

    async def update_library(
        self,
        *,
        library_id: LibraryId,
        type: str | None,
        name: str,
        organization: str | None,
        description: str | None,
        link: str | None,
        domain: str | None,
        state: str | None,
        is_default: bool | None,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update a library."""
        pass

    async def create_working_release(
        self,
        *,
        library_id: LibraryId,
        namespace_id: NamespaceId,
        requester_user_id: AppUserId,
    ) -> ReleaseId:
        """Create the initial `Working` release for a library."""
        pass

    async def create_namespace(
        self,
        *,
        library_id: LibraryId,
        uri: str,
        prefix: str,
        description: str | None,
        requester_user_id: AppUserId,
        is_std_nmsp: bool,
    ) -> NamespaceId:
        """Create a namespace for the library and return its identifier."""
        pass

    async def create_xbt_manifest_records(self, *, release_id: ReleaseId) -> None:
        """Seed XBT manifest rows for a newly created working release."""
        pass

    async def get_release_id_by_library_name_and_release_num(
        self,
        *,
        library_name: str,
        release_num: str,
    ) -> ReleaseId | None:
        """Return a release identifier by exact library name and release number."""
        pass

    async def get_working_release(self, *, library_id: LibraryId) -> LibraryReleaseRow | None:
        """Return the library's working release when present."""
        pass

    async def get_library_releases(self, *, library_id: LibraryId) -> list[LibraryReleaseRow]:
        """Return all releases belonging to a library."""
        pass

    async def get_releases_by_ids(self, *, release_ids: list[ReleaseId]) -> list[LibraryReleaseRow]:
        """Return release summaries for the supplied identifiers."""
        pass

    async def get_release_dependency_ids(self, *, release_id: ReleaseId) -> list[ReleaseId]:
        """Return direct dependency release identifiers for a release."""
        pass

    async def get_transitive_dependency_ids(self, *, release_id: ReleaseId) -> list[ReleaseId]:
        """Return transitive dependency release identifiers for a release."""
        pass

    async def get_releases_depending_on(self, *, release_id: ReleaseId) -> list[LibraryReleaseRow]:
        """Return releases that directly depend on the supplied release."""
        pass

    async def replace_release_dependencies(
        self,
        *,
        release_id: ReleaseId,
        dependency_release_ids: list[ReleaseId],
    ) -> None:
        """Replace all direct dependencies for a release."""
        pass

    async def discard_working_release(self, *, release_id: ReleaseId) -> None:
        """Delete a working release and its seed dependency rows."""
        pass

    async def discard_library(self, *, library_id: LibraryId) -> bool:
        """Delete a library row."""
        pass
