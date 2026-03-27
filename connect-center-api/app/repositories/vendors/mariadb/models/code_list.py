"""MariaDB ORM models for code list tables used by read repositories."""


from __future__ import annotations

from sqlalchemy import Boolean, DateTime, ForeignKey, Integer, String, Text, func
from sqlalchemy.orm import Mapped, mapped_column

from app.repositories.vendors.mariadb.models.base import Base


class CodeList(Base):
    """Represent CodeList."""
    __tablename__ = "code_list"

    code_list_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False)
    enum_type_guid: Mapped[str | None] = mapped_column(String(41), nullable=True)
    name: Mapped[str | None] = mapped_column(String(100), nullable=True)
    list_id: Mapped[str] = mapped_column(String(100), nullable=False)
    version_id: Mapped[str] = mapped_column(String(100), nullable=False)
    definition: Mapped[str | None] = mapped_column(Text, nullable=True)
    remark: Mapped[str | None] = mapped_column(String(225), nullable=True)
    definition_source: Mapped[str | None] = mapped_column(String(100), nullable=True)
    namespace_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("namespace.namespace_id"), nullable=True)
    extensible_indicator: Mapped[bool] = mapped_column(Boolean, nullable=False)
    is_deprecated: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    owner_user_id: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    last_updated_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    last_update_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    state: Mapped[str | None] = mapped_column(String(20), nullable=True)


class CodeListManifest(Base):
    """Represent CodeListManifest."""
    __tablename__ = "code_list_manifest"

    code_list_manifest_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    release_id: Mapped[int] = mapped_column(Integer, ForeignKey("release.release_id"), nullable=False)
    code_list_id: Mapped[int] = mapped_column(Integer, ForeignKey("code_list.code_list_id"), nullable=False)
    log_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("log.log_id"), nullable=True)


class CodeListValue(Base):
    """Represent CodeListValue."""
    __tablename__ = "code_list_value"

    code_list_value_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False)
    value: Mapped[str] = mapped_column(Text, nullable=False)
    meaning: Mapped[str | None] = mapped_column(Text, nullable=True)
    definition: Mapped[str | None] = mapped_column(Text, nullable=True)
    is_deprecated: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)


class CodeListValueManifest(Base):
    """Represent CodeListValueManifest."""
    __tablename__ = "code_list_value_manifest"

    code_list_value_manifest_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    code_list_manifest_id: Mapped[int] = mapped_column(Integer, ForeignKey("code_list_manifest.code_list_manifest_id"), nullable=False)
    code_list_value_id: Mapped[int] = mapped_column(Integer, ForeignKey("code_list_value.code_list_value_id"), nullable=False)
