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


class TestGetTags:
    """Test cases for get_tags tool."""

    @pytest.mark.asyncio
    async def test_get_tags_success_basic(self, token):
        """Test successful tag retrieval with basic parameters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_tags", {
                'offset': 0,
                'limit': 10
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            
            # Verify data types
            assert isinstance(result.data.total_items, int)
            assert isinstance(result.data.offset, int)
            assert isinstance(result.data.limit, int)
            assert isinstance(result.data.items, list)
            
            # Verify pagination values
            assert result.data.offset == 0
            assert result.data.limit == 10
            assert result.data.total_items >= 0
            
            # Verify items have required fields including 'created' and 'last_updated'
            if result.data.items:
                for item in result.data.items:
                    assert hasattr(item, 'tag_id')
                    assert hasattr(item, 'name')
                    assert hasattr(item, 'description')
                    assert hasattr(item, 'color')
                    assert hasattr(item, 'text_color')
                    assert hasattr(item, 'created')
                    assert hasattr(item, 'last_updated')
                    # Verify created field structure
                    if hasattr(item.created, 'who') and hasattr(item.created, 'when'):
                        # If the object has the expected attributes, verify they are the right type
                        if item.created.who is not None:
                            assert hasattr(item.created.who, 'user_id')
                            assert hasattr(item.created.who, 'login_id')
                            assert hasattr(item.created.who, 'username')
                            assert hasattr(item.created.who, 'roles')
                        # when can be None or a datetime
                        assert item.created.when is not None and isinstance(item.created.when, datetime)
                    # Verify last_updated field structure
                    if hasattr(item.last_updated, 'who') and hasattr(item.last_updated, 'when'):
                        # If the object has the expected attributes, verify they are the right type
                        if item.last_updated.who is not None:
                            assert hasattr(item.last_updated.who, 'user_id')
                            assert hasattr(item.last_updated.who, 'login_id')
                            assert hasattr(item.last_updated.who, 'username')
                            assert hasattr(item.last_updated.who, 'roles')
                        # when can be None or a datetime
                        assert item.last_updated.when is not None and isinstance(item.last_updated.when, datetime)

    @pytest.mark.asyncio
    async def test_get_tags_with_name_filter(self, token):
        """Test tag retrieval with name filtering."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_tags", {
                'offset': 0,
                'limit': 5,
                'name': 'BOD'
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'items')
            
            # If there are results, verify they contain the filter term
            if result.data.items:
                for item in result.data.items:
                    assert hasattr(item, 'name')
                    assert hasattr(item, 'tag_id')
                    assert hasattr(item, 'description')
                    # Name should contain 'BOD' (case-insensitive)
                    assert 'BOD' in item.name.upper()

    @pytest.mark.asyncio
    async def test_get_tags_with_description_filter(self, token):
        """Test tag retrieval with description filtering."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_tags", {
                'offset': 0,
                'limit': 5,
                'description': 'Business'
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'items')
            
            # If there are results, verify they contain the filter term
            if result.data.items:
                for item in result.data.items:
                    assert hasattr(item, 'name')
                    assert hasattr(item, 'tag_id')
                    assert hasattr(item, 'description')
                    # Description should contain 'Business' (case-insensitive)
                    if item.description:
                        assert 'BUSINESS' in item.description.upper()

    @pytest.mark.asyncio
    async def test_get_tags_with_date_range_filter(self, token):
        """Test tag retrieval with date range filtering."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_tags", {
                'offset': 0,
                'limit': 5,
                'created_on': '[2024-01-01~2025-12-31]'
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'items')
            
            # Verify items have required fields
            if result.data.items:
                for item in result.data.items:
                    assert hasattr(item, 'name')
                    assert hasattr(item, 'tag_id')
                    assert hasattr(item, 'description')

    @pytest.mark.asyncio
    async def test_get_tags_with_ordering(self, token):
        """Test tag retrieval with ordering."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_tags", {
                'offset': 0,
                'limit': 5,
                'order_by': '-creation_timestamp'
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'items')
            
            # Verify items have required fields
            if result.data.items:
                for item in result.data.items:
                    assert hasattr(item, 'name')
                    assert hasattr(item, 'tag_id')
                    assert hasattr(item, 'description')

    @pytest.mark.asyncio
    async def test_get_tags_pagination(self, token):
        """Test tag retrieval with pagination."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get first page
            result1 = await client.call_tool("get_tags", {
                'offset': 0,
                'limit': 3
            })
            
            # Get second page
            result2 = await client.call_tool("get_tags", {
                'offset': 3,
                'limit': 3
            })
            
            # Verify both results have correct structure
            assert hasattr(result1, 'data')
            assert hasattr(result2, 'data')
            
            # Verify pagination values
            assert result1.data.offset == 0
            assert result1.data.limit == 3
            assert result2.data.offset == 3
            assert result2.data.limit == 3
            
            # Total items should be the same
            assert result1.data.total_items == result2.data.total_items

    @pytest.mark.asyncio
    async def test_get_tags_default_parameters(self, token):
        """Test tag retrieval with default parameters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_tags", {})
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            
            # Verify default values
            assert result.data.offset == 0
            assert result.data.limit == 10

    @pytest.mark.asyncio
    async def test_get_tags_combined_filters(self, token):
        """Test tag retrieval with multiple filters combined."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_tags", {
                'offset': 0,
                'limit': 5,
                'name': 'BOD',
                'description': 'Business',
                'order_by': 'name'
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'items')
            
            # Verify items have required fields
            if result.data.items:
                for item in result.data.items:
                    assert hasattr(item, 'name')
                    assert hasattr(item, 'tag_id')
                    assert hasattr(item, 'description')

    @pytest.mark.asyncio
    async def test_get_tags_empty_result(self, token):
        """Test tag retrieval with filters that should return no results."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_tags", {
                'offset': 0,
                'limit': 10,
                'name': 'NonExistentTagName12345'
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            
            # Should return empty results
            assert result.data.total_items == 0
            assert result.data.items == []

    @pytest.mark.asyncio
    async def test_get_tags_invalid_date_range(self, token):
        """Test tag retrieval with invalid date range format."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):  # Should raise an error for invalid date format
                await client.call_tool("get_tags", {
                    'offset': 0,
                    'limit': 10,
                    'created_on': 'invalid-date-format'
                })

    @pytest.mark.asyncio
    async def test_get_tags_negative_offset(self, token):
        """Test tag retrieval with negative offset (should be handled gracefully)."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):  # Should raise an error for negative offset
                await client.call_tool("get_tags", {
                    'offset': -1,
                    'limit': 10
                })

    @pytest.mark.asyncio
    async def test_get_tags_zero_limit(self, token):
        """Test tag retrieval with zero limit."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception) as exc_info:
                await client.call_tool("get_tags", {
                    'offset': 0,
                    'limit': 0
                })
            
            # Should raise a validation error
            assert "validation error" in str(exc_info.value).lower()

    @pytest.mark.asyncio
    async def test_get_tags_large_limit(self, token):
        """Test tag retrieval with large limit."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception) as exc_info:
                await client.call_tool("get_tags", {
                    'offset': 0,
                    'limit': 1000
                })
            
            # Should raise a validation error
            assert "validation error" in str(exc_info.value).lower()

    @pytest.mark.asyncio
    async def test_get_tags_invalid_authentication(self, invalid_token):
        """Test tag retrieval with invalid authentication token."""
        with pytest.raises(Exception) as exc_info:  # Should raise an authentication error
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                await client.call_tool("get_tags", {
                    'offset': 0,
                    'limit': 10
                })
        
        # Should raise an authentication or connection error
        error_msg = str(exc_info.value).lower()
        assert "unauthorized" in error_msg or "401" in error_msg or "authentication" in error_msg
