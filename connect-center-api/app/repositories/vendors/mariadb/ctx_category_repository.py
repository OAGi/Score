"""MariaDB repository implementation for Context Categories.

Implements CRUD operations, list filtering, and pagination.
AppUser joins are intentionally split out to service-layer enrichment.
"""


from __future__ import annotations

from datetime import datetime
from typing import Any, Literal

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.contracts.ctx_category import ContextCategoryRepositoryContract
from app.repositories.models.ctx_category import ContextCategoryRow
from app.repositories.vendors.mariadb.models.ctx_category import ContextCategory
from app.types.identifiers import (
    AppUserId,
    ContextCategoryId,
)


class MariaDbContextCategoryRepository(ContextCategoryRepositoryContract):
    """MariaDB-backed repository for Context Categories."""

    def __init__(self, session: AsyncSession):
        """Initialize MariaDbContextCategoryRepository.

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
        description: str | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
    ) -> tuple[int, list[ContextCategoryRow]]:
        """Handle list.

        Args:
            limit: Maximum number of rows to return.
            offset: Number of rows to skip.
            sorts: Sort definitions.
            name: Optional name filter.
            description: Optional textual description filter or payload field.
            creation_timestamp_before: Include rows created on/before this timestamp.
            creation_timestamp_after: Include rows created on/after this timestamp.
            last_update_timestamp_before: Include rows updated on/before this timestamp.
            last_update_timestamp_after: Include rows updated on/after this timestamp.

        Returns:
            Result of the operation.
        """
        where_clauses = _build_where_clauses(
            name=name,
            description=description,
            creation_timestamp_before=creation_timestamp_before,
            creation_timestamp_after=creation_timestamp_after,
            last_update_timestamp_before=last_update_timestamp_before,
            last_update_timestamp_after=last_update_timestamp_after,
        )

        total_stmt = select(func.count()).select_from(ContextCategory)
        if where_clauses:
            total_stmt = total_stmt.where(*where_clauses)
        total_res = await self._session.execute(total_stmt)
        total = int(total_res.scalar_one())

        stmt = _build_base_query()
        if where_clauses:
            stmt = stmt.where(*where_clauses)
        stmt = stmt.order_by(*_build_order_by(sorts)).limit(limit).offset(offset)
        res = await self._session.execute(stmt)
        items = [_to_context_category_row(row) for row in res.all()]
        return total, items

    async def get(self, context_category_id: ContextCategoryId) -> ContextCategoryRow | None:
        """Handle get.

        Args:
            context_category_id: Context category identifier.

        Returns:
            Result of the operation.
        """
        stmt = _build_base_query().where(ContextCategory.ctx_category_id == context_category_id)
        res = await self._session.execute(stmt)
        row = res.first()
        return _to_context_category_row(row) if row else None

    async def create(
        self,
        guid: str,
        name: str,
        description: str | None,
        created_by: AppUserId,
        last_updated_by: AppUserId,
        creation_timestamp: Any,
        last_update_timestamp: Any,
    ) -> ContextCategoryId:
        """Handle create.

        Args:
            guid: Value for `guid`.
            name: Category name.
            description: Optional category description.
            created_by: Identifier of the user who created the record.
            last_updated_by: Identifier of the user who last updated the record.
            creation_timestamp: Value for `creation_timestamp`.
            last_update_timestamp: Value for `last_update_timestamp`.

        Returns:
            Result of the operation.
        """
        obj = ContextCategory(
            guid=guid,
            name=name,
            description=description,
            created_by=created_by,
            last_updated_by=last_updated_by,
            creation_timestamp=creation_timestamp,
            last_update_timestamp=last_update_timestamp,
        )
        self._session.add(obj)
        await self._session.flush()
        await self._session.refresh(obj)
        return ContextCategoryId(obj.ctx_category_id)

    async def update(
        self,
        context_category_id: ContextCategoryId,
        name: str | None,
        name_set: bool,
        description: str | None,
        description_set: bool,
        last_updated_by: AppUserId,
        last_update_timestamp: Any,
    ) -> bool:
        """Handle update.

        Args:
            context_category_id: Context category identifier.
            name: Optional name filter.
            name_set: Value for `name_set`.
            description: Optional textual description filter or payload field.
            description_set: Value for `description_set`.
            last_updated_by: Identifier of the user who last updated the record.
            last_update_timestamp: Value for `last_update_timestamp`.

        Returns:
            Result of the operation.
        """
        stmt = select(ContextCategory).where(ContextCategory.ctx_category_id == context_category_id)
        res = await self._session.execute(stmt)
        obj = res.scalars().first()
        if obj is None:
            return False

        if name_set:
            obj.name = name
        if description_set:
            obj.description = description
        obj.last_updated_by = last_updated_by
        obj.last_update_timestamp = last_update_timestamp
        return True

    async def delete(self, context_category_id: ContextCategoryId) -> bool:
        """Handle delete.

        Args:
            context_category_id: Context category identifier.

        Returns:
            Result of the operation.
        """
        stmt = select(ContextCategory).where(ContextCategory.ctx_category_id == context_category_id)
        res = await self._session.execute(stmt)
        obj = res.scalars().first()
        if obj is None:
            return False
        await self._session.delete(obj)
        await self._session.flush()
        return True


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


def _build_base_query():
    """Internal helper for select."""
    return select(
        ContextCategory.ctx_category_id,
        ContextCategory.guid,
        ContextCategory.name,
        ContextCategory.description,
        ContextCategory.creation_timestamp,
        ContextCategory.last_update_timestamp,
        ContextCategory.created_by,
        ContextCategory.last_updated_by,
    )


def _to_context_category_row(row: Any) -> ContextCategoryRow:
    """Internal helper for row to row.

    Args:
        row: Repository row model to convert into a DTO.

    Returns:
        Result of the operation.
    """
    (
        ctx_category_id,
        guid,
        name,
        description,
        creation_timestamp,
        last_update_timestamp,
        created_by,
        last_updated_by,
    ) = row
    return ContextCategoryRow(
        ctx_category_id=ctx_category_id,
        guid=str(guid),
        name=str(name),
        description=str(description) if description is not None else None,
        created_by=created_by,
        creation_timestamp=_as_dt(creation_timestamp),
        last_updated_by=last_updated_by,
        last_update_timestamp=_as_dt(last_update_timestamp),
    )


def _build_where_clauses(
    name: str | None,
    description: str | None,
    creation_timestamp_before: datetime | None,
    creation_timestamp_after: datetime | None,
    last_update_timestamp_before: datetime | None,
    last_update_timestamp_after: datetime | None,
) -> list[object]:
    """Internal helper for build where clauses.

    Args:
        name: Optional name filter.
        description: Optional textual description filter or payload field.
        creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
        last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

    Returns:
        Result of the operation.
    """
    clauses: list[object] = []
    if name:
        clauses.append(ContextCategory.name.like(f"%{name}%"))
    if description:
        clauses.append(ContextCategory.description.like(f"%{description}%"))
    if creation_timestamp_after is not None:
        clauses.append(ContextCategory.creation_timestamp >= creation_timestamp_after)
    if creation_timestamp_before is not None:
        clauses.append(ContextCategory.creation_timestamp <= creation_timestamp_before)
    if last_update_timestamp_after is not None:
        clauses.append(ContextCategory.last_update_timestamp >= last_update_timestamp_after)
    if last_update_timestamp_before is not None:
        clauses.append(ContextCategory.last_update_timestamp <= last_update_timestamp_before)
    return clauses


def _build_order_by(sorts: list[tuple[str, Literal["ASC", "DESC"]]] | None):
    """Internal helper for build order by.

    Args:
        sorts: Value for `sorts`.

    Returns:
        Result of the operation.
    """
    if not sorts:
        return [ContextCategory.ctx_category_id.asc()]

    col_map = {
        "name": ContextCategory.name,
        "description": ContextCategory.description,
        "creation_timestamp": ContextCategory.creation_timestamp,
        "last_update_timestamp": ContextCategory.last_update_timestamp,
    }
    out = []
    for column, direction in sorts:
        col = col_map[column]
        out.append(col.desc() if direction == "DESC" else col.asc())
    return out
