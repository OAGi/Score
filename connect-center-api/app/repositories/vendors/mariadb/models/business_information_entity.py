"""MariaDB ORM models for business information entity tables used by read repositories."""


from __future__ import annotations

from decimal import Decimal

from sqlalchemy import Boolean, DateTime, ForeignKey, Integer, Numeric, String, Text, func
from sqlalchemy.orm import Mapped, mapped_column

from app.repositories.vendors.mariadb.models.base import Base


class TopLevelAsbiep(Base):
    """Top-level ASBIEP table mapping."""

    __tablename__ = "top_level_asbiep"

    top_level_asbiep_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    asbiep_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("asbiep.asbiep_id"), nullable=True)
    owner_user_id: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    last_updated_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    release_id: Mapped[int] = mapped_column(Integer, ForeignKey("release.release_id"), nullable=False)
    version: Mapped[str | None] = mapped_column(String(45), nullable=True)
    status: Mapped[str | None] = mapped_column(String(45), nullable=True)
    state: Mapped[str | None] = mapped_column(String(20), nullable=True)
    is_deprecated: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    deprecated_reason: Mapped[str | None] = mapped_column(Text, nullable=True)
    deprecated_remark: Mapped[str | None] = mapped_column(Text, nullable=True)
    last_update_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())


class Asbiep(Base):
    """ASBIEP table mapping."""

    __tablename__ = "asbiep"

    asbiep_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False)
    based_asccp_manifest_id: Mapped[int] = mapped_column(Integer, ForeignKey("asccp_manifest.asccp_manifest_id"), nullable=False)
    path: Mapped[str | None] = mapped_column(Text, nullable=True)
    hash_path: Mapped[str | None] = mapped_column(String(64), nullable=True)
    role_of_abie_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("abie.abie_id"), nullable=True)
    display_name: Mapped[str | None] = mapped_column(String(100), nullable=True)
    definition: Mapped[str | None] = mapped_column(Text, nullable=True)
    biz_term: Mapped[str | None] = mapped_column(String(225), nullable=True)
    remark: Mapped[str | None] = mapped_column(String(225), nullable=True)
    last_updated_by: Mapped[int | None] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=True)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    last_update_timestamp: Mapped[object | None] = mapped_column(DateTime, nullable=True)
    owner_top_level_asbiep_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("top_level_asbiep.top_level_asbiep_id"), nullable=True)


class Abie(Base):
    """ABIE table mapping."""

    __tablename__ = "abie"

    abie_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False)
    based_acc_manifest_id: Mapped[int] = mapped_column(Integer, ForeignKey("acc_manifest.acc_manifest_id"), nullable=False)
    path: Mapped[str | None] = mapped_column(Text, nullable=True)
    hash_path: Mapped[str | None] = mapped_column(String(64), nullable=True)
    definition: Mapped[str | None] = mapped_column(Text, nullable=True)
    remark: Mapped[str | None] = mapped_column(String(225), nullable=True)
    biz_term: Mapped[str | None] = mapped_column(String(225), nullable=True)
    owner_top_level_asbiep_id: Mapped[int] = mapped_column(
        Integer,
        ForeignKey("top_level_asbiep.top_level_asbiep_id"),
        nullable=False,
    )
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    last_updated_by: Mapped[int | None] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=True)
    last_update_timestamp: Mapped[object | None] = mapped_column(DateTime, nullable=True)


class Asbie(Base):
    """ASBIE table mapping."""

    __tablename__ = "asbie"

    asbie_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False)
    based_ascc_manifest_id: Mapped[int] = mapped_column(Integer, ForeignKey("ascc_manifest.ascc_manifest_id"), nullable=False)
    path: Mapped[str | None] = mapped_column(Text, nullable=True)
    hash_path: Mapped[str | None] = mapped_column(String(64), nullable=True)
    from_abie_id: Mapped[int] = mapped_column(Integer, ForeignKey("abie.abie_id"), nullable=False)
    to_asbiep_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("asbiep.asbiep_id"), nullable=True)
    definition: Mapped[str | None] = mapped_column(Text, nullable=True)
    cardinality_min: Mapped[int] = mapped_column(Integer, nullable=False)
    cardinality_max: Mapped[int] = mapped_column(Integer, nullable=False)
    is_nillable: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    remark: Mapped[str | None] = mapped_column(String(225), nullable=True)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    last_updated_by: Mapped[int | None] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=True)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    last_update_timestamp: Mapped[object | None] = mapped_column(DateTime, nullable=True)
    seq_key: Mapped[Decimal] = mapped_column(Numeric(10, 2), nullable=False, default=Decimal('0'))
    is_used: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    owner_top_level_asbiep_id: Mapped[int] = mapped_column(
        Integer,
        ForeignKey("top_level_asbiep.top_level_asbiep_id"),
        nullable=False,
    )


class Bbiep(Base):
    """BBIEP table mapping."""

    __tablename__ = "bbiep"

    bbiep_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False)
    based_bccp_manifest_id: Mapped[int] = mapped_column(Integer, ForeignKey("bccp_manifest.bccp_manifest_id"), nullable=False)
    path: Mapped[str | None] = mapped_column(Text, nullable=True)
    hash_path: Mapped[str | None] = mapped_column(String(64), nullable=True)
    definition: Mapped[str | None] = mapped_column(Text, nullable=True)
    remark: Mapped[str | None] = mapped_column(String(225), nullable=True)
    biz_term: Mapped[str | None] = mapped_column(String(225), nullable=True)
    display_name: Mapped[str | None] = mapped_column(String(100), nullable=True)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    last_updated_by: Mapped[int | None] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=True)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    last_update_timestamp: Mapped[object | None] = mapped_column(DateTime, nullable=True)
    owner_top_level_asbiep_id: Mapped[int] = mapped_column(
        Integer,
        ForeignKey("top_level_asbiep.top_level_asbiep_id"),
        nullable=False,
    )


class Bbie(Base):
    """BBIE table mapping."""

    __tablename__ = "bbie"

    bbie_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False)
    based_bcc_manifest_id: Mapped[int] = mapped_column(Integer, ForeignKey("bcc_manifest.bcc_manifest_id"), nullable=False)
    path: Mapped[str | None] = mapped_column(Text, nullable=True)
    hash_path: Mapped[str | None] = mapped_column(String(64), nullable=True)
    from_abie_id: Mapped[int] = mapped_column(Integer, ForeignKey("abie.abie_id"), nullable=False)
    to_bbiep_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("bbiep.bbiep_id"), nullable=True)
    xbt_manifest_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("xbt_manifest.xbt_manifest_id"), nullable=True)
    code_list_manifest_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("code_list_manifest.code_list_manifest_id"), nullable=True)
    agency_id_list_manifest_id: Mapped[int | None] = mapped_column(
        Integer,
        ForeignKey("agency_id_list_manifest.agency_id_list_manifest_id"),
        nullable=True,
    )
    cardinality_min: Mapped[int] = mapped_column(Integer, nullable=False)
    cardinality_max: Mapped[int | None] = mapped_column(Integer, nullable=True)
    facet_min_length: Mapped[int | None] = mapped_column(Integer, nullable=True)
    facet_max_length: Mapped[int | None] = mapped_column(Integer, nullable=True)
    facet_pattern: Mapped[str | None] = mapped_column(Text, nullable=True)
    default_value: Mapped[str | None] = mapped_column(Text, nullable=True)
    fixed_value: Mapped[str | None] = mapped_column(Text, nullable=True)
    is_nillable: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    definition: Mapped[str | None] = mapped_column(Text, nullable=True)
    example: Mapped[str | None] = mapped_column(Text, nullable=True)
    remark: Mapped[str | None] = mapped_column(String(225), nullable=True)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    last_updated_by: Mapped[int | None] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=True)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    last_update_timestamp: Mapped[object | None] = mapped_column(DateTime, nullable=True)
    is_used: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    owner_top_level_asbiep_id: Mapped[int] = mapped_column(
        Integer,
        ForeignKey("top_level_asbiep.top_level_asbiep_id"),
        nullable=False,
    )


class BbieSc(Base):
    """BBIE_SC table mapping."""

    __tablename__ = "bbie_sc"

    bbie_sc_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False)
    based_dt_sc_manifest_id: Mapped[int] = mapped_column(Integer, ForeignKey("dt_sc_manifest.dt_sc_manifest_id"), nullable=False)
    path: Mapped[str | None] = mapped_column(Text, nullable=True)
    hash_path: Mapped[str | None] = mapped_column(String(64), nullable=True)
    bbie_id: Mapped[int] = mapped_column(Integer, ForeignKey("bbie.bbie_id"), nullable=False)
    xbt_manifest_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("xbt_manifest.xbt_manifest_id"), nullable=True)
    code_list_manifest_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("code_list_manifest.code_list_manifest_id"), nullable=True)
    agency_id_list_manifest_id: Mapped[int | None] = mapped_column(
        Integer,
        ForeignKey("agency_id_list_manifest.agency_id_list_manifest_id"),
        nullable=True,
    )
    cardinality_min: Mapped[int] = mapped_column(Integer, nullable=False)
    cardinality_max: Mapped[int] = mapped_column(Integer, nullable=False)
    facet_min_length: Mapped[int | None] = mapped_column(Integer, nullable=True)
    facet_max_length: Mapped[int | None] = mapped_column(Integer, nullable=True)
    facet_pattern: Mapped[str | None] = mapped_column(Text, nullable=True)
    default_value: Mapped[str | None] = mapped_column(Text, nullable=True)
    fixed_value: Mapped[str | None] = mapped_column(Text, nullable=True)
    definition: Mapped[str | None] = mapped_column(Text, nullable=True)
    example: Mapped[str | None] = mapped_column(Text, nullable=True)
    remark: Mapped[str | None] = mapped_column(String(225), nullable=True)
    biz_term: Mapped[str | None] = mapped_column(String(225), nullable=True)
    display_name: Mapped[str | None] = mapped_column(String(100), nullable=True)
    is_used: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    last_updated_by: Mapped[int | None] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=True)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    last_update_timestamp: Mapped[object | None] = mapped_column(DateTime, nullable=True)
    owner_top_level_asbiep_id: Mapped[int] = mapped_column(
        Integer,
        ForeignKey("top_level_asbiep.top_level_asbiep_id"),
        nullable=False,
    )
