"""Shared helpers for MCP tools hosted inside connectCenter-api."""

from __future__ import annotations

from contextlib import asynccontextmanager
from typing import Any, AsyncIterator

from fastapi import HTTPException
from fastmcp.exceptions import ToolError
from fastmcp.server.dependencies import get_access_token as get_fastmcp_access_token
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_sessionmaker
from app.repositories.vendor_plugins import get_vendor_plugin
from app.security import AuthenticatedUser, OAuth2Identity, _build_user_summary
from app.settings import settings


def tool_error_detail(detail: Any) -> str:
    """Convert an error detail payload into readable text."""
    if isinstance(detail, dict):
        message = detail.get("message")
        cause = detail.get("cause")
        if message and cause:
            return f"{message} {cause}"
        if message:
            return str(message)
        if cause:
            return str(cause)
    return str(detail)


def _to_tool_error(exc: Exception, *, fallback: str) -> ToolError:
    """Translate application exceptions into a ToolError."""
    if isinstance(exc, ToolError):
        return exc
    if isinstance(exc, HTTPException):
        return ToolError(tool_error_detail(exc.detail))
    if isinstance(exc, (LookupError, PermissionError, ValueError)):
        return ToolError(str(exc))
    return ToolError(fallback)


def str_to_bool(value: bool | str | None) -> bool | None:
    """Convert a bool-like string to ``bool`` while preserving ``None``."""
    if value is None:
        return None
    if isinstance(value, bool):
        return value
    if isinstance(value, str):
        normalized = value.lower().strip()
        if normalized in ("true", "1"):
            return True
        if normalized in ("false", "0"):
            return False
        raise ToolError(
            f"Invalid boolean string value: '{value}'. "
            "Accepted values are: 'True'/'true'/'1' for True, 'False'/'false'/'0' for False."
        )
    return value


def _tool_auth_error(*, cause: str) -> ToolError:
    """Build a FastMCP-native auth error for tool dependency resolution."""
    return ToolError(f"Bearer token authentication failed. {cause}")


async def get_tool_authenticated_user(session: AsyncSession) -> AuthenticatedUser:
    """Resolve the connectCenter authenticated user from the validated FastMCP token."""
    access_token = get_fastmcp_access_token()
    if access_token is None:
        raise _tool_auth_error(cause="The request is missing valid authentication credentials.")

    claims = access_token.claims or {}
    if _claims_match_mcp_jwt_issuer(claims):
        direct_user = await _get_tool_authenticated_user_from_login_claim(session, claims)
        if direct_user is not None:
            return direct_user

    sub = claims.get("sub")
    if not isinstance(sub, str) or not sub:
        raise _tool_auth_error(cause="Token is missing the 'sub' claim.")

    issuer_uri = _issuer_uri_from_claims(claims)
    if not issuer_uri:
        raise _tool_auth_error(cause="Token is missing the 'iss' claim and OAUTH2_ISSUER_URI is not configured.")

    app_user_repo = get_vendor_plugin().create_app_user_repository(session)
    oauth2_app = await app_user_repo.get_oauth2_app_by_issuer_uri(
        issuer_uri=issuer_uri,
    )

    if oauth2_app is None or oauth2_app.is_disabled:
        raise _tool_auth_error(cause="No OAuth2 provider is registered for the configured issuer and client_id.")

    link = await app_user_repo.get_user_auth_by_oauth2(oauth2_app_id=oauth2_app.oauth2_app_id, sub=sub)
    if link is None:
        raise _tool_auth_error(cause="This OAuth2 user is not linked to a connectCenter account.")

    if link.is_enabled is False:
        raise _tool_auth_error(cause="The linked connectCenter account is disabled.")

    user = _build_user_summary(
        user_id=int(link.app_user_id),
        login_id=link.login_id,
        name=link.name,
        is_admin=link.is_admin,
        is_developer=link.is_developer,
    )
    return AuthenticatedUser(
        user=user,
        auth_type="oauth2",
        oauth2=OAuth2Identity(
            oauth2_app_id=oauth2_app.oauth2_app_id,
            issuer_uri=issuer_uri,
            sub=sub,
        ),
    )


async def _get_tool_authenticated_user_from_login_claim(
    session: AsyncSession,
    claims: dict[str, Any],
) -> AuthenticatedUser | None:
    login_id = _login_id_from_claims(claims)
    if login_id is None:
        return None

    app_user_repo = get_vendor_plugin().create_app_user_repository(session)
    user_row = await app_user_repo.get_auth_by_login_id(login_id)

    if user_row is None:
        raise _tool_auth_error(cause="The token login claim does not match a connectCenter account.")

    is_enabled = getattr(user_row, "is_enabled", None)
    if is_enabled is False:
        raise _tool_auth_error(cause="The linked connectCenter account is disabled.")

    user = _build_user_summary(
        user_id=int(getattr(user_row, "app_user_id")),
        login_id=str(getattr(user_row, "login_id")),
        name=getattr(user_row, "name", None),
        is_admin=bool(getattr(user_row, "is_admin", False)),
        is_developer=getattr(user_row, "is_developer", None),
    )
    return AuthenticatedUser(user=user, auth_type="oauth2", oauth2=None)


def _login_id_from_claims(claims: dict[str, Any]) -> str | None:
    login_id_claim = (settings.mcp_jwt_login_id_claim or "").strip()
    if not login_id_claim:
        return None

    login_id = claims.get(login_id_claim)
    if isinstance(login_id, str) and login_id.strip():
        return login_id.strip()

    return None


def _claims_match_mcp_jwt_issuer(claims: dict[str, Any]) -> bool:
    configured_issuer = (settings.mcp_jwt_issuer_uri or "").strip().rstrip("/")
    if not configured_issuer:
        return False

    token_issuer = claims.get("iss")
    return isinstance(token_issuer, str) and token_issuer.strip().rstrip("/") == configured_issuer


def _issuer_uri_from_claims(claims: dict[str, Any]) -> str:
    issuer_uri = claims.get("iss")
    if isinstance(issuer_uri, str) and issuer_uri.strip():
        return issuer_uri.strip().rstrip("/")
    return (settings.oauth2_issuer_uri or "").strip().rstrip("/")


@asynccontextmanager
async def tool_session() -> AsyncIterator[AsyncSession]:
    """Provide a DB session for tool execution with commit/rollback handling."""
    async with get_sessionmaker()() as session:
        try:
            yield session
            await session.commit()
        except Exception:
            await session.rollback()
            raise
        finally:
            await session.close()


def current_vendor_plugin():
    """Return the active repository vendor plugin."""
    return get_vendor_plugin()
