import logging
import os
from contextlib import asynccontextmanager
from typing import AsyncIterator, Any
from urllib.parse import urlparse

from fastmcp import FastMCP
from mcp.server.lowlevel.server import LifespanResultT
# SQLModel's create_engine returns sqlalchemy.engine.Engine - import for type hints
import sqlalchemy
from sqlalchemy.engine import Engine
from sqlalchemy.exc import SQLAlchemyError
from sqlmodel import text

# Configure logging
logger = logging.getLogger(__name__)

# Import DatabaseEngineMiddleware and get_engine from middleware module
from middleware import DatabaseEngineMiddleware, get_engine


def create_engine() -> tuple[Engine, str]:
    # Create database engine for MariaDB
    # Database configuration from environment variables
    # Support both DATABASE_URL (direct) or individual components
    DATABASE_URL = os.getenv("DATABASE_URL")
    
    # Check if DATABASE_URL contains variable substitution syntax (not supported by python-dotenv)
    if DATABASE_URL and "${" in DATABASE_URL:
        logger.warning(
            "DATABASE_URL contains variable substitution syntax (${VAR}), which is not supported by python-dotenv. "
            "The ${VAR} syntax will be treated as a literal string, resulting in an invalid database URL. "
            "Please either:\n"
            "  1. Set DATABASE_URL with actual values (not ${VAR} syntax), or\n"
            "  2. Remove DATABASE_URL and set individual DATABASE_* variables (DATABASE_CONNECTOR, DATABASE_USERNAME, etc.) "
            "     and let the application construct DATABASE_URL automatically.\n"
            f"Current DATABASE_URL value: {DATABASE_URL}"
        )
        # Treat as if DATABASE_URL is not set, so we construct it from individual components
        DATABASE_URL = None
    
    if not DATABASE_URL:
        # Construct DATABASE_URL from individual components if not provided
        connector = os.getenv("DATABASE_CONNECTOR", "mariadb+mariadbconnector")
        username = os.getenv("DATABASE_USERNAME", "root")
        password = os.getenv("DATABASE_PASSWORD", "")
        hostname = os.getenv("DATABASE_HOSTNAME", "127.0.0.1")
        port = os.getenv("DATABASE_PORT", "3306")
        database = os.getenv("DATABASE_NAME", "")

        DATABASE_URL = f"{connector}://{username}:{password}@{hostname}:{port}/{database}"

    # Validate that DATABASE_URL is set
    if not DATABASE_URL:
        raise ValueError(
            "DATABASE_URL is not configured. Please set either:\n"
            "  - DATABASE_URL environment variable, or\n"
            "  - Individual DATABASE_* environment variables (DATABASE_HOSTNAME, DATABASE_USERNAME, etc.)"
        )

    # Validate that DATABASE_URL is a valid URL format
    try:
        parsed = urlparse(DATABASE_URL)
        if not parsed.scheme:
            raise ValueError("DATABASE_URL must include a scheme (e.g., 'mariadb+mariadbconnector://')")
        if not parsed.hostname:
            raise ValueError("DATABASE_URL must include a hostname")
    except Exception as e:
        if isinstance(e, ValueError):
            raise
        raise ValueError(
            f"DATABASE_URL is not a valid URL format: {DATABASE_URL}\n"
            f"Error: {str(e)}\n"
            f"Expected format: scheme://username:password@hostname:port/database"
        ) from e

    return sqlalchemy.create_engine(DATABASE_URL, echo=True), DATABASE_URL


def create_database_lifespan(engine: Engine, database_url: str):
    """
    Create a lifespan context manager for FastMCP application that performs
    database connectivity check during startup.
    
    Args:
        engine: The SQLAlchemy database engine
        database_url: The database URL string (for error messages)
    
    Returns:
        An async context manager function that can be used as FastMCP lifespan
    """
    @asynccontextmanager
    async def lifespan(server: FastMCP[LifespanResultT]) -> AsyncIterator[Any]:
        """
        Lifespan context manager for FastMCP application.
        
        Performs database connectivity check during startup to ensure the database
        is accessible before the application starts accepting requests. This provides
        fail-fast behavior if the database connection cannot be established.
        
        Args:
            mcp: The FastMCP instance (provided by FastMCP framework)
        
        Raises:
            RuntimeError: If database connection test fails during startup
        """
        # Startup: Test database connectivity
        logger.info("Starting database connectivity check...")
        try:
            # Test database connection by executing a simple query
            with engine.connect() as conn:
                result = conn.execute(text("SELECT 1"))
                result.fetchone()
            logger.info("✓ Database connectivity check passed successfully")
        except SQLAlchemyError as e:
            error_msg = (
                f"❌ Database connectivity check failed during startup. "
                f"Cannot connect to database at {database_url.split('@')[-1] if '@' in database_url else 'configured location'}. "
                f"Error: {str(e)}. "
                f"Please verify your DATABASE_URL configuration and ensure the database server is running and accessible."
            )
            logger.error(error_msg)
            raise RuntimeError(error_msg) from e
        except Exception as e:
            error_msg = (
                f"❌ Unexpected error during database connectivity check: {str(e)}. "
                f"Please check your database configuration and server status."
            )
            logger.error(error_msg)
            raise RuntimeError(error_msg) from e
        
        yield
        
        # Shutdown: Clean up database connections
        logger.info("Shutting down database connections...")
        engine.dispose()
        logger.info("✓ Database connections closed")
    
    return lifespan


def create_http_app_lifespan_wrapper(mcp_instance: FastMCP, mcp_lifespan):
    """
    Create a wrapper lifespan function for Starlette http_app that adapts
    the FastMCP lifespan function.
    
    The original lifespan function expects a FastMCP instance, but the
    Starlette app's lifespan_context receives the app instance. This wrapper
    adapts the call to pass the mcp_instance to the original lifespan function.
    
    Args:
        mcp_instance: The FastMCP instance to pass to the original lifespan
        mcp_lifespan: The original lifespan function that accepts FastMCP
    
    Returns:
        A lifespan function compatible with Starlette's lifespan_context
    """
    @asynccontextmanager
    async def http_app_lifespan(app) -> AsyncIterator[Any]:
        """
        Wrapper lifespan for Starlette http_app.
        
        This adapts the FastMCP lifespan to work with Starlette's lifespan_context
        by passing the mcp_instance to the original lifespan function.
        
        Args:
            app: The Starlette application instance (provided by the framework)
        """
        async with mcp_lifespan(mcp_instance):
            yield
    
    return http_app_lifespan
