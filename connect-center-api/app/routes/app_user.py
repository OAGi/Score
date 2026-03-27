"""Application user API routes.

Provides read-only endpoints for listing users and retrieving a user by ID.
All endpoints require authentication.
"""

from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, Path, Query, status
from pydantic import ValidationError

from app.deps import get_app_user_service
from app.routes.models.app_user import AppUserEntry, GetAppUserByAppUserIdResponse, GetAppUserListResponse
from app.services.app_user_service import AppUserService
from app.services.models.app_user import AppUserServiceResult
from app.services.models.app_user import Role
from app.types.identifiers import AppUserId

router = APIRouter(tags=["app-user"])


@router.get(
    "/accounts",
    summary="List accounts",
    description="Retrieve a paginated list of accounts.",
    response_model=GetAppUserListResponse,
)
async def get_app_user_list(
        login_id: str | None = Query(default=None, description="Filter by login ID (partial match)."),
        username: str | None = Query(default=None, description="Filter by user name (partial match)."),
        organization: str | None = Query(default=None, description="Filter by organization (partial match)."),
        email: str | None = Query(default=None, description="Filter by email (partial match)."),
        is_admin: bool | None = Query(default=None, description="Filter by admin flag."),
        is_developer: bool | None = Query(default=None, description="Filter by developer flag."),
        is_enabled: bool | None = Query(default=None, description="Filter by enabled flag."),
        order_by: str | None = Query(
            default=None,
            description=(
                    "Comma-separated list of properties to order results by. Prefix with '-' for descending, '+' for ascending. "
                    "Allowed columns: login_id, username, organization, email, is_admin, is_developer, is_enabled."
            ),
        ),
        offset: int = Query(default=0, ge=0, description="The offset from the beginning of the list."),
        limit: int = Query(default=10, ge=1, le=100, description="The maximum number of items to return."),
        app_user_service: AppUserService = Depends(get_app_user_service),
) -> GetAppUserListResponse:
    """Return a paginated list of application users.

    Args:
        login_id: Optional login ID filter.
        username: Optional username filter.
        organization: Optional organization filter.
        email: Optional email filter.
        is_admin: Optional administrator-role filter.
        is_developer: Optional developer-role filter.
        is_enabled: Optional enabled-status filter.
        order_by: Sort expression used for ordering the result set.
        offset: Zero-based index of the first item to include in the page.
        limit: Maximum number of items to return.
        app_user_service: App-user service dependency.

    Returns:
        Paginated response containing matching resources.
    """
    try:
        page = await app_user_service.list(
            limit=limit,
            offset=offset,
            order_by=order_by,
            login_id=login_id,
            username=username,
            organization=organization,
            email=email,
            is_admin=is_admin,
            is_developer=is_developer,
            is_enabled=is_enabled,
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"message": "The request is invalid. Check the parameters and try again.", "cause": str(e)},
        )

    try:
        return GetAppUserListResponse(
            total_items=page.total,
            offset=offset,
            limit=limit,
            items=[_to_app_user_entry(x) for x in page.items],
        )
    except ValidationError as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={
                "message": "Failed to serialize account data.",
                "cause": str(e),
            },
        )


@router.get(
    "/accounts/{app_user_id}",
    summary="Retrieve an account",
    description="Retrieve an account by ID.",
    response_model=GetAppUserByAppUserIdResponse,
)
async def get_app_user_by_app_user_id(
        app_user_id: AppUserId = Path(..., description="ID of the app user to retrieve."),
        app_user_service: AppUserService = Depends(get_app_user_service),
) -> GetAppUserByAppUserIdResponse:
    """Return an application user by ID.

    Args:
        app_user_id: Application user identifier.
        app_user_service: App-user service dependency.

    Returns:
        Response payload for the requested resource.
    """
    user = await app_user_service.get(app_user_id)
    if user is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "message": "The app user was not found.",
                "cause": f"No app user exists with ID {app_user_id}.",
            },
        )
    try:
        return GetAppUserByAppUserIdResponse.model_validate(_to_app_user_entry(user))
    except ValidationError as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={
                "message": "Failed to serialize account data.",
                "cause": str(e),
            },
        )


@router.get(
    "/me",
    summary="Retrieve current authenticated account",
    description="Retrieve the current authenticated account.",
    response_model=AppUserEntry,
)
async def who_am_i(
        app_user_service: AppUserService = Depends(get_app_user_service),
) -> GetAppUserByAppUserIdResponse:
    """Return the currently authenticated application user profile.

    Args:
        app_user_service: App-user service dependency.

    Returns:
        Response payload for the authenticated user.
    """
    current_user = await app_user_service.get_current_user()
    if current_user is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "message": "The app user was not found.",
                "cause": "The current authenticated app user does not exist.",
            },
        )
    return GetAppUserByAppUserIdResponse.model_validate(_to_app_user_entry(current_user))


def _to_app_user_entry(user: AppUserServiceResult) -> AppUserEntry:
    """Convert a service-layer app-user model to route response model.

    Args:
        user: Service-layer application user model.

    Returns:
        Serialized API entry used by list/detail account responses.
    """
    return AppUserEntry(
        app_user_id=user.app_user_id,
        login_id=user.login_id,
        username=user.username,
        roles=_build_roles(is_admin=user.is_admin, is_developer=user.is_developer),
        organization=user.organization,
        email=user.email,
        email_verified=user.email_verified,
        email_verified_timestamp=user.email_verified_timestamp,
        is_enabled=user.is_enabled,
    )


def _build_roles(*, is_admin: bool, is_developer: bool) -> list[Role]:
    """Build API roles from internal admin/developer flags.

    Args:
        is_admin: Whether the account has administrator privileges.
        is_developer: Whether the account has developer privileges.

    Returns:
        Ordered list of API roles. Returns ``["End-User"]`` when no elevated
        role flag is set.
    """
    roles: list[Role] = []
    if is_admin:
        roles.append("Admin")
    if is_developer:
        roles.append("Developer")
    else:
        roles.append("End-User")
    return roles
