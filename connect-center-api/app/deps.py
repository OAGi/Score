"""FastAPI dependency providers for repositories and services.

This module wires vendor-specific repository implementations into the
service layer using dependency injection. It also scopes authenticated
user context into requester-aware services.

Key features:
- Async session injection via `get_session`.
- Vendor plugin selection via `get_vendor_plugin`.
- Requester-scoped services for write operations.
"""


from __future__ import annotations

from fastapi import Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_session
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
from app.repositories.vendor_plugins import get_vendor_plugin
from app.security import get_authenticated_user, AuthenticatedUser
from app.services.agency_id_list_service import AgencyIdListService
from app.services.app_user_service import AppUserService
from app.services.biz_ctx_service import BizCtxService
from app.services.business_information_entity_service import BusinessInformationEntityService
from app.services.code_list_service import CodeListService
from app.services.core_component_service import CoreComponentService
from app.services.ctx_category_service import ContextCategoryService
from app.services.ctx_scheme_service import CtxSchemeService
from app.services.data_type_service import DataTypeService
from app.services.library_service import LibraryService
from app.services.namespace_service import NamespaceService
from app.services.release_service import ReleaseService
from app.services.tag_service import TagService
from app.services.xbt_service import XbtService


def get_app_user_repo(
    session: AsyncSession = Depends(get_session),
) -> AppUserRepositoryContract:
    """Provide an application user repository for the active vendor.

    Args:
        session: Database session bound to the current request.

    Returns:
        Result of the operation.
    """
    return get_vendor_plugin().create_app_user_repository(session)


def get_ctx_category_repo(
    session: AsyncSession = Depends(get_session),
) -> ContextCategoryRepositoryContract:
    """Provide a context category repository for the active vendor.

    Args:
        session: Database session bound to the current request.

    Returns:
        Result of the operation.
    """
    return get_vendor_plugin().create_ctx_category_repository(session)


def get_ctx_category_service(
    context_category_repository: ContextCategoryRepositoryContract = Depends(get_ctx_category_repo),
    authenticated_user: AuthenticatedUser = Depends(get_authenticated_user),
    app_user_repository: AppUserRepositoryContract = Depends(get_app_user_repo),
) -> ContextCategoryService:
    """Provide a requester-scoped context category service.

    Args:
        context_category_repository: Value for `context_category_repository`.
        authenticated_user: Authenticated user derived from request credentials.
        app_user_repository: App-user repository dependency.

    Returns:
        Result of the operation.
    """
    return ContextCategoryService(context_category_repository, requester=authenticated_user, account_service_repo=app_user_repository)


def get_ctx_scheme_repo(
    session: AsyncSession = Depends(get_session),
) -> CtxSchemeRepositoryContract:
    """Provide a context scheme repository for the active vendor.

    Args:
        session: Database session bound to the current request.

    Returns:
        Result of the operation.
    """
    return get_vendor_plugin().create_ctx_scheme_repository(session)


def get_ctx_scheme_service(
    ctx_scheme_repository: CtxSchemeRepositoryContract = Depends(get_ctx_scheme_repo),
    authenticated_user: AuthenticatedUser = Depends(get_authenticated_user),
    app_user_repository: AppUserRepositoryContract = Depends(get_app_user_repo),
) -> CtxSchemeService:
    """Provide a requester-scoped context scheme service.

    Args:
        ctx_scheme_repository: Context-scheme repository dependency.
        authenticated_user: Authenticated user derived from request credentials.
        app_user_repository: App-user repository dependency.

    Returns:
        Result of the operation.
    """
    return CtxSchemeService(ctx_scheme_repository, requester=authenticated_user, account_service_repo=app_user_repository)


def get_biz_ctx_repo(
    session: AsyncSession = Depends(get_session),
) -> BizCtxRepositoryContract:
    """Provide a business context repository for the active vendor.

    Args:
        session: Database session bound to the current request.

    Returns:
        Result of the operation.
    """
    return get_vendor_plugin().create_biz_ctx_repository(session)


def get_biz_ctx_service(
    biz_ctx_repository: BizCtxRepositoryContract = Depends(get_biz_ctx_repo),
    authenticated_user: AuthenticatedUser = Depends(get_authenticated_user),
    app_user_repository: AppUserRepositoryContract = Depends(get_app_user_repo),
) -> BizCtxService:
    """Provide a requester-scoped business context service.

    Args:
        biz_ctx_repository: Business-context repository dependency.
        authenticated_user: Authenticated user derived from request credentials.
        app_user_repository: App-user repository dependency.

    Returns:
        Result of the operation.
    """
    return BizCtxService(biz_ctx_repository, requester=authenticated_user, account_service_repo=app_user_repository)


def get_business_information_entity_repo(
    session: AsyncSession = Depends(get_session),
) -> BusinessInformationEntityRepositoryContract:
    """Provide a business information entity repository for the active vendor.

    Args:
        session: Database session bound to the current request.
    Returns:
        Result of the operation.
    """
    core_component_repository = get_vendor_plugin().create_core_component_repository(session)
    return get_vendor_plugin().create_business_information_entity_repository(
        session,
        core_component_repository,
    )


def get_business_information_entity_service(
    business_information_entity_repository: BusinessInformationEntityRepositoryContract = Depends(get_business_information_entity_repo),
    session: AsyncSession = Depends(get_session),
    authenticated_user: AuthenticatedUser = Depends(get_authenticated_user),
    app_user_repository: AppUserRepositoryContract = Depends(get_app_user_repo),
) -> BusinessInformationEntityService:
    """Provide a business information entity service.

    Args:
        business_information_entity_repository: Value for `business_information_entity_repository`.
        authenticated_user: Authenticated user derived from request credentials.
        app_user_repository: App-user repository dependency.

    Returns:
        Result of the operation.
    """
    code_list_repository = get_vendor_plugin().create_code_list_repository(session)
    return BusinessInformationEntityService(
        business_information_entity_repository,
        requester=authenticated_user,
        account_service_repo=app_user_repository,
        code_list_repo=code_list_repository,
    )


def get_app_user_service(
    app_user_repository: AppUserRepositoryContract = Depends(get_app_user_repo),
    authenticated_user: AuthenticatedUser = Depends(get_authenticated_user),
) -> AppUserService:
    """Provide an application user service.

    Args:
        app_user_repository: App-user repository dependency.

    Returns:
        Result of the operation.
    """
    return AppUserService(app_user_repository, requester=authenticated_user)


def get_library_repo(
    session: AsyncSession = Depends(get_session),
) -> LibraryRepositoryContract:
    """Provide a library repository for the active vendor.

    Args:
        session: Database session bound to the current request.

    Returns:
        Result of the operation.
    """
    return get_vendor_plugin().create_library_repository(session)


def get_library_service(
    library_repository: LibraryRepositoryContract = Depends(get_library_repo),
    authenticated_user: AuthenticatedUser = Depends(get_authenticated_user),
    app_user_repository: AppUserRepositoryContract = Depends(get_app_user_repo),
) -> LibraryService:
    """Provide a library service.

    Args:
        library_repository: Library repository dependency.
        app_user_repository: App-user repository dependency.

    Returns:
        Result of the operation.
    """
    return LibraryService(
        library_repository,
        app_user_repository,
        requester=authenticated_user,
    )


def get_namespace_repo(
    session: AsyncSession = Depends(get_session),
) -> NamespaceRepositoryContract:
    """Provide a namespace repository for the active vendor.

    Args:
        session: Database session bound to the current request.

    Returns:
        Result of the operation.
    """
    return get_vendor_plugin().create_namespace_repository(session)


def get_namespace_service(
    namespace_repository: NamespaceRepositoryContract = Depends(get_namespace_repo),
    authenticated_user: AuthenticatedUser = Depends(get_authenticated_user),
    app_user_repository: AppUserRepositoryContract = Depends(get_app_user_repo),
) -> NamespaceService:
    """Provide a namespace service.

    Args:
        namespace_repository: Namespace repository dependency.
        app_user_repository: App-user repository dependency.

    Returns:
        Result of the operation.
    """
    return NamespaceService(
        namespace_repository,
        app_user_repository,
        requester=authenticated_user,
    )


def get_release_repo(
    session: AsyncSession = Depends(get_session),
) -> ReleaseRepositoryContract:
    """Provide a release repository for the active vendor.

    Args:
        session: Database session bound to the current request.

    Returns:
        Result of the operation.
    """
    return get_vendor_plugin().create_release_repository(session)


def get_release_service(
    release_repository: ReleaseRepositoryContract = Depends(get_release_repo),
    authenticated_user: AuthenticatedUser = Depends(get_authenticated_user),
    app_user_repository: AppUserRepositoryContract = Depends(get_app_user_repo),
) -> ReleaseService:
    """Provide a release service.

    Args:
        release_repository: Release repository dependency.
        app_user_repository: App-user repository dependency.

    Returns:
        Result of the operation.
    """
    return ReleaseService(
        release_repository,
        app_user_repository,
        requester=authenticated_user,
    )


def get_data_type_repo(
    session: AsyncSession = Depends(get_session),
) -> DataTypeRepositoryContract:
    """Provide a data type repository for the active vendor.

    Args:
        session: Database session bound to the current request.

    Returns:
        Result of the operation.
    """
    return get_vendor_plugin().create_data_type_repository(session)


def get_data_type_service(
    data_type_repository: DataTypeRepositoryContract = Depends(get_data_type_repo),
    release_repository: ReleaseRepositoryContract = Depends(get_release_repo),
    authenticated_user: AuthenticatedUser = Depends(get_authenticated_user),
    app_user_repository: AppUserRepositoryContract = Depends(get_app_user_repo),
) -> DataTypeService:
    """Provide a data type service.

    Args:
        data_type_repository: Data-type repository dependency.
        release_repository: Release repository dependency.
        app_user_repository: App-user repository dependency.

    Returns:
        Result of the operation.
    """
    return DataTypeService(
        data_type_repository,
        ReleaseService(
            release_repository,
            app_user_repository,
            requester=authenticated_user,
        ),
        app_user_repository,
        requester=authenticated_user,
    )


def get_tag_repo(
    session: AsyncSession = Depends(get_session),
) -> TagRepositoryContract:
    """Provide a tag repository for the active vendor.

    Args:
        session: Database session bound to the current request.

    Returns:
        Result of the operation.
    """
    return get_vendor_plugin().create_tag_repository(session)


def get_tag_service(
    tag_repository: TagRepositoryContract = Depends(get_tag_repo),
    authenticated_user: AuthenticatedUser = Depends(get_authenticated_user),
    app_user_repository: AppUserRepositoryContract = Depends(get_app_user_repo),
) -> TagService:
    """Provide a tag service.

    Args:
        tag_repository: Value for `tag_repository`.
        app_user_repository: App-user repository dependency.

    Returns:
        Result of the operation.
    """
    return TagService(
        tag_repository,
        app_user_repository,
        requester=authenticated_user,
    )


def get_xbt_repo(
    session: AsyncSession = Depends(get_session),
) -> XbtRepositoryContract:
    """Provide an XBT repository for the active vendor.

    Args:
        session: Database session bound to the current request.

    Returns:
        Result of the operation.
    """
    return get_vendor_plugin().create_xbt_repository(session)


def get_xbt_service(
    xbt_repository: XbtRepositoryContract = Depends(get_xbt_repo),
    authenticated_user: AuthenticatedUser = Depends(get_authenticated_user),
    app_user_repository: AppUserRepositoryContract = Depends(get_app_user_repo),
) -> XbtService:
    """Provide an XBT service.

    Args:
        xbt_repository: Value for `xbt_repository`.
        app_user_repository: App-user repository dependency.

    Returns:
        Result of the operation.
    """
    return XbtService(
        xbt_repository,
        app_user_repository,
        requester=authenticated_user,
    )


def get_code_list_repo(
    session: AsyncSession = Depends(get_session),
) -> CodeListRepositoryContract:
    """Provide a code list repository for the active vendor.

    Args:
        session: Database session bound to the current request.

    Returns:
        Result of the operation.
    """
    return get_vendor_plugin().create_code_list_repository(session)


def get_code_list_service(
    code_list_repository: CodeListRepositoryContract = Depends(get_code_list_repo),
    release_repository: ReleaseRepositoryContract = Depends(get_release_repo),
    authenticated_user: AuthenticatedUser = Depends(get_authenticated_user),
    app_user_repository: AppUserRepositoryContract = Depends(get_app_user_repo),
) -> CodeListService:
    """Provide a code list service.

    Args:
        code_list_repository: Value for `code_list_repository`.
        release_repository: Release repository dependency.
        app_user_repository: App-user repository dependency.

    Returns:
        Result of the operation.
    """
    return CodeListService(
        code_list_repository,
        ReleaseService(
            release_repository,
            app_user_repository,
            requester=authenticated_user,
        ),
        app_user_repository,
        requester=authenticated_user,
    )


def get_agency_id_list_repo(
    session: AsyncSession = Depends(get_session),
) -> AgencyIdListRepositoryContract:
    """Provide an agency ID list repository for the active vendor.

    Args:
        session: Database session bound to the current request.

    Returns:
        Result of the operation.
    """
    return get_vendor_plugin().create_agency_id_list_repository(session)


def get_agency_id_list_service(
    agency_id_list_repository: AgencyIdListRepositoryContract = Depends(get_agency_id_list_repo),
    release_repository: ReleaseRepositoryContract = Depends(get_release_repo),
    authenticated_user: AuthenticatedUser = Depends(get_authenticated_user),
    app_user_repository: AppUserRepositoryContract = Depends(get_app_user_repo),
) -> AgencyIdListService:
    """Provide an agency ID list service.

    Args:
        agency_id_list_repository: Value for `agency_id_list_repository`.
        release_repository: Release repository dependency.
        app_user_repository: App-user repository dependency.

    Returns:
        Result of the operation.
    """
    return AgencyIdListService(
        agency_id_list_repository,
        ReleaseService(
            release_repository,
            app_user_repository,
            requester=authenticated_user,
        ),
        app_user_repository,
        requester=authenticated_user,
    )


def get_core_component_repo(
    session: AsyncSession = Depends(get_session),
) -> CoreComponentRepositoryContract:
    """Provide a core component repository for the active vendor.

    Args:
        session: Database session bound to the current request.

    Returns:
        Result of the operation.
    """
    return get_vendor_plugin().create_core_component_repository(session)


def get_core_component_service(
    core_component_repository: CoreComponentRepositoryContract = Depends(get_core_component_repo),
    release_repository: ReleaseRepositoryContract = Depends(get_release_repo),
    data_type_service: DataTypeService = Depends(get_data_type_service),
    authenticated_user: AuthenticatedUser = Depends(get_authenticated_user),
    app_user_repository: AppUserRepositoryContract = Depends(get_app_user_repo),
) -> CoreComponentService:
    """Provide a core component service.

    Args:
        core_component_repository: Core-component repository dependency.
        release_repository: Release repository dependency.
        app_user_repository: App-user repository dependency.

    Returns:
        Result of the operation.
    """
    return CoreComponentService(
        core_component_repository,
        ReleaseService(
            release_repository,
            app_user_repository,
            requester=authenticated_user,
        ),
        data_type_service,
        app_user_repository,
        requester=authenticated_user,
    )
