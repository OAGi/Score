"""Pydantic request and response models for Core Component endpoints."""

from __future__ import annotations

from typing import Annotated, Literal

from pydantic import BaseModel, ConfigDict, Field, model_validator

from app.routes.models.shared import LibrarySummaryRecord
from app.routes.models.shared import LogSummaryRecord
from app.routes.models.shared import NamespaceSummaryRecord
from app.routes.models.shared import ReleaseSummaryRecord
from app.routes.models.shared import TagSummaryRecord
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


class CreateAccRequest(BaseModel):
    """Request payload for creating an ACC."""

    release_id: int = Field(
        ...,
        ge=1,
        description=(
            "Target release identifier. Developers can target only the `Working` release, "
            "while end-users can target only non-`Working` releases."
        ),
    )
    based_acc_manifest_id: int | None = Field(
        default=None,
        ge=1,
        description=(
            "Base ACC manifest identifier. If provided and the base ACC is in the same library as `release_id`, "
            "it must be from that release. If it is in a different library, its release must be one of the "
            "target release dependencies."
        ),
    )
    object_class_term: str = Field(
        ...,
        min_length=1,
        description=(
            "Object class term to start with. In CCTS, this is the term that represents the activity or object "
            "the ACC stands for. It serves as the basis for the ACC DEN and for the DENs of the ASCC and BCC "
            "properties under that ACC."
        ),
    )
    component_type: Literal[
        "Base",
        "Semantics",
        "Extension",
        "SemanticGroup",
        "UserExtensionGroup",
        "Embedded",
        "OAGIS10Nouns",
        "OAGIS10BODs",
        "BOD",
        "Verb",
        "Noun",
        "Choice",
        "AttributeGroup",
    ] = Field(
        default="Semantics",
        description=(
            "OAGIS component type to start with. Use `Base` only when this ACC is meant to be the base ACC for "
            "other ACCs. Use `Extension` for a developer extension ACC that end-users may extend later in BIEs. "
            "Use `SemanticGroup` for a grouping ACC whose associations are flattened in BIEs. In most other "
            "cases, use `Semantics`. Defaults to `Semantics`."
        ),
    )
    definition: str | None = Field(
        default=None,
        description=(
            "Definition text to start with. This is the explanatory text that describes what the ACC means."
        ),
    )
    definition_source: str | None = Field(
        default=None,
        description=(
            "Definition source to start with. Use this to record where the definition came from, such as a "
            "specification, standard, or reference URL."
        ),
    )
    is_abstract: bool | None = Field(
        default=None,
        description=(
            "Whether this ACC should be abstract. If `component_type` is `Base`, this is usually `true`, because "
            "the ACC is meant to serve as a base ACC for other ACCs."
        ),
    )
    namespace_id: int | None = Field(
        default=None,
        ge=1,
        description="Namespace identifier. If provided, it must belong to the same library as the target release.",
    )
    tag_id: list[int] | None = Field(
        default=None,
        description="Optional tag identifier list to attach. Use `List tags` to discover valid tag IDs.",
        examples=[[1, 2, 3]],
    )


class CreateAccResponse(BaseModel):
    """Response payload for creating an ACC."""

    acc_manifest_id: int = Field(..., ge=1, description="Created ACC manifest identifier.")

    model_config = ConfigDict(frozen=True)


class CreateAsccpRequest(BaseModel):
    """Request payload for creating an ASCCP."""

    release_id: int = Field(..., ge=1, description="Target release identifier.")
    role_of_acc_manifest_id: int = Field(
        ...,
        ge=1,
        description=(
            "Role ACC manifest identifier. This identifies the ACC that this ASCCP points to as the associated ACC in the relationship. "
            "If the role ACC is in the same library as `release_id`, it must be "
            "from that release. If it is in a different library, its release must be one of the target release "
            "dependencies."
        ),
    )
    property_term: str = Field(
        ...,
        min_length=1,
        description=(
            "Property term to use for this ASCCP. In CCTS, this is a semantically meaningful name for the "
            "characteristic that represents the nature of the association to the associated ACC."
        ),
    )
    reusable_indicator: bool = Field(default=True, description="Whether this ASCCP should start as reusable.")
    namespace_id: int | None = Field(default=None, ge=1, description="Namespace to use for this ASCCP.")
    definition: str | None = Field(
        default=None,
        description="Definition text to start with. This is the explanatory text that describes what the ASCCP means.",
    )
    definition_source: str | None = Field(
        default=None,
        description=(
            "Definition source to start with. Use this to record where the definition came from, such as a "
            "specification, standard, or reference URL."
        ),
    )
    tag_id: list[int] | None = Field(default=None, description="Tag IDs to attach when the ASCCP is created.")


class CreateAsccpResponse(BaseModel):
    """Response payload for creating an ASCCP."""

    asccp_manifest_id: int = Field(..., ge=1, description="Created ASCCP manifest identifier.")

    model_config = ConfigDict(frozen=True)


class CreateBccpRequest(BaseModel):
    """Request payload for creating a BCCP."""

    release_id: int = Field(..., ge=1, description="Target release identifier.")
    bdt_manifest_id: int = Field(
        ...,
        ge=1,
        description=(
            "Target BDT manifest identifier. The selected data type must already be a BDT, "
            "which means its base DT link is set. If the BDT is in the same library as `release_id`, it must be "
            "from that release. If it is in a different library, its release must be one of the target release "
            "dependencies."
        ),
    )
    property_term: str = Field(
        ...,
        min_length=1,
        description=(
            "Property term to use for this BCCP. In CCTS, this is a semantically meaningful name for a unique "
            "characteristic that can be used in an ACC object class."
        ),
    )
    definition: str | None = Field(
        default=None,
        description="Definition text to start with. This is the explanatory text that describes what the BCCP means. Set `null` to leave it empty.",
    )
    definition_source: str | None = Field(
        default=None,
        description=(
            "Definition source to start with. Use this to record where the definition came from, such as a "
            "specification, standard, or reference URL. Set `null` to leave it empty."
        ),
    )
    deprecated: bool | None = Field(default=None, description="Whether this BCCP should start as deprecated.")
    is_nillable: bool | None = Field(default=None, description="Whether this BCCP should allow nil values when created.")
    namespace_id: int | None = Field(
        default=None,
        ge=1,
        description="Namespace to use for this BCCP. Set `null` to leave it empty.",
    )
    value_constraint: BccValueConstraintRequest | None = Field(
        default=None,
        description="Value constraint to start with. Provide one of `default_value` or `fixed_value`. Set `null` to leave it empty.",
    )
    tag_id: list[int] | None = Field(default=None, description="Tag IDs to attach when the BCCP is created.")


class CreateBccpResponse(BaseModel):
    """Response payload for creating a BCCP."""

    bccp_manifest_id: int = Field(..., ge=1, description="Created BCCP manifest identifier.")

    model_config = ConfigDict(frozen=True)


class AddAsccToAccResponse(BaseModel):
    """Response payload for adding an ASCC to an ACC."""

    ascc_manifest_id: int = Field(..., ge=1, description="ASCC manifest identifier.")

    model_config = ConfigDict(frozen=True)


class AddBccToAccResponse(BaseModel):
    """Response payload for adding a BCC to an ACC."""

    bcc_manifest_id: int = Field(..., ge=1, description="BCC manifest identifier.")

    model_config = ConfigDict(frozen=True)


class _MoveAccAssociationRequestBase(BaseModel):
    """Shared request payload for reordering an ACC child within the sequence."""

    index: int | None = Field(
        default=None,
        description=(
            "Optional zero-based insertion index. Use `0` to place the association first, `-1` to place it last, "
            "or another non-negative index to place the association at that position in the sequence. "
            "Mutually exclusive with all `after_*` and `before_*` options."
        ),
    )
    after_ascc_manifest_id: int | None = Field(
        default=None,
        ge=1,
        description="Place the association after this ASCC manifest identifier. Mutually exclusive with `index` and all other `after_*`/`before_*` options.",
    )
    after_bcc_manifest_id: int | None = Field(
        default=None,
        ge=1,
        description="Place the association after this BCC manifest identifier. Mutually exclusive with `index` and all other `after_*`/`before_*` options.",
    )
    before_ascc_manifest_id: int | None = Field(
        default=None,
        ge=1,
        description="Place the association before this ASCC manifest identifier. Mutually exclusive with `index` and all other `after_*`/`before_*` options.",
    )
    before_bcc_manifest_id: int | None = Field(
        default=None,
        ge=1,
        description="Place the association before this BCC manifest identifier. Mutually exclusive with `index` and all other `after_*`/`before_*` options.",
    )

    model_config = ConfigDict(frozen=True)

    @model_validator(mode="after")
    def validate_target_selector(self) -> "_MoveAccAssociationRequestBase":
        """Ensure the caller chooses at most one sequence target selector."""
        provided = sum(
            value is not None
            for value in (
                self.index,
                self.after_ascc_manifest_id,
                self.after_bcc_manifest_id,
                self.before_ascc_manifest_id,
                self.before_bcc_manifest_id,
            )
        )
        if provided > 1:
            raise ValueError("Provide only one of `index`, `after_*`, or `before_*`.")
        return self


class MoveAsccRequest(_MoveAccAssociationRequestBase):
    """Request payload for reordering an ASCC within an ACC sequence."""


class MoveBccRequest(_MoveAccAssociationRequestBase):
    """Request payload for reordering a BCC within an ACC sequence."""


class ReorderAsccInAccResponse(BaseModel):
    """Response payload for reordering an ASCC in an ACC."""

    ascc_manifest_id: int = Field(..., ge=1, description="Reordered ASCC manifest identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.", examples=[["sequence"]])

    model_config = ConfigDict(frozen=True)


class ReorderBccInAccResponse(BaseModel):
    """Response payload for reordering a BCC in an ACC."""

    bcc_manifest_id: int = Field(..., ge=1, description="Reordered BCC manifest identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.", examples=[["sequence"]])

    model_config = ConfigDict(frozen=True)


class UpdateAccRequest(BaseModel):
    """Request payload for updating mutable ACC fields."""

    based_acc_manifest_id: int | None = Field(
        default=None,
        ge=1,
        description=(
            "Base ACC. If the base ACC is in the same library as the target ACC, it must come from the same release. "
            "If it is in a different library, its release must be one of the target ACC release dependencies. "
            "Omit to leave unchanged. Set `null` to remove the current base ACC."
        ),
    )
    object_class_term: str | None = Field(
        default=None,
        description=(
            "Object class term. In CCTS, this is the term that represents the activity or "
            "object the ACC stands for. It serves as the basis for the ACC DEN and for the DENs of the ASCC and "
            "BCC properties under that ACC. Omit to leave unchanged."
        ),
    )
    component_type: Literal[
        "Base",
        "Semantics",
        "Extension",
        "SemanticGroup",
        "UserExtensionGroup",
        "Embedded",
        "OAGIS10Nouns",
        "OAGIS10BODs",
        "BOD",
        "Verb",
        "Noun",
        "Choice",
        "AttributeGroup",
    ] | None = Field(
        default=None,
        description=(
            "OAGIS component type. Use `Base` only when this ACC is meant to be the base ACC "
            "for other ACCs. Use `Extension` for a developer extension ACC that end-users may extend later in BIEs. "
            "Use `SemanticGroup` for a grouping ACC whose associations are flattened in BIEs. In most other "
            "cases, use `Semantics`. Omit to leave unchanged."
        ),
    )
    definition: str | None = Field(
        default=None,
        description="Definition text. This is the explanatory text that describes what the ACC means. Omit to leave unchanged. Set `null` to clear it.",
    )
    definition_source: str | None = Field(
        default=None,
        description=(
            "Definition source. Use this to record where the definition came from, such as a "
            "specification, standard, or reference URL. Omit to leave unchanged. Set `null` to clear it."
        ),
    )
    is_abstract: bool | None = Field(
        default=None,
        description=(
            "Whether this ACC should be abstract. If `component_type` is `Base`, this is usually `true`, because "
            "the ACC is meant to serve as a base ACC for other ACCs. Omit to leave unchanged."
        ),
    )
    deprecated: bool | None = Field(default=None, description="Whether this ACC should be deprecated. Omit to leave unchanged.")
    namespace_id: int | None = Field(
        default=None,
        ge=1,
        description="Namespace. If provided, it must belong to the same library as the ACC release. Omit to leave unchanged. Set `null` to clear it.",
    )

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "object_class_term": "Order",
                "component_type": "Semantics",
                "definition": "A document used to request goods or services.",
                "definition_source": "https://www.oagis.org",
                "is_abstract": False,
                "deprecated": False,
                "based_acc_manifest_id": 12,
                "namespace_id": 1,
            }
        },
    )


class UpdateAsccRequest(_MoveAccAssociationRequestBase):
    """Request payload for updating mutable ASCC fields."""

    cardinality_min: int | None = Field(
        default=None,
        ge=0,
        description=(
            "Minimum cardinality to use. Use `0` or a positive integer. If `cardinality_max` is also provided "
            "and is not `-1`, `cardinality_min` must be less than or equal to `cardinality_max`. Omit to leave unchanged."
        ),
    )
    cardinality_max: int | None = Field(
        default=None,
        ge=-1,
        description=(
            "Maximum cardinality to use. Use `-1` for unbounded, or `0` or a positive integer for a bounded "
            "maximum. If it is not `-1`, it must be greater than or equal to `cardinality_min`. Omit to leave unchanged."
        ),
    )
    definition: str | None = Field(
        default=None,
        description="Definition text. This is the explanatory text that describes what the ASCCP means. Omit to leave unchanged. Set `null` to clear it.",
    )
    definition_source: str | None = Field(
        default=None,
        description=(
            "Definition source. Use this to record where the definition came from, such as a "
            "specification, standard, or reference URL. Omit to leave unchanged. Set `null` to clear it."
        ),
    )
    deprecated: bool | None = Field(default=None, description="Whether this ASCC should be deprecated. Omit to leave unchanged.")

    @model_validator(mode="after")
    def validate_cardinality_range(self) -> "UpdateAsccRequest":
        """Ensure the optional ASCC cardinality range is internally consistent."""
        if (
            self.cardinality_min is not None
            and self.cardinality_max is not None
            and self.cardinality_max != -1
            and self.cardinality_min > self.cardinality_max
        ):
            raise ValueError("`cardinality_min` cannot be greater than `cardinality_max`.")
        return self

class AddAsccToAccRequest(_MoveAccAssociationRequestBase):
    """Optional request payload for setting ASCC fields at create time."""

    cardinality_min: int | None = Field(
        default=None,
        ge=0,
        description=(
            "Minimum cardinality to start with. Use `0` or a positive integer. If `cardinality_max` is also "
            "provided and is not `-1`, `cardinality_min` must be less than or equal to `cardinality_max`."
        ),
    )
    cardinality_max: int | None = Field(
        default=None,
        ge=-1,
        description=(
            "Maximum cardinality to start with. Use `-1` for unbounded, or `0` or a positive integer for a "
            "bounded maximum. If it is not `-1`, it must be greater than or equal to `cardinality_min`."
        ),
    )
    definition: str | None = Field(
        default=None,
        description="Definition text to start with. This is the explanatory text that describes what the BCC means. Set `null` to leave it empty.",
    )
    definition_source: str | None = Field(
        default=None,
        description=(
            "Definition source to start with. Use this to record where the definition came from, such as a "
            "specification, standard, or reference URL. Set `null` to leave it empty."
        ),
    )

    @model_validator(mode="after")
    def validate_cardinality_range(self) -> "AddAsccToAccRequest":
        """Ensure the optional ASCC cardinality range is internally consistent."""
        if (
            self.cardinality_min is not None
            and self.cardinality_max is not None
            and self.cardinality_max != -1
            and self.cardinality_min > self.cardinality_max
        ):
            raise ValueError("`cardinality_min` cannot be greater than `cardinality_max`.")
        return self

BccEntityTypeUpdate = Literal["Element", "Attribute", 1, 0]


def _is_attribute_bcc_entity_type(value: BccEntityTypeUpdate | None) -> bool:
    """Return whether a BCC entity-type input refers to `Attribute`."""
    return value in {"Attribute", 0}


class BccValueConstraintRequest(BaseModel):
    """Mutually exclusive value-constraint selection for BCC updates."""

    default_value: str | None = Field(
        default=None,
        description="Default value to apply when the element is omitted.",
    )
    fixed_value: str | None = Field(
        default=None,
        description="Fixed value to require for the element.",
    )

    @model_validator(mode="after")
    def validate_exactly_one_selection(self) -> "BccValueConstraintRequest":
        """Require exactly one value-constraint option."""
        provided = [self.default_value, self.fixed_value]
        if sum(value is not None for value in provided) != 1:
            raise ValueError("Exactly one of default_value or fixed_value must be provided.")
        return self

    model_config = ConfigDict(frozen=True)


class AddBccToAccRequest(_MoveAccAssociationRequestBase):
    """Optional request payload for setting BCC fields at create time."""

    cardinality_min: int | None = Field(
        default=None,
        ge=0,
        description=(
            "Minimum cardinality to start with. Use `0` or a positive integer. If `cardinality_max` is also "
            "provided and is not `-1`, `cardinality_min` must be less than or equal to `cardinality_max`."
        ),
    )
    cardinality_max: int | None = Field(
        default=None,
        ge=-1,
        description=(
            "Maximum cardinality to start with. Use `-1` for unbounded, or `0` or a positive integer for a "
            "bounded maximum. If it is not `-1`, it must be greater than or equal to `cardinality_min`. "
            "If `entity_type` is `Attribute`, this value must be `0` or `1`; `-1` is not allowed."
        ),
    )
    entity_type: BccEntityTypeUpdate | None = Field(
        default=None,
        description="Entity type to start with. Use `Element` or `Attribute`. Integer aliases `1` and `0` are also accepted.",
    )
    is_nillable: bool | None = Field(default=None, description="Whether this BCC should allow nil values when created.")
    definition: str | None = Field(default=None, description="Definition text to start with. Set `null` to leave it empty.")
    definition_source: str | None = Field(
        default=None,
        description="Definition source to start with. Set `null` to leave it empty.",
    )
    value_constraint: BccValueConstraintRequest | None = Field(
        default=None,
        description="Value constraint to start with. Provide exactly one of `default_value` or `fixed_value`.",
    )

    @model_validator(mode="after")
    def validate_cardinality_range(self) -> "AddBccToAccRequest":
        """Ensure the optional BCC cardinality range is internally consistent."""
        if (
            self.cardinality_min is not None
            and self.cardinality_max is not None
            and self.cardinality_max != -1
            and self.cardinality_min > self.cardinality_max
        ):
            raise ValueError("`cardinality_min` cannot be greater than `cardinality_max`.")
        if _is_attribute_bcc_entity_type(self.entity_type):
            if self.cardinality_min is not None and self.cardinality_min > 1:
                raise ValueError("`cardinality_min` cannot be greater than 1 for `Attribute` BCCs.")
            if self.cardinality_max is not None and (self.cardinality_max == -1 or self.cardinality_max > 1):
                raise ValueError("`cardinality_max` cannot be greater than 1 for `Attribute` BCCs.")
        return self

class UpdateBccRequest(_MoveAccAssociationRequestBase):
    """Request payload for updating mutable BCC fields."""

    entity_type: BccEntityTypeUpdate | None = Field(
        default=None,
        description="Entity type to use. Choose `Element` or `Attribute`. Integer aliases `1` and `0` are also accepted. Omit to leave unchanged.",
    )
    cardinality_min: int | None = Field(
        default=None,
        ge=0,
        description=(
            "Minimum cardinality to use. Use `0` or a positive integer. If `cardinality_max` is also provided "
            "and is not `-1`, `cardinality_min` must be less than or equal to `cardinality_max`. Omit to leave unchanged."
        ),
    )
    cardinality_max: int | None = Field(
        default=None,
        ge=-1,
        description=(
            "Maximum cardinality to use. Use `-1` for unbounded, or `0` or a positive integer for a bounded "
            "maximum. If it is not `-1`, it must be greater than or equal to `cardinality_min`. If `entity_type` "
            "changes from `Element` to `Attribute`, `-1` or a value greater than `1` is normalized to `1`. Omit to leave unchanged."
        ),
    )
    definition: str | None = Field(
        default=None,
        description="Definition text. This is the explanatory text that describes what the BCC means. Omit to leave unchanged. Set `null` to clear it.",
    )
    definition_source: str | None = Field(
        default=None,
        description=(
            "Definition source. Use this to record where the definition came from, such as a "
            "specification, standard, or reference URL. Omit to leave unchanged. Set `null` to clear it."
        ),
    )
    deprecated: bool | None = Field(default=None, description="Whether this BCC should be deprecated. Omit to leave unchanged.")
    is_nillable: bool | None = Field(default=None, description="Whether this BCC should allow nil values. Omit to leave unchanged.")
    value_constraint: BccValueConstraintRequest | None = Field(
        default=None,
        description="Value constraint. Provide one of `default_value` or `fixed_value`. Omit to leave unchanged. Set `null` to clear the current value constraint.",
    )

    @model_validator(mode="after")
    def validate_cardinality_range(self) -> "UpdateBccRequest":
        """Ensure the optional BCC cardinality range is internally consistent."""
        if (
            self.cardinality_min is not None
            and self.cardinality_max is not None
            and self.cardinality_max != -1
            and self.cardinality_min > self.cardinality_max
        ):
            raise ValueError("`cardinality_min` cannot be greater than `cardinality_max`.")
        return self

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "entity_type": "Element",
                "cardinality_min": 0,
                "cardinality_max": 1,
                "definition": "The monetary amount of the purchase order.",
                "definition_source": "https://www.oagis.org",
                "deprecated": False,
                "is_nillable": False,
                "value_constraint": {
                    "default_value": "0.00",
                },
            }
        },
    )


class UpdateAsccpRequest(BaseModel):
    """Request payload for updating mutable ASCCP fields."""

    role_of_acc_manifest_id: int | None = Field(
        default=None,
        ge=1,
        description="Role ACC. This identifies the ACC that this ASCCP points to as the associated ACC in the relationship. "
                    "If the role ACC is in the same library as `release_id`, it must be from that release. If it is in a different library, "
                    "its release must be one of the target release dependencies. Omit to leave unchanged.",
    )
    property_term: str | None = Field(
        default=None,
        description=(
            "Property term. In CCTS, this is a semantically meaningful name for the "
            "characteristic that expresses the nature of the association to the associated ACC. Omit to leave unchanged."
        ),
    )
    definition: str | None = Field(
        default=None,
        description="Definition text. This is the explanatory text that describes what the ASCCP means. Omit to leave unchanged. Set `null` to clear it.",
    )
    definition_source: str | None = Field(
        default=None,
        description=(
            "Definition source. Use this to record where the definition came from, such as a "
            "specification, standard, or reference URL. Omit to leave unchanged. Set `null` to clear it."
        ),
    )
    reusable_indicator: bool | None = Field(default=None, description="Whether this ASCCP should be reusable. Omit to leave unchanged.")
    deprecated: bool | None = Field(default=None, description="Whether this ASCCP should be deprecated. Omit to leave unchanged.")
    is_nillable: bool | None = Field(default=None, description="Whether this ASCCP should allow nil values. Omit to leave unchanged.")
    namespace_id: int | None = Field(default=None, ge=1, description="Namespace. Omit to leave unchanged. Set `null` to clear it.")

    model_config = ConfigDict(frozen=True)


class UpdateBccpRequest(BaseModel):
    """Request payload for updating mutable BCCP fields."""

    bdt_manifest_id: int | None = Field(default=None, ge=1, description="BDT. Omit to leave unchanged. The selected data type must already be a BDT, which means its base DT link is set. If the BDT is in the same library as the BCCP, it must come from the BCCP release. If it is in a different library, its release must be one of the BCCP release dependencies.")
    property_term: str | None = Field(
        default=None,
        description=(
            "Property term. In CCTS, this is a semantically meaningful name for a unique "
            "characteristic that can be used in an ACC object class. Omit to leave unchanged."
        ),
    )
    definition: str | None = Field(
        default=None,
        description="Definition text. This is the explanatory text that describes what the BCCP means. Omit to leave unchanged. Set `null` to clear it.",
    )
    definition_source: str | None = Field(
        default=None,
        description=(
            "Definition source. Use this to record where the definition came from, such as a "
            "specification, standard, or reference URL. Omit to leave unchanged. Set `null` to clear it."
        ),
    )
    deprecated: bool | None = Field(default=None, description="Whether this BCCP should be deprecated. Omit to leave unchanged.")
    is_nillable: bool | None = Field(default=None, description="Whether this BCCP should allow nil values. Omit to leave unchanged.")
    namespace_id: int | None = Field(default=None, ge=1, description="Namespace. Omit to leave unchanged. Set `null` to clear it.")
    value_constraint: BccValueConstraintRequest | None = Field(
        default=None,
        description="Value constraint. Provide one of `default_value` or `fixed_value`. Omit to leave unchanged. Set `null` to clear the current value constraint.",
    )

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "bdt_manifest_id": 18,
                "property_term": "Amount",
                "definition": "The amount for this basic core component property.",
                "definition_source": "https://www.oagis.org",
                "deprecated": False,
                "is_nillable": False,
                "value_constraint": {
                    "fixed_value": "X",
                },
            }
        },
    )


class UpdateAccStateRequest(BaseModel):
    """Request payload for ACC state transitions."""

    state: Literal[
        "Deleted",
        "WIP",
        "Draft",
        "QA",
        "Candidate",
        "Production",
    ] = Field(..., description="Target ACC lifecycle state.")

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={"example": {"state": "Draft"}},
    )


class UpdateAsccpStateRequest(BaseModel):
    """Request payload for ASCCP state transitions."""

    state: Literal["Deleted", "WIP", "Draft", "QA", "Candidate", "Production"] = Field(
        ...,
        description="Target ASCCP lifecycle state.",
    )

    model_config = ConfigDict(frozen=True)


class UpdateBccpStateRequest(BaseModel):
    """Request payload for BCCP state transitions."""

    state: Literal["Deleted", "WIP", "Draft", "QA", "Candidate", "Production"] = Field(
        ...,
        description="Target BCCP lifecycle state.",
    )

    model_config = ConfigDict(frozen=True)


class TransferOwnershipRequest(BaseModel):
    """Request payload for core-component ownership transfer."""

    target_user_id: int = Field(..., ge=1, description="Target owner user ID.")

    model_config = ConfigDict(frozen=True)


class UpdateAccResponse(BaseModel):
    """Response payload for ACC update operations."""

    acc_manifest_id: int = Field(..., ge=1, description="Target ACC manifest identifier.", examples=[1])
    updates: list[str] = Field(default_factory=list, description="Updated field names.", examples=[["definition"]])

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "acc_manifest_id": 1,
                "updates": ["definition", "definition_source"],
            }
        },
    )


class UpdateAsccResponse(BaseModel):
    """Response payload for ASCC update operations."""

    ascc_manifest_id: int = Field(..., ge=1, description="Target ASCC manifest identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.")

    model_config = ConfigDict(frozen=True)


class UpdateBccResponse(BaseModel):
    """Response payload for BCC update operations."""

    bcc_manifest_id: int = Field(..., ge=1, description="Target BCC manifest identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.")

    model_config = ConfigDict(frozen=True)


class UpdateAsccpResponse(BaseModel):
    """Response payload for ASCCP update operations."""

    asccp_manifest_id: int = Field(..., ge=1, description="Target ASCCP manifest identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.")

    model_config = ConfigDict(frozen=True)


class UpdateBccpResponse(BaseModel):
    """Response payload for BCCP update operations."""

    bccp_manifest_id: int = Field(..., ge=1, description="Target BCCP manifest identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.")

    model_config = ConfigDict(frozen=True)


class TransferAccOwnershipResponse(BaseModel):
    """Response payload for ACC ownership transfer."""

    acc_manifest_id: int = Field(..., ge=1, description="Target ACC manifest identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.")

    model_config = ConfigDict(frozen=True)


class TransferAsccpOwnershipResponse(BaseModel):
    """Response payload for ASCCP ownership transfer."""

    asccp_manifest_id: int = Field(..., ge=1, description="Target ASCCP manifest identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.")

    model_config = ConfigDict(frozen=True)


class TransferBccpOwnershipResponse(BaseModel):
    """Response payload for BCCP ownership transfer."""

    bccp_manifest_id: int = Field(..., ge=1, description="Target BCCP manifest identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.")

    model_config = ConfigDict(frozen=True)


class UpdateAccTagsResponse(BaseModel):
    """Response payload for ACC tag update operations."""

    acc_manifest_id: int = Field(..., ge=1, description="Target ACC manifest identifier.", examples=[1])
    updates: list[str] = Field(
        default_factory=list,
        description="Updated field names.",
        examples=[["tags"]],
    )

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "acc_manifest_id": 1,
                "updates": ["tags"],
            }
        },
    )


class ChangeAccStateResponse(BaseModel):
    """Response payload for ACC state change operations."""

    acc_manifest_id: int = Field(..., ge=1, description="Target ACC manifest identifier.", examples=[1])
    updates: list[str] = Field(default_factory=list, description="Updated field names.", examples=[["state:Draft"]])

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "acc_manifest_id": 1,
                "updates": ["state:Draft"],
            }
        },
    )


class DiscardAccResponse(BaseModel):
    """Response payload for ACC discard operations."""

    acc_manifest_id: int = Field(..., ge=1, description="Target ACC manifest identifier.", examples=[1])
    discarded: bool = Field(..., description="Whether the ACC was permanently discarded.", examples=[True])

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "acc_manifest_id": 1,
                "discarded": True,
            }
        },
    )


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
    property_term: str | None = Field(
        default=None,
        description=(
            "ASCCP property term. In CCTS, this is the semantically meaningful name for the characteristic that "
            "represents the nature of the association to the associated ACC."
        ),
    )
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
    tags: list[TagSummaryRecord] = Field(default_factory=list, description="ACC tags.")
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
    property_term: str | None = Field(
        default=None,
        description=(
            "ASCCP property term. In CCTS, this is the semantically meaningful name for the characteristic that "
            "represents the nature of the association to the associated ACC."
        ),
    )
    definition: str | None = Field(default=None, description="ASCCP definition.")
    definition_source: str | None = Field(default=None, description="ASCCP definition source.")
    reusable_indicator: bool = Field(..., description="Reusable indicator.")
    is_nillable: bool | None = Field(default=None, description="Nillable indicator.")
    is_deprecated: bool = Field(..., description="Deprecated indicator.")
    state: str | None = Field(default=None, description="ASCCP state.")
    namespace: NamespaceSummaryRecord | None = Field(default=None, description="ASCCP namespace.")
    library: LibrarySummaryRecord = Field(..., description="ASCCP library.")
    release: ReleaseSummaryRecord = Field(..., description="ASCCP release.")
    tags: list[TagSummaryRecord] = Field(default_factory=list, description="ASCCP tags.")
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
    tags: list[TagSummaryRecord] = Field(default_factory=list, description="BCCP tags.")
    log: LogSummaryRecord | None = Field(default=None, description="Revision log.")
    owner: UserSummary = Field(..., description="Owner information.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(frozen=True)
