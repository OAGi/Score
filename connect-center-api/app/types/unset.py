"""Shared UNSET sentinel types."""

from __future__ import annotations


class _UnsetType:
    """Sentinel used to distinguish not-provided from explicit None values."""

    def __repr__(self) -> str:  # pragma: no cover
        return "UNSET"


UNSET = _UnsetType()
UnsetType = _UnsetType

__all__ = ["UNSET", "UnsetType"]
