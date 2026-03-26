"""Authentication and authorization helpers for connectCenter.

This module implements Bearer token (OIDC) and Basic authentication, producing
uniform `AuthenticatedUser` objects used throughout the service layer.

Key features:
- Supports Bearer (OIDC/JWT) auth when configured, with Basic as fallback.
- Standardized 401 responses with `WWW-Authenticate` headers.
- Optional debug logging for auth flows when `DEBUG=true`.
- Lazy password hashing dependency to avoid import-time failures.
"""


from __future__ import annotations

import json
import logging
import time
from dataclasses import dataclass
from typing import Any, Literal

from fastapi import Depends, HTTPException, status
from fastapi import Request
from fastapi.security import HTTPAuthorizationCredentials, HTTPBasic, HTTPBasicCredentials, HTTPBearer
from pydantic import BaseModel, Field, ConfigDict

try:
    from passlib.context import CryptContext
except ModuleNotFoundError:  # pragma: no cover - environment dependent
    CryptContext = None  # type: ignore[assignment]

from app.database import get_session
from app.repositories.vendor_plugins import get_vendor_plugin
from app.settings import settings
from app.services.models.app_user import Role, UserSummary


_basic = HTTPBasic(auto_error=False)
_bearer = HTTPBearer(auto_error=False)

logger = logging.getLogger("connectcenter.security")


@dataclass(frozen=True)
class OidcDiscovery:
    """Minimal OIDC discovery data used for JWT verification."""

    issuer: str
    jwks_uri: str


class OidcDiscoveryCache:
    """TTL cache for OIDC discovery documents."""

    def __init__(self, *, ttl_seconds: int = 600):
        self._ttl = int(ttl_seconds)
        self._cache: dict[str, tuple[float, OidcDiscovery]] = {}

    async def discover(self, issuer_uri: str) -> OidcDiscovery:
        issuer_uri = issuer_uri.rstrip("/")
        now = time.time()
        cached = self._cache.get(issuer_uri)
        if cached is not None:
            expires_at, value = cached
            if now < expires_at:
                return value

        try:
            import httpx  # type: ignore
        except ModuleNotFoundError as e:  # pragma: no cover
            raise RuntimeError("httpx is required for OIDC discovery but is not installed.") from e

        url = f"{issuer_uri}/.well-known/openid-configuration"
        async with httpx.AsyncClient(timeout=10.0) as client:
            resp = await client.get(url)
            resp.raise_for_status()
            data = resp.json()

        jwks_uri = data.get("jwks_uri")
        issuer = data.get("issuer") or issuer_uri
        if not isinstance(jwks_uri, str) or not jwks_uri:
            raise RuntimeError("OIDC discovery failed: jwks_uri not found.")
        if not isinstance(issuer, str) or not issuer:
            raise RuntimeError("OIDC discovery failed: issuer not found.")

        value = OidcDiscovery(issuer=issuer, jwks_uri=jwks_uri)
        self._cache[issuer_uri] = (now + self._ttl, value)
        return value


class JwtVerifier:
    """JWT verification helper backed by a cached JWKS fetcher."""

    def __init__(self, *, ttl_seconds: int = 600):
        self._ttl = int(ttl_seconds)
        self._jwks_cache: dict[str, tuple[float, dict[str, object]]] = {}

    async def verify(self, *, token: str, issuer: str, audience: str, jwks_uri: str) -> dict[str, object]:
        try:
            import jwt  # type: ignore
        except ModuleNotFoundError as e:  # pragma: no cover
            raise RuntimeError("PyJWT is required for OAuth2 JWT verification but is not installed.") from e

        header = jwt.get_unverified_header(token)
        kid = header.get("kid")
        if not isinstance(kid, str) or not kid:
            raise RuntimeError("Invalid token: missing kid header.")

        jwks = await self._get_jwks(jwks_uri)
        keys = jwks.get("keys")
        if not isinstance(keys, list):
            raise RuntimeError("JWKS is invalid: missing keys.")

        jwk = next((k for k in keys if isinstance(k, dict) and k.get("kid") == kid), None)
        if jwk is None:
            raise RuntimeError("JWKS does not contain a key matching the token kid.")

        kty = jwk.get("kty")
        if kty == "RSA":
            key_obj = jwt.algorithms.RSAAlgorithm.from_jwk(json.dumps(jwk))
        elif kty == "EC":
            key_obj = jwt.algorithms.ECAlgorithm.from_jwk(json.dumps(jwk))
        else:
            raise RuntimeError(f"Unsupported JWK key type: {kty!r}")

        options = {
            "require": ["exp", "iat", "iss", "sub"],
            "verify_aud": True,
            "verify_iss": False,
            "verify_signature": True,
        }
        claims = jwt.decode(
            token,
            key=key_obj,
            algorithms=["RS256", "RS384", "RS512", "ES256", "ES384", "ES512"],
            audience=audience,
            options=options,
        )
        if not isinstance(claims, dict):
            raise RuntimeError("Invalid token: claims are not an object.")

        token_iss = claims.get("iss")
        if not isinstance(token_iss, str) or not token_iss:
            raise RuntimeError("Invalid token: missing iss claim.")
        if token_iss.rstrip("/") != issuer.rstrip("/"):
            raise RuntimeError("Invalid token: issuer mismatch.")
        return claims  # type: ignore[return-value]

    async def _get_jwks(self, jwks_uri: str) -> dict[str, object]:
        now = time.time()
        cached = self._jwks_cache.get(jwks_uri)
        if cached is not None:
            expires_at, value = cached
            if now < expires_at:
                return value

        try:
            import httpx  # type: ignore
        except ModuleNotFoundError as e:  # pragma: no cover
            raise RuntimeError("httpx is required for JWKS fetching but is not installed.") from e

        async with httpx.AsyncClient(timeout=10.0) as client:
            resp = await client.get(jwks_uri)
            resp.raise_for_status()
            data = json.loads(resp.text)

        if not isinstance(data, dict):
            raise RuntimeError("JWKS response is not an object.")

        self._jwks_cache[jwks_uri] = (now + self._ttl, data)
        return data


discovery_cache = OidcDiscoveryCache()
jwt_verifier = JwtVerifier()


def _unauthorized(*, schemes: str = "Bearer, Basic") -> HTTPException:
    """Build a standardized 401 response with WWW-Authenticate header(s).

    Args:
        schemes: Value for `schemes`.

    Returns:
        Result of the operation.
    """
    # Include WWW-Authenticate so browsers/clients know to prompt.
    return HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail={
            "message": "Authentication is required.",
            "cause": "The request is missing valid authentication credentials.",
        },
        headers={"WWW-Authenticate": schemes},
    )


def _get_pwd_context():
    """Lazily create a password hashing context.

    Returns:
        Result of the operation.
    """
    if CryptContext is None:
        # Lazy-fail: importing this module should not require optional password libs,
        # but actual auth checks do.
        raise RuntimeError("passlib is required for password verification but is not installed.")
    return CryptContext(schemes=["bcrypt"], deprecated="auto")


class OAuth2Identity(BaseModel):
    """OAuth2 / OIDC identity information for an authenticated request."""

    oauth2_app_id: int = Field(..., ge=1, description="OAuth2 app/provider identifier.")
    issuer_uri: str = Field(..., description="OIDC issuer URI for the provider.")
    sub: str = Field(..., description="OIDC subject (sub) claim.")

    model_config = ConfigDict(frozen=True)


class AuthenticatedUser(BaseModel):
    """Requester identity for this request.

    This wraps the internal connectCenter `UserSummary` and records how the user
    was authenticated (Basic vs OAuth2).
    """

    user: UserSummary = Field(..., description="Authenticated connectCenter user.")
    auth_type: Literal["basic", "oauth2"] = Field(..., description="Authentication mechanism used for this request.")
    oauth2: OAuth2Identity | None = Field(default=None, description="OAuth2 identity info (present when auth_type='oauth2').")

    model_config = ConfigDict(frozen=True)

    @property
    def is_oauth2_user(self) -> bool:
        """Return True when the request was authenticated via OAuth2.

        Returns:
            Result of the operation.
        """
        return self.auth_type == "oauth2"


def _roles_from_flags(*, is_admin: bool, is_developer: bool | None) -> list[Role]:
    """Build the connectCenter role list from stored auth flags."""
    roles: list[Role] = []
    if is_admin:
        roles.append("Admin")
    if is_developer is True:
        roles.append("Developer")
    else:
        roles.append("End-User")
    return roles


def _build_user_summary(*, user_id: int, login_id: str, name: str | None, is_admin: bool, is_developer: bool | None) -> UserSummary:
    """Build a standardized `UserSummary` from persisted auth data."""
    username = name or login_id
    return UserSummary(
        user_id=int(user_id),
        login_id=login_id,
        username=username,
        roles=_roles_from_flags(is_admin=is_admin, is_developer=is_developer),
    )


async def authenticate_oauth2_identity(
    *,
    issuer_uri: str,
    client_id: str,
    sub: str,
    session: Any,
) -> AuthenticatedUser:
    """Resolve a linked connectCenter user from a verified OAuth2 identity."""
    normalized_issuer = issuer_uri.strip().rstrip("/")
    normalized_client_id = client_id.strip()
    normalized_provider_name = (settings.oauth2_provider_name or "").strip()

    app_user_repo = get_vendor_plugin().create_app_user_repository(session)
    oauth2_app = None
    if normalized_provider_name:
        oauth2_app = await app_user_repo.get_oauth2_app_by_provider_name(normalized_provider_name)

    if oauth2_app is None and normalized_client_id:
        oauth2_app = await app_user_repo.get_oauth2_app_by_issuer_uri_and_client_id(
            issuer_uri=normalized_issuer,
            client_id=normalized_client_id,
        )

    if oauth2_app is None:
        oauth2_app = await app_user_repo.get_oauth2_app_by_issuer_uri(normalized_issuer)

    if oauth2_app is None or oauth2_app.is_disabled:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={
                "message": "Bearer token authentication failed.",
                "cause": "No OAuth2 provider is registered for the configured issuer and client_id.",
            },
            headers={"WWW-Authenticate": "Bearer"},
        )

    link = await app_user_repo.get_user_auth_by_oauth2(oauth2_app_id=oauth2_app.oauth2_app_id, sub=sub)
    if link is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={
                "message": "Bearer token authentication failed.",
                "cause": "This OAuth2 user is not linked to a connectCenter account.",
            },
            headers={"WWW-Authenticate": "Bearer"},
        )
    if link.is_enabled is False:
        raise _unauthorized(schemes="Bearer")

    user = _build_user_summary(
        user_id=int(link.app_user_id),
        login_id=link.login_id,
        name=link.name,
        is_admin=link.is_admin,
        is_developer=link.is_developer,
    )
    oauth2_identity = OAuth2Identity(
        oauth2_app_id=oauth2_app.oauth2_app_id,
        issuer_uri=normalized_issuer,
        sub=sub,
    )
    return AuthenticatedUser(user=user, auth_type="oauth2", oauth2=oauth2_identity)


async def get_authenticated_user(
    request: Request,
    bearer: HTTPAuthorizationCredentials | None = Depends(_bearer),
    credentials: HTTPBasicCredentials | None = Depends(_basic),
    session: Any=Depends(get_session),
) -> AuthenticatedUser:
    """Resolve the authenticated user for the request.

    Args:
        request: Incoming FastAPI request object.
        bearer: Value for `bearer`.
        credentials: Value for `credentials`.
        session: Database session bound to the current request.

    Returns:
        Result of the operation.
    """
    if settings.debug:
        auth_header = request.headers.get("authorization")
        logger.debug(
            "auth.request path=%s has_authorization=%s bearer=%s basic=%s",
            request.url.path,
            auth_header is not None,
            bearer is not None,
            credentials is not None,
        )

    # Prefer Bearer when presented; Basic remains a fallback.
    if bearer is not None:
        try:
            return await _authenticate_oauth2(bearer, session=session)
        except RuntimeError as e:
            # Server-side dependency/config issue (e.g., missing httpx / PyJWT).
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail={"message": "Bearer token authentication is temporarily unavailable.", "cause": str(e)},
            )
        except HTTPException:
            if credentials is None:
                raise
        except Exception as e:
            # Token verification errors (invalid signature, aud mismatch, expired token, etc).
            if settings.debug:
                logger.debug(
                    "auth.oauth2.error path=%s error_type=%s error=%s",
                    request.url.path,
                    type(e).__name__,
                    str(e),
                )
            if credentials is None:
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail={
                        "message": "Bearer token authentication failed.",
                        "cause": f"{type(e).__name__}: {str(e)}" if settings.debug else "The token is invalid or expired.",
                    },
                    headers={"WWW-Authenticate": "Bearer"},
                )

    if credentials is None:
        # If the client sent an Authorization header but it didn't parse as Bearer or Basic,
        # make the cause explicit (common: wrong scheme name, missing space, etc).
        auth_header = request.headers.get("authorization")
        if auth_header is not None:
            if settings.debug:
                logger.debug("auth.header.unparsed path=%s authorization_prefix=%s", request.url.path, auth_header[:32])
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail={
                    "message": "Authentication is required.",
                    "cause": "The Authorization header is present but could not be parsed. Use 'Bearer <token>' or 'Basic <credentials>'.",
                },
                headers={"WWW-Authenticate": "Bearer, Basic"},
            )
        raise _unauthorized()

    return await _authenticate_basic(credentials, session=session)


async def _authenticate_basic(credentials: HTTPBasicCredentials, *, session: Any) -> AuthenticatedUser:
    """Authenticate a user using Basic credentials.

    Args:
        credentials: Value for `credentials`.
        session: Database session bound to the current request.

    Returns:
        Result of the operation.
    """
    app_user_repo = get_vendor_plugin().create_app_user_repository(session)
    auth_row = await app_user_repo.get_auth_by_login_id(credentials.username)
    if auth_row is None or auth_row.is_enabled is False or not auth_row.password_hash:
        raise _unauthorized(schemes="Basic")

    ok = False
    try:
        ok = _get_pwd_context().verify(credentials.password, auth_row.password_hash)
    except Exception:
        ok = False
    if not ok:
        raise _unauthorized(schemes="Basic")

    user = _build_user_summary(
        user_id=int(auth_row.app_user_id),
        login_id=auth_row.login_id,
        name=auth_row.name,
        is_admin=auth_row.is_admin,
        is_developer=auth_row.is_developer,
    )
    return AuthenticatedUser(user=user, auth_type="basic", oauth2=None)


async def _authenticate_oauth2(bearer: HTTPAuthorizationCredentials, *, session: Any) -> AuthenticatedUser:
    """Authenticate a user using an OIDC Bearer token.

    Args:
        bearer: Value for `bearer`.
        session: Database session bound to the current request.

    Returns:
        Result of the operation.
    """
    token = bearer.credentials
    if not token:
        raise _unauthorized(schemes="Bearer")

    issuer_uri = (settings.oauth2_issuer_uri or "").strip().rstrip("/")
    if not issuer_uri:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={
                "message": "Bearer token authentication is not configured.",
                "cause": "OAUTH2_ISSUER_URI is not set on this server.",
            },
            headers={"WWW-Authenticate": "Bearer"},
        )

    client_id = (settings.oauth2_client_id or "").strip()
    if not client_id:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={
                "message": "Bearer token authentication is not configured.",
                "cause": "OAUTH2_CLIENT_ID is not set on this server.",
            },
            headers={"WWW-Authenticate": "Bearer"},
        )

    token_audience = (settings.oauth2_audience or "").strip() or client_id

    discovery = await discovery_cache.discover(issuer_uri)
    try:
        claims = await jwt_verifier.verify(
            token=token,
            issuer=discovery.issuer,
            audience=token_audience,
            jwks_uri=discovery.jwks_uri,
        )
    except Exception as e:
        # Raise 401 for invalid/expired/mismatched tokens.
        if settings.debug:
            logger.debug("auth.oauth2.verify_failed")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={
                "message": "Bearer token authentication failed.",
                "cause": type(e).__name__ if settings.debug else "The token is invalid or expired.",
            },
            headers={"WWW-Authenticate": "Bearer"},
        )

    sub = claims.get("sub")
    if not isinstance(sub, str) or not sub:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={"message": "Bearer token authentication failed.", "cause": "Token is missing the 'sub' claim."},
            headers={"WWW-Authenticate": "Bearer"},
        )
    return await authenticate_oauth2_identity(
        issuer_uri=issuer_uri,
        client_id=client_id,
        sub=sub,
        session=session,
    )
