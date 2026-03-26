"""Service layer package.

Services coordinate request validation, business rules, and repository calls.
"""


from __future__ import annotations

from collections.abc import Mapping, Sequence
from typing import TYPE_CHECKING

from app.repositories.models.app_user import AppUserRow
from app.services.models.app_user import UserSummary, Role

if TYPE_CHECKING:
    from app.repositories.contracts.app_user import AppUserRepositoryContract


def user_roles(*, is_admin: bool, is_developer: bool) -> list[Role]:
    """Build role list from admin/developer flags.

    Args:
        is_admin: Optional administrator-role filter.
        is_developer: Optional developer-role filter.

    Returns:
        Result of the operation.
    """
    roles: list[Role] = []
    if is_admin:
        roles.append("Admin")
    if is_developer:
        roles.append("Developer")
    else:
        roles.append("End-User")
    return roles


def to_user_summary(user_id: int, *, users_by_id: Mapping[int, AppUserRow]) -> UserSummary:
    """Convert an AppUserRow map entry into a UserSummary.

    Args:
        user_id: User identifier.
        users_by_id: Preloaded users keyed by user identifier.

    Returns:
        Result of the operation.
    """
    record = users_by_id.get(user_id)
    if record is None:
        fallback = str(user_id)
        return UserSummary(user_id=user_id, login_id=fallback, username=fallback, roles=["End-User"])
    login_id = record.login_id
    return UserSummary(
        user_id=record.app_user_id,
        login_id=login_id,
        username=record.name or login_id,
        roles=user_roles(is_admin=record.is_admin, is_developer=bool(record.is_developer)),
    )


async def load_users_by_ids(
    account_service_repo: AppUserRepositoryContract | None,
    user_ids: Sequence[int],
) -> dict[int, AppUserRow]:
    """Batch load users by IDs into an integer-keyed mapping.

    Args:
        account_service_repo: Account repository used to resolve user summaries.
        user_ids: Value for `user_ids`.

    Returns:
        Result of the operation.
    """
    if account_service_repo is None:
        return {}
    users = await account_service_repo.gets(list(user_ids))
    return {int(user.app_user_id): user for user in users}
