"""
Service class for managing Context Category operations in connectCenter.

Context Categories provide classification and organization for Context Schemes,
which are used to define business contexts. This service provides comprehensive
functionality for creating, updating, deleting, and querying Context Category
data with support for filtering, pagination, and sorting.
"""

import logging
from datetime import datetime, timezone

from fastapi import HTTPException

from sqlmodel import select, func
from sqlalchemy.orm import selectinload

from services.models import AppUser, CtxCategory, CtxScheme
from services.models.common import Sort, PaginationParams, DateRangeParams, Page
from services.transaction import transaction, db_add, db_get, db_delete, db_refresh, db_flush, db_exec
from services.utils import generate_guid
from services.cache import cache, evict_cache

# Configure logging
logger = logging.getLogger(__name__)


class CtxCategoryService:
    """
    Service class for managing Context Category operations.
    
    This service provides a comprehensive interface for working with Context Categories,
    which provide classification and organization for Context Schemes. Context Categories
    help group related context schemes together, making it easier to manage and organize
    business contexts in the Score platform.
    
    The service uses ContextVar-based transaction management, so it doesn't require
    an engine parameter. The engine is obtained from the context when needed.
    
    Key Features:
    - Create, update, and delete Context Categories
    - Query Context Categories with advanced filtering capabilities
    - Support for pagination, sorting, and date range filtering
    - Automatic loading of related entities (creator, last_updater)
    - Validation and constraint checking (prevents deletion if linked to schemes)
    - SQL injection protection through column whitelisting
    - Automatic GUID generation for new categories
    
    Main Operations:
    - create_ctx_category(): Create a new Context Category with name and optional
      description. Automatically generates a GUID and sets creation/update timestamps.
      Validates input length constraints.
    
    - update_ctx_category(): Update an existing Context Category's name and/or description.
      Returns both the updated category and the original values for audit purposes.
    
    - delete_ctx_category(): Delete a Context Category. Prevents deletion if the category
      has linked Context Schemes, providing detailed error messages with scheme IDs.
    
    - get_ctx_category(): Retrieve a single Context Category by ID with all relationships
      loaded (creator, last_updater).
    
    - get_ctx_categories(): Retrieve paginated lists of Context Categories with optional
      filters for name, description, and date ranges. Supports custom sorting.
    
    Filtering and Sorting:
    - Supports filtering by name and description (case-insensitive partial matching)
    - Date range filtering for creation and last update timestamps
    - Multi-column sorting with ascending/descending order
    - Default sorting by name (ascending)
    - Column whitelist prevents SQL injection attacks
    
    Validation and Constraints:
    - Name: Required, max 45 characters, cannot be empty
    - Description: Optional, max 1000 characters
    - Deletion protection: Cannot delete if linked to Context Schemes
    
    Relationship Loading:
    All queries automatically load related entities using SQLAlchemy's selectinload:
    - Context Category: creator, last_updater
    
    Transaction Management:
    All database operations are wrapped in transactions (read-only for queries,
    read-write for mutations) to ensure data consistency and proper connection management.
    
    Example Usage:
        service = CtxCategoryService(requester=current_user)
        
        # Create a new category
        category = service.create_ctx_category(
            name="Industry Standards",
            description="Categories for industry-specific standards"
        )
        
        # Update a category
        updated, original = service.update_ctx_category(
            ctx_category_id=1,
            name="Updated Name",
            description="Updated description"
        )
        
        # Get paginated categories with filters
        page = service.get_ctx_categories(
            name="Industry",
            pagination=PaginationParams(offset=0, limit=10)
        )
        
        # Delete a category (will fail if linked to schemes)
        service.delete_ctx_category(ctx_category_id=1)
    """

    # Allowed columns for ordering to prevent SQL injection
    allowed_columns_for_order_by = [
        'name',
        'description',
        'creation_timestamp',
        'last_update_timestamp'
    ]

    def __init__(self, requester: AppUser):
        """
        Initialize the service with the authenticated user making the request.
        
        Args:
            requester: The authenticated user making the request
        """
        self.requester = requester

    @transaction(read_only=False)
    def create_ctx_category(self, name: str, description: str = None) -> CtxCategory:
        """
        Create a new context category.
        
        Args:
            name: Short name of the context category (required)
            description: Explanation of what the context category is (optional)
        
        Returns:
            CtxCategory: The created context category
        """
        # Validate input parameters
        if not name or not name.strip():
            raise HTTPException(
                status_code=400,
                detail="Name is required and cannot be empty"
            )

        if len(name.strip()) > 45:
            raise HTTPException(
                status_code=400,
                detail="Name cannot exceed 45 characters"
            )

        if description and len(description) > 1000:
            raise HTTPException(
                status_code=400,
                detail="Description cannot exceed 1000 characters"
            )

        # Generate a GUID for the context category
        guid = generate_guid()

        # Create the context category
        ctx_category = CtxCategory(
            guid=guid,
            name=name.strip(),
            description=description.strip() if description else None,
            created_by=self.requester.app_user_id,
            last_updated_by=self.requester.app_user_id,
            creation_timestamp=datetime.now(timezone.utc),
            last_update_timestamp=datetime.now(timezone.utc)
        )

        # Save to database
        db_add(ctx_category)
        db_flush()  # Flush to get the ID assigned

        # Evict cache entries (new entity affects list queries)
        evict_cache("ctx_category.get_ctx_categories")

        # Eager load the relationships to avoid lazy loading issues
        ctx_category_with_relations = self._get_category_with_relations(ctx_category.ctx_category_id)

        return ctx_category_with_relations

    def _get_base_query_with_eager_loading(self):
        """
        Get a base query for CtxCategory with eager loading of relationships.
        
        Returns:
            Select statement with eager loading options applied
        """
        return select(CtxCategory).options(
            selectinload(CtxCategory.creator),
            selectinload(CtxCategory.last_updater)
        )

    def _get_category_with_relations(self, ctx_category_id: int) -> CtxCategory | None:
        """
        Get a context category by ID with relationships eagerly loaded.
        
        Args:
            ctx_category_id: ID of the context category to retrieve
            
        Returns:
            CtxCategory if found, None otherwise
        """
        return db_exec(
            self._get_base_query_with_eager_loading()
            .where(CtxCategory.ctx_category_id == ctx_category_id)
        ).first()

    @transaction(read_only=False)
    def update_ctx_category(self, ctx_category_id: int, name: str = None, description: str = None) -> tuple[
        CtxCategory, dict]:
        """
        Update an existing context category.
        
        Args:
            ctx_category_id: ID of the context category to update
            name: New name for the context category (optional)
            description: New description for the context category (optional)
        
        Returns:
            tuple[CtxCategory, dict]: The updated context category and original values
        """
        # Validate that at least one field is provided
        if name is None and description is None:
            raise HTTPException(
                status_code=400,
                detail="At least one of 'name' or 'description' must be provided"
            )

        # Validate input parameters
        if name is not None:
            if not name.strip():
                raise HTTPException(
                    status_code=400,
                    detail="Name cannot be empty"
                )

            if len(name.strip()) > 45:
                raise HTTPException(
                    status_code=400,
                    detail="Name cannot exceed 45 characters"
                )

        if description is not None and len(description) > 1000:
            raise HTTPException(
                status_code=400,
                detail="Description cannot exceed 1000 characters"
            )

        # Find the context category
        ctx_category = db_get(CtxCategory, ctx_category_id)
        if not ctx_category:
            raise HTTPException(
                status_code=404,
                detail=f"Context category with ID {ctx_category_id} not found"
            )

        # Capture original values
        original_values = {
            "name": ctx_category.name,
            "description": ctx_category.description
        }

        # Update the fields only if provided
        if name is not None:
            ctx_category.name = name.strip()
        if description is not None:
            ctx_category.description = description.strip() if description.strip() else None
        ctx_category.last_updated_by = self.requester.app_user_id
        ctx_category.last_update_timestamp = datetime.now(timezone.utc)

        db_flush()  # Flush changes to make sure they're visible in the query below

        # Evict cache entries for this category
        evict_cache("ctx_category.get_ctx_categories")  # Evict all list queries
        evict_cache("ctx_category.get_ctx_category", ctx_category_id=ctx_category_id)  # Evict specific get query

        # Eager load the relationships
        ctx_category_with_relations = self._get_category_with_relations(ctx_category_id)

        return ctx_category_with_relations, original_values

    @transaction(read_only=False)
    def delete_ctx_category(self, ctx_category_id: int) -> bool:
        """
        Delete a context category.
        
        Args:
            ctx_category_id: ID of the context category to delete
        
        Returns:
            bool: True if deleted successfully
        
        Raises:
            HTTPException: If the context category has linked context schemes
        """
        # Validate input parameters
        if not ctx_category_id or ctx_category_id <= 0:
            raise HTTPException(
                status_code=400,
                detail="Context category ID must be a positive integer"
            )

        # Find the context category
        ctx_category = db_get(CtxCategory, ctx_category_id)
        if not ctx_category:
            raise HTTPException(
                status_code=404,
                detail=f"Context category with ID {ctx_category_id} not found"
            )

        # Check for linked context schemes
        linked_schemes = db_exec(
            select(CtxScheme).where(CtxScheme.ctx_category_id == ctx_category_id)
        ).all()

        if linked_schemes:
            scheme_ids = [scheme.ctx_scheme_id for scheme in linked_schemes]
            raise HTTPException(
                status_code=409,
                detail=f"Cannot delete context category '{ctx_category.name}' (ID: {ctx_category_id}) because it has {len(linked_schemes)} linked context scheme(s) with IDs: {scheme_ids}. Please delete the linked context schemes first using the delete_context_scheme tool, or update them to use a different context category."
            )

        # Delete the context category
        db_delete(ctx_category)

        # Evict cache entries for this category
        evict_cache("ctx_category.get_ctx_categories")  # Evict all list queries
        evict_cache("ctx_category.get_ctx_category", ctx_category_id=ctx_category_id)  # Evict specific get query

        return True

    @cache(key_prefix="ctx_category.get_ctx_category")
    @transaction(read_only=True)
    def get_ctx_category(self, ctx_category_id: int) -> CtxCategory:
        """
        Get a context category by ID.
        
        Args:
            ctx_category_id: ID of the context category to retrieve
        
        Returns:
            CtxCategory: The context category with relationships loaded
        """
        # Validate input parameters
        if not ctx_category_id or ctx_category_id <= 0:
            raise HTTPException(
                status_code=400,
                detail="Context category ID must be a positive integer"
            )

        # Find the context category with eager loading
        ctx_category = self._get_category_with_relations(ctx_category_id)

        if not ctx_category:
            raise HTTPException(
                status_code=404,
                detail=f"Context category with ID {ctx_category_id} not found"
            )

        return ctx_category

    @cache(key_prefix="ctx_category.get_ctx_categories")
    @transaction(read_only=True)
    def get_ctx_categories(self, name: str = None, description: str = None,
                           created_on: DateRangeParams = None, last_updated_on: DateRangeParams = None,
                           pagination: PaginationParams = PaginationParams(offset=0, limit=10),
                           sort_list: list[Sort] = None) -> Page:
        """
        Get a paginated list of context categories.
        
        Args:
            name: Filter by name (partial match, case-insensitive)
            description: Filter by description (partial match, case-insensitive)
            created_on: Date range parameters for filtering by creation date
            last_updated_on: Date range parameters for filtering by last update date
            pagination: Pagination parameters (offset and limit)
            sort_list: List of Sort objects for ordering
            
        Returns:
            Page: Paginated result with total count and items
        """

        # Set default pagination if not provided
        if pagination is None:
            pagination = PaginationParams(offset=0, limit=10)

        # Build base query
        query = select(CtxCategory)

        # Apply filters
        query = self._apply_filters(query, name, description, created_on, last_updated_on)

        # Apply sorting
        query = self._apply_sorting(query, sort_list)

        # Get total count with same filters
        count_query = select(func.count(CtxCategory.ctx_category_id))
        count_query = self._apply_filters(count_query, name, description, created_on, last_updated_on)
        total = db_exec(count_query).one()

        # Apply pagination
        query = query.offset(pagination.offset).limit(pagination.limit)

        # Execute query with eager loading
        # Add eager loading options to the filtered and sorted query
        ctx_categories = db_exec(
            query.options(
                selectinload(CtxCategory.creator),
                selectinload(CtxCategory.last_updater)
            )
        ).all()

        return Page(
            total=total,
            offset=pagination.offset,
            limit=pagination.limit,
            items=list(ctx_categories)
        )

    def _apply_filters(self, query, name: str = None, description: str = None,
                      created_on: DateRangeParams = None, last_updated_on: DateRangeParams = None):
        """
        Apply filters to a query for context categories.
        
        Args:
            query: The base query to apply filters to
            name: Filter by name (partial match, case-insensitive)
            description: Filter by description (partial match, case-insensitive)
            created_on: Date range parameters for filtering by creation date
            last_updated_on: Date range parameters for filtering by last update date
            
        Returns:
            Query with filters applied
        """
        if name:
            query = query.where(CtxCategory.name.ilike(f"%{name}%"))

        if description:
            query = query.where(CtxCategory.description.ilike(f"%{description}%"))

        if created_on:
            if created_on.before:
                query = query.where(CtxCategory.creation_timestamp >= created_on.before)
            if created_on.after:
                query = query.where(CtxCategory.creation_timestamp < created_on.after)

        if last_updated_on:
            if last_updated_on.before:
                query = query.where(CtxCategory.last_update_timestamp >= last_updated_on.before)
            if last_updated_on.after:
                query = query.where(CtxCategory.last_update_timestamp < last_updated_on.after)

        return query

    def _apply_sorting(self, query, sort_list: list[Sort] = None):
        """
        Apply sorting to a query for context categories.
        
        Args:
            query: The query to apply sorting to
            sort_list: List of Sort objects for ordering
            
        Returns:
            Query with sorting applied
        """
        if sort_list:
            # Validate sort columns against allowed list
            for sort_obj in sort_list:
                if sort_obj.column not in self.allowed_columns_for_order_by:
                    raise HTTPException(
                        status_code=400,
                        detail=f"Invalid sort column: '{sort_obj.column}'. Allowed columns: {', '.join(self.allowed_columns_for_order_by)}"
                    )

            # Apply sorting
            column_map = {
                'name': CtxCategory.name,
                'description': CtxCategory.description,
                'creation_timestamp': CtxCategory.creation_timestamp,
                'last_update_timestamp': CtxCategory.last_update_timestamp
            }

            for sort_obj in sort_list:
                column = column_map[sort_obj.column]
                if sort_obj.direction == 'desc':
                    query = query.order_by(column.desc())
                else:
                    query = query.order_by(column)
        else:
            # Default ordering by name
            query = query.order_by(CtxCategory.name)

        return query
