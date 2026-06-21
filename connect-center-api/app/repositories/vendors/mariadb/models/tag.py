"""MariaDB ORM models for tags and manifest tag links used by core components."""


from __future__ import annotations

from sqlalchemy import DateTime, ForeignKey, Integer, String, Text, func
from sqlalchemy.orm import Mapped, mapped_column

from app.repositories.vendors.mariadb.models.base import Base


class Tag(Base):
    """Represent Tag."""
    __tablename__ = "tag"

    tag_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    description: Mapped[str | None] = mapped_column(Text, nullable=True)
    text_color: Mapped[str | None] = mapped_column(String(50), nullable=True)
    background_color: Mapped[str | None] = mapped_column(String(50), nullable=True)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    last_updated_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    last_update_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())


class AccManifestTag(Base):
    """Represent AccManifestTag."""
    __tablename__ = "acc_manifest_tag"

    acc_manifest_id: Mapped[int] = mapped_column(Integer, ForeignKey("acc_manifest.acc_manifest_id"), primary_key=True)
    tag_id: Mapped[int] = mapped_column(Integer, ForeignKey("tag.tag_id"), primary_key=True)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())


class AsccpManifestTag(Base):
    """Represent AsccpManifestTag."""
    __tablename__ = "asccp_manifest_tag"

    asccp_manifest_id: Mapped[int] = mapped_column(Integer, ForeignKey("asccp_manifest.asccp_manifest_id"), primary_key=True)
    tag_id: Mapped[int] = mapped_column(Integer, ForeignKey("tag.tag_id"), primary_key=True)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())


class BccpManifestTag(Base):
    """Represent BccpManifestTag."""
    __tablename__ = "bccp_manifest_tag"

    bccp_manifest_id: Mapped[int] = mapped_column(Integer, ForeignKey("bccp_manifest.bccp_manifest_id"), primary_key=True)
    tag_id: Mapped[int] = mapped_column(Integer, ForeignKey("tag.tag_id"), primary_key=True)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())


class DtManifestTag(Base):
    """Represent DtManifestTag."""
    __tablename__ = "dt_manifest_tag"

    dt_manifest_id: Mapped[int] = mapped_column(Integer, ForeignKey("dt_manifest.dt_manifest_id"), primary_key=True)
    tag_id: Mapped[int] = mapped_column(Integer, ForeignKey("tag.tag_id"), primary_key=True)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
