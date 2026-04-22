"""MariaDB repository implementation for Code Lists."""


from __future__ import annotations

from collections import defaultdict
from datetime import datetime
import secrets
from typing import Any, Literal

from sqlalchemy import bindparam, func, select, text
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.contracts.code_list import CodeListRepositoryContract
from app.repositories.contracts.log import LogRepositoryContract
from app.repositories.models import (
    CodeListValueRow,
    LibrarySummaryRow,
    LogSummaryRow,
    NamespaceSummaryRow,
    ReleaseSummaryRow,
)
from app.repositories.models.code_list import CodeListRow
from app.repositories.vendors.mariadb.models.app_user import AppUser
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
    AppUserId,
    CodeListManifestId,
    CodeListValueManifestId,
    ReleaseId,
)


class MariaDbCodeListRepository(CodeListRepositoryContract):
    """MariaDB-backed repository for code list read and command operations."""

    def __init__(self, session: AsyncSession, log_repo: LogRepositoryContract):
        """Initialize the repository."""
        self._session = session
        self._log_repo = log_repo

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
        """List code lists."""
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

        rows = [
            dict(row._mapping)
            for row in (
                await self._session.execute(
                    _build_base_query().where(*where_clauses).order_by(*_build_order_by(sorts)).limit(limit).offset(offset)
                )
            ).all()
        ]

        if not rows:
            return total, []

        values_by_manifest = await _values_by_manifest_ids(self._session, [int(row["code_list_manifest_id"]) for row in rows])
        items = [_to_code_list_row(row, values_by_manifest.get(int(row["code_list_manifest_id"]), [])) for row in rows]
        return total, items

    async def get(self, code_list_manifest_id: CodeListManifestId) -> CodeListRow | None:
        """Get one code list by manifest ID."""
        row = (
            await self._session.execute(
                _build_base_query().where(CodeListManifest.code_list_manifest_id == int(code_list_manifest_id))
            )
        ).first()
        if row is None:
            return None

        row_dict = dict(row._mapping)
        values_by_manifest = await _values_by_manifest_ids(self._session, [int(row_dict["code_list_manifest_id"])])
        return _to_code_list_row(row_dict, values_by_manifest.get(int(row_dict["code_list_manifest_id"]), []))

    async def get_code_list_manifest_id_by_value_manifest_id(
        self,
        code_list_value_manifest_id: CodeListValueManifestId,
    ) -> CodeListManifestId | None:
        """Resolve the owning code list manifest identifier from a code list value manifest identifier."""
        stmt = select(CodeListValueManifest.code_list_manifest_id).where(
            CodeListValueManifest.code_list_value_manifest_id == int(code_list_value_manifest_id)
        )
        value = (await self._session.execute(stmt)).scalar_one_or_none()
        return None if value is None else CodeListManifestId(int(value))

    async def create_code_list(
        self,
        *,
        release_id: ReleaseId,
        name: str,
        based_code_list_manifest_id: CodeListManifestId | None,
        version_id: str | None,
        list_id: str | None,
        agency_id_list_value_manifest_id: int | None,
        definition: str | None,
        definition_source: str | None,
        remark: str | None,
        namespace_id: int | None,
        deprecated: bool | None,
        extensible_indicator: bool | None,
        requester_user_id: AppUserId,
        requester_is_developer: bool,
    ) -> CodeListManifestId:
        """Create a new code list."""
        now = datetime.utcnow()

        based_manifest = None
        based_code_list = None
        if based_code_list_manifest_id is not None:
            based_manifest = await self._session.get(CodeListManifest, int(based_code_list_manifest_id))
            if based_manifest is None:
                raise LookupError(
                    f"No code list exists with manifest ID {int(based_code_list_manifest_id)}. "
                    "Please verify the identifier and try again."
                )
            based_code_list = await self._session.get(CodeList, int(based_manifest.code_list_id))
            if based_code_list is None:
                raise LookupError(
                    f"No code list exists with manifest ID {int(based_code_list_manifest_id)}. "
                    "Please verify the identifier and try again."
                )

        code_list = CodeList(
            guid=_random_guid(),
            enum_type_guid=None,
            name=str(name),
            list_id=list_id if list_id is not None else _random_guid(),
            version_id=(
                str(version_id)
                if version_id is not None
                else (str(based_code_list.version_id) if based_code_list is not None else "1")
            ),
            definition=definition,
            remark=remark,
            definition_source=definition_source,
            namespace_id=namespace_id,
            based_code_list_id=int(based_code_list.code_list_id) if based_code_list is not None else None,
            extensible_indicator=(
                bool(extensible_indicator)
                if extensible_indicator is not None
                else (
                    bool(based_code_list.extensible_indicator)
                    if based_code_list is not None and requester_is_developer
                    else bool(requester_is_developer)
                )
            ),
            is_deprecated=bool(deprecated) if deprecated is not None else False,
            replacement_code_list_id=None,
            owner_user_id=int(requester_user_id),
            created_by=int(requester_user_id),
            last_updated_by=int(requester_user_id),
            creation_timestamp=now,
            last_update_timestamp=now,
            state="WIP",
            prev_code_list_id=None,
            next_code_list_id=None,
        )
        self._session.add(code_list)
        await self._session.flush()
        await self._session.refresh(code_list)

        manifest = CodeListManifest(
            release_id=int(release_id),
            code_list_id=int(code_list.code_list_id),
            based_code_list_manifest_id=int(based_manifest.code_list_manifest_id) if based_manifest is not None else None,
            agency_id_list_value_manifest_id=(
                agency_id_list_value_manifest_id
                if agency_id_list_value_manifest_id is not None
                else (
                    int(based_manifest.agency_id_list_value_manifest_id)
                    if based_manifest is not None and based_manifest.agency_id_list_value_manifest_id is not None
                    else None
                )
            ),
            conflict=False,
            log_id=None,
            replacement_code_list_manifest_id=None,
            prev_code_list_manifest_id=None,
            next_code_list_manifest_id=None,
        )
        self._session.add(manifest)
        await self._session.flush()
        await self._session.refresh(manifest)

        if based_manifest is not None:
            based_value_rows = (
                await self._session.execute(
                    select(CodeListValueManifest, CodeListValue)
                    .join(CodeListValue, CodeListValue.code_list_value_id == CodeListValueManifest.code_list_value_id)
                    .where(CodeListValueManifest.code_list_manifest_id == int(based_manifest.code_list_manifest_id))
                    .order_by(CodeListValueManifest.code_list_value_manifest_id.asc())
                )
            ).all()
            for based_value_manifest, based_value in based_value_rows:
                value = CodeListValue(
                    guid=_random_guid(),
                    code_list_id=int(code_list.code_list_id),
                    based_code_list_value_id=int(based_value.code_list_value_id),
                    value=str(based_value.value),
                    meaning=str(based_value.meaning) if based_value.meaning is not None else None,
                    definition=str(based_value.definition) if based_value.definition is not None else None,
                    definition_source=(
                        str(based_value.definition_source)
                        if based_value.definition_source is not None
                        else None
                    ),
                    is_deprecated=bool(based_value.is_deprecated),
                    replacement_code_list_value_id=(
                        int(based_value.replacement_code_list_value_id)
                        if based_value.replacement_code_list_value_id is not None
                        else None
                    ),
                    created_by=int(requester_user_id),
                    owner_user_id=int(requester_user_id),
                    last_updated_by=int(requester_user_id),
                    creation_timestamp=now,
                    last_update_timestamp=now,
                    prev_code_list_value_id=None,
                    next_code_list_value_id=None,
                )
                self._session.add(value)
                await self._session.flush()
                await self._session.refresh(value)

                self._session.add(
                    CodeListValueManifest(
                        release_id=int(release_id),
                        code_list_manifest_id=int(manifest.code_list_manifest_id),
                        code_list_value_id=int(value.code_list_value_id),
                        based_code_list_value_manifest_id=int(based_value_manifest.code_list_value_manifest_id),
                        conflict=False,
                        replacement_code_list_value_manifest_id=None,
                        prev_code_list_value_manifest_id=None,
                        next_code_list_value_manifest_id=None,
                    )
                )

        await self._session.flush()
        return CodeListManifestId(int(manifest.code_list_manifest_id))

    async def update_code_list(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
        name: str | None,
        name_set: bool,
        version_id: str | None,
        version_id_set: bool,
        list_id: str | None,
        list_id_set: bool,
        agency_id_list_value_manifest_id: int | None,
        agency_id_list_value_manifest_id_set: bool,
        definition: str | None,
        definition_set: bool,
        definition_source: str | None,
        definition_source_set: bool,
        remark: str | None,
        remark_set: bool,
        namespace_id: int | None,
        namespace_id_set: bool,
        deprecated: bool | None,
        deprecated_set: bool,
        extensible_indicator: bool | None,
        extensible_indicator_set: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update mutable code list fields."""
        manifest, code_list = await self._get_manifest_and_code_list(code_list_manifest_id)
        if manifest is None or code_list is None:
            return False

        now = datetime.utcnow()
        updates_applied = False

        if name_set and name != code_list.name:
            code_list.name = name
            updates_applied = True
        if version_id_set:
            if version_id is None:
                raise ValueError("version_id must not be null.")
            if version_id != code_list.version_id:
                code_list.version_id = version_id
                updates_applied = True
        if list_id_set:
            if list_id is None:
                raise ValueError("list_id must not be null.")
            if list_id != code_list.list_id:
                code_list.list_id = list_id
                updates_applied = True
        if agency_id_list_value_manifest_id_set and agency_id_list_value_manifest_id != manifest.agency_id_list_value_manifest_id:
            manifest.agency_id_list_value_manifest_id = agency_id_list_value_manifest_id
            updates_applied = True
        if definition_set and definition != code_list.definition:
            code_list.definition = definition
            updates_applied = True
        if definition_source_set and definition_source != code_list.definition_source:
            code_list.definition_source = definition_source
            updates_applied = True
        if remark_set and remark != code_list.remark:
            code_list.remark = remark
            updates_applied = True
        if namespace_id_set and namespace_id != code_list.namespace_id:
            code_list.namespace_id = namespace_id
            updates_applied = True
        if deprecated_set:
            if deprecated is None:
                raise ValueError("deprecated must not be null.")
            if bool(deprecated) != bool(code_list.is_deprecated):
                code_list.is_deprecated = bool(deprecated)
                updates_applied = True
        if extensible_indicator_set:
            if extensible_indicator is None:
                raise ValueError("extensible_indicator must not be null.")
            if bool(extensible_indicator) != bool(code_list.extensible_indicator):
                code_list.extensible_indicator = bool(extensible_indicator)
                updates_applied = True

        if not updates_applied:
            return False

        code_list.last_updated_by = int(requester_user_id)
        code_list.last_update_timestamp = now
        await self._session.flush()
        return True

    async def create_code_list_value(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
        value: str,
        meaning: str | None,
        definition: str | None,
        definition_source: str | None,
        deprecated: bool,
        requester_user_id: AppUserId,
    ) -> CodeListValueManifestId:
        """Create a new code list value under a manifest."""
        manifest, code_list = await self._get_manifest_and_code_list(code_list_manifest_id)
        if manifest is None or code_list is None:
            raise LookupError(
                f"No code list exists with manifest ID {int(code_list_manifest_id)}. Please verify the identifier and try again."
            )

        now = datetime.utcnow()
        code_list_value = CodeListValue(
            guid=_random_guid(),
            code_list_id=int(code_list.code_list_id),
            based_code_list_value_id=None,
            value=value,
            meaning=meaning,
            definition=definition,
            definition_source=definition_source,
            is_deprecated=bool(deprecated),
            replacement_code_list_value_id=None,
            created_by=int(requester_user_id),
            owner_user_id=int(requester_user_id),
            last_updated_by=int(requester_user_id),
            creation_timestamp=now,
            last_update_timestamp=now,
            prev_code_list_value_id=None,
            next_code_list_value_id=None,
        )
        self._session.add(code_list_value)
        await self._session.flush()
        await self._session.refresh(code_list_value)

        value_manifest = CodeListValueManifest(
            release_id=int(manifest.release_id),
            code_list_manifest_id=int(manifest.code_list_manifest_id),
            code_list_value_id=int(code_list_value.code_list_value_id),
            based_code_list_value_manifest_id=None,
            conflict=False,
            replacement_code_list_value_manifest_id=None,
            prev_code_list_value_manifest_id=None,
            next_code_list_value_manifest_id=None,
        )
        self._session.add(value_manifest)
        await self._session.flush()
        await self._session.refresh(value_manifest)
        return CodeListValueManifestId(int(value_manifest.code_list_value_manifest_id))

    async def update_code_list_value(
        self,
        *,
        code_list_value_manifest_id: CodeListValueManifestId,
        value: str | None,
        value_set: bool,
        meaning: str | None,
        meaning_set: bool,
        definition: str | None,
        definition_set: bool,
        definition_source: str | None,
        definition_source_set: bool,
        deprecated: bool | None,
        deprecated_set: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update a mutable code list value."""
        value_manifest = await self._session.get(CodeListValueManifest, int(code_list_value_manifest_id))
        if value_manifest is None:
            return False
        code_list_value = await self._session.get(CodeListValue, int(value_manifest.code_list_value_id))
        if code_list_value is None:
            return False

        now = datetime.utcnow()
        updates_applied = False

        if value_set:
            if value is None:
                raise ValueError("value must not be null.")
            if value != code_list_value.value:
                code_list_value.value = value
                updates_applied = True
        if meaning_set and meaning != code_list_value.meaning:
            code_list_value.meaning = meaning
            updates_applied = True
        if definition_set and definition != code_list_value.definition:
            code_list_value.definition = definition
            updates_applied = True
        if definition_source_set and definition_source != code_list_value.definition_source:
            code_list_value.definition_source = definition_source
            updates_applied = True
        if deprecated_set:
            if deprecated is None:
                raise ValueError("deprecated must not be null.")
            if bool(deprecated) != bool(code_list_value.is_deprecated):
                code_list_value.is_deprecated = bool(deprecated)
                updates_applied = True

        if not updates_applied:
            return False

        code_list_value.last_updated_by = int(requester_user_id)
        code_list_value.last_update_timestamp = now
        await self._session.flush()
        return True

    async def delete_code_list_value(
        self,
        *,
        code_list_value_manifest_id: CodeListValueManifestId,
    ) -> bool:
        """Delete a code list value and its manifest row."""
        value_manifest = await self._session.get(CodeListValueManifest, int(code_list_value_manifest_id))
        if value_manifest is None:
            return False
        code_list_value = await self._session.get(CodeListValue, int(value_manifest.code_list_value_id))
        await self._session.delete(value_manifest)
        await self._session.flush()
        if code_list_value is not None:
            await self._session.delete(code_list_value)
        await self._session.flush()
        return True

    async def transfer_code_list_ownership(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
        requester_user_id: AppUserId,
        target_user_id: AppUserId,
    ) -> bool:
        """Transfer code list ownership to another user."""
        manifest, code_list = await self._get_manifest_and_code_list(code_list_manifest_id)
        if manifest is None or code_list is None:
            return False

        target_owner = await self._session.get(AppUser, int(target_user_id))
        if target_owner is None:
            raise LookupError(f"No user exists with ID {int(target_user_id)}. Please verify the identifier and try again.")
        if target_owner.is_enabled is not None and not bool(target_owner.is_enabled):
            raise ValueError(
                "Cannot transfer ownership to a disabled user. Please choose an enabled user account and try again."
            )

        now = datetime.utcnow()
        code_list.owner_user_id = int(target_user_id)
        code_list.last_updated_by = int(requester_user_id)
        code_list.last_update_timestamp = now
        await self._session.execute(
            text(
                "UPDATE code_list_value "
                "SET owner_user_id = :target_user_id, "
                "last_updated_by = :requester_user_id, "
                "last_update_timestamp = :timestamp "
                "WHERE code_list_id = :code_list_id"
            ),
            {
                "target_user_id": int(target_user_id),
                "requester_user_id": int(requester_user_id),
                "timestamp": now,
                "code_list_id": int(code_list.code_list_id),
            },
        )
        await self._session.flush()
        return True

    async def change_code_list_state(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
        state: str,
        restore_owner: bool,
        implicit_move: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update the lifecycle state of a code list."""
        _, code_list = await self._get_manifest_and_code_list(code_list_manifest_id)
        if code_list is None:
            return False

        now = datetime.utcnow()
        code_list.state = state
        if restore_owner:
            code_list.owner_user_id = int(requester_user_id)
        if not implicit_move:
            code_list.last_updated_by = int(requester_user_id)
            code_list.last_update_timestamp = now
        await self._session.flush()
        return True

    async def revise_code_list(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
        requester_user_id: AppUserId,
    ) -> bool:
        """Create a revised code list working copy."""
        manifest, current = await self._get_manifest_and_code_list(code_list_manifest_id)
        if manifest is None or current is None:
            return False

        now = datetime.utcnow()
        next_code_list = CodeList(
            guid=str(current.guid),
            enum_type_guid=str(current.enum_type_guid) if current.enum_type_guid is not None else None,
            name=str(current.name) if current.name is not None else None,
            list_id=str(current.list_id),
            version_id=f"{current.version_id}_New",
            definition=str(current.definition) if current.definition is not None else None,
            remark=str(current.remark) if current.remark is not None else None,
            definition_source=str(current.definition_source) if current.definition_source is not None else None,
            namespace_id=int(current.namespace_id) if current.namespace_id is not None else None,
            based_code_list_id=int(current.based_code_list_id) if current.based_code_list_id is not None else None,
            extensible_indicator=bool(current.extensible_indicator),
            is_deprecated=bool(current.is_deprecated),
            replacement_code_list_id=(
                int(current.replacement_code_list_id) if current.replacement_code_list_id is not None else None
            ),
            owner_user_id=int(requester_user_id),
            created_by=int(requester_user_id),
            last_updated_by=int(requester_user_id),
            creation_timestamp=now,
            last_update_timestamp=now,
            state="WIP",
            prev_code_list_id=int(current.code_list_id),
            next_code_list_id=None,
        )
        self._session.add(next_code_list)
        await self._session.flush()
        await self._session.refresh(next_code_list)

        current.next_code_list_id = int(next_code_list.code_list_id)
        manifest.code_list_id = int(next_code_list.code_list_id)

        value_rows = (
            await self._session.execute(
                select(CodeListValueManifest, CodeListValue)
                .join(CodeListValue, CodeListValue.code_list_id == int(current.code_list_id))
                .where(
                    CodeListValueManifest.code_list_manifest_id == int(code_list_manifest_id),
                    CodeListValueManifest.code_list_value_id == CodeListValue.code_list_value_id,
                )
                .order_by(CodeListValueManifest.code_list_value_manifest_id.asc())
            )
        ).all()
        for value_manifest, prev_value in value_rows:
            next_value = CodeListValue(
                guid=str(prev_value.guid),
                code_list_id=int(next_code_list.code_list_id),
                based_code_list_value_id=(
                    int(prev_value.based_code_list_value_id) if prev_value.based_code_list_value_id is not None else None
                ),
                value=str(prev_value.value),
                meaning=str(prev_value.meaning) if prev_value.meaning is not None else None,
                definition=str(prev_value.definition) if prev_value.definition is not None else None,
                definition_source=(
                    str(prev_value.definition_source) if prev_value.definition_source is not None else None
                ),
                is_deprecated=bool(prev_value.is_deprecated),
                replacement_code_list_value_id=(
                    int(prev_value.replacement_code_list_value_id)
                    if prev_value.replacement_code_list_value_id is not None
                    else None
                ),
                created_by=int(requester_user_id),
                owner_user_id=int(requester_user_id),
                last_updated_by=int(requester_user_id),
                creation_timestamp=now,
                last_update_timestamp=now,
                prev_code_list_value_id=int(prev_value.code_list_value_id),
                next_code_list_value_id=None,
            )
            self._session.add(next_value)
            await self._session.flush()
            await self._session.refresh(next_value)
            prev_value.next_code_list_value_id = int(next_value.code_list_value_id)
            value_manifest.code_list_value_id = int(next_value.code_list_value_id)

        await self._session.flush()
        return True

    async def cancel_code_list(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
    ) -> bool:
        """Cancel the current code list revision and restore the previous stable revision."""
        manifest, current = await self._get_manifest_and_code_list(code_list_manifest_id)
        if manifest is None or current is None:
            return False
        if current.prev_code_list_id is None:
            raise ValueError("Not found previous revision")

        prev_code_list = await self._session.get(CodeList, int(current.prev_code_list_id))
        if prev_code_list is None:
            raise ValueError("Not found previous revision")

        manifest.code_list_id = int(prev_code_list.code_list_id)
        prev_code_list.next_code_list_id = None

        value_manifests = (
            await self._session.execute(
                select(CodeListValueManifest).where(CodeListValueManifest.code_list_manifest_id == int(code_list_manifest_id))
            )
        ).scalars().all()
        for value_manifest in value_manifests:
            current_value = await self._session.get(CodeListValue, int(value_manifest.code_list_value_id))
            if current_value is None:
                continue

            if current_value.prev_code_list_value_id is None:
                await self._session.delete(value_manifest)
            else:
                prev_value = await self._session.get(CodeListValue, int(current_value.prev_code_list_value_id))
                if prev_value is None:
                    raise ValueError("Not found previous code list-value revision")
                prev_value.next_code_list_value_id = None
                value_manifest.code_list_value_id = int(prev_value.code_list_value_id)

            await self._session.delete(current_value)

        await self._session.flush()
        await self._session.delete(current)
        await self._session.flush()
        return True

    async def discard_code_list(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
    ) -> bool:
        """Discard a Deleted code list permanently."""
        manifest, code_list = await self._get_manifest_and_code_list(code_list_manifest_id)
        if manifest is None or code_list is None:
            return False

        if manifest.log_id is not None:
            manifest.log_id = None
            await self._session.flush()

        await self._session.execute(
            text("UPDATE log SET prev_log_id = NULL, next_log_id = NULL WHERE reference = :reference"),
            {"reference": str(code_list.guid)},
        )
        await self._session.execute(
            text("DELETE FROM log WHERE reference = :reference"),
            {"reference": str(code_list.guid)},
        )
        await self._session.execute(
            text("DELETE FROM module_code_list_manifest WHERE code_list_manifest_id = :code_list_manifest_id"),
            {"code_list_manifest_id": int(code_list_manifest_id)},
        )

        value_manifest_rows = (
            await self._session.execute(
                select(CodeListValueManifest).where(CodeListValueManifest.code_list_manifest_id == int(code_list_manifest_id))
            )
        ).scalars().all()
        value_ids = [int(row.code_list_value_id) for row in value_manifest_rows]
        if value_manifest_rows:
            await self._session.execute(
                text("DELETE FROM code_list_value_manifest WHERE code_list_manifest_id = :code_list_manifest_id"),
                {"code_list_manifest_id": int(code_list_manifest_id)},
            )
        if value_ids:
            await self._session.execute(
                text("DELETE FROM code_list_value WHERE code_list_value_id IN :value_ids").bindparams(
                    bindparam("value_ids", expanding=True)
                ),
                {"value_ids": tuple(value_ids)},
            )

        await self._session.delete(manifest)
        await self._session.flush()
        await self._session.delete(code_list)
        await self._session.flush()
        return True

    async def append_code_list_log(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
        requester_user_id: AppUserId,
        action: str,
    ) -> int:
        """Append a new log row and update the manifest head."""
        manifest, code_list = await self._get_manifest_and_code_list(code_list_manifest_id)
        if manifest is None or code_list is None:
            raise LookupError(
                f"No code list exists with manifest ID {int(code_list_manifest_id)}. Please verify the identifier and try again."
            )

        log_id = await self._log_repo.append_component_log(
            reference=str(code_list.guid),
            current_log_id=int(manifest.log_id) if manifest.log_id is not None else None,
            requester_user_id=requester_user_id,
            action=action,
            timestamp=datetime.utcnow(),
        )
        manifest.log_id = int(log_id)
        await self._session.flush()
        return int(log_id)

    async def revert_code_list_log_to_stable_state(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
    ) -> int:
        """Revert the latest revised logs and restore the stable log head."""
        manifest, code_list = await self._get_manifest_and_code_list(code_list_manifest_id)
        if manifest is None or code_list is None:
            raise LookupError(
                f"No code list exists with manifest ID {int(code_list_manifest_id)}. Please verify the identifier and try again."
            )
        stable_log_id = await self._log_repo.revert_component_log_to_stable_state(
            reference=str(code_list.guid),
            current_log_id=int(manifest.log_id) if manifest.log_id is not None else None,
        )
        manifest.log_id = int(stable_log_id)
        await self._session.flush()
        return int(stable_log_id)

    async def _get_manifest_and_code_list(
        self,
        code_list_manifest_id: CodeListManifestId,
    ) -> tuple[CodeListManifest | None, CodeList | None]:
        manifest = await self._session.get(CodeListManifest, int(code_list_manifest_id))
        if manifest is None:
            return None, None
        code_list = await self._session.get(CodeList, int(manifest.code_list_id))
        return manifest, code_list
def _random_guid() -> str:
    return secrets.token_hex(16)


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
    clauses: list[object] = [CodeListManifest.release_id.in_([int(release_id), *[int(x) for x in dependent_release_ids]])]
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
                    CodeListValue.definition_source,
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
                definition_source=(
                    str(value_row["definition_source"]) if value_row.get("definition_source") is not None else None
                ),
                is_deprecated=bool(value_row["is_deprecated"]),
                is_developer_default=False,
                is_user_default=False,
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
    if isinstance(value, datetime):
        return value
    return datetime.fromisoformat(str(value).replace("Z", "+00:00"))


def _library_summary(row: dict[str, Any]) -> LibrarySummaryRow:
    return LibrarySummaryRow(library_id=row["library_id"], name=str(row["library_name"]))


def _release_summary(row: dict[str, Any]) -> ReleaseSummaryRow:
    return ReleaseSummaryRow(
        release_id=row["release_id"],
        release_num=str(row["release_num"] or ""),
        state=str(row["release_state"] or ""),
    )


def _namespace_summary(row: dict[str, Any]) -> NamespaceSummaryRow | None:
    namespace_id = row.get("namespace_id")
    if namespace_id is None:
        return None
    return NamespaceSummaryRow(
        namespace_id=int(namespace_id),
        prefix=str(row["namespace_prefix"]) if row.get("namespace_prefix") is not None else None,
        uri=str(row["namespace_uri"]),
    )


def _log_summary(row: dict[str, Any]) -> LogSummaryRow | None:
    log_id = row.get("log_id")
    if log_id is None:
        return None
    return LogSummaryRow(
        log_id=int(log_id),
        revision_num=int(row.get("revision_num") or 0),
        revision_tracking_num=int(row.get("revision_tracking_num") or 0),
    )
