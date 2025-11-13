import pytest
import uuid
import os
import sys
import json
from pathlib import Path
from urllib.parse import urlparse

from dotenv import load_dotenv

# Load .env file from working directory or parent directory
# This ensures DATABASE_* environment variables are available for tests
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

# Add the parent directory to the Python path so we can import from the root
sys.path.insert(0, str(Path(__file__).parent.parent))

import sqlalchemy
from sqlalchemy.engine import Engine
from sqlmodel import Session, text, select

# Import AppUser model directly to avoid circular imports
from services.models.app_user import AppUser
from services.models.business_information_entity import TopLevelAsbiep
from services.models.biz_ctx import BizCtx, BizCtxValue, BizCtxAssignment
from services.models.ctx_category import CtxCategory
from services.models.ctx_scheme import CtxScheme, CtxSchemeValue


def _create_test_engine() -> Engine:
    """Create a database engine for testing without importing middleware (avoids circular imports)."""
    # Database configuration from environment variables
    # Support both DATABASE_URL (direct) or individual components
    DATABASE_URL = os.getenv("DATABASE_URL")
    
    # Check if DATABASE_URL contains variable substitution syntax (not supported by python-dotenv)
    if DATABASE_URL and "${" in DATABASE_URL:
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

    return sqlalchemy.create_engine(DATABASE_URL, echo=False)


def _table_exists(session: Session, table_name: str) -> bool:
    """Check if a table exists in the database.
    
    Args:
        session: Database session
        table_name: Name of the table to check
        
    Returns:
        True if the table exists, False otherwise
    """
    try:
        # Query information_schema to check if table exists
        result = session.exec(text(
            "SELECT COUNT(*) as count FROM information_schema.tables "
            "WHERE table_schema = DATABASE() AND table_name = :table_name"
        ), {"table_name": table_name})
        row = result.first()
        # The result is a Row object, access by index (first column is the count)
        if row is None:
            return False
        # Access the count value (first column)
        count = row[0]
        return count > 0
    except Exception:
        # If query fails, assume table doesn't exist
        return False


def _cleanup_user_associated_records(session: Session, user_id: int):
    """Delete all records associated with a user before deleting the user.
    
    This function deletes:
    1. All TopLevelAsbiep records owned by the user (and their related records:
       biz_ctx_assignment, abie, asbie, bbie, bbie_sc, asbiep_support_doc, asbiep, bbiep)
    2. All BizCtx records created by the user (and their related records:
       biz_ctx_assignment, biz_ctx_value)
    3. All CtxScheme records created by the user (and their related records:
       biz_ctx_value that reference ctx_scheme_value, ctx_scheme_value)
    4. All CtxCategory records created by the user (and their related ctx_schemes)
    
    Uses raw SQL with foreign key checks disabled to ensure all related records
    are deleted properly, even if CASCADE is not configured.
    
    Args:
        session: Database session
        user_id: The user ID to clean up records for
    """
    try:
        # Disable foreign key checks temporarily to allow deletion in any order
        session.exec(text("SET foreign_key_checks = 0"))
        
        # Step 1: Delete all TopLevelAsbiep records owned by this user and their related records
        # Get all top-level ASBIEP IDs owned by this user
        top_level_asbieps = session.exec(
            select(TopLevelAsbiep).where(TopLevelAsbiep.owner_user_id == user_id)
        ).all()
        
        for top_level_asbiep in top_level_asbieps:
            top_level_asbiep_id = top_level_asbiep.top_level_asbiep_id
            
            # Delete related records using raw SQL to ensure all are deleted
            # Delete biz_ctx_assignment records
            session.exec(text(
                "DELETE FROM biz_ctx_assignment WHERE top_level_asbiep_id = :top_level_asbiep_id"
            ), {"top_level_asbiep_id": top_level_asbiep_id})
            
            # Delete related BIE records (abie, asbie, bbie, asbiep, bbiep, bbie_sc, etc.)
            # These are deleted using owner_top_level_asbiep_id
            # Order matches the service's delete_top_level_asbiep function
            
            # Step 1: Delete abie records
            session.exec(text(
                "DELETE FROM abie WHERE owner_top_level_asbiep_id = :top_level_asbiep_id"
            ), {"top_level_asbiep_id": top_level_asbiep_id})
            
            # Step 2: Delete asbie records
            session.exec(text(
                "DELETE FROM asbie WHERE owner_top_level_asbiep_id = :top_level_asbiep_id"
            ), {"top_level_asbiep_id": top_level_asbiep_id})
            
            # Step 3: Delete bbie records
            session.exec(text(
                "DELETE FROM bbie WHERE owner_top_level_asbiep_id = :top_level_asbiep_id"
            ), {"top_level_asbiep_id": top_level_asbiep_id})
            
            # Step 4: Delete asbiep_support_doc records (only if table exists - it's in WIP state)
            # The service deletes these per asbiep, but we do it in bulk for efficiency
            if _table_exists(session, "asbiep_support_doc"):
                try:
                    session.exec(text(
                        "DELETE FROM asbiep_support_doc WHERE asbiep_id IN (SELECT asbiep_id FROM asbiep WHERE owner_top_level_asbiep_id = :top_level_asbiep_id)"
                    ), {"top_level_asbiep_id": top_level_asbiep_id})
                except Exception as e:
                    # If deletion fails (e.g., table structure changed), log and continue
                    print(f"Warning: Failed to delete asbiep_support_doc records: {e}")
            
            # Step 5: Delete asbiep records
            session.exec(text(
                "DELETE FROM asbiep WHERE owner_top_level_asbiep_id = :top_level_asbiep_id"
            ), {"top_level_asbiep_id": top_level_asbiep_id})
            
            # Step 6: Delete bbiep records
            session.exec(text(
                "DELETE FROM bbiep WHERE owner_top_level_asbiep_id = :top_level_asbiep_id"
            ), {"top_level_asbiep_id": top_level_asbiep_id})
            
            # Step 7: Delete bbie_sc records
            session.exec(text(
                "DELETE FROM bbie_sc WHERE owner_top_level_asbiep_id = :top_level_asbiep_id"
            ), {"top_level_asbiep_id": top_level_asbiep_id})
            
            # Finally delete the top-level ASBIEP
            session.delete(top_level_asbiep)
        
        # Step 2: Delete all BizCtx records created by this user
        biz_ctxs = session.exec(
            select(BizCtx).where(BizCtx.created_by == user_id)
        ).all()
        
        for biz_ctx in biz_ctxs:
            biz_ctx_id = biz_ctx.biz_ctx_id
            
            # Delete biz_ctx_assignment records for this business context
            session.exec(text(
                "DELETE FROM biz_ctx_assignment WHERE biz_ctx_id = :biz_ctx_id"
            ), {"biz_ctx_id": biz_ctx_id})
            
            # Delete biz_ctx_value records for this business context
            session.exec(text(
                "DELETE FROM biz_ctx_value WHERE biz_ctx_id = :biz_ctx_id"
            ), {"biz_ctx_id": biz_ctx_id})
            
            # Delete the business context
            session.delete(biz_ctx)
        
        # Step 3: Delete all CtxScheme records created by this user and their related records
        ctx_schemes = session.exec(
            select(CtxScheme).where(CtxScheme.created_by == user_id)
        ).all()
        
        for ctx_scheme in ctx_schemes:
            ctx_scheme_id = ctx_scheme.ctx_scheme_id
            
            # Delete biz_ctx_value records that reference ctx_scheme_value records belonging to this scheme
            # First get all ctx_scheme_value_ids for this scheme
            ctx_scheme_value_ids = session.exec(
                select(CtxSchemeValue.ctx_scheme_value_id).where(
                    CtxSchemeValue.owner_ctx_scheme_id == ctx_scheme_id
                )
            ).all()
            
            if ctx_scheme_value_ids:
                # Delete biz_ctx_value records that reference these ctx_scheme_value records
                # Build the IN clause with placeholders
                placeholders = ",".join([":id" + str(i) for i in range(len(ctx_scheme_value_ids))])
                params = {f"id{i}": ctx_scheme_value_ids[i] for i in range(len(ctx_scheme_value_ids))}
                session.exec(text(
                    f"DELETE FROM biz_ctx_value WHERE ctx_scheme_value_id IN ({placeholders})"
                ), params)
            
            # Delete ctx_scheme_value records for this scheme
            session.exec(text(
                "DELETE FROM ctx_scheme_value WHERE owner_ctx_scheme_id = :ctx_scheme_id"
            ), {"ctx_scheme_id": ctx_scheme_id})
            
            # Delete the context scheme
            session.delete(ctx_scheme)
        
        # Step 4: Delete all CtxCategory records created by this user
        # Note: This should be done after deleting ctx_schemes since ctx_schemes reference ctx_categories
        ctx_categories = session.exec(
            select(CtxCategory).where(CtxCategory.created_by == user_id)
        ).all()
        
        for ctx_category in ctx_categories:
            ctx_category_id = ctx_category.ctx_category_id
            
            # Delete ctx_schemes that belong to this category (if any remain)
            # These should already be deleted in Step 3, but we'll delete them again to be safe
            session.exec(text(
                "DELETE FROM ctx_scheme WHERE ctx_category_id = :ctx_category_id"
            ), {"ctx_category_id": ctx_category_id})
            
            # Delete the context category
            session.delete(ctx_category)
        
        # Re-enable foreign key checks
        session.exec(text("SET foreign_key_checks = 1"))
        session.commit()
        
    except Exception as e:
        # Re-enable foreign key checks even if there's an error
        try:
            session.exec(text("SET foreign_key_checks = 1"))
            session.rollback()
        except:
            pass
        raise e


def _get_access_token():
    """Get an access token for running tests.
    
    This function reads the access token from a file. The file path can be specified
    via the ACCESS_TOKEN_FILE environment variable, or it defaults to '.access_token'
    in the current working directory.
    
    The token file should contain JSON with an 'access_token' field, like:
    {
      "access_token": "...",
      "token_type": "Bearer",
      ...
    }
    
    Returns:
        str: A valid access token string that can be used for API requests
        
    Raises:
        FileNotFoundError: If the token file doesn't exist
        ValueError: If the token file is empty, contains invalid JSON, or missing access_token field
        json.JSONDecodeError: If the file contains invalid JSON
    """
    # Get token file path from environment variable or use default
    token_file = os.getenv("ACCESS_TOKEN_FILE", ".access_token")
    token_path = Path(token_file)
    
    # Try current directory first
    if not token_path.is_absolute():
        token_path = Path.cwd() / token_path
    
    # If file doesn't exist in current directory, try parent directory
    if not token_path.exists():
        token_path = Path(__file__).parent.parent / token_file
    
    # Read token from file
    if not token_path.exists():
        raise FileNotFoundError(
            f"Access token file not found: {token_path}\n"
            f"Please create a file containing your access token, or set ACCESS_TOKEN_FILE "
            f"environment variable to point to the token file."
        )
    
    try:
        with open(token_path, 'r') as f:
            content = f.read().strip()
        
        if not content:
            raise ValueError(
                f"Access token file is empty: {token_path}\n"
                f"Please add a valid access token to the file."
            )
        
        # Parse JSON and extract access_token
        try:
            token_data = json.loads(content)
        except json.JSONDecodeError as e:
            raise ValueError(
                f"Access token file contains invalid JSON: {token_path}\n"
                f"Error: {e}\n"
                f"Please ensure the file contains valid JSON with an 'access_token' field."
            ) from e
        
        if 'access_token' not in token_data:
            raise ValueError(
                f"Access token file missing 'access_token' field: {token_path}\n"
                f"Please ensure the JSON contains an 'access_token' field."
            )
        
        access_token = token_data['access_token']
        if not access_token:
            raise ValueError(
                f"Access token is empty in file: {token_path}\n"
                f"Please add a valid access token value."
            )
        
        return access_token
    except IOError as e:
        raise IOError(
            f"Failed to read access token file {token_path}: {e}\n"
            f"Please check file permissions and try again."
        ) from e

@pytest.fixture(scope="session")
def token():
    """Provides an authentication token for all tests in this session.
    
    This fixture runs once at the start of your test session and provides the same
    token to all tests. If getting the token fails, all tests will be skipped.
    
    To customize how tokens are obtained, edit the `_get_access_token()` function above.
    Common approaches include using "@modelcontextprotocol/inspector" or calling your
    authentication provider's API.
    """
    # Get the access token - customize _get_access_token() to change how this works
    access_token = _get_access_token()
    
    if not access_token:
        raise ValueError(
            "❌ Could not get an access token for testing.\n\n"
            "To fix this, please update the `_get_access_token()` function in this file.\n"
            "You can obtain tokens in several ways:\n"
            "  • Use '@modelcontextprotocol/inspector' for interactive authentication\n"
            "  • Call your authentication provider's API\n"
            "  • Read from environment variables or config files\n"
            "  • Use OAuth2 client credentials flow\n\n"
            "See the function's docstring for more details."
        )
    
    return access_token


@pytest.fixture
def invalid_token():
    """Provides a deliberately invalid token for testing error handling.
    
    Use this fixture in tests that verify your code properly handles invalid or
    expired authentication tokens.
    """
    return "invalid.token.here"


@pytest.fixture
def temp_end_user():
    """Create a temporary End-User account for testing and clean it up afterwards.
    
    This fixture creates a temporary End-User account using the database directly.
    The user is created with:
    - A unique login_id based on UUID
    - is_admin=False, is_developer=False (making it an End-User)
    - is_enabled=True
    
    The user is automatically deleted after the test completes.
    
    Returns:
        dict: Dictionary containing user_id and login_id of the created user
    """
    engine = _create_test_engine()
    session = Session(engine)
    
    # Create a unique login_id
    unique_id = str(uuid.uuid4())[:8]
    login_id = f"test_eu_{unique_id}"
    
    # Create the temporary End-User
    temp_user = AppUser(
        login_id=login_id,
        name=f"Test End-User {unique_id}",
        organization="Test Organization",
        email=f"test_eu_{unique_id}@test.example.com",
        is_admin=False,
        is_developer=False,  # This makes it an End-User
        is_enabled=True
    )
    
    try:
        session.add(temp_user)
        session.commit()
        session.refresh(temp_user)
        
        user_info = {
            'user_id': temp_user.app_user_id,
            'login_id': temp_user.login_id
        }
        
        yield user_info
        
    finally:
        # Cleanup: Delete associated records first, then the user
        try:
            # Delete all records associated with this user
            _cleanup_user_associated_records(session, temp_user.app_user_id)
            
            # Now delete the user
            session.delete(temp_user)
            session.commit()
        except Exception as e:
            # Log but don't fail if cleanup fails
            print(f"Warning: Failed to cleanup temporary user {login_id}: {e}")
            try:
                session.rollback()
            except:
                pass
        finally:
            session.close()


@pytest.fixture
def temp_end_user_for_transfer():
    """Create a temporary End-User account specifically for ownership transfer tests.
    
    This is similar to temp_end_user but creates a user that can be used as a target
    for ownership transfers. The user is automatically deleted after the test completes.
    
    Returns:
        dict: Dictionary containing user_id and login_id of the created user
    """
    engine = _create_test_engine()
    session = Session(engine)
    
    # Create a unique login_id
    unique_id = str(uuid.uuid4())[:8]
    login_id = f"test_transfer_eu_{unique_id}"
    
    # Create the temporary End-User
    temp_user = AppUser(
        login_id=login_id,
        name=f"Test Transfer End-User {unique_id}",
        organization="Test Organization",
        email=f"test_transfer_eu_{unique_id}@test.example.com",
        is_admin=False,
        is_developer=False,  # This makes it an End-User
        is_enabled=True
    )
    
    try:
        session.add(temp_user)
        session.commit()
        session.refresh(temp_user)
        
        user_info = {
            'user_id': temp_user.app_user_id,
            'login_id': temp_user.login_id
        }
        
        yield user_info
        
    finally:
        # Cleanup: Delete associated records first, then the user
        try:
            # Delete all records associated with this user
            _cleanup_user_associated_records(session, temp_user.app_user_id)
            
            # Now delete the user
            session.delete(temp_user)
            session.commit()
        except Exception as e:
            # Log but don't fail if cleanup fails
            print(f"Warning: Failed to cleanup temporary transfer user {login_id}: {e}")
            try:
                session.rollback()
            except:
                pass
        finally:
            session.close()
