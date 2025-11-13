"""
Transaction management using ContextVar for session handling.

This module provides a decorator-based transaction management system that:
- Uses ContextVar to store the database session
- Reuses existing sessions for nested function calls
- Handles automatic commit/rollback based on read_only flag
- Properly manages session lifecycle
"""

import contextvars
import logging
from functools import wraps
from typing import Callable, TypeVar, ParamSpec, Type, Any

from sqlmodel import Session
from sqlalchemy.engine import Engine

from middleware import get_engine

# Configure logging
logger = logging.getLogger(__name__)

# Create a context variable to store the database session
session_context_var: contextvars.ContextVar[Session | None] = contextvars.ContextVar(
    "session_context", default=None
)

P = ParamSpec('P')
R = TypeVar('R')

# Type variable for model classes
M = TypeVar('M')


def _get_session() -> Session:
    """
    Get the current database session from the context variable.
    
    This is an internal function. Use the public helper functions instead:
    - db_add() for adding objects
    - db_get() for getting objects by ID
    - db_delete() for deleting objects
    - db_refresh() for refreshing objects
    - db_exec() for executing queries
    
    Returns:
        Session: The current database session
        
    Raises:
        RuntimeError: If no session exists in the context. This usually means the
                     function calling this is not decorated with @transaction.
    """
    session = session_context_var.get()
    
    if session is None:
        raise RuntimeError(
            "No database session available in context. "
            "Ensure the function is decorated with @transaction or called from "
            "within a @transaction decorated function."
        )
    
    return session


# Public API: Database operation helpers
def db_add(instance: Any) -> None:
    """
    Add an object to the current database session.
    
    Args:
        instance: The model instance to add to the session
    """
    _get_session().add(instance)


def db_get(model: Type[M], ident: Any) -> M | None:
    """
    Get an object by its primary key.
    
    Args:
        model: The model class
        ident: The primary key value
        
    Returns:
        The model instance if found, None otherwise
    """
    return _get_session().get(model, ident)


def db_delete(instance: Any) -> None:
    """
    Mark an object for deletion in the current database session.
    
    Args:
        instance: The model instance to delete
    """
    _get_session().delete(instance)


def db_refresh(instance: Any) -> None:
    """
    Refresh an object from the database.
    
    Args:
        instance: The model instance to refresh
    """
    _get_session().refresh(instance)


def db_flush() -> None:
    """
    Flush pending changes to the database without committing.
    
    This is useful when you need to get auto-generated IDs (like primary keys)
    before the transaction commits. The changes will be visible within the
    current transaction but not persisted until commit.
    
    Example:
        item = Model(...)
        db_add(item)
        db_flush()  # Now item.id is available
        # Use item.id for other operations
    """
    _get_session().flush()


def db_exec(statement: Any) -> Any:
    """
    Execute a SQLModel statement (select, insert, update, etc.).
    
    This function returns the result object which can be used with methods like:
    - .first() - get first result
    - .one() - get exactly one result (raises if none or multiple)
    - .one_or_none() - get one result or None
    - .all() - get all results
    
    Args:
        statement: A SQLModel statement (e.g., select(Model))
        
    Returns:
        The execution result object
        
    Example:
        from sqlmodel import select
        result = db_exec(select(User).where(User.id == 1))
        user = result.first()
    """
    return _get_session().exec(statement)


# Backwards compatibility alias (deprecated - use helper functions instead)
def get_session() -> Session:
    """
    Get the current database session from the context variable.
    
    .. deprecated:: Use db_add(), db_get(), db_delete(), db_refresh(), or db_exec() instead.
                   This function may be removed in a future version.
    
    Returns:
        Session: The current database session
    """
    return _get_session()


def transaction(read_only: bool = False):
    """
    Decorator for managing database transactions using ContextVar.
    
    This decorator:
    - Manages session lifecycle automatically
    - Reuses existing sessions for nested function calls
    - Commits transactions for write operations (unless read_only=True)
    - Rolls back on exceptions
    - Closes sessions properly
    
    Args:
        read_only: If True, the transaction is read-only and won't commit changes.
                  If False (default), changes will be committed on success.
    
    Usage:
        from services.transaction import transaction, db_add, db_get, db_exec
        from sqlmodel import select
        
        @transaction(read_only=True)
        def get_data(self):
            # Use helper functions instead of accessing session directly
            item = db_get(Model, id)
            results = db_exec(select(Model)).all()
            return results
            
        @transaction(read_only=False)
        def create_data(self):
            # Add, modify, or delete using helper functions
            item = Model(...)
            db_add(item)
            # Changes auto-committed by decorator
    """
    def decorator(func: Callable[P, R]) -> Callable[P, R]:
        @wraps(func)
        def wrapper(*args: P.args, **kwargs: P.kwargs) -> R:
            # Check if a session already exists (nested call)
            existing_session = session_context_var.get()
            session_created = False
            session = existing_session
            
            try:
                # If no session exists, create one
                if session is None:
                    engine = get_engine()
                    if engine is None:
                        raise RuntimeError(
                            "No database engine available. "
                            "Ensure DatabaseEngineMiddleware is properly configured."
                        )
                    # Create session with expire_on_commit=False to prevent attribute expiration
                    # This allows objects to be safely used after session closes
                    session = Session(engine, expire_on_commit=False)
                    session_context_var.set(session)
                    session_created = True
                
                # Execute the function
                result = func(*args, **kwargs)
                
                # Commit if write operation and session was created by this decorator
                if not read_only and session_created:
                    try:
                        session.commit()
                        logger.debug(f"Transaction committed for {func.__name__}")
                    except Exception as e:
                        logger.error(f"Error committing transaction for {func.__name__}: {e}")
                        session.rollback()
                        raise
                
                # Expunge all objects from session before closing to prevent detached instance errors
                # This allows returned objects to be used after the session closes
                if session_created and session is not None:
                    session.expunge_all()
                
                return result
                
            except Exception as e:
                # Rollback on any exception if session was created by this decorator
                if session_created and session is not None:
                    try:
                        session.rollback()
                        logger.debug(f"Transaction rolled back for {func.__name__} due to exception")
                    except Exception as rollback_error:
                        logger.error(f"Error rolling back transaction for {func.__name__}: {rollback_error}")
                
                # Re-raise the original exception
                raise
                
            finally:
                # Close session only if we created it (not nested)
                if session_created and session is not None:
                    try:
                        session.close()
                        session_context_var.set(None)
                        logger.debug(f"Session closed for {func.__name__}")
                    except Exception as e:
                        logger.error(f"Error closing session for {func.__name__}: {e}")
        
        return wrapper
    return decorator

