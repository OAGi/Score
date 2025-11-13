"""Models for Library tools."""
from pydantic import BaseModel

from tools.models.common import WhoAndWhen


class GetLibraryResponse(BaseModel):
    """Response for get_library tool."""
    library_id: int  # Unique identifier for the library
    name: str  # Library name (e.g., "connectSpec", "UBL")
    type: str | None  # Type of library (e.g., "Standard", "Custom")
    organization: str | None  # Organization or entity that owns or maintains the library
    description: str | None  # Description of the library's purpose and contents
    link: str | None  # URL link to more information about the library
    domain: str | None  # Domain or industry area the library covers (e.g., "Supply Chain", "Finance")
    state: str | None  # Current state of the library (e.g., "Active", "Archived")
    is_read_only: bool  # Whether the library is read-only and cannot be modified
    is_default: bool  # Whether this is the default library for new components
    created: WhoAndWhen  # Information about who created the library and when
    last_updated: WhoAndWhen  # Information about who last updated the library and when


class GetLibrariesResponse(BaseModel):
    """Response for get_libraries tool."""
    total_items: int  # Total number of libraries available
    offset: int  # Offset of the first item in this page
    limit: int  # Number of items returned in this page
    items: list[GetLibraryResponse]  # List of libraries on this page

