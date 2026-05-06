"""Repository contract for Data Type persistence.

Defines the async interface for listing and retrieving data types and their
supplementary components.
"""


from __future__ import annotations

from datetime import datetime
from typing import Protocol, Literal

from app.repositories.models.data_type import DataTypePrimitiveRow, DataTypeRow
from app.types.identifiers import AppUserId, DataTypeManifestId, DataTypeSupplementaryComponentManifestId, ReleaseId


class DataTypeRepositoryContract(Protocol):
    """Protocol for data type repository implementations."""

    async def list(
        self,
        release_id: ReleaseId,
        dependent_release_ids: list[ReleaseId],
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        den: str | None = None,
        representation_term: str | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
        included_owner_login_ids: list[str] | None = None,
        excluded_owner_login_ids: list[str] | None = None,
    ) -> tuple[int, list[DataTypeRow]]:
        """Repository contract for list.

        Args:
            release_id: Release identifier used to scope the query.
            dependent_release_ids: Value for `dependent_release_ids`.
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            den: Optional Dictionary Entry Name (DEN) filter.
            representation_term: Value for `representation_term`.
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

    async def get(self, dt_manifest_id: DataTypeManifestId) -> DataTypeRow | None:
        """Repository contract for get.

        Args:
            dt_manifest_id: Data type manifest identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def create_dt(
        self,
        *,
        release_id: ReleaseId,
        based_dt_manifest_id: DataTypeManifestId,
        requester_user_id: AppUserId,
        tag_id: list[int] | None = None,
    ) -> DataTypeManifestId:
        """Create a new DT."""
        pass

    async def update_dt(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        qualifier: str | None,
        qualifier_set: bool,
        six_digit_id: str | None,
        six_digit_id_set: bool,
        deprecated: bool | None,
        deprecated_set: bool,
        namespace_id: int | None,
        namespace_id_set: bool,
        content_component_definition: str | None,
        content_component_definition_set: bool,
        definition: str | None,
        definition_set: bool,
        definition_source: str | None,
        definition_source_set: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update mutable DT fields."""
        pass

    async def update_dt_base(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        based_dt_manifest_id: DataTypeManifestId | None,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update the base DT manifest link for a DT."""
        pass

    async def transfer_dt_ownership(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        requester_user_id: AppUserId,
        target_user_id: AppUserId,
    ) -> bool:
        """Transfer DT ownership."""
        pass

    async def create_dt_sc(
        self,
        *,
        owner_dt_manifest_id: DataTypeManifestId,
        requester_user_id: AppUserId,
    ) -> DataTypeSupplementaryComponentManifestId:
        """Create a blank DT_SC under a DT."""
        pass

    async def create_dt_sc_from_base(
        self,
        *,
        owner_dt_manifest_id: DataTypeManifestId,
        based_dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
        requester_user_id: AppUserId,
    ) -> DataTypeSupplementaryComponentManifestId:
        """Create a DT_SC under a DT by cloning a base DT_SC."""
        pass

    async def update_dt_sc(
        self,
        *,
        dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
        property_term: str | None,
        property_term_set: bool,
        representation_term: str | None,
        representation_term_set: bool,
        cardinality_min: int | None,
        cardinality_min_set: bool,
        cardinality_max: int | None,
        cardinality_max_set: bool,
        deprecated: bool | None,
        deprecated_set: bool,
        default_value: str | None,
        default_value_set: bool,
        fixed_value: str | None,
        fixed_value_set: bool,
        definition: str | None,
        definition_set: bool,
        definition_source: str | None,
        definition_source_set: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update mutable DT_SC fields."""
        pass

    async def delete_dt_sc(
        self,
        *,
        dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
        requester_user_id: AppUserId,
    ) -> bool:
        """Delete a DT_SC permanently from its owning DT."""
        pass

    async def add_dt_tags(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        tag_id: list[int],
        requester_user_id: AppUserId,
    ) -> bool:
        """Attach tags to a DT."""
        pass

    async def remove_dt_tags(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        tag_id: list[int],
        requester_user_id: AppUserId,
    ) -> bool:
        """Detach tags from a DT."""
        pass

    async def change_dt_default_primitive(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        xbt_manifest_id: int | None,
        code_list_manifest_id: int | None,
        agency_id_list_manifest_id: int | None,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update the default primitive selection for a DT."""
        pass

    async def replace_dt_primitives(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        primitives: list[DataTypePrimitiveRow],
        requester_user_id: AppUserId,
    ) -> bool:
        """Replace the DT primitive rows with the provided desired set."""
        pass

    async def change_dt_sc_default_primitive(
        self,
        *,
        dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
        xbt_manifest_id: int | None,
        code_list_manifest_id: int | None,
        agency_id_list_manifest_id: int | None,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update the default primitive selection for a DT_SC."""
        pass

    async def replace_dt_sc_primitives(
        self,
        *,
        dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
        primitives: list[DataTypePrimitiveRow],
        requester_user_id: AppUserId,
    ) -> bool:
        """Replace the DT_SC primitive rows with the provided desired set."""
        pass

    async def change_dt_state(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        state: str,
        restore_owner: bool,
        implicit_move: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Update DT lifecycle state."""
        pass

    async def revise_dt(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
        requester_user_id: AppUserId,
    ) -> bool:
        """Create a revised DT working copy."""
        pass

    async def cancel_dt(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
    ) -> bool:
        """Cancel the active DT revision and restore the previous stable revision."""
        pass

    async def discard_dt(
        self,
        *,
        dt_manifest_id: DataTypeManifestId,
    ) -> bool:
        """Discard a Deleted DT permanently."""
        pass

    async def has_deriving_dts(self, dt_manifest_id: DataTypeManifestId) -> bool:
        """Return whether any DT currently derives from the target DT manifest."""
        pass

    async def has_related_bccps_for_dt(self, dt_manifest_id: DataTypeManifestId) -> bool:
        """Return whether any BCCP currently references the target DT manifest."""
        pass

    async def get_owner_dt_manifest_id_by_dt_sc(
        self,
        dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
    ) -> DataTypeManifestId | None:
        """Return the owning DT manifest identifier for a DT_SC manifest."""
        pass

    async def get_based_dt_sc_manifest_id(
        self,
        dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
    ) -> DataTypeSupplementaryComponentManifestId | None:
        """Return the base DT_SC manifest identifier for a DT_SC manifest."""
        pass

    async def list_direct_inherited_dt_manifest_ids(
        self,
        based_dt_manifest_id: DataTypeManifestId,
    ) -> list[DataTypeManifestId]:
        """Return direct child DT manifests that derive from the provided DT manifest."""
        pass

    async def list_direct_inherited_dt_sc_manifest_ids(
        self,
        based_dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
    ) -> list[DataTypeSupplementaryComponentManifestId]:
        """Return direct child DT_SC manifests that derive from the provided DT_SC manifest."""
        pass

    async def count_bbie_sc_refs(
        self,
        dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
    ) -> int:
        """Return the number of BBIE_SC rows that reference the DT_SC manifest."""
        pass
