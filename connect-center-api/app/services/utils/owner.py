"""Owner filter parsing helpers."""

from __future__ import annotations


def parse_owner_filter(owner: str | None) -> tuple[list[str] | None, list[str] | None]:
    """Parse comma-separated owner login IDs into include and exclude lists."""
    if owner is None or not owner.strip():
        return None, None

    include: list[str] = []
    exclude: list[str] = []
    for raw_token in owner.split(","):
        token = raw_token.strip()
        if not token:
            raise ValueError("The owner filter contains an empty login ID.")

        is_exclusion = token.startswith("!")
        login_id = token[1:].strip() if is_exclusion else token
        if not login_id:
            raise ValueError("The owner filter contains an empty excluded login ID.")
        if "!" in login_id or "," in login_id:
            raise ValueError("Owner login IDs cannot contain '!' or ','.")

        target = exclude if is_exclusion else include
        if login_id not in target:
            target.append(login_id)

    return include or None, exclude or None
