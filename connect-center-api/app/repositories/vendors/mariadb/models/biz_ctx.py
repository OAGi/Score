"""MariaDB ORM models for business contexts and context values."""


from __future__ import annotations

from sqlalchemy import DateTime, ForeignKey, Integer, String, UniqueConstraint, func
from sqlalchemy.orm import Mapped, mapped_column

from app.repositories.vendors.mariadb.models.base import Base


class BizCtx(Base):
    """Business context table mapping."""
    __tablename__ = "biz_ctx"

    biz_ctx_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guid: Mapped[str] = mapped_column(String(32), nullable=False, unique=True)
    name: Mapped[str | None] = mapped_column(String(100), nullable=True)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    last_updated_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
    last_update_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())


class BizCtxValue(Base):
    """Business context value mapping with uniqueness constraint."""
    __tablename__ = "biz_ctx_value"
    __table_args__ = (
        UniqueConstraint("biz_ctx_id", "ctx_scheme_value_id", name="uq_biz_ctx_value_biz_ctx_id_ctx_scheme_value_id"),
    )

    biz_ctx_value_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    biz_ctx_id: Mapped[int] = mapped_column(Integer, ForeignKey("biz_ctx.biz_ctx_id"), nullable=False)
    ctx_scheme_value_id: Mapped[int] = mapped_column(
        Integer,
        ForeignKey("ctx_scheme_value.ctx_scheme_value_id"),
        nullable=False,
    )


class BizCtxAssignment(Base):
    """Business context assignment mapping to top-level ASBIEP."""

    __tablename__ = "biz_ctx_assignment"

    biz_ctx_assignment_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    biz_ctx_id: Mapped[int] = mapped_column(Integer, ForeignKey("biz_ctx.biz_ctx_id"), nullable=False)
    top_level_asbiep_id: Mapped[int] = mapped_column(Integer, ForeignKey("top_level_asbiep.top_level_asbiep_id"), nullable=False)
