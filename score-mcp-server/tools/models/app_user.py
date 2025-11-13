"""Models for App User tools."""
from pydantic import BaseModel


class GetUserResponse(BaseModel):
    """Response for get_users tool."""
    user_id: int  # Unique identifier for the user
    login_id: str  # User's login identifier used for authentication
    username: str | None  # Display name of the user (human-readable name)
    organization: str | None  # The company or organization the user represents
    email: str | None  # Email address of the user
    roles: list[str]  # List of roles assigned to the user (Admin, Developer, End-User)
    is_enabled: bool  # Whether the user account is enabled and can access the system


class GetUsersResponse(BaseModel):
    """Response for get_users tool."""
    total_items: int  # Total number of users available
    offset: int  # Offset of the first item in this page
    limit: int  # Number of items returned in this page
    items: list[GetUserResponse]  # List of users on this page

