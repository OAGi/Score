"""Application configuration via Pydantic Settings.

This module defines the environment-driven configuration for the backend,
including database connectivity, OAuth2/OIDC integration, and logging.

Key features:
- Explicit DB vendor selection with derived SQLAlchemy URL.
- Optional override for full SQLAlchemy database URL.
- OIDC issuer/client configuration to enable Bearer token auth.
- CORS and public URLs for local development and reference docs generation.
"""


from __future__ import annotations

import json
import os
import sys
from functools import lru_cache
from ipaddress import ip_address
from pathlib import Path
from typing import Annotated
from urllib.parse import urlsplit

from pydantic import field_validator, model_validator
from pydantic_settings import BaseSettings, NoDecode, SettingsConfigDict


def _resolve_settings_env_file() -> str:
    """Resolve which environment file should be loaded."""
    override = (
        os.getenv("APP_ENV_FILE")
        or os.getenv("SETTINGS_ENV_FILE")
        or os.getenv("APP_SETTINGS_ENV_FILE")
    )
    if override:
        return override

    is_running_pytest = "pytest" in " ".join(sys.argv)
    if is_running_pytest and Path(".env.test").exists():
        return ".env.test"
    return ".env"


_ENV_FILE = _resolve_settings_env_file()


def _build_http_url(host: str, port: int) -> str:
    """Build a local HTTP URL from a host and port."""
    return f"http://{host}:{port}"


def _origin_from_url(url: str) -> str | None:
    """Extract the scheme and authority from a URL."""
    parts = urlsplit(url)
    if not parts.scheme or not parts.netloc:
        return None
    return f"{parts.scheme}://{parts.netloc}"


def _is_loopback_hostname(hostname: str | None) -> bool:
    """Return whether a hostname refers to the local loopback interface."""
    if not hostname:
        return False
    if hostname == "localhost":
        return True
    try:
        return ip_address(hostname).is_loopback
    except ValueError:
        return False


def _append_origin(origins: list[str], origin: str | None) -> None:
    """Append an origin once, preserving insertion order."""
    if origin and origin not in origins:
        origins.append(origin)


class Settings(BaseSettings):
    """Pydantic settings model for the connectCenter API/MCP."""
    app_name: str = "connect-center-services"
    debug: bool = False

    # OAuth2 / OIDC
    # If set, Bearer token auth will be attempted (and Basic will remain fallback).
    oauth2_issuer_uri: str | None = None
    # Name of the provider/app as stored in `oauth2_app.provider_name`.
    # Prefer this when the DB is shared with other applications, to avoid ambiguous issuer/client_id matches.
    oauth2_provider_name: str | None = None
    # The OAuth2 client/application identifier this backend expects tokens for.
    # Used to disambiguate providers when multiple applications share an issuer.
    oauth2_client_id: str | None = None
    oauth2_client_secret: str | None = None
    oauth2_configuration_url: str | None = None
    oauth2_audience: str | None = None

    # DB vendor selection: mariadb
    db_vendor: str = "mariadb"

    # Optional explicit SQLAlchemy URL (recommended for production).
    # Examples:
    # - mariadb: mysql+asyncmy://user:pass@host:3306/dbname
    database_url: str | None = None

    # MariaDB connection settings
    db_host: str = "127.0.0.1"
    db_port: int = 3306
    db_user: str
    db_password: str
    db_name: str

    # SQLAlchemy
    sql_echo: bool = False
    db_pool_pre_ping: bool = True

    # Dev convenience: create tables at startup (no migrations).
    db_auto_create: bool = False

    # Server bind addresses used by the local API and docs dev servers.
    host: str = "127.0.0.1"
    port: int = 5555
    docs_host: str = "127.0.0.1"
    docs_port: int = 3001

    # Public docs base URL used by the standalone docs server for OIDC callbacks.
    public_docs_base_url: str | None = None

    # CORS: allow docs dev-server (Next) to call the API directly.
    # Set `CORS_ALLOW_ORIGINS` to a comma-separated list to override.
    cors_allow_origins: Annotated[list[str] | None, NoDecode] = None

    # Public base URL used in generated reference docs examples.
    public_api_base_url: str | None = None

    model_config = SettingsConfigDict(
        env_file=_ENV_FILE,
        env_prefix="",
        extra="ignore",
    )

    @field_validator("cors_allow_origins", mode="before")
    @classmethod
    def _parse_cors_allow_origins(cls, value: object) -> list[str] | None:
        """Accept a single origin, comma-separated origins, or a JSON array."""
        if value is None:
            return None

        if isinstance(value, str):
            stripped = value.strip()
            if not stripped:
                return []
            if stripped.startswith("["):
                decoded = json.loads(stripped)
                if not isinstance(decoded, list):
                    raise ValueError("CORS_ALLOW_ORIGINS JSON value must be an array.")
                return [str(item).strip() for item in decoded if str(item).strip()]
            return [item.strip() for item in stripped.split(",") if item.strip()]

        if isinstance(value, list):
            return [str(item).strip() for item in value if str(item).strip()]

        raise TypeError("CORS_ALLOW_ORIGINS must be a string or list of strings.")

    @field_validator("public_docs_base_url", "public_api_base_url", mode="before")
    @classmethod
    def _normalize_optional_public_url(cls, value: object) -> str | None:
        """Treat blank public URLs as unset so they can be derived from host/port."""
        if value is None:
            return None
        if not isinstance(value, str):
            raise TypeError("Public base URLs must be strings.")
        stripped = value.strip()
        return stripped or None

    @model_validator(mode="after")
    def _apply_derived_public_defaults(self) -> Settings:
        """Derive local public URLs and CORS defaults from bind host/port settings."""
        if self.public_docs_base_url is None:
            self.public_docs_base_url = _build_http_url(self.docs_host, self.docs_port)

        if self.public_api_base_url is None:
            self.public_api_base_url = _build_http_url(self.host, self.port)

        if self.cors_allow_origins is None:
            origins: list[str] = []
            for base_url in (self.public_docs_base_url, self.public_api_base_url):
                origin = _origin_from_url(base_url)
                _append_origin(origins, origin)

                parsed = urlsplit(base_url)
                if _is_loopback_hostname(parsed.hostname) and parsed.hostname != "localhost":
                    localhost_origin = f"{parsed.scheme}://localhost"
                    if parsed.port is not None:
                        localhost_origin = f"{localhost_origin}:{parsed.port}"
                    _append_origin(origins, localhost_origin)

            self.cors_allow_origins = origins

        return self

    @property
    def sqlalchemy_database_url(self) -> str:
        """Build the SQLAlchemy database URL.

        Returns:
            Result of the operation.
        """
        if self.database_url:
            return self.database_url

        vendor = self.db_vendor.lower()
        if vendor == "mariadb":
            # SQLAlchemy URL for asyncmy driver
            # https://docs.sqlalchemy.org/en/20/dialects/mysql.html
            return (
                f"mysql+asyncmy://{self.db_user}:{self.db_password}"
                f"@{self.db_host}:{self.db_port}/{self.db_name}"
                f"?charset=utf8mb4"
            )

        raise ValueError(f"Unsupported DB_VENDOR: {self.db_vendor}")


@lru_cache(maxsize=1)
def get_settings() -> Settings:
    """Return a cached Settings instance.

    Returns:
        Result of the operation.
    """
    return Settings()


settings = get_settings()
