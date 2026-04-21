"""Pydantic request and response models for Data Type endpoints."""


from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, ConfigDict, Field, field_validator, model_validator

from app.routes.models.shared import LibrarySummaryRecord
from app.routes.models.shared import LogSummaryRecord
from app.routes.models.shared import NamespaceSummaryRecord
from app.routes.models.shared import ReleaseSummaryRecord
from app.routes.models.shared import TagSummaryRecord
from app.routes.models.shared import UserSummary
from app.routes.models.shared import ValueConstraintRecord
from app.routes.models.shared import WhoAndWhen
from app.services.utils.string import Guid


class DataTypeSupplementaryComponentEntry(BaseModel):
    """Data type supplementary component information."""

    dt_sc_manifest_id: int = Field(
        ...,
        description="Unique identifier for the supplementary component manifest.",
    )
    dt_sc_id: int = Field(
        ...,
        description="Unique identifier for the supplementary component.",
    )
    guid: Guid = Field(..., description="Globally unique identifier for the supplementary component.")
    object_class_term: str | None = Field(default=None, description="Object class term.")
    property_term: str | None = Field(default=None, description="Property term.")
    representation_term: str | None = Field(default=None, description="Representation term.")
    definition: str | None = Field(default=None, description="Definition of the supplementary component.")
    definition_source: str | None = Field(default=None, description="Definition source URL.")
    cardinality: Literal["Prohibited", "Optional", "Required"] = Field(
        ...,
        description="Supplementary-component cardinality label used by SCORE.",
    )
    value_constraint: ValueConstraintRecord | None = Field(default=None, description="Value constraint.")
    is_deprecated: bool = Field(..., description="Whether this supplementary component is deprecated.")
    primitives: list[DataTypePrimitiveEntry] = Field(
        default_factory=list,
        description="Allowed primitive selections for this supplementary component.",
    )

    model_config = ConfigDict(frozen=True)


class DataTypePrimitiveEntry(BaseModel):
    """Primitive selection for a DT or DT_SC."""

    cdt_pri_name: str | None = Field(
        default=None,
        description="CDT primitive name. Provided only when xbt_manifest_id is set.",
    )
    xbt_manifest_id: int | None = Field(default=None, description="XBT manifest identifier.")
    code_list_manifest_id: int | None = Field(default=None, description="Code list manifest identifier.")
    agency_id_list_manifest_id: int | None = Field(
        default=None,
        description="Agency-ID-list manifest identifier.",
    )
    is_default: bool = Field(..., description="Whether this primitive is the default selection.")

    model_config = ConfigDict(frozen=True)


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


class DefaultPrimitiveSelectionRequest(BaseModel):
    """Mutually exclusive default-primitive target selection."""

    xbt_manifest_id: int | None = Field(default=None, ge=1, description="XBT manifest identifier to set as default.")
    code_list_manifest_id: int | None = Field(
        default=None,
        ge=1,
        description="Code list manifest identifier to set as default.",
    )
    agency_id_list_manifest_id: int | None = Field(
        default=None,
        ge=1,
        description="Agency-ID-list manifest identifier to set as default.",
    )

    @model_validator(mode="after")
    def validate_exactly_one_selection(self) -> "DefaultPrimitiveSelectionRequest":
        """Require exactly one default-primitive source to be selected."""
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


class PrimitiveMutationRequest(BaseModel):
    """Primitive add/remove request entry."""

    cdt_pri_name: str | None = Field(
        default=None,
        description=(
            "CDT primitive name. Required when xbt_manifest_id is provided. When provided, it must "
            "be one of: Binary, Boolean, Decimal, Double, Float, Integer, NormalizedString, String, "
            "TimeDuration, TimePoint, Token."
        ),
    )
    xbt_manifest_id: int | None = Field(default=None, ge=1, description="XBT manifest identifier.")
    code_list_manifest_id: int | None = Field(default=None, ge=1, description="Code list manifest identifier.")
    agency_id_list_manifest_id: int | None = Field(
        default=None,
        ge=1,
        description="Agency-ID-list manifest identifier.",
    )

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
    def validate_exactly_one_selection(self) -> "PrimitiveMutationRequest":
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


class ValueConstraintRequest(BaseModel):
    """Mutually exclusive value-constraint selection."""

    default_value: str | None = Field(
        default=None,
        description="Default value to apply when the element is omitted.",
    )
    fixed_value: str | None = Field(
        default=None,
        description="Fixed value to require for the element.",
    )

    @model_validator(mode="after")
    def validate_exactly_one_selection(self) -> "ValueConstraintRequest":
        """Require exactly one value-constraint option."""
        provided = [self.default_value, self.fixed_value]
        if sum(value is not None for value in provided) != 1:
            raise ValueError("Exactly one of default_value or fixed_value must be provided.")
        return self

    model_config = ConfigDict(frozen=True)


class DataTypeBaseSummaryEntry(BaseModel):
    """Summary information for a base data type."""

    dt_manifest_id: int = Field(..., description="Unique identifier for the base data type manifest.")
    dt_id: int = Field(..., description="Unique identifier for the base data type.")
    based_dt_manifest_id: int | None = Field(default=None, description="Base data type manifest ID of this base data type.")
    guid: Guid = Field(..., description="Globally unique identifier for the base data type.")
    den: str = Field(..., description="Dictionary Entry Name (DEN).")
    data_type_term: str | None = Field(default=None, description="Data type term.")
    qualifier: str | None = Field(default=None, description="Qualifier.")
    representation_term: str | None = Field(default=None, description="Representation term.")
    six_digit_id: str | None = Field(
        default=None,
        description=(
            "Six-character suffix for the BDT Type name from the UN/CEFACT XML Schema NDR. "
            "It must be unique within the namespace and use only letters and digits."
        ),
    )
    definition: str | None = Field(default=None, description="Definition.")
    definition_source: str | None = Field(default=None, description="Definition source URL.")
    content_component_definition: str | None = Field(default=None, description="Content component definition.")
    is_deprecated: bool = Field(..., description="Whether this data type is deprecated.")
    namespace: NamespaceSummaryRecord | None = Field(default=None, description="Namespace information.")
    library: LibrarySummaryRecord = Field(..., description="Library information.")
    release: ReleaseSummaryRecord = Field(..., description="Release information.")

    model_config = ConfigDict(frozen=True)


class DataTypeEntry(BaseModel):
    """API representation of a data type record."""

    dt_manifest_id: int = Field(..., description="Unique identifier for the data type manifest.")
    dt_id: int = Field(..., description="Unique identifier for the data type.")
    base_dt: DataTypeBaseSummaryEntry | None = Field(default=None, description="Base data type information.")
    guid: Guid = Field(..., description="Globally unique identifier.")
    den: str = Field(..., description="Dictionary Entry Name (DEN).")
    data_type_term: str | None = Field(default=None, description="Data type term.")
    qualifier: str | None = Field(default=None, description="Qualifier.")
    representation_term: str | None = Field(default=None, description="Representation term.")
    six_digit_id: str | None = Field(
        default=None,
        description=(
            "Six-character suffix for the BDT Type name from the UN/CEFACT XML Schema NDR. "
            "It must be unique within the namespace and use only letters and digits."
        ),
    )
    definition: str | None = Field(default=None, description="Definition.")
    definition_source: str | None = Field(default=None, description="Definition source URL.")
    content_component_definition: str | None = Field(default=None, description="Content component definition.")
    commonly_used: bool = Field(..., description="Whether this data type is commonly used.")
    is_deprecated: bool = Field(..., description="Whether this data type is deprecated.")
    state: Literal[
        "Deleted",
        "WIP",
        "Draft",
        "QA",
        "Candidate",
        "Production",
        "ReleaseDraft",
        "Published",
    ] = Field(..., description="Lifecycle state.")
    primitives: list[DataTypePrimitiveEntry] = Field(
        default_factory=list,
        description="Allowed primitive selections for this data type.",
    )
    supplementary_components: list[DataTypeSupplementaryComponentEntry] = Field(
        default_factory=list,
        description="Supplementary components for this data type.",
    )
    namespace: NamespaceSummaryRecord | None = Field(default=None, description="Namespace information.")
    library: LibrarySummaryRecord = Field(..., description="Library information.")
    release: ReleaseSummaryRecord = Field(..., description="Release information.")
    tags: list[TagSummaryRecord] = Field(default_factory=list, description="Data type tags.")
    log: LogSummaryRecord | None = Field(default=None, description="Revision log summary.")
    owner: UserSummary = Field(..., description="Owner information.")
    created: WhoAndWhen = Field(..., description="Creation information.")
    last_updated: WhoAndWhen = Field(..., description="Last update information.")

    model_config = ConfigDict(frozen=True)


class GetDataTypeListResponse(BaseModel):
    """Paginated response envelope for data type listings."""

    total_items: int = Field(..., ge=0, description="Total number of matching data types.")
    offset: int = Field(..., ge=0, description="Offset used for this page.")
    limit: int = Field(..., ge=1, le=100, description="Limit used for this page.")
    items: list[DataTypeEntry] = Field(..., description="Data types.")


class GetDataTypeByDataTypeManifestIdResponse(DataTypeEntry):
    """Data type detail response model."""

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "dt_manifest_id": 499,
                "dt_id": 499,
                "base_dt": {
                    "dt_manifest_id": 397,
                    "dt_id": 397,
                    "based_dt_manifest_id": 167,
                    "guid": "109055a967bd4cf19ee3320755b01f8d",
                    "den": "Amount. Type",
                    "data_type_term": "Amount",
                    "qualifier": None,
                    "representation_term": "Amount",
                    "six_digit_id": None,
                    "definition": None,
                    "definition_source": None,
                    "content_component_definition": None,
                    "is_deprecated": False,
                    "namespace": {
                        "namespace_id": 1,
                        "prefix": "",
                        "uri": "http://www.openapplications.org/oagis/10",
                    },
                    "library": {
                        "library_id": 3,
                        "name": "connectSpec",
                    },
                    "release": {
                        "release_id": 1,
                        "release_num": "10.6",
                        "state": "Published",
                    },
                },
                "guid": "4b47b06600344fdb95bbe52f664c5a20",
                "den": "Open_ Amount. Type",
                "data_type_term": "Amount",
                "qualifier": "Open",
                "representation_term": "Amount",
                "six_digit_id": None,
                "definition": None,
                "definition_source": None,
                "content_component_definition": None,
                "commonly_used": True,
                "is_deprecated": False,
                "state": "Published",
                "primitives": [
                    {
                        "cdt_pri_name": "Decimal",
                        "xbt_manifest_id": 13353,
                        "code_list_manifest_id": None,
                        "agency_id_list_manifest_id": None,
                        "is_default": True,
                    },
                    {
                        "cdt_pri_name": None,
                        "xbt_manifest_id": None,
                        "code_list_manifest_id": 44,
                        "agency_id_list_manifest_id": None,
                        "is_default": False,
                    },
                ],
                "supplementary_components": [
                    {
                        "dt_sc_manifest_id": 240,
                        "dt_sc_id": 1254,
                        "guid": "63d46f46e4fa46ec8cc269212d7cfa77",
                        "object_class_term": "Amount",
                        "property_term": "Type",
                        "representation_term": "Code",
                        "definition": None,
                        "definition_source": None,
                        "cardinality": "Optional",
                        "is_deprecated": False,
                        "primitives": [
                            {
                                "cdt_pri_name": "Decimal",
                                "xbt_manifest_id": 13353,
                                "code_list_manifest_id": None,
                                "agency_id_list_manifest_id": None,
                                "is_default": True,
                            }
                        ],
                    },
                    {
                        "dt_sc_manifest_id": 433,
                        "dt_sc_id": 1255,
                        "guid": "53c4f8ea81cb4931947ef629888417c8",
                        "object_class_term": "Amount",
                        "property_term": "Currency",
                        "representation_term": "Code",
                        "definition": None,
                        "definition_source": None,
                        "cardinality": "Optional",
                        "is_deprecated": False,
                        "primitives": [
                            {
                                "cdt_pri_name": "Decimal",
                                "xbt_manifest_id": 13353,
                                "code_list_manifest_id": None,
                                "agency_id_list_manifest_id": None,
                                "is_default": True,
                            }
                        ],
                    },
                ],
                "namespace": {
                    "namespace_id": 1,
                    "prefix": "",
                    "uri": "http://www.openapplications.org/oagis/10",
                },
                "library": {
                    "library_id": 3,
                    "name": "connectSpec",
                },
                "release": {
                    "release_id": 1,
                    "release_num": "10.6",
                    "state": "Published",
                },
                "log": {
                    "log_id": 10341,
                    "revision_num": 1,
                    "revision_tracking_num": 1,
                },
                "owner": {
                    "user_id": 1,
                    "login_id": "oagis",
                    "username": "Open Applications Group Developer",
                    "roles": ["Admin", "Developer"],
                },
                "created": {
                    "who": {
                        "user_id": 1,
                        "login_id": "oagis",
                        "username": "Open Applications Group Developer",
                        "roles": ["Admin", "Developer"],
                    },
                    "when": "2019-10-02T15:32:01.766000",
                },
                "last_updated": {
                    "who": {
                        "user_id": 1,
                        "login_id": "oagis",
                        "username": "Open Applications Group Developer",
                        "roles": ["Admin", "Developer"],
                    },
                    "when": "2019-10-02T15:32:01.766000",
                },
            }
        },
    )


class DataTypePathParams(BaseModel):
    """Path parameters for data type detail routes."""

    dt_manifest_id: int = Field(..., ge=1, description="Data type manifest ID.")


class CreateDataTypeRequest(BaseModel):
    """Request payload for creating a DT."""

    release_id: int = Field(
        ...,
        ge=1,
        description=(
            "Target release identifier. Developers can target only the `Working` release, "
            "while end-users can target only non-`Working` releases."
        ),
    )
    based_dt_manifest_id: int = Field(
        ...,
        ge=1,
        description=(
            "Base DT manifest identifier used to derive the new DT revision. "
            "If the base DT is in the same library, it must be from the target release. "
            "If it is in a different library, its release must be one of the target release dependencies."
        ),
    )
    qualifier: str | None = Field(
        default=None,
        description="Optional initial qualifier override. Use null to clear the inherited qualifier.",
    )
    six_digit_id: str | None = Field(
        default=None,
        description=(
            "Optional initial six-character suffix for the BDT Type name from the UN/CEFACT XML Schema NDR. "
            "It must be unique within the namespace and use only letters and digits. "
            "Use null to clear the inherited value."
        ),
    )
    deprecated: bool | None = Field(default=None, description="Optional initial deprecation flag override.")
    namespace_id: int | None = Field(
        default=None,
        ge=1,
        description="Optional initial namespace identifier. Use null to clear the inherited namespace.",
    )
    content_component_definition: str | None = Field(
        default=None,
        description="Optional initial content component definition override. Use null to clear the inherited value.",
    )
    definition: str | None = Field(
        default=None,
        description="Optional initial definition override. Use null to clear the inherited value.",
    )
    definition_source: str | None = Field(
        default=None,
        description="Optional initial definition source override. Use null to clear the inherited value.",
    )
    tag_id: list[int] | None = Field(
        default=None,
        description="Optional tag identifier list to attach. Use `List tags` to discover valid tag IDs.",
        examples=[[1, 2, 3]],
    )
    default_primitive: DefaultPrimitiveSelectionRequest | None = Field(
        default=None,
        description=(
            "Optional initial default primitive target. Provide exactly one of xbt_manifest_id, "
            "code_list_manifest_id, or agency_id_list_manifest_id."
        ),
    )


class CreateDataTypeResponse(BaseModel):
    """Response payload for creating a DT."""

    dt_manifest_id: int = Field(..., ge=1, description="Created DT manifest identifier.")

    model_config = ConfigDict(frozen=True)


class UpdateDataTypeRequest(BaseModel):
    """Request payload for updating mutable DT fields."""

    based_dt_manifest_id: int | None = Field(
        default=None,
        ge=1,
        description=(
            "Updated base DT manifest identifier. Use null to clear the base DT link. "
            "If the base DT is in the same library, it must be from the target release. "
            "If it is in a different library, its release must be one of the target release dependencies."
        ),
    )
    qualifier: str | None = Field(default=None, description="Updated qualifier. Use null to clear it.")
    six_digit_id: str | None = Field(
        default=None,
        description=(
            "Updated six-character suffix for the BDT Type name from the UN/CEFACT XML Schema NDR. "
            "It must be unique within the namespace and use only letters and digits. "
            "Use null to clear it."
        ),
    )
    deprecated: bool | None = Field(default=None, description="Updated deprecation flag.")
    namespace_id: int | None = Field(
        default=None,
        ge=1,
        description="Updated namespace identifier. Use null to clear the namespace.",
    )
    content_component_definition: str | None = Field(
        default=None,
        description="Updated content component definition. Use null to clear it.",
    )
    definition: str | None = Field(default=None, description="Updated definition text. Use null to clear it.")
    definition_source: str | None = Field(
        default=None,
        description="Updated definition source. Use null to clear it.",
    )
    default_primitive: DefaultPrimitiveSelectionRequest | None = Field(
        default=None,
        description=(
            "Default primitive target. Provide exactly one of xbt_manifest_id, "
            "code_list_manifest_id, or agency_id_list_manifest_id."
        ),
    )
    add_primitives: list[PrimitiveMutationRequest] | None = Field(
        default=None,
        description=(
            "Primitive rows to add. Each row uses the form {cdt_pri_name, xbt_manifest_id, "
            "code_list_manifest_id, agency_id_list_manifest_id}. Use default_primitive to choose "
            "which remaining primitive is the default."
        ),
    )
    remove_primitives: list[PrimitiveMutationRequest] | None = Field(
        default=None,
        description=(
            "Primitive rows to remove. Each row uses the form {cdt_pri_name, xbt_manifest_id, "
            "code_list_manifest_id, agency_id_list_manifest_id}."
        ),
    )

    model_config = ConfigDict(frozen=True)

class CreateDataTypeSupplementaryComponentResponse(BaseModel):
    """Response payload for creating a DT_SC."""

    dt_sc_manifest_id: int = Field(..., ge=1, description="Created DT_SC manifest identifier.")

    model_config = ConfigDict(frozen=True)


class UpdateDataTypeSupplementaryComponentRequest(BaseModel):
    """Request payload for updating mutable DT_SC fields."""

    property_term: str | None = Field(default=None, description="Updated property term. Use null to clear it.")
    representation_term: str | None = Field(
        default=None,
        description=(
            "Updated representation term. Use a CDT data type term such as `Amount`, `Code`, or `Text`. "
            "When this changes, the DT_SC primitive rows are reset to the default primitive set for that term."
        ),
    )
    cardinality: Literal["Prohibited", "Optional", "Required"] | None = Field(
        default=None,
        description=(
            "Updated DT_SC cardinality. Use `Prohibited` for `0..0`, `Optional` for `0..1`, "
            "or `Required` for `1..1`."
        ),
    )
    deprecated: bool | None = Field(default=None, description="Updated deprecation flag.")
    definition: str | None = Field(default=None, description="Updated definition text. Use null to clear it.")
    definition_source: str | None = Field(
        default=None,
        description="Updated definition source. Use null to clear it.",
    )
    value_constraint: ValueConstraintRequest | None = Field(
        default=None,
        description=(
            "Updated value constraint. Use null to clear it. "
            "Provide default_value to set a default when the element is omitted, "
            "or fixed_value to require one exact value."
        ),
    )
    default_primitive: DefaultPrimitiveSelectionRequest | None = Field(
        default=None,
        description=(
            "Default primitive target. Provide exactly one of xbt_manifest_id, "
            "code_list_manifest_id, or agency_id_list_manifest_id."
        ),
    )
    add_primitives: list[PrimitiveMutationRequest] | None = Field(
        default=None,
        description=(
            "Primitive rows to add. Each row uses the form {cdt_pri_name, xbt_manifest_id, "
            "code_list_manifest_id, agency_id_list_manifest_id}. Use default_primitive to choose "
            "which remaining primitive is the default."
        ),
    )
    remove_primitives: list[PrimitiveMutationRequest] | None = Field(
        default=None,
        description=(
            "Primitive rows to remove. Each row uses the form {cdt_pri_name, xbt_manifest_id, "
            "code_list_manifest_id, agency_id_list_manifest_id}."
        ),
    )

    @model_validator(mode="after")
    def validate_representation_term_when_present(self) -> "UpdateDataTypeSupplementaryComponentRequest":
        """Require a non-null representation term when the field is present."""
        if "representation_term" in self.model_fields_set and self.representation_term is None:
            raise ValueError("representation_term must not be null.")
        return self

    model_config = ConfigDict(frozen=True)


class UpdateDataTypeStateRequest(BaseModel):
    """Request payload for DT state transitions."""

    state: Literal["Deleted", "WIP", "Draft", "QA", "Candidate", "Production"] = Field(
        ...,
        description="Target DT lifecycle state.",
    )

    model_config = ConfigDict(frozen=True)


class TransferDataTypeOwnershipRequest(BaseModel):
    """Request payload for DT ownership transfer."""

    target_user_id: int = Field(..., ge=1, description="Target owner user ID.")

    model_config = ConfigDict(frozen=True)


class UpdateDataTypeResponse(BaseModel):
    """Response payload for DT update operations."""

    dt_manifest_id: int = Field(..., ge=1, description="Target DT manifest identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.")

    model_config = ConfigDict(frozen=True)


class TransferDataTypeOwnershipResponse(BaseModel):
    """Response payload for DT ownership transfer."""

    dt_manifest_id: int = Field(..., ge=1, description="Target DT manifest identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.")

    model_config = ConfigDict(frozen=True)


class UpdateDataTypeSupplementaryComponentResponse(BaseModel):
    """Response payload for DT_SC update operations."""

    dt_sc_manifest_id: int = Field(..., ge=1, description="Target DT_SC manifest identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.")

    model_config = ConfigDict(frozen=True)
