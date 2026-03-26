"""MariaDB repository implementation for Business Contexts and Values.

Implements CRUD operations with filtering and pagination.
AppUser joins are intentionally split out to service-layer enrichment.
"""


from __future__ import annotations

from datetime import datetime
from typing import Any, Literal

from sqlalchemy import delete as sa_delete
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.contracts.biz_ctx import BizCtxRepositoryContract
from app.repositories.models import BizCtxValueRow, CtxSchemeValueSummaryRow
from app.repositories.models.biz_ctx import BizCtxRow, BizCtxValueDetailRow
from app.repositories.vendors.mariadb.models.biz_ctx import BizCtx, BizCtxValue
from app.repositories.vendors.mariadb.models.ctx_scheme import CtxSchemeValue
from app.types.identifiers import (
    AppUserId,
    BizCtxId,
    BizCtxValueId,
    CtxSchemeValueId,
)


class MariaDbBizCtxRepository(BizCtxRepositoryContract):
    """MariaDB-backed repository for Business Contexts."""

    def __init__(self, session: AsyncSession):
        """Initialize MariaDbBizCtxRepository.

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
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
    ) -> tuple[int, list[BizCtxRow]]:
        """Handle list.

        Args:
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            name: Optional name filter.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

        Returns:
            Result of the operation.
        """
        where_clauses = _build_where_clauses(name=name, creation_timestamp_before=creation_timestamp_before,
            creation_timestamp_after=creation_timestamp_after, last_update_timestamp_before=last_update_timestamp_before,
            last_update_timestamp_after=last_update_timestamp_after)

        total_stmt = select(func.count()).select_from(BizCtx)
        if where_clauses:
            total_stmt = total_stmt.where(*where_clauses)
        total = int((await self._session.execute(total_stmt)).scalar_one())

        stmt = _build_base_query()
        if where_clauses:
            stmt = stmt.where(*where_clauses)
        stmt = stmt.order_by(*_build_order_by(sorts)).limit(limit).offset(offset)
        rows = (await self._session.execute(stmt)).all()
        items = _to_biz_ctx_rows(rows)
        return total, items

    async def get(self, biz_ctx_id: BizCtxId) -> BizCtxRow | None:
        """Handle get.

        Args:
            biz_ctx_id: Business context identifier.

        Returns:
            Result of the operation.
        """
        stmt = _build_base_query().where(BizCtx.biz_ctx_id == biz_ctx_id)
        rows = (await self._session.execute(stmt)).all()
        items = _to_biz_ctx_rows(rows)
        return items[0] if items else None

    async def create(
        self,
        guid: str,
        name: str | None,
        created_by: AppUserId,
        last_updated_by: AppUserId,
        creation_timestamp: Any,
        last_update_timestamp: Any,
    ) -> BizCtxId:
        """Handle create.

        Args:
            guid: Value for `guid`.
            name: Optional name filter.
            created_by: Identifier of the user who created the record.
            last_updated_by: Identifier of the user who last updated the record.
            creation_timestamp: Value for `creation_timestamp`.
            last_update_timestamp: Value for `last_update_timestamp`.

        Returns:
            Result of the operation.
        """
        obj = BizCtx(
            guid=guid,
            name=name,
            created_by=created_by,
            last_updated_by=last_updated_by,
            creation_timestamp=creation_timestamp,
            last_update_timestamp=last_update_timestamp,
        )
        self._session.add(obj)
        await self._session.flush()
        await self._session.refresh(obj)
        return BizCtxId(obj.biz_ctx_id)

    async def update(
        self,
        biz_ctx_id: BizCtxId,
        name: str | None,
        name_set: bool,
        last_updated_by: AppUserId,
        last_update_timestamp: Any,
    ) -> bool:
        """Handle update.

        Args:
            biz_ctx_id: Business context identifier.
            name: Optional name filter.
            name_set: Value for `name_set`.
            last_updated_by: Identifier of the user who last updated the record.
            last_update_timestamp: Value for `last_update_timestamp`.

        Returns:
            Result of the operation.
        """
        stmt = select(BizCtx).where(BizCtx.biz_ctx_id == biz_ctx_id)
        res = await self._session.execute(stmt)
        obj = res.scalars().first()
        if obj is None:
            return False
        if name_set:
            obj.name = name
        obj.last_updated_by = last_updated_by
        obj.last_update_timestamp = last_update_timestamp
        return True

    async def delete(self, biz_ctx_id: BizCtxId) -> bool:
        """Handle delete.

        Args:
            biz_ctx_id: Business context identifier.

        Returns:
            Result of the operation.
        """
        stmt = select(BizCtx).where(BizCtx.biz_ctx_id == biz_ctx_id)
        res = await self._session.execute(stmt)
        obj = res.scalars().first()
        if obj is None:
            return False
        await self._session.execute(sa_delete(BizCtxValue).where(BizCtxValue.biz_ctx_id == biz_ctx_id))
        await self._session.delete(obj)
        return True

    async def create_value(self, *, biz_ctx_id: BizCtxId, ctx_scheme_value_id: CtxSchemeValueId) -> BizCtxValueId:
        """Handle create value.

        Args:
            biz_ctx_id: Business context identifier.
            ctx_scheme_value_id: Context scheme value identifier.

        Returns:
            Result of the operation.
        """
        existing_stmt = select(BizCtxValue).where(
            BizCtxValue.biz_ctx_id == biz_ctx_id,
            BizCtxValue.ctx_scheme_value_id == ctx_scheme_value_id,
        )
        existing = await self._session.execute(existing_stmt)
        if existing.scalars().first() is not None:
            raise ValueError("A business context value already exists for the given business context and scheme value.")

        obj = BizCtxValue(biz_ctx_id=biz_ctx_id, ctx_scheme_value_id=ctx_scheme_value_id)
        self._session.add(obj)
        await self._session.flush()
        await self._session.refresh(obj)
        return BizCtxValueId(obj.biz_ctx_value_id)

    async def update_value(
        self,
        biz_ctx_value_id: BizCtxValueId,
        ctx_scheme_value_id: CtxSchemeValueId,
        ctx_scheme_value_id_set: bool,
    ) -> bool:
        """Handle update value.

        Args:
            biz_ctx_value_id: Business context value identifier.
            ctx_scheme_value_id: Context scheme value identifier.
            ctx_scheme_value_id_set: Value for `ctx_scheme_value_id_set`.

        Returns:
            Result of the operation.
        """
        stmt = select(BizCtxValue).where(BizCtxValue.biz_ctx_value_id == biz_ctx_value_id)
        res = await self._session.execute(stmt)
        obj = res.scalars().first()
        if obj is None:
            return False
        if ctx_scheme_value_id_set:
            dup_stmt = select(BizCtxValue).where(
                BizCtxValue.biz_ctx_id == obj.biz_ctx_id,
                BizCtxValue.ctx_scheme_value_id == ctx_scheme_value_id,
                BizCtxValue.biz_ctx_value_id != obj.biz_ctx_value_id,
            )
            dup_res = await self._session.execute(dup_stmt)
            if dup_res.scalars().first() is not None:
                raise ValueError("A business context value already exists for the given business context and scheme value.")
            obj.ctx_scheme_value_id = ctx_scheme_value_id
        return True

    async def get_value(self, biz_ctx_value_id: BizCtxValueId) -> BizCtxValueDetailRow | None:
        """Handle get value.

        Args:
            biz_ctx_value_id: Business context value identifier.

        Returns:
            Result of the operation.
        """
        stmt = select(
            BizCtxValue.biz_ctx_value_id,
            BizCtxValue.biz_ctx_id,
            BizCtxValue.ctx_scheme_value_id,
        ).where(BizCtxValue.biz_ctx_value_id == biz_ctx_value_id)
        row = (await self._session.execute(stmt)).first()
        if row is None:
            return None
        return BizCtxValueDetailRow(
            biz_ctx_value_id=row.biz_ctx_value_id,
            biz_ctx_id=row.biz_ctx_id,
            ctx_scheme_value_id=row.ctx_scheme_value_id,
        )

    async def delete_value(self, biz_ctx_value_id: BizCtxValueId) -> bool:
        """Handle delete value.

        Args:
            biz_ctx_value_id: Business context value identifier.

        Returns:
            Result of the operation.
        """
        stmt = select(BizCtxValue).where(BizCtxValue.biz_ctx_value_id == biz_ctx_value_id)
        res = await self._session.execute(stmt)
        obj = res.scalars().first()
        if obj is None:
            return False
        await self._session.delete(obj)
        return True

    async def ctx_scheme_value_exists(self, ctx_scheme_value_id: CtxSchemeValueId) -> bool:
        """Handle ctx scheme value exists.

        Args:
            ctx_scheme_value_id: Context scheme value identifier.

        Returns:
            Result of the operation.
        """
        stmt = select(CtxSchemeValue.ctx_scheme_value_id).where(CtxSchemeValue.ctx_scheme_value_id == ctx_scheme_value_id)
        return (await self._session.execute(stmt)).first() is not None


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
    return (
        select(
            BizCtx.biz_ctx_id,
            BizCtx.guid,
            BizCtx.name,
            BizCtx.creation_timestamp,
            BizCtx.last_update_timestamp,
            BizCtx.created_by,
            BizCtx.last_updated_by,
            BizCtxValue.biz_ctx_value_id,
            CtxSchemeValue.ctx_scheme_value_id,
            CtxSchemeValue.value,
        )
        .outerjoin(BizCtxValue, BizCtxValue.biz_ctx_id == BizCtx.biz_ctx_id)
        .outerjoin(CtxSchemeValue, CtxSchemeValue.ctx_scheme_value_id == BizCtxValue.ctx_scheme_value_id)
    )


def _to_biz_ctx_rows(rows: Any) -> list[BizCtxRow]:
    """Internal helper for rows to rows.

    Args:
        rows: Value for `rows`.

    Returns:
        Result of the operation.
    """
    by_id: dict[int, BizCtxRow] = {}
    for row in rows:
        (
            biz_ctx_id,
            guid,
            name,
            creation_timestamp,
            last_update_timestamp,
            created_by,
            last_updated_by,
            biz_ctx_value_id,
            ctx_scheme_value_id,
            ctx_scheme_value_value,
        ) = row

        ctx_key = biz_ctx_id
        existing = by_id.get(ctx_key)
        if existing is None:
            existing = BizCtxRow(
                biz_ctx_id=ctx_key,
                guid=str(guid),
                name=str(name) if name is not None else None,
                values=[],
                created_by=created_by,
                creation_timestamp=_as_dt(creation_timestamp),
                last_updated_by=last_updated_by,
                last_update_timestamp=_as_dt(last_update_timestamp),
            )
            by_id[ctx_key] = existing

        if biz_ctx_value_id is not None and ctx_scheme_value_id is not None:
            existing.values.append(
                BizCtxValueRow(
                    biz_ctx_value_id=biz_ctx_value_id,
                    ctx_scheme_value=CtxSchemeValueSummaryRow(
                        ctx_scheme_value_id=ctx_scheme_value_id,
                        value=str(ctx_scheme_value_value),
                    ),
                )
            )

    items = list(by_id.values())
    for item in items:
        item.values.sort(key=lambda v: v.biz_ctx_value_id)
    return items


def _build_where_clauses(
    name: str | None,
    creation_timestamp_before: datetime | None,
    creation_timestamp_after: datetime | None,
    last_update_timestamp_before: datetime | None,
    last_update_timestamp_after: datetime | None,
):
    """Internal helper for build where clauses.

    Args:
        name: Optional name filter.
        creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
        last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

    Returns:
        Result of the operation.
    """
    clauses = []
    if name:
        clauses.append(BizCtx.name.ilike(f"%{name}%"))
    if creation_timestamp_after is not None:
        clauses.append(BizCtx.creation_timestamp >= creation_timestamp_after)
    if creation_timestamp_before is not None:
        clauses.append(BizCtx.creation_timestamp <= creation_timestamp_before)
    if last_update_timestamp_after is not None:
        clauses.append(BizCtx.last_update_timestamp >= last_update_timestamp_after)
    if last_update_timestamp_before is not None:
        clauses.append(BizCtx.last_update_timestamp <= last_update_timestamp_before)
    return clauses


def _build_order_by(sorts: list[tuple[str, Literal["ASC", "DESC"]]] | None):
    """Internal helper for build order by.

    Args:
        sorts: Value for `sorts`.

    Returns:
        Result of the operation.
    """
    if not sorts:
        return [BizCtx.name.asc()]

    mapping = {
        "name": BizCtx.name,
        "creation_timestamp": BizCtx.creation_timestamp,
        "last_update_timestamp": BizCtx.last_update_timestamp,
    }
    order_by = []
    for s in sorts:
        col = mapping.get(s[0])
        if col is None:
            continue
        order_by.append(col.desc() if s[1] == "DESC" else col.asc())
    return order_by or [BizCtx.name.asc()]
