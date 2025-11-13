"""
Service class for managing Business Context operations in connectCenter.

Business Contexts represent specific contextual information that can be associated
with Business Information Entities. They combine Context Scheme Values to create
meaningful business context definitions. This service provides comprehensive
functionality for creating, updating, deleting, and querying Business Context
data, including management of Business Context Values.
"""

import logging
from datetime import datetime, timezone

from fastapi import HTTPException

from sqlmodel import select, func
from sqlalchemy.orm import selectinload

from services.models import AppUser, BizCtx, BizCtxValue, CtxSchemeValue, BizCtxAssignment
from services.models.common import Sort, PaginationParams, DateRangeParams, Page
from services.transaction import transaction, db_add, db_get, db_delete, db_refresh, db_flush, db_exec
from services.utils import generate_guid
from services.cache import cache, evict_cache

# Configure logging
logger = logging.getLogger(__name__)


class BizCtxService:
    """
    Service class for managing Business Context operations.
    
    This service provides a comprehensive interface for working with Business Contexts,
    which represent specific contextual information that can be associated with
    Business Information Entities. Business Contexts combine multiple Context Scheme
    Values to create meaningful business context definitions (e.g., "Production
    Environment, United States, Manufacturing Industry").
    
    Key Features:
    - Create, update, and delete Business Contexts
    - Manage Business Context Values (create, update, delete)
    - Query Business Contexts with advanced filtering capabilities
    - Support for pagination, sorting, and date range filtering
    - Automatic loading of related entities (creator, last_updater, values with scheme values)
    - Validation and constraint checking (prevents deletion if linked to assignments)
    - SQL injection protection through column whitelisting
    - Automatic GUID generation for new contexts
    
    Main Operations:
    Business Context Management:
    - create_biz_ctx(): Create a new Business Context with optional name.
      Automatically generates a GUID and sets creation/update timestamps.
    
    - update_biz_ctx(): Update an existing Business Context's name.
      Returns both the updated context and the original values for audit purposes.
    
    - delete_biz_ctx(): Delete a Business Context and all associated Business Context
      Values. Prevents deletion if the context has linked BizCtxAssignment records,
      providing detailed error messages with assignment IDs.
    
    - get_biz_ctx(): Retrieve a single Business Context by ID with all relationships
      and values loaded, including nested Context Scheme Value information.
    
    - get_biz_ctxs(): Retrieve paginated lists of Business Contexts with optional
      filters for name and date ranges. Supports custom sorting. All results include
      full relationship loading.
    
    Business Context Value Management:
    - create_biz_ctx_value(): Create a new Business Context Value linking a Business
      Context to a Context Scheme Value. Validates that both the business context
      and context scheme value exist. Prevents duplicate records with the same
      biz_ctx_id and ctx_scheme_value_id combination.
    
    - update_biz_ctx_value(): Update an existing Business Context Value's associated
      Context Scheme Value. Validates that the new context scheme value exists.
      Prevents duplicate records with the same biz_ctx_id and ctx_scheme_value_id combination.
      Returns both updated value and original values.
    
    - delete_biz_ctx_value(): Delete a Business Context Value. No additional
      validation required as values can be freely removed.
    
    Filtering and Sorting:
    - Supports filtering by name (case-insensitive partial matching)
    - Date range filtering for creation and last update timestamps
    - Multi-column sorting with ascending/descending order
    - Column whitelist prevents SQL injection attacks
    
    Validation and Constraints:
    - name: Optional, max 100 characters
    - biz_ctx_id: Required for values, must be positive integer
    - ctx_scheme_value_id: Required for values, must be positive integer
    - Duplicate prevention: Cannot create a business context value if a record with the same
      biz_ctx_id and ctx_scheme_value_id combination already exists
    - Deletion protection: Cannot delete Business Context if linked to BizCtxAssignment records
    
    Relationship Loading:
    All queries automatically load related entities using SQLAlchemy's selectinload:
    - Business Context: creator, last_updater, biz_ctx_values (with ctx_scheme_value)
    
    Transaction Management:
    All database operations are wrapped in transactions (read-only for queries,
    read-write for mutations) to ensure data consistency and proper connection management.
    
    Example Usage:
        service = BizCtxService(requester=current_user)
        
        # Create a new business context
        biz_ctx = service.create_biz_ctx(name="Production Environment")
        
        # Add values to the context
        value1 = service.create_biz_ctx_value(
            biz_ctx_id=biz_ctx.biz_ctx_id,
            ctx_scheme_value_id=1  # e.g., "Production" from Environment scheme
        )
        value2 = service.create_biz_ctx_value(
            biz_ctx_id=biz_ctx.biz_ctx_id,
            ctx_scheme_value_id=2  # e.g., "US" from Country scheme
        )
        
        # Get paginated contexts with filters
        page = service.get_biz_ctxs(
            name="Production",
            pagination=PaginationParams(offset=0, limit=10)
        )
        
        # Delete a context (will fail if linked to assignments)
        service.delete_biz_ctx(biz_ctx_id=1)
    """

    # Allowed columns for ordering to prevent SQL injection
    allowed_columns_for_order_by = [
        'name',
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
    def create_biz_ctx(self, name: str = None) -> BizCtx:
        """
        Create a new business context.
        
        Args:
            name: Short, descriptive name of the business context (optional)
        
        Returns:
            BizCtx: The created business context
        """
        logger.info(f"Creating business context: name='{name}', requester={self.requester.login_id} (ID: {self.requester.app_user_id})")
        
        # Validate input parameters
        if name is not None and len(name.strip()) > 100:
            logger.warning(f"Validation failed: name length {len(name.strip())} exceeds maximum of 100 characters")
            raise HTTPException(
                status_code=400,
                detail="Business context name cannot exceed 100 characters"
            )

        # Generate GUID
        logger.debug("Generating unique identifier")
        guid = generate_guid()
        logger.debug(f"Generated identifier: {guid}")

        # Create business context
        logger.debug(f"Creating business context object: name='{name}'")
        biz_ctx = BizCtx(
            guid=guid,
            name=name.strip() if name else None,
            created_by=self.requester.app_user_id,
            last_updated_by=self.requester.app_user_id,
            creation_timestamp=datetime.now(timezone.utc),
            last_update_timestamp=datetime.now(timezone.utc)
        )

        db_add(biz_ctx)
        db_flush()  # Flush to get the ID assigned
        logger.debug(f"Business context saved: ID={biz_ctx.biz_ctx_id}")

        # Evict cache entries (new entity affects list queries)
        logger.debug("Clearing cache for business context lists")
        evict_cache("biz_ctx.get_biz_ctxs")

        logger.info(f"Business context created successfully: ID={biz_ctx.biz_ctx_id}, name='{name}'")
        return biz_ctx

    @transaction(read_only=False)
    def create_biz_ctx_value(self, biz_ctx_id: int, ctx_scheme_value_id: int) -> BizCtxValue:
        """
        Create a new business context value.
        
        Args:
            biz_ctx_id: Foreign key to the biz_ctx table (required)
            ctx_scheme_value_id: Foreign key to the CTX_SCHEME_VALUE table (required)
        
        Returns:
            BizCtxValue: The created business context value
        
        Raises:
            HTTPException: If validation fails, duplicate record exists, or resources are not found.
                - 400: Invalid input parameters or duplicate record exists
                - 404: Business context or context scheme value not found
        """
        logger.info(f"Creating business context value: biz_ctx_id={biz_ctx_id}, ctx_scheme_value_id={ctx_scheme_value_id}, requester={self.requester.login_id} (ID: {self.requester.app_user_id})")
        
        # Validate input parameters
        if not biz_ctx_id or biz_ctx_id <= 0:
            logger.warning(f"Invalid business context ID: {biz_ctx_id}")
            raise HTTPException(
                status_code=400,
                detail="Business context ID must be a positive integer"
            )

        if not ctx_scheme_value_id or ctx_scheme_value_id <= 0:
            logger.warning(f"Invalid context scheme value ID: {ctx_scheme_value_id}")
            raise HTTPException(
                status_code=400,
                detail="Context scheme value ID must be a positive integer"
            )

        # Verify business context exists
        logger.debug(f"Verifying business context exists: ID={biz_ctx_id}")
        biz_ctx = db_get(BizCtx, biz_ctx_id)
        if not biz_ctx:
            logger.warning(f"Business context not found: ID={biz_ctx_id}")
            raise HTTPException(
                status_code=404,
                detail=f"Business context with ID {biz_ctx_id} not found"
            )
        logger.debug(f"Business context verified: ID={biz_ctx_id}, name='{biz_ctx.name}'")

        # Verify context scheme value exists
        logger.debug(f"Verifying context scheme value exists: ID={ctx_scheme_value_id}")
        ctx_scheme_value = db_get(CtxSchemeValue, ctx_scheme_value_id)
        if not ctx_scheme_value:
            logger.warning(f"Context scheme value not found: ID={ctx_scheme_value_id}")
            raise HTTPException(
                status_code=404,
                detail=f"Context scheme value with ID {ctx_scheme_value_id} not found"
            )
        logger.debug(f"Context scheme value verified: ID={ctx_scheme_value_id}, value='{ctx_scheme_value.value}'")

        # Check for duplicate: a business context value with the same biz_ctx_id and ctx_scheme_value_id already exists
        logger.debug(f"Checking for duplicate value: biz_ctx_id={biz_ctx_id}, ctx_scheme_value_id={ctx_scheme_value_id}")
        existing_value = db_exec(
            select(BizCtxValue).where(
                BizCtxValue.biz_ctx_id == biz_ctx_id,
                BizCtxValue.ctx_scheme_value_id == ctx_scheme_value_id
            )
        ).first()
        if existing_value:
            logger.warning(f"Duplicate value found: biz_ctx_id={biz_ctx_id}, ctx_scheme_value_id={ctx_scheme_value_id}, existing_id={existing_value.biz_ctx_value_id}")
            raise HTTPException(
                status_code=400,
                detail=f"A business context value with biz_ctx_id={biz_ctx_id} and ctx_scheme_value_id={ctx_scheme_value_id} already exists"
            )

        # Create business context value
        logger.debug(f"Creating business context value: biz_ctx_id={biz_ctx_id}, ctx_scheme_value_id={ctx_scheme_value_id}")
        biz_ctx_value = BizCtxValue(
            biz_ctx_id=biz_ctx_id,
            ctx_scheme_value_id=ctx_scheme_value_id
        )

        db_add(biz_ctx_value)
        db_flush()  # Flush to get the ID assigned
        logger.debug(f"Business context value saved: ID={biz_ctx_value.biz_ctx_value_id}")

        # Evict cache entries for the parent business context (value changes affect context queries)
        logger.debug("Clearing cache for business context queries")
        evict_cache("biz_ctx.get_biz_ctxs")  # Evict all list queries
        evict_cache("biz_ctx.get_biz_ctx", biz_ctx_id=biz_ctx_id)  # Evict specific get query

        logger.info(f"Business context value created successfully: ID={biz_ctx_value.biz_ctx_value_id}, biz_ctx_id={biz_ctx_id}, ctx_scheme_value_id={ctx_scheme_value_id}")
        return biz_ctx_value

    @transaction(read_only=False)
    def update_biz_ctx(self, biz_ctx_id: int, name: str = None) -> tuple[BizCtx, dict]:
        """
        Update a business context.
        
        Args:
            biz_ctx_id: ID of the business context to update
            name: Updated name of the business context (optional)
        
        Returns:
            tuple: (updated_biz_ctx, original_values)
        """
        # Validate input parameters
        if not biz_ctx_id or biz_ctx_id <= 0:
            raise HTTPException(
                status_code=400,
                detail="Business context ID must be a positive integer"
            )

        if name is not None and len(name.strip()) > 100:
            raise HTTPException(
                status_code=400,
                detail="Business context name cannot exceed 100 characters"
            )

        # Find the business context
        biz_ctx = db_get(BizCtx, biz_ctx_id)
        if not biz_ctx:
            raise HTTPException(
                status_code=404,
                detail=f"Business context with ID {biz_ctx_id} not found"
            )

        # Capture original values
        original_values = {
            "name": biz_ctx.name
        }

        # Update the fields if provided
        if name is not None:
            biz_ctx.name = name.strip() if name else None

        # Update last_updated_by and last_update_timestamp
        biz_ctx.last_updated_by = self.requester.app_user_id
        biz_ctx.last_update_timestamp = datetime.now(timezone.utc)

        db_flush()  # Flush changes

        # Evict cache entries for this business context
        evict_cache("biz_ctx.get_biz_ctxs")  # Evict all list queries
        evict_cache("biz_ctx.get_biz_ctx", biz_ctx_id=biz_ctx_id)  # Evict specific get query

        return biz_ctx, original_values

    @transaction(read_only=False)
    def update_biz_ctx_value(self, biz_ctx_value_id: int,
                             ctx_scheme_value_id: int = None) -> tuple[BizCtxValue, dict]:
        """
        Update a business context value.
        
        Args:
            biz_ctx_value_id: ID of the business context value to update
            ctx_scheme_value_id: Updated context scheme value ID (optional)
        
        Returns:
            tuple: (updated_biz_ctx_value, original_values)
        
        Raises:
            HTTPException: If validation fails, duplicate record exists, or resources are not found.
                - 400: Invalid input parameters or duplicate record exists
                - 404: Business context value or context scheme value not found
        """
        # Validate input parameters
        if not biz_ctx_value_id or biz_ctx_value_id <= 0:
            raise HTTPException(
                status_code=400,
                detail="Business context value ID must be a positive integer"
            )

        if ctx_scheme_value_id is not None and (not ctx_scheme_value_id or ctx_scheme_value_id <= 0):
            raise HTTPException(
                status_code=400,
                detail="Context scheme value ID must be a positive integer"
            )

        # Find the business context value
        biz_ctx_value = db_get(BizCtxValue, biz_ctx_value_id)
        if not biz_ctx_value:
            raise HTTPException(
                status_code=404,
                detail=f"Business context value with ID {biz_ctx_value_id} not found"
            )

        # Capture original values
        original_values = {
            "ctx_scheme_value_id": biz_ctx_value.ctx_scheme_value_id
        }

        # Update the fields if provided
        if ctx_scheme_value_id is not None:
            # Verify context scheme value exists
            ctx_scheme_value = db_get(CtxSchemeValue, ctx_scheme_value_id)
            if not ctx_scheme_value:
                raise HTTPException(
                    status_code=404,
                    detail=f"Context scheme value with ID {ctx_scheme_value_id} not found"
                )
            
            # Check for duplicate: another business context value with the same biz_ctx_id and ctx_scheme_value_id already exists
            # (excluding the current record being updated)
            existing_value = db_exec(
                select(BizCtxValue).where(
                    BizCtxValue.biz_ctx_id == biz_ctx_value.biz_ctx_id,
                    BizCtxValue.ctx_scheme_value_id == ctx_scheme_value_id,
                    BizCtxValue.biz_ctx_value_id != biz_ctx_value_id
                )
            ).first()
            if existing_value:
                raise HTTPException(
                    status_code=400,
                    detail=f"A business context value with biz_ctx_id={biz_ctx_value.biz_ctx_id} and ctx_scheme_value_id={ctx_scheme_value_id} already exists"
                )
            
            biz_ctx_value.ctx_scheme_value_id = ctx_scheme_value_id

        db_flush()  # Flush changes

        # Evict cache entries for the parent business context (value changes affect context queries)
        ctx_id = biz_ctx_value.biz_ctx_id if hasattr(biz_ctx_value, 'biz_ctx_id') else None
        if ctx_id:
            evict_cache("biz_ctx.get_biz_ctxs")  # Evict all list queries
            evict_cache("biz_ctx.get_biz_ctx", biz_ctx_id=ctx_id)  # Evict specific get query

        return biz_ctx_value, original_values

    @transaction(read_only=False)
    def delete_biz_ctx(self, biz_ctx_id: int) -> bool:
        """
        Delete a business context and all associated business context values.
        
        Args:
            biz_ctx_id: ID of the business context to delete
        
        Returns:
            bool: True if deleted successfully
        
        Raises:
            HTTPException: If the business context has linked biz_ctx_assignment records
        """
        # Validate input parameters
        if not biz_ctx_id or biz_ctx_id <= 0:
            raise HTTPException(
                status_code=400,
                detail="Business context ID must be a positive integer"
            )

        # Find the business context
        biz_ctx = db_get(BizCtx, biz_ctx_id)
        if not biz_ctx:
            raise HTTPException(
                status_code=404,
                detail=f"Business context with ID {biz_ctx_id} not found"
            )

        # Check for linked biz_ctx_assignment records
        linked_assignments = db_exec(
            select(BizCtxAssignment).where(BizCtxAssignment.biz_ctx_id == biz_ctx_id)
        ).all()

        if linked_assignments:
            assignment_ids = [assignment.biz_ctx_assignment_id for assignment in linked_assignments]
            raise HTTPException(
                status_code=409,
                detail=f"Cannot delete business context '{biz_ctx.name}' (ID: {biz_ctx_id}) because it has {len(linked_assignments)} linked biz_ctx_assignment record(s) with IDs: {assignment_ids}. Please delete the linked biz_ctx_assignment records first, or update them to use a different business context."
            )

        # Delete all associated business context values first
        biz_ctx_values = db_exec(
            select(BizCtxValue).where(BizCtxValue.biz_ctx_id == biz_ctx_id)
        ).all()
        for biz_ctx_value in biz_ctx_values:
            db_delete(biz_ctx_value)

        # Delete the business context
        db_delete(biz_ctx)

        # Evict cache entries for this business context
        evict_cache("biz_ctx.get_biz_ctxs")  # Evict all list queries
        evict_cache("biz_ctx.get_biz_ctx", biz_ctx_id=biz_ctx_id)  # Evict specific get query

        return True

    @transaction(read_only=False)
    def delete_biz_ctx_value(self, biz_ctx_value_id: int) -> bool:
        """
        Delete a business context value.
        
        Args:
            biz_ctx_value_id: ID of the business context value to delete
        
        Returns:
            bool: True if deleted successfully
        """
        # Validate input parameters
        if not biz_ctx_value_id or biz_ctx_value_id <= 0:
            raise HTTPException(
                status_code=400,
                detail="Business context value ID must be a positive integer"
            )

        # Find the business context value
        biz_ctx_value = db_get(BizCtxValue, biz_ctx_value_id)
        if not biz_ctx_value:
            raise HTTPException(
                status_code=404,
                detail=f"Business context value with ID {biz_ctx_value_id} not found"
            )

        # Get the business context ID before deletion for cache eviction
        ctx_id = biz_ctx_value.biz_ctx_id if hasattr(biz_ctx_value, 'biz_ctx_id') else None

        # Delete the business context value
        db_delete(biz_ctx_value)

        # Evict cache entries for the parent business context (value changes affect context queries)
        if ctx_id:
            evict_cache("biz_ctx.get_biz_ctxs")  # Evict all list queries
            evict_cache("biz_ctx.get_biz_ctx", biz_ctx_id=ctx_id)  # Evict specific get query

        return True

    @cache(key_prefix="biz_ctx.get_biz_ctx")
    @transaction(read_only=True)
    def get_biz_ctx(self, biz_ctx_id: int) -> BizCtx:
        """
        Get a business context by ID.
        
        Args:
            biz_ctx_id: ID of the business context to retrieve
        
        Returns:
            BizCtx: The business context
        """
        # Validate input parameters
        if not biz_ctx_id or biz_ctx_id <= 0:
            raise HTTPException(
                status_code=400,
                detail="Business context ID must be a positive integer"
            )

        # Find the business context with eager loading of all relationships
        biz_ctx = db_exec(
            select(BizCtx)
            .where(BizCtx.biz_ctx_id == biz_ctx_id)
            .options(
                selectinload(BizCtx.biz_ctx_values).selectinload(BizCtxValue.ctx_scheme_value),
                selectinload(BizCtx.creator),
                selectinload(BizCtx.last_updater)
            )
        ).first()

        if not biz_ctx:
            raise HTTPException(
                status_code=404,
                detail=f"Business context with ID {biz_ctx_id} not found"
            )

        return biz_ctx

    @cache(key_prefix="biz_ctx.get_biz_ctxs")
    @transaction(read_only=True)
    def get_biz_ctxs(self, name: str = None, created_on_params: DateRangeParams = None,
                     last_updated_on_params: DateRangeParams = None,
                     pagination: PaginationParams = PaginationParams(offset=0, limit=10),
                     sort_list: list[Sort] = None) -> Page:
        """
        Get a paginated list of business contexts.
        
        Args:
            name: Filter by business context name
            created_on_params: Filter by creation date range
            last_updated_on_params: Filter by last update date range
            pagination: Pagination parameters
            sort_list: List of sort specifications
        
        Returns:
            Page: Paginated list of business contexts
        """
        # Set default pagination if not provided
        if pagination is None:
            pagination = PaginationParams(offset=0, limit=10)

        # Build the base query
        query = select(BizCtx)

        # Apply filters
        query = self._apply_filters(query, name, created_on_params, last_updated_on_params)

        # Apply sorting
        query = self._apply_sorting(query, sort_list)

        # Get total count with same filters
        count_query = select(func.count(BizCtx.biz_ctx_id))
        count_query = self._apply_filters(count_query, name, created_on_params, last_updated_on_params)
        total = db_exec(count_query).one()

        # Apply pagination
        query = query.offset(pagination.offset).limit(pagination.limit)

        # Execute query with eager loading of all relationships
        biz_ctxs = db_exec(
            query.options(
                selectinload(BizCtx.biz_ctx_values).selectinload(BizCtxValue.ctx_scheme_value),
                selectinload(BizCtx.creator),
                selectinload(BizCtx.last_updater)
            )
        ).all()

        return Page(
            total=total,
            offset=pagination.offset,
            limit=pagination.limit,
            items=list(biz_ctxs)
        )

    def _apply_filters(self, query, name: str = None, created_on_params: DateRangeParams = None,
                      last_updated_on_params: DateRangeParams = None):
        """
        Apply filters to a query for business contexts.
        
        Args:
            query: The base query to apply filters to
            name: Filter by business context name (partial match, case-insensitive)
            created_on_params: Date range parameters for filtering by creation date
            last_updated_on_params: Date range parameters for filtering by last update date
            
        Returns:
            Query with filters applied
        """
        if name:
            query = query.where(BizCtx.name.ilike(f"%{name}%"))

        if created_on_params:
            if created_on_params.after:
                query = query.where(BizCtx.creation_timestamp >= created_on_params.after)
            if created_on_params.before:
                query = query.where(BizCtx.creation_timestamp <= created_on_params.before)

        if last_updated_on_params:
            if last_updated_on_params.after:
                query = query.where(BizCtx.last_update_timestamp >= last_updated_on_params.after)
            if last_updated_on_params.before:
                query = query.where(BizCtx.last_update_timestamp <= last_updated_on_params.before)

        return query

    def _apply_sorting(self, query, sort_list: list[Sort] = None):
        """
        Apply sorting to a query for business contexts.
        
        Args:
            query: The query to apply sorting to
            sort_list: List of Sort objects for ordering
            
        Returns:
            Query with sorting applied
        """
        if sort_list:
            for sort in sort_list:
                if sort.column in self.allowed_columns_for_order_by:
                    column = getattr(BizCtx, sort.column)
                    if sort.direction == 'desc':
                        query = query.order_by(column.desc())
                    else:
                        query = query.order_by(column.asc())

        return query
