"""
Service class for managing Context Scheme operations in connectCenter.

Context Schemes define the structure and values for business contexts, which
are used to specify contextual information for business information entities.
This service provides comprehensive functionality for creating, updating, deleting,
and querying Context Scheme data, including management of Context Scheme Values.
"""

import logging
from datetime import datetime, timezone

from fastapi import HTTPException

from sqlmodel import select, func
from sqlalchemy.orm import selectinload

from services.models import AppUser, CtxCategory, CtxScheme, CtxSchemeValue, BizCtxValue
from services.models.common import Sort, PaginationParams, DateRangeParams, Page
from services.transaction import transaction, db_add, db_get, db_delete, db_refresh, db_flush, db_exec
from services.utils import generate_guid
from services.cache import cache, evict_cache

# Configure logging
logger = logging.getLogger(__name__)


class CtxSchemeService:
    """
    Service class for managing Context Scheme operations.
    
    This service provides a comprehensive interface for working with Context Schemes,
    which define the structure and values for business contexts. Context Schemes
    specify what contextual information can be associated with business information
    entities, and Context Scheme Values represent the actual values that can be used.
    
    Key Features:
    - Create, update, and delete Context Schemes
    - Manage Context Scheme Values (create, update, delete)
    - Query Context Schemes with advanced filtering capabilities
    - Support for pagination, sorting, and date range filtering
    - Automatic loading of related entities (creator, last_updater, category, values)
    - Validation and constraint checking (prevents deletion if linked to business contexts)
    - SQL injection protection through column whitelisting
    - Automatic GUID generation for new schemes and values
    
    Main Operations:
    Context Scheme Management:
    - create_ctx_scheme(): Create a new Context Scheme with scheme_id, scheme_name, and optional
      attributes (description, scheme_agency_id, scheme_version_id, ctx_category_id).
      Automatically generates a GUID.
    
    - update_ctx_scheme(): Update an existing Context Scheme. All fields are optional,
      allowing partial updates. Returns both the updated scheme and original values.
    
    - delete_ctx_scheme(): Delete a Context Scheme and all associated Context Scheme
      Values. Cascades deletion to all related values.
    
    - get_ctx_scheme(): Retrieve a single Context Scheme by ID with all relationships
      and values loaded.
    
    - get_ctx_schemes(): Retrieve paginated lists of Context Schemes with optional
      filters for scheme_id, scheme_name, description, agency_id, version_id,
      category_id, category_name, and date ranges. Supports custom sorting.
    
    Context Scheme Value Management:
    - create_ctx_scheme_value(): Create a new value for a Context Scheme with
      value and optional meaning. Automatically generates a GUID.
    
    - update_ctx_scheme_value(): Update an existing Context Scheme Value's value
      and/or meaning. Returns both updated value and original values.
    
    - delete_ctx_scheme_value(): Delete a Context Scheme Value. Prevents deletion
      if the value is linked to Business Context Values, providing detailed error
      messages with business context IDs.
    
    Filtering and Sorting:
    - Supports filtering by scheme_id, scheme_name, description, scheme_agency_id,
      scheme_version_id, ctx_category_id, and ctx_category_name (case-insensitive
      partial matching for text fields)
    - Date range filtering for creation and last update timestamps
    - Multi-column sorting with ascending/descending order
    - Default sorting by scheme_id (ascending)
    - Column whitelist prevents SQL injection attacks
    
    Validation and Constraints:
    - scheme_id: Required, max 45 characters, cannot be empty
    - scheme_name: Required, max 255 characters, cannot be empty
    - description: Optional, max 1000 characters
    - scheme_agency_id: Optional, max 45 characters
    - scheme_version_id: Optional, max 45 characters
    - value: Required for values, max 100 characters
    - meaning: Optional for values, max 1000 characters
    - Deletion protection: Cannot delete Context Scheme Value if linked to Business Context Values
    
    Relationship Loading:
    All queries automatically load related entities using SQLAlchemy's selectinload:
    - Context Scheme: creator, last_updater, ctx_category, ctx_scheme_values
    
    Transaction Management:
    All database operations are wrapped in transactions (read-only for queries,
    read-write for mutations) to ensure data consistency and proper connection management.
    
    Example Usage:
        service = CtxSchemeService(requester=current_user)
        
        # Create a new scheme
        scheme = service.create_ctx_scheme(
            scheme_id="ISO3166-1",
            scheme_name="Country Codes",
            description="ISO 3166-1 country codes",
            ctx_category_id=1
        )
        
        # Create a value for the scheme
        value = service.create_ctx_scheme_value(
            ctx_scheme_id=scheme.ctx_scheme_id,
            value="US",
            meaning="United States"
        )
        
        # Get paginated schemes with filters
        page = service.get_ctx_schemes(
            scheme_id="ISO",
            ctx_category_name="Geography",
            pagination=PaginationParams(offset=0, limit=10)
        )
        
        # Delete a scheme (cascades to values)
        service.delete_ctx_scheme(ctx_scheme_id=1)
    """

    # Allowed columns for ordering to prevent SQL injection
    allowed_columns_for_order_by = [
        'scheme_id',
        'scheme_name',
        'description',
        'scheme_agency_id',
        'scheme_version_id',
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
    def create_ctx_scheme(self, scheme_id: str, scheme_name: str,
                          description: str = None, scheme_agency_id: str = None,
                          scheme_version_id: str = None, ctx_category_id: int = None) -> CtxScheme:
        """
        Create a new context scheme.
        
        Args:
            scheme_id: External identification of the scheme (required)
            scheme_name: Pretty print name of the context scheme (required)
            description: Description of the context scheme (optional)
            scheme_agency_id: Identification of the agency maintaining the scheme (optional)
            scheme_version_id: Version number of the context scheme (optional)
            ctx_category_id: Foreign key to the CTX_CATEGORY table (optional)
        
        Returns:
            CtxScheme: The created context scheme
        """
        # Validate input parameters
        if not scheme_id or not scheme_id.strip():
            raise HTTPException(
                status_code=400,
                detail="Scheme ID is required and cannot be empty"
            )

        if len(scheme_id.strip()) > 45:
            raise HTTPException(
                status_code=400,
                detail="Scheme ID cannot exceed 45 characters"
            )

        if not scheme_name or not scheme_name.strip():
            raise HTTPException(
                status_code=400,
                detail="Scheme name is required and cannot be empty"
            )

        if len(scheme_name.strip()) > 255:
            raise HTTPException(
                status_code=400,
                detail="Scheme name cannot exceed 255 characters"
            )

        if description and len(description) > 1000:
            raise HTTPException(
                status_code=400,
                detail="Description cannot exceed 1000 characters"
            )

        if scheme_agency_id and len(scheme_agency_id) > 45:
            raise HTTPException(
                status_code=400,
                detail="Scheme agency ID cannot exceed 45 characters"
            )

        if scheme_version_id and len(scheme_version_id) > 45:
            raise HTTPException(
                status_code=400,
                detail="Scheme version ID cannot exceed 45 characters"
            )

        # Generate a GUID for the context scheme
        guid = generate_guid()

        # Create the context scheme
        ctx_scheme = CtxScheme(
            guid=guid,
            scheme_id=scheme_id.strip(),
            scheme_name=scheme_name.strip(),
            description=description.strip() if description else None,
            scheme_agency_id=scheme_agency_id.strip() if scheme_agency_id else "",
            scheme_version_id=scheme_version_id.strip() if scheme_version_id else "",
            ctx_category_id=ctx_category_id,
            created_by=self.requester.app_user_id,
            last_updated_by=self.requester.app_user_id,
            creation_timestamp=datetime.now(timezone.utc),
            last_update_timestamp=datetime.now(timezone.utc)
        )

        # Save to database
        db_add(ctx_scheme)
        db_flush()  # Flush to get the ID assigned

        # Evict cache entries (new entity affects list queries)
        evict_cache("ctx_scheme.get_ctx_schemes")

        # Eager load the relationships to avoid lazy loading issues
        ctx_scheme_with_relations = self._get_scheme_with_relations(ctx_scheme.ctx_scheme_id)

        return ctx_scheme_with_relations

    def _get_base_query_with_eager_loading(self):
        """
        Get a base query for CtxScheme with eager loading of relationships.
        
        Returns:
            Select statement with eager loading options applied
        """
        return select(CtxScheme).options(
            selectinload(CtxScheme.creator),
            selectinload(CtxScheme.last_updater),
            selectinload(CtxScheme.ctx_category)
        )

    def _get_scheme_with_relations(self, ctx_scheme_id: int, include_values: bool = False) -> CtxScheme | None:
        """
        Get a context scheme by ID with relationships eagerly loaded.
        
        Args:
            ctx_scheme_id: ID of the context scheme to retrieve
            include_values: Whether to include ctx_scheme_values in eager loading
            
        Returns:
            CtxScheme if found, None otherwise
        """
        query = self._get_base_query_with_eager_loading()
        if include_values:
            query = query.options(selectinload(CtxScheme.ctx_scheme_values))
        
        return db_exec(
            query.where(CtxScheme.ctx_scheme_id == ctx_scheme_id)
        ).first()

    @transaction(read_only=False)
    def update_ctx_scheme(self, ctx_scheme_id: int, scheme_id: str = None,
                          scheme_name: str = None, description: str = None,
                          scheme_agency_id: str = None, scheme_version_id: str = None,
                          ctx_category_id: int = None) -> tuple[CtxScheme, dict]:
        """
        Update an existing context scheme.
        
        Args:
            ctx_scheme_id: ID of the context scheme to update
            scheme_id: New external identification of the scheme (optional)
            scheme_name: New pretty print name of the context scheme (optional)
            description: New description of the context scheme (optional)
            scheme_agency_id: New identification of the agency maintaining the scheme (optional)
            scheme_version_id: New version number of the context scheme (optional)
            ctx_category_id: New foreign key to the CTX_CATEGORY table (optional)
        
        Returns:
            tuple[CtxScheme, dict]: The updated context scheme and original values
        """
        # Validate that at least one field is provided
        if all(param is None for param in [scheme_id, scheme_name, description, scheme_agency_id, scheme_version_id, ctx_category_id]):
            raise HTTPException(
                status_code=400,
                detail="At least one field must be provided for update"
            )

        # Validate input parameters
        if scheme_id is not None and (not scheme_id.strip() or len(scheme_id.strip()) > 45):
            raise HTTPException(
                status_code=400,
                detail="Scheme ID cannot be empty or exceed 45 characters"
            )

        if scheme_name and len(scheme_name) > 255:
            raise HTTPException(
                status_code=400,
                detail="Scheme name cannot exceed 255 characters"
            )

        if description and len(description) > 1000:
            raise HTTPException(
                status_code=400,
                detail="Description cannot exceed 1000 characters"
            )

        if scheme_agency_id and len(scheme_agency_id) > 45:
            raise HTTPException(
                status_code=400,
                detail="Scheme agency ID cannot exceed 45 characters"
            )

        if scheme_version_id and len(scheme_version_id) > 45:
            raise HTTPException(
                status_code=400,
                detail="Scheme version ID cannot exceed 45 characters"
            )

        # Find the context scheme
        ctx_scheme = db_get(CtxScheme, ctx_scheme_id)
        if not ctx_scheme:
            raise HTTPException(
                status_code=404,
                detail=f"Context scheme with ID {ctx_scheme_id} not found"
            )

        # Capture original values
        original_values = {
            "scheme_id": ctx_scheme.scheme_id,
            "scheme_name": ctx_scheme.scheme_name,
            "description": ctx_scheme.description,
            "scheme_agency_id": ctx_scheme.scheme_agency_id,
            "scheme_version_id": ctx_scheme.scheme_version_id,
            "ctx_category_id": ctx_scheme.ctx_category_id,
        }

        # Update the fields if provided
        if scheme_id is not None:
            ctx_scheme.scheme_id = scheme_id.strip()
        if scheme_name is not None:
            ctx_scheme.scheme_name = scheme_name.strip() if scheme_name else None
        if description is not None:
            ctx_scheme.description = description.strip() if description else None
        if scheme_agency_id is not None:
            ctx_scheme.scheme_agency_id = scheme_agency_id.strip() if scheme_agency_id else ""
        if scheme_version_id is not None:
            ctx_scheme.scheme_version_id = scheme_version_id.strip() if scheme_version_id else ""
        if ctx_category_id is not None:
            ctx_scheme.ctx_category_id = ctx_category_id

        ctx_scheme.last_updated_by = self.requester.app_user_id
        ctx_scheme.last_update_timestamp = datetime.now(timezone.utc)

        db_flush()  # Flush changes to make sure they're visible in the query below

        # Evict cache entries for this scheme
        evict_cache("ctx_scheme.get_ctx_schemes")  # Evict all list queries
        evict_cache("ctx_scheme.get_ctx_scheme", ctx_scheme_id=ctx_scheme_id)  # Evict specific get query

        # Eager load the relationships
        ctx_scheme_with_relations = self._get_scheme_with_relations(ctx_scheme_id)

        return ctx_scheme_with_relations, original_values

    @transaction(read_only=False)
    def delete_ctx_scheme(self, ctx_scheme_id: int) -> bool:
        """
        Delete a context scheme and all associated context scheme values.
        
        Args:
            ctx_scheme_id: ID of the context scheme to delete
        
        Returns:
            bool: True if deleted successfully
        """
        # Validate input parameters
        if not ctx_scheme_id or ctx_scheme_id <= 0:
            raise HTTPException(
                status_code=400,
                detail="Context scheme ID must be a positive integer"
            )

        # Find the context scheme
        ctx_scheme = db_get(CtxScheme, ctx_scheme_id)
        if not ctx_scheme:
            raise HTTPException(
                status_code=404,
                detail=f"Context scheme with ID {ctx_scheme_id} not found"
            )

        # Delete all associated context scheme values first
        ctx_scheme_values = db_exec(
            select(CtxSchemeValue).where(CtxSchemeValue.owner_ctx_scheme_id == ctx_scheme_id)
        ).all()

        for ctx_scheme_value in ctx_scheme_values:
            db_delete(ctx_scheme_value)

        # Delete the context scheme
        db_delete(ctx_scheme)

        # Evict cache entries for this scheme
        evict_cache("ctx_scheme.get_ctx_schemes")  # Evict all list queries
        evict_cache("ctx_scheme.get_ctx_scheme", ctx_scheme_id=ctx_scheme_id)  # Evict specific get query

        return True

    @cache(key_prefix="ctx_scheme.get_ctx_scheme")
    @transaction(read_only=True)
    def get_ctx_scheme(self, ctx_scheme_id: int) -> CtxScheme:
        """
        Get a context scheme by ID.
        
        Args:
            ctx_scheme_id: ID of the context scheme to retrieve
        
        Returns:
            CtxScheme: The context scheme with relationships loaded
        """
        # Validate input parameters
        if not ctx_scheme_id or ctx_scheme_id <= 0:
            raise HTTPException(
                status_code=400,
                detail="Context scheme ID must be a positive integer"
            )

        # Find the context scheme with eager loading
        ctx_scheme = self._get_scheme_with_relations(ctx_scheme_id, include_values=True)

        if not ctx_scheme:
            raise HTTPException(
                status_code=404,
                detail=f"Context scheme with ID {ctx_scheme_id} not found"
            )

        return ctx_scheme

    @cache(key_prefix="ctx_scheme.get_ctx_schemes")
    @transaction(read_only=True)
    def get_ctx_schemes(self, scheme_id: str = None, scheme_name: str = None, description: str = None,
                        scheme_agency_id: str = None, scheme_version_id: str = None,
                        ctx_category_id: int = None,
                        created_on: DateRangeParams = None, last_updated_on: DateRangeParams = None,
                        pagination: PaginationParams = PaginationParams(offset=0, limit=10),
                        sort_list: list[Sort] = None, ctx_category_name: str = None) -> Page:
        """
        Get a paginated list of context schemes.
        
        Args:
            scheme_id: Filter by scheme ID (partial match, case-insensitive)
            scheme_name: Filter by scheme name (partial match, case-insensitive)
            description: Filter by description (partial match, case-insensitive)
            scheme_agency_id: Filter by scheme agency ID (partial match, case-insensitive)
            scheme_version_id: Filter by scheme version ID (partial match, case-insensitive)
            ctx_category_id: Filter by context category ID
            created_on: Date range parameters for filtering by creation date
            last_updated_on: Date range parameters for filtering by last update date
            pagination: Pagination parameters (offset and limit)
            sort_list: List of Sort objects for ordering
            ctx_category_name: Filter by context category name (partial match, case-insensitive)
            
        Returns:
            Page: Paginated result with total count and items
        """

        # Set default pagination if not provided
        if pagination is None:
            pagination = PaginationParams(offset=0, limit=10)

        # Build base query
        query = select(CtxScheme)

        # Apply filters
        query = self._apply_filters(query, scheme_id, scheme_name, description, scheme_agency_id,
                                   scheme_version_id, ctx_category_id, ctx_category_name,
                                   created_on, last_updated_on)

        # Apply sorting
        query = self._apply_sorting(query, sort_list)

        # Get total count with same filters
        count_query = select(func.count(CtxScheme.ctx_scheme_id))
        count_query = self._apply_filters(count_query, scheme_id, scheme_name, description,
                                         scheme_agency_id, scheme_version_id, ctx_category_id,
                                         ctx_category_name, created_on, last_updated_on)
        total = db_exec(count_query).one()

        # Apply pagination
        query = query.offset(pagination.offset).limit(pagination.limit)

        # Execute query with eager loading
        ctx_schemes = db_exec(
            query.options(
                selectinload(CtxScheme.creator),
                selectinload(CtxScheme.last_updater),
                selectinload(CtxScheme.ctx_category),
                selectinload(CtxScheme.ctx_scheme_values)
            )
        ).all()

        return Page(
            total=total,
            offset=pagination.offset,
            limit=pagination.limit,
            items=list(ctx_schemes)
        )

    def _apply_filters(self, query, scheme_id: str = None, scheme_name: str = None,
                      description: str = None, scheme_agency_id: str = None,
                      scheme_version_id: str = None, ctx_category_id: int = None,
                      ctx_category_name: str = None, created_on: DateRangeParams = None,
                      last_updated_on: DateRangeParams = None):
        """
        Apply filters to a query for context schemes.
        
        Args:
            query: The base query to apply filters to
            scheme_id: Filter by scheme ID (partial match, case-insensitive)
            scheme_name: Filter by scheme name (partial match, case-insensitive)
            description: Filter by description (partial match, case-insensitive)
            scheme_agency_id: Filter by scheme agency ID (partial match, case-insensitive)
            scheme_version_id: Filter by scheme version ID (partial match, case-insensitive)
            ctx_category_id: Filter by context category ID
            ctx_category_name: Filter by context category name (partial match, case-insensitive)
            created_on: Date range parameters for filtering by creation date
            last_updated_on: Date range parameters for filtering by last update date
            
        Returns:
            Query with filters applied
        """
        if scheme_id:
            query = query.where(CtxScheme.scheme_id.ilike(f"%{scheme_id}%"))

        if scheme_name:
            query = query.where(CtxScheme.scheme_name.ilike(f"%{scheme_name}%"))

        if description:
            query = query.where(CtxScheme.description.ilike(f"%{description}%"))

        if scheme_agency_id:
            query = query.where(CtxScheme.scheme_agency_id.ilike(f"%{scheme_agency_id}%"))

        if scheme_version_id:
            query = query.where(CtxScheme.scheme_version_id.ilike(f"%{scheme_version_id}%"))

        if ctx_category_id:
            query = query.where(CtxScheme.ctx_category_id == ctx_category_id)

        if ctx_category_name:
            # Join with CtxCategory to filter by category name
            query = query.join(CtxCategory, CtxScheme.ctx_category_id == CtxCategory.ctx_category_id)
            query = query.where(CtxCategory.name.ilike(f"%{ctx_category_name}%"))

        if created_on:
            if created_on.before:
                query = query.where(CtxScheme.creation_timestamp >= created_on.before)
            if created_on.after:
                query = query.where(CtxScheme.creation_timestamp < created_on.after)

        if last_updated_on:
            if last_updated_on.before:
                query = query.where(CtxScheme.last_update_timestamp >= last_updated_on.before)
            if last_updated_on.after:
                query = query.where(CtxScheme.last_update_timestamp < last_updated_on.after)

        return query

    def _apply_sorting(self, query, sort_list: list[Sort] = None):
        """
        Apply sorting to a query for context schemes.
        
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
                'scheme_id': CtxScheme.scheme_id,
                'scheme_name': CtxScheme.scheme_name,
                'description': CtxScheme.description,
                'scheme_agency_id': CtxScheme.scheme_agency_id,
                'scheme_version_id': CtxScheme.scheme_version_id,
                'creation_timestamp': CtxScheme.creation_timestamp,
                'last_update_timestamp': CtxScheme.last_update_timestamp
            }

            for sort_obj in sort_list:
                column = column_map[sort_obj.column]
                if sort_obj.direction == 'desc':
                    query = query.order_by(column.desc())
                else:
                    query = query.order_by(column)
        else:
            # Default ordering by scheme_id
            query = query.order_by(CtxScheme.scheme_id)

        return query

    @transaction(read_only=False)
    def create_ctx_scheme_value(self, ctx_scheme_id: int, value: str,
                                meaning: str = None) -> CtxSchemeValue:
        """
        Create a new context scheme value.
        
        Args:
            ctx_scheme_id: ID of the context scheme to which this value belongs
            value: The actual value string to be added to the context scheme
            meaning: Optional descriptive meaning or human-readable explanation of the value
        
        Returns:
            CtxSchemeValue: The created context scheme value
        """
        # Validate input parameters
        if not ctx_scheme_id or ctx_scheme_id <= 0:
            raise HTTPException(
                status_code=400,
                detail="Context scheme ID must be a positive integer"
            )

        if not value or not value.strip():
            raise HTTPException(
                status_code=400,
                detail="Value is required and cannot be empty"
            )

        if len(value.strip()) > 100:
            raise HTTPException(
                status_code=400,
                detail="Value cannot exceed 100 characters"
            )

        if meaning and len(meaning) > 1000:
            raise HTTPException(
                status_code=400,
                detail="Meaning cannot exceed 1000 characters"
            )

        # Verify that the context scheme exists
        ctx_scheme = db_get(CtxScheme, ctx_scheme_id)
        if not ctx_scheme:
            raise HTTPException(
                status_code=404,
                detail=f"Context scheme with ID {ctx_scheme_id} not found"
            )

        # Generate a GUID for the context scheme value
        guid = generate_guid()

        # Create the context scheme value
        ctx_scheme_value = CtxSchemeValue(
            guid=guid,
            value=value.strip(),
            meaning=meaning.strip() if meaning else None,
            owner_ctx_scheme_id=ctx_scheme_id
        )

        # Save to database
        db_add(ctx_scheme_value)
        db_flush()  # Flush to get the ID assigned

        # Evict cache entries for the parent scheme (value changes affect scheme queries)
        evict_cache("ctx_scheme.get_ctx_schemes")  # Evict all list queries
        evict_cache("ctx_scheme.get_ctx_scheme", ctx_scheme_id=ctx_scheme_id)  # Evict specific get query

        return ctx_scheme_value

    @transaction(read_only=False)
    def update_ctx_scheme_value(self, ctx_scheme_value_id: int, value: str = None,
                                meaning: str = None) -> tuple[CtxSchemeValue, dict]:
        """
        Update an existing context scheme value.
        
        Args:
            ctx_scheme_value_id: ID of the context scheme value to update
            value: New value string for the context scheme value (optional)
            meaning: New meaning or description for the context scheme value (optional)
        
        Returns:
            tuple[CtxSchemeValue, dict]: The updated context scheme value and original values
        """
        # Validate that at least one field is provided
        if value is None and meaning is None:
            raise HTTPException(
                status_code=400,
                detail="At least one of 'value' or 'meaning' must be provided"
            )

        # Validate input parameters
        if not ctx_scheme_value_id or ctx_scheme_value_id <= 0:
            raise HTTPException(
                status_code=400,
                detail="Context scheme value ID must be a positive integer"
            )

        if value is not None:
            if not value.strip():
                raise HTTPException(
                    status_code=400,
                    detail="Value cannot be empty"
                )
            if len(value.strip()) > 100:
                raise HTTPException(
                    status_code=400,
                    detail="Value cannot exceed 100 characters"
                )

        if meaning is not None and len(meaning) > 1000:
            raise HTTPException(
                status_code=400,
                detail="Meaning cannot exceed 1000 characters"
            )

        # Find the context scheme value
        ctx_scheme_value = db_get(CtxSchemeValue, ctx_scheme_value_id)
        if not ctx_scheme_value:
            raise HTTPException(
                status_code=404,
                detail=f"Context scheme value with ID {ctx_scheme_value_id} not found"
            )

        # Capture original values
        original_values = {
            "value": ctx_scheme_value.value,
            "meaning": ctx_scheme_value.meaning
        }

        # Update the fields if provided
        if value is not None:
            ctx_scheme_value.value = value.strip()
        if meaning is not None:
            ctx_scheme_value.meaning = meaning.strip() if meaning else None

        db_flush()  # Flush changes

        evict_cache("ctx_scheme.get_ctx_schemes")  # Evict all list queries
        # Evict cache entries for the parent scheme (value changes affect scheme queries)
        ctx_scheme_id = ctx_scheme_value.owner_ctx_scheme_id if hasattr(ctx_scheme_value, 'owner_ctx_scheme_id') else None
        if ctx_scheme_id:
            evict_cache("ctx_scheme.get_ctx_scheme", ctx_scheme_id=ctx_scheme_id)  # Evict specific get query

        return ctx_scheme_value, original_values

    @transaction(read_only=False)
    def delete_ctx_scheme_value(self, ctx_scheme_value_id: int) -> bool:
        """
        Delete a context scheme value.
        
        Args:
            ctx_scheme_value_id: ID of the context scheme value to delete
        
        Returns:
            bool: True if deleted successfully
        """
        # Validate input parameters
        if not ctx_scheme_value_id or ctx_scheme_value_id <= 0:
            raise HTTPException(
                status_code=400,
                detail="Context scheme value ID must be a positive integer"
            )

        # Find the context scheme value
        ctx_scheme_value = db_get(CtxSchemeValue, ctx_scheme_value_id)
        if not ctx_scheme_value:
            raise HTTPException(
                status_code=404,
                detail=f"Context scheme value with ID {ctx_scheme_value_id} not found"
            )

        # Check if there are any linked business context values
        linked_biz_ctx_values = db_exec(
            select(BizCtxValue).where(BizCtxValue.ctx_scheme_value_id == ctx_scheme_value_id)
        ).all()

        if linked_biz_ctx_values:
            # Get business context IDs for the error message
            biz_ctx_ids = [str(bcv.biz_ctx_id) for bcv in linked_biz_ctx_values]
            raise HTTPException(
                status_code=409,
                detail=f"Cannot delete context scheme value with ID {ctx_scheme_value_id} because it is linked to {len(linked_biz_ctx_values)} business context value(s). "
                       f"Linked business context IDs: {', '.join(biz_ctx_ids)}. "
                       f"To fix this error, first delete the business context values using the delete_business_context_value tool, "
                       f"or delete the entire business contexts using the delete_business_context tool."
            )

        # Get the scheme ID before deletion for cache eviction
        ctx_scheme_id = ctx_scheme_value.owner_ctx_scheme_id if hasattr(ctx_scheme_value, 'owner_ctx_scheme_id') else None

        # Delete the context scheme value
        db_delete(ctx_scheme_value)

        evict_cache("ctx_scheme.get_ctx_schemes")  # Evict all list queries        
        # Evict cache entries for the parent scheme (value changes affect scheme queries)
        if ctx_scheme_id:
            evict_cache("ctx_scheme.get_ctx_scheme", ctx_scheme_id=ctx_scheme_id)  # Evict specific get query

        return True
