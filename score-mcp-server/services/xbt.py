"""
Service for managing XBT (XML Built-in Type) operations.

This service provides methods for querying and retrieving XBTs (XML Built-in Types),
which are fundamental XML Schema Definition (XSD) data types used in business information
exchanges. XBTs represent primitive data types such as string, integer, decimal, date,
boolean, and their specialized subtypes.

XBTs form a type hierarchy through subtype relationships (e.g., string → normalizedString → token),
and include mappings to other data representation formats (JSON Schema Draft 05, OpenAPI 3.0, Avro)
to support interoperability across different systems and standards.
"""

import logging
from sqlmodel import select
from sqlalchemy.orm import selectinload
from fastapi import HTTPException

from services.transaction import db_exec, transaction
from services.cache import cache
from services.models.data_type import XbtManifest, Xbt
from services.models.release import Release

# Configure logging
logger = logging.getLogger(__name__)


class XbtService:
    """
    Service class for managing XBT (XML Built-in Type) operations.
    
    XBTs are fundamental XML Schema Definition (XSD) data types that serve as the foundation
    for data type definitions in Core Components. They form a type hierarchy through subtype
    relationships and include mappings to JSON Schema, OpenAPI, and Avro formats.
    
    This service provides methods for querying and retrieving XBTs with full relationship
    loading, including:
    - Release and library information
    - Log information (revision history)
    - Subtype relationships (parent XBT in type hierarchy)
    - User information (owner, creator, last_updater)
    """
    
    @cache(key_prefix="xbt.get_xbt_by_manifest_id")
    @transaction(read_only=True)
    def get_xbt_by_manifest_id(self, xbt_manifest_id: int) -> XbtManifest:
        """
        Get an XBT manifest by its manifest ID with all relationships loaded.
        
        The XBT manifest is release-specific, while the underlying XBT (xbt_id) is the same
        across all releases. This method loads the XBT with all related information including
        release, library, log, subtype_of_xbt (parent in type hierarchy), and user information.
        
        Args:
            xbt_manifest_id: The XBT manifest ID to retrieve (release-specific identifier)
            
        Returns:
            XbtManifest: The XBT manifest with all relationships loaded:
                - xbt: The XBT entity with subtype_of_xbt, creator, owner, last_updater
                - release: Release information with library
                - log: Log information (if available)
            
        Raises:
            HTTPException: If XBT manifest not found (404) or data integrity issue (404)
        """
        logger.debug(f"Retrieving XBT manifest with ID: {xbt_manifest_id}")
        
        # Get XBT manifest with all relationships loaded
        xbt_manifest_query = (
            select(XbtManifest)
            .options(
                selectinload(XbtManifest.xbt).selectinload(Xbt.creator),
                selectinload(XbtManifest.xbt).selectinload(Xbt.owner),
                selectinload(XbtManifest.xbt).selectinload(Xbt.last_updater),
                selectinload(XbtManifest.xbt).selectinload(Xbt.subtype_of_xbt),
                selectinload(XbtManifest.release).selectinload(Release.library),
                selectinload(XbtManifest.log)
            )
            .where(XbtManifest.xbt_manifest_id == xbt_manifest_id)
        )
        xbt_manifest = db_exec(xbt_manifest_query).first()
        
        if not xbt_manifest:
            logger.warning(f"XBT manifest with ID {xbt_manifest_id} not found")
            raise HTTPException(
                status_code=404,
                detail=f"XBT manifest with ID {xbt_manifest_id} not found"
            )
        
        logger.debug(f"Found XBT manifest: xbt_id={xbt_manifest.xbt_id}, release_id={xbt_manifest.release_id}")
        
        # Verify XBT exists
        if not xbt_manifest.xbt:
            logger.error(f"XBT with ID {xbt_manifest.xbt_id} not found for manifest {xbt_manifest_id}")
            raise HTTPException(
                status_code=404,
                detail=f"XBT data not found for manifest ID {xbt_manifest_id}. This appears to be a data integrity issue."
            )
        
        return xbt_manifest
    
    @cache(key_prefix="xbt.get_subtype_xbt_manifest")
    @transaction(read_only=True)
    def get_subtype_xbt_manifest(self, xbt_id: int, release_id: int) -> XbtManifest | None:
        """
        Get the parent XBT manifest in the type hierarchy for a given XBT ID and release ID.
        
        XBTs form a type hierarchy through subtype relationships. For example, if an XBT has
        subtype_of_xbt_id pointing to another XBT, this method retrieves the parent XBT manifest
        for the specified release. This allows navigation up the type hierarchy.
        
        Examples of type hierarchy:
        - normalizedString → string (normalizedString.subtype_of_xbt_id = string.xbt_id)
        - integer → decimal (integer.subtype_of_xbt_id = decimal.xbt_id)
        - token → normalizedString (token.subtype_of_xbt_id = normalizedString.xbt_id)
        
        Args:
            xbt_id: The XBT ID (base entity ID) of the parent XBT to find
            release_id: The release ID to find the parent XBT manifest in
            
        Returns:
            XbtManifest | None: The parent XBT manifest if found in the specified release,
                None if not found (e.g., parent XBT doesn't exist in this release)
        """
        logger.debug(f"Retrieving subtype XBT manifest for xbt_id={xbt_id}, release_id={release_id}")
        
        subtype_xbt_manifest_query = (
            select(XbtManifest)
            .options(
                selectinload(XbtManifest.release).selectinload(Release.library)
            )
            .where(
                XbtManifest.xbt_id == xbt_id,
                XbtManifest.release_id == release_id
            )
        )
        subtype_xbt_manifest = db_exec(subtype_xbt_manifest_query).first()
        
        if subtype_xbt_manifest:
            logger.debug(f"Found subtype XBT manifest: xbt_manifest_id={subtype_xbt_manifest.xbt_manifest_id}")
        else:
            logger.debug(f"No subtype XBT manifest found for xbt_id={xbt_id}, release_id={release_id}")
        
        return subtype_xbt_manifest

