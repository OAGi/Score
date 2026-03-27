"""Models for XBT MCP tools."""

from __future__ import annotations

from pydantic import BaseModel, ConfigDict

from app.services.utils.string import Guid
from app.tools.models.shared import LibrarySummaryRecord
from app.tools.models.shared import LogSummaryRecord
from app.tools.models.shared import ReleaseSummaryRecord
from app.tools.models.shared import UserSummary
from app.tools.models.shared import WhoAndWhen

class XbtSummaryResponse(BaseModel):
    """Subtype-of XBT summary payload for MCP tools."""

    xbt_manifest_id: int
    xbt_id: int
    guid: Guid
    name: str | None = None
    builtIn_type: str | None = None
    library: LibrarySummaryRecord
    release: ReleaseSummaryRecord

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetXbtResponse(BaseModel):
    """Response for get_xbt tool."""

    xbt_manifest_id: int
    xbt_id: int
    guid: Guid
    name: str | None = None
    builtIn_type: str | None = None
    jbt_draft05_map: str | None = None
    openapi30_map: str | None = None
    avro_map: str | None = None
    subtype_of_xbt: XbtSummaryResponse | None = None
    schema_definition: str | None = None
    revision_doc: str | None = None
    state: int | None = None
    is_deprecated: bool
    library: LibrarySummaryRecord
    release: ReleaseSummaryRecord
    log: LogSummaryRecord | None = None
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen

    model_config = ConfigDict(frozen=True, from_attributes=True)
