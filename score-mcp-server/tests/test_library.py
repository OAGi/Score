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
def created_library_id(token):
    """Get an existing library ID for testing."""
    # Since we only have read-only tools, we'll use a hardcoded ID
    # In a real scenario, you would create a library first or use an existing one
    return 1


class TestGetLibrary:
    """Test cases for get_library tool."""

    @pytest.mark.asyncio
    async def test_get_library_success(self, token, created_library_id):
        """Test successful library retrieval."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_library", {
                'library_id': created_library_id
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'library_id')
            assert result.data.library_id == created_library_id
            assert hasattr(result.data, 'name')
            assert hasattr(result.data, 'type')
            assert hasattr(result.data, 'organization')
            assert hasattr(result.data, 'description')
            assert hasattr(result.data, 'link')
            assert hasattr(result.data, 'domain')
            assert hasattr(result.data, 'state')
            assert hasattr(result.data, 'is_read_only')
            assert hasattr(result.data, 'is_default')
            assert hasattr(result.data, 'created')
            assert hasattr(result.data, 'last_updated')

    @pytest.mark.asyncio
    async def test_get_library_not_found(self, token):
        """Test library retrieval with non-existent ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_library", {
                    'library_id': 99999
                })

    @pytest.mark.asyncio
    async def test_get_library_invalid_id(self, token):
        """Test library retrieval with invalid ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_library", {
                    'library_id': -1
                })

    @pytest.mark.asyncio
    async def test_get_library_error_authentication(self, invalid_token):
        """Test library retrieval with invalid authentication."""
        # Test that invalid authentication raises an exception
        with pytest.raises(Exception):
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                await client.call_tool("get_library", {
                    'library_id': 1
                })


class TestGetLibraries:
    """Test cases for get_libraries tool."""

    @pytest.mark.asyncio
    async def test_get_libraries_success(self, token):
        """Test successful libraries retrieval."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_libraries", {
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
    async def test_get_libraries_with_filters(self, token):
        """Test libraries retrieval with filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_libraries", {
                'offset': 0,
                'limit': 5,
                'name': 'test',
                'type': 'Test',
                'organization': 'Test Organization',
                'domain': 'Testing',
                'state': 'Active'
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
    async def test_get_libraries_pagination(self, token):
        """Test libraries retrieval with pagination."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Test first page
            result1 = await client.call_tool("get_libraries", {
                'offset': 0,
                'limit': 2
            })
            
            # Test second page
            result2 = await client.call_tool("get_libraries", {
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
    async def test_get_libraries_date_filters(self, token):
        """Test libraries retrieval with date filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_libraries", {
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
    async def test_get_libraries_ordering(self, token):
        """Test libraries retrieval with ordering."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_libraries", {
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
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_libraries_invalid_pagination(self, token):
        """Test libraries retrieval with invalid pagination parameters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_libraries", {
                    'offset': -1,
                    'limit': 10
                })

    @pytest.mark.asyncio
    async def test_get_libraries_invalid_limit(self, token):
        """Test libraries retrieval with invalid limit."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_libraries", {
                    'offset': 0,
                    'limit': -1
                })

    @pytest.mark.asyncio
    async def test_get_libraries_error_authentication(self, invalid_token):
        """Test libraries retrieval with invalid authentication."""
        # Test that invalid authentication raises an exception
        with pytest.raises(Exception):
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                await client.call_tool("get_libraries", {
                    'offset': 0,
                    'limit': 10
                })


class TestLibraryIntegration:
    """Integration tests for library tools."""

    @pytest.mark.asyncio
    async def test_library_workflow(self, token):
        """Test library read operations workflow."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get libraries list
            list_result = await client.call_tool("get_libraries", {
                'offset': 0,
                'limit': 10
            })
            
            assert hasattr(list_result, 'data')
            assert hasattr(list_result.data, 'items')
            assert len(list_result.data.items) >= 0
            
            # If we have libraries, test getting a specific one
            if len(list_result.data.items) > 0:
                library_id = list_result.data.items[0].library_id
                
                # Get the specific library
                get_result = await client.call_tool("get_library", {
                    'library_id': library_id
                })
                
                assert hasattr(get_result, 'data')
                assert get_result.data.library_id == library_id

    @pytest.mark.asyncio
    async def test_library_filtering_combinations(self, token):
        """Test various combinations of library filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Test with multiple filters
            result = await client.call_tool("get_libraries", {
                'offset': 0,
                'limit': 5,
                'name': 'test',
                'type': 'Test',
                'organization': 'Test Organization',
                'domain': 'Testing',
                'state': 'Active',
                'created_on': '[2024-01-01~2025-12-31]',
                'last_updated_on': '[2024-01-01~2025-12-31]',
                'order_by': '-creation_timestamp,+name'
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
    async def test_library_edge_cases(self, token):
        """Test edge cases for library operations."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Test with maximum allowed limit
            result = await client.call_tool("get_libraries", {
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
            result2 = await client.call_tool("get_libraries", {
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
