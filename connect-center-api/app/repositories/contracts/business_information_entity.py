"""Repository contract for Business Information Entity persistence."""


from __future__ import annotations

from datetime import datetime
from typing import Protocol, Literal

from app.repositories.models.business_information_entity import (
    AsbieCreatePlanRow,
    AsbiepRolePlanRow,
    BbieCreatePlanRow,
    BbiepPlanRow,
    GetAsbieRow,
    GetBbieRow,
    TopLevelAsbiepDetailRow,
    TopLevelAsbiepInfoRow,
    TopLevelAsbiepListRow,
)
from app.repositories.models.release import ReleaseSummaryRow
from app.types.identifiers import (
    AccManifestId,
    AgencyIdListManifestId,
    AppUserId,
    AsccManifestId,
    AsccpManifestId,
    BccManifestId,
    BccpManifestId,
    BizCtxId,
    CodeListManifestId,
    DataTypeSupplementaryComponentManifestId,
    LibraryId,
    ReleaseId,
    XbtManifestId,
)


class BusinessInformationEntityRepositoryContract(Protocol):
    """Protocol for BIE repository implementations."""

    async def list_top_level_asbieps(
        self,
        limit: int,
        offset: int,
        sorts: list[tuple[str, Literal["ASC", "DESC"]]],
        library_id: LibraryId | None = None,
        release_ids: list[ReleaseId] | None = None,
        den: str | None = None,
        version: str | None = None,
        status: str | None = None,
        state: str | None = None,
        is_deprecated: bool | None = None,
        creation_timestamp_before: datetime | None = None,
        creation_timestamp_after: datetime | None = None,
        last_update_timestamp_before: datetime | None = None,
        last_update_timestamp_after: datetime | None = None,
    ) -> tuple[int, list[TopLevelAsbiepListRow]]:
        """Repository contract for list top level asbieps.

        Args:
            limit: Maximum number of records to return.
            offset: Number of records to skip before collecting results.
            library_id: Library identifier used to scope the query.
            release_ids: Release identifiers used to scope the query.
            den: Optional Dictionary Entry Name (DEN) filter.
            version: Optional version filter.
            status: Optional status filter.
            state: Optional lifecycle state filter.
            is_deprecated: Optional deprecation flag filter.
            creation_timestamp_before: Optional upper bound for creation timestamp.
            creation_timestamp_after: Optional lower bound for creation timestamp.
            last_update_timestamp_before: Optional upper bound for last update timestamp.
            last_update_timestamp_after: Optional lower bound for last update timestamp.

        Returns:
            Result of the operation.
        """
        pass

    async def get_top_level_asbiep(self, *, top_level_asbiep_id: int) -> TopLevelAsbiepDetailRow | None:
        """Repository contract for get top level asbiep.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def get_asbie_by_asbie_id(self, *, asbie_id: int) -> GetAsbieRow | None:
        """Repository contract for get asbie by asbie id.

        Args:
            asbie_id: ASBIE identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def get_asbie_by_based_ascc_manifest_id(
        self,
        top_level_asbiep_id: int,
        based_ascc_manifest_id: AsccManifestId,
    ) -> GetAsbieRow | None:
        """Repository contract for get asbie by based ascc manifest id.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            based_ascc_manifest_id: ASCC manifest identifier for relationship lookup.

        Returns:
            Result of the operation.
        """
        pass

    async def get_bbie_by_bbie_id(self, *, bbie_id: int) -> GetBbieRow | None:
        """Repository contract for get bbie by bbie id.

        Args:
            bbie_id: BBIE identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def get_bbie_sc_owner_top_level(self, *, bbie_sc_id: int) -> TopLevelAsbiepInfoRow | None:
        """Repository contract for resolving BBIE_SC owner top-level info.

        Args:
            bbie_sc_id: BBIE_SC identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def get_bbie_by_based_bcc_manifest_id(
        self,
        top_level_asbiep_id: int,
        based_bcc_manifest_id: BccManifestId,
    ) -> GetBbieRow | None:
        """Repository contract for get bbie by based bcc manifest id.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            based_bcc_manifest_id: BCC manifest identifier for relationship lookup.

        Returns:
            Result of the operation.
        """
        pass

    async def create_top_level_asbiep(
        self,
        asccp_manifest_id: AsccpManifestId,
        biz_ctx_ids: list[BizCtxId],
        requester_user_id: AppUserId,
    ) -> int:
        """Create a top-level ASBIEP and return its identifier.

        Args:
            asccp_manifest_id: ASCCP manifest identifier.
            biz_ctx_ids: Business context identifiers to assign.
            requester_user_id: Requesting user identifier.

        Returns:
            Result of the operation.
        """
        pass

    async def get_release_summary_by_asccp_manifest_id(
        self,
        *,
        asccp_manifest_id: AsccpManifestId,
    ) -> ReleaseSummaryRow | None:
        """Resolve the release summary for an ASCCP manifest.

        Args:
            asccp_manifest_id: ASCCP manifest identifier.

        Returns:
            Release summary for the manifest, if found.
        """
        pass

    async def update_top_level_asbiep(
        self,
        top_level_asbiep_id: int,
        requester_user_id: AppUserId,
        is_admin: bool,
        version: str | None = None,
        status: str | None = None,
        display_name: str | None = None,
        biz_term: str | None = None,
        definition: str | None = None,
        remark: str | None = None,
        is_deprecated: bool | None = None,
        deprecated_reason: str | None = None,
        deprecated_remark: str | None = None,
    ) -> list[str]:
        """Update top-level ASBIEP fields.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            requester_user_id: Requesting user identifier.
            is_admin: Whether requester has admin role.
            version: Version value.
            status: Status value.
            display_name: ASBIEP display name.
            biz_term: ASBIEP business term.
            definition: ASBIEP definition.
            remark: ASBIEP remark.
            is_deprecated: Deprecation flag.
            deprecated_reason: Deprecation reason.
            deprecated_remark: Deprecation remark.

        Returns:
            Result of the operation.
        """
        pass

    async def update_top_level_asbiep_state(
        self,
        top_level_asbiep_id: int,
        requester_user_id: AppUserId,
        is_admin: bool,
        new_state: str,
    ) -> tuple[str, str]:
        """Transition top-level ASBIEP state.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            requester_user_id: Requesting user identifier.
            is_admin: Whether requester has admin role.
            new_state: Target state.

        Returns:
            Result of the operation.
        """
        pass

    async def delete_top_level_asbiep(
        self,
        top_level_asbiep_id: int,
        requester_user_id: AppUserId,
        is_admin: bool,
    ) -> None:
        """Delete top-level ASBIEP and owned descendants.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            requester_user_id: Requesting user identifier.
            is_admin: Whether requester has admin role.
        """
        pass

    async def list_reusing_top_level_asbiep_ids(self, *, top_level_asbiep_id: int) -> list[int]:
        """List top-level ASBIEP identifiers that reuse the target top-level ASBIEP.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier being inspected.

        Returns:
            Identifiers of other top-level ASBIEPs whose ASBIEs point at ASBIEPs
            owned by the target top-level ASBIEP.
        """
        pass

    async def list_reused_top_level_asbiep_ids(self, *, top_level_asbiep_id: int) -> list[int]:
        """List top-level ASBIEP identifiers that the target top-level ASBIEP reuses.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier being inspected.

        Returns:
            Identifiers of other top-level ASBIEPs referenced by ASBIEs owned by
            the target top-level ASBIEP.
        """
        pass

    async def list_assigned_code_list_manifest_ids(self, *, top_level_asbiep_id: int) -> list[int]:
        """List code list manifest identifiers assigned anywhere under the target top-level ASBIEP.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier being inspected.

        Returns:
            Distinct code list manifest identifiers assigned by BBIE or BBIE_SC
            rows owned by the target top-level ASBIEP.
        """
        pass

    async def has_top_level_asbiep_openapi_reference(self, *, top_level_asbiep_id: int) -> bool:
        """Return whether an OpenAPI document references the target top-level ASBIEP.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier being inspected.

        Returns:
            ``True`` if an OpenAPI request/response body references the target
            top-level ASBIEP, otherwise ``False``.
        """
        pass

    async def transfer_top_level_asbiep_ownership(
        self,
        top_level_asbiep_id: int,
        requester_user_id: AppUserId,
        is_admin: bool,
        target_user_id: AppUserId,
    ) -> None:
        """Transfer top-level ASBIEP ownership.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            requester_user_id: Requesting user identifier.
            is_admin: Whether requester has admin role.
            target_user_id: Target owner user identifier.
        """
        pass

    async def assign_biz_ctx_to_top_level_asbiep(
        self,
        top_level_asbiep_id: int,
        biz_ctx_id: BizCtxId,
        requester_user_id: AppUserId,
        is_admin: bool,
    ) -> bool:
        """Assign a business context to top-level ASBIEP.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            biz_ctx_id: Business context identifier.
            requester_user_id: Requesting user identifier.
            is_admin: Whether requester has admin role.
        """
        pass

    async def unassign_biz_ctx_from_top_level_asbiep(
        self,
        top_level_asbiep_id: int,
        biz_ctx_id: BizCtxId,
        requester_user_id: AppUserId,
        is_admin: bool,
    ) -> bool:
        """Unassign a business context from top-level ASBIEP.

        Args:
            top_level_asbiep_id: Top-level ASBIEP identifier.
            biz_ctx_id: Business context identifier.
            requester_user_id: Requesting user identifier.
            is_admin: Whether requester has admin role.
        """
        pass

    async def get_asbie_create_plan(self, *, from_abie_id: int, based_ascc_manifest_id: AsccManifestId) -> AsbieCreatePlanRow:
        """Load DB context required to create or enable an ASBIE.

        Args:
            from_abie_id: Parent ABIE identifier.
            based_ascc_manifest_id: ASCC manifest identifier.

        Returns:
            Dictionary containing top-level ownership/state and ASCC/ASCCP metadata.
        """
        pass

    async def get_asbiep_role_plan(self, *, asbiep_id: int) -> AsbiepRolePlanRow | None:
        """Load ASBIEP/role metadata used for compatibility checks.

        Args:
            asbiep_id: ASBIEP identifier.

        Returns:
            Dictionary with target ASCCP/ACC manifest IDs or None if not found.
        """
        pass

    async def create_abie(
        self,
        based_acc_manifest_id: AccManifestId,
        path: str,
        hash_path: str,
        owner_top_level_asbiep_id: int,
        requester_user_id: AppUserId,
        now: datetime,
    ) -> int:
        """Create an ABIE row.

        Args:
            based_acc_manifest_id: Based ACC manifest identifier.
            path: ABIE path value.
            hash_path: ABIE hash path value.
            owner_top_level_asbiep_id: Owner top-level ASBIEP identifier.
            requester_user_id: Requesting user identifier.
            now: Timestamp used for create/update fields.

        Returns:
            Created ABIE identifier.
        """
        pass

    async def create_asbiep(
        self,
        based_asccp_manifest_id: AsccpManifestId,
        role_of_abie_id: int,
        path: str,
        hash_path: str,
        owner_top_level_asbiep_id: int,
        requester_user_id: AppUserId,
        now: datetime,
    ) -> int:
        """Create an ASBIEP row.

        Args:
            based_asccp_manifest_id: Based ASCCP manifest identifier.
            role_of_abie_id: Role ABIE identifier.
            path: ASBIEP path value.
            hash_path: ASBIEP hash path value.
            owner_top_level_asbiep_id: Owner top-level ASBIEP identifier.
            requester_user_id: Requesting user identifier.
            now: Timestamp used for create/update fields.

        Returns:
            Created ASBIEP identifier.
        """
        pass

    async def create_asbie_record(
        self,
        based_ascc_manifest_id: AsccManifestId,
        path: str,
        hash_path: str,
        from_abie_id: int,
        to_asbiep_id: int,
        cardinality_min: int,
        cardinality_max: int,
        owner_top_level_asbiep_id: int,
        requester_user_id: AppUserId,
        now: datetime,
    ) -> int:
        """Create an ASBIE row.

        Args:
            based_ascc_manifest_id: Based ASCC manifest identifier.
            path: ASBIE path value.
            hash_path: ASBIE hash path value.
            from_abie_id: Parent ABIE identifier.
            to_asbiep_id: Target ASBIEP identifier.
            cardinality_min: Minimum cardinality value.
            cardinality_max: Maximum cardinality value.
            owner_top_level_asbiep_id: Owner top-level ASBIEP identifier.
            requester_user_id: Requesting user identifier.
            now: Timestamp used for create/update fields.

        Returns:
            Created ASBIE identifier.
        """
        pass

    async def activate_existing_asbie(
        self,
        asbie_id: int,
        requester_user_id: AppUserId,
        now: datetime,
        to_asbiep_id: int | None = None,
    ) -> None:
        """Enable an existing ASBIE and optionally rewire target ASBIEP.

        Args:
            asbie_id: ASBIE identifier.
            requester_user_id: Requesting user identifier.
            now: Timestamp used for update fields.
            to_asbiep_id: Optional replacement target ASBIEP identifier.
        """
        pass

    async def update_asbie(
        self,
        asbie_id: int,
        requester_user_id: AppUserId,
        is_admin: bool,
        is_used: bool | None = None,
        is_nillable: bool | None = None,
        definition: str | None = None,
        cardinality_min: int | None = None,
        cardinality_max: int | None = None,
        remark: str | None = None,
    ) -> list[str]:
        """Update ASBIE fields and return updated field names."""
        pass

    async def get_bbie_create_plan(self, *, from_abie_id: int, based_bcc_manifest_id: BccManifestId) -> BbieCreatePlanRow:
        """Load DB context required to create or enable a BBIE.

        Args:
            from_abie_id: Parent ABIE identifier.
            based_bcc_manifest_id: BCC manifest identifier.

        Returns:
            Dictionary containing top-level ownership/state and BCC/BCCP metadata.
        """
        pass

    async def get_bbiep_plan(self, *, bbiep_id: int) -> BbiepPlanRow | None:
        """Load BBIEP metadata used for compatibility checks.

        Args:
            bbiep_id: BBIEP identifier.

        Returns:
            Dictionary with target BCCP manifest ID or None if not found.
        """
        pass

    async def create_bbiep(
        self,
        based_bccp_manifest_id: BccpManifestId,
        path: str,
        hash_path: str,
        owner_top_level_asbiep_id: int,
        requester_user_id: AppUserId,
        now: datetime,
    ) -> int:
        """Create a BBIEP row.

        Args:
            based_bccp_manifest_id: Based BCCP manifest identifier.
            path: BBIEP path value.
            hash_path: BBIEP hash path value.
            owner_top_level_asbiep_id: Owner top-level ASBIEP identifier.
            requester_user_id: Requesting user identifier.
            now: Timestamp used for create/update fields.

        Returns:
            Created BBIEP identifier.
        """
        pass

    async def create_bbie_record(
        self,
        based_bcc_manifest_id: BccManifestId,
        path: str,
        hash_path: str,
        from_abie_id: int,
        to_bbiep_id: int,
        cardinality_min: int,
        cardinality_max: int,
        is_nillable: bool,
        default_value: str | None,
        fixed_value: str | None,
        xbt_manifest_id: XbtManifestId | None,
        code_list_manifest_id: CodeListManifestId | None,
        agency_id_list_manifest_id: AgencyIdListManifestId | None,
        owner_top_level_asbiep_id: int,
        requester_user_id: AppUserId,
        now: datetime,
    ) -> int:
        """Create a BBIE row.

        Args:
            based_bcc_manifest_id: Based BCC manifest identifier.
            path: BBIE path value.
            hash_path: BBIE hash path value.
            from_abie_id: Parent ABIE identifier.
            to_bbiep_id: Target BBIEP identifier.
            cardinality_min: Minimum cardinality value.
            cardinality_max: Maximum cardinality value.
            is_nillable: Nillable flag.
            default_value: Default value.
            fixed_value: Fixed value.
            xbt_manifest_id: XBT manifest identifier.
            code_list_manifest_id: Code list manifest identifier.
            agency_id_list_manifest_id: Agency ID list manifest identifier.
            owner_top_level_asbiep_id: Owner top-level ASBIEP identifier.
            requester_user_id: Requesting user identifier.
            now: Timestamp used for create/update fields.

        Returns:
            Created BBIE identifier.
        """
        pass

    async def activate_existing_bbie(
        self,
        bbie_id: int,
        requester_user_id: AppUserId,
        now: datetime,
        to_bbiep_id: int | None = None,
    ) -> None:
        """Enable an existing BBIE and optionally rewire target BBIEP.

        Args:
            bbie_id: BBIE identifier.
            requester_user_id: Requesting user identifier.
            now: Timestamp used for update fields.
            to_bbiep_id: Optional replacement target BBIEP identifier.
        """
        pass

    async def update_bbie(
        self,
        bbie_id: int,
        requester_user_id: AppUserId,
        is_admin: bool,
        is_used: bool | None = None,
        is_nillable: bool | None = None,
        cardinality_min: int | None = None,
        cardinality_max: int | None = None,
        definition: str | None = None,
        example: str | None = None,
        remark: str | None = None,
        default_value: str | None = None,
        fixed_value: str | None = None,
        facet_min_length: int | None = None,
        facet_max_length: int | None = None,
        facet_pattern: str | None = None,
        xbt_manifest_id: int | None = None,
        code_list_manifest_id: int | None = None,
        agency_id_list_manifest_id: int | None = None,
    ) -> list[str]:
        """Update BBIE fields and return updated field names."""
        pass

    async def create_bbie_sc(
        self,
        bbie_id: int,
        based_dt_sc_manifest_id: DataTypeSupplementaryComponentManifestId,
        requester_user_id: AppUserId,
        is_admin: bool,
    ) -> int:
        """Create or enable BBIE_SC and return BBIE_SC identifier."""
        pass

    async def update_bbie_sc(
        self,
        bbie_sc_id: int,
        requester_user_id: AppUserId,
        is_admin: bool,
        is_used: bool | None = None,
        cardinality_min: int | None = None,
        cardinality_max: int | None = None,
        definition: str | None = None,
        example: str | None = None,
        remark: str | None = None,
        biz_term: str | None = None,
        display_name: str | None = None,
        default_value: str | None = None,
        fixed_value: str | None = None,
        facet_min_length: int | None = None,
        facet_max_length: int | None = None,
        facet_pattern: str | None = None,
        xbt_manifest_id: int | None = None,
        code_list_manifest_id: int | None = None,
        agency_id_list_manifest_id: int | None = None,
    ) -> list[str]:
        """Update BBIE_SC fields and return updated field names."""
        pass

    async def reuse_top_level_asbiep(
        self,
        asbie_id: int,
        reuse_top_level_asbiep_id: int,
        requester_user_id: AppUserId,
        is_admin: bool,
    ) -> int:
        """Reuse top-level ASBIEP in ASBIE, mark the ASBIE as used, and return reused ASBIEP identifier."""
        pass

    async def remove_reused_top_level_asbiep(
        self,
        asbie_id: int,
        requester_user_id: AppUserId,
        is_admin: bool,
    ) -> None:
        """Remove reused top-level ASBIEP from ASBIE by reusing or creating an owned matching ASBIEP."""
        pass
