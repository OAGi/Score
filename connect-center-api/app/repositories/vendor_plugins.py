"""Vendor plugin discovery for repository implementations.

This module loads database vendor plugins via entry points (or in-tree fallbacks)
and exposes a typed protocol for repository factory methods.
"""


from __future__ import annotations

from importlib import import_module
from importlib.metadata import entry_points
from pathlib import Path
from typing import Protocol, runtime_checkable

from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.contracts.agency_id_list import AgencyIdListRepositoryContract
from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.repositories.contracts.biz_ctx import BizCtxRepositoryContract
from app.repositories.contracts.business_information_entity import BusinessInformationEntityRepositoryContract
from app.repositories.contracts.code_list import CodeListRepositoryContract
from app.repositories.contracts.core_component import CoreComponentRepositoryContract
from app.repositories.contracts.ctx_category import ContextCategoryRepositoryContract
from app.repositories.contracts.ctx_scheme import CtxSchemeRepositoryContract
from app.repositories.contracts.data_type import DataTypeRepositoryContract
from app.repositories.contracts.library import LibraryRepositoryContract
from app.repositories.contracts.namespace import NamespaceRepositoryContract
from app.repositories.contracts.release import ReleaseRepositoryContract
from app.repositories.contracts.tag import TagRepositoryContract
from app.repositories.contracts.xbt import XbtRepositoryContract
from app.settings import settings

ENTRYPOINT_GROUP = "connect_center_api.vendors"


@runtime_checkable
class VendorPlugin(Protocol):
    """Protocol describing the repository factories provided by a vendor plugin."""
    name: str
    base: type

    def create_ctx_category_repository(self, session: AsyncSession) -> ContextCategoryRepositoryContract:
        """Create repository instance for create ctx category repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        pass
    def create_ctx_scheme_repository(self, session: AsyncSession) -> CtxSchemeRepositoryContract:
        """Create repository instance for create ctx scheme repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        pass
    def create_biz_ctx_repository(self, session: AsyncSession) -> BizCtxRepositoryContract:
        """Create repository instance for create biz ctx repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        pass
    def create_business_information_entity_repository(
        self,
        session: AsyncSession,
        core_component_repository: CoreComponentRepositoryContract,
    ) -> BusinessInformationEntityRepositoryContract:
        """Create repository instance for create business information entity repository.

        Args:
            session: Database session bound to the current request.
            core_component_repository: Core-component repository dependency.

        Returns:
            Result of the operation.
        """
        pass
    def create_app_user_repository(self, session: AsyncSession) -> AppUserRepositoryContract:
        """Create repository instance for create app user repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        pass
    def create_library_repository(self, session: AsyncSession) -> LibraryRepositoryContract:
        """Create repository instance for create library repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        pass
    def create_namespace_repository(self, session: AsyncSession) -> NamespaceRepositoryContract:
        """Create repository instance for create namespace repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        pass
    def create_release_repository(self, session: AsyncSession) -> ReleaseRepositoryContract:
        """Create repository instance for create release repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        pass
    def create_data_type_repository(self, session: AsyncSession) -> DataTypeRepositoryContract:
        """Create repository instance for create data type repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        pass
    def create_tag_repository(self, session: AsyncSession) -> TagRepositoryContract:
        """Create repository instance for create tag repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        pass
    def create_xbt_repository(self, session: AsyncSession) -> XbtRepositoryContract:
        """Create repository instance for create xbt repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        pass
    def create_code_list_repository(self, session: AsyncSession) -> CodeListRepositoryContract:
        """Create repository instance for create code list repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        pass
    def create_agency_id_list_repository(self, session: AsyncSession) -> AgencyIdListRepositoryContract:
        """Create repository instance for create agency id list repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        pass
    def create_core_component_repository(self, session: AsyncSession) -> CoreComponentRepositoryContract:
        """Create repository instance for create core component repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        pass


def get_vendor_plugin() -> VendorPlugin:
    """Return the active vendor plugin based on `settings.db_vendor`.

    Returns:
        Result of the operation.
    """
    vendor = settings.db_vendor.lower().strip()
    plugin = _load_vendor_plugin(vendor)
    if plugin is None:
        available = ", ".join(_discover_available_vendor_names()) or "(none found)"
        raise ValueError(f"Unsupported DB_VENDOR={settings.db_vendor!r}. Available plugins: {available}")
    return plugin


def _discover_available_vendor_names() -> list[str]:
    """Collect vendor names without importing their modules.

    Returns:
        Result of the operation.
    """
    discovered: set[str] = set()

    eps = entry_points()
    group_eps = eps.select(group=ENTRYPOINT_GROUP)  # type: ignore[attr-defined]
    for ep in group_eps:
        discovered.add(ep.name.lower())

    vendors_dir = Path(__file__).with_name("vendors")
    if vendors_dir.exists():
        for child in vendors_dir.iterdir():
            if child.is_dir() and (child / "plugin.py").exists():
                discovered.add(child.name.lower())

    return sorted(discovered)


def _load_vendor_plugin(vendor: str) -> VendorPlugin | None:
    """Load only the requested vendor plugin.

    Returns:
        Result of the operation.
    """
    eps = entry_points()
    group_eps = eps.select(group=ENTRYPOINT_GROUP)  # type: ignore[attr-defined]
    for ep in group_eps:
        if ep.name.lower() != vendor:
            continue
        loaded = ep.load()
        plugin = loaded() if callable(loaded) and not isinstance(loaded, type) else loaded
        if not isinstance(plugin, VendorPlugin):
            raise TypeError(f"Invalid vendor plugin for entry point {ep.name!r}: {plugin!r}")
        return plugin

    # Dev fallback: when running from source without installing the package,
    # entry points may not be registered. If an in-tree vendor plugin exists,
    # load it so `uvicorn app.main:app` works in a checkout.
    try:
        mod = import_module(f"app.repositories.vendors.{vendor}.plugin")
        plugin = getattr(mod, "PLUGIN")
        if not isinstance(plugin, VendorPlugin):
            raise TypeError(f"Invalid in-tree vendor plugin for {vendor!r}: {plugin!r}")
        return plugin
    except ModuleNotFoundError as exc:
        if exc.name not in {
            f"app.repositories.vendors.{vendor}",
            f"app.repositories.vendors.{vendor}.plugin",
        }:
            raise
        return None
