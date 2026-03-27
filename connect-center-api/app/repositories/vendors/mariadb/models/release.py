"""MariaDB ORM models for releases and release dependencies."""


from __future__ import annotations

from sqlalchemy import DateTime, ForeignKey, Integer, String, Text, func
from sqlalchemy.orm import Mapped, mapped_column

from app.repositories.vendors.mariadb.models.base import Base


class Release(Base):
    """Release table mapping."""
    __tablename__ = "release"

    release_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    library_id: Mapped[int] = mapped_column(Integer, ForeignKey("library.library_id"), nullable=False)
    guid: Mapped[str] = mapped_column(String(32), nullable=False, unique=True)
    release_num: Mapped[str | None] = mapped_column(String(45), nullable=True)
    release_note: Mapped[str | None] = mapped_column(Text, nullable=True)
    release_license: Mapped[str | None] = mapped_column(Text, nullable=True)
    namespace_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("namespace.namespace_id"), nullable=True)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    last_updated_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    last_update_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    state: Mapped[str] = mapped_column(String(20), nullable=False, server_default="Initialized")
    prev_release_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("release.release_id"), nullable=True)
    next_release_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("release.release_id"), nullable=True)


class ReleaseDep(Base):
    """Release dependency mapping (release_id -> depend_on_release_id)."""
    __tablename__ = "release_dep"

    release_dep_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    release_id: Mapped[int] = mapped_column(Integer, ForeignKey("release.release_id"), nullable=False)
    depend_on_release_id: Mapped[int] = mapped_column(Integer, ForeignKey("release.release_id"), nullable=False)
