"""MariaDB ORM model for the `app_user` table.

Defines application users and authentication-related attributes.
"""


from __future__ import annotations

from sqlalchemy import text
from sqlalchemy.dialects import mysql
from sqlalchemy.orm import Mapped, mapped_column

from app.repositories.vendors.mariadb.models.base import Base


class AppUser(Base):
    """Application user table mapping."""
    __tablename__ = "app_user"
    __table_args__ = {
        "mysql_engine": "InnoDB",
        "mysql_charset": "utf8mb3",
        "mysql_collate": "utf8mb3_general_ci",
        "comment": "This table captures the user information for authentication and authorization purposes.",
    }

    app_user_id: Mapped[int] = mapped_column(
        mysql.BIGINT(unsigned=True),
        primary_key=True,
        autoincrement=True,
        comment="Primary key column.",
    )
    login_id: Mapped[str] = mapped_column(mysql.VARCHAR(45), nullable=False, unique=True, comment="User Id of the user.")

    # Intentionally present for ORM completeness; DO NOT expose in read routes.
    password: Mapped[str | None] = mapped_column(mysql.VARCHAR(100), nullable=True, comment="Password to authenticate the user.")

    name: Mapped[str | None] = mapped_column(mysql.VARCHAR(100), nullable=True, comment="Full name of the user.")
    organization: Mapped[str | None] = mapped_column(mysql.VARCHAR(100), nullable=True, comment="The company the user represents.")
    email: Mapped[str | None] = mapped_column(mysql.VARCHAR(100), nullable=True, comment="Email address.")

    email_verified: Mapped[int] = mapped_column(
        mysql.TINYINT(1),
        nullable=False,
        server_default=text("0"),
        comment="The fact whether the email value is verified or not.",
    )
    email_verified_timestamp: Mapped[object | None] = mapped_column(
        mysql.DATETIME(fsp=6),
        nullable=True,
        comment="The timestamp when the email address has verified.",
    )
    is_developer: Mapped[int | None] = mapped_column(mysql.TINYINT(1), nullable=True)
    is_admin: Mapped[int | None] = mapped_column(
        mysql.TINYINT(1),
        nullable=True,
        server_default=text("0"),
        comment="Indicator whether the user has an admin role or not.",
    )
    is_enabled: Mapped[int | None] = mapped_column(mysql.TINYINT(1), nullable=True, server_default=text("1"))
