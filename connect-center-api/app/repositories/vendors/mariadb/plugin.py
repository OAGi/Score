"""MariaDB vendor plugin registration.

Exposes repository factory methods for the MariaDB backend and ensures all ORM
models are imported so metadata-based schema creation includes every table.
"""


from __future__ import annotations

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
from app.repositories.contracts.log import LogRepositoryContract
from app.repositories.contracts.namespace import NamespaceRepositoryContract
from app.repositories.contracts.release import ReleaseRepositoryContract
from app.repositories.contracts.tag import TagRepositoryContract
from app.repositories.contracts.xbt import XbtRepositoryContract
from app.repositories.vendors.mariadb.agency_id_list_repository import MariaDbAgencyIdListRepository
from app.repositories.vendors.mariadb.app_user_repository import MariaDbAppUserRepository
from app.repositories.vendors.mariadb.biz_ctx_repository import MariaDbBizCtxRepository
from app.repositories.vendors.mariadb.business_information_entity_repository import \
    MariaDbBusinessInformationEntityRepository
from app.repositories.vendors.mariadb.code_list_repository import MariaDbCodeListRepository
from app.repositories.vendors.mariadb.core_component_repository import MariaDbCoreComponentRepository
from app.repositories.vendors.mariadb.ctx_category_repository import MariaDbContextCategoryRepository
from app.repositories.vendors.mariadb.ctx_scheme_repository import MariaDbCtxSchemeRepository
from app.repositories.vendors.mariadb.data_type_repository import MariaDbDataTypeRepository
from app.repositories.vendors.mariadb.library_repository import MariaDbLibraryRepository
from app.repositories.vendors.mariadb.log_repository import MariaDbLogRepository
from app.repositories.vendors.mariadb.models import agency_id_list as _agency_id_list_model  # noqa: F401
# Ensure models are imported so `Base.metadata.create_all()` sees every table.
from app.repositories.vendors.mariadb.models import app_user as _app_user_model  # noqa: F401
from app.repositories.vendors.mariadb.models import biz_ctx as _biz_ctx_model  # noqa: F401
from app.repositories.vendors.mariadb.models import \
    business_information_entity as _business_information_entity_model  # noqa: F401
from app.repositories.vendors.mariadb.models import code_list as _code_list_model  # noqa: F401
from app.repositories.vendors.mariadb.models import core_component as _core_component_model  # noqa: F401
from app.repositories.vendors.mariadb.models import ctx_category as _ctx_category_model  # noqa: F401
from app.repositories.vendors.mariadb.models import ctx_scheme as _ctx_scheme_model  # noqa: F401
from app.repositories.vendors.mariadb.models import data_type as _data_type_model  # noqa: F401
from app.repositories.vendors.mariadb.models import library as _library_model  # noqa: F401
from app.repositories.vendors.mariadb.models import log as _log_model  # noqa: F401
from app.repositories.vendors.mariadb.models import namespace as _namespace_model  # noqa: F401
from app.repositories.vendors.mariadb.models import oauth2 as _oauth2_model  # noqa: F401
from app.repositories.vendors.mariadb.models import oas as _oas_model  # noqa: F401
from app.repositories.vendors.mariadb.models import release as _release_model  # noqa: F401
from app.repositories.vendors.mariadb.models import tag as _tag_model  # noqa: F401
from app.repositories.vendors.mariadb.models import xbt as _xbt_model  # noqa: F401
from app.repositories.vendors.mariadb.models.base import Base
from app.repositories.vendors.mariadb.namespace_repository import MariaDbNamespaceRepository
from app.repositories.vendors.mariadb.release_repository import MariaDbReleaseRepository
from app.repositories.vendors.mariadb.tag_repository import MariaDbTagRepository
from app.repositories.vendors.mariadb.xbt_repository import MariaDbXbtRepository

_MODEL_IMPORTS = (
    _app_user_model,
    _biz_ctx_model,
    _ctx_category_model,
    _ctx_scheme_model,
    _library_model,
    _namespace_model,
    _oauth2_model,
    _oas_model,
    _release_model,
    _data_type_model,
    _code_list_model,
    _agency_id_list_model,
    _core_component_model,
    _business_information_entity_model,
    _tag_model,
    _xbt_model,
    _log_model,
)


class MariadbVendorPlugin:
    """MariaDB vendor plugin providing repository factories and metadata base."""
    name: str = "mariadb"
    base: type = Base
    _model_imports = _MODEL_IMPORTS

    def create_ctx_category_repository(self, session: AsyncSession) -> ContextCategoryRepositoryContract:
        """Create repository instance for create ctx category repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        return MariaDbContextCategoryRepository(session)

    def create_ctx_scheme_repository(self, session: AsyncSession) -> CtxSchemeRepositoryContract:
        """Create repository instance for create ctx scheme repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        return MariaDbCtxSchemeRepository(session)

    def create_biz_ctx_repository(self, session: AsyncSession) -> BizCtxRepositoryContract:
        """Create repository instance for create biz ctx repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        return MariaDbBizCtxRepository(session)

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
        return MariaDbBusinessInformationEntityRepository(
            session,
            core_component_repository,
        )

    def create_app_user_repository(self, session: AsyncSession) -> AppUserRepositoryContract:
        """Create repository instance for create app user repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        return MariaDbAppUserRepository(session)

    def create_library_repository(self, session: AsyncSession) -> LibraryRepositoryContract:
        """Create repository instance for create library repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        return MariaDbLibraryRepository(session)

    def create_namespace_repository(self, session: AsyncSession) -> NamespaceRepositoryContract:
        """Create repository instance for create namespace repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        return MariaDbNamespaceRepository(session)

    def create_log_repository(self, session: AsyncSession) -> LogRepositoryContract:
        """Create repository instance for create log repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        return MariaDbLogRepository(session)

    def create_release_repository(self, session: AsyncSession) -> ReleaseRepositoryContract:
        """Create repository instance for create release repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        return MariaDbReleaseRepository(session)

    def create_data_type_repository(self, session: AsyncSession) -> DataTypeRepositoryContract:
        """Create repository instance for create data type repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        return MariaDbDataTypeRepository(session, self.create_log_repository(session))

    def create_tag_repository(self, session: AsyncSession) -> TagRepositoryContract:
        """Create repository instance for create tag repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        return MariaDbTagRepository(session)

    def create_xbt_repository(self, session: AsyncSession) -> XbtRepositoryContract:
        """Create repository instance for create xbt repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        return MariaDbXbtRepository(session)

    def create_code_list_repository(self, session: AsyncSession) -> CodeListRepositoryContract:
        """Create repository instance for create code list repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        return MariaDbCodeListRepository(session, self.create_log_repository(session))

    def create_agency_id_list_repository(self, session: AsyncSession) -> AgencyIdListRepositoryContract:
        """Create repository instance for create agency id list repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        return MariaDbAgencyIdListRepository(session)

    def create_core_component_repository(self, session: AsyncSession) -> CoreComponentRepositoryContract:
        """Create repository instance for create core component repository.

        Args:
            session: Database session bound to the current request.

        Returns:
            Result of the operation.
        """
        return MariaDbCoreComponentRepository(
            session,
            self.create_log_repository(session),
        )


PLUGIN = MariadbVendorPlugin()
