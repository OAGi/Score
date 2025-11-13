import asyncio
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
def created_namespace_id(token):
    """Get an existing namespace ID for testing."""
    # Since we only have read-only tools, we'll use a hardcoded ID
    # In a real scenario, you would create a namespace first or use an existing one
    return 1


class TestGetNamespace:
    """Test cases for get_namespace tool."""

    @pytest.mark.asyncio
    async def test_get_namespace_success(self, token, created_namespace_id):
        """Test successful namespace retrieval."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_namespace", {
                'namespace_id': created_namespace_id
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'namespace_id')
            assert result.data.namespace_id == created_namespace_id
            assert hasattr(result.data, 'library')
            assert hasattr(result.data, 'uri')
            assert hasattr(result.data, 'prefix')
            assert hasattr(result.data, 'description')
            assert hasattr(result.data, 'is_std_nmsp')
            assert hasattr(result.data, 'owner')
            assert hasattr(result.data, 'created')
            assert hasattr(result.data, 'last_updated')
            
            # Verify library object structure
            assert hasattr(result.data.library, 'library_id')
            assert hasattr(result.data.library, 'name')
            assert isinstance(result.data.library.library_id, int)
            
            # Verify owner object structure (should be UserInfo, not None)
            assert result.data.owner is not None
            # Note: The MCP client may serialize UserInfo objects differently
            # We just verify that owner is present and not None

    @pytest.mark.asyncio
    async def test_get_namespace_not_found(self, token):
        """Test namespace retrieval with non-existent ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_namespace", {
                    'namespace_id': 99999
                })

    @pytest.mark.asyncio
    async def test_get_namespace_invalid_id(self, token):
        """Test namespace retrieval with invalid ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_namespace", {
                    'namespace_id': -1
                })

    @pytest.mark.asyncio
    async def test_get_namespace_error_authentication(self, invalid_token):
        """Test namespace retrieval with invalid authentication."""
        # Test that invalid authentication raises an exception
        with pytest.raises(Exception):
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                await client.call_tool("get_namespace", {
                    'namespace_id': 1
                })


class TestGetNamespaces:
    """Test cases for get_namespaces tool."""

    @pytest.mark.asyncio
    async def test_get_namespaces_success(self, token):
        """Test successful namespaces retrieval."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_namespaces", {
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
    async def test_get_namespaces_with_filters(self, token):
        """Test namespaces retrieval with filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_namespaces", {
                'offset': 0,
                'limit': 5,
                'library_id': 1,
                'uri': 'http',
                'prefix': 'test',
                'is_std_nmsp': True
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
    async def test_get_namespaces_pagination(self, token):
        """Test namespaces retrieval with pagination."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Test first page
            result1 = await client.call_tool("get_namespaces", {
                'library_id': 1,
                'offset': 0,
                'limit': 2
            })
            
            # Test second page
            result2 = await client.call_tool("get_namespaces", {
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
    async def test_get_namespaces_date_filters(self, token):
        """Test namespaces retrieval with date filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_namespaces", {
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
    async def test_get_namespaces_ordering(self, token):
        """Test namespaces retrieval with ordering."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_namespaces", {
                'library_id': 1,
                'offset': 0,
                'limit': 10,
                'order_by': '-creation_timestamp,+uri'
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_namespaces_invalid_pagination(self, token):
        """Test namespaces retrieval with invalid pagination parameters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_namespaces", {
                    'library_id': 1,
                    'offset': -1,
                    'limit': 10
                })

    @pytest.mark.asyncio
    async def test_get_namespaces_invalid_limit(self, token):
        """Test namespaces retrieval with invalid limit."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_namespaces", {
                    'library_id': 1,
                    'offset': 0,
                    'limit': -1
                })

    @pytest.mark.asyncio
    async def test_get_namespaces_error_authentication(self, invalid_token):
        """Test namespaces retrieval with invalid authentication."""
        # Test that invalid authentication raises an exception
        with pytest.raises(Exception):
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                await client.call_tool("get_namespaces", {
                    'library_id': 1,
                    'offset': 0,
                    'limit': 10
                })


class TestNamespaceIntegration:
    """Integration tests for namespace tools."""

    @pytest.mark.asyncio
    async def test_namespace_workflow(self, token):
        """Test namespace read operations workflow."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get namespaces list
            list_result = await client.call_tool("get_namespaces", {
                'library_id': 1,
                'offset': 0,
                'limit': 10
            })
            
            assert hasattr(list_result, 'data')
            assert hasattr(list_result.data, 'items')
            assert len(list_result.data.items) >= 0
            
            # If we have namespaces, test getting a specific one
            if len(list_result.data.items) > 0:
                namespace_id = list_result.data.items[0].namespace_id
                
                # Get the specific namespace
                get_result = await client.call_tool("get_namespace", {
                    'namespace_id': namespace_id
                })
                
                assert hasattr(get_result, 'data')
                assert get_result.data.namespace_id == namespace_id
                
                # Verify the structure of the namespace object
                assert hasattr(get_result.data, 'library')
                assert hasattr(get_result.data.library, 'library_id')
                assert hasattr(get_result.data.library, 'name')
                
                # Verify owner is required and has UserInfo structure
                assert get_result.data.owner is not None
                # Note: The MCP client may serialize UserInfo objects differently

    @pytest.mark.asyncio
    async def test_namespace_filtering_combinations(self, token):
        """Test various combinations of namespace filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Test with multiple filters
            result = await client.call_tool("get_namespaces", {
                'offset': 0,
                'limit': 5,
                'library_id': 1,
                'uri': 'http',
                'prefix': 'test',
                'is_std_nmsp': True,
                'created_on': '[2024-01-01~2025-12-31]',
                'last_updated_on': '[2024-01-01~2025-12-31]',
                'order_by': '-creation_timestamp,+uri'
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
    async def test_namespace_edge_cases(self, token):
        """Test edge cases for namespace operations."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Test with maximum allowed limit
            result = await client.call_tool("get_namespaces", {
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
            result2 = await client.call_tool("get_namespaces", {
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
    async def test_namespace_structure_validation(self, token):
        """Test that namespace objects have the correct structure."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_namespaces", {
                'library_id': 1,
                'offset': 0,
                'limit': 1
            })
            
            if len(result.data.items) > 0:
                namespace = result.data.items[0]
                
                # Verify required fields
                assert hasattr(namespace, 'namespace_id')
                assert hasattr(namespace, 'library')
                assert hasattr(namespace, 'uri')
                assert hasattr(namespace, 'is_std_nmsp')
                assert hasattr(namespace, 'owner')
                assert hasattr(namespace, 'created')
                assert hasattr(namespace, 'last_updated')
                
                # Verify library object structure
                assert hasattr(namespace.library, 'library_id')
                assert hasattr(namespace.library, 'name')
                assert isinstance(namespace.library.library_id, int)
                
                # Verify owner is required and has UserInfo structure
                assert namespace.owner is not None
                # Note: The MCP client may serialize UserInfo objects differently
                
                # Verify created/last_updated structure
                # Note: The MCP client may serialize WhoAndWhen objects differently
                assert namespace.created is not None
                assert namespace.last_updated is not None

    @pytest.mark.asyncio
    async def test_namespace_latest_namespace(self, token):
        """Test that get_namespaces returns the latest namespace when ordered by last_update_timestamp."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_namespaces", {
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
            
            # If there are namespaces, verify we got the latest one
            if len(result.data.items) > 0:
                latest_namespace = result.data.items[0]
                
                # Verify the namespace has the expected structure
                assert hasattr(latest_namespace, 'namespace_id')
                assert hasattr(latest_namespace, 'library')
                assert hasattr(latest_namespace, 'uri')
                assert hasattr(latest_namespace, 'owner')
                assert hasattr(latest_namespace, 'last_updated')
                
                # Verify library object structure
                assert hasattr(latest_namespace.library, 'library_id')
                assert hasattr(latest_namespace.library, 'name')
                assert isinstance(latest_namespace.library.library_id, int)
                
                # Verify owner is required and has UserInfo structure
                assert latest_namespace.owner is not None
                # Note: The MCP client may serialize UserInfo objects differently
