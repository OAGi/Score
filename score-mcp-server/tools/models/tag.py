"""Models for Tag tools."""
from pydantic import BaseModel

from tools.models.common import WhoAndWhen


class TagInfo(BaseModel):
    """Tag information object."""
    tag_id: int  # Unique identifier for the tag
    name: str  # Tag name (e.g., "BOD" for Business Object Document, "Noun", "Verb")
    description: str | None  # Description of what the tag represents or is used for
    color: str | None  # Background color code for the tag (typically hex color code like "#FF5733")
    text_color: str | None  # Text color code for the tag (typically hex color code like "#FFFFFF")
    created: WhoAndWhen  # Information about who created the tag and when
    last_updated: WhoAndWhen  # Information about who last updated the tag and when


class GetTagsResponse(BaseModel):
    """Response for get_tags tool."""
    total_items: int  # Total number of tags available
    offset: int  # Offset of the first item in this page
    limit: int  # Number of items returned in this page
    items: list[TagInfo]  # List of tags on this page

