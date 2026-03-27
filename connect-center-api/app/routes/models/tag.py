"""Pydantic response models for Tag endpoints."""


from __future__ import annotations

from pydantic import BaseModel, ConfigDict, Field

from app.routes.models.shared import WhoAndWhen


class TagEntry(BaseModel):
    """API representation of a tag record."""

    tag_id: int = Field(..., description="Unique identifier for the tag.")
    name: str = Field(..., description="Tag name.")
    description: str | None = Field(default=None, description="Description of the tag.")
    color: str | None = Field(default=None, description="Background color code for the tag.")
    text_color: str | None = Field(default=None, description="Text color code for the tag.")
    created: WhoAndWhen = Field(..., description="Information about who created the tag and when.")
    last_updated: WhoAndWhen = Field(..., description="Information about who last updated the tag and when.")

    model_config = ConfigDict(frozen=True)


class GetTagListResponse(BaseModel):
    """Paginated response envelope for tag listings."""

    total_items: int = Field(..., ge=0, description="Total number of matching tags.")
    offset: int = Field(..., ge=0, description="Offset used for this page.")
    limit: int = Field(..., ge=1, le=100, description="Limit used for this page.")
    items: list[TagEntry] = Field(..., description="Tags.")

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "total_items": 3,
                "offset": 0,
                "limit": 10,
                "items": [
                    {
                        "tag_id": 3,
                        "name": "Verb",
                        "description": None,
                        "color": "#1A48A2",
                        "text_color": "#FFFFFF",
                        "created": {
                            "who": {
                                "user_id": 1,
                                "login_id": "oagis",
                                "username": "Open Applications Group Developer",
                                "roles": ["Admin", "Developer"],
                            },
                            "when": "2023-03-13T18:55:56.697460",
                        },
                        "last_updated": {
                            "who": {
                                "user_id": 1,
                                "login_id": "oagis",
                                "username": "Open Applications Group Developer",
                                "roles": ["Admin", "Developer"],
                            },
                            "when": "2023-03-13T18:55:56.697460",
                        },
                    },
                    {
                        "tag_id": 2,
                        "name": "Noun",
                        "description": None,
                        "color": "#1C0F5C",
                        "text_color": "#FFFFFF",
                        "created": {
                            "who": {
                                "user_id": 1,
                                "login_id": "oagis",
                                "username": "Open Applications Group Developer",
                                "roles": ["Admin", "Developer"],
                            },
                            "when": "2023-03-13T18:55:56.695082",
                        },
                        "last_updated": {
                            "who": {
                                "user_id": 1,
                                "login_id": "oagis",
                                "username": "Open Applications Group Developer",
                                "roles": ["Admin", "Developer"],
                            },
                            "when": "2023-03-13T18:55:56.695082",
                        },
                    },
                    {
                        "tag_id": 1,
                        "name": "BOD",
                        "description": "Business Document Object",
                        "color": "#D1446B",
                        "text_color": "#FFFFFF",
                        "created": {
                            "who": {
                                "user_id": 1,
                                "login_id": "oagis",
                                "username": "Open Applications Group Developer",
                                "roles": ["Admin", "Developer"],
                            },
                            "when": "2023-03-13T18:55:56.692713",
                        },
                        "last_updated": {
                            "who": {
                                "user_id": 1,
                                "login_id": "oagis",
                                "username": "Open Applications Group Developer",
                                "roles": ["Admin", "Developer"],
                            },
                            "when": "2023-03-13T18:55:56.692713",
                        },
                    },
                ],
            }
        },
    )

