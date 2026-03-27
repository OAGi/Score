"""MariaDB repository implementation for Code Lists.

Provides list/get queries with joined summary information and value-manifest
expansion, plus raw user ids for service-layer AppUser enrichment.
"""


from __future__ import annotations

from collections import defaultdict
from datetime import datetime
from typing import Any, Literal

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.contracts.code_list import CodeListRepositoryContract
from app.repositories.models import (
    CodeListValueRow,
    LibrarySummaryRow,
    LogSummaryRow,
    NamespaceSummaryRow,
    ReleaseSummaryRow,
)
from app.repositories.models.code_list import CodeListRow
from app.repositories.vendors.mariadb.models.code_list import (
    CodeList,
    CodeListManifest,
    CodeListValue,
    CodeListValueManifest,
)
from app.repositories.vendors.mariadb.models.library import Library
from app.repositories.vendors.mariadb.models.log import Log
from app.repositories.vendors.mariadb.models.namespace import Namespace
from app.repositories.vendors.mariadb.models.release import Release
from app.types.identifiers import (
    CodeListManifestId,
    ReleaseId,
)


class MariaDbCodeListRepository(CodeListRepositoryContract):
    """MariaDB-backed repository for code list read operations."""

    def __init__(self, session: AsyncSession):
        """Initialize MariaDbCodeListRepository.

        Args:
            session: Database session bound to the current request.
        """
        self._session = session

    async def list(
        self,
        release_id: ReleaseId,
        dependent_release_ids: list[ReleaseId],
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        name: str | None = None,
        list_id: str | None = None,
        version_id: str | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
    ) -> tuple[int, list[CodeListRow]]:
        """Handle list.

        Args:
            release_id: Release identifier used to scope the query.
            dependent_release_ids: Value for `dependent_release_ids`.
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            name: Optional name filter.
            list_id: Value for `list_id`.
            version_id: Value for `version_id`.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

        Returns:
            Result of the operation.
        """
        where_clauses = _build_where_clauses(
            release_id=release_id,
            dependent_release_ids=dependent_release_ids,
            name=name,
            list_id=list_id,
            version_id=version_id,
            creation_timestamp_before=creation_timestamp_before,
            creation_timestamp_after=creation_timestamp_after,
            last_update_timestamp_before=last_update_timestamp_before,
            last_update_timestamp_after=last_update_timestamp_after,
        )

        total_stmt = (
            select(func.count())
            .select_from(CodeListManifest)
            .join(CodeList, CodeList.code_list_id == CodeListManifest.code_list_id)
            .where(*where_clauses)
        )
        total = int((await self._session.execute(total_stmt)).scalar_one())

        order_by = _build_order_by(sorts)

        rows = [
            dict(row._mapping)
            for row in (
                await self._session.execute(
                    _build_base_query().where(*where_clauses).order_by(*order_by).limit(limit).offset(offset)
                )
            ).all()
        ]

        if not rows:
            return total, []

        values_by_manifest = await _values_by_manifest_ids(self._session, [int(row["code_list_manifest_id"]) for row in rows])
        items = [_to_code_list_row(row, values_by_manifest.get(int(row["code_list_manifest_id"]), [])) for row in rows]
        return total, items

    async def get(self, code_list_manifest_id: CodeListManifestId) -> CodeListRow | None:
        """Handle get.

        Args:
            code_list_manifest_id: Code list manifest identifier.

        Returns:
            Result of the operation.
        """
        row = (
            await self._session.execute(
                _build_base_query().where(CodeListManifest.code_list_manifest_id == code_list_manifest_id)
            )
        ).first()
        if row is None:
            return None

        row_dict = dict(row._mapping)
        values_by_manifest = await _values_by_manifest_ids(self._session, [int(row_dict["code_list_manifest_id"])])
        return _to_code_list_row(row_dict, values_by_manifest.get(int(row_dict["code_list_manifest_id"]), []))


def _build_base_query():
    return (
        select(
            CodeListManifest.code_list_manifest_id,
            CodeList.code_list_id,
            CodeList.guid,
            CodeList.enum_type_guid,
            CodeList.name,
            CodeList.list_id,
            CodeList.version_id,
            CodeList.definition,
            CodeList.remark,
            CodeList.definition_source,
            CodeList.extensible_indicator,
            CodeList.is_deprecated,
            CodeList.state,
            CodeList.creation_timestamp,
            CodeList.last_update_timestamp,
            CodeList.owner_user_id,
            CodeList.created_by,
            CodeList.last_updated_by,
            Namespace.namespace_id,
            Namespace.prefix.label("namespace_prefix"),
            Namespace.uri.label("namespace_uri"),
            Library.library_id,
            Library.name.label("library_name"),
            Release.release_id,
            Release.release_num,
            Release.state.label("release_state"),
            Log.log_id,
            Log.revision_num,
            Log.revision_tracking_num,
        )
        .select_from(CodeListManifest)
        .join(CodeList, CodeList.code_list_id == CodeListManifest.code_list_id)
        .join(Release, Release.release_id == CodeListManifest.release_id)
        .join(Library, Library.library_id == Release.library_id)
        .outerjoin(Namespace, Namespace.namespace_id == CodeList.namespace_id)
        .outerjoin(Log, Log.log_id == CodeListManifest.log_id)
    )


def _build_where_clauses(
    release_id: ReleaseId,
    dependent_release_ids: list[ReleaseId],
    name: str | None,
    list_id: str | None,
    version_id: str | None,
    creation_timestamp_before: datetime | None,
    creation_timestamp_after: datetime | None,
    last_update_timestamp_before: datetime | None,
    last_update_timestamp_after: datetime | None,
) -> list[object]:
    clauses: list[object] = [CodeListManifest.release_id.in_([release_id, *[x for x in dependent_release_ids]])]
    if name:
        clauses.append(CodeList.name.ilike(f"%{name}%"))
    if list_id:
        clauses.append(CodeList.list_id.ilike(f"%{list_id}%"))
    if version_id:
        clauses.append(CodeList.version_id.ilike(f"%{version_id}%"))
    if creation_timestamp_after is not None:
        clauses.append(CodeList.creation_timestamp >= creation_timestamp_after)
    if creation_timestamp_before is not None:
        clauses.append(CodeList.creation_timestamp <= creation_timestamp_before)
    if last_update_timestamp_after is not None:
        clauses.append(CodeList.last_update_timestamp >= last_update_timestamp_after)
    if last_update_timestamp_before is not None:
        clauses.append(CodeList.last_update_timestamp <= last_update_timestamp_before)
    return clauses


def _build_order_by(sorts: list[tuple[str, Literal["ASC", "DESC"]]] | None):
    if not sorts:
        return [CodeList.creation_timestamp.desc()]

    col_map = {
        "name": CodeList.name,
        "list_id": CodeList.list_id,
        "version_id": CodeList.version_id,
        "definition": CodeList.definition,
        "creation_timestamp": CodeList.creation_timestamp,
        "last_update_timestamp": CodeList.last_update_timestamp,
    }
    out = []
    for column, direction in sorts:
        col = col_map.get(column)
        if col is not None:
            out.append(col.desc() if direction == "DESC" else col.asc())
    return out if out else [CodeList.creation_timestamp.desc()]


async def _values_by_manifest_ids(session: AsyncSession, manifest_ids: list[int]) -> dict[int, list[CodeListValueRow]]:
    if not manifest_ids:
        return {}

    value_rows = [
        dict(row._mapping)
        for row in (
            await session.execute(
                select(
                    CodeListValueManifest.code_list_manifest_id,
                    CodeListValueManifest.code_list_value_manifest_id,
                    CodeListValue.code_list_value_id,
                    CodeListValue.guid,
                    CodeListValue.value,
                    CodeListValue.meaning,
                    CodeListValue.definition,
                    CodeListValue.is_deprecated,
                )
                .select_from(CodeListValueManifest)
                .join(CodeListValue, CodeListValue.code_list_value_id == CodeListValueManifest.code_list_value_id)
                .where(CodeListValueManifest.code_list_manifest_id.in_(manifest_ids))
                .order_by(CodeListValueManifest.code_list_value_manifest_id.asc())
            )
        ).all()
    ]

    values_by_manifest: dict[int, list[CodeListValueRow]] = defaultdict(list)
    for value_row in value_rows:
        values_by_manifest[int(value_row["code_list_manifest_id"])].append(
            CodeListValueRow(
                code_list_value_manifest_id=int(value_row["code_list_value_manifest_id"]),
                code_list_value_id=int(value_row["code_list_value_id"]),
                guid=str(value_row["guid"]),
                value=str(value_row["value"]),
                meaning=str(value_row["meaning"]) if value_row.get("meaning") is not None else None,
                definition=str(value_row["definition"]) if value_row.get("definition") is not None else None,
                is_deprecated=bool(value_row["is_deprecated"]),
            )
        )
    return values_by_manifest


def _to_code_list_row(row: dict[str, Any], values: list[CodeListValueRow]) -> CodeListRow:
    return CodeListRow(
        code_list_manifest_id=row["code_list_manifest_id"],
        code_list_id=row["code_list_id"],
        guid=str(row["guid"]),
        enum_type_guid=str(row["enum_type_guid"]) if row.get("enum_type_guid") is not None else None,
        name=str(row["name"]) if row.get("name") is not None else None,
        list_id=str(row["list_id"]),
        version_id=str(row["version_id"]),
        definition=str(row["definition"]) if row.get("definition") is not None else None,
        remark=str(row["remark"]) if row.get("remark") is not None else None,
        definition_source=str(row["definition_source"]) if row.get("definition_source") is not None else None,
        extensible_indicator=bool(row["extensible_indicator"]),
        is_deprecated=bool(row["is_deprecated"]),
        state=str(row["state"]) if row.get("state") is not None else None,
        values=values,
        namespace=_namespace_summary(row),
        library=_library_summary(row),
        release=_release_summary(row),
        log=_log_summary(row),
        owner_user_id=row["owner_user_id"],
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


def _library_summary(row: dict[str, Any]) -> LibrarySummaryRow:
    """Internal helper for library summary.

    Args:
        row: Repository row model to convert into a DTO.

    Returns:
        Result of the operation.
    """
    return LibrarySummaryRow(library_id=row["library_id"], name=str(row["library_name"]))


def _release_summary(row: dict[str, Any]) -> ReleaseSummaryRow:
    """Internal helper for release summary.

    Args:
        row: Repository row model to convert into a DTO.

    Returns:
        Result of the operation.
    """
    return ReleaseSummaryRow(
        release_id=row["release_id"],
        release_num=str(row.get("release_num")) if row.get("release_num") is not None else None,
        state=str(row["release_state"]),
    )


def _namespace_summary(row: dict[str, Any]) -> NamespaceSummaryRow | None:
    """Internal helper for namespace summary.

    Args:
        row: Repository row model to convert into a DTO.

    Returns:
        Result of the operation.
    """
    if row.get("namespace_id") is None:
        return None
    return NamespaceSummaryRow(
        namespace_id=row["namespace_id"],
        prefix=str(row.get("namespace_prefix")) if row.get("namespace_prefix") is not None else None,
        uri=str(row["namespace_uri"]),
    )


def _log_summary(row: dict[str, Any]) -> LogSummaryRow | None:
    """Internal helper for log summary.

    Args:
        row: Repository row model to convert into a DTO.

    Returns:
        Result of the operation.
    """
    if row.get("log_id") is None:
        return None
    return LogSummaryRow(
        log_id=row["log_id"],
        revision_num=int(row.get("revision_num") or 0),
        revision_tracking_num=int(row.get("revision_tracking_num") or 0),
    )
