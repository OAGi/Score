"""
Service class for managing Tag operations in connectCenter.

Tags are labels used to categorize and organize components and entities in the
Score platform. This service provides functionality for querying and retrieving
tag information with support for filtering, pagination, and sorting.
"""

import logging

from sqlmodel import select, func, and_
from sqlalchemy.orm import selectinload

from services.models import Tag
from services.models.common import Sort, PaginationParams, DateRangeParams, Page
from services.transaction import transaction, db_exec
from services.cache import cache

# Configure logging
logger = logging.getLogger(__name__)


class TagService:
    """
    Service class for managing Tag operations.
    
    This service provides a comprehensive interface for working with Tags, which
    are labels used to categorize and organize components and entities in the
    Score platform. Tags help users find and group related items across the system.
    
    Key Features:
    - Query tags with advanced filtering capabilities
    - Support for pagination and flexible sorting
    - Filter by tag name and description (case-insensitive partial matching)
    - Date range filtering for creation and last update timestamps
    - Automatic loading of related entities (creator, last_updater)
    
    Main Operations:
    - get_tags(): Retrieve paginated lists of tags with optional filters for
      name and description. Supports date range filtering for creation and last
      update timestamps, and custom sorting.
    
    Filtering Capabilities:
    - Text fields (name, description): Case-insensitive partial matching
    - Date range filtering for creation and last update timestamps
    - All filters can be combined for complex queries
    
    Sorting:
    - Supports flexible sorting by any tag attribute
    - Multi-column sorting with ascending/descending order
    - Supports both Sort objects and string-based sorting for backward compatibility
    
    Relationship Loading:
    All queries automatically load related entities using SQLAlchemy's selectinload:
    - Tag: creator, last_updater
    
    Transaction Management:
    All database operations are wrapped in read-only transactions to ensure
    data consistency and proper connection management.
    
    Example Usage:
        service = TagService()
        
        # Get paginated tags with filters
        page = service.get_tags(
            name="production",
            pagination=PaginationParams(offset=0, limit=10),
            sort_list=[Sort(column="name", direction="asc")]
        )
    """
    
    def __init__(self):
        """
        Initialize the service.
        """
        pass
    
    @cache(key_prefix="tag.get_tags")
    @transaction(read_only=True)
    def get_tags(
        self,
        name: str | None = None,
        description: str | None = None,
        created_on_params: DateRangeParams | None = None,
        last_updated_on_params: DateRangeParams | None = None,
        pagination: PaginationParams | None = None,
        sort_list: list[Sort] | None = None
    ) -> Page:
        """
        Get tags with optional filtering and pagination.
        
        Args:
            name: Filter by tag name (partial match)
            description: Filter by description (partial match)
            created_on_params: Filter by creation date range
            last_updated_on_params: Filter by last update date range
            pagination: Pagination parameters
            sort_list: List of sort specifications
            
        Returns:
            Page: Paginated list of tags
        """
        # Set default pagination if not provided
        if pagination is None:
            pagination = PaginationParams(offset=0, limit=10)

        # Build base query with creator and last_updater relationships loaded
        query = select(Tag).options(selectinload(Tag.creator), selectinload(Tag.last_updater))
        conditions = []
        
        # Apply filters
        if name:
            conditions.append(Tag.name.ilike(f"%{name}%"))
        
        if description:
            conditions.append(Tag.description.ilike(f"%{description}%"))
        
        if created_on_params:
            if created_on_params.before:
                conditions.append(Tag.creation_timestamp < created_on_params.before)
            if created_on_params.after:
                conditions.append(Tag.creation_timestamp > created_on_params.after)
        
        if last_updated_on_params:
            if last_updated_on_params.before:
                conditions.append(Tag.last_update_timestamp < last_updated_on_params.before)
            if last_updated_on_params.after:
                conditions.append(Tag.last_update_timestamp > last_updated_on_params.after)
        
        if conditions:
            query = query.where(and_(*conditions))
        
        # Get total count
        count_query = select(func.count(Tag.tag_id))
        if conditions:
            count_query = count_query.where(and_(*conditions))
        total_count = db_exec(count_query).one()
        
        # Apply ordering
        if sort_list:
            order_columns = []
            for sort_obj in sort_list:
                if hasattr(sort_obj, 'column') and hasattr(sort_obj, 'direction'):
                    # Handle Sort objects
                    if hasattr(Tag, sort_obj.column):
                        if sort_obj.direction == 'desc':
                            order_columns.append(getattr(Tag, sort_obj.column).desc())
                        else:
                            order_columns.append(getattr(Tag, sort_obj.column))
                elif isinstance(sort_obj, str):
                    # Handle string format for backward compatibility
                    if sort_obj.startswith('-'):
                        # Descending order
                        field_name = sort_obj[1:]
                        if hasattr(Tag, field_name):
                            order_columns.append(getattr(Tag, field_name).desc())
                    else:
                        # Ascending order
                        if hasattr(Tag, sort_obj):
                            order_columns.append(getattr(Tag, sort_obj))
            if order_columns:
                query = query.order_by(*order_columns)
        
        # Apply pagination
        query = query.offset(pagination.offset).limit(pagination.limit)
        
        # Execute query
        tags = db_exec(query).all()
        
        return Page(
            total=total_count,
            offset=pagination.offset,
            limit=pagination.limit,
            items=list(tags)
        )
