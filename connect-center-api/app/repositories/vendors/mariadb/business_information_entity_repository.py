"""MariaDB repository implementation for Business Information Entity read operations."""


from __future__ import annotations

import hashlib
from datetime import datetime, timezone
from typing import Any, Sequence, Literal

from sqlalchemy import and_, delete as sa_delete, func, or_, select, text, union
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.contracts.business_information_entity import BusinessInformationEntityRepositoryContract
from app.repositories.contracts.core_component import CoreComponentRepositoryContract
from app.repositories.models import (
    AccInfoRow,
    AsccInfoRow,
    AsccRelationshipInfoRow,
    AsccpInfoRow,
    BccInfoRow,
    BccRelationshipInfoRow,
    BccpInfoRow,
    DataTypeSupplementaryComponentRow,
    DtSummaryRow,
    LibrarySummaryRow,
    ReleaseSummaryRow,
    ValueConstraintRow,
)
from app.repositories.models.business_information_entity import (
    AbieInfoRow,
    AsbieCreatePlanRow,
    AsbieRelationshipRow,
    AsbiepRolePlanRow,
    AsbiepInfoRow,
    BbieCreatePlanRow,
    BbieRelationshipRow,
    BbiepPlanRow,
    BbieScInfoRow,
    BbiepInfoRow,
    BizCtxSummaryRow,
    FacetRow,
    GetAsbieRow,
    GetBbieRow,
    PrimitiveRestrictionRow,
    TopLevelAsbiepDetailRow,
    TopLevelAsbiepInfoRow,
    TopLevelAsbiepListRow,
)
from app.repositories.vendors.mariadb.models.app_user import AppUser
from app.repositories.vendors.mariadb.models.agency_id_list import AgencyIdListManifest
from app.repositories.vendors.mariadb.models.biz_ctx import BizCtx, BizCtxAssignment
from app.repositories.vendors.mariadb.models.business_information_entity import (
    Abie,
    Asbie,
    Asbiep,
    Bbie,
    BbieSc,
    Bbiep,
    TopLevelAsbiep,
)
from app.repositories.vendors.mariadb.models.core_component import (
    Acc,
    AccManifest,
    Ascc,
    AsccManifest,
    Asccp,
    AsccpManifest,
    Bcc,
    BccManifest,
    BccpManifest,
)
from app.repositories.vendors.mariadb.models.code_list import CodeListManifest
from app.repositories.vendors.mariadb.models.data_type import DtAwdPri, DtManifest, DtSc, DtScAwdPri, DtScManifest
from app.repositories.vendors.mariadb.models.library import Library
from app.repositories.vendors.mariadb.models.oas import OasMessageBody, OasRequest, OasResponse
from app.repositories.vendors.mariadb.models.release import Release
from app.repositories.vendors.mariadb.models.xbt import XbtManifest
from app.services.utils.string import new_guid
from app.types.identifiers import (
    AccManifestId,
    AgencyIdListManifestId,
    AppUserId,
    AsccManifestId,
    AsccpManifestId,
    BccManifestId,
    BccpManifestId,
    BizCtxId,
    CodeListManifestId,
    DataTypeManifestId,
    DataTypeSupplementaryComponentId,
    DataTypeSupplementaryComponentManifestId,
    LibraryId,
    ReleaseId,
    XbtManifestId,
)

ORDER_BY_COLUMN_MAP = {
    "den": AsccpManifest.den,
    "version": TopLevelAsbiep.version,
    "status": TopLevelAsbiep.status,
    "state": TopLevelAsbiep.state,
    "creation_timestamp": Asbiep.creation_timestamp,
    "last_update_timestamp": TopLevelAsbiep.last_update_timestamp,
}


class MariaDbBusinessInformationEntityRepository(BusinessInformationEntityRepositoryContract):
    """MariaDB-backed repository for BIE read operations."""

    def __init__(
        self,
        session: AsyncSession,
        core_component_repository: CoreComponentRepositoryContract,
    ):
        """Initialize MariaDbBusinessInformationEntityRepository.

        Args:
            session: Database session bound to the current request.
            core_component_repository: Core-component repository dependency.
        """
        self._session = session
        self._core_component_repo = core_component_repository

    @staticmethod
    def _normalize_base_cardinality_max(value: int | None) -> int:
        return -1 if value is None else int(value)

    @staticmethod
    def _not_found_message(entity_name: str, identifier: int | str) -> str:
        return f"No {entity_name} exists with ID {identifier}. Please verify the identifier and try again."

    @staticmethod
    def _validate_cardinality_against_base(
        *,
        relationship_name: str,
        base_name: str,
        base_cardinality_min: int,
        base_cardinality_max: int,
        current_cardinality_min: int,
        current_cardinality_max: int,
        cardinality_min: int | None,
        cardinality_max: int | None,
    ) -> tuple[int, int]:
        final_cardinality_min = current_cardinality_min if cardinality_min is None else int(cardinality_min)
        final_cardinality_max = current_cardinality_max if cardinality_max is None else int(cardinality_max)

        if final_cardinality_min < int(base_cardinality_min):
            raise ValueError(
                f"The minimum cardinality ({final_cardinality_min}) is too low. "
                f"It must be at least {int(base_cardinality_min)} as required by the base {base_name}."
                f" Please set cardinality_min to {int(base_cardinality_min)} or higher."
            )
        if int(base_cardinality_max) != -1:
            if final_cardinality_max != -1 and final_cardinality_max > int(base_cardinality_max):
                raise ValueError(
                    f"The maximum cardinality ({final_cardinality_max}) exceeds the limit. "
                    f"It must be at most {int(base_cardinality_max)} as allowed by the base {base_name}."
                    f" Please set cardinality_max to {int(base_cardinality_max)} or lower, "
                    "or use -1 for unbounded if needed."
                )
        if final_cardinality_max != -1 and final_cardinality_min > final_cardinality_max:
            raise ValueError(
                f"The minimum cardinality ({final_cardinality_min}) cannot be greater than "
                f"the maximum cardinality ({final_cardinality_max})."
                f" Please adjust the values so that the minimum is less than or equal to the maximum. "
                f"For example, set cardinality_max to at least {final_cardinality_min}, or reduce cardinality_min."
            )
        return final_cardinality_min, final_cardinality_max

    async def _validate_primitive_restriction_for_top_level_release(
        self,
        *,
        xbt_manifest_id: int | None,
        code_list_manifest_id: int | None,
        agency_id_list_manifest_id: int | None,
        top_level_release_id: int,
    ) -> tuple[int | None, int | None, int | None]:
        if xbt_manifest_id is None and code_list_manifest_id is None and agency_id_list_manifest_id is None:
            return None, None, None

        has_xbt = xbt_manifest_id is not None
        has_code_list = code_list_manifest_id is not None
        has_agency_id_list = agency_id_list_manifest_id is not None
        count = sum([has_xbt, has_code_list, has_agency_id_list])

        if count > 1:
            set_values: list[str] = []
            if has_xbt:
                set_values.append(f"xbtManifestId={xbt_manifest_id}")
            if has_code_list:
                set_values.append(f"codeListManifestId={code_list_manifest_id}")
            if has_agency_id_list:
                set_values.append(f"agencyIdListManifestId={agency_id_list_manifest_id}")
            raise ValueError(
                f"PrimitiveRestriction validation failed: Exactly one of xbtManifestId, codeListManifestId, "
                f"or agencyIdListManifestId must be set. Found {count} values set: {', '.join(set_values)}."
            )

        selected_label: str
        selected_manifest_id: int
        selected_release_id: int
        if has_xbt:
            manifest = await self._session.get(XbtManifest, int(xbt_manifest_id))
            if manifest is None:
                raise LookupError(self._not_found_message("XBT manifest", int(xbt_manifest_id)))
            selected_label = "xbt_manifest_id"
            selected_manifest_id = int(xbt_manifest_id)
            selected_release_id = int(manifest.release_id)
        elif has_code_list:
            manifest = await self._session.get(CodeListManifest, int(code_list_manifest_id))
            if manifest is None:
                raise LookupError(self._not_found_message("code list manifest", int(code_list_manifest_id)))
            selected_label = "code_list_manifest_id"
            selected_manifest_id = int(code_list_manifest_id)
            selected_release_id = int(manifest.release_id)
        else:
            manifest = await self._session.get(AgencyIdListManifest, int(agency_id_list_manifest_id))
            if manifest is None:
                raise LookupError(self._not_found_message("agency ID list manifest", int(agency_id_list_manifest_id)))
            selected_label = "agency_id_list_manifest_id"
            selected_manifest_id = int(agency_id_list_manifest_id)
            selected_release_id = int(manifest.release_id)

        if selected_release_id != int(top_level_release_id):
            selected_release = await self._session.get(Release, selected_release_id)
            top_level_release = await self._session.get(Release, int(top_level_release_id))
            selected_release_label = (
                f"release {selected_release.release_num}"
                if selected_release is not None and getattr(selected_release, "release_num", None)
                else f"release ID {selected_release_id}"
            )
            top_level_release_label = (
                f"release {top_level_release.release_num}"
                if top_level_release is not None and getattr(top_level_release, "release_num", None)
                else f"release ID {int(top_level_release_id)}"
            )
            raise ValueError(
                f"The selected {selected_label} ({selected_manifest_id}) belongs to {selected_release_label}, "
                f"but the owning top-level ASBIEP belongs to {top_level_release_label}. "
                "Please choose a primitive restriction from the same release and try again."
            )

        return xbt_manifest_id, code_list_manifest_id, agency_id_list_manifest_id

    async def list_top_level_asbieps(
        self,
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        library_id: LibraryId | None = None,
        release_ids: list[ReleaseId] | None = None,
        den: str | None = None,
        version: str | None = None,
        status: str | None = None,
        state: str | None = None,
        is_deprecated: bool | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
        included_owner_login_ids: list[str] | None = None,
        excluded_owner_login_ids: list[str] | None = None,
    ) -> tuple[int, list[TopLevelAsbiepListRow]]:
        """Handle list top level asbieps.

        Args:
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            library_id: Library identifier used to scope the query.
            release_ids: Release identifiers used to scope the query.
            den: Optional Dictionary Entry Name (DEN) filter.
            version: Optional version filter.
            status: Optional status filter.
            state: Optional lifecycle state filter.
            is_deprecated: Optional deprecation flag filter.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.
            included_owner_login_ids: Optional owner login IDs to include by exact match.
            excluded_owner_login_ids: Optional owner login IDs to exclude by exact match.

        Returns:
            Result of the operation.
        """
        conditions = [TopLevelAsbiep.asbiep_id.is_not(None)]
        if library_id is not None:
            conditions.append(Release.library_id == library_id)
        if release_ids:
            normalized_release_ids = [value for value in release_ids if value > 0]
            if normalized_release_ids:
                conditions.append(TopLevelAsbiep.release_id.in_(normalized_release_ids))
        if den:
            pattern = f"%{den.lower()}%"
            conditions.append(
                or_(
                    func.lower(func.coalesce(AsccpManifest.den, "")).like(pattern),
                    func.lower(func.coalesce(Asbiep.display_name, "")).like(pattern),
                )
            )
        if version:
            conditions.append(func.lower(func.coalesce(TopLevelAsbiep.version, "")).like(f"%{version.lower()}%"))
        if status:
            conditions.append(func.lower(func.coalesce(TopLevelAsbiep.status, "")).like(f"%{status.lower()}%"))
        if state:
            conditions.append(func.lower(func.coalesce(TopLevelAsbiep.state, "")).like(f"%{state.lower()}%"))
        if is_deprecated is not None:
            conditions.append(TopLevelAsbiep.is_deprecated == bool(is_deprecated))
        if creation_timestamp_after is not None:
            conditions.append(Asbiep.creation_timestamp >= creation_timestamp_after)
        if creation_timestamp_before is not None:
            conditions.append(Asbiep.creation_timestamp <= creation_timestamp_before)
        if last_update_timestamp_after is not None:
            conditions.append(TopLevelAsbiep.last_update_timestamp >= last_update_timestamp_after)
        if last_update_timestamp_before is not None:
            conditions.append(TopLevelAsbiep.last_update_timestamp <= last_update_timestamp_before)
        if included_owner_login_ids:
            conditions.append(AppUser.login_id.in_(included_owner_login_ids))
        if excluded_owner_login_ids:
            conditions.append(AppUser.login_id.not_in(excluded_owner_login_ids))

        from_stmt = (
            TopLevelAsbiep.__table__
            .join(Asbiep, TopLevelAsbiep.asbiep_id == Asbiep.asbiep_id)
            .join(AsccpManifest, Asbiep.based_asccp_manifest_id == AsccpManifest.asccp_manifest_id)
            .join(Asccp, AsccpManifest.asccp_id == Asccp.asccp_id)
            .join(Release, TopLevelAsbiep.release_id == Release.release_id)
            .join(Library, Release.library_id == Library.library_id)
            .join(AppUser, TopLevelAsbiep.owner_user_id == AppUser.app_user_id)
            .join(BizCtxAssignment, TopLevelAsbiep.top_level_asbiep_id == BizCtxAssignment.top_level_asbiep_id)
        )

        count_stmt = select(func.count(func.distinct(TopLevelAsbiep.top_level_asbiep_id))).select_from(from_stmt).where(and_(*conditions))
        total = int((await self._session.execute(count_stmt)).scalar_one())

        order_by = []
        for sort in sorts:
            col = ORDER_BY_COLUMN_MAP.get(sort[0])
            if col is not None:
                order_by.append(col.desc() if sort[1] == "DESC" else col.asc())
        if not order_by:
            order_by = [TopLevelAsbiep.last_update_timestamp.desc()]

        list_stmt = (
            select(
                TopLevelAsbiep.top_level_asbiep_id,
                Asbiep.asbiep_id,
                Asbiep.guid,
                AsccpManifest.den,
                Asccp.property_term,
                Asbiep.display_name,
                TopLevelAsbiep.version,
                TopLevelAsbiep.status,
                Asbiep.biz_term,
                Asbiep.remark,
                TopLevelAsbiep.state,
                TopLevelAsbiep.is_deprecated,
                TopLevelAsbiep.deprecated_reason,
                TopLevelAsbiep.deprecated_remark,
                Library.library_id,
                Library.name.label("library_name"),
                Release.release_id,
                Release.release_num,
                Release.state.label("release_state"),
                TopLevelAsbiep.owner_user_id,
                Asbiep.created_by,
                Asbiep.creation_timestamp,
                TopLevelAsbiep.last_updated_by,
                TopLevelAsbiep.last_update_timestamp,
            )
            .select_from(from_stmt)
            .where(and_(*conditions))
            .distinct()
            .order_by(*order_by)
            .limit(limit)
            .offset(offset)
        )
        rows = [dict(row._mapping) for row in (await self._session.execute(list_stmt)).all()]
        if not rows:
            return total, []

        top_level_ids = [row["top_level_asbiep_id"] for row in rows]
        business_contexts_by_top_level_id = await self._load_business_contexts(top_level_ids)

        items = [
            TopLevelAsbiepListRow(
                top_level_asbiep_id=row["top_level_asbiep_id"],
                asbiep_id=row["asbiep_id"],
                guid=str(row["guid"]),
                den=str(row["den"]) if row.get("den") is not None else None,
                property_term=str(row["property_term"]) if row.get("property_term") is not None else None,
                display_name=str(row["display_name"]) if row.get("display_name") is not None else None,
                version=str(row["version"]) if row.get("version") is not None else None,
                status=str(row["status"]) if row.get("status") is not None else None,
                biz_term=str(row["biz_term"]) if row.get("biz_term") is not None else None,
                remark=str(row["remark"]) if row.get("remark") is not None else None,
                business_contexts=business_contexts_by_top_level_id.get(row["top_level_asbiep_id"], []),
                state=str(row["state"]) if row.get("state") is not None else None,
                is_deprecated=bool(row["is_deprecated"]),
                deprecated_reason=str(row["deprecated_reason"]) if row.get("deprecated_reason") is not None else None,
                deprecated_remark=str(row["deprecated_remark"]) if row.get("deprecated_remark") is not None else None,
                library=LibrarySummaryRow(
                    library_id=row["library_id"],
                    name=str(row["library_name"]),
                ),
                release=ReleaseSummaryRow(
                    release_id=row["release_id"],
                    release_num=str(row["release_num"]) if row.get("release_num") is not None else None,
                    state=str(row["release_state"]),
                ),
                owner_user_id=row["owner_user_id"],
                created_by=row["created_by"],
                creation_timestamp=_as_dt(row["creation_timestamp"]),
                last_updated_by=row["last_updated_by"],
                last_update_timestamp=_as_dt(row["last_update_timestamp"]),
            )
            for row in rows
        ]
        return total, items

    async def get_top_level_asbiep(self, *, top_level_asbiep_id: int) -> TopLevelAsbiepDetailRow | None:
        """Handle get top level asbiep.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.

        Returns:
            Result of the operation.
        """
        row = (
            await self._session.execute(
                select(TopLevelAsbiep, Asbiep)
                .join(Asbiep, TopLevelAsbiep.asbiep_id == Asbiep.asbiep_id)
                .where(TopLevelAsbiep.top_level_asbiep_id == top_level_asbiep_id)
            )
        ).first()
        if row is None:
            return None

        top_level = row[0]
        asbiep = row[1]
        asbiep_info = await self._get_asbiep_info(
            top_level_asbiep_id=top_level.top_level_asbiep_id,
            asbiep_id=asbiep.asbiep_id,
            asccp_manifest_id=AsccpManifestId(asbiep.based_asccp_manifest_id),
            parent_asbie_path=None,
        )

        business_contexts = await self._load_business_contexts([top_level.top_level_asbiep_id])

        return TopLevelAsbiepDetailRow(
            top_level_asbiep_id=top_level.top_level_asbiep_id,
            asbiep=asbiep_info,
            version=top_level.version,
            status=top_level.status,
            business_contexts=business_contexts.get(top_level.top_level_asbiep_id, []),
            state=top_level.state,
            is_deprecated=bool(top_level.is_deprecated),
            deprecated_reason=top_level.deprecated_reason,
            deprecated_remark=top_level.deprecated_remark,
            owner_user_id=top_level.owner_user_id,
            created_by=asbiep.created_by,
            creation_timestamp=_as_dt(asbiep.creation_timestamp),
            last_updated_by=top_level.last_updated_by,
            last_update_timestamp=_as_dt(top_level.last_update_timestamp),
        )

    async def get_asbie_by_asbie_id(self, *, asbie_id: int) -> GetAsbieRow | None:
        """Handle get asbie by asbie id.

        Args:
            asbie_id: ASBIE identifier.

        Returns:
            Result of the operation.
        """
        asbie = await self._session.get(Asbie, asbie_id)
        if asbie is None:
            return None

        owner_top = await self._build_top_level_info(asbie.owner_top_level_asbiep_id)
        based_ascc = await self._build_ascc_info(AsccManifestId(asbie.based_ascc_manifest_id))
        if based_ascc is None:
            return None
        if asbie.to_asbiep_id is None:
            return None

        target_asbiep = await self._session.get(Asbiep, asbie.to_asbiep_id)
        if target_asbiep is None:
            return None

        to_asbiep = await self._get_asbiep_info(
            top_level_asbiep_id=int(target_asbiep.owner_top_level_asbiep_id or asbie.owner_top_level_asbiep_id),
            asbiep_id=target_asbiep.asbiep_id,
            asccp_manifest_id=AsccpManifestId(target_asbiep.based_asccp_manifest_id),
            parent_asbie_path=asbie.path,
        )

        return GetAsbieRow(
            asbie_id=asbie.asbie_id,
            owner_top_level_asbiep=owner_top,
            guid=asbie.guid,
            based_ascc=based_ascc,
            to_asbiep=to_asbiep,
            is_used=bool(asbie.is_used),
            cardinality_min=asbie.cardinality_min,
            cardinality_max=asbie.cardinality_max,
            is_nillable=bool(asbie.is_nillable),
            definition=asbie.definition,
            remark=asbie.remark,
            created_by=asbie.created_by if asbie.created_by is not None else None,
            creation_timestamp=_as_dt(asbie.creation_timestamp) if asbie.creation_timestamp is not None else None,
            last_updated_by=asbie.last_updated_by if asbie.last_updated_by is not None else None,
            last_update_timestamp=_as_dt(asbie.last_update_timestamp) if asbie.last_update_timestamp is not None else None,
        )

    async def get_asbie_by_based_ascc_manifest_id(
        self,
        top_level_asbiep_id: int,
        based_ascc_manifest_id: AsccManifestId,
    ) -> GetAsbieRow | None:
        """Handle get asbie by based ascc manifest id.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            based_ascc_manifest_id: ASCC manifest identifier for relationship lookup.

        Returns:
            Result of the operation.
        """
        owner_top = await self._build_top_level_info(top_level_asbiep_id)
        based_ascc = await self._build_ascc_info(based_ascc_manifest_id)
        if based_ascc is None:
            return None

        ascc_manifest = await self._session.get(AsccManifest, based_ascc_manifest_id)
        if ascc_manifest is None:
            return None

        to_asbiep = await self._get_asbiep_info(
            top_level_asbiep_id=top_level_asbiep_id,
            asbiep_id=None,
            asccp_manifest_id=AsccpManifestId(ascc_manifest.to_asccp_manifest_id),
            parent_asbie_path=None,
        )

        return GetAsbieRow(
            asbie_id=None,
            owner_top_level_asbiep=owner_top,
            guid=None,
            based_ascc=based_ascc,
            to_asbiep=to_asbiep,
            is_used=False,
            cardinality_min=based_ascc.cardinality_min,
            cardinality_max=based_ascc.cardinality_max,
            is_nillable=False,
            definition=None,
            remark=None,
            created_by=None,
            creation_timestamp=None,
            last_updated_by=None,
            last_update_timestamp=None,
        )

    async def get_bbie_by_bbie_id(self, *, bbie_id: int) -> GetBbieRow | None:
        """Handle get bbie by bbie id.

        Args:
            bbie_id: BBIE identifier.

        Returns:
            Result of the operation.
        """
        bbie = await self._session.get(Bbie, bbie_id)
        if bbie is None:
            return None

        owner_top = await self._build_top_level_info(bbie.owner_top_level_asbiep_id)
        based_bcc = await self._build_bcc_info(BccManifestId(bbie.based_bcc_manifest_id))
        if based_bcc is None:
            return None

        to_bbiep = await self._get_bbiep_info(
            owner_top_level_asbiep_id=bbie.owner_top_level_asbiep_id,
            bbie_id=bbie.bbie_id,
            bbiep_id=bbie.to_bbiep_id if bbie.to_bbiep_id is not None else None,
            bccp_manifest_id=BccpManifestId(based_bcc.to_bccp_manifest_id),
            parent_bbie_path=bbie.path,
        )

        return GetBbieRow(
            bbie_id=bbie.bbie_id,
            owner_top_level_asbiep=owner_top,
            guid=bbie.guid,
            based_bcc=based_bcc,
            to_bbiep=to_bbiep,
            is_used=bool(bbie.is_used),
            cardinality_min=bbie.cardinality_min,
            cardinality_max=bbie.cardinality_max if bbie.cardinality_max is not None else -1,
            is_nillable=bool(bbie.is_nillable),
            remark=bbie.remark,
            primitive_restriction=PrimitiveRestrictionRow(
                xbt_manifest_id=bbie.xbt_manifest_id,
                code_list_manifest_id=bbie.code_list_manifest_id,
                agency_id_list_manifest_id=bbie.agency_id_list_manifest_id,
            ),
            value_constraint=_value_constraint(bbie.default_value, bbie.fixed_value),
            facet=_facet_row(bbie.facet_min_length, bbie.facet_max_length, bbie.facet_pattern),
            created_by=bbie.created_by if bbie.created_by is not None else None,
            creation_timestamp=_as_dt(bbie.creation_timestamp) if bbie.creation_timestamp is not None else None,
            last_updated_by=bbie.last_updated_by if bbie.last_updated_by is not None else None,
            last_update_timestamp=_as_dt(bbie.last_update_timestamp) if bbie.last_update_timestamp is not None else None,
        )

    async def get_bbie_sc_owner_top_level(self, *, bbie_sc_id: int) -> TopLevelAsbiepInfoRow | None:
        """Return owner top-level information for a BBIE_SC.

        Args:
            bbie_sc_id: BBIE_SC identifier.

        Returns:
            Result of the operation.
        """
        bbie_sc = await self._session.get(BbieSc, bbie_sc_id)
        if bbie_sc is None:
            return None
        return await self._build_top_level_info(int(bbie_sc.owner_top_level_asbiep_id))

    async def get_bbie_by_based_bcc_manifest_id(
        self,
        top_level_asbiep_id: int,
        based_bcc_manifest_id: BccManifestId,
    ) -> GetBbieRow | None:
        """Handle get bbie by based bcc manifest id.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            based_bcc_manifest_id: BCC manifest identifier for relationship lookup.

        Returns:
            Result of the operation.
        """
        owner_top = await self._build_top_level_info(top_level_asbiep_id)
        based_bcc = await self._build_bcc_info(based_bcc_manifest_id)
        if based_bcc is None:
            return None

        bcc = await self._session.get(Bcc, based_bcc.bcc_id)

        to_bbiep = await self._get_bbiep_info(
            owner_top_level_asbiep_id=top_level_asbiep_id,
            bbie_id=None,
            bbiep_id=None,
            bccp_manifest_id=BccpManifestId(based_bcc.to_bccp_manifest_id),
            parent_bbie_path=None,
        )

        primitive = await self._get_default_primitive_for_bccp(BccpManifestId(based_bcc.to_bccp_manifest_id))

        return GetBbieRow(
            bbie_id=None,
            owner_top_level_asbiep=owner_top,
            guid=None,
            based_bcc=based_bcc,
            to_bbiep=to_bbiep,
            is_used=False,
            cardinality_min=based_bcc.cardinality_min,
            cardinality_max=based_bcc.cardinality_max,
            is_nillable=bool(based_bcc.is_nillable),
            remark=None,
            primitive_restriction=primitive,
            value_constraint=_value_constraint(bcc.default_value if bcc else None, bcc.fixed_value if bcc else None),
            facet=None,
            created_by=None,
            creation_timestamp=None,
            last_updated_by=None,
            last_update_timestamp=None,
        )

    async def _load_business_contexts(self, top_level_ids: Sequence[int]) -> dict[int, list[BizCtxSummaryRow]]:
        """Internal helper for load business contexts.

        Args:
            top_level_ids: Value for `top_level_ids`.

        Returns:
            Result of the operation.
        """
        if not top_level_ids:
            return {}
        biz_ctx_stmt = (
            select(BizCtxAssignment.top_level_asbiep_id, BizCtx.biz_ctx_id, BizCtx.guid, BizCtx.name)
            .select_from(BizCtxAssignment)
            .join(BizCtx, BizCtxAssignment.biz_ctx_id == BizCtx.biz_ctx_id)
            .where(BizCtxAssignment.top_level_asbiep_id.in_([x for x in top_level_ids]))
            .order_by(BizCtxAssignment.top_level_asbiep_id.asc(), BizCtx.biz_ctx_id.asc())
        )
        contexts: dict[int, list[BizCtxSummaryRow]] = {tid: [] for tid in top_level_ids}
        for row in (await self._session.execute(biz_ctx_stmt)).all():
            m = row._mapping
            top_level_id = int(m["top_level_asbiep_id"])
            contexts.setdefault(top_level_id, []).append(
                BizCtxSummaryRow(
                    biz_ctx_id=m["biz_ctx_id"],
                    guid=str(m["guid"]),
                    name=str(m["name"]) if m.get("name") is not None else "",
                )
            )
        return contexts

    async def _build_top_level_info(self, top_level_asbiep_id: int) -> TopLevelAsbiepInfoRow:
        """Internal helper for build top level info.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.

        Returns:
            Result of the operation.
        """
        row = (
            await self._session.execute(
                select(
                    TopLevelAsbiep,
                    Asbiep.created_by.label("asbiep_created_by"),
                    Asbiep.creation_timestamp.label("asbiep_creation_timestamp"),
                    Release.release_id,
                    Release.release_num,
                    Release.state.label("release_state"),
                    Library.library_id,
                    Library.name.label("library_name"),
                )
                .select_from(TopLevelAsbiep)
                .join(Asbiep, Asbiep.asbiep_id == TopLevelAsbiep.asbiep_id)
                .join(Release, Release.release_id == TopLevelAsbiep.release_id)
                .join(Library, Library.library_id == Release.library_id)
                .where(TopLevelAsbiep.top_level_asbiep_id == top_level_asbiep_id)
            )
        ).first()
        if row is None:
            raise LookupError(
                f"Top-level ASBIEP {top_level_asbiep_id} not found. "
                "Please verify the identifier and try again."
            )
        m = row._mapping
        top = m[TopLevelAsbiep]
        return TopLevelAsbiepInfoRow(
            top_level_asbiep_id=top.top_level_asbiep_id,
            library=LibrarySummaryRow(library_id=m["library_id"], name=str(m["library_name"])),
            release=ReleaseSummaryRow(
                release_id=m["release_id"],
                release_num=str(m["release_num"]) if m.get("release_num") is not None else None,
                state=str(m["release_state"]),
            ),
            version=top.version,
            status=top.status,
            state=top.state,
            is_deprecated=bool(top.is_deprecated),
            deprecated_reason=top.deprecated_reason,
            deprecated_remark=top.deprecated_remark,
            owner_user_id=top.owner_user_id,
            created_by=(
                m["asbiep_created_by"] if m.get("asbiep_created_by") is not None else None
            ),
            creation_timestamp=_as_dt(m["asbiep_creation_timestamp"]) if m.get("asbiep_creation_timestamp") is not None else None,
            last_updated_by=top.last_updated_by if top.last_updated_by is not None else None,
            last_update_timestamp=_as_dt(top.last_update_timestamp) if top.last_update_timestamp is not None else None,
        )

    async def _build_acc_info(self, acc_manifest_id: AccManifestId) -> AccInfoRow:
        """Internal helper for build acc info.

        Args:
            acc_manifest_id: ACC manifest identifier.

        Returns:
            Result of the operation.
        """
        row = await self._core_component_repo.get_acc(acc_manifest_id)
        if row is None:
            raise LookupError(f"ACC manifest {acc_manifest_id} not found. Please verify the identifier and try again.")
        return AccInfoRow(
            acc_manifest_id=row.acc_manifest_id,
            acc_id=row.acc_id,
            guid=str(row.guid),
            den=str(row.den),
            object_class_term=str(row.object_class_term or ""),
            definition=row.definition,
            definition_source=row.definition_source,
            is_deprecated=bool(row.is_deprecated),
        )

    async def _build_asccp_info(self, asccp_manifest_id: AsccpManifestId) -> AsccpInfoRow:
        """Internal helper for build asccp info.

        Args:
            asccp_manifest_id: ASCCP manifest identifier.

        Returns:
            Result of the operation.
        """
        row = await self._core_component_repo.get_asccp(asccp_manifest_id)
        if row is None:
            raise LookupError(
                f"ASCCP manifest {asccp_manifest_id} not found. Please verify the identifier and try again."
            )
        if row.role_of_acc is None:
            raise LookupError(
                f"ASCCP manifest {asccp_manifest_id} does not have role-of ACC. "
                "Please verify the manifest data and try again."
            )
        return AsccpInfoRow(
            asccp_manifest_id=row.asccp_manifest_id,
            asccp_id=row.asccp_id,
            role_of_acc_manifest_id=row.role_of_acc.acc_manifest_id,
            guid=str(row.guid),
            den=str(row.den) if row.den is not None else "",
            property_term=str(row.property_term) if row.property_term is not None else None,
            definition=row.definition,
            definition_source=row.definition_source,
            is_deprecated=bool(row.is_deprecated),
        )

    async def _build_ascc_info(self, ascc_manifest_id: AsccManifestId) -> AsccInfoRow | None:
        """Internal helper for build ascc info.

        Args:
            ascc_manifest_id: Value for `ascc_manifest_id`.

        Returns:
            Result of the operation.
        """
        manifest = await self._session.get(AsccManifest, ascc_manifest_id)
        if manifest is None:
            return None
        acc_row = await self._core_component_repo.get_acc(manifest.from_acc_manifest_id)
        if acc_row is None:
            return None
        relationship = next(
            (
                rel
                for rel in acc_row.relationships
                if isinstance(rel, AsccRelationshipInfoRow) and rel.ascc_manifest_id == ascc_manifest_id
            ),
            None,
        )
        if relationship is None:
            return None
        return AsccInfoRow(
            ascc_manifest_id=relationship.ascc_manifest_id,
            ascc_id=relationship.ascc_id,
            guid=str(relationship.guid),
            den=str(relationship.den),
            cardinality_min=relationship.cardinality_min,
            cardinality_max=relationship.cardinality_max,
            is_deprecated=bool(relationship.is_deprecated),
            definition=relationship.definition,
            definition_source=relationship.definition_source,
            from_acc_manifest_id=relationship.from_acc.acc_manifest_id,
            to_asccp_manifest_id=relationship.to_asccp.asccp_manifest_id,
        )

    async def _build_bcc_info(self, bcc_manifest_id: BccManifestId) -> BccInfoRow | None:
        """Internal helper for build bcc info.

        Args:
            bcc_manifest_id: Value for `bcc_manifest_id`.

        Returns:
            Result of the operation.
        """
        manifest = await self._session.get(BccManifest, bcc_manifest_id)
        if manifest is None:
            return None
        acc_row = await self._core_component_repo.get_acc(manifest.from_acc_manifest_id)
        if acc_row is None:
            return None
        relationship = next(
            (
                rel
                for rel in acc_row.relationships
                if isinstance(rel, BccRelationshipInfoRow) and rel.bcc_manifest_id == bcc_manifest_id
            ),
            None,
        )
        if relationship is None:
            return None
        return BccInfoRow(
            bcc_manifest_id=relationship.bcc_manifest_id,
            bcc_id=relationship.bcc_id,
            guid=str(relationship.guid),
            den=str(relationship.den),
            cardinality_min=relationship.cardinality_min,
            cardinality_max=relationship.cardinality_max,
            entity_type=relationship.entity_type,
            is_nillable=bool(relationship.is_nillable),
            is_deprecated=bool(relationship.is_deprecated),
            definition=relationship.definition,
            definition_source=relationship.definition_source,
            from_acc_manifest_id=relationship.from_acc.acc_manifest_id,
            to_bccp_manifest_id=relationship.to_bccp.bccp_manifest_id,
        )

    async def _build_bccp_info(self, bccp_manifest_id: BccpManifestId) -> BccpInfoRow:
        """Internal helper for build bccp info.

        Args:
            bccp_manifest_id: BCCP manifest identifier.

        Returns:
            Result of the operation.
        """
        row = await self._core_component_repo.get_bccp(bccp_manifest_id)
        if row is None:
            raise LookupError(
                f"BCCP manifest {bccp_manifest_id} not found. Please verify the identifier and try again."
            )
        return BccpInfoRow(
            bccp_manifest_id=row.bccp_manifest_id,
            bccp_id=row.bccp_id,
            guid=str(row.guid),
            den=str(row.den),
            property_term=str(row.property_term),
            representation_term=str(row.representation_term),
            definition=row.definition,
            definition_source=row.definition_source,
            is_deprecated=bool(row.is_deprecated),
            bdt_manifest=DtSummaryRow(
                dt_manifest_id=row.bdt.dt_manifest_id,
                dt_id=row.bdt.dt_id,
                based_dt_manifest_id=(
                    row.bdt.based_dt_manifest_id if row.bdt.based_dt_manifest_id is not None else None
                ),
                guid=str(row.bdt.guid),
                den=str(row.bdt.den),
                data_type_term=row.bdt.data_type_term,
                qualifier=row.bdt.qualifier,
                representation_term=row.bdt.representation_term,
                six_digit_id=row.bdt.six_digit_id,
                definition=row.bdt.definition,
                definition_source=row.bdt.definition_source,
                content_component_definition=row.bdt.content_component_definition,
                is_deprecated=bool(row.bdt.is_deprecated),
                namespace=row.bdt.namespace,
                library=row.bdt.library,
                release=row.bdt.release,
            ),
        )

    async def _get_asbiep_info(
        self,
        top_level_asbiep_id: int,
        asbiep_id: int | None,
        asccp_manifest_id: AsccpManifestId,
        parent_asbie_path: str | None,
    ) -> AsbiepInfoRow:
        """Internal helper for get asbiep info.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            asbiep_id: Value for `asbiep_id`.
            asccp_manifest_id: ASCCP manifest identifier.
            parent_asbie_path: Value for `parent_asbie_path`.

        Returns:
            Result of the operation.
        """
        owner_top_level_asbiep = await self._build_top_level_info(top_level_asbiep_id)
        asccp_info = await self._build_asccp_info(asccp_manifest_id)

        asbiep_path = f"{parent_asbie_path}>ASCCP-{asccp_info.asccp_manifest_id}" if parent_asbie_path else f"ASCCP-{asccp_info.asccp_manifest_id}"
        abie_path = f"{asbiep_path}>ACC-{asccp_info.role_of_acc_manifest_id}"

        asbiep = await self._session.get(Asbiep, asbiep_id) if asbiep_id is not None else None

        if asbiep is not None and asbiep.role_of_abie_id is not None:
            abie = await self._session.get(Abie, asbiep.role_of_abie_id)
        else:
            abie = None

        if abie is not None:
            based_acc = await self._build_acc_info(AccManifestId(abie.based_acc_manifest_id))
            relationships = await self._get_abie_relationships(
                top_level_asbiep_id=top_level_asbiep_id,
                abie_id=abie.abie_id,
                acc_manifest_id=AccManifestId(abie.based_acc_manifest_id),
                abie_path=abie.path or abie_path,
            )
            abie_info = AbieInfoRow(
                abie_id=abie.abie_id,
                guid=abie.guid,
                path=abie.path or abie_path,
                hash_path=abie.hash_path or hashlib.sha256((abie.path or abie_path).encode()).hexdigest(),
                based_acc_manifest=based_acc,
                definition=abie.definition,
                remark=abie.remark,
                relationships=relationships,
                created_by=abie.created_by if abie.created_by is not None else None,
                creation_timestamp=_as_dt(abie.creation_timestamp) if abie.creation_timestamp is not None else None,
                last_updated_by=abie.last_updated_by if abie.last_updated_by is not None else None,
                last_update_timestamp=_as_dt(abie.last_update_timestamp) if abie.last_update_timestamp is not None else None,
            )
        else:
            based_acc = await self._build_acc_info(AccManifestId(asccp_info.role_of_acc_manifest_id))
            relationships = await self._get_abie_relationships(
                top_level_asbiep_id=top_level_asbiep_id,
                abie_id=None,
                acc_manifest_id=AccManifestId(asccp_info.role_of_acc_manifest_id),
                abie_path=abie_path,
            )
            abie_info = AbieInfoRow(
                abie_id=None,
                guid=None,
                path=abie_path,
                hash_path=hashlib.sha256(abie_path.encode()).hexdigest(),
                based_acc_manifest=based_acc,
                definition=None,
                remark=None,
                relationships=relationships,
                created_by=None,
                creation_timestamp=None,
                last_updated_by=None,
                last_update_timestamp=None,
            )

        if asbiep is None:
            return AsbiepInfoRow(
                asbiep_id=None,
                guid=None,
                owner_top_level_asbiep=owner_top_level_asbiep,
                based_asccp_manifest=asccp_info,
                path=asbiep_path,
                hash_path=hashlib.sha256(asbiep_path.encode()).hexdigest(),
                role_of_abie=abie_info,
                definition=None,
                remark=None,
                biz_term=None,
                display_name=None,
                created_by=None,
                creation_timestamp=None,
                last_updated_by=None,
                last_update_timestamp=None,
            )

        return AsbiepInfoRow(
            asbiep_id=asbiep.asbiep_id,
            guid=asbiep.guid,
            owner_top_level_asbiep=await self._build_top_level_info(int(asbiep.owner_top_level_asbiep_id or top_level_asbiep_id)),
            based_asccp_manifest=asccp_info,
            path=asbiep.path,
            hash_path=asbiep.hash_path,
            role_of_abie=abie_info,
            definition=asbiep.definition,
            remark=asbiep.remark,
            biz_term=asbiep.biz_term,
            display_name=asbiep.display_name,
            created_by=asbiep.created_by if asbiep.created_by is not None else None,
            creation_timestamp=_as_dt(asbiep.creation_timestamp) if asbiep.creation_timestamp is not None else None,
            last_updated_by=asbiep.last_updated_by if asbiep.last_updated_by is not None else None,
            last_update_timestamp=_as_dt(asbiep.last_update_timestamp) if asbiep.last_update_timestamp is not None else None,
        )

    async def _get_bbiep_info(
        self,
        owner_top_level_asbiep_id: int,
        bbie_id: int | None,
        bbiep_id: int | None,
        bccp_manifest_id: BccpManifestId,
        parent_bbie_path: str | None,
    ) -> BbiepInfoRow:
        """Internal helper for get bbiep info.

        Args:
            owner_top_level_asbiep_id: Value for `owner_top_level_asbiep_id`.
            bbie_id: BBIE identifier.
            bbiep_id: Value for `bbiep_id`.
            bccp_manifest_id: BCCP manifest identifier.
            parent_bbie_path: Value for `parent_bbie_path`.

        Returns:
            Result of the operation.
        """
        owner_top_level = await self._build_top_level_info(owner_top_level_asbiep_id)
        based_bccp = await self._build_bccp_info(bccp_manifest_id)

        bbiep_path = f"{parent_bbie_path}>BCCP-{based_bccp.bccp_manifest_id}" if parent_bbie_path else f"BCCP-{based_bccp.bccp_manifest_id}"
        hash_path = hashlib.sha256(bbiep_path.encode()).hexdigest()

        bbiep = await self._session.get(Bbiep, bbiep_id) if bbiep_id is not None else None
        if bbiep is not None:
            bbiep_path = bbiep.path or bbiep_path
            hash_path = bbiep.hash_path or hash_path

        supplementary_components = await self._get_bbie_sc_rows(
            owner_top_level_asbiep_id=owner_top_level_asbiep_id,
            bbie_id=bbie_id,
            bdt_manifest_id=await self._resolve_bdt_manifest_id(BccpManifestId(based_bccp.bccp_manifest_id)),
            bbiep_path=bbiep_path,
        )

        return BbiepInfoRow(
            bbiep_id=bbiep.bbiep_id if bbiep is not None else None,
            guid=bbiep.guid if bbiep is not None else None,
            based_bccp=based_bccp,
            path=bbiep_path,
            hash_path=hash_path,
            definition=bbiep.definition if bbiep is not None else None,
            remark=bbiep.remark if bbiep is not None else None,
            biz_term=bbiep.biz_term if bbiep is not None else None,
            display_name=bbiep.display_name if bbiep is not None else None,
            supplementary_components=supplementary_components,
            owner_top_level_asbiep=owner_top_level,
            created_by=bbiep.created_by if bbiep is not None else None,
            creation_timestamp=_as_dt(bbiep.creation_timestamp) if bbiep is not None and bbiep.creation_timestamp is not None else None,
            last_updated_by=bbiep.last_updated_by if bbiep is not None else None,
            last_update_timestamp=_as_dt(bbiep.last_update_timestamp) if bbiep is not None and bbiep.last_update_timestamp is not None else None,
        )

    async def _resolve_bdt_manifest_id(self, bccp_manifest_id: BccpManifestId) -> DataTypeManifestId:
        """Internal helper for resolve bdt manifest id.

        Args:
            bccp_manifest_id: BCCP manifest identifier.

        Returns:
            Result of the operation.
        """
        row = (
            await self._session.execute(
                select(BccpManifest.bdt_manifest_id).where(BccpManifest.bccp_manifest_id == bccp_manifest_id)
            )
        ).first()
        if row is None:
            raise LookupError(
                f"BCCP manifest {bccp_manifest_id} not found. Please verify the identifier and try again."
            )
        return DataTypeManifestId(row[0])

    async def _get_bbie_sc_rows(
        self,
        owner_top_level_asbiep_id: int,
        bbie_id: int | None,
        bdt_manifest_id: DataTypeManifestId,
        bbiep_path: str,
    ) -> list[BbieScInfoRow]:
        """Internal helper for get bbie sc rows.

        Args:
            owner_top_level_asbiep_id: Value for `owner_top_level_asbiep_id`.
            bbie_id: BBIE identifier.
            bdt_manifest_id: Value for `bdt_manifest_id`.
            bbiep_path: Value for `bbiep_path`.

        Returns:
            Result of the operation.
        """
        dt_sc_manifest_rows = (
            await self._session.execute(
                select(DtScManifest, DtSc)
                .join(DtSc, DtSc.dt_sc_id == DtScManifest.dt_sc_id)
                .where(DtScManifest.owner_dt_manifest_id == bdt_manifest_id)
                .order_by(DtScManifest.dt_sc_manifest_id.asc())
            )
        ).all()

        bbie_sc_map: dict[int, BbieSc] = {}
        if bbie_id is not None:
            for item in (
                await self._session.execute(select(BbieSc).where(BbieSc.bbie_id == bbie_id))
            ).scalars().all():
                bbie_sc_map[item.based_dt_sc_manifest_id] = item

        owner_top = await self._build_top_level_info(owner_top_level_asbiep_id)
        result: list[BbieScInfoRow] = []
        for manifest, dt_sc in dt_sc_manifest_rows:
            path = self._build_bbie_sc_path(
                bbiep_path=bbiep_path,
                bdt_manifest_id=DataTypeManifestId(manifest.owner_dt_manifest_id),
                based_dt_sc_manifest_id=DataTypeSupplementaryComponentManifestId(manifest.dt_sc_manifest_id),
            )
            hash_path = hashlib.sha256(path.encode()).hexdigest()
            bbie_sc = bbie_sc_map.get(manifest.dt_sc_manifest_id)
            primitive = await self._get_default_primitive_for_dt_sc(
                dt_sc_id=DataTypeSupplementaryComponentId(dt_sc.dt_sc_id),
                owner_dt_manifest_id=DataTypeManifestId(manifest.owner_dt_manifest_id),
            )
            if bbie_sc is not None:
                stored_primitive = PrimitiveRestrictionRow(
                    xbt_manifest_id=bbie_sc.xbt_manifest_id,
                    code_list_manifest_id=bbie_sc.code_list_manifest_id,
                    agency_id_list_manifest_id=bbie_sc.agency_id_list_manifest_id,
                )
                if (
                    stored_primitive.xbt_manifest_id is not None
                    or stored_primitive.code_list_manifest_id is not None
                    or stored_primitive.agency_id_list_manifest_id is not None
                ):
                    primitive = stored_primitive
            result.append(
                BbieScInfoRow(
                    bbie_sc_id=bbie_sc.bbie_sc_id if bbie_sc is not None else None,
                    guid=bbie_sc.guid if bbie_sc is not None else None,
                    based_dt_sc=DataTypeSupplementaryComponentRow(
                        dt_sc_manifest_id=manifest.dt_sc_manifest_id,
                        dt_sc_id=dt_sc.dt_sc_id,
                        guid=str(dt_sc.guid),
                        object_class_term=dt_sc.object_class_term,
                        property_term=dt_sc.property_term,
                        representation_term=dt_sc.representation_term,
                        definition=dt_sc.definition,
                        definition_source=dt_sc.definition_source,
                        cardinality_min=dt_sc.cardinality_min,
                        cardinality_max=dt_sc.cardinality_max if dt_sc.cardinality_max is not None else None,
                        is_deprecated=bool(dt_sc.is_deprecated),
                    ),
                    path=path,
                    hash_path=hash_path,
                    definition=bbie_sc.definition if bbie_sc is not None else None,
                    biz_term=bbie_sc.biz_term if bbie_sc is not None else None,
                    display_name=bbie_sc.display_name if bbie_sc is not None else None,
                    cardinality_min=bbie_sc.cardinality_min if bbie_sc is not None else dt_sc.cardinality_min,
                    cardinality_max=bbie_sc.cardinality_max if bbie_sc is not None else int(dt_sc.cardinality_max or 1),
                    primitive_restriction=primitive,
                    value_constraint=_value_constraint(
                        bbie_sc.default_value if bbie_sc is not None else dt_sc.default_value,
                        bbie_sc.fixed_value if bbie_sc is not None else dt_sc.fixed_value,
                    ),
                    facet=_facet_row(
                        bbie_sc.facet_min_length if bbie_sc is not None else None,
                        bbie_sc.facet_max_length if bbie_sc is not None else None,
                        bbie_sc.facet_pattern if bbie_sc is not None else None,
                    ),
                    owner_top_level_asbiep=owner_top,
                )
            )
        return result

    async def _get_default_primitive_for_bccp(self, bccp_manifest_id: BccpManifestId) -> PrimitiveRestrictionRow | None:
        """Internal helper for get default primitive for bccp.

        Args:
            bccp_manifest_id: BCCP manifest identifier.

        Returns:
            Result of the operation.
        """
        bccp_manifest = await self._session.get(BccpManifest, bccp_manifest_id)
        if bccp_manifest is None:
            return None

        dt_manifest = await self._session.get(DtManifest, bccp_manifest.bdt_manifest_id)
        if dt_manifest is None:
            return None

        row = (
            await self._session.execute(
                select(DtAwdPri)
                .where(
                    DtAwdPri.release_id == bccp_manifest.release_id,
                    DtAwdPri.dt_id == dt_manifest.dt_id,
                    DtAwdPri.is_default == True,
                )
                .order_by(DtAwdPri.dt_awd_pri_id.desc())
            )
        ).scalars().first()
        if row is None:
            return None
        return PrimitiveRestrictionRow(
            xbt_manifest_id=row.xbt_manifest_id,
            code_list_manifest_id=row.code_list_manifest_id,
            agency_id_list_manifest_id=row.agency_id_list_manifest_id,
        )

    async def _get_default_primitive_for_dt_manifest(
        self,
        dt_manifest_id: DataTypeManifestId,
    ) -> PrimitiveRestrictionRow | None:
        """Resolve default primitive restriction for a DT manifest.

        Args:
            dt_manifest_id: DT manifest identifier.

        Returns:
            Result of the operation.
        """
        dt_manifest = await self._session.get(DtManifest, dt_manifest_id)
        if dt_manifest is None:
            return None

        row = (
            await self._session.execute(
                select(DtAwdPri)
                .where(
                    DtAwdPri.release_id == dt_manifest.release_id,
                    DtAwdPri.dt_id == dt_manifest.dt_id,
                    DtAwdPri.is_default == True,
                )
                .order_by(DtAwdPri.dt_awd_pri_id.desc())
            )
        ).scalars().first()
        if row is None:
            return None
        return PrimitiveRestrictionRow(
            xbt_manifest_id=row.xbt_manifest_id,
            code_list_manifest_id=row.code_list_manifest_id,
            agency_id_list_manifest_id=row.agency_id_list_manifest_id,
        )

    async def _get_abie_relationships(
        self,
        top_level_asbiep_id: int,
        abie_id: int | None,
        acc_manifest_id: AccManifestId,
        abie_path: str,
    ) -> list[AsbieRelationshipRow | BbieRelationshipRow]:
        """Internal helper for get abie relationships.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            abie_id: Value for `abie_id`.
            acc_manifest_id: ACC manifest identifier.
            abie_path: Value for `abie_path`.

        Returns:
            Result of the operation.
        """
        acc_queue: list[tuple[str, AccManifestId]] = []
        current_acc_manifest_id = acc_manifest_id
        current_path = abie_path
        while True:
            acc_row = await self._session.get(AccManifest, current_acc_manifest_id)
            if acc_row is None:
                break
            acc_queue.insert(0, (current_path, AccManifestId(acc_row.acc_manifest_id)))
            if acc_row.based_acc_manifest_id is None:
                break
            current_acc_manifest_id = AccManifestId(acc_row.based_acc_manifest_id)
            current_path = f"{current_path}>ACC-{current_acc_manifest_id}"

        rels: list[AsbieRelationshipRow | BbieRelationshipRow] = []
        for current_abie_path, current_acc_manifest_id in acc_queue:
            cc_relationships = await self._get_cc_relationships_for_acc(current_acc_manifest_id, current_abie_path)
            for rel in cc_relationships:
                if isinstance(rel, AsbieRelationshipRow):
                    # to_asccp_manifest_id here is ASCCP manifest id; resolve role ACC via AsccpManifest
                    asccp_manifest = await self._session.get(AsccpManifest, rel.based_ascc.to_asccp_manifest_id)
                    role_acc_manifest_id = asccp_manifest.role_of_acc_manifest_id
                    role_acc = (
                        await self._session.execute(
                            select(Acc)
                            .join(AccManifest, AccManifest.acc_id == Acc.acc_id)
                            .where(AccManifest.acc_manifest_id == role_acc_manifest_id)
                        )
                    ).scalar_one_or_none()
                    if role_acc is not None and int(role_acc.oagis_component_type or 0) in (3, 4):
                        asbiep_path = f"{rel.path}>ASCCP-{rel.based_ascc.to_asccp_manifest_id}"
                        role_of_abie_path = f"{asbiep_path}>ACC-{role_acc_manifest_id}"
                        rels.extend(
                            await self._get_abie_relationships(
                                top_level_asbiep_id=top_level_asbiep_id,
                                abie_id=abie_id,
                                acc_manifest_id=AccManifestId(role_acc_manifest_id),
                                abie_path=role_of_abie_path,
                            )
                        )
                    else:
                        rels.append(rel)
                else:
                    rels.append(rel)

        asbie_map: dict[str, Asbie] = {}
        bbie_map: dict[str, Bbie] = {}
        if abie_id is not None:
            for item in (
                await self._session.execute(select(Asbie).where(Asbie.from_abie_id == abie_id))
            ).scalars().all():
                if item.hash_path:
                    asbie_map[item.hash_path] = item
            for item in (
                await self._session.execute(select(Bbie).where(Bbie.from_abie_id == abie_id))
            ).scalars().all():
                if item.hash_path:
                    bbie_map[item.hash_path] = item

        attributes: list[BbieRelationshipRow] = []
        elements: list[AsbieRelationshipRow | BbieRelationshipRow] = []
        for rel in rels:
            if isinstance(rel, AsbieRelationshipRow):
                stored = asbie_map.get(rel.hash_path)
                if stored is not None:
                    rel = AsbieRelationshipRow(
                        component_type="ASBIE",
                        asbie_id=stored.asbie_id,
                        guid=stored.guid,
                        based_ascc=rel.based_ascc,
                        to_asbiep_id=stored.to_asbiep_id if stored.to_asbiep_id is not None else None,
                        is_used=bool(stored.is_used),
                        path=stored.path or rel.path,
                        hash_path=stored.hash_path or rel.hash_path,
                        cardinality_min=stored.cardinality_min,
                        cardinality_max=stored.cardinality_max,
                        is_nillable=bool(stored.is_nillable),
                        remark=stored.remark,
                    )
                elements.append(rel)
            else:
                stored = bbie_map.get(rel.hash_path)
                if stored is not None:
                    rel = BbieRelationshipRow(
                        component_type="BBIE",
                        bbie_id=stored.bbie_id,
                        guid=stored.guid,
                        based_bcc=rel.based_bcc,
                        to_bbiep_id=stored.to_bbiep_id if stored.to_bbiep_id is not None else None,
                        is_used=bool(stored.is_used),
                        path=stored.path or rel.path,
                        hash_path=stored.hash_path or rel.hash_path,
                        cardinality_min=stored.cardinality_min,
                        cardinality_max=stored.cardinality_max if stored.cardinality_max is not None else -1,
                        is_nillable=bool(stored.is_nillable),
                        remark=stored.remark,
                        primitive_restriction=PrimitiveRestrictionRow(
                            xbt_manifest_id=stored.xbt_manifest_id,
                            code_list_manifest_id=stored.code_list_manifest_id,
                            agency_id_list_manifest_id=stored.agency_id_list_manifest_id,
                        ),
                        value_constraint=_value_constraint(stored.default_value, stored.fixed_value),
                        facet=_facet_row(stored.facet_min_length, stored.facet_max_length, stored.facet_pattern),
                    )
                if rel.based_bcc.entity_type == "Attribute":
                    attributes.append(rel)
                else:
                    elements.append(rel)

        return [*attributes, *elements]

    async def _get_cc_relationships_for_acc(
        self,
        acc_manifest_id: AccManifestId,
        base_abie_path: str,
    ) -> list[AsbieRelationshipRow | BbieRelationshipRow]:
        """Internal helper for get cc relationships for acc.

        Args:
            acc_manifest_id: ACC manifest identifier.
            base_abie_path: Value for `base_abie_path`.

        Returns:
            Result of the operation.
        """
        cc_relationships = await self._core_component_repo.get_acc_relationships(acc_manifest_id)
        rels: list[AsbieRelationshipRow | BbieRelationshipRow] = []
        for relationship in cc_relationships:
            if isinstance(relationship, AsccRelationshipInfoRow):
                path = f"{base_abie_path}>ASCC-{relationship.ascc_manifest_id}"
                rels.append(
                    AsbieRelationshipRow(
                        component_type="ASBIE",
                        asbie_id=None,
                        guid=None,
                        based_ascc=AsccInfoRow(
                            ascc_manifest_id=relationship.ascc_manifest_id,
                            ascc_id=relationship.ascc_id,
                            guid=relationship.guid,
                            den=relationship.den,
                            cardinality_min=relationship.cardinality_min,
                            cardinality_max=relationship.cardinality_max,
                            is_deprecated=relationship.is_deprecated,
                            definition=relationship.definition,
                            definition_source=relationship.definition_source,
                            from_acc_manifest_id=relationship.from_acc.acc_manifest_id,
                            to_asccp_manifest_id=relationship.to_asccp.asccp_manifest_id,
                        ),
                        to_asbiep_id=None,
                        is_used=False,
                        path=path,
                        hash_path=hashlib.sha256(path.encode()).hexdigest(),
                        cardinality_min=relationship.cardinality_min,
                        cardinality_max=relationship.cardinality_max,
                        is_nillable=False,
                        remark=None,
                    )
                )
                continue

            path = f"{base_abie_path}>BCC-{relationship.bcc_manifest_id}"
            rels.append(
                BbieRelationshipRow(
                    component_type="BBIE",
                    bbie_id=None,
                    guid=None,
                    based_bcc=BccInfoRow(
                        bcc_manifest_id=relationship.bcc_manifest_id,
                        bcc_id=relationship.bcc_id,
                        guid=relationship.guid,
                        den=relationship.den,
                        cardinality_min=relationship.cardinality_min,
                        cardinality_max=relationship.cardinality_max,
                        entity_type=relationship.entity_type,
                        is_nillable=relationship.is_nillable,
                        is_deprecated=relationship.is_deprecated,
                        definition=relationship.definition,
                        definition_source=relationship.definition_source,
                        from_acc_manifest_id=relationship.from_acc.acc_manifest_id,
                        to_bccp_manifest_id=relationship.to_bccp.bccp_manifest_id,
                    ),
                    to_bbiep_id=None,
                    is_used=False,
                    path=path,
                    hash_path=hashlib.sha256(path.encode()).hexdigest(),
                    cardinality_min=relationship.cardinality_min,
                    cardinality_max=relationship.cardinality_max,
                    is_nillable=relationship.is_nillable,
                    remark=None,
                    primitive_restriction=await self._get_default_primitive_for_bccp(
                        relationship.to_bccp.bccp_manifest_id
                    ),
                    value_constraint=_value_constraint(
                        relationship.value_constraint.default_value if relationship.value_constraint else None,
                        relationship.value_constraint.fixed_value if relationship.value_constraint else None,
                    ),
                    facet=None,
                )
            )

        return rels

    async def create_top_level_asbiep(
        self,
        asccp_manifest_id: AsccpManifestId,
        biz_ctx_ids: list[BizCtxId],
        requester_user_id: AppUserId,
    ) -> int:
        """Create top-level ASBIEP, root ABIE, and root ASBIEP records.

        Args:
            asccp_manifest_id: ASCCP manifest identifier.
            biz_ctx_ids: Business context identifiers to assign.
            requester_user_id: Requesting user identifier.

        Returns:
            Result of the operation.
        """
        if not biz_ctx_ids:
            raise ValueError(
                "At least one business context ID is required. "
                "Please provide one or more business context IDs and try again."
            )

        manifest_row = (
            await self._session.execute(
                select(AsccpManifest, AccManifest, Acc)
                .join(AccManifest, AccManifest.acc_manifest_id == AsccpManifest.role_of_acc_manifest_id)
                .join(Acc, Acc.acc_id == AccManifest.acc_id)
                .where(AsccpManifest.asccp_manifest_id == asccp_manifest_id)
            )
        ).first()
        if manifest_row is None:
            raise LookupError(self._not_found_message("ASCCP manifest", asccp_manifest_id))
        asccp_manifest, role_of_acc_manifest, acc = manifest_row
        if int(acc.oagis_component_type or 1) in (3, 4):
            raise ValueError(
                "Cannot create BIE from ASCCP whose role-of ACC is a group type. "
                "Please choose an ASCCP whose role-of ACC is not a group type."
            )

        existing_biz_ctx = (
            await self._session.execute(select(BizCtx.biz_ctx_id).where(BizCtx.biz_ctx_id.in_([x for x in biz_ctx_ids])))
        ).scalars().all()
        missing = sorted(set(x for x in biz_ctx_ids) - set(x for x in existing_biz_ctx))
        if missing:
            raise LookupError(
                f"No business context exists with ID(s): {', '.join(str(v) for v in missing)}. "
                "Please verify the identifier values and try again."
            )

        now = datetime.now(timezone.utc).replace(tzinfo=None)
        top_level = TopLevelAsbiep(
            asbiep_id=None,
            owner_user_id=requester_user_id,
            last_updated_by=requester_user_id,
            release_id=asccp_manifest.release_id,
            version=None,
            status=None,
            state="WIP",
            is_deprecated=False,
            deprecated_reason=None,
            deprecated_remark=None,
            last_update_timestamp=now,
        )
        self._session.add(top_level)
        await self._session.flush()

        for biz_ctx_id in sorted(set(x for x in biz_ctx_ids)):
            self._session.add(BizCtxAssignment(top_level_asbiep_id=top_level.top_level_asbiep_id, biz_ctx_id=biz_ctx_id))

        asbiep_path = f"ASCCP-{asccp_manifest.asccp_manifest_id}"
        abie_path = f"{asbiep_path}>ACC-{role_of_acc_manifest.acc_manifest_id}"
        abie = Abie(
            guid=new_guid(),
            based_acc_manifest_id=role_of_acc_manifest.acc_manifest_id,
            path=abie_path,
            hash_path=hashlib.sha256(abie_path.encode()).hexdigest(),
            definition=None,
            remark=None,
            biz_term=None,
            owner_top_level_asbiep_id=top_level.top_level_asbiep_id,
            created_by=requester_user_id,
            creation_timestamp=now,
            last_updated_by=requester_user_id,
            last_update_timestamp=now,
        )
        self._session.add(abie)
        await self._session.flush()

        asbiep = Asbiep(
            guid=new_guid(),
            based_asccp_manifest_id=asccp_manifest.asccp_manifest_id,
            path=asbiep_path,
            hash_path=hashlib.sha256(asbiep_path.encode()).hexdigest(),
            role_of_abie_id=abie.abie_id,
            display_name=None,
            definition=None,
            biz_term=None,
            remark=None,
            last_updated_by=requester_user_id,
            created_by=requester_user_id,
            creation_timestamp=now,
            last_update_timestamp=now,
            owner_top_level_asbiep_id=top_level.top_level_asbiep_id,
        )
        self._session.add(asbiep)
        await self._session.flush()

        top_level.asbiep_id = asbiep.asbiep_id
        return top_level.top_level_asbiep_id

    async def get_release_summary_by_asccp_manifest_id(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
    ) -> ReleaseSummaryRow | None:
        """Resolve the release summary for an ASCCP manifest."""
        row = (
            await self._session.execute(
                select(
                    Release.release_id,
                    Release.release_num,
                    Release.state,
                )
                .join(AsccpManifest, AsccpManifest.release_id == Release.release_id)
                .where(AsccpManifest.asccp_manifest_id == asccp_manifest_id)
            )
        ).first()
        if row is None:
            return None

        release_id, release_num, state = row
        return ReleaseSummaryRow(
            release_id=int(release_id),
            release_num=str(release_num) if release_num is not None else None,
            state=str(state) if state is not None else None,
        )

    async def update_top_level_asbiep(
        self,
        top_level_asbiep_id: int,
        requester_user_id: AppUserId,
        is_admin: bool,
        version: str | None = None,
        status: str | None = None,
        display_name: str | None = None,
        biz_term: str | None = None,
        definition: str | None = None,
        remark: str | None = None,
        is_deprecated: bool | None = None,
        deprecated_reason: str | None = None,
        deprecated_remark: str | None = None,
    ) -> list[str]:
        """Update mutable top-level ASBIEP and root ASBIEP fields.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            requester_user_id: Requesting user identifier.
            is_admin: Whether requester has admin role.
            version: Version value.
            status: Status value.
            display_name: ASBIEP display name.
            biz_term: ASBIEP business term.
            definition: ASBIEP definition.
            remark: ASBIEP remark.
            is_deprecated: Deprecation flag.
            deprecated_reason: Deprecation reason.
            deprecated_remark: Deprecation remark.

        Returns:
            Result of the operation.
        """
        top_level, asbiep = await self._get_top_level_with_root_asbiep(top_level_asbiep_id=top_level_asbiep_id)

        updates: list[str] = []
        if version is not None:
            top_level.version = version
            updates.append("version")
        if status is not None:
            top_level.status = status
            updates.append("status")
        if is_deprecated is not None:
            top_level.is_deprecated = bool(is_deprecated)
            updates.append("is_deprecated")
            if is_deprecated:
                if deprecated_reason is not None:
                    top_level.deprecated_reason = deprecated_reason
                    updates.append("deprecated_reason")
                if deprecated_remark is not None:
                    top_level.deprecated_remark = deprecated_remark
                    updates.append("deprecated_remark")
            else:
                top_level.deprecated_reason = None
                top_level.deprecated_remark = None
                updates.extend(["deprecated_reason", "deprecated_remark"])

        if display_name is not None:
            asbiep.display_name = display_name
            updates.append("display_name")
        if biz_term is not None:
            asbiep.biz_term = biz_term
            updates.append("biz_term")
        if definition is not None:
            asbiep.definition = definition
            updates.append("definition")
        if remark is not None:
            asbiep.remark = remark
            updates.append("remark")

        if updates:
            now = datetime.now(timezone.utc).replace(tzinfo=None)
            top_level.last_updated_by = requester_user_id
            top_level.last_update_timestamp = now
            asbiep.last_updated_by = requester_user_id
            asbiep.last_update_timestamp = now
        return sorted(set(updates))

    async def update_top_level_asbiep_state(
        self,
        top_level_asbiep_id: int,
        requester_user_id: AppUserId,
        is_admin: bool,
        new_state: str,
    ) -> tuple[str, str]:
        """Transition top-level ASBIEP state.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            requester_user_id: Requesting user identifier.
            is_admin: Whether requester has admin role.
            new_state: Target state.

        Returns:
            Result of the operation.
        """
        top_level = await self._session.get(TopLevelAsbiep, top_level_asbiep_id)
        if top_level is None:
            raise LookupError(self._not_found_message("top-level ASBIEP", top_level_asbiep_id))

        current_state = str(top_level.state or "")
        valid_transitions = {
            "WIP": {"QA"},
            "QA": {"WIP", "Production"},
            "Production": {"WIP", "QA"} if is_admin else set(),
        }
        if current_state not in valid_transitions:
            raise ValueError(
                f"Unsupported current state: {current_state}. "
                "Please verify the stored lifecycle state and try again."
            )
        if new_state not in valid_transitions[current_state]:
            if current_state == "Production":
                if not is_admin:
                    raise PermissionError(
                        "Top-level ASBIEP in Production state cannot be changed."
                    )
                raise ValueError(
                    f"Invalid state transition from {current_state} to {new_state}. Allowed: QA, WIP. "
                    "Please choose one of the allowed target states and try again."
                )
            allowed = ", ".join(sorted(valid_transitions[current_state]))
            raise ValueError(
                f"Invalid state transition from {current_state} to {new_state}. Allowed: {allowed}. "
                "Please choose one of the allowed target states and try again."
            )

        top_level.state = new_state
        top_level.last_updated_by = requester_user_id
        top_level.last_update_timestamp = datetime.now(timezone.utc).replace(tzinfo=None)
        return current_state, new_state

    async def delete_top_level_asbiep(
        self,
        top_level_asbiep_id: int,
        requester_user_id: AppUserId,
        is_admin: bool,
    ) -> None:
        """Delete top-level ASBIEP and owned descendants.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            requester_user_id: Requesting user identifier.
            is_admin: Whether requester has admin role.
        """
        top_level = await self._session.get(TopLevelAsbiep, top_level_asbiep_id)
        if top_level is None:
            raise LookupError(self._not_found_message("top-level ASBIEP", top_level_asbiep_id))

        await self._session.execute(text("SET foreign_key_checks = 0"))
        try:
            await self._session.execute(sa_delete(BizCtxAssignment).where(BizCtxAssignment.top_level_asbiep_id == top_level_asbiep_id))
            await self._session.execute(sa_delete(Asbie).where(Asbie.owner_top_level_asbiep_id == top_level_asbiep_id))
            await self._session.execute(sa_delete(BbieSc).where(BbieSc.owner_top_level_asbiep_id == top_level_asbiep_id))
            await self._session.execute(sa_delete(Bbie).where(Bbie.owner_top_level_asbiep_id == top_level_asbiep_id))
            await self._session.execute(sa_delete(Bbiep).where(Bbiep.owner_top_level_asbiep_id == top_level_asbiep_id))
            await self._session.execute(sa_delete(Asbiep).where(Asbiep.owner_top_level_asbiep_id == top_level_asbiep_id))
            await self._session.execute(sa_delete(Abie).where(Abie.owner_top_level_asbiep_id == top_level_asbiep_id))
            await self._session.delete(top_level)
        finally:
            await self._session.execute(text("SET foreign_key_checks = 1"))

    async def list_reusing_top_level_asbiep_ids(self, *, top_level_asbiep_id: int) -> list[int]:
        """Return top-level ASBIEP ids that currently reuse the target top-level ASBIEP."""
        stmt = (
            select(Asbie.owner_top_level_asbiep_id)
            .join(Asbiep, Asbie.to_asbiep_id == Asbiep.asbiep_id)
            .where(
                Asbie.owner_top_level_asbiep_id != top_level_asbiep_id,
                Asbiep.owner_top_level_asbiep_id == top_level_asbiep_id,
            )
            .distinct()
        )
        return [int(value) for value in (await self._session.execute(stmt)).scalars().all()]

    async def list_reused_top_level_asbiep_ids(self, *, top_level_asbiep_id: int) -> list[int]:
        """Return top-level ASBIEP ids that the target top-level ASBIEP currently reuses."""
        stmt = (
            select(Asbiep.owner_top_level_asbiep_id)
            .join(Asbie, Asbie.to_asbiep_id == Asbiep.asbiep_id)
            .where(
                Asbie.owner_top_level_asbiep_id == top_level_asbiep_id,
                Asbiep.owner_top_level_asbiep_id.is_not(None),
                Asbiep.owner_top_level_asbiep_id != top_level_asbiep_id,
            )
            .distinct()
        )
        return [int(value) for value in (await self._session.execute(stmt)).scalars().all() if value is not None]

    async def list_assigned_code_list_manifest_ids(self, *, top_level_asbiep_id: int) -> list[int]:
        """Return code list manifest ids assigned by BBIE and BBIE_SC rows under the target top-level ASBIEP."""
        stmt = union(
            select(Bbie.code_list_manifest_id.label("code_list_manifest_id")).where(
                Bbie.owner_top_level_asbiep_id == top_level_asbiep_id,
                Bbie.code_list_manifest_id.is_not(None),
            ),
            select(BbieSc.code_list_manifest_id.label("code_list_manifest_id")).where(
                BbieSc.owner_top_level_asbiep_id == top_level_asbiep_id,
                BbieSc.code_list_manifest_id.is_not(None),
            ),
        )
        return [int(value) for value in (await self._session.execute(stmt)).scalars().all() if value is not None]

    async def has_top_level_asbiep_openapi_reference(self, *, top_level_asbiep_id: int) -> bool:
        """Return whether OAS request/response rows reference the target top-level ASBIEP."""
        oas_request_body_ref = await self._session.execute(
            select(OasRequest.oas_request_id)
            .join(OasMessageBody, OasRequest.oas_message_body_id == OasMessageBody.oas_message_body_id)
            .where(OasMessageBody.top_level_asbiep_id == top_level_asbiep_id)
            .limit(1)
        )
        if oas_request_body_ref.scalar_one_or_none() is not None:
            return True

        oas_response_body_ref = await self._session.execute(
            select(OasResponse.oas_response_id)
            .join(OasMessageBody, OasResponse.oas_message_body_id == OasMessageBody.oas_message_body_id)
            .where(OasMessageBody.top_level_asbiep_id == top_level_asbiep_id)
            .limit(1)
        )
        if oas_response_body_ref.scalar_one_or_none() is not None:
            return True

        oas_request_direct_ref = await self._session.execute(
            select(OasRequest.oas_request_id)
            .where(
                or_(
                    OasRequest.meta_header_top_level_asbiep_id == top_level_asbiep_id,
                    OasRequest.pagination_top_level_asbiep_id == top_level_asbiep_id,
                )
            )
            .limit(1)
        )
        if oas_request_direct_ref.scalar_one_or_none() is not None:
            return True

        oas_response_direct_ref = await self._session.execute(
            select(OasResponse.oas_response_id)
            .where(
                or_(
                    OasResponse.meta_header_top_level_asbiep_id == top_level_asbiep_id,
                    OasResponse.pagination_top_level_asbiep_id == top_level_asbiep_id,
                )
            )
            .limit(1)
        )
        return oas_response_direct_ref.scalar_one_or_none() is not None

    async def transfer_top_level_asbiep_ownership(
        self,
        top_level_asbiep_id: int,
        requester_user_id: AppUserId,
        is_admin: bool,
        target_user_id: AppUserId,
    ) -> None:
        """Transfer top-level ASBIEP ownership.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            requester_user_id: Requesting user identifier.
            is_admin: Whether requester has admin role.
            target_user_id: Target owner user identifier.
        """
        top_level = await self._session.get(TopLevelAsbiep, top_level_asbiep_id)
        if top_level is None:
            raise LookupError(f"No top-level ASBIEP exists with ID {top_level_asbiep_id}.")
        target_owner = await self._session.get(AppUser, target_user_id)
        if target_owner is None:
            raise LookupError(self._not_found_message("user", target_user_id))
        if target_owner.is_enabled is not None and not bool(target_owner.is_enabled):
            raise ValueError(
                "Cannot transfer ownership to a disabled user. "
                "Please choose an enabled user account and try again."
            )

        top_level.owner_user_id = target_user_id
        top_level.last_updated_by = requester_user_id
        top_level.last_update_timestamp = datetime.now(timezone.utc).replace(tzinfo=None)

    async def assign_biz_ctx_to_top_level_asbiep(
        self,
        top_level_asbiep_id: int,
        biz_ctx_id: BizCtxId,
        requester_user_id: AppUserId,
        is_admin: bool,
    ) -> bool:
        """Assign a business context to top-level ASBIEP.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            biz_ctx_id: Business context identifier.
            requester_user_id: Requesting user identifier.
            is_admin: Whether requester has admin role.
        """
        top_level = await self._session.get(TopLevelAsbiep, top_level_asbiep_id)
        if top_level is None:
            raise LookupError(self._not_found_message("top-level ASBIEP", top_level_asbiep_id))
        biz_ctx = await self._session.get(BizCtx, biz_ctx_id)
        if biz_ctx is None:
            raise LookupError(self._not_found_message("business context", biz_ctx_id))
        existing = (
            await self._session.execute(
                select(BizCtxAssignment).where(
                    BizCtxAssignment.top_level_asbiep_id == top_level_asbiep_id,
                    BizCtxAssignment.biz_ctx_id == biz_ctx_id,
                )
            )
        ).scalar_one_or_none()
        if existing is not None:
            return False
        self._session.add(BizCtxAssignment(top_level_asbiep_id=top_level_asbiep_id, biz_ctx_id=biz_ctx_id))
        return True

    async def unassign_biz_ctx_from_top_level_asbiep(
        self,
        top_level_asbiep_id: int,
        biz_ctx_id: BizCtxId,
        requester_user_id: AppUserId,
        is_admin: bool,
    ) -> bool:
        """Unassign a business context from top-level ASBIEP.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            biz_ctx_id: Business context identifier.
            requester_user_id: Requesting user identifier.
            is_admin: Whether requester has admin role.
        """
        top_level = await self._session.get(TopLevelAsbiep, top_level_asbiep_id)
        if top_level is None:
            raise LookupError(self._not_found_message("top-level ASBIEP", top_level_asbiep_id))
        result = await self._session.execute(
            sa_delete(BizCtxAssignment).where(
                BizCtxAssignment.top_level_asbiep_id == top_level_asbiep_id,
                BizCtxAssignment.biz_ctx_id == biz_ctx_id,
            )
        )
        return bool(result.rowcount)

    async def get_asbie_create_plan(self, *, from_abie_id: int, based_ascc_manifest_id: AsccManifestId) -> AsbieCreatePlanRow:
        """Load DB context required to create or enable ASBIE.

        Args:
            from_abie_id: Parent ABIE identifier.
            based_ascc_manifest_id: ASCC manifest identifier.

        Returns:
            Result of the operation.
        """
        from_abie = await self._session.get(Abie, from_abie_id)
        if from_abie is None:
            raise LookupError(self._not_found_message("ABIE", from_abie_id))
        top_level = await self._session.get(TopLevelAsbiep, from_abie.owner_top_level_asbiep_id)
        if top_level is None:
            raise LookupError(self._not_found_message("top-level ASBIEP", from_abie.owner_top_level_asbiep_id))
        ascc_manifest = await self._session.get(AsccManifest, based_ascc_manifest_id)
        if ascc_manifest is None:
            raise LookupError(self._not_found_message("ASCC manifest", based_ascc_manifest_id))
        ascc = await self._session.get(Ascc, ascc_manifest.ascc_id)
        if ascc is None:
            raise LookupError(self._not_found_message("ASCC", ascc_manifest.ascc_id))
        asccp_manifest = await self._session.get(AsccpManifest, ascc_manifest.to_asccp_manifest_id)
        if asccp_manifest is None:
            raise LookupError(self._not_found_message("ASCCP manifest", ascc_manifest.to_asccp_manifest_id))

        relationships = await self._get_abie_relationships(
            top_level_asbiep_id=top_level.top_level_asbiep_id,
            abie_id=from_abie_id,
            acc_manifest_id=from_abie.based_acc_manifest_id,
            abie_path=str(from_abie.path),
        )
        matching_asbie_rel = next(
            (
                rel
                for rel in relationships
                if isinstance(rel, AsbieRelationshipRow) and rel.based_ascc.ascc_manifest_id == based_ascc_manifest_id
            ),
            None,
        )
        if matching_asbie_rel is None:
            raise ValueError(
                "The requested ASCC is not available in the parent ABIE relationships. "
                "Please choose an ASCC that belongs to the selected parent ABIE and try again."
            )
        asbie_path = str(matching_asbie_rel.path)
        asbie_hash_path = str(matching_asbie_rel.hash_path)
        existing = (
            await self._session.execute(
                select(Asbie).where(
                    Asbie.from_abie_id == from_abie_id,
                    Asbie.based_ascc_manifest_id == based_ascc_manifest_id,
                    Asbie.hash_path == asbie_hash_path,
                    Asbie.owner_top_level_asbiep_id == top_level.top_level_asbiep_id,
                )
            )
        ).scalar_one_or_none()

        return AsbieCreatePlanRow(
            top_level_asbiep_id=top_level.top_level_asbiep_id,
            top_level_owner_user_id=top_level.owner_user_id,
            top_level_state=str(top_level.state),
            from_abie_based_acc_manifest_id=from_abie.based_acc_manifest_id,
            ascc_manifest_from_acc_manifest_id=ascc_manifest.from_acc_manifest_id,
            asccp_manifest_id=asccp_manifest.asccp_manifest_id,
            role_of_acc_manifest_id=asccp_manifest.role_of_acc_manifest_id,
            ascc_cardinality_min=matching_asbie_rel.cardinality_min,
            ascc_cardinality_max=matching_asbie_rel.cardinality_max,
            asbie_path=asbie_path,
            asbie_hash_path=asbie_hash_path,
            existing_asbie_id=existing.asbie_id if existing is not None else None,
            existing_to_asbiep_id=existing.to_asbiep_id if existing is not None and existing.to_asbiep_id is not None else None,
        )

    async def get_asbiep_role_plan(self, *, asbiep_id: int) -> AsbiepRolePlanRow | None:
        """Return ASBIEP role metadata needed for compatibility checks.

        Args:
            asbiep_id: ASBIEP identifier.

        Returns:
            Result of the operation.
        """
        asbiep = await self._session.get(Asbiep, asbiep_id)
        if asbiep is None or asbiep.role_of_abie_id is None:
            return None
        role_abie = await self._session.get(Abie, asbiep.role_of_abie_id)
        if role_abie is None:
            return None
        return AsbiepRolePlanRow(
            based_asccp_manifest_id=asbiep.based_asccp_manifest_id,
            role_based_acc_manifest_id=role_abie.based_acc_manifest_id,
        )

    async def create_abie(
        self,
        based_acc_manifest_id: AccManifestId,
        path: str,
        hash_path: str,
        owner_top_level_asbiep_id: int,
        requester_user_id: AppUserId,
        now: datetime,
    ) -> int:
        """Insert ABIE row and return identifier.

        Args:
            based_acc_manifest_id: Based ACC manifest identifier.
            path: ABIE path.
            hash_path: ABIE hash path.
            owner_top_level_asbiep_id: Owner top-level ASBIEP identifier.
            requester_user_id: Requesting user identifier.
            now: Timestamp for created/updated fields.

        Returns:
            Result of the operation.
        """
        role_of_abie = Abie(
            guid=new_guid(),
            based_acc_manifest_id=based_acc_manifest_id,
            path=path,
            hash_path=hash_path,
            definition=None,
            remark=None,
            biz_term=None,
            owner_top_level_asbiep_id=owner_top_level_asbiep_id,
            created_by=requester_user_id,
            creation_timestamp=now,
            last_updated_by=requester_user_id,
            last_update_timestamp=now,
        )
        self._session.add(role_of_abie)
        await self._session.flush()
        return role_of_abie.abie_id

    async def create_asbiep(
        self,
        based_asccp_manifest_id: AsccpManifestId,
        role_of_abie_id: int,
        path: str,
        hash_path: str,
        owner_top_level_asbiep_id: int,
        requester_user_id: AppUserId,
        now: datetime,
    ) -> int:
        """Insert ASBIEP row and return identifier.

        Args:
            based_asccp_manifest_id: Based ASCCP manifest identifier.
            role_of_abie_id: Role ABIE identifier.
            path: ASBIEP path.
            hash_path: ASBIEP hash path.
            owner_top_level_asbiep_id: Owner top-level ASBIEP identifier.
            requester_user_id: Requesting user identifier.
            now: Timestamp for created/updated fields.

        Returns:
            Result of the operation.
        """
        to_asbiep = Asbiep(
            guid=new_guid(),
            based_asccp_manifest_id=based_asccp_manifest_id,
            path=path,
            hash_path=hash_path,
            role_of_abie_id=role_of_abie_id,
            display_name=None,
            definition=None,
            biz_term=None,
            remark=None,
            last_updated_by=requester_user_id,
            created_by=requester_user_id,
            creation_timestamp=now,
            last_update_timestamp=now,
            owner_top_level_asbiep_id=owner_top_level_asbiep_id,
        )
        self._session.add(to_asbiep)
        await self._session.flush()
        return to_asbiep.asbiep_id

    async def create_asbie_record(
        self,
        based_ascc_manifest_id: AsccManifestId,
        path: str,
        hash_path: str,
        from_abie_id: int,
        to_asbiep_id: int,
        cardinality_min: int,
        cardinality_max: int,
        owner_top_level_asbiep_id: int,
        requester_user_id: AppUserId,
        now: datetime,
    ) -> int:
        """Insert ASBIE row and return identifier.

        Args:
            based_ascc_manifest_id: Based ASCC manifest identifier.
            path: ASBIE path.
            hash_path: ASBIE hash path.
            from_abie_id: Parent ABIE identifier.
            to_asbiep_id: Target ASBIEP identifier.
            cardinality_min: Minimum cardinality.
            cardinality_max: Maximum cardinality.
            owner_top_level_asbiep_id: Owner top-level ASBIEP identifier.
            requester_user_id: Requesting user identifier.
            now: Timestamp for created/updated fields.

        Returns:
            Result of the operation.
        """
        asbie = Asbie(
            guid=new_guid(),
            based_ascc_manifest_id=based_ascc_manifest_id,
            path=path,
            hash_path=hash_path,
            from_abie_id=from_abie_id,
            to_asbiep_id=to_asbiep_id,
            definition=None,
            cardinality_min=cardinality_min,
            cardinality_max=cardinality_max,
            is_nillable=False,
            remark=None,
            created_by=requester_user_id,
            last_updated_by=requester_user_id,
            creation_timestamp=now,
            last_update_timestamp=now,
            is_used=True,
            owner_top_level_asbiep_id=owner_top_level_asbiep_id,
        )
        self._session.add(asbie)
        await self._session.flush()
        return asbie.asbie_id

    async def activate_existing_asbie(
        self,
        asbie_id: int,
        requester_user_id: AppUserId,
        now: datetime,
        to_asbiep_id: int | None = None,
    ) -> None:
        """Mark existing ASBIE as used and optionally rewire target ASBIEP.

        Args:
            asbie_id: ASBIE identifier.
            requester_user_id: Requesting user identifier.
            now: Timestamp for updated fields.
            to_asbiep_id: Optional target ASBIEP identifier to assign.
        """
        asbie = await self._session.get(Asbie, asbie_id)
        if asbie is None:
            raise LookupError(self._not_found_message("ASBIE", asbie_id))
        if to_asbiep_id is not None:
            asbie.to_asbiep_id = to_asbiep_id
        asbie.is_used = True
        asbie.last_updated_by = requester_user_id
        asbie.last_update_timestamp = now

    async def get_bbie_create_plan(self, *, from_abie_id: int, based_bcc_manifest_id: BccManifestId) -> BbieCreatePlanRow:
        """Load DB context required to create or enable BBIE.

        Args:
            from_abie_id: Parent ABIE identifier.
            based_bcc_manifest_id: BCC manifest identifier.

        Returns:
            Result of the operation.
        """
        from_abie = await self._session.get(Abie, from_abie_id)
        if from_abie is None:
            raise LookupError(self._not_found_message("ABIE", from_abie_id))
        top_level = await self._session.get(TopLevelAsbiep, from_abie.owner_top_level_asbiep_id)
        if top_level is None:
            raise LookupError(self._not_found_message("top-level ASBIEP", from_abie.owner_top_level_asbiep_id))
        bcc_manifest = await self._session.get(BccManifest, based_bcc_manifest_id)
        if bcc_manifest is None:
            raise LookupError(self._not_found_message("BCC manifest", based_bcc_manifest_id))
        bcc = await self._session.get(Bcc, bcc_manifest.bcc_id)
        if bcc is None:
            raise LookupError(self._not_found_message("BCC", bcc_manifest.bcc_id))
        bccp_manifest = await self._session.get(BccpManifest, bcc_manifest.to_bccp_manifest_id)
        if bccp_manifest is None:
            raise LookupError(self._not_found_message("BCCP manifest", bcc_manifest.to_bccp_manifest_id))

        relationships = await self._get_abie_relationships(
            top_level_asbiep_id=top_level.top_level_asbiep_id,
            abie_id=from_abie_id,
            acc_manifest_id=from_abie.based_acc_manifest_id,
            abie_path=str(from_abie.path),
        )
        matching_bbie_rel = next(
            (
                rel
                for rel in relationships
                if isinstance(rel, BbieRelationshipRow) and rel.based_bcc.bcc_manifest_id == based_bcc_manifest_id
            ),
            None,
        )
        if matching_bbie_rel is None:
            raise ValueError(
                "The requested BCC is not available in the parent ABIE relationships. "
                "Please choose a BCC that belongs to the selected parent ABIE and try again."
            )
        bbie_path = str(matching_bbie_rel.path)
        bbie_hash_path = str(matching_bbie_rel.hash_path)
        existing = (
            await self._session.execute(
                select(Bbie).where(
                    Bbie.from_abie_id == from_abie_id,
                    Bbie.based_bcc_manifest_id == based_bcc_manifest_id,
                    Bbie.hash_path == bbie_hash_path,
                    Bbie.owner_top_level_asbiep_id == top_level.top_level_asbiep_id,
                )
            )
        ).scalar_one_or_none()
        primitive = await self._get_default_primitive_for_bccp(BccpManifestId(bccp_manifest.bccp_manifest_id))

        return BbieCreatePlanRow(
            top_level_asbiep_id=top_level.top_level_asbiep_id,
            top_level_owner_user_id=top_level.owner_user_id,
            top_level_state=str(top_level.state),
            from_abie_based_acc_manifest_id=from_abie.based_acc_manifest_id,
            bcc_manifest_from_acc_manifest_id=bcc_manifest.from_acc_manifest_id,
            bccp_manifest_id=bccp_manifest.bccp_manifest_id,
            bcc_cardinality_min=matching_bbie_rel.cardinality_min,
            bcc_cardinality_max=matching_bbie_rel.cardinality_max,
            bcc_is_nillable=bool(matching_bbie_rel.is_nillable),
            bcc_default_value=matching_bbie_rel.value_constraint.default_value if matching_bbie_rel.value_constraint else None,
            bcc_fixed_value=matching_bbie_rel.value_constraint.fixed_value if matching_bbie_rel.value_constraint else None,
            primitive_xbt_manifest_id=primitive.xbt_manifest_id if primitive else None,
            primitive_code_list_manifest_id=primitive.code_list_manifest_id if primitive else None,
            primitive_agency_id_list_manifest_id=primitive.agency_id_list_manifest_id if primitive else None,
            bbie_path=bbie_path,
            bbie_hash_path=bbie_hash_path,
            existing_bbie_id=existing.bbie_id if existing is not None else None,
            existing_to_bbiep_id=existing.to_bbiep_id if existing is not None and existing.to_bbiep_id is not None else None,
        )

    async def get_bbiep_plan(self, *, bbiep_id: int) -> BbiepPlanRow | None:
        """Return BBIEP metadata needed for compatibility checks.

        Args:
            bbiep_id: BBIEP identifier.

        Returns:
            Result of the operation.
        """
        bbiep = await self._session.get(Bbiep, bbiep_id)
        if bbiep is None:
            return None
        return BbiepPlanRow(based_bccp_manifest_id=bbiep.based_bccp_manifest_id)

    async def create_bbiep(
        self,
        based_bccp_manifest_id: BccpManifestId,
        path: str,
        hash_path: str,
        owner_top_level_asbiep_id: int,
        requester_user_id: AppUserId,
        now: datetime,
    ) -> int:
        """Insert BBIEP row and return identifier.

        Args:
            based_bccp_manifest_id: Based BCCP manifest identifier.
            path: BBIEP path.
            hash_path: BBIEP hash path.
            owner_top_level_asbiep_id: Owner top-level ASBIEP identifier.
            requester_user_id: Requesting user identifier.
            now: Timestamp for created/updated fields.

        Returns:
            Result of the operation.
        """
        bbiep = Bbiep(
            guid=new_guid(),
            based_bccp_manifest_id=based_bccp_manifest_id,
            path=path,
            hash_path=hash_path,
            definition=None,
            remark=None,
            biz_term=None,
            display_name=None,
            created_by=requester_user_id,
            last_updated_by=requester_user_id,
            creation_timestamp=now,
            last_update_timestamp=now,
            owner_top_level_asbiep_id=owner_top_level_asbiep_id,
        )
        self._session.add(bbiep)
        await self._session.flush()
        return bbiep.bbiep_id

    async def create_bbie_record(
        self,
        based_bcc_manifest_id: BccManifestId,
        path: str,
        hash_path: str,
        from_abie_id: int,
        to_bbiep_id: int,
        cardinality_min: int,
        cardinality_max: int,
        is_nillable: bool,
        default_value: str | None,
        fixed_value: str | None,
        xbt_manifest_id: XbtManifestId | None,
        code_list_manifest_id: CodeListManifestId | None,
        agency_id_list_manifest_id: AgencyIdListManifestId | None,
        owner_top_level_asbiep_id: int,
        requester_user_id: AppUserId,
        now: datetime,
    ) -> int:
        """Insert BBIE row and return identifier.

        Args:
            based_bcc_manifest_id: Based BCC manifest identifier.
            path: BBIE path.
            hash_path: BBIE hash path.
            from_abie_id: Parent ABIE identifier.
            to_bbiep_id: Target BBIEP identifier.
            cardinality_min: Minimum cardinality.
            cardinality_max: Maximum cardinality.
            is_nillable: Nillable flag.
            default_value: Default value.
            fixed_value: Fixed value.
            xbt_manifest_id: XBT manifest identifier.
            code_list_manifest_id: Code list manifest identifier.
            agency_id_list_manifest_id: Agency ID list manifest identifier.
            owner_top_level_asbiep_id: Owner top-level ASBIEP identifier.
            requester_user_id: Requesting user identifier.
            now: Timestamp for created/updated fields.

        Returns:
            Result of the operation.
        """
        bbie = Bbie(
            guid=new_guid(),
            based_bcc_manifest_id=based_bcc_manifest_id,
            path=path,
            hash_path=hash_path,
            from_abie_id=from_abie_id,
            to_bbiep_id=to_bbiep_id,
            xbt_manifest_id=xbt_manifest_id,
            code_list_manifest_id=code_list_manifest_id,
            agency_id_list_manifest_id=agency_id_list_manifest_id,
            cardinality_min=cardinality_min,
            cardinality_max=cardinality_max,
            facet_min_length=None,
            facet_max_length=None,
            facet_pattern=None,
            default_value=default_value,
            fixed_value=fixed_value,
            is_nillable=bool(is_nillable),
            definition=None,
            remark=None,
            created_by=requester_user_id,
            last_updated_by=requester_user_id,
            creation_timestamp=now,
            last_update_timestamp=now,
            is_used=True,
            owner_top_level_asbiep_id=owner_top_level_asbiep_id,
        )
        self._session.add(bbie)
        await self._session.flush()
        return bbie.bbie_id

    async def activate_existing_bbie(
        self,
        bbie_id: int,
        requester_user_id: AppUserId,
        now: datetime,
        to_bbiep_id: int | None = None,
    ) -> None:
        """Mark existing BBIE as used and optionally rewire target BBIEP.

        Args:
            bbie_id: BBIE identifier.
            requester_user_id: Requesting user identifier.
            now: Timestamp for updated fields.
            to_bbiep_id: Optional target BBIEP identifier to assign.
        """
        bbie = await self._session.get(Bbie, bbie_id)
        if bbie is None:
            raise LookupError(self._not_found_message("BBIE", bbie_id))
        if to_bbiep_id is not None:
            bbie.to_bbiep_id = to_bbiep_id
        bbie.is_used = True
        bbie.last_updated_by = requester_user_id
        bbie.last_update_timestamp = now

    async def create_asbie(
        self,
        from_abie_id: int,
        based_ascc_manifest_id: AsccManifestId,
        requester_user_id: AppUserId,
        is_admin: bool,
    ) -> int:
        """Create or enable ASBIE and return ASBIE identifier.

        Args:
            from_abie_id: Parent ABIE identifier.
            based_ascc_manifest_id: ASCC manifest identifier.
            requester_user_id: Requesting user identifier.
            is_admin: Whether requester has admin role.

        Returns:
            Result of the operation.
        """
        from_abie = await self._session.get(Abie, from_abie_id)
        if from_abie is None:
            raise LookupError(f"No ABIE exists with ID {from_abie_id}.")
        top_level = await self._session.get(TopLevelAsbiep, from_abie.owner_top_level_asbiep_id)
        if top_level is None:
            raise LookupError(f"No top-level ASBIEP exists with ID {from_abie.owner_top_level_asbiep_id}.")

        ascc_manifest = await self._session.get(AsccManifest, based_ascc_manifest_id)
        if ascc_manifest is None:
            raise LookupError(f"No ASCC manifest exists with ID {based_ascc_manifest_id}.")
        if ascc_manifest.from_acc_manifest_id != from_abie.based_acc_manifest_id:
            raise ValueError("The requested ASCC does not belong to the parent ABIE's ACC.")
        ascc = await self._session.get(Ascc, ascc_manifest.ascc_id)
        if ascc is None:
            raise LookupError(f"No ASCC exists with ID {ascc_manifest.ascc_id}.")
        asccp_manifest = await self._session.get(AsccpManifest, ascc_manifest.to_asccp_manifest_id)
        if asccp_manifest is None:
            raise LookupError(f"No ASCCP manifest exists with ID {ascc_manifest.to_asccp_manifest_id}.")

        asbie_path = f"{from_abie.path}>ASCC-{based_ascc_manifest_id}"
        existing = (
            await self._session.execute(
                select(Asbie).where(
                    Asbie.from_abie_id == from_abie_id,
                    Asbie.based_ascc_manifest_id == based_ascc_manifest_id,
                    Asbie.hash_path == hashlib.sha256(asbie_path.encode()).hexdigest(),
                    Asbie.owner_top_level_asbiep_id == top_level.top_level_asbiep_id,
                )
            )
        ).scalar_one_or_none()
        now = datetime.now(timezone.utc).replace(tzinfo=None)
        async def _create_target_asbiep() -> Asbiep:
            role_of_abie_path = (
                f"{asbie_path}>ASCCP-{asccp_manifest.asccp_manifest_id}>ACC-{asccp_manifest.role_of_acc_manifest_id}"
            )
            role_of_abie = Abie(
                guid=new_guid(),
                based_acc_manifest_id=asccp_manifest.role_of_acc_manifest_id,
                path=role_of_abie_path,
                hash_path=hashlib.sha256(role_of_abie_path.encode()).hexdigest(),
                definition=None,
                remark=None,
                biz_term=None,
                owner_top_level_asbiep_id=top_level.top_level_asbiep_id,
                created_by=requester_user_id,
                creation_timestamp=now,
                last_updated_by=requester_user_id,
                last_update_timestamp=now,
            )
            self._session.add(role_of_abie)
            await self._session.flush()

            to_asbiep_path = f"{asbie_path}>ASCCP-{asccp_manifest.asccp_manifest_id}"
            to_asbiep = Asbiep(
                guid=new_guid(),
                based_asccp_manifest_id=asccp_manifest.asccp_manifest_id,
                path=to_asbiep_path,
                hash_path=hashlib.sha256(to_asbiep_path.encode()).hexdigest(),
                role_of_abie_id=role_of_abie.abie_id,
                display_name=None,
                definition=None,
                biz_term=None,
                remark=None,
                last_updated_by=requester_user_id,
                created_by=requester_user_id,
                creation_timestamp=now,
                last_update_timestamp=now,
                owner_top_level_asbiep_id=top_level.top_level_asbiep_id,
            )
            self._session.add(to_asbiep)
            await self._session.flush()
            return to_asbiep

        if existing is not None:
            to_asbiep: Asbiep | None = None
            if existing.to_asbiep_id is not None:
                to_asbiep = await self._session.get(Asbiep, existing.to_asbiep_id)
            recreate = to_asbiep is None or to_asbiep.based_asccp_manifest_id != asccp_manifest.asccp_manifest_id
            if not recreate:
                if to_asbiep.role_of_abie_id is None:
                    recreate = True
                else:
                    role_abie = await self._session.get(Abie, to_asbiep.role_of_abie_id)
                    if role_abie is None or role_abie.based_acc_manifest_id != asccp_manifest.role_of_acc_manifest_id:
                        recreate = True
            if recreate:
                to_asbiep = await _create_target_asbiep()
                existing.to_asbiep_id = to_asbiep.asbiep_id
            existing.is_used = True
            existing.last_updated_by = requester_user_id
            existing.last_update_timestamp = now
            return existing.asbie_id

        to_asbiep = await _create_target_asbiep()
        asbie = Asbie(
            guid=new_guid(),
            based_ascc_manifest_id=based_ascc_manifest_id,
            path=asbie_path,
            hash_path=hashlib.sha256(asbie_path.encode()).hexdigest(),
            from_abie_id=from_abie_id,
            to_asbiep_id=to_asbiep.asbiep_id,
            definition=None,
            cardinality_min=ascc.cardinality_min,
            cardinality_max=ascc.cardinality_max,
            is_nillable=False,
            remark=None,
            created_by=requester_user_id,
            last_updated_by=requester_user_id,
            creation_timestamp=now,
            last_update_timestamp=now,
            is_used=True,
            owner_top_level_asbiep_id=top_level.top_level_asbiep_id,
        )
        self._session.add(asbie)
        await self._session.flush()
        return asbie.asbie_id

    async def update_asbie(
        self,
        asbie_id: int,
        requester_user_id: AppUserId,
        is_admin: bool,
        is_used: bool | None = None,
        is_nillable: bool | None = None,
        definition: str | None = None,
        cardinality_min: int | None = None,
        cardinality_max: int | None = None,
        remark: str | None = None,
    ) -> list[str]:
        """Update ASBIE fields.

        Args:
            asbie_id: ASBIE identifier.
            requester_user_id: Requesting user identifier.
            is_admin: Whether requester has admin role.
            is_used: Profile flag.
            is_nillable: Nillable flag.
            definition: Definition text for this ASBIE.
            cardinality_min: Minimum cardinality.
            cardinality_max: Maximum cardinality.
            remark: Remark text for the ASBIE property.

        Returns:
            Result of the operation.
        """
        asbie = await self._session.get(Asbie, asbie_id)
        if asbie is None:
            raise LookupError(self._not_found_message("ASBIE", asbie_id))
        top_level = await self._session.get(TopLevelAsbiep, asbie.owner_top_level_asbiep_id)
        if top_level is None:
            raise LookupError(self._not_found_message("top-level ASBIEP", asbie.owner_top_level_asbiep_id))
        ascc_manifest = await self._session.get(AsccManifest, asbie.based_ascc_manifest_id)
        if ascc_manifest is None:
            raise LookupError(self._not_found_message("ASCC manifest", asbie.based_ascc_manifest_id))
        ascc = await self._session.get(Ascc, ascc_manifest.ascc_id)
        if ascc is None:
            raise LookupError(self._not_found_message("ASCC", ascc_manifest.ascc_id))

        updates: list[str] = []
        if is_used is not None:
            asbie.is_used = bool(is_used)
            updates.append("is_used")
        if is_nillable is not None:
            asbie.is_nillable = bool(is_nillable)
            updates.append("is_nillable")
        if definition is not None:
            asbie.definition = definition
            updates.append("definition")
        next_min, next_max = self._validate_cardinality_against_base(
            relationship_name="ASBIE",
            base_name="ASCC",
            base_cardinality_min=int(ascc.cardinality_min),
            base_cardinality_max=self._normalize_base_cardinality_max(ascc.cardinality_max),
            current_cardinality_min=int(asbie.cardinality_min),
            current_cardinality_max=int(asbie.cardinality_max),
            cardinality_min=cardinality_min,
            cardinality_max=cardinality_max,
        )
        if cardinality_min is not None:
            asbie.cardinality_min = next_min
            updates.append("cardinality_min")
        if cardinality_max is not None:
            asbie.cardinality_max = next_max
            updates.append("cardinality_max")
        if remark is not None and asbie.to_asbiep_id is not None:
            target_asbiep = await self._session.get(Asbiep, asbie.to_asbiep_id)
            if target_asbiep is not None:
                if (
                    target_asbiep.owner_top_level_asbiep_id is not None
                    and int(target_asbiep.owner_top_level_asbiep_id) != int(asbie.owner_top_level_asbiep_id)
                ):
                    raise ValueError(
                        "Cannot update the reused ASBIEP remark through the reusing BIE. "
                        "Please update the reused top-level ASBIEP directly instead."
                    )
                target_asbiep.remark = remark
                target_asbiep.last_updated_by = requester_user_id
                target_asbiep.last_update_timestamp = datetime.now(timezone.utc).replace(tzinfo=None)
                updates.append("remark")

        if updates:
            asbie.last_updated_by = requester_user_id
            asbie.last_update_timestamp = datetime.now(timezone.utc).replace(tzinfo=None)
        return sorted(set(updates))

    async def create_bbie(
        self,
        from_abie_id: int,
        based_bcc_manifest_id: BccManifestId,
        requester_user_id: AppUserId,
        is_admin: bool,
    ) -> int:
        """Create or enable BBIE and return BBIE identifier.

        Args:
            from_abie_id: Parent ABIE identifier.
            based_bcc_manifest_id: BCC manifest identifier.
            requester_user_id: Requesting user identifier.
            is_admin: Whether requester has admin role.

        Returns:
            Result of the operation.
        """
        from_abie = await self._session.get(Abie, from_abie_id)
        if from_abie is None:
            raise LookupError(self._not_found_message("ABIE", from_abie_id))
        top_level = await self._session.get(TopLevelAsbiep, from_abie.owner_top_level_asbiep_id)
        if top_level is None:
            raise LookupError(self._not_found_message("top-level ASBIEP", from_abie.owner_top_level_asbiep_id))

        bcc_manifest = await self._session.get(BccManifest, based_bcc_manifest_id)
        if bcc_manifest is None:
            raise LookupError(self._not_found_message("BCC manifest", based_bcc_manifest_id))
        if bcc_manifest.from_acc_manifest_id != from_abie.based_acc_manifest_id:
            raise ValueError(
                "The requested BCC does not belong to the parent ABIE's ACC. "
                "Please choose a BCC that belongs to the parent ABIE and try again."
            )
        bcc = await self._session.get(Bcc, bcc_manifest.bcc_id)
        if bcc is None:
            raise LookupError(self._not_found_message("BCC", bcc_manifest.bcc_id))
        bccp_manifest = await self._session.get(BccpManifest, bcc_manifest.to_bccp_manifest_id)
        if bccp_manifest is None:
            raise LookupError(self._not_found_message("BCCP manifest", bcc_manifest.to_bccp_manifest_id))

        bbie_path = f"{from_abie.path}>BCC-{based_bcc_manifest_id}"
        existing = (
            await self._session.execute(
                select(Bbie).where(
                    Bbie.from_abie_id == from_abie_id,
                    Bbie.based_bcc_manifest_id == based_bcc_manifest_id,
                    Bbie.hash_path == hashlib.sha256(bbie_path.encode()).hexdigest(),
                    Bbie.owner_top_level_asbiep_id == top_level.top_level_asbiep_id,
                )
            )
        ).scalar_one_or_none()
        now = datetime.now(timezone.utc).replace(tzinfo=None)
        async def _create_target_bbiep() -> Bbiep:
            bbiep_path = f"{bbie_path}>BCCP-{bccp_manifest.bccp_manifest_id}"
            bbiep = Bbiep(
                guid=new_guid(),
                based_bccp_manifest_id=bccp_manifest.bccp_manifest_id,
                path=bbiep_path,
                hash_path=hashlib.sha256(bbiep_path.encode()).hexdigest(),
                definition=None,
                remark=None,
                biz_term=None,
                display_name=None,
                created_by=requester_user_id,
                last_updated_by=requester_user_id,
                creation_timestamp=now,
                last_update_timestamp=now,
                owner_top_level_asbiep_id=top_level.top_level_asbiep_id,
            )
            self._session.add(bbiep)
            await self._session.flush()
            return bbiep

        if existing is not None:
            bbiep: Bbiep | None = None
            if existing.to_bbiep_id is not None:
                bbiep = await self._session.get(Bbiep, existing.to_bbiep_id)
            if bbiep is None or bbiep.based_bccp_manifest_id != bccp_manifest.bccp_manifest_id:
                bbiep = await _create_target_bbiep()
                existing.to_bbiep_id = bbiep.bbiep_id
            existing.is_used = True
            existing.last_updated_by = requester_user_id
            existing.last_update_timestamp = now
            await self._validate_primitive_restriction_for_top_level_release(
                xbt_manifest_id=existing.xbt_manifest_id,
                code_list_manifest_id=existing.code_list_manifest_id,
                agency_id_list_manifest_id=existing.agency_id_list_manifest_id,
                top_level_release_id=int(top_level.release_id),
            )
            return existing.bbie_id

        bbiep = await _create_target_bbiep()

        primitive = await self._get_default_primitive_for_bccp(BccpManifestId(bccp_manifest.bccp_manifest_id))
        primitive_xbt_manifest_id, primitive_code_list_manifest_id, primitive_agency_id_list_manifest_id = (
            await self._validate_primitive_restriction_for_top_level_release(
                xbt_manifest_id=primitive.xbt_manifest_id if primitive else None,
                code_list_manifest_id=primitive.code_list_manifest_id if primitive else None,
                agency_id_list_manifest_id=primitive.agency_id_list_manifest_id if primitive else None,
                top_level_release_id=int(top_level.release_id),
            )
        )
        bbie = Bbie(
            guid=new_guid(),
            based_bcc_manifest_id=based_bcc_manifest_id,
            path=bbie_path,
            hash_path=hashlib.sha256(bbie_path.encode()).hexdigest(),
            from_abie_id=from_abie_id,
            to_bbiep_id=bbiep.bbiep_id,
            xbt_manifest_id=primitive_xbt_manifest_id,
            code_list_manifest_id=primitive_code_list_manifest_id,
            agency_id_list_manifest_id=primitive_agency_id_list_manifest_id,
            cardinality_min=bcc.cardinality_min,
            cardinality_max=bcc.cardinality_max if bcc.cardinality_max is not None else -1,
            facet_min_length=None,
            facet_max_length=None,
            facet_pattern=None,
            default_value=bcc.default_value,
            fixed_value=bcc.fixed_value,
            is_nillable=bool(bcc.is_nillable),
            definition=None,
            remark=None,
            created_by=requester_user_id,
            last_updated_by=requester_user_id,
            creation_timestamp=now,
            last_update_timestamp=now,
            is_used=True,
            owner_top_level_asbiep_id=top_level.top_level_asbiep_id,
        )
        self._session.add(bbie)
        await self._session.flush()
        return bbie.bbie_id

    async def update_bbie(
        self,
        bbie_id: int,
        requester_user_id: AppUserId,
        is_admin: bool,
        is_used: bool | None = None,
        is_nillable: bool | None = None,
        cardinality_min: int | None = None,
        cardinality_max: int | None = None,
        definition: str | None = None,
        example: str | None = None,
        remark: str | None = None,
        default_value: str | None = None,
        fixed_value: str | None = None,
        facet_min_length: int | None = None,
        facet_max_length: int | None = None,
        facet_pattern: str | None = None,
        xbt_manifest_id: int | None = None,
        code_list_manifest_id: int | None = None,
        agency_id_list_manifest_id: int | None = None,
    ) -> list[str]:
        """Update BBIE fields.

        Args:
            bbie_id: BBIE identifier.
            requester_user_id: Requesting user identifier.
            is_admin: Whether requester has admin role.
            is_used: Profile flag.
            is_nillable: Nillable flag.
            cardinality_min: Minimum cardinality.
            cardinality_max: Maximum cardinality.
            definition: Definition text for this BBIE.
            example: Illustrative example value or content for this BBIE.
            remark: Remark text for the BBIE property.
            default_value: Default value.
            fixed_value: Fixed value.
            facet_min_length: Facet minimum length.
            facet_max_length: Facet maximum length.
            facet_pattern: Facet regular expression pattern.
            xbt_manifest_id: XBT manifest identifier to use as the primitive restriction for this BBIE.
            code_list_manifest_id: Code list manifest identifier to use as the primitive restriction for this BBIE.
            agency_id_list_manifest_id: Agency-ID-list manifest identifier to use as the primitive restriction for this BBIE.

        Returns:
            Result of the operation.
        """
        bbie = await self._session.get(Bbie, bbie_id)
        if bbie is None:
            raise LookupError(f"No BBIE exists with ID {bbie_id}.")
        top_level = await self._session.get(TopLevelAsbiep, bbie.owner_top_level_asbiep_id)
        if top_level is None:
            raise LookupError(self._not_found_message("top-level ASBIEP", bbie.owner_top_level_asbiep_id))
        bcc_manifest = await self._session.get(BccManifest, bbie.based_bcc_manifest_id)
        if bcc_manifest is None:
            raise LookupError(self._not_found_message("BCC manifest", bbie.based_bcc_manifest_id))
        bcc = await self._session.get(Bcc, bcc_manifest.bcc_id)
        if bcc is None:
            raise LookupError(self._not_found_message("BCC", bcc_manifest.bcc_id))
        if default_value is not None and fixed_value is not None:
            raise ValueError(
                "default_value and fixed_value are mutually exclusive. "
                "Please provide only one of those fields and try again."
            )
        final_xbt_manifest_id = bbie.xbt_manifest_id
        final_code_list_manifest_id = bbie.code_list_manifest_id
        final_agency_id_list_manifest_id = bbie.agency_id_list_manifest_id
        if xbt_manifest_id is not None:
            final_xbt_manifest_id = xbt_manifest_id
            final_code_list_manifest_id = None
            final_agency_id_list_manifest_id = None
        elif code_list_manifest_id is not None:
            final_xbt_manifest_id = None
            final_code_list_manifest_id = code_list_manifest_id
            final_agency_id_list_manifest_id = None
        elif agency_id_list_manifest_id is not None:
            final_xbt_manifest_id = None
            final_code_list_manifest_id = None
            final_agency_id_list_manifest_id = agency_id_list_manifest_id
        final_xbt_manifest_id, final_code_list_manifest_id, final_agency_id_list_manifest_id = (
            await self._validate_primitive_restriction_for_top_level_release(
                xbt_manifest_id=final_xbt_manifest_id,
                code_list_manifest_id=final_code_list_manifest_id,
                agency_id_list_manifest_id=final_agency_id_list_manifest_id,
                top_level_release_id=int(top_level.release_id),
            )
        )

        updates: list[str] = []
        if is_used is not None:
            bbie.is_used = bool(is_used)
            updates.append("is_used")
        if is_nillable is not None:
            bbie.is_nillable = bool(is_nillable)
            updates.append("is_nillable")
        next_min, next_max = self._validate_cardinality_against_base(
            relationship_name="BBIE",
            base_name="BCC",
            base_cardinality_min=int(bcc.cardinality_min),
            base_cardinality_max=self._normalize_base_cardinality_max(bcc.cardinality_max),
            current_cardinality_min=int(bbie.cardinality_min),
            current_cardinality_max=self._normalize_base_cardinality_max(bbie.cardinality_max),
            cardinality_min=cardinality_min,
            cardinality_max=cardinality_max,
        )
        if cardinality_min is not None:
            bbie.cardinality_min = next_min
            updates.append("cardinality_min")
        if cardinality_max is not None:
            bbie.cardinality_max = next_max
            updates.append("cardinality_max")
        if definition is not None:
            bbie.definition = definition
            updates.append("definition")
        if example is not None:
            bbie.example = example
            updates.append("example")
        if default_value is not None:
            bbie.default_value = default_value
            bbie.fixed_value = None
            updates.append("default_value")
        if fixed_value is not None:
            bbie.fixed_value = fixed_value
            bbie.default_value = None
            updates.append("fixed_value")
        if facet_min_length is not None:
            bbie.facet_min_length = facet_min_length
            updates.append("facet_min_length")
        if facet_max_length is not None:
            bbie.facet_max_length = facet_max_length
            updates.append("facet_max_length")
        if facet_pattern is not None:
            bbie.facet_pattern = facet_pattern
            updates.append("facet_pattern")
        if xbt_manifest_id is not None:
            bbie.xbt_manifest_id = final_xbt_manifest_id
            bbie.code_list_manifest_id = final_code_list_manifest_id
            bbie.agency_id_list_manifest_id = final_agency_id_list_manifest_id
            updates.append("xbt_manifest_id")
        elif code_list_manifest_id is not None:
            bbie.xbt_manifest_id = final_xbt_manifest_id
            bbie.code_list_manifest_id = final_code_list_manifest_id
            bbie.agency_id_list_manifest_id = final_agency_id_list_manifest_id
            updates.append("code_list_manifest_id")
        elif agency_id_list_manifest_id is not None:
            bbie.xbt_manifest_id = final_xbt_manifest_id
            bbie.code_list_manifest_id = final_code_list_manifest_id
            bbie.agency_id_list_manifest_id = final_agency_id_list_manifest_id
            updates.append("agency_id_list_manifest_id")
        if remark is not None and bbie.to_bbiep_id is not None:
            bbiep = await self._session.get(Bbiep, bbie.to_bbiep_id)
            if bbiep is not None:
                bbiep.remark = remark
                bbiep.last_updated_by = requester_user_id
                bbiep.last_update_timestamp = datetime.now(timezone.utc).replace(tzinfo=None)
                updates.append("remark")
        if updates:
            bbie.last_updated_by = requester_user_id
            bbie.last_update_timestamp = datetime.now(timezone.utc).replace(tzinfo=None)
        return sorted(set(updates))

    async def create_bbie_sc(
        self,
        bbie_id: int,
        based_dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
        requester_user_id: AppUserId,
        is_admin: bool,
    ) -> int:
        """Create or enable BBIE_SC and return BBIE_SC identifier.

        Args:
            bbie_id: Parent BBIE identifier.
            based_dt_sc_manifest_id: DT_SC manifest identifier.
            requester_user_id: Requesting user identifier.
            is_admin: Whether requester has admin role.

        Returns:
            Result of the operation.
        """
        bbie = await self._session.get(Bbie, bbie_id)
        if bbie is None:
            raise LookupError(self._not_found_message("BBIE", bbie_id))
        top_level = await self._session.get(TopLevelAsbiep, bbie.owner_top_level_asbiep_id)
        if top_level is None:
            raise LookupError(self._not_found_message("top-level ASBIEP", bbie.owner_top_level_asbiep_id))

        dt_sc_manifest = await self._session.get(DtScManifest, based_dt_sc_manifest_id)
        if dt_sc_manifest is None:
            raise LookupError(self._not_found_message("DT_SC manifest", based_dt_sc_manifest_id))
        if bbie.to_bbiep_id is None:
            raise ValueError(
                f"BBIE {bbie_id} has no BBIEP. "
                "Please verify that the BBIE is fully initialized before creating supplementary components."
            )
        bbiep = await self._session.get(Bbiep, bbie.to_bbiep_id)
        if bbiep is None:
            raise LookupError(self._not_found_message("BBIEP", bbie.to_bbiep_id))
        bccp_manifest = await self._session.get(BccpManifest, bbiep.based_bccp_manifest_id)
        if bccp_manifest is None:
            raise LookupError(self._not_found_message("BCCP manifest", bbiep.based_bccp_manifest_id))
        if dt_sc_manifest.owner_dt_manifest_id != bccp_manifest.bdt_manifest_id:
            raise ValueError(
                "DT_SC manifest does not belong to the BBIE's BDT. "
                "Please choose a DT_SC that belongs to the BBIE's base data type and try again."
            )

        bbie_sc_path = self._build_bbie_sc_path(
            bbiep_path=str(bbiep.path),
            bdt_manifest_id=DataTypeManifestId(bccp_manifest.bdt_manifest_id),
            based_dt_sc_manifest_id=based_dt_sc_manifest_id,
        )
        bbie_sc_hash_path = hashlib.sha256(bbie_sc_path.encode()).hexdigest()
        existing = (
            await self._session.execute(
                select(BbieSc).where(
                    BbieSc.bbie_id == bbie_id,
                    BbieSc.based_dt_sc_manifest_id == based_dt_sc_manifest_id,
                    BbieSc.owner_top_level_asbiep_id == top_level.top_level_asbiep_id,
                )
                .order_by(BbieSc.bbie_sc_id.desc())
                .limit(1)
            )
        ).scalar_one_or_none()
        now = datetime.now(timezone.utc).replace(tzinfo=None)
        if existing is not None:
            existing.path = bbie_sc_path
            existing.hash_path = bbie_sc_hash_path
            existing.is_used = True
            existing.last_updated_by = requester_user_id
            existing.last_update_timestamp = now
            await self._validate_primitive_restriction_for_top_level_release(
                xbt_manifest_id=existing.xbt_manifest_id,
                code_list_manifest_id=existing.code_list_manifest_id,
                agency_id_list_manifest_id=existing.agency_id_list_manifest_id,
                top_level_release_id=int(top_level.release_id),
            )
            return existing.bbie_sc_id

        dt_sc = await self._session.get(DtSc, dt_sc_manifest.dt_sc_id)
        if dt_sc is None:
            raise LookupError(self._not_found_message("DT_SC", dt_sc_manifest.dt_sc_id))
        primitive = await self._get_default_primitive_for_dt_sc(
            dt_sc_id=DataTypeSupplementaryComponentId(dt_sc_manifest.dt_sc_id),
            owner_dt_manifest_id=DataTypeManifestId(dt_sc_manifest.owner_dt_manifest_id),
        )
        primitive_xbt_manifest_id, primitive_code_list_manifest_id, primitive_agency_id_list_manifest_id = (
            await self._validate_primitive_restriction_for_top_level_release(
                xbt_manifest_id=primitive.xbt_manifest_id if primitive else None,
                code_list_manifest_id=primitive.code_list_manifest_id if primitive else None,
                agency_id_list_manifest_id=primitive.agency_id_list_manifest_id if primitive else None,
                top_level_release_id=int(top_level.release_id),
            )
        )
        bbie_sc = BbieSc(
            guid=new_guid(),
            based_dt_sc_manifest_id=based_dt_sc_manifest_id,
            path=bbie_sc_path,
            hash_path=bbie_sc_hash_path,
            bbie_id=bbie_id,
            xbt_manifest_id=primitive_xbt_manifest_id,
            code_list_manifest_id=primitive_code_list_manifest_id,
            agency_id_list_manifest_id=primitive_agency_id_list_manifest_id,
            cardinality_min=dt_sc.cardinality_min,
            cardinality_max=dt_sc.cardinality_max if dt_sc.cardinality_max is not None else -1,
            facet_min_length=None,
            facet_max_length=None,
            facet_pattern=None,
            default_value=dt_sc.default_value,
            fixed_value=dt_sc.fixed_value,
            definition=None,
            remark=None,
            biz_term=None,
            display_name=None,
            is_used=True,
            created_by=requester_user_id,
            last_updated_by=requester_user_id,
            creation_timestamp=now,
            last_update_timestamp=now,
            owner_top_level_asbiep_id=top_level.top_level_asbiep_id,
        )
        self._session.add(bbie_sc)
        await self._session.flush()
        return bbie_sc.bbie_sc_id

    async def update_bbie_sc(
        self,
        bbie_sc_id: int,
        requester_user_id: AppUserId,
        is_admin: bool,
        is_used: bool | None = None,
        cardinality_min: int | None = None,
        cardinality_max: int | None = None,
        definition: str | None = None,
        example: str | None = None,
        remark: str | None = None,
        biz_term: str | None = None,
        display_name: str | None = None,
        default_value: str | None = None,
        fixed_value: str | None = None,
        facet_min_length: int | None = None,
        facet_max_length: int | None = None,
        facet_pattern: str | None = None,
        xbt_manifest_id: int | None = None,
        code_list_manifest_id: int | None = None,
        agency_id_list_manifest_id: int | None = None,
    ) -> list[str]:
        """Update BBIE_SC fields.

        Args:
            bbie_sc_id: BBIE_SC identifier.
            requester_user_id: Requesting user identifier.
            is_admin: Whether requester has admin role.
            is_used: Profile flag.
            cardinality_min: Minimum cardinality.
            cardinality_max: Maximum cardinality.
            definition: Definition text for this BBIE supplementary component.
            example: Illustrative example value or content for this BBIE supplementary component.
            remark: Remark text for this BBIE supplementary component.
            biz_term: Business term override.
            display_name: Display name override.
            default_value: Default value.
            fixed_value: Fixed value.
            facet_min_length: Facet minimum length.
            facet_max_length: Facet maximum length.
            facet_pattern: Facet regular expression pattern.
            xbt_manifest_id: XBT manifest identifier to use as the primitive restriction for this BBIE supplementary component.
            code_list_manifest_id: Code list manifest identifier to use as the primitive restriction for this BBIE supplementary component.
            agency_id_list_manifest_id: Agency-ID-list manifest identifier to use as the primitive restriction for this BBIE supplementary component.

        Returns:
            Result of the operation.
        """
        bbie_sc = await self._session.get(BbieSc, bbie_sc_id)
        if bbie_sc is None:
            raise LookupError(self._not_found_message("BBIE_SC", bbie_sc_id))
        dt_sc_manifest = await self._session.get(DtScManifest, bbie_sc.based_dt_sc_manifest_id)
        if dt_sc_manifest is None:
            raise LookupError(self._not_found_message("DT_SC manifest", bbie_sc.based_dt_sc_manifest_id))
        dt_sc = await self._session.get(DtSc, dt_sc_manifest.dt_sc_id)
        if dt_sc is None:
            raise LookupError(self._not_found_message("DT_SC", dt_sc_manifest.dt_sc_id))
        if default_value is not None and fixed_value is not None:
            raise ValueError(
                "default_value and fixed_value are mutually exclusive. "
                "Please provide only one of those fields and try again."
            )
        top_level = await self._session.get(TopLevelAsbiep, bbie_sc.owner_top_level_asbiep_id)
        if top_level is None:
            raise LookupError(self._not_found_message("top-level ASBIEP", bbie_sc.owner_top_level_asbiep_id))
        final_xbt_manifest_id = bbie_sc.xbt_manifest_id
        final_code_list_manifest_id = bbie_sc.code_list_manifest_id
        final_agency_id_list_manifest_id = bbie_sc.agency_id_list_manifest_id
        if xbt_manifest_id is not None:
            final_xbt_manifest_id = xbt_manifest_id
            final_code_list_manifest_id = None
            final_agency_id_list_manifest_id = None
        elif code_list_manifest_id is not None:
            final_xbt_manifest_id = None
            final_code_list_manifest_id = code_list_manifest_id
            final_agency_id_list_manifest_id = None
        elif agency_id_list_manifest_id is not None:
            final_xbt_manifest_id = None
            final_code_list_manifest_id = None
            final_agency_id_list_manifest_id = agency_id_list_manifest_id
        final_xbt_manifest_id, final_code_list_manifest_id, final_agency_id_list_manifest_id = (
            await self._validate_primitive_restriction_for_top_level_release(
                xbt_manifest_id=final_xbt_manifest_id,
                code_list_manifest_id=final_code_list_manifest_id,
                agency_id_list_manifest_id=final_agency_id_list_manifest_id,
                top_level_release_id=int(top_level.release_id),
            )
        )

        updates: list[str] = []
        if is_used is not None:
            bbie_sc.is_used = bool(is_used)
            updates.append("is_used")
        next_min, next_max = self._validate_cardinality_against_base(
            relationship_name="BBIE_SC",
            base_name="DT_SC",
            base_cardinality_min=int(dt_sc.cardinality_min),
            base_cardinality_max=self._normalize_base_cardinality_max(dt_sc.cardinality_max),
            current_cardinality_min=int(bbie_sc.cardinality_min),
            current_cardinality_max=self._normalize_base_cardinality_max(bbie_sc.cardinality_max),
            cardinality_min=cardinality_min,
            cardinality_max=cardinality_max,
        )
        if cardinality_min is not None:
            bbie_sc.cardinality_min = next_min
            updates.append("cardinality_min")
        if cardinality_max is not None:
            bbie_sc.cardinality_max = next_max
            updates.append("cardinality_max")
        if definition is not None:
            bbie_sc.definition = definition
            updates.append("definition")
        if example is not None:
            bbie_sc.example = example
            updates.append("example")
        if remark is not None:
            bbie_sc.remark = remark
            updates.append("remark")
        if biz_term is not None:
            bbie_sc.biz_term = biz_term
            updates.append("biz_term")
        if display_name is not None:
            bbie_sc.display_name = display_name
            updates.append("display_name")
        if default_value is not None:
            bbie_sc.default_value = default_value
            bbie_sc.fixed_value = None
            updates.append("default_value")
        if fixed_value is not None:
            bbie_sc.fixed_value = fixed_value
            bbie_sc.default_value = None
            updates.append("fixed_value")
        if facet_min_length is not None:
            bbie_sc.facet_min_length = facet_min_length
            updates.append("facet_min_length")
        if facet_max_length is not None:
            bbie_sc.facet_max_length = facet_max_length
            updates.append("facet_max_length")
        if facet_pattern is not None:
            bbie_sc.facet_pattern = facet_pattern
            updates.append("facet_pattern")
        if xbt_manifest_id is not None:
            bbie_sc.xbt_manifest_id = final_xbt_manifest_id
            bbie_sc.code_list_manifest_id = final_code_list_manifest_id
            bbie_sc.agency_id_list_manifest_id = final_agency_id_list_manifest_id
            updates.append("xbt_manifest_id")
        elif code_list_manifest_id is not None:
            bbie_sc.xbt_manifest_id = final_xbt_manifest_id
            bbie_sc.code_list_manifest_id = final_code_list_manifest_id
            bbie_sc.agency_id_list_manifest_id = final_agency_id_list_manifest_id
            updates.append("code_list_manifest_id")
        elif agency_id_list_manifest_id is not None:
            bbie_sc.xbt_manifest_id = final_xbt_manifest_id
            bbie_sc.code_list_manifest_id = final_code_list_manifest_id
            bbie_sc.agency_id_list_manifest_id = final_agency_id_list_manifest_id
            updates.append("agency_id_list_manifest_id")
        if updates:
            bbie_sc.last_updated_by = requester_user_id
            bbie_sc.last_update_timestamp = datetime.now(timezone.utc).replace(tzinfo=None)
        return sorted(set(updates))

    async def reuse_top_level_asbiep(
        self,
        asbie_id: int,
        reuse_top_level_asbiep_id: int,
        requester_user_id: AppUserId,
        is_admin: bool,
    ) -> int:
        """Reuse another top-level ASBIEP's ASBIEP in ASBIE.

        Args:
            asbie_id: ASBIE identifier.
            reuse_top_level_asbiep_id: Target top-level ASBIEP identifier to reuse.
            requester_user_id: Requesting user identifier.
            is_admin: Whether requester has admin role.

        Returns:
            Result of the operation.
        """
        asbie = await self._session.get(Asbie, asbie_id)
        if asbie is None:
            raise LookupError(f"No ASBIE exists with ID {asbie_id}.")
        owner_top = await self._session.get(TopLevelAsbiep, asbie.owner_top_level_asbiep_id)
        if owner_top is None:
            raise LookupError(self._not_found_message("top-level ASBIEP", asbie.owner_top_level_asbiep_id))
        if asbie.owner_top_level_asbiep_id == reuse_top_level_asbiep_id:
            raise ValueError(
                "reuse_top_level_asbiep_id must be different from ASBIE owner top-level ASBIEP. "
                "Please choose a different top-level ASBIEP to reuse."
            )
        reuse_top = await self._session.get(TopLevelAsbiep, reuse_top_level_asbiep_id)
        if reuse_top is None:
            raise LookupError(self._not_found_message("top-level ASBIEP", reuse_top_level_asbiep_id))
        if owner_top.release_id != reuse_top.release_id:
            raise ValueError(
                "Top-level ASBIEPs must belong to the same release. "
                "Please choose a top-level ASBIEP from the same release and try again."
            )
        if reuse_top.asbiep_id is None:
            raise ValueError(
                "The requested top-level ASBIEP does not have an ASBIEP to reuse. "
                "Please choose a top-level ASBIEP that has a root ASBIEP and try again."
            )
        ascc_manifest = await self._session.get(AsccManifest, asbie.based_ascc_manifest_id)
        if ascc_manifest is None:
            raise LookupError(self._not_found_message("ASCC manifest", asbie.based_ascc_manifest_id))
        reuse_asbiep = await self._session.get(Asbiep, reuse_top.asbiep_id)
        if reuse_asbiep is None:
            raise LookupError(self._not_found_message("ASBIEP", reuse_top.asbiep_id))
        if reuse_asbiep.based_asccp_manifest_id != ascc_manifest.to_asccp_manifest_id:
            raise ValueError(
                "ASBIE's ASCC target ASCCP does not match the reused top-level ASBIEP ASCCP. "
                "Please choose a compatible top-level ASBIEP and try again."
            )

        asbie.to_asbiep_id = reuse_asbiep.asbiep_id
        asbie.is_used = True
        asbie.last_updated_by = requester_user_id
        asbie.last_update_timestamp = datetime.now(timezone.utc).replace(tzinfo=None)
        return reuse_asbiep.asbiep_id

    async def remove_reused_top_level_asbiep(
        self,
        asbie_id: int,
        requester_user_id: AppUserId,
        is_admin: bool,
    ) -> None:
        """Remove reused ASBIEP by creating owner-local ASBIEP and ABIE.

        Args:
            asbie_id: ASBIE identifier.
            requester_user_id: Requesting user identifier.
            is_admin: Whether requester has admin role.
        """
        asbie = await self._session.get(Asbie, asbie_id)
        if asbie is None:
            raise LookupError(self._not_found_message("ASBIE", asbie_id))
        owner_top = await self._session.get(TopLevelAsbiep, asbie.owner_top_level_asbiep_id)
        if owner_top is None:
            raise LookupError(self._not_found_message("top-level ASBIEP", asbie.owner_top_level_asbiep_id))
        if asbie.to_asbiep_id is None:
            raise ValueError(
                "ASBIE has no target ASBIEP. Please verify that the ASBIE is fully initialized and try again."
            )
        current_to_asbiep = await self._session.get(Asbiep, asbie.to_asbiep_id)
        if current_to_asbiep is None:
            raise LookupError(self._not_found_message("ASBIEP", asbie.to_asbiep_id))
        if current_to_asbiep.owner_top_level_asbiep_id == asbie.owner_top_level_asbiep_id:
            raise ValueError(
                "ASBIE is not using a reused top-level ASBIEP. "
                "Please verify that the ASBIE is currently reusing another top-level ASBIEP."
            )
        ascc_manifest = await self._session.get(AsccManifest, asbie.based_ascc_manifest_id)
        if ascc_manifest is None:
            raise LookupError(self._not_found_message("ASCC manifest", asbie.based_ascc_manifest_id))
        asccp_manifest = await self._session.get(AsccpManifest, ascc_manifest.to_asccp_manifest_id)
        if asccp_manifest is None:
            raise LookupError(self._not_found_message("ASCCP manifest", ascc_manifest.to_asccp_manifest_id))
        asbiep_path = f"{asbie.path}>ASCCP-{asccp_manifest.asccp_manifest_id}"
        asbiep_hash_path = hashlib.sha256(asbiep_path.encode()).hexdigest()
        replacement_asbiep = (
            await self._session.execute(
                select(Asbiep).where(
                    Asbiep.owner_top_level_asbiep_id == asbie.owner_top_level_asbiep_id,
                    Asbiep.hash_path == asbiep_hash_path,
                    Asbiep.based_asccp_manifest_id == asccp_manifest.asccp_manifest_id,
                )
            )
        ).scalar_one_or_none()
        now = datetime.now(timezone.utc).replace(tzinfo=None)
        if replacement_asbiep is None:
            role_abie_path = f"{asbie.path}>ASCCP-{asccp_manifest.asccp_manifest_id}>ACC-{asccp_manifest.role_of_acc_manifest_id}"
            role_abie = Abie(
                guid=new_guid(),
                based_acc_manifest_id=asccp_manifest.role_of_acc_manifest_id,
                path=role_abie_path,
                hash_path=hashlib.sha256(role_abie_path.encode()).hexdigest(),
                definition=None,
                remark=None,
                biz_term=None,
                owner_top_level_asbiep_id=asbie.owner_top_level_asbiep_id,
                created_by=requester_user_id,
                creation_timestamp=now,
                last_updated_by=requester_user_id,
                last_update_timestamp=now,
            )
            self._session.add(role_abie)
            await self._session.flush()
            replacement_asbiep = Asbiep(
                guid=new_guid(),
                based_asccp_manifest_id=asccp_manifest.asccp_manifest_id,
                path=asbiep_path,
                hash_path=asbiep_hash_path,
                role_of_abie_id=role_abie.abie_id,
                display_name=None,
                definition=None,
                biz_term=None,
                remark=None,
                last_updated_by=requester_user_id,
                created_by=requester_user_id,
                creation_timestamp=now,
                last_update_timestamp=now,
                owner_top_level_asbiep_id=asbie.owner_top_level_asbiep_id,
            )
            self._session.add(replacement_asbiep)
            await self._session.flush()
        asbie.to_asbiep_id = replacement_asbiep.asbiep_id
        asbie.last_updated_by = requester_user_id
        asbie.last_update_timestamp = now

    async def _get_top_level_with_root_asbiep(self, *, top_level_asbiep_id: int) -> tuple[TopLevelAsbiep, Asbiep]:
        """Return top-level ASBIEP and its root ASBIEP.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.

        Returns:
            Result of the operation.
        """
        top_level = await self._session.get(TopLevelAsbiep, top_level_asbiep_id)
        if top_level is None:
            raise LookupError(self._not_found_message("top-level ASBIEP", top_level_asbiep_id))
        if top_level.asbiep_id is None:
            raise ValueError(
                f"Top-level ASBIEP {top_level_asbiep_id} does not have a root ASBIEP. "
                "Please verify that the BIE is fully initialized and try again."
            )
        asbiep = await self._session.get(Asbiep, top_level.asbiep_id)
        if asbiep is None:
            raise LookupError(self._not_found_message("ASBIEP", top_level.asbiep_id))
        return top_level, asbiep

    async def _get_default_primitive_for_dt_sc(
        self,
        *,
        dt_sc_id: DataTypeSupplementaryComponentId,
        owner_dt_manifest_id: DataTypeManifestId | None = None,
    ) -> PrimitiveRestrictionRow | None:
        """Resolve default primitive restriction for a DT supplementary component.

        Args:
            dt_sc_id: DT supplementary component identifier.
            owner_dt_manifest_id: Owning DT manifest identifier used as a fallback.

        Returns:
            Result of the operation.
        """
        row = (
            await self._session.execute(
                select(DtScAwdPri)
                .where(
                    DtScAwdPri.dt_sc_id == dt_sc_id,
                    DtScAwdPri.is_default == True,
                )
                .order_by(DtScAwdPri.release_id.desc(), DtScAwdPri.dt_sc_awd_pri_id.desc())
            )
        ).scalars().first()
        if row is not None:
            return PrimitiveRestrictionRow(
                xbt_manifest_id=row.xbt_manifest_id,
                code_list_manifest_id=row.code_list_manifest_id,
                agency_id_list_manifest_id=row.agency_id_list_manifest_id,
            )
        if owner_dt_manifest_id is None:
            return None
        return await self._get_default_primitive_for_dt_manifest(owner_dt_manifest_id)

    @staticmethod
    def _build_bbie_sc_path(
        *,
        bbiep_path: str,
        bdt_manifest_id: DataTypeManifestId,
        based_dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
    ) -> str:
        """Build the canonical BBIE_SC path used by UI and API flows."""
        return f"{bbiep_path}>DT-{bdt_manifest_id}>DT_SC-{based_dt_sc_manifest_id}"


def _value_constraint(default_value: str | None, fixed_value: str | None) -> ValueConstraintRow | None:
    """Internal helper for value constraint.

    Args:
        default_value: Value for `default_value`.
        fixed_value: Value for `fixed_value`.

    Returns:
        Result of the operation.
    """
    if default_value is None and fixed_value is None:
        return None
    return ValueConstraintRow(default_value=default_value, fixed_value=fixed_value)


def _facet_row(min_length: int | None, max_length: int | None, pattern: str | None) -> FacetRow | None:
    """Internal helper for facet row.

    Args:
        min_length: Value for `min_length`.
        max_length: Value for `max_length`.
        pattern: Value for `pattern`.

    Returns:
        Result of the operation.
    """
    if min_length is None and max_length is None and pattern is None:
        return None
    return FacetRow(facet_min_length=min_length, facet_max_length=max_length, facet_pattern=pattern)


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
