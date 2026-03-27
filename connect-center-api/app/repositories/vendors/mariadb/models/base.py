"""SQLAlchemy declarative base for MariaDB models."""


from __future__ import annotations

from sqlalchemy.orm import DeclarativeBase


class Base(DeclarativeBase):
    """Declarative base class for MariaDB ORM models."""
