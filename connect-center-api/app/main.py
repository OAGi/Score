"""Application entrypoint for connectCenter.

This module constructs the FastAPI app and the FastMCP server, wires routing,
configures error handling, and sets up CORS.

Key features:
- Lifecycle management via FastAPI lifespan for optional DB initialization.
- Consistent HTTPException response formatting.
- Centralized logging configuration for app-level loggers.
- FastMCP server with all MCP tool sub-servers mounted under /mcp.
- Optional OIDC authentication proxy for the MCP server.
"""


from __future__ import annotations

import logging
import warnings
from contextlib import asynccontextmanager
from typing import Any, AsyncIterator

# FastAPI
from fastapi import FastAPI, HTTPException, Request
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware
from fastapi.openapi.utils import get_openapi
from fastapi.responses import JSONResponse, Response

# FastMCP
from fastmcp import FastMCP
from fastmcp.server.auth import MultiAuth
from fastmcp.server.auth.providers.jwt import JWTVerifier
from mcp.types import Icon
from fastmcp.utilities.lifespan import combine_lifespans

# Application
from app.database import init_models
from app.routes.agency_id_list import router as agency_id_list_router
from app.routes.app_user import router as app_user_router
from app.routes.biz_ctx import router as biz_ctx_router
from app.routes.biz_ctx import value_router as biz_ctx_value_router
from app.routes.business_information_entity import router as business_information_entity_router
from app.routes.code_list import router as code_list_router
from app.routes.code_list import value_router as code_list_value_router
from app.routes.core_component import router as core_component_router
from app.routes.ctx_category import router as ctx_category_router
from app.routes.ctx_scheme import router as ctx_scheme_router
from app.routes.ctx_scheme import value_router as ctx_scheme_value_router
from app.routes.data_type import router as data_type_router
from app.routes.health import router as health_router
from app.routes.library import router as library_router
from app.routes.namespace import router as namespace_router
from app.routes.release import router as release_router
from app.routes.tag import router as tag_router
from app.routes.xbt import router as xbt_router
from app.security import ConnectCenterOIDCProxy
from app.settings import settings

_API_ROUTE_PREFIX = "/api"
_MCP_ROUTE_PREFIX = "/mcp"
_INTERNAL_ROUTE_PREFIXES = (_MCP_ROUTE_PREFIX, f"{_API_ROUTE_PREFIX}/openapi.json", "/swagger-ui", "/redoc")
logger = logging.getLogger("connectcenter.main")


_FASTMCP_SUPPORTED_JWT_ALGORITHMS = {
    "HS256",
    "HS384",
    "HS512",
    "RS256",
    "RS384",
    "RS512",
    "ES256",
    "ES384",
    "ES512",
    "PS256",
    "PS384",
    "PS512",
}


def _configured_mcp_jwt_algorithms() -> list[str]:
    raw_value = (settings.mcp_jwt_algorithms or settings.mcp_jwt_algorithm or "ES256").strip()
    algorithms: list[str] = []
    for item in raw_value.split(","):
        algorithm = item.strip()
        if not algorithm or algorithm in algorithms:
            continue
        if algorithm not in _FASTMCP_SUPPORTED_JWT_ALGORITHMS:
            raise ValueError(
                f"Unsupported MCP JWT algorithm '{algorithm}'. "
                "The installed FastMCP JWT verifier currently supports "
                "HS256/384/512, RS256/384/512, ES256/384/512, and PS256/384/512."
            )
        algorithms.append(algorithm)
    return algorithms or ["ES256"]


# ---------------------------------------------------------------------------
# Logging
# ---------------------------------------------------------------------------


def _configure_logging() -> None:
    """Configure application logging."""
    root = logging.getLogger()
    # Avoid double-configuring if uvicorn already set handlers.
    if root.handlers:
        if settings.debug:
            root.setLevel(logging.DEBUG)
        return

    level = logging.DEBUG if settings.debug else logging.INFO
    logging.basicConfig(
        level=level,
        format="%(asctime)s %(levelname)s %(name)s: %(message)s",
    )

    # Common uvicorn-related loggers to keep consistent with root level.
    if settings.debug:
        logging.getLogger("uvicorn.error").setLevel(logging.DEBUG)
        logging.getLogger("uvicorn.access").setLevel(logging.INFO)


_configure_logging()


# ---------------------------------------------------------------------------
# FastAPI
# ---------------------------------------------------------------------------


@asynccontextmanager
async def lifespan(_: FastAPI) -> AsyncIterator[None]:
    """Application lifespan hook.

    Yields:
        Result of the operation.
    """
    if settings.db_auto_create:
        await init_models()
    yield


def _serialize_routes(
    api_app: FastAPI,
    *,
    include_internal: bool,
) -> list[dict[str, Any]]:
    """Convert FastAPI routes into a lightweight JSON-serializable shape."""
    routes: list[dict[str, Any]] = []

    for route in api_app.router.routes:
        path = getattr(route, "path", None)
        methods = sorted(set(getattr(route, "methods", set()) or set()) - {"HEAD", "OPTIONS"})

        if not path or not methods:
            continue
        if not include_internal and path.startswith(_INTERNAL_ROUTE_PREFIXES):
            continue

        routes.append(
            {
                "path": path,
                "methods": methods,
                "name": getattr(route, "name", path),
                "summary": getattr(route, "summary", None),
                "tags": list(getattr(route, "tags", []) or []),
            }
        )

    return routes


def _schema_path_without_api_prefix(path: str) -> str:
    """Expose API schema paths relative to the documented `/api` server base."""
    if path == _API_ROUTE_PREFIX:
        return "/"
    if path.startswith(f"{_API_ROUTE_PREFIX}/"):
        return path[len(_API_ROUTE_PREFIX) :]
    return path


app = FastAPI(
    title=settings.app_name,
    lifespan=lifespan,
    openapi_url=f"{_API_ROUTE_PREFIX}/openapi.json",
    docs_url="/swagger-ui",
)


def custom_openapi() -> dict[str, Any]:
    """Generate an OpenAPI schema whose paths are relative to the `/api` base URL."""
    if app.openapi_schema:
        return app.openapi_schema

    schema = get_openapi(
        title=app.title,
        version=app.version,
        openapi_version=app.openapi_version,
        summary=app.summary,
        description=app.description,
        routes=app.routes,
        tags=app.openapi_tags,
        servers=[{"url": _API_ROUTE_PREFIX}],
    )
    schema["paths"] = {
        _schema_path_without_api_prefix(path): operations
        for path, operations in schema.get("paths", {}).items()
    }
    app.openapi_schema = schema
    return schema


app.openapi = custom_openapi


@app.middleware("http")
async def normalize_standalone_mcp_get(request: Request, call_next):
    """Tell streamable MCP clients that standalone GET streams are unsupported."""
    if request.method == "GET" and request.url.path in {_MCP_ROUTE_PREFIX, f"{_MCP_ROUTE_PREFIX}/"}:
        if "mcp-session-id" not in {name.lower() for name in request.headers.keys()}:
            return Response(status_code=405)
    return await call_next(request)


@app.exception_handler(HTTPException)
async def http_exception_handler(_: Request, exc: HTTPException) -> JSONResponse:
    """Standardize error responses for explicit HTTPException raises.

    Args:
        exc: Exception instance raised by request handling.

    Returns:
        Result of the operation.
    """
    detail = exc.detail
    message: str | None = None
    cause: str | None = None
    extra_content: dict[str, Any] = {}

    if isinstance(detail, dict):
        raw_message = detail.get("message") or detail.get("detail")
        if isinstance(raw_message, str):
            message = raw_message
        raw_cause = detail.get("cause")
        if isinstance(raw_cause, str):
            cause = raw_cause
        extra_content = {
            str(key): value
            for key, value in detail.items()
            if key not in {"message", "detail", "cause"}
        }
    elif isinstance(detail, str):
        message = detail

    # Replace generic defaults with user-friendly sentences.
    if not message or message.strip().lower() in {"not found", "unauthorized", "forbidden", "bad request"}:
        default_by_status = {
            400: "The request is invalid. Check the request and try again.",
            401: "Authentication is required.",
            403: "You do not have permission to perform this action.",
            404: "The requested resource was not found.",
            500: "The server encountered an internal error.",
        }
        message = default_by_status.get(int(exc.status_code), "An error occurred.")

    content: dict[str, Any] = {"message": message}

    # Only include causes for 5xx when explicitly enabled (avoid leaking internals).
    if cause and (exc.status_code < 500 or settings.debug is True):
        content["cause"] = cause
    content.update(extra_content)

    headers = exc.headers or None
    return JSONResponse(status_code=exc.status_code, content=content, headers=headers)


@app.exception_handler(RequestValidationError)
async def request_validation_exception_handler(_: Request, exc: RequestValidationError) -> JSONResponse:
    """Standardize request validation errors with user-friendly sentences.

    Args:
        exc: Exception instance raised by request handling.

    Returns:
        Result of the operation.
    """

    def _friendly_cause(error: dict) -> str:
        """Handle  friendly cause.

        Args:
            error: Validation error object to convert into a client-friendly message.

        Returns:
            Result of the operation.
        """
        error_type = str(error.get("type", ""))
        loc = error.get("loc", [])
        msg = str(error.get("msg", "Invalid value."))

        if isinstance(loc, (list, tuple)) and len(loc) >= 2:
            scope = str(loc[0])
            field = str(loc[1])
            if error_type == "missing":
                if scope == "query":
                    return f"Missing required query parameter '{field}'."
                if scope == "path":
                    return f"Missing required path parameter '{field}'."
                if scope == "header":
                    return f"Missing required header '{field}'."
                if scope == "body":
                    return f"Missing required request body field '{field}'."
                return f"Missing required field '{field}'."
            if scope == "query":
                return f"Invalid value for query parameter '{field}': {msg}"
            if scope == "path":
                return f"Invalid value for path parameter '{field}': {msg}"
            if scope == "header":
                return f"Invalid value for header '{field}': {msg}"
            if scope == "body":
                return f"Invalid value for request body field '{field}': {msg}"

        return msg

    errors = exc.errors()
    message = "The request is invalid. Check the parameters and try again."
    cause = _friendly_cause(errors[0]) if errors else "The request payload or parameters are invalid."
    if len(errors) > 1:
        cause = f"{cause} ({len(errors)} validation issues detected.)"

    return JSONResponse(
        status_code=422,
        content={
            "message": message,
            "cause": cause,
        },
    )

# Allow the docs dev server (and other configured origins) to read OpenAPI and call APIs.
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_allow_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
for router in (
    health_router,
    ctx_category_router,
    ctx_scheme_router,
    ctx_scheme_value_router,
    biz_ctx_router,
    biz_ctx_value_router,
    app_user_router,
    library_router,
    namespace_router,
    release_router,
    data_type_router,
    tag_router,
    xbt_router,
    code_list_router,
    code_list_value_router,
    agency_id_list_router,
    core_component_router,
    business_information_entity_router,
):
    app.include_router(router, prefix=_API_ROUTE_PREFIX)

def _create_mcp_server(api_app: FastAPI) -> tuple[FastMCP, MultiAuth | ConnectCenterOIDCProxy | JWTVerifier | None]:
    """Build the FastMCP server exposed by the API process."""
    from app.tools.agency_id_list import mcp as agency_id_list_mcp
    from app.tools.app_user import mcp as app_user_mcp
    from app.tools.biz_ctx import mcp as biz_ctx_mcp
    from app.tools.business_information_entity import mcp as business_information_entity_mcp
    from app.tools.code_list import mcp as code_list_mcp
    from app.tools.core_component import mcp as core_component_mcp
    from app.tools.ctx_category import mcp as ctx_category_mcp
    from app.tools.ctx_scheme import mcp as ctx_scheme_mcp
    from app.tools.data_type import mcp as data_type_mcp
    from app.tools.library import mcp as library_mcp
    from app.tools.namespace import mcp as namespace_mcp
    from app.tools.release import mcp as release_mcp
    from app.tools.tag import mcp as tag_mcp
    from app.tools.xbt import mcp as xbt_mcp

    mcp_auth = _create_mcp_auth()

    docs_base_url = settings.public_docs_base_url.rstrip("/")
    website_url = docs_base_url or None
    icon_src = f"{docs_base_url}/connectcenter-developers.svg" if docs_base_url else None
    icons = [Icon(src=icon_src, mimeType="image/svg+xml")] if icon_src else None

    mcp = FastMCP(
        "connectCenter MCP",
        auth=mcp_auth,
        website_url=website_url,
        icons=icons,
    )

    mcp.mount(agency_id_list_mcp)
    mcp.mount(app_user_mcp)
    mcp.mount(biz_ctx_mcp)
    mcp.mount(business_information_entity_mcp)
    mcp.mount(code_list_mcp)
    mcp.mount(core_component_mcp)
    mcp.mount(ctx_category_mcp)
    mcp.mount(ctx_scheme_mcp)
    mcp.mount(data_type_mcp)
    mcp.mount(library_mcp)
    mcp.mount(namespace_mcp)
    mcp.mount(release_mcp)
    mcp.mount(tag_mcp)
    mcp.mount(xbt_mcp)
    return mcp, mcp_auth


# ---------------------------------------------------------------------------
# FastMCP
# ---------------------------------------------------------------------------


def _create_mcp_auth() -> MultiAuth | ConnectCenterOIDCProxy | JWTVerifier | None:
    """Build the FastMCP auth configuration for interactive and external JWT access."""
    auth_server: ConnectCenterOIDCProxy | None = None
    token_verifiers: list[JWTVerifier] = []

    config_url = (settings.oauth2_configuration_url or "").strip()
    issuer_uri = (settings.oauth2_issuer_uri or "").strip().rstrip("/")
    client_id = (settings.oauth2_client_id or "").strip()
    client_secret = (settings.oauth2_client_secret or "").strip()
    audience = (settings.oauth2_audience or "").strip()

    if not config_url and issuer_uri:
        config_url = f"{issuer_uri}/.well-known/openid-configuration"

    base_url = f"{settings.public_api_base_url.rstrip('/')}{_MCP_ROUTE_PREFIX}"
    if config_url and client_id and client_secret:
        auth_server = ConnectCenterOIDCProxy(
            config_url=config_url,
            client_id=client_id,
            client_secret=client_secret,
            audience=audience or None,
            base_url=base_url,
        )
    else:
        logger.info("FastMCP OIDC proxy disabled: incomplete OAuth2 configuration.")

    mcp_jwt_issuer = (settings.mcp_jwt_issuer_uri or "").strip().rstrip("/")
    mcp_jwt_jwks_uri = (settings.mcp_jwt_jwks_uri or "").strip()
    mcp_jwt_audience = (settings.mcp_jwt_audience or "").strip()
    if mcp_jwt_issuer and mcp_jwt_jwks_uri and mcp_jwt_audience:
        for algorithm in _configured_mcp_jwt_algorithms():
            token_verifiers.append(
                JWTVerifier(
                    jwks_uri=mcp_jwt_jwks_uri,
                    issuer=mcp_jwt_issuer,
                    audience=mcp_jwt_audience,
                    algorithm=algorithm,
                    base_url=base_url,
                )
            )

    if auth_server and token_verifiers:
        return MultiAuth(server=auth_server, verifiers=token_verifiers, base_url=base_url)
    if auth_server:
        return auth_server
    if token_verifiers:
        if len(token_verifiers) > 1:
            return MultiAuth(verifiers=token_verifiers, base_url=base_url)
        return token_verifiers[0]
    return None


# Build the MCP server and OIDC auth proxy.
mcp, mcp_auth = _create_mcp_server(app)
# Wrap the MCP server as an ASGI app mounted at the root of the configured MCP prefix.
# Use Streamable HTTP SSE responses so MCP progress notifications can reach
# Spring's WebClientStreamableHttpTransport progress consumer during tool calls.
mcp_app = mcp.http_app(path="/", json_response=False, stateless_http=False)
# Merge the MCP lifespan with the FastAPI lifespan so both start and stop together.
app.router.lifespan_context = combine_lifespans(app.router.lifespan_context, mcp_app.lifespan)
# Mount the MCP ASGI app under the configured route prefix.
app.mount(_MCP_ROUTE_PREFIX, mcp_app)
if mcp_auth is not None:
    # The proxy base_url already includes the public MCP path, so passing the
    # mount prefix here would duplicate `/mcp` in protected-resource metadata.
    app.router.routes.extend(mcp_auth.get_well_known_routes())
# Expose the MCP server and auth proxy on app.state for access in tests and middleware.
app.state.mcp_server = mcp
app.state.mcp_auth = mcp_auth
