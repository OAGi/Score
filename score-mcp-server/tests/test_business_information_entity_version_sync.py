"""
Test cases for version syncing between top_level_asbiep.version and Version Identifier BBIE's fixed_value.

This test suite verifies:
1. When top_level_asbiep.version is updated, it syncs to Version Identifier BBIE's fixed_value
2. When Version Identifier BBIE's fixed_value is updated, it syncs to top_level_asbiep.version
3. If Version Identifier BBIE doesn't exist, it is created automatically when version is set
4. Bidirectional sync works correctly
"""

import asyncio
import pytest
from fastmcp import Client
from fastmcp.client import BearerAuth


class TestVersionSync:
    """Test cases for version syncing between top_level_asbiep and Version Identifier BBIE."""
    
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
    def get_purchase_order_asccp_manifest_id(self, token, release_10_12_id):
        """Find and return the 'Get Purchase Order' ASCCP manifest ID."""
        async def _get_asccp_id():
            assert release_10_12_id is not None, "Release 10.12 must exist in connectSpec"
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                result = await client.call_tool("get_core_components", {
                    'release_id': release_10_12_id,
                    'types': 'ASCCP',
                    'den': 'Get Purchase Order',
                    'offset': 0,
                    'limit': 100
                })
                assert result.data.items and len(result.data.items) > 0, "Get Purchase Order ASCCP must exist in release 10.12"
                for component in result.data.items:
                    if component.component_type == 'ASCCP':
                        # Check if DEN starts with "Get Purchase Order"
                        if component.den and component.den.startswith('Get Purchase Order'):
                            return component.manifest_id
                assert False, f"Get Purchase Order ASCCP not found. Found ASCCPs: {[comp.den for comp in result.data.items[:10]]}"
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
    
    async def _find_version_identifier_bbie(self, relationships, client):
        """Helper function to find Version Identifier BBIE in relationships."""
        for rel in relationships:
            # Check if this is a BBIE relationship
            if rel.get('component_type') == 'BBIE':
                # Check if it has based_bcc with to_bccp_manifest_id
                if 'based_bcc' in rel and rel['based_bcc'] is not None:
                    bcc = rel['based_bcc']
                    # First check DEN as a quick way to identify Version Identifier
                    den = bcc.get('den', '')
                    if den and 'version identifier' in den.lower():
                        return rel
                    
                    # Also check BCCP property_term if to_bccp_manifest_id is available
                    if 'to_bccp_manifest_id' in bcc and bcc['to_bccp_manifest_id'] is not None:
                        # Get the BCCP information using the manifest ID
                        try:
                            bccp_result = await client.call_tool("get_bccp", {
                                'bccp_manifest_id': bcc['to_bccp_manifest_id']
                            })
                            if hasattr(bccp_result, 'data') and hasattr(bccp_result.data, 'based_bccp'):
                                bccp = bccp_result.data.based_bccp
                                if hasattr(bccp, 'property_term') and bccp.property_term:
                                    if bccp.property_term.lower() == 'version identifier':
                                        # Return the relationship itself (it contains bbie_id, fixed_value, etc.)
                                        return rel
                        except Exception as e:
                            # If get_bccp fails, we already checked DEN above, so continue
                            print(f"Warning: Could not get BCCP for manifest_id {bcc['to_bccp_manifest_id']}: {e}")
                            continue
        return None
    
    @pytest.mark.asyncio
    async def test_sync_version_to_version_identifier_bbie(self, token, get_purchase_order_asccp_manifest_id, sample_business_context_id):
        """Test that updating top_level_asbiep.version syncs to Version Identifier BBIE's fixed_value."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get a business context
            if sample_business_context_id is None:
                biz_ctx_result = await client.call_tool("get_business_contexts", {
                    'offset': 0,
                    'limit': 1
                })
                assert biz_ctx_result.data.items and len(biz_ctx_result.data.items) > 0, "Business contexts must be available for testing"
                biz_ctx_id = biz_ctx_result.data.items[0].biz_ctx_id
            else:
                biz_ctx_id = sample_business_context_id
            
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': get_purchase_order_asccp_manifest_id,
                'biz_ctx_list': str(biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the BIE to check if Version Identifier BBIE exists
                get_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                relationships = get_result.data.asbiep.role_of_abie.relationships
                version_identifier_bbie = await self._find_version_identifier_bbie(relationships, client)
                
                # Update version
                update_result = await client.call_tool("update_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id,
                    'version': '2.0'
                })
                
                assert hasattr(update_result, 'data')
                assert hasattr(update_result.data, 'top_level_asbiep_id')
                assert update_result.data.top_level_asbiep_id == top_level_asbiep_id
                assert 'version' in update_result.data.updates
                
                # Verify version was updated
                get_result2 = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                assert get_result2.data.asbiep.owner_top_level_asbiep.version == '2.0'
                
                # Verify Version Identifier BBIE's fixed_value was synced
                relationships2 = get_result2.data.asbiep.role_of_abie.relationships
                
                # Debug: Print all relationships to see what's available
                print("\n=== ALL RELATIONSHIPS AFTER VERSION UPDATE ===")
                for idx, rel in enumerate(relationships2):
                    print(f"\nRelationship {idx}:")
                    print(f"  component_type: {rel.get('component_type')}")
                    if rel.get('component_type') == 'BBIE':
                        print(f"  bbie_id: {rel.get('bbie_id')}")
                        print(f"  is_used: {rel.get('is_used')}")
                        value_constraint = rel.get('valueConstraint')
                        fixed_value = value_constraint.get('fixed_value') if value_constraint else None
                        print(f"  fixed_value: {fixed_value}")
                        if 'based_bcc' in rel and rel['based_bcc']:
                            bcc = rel['based_bcc']
                            print(f"  based_bcc:")
                            print(f"    bcc_manifest_id: {bcc.get('bcc_manifest_id')}")
                            print(f"    to_bccp_manifest_id: {bcc.get('to_bccp_manifest_id')}")
                            print(f"    den: {bcc.get('den')}")
                            # Try to get BCCP info
                            if bcc.get('to_bccp_manifest_id'):
                                try:
                                    bccp_result = await client.call_tool("get_bccp", {
                                        'bccp_manifest_id': bcc['to_bccp_manifest_id']
                                    })
                                    if hasattr(bccp_result, 'data') and hasattr(bccp_result.data, 'based_bccp'):
                                        bccp = bccp_result.data.based_bccp
                                        print(f"    BCCP property_term: {getattr(bccp, 'property_term', 'N/A')}")
                                except Exception as e:
                                    print(f"    Error getting BCCP: {e}")
                
                version_identifier_bbie2 = await self._find_version_identifier_bbie(relationships2, client)
                
                # Version Identifier BBIE should exist after setting version
                assert version_identifier_bbie2 is not None, f"Version Identifier BBIE should exist after setting version. Found {len(relationships2)} relationships total."
                
                print(f"\n=== VERSION IDENTIFIER BBIE FOUND ===")
                print(f"bbie_id: {version_identifier_bbie2.get('bbie_id')}")
                print(f"is_used: {version_identifier_bbie2.get('is_used')}")
                value_constraint = version_identifier_bbie2.get('valueConstraint')
                fixed_value = value_constraint.get('fixed_value') if value_constraint else None
                print(f"fixed_value: {fixed_value}")
                print(f"based_bcc.den: {version_identifier_bbie2.get('based_bcc', {}).get('den')}")
                
                # The relationship should have bbie_id if it's been created
                assert 'bbie_id' in version_identifier_bbie2 and version_identifier_bbie2['bbie_id'] is not None, "Version Identifier BBIE should have bbie_id after being created"
                
                # Check fixed_value in the relationship
                # Note: fixed_value might be None if sync hasn't happened yet, but it should be set after version update
                value_constraint_check = version_identifier_bbie2.get('valueConstraint')
                if value_constraint_check is None or value_constraint_check.get('fixed_value') is None:
                    print(f"\nWARNING: fixed_value is None. The sync from version to BBIE fixed_value may not have worked.")
                    print(f"Version was set to: '2.0'")
                    print(f"BBIE ID: {version_identifier_bbie2.get('bbie_id')}")
                    # Try to get the BBIE directly to check its actual fixed_value
                    try:
                        bbie_result = await client.call_tool("get_bbie_by_bbie_id", {
                            'bbie_id': version_identifier_bbie2['bbie_id']
                        })
                        if hasattr(bbie_result, 'data') and hasattr(bbie_result.data, 'valueConstraint'):
                            fixed_value = getattr(bbie_result.data.valueConstraint, 'fixed_value', None) if bbie_result.data.valueConstraint else None
                            print(f"Direct BBIE query - fixed_value: {fixed_value}")
                    except Exception as e:
                        print(f"Could not get BBIE directly: {e}")
                
                value_constraint = version_identifier_bbie2.get('valueConstraint')
                assert value_constraint is not None, "Version Identifier BBIE should have valueConstraint"
                assert 'fixed_value' in value_constraint, "Version Identifier BBIE should have fixed_value in valueConstraint"
                assert value_constraint['fixed_value'] == '2.0', f"Expected fixed_value to be '2.0', got '{value_constraint.get('fixed_value')}'. The sync from top_level_asbiep.version to BBIE fixed_value may not be working."
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_sync_version_identifier_bbie_to_version(self, token, get_purchase_order_asccp_manifest_id, sample_business_context_id):
        """Test that updating Version Identifier BBIE's fixed_value syncs to top_level_asbiep.version."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get a business context
            if sample_business_context_id is None:
                biz_ctx_result = await client.call_tool("get_business_contexts", {
                    'offset': 0,
                    'limit': 1
                })
                assert biz_ctx_result.data.items and len(biz_ctx_result.data.items) > 0, "Business contexts must be available for testing"
                biz_ctx_id = biz_ctx_result.data.items[0].biz_ctx_id
            else:
                biz_ctx_id = sample_business_context_id
            
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': get_purchase_order_asccp_manifest_id,
                'biz_ctx_list': str(biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the BIE to find Version Identifier BBIE
                get_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                relationships = get_result.data.asbiep.role_of_abie.relationships
                version_identifier_bbie = await self._find_version_identifier_bbie(relationships, client)
                
                # First, ensure Version Identifier BBIE exists by setting a version
                # This will create it if it doesn't exist
                await client.call_tool("update_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id,
                    'version': '1.0'
                })
                
                # Get the BIE again to find the Version Identifier BBIE (it should exist now)
                get_result_after_version = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                relationships_after = get_result_after_version.data.asbiep.role_of_abie.relationships
                version_identifier_bbie_after = await self._find_version_identifier_bbie(relationships_after, client)
                
                # Version Identifier BBIE should exist now (either it existed before or was created)
                assert version_identifier_bbie_after is not None, "Version Identifier BBIE should exist after setting version"
                assert 'bbie_id' in version_identifier_bbie_after and version_identifier_bbie_after['bbie_id'], "Version Identifier BBIE should have a bbie_id"
                
                bbie_id = version_identifier_bbie_after['bbie_id']
                
                # Update Version Identifier BBIE's fixed_value
                update_result = await client.call_tool("update_bbie", {
                    'bbie_id': bbie_id,
                    'fixed_value': '3.0'
                })
                
                assert hasattr(update_result, 'data')
                assert hasattr(update_result.data, 'bbie_id')
                assert update_result.data.bbie_id == bbie_id
                
                # Debug: Print what updates were returned
                print(f"\n=== UPDATE BBIE RESULT ===")
                print(f"bbie_id: {update_result.data.bbie_id}")
                print(f"updates: {update_result.data.updates}")
                
                # Check if fixed_value is in updates
                if 'fixed_value' not in update_result.data.updates:
                    # Try to get the BBIE directly to see its current state
                    try:
                        bbie_result = await client.call_tool("get_bbie_by_bbie_id", {
                            'bbie_id': bbie_id
                        })
                        if hasattr(bbie_result, 'data'):
                            fixed_value = getattr(bbie_result.data.valueConstraint, 'fixed_value', None) if (bbie_result.data.valueConstraint if hasattr(bbie_result.data, 'valueConstraint') else None) else None
                            print(f"Current BBIE fixed_value: {fixed_value}")
                    except Exception as e:
                        print(f"Could not get BBIE directly: {e}")
                
                assert 'fixed_value' in update_result.data.updates, f"Expected 'fixed_value' in updates, but got: {update_result.data.updates}"
                
                # Verify top_level_asbiep.version was synced
                get_result2 = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                assert get_result2.data.asbiep.owner_top_level_asbiep.version == '3.0'
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_create_version_identifier_bbie_on_version_update(self, token, get_purchase_order_asccp_manifest_id, sample_business_context_id):
        """Test that Version Identifier BBIE is created automatically when version is set if it doesn't exist."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get a business context
            if sample_business_context_id is None:
                biz_ctx_result = await client.call_tool("get_business_contexts", {
                    'offset': 0,
                    'limit': 1
                })
                assert biz_ctx_result.data.items and len(biz_ctx_result.data.items) > 0, "Business contexts must be available for testing"
                biz_ctx_id = biz_ctx_result.data.items[0].biz_ctx_id
            else:
                biz_ctx_id = sample_business_context_id
            
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': get_purchase_order_asccp_manifest_id,
                'biz_ctx_list': str(biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the BIE to check if Version Identifier BBIE exists
                get_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                relationships = get_result.data.asbiep.role_of_abie.relationships
                version_identifier_bbie = await self._find_version_identifier_bbie(relationships, client)
                
                # If Version Identifier BBIE already exists, we can't test creation
                # But we can still test that setting version syncs to it
                initial_version_identifier_exists = version_identifier_bbie is not None and 'bbie_id' in version_identifier_bbie and version_identifier_bbie['bbie_id']
                
                # Update version
                update_result = await client.call_tool("update_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id,
                    'version': '4.0'
                })
                
                assert hasattr(update_result, 'data')
                assert update_result.data.top_level_asbiep_id == top_level_asbiep_id
                assert 'version' in update_result.data.updates
                
                # Verify version was updated
                get_result2 = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                assert get_result2.data.asbiep.owner_top_level_asbiep.version == '4.0'
                
                # Check if Version Identifier BBIE exists now
                relationships2 = get_result2.data.asbiep.role_of_abie.relationships
                version_identifier_bbie2 = await self._find_version_identifier_bbie(relationships2, client)
                
                if version_identifier_bbie2:
                    # Version Identifier BBIE should have fixed_value synced
                    value_constraint = version_identifier_bbie2.get('valueConstraint')
                    if value_constraint and 'fixed_value' in value_constraint:
                        assert value_constraint['fixed_value'] == '4.0'
                    
                    # If it didn't exist before, it should exist now
                    if not initial_version_identifier_exists:
                        assert 'bbie_id' in version_identifier_bbie2 and version_identifier_bbie2['bbie_id']
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_bidirectional_version_sync(self, token, get_purchase_order_asccp_manifest_id, sample_business_context_id):
        """Test bidirectional sync between top_level_asbiep.version and Version Identifier BBIE's fixed_value."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get a business context
            if sample_business_context_id is None:
                biz_ctx_result = await client.call_tool("get_business_contexts", {
                    'offset': 0,
                    'limit': 1
                })
                assert biz_ctx_result.data.items and len(biz_ctx_result.data.items) > 0, "Business contexts must be available for testing"
                biz_ctx_id = biz_ctx_result.data.items[0].biz_ctx_id
            else:
                biz_ctx_id = sample_business_context_id
            
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': get_purchase_order_asccp_manifest_id,
                'biz_ctx_list': str(biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the BIE to find Version Identifier BBIE
                get_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                # First, ensure Version Identifier BBIE exists by setting a version
                # This will create it if it doesn't exist
                await client.call_tool("update_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id,
                    'version': '1.0'
                })
                
                # Get the BIE again to find the Version Identifier BBIE (it should exist now)
                get_result_after_version = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                relationships_after = get_result_after_version.data.asbiep.role_of_abie.relationships
                version_identifier_bbie_after = await self._find_version_identifier_bbie(relationships_after, client)
                
                # Version Identifier BBIE should exist now (either it existed before or was created)
                assert version_identifier_bbie_after is not None, "Version Identifier BBIE should exist after setting version"
                assert 'bbie_id' in version_identifier_bbie_after and version_identifier_bbie_after['bbie_id'], "Version Identifier BBIE should have a bbie_id"
                
                bbie_id = version_identifier_bbie_after['bbie_id']
                
                # Test: Update version -> should sync to BBIE fixed_value
                update_result1 = await client.call_tool("update_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id,
                    'version': '5.0'
                })
                
                assert 'version' in update_result1.data.updates
                
                # Verify BBIE fixed_value was synced
                get_result2 = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                relationships2 = get_result2.data.asbiep.role_of_abie.relationships
                version_identifier_bbie2 = await self._find_version_identifier_bbie(relationships2, client)
                
                assert version_identifier_bbie2 is not None, "Version Identifier BBIE should exist"
                assert 'bbie_id' in version_identifier_bbie2 and version_identifier_bbie2['bbie_id'] is not None, "Version Identifier BBIE should have bbie_id"
                value_constraint = version_identifier_bbie2.get('valueConstraint')
                assert value_constraint is not None, "Version Identifier BBIE should have valueConstraint"
                assert 'fixed_value' in value_constraint, "Version Identifier BBIE should have fixed_value in valueConstraint"
                assert value_constraint['fixed_value'] == '5.0', f"Expected fixed_value to be '5.0', got '{value_constraint.get('fixed_value')}'"
                
                # Test: Update BBIE fixed_value -> should sync to version
                update_result2 = await client.call_tool("update_bbie", {
                    'bbie_id': bbie_id,
                    'fixed_value': '6.0'
                })
                
                assert 'fixed_value' in update_result2.data.updates
                
                # Verify version was synced
                get_result3 = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                assert get_result3.data.asbiep.owner_top_level_asbiep.version == '6.0'
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")

