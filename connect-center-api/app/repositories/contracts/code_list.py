"""Repository contract for Code List persistence."""


from __future__ import annotations

from datetime import datetime
from typing import Protocol, Literal

from app.repositories.models.code_list import CodeListRow
from app.types.identifiers import AppUserId, CodeListManifestId, CodeListValueManifestId, ReleaseId


class CodeListRepositoryContract(Protocol):
    """Protocol for code list repository implementations."""

    async def list(
        self,
        release_id: ReleaseId,
        dependent_release_ids: list[ReleaseId],
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        name: str | None = None,
        list_id: str | None = None,
        version_id: str | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
        included_owner_login_ids: list[str] | None = None,
        excluded_owner_login_ids: list[str] | None = None,
    ) -> tuple[int, list[CodeListRow]]:
        """Repository contract for list.

        Args:
            release_id: Release identifier used to scope the query.
            dependent_release_ids: Value for `dependent_release_ids`.
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            name: Optional name filter.
            list_id: Value for `list_id`.
            version_id: Value for `version_id`.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.
            included_owner_login_ids: Optional owner login IDs to include by exact match.
            excluded_owner_login_ids: Optional owner login IDs to exclude by exact match.

        Returns:
            Result of the operation.
        """
        pass

    async def get(self, code_list_manifest_id: CodeListManifestId) -> CodeListRow | None:
        """Repository contract for get.

        Args:
            code_list_manifest_id: Code list manifest identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def get_code_list_manifest_id_by_value_manifest_id(
        self,
        code_list_value_manifest_id: CodeListValueManifestId,
    ) -> CodeListManifestId | None:
        """Resolve the owning code list manifest identifier from a code list value manifest identifier."""
        pass

    async def create_code_list(
        self,
        *,
        release_id: ReleaseId,
        name: str,
        based_code_list_manifest_id: CodeListManifestId | None,
        version_id: str | None,
        list_id: str | None,
        agency_id_list_value_manifest_id: int | None,
        definition: str | None,
        definition_source: str | None,
        remark: str | None,
        namespace_id: int | None,
        deprecated: bool | None,
        extensible_indicator: bool | None,
        requester_user_id: AppUserId,
        requester_is_developer: bool,
    ) -> CodeListManifestId:
        """Create a new code list."""
        pass

    async def update_code_list(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
        name: str | None,
        name_set: bool,
        version_id: str | None,
        version_id_set: bool,
        list_id: str | None,
        list_id_set: bool,
        agency_id_list_value_manifest_id: int | None,
        agency_id_list_value_manifest_id_set: bool,
        definition: str | None,
        definition_set: bool,
        definition_source: str | None,
        definition_source_set: bool,
        remark: str | None,
        remark_set: bool,
        namespace_id: int | None,
        namespace_id_set: bool,
        deprecated: bool | None,
        deprecated_set: bool,
        extensible_indicator: bool | None,
        extensible_indicator_set: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update mutable code list fields."""
        pass

    async def create_code_list_value(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
        value: str,
        meaning: str | None,
        definition: str | None,
        definition_source: str | None,
        deprecated: bool,
        requester_user_id: AppUserId,
    ) -> CodeListValueManifestId:
        """Create a new code list value under a manifest."""
        pass

    async def update_code_list_value(
        self,
        *,
        code_list_value_manifest_id: CodeListValueManifestId,
        value: str | None,
        value_set: bool,
        meaning: str | None,
        meaning_set: bool,
        definition: str | None,
        definition_set: bool,
        definition_source: str | None,
        definition_source_set: bool,
        deprecated: bool | None,
        deprecated_set: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update a mutable code list value."""
        pass

    async def delete_code_list_value(
        self,
        *,
        code_list_value_manifest_id: CodeListValueManifestId,
    ) -> bool:
        """Delete a code list value and its manifest row."""
        pass

    async def transfer_code_list_ownership(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
        requester_user_id: AppUserId,
        target_user_id: AppUserId,
    ) -> bool:
        """Transfer code list ownership to another user."""
        pass

    async def change_code_list_state(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
        state: str,
        restore_owner: bool,
        implicit_move: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update the lifecycle state of a code list."""
        pass

    async def revise_code_list(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
        requester_user_id: AppUserId,
    ) -> bool:
        """Create a revised code list working copy."""
        pass

    async def cancel_code_list(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
    ) -> bool:
        """Cancel the current code list revision and restore the previous stable revision."""
        pass

    async def discard_code_list(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
    ) -> bool:
        """Discard a Deleted code list permanently."""
        pass

    async def append_code_list_log(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
        requester_user_id: AppUserId,
        action: str,
    ) -> int:
        """Append a new log row and update the manifest head."""
        pass

    async def revert_code_list_log_to_stable_state(
        self,
        *,
        code_list_manifest_id: CodeListManifestId,
    ) -> int:
        """Revert the latest revised code list logs and restore the stable log head."""
        pass
