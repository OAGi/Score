"""Shared pagination request/response helpers."""

from __future__ import annotations

from typing import Generic, TypeVar

from dataclasses import dataclass, field


@dataclass(kw_only=True)
class Sort:
    """Sort specification with column name and direction."""

    column: str
    direction: str  # "asc" | "desc"


def parse_order_by(order_by: str | None, *, allowed: set[str]) -> list[Sort]:
    """Parse an order_by string into a list of Sort specifications."""

    if not order_by:
        return []
    sorts: list[Sort] = []
    for part in [p.strip() for p in order_by.split(",") if p.strip()]:
        direction = "asc"
        col = part
        if part[0] in {"+", "-"}:
            direction = "desc" if part[0] == "-" else "asc"
            col = part[1:]
        if col not in allowed:
            raise ValueError(f"Invalid order_by column: {col}")
        sorts.append(Sort(column=col, direction=direction))
    return sorts


@dataclass(kw_only=True)
class PaginationParams:
    """Common pagination and sorting parameters used by list endpoints."""

    limit: int
    offset: int
    sorts: list[Sort]

    @classmethod
    def from_query(
        cls,
        *,
        limit: int = 100,
        offset: int = 0,
        order_by: str | None = None,
        allowed_sort_columns: set[str],
    ) -> "PaginationParams":
        return cls(limit=limit, offset=offset, sorts=parse_order_by(order_by, allowed=allowed_sort_columns))


T = TypeVar("T")


@dataclass(kw_only=True)
class PaginationResponse(Generic[T]):
    """Generic pagination response envelope."""

    items: list[T] = field(default_factory=list)
    total: int = 0
    limit: int = 1
    offset: int = 0

    def __post_init__(self) -> None:
        if self.total < 0:
            raise ValueError("total must be greater than or equal to 0")
        if self.limit < 1:
            raise ValueError("limit must be greater than or equal to 1")
        if self.offset < 0:
            raise ValueError("offset must be greater than or equal to 0")
