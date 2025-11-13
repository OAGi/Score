"""Models for Release tools."""
from pydantic import BaseModel

from tools.models.common import LibraryInfo, NamespaceInfo, WhoAndWhen


class ReleaseReference(BaseModel):
    """Release reference object for prev/next release."""
    release_id: int  # Unique identifier for the referenced release
    release_num: str | None  # Release number of the referenced release (e.g., "10.0", "10.1")


class GetReleaseResponse(BaseModel):
    """Response for get_release tool."""
    release_id: int  # Unique identifier for the release
    library: LibraryInfo  # Library information that this release belongs to
    guid: str  # Globally unique identifier for the release
    release_num: str | None  # Release number indicating the version (e.g., "10.0", "10.1", "10.2")
    release_note: str | None  # Release notes describing what changed in this release
    release_license: str | None  # License information for the release (e.g., license text or URL)
    namespace: NamespaceInfo | None  # Default namespace information for this release (if any)
    state: str  # Current state of the release (e.g., "Published", "Draft", "Processing", "Initialized")
    created: WhoAndWhen  # Information about who created the release and when
    last_updated: WhoAndWhen  # Information about who last updated the release and when


class GetReleasesResponse(BaseModel):
    """Response for get_releases tool."""
    total_items: int  # Total number of releases available
    offset: int  # Offset of the first item in this page
    limit: int  # Number of items returned in this page
    items: list[GetReleaseResponse]  # List of releases on this page

