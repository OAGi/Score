"""
Service class for managing Library operations in connectCenter.

Libraries are organizational containers that group related releases and components
in the Score platform. This service provides functionality for querying and retrieving
library information with support for filtering, pagination, and sorting.
"""

import logging

from fastapi import HTTPException

from sqlmodel import select, func
from sqlalchemy.orm import selectinload

from services.models import Library
from services.models.common import Sort, PaginationParams, DateRangeParams, Page
from services.transaction import transaction, db_exec
from services.cache import cache

# Configure logging
logger = logging.getLogger(__name__)


class LibraryService:
    """
    Service class for managing Library operations.
    
    This service provides a comprehensive interface for working with Libraries,
    which are organizational containers that group related releases, components,
    and other entities in the Score platform. Libraries help organize and manage
    collections of business information standards.
    
    Key Features:
    - Query libraries with advanced filtering capabilities
    - Retrieve individual libraries by ID
    - Support for pagination, sorting, and date range filtering
    - Automatic loading of related entities (creator, last_updater)
    - SQL injection protection through column whitelisting
    
    Main Operations:
    - get_library(): Retrieve a single library by its ID, including creator and
      last_updater information.
    
    - get_libraries(): Retrieve paginated lists of libraries with optional filters
      for name, type, organization, domain, state, description, and is_default flag.
      Supports date range filtering and custom sorting.
    
    Filtering Capabilities:
    - Text fields (name, type, organization, domain, state, description): Case-insensitive partial matching
    - Boolean field (is_default): Exact matching
    - Date range filtering for creation and last update timestamps
    - All filters can be combined for complex queries
    
    Sorting:
    - Supports sorting by multiple columns (name, type, organization, domain, state, etc.)
    - Multi-column sorting with ascending/descending order
    - Default sorting by creation timestamp (newest first)
    - Column whitelist prevents SQL injection attacks
    
    Relationship Loading:
    All queries automatically load related entities using SQLAlchemy's selectinload:
    - Library: creator, last_updater
    
    Transaction Management:
    All database operations are wrapped in read-only transactions to ensure
    data consistency and proper connection management.
    
    Example Usage:
        service = LibraryService()
        
        # Get a specific library
        library = service.get_library(library_id=1)
        
        # Get paginated libraries with filters
        page = service.get_libraries(
            name="ISO",
            type="Standard",
            pagination=PaginationParams(offset=0, limit=10),
            sort_list=[Sort(column="name", direction="asc")]
        )
    """

    # Allowed columns for ordering to prevent SQL injection
    allowed_columns_for_order_by = [
        'name',
        'type',
        'organization',
        'domain',
        'state',
        'description',
        'is_default',
        'creation_timestamp',
        'last_update_timestamp'
    ]

    def __init__(self):
        """
        Initialize the service.
        """
        pass

    @cache(key_prefix="library.get_library")
    @transaction(read_only=True)
    def get_library(self, library_id: int) -> Library:
        """
        Get a library by ID.
        
        Args:
            library_id: The ID of the library to retrieve
        
        Returns:
            Library: The library with the specified ID
        
        Raises:
            HTTPException: If the library is not found
        """
        library = db_exec(
            select(Library)
            .options(
                selectinload(Library.creator),
                selectinload(Library.last_updater)
            )
            .where(Library.library_id == library_id)
        ).first()

        if not library:
            raise HTTPException(
                status_code=404,
                detail=f"Library with ID {library_id} not found"
            )

        return library

    @cache(key_prefix="library.get_libraries")
    @transaction(read_only=True)
    def get_libraries(self,
                      name: str = None, type: str = None, organization: str = None,
                      domain: str = None, state: str = None, description: str = None, is_default: bool = None,
                      created_on_params: DateRangeParams = None, last_updated_on_params: DateRangeParams = None,
                      pagination: PaginationParams = PaginationParams(offset=0, limit=10),
                      sort_list: list[Sort] = None) -> Page:
        """
        Get libraries with optional filtering and pagination.
        
        Args:
            pagination: Pagination parameters (offset, limit)
            sort_list: List of sort specifications (optional)
            created_on_params: Date range filter for creation timestamp (optional)
            last_updated_on_params: Date range filter for last update timestamp (optional)
            name: Filter by library name (optional)
            type: Filter by library type (optional)
            organization: Filter by organization (optional)
            domain: Filter by domain (optional)
            state: Filter by state (optional)
            description: Filter by description (optional)
            is_default: Filter by default library flag (optional)
        
        Returns:
            Page: Paginated response containing libraries and pagination metadata
        """
        # Set default pagination if not provided
        if pagination is None:
            pagination = PaginationParams(offset=0, limit=10)

        # Build the base query
        query = select(Library).options(
            selectinload(Library.creator),
            selectinload(Library.last_updater)
        )

        # Apply filters
        query = self._apply_filters(query, name, type, organization, domain, state,
                                   description, is_default, created_on_params,
                                   last_updated_on_params)

        # Get total count
        count_query = select(func.count()).select_from(query.subquery())
        total_count = db_exec(count_query).one()

        # Apply sorting
        query = self._apply_sorting(query, sort_list)

        # Apply pagination
        query = query.offset(pagination.offset).limit(pagination.limit)

        # Execute query
        libraries = db_exec(query).all()

        # Create Page object
        return Page(
            total=total_count,
            offset=pagination.offset,
            limit=pagination.limit,
            items=list(libraries)
        )

    def _apply_filters(self, query, name: str = None, type: str = None,
                      organization: str = None, domain: str = None, state: str = None,
                      description: str = None, is_default: bool = None,
                      created_on_params: DateRangeParams = None,
                      last_updated_on_params: DateRangeParams = None):
        """
        Apply filters to a query for libraries.
        
        Args:
            query: The base query to apply filters to
            name: Filter by library name (partial match)
            type: Filter by library type (partial match)
            organization: Filter by organization (partial match)
            domain: Filter by domain (partial match)
            state: Filter by state (partial match)
            description: Filter by description (partial match)
            is_default: Filter by default library flag
            created_on_params: Date range filter for creation timestamp
            last_updated_on_params: Date range filter for last update timestamp
            
        Returns:
            Query with filters applied
        """
        if name:
            query = query.where(Library.name.ilike(f"%{name}%"))

        if type:
            query = query.where(Library.type.ilike(f"%{type}%"))

        if organization:
            query = query.where(Library.organization.ilike(f"%{organization}%"))

        if domain:
            query = query.where(Library.domain.ilike(f"%{domain}%"))

        if state:
            query = query.where(Library.state.ilike(f"%{state}%"))

        if description:
            query = query.where(Library.description.ilike(f"%{description}%"))

        if is_default is not None:
            query = query.where(Library.is_default == is_default)

        if created_on_params:
            if created_on_params.before:
                query = query.where(Library.creation_timestamp >= created_on_params.before)
            if created_on_params.after:
                query = query.where(Library.creation_timestamp <= created_on_params.after)

        if last_updated_on_params:
            if last_updated_on_params.before:
                query = query.where(Library.last_update_timestamp >= last_updated_on_params.before)
            if last_updated_on_params.after:
                query = query.where(Library.last_update_timestamp <= last_updated_on_params.after)

        return query

    def _apply_sorting(self, query, sort_list: list[Sort] = None):
        """
        Apply sorting to a query for libraries.
        
        Args:
            query: The query to apply sorting to
            sort_list: List of Sort objects for ordering
            
        Returns:
            Query with sorting applied
        """
        if sort_list:
            for sort in sort_list:
                if sort.column in self.allowed_columns_for_order_by:
                    column = getattr(Library, sort.column)
                    if sort.direction == 'desc':
                        query = query.order_by(column.desc())
                    else:
                        query = query.order_by(column.asc())
        else:
            query = query.order_by(Library.creation_timestamp.desc())

        return query
