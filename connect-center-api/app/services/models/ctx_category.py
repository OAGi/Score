"""Service models for context-category operations."""

from __future__ import annotations

from dataclasses import dataclass

from app.services.models import WhoAndWhen
from app.services.utils.date import DateRange
from app.services.utils.pagination import PaginationParams
from app.services.utils.string import Guid
from app.types.identifiers import ContextCategoryId


class ContextCategoryServiceParams:
    """Parameters for listing context categories."""

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
class ContextCategoryServiceResult:
    """Context category information with full details."""

    ctx_category_id: ContextCategoryId
    guid: Guid
    name: str
    description: str | None = None
    created: WhoAndWhen
    last_updated: WhoAndWhen


@dataclass(kw_only=True)
class ContextCategorySummaryServiceRecord:
    """Summary information for a context category."""

    ctx_category_id: ContextCategoryId # Unique identifier for the context category.
    name: str # Name of the context category.
