"""MariaDB ORM models for agency id list tables used by read repositories."""


from __future__ import annotations

from sqlalchemy import Boolean, DateTime, ForeignKey, Integer, String, Text, func
from sqlalchemy.orm import Mapped, mapped_column

from app.repositories.vendors.mariadb.models.base import Base


class AgencyIdList(Base):
    """Represent AgencyIdList."""
    __tablename__ = "agency_id_list"

    agency_id_list_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False)
    enum_type_guid: Mapped[str | None] = mapped_column(String(41), nullable=True)
    name: Mapped[str | None] = mapped_column(String(100), nullable=True)
    list_id: Mapped[str | None] = mapped_column(String(100), nullable=True)
    version_id: Mapped[str | None] = mapped_column(String(100), nullable=True)
    definition: Mapped[str | None] = mapped_column(Text, nullable=True)
    remark: Mapped[str | None] = mapped_column(String(225), nullable=True)
    definition_source: Mapped[str | None] = mapped_column(String(100), nullable=True)
    namespace_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("namespace.namespace_id"), nullable=True)
    is_deprecated: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    owner_user_id: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    last_updated_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    last_update_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    state: Mapped[str | None] = mapped_column(String(20), nullable=True)


class AgencyIdListManifest(Base):
    """Represent AgencyIdListManifest."""
    __tablename__ = "agency_id_list_manifest"

    agency_id_list_manifest_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    release_id: Mapped[int] = mapped_column(Integer, ForeignKey("release.release_id"), nullable=False)
    agency_id_list_id: Mapped[int] = mapped_column(Integer, ForeignKey("agency_id_list.agency_id_list_id"), nullable=False)
    log_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("log.log_id"), nullable=True)


class AgencyIdListValue(Base):
    """Represent AgencyIdListValue."""
    __tablename__ = "agency_id_list_value"

    agency_id_list_value_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False)
    value: Mapped[str] = mapped_column(String(150), nullable=False)
    name: Mapped[str | None] = mapped_column(String(150), nullable=True)
    definition: Mapped[str | None] = mapped_column(Text, nullable=True)
    is_deprecated: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    is_developer_default: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    is_user_default: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)


class AgencyIdListValueManifest(Base):
    """Represent AgencyIdListValueManifest."""
    __tablename__ = "agency_id_list_value_manifest"

    agency_id_list_value_manifest_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    agency_id_list_manifest_id: Mapped[int] = mapped_column(Integer, ForeignKey("agency_id_list_manifest.agency_id_list_manifest_id"), nullable=False)
    agency_id_list_value_id: Mapped[int] = mapped_column(Integer, ForeignKey("agency_id_list_value.agency_id_list_value_id"), nullable=False)
