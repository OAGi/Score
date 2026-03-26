"""Repository row models for application user resources."""

from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, ConfigDict


class AppUserRow(BaseModel):
    """Repository row for an application user."""

    app_user_id: int
    login_id: str
    name: str | None
    organization: str | None
    email: str | None
    email_verified: bool
    email_verified_timestamp: datetime | None
    is_developer: bool | None
    is_admin: bool
    is_enabled: bool | None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class AppUserAuthRow(AppUserRow):
    """Repository row for authentication-enabled user lookup."""

    password_hash: str | None

    model_config = ConfigDict(frozen=True, from_attributes=True)


class OAuth2AppRow(BaseModel):
    """Repository row for OAuth2 provider configuration."""

    oauth2_app_id: int
    provider_name: str
    issuer_uri: str | None = None
    authorization_uri: str | None = None
    token_uri: str | None = None
    user_info_uri: str | None = None
    jwk_set_uri: str | None = None
    end_session_endpoint: str | None = None
    redirect_uri: str
    client_id: str
    client_secret: str
    client_authentication_method: str
    authorization_grant_type: str
    prompt: str | None = None
    is_disabled: bool = False

    model_config = ConfigDict(frozen=True, from_attributes=True)


class OAuth2UserAuthLinkRow(BaseModel):
    """Repository row for OAuth2 subject to app-user mapping."""

    oauth2_app_id: int
    sub: str
    app_oauth2_user_id: int
    app_user_id: int
    login_id: str
    name: str | None
    is_admin: bool
    is_developer: bool | None
    is_enabled: bool | None

    model_config = ConfigDict(frozen=True, from_attributes=True)
