"""MariaDB ORM models for OpenAPI document tables."""


from __future__ import annotations

from sqlalchemy import ForeignKey
from sqlalchemy.dialects import mysql
from sqlalchemy.orm import Mapped, mapped_column

from app.repositories.vendors.mariadb.models.base import Base


class OasDoc(Base):
    """OpenAPI document table mapping."""

    __tablename__ = "oas_doc"

    oas_doc_id: Mapped[int] = mapped_column(mysql.BIGINT(unsigned=True), primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(mysql.VARCHAR(41), nullable=False)
    open_api_version: Mapped[str] = mapped_column(mysql.VARCHAR(20), nullable=False)
    title: Mapped[str] = mapped_column(mysql.TEXT, nullable=False)
    description: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    terms_of_service: Mapped[str | None] = mapped_column(mysql.VARCHAR(250), nullable=True)
    version: Mapped[str] = mapped_column(mysql.VARCHAR(20), nullable=False)
    contact_name: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    contact_url: Mapped[str | None] = mapped_column(mysql.VARCHAR(250), nullable=True)
    contact_email: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    license_name: Mapped[str | None] = mapped_column(mysql.VARCHAR(100), nullable=True)
    license_url: Mapped[str | None] = mapped_column(mysql.VARCHAR(250), nullable=True)
    owner_user_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    created_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    last_updated_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    creation_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
    last_update_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)


class OasTag(Base):
    """OpenAPI tag table mapping."""

    __tablename__ = "oas_tag"

    oas_tag_id: Mapped[int] = mapped_column(mysql.BIGINT(unsigned=True), primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(mysql.VARCHAR(41), nullable=False)
    name: Mapped[str] = mapped_column(mysql.VARCHAR(200), nullable=False)
    description: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    created_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    last_updated_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    creation_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
    last_update_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)


class OasDocTag(Base):
    """Join table between OAS docs and tags."""

    __tablename__ = "oas_doc_tag"

    oas_doc_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_doc.oas_doc_id"),
        primary_key=True,
    )
    oas_tag_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_tag.oas_tag_id"),
        primary_key=True,
    )
    created_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    last_updated_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    creation_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
    last_update_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)


class OasExample(Base):
    """OpenAPI example table mapping."""

    __tablename__ = "oas_example"

    oas_example_id: Mapped[int] = mapped_column(mysql.BIGINT(unsigned=True), primary_key=True, autoincrement=True)
    summary: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    description: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    ref: Mapped[str | None] = mapped_column(mysql.VARCHAR(250), nullable=True)
    value: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    created_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    last_updated_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    creation_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
    last_update_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)


class OasExternalDoc(Base):
    """OpenAPI external-doc table mapping."""

    __tablename__ = "oas_external_doc"

    oas_external_doc_id: Mapped[int] = mapped_column(mysql.BIGINT(unsigned=True), primary_key=True, autoincrement=True)
    url: Mapped[str] = mapped_column(mysql.VARCHAR(250), nullable=False)
    description: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    created_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    last_updated_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    creation_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
    last_update_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)


class OasExternalDocDoc(Base):
    """Join table between OAS docs and external docs."""

    __tablename__ = "oas_external_doc_doc"

    oas_external_doc_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_external_doc.oas_external_doc_id"),
        primary_key=True,
    )
    oas_doc_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_doc.oas_doc_id"),
        primary_key=True,
    )
    created_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    last_updated_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    creation_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
    last_update_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)


class OasHttpHeader(Base):
    """OpenAPI HTTP header table mapping."""

    __tablename__ = "oas_http_header"

    oas_http_header_id: Mapped[int] = mapped_column(mysql.BIGINT(unsigned=True), primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(mysql.VARCHAR(41), nullable=False)
    header: Mapped[str | None] = mapped_column(mysql.VARCHAR(200), nullable=True)
    description: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    agency_id_list_value_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("agency_id_list_value.agency_id_list_value_id"),
        nullable=False,
    )
    schema_type_reference: Mapped[str] = mapped_column(mysql.TEXT, nullable=False)
    owner_user_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    created_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    last_updated_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    creation_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
    last_update_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)


class OasMediaType(Base):
    """OpenAPI media type table mapping."""

    __tablename__ = "oas_media_type"

    oas_media_type_id: Mapped[int] = mapped_column(mysql.BIGINT(unsigned=True), primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(mysql.VARCHAR(41), nullable=False)
    description: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    owner_user_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    created_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    last_updated_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    creation_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
    last_update_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)


class OasMessageBody(Base):
    """OpenAPI message body table mapping."""

    __tablename__ = "oas_message_body"

    oas_message_body_id: Mapped[int] = mapped_column(mysql.BIGINT(unsigned=True), primary_key=True, autoincrement=True)
    top_level_asbiep_id: Mapped[int | None] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("top_level_asbiep.top_level_asbiep_id"),
        nullable=True,
    )
    created_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    last_updated_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    creation_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
    last_update_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)


class OasResource(Base):
    """OpenAPI resource table mapping."""

    __tablename__ = "oas_resource"

    oas_resource_id: Mapped[int] = mapped_column(mysql.BIGINT(unsigned=True), primary_key=True, autoincrement=True)
    oas_doc_id: Mapped[int | None] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_doc.oas_doc_id"),
        nullable=True,
    )
    path: Mapped[str] = mapped_column(mysql.TEXT, nullable=False)
    ref: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    created_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    last_updated_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    creation_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
    last_update_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)


class OasOperation(Base):
    """OpenAPI operation table mapping."""

    __tablename__ = "oas_operation"

    oas_operation_id: Mapped[int] = mapped_column(mysql.BIGINT(unsigned=True), primary_key=True, autoincrement=True)
    oas_resource_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_resource.oas_resource_id"),
        nullable=False,
    )
    verb: Mapped[str] = mapped_column(mysql.VARCHAR(30), nullable=False)
    operation_id: Mapped[str] = mapped_column(mysql.VARCHAR(1024), nullable=False)
    summary: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    description: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    deprecated: Mapped[int | None] = mapped_column(mysql.TINYINT(1), nullable=True, default=0)
    created_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    last_updated_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    creation_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
    last_update_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)


class OasParameter(Base):
    """OpenAPI parameter table mapping."""

    __tablename__ = "oas_parameter"

    oas_parameter_id: Mapped[int] = mapped_column(mysql.BIGINT(unsigned=True), primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(mysql.VARCHAR(41), nullable=False)
    name: Mapped[str] = mapped_column(mysql.VARCHAR(200), nullable=False)
    in_: Mapped[str] = mapped_column("in", mysql.VARCHAR(100), nullable=False)
    required: Mapped[int] = mapped_column(mysql.TINYINT(1), nullable=False, default=0)
    description: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    schema_type_reference: Mapped[str] = mapped_column(mysql.TEXT, nullable=False)
    allow_reserved: Mapped[int | None] = mapped_column(mysql.TINYINT(1), nullable=True, default=0)
    deprecated: Mapped[int | None] = mapped_column(mysql.TINYINT(1), nullable=True, default=0)
    oas_http_header_id: Mapped[int | None] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_http_header.oas_http_header_id"),
        nullable=True,
    )
    created_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    last_updated_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    creation_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
    last_update_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)


class OasParameterLink(Base):
    """OpenAPI parameter-link table mapping."""

    __tablename__ = "oas_parameter_link"

    oas_parameter_link_id: Mapped[int] = mapped_column(mysql.BIGINT(unsigned=True), primary_key=True, autoincrement=True)
    oas_response_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_response.oas_response_id"),
        nullable=False,
    )
    oas_parameter_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_parameter.oas_parameter_id"),
        nullable=False,
    )
    oas_operation_id: Mapped[int | None] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_operation.oas_operation_id"),
        nullable=True,
    )
    expression: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    description: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    created_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    last_updated_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    creation_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
    last_update_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)


class OasRequest(Base):
    """OpenAPI request table mapping."""

    __tablename__ = "oas_request"

    oas_request_id: Mapped[int] = mapped_column(mysql.BIGINT(unsigned=True), primary_key=True, autoincrement=True)
    oas_operation_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_operation.oas_operation_id"),
        nullable=False,
    )
    description: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    required: Mapped[int] = mapped_column(mysql.TINYINT(1), nullable=False, default=0)
    oas_message_body_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_message_body.oas_message_body_id"),
        nullable=False,
    )
    make_array_indicator: Mapped[int | None] = mapped_column(mysql.TINYINT(1), nullable=True, default=0)
    suppress_root_indicator: Mapped[int | None] = mapped_column(mysql.TINYINT(1), nullable=True, default=0)
    meta_header_top_level_asbiep_id: Mapped[int | None] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("top_level_asbiep.top_level_asbiep_id"),
        nullable=True,
    )
    pagination_top_level_asbiep_id: Mapped[int | None] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("top_level_asbiep.top_level_asbiep_id"),
        nullable=True,
    )
    is_callback: Mapped[int | None] = mapped_column(mysql.TINYINT(1), nullable=True, default=0)
    created_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    last_updated_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    creation_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
    last_update_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)


class OasRequestParameter(Base):
    """Join table between OAS requests and parameters."""

    __tablename__ = "oas_request_parameter"

    oas_parameter_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_parameter.oas_parameter_id"),
        primary_key=True,
    )
    oas_request_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_request.oas_request_id"),
        primary_key=True,
    )
    created_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    last_updated_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    creation_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
    last_update_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)


class OasResponse(Base):
    """OpenAPI response table mapping."""

    __tablename__ = "oas_response"

    oas_response_id: Mapped[int] = mapped_column(mysql.BIGINT(unsigned=True), primary_key=True, autoincrement=True)
    oas_operation_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_operation.oas_operation_id"),
        nullable=False,
    )
    http_status_code: Mapped[int | None] = mapped_column(mysql.INTEGER, nullable=True)
    description: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    oas_message_body_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_message_body.oas_message_body_id"),
        nullable=False,
    )
    make_array_indicator: Mapped[int | None] = mapped_column(mysql.TINYINT(1), nullable=True, default=0)
    suppress_root_indicator: Mapped[int | None] = mapped_column(mysql.TINYINT(1), nullable=True, default=0)
    meta_header_top_level_asbiep_id: Mapped[int | None] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("top_level_asbiep.top_level_asbiep_id"),
        nullable=True,
    )
    pagination_top_level_asbiep_id: Mapped[int | None] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("top_level_asbiep.top_level_asbiep_id"),
        nullable=True,
    )
    include_confirm_indicator: Mapped[int | None] = mapped_column(mysql.TINYINT(1), nullable=True, default=0)
    created_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    last_updated_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    creation_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
    last_update_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)


class OasResponseHeaders(Base):
    """Join table between OAS responses and HTTP headers."""

    __tablename__ = "oas_response_headers"

    oas_response_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_response.oas_response_id"),
        primary_key=True,
    )
    oas_http_header_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_http_header.oas_http_header_id"),
        primary_key=True,
    )
    created_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    last_updated_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    creation_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
    last_update_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)


class OasResourceTag(Base):
    """Join table between OAS operations and tags."""

    __tablename__ = "oas_resource_tag"

    oas_operation_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_operation.oas_operation_id"),
        primary_key=True,
    )
    oas_tag_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_tag.oas_tag_id"),
        primary_key=True,
    )
    created_by: Mapped[int] = mapped_column(mysql.BIGINT(unsigned=True), nullable=False)
    last_updated_by: Mapped[int] = mapped_column(mysql.BIGINT(unsigned=True), nullable=False)
    creation_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
    last_update_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)


class OasServer(Base):
    """OpenAPI server table mapping."""

    __tablename__ = "oas_server"

    oas_server_id: Mapped[int] = mapped_column(mysql.BIGINT(unsigned=True), primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(mysql.VARCHAR(41), nullable=False)
    oas_doc_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_doc.oas_doc_id"),
        nullable=False,
    )
    description: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    url: Mapped[str] = mapped_column(mysql.VARCHAR(250), nullable=False)
    variables: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    owner_user_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    created_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    last_updated_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    creation_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
    last_update_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)


class OasServerVariable(Base):
    """OpenAPI server-variable table mapping."""

    __tablename__ = "oas_server_variable"

    oas_server_variable_id: Mapped[int] = mapped_column(mysql.BIGINT(unsigned=True), primary_key=True, autoincrement=True)
    oas_server_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("oas_server.oas_server_id"),
        nullable=False,
    )
    name: Mapped[str | None] = mapped_column(mysql.VARCHAR(100), nullable=True)
    description: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)
    default_: Mapped[str | None] = mapped_column("default", mysql.TEXT, nullable=True)
    enum_: Mapped[str | None] = mapped_column("enum", mysql.TEXT, nullable=True)
    created_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    last_updated_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    creation_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
    last_update_timestamp: Mapped[object] = mapped_column(mysql.DATETIME(fsp=6), nullable=False)
