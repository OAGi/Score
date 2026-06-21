"""Dependency-aware validation for top-level BIE state transitions."""

from __future__ import annotations

from abc import ABC, abstractmethod
from dataclasses import dataclass
from enum import Enum

from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.repositories.contracts.business_information_entity import BusinessInformationEntityRepositoryContract
from app.repositories.contracts.code_list import CodeListRepositoryContract
from app.repositories.models.business_information_entity import TopLevelAsbiepDetailRow
from app.repositories.models.code_list import CodeListRow
from app.types.identifiers import AppUserId


@dataclass(frozen=True, kw_only=True)
class TopLevelAsbiepDependencyDetail:
    """Minimal dependent top-level ASBIEP information for blocked mutations."""

    top_level_asbiep_id: int
    state: str | None = None
    display_name: str | None = None
    den: str | None = None


@dataclass(frozen=True, kw_only=True)
class CodeListDependencyDetail:
    """Minimal code list information for blocked BIE transitions."""

    code_list_manifest_id: int
    state: str | None = None
    name: str | None = None
    list_id: str | None = None
    version_id: str | None = None


class TopLevelAsbiepDependencyBlockedError(ValueError):
    """Raised when a top-level ASBIEP mutation is blocked by dependencies."""

    def __init__(
        self,
        *,
        message: str,
        dependents: list[TopLevelAsbiepDependencyDetail] | None = None,
        code_lists: list[CodeListDependencyDetail] | None = None,
    ) -> None:
        super().__init__(message)
        self.dependents = list(dependents or [])
        self.code_lists = list(code_lists or [])


class BieStateTransitionDependency(Enum):
    """Supported dependency edge kinds for BIE transition validation."""

    REUSES = "REUSES"
    REUSED_BY = "REUSED_BY"
    INHERITS_FROM = "INHERITS_FROM"
    IS_A_BASED_OF = "IS_A_BASED_OF"
    USES_CODE_LIST = "USES_CODE_LIST"
    USED_BY_BIE = "USED_BY_BIE"


@dataclass(frozen=True)
class _BieFutureStateCarrier:
    record: TopLevelAsbiepDetailRow
    future_state: str | None


@dataclass(frozen=True)
class _CodeListFutureStateCarrier:
    record: CodeListRow
    future_state: str | None


class _BieStateTransitionRuleViolation(Exception):
    """Internal marker for one rejected dependency edge."""


class _BieStateLevel:
    """Compatibility rules between target BIE states and related BIE states."""

    @classmethod
    def compatible_states(cls, bie_state: str | None) -> list[str]:
        if bie_state is None:
            return []
        return {
            "WIP": ["WIP", "QA", "Production"],
            "QA": ["QA", "Production"],
            "Production": ["Production"],
        }.get(str(bie_state), [])

    @classmethod
    def is_compatible(cls, requested_state: str | None, candidate_state: str | None) -> bool:
        return requested_state is not None and candidate_state is not None and candidate_state in cls.compatible_states(requested_state)


class _CodeListStateLevel:
    """Compatibility rules between BIE states and assigned code list states."""

    @classmethod
    def compatible_states(cls, bie_state: str | None, *, developer_owned: bool) -> list[str]:
        if bie_state is None:
            return []
        if developer_owned:
            return ["Published"]
        return {
            "WIP": ["WIP", "QA", "Production"],
            "QA": ["QA", "Production"],
            "Production": ["Production"],
        }.get(str(bie_state), [])

    @classmethod
    def is_compatible(cls, bie_state: str | None, code_list_state: str | None, *, developer_owned: bool) -> bool:
        return (
            bie_state is not None
            and code_list_state is not None
            and code_list_state in cls.compatible_states(bie_state, developer_owned=developer_owned)
        )

    @classmethod
    def compatible_bie_states(cls, code_list_state: str | None, *, developer_owned: bool) -> list[str]:
        if code_list_state is None:
            return []
        if developer_owned:
            return ["WIP", "QA", "Production"] if str(code_list_state) == "Published" else []
        return {
            "WIP": ["WIP"],
            "QA": ["WIP", "QA"],
            "Production": ["WIP", "QA", "Production"],
        }.get(str(code_list_state), [])


class _BieStateTransitionRule(ABC):
    """Contract for validating one directed dependency edge."""

    dependency: BieStateTransitionDependency

    @abstractmethod
    def validate(
        self,
        source: _BieFutureStateCarrier | _CodeListFutureStateCarrier,
        target: _BieFutureStateCarrier | _CodeListFutureStateCarrier,
        dependency: BieStateTransitionDependency,
        *,
        target_developer_owned: bool | None = None,
        source_developer_owned: bool | None = None,
    ) -> None:
        """Validate the dependency edge or raise a rule violation."""


class _ReusesStateTransitionRule(_BieStateTransitionRule):
    dependency = BieStateTransitionDependency.REUSES

    def validate(
        self,
        source: _BieFutureStateCarrier | _CodeListFutureStateCarrier,
        target: _BieFutureStateCarrier | _CodeListFutureStateCarrier,
        dependency: BieStateTransitionDependency,
        *,
        target_developer_owned: bool | None = None,
        source_developer_owned: bool | None = None,
    ) -> None:
        if dependency != self.dependency or not isinstance(source, _BieFutureStateCarrier) or not isinstance(target, _BieFutureStateCarrier):
            return
        if source.future_state == "Discard":
            return
        if target.future_state == "Discard":
            raise _BieStateTransitionRuleViolation()
        if not _BieStateLevel.is_compatible(source.future_state, target.future_state):
            raise _BieStateTransitionRuleViolation()


class _ReusedByStateTransitionRule(_BieStateTransitionRule):
    dependency = BieStateTransitionDependency.REUSED_BY

    def validate(
        self,
        source: _BieFutureStateCarrier | _CodeListFutureStateCarrier,
        target: _BieFutureStateCarrier | _CodeListFutureStateCarrier,
        dependency: BieStateTransitionDependency,
        *,
        target_developer_owned: bool | None = None,
        source_developer_owned: bool | None = None,
    ) -> None:
        if dependency != self.dependency or not isinstance(source, _BieFutureStateCarrier) or not isinstance(target, _BieFutureStateCarrier):
            return
        if target.future_state == "Discard":
            return
        if source.future_state == "Discard":
            raise _BieStateTransitionRuleViolation()
        if not _BieStateLevel.is_compatible(target.future_state, source.future_state):
            raise _BieStateTransitionRuleViolation()


class _InheritsFromStateTransitionRule(_BieStateTransitionRule):
    dependency = BieStateTransitionDependency.INHERITS_FROM

    def validate(
        self,
        source: _BieFutureStateCarrier | _CodeListFutureStateCarrier,
        target: _BieFutureStateCarrier | _CodeListFutureStateCarrier,
        dependency: BieStateTransitionDependency,
        *,
        target_developer_owned: bool | None = None,
        source_developer_owned: bool | None = None,
    ) -> None:
        if dependency != self.dependency or not isinstance(source, _BieFutureStateCarrier) or not isinstance(target, _BieFutureStateCarrier):
            return
        if source.future_state == "Discard":
            return
        if target.future_state == "Discard":
            raise _BieStateTransitionRuleViolation()
        if not _BieStateLevel.is_compatible(source.future_state, target.future_state):
            raise _BieStateTransitionRuleViolation()


class _IsABasedOfStateTransitionRule(_BieStateTransitionRule):
    dependency = BieStateTransitionDependency.IS_A_BASED_OF

    def validate(
        self,
        source: _BieFutureStateCarrier | _CodeListFutureStateCarrier,
        target: _BieFutureStateCarrier | _CodeListFutureStateCarrier,
        dependency: BieStateTransitionDependency,
        *,
        target_developer_owned: bool | None = None,
        source_developer_owned: bool | None = None,
    ) -> None:
        if dependency != self.dependency or not isinstance(source, _BieFutureStateCarrier) or not isinstance(target, _BieFutureStateCarrier):
            return
        if target.future_state == "Discard":
            return
        if source.future_state == "Discard":
            raise _BieStateTransitionRuleViolation()
        if not _BieStateLevel.is_compatible(target.future_state, source.future_state):
            raise _BieStateTransitionRuleViolation()


class _UsesCodeListStateTransitionRule(_BieStateTransitionRule):
    dependency = BieStateTransitionDependency.USES_CODE_LIST

    def validate(
        self,
        source: _BieFutureStateCarrier | _CodeListFutureStateCarrier,
        target: _BieFutureStateCarrier | _CodeListFutureStateCarrier,
        dependency: BieStateTransitionDependency,
        *,
        target_developer_owned: bool | None = None,
        source_developer_owned: bool | None = None,
    ) -> None:
        if dependency != self.dependency or not isinstance(source, _BieFutureStateCarrier) or not isinstance(target, _CodeListFutureStateCarrier):
            return
        if not _CodeListStateLevel.is_compatible(
            source.future_state,
            target.future_state,
            developer_owned=bool(target_developer_owned),
        ):
            raise _BieStateTransitionRuleViolation()


class _UsedByBieCodeListStateTransitionRule(_BieStateTransitionRule):
    dependency = BieStateTransitionDependency.USED_BY_BIE

    def validate(
        self,
        source: _BieFutureStateCarrier | _CodeListFutureStateCarrier,
        target: _BieFutureStateCarrier | _CodeListFutureStateCarrier,
        dependency: BieStateTransitionDependency,
        *,
        target_developer_owned: bool | None = None,
        source_developer_owned: bool | None = None,
    ) -> None:
        if dependency != self.dependency or not isinstance(source, _CodeListFutureStateCarrier) or not isinstance(target, _BieFutureStateCarrier):
            return
        if target.future_state not in _CodeListStateLevel.compatible_bie_states(
            source.future_state,
            developer_owned=bool(source_developer_owned),
        ):
            raise _BieStateTransitionRuleViolation()


class BieStateTransitionService:
    """Rule-based validator for one top-level ASBIEP transition."""

    def __init__(
        self,
        *,
        bie_repo: BusinessInformationEntityRepositoryContract,
        app_user_repo: AppUserRepositoryContract | None,
        top_level_asbiep_id: int,
        code_list_repo: CodeListRepositoryContract | None = None,
    ) -> None:
        self._bie_repo = bie_repo
        self._app_user_repo = app_user_repo
        self._code_list_repo = code_list_repo
        self._top_level_asbiep_id = top_level_asbiep_id
        self._rules: tuple[_BieStateTransitionRule, ...] = (
            _ReusesStateTransitionRule(),
            _ReusedByStateTransitionRule(),
            _InheritsFromStateTransitionRule(),
            _IsABasedOfStateTransitionRule(),
            _UsesCodeListStateTransitionRule(),
            _UsedByBieCodeListStateTransitionRule(),
        )

    async def ensure_state_transition_allowed(
        self,
        *,
        top_level_asbiep: TopLevelAsbiepDetailRow,
        target_state: str,
    ) -> None:
        reuses_blockers = await self._list_bie_blockers(
            source_row=top_level_asbiep,
            source_future_state=target_state,
            dependency=BieStateTransitionDependency.REUSES,
            dependency_ids=await self._bie_repo.list_reused_top_level_asbiep_ids(
                top_level_asbiep_id=self._top_level_asbiep_id,
            ),
        )
        reused_by_blockers = await self._list_bie_blockers(
            source_row=top_level_asbiep,
            source_future_state=target_state,
            dependency=BieStateTransitionDependency.REUSED_BY,
            dependency_ids=await self._bie_repo.list_reusing_top_level_asbiep_ids(
                top_level_asbiep_id=self._top_level_asbiep_id,
            ),
        )
        based_of_blockers = await self._list_bie_blockers(
            source_row=top_level_asbiep,
            source_future_state=target_state,
            dependency=BieStateTransitionDependency.IS_A_BASED_OF,
            dependency_ids=await self._list_optional_dependency_ids("list_inheriting_top_level_asbiep_ids"),
        )
        inherits_from_blockers = await self._list_bie_blockers(
            source_row=top_level_asbiep,
            source_future_state=target_state,
            dependency=BieStateTransitionDependency.INHERITS_FROM,
            dependency_ids=await self._list_optional_dependency_ids("list_inherited_top_level_asbiep_ids"),
        )
        code_list_blockers = await self._list_code_list_blockers(
            source_row=top_level_asbiep,
            source_future_state=target_state,
        )

        dependent_blockers = sorted(
            [*reuses_blockers, *reused_by_blockers, *based_of_blockers, *inherits_from_blockers],
            key=lambda item: item.top_level_asbiep_id,
        )
        if dependent_blockers or code_list_blockers:
            reasons: list[str] = []
            if dependent_blockers:
                reasons.append(
                    "dependent top-level ASBIEP(s) would violate the transition: "
                    f"{self._format_top_level_id_list(dependent_blockers)}"
                )
            if code_list_blockers:
                reasons.append(
                    "assigned code list(s) are not compatible: "
                    f"{self._format_code_list_id_list(code_list_blockers)}"
                )
            raise TopLevelAsbiepDependencyBlockedError(
                message=(
                    f"Cannot move top-level ASBIEP {int(self._top_level_asbiep_id)} to {target_state} because "
                    + "; ".join(reasons)
                    + "."
                ),
                dependents=dependent_blockers,
                code_lists=code_list_blockers,
            )

    async def ensure_delete_allowed(self, *, top_level_asbiep: TopLevelAsbiepDetailRow) -> None:
        dependents = await self._list_bie_blockers(
            source_row=top_level_asbiep,
            source_future_state="Discard",
            dependency=BieStateTransitionDependency.REUSED_BY,
            dependency_ids=await self._bie_repo.list_reusing_top_level_asbiep_ids(
                top_level_asbiep_id=self._top_level_asbiep_id,
            ),
        )
        inherits = await self._list_bie_blockers(
            source_row=top_level_asbiep,
            source_future_state="Discard",
            dependency=BieStateTransitionDependency.IS_A_BASED_OF,
            dependency_ids=await self._list_optional_dependency_ids("list_inheriting_top_level_asbiep_ids"),
        )
        blockers = sorted([*dependents, *inherits], key=lambda item: item.top_level_asbiep_id)
        if blockers:
            raise TopLevelAsbiepDependencyBlockedError(
                message=(
                    "Cannot delete the requested top-level ASBIEP because it is referenced by "
                    f"top-level ASBIEP(s): {self._format_top_level_id_list(blockers)}. Delete those references first."
                ),
                dependents=blockers,
            )

    async def _list_bie_blockers(
        self,
        *,
        source_row: TopLevelAsbiepDetailRow,
        source_future_state: str,
        dependency: BieStateTransitionDependency,
        dependency_ids: list[int],
    ) -> list[TopLevelAsbiepDependencyDetail]:
        source = _BieFutureStateCarrier(record=source_row, future_state=source_future_state)
        blockers: list[TopLevelAsbiepDependencyDetail] = []
        for dependency_id in sorted(set(int(value) for value in dependency_ids)):
            target_row = await self._bie_repo.get_top_level_asbiep(top_level_asbiep_id=dependency_id)
            if target_row is None:
                continue
            target = _BieFutureStateCarrier(record=target_row, future_state=str(target_row.state) if target_row.state is not None else None)
            if self._edge_is_blocked(source=source, target=target, dependency=dependency):
                blockers.append(self._to_top_level_dependency_detail(dependency_id=dependency_id, row=target_row))
        return blockers

    async def _list_code_list_blockers(
        self,
        *,
        source_row: TopLevelAsbiepDetailRow,
        source_future_state: str,
    ) -> list[CodeListDependencyDetail]:
        if self._code_list_repo is None:
            return []
        source = _BieFutureStateCarrier(record=source_row, future_state=source_future_state)
        blockers: list[CodeListDependencyDetail] = []
        dependency_ids = await self._bie_repo.list_assigned_code_list_manifest_ids(
            top_level_asbiep_id=self._top_level_asbiep_id,
        )
        for dependency_id in sorted(set(int(value) for value in dependency_ids)):
            code_list = await self._code_list_repo.get(dependency_id)
            if code_list is None:
                continue
            owner = None
            if self._app_user_repo is not None:
                owner = await self._app_user_repo.get(AppUserId(int(code_list.owner_user_id)))
            target = _CodeListFutureStateCarrier(record=code_list, future_state=str(code_list.state) if code_list.state is not None else None)
            if self._edge_is_blocked(
                source=source,
                target=target,
                dependency=BieStateTransitionDependency.USES_CODE_LIST,
                target_developer_owned=bool(owner.is_developer) if owner is not None else False,
            ):
                blockers.append(
                    CodeListDependencyDetail(
                        code_list_manifest_id=dependency_id,
                        state=str(code_list.state) if code_list.state is not None else None,
                        name=code_list.name,
                        list_id=code_list.list_id,
                        version_id=code_list.version_id,
                    )
                )
        return blockers

    async def _list_optional_dependency_ids(self, method_name: str) -> list[int]:
        if method_name not in vars(self._bie_repo) and getattr(type(self._bie_repo), method_name, None) is None:
            return []
        loader = getattr(self._bie_repo, method_name, None)
        if loader is None:
            return []
        values = await loader(top_level_asbiep_id=self._top_level_asbiep_id)
        return [int(value) for value in values]

    def _edge_is_blocked(
        self,
        *,
        source: _BieFutureStateCarrier | _CodeListFutureStateCarrier,
        target: _BieFutureStateCarrier | _CodeListFutureStateCarrier,
        dependency: BieStateTransitionDependency,
        target_developer_owned: bool | None = None,
        source_developer_owned: bool | None = None,
    ) -> bool:
        try:
            for rule in self._rules:
                rule.validate(
                    source,
                    target,
                    dependency,
                    target_developer_owned=target_developer_owned,
                    source_developer_owned=source_developer_owned,
                )
        except _BieStateTransitionRuleViolation:
            return True
        return False

    def _to_top_level_dependency_detail(
        self,
        *,
        dependency_id: int,
        row: TopLevelAsbiepDetailRow | None,
    ) -> TopLevelAsbiepDependencyDetail:
        if row is None:
            return TopLevelAsbiepDependencyDetail(top_level_asbiep_id=dependency_id)
        return TopLevelAsbiepDependencyDetail(
            top_level_asbiep_id=dependency_id,
            state=str(row.state) if row.state is not None else None,
            display_name=row.asbiep.display_name,
            den=row.asbiep.based_asccp_manifest.den,
        )

    def _format_top_level_id_list(self, dependents: list[TopLevelAsbiepDependencyDetail]) -> str:
        return ", ".join(str(dependent.top_level_asbiep_id) for dependent in dependents)

    def _format_code_list_id_list(self, code_lists: list[CodeListDependencyDetail]) -> str:
        return ", ".join(str(code_list.code_list_manifest_id) for code_list in code_lists)
