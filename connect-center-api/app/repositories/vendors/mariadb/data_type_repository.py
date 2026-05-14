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
import secrets
from typing import Any, Literal

from sqlalchemy import bindparam, func, select, table, column, text
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import aliased

from app.repositories.contracts.data_type import DataTypeRepositoryContract
from app.repositories.contracts.log import LogRepositoryContract
from app.repositories.models import (
    DataTypeBaseSummaryRow,
    DataTypePrimitiveRow,
    DataTypeSupplementaryComponentRow,
    LibrarySummaryRow,
    LogSummaryRow,
    NamespaceSummaryRow,
    ReleaseSummaryRow,
)
from app.repositories.models.data_type import DataTypeRow
from app.repositories.models.tag import TagSummaryRow
from app.repositories.vendors.mariadb.models.app_user import AppUser
from app.repositories.vendors.mariadb.models.data_type import Dt, DtAwdPri, DtManifest, DtSc, DtScAwdPri, DtScManifest
from app.repositories.vendors.mariadb.models.business_information_entity import BbieSc
from app.repositories.vendors.mariadb.models.core_component import Acc, AccManifest, BccManifest, Bccp, BccpManifest
from app.repositories.vendors.mariadb.models.library import Library
from app.repositories.vendors.mariadb.models.log import Log
from app.repositories.vendors.mariadb.models.namespace import Namespace
from app.repositories.vendors.mariadb.models.release import Release
from app.repositories.vendors.mariadb.models.tag import DtManifestTag, Tag
from app.types.identifiers import (
    AppUserId,
    DataTypeManifestId,
    DataTypeSupplementaryComponentManifestId,
    ReleaseId,
)

CDT_PRI = table("cdt_pri", column("cdt_pri_id"), column("name"))


class MariaDbDataTypeRepository(DataTypeRepositoryContract):
    """MariaDB-backed repository for data type read and command operations."""

    def __init__(self, session: AsyncSession, log_repository: LogRepositoryContract):
        """Initialize MariaDbDataTypeRepository.

        Args:
            session: Database session bound to the current request.
            log_repository: Revision-log repository dependency.
        """
        self._session = session
        self._log_repo = log_repository

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
        included_owner_login_ids: list[str] | None = None,
        excluded_owner_login_ids: list[str] | None = None,
        included_updater_login_ids: list[str] | None = None,
        excluded_updater_login_ids: list[str] | None = None,
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
            included_owner_login_ids: Optional owner login IDs to include by exact match.
            excluded_owner_login_ids: Optional owner login IDs to exclude by exact match.
            included_updater_login_ids: Optional updater login IDs to include by exact match.
            excluded_updater_login_ids: Optional updater login IDs to exclude by exact match.

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
            included_owner_login_ids=included_owner_login_ids,
            excluded_owner_login_ids=excluded_owner_login_ids,
            included_updater_login_ids=included_updater_login_ids,
            excluded_updater_login_ids=excluded_updater_login_ids,
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

        dt_manifest_ids = [int(row["dt_manifest_id"]) for row in rows]
        primitives_by_manifest = await _primitives_by_dt_manifest_ids(self._session, dt_manifest_ids)
        sc_by_owner = await _scs_by_owner_manifest_ids(self._session, dt_manifest_ids)
        tags_by_manifest = await _tags_by_manifest_ids(self._session, dt_manifest_ids)
        return total, [
            _to_data_type_row(
                row,
                primitives_by_manifest.get(row["dt_manifest_id"], []),
                sc_by_owner.get(row["dt_manifest_id"], []),
                tags_by_manifest.get(row["dt_manifest_id"], []),
            )
            for row in rows
        ]

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
        primitives_by_manifest = await _primitives_by_dt_manifest_ids(self._session, [dt_manifest_id_int])
        sc_by_owner = await _scs_by_owner_manifest_ids(self._session, [dt_manifest_id_int])
        tags_by_manifest = await _tags_by_manifest_ids(self._session, [dt_manifest_id_int])
        return _to_data_type_row(
            dict(row._mapping),
            primitives_by_manifest.get(dt_manifest_id_int, []),
            sc_by_owner.get(dt_manifest_id_int, []),
            tags_by_manifest.get(dt_manifest_id_int, []),
        )

    async def create_dt(
        self,
        *,
        release_id: ReleaseId,
        based_dt_manifest_id: DataTypeManifestId,
        requester_user_id: AppUserId,
        tag_id: list[int] | None = None,
    ) -> DataTypeManifestId:
        """Create a DT, its manifest row, initial log row, and optional tags."""
        based_dt_manifest = await self._session.get(DtManifest, int(based_dt_manifest_id))
        if based_dt_manifest is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(based_dt_manifest_id)}. Please verify the identifier and try again."
            )
        based_dt = await self._session.get(Dt, int(based_dt_manifest.dt_id))
        if based_dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(based_dt_manifest_id)}. Please verify the identifier and try again."
            )

        tags_by_id: dict[int, Tag] = {}
        if tag_id is not None:
            for single_tag_id in tag_id:
                tag = await self._session.get(Tag, int(single_tag_id))
                if tag is None:
                    raise LookupError(
                        f"No tag exists with ID {int(single_tag_id)}. Please verify the identifier and try again."
                    )
                tags_by_id[int(single_tag_id)] = tag

        namespace_id = int(based_dt.namespace_id) if based_dt.namespace_id is not None else None

        now = datetime.utcnow()
        dt = Dt(
            guid=secrets.token_hex(16),
            data_type_term=based_dt.data_type_term,
            qualifier=based_dt.qualifier,
            representation_term=based_dt.representation_term,
            six_digit_id=None,
            based_dt_id=int(based_dt.dt_id),
            definition=based_dt.definition,
            definition_source=based_dt.definition_source,
            namespace_id=namespace_id,
            content_component_definition=based_dt.content_component_definition,
            state="WIP",
            commonly_used=False,
            owner_user_id=int(requester_user_id),
            created_by=int(requester_user_id),
            last_updated_by=int(requester_user_id),
            creation_timestamp=now,
            last_update_timestamp=now,
            is_deprecated=False,
            replacement_dt_id=based_dt.replacement_dt_id,
            prev_dt_id=None,
            next_dt_id=None,
        )
        self._session.add(dt)
        await self._session.flush()
        await self._session.refresh(dt)
        dt_id = int(dt.dt_id)

        den = _build_dt_den(qualifier=dt.qualifier, data_type_term=dt.data_type_term)
        dt_manifest = DtManifest(
            release_id=int(release_id),
            dt_id=dt_id,
            based_dt_manifest_id=int(based_dt_manifest_id),
            den=den,
            log_id=None,
        )
        self._session.add(dt_manifest)
        await self._session.flush()
        await self._session.refresh(dt_manifest)
        dt_manifest_id = int(dt_manifest.dt_manifest_id)

        await self._copy_dt_awd_pri_from_manifest(
            release_id=release_id,
            dt_id=dt_id,
            source_dt_manifest_id=based_dt_manifest_id,
        )
        await self._copy_dt_scs_from_manifest(
            release_id=release_id,
            owner_dt_manifest_id=DataTypeManifestId(dt_manifest_id),
            owner_dt_id=dt_id,
            source_owner_dt_manifest_id=based_dt_manifest_id,
            requester_user_id=requester_user_id,
            timestamp=now,
        )
        await self._append_dt_log(
            dt_manifest_id=DataTypeManifestId(dt_manifest_id),
            requester_user_id=requester_user_id,
            action="Added",
            timestamp=now,
        )

        for single_tag_id in tags_by_id:
            self._session.add(
                DtManifestTag(
                    dt_manifest_id=dt_manifest_id,
                    tag_id=int(single_tag_id),
                    created_by=int(requester_user_id),
                    creation_timestamp=now,
                )
            )
        await self._session.flush()
        return DataTypeManifestId(dt_manifest_id)

    async def update_dt(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        qualifier: str | None,
        qualifier_set: bool,
        six_digit_id: str | None,
        six_digit_id_set: bool,
        deprecated: bool | None,
        deprecated_set: bool,
        namespace_id: int | None,
        namespace_id_set: bool,
        content_component_definition: str | None,
        content_component_definition_set: bool,
        definition: str | None,
        definition_set: bool,
        definition_source: str | None,
        definition_source_set: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update mutable DT fields and append a log entry when something changed."""
        dt_manifest = await self._session.get(DtManifest, int(dt_manifest_id))
        if dt_manifest is None:
            return False
        dt = await self._session.get(Dt, int(dt_manifest.dt_id))
        if dt is None:
            return False

        now = datetime.utcnow()
        updates_applied = False
        den_changed = False

        if qualifier_set and qualifier != dt.qualifier:
            dt.qualifier = qualifier
            dt_manifest.den = _build_dt_den(qualifier=qualifier, data_type_term=dt.data_type_term)
            updates_applied = True
            den_changed = True

        if six_digit_id_set and six_digit_id != dt.six_digit_id:
            if six_digit_id:
                duplicate_count = await self._session.scalar(
                    select(func.count())
                    .select_from(DtManifest)
                    .join(Dt, DtManifest.dt_id == Dt.dt_id)
                    .where(
                        DtManifest.release_id == int(dt_manifest.release_id),
                        Dt.six_digit_id == str(six_digit_id),
                        DtManifest.dt_manifest_id != int(dt_manifest_id),
                    )
                )
                if int(duplicate_count or 0) > 0:
                    raise ValueError(f"Six Digit Id '{six_digit_id}' already exist.")
            dt.six_digit_id = six_digit_id
            updates_applied = True

        if deprecated_set and bool(deprecated) != bool(dt.is_deprecated):
            dt.is_deprecated = bool(deprecated)
            updates_applied = True

        if namespace_id_set and namespace_id != dt.namespace_id:
            dt.namespace_id = namespace_id
            updates_applied = True

        if (
            content_component_definition_set
            and content_component_definition != dt.content_component_definition
        ):
            dt.content_component_definition = content_component_definition
            updates_applied = True

        if definition_set and definition != dt.definition:
            dt.definition = definition
            updates_applied = True

        if definition_source_set and definition_source != dt.definition_source:
            dt.definition_source = definition_source
            updates_applied = True

        if not updates_applied:
            return False

        dt.last_updated_by = int(requester_user_id)
        dt.last_update_timestamp = now
        await self._session.flush()
        if den_changed:
            new_den = str(dt_manifest.den)
            bccp_rows = (
                await self._session.execute(
                    select(BccpManifest, Bccp)
                    .join(Bccp, BccpManifest.bccp_id == Bccp.bccp_id)
                    .where(BccpManifest.bdt_manifest_id == int(dt_manifest_id))
                )
            ).all()
            for bccp_manifest, bccp in bccp_rows:
                bccp_den_suffix = new_den[:-6] if new_den.endswith(". Type") else new_den
                bccp_manifest.den = f"{bccp.property_term}. {bccp_den_suffix}"
                bcc_rows = (
                    await self._session.execute(
                        select(BccManifest, Acc)
                        .join(AccManifest, BccManifest.from_acc_manifest_id == AccManifest.acc_manifest_id)
                        .join(Acc, AccManifest.acc_id == Acc.acc_id)
                        .where(BccManifest.to_bccp_manifest_id == int(bccp_manifest.bccp_manifest_id))
                    )
                ).all()
                for bcc_manifest, owner_acc in bcc_rows:
                    bcc_manifest.den = f"{owner_acc.object_class_term}. {bccp_manifest.den}"
            await self._session.flush()
        await self._append_dt_log(
            dt_manifest_id=dt_manifest_id,
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def transfer_dt_ownership(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        requester_user_id: AppUserId,
        target_user_id: AppUserId,
    ) -> bool:
        """Transfer DT ownership, cascade to DT_SC rows, and append a log entry."""
        dt_manifest = await self._session.get(DtManifest, int(dt_manifest_id))
        if dt_manifest is None:
            return False
        dt = await self._session.get(Dt, int(dt_manifest.dt_id))
        if dt is None:
            return False

        target_owner = await self._session.get(AppUser, int(target_user_id))
        if target_owner is None:
            raise LookupError(f"No user exists with ID {int(target_user_id)}. Please verify the identifier and try again.")
        if target_owner.is_enabled is not None and not bool(target_owner.is_enabled):
            raise ValueError(
                "Cannot transfer ownership to a disabled user. "
                "Please choose an enabled user account and try again."
            )

        now = datetime.utcnow()
        dt.owner_user_id = int(target_user_id)
        dt.last_updated_by = int(requester_user_id)
        dt.last_update_timestamp = now
        await self._session.execute(
            text(
                "UPDATE dt_sc "
                "SET owner_user_id = :target_user_id, "
                "last_updated_by = :requester_user_id, "
                "last_update_timestamp = :timestamp "
                "WHERE dt_sc_id IN (SELECT dt_sc_id FROM dt_sc_manifest WHERE owner_dt_manifest_id = :dt_manifest_id)"
            ),
            {
                "target_user_id": int(target_user_id),
                "requester_user_id": int(requester_user_id),
                "timestamp": now,
                "dt_manifest_id": int(dt_manifest_id),
            },
        )
        await self._session.flush()
        await self._append_dt_log(
            dt_manifest_id=dt_manifest_id,
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def create_dt_sc(
        self,
        *,
        owner_dt_manifest_id: DataTypeManifestId,
        requester_user_id: AppUserId,
    ) -> DataTypeSupplementaryComponentManifestId:
        """Create a blank DT_SC and seed default primitives from the CCTS catalogue."""
        owner_dt_manifest = await self._session.get(DtManifest, int(owner_dt_manifest_id))
        if owner_dt_manifest is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(owner_dt_manifest_id)}. Please verify the identifier and try again."
            )
        owner_dt = await self._session.get(Dt, int(owner_dt_manifest.dt_id))
        if owner_dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(owner_dt_manifest_id)}. Please verify the identifier and try again."
            )

        now = datetime.utcnow()
        dt_sc = DtSc(
            guid=secrets.token_hex(16),
            object_class_term=owner_dt.data_type_term,
            property_term=await self._next_default_property_term(release_id=ReleaseId(int(owner_dt_manifest.release_id))),
            representation_term="Text",
            owner_dt_id=int(owner_dt.dt_id),
            cardinality_min=0,
            cardinality_max=1,
            based_dt_sc_id=None,
            default_value=None,
            fixed_value=None,
            is_deprecated=False,
            replacement_dt_sc_id=None,
            created_by=int(requester_user_id),
            owner_user_id=int(requester_user_id),
            last_updated_by=int(requester_user_id),
            creation_timestamp=now,
            last_update_timestamp=now,
            prev_dt_sc_id=None,
            next_dt_sc_id=None,
        )
        self._session.add(dt_sc)
        await self._session.flush()
        await self._session.refresh(dt_sc)

        dt_sc_manifest = DtScManifest(
            release_id=int(owner_dt_manifest.release_id),
            dt_sc_id=int(dt_sc.dt_sc_id),
            owner_dt_manifest_id=int(owner_dt_manifest_id),
            based_dt_sc_manifest_id=None,
            conflict=False,
            replacement_dt_sc_manifest_id=None,
            prev_dt_sc_manifest_id=None,
            next_dt_sc_manifest_id=None,
        )
        self._session.add(dt_sc_manifest)
        await self._session.flush()
        await self._session.refresh(dt_sc_manifest)

        await self._copy_default_primitives_for_representation_term(
            representation_term="Text",
            target_release_id=ReleaseId(int(owner_dt_manifest.release_id)),
            target_dt_sc_id=int(dt_sc.dt_sc_id),
        )
        owner_dt.last_updated_by = int(requester_user_id)
        owner_dt.last_update_timestamp = now
        await self._append_dt_log(
            dt_manifest_id=owner_dt_manifest_id,
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return DataTypeSupplementaryComponentManifestId(int(dt_sc_manifest.dt_sc_manifest_id))

    async def create_dt_sc_from_base(
        self,
        *,
        owner_dt_manifest_id: DataTypeManifestId,
        based_dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
        requester_user_id: AppUserId,
    ) -> DataTypeSupplementaryComponentManifestId:
        """Clone a base DT_SC under a target DT manifest."""
        owner_dt_manifest = await self._session.get(DtManifest, int(owner_dt_manifest_id))
        if owner_dt_manifest is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(owner_dt_manifest_id)}. Please verify the identifier and try again."
            )
        owner_dt = await self._session.get(Dt, int(owner_dt_manifest.dt_id))
        if owner_dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(owner_dt_manifest_id)}. Please verify the identifier and try again."
            )
        based_dt_sc_manifest = await self._session.get(DtScManifest, int(based_dt_sc_manifest_id))
        if based_dt_sc_manifest is None:
            raise LookupError(
                f"No DT_SC exists with manifest ID {int(based_dt_sc_manifest_id)}. Please verify the identifier and try again."
            )
        based_dt_sc = await self._session.get(DtSc, int(based_dt_sc_manifest.dt_sc_id))
        if based_dt_sc is None:
            raise LookupError(
                f"No DT_SC exists with manifest ID {int(based_dt_sc_manifest_id)}. Please verify the identifier and try again."
            )

        now = datetime.utcnow()
        dt_sc = DtSc(
            guid=str(based_dt_sc.guid),
            object_class_term=based_dt_sc.object_class_term,
            property_term=based_dt_sc.property_term,
            representation_term=based_dt_sc.representation_term,
            definition=based_dt_sc.definition,
            definition_source=based_dt_sc.definition_source,
            owner_dt_id=int(owner_dt.dt_id),
            cardinality_min=int(based_dt_sc.cardinality_min),
            cardinality_max=based_dt_sc.cardinality_max,
            based_dt_sc_id=int(based_dt_sc.dt_sc_id),
            default_value=based_dt_sc.default_value,
            fixed_value=based_dt_sc.fixed_value,
            is_deprecated=bool(based_dt_sc.is_deprecated),
            replacement_dt_sc_id=based_dt_sc.replacement_dt_sc_id,
            created_by=int(requester_user_id),
            owner_user_id=int(requester_user_id),
            last_updated_by=int(requester_user_id),
            creation_timestamp=now,
            last_update_timestamp=now,
            prev_dt_sc_id=None,
            next_dt_sc_id=None,
        )
        self._session.add(dt_sc)
        await self._session.flush()
        await self._session.refresh(dt_sc)

        dt_sc_manifest = DtScManifest(
            release_id=int(owner_dt_manifest.release_id),
            dt_sc_id=int(dt_sc.dt_sc_id),
            owner_dt_manifest_id=int(owner_dt_manifest_id),
            based_dt_sc_manifest_id=int(based_dt_sc_manifest_id),
            conflict=False,
            replacement_dt_sc_manifest_id=based_dt_sc_manifest.replacement_dt_sc_manifest_id,
            prev_dt_sc_manifest_id=None,
            next_dt_sc_manifest_id=None,
        )
        self._session.add(dt_sc_manifest)
        await self._session.flush()
        await self._session.refresh(dt_sc_manifest)

        await self._copy_dt_sc_awd_pri_by_dt_sc_id(
            source_release_id=ReleaseId(int(based_dt_sc_manifest.release_id)),
            target_release_id=ReleaseId(int(owner_dt_manifest.release_id)),
            source_dt_sc_id=int(based_dt_sc.dt_sc_id),
            target_dt_sc_id=int(dt_sc.dt_sc_id),
        )
        owner_dt.last_updated_by = int(requester_user_id)
        owner_dt.last_update_timestamp = now
        await self._append_dt_log(
            dt_manifest_id=owner_dt_manifest_id,
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return DataTypeSupplementaryComponentManifestId(int(dt_sc_manifest.dt_sc_manifest_id))

    async def update_dt_sc(
        self,
        *,
        dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
        property_term: str | None,
        property_term_set: bool,
        representation_term: str | None,
        representation_term_set: bool,
        cardinality_min: int | None,
        cardinality_min_set: bool,
        cardinality_max: int | None,
        cardinality_max_set: bool,
        deprecated: bool | None,
        deprecated_set: bool,
        default_value: str | None,
        default_value_set: bool,
        fixed_value: str | None,
        fixed_value_set: bool,
        definition: str | None,
        definition_set: bool,
        definition_source: str | None,
        definition_source_set: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update mutable DT_SC fields and append a modified log on the owner DT."""
        dt_sc_manifest = await self._session.get(DtScManifest, int(dt_sc_manifest_id))
        if dt_sc_manifest is None:
            return False
        dt_sc = await self._session.get(DtSc, int(dt_sc_manifest.dt_sc_id))
        if dt_sc is None:
            return False
        owner_dt_manifest = await self._session.get(DtManifest, int(dt_sc_manifest.owner_dt_manifest_id))
        owner_dt = await self._session.get(Dt, int(owner_dt_manifest.dt_id)) if owner_dt_manifest is not None else None
        if owner_dt_manifest is None or owner_dt is None:
            return False

        now = datetime.utcnow()
        updates_applied = False

        if property_term_set and property_term != dt_sc.property_term:
            dt_sc.property_term = property_term
            updates_applied = True
        if representation_term_set and representation_term != dt_sc.representation_term:
            if not representation_term:
                raise ValueError("representation_term must not be null.")
            dt_sc.representation_term = representation_term
            if dt_sc.default_value is not None:
                dt_sc.default_value = None
            if dt_sc.fixed_value is not None:
                dt_sc.fixed_value = None
            await self._replace_dt_sc_primitives_for_representation_term(
                release_id=ReleaseId(int(dt_sc_manifest.release_id)),
                target_dt_sc_id=int(dt_sc.dt_sc_id),
                representation_term=representation_term,
            )
            updates_applied = True
        if cardinality_min_set and cardinality_min != dt_sc.cardinality_min:
            dt_sc.cardinality_min = int(cardinality_min or 0)
            updates_applied = True
        if cardinality_max_set and cardinality_max != dt_sc.cardinality_max:
            dt_sc.cardinality_max = cardinality_max
            updates_applied = True
        if deprecated_set and bool(deprecated) != bool(dt_sc.is_deprecated):
            dt_sc.is_deprecated = bool(deprecated)
            updates_applied = True
        if definition_set and definition != dt_sc.definition:
            dt_sc.definition = definition
            updates_applied = True
        if definition_source_set and definition_source != dt_sc.definition_source:
            dt_sc.definition_source = definition_source
            updates_applied = True
        if default_value_set and (default_value != dt_sc.default_value or dt_sc.fixed_value is not None):
            dt_sc.default_value = default_value
            if default_value is not None:
                dt_sc.fixed_value = None
            updates_applied = True
        if fixed_value_set and (fixed_value != dt_sc.fixed_value or dt_sc.default_value is not None):
            dt_sc.fixed_value = fixed_value
            if fixed_value is not None:
                dt_sc.default_value = None
            updates_applied = True

        if not updates_applied:
            return False

        dt_sc.last_updated_by = int(requester_user_id)
        dt_sc.last_update_timestamp = now
        owner_dt.last_updated_by = int(requester_user_id)
        owner_dt.last_update_timestamp = now
        await self._append_dt_log(
            dt_manifest_id=DataTypeManifestId(int(owner_dt_manifest.dt_manifest_id)),
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def update_dt_base(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        based_dt_manifest_id: DataTypeManifestId | None,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update the base DT link and append a log entry."""
        dt_manifest = await self._session.get(DtManifest, int(dt_manifest_id))
        if dt_manifest is None:
            return False
        dt = await self._session.get(Dt, int(dt_manifest.dt_id))
        if dt is None:
            return False

        current_base_manifest_id = int(dt_manifest.based_dt_manifest_id) if dt_manifest.based_dt_manifest_id is not None else None
        requested_base_manifest_id = int(based_dt_manifest_id) if based_dt_manifest_id is not None else None
        if current_base_manifest_id == requested_base_manifest_id:
            return False

        based_dt_id: int | None = None
        if based_dt_manifest_id is not None:
            based_dt_manifest = await self._session.get(DtManifest, int(based_dt_manifest_id))
            if based_dt_manifest is None:
                raise LookupError(
                    f"No DT exists with manifest ID {int(based_dt_manifest_id)}. Please verify the identifier and try again."
                )
            based_dt = await self._session.get(Dt, int(based_dt_manifest.dt_id))
            if based_dt is None:
                raise LookupError(
                    f"No DT exists with manifest ID {int(based_dt_manifest_id)}. Please verify the identifier and try again."
                )
            based_dt_id = int(based_dt.dt_id)

        now = datetime.utcnow()
        dt.based_dt_id = based_dt_id
        dt_manifest.based_dt_manifest_id = requested_base_manifest_id
        dt.last_updated_by = int(requester_user_id)
        dt.last_update_timestamp = now
        await self._session.flush()
        await self._append_dt_log(
            dt_manifest_id=dt_manifest_id,
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def delete_dt_sc(
        self,
        *,
        dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
        requester_user_id: AppUserId,
    ) -> bool:
        """Delete a DT_SC and its primitive rows."""
        dt_sc_manifest = await self._session.get(DtScManifest, int(dt_sc_manifest_id))
        if dt_sc_manifest is None:
            return False
        dt_sc = await self._session.get(DtSc, int(dt_sc_manifest.dt_sc_id))
        if dt_sc is None:
            return False
        owner_dt_manifest_id = DataTypeManifestId(int(dt_sc_manifest.owner_dt_manifest_id))
        owner_dt_manifest = await self._session.get(DtManifest, int(owner_dt_manifest_id))
        owner_dt = await self._session.get(Dt, int(owner_dt_manifest.dt_id)) if owner_dt_manifest is not None else None
        now = datetime.utcnow()

        await self._session.execute(
            text("DELETE FROM dt_sc_awd_pri WHERE release_id = :release_id AND dt_sc_id = :dt_sc_id"),
            {"release_id": int(dt_sc_manifest.release_id), "dt_sc_id": int(dt_sc.dt_sc_id)},
        )
        await self._session.delete(dt_sc_manifest)
        await self._session.flush()
        await self._session.delete(dt_sc)
        if owner_dt is not None:
            owner_dt.last_updated_by = int(requester_user_id)
            owner_dt.last_update_timestamp = now
        await self._append_dt_log(
            dt_manifest_id=owner_dt_manifest_id,
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def add_dt_tags(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        tag_id: list[int],
        requester_user_id: AppUserId,
    ) -> bool:
        """Attach tags to a manifest-scoped DT."""
        return await self._add_manifest_tags(
            dt_manifest_id=dt_manifest_id,
            requester_user_id=requester_user_id,
            tag_id=tag_id,
        )

    async def remove_dt_tags(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        tag_id: list[int],
        requester_user_id: AppUserId,
    ) -> bool:
        """Detach tags from a manifest-scoped DT."""
        return await self._remove_manifest_tags(
            dt_manifest_id=dt_manifest_id,
            requester_user_id=requester_user_id,
            tag_id=tag_id,
        )

    async def change_dt_default_primitive(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        xbt_manifest_id: int | None,
        code_list_manifest_id: int | None,
        agency_id_list_manifest_id: int | None,
        requester_user_id: AppUserId,
    ) -> bool:
        """Set exactly one DT primitive row as default."""
        dt_manifest = await self._session.get(DtManifest, int(dt_manifest_id))
        if dt_manifest is None:
            return False
        primitive_rows = (
            await self._session.execute(
                select(DtAwdPri)
                .where(
                    DtAwdPri.release_id == int(dt_manifest.release_id),
                    DtAwdPri.dt_id == int(dt_manifest.dt_id),
                )
                .order_by(DtAwdPri.dt_awd_pri_id.asc())
            )
        ).scalars().all()
        target = self._find_matching_default_primitive_row(
            rows=primitive_rows,
            xbt_manifest_id=xbt_manifest_id,
            code_list_manifest_id=code_list_manifest_id,
            agency_id_list_manifest_id=agency_id_list_manifest_id,
        )
        if target is None:
            raise ValueError("The requested primitive is not available on this DT.")

        updates_applied = False
        for row in primitive_rows:
            should_be_default = int(row.dt_awd_pri_id) == int(target.dt_awd_pri_id)
            if bool(row.is_default) != should_be_default:
                row.is_default = should_be_default
                updates_applied = True
        if not updates_applied:
            return False

        dt = await self._session.get(Dt, int(dt_manifest.dt_id))
        now = datetime.utcnow()
        if dt is not None:
            dt.last_updated_by = int(requester_user_id)
            dt.last_update_timestamp = now
        await self._append_dt_log(
            dt_manifest_id=dt_manifest_id,
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def replace_dt_primitives(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        primitives: list[DataTypePrimitiveRow],
        requester_user_id: AppUserId,
    ) -> bool:
        """Replace DT primitive rows with the desired manifest set."""
        dt_manifest = await self._session.get(DtManifest, int(dt_manifest_id))
        if dt_manifest is None:
            return False
        primitive_rows = (
            await self._session.execute(
                select(DtAwdPri)
                .where(
                    DtAwdPri.release_id == int(dt_manifest.release_id),
                    DtAwdPri.dt_id == int(dt_manifest.dt_id),
                )
                .order_by(DtAwdPri.dt_awd_pri_id.asc())
            )
        ).scalars().all()

        desired_by_key = {_primitive_key(primitive): primitive for primitive in primitives}
        current_by_key = {_primitive_key(row): row for row in primitive_rows}
        updates_applied = False

        for key, row in current_by_key.items():
            if key not in desired_by_key:
                await self._session.delete(row)
                updates_applied = True

        for key, primitive in desired_by_key.items():
            current = current_by_key.get(key)
            if current is None:
                await self._create_dt_primitive_row(
                    dt_manifest=dt_manifest,
                    cdt_pri_name=primitive.cdt_pri_name,
                    xbt_manifest_id=primitive.xbt_manifest_id,
                    code_list_manifest_id=primitive.code_list_manifest_id,
                    agency_id_list_manifest_id=primitive.agency_id_list_manifest_id,
                    is_default=bool(primitive.is_default),
                )
                updates_applied = True
                continue
            if bool(current.is_default) != bool(primitive.is_default):
                current.is_default = bool(primitive.is_default)
                updates_applied = True

        if not updates_applied:
            return False

        dt = await self._session.get(Dt, int(dt_manifest.dt_id))
        now = datetime.utcnow()
        if dt is not None:
            dt.last_updated_by = int(requester_user_id)
            dt.last_update_timestamp = now
        await self._append_dt_log(
            dt_manifest_id=dt_manifest_id,
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def change_dt_sc_default_primitive(
        self,
        *,
        dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
        xbt_manifest_id: int | None,
        code_list_manifest_id: int | None,
        agency_id_list_manifest_id: int | None,
        requester_user_id: AppUserId,
    ) -> bool:
        """Set exactly one DT_SC primitive row as default."""
        dt_sc_manifest = await self._session.get(DtScManifest, int(dt_sc_manifest_id))
        if dt_sc_manifest is None:
            return False
        primitive_rows = (
            await self._session.execute(
                select(DtScAwdPri)
                .where(
                    DtScAwdPri.release_id == int(dt_sc_manifest.release_id),
                    DtScAwdPri.dt_sc_id == int(dt_sc_manifest.dt_sc_id),
                )
                .order_by(DtScAwdPri.dt_sc_awd_pri_id.asc())
            )
        ).scalars().all()
        target = self._find_matching_default_primitive_row(
            rows=primitive_rows,
            xbt_manifest_id=xbt_manifest_id,
            code_list_manifest_id=code_list_manifest_id,
            agency_id_list_manifest_id=agency_id_list_manifest_id,
        )
        if target is None and (
            xbt_manifest_id is not None or code_list_manifest_id is not None or agency_id_list_manifest_id is not None
        ):
            target = await self._create_dt_sc_primitive_row(
                dt_sc_manifest=dt_sc_manifest,
                cdt_pri_name=None,
                xbt_manifest_id=xbt_manifest_id,
                code_list_manifest_id=code_list_manifest_id,
                agency_id_list_manifest_id=agency_id_list_manifest_id,
            )
            primitive_rows.append(target)
        if target is None:
            raise ValueError("The requested primitive is not available on this DT_SC.")

        updates_applied = False
        for row in primitive_rows:
            should_be_default = int(row.dt_sc_awd_pri_id) == int(target.dt_sc_awd_pri_id)
            if bool(row.is_default) != should_be_default:
                row.is_default = should_be_default
                updates_applied = True
        if not updates_applied:
            return False

        owner_dt_manifest_id = DataTypeManifestId(int(dt_sc_manifest.owner_dt_manifest_id))
        owner_dt_manifest = await self._session.get(DtManifest, int(owner_dt_manifest_id))
        owner_dt = await self._session.get(Dt, int(owner_dt_manifest.dt_id)) if owner_dt_manifest is not None else None
        now = datetime.utcnow()
        if owner_dt is not None:
            owner_dt.last_updated_by = int(requester_user_id)
            owner_dt.last_update_timestamp = now
        await self._append_dt_log(
            dt_manifest_id=owner_dt_manifest_id,
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def replace_dt_sc_primitives(
        self,
        *,
        dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
        primitives: list[DataTypePrimitiveRow],
        requester_user_id: AppUserId,
    ) -> bool:
        """Replace DT_SC primitive rows with the desired manifest set."""
        dt_sc_manifest = await self._session.get(DtScManifest, int(dt_sc_manifest_id))
        if dt_sc_manifest is None:
            return False
        primitive_rows = (
            await self._session.execute(
                select(DtScAwdPri)
                .where(
                    DtScAwdPri.release_id == int(dt_sc_manifest.release_id),
                    DtScAwdPri.dt_sc_id == int(dt_sc_manifest.dt_sc_id),
                )
                .order_by(DtScAwdPri.dt_sc_awd_pri_id.asc())
            )
        ).scalars().all()

        desired_by_key = {_primitive_key(primitive): primitive for primitive in primitives}
        current_by_key = {_primitive_key(row): row for row in primitive_rows}
        updates_applied = False

        for key, row in current_by_key.items():
            if key not in desired_by_key:
                await self._session.delete(row)
                updates_applied = True

        for key, primitive in desired_by_key.items():
            current = current_by_key.get(key)
            if current is None:
                await self._create_dt_sc_primitive_row(
                    dt_sc_manifest=dt_sc_manifest,
                    cdt_pri_name=primitive.cdt_pri_name,
                    xbt_manifest_id=primitive.xbt_manifest_id,
                    code_list_manifest_id=primitive.code_list_manifest_id,
                    agency_id_list_manifest_id=primitive.agency_id_list_manifest_id,
                    is_default=bool(primitive.is_default),
                )
                updates_applied = True
                continue
            if bool(current.is_default) != bool(primitive.is_default):
                current.is_default = bool(primitive.is_default)
                updates_applied = True

        if not updates_applied:
            return False

        owner_dt_manifest_id = DataTypeManifestId(int(dt_sc_manifest.owner_dt_manifest_id))
        owner_dt_manifest = await self._session.get(DtManifest, int(owner_dt_manifest_id))
        owner_dt = await self._session.get(Dt, int(owner_dt_manifest.dt_id)) if owner_dt_manifest is not None else None
        now = datetime.utcnow()
        if owner_dt is not None:
            owner_dt.last_updated_by = int(requester_user_id)
            owner_dt.last_update_timestamp = now
        await self._append_dt_log(
            dt_manifest_id=owner_dt_manifest_id,
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def change_dt_state(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        state: str,
        restore_owner: bool,
        implicit_move: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update a DT lifecycle state and append a corresponding log entry."""
        dt_manifest = await self._session.get(DtManifest, int(dt_manifest_id))
        if dt_manifest is None:
            return False
        dt = await self._session.get(Dt, int(dt_manifest.dt_id))
        if dt is None:
            return False

        now = datetime.utcnow()
        dt.state = state
        if restore_owner:
            dt.owner_user_id = int(requester_user_id)
        if not implicit_move:
            dt.last_updated_by = int(requester_user_id)
            dt.last_update_timestamp = now

        await self._session.execute(
            text(
                "UPDATE dt_sc "
                "SET owner_user_id = CASE WHEN :restore_owner THEN :requester_user_id ELSE owner_user_id END, "
                "last_updated_by = CASE WHEN :implicit_move THEN last_updated_by ELSE :requester_user_id END, "
                "last_update_timestamp = CASE WHEN :implicit_move THEN last_update_timestamp ELSE :timestamp END "
                "WHERE dt_sc_id IN (SELECT dt_sc_id FROM dt_sc_manifest WHERE owner_dt_manifest_id = :dt_manifest_id)"
            ),
            {
                "restore_owner": bool(restore_owner),
                "requester_user_id": int(requester_user_id),
                "implicit_move": bool(implicit_move),
                "timestamp": now,
                "dt_manifest_id": int(dt_manifest_id),
            },
        )
        await self._session.flush()
        await self._append_dt_log(
            dt_manifest_id=dt_manifest_id,
            requester_user_id=requester_user_id,
            action="Deleted" if state == "Deleted" else ("Restored" if restore_owner else "Modified"),
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def revise_dt(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        requester_user_id: AppUserId,
    ) -> bool:
        """Create a revised DT working copy and move the manifest head to it."""
        dt_manifest = await self._session.get(DtManifest, int(dt_manifest_id))
        if dt_manifest is None:
            return False
        dt = await self._session.get(Dt, int(dt_manifest.dt_id))
        if dt is None:
            return False

        now = datetime.utcnow()
        prev_dt_id = int(dt.dt_id)
        next_dt = Dt(
            guid=str(dt.guid),
            data_type_term=dt.data_type_term,
            qualifier=dt.qualifier,
            representation_term=dt.representation_term,
            six_digit_id=dt.six_digit_id,
            based_dt_id=dt.based_dt_id,
            definition=dt.definition,
            definition_source=dt.definition_source,
            namespace_id=dt.namespace_id,
            content_component_definition=dt.content_component_definition,
            state="WIP",
            commonly_used=bool(dt.commonly_used),
            owner_user_id=int(requester_user_id),
            created_by=int(requester_user_id),
            last_updated_by=int(requester_user_id),
            creation_timestamp=now,
            last_update_timestamp=now,
            is_deprecated=bool(dt.is_deprecated),
            replacement_dt_id=dt.replacement_dt_id,
            prev_dt_id=prev_dt_id,
            next_dt_id=None,
        )
        self._session.add(next_dt)
        await self._session.flush()
        await self._session.refresh(next_dt)

        dt.next_dt_id = int(next_dt.dt_id)
        await self._copy_dt_awd_pri_by_dt_id(
            release_id=ReleaseId(int(dt_manifest.release_id)),
            source_dt_id=int(dt.dt_id),
            target_dt_id=int(next_dt.dt_id),
        )
        dt_manifest.dt_id = int(next_dt.dt_id)

        dt_sc_manifests = (
            await self._session.execute(
                select(DtScManifest).where(DtScManifest.owner_dt_manifest_id == int(dt_manifest_id))
            )
        ).scalars().all()
        for dt_sc_manifest in dt_sc_manifests:
            prev_dt_sc = await self._session.get(DtSc, int(dt_sc_manifest.dt_sc_id))
            if prev_dt_sc is None:
                continue
            next_dt_sc = DtSc(
                guid=str(prev_dt_sc.guid),
                object_class_term=prev_dt_sc.object_class_term,
                property_term=prev_dt_sc.property_term,
                representation_term=prev_dt_sc.representation_term,
                definition=prev_dt_sc.definition,
                definition_source=prev_dt_sc.definition_source,
                owner_dt_id=int(next_dt.dt_id),
                cardinality_min=int(prev_dt_sc.cardinality_min),
                cardinality_max=prev_dt_sc.cardinality_max,
                based_dt_sc_id=prev_dt_sc.based_dt_sc_id,
                default_value=prev_dt_sc.default_value,
                fixed_value=prev_dt_sc.fixed_value,
                is_deprecated=bool(prev_dt_sc.is_deprecated),
                replacement_dt_sc_id=prev_dt_sc.replacement_dt_sc_id,
                created_by=int(requester_user_id),
                owner_user_id=int(requester_user_id),
                last_updated_by=int(requester_user_id),
                creation_timestamp=now,
                last_update_timestamp=now,
                prev_dt_sc_id=int(prev_dt_sc.dt_sc_id),
                next_dt_sc_id=None,
            )
            self._session.add(next_dt_sc)
            await self._session.flush()
            await self._session.refresh(next_dt_sc)
            prev_dt_sc.next_dt_sc_id = int(next_dt_sc.dt_sc_id)
            await self._copy_dt_sc_awd_pri_by_dt_sc_id(
                source_release_id=ReleaseId(int(dt_manifest.release_id)),
                target_release_id=ReleaseId(int(dt_manifest.release_id)),
                source_dt_sc_id=int(prev_dt_sc.dt_sc_id),
                target_dt_sc_id=int(next_dt_sc.dt_sc_id),
            )
            dt_sc_manifest.dt_sc_id = int(next_dt_sc.dt_sc_id)

        await self._session.flush()
        await self._append_dt_log(
            dt_manifest_id=dt_manifest_id,
            requester_user_id=requester_user_id,
            action="Revised",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def cancel_dt(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
    ) -> bool:
        """Cancel the active DT revision and restore the previous stable revision."""
        dt_manifest = await self._session.get(DtManifest, int(dt_manifest_id))
        if dt_manifest is None:
            return False
        dt = await self._session.get(Dt, int(dt_manifest.dt_id))
        if dt is None:
            return False
        if dt.prev_dt_id is None:
            raise ValueError("Not found previous revision")
        prev_dt = await self._session.get(Dt, int(dt.prev_dt_id))
        if prev_dt is None:
            raise ValueError("Not found previous revision")

        current_log_id = int(dt_manifest.log_id) if dt_manifest.log_id is not None else None
        dt_manifest.log_id = None
        await self._session.flush()
        stable_log_id = await self._log_repo.revert_component_log_to_stable_state(
            reference=str(dt.guid),
            current_log_id=current_log_id,
        )
        dt_manifest.dt_id = int(prev_dt.dt_id)
        dt_manifest.log_id = int(stable_log_id)
        prev_dt.next_dt_id = None

        dt_sc_manifests = (
            await self._session.execute(
                select(DtScManifest).where(DtScManifest.owner_dt_manifest_id == int(dt_manifest_id))
            )
        ).scalars().all()
        for dt_sc_manifest in dt_sc_manifests:
            current_dt_sc = await self._session.get(DtSc, int(dt_sc_manifest.dt_sc_id))
            if current_dt_sc is None:
                continue

            await self._session.execute(
                text(
                    "DELETE FROM dt_sc_awd_pri WHERE release_id = :release_id AND dt_sc_id = :dt_sc_id"
                ),
                {"release_id": int(dt_manifest.release_id), "dt_sc_id": int(current_dt_sc.dt_sc_id)},
            )

            if current_dt_sc.prev_dt_sc_id is None:
                await self._session.delete(dt_sc_manifest)
            else:
                prev_dt_sc = await self._session.get(DtSc, int(current_dt_sc.prev_dt_sc_id))
                if prev_dt_sc is None:
                    raise ValueError("Not found previous DT supplementary-component revision")
                prev_dt_sc.next_dt_sc_id = None
                dt_sc_manifest.dt_sc_id = int(prev_dt_sc.dt_sc_id)

            await self._session.delete(current_dt_sc)

        await self._session.execute(
            text("DELETE FROM dt_awd_pri WHERE release_id = :release_id AND dt_id = :dt_id"),
            {"release_id": int(dt_manifest.release_id), "dt_id": int(dt.dt_id)},
        )
        await self._session.delete(dt)
        await self._session.flush()
        return True

    async def discard_dt(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
    ) -> bool:
        """Discard a Deleted DT permanently."""
        dt_manifest = await self._session.get(DtManifest, int(dt_manifest_id))
        if dt_manifest is None:
            return False
        dt = await self._session.get(Dt, int(dt_manifest.dt_id))
        if dt is None:
            return False

        if dt_manifest.log_id is not None:
            dt_manifest.log_id = None
            await self._session.flush()
        await self._session.execute(
            text(
                "UPDATE log SET prev_log_id = NULL, next_log_id = NULL WHERE reference = :reference"
            ),
            {"reference": str(dt.guid)},
        )
        await self._session.execute(
            text("DELETE FROM log WHERE reference = :reference"),
            {"reference": str(dt.guid)},
        )

        dt_sc_manifest_rows = (
            await self._session.execute(
                select(DtScManifest).where(DtScManifest.owner_dt_manifest_id == int(dt_manifest_id))
            )
        ).scalars().all()
        dt_sc_ids = [int(row.dt_sc_id) for row in dt_sc_manifest_rows]
        if dt_sc_ids:
            await self._session.execute(
                text(
                    "DELETE FROM dt_sc_awd_pri WHERE dt_sc_id IN :dt_sc_ids"
                ).bindparams(bindparam("dt_sc_ids", expanding=True)),
                {"dt_sc_ids": tuple(dt_sc_ids)},
            )
            await self._session.execute(
                text("DELETE FROM dt_sc_manifest WHERE owner_dt_manifest_id = :dt_manifest_id"),
                {"dt_manifest_id": int(dt_manifest_id)},
            )
            await self._session.execute(
                text("DELETE FROM dt_sc WHERE dt_sc_id IN :dt_sc_ids").bindparams(bindparam("dt_sc_ids", expanding=True)),
                {"dt_sc_ids": tuple(dt_sc_ids)},
            )

        await self._session.execute(
            text("DELETE FROM module_dt_manifest WHERE dt_manifest_id = :dt_manifest_id"),
            {"dt_manifest_id": int(dt_manifest_id)},
        )
        await self._session.execute(
            text("DELETE FROM dt_awd_pri WHERE release_id = :release_id AND dt_id = :dt_id"),
            {"release_id": int(dt_manifest.release_id), "dt_id": int(dt.dt_id)},
        )
        await self._session.execute(
            text("DELETE FROM dt_manifest_tag WHERE dt_manifest_id = :dt_manifest_id"),
            {"dt_manifest_id": int(dt_manifest_id)},
        )
        await self._session.delete(dt_manifest)
        await self._session.flush()
        await self._session.delete(dt)
        await self._session.flush()
        return True

    async def has_deriving_dts(self, dt_manifest_id: DataTypeManifestId) -> bool:
        """Return whether any DT still derives from the target DT manifest."""
        dt_manifest = await self._session.get(DtManifest, int(dt_manifest_id))
        if dt_manifest is None:
            return False
        count = await self._session.scalar(
            select(func.count())
            .select_from(DtManifest)
            .join(Dt, DtManifest.dt_id == Dt.dt_id)
            .where(
                DtManifest.dt_manifest_id != int(dt_manifest_id),
                Dt.based_dt_id == int(dt_manifest.dt_id),
            )
        )
        return int(count or 0) > 0

    async def has_related_bccps_for_dt(self, dt_manifest_id: DataTypeManifestId) -> bool:
        """Return whether any BCCP still references the target DT manifest."""
        count = await self._session.scalar(
            select(func.count())
            .select_from(BccpManifest)
            .where(BccpManifest.bdt_manifest_id == int(dt_manifest_id))
        )
        return int(count or 0) > 0

    async def get_owner_dt_manifest_id_by_dt_sc(
        self,
        dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
    ) -> DataTypeManifestId | None:
        """Resolve the owner DT manifest of a DT_SC manifest."""
        dt_sc_manifest = await self._session.get(DtScManifest, int(dt_sc_manifest_id))
        if dt_sc_manifest is None:
            return None
        return DataTypeManifestId(int(dt_sc_manifest.owner_dt_manifest_id))

    async def get_based_dt_sc_manifest_id(
        self,
        dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
    ) -> DataTypeSupplementaryComponentManifestId | None:
        """Resolve the base DT_SC manifest of a DT_SC manifest."""
        dt_sc_manifest = await self._session.get(DtScManifest, int(dt_sc_manifest_id))
        if dt_sc_manifest is None or dt_sc_manifest.based_dt_sc_manifest_id is None:
            return None
        return DataTypeSupplementaryComponentManifestId(int(dt_sc_manifest.based_dt_sc_manifest_id))

    async def list_direct_inherited_dt_manifest_ids(
        self,
        based_dt_manifest_id: DataTypeManifestId,
    ) -> list[DataTypeManifestId]:
        """Return direct child DT manifests derived from the provided DT manifest."""
        values = (
            await self._session.scalars(
                select(DtManifest.dt_manifest_id)
                .where(DtManifest.based_dt_manifest_id == int(based_dt_manifest_id))
                .order_by(DtManifest.dt_manifest_id.asc())
            )
        ).all()
        return [DataTypeManifestId(int(value)) for value in values]

    async def list_direct_inherited_dt_sc_manifest_ids(
        self,
        based_dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
    ) -> list[DataTypeSupplementaryComponentManifestId]:
        """Return direct child DT_SC manifests derived from the provided DT_SC manifest."""
        values = (
            await self._session.scalars(
                select(DtScManifest.dt_sc_manifest_id)
                .where(DtScManifest.based_dt_sc_manifest_id == int(based_dt_sc_manifest_id))
                .order_by(DtScManifest.dt_sc_manifest_id.asc())
            )
        ).all()
        return [DataTypeSupplementaryComponentManifestId(int(value)) for value in values]

    async def count_bbie_sc_refs(
        self,
        dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
    ) -> int:
        """Return the number of BBIE_SC rows that reference the DT_SC manifest."""
        count = await self._session.scalar(
            select(func.count())
            .select_from(BbieSc)
            .where(BbieSc.based_dt_sc_manifest_id == int(dt_sc_manifest_id))
        )
        return int(count or 0)

    async def _append_dt_log(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        requester_user_id: AppUserId,
        action: str,
        timestamp: datetime,
    ) -> int:
        """Append a DT log row and update the manifest head."""
        dt_manifest = await self._session.get(DtManifest, int(dt_manifest_id))
        if dt_manifest is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(dt_manifest_id)}. Please verify the identifier and try again."
            )
        dt = await self._session.get(Dt, int(dt_manifest.dt_id))
        if dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(dt_manifest_id)}. Please verify the identifier and try again."
            )
        log_id = await self._log_repo.append_component_log(
            reference=str(dt.guid),
            current_log_id=int(dt_manifest.log_id) if dt_manifest.log_id is not None else None,
            requester_user_id=requester_user_id,
            action=action,
            timestamp=timestamp,
        )
        dt_manifest.log_id = int(log_id)
        return int(log_id)

    async def _copy_dt_awd_pri_from_manifest(
        self,
        *,
        release_id: ReleaseId,
        dt_id: int,
        source_dt_manifest_id: DataTypeManifestId,
    ) -> None:
        """Copy primitive restrictions from a source DT manifest to a target DT row."""
        await self._session.execute(
            text(
                "INSERT INTO dt_awd_pri "
                "(release_id, dt_id, cdt_pri_id, xbt_manifest_id, code_list_manifest_id, agency_id_list_manifest_id, is_default) "
                "SELECT :release_id, :dt_id, dt_awd_pri.cdt_pri_id, dt_awd_pri.xbt_manifest_id, dt_awd_pri.code_list_manifest_id, "
                "dt_awd_pri.agency_id_list_manifest_id, dt_awd_pri.is_default "
                "FROM dt_awd_pri "
                "JOIN dt_manifest ON dt_awd_pri.release_id = dt_manifest.release_id AND dt_awd_pri.dt_id = dt_manifest.dt_id "
                "WHERE dt_manifest.dt_manifest_id = :source_dt_manifest_id"
            ),
            {
                "release_id": int(release_id),
                "dt_id": int(dt_id),
                "source_dt_manifest_id": int(source_dt_manifest_id),
            },
        )

    async def _copy_dt_awd_pri_by_dt_id(
        self,
        *,
        release_id: ReleaseId,
        source_dt_id: int,
        target_dt_id: int,
    ) -> None:
        """Copy primitive restrictions from one DT row to another within the same release."""
        await self._session.execute(
            text(
                "INSERT INTO dt_awd_pri "
                "(release_id, dt_id, cdt_pri_id, xbt_manifest_id, code_list_manifest_id, agency_id_list_manifest_id, is_default) "
                "SELECT :release_id, :target_dt_id, cdt_pri_id, xbt_manifest_id, code_list_manifest_id, agency_id_list_manifest_id, is_default "
                "FROM dt_awd_pri WHERE release_id = :release_id AND dt_id = :source_dt_id"
            ),
            {
                "release_id": int(release_id),
                "source_dt_id": int(source_dt_id),
                "target_dt_id": int(target_dt_id),
            },
        )

    async def _copy_dt_sc_awd_pri_by_dt_sc_id(
        self,
        *,
        source_release_id: ReleaseId,
        target_release_id: ReleaseId,
        source_dt_sc_id: int,
        target_dt_sc_id: int,
    ) -> None:
        """Copy supplementary-component primitive restrictions from one DT_SC row to another."""
        await self._session.execute(
            text(
                "INSERT INTO dt_sc_awd_pri "
                "(release_id, dt_sc_id, cdt_pri_id, xbt_manifest_id, code_list_manifest_id, agency_id_list_manifest_id, is_default) "
                "SELECT :target_release_id, :target_dt_sc_id, cdt_pri_id, xbt_manifest_id, code_list_manifest_id, agency_id_list_manifest_id, is_default "
                "FROM dt_sc_awd_pri WHERE release_id = :source_release_id AND dt_sc_id = :source_dt_sc_id"
            ),
            {
                "source_release_id": int(source_release_id),
                "target_release_id": int(target_release_id),
                "source_dt_sc_id": int(source_dt_sc_id),
                "target_dt_sc_id": int(target_dt_sc_id),
            },
        )

    async def _copy_default_primitives_for_representation_term(
        self,
        *,
        representation_term: str,
        target_release_id: ReleaseId,
        target_dt_sc_id: int,
    ) -> None:
        """Seed DT_SC primitives from the CCTS Data Type Catalogue v3.1 DT matching the representation term."""
        ccts_library = await self._session.scalar(
            select(Library).where(Library.name == "CCTS Data Type Catalogue v3")
        )
        if ccts_library is None:
            raise ValueError("Could not find CCTS Data Type Catalogue v3.")
        ccts_release = await self._session.scalar(
            select(Release)
            .where(
                Release.library_id == int(ccts_library.library_id),
                Release.release_num == "3.1",
            )
        )
        if ccts_release is None:
            raise ValueError("Could not find CCTS Data Type Catalogue v3.1.")

        source_dt_manifest = await self._session.scalar(
            select(DtManifest)
            .join(Dt, Dt.dt_id == DtManifest.dt_id)
            .where(
                DtManifest.release_id == int(ccts_release.release_id),
                Dt.representation_term == representation_term,
            )
            .order_by(DtManifest.dt_manifest_id.asc())
        )
        if source_dt_manifest is None:
            raise ValueError(f"Could not find a matched DT record for the '{representation_term}' representation term.")

        await self._session.execute(
            text(
                "INSERT INTO dt_sc_awd_pri "
                "(release_id, dt_sc_id, cdt_pri_id, xbt_manifest_id, code_list_manifest_id, agency_id_list_manifest_id, is_default) "
                "SELECT :target_release_id, :target_dt_sc_id, dt_awd_pri.cdt_pri_id, dt_awd_pri.xbt_manifest_id, "
                "dt_awd_pri.code_list_manifest_id, dt_awd_pri.agency_id_list_manifest_id, dt_awd_pri.is_default "
                "FROM dt_awd_pri "
                "JOIN dt_manifest ON dt_awd_pri.release_id = dt_manifest.release_id AND dt_awd_pri.dt_id = dt_manifest.dt_id "
                "WHERE dt_manifest.dt_manifest_id = :source_dt_manifest_id "
                "AND dt_awd_pri.xbt_manifest_id IS NOT NULL"
            ),
            {
                "target_release_id": int(target_release_id),
                "target_dt_sc_id": int(target_dt_sc_id),
                "source_dt_manifest_id": int(source_dt_manifest.dt_manifest_id),
            },
        )

    async def _replace_dt_sc_primitives_for_representation_term(
        self,
        *,
        release_id: ReleaseId,
        target_dt_sc_id: int,
        representation_term: str,
    ) -> None:
        """Replace DT_SC primitive rows with the default CDT set for the representation term."""
        await self._session.execute(
            text("DELETE FROM dt_sc_awd_pri WHERE release_id = :release_id AND dt_sc_id = :dt_sc_id"),
            {
                "release_id": int(release_id),
                "dt_sc_id": int(target_dt_sc_id),
            },
        )
        await self._copy_default_primitives_for_representation_term(
            representation_term=representation_term,
            target_release_id=release_id,
            target_dt_sc_id=target_dt_sc_id,
        )

    async def _create_dt_sc_primitive_row(
        self,
        *,
        dt_sc_manifest: DtScManifest,
        cdt_pri_name: str | None,
        xbt_manifest_id: int | None,
        code_list_manifest_id: int | None,
        agency_id_list_manifest_id: int | None,
        is_default: bool = False,
    ) -> DtScAwdPri:
        """Create a DT_SC primitive row for an XBT, code-list, or agency restriction."""
        cdt_pri_id = await self._resolve_cdt_pri_id_for_primitive(
            cdt_pri_name=cdt_pri_name,
            xbt_manifest_id=xbt_manifest_id,
            code_list_manifest_id=code_list_manifest_id,
            agency_id_list_manifest_id=agency_id_list_manifest_id,
        )

        row = DtScAwdPri(
            release_id=int(dt_sc_manifest.release_id),
            dt_sc_id=int(dt_sc_manifest.dt_sc_id),
            cdt_pri_id=int(cdt_pri_id),
            xbt_manifest_id=None if xbt_manifest_id is None else int(xbt_manifest_id),
            code_list_manifest_id=None if code_list_manifest_id is None else int(code_list_manifest_id),
            agency_id_list_manifest_id=(
                None if agency_id_list_manifest_id is None else int(agency_id_list_manifest_id)
            ),
            is_default=bool(is_default),
        )
        self._session.add(row)
        await self._session.flush()
        await self._session.refresh(row)
        return row

    async def _create_dt_primitive_row(
        self,
        *,
        dt_manifest: DtManifest,
        cdt_pri_name: str | None,
        xbt_manifest_id: int | None,
        code_list_manifest_id: int | None,
        agency_id_list_manifest_id: int | None,
        is_default: bool = False,
    ) -> DtAwdPri:
        """Create a DT primitive row for an XBT, code-list, or agency restriction."""
        cdt_pri_id = await self._resolve_cdt_pri_id_for_primitive(
            cdt_pri_name=cdt_pri_name,
            xbt_manifest_id=xbt_manifest_id,
            code_list_manifest_id=code_list_manifest_id,
            agency_id_list_manifest_id=agency_id_list_manifest_id,
        )

        row = DtAwdPri(
            release_id=int(dt_manifest.release_id),
            dt_id=int(dt_manifest.dt_id),
            cdt_pri_id=int(cdt_pri_id),
            xbt_manifest_id=None if xbt_manifest_id is None else int(xbt_manifest_id),
            code_list_manifest_id=None if code_list_manifest_id is None else int(code_list_manifest_id),
            agency_id_list_manifest_id=None if agency_id_list_manifest_id is None else int(agency_id_list_manifest_id),
            is_default=bool(is_default),
        )
        self._session.add(row)
        await self._session.flush()
        await self._session.refresh(row)
        return row

    async def _resolve_cdt_pri_id_for_primitive(
        self,
        *,
        cdt_pri_name: str | None,
        xbt_manifest_id: int | None,
        code_list_manifest_id: int | None,
        agency_id_list_manifest_id: int | None,
    ) -> int:
        """Resolve the CDT primitive ID for a new primitive row."""
        if cdt_pri_name is not None:
            cdt_pri_id = await self._session.scalar(
                text("SELECT cdt_pri_id FROM cdt_pri WHERE name = :name LIMIT 1"),
                {"name": cdt_pri_name},
            )
            if cdt_pri_id is None:
                raise ValueError(f"Could not find the '{cdt_pri_name}' CDT primitive.")
            return int(cdt_pri_id)
        if code_list_manifest_id is not None or agency_id_list_manifest_id is not None:
            cdt_pri_id = await self._session.scalar(
                text("SELECT cdt_pri_id FROM cdt_pri WHERE name = :name LIMIT 1"),
                {"name": "Token"},
            )
            if cdt_pri_id is None:
                raise ValueError("Could not find the 'Token' CDT primitive.")
            return int(cdt_pri_id)
        if xbt_manifest_id is None:
            raise ValueError("One primitive manifest identifier is required.")

        cdt_pri_id = await self._session.scalar(
            text(
                """
                SELECT cdt_pri_id
                FROM (
                    SELECT p.cdt_pri_id AS cdt_pri_id
                    FROM dt_awd_pri p
                    WHERE p.xbt_manifest_id = :xbt_manifest_id
                    UNION ALL
                    SELECT p.cdt_pri_id AS cdt_pri_id
                    FROM dt_sc_awd_pri p
                    WHERE p.xbt_manifest_id = :xbt_manifest_id
                ) candidates
                LIMIT 1
                """
            ),
            {"xbt_manifest_id": int(xbt_manifest_id)},
        )
        if cdt_pri_id is None:
            raise ValueError(
                f"Could not infer the CDT primitive for xbt_manifest_id {int(xbt_manifest_id)}."
            )
        return int(cdt_pri_id)

    async def _next_default_property_term(self, *, release_id: ReleaseId) -> str:
        """Return the next available `Property Term N` label for a release."""
        rows = (
            await self._session.scalars(
                select(DtSc.property_term)
                .select_from(DtScManifest)
                .join(DtSc, DtSc.dt_sc_id == DtScManifest.dt_sc_id)
                .where(
                    DtScManifest.release_id == int(release_id),
                    DtSc.property_term.like("Property Term%"),
                )
            )
        ).all()
        existing_numbers: set[int] = set()
        prefix = "Property Term "
        for value in rows:
            if value is None or not str(value).startswith(prefix):
                continue
            try:
                existing_numbers.add(int(str(value)[len(prefix):].strip()))
            except ValueError:
                continue
        next_number = 1
        while next_number in existing_numbers:
            next_number += 1
        return f"Property Term {next_number}"

    @staticmethod
    def _find_matching_default_primitive_row(
        *,
        rows: list[DtAwdPri] | list[DtScAwdPri],
        xbt_manifest_id: int | None,
        code_list_manifest_id: int | None,
        agency_id_list_manifest_id: int | None,
    ) -> DtAwdPri | DtScAwdPri | None:
        """Return the primitive row matching the requested manifest identifier."""
        for row in rows:
            if xbt_manifest_id is not None and int(row.xbt_manifest_id or 0) == int(xbt_manifest_id):
                return row
            if code_list_manifest_id is not None and int(row.code_list_manifest_id or 0) == int(code_list_manifest_id):
                return row
            if (
                agency_id_list_manifest_id is not None
                and int(row.agency_id_list_manifest_id or 0) == int(agency_id_list_manifest_id)
            ):
                return row
        return None

    async def _copy_dt_scs_from_manifest(
        self,
        *,
        release_id: ReleaseId,
        owner_dt_manifest_id: DataTypeManifestId,
        owner_dt_id: int,
        source_owner_dt_manifest_id: DataTypeManifestId,
        requester_user_id: AppUserId,
        timestamp: datetime,
    ) -> None:
        """Clone DT supplementary components from a source manifest into a target DT manifest."""
        source_sc_manifests = (
            await self._session.execute(
                select(DtScManifest)
                .where(DtScManifest.owner_dt_manifest_id == int(source_owner_dt_manifest_id))
                .order_by(DtScManifest.dt_sc_manifest_id.asc())
            )
        ).scalars().all()
        for source_sc_manifest in source_sc_manifests:
            source_sc = await self._session.get(DtSc, int(source_sc_manifest.dt_sc_id))
            if source_sc is None:
                continue

            next_sc = DtSc(
                guid=secrets.token_hex(16),
                object_class_term=source_sc.object_class_term,
                property_term=source_sc.property_term,
                representation_term=source_sc.representation_term,
                definition=source_sc.definition,
                definition_source=source_sc.definition_source,
                owner_dt_id=int(owner_dt_id),
                cardinality_min=int(source_sc.cardinality_min),
                cardinality_max=source_sc.cardinality_max,
                based_dt_sc_id=int(source_sc.dt_sc_id),
                default_value=source_sc.default_value,
                fixed_value=source_sc.fixed_value,
                is_deprecated=bool(source_sc.is_deprecated),
                replacement_dt_sc_id=source_sc.replacement_dt_sc_id,
                created_by=int(requester_user_id),
                owner_user_id=int(requester_user_id),
                last_updated_by=int(requester_user_id),
                creation_timestamp=timestamp,
                last_update_timestamp=timestamp,
                prev_dt_sc_id=None,
                next_dt_sc_id=None,
            )
            self._session.add(next_sc)
            await self._session.flush()
            await self._session.refresh(next_sc)

            next_sc_manifest = DtScManifest(
                release_id=int(release_id),
                dt_sc_id=int(next_sc.dt_sc_id),
                owner_dt_manifest_id=int(owner_dt_manifest_id),
                based_dt_sc_manifest_id=int(source_sc_manifest.dt_sc_manifest_id),
                conflict=False,
                replacement_dt_sc_manifest_id=source_sc_manifest.replacement_dt_sc_manifest_id,
                prev_dt_sc_manifest_id=None,
                next_dt_sc_manifest_id=None,
            )
            self._session.add(next_sc_manifest)
            await self._session.flush()

            await self._copy_dt_sc_awd_pri_by_dt_sc_id(
                source_release_id=ReleaseId(int(source_sc_manifest.release_id)),
                target_release_id=release_id,
                source_dt_sc_id=int(source_sc.dt_sc_id),
                target_dt_sc_id=int(next_sc.dt_sc_id),
            )

    async def _add_manifest_tags(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        requester_user_id: AppUserId,
        tag_id: list[int],
    ) -> bool:
        """Attach tags to a manifest-scoped DT."""
        dt_manifest = await self._session.get(DtManifest, int(dt_manifest_id))
        if dt_manifest is None:
            return False
        dt = await self._session.get(Dt, int(dt_manifest.dt_id))
        if dt is None:
            return False

        tags_by_id: dict[int, Tag] = {}
        for single_tag_id in tag_id:
            tag = await self._session.get(Tag, int(single_tag_id))
            if tag is None:
                raise LookupError(
                    f"No tag exists with ID {int(single_tag_id)}. Please verify the identifier and try again."
                )
            tags_by_id[int(single_tag_id)] = tag

        current_tag_ids = {
            int(value)
            for value in (
                await self._session.scalars(
                    select(DtManifestTag.tag_id).where(DtManifestTag.dt_manifest_id == int(dt_manifest_id))
                )
            ).all()
        }
        tag_ids_to_add = [single_tag_id for single_tag_id in tags_by_id if single_tag_id not in current_tag_ids]
        if not tag_ids_to_add:
            return False

        now = datetime.utcnow()
        for single_tag_id in tag_ids_to_add:
            self._session.add(
                DtManifestTag(
                    dt_manifest_id=int(dt_manifest_id),
                    tag_id=int(single_tag_id),
                    created_by=int(requester_user_id),
                    creation_timestamp=now,
                )
            )
        dt.last_updated_by = int(requester_user_id)
        dt.last_update_timestamp = now
        await self._session.flush()
        await self._append_dt_log(
            dt_manifest_id=dt_manifest_id,
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return True

    async def _remove_manifest_tags(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        requester_user_id: AppUserId,
        tag_id: list[int],
    ) -> bool:
        """Detach tags from a manifest-scoped DT."""
        dt_manifest = await self._session.get(DtManifest, int(dt_manifest_id))
        if dt_manifest is None:
            return False
        dt = await self._session.get(Dt, int(dt_manifest.dt_id))
        if dt is None:
            return False

        for single_tag_id in tag_id:
            tag = await self._session.get(Tag, int(single_tag_id))
            if tag is None:
                raise LookupError(
                    f"No tag exists with ID {int(single_tag_id)}. Please verify the identifier and try again."
                )

        current_tag_ids = {
            int(value)
            for value in (
                await self._session.scalars(
                    select(DtManifestTag.tag_id).where(DtManifestTag.dt_manifest_id == int(dt_manifest_id))
                )
            ).all()
        }
        tag_ids_to_remove = [single_tag_id for single_tag_id in tag_id if single_tag_id in current_tag_ids]
        if not tag_ids_to_remove:
            return False

        now = datetime.utcnow()
        await self._session.execute(
            text("DELETE FROM dt_manifest_tag WHERE dt_manifest_id = :dt_manifest_id AND tag_id IN :tag_ids")
            .bindparams(bindparam("tag_ids", expanding=True)),
            {"dt_manifest_id": int(dt_manifest_id), "tag_ids": tuple(tag_ids_to_remove)},
        )
        dt.last_updated_by = int(requester_user_id)
        dt.last_update_timestamp = now
        await self._session.flush()
        await self._append_dt_log(
            dt_manifest_id=dt_manifest_id,
            requester_user_id=requester_user_id,
            action="Modified",
            timestamp=now,
        )
        await self._session.flush()
        return True


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
    included_owner_login_ids: list[str] | None = None,
    excluded_owner_login_ids: list[str] | None = None,
    included_updater_login_ids: list[str] | None = None,
    excluded_updater_login_ids: list[str] | None = None,
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
    if included_owner_login_ids:
        clauses.append(
            Dt.owner_user_id.in_(
                select(AppUser.app_user_id).where(AppUser.login_id.in_(included_owner_login_ids))
            )
        )
    if excluded_owner_login_ids:
        clauses.append(
            Dt.owner_user_id.not_in(
                select(AppUser.app_user_id).where(AppUser.login_id.in_(excluded_owner_login_ids))
            )
        )
    if included_updater_login_ids:
        clauses.append(
            Dt.last_updated_by.in_(
                select(AppUser.app_user_id).where(AppUser.login_id.in_(included_updater_login_ids))
            )
        )
    if excluded_updater_login_ids:
        clauses.append(
            Dt.last_updated_by.not_in(
                select(AppUser.app_user_id).where(AppUser.login_id.in_(excluded_updater_login_ids))
            )
        )
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

    primitives_by_sc_manifest = await _primitives_by_dt_sc_manifest_ids(session, dt_manifest_ids)

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
                primitives=primitives_by_sc_manifest.get(int(sc_row["dt_sc_manifest_id"]), []),
            )
        )
    return sc_by_owner


async def _primitives_by_dt_manifest_ids(
    session: AsyncSession,
    dt_manifest_ids: list[int],
) -> dict[int, list[DataTypePrimitiveRow]]:
    if not dt_manifest_ids:
        return {}

    rows = (
        await session.execute(
            select(
                DtManifest.dt_manifest_id,
                CDT_PRI.c.name.label("cdt_pri_name"),
                DtAwdPri.xbt_manifest_id,
                DtAwdPri.code_list_manifest_id,
                DtAwdPri.agency_id_list_manifest_id,
                DtAwdPri.is_default,
            )
            .select_from(DtManifest)
            .join(DtAwdPri, (DtAwdPri.release_id == DtManifest.release_id) & (DtAwdPri.dt_id == DtManifest.dt_id))
            .join(CDT_PRI, CDT_PRI.c.cdt_pri_id == DtAwdPri.cdt_pri_id)
            .where(DtManifest.dt_manifest_id.in_(dt_manifest_ids))
            .order_by(DtManifest.dt_manifest_id.asc(), DtAwdPri.dt_awd_pri_id.asc())
        )
    ).all()

    primitives_by_manifest: dict[int, list[DataTypePrimitiveRow]] = defaultdict(list)
    for row in rows:
        primitives_by_manifest[int(row.dt_manifest_id)].append(
            DataTypePrimitiveRow(
                cdt_pri_name=(
                    str(row.cdt_pri_name)
                    if row.cdt_pri_name is not None and row.xbt_manifest_id is not None
                    else None
                ),
                xbt_manifest_id=int(row.xbt_manifest_id) if row.xbt_manifest_id is not None else None,
                code_list_manifest_id=int(row.code_list_manifest_id) if row.code_list_manifest_id is not None else None,
                agency_id_list_manifest_id=(
                    int(row.agency_id_list_manifest_id) if row.agency_id_list_manifest_id is not None else None
                ),
                is_default=bool(row.is_default),
            )
        )
    return primitives_by_manifest


async def _primitives_by_dt_sc_manifest_ids(
    session: AsyncSession,
    dt_manifest_ids: list[int],
) -> dict[int, list[DataTypePrimitiveRow]]:
    if not dt_manifest_ids:
        return {}

    rows = (
        await session.execute(
            select(
                DtScManifest.dt_sc_manifest_id,
                CDT_PRI.c.name.label("cdt_pri_name"),
                DtScAwdPri.xbt_manifest_id,
                DtScAwdPri.code_list_manifest_id,
                DtScAwdPri.agency_id_list_manifest_id,
                DtScAwdPri.is_default,
            )
            .select_from(DtScManifest)
            .join(DtScAwdPri, (DtScAwdPri.release_id == DtScManifest.release_id) & (DtScAwdPri.dt_sc_id == DtScManifest.dt_sc_id))
            .join(CDT_PRI, CDT_PRI.c.cdt_pri_id == DtScAwdPri.cdt_pri_id)
            .where(DtScManifest.owner_dt_manifest_id.in_(dt_manifest_ids))
            .order_by(DtScManifest.dt_sc_manifest_id.asc(), DtScAwdPri.dt_sc_awd_pri_id.asc())
        )
    ).all()

    primitives_by_manifest: dict[int, list[DataTypePrimitiveRow]] = defaultdict(list)
    for row in rows:
        primitives_by_manifest[int(row.dt_sc_manifest_id)].append(
            DataTypePrimitiveRow(
                cdt_pri_name=(
                    str(row.cdt_pri_name)
                    if row.cdt_pri_name is not None and row.xbt_manifest_id is not None
                    else None
                ),
                xbt_manifest_id=int(row.xbt_manifest_id) if row.xbt_manifest_id is not None else None,
                code_list_manifest_id=int(row.code_list_manifest_id) if row.code_list_manifest_id is not None else None,
                agency_id_list_manifest_id=(
                    int(row.agency_id_list_manifest_id) if row.agency_id_list_manifest_id is not None else None
                ),
                is_default=bool(row.is_default),
            )
        )
    return primitives_by_manifest


async def _tags_by_manifest_ids(
    session: AsyncSession,
    dt_manifest_ids: list[int],
) -> dict[int, list[TagSummaryRow]]:
    if not dt_manifest_ids:
        return {}

    tag_rows = (
        await session.execute(
            select(
                DtManifestTag.dt_manifest_id,
                Tag.tag_id,
                Tag.name,
            )
            .select_from(DtManifestTag)
            .join(Tag, Tag.tag_id == DtManifestTag.tag_id)
            .where(DtManifestTag.dt_manifest_id.in_(dt_manifest_ids))
            .order_by(DtManifestTag.dt_manifest_id.asc(), Tag.name.asc(), Tag.tag_id.asc())
        )
    ).all()

    tags_by_manifest: dict[int, list[TagSummaryRow]] = defaultdict(list)
    for tag_row in tag_rows:
        tags_by_manifest[int(tag_row.dt_manifest_id)].append(
            TagSummaryRow(tag_id=int(tag_row.tag_id), name=str(tag_row.name))
        )
    return tags_by_manifest


def _to_data_type_row(
    row: dict[str, Any],
    primitives: list[DataTypePrimitiveRow],
    scs: list[DataTypeSupplementaryComponentRow],
    tags: list[TagSummaryRow],
) -> DataTypeRow:
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
        primitives=primitives,
        supplementary_components=scs,
        tags=tags,
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


def _build_dt_den(*, qualifier: str | None, data_type_term: str | None) -> str:
    """Build a DT DEN from qualifier and data type term."""
    normalized_data_type_term = (data_type_term or "").strip()
    if not normalized_data_type_term:
        raise ValueError("A data type term is required to build the DT DEN.")
    normalized_qualifier = (qualifier or "").strip()
    if normalized_qualifier:
        return f"{normalized_qualifier}_ {normalized_data_type_term}. Type"
    return f"{normalized_data_type_term}. Type"


def _primitive_key(row: DataTypePrimitiveRow | DtAwdPri | DtScAwdPri) -> tuple[int | None, int | None, int | None]:
    """Build a stable primitive identity key."""
    return (
        int(row.xbt_manifest_id) if row.xbt_manifest_id is not None else None,
        int(row.code_list_manifest_id) if row.code_list_manifest_id is not None else None,
        int(row.agency_id_list_manifest_id) if row.agency_id_list_manifest_id is not None else None,
    )


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
