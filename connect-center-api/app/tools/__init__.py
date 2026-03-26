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
    sub = claims.get("sub")
    issuer_uri = (settings.oauth2_issuer_uri or "").strip()

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
