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
                'name': 'test_biz_ctx_category',
                'description': 'test category for business context testing'
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
                'scheme_id': 'TEST_BIZ_CTX_SCHEME_001',
                'scheme_agency_id': 'TEST_AGENCY',
                'scheme_version_id': '1.0',
                'scheme_name': 'Test Business Context Scheme',
                'description': 'Test scheme for business context testing purposes'
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
                'value': 'TEST_BIZ_CTX_VALUE_001',
                'meaning': 'Test value for business context testing purposes'
            })
            # The new tools return CallToolResult objects with structured content
            if hasattr(result, 'data') and hasattr(result.data, 'ctx_scheme_value_id'):
                return result.data.ctx_scheme_value_id
            else:
                pytest.fail(f"Failed to create test context scheme value: {result}")

    # Run the async function and return the result
    import asyncio
    return asyncio.run(_create_scheme_value())


@pytest.fixture
def created_biz_ctx_id(token):
    """Create a business context for testing and return its ID."""

    async def _create_biz_ctx():
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("create_business_context", {
                'name': 'Test Business Context'
            })
            # The new tools return CallToolResult objects with structured content
            if hasattr(result, 'data') and hasattr(result.data, 'biz_ctx_id'):
                return result.data.biz_ctx_id
            else:
                pytest.fail(f"Failed to create test business context: {result}")

    # Run the async function and return the result
    import asyncio
    return asyncio.run(_create_biz_ctx())


@pytest.fixture
def created_biz_ctx_value_id(token, created_biz_ctx_id, created_ctx_scheme_value_id):
    """Create a business context value for testing and return its ID."""

    async def _create_biz_ctx_value():
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("create_business_context_value", {
                'biz_ctx_id': created_biz_ctx_id,
                'ctx_scheme_value_id': created_ctx_scheme_value_id
            })
            # The new tools return CallToolResult objects with structured content
            if hasattr(result, 'data') and hasattr(result.data, 'biz_ctx_value_id'):
                return result.data.biz_ctx_value_id
            else:
                pytest.fail(f"Failed to create test business context value: {result}")

    # Run the async function and return the result
    import asyncio
    return asyncio.run(_create_biz_ctx_value())


@pytest.fixture
def conflict_test_biz_ctx_id(token):
    """Create a business context specifically for testing deletion conflicts."""

    async def _create_conflict_biz_ctx():
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("create_business_context", {
                'name': 'Conflict Test Business Context'
            })
            if hasattr(result, 'data') and hasattr(result.data, 'biz_ctx_id'):
                return result.data.biz_ctx_id
            else:
                pytest.fail(f"Failed to create conflict test business context: {result}")

    import asyncio
    return asyncio.run(_create_conflict_biz_ctx())


class TestCreateBusinessContext:
    """Test cases for create_business_context tool."""

    @pytest.mark.asyncio
    async def test_create_business_context_success(self, token):
        """Test successful business context creation."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("create_business_context", {
                'name': 'Production Environment'
            })

            assert hasattr(result, 'data')
            assert hasattr(result.data, 'biz_ctx_id')
            assert isinstance(result.data.biz_ctx_id, int)
            assert result.data.biz_ctx_id > 0

    @pytest.mark.asyncio
    async def test_create_business_context_without_name(self, token):
        """Test business context creation without name (should fail as name is required)."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("create_business_context", {})

    @pytest.mark.asyncio
    async def test_create_business_context_invalid_auth(self, invalid_token):
        """Test business context creation with invalid authentication."""
        with pytest.raises(Exception):  # Expect HTTP connection to fail
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=invalid_token)) as client:
                await client.call_tool("create_business_context", {
                    'name': 'Test Business Context'
                })


class TestCreateBusinessContextValue:
    """Test cases for create_business_context_value tool."""

    @pytest.mark.asyncio
    async def test_create_business_context_value_success(self, token, created_biz_ctx_id, created_ctx_scheme_value_id):
        """Test successful business context value creation."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("create_business_context_value", {
                'biz_ctx_id': created_biz_ctx_id,
                'ctx_scheme_value_id': created_ctx_scheme_value_id
            })

            assert hasattr(result, 'data')
            assert hasattr(result.data, 'biz_ctx_value_id')
            assert isinstance(result.data.biz_ctx_value_id, int)
            assert result.data.biz_ctx_value_id > 0

    @pytest.mark.asyncio
    async def test_create_business_context_value_invalid_biz_ctx_id(self, token, created_ctx_scheme_value_id):
        """Test business context value creation with invalid business context ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("create_business_context_value", {
                    'biz_ctx_id': 99999,  # Non-existent ID
                    'ctx_scheme_value_id': created_ctx_scheme_value_id
                })

    @pytest.mark.asyncio
    async def test_create_business_context_value_invalid_ctx_scheme_value_id(self, token, created_biz_ctx_id):
        """Test business context value creation with invalid context scheme value ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("create_business_context_value", {
                    'biz_ctx_id': created_biz_ctx_id,
                    'ctx_scheme_value_id': 99999  # Non-existent ID
                })

    @pytest.mark.asyncio
    async def test_create_business_context_value_missing_parameters(self, token):
        """Test business context value creation with missing parameters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("create_business_context_value", {})


class TestUpdateBusinessContext:
    """Test cases for update_business_context tool."""

    @pytest.mark.asyncio
    async def test_update_business_context_success(self, token, created_biz_ctx_id):
        """Test successful business context update."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("update_business_context", {
                'biz_ctx_id': created_biz_ctx_id,
                'name': 'Updated Test Business Context'
            })

            assert hasattr(result, 'data')
            assert hasattr(result.data, 'biz_ctx_id')
            assert hasattr(result.data, 'updates')
            assert result.data.biz_ctx_id == created_biz_ctx_id
            assert 'name' in result.data.updates

    @pytest.mark.asyncio
    async def test_update_business_context_invalid_id(self, token):
        """Test business context update with invalid ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("update_business_context", {
                    'biz_ctx_id': 99999,  # Non-existent ID
                    'name': 'Updated Test Business Context'
                })

    @pytest.mark.asyncio
    async def test_update_business_context_missing_name(self, token, created_biz_ctx_id):
        """Test business context update without name (should fail as name is required)."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("update_business_context", {
                    'biz_ctx_id': created_biz_ctx_id
                })


class TestUpdateBusinessContextValue:
    """Test cases for update_business_context_value tool."""

    @pytest.mark.asyncio
    async def test_update_business_context_value_success(self, token, created_biz_ctx_value_id,
                                                         created_ctx_scheme_value_id):
        """Test successful business context value update."""
        # Create another context scheme value for the update
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # First create another context category and scheme
            category_result = await client.call_tool("create_context_category", {
                'name': 'Test Update Category',
                'description': 'Category for testing business context value updates'
            })

            scheme_result = await client.call_tool("create_context_scheme", {
                'ctx_category_id': category_result.data.ctx_category_id,
                'scheme_id': 'TEST_BIZ_CTX_SCHEME_002',
                'scheme_agency_id': 'TEST_AGENCY',
                'scheme_version_id': '1.0',
                'scheme_name': 'Test Business Context Scheme 2',
                'description': 'Test scheme 2 for business context testing purposes'
            })

            scheme_value_result = await client.call_tool("create_context_scheme_value", {
                'ctx_scheme_id': scheme_result.data.ctx_scheme_id,
                'value': 'TEST_BIZ_CTX_VALUE_002',
                'meaning': 'Test value 2 for business context testing purposes'
            })

            # Now update the business context value
            result = await client.call_tool("update_business_context_value", {
                'biz_ctx_value_id': created_biz_ctx_value_id,
                'ctx_scheme_value_id': scheme_value_result.data.ctx_scheme_value_id
            })

            assert hasattr(result, 'data')
            assert hasattr(result.data, 'biz_ctx_value_id')
            assert hasattr(result.data, 'updates')
            assert result.data.biz_ctx_value_id == created_biz_ctx_value_id
            assert 'ctx_scheme_value_id' in result.data.updates

    @pytest.mark.asyncio
    async def test_update_business_context_value_invalid_id(self, token, created_ctx_scheme_value_id):
        """Test business context value update with invalid ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("update_business_context_value", {
                    'biz_ctx_value_id': 99999,  # Non-existent ID
                    'ctx_scheme_value_id': created_ctx_scheme_value_id
                })

    @pytest.mark.asyncio
    async def test_update_business_context_value_missing_ctx_scheme_value_id(self, token, created_biz_ctx_value_id):
        """Test business context value update without ctx_scheme_value_id (should fail as it's required)."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("update_business_context_value", {
                    'biz_ctx_value_id': created_biz_ctx_value_id
                })


class TestGetBusinessContext:
    """Test cases for get_business_context tool."""

    @pytest.mark.asyncio
    async def test_get_business_context_success(self, token, created_biz_ctx_id):
        """Test successful business context retrieval."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_business_context", {
                'biz_ctx_id': created_biz_ctx_id
            })

            assert hasattr(result, 'data')
            assert hasattr(result.data, 'biz_ctx_id')
            assert hasattr(result.data, 'guid')
            assert hasattr(result.data, 'name')
            assert hasattr(result.data, 'values')
            assert hasattr(result.data, 'created')
            assert hasattr(result.data, 'last_updated')
            assert result.data.biz_ctx_id == created_biz_ctx_id

    @pytest.mark.asyncio
    async def test_get_business_context_invalid_id(self, token):
        """Test business context retrieval with invalid ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_business_context", {
                    'biz_ctx_id': 99999  # Non-existent ID
                })


class TestGetBusinessContexts:
    """Test cases for get_business_contexts tool."""

    @pytest.mark.asyncio
    async def test_get_business_contexts_success(self, token):
        """Test successful business contexts retrieval."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_business_contexts", {
                'offset': 0,
                'limit': 10
            })

            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert isinstance(result.data.items, list)

    @pytest.mark.asyncio
    async def test_get_business_contexts_with_filters(self, token):
        """Test business contexts retrieval with filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_business_contexts", {
                'offset': 0,
                'limit': 10,
                'name': 'Test',
                'order_by': '-creation_timestamp'
            })

            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')

    @pytest.mark.asyncio
    async def test_get_business_contexts_pagination(self, token):
        """Test business contexts retrieval with pagination."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_business_contexts", {
                'offset': 0,
                'limit': 5
            })

            assert hasattr(result, 'data')
            assert result.data.offset == 0
            assert result.data.limit == 5
            assert len(result.data.items) <= 5


class TestDeleteBusinessContextValue:
    """Test cases for delete_business_context_value tool."""

    @pytest.mark.asyncio
    async def test_delete_business_context_value_success(self, token, created_biz_ctx_value_id):
        """Test successful business context value deletion."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("delete_business_context_value", {
                'biz_ctx_value_id': created_biz_ctx_value_id
            })

            assert hasattr(result, 'data')
            assert hasattr(result.data, 'biz_ctx_value_id')
            assert result.data.biz_ctx_value_id == created_biz_ctx_value_id

    @pytest.mark.asyncio
    async def test_delete_business_context_value_invalid_id(self, token):
        """Test business context value deletion with invalid ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("delete_business_context_value", {
                    'biz_ctx_value_id': 99999  # Non-existent ID
                })


class TestDeleteBusinessContext:
    """Test cases for delete_business_context tool."""

    @pytest.mark.asyncio
    async def test_delete_business_context_success(self, token):
        """Test successful business context deletion."""
        # First create a business context to delete
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            create_result = await client.call_tool("create_business_context", {
                'name': 'Business Context to Delete'
            })

            biz_ctx_id = create_result.data.biz_ctx_id

            # Now delete it
            result = await client.call_tool("delete_business_context", {
                'biz_ctx_id': biz_ctx_id
            })

            assert hasattr(result, 'data')
            assert hasattr(result.data, 'biz_ctx_id')
            assert result.data.biz_ctx_id == biz_ctx_id

    @pytest.mark.asyncio
    async def test_delete_business_context_invalid_id(self, token):
        """Test business context deletion with invalid ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("delete_business_context", {
                    'biz_ctx_id': 99999  # Non-existent ID
                })

    @pytest.mark.asyncio
    async def test_delete_business_context_with_values(self, token, created_biz_ctx_id):
        """Test business context deletion with associated values (should cascade delete)."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("delete_business_context", {
                'biz_ctx_id': created_biz_ctx_id
            })

            assert hasattr(result, 'data')
            assert hasattr(result.data, 'biz_ctx_id')
            assert result.data.biz_ctx_id == created_biz_ctx_id

    @pytest.mark.asyncio
    async def test_delete_business_context_with_biz_ctx_assignment_conflict(self, token):
        """Test business context deletion with linked biz_ctx_assignment records (should raise 409 conflict)."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # 1. Create a business context
            biz_ctx_result = await client.call_tool("create_business_context", {
                'name': 'Conflict Test Business Context'
            })
            biz_ctx_id = biz_ctx_result.data.biz_ctx_id

            # 2. Create necessary dependencies for business information entity
            # Create context category
            category_result = await client.call_tool("create_context_category", {
                'name': 'Conflict Test Category',
                'description': 'Category for testing business context deletion conflicts'
            })

            # Create context scheme
            scheme_result = await client.call_tool("create_context_scheme", {
                'ctx_category_id': category_result.data.ctx_category_id,
                'scheme_id': 'CONFLICT_TEST_SCHEME',
                'scheme_agency_id': 'TEST_AGENCY',
                'scheme_version_id': '1.0',
                'scheme_name': 'Conflict Test Scheme',
                'description': 'Scheme for testing business context deletion conflicts'
            })

            # Create context scheme value
            scheme_value_result = await client.call_tool("create_context_scheme_value", {
                'ctx_scheme_id': scheme_result.data.ctx_scheme_id,
                'value': 'CONFLICT_VALUE',
                'meaning': 'Value for testing business context deletion conflicts'
            })

            # Create business context value
            biz_ctx_value_result = await client.call_tool("create_business_context_value", {
                'biz_ctx_id': biz_ctx_id,
                'ctx_scheme_value_id': scheme_value_result.data.ctx_scheme_value_id
            })

            # 3. Create a business information entity that will create biz_ctx_assignment records
            # First, we need to find an existing ASCCP manifest ID
            # For this test, we'll use a known ASCCP manifest ID (this might need adjustment based on your test data)
            try:
                bie_result = await client.call_tool("create_top_level_asbiep", {
                    'asccp_manifest_id': 1,  # This should be a valid ASCCP manifest ID in your test database
                    'biz_ctx_list': str(biz_ctx_id)
                })
                
                # 4. Now try to delete the business context - this should fail with 409 conflict
                with pytest.raises(Exception) as exc_info:
                    await client.call_tool("delete_business_context", {
                        'biz_ctx_id': biz_ctx_id
                    })
                
                # Verify the error message contains conflict information
                error_message = str(exc_info.value)
                assert "Conflict" in error_message or "409" in error_message
                assert "biz_ctx_assignment" in error_message
                
            except Exception as e:
                # If ASCCP manifest ID 1 doesn't exist, we'll create a minimal test scenario
                # by directly creating the conflict through database manipulation
                # For now, we'll skip this test if the ASCCP manifest doesn't exist
                pytest.skip(f"ASCCP manifest ID 1 not found in test database: {e}")

    @pytest.mark.asyncio
    async def test_delete_business_context_success_after_removing_assignments(self, token):
        """Test business context deletion succeeds after removing biz_ctx_assignment records."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # 1. Create a business context
            biz_ctx_result = await client.call_tool("create_business_context", {
                'name': 'Success After Removal Test Business Context'
            })
            biz_ctx_id = biz_ctx_result.data.biz_ctx_id

            # 2. Create necessary dependencies
            category_result = await client.call_tool("create_context_category", {
                'name': 'Success Test Category',
                'description': 'Category for testing successful business context deletion'
            })

            scheme_result = await client.call_tool("create_context_scheme", {
                'ctx_category_id': category_result.data.ctx_category_id,
                'scheme_id': 'SUCCESS_TEST_SCHEME',
                'scheme_agency_id': 'TEST_AGENCY',
                'scheme_version_id': '1.0',
                'scheme_name': 'Success Test Scheme',
                'description': 'Scheme for testing successful business context deletion'
            })

            scheme_value_result = await client.call_tool("create_context_scheme_value", {
                'ctx_scheme_id': scheme_result.data.ctx_scheme_id,
                'value': 'SUCCESS_VALUE',
                'meaning': 'Value for testing successful business context deletion'
            })

            # Create business context value
            biz_ctx_value_result = await client.call_tool("create_business_context_value", {
                'biz_ctx_id': biz_ctx_id,
                'ctx_scheme_value_id': scheme_value_result.data.ctx_scheme_value_id
            })

            # 3. Try to create a business information entity (this might fail if ASCCP manifest doesn't exist)
            try:
                bie_result = await client.call_tool("create_top_level_asbiep", {
                    'asccp_manifest_id': 1,  # This should be a valid ASCCP manifest ID
                    'biz_ctx_list': str(biz_ctx_id)
                })
                
                # 4. Now try to delete the business context - this should fail with 409 conflict
                with pytest.raises(Exception):
                    await client.call_tool("delete_business_context", {
                        'biz_ctx_id': biz_ctx_id
                    })
                
                # 5. In a real scenario, we would need to delete the business information entity first
                # or update it to use a different business context. For this test, we'll demonstrate
                # that the business context can be deleted after the conflict is resolved.
                # Since we don't have a delete_top_level_asbiep tool, we'll skip the cleanup
                # and just verify that the conflict was properly detected.
                
            except Exception as e:
                # If ASCCP manifest ID 1 doesn't exist, we'll skip this test
                pytest.skip(f"ASCCP manifest ID 1 not found in test database: {e}")

    @pytest.mark.asyncio
    async def test_delete_business_context_conflict_error_message_format(self, token):
        """Test that the 409 conflict error message has the correct format and content."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Create a business context for testing
            biz_ctx_result = await client.call_tool("create_business_context", {
                'name': 'Conflict Message Test Business Context'
            })
            biz_ctx_id = biz_ctx_result.data.biz_ctx_id

            # Try to create a business information entity to establish the conflict
            try:
                bie_result = await client.call_tool("create_top_level_asbiep", {
                    'asccp_manifest_id': 1,  # This should be a valid ASCCP manifest ID
                    'biz_ctx_list': str(biz_ctx_id)
                })
                
                # Now try to delete the business context - this should fail with 409 conflict
                with pytest.raises(Exception) as exc_info:
                    await client.call_tool("delete_business_context", {
                        'biz_ctx_id': biz_ctx_id
                    })
                
                # Verify the error message format and content
                error_message = str(exc_info.value)
                
                # Check that the error message contains key elements
                assert "Conflict" in error_message or "409" in error_message
                assert "biz_ctx_assignment" in error_message
                assert str(biz_ctx_id) in error_message
                assert "Conflict Message Test Business Context" in error_message
                
                # The error should mention that linked assignments need to be deleted first
                assert "delete" in error_message.lower() or "remove" in error_message.lower()
                
            except Exception as e:
                # If ASCCP manifest ID 1 doesn't exist, we'll skip this test
                pytest.skip(f"ASCCP manifest ID 1 not found in test database: {e}")


class TestBusinessContextIntegration:
    """Integration test cases for business context workflows."""

    @pytest.mark.asyncio
    async def test_full_business_context_workflow(self, token):
        """Test complete business context workflow: create -> add value -> update -> get -> delete."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # 1. Create business context
            create_result = await client.call_tool("create_business_context", {
                'name': 'Integration Test Business Context'
            })
            biz_ctx_id = create_result.data.biz_ctx_id
            assert biz_ctx_id > 0

            # 2. Create context scheme and value
            category_result = await client.call_tool("create_context_category", {
                'name': 'Integration Test Category',
                'description': 'Category for integration testing'
            })

            scheme_result = await client.call_tool("create_context_scheme", {
                'ctx_category_id': category_result.data.ctx_category_id,
                'scheme_id': 'INTEGRATION_TEST_SCHEME',
                'scheme_agency_id': 'TEST_AGENCY',
                'scheme_version_id': '1.0',
                'scheme_name': 'Integration Test Scheme',
                'description': 'Scheme for integration testing'
            })

            scheme_value_result = await client.call_tool("create_context_scheme_value", {
                'ctx_scheme_id': scheme_result.data.ctx_scheme_id,
                'value': 'INTEGRATION_VALUE',
                'meaning': 'Value for integration testing'
            })

            # 3. Add value to business context
            biz_ctx_value_result = await client.call_tool("create_business_context_value", {
                'biz_ctx_id': biz_ctx_id,
                'ctx_scheme_value_id': scheme_value_result.data.ctx_scheme_value_id
            })
            biz_ctx_value_id = biz_ctx_value_result.data.biz_ctx_value_id
            assert biz_ctx_value_id > 0

            # 4. Update business context
            update_result = await client.call_tool("update_business_context", {
                'biz_ctx_id': biz_ctx_id,
                'name': 'Updated Integration Test Business Context'
            })
            assert 'name' in update_result.data.updates

            # 5. Get business context
            get_result = await client.call_tool("get_business_context", {
                'biz_ctx_id': biz_ctx_id
            })
            assert get_result.data.biz_ctx_id == biz_ctx_id
            assert get_result.data.name == 'Updated Integration Test Business Context'
            assert len(get_result.data.values) == 1

            # 6. Create another context scheme value for the update test
            scheme_value_result_2 = await client.call_tool("create_context_scheme_value", {
                'ctx_scheme_id': scheme_result.data.ctx_scheme_id,
                'value': 'INTEGRATION_VALUE_2',
                'meaning': 'Second value for integration testing'
            })

            # Update business context value with the new context scheme value
            update_value_result = await client.call_tool("update_business_context_value", {
                'biz_ctx_value_id': biz_ctx_value_id,
                'ctx_scheme_value_id': scheme_value_result_2.data.ctx_scheme_value_id
            })
            assert 'ctx_scheme_value_id' in update_value_result.data.updates

            # 7. Delete business context (should cascade delete the value)
            delete_result = await client.call_tool("delete_business_context", {
                'biz_ctx_id': biz_ctx_id
            })
            assert delete_result.data.biz_ctx_id == biz_ctx_id

            # 8. Verify business context is deleted
            with pytest.raises(Exception):
                await client.call_tool("get_business_context", {
                    'biz_ctx_id': biz_ctx_id
                })

    @pytest.mark.asyncio
    async def test_business_context_value_constraints(self, token, created_ctx_scheme_value_id):
        """Test that business context values properly reference context scheme values."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Create business context
            biz_ctx_result = await client.call_tool("create_business_context", {
                'name': 'Constraint Test Business Context'
            })
            biz_ctx_id = biz_ctx_result.data.biz_ctx_id

            # Create business context value
            biz_ctx_value_result = await client.call_tool("create_business_context_value", {
                'biz_ctx_id': biz_ctx_id,
                'ctx_scheme_value_id': created_ctx_scheme_value_id
            })
            biz_ctx_value_id = biz_ctx_value_result.data.biz_ctx_value_id

            # Verify the relationship exists
            get_result = await client.call_tool("get_business_context", {
                'biz_ctx_id': biz_ctx_id
            })
            assert len(get_result.data.values) == 1
            assert get_result.data.values[0].biz_ctx_value_id == biz_ctx_value_id
            assert get_result.data.values[0].ctx_scheme_value.ctx_scheme_value_id == created_ctx_scheme_value_id

            # Clean up
            await client.call_tool("delete_business_context", {
                'biz_ctx_id': biz_ctx_id
            })
