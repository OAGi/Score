"""MariaDB ORM model for the `ctx_category` table.

Defines context category metadata used to organize context schemes.
"""


from __future__ import annotations

from sqlalchemy import ForeignKey, text
from sqlalchemy.dialects import mysql
from sqlalchemy.orm import Mapped, mapped_column

from app.repositories.vendors.mariadb.models.base import Base


class ContextCategory(Base):
    """Context category table mapping."""
    __tablename__ = "ctx_category"
    __table_args__ = {
        "mysql_engine": "InnoDB",
        "mysql_charset": "utf8mb3",
        "mysql_collate": "utf8mb3_general_ci",
        "comment": (
            "This table captures the context category. Examples of context categories as described "
            "in the CCTS are business process, industry, etc."
        ),
    }

    ctx_category_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        primary_key=True,
        autoincrement=True,
        comment="Internal, primary, database key.",
    )

    guid: Mapped[str] = mapped_column(
        mysql.CHAR(32, charset="ascii", collation="ascii_general_ci"),
        nullable=False,
        unique=True,
        comment="A globally unique identifier (GUID).",
    )

    name: Mapped[str | None] = mapped_column(mysql.VARCHAR(45), nullable=True)
    description: Mapped[str | None] = mapped_column(mysql.TEXT, nullable=True)

    created_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )
    last_updated_by: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        ForeignKey("app_user.app_user_id"),
        nullable=False,
    )

    creation_timestamp: Mapped[object] = mapped_column(
        mysql.DATETIME(fsp=6),
        nullable=False,
        server_default=text("CURRENT_TIMESTAMP(6)"),
    )
    last_update_timestamp: Mapped[object] = mapped_column(
        mysql.DATETIME(fsp=6),
        nullable=False,
        server_default=text("CURRENT_TIMESTAMP(6)"),
    )
