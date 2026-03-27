"""Pydantic response models for XBT endpoints."""


from __future__ import annotations

from pydantic import BaseModel, ConfigDict, Field

from app.routes.models.shared import LibrarySummaryRecord
from app.routes.models.shared import LogSummaryRecord
from app.routes.models.shared import ReleaseSummaryRecord
from app.routes.models.shared import UserSummary
from app.routes.models.shared import WhoAndWhen
from app.services.utils.string import Guid


class XbtSummaryEntry(BaseModel):
    """Subtype-of XBT summary used by XBT detail payloads."""

    xbt_manifest_id: int = Field(..., description="XBT manifest identifier.")
    xbt_id: int = Field(..., description="XBT identifier.")
    guid: Guid = Field(..., description="XBT GUID.")
    name: str | None = Field(default=None, description="XBT name.")
    builtIn_type: str | None = Field(default=None, description="Built-in type string with namespace prefix.")
    library: LibrarySummaryRecord = Field(..., description="Library information.")
    release: ReleaseSummaryRecord = Field(..., description="Release information.")

    model_config = ConfigDict(frozen=True)


class GetXbtByXbtManifestIdResponse(BaseModel):
    """XBT detail response model."""

    xbt_manifest_id: int = Field(..., description="XBT manifest identifier.")
    xbt_id: int = Field(..., description="XBT identifier.")
    guid: Guid = Field(..., description="XBT GUID.")
    name: str | None = Field(default=None, description="XBT name.")
    builtIn_type: str | None = Field(default=None, description="Built-in type string with namespace prefix.")
    jbt_draft05_map: str | None = Field(default=None, description="JSON Schema Draft 05 mapping.")
    openapi30_map: str | None = Field(default=None, description="OpenAPI 3.0 mapping.")
    avro_map: str | None = Field(default=None, description="Avro mapping.")
    subtype_of_xbt: XbtSummaryEntry | None = Field(default=None, description="Subtype-of XBT summary.")
    schema_definition: str | None = Field(default=None, description="Schema definition.")
    revision_doc: str | None = Field(default=None, description="Revision documentation.")
    state: int | None = Field(default=None, description="Lifecycle state.")
    is_deprecated: bool = Field(..., description="Whether this XBT is deprecated.")
    library: LibrarySummaryRecord = Field(..., description="Library information.")
    release: ReleaseSummaryRecord = Field(..., description="Release information.")
    log: LogSummaryRecord | None = Field(default=None, description="Revision log information.")
    owner: UserSummary = Field(..., description="Owner information.")
    created: WhoAndWhen = Field(..., description="Creation metadata.")
    last_updated: WhoAndWhen = Field(..., description="Last update metadata.")

    model_config = ConfigDict(
        frozen=True,
        json_schema_extra={
            "example": {
                "xbt_manifest_id": 12,
                "xbt_id": 12,
                "guid": "e7fe90c1b6a24d4a88ef7582267d57cd",
                "name": "string",
                "builtIn_type": "xsd:string",
                "jbt_draft05_map": "{\"type\":\"string\"}",
                "openapi30_map": "{\"type\":\"string\"}",
                "avro_map": "{\"type\":\"string\"}",
                "subtype_of_xbt": {
                    "xbt_manifest_id": 2,
                    "xbt_id": 2,
                    "guid": "7114441198054ad78d2fcf9bcdab2cbf",
                    "name": "any simple type",
                    "builtIn_type": "xsd:anySimpleType",
                    "library": {
                        "library_id": 3,
                        "name": "connectSpec",
                    },
                    "release": {
                        "release_id": 1,
                        "release_num": "10.6",
                        "state": "Published",
                    },
                },
                "schema_definition": None,
                "revision_doc": None,
                "state": 3,
                "is_deprecated": False,
                "library": {
                    "library_id": 3,
                    "name": "connectSpec",
                },
                "release": {
                    "release_id": 1,
                    "release_num": "10.6",
                    "state": "Published",
                },
                "log": {
                    "log_id": 10449,
                    "revision_num": 1,
                    "revision_tracking_num": 1,
                },
                "owner": {
                    "user_id": 1,
                    "login_id": "oagis",
                    "username": "Open Applications Group Developer",
                    "roles": ["Admin", "Developer"],
                },
                "created": {
                    "who": {
                        "user_id": 0,
                        "login_id": "sysadm",
                        "username": "System",
                        "roles": ["Admin", "Developer"],
                    },
                    "when": "2019-10-02T15:28:29.691000",
                },
                "last_updated": {
                    "who": {
                        "user_id": 0,
                        "login_id": "sysadm",
                        "username": "System",
                        "roles": ["Admin", "Developer"],
                    },
                    "when": "2019-10-02T15:28:29.691000",
                },
            }
        },
    )

