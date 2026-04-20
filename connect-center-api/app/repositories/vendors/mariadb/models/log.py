"""MariaDB ORM model for the `log` table."""


from __future__ import annotations

from sqlalchemy import DateTime, ForeignKey, Integer, String, Text, func
from sqlalchemy.orm import Mapped, mapped_column

from app.repositories.vendors.mariadb.models.base import Base


class Log(Base):
    """Revision log table mapping."""
    __tablename__ = "log"

    log_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    hash: Mapped[str] = mapped_column(String(40), nullable=False)
    revision_num: Mapped[int] = mapped_column(Integer, nullable=False)
    revision_tracking_num: Mapped[int] = mapped_column(Integer, nullable=False)
    log_action: Mapped[str | None] = mapped_column(String(20), nullable=True)
    reference: Mapped[str] = mapped_column(String(100), nullable=False)
    snapshot: Mapped[str | None] = mapped_column(Text, nullable=True)
    prev_log_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("log.log_id"), nullable=True)
    next_log_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("log.log_id"), nullable=True)
    created_by: Mapped[int] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=False)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
