"""Shared models used across multiple tool domains."""
from datetime import datetime, timezone
from typing import Union

from pydantic import BaseModel, field_serializer, field_validator


class NamespaceInfo(BaseModel):
    """Namespace information object."""
    namespace_id: int  # Unique identifier for the namespace
    prefix: str | None  # Default short name for the URI (if any), used as an XML namespace prefix
    uri: str  # Namespace URI (Uniform Resource Identifier), uniquely identifies the namespace


class LibraryInfo(BaseModel):
    """Library information object."""
    library_id: int  # Unique identifier for the library
    name: str  # Library name (e.g., "connectSpec")


class ReleaseInfo(BaseModel):
    """Release information object."""
    release_id: int  # Unique identifier for the release
    release_num: str | None  # Release number (e.g., "10.0", "10.1"), indicating the version of the release
    state: str  # Current state of the release (e.g., "Published", "Draft", "Processing")


class LogInfo(BaseModel):
    """Log information object."""
    log_id: int  # Unique identifier for the log entry
    revision_num: int  # Revision number indicating the version of the logged item
    revision_tracking_num: int  # Revision tracking number used for tracking changes across revisions


class UserInfo(BaseModel):
    """User information object."""
    user_id: int  # Unique identifier for the user
    login_id: str  # User's login identifier
    username: str  # Display name of the user
    roles: list[str]  # List of roles assigned to the user. Three types of roles are supported:
                      # - Admin: Administrator that can control system configurations, data ownership management, and library/release creation
                      # - Developer: Can create/edit core components for the library
                      # - End-User: Can utilize core components for profiling business information entities


class WhoAndWhen(BaseModel):
    """Who and when information object."""
    who: UserInfo  # Information about the user who performed the action
    when: datetime  # RFC 3339 timestamp

    @field_validator('when', mode='before')
    @classmethod
    def parse_when(cls, v: Union[datetime, str]) -> datetime:
        """Parse datetime string (YYYY-MM-DDTHH:MM:SSZ format) to datetime object."""
        if isinstance(v, str):
            # Handle Z suffix (UTC indicator)
            if v.endswith('Z'):
                v = v[:-1] + '+00:00'
            # Parse ISO format string to datetime
            return datetime.fromisoformat(v)
        return v

    @field_serializer('when')
    def serialize_when(self, dt: datetime, _info) -> str:
        """Serialize datetime to RFC 3339 format string with Z suffix for UTC."""
        if dt.tzinfo is None:
            dt = dt.replace(tzinfo=timezone.utc)
        # Convert to UTC if timezone-aware
        if dt.tzinfo != timezone.utc:
            dt = dt.astimezone(timezone.utc)
        # Format as YYYY-MM-DDTHH:MM:SSZ
        return dt.strftime('%Y-%m-%dT%H:%M:%SZ')


class PaginationResponse(BaseModel):
    """Response model for paginated data."""
    total: int  # Total number of items available
    offset: int  # Number of items to skip from the beginning
    limit: int  # Maximum number of items to return
    items: list  # List of items in the current page

