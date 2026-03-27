"""MariaDB ORM models for OAuth2 configuration and linked users."""


from __future__ import annotations

from sqlalchemy import Boolean, DateTime, ForeignKey, Integer, String, UniqueConstraint, func
from sqlalchemy.orm import Mapped, mapped_column

from app.repositories.vendors.mariadb.models.base import Base


class OAuth2App(Base):
    """OAuth2 application/provider table mapping."""
    __tablename__ = "oauth2_app"

    oauth2_app_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    provider_name: Mapped[str] = mapped_column(String(100), nullable=False)
    issuer_uri: Mapped[str | None] = mapped_column(String(200), nullable=True)
    authorization_uri: Mapped[str | None] = mapped_column(String(200), nullable=True)
    token_uri: Mapped[str | None] = mapped_column(String(200), nullable=True)
    user_info_uri: Mapped[str | None] = mapped_column(String(200), nullable=True)
    jwk_set_uri: Mapped[str | None] = mapped_column(String(200), nullable=True)
    redirect_uri: Mapped[str] = mapped_column(String(200), nullable=False)
    end_session_endpoint: Mapped[str | None] = mapped_column(String(200), nullable=True)
    client_id: Mapped[str] = mapped_column(String(200), nullable=False)
    client_secret: Mapped[str] = mapped_column(String(200), nullable=False)
    client_authentication_method: Mapped[str] = mapped_column(String(50), nullable=False)
    authorization_grant_type: Mapped[str] = mapped_column(String(50), nullable=False)
    prompt: Mapped[str | None] = mapped_column(String(20), nullable=True)
    display_provider_name: Mapped[str | None] = mapped_column(String(100), nullable=True)
    background_color: Mapped[str | None] = mapped_column(String(50), nullable=True)
    font_color: Mapped[str | None] = mapped_column(String(50), nullable=True)
    display_order: Mapped[int] = mapped_column(Integer, nullable=False, server_default="0")
    is_disabled: Mapped[bool] = mapped_column(Boolean, nullable=False, server_default="0")


class AppOAuth2User(Base):
    """OAuth2 user linkage table mapping."""
    __tablename__ = "app_oauth2_user"
    __table_args__ = (UniqueConstraint("oauth2_app_id", "sub", name="uq_app_oauth2_user_oauth2_app_id_sub"),)

    app_oauth2_user_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    app_user_id: Mapped[int | None] = mapped_column(Integer, ForeignKey("app_user.app_user_id"), nullable=True)
    oauth2_app_id: Mapped[int] = mapped_column(Integer, ForeignKey("oauth2_app.oauth2_app_id"), nullable=False)
    sub: Mapped[str] = mapped_column(String(100), nullable=False)
    name: Mapped[str | None] = mapped_column(String(200), nullable=True)
    email: Mapped[str | None] = mapped_column(String(200), nullable=True)
    nickname: Mapped[str | None] = mapped_column(String(200), nullable=True)
    preferred_username: Mapped[str | None] = mapped_column(String(200), nullable=True)
    phone_number: Mapped[str | None] = mapped_column(String(200), nullable=True)
    creation_timestamp: Mapped[object] = mapped_column(DateTime, nullable=False, server_default=func.now())
