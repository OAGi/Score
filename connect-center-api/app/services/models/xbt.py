"""Service models for XBT operations."""

from __future__ import annotations

from dataclasses import dataclass

from app.services.models import WhoAndWhen
from app.services.models.app_user import UserSummary
from app.services.models.library import LibrarySummaryServiceRecord
from app.services.models.log import LogSummaryServiceRecord
from app.services.models.release import ReleaseSummaryServiceRecord
from app.services.utils.string import Guid
from app.types.identifiers import XbtId, XbtManifestId


@dataclass(kw_only=True)
class XbtSummaryServiceRecord:
    """Subtype-of XBT summary used by service-layer detail payloads."""

    xbt_manifest_id: XbtManifestId
    xbt_id: XbtId
    guid: Guid
    name: str | None = None
    builtIn_type: str | None = None
    library: LibrarySummaryServiceRecord
    release: ReleaseSummaryServiceRecord


@dataclass(kw_only=True)
class XbtServiceResult:
    """Service-layer XBT detail model."""

    xbt_manifest_id: XbtManifestId
    xbt_id: XbtId
    guid: Guid
    name: str | None = None
    builtIn_type: str | None = None
    jbt_draft05_map: str | None = None
    openapi30_map: str | None = None
    avro_map: str | None = None
    subtype_of_xbt: XbtSummaryServiceRecord | None = None
    schema_definition: str | None = None
    revision_doc: str | None = None
    state: int | None = None
    is_deprecated: bool
    library: LibrarySummaryServiceRecord
    release: ReleaseSummaryServiceRecord
    log: LogSummaryServiceRecord | None = None
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen
