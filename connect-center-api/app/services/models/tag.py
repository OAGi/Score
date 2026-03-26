"""Service models for tag operations."""

from __future__ import annotations

from dataclasses import dataclass

from app.services.models import WhoAndWhen
from app.services.utils.date import DateRange
from app.services.utils.pagination import PaginationParams
from app.types.identifiers import TagId


class TagServiceParams:
    """Parameters for listing tags."""

    def __init__(
        self,
        *,
        pagination: PaginationParams,
        name: str | None = None,
        description: str | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
    ) -> None:
        self.pagination = pagination
        self.name = name
        self.description = description
        self.created_on = created_on
        self.last_updated_on = last_updated_on


@dataclass(kw_only=True)
class TagServiceResult:
    """Tag information object."""

    tag_id: TagId
    name: str
    description: str | None = None
    color: str | None = None
    text_color: str | None = None
    created: WhoAndWhen
    last_updated: WhoAndWhen
