"""Models for Code List MCP tools."""

from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, ConfigDict, Field

from app.services.utils.string import Guid
from app.tools.models.shared import LibrarySummaryRecord
from app.tools.models.shared import LogSummaryRecord
from app.tools.models.shared import NamespaceSummaryRecord
from app.tools.models.shared import ReleaseSummaryRecord
from app.tools.models.shared import UserSummary
from app.tools.models.shared import WhoAndWhen


class CodeListValueResponse(BaseModel):
    """Code list value payload for MCP tools."""

    code_list_value_manifest_id: int
    code_list_value_id: int
    guid: Guid
    value: str
    meaning: str | None = None
    definition: str | None = None
    definition_source: str | None = None
    is_deprecated: bool = False

    model_config = ConfigDict(frozen=True, from_attributes=True)


class CodeListResponseEntry(BaseModel):
    """Code list payload for MCP tools."""

    code_list_manifest_id: int
    code_list_id: int
    guid: Guid
    enum_type_guid: str | None = None
    name: str
    list_id: str
    version_id: str
    definition: str | None = None
    remark: str | None = None
    definition_source: str | None = None
    extensible_indicator: bool = False
    is_deprecated: bool = False
    state: str | None = None
    values: list[CodeListValueResponse] = Field(default_factory=list)
    namespace: NamespaceSummaryRecord | None = None
    library: LibrarySummaryRecord
    release: ReleaseSummaryRecord
    log: LogSummaryRecord | None = None
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetCodeListResponse(CodeListResponseEntry):
    """Response for get_code_list tool."""


class GetCodeListPaginationResponse(BaseModel):
    """Response for get_code_lists tool."""

    total_items: int
    offset: int
    limit: int
    items: list[CodeListResponseEntry]

    model_config = ConfigDict(frozen=True)


class CreateCodeListResponse(BaseModel):
    """Response for create_code_list tool."""

    code_list_manifest_id: int

    model_config = ConfigDict(frozen=True)


class CreateCodeListValueResponse(BaseModel):
    """Response for create_code_list_value tool."""

    code_list_value_manifest_id: int

    model_config = ConfigDict(frozen=True)


class UpdateCodeListResponse(BaseModel):
    """Response for update_code_list tool."""

    code_list_manifest_id: int
    updates: list[str] = Field(default_factory=list)

    model_config = ConfigDict(frozen=True)


class UpdateCodeListValueResponse(BaseModel):
    """Response for update_code_list_value tool."""

    code_list_value_manifest_id: int
    updates: list[str] = Field(default_factory=list)

    model_config = ConfigDict(frozen=True)


class TransferCodeListOwnershipResponse(BaseModel):
    """Response for transfer_code_list_ownership tool."""

    code_list_manifest_id: int
    updates: list[str] = Field(default_factory=list)

    model_config = ConfigDict(frozen=True)


CodeListLifecycleState = Literal["Deleted", "WIP", "Draft", "QA", "Candidate", "Production"]
