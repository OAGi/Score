"""Service models for business-context operations."""

from __future__ import annotations

from dataclasses import dataclass, field

from app.services.models import WhoAndWhen
from app.services.models.ctx_scheme import CtxSchemeValueSummaryServiceRecord
from app.services.utils.date import DateRange
from app.services.utils.pagination import PaginationParams
from app.services.utils.string import Guid
from app.types.identifiers import BizCtxId, BizCtxValueId


class BizCtxServiceParams:
    """Parameters for listing business contexts."""

    def __init__(
        self,
        *,
        pagination: PaginationParams,
        name: str | None = None,
        created_on: DateRange | None = None,
        last_updated_on: DateRange | None = None,
    ) -> None:
        self.pagination = pagination
        self.name = name
        self.created_on = created_on
        self.last_updated_on = last_updated_on


@dataclass(kw_only=True)
class BizCtxServiceResult:
    """Business context information with nested values."""

    biz_ctx_id: BizCtxId
    guid: Guid
    name: str | None = None
    values: list["BizCtxValueServiceRecord"] = field(default_factory=list)
    created: WhoAndWhen
    last_updated: WhoAndWhen


@dataclass(kw_only=True)
class BizCtxValueServiceRecord:
    """Business context value linking to a context scheme value."""

    biz_ctx_value_id: BizCtxValueId
    ctx_scheme_value: CtxSchemeValueSummaryServiceRecord


@dataclass(kw_only=True)
class BizCtxValueDetailServiceRecord:
    """Business context value detail including owner and linked value IDs."""

    biz_ctx_value_id: BizCtxValueId
    biz_ctx_id: BizCtxId
    ctx_scheme_value_id: int


@dataclass(kw_only=True)
class BizCtxSummaryServiceRecord:
    """Minimal business-context summary."""

    biz_ctx_id: BizCtxId
    guid: Guid
    name: str
