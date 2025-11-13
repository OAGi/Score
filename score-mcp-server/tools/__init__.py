import logging

from fastmcp.exceptions import ToolError
# SQLModel's create_engine returns sqlalchemy.engine.Engine - import for type hints
from sqlalchemy.engine import Engine

from middleware import get_current_user, get_engine
from services.models import AppUser
from services import Sort
from tools.models.common import PaginationResponse, UserInfo, WhoAndWhen

# Configure logging
logger = logging.getLogger(__name__)




def _get_user_roles(creator: AppUser) -> list[str]:
    """Extract user roles from creator information."""
    roles = []
    if creator.is_admin:
        roles.append('Admin')
    if creator.is_developer:
        roles.append('Developer')
    if not roles:  # Only add End-User if no other roles
        roles.append('End-User')
    return roles


def _validate_auth_and_db() -> tuple[AppUser, Engine]:
    """
    Validate authentication, user enabled status, and database connection.

    Returns:
        tuple: (app_user, engine) - authenticated and enabled user with database engine
        
    Raises:
        ToolError: If authentication fails, user is disabled, or database connection is unavailable
    """
    logger.debug("Validating authentication and database connection")
    try:
        # Validate authentication
        logger.debug("Checking user authentication")
        app_user: AppUser | None = get_current_user()
        if not app_user:
            logger.warning("Authentication failed: no user found")
            raise ToolError(
                "Authentication required to perform this operation. Please ensure you are logged in with a valid authentication token. Check that your token is not expired and has the necessary permissions. Contact your system administrator if the issue persists.")

        logger.debug(f"User found: {app_user.login_id} (ID: {app_user.app_user_id}, enabled: {app_user.is_enabled})")

        # Check if user is enabled
        if not app_user.is_enabled:
            logger.warning(f"Access denied: account disabled for user {app_user.login_id} (ID: {app_user.app_user_id})")
            raise ToolError(
                f"Access denied. Your user account (login_id: {app_user.login_id}) is currently disabled. Please contact your system administrator to enable your account.")

        # Validate database connection
        logger.debug("Checking database connection")
        engine: Engine | None = get_engine()
        if not engine:
            logger.error("Database connection unavailable")
            raise ToolError(
                "Database connection is not available. Please check that the system environment variable is properly configured and the database server is running. Contact your system administrator if the issue persists.")

        logger.debug(f"Validation successful for user: {app_user.login_id}")
        return app_user, engine
    except Exception as e:
        if isinstance(e, ToolError):
            logger.debug(f"Validation error: {e}")
            raise e
        else:
            logger.error(f"Unexpected error during validation: {str(e)}", exc_info=True)
            raise ToolError(
                f"An unexpected error occurred during authentication or database validation: {str(e)}. Please contact your system administrator if the issue persists.") from e


def _create_user_info(creator: AppUser | None) -> UserInfo | None:
    """Create UserInfo object from creator."""
    if creator is None:
        return None
    return UserInfo(
        user_id=creator.app_user_id,
        login_id=creator.login_id,
        username=creator.name or creator.login_id,
        roles=_get_user_roles(creator)
    )


def parse_order_by_to_sorts(order_by: str) -> list[Sort]:
    """
    Parse order_by string format and create Sort objects.

    Args:
        order_by: Order by string in format "(-|+)?<column_name>(,(-|+)?<column_name>)*"

    Returns:
        list[Sort]: List of Sort objects

    Raises:
        ValueError: If the format is invalid
    """
    logger.debug(f"Parsing sort order: {order_by}")
    
    if not order_by.strip():
        logger.warning("Empty sort order string provided")
        raise ValueError("Order by string cannot be empty")

    sorts = []
    parts = order_by.split(',')
    logger.debug(f"Processing {len(parts)} sort criteria")

    for part in parts:
        part = part.strip()
        if not part:
            logger.debug(f"Skipping empty sort criteria")
            continue

        # Check for direction prefix
        if part.startswith('-'):
            direction = 'desc'
            column_name = part[1:].strip()
        elif part.startswith('+'):
            direction = 'asc'
            column_name = part[1:].strip()
        else:
            direction = 'asc'  # Default to ascending
            column_name = part.strip()

        if not column_name:
            logger.error(f"Invalid sort criteria: empty column name in '{part}'")
            raise ValueError(f"Empty column name in order_by: '{part}'")

        logger.debug(f"Sort criteria: column '{column_name}', direction '{direction}'")
        sorts.append(Sort(column=column_name, direction=direction))

    logger.debug(f"Parsed {len(sorts)} sort criteria")
    return sorts
