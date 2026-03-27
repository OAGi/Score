"""MariaDB repository implementation for Namespaces.

Implements list/get operations with filtering, pagination, and sorting.
Joins library and user tables to include related summaries in response DTOs.
"""


from __future__ import annotations

from datetime import datetime
from typing import Any, Literal

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.contracts.namespace import NamespaceRepositoryContract
from app.repositories.models import LibrarySummaryRow
from app.repositories.models.namespace import NamespaceRow
from app.repositories.vendors.mariadb.models.library import Library
from app.repositories.vendors.mariadb.models.namespace import Namespace
from app.types.identifiers import (
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
