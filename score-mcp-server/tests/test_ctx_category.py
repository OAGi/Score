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
def created_ctx_category_id(token):
    """Create a context category for testing and return its ID."""

    async def _create_category():
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("create_context_category", {
                'name': 'test_category',
                'description': 'test description'
            })
            # The new tools return CallToolResult objects with structured content
            if hasattr(result, 'data') and hasattr(result.data, 'ctx_category_id'):
                return result.data.ctx_category_id
            else:
                pytest.fail(f"Failed to create test context category: {result}")

    # Run the async function and return the result
    import asyncio
    return asyncio.run(_create_category())


class TestCreateCtxCategory:
    """Test cases for create_context_category tool."""

    @pytest.mark.asyncio
    async def test_create_context_category_success(self, token):
        """Test successful context category creation."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("create_context_category", {
                'name': 'test_create_success',
                'description': 'test description for create success'
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'ctx_category_id')
            assert isinstance(result.data.ctx_category_id, int)
            assert result.data.ctx_category_id > 0

    @pytest.mark.asyncio
    async def test_create_context_category_success_minimal(self, token):
        """Test successful context category creation with minimal data."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("create_context_category", {
                'name': 'minimal_test'
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'ctx_category_id')
            assert isinstance(result.data.ctx_category_id, int)
            assert result.data.ctx_category_id > 0

    @pytest.mark.asyncio
    async def test_create_context_category_error_authentication(self, invalid_token):
        """Test context category creation with invalid authentication."""
        with pytest.raises(Exception):  # Expect HTTP connection to fail
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                result = await client.call_tool("create_context_category", {
                    'name': 'test_auth_error',
                    'description': 'test description'
                })
                content = extract_content(result)
                data = json.loads(content)

    @pytest.mark.asyncio
    async def test_create_context_category_error_empty_name(self, token):
        """Test context category creation with empty name."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("create_context_category", {
                    'name': '',
                    'description': 'test description'
                })

    @pytest.mark.asyncio
    async def test_create_context_category_error_long_name(self, token):
        """Test context category creation with name too long."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("create_context_category", {
                    'name': 'a' * 50,  # Exceeds 45 character limit
                    'description': 'test description'
                })


class TestGetCtxCategory:
    """Test cases for get_context_category tool."""

    @pytest.mark.asyncio
    async def test_get_context_category_success(self, token, created_ctx_category_id):
        """Test successful context category retrieval."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_category", {
                'ctx_category_id': created_ctx_category_id
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'ctx_category_id')
            assert result.data.ctx_category_id == created_ctx_category_id
            assert hasattr(result.data, 'name')
            assert result.data.name == 'test_category'
            assert hasattr(result.data, 'description')
            assert result.data.description == 'test description'
            assert hasattr(result.data, 'created')

    @pytest.mark.asyncio
    async def test_get_context_category_error_not_found(self, token):
        """Test context category retrieval with non-existent ID."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("get_context_category", {
                    'ctx_category_id': 999999
                })

    @pytest.mark.asyncio
    async def test_get_context_category_error_invalid_id(self, token):
        """Test context category retrieval with invalid ID."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("get_context_category", {
                    'ctx_category_id': -1
                })

    @pytest.mark.asyncio
    async def test_get_context_category_error_authentication(self, invalid_token, created_ctx_category_id):
        """Test context category retrieval with invalid authentication."""
        with pytest.raises(Exception):  # Expect HTTP connection to fail
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                result = await client.call_tool("get_context_category", {
                    'ctx_category_id': created_ctx_category_id
                })
                content = extract_content(result)
                data = json.loads(content)


class TestUpdateCtxCategory:
    """Test cases for update_context_category tool."""

    @pytest.mark.asyncio
    async def test_update_context_category_success(self, token, created_ctx_category_id):
        """Test successful context category update."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("update_context_category", {
                'ctx_category_id': created_ctx_category_id,
                'name': 'updated_test_category',
                'description': 'updated test description'
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'ctx_category_id')
            assert result.data.ctx_category_id == created_ctx_category_id
            assert hasattr(result.data, 'updates')
            # The updates field is now a list of strings
            assert isinstance(result.data.updates, list)
            assert len(result.data.updates) > 0

    @pytest.mark.asyncio
    async def test_update_context_category_success_minimal(self, token, created_ctx_category_id):
        """Test successful context category update with minimal data."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("update_context_category", {
                'ctx_category_id': created_ctx_category_id,
                'name': 'minimal_update',
                'description': 'minimal update description'
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'ctx_category_id')
            assert result.data.ctx_category_id == created_ctx_category_id
            assert hasattr(result.data, 'updates')
            # The updates field is now a list of strings
            assert isinstance(result.data.updates, list)
            assert len(result.data.updates) > 0

    @pytest.mark.asyncio
    async def test_update_context_category_error_not_found(self, token):
        """Test context category update with non-existent ID."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("update_context_category", {
                    'ctx_category_id': 999999,
                    'name': 'not_found_test'
                })

    @pytest.mark.asyncio
    async def test_update_context_category_error_empty_name(self, token, created_ctx_category_id):
        """Test context category update with empty name."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("update_context_category", {
                    'ctx_category_id': created_ctx_category_id,
                    'name': ''
                })

    @pytest.mark.asyncio
    async def test_update_context_category_error_long_name(self, token, created_ctx_category_id):
        """Test context category update with name too long."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("update_context_category", {
                    'ctx_category_id': created_ctx_category_id,
                    'name': 'a' * 50  # Exceeds 45 character limit
                })

    @pytest.mark.asyncio
    async def test_update_context_category_error_authentication(self, invalid_token, created_ctx_category_id):
        """Test context category update with invalid authentication."""
        with pytest.raises(Exception):  # Expect HTTP connection to fail
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                result = await client.call_tool("update_context_category", {
                    'ctx_category_id': created_ctx_category_id,
                    'name': 'auth_error_test'
                })
                content = extract_content(result)
                data = json.loads(content)


class TestDeleteCtxCategory:
    """Test cases for delete_context_category tool."""

    @pytest.mark.asyncio
    async def test_delete_context_category_success(self, token):
        """Test successful context category deletion."""
        # First create a category to delete
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            create_result = await client.call_tool("create_context_category", {
                'name': 'to_be_deleted',
                'description': 'will be deleted'
            })
            ctx_category_id = create_result.data.ctx_category_id

            # Now delete it
            result = await client.call_tool("delete_context_category", {
                'ctx_category_id': ctx_category_id
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'ctx_category_id')
            assert result.data.ctx_category_id == ctx_category_id

    @pytest.mark.asyncio
    async def test_delete_context_category_error_not_found(self, token):
        """Test context category deletion with non-existent ID."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("delete_context_category", {
                    'ctx_category_id': 999999
                })

    @pytest.mark.asyncio
    async def test_delete_context_category_error_invalid_id(self, token):
        """Test context category deletion with invalid ID."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("delete_context_category", {
                    'ctx_category_id': -1
                })

    @pytest.mark.asyncio
    async def test_delete_context_category_error_authentication(self, invalid_token):
        """Test context category deletion with invalid authentication."""
        with pytest.raises(Exception):  # Expect HTTP connection to fail
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                result = await client.call_tool("delete_context_category", {
                    'ctx_category_id': 1
                })
                content = extract_content(result)
                data = json.loads(content)


class TestGetCtxCategories:
    """Test cases for get_context_categories tool."""

    @pytest.mark.asyncio
    async def test_get_context_categories_success(self, token):
        """Test successful context categories retrieval with pagination."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_categories", {
                'offset': 0,
                'limit': 5
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert result.data.offset == 0
            assert result.data.limit == 5
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_context_categories_with_filters(self, token):
        """Test context categories retrieval with filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_categories", {
                'offset': 0,
                'limit': 10,
                'name': 'test'
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_context_categories_with_ordering(self, token):
        """Test context categories retrieval with ordering."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_categories", {
                'offset': 0,
                'limit': 10,
                'order_by': '-name'
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_context_categories_with_multiple_ordering(self, token):
        """Test context categories retrieval with multiple column ordering."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_categories", {
                'offset': 0,
                'limit': 10,
                'order_by': '-last_update_timestamp,+name,description'
            })
            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_context_categories_with_default_ordering(self, token):
        """Test context categories retrieval with default ordering (no prefix)."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_categories", {
                'offset': 0,
                'limit': 10,
                'order_by': 'name,description'
            })
            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_context_categories_error_invalid_order_by_column(self, token):
        """Test context categories retrieval with invalid order_by column."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("get_context_categories", {
                    'offset': 0,
                    'limit': 10,
                    'order_by': 'invalid_column'
                })

    @pytest.mark.asyncio
    async def test_get_context_categories_error_invalid_order_by_format(self, token):
        """Test context categories retrieval with invalid order_by format."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("get_context_categories", {
                    'offset': 0,
                    'limit': 10,
                    'order_by': '-'
                })

    @pytest.mark.asyncio
    async def test_get_context_categories_with_description_filter(self, token):
        """Test context categories retrieval with description filter."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_categories", {
                'offset': 0,
                'limit': 10,
                'description': 'test'
            })
            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_context_categories_with_date_range_filters(self, token):
        """Test context categories retrieval with date range filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_categories", {
                'offset': 0,
                'limit': 10,
                'created_on': '[2000-01-01~2099-12-31]'
            })
            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_context_categories_with_before_date_filter(self, token):
        """Test context categories retrieval with before date filter."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_categories", {
                'offset': 0,
                'limit': 10,
                'created_on': '[~2025-12-31]'
            })
            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_context_categories_with_after_date_filter(self, token):
        """Test context categories retrieval with after date filter."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_categories", {
                'offset': 0,
                'limit': 10,
                'last_updated_on': '[2025-01-01~]'
            })
            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_context_categories_error_negative_offset(self, token):
        """Test context categories retrieval with negative offset."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("get_context_categories", {
                    'offset': -1,
                    'limit': 10
                })

    @pytest.mark.asyncio
    async def test_get_context_categories_error_negative_limit(self, token):
        """Test context categories retrieval with negative limit."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("get_context_categories", {
                    'offset': 0,
                    'limit': -1
                })

    @pytest.mark.asyncio
    async def test_get_context_categories_error_zero_limit(self, token):
        """Test context categories retrieval with zero limit."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("get_context_categories", {
                    'offset': 0,
                    'limit': 0
                })

    @pytest.mark.asyncio
    async def test_get_context_categories_error_limit_too_large(self, token):
        """Test context categories retrieval with limit exceeding maximum."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("get_context_categories", {
                    'offset': 0,
                    'limit': 101
                })

    @pytest.mark.asyncio
    async def test_get_context_categories_error_authentication(self, invalid_token):
        """Test context categories retrieval with invalid authentication."""
        with pytest.raises(Exception):  # Expect HTTP connection to fail
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                result = await client.call_tool("get_context_categories", {
                    'offset': 0,
                    'limit': 10
                })
                content = extract_content(result)
                data = json.loads(content)


class TestCtxCategoryIntegration:
    """Integration tests for context category CRUD operations."""

    @pytest.mark.asyncio
    async def test_full_crud_cycle(self, token):
        """Test complete CRUD cycle: create, read, update, delete."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Create
            create_result = await client.call_tool("create_context_category", {
                'name': 'integration_test',
                'description': 'integration test description'
            })
            ctx_category_id = create_result.data.ctx_category_id

            # Read
            get_result = await client.call_tool("get_context_category", {
                'ctx_category_id': ctx_category_id
            })
            assert get_result.data.name == 'integration_test'

            # Update
            update_result = await client.call_tool("update_context_category", {
                'ctx_category_id': ctx_category_id,
                'name': 'updated_integration_test',
                'description': 'updated integration test description'
            })
            assert update_result.data.ctx_category_id == ctx_category_id
            assert hasattr(update_result.data, 'updates')
            assert isinstance(update_result.data.updates, list)
            assert len(update_result.data.updates) > 0

            # Delete
            delete_result = await client.call_tool("delete_context_category", {
                'ctx_category_id': ctx_category_id
            })
            assert delete_result.data.ctx_category_id == ctx_category_id

            # Verify deletion - should raise ToolError
            with pytest.raises(Exception):  # Expect ToolError to be raised
                await client.call_tool("get_context_category", {
                    'ctx_category_id': ctx_category_id
                })
