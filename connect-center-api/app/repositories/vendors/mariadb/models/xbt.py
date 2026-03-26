"""MariaDB ORM models for XBT and XBT manifest tables."""


from __future__ import annotations

from sqlalchemy import Boolean, DateTime, ForeignKey, Integer, String, Text, func
from sqlalchemy.orm import Mapped, mapped_column

from app.repositories.vendors.mariadb.models.base import Base


class Xbt(Base):
    """Represent Xbt."""
    __tablename__ = "xbt"

    xbt_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False)
    name: Mapped[str | None] = mapped_column(String(45), nullable=True)
    builtIn_type: Mapped[str | None] = mapped_column(String(45), nullable=True)
    jbt_draft05_map: Mapped[str | None] = mapped_column(String(500), nullable=True)
    openapi30_map: Mapped[str | None] = mapped_column(String(500), nullable=True)
    avro_map: Mapped[str | None] = mapped_column(String(500), nullable=True)
    subtype_of_xbt_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("xbt.xbt_id"), nullable=True)
    schema_definition: Mapped[str | None] = mapped_column(Text, nullable=True)
    revision_doc: Mapped[str | None] = mapped_column(Text, nullable=True)
    state: Mapped[int | None] = mapped_column(Integer, nullable=True)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    owner_user_id: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    last_updated_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    last_update_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    is_deprecated: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)


class XbtManifest(Base):
    """Represent XbtManifest."""
    __tablename__ = "xbt_manifest"

    xbt_manifest_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    release_id: Mapped[int] = mapped_column(Integer, ForeignKey("release.release_id"), nullable=False)
    xbt_id: Mapped[int] = mapped_column(Integer, ForeignKey("xbt.xbt_id"), nullable=False)
    log_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("log.log_id"), nullable=True)
