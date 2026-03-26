"""MariaDB repository implementation for Context Schemes and Values.

Implements CRUD operations with filtering and pagination.
AppUser joins are intentionally split out to service-layer enrichment.
"""


from __future__ import annotations

from datetime import datetime
from typing import Any, Literal

from sqlalchemy import delete as sa_delete
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.contracts.ctx_scheme import CtxSchemeRepositoryContract
from app.repositories.models import ContextCategorySummaryRow, CtxSchemeValueRow
from app.repositories.models.ctx_scheme import CtxSchemeRow
from app.repositories.vendors.mariadb.models.biz_ctx import BizCtxValue
from app.repositories.vendors.mariadb.models.ctx_category import ContextCategory
from app.repositories.vendors.mariadb.models.ctx_scheme import CtxScheme, CtxSchemeValue
from app.types.identifiers import (
    AppUserId,
    BizCtxId,
    ContextCategoryId,
    CtxSchemeId,
    CtxSchemeValueId,
)


class MariaDbCtxSchemeRepository(CtxSchemeRepositoryContract):
    """MariaDB-backed repository for Context Schemes."""

    def __init__(self, session: AsyncSession):
        """Initialize MariaDbCtxSchemeRepository.

        Args:
            session: Database session bound to the current request.
        """
        self._session = session

    async def list(
        self,
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        scheme_id: str | None = None,
        scheme_name: str | None = None,
        description: str | None = None,
        scheme_agency_id: str | None = None,
        scheme_version_id: str | None = None,
        ctx_category_id: ContextCategoryId | None = None,
        ctx_category_name: str | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
    ) -> tuple[int, list[CtxSchemeRow]]:
        """Handle list.

        Args:
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            scheme_id: Optional context scheme identifier filter.
            scheme_name: Optional context scheme name filter.
            description: Optional textual description filter or payload field.
            scheme_agency_id: Optional context scheme agency identifier filter.
            scheme_version_id: Optional context scheme version identifier filter.
            ctx_category_id: Optional context category identifier filter.
            ctx_category_name: Optional context category name filter.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

        Returns:
            Result of the operation.
        """
        where_clauses = _build_where_clauses(
            scheme_id=scheme_id,
            scheme_name=scheme_name,
            description=description,
            scheme_agency_id=scheme_agency_id,
            scheme_version_id=scheme_version_id,
            ctx_category_id=ctx_category_id,
            ctx_category_name=ctx_category_name,
            creation_timestamp_before=creation_timestamp_before,
            creation_timestamp_after=creation_timestamp_after,
            last_update_timestamp_before=last_update_timestamp_before,
            last_update_timestamp_after=last_update_timestamp_after,
        )

        total_stmt = select(func.count()).select_from(CtxScheme)
        if ctx_category_name:
            total_stmt = total_stmt.outerjoin(ContextCategory, ContextCategory.ctx_category_id == CtxScheme.ctx_category_id)
        if where_clauses:
            total_stmt = total_stmt.where(*where_clauses)
        total = int((await self._session.execute(total_stmt)).scalar_one())

        stmt = _build_base_query()
        if where_clauses:
            stmt = stmt.where(*where_clauses)
        stmt = stmt.order_by(*_build_order_by(sorts)).limit(limit).offset(offset)
        rows = (await self._session.execute(stmt)).all()
        items = _to_ctx_scheme_rows(rows)
        return total, items

    async def get(self, ctx_scheme_id: CtxSchemeId) -> CtxSchemeRow | None:
        """Handle get.

        Args:
            ctx_scheme_id: Context scheme identifier.

        Returns:
            Result of the operation.
        """
        stmt = _build_base_query().where(CtxScheme.ctx_scheme_id == ctx_scheme_id)
        rows = (await self._session.execute(stmt)).all()
        items = _to_ctx_scheme_rows(rows)
        return items[0] if items else None

    async def create(
        self,
        guid: str,
        scheme_id: str,
        scheme_name: str,
        description: str | None,
        scheme_agency_id: str,
        scheme_version_id: str,
        ctx_category_id: ContextCategoryId | None,
        created_by: AppUserId,
        last_updated_by: AppUserId,
        creation_timestamp: Any,
        last_update_timestamp: Any,
    ) -> CtxSchemeId:
        """Handle create.

        Args:
            guid: Value for `guid`.
            scheme_id: Optional context scheme identifier filter.
            scheme_name: Optional context scheme name filter.
            description: Optional textual description filter or payload field.
            scheme_agency_id: Optional context scheme agency identifier filter.
            scheme_version_id: Optional context scheme version identifier filter.
            ctx_category_id: Optional context category identifier filter.
            created_by: Identifier of the user who created the record.
            last_updated_by: Identifier of the user who last updated the record.
            creation_timestamp: Value for `creation_timestamp`.
            last_update_timestamp: Value for `last_update_timestamp`.

        Returns:
            Result of the operation.
        """
        obj = CtxScheme(
            guid=guid,
            scheme_id=scheme_id,
            scheme_name=scheme_name,
            description=description,
            scheme_agency_id=scheme_agency_id,
            scheme_version_id=scheme_version_id,
            ctx_category_id=ctx_category_id,
            created_by=created_by,
            last_updated_by=last_updated_by,
            creation_timestamp=creation_timestamp,
            last_update_timestamp=last_update_timestamp,
        )
        self._session.add(obj)
        await self._session.flush()
        await self._session.refresh(obj)
        return CtxSchemeId(obj.ctx_scheme_id)

    async def update(
        self,
        ctx_scheme_id: CtxSchemeId,
        scheme_id: str | None,
        scheme_id_set: bool,
        scheme_name: str | None,
        scheme_name_set: bool,
        description: str | None,
        description_set: bool,
        scheme_agency_id: str | None,
        scheme_agency_id_set: bool,
        scheme_version_id: str | None,
        scheme_version_id_set: bool,
        ctx_category_id: ContextCategoryId | None,
        ctx_category_id_set: bool,
        last_updated_by: AppUserId,
        last_update_timestamp: Any,
    ) -> bool:
        """Handle update.

        Args:
            ctx_scheme_id: Context scheme identifier.
            scheme_id: Optional context scheme identifier filter.
            scheme_id_set: Value for `scheme_id_set`.
            scheme_name: Optional context scheme name filter.
            scheme_name_set: Value for `scheme_name_set`.
            description: Optional textual description filter or payload field.
            description_set: Value for `description_set`.
            scheme_agency_id: Optional context scheme agency identifier filter.
            scheme_agency_id_set: Value for `scheme_agency_id_set`.
            scheme_version_id: Optional context scheme version identifier filter.
            scheme_version_id_set: Value for `scheme_version_id_set`.
            ctx_category_id: Optional context category identifier filter.
            ctx_category_id_set: Value for `ctx_category_id_set`.
            last_updated_by: Identifier of the user who last updated the record.
            last_update_timestamp: Value for `last_update_timestamp`.

        Returns:
            Result of the operation.
        """
        stmt = select(CtxScheme).where(CtxScheme.ctx_scheme_id == ctx_scheme_id)
        res = await self._session.execute(stmt)
        obj = res.scalars().first()
        if obj is None:
            return False

        if scheme_id_set:
            obj.scheme_id = "" if scheme_id is None else scheme_id
        if scheme_name_set:
            obj.scheme_name = "" if scheme_name is None else scheme_name
        if description_set:
            obj.description = description
        if scheme_agency_id_set:
            obj.scheme_agency_id = "" if scheme_agency_id is None else scheme_agency_id
        if scheme_version_id_set:
            obj.scheme_version_id = "" if scheme_version_id is None else scheme_version_id
        if ctx_category_id_set:
            obj.ctx_category_id = ctx_category_id

        obj.last_updated_by = last_updated_by
        obj.last_update_timestamp = last_update_timestamp
        return True

    async def delete(self, ctx_scheme_id: CtxSchemeId) -> bool:
        """Handle delete.

        Args:
            ctx_scheme_id: Context scheme identifier.

        Returns:
            Result of the operation.
        """
        stmt = select(CtxScheme).where(CtxScheme.ctx_scheme_id == ctx_scheme_id)
        res = await self._session.execute(stmt)
        obj = res.scalars().first()
        if obj is None:
            return False
        await self._session.execute(sa_delete(CtxSchemeValue).where(CtxSchemeValue.owner_ctx_scheme_id == ctx_scheme_id))
        await self._session.delete(obj)
        return True

    async def create_value(
        self,
        guid: str,
        owner_ctx_scheme_id: CtxSchemeId,
        value: str,
        meaning: str | None,
    ) -> CtxSchemeValueId:
        """Handle create value.

        Args:
            guid: Value for `guid`.
            owner_ctx_scheme_id: Owner context scheme identifier.
            value: Context value string.
            meaning: Context value meaning/description.

        Returns:
            Result of the operation.
        """
        obj = CtxSchemeValue(guid=guid, owner_ctx_scheme_id=owner_ctx_scheme_id, value=value, meaning=meaning)
        self._session.add(obj)
        await self._session.flush()
        await self._session.refresh(obj)
        return CtxSchemeValueId(obj.ctx_scheme_value_id)

    async def get_value(self, ctx_scheme_value_id: CtxSchemeValueId) -> CtxSchemeValueRow | None:
        """Handle get value.

        Args:
            ctx_scheme_value_id: Context scheme value identifier.

        Returns:
            Result of the operation.
        """
        stmt = select(CtxSchemeValue).where(CtxSchemeValue.ctx_scheme_value_id == ctx_scheme_value_id)
        res = await self._session.execute(stmt)
        obj = res.scalars().first()
        if obj is None:
            return None
        return CtxSchemeValueRow(
            ctx_scheme_value_id=obj.ctx_scheme_value_id,
            guid=str(obj.guid),
            owner_ctx_scheme_id=obj.owner_ctx_scheme_id,
            value=str(obj.value),
            meaning=obj.meaning,
        )

    async def update_value(
        self,
        ctx_scheme_value_id: CtxSchemeValueId,
        value: str | None,
        value_set: bool,
        meaning: str | None,
        meaning_set: bool,
    ) -> bool:
        """Handle update value.

        Args:
            ctx_scheme_value_id: Context scheme value identifier.
            value: Context value string.
            value_set: Value for `value_set`.
            meaning: Context value meaning/description.
            meaning_set: Value for `meaning_set`.

        Returns:
            Result of the operation.
        """
        stmt = select(CtxSchemeValue).where(CtxSchemeValue.ctx_scheme_value_id == ctx_scheme_value_id)
        res = await self._session.execute(stmt)
        obj = res.scalars().first()
        if obj is None:
            return False
        if value_set:
            obj.value = "" if value is None else value
        if meaning_set:
            obj.meaning = meaning
        return True

    async def delete_value(self, ctx_scheme_value_id: CtxSchemeValueId) -> bool:
        """Handle delete value.

        Args:
            ctx_scheme_value_id: Context scheme value identifier.

        Returns:
            Result of the operation.
        """
        stmt = select(CtxSchemeValue).where(CtxSchemeValue.ctx_scheme_value_id == ctx_scheme_value_id)
        res = await self._session.execute(stmt)
        obj = res.scalars().first()
        if obj is None:
            return False
        await self._session.delete(obj)
        return True

    async def get_biz_ctx_ids_using_ctx_scheme_value(self, ctx_scheme_value_id: CtxSchemeValueId) -> list[BizCtxId]:
        """Handle get biz ctx ids using ctx scheme value.

        Args:
            ctx_scheme_value_id: Context scheme value identifier.

        Returns:
            Result of the operation.
        """
        stmt = select(BizCtxValue.biz_ctx_id).where(BizCtxValue.ctx_scheme_value_id == ctx_scheme_value_id).distinct()
        res = await self._session.execute(stmt)
        return [row[0] for row in res.all()]


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
            CtxScheme.ctx_scheme_id,
            CtxScheme.guid,
            CtxScheme.scheme_id,
            CtxScheme.scheme_name,
            CtxScheme.description,
            CtxScheme.scheme_agency_id,
            CtxScheme.scheme_version_id,
            CtxScheme.creation_timestamp,
            CtxScheme.last_update_timestamp,
            CtxScheme.created_by,
            CtxScheme.last_updated_by,
            ContextCategory.ctx_category_id,
            ContextCategory.name,
            CtxSchemeValue.ctx_scheme_value_id,
            CtxSchemeValue.guid,
            CtxSchemeValue.value,
            CtxSchemeValue.meaning,
        )
        .outerjoin(ContextCategory, ContextCategory.ctx_category_id == CtxScheme.ctx_category_id)
        .outerjoin(CtxSchemeValue, CtxSchemeValue.owner_ctx_scheme_id == CtxScheme.ctx_scheme_id)
    )


def _to_ctx_scheme_rows(rows: Any) -> list[CtxSchemeRow]:
    """Internal helper for rows to rows.

    Args:
        rows: Value for `rows`.

    Returns:
        Result of the operation.
    """
    by_id: dict[int, CtxSchemeRow] = {}
    for row in rows:
        (
            ctx_scheme_id,
            guid,
            scheme_id,
            scheme_name,
            description,
            scheme_agency_id,
            scheme_version_id,
            creation_timestamp,
            last_update_timestamp,
            created_by,
            last_updated_by,
            ctx_category_id,
            ctx_category_name,
            ctx_scheme_value_id,
            value_guid,
            value_value,
            value_meaning,
        ) = row

        scheme_key = ctx_scheme_id
        existing = by_id.get(scheme_key)
        if existing is None:
            category = (
                ContextCategorySummaryRow(ctx_category_id=ctx_category_id, name=str(ctx_category_name))
                if ctx_category_id is not None
                else None
            )

            existing = CtxSchemeRow(
                ctx_scheme_id=scheme_key,
                guid=str(guid),
                scheme_id=str(scheme_id),
                scheme_name=str(scheme_name),
                description=str(description) if description is not None else None,
                scheme_agency_id=str(scheme_agency_id),
                scheme_version_id=str(scheme_version_id),
                ctx_category=category,
                values=[],
                created_by=created_by,
                creation_timestamp=_as_dt(creation_timestamp),
                last_updated_by=last_updated_by,
                last_update_timestamp=_as_dt(last_update_timestamp),
            )
            by_id[scheme_key] = existing

        if ctx_scheme_value_id is not None:
            existing.values.append(
                CtxSchemeValueRow(
                    ctx_scheme_value_id=ctx_scheme_value_id,
                    guid=str(value_guid),
                    owner_ctx_scheme_id=scheme_key,
                    value=str(value_value),
                    meaning=str(value_meaning) if value_meaning is not None else None,
                )
            )

    items = list(by_id.values())
    for item in items:
        item.values.sort(key=lambda v: v.ctx_scheme_value_id)
    return items


def _build_where_clauses(
    scheme_id: str | None,
    scheme_name: str | None,
    description: str | None,
    scheme_agency_id: str | None,
    scheme_version_id: str | None,
    ctx_category_id: int | None,
    ctx_category_name: str | None,
    creation_timestamp_before: datetime | None,
    creation_timestamp_after: datetime | None,
    last_update_timestamp_before: datetime | None,
    last_update_timestamp_after: datetime | None,
):
    """Internal helper for build where clauses.

    Args:
        scheme_id: Optional context scheme identifier filter.
        scheme_name: Optional context scheme name filter.
        description: Optional textual description filter or payload field.
        scheme_agency_id: Optional context scheme agency identifier filter.
        scheme_version_id: Optional context scheme version identifier filter.
        ctx_category_id: Optional context category identifier filter.
        ctx_category_name: Optional context category name filter.
        creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
        last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

    Returns:
        Result of the operation.
    """
    clauses = []

    if scheme_id:
        clauses.append(CtxScheme.scheme_id.ilike(f"%{scheme_id}%"))
    if scheme_name:
        clauses.append(CtxScheme.scheme_name.ilike(f"%{scheme_name}%"))
    if description:
        clauses.append(CtxScheme.description.ilike(f"%{description}%"))
    if scheme_agency_id:
        clauses.append(CtxScheme.scheme_agency_id.ilike(f"%{scheme_agency_id}%"))
    if scheme_version_id:
        clauses.append(CtxScheme.scheme_version_id.ilike(f"%{scheme_version_id}%"))
    if ctx_category_id is not None:
        clauses.append(CtxScheme.ctx_category_id == ctx_category_id)
    if ctx_category_name:
        clauses.append(ContextCategory.name.ilike(f"%{ctx_category_name}%"))

    if creation_timestamp_after is not None:
        clauses.append(CtxScheme.creation_timestamp >= creation_timestamp_after)
    if creation_timestamp_before is not None:
        clauses.append(CtxScheme.creation_timestamp <= creation_timestamp_before)

    if last_update_timestamp_after is not None:
        clauses.append(CtxScheme.last_update_timestamp >= last_update_timestamp_after)
    if last_update_timestamp_before is not None:
        clauses.append(CtxScheme.last_update_timestamp <= last_update_timestamp_before)

    return clauses


def _build_order_by(sorts: list[tuple[str, Literal["ASC", "DESC"]]] | None):
    """Internal helper for build order by.

    Args:
        sorts: Value for `sorts`.

    Returns:
        Result of the operation.
    """
    if not sorts:
        return [CtxScheme.scheme_id.asc()]

    mapping = {
        "scheme_id": CtxScheme.scheme_id,
        "scheme_name": CtxScheme.scheme_name,
        "description": CtxScheme.description,
        "scheme_agency_id": CtxScheme.scheme_agency_id,
        "scheme_version_id": CtxScheme.scheme_version_id,
        "creation_timestamp": CtxScheme.creation_timestamp,
        "last_update_timestamp": CtxScheme.last_update_timestamp,
    }
    order_by = []
    for s in sorts:
        col = mapping.get(s[0])
        if col is None:
            continue
        order_by.append(col.desc() if s[1] == "DESC" else col.asc())
    return order_by or [CtxScheme.scheme_id.asc()]
