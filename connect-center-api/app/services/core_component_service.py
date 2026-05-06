"""Service class for managing Core Component operations in connectCenter.

Core Components are the foundational building blocks of business information
standards:
- ACC (Aggregate Core Component): Complex aggregate structures.
- ASCCP (Association Core Component Property): Association properties to ACCs.
- BCCP (Basic Core Component Property): Basic scalar-like properties.

These abbreviations and semantics follow CCTS (UN/CEFACT Core Components
Technical Specification), standardized as ISO 15000-5.

This service coordinates repository-level reads, ACC creation, and release dependency logic to
deliver connectCenter-style behavior in a backend-friendly contract:
- Unified list retrieval across component types.
- ACC creation with role-sensitive `Working` vs non-`Working` release rules.
- Type-specific detail retrieval by manifest ID.
- Release dependency expansion (include dependent releases in list queries).
- Filter/pagination/sort handoff with defensive type validation.
"""


from __future__ import annotations

import logging
from dataclasses import replace
from dataclasses import dataclass
from typing import Literal

from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.repositories.contracts.core_component import CoreComponentRepositoryContract
from app.security import AuthenticatedUser
from app.services import load_users_by_ids, to_user_summary, UserSummary
from app.services.models import WhoAndWhen
from app.services.models.core_component import (
    AsccRelationshipServiceRecord,
    BccRelationshipServiceRecord,
    BaseAccSummaryServiceRecord,
    CancelAccServiceResult,
    CancelAsccpServiceResult,
    CancelBccpServiceResult,
    CoreComponentState,
    CreateAsccServiceResult,
    CreateBccServiceResult,
    CoreComponentServiceResult,
    CreateAccServiceResult,
    CreateAsccpServiceResult,
    CreateBccpServiceResult,
    DataTypeSummaryServiceRecord,
    GetAccServiceResult,
    GetAsccpServiceResult,
    GetBccpServiceResult,
    MoveAsccServiceResult,
    MoveBccServiceResult,
    OagisComponentType,
    DiscardAccServiceResult,
    DiscardAsccpServiceResult,
    DiscardBccpServiceResult,
    ReviseAccServiceResult,
    ReviseAsccpServiceResult,
    ReviseBccpServiceResult,
    TransferAccOwnershipServiceResult,
    TransferAsccpOwnershipServiceResult,
    TransferBccpOwnershipServiceResult,
    UpdateAsccServiceResult,
    UpdateAccServiceResult,
    UpdateAccBaseServiceResult,
    UpdateAccTagsServiceResult,
    UpdateBccServiceResult,
    UpdateAsccpServiceResult,
    UpdateBccpServiceResult,
    ValueConstraintServiceRecord,
)
from app.services.models.data_type import DataTypeServiceResult
from app.services.models.library import LibrarySummaryServiceRecord
from app.services.models.log import LogSummaryServiceRecord
from app.services.models.mapper import to_dataclass
from app.services.models.namespace import NamespaceSummaryServiceRecord
from app.services.models.release import ReleaseSummaryServiceRecord
from app.services.models.release import ReleaseServiceResult
from app.services.models.tag import TagSummaryServiceRecord
from app.services.data_type_service import DataTypeService
from app.services.release_service import ReleaseService
from app.services.utils.date import DateRange
from app.services.utils.owner import parse_owner_filter
from app.services.utils.pagination import PaginationParams, PaginationResponse
from app.types.unset import UNSET, UnsetType
from app.types.identifiers import (
    AccManifestId,
    AsccManifestId,
    AsccpManifestId,
    BccManifestId,
    BccpManifestId,
    DataTypeManifestId,
)
from app.types.identifiers import NamespaceId
from app.types.identifiers import ReleaseId
from app.utils.core_component_constants import (
    NON_WORKING_BRANCH_CORE_COMPONENT_STATE_TRANSITIONS,
    OAGIS_COMPONENT_TYPE_NAMES,
    OAGIS_COMPONENT_TYPE_VALUES,
    WORKING_BRANCH_CORE_COMPONENT_STATE_TRANSITIONS,
)

logger = logging.getLogger("connectcenter.service.core_component")


def _parse_tag_filter(tag: str | None) -> list[str] | None:
    """Parse comma-separated tag names for exact-match filtering."""
    if tag is None or not tag.strip():
        return None

    tag_names: list[str] = []
    for raw_token in tag.split(","):
        tag_name = raw_token.strip()
        if not tag_name:
            raise ValueError("The tag filter contains an empty tag name.")
        if tag_name not in tag_names:
            tag_names.append(tag_name)
    return tag_names


@dataclass(frozen=True)
class _FlattenedAccPropertyTerm:
    """Flattened property-term entry used for ACC structure validation."""

    property_term: str
    source: str


@dataclass(frozen=True)
class _SequencePreviewItem:
    """Preview item used to detect sequence-order ambiguity warnings."""

    component_type: str
    manifest_id: int
    relationship_label: str
    cardinality_max: int


class CoreComponentService:
    """Service façade for Core Component read and ACC-create operations.

    Responsibilities:
    - Validate requested component types against the supported type set.
    - Resolve dependent release IDs through ReleaseService before list queries.
    - Delegate data access and projection to repository implementations.

    Supported list sort keys:
    - `den`
    - `name`
    - `definition`
    - `creation_timestamp`
    - `last_update_timestamp`
    """

    _ORDER_BY_ALLOWED: set[str] = {
        "den",
        "name",
        "definition",
        "creation_timestamp",
        "last_update_timestamp",
    }
    def __init__(
        self,
        core_component_repository: CoreComponentRepositoryContract,
        release_service: ReleaseService,
        data_type_service: DataTypeService,
        account_service_repo: AppUserRepositoryContract,
        requester: AuthenticatedUser,
    ):
        """Initialize service with component, release, and account dependencies.

        Args:
            core_component_repository: Core-component repository dependency.
            release_service: Release service dependency.
            account_service_repo: Account repository used to resolve user summaries.
            requester: Requesting user context.
        """
        self._repo = core_component_repository
        self._release_service = release_service
        self._data_type_service = data_type_service
        self._account_service_repo = account_service_repo
        self._requester = requester

    async def create_acc(
        self,
        *,
        release_id: ReleaseId,
        based_acc_manifest_id: AccManifestId | None = None,
        object_class_term: str,
        component_type: OagisComponentType = "Semantics",
        definition: str | None = None,
        definition_source: str | None = None,
        is_abstract: bool | None = None,
        namespace_id: NamespaceId | None = None,
        tag_id: list[int] | None = None,
    ) -> CreateAccServiceResult:
        """Create an ACC in a release allowed for the requester's role."""
        logger.info(
            "create acc release_id=%d object_class_term=%r",
            int(release_id),
            object_class_term,
        )
        effective_release_id, release = await self._resolve_create_acc_release_id(release_id=release_id)
        self._assert_can_create_core_components(release_num=release.release_num)

        object_class_term = str(object_class_term).strip()
        if not object_class_term:
            raise ValueError("Object class term is required. Please provide a non-empty value.")
        if len(object_class_term) > 100:
            raise ValueError(
                "Object class term cannot exceed 100 characters. Please shorten it and try again."
            )

        if component_type not in OAGIS_COMPONENT_TYPE_VALUES:
            allowed = ", ".join(OAGIS_COMPONENT_TYPE_VALUES)
            raise ValueError(
                f"Invalid component_type: {component_type}. Allowed values are: {allowed}."
            )

        normalized_definition = definition.strip() if isinstance(definition, str) else None
        normalized_definition_source = definition_source.strip() if isinstance(definition_source, str) else None
        if normalized_definition_source is not None and len(normalized_definition_source) > 100:
            raise ValueError(
                "Definition source cannot exceed 100 characters. Please shorten it and try again."
            )
        normalized_tag_ids = self._normalize_tag_ids(tag_id)

        if based_acc_manifest_id is not None:
            based_acc = await self.get_acc(based_acc_manifest_id)
            if based_acc is None:
                raise LookupError(
                    f"No ACC exists with manifest ID {int(based_acc_manifest_id)}. Please verify the identifier and try again."
                )
            await self._assert_reference_release_allowed(
                target_release_id=effective_release_id,
                target_library_id=release.library.library_id,
                referenced_release_id=based_acc.release.release_id,
                referenced_library_id=based_acc.library.library_id,
                referenced_label="The base ACC",
                target_label="the target release",
                selection_phrase="a base ACC",
            )

        acc_manifest_id = await self._repo.create_acc(
            release_id=effective_release_id,
            based_acc_manifest_id=based_acc_manifest_id,
            object_class_term=object_class_term,
            oagis_component_type=OAGIS_COMPONENT_TYPE_VALUES[component_type],
            acc_type=self._derive_acc_type(component_type),
            definition=normalized_definition,
            definition_source=normalized_definition_source,
            is_abstract=bool(is_abstract) if is_abstract is not None else False,
            namespace_id=namespace_id,
            tag_id=normalized_tag_ids,
            requester_user_id=self._requester.user.user_id,
        )
        logger.info("create acc release_id=%d → acc_manifest_id=%d", int(effective_release_id), int(acc_manifest_id))
        return CreateAccServiceResult(acc_manifest_id=int(acc_manifest_id))

    async def create_asccp(
        self,
        *,
        release_id: ReleaseId,
        role_of_acc_manifest_id: AccManifestId,
        property_term: str,
        reusable_indicator: bool = True,
        namespace_id: NamespaceId | None = None,
        definition: str | None = None,
        definition_source: str | None = None,
        tag_id: list[int] | None = None,
    ) -> CreateAsccpServiceResult:
        """Create an ASCCP in a release allowed for the requester's role."""
        release = await self._release_service.get(release_id)
        if release is None:
            raise LookupError(f"No release exists with ID {int(release_id)}. Please verify the identifier and try again.")
        self._assert_release_is_published(release)
        self._assert_can_create_core_components(release_num=release.release_num)

        role_of_acc = await self.get_acc(role_of_acc_manifest_id)
        if role_of_acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(role_of_acc_manifest_id)}. Please verify the identifier and try again."
            )
        await self._assert_reference_release_allowed(
            target_release_id=release_id,
            target_library_id=release.library.library_id,
            referenced_release_id=role_of_acc.release.release_id,
            referenced_library_id=role_of_acc.library.library_id,
            referenced_label="The role ACC",
            target_label="the target release",
            selection_phrase="a role ACC",
        )
        if role_of_acc.is_abstract:
            raise ValueError("An abstract ACC cannot be used to create a new ASCCP.")

        normalized_property_term = property_term.strip()
        if not normalized_property_term:
            raise ValueError("Property term cannot be empty. Please provide a non-empty value and try again.")
        normalized_definition = definition.strip() if isinstance(definition, str) else None
        normalized_definition_source = definition_source.strip() if isinstance(definition_source, str) else None
        normalized_tag_ids = self._normalize_tag_ids(tag_id)
        derived_asccp_type = self._derive_asccp_type_for_role_acc(role_of_acc.component_type)

        asccp_manifest_id = await self._repo.create_asccp(
            release_id=release_id,
            role_of_acc_manifest_id=role_of_acc_manifest_id,
            property_term=normalized_property_term,
            asccp_type=derived_asccp_type,
            reusable_indicator=bool(reusable_indicator),
            namespace_id=namespace_id,
            definition=normalized_definition,
            definition_source=normalized_definition_source,
            requester_user_id=self._requester.user.user_id,
        )
        if normalized_tag_ids:
            await self._repo.add_asccp_tags(
                asccp_manifest_id=asccp_manifest_id,
                tag_id=normalized_tag_ids,
                requester_user_id=self._requester.user.user_id,
            )
        return CreateAsccpServiceResult(asccp_manifest_id=int(asccp_manifest_id))

    async def create_bccp(
        self,
        *,
        release_id: ReleaseId,
        bdt_manifest_id: DataTypeManifestId,
        property_term: str,
        definition: str | None = None,
        definition_source: str | None = None,
        deprecated: bool | None = None,
        is_nillable: bool | None = None,
        namespace_id: NamespaceId | None = None,
        default_value: str | None = None,
        fixed_value: str | None = None,
        tag_id: list[int] | None = None,
    ) -> CreateBccpServiceResult:
        """Create a BCCP in a release allowed for the requester's role."""
        release = await self._release_service.get(release_id)
        if release is None:
            raise LookupError(f"No release exists with ID {int(release_id)}. Please verify the identifier and try again.")
        self._assert_release_is_published(release)
        self._assert_can_create_core_components(release_num=release.release_num)

        bdt = await self._data_type_service.get(bdt_manifest_id)
        if bdt is None:
            raise LookupError(
                f"No data type exists with manifest ID {int(bdt_manifest_id)}. Please verify the identifier and try again."
            )
        await self._assert_reference_release_allowed(
            target_release_id=release_id,
            target_library_id=release.library.library_id,
            referenced_release_id=bdt.release.release_id,
            referenced_library_id=bdt.library.library_id,
            referenced_label="The target BDT",
            target_label="the target release",
            selection_phrase="a BDT",
        )

        normalized_property_term = property_term.strip()
        if not normalized_property_term:
            raise ValueError("Property term cannot be empty. Please provide a non-empty value and try again.")
        normalized_definition = definition.strip() if isinstance(definition, str) else None
        normalized_definition_source = definition_source.strip() if isinstance(definition_source, str) else None
        normalized_default_value = default_value.strip() if isinstance(default_value, str) else None
        normalized_fixed_value = fixed_value.strip() if isinstance(fixed_value, str) else None
        if normalized_default_value is not None and normalized_fixed_value is not None:
            raise ValueError("Provide only one of `default_value` or `fixed_value`.")
        normalized_tag_ids = self._normalize_tag_ids(tag_id)
        bccp_manifest_id = await self._repo.create_bccp(
            release_id=release_id,
            bdt_manifest_id=bdt_manifest_id,
            property_term=normalized_property_term,
            definition=normalized_definition,
            definition_source=normalized_definition_source,
            deprecated=bool(deprecated) if deprecated is not None else False,
            is_nillable=bool(is_nillable) if is_nillable is not None else False,
            namespace_id=namespace_id,
            default_value=normalized_default_value,
            fixed_value=normalized_fixed_value,
            requester_user_id=self._requester.user.user_id,
        )
        if normalized_tag_ids:
            await self._repo.add_bccp_tags(
                bccp_manifest_id=bccp_manifest_id,
                tag_id=normalized_tag_ids,
                requester_user_id=self._requester.user.user_id,
            )
        return CreateBccpServiceResult(bccp_manifest_id=int(bccp_manifest_id))

    async def _assert_reference_release_allowed(
        self,
        *,
        target_release_id: ReleaseId | int,
        target_library_id: int,
        referenced_release_id: ReleaseId | int,
        referenced_library_id: int,
        referenced_label: str,
        target_label: str,
        selection_phrase: str,
    ) -> None:
        normalized_target_release_id = int(target_release_id)
        normalized_referenced_release_id = int(referenced_release_id)

        if int(referenced_library_id) == int(target_library_id):
            if normalized_referenced_release_id != normalized_target_release_id:
                raise ValueError(
                    f"{referenced_label} must belong to the same release as {target_label} when both components are in the same library. "
                    f"Please choose {selection_phrase} from the target release and try again."
                )
            return

        direct_dependency_release_ids = {
            int(dependent_release_id)
            for dependent_release_id in await self._release_service.get_release_dependency_ids(
                ReleaseId(normalized_target_release_id)
            )
        }
        if normalized_referenced_release_id not in direct_dependency_release_ids:
            raise ValueError(
                f"{referenced_label} must come from a release dependency of {target_label} when the components are in different libraries. "
                f"Please choose {selection_phrase} from one of the target release dependencies and try again."
            )

    async def update_acc(
        self,
        *,
        acc_manifest_id: AccManifestId,
        object_class_term: str | None | UnsetType = UNSET,
        component_type: OagisComponentType | UnsetType = UNSET,
        definition: str | None | UnsetType = UNSET,
        definition_source: str | None | UnsetType = UNSET,
        is_abstract: bool | UnsetType = UNSET,
        deprecated: bool | UnsetType = UNSET,
        based_acc_manifest_id: AccManifestId | None | UnsetType = UNSET,
        namespace_id: NamespaceId | None | UnsetType = UNSET,
    ) -> UpdateAccServiceResult:
        """Update mutable ACC fields when the requester owns a WIP ACC."""
        logger.info("update acc id=%d", int(acc_manifest_id))
        acc = await self.get_acc(acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_wip_core_component(acc, label="ACC")

        normalized_object_class_term = object_class_term
        if object_class_term is not UNSET:
            if object_class_term is None or not str(object_class_term).strip():
                raise ValueError(
                    "Object class term cannot be empty. Please provide a non-empty value and try again."
                )
            normalized_object_class_term = str(object_class_term).strip()
            if len(normalized_object_class_term) > 100:
                raise ValueError(
                    "Object class term cannot exceed 100 characters. Please shorten it and try again."
                )

        if component_type is not UNSET and component_type not in OAGIS_COMPONENT_TYPE_VALUES:
            allowed = ", ".join(OAGIS_COMPONENT_TYPE_VALUES)
            raise ValueError(
                f"Invalid component_type: {component_type}. Allowed values are: {allowed}."
            )

        normalized_definition = definition
        if definition is not UNSET and isinstance(definition, str):
            normalized_definition = definition.strip() or None

        normalized_definition_source = definition_source
        if definition_source is not UNSET and isinstance(definition_source, str):
            normalized_definition_source = definition_source.strip() or None
        if normalized_definition_source is not UNSET and normalized_definition_source is not None:
            if len(str(normalized_definition_source)) > 100:
                raise ValueError(
                    "Definition source cannot exceed 100 characters. Please shorten it and try again."
                )

        if namespace_id is not UNSET and namespace_id is not None and int(namespace_id) <= 0:
            raise ValueError("namespace_id must be a positive integer when provided.")

        current_component_type = (
            OAGIS_COMPONENT_TYPE_NAMES.get(int(acc.component_type))
            if acc.component_type is not None
            else None
        )
        current_namespace_id = int(acc.namespace.namespace_id) if acc.namespace is not None else None

        updates: list[str] = []
        if normalized_object_class_term is not UNSET and normalized_object_class_term != acc.object_class_term:
            updates.append("object_class_term")
        if component_type is not UNSET and component_type != current_component_type:
            updates.append("component_type")
        if normalized_definition is not UNSET and normalized_definition != acc.definition:
            updates.append("definition")
        if normalized_definition_source is not UNSET and normalized_definition_source != acc.definition_source:
            updates.append("definition_source")
        if is_abstract is not UNSET and bool(is_abstract) != bool(acc.is_abstract):
            updates.append("is_abstract")
        if deprecated is not UNSET and bool(deprecated) != bool(acc.is_deprecated):
            updates.append("deprecated")
        if namespace_id is not UNSET and (
            None if namespace_id is None else int(namespace_id)
        ) != current_namespace_id:
            updates.append("namespace_id")
        base_changed = based_acc_manifest_id is not UNSET and based_acc_manifest_id != self._acc_base_manifest_id(acc)
        if base_changed:
            updates.append("based_acc_manifest_id")

        if not updates:
            logger.info("update acc id=%d → no changes", int(acc_manifest_id))
            return UpdateAccServiceResult(acc_manifest_id=int(acc_manifest_id), updates=[])

        scalar_updates = [update for update in updates if update != "based_acc_manifest_id"]
        if scalar_updates:
            await self._repo.update_acc(
                acc_manifest_id=acc_manifest_id,
                object_class_term=None if normalized_object_class_term is UNSET else normalized_object_class_term,
                object_class_term_set=normalized_object_class_term is not UNSET,
                oagis_component_type=None
                if component_type is UNSET
                else OAGIS_COMPONENT_TYPE_VALUES[component_type],
                oagis_component_type_set=component_type is not UNSET,
                acc_type=None if component_type is UNSET else self._derive_acc_type(component_type),
                acc_type_set=component_type is not UNSET,
                definition=None if normalized_definition is UNSET else normalized_definition,
                definition_set=normalized_definition is not UNSET,
                definition_source=None
                if normalized_definition_source is UNSET
                else normalized_definition_source,
                definition_source_set=normalized_definition_source is not UNSET,
                is_abstract=None if is_abstract is UNSET else bool(is_abstract),
                is_abstract_set=is_abstract is not UNSET,
                deprecated=None if deprecated is UNSET else bool(deprecated),
                deprecated_set=deprecated is not UNSET,
                namespace_id=None if namespace_id in {UNSET, None} else namespace_id,
                namespace_id_set=namespace_id is not UNSET,
                requester_user_id=self._requester.user.user_id,
            )
        if base_changed:
            await self.update_acc_base(
                acc_manifest_id=acc_manifest_id,
                based_acc_manifest_id=based_acc_manifest_id,
            )
        logger.info("update acc id=%d → %s", int(acc_manifest_id), sorted(updates))
        return UpdateAccServiceResult(acc_manifest_id=int(acc_manifest_id), updates=sorted(updates))

    async def update_asccp(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        role_of_acc_manifest_id: AccManifestId | UnsetType = UNSET,
        property_term: str | None | UnsetType = UNSET,
        definition: str | None | UnsetType = UNSET,
        definition_source: str | None | UnsetType = UNSET,
        reusable_indicator: bool | UnsetType = UNSET,
        deprecated: bool | UnsetType = UNSET,
        is_nillable: bool | UnsetType = UNSET,
        namespace_id: NamespaceId | None | UnsetType = UNSET,
        allow_warnings: bool = False,
    ) -> UpdateAsccpServiceResult:
        """Update mutable ASCCP fields when the requester owns a WIP ASCCP."""
        asccp = await self.get_asccp(asccp_manifest_id)
        if asccp is None:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_wip_core_component(asccp, label="ASCCP")

        normalized_role_of_acc_manifest_id = role_of_acc_manifest_id
        if role_of_acc_manifest_id is not UNSET:
            role_of_acc = await self.get_acc(role_of_acc_manifest_id)
            if role_of_acc is None:
                raise LookupError(
                    f"No ACC exists with manifest ID {int(role_of_acc_manifest_id)}. Please verify the identifier and try again."
                )
            await self._assert_reference_release_allowed(
                target_release_id=asccp.release.release_id,
                target_library_id=asccp.library.library_id,
                referenced_release_id=role_of_acc.release.release_id,
                referenced_library_id=role_of_acc.library.library_id,
                referenced_label="The role ACC",
                target_label="the target ASCCP",
                selection_phrase="a role ACC",
            )
            if role_of_acc.is_abstract:
                raise ValueError("An abstract ACC cannot be used as the role ACC of an ASCCP.")

        normalized_property_term = property_term
        if property_term is not UNSET:
            if property_term is None or not str(property_term).strip():
                raise ValueError("Property term cannot be empty. Please provide a non-empty value and try again.")
            normalized_property_term = str(property_term).strip()

        normalized_definition = definition
        if definition is not UNSET and isinstance(definition, str):
            normalized_definition = definition.strip() or None
        normalized_definition_source = definition_source
        if definition_source is not UNSET and isinstance(definition_source, str):
            normalized_definition_source = definition_source.strip() or None

        current_namespace_id = int(asccp.namespace.namespace_id) if asccp.namespace is not None else None
        updates: list[str] = []
        current_role_of_acc_manifest_id = (
            int(asccp.role_of_acc.acc_manifest_id) if asccp.role_of_acc is not None else None
        )
        if (
            normalized_role_of_acc_manifest_id is not UNSET
            and int(normalized_role_of_acc_manifest_id) != current_role_of_acc_manifest_id
        ):
            updates.append("role_of_acc_manifest_id")
        if normalized_property_term is not UNSET and normalized_property_term != asccp.property_term:
            updates.append("property_term")
        if normalized_definition is not UNSET and normalized_definition != asccp.definition:
            updates.append("definition")
        if normalized_definition_source is not UNSET and normalized_definition_source != asccp.definition_source:
            updates.append("definition_source")
        if reusable_indicator is not UNSET and bool(reusable_indicator) != bool(asccp.reusable_indicator):
            updates.append("reusable_indicator")
        if deprecated is not UNSET and bool(deprecated) != bool(asccp.is_deprecated):
            updates.append("deprecated")
        if is_nillable is not UNSET and bool(is_nillable) != bool(asccp.is_nillable):
            updates.append("is_nillable")
        if namespace_id is not UNSET and (None if namespace_id is None else int(namespace_id)) != current_namespace_id:
            updates.append("namespace_id")
        if not updates:
            return UpdateAsccpServiceResult(asccp_manifest_id=int(asccp_manifest_id), updates=[])

        warnings = await self.get_update_asccp_warnings(
            asccp_manifest_id=asccp_manifest_id,
            role_of_acc_manifest_id=normalized_role_of_acc_manifest_id,
            property_term=normalized_property_term,
        )
        self._raise_for_structural_warnings(warnings=warnings, allow_warnings=allow_warnings)

        await self._repo.update_asccp(
            asccp_manifest_id=asccp_manifest_id,
            role_of_acc_manifest_id=None if normalized_role_of_acc_manifest_id is UNSET else normalized_role_of_acc_manifest_id,
            role_of_acc_manifest_id_set=normalized_role_of_acc_manifest_id is not UNSET,
            property_term=None if normalized_property_term is UNSET else normalized_property_term,
            property_term_set=normalized_property_term is not UNSET,
            reusable_indicator=None if reusable_indicator is UNSET else bool(reusable_indicator),
            reusable_indicator_set=reusable_indicator is not UNSET,
            deprecated=None if deprecated is UNSET else bool(deprecated),
            deprecated_set=deprecated is not UNSET,
            is_nillable=None if is_nillable is UNSET else bool(is_nillable),
            is_nillable_set=is_nillable is not UNSET,
            namespace_id=None if namespace_id in {UNSET, None} else namespace_id,
            namespace_id_set=namespace_id is not UNSET,
            definition=None if normalized_definition is UNSET else normalized_definition,
            definition_set=normalized_definition is not UNSET,
            definition_source=None if normalized_definition_source is UNSET else normalized_definition_source,
            definition_source_set=normalized_definition_source is not UNSET,
            requester_user_id=self._requester.user.user_id,
        )
        return UpdateAsccpServiceResult(asccp_manifest_id=int(asccp_manifest_id), updates=sorted(updates))

    async def update_bccp(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        bdt_manifest_id: DataTypeManifestId | UnsetType = UNSET,
        property_term: str | None | UnsetType = UNSET,
        definition: str | None | UnsetType = UNSET,
        definition_source: str | None | UnsetType = UNSET,
        deprecated: bool | UnsetType = UNSET,
        is_nillable: bool | UnsetType = UNSET,
        namespace_id: NamespaceId | None | UnsetType = UNSET,
        default_value: str | None | UnsetType = UNSET,
        fixed_value: str | None | UnsetType = UNSET,
        allow_warnings: bool = False,
    ) -> UpdateBccpServiceResult:
        """Update mutable BCCP fields when the requester owns a WIP BCCP."""
        bccp = await self.get_bccp(bccp_manifest_id)
        if bccp is None:
            raise LookupError(
                f"No BCCP exists with manifest ID {int(bccp_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_wip_core_component(bccp, label="BCCP")

        normalized_bdt_manifest_id = bdt_manifest_id
        if bdt_manifest_id is not UNSET:
            bdt = await self._data_type_service.get(bdt_manifest_id)
            if bdt is None:
                raise LookupError(
                    f"No data type exists with manifest ID {int(bdt_manifest_id)}. Please verify the identifier and try again."
                )
            await self._assert_reference_release_allowed(
                target_release_id=bccp.release.release_id,
                target_library_id=bccp.library.library_id,
                referenced_release_id=bdt.release.release_id,
                referenced_library_id=bdt.library.library_id,
                referenced_label="The target BDT",
                target_label="the target BCCP",
                selection_phrase="a BDT",
            )

        normalized_property_term = property_term
        if property_term is not UNSET:
            if property_term is None or not str(property_term).strip():
                raise ValueError("Property term cannot be empty. Please provide a non-empty value and try again.")
            normalized_property_term = str(property_term).strip()
        normalized_definition = definition
        if definition is not UNSET and isinstance(definition, str):
            normalized_definition = definition.strip() or None
        normalized_definition_source = definition_source
        if definition_source is not UNSET and isinstance(definition_source, str):
            normalized_definition_source = definition_source.strip() or None
        normalized_default_value = default_value
        if default_value is not UNSET and isinstance(default_value, str):
            normalized_default_value = default_value.strip() or None
        normalized_fixed_value = fixed_value
        if fixed_value is not UNSET and isinstance(fixed_value, str):
            normalized_fixed_value = fixed_value.strip() or None
        if normalized_default_value not in {UNSET, None} and normalized_fixed_value not in {UNSET, None}:
            raise ValueError("Provide only one of `default_value` or `fixed_value`.")

        current_namespace_id = int(bccp.namespace.namespace_id) if bccp.namespace is not None else None
        current_default_value = bccp.value_constraint.default_value if bccp.value_constraint is not None else None
        current_fixed_value = bccp.value_constraint.fixed_value if bccp.value_constraint is not None else None

        updates: list[str] = []
        if normalized_bdt_manifest_id is not UNSET and int(normalized_bdt_manifest_id) != int(bccp.bdt.dt_manifest_id):
            updates.append("bdt_manifest_id")
        if normalized_property_term is not UNSET and normalized_property_term != bccp.property_term:
            updates.append("property_term")
        if normalized_definition is not UNSET and normalized_definition != bccp.definition:
            updates.append("definition")
        if normalized_definition_source is not UNSET and normalized_definition_source != bccp.definition_source:
            updates.append("definition_source")
        if deprecated is not UNSET and bool(deprecated) != bool(bccp.is_deprecated):
            updates.append("deprecated")
        if is_nillable is not UNSET and bool(is_nillable) != bool(bccp.is_nillable):
            updates.append("is_nillable")
        if namespace_id is not UNSET and (None if namespace_id is None else int(namespace_id)) != current_namespace_id:
            updates.append("namespace_id")
        if normalized_default_value is not UNSET and normalized_default_value != current_default_value:
            updates.append("default_value")
        if normalized_fixed_value is not UNSET and normalized_fixed_value != current_fixed_value:
            updates.append("fixed_value")
        if not updates:
            return UpdateBccpServiceResult(bccp_manifest_id=int(bccp_manifest_id), updates=[])

        warnings = await self.get_update_bccp_warnings(
            bccp_manifest_id=bccp_manifest_id,
            property_term=normalized_property_term,
        )
        self._raise_for_structural_warnings(warnings=warnings, allow_warnings=allow_warnings)

        await self._repo.update_bccp(
            bccp_manifest_id=bccp_manifest_id,
            bdt_manifest_id=None if normalized_bdt_manifest_id is UNSET else normalized_bdt_manifest_id,
            bdt_manifest_id_set=normalized_bdt_manifest_id is not UNSET,
            property_term=None if normalized_property_term is UNSET else normalized_property_term,
            property_term_set=normalized_property_term is not UNSET,
            deprecated=None if deprecated is UNSET else bool(deprecated),
            deprecated_set=deprecated is not UNSET,
            is_nillable=None if is_nillable is UNSET else bool(is_nillable),
            is_nillable_set=is_nillable is not UNSET,
            namespace_id=None if namespace_id in {UNSET, None} else namespace_id,
            namespace_id_set=namespace_id is not UNSET,
            default_value=None if normalized_default_value is UNSET else normalized_default_value,
            default_value_set=normalized_default_value is not UNSET,
            fixed_value=None if normalized_fixed_value is UNSET else normalized_fixed_value,
            fixed_value_set=normalized_fixed_value is not UNSET,
            definition=None if normalized_definition is UNSET else normalized_definition,
            definition_set=normalized_definition is not UNSET,
            definition_source=None if normalized_definition_source is UNSET else normalized_definition_source,
            definition_source_set=normalized_definition_source is not UNSET,
            requester_user_id=self._requester.user.user_id,
        )
        return UpdateBccpServiceResult(bccp_manifest_id=int(bccp_manifest_id), updates=sorted(updates))

    async def transfer_acc_ownership(
        self,
        *,
        acc_manifest_id: AccManifestId,
        target_user_id: int,
    ) -> TransferAccOwnershipServiceResult:
        """Transfer ACC ownership to another user while the ACC is in `WIP`."""
        logger.info("transfer acc ownership id=%d → user_id=%d", int(acc_manifest_id), int(target_user_id))
        acc = await self.get_acc(acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_transfer_wip_component(acc, label="ACC")
        if int(target_user_id) == int(acc.owner.user_id):
            logger.info("transfer acc ownership id=%d → no-op (same owner)", int(acc_manifest_id))
            return TransferAccOwnershipServiceResult(acc_manifest_id=int(acc_manifest_id), updates=[])
        transferred = await self._repo.transfer_acc_ownership(
            acc_manifest_id=acc_manifest_id,
            requester_user_id=self._requester_user_id,
            target_user_id=int(target_user_id),
        )
        if not transferred:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        logger.info("transfer acc ownership id=%d → transferred to user_id=%d", int(acc_manifest_id), int(target_user_id))
        return TransferAccOwnershipServiceResult(acc_manifest_id=int(acc_manifest_id), updates=["owner_user_id"])

    async def transfer_asccp_ownership(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        target_user_id: int,
    ) -> TransferAsccpOwnershipServiceResult:
        """Transfer ASCCP ownership to another user while the ASCCP is in `WIP`."""
        logger.info("transfer asccp ownership id=%d → user_id=%d", int(asccp_manifest_id), int(target_user_id))
        asccp = await self.get_asccp(asccp_manifest_id)
        if asccp is None:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_transfer_wip_component(asccp, label="ASCCP")
        if int(target_user_id) == int(asccp.owner.user_id):
            logger.info("transfer asccp ownership id=%d → no-op (same owner)", int(asccp_manifest_id))
            return TransferAsccpOwnershipServiceResult(asccp_manifest_id=int(asccp_manifest_id), updates=[])
        transferred = await self._repo.transfer_asccp_ownership(
            asccp_manifest_id=asccp_manifest_id,
            requester_user_id=self._requester_user_id,
            target_user_id=int(target_user_id),
        )
        if not transferred:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )
        logger.info("transfer asccp ownership id=%d → transferred to user_id=%d", int(asccp_manifest_id), int(target_user_id))
        return TransferAsccpOwnershipServiceResult(asccp_manifest_id=int(asccp_manifest_id), updates=["owner_user_id"])

    async def transfer_bccp_ownership(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        target_user_id: int,
    ) -> TransferBccpOwnershipServiceResult:
        """Transfer BCCP ownership to another user while the BCCP is in `WIP`."""
        logger.info("transfer bccp ownership id=%d → user_id=%d", int(bccp_manifest_id), int(target_user_id))
        bccp = await self.get_bccp(bccp_manifest_id)
        if bccp is None:
            raise LookupError(
                f"No BCCP exists with manifest ID {int(bccp_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_transfer_wip_component(bccp, label="BCCP")
        if int(target_user_id) == int(bccp.owner.user_id):
            logger.info("transfer bccp ownership id=%d → no-op (same owner)", int(bccp_manifest_id))
            return TransferBccpOwnershipServiceResult(bccp_manifest_id=int(bccp_manifest_id), updates=[])
        transferred = await self._repo.transfer_bccp_ownership(
            bccp_manifest_id=bccp_manifest_id,
            requester_user_id=self._requester_user_id,
            target_user_id=int(target_user_id),
        )
        if not transferred:
            raise LookupError(
                f"No BCCP exists with manifest ID {int(bccp_manifest_id)}. Please verify the identifier and try again."
            )
        logger.info("transfer bccp ownership id=%d → transferred to user_id=%d", int(bccp_manifest_id), int(target_user_id))
        return TransferBccpOwnershipServiceResult(bccp_manifest_id=int(bccp_manifest_id), updates=["owner_user_id"])

    async def update_ascc(
        self,
        *,
        ascc_manifest_id: AsccManifestId,
        cardinality_min: int | UnsetType = UNSET,
        cardinality_max: int | UnsetType = UNSET,
        definition: str | None | UnsetType = UNSET,
        definition_source: str | None | UnsetType = UNSET,
        deprecated: bool | UnsetType = UNSET,
        index: int | UnsetType = UNSET,
        after_ascc_manifest_id: AsccManifestId | UnsetType = UNSET,
        after_bcc_manifest_id: BccManifestId | UnsetType = UNSET,
        before_ascc_manifest_id: AsccManifestId | UnsetType = UNSET,
        before_bcc_manifest_id: BccManifestId | UnsetType = UNSET,
    ) -> UpdateAsccServiceResult:
        """Update mutable ASCC fields when the requester owns the parent WIP ACC."""
        owner_acc_manifest_id = await self._repo.get_owner_acc_manifest_id_by_ascc_manifest(ascc_manifest_id)
        if owner_acc_manifest_id is None:
            raise LookupError(
                f"No ASCC exists with manifest ID {int(ascc_manifest_id)}. Please verify the identifier and try again."
            )
        acc = await self.get_acc(owner_acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(owner_acc_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_wip_core_component(acc, label="ACC")

        relationship = next(
            (
                relationship
                for relationship in acc.relationships
                if getattr(relationship, "component_type", None) == "ASCC"
                and int(relationship.ascc_manifest_id) == int(ascc_manifest_id)
            ),
            None,
        )
        if relationship is None:
            raise LookupError(
                f"No ASCC exists with manifest ID {int(ascc_manifest_id)} in the target ACC. Please verify the identifier and try again."
            )

        normalized_definition = definition
        if definition is not UNSET and isinstance(definition, str):
            normalized_definition = definition.strip() or None
        normalized_definition_source = definition_source
        if definition_source is not UNSET and isinstance(definition_source, str):
            normalized_definition_source = definition_source.strip() or None

        normalized_cardinality_min = self._normalize_optional_cardinality_min(cardinality_min)
        normalized_cardinality_max = self._normalize_optional_cardinality_max(cardinality_max)

        final_cardinality_min = int(relationship.cardinality_min) if normalized_cardinality_min is UNSET else int(normalized_cardinality_min)
        final_cardinality_max = int(relationship.cardinality_max) if normalized_cardinality_max is UNSET else int(normalized_cardinality_max)
        self._assert_valid_cardinality_range(
            cardinality_min=final_cardinality_min,
            cardinality_max=final_cardinality_max,
        )

        updates: list[str] = []
        if normalized_cardinality_min is not UNSET and final_cardinality_min != int(relationship.cardinality_min):
            updates.append("cardinality_min")
        if normalized_cardinality_max is not UNSET and final_cardinality_max != int(relationship.cardinality_max):
            updates.append("cardinality_max")
        if normalized_definition is not UNSET and normalized_definition != relationship.definition:
            updates.append("definition")
        if normalized_definition_source is not UNSET and normalized_definition_source != relationship.definition_source:
            updates.append("definition_source")
        if deprecated is not UNSET and bool(deprecated) != bool(relationship.is_deprecated):
            updates.append("deprecated")
        sequence_requested = any(
            value is not UNSET and value is not None
            for value in (
                index,
                after_ascc_manifest_id,
                after_bcc_manifest_id,
                before_ascc_manifest_id,
                before_bcc_manifest_id,
            )
        )
        if sequence_requested:
            updates.append("sequence")
        if not updates:
            return UpdateAsccServiceResult(ascc_manifest_id=int(ascc_manifest_id), updates=[])

        scalar_updates_requested = any(
            value is not UNSET
            for value in (
                normalized_cardinality_min,
                normalized_cardinality_max,
                normalized_definition,
                normalized_definition_source,
                deprecated,
            )
        )
        if scalar_updates_requested:
            await self._repo.update_ascc(
                ascc_manifest_id=ascc_manifest_id,
                cardinality_min=None if normalized_cardinality_min is UNSET else final_cardinality_min,
                cardinality_min_set=normalized_cardinality_min is not UNSET,
                cardinality_max=None if normalized_cardinality_max is UNSET else final_cardinality_max,
                cardinality_max_set=normalized_cardinality_max is not UNSET,
                deprecated=None if deprecated is UNSET else bool(deprecated),
                deprecated_set=deprecated is not UNSET,
                definition=None if normalized_definition is UNSET else normalized_definition,
                definition_set=normalized_definition is not UNSET,
                definition_source=None if normalized_definition_source is UNSET else normalized_definition_source,
                definition_source_set=normalized_definition_source is not UNSET,
                requester_user_id=self._requester.user.user_id,
            )
        if sequence_requested:
            resolved_after_ascc_manifest_id, resolved_after_bcc_manifest_id = self._resolve_move_after_reference(
                relationships=acc.relationships,
                item_component_type="ASCC",
                item_manifest_id=int(ascc_manifest_id),
                index=None if index in {UNSET, None} else int(index),
                after_ascc_manifest_id=None if after_ascc_manifest_id in {UNSET, None} else after_ascc_manifest_id,
                after_bcc_manifest_id=None if after_bcc_manifest_id in {UNSET, None} else after_bcc_manifest_id,
                before_ascc_manifest_id=None if before_ascc_manifest_id in {UNSET, None} else before_ascc_manifest_id,
                before_bcc_manifest_id=None if before_bcc_manifest_id in {UNSET, None} else before_bcc_manifest_id,
            )
            await self._repo.move_acc_sequence(
                acc_manifest_id=owner_acc_manifest_id,
                item_ascc_manifest_id=ascc_manifest_id,
                item_bcc_manifest_id=None,
                after_ascc_manifest_id=resolved_after_ascc_manifest_id,
                after_bcc_manifest_id=resolved_after_bcc_manifest_id,
                requester_user_id=self._requester.user.user_id,
            )
        return UpdateAsccServiceResult(ascc_manifest_id=int(ascc_manifest_id), updates=sorted(set(updates)))

    async def update_bcc(
        self,
        *,
        bcc_manifest_id: BccManifestId,
        entity_type: Literal["Attribute", "Element"] | UnsetType = UNSET,
        cardinality_min: int | UnsetType = UNSET,
        cardinality_max: int | UnsetType = UNSET,
        definition: str | None | UnsetType = UNSET,
        definition_source: str | None | UnsetType = UNSET,
        deprecated: bool | UnsetType = UNSET,
        is_nillable: bool | UnsetType = UNSET,
        index: int | UnsetType = UNSET,
        after_ascc_manifest_id: AsccManifestId | UnsetType = UNSET,
        after_bcc_manifest_id: BccManifestId | UnsetType = UNSET,
        before_ascc_manifest_id: AsccManifestId | UnsetType = UNSET,
        before_bcc_manifest_id: BccManifestId | UnsetType = UNSET,
        default_value: str | None | UnsetType = UNSET,
        fixed_value: str | None | UnsetType = UNSET,
    ) -> UpdateBccServiceResult:
        """Update mutable BCC fields when the requester owns the parent WIP ACC."""
        owner_acc_manifest_id = await self._repo.get_owner_acc_manifest_id_by_bcc_manifest(bcc_manifest_id)
        if owner_acc_manifest_id is None:
            raise LookupError(
                f"No BCC exists with manifest ID {int(bcc_manifest_id)}. Please verify the identifier and try again."
            )
        acc = await self.get_acc(owner_acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(owner_acc_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_wip_core_component(acc, label="ACC")

        relationship = next(
            (
                relationship
                for relationship in acc.relationships
                if getattr(relationship, "component_type", None) == "BCC"
                and int(relationship.bcc_manifest_id) == int(bcc_manifest_id)
            ),
            None,
        )
        if relationship is None:
            raise LookupError(
                f"No BCC exists with manifest ID {int(bcc_manifest_id)} in the target ACC. Please verify the identifier and try again."
            )

        normalized_definition = definition
        if definition is not UNSET and isinstance(definition, str):
            normalized_definition = definition.strip() or None
        normalized_definition_source = definition_source
        if definition_source is not UNSET and isinstance(definition_source, str):
            normalized_definition_source = definition_source.strip() or None
        normalized_default_value = default_value
        if default_value is not UNSET and isinstance(default_value, str):
            normalized_default_value = default_value.strip() or None
        normalized_fixed_value = fixed_value
        if fixed_value is not UNSET and isinstance(fixed_value, str):
            normalized_fixed_value = fixed_value.strip() or None
        if normalized_default_value not in {UNSET, None} and normalized_fixed_value not in {UNSET, None}:
            raise ValueError("Provide only one of `default_value` or `fixed_value`.")

        current_entity_type = self._normalize_bcc_entity_type(relationship.entity_type or "Element")
        normalized_entity_type = self._normalize_bcc_entity_type(entity_type)

        normalized_cardinality_min = self._normalize_optional_cardinality_min(cardinality_min)
        normalized_cardinality_max = self._normalize_optional_cardinality_max(cardinality_max)

        final_entity_type = current_entity_type if normalized_entity_type is UNSET else normalized_entity_type

        if final_entity_type == "Attribute":
            await self._assert_attribute_bcc_allowed_for_bdt(
                bdt_manifest_id=int(relationship.to_bccp.bdt_manifest.dt_manifest_id),
            )
            transitioning_to_attribute = current_entity_type != "Attribute"
            default_cardinality_min = 0 if transitioning_to_attribute else int(relationship.cardinality_min)
            default_cardinality_max = 1 if transitioning_to_attribute else int(relationship.cardinality_max)
            final_cardinality_min = default_cardinality_min if normalized_cardinality_min is UNSET else int(normalized_cardinality_min)
            if transitioning_to_attribute and (
                normalized_cardinality_max is UNSET
                or int(normalized_cardinality_max) == -1
                or int(normalized_cardinality_max) > 1
            ):
                final_cardinality_max = 1
            else:
                final_cardinality_max = default_cardinality_max if normalized_cardinality_max is UNSET else int(normalized_cardinality_max)
            self._assert_valid_attribute_bcc_cardinality(
                cardinality_min=final_cardinality_min,
                cardinality_max=final_cardinality_max,
            )
        else:
            final_cardinality_min = int(relationship.cardinality_min) if normalized_cardinality_min is UNSET else int(normalized_cardinality_min)
            final_cardinality_max = int(relationship.cardinality_max) if normalized_cardinality_max is UNSET else int(normalized_cardinality_max)

        self._assert_valid_cardinality_range(
            cardinality_min=final_cardinality_min,
            cardinality_max=final_cardinality_max,
        )

        current_default_value = relationship.value_constraint.default_value if relationship.value_constraint is not None else None
        current_fixed_value = relationship.value_constraint.fixed_value if relationship.value_constraint is not None else None

        updates: list[str] = []
        if normalized_entity_type is not UNSET and normalized_entity_type != current_entity_type:
            updates.append("entity_type")
        if final_cardinality_min != int(relationship.cardinality_min):
            updates.append("cardinality_min")
        if final_cardinality_max != int(relationship.cardinality_max):
            updates.append("cardinality_max")
        if normalized_definition is not UNSET and normalized_definition != relationship.definition:
            updates.append("definition")
        if normalized_definition_source is not UNSET and normalized_definition_source != relationship.definition_source:
            updates.append("definition_source")
        if deprecated is not UNSET and bool(deprecated) != bool(relationship.is_deprecated):
            updates.append("deprecated")
        if is_nillable is not UNSET and bool(is_nillable) != bool(relationship.is_nillable):
            updates.append("is_nillable")
        value_constraint_changed = False
        if normalized_default_value is not UNSET and normalized_default_value != current_default_value:
            value_constraint_changed = True
        if normalized_fixed_value is not UNSET and normalized_fixed_value != current_fixed_value:
            value_constraint_changed = True
        if value_constraint_changed:
            updates.append("value_constraint")
        sequence_requested = any(
            value is not UNSET and value is not None
            for value in (
                index,
                after_ascc_manifest_id,
                after_bcc_manifest_id,
                before_ascc_manifest_id,
                before_bcc_manifest_id,
            )
        )
        if sequence_requested:
            updates.append("sequence")
        if not updates:
            return UpdateBccServiceResult(bcc_manifest_id=int(bcc_manifest_id), updates=[])

        scalar_updates_requested = any(
            value is not UNSET
            for value in (
                normalized_entity_type,
                normalized_cardinality_min,
                normalized_cardinality_max,
                normalized_definition,
                normalized_definition_source,
                deprecated,
                is_nillable,
                normalized_default_value,
                normalized_fixed_value,
            )
        )
        if scalar_updates_requested:
            await self._repo.update_bcc(
                bcc_manifest_id=bcc_manifest_id,
                entity_type=None if normalized_entity_type is UNSET else normalized_entity_type,
                entity_type_set=normalized_entity_type is not UNSET,
                cardinality_min=final_cardinality_min,
                cardinality_min_set=(
                    normalized_cardinality_min is not UNSET
                    or (normalized_entity_type is not UNSET and normalized_entity_type == "Attribute" and current_entity_type != "Attribute")
                ),
                cardinality_max=final_cardinality_max,
                cardinality_max_set=(
                    normalized_cardinality_max is not UNSET
                    or (normalized_entity_type is not UNSET and normalized_entity_type == "Attribute" and current_entity_type != "Attribute")
                ),
                deprecated=None if deprecated is UNSET else bool(deprecated),
                deprecated_set=deprecated is not UNSET,
                is_nillable=None if is_nillable is UNSET else bool(is_nillable),
                is_nillable_set=is_nillable is not UNSET,
                default_value=None if normalized_default_value is UNSET else normalized_default_value,
                default_value_set=normalized_default_value is not UNSET,
                fixed_value=None if normalized_fixed_value is UNSET else normalized_fixed_value,
                fixed_value_set=normalized_fixed_value is not UNSET,
                definition=None if normalized_definition is UNSET else normalized_definition,
                definition_set=normalized_definition is not UNSET,
                definition_source=None if normalized_definition_source is UNSET else normalized_definition_source,
                definition_source_set=normalized_definition_source is not UNSET,
                requester_user_id=self._requester.user.user_id,
            )
        if sequence_requested:
            resolved_after_ascc_manifest_id, resolved_after_bcc_manifest_id = self._resolve_move_after_reference(
                relationships=acc.relationships,
                item_component_type="BCC",
                item_manifest_id=int(bcc_manifest_id),
                index=None if index in {UNSET, None} else int(index),
                after_ascc_manifest_id=None if after_ascc_manifest_id in {UNSET, None} else after_ascc_manifest_id,
                after_bcc_manifest_id=None if after_bcc_manifest_id in {UNSET, None} else after_bcc_manifest_id,
                before_ascc_manifest_id=None if before_ascc_manifest_id in {UNSET, None} else before_ascc_manifest_id,
                before_bcc_manifest_id=None if before_bcc_manifest_id in {UNSET, None} else before_bcc_manifest_id,
            )
            await self._repo.move_acc_sequence(
                acc_manifest_id=owner_acc_manifest_id,
                item_ascc_manifest_id=None,
                item_bcc_manifest_id=bcc_manifest_id,
                after_ascc_manifest_id=resolved_after_ascc_manifest_id,
                after_bcc_manifest_id=resolved_after_bcc_manifest_id,
                requester_user_id=self._requester.user.user_id,
            )
        return UpdateBccServiceResult(bcc_manifest_id=int(bcc_manifest_id), updates=sorted(set(updates)))

    async def update_acc_base(
        self,
        *,
        acc_manifest_id: AccManifestId,
        based_acc_manifest_id: AccManifestId | None,
    ) -> UpdateAccBaseServiceResult:
        """Set or unset the base ACC for a WIP ACC owned by the requester."""
        logger.info(
            "update acc base id=%d based_acc_manifest_id=%s",
            int(acc_manifest_id),
            int(based_acc_manifest_id) if based_acc_manifest_id is not None else None,
        )
        acc = await self.get_acc(acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_wip_core_component(acc, label="ACC")

        current_base_manifest_id = self._acc_base_manifest_id(acc)
        requested_base_manifest_id = None if based_acc_manifest_id is None else int(based_acc_manifest_id)

        if requested_base_manifest_id == current_base_manifest_id:
            logger.info("update acc base id=%d → no changes", int(acc_manifest_id))
            return UpdateAccBaseServiceResult(acc_manifest_id=int(acc_manifest_id), updates=[])

        if requested_base_manifest_id is not None:
            if requested_base_manifest_id <= 0:
                raise ValueError("based_acc_manifest_id must be a positive integer when provided.")
            if requested_base_manifest_id == int(acc_manifest_id):
                raise ValueError("An ACC cannot be based on itself.")

            based_acc = await self.get_acc(AccManifestId(requested_base_manifest_id))
            if based_acc is None:
                raise LookupError(
                    f"No ACC exists with manifest ID {requested_base_manifest_id}. "
                    "Please verify the identifier and try again."
                )
            await self._assert_reference_release_allowed(
                target_release_id=acc.release.release_id,
                target_library_id=acc.library.library_id,
                referenced_release_id=based_acc.release.release_id,
                referenced_library_id=based_acc.library.library_id,
                referenced_label="The base ACC",
                target_label="the target ACC",
                selection_phrase="a base ACC",
            )
            await self._assert_no_acc_base_cycle(
                acc_manifest_id=acc_manifest_id,
                based_acc=based_acc,
            )
            await self._assert_no_acc_base_conflicts(
                acc=acc,
                based_acc=based_acc,
            )

        updated = await self._repo.update_acc_base(
            acc_manifest_id=acc_manifest_id,
            based_acc_manifest_id=(
                None
                if requested_base_manifest_id is None
                else AccManifestId(requested_base_manifest_id)
            ),
            requester_user_id=self._requester.user.user_id,
        )
        if not updated:
            logger.info("update acc base id=%d → no changes", int(acc_manifest_id))
            return UpdateAccBaseServiceResult(acc_manifest_id=int(acc_manifest_id), updates=[])

        logger.info(
            "update acc base id=%d → based_acc_manifest_id=%s",
            int(acc_manifest_id),
            requested_base_manifest_id,
        )
        return UpdateAccBaseServiceResult(
            acc_manifest_id=int(acc_manifest_id),
            updates=["based_acc_manifest_id"],
        )

    async def get_set_base_acc_warnings(
        self,
        *,
        acc_manifest_id: AccManifestId,
        based_acc_manifest_id: AccManifestId,
    ) -> list[str]:
        """Preview flattened BIE warnings for setting a base ACC."""
        acc = await self.get_acc(acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        based_acc = await self.get_acc(based_acc_manifest_id)
        if based_acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(based_acc_manifest_id)}. Please verify the identifier and try again."
            )
        await self._assert_reference_release_allowed(
            target_release_id=acc.release.release_id,
            target_library_id=acc.library.library_id,
            referenced_release_id=based_acc.release.release_id,
            referenced_library_id=based_acc.library.library_id,
            referenced_label="The base ACC",
            target_label="the target ACC",
            selection_phrase="a base ACC",
        )

        current_terms = await self._collect_flattened_property_terms(acc)
        incoming_terms = await self._collect_flattened_property_terms(based_acc)
        return self._build_flattened_duplicate_warnings(
            current_terms=current_terms,
            incoming_terms=incoming_terms,
            context_label=f"setting base ACC '{based_acc.den}' on '{acc.den}'",
        ) + self._build_sequence_ambiguity_warnings_for_sequence(
            context_label=f"setting base ACC '{based_acc.den}' on '{acc.den}'",
            ordered_items=(
                await self._collect_inherited_sequence_preview_items(based_acc)
            )
            + [
                self._to_sequence_preview_item(relationship)
                for relationship in acc.relationships
            ],
        )

    async def add_acc_tags(
        self,
        *,
        acc_manifest_id: AccManifestId,
        tag_id: list[int],
    ) -> UpdateAccTagsServiceResult:
        """Attach tags to a WIP ACC owned by the requester."""
        logger.info("add acc tags id=%d tag_id=%s", int(acc_manifest_id), tag_id)
        acc = await self.get_acc(acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_wip_core_component(acc, label="ACC")

        normalized_tag_ids = self._normalize_tag_ids(tag_id) or []
        current_tag_ids = {int(tag.tag_id) for tag in acc.tags}
        tag_ids_to_add = [single_tag_id for single_tag_id in normalized_tag_ids if single_tag_id not in current_tag_ids]
        if not tag_ids_to_add:
            logger.info("add acc tags id=%d → no changes", int(acc_manifest_id))
            return UpdateAccTagsServiceResult(acc_manifest_id=int(acc_manifest_id), updates=[])

        updated = await self._repo.add_acc_tags(
            acc_manifest_id=acc_manifest_id,
            tag_id=tag_ids_to_add,
            requester_user_id=self._requester.user.user_id,
        )
        if not updated:
            logger.info("add acc tags id=%d → no changes", int(acc_manifest_id))
            return UpdateAccTagsServiceResult(acc_manifest_id=int(acc_manifest_id), updates=[])

        logger.info("add acc tags id=%d → %s", int(acc_manifest_id), tag_ids_to_add)
        return UpdateAccTagsServiceResult(acc_manifest_id=int(acc_manifest_id), updates=["tags"])

    async def remove_acc_tags(
        self,
        *,
        acc_manifest_id: AccManifestId,
        tag_id: list[int],
    ) -> UpdateAccTagsServiceResult:
        """Remove tags from a WIP ACC owned by the requester."""
        logger.info("remove acc tags id=%d tag_id=%s", int(acc_manifest_id), tag_id)
        acc = await self.get_acc(acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_wip_core_component(acc, label="ACC")

        normalized_tag_ids = self._normalize_tag_ids(tag_id) or []
        current_tag_ids = {int(tag.tag_id) for tag in acc.tags}
        tag_ids_to_remove = [single_tag_id for single_tag_id in normalized_tag_ids if single_tag_id in current_tag_ids]
        if not tag_ids_to_remove:
            logger.info("remove acc tags id=%d → no changes", int(acc_manifest_id))
            return UpdateAccTagsServiceResult(acc_manifest_id=int(acc_manifest_id), updates=[])

        updated = await self._repo.remove_acc_tags(
            acc_manifest_id=acc_manifest_id,
            tag_id=tag_ids_to_remove,
            requester_user_id=self._requester.user.user_id,
        )
        if not updated:
            logger.info("remove acc tags id=%d → no changes", int(acc_manifest_id))
            return UpdateAccTagsServiceResult(acc_manifest_id=int(acc_manifest_id), updates=[])

        logger.info("remove acc tags id=%d → %s", int(acc_manifest_id), tag_ids_to_remove)
        return UpdateAccTagsServiceResult(acc_manifest_id=int(acc_manifest_id), updates=["tags"])

    async def change_asccp_role_of_acc(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        role_of_acc_manifest_id: AccManifestId,
        allow_warnings: bool = False,
    ) -> None:
        """Change the role ACC of a WIP ASCCP."""
        asccp = await self.get_asccp(asccp_manifest_id)
        if asccp is None:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_wip_core_component(asccp, label="ASCCP")
        warnings = await self.get_change_asccp_role_of_acc_warnings(
            asccp_manifest_id=asccp_manifest_id,
            role_of_acc_manifest_id=role_of_acc_manifest_id,
        )
        self._raise_for_structural_warnings(warnings=warnings, allow_warnings=allow_warnings)
        await self._repo.change_asccp_role_of_acc(
            asccp_manifest_id=asccp_manifest_id,
            role_of_acc_manifest_id=role_of_acc_manifest_id,
            requester_user_id=self._requester.user.user_id,
        )

    async def change_bccp_bdt(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        bdt_manifest_id: DataTypeManifestId,
    ) -> None:
        """Change the BDT of a WIP BCCP."""
        bccp = await self.get_bccp(bccp_manifest_id)
        if bccp is None:
            raise LookupError(
                f"No BCCP exists with manifest ID {int(bccp_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_wip_core_component(bccp, label="BCCP")
        bdt = await self._data_type_service.get(bdt_manifest_id)
        if bdt is None:
            raise LookupError(
                f"No data type exists with manifest ID {int(bdt_manifest_id)}. Please verify the identifier and try again."
            )
        await self._assert_reference_release_allowed(
            target_release_id=bccp.release.release_id,
            target_library_id=bccp.library.library_id,
            referenced_release_id=bdt.release.release_id,
            referenced_library_id=bdt.library.library_id,
            referenced_label="The target BDT",
            target_label="the target BCCP",
            selection_phrase="a BDT",
        )
        await self._repo.change_bccp_bdt(
            bccp_manifest_id=bccp_manifest_id,
            bdt_manifest_id=bdt_manifest_id,
            requester_user_id=self._requester.user.user_id,
        )

    async def get_update_asccp_warnings(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        role_of_acc_manifest_id: AccManifestId | UnsetType = UNSET,
        property_term: str | None | UnsetType = UNSET,
    ) -> list[str]:
        """Preview structural warnings for ASCCP field updates."""
        if property_term is UNSET and role_of_acc_manifest_id is UNSET:
            return []
        asccp = await self.get_asccp(asccp_manifest_id)
        if asccp is None:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )

        preview_asccp = asccp
        if property_term is not UNSET:
            preview_asccp = replace(preview_asccp, property_term=property_term)
        if role_of_acc_manifest_id is not UNSET:
            role_of_acc = await self.get_acc(role_of_acc_manifest_id)
            if role_of_acc is None:
                raise LookupError(
                    f"No ACC exists with manifest ID {int(role_of_acc_manifest_id)}. Please verify the identifier and try again."
                )
            await self._assert_reference_release_allowed(
                target_release_id=asccp.release.release_id,
                target_library_id=asccp.library.library_id,
                referenced_release_id=role_of_acc.release.release_id,
                referenced_library_id=role_of_acc.library.library_id,
                referenced_label="The role ACC",
                target_label="the target ASCCP",
                selection_phrase="a role ACC",
            )
            preview_asccp = replace(
                preview_asccp,
                role_of_acc=BaseAccSummaryServiceRecord(
                    acc_manifest_id=int(role_of_acc.acc_manifest_id),
                    acc_id=int(role_of_acc.acc_id),
                    guid=str(role_of_acc.guid),
                    den=str(role_of_acc.den),
                    object_class_term=str(role_of_acc.object_class_term),
                    type=OAGIS_COMPONENT_TYPE_NAMES.get(int(role_of_acc.component_type))
                    if role_of_acc.component_type is not None
                    else None,
                    definition=role_of_acc.definition,
                    definition_source=role_of_acc.definition_source,
                    namespace=role_of_acc.namespace,
                    library=role_of_acc.library,
                    release=role_of_acc.release,
                ),
            )
        return await self._collect_acc_warnings_for_asccp_preview(
            asccp_manifest_id=asccp_manifest_id,
            current_asccp=asccp,
            preview_asccp=preview_asccp,
            context_action=f"updating ASCCP '{asccp.den}'",
        )

    async def get_change_asccp_role_of_acc_warnings(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        role_of_acc_manifest_id: AccManifestId,
    ) -> list[str]:
        """Preview structural warnings for changing an ASCCP role ACC."""
        asccp = await self.get_asccp(asccp_manifest_id)
        if asccp is None:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )
        role_of_acc = await self.get_acc(role_of_acc_manifest_id)
        if role_of_acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(role_of_acc_manifest_id)}. Please verify the identifier and try again."
            )
        await self._assert_reference_release_allowed(
            target_release_id=asccp.release.release_id,
            target_library_id=asccp.library.library_id,
            referenced_release_id=role_of_acc.release.release_id,
            referenced_library_id=role_of_acc.library.library_id,
            referenced_label="The role ACC",
            target_label="the target ASCCP",
            selection_phrase="a role ACC",
        )
        preview_asccp = replace(
            asccp,
            role_of_acc=BaseAccSummaryServiceRecord(
                acc_manifest_id=int(role_of_acc.acc_manifest_id),
                acc_id=int(role_of_acc.acc_id),
                guid=str(role_of_acc.guid),
                den=str(role_of_acc.den),
                object_class_term=str(role_of_acc.object_class_term),
                type=OAGIS_COMPONENT_TYPE_NAMES.get(int(role_of_acc.component_type))
                if role_of_acc.component_type is not None
                else None,
                definition=role_of_acc.definition,
                definition_source=role_of_acc.definition_source,
                namespace=role_of_acc.namespace,
                library=role_of_acc.library,
                release=role_of_acc.release,
            ),
        )
        return await self._collect_acc_warnings_for_asccp_preview(
            asccp_manifest_id=asccp_manifest_id,
            current_asccp=asccp,
            preview_asccp=preview_asccp,
            context_action=f"changing the role ACC of ASCCP '{asccp.den}'",
        )

    async def get_update_bccp_warnings(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        property_term: str | None | UnsetType = UNSET,
    ) -> list[str]:
        """Preview structural warnings for BCCP field updates."""
        if property_term is UNSET:
            return []
        bccp = await self.get_bccp(bccp_manifest_id)
        if bccp is None:
            raise LookupError(
                f"No BCCP exists with manifest ID {int(bccp_manifest_id)}. Please verify the identifier and try again."
            )
        warnings: list[str] = []
        impacted_acc_manifest_ids = await self._repo.list_owner_acc_manifest_ids_by_bccp_manifest(bccp_manifest_id)
        for acc_manifest_id in impacted_acc_manifest_ids:
            acc = await self.get_acc(acc_manifest_id)
            if acc is None:
                continue
            remaining_terms = self._subtract_flattened_terms(
                source_terms=await self._collect_flattened_property_terms(acc),
                terms_to_remove=[
                    _FlattenedAccPropertyTerm(
                        property_term=bccp.property_term,
                        source=f"BCCP '{bccp.den}'",
                    )
                ] if bccp.property_term else [],
            )
            warnings.extend(
                self._build_flattened_duplicate_warnings(
                    current_terms=remaining_terms,
                    incoming_terms=[
                        _FlattenedAccPropertyTerm(
                            property_term=property_term,
                            source=f"BCCP '{bccp.den}'",
                        )
                    ] if property_term else [],
                    context_label=f"updating BCCP '{bccp.den}' used by '{acc.den}'",
                )
            )
        return list(dict.fromkeys(warnings))

    async def add_asccp_tags(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        tag_id: list[int],
    ) -> None:
        """Attach tags to a WIP ASCCP."""
        asccp = await self.get_asccp(asccp_manifest_id)
        if asccp is None:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_wip_core_component(asccp, label="ASCCP")
        normalized_tag_ids = self._normalize_tag_ids(tag_id) or []
        current_tag_ids = {int(tag.tag_id) for tag in asccp.tags}
        tag_ids_to_add = [single_tag_id for single_tag_id in normalized_tag_ids if single_tag_id not in current_tag_ids]
        if not tag_ids_to_add:
            return
        await self._repo.add_asccp_tags(
            asccp_manifest_id=asccp_manifest_id,
            tag_id=tag_ids_to_add,
            requester_user_id=self._requester.user.user_id,
        )

    async def remove_asccp_tags(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        tag_id: list[int],
    ) -> None:
        """Remove tags from a WIP ASCCP."""
        asccp = await self.get_asccp(asccp_manifest_id)
        if asccp is None:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_wip_core_component(asccp, label="ASCCP")
        normalized_tag_ids = self._normalize_tag_ids(tag_id) or []
        current_tag_ids = {int(tag.tag_id) for tag in asccp.tags}
        tag_ids_to_remove = [single_tag_id for single_tag_id in normalized_tag_ids if single_tag_id in current_tag_ids]
        if not tag_ids_to_remove:
            return
        await self._repo.remove_asccp_tags(
            asccp_manifest_id=asccp_manifest_id,
            tag_id=tag_ids_to_remove,
            requester_user_id=self._requester.user.user_id,
        )

    async def add_bccp_tags(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        tag_id: list[int],
    ) -> None:
        """Attach tags to a WIP BCCP."""
        bccp = await self.get_bccp(bccp_manifest_id)
        if bccp is None:
            raise LookupError(
                f"No BCCP exists with manifest ID {int(bccp_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_wip_core_component(bccp, label="BCCP")
        normalized_tag_ids = self._normalize_tag_ids(tag_id) or []
        current_tag_ids = {int(tag.tag_id) for tag in bccp.tags}
        tag_ids_to_add = [single_tag_id for single_tag_id in normalized_tag_ids if single_tag_id not in current_tag_ids]
        if not tag_ids_to_add:
            return
        await self._repo.add_bccp_tags(
            bccp_manifest_id=bccp_manifest_id,
            tag_id=tag_ids_to_add,
            requester_user_id=self._requester.user.user_id,
        )

    async def remove_bccp_tags(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        tag_id: list[int],
    ) -> None:
        """Remove tags from a WIP BCCP."""
        bccp = await self.get_bccp(bccp_manifest_id)
        if bccp is None:
            raise LookupError(
                f"No BCCP exists with manifest ID {int(bccp_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_wip_core_component(bccp, label="BCCP")
        normalized_tag_ids = self._normalize_tag_ids(tag_id) or []
        current_tag_ids = {int(tag.tag_id) for tag in bccp.tags}
        tag_ids_to_remove = [single_tag_id for single_tag_id in normalized_tag_ids if single_tag_id in current_tag_ids]
        if not tag_ids_to_remove:
            return
        await self._repo.remove_bccp_tags(
            bccp_manifest_id=bccp_manifest_id,
            tag_id=tag_ids_to_remove,
            requester_user_id=self._requester.user.user_id,
        )

    async def add_ascc_to_acc(
        self,
        *,
        acc_manifest_id: AccManifestId,
        asccp_manifest_id: AsccpManifestId,
        index: int | None = None,
        after_ascc_manifest_id: AsccManifestId | None = None,
        after_bcc_manifest_id: BccManifestId | None = None,
        before_ascc_manifest_id: AsccManifestId | None = None,
        before_bcc_manifest_id: BccManifestId | None = None,
        cardinality_min: int | None = None,
        cardinality_max: int | None = None,
        definition: str | None = None,
        definition_source: str | None = None,
    ) -> CreateAsccServiceResult:
        """Add an ASCC relationship to an ACC, creating it if needed."""
        logger.info(
            "place ascc acc_manifest_id=%d asccp_manifest_id=%d index=%s",
            int(acc_manifest_id),
            int(asccp_manifest_id),
            index,
        )
        acc = await self.get_acc(acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_wip_core_component(acc, label="ACC")

        asccp = await self.get_asccp(asccp_manifest_id)
        if asccp is None:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )
        await self._assert_reference_release_allowed(
            target_release_id=acc.release.release_id,
            target_library_id=acc.library.library_id,
            referenced_release_id=asccp.release.release_id,
            referenced_library_id=asccp.library.library_id,
            referenced_label="The target ASCCP",
            target_label="the source ACC",
            selection_phrase="an ASCCP",
        )
        if not asccp.reusable_indicator:
            raise ValueError("Target ASCCP is not reusable.")

        existing_relationship = next(
            (
                relationship
                for relationship in acc.relationships
                if getattr(relationship, "component_type", None) == "ASCC"
                and int(self._relationship_target_manifest_id(relationship)) == int(asccp_manifest_id)
            ),
            None,
        )
        if existing_relationship is not None:
            raise ValueError(
                "This ASCC is already in the ACC sequence. Please use `reorder_ascc_in_acc` instead."
            )

        resolved_index = self._resolve_add_sequence_index(
            relationships=acc.relationships,
            index=index,
            after_ascc_manifest_id=after_ascc_manifest_id,
            after_bcc_manifest_id=after_bcc_manifest_id,
            before_ascc_manifest_id=before_ascc_manifest_id,
            before_bcc_manifest_id=before_bcc_manifest_id,
            default_to_end=True,
        )
        normalized_definition = definition.strip() or None if isinstance(definition, str) else None
        normalized_definition_source = definition_source.strip() or None if isinstance(definition_source, str) else None

        final_cardinality_min = self._normalize_required_cardinality_min(cardinality_min, default=0)
        final_cardinality_max = self._normalize_required_cardinality_max(cardinality_max, default=-1)
        self._assert_valid_cardinality_range(
            cardinality_min=final_cardinality_min,
            cardinality_max=final_cardinality_max,
        )

        ascc_manifest_id = await self._repo.create_ascc(
            acc_manifest_id=acc_manifest_id,
            asccp_manifest_id=asccp_manifest_id,
            index=resolved_index,
            cardinality_min=final_cardinality_min,
            cardinality_max=final_cardinality_max,
            definition=normalized_definition,
            definition_source=normalized_definition_source,
            requester_user_id=self._requester.user.user_id,
        )
        logger.info(
            "place ascc acc_manifest_id=%d asccp_manifest_id=%d → ascc_manifest_id=%d",
            int(acc_manifest_id),
            int(asccp_manifest_id),
            int(ascc_manifest_id),
        )
        return CreateAsccServiceResult(ascc_manifest_id=int(ascc_manifest_id))

    async def get_add_ascc_to_acc_warnings(
        self,
        *,
        acc_manifest_id: AccManifestId,
        asccp_manifest_id: AsccpManifestId,
        index: int | None = None,
        after_ascc_manifest_id: AsccManifestId | None = None,
        after_bcc_manifest_id: BccManifestId | None = None,
        before_ascc_manifest_id: AsccManifestId | None = None,
        before_bcc_manifest_id: BccManifestId | None = None,
    ) -> list[str]:
        """Preview flattened BIE warnings for adding an ASCC to an ACC."""
        acc = await self.get_acc(acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        asccp = await self.get_asccp(asccp_manifest_id)
        if asccp is None:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )
        await self._assert_reference_release_allowed(
            target_release_id=acc.release.release_id,
            target_library_id=acc.library.library_id,
            referenced_release_id=asccp.release.release_id,
            referenced_library_id=asccp.library.library_id,
            referenced_label="The target ASCCP",
            target_label="the source ACC",
            selection_phrase="an ASCCP",
        )

        current_terms = await self._collect_flattened_property_terms(acc)
        incoming_terms = await self._collect_flattened_property_terms_from_asccp(asccp)
        label = asccp.den or asccp.property_term or f"ASCCP {int(asccp_manifest_id)}"
        warnings = self._build_flattened_duplicate_warnings(
            current_terms=current_terms,
            incoming_terms=incoming_terms,
            context_label=f"adding ASCC '{label}' to '{acc.den}'",
        )
        existing_relationship = next(
            (
                relationship
                for relationship in acc.relationships
                if getattr(relationship, "component_type", None) == "ASCC"
                and int(self._relationship_target_manifest_id(relationship)) == int(asccp_manifest_id)
            ),
            None,
        )
        if existing_relationship is not None:
            raise ValueError(
                "This ASCC is already in the ACC sequence. Please use `reorder_ascc_in_acc` instead."
            )
        resolved_index = self._resolve_add_sequence_index(
            relationships=acc.relationships,
            index=index,
            after_ascc_manifest_id=after_ascc_manifest_id,
            after_bcc_manifest_id=after_bcc_manifest_id,
            before_ascc_manifest_id=before_ascc_manifest_id,
            before_bcc_manifest_id=before_bcc_manifest_id,
            default_to_end=True,
        )
        final_sequence = self._preview_sequence_for_add(
            relationships=acc.relationships,
            new_item=_SequencePreviewItem(
                component_type="ASCC",
                manifest_id=int(asccp_manifest_id),
                relationship_label=label,
                cardinality_max=-1,
            ),
            index=resolved_index,
        )
        warnings.extend(
            self._build_sequence_ambiguity_warnings_for_sequence(
                context_label=f"adding ASCC '{label}' to '{acc.den}'",
                ordered_items=final_sequence,
            )
        )
        return warnings

    async def add_bcc_to_acc(
        self,
        *,
        acc_manifest_id: AccManifestId,
        bccp_manifest_id: BccpManifestId,
        index: int | None = None,
        after_ascc_manifest_id: AsccManifestId | None = None,
        after_bcc_manifest_id: BccManifestId | None = None,
        before_ascc_manifest_id: AsccManifestId | None = None,
        before_bcc_manifest_id: BccManifestId | None = None,
        entity_type: Literal["Attribute", "Element"] | None = None,
        cardinality_min: int | None = None,
        cardinality_max: int | None = None,
        definition: str | None = None,
        definition_source: str | None = None,
        is_nillable: bool | None = None,
        default_value: str | None = None,
        fixed_value: str | None = None,
    ) -> CreateBccServiceResult:
        """Add a BCC relationship to an ACC, creating it if needed."""
        logger.info(
            "place bcc acc_manifest_id=%d bccp_manifest_id=%d index=%s",
            int(acc_manifest_id),
            int(bccp_manifest_id),
            index,
        )
        acc = await self.get_acc(acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_wip_core_component(acc, label="ACC")

        bccp = await self.get_bccp(bccp_manifest_id)
        if bccp is None:
            raise LookupError(
                f"No BCCP exists with manifest ID {int(bccp_manifest_id)}. Please verify the identifier and try again."
            )
        await self._assert_reference_release_allowed(
            target_release_id=acc.release.release_id,
            target_library_id=acc.library.library_id,
            referenced_release_id=bccp.release.release_id,
            referenced_library_id=bccp.library.library_id,
            referenced_label="The target BCCP",
            target_label="the source ACC",
            selection_phrase="a BCCP",
        )

        existing_relationship = next(
            (
                relationship
                for relationship in acc.relationships
                if getattr(relationship, "component_type", None) == "BCC"
                and int(self._relationship_target_manifest_id(relationship)) == int(bccp_manifest_id)
            ),
            None,
        )
        if existing_relationship is not None:
            raise ValueError(
                "This BCC is already in the ACC sequence. Please use `reorder_bcc_in_acc` instead."
            )

        normalized_definition = definition.strip() or None if isinstance(definition, str) else None
        normalized_definition_source = definition_source.strip() or None if isinstance(definition_source, str) else None
        normalized_default_value = default_value.strip() or None if isinstance(default_value, str) else default_value
        normalized_fixed_value = fixed_value.strip() or None if isinstance(fixed_value, str) else fixed_value
        if normalized_default_value is not None and normalized_fixed_value is not None:
            raise ValueError("Provide only one of `default_value` or `fixed_value`.")

        final_entity_type = "Element" if entity_type is None else self._normalize_bcc_entity_type(entity_type)

        resolved_index = self._resolve_add_sequence_index(
            relationships=acc.relationships,
            index=index,
            after_ascc_manifest_id=after_ascc_manifest_id,
            after_bcc_manifest_id=after_bcc_manifest_id,
            before_ascc_manifest_id=before_ascc_manifest_id,
            before_bcc_manifest_id=before_bcc_manifest_id,
            default_to_end=True,
        )
        effective_index = resolved_index
        if final_entity_type == "Attribute":
            await self._assert_attribute_bcc_allowed_for_bdt(
                bdt_manifest_id=int(bccp.bdt.dt_manifest_id),
            )
            effective_index = self._resolve_attribute_bcc_add_index(
                relationships=acc.relationships,
                index=resolved_index,
            )
            final_cardinality_min = self._normalize_required_cardinality_min(cardinality_min, default=0)
            final_cardinality_max = self._normalize_required_cardinality_max(cardinality_max, default=1)
            self._assert_valid_attribute_bcc_cardinality(
                cardinality_min=final_cardinality_min,
                cardinality_max=final_cardinality_max,
            )
        else:
            final_cardinality_min = self._normalize_required_cardinality_min(cardinality_min, default=0)
            final_cardinality_max = self._normalize_required_cardinality_max(cardinality_max, default=-1)

        self._assert_valid_sequence_index(index=effective_index, sequence_length=len(acc.relationships))
        self._assert_valid_cardinality_range(
            cardinality_min=final_cardinality_min,
            cardinality_max=final_cardinality_max,
        )

        bcc_manifest_id = await self._repo.create_bcc(
            acc_manifest_id=acc_manifest_id,
            bccp_manifest_id=bccp_manifest_id,
            index=effective_index,
            entity_type=final_entity_type,
            cardinality_min=final_cardinality_min,
            cardinality_max=final_cardinality_max,
            definition=normalized_definition,
            definition_source=normalized_definition_source,
            is_nillable=bool(is_nillable) if is_nillable is not None else False,
            default_value=normalized_default_value,
            fixed_value=normalized_fixed_value,
            requester_user_id=self._requester.user.user_id,
        )
        logger.info(
            "place bcc acc_manifest_id=%d bccp_manifest_id=%d → bcc_manifest_id=%d",
            int(acc_manifest_id),
            int(bccp_manifest_id),
            int(bcc_manifest_id),
        )
        return CreateBccServiceResult(bcc_manifest_id=int(bcc_manifest_id))

    async def get_add_bcc_to_acc_warnings(
        self,
        *,
        acc_manifest_id: AccManifestId,
        bccp_manifest_id: BccpManifestId,
        index: int | None = None,
        after_ascc_manifest_id: AsccManifestId | None = None,
        after_bcc_manifest_id: BccManifestId | None = None,
        before_ascc_manifest_id: AsccManifestId | None = None,
        before_bcc_manifest_id: BccManifestId | None = None,
        entity_type: Literal["Attribute", "Element"] | None = None,
    ) -> list[str]:
        """Preview flattened BIE warnings for adding a BCC to an ACC."""
        acc = await self.get_acc(acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        bccp = await self.get_bccp(bccp_manifest_id)
        if bccp is None:
            raise LookupError(
                f"No BCCP exists with manifest ID {int(bccp_manifest_id)}. Please verify the identifier and try again."
            )
        await self._assert_reference_release_allowed(
            target_release_id=acc.release.release_id,
            target_library_id=acc.library.library_id,
            referenced_release_id=bccp.release.release_id,
            referenced_library_id=bccp.library.library_id,
            referenced_label="The target BCCP",
            target_label="the source ACC",
            selection_phrase="a BCCP",
        )

        current_terms = await self._collect_flattened_property_terms(acc)
        incoming_terms = [
            _FlattenedAccPropertyTerm(
                property_term=bccp.property_term,
                source=f"BCCP '{bccp.den}'",
            )
        ]
        warnings = self._build_flattened_duplicate_warnings(
            current_terms=current_terms,
            incoming_terms=incoming_terms,
            context_label=f"adding BCC '{bccp.den}' to '{acc.den}'",
        )
        existing_relationship = next(
            (
                relationship
                for relationship in acc.relationships
                if getattr(relationship, "component_type", None) == "BCC"
                and int(self._relationship_target_manifest_id(relationship)) == int(bccp_manifest_id)
            ),
            None,
        )
        if existing_relationship is not None:
            raise ValueError(
                "This BCC is already in the ACC sequence. Please use `reorder_bcc_in_acc` instead."
            )
        final_entity_type = "Element" if entity_type is None else str(entity_type)
        if final_entity_type not in {"Attribute", "Element"}:
            raise ValueError("`entity_type` must be either `Attribute` or `Element`.")
        resolved_index = self._resolve_add_sequence_index(
            relationships=acc.relationships,
            index=index,
            after_ascc_manifest_id=after_ascc_manifest_id,
            after_bcc_manifest_id=after_bcc_manifest_id,
            before_ascc_manifest_id=before_ascc_manifest_id,
            before_bcc_manifest_id=before_bcc_manifest_id,
            default_to_end=True,
        )
        effective_index = (
            self._resolve_attribute_bcc_add_index(
                relationships=acc.relationships,
                index=resolved_index,
            )
            if final_entity_type == "Attribute"
            else resolved_index
        )
        final_sequence = self._preview_sequence_for_add(
            relationships=acc.relationships,
            new_item=_SequencePreviewItem(
                component_type="BCC",
                manifest_id=int(bccp_manifest_id),
                relationship_label=bccp.den,
                cardinality_max=-1,
            ),
            index=effective_index,
        )
        warnings.extend(
            self._build_sequence_ambiguity_warnings_for_sequence(
                context_label=f"adding BCC '{bccp.den}' to '{acc.den}'",
                ordered_items=final_sequence,
            )
        )
        return warnings

    async def remove_ascc(
        self,
        *,
        ascc_manifest_id: AsccManifestId,
    ) -> None:
        """Remove an existing ASCC relationship from an ACC."""
        acc_manifest_id = await self._repo.get_owner_acc_manifest_id_by_ascc_manifest(ascc_manifest_id)
        if acc_manifest_id is None:
            raise LookupError(
                f"No ASCC exists with manifest ID {int(ascc_manifest_id)}. Please verify the identifier and try again."
            )
        logger.info("remove ascc acc_manifest_id=%d ascc_manifest_id=%d", int(acc_manifest_id), int(ascc_manifest_id))
        acc = await self.get_acc(acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_wip_core_component(acc, label="ACC")
        self._ensure_relationship_exists(
            relationships=acc.relationships,
            component_type="ASCC",
            manifest_id=int(ascc_manifest_id),
            label="ASCC",
        )
        bie_usage_count = await self._repo.count_bie_references_by_ascc_manifest(ascc_manifest_id)
        if bie_usage_count > 0:
            raise ValueError(
                f"This association is referenced in {bie_usage_count} BIE(s) and cannot be deleted."
            )
        removed = await self._repo.remove_ascc(
            ascc_manifest_id=ascc_manifest_id,
            requester_user_id=self._requester.user.user_id,
        )
        if not removed:
            raise LookupError(
                f"No ASCC exists with manifest ID {int(ascc_manifest_id)}. Please verify the identifier and try again."
            )
        logger.info("remove ascc acc_manifest_id=%d ascc_manifest_id=%d → removed", int(acc_manifest_id), int(ascc_manifest_id))

    async def remove_bcc(
        self,
        *,
        bcc_manifest_id: BccManifestId,
    ) -> None:
        """Remove an existing BCC relationship from an ACC."""
        acc_manifest_id = await self._repo.get_owner_acc_manifest_id_by_bcc_manifest(bcc_manifest_id)
        if acc_manifest_id is None:
            raise LookupError(
                f"No BCC exists with manifest ID {int(bcc_manifest_id)}. Please verify the identifier and try again."
            )
        logger.info("remove bcc acc_manifest_id=%d bcc_manifest_id=%d", int(acc_manifest_id), int(bcc_manifest_id))
        acc = await self.get_acc(acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_wip_core_component(acc, label="ACC")
        self._ensure_relationship_exists(
            relationships=acc.relationships,
            component_type="BCC",
            manifest_id=int(bcc_manifest_id),
            label="BCC",
        )
        bie_usage_count = await self._repo.count_bie_references_by_bcc_manifest(bcc_manifest_id)
        if bie_usage_count > 0:
            raise ValueError(
                f"This association is referenced in {bie_usage_count} BIE(s) and cannot be deleted."
            )
        removed = await self._repo.remove_bcc(
            bcc_manifest_id=bcc_manifest_id,
            requester_user_id=self._requester.user.user_id,
        )
        if not removed:
            raise LookupError(
                f"No BCC exists with manifest ID {int(bcc_manifest_id)}. Please verify the identifier and try again."
            )
        logger.info("remove bcc acc_manifest_id=%d bcc_manifest_id=%d → removed", int(acc_manifest_id), int(bcc_manifest_id))

    async def get_reorder_ascc_in_acc_warnings(
        self,
        *,
        ascc_manifest_id: AsccManifestId,
        index: int | None = None,
        after_ascc_manifest_id: AsccManifestId | None = None,
        after_bcc_manifest_id: BccManifestId | None = None,
        before_ascc_manifest_id: AsccManifestId | None = None,
        before_bcc_manifest_id: BccManifestId | None = None,
    ) -> list[str]:
        """Preview sequence warnings for reordering an ASCC within an ACC."""
        acc_manifest_id = await self._repo.get_owner_acc_manifest_id_by_ascc_manifest(ascc_manifest_id)
        if acc_manifest_id is None:
            raise LookupError(
                f"No ASCC exists with manifest ID {int(ascc_manifest_id)}. Please verify the identifier and try again."
            )
        acc = await self.get_acc(acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        relationship = next(
            (
                item for item in acc.relationships
                if getattr(item, "component_type", None) == "ASCC"
                and int(item.ascc_manifest_id) == int(ascc_manifest_id)
            ),
            None,
        )
        if relationship is None:
            raise LookupError(
                f"No ASCC exists with manifest ID {int(ascc_manifest_id)}. Please verify the identifier and try again."
            )
        final_sequence = self._preview_sequence_for_move(
            relationships=acc.relationships,
            item_component_type="ASCC",
            item_manifest_id=int(ascc_manifest_id),
            index=index,
            after_ascc_manifest_id=after_ascc_manifest_id,
            after_bcc_manifest_id=after_bcc_manifest_id,
            before_ascc_manifest_id=before_ascc_manifest_id,
            before_bcc_manifest_id=before_bcc_manifest_id,
        )
        return self._build_sequence_ambiguity_warnings_for_sequence(
            context_label=f"reordering ASCC '{self._relationship_target_den(relationship)}' in '{acc.den}'",
            ordered_items=final_sequence,
        )

    async def get_reorder_bcc_in_acc_warnings(
        self,
        *,
        bcc_manifest_id: BccManifestId,
        index: int | None = None,
        after_ascc_manifest_id: AsccManifestId | None = None,
        after_bcc_manifest_id: BccManifestId | None = None,
        before_ascc_manifest_id: AsccManifestId | None = None,
        before_bcc_manifest_id: BccManifestId | None = None,
    ) -> list[str]:
        """Preview sequence warnings for reordering a BCC within an ACC."""
        acc_manifest_id = await self._repo.get_owner_acc_manifest_id_by_bcc_manifest(bcc_manifest_id)
        if acc_manifest_id is None:
            raise LookupError(
                f"No BCC exists with manifest ID {int(bcc_manifest_id)}. Please verify the identifier and try again."
            )
        acc = await self.get_acc(acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        relationship = next(
            (
                item for item in acc.relationships
                if getattr(item, "component_type", None) == "BCC"
                and int(item.bcc_manifest_id) == int(bcc_manifest_id)
            ),
            None,
        )
        if relationship is None:
            raise LookupError(
                f"No BCC exists with manifest ID {int(bcc_manifest_id)}. Please verify the identifier and try again."
            )
        final_sequence = self._preview_sequence_for_move(
            relationships=acc.relationships,
            item_component_type="BCC",
            item_manifest_id=int(bcc_manifest_id),
            index=index,
            after_ascc_manifest_id=after_ascc_manifest_id,
            after_bcc_manifest_id=after_bcc_manifest_id,
            before_ascc_manifest_id=before_ascc_manifest_id,
            before_bcc_manifest_id=before_bcc_manifest_id,
        )
        return self._build_sequence_ambiguity_warnings_for_sequence(
            context_label=f"reordering BCC '{self._relationship_target_den(relationship)}' in '{acc.den}'",
            ordered_items=final_sequence,
        )

    async def reorder_ascc_in_acc(
        self,
        *,
        ascc_manifest_id: AsccManifestId,
        index: int | None = None,
        after_ascc_manifest_id: AsccManifestId | None = None,
        after_bcc_manifest_id: BccManifestId | None = None,
        before_ascc_manifest_id: AsccManifestId | None = None,
        before_bcc_manifest_id: BccManifestId | None = None,
    ) -> MoveAsccServiceResult:
        """Reorder an existing ASCC within an ACC sequence."""
        acc_manifest_id = await self._repo.get_owner_acc_manifest_id_by_ascc_manifest(ascc_manifest_id)
        if acc_manifest_id is None:
            raise LookupError(
                f"No ASCC exists with manifest ID {int(ascc_manifest_id)}. Please verify the identifier and try again."
            )
        logger.info("move ascc acc_manifest_id=%d ascc_manifest_id=%d", int(acc_manifest_id), int(ascc_manifest_id))
        acc = await self.get_acc(acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_wip_core_component(acc, label="ACC")

        self._ensure_relationship_exists(
            relationships=acc.relationships,
            component_type="ASCC",
            manifest_id=int(ascc_manifest_id),
            label="ASCC",
        )
        resolved_after_ascc_manifest_id, resolved_after_bcc_manifest_id = self._resolve_move_after_reference(
            relationships=acc.relationships,
            item_component_type="ASCC",
            item_manifest_id=int(ascc_manifest_id),
            index=index,
            after_ascc_manifest_id=after_ascc_manifest_id,
            after_bcc_manifest_id=after_bcc_manifest_id,
            before_ascc_manifest_id=before_ascc_manifest_id,
            before_bcc_manifest_id=before_bcc_manifest_id,
        )

        await self._repo.move_acc_sequence(
            acc_manifest_id=acc_manifest_id,
            item_ascc_manifest_id=ascc_manifest_id,
            item_bcc_manifest_id=None,
            after_ascc_manifest_id=resolved_after_ascc_manifest_id,
            after_bcc_manifest_id=resolved_after_bcc_manifest_id,
            requester_user_id=self._requester.user.user_id,
        )
        return MoveAsccServiceResult(ascc_manifest_id=int(ascc_manifest_id), updates=["sequence"])

    async def reorder_bcc_in_acc(
        self,
        *,
        bcc_manifest_id: BccManifestId,
        index: int | None = None,
        after_ascc_manifest_id: AsccManifestId | None = None,
        after_bcc_manifest_id: BccManifestId | None = None,
        before_ascc_manifest_id: AsccManifestId | None = None,
        before_bcc_manifest_id: BccManifestId | None = None,
    ) -> MoveBccServiceResult:
        """Reorder an existing BCC within an ACC sequence."""
        acc_manifest_id = await self._repo.get_owner_acc_manifest_id_by_bcc_manifest(bcc_manifest_id)
        if acc_manifest_id is None:
            raise LookupError(
                f"No BCC exists with manifest ID {int(bcc_manifest_id)}. Please verify the identifier and try again."
            )
        logger.info("move bcc acc_manifest_id=%d bcc_manifest_id=%d", int(acc_manifest_id), int(bcc_manifest_id))
        acc = await self.get_acc(acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_update_wip_core_component(acc, label="ACC")

        self._ensure_relationship_exists(
            relationships=acc.relationships,
            component_type="BCC",
            manifest_id=int(bcc_manifest_id),
            label="BCC",
        )
        resolved_after_ascc_manifest_id, resolved_after_bcc_manifest_id = self._resolve_move_after_reference(
            relationships=acc.relationships,
            item_component_type="BCC",
            item_manifest_id=int(bcc_manifest_id),
            index=index,
            after_ascc_manifest_id=after_ascc_manifest_id,
            after_bcc_manifest_id=after_bcc_manifest_id,
            before_ascc_manifest_id=before_ascc_manifest_id,
            before_bcc_manifest_id=before_bcc_manifest_id,
        )

        await self._repo.move_acc_sequence(
            acc_manifest_id=acc_manifest_id,
            item_ascc_manifest_id=None,
            item_bcc_manifest_id=bcc_manifest_id,
            after_ascc_manifest_id=resolved_after_ascc_manifest_id,
            after_bcc_manifest_id=resolved_after_bcc_manifest_id,
            requester_user_id=self._requester.user.user_id,
        )
        return MoveBccServiceResult(bcc_manifest_id=int(bcc_manifest_id), updates=["sequence"])

    async def change_acc_state(
        self,
        *,
        acc_manifest_id: AccManifestId,
        state: CoreComponentState,
    ) -> UpdateAccServiceResult:
        """Transition an ACC lifecycle state according to connectCenter rules."""
        logger.info("update acc state id=%d state=%s", int(acc_manifest_id), state)
        acc = await self.get_acc(acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )

        current_state = str(acc.state or "")
        self._assert_component_state_transition_allowed(acc, state=state, label="ACC")
        restore = current_state == "Deleted" and state == "WIP"
        if restore:
            self._assert_can_restore_deleted_component(acc)
        else:
            self._assert_owner_or_admin(acc)
            if state == "Deleted":
                self._assert_revision_one_only(acc, label="ACC")
            if state == "QA":
                if acc.namespace is None:
                    raise ValueError(
                        f"'{acc.den}' needs a namespace before it can move to 'QA' state."
                    )
            elif state != "Deleted" and acc.namespace is None:
                raise ValueError(f"'{acc.den}' namespace required.")

        await self._repo.change_acc_state(
            acc_manifest_id=acc_manifest_id,
            state=state,
            restore_owner=restore,
            implicit_move=False,
            requester_user_id=self._requester.user.user_id,
        )
        logger.info("update acc state id=%d → %s", int(acc_manifest_id), state)
        return UpdateAccServiceResult(acc_manifest_id=int(acc_manifest_id), updates=[f"state:{state}"])

    async def discard_acc(
        self,
        *,
        acc_manifest_id: AccManifestId,
    ) -> DiscardAccServiceResult:
        """Discard a Deleted ACC and its direct related records permanently."""
        logger.info("discard acc id=%d", int(acc_manifest_id))
        acc = await self.get_acc(acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        if acc.state != "Deleted":
            raise ValueError(
                f"The ACC '{acc.den}' cannot be discarded because it is in the '{acc.state}' state. "
                "Only ACCs in the 'Deleted' state can be discarded."
            )
        if await self._repo.has_related_asccps_for_acc(acc_manifest_id):
            raise ValueError(
                f"Please discard related ASCCPs first before discarding the ACC '{acc.den}'."
            )
        if await self._repo.has_deriving_accs(acc_manifest_id):
            raise ValueError(
                f"Please discard derived ACCs first before discarding the ACC '{acc.den}'."
            )
        discarded = await self._repo.discard_acc(acc_manifest_id=acc_manifest_id)
        if not discarded:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        logger.info("discard acc id=%d → discarded", int(acc_manifest_id))
        return DiscardAccServiceResult(acc_manifest_id=int(acc_manifest_id), discarded=True)

    async def revise_acc(
        self,
        *,
        acc_manifest_id: AccManifestId,
        requested_action: Literal["revise", "amend"] | None = None,
    ) -> ReviseAccServiceResult:
        """Create a new ACC working revision from a stable ACC revision."""
        logger.info("revise acc id=%d", int(acc_manifest_id))
        acc = await self.get_acc(acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )

        self._assert_can_revise_component(acc, requested_action=requested_action)

        revised = await self._repo.revise_acc(
            acc_manifest_id=acc_manifest_id,
            requester_user_id=self._requester.user.user_id,
        )
        if not revised:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        logger.info("revise acc id=%d → revised", int(acc_manifest_id))
        return ReviseAccServiceResult(acc_manifest_id=int(acc_manifest_id), revised=True)

    async def cancel_acc(
        self,
        *,
        acc_manifest_id: AccManifestId,
    ) -> CancelAccServiceResult:
        """Cancel the current ACC revision and restore the previous stable revision."""
        logger.info("cancel acc id=%d", int(acc_manifest_id))
        acc = await self.get_acc(acc_manifest_id)
        if acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_cancel_component(acc, label="ACC")

        cancelled = await self._repo.cancel_acc(acc_manifest_id=acc_manifest_id)
        if not cancelled:
            raise LookupError(
                f"No ACC exists with manifest ID {int(acc_manifest_id)}. Please verify the identifier and try again."
        )
        logger.info("cancel acc id=%d → cancelled", int(acc_manifest_id))
        return CancelAccServiceResult(acc_manifest_id=int(acc_manifest_id), cancelled=True)

    async def change_asccp_state(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        state: CoreComponentState,
    ) -> None:
        """Change the lifecycle state of an ASCCP."""
        asccp = await self.get_asccp(asccp_manifest_id)
        if asccp is None:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_component_state_transition_allowed(asccp, state=state, label="ASCCP")
        restore = str(asccp.state or "") == "Deleted" and state == "WIP"
        if restore:
            self._assert_can_restore_deleted_component(asccp)
        else:
            self._assert_owner_or_admin(asccp)
            if state == "Deleted":
                self._assert_revision_one_only(asccp, label="ASCCP")
            elif asccp.namespace is None:
                raise ValueError(f"'{asccp.den}' namespace required.")
        await self._repo.change_asccp_state(
            asccp_manifest_id=asccp_manifest_id,
            state=state,
            restore_owner=restore,
            implicit_move=False,
            requester_user_id=self._requester.user.user_id,
        )

    async def change_bccp_state(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        state: CoreComponentState,
    ) -> None:
        """Change the lifecycle state of a BCCP."""
        bccp = await self.get_bccp(bccp_manifest_id)
        if bccp is None:
            raise LookupError(
                f"No BCCP exists with manifest ID {int(bccp_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_component_state_transition_allowed(bccp, state=state, label="BCCP")
        restore = str(bccp.state or "") == "Deleted" and state == "WIP"
        if restore:
            self._assert_can_restore_deleted_component(bccp)
        else:
            self._assert_owner_or_admin(bccp)
            if state == "Deleted":
                self._assert_revision_one_only(bccp, label="BCCP")
            elif bccp.namespace is None:
                raise ValueError(f"'{bccp.den}' namespace required.")
        await self._repo.change_bccp_state(
            bccp_manifest_id=bccp_manifest_id,
            state=state,
            restore_owner=restore,
            implicit_move=False,
            requester_user_id=self._requester.user.user_id,
        )

    async def discard_asccp(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
    ) -> DiscardAsccpServiceResult:
        """Discard a Deleted ASCCP permanently."""
        asccp = await self.get_asccp(asccp_manifest_id)
        if asccp is None:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )
        if asccp.state != "Deleted":
            raise ValueError(
                f"The ASCCP '{asccp.den}' cannot be discarded because it is in the '{asccp.state}' state. "
                "Only ASCCPs in the 'Deleted' state can be discarded."
            )
        if await self._repo.has_related_asccs_for_asccp(asccp_manifest_id):
            raise ValueError(
                f"Please discard related ASCCs first before discarding the ASCCP '{asccp.den}'."
            )
        discarded = await self._repo.discard_asccp(asccp_manifest_id=asccp_manifest_id)
        if not discarded:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )
        return DiscardAsccpServiceResult(asccp_manifest_id=int(asccp_manifest_id), discarded=True)

    async def discard_bccp(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
    ) -> DiscardBccpServiceResult:
        """Discard a Deleted BCCP permanently."""
        bccp = await self.get_bccp(bccp_manifest_id)
        if bccp is None:
            raise LookupError(
                f"No BCCP exists with manifest ID {int(bccp_manifest_id)}. Please verify the identifier and try again."
            )
        if bccp.state != "Deleted":
            raise ValueError(
                f"The BCCP '{bccp.den}' cannot be discarded because it is in the '{bccp.state}' state. "
                "Only BCCPs in the 'Deleted' state can be discarded."
            )
        if await self._repo.has_related_bccs_for_bccp(bccp_manifest_id):
            raise ValueError(
                f"Please discard related BCCs first before discarding the BCCP '{bccp.den}'."
            )
        discarded = await self._repo.discard_bccp(bccp_manifest_id=bccp_manifest_id)
        if not discarded:
            raise LookupError(
                f"No BCCP exists with manifest ID {int(bccp_manifest_id)}. Please verify the identifier and try again."
            )
        return DiscardBccpServiceResult(bccp_manifest_id=int(bccp_manifest_id), discarded=True)

    async def revise_asccp(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        requested_action: Literal["revise", "amend"] | None = None,
    ) -> ReviseAsccpServiceResult:
        """Create a new ASCCP working revision from a stable ASCCP revision."""
        asccp = await self.get_asccp(asccp_manifest_id)
        if asccp is None:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_revise_component(asccp, requested_action=requested_action, label_plural="ASCCPs")
        revised = await self._repo.revise_asccp(
            asccp_manifest_id=asccp_manifest_id,
            requester_user_id=self._requester.user.user_id,
        )
        if not revised:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )
        return ReviseAsccpServiceResult(asccp_manifest_id=int(asccp_manifest_id), revised=True)

    async def cancel_asccp(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
    ) -> CancelAsccpServiceResult:
        """Cancel the current ASCCP revision and restore the previous stable revision."""
        asccp = await self.get_asccp(asccp_manifest_id)
        if asccp is None:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_cancel_component(asccp, label="ASCCP")
        cancelled = await self._repo.cancel_asccp(asccp_manifest_id=asccp_manifest_id)
        if not cancelled:
            raise LookupError(
                f"No ASCCP exists with manifest ID {int(asccp_manifest_id)}. Please verify the identifier and try again."
            )
        return CancelAsccpServiceResult(asccp_manifest_id=int(asccp_manifest_id), cancelled=True)

    async def revise_bccp(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        requested_action: Literal["revise", "amend"] | None = None,
    ) -> ReviseBccpServiceResult:
        """Create a new BCCP working revision from a stable BCCP revision."""
        bccp = await self.get_bccp(bccp_manifest_id)
        if bccp is None:
            raise LookupError(
                f"No BCCP exists with manifest ID {int(bccp_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_revise_component(bccp, requested_action=requested_action, label_plural="BCCPs")
        revised = await self._repo.revise_bccp(
            bccp_manifest_id=bccp_manifest_id,
            requester_user_id=self._requester.user.user_id,
        )
        if not revised:
            raise LookupError(
                f"No BCCP exists with manifest ID {int(bccp_manifest_id)}. Please verify the identifier and try again."
            )
        return ReviseBccpServiceResult(bccp_manifest_id=int(bccp_manifest_id), revised=True)

    async def cancel_bccp(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
    ) -> CancelBccpServiceResult:
        """Cancel the current BCCP revision and restore the previous stable revision."""
        bccp = await self.get_bccp(bccp_manifest_id)
        if bccp is None:
            raise LookupError(
                f"No BCCP exists with manifest ID {int(bccp_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_can_cancel_component(bccp, label="BCCP")
        cancelled = await self._repo.cancel_bccp(bccp_manifest_id=bccp_manifest_id)
        if not cancelled:
            raise LookupError(
                f"No BCCP exists with manifest ID {int(bccp_manifest_id)}. Please verify the identifier and try again."
            )
        return CancelBccpServiceResult(bccp_manifest_id=int(bccp_manifest_id), cancelled=True)

    async def list(
        self,
        *,
        release_id: ReleaseId,
        types: list[str],
        limit: int,
        offset: int,
        order_by: str | None = None,
        den: str | None = None,
        tag: str | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
        owner: str | None = None,
    ) -> PaginationResponse[CoreComponentServiceResult]:
        """Get a unified list of core components for the target release scope.

        Args:
            release_id: Release identifier used to scope the query.
            types: Requested core-component types.
            limit: Maximum number of items to return.
            offset: Zero-based index of the first item in the page.
            order_by: Sort expression for the result set.

        Returns:
            Result of the operation.
        """
        logger.info("list core_components release_id=%d types=%s limit=%d offset=%d", int(release_id), types, limit, offset)
        pagination = PaginationParams.from_query(
            limit=limit,
            offset=offset,
            order_by=order_by,
            allowed_sort_columns=self._ORDER_BY_ALLOWED,
        )
        release = await self._release_service.get(release_id)
        if release is None:
            raise LookupError(f"No release exists with ID {int(release_id)}. Please verify the identifier and try again.")

        valid_types = {"ACC", "ASCCP", "BCCP"}
        invalid = [t for t in types if t not in valid_types]
        if invalid:
            raise ValueError(
                f"Invalid component types: {', '.join(invalid)}. Allowed values are: ACC, ASCCP, BCCP. "
                "Please choose only supported component types and try again."
            )

        dependent_release_ids = await self._release_service.get_dependent_releases(release_id)
        tag_names = _parse_tag_filter(tag)
        included_owner_login_ids, excluded_owner_login_ids = parse_owner_filter(owner)
        total, rows = await self._repo.list(
            release_id=release_id,
            dependent_release_ids=dependent_release_ids,
            types=types,
            limit=pagination.limit,
            offset=pagination.offset,
            sorts=[(s.column, s.direction.upper()) for s in pagination.sorts],
            den=den,
            tag_names=tag_names,
            creation_timestamp_before=created_on.before if created_on else None,
            creation_timestamp_after=created_on.after if created_on else None,
            last_update_timestamp_before=last_updated_on.before if last_updated_on else None,
            last_update_timestamp_after=last_updated_on.after if last_updated_on else None,
            included_owner_login_ids=included_owner_login_ids,
            excluded_owner_login_ids=excluded_owner_login_ids,
        )
        # Repository intentionally returns only AppUser IDs. We "join" AppUser at
        # service layer via one batched lookup so account data can be cached.
        user_ids = sorted(
            {
                user_id
                for raw in rows
                for user_id in (raw.owner_user_id, raw.created_by, raw.last_updated_by)
            },
            key=int,
        )
        users_by_id = await load_users_by_ids(self._account_service_repo, user_ids)
        items = []
        for raw in rows:
            owner, created_who, updated_who = await self._enrich_audit_users(
                owner_user_id=int(raw.owner_user_id),
                created_by=int(raw.created_by),
                last_updated_by=int(raw.last_updated_by),
                users_by_id=users_by_id,
            )
            items.append(
                CoreComponentServiceResult(
                    component_type=raw.component_type,
                    manifest_id=raw.manifest_id,
                    component_id=raw.component_id,
                    guid=raw.guid,
                    den=raw.den,
                    name=raw.name,
                    definition=raw.definition,
                    definition_source=raw.definition_source,
                    is_deprecated=raw.is_deprecated,
                    state=raw.state,
                    namespace=to_dataclass(NamespaceSummaryServiceRecord, raw.namespace),
                    library=to_dataclass(LibrarySummaryServiceRecord, raw.library),
                    release=to_dataclass(ReleaseSummaryServiceRecord, raw.release),
                    log=to_dataclass(LogSummaryServiceRecord, raw.log) if raw.log is not None else None,
                    owner=owner,
                    created=WhoAndWhen(who=created_who, when=raw.creation_timestamp),
                    last_updated=WhoAndWhen(who=updated_who, when=raw.last_update_timestamp),
                    tag=raw.tag,
                )
            )
        logger.info("list core_components release_id=%d → %d/%d", int(release_id), len(items), total)
        return PaginationResponse(items=items, total=total, limit=pagination.limit, offset=pagination.offset)

    async def get_acc(self, acc_manifest_id: AccManifestId) -> GetAccServiceResult | None:
        """Get ACC details by manifest ID.

        Args:
            acc_manifest_id: ACC manifest identifier.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get_acc(acc_manifest_id)
        if row is None:
            logger.info("get acc id=%d → not found", int(acc_manifest_id))
            return None
        owner, created_who, updated_who = await self._enrich_audit_users(
            owner_user_id=int(row.owner_user_id),
            created_by=int(row.created_by),
            last_updated_by=int(row.last_updated_by),
        )
        logger.info("get acc id=%d → found", int(acc_manifest_id))
        payload = GetAccServiceResult(
            acc_manifest_id=row.acc_manifest_id,
            acc_id=row.acc_id,
            base_acc=to_dataclass(BaseAccSummaryServiceRecord, row.base_acc) if row.base_acc is not None else None,
            relationships=[
                self._to_ascc_relationship_service_record(relationship)
                if getattr(relationship, "component_type", None) == "ASCC"
                else self._to_bcc_relationship_service_record(relationship)
                for relationship in row.relationships
            ],
            guid=row.guid,
            den=row.den,
            object_class_term=row.object_class_term,
            definition=row.definition,
            definition_source=row.definition_source,
            object_class_qualifier=row.object_class_qualifier,
            component_type=row.component_type,
            is_abstract=row.is_abstract,
            is_deprecated=row.is_deprecated,
            state=row.state,
            namespace=to_dataclass(NamespaceSummaryServiceRecord, row.namespace),
            library=to_dataclass(LibrarySummaryServiceRecord, row.library),
            release=to_dataclass(ReleaseSummaryServiceRecord, row.release),
            tags=[to_dataclass(TagSummaryServiceRecord, tag) for tag in row.tags],
            log=to_dataclass(LogSummaryServiceRecord, row.log) if row.log is not None else None,
            owner=owner,
            created=WhoAndWhen(
                who=created_who,
                when=row.creation_timestamp,
            ),
            last_updated=WhoAndWhen(
                who=updated_who,
                when=row.last_update_timestamp,
            ),
        )
        return payload

    async def get_asccp(self, asccp_manifest_id: AsccpManifestId) -> GetAsccpServiceResult | None:
        """Get ASCCP details by manifest ID.

        Args:
            asccp_manifest_id: ASCCP manifest identifier.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get_asccp(asccp_manifest_id)
        if row is None:
            logger.info("get asccp id=%d → not found", int(asccp_manifest_id))
            return None
        owner, created_who, updated_who = await self._enrich_audit_users(
            owner_user_id=int(row.owner_user_id),
            created_by=int(row.created_by),
            last_updated_by=int(row.last_updated_by),
        )
        logger.info("get asccp id=%d → found", int(asccp_manifest_id))
        payload = GetAsccpServiceResult(
            asccp_manifest_id=row.asccp_manifest_id,
            asccp_id=row.asccp_id,
            role_of_acc=to_dataclass(BaseAccSummaryServiceRecord, row.role_of_acc),
            guid=row.guid,
            den=row.den,
            property_term=row.property_term,
            definition=row.definition,
            definition_source=row.definition_source,
            reusable_indicator=row.reusable_indicator,
            is_nillable=row.is_nillable,
            is_deprecated=row.is_deprecated,
            state=row.state,
            namespace=to_dataclass(NamespaceSummaryServiceRecord, row.namespace),
            library=to_dataclass(LibrarySummaryServiceRecord, row.library),
            release=to_dataclass(ReleaseSummaryServiceRecord, row.release),
            tags=[to_dataclass(TagSummaryServiceRecord, tag) for tag in row.tags],
            log=to_dataclass(LogSummaryServiceRecord, row.log) if row.log is not None else None,
            owner=owner,
            created=WhoAndWhen(
                who=created_who,
                when=row.creation_timestamp,
            ),
            last_updated=WhoAndWhen(
                who=updated_who,
                when=row.last_update_timestamp,
            ),
        )
        return payload

    async def get_bccp(self, bccp_manifest_id: BccpManifestId) -> GetBccpServiceResult | None:
        """Get BCCP details by manifest ID.

        Args:
            bccp_manifest_id: BCCP manifest identifier.

        Returns:
            Result of the operation.
        """
        row = await self._repo.get_bccp(bccp_manifest_id)
        if row is None:
            logger.info("get bccp id=%d → not found", int(bccp_manifest_id))
            return None
        owner, created_who, updated_who = await self._enrich_audit_users(
            owner_user_id=int(row.owner_user_id),
            created_by=int(row.created_by),
            last_updated_by=int(row.last_updated_by),
        )
        logger.info("get bccp id=%d → found", int(bccp_manifest_id))
        payload = GetBccpServiceResult(
            bccp_manifest_id=row.bccp_manifest_id,
            bccp_id=row.bccp_id,
            bdt=to_dataclass(DataTypeSummaryServiceRecord, row.bdt),
            guid=row.guid,
            den=row.den,
            property_term=row.property_term,
            representation_term=row.representation_term,
            definition=row.definition,
            definition_source=row.definition_source,
            is_nillable=row.is_nillable,
            value_constraint=to_dataclass(ValueConstraintServiceRecord, row.value_constraint)
            if row.value_constraint is not None
            else None,
            is_deprecated=row.is_deprecated,
            state=row.state,
            namespace=to_dataclass(NamespaceSummaryServiceRecord, row.namespace),
            library=to_dataclass(LibrarySummaryServiceRecord, row.library),
            release=to_dataclass(ReleaseSummaryServiceRecord, row.release),
            tags=[to_dataclass(TagSummaryServiceRecord, tag) for tag in row.tags],
            log=to_dataclass(LogSummaryServiceRecord, row.log) if row.log is not None else None,
            owner=owner,
            created=WhoAndWhen(
                who=created_who,
                when=row.creation_timestamp,
            ),
            last_updated=WhoAndWhen(
                who=updated_who,
                when=row.last_update_timestamp,
            ),
        )
        return payload

    def _assert_can_create_core_components(self, *, release_num: str) -> None:
        """Validate that the requester can create ACCs in the target release branch."""
        roles = set(self._requester.user.roles)
        if "Developer" in roles:
            if release_num != "Working":
                raise ValueError(
                    "It only allows to create the component in 'Working' branch for developers."
                )
            return
        if "End-User" in roles:
            if release_num == "Working":
                raise ValueError(
                    "It only allows to create the component in non-'Working' branch for end-users."
                )
            return
        raise PermissionError("A recognized application role is required to create ACCs.")

    def _assert_can_update_wip_core_component(
        self,
        component: GetAccServiceResult | GetAsccpServiceResult | GetBccpServiceResult,
        *,
        label: str,
    ) -> None:
        """Validate owner/admin permissions for mutable WIP core-component edits."""
        if component.state != "WIP":
            raise ValueError(
                f"The {label} '{component.den}' cannot be updated because it is in the '{component.state}' state. "
                f"Only {label}s in the 'WIP' state can be updated."
            )
        self._assert_owner_or_admin(component)

    def _assert_can_transfer_wip_component(
        self,
        component: GetAccServiceResult | GetAsccpServiceResult | GetBccpServiceResult,
        *,
        label: str,
    ) -> None:
        """Validate owner/admin permissions for WIP ownership transfer."""
        if component.state != "WIP":
            raise ValueError(
                f"The {label} '{component.den}' cannot be transferred because it is in the '{component.state}' state. "
                f"Only {label}s in the 'WIP' state can be transferred."
            )
        self._assert_owner_or_admin(component)

    def _assert_can_restore_deleted_component(
        self,
        component: GetAccServiceResult | GetAsccpServiceResult | GetBccpServiceResult,
    ) -> None:
        """Validate role-based restore permissions for Deleted components."""
        roles = set(self._requester.user.roles)
        release_num = str(component.release.release_num or "")
        if release_num == "Working":
            if "Developer" not in roles:
                raise PermissionError(
                    "It only allows to restore the component in 'Working' branch for developers."
                )
            return
        if "End-User" not in roles:
            raise PermissionError(
                "It only allows to restore the component in non-'Working' branch for end-users."
            )

    def _assert_same_role_family_as_component_owner(
        self,
        component: GetAccServiceResult | GetAsccpServiceResult | GetBccpServiceResult,
    ) -> None:
        """Require the requester and owner to both be developer-side or both end-user-side."""
        owner_roles = set(component.owner.roles)
        owner_is_developer = "Developer" in owner_roles
        if self._requester_is_developer() != owner_is_developer:
            raise ValueError("It only allows to revise the component for users in the same roles.")

    async def _resolve_create_acc_release_id(
        self,
        *,
        release_id: ReleaseId,
    ) -> tuple[ReleaseId, ReleaseServiceResult]:
        """Resolve the explicit release for ACC creation."""
        explicit_release = await self._release_service.get(release_id)
        if explicit_release is None:
            raise LookupError(f"No release exists with ID {int(release_id)}. Please verify the identifier and try again.")
        return release_id, explicit_release

    @staticmethod
    def _assert_release_is_published(release: ReleaseServiceResult) -> None:
        """Require the target release to be in Published state."""
        if str(release.state) != "Published":
            raise ValueError(f"'{release.state}' release cannot be modified.")

    def _assert_owner_or_admin(
        self,
        component: GetAccServiceResult | GetAsccpServiceResult | GetBccpServiceResult,
    ) -> None:
        """Require the requester to own the component or be an admin."""
        if not self._is_admin() and int(component.owner.user_id) != int(self._requester.user.user_id):
            raise PermissionError("It only allows to modify the core component by the owner.")

    def _assert_component_state_transition_allowed(
        self,
        component: GetAccServiceResult | GetAsccpServiceResult | GetBccpServiceResult,
        *,
        state: CoreComponentState,
        label: str,
    ) -> None:
        """Validate branch-specific component state transitions."""
        current_state = str(component.state or "")
        release_num = str(component.release.release_num or "")
        allowed_transitions = self._acc_state_transitions_for_release(release_num=release_num)
        if current_state not in allowed_transitions:
            branch_label = "'Working'" if release_num == "Working" else "non-'Working'"
            raise ValueError(
                f"The {label} is in '{current_state}' state, which cannot be changed by this service for {branch_label} releases."
            )
        if state not in allowed_transitions[current_state]:  # type: ignore[index]
            raise ValueError(
                f"The core component in '{current_state}' state cannot move to '{state}' state."
            )

    def _assert_revision_one_only(
        self,
        component: GetAccServiceResult | GetAsccpServiceResult | GetBccpServiceResult,
        *,
        label: str,
    ) -> None:
        """Require revision 1 before allowing Deleted state."""
        revision_num = int(component.log.revision_num) if component.log is not None else 1
        if revision_num != 1:
            raise ValueError(
                f"'{component.den}' can't be marked as deleted because it is a later revision. "
                "Only the first revision can be deleted. If you want to undo this revised version, "
                f"please cancel the {label} instead."
            )

    def _assert_can_revise_component(
        self,
        component: GetAccServiceResult | GetAsccpServiceResult | GetBccpServiceResult,
        *,
        requested_action: Literal["revise", "amend"] | None = None,
        label_plural: str = "ACCs",
    ) -> None:
        """Validate revise rules for ACC/ASCCP/BCCP revisions."""
        expected_action: Literal["revise", "amend"] = "revise" if self._requester_is_developer() else "amend"
        if requested_action is not None and requested_action != expected_action:
            if expected_action == "revise":
                raise PermissionError(f"Developer-side {label_plural} must use revise.")
            raise PermissionError(f"End-user {label_plural} must use amend.")
        if self._requester_is_developer():
            if component.state != "Published":
                raise ValueError("Only the core component in 'Published' state can be revised.")
        else:
            if component.state != "Production":
                raise ValueError("Only the core component in 'Production' state can be amended.")
        self._assert_can_access_component_revision_branch(component)
        self._assert_same_role_family_as_component_owner(component)

    def _assert_can_cancel_component(
        self,
        component: GetAccServiceResult | GetAsccpServiceResult | GetBccpServiceResult,
        *,
        label: str,
    ) -> None:
        """Validate cancel rules for ACC/ASCCP/BCCP revisions."""
        if component.state != "WIP":
            raise ValueError(
                f"The {label} '{component.den}' cannot be cancelled because it is in the '{component.state}' state. "
                f"Only {label}s in the 'WIP' state can be cancelled."
            )
        self._assert_can_access_component_revision_branch(component)
        self._assert_same_role_family_as_component_owner(component)

    def _assert_can_access_component_revision_branch(
        self,
        component: GetAccServiceResult | GetAsccpServiceResult | GetBccpServiceResult,
    ) -> None:
        """Validate revise/cancel branch rules for ACC/ASCCP/BCCP."""
        release_num = str(component.release.release_num or "")
        if self._requester_is_developer():
            if release_num != "Working":
                raise ValueError("It only allows to revise the component in 'Working' branch for developers.")
            return
        if release_num == "Working":
            raise ValueError("It only allows to amend the component in non-'Working' branches for end-users.")


    @staticmethod
    def _derive_acc_type(component_type: OagisComponentType) -> str:
        """Derive persisted ACC type from the selected OAGIS component type.

        connectCenter's extension-specific flows persist `Extension` for extension-like
        component types and `Default` for the regular authoring path.
        """
        if component_type in {"Extension", "UserExtensionGroup"}:
            return "Extension"
        return "Default"

    @staticmethod
    def _derive_asccp_type_for_role_acc(component_type: int | None) -> str:
        """Derive persisted ASCCP type from the role ACC's OAGIS component type."""
        if component_type in {
            OAGIS_COMPONENT_TYPE_VALUES["Extension"],
            OAGIS_COMPONENT_TYPE_VALUES["UserExtensionGroup"],
        }:
            return "Extension"
        return "Default"

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

    @staticmethod
    def _acc_base_manifest_id(acc: GetAccServiceResult) -> int | None:
        """Return the manifest ID of the ACC's current base, if any."""
        if acc.base_acc is None:
            return None
        return int(acc.base_acc.acc_manifest_id)

    def _acc_state_transitions_for_release(
        self,
        *,
        release_num: str,
    ) -> dict[CoreComponentState, set[CoreComponentState]]:
        """Return the ACC state-transition graph for the release branch."""
        if release_num == "Working":
            return WORKING_BRANCH_CORE_COMPONENT_STATE_TRANSITIONS
        return NON_WORKING_BRANCH_CORE_COMPONENT_STATE_TRANSITIONS

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

    async def _assert_no_acc_base_cycle(
        self,
        *,
        acc_manifest_id: AccManifestId,
        based_acc: GetAccServiceResult,
    ) -> None:
        """Ensure the selected base ACC does not create an inheritance cycle."""
        visited: set[int] = set()
        current: GetAccServiceResult | None = based_acc
        while current is not None:
            current_acc_manifest_id = int(current.acc_manifest_id)
            if current_acc_manifest_id == int(acc_manifest_id):
                raise ValueError("The selected base ACC would create a cycle in the ACC inheritance chain.")
            if current_acc_manifest_id in visited:
                break
            visited.add(current_acc_manifest_id)
            next_base_manifest_id = self._acc_base_manifest_id(current)
            if next_base_manifest_id is None:
                break
            current = await self.get_acc(AccManifestId(next_base_manifest_id))
            if current is None:
                raise LookupError(
                    f"No ACC exists with manifest ID {next_base_manifest_id} in the base ACC chain. "
                    "Please verify the identifier and try again."
                )

    async def _assert_no_acc_base_conflicts(
        self,
        *,
        acc: GetAccServiceResult,
        based_acc: GetAccServiceResult,
    ) -> None:
        """Ensure the selected base chain does not duplicate direct ASCCP/BCCP associations."""
        asccp_manifest_ids = {
            int(self._relationship_target_manifest_id(relationship))
            for relationship in acc.relationships
            if getattr(relationship, "component_type", None) == "ASCC"
        }
        bccp_manifest_ids = {
            int(self._relationship_target_manifest_id(relationship))
            for relationship in acc.relationships
            if getattr(relationship, "component_type", None) == "BCC"
        }

        visited: set[int] = set()
        current: GetAccServiceResult | None = based_acc
        while current is not None:
            current_acc_manifest_id = int(current.acc_manifest_id)
            if current_acc_manifest_id in visited:
                break
            visited.add(current_acc_manifest_id)

            conflict_asccp = sorted(
                {
                    self._relationship_target_den(relationship)
                    for relationship in current.relationships
                    if getattr(relationship, "component_type", None) == "ASCC"
                    and int(self._relationship_target_manifest_id(relationship)) in asccp_manifest_ids
                }
            )
            if conflict_asccp:
                noun = "is" if len(conflict_asccp) == 1 else "are"
                raise ValueError(
                    f"There {noun} conflict{'s' if len(conflict_asccp) != 1 else ''} in ASCCPs between "
                    f"the current ACC and the base ACC [{', '.join(conflict_asccp)}]"
                )

            conflict_bccp = sorted(
                {
                    self._relationship_target_den(relationship)
                    for relationship in current.relationships
                    if getattr(relationship, "component_type", None) == "BCC"
                    and int(self._relationship_target_manifest_id(relationship)) in bccp_manifest_ids
                }
            )
            if conflict_bccp:
                noun = "is" if len(conflict_bccp) == 1 else "are"
                raise ValueError(
                    f"There {noun} conflict{'s' if len(conflict_bccp) != 1 else ''} in BCCPs between "
                    f"the current ACC and the base ACC [{', '.join(conflict_bccp)}]"
                )

            next_base_manifest_id = self._acc_base_manifest_id(current)
            if next_base_manifest_id is None:
                break
            current = await self.get_acc(AccManifestId(next_base_manifest_id))
            if current is None:
                raise LookupError(
                    f"No ACC exists with manifest ID {next_base_manifest_id} in the base ACC chain. "
                    "Please verify the identifier and try again."
                )

    async def _collect_flattened_property_terms(
        self,
        acc: GetAccServiceResult,
        *,
        visited_acc_manifest_ids: set[int] | None = None,
    ) -> list[_FlattenedAccPropertyTerm]:
        """Collect property terms that would appear in flattened BIE output for an ACC."""
        visited = visited_acc_manifest_ids or set()
        acc_manifest_id = int(acc.acc_manifest_id)
        if acc_manifest_id in visited:
            return []
        visited.add(acc_manifest_id)

        collected: list[_FlattenedAccPropertyTerm] = []
        for relationship in acc.relationships:
            component_type = getattr(relationship, "component_type", None)
            if component_type == "ASCC":
                asccp = await self.get_asccp(AsccpManifestId(int(relationship.to_asccp.asccp_manifest_id)))
                if asccp is None:
                    continue
                collected.extend(await self._collect_flattened_property_terms_from_asccp(asccp, visited_acc_manifest_ids=visited))
                continue
            if component_type == "BCC":
                to_bccp = relationship.to_bccp
                property_term = to_bccp.property_term
                bccp_den = to_bccp.den
                if property_term:
                    collected.append(
                        _FlattenedAccPropertyTerm(
                            property_term=property_term,
                            source=f"BCCP '{bccp_den}'",
                        )
                    )

        next_base_manifest_id = self._acc_base_manifest_id(acc)
        if next_base_manifest_id is not None:
            base_acc = await self.get_acc(AccManifestId(next_base_manifest_id))
            if base_acc is None:
                raise LookupError(
                    f"No ACC exists with manifest ID {next_base_manifest_id} in the base ACC chain. "
                    "Please verify the identifier and try again."
                )
            collected.extend(await self._collect_flattened_property_terms(base_acc, visited_acc_manifest_ids=visited))

        return collected

    async def _collect_flattened_property_terms_from_asccp(
        self,
        asccp: GetAsccpServiceResult,
        *,
        visited_acc_manifest_ids: set[int] | None = None,
    ) -> list[_FlattenedAccPropertyTerm]:
        """Collect flattened property terms contributed by an ASCCP in BIE expressions."""
        if asccp.role_of_acc is None:
            return []

        role_of_acc = await self.get_acc(AccManifestId(int(asccp.role_of_acc.acc_manifest_id)))
        if role_of_acc is None:
            raise LookupError(
                f"No ACC exists with manifest ID {int(asccp.role_of_acc.acc_manifest_id)}. "
                "Please verify the identifier and try again."
            )

        if int(role_of_acc.component_type or -1) in {
            OAGIS_COMPONENT_TYPE_VALUES["SemanticGroup"],
            OAGIS_COMPONENT_TYPE_VALUES["UserExtensionGroup"],
        }:
            return await self._collect_flattened_property_terms(
                role_of_acc,
                visited_acc_manifest_ids=visited_acc_manifest_ids,
            )

        if asccp.property_term:
            return [
                _FlattenedAccPropertyTerm(
                    property_term=asccp.property_term,
                    source=f"ASCCP '{asccp.den or asccp.property_term}'",
                )
            ]
        return []

    async def _collect_acc_warnings_for_asccp_preview(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        current_asccp: GetAsccpServiceResult,
        preview_asccp: GetAsccpServiceResult,
        context_action: str,
    ) -> list[str]:
        """Collect structural warnings for ACCs that reuse an ASCCP."""
        warnings: list[str] = []
        current_contribution = await self._collect_flattened_property_terms_from_asccp(current_asccp)
        preview_contribution = await self._collect_flattened_property_terms_from_asccp(preview_asccp)
        impacted_acc_manifest_ids = await self._repo.list_owner_acc_manifest_ids_by_asccp_manifest(asccp_manifest_id)
        for acc_manifest_id in impacted_acc_manifest_ids:
            acc = await self.get_acc(acc_manifest_id)
            if acc is None:
                continue
            remaining_terms = self._subtract_flattened_terms(
                source_terms=await self._collect_flattened_property_terms(acc),
                terms_to_remove=current_contribution,
            )
            warnings.extend(
                self._build_flattened_duplicate_warnings(
                    current_terms=remaining_terms,
                    incoming_terms=preview_contribution,
                    context_label=f"{context_action} used by '{acc.den}'",
                )
            )
        return list(dict.fromkeys(warnings))

    async def _collect_inherited_sequence_preview_items(
        self,
        acc: GetAccServiceResult,
        *,
        visited_acc_manifest_ids: set[int] | None = None,
    ) -> list[_SequencePreviewItem]:
        """Collect inherited/base-side sequence items in effective ACC order."""
        visited = visited_acc_manifest_ids or set()
        acc_manifest_id = int(acc.acc_manifest_id)
        if acc_manifest_id in visited:
            return []
        visited.add(acc_manifest_id)

        collected: list[_SequencePreviewItem] = []
        next_base_manifest_id = self._acc_base_manifest_id(acc)
        if next_base_manifest_id is not None:
            base_acc = await self.get_acc(AccManifestId(next_base_manifest_id))
            if base_acc is None:
                raise LookupError(
                    f"No ACC exists with manifest ID {next_base_manifest_id} in the base ACC chain. "
                    "Please verify the identifier and try again."
                )
            collected.extend(
                await self._collect_inherited_sequence_preview_items(
                    base_acc,
                    visited_acc_manifest_ids=visited,
                )
            )

        collected.extend(
            self._to_sequence_preview_item(relationship)
            for relationship in acc.relationships
        )
        return collected

    @staticmethod
    def _build_flattened_duplicate_warnings(
        *,
        current_terms: list[_FlattenedAccPropertyTerm],
        incoming_terms: list[_FlattenedAccPropertyTerm],
        context_label: str,
    ) -> list[str]:
        """Build user-facing warnings for duplicate flattened property terms."""
        if not current_terms or not incoming_terms:
            return []

        current_sources_by_term: dict[str, list[str]] = {}
        for item in current_terms:
            current_sources_by_term.setdefault(item.property_term, []).append(item.source)

        incoming_sources_by_term: dict[str, list[str]] = {}
        for item in incoming_terms:
            incoming_sources_by_term.setdefault(item.property_term, []).append(item.source)

        duplicate_terms = sorted(set(current_sources_by_term) & set(incoming_sources_by_term))
        warnings: list[str] = []
        for property_term in duplicate_terms:
            existing_sources = ", ".join(sorted(set(current_sources_by_term[property_term])))
            incoming_sources = ", ".join(sorted(set(incoming_sources_by_term[property_term])))
            warnings.append(
                f"{context_label} may repeat the field name '{property_term}' in generated business content "
                f"(existing: {existing_sources}; incoming: {incoming_sources})."
            )
        return warnings

    @staticmethod
    def _subtract_flattened_terms(
        *,
        source_terms: list[_FlattenedAccPropertyTerm],
        terms_to_remove: list[_FlattenedAccPropertyTerm],
    ) -> list[_FlattenedAccPropertyTerm]:
        """Remove a multiset of flattened terms from a source term list."""
        remaining = list(source_terms)
        for removable in terms_to_remove:
            for index, current in enumerate(remaining):
                if current == removable:
                    del remaining[index]
                    break
        return remaining

    @staticmethod
    def _raise_for_structural_warnings(
        *,
        warnings: list[str],
        allow_warnings: bool,
    ) -> None:
        """Reject structural warnings unless the caller has explicitly confirmed them."""
        if not warnings or allow_warnings:
            return
        raise ValueError(
            "This change may create structural issues in ACCs that already use this component. "
            f"{warnings[0]}"
        )

    @staticmethod
    def _build_sequence_ambiguity_warnings_for_sequence(
        *,
        context_label: str,
        ordered_items: list[_SequencePreviewItem],
    ) -> list[str]:
        """Build warnings for any repeating associations that remain before later sequence items."""
        if len(ordered_items) <= 1:
            return []

        warnings: list[str] = []
        for item in ordered_items[:-1]:
            if item.cardinality_max not in {-1} and item.cardinality_max <= 1:
                continue
            warnings.append(
                f"{context_label} leaves repeating field '{item.relationship_label}' before later fields in sequence, "
                "which can make order-sensitive XML output harder to interpret."
            )
        return warnings

    @staticmethod
    def _preview_sequence_for_add(
        *,
        relationships: list[AsccRelationshipServiceRecord | BccRelationshipServiceRecord],
        new_item: _SequencePreviewItem,
        index: int,
    ) -> list[_SequencePreviewItem]:
        """Preview the final sequence order for add operations driven by `index`."""
        ordered = [CoreComponentService._to_sequence_preview_item(relationship) for relationship in relationships]
        CoreComponentService._assert_valid_sequence_index(index=index, sequence_length=len(ordered))
        final_index = len(ordered) if index < 0 else int(index)
        return ordered[:final_index] + [new_item] + ordered[final_index:]

    @staticmethod
    def _resolve_add_sequence_index(
        *,
        relationships: list[AsccRelationshipServiceRecord | BccRelationshipServiceRecord],
        index: int | None,
        after_ascc_manifest_id: AsccManifestId | None,
        after_bcc_manifest_id: BccManifestId | None,
        before_ascc_manifest_id: AsccManifestId | None,
        before_bcc_manifest_id: BccManifestId | None,
        default_to_end: bool,
    ) -> int:
        """Resolve add-placement inputs into a concrete zero-based insertion index."""
        provided = sum(
            value is not None
            for value in (
                index,
                after_ascc_manifest_id,
                after_bcc_manifest_id,
                before_ascc_manifest_id,
                before_bcc_manifest_id,
            )
        )
        if provided > 1:
            raise ValueError("Provide only one of `index`, `after_*`, or `before_*`.")

        ordered = list(relationships)
        if index is not None:
            CoreComponentService._assert_valid_sequence_index(index=index, sequence_length=len(ordered))
            return len(ordered) if index < 0 else int(index)

        if after_ascc_manifest_id is not None:
            position = CoreComponentService._find_sequence_reference_index(
                ordered=ordered,
                target_component_type="ASCC",
                target_manifest_id=int(after_ascc_manifest_id),
            )
            return position + 1

        if after_bcc_manifest_id is not None:
            position = CoreComponentService._find_sequence_reference_index(
                ordered=ordered,
                target_component_type="BCC",
                target_manifest_id=int(after_bcc_manifest_id),
            )
            return position + 1

        if before_ascc_manifest_id is not None:
            return CoreComponentService._find_sequence_reference_index(
                ordered=ordered,
                target_component_type="ASCC",
                target_manifest_id=int(before_ascc_manifest_id),
            )

        if before_bcc_manifest_id is not None:
            return CoreComponentService._find_sequence_reference_index(
                ordered=ordered,
                target_component_type="BCC",
                target_manifest_id=int(before_bcc_manifest_id),
            )

        return len(ordered) if default_to_end else 0

    @staticmethod
    def _resolve_attribute_bcc_add_index(
        relationships: list[AsccRelationshipServiceRecord | BccRelationshipServiceRecord],
        index: int,
    ) -> int:
        """Resolve an attribute BCC add index within the leading attribute block.

        Attribute BCCs live in the leading block of the ACC sequence.
        - indexes inside that block are honored directly
        - `-1` or any index beyond that block append to the end of the attribute block
        """
        attribute_count = 0
        for relationship in relationships:
            if getattr(relationship, "component_type", None) != "BCC":
                break
            if getattr(relationship, "entity_type", None) != "Attribute":
                break
            attribute_count += 1
        CoreComponentService._assert_valid_sequence_index(index=index, sequence_length=len(relationships))
        if index < 0 or index > attribute_count:
            return attribute_count
        return int(index)

    @staticmethod
    def _preview_sequence_for_move(
        *,
        relationships: list[AsccRelationshipServiceRecord | BccRelationshipServiceRecord],
        item_component_type: str,
        item_manifest_id: int,
        index: int | None,
        after_ascc_manifest_id: AsccManifestId | None,
        after_bcc_manifest_id: BccManifestId | None,
        before_ascc_manifest_id: AsccManifestId | None,
        before_bcc_manifest_id: BccManifestId | None,
    ) -> list[_SequencePreviewItem]:
        """Preview the final sequence order for move operations."""
        final_index, _ = CoreComponentService._preview_move_sequence_position(
            relationships=relationships,
            item_component_type=item_component_type,
            item_manifest_id=item_manifest_id,
            index=index,
            after_ascc_manifest_id=after_ascc_manifest_id,
            after_bcc_manifest_id=after_bcc_manifest_id,
            before_ascc_manifest_id=before_ascc_manifest_id,
            before_bcc_manifest_id=before_bcc_manifest_id,
        )
        moving_item = next(
            CoreComponentService._to_sequence_preview_item(relationship)
            for relationship in relationships
            if getattr(relationship, "component_type", None) == item_component_type
            and int(getattr(relationship, f"{item_component_type.lower()}_manifest_id")) == int(item_manifest_id)
        )
        ordered = [
            CoreComponentService._to_sequence_preview_item(relationship)
            for relationship in relationships
            if not (
                getattr(relationship, "component_type", None) == item_component_type
                and int(getattr(relationship, f"{item_component_type.lower()}_manifest_id")) == int(item_manifest_id)
            )
        ]
        return ordered[:final_index] + [moving_item] + ordered[final_index:]

    @staticmethod
    def _preview_move_sequence_position(
        *,
        relationships: list[AsccRelationshipServiceRecord | BccRelationshipServiceRecord],
        item_component_type: str,
        item_manifest_id: int,
        index: int | None,
        after_ascc_manifest_id: AsccManifestId | None,
        after_bcc_manifest_id: BccManifestId | None,
        before_ascc_manifest_id: AsccManifestId | None,
        before_bcc_manifest_id: BccManifestId | None,
    ) -> tuple[int, int]:
        """Preview the final index and sequence length for move operations."""
        ordered = [
            relationship
            for relationship in relationships
            if not (
                getattr(relationship, "component_type", None) == item_component_type
                and int(getattr(relationship, f"{item_component_type.lower()}_manifest_id")) == int(item_manifest_id)
            )
        ]
        if index is not None:
            CoreComponentService._assert_valid_sequence_index(index=index, sequence_length=len(ordered))
            return (len(ordered) if index < 0 else int(index)), len(ordered) + 1

        if after_ascc_manifest_id is not None:
            position = CoreComponentService._find_sequence_reference_index(
                ordered=ordered,
                target_component_type="ASCC",
                target_manifest_id=int(after_ascc_manifest_id),
            )
            return position + 1, len(ordered) + 1

        if after_bcc_manifest_id is not None:
            position = CoreComponentService._find_sequence_reference_index(
                ordered=ordered,
                target_component_type="BCC",
                target_manifest_id=int(after_bcc_manifest_id),
            )
            return position + 1, len(ordered) + 1

        if before_ascc_manifest_id is not None:
            position = CoreComponentService._find_sequence_reference_index(
                ordered=ordered,
                target_component_type="ASCC",
                target_manifest_id=int(before_ascc_manifest_id),
            )
            return position, len(ordered) + 1

        if before_bcc_manifest_id is not None:
            position = CoreComponentService._find_sequence_reference_index(
                ordered=ordered,
                target_component_type="BCC",
                target_manifest_id=int(before_bcc_manifest_id),
            )
            return position, len(ordered) + 1

        return 0, len(ordered) + 1

    @staticmethod
    def _to_sequence_preview_item(
        relationship: AsccRelationshipServiceRecord | BccRelationshipServiceRecord,
    ) -> _SequencePreviewItem:
        """Convert an ACC relationship into a sequence-preview item."""
        component_type = str(getattr(relationship, "component_type", ""))
        manifest_id = int(getattr(relationship, f"{component_type.lower()}_manifest_id"))
        return _SequencePreviewItem(
            component_type=component_type,
            manifest_id=manifest_id,
            relationship_label=CoreComponentService._relationship_target_den(relationship),
            cardinality_max=int(relationship.cardinality_max),
        )

    @staticmethod
    def _find_sequence_reference_index(
        *,
        ordered: list[AsccRelationshipServiceRecord | BccRelationshipServiceRecord],
        target_component_type: str,
        target_manifest_id: int,
    ) -> int:
        """Find the index of a target ASCC/BCC within a filtered ACC relationship sequence."""
        for index, relationship in enumerate(ordered):
            if getattr(relationship, "component_type", None) != target_component_type:
                continue
            relationship_manifest_id = int(getattr(relationship, f"{target_component_type.lower()}_manifest_id"))
            if relationship_manifest_id == int(target_manifest_id):
                return index
        raise LookupError(f"No {target_component_type} exists with manifest ID {target_manifest_id} in the target ACC.")

    @staticmethod
    def _resolve_after_from_position(
        *,
        relationships: list[AsccRelationshipServiceRecord | BccRelationshipServiceRecord],
        item_component_type: str,
        item_manifest_id: int,
        index: int,
    ) -> tuple[AsccManifestId | None, BccManifestId | None]:
        """Convert a zero-based target index into an `after` association reference."""
        ordered = [
            relationship
            for relationship in relationships
            if not (
                getattr(relationship, "component_type", None) == item_component_type
                and int(getattr(relationship, f"{item_component_type.lower()}_manifest_id")) == int(item_manifest_id)
            )
        ]
        CoreComponentService._assert_valid_sequence_index(index=index, sequence_length=len(ordered))
        if not ordered or index == 0:
            return None, None
        if index < 0:
            target = ordered[-1]
        else:
            target = ordered[index - 1]

        if getattr(target, "component_type", None) == "ASCC":
            return AsccManifestId(int(target.ascc_manifest_id)), None
        return None, BccManifestId(int(target.bcc_manifest_id))

    @staticmethod
    def _resolve_move_after_reference(
        *,
        relationships: list[AsccRelationshipServiceRecord | BccRelationshipServiceRecord],
        item_component_type: str,
        item_manifest_id: int,
        index: int | None,
        after_ascc_manifest_id: AsccManifestId | None,
        after_bcc_manifest_id: BccManifestId | None,
        before_ascc_manifest_id: AsccManifestId | None,
        before_bcc_manifest_id: BccManifestId | None,
    ) -> tuple[AsccManifestId | None, BccManifestId | None]:
        """Resolve sequence movement inputs into a single `after` association reference."""
        provided = sum(
            value is not None
            for value in (
                index,
                after_ascc_manifest_id,
                after_bcc_manifest_id,
                before_ascc_manifest_id,
                before_bcc_manifest_id,
            )
        )
        if provided > 1:
            raise ValueError("Provide only one of `index`, `after_*`, or `before_*`.")

        ordered = [
            relationship
            for relationship in relationships
            if not (
                getattr(relationship, "component_type", None) == item_component_type
                and int(getattr(relationship, f"{item_component_type.lower()}_manifest_id")) == int(item_manifest_id)
            )
        ]

        if index is not None:
            CoreComponentService._assert_valid_sequence_index(index=index, sequence_length=len(ordered))
            if index == -1:
                if not ordered:
                    return None, None
                return CoreComponentService._to_after_reference(ordered[-1])
            if index == 0:
                return None, None
            return CoreComponentService._to_after_reference(ordered[index - 1])

        if after_ascc_manifest_id is not None:
            CoreComponentService._validate_single_sequence_reference(
                relationships=relationships,
                item_component_type=item_component_type,
                item_manifest_id=item_manifest_id,
                target_component_type="ASCC",
                target_manifest_id=int(after_ascc_manifest_id),
                relation_label="after",
            )
            return after_ascc_manifest_id, None

        if after_bcc_manifest_id is not None:
            CoreComponentService._validate_single_sequence_reference(
                relationships=relationships,
                item_component_type=item_component_type,
                item_manifest_id=item_manifest_id,
                target_component_type="BCC",
                target_manifest_id=int(after_bcc_manifest_id),
                relation_label="after",
            )
            return None, after_bcc_manifest_id

        if before_ascc_manifest_id is not None:
            CoreComponentService._validate_single_sequence_reference(
                relationships=relationships,
                item_component_type=item_component_type,
                item_manifest_id=item_manifest_id,
                target_component_type="ASCC",
                target_manifest_id=int(before_ascc_manifest_id),
                relation_label="before",
            )
            return CoreComponentService._resolve_before_reference(
                ordered=ordered,
                target_component_type="ASCC",
                target_manifest_id=int(before_ascc_manifest_id),
            )

        if before_bcc_manifest_id is not None:
            CoreComponentService._validate_single_sequence_reference(
                relationships=relationships,
                item_component_type=item_component_type,
                item_manifest_id=item_manifest_id,
                target_component_type="BCC",
                target_manifest_id=int(before_bcc_manifest_id),
                relation_label="before",
            )
            return CoreComponentService._resolve_before_reference(
                ordered=ordered,
                target_component_type="BCC",
                target_manifest_id=int(before_bcc_manifest_id),
            )

        return None, None

    @staticmethod
    def _ensure_relationship_exists(
        *,
        relationships: list[AsccRelationshipServiceRecord | BccRelationshipServiceRecord],
        component_type: str,
        manifest_id: int,
        label: str,
    ) -> None:
        """Ensure a relationship manifest belongs to the ACC."""
        for relationship in relationships:
            if getattr(relationship, "component_type", None) != component_type:
                continue
            if int(getattr(relationship, f"{component_type.lower()}_manifest_id")) == int(manifest_id):
                return
        raise LookupError(f"No {label} exists with manifest ID {manifest_id} in the target ACC.")

    @staticmethod
    def _validate_single_sequence_reference(
        *,
        relationships: list[AsccRelationshipServiceRecord | BccRelationshipServiceRecord],
        item_component_type: str,
        item_manifest_id: int,
        target_component_type: str,
        target_manifest_id: int,
        relation_label: str,
    ) -> None:
        """Validate a referenced neighbor for sequence reordering."""
        if item_component_type == target_component_type and int(target_manifest_id) == int(item_manifest_id):
            raise ValueError(f"The association cannot be placed {relation_label} itself.")
        CoreComponentService._ensure_relationship_exists(
            relationships=relationships,
            component_type=target_component_type,
            manifest_id=int(target_manifest_id),
            label=target_component_type,
        )

    @staticmethod
    def _resolve_before_reference(
        *,
        ordered: list[AsccRelationshipServiceRecord | BccRelationshipServiceRecord],
        target_component_type: str,
        target_manifest_id: int,
    ) -> tuple[AsccManifestId | None, BccManifestId | None]:
        """Resolve a `before_*` selector into the equivalent `after` reference."""
        for index, relationship in enumerate(ordered):
            if getattr(relationship, "component_type", None) != target_component_type:
                continue
            relationship_manifest_id = int(getattr(relationship, f"{target_component_type.lower()}_manifest_id"))
            if relationship_manifest_id != int(target_manifest_id):
                continue
            if index == 0:
                return None, None
            return CoreComponentService._to_after_reference(ordered[index - 1])
        raise LookupError(f"No {target_component_type} exists with manifest ID {target_manifest_id} in the target ACC.")

    @staticmethod
    def _to_after_reference(
        relationship: AsccRelationshipServiceRecord | BccRelationshipServiceRecord,
    ) -> tuple[AsccManifestId | None, BccManifestId | None]:
        """Convert a sequence relationship record into an `after` reference pair."""
        if getattr(relationship, "component_type", None) == "ASCC":
            return AsccManifestId(int(relationship.ascc_manifest_id)), None
        return None, BccManifestId(int(relationship.bcc_manifest_id))

    @staticmethod
    def _assert_valid_sequence_index(*, index: int, sequence_length: int) -> None:
        """Validate an ACC sequence index for insert-or-move operations."""
        if index < -1:
            raise ValueError("`index` must be -1 or a zero-based index.")
        if index > sequence_length:
            raise ValueError(
                f"`index` is out of range for the ACC sequence. Allowed values are 0 to {sequence_length}, or -1 for the end."
            )

    @staticmethod
    def _normalize_optional_cardinality_min(value: int | UnsetType) -> int | UnsetType:
        """Normalize an optional minimum cardinality for update-style operations."""
        if value is UNSET:
            return UNSET
        normalized = int(value)
        if normalized < 0:
            raise ValueError("`cardinality_min` cannot be less than 0.")
        return normalized

    @staticmethod
    def _normalize_optional_cardinality_max(value: int | UnsetType) -> int | UnsetType:
        """Normalize an optional maximum cardinality for update-style operations."""
        if value is UNSET:
            return UNSET
        normalized = int(value)
        if normalized < -1:
            raise ValueError("`cardinality_max` cannot be less than -1.")
        return normalized

    @staticmethod
    def _normalize_required_cardinality_min(value: int | None, *, default: int) -> int:
        """Normalize a minimum cardinality for create-style operations."""
        normalized = default if value is None else int(value)
        if normalized < 0:
            raise ValueError("`cardinality_min` cannot be less than 0.")
        return normalized

    @staticmethod
    def _normalize_required_cardinality_max(value: int | None, *, default: int) -> int:
        """Normalize a maximum cardinality for create-style operations."""
        normalized = default if value is None else int(value)
        if normalized < -1:
            raise ValueError("`cardinality_max` cannot be less than -1.")
        return normalized

    @staticmethod
    def _assert_valid_cardinality_range(*, cardinality_min: int, cardinality_max: int) -> None:
        """Ensure the final cardinality range is internally consistent."""
        if cardinality_max != -1 and cardinality_min > cardinality_max:
            raise ValueError("`cardinality_min` cannot be greater than `cardinality_max`.")

    @staticmethod
    def _normalize_bcc_entity_type(value: Literal["Attribute", "Element"] | int | UnsetType) -> Literal["Attribute", "Element"] | UnsetType:
        """Normalize BCC entity-type aliases into canonical string values."""
        if value is UNSET:
            return UNSET
        if value in {"Attribute", 0}:
            return "Attribute"
        if value in {"Element", 1}:
            return "Element"
        raise ValueError("`entity_type` must be either `Attribute` or `Element`.")

    @staticmethod
    def _assert_valid_attribute_bcc_cardinality(*, cardinality_min: int, cardinality_max: int) -> None:
        """Ensure `Attribute` BCC cardinality stays within the allowed bounded range."""
        if cardinality_min > 1:
            raise ValueError("`cardinality_min` cannot be greater than 1 for `Attribute` BCCs.")
        if cardinality_max == -1 or cardinality_max > 1:
            raise ValueError("`cardinality_max` cannot be greater than 1 for `Attribute` BCCs.")

    async def _assert_attribute_bcc_allowed_for_bdt(self, *, bdt_manifest_id: int) -> None:
        """Reject `Attribute` BCCs when the linked BDT has active supplementary components."""
        dt = await self._data_type_service.get(bdt_manifest_id)
        if dt is None:
            raise LookupError(
                f"No data type exists with manifest ID {int(bdt_manifest_id)}. Please verify the identifier and try again."
            )
        self._assert_no_active_dt_scs_for_attribute_bcc(dt=dt)

    @staticmethod
    def _assert_no_active_dt_scs_for_attribute_bcc(*, dt: DataTypeServiceResult) -> None:
        """Ensure the target BDT has no active DT_SC entries before allowing an `Attribute` BCC."""
        if any(sc.cardinality != "Prohibited" for sc in dt.supplementary_components):
            raise ValueError(
                "A BCC cannot be `Attribute` when its BCCP's BDT has active DT supplementary components."
            )

    @staticmethod
    def _relationship_target_manifest_id(
        relationship: AsccRelationshipServiceRecord | BccRelationshipServiceRecord,
    ) -> int:
        """Return the target ASCCP/BCCP manifest ID from a relationship payload."""
        if getattr(relationship, "component_type", None) == "ASCC":
            return int(relationship.to_asccp.asccp_manifest_id)
        return int(relationship.to_bccp.bccp_manifest_id)

    @staticmethod
    def _relationship_target_den(
        relationship: AsccRelationshipServiceRecord | BccRelationshipServiceRecord,
    ) -> str:
        """Return the target ASCCP/BCCP DEN from a relationship payload."""
        if getattr(relationship, "component_type", None) == "ASCC":
            return str(relationship.to_asccp.den)
        return str(relationship.to_bccp.den)

    async def _enrich_audit_users(
        self,
        *,
        owner_user_id: int,
        created_by: int,
        last_updated_by: int,
        users_by_id: dict[int] | None = None,
    ) -> tuple[UserSummary, UserSummary, UserSummary]:
        """Resolve owner/created/updated users into summaries.

        Args:
            owner_user_id: Identifier of the owning user.
            created_by: Identifier of the user who created the record.
            last_updated_by: Identifier of the user who last updated the record.
            users_by_id: Preloaded users keyed by user identifier.

        Returns:
            Result of the operation.
        """
        owner_id = owner_user_id
        created_id = created_by
        updated_id = last_updated_by
        if users_by_id is None:
            user_ids = sorted(
                {
                    owner_id,
                    created_id,
                    updated_id,
                },
                key=int,
            )
            users_by_id = await load_users_by_ids(self._account_service_repo, user_ids)
        owner = to_user_summary(owner_id, users_by_id=users_by_id)
        created_who = to_user_summary(created_id, users_by_id=users_by_id)
        updated_who = to_user_summary(updated_id, users_by_id=users_by_id)
        return owner, created_who, updated_who

    @staticmethod
    def _cardinality_display(*, cardinality_min: int, cardinality_max: int) -> str:
        max_display = "unbounded" if cardinality_max == -1 else str(cardinality_max)
        return f"{cardinality_min}..{max_display}"

    def _to_ascc_relationship_service_record(self, relationship: object) -> AsccRelationshipServiceRecord:
        record = to_dataclass(AsccRelationshipServiceRecord, relationship)
        return replace(
            record,
            manifest_id=record.ascc_manifest_id,
            cardinality_display=self._cardinality_display(
                cardinality_min=record.cardinality_min,
                cardinality_max=record.cardinality_max,
            ),
        )

    def _to_bcc_relationship_service_record(self, relationship: object) -> BccRelationshipServiceRecord:
        record = to_dataclass(BccRelationshipServiceRecord, relationship)
        return replace(
            record,
            manifest_id=record.bcc_manifest_id,
            cardinality_display=self._cardinality_display(
                cardinality_min=record.cardinality_min,
                cardinality_max=record.cardinality_max,
            ),
        )
