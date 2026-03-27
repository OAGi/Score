"""Service models for library operations."""

from __future__ import annotations

from dataclasses import dataclass

from app.services.models import WhoAndWhen
from app.services.utils.date import DateRange
from app.services.utils.pagination import PaginationParams
from app.types.identifiers import LibraryId


class LibraryServiceParams:
    """Parameters for listing libraries."""

    def __init__(
        self,
        *,
        pagination: PaginationParams,
        name: str | None = None,
        type: str | None = None,
        organization: str | None = None,
        domain: str | None = None,
        state: str | None = None,
        description: str | None = None,
        is_default: bool | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
    ) -> None:
        self.pagination = pagination
        self.name = name
        self.type = type
        self.organization = organization
        self.domain = domain
        self.state = state
        self.description = description
        self.is_default = is_default
        self.created_on = created_on
        self.last_updated_on = last_updated_on


@dataclass(kw_only=True)
class LibraryServiceResult:
    """Library information with full details."""

    library_id: LibraryId
    name: str
    type: str | None = None
    organization: str | None = None
    description: str | None = None
    link: str | None = None
    domain: str | None = None
    state: str | None = None
    is_read_only: bool
    is_default: bool
    created: WhoAndWhen
    last_updated: WhoAndWhen


@dataclass(kw_only=True)
class LibrarySummaryServiceRecord:
    """Service-layer library summary."""

    library_id: LibraryId
    name: str
