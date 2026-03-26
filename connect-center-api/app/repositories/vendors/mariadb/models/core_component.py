"""MariaDB ORM models for core component tables used by read repositories."""


from __future__ import annotations

from sqlalchemy import Boolean, DateTime, ForeignKey, Integer, String, Text, func
from sqlalchemy.orm import Mapped, mapped_column

from app.repositories.vendors.mariadb.models.base import Base


class Acc(Base):
    """Represent Acc."""
    __tablename__ = "acc"

    acc_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False)
    type: Mapped[str | None] = mapped_column(String(50), nullable=True)
    object_class_term: Mapped[str | None] = mapped_column(String(100), nullable=True)
    object_class_qualifier: Mapped[str | None] = mapped_column(String(100), nullable=True)
    definition: Mapped[str | None] = mapped_column(Text, nullable=True)
    definition_source: Mapped[str | None] = mapped_column(String(100), nullable=True)
    namespace_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("namespace.namespace_id"), nullable=True)
    owner_user_id: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    last_updated_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    last_update_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    state: Mapped[str | None] = mapped_column(String(20), nullable=True)
    is_deprecated: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    oagis_component_type: Mapped[int | None] = mapped_column(Integer, nullable=True)
    is_abstract: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)


class AccManifest(Base):
    """Represent AccManifest."""
    __tablename__ = "acc_manifest"

    acc_manifest_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    release_id: Mapped[int] = mapped_column(Integer, ForeignKey("release.release_id"), nullable=False)
    acc_id: Mapped[int] = mapped_column(Integer, ForeignKey("acc.acc_id"), nullable=False)
    based_acc_manifest_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("acc_manifest.acc_manifest_id"), nullable=True)
    den: Mapped[str] = mapped_column(String(351), nullable=False)
    log_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("log.log_id"), nullable=True)


class Asccp(Base):
    """Represent Asccp."""
    __tablename__ = "asccp"

    asccp_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False)
    property_term: Mapped[str | None] = mapped_column(String(100), nullable=True)
    type: Mapped[str] = mapped_column(String(32), nullable=False, server_default="Default")
    reusable_indicator: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    is_nillable: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    definition: Mapped[str | None] = mapped_column(Text, nullable=True)
    definition_source: Mapped[str | None] = mapped_column(String(100), nullable=True)
    namespace_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("namespace.namespace_id"), nullable=True)
    owner_user_id: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    last_updated_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    last_update_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    state: Mapped[str | None] = mapped_column(String(20), nullable=True)
    is_deprecated: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)


class AsccpManifest(Base):
    """Represent AsccpManifest."""
    __tablename__ = "asccp_manifest"

    asccp_manifest_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    release_id: Mapped[int] = mapped_column(Integer, ForeignKey("release.release_id"), nullable=False)
    asccp_id: Mapped[int] = mapped_column(Integer, ForeignKey("asccp.asccp_id"), nullable=False)
    role_of_acc_manifest_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("acc_manifest.acc_manifest_id"), nullable=True)
    den: Mapped[str] = mapped_column(String(304), nullable=False)
    log_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("log.log_id"), nullable=True)


class Bccp(Base):
    """Represent Bccp."""
    __tablename__ = "bccp"

    bccp_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False)
    property_term: Mapped[str] = mapped_column(String(100), nullable=False)
    representation_term: Mapped[str] = mapped_column(String(20), nullable=False)
    bdt_id: Mapped[int] = mapped_column(Integer, ForeignKey("dt.dt_id"), nullable=False)
    definition: Mapped[str | None] = mapped_column(Text, nullable=True)
    definition_source: Mapped[str | None] = mapped_column(String(100), nullable=True)
    namespace_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("namespace.namespace_id"), nullable=True)
    owner_user_id: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    last_updated_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    last_update_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    state: Mapped[str | None] = mapped_column(String(20), nullable=True)
    is_deprecated: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    default_value: Mapped[str | None] = mapped_column(Text, nullable=True)
    fixed_value: Mapped[str | None] = mapped_column(Text, nullable=True)
    is_nillable: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)


class BccpManifest(Base):
    """Represent BccpManifest."""
    __tablename__ = "bccp_manifest"

    bccp_manifest_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    release_id: Mapped[int] = mapped_column(Integer, ForeignKey("release.release_id"), nullable=False)
    bccp_id: Mapped[int] = mapped_column(Integer, ForeignKey("bccp.bccp_id"), nullable=False)
    bdt_manifest_id: Mapped[int] = mapped_column(Integer, ForeignKey("dt_manifest.dt_manifest_id"), nullable=False)
    den: Mapped[str] = mapped_column(String(351), nullable=False)
    log_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("log.log_id"), nullable=True)


class Ascc(Base):
    """Represent Ascc."""
    __tablename__ = "ascc"

    ascc_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False)
    cardinality_min: Mapped[int] = mapped_column(Integer, nullable=False)
    cardinality_max: Mapped[int] = mapped_column(Integer, nullable=False)
    definition: Mapped[str | None] = mapped_column(Text, nullable=True)
    definition_source: Mapped[str | None] = mapped_column(String(100), nullable=True)
    is_deprecated: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)


class AsccManifest(Base):
    """Represent AsccManifest."""
    __tablename__ = "ascc_manifest"

    ascc_manifest_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    ascc_id: Mapped[int] = mapped_column(Integer, ForeignKey("ascc.ascc_id"), nullable=False)
    from_acc_manifest_id: Mapped[int] = mapped_column(Integer, ForeignKey("acc_manifest.acc_manifest_id"), nullable=False)
    to_asccp_manifest_id: Mapped[int] = mapped_column(Integer, ForeignKey("asccp_manifest.asccp_manifest_id"), nullable=False)
    den: Mapped[str] = mapped_column(String(304), nullable=False)


class Bcc(Base):
    """Represent Bcc."""
    __tablename__ = "bcc"

    bcc_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False)
    cardinality_min: Mapped[int] = mapped_column(Integer, nullable=False)
    cardinality_max: Mapped[int | None] = mapped_column(Integer, nullable=True)
    entity_type: Mapped[int | None] = mapped_column(Integer, nullable=True)
    definition: Mapped[str | None] = mapped_column(Text, nullable=True)
    definition_source: Mapped[str | None] = mapped_column(String(100), nullable=True)
    is_deprecated: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    is_nillable: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    default_value: Mapped[str | None] = mapped_column(Text, nullable=True)
    fixed_value: Mapped[str | None] = mapped_column(Text, nullable=True)


class BccManifest(Base):
    """Represent BccManifest."""
    __tablename__ = "bcc_manifest"

    bcc_manifest_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    bcc_id: Mapped[int] = mapped_column(Integer, ForeignKey("bcc.bcc_id"), nullable=False)
    from_acc_manifest_id: Mapped[int] = mapped_column(Integer, ForeignKey("acc_manifest.acc_manifest_id"), nullable=False)
    to_bccp_manifest_id: Mapped[int] = mapped_column(Integer, ForeignKey("bccp_manifest.bccp_manifest_id"), nullable=False)
    den: Mapped[str] = mapped_column(String(351), nullable=False)


class SeqKey(Base):
    """Represent SeqKey."""
    __tablename__ = "seq_key"

    seq_key_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    from_acc_manifest_id: Mapped[int] = mapped_column(Integer, ForeignKey("acc_manifest.acc_manifest_id"), nullable=False)
    ascc_manifest_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("ascc_manifest.ascc_manifest_id"), nullable=True)
    bcc_manifest_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("bcc_manifest.bcc_manifest_id"), nullable=True)
    prev_seq_key_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("seq_key.seq_key_id"), nullable=True)
    next_seq_key_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("seq_key.seq_key_id"), nullable=True)
