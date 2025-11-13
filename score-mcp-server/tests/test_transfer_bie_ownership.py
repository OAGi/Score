import asyncio
import json
import pytest
from fastmcp import Client
from fastmcp.client import BearerAuth


@pytest.fixture
def created_release_id(token):
    """Get an existing release ID for testing."""
    # Since we only have read-only tools, we'll use a hardcoded ID
    # In a real scenario, you would create a release first or use an existing one
    return 1


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
def valid_asccp_manifest_id(token, created_release_id):
    """Get a valid ASCCP manifest ID for testing."""
    
    async def _get_asccp_manifest_id():
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get ASCCP manifests from the release
            result = await client.call_tool("get_core_components", {
                'release_id': created_release_id,
                'types': 'ASCCP',
                'limit': 1
            })
            
            if result.data.items:
                return result.data.items[0].manifest_id
            else:
                pytest.skip("No ASCCP manifests found in the database")
    
    # Run the async function and return the result
    import asyncio
    return asyncio.run(_get_asccp_manifest_id())


def extract_content(result):
    """Extract text content from result.content."""
    if hasattr(result, 'content') and result.content:
        if isinstance(result.content, list) and len(result.content) > 0:
            return result.content[0].text
        elif hasattr(result.content, 'text'):
            return result.content.text
    return str(result.content)


class TestTransferBieOwnership:
    """Test cases for transfer_top_level_asbiep_ownership tool."""

    @pytest.mark.asyncio
    async def test_transfer_top_level_asbiep_ownership_success(self, token, created_biz_ctx_id, valid_asccp_manifest_id, temp_end_user_for_transfer):
        """Test successful ownership transfer."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Create a business information entity
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': valid_asccp_manifest_id,  # Use dynamically found ASCCP manifest ID
                'biz_ctx_list': str(created_biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            # Transfer ownership to the temporary End-User created by the fixture
            new_owner_user_id = temp_end_user_for_transfer['user_id']
            transfer_result = await client.call_tool("transfer_top_level_asbiep_ownership", {
                'top_level_asbiep_id': top_level_asbiep_id,
                'new_owner_user_id': new_owner_user_id
            })
            
            # Verify transfer was successful
            assert transfer_result.data.top_level_asbiep_id == top_level_asbiep_id
            assert transfer_result.data.updates == ["owner_user_id"]
            
            # Verify the BIE now has the new owner
            get_result = await client.call_tool("get_top_level_asbiep", {
                'top_level_asbiep_id': top_level_asbiep_id
            })
            
            assert get_result.data.owner.user_id == new_owner_user_id

    @pytest.mark.asyncio
    async def test_transfer_top_level_asbiep_ownership_invalid_bie_id(self, token):
        """Test transfer ownership with invalid BIE ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Try to transfer ownership of non-existent BIE
            with pytest.raises(Exception) as exc_info:
                await client.call_tool("transfer_top_level_asbiep_ownership", {
                    'top_level_asbiep_id': 99999,
                    'new_owner_user_id': 100000013
                })
            
            # Should get a not found error
            assert "not found" in str(exc_info.value).lower() or "404" in str(exc_info.value)

    @pytest.mark.asyncio
    async def test_transfer_top_level_asbiep_ownership_invalid_user_id(self, token, created_biz_ctx_id, valid_asccp_manifest_id):
        """Test transfer ownership with invalid user ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Create a business information entity
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': valid_asccp_manifest_id,
                'biz_ctx_list': str(created_biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            # Cleanup the created BIE first
            try:
                await client.call_tool("delete_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
            except Exception as e:
                print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
            
            # Try to transfer to non-existent user (using a very large user_id that won't exist)
            with pytest.raises(Exception) as exc_info:
                await client.call_tool("transfer_top_level_asbiep_ownership", {
                    'top_level_asbiep_id': top_level_asbiep_id,
                    'new_owner_user_id': 999999999
                })
            
            # Should get a not found error
            assert "not found" in str(exc_info.value).lower() or "404" in str(exc_info.value)

    @pytest.mark.asyncio
    async def test_transfer_top_level_asbiep_ownership_role_mismatch(self, token, created_biz_ctx_id, valid_asccp_manifest_id):
        """Test transfer ownership with role mismatch (End-User to Developer)."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Create a business information entity
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': valid_asccp_manifest_id,
                'biz_ctx_list': str(created_biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            # Try to transfer from End-User to Developer (should fail due to role mismatch)
            with pytest.raises(Exception) as exc_info:
                await client.call_tool("transfer_top_level_asbiep_ownership", {
                    'top_level_asbiep_id': top_level_asbiep_id,
                    'new_owner_user_id': 1  # This is a Developer user
                })
            
            # Should get a role mismatch error
            assert "role mismatch" in str(exc_info.value).lower() or "validation error" in str(exc_info.value).lower()

    @pytest.mark.asyncio
    async def test_transfer_top_level_asbiep_ownership_to_self(self, token, created_biz_ctx_id, valid_asccp_manifest_id):
        """Test transfer ownership to self (should fail)."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get current user ID
            current_user_result = await client.call_tool("who_am_i", {})
            current_user_id = current_user_result.data.user_id
            
            # Create a business information entity
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': valid_asccp_manifest_id,
                'biz_ctx_list': str(created_biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Try to transfer to self (should fail)
                with pytest.raises(Exception) as exc_info:
                    await client.call_tool("transfer_top_level_asbiep_ownership", {
                        'top_level_asbiep_id': top_level_asbiep_id,
                        'new_owner_user_id': current_user_id
                    })
                
                # Should get a validation error
                assert "cannot transfer ownership to yourself" in str(exc_info.value).lower() or "validation error" in str(exc_info.value).lower()
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")

    @pytest.mark.asyncio
    async def test_transfer_top_level_asbiep_ownership_response_format(self, token, created_biz_ctx_id, valid_asccp_manifest_id, temp_end_user_for_transfer):
        """Test that transfer ownership returns the correct response format."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Create a business information entity
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': valid_asccp_manifest_id,
                'biz_ctx_list': str(created_biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            # Transfer ownership to the temporary End-User created by the fixture
            new_owner_user_id = temp_end_user_for_transfer['user_id']
            transfer_result = await client.call_tool("transfer_top_level_asbiep_ownership", {
                'top_level_asbiep_id': top_level_asbiep_id,
                'new_owner_user_id': new_owner_user_id
            })
            
            # Verify response format
            assert hasattr(transfer_result, 'data')
            assert hasattr(transfer_result.data, 'top_level_asbiep_id')
            assert hasattr(transfer_result.data, 'updates')
            
            # Verify response content
            assert transfer_result.data.top_level_asbiep_id == top_level_asbiep_id
            assert isinstance(transfer_result.data.updates, list)
            assert "owner_user_id" in transfer_result.data.updates
            
            # Verify no extra fields are present
            response_dict = transfer_result.data.__dict__
            expected_keys = {'top_level_asbiep_id', 'updates'}
            actual_keys = set(response_dict.keys())
            assert actual_keys == expected_keys, f"Unexpected fields in response: {actual_keys - expected_keys}"

    @pytest.mark.asyncio
    async def test_transfer_top_level_asbiep_ownership_unauthorized(self, token, created_biz_ctx_id, valid_asccp_manifest_id, temp_end_user_for_transfer):
        """Test transfer ownership without permission (should fail)."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Create a business information entity
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': valid_asccp_manifest_id,
                'biz_ctx_list': str(created_biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            # First transfer ownership to the temporary End-User
            new_owner_user_id = temp_end_user_for_transfer['user_id']
            await client.call_tool("transfer_top_level_asbiep_ownership", {
                'top_level_asbiep_id': top_level_asbiep_id,
                'new_owner_user_id': new_owner_user_id
            })
            
            # Now try to transfer again (should fail since we're no longer the owner)
            with pytest.raises(Exception) as exc_info:
                await client.call_tool("transfer_top_level_asbiep_ownership", {
                    'top_level_asbiep_id': top_level_asbiep_id,
                    'new_owner_user_id': new_owner_user_id  # Same user as current owner
                })
            
            # Should get an access denied error or validation error
            error_msg = str(exc_info.value).lower()
            assert ("access denied" in error_msg or 
                   "not the owner" in error_msg or 
                   "cannot transfer ownership to yourself" in error_msg or
                   "validation error" in error_msg)
