"""Models for Data Type MCP tools."""

from __future__ import annotations

from pydantic import BaseModel, ConfigDict, Field, field_validator, model_validator

from app.services.utils.string import Guid
from app.tools.models.shared import LibrarySummaryRecord
from app.tools.models.shared import LogSummaryRecord
from app.tools.models.shared import NamespaceSummaryRecord
from app.tools.models.shared import ReleaseSummaryRecord
from app.tools.models.shared import TagSummaryRecord
from app.tools.models.shared import UserSummary
from app.tools.models.shared import ValueConstraintRecord
from app.tools.models.shared import WhoAndWhen


class DataTypeBaseResponse(BaseModel):
    """Base data-type payload for MCP tools."""

    dt_manifest_id: int
    dt_id: int
    based_dt_manifest_id: int | None = None
    guid: Guid
    den: str
    data_type_term: str | None = None
    qualifier: str | None = None
    representation_term: str | None = None
    six_digit_id: str | None = None
    definition: str | None = None
    definition_source: str | None = None
    content_component_definition: str | None = None
    is_deprecated: bool = False
    namespace: NamespaceSummaryRecord | None = None
    library: LibrarySummaryRecord
    release: ReleaseSummaryRecord

    model_config = ConfigDict(frozen=True, from_attributes=True)


class DataTypeSupplementaryComponentResponse(BaseModel):
    """Supplementary-component payload for MCP tools."""

    dt_sc_manifest_id: int
    dt_sc_id: int
    guid: Guid
    object_class_term: str | None = None
    property_term: str | None = None
    representation_term: str | None = None
    definition: str | None = None
    definition_source: str | None = None
    cardinality: str
    value_constraint: ValueConstraintRecord | None = None
    is_deprecated: bool
    primitives: list[DataTypePrimitiveResponse] = Field(default_factory=list)

    model_config = ConfigDict(frozen=True, from_attributes=True)


class DataTypePrimitiveResponse(BaseModel):
    """Primitive selection payload for MCP tools."""

    cdt_pri_name: str | None = None
    xbt_manifest_id: int | None = None
    code_list_manifest_id: int | None = None
    agency_id_list_manifest_id: int | None = None
    is_default: bool

    model_config = ConfigDict(frozen=True, from_attributes=True)


_CDT_PRI_NAMES = (
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
)


class DefaultPrimitiveSelectionInput(BaseModel):
    """Mutually exclusive default-primitive target for DT update tools."""

    xbt_manifest_id: int | None = Field(default=None, gt=0)
    code_list_manifest_id: int | None = Field(default=None, gt=0)
    agency_id_list_manifest_id: int | None = Field(default=None, gt=0)

    @model_validator(mode="after")
    def validate_exactly_one_selection(self) -> "DefaultPrimitiveSelectionInput":
        """Require exactly one primitive target."""
        provided = [
            self.xbt_manifest_id,
            self.code_list_manifest_id,
            self.agency_id_list_manifest_id,
        ]
        if sum(value is not None for value in provided) != 1:
            raise ValueError(
                "Exactly one of xbt_manifest_id, code_list_manifest_id, or agency_id_list_manifest_id must be provided."
            )
        return self

    model_config = ConfigDict(frozen=True)


class PrimitiveMutationInput(BaseModel):
    """Primitive add/remove payload for DT update tools."""

    cdt_pri_name: str | None = Field(
        default=None,
        description=(
            "CDT primitive name. Required when xbt_manifest_id is provided. When provided, it must "
            "be one of: Binary, Boolean, Decimal, Double, Float, Integer, NormalizedString, String, "
            "TimeDuration, TimePoint, Token."
        ),
    )
    xbt_manifest_id: int | None = Field(default=None, gt=0)
    code_list_manifest_id: int | None = Field(default=None, gt=0)
    agency_id_list_manifest_id: int | None = Field(default=None, gt=0)

    @field_validator("cdt_pri_name")
    @classmethod
    def validate_cdt_pri_name(cls, value: str | None) -> str | None:
        """Require a supported CDT primitive name when populated."""
        if value is None:
            return None
        if value not in _CDT_PRI_NAMES:
            allowed = ", ".join(_CDT_PRI_NAMES)
            raise ValueError(f"cdt_pri_name must be one of: {allowed}.")
        return value

    @model_validator(mode="after")
    def validate_exactly_one_selection(self) -> "PrimitiveMutationInput":
        """Require exactly one primitive target and an XBT primitive name when needed."""
        provided = [
            self.xbt_manifest_id,
            self.code_list_manifest_id,
            self.agency_id_list_manifest_id,
        ]
        if sum(value is not None for value in provided) != 1:
            raise ValueError(
                "Exactly one of xbt_manifest_id, code_list_manifest_id, or agency_id_list_manifest_id must be provided."
            )
        if self.xbt_manifest_id is not None and self.cdt_pri_name is None:
            raise ValueError("cdt_pri_name must be provided when xbt_manifest_id is provided.")
        return self

    model_config = ConfigDict(frozen=True)


class ValueConstraintInput(BaseModel):
    """Mutually exclusive value-constraint input for DT_SC updates."""

    default_value: str | None = Field(
        default=None,
        description="Default value to apply when the element is omitted.",
    )
    fixed_value: str | None = Field(
        default=None,
        description="Fixed value to require for the element.",
    )

    @model_validator(mode="after")
    def validate_exactly_one_selection(self) -> "ValueConstraintInput":
        """Require exactly one value-constraint option."""
        provided = [self.default_value, self.fixed_value]
        if sum(value is not None for value in provided) != 1:
            raise ValueError("Exactly one of default_value or fixed_value must be provided.")
        return self

    model_config = ConfigDict(frozen=True)


class DataTypeResponseEntry(BaseModel):
    """Data-type payload for MCP tools."""

    dt_manifest_id: int
    dt_id: int
    base_dt: DataTypeBaseResponse | None = None
    guid: Guid
    den: str
    data_type_term: str | None = None
    qualifier: str | None = None
    representation_term: str | None = None
    six_digit_id: str | None = None
    definition: str | None = None
    definition_source: str | None = None
    content_component_definition: str | None = None
    commonly_used: bool
    is_deprecated: bool
    state: str | None = None
    primitives: list[DataTypePrimitiveResponse] = Field(default_factory=list)
    supplementary_components: list[DataTypeSupplementaryComponentResponse]
    tags: list[TagSummaryRecord] = Field(default_factory=list)
    namespace: NamespaceSummaryRecord | None = None
    library: LibrarySummaryRecord
    release: ReleaseSummaryRecord
    log: LogSummaryRecord | None = None
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetDataTypeResponse(DataTypeResponseEntry):
    """Response for get_data_type tool."""


class GetDataTypePaginationResponse(BaseModel):
    """Response for get_data_types tool."""

    total_items: int
    offset: int
    limit: int
    items: list[DataTypeResponseEntry]

    model_config = ConfigDict(frozen=True)


class CreateDataTypeResponse(BaseModel):
    """Response for create_dt tool."""

    dt_manifest_id: int

    model_config = ConfigDict(frozen=True)


class UpdateDataTypeResponse(BaseModel):
    """Response for update_dt tool."""

    dt_manifest_id: int
    updates: list[str] = Field(default_factory=list)

    model_config = ConfigDict(frozen=True)


class TransferDataTypeOwnershipResponse(BaseModel):
    """Response for transfer_dt_ownership tool."""

    dt_manifest_id: int
    updates: list[str] = Field(default_factory=list)

    model_config = ConfigDict(frozen=True)


class CreateDataTypeSupplementaryComponentResponse(BaseModel):
    """Response for create_dt_sc tool."""

    dt_sc_manifest_id: int

    model_config = ConfigDict(frozen=True)


class UpdateDataTypeSupplementaryComponentResponse(BaseModel):
    """Response for update_dt_sc tool."""

    dt_sc_manifest_id: int
    updates: list[str] = Field(default_factory=list)

    model_config = ConfigDict(frozen=True)
