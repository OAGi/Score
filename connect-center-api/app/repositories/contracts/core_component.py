"""Repository contract for Core Component persistence.

Defines the async interface for listing, creating, and retrieving ACC, ASCCP,
and BCCP records and their relationships.
"""


from __future__ import annotations

from datetime import datetime
from typing import Protocol, Literal

from app.repositories.models.core_component import (
    AsccRelationshipInfoRow,
    BccRelationshipInfoRow,
    CoreComponentListRow,
    GetAccRow,
    GetAsccpRow,
    GetBccpRow,
)
from app.types.identifiers import (
    AccManifestId,
    AppUserId,
    AsccManifestId,
    AsccpManifestId,
    BccManifestId,
    BccpManifestId,
    DataTypeManifestId,
    NamespaceId,
    ReleaseId,
)


class CoreComponentRepositoryContract(Protocol):
    """Protocol for core component repository implementations."""

    async def create_acc(
        self,
        *,
        release_id: ReleaseId,
        based_acc_manifest_id: AccManifestId | None,
        object_class_term: str,
        oagis_component_type: int,
        acc_type: str,
        definition: str | None,
        namespace_id: NamespaceId | None,
        tag_id: list[int] | None,
        requester_user_id: AppUserId,
    ) -> AccManifestId:
        """Repository contract for create ACC.

        Args:
            release_id: Target release identifier.
            based_acc_manifest_id: Optional base ACC manifest identifier from the same release.
            object_class_term: Initial ACC object class term.
            oagis_component_type: Initial OAGIS component type numeric value.
            acc_type: Initial ACC type string.
            definition: Optional definition text.
            namespace_id: Optional namespace identifier from the target release library.
            tag_id: Optional tag identifier list to attach after create.
            requester_user_id: Requesting user identifier for audit fields.

        Returns:
            Result of the operation.
        """
        pass

    async def create_ascc(
        self,
        *,
        acc_manifest_id: AccManifestId,
        asccp_manifest_id: AsccpManifestId,
        index: int,
        cardinality_min: int,
        cardinality_max: int,
        definition: str | None,
        definition_source: str | None,
        requester_user_id: AppUserId,
    ) -> AsccManifestId:
        """Repository contract for appending an ASCC to an ACC."""
        pass

    async def create_bcc(
        self,
        *,
        acc_manifest_id: AccManifestId,
        bccp_manifest_id: BccpManifestId,
        index: int,
        entity_type: Literal["Attribute", "Element"],
        cardinality_min: int,
        cardinality_max: int,
        definition: str | None,
        definition_source: str | None,
        is_nillable: bool,
        default_value: str | None,
        fixed_value: str | None,
        requester_user_id: AppUserId,
    ) -> BccManifestId:
        """Repository contract for appending a BCC to an ACC."""
        pass

    async def remove_ascc(
        self,
        *,
        ascc_manifest_id: AsccManifestId,
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for removing an ASCC relationship from its ACC."""
        pass

    async def remove_bcc(
        self,
        *,
        bcc_manifest_id: BccManifestId,
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for removing a BCC relationship from its ACC."""
        pass

    async def count_bie_references_by_ascc_manifest(
        self,
        ascc_manifest_id: AsccManifestId,
    ) -> int:
        """Count BIE references to the given ASCC manifest."""
        pass

    async def count_bie_references_by_bcc_manifest(
        self,
        bcc_manifest_id: BccManifestId,
    ) -> int:
        """Count BIE references to the given BCC manifest."""
        pass

    async def create_asccp(
        self,
        *,
        release_id: ReleaseId,
        role_of_acc_manifest_id: AccManifestId,
        property_term: str,
        asccp_type: str,
        reusable_indicator: bool,
        namespace_id: NamespaceId | None,
        definition: str | None,
        definition_source: str | None,
        requester_user_id: AppUserId,
    ) -> AsccpManifestId:
        """Repository contract for creating an ASCCP."""
        pass

    async def create_bccp(
        self,
        *,
        release_id: ReleaseId,
        bdt_manifest_id: DataTypeManifestId,
        property_term: str,
        requester_user_id: AppUserId,
    ) -> BccpManifestId:
        """Repository contract for creating a BCCP."""
        pass

    async def move_acc_sequence(
        self,
        *,
        acc_manifest_id: AccManifestId,
        item_ascc_manifest_id: AsccManifestId | None,
        item_bcc_manifest_id: BccManifestId | None,
        after_ascc_manifest_id: AsccManifestId | None,
        after_bcc_manifest_id: BccManifestId | None,
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for moving an ACC child within `seq_key` order."""
        pass

    async def get_owner_acc_manifest_id_by_ascc_manifest(
        self,
        ascc_manifest_id: AsccManifestId,
    ) -> AccManifestId | None:
        """Resolve the owning ACC manifest identifier for an ASCC manifest."""
        pass

    async def get_owner_acc_manifest_id_by_bcc_manifest(
        self,
        bcc_manifest_id: BccManifestId,
    ) -> AccManifestId | None:
        """Resolve the owning ACC manifest identifier for a BCC manifest."""
        pass

    async def list_owner_acc_manifest_ids_by_asccp_manifest(
        self,
        asccp_manifest_id: AsccpManifestId,
    ) -> list[AccManifestId]:
        """List ACC manifests that currently reference the given ASCCP manifest."""
        pass

    async def list_owner_acc_manifest_ids_by_bccp_manifest(
        self,
        bccp_manifest_id: BccpManifestId,
    ) -> list[AccManifestId]:
        """List ACC manifests that currently reference the given BCCP manifest."""
        pass

    async def update_acc(
        self,
        *,
        acc_manifest_id: AccManifestId,
        object_class_term: str | None,
        object_class_term_set: bool,
        oagis_component_type: int | None,
        oagis_component_type_set: bool,
        acc_type: str | None,
        acc_type_set: bool,
        definition: str | None,
        definition_set: bool,
        definition_source: str | None,
        definition_source_set: bool,
        is_abstract: bool | None,
        is_abstract_set: bool,
        deprecated: bool | None,
        deprecated_set: bool,
        namespace_id: NamespaceId | None,
        namespace_id_set: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for updating mutable ACC fields."""
        pass

    async def transfer_acc_ownership(
        self,
        *,
        acc_manifest_id: AccManifestId,
        requester_user_id: AppUserId,
        target_user_id: AppUserId,
    ) -> bool:
        """Repository contract for transferring ACC ownership."""
        pass

    async def update_ascc(
        self,
        *,
        ascc_manifest_id: AsccManifestId,
        cardinality_min: int | None,
        cardinality_min_set: bool,
        cardinality_max: int | None,
        cardinality_max_set: bool,
        deprecated: bool | None,
        deprecated_set: bool,
        definition: str | None,
        definition_set: bool,
        definition_source: str | None,
        definition_source_set: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for updating mutable ASCC fields."""
        pass

    async def update_bcc(
        self,
        *,
        bcc_manifest_id: BccManifestId,
        entity_type: Literal["Attribute", "Element"] | None,
        entity_type_set: bool,
        cardinality_min: int | None,
        cardinality_min_set: bool,
        cardinality_max: int | None,
        cardinality_max_set: bool,
        deprecated: bool | None,
        deprecated_set: bool,
        is_nillable: bool | None,
        is_nillable_set: bool,
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
        """Repository contract for updating mutable BCC fields."""
        pass

    async def transfer_asccp_ownership(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        requester_user_id: AppUserId,
        target_user_id: AppUserId,
    ) -> bool:
        """Repository contract for transferring ASCCP ownership."""
        pass

    async def transfer_bccp_ownership(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        requester_user_id: AppUserId,
        target_user_id: AppUserId,
    ) -> bool:
        """Repository contract for transferring BCCP ownership."""
        pass

    async def update_acc_base(
        self,
        *,
        acc_manifest_id: AccManifestId,
        based_acc_manifest_id: AccManifestId | None,
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for setting or unsetting an ACC base manifest."""
        pass

    async def update_asccp(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        property_term: str | None,
        property_term_set: bool,
        reusable_indicator: bool | None,
        reusable_indicator_set: bool,
        deprecated: bool | None,
        deprecated_set: bool,
        is_nillable: bool | None,
        is_nillable_set: bool,
        namespace_id: NamespaceId | None,
        namespace_id_set: bool,
        definition: str | None,
        definition_set: bool,
        definition_source: str | None,
        definition_source_set: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for updating mutable ASCCP fields."""
        pass

    async def update_bccp(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        property_term: str | None,
        property_term_set: bool,
        deprecated: bool | None,
        deprecated_set: bool,
        is_nillable: bool | None,
        is_nillable_set: bool,
        namespace_id: NamespaceId | None,
        namespace_id_set: bool,
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
        """Repository contract for updating mutable BCCP fields."""
        pass

    async def change_asccp_role_of_acc(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        role_of_acc_manifest_id: AccManifestId,
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for changing the role ACC of an ASCCP."""
        pass

    async def change_bccp_bdt(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        bdt_manifest_id: DataTypeManifestId,
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for changing the BDT of a BCCP."""
        pass

    async def add_acc_tags(
        self,
        *,
        acc_manifest_id: AccManifestId,
        tag_id: list[int],
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for attaching tag links to an ACC manifest."""
        pass

    async def remove_acc_tags(
        self,
        *,
        acc_manifest_id: AccManifestId,
        tag_id: list[int],
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for removing tag links from an ACC manifest."""
        pass

    async def add_asccp_tags(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        tag_id: list[int],
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for attaching tag links to an ASCCP manifest."""
        pass

    async def remove_asccp_tags(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        tag_id: list[int],
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for removing tag links from an ASCCP manifest."""
        pass

    async def add_bccp_tags(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        tag_id: list[int],
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for attaching tag links to a BCCP manifest."""
        pass

    async def remove_bccp_tags(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        tag_id: list[int],
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for removing tag links from a BCCP manifest."""
        pass

    async def change_acc_state(
        self,
        *,
        acc_manifest_id: AccManifestId,
        state: str,
        restore_owner: bool,
        implicit_move: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for ACC lifecycle state transitions."""
        pass

    async def revise_acc(
        self,
        *,
        acc_manifest_id: AccManifestId,
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for creating a revised ACC working copy."""
        pass

    async def change_asccp_state(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        state: str,
        restore_owner: bool,
        implicit_move: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for ASCCP lifecycle state transitions."""
        pass

    async def change_bccp_state(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        state: str,
        restore_owner: bool,
        implicit_move: bool,
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for BCCP lifecycle state transitions."""
        pass

    async def revise_asccp(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for creating a revised ASCCP working copy."""
        pass

    async def cancel_asccp(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
    ) -> bool:
        """Repository contract for cancelling the current ASCCP revision."""
        pass

    async def revise_bccp(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
        requester_user_id: AppUserId,
    ) -> bool:
        """Repository contract for creating a revised BCCP working copy."""
        pass

    async def cancel_bccp(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
    ) -> bool:
        """Repository contract for cancelling the current BCCP revision."""
        pass

    async def cancel_acc(
        self,
        *,
        acc_manifest_id: AccManifestId,
    ) -> bool:
        """Repository contract for cancelling the current ACC revision and restoring the stable one."""
        pass

    async def has_related_asccps_for_acc(
        self,
        acc_manifest_id: AccManifestId,
    ) -> bool:
        """Return whether the ACC has ASCCPs that still reference it as role-of ACC."""
        pass

    async def has_deriving_accs(
        self,
        acc_manifest_id: AccManifestId,
    ) -> bool:
        """Return whether the ACC still has derived ACC manifests."""
        pass

    async def has_related_asccs_for_asccp(
        self,
        asccp_manifest_id: AsccpManifestId,
    ) -> bool:
        """Return whether the ASCCP still has ASCC relationships."""
        pass

    async def has_related_bccs_for_bccp(
        self,
        bccp_manifest_id: BccpManifestId,
    ) -> bool:
        """Return whether the BCCP still has BCC relationships."""
        pass

    async def discard_acc(
        self,
        *,
        acc_manifest_id: AccManifestId,
    ) -> bool:
        """Repository contract for permanently deleting an ACC and its related rows."""
        pass

    async def discard_asccp(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
    ) -> bool:
        """Repository contract for permanently deleting an ASCCP and its related rows."""
        pass

    async def discard_bccp(
        self,
        *,
        bccp_manifest_id: BccpManifestId,
    ) -> bool:
        """Repository contract for permanently deleting a BCCP and its related rows."""
        pass

    async def list(
        self,
        release_id: ReleaseId,
        dependent_release_ids: list[ReleaseId],
        types: list[str],
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        den: str | None = None,
        tag: str | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
    ) -> tuple[int, list[CoreComponentListRow]]:
        """Repository contract for list.

        Args:
            release_id: Release identifier used to scope the query.
            dependent_release_ids: Value for `dependent_release_ids`.
            types: Optional component type filter list.
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            den: Optional Dictionary Entry Name (DEN) filter.
            tag: Optional tag filter.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

        Returns:
            Result of the operation.
        """
        pass

    async def get_acc(self, acc_manifest_id: AccManifestId) -> GetAccRow | None:
        """Repository contract for get acc.

        Args:
            acc_manifest_id: ACC manifest identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def get_acc_relationships(
        self,
        acc_manifest_id: AccManifestId,
    ) -> list[AsccRelationshipInfoRow | BccRelationshipInfoRow]:
        """Repository contract for get acc relationships.

        Args:
            acc_manifest_id: ACC manifest identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def get_asccp(self, asccp_manifest_id: AsccpManifestId) -> GetAsccpRow | None:
        """Repository contract for get asccp.

        Args:
            asccp_manifest_id: ASCCP manifest identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def get_bccp(self, bccp_manifest_id: BccpManifestId) -> GetBccpRow | None:
        """Repository contract for get bccp.

        Args:
            bccp_manifest_id: BCCP manifest identifier.

        Returns:
            Result of the operation.
        """
        pass
