"""MariaDB ORM models for data type tables used by read repositories."""


from __future__ import annotations

from sqlalchemy import Boolean, DateTime, ForeignKey, Integer, String, Text, func
from sqlalchemy.orm import Mapped, mapped_column

from app.repositories.vendors.mariadb.models.base import Base


class Dt(Base):
    """Represent Dt."""
    __tablename__ = "dt"

    dt_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False)
    data_type_term: Mapped[str | None] = mapped_column(String(45), nullable=True)
    qualifier: Mapped[str | None] = mapped_column(String(100), nullable=True)
    representation_term: Mapped[str | None] = mapped_column(String(100), nullable=True)
    six_digit_id: Mapped[str | None] = mapped_column(String(45), nullable=True)
    based_dt_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("dt.dt_id"), nullable=True)
    definition: Mapped[str | None] = mapped_column(Text, nullable=True)
    definition_source: Mapped[str | None] = mapped_column(String(200), nullable=True)
    namespace_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("namespace.namespace_id"), nullable=True)
    content_component_definition: Mapped[str | None] = mapped_column(Text, nullable=True)
    state: Mapped[str | None] = mapped_column(String(20), nullable=True)
    commonly_used: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    owner_user_id: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    last_updated_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    last_update_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    is_deprecated: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    replacement_dt_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("dt.dt_id"), nullable=True)
    prev_dt_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("dt.dt_id"), nullable=True)
    next_dt_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("dt.dt_id"), nullable=True)


class DtManifest(Base):
    """Represent DtManifest."""
    __tablename__ = "dt_manifest"

    dt_manifest_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    release_id: Mapped[int] = mapped_column(Integer, ForeignKey("release.release_id"), nullable=False)
    dt_id: Mapped[int] = mapped_column(Integer, ForeignKey("dt.dt_id"), nullable=False)
    based_dt_manifest_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("dt_manifest.dt_manifest_id"), nullable=True)
    den: Mapped[str] = mapped_column(String(200), nullable=False)
    log_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("log.log_id"), nullable=True)


class DtSc(Base):
    """Represent DtSc."""
    __tablename__ = "dt_sc"

    dt_sc_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False)
    object_class_term: Mapped[str | None] = mapped_column(String(60), nullable=True)
    property_term: Mapped[str | None] = mapped_column(String(60), nullable=True)
    representation_term: Mapped[str | None] = mapped_column(String(20), nullable=True)
    definition: Mapped[str | None] = mapped_column(Text, nullable=True)
    definition_source: Mapped[str | None] = mapped_column(String(200), nullable=True)
    owner_dt_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("dt.dt_id"), nullable=True)
    cardinality_min: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    cardinality_max: Mapped[int | None] = mapped_column(Integer, nullable=True)
    based_dt_sc_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("dt_sc.dt_sc_id"), nullable=True)
    default_value: Mapped[str | None] = mapped_column(Text, nullable=True)
    fixed_value: Mapped[str | None] = mapped_column(Text, nullable=True)
    is_deprecated: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    replacement_dt_sc_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("dt_sc.dt_sc_id"), nullable=True)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    owner_user_id: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    last_updated_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    last_update_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    prev_dt_sc_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("dt_sc.dt_sc_id"), nullable=True)
    next_dt_sc_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("dt_sc.dt_sc_id"), nullable=True)


class DtScManifest(Base):
    """Represent DtScManifest."""
    __tablename__ = "dt_sc_manifest"

    dt_sc_manifest_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    release_id: Mapped[int] = mapped_column(Integer, ForeignKey("release.release_id"), nullable=False)
    dt_sc_id: Mapped[int] = mapped_column(Integer, ForeignKey("dt_sc.dt_sc_id"), nullable=False)
    owner_dt_manifest_id: Mapped[int] = mapped_column(Integer, ForeignKey("dt_manifest.dt_manifest_id"), nullable=False)
    based_dt_sc_manifest_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("dt_sc_manifest.dt_sc_manifest_id"), nullable=True)
    conflict: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    replacement_dt_sc_manifest_id: Mapped[int | None] = mapped_column(
        Integer,
        ForeignKey("dt_sc_manifest.dt_sc_manifest_id"),
        nullable=True,
    )
    prev_dt_sc_manifest_id: Mapped[int | None] = mapped_column(
        Integer,
        ForeignKey("dt_sc_manifest.dt_sc_manifest_id"),
        nullable=True,
    )
    next_dt_sc_manifest_id: Mapped[int | None] = mapped_column(
        Integer,
        ForeignKey("dt_sc_manifest.dt_sc_manifest_id"),
        nullable=True,
    )


class DtAwdPri(Base):
    """Represent DtAwdPri."""
    __tablename__ = "dt_awd_pri"

    dt_awd_pri_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    release_id: Mapped[int] = mapped_column(Integer, ForeignKey("release.release_id"), nullable=False)
    dt_id: Mapped[int] = mapped_column(Integer, ForeignKey("dt.dt_id"), nullable=False)
    cdt_pri_id: Mapped[int] = mapped_column(Integer, nullable=False)
    xbt_manifest_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("xbt_manifest.xbt_manifest_id"), nullable=True)
    code_list_manifest_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("code_list_manifest.code_list_manifest_id"), nullable=True)
    agency_id_list_manifest_id: Mapped[int | None] = mapped_column(
        Integer,
        ForeignKey("agency_id_list_manifest.agency_id_list_manifest_id"),
        nullable=True,
    )
    is_default: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)


class DtScAwdPri(Base):
    """Represent DtScAwdPri."""
    __tablename__ = "dt_sc_awd_pri"

    dt_sc_awd_pri_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    release_id: Mapped[int] = mapped_column(Integer, ForeignKey("release.release_id"), nullable=False)
    dt_sc_id: Mapped[int] = mapped_column(Integer, ForeignKey("dt_sc.dt_sc_id"), nullable=False)
    cdt_pri_id: Mapped[int] = mapped_column(Integer, nullable=False)
    xbt_manifest_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("xbt_manifest.xbt_manifest_id"), nullable=True)
    code_list_manifest_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("code_list_manifest.code_list_manifest_id"), nullable=True)
    agency_id_list_manifest_id: Mapped[int | None] = mapped_column(
        Integer,
        ForeignKey("agency_id_list_manifest.agency_id_list_manifest_id"),
        nullable=True,
    )
    is_default: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
