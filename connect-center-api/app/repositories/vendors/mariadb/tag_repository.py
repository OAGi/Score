"""MariaDB repository implementation for tags."""


from __future__ import annotations

from datetime import datetime
from typing import Any, Literal

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.contracts.tag import TagRepositoryContract
from app.repositories.models.tag import TagRow
from app.repositories.vendors.mariadb.models.tag import Tag


class MariaDbTagRepository(TagRepositoryContract):
    """MariaDB-backed repository for tag read operations."""

    def __init__(self, session: AsyncSession):
        """Initialize MariaDbTagRepository.

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
    ) -> tuple[int, list[TagRow]]:
        """Handle list.

        Args:
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            name: Optional name filter.
            description: Optional textual description filter or payload field.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

        Returns:
            Result of the operation.
        """
        where_clauses = []
        if name:
            where_clauses.append(Tag.name.ilike(f"%{name}%"))
        if description:
            where_clauses.append(Tag.description.ilike(f"%{description}%"))
        if creation_timestamp_after is not None:
            where_clauses.append(Tag.creation_timestamp >= creation_timestamp_after)
        if creation_timestamp_before is not None:
            where_clauses.append(Tag.creation_timestamp <= creation_timestamp_before)
        if last_update_timestamp_after is not None:
            where_clauses.append(Tag.last_update_timestamp >= last_update_timestamp_after)
        if last_update_timestamp_before is not None:
            where_clauses.append(Tag.last_update_timestamp <= last_update_timestamp_before)

        total_stmt = select(func.count()).select_from(Tag).where(*where_clauses)
        total = int((await self._session.execute(total_stmt)).scalar_one())

        sort_map = {
            "name": Tag.name,
            "description": Tag.description,
            "creation_timestamp": Tag.creation_timestamp,
            "last_update_timestamp": Tag.last_update_timestamp,
        }
        order_by = []
        for sort in sorts:
            col = sort_map.get(sort[0])
            if col is not None:
                order_by.append(col.desc() if sort[1] == "DESC" else col.asc())
        if not order_by:
            order_by = [Tag.creation_timestamp.desc()]

        rows = [
            dict(row._mapping)
            for row in (
                await self._session.execute(
                    select(
                        Tag.tag_id,
                        Tag.name,
                        Tag.description,
                        Tag.background_color,
                        Tag.text_color,
                        Tag.created_by,
                        Tag.last_updated_by,
                        Tag.creation_timestamp,
                        Tag.last_update_timestamp,
                    )
                    .select_from(Tag)
                    .where(*where_clauses)
                    .order_by(*order_by)
                    .limit(limit)
                    .offset(offset)
                )
            ).all()
        ]
        return total, [self._to_row(row) for row in rows]

    def _to_row(self, row: dict[str, Any]) -> TagRow:
        """Internal helper for to row.

        Args:
            row: Repository row model to convert into a DTO.

        Returns:
            Result of the operation.
        """
        return TagRow(
            tag_id=row["tag_id"],
            name=str(row["name"]),
            description=str(row["description"]) if row.get("description") is not None else None,
            color=str(row["background_color"]) if row.get("background_color") is not None else None,
            text_color=str(row["text_color"]) if row.get("text_color") is not None else None,
            created_by=row["created_by"],
            last_updated_by=row["last_updated_by"],
            creation_timestamp=_as_dt(row["creation_timestamp"]),
            last_update_timestamp=_as_dt(row["last_update_timestamp"]),
        )


def _as_dt(value: Any) -> datetime:
    """Internal helper for as dt.

    Args:
        value: Context value string.

    Returns:
        Result of the operation.
    """
    if isinstance(value, datetime):
        return value
    return datetime.fromisoformat(str(value).replace("Z", "+00:00"))
