"""MariaDB repository implementation for Releases.

Implements list/get operations with filtering, pagination, and sorting, and
supports recursive dependency resolution via the ReleaseDep table.
"""


from __future__ import annotations

from datetime import datetime
from typing import Any, Literal

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.contracts.release import ReleaseRepositoryContract
from app.repositories.models import LibrarySummaryRow, NamespaceSummaryRow
from app.repositories.models.release import ReleaseRow
from app.repositories.vendors.mariadb.models.library import Library
from app.repositories.vendors.mariadb.models.namespace import Namespace
from app.repositories.vendors.mariadb.models.release import Release, ReleaseDep
from app.types.identifiers import (
    LibraryId,
    ReleaseId,
)


class MariaDbReleaseRepository(ReleaseRepositoryContract):
    """MariaDB-backed repository for Releases.

    Key features:
    - List releases with optional filters on library_id/release_num/state.
    - Date range filtering for creation and last update timestamps.
    - Pagination and multi-column sorting with safe column mapping.
    - Excludes releases with release_num="Working" at the query layer.
    - DTO assembly with library/namespace and creator/last_updater summaries.
    - Dependency traversal for release dependency chains.
    """

    def __init__(self, session: AsyncSession):
        """Initialize MariaDbReleaseRepository.

        Args:
            session: Database session bound to the current request.
        """
        self._session = session

    async def list(
        self,
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        library_id: LibraryId | None,
        release_num: str | None = None,
        state: str | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
    ) -> tuple[int, list[ReleaseRow]]:
        """Handle list.

        Args:
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            library_id: Library identifier used to scope the query.
            release_num: Optional release number filter.
            state: Optional lifecycle state filter.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

        Returns:
            Result of the operation.
        """
        where_clauses = _build_where_clauses(
            library_id=library_id,
            release_num=release_num,
            state=state,
            creation_timestamp_before=creation_timestamp_before,
            creation_timestamp_after=creation_timestamp_after,
            last_update_timestamp_before=last_update_timestamp_before,
            last_update_timestamp_after=last_update_timestamp_after,
        )

        total_stmt = select(func.count()).select_from(Release)
        if where_clauses:
            total_stmt = total_stmt.where(*where_clauses)
        total_res = await self._session.execute(total_stmt)
        total = int(total_res.scalar_one())

        stmt = _select()
        if where_clauses:
            stmt = stmt.where(*where_clauses)
        limit = limit
        offset = offset
        stmt = stmt.order_by(*_build_order_by(sorts)).limit(limit).offset(offset)
        res = await self._session.execute(stmt)
        items = [_to_release_row(row) for row in res.all()]
        return total, items

    async def get(self, release_id: ReleaseId) -> ReleaseRow | None:
        """Handle get.

        Args:
            release_id: Release identifier used to scope the query.

        Returns:
            Result of the operation.
        """
        stmt = _select().where(Release.release_id == release_id)
        res = await self._session.execute(stmt)
        row = res.first()
        return _to_release_row(row) if row else None

    async def get_by_library_id_and_release_num(
        self,
        library_id: LibraryId,
        release_num: str,
    ) -> ReleaseRow | None:
        """Return a release by exact library and release number, including `Working`."""
        stmt = _select().where(
            Release.library_id == int(library_id),
            Release.release_num == release_num,
        )
        res = await self._session.execute(stmt)
        row = res.first()
        return _to_release_row(row) if row else None

    async def get_dependent_releases(self, release_id: ReleaseId) -> list[ReleaseId]:
        """Handle get dependent releases.

        Args:
            release_id: Release identifier used to scope the query.

        Returns:
            Result of the operation.
        """
        all_dependent_releases: set[int] = set()
        processed_releases: set[int] = set()
        releases_to_process: list[int] = [release_id]

        while releases_to_process:
            current_release_id = releases_to_process.pop(0)
            if current_release_id in processed_releases:
                continue
            processed_releases.add(current_release_id)

            res = await self._session.execute(
                select(ReleaseDep.depend_on_release_id).where(ReleaseDep.release_id == current_release_id)
            )
            direct_dependencies = [x for x in res.scalars().all()]
            for dep_release_id in direct_dependencies:
                if dep_release_id not in all_dependent_releases and dep_release_id not in processed_releases:
                    all_dependent_releases.add(dep_release_id)
                    releases_to_process.append(dep_release_id)

        return [x for x in all_dependent_releases]


def _as_dt(value: object) -> datetime:
    """Internal helper for as dt.

    Args:
        value: Context value string.

    Returns:
        Result of the operation.
    """
    if isinstance(value, datetime):
        return value
    raise TypeError(f"Expected datetime, got {type(value)}")


def _select():
    """Internal helper for select."""
    return (
        select(
            Release.release_id,
            Release.library_id,
            Library.name,
            Release.guid,
            Release.release_num,
            Release.release_note,
            Release.release_license,
            Release.namespace_id,
            Namespace.prefix,
            Namespace.uri,
            Release.state,
            Release.creation_timestamp,
            Release.last_update_timestamp,
            Release.created_by,
            Release.last_updated_by,
        )
        .join(Library, Library.library_id == Release.library_id)
        .outerjoin(Namespace, Namespace.namespace_id == Release.namespace_id)
    )


def _to_release_row(row: Any) -> ReleaseRow:
    """Internal helper for row to row.

    Args:
        row: Repository row model to convert into a DTO.

    Returns:
        Result of the operation.
    """
    (
        release_id,
        library_id,
        library_name,
        guid,
        release_num,
        release_note,
        release_license,
        namespace_id,
        namespace_prefix,
        namespace_uri,
        state,
        creation_timestamp,
        last_update_timestamp,
        created_by,
        last_updated_by,
    ) = row

    namespace_summary = None
    if namespace_id is not None:
        namespace_summary = NamespaceSummaryRow(
            namespace_id=namespace_id,
            prefix=str(namespace_prefix) if namespace_prefix is not None else None,
            uri=str(namespace_uri),
        )

    return ReleaseRow(
        release_id=release_id,
        library=LibrarySummaryRow(library_id=library_id, name=str(library_name)),
        guid=str(guid),
        release_num=str(release_num) if release_num is not None else None,
        release_note=str(release_note) if release_note is not None else None,
        release_license=str(release_license) if release_license is not None else None,
        namespace=namespace_summary,
        state=str(state),
        created_by=created_by,
        creation_timestamp=_as_dt(creation_timestamp),
        last_updated_by=last_updated_by,
        last_update_timestamp=_as_dt(last_update_timestamp),
    )


def _build_where_clauses(
    library_id: int | None,
    release_num: str | None,
    state: str | None,
    creation_timestamp_before: datetime | None,
    creation_timestamp_after: datetime | None,
    last_update_timestamp_before: datetime | None,
    last_update_timestamp_after: datetime | None,
) -> list[object]:
    """Internal helper for build where clauses.

    Args:
        library_id: Library identifier used to scope the query.
        release_num: Optional release number filter.
        state: Optional lifecycle state filter.
        creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
        last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

    Returns:
        Result of the operation.
    """
    clauses: list[object] = []
    if library_id is not None:
        clauses.append(Release.library_id == library_id)
    if release_num:
        clauses.append(Release.release_num.ilike(f"%{release_num}%"))
    if state:
        clauses.append(Release.state == state)
    clauses.append(Release.release_num != "Working")
    if creation_timestamp_after is not None:
        clauses.append(Release.creation_timestamp >= creation_timestamp_after)
    if creation_timestamp_before is not None:
        clauses.append(Release.creation_timestamp <= creation_timestamp_before)
    if last_update_timestamp_after is not None:
        clauses.append(Release.last_update_timestamp >= last_update_timestamp_after)
    if last_update_timestamp_before is not None:
        clauses.append(Release.last_update_timestamp <= last_update_timestamp_before)
    return clauses


def _build_order_by(sorts: list[tuple[str, Literal["ASC", "DESC"]]] | None):
    """Internal helper for build order by.

    Args:
        sorts: Value for `sorts`.

    Returns:
        Result of the operation.
    """
    if not sorts:
        return [Release.creation_timestamp.desc()]

    mapping = {
        "release_num": Release.release_num,
        "state": Release.state,
        "creation_timestamp": Release.creation_timestamp,
        "last_update_timestamp": Release.last_update_timestamp,
    }
    order_by = []
    for s in sorts:
        col = mapping.get(s[0])
        if col is None:
            continue
        order_by.append(col.desc() if s[1] == "DESC" else col.asc())
    return order_by or [Release.creation_timestamp.desc()]
