"""
Service class for managing Data Type operations in connectCenter.

Data Types define the structure and constraints for data elements in business
information exchanges. This service provides comprehensive functionality for
querying, filtering, and retrieving Data Type data with support for pagination,
sorting, and date range filtering, including dependency-aware release queries.
"""

import logging

from fastapi import HTTPException

from sqlmodel import select, func
from sqlalchemy.orm import selectinload

from services.models import Dt, DtManifest, DtScManifest, Release
from .release import ReleaseService
from services.models.common import Sort, PaginationParams, DateRangeParams, Page
from services.transaction import transaction, db_exec
from services.cache import cache

# Configure logging
logger = logging.getLogger(__name__)


class DataTypeService:
    """
    Service class for managing Data Type operations.
    
    This service provides a comprehensive interface for working with Data Types,
    which define the structure and constraints for data elements in business
    information exchanges. The service handles querying, filtering, pagination,
    and retrieval of Data Type data with full relationship loading and dependency
    management.
    
    Key Features:
    - Query Data Types by release with automatic inclusion of dependent releases
    - Retrieve individual Data Types by ID or manifest ID
    - Get associated supplementary component manifests for Data Types
    - Support for pagination, sorting, and date range filtering
    - Automatic loading of related entities (namespace, creator, owner, release, etc.)
    - SQL injection protection through column whitelisting
    - Dependency-aware queries that include data types from dependent releases
    
    Main Operations:
    - get_data_types_by_release(): Retrieve paginated lists of Data Types filtered
      by release. Automatically includes Data Types from all dependent releases
      (recursively). Supports optional filters for DEN (Dictionary Entry Name) and
      representation_term, with date range filtering and custom sorting.
    
    - get_data_type_by_id(): Retrieve a single Data Type by its internal ID,
      including all related entities (namespace, creator, owner, release, log,
      based_dt_manifest).
    
    - get_data_type_by_manifest_id(): Retrieve a single Data Type by its manifest ID,
      which represents the type in a specific release context. Returns both the
      manifest and all associated supplementary component manifests.
    
    - get_supplementary_components_by_dt_manifest_id(): Retrieve all supplementary
      component manifests associated with a specific Data Type manifest, representing
      the additional components that extend the base data type.
    
    Filtering and Sorting:
    - Supports filtering by DEN (Dictionary Entry Name) using case-insensitive partial matching
      DEN format: '((qualifier) ? qualifier + "_ " : "") + data_type_term + ". Type"'
    - Filtering by representation_term (case-insensitive partial matching)
    - Date range filtering for creation and last update timestamps
    - Multi-column sorting with ascending/descending order
    - Default sorting by creation timestamp (newest first)
    - Column whitelist prevents SQL injection attacks
    
    Release Dependency Management:
    When querying by release, the service automatically includes Data Types from
    all dependent releases (recursively). This ensures that all relevant Data
    Types are available when working with a specific release, including those
    inherited from previous versions.
    
    Relationship Loading:
    All queries automatically load related entities using SQLAlchemy's selectinload:
    - Data Type: namespace, creator, owner, last_updater
    - Manifest: release (with library), log, based_dt_manifest (with dt and release)
    - Supplementary Components: associated dt_sc entities
    
    Transaction Management:
    All database operations are wrapped in read-only transactions to ensure
    data consistency and proper connection management.
    
    Example Usage:
        service = DataTypeService()
        
        # Get paginated data types for a release (includes dependent releases)
        page = service.get_data_types_by_release(
            release_id=1,
            den="Amount. Type",
            pagination=PaginationParams(offset=0, limit=10),
            sort_list=[Sort(column="den", direction="asc")]
        )
        
        # Get a specific data type by manifest ID with supplementary components
        manifest, sc_manifests = service.get_data_type_by_manifest_id(123)
        
        # Get supplementary components for a data type
        sc_manifests = service.get_supplementary_components_by_dt_manifest_id(123)
    """

    # Allowed columns for ordering to prevent SQL injection
    allowed_columns_for_order_by = [
        'den',
        'data_type_term',
        'qualifier',
        'representation_term',
        'six_digit_id',
        'definition',
        'creation_timestamp',
        'last_update_timestamp'
    ]

    def __init__(self):
        """
        Initialize the service.
        """
        pass

    @cache(key_prefix="data_type.get_data_types_by_release")
    @transaction(read_only=True)
    def get_data_types_by_release(
        self,
        release_id: int,
        den: str = None,
        representation_term: str = None,
        created_on_params: DateRangeParams = None,
        last_updated_on_params: DateRangeParams = None,
        pagination: PaginationParams = PaginationParams(offset=0, limit=10),
        sort_list: list[Sort] = None
    ) -> Page:
        """
        Get data types associated with a specific release and its dependent releases.
        
        Args:
            release_id: ID of the release to filter by
            pagination: Pagination parameters
            sort_list: List of sort specifications
            den: Filter by Dictionary Entry Name (DEN) using partial match (case-insensitive).
                DEN format: '((qualifier) ? qualifier + "_ " : "") + data_type_term + ". Type"'
            representation_term: Filter by representation term (partial match)
            created_on_params: Date range filter for creation timestamp
            last_updated_on_params: Date range filter for last update timestamp
        
        Returns:
            Page: Paginated response containing data type manifests with supplementary components included
        """
        # Set default pagination if not provided
        if pagination is None:
            pagination = PaginationParams(offset=0, limit=10)

        # Get dependent releases
        release_service = ReleaseService()
        dependent_release_ids = release_service.get_dependent_releases(release_id)
        
        # Include the original release ID and all dependent release IDs
        all_release_ids = [release_id] + dependent_release_ids
        
        # Build the base query to get manifests with loaded relationships
        query = select(DtManifest).options(
            selectinload(DtManifest.dt).selectinload(Dt.namespace),
            selectinload(DtManifest.dt).selectinload(Dt.creator),
            selectinload(DtManifest.dt).selectinload(Dt.owner),
            selectinload(DtManifest.dt).selectinload(Dt.last_updater),
            selectinload(DtManifest.release).selectinload(Release.library),
            selectinload(DtManifest.log),
            selectinload(DtManifest.based_dt_manifest).selectinload(DtManifest.dt).selectinload(Dt.namespace),
            selectinload(DtManifest.based_dt_manifest).selectinload(DtManifest.release).selectinload(Release.library)
        ).join(Dt).where(DtManifest.release_id.in_(all_release_ids)).distinct()

        # Apply filters
        query = self._apply_filters(query, den, representation_term, created_on_params, last_updated_on_params)

        # Get total count
        count_query = select(func.count()).select_from(query.subquery())
        total_count = db_exec(count_query).one()

        # Apply sorting
        query = self._apply_sorting(query, sort_list)

        # Apply pagination
        query = query.offset(pagination.offset).limit(pagination.limit)

        # Execute query
        manifests = db_exec(query).all()

        # Create Page object
        return Page(
            total=total_count,
            offset=pagination.offset,
            limit=pagination.limit,
            items=list(manifests)
        )

    def _get_base_query_with_eager_loading(self):
        """
        Get a base query for DtManifest with eager loading of relationships.
        
        Returns:
            Select statement with eager loading options applied
        """
        return select(DtManifest).options(
            selectinload(DtManifest.dt).selectinload(Dt.namespace),
            selectinload(DtManifest.dt).selectinload(Dt.creator),
            selectinload(DtManifest.dt).selectinload(Dt.owner),
            selectinload(DtManifest.dt).selectinload(Dt.last_updater),
            selectinload(DtManifest.release).selectinload(Release.library),
            selectinload(DtManifest.log),
            selectinload(DtManifest.based_dt_manifest).selectinload(DtManifest.dt).selectinload(Dt.namespace),
            selectinload(DtManifest.based_dt_manifest).selectinload(DtManifest.release).selectinload(Release.library)
        )

    def _apply_filters(self, query, den: str = None, representation_term: str = None,
                      created_on_params: DateRangeParams = None,
                      last_updated_on_params: DateRangeParams = None):
        """
        Apply filters to a query for data types.
        
        Args:
            query: The base query to apply filters to
            den: Filter by Dictionary Entry Name (DEN) using partial match
            representation_term: Filter by representation term (partial match)
            created_on_params: Date range filter for creation timestamp
            last_updated_on_params: Date range filter for last update timestamp
            
        Returns:
            Query with filters applied
        """
        if den:
            query = query.where(DtManifest.den.ilike(f"%{den}%"))

        if representation_term:
            query = query.where(Dt.representation_term.ilike(f"%{representation_term}%"))

        if created_on_params:
            if created_on_params.before:
                query = query.where(Dt.creation_timestamp >= created_on_params.before)
            if created_on_params.after:
                query = query.where(Dt.creation_timestamp <= created_on_params.after)

        if last_updated_on_params:
            if last_updated_on_params.before:
                query = query.where(Dt.last_update_timestamp >= last_updated_on_params.before)
            if last_updated_on_params.after:
                query = query.where(Dt.last_update_timestamp <= last_updated_on_params.after)

        return query

    def _apply_sorting(self, query, sort_list: list[Sort] = None):
        """
        Apply sorting to a query for data types.
        
        Args:
            query: The query to apply sorting to
            sort_list: List of Sort objects for ordering
            
        Returns:
            Query with sorting applied
        """
        if sort_list:
            # Validate sort columns against allowed list
            for sort in sort_list:
                if sort.column not in self.allowed_columns_for_order_by:
                    raise HTTPException(
                        status_code=400,
                        detail=f"Invalid sort column: '{sort.column}'. Allowed columns: {', '.join(self.allowed_columns_for_order_by)}"
                    )
            
            # Apply sorting
            for sort in sort_list:
                if sort.column == 'den':
                    column = DtManifest.den
                else:
                    column = getattr(Dt, sort.column)
                if sort.direction == 'desc':
                    query = query.order_by(column.desc())
                else:
                    query = query.order_by(column.asc())
        else:
            query = query.order_by(Dt.creation_timestamp.desc())

        return query

    @cache(key_prefix="data_type.get_data_type_by_id")
    @transaction(read_only=True)
    def get_data_type_by_id(self, dt_id: int) -> DtManifest:
        """
        Get a data type by its ID.
        
        Args:
            dt_id: ID of the data type to retrieve
        
        Returns:
            DtManifest: The data type manifest if found
        
        Raises:
            HTTPException: If data type not found
        """
        query = self._get_base_query_with_eager_loading().where(DtManifest.dt_id == dt_id)

        manifest = db_exec(query).first()
        if not manifest:
            raise HTTPException(
                status_code=404,
                detail=f"Data type with ID {dt_id} not found"
            )

        return manifest

    @cache(key_prefix="data_type.get_data_type_by_manifest_id")
    @transaction(read_only=True)
    def get_data_type_by_manifest_id(self, dt_manifest_id: int) -> tuple[DtManifest, list[DtScManifest]]:
        """
        Get a data type by its manifest ID.
        
        Args:
            dt_manifest_id: ID of the data type manifest to retrieve
        
        Returns:
            DtManifest: The data type manifest with loaded relationships if found
        
        Raises:
            HTTPException: If data type manifest not found
        """
        # Get the manifest with all relationships loaded
        manifest_query = self._get_base_query_with_eager_loading().where(
            DtManifest.dt_manifest_id == dt_manifest_id
        )
        manifest = db_exec(manifest_query).first()
        if not manifest:
            raise HTTPException(
                status_code=404,
                detail=f"Data type manifest with ID {dt_manifest_id} not found"
            )

        # Get supplementary component manifests for this manifest
        sc_manifests = self.get_supplementary_components_by_dt_manifest_id(manifest.dt_manifest_id)

        return manifest, sc_manifests

    @cache(key_prefix="data_type.get_supplementary_components_by_dt_manifest_id")
    @transaction(read_only=True)
    def get_supplementary_components_by_dt_manifest_id(self, dt_manifest_id: int) -> list["DtScManifest"]:
        """
        Get supplementary components for a specific data type manifest.
        
        Args:
            dt_manifest_id: The data type manifest ID
            
        Returns:
            list[DtScManifest]: List of supplementary component manifests for the data type
        """
        query = select(DtScManifest).options(
            selectinload(DtScManifest.dt_sc)
        ).where(DtScManifest.owner_dt_manifest_id == dt_manifest_id)
        sc_manifests = db_exec(query).all()
        return list(sc_manifests)
