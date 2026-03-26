"""Repository row model for code list resources."""


from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field

from app.repositories.models.library import LibrarySummaryRow
from app.repositories.models.log import LogSummaryRow
from app.repositories.models.namespace import NamespaceSummaryRow
from app.repositories.models.release import ReleaseSummaryRow


class CodeListRow(BaseModel):
    """Represent CodeListRow."""
    code_list_manifest_id: int
    code_list_id: int
    guid: str
    enum_type_guid: str | None = None
    name: str | None = None
    list_id: str
    version_id: str
    definition: str | None = None
    remark: str | None = None
    definition_source: str | None = None
    extensible_indicator: bool
    is_deprecated: bool
    state: str | None = None
    values: list[CodeListValueRow] = Field(default_factory=list)
    namespace: NamespaceSummaryRow | None = None
    library: LibrarySummaryRow
    release: ReleaseSummaryRow
    log: LogSummaryRow | None = None
    owner_user_id: int
    created_by: int
    last_updated_by: int
    creation_timestamp: datetime
    last_update_timestamp: datetime

    model_config = ConfigDict(frozen=True, from_attributes=True)


class CodeListValueRow(BaseModel):
    """Repository row for code-list values."""

    code_list_value_manifest_id: int
    code_list_value_id: int
    guid: str
    value: str
    meaning: str | None = None
    definition: str | None = None
    is_deprecated: bool
    is_developer_default: bool
    is_user_default: bool

    model_config = ConfigDict(frozen=True, from_attributes=True)
