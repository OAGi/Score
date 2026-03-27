"""Repository projection models for core-component queries."""


from __future__ import annotations

from datetime import datetime
from typing import Literal

from pydantic import BaseModel, ConfigDict, Field

from app.repositories.models.library import LibrarySummaryRow
from app.repositories.models.log import LogSummaryRow
from app.repositories.models.namespace import NamespaceSummaryRow
from app.repositories.models.release import ReleaseSummaryRow

CoreComponentType = Literal["ACC", "ASCCP", "BCCP"]


class ValueConstraintRow(BaseModel):
    """Repository value-constraint row."""

    default_value: str | None = None
    fixed_value: str | None = None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class AccInfoRow(BaseModel):
    """Repository row for ACC info."""

    acc_manifest_id: int
    acc_id: int
    guid: str
    den: str
    object_class_term: str
    definition: str | None = None
    definition_source: str | None = None
    is_deprecated: bool

    model_config = ConfigDict(frozen=True, from_attributes=True)


class AsccpInfoRow(BaseModel):
    """Repository row for ASCCP info."""

    asccp_manifest_id: int
    asccp_id: int
    role_of_acc_manifest_id: int
    guid: str
    den: str
    property_term: str
    definition: str | None = None
    definition_source: str | None = None
    is_deprecated: bool

    model_config = ConfigDict(frozen=True, from_attributes=True)


class AsccInfoRow(BaseModel):
    """Repository row for ASCC info."""

    ascc_manifest_id: int
    ascc_id: int
    guid: str
    den: str
    cardinality_min: int
    cardinality_max: int
    is_deprecated: bool
    definition: str | None = None
    definition_source: str | None = None
    from_acc_manifest_id: int
    to_asccp_manifest_id: int

    model_config = ConfigDict(frozen=True, from_attributes=True)


class BccInfoRow(BaseModel):
    """Repository row for BCC info."""

    bcc_manifest_id: int
    bcc_id: int
    guid: str
    den: str
    cardinality_min: int
    cardinality_max: int
    entity_type: Literal["Attribute", "Element"] | None = None
    is_nillable: bool
    is_deprecated: bool
    definition: str | None = None
    definition_source: str | None = None
    from_acc_manifest_id: int
    to_bccp_manifest_id: int

    model_config = ConfigDict(frozen=True, from_attributes=True)


class DtSummaryRow(BaseModel):
    """Repository row for data type summary."""

    dt_manifest_id: int
    dt_id: int
    based_dt_manifest_id: int | None = None
    guid: str
    den: str
    data_type_term: str | None = None
    qualifier: str | None = None
    representation_term: str | None = None
    six_digit_id: str | None = None
    definition: str | None = None
    definition_source: str | None = None
    content_component_definition: str | None = None
    is_deprecated: bool
    namespace: NamespaceSummaryRow | None = None
    library: LibrarySummaryRow
    release: ReleaseSummaryRow

    model_config = ConfigDict(frozen=True, from_attributes=True)


class BccpInfoRow(BaseModel):
    """Repository row for BCCP info."""

    bccp_manifest_id: int
    bccp_id: int
    guid: str
    den: str
    property_term: str
    representation_term: str
    definition: str | None = None
    definition_source: str | None = None
    bdt_manifest: DtSummaryRow
    is_deprecated: bool

    model_config = ConfigDict(frozen=True, from_attributes=True)


class BaseAccInfoRow(BaseModel):
    """Repository row for base ACC info."""

    acc_manifest_id: int
    acc_id: int
    guid: str
    den: str
    object_class_term: str
    type: str | None = None
    definition: str | None = None
    definition_source: str | None = None
    namespace: NamespaceSummaryRow | None = None
    library: LibrarySummaryRow
    release: ReleaseSummaryRow

    model_config = ConfigDict(frozen=True, from_attributes=True)


class AsccRelationshipInfoRow(BaseModel):
    """Repository row for ASCC relationship from ACC."""

    component_type: Literal["ASCC"] = "ASCC"
    ascc_manifest_id: int
    ascc_id: int
    guid: str
    den: str
    cardinality_min: int
    cardinality_max: int
    is_deprecated: bool
    definition: str | None = None
    definition_source: str | None = None
    from_acc: AccInfoRow
    to_asccp: AsccpInfoRow

    model_config = ConfigDict(frozen=True, from_attributes=True)


class BccRelationshipInfoRow(BaseModel):
    """Repository row for BCC relationship from ACC."""

    component_type: Literal["BCC"] = "BCC"
    bcc_manifest_id: int
    bcc_id: int
    guid: str
    den: str
    cardinality_min: int
    cardinality_max: int
    entity_type: Literal["Attribute", "Element"] | None = None
    is_nillable: bool
    value_constraint: ValueConstraintRow | None = None
    is_deprecated: bool
    definition: str | None = None
    definition_source: str | None = None
    from_acc: AccInfoRow
    to_bccp: BccpInfoRow

    model_config = ConfigDict(frozen=True, from_attributes=True)


class CoreComponentListRow(BaseModel):
    """Repository row shape for core-component list before user enrichment."""

    component_type: CoreComponentType = Field(..., description="Core component type.")
    manifest_id: int = Field(..., ge=1, description="Component manifest identifier.")
    component_id: int = Field(..., ge=1, description="Component identifier.")
    guid: str = Field(..., description="Globally unique identifier.")
    den: str = Field(..., description="Dictionary Entry Name (DEN).")
    name: str | None = Field(default=None, description="Display name of the component.")
    definition: str | None = Field(default=None, description="Definition.")
    definition_source: str | None = Field(default=None, description="Definition source URL.")
    is_deprecated: bool = Field(..., description="Whether this component is deprecated.")
    state: str | None = Field(default=None, description="Lifecycle state.")
    namespace: NamespaceSummaryRow | None = Field(default=None, description="Namespace information.")
    library: LibrarySummaryRow = Field(..., description="Library information.")
    release: ReleaseSummaryRow = Field(..., description="Release information.")
    log: LogSummaryRow | None = Field(default=None, description="Revision log summary.")
    owner_user_id: int = Field(..., description="Owner user ID.")
    created_by: int = Field(..., description="Creator user ID.")
    creation_timestamp: datetime = Field(..., description="Creation timestamp.")
    last_updated_by: int = Field(..., description="Last updater user ID.")
    last_update_timestamp: datetime = Field(..., description="Last update timestamp.")
    tag: str | None = Field(default=None, description="Tag name.")

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetAccRow(BaseModel):
    """Repository row shape for ACC detail before user enrichment."""

    acc_manifest_id: int
    acc_id: int
    base_acc: BaseAccInfoRow | None = None
    relationships: list[AsccRelationshipInfoRow | BccRelationshipInfoRow] = Field(default_factory=list)
    guid: str
    den: str
    object_class_term: str
    definition: str | None = None
    definition_source: str | None = None
    object_class_qualifier: str | None = None
    component_type: int | None = None
    is_abstract: bool
    is_deprecated: bool
    state: str | None = None
    namespace: NamespaceSummaryRow | None = None
    library: LibrarySummaryRow
    release: ReleaseSummaryRow
    log: LogSummaryRow | None = None
    owner_user_id: int
    created_by: int
    creation_timestamp: datetime
    last_updated_by: int
    last_update_timestamp: datetime

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetAsccpRow(BaseModel):
    """Repository row shape for ASCCP detail before user enrichment."""

    asccp_manifest_id: int
    asccp_id: int
    role_of_acc: BaseAccInfoRow | None = None
    guid: str
    den: str | None = None
    property_term: str | None = None
    definition: str | None = None
    definition_source: str | None = None
    reusable_indicator: bool
    is_nillable: bool | None = None
    is_deprecated: bool
    state: str | None = None
    namespace: NamespaceSummaryRow | None = None
    library: LibrarySummaryRow
    release: ReleaseSummaryRow
    log: LogSummaryRow | None = None
    owner_user_id: int
    created_by: int
    creation_timestamp: datetime
    last_updated_by: int
    last_update_timestamp: datetime

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetBccpRow(BaseModel):
    """Repository row shape for BCCP detail before user enrichment."""

    bccp_manifest_id: int
    bccp_id: int
    bdt: DtSummaryRow
    guid: str
    den: str
    property_term: str
    representation_term: str
    definition: str | None = None
    definition_source: str | None = None
    is_nillable: bool
    value_constraint: ValueConstraintRow | None = None
    is_deprecated: bool
    state: str | None = None
    namespace: NamespaceSummaryRow | None = None
    library: LibrarySummaryRow
    release: ReleaseSummaryRow
    log: LogSummaryRow | None = None
    owner_user_id: int
    created_by: int
    creation_timestamp: datetime
    last_updated_by: int
    last_update_timestamp: datetime

    model_config = ConfigDict(frozen=True, from_attributes=True)
