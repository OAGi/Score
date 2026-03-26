"""Service models for namespace operations."""

from __future__ import annotations

from dataclasses import dataclass

from app.services.models import WhoAndWhen
from app.services.models.app_user import UserSummary
from app.services.models.library import LibrarySummaryServiceRecord
from app.services.utils.date import DateRange
from app.services.utils.pagination import PaginationParams
from app.types.identifiers import LibraryId
from app.types.identifiers import NamespaceId


class NamespaceServiceParams:
    """Parameters for listing namespaces."""

    def __init__(
        self,
        *,
        pagination: PaginationParams,
        library_id: LibraryId | None = None,
        uri: str | None = None,
        prefix: str | None = None,
        is_std_nmsp: bool | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
    ) -> None:
        self.pagination = pagination
        self.library_id = library_id
        self.uri = uri
        self.prefix = prefix
        self.is_std_nmsp = is_std_nmsp
        self.created_on = created_on
        self.last_updated_on = last_updated_on


@dataclass(kw_only=True)
class NamespaceServiceResult:
    """Namespace information with full details."""

    namespace_id: NamespaceId
    library: LibrarySummaryServiceRecord
    uri: str
    prefix: str | None = None
    description: str | None = None
    is_std_nmsp: bool
    owner: UserSummary
    created: WhoAndWhen
    last_updated: WhoAndWhen


@dataclass(kw_only=True)
class NamespaceSummaryServiceRecord:
    """Service-layer namespace summary."""

    namespace_id: NamespaceId
    prefix: str | None = None
    uri: str
