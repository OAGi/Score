import asyncio
import json
from datetime import datetime

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


@pytest.fixture
def created_release_id(token):
    """Get an existing release ID for testing."""
    # Since we only have read-only tools, we'll use a hardcoded ID
    # In a real scenario, you would create a release first or use an existing one
    return 1


@pytest.fixture
def created_code_list_manifest_id(token):
    """Get an existing code list manifest ID for testing."""
    # Since we only have read-only tools, we'll use a hardcoded ID
    # In a real scenario, you would create a code list manifest first or use an existing one
    return 1


class TestGetCodeList:
    """Test cases for get_code_list tool."""

    @pytest.mark.asyncio
    async def test_get_code_list_success(self, token, created_release_id):
        """Test successful code list retrieval."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # First get a list of code lists to find a valid manifest ID
            list_result = await client.call_tool("get_code_lists", {
                'release_id': created_release_id,
                'offset': 0,
                'limit': 1
            })
            
            # Skip test if no code lists exist
            if not list_result.data.items:
                pytest.skip("No code lists found in the database")
            
            # Use the first code list's manifest ID
            code_list_manifest_id = list_result.data.items[0].code_list_manifest_id
            
            result = await client.call_tool("get_code_list", {
                'code_list_manifest_id': code_list_manifest_id
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'code_list_manifest_id')
            assert result.data.code_list_manifest_id == code_list_manifest_id
            assert hasattr(result.data, 'code_list_id')
            assert hasattr(result.data, 'guid')
            assert hasattr(result.data, 'enum_type_guid')
            assert hasattr(result.data, 'name')
            assert hasattr(result.data, 'list_id')
            assert hasattr(result.data, 'version_id')
            assert hasattr(result.data, 'definition')
            assert hasattr(result.data, 'remark')
            assert hasattr(result.data, 'definition_source')
            assert hasattr(result.data, 'namespace')
            assert hasattr(result.data, 'library')
            assert hasattr(result.data, 'extensible_indicator')
            assert hasattr(result.data, 'is_deprecated')
            assert hasattr(result.data, 'state')
            assert hasattr(result.data, 'release')
            assert hasattr(result.data, 'log')
            assert hasattr(result.data, 'owner')
            assert hasattr(result.data, 'values')
            assert hasattr(result.data, 'created')
            assert hasattr(result.data, 'last_updated')
            
            # Verify owner object structure
            assert hasattr(result.data.owner, 'user_id')
            assert hasattr(result.data.owner, 'login_id')
            assert hasattr(result.data.owner, 'username')
            assert hasattr(result.data.owner, 'roles')
            
            # Verify created object structure
            # The created object should exist and have the expected structure
            # Note: If the creator or creation_timestamp are None in the database,
            # the object may be empty but should still have the expected attributes
            if hasattr(result.data.created, 'who') and hasattr(result.data.created, 'when'):
                # If the object has the expected attributes, verify they are the right type
                if result.data.created.who is not None:
                    assert hasattr(result.data.created.who, 'user_id')
                    assert hasattr(result.data.created.who, 'login_id')
                    assert hasattr(result.data.created.who, 'username')
                    assert hasattr(result.data.created.who, 'roles')
                # when can be None or a datetime string (ISO format)
                assert result.data.created.when is not None and isinstance(result.data.created.when, datetime)
            
            # Verify last_updated object structure
            # Similar to created object, it should exist and have the expected structure
            if hasattr(result.data.last_updated, 'who') and hasattr(result.data.last_updated, 'when'):
                # If the object has the expected attributes, verify they are the right type
                if result.data.last_updated.who is not None:
                    assert hasattr(result.data.last_updated.who, 'user_id')
                    assert hasattr(result.data.last_updated.who, 'login_id')
                    assert hasattr(result.data.last_updated.who, 'username')
                    assert hasattr(result.data.last_updated.who, 'roles')
                # when can be None or a datetime string (ISO format)
                assert result.data.last_updated.when is not None and isinstance(result.data.last_updated.when, datetime)
            
            # Verify values is a list
            assert isinstance(result.data.values, list)
            
            # If namespace exists, verify its structure
            if result.data.namespace:
                assert hasattr(result.data.namespace, 'namespace_id')
                assert hasattr(result.data.namespace, 'prefix')
                assert hasattr(result.data.namespace, 'uri')
            
            # Verify release object structure
            assert hasattr(result.data.release, 'release_id')
            assert hasattr(result.data.release, 'release_num')
            assert hasattr(result.data.release, 'state')
            
            # Verify log object structure if log exists
            if result.data.log:
                assert hasattr(result.data.log, 'log_id')
                assert hasattr(result.data.log, 'revision_num')
                assert hasattr(result.data.log, 'revision_tracking_num')
            
            # Verify values structure if values exist
            if result.data.values:
                for value in result.data.values:
                    assert hasattr(value, 'code_list_value_manifest_id')
                    assert hasattr(value, 'code_list_value_id')
                    assert hasattr(value, 'guid')
                    assert hasattr(value, 'value')
                    assert hasattr(value, 'meaning')
                    assert hasattr(value, 'definition')
                    assert hasattr(value, 'is_deprecated')

    @pytest.mark.asyncio
    async def test_get_code_list_not_found(self, token):
        """Test code list retrieval with non-existent ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_code_list", {
                    'code_list_manifest_id': 99999
                })

    @pytest.mark.asyncio
    async def test_get_code_list_invalid_id(self, token):
        """Test code list retrieval with invalid ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_code_list", {
                    'code_list_manifest_id': -1
                })


class TestGetCodeLists:
    """Test cases for get_code_lists tool."""

    @pytest.mark.asyncio
    async def test_get_code_lists_success(self, token, created_release_id):
        """Test successful code lists retrieval."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_code_lists", {
                'release_id': created_release_id,
                'offset': 0,
                'limit': 10
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            
            # Verify pagination values
            assert result.data.offset == 0
            assert result.data.limit == 10
            assert isinstance(result.data.total_items, int)
            assert result.data.total_items >= 0
            
            # Verify items is a list
            assert isinstance(result.data.items, list)
            
            # If there are items, verify their structure
            if result.data.items:
                item = result.data.items[0]
                assert hasattr(item, 'code_list_id')
                assert hasattr(item, 'guid')
                assert hasattr(item, 'name')
                assert hasattr(item, 'list_id')
                assert hasattr(item, 'version_id')
                assert hasattr(item, 'definition')
                assert hasattr(item, 'library')
                assert hasattr(item, 'owner')
                assert hasattr(item, 'created')
                assert hasattr(item, 'last_updated')

    @pytest.mark.asyncio
    async def test_get_code_lists_with_filters(self, token, created_release_id):
        """Test code lists retrieval with filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_code_lists", {
                'release_id': created_release_id,
                'offset': 0,
                'limit': 5,
                'name': 'test',
                'list_id': 'test',
                'version_id': '1.0'
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            
            # Verify pagination values
            assert result.data.offset == 0
            assert result.data.limit == 5

    @pytest.mark.asyncio
    async def test_get_code_lists_with_date_filters(self, token, created_release_id):
        """Test code lists retrieval with date range filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_code_lists", {
                'release_id': created_release_id,
                'offset': 0,
                'limit': 10,
                'created_on': '[2020-01-01~2025-12-31]',
                'last_updated_on': '[2020-01-01~2025-12-31]'
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')

    @pytest.mark.asyncio
    async def test_get_code_lists_with_ordering(self, token, created_release_id):
        """Test code lists retrieval with ordering."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_code_lists", {
                'release_id': created_release_id,
                'offset': 0,
                'limit': 10,
                'order_by': '-creation_timestamp,+name'
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')

    @pytest.mark.asyncio
    async def test_get_code_lists_invalid_pagination(self, token, created_release_id):
        """Test code lists retrieval with invalid pagination parameters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_code_lists", {
                    'release_id': created_release_id,
                    'offset': -1,
                    'limit': 10
                })

    @pytest.mark.asyncio
    async def test_get_code_lists_invalid_limit(self, token, created_release_id):
        """Test code lists retrieval with invalid limit."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_code_lists", {
                    'release_id': created_release_id,
                    'offset': 0,
                    'limit': 0
                })

    @pytest.mark.asyncio
    async def test_get_code_lists_invalid_date_range(self, token, created_release_id):
        """Test code lists retrieval with invalid date range format."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_code_lists", {
                    'release_id': created_release_id,
                    'offset': 0,
                    'limit': 10,
                    'created_on': 'invalid-date-format'
                })

    @pytest.mark.asyncio
    async def test_get_code_lists_invalid_order_by(self, token, created_release_id):
        """Test code lists retrieval with invalid order_by format."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_code_lists", {
                    'release_id': created_release_id,
                    'offset': 0,
                    'limit': 10,
                    'order_by': 'invalid_column'
                })

    @pytest.mark.asyncio
    async def test_get_code_lists_missing_release_id(self, token):
        """Test code lists retrieval without required release_id."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_code_lists", {
                    'offset': 0,
                    'limit': 10
                })

    @pytest.mark.asyncio
    async def test_get_code_lists_nonexistent_release(self, token):
        """Test code lists retrieval with non-existent release ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_code_lists", {
                'release_id': 99999,
                'offset': 0,
                'limit': 10
            })
            
            # Should return empty results or error
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'items')
            # Should have 0 items for non-existent release
            assert result.data.total_items == 0
            assert len(result.data.items) == 0


class TestCodeListAuthentication:
    """Test cases for authentication and authorization."""

    @pytest.mark.asyncio
    async def test_get_code_list_without_auth(self):
        """Test code list retrieval without authentication."""
        with pytest.raises(Exception):  # Expect HTTP connection to fail
            async with Client("http://localhost:8000/mcp") as client:
                await client.call_tool("get_code_list", {
                    'code_list_manifest_id': 1
                })

    @pytest.mark.asyncio
    async def test_get_code_lists_without_auth(self):
        """Test code lists retrieval without authentication."""
        with pytest.raises(Exception):  # Expect HTTP connection to fail
            async with Client("http://localhost:8000/mcp") as client:
                await client.call_tool("get_code_lists", {
                    'release_id': 1,
                    'offset': 0,
                    'limit': 10
                })

    @pytest.mark.asyncio
    async def test_get_code_list_with_invalid_token(self, invalid_token):
        """Test code list retrieval with invalid token."""
        with pytest.raises(Exception):  # Expect HTTP connection to fail
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                await client.call_tool("get_code_list", {
                    'code_list_manifest_id': 1
                })

    @pytest.mark.asyncio
    async def test_get_code_lists_with_invalid_token(self, invalid_token):
        """Test code lists retrieval with invalid token."""
        with pytest.raises(Exception):  # Expect HTTP connection to fail
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                await client.call_tool("get_code_lists", {
                    'release_id': 1,
                    'offset': 0,
                    'limit': 10
                })


class TestCodeListEdgeCases:
    """Test cases for edge cases and boundary conditions."""

    @pytest.mark.asyncio
    async def test_get_code_lists_max_limit(self, token, created_release_id):
        """Test code lists retrieval with maximum limit."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_code_lists", {
                'release_id': created_release_id,
                'offset': 0,
                'limit': 100  # Maximum allowed limit
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert result.data.limit == 100

    @pytest.mark.asyncio
    async def test_get_code_lists_large_offset(self, token, created_release_id):
        """Test code lists retrieval with large offset."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_code_lists", {
                'release_id': created_release_id,
                'offset': 1000,
                'limit': 10
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert result.data.offset == 1000
            # Should have 0 items if offset is beyond available data
            assert len(result.data.items) == 0

    @pytest.mark.asyncio
    async def test_get_code_lists_empty_filters(self, token, created_release_id):
        """Test code lists retrieval with empty filter values."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_code_lists", {
                'release_id': created_release_id,
                'offset': 0,
                'limit': 10,
                'name': '',
                'list_id': '',
                'version_id': ''
            })
            
            # Should work fine with empty strings (treated as None)
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'items')

    @pytest.mark.asyncio
    async def test_get_code_lists_special_characters_in_filters(self, token, created_release_id):
        """Test code lists retrieval with special characters in filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_code_lists", {
                'release_id': created_release_id,
                'offset': 0,
                'limit': 10,
                'name': 'test@#$%^&*()',
                'list_id': 'test-123_456',
                'version_id': 'v1.0.0-beta'
            })
            
            # Should handle special characters gracefully
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'items')
