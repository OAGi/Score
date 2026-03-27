"""MariaDB repository implementation for Core Components.

This repository follows the same model-first query style used by other
connectCenter repositories. It provides:
- Unified list queries across ACC, ASCCP, and BCCP.
- Type-specific detail queries for ACC/ASCCP/BCCP.
- User, namespace, release, library, and log summary projection in one pass.
- Optional filtering by DEN/tag/date ranges, with safe sort mapping.
"""


from __future__ import annotations

from datetime import datetime
from typing import Any, Literal

from sqlalchemy import Select, and_, func, literal, select, union_all
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.contracts.core_component import CoreComponentRepositoryContract
from app.repositories.models import (
    AccInfoRow,
    AsccRelationshipInfoRow,
    AsccpInfoRow,
    BaseAccInfoRow,
    BccRelationshipInfoRow,
    BccpInfoRow,
    DtSummaryRow,
    LibrarySummaryRow,
    LogSummaryRow,
    NamespaceSummaryRow,
    ReleaseSummaryRow,
    ValueConstraintRow,
)
from app.repositories.models.core_component import CoreComponentListRow, GetAccRow, GetAsccpRow, GetBccpRow
from app.repositories.vendors.mariadb.models.core_component import (
    Acc,
    AccManifest,
    Ascc,
    AsccManifest,
    Asccp,
    AsccpManifest,
    Bcc,
    BccManifest,
    Bccp,
    BccpManifest,
    SeqKey,
)
from app.repositories.vendors.mariadb.models.data_type import Dt, DtManifest
from app.repositories.vendors.mariadb.models.library import Library
from app.repositories.vendors.mariadb.models.log import Log
from app.repositories.vendors.mariadb.models.namespace import Namespace
from app.repositories.vendors.mariadb.models.release import Release
from app.repositories.vendors.mariadb.models.tag import (
    AccManifestTag,
    AsccpManifestTag,
    BccpManifestTag,
    Tag,
)
from app.types.identifiers import (
    AccManifestId,
    AsccpManifestId,
    BccpManifestId,
    ReleaseId,
)


class MariaDbCoreComponentRepository(CoreComponentRepositoryContract):
    """MariaDB-backed repository for core component list/get operations."""

    def __init__(self, session: AsyncSession):
        """Initialize MariaDbCoreComponentRepository.

        Args:
            session: Database session bound to the current request.
        """
        self._session = session

    async def list(
        self,
        release_id: ReleaseId,
        dependent_release_ids: list[ReleaseId],
        types: list[str],
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        den: str | None = None,
        tag: str | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
    ) -> tuple[int, list[CoreComponentListRow]]:
        """Return a unified paginated list for requested core component types.

        Args:
            release_id: Release identifier used to scope the query.
            dependent_release_ids: Value for `dependent_release_ids`.
            types: Optional component type filter list.
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            den: Optional Dictionary Entry Name (DEN) filter.
            tag: Optional tag filter.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

        Returns:
            Result of the operation.
        """
        release_ids = [release_id, *[x for x in dependent_release_ids]]
        union_queries: list[Select[Any]] = []

        if "ACC" in types:
            union_queries.append(
                self._build_component_select(
                    component_type="ACC",
                    manifest_model=AccManifest,
                    component_model=Acc,
                    manifest_id_col=AccManifest.acc_manifest_id,
                    manifest_release_col=AccManifest.release_id,
                    manifest_log_col=AccManifest.log_id,
                    manifest_component_id_col=AccManifest.acc_id,
                    component_id_col=Acc.acc_id,
                    component_namespace_col=Acc.namespace_id,
                    component_owner_col=Acc.owner_user_id,
                    component_created_by_col=Acc.created_by,
                    component_updated_by_col=Acc.last_updated_by,
                    guid_col=Acc.guid,
                    den_col=AccManifest.den,
                    name_col=Acc.object_class_term,
                    definition_col=Acc.definition,
                    definition_source_col=Acc.definition_source,
                    state_col=Acc.state,
                    is_deprecated_col=Acc.is_deprecated,
                    creation_ts_col=Acc.creation_timestamp,
                    update_ts_col=Acc.last_update_timestamp,
                    tag_link_model=AccManifestTag,
                    tag_link_manifest_col=AccManifestTag.acc_manifest_id,
                    release_ids=release_ids,
                    den=den,
                    tag=tag,
                    creation_timestamp_before=creation_timestamp_before,
            creation_timestamp_after=creation_timestamp_after,
                    last_update_timestamp_before=last_update_timestamp_before,
            last_update_timestamp_after=last_update_timestamp_after,
                )
            )

        if "ASCCP" in types:
            union_queries.append(
                self._build_component_select(
                    component_type="ASCCP",
                    manifest_model=AsccpManifest,
                    component_model=Asccp,
                    manifest_id_col=AsccpManifest.asccp_manifest_id,
                    manifest_release_col=AsccpManifest.release_id,
                    manifest_log_col=AsccpManifest.log_id,
                    manifest_component_id_col=AsccpManifest.asccp_id,
                    component_id_col=Asccp.asccp_id,
                    component_namespace_col=Asccp.namespace_id,
                    component_owner_col=Asccp.owner_user_id,
                    component_created_by_col=Asccp.created_by,
                    component_updated_by_col=Asccp.last_updated_by,
                    guid_col=Asccp.guid,
                    den_col=AsccpManifest.den,
                    name_col=Asccp.property_term,
                    definition_col=Asccp.definition,
                    definition_source_col=Asccp.definition_source,
                    state_col=Asccp.state,
                    is_deprecated_col=Asccp.is_deprecated,
                    creation_ts_col=Asccp.creation_timestamp,
                    update_ts_col=Asccp.last_update_timestamp,
                    tag_link_model=AsccpManifestTag,
                    tag_link_manifest_col=AsccpManifestTag.asccp_manifest_id,
                    release_ids=release_ids,
                    den=den,
                    tag=tag,
                    creation_timestamp_before=creation_timestamp_before,
            creation_timestamp_after=creation_timestamp_after,
                    last_update_timestamp_before=last_update_timestamp_before,
            last_update_timestamp_after=last_update_timestamp_after,
                )
            )

        if "BCCP" in types:
            union_queries.append(
                self._build_component_select(
                    component_type="BCCP",
                    manifest_model=BccpManifest,
                    component_model=Bccp,
                    manifest_id_col=BccpManifest.bccp_manifest_id,
                    manifest_release_col=BccpManifest.release_id,
                    manifest_log_col=BccpManifest.log_id,
                    manifest_component_id_col=BccpManifest.bccp_id,
                    component_id_col=Bccp.bccp_id,
                    component_namespace_col=Bccp.namespace_id,
                    component_owner_col=Bccp.owner_user_id,
                    component_created_by_col=Bccp.created_by,
                    component_updated_by_col=Bccp.last_updated_by,
                    guid_col=Bccp.guid,
                    den_col=BccpManifest.den,
                    name_col=Bccp.property_term,
                    definition_col=Bccp.definition,
                    definition_source_col=Bccp.definition_source,
                    state_col=Bccp.state,
                    is_deprecated_col=Bccp.is_deprecated,
                    creation_ts_col=Bccp.creation_timestamp,
                    update_ts_col=Bccp.last_update_timestamp,
                    tag_link_model=BccpManifestTag,
                    tag_link_manifest_col=BccpManifestTag.bccp_manifest_id,
                    release_ids=release_ids,
                    den=den,
                    tag=tag,
                    creation_timestamp_before=creation_timestamp_before,
            creation_timestamp_after=creation_timestamp_after,
                    last_update_timestamp_before=last_update_timestamp_before,
            last_update_timestamp_after=last_update_timestamp_after,
                )
            )

        if not union_queries:
            return 0, []

        query = union_queries[0] if len(union_queries) == 1 else union_all(*union_queries)
        sub = query.subquery("q")
        total_stmt = select(func.count()).select_from(sub)
        total = int((await self._session.execute(total_stmt)).scalar_one())

        sort_map = {
            "den": sub.c.den,
            "name": sub.c.name,
            "definition": sub.c.definition,
            "creation_timestamp": sub.c.creation_timestamp,
            "last_update_timestamp": sub.c.last_update_timestamp,
        }
        order_by = []
        for sort in sorts:
            col = sort_map.get(sort[0])
            if col is not None:
                order_by.append(col.desc() if sort[1] == "DESC" else col.asc())
        if not order_by:
            order_by = [sub.c.creation_timestamp.desc()]

        rows = [
            dict(row._mapping)
            for row in (
                await self._session.execute(
                    select(sub).order_by(*order_by).limit(limit).offset(offset)
                )
            ).all()
        ]
        items = [self._to_core_component_raw(row) for row in rows]
        return total, items

    async def get_acc(self, acc_manifest_id: AccManifestId) -> GetAccRow | None:
        """Get ACC details using score-mcp-server compatible response shape.

        Args:
            acc_manifest_id: ACC manifest identifier.

        Returns:
            Result of the operation.
        """
        core = await self._fetch_one_core("ACC", acc_manifest_id)
        if core is None:
            return None

        extra = (
            await self._session.execute(
                select(
                    AccManifest.based_acc_manifest_id,
                    Acc.object_class_term,
                    Acc.object_class_qualifier,
                    Acc.oagis_component_type,
                    Acc.is_abstract,
                    Acc.type,
                )
                .select_from(AccManifest)
                .join(Acc, Acc.acc_id == AccManifest.acc_id)
                .where(AccManifest.acc_manifest_id == acc_manifest_id)
            )
        ).first()
        if extra is None:
            return None

        base_acc: BaseAccInfoRow | None = None
        based_acc_manifest_id = int(extra[0]) if extra[0] is not None else None
        if based_acc_manifest_id is not None:
            base_row = (
                await self._session.execute(
                    select(
                        AccManifest.acc_manifest_id,
                        Acc.acc_id,
                        Acc.guid,
                        AccManifest.den,
                        Acc.object_class_term,
                        Acc.type,
                        Acc.definition,
                        Acc.definition_source,
                        Namespace.namespace_id,
                        Namespace.prefix.label("namespace_prefix"),
                        Namespace.uri.label("namespace_uri"),
                        Library.library_id,
                        Library.name.label("library_name"),
                        Release.release_id,
                        Release.release_num,
                        Release.state.label("release_state"),
                    )
                    .select_from(AccManifest)
                    .join(Acc, Acc.acc_id == AccManifest.acc_id)
                    .join(Release, Release.release_id == AccManifest.release_id)
                    .join(Library, Library.library_id == Release.library_id)
                    .outerjoin(Namespace, Namespace.namespace_id == Acc.namespace_id)
                    .where(AccManifest.acc_manifest_id == based_acc_manifest_id)
                )
            ).first()
            if base_row is not None:
                br = dict(base_row._mapping)
                base_acc = BaseAccInfoRow(
                    acc_manifest_id=int(br["acc_manifest_id"]),
                    acc_id=int(br["acc_id"]),
                    guid=str(br["guid"]),
                    den=str(br["den"]),
                    object_class_term=str(br["object_class_term"]),
                    type=str(br["type"]) if br.get("type") is not None else None,
                    definition=str(br["definition"]) if br.get("definition") is not None else None,
                    definition_source=str(br["definition_source"]) if br.get("definition_source") is not None else None,
                    namespace=_namespace_summary(br),
                    library=_library_summary(br),
                    release=_release_summary(br),
                )

        relationships = await self.get_acc_relationships(acc_manifest_id)

        return GetAccRow(
            acc_manifest_id=acc_manifest_id,
            acc_id=core.component_id,
            base_acc=base_acc,
            relationships=relationships,
            guid=core.guid,
            den=core.den,
            object_class_term=str(extra[1]) if extra[1] is not None else (core.name or ""),
            definition=core.definition,
            definition_source=core.definition_source,
            object_class_qualifier=str(extra[2]) if extra[2] is not None else None,
            component_type=int(extra[3]) if extra[3] is not None else None,
            is_abstract=bool(extra[4]),
            is_deprecated=core.is_deprecated,
            state=core.state,
            namespace=core.namespace,
            library=core.library,
            release=core.release,
            log=core.log,
            owner_user_id=core.owner_user_id,
            created_by=core.created_by,
            creation_timestamp=core.creation_timestamp,
            last_updated_by=core.last_updated_by,
            last_update_timestamp=core.last_update_timestamp,
        )

    async def get_acc_relationships(
        self,
        acc_manifest_id: AccManifestId,
    ) -> list[AsccRelationshipInfoRow | BccRelationshipInfoRow]:
        """Get ACC relationships in canonical `seq_key` order."""
        core = await self._fetch_one_core("ACC", acc_manifest_id)
        if core is None:
            return []

        from_acc = AccInfoRow(
            acc_manifest_id=acc_manifest_id,
            acc_id=core.component_id,
            guid=core.guid,
            den=core.den,
            object_class_term=core.name or "",
            definition=core.definition,
            definition_source=core.definition_source,
            is_deprecated=core.is_deprecated,
        )

        # `seq_key` defines canonical ACC relationship order as a linked list.
        ordered_seq_rows = await self._load_ordered_seq_rows(acc_manifest_id)
        ascc_rows_by_manifest_id, bcc_rows_by_manifest_id = await self._load_acc_relationship_rows(ordered_seq_rows)
        return self._build_acc_relationships(
            ordered_seq_rows=ordered_seq_rows,
            ascc_rows_by_manifest_id=ascc_rows_by_manifest_id,
            bcc_rows_by_manifest_id=bcc_rows_by_manifest_id,
            from_acc=from_acc,
        )

    async def _load_ordered_seq_rows(self, from_acc_manifest_id: AccManifestId) -> list[dict[str, Any]]:
        """Load `seq_key` rows for an ACC and return them in linked-list order."""
        seq_rows = [
            dict(row._mapping)
            for row in (
                await self._session.execute(
                    select(
                        SeqKey.seq_key_id,
                        SeqKey.ascc_manifest_id,
                        SeqKey.bcc_manifest_id,
                        SeqKey.prev_seq_key_id,
                        SeqKey.next_seq_key_id,
                    ).where(SeqKey.from_acc_manifest_id == from_acc_manifest_id)
                )
            ).all()
        ]
        if not seq_rows:
            return []

        # Traverse the explicit prev/next chain and guard against cyclic data.
        seq_by_id = {int(row["seq_key_id"]): row for row in seq_rows}
        head_candidates = [row for row in seq_rows if row.get("prev_seq_key_id") is None]
        current = (
            min(head_candidates, key=lambda row: int(row["seq_key_id"]))
            if head_candidates
            else min(seq_rows, key=lambda row: int(row["seq_key_id"]))
        )

        ordered_seq_rows: list[dict[str, Any]] = []
        visited_seq_ids: set[int] = set()
        while current is not None:
            seq_key_id = int(current["seq_key_id"])
            if seq_key_id in visited_seq_ids:
                break
            visited_seq_ids.add(seq_key_id)
            ordered_seq_rows.append(current)
            next_seq_key_id = current.get("next_seq_key_id")
            if next_seq_key_id is None:
                break
            current = seq_by_id.get(int(next_seq_key_id))
        return ordered_seq_rows

    async def _load_acc_relationship_rows(
        self,
        ordered_seq_rows: list[dict[str, Any]],
    ) -> tuple[dict[int, dict[str, Any]], dict[int, dict[str, Any]]]:
        """Load ASCC/BCC detail rows needed to materialize ACC relationships."""
        ascc_manifest_ids = [
            int(row["ascc_manifest_id"])
            for row in ordered_seq_rows
            if row.get("ascc_manifest_id") is not None
        ]
        bcc_manifest_ids = [
            int(row["bcc_manifest_id"])
            for row in ordered_seq_rows
            if row.get("bcc_manifest_id") is not None
        ]
        ascc_rows = await self._load_ascc_rows_by_manifest_id(ascc_manifest_ids)
        bcc_rows = await self._load_bcc_rows_by_manifest_id(bcc_manifest_ids)
        return ascc_rows, bcc_rows

    async def _load_ascc_rows_by_manifest_id(self, ascc_manifest_ids: list[int]) -> dict[int, dict[str, Any]]:
        """Load ASCC relationship rows keyed by ASCC manifest ID."""
        if not ascc_manifest_ids:
            return {}
        rows = [
            dict(result_row._mapping)
            for result_row in (
                await self._session.execute(
                    select(
                        AsccManifest.ascc_manifest_id,
                        Ascc.ascc_id,
                        Ascc.guid,
                        AsccManifest.den,
                        Ascc.cardinality_min,
                        Ascc.cardinality_max,
                        Ascc.is_deprecated,
                        Ascc.definition,
                        Ascc.definition_source,
                        AsccpManifest.asccp_manifest_id,
                        AsccpManifest.role_of_acc_manifest_id,
                        Asccp.asccp_id,
                        Asccp.guid.label("asccp_guid"),
                        AsccpManifest.den.label("asccp_den"),
                        Asccp.property_term,
                        Asccp.definition.label("asccp_definition"),
                        Asccp.definition_source.label("asccp_definition_source"),
                        Asccp.is_deprecated.label("asccp_is_deprecated"),
                    )
                    .select_from(AsccManifest)
                    .join(Ascc, Ascc.ascc_id == AsccManifest.ascc_id)
                    .join(AsccpManifest, AsccpManifest.asccp_manifest_id == AsccManifest.to_asccp_manifest_id)
                    .join(Asccp, Asccp.asccp_id == AsccpManifest.asccp_id)
                    .where(AsccManifest.ascc_manifest_id.in_(ascc_manifest_ids))
                )
            ).all()
        ]
        return {int(row["ascc_manifest_id"]): row for row in rows}

    async def _load_bcc_rows_by_manifest_id(self, bcc_manifest_ids: list[int]) -> dict[int, dict[str, Any]]:
        """Load BCC relationship rows keyed by BCC manifest ID."""
        if not bcc_manifest_ids:
            return {}
        rows = [
            dict(result_row._mapping)
            for result_row in (
                await self._session.execute(
                    select(
                        BccManifest.bcc_manifest_id,
                        Bcc.bcc_id,
                        Bcc.guid,
                        BccManifest.den,
                        Bcc.cardinality_min,
                        Bcc.cardinality_max,
                        Bcc.entity_type,
                        Bcc.is_nillable,
                        Bcc.default_value,
                        Bcc.fixed_value,
                        Bcc.is_deprecated,
                        Bcc.definition,
                        Bcc.definition_source,
                        BccpManifest.bccp_manifest_id,
                        Bccp.bccp_id,
                        Bccp.guid.label("bccp_guid"),
                        BccpManifest.den.label("bccp_den"),
                        Bccp.property_term,
                        Bccp.representation_term,
                        Bccp.definition.label("bccp_definition"),
                        Bccp.definition_source.label("bccp_definition_source"),
                        Bccp.is_deprecated.label("bccp_is_deprecated"),
                        DtManifest.dt_manifest_id,
                        DtManifest.dt_id,
                        DtManifest.based_dt_manifest_id,
                        DtManifest.den.label("dt_den"),
                        Dt.guid.label("dt_guid"),
                        Dt.data_type_term,
                        Dt.qualifier,
                        Dt.representation_term.label("dt_representation_term"),
                        Dt.six_digit_id,
                        Dt.definition.label("dt_definition"),
                        Dt.definition_source.label("dt_definition_source"),
                        Dt.content_component_definition,
                        Dt.is_deprecated.label("dt_is_deprecated"),
                        Namespace.namespace_id.label("dt_namespace_id"),
                        Namespace.prefix.label("dt_namespace_prefix"),
                        Namespace.uri.label("dt_namespace_uri"),
                        Library.library_id.label("dt_library_id"),
                        Library.name.label("dt_library_name"),
                        Release.release_id.label("dt_release_id"),
                        Release.release_num.label("dt_release_num"),
                        Release.state.label("dt_release_state"),
                    )
                    .select_from(BccManifest)
                    .join(Bcc, Bcc.bcc_id == BccManifest.bcc_id)
                    .join(BccpManifest, BccpManifest.bccp_manifest_id == BccManifest.to_bccp_manifest_id)
                    .join(Bccp, Bccp.bccp_id == BccpManifest.bccp_id)
                    .join(DtManifest, DtManifest.dt_manifest_id == BccpManifest.bdt_manifest_id)
                    .join(Dt, Dt.dt_id == DtManifest.dt_id)
                    .join(Release, Release.release_id == DtManifest.release_id)
                    .join(Library, Library.library_id == Release.library_id)
                    .outerjoin(Namespace, Namespace.namespace_id == Dt.namespace_id)
                    .where(BccManifest.bcc_manifest_id.in_(bcc_manifest_ids))
                )
            ).all()
        ]
        return {int(row["bcc_manifest_id"]): row for row in rows}

    def _build_acc_relationships(
        self,
        ordered_seq_rows: list[dict[str, Any]],
        ascc_rows_by_manifest_id: dict[int, dict[str, Any]],
        bcc_rows_by_manifest_id: dict[int, dict[str, Any]],
        from_acc: AccInfoRow,
    ) -> list[AsccRelationshipInfoRow | BccRelationshipInfoRow]:
        """Build ACC relationship DTOs in `seq_key` order."""
        relationships: list[AsccRelationshipInfoRow | BccRelationshipInfoRow] = []
        for seq_row in ordered_seq_rows:
            ascc_manifest_id = seq_row.get("ascc_manifest_id")
            if ascc_manifest_id is not None:
                row = ascc_rows_by_manifest_id.get(int(ascc_manifest_id))
                if row is not None:
                    relationships.append(self._to_ascc_relationship(row, from_acc))
                continue
            bcc_manifest_id = seq_row.get("bcc_manifest_id")
            if bcc_manifest_id is None:
                continue
            row = bcc_rows_by_manifest_id.get(int(bcc_manifest_id))
            if row is not None:
                relationships.append(self._to_bcc_relationship(row, from_acc))
        return relationships

    def _to_ascc_relationship(self, row: dict[str, Any], from_acc: AccInfoRow) -> AsccRelationshipInfoRow:
        """Convert one ASCC row into relationship DTO."""
        return AsccRelationshipInfoRow(
            ascc_manifest_id=row["ascc_manifest_id"],
            ascc_id=row["ascc_id"],
            guid=str(row["guid"]),
            den=str(row["den"]),
            cardinality_min=row["cardinality_min"],
            cardinality_max=row["cardinality_max"],
            is_deprecated=bool(row["is_deprecated"]),
            definition=str(row["definition"]) if row.get("definition") is not None else None,
            definition_source=str(row["definition_source"]) if row.get("definition_source") is not None else None,
            from_acc=from_acc,
            to_asccp=AsccpInfoRow(
                asccp_manifest_id=row["asccp_manifest_id"],
                asccp_id=row["asccp_id"],
                role_of_acc_manifest_id=row["role_of_acc_manifest_id"],
                guid=str(row["asccp_guid"]),
                den=str(row["asccp_den"]),
                property_term=str(row["property_term"]),
                definition=str(row["asccp_definition"]) if row.get("asccp_definition") is not None else None,
                definition_source=(
                    str(row["asccp_definition_source"]) if row.get("asccp_definition_source") is not None else None
                ),
                is_deprecated=bool(row["asccp_is_deprecated"]),
            ),
        )

    def _to_bcc_relationship(self, row: dict[str, Any], from_acc: AccInfoRow) -> BccRelationshipInfoRow:
        """Convert one BCC row into relationship DTO."""
        return BccRelationshipInfoRow(
            bcc_manifest_id=row["bcc_manifest_id"],
            bcc_id=row["bcc_id"],
            guid=str(row["guid"]),
            den=str(row["den"]),
            cardinality_min=row["cardinality_min"],
            cardinality_max=row["cardinality_max"] if row.get("cardinality_max") is not None else -1,
            entity_type="Attribute" if row["entity_type"] == 0 else "Element" if row["entity_type"] == 1 else None,
            is_nillable=bool(row["is_nillable"]),
            value_constraint=self._build_value_constraint(row),
            is_deprecated=bool(row["is_deprecated"]),
            definition=str(row["definition"]) if row.get("definition") is not None else None,
            definition_source=str(row["definition_source"]) if row.get("definition_source") is not None else None,
            from_acc=from_acc,
            to_bccp=BccpInfoRow(
                bccp_manifest_id=row["bccp_manifest_id"],
                bccp_id=row["bccp_id"],
                guid=str(row["bccp_guid"]),
                den=str(row["bccp_den"]),
                property_term=str(row["property_term"]),
                representation_term=str(row["representation_term"]),
                definition=str(row["bccp_definition"]) if row.get("bccp_definition") is not None else None,
                definition_source=(
                    str(row["bccp_definition_source"]) if row.get("bccp_definition_source") is not None else None
                ),
                is_deprecated=bool(row["bccp_is_deprecated"]),
                bdt_manifest=self._to_bdt_summary_from_bcc_row(row),
            ),
        )

    def _build_value_constraint(self, row: dict[str, Any]) -> ValueConstraintRow | None:
        """Create a value-constraint DTO when BCC default/fixed values are present."""
        if row.get("default_value") is None and row.get("fixed_value") is None:
            return None
        return ValueConstraintRow(
            default_value=str(row["default_value"]) if row.get("default_value") is not None else None,
            fixed_value=str(row["fixed_value"]) if row.get("fixed_value") is not None else None,
        )

    def _to_bdt_summary_from_bcc_row(self, row: dict[str, Any]) -> DtSummaryRow:
        """Convert BCC query columns into BDT summary DTO."""
        return DtSummaryRow(
            dt_manifest_id=row["dt_manifest_id"],
            dt_id=row["dt_id"],
            based_dt_manifest_id=row["based_dt_manifest_id"] if row.get("based_dt_manifest_id") is not None else None,
            guid=str(row["dt_guid"]),
            den=str(row["dt_den"]),
            data_type_term=str(row["data_type_term"]) if row.get("data_type_term") is not None else None,
            qualifier=str(row["qualifier"]) if row.get("qualifier") is not None else None,
            representation_term=str(row["dt_representation_term"]) if row.get("dt_representation_term") is not None else None,
            six_digit_id=str(row["six_digit_id"]) if row.get("six_digit_id") is not None else None,
            definition=str(row["dt_definition"]) if row.get("dt_definition") is not None else None,
            definition_source=str(row["dt_definition_source"]) if row.get("dt_definition_source") is not None else None,
            content_component_definition=(
                str(row["content_component_definition"]) if row.get("content_component_definition") is not None else None
            ),
            is_deprecated=bool(row["dt_is_deprecated"]),
            namespace=_namespace_summary(
                {
                    "namespace_id": row.get("dt_namespace_id"),
                    "namespace_prefix": row.get("dt_namespace_prefix"),
                    "namespace_uri": row.get("dt_namespace_uri"),
                }
            ),
            library=_library_summary({"library_id": row["dt_library_id"], "library_name": row["dt_library_name"]}),
            release=_release_summary(
                {
                    "release_id": row["dt_release_id"],
                    "release_num": row.get("dt_release_num"),
                    "release_state": row["dt_release_state"],
                }
            ),
        )

    async def get_asccp(self, asccp_manifest_id: AsccpManifestId) -> GetAsccpRow | None:
        """Get ASCCP details using score-mcp-server compatible response shape.

        Args:
            asccp_manifest_id: ASCCP manifest identifier.

        Returns:
            Result of the operation.
        """
        core = await self._fetch_one_core("ASCCP", asccp_manifest_id)
        if core is None:
            return None

        row = (
            await self._session.execute(
                select(
                    AsccpManifest.role_of_acc_manifest_id,
                    Asccp.property_term,
                    Asccp.reusable_indicator,
                    Asccp.is_nillable,
                )
                .select_from(AsccpManifest)
                .join(Asccp, Asccp.asccp_id == AsccpManifest.asccp_id)
                .where(AsccpManifest.asccp_manifest_id == asccp_manifest_id)
            )
        ).first()
        if row is None:
            return None

        role_of_acc = None
        if row[0] is not None:
            role_row = (
                await self._session.execute(
                    select(
                        AccManifest.acc_manifest_id,
                        Acc.acc_id,
                        Acc.guid,
                        AccManifest.den,
                        Acc.object_class_term,
                        Acc.type,
                        Acc.definition,
                        Acc.definition_source,
                        Namespace.namespace_id,
                        Namespace.prefix.label("namespace_prefix"),
                        Namespace.uri.label("namespace_uri"),
                        Library.library_id,
                        Library.name.label("library_name"),
                        Release.release_id,
                        Release.release_num,
                        Release.state.label("release_state"),
                    )
                    .select_from(AccManifest)
                    .join(Acc, Acc.acc_id == AccManifest.acc_id)
                    .join(Release, Release.release_id == AccManifest.release_id)
                    .join(Library, Library.library_id == Release.library_id)
                    .outerjoin(Namespace, Namespace.namespace_id == Acc.namespace_id)
                    .where(AccManifest.acc_manifest_id == row[0])
                )
            ).first()
            if role_row is not None:
                rr = dict(role_row._mapping)
                role_of_acc = BaseAccInfoRow(
                    acc_manifest_id=int(rr["acc_manifest_id"]),
                    acc_id=int(rr["acc_id"]),
                    guid=str(rr["guid"]),
                    den=str(rr["den"]),
                    object_class_term=str(rr["object_class_term"]),
                    type=str(rr["type"]) if rr.get("type") is not None else None,
                    definition=str(rr["definition"]) if rr.get("definition") is not None else None,
                    definition_source=str(rr["definition_source"]) if rr.get("definition_source") is not None else None,
                    namespace=_namespace_summary(rr),
                    library=_library_summary(rr),
                    release=_release_summary(rr),
                )

        return GetAsccpRow(
            asccp_manifest_id=asccp_manifest_id,
            asccp_id=core.component_id,
            role_of_acc=role_of_acc,
            guid=core.guid,
            den=core.den,
            property_term=str(row[1]) if row[1] is not None else None,
            definition=core.definition,
            definition_source=core.definition_source,
            reusable_indicator=bool(row[2]),
            is_nillable=bool(row[3]) if row[3] is not None else None,
            is_deprecated=core.is_deprecated,
            state=core.state,
            namespace=core.namespace,
            library=core.library,
            release=core.release,
            log=core.log,
            owner_user_id=core.owner_user_id,
            created_by=core.created_by,
            creation_timestamp=core.creation_timestamp,
            last_updated_by=core.last_updated_by,
            last_update_timestamp=core.last_update_timestamp,
        )

    async def get_bccp(self, bccp_manifest_id: BccpManifestId) -> GetBccpRow | None:
        """Get BCCP details using score-mcp-server compatible response shape.

        Args:
            bccp_manifest_id: BCCP manifest identifier.

        Returns:
            Result of the operation.
        """
        core = await self._fetch_one_core("BCCP", bccp_manifest_id)
        if core is None:
            return None

        row = (
            await self._session.execute(
                select(
                    BccpManifest.bdt_manifest_id,
                    Bccp.property_term,
                    Bccp.representation_term,
                    Bccp.is_nillable,
                    Bccp.default_value,
                    Bccp.fixed_value,
                    DtManifest.dt_manifest_id,
                    DtManifest.dt_id,
                    DtManifest.based_dt_manifest_id,
                    DtManifest.den.label("dt_den"),
                    Dt.guid.label("dt_guid"),
                    Dt.data_type_term,
                    Dt.qualifier,
                    Dt.representation_term.label("dt_representation_term"),
                    Dt.six_digit_id,
                    Dt.definition.label("dt_definition"),
                    Dt.definition_source.label("dt_definition_source"),
                    Dt.content_component_definition,
                    Dt.is_deprecated.label("dt_is_deprecated"),
                    Namespace.namespace_id.label("dt_namespace_id"),
                    Namespace.prefix.label("dt_namespace_prefix"),
                    Namespace.uri.label("dt_namespace_uri"),
                    Library.library_id.label("dt_library_id"),
                    Library.name.label("dt_library_name"),
                    Release.release_id.label("dt_release_id"),
                    Release.release_num.label("dt_release_num"),
                    Release.state.label("dt_release_state"),
                )
                .select_from(BccpManifest)
                .join(Bccp, Bccp.bccp_id == BccpManifest.bccp_id)
                .join(DtManifest, DtManifest.dt_manifest_id == BccpManifest.bdt_manifest_id)
                .join(Dt, Dt.dt_id == DtManifest.dt_id)
                .join(Release, Release.release_id == DtManifest.release_id)
                .join(Library, Library.library_id == Release.library_id)
                .outerjoin(Namespace, Namespace.namespace_id == Dt.namespace_id)
                .where(BccpManifest.bccp_manifest_id == bccp_manifest_id)
            )
        ).first()
        if row is None:
            return None

        value_constraint = None
        if row[4] is not None or row[5] is not None:
            value_constraint = ValueConstraintRow(
                default_value=str(row[4]) if row[4] is not None else None,
                fixed_value=str(row[5]) if row[5] is not None else None,
            )

        bdt = DtSummaryRow(
            dt_manifest_id=row[6],
            dt_id=row[7],
            based_dt_manifest_id=row[8] if row[8] is not None else None,
            guid=str(row[10]),
            den=str(row[9]),
            data_type_term=str(row[11]) if row[11] is not None else None,
            qualifier=str(row[12]) if row[12] is not None else None,
            representation_term=str(row[13]) if row[13] is not None else None,
            six_digit_id=str(row[14]) if row[14] is not None else None,
            definition=str(row[15]) if row[15] is not None else None,
            definition_source=str(row[16]) if row[16] is not None else None,
            content_component_definition=str(row[17]) if row[17] is not None else None,
            is_deprecated=bool(row[18]),
            namespace=_namespace_summary(
                {"namespace_id": row[19], "namespace_prefix": row[20], "namespace_uri": row[21]}
            ),
            library=_library_summary({"library_id": row[22], "library_name": row[23]}),
            release=_release_summary({"release_id": row[24], "release_num": row[25], "release_state": row[26]}),
        )

        return GetBccpRow(
            bccp_manifest_id=bccp_manifest_id,
            bccp_id=core.component_id,
            bdt=bdt,
            guid=core.guid,
            den=core.den,
            property_term=str(row[1]),
            representation_term=str(row[2]),
            definition=core.definition,
            definition_source=core.definition_source,
            is_nillable=bool(row[3]),
            value_constraint=value_constraint,
            is_deprecated=core.is_deprecated,
            state=core.state,
            namespace=core.namespace,
            library=core.library,
            release=core.release,
            log=core.log,
            owner_user_id=core.owner_user_id,
            created_by=core.created_by,
            creation_timestamp=core.creation_timestamp,
            last_updated_by=core.last_updated_by,
            last_update_timestamp=core.last_update_timestamp,
        )

    async def _fetch_one_core(self, component_type: str, manifest_id: int) -> CoreComponentListRow | None:
        """Resolve a single component using a direct manifest-id query.

        Args:
            component_type: Value for `component_type`.
            manifest_id: Value for `manifest_id`.

        Returns:
            Result of the operation.
        """
        if component_type == "ACC":
            base_query = self._build_component_select(
                component_type="ACC",
                manifest_model=AccManifest,
                component_model=Acc,
                manifest_id_col=AccManifest.acc_manifest_id,
                manifest_release_col=AccManifest.release_id,
                manifest_log_col=AccManifest.log_id,
                manifest_component_id_col=AccManifest.acc_id,
                component_id_col=Acc.acc_id,
                component_namespace_col=Acc.namespace_id,
                component_owner_col=Acc.owner_user_id,
                component_created_by_col=Acc.created_by,
                component_updated_by_col=Acc.last_updated_by,
                guid_col=Acc.guid,
                den_col=AccManifest.den,
                name_col=Acc.object_class_term,
                definition_col=Acc.definition,
                definition_source_col=Acc.definition_source,
                state_col=Acc.state,
                is_deprecated_col=Acc.is_deprecated,
                creation_ts_col=Acc.creation_timestamp,
                update_ts_col=Acc.last_update_timestamp,
                tag_link_model=AccManifestTag,
                tag_link_manifest_col=AccManifestTag.acc_manifest_id,
                release_ids=None,
                den=None,
                tag=None,
                creation_timestamp_before=None,
                creation_timestamp_after=None,
                last_update_timestamp_before=None,
                last_update_timestamp_after=None,
            )
        elif component_type == "ASCCP":
            base_query = self._build_component_select(
                component_type="ASCCP",
                manifest_model=AsccpManifest,
                component_model=Asccp,
                manifest_id_col=AsccpManifest.asccp_manifest_id,
                manifest_release_col=AsccpManifest.release_id,
                manifest_log_col=AsccpManifest.log_id,
                manifest_component_id_col=AsccpManifest.asccp_id,
                component_id_col=Asccp.asccp_id,
                component_namespace_col=Asccp.namespace_id,
                component_owner_col=Asccp.owner_user_id,
                component_created_by_col=Asccp.created_by,
                component_updated_by_col=Asccp.last_updated_by,
                guid_col=Asccp.guid,
                den_col=AsccpManifest.den,
                name_col=Asccp.property_term,
                definition_col=Asccp.definition,
                definition_source_col=Asccp.definition_source,
                state_col=Asccp.state,
                is_deprecated_col=Asccp.is_deprecated,
                creation_ts_col=Asccp.creation_timestamp,
                update_ts_col=Asccp.last_update_timestamp,
                tag_link_model=AsccpManifestTag,
                tag_link_manifest_col=AsccpManifestTag.asccp_manifest_id,
                release_ids=None,
                den=None,
                tag=None,
                creation_timestamp_before=None,
                creation_timestamp_after=None,
                last_update_timestamp_before=None,
                last_update_timestamp_after=None,
            )
        else:
            base_query = self._build_component_select(
                component_type="BCCP",
                manifest_model=BccpManifest,
                component_model=Bccp,
                manifest_id_col=BccpManifest.bccp_manifest_id,
                manifest_release_col=BccpManifest.release_id,
                manifest_log_col=BccpManifest.log_id,
                manifest_component_id_col=BccpManifest.bccp_id,
                component_id_col=Bccp.bccp_id,
                component_namespace_col=Bccp.namespace_id,
                component_owner_col=Bccp.owner_user_id,
                component_created_by_col=Bccp.created_by,
                component_updated_by_col=Bccp.last_updated_by,
                guid_col=Bccp.guid,
                den_col=BccpManifest.den,
                name_col=Bccp.property_term,
                definition_col=Bccp.definition,
                definition_source_col=Bccp.definition_source,
                state_col=Bccp.state,
                is_deprecated_col=Bccp.is_deprecated,
                creation_ts_col=Bccp.creation_timestamp,
                update_ts_col=Bccp.last_update_timestamp,
                tag_link_model=BccpManifestTag,
                tag_link_manifest_col=BccpManifestTag.bccp_manifest_id,
                release_ids=None,
                den=None,
                tag=None,
                creation_timestamp_before=None,
                creation_timestamp_after=None,
                last_update_timestamp_before=None,
                last_update_timestamp_after=None,
            )

        sub = base_query.subquery("one_core")
        row = (
            await self._session.execute(
                select(sub).where(sub.c.manifest_id == manifest_id)
            )
        ).first()
        if row is None:
            return None
        return self._to_core_component_raw(dict(row._mapping))

    def _build_component_select(
        self,
        component_type: str,
        manifest_model: Any,
        component_model: Any,
        manifest_id_col: Any,
        manifest_release_col: Any,
        manifest_log_col: Any,
        manifest_component_id_col: Any,
        component_id_col: Any,
        component_namespace_col: Any,
        component_owner_col: Any,
        component_created_by_col: Any,
        component_updated_by_col: Any,
        guid_col: Any,
        den_col: Any,
        name_col: Any,
        definition_col: Any,
        definition_source_col: Any,
        state_col: Any,
        is_deprecated_col: Any,
        creation_ts_col: Any,
        update_ts_col: Any,
        tag_link_model: Any,
        tag_link_manifest_col: Any,
        release_ids: list[int] | None,
        den: str | None,
        tag: str | None,
        creation_timestamp_before: datetime | None,
    creation_timestamp_after: datetime | None,
        last_update_timestamp_before: datetime | None,
    last_update_timestamp_after: datetime | None,
    ) -> Select[Any]:
        # Intentionally exclude AppUser joins here.
        # User resolution is handled in the service layer via batched `gets(...)`
        # so AppUser data can be cached and reused across list/detail calls.
        """Build a typed component select for list/detail core fields without user joins.

        Args:
            component_type: Value for `component_type`.
            manifest_model: Value for `manifest_model`.
            component_model: Value for `component_model`.
            manifest_id_col: Value for `manifest_id_col`.
            manifest_release_col: Value for `manifest_release_col`.
            manifest_log_col: Value for `manifest_log_col`.
            manifest_component_id_col: Value for `manifest_component_id_col`.
            component_id_col: Value for `component_id_col`.
            component_namespace_col: Value for `component_namespace_col`.
            component_owner_col: Value for `component_owner_col`.
            component_created_by_col: Value for `component_created_by_col`.
            component_updated_by_col: Value for `component_updated_by_col`.
            guid_col: Value for `guid_col`.
            den_col: Value for `den_col`.
            name_col: Value for `name_col`.
            definition_col: Value for `definition_col`.
            definition_source_col: Value for `definition_source_col`.
            state_col: Value for `state_col`.
            is_deprecated_col: Value for `is_deprecated_col`.
            creation_ts_col: Value for `creation_ts_col`.
            update_ts_col: Value for `update_ts_col`.
            tag_link_model: Value for `tag_link_model`.
            tag_link_manifest_col: Value for `tag_link_manifest_col`.
            release_ids: Release identifiers used to scope the query.
            den: Optional Dictionary Entry Name (DEN) filter.
            tag: Optional tag filter.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

        Returns:
            Result of the operation.
        """
        where_clauses = []
        if release_ids is not None:
            where_clauses.append(manifest_release_col.in_(release_ids))
        if den:
            for word in [w.strip() for w in den.split() if w.strip()]:
                where_clauses.append(func.lower(den_col).like(func.lower(f"%{word}%")))
        if tag:
            where_clauses.append(func.lower(Tag.name).like(func.lower(f"%{tag}%")))
        if creation_timestamp_after is not None:
            where_clauses.append(creation_ts_col >= creation_timestamp_after)
        if creation_timestamp_before is not None:
            where_clauses.append(creation_ts_col <= creation_timestamp_before)
        if last_update_timestamp_after is not None:
            where_clauses.append(update_ts_col >= last_update_timestamp_after)
        if last_update_timestamp_before is not None:
            where_clauses.append(update_ts_col <= last_update_timestamp_before)

        return (
            select(
                literal(component_type).label("component_type"),
                manifest_id_col.label("manifest_id"),
                component_id_col.label("component_id"),
                guid_col.label("guid"),
                den_col.label("den"),
                name_col.label("name"),
                definition_col.label("definition"),
                definition_source_col.label("definition_source"),
                is_deprecated_col.label("is_deprecated"),
                state_col.label("state"),
                creation_ts_col.label("creation_timestamp"),
                update_ts_col.label("last_update_timestamp"),
                Namespace.namespace_id.label("namespace_id"),
                Namespace.prefix.label("namespace_prefix"),
                Namespace.uri.label("namespace_uri"),
                Library.library_id.label("library_id"),
                Library.name.label("library_name"),
                Release.release_id.label("release_id"),
                Release.release_num.label("release_num"),
                Release.state.label("release_state"),
                Log.log_id.label("log_id"),
                Log.revision_num.label("revision_num"),
                Log.revision_tracking_num.label("revision_tracking_num"),
                component_owner_col.label("owner_user_id"),
                component_created_by_col.label("created_by"),
                component_updated_by_col.label("last_updated_by"),
                func.min(Tag.name).label("tag"),
            )
            .select_from(manifest_model)
            .join(component_model, component_id_col == manifest_component_id_col)
            .join(Release, Release.release_id == manifest_release_col)
            .join(Library, Library.library_id == Release.library_id)
            .outerjoin(Namespace, Namespace.namespace_id == component_namespace_col)
            .outerjoin(Log, Log.log_id == manifest_log_col)
            .outerjoin(tag_link_model, tag_link_manifest_col == manifest_id_col)
            .outerjoin(Tag, Tag.tag_id == tag_link_model.tag_id)
            .where(and_(*where_clauses))
            .group_by(
                manifest_id_col,
                component_id_col,
                guid_col,
                den_col,
                name_col,
                definition_col,
                definition_source_col,
                is_deprecated_col,
                state_col,
                creation_ts_col,
                update_ts_col,
                Namespace.namespace_id,
                Namespace.prefix,
                Namespace.uri,
                Library.library_id,
                Library.name,
                Release.release_id,
                Release.release_num,
                Release.state,
                Log.log_id,
                Log.revision_num,
                Log.revision_tracking_num,
                component_owner_col,
                component_created_by_col,
                component_updated_by_col,
            )
        )

    def _to_core_component_raw(self, row: dict[str, Any]) -> CoreComponentListRow:
        """Internal helper for to core component raw.

        Args:
            row: Repository row model to convert into a DTO.

        Returns:
            Result of the operation.
        """
        return CoreComponentListRow(
            component_type=str(row["component_type"]),
            manifest_id=row["manifest_id"],
            component_id=row["component_id"],
            guid=str(row["guid"]),
            den=str(row["den"]),
            name=str(row["name"]) if row.get("name") is not None else None,
            definition=str(row["definition"]) if row.get("definition") is not None else None,
            definition_source=str(row["definition_source"]) if row.get("definition_source") is not None else None,
            is_deprecated=bool(row["is_deprecated"]),
            state=str(row["state"]) if row.get("state") is not None else None,
            namespace=_namespace_summary(row),
            library=_library_summary(row),
            release=_release_summary(row),
            log=_log_summary(row),
            owner_user_id=row["owner_user_id"],
            created_by=row["created_by"],
            creation_timestamp=_as_dt(row["creation_timestamp"]),
            last_updated_by=row["last_updated_by"],
            last_update_timestamp=_as_dt(row["last_update_timestamp"]),
            tag=str(row["tag"]) if row.get("tag") is not None else None,
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
