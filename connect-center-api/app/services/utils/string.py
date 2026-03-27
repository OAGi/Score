"""GUID type alias and generator."""

from __future__ import annotations

from typing import Annotated
from uuid import uuid4

from pydantic import Field

Guid = Annotated[
    str,
    Field(
        min_length=32,
        max_length=32,
        pattern=r"^[0-9a-fA-F]{32}$",
    ),
]


def new_guid() -> str:
    """Generate a 32-character hex GUID string."""

    return uuid4().hex
