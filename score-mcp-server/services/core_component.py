"""
Service class for managing Core Component operations in connectCenter.

Core Components are the fundamental building blocks of business information
standards, including ACCs (Aggregate Core Components), ASCCPs (Association
Core Component Properties), and BCCPs (Basic Core Component Properties).
This service provides comprehensive functionality for querying and retrieving
Core Component data with full relationship loading.
"""

import logging

from fastapi import HTTPException

from sqlmodel import select
from sqlalchemy.orm import selectinload
from sqlalchemy import and_

from services.models import AppUser, Acc, AccManifest, Ascc, AsccManifest, Asccp, AsccpManifest, Bccp, BccpManifest, Release, \
    Library, Log, Tag, \
    Namespace, AccManifestTag, AsccpManifestTag, BccpManifestTag, DtManifest, Dt, BccManifest, Bcc, SeqKey, Dt
from .release import ReleaseService
from services.models.common import Sort, PaginationParams, DateRangeParams, Page
from services.transaction import transaction, db_exec
from services.cache import cache

# Configure logging
logger = logging.getLogger(__name__)


class CoreComponentService:
    """
    Service class for managing Core Component operations.
    
    This service provides a comprehensive interface for working with Core Components,
    which are the fundamental building blocks of business information standards:
    - ACC (Aggregate Core Component): Represents complex data structures
    - ASCCP (Association Core Component Property): Represents associations between ACCs
    - BCCP (Basic Core Component Property): Represents simple data elements
    
    The service handles querying and retrieval of Core Component data with full
    relationship loading, including nested relationships between components.
    
    Key Features:
    - Retrieve ACCs, ASCCPs, and BCCPs by manifest ID
    - Get associated components (ASCCs, BCCs) for ACCs
    - Support for complex relationship traversal
    - Automatic loading of related entities (namespace, creator, owner, release, etc.)
    - Full relationship loading including nested component hierarchies
    - Support for tags and other metadata
    
    Main Operations:
    ACC (Aggregate Core Component) Operations:
    - get_acc_by_manifest_id(): Retrieve a single ACC by its manifest ID, including
      all related entities (namespace, creator, owner, release, log, based_acc_manifest).
      Returns the ACC manifest with full relationship loading.
    
    ASCC (Association Core Component) Operations:
    - get_ascc_manifest(): Retrieve an ASCC manifest by ID, including relationships
      to the from ACC manifest and to ASCCP manifest, with nested component loading.
    
    BCC (Basic Core Component) Operations:
    - get_bcc_manifest(): Retrieve a BCC manifest by ID, including relationships
      to the from ACC manifest and to BCCP manifest, with nested component loading.
    
    Component Hierarchy:
    Core Components form a hierarchical structure:
    - ACCs contain ASCCs (which reference ASCCPs) and BCCs (which reference BCCPs)
    - ASCCPs reference ACCs (role_of_acc)
    - BCCPs reference Data Types (dt_id)
    - Components can be based on other components (based_* relationships)
    
    Relationship Loading:
    All queries automatically load related entities using SQLAlchemy's selectinload:
    - ACC: namespace, creator, owner, last_updater, release (with library), log,
      based_acc_manifest (with acc and release)
    - ASCC: creator, owner, last_updater, release (with library), from_acc_manifest
      (with acc), to_asccp_manifest (with asccp)
    - BCC: creator, owner, last_updater, release (with library), from_acc_manifest
      (with acc), to_bccp_manifest (with bccp)
    
    Transaction Management:
    All database operations are wrapped in read-only transactions to ensure
    data consistency and proper connection management.
    
    Example Usage:
        service = CoreComponentService()
        
        # Get an ACC with all relationships
        acc_manifest = service.get_acc_by_manifest_id(acc_manifest_id=123)
        
        # Get an ASCC manifest
        ascc_manifest = service.get_ascc_manifest(ascc_manifest_id=456)
        
        # Get a BCC manifest
        bcc_manifest = service.get_bcc_manifest(bcc_manifest_id=789)
    """

    # Allowed columns for ordering to prevent SQL injection
    allowed_columns_for_order_by = [
        'den',
        'name',
        'definition',
        'creation_timestamp',
        'last_update_timestamp'
    ]

    def __init__(self):
        """
        Initialize the service.
        """
        pass

    @cache(key_prefix="core_component.get_acc_by_manifest_id")
    @transaction(read_only=True)
    def get_acc_by_manifest_id(self, acc_manifest_id: int) -> AccManifest:
        """
        Get an ACC by its manifest ID.
        
        Args:
            acc_manifest_id: ID of the ACC manifest to retrieve
        
        Returns:
            AccManifest: The ACC manifest with loaded relationships if found
        
        Raises:
            HTTPException: If ACC manifest not found
        """
        # Get the manifest with all relationships loaded
        manifest_query = select(AccManifest).options(
            selectinload(AccManifest.acc).selectinload(Acc.namespace),
            selectinload(AccManifest.acc).selectinload(Acc.creator),
            selectinload(AccManifest.acc).selectinload(Acc.owner),
            selectinload(AccManifest.acc).selectinload(Acc.last_updater),
            selectinload(AccManifest.release).selectinload(Release.library),
            selectinload(AccManifest.log),
            selectinload(AccManifest.based_acc_manifest).selectinload(AccManifest.acc).selectinload(Acc.namespace),
            selectinload(AccManifest.based_acc_manifest).selectinload(AccManifest.release).selectinload(Release.library)
        ).where(
            AccManifest.acc_manifest_id == acc_manifest_id
        )
        manifest = db_exec(manifest_query).first()
        if not manifest:
            raise HTTPException(
                status_code=404,
                detail=f"ACC manifest with ID {acc_manifest_id} not found"
            )

        return manifest

    @cache(key_prefix="core_component.get_ascc_manifest")
    @transaction(read_only=True)
    def get_ascc_manifest(self, ascc_manifest_id: int) -> AsccManifest:
        """
        Get an ASCC by its manifest ID.
        
        Args:
            ascc_manifest_id: ID of the ASCC manifest to retrieve
        
        Returns:
            AsccManifest: The ASCC manifest with loaded relationships if found
        
        Raises:
            HTTPException: If ASCC manifest not found
        """
        manifest_query = select(AsccManifest).options(
            # ASCC and its common relationships
            selectinload(AsccManifest.ascc).selectinload(Ascc.creator),
            selectinload(AsccManifest.ascc).selectinload(Ascc.owner),
            selectinload(AsccManifest.ascc).selectinload(Ascc.last_updater),
            # Release and library
            selectinload(AsccManifest.release).selectinload(Release.library),
            # From ACC manifest and nested ACC
            selectinload(AsccManifest.from_acc_manifest).selectinload(AccManifest.acc),
            # To ASCCP manifest and nested ASCCP
            selectinload(AsccManifest.to_asccp_manifest).selectinload(AsccpManifest.asccp)
        ).where(
            AsccManifest.ascc_manifest_id == ascc_manifest_id
        )
        manifest = db_exec(manifest_query).first()
        if not manifest:
            raise HTTPException(
                status_code=404,
                detail=f"ASCC manifest with ID {ascc_manifest_id} not found"
            )

        return manifest

    @cache(key_prefix="core_component.get_bcc_manifest")
    @transaction(read_only=True)
    def get_bcc_manifest(self, bcc_manifest_id: int) -> BccManifest:
        """
        Get an BCC by its manifest ID.

        Args:
            bcc_manifest_id: ID of the BCC manifest to retrieve

        Returns:
            BccManifest: The BCC manifest with loaded relationships if found

        Raises:
            HTTPException: If BCC manifest not found
        """
        manifest_query = select(BccManifest).options(
            # BCC and its common relationships
            selectinload(BccManifest.bcc).selectinload(Bcc.creator),
            selectinload(BccManifest.bcc).selectinload(Bcc.owner),
            selectinload(BccManifest.bcc).selectinload(Bcc.last_updater),
            # Release and library
            selectinload(BccManifest.release).selectinload(Release.library),
            # From ACC manifest and nested ACC
            selectinload(BccManifest.from_acc_manifest).selectinload(AccManifest.acc),
            # To BCCP manifest and nested BCCP
            selectinload(BccManifest.to_bccp_manifest).selectinload(BccpManifest.bccp)
        ).where(
            BccManifest.bcc_manifest_id == bcc_manifest_id
        )
        manifest = db_exec(manifest_query).first()
        if not manifest:
            raise HTTPException(
                status_code=404,
                detail=f"BCC manifest with ID {bcc_manifest_id} not found"
            )

        return manifest

    @cache(key_prefix="core_component.get_asccp_by_manifest_id")
    @transaction(read_only=True)
    def get_asccp_by_manifest_id(self, asccp_manifest_id: int) -> AsccpManifest:
        """
        Get an ASCCP by its manifest ID.
        
        Args:
            asccp_manifest_id: ID of the ASCCP manifest to retrieve
        
        Returns:
            AsccpManifest: The ASCCP manifest with loaded relationships if found
        
        Raises:
            HTTPException: If ASCCP manifest not found
        """
        # Get the manifest with all relationships loaded
        manifest_query = select(AsccpManifest).options(
            selectinload(AsccpManifest.asccp).selectinload(Asccp.namespace),
            selectinload(AsccpManifest.asccp).selectinload(Asccp.creator),
            selectinload(AsccpManifest.asccp).selectinload(Asccp.owner),
            selectinload(AsccpManifest.asccp).selectinload(Asccp.last_updater),
            selectinload(AsccpManifest.release).selectinload(Release.library),
            selectinload(AsccpManifest.log),
            selectinload(AsccpManifest.role_of_acc_manifest).selectinload(AccManifest.acc).selectinload(Acc.namespace),
            selectinload(AsccpManifest.role_of_acc_manifest).selectinload(AccManifest.release).selectinload(Release.library)
        ).where(
            AsccpManifest.asccp_manifest_id == asccp_manifest_id
        )
        manifest = db_exec(manifest_query).first()
        if not manifest:
            raise HTTPException(
                status_code=404,
                detail=f"ASCCP manifest with ID {asccp_manifest_id} not found"
            )

        return manifest

    @cache(key_prefix="core_component.get_bccp_by_manifest_id")
    @transaction(read_only=True)
    def get_bccp_by_manifest_id(self, bccp_manifest_id: int) -> BccpManifest:
        """
        Get a BCCP by its manifest ID.
        
        Args:
            bccp_manifest_id: ID of the BCCP manifest to retrieve
        
        Returns:
            BccpManifest: The BCCP manifest with loaded relationships if found
        
        Raises:
            HTTPException: If BCCP manifest not found
        """
        # Get the manifest with all relationships loaded
        manifest_query = select(BccpManifest).options(
            selectinload(BccpManifest.bccp).selectinload(Bccp.namespace),
            selectinload(BccpManifest.bccp).selectinload(Bccp.creator),
            selectinload(BccpManifest.bccp).selectinload(Bccp.owner),
            selectinload(BccpManifest.bccp).selectinload(Bccp.last_updater),
            selectinload(BccpManifest.release).selectinload(Release.library),
            selectinload(BccpManifest.log),
            selectinload(BccpManifest.bdt_manifest).selectinload(DtManifest.dt).selectinload(Dt.namespace),
            selectinload(BccpManifest.bdt_manifest).selectinload(DtManifest.release).selectinload(Release.library)
        ).where(
            BccpManifest.bccp_manifest_id == bccp_manifest_id
        )
        manifest = db_exec(manifest_query).first()
        if not manifest:
            raise HTTPException(
                status_code=404,
                detail=f"BCCP manifest with ID {bccp_manifest_id} not found"
            )

        return manifest

    def _build_common_columns(self, component_type: str, manifest_id_col, component_id_col, guid_col, den_col, name_col,
                              definition_col, definition_source_col, is_deprecated_col, state_col,
                              AppUser, AppUserCreator, AppUserUpdater, creation_timestamp_col, last_update_timestamp_col,
                              den: str = None, sort_list: list[Sort] = None):
        """Build common columns for component queries."""
        from sqlmodel import literal_column, func, text
        columns = [
            literal_column(f"'{component_type}'").label("component_type"),
            manifest_id_col.label("manifest_id"),
            component_id_col.label("component_id"),
            guid_col,
            den_col,
            name_col.label("name"),
            definition_col,
            definition_source_col,
            is_deprecated_col,
            state_col,
            # Tag info
            Tag.name.label("tag_name"),
            # Namespace info
            Namespace.namespace_id.label("ns_id"),
            Namespace.prefix.label("ns_prefix"),
            Namespace.uri.label("ns_uri"),
            # Library info
            Library.library_id.label("lib_id"),
            Library.name.label("lib_name"),
            # Release info
            Release.release_id.label("rel_id"),
            Release.release_num.label("rel_num"),
            Release.state.label("rel_state"),
            # Log info
            Log.log_id.label("log_log_id"),
            Log.revision_num.label("log_revision_num"),
            Log.revision_tracking_num.label("log_revision_tracking_num"),
            # Owner info
            AppUser.app_user_id.label("owner_id"),
            AppUser.login_id.label("owner_login_id"),
            AppUser.name.label("owner_name"),
            AppUser.is_admin.label("owner_is_admin"),
            AppUser.is_developer.label("owner_is_developer"),
            # Creator info
            AppUserCreator.app_user_id.label("creator_id"),
            AppUserCreator.login_id.label("creator_login_id"),
            AppUserCreator.name.label("creator_name"),
            AppUserCreator.is_admin.label("creator_is_admin"),
            AppUserCreator.is_developer.label("creator_is_developer"),
            # Last updater info
            AppUserUpdater.app_user_id.label("updater_id"),
            AppUserUpdater.login_id.label("updater_login_id"),
            AppUserUpdater.name.label("updater_name"),
            AppUserUpdater.is_admin.label("updater_is_admin"),
            AppUserUpdater.is_developer.label("updater_is_developer"),
            # Timestamps
            creation_timestamp_col,
            last_update_timestamp_col
        ]
        # Check if 'den' is in the sort_list - if so, ignore levenshtein_score
        # because we're ordering by actual DEN values, not by similarity
        ordering_by_den = False
        if sort_list:
            ordering_by_den = any(sort.column == 'den' for sort in sort_list)
        
        # Add Levenshtein score if DEN filter is provided AND we're not ordering by den
        # When ordering by den, we want actual alphabetical ordering, not similarity-based ordering
        if den and not ordering_by_den:
            columns.append(func.levenshtein(den_col, text(f"'{den}'")).label("levenshtein_score"))
        else:
            columns.append(literal_column("0").label("levenshtein_score"))
        return columns

    def _build_where_conditions(self, release_id_col, den_col, component_col, all_release_ids: list[int],
                               den: str = None, tag: str = None, created_on_params: DateRangeParams = None,
                               last_updated_on_params: DateRangeParams = None):
        """Build where conditions for component queries."""
        from sqlmodel import func
        conditions = [release_id_col.in_(all_release_ids)]
        if den:
            conditions.append(func.lower(den_col).like(func.lower(f"%{den}%")))
        if tag:
            conditions.append(func.lower(Tag.name).like(func.lower(f"%{tag}%")))
        if created_on_params:
            if created_on_params.before:
                conditions.append(component_col.creation_timestamp <= created_on_params.before)
            if created_on_params.after:
                conditions.append(component_col.creation_timestamp >= created_on_params.after)
        if last_updated_on_params:
            if last_updated_on_params.before:
                conditions.append(component_col.last_update_timestamp <= last_updated_on_params.before)
            if last_updated_on_params.after:
                conditions.append(component_col.last_update_timestamp >= last_updated_on_params.after)
        return conditions

    def _build_user_info(self, user_id, login_id, name, is_admin, is_developer):
        """Build user info dictionary from row data."""
        if not user_id:
            return None
        info = {
            "user_id": user_id,
            "login_id": login_id,
            "username": name or login_id,
            "roles": []
        }
        if is_admin:
            info["roles"].append("Admin")
        if is_developer:
            info["roles"].append("Developer")
        if not info["roles"]:
            info["roles"].append("End-User")
        return info

    def _process_component_row(self, row):
        """Process a single component row into unified format."""
        # Build namespace info
        namespace_info = {
            "namespace_id": row.ns_id,
            "prefix": row.ns_prefix,
            "uri": row.ns_uri
        } if row.ns_id else None

        # Build library info
        library_info = {
            "library_id": row.lib_id,
            "name": row.lib_name
        } if row.lib_id else None

        # Build release info
        release_info = {
            "release_id": row.rel_id,
            "release_num": row.rel_num,
            "state": row.rel_state
        } if row.rel_id else None

        # Build log info
        log_info = {
            "log_id": row.log_log_id,
            "revision_num": row.log_revision_num,
            "revision_tracking_num": row.log_revision_tracking_num
        } if row.log_log_id else None

        # Build user info
        owner_info = self._build_user_info(
            row.owner_id, row.owner_login_id, row.owner_name,
            row.owner_is_admin, row.owner_is_developer
        )
        creator_info = self._build_user_info(
            row.creator_id, row.creator_login_id, row.creator_name,
            row.creator_is_admin, row.creator_is_developer
        )
        updater_info = self._build_user_info(
            row.updater_id, row.updater_login_id, row.updater_name,
            row.updater_is_admin, row.updater_is_developer
        )

        # Build created and last_updated info
        created_info = {
            "who": creator_info,
            "when": row.creation_timestamp
        } if creator_info and row.creation_timestamp else None

        last_updated_info = {
            "who": updater_info,
            "when": row.last_update_timestamp
        } if updater_info and row.last_update_timestamp else None

        # Create unified component info
        return {
            "component_type": row.component_type,
            "manifest_id": row.manifest_id,
            "component_id": row.component_id,
            "guid": row.guid,
            "den": row.den,
            "name": row.name,
            "definition": row.definition,
            "definition_source": row.definition_source,
            "is_deprecated": row.is_deprecated,
            "state": row.state,
            "namespace": namespace_info,
            "library": library_info,
            "release": release_info,
            "log": log_info,
            "owner": owner_info,
            "created": created_info,
            "last_updated": last_updated_info,
            "tag": row.tag_name
        }

    def _apply_sorting_to_union_query(self, base_query, sort_list):
        """Apply sorting to union query."""
        from sqlmodel import text
        if sort_list:
            # Validate sort columns
            for sort in sort_list:
                if sort.column not in self.allowed_columns_for_order_by:
                    raise HTTPException(
                        status_code=400,
                        detail=f"Invalid sort column: '{sort.column}'. Allowed columns: {', '.join(self.allowed_columns_for_order_by)}"
                    )

            order_clauses = []
            for sort in sort_list:
                direction = 'DESC' if sort.direction == 'desc' else 'ASC'
                order_clauses.append(text(f"{sort.column} {direction}"))

            if order_clauses:
                base_query = base_query.order_by(*order_clauses)
        else:
            # Default: use Levenshtein score for ordering
            base_query = base_query.order_by(text("levenshtein_score ASC"))
        return base_query

    @cache(key_prefix="core_component.get_core_components_by_release")
    @transaction(read_only=True)
    def get_core_components_by_release(
        self,
        release_id: int,
        types: list[str],
        den: str = None,
        tag: str = None,
        created_on_params: DateRangeParams = None,
        last_updated_on_params: DateRangeParams = None,
        pagination: PaginationParams = PaginationParams(offset=0, limit=10),
        sort_list: list[Sort] = None
    ) -> Page:
        """
        Get core components (ACC, ASCCP, BCCP) using UNION query with SQLModel select for proper pagination.
        
        Args:
            release_id: Release ID to filter by
            types: List of component types to include ('ACC', 'ASCCP', 'BCCP')
            den: DEN filter
            tag: Tag name filter
            created_on_params: Creation date range filter
            last_updated_on_params: Last update date range filter
            pagination: Pagination parameters
            sort_list: Sort parameters
            
        Returns:
            Page: Paginated response containing core components and pagination metadata
        """
        # Set default pagination if not provided
        if pagination is None:
            pagination = PaginationParams(offset=0, limit=10)

        from sqlalchemy.orm import aliased
        from sqlmodel import union_all, func, and_, text, literal_column

        # Get dependent releases using the existing ReleaseService method
        try:
            release_service = ReleaseService()
            dependent_release_ids = release_service.get_dependent_releases(release_id)
        except Exception as e:
            logger.warning(f"Failed to get dependent releases for {release_id}: {e}")
            # If we can't get dependencies, just use the original release
            dependent_release_ids = []

        # Include the original release ID and all dependent release IDs
        all_release_ids = [release_id] + dependent_release_ids

        # Create aliases for AppUser
        AppUserCreator = aliased(AppUser)
        AppUserUpdater = aliased(AppUser)

        union_queries = []

        # ACC query with all joins
        if not types or "ACC" in types:
            acc_columns = self._build_common_columns(
                "ACC", AccManifest.acc_manifest_id, AccManifest.acc_id, Acc.guid,
                AccManifest.den, Acc.object_class_term, Acc.definition, Acc.definition_source,
                Acc.is_deprecated, Acc.state, AppUser, AppUserCreator, AppUserUpdater,
                Acc.creation_timestamp, Acc.last_update_timestamp, den, sort_list
            )

            acc_query = select(*acc_columns).select_from(
                AccManifest.__table__
                .join(Acc.__table__, AccManifest.acc_id == Acc.acc_id)
                .join(Release.__table__, AccManifest.release_id == Release.release_id)
                .join(Library.__table__, Release.library_id == Library.library_id)
                .join(Log.__table__, AccManifest.log_id == Log.log_id)
                .join(AppUser, Acc.owner_user_id == AppUser.app_user_id)
                .join(AppUserCreator, Acc.created_by == AppUserCreator.app_user_id)
                .join(AppUserUpdater, Acc.last_updated_by == AppUserUpdater.app_user_id)
                .outerjoin(Namespace.__table__, Acc.namespace_id == Namespace.namespace_id)
                .outerjoin(AccManifestTag.__table__, AccManifest.acc_manifest_id == AccManifestTag.acc_manifest_id)
                .outerjoin(Tag.__table__, AccManifestTag.tag_id == Tag.tag_id)
            ).where(and_(*self._build_where_conditions(
                AccManifest.release_id, AccManifest.den, Acc, all_release_ids,
                den, tag, created_on_params, last_updated_on_params
            )))

            union_queries.append(acc_query)

        # ASCCP query with all joins
        if not types or "ASCCP" in types:
            asccp_columns = self._build_common_columns(
                "ASCCP", AsccpManifest.asccp_manifest_id, AsccpManifest.asccp_id, Asccp.guid,
                AsccpManifest.den, Asccp.property_term, Asccp.definition, Asccp.definition_source,
                Asccp.is_deprecated, Asccp.state, AppUser, AppUserCreator, AppUserUpdater,
                Asccp.creation_timestamp, Asccp.last_update_timestamp, den, sort_list
            )

            asccp_query = select(*asccp_columns).select_from(
                AsccpManifest.__table__
                .join(Asccp.__table__, AsccpManifest.asccp_id == Asccp.asccp_id)
                .join(Release.__table__, AsccpManifest.release_id == Release.release_id)
                .join(Library.__table__, Release.library_id == Library.library_id)
                .join(Log.__table__, AsccpManifest.log_id == Log.log_id)
                .join(AppUser, Asccp.owner_user_id == AppUser.app_user_id)
                .join(AppUserCreator, Asccp.created_by == AppUserCreator.app_user_id)
                .join(AppUserUpdater, Asccp.last_updated_by == AppUserUpdater.app_user_id)
                .outerjoin(Namespace.__table__, Asccp.namespace_id == Namespace.namespace_id)
                .outerjoin(AsccpManifestTag.__table__, AsccpManifest.asccp_manifest_id == AsccpManifestTag.asccp_manifest_id)
                .outerjoin(Tag.__table__, AsccpManifestTag.tag_id == Tag.tag_id)
            ).where(and_(*self._build_where_conditions(
                AsccpManifest.release_id, AsccpManifest.den, Asccp, all_release_ids,
                den, tag, created_on_params, last_updated_on_params
            )))

            union_queries.append(asccp_query)

        # BCCP query with all joins
        if not types or "BCCP" in types:
            bccp_columns = self._build_common_columns(
                "BCCP", BccpManifest.bccp_manifest_id, BccpManifest.bccp_id, Bccp.guid,
                BccpManifest.den, Bccp.property_term, Bccp.definition, Bccp.definition_source,
                Bccp.is_deprecated, Bccp.state, AppUser, AppUserCreator, AppUserUpdater,
                Bccp.creation_timestamp, Bccp.last_update_timestamp, den, sort_list
            )

            bccp_query = select(*bccp_columns).select_from(
                BccpManifest.__table__
                .join(Bccp.__table__, BccpManifest.bccp_id == Bccp.bccp_id)
                .join(Release.__table__, BccpManifest.release_id == Release.release_id)
                .join(Library.__table__, Release.library_id == Library.library_id)
                .join(Log.__table__, BccpManifest.log_id == Log.log_id)
                .join(AppUser, Bccp.owner_user_id == AppUser.app_user_id)
                .join(AppUserCreator, Bccp.created_by == AppUserCreator.app_user_id)
                .join(AppUserUpdater, Bccp.last_updated_by == AppUserUpdater.app_user_id)
                .outerjoin(Namespace.__table__, Bccp.namespace_id == Namespace.namespace_id)
                .outerjoin(BccpManifestTag.__table__, BccpManifest.bccp_manifest_id == BccpManifestTag.bccp_manifest_id)
                .outerjoin(Tag.__table__, BccpManifestTag.tag_id == Tag.tag_id)
            ).where(and_(*self._build_where_conditions(
                BccpManifest.release_id, BccpManifest.den, Bccp, all_release_ids,
                den, tag, created_on_params, last_updated_on_params
            )))

            union_queries.append(bccp_query)

        # Note: union_queries should never be empty now since tool layer ensures
        # empty strings are converted to all types ["ACC", "ASCCP", "BCCP"]

        # Create UNION query
        if len(union_queries) == 1:
            base_query = union_queries[0]
        else:
            base_query = union_all(*union_queries)

        # Where conditions are now applied to individual queries before unionization

        # Apply sorting
        base_query = self._apply_sorting_to_union_query(base_query, sort_list)

        # Get total count
        count_query = select(func.count()).select_from(base_query.subquery())
        total_count = db_exec(count_query).one() or 0

        # Add pagination
        paginated_query = base_query.limit(pagination.limit).offset(pagination.offset)

        # Execute query
        results = db_exec(paginated_query).all()

        # Convert to unified format with all joined data
        unified_items = [self._process_component_row(row) for row in results]

        # Create Page object
        return Page(
            total=total_count,
            offset=pagination.offset,
            limit=pagination.limit,
            items=unified_items
        )

    @cache(key_prefix="core_component.get_relationships_for_acc")
    @transaction(read_only=True)
    def get_relationships_for_acc(self, acc_manifest_id: int) -> list[SeqKey]:
        """
        Get relationships (SeqKeys) for a specific ACC manifest.
        
        ACC has relationships with ASCC (which relates to ASCCP → ACC) and BCC (which relates to BCCP → DT).
        This method returns SeqKey objects with all relationships eagerly loaded.
        
        Args:
            acc_manifest_id: Unique identifier of the ACC manifest
            
        Returns:
            list[SeqKey]: List of SeqKey objects with relationships loaded, ordered by their linked list structure
        """
        # Get the ACC manifest information
        acc_manifest = db_exec(
            select(AccManifest)
            .where(AccManifest.acc_manifest_id == acc_manifest_id)
        ).one_or_none()
        
        if not acc_manifest:
            return []
        
        # Find the first seq_key (where prev_seq_key_id is null)
        first_seq_key = db_exec(
            select(SeqKey)
            .options(
                selectinload(SeqKey.ascc_manifest).selectinload(AsccManifest.ascc),
                selectinload(SeqKey.ascc_manifest).selectinload(AsccManifest.to_asccp_manifest).selectinload(AsccpManifest.asccp),
                selectinload(SeqKey.bcc_manifest).selectinload(BccManifest.bcc),
                selectinload(SeqKey.bcc_manifest).selectinload(BccManifest.to_bccp_manifest).selectinload(BccpManifest.bccp),
                selectinload(SeqKey.bcc_manifest).selectinload(BccManifest.to_bccp_manifest).selectinload(BccpManifest.bdt_manifest).selectinload(DtManifest.dt)
            )
            .where(
                and_(
                    SeqKey.from_acc_manifest_id == acc_manifest_id,
                    SeqKey.prev_seq_key_id.is_(None)
                )
            )
        ).one_or_none()
        
        if not first_seq_key:
            return []
        
        # Traverse the linked list to get all associations in order
        seq_keys = []
        current_seq_key = first_seq_key
        
        while current_seq_key:
            seq_keys.append(current_seq_key)
            
            # Move to next seq_key
            if current_seq_key.next_seq_key_id:
                current_seq_key = db_exec(
                    select(SeqKey)
                    .options(
                        selectinload(SeqKey.ascc_manifest).selectinload(AsccManifest.ascc),
                        selectinload(SeqKey.ascc_manifest).selectinload(AsccManifest.to_asccp_manifest).selectinload(AsccpManifest.asccp),
                        selectinload(SeqKey.bcc_manifest).selectinload(BccManifest.bcc),
                        selectinload(SeqKey.bcc_manifest).selectinload(BccManifest.to_bccp_manifest).selectinload(BccpManifest.bccp),
                        selectinload(SeqKey.bcc_manifest).selectinload(BccManifest.to_bccp_manifest).selectinload(BccpManifest.bdt_manifest).selectinload(DtManifest.dt)
                    )
                    .where(SeqKey.seq_key_id == current_seq_key.next_seq_key_id)
                ).one_or_none()
            else:
                current_seq_key = None
        
        return seq_keys
