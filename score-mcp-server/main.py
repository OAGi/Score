import logging
import os
from pathlib import Path

from dotenv import load_dotenv
from fastmcp import FastMCP
from fastmcp.server.auth.providers.github import GitHubProvider
from starlette.middleware import Middleware
from starlette.middleware.cors import CORSMiddleware

# Load .env file from working directory first (before configuring logging)
# This ensures LOG_LEVEL and other environment variables are available
# override=False means environment variables take precedence over .env file values
# Try multiple locations in order:
# 1. Working directory (.env)
# 2. Working directory parent (.env) - for cases where script is in a subdirectory
# 3. Fallback to default dotenv behavior
working_dir = Path.cwd()
env_path = working_dir / ".env"
if not env_path.exists():
    # Try parent directory (useful if running from a subdirectory)
    env_path = working_dir.parent / ".env"
if env_path.exists():
    load_dotenv(dotenv_path=env_path, override=False)
else:
    # Fallback: try loading from current directory (for local development)
    load_dotenv(override=False)

# Configure logging with level from environment variable
# Parse LOG_LEVEL from environment (default: INFO)
# Valid values: DEBUG, INFO, WARNING, ERROR, CRITICAL (case-insensitive)
log_level_str = os.getenv("LOG_LEVEL", "INFO").upper()
log_level_map = {
    "DEBUG": logging.DEBUG,
    "INFO": logging.INFO,
    "WARNING": logging.WARNING,
    "ERROR": logging.ERROR,
    "CRITICAL": logging.CRITICAL,
}
log_level = log_level_map.get(log_level_str, logging.INFO)
if log_level_str not in log_level_map:
    # Use INFO as default if invalid level is provided
    print(f"Warning: Invalid LOG_LEVEL '{log_level_str}', using INFO. Valid values: DEBUG, INFO, WARNING, ERROR, CRITICAL")

logging.basicConfig(level=log_level, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# Log that .env file was loaded (now that logging is configured)
if env_path.exists():
    logger.info(f"Loaded .env file from: {env_path}")
    logger.debug(f"Logging level set to: {log_level_str} ({log_level})")
else:
    logger.debug("No .env file found, using environment variables only")

from auth import create_auth_provider
from database import create_engine, create_database_lifespan
from middleware import (
    AppUserContextMiddleware,
    DatabaseEngineMiddleware,
    RequestResponseLoggingMiddleware,
    parse_cors_config,
)

# Create Database Engine
engine, database_url = create_engine()

# Create authentication provider
auth = create_auth_provider()

# Create database lifespan for startup connectivity check
mcp_lifespan = create_database_lifespan(engine, database_url)

# Parse stateless_http setting from environment
# stateless_http=True is required to avoid "Bad Request: No valid session ID provided" errors
# when running in Docker containers. Without this parameter, FastMCP expects session management
# which causes issues with HTTP transport in containerized environments.
# See: https://github.com/modelcontextprotocol/python-sdk/issues/808
# If CORS is enabled, stateless_http will be automatically set to True.
def parse_bool_env(var_name: str, default: bool = False) -> bool:
    """Parse boolean from environment variable."""
    value = os.getenv(var_name)
    if value is None:
        return default
    return value.lower() in ("true", "1", "yes", "on")

# Check if CORS is enabled
cors_enabled = parse_bool_env("CORS_ENABLED", False)

# Determine stateless_http setting
# If CORS is enabled, stateless_http must be True
# Otherwise, use STATELESS_HTTP env var (default: True for Docker compatibility)
if cors_enabled:
    stateless_http = True
    logger.info("CORS is enabled, setting stateless_http=True (required)")
else:
    stateless_http = parse_bool_env("STATELESS_HTTP", True)
    logger.info(f"stateless_http={stateless_http} (from STATELESS_HTTP env var)")

mcp = FastMCP("Score MCP Server", auth=auth, 
              tool_serializer=lambda res: res.model_dump_json(exclude_none=True),
              stateless_http=stateless_http)

# Add the database engine middleware
mcp.add_middleware(DatabaseEngineMiddleware(engine))

# Add the app user middleware
mcp.add_middleware(AppUserContextMiddleware(engine))


def auto_mount_tools():
    """
    Automatically discover and mount FastMCP instances from Python files in the tools directory.
    
    This function scans the tools directory for Python files, imports each module,
    and mounts any FastMCP instances found. This allows new tools to be automatically
    discovered without requiring manual import and mount statements.
    """
    import importlib
    from pathlib import Path
    
    tools_dir = Path(__file__).parent / "tools"
    
    if not tools_dir.exists():
        logger.warning(f"Tools directory not found: {tools_dir}")
        return
    
    # Get all Python files in the tools directory (excluding __init__.py and subdirectories)
    tool_files = [
        f for f in tools_dir.iterdir()
        if f.is_file() and f.suffix == ".py" and f.name != "__init__.py"
    ]
    
    mounted_count = 0
    for tool_file in tool_files:
        module_name = f"tools.{tool_file.stem}"
        
        try:
            # Import the module
            module = importlib.import_module(module_name)
            
            # Check if the module has a 'mcp' attribute
            if hasattr(module, "mcp"):
                mcp_instance = getattr(module, "mcp")
                
                # Verify it's a FastMCP instance
                if isinstance(mcp_instance, FastMCP):
                    mcp.mount(mcp_instance)
                    mounted_count += 1
                    logger.info(f"Mounted tool module: {module_name}")
                else:
                    logger.debug(f"Module {module_name} has 'mcp' attribute but it's not a FastMCP instance")
            else:
                logger.debug(f"Module {module_name} does not have 'mcp' attribute")
                
        except ImportError as e:
            logger.warning(f"Failed to import tool module {module_name}: {e}")
        except Exception as e:
            logger.error(f"Error processing tool module {module_name}: {e}")
    
    logger.info(f"Auto-mounted {mounted_count} tool module(s)")


# Auto-mount all tools from the tools directory
auto_mount_tools()


@mcp.custom_route("/health", methods=["GET"])
async def health_check(request):
    from starlette.responses import JSONResponse
    return JSONResponse({"status": "healthy", "service": "score-mcp-server"})


# Define custom middleware
custom_middleware = [
    Middleware(RequestResponseLoggingMiddleware, max_body_size=1024*1024),
]

# Add CORS middleware only if enabled
if cors_enabled:
    cors_config = parse_cors_config()
    logger.info(f"CORS is enabled. CORS configuration: {cors_config}")
    custom_middleware.append(
        Middleware(
            CORSMiddleware,
            **cors_config
        )
    )
else:
    logger.info("CORS middleware is disabled (CORS_ENABLED=false or not set)")

# Create MCP app with path
app = mcp.http_app(path="/mcp", middleware=custom_middleware)

if __name__ == "__main__":
    # Get host and port from environment variables with defaults
    host = os.getenv("HOST", "0.0.0.0")
    port = int(os.getenv("PORT", "8000"))
    mcp.run(transport='http', host=host, port=port)
