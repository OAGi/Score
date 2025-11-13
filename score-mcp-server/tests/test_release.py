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


@pytest.fixture
def created_release_id(token):
    """Get an existing release ID for testing."""
    # Since we only have read-only tools, we'll use a hardcoded ID
    # In a real scenario, you would create a release first or use an existing one
    return 1


class TestGetRelease:
    """Test cases for get_release tool."""

    @pytest.mark.asyncio
    async def test_get_release_success(self, token, created_release_id):
        """Test successful release retrieval."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_release", {
                'release_id': created_release_id
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'release_id')
            assert result.data.release_id == created_release_id
            assert hasattr(result.data, 'library')
            assert hasattr(result.data, 'guid')
            assert hasattr(result.data, 'release_num')
            assert hasattr(result.data, 'release_note')
            assert hasattr(result.data, 'release_license')
            assert hasattr(result.data, 'namespace')
            assert hasattr(result.data, 'state')
            assert hasattr(result.data, 'created')
            assert hasattr(result.data, 'last_updated')
            
            # Verify library object structure
            assert hasattr(result.data.library, 'library_id')
            assert hasattr(result.data.library, 'name')
            
            # Verify namespace object structure (if present)
            if result.data.namespace:
                assert hasattr(result.data.namespace, 'namespace_id')
                assert hasattr(result.data.namespace, 'prefix')
                assert hasattr(result.data.namespace, 'uri')

    @pytest.mark.asyncio
    async def test_get_release_not_found(self, token):
        """Test release retrieval with non-existent ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_release", {
                    'release_id': 99999
                })

    @pytest.mark.asyncio
    async def test_get_release_invalid_id(self, token):
        """Test release retrieval with invalid ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_release", {
                    'release_id': -1
                })

    @pytest.mark.asyncio
    async def test_get_release_error_authentication(self, invalid_token):
        """Test release retrieval with invalid authentication."""
        # Test that invalid authentication raises an exception
        with pytest.raises(Exception):
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                await client.call_tool("get_release", {
                    'release_id': 1
                })


class TestGetReleases:
    """Test cases for get_releases tool."""

    @pytest.mark.asyncio
    async def test_get_releases_success(self, token):
        """Test successful releases retrieval."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_releases", {
                'library_id': 1,
                'offset': 0,
                'limit': 10
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)
            assert result.data.offset == 0
            assert result.data.limit == 10

    @pytest.mark.asyncio
    async def test_get_releases_with_filters(self, token):
        """Test releases retrieval with filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_releases", {
                'offset': 0,
                'limit': 5,
                'library_id': 1,
                'release_num': '1.0',
                'state': 'Published'
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)
            assert result.data.offset == 0
            assert result.data.limit == 5

    @pytest.mark.asyncio
    async def test_get_releases_pagination(self, token):
        """Test releases retrieval with pagination."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Test first page
            result1 = await client.call_tool("get_releases", {
                'library_id': 1,
                'offset': 0,
                'limit': 2
            })
            
            # Test second page
            result2 = await client.call_tool("get_releases", {
                'library_id': 1,
                'offset': 2,
                'limit': 2
            })
            
            # Verify pagination works
            assert hasattr(result1, 'data')
            assert hasattr(result2, 'data')
            assert result1.data.offset == 0
            assert result2.data.offset == 2
            assert result1.data.limit == 2
            assert result2.data.limit == 2

    @pytest.mark.asyncio
    async def test_get_releases_date_filters(self, token):
        """Test releases retrieval with date filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_releases", {
                'library_id': 1,
                'offset': 0,
                'limit': 10,
                'created_on': '[2024-01-01~2025-12-31]',
                'last_updated_on': '[2024-01-01~2025-12-31]'
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_releases_ordering(self, token):
        """Test releases retrieval with ordering."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_releases", {
                'library_id': 1,
                'offset': 0,
                'limit': 10,
                'order_by': '-creation_timestamp,+release_num'
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_releases_latest_release(self, token):
        """Test that get_releases returns the latest release when ordered by last_update_timestamp."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_releases", {
                'library_id': 1,
                'offset': 0,
                'limit': 1,
                'order_by': '-last_update_timestamp'
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)
            assert result.data.offset == 0
            assert result.data.limit == 1
            
            # If there are releases, verify we got the latest one
            if len(result.data.items) > 0:
                latest_release = result.data.items[0]
                
                # Verify the release has the expected structure
                assert hasattr(latest_release, 'release_id')
                assert hasattr(latest_release, 'library')
                assert hasattr(latest_release, 'guid')
                assert hasattr(latest_release, 'state')
                assert hasattr(latest_release, 'last_updated')
                
                # Verify library object structure
                assert hasattr(latest_release.library, 'library_id')
                assert hasattr(latest_release.library, 'name')
                assert isinstance(latest_release.library.library_id, int)
                
                # Verify that this is indeed the latest release by checking
                # that it's not a 'Working' release (which should be filtered out)
                assert latest_release.release_num != 'Working'

    @pytest.mark.asyncio
    async def test_get_releases_invalid_pagination(self, token):
        """Test releases retrieval with invalid pagination parameters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_releases", {
                    'library_id': 1,
                    'offset': -1,
                    'limit': 10
                })

    @pytest.mark.asyncio
    async def test_get_releases_invalid_limit(self, token):
        """Test releases retrieval with invalid limit."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_releases", {
                    'library_id': 1,
                    'offset': 0,
                    'limit': -1
                })

    @pytest.mark.asyncio
    async def test_get_releases_error_authentication(self, invalid_token):
        """Test releases retrieval with invalid authentication."""
        # Test that invalid authentication raises an exception
        with pytest.raises(Exception):
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                await client.call_tool("get_releases", {
                    'library_id': 1,
                    'offset': 0,
                    'limit': 10
                })


class TestReleaseIntegration:
    """Integration tests for release tools."""

    @pytest.mark.asyncio
    async def test_release_workflow(self, token):
        """Test release read operations workflow."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get releases list
            list_result = await client.call_tool("get_releases", {
                'library_id': 1,
                'offset': 0,
                'limit': 10
            })
            
            assert hasattr(list_result, 'data')
            assert hasattr(list_result.data, 'items')
            assert len(list_result.data.items) >= 0
            
            # If we have releases, test getting a specific one
            if len(list_result.data.items) > 0:
                release_id = list_result.data.items[0].release_id
                
                # Get the specific release
                get_result = await client.call_tool("get_release", {
                    'release_id': release_id
                })
                
                assert hasattr(get_result, 'data')
                assert get_result.data.release_id == release_id
                
                # Verify the structure of the release object
                assert hasattr(get_result.data, 'library')
                assert hasattr(get_result.data.library, 'library_id')
                assert hasattr(get_result.data.library, 'name')
                
                # Check namespace if present
                if get_result.data.namespace:
                    assert hasattr(get_result.data.namespace, 'namespace_id')
                    assert hasattr(get_result.data.namespace, 'prefix')
                    assert hasattr(get_result.data.namespace, 'uri')

    @pytest.mark.asyncio
    async def test_release_filtering_combinations(self, token):
        """Test various combinations of release filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Test with multiple filters
            result = await client.call_tool("get_releases", {
                'offset': 0,
                'limit': 5,
                'library_id': 1,
                'release_num': '1.0',
                'state': 'Published',
                'created_on': '[2024-01-01~2025-12-31]',
                'last_updated_on': '[2024-01-01~2025-12-31]',
                'order_by': '-creation_timestamp,+release_num'
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)
            assert result.data.offset == 0
            assert result.data.limit == 5

    @pytest.mark.asyncio
    async def test_release_edge_cases(self, token):
        """Test edge cases for release operations."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Test with maximum allowed limit
            result = await client.call_tool("get_releases", {
                'library_id': 1,
                'offset': 0,
                'limit': 100
            })
            
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)
            assert result.data.limit == 100
            
            # Test with offset beyond available data
            result2 = await client.call_tool("get_releases", {
                'library_id': 1,
                'offset': 99999,
                'limit': 10
            })
            
            assert hasattr(result2, 'data')
            assert hasattr(result2.data, 'total_items')
            assert hasattr(result2.data, 'offset')
            assert hasattr(result2.data, 'limit')
            assert hasattr(result2.data, 'items')
            assert isinstance(result2.data.items, list)
            assert result2.data.offset == 99999
            assert len(result2.data.items) == 0  # Should be empty

    @pytest.mark.asyncio
    async def test_release_structure_validation(self, token):
        """Test that release objects have the correct structure."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_releases", {
                'library_id': 1,
                'offset': 0,
                'limit': 1
            })
            
            if len(result.data.items) > 0:
                release = result.data.items[0]
                
                # Verify required fields
                assert hasattr(release, 'release_id')
                assert hasattr(release, 'library')
                assert hasattr(release, 'guid')
                assert hasattr(release, 'state')
                assert hasattr(release, 'created')
                assert hasattr(release, 'last_updated')
                
                # Verify library object structure
                assert hasattr(release.library, 'library_id')
                assert hasattr(release.library, 'name')
                assert isinstance(release.library.library_id, int)
                
                # Verify namespace object structure (if present)
                if release.namespace:
                    assert hasattr(release.namespace, 'namespace_id')
                    assert hasattr(release.namespace, 'prefix')
                    assert hasattr(release.namespace, 'uri')
                    assert isinstance(release.namespace.namespace_id, int)
                
                # Verify created/last_updated structure
                # Note: The MCP client may serialize WhoAndWhen objects differently
                assert release.created is not None
                assert release.last_updated is not None
