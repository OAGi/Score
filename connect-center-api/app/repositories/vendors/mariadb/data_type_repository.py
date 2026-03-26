"""MariaDB repository implementation for Data Types.

Provides list/get queries with:
- dependent release scope support
- base data type summary loading
- supplementary component expansion
- raw owner/created/updated user ids (service-layer AppUser enrichment)
"""


from __future__ import annotations

from collections import defaultdict
from datetime import datetime
from typing import Any, Literal

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import aliased

from app.repositories.contracts.data_type import DataTypeRepositoryContract
from app.repositories.models import (
    DataTypeBaseSummaryRow,
    DataTypeSupplementaryComponentRow,
    LibrarySummaryRow,
    LogSummaryRow,
    NamespaceSummaryRow,
    ReleaseSummaryRow,
)
from app.repositories.models.data_type import DataTypeRow
from app.repositories.vendors.mariadb.models.data_type import Dt, DtManifest, DtSc, DtScManifest
from app.repositories.vendors.mariadb.models.library import Library
from app.repositories.vendors.mariadb.models.log import Log
from app.repositories.vendors.mariadb.models.namespace import Namespace
from app.repositories.vendors.mariadb.models.release import Release
from app.types.identifiers import (
    DataTypeManifestId,
    ReleaseId,
)


class MariaDbDataTypeRepository(DataTypeRepositoryContract):
    """MariaDB-backed repository for data type read operations."""

    def __init__(self, session: AsyncSession):
        """Initialize MariaDbDataTypeRepository.

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
        den: str | None = None,
        representation_term: str | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
    ) -> tuple[int, list[DataTypeRow]]:
        """Handle list.

        Args:
            release_id: Release identifier used to scope the query.
            dependent_release_ids: Value for `dependent_release_ids`.
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            den: Optional Dictionary Entry Name (DEN) filter.
            representation_term: Value for `representation_term`.
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
            den=den,
            representation_term=representation_term,
            creation_timestamp_before=creation_timestamp_before,
            creation_timestamp_after=creation_timestamp_after,
            last_update_timestamp_before=last_update_timestamp_before,
            last_update_timestamp_after=last_update_timestamp_after,
        )

        total_stmt = (
            select(func.count())
            .select_from(DtManifest)
            .join(Dt, Dt.dt_id == DtManifest.dt_id)
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

        sc_by_owner = await _scs_by_owner_manifest_ids(self._session, [int(row["dt_manifest_id"]) for row in rows])
        return total, [_to_data_type_row(row, sc_by_owner.get(row["dt_manifest_id"], [])) for row in rows]

    async def get(self, dt_manifest_id: DataTypeManifestId) -> DataTypeRow | None:
        """Handle get.

        Args:
            dt_manifest_id: Data type manifest identifier.

        Returns:
            Result of the operation.
        """
        row = (
            await self._session.execute(
                _build_base_query().where(DtManifest.dt_manifest_id == dt_manifest_id)
            )
        ).first()
        if row is None:
            return None

        dt_manifest_id_int = int(dt_manifest_id)
        sc_by_owner = await _scs_by_owner_manifest_ids(self._session, [dt_manifest_id_int])
        return _to_data_type_row(dict(row._mapping), sc_by_owner.get(dt_manifest_id_int, []))


def _build_base_query():
    base_manifest = aliased(DtManifest)
    base_dt = aliased(Dt)
    base_ns = aliased(Namespace)
    base_release = aliased(Release)
    base_library = aliased(Library)

    return (
        select(
            DtManifest.dt_manifest_id,
            DtManifest.dt_id,
            DtManifest.based_dt_manifest_id,
            DtManifest.den,
            Dt.guid,
            Dt.data_type_term,
            Dt.qualifier,
            Dt.representation_term,
            Dt.six_digit_id,
            Dt.definition,
            Dt.definition_source,
            Dt.content_component_definition,
            Dt.commonly_used,
            Dt.is_deprecated,
            Dt.state,
            Dt.creation_timestamp,
            Dt.last_update_timestamp,
            Dt.owner_user_id,
            Dt.created_by,
            Dt.last_updated_by,
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
            base_manifest.dt_manifest_id.label("base_dt_manifest_id"),
            base_manifest.dt_id.label("base_dt_id"),
            base_manifest.based_dt_manifest_id.label("base_based_dt_manifest_id"),
            base_manifest.den.label("base_den"),
            base_dt.guid.label("base_guid"),
            base_dt.data_type_term.label("base_data_type_term"),
            base_dt.qualifier.label("base_qualifier"),
            base_dt.representation_term.label("base_representation_term"),
            base_dt.six_digit_id.label("base_six_digit_id"),
            base_dt.definition.label("base_definition"),
            base_dt.definition_source.label("base_definition_source"),
            base_dt.content_component_definition.label("base_content_component_definition"),
            base_dt.is_deprecated.label("base_is_deprecated"),
            base_ns.namespace_id.label("base_namespace_id"),
            base_ns.prefix.label("base_namespace_prefix"),
            base_ns.uri.label("base_namespace_uri"),
            base_library.library_id.label("base_library_id"),
            base_library.name.label("base_library_name"),
            base_release.release_id.label("base_release_id"),
            base_release.release_num.label("base_release_num"),
            base_release.state.label("base_release_state"),
        )
        .select_from(DtManifest)
        .join(Dt, Dt.dt_id == DtManifest.dt_id)
        .join(Release, Release.release_id == DtManifest.release_id)
        .join(Library, Library.library_id == Release.library_id)
        .outerjoin(Namespace, Namespace.namespace_id == Dt.namespace_id)
        .outerjoin(Log, Log.log_id == DtManifest.log_id)
        .outerjoin(base_manifest, base_manifest.dt_manifest_id == DtManifest.based_dt_manifest_id)
        .outerjoin(base_dt, base_dt.dt_id == base_manifest.dt_id)
        .outerjoin(base_ns, base_ns.namespace_id == base_dt.namespace_id)
        .outerjoin(base_release, base_release.release_id == base_manifest.release_id)
        .outerjoin(base_library, base_library.library_id == base_release.library_id)
    )


def _build_where_clauses(
    release_id: ReleaseId,
    dependent_release_ids: list[ReleaseId],
    den: str | None,
    representation_term: str | None,
    creation_timestamp_before: datetime | None,
    creation_timestamp_after: datetime | None,
    last_update_timestamp_before: datetime | None,
    last_update_timestamp_after: datetime | None,
) -> list[object]:
    clauses: list[object] = [DtManifest.release_id.in_([release_id, *[x for x in dependent_release_ids]])]
    if den:
        clauses.append(DtManifest.den.ilike(f"%{den}%"))
    if representation_term:
        clauses.append(Dt.representation_term.ilike(f"%{representation_term}%"))
    if creation_timestamp_after is not None:
        clauses.append(Dt.creation_timestamp >= creation_timestamp_after)
    if creation_timestamp_before is not None:
        clauses.append(Dt.creation_timestamp <= creation_timestamp_before)
    if last_update_timestamp_after is not None:
        clauses.append(Dt.last_update_timestamp >= last_update_timestamp_after)
    if last_update_timestamp_before is not None:
        clauses.append(Dt.last_update_timestamp <= last_update_timestamp_before)
    return clauses


def _build_order_by(sorts: list[tuple[str, Literal["ASC", "DESC"]]] | None):
    if not sorts:
        return [Dt.creation_timestamp.desc()]

    sort_map = {
        "den": DtManifest.den,
        "data_type_term": Dt.data_type_term,
        "qualifier": Dt.qualifier,
        "representation_term": Dt.representation_term,
        "six_digit_id": Dt.six_digit_id,
        "definition": Dt.definition,
        "creation_timestamp": Dt.creation_timestamp,
        "last_update_timestamp": Dt.last_update_timestamp,
    }
    order_by = []
    for column, direction in sorts:
        col = sort_map.get(column)
        if col is not None:
            order_by.append(col.desc() if direction == "DESC" else col.asc())
    return order_by if order_by else [Dt.creation_timestamp.desc()]


async def _scs_by_owner_manifest_ids(
    session: AsyncSession,
    dt_manifest_ids: list[int],
) -> dict[int, list[DataTypeSupplementaryComponentRow]]:
    if not dt_manifest_ids:
        return {}

    sc_rows = [
        dict(row._mapping)
        for row in (
            await session.execute(
                select(
                    DtScManifest.owner_dt_manifest_id,
                    DtScManifest.dt_sc_manifest_id,
                    DtScManifest.dt_sc_id,
                    DtSc.guid,
                    DtSc.object_class_term,
                    DtSc.property_term,
                    DtSc.representation_term,
                    DtSc.definition,
                    DtSc.definition_source,
                    DtSc.cardinality_min,
                    DtSc.cardinality_max,
                    DtSc.default_value,
                    DtSc.fixed_value,
                    DtSc.is_deprecated,
                )
                .select_from(DtScManifest)
                .join(DtSc, DtSc.dt_sc_id == DtScManifest.dt_sc_id)
                .where(DtScManifest.owner_dt_manifest_id.in_(dt_manifest_ids))
                .order_by(DtScManifest.dt_sc_manifest_id.asc())
            )
        ).all()
    ]

    sc_by_owner: dict[int, list[DataTypeSupplementaryComponentRow]] = defaultdict(list)
    for sc_row in sc_rows:
        sc_by_owner[int(sc_row["owner_dt_manifest_id"])].append(
            DataTypeSupplementaryComponentRow(
                dt_sc_manifest_id=int(sc_row["dt_sc_manifest_id"]),
                dt_sc_id=int(sc_row["dt_sc_id"]),
                guid=str(sc_row["guid"]),
                object_class_term=(str(sc_row["object_class_term"]) if sc_row.get("object_class_term") is not None else None),
                property_term=str(sc_row["property_term"]) if sc_row.get("property_term") is not None else None,
                representation_term=(
                    str(sc_row["representation_term"]) if sc_row.get("representation_term") is not None else None
                ),
                definition=str(sc_row["definition"]) if sc_row.get("definition") is not None else None,
                definition_source=(str(sc_row["definition_source"]) if sc_row.get("definition_source") is not None else None),
                cardinality_min=int(sc_row["cardinality_min"]),
                cardinality_max=int(sc_row["cardinality_max"]) if sc_row.get("cardinality_max") is not None else None,
                default_value=str(sc_row["default_value"]) if sc_row.get("default_value") is not None else None,
                fixed_value=str(sc_row["fixed_value"]) if sc_row.get("fixed_value") is not None else None,
                is_deprecated=bool(sc_row["is_deprecated"]),
            )
        )
    return sc_by_owner


def _to_data_type_row(row: dict[str, Any], scs: list[DataTypeSupplementaryComponentRow]) -> DataTypeRow:
    base_dt = None
    if row.get("base_dt_manifest_id") is not None:
        base_dt = DataTypeBaseSummaryRow(
            dt_manifest_id=row["base_dt_manifest_id"],
            dt_id=row["base_dt_id"],
            based_dt_manifest_id=(
                row["base_based_dt_manifest_id"] if row.get("base_based_dt_manifest_id") is not None else None
            ),
            guid=str(row["base_guid"]),
            den=str(row["base_den"]),
            data_type_term=str(row["base_data_type_term"]) if row.get("base_data_type_term") is not None else None,
            qualifier=str(row["base_qualifier"]) if row.get("base_qualifier") is not None else None,
            representation_term=(
                str(row["base_representation_term"]) if row.get("base_representation_term") is not None else None
            ),
            six_digit_id=str(row["base_six_digit_id"]) if row.get("base_six_digit_id") is not None else None,
            definition=str(row["base_definition"]) if row.get("base_definition") is not None else None,
            definition_source=(
                str(row["base_definition_source"]) if row.get("base_definition_source") is not None else None
            ),
            content_component_definition=(
                str(row["base_content_component_definition"])
                if row.get("base_content_component_definition") is not None
                else None
            ),
            is_deprecated=bool(row.get("base_is_deprecated") or False),
            namespace=_namespace_summary(row, "base_"),
            library=_library_summary(row, "base_"),
            release=_release_summary(row, "base_"),
        )

    return DataTypeRow(
        dt_manifest_id=row["dt_manifest_id"],
        dt_id=row["dt_id"],
        base_dt=base_dt,
        guid=str(row["guid"]),
        den=str(row["den"]),
        data_type_term=str(row["data_type_term"]) if row.get("data_type_term") is not None else None,
        qualifier=str(row["qualifier"]) if row.get("qualifier") is not None else None,
        representation_term=str(row["representation_term"]) if row.get("representation_term") is not None else None,
        six_digit_id=str(row["six_digit_id"]) if row.get("six_digit_id") is not None else None,
        definition=str(row["definition"]) if row.get("definition") is not None else None,
        definition_source=str(row["definition_source"]) if row.get("definition_source") is not None else None,
        content_component_definition=(
            str(row["content_component_definition"]) if row.get("content_component_definition") is not None else None
        ),
        commonly_used=bool(row["commonly_used"]),
        is_deprecated=bool(row["is_deprecated"]),
        state=str(row["state"]) if row.get("state") is not None else None,
        supplementary_components=scs,
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


def _library_summary(row: dict[str, Any], prefix: str = "") -> LibrarySummaryRow:
    """Internal helper for library summary.

    Args:
        row: Repository row model to convert into a DTO.
        prefix: Optional namespace prefix filter.

    Returns:
        Result of the operation.
    """
    return LibrarySummaryRow(library_id=row[f"{prefix}library_id"], name=str(row[f"{prefix}library_name"]))


def _release_summary(row: dict[str, Any], prefix: str = "") -> ReleaseSummaryRow:
    """Internal helper for release summary.

    Args:
        row: Repository row model to convert into a DTO.
        prefix: Optional namespace prefix filter.

    Returns:
        Result of the operation.
    """
    return ReleaseSummaryRow(
        release_id=row[f"{prefix}release_id"],
        release_num=str(row.get(f"{prefix}release_num")) if row.get(f"{prefix}release_num") is not None else None,
        state=str(row[f"{prefix}release_state"]),
    )


def _namespace_summary(row: dict[str, Any], prefix: str = "") -> NamespaceSummaryRow | None:
    """Internal helper for namespace summary.

    Args:
        row: Repository row model to convert into a DTO.
        prefix: Optional namespace prefix filter.

    Returns:
        Result of the operation.
    """
    ns_id = row.get(f"{prefix}namespace_id")
    if ns_id is None:
        return None
    return NamespaceSummaryRow(
        namespace_id=ns_id,
        prefix=str(row.get(f"{prefix}namespace_prefix")) if row.get(f"{prefix}namespace_prefix") is not None else None,
        uri=str(row[f"{prefix}namespace_uri"]),
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
