"""Pydantic request and response models for Code List endpoints."""


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
    definition_source: str | None = Field(default=None, description="Definition source URL of the code list value.")
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


class GetCodeListValueByCodeListValueManifestIdResponse(CodeListValueEntry):
    """Response payload for retrieving one code list value by manifest ID."""

    model_config = ConfigDict(frozen=True)


class CreateCodeListRequest(BaseModel):
    """Request payload for creating a code list."""

    release_id: int = Field(
        ...,
        ge=1,
        description=(
            "Target release identifier. Developers can target only the `Working` release, "
            "while end-users can target only non-`Working` releases."
        ),
    )
    based_code_list_manifest_id: int | None = Field(
        default=None,
        ge=1,
        description="Optional base code list manifest identifier used to derive the new code list.",
    )

    model_config = ConfigDict(frozen=True)


class CreateCodeListValueRequest(BaseModel):
    """Request payload for creating one code list value."""

    value: str = Field(..., min_length=1, description="Value for the new code list entry.")
    meaning: str | None = Field(default=None, description="Meaning of the new code list value.")
    definition: str | None = Field(default=None, description="Definition of the new code list value.")
    definition_source: str | None = Field(
        default=None,
        description="Definition source URL for the new code list value.",
    )
    deprecated: bool = Field(default=False, description="Whether the new code list value is deprecated.")

    model_config = ConfigDict(frozen=True)


class UpdateCodeListValueRequest(BaseModel):
    """Request payload for updating one existing code list value by manifest ID."""

    value: str | None = Field(default=None, description="Updated code list value.")
    meaning: str | None = Field(default=None, description="Updated meaning. Use null to clear it.")
    definition: str | None = Field(default=None, description="Updated definition. Use null to clear it.")
    definition_source: str | None = Field(
        default=None,
        description="Updated definition source URL. Use null to clear it.",
    )
    deprecated: bool | None = Field(default=None, description="Updated deprecation flag.")

    model_config = ConfigDict(frozen=True)


class UpdateCodeListRequest(BaseModel):
    """Request payload for updating mutable code list fields."""

    name: str | None = Field(default=None, description="Updated name. Use null to clear it.")
    version_id: str | None = Field(default=None, description="Updated version identifier. Use null to clear it.")
    list_id: str | None = Field(default=None, description="Updated external list identifier. Use null to clear it.")
    agency_id_list_value_manifest_id: int | None = Field(
        default=None,
        ge=1,
        description="Updated agency-ID-list value manifest identifier. Use null to clear it.",
    )
    definition: str | None = Field(default=None, description="Updated definition text. Use null to clear it.")
    definition_source: str | None = Field(
        default=None,
        description="Updated definition source URL. Use null to clear it.",
    )
    remark: str | None = Field(default=None, description="Updated remark. Use null to clear it.")
    namespace_id: int | None = Field(
        default=None,
        ge=1,
        description="Updated namespace identifier. Use null to clear the namespace.",
    )
    deprecated: bool | None = Field(default=None, description="Updated deprecation flag.")
    extensible_indicator: bool | None = Field(default=None, description="Updated extensibility flag.")

    model_config = ConfigDict(frozen=True)


class UpdateCodeListStateRequest(BaseModel):
    """Request payload for code list state transitions."""

    state: Literal["Deleted", "WIP", "Draft", "QA", "Candidate", "Production"] = Field(
        ...,
        description="Target code list lifecycle state.",
    )

    model_config = ConfigDict(frozen=True)


class TransferCodeListOwnershipRequest(BaseModel):
    """Request payload for code list ownership transfer."""

    target_user_id: int = Field(..., ge=1, description="Target owner user ID.")

    model_config = ConfigDict(frozen=True)


class CreateCodeListResponse(BaseModel):
    """Response payload for creating a code list."""

    code_list_manifest_id: int = Field(..., ge=1, description="Created code list manifest identifier.")

    model_config = ConfigDict(frozen=True)


class CreateCodeListValueResponse(BaseModel):
    """Response payload for creating a code list value."""

    code_list_value_manifest_id: int = Field(..., ge=1, description="Created code list value manifest identifier.")

    model_config = ConfigDict(frozen=True)


class UpdateCodeListResponse(BaseModel):
    """Response payload for code list update operations."""

    code_list_manifest_id: int = Field(..., ge=1, description="Target code list manifest identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.")

    model_config = ConfigDict(frozen=True)


class UpdateCodeListValueResponse(BaseModel):
    """Response payload for code list value update operations."""

    code_list_value_manifest_id: int = Field(..., ge=1, description="Target code list value manifest identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.")

    model_config = ConfigDict(frozen=True)


class TransferCodeListOwnershipResponse(BaseModel):
    """Response payload for code list ownership transfer."""

    code_list_manifest_id: int = Field(..., ge=1, description="Target code list manifest identifier.")
    updates: list[str] = Field(default_factory=list, description="Updated field names.")

    model_config = ConfigDict(frozen=True)
