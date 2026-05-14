"""MariaDB repository implementation for Namespaces."""


from __future__ import annotations

from datetime import datetime
from typing import Any, Literal

from sqlalchemy import delete, func, literal, or_, select, union_all, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.contracts.namespace import NamespaceRepositoryContract
from app.repositories.models import LibrarySummaryRow
from app.repositories.models.namespace import NamespaceRow
from app.repositories.vendors.mariadb.models.app_user import AppUser
from app.repositories.vendors.mariadb.models.agency_id_list import AgencyIdList
from app.repositories.vendors.mariadb.models.core_component import Acc, Asccp, Bccp
from app.repositories.vendors.mariadb.models.data_type import Dt
from app.repositories.vendors.mariadb.models.code_list import CodeList
from app.repositories.vendors.mariadb.models.library import Library
from app.repositories.vendors.mariadb.models.namespace import Namespace
from app.repositories.vendors.mariadb.models.release import Release
from app.types.identifiers import (
    AppUserId,
    LibraryId,
    NamespaceId,
)


class MariaDbNamespaceRepository(NamespaceRepositoryContract):
    """MariaDB-backed repository for Namespaces.

    Key features:
    - List namespaces with optional filters on library_id/uri/prefix/is_std_nmsp.
    - Date range filtering for creation and last update timestamps.
    - Pagination and multi-column sorting with safe column mapping.
    - DTO assembly with library, owner, creator, and last_updater summaries.
    """

    def __init__(self, session: AsyncSession):
        """Initialize MariaDbNamespaceRepository.

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
        uri: str | None = None,
        prefix: str | None = None,
        is_std_nmsp: bool | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
        included_owner_login_ids: list[str] | None = None,
        excluded_owner_login_ids: list[str] | None = None,
        included_updater_login_ids: list[str] | None = None,
        excluded_updater_login_ids: list[str] | None = None,
    ) -> tuple[int, list[NamespaceRow]]:
        """Handle list.

        Args:
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            library_id: Library identifier used to scope the query.
            uri: Optional namespace URI filter.
            prefix: Optional namespace prefix filter.
            is_std_nmsp: Optional standard-namespace flag filter.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.
            included_owner_login_ids: Optional owner login IDs to include by exact match.
            excluded_owner_login_ids: Optional owner login IDs to exclude by exact match.
            included_updater_login_ids: Optional updater login IDs to include by exact match.
            excluded_updater_login_ids: Optional updater login IDs to exclude by exact match.

        Returns:
            Result of the operation.
        """
        where_clauses = _build_where_clauses(
            library_id=library_id,
            uri=uri,
            prefix=prefix,
            is_std_nmsp=is_std_nmsp,
            creation_timestamp_before=creation_timestamp_before,
            creation_timestamp_after=creation_timestamp_after,
            last_update_timestamp_before=last_update_timestamp_before,
            last_update_timestamp_after=last_update_timestamp_after,
            included_owner_login_ids=included_owner_login_ids,
            excluded_owner_login_ids=excluded_owner_login_ids,
            included_updater_login_ids=included_updater_login_ids,
            excluded_updater_login_ids=excluded_updater_login_ids,
        )

        total_stmt = select(func.count()).select_from(Namespace)
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
        items = [_to_namespace_row(row) for row in res.all()]
        return total, items

    async def get(self, namespace_id: NamespaceId) -> NamespaceRow | None:
        """Handle get.

        Args:
            namespace_id: Namespace identifier.

        Returns:
            Result of the operation.
        """
        stmt = _select().where(Namespace.namespace_id == namespace_id)
        res = await self._session.execute(stmt)
        row = res.first()
        return _to_namespace_row(row) if row else None

    async def library_exists(self, library_id: LibraryId) -> bool:
        """Return whether the target library exists."""
        stmt = select(func.count()).select_from(Library).where(Library.library_id == int(library_id))
        res = await self._session.execute(stmt)
        return int(res.scalar_one()) > 0

    async def has_duplicate_uri(
        self,
        *,
        library_id: LibraryId,
        uri: str,
        exclude_namespace_id: NamespaceId | None = None,
    ) -> bool:
        """Return whether another namespace in the library already uses the URI."""
        stmt = (
            select(func.count())
            .select_from(Namespace)
            .where(
                Namespace.library_id == int(library_id),
                Namespace.uri == uri,
            )
        )
        if exclude_namespace_id is not None:
            stmt = stmt.where(Namespace.namespace_id != int(exclude_namespace_id))
        res = await self._session.execute(stmt)
        return int(res.scalar_one()) > 0

    async def has_duplicate_prefix(
        self,
        *,
        library_id: LibraryId,
        prefix: str,
        exclude_namespace_id: NamespaceId | None = None,
    ) -> bool:
        """Return whether another namespace in the library already uses the prefix."""
        normalized_prefix = prefix.strip()
        blank_prefix_filter = or_(Namespace.prefix.is_(None), Namespace.prefix == "")
        prefix_filter = blank_prefix_filter if normalized_prefix == "" else Namespace.prefix == normalized_prefix
        stmt = (
            select(func.count())
            .select_from(Namespace)
            .where(
                Namespace.library_id == int(library_id),
                prefix_filter,
            )
        )
        if exclude_namespace_id is not None:
            stmt = stmt.where(Namespace.namespace_id != int(exclude_namespace_id))
        res = await self._session.execute(stmt)
        return int(res.scalar_one()) > 0

    async def create_namespace(
        self,
        *,
        library_id: LibraryId,
        uri: str,
        prefix: str,
        description: str | None,
        requester_user_id: AppUserId,
        requester_is_developer: bool,
    ) -> NamespaceId:
        """Create a namespace and return its identifier."""
        now = datetime.utcnow()
        namespace = Namespace(
            library_id=int(library_id),
            uri=uri,
            prefix=prefix,
            description=description,
            is_std_nmsp=bool(requester_is_developer),
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

    async def update_namespace(
        self,
        *,
        namespace_id: NamespaceId,
        uri: str,
        prefix: str,
        description: str | None,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update a namespace owned by the requester."""
        stmt = (
            update(Namespace)
            .where(
                Namespace.namespace_id == int(namespace_id),
                Namespace.owner_user_id == int(requester_user_id),
            )
            .values(
                uri=uri,
                prefix=prefix,
                description=description,
                last_updated_by=int(requester_user_id),
                last_update_timestamp=datetime.utcnow(),
            )
        )
        res = await self._session.execute(stmt)
        return int(res.rowcount or 0) == 1

    async def discard_namespace(self, *, namespace_id: NamespaceId) -> bool:
        """Delete a namespace row by identifier."""
        stmt = delete(Namespace).where(Namespace.namespace_id == int(namespace_id))
        res = await self._session.execute(stmt)
        return int(res.rowcount or 0) == 1

    async def transfer_namespace_ownership(
        self,
        *,
        namespace_id: NamespaceId,
        requester_user_id: AppUserId,
        target_user_id: AppUserId,
    ) -> bool:
        """Transfer namespace ownership."""
        stmt = (
            update(Namespace)
            .where(Namespace.namespace_id == int(namespace_id))
            .values(
                owner_user_id=int(target_user_id),
                last_updated_by=int(requester_user_id),
                last_update_timestamp=datetime.utcnow(),
            )
        )
        res = await self._session.execute(stmt)
        return int(res.rowcount or 0) == 1

    async def namespace_is_used(self, *, namespace_id: NamespaceId) -> bool:
        """Return whether any release or component still references the namespace."""
        ns_id = int(namespace_id)
        usage_union = union_all(
            select(literal(1)).select_from(Release).where(Release.namespace_id == ns_id).limit(1),
            select(literal(1)).select_from(Acc).where(Acc.namespace_id == ns_id).limit(1),
            select(literal(1)).select_from(Asccp).where(Asccp.namespace_id == ns_id).limit(1),
            select(literal(1)).select_from(Bccp).where(Bccp.namespace_id == ns_id).limit(1),
            select(literal(1)).select_from(Dt).where(Dt.namespace_id == ns_id).limit(1),
            select(literal(1)).select_from(CodeList).where(CodeList.namespace_id == ns_id).limit(1),
            select(literal(1)).select_from(AgencyIdList).where(AgencyIdList.namespace_id == ns_id).limit(1),
        ).subquery()
        stmt = select(func.count()).select_from(usage_union)
        res = await self._session.execute(stmt)
        return int(res.scalar_one()) > 0


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
            Namespace.namespace_id,
            Namespace.library_id,
            Library.name,
            Namespace.uri,
            Namespace.prefix,
            Namespace.description,
            Namespace.is_std_nmsp,
            Namespace.creation_timestamp,
            Namespace.last_update_timestamp,
            Namespace.owner_user_id,
            Namespace.created_by,
            Namespace.last_updated_by,
        )
        .join(Library, Library.library_id == Namespace.library_id)
    )


def _to_namespace_row(row: Any) -> NamespaceRow:
    """Internal helper for row to row.

    Args:
        row: Repository row model to convert into a DTO.

    Returns:
        Result of the operation.
    """
    (
        namespace_id,
        library_id,
        library_name,
        uri,
        prefix,
        description,
        is_std_nmsp,
        creation_timestamp,
        last_update_timestamp,
        owner_user_id,
        created_by,
        last_updated_by,
    ) = row
    return NamespaceRow(
        namespace_id=namespace_id,
        library=LibrarySummaryRow(library_id=library_id, name=str(library_name)),
        uri=str(uri),
        prefix=str(prefix) if prefix is not None else None,
        description=str(description) if description is not None else None,
        is_std_nmsp=bool(is_std_nmsp),
        owner_user_id=owner_user_id,
        created_by=created_by,
        creation_timestamp=_as_dt(creation_timestamp),
        last_updated_by=last_updated_by,
        last_update_timestamp=_as_dt(last_update_timestamp),
    )


def _build_where_clauses(
    library_id: int | None,
    uri: str | None,
    prefix: str | None,
    is_std_nmsp: bool | None,
    creation_timestamp_before: datetime | None,
    creation_timestamp_after: datetime | None,
    last_update_timestamp_before: datetime | None,
    last_update_timestamp_after: datetime | None,
    included_owner_login_ids: list[str] | None = None,
    excluded_owner_login_ids: list[str] | None = None,
    included_updater_login_ids: list[str] | None = None,
    excluded_updater_login_ids: list[str] | None = None,
) -> list[object]:
    """Internal helper for build where clauses.

    Args:
        library_id: Library identifier used to scope the query.
        uri: Optional namespace URI filter.
        prefix: Optional namespace prefix filter.
        is_std_nmsp: Optional standard-namespace flag filter.
        creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
        last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

    Returns:
        Result of the operation.
    """
    clauses: list[object] = []
    if library_id is not None:
        clauses.append(Namespace.library_id == library_id)
    if uri:
        clauses.append(Namespace.uri.ilike(f"%{uri}%"))
    if prefix:
        clauses.append(Namespace.prefix.ilike(f"%{prefix}%"))
    if is_std_nmsp is not None:
        clauses.append(Namespace.is_std_nmsp == is_std_nmsp)
    if creation_timestamp_after is not None:
        clauses.append(Namespace.creation_timestamp >= creation_timestamp_after)
    if creation_timestamp_before is not None:
        clauses.append(Namespace.creation_timestamp <= creation_timestamp_before)
    if last_update_timestamp_after is not None:
        clauses.append(Namespace.last_update_timestamp >= last_update_timestamp_after)
    if last_update_timestamp_before is not None:
        clauses.append(Namespace.last_update_timestamp <= last_update_timestamp_before)
    if included_owner_login_ids:
        clauses.append(
            Namespace.owner_user_id.in_(
                select(AppUser.app_user_id).where(AppUser.login_id.in_(included_owner_login_ids))
            )
        )
    if excluded_owner_login_ids:
        clauses.append(
            Namespace.owner_user_id.not_in(
                select(AppUser.app_user_id).where(AppUser.login_id.in_(excluded_owner_login_ids))
            )
        )
    if included_updater_login_ids:
        clauses.append(
            Namespace.last_updated_by.in_(
                select(AppUser.app_user_id).where(AppUser.login_id.in_(included_updater_login_ids))
            )
        )
    if excluded_updater_login_ids:
        clauses.append(
            Namespace.last_updated_by.not_in(
                select(AppUser.app_user_id).where(AppUser.login_id.in_(excluded_updater_login_ids))
            )
        )
    return clauses


def _build_order_by(sorts: list[tuple[str, Literal["ASC", "DESC"]]] | None):
    """Internal helper for build order by.

    Args:
        sorts: Value for `sorts`.

    Returns:
        Result of the operation.
    """
    if not sorts:
        return [Namespace.creation_timestamp.desc()]

    mapping = {
        "uri": Namespace.uri,
        "prefix": Namespace.prefix,
        "is_std_nmsp": Namespace.is_std_nmsp,
        "creation_timestamp": Namespace.creation_timestamp,
        "last_update_timestamp": Namespace.last_update_timestamp,
    }
    order_by = []
    for s in sorts:
        col = mapping.get(s[0])
        if col is None:
            continue
        order_by.append(col.desc() if s[1] == "DESC" else col.asc())
    return order_by or [Namespace.creation_timestamp.desc()]
