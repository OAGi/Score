"""
Service class for managing Agency ID List operations in connectCenter.

Agency ID Lists are standardized lists used to identify agencies or organizations
in business information exchanges. This service provides comprehensive functionality
for querying, filtering, and retrieving Agency ID List data with support for
pagination, sorting, and date range filtering.
"""

import logging

from fastapi import HTTPException

from sqlmodel import select, func
from sqlalchemy.orm import selectinload

from services.models import AgencyIdList, AgencyIdListManifest, AgencyIdListValueManifest, Release
from services.models.common import Sort, PaginationParams, DateRangeParams, Page
from services.transaction import transaction, db_exec
from services.cache import cache

# Configure logging
logger = logging.getLogger(__name__)


class AgencyIdListService:
    """
    Service class for managing Agency ID List operations.
    
    This service provides a comprehensive interface for working with Agency ID Lists,
    which are standardized identification lists used to identify agencies or organizations
    in business information exchanges. The service handles querying, filtering, pagination,
    and retrieval of Agency ID List data with full relationship loading.
    
    Key Features:
    - Query Agency ID Lists by release with advanced filtering
    - Retrieve individual Agency ID Lists by ID or manifest ID
    - Get associated value manifests for Agency ID Lists
    - Support for pagination, sorting, and date range filtering
    - Automatic loading of related entities (namespace, creator, owner, release, etc.)
    - SQL injection protection through column whitelisting
    
    Main Operations:
    - get_agency_id_lists_by_release(): Retrieve paginated lists of Agency ID Lists
      filtered by release with optional filters for name, list_id, version_id, and
      date ranges. Supports custom sorting and pagination.
    
    - get_agency_id_list_by_id(): Retrieve a single Agency ID List by its internal ID,
      including all related entities (namespace, creator, owner, values, release, log).
    
    - get_agency_id_list_by_manifest_id(): Retrieve a single Agency ID List by its
      manifest ID, which represents the list in a specific release context. Includes
      all relationships and value manifests.
    
    - get_agency_id_list_value_manifests_by_manifest_id(): Retrieve all value manifests
      associated with a specific Agency ID List manifest, representing the individual
      values/entries in the list.
    
    Filtering and Sorting:
    - Supports filtering by name, list_id, and version_id (case-insensitive partial matching)
    - Date range filtering for creation and last update timestamps
    - Multi-column sorting with ascending/descending order
    - Default sorting by creation timestamp (newest first)
    - Column whitelist prevents SQL injection attacks
    
    Relationship Loading:
    All queries automatically load related entities using SQLAlchemy's selectinload:
    - Agency ID List: namespace, creator, owner, last_updater, agency_id_list_values
    - Manifest: release (with library), log, agency_id_list_value_manifests
    - Value Manifests: associated agency_id_list_value entities
    
    Transaction Management:
    All database operations are wrapped in read-only transactions to ensure
    data consistency and proper connection management.
    
    Example Usage:
        service = AgencyIdListService()
        
        # Get paginated lists for a release with filters
        page = service.get_agency_id_lists_by_release(
            release_id=1,
            name="ISO",
            pagination=PaginationParams(offset=0, limit=10),
            sort_list=[Sort(column="name", direction="asc")]
        )
        
        # Get a specific list by manifest ID
        manifest = service.get_agency_id_list_by_manifest_id(123)
        
        # Get value manifests for a list
        values = service.get_agency_id_list_value_manifests_by_manifest_id(123)
    """

    # Allowed columns for ordering to prevent SQL injection
    allowed_columns_for_order_by = [
        'name',
        'list_id',
        'version_id',
        'definition',
        'creation_timestamp',
        'last_update_timestamp'
    ]

    def __init__(self):
        """
        Initialize the service.
        """
        pass

    @cache(key_prefix="agency_id_list.get_agency_id_lists_by_release")
    @transaction(read_only=True)
    def get_agency_id_lists_by_release(
        self,
        release_id: int,
        name: str = None,
        list_id: str = None,
        version_id: str = None,
        created_on_params: DateRangeParams = None,
        last_updated_on_params: DateRangeParams = None,
        pagination: PaginationParams = PaginationParams(offset=0, limit=10),
        sort_list: list[Sort] = None
    ) -> Page:
        """
        Get agency ID lists associated with a specific release.
        
        Args:
            release_id: ID of the release to filter by
            pagination: Pagination parameters
            sort_list: List of sort specifications
            name: Filter by agency ID list name (partial match)
            list_id: Filter by list ID (partial match)
            version_id: Filter by version ID (partial match)
            created_on_params: Date range filter for creation timestamp
            last_updated_on_params: Date range filter for last update timestamp
        
        Returns:
            Page: Paginated response containing agency ID list manifests with value manifests included
        """
        logger.info(
            f"Querying agency ID lists: release_id={release_id}, name={name}, "
            f"list_id={list_id}, version_id={version_id}, pagination={pagination}, sort_list={sort_list}"
        )
        
        # Set default pagination if not provided
        if pagination is None:
            pagination = PaginationParams(offset=0, limit=10)
            logger.debug("Using default pagination: offset=0, limit=10")

        # Build the base query to get manifests with loaded relationships
        query = select(AgencyIdListManifest).options(
            selectinload(AgencyIdListManifest.agency_id_list).selectinload(AgencyIdList.namespace),
            selectinload(AgencyIdListManifest.agency_id_list).selectinload(AgencyIdList.creator),
            selectinload(AgencyIdListManifest.agency_id_list).selectinload(AgencyIdList.owner),
            selectinload(AgencyIdListManifest.agency_id_list).selectinload(AgencyIdList.last_updater),
            selectinload(AgencyIdListManifest.release).selectinload(Release.library),
            selectinload(AgencyIdListManifest.log)
        ).join(AgencyIdList).where(AgencyIdListManifest.release_id == release_id)

        # Apply filters
        query = self._apply_filters(query, name, list_id, version_id, created_on_params, last_updated_on_params)

        # Get total count
        logger.debug("Counting total matching records")
        count_query = select(func.count()).select_from(query.subquery())
        total_count = db_exec(count_query).one()
        logger.debug(f"Total matching records: {total_count}")

        # Apply sorting
        query = self._apply_sorting(query, sort_list)
        if sort_list:
            logger.debug(f"Applied sorting: {[f'{s.column} {s.direction}' for s in sort_list]}")

        # Apply pagination
        query = query.offset(pagination.offset).limit(pagination.limit)
        logger.debug(f"Applied pagination: offset={pagination.offset}, limit={pagination.limit}")

        # Execute query
        logger.debug("Executing query to retrieve agency ID list manifests")
        manifests = db_exec(query).all()
        logger.info(f"Retrieved {len(manifests)} agency ID list manifests (total available: {total_count})")

        # Create Page object
        result = Page(
            total=total_count,
            offset=pagination.offset,
            limit=pagination.limit,
            items=list(manifests)
        )
        logger.debug(f"Prepared page with {len(result.items)} items")
        return result

    def _apply_filters(self, query, name: str = None, list_id: str = None,
                      version_id: str = None, created_on_params: DateRangeParams = None,
                      last_updated_on_params: DateRangeParams = None):
        """
        Apply filters to a query for agency ID lists.
        
        Args:
            query: The base query to apply filters to
            name: Filter by agency ID list name (partial match)
            list_id: Filter by list ID (partial match)
            version_id: Filter by version ID (partial match)
            created_on_params: Date range filter for creation timestamp
            last_updated_on_params: Date range filter for last update timestamp
            
        Returns:
            Query with filters applied
        """
        if name:
            query = query.where(AgencyIdList.name.ilike(f"%{name}%"))

        if list_id:
            query = query.where(AgencyIdList.list_id.ilike(f"%{list_id}%"))

        if version_id:
            query = query.where(AgencyIdList.version_id.ilike(f"%{version_id}%"))

        if created_on_params:
            if created_on_params.before:
                query = query.where(AgencyIdList.creation_timestamp >= created_on_params.before)
            if created_on_params.after:
                query = query.where(AgencyIdList.creation_timestamp <= created_on_params.after)

        if last_updated_on_params:
            if last_updated_on_params.before:
                query = query.where(AgencyIdList.last_update_timestamp >= last_updated_on_params.before)
            if last_updated_on_params.after:
                query = query.where(AgencyIdList.last_update_timestamp <= last_updated_on_params.after)

        return query

    def _apply_sorting(self, query, sort_list: list[Sort] = None):
        """
        Apply sorting to a query for agency ID lists.
        
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
                column = getattr(AgencyIdList, sort.column)
                if sort.direction == 'desc':
                    query = query.order_by(column.desc())
                else:
                    query = query.order_by(column.asc())
        else:
            query = query.order_by(AgencyIdList.creation_timestamp.desc())

        return query

    @cache(key_prefix="agency_id_list.get_agency_id_list_by_id")
    @transaction(read_only=True)
    def get_agency_id_list_by_id(self, agency_id_list_id: int) -> AgencyIdListManifest:
        """
        Get an agency ID list by its ID.
        
        Args:
            agency_id_list_id: ID of the agency ID list to retrieve
        
        Returns:
            AgencyIdListManifest: The agency ID list manifest if found
        
        Raises:
            HTTPException: If agency ID list not found
        """
        query = select(AgencyIdListManifest).options(
            selectinload(AgencyIdListManifest.agency_id_list).selectinload(AgencyIdList.namespace),
            selectinload(AgencyIdListManifest.agency_id_list).selectinload(AgencyIdList.creator),
            selectinload(AgencyIdListManifest.agency_id_list).selectinload(AgencyIdList.owner),
            selectinload(AgencyIdListManifest.agency_id_list).selectinload(AgencyIdList.last_updater),
            selectinload(AgencyIdListManifest.agency_id_list).selectinload(AgencyIdList.agency_id_list_values),
            selectinload(AgencyIdListManifest.release),
            selectinload(AgencyIdListManifest.log)
        ).where(AgencyIdListManifest.agency_id_list_id == agency_id_list_id)

        manifest = db_exec(query).first()
        if not manifest:
            raise HTTPException(
                status_code=404,
                detail=f"Agency ID list with ID {agency_id_list_id} not found"
            )

        return manifest

    @cache(key_prefix="agency_id_list.get_agency_id_list_by_manifest_id")
    @transaction(read_only=True)
    def get_agency_id_list_by_manifest_id(self, agency_id_list_manifest_id: int) -> AgencyIdListManifest:
        """
        Get an agency ID list by its manifest ID.
        
        Args:
            agency_id_list_manifest_id: ID of the agency ID list manifest to retrieve
        
        Returns:
            AgencyIdListManifest: The agency ID list manifest with loaded relationships if found
        
        Raises:
            HTTPException: If agency ID list manifest not found
        """
        logger.info(f"Retrieving agency ID list: manifest_id={agency_id_list_manifest_id}")
        
        # Get the manifest with all relationships loaded
        logger.debug(f"Building query with relationships for manifest {agency_id_list_manifest_id}")
        manifest_query = select(AgencyIdListManifest).options(
            selectinload(AgencyIdListManifest.agency_id_list).selectinload(AgencyIdList.namespace),
            selectinload(AgencyIdListManifest.agency_id_list).selectinload(AgencyIdList.creator),
            selectinload(AgencyIdListManifest.agency_id_list).selectinload(AgencyIdList.owner),
            selectinload(AgencyIdListManifest.agency_id_list).selectinload(AgencyIdList.last_updater),
            selectinload(AgencyIdListManifest.agency_id_list).selectinload(AgencyIdList.agency_id_list_values),
            selectinload(AgencyIdListManifest.release).selectinload(Release.library),
            selectinload(AgencyIdListManifest.log),
            selectinload(AgencyIdListManifest.agency_id_list_value_manifests).selectinload(AgencyIdListValueManifest.agency_id_list_value)
        ).where(
            AgencyIdListManifest.agency_id_list_manifest_id == agency_id_list_manifest_id
        )
        logger.debug(f"Executing query for manifest {agency_id_list_manifest_id}")
        manifest = db_exec(manifest_query).first()
        if not manifest:
            logger.warning(f"Agency ID list not found: manifest_id={agency_id_list_manifest_id}")
            raise HTTPException(
                status_code=404,
                detail=f"Agency ID list manifest with ID {agency_id_list_manifest_id} not found"
            )

        logger.info(f"Retrieved agency ID list: '{manifest.agency_id_list.name if manifest.agency_id_list else 'N/A'}' (manifest_id: {agency_id_list_manifest_id})")
        return manifest

    @cache(key_prefix="agency_id_list.get_agency_id_list_value_manifests_by_manifest_id")
    @transaction(read_only=True)
    def get_agency_id_list_value_manifests_by_manifest_id(self, agency_id_list_manifest_id: int) -> list["AgencyIdListValueManifest"]:
        """
        Get agency ID list value manifests for a specific agency ID list manifest.
        
        Args:
            agency_id_list_manifest_id: The agency ID list manifest ID
            
        Returns:
            list[AgencyIdListValueManifest]: List of value manifests for the agency ID list
        """
        logger.info(f"Retrieving value manifests: manifest_id={agency_id_list_manifest_id}")
        
        logger.debug(f"Building query for value manifests of manifest {agency_id_list_manifest_id}")
        query = select(AgencyIdListValueManifest).options(
            selectinload(AgencyIdListValueManifest.agency_id_list_value)
        ).where(AgencyIdListValueManifest.agency_id_list_manifest_id == agency_id_list_manifest_id)
        
        logger.debug("Executing query for value manifests")
        value_manifests = db_exec(query).all()
        logger.info(f"Found {len(value_manifests)} value manifests for manifest {agency_id_list_manifest_id}")
        return list(value_manifests)
