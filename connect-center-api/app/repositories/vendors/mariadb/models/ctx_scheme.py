"""MariaDB ORM models for context schemes and scheme values."""


from __future__ import annotations

from sqlalchemy import DateTime, ForeignKey, Integer, String, Text, func
from sqlalchemy.orm import Mapped, mapped_column

from app.repositories.vendors.mariadb.models.base import Base


class CtxScheme(Base):
    """Context scheme table mapping."""
    __tablename__ = "ctx_scheme"

    ctx_scheme_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False, unique=True)

    scheme_id: Mapped[str] = mapped_column(String(45), nullable=False)
    scheme_name: Mapped[str] = mapped_column(String(255), nullable=False)
    description: Mapped[str | None] = mapped_column(Text, nullable=True)
    scheme_agency_id: Mapped[str] = mapped_column(String(45), nullable=False, server_default="")
    scheme_version_id: Mapped[str] = mapped_column(String(45), nullable=False, server_default="")

    ctx_category_id: Mapped[int | None] = mapped_column(
        Integer,
        ForeignKey("ctx_category.ctx_category_id"),
        nullable=True,
    )

    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    last_updated_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    last_update_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())


class CtxSchemeValue(Base):
    """Context scheme value table mapping."""
    __tablename__ = "ctx_scheme_value"

    ctx_scheme_value_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False, unique=True)
    value: Mapped[str] = mapped_column(String(100), nullable=False)
    meaning: Mapped[str | None] = mapped_column(Text, nullable=True)
    owner_ctx_scheme_id: Mapped[int] = mapped_column(
        Integer,
        ForeignKey("ctx_scheme.ctx_scheme_id"),
        nullable=False,
    )
