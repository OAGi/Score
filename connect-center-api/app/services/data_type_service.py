"""Service layer for Data Type operations in connectCenter."""


from __future__ import annotations

import logging
from typing import Any, Literal

from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.repositories.contracts.data_type import DataTypeRepositoryContract
from app.repositories.models.app_user import AppUserRow
from app.security import AuthenticatedUser
from app.services import load_users_by_ids, to_user_summary
from app.services.models import WhoAndWhen
from app.services.models.data_type import (
    CancelDataTypeServiceResult,
    CreateDataTypeSupplementaryComponentServiceResult,
    CreateDataTypeServiceResult,
    DataTypePrimitiveServiceRecord,
    DataTypeServiceResult,
    DataTypeSupplementaryComponentServiceRecord,
    DataTypeValueConstraintServiceRecord,
    DiscardDataTypeServiceResult,
    ReviseDataTypeServiceResult,
    TransferDataTypeOwnershipServiceResult,
    UpdateDataTypeServiceResult,
    UpdateDataTypeSupplementaryComponentServiceResult,
)
from app.services.models.mapper import to_dataclass
from app.services.models.tag import TagSummaryServiceRecord
from app.services.models.release import ReleaseServiceResult
from app.services.release_service import ReleaseService
from app.services.utils.date import DateRange
from app.services.utils.owner import parse_owner_filter
from app.services.utils.pagination import PaginationParams, PaginationResponse
from app.types.unset import UNSET, UnsetType
from app.types.identifiers import DataTypeManifestId, DataTypeSupplementaryComponentManifestId, ReleaseId
from app.utils.core_component_constants import (
    NON_WORKING_BRANCH_CORE_COMPONENT_STATE_TRANSITIONS,
    WORKING_BRANCH_CORE_COMPONENT_STATE_TRANSITIONS,
)

logger = logging.getLogger("connectcenter.service.data_type")


DataTypeState = Literal[
    "Deleted",
    "WIP",
    "Draft",
    "QA",
    "Candidate",
    "Production",
    "ReleaseDraft",
    "Published",
]

DataTypeSupplementaryComponentCardinality = Literal["Prohibited", "Optional", "Required"]

CDT_PRIMITIVE_NAMES = {
    "Binary",
    "Boolean",
    "Decimal",
    "Double",
    "Float",
    "Integer",
    "NormalizedString",
    "String",
    "TimeDuration",
    "TimePoint",
    "Token",
}


class DataTypeService:
    """Service for Data Type read and command operations."""

    _ORDER_BY_ALLOWED: set[str] = {
        "den",
        "data_type_term",
        "qualifier",
        "representation_term",
        "six_digit_id",
        "definition",
        "creation_timestamp",
        "last_update_timestamp",
    }

    def __init__(
        self,
        data_type_repository: DataTypeRepositoryContract,
        release_service: ReleaseService,
        account_service_repo: AppUserRepositoryContract,
        requester: AuthenticatedUser,
    ):
        """Initialize the service with repository and dependency services.

        Args:
            data_type_repository: Data-type repository dependency.
            release_service: Release service dependency.
            account_service_repo: Account repository used to resolve user summaries.
            requester: Requesting user context.
        """
        self._repo = data_type_repository
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
        den: str | None = None,
        representation_term: str | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
        owner: str | None = None,
    ) -> PaginationResponse[DataTypeServiceResult]:
        """List Data Types for a release scope with filters and pagination.

        Args:
            release_id: Release identifier used to scope the query.
            limit: Maximum number of items to return.
            offset: Zero-based index of the first item in the page.
            order_by: Sort expression for the result set.

        Returns:
            Result of the operation.
        """
        logger.info("list data_types release_id=%d limit=%d offset=%d", int(release_id), limit, offset)
        pagination = PaginationParams.from_query(
            limit=limit,
            offset=offset,
            order_by=order_by,
            allowed_sort_columns=self._ORDER_BY_ALLOWED,
        )
        dependent_release_ids = await self._release_service.get_dependent_releases(release_id)
        included_owner_login_ids, excluded_owner_login_ids = parse_owner_filter(owner)
        total, rows = await self._repo.list(
            release_id=release_id,
            dependent_release_ids=dependent_release_ids,
            limit=pagination.limit,
            offset=pagination.offset,
            sorts=[(s.column, s.direction.upper()) for s in pagination.sorts],
            den=den,
            representation_term=representation_term,
            creation_timestamp_before=created_on.before if created_on else None,
            creation_timestamp_after=created_on.after if created_on else None,
            last_update_timestamp_before=last_updated_on.before if last_updated_on else None,
            last_update_timestamp_after=last_updated_on.after if last_updated_on else None,
            included_owner_login_ids=included_owner_login_ids,
            excluded_owner_login_ids=excluded_owner_login_ids,
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
        items = [self._to_data_type_result(row, users_by_id=users_by_id) for row in rows]
        logger.info("list data_types release_id=%d → %d/%d", int(release_id), len(items), total)
        return PaginationResponse(items=items, total=total, limit=pagination.limit, offset=pagination.offset)

    async def get(self, dt_manifest_id: DataTypeManifestId) -> DataTypeServiceResult | None:
        """Get Data Type detail by manifest ID.

        Args:
            dt_manifest_id: Data type manifest identifier.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get(dt_manifest_id)
        if row is None:
            logger.info("get data_type id=%d → not found", int(dt_manifest_id))
            return None
        users_by_id = await load_users_by_ids(self._account_service_repo, [row.owner_user_id, row.created_by, row.last_updated_by])
        result = self._to_data_type_result(row, users_by_id=users_by_id)
        logger.info("get data_type id=%d → found", int(dt_manifest_id))
        return result

    async def create_dt(
        self,
        *,
        release_id: ReleaseId,
        based_dt_manifest_id: DataTypeManifestId,
        tag_id: list[int] | None = None,
        qualifier: str | None | UnsetType = UNSET,
        six_digit_id: str | None | UnsetType = UNSET,
        deprecated: bool | UnsetType = UNSET,
        namespace_id: int | None | UnsetType = UNSET,
        content_component_definition: str | None | UnsetType = UNSET,
        definition: str | None | UnsetType = UNSET,
        definition_source: str | None | UnsetType = UNSET,
        xbt_manifest_id: int | None | UnsetType = UNSET,
        code_list_manifest_id: int | None | UnsetType = UNSET,
        agency_id_list_manifest_id: int | None | UnsetType = UNSET,
        add_primitives: list[DataTypePrimitiveServiceRecord] | UnsetType = UNSET,
        remove_primitives: list[DataTypePrimitiveServiceRecord] | UnsetType = UNSET,
    ) -> CreateDataTypeServiceResult:
        """Create a DT in a release allowed for the requester's role."""
        release = await self._release_service.get(release_id)
        if release is None:
            raise LookupError(f"No release exists with ID {int(release_id)}. Please verify the identifier and try again.")
        self._assert_release_is_published(release)
        self._assert_can_create_data_types(release_num=release.release_num)

        based_dt = await self.get(based_dt_manifest_id)
        if based_dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(based_dt_manifest_id)}. Please verify the identifier and try again."
            )
        await self._assert_base_dt_allowed_for_release(
            target_release_id=release.release_id,
            target_library_id=release.library.library_id,
            based_dt=based_dt,
        )

        normalized_tag_ids = self._normalize_tag_ids(tag_id)
        dt_manifest_id = await self._repo.create_dt(
            release_id=release_id,
            based_dt_manifest_id=based_dt_manifest_id,
            requester_user_id=self._requester_user_id,
            tag_id=normalized_tag_ids,
        )
        if any(
            value is not UNSET
            for value in (
                qualifier,
                six_digit_id,
                deprecated,
                namespace_id,
                content_component_definition,
                definition,
                definition_source,
                xbt_manifest_id,
                code_list_manifest_id,
                agency_id_list_manifest_id,
                add_primitives,
                remove_primitives,
            )
        ):
            await self.update_dt(
                dt_manifest_id=dt_manifest_id,
                qualifier=qualifier,
                six_digit_id=six_digit_id,
                deprecated=deprecated,
                namespace_id=namespace_id,
                content_component_definition=content_component_definition,
                definition=definition,
                definition_source=definition_source,
                xbt_manifest_id=xbt_manifest_id,
                code_list_manifest_id=code_list_manifest_id,
                agency_id_list_manifest_id=agency_id_list_manifest_id,
                add_primitives=add_primitives,
                remove_primitives=remove_primitives,
            )
        return CreateDataTypeServiceResult(dt_manifest_id=int(dt_manifest_id))

    async def update_dt(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        based_dt_manifest_id: DataTypeManifestId | None | UnsetType = UNSET,
        qualifier: str | None | UnsetType = UNSET,
        six_digit_id: str | None | UnsetType = UNSET,
        deprecated: bool | UnsetType = UNSET,
        namespace_id: int | None | UnsetType = UNSET,
        content_component_definition: str | None | UnsetType = UNSET,
        definition: str | None | UnsetType = UNSET,
        definition_source: str | None | UnsetType = UNSET,
        xbt_manifest_id: int | None | UnsetType = UNSET,
        code_list_manifest_id: int | None | UnsetType = UNSET,
        agency_id_list_manifest_id: int | None | UnsetType = UNSET,
        add_primitives: list[DataTypePrimitiveServiceRecord] | UnsetType = UNSET,
        remove_primitives: list[DataTypePrimitiveServiceRecord] | UnsetType = UNSET,
    ) -> UpdateDataTypeServiceResult:
        """Update mutable DT fields when the requester owns a WIP DT."""
        dt = await self.get(dt_manifest_id)
        if dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(dt_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_dt(dt)

        normalized_qualifier = qualifier
        if qualifier is not UNSET and isinstance(qualifier, str):
            normalized_qualifier = qualifier.strip() or None

        normalized_six_digit_id = six_digit_id
        if six_digit_id is not UNSET and isinstance(six_digit_id, str):
            normalized_six_digit_id = six_digit_id.strip() or None

        normalized_content_component_definition = content_component_definition
        if content_component_definition is not UNSET and isinstance(content_component_definition, str):
            normalized_content_component_definition = content_component_definition.strip() or None

        normalized_definition = definition
        if definition is not UNSET and isinstance(definition, str):
            normalized_definition = definition.strip() or None

        normalized_definition_source = definition_source
        if definition_source is not UNSET and isinstance(definition_source, str):
            normalized_definition_source = definition_source.strip() or None

        current_namespace_id = int(dt.namespace.namespace_id) if dt.namespace is not None else None
        current_based_dt_manifest_id = int(dt.base_dt.dt_manifest_id) if dt.base_dt is not None else None
        based_dt_changed = False
        replacement_base_dt: DataTypeServiceResult | None = None
        if based_dt_manifest_id is not UNSET:
            requested_based_dt_manifest_id = None if based_dt_manifest_id is None else int(based_dt_manifest_id)
            based_dt_changed = requested_based_dt_manifest_id != current_based_dt_manifest_id
            if based_dt_changed and requested_based_dt_manifest_id is not None:
                replacement_base_dt = await self._resolve_replacement_base_dt(
                    target_dt=dt,
                    based_dt_manifest_id=DataTypeManifestId(requested_based_dt_manifest_id),
                )
                blocking_derived_dts = await self._find_non_wip_inherited_dts(dt_manifest_id)
                if blocking_derived_dts:
                    blocked = ", ".join(f"'{item.den}' ({item.state})" for item in blocking_derived_dts)
                    subject = "the following derived DT is" if len(blocking_derived_dts) == 1 else "the following derived DTs are"
                    raise ValueError(
                        f"The base DT cannot be changed for DT '{dt.den}' because {subject} "
                        f"not in the 'WIP' state: {blocked}. Base DT changes that resync inherited "
                        "primitives and supplementary components require the target DT and all derived DTs "
                        "to be in the 'WIP' state."
                    )

        updates: list[str] = []
        if based_dt_changed:
            updates.append("based_dt_manifest_id")
        if normalized_qualifier is not UNSET and normalized_qualifier != dt.qualifier:
            updates.append("qualifier")
        if normalized_six_digit_id is not UNSET and normalized_six_digit_id != dt.six_digit_id:
            updates.append("six_digit_id")
        if deprecated is not UNSET and bool(deprecated) != bool(dt.is_deprecated):
            updates.append("deprecated")
        if namespace_id is not UNSET and (None if namespace_id is None else int(namespace_id)) != current_namespace_id:
            updates.append("namespace_id")
        if (
            normalized_content_component_definition is not UNSET
            and normalized_content_component_definition != dt.content_component_definition
        ):
            updates.append("content_component_definition")
        if normalized_definition is not UNSET and normalized_definition != dt.definition:
            updates.append("definition")
        if normalized_definition_source is not UNSET and normalized_definition_source != dt.definition_source:
            updates.append("definition_source")
        primitive_selection_requested = any(
            value is not UNSET for value in (xbt_manifest_id, code_list_manifest_id, agency_id_list_manifest_id)
        )
        primitive_mutation_requested = add_primitives is not UNSET or remove_primitives is not UNSET
        primitive_change_requested = primitive_selection_requested or primitive_mutation_requested
        normalized_xbt_manifest_id: int | None = None
        normalized_code_list_manifest_id: int | None = None
        normalized_agency_id_list_manifest_id: int | None = None
        if primitive_selection_requested:
            (
                normalized_xbt_manifest_id,
                normalized_code_list_manifest_id,
                normalized_agency_id_list_manifest_id,
            ) = self._normalize_default_primitive_target(
                xbt_manifest_id=None if xbt_manifest_id is UNSET else xbt_manifest_id,
                code_list_manifest_id=None if code_list_manifest_id is UNSET else code_list_manifest_id,
                agency_id_list_manifest_id=None if agency_id_list_manifest_id is UNSET else agency_id_list_manifest_id,
            )
        if not updates and not primitive_change_requested:
            return UpdateDataTypeServiceResult(dt_manifest_id=int(dt_manifest_id), updates=[])

        scalar_updates = [
            update
            for update in updates
            if update not in {"based_dt_manifest_id", "primitives", "supplementary_components", "default_primitive"}
        ]

        if based_dt_changed:
            await self._repo.update_dt_base(
                dt_manifest_id=dt_manifest_id,
                based_dt_manifest_id=(
                    None if based_dt_manifest_id is None else DataTypeManifestId(int(based_dt_manifest_id))
                ),
                requester_user_id=self._requester_user_id,
            )
            if replacement_base_dt is not None:
                primitives_replaced = await self._replace_dt_primitives_recursive(
                    dt_manifest_id=dt_manifest_id,
                    primitives=replacement_base_dt.primitives,
                    skip_ownership_check=False,
                )
                if primitives_replaced:
                    updates.append("primitives")
                inherited_dt_sc_manifest_ids = await self._list_inherited_dt_sc_manifest_ids(dt_manifest_id)
                for inherited_dt_sc_manifest_id in inherited_dt_sc_manifest_ids:
                    await self._delete_dt_sc_branch_recursive(inherited_dt_sc_manifest_id)
                for supplementary_component in replacement_base_dt.supplementary_components:
                    await self._create_dt_sc_from_base_recursive(
                        owner_dt_manifest_id=dt_manifest_id,
                        based_dt_sc_manifest_id=supplementary_component.dt_sc_manifest_id,
                    )
                if inherited_dt_sc_manifest_ids or replacement_base_dt.supplementary_components:
                    updates.append("supplementary_components")

        if scalar_updates:
            await self._repo.update_dt(
                dt_manifest_id=dt_manifest_id,
                qualifier=None if normalized_qualifier is UNSET else normalized_qualifier,
                qualifier_set=normalized_qualifier is not UNSET,
                six_digit_id=None if normalized_six_digit_id is UNSET else normalized_six_digit_id,
                six_digit_id_set=normalized_six_digit_id is not UNSET,
                deprecated=None if deprecated is UNSET else bool(deprecated),
                deprecated_set=deprecated is not UNSET,
                namespace_id=None if namespace_id in {UNSET, None} else int(namespace_id),
                namespace_id_set=namespace_id is not UNSET,
                content_component_definition=(
                    None if normalized_content_component_definition is UNSET else normalized_content_component_definition
                ),
                content_component_definition_set=normalized_content_component_definition is not UNSET,
                definition=None if normalized_definition is UNSET else normalized_definition,
                definition_set=normalized_definition is not UNSET,
                definition_source=None if normalized_definition_source is UNSET else normalized_definition_source,
                definition_source_set=normalized_definition_source is not UNSET,
                requester_user_id=self._requester_user_id,
            )
        primitive_updated = False
        if primitive_change_requested:
            if primitive_mutation_requested:
                desired_primitives, membership_changed, default_changed = self._resolve_primitive_update(
                    current_primitives=dt.primitives,
                    add_primitives=[] if add_primitives is UNSET else add_primitives,
                    remove_primitives=[] if remove_primitives is UNSET else remove_primitives,
                    default_primitive=(
                        None
                        if not primitive_selection_requested
                        else DataTypePrimitiveServiceRecord(
                            xbt_manifest_id=normalized_xbt_manifest_id,
                            code_list_manifest_id=normalized_code_list_manifest_id,
                            agency_id_list_manifest_id=normalized_agency_id_list_manifest_id,
                            is_default=True,
                        )
                    ),
                )
                primitive_updated = await self._replace_dt_primitives_recursive(
                    dt_manifest_id=dt_manifest_id,
                    primitives=desired_primitives,
                    skip_ownership_check=False,
                )
                if primitive_updated and membership_changed:
                    updates.append("primitives")
                if primitive_updated and default_changed:
                    updates.append("default_primitive")
            else:
                primitive_updated = await self._change_dt_default_primitive_recursive(
                    dt_manifest_id=dt_manifest_id,
                    xbt_manifest_id=normalized_xbt_manifest_id,
                    code_list_manifest_id=normalized_code_list_manifest_id,
                    agency_id_list_manifest_id=normalized_agency_id_list_manifest_id,
                    skip_ownership_check=False,
                )
                if primitive_updated:
                    updates.append("default_primitive")
        return UpdateDataTypeServiceResult(dt_manifest_id=int(dt_manifest_id), updates=sorted(set(updates)))

    async def transfer_dt_ownership(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        target_user_id: int,
    ) -> TransferDataTypeOwnershipServiceResult:
        """Transfer DT ownership to another user while the DT is in `WIP`."""
        logger.info("transfer dt ownership id=%d → user_id=%d", int(dt_manifest_id), int(target_user_id))
        dt = await self.get(dt_manifest_id)
        if dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(dt_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_transfer_dt(dt)
        if int(target_user_id) == int(dt.owner.user_id):
            logger.info("transfer dt ownership id=%d → no-op (same owner)", int(dt_manifest_id))
            return TransferDataTypeOwnershipServiceResult(dt_manifest_id=int(dt_manifest_id), updates=[])
        transferred = await self._repo.transfer_dt_ownership(
            dt_manifest_id=dt_manifest_id,
            requester_user_id=self._requester_user_id,
            target_user_id=int(target_user_id),
        )
        if not transferred:
            raise LookupError(
                f"No DT exists with manifest ID {int(dt_manifest_id)}. Please verify the identifier and try again."
            )
        logger.info("transfer dt ownership id=%d → transferred to user_id=%d", int(dt_manifest_id), int(target_user_id))
        return TransferDataTypeOwnershipServiceResult(dt_manifest_id=int(dt_manifest_id), updates=["owner_user_id"])

    async def add_dt_tags(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        tag_id: list[int],
    ) -> UpdateDataTypeServiceResult:
        """Attach tags to a WIP DT owned by the requester."""
        dt = await self.get(dt_manifest_id)
        if dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(dt_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_dt(dt)

        normalized_tag_ids = self._normalize_tag_ids(tag_id) or []
        current_tag_ids = {int(tag.tag_id) for tag in dt.tags}
        tag_ids_to_add = [single_tag_id for single_tag_id in normalized_tag_ids if single_tag_id not in current_tag_ids]
        if not tag_ids_to_add:
            return UpdateDataTypeServiceResult(dt_manifest_id=int(dt_manifest_id), updates=[])

        await self._repo.add_dt_tags(
            dt_manifest_id=dt_manifest_id,
            tag_id=tag_ids_to_add,
            requester_user_id=self._requester_user_id,
        )
        return UpdateDataTypeServiceResult(dt_manifest_id=int(dt_manifest_id), updates=["tags"])

    async def create_dt_sc(
        self,
        *,
        owner_dt_manifest_id: DataTypeManifestId,
        property_term: str,
        representation_term: str,
        cardinality: DataTypeSupplementaryComponentCardinality | UnsetType = UNSET,
        deprecated: bool | UnsetType = UNSET,
        default_value: str | None | UnsetType = UNSET,
        fixed_value: str | None | UnsetType = UNSET,
        definition: str | None | UnsetType = UNSET,
        definition_source: str | None | UnsetType = UNSET,
        xbt_manifest_id: int | None | UnsetType = UNSET,
        code_list_manifest_id: int | None | UnsetType = UNSET,
        agency_id_list_manifest_id: int | None | UnsetType = UNSET,
        add_primitives: list[DataTypePrimitiveServiceRecord] | UnsetType = UNSET,
        remove_primitives: list[DataTypePrimitiveServiceRecord] | UnsetType = UNSET,
    ) -> CreateDataTypeSupplementaryComponentServiceResult:
        """Create a DT_SC on a WIP DT, optionally apply mutable fields, and propagate it to inherited DTs."""
        dt = await self.get(owner_dt_manifest_id)
        if dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(owner_dt_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_dt(dt)
        blocking_derived_dts = await self._find_non_wip_inherited_dts(owner_dt_manifest_id)
        if blocking_derived_dts:
            blocked = ", ".join(f"'{item.den}' ({item.state})" for item in blocking_derived_dts)
            subject = "the following derived DT is" if len(blocking_derived_dts) == 1 else "the following derived DTs are"
            raise ValueError(
                f"The supplementary component cannot be added to DT '{dt.den}' because {subject} "
                f"not in the 'WIP' state: {blocked}. Supplementary components can only be added when "
                "the target DT and all derived DTs are in the 'WIP' state."
            )
        if not property_term.strip():
            raise ValueError("property_term must not be empty.")
        if not representation_term.strip():
            raise ValueError("representation_term must not be empty.")

        dt_sc_manifest_id = await self._repo.create_dt_sc(
            owner_dt_manifest_id=owner_dt_manifest_id,
            requester_user_id=self._requester_user_id,
        )
        for inherited_dt_manifest_id in await self._repo.list_direct_inherited_dt_manifest_ids(owner_dt_manifest_id):
            await self._create_dt_sc_from_base_recursive(
                owner_dt_manifest_id=inherited_dt_manifest_id,
                based_dt_sc_manifest_id=dt_sc_manifest_id,
            )
        if any(
            value is not UNSET
            for value in (
                cardinality,
                deprecated,
                default_value,
                fixed_value,
                definition,
                definition_source,
                xbt_manifest_id,
                code_list_manifest_id,
                agency_id_list_manifest_id,
                add_primitives,
                remove_primitives,
            )
        ):
            await self.update_dt_sc(
                dt_sc_manifest_id=dt_sc_manifest_id,
                property_term=property_term,
                representation_term=representation_term,
                cardinality=cardinality,
                deprecated=deprecated,
                default_value=default_value,
                fixed_value=fixed_value,
                definition=definition,
                definition_source=definition_source,
                xbt_manifest_id=xbt_manifest_id,
                code_list_manifest_id=code_list_manifest_id,
                agency_id_list_manifest_id=agency_id_list_manifest_id,
                add_primitives=add_primitives,
                remove_primitives=remove_primitives,
            )
        return CreateDataTypeSupplementaryComponentServiceResult(dt_sc_manifest_id=int(dt_sc_manifest_id))

    async def update_dt_sc(
        self,
        *,
        dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
        property_term: str | None | UnsetType = UNSET,
        representation_term: str | None | UnsetType = UNSET,
        cardinality: DataTypeSupplementaryComponentCardinality | UnsetType = UNSET,
        deprecated: bool | UnsetType = UNSET,
        default_value: str | None | UnsetType = UNSET,
        fixed_value: str | None | UnsetType = UNSET,
        definition: str | None | UnsetType = UNSET,
        definition_source: str | None | UnsetType = UNSET,
        xbt_manifest_id: int | None | UnsetType = UNSET,
        code_list_manifest_id: int | None | UnsetType = UNSET,
        agency_id_list_manifest_id: int | None | UnsetType = UNSET,
        add_primitives: list[DataTypePrimitiveServiceRecord] | UnsetType = UNSET,
        remove_primitives: list[DataTypePrimitiveServiceRecord] | UnsetType = UNSET,
        skip_ownership_check: bool = False,
    ) -> UpdateDataTypeSupplementaryComponentServiceResult:
        """Update mutable DT_SC fields and propagate the same change to inherited DT_SCs."""
        dt, dt_sc = await self._get_owner_dt_and_sc(dt_sc_manifest_id)
        self._assert_can_update_dt_sc(dt, dt_sc, skip_ownership_check=skip_ownership_check)

        normalized_property_term = property_term
        if property_term is not UNSET and isinstance(property_term, str):
            normalized_property_term = property_term.strip() or None
        normalized_representation_term = representation_term
        if representation_term is not UNSET:
            if representation_term is None:
                raise ValueError("representation_term must not be null.")
            if isinstance(representation_term, str):
                normalized_representation_term = representation_term.strip() or None
            if not normalized_representation_term:
                raise ValueError("representation_term must not be empty.")
        normalized_definition = definition
        if definition is not UNSET and isinstance(definition, str):
            normalized_definition = definition.strip() or None
        normalized_definition_source = definition_source
        if definition_source is not UNSET and isinstance(definition_source, str):
            normalized_definition_source = definition_source.strip() or None
        normalized_default_value = default_value
        if default_value is not UNSET and isinstance(default_value, str):
            normalized_default_value = default_value
        normalized_fixed_value = fixed_value
        if fixed_value is not UNSET and isinstance(fixed_value, str):
            normalized_fixed_value = fixed_value

        if normalized_default_value is not UNSET and normalized_fixed_value is not UNSET:
            if normalized_default_value is not None and normalized_fixed_value is not None:
                raise ValueError("Only one of default_value or fixed_value can be set at a time.")

        normalized_cardinality_min: int | None | UnsetType = UNSET
        normalized_cardinality_max: int | None | UnsetType = UNSET
        if cardinality is not UNSET:
            normalized_cardinality_min, normalized_cardinality_max = self._normalize_dt_sc_cardinality(cardinality)

        primitive_selection_requested = any(
            value is not UNSET for value in (xbt_manifest_id, code_list_manifest_id, agency_id_list_manifest_id)
        )
        primitive_mutation_requested = add_primitives is not UNSET or remove_primitives is not UNSET
        primitive_change_requested = primitive_selection_requested or primitive_mutation_requested
        normalized_xbt_manifest_id: int | None = None
        normalized_code_list_manifest_id: int | None = None
        normalized_agency_id_list_manifest_id: int | None = None
        if primitive_selection_requested:
            (
                normalized_xbt_manifest_id,
                normalized_code_list_manifest_id,
                normalized_agency_id_list_manifest_id,
            ) = self._normalize_default_primitive_target(
                xbt_manifest_id=None if xbt_manifest_id is UNSET else xbt_manifest_id,
                code_list_manifest_id=None if code_list_manifest_id is UNSET else code_list_manifest_id,
                agency_id_list_manifest_id=None if agency_id_list_manifest_id is UNSET else agency_id_list_manifest_id,
            )

        updates: list[str] = []
        if normalized_property_term is not UNSET and normalized_property_term != dt_sc.property_term:
            updates.append("property_term")
        if normalized_representation_term is not UNSET and normalized_representation_term != dt_sc.representation_term:
            updates.append("representation_term")
        if (
            normalized_cardinality_min is not UNSET
            and (
                int(normalized_cardinality_min) != int(dt_sc.cardinality_min)
                or int(normalized_cardinality_max) != int(dt_sc.cardinality_max)
            )
        ):
            updates.append("cardinality")
        if deprecated is not UNSET and bool(deprecated) != bool(dt_sc.is_deprecated):
            updates.append("deprecated")

        current_default_value = dt_sc.value_constraint.default_value if dt_sc.value_constraint else None
        current_fixed_value = dt_sc.value_constraint.fixed_value if dt_sc.value_constraint else None
        value_constraint_changed = False
        if normalized_default_value is not UNSET and normalized_default_value != current_default_value:
            value_constraint_changed = True
        if normalized_fixed_value is not UNSET and normalized_fixed_value != current_fixed_value:
            value_constraint_changed = True
        if value_constraint_changed:
            updates.append("value_constraint")
        if normalized_definition is not UNSET and normalized_definition != dt_sc.definition:
            updates.append("definition")
        if normalized_definition_source is not UNSET and normalized_definition_source != dt_sc.definition_source:
            updates.append("definition_source")
        if not updates and not primitive_change_requested:
            return UpdateDataTypeSupplementaryComponentServiceResult(dt_sc_manifest_id=int(dt_sc_manifest_id), updates=[])

        if updates:
            await self._repo.update_dt_sc(
                dt_sc_manifest_id=dt_sc_manifest_id,
                property_term=None if normalized_property_term is UNSET else normalized_property_term,
                property_term_set=normalized_property_term is not UNSET,
                representation_term=None if normalized_representation_term is UNSET else normalized_representation_term,
                representation_term_set=normalized_representation_term is not UNSET,
                cardinality_min=(
                    None
                    if normalized_cardinality_min is UNSET
                    else (None if normalized_cardinality_min is None else int(normalized_cardinality_min))
                ),
                cardinality_min_set=normalized_cardinality_min is not UNSET,
                cardinality_max=(
                    None
                    if normalized_cardinality_max is UNSET
                    else (None if normalized_cardinality_max is None else int(normalized_cardinality_max))
                ),
                cardinality_max_set=normalized_cardinality_max is not UNSET,
                deprecated=None if deprecated is UNSET else bool(deprecated),
                deprecated_set=deprecated is not UNSET,
                default_value=None if normalized_default_value is UNSET else normalized_default_value,
                default_value_set=normalized_default_value is not UNSET,
                fixed_value=None if normalized_fixed_value is UNSET else normalized_fixed_value,
                fixed_value_set=normalized_fixed_value is not UNSET,
                definition=None if normalized_definition is UNSET else normalized_definition,
                definition_set=normalized_definition is not UNSET,
                definition_source=None if normalized_definition_source is UNSET else normalized_definition_source,
                definition_source_set=normalized_definition_source is not UNSET,
                requester_user_id=self._requester_user_id,
            )
            if primitive_mutation_requested and normalized_representation_term is not UNSET:
                dt, dt_sc = await self._get_owner_dt_and_sc(dt_sc_manifest_id)
        primitive_updated = False
        if primitive_change_requested:
            if primitive_mutation_requested:
                desired_primitives, membership_changed, default_changed = self._resolve_primitive_update(
                    current_primitives=dt_sc.primitives,
                    add_primitives=[] if add_primitives is UNSET else add_primitives,
                    remove_primitives=[] if remove_primitives is UNSET else remove_primitives,
                    default_primitive=(
                        None
                        if not primitive_selection_requested
                        else DataTypePrimitiveServiceRecord(
                            xbt_manifest_id=normalized_xbt_manifest_id,
                            code_list_manifest_id=normalized_code_list_manifest_id,
                            agency_id_list_manifest_id=normalized_agency_id_list_manifest_id,
                            is_default=True,
                        )
                    ),
                )
                primitive_updated = await self._replace_dt_sc_primitives(
                    dt_sc_manifest_id=dt_sc_manifest_id,
                    primitives=desired_primitives,
                )
                if primitive_updated and membership_changed:
                    updates.append("primitives")
                if primitive_updated and default_changed:
                    updates.append("default_primitive")
            else:
                primitive_updated = await self._repo.change_dt_sc_default_primitive(
                    dt_sc_manifest_id=dt_sc_manifest_id,
                    xbt_manifest_id=normalized_xbt_manifest_id,
                    code_list_manifest_id=normalized_code_list_manifest_id,
                    agency_id_list_manifest_id=normalized_agency_id_list_manifest_id,
                    requester_user_id=self._requester_user_id,
                )
                if primitive_updated:
                    updates.append("default_primitive")
        for inherited_dt_sc_manifest_id in await self._repo.list_direct_inherited_dt_sc_manifest_ids(dt_sc_manifest_id):
            await self.update_dt_sc(
                dt_sc_manifest_id=inherited_dt_sc_manifest_id,
                property_term=normalized_property_term,
                representation_term=normalized_representation_term,
                cardinality=cardinality,
                deprecated=deprecated,
                default_value=normalized_default_value,
                fixed_value=normalized_fixed_value,
                definition=normalized_definition,
                definition_source=normalized_definition_source,
                xbt_manifest_id=normalized_xbt_manifest_id if primitive_selection_requested else UNSET,
                code_list_manifest_id=normalized_code_list_manifest_id if primitive_selection_requested else UNSET,
                agency_id_list_manifest_id=normalized_agency_id_list_manifest_id if primitive_selection_requested else UNSET,
                add_primitives=add_primitives,
                remove_primitives=remove_primitives,
                skip_ownership_check=True,
            )
        return UpdateDataTypeSupplementaryComponentServiceResult(
            dt_sc_manifest_id=int(dt_sc_manifest_id),
            updates=sorted(set(updates)),
        )

    async def delete_dt_sc(
        self,
        *,
        dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
        skip_ownership_check: bool = False,
    ) -> UpdateDataTypeSupplementaryComponentServiceResult:
        """Delete a DT_SC and any inherited DT_SC copies."""
        dt, dt_sc = await self._get_owner_dt_and_sc(dt_sc_manifest_id)
        self._assert_can_update_dt_sc(dt, dt_sc, skip_ownership_check=skip_ownership_check)
        if not skip_ownership_check and await self._repo.get_based_dt_sc_manifest_id(dt_sc_manifest_id) is not None:
            raise ValueError("Inherited DT_SCs cannot be deleted directly. Delete the base DT_SC instead.")
        if await self._repo.count_bbie_sc_refs(dt_sc_manifest_id) > 0:
            raise ValueError("This supplementary component is referenced in one or more BIEs and cannot be deleted.")

        for inherited_dt_sc_manifest_id in await self._repo.list_direct_inherited_dt_sc_manifest_ids(dt_sc_manifest_id):
            await self.delete_dt_sc(dt_sc_manifest_id=inherited_dt_sc_manifest_id, skip_ownership_check=True)
        deleted = await self._repo.delete_dt_sc(
            dt_sc_manifest_id=dt_sc_manifest_id,
            requester_user_id=self._requester_user_id,
        )
        if not deleted:
            raise LookupError(
                f"No DT_SC exists with manifest ID {int(dt_sc_manifest_id)}. Please verify the identifier and try again."
            )
        return UpdateDataTypeSupplementaryComponentServiceResult(
            dt_sc_manifest_id=int(dt_sc_manifest_id),
            updates=["deleted"],
        )

    async def remove_dt_tags(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        tag_id: list[int],
    ) -> UpdateDataTypeServiceResult:
        """Detach tags from a WIP DT owned by the requester."""
        dt = await self.get(dt_manifest_id)
        if dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(dt_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_dt(dt)

        normalized_tag_ids = self._normalize_tag_ids(tag_id) or []
        current_tag_ids = {int(tag.tag_id) for tag in dt.tags}
        tag_ids_to_remove = [single_tag_id for single_tag_id in normalized_tag_ids if single_tag_id in current_tag_ids]
        if not tag_ids_to_remove:
            return UpdateDataTypeServiceResult(dt_manifest_id=int(dt_manifest_id), updates=[])

        await self._repo.remove_dt_tags(
            dt_manifest_id=dt_manifest_id,
            tag_id=tag_ids_to_remove,
            requester_user_id=self._requester_user_id,
        )
        return UpdateDataTypeServiceResult(dt_manifest_id=int(dt_manifest_id), updates=["tags"])

    async def change_dt_state(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        state: DataTypeState,
    ) -> UpdateDataTypeServiceResult:
        """Transition a DT lifecycle state according to connectCenter rules."""
        dt = await self.get(dt_manifest_id)
        if dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(dt_manifest_id)}. Please verify the identifier and try again."
            )

        self._assert_data_type_state_transition_allowed(dt, state=state)
        restore = str(dt.state or "") == "Deleted" and state == "WIP"
        if restore:
            self._assert_can_restore_deleted_dt(dt)
        else:
            self._assert_owner_or_admin(dt)
            if state == "Deleted":
                self._assert_revision_one_only(dt, label="DT")
            elif dt.namespace is None:
                raise ValueError(f"'{dt.den}' namespace required.")

        await self._repo.change_dt_state(
            dt_manifest_id=dt_manifest_id,
            state=state,
            restore_owner=restore,
            implicit_move=False,
            requester_user_id=self._requester_user_id,
        )
        return UpdateDataTypeServiceResult(dt_manifest_id=int(dt_manifest_id), updates=[f"state:{state}"])

    async def change_dt_default_primitive(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        xbt_manifest_id: int | None = None,
        code_list_manifest_id: int | None = None,
        agency_id_list_manifest_id: int | None = None,
    ) -> UpdateDataTypeServiceResult:
        """Change the default primitive selection for a DT and inherited DTs."""
        dt = await self.get(dt_manifest_id)
        if dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(dt_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_dt(dt)
        xbt_manifest_id, code_list_manifest_id, agency_id_list_manifest_id = self._normalize_default_primitive_target(
            xbt_manifest_id=xbt_manifest_id,
            code_list_manifest_id=code_list_manifest_id,
            agency_id_list_manifest_id=agency_id_list_manifest_id,
        )
        updated = await self._change_dt_default_primitive_recursive(
            dt_manifest_id=dt_manifest_id,
            xbt_manifest_id=xbt_manifest_id,
            code_list_manifest_id=code_list_manifest_id,
            agency_id_list_manifest_id=agency_id_list_manifest_id,
            skip_ownership_check=False,
        )
        return UpdateDataTypeServiceResult(
            dt_manifest_id=int(dt_manifest_id),
            updates=["default_primitive"] if updated else [],
        )

    async def change_dt_sc_default_primitive(
        self,
        *,
        dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
        xbt_manifest_id: int | None = None,
        code_list_manifest_id: int | None = None,
        agency_id_list_manifest_id: int | None = None,
        skip_ownership_check: bool = False,
    ) -> UpdateDataTypeSupplementaryComponentServiceResult:
        """Change the default primitive selection for a DT_SC and inherited DT_SCs."""
        dt, dt_sc = await self._get_owner_dt_and_sc(dt_sc_manifest_id)
        self._assert_can_update_dt_sc(dt, dt_sc, skip_ownership_check=skip_ownership_check)
        xbt_manifest_id, code_list_manifest_id, agency_id_list_manifest_id = self._normalize_default_primitive_target(
            xbt_manifest_id=xbt_manifest_id,
            code_list_manifest_id=code_list_manifest_id,
            agency_id_list_manifest_id=agency_id_list_manifest_id,
        )

        updated = await self._repo.change_dt_sc_default_primitive(
            dt_sc_manifest_id=dt_sc_manifest_id,
            xbt_manifest_id=xbt_manifest_id,
            code_list_manifest_id=code_list_manifest_id,
            agency_id_list_manifest_id=agency_id_list_manifest_id,
            requester_user_id=self._requester_user_id,
        )
        for inherited_dt_sc_manifest_id in await self._repo.list_direct_inherited_dt_sc_manifest_ids(dt_sc_manifest_id):
            await self.change_dt_sc_default_primitive(
                dt_sc_manifest_id=inherited_dt_sc_manifest_id,
                xbt_manifest_id=xbt_manifest_id,
                code_list_manifest_id=code_list_manifest_id,
                agency_id_list_manifest_id=agency_id_list_manifest_id,
                skip_ownership_check=True,
            )
        return UpdateDataTypeSupplementaryComponentServiceResult(
            dt_sc_manifest_id=int(dt_sc_manifest_id),
            updates=["default_primitive"] if updated else [],
        )

    async def revise_dt(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        requested_action: Literal["revise", "amend"] | None = None,
    ) -> ReviseDataTypeServiceResult:
        """Create a new DT working revision from a stable DT revision."""
        dt = await self.get(dt_manifest_id)
        if dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(dt_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_revise_dt(dt, requested_action=requested_action)

        revised = await self._repo.revise_dt(
            dt_manifest_id=dt_manifest_id,
            requester_user_id=self._requester_user_id,
        )
        if not revised:
            raise LookupError(
                f"No DT exists with manifest ID {int(dt_manifest_id)}. Please verify the identifier and try again."
            )
        return ReviseDataTypeServiceResult(dt_manifest_id=int(dt_manifest_id), revised=True)

    async def cancel_dt(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
    ) -> CancelDataTypeServiceResult:
        """Cancel the current DT revision and restore the previous stable revision."""
        dt = await self.get(dt_manifest_id)
        if dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(dt_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_cancel_dt(dt)

        cancelled = await self._repo.cancel_dt(dt_manifest_id=dt_manifest_id)
        if not cancelled:
            raise LookupError(
                f"No DT exists with manifest ID {int(dt_manifest_id)}. Please verify the identifier and try again."
            )
        return CancelDataTypeServiceResult(dt_manifest_id=int(dt_manifest_id), cancelled=True)

    async def discard_dt(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
    ) -> DiscardDataTypeServiceResult:
        """Discard a Deleted DT and its direct related records permanently."""
        dt = await self.get(dt_manifest_id)
        if dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(dt_manifest_id)}. Please verify the identifier and try again."
            )
        if dt.state != "Deleted":
            raise ValueError(
                f"The DT '{dt.den}' cannot be discarded because it is in the '{dt.state}' state. "
                "Only DTs in the 'Deleted' state can be discarded."
            )
        if await self._repo.has_deriving_dts(dt_manifest_id):
            raise ValueError(
                f"Please discard derived DTs first before discarding the DT '{dt.den}'."
            )
        if await self._repo.has_related_bccps_for_dt(dt_manifest_id):
            raise ValueError(
                f"Please discard related BCCPs first before discarding the DT '{dt.den}'."
            )

        discarded = await self._repo.discard_dt(dt_manifest_id=dt_manifest_id)
        if not discarded:
            raise LookupError(
                f"No DT exists with manifest ID {int(dt_manifest_id)}. Please verify the identifier and try again."
            )
        return DiscardDataTypeServiceResult(dt_manifest_id=int(dt_manifest_id), discarded=True)

    def _to_data_type_result(self, row: Any, *, users_by_id: dict[int, AppUserRow]) -> DataTypeServiceResult:
        """Map repository row to Data Type DTO.

        Args:
            row: Repository row model to convert into a DTO.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Result of the operation.
        """
        return DataTypeServiceResult(
            dt_manifest_id=row.dt_manifest_id,
            dt_id=row.dt_id,
            base_dt=row.base_dt,
            guid=row.guid,
            den=row.den,
            data_type_term=row.data_type_term,
            qualifier=row.qualifier,
            representation_term=row.representation_term,
            six_digit_id=row.six_digit_id,
            definition=row.definition,
            definition_source=row.definition_source,
            content_component_definition=row.content_component_definition,
            commonly_used=row.commonly_used,
            is_deprecated=row.is_deprecated,
            state=row.state,
            primitives=[self._to_primitive_result(primitive) for primitive in row.primitives],
            supplementary_components=[
                self._to_supplementary_component_result(component)
                for component in row.supplementary_components
            ],
            tags=[to_dataclass(TagSummaryServiceRecord, tag) for tag in row.tags],
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

    @staticmethod
    def _to_supplementary_component_result(component: Any) -> DataTypeSupplementaryComponentServiceRecord:
        value_constraint = None
        if component.default_value is not None or component.fixed_value is not None:
            value_constraint = DataTypeValueConstraintServiceRecord(
                default_value=component.default_value,
                fixed_value=component.fixed_value,
            )
        return DataTypeSupplementaryComponentServiceRecord(
            dt_sc_manifest_id=component.dt_sc_manifest_id,
            dt_sc_id=component.dt_sc_id,
            guid=component.guid,
            object_class_term=component.object_class_term,
            property_term=component.property_term,
            representation_term=component.representation_term,
            definition=component.definition,
            definition_source=component.definition_source,
            cardinality_min=component.cardinality_min,
            cardinality_max=component.cardinality_max,
            value_constraint=value_constraint,
            is_deprecated=component.is_deprecated,
            primitives=[DataTypeService._to_primitive_result(primitive) for primitive in component.primitives],
        )

    @staticmethod
    def _to_primitive_result(component: Any) -> DataTypePrimitiveServiceRecord:
        return DataTypePrimitiveServiceRecord(
            cdt_pri_name=component.cdt_pri_name,
            xbt_manifest_id=component.xbt_manifest_id,
            code_list_manifest_id=component.code_list_manifest_id,
            agency_id_list_manifest_id=component.agency_id_list_manifest_id,
            is_default=bool(component.is_default),
        )

    @property
    def _requester_user_id(self) -> int:
        """Return the current requester user ID as a plain integer."""
        return int(self._requester.user.user_id)

    def _is_admin(self) -> bool:
        """Return True when the requester has the Admin role."""
        return "Admin" in self._requester.user.roles

    def _requester_is_developer(self) -> bool:
        """Return True when the requester belongs to the developer role family."""
        return "Developer" in self._requester.user.roles

    def _assert_can_create_data_types(self, *, release_num: str) -> None:
        """Validate create-branch rules for the requester's role family."""
        if self._requester_is_developer():
            if str(release_num) != "Working":
                raise PermissionError("It only allows to create DTs in the 'Working' branch for developers.")
            return
        if str(release_num) == "Working":
            raise PermissionError("It only allows to create DTs in non-'Working' branches for end-users.")

    @staticmethod
    def _assert_release_is_published(release: ReleaseServiceResult) -> None:
        """Require the target release to be in Published state."""
        if str(release.state) != "Published":
            raise ValueError(f"'{release.state}' release cannot be modified.")

    def _assert_owner_or_admin(self, dt: DataTypeServiceResult) -> None:
        """Require the requester to own the DT or be an admin."""
        if not self._is_admin() and int(dt.owner.user_id) != self._requester_user_id:
            raise PermissionError("It only allows to modify the core component by the owner.")

    def _assert_dt_is_wip(self, dt: DataTypeServiceResult) -> None:
        """Require the DT to be in the mutable WIP state."""
        if dt.state != "WIP":
            raise ValueError(
                f"The DT '{dt.den}' cannot be updated because it is in the '{dt.state}' state. "
                "Only DTs in the 'WIP' state can be updated."
            )

    def _assert_dt_allows_new_supplementary_component(self, dt: DataTypeServiceResult) -> None:
        """Require the owner DT to be WIP before adding a supplementary component."""
        if dt.state != "WIP":
            raise ValueError(
                f"The supplementary component cannot be added to DT '{dt.den}' because it is in the '{dt.state}' state. "
                "Supplementary components can only be added to DTs in the 'WIP' state."
            )

    def _assert_can_update_dt(self, dt: DataTypeServiceResult) -> None:
        """Validate owner/admin and WIP requirements for DT updates."""
        self._assert_dt_is_wip(dt)
        self._assert_owner_or_admin(dt)

    def _assert_can_transfer_dt(self, dt: DataTypeServiceResult) -> None:
        """Validate owner/admin and WIP requirements for DT ownership transfer."""
        if dt.state != "WIP":
            raise ValueError(
                f"The DT '{dt.den}' cannot be transferred because it is in the '{dt.state}' state. "
                "Only DTs in the 'WIP' state can be transferred."
            )
        self._assert_owner_or_admin(dt)

    def _assert_can_update_dt_sc(
        self,
        dt: DataTypeServiceResult,
        dt_sc: DataTypeSupplementaryComponentServiceRecord,
        *,
        skip_ownership_check: bool,
    ) -> None:
        """Validate owner/admin and WIP requirements for DT_SC updates."""
        if dt.state != "WIP":
            raise ValueError(
                f"The DT_SC '{dt_sc.property_term or int(dt_sc.dt_sc_manifest_id)}' cannot be updated because "
                f"its owner DT '{dt.den}' is in the '{dt.state}' state. Only DT_SCs under `WIP` DTs can be updated."
            )
        if not skip_ownership_check:
            self._assert_owner_or_admin(dt)

    def _assert_data_type_state_transition_allowed(
        self,
        dt: DataTypeServiceResult,
        *,
        state: DataTypeState,
    ) -> None:
        """Validate branch-specific DT state transitions."""
        current_state = str(dt.state or "")
        release_num = str(dt.release.release_num or "")
        allowed_transitions = self._state_transitions_for_release(release_num=release_num)
        if current_state not in allowed_transitions:
            branch_label = "'Working'" if release_num == "Working" else "non-'Working'"
            raise ValueError(
                f"The DT is in '{current_state}' state, which cannot be changed by this service for {branch_label} releases."
            )
        if state not in allowed_transitions[current_state]:
            raise ValueError(f"The core component in '{current_state}' state cannot move to '{state}' state.")

    def _assert_can_restore_deleted_dt(self, dt: DataTypeServiceResult) -> None:
        """Validate restore-branch rules for Deleted -> WIP transitions."""
        roles = set(self._requester.user.roles)
        release_num = str(dt.release.release_num or "")
        if release_num == "Working":
            if "Developer" not in roles:
                raise PermissionError("It only allows to restore the component in 'Working' branch for developers.")
            return
        if "End-User" not in roles:
            raise PermissionError("It only allows to restore the component in non-'Working' branch for end-users.")

    def _assert_revision_one_only(self, dt: DataTypeServiceResult, *, label: str) -> None:
        """Require revision 1 before allowing Deleted state."""
        revision_num = int(dt.log.revision_num) if dt.log is not None else 1
        if revision_num != 1:
            raise ValueError(
                f"'{dt.den}' can't be marked as deleted because it is a later revision. "
                "Only the first revision can be deleted. If you want to undo this revised version, "
                f"please cancel the {label} instead."
            )

    def _assert_can_access_revision_branch(self, dt: DataTypeServiceResult) -> None:
        """Validate revise/cancel branch rules for the requester's role family."""
        release_num = str(dt.release.release_num or "")
        if self._requester_is_developer():
            if release_num != "Working":
                raise ValueError("It only allows to revise the component in 'Working' branch for developers.")
            return
        if release_num == "Working":
            raise ValueError("It only allows to amend the component in non-'Working' branches for end-users.")

    def _assert_same_role_family_as_owner(self, dt: DataTypeServiceResult) -> None:
        """Require the requester and owner to both be developer-side or both end-user-side."""
        owner_roles = set(dt.owner.roles)
        owner_is_developer = "Developer" in owner_roles
        if self._requester_is_developer() != owner_is_developer:
            raise ValueError("It only allows to revise the component for users in the same roles.")

    def _assert_can_revise_dt(
        self,
        dt: DataTypeServiceResult,
        *,
        requested_action: Literal["revise", "amend"] | None = None,
    ) -> None:
        """Validate revise rules for DT revisions."""
        expected_action: Literal["revise", "amend"] = "revise" if self._requester_is_developer() else "amend"
        if requested_action is not None and requested_action != expected_action:
            if expected_action == "revise":
                raise PermissionError("Developer-side DTs must use revise.")
            raise PermissionError("End-user DTs must use amend.")
        if self._requester_is_developer():
            if dt.state != "Published":
                raise ValueError("Only the core component in 'Published' state can be revised.")
        else:
            if dt.state != "Production":
                raise ValueError("Only the core component in 'Production' state can be amended.")
        self._assert_can_access_revision_branch(dt)
        self._assert_same_role_family_as_owner(dt)

    def _assert_can_cancel_dt(self, dt: DataTypeServiceResult) -> None:
        """Validate cancel rules for DT revisions."""
        if dt.state != "WIP":
            raise ValueError(
                f"The DT '{dt.den}' cannot be cancelled because it is in the '{dt.state}' state. "
                "Only DTs in the 'WIP' state can be cancelled."
            )
        self._assert_can_access_revision_branch(dt)
        self._assert_same_role_family_as_owner(dt)

    async def _get_owner_dt_and_sc(
        self,
        dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
    ) -> tuple[DataTypeServiceResult, DataTypeSupplementaryComponentServiceRecord]:
        owner_dt_manifest_id = await self._repo.get_owner_dt_manifest_id_by_dt_sc(dt_sc_manifest_id)
        if owner_dt_manifest_id is None:
            raise LookupError(
                f"No DT_SC exists with manifest ID {int(dt_sc_manifest_id)}. Please verify the identifier and try again."
            )
        dt = await self.get(owner_dt_manifest_id)
        if dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(owner_dt_manifest_id)}. Please verify the identifier and try again."
            )
        dt_sc = next((component for component in dt.supplementary_components if int(component.dt_sc_manifest_id) == int(dt_sc_manifest_id)), None)
        if dt_sc is None:
            raise LookupError(
                f"No DT_SC exists with manifest ID {int(dt_sc_manifest_id)}. Please verify the identifier and try again."
            )
        return dt, dt_sc

    async def _create_dt_sc_from_base_recursive(
        self,
        *,
        owner_dt_manifest_id: DataTypeManifestId,
        based_dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
    ) -> DataTypeSupplementaryComponentManifestId:
        dt = await self.get(owner_dt_manifest_id)
        if dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(owner_dt_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_dt_allows_new_supplementary_component(dt)
        dt_sc_manifest_id = await self._repo.create_dt_sc_from_base(
            owner_dt_manifest_id=owner_dt_manifest_id,
            based_dt_sc_manifest_id=based_dt_sc_manifest_id,
            requester_user_id=self._requester_user_id,
        )
        for inherited_dt_manifest_id in await self._repo.list_direct_inherited_dt_manifest_ids(owner_dt_manifest_id):
            await self._create_dt_sc_from_base_recursive(
                owner_dt_manifest_id=inherited_dt_manifest_id,
                based_dt_sc_manifest_id=dt_sc_manifest_id,
            )
        return dt_sc_manifest_id

    async def _change_dt_default_primitive_recursive(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        xbt_manifest_id: int | None,
        code_list_manifest_id: int | None,
        agency_id_list_manifest_id: int | None,
        skip_ownership_check: bool,
    ) -> bool:
        dt = await self.get(dt_manifest_id)
        if dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(dt_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_dt_is_wip(dt)
        if not skip_ownership_check:
            self._assert_owner_or_admin(dt)

        updated = await self._repo.change_dt_default_primitive(
            dt_manifest_id=dt_manifest_id,
            xbt_manifest_id=xbt_manifest_id,
            code_list_manifest_id=code_list_manifest_id,
            agency_id_list_manifest_id=agency_id_list_manifest_id,
            requester_user_id=self._requester_user_id,
        )
        inherited_updated = False
        for inherited_dt_manifest_id in await self._repo.list_direct_inherited_dt_manifest_ids(dt_manifest_id):
            inherited_updated = (
                await self._change_dt_default_primitive_recursive(
                    dt_manifest_id=inherited_dt_manifest_id,
                    xbt_manifest_id=xbt_manifest_id,
                    code_list_manifest_id=code_list_manifest_id,
                    agency_id_list_manifest_id=agency_id_list_manifest_id,
                    skip_ownership_check=True,
                )
                or inherited_updated
            )
        return updated or inherited_updated

    async def _replace_dt_primitives_recursive(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        primitives: list[DataTypePrimitiveServiceRecord],
        skip_ownership_check: bool,
    ) -> bool:
        dt = await self.get(dt_manifest_id)
        if dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(dt_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_dt_is_wip(dt)
        if not skip_ownership_check:
            self._assert_owner_or_admin(dt)

        updated = await self._repo.replace_dt_primitives(
            dt_manifest_id=dt_manifest_id,
            primitives=[
                DataTypePrimitiveServiceRecord(
                    cdt_pri_name=primitive.cdt_pri_name,
                    xbt_manifest_id=primitive.xbt_manifest_id,
                    code_list_manifest_id=primitive.code_list_manifest_id,
                    agency_id_list_manifest_id=primitive.agency_id_list_manifest_id,
                    is_default=primitive.is_default,
                )
                for primitive in primitives
            ],
            requester_user_id=self._requester_user_id,
        )
        inherited_updated = False
        for inherited_dt_manifest_id in await self._repo.list_direct_inherited_dt_manifest_ids(dt_manifest_id):
            inherited_updated = (
                await self._replace_dt_primitives_recursive(
                    dt_manifest_id=inherited_dt_manifest_id,
                    primitives=primitives,
                    skip_ownership_check=True,
                )
                or inherited_updated
            )
        return updated or inherited_updated

    async def _replace_dt_sc_primitives(
        self,
        *,
        dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
        primitives: list[DataTypePrimitiveServiceRecord],
    ) -> bool:
        return await self._repo.replace_dt_sc_primitives(
            dt_sc_manifest_id=dt_sc_manifest_id,
            primitives=[
                DataTypePrimitiveServiceRecord(
                    cdt_pri_name=primitive.cdt_pri_name,
                    xbt_manifest_id=primitive.xbt_manifest_id,
                    code_list_manifest_id=primitive.code_list_manifest_id,
                    agency_id_list_manifest_id=primitive.agency_id_list_manifest_id,
                    is_default=primitive.is_default,
                )
                for primitive in primitives
            ],
            requester_user_id=self._requester_user_id,
        )

    async def _find_non_wip_inherited_dts(
        self,
        dt_manifest_id: DataTypeManifestId,
    ) -> list[DataTypeServiceResult]:
        results: list[DataTypeServiceResult] = []
        for inherited_dt_manifest_id in await self._repo.list_direct_inherited_dt_manifest_ids(dt_manifest_id):
            inherited_dt = await self.get(inherited_dt_manifest_id)
            if inherited_dt is None:
                continue
            if inherited_dt.state != "WIP":
                results.append(inherited_dt)
            results.extend(await self._find_non_wip_inherited_dts(inherited_dt_manifest_id))
        return results

    async def _resolve_replacement_base_dt(
        self,
        *,
        target_dt: DataTypeServiceResult,
        based_dt_manifest_id: DataTypeManifestId,
    ) -> DataTypeServiceResult:
        if int(based_dt_manifest_id) == int(target_dt.dt_manifest_id):
            raise ValueError("A DT cannot be based on itself.")
        based_dt = await self.get(based_dt_manifest_id)
        if based_dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(based_dt_manifest_id)}. Please verify the identifier and try again."
            )
        await self._assert_base_dt_allowed_for_release(
            target_release_id=target_dt.release.release_id,
            target_library_id=target_dt.library.library_id,
            based_dt=based_dt,
        )

        current_base = based_dt
        visited: set[int] = set()
        while current_base.base_dt is not None:
            current_base_manifest_id = int(current_base.dt_manifest_id)
            if current_base_manifest_id in visited:
                break
            visited.add(current_base_manifest_id)
            next_base_manifest_id = current_base.base_dt.dt_manifest_id
            if int(next_base_manifest_id) == int(target_dt.dt_manifest_id):
                raise ValueError("Changing the base DT would create a cycle.")
            next_base = await self.get(next_base_manifest_id)
            if next_base is None:
                break
            current_base = next_base

        return based_dt

    async def _assert_base_dt_allowed_for_release(
        self,
        *,
        target_release_id: ReleaseId | int,
        target_library_id: int,
        based_dt: DataTypeServiceResult,
    ) -> None:
        base_library_id = int(based_dt.library.library_id)
        base_release_id = int(based_dt.release.release_id)
        normalized_target_release_id = int(target_release_id)
        if base_library_id == int(target_library_id):
            if base_release_id != normalized_target_release_id:
                raise ValueError(
                    "The base DT must belong to the same release as the target release when both DTs are in the same library. "
                    "Please choose a base DT from the target release and try again."
                )
            return

        dependent_release_ids = {
            int(dependent_release_id)
            for dependent_release_id in await self._release_service.get_dependent_releases(
                ReleaseId(normalized_target_release_id)
            )
        }
        if base_release_id not in dependent_release_ids:
            raise ValueError(
                "The base DT must come from a dependent release when the base DT belongs to a different library. "
                "Please choose a base DT from one of the target release dependencies and try again."
            )

    async def _list_inherited_dt_sc_manifest_ids(
        self,
        dt_manifest_id: DataTypeManifestId,
    ) -> list[DataTypeSupplementaryComponentManifestId]:
        dt = await self.get(dt_manifest_id)
        if dt is None:
            raise LookupError(
                f"No DT exists with manifest ID {int(dt_manifest_id)}. Please verify the identifier and try again."
            )
        inherited_manifest_ids: list[DataTypeSupplementaryComponentManifestId] = []
        for supplementary_component in dt.supplementary_components:
            if (
                await self._repo.get_based_dt_sc_manifest_id(supplementary_component.dt_sc_manifest_id)
                is not None
            ):
                inherited_manifest_ids.append(supplementary_component.dt_sc_manifest_id)
        return inherited_manifest_ids

    async def _delete_dt_sc_branch_recursive(
        self,
        dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
    ) -> bool:
        deleted = False
        for inherited_dt_sc_manifest_id in await self._repo.list_direct_inherited_dt_sc_manifest_ids(dt_sc_manifest_id):
            deleted = await self._delete_dt_sc_branch_recursive(inherited_dt_sc_manifest_id) or deleted
        deleted_here = await self._repo.delete_dt_sc(
            dt_sc_manifest_id=dt_sc_manifest_id,
            requester_user_id=self._requester_user_id,
        )
        return deleted_here or deleted

    @staticmethod
    def _normalize_default_primitive_target(
        *,
        xbt_manifest_id: int | None,
        code_list_manifest_id: int | None,
        agency_id_list_manifest_id: int | None,
    ) -> tuple[int | None, int | None, int | None]:
        provided = [
            ("xbt_manifest_id", xbt_manifest_id),
            ("code_list_manifest_id", code_list_manifest_id),
            ("agency_id_list_manifest_id", agency_id_list_manifest_id),
        ]
        populated = [(name, value) for name, value in provided if value is not None]
        if len(populated) != 1:
            raise ValueError(
                "Exactly one of xbt_manifest_id, code_list_manifest_id, or agency_id_list_manifest_id must be provided."
            )
        return xbt_manifest_id, code_list_manifest_id, agency_id_list_manifest_id

    @classmethod
    def _resolve_primitive_update(
        cls,
        *,
        current_primitives: list[DataTypePrimitiveServiceRecord],
        add_primitives: list[DataTypePrimitiveServiceRecord],
        remove_primitives: list[DataTypePrimitiveServiceRecord],
        default_primitive: DataTypePrimitiveServiceRecord | None,
    ) -> tuple[list[DataTypePrimitiveServiceRecord], bool, bool]:
        desired_by_key = {cls._primitive_key(primitive): primitive for primitive in current_primitives}
        current_keys = set(desired_by_key)
        current_default_key = next(
            (cls._primitive_key(primitive) for primitive in current_primitives if primitive.is_default),
            None,
        )

        for primitive in remove_primitives:
            cls._assert_primitive_mutation_supported(primitive, action="remove")
            desired_by_key.pop(cls._primitive_key(primitive), None)

        for primitive in add_primitives:
            cls._assert_primitive_mutation_supported(primitive, action="add")
            key = cls._primitive_key(primitive)
            desired_by_key[key] = DataTypePrimitiveServiceRecord(
                cdt_pri_name=primitive.cdt_pri_name,
                xbt_manifest_id=primitive.xbt_manifest_id,
                code_list_manifest_id=primitive.code_list_manifest_id,
                agency_id_list_manifest_id=primitive.agency_id_list_manifest_id,
                is_default=False,
            )
        explicit_default_key = None
        if default_primitive is not None:
            explicit_default_key = cls._primitive_key(default_primitive)

        desired_keys = list(desired_by_key)
        if not desired_keys:
            raise ValueError("At least one primitive must remain after applying the requested changes.")

        if explicit_default_key is not None:
            if explicit_default_key not in desired_by_key:
                raise ValueError("The requested default primitive must exist after applying primitive additions/removals.")
            target_default_key = explicit_default_key
        elif current_default_key in desired_by_key:
            target_default_key = current_default_key
        elif len(desired_keys) == 1:
            target_default_key = desired_keys[0]
        else:
            raise ValueError("Removing the default primitive requires selecting a replacement default primitive.")

        desired = [
            DataTypePrimitiveServiceRecord(
                cdt_pri_name=primitive.cdt_pri_name,
                xbt_manifest_id=primitive.xbt_manifest_id,
                code_list_manifest_id=primitive.code_list_manifest_id,
                agency_id_list_manifest_id=primitive.agency_id_list_manifest_id,
                is_default=cls._primitive_key(primitive) == target_default_key,
            )
            for primitive in desired_by_key.values()
        ]
        desired_keys_set = {cls._primitive_key(primitive) for primitive in desired}
        membership_changed = desired_keys_set != current_keys
        default_changed = target_default_key != current_default_key
        return desired, membership_changed, default_changed

    @staticmethod
    def _assert_primitive_mutation_supported(
        primitive: DataTypePrimitiveServiceRecord,
        *,
        action: str,
    ) -> None:
        populated = [
            primitive.xbt_manifest_id,
            primitive.code_list_manifest_id,
            primitive.agency_id_list_manifest_id,
        ]
        if sum(value is not None for value in populated) != 1:
            raise ValueError(
                f"Primitive {action} operations require exactly one of xbt_manifest_id, "
                "code_list_manifest_id, or agency_id_list_manifest_id."
            )
        if primitive.xbt_manifest_id is not None and primitive.cdt_pri_name is None:
            raise ValueError(
                f"Primitive {action} operations require cdt_pri_name when xbt_manifest_id is provided."
            )
        if primitive.cdt_pri_name is not None and primitive.cdt_pri_name not in CDT_PRIMITIVE_NAMES:
            allowed = ", ".join(sorted(CDT_PRIMITIVE_NAMES))
            raise ValueError(f"Primitive {action} operations require cdt_pri_name to be one of: {allowed}.")

    @staticmethod
    def _primitive_key(
        primitive: DataTypePrimitiveServiceRecord,
    ) -> tuple[int | None, int | None, int | None]:
        return (
            int(primitive.xbt_manifest_id) if primitive.xbt_manifest_id is not None else None,
            int(primitive.code_list_manifest_id) if primitive.code_list_manifest_id is not None else None,
            int(primitive.agency_id_list_manifest_id) if primitive.agency_id_list_manifest_id is not None else None,
        )

    @staticmethod
    def _normalize_dt_sc_cardinality(
        cardinality: DataTypeSupplementaryComponentCardinality,
    ) -> tuple[int, int]:
        if cardinality == "Prohibited":
            return 0, 0
        if cardinality == "Optional":
            return 0, 1
        return 1, 1

    @staticmethod
    def _normalize_tag_ids(tag_ids: list[int] | None) -> list[int] | None:
        """Validate and deduplicate optional tag identifiers while preserving order."""
        if tag_ids is None:
            return None
        normalized: list[int] = []
        seen: set[int] = set()
        for raw_tag_id in tag_ids:
            tag_id = int(raw_tag_id)
            if tag_id <= 0:
                raise ValueError("Each tag_id value must be a positive integer.")
            if tag_id not in seen:
                normalized.append(tag_id)
                seen.add(tag_id)
        return normalized

    @staticmethod
    def _state_transitions_for_release(*, release_num: str) -> dict[str, set[str]]:
        """Return the branch-specific DT state-transition graph."""
        if release_num == "Working":
            return {key: set(value) for key, value in WORKING_BRANCH_CORE_COMPONENT_STATE_TRANSITIONS.items()}
        return {key: set(value) for key, value in NON_WORKING_BRANCH_CORE_COMPONENT_STATE_TRANSITIONS.items()}
