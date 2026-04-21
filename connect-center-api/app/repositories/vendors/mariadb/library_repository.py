"""MariaDB repository implementation for Libraries."""


from __future__ import annotations

from datetime import datetime
from uuid import uuid4
from typing import Any, Literal

from sqlalchemy import delete, func, insert, literal, select, update
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.sql import Select

from app.repositories.contracts.library import LibraryRepositoryContract
from app.repositories.models.library import LibraryReleaseRow, LibraryRow
from app.repositories.vendors.mariadb.models.library import Library
from app.repositories.vendors.mariadb.models.namespace import Namespace
from app.repositories.vendors.mariadb.models.release import Release, ReleaseDep
from app.repositories.vendors.mariadb.models.xbt import Xbt, XbtManifest
from app.types.identifiers import AppUserId, LibraryId, NamespaceId, ReleaseId


class MariaDbLibraryRepository(LibraryRepositoryContract):
    """MariaDB-backed repository for library queries and commands."""

    def __init__(self, session: AsyncSession):
        self._session = session

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
        """Handle list."""
        where_clauses = _build_where_clauses(
            name=name,
            type=type,
            organization=organization,
            domain=domain,
            state=state,
            description=description,
            is_default=is_default,
            creation_timestamp_before=creation_timestamp_before,
            creation_timestamp_after=creation_timestamp_after,
            last_update_timestamp_before=last_update_timestamp_before,
            last_update_timestamp_after=last_update_timestamp_after,
        )

        total_stmt = select(func.count()).select_from(Library)
        if where_clauses:
            total_stmt = total_stmt.where(*where_clauses)
        total_res = await self._session.execute(total_stmt)
        total = int(total_res.scalar_one())

        stmt = _select_library()
        if where_clauses:
            stmt = stmt.where(*where_clauses)
        stmt = stmt.order_by(*_build_order_by(sorts)).limit(limit).offset(offset)
        res = await self._session.execute(stmt)
        items = [_to_library_row(row) for row in res.all()]
        return total, items

    async def get(self, library_id: LibraryId) -> LibraryRow | None:
        """Handle get."""
        stmt = _select_library().where(Library.library_id == int(library_id))
        res = await self._session.execute(stmt)
        row = res.first()
        return _to_library_row(row) if row else None

    async def exists(self, library_id: LibraryId) -> bool:
        """Return whether the target library exists."""
        stmt = select(func.count()).select_from(Library).where(Library.library_id == int(library_id))
        res = await self._session.execute(stmt)
        return int(res.scalar_one()) > 0

    async def has_duplicate_name(
        self,
        *,
        name: str,
        exclude_library_id: LibraryId | None = None,
    ) -> bool:
        """Return whether another library already uses the same name."""
        stmt = select(func.count()).select_from(Library).where(Library.name == name)
        if exclude_library_id is not None:
            stmt = stmt.where(Library.library_id != int(exclude_library_id))
        res = await self._session.execute(stmt)
        return int(res.scalar_one()) > 0

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
        now = datetime.utcnow()
        library = Library(
            type=type,
            name=name,
            organization=organization,
            description=description,
            link=link,
            domain=domain,
            state=state,
            created_by=int(requester_user_id),
            last_updated_by=int(requester_user_id),
            creation_timestamp=now,
            last_update_timestamp=now,
        )
        self._session.add(library)
        await self._session.flush()
        await self._session.refresh(library)
        return LibraryId(int(library.library_id))

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
        now = datetime.utcnow()
        if is_default:
            await self._session.execute(
                update(Library)
                .where(Library.library_id != int(library_id))
                .values(is_default=False)
            )

        values: dict[str, object] = {
            "type": type,
            "name": name,
            "organization": organization,
            "description": description,
            "link": link,
            "domain": domain,
            "state": state,
            "last_updated_by": int(requester_user_id),
            "last_update_timestamp": now,
        }
        if is_default is not None:
            values["is_default"] = is_default

        res = await self._session.execute(
            update(Library)
            .where(Library.library_id == int(library_id))
            .values(**values)
        )
        return int(res.rowcount or 0) == 1

    async def create_working_release(
        self,
        *,
        library_id: LibraryId,
        namespace_id: NamespaceId,
        requester_user_id: AppUserId,
    ) -> ReleaseId:
        """Create the initial `Working` release for a library."""
        now = datetime.utcnow()
        release = Release(
            library_id=int(library_id),
            guid=uuid4().hex,
            release_num="Working",
            namespace_id=int(namespace_id),
            state="Published",
            created_by=int(requester_user_id),
            last_updated_by=int(requester_user_id),
            creation_timestamp=now,
            last_update_timestamp=now,
        )
        self._session.add(release)
        await self._session.flush()
        await self._session.refresh(release)
        return ReleaseId(int(release.release_id))

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
        """Create a namespace and return its identifier."""
        now = datetime.utcnow()
        namespace = Namespace(
            library_id=int(library_id),
            uri=uri,
            prefix=prefix,
            description=description,
            is_std_nmsp=is_std_nmsp,
            owner_user_id=int(requester_user_id),
            created_by=int(requester_user_id),
            last_updated_by=int(requester_user_id),
            creation_timestamp=now,
            last_update_timestamp=now,
        )
        self._session.add(namespace)
        await self._session.flush()
        await self._session.refresh(namespace)
        return NamespaceId(int(namespace.namespace_id))

    async def create_xbt_manifest_records(self, *, release_id: ReleaseId) -> None:
        """Seed XBT manifest rows for a newly created working release."""
        log_id_subquery = (
            select(func.min(XbtManifest.log_id))
            .where(XbtManifest.xbt_id == Xbt.xbt_id)
            .scalar_subquery()
        )
        stmt = insert(XbtManifest).from_select(
            ["release_id", "xbt_id", "log_id"],
            select(
                literal(int(release_id)),
                Xbt.xbt_id,
                log_id_subquery,
            ).where(Xbt.builtIn_type.like("xsd:%")),
        )
        await self._session.execute(stmt)

    async def get_release_id_by_library_name_and_release_num(
        self,
        *,
        library_name: str,
        release_num: str,
    ) -> ReleaseId | None:
        """Return a release identifier by exact library name and release number."""
        stmt = (
            select(Release.release_id)
            .join(Library, Library.library_id == Release.library_id)
            .where(
                Library.name == library_name,
                Release.release_num == release_num,
            )
            .limit(1)
        )
        res = await self._session.execute(stmt)
        value = res.scalar_one_or_none()
        return ReleaseId(int(value)) if value is not None else None

    async def get_working_release(self, *, library_id: LibraryId) -> LibraryReleaseRow | None:
        """Return the library's working release when present."""
        stmt = _select_release_summary().where(
            Release.library_id == int(library_id),
            Release.release_num == "Working",
        )
        res = await self._session.execute(stmt)
        row = res.first()
        return _to_library_release_row(row) if row else None

    async def get_library_releases(self, *, library_id: LibraryId) -> list[LibraryReleaseRow]:
        """Return all releases belonging to a library."""
        stmt = (
            _select_release_summary()
            .where(Release.library_id == int(library_id))
            .order_by(Release.creation_timestamp.asc(), Release.release_id.asc())
        )
        res = await self._session.execute(stmt)
        return [_to_library_release_row(row) for row in res.all()]

    async def get_releases_by_ids(self, *, release_ids: list[ReleaseId]) -> list[LibraryReleaseRow]:
        """Return release summaries for the supplied identifiers."""
        if not release_ids:
            return []
        stmt = (
            _select_release_summary()
            .where(Release.release_id.in_([int(release_id) for release_id in release_ids]))
            .order_by(Release.release_id.asc())
        )
        res = await self._session.execute(stmt)
        return [_to_library_release_row(row) for row in res.all()]

    async def get_release_dependency_ids(self, *, release_id: ReleaseId) -> list[ReleaseId]:
        """Return direct dependency release identifiers for a release."""
        stmt = (
            select(ReleaseDep.depend_on_release_id)
            .where(ReleaseDep.release_id == int(release_id))
            .order_by(ReleaseDep.release_dep_id.asc())
        )
        res = await self._session.execute(stmt)
        return [ReleaseId(int(value)) for value in res.scalars().all()]

    async def get_transitive_dependency_ids(self, *, release_id: ReleaseId) -> list[ReleaseId]:
        """Return transitive dependency release identifiers for a release."""
        discovered: set[int] = set()
        processed: set[int] = set()
        queue: list[int] = [int(release_id)]

        while queue:
            current_release_id = queue.pop(0)
            if current_release_id in processed:
                continue
            processed.add(current_release_id)

            res = await self._session.execute(
                select(ReleaseDep.depend_on_release_id).where(ReleaseDep.release_id == current_release_id)
            )
            for dependency_release_id in res.scalars().all():
                dependency_int = int(dependency_release_id)
                if dependency_int not in discovered and dependency_int not in processed:
                    discovered.add(dependency_int)
                    queue.append(dependency_int)

        return [ReleaseId(release_id) for release_id in discovered]

    async def get_releases_depending_on(self, *, release_id: ReleaseId) -> list[LibraryReleaseRow]:
        """Return releases that directly depend on the supplied release."""
        stmt = (
            _select_release_summary()
            .join(ReleaseDep, ReleaseDep.release_id == Release.release_id)
            .where(ReleaseDep.depend_on_release_id == int(release_id))
            .order_by(Library.name.asc(), Release.release_num.asc(), Release.release_id.asc())
        )
        res = await self._session.execute(stmt)
        return [_to_library_release_row(row) for row in res.all()]

    async def replace_release_dependencies(
        self,
        *,
        release_id: ReleaseId,
        dependency_release_ids: list[ReleaseId],
    ) -> None:
        """Replace direct dependencies for a release using a minimal diff."""
        current_dependency_ids = {
            int(existing_release_id)
            for existing_release_id in await self.get_release_dependency_ids(release_id=release_id)
        }
        target_dependency_ids = {int(dependency_release_id) for dependency_release_id in dependency_release_ids}

        dependency_ids_to_delete = current_dependency_ids - target_dependency_ids
        if dependency_ids_to_delete:
            await self._session.execute(
                delete(ReleaseDep).where(
                    ReleaseDep.release_id == int(release_id),
                    ReleaseDep.depend_on_release_id.in_(dependency_ids_to_delete),
                )
            )

        dependency_ids_to_create = target_dependency_ids - current_dependency_ids
        for dependency_release_id in sorted(dependency_ids_to_create):
            await self._session.execute(
                insert(ReleaseDep).values(
                    release_id=int(release_id),
                    depend_on_release_id=dependency_release_id,
                )
            )

    async def discard_working_release(self, *, release_id: ReleaseId) -> None:
        """Delete a working release and its seed dependency rows."""
        await self._session.execute(delete(XbtManifest).where(XbtManifest.release_id == int(release_id)))
        await self._session.execute(delete(ReleaseDep).where(ReleaseDep.release_id == int(release_id)))
        await self._session.execute(delete(Release).where(Release.release_id == int(release_id)))

    async def discard_library(self, *, library_id: LibraryId) -> bool:
        """Delete a library row."""
        res = await self._session.execute(delete(Library).where(Library.library_id == int(library_id)))
        return int(res.rowcount or 0) == 1


def _as_dt(value: object) -> datetime:
    """Return a datetime or raise."""
    if isinstance(value, datetime):
        return value
    raise TypeError(f"Expected datetime, got {type(value)}")


def _select_library() -> Select[Any]:
    """Base select for library rows."""
    return select(
        Library.library_id,
        Library.name,
        Library.type,
        Library.organization,
        Library.description,
        Library.link,
        Library.domain,
        Library.state,
        Library.is_read_only,
        Library.is_default,
        Library.creation_timestamp,
        Library.last_update_timestamp,
        Library.created_by,
        Library.last_updated_by,
    )


def _select_release_summary() -> Select[Any]:
    """Base select for release summaries used by library commands."""
    return (
        select(
            Release.release_id,
            Release.library_id,
            Library.name,
            Release.release_num,
            Release.state,
        )
        .join(Library, Library.library_id == Release.library_id)
    )


def _to_library_row(row: Any) -> LibraryRow:
    """Convert a library result row into a repository DTO."""
    (
        library_id,
        name,
        type,
        organization,
        description,
        link,
        domain,
        state,
        is_read_only,
        is_default,
        creation_timestamp,
        last_update_timestamp,
        created_by,
        last_updated_by,
    ) = row

    return LibraryRow(
        library_id=int(library_id),
        name=str(name),
        type=str(type) if type is not None else None,
        organization=str(organization) if organization is not None else None,
        description=str(description) if description is not None else None,
        link=str(link) if link is not None else None,
        domain=str(domain) if domain is not None else None,
        state=str(state) if state is not None else None,
        is_read_only=bool(is_read_only),
        is_default=bool(is_default),
        created_by=int(created_by),
        creation_timestamp=_as_dt(creation_timestamp),
        last_updated_by=int(last_updated_by),
        last_update_timestamp=_as_dt(last_update_timestamp),
    )


def _to_library_release_row(row: Any) -> LibraryReleaseRow:
    """Convert a release summary row into a repository DTO."""
    release_id, library_id, library_name, release_num, state = row
    release_num_str = str(release_num or "")
    return LibraryReleaseRow(
        release_id=int(release_id),
        library_id=int(library_id),
        library_name=str(library_name),
        release_num=release_num_str,
        state=str(state),
        is_working_release=release_num_str == "Working",
    )


def _build_where_clauses(
    name: str | None,
    type: str | None,
    organization: str | None,
    domain: str | None,
    state: str | None,
    description: str | None,
    is_default: bool | None,
    creation_timestamp_before: datetime | None,
    creation_timestamp_after: datetime | None,
    last_update_timestamp_before: datetime | None,
    last_update_timestamp_after: datetime | None,
) -> list[object]:
    """Build shared library filters."""
    clauses: list[object] = []
    if name:
        clauses.append(Library.name.ilike(f"%{name}%"))
    if type:
        clauses.append(Library.type.ilike(f"%{type}%"))
    if organization:
        clauses.append(Library.organization.ilike(f"%{organization}%"))
    if domain:
        clauses.append(Library.domain.ilike(f"%{domain}%"))
    if state:
        clauses.append(Library.state.ilike(f"%{state}%"))
    if description:
        clauses.append(Library.description.ilike(f"%{description}%"))
    if is_default is not None:
        clauses.append(Library.is_default == is_default)
    if creation_timestamp_after is not None:
        clauses.append(Library.creation_timestamp >= creation_timestamp_after)
    if creation_timestamp_before is not None:
        clauses.append(Library.creation_timestamp <= creation_timestamp_before)
    if last_update_timestamp_after is not None:
        clauses.append(Library.last_update_timestamp >= last_update_timestamp_after)
    if last_update_timestamp_before is not None:
        clauses.append(Library.last_update_timestamp <= last_update_timestamp_before)
    return clauses


def _build_order_by(sorts: list[tuple[str, Literal["ASC", "DESC"]]] | None):
    """Build order-by expressions for library listing."""
    if not sorts:
        return [Library.creation_timestamp.desc()]

    mapping = {
        "name": Library.name,
        "type": Library.type,
        "organization": Library.organization,
        "domain": Library.domain,
        "state": Library.state,
        "description": Library.description,
        "is_default": Library.is_default,
        "creation_timestamp": Library.creation_timestamp,
        "last_update_timestamp": Library.last_update_timestamp,
    }
    order_by = []
    for column, direction in sorts:
        mapped = mapping.get(column)
        if mapped is None:
            continue
        order_by.append(mapped.desc() if direction == "DESC" else mapped.asc())
    return order_by or [Library.creation_timestamp.desc()]
