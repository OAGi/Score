"""Pydantic response models for Core Component endpoints."""

from __future__ import annotations

from typing import Annotated, Literal

from pydantic import BaseModel, ConfigDict, Field

from app.routes.models.shared import LibrarySummaryRecord
from app.routes.models.shared import LogSummaryRecord
from app.routes.models.shared import NamespaceSummaryRecord
from app.routes.models.shared import ReleaseSummaryRecord
from app.routes.models.shared import UserSummary
from app.routes.models.shared import ValueConstraintRecord
from app.routes.models.shared import WhoAndWhen


class CoreComponentListEntry(BaseModel):
    """API representation of a unified core component list record."""

    component_type: Literal["ACC", "ASCCP", "BCCP"] = Field(
        ...,
        description=(
            "Core component type: "
            "ACC (Aggregate Core Component), ASCCP (Association Core Component Property), "
            "or BCCP (Basic Core Component Property)."
        ),
    )
    manifest_id: int = Field(..., ge=1, description="Component manifest identifier.")
    component_id: int = Field(..., ge=1, description="Component identifier.")
    guid: str = Field(..., description="Globally unique identifier.")
    den: str = Field(..., description="Dictionary Entry Name (DEN).")
    name: str | None = Field(default=None, description="Display name of the component.")
    definition: str | None = Field(default=None, description="Definition.")
    definition_source: str | None = Field(default=None, description="Definition source URL.")
    is_deprecated: bool = Field(..., description="Whether this component is deprecated.")
    state: str | None = Field(default=None, description="Lifecycle state.")
    namespace: NamespaceSummaryRecord | None = Field(default=None, description="Namespace information.")
    library: LibrarySummaryRecord = Field(..., description="Library information.")
    release: ReleaseSummaryRecord = Field(..., description="Release information.")
    log: LogSummaryRecord | None = Field(default=None, description="Revision log summary.")
    owner: UserSummary = Field(..., description="Owner information.")
    created: WhoAndWhen = Field(..., description="Creation information.")
    last_updated: WhoAndWhen = Field(..., description="Last update information.")
    tag: str | None = Field(default=None, description="Tag name.")

    model_config = ConfigDict(frozen=True)


class GetCoreComponentListResponse(BaseModel):
    """Paginated response envelope for unified core component listings."""

    total_items: int = Field(..., ge=0, description="Total number of matching core components.")
    offset: int = Field(..., ge=0, description="Offset used for this page.")
    limit: int = Field(..., ge=1, le=100, description="Limit used for this page.")
    items: list[CoreComponentListEntry] = Field(..., description="Core components.")

    model_config = ConfigDict(frozen=True)


class DataTypeSummaryRecord(BaseModel):
    """Manifest-scoped BDT summary."""

    dt_manifest_id: int = Field(..., ge=1, description="Data type manifest identifier.")
    dt_id: int = Field(..., ge=1, description="Data type identifier.")
    based_dt_manifest_id: int | None = Field(default=None, ge=1, description="Base data type manifest identifier.")
    guid: str = Field(..., description="Data type GUID.")
    den: str = Field(..., description="Data type DEN.")
    data_type_term: str | None = Field(default=None, description="Data type term.")
    qualifier: str | None = Field(default=None, description="Data type qualifier.")
    representation_term: str | None = Field(default=None, description="Data type representation term.")
    six_digit_id: str | None = Field(default=None, description="Data type six-digit identifier.")
    definition: str | None = Field(default=None, description="Data type definition.")
    definition_source: str | None = Field(default=None, description="Data type definition source.")
    content_component_definition: str | None = Field(default=None, description="Content component definition.")
    is_deprecated: bool = Field(..., description="Whether the data type is deprecated.")
    namespace: NamespaceSummaryRecord | None = Field(default=None, description="Namespace information.")
    library: LibrarySummaryRecord = Field(..., description="Library information.")
    release: ReleaseSummaryRecord = Field(..., description="Release information.")

    model_config = ConfigDict(frozen=True)


class DataTypeSupplementaryComponentRecord(BaseModel):
    """Manifest-scoped data type supplementary component."""

    dt_sc_manifest_id: int = Field(..., ge=1, description="Supplementary component manifest identifier.")
    dt_sc_id: int = Field(..., ge=1, description="Supplementary component identifier.")
    guid: str = Field(..., description="Supplementary component GUID.")
    object_class_term: str | None = Field(default=None, description="Object class term.")
    property_term: str | None = Field(default=None, description="Property term.")
    representation_term: str | None = Field(default=None, description="Representation term.")
    definition: str | None = Field(default=None, description="Definition.")
    definition_source: str | None = Field(default=None, description="Definition source.")
    cardinality_min: int = Field(..., description="Minimum cardinality.")
    cardinality_max: int | None = Field(default=None, description="Maximum cardinality.")
    is_deprecated: bool = Field(..., description="Whether deprecated.")

    model_config = ConfigDict(frozen=True)


class AccInfoRecord(BaseModel):
    """Manifest-scoped ACC information."""

    acc_manifest_id: int = Field(..., ge=1, description="ACC manifest identifier.")
    acc_id: int = Field(..., ge=1, description="ACC identifier.")
    guid: str = Field(..., description="ACC GUID.")
    den: str = Field(..., description="ACC DEN.")
    object_class_term: str = Field(..., description="ACC object class term.")
    definition: str | None = Field(default=None, description="ACC definition.")
    definition_source: str | None = Field(default=None, description="ACC definition source.")
    is_deprecated: bool = Field(..., description="Whether ACC is deprecated.")

    model_config = ConfigDict(frozen=True)


class BaseAccInfoRecord(BaseModel):
    """Manifest-scoped base ACC information."""

    acc_manifest_id: int = Field(..., ge=1, description="Base ACC manifest identifier.")
    acc_id: int = Field(..., ge=1, description="Base ACC identifier.")
    guid: str = Field(..., description="Base ACC GUID.")
    den: str = Field(..., description="Base ACC DEN.")
    object_class_term: str = Field(..., description="Base ACC object class term.")
    type: str | None = Field(default=None, description="ACC type.")
    definition: str | None = Field(default=None, description="ACC definition.")
    definition_source: str | None = Field(default=None, description="ACC definition source.")
    namespace: NamespaceSummaryRecord | None = Field(default=None, description="Namespace information.")
    library: LibrarySummaryRecord = Field(..., description="Library information.")
    release: ReleaseSummaryRecord = Field(..., description="Release information.")

    model_config = ConfigDict(frozen=True)


class AsccpInfoRecord(BaseModel):
    """Manifest-scoped ASCCP information."""

    asccp_manifest_id: int = Field(..., ge=1, description="ASCCP manifest identifier.")
    asccp_id: int = Field(..., ge=1, description="ASCCP identifier.")
    role_of_acc_manifest_id: int = Field(..., ge=1, description="Role-of ACC manifest identifier.")
    guid: str = Field(..., description="ASCCP GUID.")
    den: str | None = Field(default=None, description="ASCCP DEN.")
    property_term: str | None = Field(default=None, description="ASCCP property term.")
    definition: str | None = Field(default=None, description="ASCCP definition.")
    definition_source: str | None = Field(default=None, description="ASCCP definition source.")
    is_deprecated: bool = Field(..., description="Whether ASCCP is deprecated.")

    model_config = ConfigDict(frozen=True)


class AsccInfoRecord(BaseModel):
    """Manifest-scoped ASCC information."""

    ascc_manifest_id: int = Field(..., ge=1, description="ASCC manifest identifier.")
    ascc_id: int = Field(..., ge=1, description="ASCC identifier.")
    guid: str = Field(..., description="ASCC GUID.")
    den: str = Field(..., description="ASCC DEN.")
    cardinality_min: int = Field(..., description="Minimum cardinality.")
    cardinality_max: int = Field(..., description="Maximum cardinality.")
    is_deprecated: bool = Field(..., description="Whether ASCC is deprecated.")
    definition: str | None = Field(default=None, description="ASCC definition.")
    definition_source: str | None = Field(default=None, description="ASCC definition source.")
    from_acc_manifest_id: int = Field(..., ge=1, description="Source ACC manifest identifier.")
    to_asccp_manifest_id: int = Field(..., ge=1, description="Target ASCCP manifest identifier.")

    model_config = ConfigDict(frozen=True)


class BccpInfoRecord(BaseModel):
    """Manifest-scoped BCCP information."""

    bccp_manifest_id: int = Field(..., ge=1, description="BCCP manifest identifier.")
    bccp_id: int = Field(..., ge=1, description="BCCP identifier.")
    guid: str = Field(..., description="BCCP GUID.")
    den: str = Field(..., description="BCCP DEN.")
    property_term: str = Field(..., description="BCCP property term.")
    representation_term: str = Field(..., description="BCCP representation term.")
    definition: str | None = Field(default=None, description="BCCP definition.")
    definition_source: str | None = Field(default=None, description="BCCP definition source.")
    bdt_manifest: DataTypeSummaryRecord = Field(..., description="BDT summary associated with this BCCP.")
    is_deprecated: bool = Field(..., description="Whether BCCP is deprecated.")

    model_config = ConfigDict(frozen=True)


class BccInfoRecord(BaseModel):
    """Manifest-scoped BCC information."""

    bcc_manifest_id: int = Field(..., ge=1, description="BCC manifest identifier.")
    bcc_id: int = Field(..., ge=1, description="BCC identifier.")
    guid: str = Field(..., description="BCC GUID.")
    den: str = Field(..., description="BCC DEN.")
    cardinality_min: int = Field(..., description="Minimum cardinality.")
    cardinality_max: int = Field(..., description="Maximum cardinality.")
    entity_type: Literal["Attribute", "Element"] | None = Field(default=None, description="Entity type.")
    is_nillable: bool = Field(..., description="Whether BCC is nillable.")
    is_deprecated: bool = Field(..., description="Whether BCC is deprecated.")
    definition: str | None = Field(default=None, description="BCC definition.")
    definition_source: str | None = Field(default=None, description="BCC definition source.")
    from_acc_manifest_id: int = Field(..., ge=1, description="Source ACC manifest identifier.")
    to_bccp_manifest_id: int = Field(..., ge=1, description="Target BCCP manifest identifier.")

    model_config = ConfigDict(frozen=True)


class AsccRelationshipInfoRecord(BaseModel):
    """ASCC relationship entry inside ACC detail."""

    component_type: Literal["ASCC"] = Field(default="ASCC", description='Component type. Always "ASCC".')
    manifest_id: int = Field(..., ge=1, description="ASCC manifest identifier.")
    ascc_manifest_id: int = Field(..., ge=1, description="ASCC manifest identifier.")
    ascc_id: int = Field(..., ge=1, description="ASCC identifier.")
    guid: str = Field(..., description="ASCC GUID.")
    den: str = Field(..., description="ASCC DEN.")
    cardinality_min: int = Field(..., description="Minimum cardinality.")
    cardinality_max: int = Field(..., description="Maximum cardinality. `-1` means unbounded.")
    cardinality_display: str = Field(..., description="Human-readable cardinality display.")
    is_deprecated: bool = Field(..., description="Whether ASCC is deprecated.")
    definition: str | None = Field(default=None, description="ASCC definition.")
    definition_source: str | None = Field(default=None, description="ASCC definition source.")
    from_acc: AccInfoRecord = Field(..., description="Source ACC information.")
    to_asccp: AsccpInfoRecord = Field(..., description="Target ASCCP information.")

    model_config = ConfigDict(frozen=True)


class BccRelationshipInfoRecord(BaseModel):
    """BCC relationship entry inside ACC detail."""

    component_type: Literal["BCC"] = Field(default="BCC", description='Component type. Always "BCC".')
    manifest_id: int = Field(..., ge=1, description="BCC manifest identifier.")
    bcc_manifest_id: int = Field(..., ge=1, description="BCC manifest identifier.")
    bcc_id: int = Field(..., ge=1, description="BCC identifier.")
    guid: str = Field(..., description="BCC GUID.")
    den: str = Field(..., description="BCC DEN.")
    cardinality_min: int = Field(..., description="Minimum cardinality.")
    cardinality_max: int = Field(..., description="Maximum cardinality. `-1` means unbounded.")
    cardinality_display: str = Field(..., description="Human-readable cardinality display.")
    entity_type: Literal["Attribute", "Element"] | None = Field(default=None, description="Entity type.")
    is_nillable: bool = Field(..., description="Whether BCC is nillable.")
    value_constraint: ValueConstraintRecord | None = Field(default=None, description="Value constraint.")
    is_deprecated: bool = Field(..., description="Whether BCC is deprecated.")
    definition: str | None = Field(default=None, description="BCC definition.")
    definition_source: str | None = Field(default=None, description="BCC definition source.")
    from_acc: AccInfoRecord = Field(..., description="Source ACC information.")
    to_bccp: BccpInfoRecord = Field(..., description="Target BCCP information.")

    model_config = ConfigDict(frozen=True)


class GetAccByAccManifestIdResponse(BaseModel):
    """ACC detail response model (manifest-only identifiers)."""

    acc_manifest_id: int = Field(..., ge=1, description="ACC manifest identifier.")
    acc_id: int = Field(..., ge=1, description="ACC identifier.")
    base_acc: BaseAccInfoRecord | None = Field(default=None, description="Base ACC information.")
    relationships: list[
        Annotated[AsccRelationshipInfoRecord | BccRelationshipInfoRecord, Field(discriminator="component_type")]
    ] = Field(default_factory=list, description="ACC relationships.")
    guid: str = Field(..., description="ACC GUID.")
    den: str = Field(..., description="ACC DEN.")
    object_class_term: str = Field(..., description="Object class term.")
    definition: str | None = Field(default=None, description="ACC definition.")
    definition_source: str | None = Field(default=None, description="ACC definition source.")
    object_class_qualifier: str | None = Field(default=None, description="Object class qualifier.")
    component_type: int | None = Field(default=None, description="OAGIS component type.")
    is_abstract: bool = Field(..., description="Whether ACC is abstract.")
    is_deprecated: bool = Field(..., description="Whether ACC is deprecated.")
    state: str | None = Field(default=None, description="ACC state.")
    namespace: NamespaceSummaryRecord | None = Field(default=None, description="ACC namespace.")
    library: LibrarySummaryRecord = Field(..., description="ACC library.")
    release: ReleaseSummaryRecord = Field(..., description="ACC release.")
    log: LogSummaryRecord | None = Field(default=None, description="Revision log.")
    owner: UserSummary = Field(..., description="Owner information.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class GetAsccpByAsccpManifestIdResponse(BaseModel):
    """ASCCP detail response model (manifest-only identifiers)."""

    asccp_manifest_id: int = Field(..., ge=1, description="ASCCP manifest identifier.")
    asccp_id: int = Field(..., ge=1, description="ASCCP identifier.")
    role_of_acc: BaseAccInfoRecord | None = Field(default=None, description="Role-of ACC information.")
    guid: str = Field(..., description="ASCCP GUID.")
    den: str | None = Field(default=None, description="ASCCP DEN.")
    property_term: str | None = Field(default=None, description="ASCCP property term.")
    definition: str | None = Field(default=None, description="ASCCP definition.")
    definition_source: str | None = Field(default=None, description="ASCCP definition source.")
    reusable_indicator: bool = Field(..., description="Reusable indicator.")
    is_nillable: bool | None = Field(default=None, description="Nillable indicator.")
    is_deprecated: bool = Field(..., description="Deprecated indicator.")
    state: str | None = Field(default=None, description="ASCCP state.")
    namespace: NamespaceSummaryRecord | None = Field(default=None, description="ASCCP namespace.")
    library: LibrarySummaryRecord = Field(..., description="ASCCP library.")
    release: ReleaseSummaryRecord = Field(..., description="ASCCP release.")
    log: LogSummaryRecord | None = Field(default=None, description="Revision log.")
    owner: UserSummary = Field(..., description="Owner information.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(frozen=True)


class GetBccpByBccpManifestIdResponse(BaseModel):
    """BCCP detail response model (manifest-only identifiers)."""

    bccp_manifest_id: int = Field(..., ge=1, description="BCCP manifest identifier.")
    bccp_id: int = Field(..., ge=1, description="BCCP identifier.")
    bdt: DataTypeSummaryRecord = Field(..., description="Associated BDT information.")
    guid: str = Field(..., description="BCCP GUID.")
    den: str = Field(..., description="BCCP DEN.")
    property_term: str = Field(..., description="BCCP property term.")
    representation_term: str = Field(..., description="BCCP representation term.")
    definition: str | None = Field(default=None, description="BCCP definition.")
    definition_source: str | None = Field(default=None, description="BCCP definition source.")
    is_nillable: bool = Field(..., description="Nillable indicator.")
    value_constraint: ValueConstraintRecord | None = Field(default=None, description="Value constraint.")
    is_deprecated: bool = Field(..., description="Deprecated indicator.")
    state: str | None = Field(default=None, description="BCCP state.")
    namespace: NamespaceSummaryRecord | None = Field(default=None, description="BCCP namespace.")
    library: LibrarySummaryRecord = Field(..., description="BCCP library.")
    release: ReleaseSummaryRecord = Field(..., description="BCCP release.")
    log: LogSummaryRecord | None = Field(default=None, description="Revision log.")
    owner: UserSummary = Field(..., description="Owner information.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(frozen=True)
