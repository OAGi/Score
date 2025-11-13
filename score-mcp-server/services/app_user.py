"""
Service class for managing Application User operations in connectCenter.

Application Users represent the individuals or systems that interact with the Score
platform. This service provides functionality for querying and retrieving user
information with support for filtering, pagination, and sorting.
"""

import logging

from fastapi import HTTPException
from sqlmodel import select, func

from services.models import AppUser, AppOAuth2User
from services.models.common import Sort, PaginationParams, Page
from services.transaction import transaction, db_exec
from services.cache import cache

# Configure logging
logger = logging.getLogger(__name__)


class AppUserService:
    """
    Service class for managing Application User operations.
    
    This service provides a comprehensive interface for working with Application Users,
    which represent the individuals, systems, or services that interact with the Score
    platform. The service handles querying, filtering, and retrieval of user data.
    
    Key Features:
    - Query users with advanced filtering capabilities
    - Support for pagination and multi-column sorting
    - Filter by user attributes (login ID, username, organization, email, roles)
    - Filter by user status (admin, developer, enabled/disabled)
    - Flexible column-based sorting with validation
    
    Main Operations:
    - get_users(): Retrieve paginated lists of users with optional filters for
      login_id, username, organization, email, and role flags (is_admin, is_developer,
      is_enabled). Supports custom sorting and pagination.
    
    Filtering Capabilities:
    - Text fields (login_id, username, organization, email): Case-insensitive partial matching
    - Boolean fields (is_admin, is_developer, is_enabled): Exact matching
    - All filters can be combined for complex queries
    
    Sorting:
    - Supports sorting by any user attribute (login_id, username, organization, email, etc.)
    - Multi-column sorting with ascending/descending order
    - Column validation ensures only valid attributes can be sorted
    
    Transaction Management:
    All database operations are wrapped in read-only transactions to ensure
    data consistency and proper connection management.
    
    Example Usage:
        service = AppUserService()
        
        # Get paginated users with filters
        page = service.get_users(
            login_id="admin",
            is_enabled=True,
            pagination=PaginationParams(offset=0, limit=10),
            sort_list=[Sort(column="login_id", direction="asc")]
        )
        
        # Get all enabled developers
        developers = service.get_users(
            is_developer=True,
            is_enabled=True
        )
    """
    
    def __init__(self):
        """
        Initialize the service.
        """
        self.allowed_columns_for_order_by = [
            'login_id', 'username', 'organization', 'email', 
            'is_admin', 'is_developer', 'is_enabled'
        ]

    @cache(key_prefix="app_user.get_users")
    @transaction(read_only=True)
    def get_users(
        self,
        login_id: str = None,
        username: str = None,
        organization: str = None,
        email: str = None,
        is_admin: bool = None,
        is_developer: bool = None,
        is_enabled: bool = None,
        pagination: PaginationParams = PaginationParams(offset=0, limit=10),
        sort_list: list[Sort] = None
    ) -> Page:
        """
        Get users with optional filtering and pagination.
        
        Args:
            login_id: Filter by login ID (partial match)
            username: Filter by username (partial match)
            organization: Filter by organization (partial match)
            email: Filter by email (partial match)
            is_admin: Filter by admin status
            is_developer: Filter by developer status
            is_enabled: Filter by enabled status
            pagination: Pagination parameters
            sort_list: List of sort specifications
        
        Returns:
            Page: Paginated response containing users
        """
        logger.info(
            f"Querying users: login_id={login_id}, username={username}, organization={organization}, "
            f"email={email}, is_admin={is_admin}, is_developer={is_developer}, is_enabled={is_enabled}, "
            f"pagination={pagination}, sort_list={sort_list}"
        )
        
        # Set default pagination if not provided
        if pagination is None:
            pagination = PaginationParams(offset=0, limit=10)
            logger.debug("Using default pagination: offset=0, limit=10")

        # Build the base query
        query = select(AppUser)
        
        # Apply filters
        query = self._apply_filters(query, login_id, username, organization, email,
                                   is_admin, is_developer, is_enabled)
        
        # Get total count
        logger.debug("Counting total matching users")
        count_query = select(func.count()).select_from(query.subquery())
        total_count = db_exec(count_query).one()
        logger.debug(f"Total matching users: {total_count}")
        
        # Apply sorting
        query = self._apply_sorting(query, sort_list)
        if sort_list:
            logger.debug(f"Applied sorting: {[f'{s.column} {s.direction}' for s in sort_list]}")
        
        # Apply pagination
        query = query.offset(pagination.offset).limit(pagination.limit)
        logger.debug(f"Applied pagination: offset={pagination.offset}, limit={pagination.limit}")
        
        # Execute query
        logger.debug("Executing query to retrieve users")
        users = db_exec(query).all()
        logger.info(f"Retrieved {len(users)} users (total available: {total_count})")
        
        # Create Page object
        result = Page(
            total=total_count,
            offset=pagination.offset,
            limit=pagination.limit,
            items=list(users)
        )
        logger.debug(f"Prepared page with {len(result.items)} users")
        return result

    def _apply_filters(self, query, login_id: str = None, username: str = None,
                      organization: str = None, email: str = None,
                      is_admin: bool = None, is_developer: bool = None,
                      is_enabled: bool = None):
        """
        Apply filters to a query for app users.
        
        Args:
            query: The base query to apply filters to
            login_id: Filter by login ID (partial match)
            username: Filter by username (partial match)
            organization: Filter by organization (partial match)
            email: Filter by email (partial match)
            is_admin: Filter by admin status
            is_developer: Filter by developer status
            is_enabled: Filter by enabled status
            
        Returns:
            Query with filters applied
        """
        if login_id:
            query = query.where(AppUser.login_id.ilike(f"%{login_id}%"))
        if username:
            query = query.where(AppUser.name.ilike(f"%{username}%"))
        if organization:
            query = query.where(AppUser.organization.ilike(f"%{organization}%"))
        if email:
            query = query.where(AppUser.email.ilike(f"%{email}%"))
        if is_admin is not None:
            query = query.where(AppUser.is_admin == is_admin)
        if is_developer is not None:
            query = query.where(AppUser.is_developer == is_developer)
        if is_enabled is not None:
            query = query.where(AppUser.is_enabled == is_enabled)
        
        return query

    @transaction(read_only=True)
    def get_user_by_oauth2_sub(self, sub: str) -> AppUser | None:
        """
        Get an AppUser by OAuth2 sub claim.
        
        This method looks up an AppOAuth2User record by the 'sub' claim and returns
        the associated AppUser if found.
        
        Args:
            sub: The OAuth2 sub claim value
            
        Returns:
            AppUser if found, None otherwise
        """
        logger.debug(f"Looking up AppUser by OAuth2 sub claim: {sub}")
        
        # Find the OAuth2 user record by sub claim
        oauth2_user_stmt = select(AppOAuth2User).where(AppOAuth2User.sub == sub)
        oauth2_user = db_exec(oauth2_user_stmt).first()
        
        if oauth2_user and oauth2_user.app_user_id:
            # Get the corresponding app_user
            user_stmt = select(AppUser).where(AppUser.app_user_id == oauth2_user.app_user_id)
            app_user = db_exec(user_stmt).first()
            
            if app_user:
                logger.debug(f"Found AppUser: {app_user.login_id} (ID: {app_user.app_user_id})")
            else:
                logger.warning(f"AppUser not found for app_user_id: {oauth2_user.app_user_id}")
            return app_user
        else:
            logger.debug(f"OAuth2User not found for sub: {sub}")
            return None

    def _apply_sorting(self, query, sort_list: list[Sort] = None):
        """
        Apply sorting to a query for app users.
        
        Args:
            query: The query to apply sorting to
            sort_list: List of Sort objects for ordering
            
        Returns:
            Query with sorting applied
        """
        if sort_list:
            for sort in sort_list:
                if hasattr(AppUser, sort.column):
                    column = getattr(AppUser, sort.column)
                    if sort.direction == 'desc':
                        query = query.order_by(column.desc())
                    else:
                        query = query.order_by(column.asc())
        
        return query
