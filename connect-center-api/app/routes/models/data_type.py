"""Pydantic response models for Data Type endpoints."""


from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, ConfigDict, Field

from app.routes.models.shared import LibrarySummaryRecord
from app.routes.models.shared import LogSummaryRecord
from app.routes.models.shared import NamespaceSummaryRecord
from app.routes.models.shared import ReleaseSummaryRecord
from app.routes.models.shared import UserSummary
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
    cardinality_min: int = Field(..., description="Minimum cardinality.")
    cardinality_max: int | None = Field(default=None, description="Maximum cardinality.")
    is_deprecated: bool = Field(..., description="Whether this supplementary component is deprecated.")

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
    six_digit_id: str | None = Field(default=None, description="Six-digit identifier.")
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
    six_digit_id: str | None = Field(default=None, description="Six-digit identifier.")
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
    supplementary_components: list[DataTypeSupplementaryComponentEntry] = Field(
        default_factory=list,
        description="Supplementary components for this data type.",
    )
    namespace: NamespaceSummaryRecord | None = Field(default=None, description="Namespace information.")
    library: LibrarySummaryRecord = Field(..., description="Library information.")
    release: ReleaseSummaryRecord = Field(..., description="Release information.")
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
                        "cardinality_min": 0,
                        "cardinality_max": 1,
                        "is_deprecated": False,
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
                        "cardinality_min": 0,
                        "cardinality_max": 1,
                        "is_deprecated": False,
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


