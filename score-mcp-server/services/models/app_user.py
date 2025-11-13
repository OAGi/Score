from datetime import datetime
from typing import Optional, List
from sqlmodel import SQLModel, Field, Relationship


class AppUserBase(SQLModel):
    """Base model for AppUser with common fields."""
    login_id: str = Field(max_length=45, description="User Id of the user.")
    password: Optional[str] = Field(default=None, max_length=100, description="Password to authenticate the user.")
    name: Optional[str] = Field(default=None, max_length=100, description="Full name of the user.")
    organization: Optional[str] = Field(default=None, max_length=100, description="The company the user represents.")
    email: Optional[str] = Field(default=None, max_length=100, description="Email address.")
    email_verified: bool = Field(default=False, description="The fact whether the email value is verified or not.")
    email_verified_timestamp: Optional[datetime] = Field(default=None, description="The timestamp when the email address has verified.")
    is_developer: Optional[bool] = Field(default=None)
    is_admin: bool = Field(default=False, description="Indicator whether the user has an admin role or not.")
    is_enabled: bool = Field(default=True)

class AppUser(AppUserBase, table=True):
    """Model for the app_user table."""
    __tablename__ = "app_user"
    
    app_user_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key column.")
    
    # Relationships
    oauth2_users: List["AppOAuth2User"] = Relationship(back_populates="app_user")

class AppUserRead(AppUserBase):
    """Model for reading AppUser data."""
    app_user_id: int

class AppOAuth2UserBase(SQLModel):
    """Base model for AppOAuth2User with common fields."""
    sub: str = Field(max_length=100, description="sub claim defined in OIDC spec. This is a unique identifier of the subject in the provider.")
    name: Optional[str] = Field(default=None, max_length=200, description="name claim defined in OIDC spec.")
    email: Optional[str] = Field(default=None, max_length=200, description="email claim defined in OIDC spec.")
    nickname: Optional[str] = Field(default=None, max_length=200, description="nickname claim defined in OIDC spec.")
    preferred_username: Optional[str] = Field(default=None, max_length=200, description="preferred_username claim defined in OIDC spec.")
    phone_number: Optional[str] = Field(default=None, max_length=200, description="phone_number claim defined in OIDC spec.")
    creation_timestamp: datetime = Field(description="Timestamp when this record is created.")

class AppOAuth2User(AppOAuth2UserBase, table=True):
    """Model for the app_oauth2_user table."""
    __tablename__ = "app_oauth2_user"
    
    app_oauth2_user_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key.")
    app_user_id: Optional[int] = Field(default=None, foreign_key="app_user.app_user_id", description="A reference to the record in app_user. If it is not set, this is treated as a pending record.")
    oauth2_app_id: int = Field(foreign_key="oauth2_app.oauth2_app_id", description="A reference to the record in oauth2_app.")
    
    # Relationships
    app_user: Optional[AppUser] = Relationship(back_populates="oauth2_users")
    oauth2_app: "OAuth2App" = Relationship(back_populates="oauth2_users")

class AppOAuth2UserRead(AppOAuth2UserBase):
    """Model for reading AppOAuth2User data."""
    app_oauth2_user_id: int
    app_user_id: Optional[int] = None
    oauth2_app_id: int

class OAuth2AppBase(SQLModel):
    """Base model for OAuth2App with common fields."""
    provider_name: str = Field(max_length=100, description="OAuth2 provider name.")
    issuer_uri: Optional[str] = Field(default=None, max_length=200, description="OIDC issuer URI.")
    authorization_uri: Optional[str] = Field(default=None, max_length=200, description="Authorization endpoint URI.")
    token_uri: Optional[str] = Field(default=None, max_length=200, description="Token endpoint URI.")
    user_info_uri: Optional[str] = Field(default=None, max_length=200, description="User info endpoint URI.")
    jwk_set_uri: Optional[str] = Field(default=None, max_length=200, description="JWK Set URI.")
    redirect_uri: str = Field(max_length=200, description="Redirect URI for OAuth2 flow.")
    end_session_endpoint: Optional[str] = Field(default=None, max_length=200, description="End session endpoint URI.")
    client_id: str = Field(max_length=200, description="OAuth2 client ID.")
    client_secret: str = Field(max_length=200, description="OAuth2 client secret.")
    client_authentication_method: str = Field(max_length=50, description="Client authentication method.")
    authorization_grant_type: str = Field(max_length=50, description="Authorization grant type.")
    prompt: Optional[str] = Field(default=None, max_length=20, description="OAuth2 prompt parameter.")
    display_provider_name: Optional[str] = Field(default=None, max_length=100, description="Display name for the provider.")
    background_color: Optional[str] = Field(default=None, max_length=50, description="Background color for UI.")
    font_color: Optional[str] = Field(default=None, max_length=50, description="Font color for UI.")
    display_order: int = Field(default=0, description="Display order for UI.")
    is_disabled: bool = Field(default=False, description="Whether the OAuth2 app is disabled.")

class OAuth2App(OAuth2AppBase, table=True):
    """Model for the oauth2_app table."""
    __tablename__ = "oauth2_app"
    
    oauth2_app_id: Optional[int] = Field(default=None, primary_key=True, description="Primary key.")
    
    # Relationships
    oauth2_users: List["AppOAuth2User"] = Relationship(back_populates="oauth2_app")

class OAuth2AppRead(OAuth2AppBase):
    """Model for reading OAuth2App data."""
    oauth2_app_id: int
