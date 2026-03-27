"""Models for Tag MCP tools."""

from __future__ import annotations

from pydantic import BaseModel, ConfigDict

from app.tools.models.shared import WhoAndWhen


class TagEntryResponse(BaseModel):
    """Tag payload for MCP tools."""

    tag_id: int
    name: str
    description: str | None = None
    color: str | None = None
    text_color: str | None = None
    created: WhoAndWhen
    last_updated: WhoAndWhen

    model_config = ConfigDict(frozen=True, from_attributes=True)


class GetTagPaginationResponse(BaseModel):
    """Response for get_tags tool."""

    total_items: int
    offset: int
    limit: int
    items: list[TagEntryResponse]

    model_config = ConfigDict(frozen=True)
