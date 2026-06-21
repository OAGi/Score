"""Tests for MariaDB core component repository helpers."""

import pytest
from sqlalchemy import literal, select

from app.repositories.vendors.mariadb.core_component_repository import MariaDbCoreComponentRepository


class _EmptyResult:
    def first(self):
        return None


class _FakeSession:
    async def execute(self, _statement):
        return _EmptyResult()


@pytest.mark.asyncio
@pytest.mark.parametrize("component_type", ["ACC", "ASCCP", "BCCP"])
async def test_fetch_one_core_uses_tag_names_keyword(monkeypatch, component_type):
    """Detail fetches use the shared component select with the current tag filter keyword."""
    repo = object.__new__(MariaDbCoreComponentRepository)
    repo._session = _FakeSession()
    seen = {}

    def fake_build_component_select(self, **kwargs):
        seen.update(kwargs)
        return select(literal(1).label("manifest_id"))

    monkeypatch.setattr(
        MariaDbCoreComponentRepository,
        "_build_component_select",
        fake_build_component_select,
    )

    result = await repo._fetch_one_core(component_type, 123)

    assert result is None
    assert seen["tag_names"] is None
    assert "tag" not in seen
