"""Service models for context-scheme operations."""

from __future__ import annotations

from dataclasses import dataclass, field

from app.services.models import WhoAndWhen
from app.services.models.ctx_category import ContextCategorySummaryServiceRecord
from app.services.utils.date import DateRange
from app.services.utils.pagination import PaginationParams
from app.services.utils.string import Guid
from app.types.identifiers import CtxSchemeId, CtxSchemeValueId


class CtxSchemeServiceParams:
    """Parameters for listing context schemes."""

    def __init__(
        self,
        *,
        pagination: PaginationParams,
        scheme_id: str | None = None,
        scheme_name: str | None = None,
        description: str | None = None,
        scheme_agency_id: str | None = None,
        scheme_version_id: str | None = None,
        ctx_category_id: int | None = None,
        ctx_category_name: str | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
    ) -> None:
        self.pagination = pagination
        self.scheme_id = scheme_id
        self.scheme_name = scheme_name
        self.description = description
        self.scheme_agency_id = scheme_agency_id
        self.scheme_version_id = scheme_version_id
        self.ctx_category_id = ctx_category_id
        self.ctx_category_name = ctx_category_name
        self.created_on = created_on
        self.last_updated_on = last_updated_on


@dataclass(kw_only=True)
class CtxSchemeServiceResult:
    """Context scheme information with details and nested values."""

    ctx_scheme_id: CtxSchemeId
    guid: Guid
    scheme_id: str
    scheme_name: str
    description: str | None = None
    scheme_agency_id: str
    scheme_version_id: str
    ctx_category: ContextCategorySummaryServiceRecord | None = None
    values: list["CtxSchemeValueServiceRecord"] = field(default_factory=list)
    created: WhoAndWhen
    last_updated: WhoAndWhen


@dataclass(kw_only=True)
class CtxSchemeValueServiceRecord:
    """Context scheme value information."""

    ctx_scheme_value_id: CtxSchemeValueId
    guid: Guid
    value: str
    meaning: str | None = None


@dataclass(kw_only=True)
class CtxSchemeValueDetailServiceRecord:
    """Context scheme value detail including owner identifier."""

    ctx_scheme_value_id: CtxSchemeValueId
    guid: Guid
    owner_ctx_scheme_id: CtxSchemeId
    value: str
    meaning: str | None = None


@dataclass(kw_only=True)
class CtxSchemeValueSummaryServiceRecord:
    """Summary information for a context scheme value."""

    ctx_scheme_value_id: CtxSchemeValueId
    value: str
