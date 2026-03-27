"""MariaDB repository implementation for Application Users.

Provides user listing and lookup, plus authentication-related queries for
password-based and OAuth2-based authentication.
"""


from __future__ import annotations

from datetime import datetime
from typing import Any, Literal

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.contracts.app_user import AppUserRepositoryContract
from app.repositories.models.app_user import AppUserAuthRow, AppUserRow, OAuth2AppRow, OAuth2UserAuthLinkRow
from app.repositories.vendors.mariadb.models.app_user import AppUser
from app.repositories.vendors.mariadb.models.oauth2 import AppOAuth2User, OAuth2App
from app.types.identifiers import (
    AppUserId,
)


class MariaDbAppUserRepository(AppUserRepositoryContract):
    """MariaDB-backed repository for application users.

    Key features:
    - Paginated list of users with stable ordering.
    - Fetch user details by ID.
    - Fetch auth credentials by login_id for Basic auth.
    - Resolve OAuth2 provider configuration by issuer or provider name.
    - Resolve OAuth2 user links by (oauth2_app_id, sub).
    """
    def __init__(self, session: AsyncSession):
        """Initialize MariaDbAppUserRepository.

        Args:
            session: Database session bound to the current request.
        """
        self._session = session

    async def list(
        self,
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        login_id: str | None = None,
        username: str | None = None,
        organization: str | None = None,
        email: str | None = None,
        is_admin: bool | None = None,
        is_developer: bool | None = None,
        is_enabled: bool | None = None,
    ) -> tuple[int, list[AppUserRow]]:
        """Handle list.

        Args:
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            login_id: Optional login ID filter.
            username: Optional username filter.
            organization: Optional organization filter.
            email: Optional email filter.
            is_admin: Optional administrator-role filter.
            is_developer: Optional developer-role filter.
            is_enabled: Optional enabled-status filter.

        Returns:
            Result of the operation.
        """
        where_clauses = _build_where_clauses(
            login_id=login_id,
            username=username,
            organization=organization,
            email=email,
            is_admin=is_admin,
            is_developer=is_developer,
            is_enabled=is_enabled,
        )

        total_stmt = select(func.count()).select_from(AppUser)
        if where_clauses:
            total_stmt = total_stmt.where(*where_clauses)
        total_res = await self._session.execute(total_stmt)
        total = int(total_res.scalar_one())

        limit = limit
        offset = offset
        stmt = _select().order_by(*_build_order_by(sorts)).limit(limit).offset(offset)
        if where_clauses:
            stmt = stmt.where(*where_clauses)
        res = await self._session.execute(stmt)
        items = [_to_app_user_row(row) for row in res.all()]
        return total, items

    async def get(self, app_user_id: AppUserId) -> AppUserRow | None:
        """Handle get.

        Args:
            app_user_id: Application user identifier.

        Returns:
            Result of the operation.
        """
        stmt = _select().where(AppUser.app_user_id == app_user_id)
        res = await self._session.execute(stmt)
        row = res.first()
        return _to_app_user_row(row) if row else None

    async def gets(self, app_user_ids: list[AppUserId]) -> list[AppUserRow]:
        """Handle gets.

        Args:
            app_user_ids: Value for `app_user_ids`.

        Returns:
            Result of the operation.
        """
        ids = sorted({app_user_id for app_user_id in app_user_ids})
        if not ids:
            return []

        stmt = _select().where(AppUser.app_user_id.in_(ids)).order_by(AppUser.app_user_id.asc())
        res = await self._session.execute(stmt)
        return [_to_app_user_row(row) for row in res.all()]

    async def get_auth_by_login_id(self, login_id: str) -> AppUserAuthRow | None:
        """Handle get auth by login id.

        Args:
            login_id: Optional login ID filter.

        Returns:
            Result of the operation.
        """
        stmt = select(
            AppUser.app_user_id,
            AppUser.login_id,
            AppUser.name,
            AppUser.organization,
            AppUser.email,
            AppUser.email_verified,
            AppUser.email_verified_timestamp,
            AppUser.is_developer,
            AppUser.password,
            AppUser.is_admin,
            AppUser.is_enabled,
        ).where(AppUser.login_id == login_id)
        res = await self._session.execute(stmt)
        row = res.first()
        return _row_to_auth_row(row) if row else None

    async def get_oauth2_app_by_issuer_uri(self, issuer_uri: str) -> OAuth2AppRow | None:
        """Handle get oauth2 app by issuer uri.

        Args:
            issuer_uri: Value for `issuer_uri`.

        Returns:
            Result of the operation.
        """
        issuer_uri = issuer_uri.rstrip("/")
        issuer_uri_slash = issuer_uri + "/"
        stmt = select(
            OAuth2App.oauth2_app_id,
            OAuth2App.provider_name,
            OAuth2App.issuer_uri,
            OAuth2App.authorization_uri,
            OAuth2App.token_uri,
            OAuth2App.user_info_uri,
            OAuth2App.jwk_set_uri,
            OAuth2App.end_session_endpoint,
            OAuth2App.redirect_uri,
            OAuth2App.client_id,
            OAuth2App.client_secret,
            OAuth2App.client_authentication_method,
            OAuth2App.authorization_grant_type,
            OAuth2App.prompt,
            OAuth2App.is_disabled,
        ).where((OAuth2App.issuer_uri == issuer_uri) | (OAuth2App.issuer_uri == issuer_uri_slash))
        res = await self._session.execute(stmt)
        row = res.first()
        return _row_to_oauth2_app_row(row) if row else None

    async def get_oauth2_app_by_issuer_uri_and_client_id(self, *, issuer_uri: str, client_id: str) -> OAuth2AppRow | None:
        """Handle get oauth2 app by issuer uri and client id.

        Args:
            issuer_uri: Value for `issuer_uri`.
            client_id: Value for `client_id`.

        Returns:
            Result of the operation.
        """
        issuer_uri = issuer_uri.rstrip("/")
        issuer_uri_slash = issuer_uri + "/"
        stmt = (
            select(
                OAuth2App.oauth2_app_id,
                OAuth2App.provider_name,
                OAuth2App.issuer_uri,
                OAuth2App.authorization_uri,
                OAuth2App.token_uri,
                OAuth2App.user_info_uri,
                OAuth2App.jwk_set_uri,
                OAuth2App.end_session_endpoint,
                OAuth2App.redirect_uri,
                OAuth2App.client_id,
                OAuth2App.client_secret,
                OAuth2App.client_authentication_method,
                OAuth2App.authorization_grant_type,
                OAuth2App.prompt,
                OAuth2App.is_disabled,
            )
            .where((OAuth2App.issuer_uri == issuer_uri) | (OAuth2App.issuer_uri == issuer_uri_slash))
            .where(OAuth2App.client_id == client_id)
        )
        res = await self._session.execute(stmt)
        row = res.first()
        return _row_to_oauth2_app_row(row) if row else None

    async def get_oauth2_app_by_provider_name(self, provider_name: str) -> OAuth2AppRow | None:
        """Handle get oauth2 app by provider name.

        Args:
            provider_name: Value for `provider_name`.

        Returns:
            Result of the operation.
        """
        stmt = select(
            OAuth2App.oauth2_app_id,
            OAuth2App.provider_name,
            OAuth2App.issuer_uri,
            OAuth2App.authorization_uri,
            OAuth2App.token_uri,
            OAuth2App.user_info_uri,
            OAuth2App.jwk_set_uri,
            OAuth2App.end_session_endpoint,
            OAuth2App.redirect_uri,
            OAuth2App.client_id,
            OAuth2App.client_secret,
            OAuth2App.client_authentication_method,
            OAuth2App.authorization_grant_type,
            OAuth2App.prompt,
            OAuth2App.is_disabled,
        ).where(OAuth2App.provider_name == provider_name)
        res = await self._session.execute(stmt)
        row = res.first()
        return _row_to_oauth2_app_row(row) if row else None

    async def get_user_auth_by_oauth2(self, *, oauth2_app_id: int, sub: str) -> OAuth2UserAuthLinkRow | None:
        """Handle get user auth by oauth2.

        Args:
            oauth2_app_id: Value for `oauth2_app_id`.
            sub: Value for `sub`.

        Returns:
            Result of the operation.
        """
        stmt = (
            select(
                AppOAuth2User.oauth2_app_id,
                AppOAuth2User.sub,
                AppOAuth2User.app_oauth2_user_id,
                AppUser.app_user_id,
                AppUser.login_id,
                AppUser.name,
                AppUser.is_admin,
                AppUser.is_developer,
                AppUser.is_enabled,
            )
            .join(AppUser, AppUser.app_user_id == AppOAuth2User.app_user_id)
            .where(AppOAuth2User.oauth2_app_id == oauth2_app_id)
            .where(AppOAuth2User.sub == sub)
        )
        res = await self._session.execute(stmt)
        row = res.first()
        return _row_to_oauth2_user_auth_link_row(row) if row else None


def _select():
    """Internal helper for select."""
    return select(
        AppUser.app_user_id,
        AppUser.login_id,
        AppUser.name,
        AppUser.organization,
        AppUser.email,
        AppUser.email_verified,
        AppUser.email_verified_timestamp,
        AppUser.is_developer,
        AppUser.is_admin,
        AppUser.is_enabled,
    )


def _to_app_user_row(row: Any) -> AppUserRow:
    # Row is a SQLAlchemy Row tuple in the same order as select(...)
    """Internal helper for row to record.

    Args:
        row: Repository row model to convert into a DTO.

    Returns:
        Result of the operation.
    """
    (
        app_user_id,
        login_id,
        name,
        organization,
        email,
        email_verified,
        email_verified_timestamp,
        is_developer,
        is_admin,
        is_enabled,
    ) = row

    return AppUserRow(
        app_user_id=app_user_id,
        login_id=str(login_id),
        name=str(name) if name is not None else None,
        organization=str(organization) if organization is not None else None,
        email=str(email) if email is not None else None,
        email_verified=bool(email_verified),
        email_verified_timestamp=_as_dt_opt(email_verified_timestamp),
        is_developer=bool(is_developer) if is_developer is not None else None,
        is_admin=bool(is_admin) if is_admin is not None else False,
        is_enabled=bool(is_enabled) if is_enabled is not None else None,
    )


def _row_to_auth_row(row: Any) -> AppUserAuthRow:
    """Internal helper for row to auth record.

    Args:
        row: Repository row model to convert into a DTO.

    Returns:
        Result of the operation.
    """
    (
        app_user_id,
        login_id,
        name,
        organization,
        email,
        email_verified,
        email_verified_timestamp,
        is_developer,
        password,
        is_admin,
        is_enabled,
    ) = row
    return AppUserAuthRow(
        app_user_id=app_user_id,
        login_id=str(login_id),
        name=str(name) if name is not None else None,
        organization=str(organization) if organization is not None else None,
        email=str(email) if email is not None else None,
        email_verified=bool(email_verified),
        email_verified_timestamp=_as_dt_opt(email_verified_timestamp),
        is_developer=bool(is_developer) if is_developer is not None else None,
        password_hash=str(password) if password is not None else None,
        is_admin=bool(is_admin) if is_admin is not None else False,
        is_enabled=bool(is_enabled) if is_enabled is not None else None,
    )


def _as_dt_opt(value: object | None) -> datetime | None:
    """Internal helper for as dt opt.

    Args:
        value: Context value string.

    Returns:
        Result of the operation.
    """
    if value is None:
        return None
    if isinstance(value, datetime):
        return value
    raise TypeError(f"Expected datetime, got {type(value)}")


def _build_where_clauses(
    login_id: str | None,
    username: str | None,
    organization: str | None,
    email: str | None,
    is_admin: bool | None,
    is_developer: bool | None,
    is_enabled: bool | None,
) -> list[object]:
    """Internal helper for build where clauses.

    Args:
        login_id: Optional login ID filter.
        username: Optional username filter.
        organization: Optional organization filter.
        email: Optional email filter.
        is_admin: Optional administrator-role filter.
        is_developer: Optional developer-role filter.
        is_enabled: Optional enabled-status filter.

    Returns:
        Result of the operation.
    """
    clauses: list[object] = []
    if login_id:
        clauses.append(AppUser.login_id.ilike(f"%{login_id}%"))
    if username:
        clauses.append(AppUser.name.ilike(f"%{username}%"))
    if organization:
        clauses.append(AppUser.organization.ilike(f"%{organization}%"))
    if email:
        clauses.append(AppUser.email.ilike(f"%{email}%"))
    if is_admin is not None:
        clauses.append(AppUser.is_admin == is_admin)
    if is_developer is not None:
        clauses.append(AppUser.is_developer == is_developer)
    if is_enabled is not None:
        clauses.append(AppUser.is_enabled == is_enabled)
    return clauses


def _build_order_by(sorts: list[tuple[str, Literal["ASC", "DESC"]]]) -> list[object]:
    """Internal helper for build order by.

    Args:
        sorts: Value for `sorts`.

    Returns:
        Result of the operation.
    """
    if not sorts:
        return [AppUser.app_user_id.asc()]

    column_map = {
        "login_id": AppUser.login_id,
        "username": AppUser.name,
        "organization": AppUser.organization,
        "email": AppUser.email,
        "is_admin": AppUser.is_admin,
        "is_developer": AppUser.is_developer,
        "is_enabled": AppUser.is_enabled,
    }
    clauses: list[object] = []
    for s in sorts:
        col = column_map[s[0]]
        clauses.append(col.desc() if s[1] == "DESC" else col.asc())
    clauses.append(AppUser.app_user_id.asc())
    return clauses


def _row_to_oauth2_app_row(row: Any) -> OAuth2AppRow:
    """Internal helper for row to oauth2 app record.

    Args:
        row: Repository row model to convert into a DTO.

    Returns:
        Result of the operation.
    """
    (
        oauth2_app_id,
        provider_name,
        issuer_uri,
        authorization_uri,
        token_uri,
        user_info_uri,
        jwk_set_uri,
        end_session_endpoint,
        redirect_uri,
        client_id,
        client_secret,
        client_authentication_method,
        authorization_grant_type,
        prompt,
        is_disabled,
    ) = row
    return OAuth2AppRow(
        oauth2_app_id=oauth2_app_id,
        provider_name=str(provider_name),
        issuer_uri=str(issuer_uri) if issuer_uri is not None else None,
        authorization_uri=str(authorization_uri) if authorization_uri is not None else None,
        token_uri=str(token_uri) if token_uri is not None else None,
        user_info_uri=str(user_info_uri) if user_info_uri is not None else None,
        jwk_set_uri=str(jwk_set_uri) if jwk_set_uri is not None else None,
        end_session_endpoint=str(end_session_endpoint) if end_session_endpoint is not None else None,
        redirect_uri=str(redirect_uri),
        client_id=str(client_id),
        client_secret=str(client_secret),
        client_authentication_method=str(client_authentication_method),
        authorization_grant_type=str(authorization_grant_type),
        prompt=str(prompt) if prompt is not None else None,
        is_disabled=bool(is_disabled),
    )


def _row_to_oauth2_user_auth_link_row(row: Any) -> OAuth2UserAuthLinkRow:
    """Internal helper for row to oauth2 user auth link.

    Args:
        row: Repository row model to convert into a DTO.

    Returns:
        Result of the operation.
    """
    (
        oauth2_app_id,
        sub,
        app_oauth2_user_id,
        app_user_id,
        login_id,
        name,
        is_admin,
        is_developer,
        is_enabled,
    ) = row
    return OAuth2UserAuthLinkRow(
        oauth2_app_id=oauth2_app_id,
        sub=str(sub),
        app_oauth2_user_id=app_oauth2_user_id,
        app_user_id=app_user_id,
        login_id=str(login_id),
        name=str(name) if name is not None else None,
        is_admin=bool(is_admin) if is_admin is not None else False,
        is_developer=bool(is_developer) if is_developer is not None else None,
        is_enabled=bool(is_enabled) if is_enabled is not None else None,
    )
