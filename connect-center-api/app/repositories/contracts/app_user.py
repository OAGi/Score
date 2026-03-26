"""Repository contract for Application User persistence and authentication.

Defines the async interface for listing users, retrieving user details, and
fetching authentication-related records (password and OAuth2 links).
"""


from __future__ import annotations

from typing import Protocol, Literal

from app.repositories.models.app_user import AppUserAuthRow, AppUserRow, OAuth2AppRow, OAuth2UserAuthLinkRow
from app.types.identifiers import AppUserId


class AppUserRepositoryContract(Protocol):
    """Protocol for app user repository implementations."""
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
        """Repository contract for list.

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
        pass

    async def get(self, app_user_id: AppUserId) -> AppUserRow | None:
        """Repository contract for get.

        Args:
            app_user_id: Application user identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def gets(self, app_user_ids: list[AppUserId]) -> list[AppUserRow]:
        """Repository contract for gets.

        Args:
            app_user_ids: Value for `app_user_ids`.

        Returns:
            Result of the operation.
        """
        pass

    async def get_auth_by_login_id(self, login_id: str) -> AppUserAuthRow | None:
        """Repository contract for get auth by login id.

        Args:
            login_id: Optional login ID filter.

        Returns:
            Result of the operation.
        """
        pass

    async def get_oauth2_app_by_issuer_uri(self, issuer_uri: str) -> OAuth2AppRow | None:
        """Repository contract for get oauth2 app by issuer uri.

        Args:
            issuer_uri: Value for `issuer_uri`.

        Returns:
            Result of the operation.
        """
        pass

    async def get_oauth2_app_by_issuer_uri_and_client_id(
        self,
        issuer_uri: str,
        client_id: str,
    ) -> OAuth2AppRow | None:
        """Repository contract for get oauth2 app by issuer uri and client id.

        Args:
            issuer_uri: Value for `issuer_uri`.
            client_id: Value for `client_id`.

        Returns:
            Result of the operation.
        """
        pass

    async def get_oauth2_app_by_provider_name(self, provider_name: str) -> OAuth2AppRow | None:
        """Repository contract for get oauth2 app by provider name.

        Args:
            provider_name: Value for `provider_name`.

        Returns:
            Result of the operation.
        """
        pass

    async def get_user_auth_by_oauth2(self, *, oauth2_app_id: int, sub: str) -> OAuth2UserAuthLinkRow | None:
        """Repository contract for get user auth by oauth2.

        Args:
            oauth2_app_id: Value for `oauth2_app_id`.
            sub: Value for `sub`.

        Returns:
            Result of the operation.
        """
        pass
