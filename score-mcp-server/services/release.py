"""
Service class for managing Release operations in connectCenter.

Releases represent versions of business information standards within a library.
This service provides functionality for querying releases, retrieving release
dependencies, and managing release-related data with support for filtering,
pagination, and sorting.
"""

import logging

from fastapi import HTTPException

from sqlmodel import select, func
from sqlalchemy.orm import selectinload

from services.models import Release
from services.models.common import Sort, PaginationParams, DateRangeParams, Page
from services.transaction import transaction, db_exec
from services.cache import cache

# Configure logging
logger = logging.getLogger(__name__)


class ReleaseService:
    """
    Service class for managing Release operations.
    
    This service provides a comprehensive interface for working with Releases,
    which represent versions of business information standards within a library.
    Releases organize components, code lists, and other entities into versioned
    collections. The service handles querying, filtering, pagination, and dependency
    management for releases.
    
    Key Features:
    - Query releases with advanced filtering capabilities
    - Retrieve individual releases by ID with full relationship loading
    - Get release dependency chains (recursive dependency resolution)
    - Support for pagination, sorting, and date range filtering
    - Automatic exclusion of 'Working' releases from queries
    - Automatic loading of related entities (library, namespace, creator, etc.)
    - SQL injection protection through column whitelisting
    
    Main Operations:
    - get_release(): Retrieve a single release by its ID, including library, namespace,
      creator, last_updater, and linked releases (prev_release, next_release).
    
    - get_releases(): Retrieve paginated lists of releases with optional filters for
      library_id, release_num, and state. Supports date range filtering and custom
      sorting. Automatically excludes releases with release_num='Working'.
    
    - get_dependent_releases(): Recursively retrieve all release IDs that a given
      release depends on, either directly or indirectly. Includes protection against
      circular dependencies.
    
    Filtering and Sorting:
    - Supports filtering by library_id (exact match), release_num (partial match), and state (exact match)
    - Date range filtering for creation and last update timestamps
    - Multi-column sorting with ascending/descending order
    - Default sorting by creation timestamp (newest first)
    - Column whitelist prevents SQL injection attacks
    - 'Working' releases are automatically excluded from results
    
    Relationship Loading:
    All queries automatically load related entities using SQLAlchemy's selectinload:
    - Release: library, namespace, creator, last_updater, prev_release, next_release
    
    Dependency Management:
    The get_dependent_releases() method performs recursive traversal of the release
    dependency tree, finding all releases that a given release depends on. It includes
    protection against circular dependencies to prevent infinite loops.
    
    Transaction Management:
    All database operations are wrapped in read-only transactions to ensure
    data consistency and proper connection management.
    
    Example Usage:
        service = ReleaseService()
        
        # Get a specific release
        release = service.get_release(release_id=1)
        
        # Get paginated releases with filters
        page = service.get_releases(
            library_id=1,
            state="Published",
            pagination=PaginationParams(offset=0, limit=20)
        )
        
        # Get all dependencies for a release
        dependencies = service.get_dependent_releases(release_id=1)
    """

    # Allowed columns for ordering to prevent SQL injection
    allowed_columns_for_order_by = [
        'release_num',
        'state',
        'creation_timestamp',
        'last_update_timestamp'
    ]

    def __init__(self):
        """
        Initialize the service.
        """
        pass

    @cache(key_prefix="release.get_release")
    @transaction(read_only=True)
    def get_release(self, release_id: int) -> Release:
        """
        Get a release by ID.
        
        Args:
            release_id: The ID of the release to retrieve
        
        Returns:
            Release: The release with the specified ID
        
        Raises:
            HTTPException: If the release is not found
        """
        release = db_exec(
            select(Release)
            .options(
                selectinload(Release.library),
                selectinload(Release.namespace),
                selectinload(Release.creator),
                selectinload(Release.last_updater),
                selectinload(Release.prev_release),
                selectinload(Release.next_release)
            )
            .where(Release.release_id == release_id)
        ).first()

        if not release:
            raise HTTPException(
                status_code=404,
                detail=f"Release with ID {release_id} not found"
            )

        return release

    @cache(key_prefix="release.get_releases")
    @transaction(read_only=True)
    def get_releases(self,
                     library_id: int = None, release_num: str = None, state: str = None,
                     created_on_params: DateRangeParams = None, last_updated_on_params: DateRangeParams = None,
                     pagination: PaginationParams = PaginationParams(offset=0, limit=10),
                     sort_list: list[Sort] = None) -> Page:
        """
        Get releases with optional filtering and pagination.
        
        Args:
            pagination: Pagination parameters (offset, limit)
            sort_list: List of sort specifications (optional)
            created_on_params: Date range filter for creation timestamp (optional)
            last_updated_on_params: Date range filter for last update timestamp (optional)
            library_id: Filter by library ID (optional)
            release_num: Filter by release number (optional)
            state: Filter by state (optional)
        
        Returns:
            Page: Paginated response containing releases and pagination metadata
            
        Note: Releases with release_num='Working' are automatically excluded from results.
        """
        # Set default pagination if not provided
        if pagination is None:
            pagination = PaginationParams(offset=0, limit=10)

        # Build the base query
        query = select(Release).options(
            selectinload(Release.library),
            selectinload(Release.namespace),
            selectinload(Release.creator),
            selectinload(Release.last_updater),
            selectinload(Release.prev_release),
            selectinload(Release.next_release)
        )

        # Apply filters
        query = self._apply_filters(query, library_id, release_num, state,
                                   created_on_params, last_updated_on_params)

        # Get total count
        count_query = select(func.count()).select_from(query.subquery())
        total_count = db_exec(count_query).one()

        # Apply sorting
        query = self._apply_sorting(query, sort_list)

        # Apply pagination
        query = query.offset(pagination.offset).limit(pagination.limit)

        # Execute query
        releases = db_exec(query).all()

        # Create Page object
        return Page(
            total=total_count,
            offset=pagination.offset,
            limit=pagination.limit,
            items=list(releases)
        )

    def _apply_filters(self, query, library_id: int = None, release_num: str = None,
                      state: str = None, created_on_params: DateRangeParams = None,
                      last_updated_on_params: DateRangeParams = None):
        """
        Apply filters to a query for releases.
        
        Args:
            query: The base query to apply filters to
            library_id: Filter by library ID
            release_num: Filter by release number (partial match)
            state: Filter by state
            created_on_params: Date range filter for creation timestamp
            last_updated_on_params: Date range filter for last update timestamp
            
        Returns:
            Query with filters applied
        """
        if library_id:
            query = query.where(Release.library_id == library_id)

        if release_num:
            query = query.where(Release.release_num.ilike(f"%{release_num}%"))

        if state:
            query = query.where(Release.state == state)
        
        # Always exclude releases with release_num = 'Working'
        query = query.where(Release.release_num != 'Working')

        if created_on_params:
            if created_on_params.before:
                query = query.where(Release.creation_timestamp >= created_on_params.before)
            if created_on_params.after:
                query = query.where(Release.creation_timestamp <= created_on_params.after)

        if last_updated_on_params:
            if last_updated_on_params.before:
                query = query.where(Release.last_update_timestamp >= last_updated_on_params.before)
            if last_updated_on_params.after:
                query = query.where(Release.last_update_timestamp <= last_updated_on_params.after)

        return query

    def _apply_sorting(self, query, sort_list: list[Sort] = None):
        """
        Apply sorting to a query for releases.
        
        Args:
            query: The query to apply sorting to
            sort_list: List of Sort objects for ordering
            
        Returns:
            Query with sorting applied
        """
        if sort_list:
            for sort in sort_list:
                if sort.column in self.allowed_columns_for_order_by:
                    column = getattr(Release, sort.column)
                    if sort.direction == 'desc':
                        query = query.order_by(column.desc())
                    else:
                        query = query.order_by(column.asc())
        else:
            query = query.order_by(Release.creation_timestamp.desc())

        return query

    @cache(key_prefix="release.get_dependent_releases")
    @transaction(read_only=True)
    def get_dependent_releases(self, release_id: int) -> list[int]:
        """
        Get all release IDs that the given release depends on, recursively.
        
        This method performs a recursive traversal of the dependency tree to find
        all releases that the given release depends on, either directly or indirectly.
        It includes protection against circular dependencies.
        
        Args:
            release_id: The ID of the release to get dependencies for
        
        Returns:
            list[int]: List of release IDs that the given release depends on (recursively)
        """
        from services.models import ReleaseDep
        
        # Use a set to avoid duplicates and track processed releases to prevent circular dependencies
        all_dependent_releases = set()
        processed_releases = set()  # Track processed releases to prevent infinite loops
        releases_to_process = [release_id]
        
        # Recursively find all dependencies
        while releases_to_process:
            current_release_id = releases_to_process.pop(0)
            
            # Skip if we've already processed this release (prevents circular dependencies)
            if current_release_id in processed_releases:
                continue
            
            processed_releases.add(current_release_id)
            
            # Get direct dependencies of the current release
            direct_dependencies = db_exec(
                select(ReleaseDep.depend_on_release_id)
                .where(ReleaseDep.release_id == current_release_id)
            ).all()
            
            # Add new dependencies to the set and queue for further processing
            for dep_release_id in direct_dependencies:
                if dep_release_id not in all_dependent_releases and dep_release_id not in processed_releases:
                    all_dependent_releases.add(dep_release_id)
                    releases_to_process.append(dep_release_id)
        
        return list(all_dependent_releases)
