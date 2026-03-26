"""Pydantic response models for Code List endpoints."""


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


class CodeListValueEntry(BaseModel):
    """Code list value information."""

    code_list_value_manifest_id: int = Field(..., description="Unique identifier for the code list value manifest.")
    code_list_value_id: int = Field(..., description="Unique identifier for the code list value.")
    guid: Guid = Field(..., description="Globally unique identifier.")
    value: str = Field(..., description="Code list value.")
    meaning: str | None = Field(default=None, description="Meaning of the code list value.")
    definition: str | None = Field(default=None, description="Definition of the code list value.")
    is_deprecated: bool = Field(..., description="Whether this code list value is deprecated.")

    model_config = ConfigDict(frozen=True)


class CodeListEntry(BaseModel):
    """API representation of a code list record."""

    code_list_manifest_id: int = Field(..., description="Unique identifier for the code list manifest.")
    code_list_id: int = Field(..., description="Unique identifier for the code list.")
    guid: Guid = Field(..., description="Globally unique identifier.")
    enum_type_guid: str | None = Field(default=None, description="Enum type GUID.")
    name: str | None = Field(default=None, description="Name of the code list.")
    list_id: str = Field(..., description="List identifier.")
    version_id: str = Field(..., description="Version identifier.")
    definition: str | None = Field(default=None, description="Definition.")
    remark: str | None = Field(default=None, description="Remark.")
    definition_source: str | None = Field(default=None, description="Definition source URL.")
    extensible_indicator: bool = Field(..., description="Whether this code list is extensible.")
    is_deprecated: bool = Field(..., description="Whether this code list is deprecated.")
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
    values: list[CodeListValueEntry] = Field(default_factory=list, description="Code list values.")
    namespace: NamespaceSummaryRecord | None = Field(default=None, description="Namespace information.")
    library: LibrarySummaryRecord = Field(..., description="Library information.")
    release: ReleaseSummaryRecord = Field(..., description="Release information.")
    log: LogSummaryRecord | None = Field(default=None, description="Revision log summary.")
    owner: UserSummary = Field(..., description="Owner information.")
    created: WhoAndWhen = Field(..., description="Creation information.")
    last_updated: WhoAndWhen = Field(..., description="Last update information.")

    model_config = ConfigDict(frozen=True)


class GetCodeListListResponse(BaseModel):
    """Paginated response envelope for code list listings."""

    total_items: int = Field(..., ge=0, description="Total number of matching code lists.")
    offset: int = Field(..., ge=0, description="Offset used for this page.")
    limit: int = Field(..., ge=1, le=100, description="Limit used for this page.")
    items: list[CodeListEntry] = Field(..., description="Code lists.")


class GetCodeListByCodeListManifestIdResponse(CodeListEntry):
    """Response payload for retrieving one code list by manifest ID."""

    model_config = ConfigDict(frozen=True)

