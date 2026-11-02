"""Deleting context entities honours their dependencies: children go first, a scheme's category never blocks it."""

from unittest.mock import AsyncMock, MagicMock

import pytest

from app.services.biz_ctx_service import BizCtxService
from app.services.ctx_category_service import ContextCategoryService
from app.services.ctx_scheme_service import CtxSchemeService


async def test_category_with_schemes_cannot_be_deleted():
    repo = AsyncMock()
    repo.get_ctx_scheme_ids_using_category.return_value = [5, 9]

    with pytest.raises(ValueError, match="used by context schemes: 5, 9"):
        await ContextCategoryService(repo, MagicMock()).delete(1)

    repo.delete.assert_not_called()


async def test_category_without_schemes_is_deleted():
    repo = AsyncMock()
    repo.get_ctx_scheme_ids_using_category.return_value = []
    repo.delete.return_value = True

    assert await ContextCategoryService(repo, MagicMock()).delete(1) is True


async def test_business_context_assigned_to_a_bie_cannot_be_deleted():
    repo = AsyncMock()
    repo.get_top_level_asbiep_ids_using_biz_ctx.return_value = [7]

    with pytest.raises(ValueError, match="assigned to top-level ASBIEPs: 7"):
        await BizCtxService(repo, MagicMock()).delete(3)

    repo.delete.assert_not_called()


async def test_unassigned_business_context_is_deleted():
    repo = AsyncMock()
    repo.get_top_level_asbiep_ids_using_biz_ctx.return_value = []
    repo.delete.return_value = True

    assert await BizCtxService(repo, MagicMock()).delete(3) is True


async def test_scheme_in_a_category_can_be_deleted_when_no_business_context_uses_it():
    service = CtxSchemeService(AsyncMock(), MagicMock())
    scheme = MagicMock(values=[], ctx_category=MagicMock())
    service.get = AsyncMock(return_value=scheme)
    service._repo.delete.return_value = True

    assert await service.delete(5) is True
    service._repo.delete.assert_awaited_once_with(5)


async def test_scheme_whose_value_is_used_by_a_business_context_cannot_be_deleted():
    service = CtxSchemeService(AsyncMock(), MagicMock())
    scheme = MagicMock(values=[MagicMock(ctx_scheme_value_id=11)], ctx_category=MagicMock())
    service.get = AsyncMock(return_value=scheme)
    service._repo.get_biz_ctx_ids_using_ctx_scheme_value.return_value = [4]

    with pytest.raises(ValueError, match="referenced by business contexts: 4"):
        await service.delete(5)

    service._repo.delete.assert_not_called()
