"""Shared core-component constants used across services and repositories."""


from __future__ import annotations

from app.services.models.core_component import CoreComponentState, OagisComponentType

OAGIS_COMPONENT_TYPE_VALUES: dict[OagisComponentType, int] = {
    "Base": 0,
    "Semantics": 1,
    "Extension": 2,
    "SemanticGroup": 3,
    "UserExtensionGroup": 4,
    "Embedded": 5,
    "OAGIS10Nouns": 6,
    "OAGIS10BODs": 7,
    "BOD": 8,
    "Verb": 9,
    "Noun": 10,
    "Choice": 11,
    "AttributeGroup": 12,
}

OAGIS_COMPONENT_TYPE_NAMES: dict[int, OagisComponentType] = {
    value: key for key, value in OAGIS_COMPONENT_TYPE_VALUES.items()
}

WORKING_BRANCH_CORE_COMPONENT_STATE_TRANSITIONS: dict[CoreComponentState, set[CoreComponentState]] = {
    "Deleted": {"WIP"},
    "WIP": {"Deleted", "Draft"},
    "Draft": {"WIP", "Candidate"},
    "Candidate": {"WIP"},
}

NON_WORKING_BRANCH_CORE_COMPONENT_STATE_TRANSITIONS: dict[
    CoreComponentState, set[CoreComponentState]
] = {
    "Deleted": {"WIP"},
    "WIP": {"Deleted", "QA"},
    "QA": {"WIP", "Production"},
    "Production": {"QA"},
}
