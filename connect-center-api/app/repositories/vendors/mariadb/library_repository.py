"""MariaDB repository implementation for Libraries.

Implements list/get operations with filtering, pagination, and sorting.
Joins user tables to include creator/last_updater summaries in response DTOs.
"""


from __future__ import annotations

from datetime import datetime
from typing import Any, Literal

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.contracts.library import LibraryRepositoryContract
from app.repositories.models.library import LibraryRow
from app.repositories.vendors.mariadb.models.library import Library
from app.types.identifiers import (
    LibraryId,
)


class MariaDbLibraryRepository(LibraryRepositoryContract):
    """MariaDB-backed repository for Libraries.

    Key features:
    - List libraries with optional filters on name/type/organization/domain/state/description/is_default.
    - Date range filtering for creation and last update timestamps.
    - Pagination and multi-column sorting with safe column mapping.
    - DTO assembly with creator and last_updater summaries.
    """

    def __init__(self, session: AsyncSession):
        """Initialize MariaDbLibraryRepository.

        Args:
            session: Database session bound to the current request.
        """
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
        """Handle list.

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

        stmt = _select()
        if where_clauses:
            stmt = stmt.where(*where_clauses)
        limit = limit
        offset = offset
        stmt = stmt.order_by(*_build_order_by(sorts)).limit(limit).offset(offset)
        res = await self._session.execute(stmt)
        items = [_to_library_row(row) for row in res.all()]
        return total, items

    async def get(self, library_id: LibraryId) -> LibraryRow | None:
        """Handle get.

        Args:
            library_id: Library identifier used to scope the query.

        Returns:
            Result of the operation.
        """
        stmt = _select().where(Library.library_id == library_id)
        res = await self._session.execute(stmt)
        row = res.first()
        return _to_library_row(row) if row else None


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


def _to_library_row(row: Any) -> LibraryRow:
    """Internal helper for row to row.

    Args:
        row: Repository row model to convert into a DTO.

    Returns:
        Result of the operation.
    """
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
        library_id=library_id,
        name=str(name),
        type=str(type) if type is not None else None,
        organization=str(organization) if organization is not None else None,
        description=str(description) if description is not None else None,
        link=str(link) if link is not None else None,
        domain=str(domain) if domain is not None else None,
        state=str(state) if state is not None else None,
        is_read_only=bool(is_read_only),
        is_default=bool(is_default),
        created_by=created_by,
        creation_timestamp=_as_dt(creation_timestamp),
        last_updated_by=last_updated_by,
        last_update_timestamp=_as_dt(last_update_timestamp),
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
    """Internal helper for build where clauses.

    Args:
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
    """Internal helper for build order by.

    Args:
        sorts: Value for `sorts`.

    Returns:
        Result of the operation.
    """
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
    for s in sorts:
        col = mapping.get(s[0])
        if col is None:
            continue
        order_by.append(col.desc() if s[1] == "DESC" else col.asc())
    return order_by or [Library.creation_timestamp.desc()]
