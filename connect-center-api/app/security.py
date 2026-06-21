"""Authentication and authorization helpers for connectCenter.

This module implements Bearer token (OIDC) and Basic authentication, producing
uniform `AuthenticatedUser` objects used throughout the service layer.

Key features:
- Supports Bearer (OIDC/JWT) auth when configured, with Basic as fallback.
- Standardized 401 responses with `WWW-Authenticate` headers.
- Optional debug logging for auth flows when `DEBUG=true`.
- Lazy bcrypt loading for Basic auth password verification.
"""


from __future__ import annotations

import json
import logging
import html
import secrets
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Literal
from urllib.parse import urlencode, urlparse

from fastapi import Depends, HTTPException, status
from fastapi import Request
from fastapi.security import HTTPAuthorizationCredentials, HTTPBasic, HTTPBasicCredentials, HTTPBearer
from fastmcp.server.auth.oauth_proxy.models import ProxyDCRClient
from fastmcp.server.auth.oidc_proxy import OIDCProxy
from fastmcp.server.server import FastMCP
from fastmcp.utilities.ui import create_page, create_secure_html_response
from pydantic import BaseModel, Field, ConfigDict
from starlette.responses import HTMLResponse, RedirectResponse

try:
    import bcrypt
except ModuleNotFoundError:  # pragma: no cover - environment dependent
    bcrypt = None  # type: ignore[assignment]

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


def _verify_password(password: str, password_hash: str) -> bool:
    """Verify a plaintext password against a stored bcrypt hash."""
    if bcrypt is None:
        # Lazy-fail: importing this module should not require optional password libs,
        # but actual auth checks do.
        raise RuntimeError("bcrypt is required for password verification but is not installed.")

    # bcrypt only uses the first 72 bytes of a password; stored hashes were produced
    # under that same truncation. bcrypt >=5.0 raises ValueError for longer inputs
    # instead of truncating silently, so clamp explicitly to keep verifying long
    # passwords exactly as before.
    return bcrypt.checkpw(password.encode("utf-8")[:72], password_hash.encode("utf-8"))


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
        ok = _verify_password(credentials.password, auth_row.password_hash)
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


# ---------------------------------------------------------------------------
# FastMCP Consent UI
# ---------------------------------------------------------------------------


_WORKSPACE_ROOT = Path(__file__).resolve().parents[2]
_CONNECTCENTER_LOGO_PATH = (
    _WORKSPACE_ROOT
    / "score-chrome-extensions"
    / "connectCenterNavigator"
    / "images"
    / "connectcenter-logo.svg"
)
CONNECTCENTER_CONSENT_LOGO_SVG = (
    _CONNECTCENTER_LOGO_PATH.read_text(encoding="utf-8")
    if _CONNECTCENTER_LOGO_PATH.exists()
    else ""
)


def _build_default_consent_csp(*, redirect_uri: str, csp_policy: str | None) -> str:
    """Return the consent-page CSP, matching FastMCP's default behavior."""
    if csp_policy is not None:
        return csp_policy

    parsed_redirect = urlparse(redirect_uri)
    redirect_scheme = parsed_redirect.scheme.lower()

    form_action_schemes = ["https:", "http:"]
    if redirect_scheme and redirect_scheme not in ("http", "https"):
        form_action_schemes.append(f"{redirect_scheme}:")

    form_action_directive = " ".join(form_action_schemes)
    return (
        "default-src 'none'; "
        "style-src 'unsafe-inline'; "
        "img-src https: data:; "
        "base-uri 'none'; "
        f"form-action {form_action_directive}"
    )


def create_connectcenter_consent_html(
    *,
    client_id: str,
    redirect_uri: str,
    scopes: list[str],
    txn_id: str,
    csrf_token: str,
    client_name: str | None = None,
    client_website_url: str | None = None,
    server_name: str | None = None,
    server_icon_url: str | None = None,
    server_website_url: str | None = None,
    csp_policy: str | None = None,
    is_cimd_client: bool = False,
    cimd_domain: str | None = None,
) -> str:
    """Create a connectCenter-styled consent page."""
    client_display = html.escape(client_name or client_id)
    client_id_display = html.escape(client_id)
    redirect_uri_display = html.escape(redirect_uri)
    scopes_display = ", ".join(html.escape(scope) for scope in scopes) if scopes else "None"
    client_website_display = html.escape(client_website_url or "N/A")

    server_label = html.escape(server_name or "NIST/OAGi connectCenter")
    if server_website_url:
        server_display = (
            f'<a href="{html.escape(server_website_url, quote=True)}" '
            'target="_blank" rel="noopener noreferrer" class="brand-link">'
            f"{server_label}</a>"
        )
    else:
        server_display = server_label

    request_heading = f"{client_display} is requesting access to {server_display}."
    logo_markup = ""
    if CONNECTCENTER_CONSENT_LOGO_SVG:
        logo_markup = f'<div class="brand-logo" aria-label="{server_label}">{CONNECTCENTER_CONSENT_LOGO_SVG}</div>'

    cimd_badge = ""
    if is_cimd_client and cimd_domain:
        cimd_badge = (
            '<div class="consent-badge">'
            f"Verified domain: <strong>{html.escape(cimd_domain)}</strong>"
            "</div>"
        )

    content = f"""
        <div class="auth-box">
          <div class="auth-form">
            <div class="auth-form-header">
              {logo_markup}
              <h1>{request_heading}</h1>
              <h4>Authorize a client to use your MCP session.</h4>
            </div>

            <div class="auth-form-body">
              <p class="helper-copy">
                This approval helps prevent confused deputy attacks by making the requesting
                client explicit before credentials are returned.
              </p>
              {cimd_badge}

              <div class="redirect-target">
                <div class="redirect-target-label">Credentials will be sent to</div>
                <div class="redirect-target-value">{redirect_uri_display}</div>
              </div>

              <details class="consent-details">
                <summary>Advanced Details</summary>
                <div class="consent-detail-box">
                  <div class="consent-detail-row">
                    <div class="consent-detail-label">Application Name</div>
                    <div class="consent-detail-value">{client_display}</div>
                  </div>
                  <div class="consent-detail-row">
                    <div class="consent-detail-label">Application Website</div>
                    <div class="consent-detail-value">{client_website_display}</div>
                  </div>
                  <div class="consent-detail-row">
                    <div class="consent-detail-label">Application ID</div>
                    <div class="consent-detail-value monospace">{client_id_display}</div>
                  </div>
                  <div class="consent-detail-row">
                    <div class="consent-detail-label">Redirect URI</div>
                    <div class="consent-detail-value monospace">{redirect_uri_display}</div>
                  </div>
                  <div class="consent-detail-row">
                    <div class="consent-detail-label">Requested Scopes</div>
                    <div class="consent-detail-value">{scopes_display}</div>
                  </div>
                </div>
              </details>

              <form role="form" autocomplete="off" method="POST" action="">
                <input type="hidden" name="txn_id" value="{html.escape(txn_id, quote=True)}" />
                <input type="hidden" name="csrf_token" value="{html.escape(csrf_token, quote=True)}" />
                <input type="hidden" name="submit" value="true" />
                <button type="submit" name="action" value="approve" class="btn btn-primary consent-submit-button">
                  Allow access
                </button>
                <button type="submit" name="action" value="deny" class="btn btn-secondary consent-secondary-button">
                  Deny
                </button>
              </form>
            </div>

            <div class="statement-container">
              <p>
                You will only need to approve this client once per browser session unless cookies are cleared
                or the client redirect target changes.
              </p>
            </div>
          </div>
        </div>
    """

    additional_styles = """
        body {
            margin: 0;
            background: #f6f8fa;
            color: #24292f;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
        }
        .auth-box {
            position: fixed;
            top: 50%;
            left: 50%;
            width: min(100vw - 32px, 420px);
            transform: translate(-50%, -50%);
        }
        .auth-form {
            width: 100%;
            margin: 0 auto;
        }
        .auth-form-header {
            margin: 0;
            padding: 0 0 12px 0;
            text-align: center;
            color: #333;
        }
        .auth-form-header h1 {
            margin: 0;
            font-size: 24px;
            font-weight: 300;
            line-height: 1.25;
            letter-spacing: -0.5px;
        }
        .auth-form-header h4 {
            margin: 8px 0 0 0;
            color: #57606a;
            font-size: 14px;
            font-weight: 400;
        }
        .brand-logo {
            display: flex;
            justify-content: center;
            align-items: center;
            margin: 0 auto 24px auto;
            min-height: 26px;
        }
        .brand-logo svg {
            display: block;
            width: 255px;
            max-width: 100%;
            height: auto;
        }
        .auth-form-body {
            margin-top: 16px;
            padding: 20px;
            font-size: 14px;
            background-color: #fff;
            border: 1px solid #d8dee2;
            border-radius: 5px;
            box-sizing: border-box;
        }
        .helper-copy {
            margin: 0 0 16px 0;
            color: #57606a;
            line-height: 1.5;
        }
        .brand-link {
            color: #0969da;
            text-decoration: none;
        }
        .brand-link:hover {
            text-decoration: underline;
        }
        .consent-badge {
            margin: 0 0 16px 0;
            padding: 8px 12px;
            border: 1px solid #b7ebc6;
            border-radius: 5px;
            background: #edfdf3;
            color: #1a7f37;
            text-align: center;
        }
        .redirect-target {
            margin: 0 0 16px 0;
            padding: 14px 16px;
            border: 1px solid #d8dee2;
            border-radius: 5px;
            background: #fff8c5;
        }
        .redirect-target-label {
            margin-bottom: 6px;
            color: #57606a;
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.04em;
        }
        .redirect-target-value {
            color: #24292f;
            font-size: 13px;
            font-weight: 600;
            line-height: 1.45;
            overflow-wrap: anywhere;
        }
        .consent-details {
            margin: 0 0 20px 0;
        }
        .consent-details summary {
            cursor: pointer;
            color: #24292f;
            font-weight: 600;
        }
        .consent-detail-box {
            margin-top: 12px;
            border: 1px solid #d8dee2;
            border-radius: 5px;
            background: #f6f8fa;
            overflow: hidden;
        }
        .consent-detail-row {
            display: grid;
            grid-template-columns: 132px 1fr;
            gap: 12px;
            padding: 10px 12px;
            border-top: 1px solid #d8dee2;
        }
        .consent-detail-row:first-child {
            border-top: 0;
        }
        .consent-detail-label {
            color: #57606a;
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.04em;
        }
        .consent-detail-value {
            color: #24292f;
            overflow-wrap: anywhere;
        }
        .monospace {
            font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
            font-size: 12px;
        }
        .btn {
            display: block;
            width: 100%;
            margin-top: 16px;
            padding: 10px 16px;
            border-radius: 6px;
            border: 1px solid transparent;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
        }
        .btn-primary {
            background: #2da44e;
            border-color: rgba(31, 35, 40, 0.15);
            color: #fff;
        }
        .btn-primary:hover {
            background: #2c974b;
        }
        .btn-secondary {
            background: #f6f8fa;
            border-color: #d0d7de;
            color: #24292f;
        }
        .btn-secondary:hover {
            background: #eef2f6;
        }
        .consent-secondary-button {
            margin-top: 12px;
        }
        .statement-container {
            margin-top: 16px;
            padding: 16px 18px;
            border: 1px solid #d8dee2;
            border-radius: 5px;
            background: #fff;
            color: #57606a;
            font-size: 13px;
            line-height: 1.5;
            text-align: center;
        }
        .statement-container p {
            margin: 0;
        }
        @media (max-width: 560px) {
            .auth-box {
                position: static;
                width: auto;
                transform: none;
                margin: 24px 16px;
            }
            .consent-detail-row {
                grid-template-columns: 1fr;
                gap: 6px;
            }
        }
    """

    return create_page(
        content=content,
        title="ConnectCenter Access Approval",
        additional_styles=additional_styles,
        csp_policy=_build_default_consent_csp(redirect_uri=redirect_uri, csp_policy=csp_policy),
    )


class ConnectCenterConsentMixin:
    """Override FastMCP consent rendering with connectCenter branding."""

    async def _show_consent_page(
        self,
        request: Request,
    ) -> HTMLResponse | RedirectResponse:
        """Display the connectCenter-branded consent page."""
        txn_id = request.query_params.get("txn_id")
        if not txn_id:
            return create_secure_html_response(
                "<h1>Error</h1><p>Invalid or expired transaction</p>",
                status_code=400,
            )

        txn_model = await self._transaction_store.get(key=txn_id)
        if not txn_model:
            return create_secure_html_response(
                "<h1>Error</h1><p>Invalid or expired transaction</p>",
                status_code=400,
            )

        txn = txn_model.model_dump()
        client_key = self._make_client_key(txn["client_id"], txn["client_redirect_uri"])

        approved = set(self._decode_list_cookie(request, "MCP_APPROVED_CLIENTS"))
        denied = set(self._decode_list_cookie(request, "MCP_DENIED_CLIENTS"))

        if client_key in approved:
            consent_token = secrets.token_urlsafe(32)
            txn_model.consent_token = consent_token
            await self._transaction_store.put(key=txn_id, value=txn_model, ttl=15 * 60)
            upstream_url = self._build_upstream_authorize_url(txn_id, txn)
            response = RedirectResponse(url=upstream_url, status_code=302)
            self._set_consent_binding_cookie(request, response, txn_id, consent_token)
            return response

        if client_key in denied:
            callback_params = {
                "error": "access_denied",
                "state": txn.get("client_state") or "",
            }
            sep = "&" if "?" in txn["client_redirect_uri"] else "?"
            return RedirectResponse(
                url=f"{txn['client_redirect_uri']}{sep}{urlencode(callback_params)}",
                status_code=302,
            )

        csrf_token = secrets.token_urlsafe(32)
        csrf_expires_at = time.time() + 15 * 60

        txn_model.csrf_token = csrf_token
        txn_model.csrf_expires_at = csrf_expires_at
        await self._transaction_store.put(key=txn_id, value=txn_model, ttl=15 * 60)

        client = await self.get_client(txn["client_id"])
        client_name = getattr(client, "client_name", None) if client else None
        client_website_url = (
            getattr(client, "client_uri", None)
            or getattr(client, "website_uri", None)
            or getattr(client, "homepage_uri", None)
        )

        is_cimd_client = False
        cimd_domain: str | None = None
        if isinstance(client, ProxyDCRClient) and client.cimd_document is not None:
            is_cimd_client = True
            cimd_domain = urlparse(txn["client_id"]).hostname

        fastmcp = getattr(request.app.state, "fastmcp_server", None)
        if isinstance(fastmcp, FastMCP):
            server_name = fastmcp.name
            icons = fastmcp.icons
            server_icon_url = icons[0].src if icons else None
            server_website_url = fastmcp.website_url
        else:
            server_name = None
            server_icon_url = None
            server_website_url = None

        html_content = create_connectcenter_consent_html(
            client_id=txn["client_id"],
            redirect_uri=txn["client_redirect_uri"],
            scopes=txn.get("scopes") or [],
            txn_id=txn_id,
            csrf_token=csrf_token,
            client_name=client_name,
            client_website_url=client_website_url,
            server_name=server_name,
            server_icon_url=server_icon_url,
            server_website_url=server_website_url,
            csp_policy=self._consent_csp_policy,
            is_cimd_client=is_cimd_client,
            cimd_domain=cimd_domain,
        )
        response = create_secure_html_response(html_content)
        self._set_list_cookie(
            response,
            "MCP_CONSENT_STATE",
            self._encode_list_cookie([csrf_token]),
            max_age=15 * 60,
        )
        return response


class ConnectCenterOIDCProxy(ConnectCenterConsentMixin, OIDCProxy):
    """OIDC proxy with connectCenter-branded consent UI."""

    pass
