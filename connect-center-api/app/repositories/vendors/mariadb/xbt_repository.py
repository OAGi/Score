"""MariaDB repository implementation for XBT."""


from __future__ import annotations

from datetime import datetime
from typing import Any

from sqlalchemy import and_, select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import aliased

from app.repositories.contracts.xbt import XbtRepositoryContract
from app.repositories.models import LibrarySummaryRow, LogSummaryRow, ReleaseSummaryRow, XbtSummaryRow
from app.repositories.models.xbt import XbtRow
from app.repositories.vendors.mariadb.models.library import Library
from app.repositories.vendors.mariadb.models.log import Log
from app.repositories.vendors.mariadb.models.release import Release
from app.repositories.vendors.mariadb.models.xbt import Xbt, XbtManifest
from app.types.identifiers import (
    XbtManifestId,
)


class MariaDbXbtRepository(XbtRepositoryContract):
    """MariaDB-backed repository for XBT read operations."""

    def __init__(self, session: AsyncSession):
        """Initialize MariaDbXbtRepository.

        Args:
            session: Database session bound to the current request.
        """
        self._session = session

    async def get(self, xbt_manifest_id: XbtManifestId) -> XbtRow | None:
        """Handle get.

        Args:
            xbt_manifest_id: XBT manifest identifier.

        Returns:
            Result of the operation.
        """
        sub_xbt = aliased(Xbt)
        sub_manifest = aliased(XbtManifest)
        sub_release = aliased(Release)
        sub_library = aliased(Library)

        stmt = (
            select(
                XbtManifest.xbt_manifest_id,
                XbtManifest.xbt_id,
                Xbt.guid,
                Xbt.name,
                Xbt.builtIn_type,
                Xbt.jbt_draft05_map,
                Xbt.openapi30_map,
                Xbt.avro_map,
                Xbt.schema_definition,
                Xbt.revision_doc,
                Xbt.state,
                Xbt.is_deprecated,
                Xbt.owner_user_id,
                Xbt.created_by,
                Xbt.last_updated_by,
                Xbt.creation_timestamp,
                Xbt.last_update_timestamp,
                Library.library_id,
                Library.name.label("library_name"),
                Release.release_id,
                Release.release_num,
                Release.state.label("release_state"),
                Log.log_id,
                Log.revision_num,
                Log.revision_tracking_num,
                sub_manifest.xbt_manifest_id.label("sub_xbt_manifest_id"),
                sub_xbt.xbt_id.label("sub_xbt_id"),
                sub_xbt.guid.label("sub_guid"),
                sub_xbt.name.label("sub_name"),
                sub_xbt.builtIn_type.label("sub_builtIn_type"),
                sub_library.library_id.label("sub_library_id"),
                sub_library.name.label("sub_library_name"),
                sub_release.release_id.label("sub_release_id"),
                sub_release.release_num.label("sub_release_num"),
                sub_release.state.label("sub_release_state"),
            )
            .select_from(XbtManifest)
            .join(Xbt, Xbt.xbt_id == XbtManifest.xbt_id)
            .join(Release, Release.release_id == XbtManifest.release_id)
            .join(Library, Library.library_id == Release.library_id)
            .outerjoin(Log, Log.log_id == XbtManifest.log_id)
            .outerjoin(sub_xbt, sub_xbt.xbt_id == Xbt.subtype_of_xbt_id)
            .outerjoin(
                sub_manifest,
                and_(
                    sub_manifest.xbt_id == sub_xbt.xbt_id,
                    sub_manifest.release_id == XbtManifest.release_id,
                ),
            )
            .outerjoin(sub_release, sub_release.release_id == sub_manifest.release_id)
            .outerjoin(sub_library, sub_library.library_id == sub_release.library_id)
            .where(XbtManifest.xbt_manifest_id == xbt_manifest_id)
            .limit(1)
        )
        result = (await self._session.execute(stmt)).first()
        if result is None:
            return None
        return self._to_row(dict(result._mapping))

    def _to_row(self, row: dict[str, Any]) -> XbtRow:
        """Internal helper for to row.

        Args:
            row: Repository row model to convert into a DTO.

        Returns:
            Result of the operation.
        """
        subtype = None
        if row.get("sub_xbt_manifest_id") is not None:
            subtype = XbtSummaryRow(
                xbt_manifest_id=row["sub_xbt_manifest_id"],
                xbt_id=row["sub_xbt_id"],
                guid=str(row["sub_guid"]),
                name=str(row["sub_name"]) if row.get("sub_name") is not None else None,
                builtIn_type=str(row["sub_builtIn_type"]) if row.get("sub_builtIn_type") is not None else None,
                library=LibrarySummaryRow(
                    library_id=row["sub_library_id"],
                    name=str(row["sub_library_name"]),
                ),
                release=ReleaseSummaryRow(
                    release_id=row["sub_release_id"],
                    release_num=(
                        str(row["sub_release_num"])
                        if row.get("sub_release_num") is not None
                        else None
                    ),
                    state=str(row["sub_release_state"]),
                ),
            )

        return XbtRow(
            xbt_manifest_id=row["xbt_manifest_id"],
            xbt_id=row["xbt_id"],
            guid=str(row["guid"]),
            name=str(row["name"]) if row.get("name") is not None else None,
            builtIn_type=str(row["builtIn_type"]) if row.get("builtIn_type") is not None else None,
            jbt_draft05_map=str(row["jbt_draft05_map"]) if row.get("jbt_draft05_map") is not None else None,
            openapi30_map=str(row["openapi30_map"]) if row.get("openapi30_map") is not None else None,
            avro_map=str(row["avro_map"]) if row.get("avro_map") is not None else None,
            subtype_of_xbt=subtype,
            schema_definition=(
                str(row["schema_definition"])
                if row.get("schema_definition") is not None
                else None
            ),
            revision_doc=str(row["revision_doc"]) if row.get("revision_doc") is not None else None,
            state=row["state"] if row.get("state") is not None else None,
            is_deprecated=bool(row["is_deprecated"]),
            library=LibrarySummaryRow(
                library_id=row["library_id"],
                name=str(row["library_name"]),
            ),
            release=ReleaseSummaryRow(
                release_id=row["release_id"],
                release_num=str(row["release_num"]) if row.get("release_num") is not None else None,
                state=str(row["release_state"]),
            ),
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


def _log_summary(row: dict[str, Any]) -> LogSummaryRow | None:
    """Internal helper for log summary.

    Args:
        row: Repository row model to convert into a DTO.

    Returns:
        Result of the operation.
    """
    log_id = row.get("log_id")
    if log_id is None:
        return None
    return LogSummaryRow(
        log_id=log_id,
        revision_num=row["revision_num"],
        revision_tracking_num=row["revision_tracking_num"],
    )
