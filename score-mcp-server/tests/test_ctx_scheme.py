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
                'name': 'test_scheme_category',
                'description': 'test category for scheme testing'
            })
            # The new tools return CallToolResult objects with structured content
            if hasattr(result, 'data') and hasattr(result.data, 'ctx_category_id'):
                return result.data.ctx_category_id
            else:
                pytest.fail(f"Failed to create test context category: {result}")

    # Run the async function and return the result
    import asyncio
    return asyncio.run(_create_category())


@pytest.fixture
def created_ctx_scheme_id(token, created_ctx_category_id):
    """Create a context scheme for testing and return its ID."""

    async def _create_scheme():
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("create_context_scheme", {
                'ctx_category_id': created_ctx_category_id,
                'scheme_id': 'TEST_SCHEME_001',
                'scheme_agency_id': 'TEST_AGENCY',
                'scheme_version_id': '1.0',
                'scheme_name': 'Test Context Scheme',
                'description': 'Test scheme for testing purposes'
            })
            # The new tools return CallToolResult objects with structured content
            if hasattr(result, 'data') and hasattr(result.data, 'ctx_scheme_id'):
                return result.data.ctx_scheme_id
            else:
                pytest.fail(f"Failed to create test context scheme: {result}")

    # Run the async function and return the result
    import asyncio
    return asyncio.run(_create_scheme())


@pytest.fixture
def created_ctx_scheme_value_id(token, created_ctx_scheme_id):
    """Create a context scheme value for testing and return its ID."""

    async def _create_scheme_value():
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("create_context_scheme_value", {
                'ctx_scheme_id': created_ctx_scheme_id,
                'value': 'TEST_VALUE_001',
                'meaning': 'Test value for testing purposes'
            })
            # The new tools return CallToolResult objects with structured content
            if hasattr(result, 'data') and hasattr(result.data, 'ctx_scheme_value_id'):
                return result.data.ctx_scheme_value_id
            else:
                pytest.fail(f"Failed to create test context scheme value: {result}")

    # Run the async function and return the result
    import asyncio
    return asyncio.run(_create_scheme_value())


class TestCreateCtxScheme:
    """Test cases for create_context_scheme tool."""

    @pytest.mark.asyncio
    async def test_create_context_scheme_success(self, token, created_ctx_category_id):
        """Test successful context scheme creation."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("create_context_scheme", {
                'ctx_category_id': created_ctx_category_id,
                'scheme_id': 'TEST_CREATE_SUCCESS',
                'scheme_agency_id': 'TEST_AGENCY',
                'scheme_version_id': '1.0',
                'scheme_name': 'Test Create Success',
                'description': 'Test description for create success'
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'ctx_scheme_id')
            assert isinstance(result.data.ctx_scheme_id, int)
            assert result.data.ctx_scheme_id > 0

    @pytest.mark.asyncio
    async def test_create_context_scheme_success_minimal(self, token, created_ctx_category_id):
        """Test successful context scheme creation with minimal data."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("create_context_scheme", {
                'ctx_category_id': created_ctx_category_id,
                'scheme_id': 'MINIMAL_TEST',
                'scheme_name': 'Minimal Test Scheme',
                'scheme_agency_id': 'MINIMAL_AGENCY',
                'scheme_version_id': '1.0'
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'ctx_scheme_id')
            assert isinstance(result.data.ctx_scheme_id, int)
            assert result.data.ctx_scheme_id > 0

    @pytest.mark.asyncio
    async def test_create_context_scheme_error_authentication(self, invalid_token, created_ctx_category_id):
        """Test context scheme creation with invalid authentication."""
        with pytest.raises(Exception):  # Expect HTTP connection to fail
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                result = await client.call_tool("create_context_scheme", {
                    'ctx_category_id': created_ctx_category_id,
                    'scheme_id': 'TEST_AUTH_ERROR',
                    'scheme_agency_id': 'TEST_AGENCY',
                    'scheme_version_id': '1.0'
                })
                content = extract_content(result)
                data = json.loads(content)

    @pytest.mark.asyncio
    async def test_create_context_scheme_error_empty_scheme_id(self, token, created_ctx_category_id):
        """Test context scheme creation with empty scheme_id."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("create_context_scheme", {
                    'ctx_category_id': created_ctx_category_id,
                    'scheme_id': '',
                    'scheme_agency_id': 'TEST_AGENCY',
                    'scheme_version_id': '1.0'
                })

    @pytest.mark.asyncio
    async def test_create_context_scheme_error_long_scheme_id(self, token, created_ctx_category_id):
        """Test context scheme creation with scheme_id too long."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("create_context_scheme", {
                    'ctx_category_id': created_ctx_category_id,
                    'scheme_id': 'a' * 50,  # Exceeds 45 character limit
                    'scheme_agency_id': 'TEST_AGENCY',
                    'scheme_version_id': '1.0'
                })

    @pytest.mark.asyncio
    async def test_create_context_scheme_error_invalid_category_id(self, token):
        """Test context scheme creation with invalid category ID."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("create_context_scheme", {
                    'ctx_category_id': 999999,
                    'scheme_id': 'TEST_INVALID_CATEGORY',
                    'scheme_agency_id': 'TEST_AGENCY',
                    'scheme_version_id': '1.0'
                })


class TestCreateCtxSchemeValue:
    """Test cases for create_context_scheme_value tool."""

    @pytest.mark.asyncio
    async def test_create_context_scheme_value_success(self, token, created_ctx_scheme_id):
        """Test successful context scheme value creation."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("create_context_scheme_value", {
                'ctx_scheme_id': created_ctx_scheme_id,
                'value': 'TEST_VALUE_SUCCESS',
                'meaning': 'Test value for success testing'
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'ctx_scheme_value_id')
            assert isinstance(result.data.ctx_scheme_value_id, int)
            assert result.data.ctx_scheme_value_id > 0

    @pytest.mark.asyncio
    async def test_create_context_scheme_value_success_minimal(self, token, created_ctx_scheme_id):
        """Test successful context scheme value creation with minimal data."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("create_context_scheme_value", {
                'ctx_scheme_id': created_ctx_scheme_id,
                'value': 'MINIMAL_VALUE'
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'ctx_scheme_value_id')
            assert isinstance(result.data.ctx_scheme_value_id, int)
            assert result.data.ctx_scheme_value_id > 0

    @pytest.mark.asyncio
    async def test_create_context_scheme_value_error_authentication(self, invalid_token, created_ctx_scheme_id):
        """Test context scheme value creation with invalid authentication."""
        with pytest.raises(Exception):  # Expect HTTP connection to fail
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                result = await client.call_tool("create_context_scheme_value", {
                    'ctx_scheme_id': created_ctx_scheme_id,
                    'value': 'TEST_AUTH_ERROR'
                })
                content = extract_content(result)
                data = json.loads(content)

    @pytest.mark.asyncio
    async def test_create_context_scheme_value_error_empty_value(self, token, created_ctx_scheme_id):
        """Test context scheme value creation with empty value."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("create_context_scheme_value", {
                    'ctx_scheme_id': created_ctx_scheme_id,
                    'value': ''
                })

    @pytest.mark.asyncio
    async def test_create_context_scheme_value_error_long_value(self, token, created_ctx_scheme_id):
        """Test context scheme value creation with value too long."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("create_context_scheme_value", {
                    'ctx_scheme_id': created_ctx_scheme_id,
                    'value': 'a' * 110  # Exceeds 100 character limit
                })

    @pytest.mark.asyncio
    async def test_create_context_scheme_value_error_invalid_scheme_id(self, token):
        """Test context scheme value creation with invalid scheme ID."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("create_context_scheme_value", {
                    'ctx_scheme_id': 999999,
                    'value': 'TEST_INVALID_SCHEME'
                })


class TestGetCtxScheme:
    """Test cases for get_context_scheme tool."""

    @pytest.mark.asyncio
    async def test_get_context_scheme_success(self, token, created_ctx_scheme_id):
        """Test successful context scheme retrieval."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_scheme", {
                'ctx_scheme_id': created_ctx_scheme_id
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'ctx_scheme_id')
            assert result.data.ctx_scheme_id == created_ctx_scheme_id
            assert hasattr(result.data, 'scheme_id')
            assert result.data.scheme_id == 'TEST_SCHEME_001'
            assert hasattr(result.data, 'scheme_name')
            assert result.data.scheme_name == 'Test Context Scheme'
            assert hasattr(result.data, 'ctx_category')
            assert hasattr(result.data, 'values')
            assert hasattr(result.data, 'created')
            assert hasattr(result.data, 'last_updated')

    @pytest.mark.asyncio
    async def test_get_context_scheme_error_not_found(self, token):
        """Test context scheme retrieval with non-existent ID."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("get_context_scheme", {
                    'ctx_scheme_id': 999999
                })

    @pytest.mark.asyncio
    async def test_get_context_scheme_error_invalid_id(self, token):
        """Test context scheme retrieval with invalid ID."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("get_context_scheme", {
                    'ctx_scheme_id': -1
                })

    @pytest.mark.asyncio
    async def test_get_context_scheme_error_authentication(self, invalid_token, created_ctx_scheme_id):
        """Test context scheme retrieval with invalid authentication."""
        with pytest.raises(Exception):  # Expect HTTP connection to fail
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                result = await client.call_tool("get_context_scheme", {
                    'ctx_scheme_id': created_ctx_scheme_id
                })
                content = extract_content(result)
                data = json.loads(content)


class TestUpdateCtxScheme:
    """Test cases for update_context_scheme tool."""

    @pytest.mark.asyncio
    async def test_update_context_scheme_success(self, token, created_ctx_scheme_id):
        """Test successful context scheme update."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("update_context_scheme", {
                'ctx_scheme_id': created_ctx_scheme_id,
                'scheme_name': 'Updated Test Context Scheme',
                'description': 'Updated test description'
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'ctx_scheme_id')
            assert result.data.ctx_scheme_id == created_ctx_scheme_id
            assert hasattr(result.data, 'updates')
            # The updates field is now a list of strings
            assert isinstance(result.data.updates, list)
            assert len(result.data.updates) > 0

    @pytest.mark.asyncio
    async def test_update_context_scheme_success_minimal(self, token, created_ctx_scheme_id):
        """Test successful context scheme update with minimal data."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("update_context_scheme", {
                'ctx_scheme_id': created_ctx_scheme_id,
                'scheme_name': 'Minimal Update',
                'description': 'Minimal update description'
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'ctx_scheme_id')
            assert result.data.ctx_scheme_id == created_ctx_scheme_id
            assert hasattr(result.data, 'updates')
            # The updates field is now a list of strings
            assert isinstance(result.data.updates, list)
            assert len(result.data.updates) > 0

    @pytest.mark.asyncio
    async def test_update_context_scheme_error_not_found(self, token):
        """Test context scheme update with non-existent ID."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("update_context_scheme", {
                    'ctx_scheme_id': 999999,
                    'scheme_name': 'Not Found Test'
                })

    @pytest.mark.asyncio
    async def test_update_context_scheme_error_empty_scheme_id(self, token, created_ctx_scheme_id):
        """Test context scheme update with empty scheme_id."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("update_context_scheme", {
                    'ctx_scheme_id': created_ctx_scheme_id,
                    'scheme_id': ''
                })

    @pytest.mark.asyncio
    async def test_update_context_scheme_error_long_scheme_id(self, token, created_ctx_scheme_id):
        """Test context scheme update with scheme_id too long."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("update_context_scheme", {
                    'ctx_scheme_id': created_ctx_scheme_id,
                    'scheme_id': 'a' * 50  # Exceeds 45 character limit
                })

    @pytest.mark.asyncio
    async def test_update_context_scheme_error_authentication(self, invalid_token, created_ctx_scheme_id):
        """Test context scheme update with invalid authentication."""
        with pytest.raises(Exception):  # Expect HTTP connection to fail
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                result = await client.call_tool("update_context_scheme", {
                    'ctx_scheme_id': created_ctx_scheme_id,
                    'scheme_name': 'Auth Error Test'
                })
                content = extract_content(result)
                data = json.loads(content)


class TestUpdateCtxSchemeValue:
    """Test cases for update_context_scheme_value tool."""

    @pytest.mark.asyncio
    async def test_update_context_scheme_value_success(self, token, created_ctx_scheme_value_id):
        """Test successful context scheme value update."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("update_context_scheme_value", {
                'ctx_scheme_value_id': created_ctx_scheme_value_id,
                'value': 'UPDATED_TEST_VALUE',
                'meaning': 'Updated test value meaning'
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'ctx_scheme_value_id')
            assert result.data.ctx_scheme_value_id == created_ctx_scheme_value_id
            assert hasattr(result.data, 'updates')
            # The updates field is now a list of strings
            assert isinstance(result.data.updates, list)
            assert len(result.data.updates) > 0

    @pytest.mark.asyncio
    async def test_update_context_scheme_value_success_minimal(self, token, created_ctx_scheme_value_id):
        """Test successful context scheme value update with minimal data."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("update_context_scheme_value", {
                'ctx_scheme_value_id': created_ctx_scheme_value_id,
                'value': 'MINIMAL_UPDATE_VALUE'
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'ctx_scheme_value_id')
            assert result.data.ctx_scheme_value_id == created_ctx_scheme_value_id
            assert hasattr(result.data, 'updates')
            # The updates field is now a list of strings
            assert isinstance(result.data.updates, list)
            assert len(result.data.updates) > 0

    @pytest.mark.asyncio
    async def test_update_context_scheme_value_error_not_found(self, token):
        """Test context scheme value update with non-existent ID."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("update_context_scheme_value", {
                    'ctx_scheme_value_id': 999999,
                    'value': 'Not Found Test'
                })

    @pytest.mark.asyncio
    async def test_update_context_scheme_value_error_empty_value(self, token, created_ctx_scheme_value_id):
        """Test context scheme value update with empty value."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("update_context_scheme_value", {
                    'ctx_scheme_value_id': created_ctx_scheme_value_id,
                    'value': ''
                })

    @pytest.mark.asyncio
    async def test_update_context_scheme_value_error_long_value(self, token, created_ctx_scheme_value_id):
        """Test context scheme value update with value too long."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("update_context_scheme_value", {
                    'ctx_scheme_value_id': created_ctx_scheme_value_id,
                    'value': 'a' * 110  # Exceeds 100 character limit
                })

    @pytest.mark.asyncio
    async def test_update_context_scheme_value_error_authentication(self, invalid_token, created_ctx_scheme_value_id):
        """Test context scheme value update with invalid authentication."""
        with pytest.raises(Exception):  # Expect HTTP connection to fail
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                result = await client.call_tool("update_context_scheme_value", {
                    'ctx_scheme_value_id': created_ctx_scheme_value_id,
                    'value': 'Auth Error Test'
                })
                content = extract_content(result)
                data = json.loads(content)


class TestDeleteCtxScheme:
    """Test cases for delete_context_scheme tool."""

    @pytest.mark.asyncio
    async def test_delete_context_scheme_success(self, token, created_ctx_category_id):
        """Test successful context scheme deletion."""
        # First create a scheme to delete
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            create_result = await client.call_tool("create_context_scheme", {
                'ctx_category_id': created_ctx_category_id,
                'scheme_id': 'TO_BE_DELETED',
                'scheme_agency_id': 'DELETE_AGENCY',
                'scheme_version_id': '1.0',
                'scheme_name': 'To Be Deleted'
            })
            ctx_scheme_id = create_result.data.ctx_scheme_id

            # Now delete it
            result = await client.call_tool("delete_context_scheme", {
                'ctx_scheme_id': ctx_scheme_id
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'ctx_scheme_id')
            assert result.data.ctx_scheme_id == ctx_scheme_id

    @pytest.mark.asyncio
    async def test_delete_context_scheme_error_not_found(self, token):
        """Test context scheme deletion with non-existent ID."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("delete_context_scheme", {
                    'ctx_scheme_id': 999999
                })

    @pytest.mark.asyncio
    async def test_delete_context_scheme_error_invalid_id(self, token):
        """Test context scheme deletion with invalid ID."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("delete_context_scheme", {
                    'ctx_scheme_id': -1
                })

    @pytest.mark.asyncio
    async def test_delete_context_scheme_error_authentication(self, invalid_token):
        """Test context scheme deletion with invalid authentication."""
        with pytest.raises(Exception):  # Expect HTTP connection to fail
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                result = await client.call_tool("delete_context_scheme", {
                    'ctx_scheme_id': 1
                })
                content = extract_content(result)
                data = json.loads(content)


class TestDeleteCtxSchemeValue:
    """Test cases for delete_context_scheme_value tool."""

    @pytest.mark.asyncio
    async def test_delete_context_scheme_value_success(self, token, created_ctx_scheme_id):
        """Test successful context scheme value deletion."""
        # First create a scheme value to delete
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            create_result = await client.call_tool("create_context_scheme_value", {
                'ctx_scheme_id': created_ctx_scheme_id,
                'value': 'TO_BE_DELETED_VALUE',
                'meaning': 'Will be deleted'
            })
            ctx_scheme_value_id = create_result.data.ctx_scheme_value_id

            # Now delete it
            result = await client.call_tool("delete_context_scheme_value", {
                'ctx_scheme_value_id': ctx_scheme_value_id
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'ctx_scheme_value_id')
            assert result.data.ctx_scheme_value_id == ctx_scheme_value_id

    @pytest.mark.asyncio
    async def test_delete_context_scheme_value_error_not_found(self, token):
        """Test context scheme value deletion with non-existent ID."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("delete_context_scheme_value", {
                    'ctx_scheme_value_id': 999999
                })

    @pytest.mark.asyncio
    async def test_delete_context_scheme_value_error_invalid_id(self, token):
        """Test context scheme value deletion with invalid ID."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("delete_context_scheme_value", {
                    'ctx_scheme_value_id': -1
                })

    @pytest.mark.asyncio
    async def test_delete_context_scheme_value_error_authentication(self, invalid_token):
        """Test context scheme value deletion with invalid authentication."""
        with pytest.raises(Exception):  # Expect HTTP connection to fail
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                result = await client.call_tool("delete_context_scheme_value", {
                    'ctx_scheme_value_id': 1
                })
                content = extract_content(result)
                data = json.loads(content)


class TestGetCtxSchemes:
    """Test cases for get_context_schemes tool."""

    @pytest.mark.asyncio
    async def test_get_context_schemes_success(self, token):
        """Test successful context schemes retrieval with pagination."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_schemes", {
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
    async def test_get_context_schemes_with_filters(self, token):
        """Test context schemes retrieval with filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_schemes", {
                'offset': 0,
                'limit': 10,
                'scheme_id': 'TEST'
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_context_schemes_with_ordering(self, token):
        """Test context schemes retrieval with ordering."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_schemes", {
                'offset': 0,
                'limit': 10,
                'order_by': '-scheme_id'
            })

            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_context_schemes_with_multiple_ordering(self, token):
        """Test context schemes retrieval with multiple column ordering."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_schemes", {
                'offset': 0,
                'limit': 10,
                'order_by': '-last_update_timestamp,+scheme_id,scheme_name'
            })
            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_context_schemes_with_default_ordering(self, token):
        """Test context schemes retrieval with default ordering (no prefix)."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_schemes", {
                'offset': 0,
                'limit': 10,
                'order_by': 'scheme_id,scheme_name'
            })
            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_context_schemes_error_invalid_order_by_column(self, token):
        """Test context schemes retrieval with invalid order_by column."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("get_context_schemes", {
                    'offset': 0,
                    'limit': 10,
                    'order_by': 'invalid_column'
                })

    @pytest.mark.asyncio
    async def test_get_context_schemes_error_invalid_order_by_format(self, token):
        """Test context schemes retrieval with invalid order_by format."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("get_context_schemes", {
                    'offset': 0,
                    'limit': 10,
                    'order_by': '-'
                })

    @pytest.mark.asyncio
    async def test_get_context_schemes_with_description_filter(self, token):
        """Test context schemes retrieval with description filter."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_schemes", {
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
    async def test_get_context_schemes_with_ctx_category_name_filter(self, token):
        """Test context schemes retrieval with context category name filter."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_schemes", {
                'offset': 0,
                'limit': 10,
                'ctx_category_name': 'test'
            })
            # The new tools return CallToolResult objects with structured content
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_context_schemes_with_date_range_filters(self, token):
        """Test context schemes retrieval with date range filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_schemes", {
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
    async def test_get_context_schemes_with_before_date_filter(self, token):
        """Test context schemes retrieval with before date filter."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_schemes", {
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
    async def test_get_context_schemes_with_after_date_filter(self, token):
        """Test context schemes retrieval with after date filter."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_context_schemes", {
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
    async def test_get_context_schemes_error_negative_offset(self, token):
        """Test context schemes retrieval with negative offset."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("get_context_schemes", {
                    'offset': -1,
                    'limit': 10
                })

    @pytest.mark.asyncio
    async def test_get_context_schemes_error_negative_limit(self, token):
        """Test context schemes retrieval with negative limit."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("get_context_schemes", {
                    'offset': 0,
                    'limit': -1
                })

    @pytest.mark.asyncio
    async def test_get_context_schemes_error_zero_limit(self, token):
        """Test context schemes retrieval with zero limit."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("get_context_schemes", {
                    'offset': 0,
                    'limit': 0
                })

    @pytest.mark.asyncio
    async def test_get_context_schemes_error_limit_too_large(self, token):
        """Test context schemes retrieval with limit exceeding maximum."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("get_context_schemes", {
                    'offset': 0,
                    'limit': 101
                })

    @pytest.mark.asyncio
    async def test_get_context_schemes_error_authentication(self, invalid_token):
        """Test context schemes retrieval with invalid authentication."""
        with pytest.raises(Exception):  # Expect HTTP connection to fail
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                result = await client.call_tool("get_context_schemes", {
                    'offset': 0,
                    'limit': 10
                })
                content = extract_content(result)
                data = json.loads(content)


class TestCtxSchemeIntegration:
    """Integration tests for context scheme CRUD operations."""

    @pytest.mark.asyncio
    async def test_full_crud_cycle(self, token, created_ctx_category_id):
        """Test complete CRUD cycle: create, read, update, delete."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Create
            create_result = await client.call_tool("create_context_scheme", {
                'ctx_category_id': created_ctx_category_id,
                'scheme_id': 'INTEGRATION_TEST',
                'scheme_agency_id': 'INTEGRATION_AGENCY',
                'scheme_version_id': '1.0',
                'scheme_name': 'Integration Test Scheme',
                'description': 'Integration test description'
            })
            ctx_scheme_id = create_result.data.ctx_scheme_id

            # Read
            get_result = await client.call_tool("get_context_scheme", {
                'ctx_scheme_id': ctx_scheme_id
            })
            assert get_result.data.scheme_name == 'Integration Test Scheme'

            # Update
            update_result = await client.call_tool("update_context_scheme", {
                'ctx_scheme_id': ctx_scheme_id,
                'scheme_name': 'Updated Integration Test Scheme',
                'description': 'Updated integration test description'
            })
            assert update_result.data.ctx_scheme_id == ctx_scheme_id
            assert hasattr(update_result.data, 'updates')
            assert isinstance(update_result.data.updates, list)
            assert len(update_result.data.updates) > 0

            # Delete
            delete_result = await client.call_tool("delete_context_scheme", {
                'ctx_scheme_id': ctx_scheme_id
            })
            assert delete_result.data.ctx_scheme_id == ctx_scheme_id

            # Verify deletion - should raise ToolError
            with pytest.raises(Exception):  # Expect ToolError to be raised
                await client.call_tool("get_context_scheme", {
                    'ctx_scheme_id': ctx_scheme_id
                })


class TestCtxSchemeValueIntegration:
    """Integration tests for context scheme value CRUD operations."""

    @pytest.mark.asyncio
    async def test_full_crud_cycle(self, token, created_ctx_scheme_id):
        """Test complete CRUD cycle: create, read, update, delete."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Create
            create_result = await client.call_tool("create_context_scheme_value", {
                'ctx_scheme_id': created_ctx_scheme_id,
                'value': 'INTEGRATION_VALUE',
                'meaning': 'Integration test value'
            })
            ctx_scheme_value_id = create_result.data.ctx_scheme_value_id

            # Update
            update_result = await client.call_tool("update_context_scheme_value", {
                'ctx_scheme_value_id': ctx_scheme_value_id,
                'value': 'UPDATED_INTEGRATION_VALUE',
                'meaning': 'Updated integration test value'
            })
            assert update_result.data.ctx_scheme_value_id == ctx_scheme_value_id
            assert hasattr(update_result.data, 'updates')
            assert isinstance(update_result.data.updates, list)
            assert len(update_result.data.updates) > 0

            # Delete
            delete_result = await client.call_tool("delete_context_scheme_value", {
                'ctx_scheme_value_id': ctx_scheme_value_id
            })
            assert delete_result.data.ctx_scheme_value_id == ctx_scheme_value_id


class TestCtxSchemeReferentialIntegrity:
    """Test cases for referential integrity between context schemes and context categories."""

    @pytest.mark.asyncio
    async def test_delete_category_with_linked_schemes(self, token, created_ctx_category_id, created_ctx_scheme_id):
        """Test that deleting a context category with linked schemes fails."""
        with pytest.raises(Exception):  # Expect ToolError to be raised
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                await client.call_tool("delete_context_category", {
                    'ctx_category_id': created_ctx_category_id
                })

    @pytest.mark.asyncio
    async def test_delete_category_after_deleting_schemes(self, token, created_ctx_category_id, created_ctx_scheme_id):
        """Test that deleting a context category succeeds after deleting linked schemes."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # First delete the linked scheme
            delete_scheme_result = await client.call_tool("delete_context_scheme", {
                'ctx_scheme_id': created_ctx_scheme_id
            })
            assert delete_scheme_result.data.ctx_scheme_id == created_ctx_scheme_id

            # Now delete the category should succeed
            delete_category_result = await client.call_tool("delete_context_category", {
                'ctx_category_id': created_ctx_category_id
            })
            assert delete_category_result.data.ctx_category_id == created_ctx_category_id
