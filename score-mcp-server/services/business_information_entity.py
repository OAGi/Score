"""
Service class for managing Business Information Entity (BIE) operations in connectCenter.

Business Information Entities are runtime instances of Core Components that are
configured for specific business contexts. They represent the actual business
documents and messages used in information exchanges. This service provides
comprehensive functionality for querying, creating, updating, and managing
BIE data with support for filtering, pagination, and sorting.
"""

import hashlib
import logging
from datetime import datetime, timezone

from fastapi import HTTPException

from sqlmodel import select, func, or_, text
from sqlalchemy.orm import selectinload

from services.models import AppUser, TopLevelAsbiep, Asbiep, BizCtxAssignment, Abie, BizCtx, Release, AsccpManifest, Asccp, \
    AccManifest, Asbie, AsccManifest, Bbie, BccManifest, BccpManifest, Bbiep, DtManifest, DtScManifest, BbieSc, \
    AsbiepSupportDoc, DtAwdPri, DtScAwdPri
from services.models.common import Sort, PaginationParams, DateRangeParams, Page
from services.transaction import transaction, db_add, db_get, db_delete, db_refresh, db_flush, db_exec
from services.utils import generate_guid
from services.cache import cache, evict_cache
from .core_component import CoreComponentService

# Configure logging
logger = logging.getLogger(__name__)


class BusinessInformationEntityService:
    """
    Service class for managing Business Information Entity (BIE) operations.
    
    This service provides a comprehensive interface for working with Business
    Information Entities, which are runtime instances of Core Components configured
    for specific business contexts. BIEs represent the actual business documents
    and messages used in information exchanges, such as purchase orders, invoices,
    or shipping notices.
    
    Key Features:
    - Query Top-Level ASBIEPs (Business Information Entities) with advanced filtering
    - Retrieve individual BIEs with full relationship loading
    - Support for pagination, sorting, and date range filtering
    - Automatic loading of related entities (owner, creator, release, business contexts, etc.)
    - Business context assignment management
    - Complex relationship traversal (ASBIE, BBIE hierarchies)
    - Support for supplementary components and data type facets
    - SQL injection protection through column whitelisting
    
    Main Operations:
    - get_top_level_asbiep_list_by_release(): Retrieve paginated lists of Top-Level
      ASBIEPs filtered by release with optional filters for library_id, release_id_list,
      den, version, status, state, is_deprecated, and date ranges. Supports custom
      sorting. Note: Only returns BIEs that have at least one business context assigned
      (business rule requirement).
    
    - Additional methods for retrieving individual BIEs and managing BIE relationships
      are available in the service implementation.
    
    Business Rules:
    - Every BIE must have at least one business context assigned
    - Top-Level ASBIEPs without business contexts are excluded from query results
    - BIEs are based on ASCCP Manifests, which in turn reference ACC Manifests
    
    Filtering and Sorting:
    - Supports filtering by library_id (exact match), release_id_list (list of IDs),
      den (partial match on DEN or display name, case-insensitive, OR clause),
      version, status, state (partial match, case-insensitive), is_deprecated (exact match)
    - Date range filtering for creation and last update timestamps
    - Multi-column sorting with ascending/descending order
    - Column whitelist prevents SQL injection attacks
    
    Relationship Loading:
    All queries automatically load related entities using SQLAlchemy's selectinload:
    - TopLevelAsbiep: asbiep (with based_asccp_manifest, role_of_acc_manifest),
      owner_user, last_updated_by_user, release (with library), biz_ctx_assignments
      (with biz_ctx and biz_ctx_values)
    - Full hierarchy of ASBIEs, BBIEs, and their relationships
    - Data type manifests and supplementary components
    - Support documents and other metadata
    
    Transaction Management:
    All database operations are wrapped in transactions (read-only for queries,
    read-write for mutations) to ensure data consistency and proper connection management.
    
    Example Usage:
        service = BusinessInformationEntityService(requester=current_user)
        
        # Get paginated BIEs for a release with filters
        page = service.get_top_level_asbiep_list_by_release(
            release_id_list=[1, 2],
            den="Purchase Order",
            state="Published",
            pagination=PaginationParams(offset=0, limit=10),
            sort_list=[Sort(column="den", direction="asc")]
        )
    """

    # Allowed columns for ordering to prevent SQL injection
    allowed_columns_for_order_by = [
        'den',
        'version',
        'status',
        'state',
        'creation_timestamp',
        'last_update_timestamp'
    ]

    def __init__(self, requester: AppUser):
        """
        Initialize the service.
        
        Args:
            requester: The user making the request
        """
        self.requester = requester

    @cache(key_prefix="business_information_entity.get_top_level_asbiep_list_by_release")
    @transaction(read_only=True)
    def get_top_level_asbiep_list_by_release(
            self,
            library_id: int = None,
            release_id_list: list[int] = None,
            den: str = None,
            version: str = None,
            status: str = None,
            state: str = None,
            is_deprecated: bool = None,
            created_on_params: DateRangeParams = None,
            last_updated_on_params: DateRangeParams = None,
            pagination: PaginationParams = None,
            sort_list: list[Sort] = None
    ) -> Page:
        """
        Get business information entities by release with filtering, pagination, and sorting.
        
        Note: Only returns top_level_asbieps that have at least one business context assigned.
        This is a business rule - every BIE must have at least one business context.
        
        Args:
            library_id: Filter by library ID (exact match)
            release_id_list: List of release IDs to filter by (optional, if not provided searches all releases)
            den: Filter by Data Element Name (DEN) or display name (partial match, case-insensitive, OR clause)
            version: Filter by version (partial match, case-insensitive)
            status: Filter by status (partial match, case-insensitive)
            state: Filter by state (partial match, case-insensitive)
            is_deprecated: Filter by deprecation status
            created_on_params: Date range for creation timestamp filtering
            last_updated_on_params: Date range for last update timestamp filtering
            pagination: Pagination parameters
            sort_list: List of sort specifications
            
        Returns:
            Page: Paginated response containing top_level_asbieps and pagination metadata
        """
        # Set default pagination if not provided
        if pagination is None:
            pagination = PaginationParams(offset=0, limit=10)

        # Build the base query with eager loading
        query = self._get_base_query_with_eager_loading()

        # Apply filters
        query = self._apply_filters(
            query,
            library_id=library_id,
            release_id_list=release_id_list,
            den=den,
            version=version,
            status=status,
            state=state,
            is_deprecated=is_deprecated,
            created_on_params=created_on_params,
            last_updated_on_params=last_updated_on_params
        )

        # Apply sorting
        query = self._apply_sorting(query, sort_list)

        # Get total count with same filters
        count_query = select(func.count()).select_from(query.subquery())
        total_count = db_exec(count_query).one()

        # Apply pagination
        query = query.offset(pagination.offset).limit(pagination.limit)

        # Execute query
        top_level_asbieps = db_exec(query).all()

        # Create Page object
        return Page(
            total=total_count,
            offset=pagination.offset,
            limit=pagination.limit,
            items=list(top_level_asbieps)
        )

    def _get_base_query_with_eager_loading(self):
        """
        Build the base query with all necessary joins and eager loading for top-level ASBIEPs.
        
        Includes join with BizCtxAssignment to ensure only top_level_asbieps with business contexts are returned.
        This is a business rule - every BIE must have at least one business context.
        
        Returns:
            Query with joins and eager loading options
        """
        return (
            select(TopLevelAsbiep)
            .join(Asbiep, TopLevelAsbiep.asbiep_id == Asbiep.asbiep_id)
            .join(AsccpManifest, Asbiep.based_asccp_manifest_id == AsccpManifest.asccp_manifest_id)
            .join(Asccp, AsccpManifest.asccp_id == Asccp.asccp_id)
            .join(BizCtxAssignment, TopLevelAsbiep.top_level_asbiep_id == BizCtxAssignment.top_level_asbiep_id)
            .options(
                selectinload(TopLevelAsbiep.asbiep).selectinload(Asbiep.based_asccp_manifest).selectinload(
                    AsccpManifest.asccp),
                selectinload(TopLevelAsbiep.asbiep).selectinload(Asbiep.based_asccp_manifest).selectinload(
                    AsccpManifest.role_of_acc_manifest).selectinload(AccManifest.acc),
                selectinload(TopLevelAsbiep.owner_user),
                selectinload(TopLevelAsbiep.last_updated_by_user),
                selectinload(TopLevelAsbiep.asbiep).selectinload(Asbiep.created_by_user),
                selectinload(TopLevelAsbiep.asbiep).selectinload(Asbiep.last_updated_by_user)
            )
        )

    def _apply_filters(self, query, library_id: int = None, release_id_list: list[int] = None,
                      den: str = None, version: str = None, status: str = None, state: str = None,
                      is_deprecated: bool = None, created_on_params: DateRangeParams = None,
                      last_updated_on_params: DateRangeParams = None):
        """
        Apply filters to a query for top-level ASBIEPs.
        
        Args:
            query: The base query to apply filters to
            library_id: Filter by library ID (exact match)
            release_id_list: List of release IDs to filter by
            den: Filter by Data Element Name (DEN) or display name (partial match, case-insensitive, OR clause)
            version: Filter by version (partial match, case-insensitive)
            status: Filter by status (partial match, case-insensitive)
            state: Filter by state (partial match, case-insensitive)
            is_deprecated: Filter by deprecation status
            created_on_params: Date range for creation timestamp filtering
            last_updated_on_params: Date range for last update timestamp filtering
            
        Returns:
            Query with filters applied
        """
        # Apply library_id filter if provided
        if library_id:
            query = query.where(AsccpManifest.library_id == library_id)

        # Apply release_id_list filter if provided
        if release_id_list and len(release_id_list) > 0:
            query = query.where(TopLevelAsbiep.release_id.in_(release_id_list))

        if den:
            # Use OR clause to match either den field or display_name field
            query = query.where(
                or_(
                    AsccpManifest.den.ilike(f"%{den}%"),
                    Asbiep.display_name.ilike(f"%{den}%")
                )
            )

        if version:
            query = query.where(TopLevelAsbiep.version.ilike(f"%{version}%"))

        if status:
            query = query.where(TopLevelAsbiep.status.ilike(f"%{status}%"))

        if state:
            query = query.where(TopLevelAsbiep.state.ilike(f"%{state}%"))

        if is_deprecated is not None:
            query = query.where(TopLevelAsbiep.is_deprecated == is_deprecated)

        if created_on_params:
            if created_on_params.after:
                query = query.where(Asbiep.creation_timestamp >= created_on_params.after)
            if created_on_params.before:
                query = query.where(Asbiep.creation_timestamp <= created_on_params.before)

        if last_updated_on_params:
            if last_updated_on_params.after:
                query = query.where(TopLevelAsbiep.last_update_timestamp >= last_updated_on_params.after)
            if last_updated_on_params.before:
                query = query.where(TopLevelAsbiep.last_update_timestamp <= last_updated_on_params.before)

        return query

    def _apply_sorting(self, query, sort_list: list[Sort] = None):
        """
        Apply sorting to a query for top-level ASBIEPs.
        
        Args:
            query: The query to apply sorting to
            sort_list: List of Sort objects for ordering
            
        Returns:
            Query with sorting applied
        """
        if sort_list:
            for sort in sort_list:
                column = None
                if sort.column == 'den':
                    # Use the den field directly from asccp_manifest table
                    column = AsccpManifest.den
                elif sort.column == 'version':
                    column = TopLevelAsbiep.version
                elif sort.column == 'status':
                    column = TopLevelAsbiep.status
                elif sort.column == 'state':
                    column = TopLevelAsbiep.state
                elif sort.column == 'creation_timestamp':
                    column = Asbiep.creation_timestamp
                elif sort.column == 'last_update_timestamp':
                    column = TopLevelAsbiep.last_update_timestamp
                else:
                    continue  # Skip invalid columns

                if column:
                    if sort.direction == 'desc':
                        query = query.order_by(column.desc())
                    else:
                        query = query.order_by(column.asc())
        else:
            # Default sorting by last update timestamp descending
            query = query.order_by(TopLevelAsbiep.last_update_timestamp.desc())

        return query

    def _calculate_hash_path(self, path: str) -> str:
        """Calculate hash path from path string."""
        return hashlib.sha256(path.encode()).hexdigest()

    def _check_duplicate_entity(self, entity_class, path: str, manifest_id_col, manifest_id: int,
                                owner_top_level_asbiep_id: int):
        """
        Check for duplicate entity by path hash and manifest ID.
        
        Args:
            entity_class: The entity class to check (Asbie, Bbie, etc.)
            path: The path string to hash
            manifest_id_col: The column to check (e.g., Asbie.based_ascc_manifest_id)
            manifest_id: The manifest ID value
            owner_top_level_asbiep_id: The owner top-level ASBIEP ID
            
        Returns:
            The duplicate entity if found, None otherwise
        """
        hash_path = self._calculate_hash_path(path)
        duplicate_query = (
            select(entity_class)
            .where(
                manifest_id_col == manifest_id,
                entity_class.hash_path == hash_path,
                entity_class.owner_top_level_asbiep_id == owner_top_level_asbiep_id
            )
        )
        return db_exec(duplicate_query).first()

    def _create_abie(
            self,
            acc_manifest_id: int,
            top_level_asbiep_id: int,
            parent_asbiep_path: str = None
    ) -> "Abie":
        """
        Internal helper method to create an ABIE record.
        
        Args:
            acc_manifest_id: The ACC manifest ID to base the ABIE on
            top_level_asbiep_id: The top-level ASBIEP ID that owns this ABIE
            parent_asbiep_path: Optional parent path to concatenate with current path. It must end with 'ASCCP-{asccp_manifest_id}'.
            
        Returns:
            Abie: The created ABIE record
        """
        # Generate GUID
        abie_guid = generate_guid()

        # Generate path
        path = f"ACC-{acc_manifest_id}"

        # Build the full path by concatenating parent_asbiep_path and current path
        if parent_asbiep_path:
            full_path = f"{parent_asbiep_path}>{path}"
        else:
            full_path = path

        # Check for duplicate ABIE
        existing_abie = self._check_duplicate_entity(
            Abie, full_path, Abie.based_acc_manifest_id,
            acc_manifest_id, top_level_asbiep_id
        )
        
        if existing_abie:
            # Return existing ABIE instead of creating a new one
            return existing_abie

        # Create ABIE record
        abie = Abie(
            guid=abie_guid,
            based_acc_manifest_id=acc_manifest_id,
            path=full_path,
            hash_path=self._calculate_hash_path(full_path),
            biz_ctx_id=None,  # Deprecated field
            definition=None,
            created_by=self.requester.app_user_id,
            last_updated_by=self.requester.app_user_id,
            creation_timestamp=datetime.now(timezone.utc),
            last_update_timestamp=datetime.now(timezone.utc),
            state=None,
            remark=None,
            biz_term=None,
            owner_top_level_asbiep_id=top_level_asbiep_id
        )

        db_add(abie)
        db_flush()  # Get the ID without committing

        return abie

    def _create_asbiep(
            self,
            asccp_manifest_id: int,
            abie_id: int,
            top_level_asbiep_id: int,
            parent_asbie_path: str = None
    ) -> "Asbiep":
        """
        Internal helper method to create an ASBIEP record.
        
        Args:
            asccp_manifest_id: The ASCCP manifest ID to base the ASBIEP on
            abie_id: The ABIE ID that this ASBIEP is a role of
            top_level_asbiep_id: The top-level ASBIEP ID that owns this ASBIEP
            parent_asbie_path: Optional parent path to concatenate with current path. It must end with 'ASCC-{ascc_manifest_id}'.
            
        Returns:
            Asbiep: The created ASBIEP record
        """
        # Generate GUID
        asbiep_guid = generate_guid()

        # Generate path
        path = f"ASCCP-{asccp_manifest_id}"

        # Build the full path by concatenating parent_asbie_path and current path
        if parent_asbie_path:
            full_path = f"{parent_asbie_path}>{path}"
        else:
            full_path = path

        # Check for duplicate ASBIEP
        existing_asbiep = self._check_duplicate_entity(
            Asbiep, full_path, Asbiep.based_asccp_manifest_id,
            asccp_manifest_id, top_level_asbiep_id
        )
        
        if existing_asbiep:
            # Return existing ASBIEP instead of creating a new one
            return existing_asbiep

        # Create ASBIEP record
        asbiep = Asbiep(
            guid=asbiep_guid,
            based_asccp_manifest_id=asccp_manifest_id,
            path=full_path,
            hash_path=self._calculate_hash_path(full_path),
            role_of_abie_id=abie_id,
            definition=None,
            remark=None,
            biz_term=None,
            display_name=None,
            created_by=self.requester.app_user_id,
            last_updated_by=self.requester.app_user_id,
            creation_timestamp=datetime.now(timezone.utc),
            last_update_timestamp=datetime.now(timezone.utc),
            owner_top_level_asbiep_id=top_level_asbiep_id
        )

        db_add(asbiep)
        db_flush()  # Get the ID without committing

        return asbiep

    @transaction(read_only=False)
    def create_top_level_asbiep(
            self,
            asccp_manifest_id: int,
            biz_ctx_list: list[int]
    ) -> tuple[int, int, int]:
        """
        Create a new business information entity with the specified ASCCP manifest and business contexts.
        
        This method creates the necessary database records in the correct order:
        1. Creates top_level_asbiep without asbiep_id initially
        2. Creates biz_ctx_assignment records for each business context
        3. Creates abie record
        4. Creates asbiep record
        5. Updates top_level_asbiep with the asbiep_id
        
        Args:
            asccp_manifest_id: The ASCCP manifest ID to base the business information entity on
            biz_ctx_list: List of business context IDs to assign to the business information entity
            
        Returns:
            tuple: (top_level_asbiep_id, asbiep_id, abie_id)
            
        Raises:
            HTTPException: If validation fails, database errors occur, or resources don't exist.
            Specifically raises 400 error if the ASCCP's role_of_acc is a group type (SemanticGroup or UserExtensionGroup)
        """
        # Create service instances and get data
        core_service = CoreComponentService()

        # Get ASCCP manifest with all relationships loaded (including role_of_acc_manifest.acc)
        asccp_manifest = core_service.get_asccp_by_manifest_id(asccp_manifest_id)

        # Get the ACC manifest from the ASCCP manifest
        acc_manifest = asccp_manifest.role_of_acc_manifest
        if not acc_manifest:
            raise HTTPException(
                status_code=404,
                detail=f"ACC manifest with ID {asccp_manifest.role_of_acc_manifest_id} not found"
            )

        # Get the ACC from the ACC manifest to check component type
        acc = acc_manifest.acc
        if not acc:
            raise HTTPException(
                status_code=404,
                detail=f"ACC with ID {acc_manifest.acc_id} not found"
            )

        # Validate that the ACC is not a group type (SemanticGroup or UserExtensionGroup)
        if acc.oagis_component_type in [3, 4]:  # 3 = SEMANTIC_GROUP, 4 = USER_EXTENSION_GROUP
            group_type_name = "SemanticGroup" if acc.oagis_component_type == 3 else "UserExtensionGroup"
            raise HTTPException(
                status_code=400,
                detail=f"Cannot create BIE from ASCCP with {group_type_name} ACC. Group types are not allowed for BIE creation."
            )

        # Get release from ASCCP manifest (already loaded by get_asccp_by_manifest_id)
        release = asccp_manifest.release
        if not release:
            raise HTTPException(
                status_code=404,
                detail=f"Release with ID {asccp_manifest.release_id} not found"
            )

        # Validate business contexts exist
        existing_biz_ctx_ids = db_exec(
            select(BizCtx.biz_ctx_id)
            .where(BizCtx.biz_ctx_id.in_(biz_ctx_list))
        ).all()

        if len(existing_biz_ctx_ids) != len(biz_ctx_list):
            missing_ids = set(biz_ctx_list) - set(existing_biz_ctx_ids)
            raise HTTPException(
                status_code=404,
                detail=f"Business contexts with IDs {list(missing_ids)} not found"
            )

        # Step 1: Create top_level_asbiep without asbiep_id initially
        top_level_asbiep = TopLevelAsbiep(
            asbiep_id=None,  # Will be updated later
            owner_user_id=self.requester.app_user_id,
            last_updated_by=self.requester.app_user_id,
            release_id=asccp_manifest.release_id,
            version=None,
            status=None,
            state='WIP',
            inverse_mode=False,
            is_deprecated=False,
            deprecated_reason=None,
            deprecated_remark=None,
            source_top_level_asbiep_id=None,
            source_timestamp=None
        )

        db_add(top_level_asbiep)
        db_flush()  # Get the ID without committing

        # Step 2: Create biz_ctx_assignment records
        for biz_ctx_id in biz_ctx_list:
            biz_ctx_assignment = BizCtxAssignment(
                biz_ctx_id=biz_ctx_id,
                top_level_asbiep_id=top_level_asbiep.top_level_asbiep_id
            )
            db_add(biz_ctx_assignment)

        # Step 3: Create abie record using the internal helper method with ASBIEP as parent
        abie = self._create_abie(
            acc_manifest_id=acc_manifest.acc_manifest_id,
            top_level_asbiep_id=top_level_asbiep.top_level_asbiep_id,
            parent_asbiep_path=f'ASCCP-{asccp_manifest_id}'
        )

        # Step 4: Create asbiep record using the internal helper method
        asbiep = self._create_asbiep(
            asccp_manifest_id=asccp_manifest_id,
            abie_id=abie.abie_id,
            top_level_asbiep_id=top_level_asbiep.top_level_asbiep_id
        )

        # Step 5: Update asbiep with the abie_id
        asbiep.role_of_abie_id = abie.abie_id

        # Step 6: Update top_level_asbiep with the asbiep_id
        top_level_asbiep.asbiep_id = asbiep.asbiep_id

        # Evict cache entries for the new BIE
        evict_cache("business_information_entity.get_top_level_asbiep_list_by_release")  # Evict all list queries
        evict_cache("business_information_entity.get_top_level_asbiep_by_id", top_level_asbiep_id=top_level_asbiep.top_level_asbiep_id)  # Evict specific get query

        return top_level_asbiep.top_level_asbiep_id, asbiep.asbiep_id, abie.abie_id

    @transaction(read_only=False)
    def delete_top_level_asbiep(self, top_level_asbiep_id: int) -> bool:
        """
        Delete a business information entity and all related records.
        
        This method deletes the necessary database records in the correct order:
        1. Delete biz_ctx_assignment records
        2. Delete abie records (using owner_top_level_asbiep_id)
        3. Delete asbie records (using owner_top_level_asbiep_id)
        4. Delete bbie records (using owner_top_level_asbiep_id)
        5. Delete asbiep_support_doc records (for each asbiep)
        6. Delete asbiep records (using owner_top_level_asbiep_id)
        7. Delete bbiep records (using owner_top_level_asbiep_id)
        8. Delete bbie_sc records (using owner_top_level_asbiep_id)
        9. Delete top_level_asbiep record
        
        Ownership and state validation:
        - If the current user is an admin, it can be deleted
        - If the current user is the owner and the state is not "Production", then it can be deleted
        - Otherwise, it cannot be deleted
        
        Args:
            top_level_asbiep_id: The top-level ASBIEP ID to delete
            
        Returns:
            bool: True if deleted successfully
            
        Raises:
            HTTPException: If validation fails, the business information entity is not found, 
                          user lacks permission, or database errors occur
        """

        # Validate input parameters
        if not top_level_asbiep_id or top_level_asbiep_id <= 0:
            raise HTTPException(
                status_code=400,
                detail="Top-level ASBIEP ID must be a positive integer"
            )

        # Find the top-level ASBIEP
        top_level_asbiep = db_get(TopLevelAsbiep, top_level_asbiep_id)
        if not top_level_asbiep:
            raise HTTPException(
                status_code=404,
                detail=f"Business information entity with top-level ASBIEP ID {top_level_asbiep_id} not found"
            )

        # Check ownership and state permissions
        # If the current user is an admin, it can be deleted
        is_admin = self.requester.is_admin

        if not is_admin:
            # If not admin, check if user is the owner and state is not "Production"
            is_owner = top_level_asbiep.owner_user_id == self.requester.app_user_id
            is_production = top_level_asbiep.state == "Production"

            if not is_owner:
                raise HTTPException(
                    status_code=403,
                    detail=f"Access denied. You are not the owner of this business information entity (top-level ASBIEP ID: {top_level_asbiep_id}). "
                           f"To fix this: 1) Contact the owner (user ID: {top_level_asbiep.owner_user_id}) to delete it, or "
                           f"2) Ask an administrator to delete it, or 3) Ask the owner to transfer ownership to you first."
                )

            if is_production:
                raise HTTPException(
                    status_code=403,
                    detail=f"Access denied. Cannot delete business information entity in 'Production' state (top-level ASBIEP ID: {top_level_asbiep_id}). "
                           f"To fix this: Ask an administrator to delete it (only admins can delete Production entities). "
                           f"Note: 'Production' is a final state and cannot be changed."
                )

        # Disable foreign key checks temporarily
        db_exec(text("SET foreign_key_checks = 0"))

        try:
            # Step 1: Delete biz_ctx_assignment records
            biz_ctx_assignments = db_exec(
                select(BizCtxAssignment).where(
                    BizCtxAssignment.top_level_asbiep_id == top_level_asbiep_id
                )
            ).all()
            for assignment in biz_ctx_assignments:
                db_delete(assignment)

            # Step 2: Get and delete abie records (using owner_top_level_asbiep_id)
            # Collect IDs before deletion for cache eviction
            abies = db_exec(
                select(Abie).where(
                    Abie.owner_top_level_asbiep_id == top_level_asbiep_id
                )
            ).all()
            abie_ids = [abie.abie_id for abie in abies]
            for abie in abies:
                db_delete(abie)

            # Step 3: Get and delete asbie records (using owner_top_level_asbiep_id)
            # Collect IDs and from_abie_ids before deletion for cache eviction
            asbies = db_exec(
                select(Asbie).where(
                    Asbie.owner_top_level_asbiep_id == top_level_asbiep_id
                )
            ).all()
            asbie_ids = [asbie.asbie_id for asbie in asbies]
            from_abie_ids_for_asbie = list(set([asbie.from_abie_id for asbie in asbies if asbie.from_abie_id]))
            for asbie in asbies:
                db_delete(asbie)

            # Step 4: Get and delete bbie records (using owner_top_level_asbiep_id)
            # Collect IDs and from_abie_ids before deletion for cache eviction
            bbies = db_exec(
                select(Bbie).where(
                    Bbie.owner_top_level_asbiep_id == top_level_asbiep_id
                )
            ).all()
            bbie_ids = [bbie.bbie_id for bbie in bbies]
            from_abie_ids_for_bbie = list(set([bbie.from_abie_id for bbie in bbies if bbie.from_abie_id]))
            for bbie in bbies:
                db_delete(bbie)

            # Step 5: Get and delete asbiep records (using owner_top_level_asbiep_id)
            # Collect IDs before deletion for cache eviction
            asbieps = db_exec(
                select(Asbiep).where(
                    Asbiep.owner_top_level_asbiep_id == top_level_asbiep_id
                )
            ).all()
            asbiep_ids = [asbiep.asbiep_id for asbiep in asbieps]
            for asbiep in asbieps:
                # Delete associated asbiep_support_doc records first (only if table exists - it's in WIP state)
                if self._table_exists("asbiep_support_doc"):
                    try:
                        support_docs = db_exec(
                            select(AsbiepSupportDoc).where(
                                AsbiepSupportDoc.asbiep_id == asbiep.asbiep_id
                            )
                        ).all()
                        for support_doc in support_docs:
                            db_delete(support_doc)
                    except Exception as e:
                        # If deletion fails (e.g., table structure changed), log and continue
                        logger.warning(f"Failed to delete asbiep_support_doc records for asbiep {asbiep.asbiep_id}: {e}")

                # Then delete the asbiep
                db_delete(asbiep)

            # Step 6: Get and delete bbiep records (using owner_top_level_asbiep_id)
            # Collect IDs before deletion for cache eviction
            bbieps = db_exec(
                select(Bbiep).where(
                    Bbiep.owner_top_level_asbiep_id == top_level_asbiep_id
                )
            ).all()
            bbiep_ids = [bbiep.bbiep_id for bbiep in bbieps]
            for bbiep in bbieps:
                db_delete(bbiep)

            # Step 7: Get and delete bbie_sc records (using owner_top_level_asbiep_id)
            # Collect IDs and bbie_ids before deletion for cache eviction
            bbie_scs = db_exec(
                select(BbieSc).where(
                    BbieSc.owner_top_level_asbiep_id == top_level_asbiep_id
                )
            ).all()
            bbie_sc_ids = [bbie_sc.bbie_sc_id for bbie_sc in bbie_scs]
            bbie_ids_for_bbie_sc = list(set([bbie_sc.bbie_id for bbie_sc in bbie_scs if bbie_sc.bbie_id]))
            for bbie_sc in bbie_scs:
                db_delete(bbie_sc)

            # Step 8: Delete top_level_asbiep record
            db_delete(top_level_asbiep)

            # Evict cache entries for the deleted BIE and all related entities
            # Top-level BIE cache eviction
            evict_cache("business_information_entity.get_top_level_asbiep_list_by_release")  # Evict all list queries
            evict_cache("business_information_entity.get_top_level_asbiep_by_id", top_level_asbiep_id=top_level_asbiep_id)  # Evict specific get query
            evict_cache("business_information_entity.get_business_contexts_by_top_level_asbiep_id", top_level_asbiep_id=top_level_asbiep_id)  # Evict business contexts
            
            # Evict cache for ABIEs
            for abie_id in abie_ids:
                evict_cache("business_information_entity.get_abie", abie_id=abie_id)
            
            # Evict cache for ASBIEs
            for asbie_id in asbie_ids:
                evict_cache("business_information_entity.get_asbie_by_asbie_id", asbie_id=asbie_id)
            for from_abie_id in from_abie_ids_for_asbie:
                evict_cache("business_information_entity.get_asbie_list", from_abie_id=from_abie_id)
            evict_cache("business_information_entity.get_asbie_by_based_ascc_manifest_id")  # Evict all queries by based_ascc_manifest_id
            
            # Evict cache for ASBIEPs
            for asbiep_id in asbiep_ids:
                evict_cache("business_information_entity.get_asbiep", asbiep_id=asbiep_id)
            
            # Evict cache for BBIEs
            for bbie_id in bbie_ids:
                evict_cache("business_information_entity.get_bbie_by_bbie_id", bbie_id=bbie_id)
            for from_abie_id in from_abie_ids_for_bbie:
                evict_cache("business_information_entity.get_bbie_list", from_abie_id=from_abie_id)
            evict_cache("business_information_entity.get_bbie_by_based_bcc_manifest_id")  # Evict all queries by based_bcc_manifest_id
            
            # Evict cache for BBIEPs
            for bbiep_id in bbiep_ids:
                evict_cache("business_information_entity.get_bbiep", bbiep_id=bbiep_id)
            
            # Evict cache for BBIE_SCs
            for bbie_id in bbie_ids_for_bbie_sc:
                evict_cache("business_information_entity.get_bbie_sc_list", bbie_id=bbie_id)

            return True

        finally:
            # Re-enable foreign key checks
            db_exec(text("SET foreign_key_checks = 1"))

    def _table_exists(self, table_name: str) -> bool:
        """Check if a table exists in the database.
        
        Args:
            table_name: Name of the table to check
            
        Returns:
            True if the table exists, False otherwise
        """
        try:
            # Query information_schema to check if table exists
            # Use bindparams to safely pass the table name parameter
            result = db_exec(
                text("SELECT COUNT(*) as count FROM information_schema.tables "
                     "WHERE table_schema = DATABASE() AND table_name = :table_name")
                .bindparams(table_name=table_name)
            )
            row = result.first()
            # The result is a Row object, access by index (first column is the count)
            if row is None:
                return False
            # Access the count value (first column)
            count = row[0]
            return count > 0
        except Exception:
            # If query fails, assume table doesn't exist
            return False

    @transaction(read_only=False)
    def transfer_ownership(self, top_level_asbiep_id: int, new_owner_user_id: int) -> tuple[dict, dict]:
        """
        Transfer ownership of a business information entity (BIE) to another user.
        
        This method transfers ownership of a business information entity (BIE) from the current owner
        to a new owner. The transfer is subject to the following rules:
        
        1. Permission checks:
           - If the current user is an admin, they can transfer ownership
           - If the current user is the owner, they can transfer ownership
           - Otherwise, access is denied
        
        2. Role compatibility:
           - If current owner is 'End-User', new owner must be 'End-User'
           - If current owner is 'Developer', new owner must be 'Developer'
           - This prevents privilege escalation regardless of who initiates the transfer
        
        Args:
            top_level_asbiep_id: The top-level ASBIEP ID to transfer ownership
            new_owner_user_id: The user ID of the new owner
            
        Returns:
            tuple: (previous_owner_info, new_owner_info) - Dictionaries containing user information for both users
            
        Raises:
            HTTPException: If validation fails, the business information entity is not found,
                          user lacks permission, role mismatch, or database errors occur
        """
        # Validate input parameters
        if not top_level_asbiep_id or top_level_asbiep_id <= 0:
            raise HTTPException(
                status_code=400,
                detail="Top-level ASBIEP ID must be a positive integer"
            )

        if not new_owner_user_id or new_owner_user_id <= 0:
            raise HTTPException(
                status_code=400,
                detail="New owner user ID must be a positive integer"
            )

        if new_owner_user_id == self.requester.app_user_id:
            raise HTTPException(
                status_code=400,
                detail="You cannot transfer ownership to yourself. Please select a different user as the new owner."
            )

        # Find the top-level ASBIEP
        top_level_asbiep = db_get(TopLevelAsbiep, top_level_asbiep_id)
        if not top_level_asbiep:
            raise HTTPException(
                status_code=404,
                detail=f"Business information entity with top-level ASBIEP ID {top_level_asbiep_id} not found"
            )

        # Get the current owner
        current_owner = db_get(AppUser, top_level_asbiep.owner_user_id)
        if not current_owner:
            raise HTTPException(
                status_code=404,
                detail=f"Current owner with user ID {top_level_asbiep.owner_user_id} not found"
            )

        # Get the new owner
        new_owner = db_get(AppUser, new_owner_user_id)
        if not new_owner:
            raise HTTPException(
                status_code=404,
                detail=f"New owner with user ID {new_owner_user_id} not found"
            )

        # Check if new owner is enabled
        if not new_owner.is_enabled:
            raise HTTPException(
                status_code=400,
                detail=f"New owner with user ID {new_owner_user_id} is disabled and cannot receive ownership"
            )

        # Permission checks
        is_admin = self.requester.is_admin
        is_current_owner = top_level_asbiep.owner_user_id == self.requester.app_user_id

        if not is_admin and not is_current_owner:
            raise HTTPException(
                status_code=403,
                detail=f"Access denied. You are not the owner of this business information entity (top-level ASBIEP ID: {top_level_asbiep_id}) "
                       f"and you are not an admin. To fix this: 1) Contact the owner (user ID: {top_level_asbiep.owner_user_id}) to transfer it, or "
                       f"2) Ask an administrator to transfer it."
            )

        # Role compatibility checks
        # Role compatibility rule: New owner must have the same role as current owner
        # This prevents privilege escalation regardless of who initiates the transfer
        if current_owner.is_developer != new_owner.is_developer:
            raise HTTPException(
                status_code=400,
                detail=f"Role mismatch. Current owner is {'Developer' if current_owner.is_developer else 'End-User'}, but new owner is {'Developer' if new_owner.is_developer else 'End-User'}. "
                       f"The new owner must have the same role as the current owner to prevent privilege escalation."
            )

        # Transfer ownership
        top_level_asbiep.owner_user_id = new_owner_user_id
        db_add(top_level_asbiep)

        # Evict cache entries for the updated BIE
        evict_cache("business_information_entity.get_top_level_asbiep_list_by_release")  # Evict all list queries
        evict_cache("business_information_entity.get_top_level_asbiep_by_id", top_level_asbiep_id=top_level_asbiep_id)  # Evict specific get query

        # Extract user information before session closes to avoid DetachedInstanceError
        previous_owner_info = {
            'app_user_id': current_owner.app_user_id,
            'login_id': current_owner.login_id,
            'name': current_owner.name,
            'is_admin': current_owner.is_admin,
            'is_developer': current_owner.is_developer
        }

        new_owner_info = {
            'app_user_id': new_owner.app_user_id,
            'login_id': new_owner.login_id,
            'name': new_owner.name,
            'is_admin': new_owner.is_admin,
            'is_developer': new_owner.is_developer
        }

        return previous_owner_info, new_owner_info

    @transaction(read_only=False)
    def update_top_level_asbiep(self, top_level_asbiep_id: int,
                                version: str | None = None, status: str | None = None,
                                display_name: str | None = None,
                                biz_term: str | None = None, remark: str | None = None,
                                is_deprecated: bool | None = None, deprecated_reason: str | None = None,
                                deprecated_remark: str | None = None) -> list[str]:
        """
        Update a business information entity (BIE) with new version, status, deprecation information, and ASBIEP properties.
        
        This method allows updating specific fields of a BIE including:
        - version: Version number assigned by the user
        - status: Usage status (e.g., 'Prototype', 'Test', 'Production')
        - display_name: Display name of the ASBIEP
        - biz_term: Business term to indicate what the BIE is called in a particular business context
        - remark: Context-specific usage remarks about the BIE
        - is_deprecated: Whether the BIE is deprecated
        - deprecated_reason: Reason for deprecation (required if deprecating)
        - deprecated_remark: Additional deprecation remarks (optional)
        
        Permission Requirements:
        - The current user must be the owner of the BIE
        - The BIE state must be 'WIP' (Work In Progress)
        
        Args:
            top_level_asbiep_id: The top-level ASBIEP ID to update
            version: New version number (optional)
            status: New status (optional)
            display_name: New display name (optional)
            biz_term: New business term (optional)
            remark: New remark (optional)
            is_deprecated: New deprecation status (optional)
            deprecated_reason: Reason for deprecation (required if deprecating)
            deprecated_remark: Additional deprecation remarks (optional)
            
        Returns:
            list[str]: List of fields that were updated
            
        Raises:
            HTTPException: If validation fails, the BIE is not found, user lacks permission,
                          BIE state is not 'WIP', or database errors occur
        """
        # Get the BIE
        top_level_asbiep = db_get(TopLevelAsbiep, top_level_asbiep_id)
        if not top_level_asbiep:
            raise HTTPException(
                status_code=404,
                detail=f"Business information entity (BIE) with top-level ASBIEP ID {top_level_asbiep_id} not found"
            )

        # Check ownership
        if not self.requester.is_admin and top_level_asbiep.owner_user_id != self.requester.app_user_id:
            raise HTTPException(
                status_code=403,
                detail="Access denied. Only the owner or an admin can update this business information entity (BIE)"
            )

        # Check state - must be 'WIP' for updates
        if top_level_asbiep.state != 'WIP':
            raise HTTPException(
                status_code=400,
                detail=f"Business information entity (BIE) state must be 'WIP' for updates. Current state: {top_level_asbiep.state}"
            )

        # Track updated fields
        updated_fields = []

        # Update version if provided
        if version is not None:
            top_level_asbiep.version = version
            updated_fields.append("version")

        # Update status if provided
        if status is not None:
            top_level_asbiep.status = status
            updated_fields.append("status")

        # Update deprecation fields if provided
        if is_deprecated is not None:
            top_level_asbiep.is_deprecated = is_deprecated
            updated_fields.append("is_deprecated")

            if is_deprecated:
                # If deprecating, set reason and remark
                if deprecated_reason is not None:
                    top_level_asbiep.deprecated_reason = deprecated_reason
                    updated_fields.append("deprecated_reason")
                if deprecated_remark is not None:
                    top_level_asbiep.deprecated_remark = deprecated_remark
                    updated_fields.append("deprecated_remark")
            else:
                # If undeprecating, clear reason and remark
                top_level_asbiep.deprecated_reason = None
                top_level_asbiep.deprecated_remark = None
                updated_fields.extend(["deprecated_reason", "deprecated_remark"])

        # Update last update timestamp and user
        top_level_asbiep.last_update_timestamp = datetime.now(timezone.utc)
        top_level_asbiep.last_updated_by = self.requester.app_user_id
        db_add(top_level_asbiep)

        # Update ASBIEP properties if any are provided
        asbiep_updated_fields = []
        if any(field is not None for field in [biz_term, remark, display_name]):
            asbiep_updated_fields = self.update_asbiep_properties(
                top_level_asbiep_id=top_level_asbiep_id,
                display_name=display_name,
                biz_term=biz_term,
                remark=remark
            )


        # Combine all updated fields
        all_updated_fields = updated_fields + asbiep_updated_fields

        # Evict cache entries for the updated BIE
        evict_cache("business_information_entity.get_top_level_asbiep_list_by_release")  # Evict all list queries
        evict_cache("business_information_entity.get_top_level_asbiep_by_id", top_level_asbiep_id=top_level_asbiep_id)  # Evict specific get query

        return all_updated_fields

    @transaction(read_only=False)
    def update_top_level_asbiep_state(self, top_level_asbiep_id: int, new_state: str) -> tuple[str, str]:
        """
        Update the state of a business information entity (BIE) following the state transition rules.
        
        This method allows updating the state of a BIE following these rules:
        - If BIE state is 'WIP', it can transition to 'QA'
        - If BIE state is 'QA', it can transition to either 'WIP' (back) or 'Production'
        - If BIE state is 'Production', it cannot be changed
        
        Permission Requirements:
        - The current user must be the owner of the BIE
        - The state transition must be valid according to the rules above
        
        Args:
            top_level_asbiep_id: The top-level ASBIEP ID to update state
            new_state: The new state for the BIE
            
        Returns:
            tuple[str, str]: (previous_state, new_state) - The previous and new states
            
        Raises:
            HTTPException: If validation fails, the BIE is not found, user lacks permission,
                          invalid state transition, or database errors occur
        """
        # Get the BIE
        top_level_asbiep = db_get(TopLevelAsbiep, top_level_asbiep_id)
        if not top_level_asbiep:
            raise HTTPException(
                status_code=404,
                detail=f"Business information entity (BIE) with top-level ASBIEP ID {top_level_asbiep_id} not found"
            )

        # Check ownership
        if not self.requester.is_admin and top_level_asbiep.owner_user_id != self.requester.app_user_id:
            raise HTTPException(
                status_code=403,
                detail="Access denied. Only the owner or an admin can update the state of this business information entity (BIE)"
            )

        current_state = top_level_asbiep.state

        # Validate state transition
        valid_transitions = {
            'WIP': ['QA'],
            'QA': ['WIP', 'Production'],
            'Production': []  # Cannot transition from Production
        }

        if current_state not in valid_transitions:
            raise HTTPException(
                status_code=400,
                detail=f"Invalid current state '{current_state}'. Valid states are: {', '.join(valid_transitions.keys())}"
            )

        if new_state not in valid_transitions[current_state]:
            if current_state == 'Production':
                raise HTTPException(
                    status_code=400,
                    detail="Business information entity (BIE) state is 'Production' and cannot be changed"
                )
            else:
                valid_next_states = ', '.join(valid_transitions[current_state])
                raise HTTPException(
                    status_code=400,
                    detail=f"Invalid state transition from '{current_state}' to '{new_state}'. Valid transitions from '{current_state}' are: {valid_next_states}"
                )

        # Update state
        top_level_asbiep.state = new_state
        top_level_asbiep.last_update_timestamp = datetime.now(timezone.utc)
        top_level_asbiep.last_updated_by = self.requester.app_user_id
        db_add(top_level_asbiep)

        # Evict cache entries for the updated BIE
        evict_cache("business_information_entity.get_top_level_asbiep_list_by_release")  # Evict all list queries
        evict_cache("business_information_entity.get_top_level_asbiep_by_id", top_level_asbiep_id=top_level_asbiep_id)  # Evict specific get query

        return current_state, new_state

    @transaction(read_only=False)
    def assign_business_context(self, top_level_asbiep_id: int, biz_ctx_id: int) -> None:
        """
        Assign a business context to a business information entity (BIE).
        
        This method creates a new business context assignment for a BIE. If the assignment
        already exists, the request will be denied with a 400 error.
        
        Permission Requirements:
        - The current user must be the owner of the BIE
        - The BIE state must be 'WIP' (Work In Progress)
        
        Args:
            top_level_asbiep_id: The top-level ASBIEP ID to assign the business context to
            biz_ctx_id: The business context ID to assign
            
        Raises:
            HTTPException: If validation fails, the BIE or business context is not found,
                          user lacks permission, BIE state is not 'WIP', duplicate assignment exists,
                          or database errors occur
        """
        # Get the BIE
        top_level_asbiep = db_get(TopLevelAsbiep, top_level_asbiep_id)
        if not top_level_asbiep:
            raise HTTPException(
                status_code=404,
                detail=f"Business information entity (BIE) with top-level ASBIEP ID {top_level_asbiep_id} not found"
            )

        # Check ownership
        if not self.requester.is_admin and top_level_asbiep.owner_user_id != self.requester.app_user_id:
            raise HTTPException(
                status_code=403,
                detail="Access denied. Only the owner or an admin can assign business contexts to this business information entity (BIE)"
            )

        # Check state - must be 'WIP' for updates
        if top_level_asbiep.state != 'WIP':
            raise HTTPException(
                status_code=400,
                detail=f"Business information entity (BIE) state must be 'WIP' for business context assignments. Current state: {top_level_asbiep.state}"
            )

        # Verify business context exists
        biz_ctx = db_get(BizCtx, biz_ctx_id)
        if not biz_ctx:
            raise HTTPException(
                status_code=404,
                detail=f"Business context with ID {biz_ctx_id} not found"
            )

        # Check if assignment already exists
        existing_assignment = db_exec(
            select(BizCtxAssignment).where(
                BizCtxAssignment.top_level_asbiep_id == top_level_asbiep_id,
                BizCtxAssignment.biz_ctx_id == biz_ctx_id
            )
        ).first()

        if existing_assignment:
            # Assignment already exists, deny the request
            raise HTTPException(
                status_code=400,
                detail=f"Business context with ID {biz_ctx_id} is already assigned to the Top-Level ASBIEP with ID {top_level_asbiep_id}. Duplicate assignments are not allowed."
            )

        # Create new assignment
        new_assignment = BizCtxAssignment(
            top_level_asbiep_id=top_level_asbiep_id,
            biz_ctx_id=biz_ctx_id,
            created_by=self.requester.app_user_id,
            creation_timestamp=datetime.now(timezone.utc)
        )
        db_add(new_assignment)

        # Evict cache entries for business contexts and BIE
        evict_cache("business_information_entity.get_business_contexts_by_top_level_asbiep_id", top_level_asbiep_id=top_level_asbiep_id)  # Evict business contexts
        evict_cache("business_information_entity.get_top_level_asbiep_by_id", top_level_asbiep_id=top_level_asbiep_id)  # Evict specific get query

    @transaction(read_only=False)
    def unassign_business_context(self, top_level_asbiep_id: int, biz_ctx_id: int) -> None:
        """
        Unassign a business context from a business information entity (BIE).
        
        This method removes a business context assignment from a BIE. If the assignment
        doesn't exist, it will succeed without error.
        
        Permission Requirements:
        - The current user must be the owner of the BIE
        - The BIE state must be 'WIP' (Work In Progress)
        
        Args:
            top_level_asbiep_id: The top-level ASBIEP ID to unassign the business context from
            biz_ctx_id: The business context ID to unassign
            
        Raises:
            HTTPException: If validation fails, the BIE or business context is not found,
                          user lacks permission, BIE state is not 'WIP', or database errors occur
        """
        # Get the BIE
        top_level_asbiep = db_get(TopLevelAsbiep, top_level_asbiep_id)
        if not top_level_asbiep:
            raise HTTPException(
                status_code=404,
                detail=f"Business information entity (BIE) with top-level ASBIEP ID {top_level_asbiep_id} not found"
            )

        # Check ownership
        if not self.requester.is_admin and top_level_asbiep.owner_user_id != self.requester.app_user_id:
            raise HTTPException(
                status_code=403,
                detail="Access denied. Only the owner or an admin can unassign business contexts from this business information entity (BIE)"
            )

        # Check state - must be 'WIP' for updates
        if top_level_asbiep.state != 'WIP':
            raise HTTPException(
                status_code=400,
                detail=f"Business information entity (BIE) state must be 'WIP' for business context unassignments. Current state: {top_level_asbiep.state}"
            )

        # Verify business context exists
        biz_ctx = db_get(BizCtx, biz_ctx_id)
        if not biz_ctx:
            raise HTTPException(
                status_code=404,
                detail=f"Business context with ID {biz_ctx_id} not found"
            )

        # Find and delete the assignment
        assignment = db_exec(
            select(BizCtxAssignment).where(
                BizCtxAssignment.top_level_asbiep_id == top_level_asbiep_id,
                BizCtxAssignment.biz_ctx_id == biz_ctx_id
            )
        ).first()

        if assignment:
            db_delete(assignment)
        
        # Evict cache entries for business contexts and BIE
        evict_cache("business_information_entity.get_business_contexts_by_top_level_asbiep_id", top_level_asbiep_id=top_level_asbiep_id)  # Evict business contexts
        evict_cache("business_information_entity.get_top_level_asbiep_by_id", top_level_asbiep_id=top_level_asbiep_id)  # Evict specific get query
        # If assignment doesn't exist, that's fine - no error needed

    @cache(key_prefix="business_information_entity.get_business_contexts_by_top_level_asbiep_id")
    @transaction(read_only=True)
    def get_business_contexts_by_top_level_asbiep_id(self, top_level_asbiep_id: int) -> list["BizCtx"]:
        """
        Get business contexts for a specific top-level ASBIEP.
        
        Args:
            top_level_asbiep_id: The top-level ASBIEP ID
            
        Returns:
            list[BizCtx]: List of business contexts assigned to the top-level ASBIEP
        """
        query = (
            select(BizCtx)
            .join(BizCtxAssignment, BizCtx.biz_ctx_id == BizCtxAssignment.biz_ctx_id)
            .where(BizCtxAssignment.top_level_asbiep_id == top_level_asbiep_id)
        )
        business_contexts = db_exec(query).all()
        return business_contexts

    @transaction(read_only=False)
    def update_asbiep_properties(self, top_level_asbiep_id: int,
                                 display_name: str | None = None, biz_term: str | None = None,
                                 remark: str | None = None) -> list[str]:
        """
        Update ASBIEP properties (biz_term, remark, display_name) for a business information entity.
        
        This method allows updating specific ASBIEP fields including:
        - display_name: Display name of the ASBIEP
        - biz_term: Business term to indicate what the BIE is called in a particular business context
        - remark: Context-specific usage remarks about the BIE
        
        Permission Requirements:
        - The current user must be the owner of the BIE
        - The BIE state must be 'WIP' (Work In Progress)
        
        Args:
            top_level_asbiep_id: The top-level ASBIEP ID to update
            display_name: New display name (optional)
            biz_term: New business term (optional)
            remark: New remark (optional)
            
        Returns:
            list[str]: List of fields that were updated
            
        Raises:
            HTTPException: If validation fails, the BIE is not found, user lacks permission,
                          BIE state is not 'WIP', or database errors occur
        """
        # Get the BIE
        top_level_asbiep = db_get(TopLevelAsbiep, top_level_asbiep_id)
        if not top_level_asbiep:
            raise HTTPException(
                status_code=404,
                detail=f"Business information entity (BIE) with top-level ASBIEP ID {top_level_asbiep_id} not found"
            )

        # Check ownership
        if not self.requester.is_admin and top_level_asbiep.owner_user_id != self.requester.app_user_id:
            raise HTTPException(
                status_code=403,
                detail="Access denied. Only the owner or an admin can update this business information entity (BIE)"
            )

        # Check state - must be 'WIP' for updates
        if top_level_asbiep.state != 'WIP':
            raise HTTPException(
                status_code=400,
                detail=f"Business information entity (BIE) state must be 'WIP' for updates. Current state: {top_level_asbiep.state}"
            )

        # Get the ASBIEP
        asbiep = db_get(Asbiep, top_level_asbiep.asbiep_id)
        if not asbiep:
            raise HTTPException(
                status_code=404,
                detail=f"ASBIEP with ID {top_level_asbiep.asbiep_id} not found"
            )

        # Track updated fields
        updated_fields = []

        # Update display_name if provided
        if display_name is not None:
            asbiep.display_name = display_name
            updated_fields.append("display_name")

        # Update biz_term if provided
        if biz_term is not None:
            asbiep.biz_term = biz_term
            updated_fields.append("biz_term")

        # Update remark if provided
        if remark is not None:
            asbiep.remark = remark
            updated_fields.append("remark")

        # Update last update timestamp and user
        asbiep.last_update_timestamp = datetime.now(timezone.utc)
        asbiep.last_updated_by = self.requester.app_user_id

        # Also update the top-level ASBIEP timestamp
        top_level_asbiep.last_update_timestamp = datetime.now(timezone.utc)
        top_level_asbiep.last_updated_by = self.requester.app_user_id

        db_add(asbiep)
        db_add(top_level_asbiep)

        # Evict cache entries for the updated ASBIEP and BIE
        evict_cache("business_information_entity.get_top_level_asbiep_by_id", top_level_asbiep_id=top_level_asbiep_id)  # Evict specific get query
        evict_cache("business_information_entity.get_asbiep", asbiep_id=asbiep.asbiep_id)  # Evict ASBIEP query

        return updated_fields

    @cache(key_prefix="business_information_entity.get_top_level_asbiep_by_id")
    @transaction(read_only=True)
    def get_top_level_asbiep_by_id(self, top_level_asbiep_id: int) -> "TopLevelAsbiep | None":
        """
        Get a single BIE (Business Information Entity) by its top-level ASBIEP ID.
        
        Args:
            top_level_asbiep_id: The top-level ASBIEP ID
            
        Returns:
            TopLevelAsbiep | None: The top-level ASBIEP if found, None otherwise
        """
        query = (
            select(TopLevelAsbiep)
            .options(
                selectinload(TopLevelAsbiep.asbiep).selectinload(Asbiep.based_asccp_manifest).selectinload(
                    AsccpManifest.asccp),
                selectinload(TopLevelAsbiep.asbiep).selectinload(Asbiep.based_asccp_manifest).selectinload(
                    AsccpManifest.role_of_acc_manifest).selectinload(AccManifest.acc),
                selectinload(TopLevelAsbiep.owner_user),
                selectinload(TopLevelAsbiep.last_updated_by_user),
                selectinload(TopLevelAsbiep.asbiep).selectinload(Asbiep.created_by_user),
                selectinload(TopLevelAsbiep.asbiep).selectinload(Asbiep.last_updated_by_user),
                selectinload(TopLevelAsbiep.release).selectinload(Release.library)
            )
            .where(TopLevelAsbiep.top_level_asbiep_id == top_level_asbiep_id)
        )
        top_level_asbiep = db_exec(query).first()
        return top_level_asbiep

    @cache(key_prefix="business_information_entity.get_asbiep")
    @transaction(read_only=True)
    def get_asbiep(self, asbiep_id: int) -> "Asbiep | None":
        """
        Get a single ASBIEP (Association Business Information Entity Property) by its ASBIEP ID.
        
        Args:
            asbiep_id: The ASBIEP ID
            
        Returns:
            Asbiep | None: The ASBIEP if found, None otherwise
        """
        query = (
            select(Asbiep)
            .options(
                selectinload(Asbiep.based_asccp_manifest).selectinload(AsccpManifest.asccp),
                selectinload(Asbiep.based_asccp_manifest).selectinload(
                    AsccpManifest.role_of_acc_manifest).selectinload(AccManifest.acc),
                selectinload(Asbiep.created_by_user),
                selectinload(Asbiep.last_updated_by_user),
                selectinload(Asbiep.owner_top_level_asbiep).selectinload(TopLevelAsbiep.release).selectinload(
                    Release.library),
                selectinload(Asbiep.owner_top_level_asbiep).selectinload(TopLevelAsbiep.owner_user),
                selectinload(Asbiep.owner_top_level_asbiep).selectinload(TopLevelAsbiep.last_updated_by_user)
            )
            .where(Asbiep.asbiep_id == asbiep_id)
        )
        asbiep = db_exec(query).first()
        return asbiep

    @cache(key_prefix="business_information_entity.get_bbiep")
    @transaction(read_only=True)
    def get_bbiep(self, bbiep_id: int) -> "Bbiep | None":
        """
        Get a single BBIEP (Association Business Information Entity Property) by its BBIEP ID.

        Args:
            bbiep_id: The BBIEP ID

        Returns:
            Bbiep | None: The BBIEP if found, None otherwise
        """
        query = (
            select(Bbiep)
            .options(
                selectinload(Bbiep.based_bccp_manifest).selectinload(BccpManifest.bccp),
                selectinload(Bbiep.based_bccp_manifest)
                    .selectinload(BccpManifest.bdt_manifest)
                    .selectinload(DtManifest.dt),
                selectinload(Bbiep.created_by_user),
                selectinload(Bbiep.last_updated_by_user),
                selectinload(Bbiep.owner_top_level_asbiep).selectinload(TopLevelAsbiep.release).selectinload(
                    Release.library),
                selectinload(Bbiep.owner_top_level_asbiep).selectinload(TopLevelAsbiep.owner_user),
                selectinload(Bbiep.owner_top_level_asbiep).selectinload(TopLevelAsbiep.last_updated_by_user)
            )
            .where(Bbiep.bbiep_id == bbiep_id)
        )
        bbiep = db_exec(query).first()
        return bbiep

    @cache(key_prefix="business_information_entity.get_abie")
    @transaction(read_only=True)
    def get_abie(self, abie_id: int) -> "Abie | None":
        """
        Get a single ABIE (Aggregation Business Information Entity) by its ABIE ID.
        
        Args:
            abie_id: The ABIE ID
            
        Returns:
            Abie | None: The ABIE if found, None otherwise
        """
        query = (
            select(Abie)
            .options(
                selectinload(Abie.based_acc_manifest).selectinload(AccManifest.acc),
                selectinload(Abie.created_by_user),
                selectinload(Abie.last_updated_by_user),
                selectinload(Abie.owner_top_level_asbiep).selectinload(TopLevelAsbiep.release).selectinload(
                    Release.library),
                selectinload(Abie.owner_top_level_asbiep).selectinload(TopLevelAsbiep.owner_user),
                selectinload(Abie.owner_top_level_asbiep).selectinload(TopLevelAsbiep.last_updated_by_user)
            )
            .where(Abie.abie_id == abie_id)
        )
        abie = db_exec(query).first()
        return abie

    @cache(key_prefix="business_information_entity.get_asbie_list")
    @transaction(read_only=True)
    def get_asbie_list(self, from_abie_id: int):
        query = (
            select(Asbie)
            .options(
                selectinload(Asbie.based_ascc_manifest).selectinload(AsccManifest.ascc),
                selectinload(Asbie.based_ascc_manifest).selectinload(
                    AsccManifest.to_asccp_manifest).selectinload(AsccpManifest.asccp),
                selectinload(Asbie.to_asbiep)
            )
            .where(Asbie.from_abie_id == from_abie_id)
        )
        asbie_list = db_exec(query).all()
        return asbie_list

    @cache(key_prefix="business_information_entity.get_bbie_list")
    @transaction(read_only=True)
    def get_bbie_list(self, from_abie_id: int):
        query = (
            select(Bbie)
            .options(
                selectinload(Bbie.based_bcc_manifest).selectinload(BccManifest.bcc),
                selectinload(Bbie.based_bcc_manifest).selectinload(
                    BccManifest.to_bccp_manifest).selectinload(BccpManifest.bccp),
                selectinload(Bbie.to_bbiep)
            )
            .where(Bbie.from_abie_id == from_abie_id)
        )
        bbie_list = db_exec(query).all()
        return bbie_list

    @cache(key_prefix="business_information_entity.get_asbie_by_asbie_id")
    @transaction(read_only=True)
    def get_asbie_by_asbie_id(self, asbie_id: int) -> "Asbie | None":
        """
        Get a single ASBIE (Association Business Information Entity) by its ASBIE ID.
        
        Args:
            asbie_id: The ASBIE ID
            
        Returns:
            Asbie | None: The ASBIE if found, None otherwise
        """
        query = (
            select(Asbie)
            .options(
                selectinload(Asbie.based_ascc_manifest).selectinload(AsccManifest.ascc),
                selectinload(Asbie.based_ascc_manifest).selectinload(
                    AsccManifest.to_asccp_manifest).selectinload(AsccpManifest.asccp),
                selectinload(Asbie.to_asbiep),
                selectinload(Asbie.owner_top_level_asbiep)
            )
            .where(Asbie.asbie_id == asbie_id)
        )
        asbie = db_exec(query).first()
        return asbie

    @cache(key_prefix="business_information_entity.get_asbie_by_based_ascc_manifest_id")
    @transaction(read_only=True)
    def get_asbie_by_based_ascc_manifest_id(
            self,
            based_ascc_manifest_id: int,
            owner_top_level_asbiep_id: int,
            hash_path: str | None = None
    ) -> "Asbie | None":
        """
        Get a single ASBIE (Association Business Information Entity) by its based ASCC manifest ID.
        
        Args:
            based_ascc_manifest_id: The ASCC manifest ID that this ASBIE is based on
            owner_top_level_asbiep_id: The top-level ASBIEP ID that owns this ASBIE
            hash_path: Optional hash_path for more precise matching. If provided, filters by hash_path as well.
            
        Returns:
            Asbie | None: The ASBIE if found, None otherwise
        """
        query = (
            select(Asbie)
            .options(
                selectinload(Asbie.based_ascc_manifest).selectinload(AsccManifest.ascc),
                selectinload(Asbie.based_ascc_manifest).selectinload(
                    AsccManifest.to_asccp_manifest).selectinload(AsccpManifest.asccp),
                selectinload(Asbie.to_asbiep),
                selectinload(Asbie.owner_top_level_asbiep)
            )
            .where(
                Asbie.based_ascc_manifest_id == based_ascc_manifest_id,
                Asbie.owner_top_level_asbiep_id == owner_top_level_asbiep_id
            )
        )
        if hash_path:
            query = query.where(Asbie.hash_path == hash_path)
        
        asbie = db_exec(query).first()
        return asbie

    def _validate_and_get_abie_for_asbie_update(
            self,
            from_abie_id: int
    ) -> tuple["Abie", int]:
        """
        Validate and get the ABIE for ASBIE update operation.
        
        Args:
            from_abie_id: The ABIE ID that this ASBIE originates from
            
        Returns:
            tuple: (from_abie, owner_top_level_asbiep_id)
            
        Raises:
            HTTPException: If ABIE not found or has no associated top-level ASBIEP
        """
        from_abie_query = (
            select(Abie)
            .options(
                selectinload(Abie.based_acc_manifest),
                selectinload(Abie.owner_top_level_asbiep)
            )
            .where(Abie.abie_id == from_abie_id)
        )
        from_abie = db_exec(from_abie_query).first()
        if not from_abie:
            raise HTTPException(
                status_code=404,
                detail=f"Could not find the ABIE with ID {from_abie_id}. "
                       f"Please check that the ID is correct and the ABIE exists. "
                       f"You can use get_abie or get_top_level_asbiep to view available ABIEs."
            )
        
        owner_top_level_asbiep_id = from_abie.owner_top_level_asbiep_id
        if not owner_top_level_asbiep_id:
            raise HTTPException(
                status_code=404,
                detail=f"The ABIE with ID {from_abie_id} is not associated with a top-level ASBIEP. "
                       f"This appears to be a data integrity issue. Please contact your system administrator for assistance."
            )
        
        return (from_abie, owner_top_level_asbiep_id)

    def _validate_existing_asbie_for_update(
            self,
            asbie_id: int,
            from_abie_id: int,
            based_ascc_manifest_id: int,
            owner_top_level_asbiep_id: int
    ) -> "Asbie":
        """
        Validate an existing ASBIE for update operation.
        
        Args:
            asbie_id: The ASBIE ID to update
            from_abie_id: The expected from_abie_id
            based_ascc_manifest_id: The expected based_ascc_manifest_id
            owner_top_level_asbiep_id: The expected owner_top_level_asbiep_id
            
        Returns:
            Asbie: The validated ASBIE
            
        Raises:
            HTTPException: If ASBIE not found or validation fails
        """
        existing_asbie_query = select(Asbie).where(Asbie.asbie_id == asbie_id)
        existing_asbie = db_exec(existing_asbie_query).first()
        if not existing_asbie:
            raise HTTPException(
                status_code=404,
                detail=f"Could not find the ASBIE with ID {asbie_id}. "
                       f"Please check that the ID is correct. "
                       f"If you want to create a new ASBIE instead, simply omit the asbie_id parameter."
            )
        
        # Verify it belongs to the same top-level ASBIEP
        if existing_asbie.owner_top_level_asbiep_id != owner_top_level_asbiep_id:
            raise HTTPException(
                status_code=400,
                detail=f"The ASBIE (ID {asbie_id}) and ABIE (ID {from_abie_id}) belong to different top-level ASBIEPs. "
                       f"Please use an asbie_id that belongs to the same top-level ASBIEP as the from_abie_id, "
                       f"or use a different from_abie_id that matches the ASBIE's top-level ASBIEP."
            )
        
        # Verify it belongs to the same from_abie_id
        if existing_asbie.from_abie_id != from_abie_id:
            raise HTTPException(
                status_code=400,
                detail=f"The ASBIE (ID {asbie_id}) does not belong to the ABIE (ID {from_abie_id}). "
                       f"Please use a from_abie_id that matches the ASBIE's parent ABIE, "
                       f"or use a different asbie_id that belongs to the specified ABIE."
            )
        
        # Verify it matches the based_ascc_manifest_id
        if existing_asbie.based_ascc_manifest_id != based_ascc_manifest_id:
            raise HTTPException(
                status_code=400,
                detail=f"The ASBIE (ID {asbie_id}) is not based on the ASCC manifest (ID {based_ascc_manifest_id}). "
                       f"Please use a based_ascc_manifest_id that matches the ASBIE's ASCC, "
                       f"or use a different asbie_id that is based on the specified ASCC manifest."
            )
        
        return existing_asbie

    def _validate_ownership_and_state_for_asbie_update(
            self,
            owner_top_level_asbiep_id: int
    ) -> "TopLevelAsbiep":
        """
        Validate ownership and state for ASBIE update operation.
        
        Args:
            owner_top_level_asbiep_id: The top-level ASBIEP ID
            
        Returns:
            TopLevelAsbiep: The validated top-level ASBIEP
            
        Raises:
            HTTPException: If validation fails
        """
        top_level_asbiep_query = (
            select(TopLevelAsbiep)
            .options(
                selectinload(TopLevelAsbiep.owner_user)
            )
            .where(TopLevelAsbiep.top_level_asbiep_id == owner_top_level_asbiep_id)
        )
        top_level_asbiep = db_exec(top_level_asbiep_query).first()
        if not top_level_asbiep:
            raise HTTPException(
                status_code=404,
                detail=f"Could not find the top-level ASBIEP with ID {owner_top_level_asbiep_id}. "
                       f"This appears to be a data integrity issue. Please contact your system administrator for assistance."
            )
        
        # Check if user is the owner
        if top_level_asbiep.owner_user_id != self.requester.app_user_id:
            raise HTTPException(
                status_code=403,
                detail=f"You don't have permission to update this business information entity. "
                       f"Only the owner (user ID: {top_level_asbiep.owner_user_id}) can make changes. "
                       f"Please contact the owner to update it, or ask them to transfer ownership to you using the transfer_top_level_asbiep_ownership tool."
            )
        
        # Check if state is WIP
        if top_level_asbiep.state != "WIP":
            raise HTTPException(
                status_code=400,
                detail=f"Cannot update the ASBIE because the business information entity is in '{top_level_asbiep.state}' state. "
                       f"Updates are only allowed when the state is 'WIP' (Work In Progress). "
                       f"Please use the update_top_level_asbiep_state tool to change the state to 'WIP' first, then try again."
            )
        
        return top_level_asbiep

    def _validate_ascc_relationship_for_abie(
            self,
            from_abie: "Abie",
            based_ascc_manifest_id: int
    ) -> None:
        """
        Validate that the ASCC manifest is a valid relationship for the ABIE.
        
        Args:
            from_abie: The ABIE to check relationships for
            based_ascc_manifest_id: The ASCC manifest ID to validate
            
        Raises:
            HTTPException: If the ASCC is not a valid relationship
        """
        # First check direct relationship
        ascc_relationship_query = (
            select(AsccManifest)
            .where(AsccManifest.from_acc_manifest_id == from_abie.based_acc_manifest_id)
            .where(AsccManifest.ascc_manifest_id == based_ascc_manifest_id)
        )
        ascc_relationship = db_exec(ascc_relationship_query).first()
        
        # If not found directly, check inherited relationships by traversing ACC hierarchy
        if not ascc_relationship:
            acc_manifest_id = from_abie.based_acc_manifest_id
            checked_manifests = set()
            
            # Traverse the ACC hierarchy to find valid ASCC relationships
            while acc_manifest_id and acc_manifest_id not in checked_manifests:
                checked_manifests.add(acc_manifest_id)
                ascc_check_query = (
                    select(AsccManifest)
                    .where(AsccManifest.from_acc_manifest_id == acc_manifest_id)
                    .where(AsccManifest.ascc_manifest_id == based_ascc_manifest_id)
                )
                ascc_relationship = db_exec(ascc_check_query).first()
                if ascc_relationship:
                    break
                
                # Get the based_acc_manifest_id for inheritance traversal
                acc_manifest = db_get(AccManifest, acc_manifest_id)
                if acc_manifest and acc_manifest.based_acc_manifest_id:
                    acc_manifest_id = acc_manifest.based_acc_manifest_id
                else:
                    break
            
            if not ascc_relationship:
                raise HTTPException(
                    status_code=400,
                    detail=f"The ASCC manifest (ID {based_ascc_manifest_id}) is not a valid relationship for this ABIE (ID {from_abie.abie_id}). "
                           f"The ASCC must be available as a relationship from the ABIE's ACC. "
                           f"Please use get_abie or get_top_level_asbiep to view the available relationships, "
                           f"and choose a based_ascc_manifest_id that appears in the relationships list."
                )

    def _get_ascc_manifest_for_asbie(
            self,
            based_ascc_manifest_id: int
    ) -> tuple["AsccManifest", "AsccpManifest", "AccManifest"]:
        """
        Get and validate ASCC, ASCCP, and ACC manifests for ASBIE creation.
        
        Args:
            based_ascc_manifest_id: The ASCC manifest ID
            
        Returns:
            tuple: (ascc_manifest, asccp_manifest, role_of_acc_manifest)
            
        Raises:
            HTTPException: If any manifest is not found
        """
        # Get ASCC manifest information within session
        ascc_manifest_query = (
            select(AsccManifest)
            .options(
                selectinload(AsccManifest.ascc),
                selectinload(AsccManifest.to_asccp_manifest).selectinload(AsccpManifest.asccp),
                selectinload(AsccManifest.to_asccp_manifest).selectinload(AsccpManifest.role_of_acc_manifest).selectinload(AccManifest.acc)
            )
            .where(AsccManifest.ascc_manifest_id == based_ascc_manifest_id)
        )
        ascc_manifest = db_exec(ascc_manifest_query).first()
        if not ascc_manifest:
            raise HTTPException(
                status_code=404,
                detail=f"Could not find the ASCC manifest with ID {based_ascc_manifest_id}. "
                       f"Please check that the ID is correct. "
                       f"You can use get_ascc or get_acc tools to view available ASCC relationships."
            )
        
        # Get ASCCP manifest from ASCC
        asccp_manifest = ascc_manifest.to_asccp_manifest
        if not asccp_manifest:
            raise HTTPException(
                status_code=404,
                detail=f"The ASCC manifest (ID {based_ascc_manifest_id}) is missing its associated ASCCP manifest. "
                       f"This appears to be a data integrity issue. Please contact your system administrator for assistance."
            )
        
        # Get role_of_acc_manifest for creating ABIE
        role_of_acc_manifest = asccp_manifest.role_of_acc_manifest
        if not role_of_acc_manifest:
            raise HTTPException(
                status_code=404,
                detail=f"The ASCCP manifest (ID {asccp_manifest.asccp_manifest_id}) is missing its associated ACC manifest. "
                       f"This appears to be a data integrity issue. Please contact your system administrator for assistance."
            )
        
        return (ascc_manifest, asccp_manifest, role_of_acc_manifest)

    def _validate_cardinality_for_asbie(
            self,
            ascc_manifest: "AsccManifest",
            cardinality_min: int | None,
            cardinality_max: int | None
    ) -> tuple[int, int]:
        """
        Validate and determine final cardinality values for ASBIE.
        
        Args:
            ascc_manifest: The ASCC manifest to get base cardinality from
            cardinality_min: Provided minimum cardinality (optional)
            cardinality_max: Provided maximum cardinality (optional)
            
        Returns:
            tuple: (final_cardinality_min, final_cardinality_max)
            
        Raises:
            HTTPException: If validation fails
        """
        # Get the base cardinality from ASCC
        base_cardinality_min = ascc_manifest.ascc.cardinality_min
        base_cardinality_max = ascc_manifest.ascc.cardinality_max
        
        # Determine final cardinality values (use provided values or defaults from ASCC)
        final_cardinality_min = cardinality_min if cardinality_min is not None else base_cardinality_min
        final_cardinality_max = cardinality_max if cardinality_max is not None else base_cardinality_max
        
        # Validate cardinality_min must not be less than base cardinality_min
        if final_cardinality_min < base_cardinality_min:
            raise HTTPException(
                status_code=400,
                detail=f"The minimum cardinality ({final_cardinality_min}) is too low. "
                       f"It must be at least {base_cardinality_min} as required by the base ASCC. "
                       f"Please set cardinality_min to {base_cardinality_min} or higher."
            )
        
        # Validate cardinality_max must not exceed base cardinality_max (unless base is -1 for unbounded)
        if base_cardinality_max != -1:
            if final_cardinality_max != -1 and final_cardinality_max > base_cardinality_max:
                raise HTTPException(
                    status_code=400,
                    detail=f"The maximum cardinality ({final_cardinality_max}) exceeds the limit. "
                           f"It must be at most {base_cardinality_max} as allowed by the base ASCC. "
                           f"Please set cardinality_max to {base_cardinality_max} or lower, or use -1 for unbounded if needed."
                )
        
        # Validate cardinality_min must be <= cardinality_max
        if final_cardinality_max != -1:
            if final_cardinality_min > final_cardinality_max:
                raise HTTPException(
                    status_code=400,
                    detail=f"The minimum cardinality ({final_cardinality_min}) cannot be greater than the maximum cardinality ({final_cardinality_max}). "
                           f"Please adjust the values so that the minimum is less than or equal to the maximum. "
                           f"For example, set cardinality_max to at least {final_cardinality_min}, or reduce cardinality_min."
                )
        
        return (final_cardinality_min, final_cardinality_max)

    def _create_new_asbie(
            self,
            from_abie: "Abie",
            based_ascc_manifest_id: int,
            ascc_manifest: "AsccManifest",
            asccp_manifest: "AsccpManifest",
            role_of_acc_manifest: "AccManifest",
            owner_top_level_asbiep_id: int,
            final_cardinality_min: int,
            final_cardinality_max: int,
            asbie_path: str
    ) -> tuple["Asbie", list[str]]:
        """
        Create a new ASBIE with associated ASBIEP and ABIE.
        
        Args:
            from_abie: The parent ABIE
            based_ascc_manifest_id: The ASCC manifest ID
            ascc_manifest: The ASCC manifest
            asccp_manifest: The ASCCP manifest
            role_of_acc_manifest: The role of ACC manifest
            owner_top_level_asbiep_id: The top-level ASBIEP ID
            final_cardinality_min: Validated minimum cardinality
            final_cardinality_max: Validated maximum cardinality
            asbie_path: The calculated path for the ASBIE, retrieved from _get_abie_related_components
            
        Returns:
            tuple: (new_asbie, list of updated fields)
        """
        # Use the provided path (calculated from _get_abie_related_components in tool layer)
        # Construct ASBIEP path from ASBIE path
        asbiep_path = f"{asbie_path}>ASCCP-{asccp_manifest.asccp_manifest_id}"
        
        # Create ABIE first
        role_of_abie = self._create_abie(
            acc_manifest_id=role_of_acc_manifest.acc_manifest_id,
            top_level_asbiep_id=owner_top_level_asbiep_id,
            parent_asbiep_path=asbiep_path
        )
        
        # Create ASBIEP
        asbiep = self._create_asbiep(
            asccp_manifest_id=asccp_manifest.asccp_manifest_id,
            abie_id=role_of_abie.abie_id,
            top_level_asbiep_id=owner_top_level_asbiep_id,
            parent_asbie_path=asbie_path
        )
        
        # Update ASBIEP remark if provided
        updates = []
        
        # Create ASBIE
        asbie_guid = generate_guid()
        asbie_hash_path = self._calculate_hash_path(asbie_path)
        
        new_asbie = Asbie(
            guid=asbie_guid,
            based_ascc_manifest_id=based_ascc_manifest_id,
            path=asbie_path,
            hash_path=asbie_hash_path,
            from_abie_id=from_abie.abie_id,
            to_asbiep_id=asbiep.asbiep_id,
            cardinality_min=final_cardinality_min,
            cardinality_max=final_cardinality_max,
            is_nillable=False,
            is_used=True,
            is_deprecated=False,
            remark=None,  # ASBIE remark is separate from ASBIEP remark
            seq_key=0.0,
            created_by=self.requester.app_user_id,
            last_updated_by=self.requester.app_user_id,
            creation_timestamp=datetime.now(timezone.utc),
            last_update_timestamp=datetime.now(timezone.utc),
            owner_top_level_asbiep_id=owner_top_level_asbiep_id
        )
        
        db_add(new_asbie)
        db_flush()
        
        updates.append("asbie_id")  # Created new ASBIE
        if final_cardinality_min != ascc_manifest.ascc.cardinality_min:
            updates.append("cardinality_min")
        if final_cardinality_max != ascc_manifest.ascc.cardinality_max:
            updates.append("cardinality_max")
        updates.append("is_used")
        updates.append("is_deprecated")
        
        return (new_asbie, updates)

    def _update_asbie(
            self,
            existing_asbie: "Asbie",
            final_cardinality_min: int,
            final_cardinality_max: int,
            is_used: bool | None = None,
            is_deprecated: bool | None = None,
            definition: str | None = None,
            cardinality_min: int | None = None,
            cardinality_max: int | None = None,
            is_nillable: bool | None = None,
            remark: str | None = None
    ) -> list[str]:
        """
        Update an ASBIE and its associated ASBIEP.
        
        Args:
            existing_asbie: The existing ASBIE to update
            final_cardinality_min: Validated minimum cardinality
            final_cardinality_max: Validated maximum cardinality
            is_used: Whether the ASBIE is used. If None, will not be updated.
            is_deprecated: Whether the ASBIE is deprecated. If None, will not be updated.
            definition: Definition for the ASBIE
            cardinality_min: Original cardinality_min parameter (to track if it was provided)
            cardinality_max: Original cardinality_max parameter (to track if it was provided)
            is_nillable: Whether the ASBIE is nillable
            remark: Remark for the ASBIEP
            
        Returns:
            list: List of updated field names (only includes fields that were actually changed)
        """
        existing_asbie.last_updated_by = self.requester.app_user_id
        existing_asbie.last_update_timestamp = datetime.now(timezone.utc)
        
        updates = []
        
        # Only update is_used if it was provided and actually changed
        if is_used is not None:
            if existing_asbie.is_used != is_used:
                existing_asbie.is_used = is_used
                updates.append("is_used")

        if definition is not None:
            existing_asbie.definition = definition
            updates.append("definition")
        
        if cardinality_min is not None:
            existing_asbie.cardinality_min = final_cardinality_min
            updates.append("cardinality_min")
        
        if cardinality_max is not None:
            existing_asbie.cardinality_max = final_cardinality_max
            updates.append("cardinality_max")
        
        if is_nillable is not None:
            existing_asbie.is_nillable = is_nillable
            updates.append("is_nillable")

        # Only update is_deprecated if it was provided and actually changed
        if is_deprecated is not None:
            if existing_asbie.is_deprecated != is_deprecated:
                existing_asbie.is_deprecated = is_deprecated
                updates.append("is_deprecated")
        
        # Update ASBIEP remark if provided
        if remark is not None:
            if existing_asbie.to_asbiep_id:
                asbiep_query = select(Asbiep).where(Asbiep.asbiep_id == existing_asbie.to_asbiep_id)
                asbiep = db_exec(asbiep_query).first()
                if asbiep:
                    asbiep.remark = remark
                    asbiep.last_updated_by = self.requester.app_user_id
                    asbiep.last_update_timestamp = datetime.now(timezone.utc)
                    db_add(asbiep)
                    updates.append("remark")
        
        db_add(existing_asbie)
        return updates

    @transaction(read_only=False)
    def create_asbie(
            self,
            from_abie_id: int,
            based_ascc_manifest_id: int,
            asbie_path: str
    ) -> tuple[int, list[str]]:
        """
        Create a new ASBIE (Association Business Information Entity).
        
        Creates a new ASBIE with associated ASBIEP and ABIE records. The path and hash_path 
        columns are automatically calculated for all created records.
        
        Permission Requirements:
        - The current user must be the owner of the top-level ASBIEP
        - The top-level ASBIEP state must be 'WIP' (Work In Progress)
        
        Args:
            from_abie_id: The ABIE ID that this ASBIE originates from (parent ABIE)
            based_ascc_manifest_id: The ASCC manifest ID that this ASBIE is based on
            asbie_path: The calculated path for the ASBIE, retrieved from _get_abie_related_components
            
        Returns:
            tuple: (asbie_id, list of updated fields)
            
        Raises:
            HTTPException: If validation fails, resources are not found, user lacks permission,
                or database errors occur.
        """
        # 1) Validate and get ABIE
        from_abie, owner_top_level_asbiep_id = self._validate_and_get_abie_for_asbie_update(
            from_abie_id
        )
        
        # 2) Validate ownership and state
        self._validate_ownership_and_state_for_asbie_update(
            owner_top_level_asbiep_id
        )
        
        # 3) Validate ASCC relationship
        self._validate_ascc_relationship_for_abie(
            from_abie, based_ascc_manifest_id
        )
        
        # 4) Get ASCC/ASCCP/ACC manifests
        ascc_manifest, asccp_manifest, role_of_acc_manifest = self._get_ascc_manifest_for_asbie(
            based_ascc_manifest_id
        )
        
        # 5) Validate cardinality
        final_cardinality_min, final_cardinality_max = ascc_manifest.ascc.cardinality_min, ascc_manifest.ascc.cardinality_max
        
        # 6) Check for duplicate ASBIE before creating
        duplicate_asbie = self._check_duplicate_entity(
            Asbie, asbie_path, Asbie.based_ascc_manifest_id,
            based_ascc_manifest_id, owner_top_level_asbiep_id
        )
        
        if duplicate_asbie:
            # Use existing ASBIE instead of creating a new one
            existing_asbie = duplicate_asbie
            # Update it instead
            updates = self._update_asbie(
                existing_asbie=existing_asbie,
                final_cardinality_min=final_cardinality_min,
                final_cardinality_max=final_cardinality_max,
                is_used=True
            )
            
            # Evict cache entries for ASBIE and related queries
            evict_cache("business_information_entity.get_asbie_list", from_abie_id=from_abie_id)  # Evict ASBIE list
            evict_cache("business_information_entity.get_asbie_by_asbie_id", asbie_id=existing_asbie.asbie_id)  # Evict specific ASBIE
            evict_cache("business_information_entity.get_asbie_by_based_ascc_manifest_id")  # Evict all queries by based_ascc_manifest_id
            evict_cache("business_information_entity.get_top_level_asbiep_by_id", top_level_asbiep_id=owner_top_level_asbiep_id)  # Evict parent BIE
            evict_cache("business_information_entity.get_abie", abie_id=from_abie_id)  # Evict parent ABIE
            
            return (existing_asbie.asbie_id, updates)
        
        # 7) Create new ASBIE
        new_asbie, updates = self._create_new_asbie(
            from_abie=from_abie,
            based_ascc_manifest_id=based_ascc_manifest_id,
            ascc_manifest=ascc_manifest,
            asccp_manifest=asccp_manifest,
            role_of_acc_manifest=role_of_acc_manifest,
            owner_top_level_asbiep_id=owner_top_level_asbiep_id,
            final_cardinality_min=final_cardinality_min,
            final_cardinality_max=final_cardinality_max,
            asbie_path=asbie_path
        )
        
        # Evict cache entries for ASBIE and related queries
        evict_cache("business_information_entity.get_asbie_list", from_abie_id=from_abie_id)  # Evict ASBIE list
        evict_cache("business_information_entity.get_asbie_by_asbie_id", asbie_id=new_asbie.asbie_id)  # Evict specific ASBIE
        evict_cache("business_information_entity.get_asbie_by_based_ascc_manifest_id")  # Evict all queries by based_ascc_manifest_id
        evict_cache("business_information_entity.get_top_level_asbiep_by_id", top_level_asbiep_id=owner_top_level_asbiep_id)  # Evict parent BIE
        evict_cache("business_information_entity.get_abie", abie_id=from_abie_id)  # Evict parent ABIE
        
        return (new_asbie.asbie_id, updates)

    @transaction(read_only=False)
    def update_asbie(
            self,
            asbie_id: int,
            is_used: bool | None = None,
            is_deprecated: bool | None = None,
            is_nillable: bool | None = None,
            definition: str | None = None,
            cardinality_min: int | None = None,
            cardinality_max: int | None = None,
            remark: str | None = None
    ) -> tuple[int, list[str]]:
        """
        Update an existing ASBIE (Association Business Information Entity).
        
        Updates an existing ASBIE with the provided values. Only the provided fields will be updated.
        
        Permission Requirements:
        - The current user must be the owner of the top-level ASBIEP
        - The top-level ASBIEP state must be 'WIP' (Work In Progress)
        
        Args:
            asbie_id: The ASBIE ID to update
            is_used: Whether this ASBIE is currently being used. If not provided, will not be updated.
            is_deprecated: Whether the ASBIE is deprecated. If not provided, will not be updated.
            is_nillable: Whether the ASBIE can have a nil/null value. If not provided, will not be updated.
            definition: Definition to override the ASCC definition. If not provided, will not be updated.
            cardinality_min: Minimum cardinality. If not provided, will not be updated.
            cardinality_max: Maximum cardinality (-1 means unbounded). If not provided, will not be updated.
            remark: Additional remarks for the ASBIEP. If not provided, will not be updated.
            
        Returns:
            tuple: (asbie_id, list of updated fields)
            
        Raises:
            HTTPException: If validation fails, resources are not found, user lacks permission,
                or database errors occur.
        """
        # Validate that at least one field is provided
        if all(param is None for param in [is_used, is_deprecated, is_nillable, definition, cardinality_min, cardinality_max, remark]):
            raise HTTPException(
                status_code=400,
                detail="At least one field must be provided for update"
            )

        # 1) Get existing ASBIE
        existing_asbie = db_exec(
            select(Asbie).where(Asbie.asbie_id == asbie_id)
        ).first()
        
        if not existing_asbie:
            raise HTTPException(
                status_code=404,
                detail=f"ASBIE with ID {asbie_id} not found"
            )
        
        # 2) Get from_abie_id and based_ascc_manifest_id from existing ASBIE
        from_abie_id = existing_asbie.from_abie_id
        based_ascc_manifest_id = existing_asbie.based_ascc_manifest_id
        
        # 3) Validate and get ABIE
        from_abie, owner_top_level_asbiep_id = self._validate_and_get_abie_for_asbie_update(
            from_abie_id
        )
        
        # 4) Validate existing ASBIE
        existing_asbie = self._validate_existing_asbie_for_update(
            asbie_id, from_abie_id, based_ascc_manifest_id, owner_top_level_asbiep_id
        )
        
        # 5) Validate ownership and state
        self._validate_ownership_and_state_for_asbie_update(
            owner_top_level_asbiep_id
        )
        
        # 6) Get ASCC/ASCCP/ACC manifests
        ascc_manifest, asccp_manifest, role_of_acc_manifest = self._get_ascc_manifest_for_asbie(
            based_ascc_manifest_id
        )
        
        # 7) Validate cardinality
        final_cardinality_min, final_cardinality_max = self._validate_cardinality_for_asbie(
            ascc_manifest, cardinality_min, cardinality_max
        )
        
        # 8) Update existing ASBIE
        updates = self._update_asbie(
            existing_asbie=existing_asbie,
            final_cardinality_min=final_cardinality_min,
            final_cardinality_max=final_cardinality_max,
            is_used=is_used,
            is_deprecated=is_deprecated,
            definition=definition,
            cardinality_min=cardinality_min,
            cardinality_max=cardinality_max,
            is_nillable=is_nillable,
            remark=remark
        )
        
        # Evict cache entries for ASBIE and related queries
        evict_cache("business_information_entity.get_asbie_list", from_abie_id=from_abie_id)  # Evict ASBIE list
        evict_cache("business_information_entity.get_asbie_by_asbie_id", asbie_id=existing_asbie.asbie_id)  # Evict specific ASBIE
        evict_cache("business_information_entity.get_asbie_by_based_ascc_manifest_id")  # Evict all queries by based_ascc_manifest_id
        evict_cache("business_information_entity.get_top_level_asbiep_by_id", top_level_asbiep_id=owner_top_level_asbiep_id)  # Evict parent BIE
        evict_cache("business_information_entity.get_abie", abie_id=from_abie_id)  # Evict parent ABIE
        
        return (existing_asbie.asbie_id, updates)

    def _create_bbiep(
            self,
            bccp_manifest_id: int,
            top_level_asbiep_id: int,
            parent_bbie_path: str
    ) -> "Bbiep":
        """
        Internal helper method to create a BBIEP record within an existing session.
        
        Args:
            bccp_manifest_id: The BCCP manifest ID to base the BBIEP on
            top_level_asbiep_id: The top-level ASBIEP ID that owns this BBIEP
            parent_bbie_path: A parent path to concatenate with current path. It must end with 'BCC-{bcc_manifest_id}'
            
        Returns:
            Bbiep: The created BBIEP record
        """
        # Generate GUID
        bbiep_guid = generate_guid()

        # Generate path
        path = f"BCCP-{bccp_manifest_id}"

        # Build the full path by concatenating parent_bbie_path and current path
        full_path = f"{parent_bbie_path}>{path}"

        # Check for duplicate BBIEP
        existing_bbiep = self._check_duplicate_entity(
            Bbiep, full_path, Bbiep.based_bccp_manifest_id,
            bccp_manifest_id, top_level_asbiep_id
        )
        
        if existing_bbiep:
            # Return existing BBIEP instead of creating a new one
            return existing_bbiep

        # Create BBIEP record
        bbiep = Bbiep(
            guid=bbiep_guid,
            based_bccp_manifest_id=bccp_manifest_id,
            path=full_path,
            hash_path=self._calculate_hash_path(full_path),
            definition=None,
            remark=None,
            biz_term=None,
            display_name=None,
            created_by=self.requester.app_user_id,
            last_updated_by=self.requester.app_user_id,
            creation_timestamp=datetime.now(timezone.utc),
            last_update_timestamp=datetime.now(timezone.utc),
            owner_top_level_asbiep_id=top_level_asbiep_id
        )

        db_add(bbiep)
        db_flush()  # Get the ID without committing

        return bbiep

    def _validate_bcc_relationship_for_abie(
            self,
            from_abie: "Abie",
            based_bcc_manifest_id: int
    ) -> None:
        """
        Validate that the BCC manifest is a valid relationship for the ABIE.
        
        Args:
            from_abie: The ABIE to check relationships for
            based_bcc_manifest_id: The BCC manifest ID to validate
            
        Raises:
            HTTPException: If the BCC is not a valid relationship
        """
        # First check direct relationship
        bcc_relationship_query = (
            select(BccManifest)
            .where(BccManifest.from_acc_manifest_id == from_abie.based_acc_manifest_id)
            .where(BccManifest.bcc_manifest_id == based_bcc_manifest_id)
        )
        bcc_relationship = db_exec(bcc_relationship_query).first()
        
        # If not found directly, check inherited relationships by traversing ACC hierarchy
        if not bcc_relationship:
            acc_manifest_id = from_abie.based_acc_manifest_id
            checked_manifests = set()
            
            # Traverse the ACC hierarchy to find valid BCC relationships
            while acc_manifest_id and acc_manifest_id not in checked_manifests:
                checked_manifests.add(acc_manifest_id)
                bcc_check_query = (
                    select(BccManifest)
                    .where(BccManifest.from_acc_manifest_id == acc_manifest_id)
                    .where(BccManifest.bcc_manifest_id == based_bcc_manifest_id)
                )
                bcc_relationship = db_exec(bcc_check_query).first()
                if bcc_relationship:
                    break
                
                # Get the based_acc_manifest_id for inheritance traversal
                acc_manifest = db_get(AccManifest, acc_manifest_id)
                if acc_manifest and acc_manifest.based_acc_manifest_id:
                    acc_manifest_id = acc_manifest.based_acc_manifest_id
                else:
                    break
            
            if not bcc_relationship:
                raise HTTPException(
                    status_code=400,
                    detail=f"The BCC manifest (ID {based_bcc_manifest_id}) is not a valid relationship for this ABIE (ID {from_abie.abie_id}). "
                           f"The BCC must be available as a relationship from the ABIE's ACC. "
                           f"Please use get_abie or get_top_level_asbiep to view the available relationships, "
                           f"and choose a based_bcc_manifest_id that appears in the relationships list."
                )

    def _get_bcc_manifest_for_bbie(
            self,
            based_bcc_manifest_id: int
    ) -> tuple["BccManifest", "BccpManifest"]:
        """
        Get and validate BCC and BCCP manifests for BBIE creation.
        
        Args:
            based_bcc_manifest_id: The BCC manifest ID
            
        Returns:
            tuple: (bcc_manifest, bccp_manifest)
            
        Raises:
            HTTPException: If any manifest is not found
        """
        # Get BCC manifest information within session
        bcc_manifest_query = (
            select(BccManifest)
            .options(
                selectinload(BccManifest.bcc),
                selectinload(BccManifest.to_bccp_manifest).selectinload(BccpManifest.bccp)
            )
            .where(BccManifest.bcc_manifest_id == based_bcc_manifest_id)
        )
        bcc_manifest = db_exec(bcc_manifest_query).first()
        if not bcc_manifest:
            raise HTTPException(
                status_code=404,
                detail=f"Could not find the BCC manifest with ID {based_bcc_manifest_id}. "
                       f"Please check that the ID is correct. "
                       f"You can use get_bcc or get_acc tools to view available BCC relationships."
            )
        
        # Get BCCP manifest from BCC
        bccp_manifest = bcc_manifest.to_bccp_manifest
        if not bccp_manifest:
            raise HTTPException(
                status_code=404,
                detail=f"The BCC manifest (ID {based_bcc_manifest_id}) is missing its associated BCCP manifest. "
                       f"This appears to be a data integrity issue. Please contact your system administrator for assistance."
            )
        
        return (bcc_manifest, bccp_manifest)

    @cache(key_prefix="business_information_entity.get_dt_awd_pri")
    @transaction(read_only=True)
    def get_dt_awd_pri(
            self,
            bccp_manifest_id: int
    ) -> tuple[int | None, int | None, int | None]:
        """
        Get default manifest IDs from dt_awd_pri for the given BCCP manifest ID.
        
        The manifest IDs are retrieved by:
        1. Getting BCCP manifest from bccp_manifest_id
        2. Getting dt_manifest from bccp_manifest.bdt_manifest_id
        3. Querying dt_awd_pri with release_id, dt_id, and is_default=True
        4. Returning the xbt_manifest_id, code_list_manifest_id, and agency_id_list_manifest_id
           from the matching record (only one of these should be set)
        
        Args:
            bccp_manifest_id: The BCCP manifest ID to get manifest IDs for
            
        Returns:
            tuple: (xbt_manifest_id, code_list_manifest_id, agency_id_list_manifest_id)
                   Only one of these values should be set, others will be None
            
        Raises:
            HTTPException: If BCCP manifest is not found, BDT manifest ID is missing,
                DT manifest is not found, or no default dt_awd_pri record is found
        """
        # Get BCCP manifest
        bccp_manifest_query = (
            select(BccpManifest)
            .where(BccpManifest.bccp_manifest_id == bccp_manifest_id)
        )
        bccp_manifest = db_exec(bccp_manifest_query).first()
        
        if not bccp_manifest:
            raise HTTPException(
                status_code=404,
                detail=f"BCCP manifest with ID {bccp_manifest_id} not found. "
                       f"Cannot retrieve default manifest IDs from dt_awd_pri."
            )
        
        if not bccp_manifest.bdt_manifest_id:
            raise HTTPException(
                status_code=400,
                detail=f"BCCP manifest (ID {bccp_manifest_id}) does not have a BDT manifest ID assigned. "
                       f"Cannot retrieve default manifest IDs from dt_awd_pri."
            )
        
        # Get dt_manifest to retrieve dt_id
        dt_manifest_query = (
            select(DtManifest)
            .where(DtManifest.dt_manifest_id == bccp_manifest.bdt_manifest_id)
        )
        dt_manifest = db_exec(dt_manifest_query).first()
        
        if not dt_manifest:
            raise HTTPException(
                status_code=404,
                detail=f"DT manifest with ID {bccp_manifest.bdt_manifest_id} not found. "
                       f"Cannot retrieve default manifest IDs from dt_awd_pri."
            )
        
        # Query dt_awd_pri with release_id, dt_id, and is_default=True
        dt_awd_pri_query = (
            select(DtAwdPri)
            .where(
                DtAwdPri.release_id == bccp_manifest.release_id,
                DtAwdPri.dt_id == dt_manifest.dt_id,
                DtAwdPri.is_default == True
            )
        )
        dt_awd_pri = db_exec(dt_awd_pri_query).first()
        
        if not dt_awd_pri:
            raise HTTPException(
                status_code=404,
                detail=f"No default dt_awd_pri record found for release_id {bccp_manifest.release_id} "
                       f"and dt_id {dt_manifest.dt_id} (DT manifest ID {bccp_manifest.bdt_manifest_id}). "
                       f"A default allowed primitive must be configured for this data type."
            )
        
        return (
            dt_awd_pri.xbt_manifest_id,
            dt_awd_pri.code_list_manifest_id,
            dt_awd_pri.agency_id_list_manifest_id
        )

    @cache(key_prefix="business_information_entity.get_dt_sc_awd_pri")
    @transaction(read_only=True)
    def get_dt_sc_awd_pri(
            self,
            dt_sc_manifest_id: int
    ) -> tuple[int | None, int | None, int | None]:
        """
        Get default manifest IDs from dt_sc_awd_pri for the given DT_SC manifest ID.
        
        The manifest IDs are retrieved by:
        1. Getting DT_SC manifest from dt_sc_manifest_id
        2. Getting dt_sc from dt_sc_manifest.dt_sc
        3. Querying dt_sc_awd_pri with release_id, dt_sc_id, and is_default=True
        4. Returning the xbt_manifest_id, code_list_manifest_id, and agency_id_list_manifest_id
           from the matching record (only one of these should be set)
        
        Args:
            dt_sc_manifest_id: The DT_SC manifest ID to get manifest IDs for
            
        Returns:
            tuple: (xbt_manifest_id, code_list_manifest_id, agency_id_list_manifest_id)
                   Only one of these values should be set, others will be None
            
        Raises:
            HTTPException: If DT_SC manifest is not found, DT_SC is missing,
                or no default dt_sc_awd_pri record is found
        """
        # Get DT_SC manifest with dt_sc relationship loaded
        dt_sc_manifest_query = (
            select(DtScManifest)
            .options(selectinload(DtScManifest.dt_sc))
            .where(DtScManifest.dt_sc_manifest_id == dt_sc_manifest_id)
        )
        dt_sc_manifest = db_exec(dt_sc_manifest_query).first()
        
        if not dt_sc_manifest:
            raise HTTPException(
                status_code=404,
                detail=f"DT_SC manifest with ID {dt_sc_manifest_id} not found. "
                       f"Cannot retrieve default manifest IDs from dt_sc_awd_pri."
            )
        
        if not dt_sc_manifest.dt_sc:
            raise HTTPException(
                status_code=400,
                detail=f"DT_SC manifest (ID {dt_sc_manifest_id}) does not have a DT_SC assigned. "
                       f"Cannot retrieve default manifest IDs from dt_sc_awd_pri."
            )
        
        dt_sc_id = dt_sc_manifest.dt_sc.dt_sc_id
        
        # Query dt_sc_awd_pri with release_id, dt_sc_id, and is_default=True
        dt_sc_awd_pri_query = (
            select(DtScAwdPri)
            .where(
                DtScAwdPri.release_id == dt_sc_manifest.release_id,
                DtScAwdPri.dt_sc_id == dt_sc_id,
                DtScAwdPri.is_default == True
            )
        )
        dt_sc_awd_pri = db_exec(dt_sc_awd_pri_query).first()
        
        if not dt_sc_awd_pri:
            raise HTTPException(
                status_code=404,
                detail=f"No default dt_sc_awd_pri record found for release_id {dt_sc_manifest.release_id} "
                       f"and dt_sc_id {dt_sc_id} (DT_SC manifest ID {dt_sc_manifest_id}). "
                       f"A default allowed primitive must be configured for this supplementary component."
            )
        
        return (
            dt_sc_awd_pri.xbt_manifest_id,
            dt_sc_awd_pri.code_list_manifest_id,
            dt_sc_awd_pri.agency_id_list_manifest_id
        )

    def _validate_default_and_fixed_value(
            self,
            default_value: str | None,
            fixed_value: str | None
    ) -> None:
        """
        Validate that default_value and fixed_value are mutually exclusive.
        
        Args:
            default_value: Default value to validate
            fixed_value: Fixed value to validate
            
        Raises:
            HTTPException: If both default_value and fixed_value are provided
        """
        if default_value is not None and fixed_value is not None:
            raise HTTPException(
                status_code=400,
                detail=f"Cannot specify both default_value and fixed_value at the same time. "
                       f"They are mutually exclusive in XML. Please provide only one of them, or set both to None."
            )
    
    def _find_version_identifier_bbie(
            self,
            abie_id: int
    ) -> "Bbie | None":
        """
        Check if there is a 'Version Identifier' BBIE in the root ABIE.
        
        Args:
            abie_id: The root ABIE ID (role_of_abie of the top-level ASBIEP's ASBIEP)
            
        Returns:
            Bbie | None: The Version Identifier BBIE if found, None otherwise
        """
        # Get all BBIEs for this ABIE
        bbie_list = self.get_bbie_list(abie_id)
        
        # Check each BBIE to see if it's the Version Identifier
        for bbie in bbie_list:
            # Check if this BBIE is based on a BCC with property_term "Version Identifier"
            if bbie.based_bcc_manifest and bbie.based_bcc_manifest.to_bccp_manifest:
                bccp = bbie.based_bcc_manifest.to_bccp_manifest.bccp
                if bccp and bccp.property_term:
                    # Check if property_term matches "Version Identifier" exactly (case-insensitive)
                    if bccp.property_term.lower() == "version identifier":
                        return bbie
        
        return None
    

    def _validate_cardinality_for_bbie(
            self,
            bcc_manifest: "BccManifest",
            cardinality_min: int | None,
            cardinality_max: int | None
    ) -> tuple[int, int]:
        """
        Validate and determine final cardinality values for BBIE.
        
        Args:
            bcc_manifest: The BCC manifest to get base cardinality from
            cardinality_min: Provided minimum cardinality (optional)
            cardinality_max: Provided maximum cardinality (optional)
            
        Returns:
            tuple: (final_cardinality_min, final_cardinality_max)
            
        Raises:
            HTTPException: If validation fails
        """
        # Get the base cardinality from BCC
        base_cardinality_min = bcc_manifest.bcc.cardinality_min
        base_cardinality_max = bcc_manifest.bcc.cardinality_max
        
        # Determine final cardinality values (use provided values or defaults from BCC)
        final_cardinality_min = cardinality_min if cardinality_min is not None else base_cardinality_min
        final_cardinality_max = cardinality_max if cardinality_max is not None else base_cardinality_max
        
        # Validate cardinality_min must not be less than base cardinality_min
        if final_cardinality_min < base_cardinality_min:
            raise HTTPException(
                status_code=400,
                detail=f"The minimum cardinality ({final_cardinality_min}) is too low. "
                       f"It must be at least {base_cardinality_min} as required by the base BCC. "
                       f"Please set cardinality_min to {base_cardinality_min} or higher."
            )
        
        # Validate cardinality_max must not exceed base cardinality_max (unless base is -1 for unbounded)
        if base_cardinality_max != -1:
            if final_cardinality_max != -1 and final_cardinality_max > base_cardinality_max:
                raise HTTPException(
                    status_code=400,
                    detail=f"The maximum cardinality ({final_cardinality_max}) exceeds the limit. "
                           f"It must be at most {base_cardinality_max} as allowed by the base BCC. "
                           f"Please set cardinality_max to {base_cardinality_max} or lower, or use -1 for unbounded if needed."
                )
        
        # Validate cardinality_min must be <= cardinality_max
        if final_cardinality_max != -1:
            if final_cardinality_min > final_cardinality_max:
                raise HTTPException(
                    status_code=400,
                    detail=f"The minimum cardinality ({final_cardinality_min}) cannot be greater than the maximum cardinality ({final_cardinality_max}). "
                           f"Please adjust the values so that the minimum is less than or equal to the maximum. "
                           f"For example, set cardinality_max to at least {final_cardinality_min}, or reduce cardinality_min."
                )
        
        return (final_cardinality_min, final_cardinality_max)

    def _validate_existing_bbie_for_update(
            self,
            bbie_id: int,
            from_abie_id: int,
            based_bcc_manifest_id: int,
            owner_top_level_asbiep_id: int
    ) -> "Bbie":
        """
        Validate an existing BBIE for update operation.
        
        Args:
            bbie_id: The BBIE ID to update
            from_abie_id: The expected from_abie_id
            based_bcc_manifest_id: The expected based_bcc_manifest_id
            owner_top_level_asbiep_id: The expected owner_top_level_asbiep_id
            
        Returns:
            Bbie: The validated BBIE
            
        Raises:
            HTTPException: If BBIE not found or validation fails
        """
        existing_bbie_query = select(Bbie).where(Bbie.bbie_id == bbie_id)
        existing_bbie = db_exec(existing_bbie_query).first()
        if not existing_bbie:
            raise HTTPException(
                status_code=404,
                detail=f"Could not find the BBIE with ID {bbie_id}. "
                       f"Please check that the ID is correct. "
                       f"If you want to create a new BBIE instead, simply omit the bbie_id parameter."
            )
        
        # Verify it belongs to the same top-level ASBIEP
        if existing_bbie.owner_top_level_asbiep_id != owner_top_level_asbiep_id:
            raise HTTPException(
                status_code=400,
                detail=f"The BBIE (ID {bbie_id}) and ABIE (ID {from_abie_id}) belong to different top-level ASBIEPs. "
                       f"Please use a bbie_id that belongs to the same top-level ASBIEP as the from_abie_id, "
                       f"or use a different from_abie_id that matches the BBIE's top-level ASBIEP."
            )
        
        # Verify it belongs to the same from_abie_id
        if existing_bbie.from_abie_id != from_abie_id:
            raise HTTPException(
                status_code=400,
                detail=f"The BBIE (ID {bbie_id}) does not belong to the ABIE (ID {from_abie_id}). "
                       f"Please use a from_abie_id that matches the BBIE's parent ABIE, "
                       f"or use a different bbie_id that belongs to the specified ABIE."
            )
        
        # Verify it matches the based_bcc_manifest_id
        if existing_bbie.based_bcc_manifest_id != based_bcc_manifest_id:
            raise HTTPException(
                status_code=400,
                detail=f"The BBIE (ID {bbie_id}) is not based on the BCC manifest (ID {based_bcc_manifest_id}). "
                       f"Please use a based_bcc_manifest_id that matches the BBIE's BCC, "
                       f"or use a different bbie_id that is based on the specified BCC manifest."
            )
        
        return existing_bbie

    def _create_new_bbie(
            self,
            from_abie: "Abie",
            based_bcc_manifest_id: int,
            bcc_manifest: "BccManifest",
            bccp_manifest: "BccpManifest",
            owner_top_level_asbiep_id: int,
            final_cardinality_min: int,
            final_cardinality_max: int,
            bbie_path: str
    ) -> tuple["Bbie", list[str]]:
        """
        Create a new BBIE with associated BBIEP.
        
        Args:
            from_abie: The parent ABIE
            based_bcc_manifest_id: The BCC manifest ID
            bcc_manifest: The BCC manifest
            bccp_manifest: The BCCP manifest
            owner_top_level_asbiep_id: The top-level ASBIEP ID
            final_cardinality_min: Validated minimum cardinality
            final_cardinality_max: Validated maximum cardinality
            bbie_path: The calculated path for the BBIE, retrieved from _get_abie_related_components
            
        Returns:
            tuple: (new_bbie, list of updated fields)
        """
        # Create BBIEP
        bbiep = self._create_bbiep(
            bccp_manifest_id=bccp_manifest.bccp_manifest_id,
            top_level_asbiep_id=owner_top_level_asbiep_id,
            parent_bbie_path=bbie_path
        )
        
        # Update BBIEP remark if provided
        updates = []
        
        # Get default manifest IDs from dt_awd_pri
        xbt_manifest_id, code_list_manifest_id, agency_id_list_manifest_id = self.get_dt_awd_pri(bccp_manifest.bccp_manifest_id)
        
        # Create BBIE
        bbie_guid = generate_guid()
        bbie_hash_path = self._calculate_hash_path(bbie_path)
        
        final_is_nillable = False
        
        # Cascade default_value and fixed_value from BCC if they exist (mutually exclusive)
        cascaded_default_value = None
        cascaded_fixed_value = None
        if bcc_manifest.bcc.fixed_value is not None:
            cascaded_fixed_value = bcc_manifest.bcc.fixed_value
        elif bcc_manifest.bcc.default_value is not None:
            cascaded_default_value = bcc_manifest.bcc.default_value
        
        new_bbie = Bbie(
            guid=bbie_guid,
            based_bcc_manifest_id=based_bcc_manifest_id,
            path=bbie_path,
            hash_path=bbie_hash_path,
            from_abie_id=from_abie.abie_id,
            to_bbiep_id=bbiep.bbiep_id,
            xbt_manifest_id=xbt_manifest_id,
            code_list_manifest_id=code_list_manifest_id,
            agency_id_list_manifest_id=agency_id_list_manifest_id,
            cardinality_min=final_cardinality_min,
            cardinality_max=final_cardinality_max,
            is_nillable=False,
            is_null=False,
            default_value=cascaded_default_value,
            fixed_value=cascaded_fixed_value,
            created_by=self.requester.app_user_id,
            last_updated_by=self.requester.app_user_id,
            creation_timestamp=datetime.now(timezone.utc),
            last_update_timestamp=datetime.now(timezone.utc),
            seq_key=0.0,
            is_used=True,
            is_deprecated=False,
            owner_top_level_asbiep_id=owner_top_level_asbiep_id
        )
        
        db_add(new_bbie)
        db_flush()
        
        updates.append("bbie_id")  # Created new BBIE
        if xbt_manifest_id is not None:
            updates.append("xbt_manifest_id")
        if code_list_manifest_id is not None:
            updates.append("code_list_manifest_id")
        if agency_id_list_manifest_id is not None:
            updates.append("agency_id_list_manifest_id")
        if final_cardinality_min != bcc_manifest.bcc.cardinality_min:
            updates.append("cardinality_min")
        if final_cardinality_max != bcc_manifest.bcc.cardinality_max:
            updates.append("cardinality_max")
        if cascaded_default_value is not None:
            updates.append("default_value")
        if cascaded_fixed_value is not None:
            updates.append("fixed_value")
        updates.append("is_used")
        updates.append("is_deprecated")
        
        return (new_bbie, updates)

    def _update_bbie(
            self,
            existing_bbie: "Bbie",
            final_cardinality_min: int,
            final_cardinality_max: int,
            is_used: bool | None = None,
            is_deprecated: bool | None = None,
            is_nillable: bool = False,
            cardinality_min: int | None = None,
            cardinality_max: int | None = None,
            definition: str | None = None,
            remark: str | None = None,
            example: str | None = None,
            default_value: str | None = ...,
            fixed_value: str | None = ...,
            facet_min_length: int | None = None,
            facet_max_length: int | None = None,
            facet_pattern: str | None = None,
            bcc_manifest: "BccManifest | None" = None
    ) -> list[str]:
        """
        Update a BBIE and its associated BBIEP.
        
        Args:
            existing_bbie: The existing BBIE to update
            final_cardinality_min: Validated minimum cardinality
            final_cardinality_max: Validated maximum cardinality
            is_used: Whether the BBIE is used. If None, will not be updated.
            is_deprecated: Whether the BBIE is deprecated. If None, will not be updated.
            is_nillable: Whether the BBIE is nillable
            cardinality_min: Original cardinality_min parameter (to track if it was provided)
            cardinality_max: Original cardinality_max parameter (to track if it was provided)
            definition: Definition for the BBIE
            remark: Remark for the BBIEP
            example: Example for the BBIE
            default_value: Default value for the BBIE
            fixed_value: Fixed value for the BBIE
            facet_min_length: Minimum length facet
            facet_max_length: Maximum length facet
            facet_pattern: Pattern facet
            
        Returns:
            list: List of updated field names (only includes fields that were actually changed)
        """
        existing_bbie.last_updated_by = self.requester.app_user_id
        existing_bbie.last_update_timestamp = datetime.now(timezone.utc)
        
        updates = []
        
        # Update fields if provided
        # Always update if parameter is provided, regardless of current value
        if definition is not None:
            existing_bbie.definition = definition
            updates.append("definition")

        if example is not None:
            existing_bbie.example = example
            updates.append("example")
        
        if cardinality_min is not None:
            existing_bbie.cardinality_min = final_cardinality_min
            updates.append("cardinality_min")
        
        if cardinality_max is not None:
            existing_bbie.cardinality_max = final_cardinality_max
            updates.append("cardinality_max")
        
        if is_nillable is not None:
            existing_bbie.is_nillable = is_nillable
            updates.append("is_nillable")
        
        # Handle default_value and fixed_value (mutually exclusive)
        # Check if parameters were explicitly provided (not Ellipsis)
        default_value_provided = default_value is not ...
        fixed_value_provided = fixed_value is not ...
        
        # Normalize ellipsis to None for easier handling
        if default_value is ...:
            default_value = None
        if fixed_value is ...:
            fixed_value = None
        
        # Check if this is a Version Identifier BBIE (special case - allows user to override fixed_value)
        is_version_identifier = False
        if bcc_manifest is not None and bcc_manifest.to_bccp_manifest and bcc_manifest.to_bccp_manifest.bccp:
            bccp = bcc_manifest.to_bccp_manifest.bccp
            if bccp.property_term and bccp.property_term.lower() == "version identifier":
                is_version_identifier = True
                logger.debug(f"Version Identifier BBIE detected - allowing fixed_value override")
        
        # If BCC has fixed_value, prevent changes and cascade it
        # Exception: Version Identifier BBIE allows user to override fixed_value for version syncing
        if bcc_manifest is not None and bcc_manifest.bcc.fixed_value is not None and not is_version_identifier:
            # Prevent setting default_value when BCC has fixed_value
            if default_value is not None:
                raise HTTPException(
                    status_code=400,
                    detail=f"Cannot set default_value because the original value constraint (BCC) uses a fixed value of '{bcc_manifest.bcc.fixed_value}'. "
                           f"The fixed_value constraint takes precedence and cannot be overridden with a default_value."
                )
            # Prevent clearing fixed_value when BCC has fixed_value (user explicitly passed fixed_value=None)
            if fixed_value_provided and fixed_value is None:
                raise HTTPException(
                    status_code=400,
                    detail=f"Cannot clear fixed_value because the original value constraint (BCC) uses a fixed value of '{bcc_manifest.bcc.fixed_value}'. "
                           f"The fixed_value cannot be cleared when the base BCC has a fixed value constraint."
                )
            # Prevent changing fixed_value to a different value
            if fixed_value is not None and fixed_value != bcc_manifest.bcc.fixed_value:
                raise HTTPException(
                    status_code=400,
                    detail=f"Cannot change fixed_value because the original value constraint (BCC) uses a fixed value of '{bcc_manifest.bcc.fixed_value}'. "
                           f"The fixed_value cannot be modified when the base BCC has a fixed value constraint."
                )
            # Cascade fixed_value from BCC if it's not already set correctly (and user didn't explicitly provide it)
            if not fixed_value_provided and existing_bbie.fixed_value != bcc_manifest.bcc.fixed_value:
                fixed_value = bcc_manifest.bcc.fixed_value
                fixed_value_provided = True  # Mark as provided so it gets set
        
        # Cascade default_value from BCC if user didn't provide one and BCC has it (and BCC doesn't have fixed_value)
        # Don't cascade if user explicitly provided fixed_value (they are mutually exclusive)
        if not default_value_provided and not fixed_value_provided and bcc_manifest is not None and bcc_manifest.bcc.default_value is not None and bcc_manifest.bcc.fixed_value is None:
            # Only cascade if the current value is different from the BCC's default_value
            if existing_bbie.default_value != bcc_manifest.bcc.default_value:
                default_value = bcc_manifest.bcc.default_value
                default_value_provided = True  # Mark as provided so it gets set
        
        # Handle explicit None values (user wants to clear) or provided values
        # If both are provided, prioritize fixed_value (user explicitly provided it)
        if default_value_provided and not fixed_value_provided:
            # Only handle default_value if fixed_value was not explicitly provided
            if default_value is not None:
                existing_bbie.default_value = default_value
                updates.append("default_value")
                # Always clear fixed_value when default_value is set (mutually exclusive)
                if existing_bbie.fixed_value is not None:
                    existing_bbie.fixed_value = None
                    updates.append("fixed_value")
            else:
                # User explicitly passed default_value=None to clear it (default_value can be changed)
                if existing_bbie.default_value is not None:
                    existing_bbie.default_value = None
                    updates.append("default_value")
        elif fixed_value_provided:
            if fixed_value is not None:
                existing_bbie.fixed_value = fixed_value
                updates.append("fixed_value")
                # Always clear default_value when fixed_value is set (mutually exclusive)
                if existing_bbie.default_value is not None:
                    existing_bbie.default_value = None
                    updates.append("default_value")
            else:
                # User explicitly passed fixed_value=None to clear it (only if BCC doesn't have fixed_value)
                # This case is already handled above - if BCC has fixed_value, we raise an error
                if existing_bbie.fixed_value is not None:
                    existing_bbie.fixed_value = None
                    updates.append("fixed_value")
        
        if facet_min_length is not None:
            existing_bbie.facet_min_length = facet_min_length
            updates.append("facet_min_length")
        
        if facet_max_length is not None:
            existing_bbie.facet_max_length = facet_max_length
            updates.append("facet_max_length")
        
        if facet_pattern is not None:
            existing_bbie.facet_pattern = facet_pattern
            updates.append("facet_pattern")
        
        # Only update is_used if it was provided and actually changed
        if is_used is not None:
            if existing_bbie.is_used != is_used:
                existing_bbie.is_used = is_used
                updates.append("is_used")
        
        # Only update is_deprecated if it was provided and actually changed
        if is_deprecated is not None:
            if existing_bbie.is_deprecated != is_deprecated:
                existing_bbie.is_deprecated = is_deprecated
                updates.append("is_deprecated")
        
        # Update BBIEP remark if provided
        if remark is not None:
            if existing_bbie.to_bbiep_id:
                bbiep_query = select(Bbiep).where(Bbiep.bbiep_id == existing_bbie.to_bbiep_id)
                bbiep = db_exec(bbiep_query).first()
                if bbiep:
                    bbiep.remark = remark
                    bbiep.last_updated_by = self.requester.app_user_id
                    bbiep.last_update_timestamp = datetime.now(timezone.utc)
                    db_add(bbiep)
                    updates.append("remark")
        
        
        db_add(existing_bbie)
        return updates

    @transaction(read_only=False)
    def create_bbie(
            self,
            from_abie_id: int,
            based_bcc_manifest_id: int,
            bbie_path: str
    ) -> tuple[int, list[str]]:
        """
        Create a new BBIE (Basic Business Information Entity).
        
        Creates a new BBIE with associated BBIEP record. The path and hash_path columns are
        automatically calculated for all created records.
        
        Permission Requirements:
        - The current user must be the owner of the top-level ASBIEP
        - The top-level ASBIEP state must be 'WIP' (Work In Progress)
        
        Args:
            from_abie_id: The ABIE ID that this BBIE originates from (parent ABIE)
            based_bcc_manifest_id: The BCC manifest ID that this BBIE is based on
            bbie_path: The calculated path for the BBIE, retrieved from _get_abie_related_components
            
        Returns:
            tuple: (bbie_id, list of updated fields)
            
        Raises:
            HTTPException: If validation fails, resources are not found, user lacks permission,
                or database errors occur.
        """
        # 1) Validate and get ABIE
        from_abie, owner_top_level_asbiep_id = self._validate_and_get_abie_for_asbie_update(
            from_abie_id
        )
        
        # 2) Validate ownership and state (reuse ASBIE method)
        self._validate_ownership_and_state_for_asbie_update(
            owner_top_level_asbiep_id
        )
        
        # 3) Validate BCC relationship
        self._validate_bcc_relationship_for_abie(
            from_abie, based_bcc_manifest_id
        )
        
        # 4) Get BCC/BCCP manifests
        bcc_manifest, bccp_manifest = self._get_bcc_manifest_for_bbie(
            based_bcc_manifest_id
        )
        
        # 5) Validate cardinality
        final_cardinality_min, final_cardinality_max = bcc_manifest.bcc.cardinality_min, bcc_manifest.bcc.cardinality_max
        
        # 6) Check for duplicate BBIE before creating
        duplicate_bbie = self._check_duplicate_entity(
            Bbie, bbie_path, Bbie.based_bcc_manifest_id,
            based_bcc_manifest_id, owner_top_level_asbiep_id
        )
        
        if duplicate_bbie:
            # Use existing BBIE instead of creating a new one
            existing_bbie = duplicate_bbie
            # Update it instead
            updates = self._update_bbie(
                existing_bbie=existing_bbie,
                final_cardinality_min=final_cardinality_min,
                final_cardinality_max=final_cardinality_max,
                is_used=True,
                default_value=...,
                fixed_value=...,
                bcc_manifest=bcc_manifest
            )
            
            # Evict cache entries for BBIE and related queries
            evict_cache("business_information_entity.get_bbie_list", from_abie_id=from_abie_id)  # Evict BBIE list
            evict_cache("business_information_entity.get_bbie_by_bbie_id", bbie_id=existing_bbie.bbie_id)  # Evict specific BBIE
            evict_cache("business_information_entity.get_bbie_by_based_bcc_manifest_id")  # Evict all queries by based_bcc_manifest_id
            evict_cache("business_information_entity.get_top_level_asbiep_by_id", top_level_asbiep_id=owner_top_level_asbiep_id)  # Evict parent BIE
            evict_cache("business_information_entity.get_abie", abie_id=from_abie_id)  # Evict parent ABIE
            
            return (existing_bbie.bbie_id, updates)
        
        # 7) Create new BBIE
        new_bbie, updates = self._create_new_bbie(
            from_abie=from_abie,
            based_bcc_manifest_id=based_bcc_manifest_id,
            bcc_manifest=bcc_manifest,
            bccp_manifest=bccp_manifest,
            owner_top_level_asbiep_id=owner_top_level_asbiep_id,
            final_cardinality_min=final_cardinality_min,
            final_cardinality_max=final_cardinality_max,
            bbie_path=bbie_path
        )
        
        # Evict cache entries for BBIE and related queries
        evict_cache("business_information_entity.get_bbie_list", from_abie_id=from_abie_id)  # Evict BBIE list
        evict_cache("business_information_entity.get_bbie_by_bbie_id", bbie_id=new_bbie.bbie_id)  # Evict specific BBIE
        evict_cache("business_information_entity.get_bbie_by_based_bcc_manifest_id")  # Evict all queries by based_bcc_manifest_id
        evict_cache("business_information_entity.get_top_level_asbiep_by_id", top_level_asbiep_id=owner_top_level_asbiep_id)  # Evict parent BIE
        evict_cache("business_information_entity.get_abie", abie_id=from_abie_id)  # Evict parent ABIE
        
        return (new_bbie.bbie_id, updates)

    @transaction(read_only=False)
    def update_bbie(
            self,
            bbie_id: int,
            is_used: bool | None = None,
            is_deprecated: bool | None = None,
            is_nillable: bool | None = None,
            cardinality_min: int | None = None,
            cardinality_max: int | None = None,
            definition: str | None = None,
            remark: str | None = None,
            example: str | None = None,
            default_value: str | None = ...,
            fixed_value: str | None = ...,
            facet_min_length: int | None = None,
            facet_max_length: int | None = None,
            facet_pattern: str | None = None
    ) -> tuple[int, list[str]]:
        """
        Update an existing BBIE (Basic Business Information Entity).
        
        Updates an existing BBIE with the provided values. Only the provided fields will be updated.
        
        Permission Requirements:
        - The current user must be the owner of the top-level ASBIEP
        - The top-level ASBIEP state must be 'WIP' (Work In Progress)
        
        Args:
            bbie_id: The BBIE ID to update
            is_used: Whether this BBIE is currently being used. If not provided, will not be updated.
            is_deprecated: Whether the BBIE is deprecated. If not provided, will not be updated.
            is_nillable: Whether the BBIE can have a nil/null value. If not provided, will not be updated.
            cardinality_min: Minimum cardinality. If not provided, will not be updated.
            cardinality_max: Maximum cardinality (-1 means unbounded). If not provided, will not be updated.
            definition: Definition to override the BCC definition. If not provided, will not be updated.
            remark: Additional remarks for the BBIEP. If not provided, will not be updated.
            example: Example for the BBIE. If not provided, will not be updated.
            default_value: Default value for the BBIE (mutually exclusive with fixed_value). If not provided, will not be updated.
            fixed_value: Fixed value for the BBIE (mutually exclusive with default_value). If not provided, will not be updated.
            facet_min_length: Minimum length facet for string types. If not provided, will not be updated.
            facet_max_length: Maximum length facet for string types. If not provided, will not be updated.
            facet_pattern: Pattern facet (regex) for string types. If not provided, will not be updated.
            
        Returns:
            tuple: (bbie_id, list of updated fields)
            
        Raises:
            HTTPException: If validation fails, resources are not found, user lacks permission,
                or database errors occur.
        """
        # Validate that at least one field is provided
        # Convert ellipsis to None for validation check
        default_value_for_validation = None if default_value is ... else default_value
        fixed_value_for_validation = None if fixed_value is ... else fixed_value
        if all(param is None for param in [is_used, is_deprecated, is_nillable, cardinality_min, cardinality_max, definition, remark, example, default_value_for_validation, fixed_value_for_validation, facet_min_length, facet_max_length, facet_pattern]):
            raise HTTPException(
                status_code=400,
                detail="At least one field must be provided for update"
            )

        # Validate default_value and fixed_value are mutually exclusive
        default_value_for_validation = None if default_value is ... else default_value
        fixed_value_for_validation = None if fixed_value is ... else fixed_value
        self._validate_default_and_fixed_value(default_value_for_validation, fixed_value_for_validation)
        
        # 1) Get existing BBIE
        existing_bbie = db_exec(
            select(Bbie).where(Bbie.bbie_id == bbie_id)
        ).first()
        
        if not existing_bbie:
            raise HTTPException(
                status_code=404,
                detail=f"BBIE with ID {bbie_id} not found"
            )
        
        # 2) Get from_abie_id and based_bcc_manifest_id from existing BBIE
        from_abie_id = existing_bbie.from_abie_id
        based_bcc_manifest_id = existing_bbie.based_bcc_manifest_id
        
        # 3) Validate and get ABIE
        from_abie, owner_top_level_asbiep_id = self._validate_and_get_abie_for_asbie_update(
            from_abie_id
        )
        
        # 4) Validate existing BBIE
        existing_bbie = self._validate_existing_bbie_for_update(
            bbie_id, from_abie_id, based_bcc_manifest_id, owner_top_level_asbiep_id
        )
        
        # 5) Validate ownership and state (reuse ASBIE method)
        self._validate_ownership_and_state_for_asbie_update(
            owner_top_level_asbiep_id
        )
        
        # 6) Get BCC/BCCP manifests
        bcc_manifest, bccp_manifest = self._get_bcc_manifest_for_bbie(
            based_bcc_manifest_id
        )
        
        # 7) Validate cardinality
        final_cardinality_min, final_cardinality_max = self._validate_cardinality_for_bbie(
            bcc_manifest, cardinality_min, cardinality_max
        )
        
        # 8) Update existing BBIE
        # Pass parameters as-is (sentinel for not provided, None for explicitly None, str for value)
        logger.debug(f"update_bbie calling _update_bbie with fixed_value={fixed_value}, fixed_value is ... = {fixed_value is ...}")
        updates = self._update_bbie(
            existing_bbie=existing_bbie,
            final_cardinality_min=final_cardinality_min,
            final_cardinality_max=final_cardinality_max,
            is_used=is_used,
            is_deprecated=is_deprecated,
            is_nillable=is_nillable,
            cardinality_min=cardinality_min,
            cardinality_max=cardinality_max,
            definition=definition,
            remark=remark,
            example=example,
            default_value=default_value,
            fixed_value=fixed_value,
            facet_min_length=facet_min_length,
            facet_max_length=facet_max_length,
            facet_pattern=facet_pattern,
            bcc_manifest=bcc_manifest
        )
        logger.debug(f"update_bbie returned updates: {updates}")
        
        # Evict cache entries for BBIE and related queries
        evict_cache("business_information_entity.get_bbie_list", from_abie_id=from_abie_id)  # Evict BBIE list
        evict_cache("business_information_entity.get_bbie_by_bbie_id", bbie_id=existing_bbie.bbie_id)  # Evict specific BBIE
        evict_cache("business_information_entity.get_bbie_by_based_bcc_manifest_id")  # Evict all queries by based_bcc_manifest_id
        evict_cache("business_information_entity.get_top_level_asbiep_by_id", top_level_asbiep_id=owner_top_level_asbiep_id)  # Evict parent BIE
        evict_cache("business_information_entity.get_abie", abie_id=from_abie_id)  # Evict parent ABIE
        
        return (existing_bbie.bbie_id, updates)

    @cache(key_prefix="business_information_entity.get_bbie_by_bbie_id")
    @transaction(read_only=True)
    def get_bbie_by_bbie_id(self, bbie_id: int) -> "Bbie | None":
        """
        Get a single BBIE (Basic Business Information Entity) by its BBIE ID.
        
        Args:
            bbie_id: The BBIE ID
            
        Returns:
            Bbie | None: The BBIE if found, None otherwise
        """
        query = (
            select(Bbie)
            .options(
                selectinload(Bbie.based_bcc_manifest).selectinload(BccManifest.bcc),
                selectinload(Bbie.based_bcc_manifest).selectinload(
                    BccManifest.to_bccp_manifest).selectinload(BccpManifest.bccp),
                selectinload(Bbie.to_bbiep),
                selectinload(Bbie.owner_top_level_asbiep)
            )
            .where(Bbie.bbie_id == bbie_id)
        )
        bbie = db_exec(query).first()
        return bbie

    @cache(key_prefix="business_information_entity.get_bbie_by_based_bcc_manifest_id")
    @transaction(read_only=True)
    def get_bbie_by_based_bcc_manifest_id(
            self,
            based_bcc_manifest_id: int,
            owner_top_level_asbiep_id: int,
            hash_path: str | None = None
    ) -> "Bbie | None":
        """
        Get a single BBIE (Basic Business Information Entity) by its based BCC manifest ID.
        
        Args:
            based_bcc_manifest_id: The BCC manifest ID that this BBIE is based on
            owner_top_level_asbiep_id: The top-level ASBIEP ID that owns this BBIE
            hash_path: Optional hash_path for more precise matching. If provided, filters by hash_path as well.
            
        Returns:
            Bbie | None: The BBIE if found, None otherwise
        """
        query = (
            select(Bbie)
            .options(
                selectinload(Bbie.based_bcc_manifest).selectinload(BccManifest.bcc),
                selectinload(Bbie.based_bcc_manifest).selectinload(
                    BccManifest.to_bccp_manifest).selectinload(BccpManifest.bccp),
                selectinload(Bbie.to_bbiep),
                selectinload(Bbie.owner_top_level_asbiep)
            )
            .where(
                Bbie.based_bcc_manifest_id == based_bcc_manifest_id,
                Bbie.owner_top_level_asbiep_id == owner_top_level_asbiep_id
            )
        )
        if hash_path:
            query = query.where(Bbie.hash_path == hash_path)
        
        bbie = db_exec(query).first()
        return bbie

    @cache(key_prefix="business_information_entity.get_bbie_sc_list")
    @transaction(read_only=True)
    def get_bbie_sc_list(self, bbie_id: int) -> list["BbieSc"]:
        """
        Get all BBIE_SC (Basic Business Information Entity Supplementary Component) records by BBIE ID.

        Args:
            bbie_id: The BBIE ID

        Returns:
            list[BbieSc]: List of BBIE_SC records associated with the BBIE
        """
        query = (
            select(BbieSc)
            .options(
                selectinload(BbieSc.based_dt_sc_manifest).selectinload(DtScManifest.dt_sc),
                selectinload(BbieSc.owner_top_level_asbiep)
            )
            .where(BbieSc.bbie_id == bbie_id)
        )
        bbie_scs = db_exec(query).all()
        return bbie_scs

    @transaction(read_only=False)
    def create_bbie_sc(
            self,
            bbie_id: int,
            based_dt_sc_manifest_id: int,
            bbie_sc_path: str
    ) -> tuple[int, list[str]]:
        """
        Create a new BBIE_SC (Basic Business Information Entity Supplementary Component).
        
        Creates a new BBIE_SC record. The path and hash_path columns are automatically calculated.
        
        Permission Requirements:
        - The current user must be the owner of the top-level ASBIEP
        - The top-level ASBIEP state must be 'WIP' (Work In Progress)
        
        Args:
            bbie_id: The BBIE ID that this BBIE_SC belongs to
            based_dt_sc_manifest_id: The DT_SC manifest ID that this BBIE_SC is based on
            bbie_sc_path: The calculated path for the BBIE_SC
            
        Returns:
            tuple: (bbie_sc_id, list of updated fields)
            
        Raises:
            HTTPException: If validation fails, resources are not found, user lacks permission,
                or database errors occur.
        """
        # 1) Get and validate BBIE
        bbie = db_exec(
            select(Bbie)
            .options(
                selectinload(Bbie.based_bcc_manifest).selectinload(BccManifest.to_bccp_manifest).selectinload(BccpManifest.bdt_manifest),
                selectinload(Bbie.owner_top_level_asbiep)
            )
            .where(Bbie.bbie_id == bbie_id)
        ).first()
        
        if not bbie:
            raise HTTPException(
                status_code=404,
                detail=f"BBIE with ID {bbie_id} not found"
            )
        
        owner_top_level_asbiep_id = bbie.owner_top_level_asbiep_id
        
        # 2) Validate ownership and state
        self._validate_ownership_and_state_for_asbie_update(
            owner_top_level_asbiep_id
        )
        
        # 3) Get and validate DT_SC manifest
        dt_sc_manifest = db_exec(
            select(DtScManifest)
            .options(
                selectinload(DtScManifest.dt_sc),
                selectinload(DtScManifest.owner_dt_manifest)
            )
            .where(DtScManifest.dt_sc_manifest_id == based_dt_sc_manifest_id)
        ).first()
        
        if not dt_sc_manifest:
            raise HTTPException(
                status_code=404,
                detail=f"DT_SC manifest with ID {based_dt_sc_manifest_id} not found"
            )
        
        # 4) Validate DT_SC belongs to the BDT of the BBIE's BCCP
        bccp_manifest = bbie.based_bcc_manifest.to_bccp_manifest
        if not bccp_manifest:
            raise HTTPException(
                status_code=404,
                detail=f"BCCP manifest not found for BBIE {bbie_id}"
            )
        
        bdt_manifest_id = bccp_manifest.bdt_manifest_id
        if dt_sc_manifest.owner_dt_manifest_id != bdt_manifest_id:
            raise HTTPException(
                status_code=400,
                detail=f"DT_SC manifest {based_dt_sc_manifest_id} does not belong to the BDT (ID {bdt_manifest_id}) "
                       f"of the BCCP associated with BBIE {bbie_id}"
            )
        
        # 5) Check for duplicate BBIE_SC before creating
        bbie_sc_hash_path = self._calculate_hash_path(bbie_sc_path)
        duplicate_bbie_sc_query = (
            select(BbieSc)
            .options(
                selectinload(BbieSc.based_dt_sc_manifest).selectinload(DtScManifest.dt_sc)
            )
            .where(
                BbieSc.bbie_id == bbie_id,
                BbieSc.based_dt_sc_manifest_id == based_dt_sc_manifest_id,
                BbieSc.hash_path == bbie_sc_hash_path,
                BbieSc.owner_top_level_asbiep_id == owner_top_level_asbiep_id
            )
        )
        duplicate_bbie_sc = db_exec(duplicate_bbie_sc_query).first()
        
        # 6) Get cardinality from DT_SC
        dt_sc = dt_sc_manifest.dt_sc
        final_cardinality_min = dt_sc.cardinality_min
        final_cardinality_max = dt_sc.cardinality_max if dt_sc.cardinality_max is not None else -1
        
        # 7) Get default manifest IDs from dt_sc_awd_pri
        xbt_manifest_id, code_list_manifest_id, agency_id_list_manifest_id = self.get_dt_sc_awd_pri(dt_sc_manifest.dt_sc_manifest_id)
        
        if duplicate_bbie_sc:
            # Use existing BBIE_SC instead of creating a new one
            existing_bbie_sc = duplicate_bbie_sc
            # Update it instead
            updates = self._update_bbie_sc(
                existing_bbie_sc=existing_bbie_sc,
                final_cardinality_min=final_cardinality_min,
                final_cardinality_max=final_cardinality_max,
                is_used=True,
                default_value=...,
                fixed_value=...,
                xbt_manifest_id=xbt_manifest_id,
                code_list_manifest_id=code_list_manifest_id,
                agency_id_list_manifest_id=agency_id_list_manifest_id
            )
            # Evict cache entries for BBIE_SC list
            evict_cache("business_information_entity.get_bbie_sc_list", bbie_id=bbie_id)  # Evict BBIE_SC list
            return (existing_bbie_sc.bbie_sc_id, updates)
        
        # 8) Create new BBIE_SC
        new_bbie_sc, updates = self._create_new_bbie_sc(
            bbie_id=bbie_id,
            based_dt_sc_manifest_id=based_dt_sc_manifest_id,
            dt_sc_manifest=dt_sc_manifest,
            owner_top_level_asbiep_id=owner_top_level_asbiep_id,
            final_cardinality_min=final_cardinality_min,
            final_cardinality_max=final_cardinality_max,
            bbie_sc_path=bbie_sc_path,
            bbie_sc_hash_path=bbie_sc_hash_path,
            xbt_manifest_id=xbt_manifest_id,
            code_list_manifest_id=code_list_manifest_id,
            agency_id_list_manifest_id=agency_id_list_manifest_id
        )
        
        # Evict cache entries for BBIE_SC list
        evict_cache("business_information_entity.get_bbie_sc_list", bbie_id=bbie_id)  # Evict BBIE_SC list
        
        return (new_bbie_sc.bbie_sc_id, updates)

    @transaction(read_only=False)
    def update_bbie_sc(
            self,
            bbie_sc_id: int,
            is_used: bool | None = None,
            is_deprecated: bool | None = None,
            cardinality_min: int | None = None,
            cardinality_max: int | None = None,
            definition: str | None = None,
            remark: str | None = None,
            example: str | None = None,
            default_value: str | None = ...,
            fixed_value: str | None = ...,
            facet_min_length: int | None = None,
            facet_max_length: int | None = None,
            facet_pattern: str | None = None
    ) -> tuple[int, list[str]]:
        """
        Update an existing BBIE_SC (Basic Business Information Entity Supplementary Component).
        
        Updates an existing BBIE_SC with the provided values. Only the provided fields will be updated.
        
        Permission Requirements:
        - The current user must be the owner of the top-level ASBIEP
        - The top-level ASBIEP state must be 'WIP' (Work In Progress)
        
        Args:
            bbie_sc_id: The BBIE_SC ID to update
            is_used: Whether this BBIE_SC is currently being used. If not provided, will not be updated.
            is_deprecated: Whether the BBIE_SC is deprecated. If not provided, will not be updated.
            cardinality_min: Minimum cardinality. If not provided, will not be updated.
            cardinality_max: Maximum cardinality (-1 means unbounded). If not provided, will not be updated.
            definition: Definition to override the DT_SC definition. If not provided, will not be updated.
            remark: Additional remarks. If not provided, will not be updated.
            example: Example value. If not provided, will not be updated.
            default_value: Default value (mutually exclusive with fixed_value). If not provided, will not be updated.
            fixed_value: Fixed value (mutually exclusive with default_value). If not provided, will not be updated.
            facet_min_length: Minimum length facet for string types. If not provided, will not be updated.
            facet_max_length: Maximum length facet for string types. If not provided, will not be updated.
            facet_pattern: Pattern facet (regex) for string types. If not provided, will not be updated.
            
        Returns:
            tuple: (bbie_sc_id, list of updated fields)
            
        Raises:
            HTTPException: If validation fails, resources are not found, user lacks permission,
                or database errors occur.
        """
        # Validate that at least one field is provided
        # Convert ellipsis to None for validation check
        default_value_for_validation = None if default_value is ... else default_value
        fixed_value_for_validation = None if fixed_value is ... else fixed_value
        if all(param is None for param in [is_used, is_deprecated, cardinality_min, cardinality_max, definition, remark, example, default_value_for_validation, fixed_value_for_validation, facet_min_length, facet_max_length, facet_pattern]):
            raise HTTPException(
                status_code=400,
                detail="At least one field must be provided for update"
            )

        # Validate default_value and fixed_value are mutually exclusive
        self._validate_default_and_fixed_value(default_value_for_validation, fixed_value_for_validation)
        
        # 1) Get existing BBIE_SC
        existing_bbie_sc = db_exec(
            select(BbieSc)
            .options(
                selectinload(BbieSc.based_dt_sc_manifest).selectinload(DtScManifest.dt_sc),
                selectinload(BbieSc.owner_top_level_asbiep)
            )
            .where(BbieSc.bbie_sc_id == bbie_sc_id)
        ).first()
        
        if not existing_bbie_sc:
            raise HTTPException(
                status_code=404,
                detail=f"BBIE_SC with ID {bbie_sc_id} not found"
            )
        
        owner_top_level_asbiep_id = existing_bbie_sc.owner_top_level_asbiep_id
        
        # 2) Validate ownership and state
        self._validate_ownership_and_state_for_asbie_update(
            owner_top_level_asbiep_id
        )
        
        # 3) Get DT_SC manifest for validation
        dt_sc_manifest = existing_bbie_sc.based_dt_sc_manifest
        dt_sc = dt_sc_manifest.dt_sc
        
        # 4) Validate cardinality if provided
        base_cardinality_min = dt_sc.cardinality_min
        base_cardinality_max = dt_sc.cardinality_max if dt_sc.cardinality_max is not None else -1
        
        # Determine final cardinality values (use provided values or defaults from existing)
        final_cardinality_min = cardinality_min if cardinality_min is not None else existing_bbie_sc.cardinality_min
        final_cardinality_max = cardinality_max if cardinality_max is not None else existing_bbie_sc.cardinality_max
        
        # Validate cardinality_min must not be less than base cardinality_min
        if final_cardinality_min < base_cardinality_min:
            raise HTTPException(
                status_code=400,
                detail=f"The minimum cardinality ({final_cardinality_min}) is too low. "
                       f"It must be at least {base_cardinality_min} as required by the base DT_SC."
            )
        
        # Validate cardinality_max must not exceed base cardinality_max (unless base is -1 for unbounded)
        if base_cardinality_max != -1:
            if final_cardinality_max != -1 and final_cardinality_max > base_cardinality_max:
                raise HTTPException(
                    status_code=400,
                    detail=f"The maximum cardinality ({final_cardinality_max}) exceeds the limit. "
                           f"It must be at most {base_cardinality_max} as allowed by the base DT_SC."
                )
        
        # Validate cardinality_min must be <= cardinality_max
        if final_cardinality_max != -1:
            if final_cardinality_min > final_cardinality_max:
                raise HTTPException(
                    status_code=400,
                    detail=f"The minimum cardinality ({final_cardinality_min}) cannot be greater than the maximum cardinality ({final_cardinality_max})."
                )
        
        # 5) Update existing BBIE_SC
        updates = self._update_bbie_sc(
            existing_bbie_sc=existing_bbie_sc,
            final_cardinality_min=final_cardinality_min,
            final_cardinality_max=final_cardinality_max,
            is_used=is_used,
            is_deprecated=is_deprecated,
            cardinality_min=cardinality_min,
            cardinality_max=cardinality_max,
            definition=definition,
            remark=remark,
            example=example,
            default_value=default_value,
            fixed_value=fixed_value,
            facet_min_length=facet_min_length,
            facet_max_length=facet_max_length,
            facet_pattern=facet_pattern
        )
        
        # Evict cache entries for BBIE_SC list
        evict_cache("business_information_entity.get_bbie_sc_list", bbie_id=existing_bbie_sc.bbie_id)  # Evict BBIE_SC list
        
        return (existing_bbie_sc.bbie_sc_id, updates)

    def _create_new_bbie_sc(
            self,
            bbie_id: int,
            based_dt_sc_manifest_id: int,
            dt_sc_manifest: "DtScManifest",
            owner_top_level_asbiep_id: int,
            final_cardinality_min: int,
            final_cardinality_max: int,
            bbie_sc_path: str,
            bbie_sc_hash_path: str,
            xbt_manifest_id: int | None,
            code_list_manifest_id: int | None,
            agency_id_list_manifest_id: int | None
    ) -> tuple["BbieSc", list[str]]:
        """
        Create a new BBIE_SC.
        
        Args:
            bbie_id: The BBIE ID that this BBIE_SC belongs to
            based_dt_sc_manifest_id: The DT_SC manifest ID that this BBIE_SC is based on
            dt_sc_manifest: The DT_SC manifest
            owner_top_level_asbiep_id: The top-level ASBIEP ID
            final_cardinality_min: Validated minimum cardinality
            final_cardinality_max: Validated maximum cardinality
            bbie_sc_path: The calculated path for the BBIE_SC
            bbie_sc_hash_path: The calculated hash_path for the BBIE_SC
            xbt_manifest_id: XBT manifest ID (from dt_sc_awd_pri)
            code_list_manifest_id: Code list manifest ID (from dt_sc_awd_pri)
            agency_id_list_manifest_id: Agency ID list manifest ID (from dt_sc_awd_pri)
            
        Returns:
            tuple: (new_bbie_sc, list of updated fields)
        """
        dt_sc = dt_sc_manifest.dt_sc
        
        # Cascade default_value and fixed_value from DT_SC if they exist (mutually exclusive)
        cascaded_default_value = None
        cascaded_fixed_value = None
        if dt_sc.fixed_value is not None:
            cascaded_fixed_value = dt_sc.fixed_value
        elif dt_sc.default_value is not None:
            cascaded_default_value = dt_sc.default_value
        
        # Create new BBIE_SC
        bbie_sc_guid = generate_guid()
        
        new_bbie_sc = BbieSc(
            guid=bbie_sc_guid,
            based_dt_sc_manifest_id=based_dt_sc_manifest_id,
            path=bbie_sc_path,
            hash_path=bbie_sc_hash_path,
            bbie_id=bbie_id,
            xbt_manifest_id=xbt_manifest_id,
            code_list_manifest_id=code_list_manifest_id,
            agency_id_list_manifest_id=agency_id_list_manifest_id,
            cardinality_min=final_cardinality_min,
            cardinality_max=final_cardinality_max,
            definition=None,
            default_value=cascaded_default_value,
            fixed_value=cascaded_fixed_value,
            is_used=True,
            is_deprecated=False,
            created_by=self.requester.app_user_id,
            last_updated_by=self.requester.app_user_id,
            creation_timestamp=datetime.now(timezone.utc),
            last_update_timestamp=datetime.now(timezone.utc),
            owner_top_level_asbiep_id=owner_top_level_asbiep_id
        )
        
        db_add(new_bbie_sc)
        db_flush()
        
        updates = ["bbie_sc_id"]  # Created new BBIE_SC
        if xbt_manifest_id is not None:
            updates.append("xbt_manifest_id")
        if code_list_manifest_id is not None:
            updates.append("code_list_manifest_id")
        if agency_id_list_manifest_id is not None:
            updates.append("agency_id_list_manifest_id")
        if final_cardinality_min != dt_sc.cardinality_min:
            updates.append("cardinality_min")
        if final_cardinality_max != (dt_sc.cardinality_max if dt_sc.cardinality_max is not None else -1):
            updates.append("cardinality_max")
        if cascaded_default_value is not None:
            updates.append("default_value")
        if cascaded_fixed_value is not None:
            updates.append("fixed_value")
        updates.append("is_used")
        updates.append("is_deprecated")
        
        return (new_bbie_sc, updates)

    def _update_bbie_sc(
            self,
            existing_bbie_sc: "BbieSc",
            final_cardinality_min: int,
            final_cardinality_max: int,
            is_used: bool | None = None,
            is_deprecated: bool | None = None,
            cardinality_min: int | None = None,
            cardinality_max: int | None = None,
            definition: str | None = None,
            remark: str | None = None,
            example: str | None = None,
            default_value: str | None = ...,
            fixed_value: str | None = ...,
            facet_min_length: int | None = None,
            facet_max_length: int | None = None,
            facet_pattern: str | None = None,
            xbt_manifest_id: int | None = None,
            code_list_manifest_id: int | None = None,
            agency_id_list_manifest_id: int | None = None
    ) -> list[str]:
        """
        Update a BBIE_SC.
        
        Args:
            existing_bbie_sc: The existing BBIE_SC to update
            final_cardinality_min: Validated minimum cardinality
            final_cardinality_max: Validated maximum cardinality
            is_used: Whether the BBIE_SC is used. If None, will not be updated.
            is_deprecated: Whether the BBIE_SC is deprecated. If None, will not be updated.
            cardinality_min: Original cardinality_min parameter (to track if it was provided)
            cardinality_max: Original cardinality_max parameter (to track if it was provided)
            definition: Definition for the BBIE_SC
            remark: Remark for the BBIE_SC
            example: Example for the BBIE_SC
            default_value: Default value for the BBIE_SC
            fixed_value: Fixed value for the BBIE_SC
            facet_min_length: Minimum length facet
            facet_max_length: Maximum length facet
            facet_pattern: Pattern facet
            xbt_manifest_id: XBT manifest ID (from dt_sc_awd_pri, only used during creation/duplicate update)
            code_list_manifest_id: Code list manifest ID (from dt_sc_awd_pri, only used during creation/duplicate update)
            agency_id_list_manifest_id: Agency ID list manifest ID (from dt_sc_awd_pri, only used during creation/duplicate update)
            
        Returns:
            list: List of updated field names (only includes fields that were actually changed)
        """
        existing_bbie_sc.last_updated_by = self.requester.app_user_id
        existing_bbie_sc.last_update_timestamp = datetime.now(timezone.utc)
        
        updates = []
        
        # Update fields if provided
        # Always update if parameter is provided, regardless of current value
        if definition is not None:
            existing_bbie_sc.definition = definition
            updates.append("definition")

        if example is not None:
            existing_bbie_sc.example = example
            updates.append("example")
        
        if cardinality_min is not None:
            existing_bbie_sc.cardinality_min = final_cardinality_min
            updates.append("cardinality_min")
        
        if cardinality_max is not None:
            existing_bbie_sc.cardinality_max = final_cardinality_max
            updates.append("cardinality_max")
        
        # Handle default_value and fixed_value (mutually exclusive)
        # Check if parameters were explicitly provided (not Ellipsis)
        default_value_provided = default_value is not ...
        fixed_value_provided = fixed_value is not ...
        
        # Normalize ellipsis to None for easier handling
        if default_value is ...:
            default_value = None
        if fixed_value is ...:
            fixed_value = None
        
        # Get DT_SC to check for value constraints
        dt_sc_manifest = existing_bbie_sc.based_dt_sc_manifest
        dt_sc = dt_sc_manifest.dt_sc if dt_sc_manifest else None
        
        # If DT_SC has fixed_value, prevent changes and cascade it
        if dt_sc is not None and dt_sc.fixed_value is not None:
            # Prevent setting default_value when DT_SC has fixed_value
            if default_value is not None:
                raise HTTPException(
                    status_code=400,
                    detail=f"Cannot set default_value because the original value constraint (DT_SC) uses a fixed value of '{dt_sc.fixed_value}'. "
                           f"The fixed_value constraint takes precedence and cannot be overridden with a default_value."
                )
            # Prevent clearing fixed_value when DT_SC has fixed_value (user explicitly passed fixed_value=None)
            if fixed_value_provided and fixed_value is None:
                raise HTTPException(
                    status_code=400,
                    detail=f"Cannot clear fixed_value because the original value constraint (DT_SC) uses a fixed value of '{dt_sc.fixed_value}'. "
                           f"The fixed_value cannot be cleared when the base DT_SC has a fixed value constraint."
                )
            # Prevent changing fixed_value to a different value
            if fixed_value is not None and fixed_value != dt_sc.fixed_value:
                raise HTTPException(
                    status_code=400,
                    detail=f"Cannot change fixed_value because the original value constraint (DT_SC) uses a fixed value of '{dt_sc.fixed_value}'. "
                           f"The fixed_value cannot be modified when the base DT_SC has a fixed value constraint."
                )
            # Cascade fixed_value from DT_SC if it's not already set correctly (and user didn't explicitly provide it)
            if not fixed_value_provided and existing_bbie_sc.fixed_value != dt_sc.fixed_value:
                fixed_value = dt_sc.fixed_value
                fixed_value_provided = True  # Mark as provided so it gets set
        
        # Cascade default_value from DT_SC if user didn't provide one and DT_SC has it (and DT_SC doesn't have fixed_value)
        if not default_value_provided and dt_sc is not None and dt_sc.default_value is not None and dt_sc.fixed_value is None:
            # Only cascade if the current value is different from the DT_SC's default_value
            if existing_bbie_sc.default_value != dt_sc.default_value:
                default_value = dt_sc.default_value
                default_value_provided = True  # Mark as provided so it gets set
        
        # Handle explicit None values (user wants to clear) or provided values
        if default_value_provided:
            if default_value is not None:
                existing_bbie_sc.default_value = default_value
                updates.append("default_value")
                # Always clear fixed_value when default_value is set (mutually exclusive)
                if existing_bbie_sc.fixed_value is not None:
                    existing_bbie_sc.fixed_value = None
                    updates.append("fixed_value")
            else:
                # User explicitly passed default_value=None to clear it (default_value can be changed)
                if existing_bbie_sc.default_value is not None:
                    existing_bbie_sc.default_value = None
                    updates.append("default_value")
        elif fixed_value_provided:
            if fixed_value is not None:
                existing_bbie_sc.fixed_value = fixed_value
                updates.append("fixed_value")
                # Always clear default_value when fixed_value is set (mutually exclusive)
                if existing_bbie_sc.default_value is not None:
                    existing_bbie_sc.default_value = None
                    updates.append("default_value")
            else:
                # User explicitly passed fixed_value=None to clear it (only if DT_SC doesn't have fixed_value)
                # This case is already handled above - if DT_SC has fixed_value, we raise an error
                if existing_bbie_sc.fixed_value is not None:
                    existing_bbie_sc.fixed_value = None
                    updates.append("fixed_value")
        
        if facet_min_length is not None:
            existing_bbie_sc.facet_min_length = facet_min_length
            updates.append("facet_min_length")
        
        if facet_max_length is not None:
            existing_bbie_sc.facet_max_length = facet_max_length
            updates.append("facet_max_length")
        
        if facet_pattern is not None:
            existing_bbie_sc.facet_pattern = facet_pattern
            updates.append("facet_pattern")
        
        # Only update is_used if it was provided and actually changed
        if is_used is not None:
            if existing_bbie_sc.is_used != is_used:
                existing_bbie_sc.is_used = is_used
                updates.append("is_used")
        
        # Only update is_deprecated if it was provided and actually changed
        if is_deprecated is not None:
            if existing_bbie_sc.is_deprecated != is_deprecated:
                existing_bbie_sc.is_deprecated = is_deprecated
                updates.append("is_deprecated")
        
        # Update remark if provided
        if remark is not None:
            existing_bbie_sc.remark = remark
            updates.append("remark")
        
        # Update manifest IDs if provided (only during creation/duplicate update)
        if xbt_manifest_id is not None:
            existing_bbie_sc.xbt_manifest_id = xbt_manifest_id
            updates.append("xbt_manifest_id")
        
        if code_list_manifest_id is not None:
            existing_bbie_sc.code_list_manifest_id = code_list_manifest_id
            updates.append("code_list_manifest_id")
        
        if agency_id_list_manifest_id is not None:
            existing_bbie_sc.agency_id_list_manifest_id = agency_id_list_manifest_id
            updates.append("agency_id_list_manifest_id")
        
        db_add(existing_bbie_sc)
        return updates
