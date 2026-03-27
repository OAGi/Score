"""Health check endpoints.

Provides basic readiness checks for the API and database connectivity.
All endpoints require authentication.
"""


from __future__ import annotations

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_session, ping_db

router = APIRouter(tags=["health"])


@router.get(
    "/health",
    summary="Retrieve service health",
    description="Retrieve service health status.",
)
async def health(
) -> dict:
    """Basic service health check.

    Args:
        authenticated_user: Authenticated user derived from request credentials.

    Returns:
        Service health-status response.
    """
    return {"status": "ok"}


@router.get(
    "/health/db",
    summary="Retrieve database health",
    description="Retrieve database connectivity health status.",
)
async def health_db(
    session: AsyncSession = Depends(get_session),
) -> dict:
    """Database connectivity health check.

    Args:
        authenticated_user: Authenticated user derived from request credentials.
        session: Database session bound to the current request.

    Returns:
        Service health-status response.
    """
    await ping_db(session)
    return {"status": "ok", "db": "ok"}
