"""Service models for release operations."""

from __future__ import annotations

from dataclasses import dataclass

from app.services.models import WhoAndWhen
from app.services.models.library import LibrarySummaryServiceRecord
from app.services.models.namespace import NamespaceSummaryServiceRecord
from app.services.utils.date import DateRange
from app.services.utils.pagination import PaginationParams
from app.services.utils.string import Guid
from app.types.identifiers import LibraryId
from app.types.identifiers import ReleaseId


class ReleaseServiceParams:
    """Parameters for listing releases."""

    def __init__(
        self,
        *,
        pagination: PaginationParams,
        library_id: LibraryId | None = None,
        release_num: str | None = None,
        state: str | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
    ) -> None:
        self.pagination = pagination
        self.library_id = library_id
        self.release_num = release_num
        self.state = state
        self.created_on = created_on
        self.last_updated_on = last_updated_on


@dataclass(kw_only=True)
class ReleaseServiceResult:
    """Release information with full details."""

    release_id: ReleaseId
    library: LibrarySummaryServiceRecord
    guid: Guid
    release_num: str | None = None
    release_note: str | None = None
    release_license: str | None = None
    namespace: NamespaceSummaryServiceRecord | None = None
    state: str
    created: WhoAndWhen
    last_updated: WhoAndWhen


@dataclass(kw_only=True)
class ReleaseSummaryServiceRecord:
    """Service-layer release summary."""

    release_id: ReleaseId
    release_num: str | None = None
    state: str
