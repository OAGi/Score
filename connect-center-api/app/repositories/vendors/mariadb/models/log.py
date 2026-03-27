"""MariaDB ORM model for the `log` table."""


from __future__ import annotations

from sqlalchemy import Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from app.repositories.vendors.mariadb.models.base import Base


class Log(Base):
    """Revision log table mapping."""
    __tablename__ = "log"

    log_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    revision_num: Mapped[int] = mapped_column(Integer, nullable=False)
    revision_tracking_num: Mapped[int] = mapped_column(Integer, nullable=False)
    hash: Mapped[str | None] = mapped_column(String(64), nullable=True)
