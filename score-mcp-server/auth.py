import contextvars
import json
import logging
import os
from typing import Optional
from typing import Union

from fastmcp.exceptions import ToolError
from fastmcp.server.dependencies import get_access_token, AccessToken
from fastmcp.server.middleware import Middleware as FastMCPMiddleware, MiddlewareContext
from pydantic import AnyHttpUrl
from pydantic_settings import BaseSettings, SettingsConfigDict
from sqlmodel import Session, select

from services.models import AppUser, AppOAuth2User

# Import AppUserContextMiddleware and get_current_user from middleware module
# Note: This import is placed here to avoid circular dependencies
# since middleware imports from services.models
from middleware import AppUserContextMiddleware, get_current_user

logger = logging.getLogger(__name__)


class AuthProviderParams(BaseSettings):
    """Authentication provider parameters from environment variables."""
    model_config = SettingsConfigDict(
        env_prefix="AUTH_PROVIDER_PARAMS_",
        env_file=".env",
        extra="ignore"
    )
    
    # OIDC configuration
    config_url: Optional[Union[AnyHttpUrl, str]] = None
    strict: Optional[bool] = None
    
    # Upstream server configuration
    client_id: Optional[str] = None
    client_secret: Optional[str] = None
    audience: Optional[str] = None
    timeout_seconds: Optional[int] = None
    
    # Token verifier
    algorithm: Optional[str] = None
    required_scopes: Optional[list[str]] = None
    
    # FastMCP server configuration
    base_url: Optional[Union[AnyHttpUrl, str]] = None
    redirect_path: Optional[str] = None
    
    # Client configuration
    allowed_client_redirect_uris: Optional[list[str]] = None
    client_storage: Optional[str] = None
    
    # Token validation configuration
    token_endpoint_auth_method: Optional[str] = None
    
    def model_dump_safe(self, exclude_none: bool = True) -> dict:
        """Return model data with sensitive fields hidden for logging."""
        data = self.model_dump(exclude_none=exclude_none)
        # Hide sensitive fields
        if 'client_secret' in data:
            data['client_secret'] = '**********'
        return data


class ClientStorageParams(BaseSettings):
    """OIDC client storage parameters from environment variables."""
    model_config = SettingsConfigDict(
        env_prefix="CLIENT_STORAGE_PARAMS_",
        env_file=".env",
        extra="ignore"
    )
    
    # Common storage parameters
    cache_dir: Optional[str] = None
    storage_path: Optional[str] = None
    path: Optional[str] = None


def get_env_map(prefix: str) -> dict:
    """Collect environment variables by prefix into a lowercase-keyed dict.

    Example: prefix="AUTH_PROVIDER_PARAMS_" collects AUTH_PROVIDER_PARAMS_CLIENT_ID
    into {"client_id": value}.
    """
    result: dict[str, str] = {}
    plen = len(prefix)
    for key, value in os.environ.items():
        if key.startswith(prefix):
            short_key = key[plen:].lower()
            if short_key:
                result[short_key] = value
    return result


def parse_json_env(var_name: str) -> tuple[object | None, Exception | None]:
    """Parse a JSON environment variable safely.

    Returns (value, error). When parsing fails, returns (None, error).
    """
    raw = os.getenv(var_name)
    if not raw:
        return None, None
    try:
        return json.loads(raw), None
    except (json.JSONDecodeError, TypeError) as e:
        return None, e


def merge_params(base_params: object | None, prefix: str) -> object | None:
    """Merge JSON-derived params with prefix-based overrides.

    - If base_params is a dict, overlay with prefix map (prefix wins)
    - If base_params is list/str/None and prefix map exists, return prefix map (dict)
    - If neither present, return None
    """
    overrides = get_env_map(prefix)

    if base_params is None and not overrides:
        return None

    if isinstance(base_params, dict):
        if overrides:
            merged = dict(base_params)
            merged.update(overrides)
            return merged
        return base_params

    if overrides:
        # Convert to kwargs-style dict if we have overrides but base isn't a dict
        return overrides

    return base_params

def create_auth_provider():
    """Create authentication provider based on environment variables."""
    auth_provider = os.getenv("AUTH_PROVIDER")
    logger = logging.getLogger(__name__)

    logger.info("create_auth_provider: AUTH_PROVIDER=%s", auth_provider)
    
    # If no AUTH_PROVIDER is specified, return None (no authentication)
    if not auth_provider:
        return None
    
    try:
        import importlib
        module_path, class_name = auth_provider.rsplit(".", 1)
        module = importlib.import_module(module_path)
        auth_class = getattr(module, class_name)
        logger.debug("Resolved auth class: %s.%s", module_path, class_name)
        
        # Load auth provider parameters using Pydantic settings
        auth_params = AuthProviderParams()
        logger.debug("Auth params (from AUTH_PROVIDER_PARAMS_): %s", auth_params.model_dump_safe())

        # Convert to dict and filter out None values
        params_dict = auth_params.model_dump(exclude_none=True)
        
        if params_dict:
            try:
                # Handle client_storage specially for OIDCProxy
                if class_name == "OIDCProxy" and "client_storage" in params_dict:
                    client_storage_config = params_dict.pop("client_storage")
                    client_storage = None
                    
                    if client_storage_config:
                        try:
                            import importlib
                            if isinstance(client_storage_config, str):
                                # It's a class name string
                                module_path, storage_class_name = client_storage_config.rsplit(".", 1)
                                module = importlib.import_module(module_path)
                                storage_class = getattr(module, storage_class_name)
                                logger.debug("OIDC client_storage class: %s.%s", module_path, storage_class_name)
                                
                                # Load client storage parameters using Pydantic settings
                                storage_params = ClientStorageParams()
                                storage_params_dict = storage_params.model_dump(exclude_none=True)
                                logger.debug("Client storage params (from CLIENT_STORAGE_PARAMS_): %s", storage_params_dict)
                                
                                # Convert string paths to pathlib.Path objects for JSONFileStorage
                                if storage_params_dict:
                                    from pathlib import Path
                                    # Convert cache_dir to Path if it exists
                                    if 'cache_dir' in storage_params_dict:
                                        storage_params_dict['cache_dir'] = Path(storage_params_dict['cache_dir'])
                                    # Convert storage_path to Path if it exists
                                    if 'storage_path' in storage_params_dict:
                                        storage_params_dict['storage_path'] = Path(storage_params_dict['storage_path'])
                                    # Convert path to Path if it exists
                                    if 'path' in storage_params_dict:
                                        storage_params_dict['path'] = Path(storage_params_dict['path'])
                                    
                                    client_storage = storage_class(**storage_params_dict)
                                    logger.debug("Client storage instantiated with params: %s", storage_params_dict)
                                else:
                                    client_storage = storage_class()
                                    logger.debug("Client storage instantiated with default constructor")
                            else:
                                # It's already an object
                                client_storage = client_storage_config
                        except (ImportError, AttributeError) as e:
                            logger.error("Error importing client storage %s: %s", client_storage_config, e)
                            client_storage = None
                    
                    # Add client_storage to params
                    params_dict["client_storage"] = client_storage
                
                # Use keyword arguments
                instance = auth_class(**params_dict)
                # Create a safe version of params_dict for logging
                safe_params = params_dict.copy()
                if 'client_secret' in safe_params:
                    safe_params['client_secret'] = '**********'
                logger.info("Auth provider instantiated with kwargs: %s", safe_params)
                return instance
            except (TypeError,) as e:
                logger.error("Error applying AUTH_PROVIDER_PARAMS: %s", e)
                # Fall through to default construction
                instance = auth_class()
                logger.info("Auth provider instantiated with default constructor after error")
                return instance
        else:
            # Use default constructor
            instance = auth_class()
            logger.info("Auth provider instantiated with default constructor (no params)")
            return instance
    except (ImportError, AttributeError) as e:
        logger.error("Error importing %s: %s", auth_provider, e)
        return None