"""
Service class for managing Namespace operations in connectCenter.

Namespaces provide unique identification and scoping for components and entities
in the Score platform. This service provides functionality for querying and
retrieving namespace information with support for filtering, pagination, and sorting.
"""

import logging

from fastapi import HTTPException

from sqlmodel import select, func
from sqlalchemy.orm import selectinload

from services.models import Namespace
from services.models.common import Sort, PaginationParams, DateRangeParams, Page
from services.transaction import transaction, db_exec
from services.cache import cache

# Configure logging
logger = logging.getLogger(__name__)


class NamespaceService:
    """
    Service class for managing Namespace operations.
    
    This service provides a comprehensive interface for working with Namespaces,
    which provide unique identification and scoping for components and entities
    in the Score platform. Namespaces help organize and distinguish components
    from different sources or standards.
    
    Key Features:
    - Query namespaces with advanced filtering capabilities
    - Retrieve individual namespaces by ID
    - Support for pagination, sorting, and date range filtering
    - Automatic loading of related entities (library, owner, creator, last_updater)
    - SQL injection protection through column whitelisting
    
    Main Operations:
    - get_namespace(): Retrieve a single namespace by its ID, including library,
      owner, creator, and last_updater information.
    
    - get_namespaces(): Retrieve paginated lists of namespaces with optional filters
      for library_id, uri, prefix, and is_std_nmsp flag. Supports date range
      filtering and custom sorting.
    
    Filtering Capabilities:
    - Text fields (uri, prefix): Case-insensitive partial matching
    - Boolean field (is_std_nmsp): Exact matching for standard namespace flag
    - Library ID: Exact matching
    - Date range filtering for creation and last update timestamps
    - All filters can be combined for complex queries
    
    Sorting:
    - Supports sorting by multiple columns (uri, prefix, is_std_nmsp, timestamps)
    - Multi-column sorting with ascending/descending order
    - Default sorting by creation timestamp (newest first)
    - Column whitelist prevents SQL injection attacks
    
    Relationship Loading:
    All queries automatically load related entities using SQLAlchemy's selectinload:
    - Namespace: library, owner, creator, last_updater
    
    Transaction Management:
    All database operations are wrapped in read-only transactions to ensure
    data consistency and proper connection management.
    
    Example Usage:
        service = NamespaceService()
        
        # Get a specific namespace
        namespace = service.get_namespace(namespace_id=1)
        
        # Get paginated namespaces with filters
        page = service.get_namespaces(
            uri="http://www.example.org",
            is_std_nmsp=True,
            pagination=PaginationParams(offset=0, limit=10)
        )
    """

    # Allowed columns for ordering to prevent SQL injection
    allowed_columns_for_order_by = [
        'uri',
        'prefix',
        'is_std_nmsp',
        'creation_timestamp',
        'last_update_timestamp'
    ]

    def __init__(self):
        """
        Initialize the service.
        """
        pass

    @cache(key_prefix="namespace.get_namespace")
    @transaction(read_only=True)
    def get_namespace(self, namespace_id: int) -> Namespace:
        """
        Get a namespace by ID.
        
        Args:
            namespace_id: The ID of the namespace to retrieve
        
        Returns:
            Namespace: The namespace with the specified ID
        
        Raises:
            HTTPException: If the namespace is not found
        """
        namespace = db_exec(
            select(Namespace)
            .options(
                selectinload(Namespace.library),
                selectinload(Namespace.owner),
                selectinload(Namespace.creator),
                selectinload(Namespace.last_updater)
            )
            .where(Namespace.namespace_id == namespace_id)
        ).first()

        if not namespace:
            raise HTTPException(
                status_code=404,
                detail=f"Namespace with ID {namespace_id} not found"
            )

        return namespace

    @cache(key_prefix="namespace.get_namespaces")
    @transaction(read_only=True)
    def get_namespaces(self,
                       library_id: int = None, uri: str = None, prefix: str = None, is_std_nmsp: bool = None,
                       created_on_params: DateRangeParams = None, last_updated_on_params: DateRangeParams = None,
                       pagination: PaginationParams = PaginationParams(offset=0, limit=10),
                       sort_list: list[Sort] = None) -> Page:
        """
        Get namespaces with optional filtering and pagination.
        
        Args:
            pagination: Pagination parameters (offset, limit)
            sort_list: List of sort specifications (optional)
            created_on_params: Date range filter for creation timestamp (optional)
            last_updated_on_params: Date range filter for last update timestamp (optional)
            library_id: Filter by library ID (optional)
            uri: Filter by URI (partial match, optional)
            prefix: Filter by prefix (partial match, optional)
            is_std_nmsp: Filter by standard namespace flag (optional)
        
        Returns:
            Page: Paginated response containing namespaces and pagination metadata
        """
        # Set default pagination if not provided
        if pagination is None:
            pagination = PaginationParams(offset=0, limit=10)

        # Build the base query
        query = select(Namespace).options(
            selectinload(Namespace.library),
            selectinload(Namespace.owner),
            selectinload(Namespace.creator),
            selectinload(Namespace.last_updater)
        )

        # Apply filters
        query = self._apply_filters(query, library_id, uri, prefix, is_std_nmsp,
                                   created_on_params, last_updated_on_params)

        # Get total count
        count_query = select(func.count()).select_from(query.subquery())
        total_count = db_exec(count_query).one()

        # Apply sorting
        query = self._apply_sorting(query, sort_list)

        # Apply pagination
        query = query.offset(pagination.offset).limit(pagination.limit)

        # Execute query
        namespaces = db_exec(query).all()

        # Create Page object
        return Page(
            total=total_count,
            offset=pagination.offset,
            limit=pagination.limit,
            items=list(namespaces)
        )

    def _apply_filters(self, query, library_id: int = None, uri: str = None,
                      prefix: str = None, is_std_nmsp: bool = None,
                      created_on_params: DateRangeParams = None,
                      last_updated_on_params: DateRangeParams = None):
        """
        Apply filters to a query for namespaces.
        
        Args:
            query: The base query to apply filters to
            library_id: Filter by library ID
            uri: Filter by URI (partial match)
            prefix: Filter by prefix (partial match)
            is_std_nmsp: Filter by standard namespace flag
            created_on_params: Date range filter for creation timestamp
            last_updated_on_params: Date range filter for last update timestamp
            
        Returns:
            Query with filters applied
        """
        if library_id:
            query = query.where(Namespace.library_id == library_id)

        if uri:
            query = query.where(Namespace.uri.ilike(f"%{uri}%"))

        if prefix:
            query = query.where(Namespace.prefix.ilike(f"%{prefix}%"))

        if is_std_nmsp is not None:
            query = query.where(Namespace.is_std_nmsp == is_std_nmsp)

        if created_on_params:
            if created_on_params.before:
                query = query.where(Namespace.creation_timestamp >= created_on_params.before)
            if created_on_params.after:
                query = query.where(Namespace.creation_timestamp <= created_on_params.after)

        if last_updated_on_params:
            if last_updated_on_params.before:
                query = query.where(Namespace.last_update_timestamp >= last_updated_on_params.before)
            if last_updated_on_params.after:
                query = query.where(Namespace.last_update_timestamp <= last_updated_on_params.after)

        return query

    def _apply_sorting(self, query, sort_list: list[Sort] = None):
        """
        Apply sorting to a query for namespaces.
        
        Args:
            query: The query to apply sorting to
            sort_list: List of Sort objects for ordering
            
        Returns:
            Query with sorting applied
        """
        if sort_list:
            for sort in sort_list:
                if sort.column in self.allowed_columns_for_order_by:
                    column = getattr(Namespace, sort.column)
                    if sort.direction == 'desc':
                        query = query.order_by(column.desc())
                    else:
                        query = query.order_by(column.asc())
        else:
            query = query.order_by(Namespace.creation_timestamp.desc())

        return query
