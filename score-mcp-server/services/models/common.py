"""
Shared models and data classes used across services.
"""

from dataclasses import dataclass
from datetime import datetime


@dataclass
class Sort:
    """Sort specification for ordering."""
    column: str
    direction: str  # 'asc' or 'desc'

    def __post_init__(self):
        """Validate sort direction."""
        if self.direction not in ['asc', 'desc']:
            raise ValueError(f"Invalid sort direction: {self.direction}. Must be 'asc' or 'desc'")


@dataclass
class PaginationParams:
    """Pagination parameters for database queries."""
    offset: int
    limit: int

    def __post_init__(self):
        """Validate pagination parameters."""
        if self.offset < 0:
            raise ValueError(f"Offset must be non-negative, got: {self.offset}")
        if self.limit < 1:
            raise ValueError(f"Limit must be at least 1, got: {self.limit}")
        if self.limit > 100:
            raise ValueError(f"Limit cannot exceed 100, got: {self.limit}")


@dataclass
class DateRangeParams:
    """Date range parameters for filtering database queries."""
    before: datetime | None = None
    after: datetime | None = None

    def __post_init__(self):
        """Validate date range parameters."""
        if self.before is not None and self.after is not None and self.before >= self.after:
            raise ValueError("Before date must be earlier than after date")


@dataclass
class Page:
    """Paginated response object."""
    total: int
    offset: int
    limit: int
    items: list
