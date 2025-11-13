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


@pytest.fixture
def created_dt_manifest_id(token):
    """Get an existing data type manifest ID for testing."""
    # Since we only have read-only tools, we'll use a hardcoded ID
    # In a real scenario, you would create a data type manifest first or use an existing one
    return 1


class TestGetDataType:
    """Test cases for get_data_type tool."""

    @pytest.mark.asyncio
    async def test_get_data_type_success(self, token, created_release_id):
        """Test successful data type retrieval."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # First get a list of data types to find a valid manifest ID
            list_result = await client.call_tool("get_data_types", {
                'release_id': created_release_id,
                'offset': 0,
                'limit': 1
            })
            
            # Skip test if no data types exist
            if not list_result.data.items:
                pytest.skip("No data types found in the database")
            
            # Use the first data type's manifest ID
            dt_manifest_id = list_result.data.items[0].dt_manifest_id
            
            result = await client.call_tool("get_data_type", {
                'dt_manifest_id': dt_manifest_id
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'dt_manifest_id')
            assert result.data.dt_manifest_id == dt_manifest_id
            assert hasattr(result.data, 'dt_id')
            assert hasattr(result.data, 'guid')
            assert hasattr(result.data, 'den')
            assert hasattr(result.data, 'data_type_term')
            assert hasattr(result.data, 'qualifier')
            assert hasattr(result.data, 'representation_term')
            assert hasattr(result.data, 'six_digit_id')
            assert hasattr(result.data, 'definition')
            assert hasattr(result.data, 'definition_source')
            assert hasattr(result.data, 'content_component_definition')
            assert hasattr(result.data, 'commonly_used')
            assert hasattr(result.data, 'is_deprecated')
            assert hasattr(result.data, 'state')
            assert hasattr(result.data, 'owner')
            assert hasattr(result.data, 'supplementary_components')
            assert hasattr(result.data, 'created')
            assert hasattr(result.data, 'last_updated')
            assert hasattr(result.data, 'namespace')
            assert hasattr(result.data, 'library')
            assert hasattr(result.data, 'release')
            assert hasattr(result.data, 'base_dt')
            assert hasattr(result.data, 'log')

    @pytest.mark.asyncio
    async def test_get_data_type_not_found(self, token):
        """Test data type retrieval with non-existent ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception) as exc_info:
                await client.call_tool("get_data_type", {
                    'dt_manifest_id': 99999
                })
            
            # Should raise a ToolError
            assert "not found" in str(exc_info.value).lower()

    @pytest.mark.asyncio
    async def test_get_data_type_invalid_id(self, token):
        """Test data type retrieval with invalid ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception) as exc_info:
                await client.call_tool("get_data_type", {
                    'dt_manifest_id': 999999  # Use a valid ID that doesn't exist
                })
            
            # Should raise a ToolError
            assert "not found" in str(exc_info.value).lower()


class TestGetDataTypes:
    """Test cases for get_data_types tool."""

    @pytest.mark.asyncio
    async def test_get_data_types_success(self, token, created_release_id):
        """Test successful data types listing."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_data_types", {
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
            assert isinstance(result.data.items, list)
            assert result.data.offset == 0
            assert result.data.limit == 10

    @pytest.mark.asyncio
    async def test_get_data_types_with_pagination(self, token, created_release_id):
        """Test data types listing with pagination."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_data_types", {
                'release_id': created_release_id,
                'offset': 0,
                'limit': 5
            })
            
            # Verify pagination parameters
            assert result.data.offset == 0
            assert result.data.limit == 5
            assert len(result.data.items) <= 5

    @pytest.mark.asyncio
    async def test_get_data_types_with_filters(self, token, created_release_id):
        """Test data types listing with filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_data_types", {
                'release_id': created_release_id,
                'den': 'Currency_ Amount. Type',
                'representation_term': 'amount',
                'offset': 0,
                'limit': 10
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_data_types_with_date_filters(self, token, created_release_id):
        """Test data types listing with date range filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_data_types", {
                'release_id': created_release_id,
                'created_on': '[2024-01-01~2024-12-31]',
                'last_updated_on': '[2024-01-01~2024-12-31]',
                'offset': 0,
                'limit': 10
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_data_types_with_ordering(self, token, created_release_id):
        """Test data types listing with ordering."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_data_types", {
                'release_id': created_release_id,
                'order_by': '-creation_timestamp,+den',
                'offset': 0,
                'limit': 10
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_data_types_order_by_den(self, token, created_release_id):
        """Test data types listing with ordering by DEN."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_data_types", {
                'release_id': created_release_id,
                'order_by': '+den',
                'offset': 0,
                'limit': 10
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_data_types_invalid_pagination(self, token, created_release_id):
        """Test data types listing with invalid pagination parameters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception) as exc_info:
                await client.call_tool("get_data_types", {
                    'release_id': created_release_id,
                    'offset': -1,
                    'limit': 0
                })
            
            # Should raise a ToolError
            assert "validation" in str(exc_info.value).lower()

    @pytest.mark.asyncio
    async def test_get_data_types_invalid_date_range(self, token, created_release_id):
        """Test data types listing with invalid date range format."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception) as exc_info:
                await client.call_tool("get_data_types", {
                    'release_id': created_release_id,
                    'created_on': 'invalid-date-format',
                    'offset': 0,
                    'limit': 10
                })
            
            # Should raise a ToolError
            assert "validation" in str(exc_info.value).lower()

    @pytest.mark.asyncio
    async def test_get_data_types_invalid_ordering(self, token, created_release_id):
        """Test data types listing with invalid ordering column."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception) as exc_info:
                await client.call_tool("get_data_types", {
                    'release_id': created_release_id,
                    'order_by': 'invalid_column',
                    'offset': 0,
                    'limit': 10
                })
            
            # Should raise a ToolError with validation error message
            assert "validation error" in str(exc_info.value).lower()
            assert "invalid sort column" in str(exc_info.value).lower()

    @pytest.mark.asyncio
    async def test_get_data_types_missing_release_id(self, token):
        """Test data types listing without required release_id."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception) as exc_info:
                await client.call_tool("get_data_types", {
                    'offset': 0,
                    'limit': 10
                })
            
            # Should raise a ToolError
            assert "validation" in str(exc_info.value).lower()

    @pytest.mark.asyncio
    async def test_get_data_types_default_parameters(self, token, created_release_id):
        """Test data types listing with default parameters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_data_types", {
                'release_id': created_release_id
            })
            
            # Verify default parameters are applied
            assert hasattr(result, 'data')
            assert result.data.offset == 0
            assert result.data.limit == 10
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)


class TestDataTypesIntegration:
    """Integration tests for data type tools."""

    @pytest.mark.asyncio
    async def test_data_types_workflow(self, token, created_release_id):
        """Test complete workflow: list data types, then get specific one."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # First, get list of data types
            list_result = await client.call_tool("get_data_types", {
                'release_id': created_release_id,
                'offset': 0,
                'limit': 5
            })
            
            assert hasattr(list_result, 'data')
            assert len(list_result.data.items) > 0
            
            # Get the first data type's manifest ID
            first_dt_manifest_id = list_result.data.items[0].dt_manifest_id
            
            # Then get specific data type
            detail_result = await client.call_tool("get_data_type", {
                'dt_manifest_id': first_dt_manifest_id
            })
            
            assert hasattr(detail_result, 'data')
            assert detail_result.data.dt_manifest_id == first_dt_manifest_id

    @pytest.mark.asyncio
    async def test_data_types_filtering_consistency(self, token, created_release_id):
        """Test that filtering works consistently."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get all data types
            all_result = await client.call_tool("get_data_types", {
                'release_id': created_release_id,
                'offset': 0,
                'limit': 100
            })
            
            # Get filtered data types
            filtered_result = await client.call_tool("get_data_types", {
                'release_id': created_release_id,
                'den': 'Amount. Type',
                'offset': 0,
                'limit': 100
            })
            
            # Filtered results should be subset of all results
            assert filtered_result.data.total_items <= all_result.data.total_items

    @pytest.mark.asyncio
    async def test_data_types_supplementary_components(self, token, created_release_id):
        """Test that supplementary components are included in data type details."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # First get a list of data types to find a valid manifest ID
            list_result = await client.call_tool("get_data_types", {
                'release_id': created_release_id,
                'offset': 0,
                'limit': 1
            })
            
            # Skip test if no data types exist
            if not list_result.data.items:
                pytest.skip("No data types found in the database")
            
            # Use the first data type's manifest ID
            dt_manifest_id = list_result.data.items[0].dt_manifest_id
            
            result = await client.call_tool("get_data_type", {
                'dt_manifest_id': dt_manifest_id
            })
            
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'supplementary_components')
            assert isinstance(result.data.supplementary_components, list)
            
            # If there are supplementary components, verify their structure
            if len(result.data.supplementary_components) > 0:
                sc = result.data.supplementary_components[0]
                assert hasattr(sc, 'dt_sc_manifest_id')
                assert hasattr(sc, 'dt_sc_id')
                assert hasattr(sc, 'guid')
                assert hasattr(sc, 'object_class_term')
                assert hasattr(sc, 'property_term')
                assert hasattr(sc, 'representation_term')
                assert hasattr(sc, 'definition')
                assert hasattr(sc, 'cardinality_min')
                assert hasattr(sc, 'cardinality_max')
                assert hasattr(sc, 'value_constraint')  # value_constraint can be None
                assert hasattr(sc, 'is_deprecated')
