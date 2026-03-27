"""Database engine/session management for the connectCenter backend.

This module provides lazy initialization of the SQLAlchemy async engine and
session factory, plus helpers for health checks and model initialization.

Key features:
- Lazy engine creation to avoid importing optional DB drivers during test collection.
- Shared sessionmaker configured for async sessions with `expire_on_commit=False`.
- Health-check helpers (`ping_db`) for readiness probes.
- Model initialization (`init_models`) via the active vendor plugin.
"""


from __future__ import annotations

from typing import AsyncIterator

from sqlalchemy import text
from sqlalchemy.ext.asyncio import AsyncEngine, AsyncSession, async_sessionmaker, create_async_engine

from app.repositories.vendor_plugins import get_vendor_plugin
from app.settings import settings

_engine: AsyncEngine | None = None
_sessionmaker: async_sessionmaker[AsyncSession] | None = None


def get_engine() -> AsyncEngine:
    """Lazily create the SQLAlchemy engine.

    Returns:
        Result of the operation.
    """
    global _engine
    if _engine is None:
        _engine = create_async_engine(
            settings.sqlalchemy_database_url,
            echo=settings.sql_echo,
            pool_pre_ping=settings.db_pool_pre_ping,
        )
    return _engine


def get_sessionmaker() -> async_sessionmaker[AsyncSession]:
    """Return a cached async sessionmaker.

    Returns:
        Result of the operation.
    """
    global _sessionmaker
    if _sessionmaker is None:
        _sessionmaker = async_sessionmaker(bind=get_engine(), class_=AsyncSession, expire_on_commit=False)
    return _sessionmaker


async def get_session() -> AsyncIterator[AsyncSession]:
    """Yield an async SQLAlchemy session for request-scoped usage.

    Yields:
        Result of the operation.
    """
    async with get_sessionmaker()() as session:
        try:
            yield session
            await session.commit()
        except Exception:
            await session.rollback()
            raise
        finally:
            await session.close()


async def ping_db(session: AsyncSession) -> None:
    """Run a lightweight query to verify database connectivity.

    Args:
        session: Database session bound to the current request.
    """
    await session.execute(text("SELECT 1"))


async def init_models() -> None:
    """Initialize database schema for the active vendor plugin."""
    async with get_engine().begin() as conn:
        plugin = get_vendor_plugin()
        await conn.run_sync(plugin.base.metadata.create_all)
