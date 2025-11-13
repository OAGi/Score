"""
Test cases for value constraint rules (default_value and fixed_value) in BBIE and BBIE_SC.

This test suite verifies:
1. If default_value and fixed_value are not provided, they won't change (existing values remain)
2. If default_value and fixed_value are set to None, they should be updated to None (clearing)
3. If CC has fixed_value, BIE's fixed_value should be cascaded on creation
4. If CC's fixed_value is set, BIE's fixed_value cannot be changed
5. If CC has default_value, BIE's default_value should be set during creation, but it can be changed
"""

import asyncio
import pytest
from fastmcp import Client
from fastmcp.client import BearerAuth


class TestValueConstraintRules:
    """Test cases for value constraint rules (default_value and fixed_value)."""
    
    @pytest.fixture
    def connectspec_library_id(self, token):
        """Find and return the connectSpec library ID."""
        async def _get_library_id():
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                result = await client.call_tool("get_libraries", {
                    'name': 'connectSpec',
                    'offset': 0,
                    'limit': 10
                })
                assert result.data.items and len(result.data.items) > 0, "connectSpec library must exist"
                for lib in result.data.items:
                    if lib.name == 'connectSpec':
                        return lib.library_id
                assert False, f"connectSpec library not found. Found libraries: {[lib.name for lib in result.data.items]}"
        return asyncio.run(_get_library_id())
    
    @pytest.fixture
    def release_10_12_id(self, token, connectspec_library_id):
        """Find and return the release ID for connectSpec 10.12."""
        async def _get_release_id():
            assert connectspec_library_id is not None, "connectSpec library must exist"
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                result = await client.call_tool("get_releases", {
                    'library_id': connectspec_library_id,
                    'release_num': '10.12',
                    'offset': 0,
                    'limit': 10
                })
                assert result.data.items and len(result.data.items) > 0, "Release 10.12 must exist in connectSpec"
                for release in result.data.items:
                    if release.release_num == '10.12':
                        return release.release_id
                assert False, f"Release 10.12 not found. Found releases: {[r.release_num for r in result.data.items]}"
        return asyncio.run(_get_release_id())
    
    @pytest.fixture
    def item_master_asccp_manifest_id(self, token, release_10_12_id):
        """Find and return the Item Master ASCCP manifest ID."""
        async def _get_asccp_id():
            assert release_10_12_id is not None, "Release 10.12 must exist in connectSpec"
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                result = await client.call_tool("get_core_components", {
                    'release_id': release_10_12_id,
                    'types': 'ASCCP',
                    'den': 'Item Master',
                    'offset': 0,
                    'limit': 100
                })
                assert result.data.items and len(result.data.items) > 0, "Item Master ASCCP must exist in release 10.12"
                for component in result.data.items:
                    if component.component_type == 'ASCCP' and 'Item Master' in component.den:
                        return component.manifest_id
                assert False, f"Item Master ASCCP not found. Found ASCCPs: {[comp.den for comp in result.data.items[:10]]}"
        return asyncio.run(_get_asccp_id())
    
    @pytest.fixture
    def sample_business_context_id(self, token):
        """Find or create a sample business context for testing."""
        async def _get_biz_ctx_id():
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                # Try to get an existing business context
                result = await client.call_tool("get_business_contexts", {
                    'offset': 0,
                    'limit': 10
                })
                if result.data.items and len(result.data.items) > 0:
                    return result.data.items[0].biz_ctx_id
                return None
        return asyncio.run(_get_biz_ctx_id())
    
    @pytest.mark.asyncio
    async def test_update_bbie_not_provided_values_dont_change(
        self, token, item_master_asccp_manifest_id, sample_business_context_id
    ):
        """Test that if default_value and fixed_value are not provided, existing values don't change."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(sample_business_context_id) if sample_business_context_id else ''
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the top-level ASBIEP to find BCC relationships
                top_level_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                # Find a BCC relationship
                relationships = top_level_result.data.asbiep.role_of_abie.relationships
                bcc_relationships = [r for r in relationships if r.get('component_type') == 'BCC']
                
                if len(bcc_relationships) > 0:
                    first_bcc = bcc_relationships[0]
                    from_abie_id = top_level_result.data.asbiep.role_of_abie.abie_id
                    based_bcc_manifest_id = first_bcc['bcc_manifest_id']
                    
                    # Create the BBIE
                    create_bbie_result = await client.call_tool("create_bbie", {
                        'from_abie_id': from_abie_id,
                        'based_bcc_manifest_id': based_bcc_manifest_id
                    })
                    
                    bbie_id = create_bbie_result.data.bbie_id
                    
                    # Get the BBIE to check initial values
                    initial_bbie = await client.call_tool("get_bbie_by_bbie_id", {
                        'bbie_id': bbie_id
                    })
                    
                    initial_default_value = getattr(initial_bbie.data.valueConstraint, 'default_value', None) if initial_bbie.data.valueConstraint else None
                    initial_fixed_value = getattr(initial_bbie.data.valueConstraint, 'fixed_value', None) if initial_bbie.data.valueConstraint else None
                    
                    # Set some values first
                    await client.call_tool("update_bbie", {
                        'bbie_id': bbie_id,
                        'default_value': 'test_default'
                    })
                    
                    # Verify the value was set
                    after_set = await client.call_tool("get_bbie_by_bbie_id", {
                        'bbie_id': bbie_id
                    })
                    assert (getattr(after_set.data.valueConstraint, 'default_value', None) if after_set.data.valueConstraint else None) == 'test_default'
                    
                    # Update without providing default_value or fixed_value (not in the call)
                    # This simulates not providing the parameters
                    await client.call_tool("update_bbie", {
                        'bbie_id': bbie_id,
                        'definition': 'Updated definition'
                    })
                    
                    # Verify default_value didn't change (wasn't provided)
                    after_update = await client.call_tool("get_bbie_by_bbie_id", {
                        'bbie_id': bbie_id
                    })
                    assert (getattr(after_update.data.valueConstraint, 'default_value', None) if after_update.data.valueConstraint else None) == 'test_default', \
                        "default_value should not change when not provided"
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_update_bbie_explicitly_none_clears_values(
        self, token, item_master_asccp_manifest_id, sample_business_context_id
    ):
        """Test that if default_value and fixed_value are set to None, they should be updated to None (clearing)."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(sample_business_context_id) if sample_business_context_id else ''
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the top-level ASBIEP to find BCC relationships
                top_level_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                # Find a BCC relationship
                relationships = top_level_result.data.asbiep.role_of_abie.relationships
                bcc_relationships = [r for r in relationships if r.get('component_type') == 'BCC']
                
                if len(bcc_relationships) > 0:
                    first_bcc = bcc_relationships[0]
                    from_abie_id = top_level_result.data.asbiep.role_of_abie.abie_id
                    based_bcc_manifest_id = first_bcc['bcc_manifest_id']
                    
                    # Create the BBIE
                    create_bbie_result = await client.call_tool("create_bbie", {
                        'from_abie_id': from_abie_id,
                        'based_bcc_manifest_id': based_bcc_manifest_id
                    })
                    
                    bbie_id = create_bbie_result.data.bbie_id
                    
                    # Set some values first
                    await client.call_tool("update_bbie", {
                        'bbie_id': bbie_id,
                        'default_value': 'test_default'
                    })
                    
                    # Verify the value was set
                    after_set = await client.call_tool("get_bbie_by_bbie_id", {
                        'bbie_id': bbie_id
                    })
                    assert (getattr(after_set.data.valueConstraint, 'default_value', None) if after_set.data.valueConstraint else None) == 'test_default'
                    
                    # Explicitly set default_value to None to clear it
                    await client.call_tool("update_bbie", {
                        'bbie_id': bbie_id,
                        'default_value': None
                    })
                    
                    # Verify default_value was cleared
                    after_clear = await client.call_tool("get_bbie_by_bbie_id", {
                        'bbie_id': bbie_id
                    })
                    assert (getattr(after_clear.data.valueConstraint, 'default_value', None) if after_clear.data.valueConstraint else None) is None, \
                        "default_value should be cleared when explicitly set to None"
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_create_bbie_cascades_default_value_from_bcc(
        self, token, item_master_asccp_manifest_id, sample_business_context_id
    ):
        """Test that if BCC has default_value, BBIE's default_value should be set during creation."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(sample_business_context_id) if sample_business_context_id else ''
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the top-level ASBIEP to find BCC relationships
                top_level_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                # Find a BCC relationship that has default_value
                relationships = top_level_result.data.asbiep.role_of_abie.relationships
                bcc_relationships = [r for r in relationships if r.get('component_type') == 'BCC']
                
                # Look for a BCC with default_value
                bcc_with_default = None
                for bcc in bcc_relationships:
                    value_constraint = bcc.get('valueConstraint')
                    if value_constraint and value_constraint.get('default_value') is not None:
                        bcc_with_default = bcc
                        break
                
                if bcc_with_default:
                    from_abie_id = top_level_result.data.asbiep.role_of_abie.abie_id
                    based_bcc_manifest_id = bcc_with_default['bcc_manifest_id']
                    expected_default_value = bcc_with_default.get('valueConstraint', {}).get('default_value')
                    
                    # Create the BBIE
                    create_bbie_result = await client.call_tool("create_bbie", {
                        'from_abie_id': from_abie_id,
                        'based_bcc_manifest_id': based_bcc_manifest_id
                    })
                    
                    bbie_id = create_bbie_result.data.bbie_id
                    
                    # Verify default_value was cascaded from BCC
                    bbie_result = await client.call_tool("get_bbie_by_bbie_id", {
                        'bbie_id': bbie_id
                    })
                    
                    actual_default_value = getattr(bbie_result.data.valueConstraint, 'default_value', None) if bbie_result.data.valueConstraint else None
                    assert actual_default_value == expected_default_value, \
                        f"default_value should be cascaded from BCC. Expected: {expected_default_value}, Got: {actual_default_value}"
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_create_bbie_cascades_fixed_value_from_bcc(
        self, token, item_master_asccp_manifest_id, sample_business_context_id
    ):
        """Test that if BCC has fixed_value, BBIE's fixed_value should be cascaded on creation."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(sample_business_context_id) if sample_business_context_id else ''
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the top-level ASBIEP to find BCC relationships
                top_level_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                # Find a BCC relationship that has fixed_value
                relationships = top_level_result.data.asbiep.role_of_abie.relationships
                bcc_relationships = [r for r in relationships if r.get('component_type') == 'BCC']
                
                # Look for a BCC with fixed_value
                bcc_with_fixed = None
                for bcc in bcc_relationships:
                    value_constraint = bcc.get('valueConstraint')
                    if value_constraint and value_constraint.get('fixed_value') is not None:
                        bcc_with_fixed = bcc
                        break
                
                if bcc_with_fixed:
                    from_abie_id = top_level_result.data.asbiep.role_of_abie.abie_id
                    based_bcc_manifest_id = bcc_with_fixed['bcc_manifest_id']
                    expected_fixed_value = bcc_with_fixed.get('valueConstraint', {}).get('fixed_value')
                    
                    # Create the BBIE
                    create_bbie_result = await client.call_tool("create_bbie", {
                        'from_abie_id': from_abie_id,
                        'based_bcc_manifest_id': based_bcc_manifest_id
                    })
                    
                    bbie_id = create_bbie_result.data.bbie_id
                    
                    # Verify fixed_value was cascaded from BCC
                    bbie_result = await client.call_tool("get_bbie_by_bbie_id", {
                        'bbie_id': bbie_id
                    })
                    
                    actual_fixed_value = getattr(bbie_result.data.valueConstraint, 'fixed_value', None) if bbie_result.data.valueConstraint else None
                    assert actual_fixed_value == expected_fixed_value, \
                        f"fixed_value should be cascaded from BCC. Expected: {expected_fixed_value}, Got: {actual_fixed_value}"
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_update_bbie_cannot_change_fixed_value_when_bcc_has_fixed_value(
        self, token, item_master_asccp_manifest_id, sample_business_context_id
    ):
        """Test that if BCC's fixed_value is set, BBIE's fixed_value cannot be changed."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(sample_business_context_id) if sample_business_context_id else ''
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the top-level ASBIEP to find BCC relationships
                top_level_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                # Find a BCC relationship that has fixed_value
                relationships = top_level_result.data.asbiep.role_of_abie.relationships
                bcc_relationships = [r for r in relationships if r.get('component_type') == 'BCC']
                
                # Look for a BCC with fixed_value
                bcc_with_fixed = None
                for bcc in bcc_relationships:
                    value_constraint = bcc.get('valueConstraint')
                    if value_constraint and value_constraint.get('fixed_value') is not None:
                        bcc_with_fixed = bcc
                        break
                
                if bcc_with_fixed:
                    from_abie_id = top_level_result.data.asbiep.role_of_abie.abie_id
                    based_bcc_manifest_id = bcc_with_fixed['bcc_manifest_id']
                    expected_fixed_value = bcc_with_fixed.get('valueConstraint', {}).get('fixed_value')
                    
                    # Create the BBIE
                    create_bbie_result = await client.call_tool("create_bbie", {
                        'from_abie_id': from_abie_id,
                        'based_bcc_manifest_id': based_bcc_manifest_id
                    })
                    
                    bbie_id = create_bbie_result.data.bbie_id
                    
                    # Try to change fixed_value - should fail
                    with pytest.raises(Exception) as exc_info:
                        await client.call_tool("update_bbie", {
                            'bbie_id': bbie_id,
                            'fixed_value': 'different_value'
                        })
                    
                    # Verify the error message mentions fixed_value constraint
                    error_message = str(exc_info.value)
                    assert 'fixed_value' in error_message.lower() or 'fixed value' in error_message.lower(), \
                        f"Error should mention fixed_value constraint. Got: {error_message}"
                    
                    # Verify fixed_value is still the original (cascaded) value
                    bbie_result = await client.call_tool("get_bbie_by_bbie_id", {
                        'bbie_id': bbie_id
                    })
                    
                    actual_fixed_value = getattr(bbie_result.data.valueConstraint, 'fixed_value', None) if bbie_result.data.valueConstraint else None
                    assert actual_fixed_value == expected_fixed_value, \
                        f"fixed_value should not change. Expected: {expected_fixed_value}, Got: {actual_fixed_value}"
                    
                    # Try to clear fixed_value - should also fail
                    with pytest.raises(Exception) as exc_info2:
                        await client.call_tool("update_bbie", {
                            'bbie_id': bbie_id,
                            'fixed_value': None
                        })
                    
                    # Verify the error message mentions clearing is not allowed
                    error_message2 = str(exc_info2.value)
                    assert 'clear' in error_message2.lower() or 'cannot' in error_message2.lower(), \
                        f"Error should mention that clearing is not allowed. Got: {error_message2}"
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_update_bbie_can_change_default_value_even_when_bcc_has_default_value(
        self, token, item_master_asccp_manifest_id, sample_business_context_id
    ):
        """Test that if BCC has default_value, BBIE's default_value can be changed."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(sample_business_context_id) if sample_business_context_id else ''
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the top-level ASBIEP to find BCC relationships
                top_level_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                # Find a BCC relationship that has default_value
                relationships = top_level_result.data.asbiep.role_of_abie.relationships
                bcc_relationships = [r for r in relationships if r.get('component_type') == 'BCC']
                
                # Look for a BCC with default_value
                bcc_with_default = None
                for bcc in bcc_relationships:
                    value_constraint = bcc.get('valueConstraint')
                    if value_constraint and value_constraint.get('default_value') is not None:
                        bcc_with_default = bcc
                        break
                
                if bcc_with_default:
                    from_abie_id = top_level_result.data.asbiep.role_of_abie.abie_id
                    based_bcc_manifest_id = bcc_with_default['bcc_manifest_id']
                    original_default_value = bcc_with_default.get('valueConstraint', {}).get('default_value')
                    
                    # Create the BBIE
                    create_bbie_result = await client.call_tool("create_bbie", {
                        'from_abie_id': from_abie_id,
                        'based_bcc_manifest_id': based_bcc_manifest_id
                    })
                    
                    bbie_id = create_bbie_result.data.bbie_id
                    
                    # Verify default_value was cascaded from BCC
                    initial_bbie = await client.call_tool("get_bbie_by_bbie_id", {
                        'bbie_id': bbie_id
                    })
                    assert (getattr(initial_bbie.data.valueConstraint, 'default_value', None) if initial_bbie.data.valueConstraint else None) == original_default_value
                    
                    # Change default_value to a different value - should succeed
                    new_default_value = 'custom_default_value'
                    await client.call_tool("update_bbie", {
                        'bbie_id': bbie_id,
                        'default_value': new_default_value
                    })
                    
                    # Verify default_value was changed
                    updated_bbie = await client.call_tool("get_bbie_by_bbie_id", {
                        'bbie_id': bbie_id
                    })
                    assert (getattr(updated_bbie.data.valueConstraint, 'default_value', None) if updated_bbie.data.valueConstraint else None) == new_default_value, \
                        f"default_value should be changeable. Expected: {new_default_value}, Got: {(getattr(updated_bbie.data.valueConstraint, 'default_value', None) if updated_bbie.data.valueConstraint else None)}"
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_update_bbie_sc_not_provided_values_dont_change(
        self, token, item_master_asccp_manifest_id, sample_business_context_id
    ):
        """Test that if default_value and fixed_value are not provided for BBIE_SC, existing values don't change."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(sample_business_context_id) if sample_business_context_id else ''
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the top-level ASBIEP to find BCC relationships
                top_level_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                # Find a BCC relationship
                relationships = top_level_result.data.asbiep.role_of_abie.relationships
                bcc_relationships = [r for r in relationships if r.get('component_type') == 'BCC']
                
                if len(bcc_relationships) > 0:
                    first_bcc = bcc_relationships[0]
                    from_abie_id = top_level_result.data.asbiep.role_of_abie.abie_id
                    based_bcc_manifest_id = first_bcc['bcc_manifest_id']
                    
                    # Create the BBIE
                    create_bbie_result = await client.call_tool("create_bbie", {
                        'from_abie_id': from_abie_id,
                        'based_bcc_manifest_id': based_bcc_manifest_id
                    })
                    
                    bbie_id = create_bbie_result.data.bbie_id
                    
                    # Get the BBIE to find supplementary components
                    bbie_result = await client.call_tool("get_bbie_by_bbie_id", {
                        'bbie_id': bbie_id
                    })
                    
                    if hasattr(bbie_result.data, 'to_bbiep') and hasattr(bbie_result.data.to_bbiep, 'supplementary_components'):
                        supp_components = bbie_result.data.to_bbiep.supplementary_components
                        
                        if supp_components and len(supp_components) > 0:
                            first_supp = supp_components[0]
                            based_dt_sc_manifest_id = first_supp.based_dt_sc.dt_sc_manifest_id
                            
                            # Create BBIE_SC
                            create_bbie_sc_result = await client.call_tool("create_bbie_sc", {
                                'bbie_id': bbie_id,
                                'based_dt_sc_manifest_id': based_dt_sc_manifest_id
                            })
                            
                            bbie_sc_id = create_bbie_sc_result.data.bbie_sc_id
                            
                            # Set some values first
                            await client.call_tool("update_bbie_sc", {
                                'bbie_sc_id': bbie_sc_id,
                                'default_value': 'test_default_sc'
                            })
                            
                            # Verify the value was set
                            after_set = await client.call_tool("get_bbie_by_bbie_id", {
                                'bbie_id': bbie_id
                            })
                            supp_after_set = [sc for sc in after_set.data.to_bbiep.supplementary_components 
                                            if hasattr(sc, 'bbie_sc_id') and sc.bbie_sc_id == bbie_sc_id]
                            if supp_after_set:
                                supp_value_constraint = getattr(supp_after_set[0], 'valueConstraint', None)
                                assert (getattr(supp_value_constraint, 'default_value', None) if supp_value_constraint else None) == 'test_default_sc'
                            
                            # Update without providing default_value or fixed_value
                            await client.call_tool("update_bbie_sc", {
                                'bbie_sc_id': bbie_sc_id,
                                'definition': 'Updated definition'
                            })
                            
                            # Verify default_value didn't change
                            after_update = await client.call_tool("get_bbie_by_bbie_id", {
                                'bbie_id': bbie_id
                            })
                            supp_after_update = [sc for sc in after_update.data.to_bbiep.supplementary_components 
                                                if hasattr(sc, 'bbie_sc_id') and sc.bbie_sc_id == bbie_sc_id]
                            if supp_after_update:
                                supp_value_constraint = getattr(supp_after_update[0], 'valueConstraint', None)
                                assert (getattr(supp_value_constraint, 'default_value', None) if supp_value_constraint else None) == 'test_default_sc', \
                                    "default_value should not change when not provided"
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_update_bbie_sc_explicitly_none_clears_values(
        self, token, item_master_asccp_manifest_id, sample_business_context_id
    ):
        """Test that if default_value and fixed_value are set to None for BBIE_SC, they should be cleared."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(sample_business_context_id) if sample_business_context_id else ''
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the top-level ASBIEP to find BCC relationships
                top_level_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                # Find a BCC relationship
                relationships = top_level_result.data.asbiep.role_of_abie.relationships
                bcc_relationships = [r for r in relationships if r.get('component_type') == 'BCC']
                
                if len(bcc_relationships) > 0:
                    first_bcc = bcc_relationships[0]
                    from_abie_id = top_level_result.data.asbiep.role_of_abie.abie_id
                    based_bcc_manifest_id = first_bcc['bcc_manifest_id']
                    
                    # Create the BBIE
                    create_bbie_result = await client.call_tool("create_bbie", {
                        'from_abie_id': from_abie_id,
                        'based_bcc_manifest_id': based_bcc_manifest_id
                    })
                    
                    bbie_id = create_bbie_result.data.bbie_id
                    
                    # Get the BBIE to find supplementary components
                    bbie_result = await client.call_tool("get_bbie_by_bbie_id", {
                        'bbie_id': bbie_id
                    })
                    
                    if hasattr(bbie_result.data, 'to_bbiep') and hasattr(bbie_result.data.to_bbiep, 'supplementary_components'):
                        supp_components = bbie_result.data.to_bbiep.supplementary_components
                        
                        if supp_components and len(supp_components) > 0:
                            first_supp = supp_components[0]
                            based_dt_sc_manifest_id = first_supp.based_dt_sc.dt_sc_manifest_id
                            
                            # Create BBIE_SC
                            create_bbie_sc_result = await client.call_tool("create_bbie_sc", {
                                'bbie_id': bbie_id,
                                'based_dt_sc_manifest_id': based_dt_sc_manifest_id
                            })
                            
                            bbie_sc_id = create_bbie_sc_result.data.bbie_sc_id
                            
                            # Set some values first
                            await client.call_tool("update_bbie_sc", {
                                'bbie_sc_id': bbie_sc_id,
                                'default_value': 'test_default_sc'
                            })
                            
                            # Verify the value was set
                            after_set = await client.call_tool("get_bbie_by_bbie_id", {
                                'bbie_id': bbie_id
                            })
                            supp_after_set = [sc for sc in after_set.data.to_bbiep.supplementary_components 
                                            if hasattr(sc, 'bbie_sc_id') and sc.bbie_sc_id == bbie_sc_id]
                            if supp_after_set:
                                supp_value_constraint = getattr(supp_after_set[0], 'valueConstraint', None)
                                assert (getattr(supp_value_constraint, 'default_value', None) if supp_value_constraint else None) == 'test_default_sc'
                            
                            # Explicitly set default_value to None to clear it
                            await client.call_tool("update_bbie_sc", {
                                'bbie_sc_id': bbie_sc_id,
                                'default_value': None
                            })
                            
                            # Verify default_value was cleared
                            after_clear = await client.call_tool("get_bbie_by_bbie_id", {
                                'bbie_id': bbie_id
                            })
                            supp_after_clear = [sc for sc in after_clear.data.to_bbiep.supplementary_components 
                                              if hasattr(sc, 'bbie_sc_id') and sc.bbie_sc_id == bbie_sc_id]
                            if supp_after_clear:
                                supp_value_constraint = getattr(supp_after_clear[0], 'valueConstraint', None)
                                assert (getattr(supp_value_constraint, 'default_value', None) if supp_value_constraint else None) is None, \
                                    "default_value should be cleared when explicitly set to None"
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_update_bbie_sc_cannot_change_fixed_value_when_dt_sc_has_fixed_value(
        self, token, item_master_asccp_manifest_id, sample_business_context_id
    ):
        """Test that if DT_SC's fixed_value is set, BBIE_SC's fixed_value cannot be changed."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(sample_business_context_id) if sample_business_context_id else ''
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the top-level ASBIEP to find BCC relationships
                top_level_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                # Find a BCC relationship
                relationships = top_level_result.data.asbiep.role_of_abie.relationships
                bcc_relationships = [r for r in relationships if r.get('component_type') == 'BCC']
                
                if len(bcc_relationships) > 0:
                    first_bcc = bcc_relationships[0]
                    from_abie_id = top_level_result.data.asbiep.role_of_abie.abie_id
                    based_bcc_manifest_id = first_bcc['bcc_manifest_id']
                    
                    # Create the BBIE
                    create_bbie_result = await client.call_tool("create_bbie", {
                        'from_abie_id': from_abie_id,
                        'based_bcc_manifest_id': based_bcc_manifest_id
                    })
                    
                    bbie_id = create_bbie_result.data.bbie_id
                    
                    # Get the BBIE to find supplementary components
                    bbie_result = await client.call_tool("get_bbie_by_bbie_id", {
                        'bbie_id': bbie_id
                    })
                    
                    if hasattr(bbie_result.data, 'to_bbiep') and hasattr(bbie_result.data.to_bbiep, 'supplementary_components'):
                        supp_components = bbie_result.data.to_bbiep.supplementary_components
                        
                        # Look for a supplementary component with fixed_value in DT_SC
                        supp_with_fixed = None
                        for supp in supp_components:
                            if hasattr(supp, 'based_dt_sc') and supp.based_dt_sc and \
                               hasattr(supp.based_dt_sc, 'value_constraint') and supp.based_dt_sc.value_constraint and \
                               supp.based_dt_sc.value_constraint.fixed_value is not None:
                                supp_with_fixed = supp
                                break
                        
                        if supp_with_fixed:
                            based_dt_sc_manifest_id = supp_with_fixed.based_dt_sc.dt_sc_manifest_id
                            expected_fixed_value = supp_with_fixed.based_dt_sc.value_constraint.fixed_value
                            
                            # Create BBIE_SC
                            create_bbie_sc_result = await client.call_tool("create_bbie_sc", {
                                'bbie_id': bbie_id,
                                'based_dt_sc_manifest_id': based_dt_sc_manifest_id
                            })
                            
                            bbie_sc_id = create_bbie_sc_result.data.bbie_sc_id
                            
                            # Verify fixed_value was cascaded from DT_SC
                            after_create = await client.call_tool("get_bbie_by_bbie_id", {
                                'bbie_id': bbie_id
                            })
                            supp_after_create = [sc for sc in after_create.data.to_bbiep.supplementary_components 
                                                if hasattr(sc, 'bbie_sc_id') and sc.bbie_sc_id == bbie_sc_id]
                            if supp_after_create:
                                supp_value_constraint = getattr(supp_after_create[0], 'valueConstraint', None)
                                assert (getattr(supp_value_constraint, 'fixed_value', None) if supp_value_constraint else None) == expected_fixed_value
                            
                            # Try to change fixed_value - should fail
                            with pytest.raises(Exception) as exc_info:
                                await client.call_tool("update_bbie_sc", {
                                    'bbie_sc_id': bbie_sc_id,
                                    'fixed_value': 'different_value'
                                })
                            
                            # Verify the error message mentions fixed_value constraint
                            error_message = str(exc_info.value)
                            assert 'fixed_value' in error_message.lower() or 'fixed value' in error_message.lower(), \
                                f"Error should mention fixed_value constraint. Got: {error_message}"
                            
                            # Try to clear fixed_value - should also fail
                            with pytest.raises(Exception) as exc_info2:
                                await client.call_tool("update_bbie_sc", {
                                    'bbie_sc_id': bbie_sc_id,
                                    'fixed_value': None
                                })
                            
                            # Verify the error message mentions clearing is not allowed
                            error_message2 = str(exc_info2.value)
                            assert 'clear' in error_message2.lower() or 'cannot' in error_message2.lower(), \
                                f"Error should mention that clearing is not allowed. Got: {error_message2}"
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")

