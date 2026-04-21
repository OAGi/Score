"""Service layer for Code List operations in connectCenter."""


from __future__ import annotations

import logging

from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.repositories.contracts.code_list import CodeListRepositoryContract
from app.repositories.models.app_user import AppUserRow
from app.security import AuthenticatedUser
from app.services import load_users_by_ids, to_user_summary
from app.services.models import WhoAndWhen
from app.services.models.code_list import (
    CodeListServiceResult,
    CodeListState,
    CreateCodeListServiceResult,
    CreateCodeListValueServiceResult,
    CodeListValueServiceRecord,
    TransferCodeListOwnershipServiceResult,
    UpdateCodeListServiceResult,
    UpdateCodeListValueServiceResult,
)
from app.services.release_service import ReleaseService
from app.services.utils.date import DateRange
from app.services.utils.pagination import PaginationParams, PaginationResponse
from app.types.identifiers import CodeListManifestId, CodeListValueManifestId, ReleaseId
from app.types.unset import UNSET, UnsetType
from app.utils.core_component_constants import (
    NON_WORKING_BRANCH_CORE_COMPONENT_STATE_TRANSITIONS,
    WORKING_BRANCH_CORE_COMPONENT_STATE_TRANSITIONS,
)

logger = logging.getLogger("connectcenter.service.code_list")


class CodeListService:
    """Service for code list read and command operations."""

    _ORDER_BY_ALLOWED: set[str] = {
        "name",
        "list_id",
        "version_id",
        "definition",
        "creation_timestamp",
        "last_update_timestamp",
    }

    def __init__(
        self,
        code_list_repository: CodeListRepositoryContract,
        release_service: ReleaseService,
        account_service_repo: AppUserRepositoryContract,
        requester: AuthenticatedUser,
    ):
        self._repo = code_list_repository
        self._release_service = release_service
        self._account_service_repo = account_service_repo
        self._requester = requester

    async def list(
        self,
        *,
        release_id: ReleaseId,
        limit: int,
        offset: int,
        order_by: str | None = None,
        name: str | None = None,
        list_id: str | None = None,
        version_id: str | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
    ) -> PaginationResponse[CodeListServiceResult]:
        """List code lists for a release scope."""
        logger.info("list code_lists release_id=%d limit=%d offset=%d", int(release_id), limit, offset)
        pagination = PaginationParams.from_query(
            limit=limit,
            offset=offset,
            order_by=order_by,
            allowed_sort_columns=self._ORDER_BY_ALLOWED,
        )
        dependent_release_ids = await self._release_service.get_dependent_releases(release_id)
        total, rows = await self._repo.list(
            release_id=release_id,
            dependent_release_ids=dependent_release_ids,
            limit=pagination.limit,
            offset=pagination.offset,
            sorts=[(s.column, s.direction.upper()) for s in pagination.sorts],
            name=name,
            list_id=list_id,
            version_id=version_id,
            creation_timestamp_before=created_on.before if created_on else None,
            creation_timestamp_after=created_on.after if created_on else None,
            last_update_timestamp_before=last_updated_on.before if last_updated_on else None,
            last_update_timestamp_after=last_updated_on.after if last_updated_on else None,
        )
        user_ids = sorted(
            {
                user_id
                for row in rows
                for user_id in (row.owner_user_id, row.created_by, row.last_updated_by)
            },
            key=int,
        )
        users_by_id = await load_users_by_ids(self._account_service_repo, user_ids)
        items = [self._to_code_list_result(row, users_by_id=users_by_id) for row in rows]
        logger.info("list code_lists release_id=%d → %d/%d", int(release_id), len(items), total)
        return PaginationResponse(items=items, total=total, limit=pagination.limit, offset=pagination.offset)

    async def get(self, code_list_manifest_id: CodeListManifestId) -> CodeListServiceResult | None:
        """Get code list detail by manifest ID."""
        row = await self._repo.get(code_list_manifest_id)
        if row is None:
            logger.info("get code_list id=%d → not found", int(code_list_manifest_id))
            return None
        users_by_id = await load_users_by_ids(
            self._account_service_repo,
            [row.owner_user_id, row.created_by, row.last_updated_by],
        )
        result = self._to_code_list_result(row, users_by_id=users_by_id)
        logger.info("get code_list id=%d → found", int(code_list_manifest_id))
        return result

    async def create_code_list(
        self,
        *,
        release_id: ReleaseId,
        based_code_list_manifest_id: CodeListManifestId | None = None,
    ) -> CreateCodeListServiceResult:
        """Create a code list in a release allowed for the requester's role."""
        release = await self._release_service.get(release_id)
        if release is None:
            raise LookupError(f"No release exists with ID {int(release_id)}. Please verify the identifier and try again.")
        self._assert_release_is_published(release.state)
        self._assert_can_create_code_lists(release_num=release.release_num)

        if based_code_list_manifest_id is not None:
            based = await self.get(based_code_list_manifest_id)
            if based is None:
                raise LookupError(
                    f"No code list exists with manifest ID {int(based_code_list_manifest_id)}. "
                    "Please verify the identifier and try again."
                )

        code_list_manifest_id = await self._repo.create_code_list(
            release_id=release_id,
            based_code_list_manifest_id=based_code_list_manifest_id,
            requester_user_id=self._requester_user_id,
            requester_is_developer=self._requester_is_developer(),
        )
        await self._repo.append_code_list_log(
            code_list_manifest_id=code_list_manifest_id,
            requester_user_id=self._requester_user_id,
            action="Added",
        )
        return CreateCodeListServiceResult(code_list_manifest_id=int(code_list_manifest_id))

    async def update_code_list(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
        name: str | None | UnsetType = UNSET,
        version_id: str | None | UnsetType = UNSET,
        list_id: str | None | UnsetType = UNSET,
        agency_id_list_value_manifest_id: int | None | UnsetType = UNSET,
        definition: str | None | UnsetType = UNSET,
        definition_source: str | None | UnsetType = UNSET,
        remark: str | None | UnsetType = UNSET,
        namespace_id: int | None | UnsetType = UNSET,
        deprecated: bool | UnsetType = UNSET,
        extensible_indicator: bool | UnsetType = UNSET,
    ) -> UpdateCodeListServiceResult:
        """Update mutable code list fields."""
        code_list = await self.get(code_list_manifest_id)
        if code_list is None:
            raise LookupError(
                f"No code list exists with manifest ID {int(code_list_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_code_list(code_list)

        updates: list[str] = []
        if name is not UNSET and name != code_list.name:
            updates.append("name")
        if version_id is not UNSET and version_id != code_list.version_id:
            updates.append("version_id")
        if list_id is not UNSET and list_id != code_list.list_id:
            updates.append("list_id")
        if agency_id_list_value_manifest_id is not UNSET:
            updates.append("agency_id_list_value_manifest_id")
        if definition is not UNSET and definition != code_list.definition:
            updates.append("definition")
        if definition_source is not UNSET and definition_source != code_list.definition_source:
            updates.append("definition_source")
        if remark is not UNSET and remark != code_list.remark:
            updates.append("remark")
        current_namespace_id = int(code_list.namespace.namespace_id) if code_list.namespace is not None else None
        if namespace_id is not UNSET and namespace_id != current_namespace_id:
            updates.append("namespace_id")
        if deprecated is not UNSET and bool(deprecated) != bool(code_list.is_deprecated):
            updates.append("deprecated")
        if extensible_indicator is not UNSET and bool(extensible_indicator) != bool(code_list.extensible_indicator):
            updates.append("extensible_indicator")

        top_level_changed = await self._repo.update_code_list(
            code_list_manifest_id=code_list_manifest_id,
            name=name if name is not UNSET else None,
            name_set=name is not UNSET,
            version_id=version_id if version_id is not UNSET else None,
            version_id_set=version_id is not UNSET,
            list_id=list_id if list_id is not UNSET else None,
            list_id_set=list_id is not UNSET,
            agency_id_list_value_manifest_id=(
                agency_id_list_value_manifest_id if agency_id_list_value_manifest_id is not UNSET else None
            ),
            agency_id_list_value_manifest_id_set=agency_id_list_value_manifest_id is not UNSET,
            definition=definition if definition is not UNSET else None,
            definition_set=definition is not UNSET,
            definition_source=definition_source if definition_source is not UNSET else None,
            definition_source_set=definition_source is not UNSET,
            remark=remark if remark is not UNSET else None,
            remark_set=remark is not UNSET,
            namespace_id=namespace_id if namespace_id is not UNSET else None,
            namespace_id_set=namespace_id is not UNSET,
            deprecated=deprecated if deprecated is not UNSET else None,
            deprecated_set=deprecated is not UNSET,
            extensible_indicator=extensible_indicator if extensible_indicator is not UNSET else None,
            extensible_indicator_set=extensible_indicator is not UNSET,
            requester_user_id=self._requester_user_id,
        )

        if top_level_changed:
            await self._repo.append_code_list_log(
                code_list_manifest_id=code_list_manifest_id,
                requester_user_id=self._requester_user_id,
                action="Modified",
            )

        return UpdateCodeListServiceResult(
            code_list_manifest_id=int(code_list_manifest_id),
            updates=updates,
        )

    async def create_code_list_value(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
        value: str,
        meaning: str | None = None,
        definition: str | None = None,
        definition_source: str | None = None,
        deprecated: bool = False,
    ) -> CreateCodeListValueServiceResult:
        """Create one code list value under a WIP code list."""
        code_list = await self.get(code_list_manifest_id)
        if code_list is None:
            raise LookupError(
                f"No code list exists with manifest ID {int(code_list_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_code_list(code_list)

        if str(value).strip() == "":
            raise ValueError("New code list values require a non-empty `value`.")

        code_list_value_manifest_id = await self._repo.create_code_list_value(
            code_list_manifest_id=code_list_manifest_id,
            value=str(value),
            meaning=meaning,
            definition=definition,
            definition_source=definition_source,
            deprecated=bool(deprecated),
            requester_user_id=self._requester_user_id,
        )
        await self._repo.append_code_list_log(
            code_list_manifest_id=code_list_manifest_id,
            requester_user_id=self._requester_user_id,
            action="Modified",
        )
        return CreateCodeListValueServiceResult(
            code_list_value_manifest_id=int(code_list_value_manifest_id),
        )

    async def get_code_list_value(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
        code_list_value_manifest_id: CodeListValueManifestId,
    ) -> CodeListValueServiceRecord | None:
        """Get one code list value under the specified code list manifest."""
        code_list = await self.get(code_list_manifest_id)
        if code_list is None:
            logger.info(
                "get code_list_value code_list_id=%d value_id=%d → code list not found",
                int(code_list_manifest_id),
                int(code_list_value_manifest_id),
            )
            return None

        value = next(
            (row for row in code_list.values if int(row.code_list_value_manifest_id) == int(code_list_value_manifest_id)),
            None,
        )
        logger.info(
            "get code_list_value code_list_id=%d value_id=%d → %s",
            int(code_list_manifest_id),
            int(code_list_value_manifest_id),
            "found" if value is not None else "not found",
        )
        return value

    async def get_code_list_value_by_id(
        self,
        *,
        code_list_value_manifest_id: CodeListValueManifestId,
    ) -> CodeListValueServiceRecord | None:
        """Get one code list value by value manifest identifier only."""
        code_list_manifest_id = await self._repo.get_code_list_manifest_id_by_value_manifest_id(code_list_value_manifest_id)
        if code_list_manifest_id is None:
            logger.info("get code_list_value value_id=%d → not found", int(code_list_value_manifest_id))
            return None
        return await self.get_code_list_value(
            code_list_manifest_id=code_list_manifest_id,
            code_list_value_manifest_id=code_list_value_manifest_id,
        )

    async def update_code_list_value(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
        code_list_value_manifest_id: CodeListValueManifestId,
        value: str | None | UnsetType = UNSET,
        meaning: str | None | UnsetType = UNSET,
        definition: str | None | UnsetType = UNSET,
        definition_source: str | None | UnsetType = UNSET,
        deprecated: bool | UnsetType = UNSET,
    ) -> UpdateCodeListValueServiceResult:
        """Update one existing code list value under a WIP code list."""
        code_list = await self.get(code_list_manifest_id)
        if code_list is None:
            raise LookupError(
                f"No code list exists with manifest ID {int(code_list_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_code_list(code_list)

        current_value = next(
            (row for row in code_list.values if int(row.code_list_value_manifest_id) == int(code_list_value_manifest_id)),
            None,
        )
        if current_value is None:
            raise LookupError(
                f"No code list value exists with manifest ID {int(code_list_value_manifest_id)}. "
                "Please verify the identifier and try again."
            )

        updates: list[str] = []
        if value is not UNSET and value != current_value.value:
            updates.append("value")
        if meaning is not UNSET and meaning != current_value.meaning:
            updates.append("meaning")
        if definition is not UNSET and definition != current_value.definition:
            updates.append("definition")
        if definition_source is not UNSET and definition_source != current_value.definition_source:
            updates.append("definition_source")
        if deprecated is not UNSET and bool(deprecated) != bool(current_value.is_deprecated):
            updates.append("deprecated")

        changed = await self._repo.update_code_list_value(
            code_list_value_manifest_id=code_list_value_manifest_id,
            value=value if value is not UNSET else None,
            value_set=value is not UNSET,
            meaning=meaning if meaning is not UNSET else None,
            meaning_set=meaning is not UNSET,
            definition=definition if definition is not UNSET else None,
            definition_set=definition is not UNSET,
            definition_source=definition_source if definition_source is not UNSET else None,
            definition_source_set=definition_source is not UNSET,
            deprecated=deprecated if deprecated is not UNSET else None,
            deprecated_set=deprecated is not UNSET,
            requester_user_id=self._requester_user_id,
        )
        if not changed:
            updates = []
        else:
            await self._repo.append_code_list_log(
                code_list_manifest_id=code_list_manifest_id,
                requester_user_id=self._requester_user_id,
                action="Modified",
            )

        return UpdateCodeListValueServiceResult(
            code_list_value_manifest_id=int(code_list_value_manifest_id),
            updates=updates,
        )

    async def delete_code_list_value(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
        code_list_value_manifest_id: CodeListValueManifestId,
    ) -> None:
        """Delete one existing code list value under a WIP code list."""
        code_list = await self.get(code_list_manifest_id)
        if code_list is None:
            raise LookupError(
                f"No code list exists with manifest ID {int(code_list_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_code_list(code_list)

        current_value = next(
            (row for row in code_list.values if int(row.code_list_value_manifest_id) == int(code_list_value_manifest_id)),
            None,
        )
        if current_value is None:
            raise LookupError(
                f"No code list value exists with manifest ID {int(code_list_value_manifest_id)}. "
                "Please verify the identifier and try again."
            )

        deleted = await self._repo.delete_code_list_value(
            code_list_value_manifest_id=code_list_value_manifest_id,
        )
        if not deleted:
            raise LookupError(
                f"No code list value exists with manifest ID {int(code_list_value_manifest_id)}. "
                "Please verify the identifier and try again."
            )
        await self._repo.append_code_list_log(
            code_list_manifest_id=code_list_manifest_id,
            requester_user_id=self._requester_user_id,
            action="Modified",
        )

    async def update_code_list_value_by_id(
        self,
        *,
        code_list_value_manifest_id: CodeListValueManifestId,
        value: str | None | UnsetType = UNSET,
        meaning: str | None | UnsetType = UNSET,
        definition: str | None | UnsetType = UNSET,
        definition_source: str | None | UnsetType = UNSET,
        deprecated: bool | UnsetType = UNSET,
    ) -> UpdateCodeListValueServiceResult:
        """Update one existing code list value by value identifier only."""
        code_list_manifest_id = await self._repo.get_code_list_manifest_id_by_value_manifest_id(code_list_value_manifest_id)
        if code_list_manifest_id is None:
            raise LookupError(
                f"No code list value exists with manifest ID {int(code_list_value_manifest_id)}. "
                "Please verify the identifier and try again."
            )
        return await self.update_code_list_value(
            code_list_manifest_id=code_list_manifest_id,
            code_list_value_manifest_id=code_list_value_manifest_id,
            value=value,
            meaning=meaning,
            definition=definition,
            definition_source=definition_source,
            deprecated=deprecated,
        )

    async def delete_code_list_value_by_id(
        self,
        *,
        code_list_value_manifest_id: CodeListValueManifestId,
    ) -> None:
        """Delete one existing code list value by value identifier only."""
        code_list_manifest_id = await self._repo.get_code_list_manifest_id_by_value_manifest_id(code_list_value_manifest_id)
        if code_list_manifest_id is None:
            raise LookupError(
                f"No code list value exists with manifest ID {int(code_list_value_manifest_id)}. "
                "Please verify the identifier and try again."
            )
        await self.delete_code_list_value(
            code_list_manifest_id=code_list_manifest_id,
            code_list_value_manifest_id=code_list_value_manifest_id,
        )

    async def transfer_code_list_ownership(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
        target_user_id: int,
    ) -> TransferCodeListOwnershipServiceResult:
        """Transfer code list ownership to another user while the code list is in WIP."""
        code_list = await self.get(code_list_manifest_id)
        if code_list is None:
            raise LookupError(
                f"No code list exists with manifest ID {int(code_list_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_transfer_code_list(code_list)

        if int(code_list.owner.user_id) == int(target_user_id):
            return TransferCodeListOwnershipServiceResult(
                code_list_manifest_id=int(code_list_manifest_id),
                updates=[],
            )

        transferred = await self._repo.transfer_code_list_ownership(
            code_list_manifest_id=code_list_manifest_id,
            requester_user_id=self._requester_user_id,
            target_user_id=target_user_id,
        )
        if not transferred:
            raise LookupError(
                f"No code list exists with manifest ID {int(code_list_manifest_id)}. Please verify the identifier and try again."
            )
        await self._repo.append_code_list_log(
            code_list_manifest_id=code_list_manifest_id,
            requester_user_id=self._requester_user_id,
            action="Modified",
        )
        return TransferCodeListOwnershipServiceResult(
            code_list_manifest_id=int(code_list_manifest_id),
            updates=["owner_user_id"],
        )

    async def change_code_list_state(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
        state: CodeListState,
    ) -> None:
        """Change the lifecycle state of a code list."""
        code_list = await self.get(code_list_manifest_id)
        if code_list is None:
            raise LookupError(
                f"No code list exists with manifest ID {int(code_list_manifest_id)}. Please verify the identifier and try again."
            )

        current_state = str(code_list.state or "")
        self._assert_code_list_state_transition_allowed(code_list, state=state)

        restore_owner = current_state == "Deleted" and state == "WIP"
        if restore_owner:
            self._assert_can_restore_deleted_code_list(code_list)
        elif not self._is_admin() and int(code_list.owner.user_id) != self._requester_user_id:
            raise PermissionError("It only allows to modify the core component by the owner.")

        changed = await self._repo.change_code_list_state(
            code_list_manifest_id=code_list_manifest_id,
            state=state,
            restore_owner=restore_owner,
            implicit_move=False,
            requester_user_id=self._requester_user_id,
        )
        if not changed:
            raise LookupError(
                f"No code list exists with manifest ID {int(code_list_manifest_id)}. Please verify the identifier and try again."
            )
        await self._repo.append_code_list_log(
            code_list_manifest_id=code_list_manifest_id,
            requester_user_id=self._requester_user_id,
            action="Deleted" if state == "Deleted" else ("Restored" if restore_owner else "Modified"),
        )

    async def revise_code_list(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
    ) -> None:
        """Create a revised code list working copy."""
        code_list = await self.get(code_list_manifest_id)
        if code_list is None:
            raise LookupError(
                f"No code list exists with manifest ID {int(code_list_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_revise_code_list(code_list)

        revised = await self._repo.revise_code_list(
            code_list_manifest_id=code_list_manifest_id,
            requester_user_id=self._requester_user_id,
        )
        if not revised:
            raise LookupError(
                f"No code list exists with manifest ID {int(code_list_manifest_id)}. Please verify the identifier and try again."
            )
        await self._repo.append_code_list_log(
            code_list_manifest_id=code_list_manifest_id,
            requester_user_id=self._requester_user_id,
            action="Revised",
        )

    async def cancel_code_list(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
    ) -> None:
        """Cancel the current code list revision and restore the previous stable revision."""
        code_list = await self.get(code_list_manifest_id)
        if code_list is None:
            raise LookupError(
                f"No code list exists with manifest ID {int(code_list_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_cancel_code_list(code_list)

        cancelled = await self._repo.cancel_code_list(code_list_manifest_id=code_list_manifest_id)
        if not cancelled:
            raise LookupError(
                f"No code list exists with manifest ID {int(code_list_manifest_id)}. Please verify the identifier and try again."
            )
        await self._repo.revert_code_list_log_to_stable_state(code_list_manifest_id=code_list_manifest_id)

    async def discard_code_list(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
    ) -> None:
        """Discard a Deleted code list and its direct records permanently."""
        code_list = await self.get(code_list_manifest_id)
        if code_list is None:
            raise LookupError(
                f"No code list exists with manifest ID {int(code_list_manifest_id)}. Please verify the identifier and try again."
            )
        if code_list.state != "Deleted":
            raise ValueError(
                f"The code list '{code_list.name}' cannot be discarded because it is in the '{code_list.state}' state. "
                "Only code lists in the 'Deleted' state can be discarded."
            )

        discarded = await self._repo.discard_code_list(code_list_manifest_id=code_list_manifest_id)
        if not discarded:
            raise LookupError(
                f"No code list exists with manifest ID {int(code_list_manifest_id)}. Please verify the identifier and try again."
            )

    def _to_code_list_result(self, row: Any, *, users_by_id: dict[int, AppUserRow]) -> CodeListServiceResult:
        """Map repository row to Code List DTO."""
        return CodeListServiceResult(
            code_list_manifest_id=row.code_list_manifest_id,
            code_list_id=row.code_list_id,
            guid=row.guid,
            enum_type_guid=row.enum_type_guid,
            name=str(row.name or ""),
            list_id=row.list_id,
            version_id=row.version_id,
            definition=row.definition,
            remark=row.remark,
            definition_source=row.definition_source,
            extensible_indicator=row.extensible_indicator,
            is_deprecated=row.is_deprecated,
            state=row.state,
            values=row.values,
            namespace=row.namespace,
            library=row.library,
            release=row.release,
            log=row.log,
            owner=to_user_summary(int(row.owner_user_id), users_by_id=users_by_id),
            created=WhoAndWhen(
                who=to_user_summary(int(row.created_by), users_by_id=users_by_id),
                when=row.creation_timestamp,
            ),
            last_updated=WhoAndWhen(
                who=to_user_summary(int(row.last_updated_by), users_by_id=users_by_id),
                when=row.last_update_timestamp,
            ),
        )

    @property
    def _requester_user_id(self) -> int:
        return int(self._requester.user.user_id)

    def _is_admin(self) -> bool:
        return "Admin" in self._requester.user.roles

    def _requester_is_developer(self) -> bool:
        return "Developer" in self._requester.user.roles

    def _assert_can_create_code_lists(self, *, release_num: str) -> None:
        if self._requester_is_developer():
            if str(release_num) != "Working":
                raise PermissionError("It only allows to create code lists in the 'Working' branch for developers.")
            return
        if str(release_num) == "Working":
            raise PermissionError("It only allows to create code lists in non-'Working' branches for end-users.")

    @staticmethod
    def _assert_release_is_published(release_state: str) -> None:
        if str(release_state) != "Published":
            raise ValueError(f"'{release_state}' release cannot be modified.")

    def _assert_owner_or_admin(self, code_list: CodeListServiceResult) -> None:
        if not self._is_admin() and int(code_list.owner.user_id) != self._requester_user_id:
            raise PermissionError("It only allows to modify the core component by the owner.")

    def _assert_code_list_is_wip(self, code_list: CodeListServiceResult) -> None:
        if code_list.state != "WIP":
            raise ValueError(
                f"The code list '{code_list.name}' cannot be updated because it is in the '{code_list.state}' state. "
                "Only code lists in the 'WIP' state can be updated."
            )

    def _assert_can_update_code_list(self, code_list: CodeListServiceResult) -> None:
        self._assert_code_list_is_wip(code_list)
        self._assert_owner_or_admin(code_list)

    def _assert_can_transfer_code_list(self, code_list: CodeListServiceResult) -> None:
        if code_list.state != "WIP":
            raise ValueError(
                f"The code list '{code_list.name}' cannot be transferred because it is in the '{code_list.state}' state. "
                "Only code lists in the 'WIP' state can be transferred."
            )
        self._assert_owner_or_admin(code_list)

    def _assert_code_list_state_transition_allowed(
        self,
        code_list: CodeListServiceResult,
        *,
        state: CodeListState,
    ) -> None:
        current_state = str(code_list.state or "")
        release_num = str(code_list.release.release_num or "")
        allowed_transitions = self._state_transitions_for_release(release_num=release_num)
        if current_state not in allowed_transitions:
            branch_label = "'Working'" if release_num == "Working" else "non-'Working'"
            raise ValueError(
                f"The code list is in '{current_state}' state, which cannot be changed by this service for {branch_label} releases."
            )
        if state not in allowed_transitions[current_state]:
            raise ValueError(f"The core component in '{current_state}' state cannot move to '{state}' state.")

    def _assert_can_restore_deleted_code_list(self, code_list: CodeListServiceResult) -> None:
        roles = set(self._requester.user.roles)
        release_num = str(code_list.release.release_num or "")
        if release_num == "Working":
            if "Developer" not in roles:
                raise PermissionError("It only allows to restore the component in 'Working' branch for developers.")
            return
        if "End-User" not in roles:
            raise PermissionError("It only allows to restore the component in non-'Working' branch for end-users.")

    def _assert_can_access_revision_branch(self, code_list: CodeListServiceResult) -> None:
        release_num = str(code_list.release.release_num or "")
        if self._requester_is_developer():
            if release_num != "Working":
                raise ValueError("It only allows to revise the component in 'Working' branch for developers.")
            return
        if release_num == "Working":
            raise ValueError("It only allows to revise the component in non-'Working' branch for end-users.")

    def _assert_same_role_family_as_owner(self, code_list: CodeListServiceResult) -> None:
        owner_roles = set(code_list.owner.roles)
        owner_is_developer = "Developer" in owner_roles
        if self._requester_is_developer() != owner_is_developer:
            raise ValueError("It only allows to revise the component for users in the same roles.")

    def _assert_can_revise_code_list(self, code_list: CodeListServiceResult) -> None:
        if self._requester_is_developer():
            if code_list.state != "Published":
                raise ValueError("Only the core component in 'Published' state can be revised.")
        else:
            if code_list.state != "Production":
                raise ValueError("Only the core component in 'Production' state can be revised.")
        self._assert_can_access_revision_branch(code_list)
        self._assert_same_role_family_as_owner(code_list)

    def _assert_can_cancel_code_list(self, code_list: CodeListServiceResult) -> None:
        if code_list.state != "WIP":
            raise ValueError(
                f"The code list '{code_list.name}' cannot be cancelled because it is in the '{code_list.state}' state. "
                "Only code lists in the 'WIP' state can be cancelled."
            )
        self._assert_can_access_revision_branch(code_list)
        self._assert_same_role_family_as_owner(code_list)

    @staticmethod
    def _state_transitions_for_release(*, release_num: str):
        if str(release_num) == "Working":
            return WORKING_BRANCH_CORE_COMPONENT_STATE_TRANSITIONS
        return NON_WORKING_BRANCH_CORE_COMPONENT_STATE_TRANSITIONS
