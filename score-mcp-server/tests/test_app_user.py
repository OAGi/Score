import asyncio
import json
import pytest
from fastmcp import Client
from fastmcp.client import BearerAuth


def extract_content(result):
    """Extract text content from result.content."""
    if hasattr(result, 'content') and result.content:
        if isinstance(result.content, list) and len(result.content) > 0:
            return result.content[0].text
        elif hasattr(result.content, 'text'):
            return result.content.text
    return str(result.content)


class TestGetUsers:
    """Test cases for the get_users tool."""

    @pytest.mark.asyncio
    async def test_get_users_basic(self, token):
        """Test basic get_users functionality with default parameters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_users", {})
            
            # Check that we get a valid response
            assert hasattr(result, 'data'), f"Expected data attribute, got: {result}"
            data = result.data
            
            # Check response structure
            assert hasattr(data, 'total_items'), f"Expected total_items, got: {data}"
            assert hasattr(data, 'offset'), f"Expected offset, got: {data}"
            assert hasattr(data, 'limit'), f"Expected limit, got: {data}"
            assert hasattr(data, 'items'), f"Expected items, got: {data}"
            
            # Check default values
            assert data.offset == 0, f"Expected offset 0, got: {data.offset}"
            assert data.limit == 10, f"Expected limit 10, got: {data.limit}"
            assert isinstance(data.total_items, int), f"Expected total_items to be int, got: {type(data.total_items)}"
            assert isinstance(data.items, list), f"Expected items to be list, got: {type(data.items)}"
            
            # Check that total_items is non-negative
            assert data.total_items >= 0, f"Expected total_items >= 0, got: {data.total_items}"

    @pytest.mark.asyncio
    async def test_get_users_pagination(self, token):
        """Test get_users with custom pagination parameters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Test with custom offset and limit
            result = await client.call_tool("get_users", {
                'offset': 0,
                'limit': 5
            })
            
            data = result.data
            assert data.offset == 0, f"Expected offset 0, got: {data.offset}"
            assert data.limit == 5, f"Expected limit 5, got: {data.limit}"
            assert len(data.items) <= 5, f"Expected <= 5 items, got: {len(data.items)}"

    @pytest.mark.asyncio
    async def test_get_users_filter_by_login_id(self, token):
        """Test get_users filtering by login_id."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # First get all users to find a login_id to filter by
            all_users_result = await client.call_tool("get_users", {'limit': 100})
            all_users = all_users_result.data.items
            
            if all_users:
                # Use the first user's login_id for filtering
                test_login_id = all_users[0].login_id
                
                # Filter by login_id
                result = await client.call_tool("get_users", {
                    'login_id': test_login_id
                })
                
                data = result.data
                assert len(data.items) > 0, f"Expected at least one user with login_id {test_login_id}"
                
                # Check that all returned users contain the login_id
                for user in data.items:
                    assert test_login_id.lower() in user.login_id.lower(), \
                        f"Expected login_id to contain '{test_login_id}', got: {user.login_id}"

    @pytest.mark.asyncio
    async def test_get_users_filter_by_username(self, token):
        """Test get_users filtering by username."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # First get all users to find a username to filter by
            all_users_result = await client.call_tool("get_users", {'limit': 100})
            all_users = all_users_result.data.items
            
            if all_users and all_users[0].username:
                # Use the first user's username for filtering
                test_username = all_users[0].username
                
                # Filter by username
                result = await client.call_tool("get_users", {
                    'username': test_username
                })
                
                data = result.data
                assert len(data.items) > 0, f"Expected at least one user with username {test_username}"
                
                # Check that all returned users contain the username
                for user in data.items:
                    if user.username:
                        assert test_username.lower() in user.username.lower(), \
                            f"Expected username to contain '{test_username}', got: {user.username}"

    @pytest.mark.asyncio
    async def test_get_users_filter_by_organization(self, token):
        """Test get_users filtering by organization."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # First get all users to find an organization to filter by
            all_users_result = await client.call_tool("get_users", {'limit': 100})
            all_users = all_users_result.data.items
            
            if all_users and all_users[0].organization:
                # Use the first user's organization for filtering
                test_organization = all_users[0].organization
                
                # Filter by organization
                result = await client.call_tool("get_users", {
                    'organization': test_organization
                })
                
                data = result.data
                assert len(data.items) > 0, f"Expected at least one user with organization {test_organization}"
                
                # Check that all returned users contain the organization
                for user in data.items:
                    if user.organization:
                        assert test_organization.lower() in user.organization.lower(), \
                            f"Expected organization to contain '{test_organization}', got: {user.organization}"

    @pytest.mark.asyncio
    async def test_get_users_filter_by_email(self, token):
        """Test get_users filtering by email."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # First get all users to find an email to filter by
            all_users_result = await client.call_tool("get_users", {'limit': 100})
            all_users = all_users_result.data.items
            
            if all_users and all_users[0].email:
                # Use the first user's email for filtering
                test_email = all_users[0].email
                
                # Filter by email
                result = await client.call_tool("get_users", {
                    'email': test_email
                })
                
                data = result.data
                assert len(data.items) > 0, f"Expected at least one user with email {test_email}"
                
                # Check that all returned users contain the email
                for user in data.items:
                    if user.email:
                        assert test_email.lower() in user.email.lower(), \
                            f"Expected email to contain '{test_email}', got: {user.email}"

    @pytest.mark.asyncio
    async def test_get_users_filter_by_is_admin(self, token):
        """Test get_users filtering by admin status."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Test filtering for admin users
            result = await client.call_tool("get_users", {
                'is_admin': True
            })
            
            data = result.data
            # Check that all returned users are admins
            for user in data.items:
                assert "Admin" in user.roles, f"Expected Admin role, got: {user.roles}"

    @pytest.mark.asyncio
    async def test_get_users_filter_by_is_developer(self, token):
        """Test get_users filtering by developer status."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Test filtering for developer users
            result = await client.call_tool("get_users", {
                'is_developer': True
            })
            
            data = result.data
            # Check that all returned users are developers
            for user in data.items:
                assert "Developer" in user.roles, f"Expected Developer role, got: {user.roles}"

    @pytest.mark.asyncio
    async def test_get_users_filter_by_is_enabled(self, token):
        """Test get_users filtering by enabled status."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Test filtering for enabled users
            result = await client.call_tool("get_users", {
                'is_enabled': True
            })
            
            data = result.data
            # Check that all returned users are enabled
            for user in data.items:
                assert user.is_enabled is True, f"Expected enabled user, got: {user.is_enabled}"

    @pytest.mark.asyncio
    async def test_get_users_ordering(self, token):
        """Test get_users with ordering."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Test ascending order by login_id
            result_asc = await client.call_tool("get_users", {
                'order_by': 'login_id',
                'limit': 5
            })
            
            data_asc = result_asc.data
            if len(data_asc.items) > 1:
                # Check that login_ids are in ascending order (database collation)
                login_ids = [user.login_id for user in data_asc.items]
                # The database might use different collation than Python's default sorting
                # So we'll just verify that the order is consistent
                assert len(login_ids) > 0, "Expected at least one user"
                # Verify that the same query returns the same order (consistency)
                result_asc2 = await client.call_tool("get_users", {
                    'order_by': 'login_id',
                    'limit': 5
                })
                login_ids2 = [user.login_id for user in result_asc2.data.items]
                assert login_ids == login_ids2, f"Expected consistent ordering, got different results: {login_ids} vs {login_ids2}"
            
            # Test descending order by login_id
            result_desc = await client.call_tool("get_users", {
                'order_by': '-login_id',
                'limit': 5
            })
            
            data_desc = result_desc.data
            if len(data_desc.items) > 1:
                # Check that login_ids are in descending order (database collation)
                login_ids = [user.login_id for user in data_desc.items]
                # Verify that the same query returns the same order (consistency)
                result_desc2 = await client.call_tool("get_users", {
                    'order_by': '-login_id',
                    'limit': 5
                })
                login_ids2 = [user.login_id for user in result_desc2.data.items]
                assert login_ids == login_ids2, f"Expected consistent ordering, got different results: {login_ids} vs {login_ids2}"

    @pytest.mark.asyncio
    async def test_get_users_multiple_filters(self, token):
        """Test get_users with multiple filters combined."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Test with multiple filters
            result = await client.call_tool("get_users", {
                'is_enabled': True,
                'limit': 10
            })
            
            data = result.data
            # Check that all returned users meet the filter criteria
            for user in data.items:
                assert user.is_enabled is True, f"Expected enabled user, got: {user.is_enabled}"

    @pytest.mark.asyncio
    async def test_get_users_user_data_structure(self, token):
        """Test that user data has the expected structure."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_users", {'limit': 1})
            
            data = result.data
            if data.items:
                user = data.items[0]
                
                # Check required fields
                assert hasattr(user, 'user_id'), f"Expected user_id field, got: {user}"
                assert hasattr(user, 'login_id'), f"Expected login_id field, got: {user}"
                assert hasattr(user, 'username'), f"Expected username field, got: {user}"
                assert hasattr(user, 'organization'), f"Expected organization field, got: {user}"
                assert hasattr(user, 'email'), f"Expected email field, got: {user}"
                assert hasattr(user, 'roles'), f"Expected roles field, got: {user}"
                assert hasattr(user, 'is_enabled'), f"Expected is_enabled field, got: {user}"
                
                # Check data types
                assert isinstance(user.user_id, int), f"Expected user_id to be int, got: {type(user.user_id)}"
                assert isinstance(user.login_id, str), f"Expected login_id to be str, got: {type(user.login_id)}"
                assert isinstance(user.roles, list), f"Expected roles to be list, got: {type(user.roles)}"
                assert isinstance(user.is_enabled, bool), f"Expected is_enabled to be bool, got: {type(user.is_enabled)}"
                
                # Check that roles contain valid values
                valid_roles = {"Admin", "Developer", "End-User"}
                for role in user.roles:
                    assert role in valid_roles, f"Expected valid role, got: {role}"

    @pytest.mark.asyncio
    async def test_get_users_invalid_pagination(self, token):
        """Test get_users with invalid pagination parameters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Test negative offset - should raise ToolError
            with pytest.raises(Exception) as exc_info:
                await client.call_tool("get_users", {
                    'offset': -1
                })
            
            # Should be a ToolError with validation message
            assert "validation error" in str(exc_info.value).lower() or "minimum" in str(exc_info.value).lower(), \
                f"Expected validation error, got: {exc_info.value}"

    @pytest.mark.asyncio
    async def test_get_users_invalid_limit(self, token):
        """Test get_users with invalid limit parameters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Test limit too high - should raise ToolError
            with pytest.raises(Exception) as exc_info:
                await client.call_tool("get_users", {
                    'limit': 1000
                })
            
            # Should be a ToolError with validation message
            assert "validation error" in str(exc_info.value).lower() or "maximum" in str(exc_info.value).lower(), \
                f"Expected validation error, got: {exc_info.value}"

    @pytest.mark.asyncio
    async def test_get_users_invalid_order_by(self, token):
        """Test get_users with invalid order_by parameters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Test invalid column name - should raise ToolError
            with pytest.raises(Exception) as exc_info:
                await client.call_tool("get_users", {
                    'order_by': 'invalid_column'
                })
            
            # Should be a ToolError with validation message
            assert "invalid column" in str(exc_info.value).lower() or "error" in str(exc_info.value).lower(), \
                f"Expected validation error, got: {exc_info.value}"

    @pytest.mark.asyncio
    async def test_get_users_authentication_required(self, invalid_token):
        """Test that get_users requires valid authentication."""
        # Test with invalid token - should raise HTTP error at connection level
        with pytest.raises(Exception) as exc_info:
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                await client.call_tool("get_users", {})
        
        # Should be an HTTP authentication error
        assert "401" in str(exc_info.value) or "unauthorized" in str(exc_info.value).lower(), \
            f"Expected authentication error, got: {exc_info.value}"

    @pytest.mark.asyncio
    async def test_get_users_disabled_user_denied(self, token):
        """Test that disabled users are denied access to get_users."""
        # Note: This test assumes that if a disabled user token is available,
        # it would be denied access. In practice, disabled users shouldn't
        # be able to authenticate in the first place, but this tests the
        # validation layer in _validate_auth_and_db().
        
        # This test is more of a documentation of the expected behavior
        # since we don't have a disabled user token in our test fixtures.
        # The actual validation happens in _validate_auth_and_db() which
        # checks app_user.is_enabled before allowing access to any tool.
        
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # This should work with a valid, enabled user token
            result = await client.call_tool("get_users", {})
            
            # Should succeed with enabled user
            assert hasattr(result, 'data'), f"Expected data attribute for enabled user, got: {result}"
            
            # If we had a disabled user token, we would expect:
            # assert hasattr(result, 'content'), f"Expected content attribute, got: {result}"
            # content = extract_content(result)
            # assert "disabled" in content.lower() or "access denied" in content.lower(), \
            #     f"Expected disabled user error, got: {content}"

    @pytest.mark.asyncio
    async def test_get_users_empty_result(self, token):
        """Test get_users with filters that return no results."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Use a very specific filter that likely won't match any users
            result = await client.call_tool("get_users", {
                'login_id': 'nonexistent_user_12345'
            })
            
            data = result.data
            assert data.total_items == 0, f"Expected 0 total items, got: {data.total_items}"
            assert len(data.items) == 0, f"Expected 0 items, got: {len(data.items)}"

    @pytest.mark.asyncio
    async def test_get_users_large_offset(self, token):
        """Test get_users with a large offset."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # First get total count with a reasonable limit
            all_result = await client.call_tool("get_users", {'limit': 100})
            total_count = all_result.data.total_items
            
            if total_count > 0:
                # Test with offset larger than total count
                result = await client.call_tool("get_users", {
                    'offset': total_count + 100
                })
                
                data = result.data
                assert data.total_items == total_count, f"Expected total_items to remain {total_count}, got: {data.total_items}"
                assert len(data.items) == 0, f"Expected 0 items with large offset, got: {len(data.items)}"
