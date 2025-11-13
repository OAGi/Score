import asyncio
import pytest
from fastmcp import Client
from fastmcp.client import BearerAuth


class TestGetXbt:
    """Test cases for get_xbt tool."""
    
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
                # If no business contexts exist, we'll need to create one
                # For now, return None and tests will handle it
                return None
        return asyncio.run(_get_biz_ctx_id())
    
    @pytest.fixture
    def xbt_manifest_id_from_bbie(self, token, item_master_asccp_manifest_id, sample_business_context_id, release_10_12_id):
        """Create a BBIE and return its xbt_manifest_id from primitiveRestriction."""
        async def _get_xbt_manifest_id():
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                # Create a top-level ASBIEP
                create_result = await client.call_tool("create_top_level_asbiep", {
                    'asccp_manifest_id': item_master_asccp_manifest_id,
                    'biz_ctx_list': str(sample_business_context_id)
                })
                
                top_level_asbiep_id = create_result.data.top_level_asbiep_id
                assert top_level_asbiep_id is not None, "Top-level ASBIEP should be created"
                
                try:
                    # Get the top-level ASBIEP to find a BBIE relationship
                    get_result = await client.call_tool("get_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                    
                    # Find relationships - they can be ASBIE or BBIE (component_type field)
                    relationships = get_result.data.asbiep.role_of_abie.relationships
                    from_abie_id = get_result.data.asbiep.role_of_abie.abie_id
                    
                    # Find the first BBIE relationship (component_type == "BBIE")
                    first_bbie_rel = None
                    for rel in relationships:
                        # Check component_type - can be accessed as attribute or dict key
                        component_type = None
                        if isinstance(rel, dict):
                            component_type = rel.get('component_type')
                        elif hasattr(rel, 'component_type'):
                            component_type = rel.component_type
                        
                        if component_type == 'BBIE':
                            first_bbie_rel = rel
                            break
                    
                    if not first_bbie_rel:
                        assert False, (
                            f"No BBIE relationships found in Item Master ASCCP. "
                            f"Found {len(relationships)} total relationships, but none are BBIE type."
                        )
                    
                    # Get bcc_manifest_id from the first BBIE relationship
                    based_bcc = None
                    if isinstance(first_bbie_rel, dict):
                        based_bcc = first_bbie_rel.get('based_bcc')
                    elif hasattr(first_bbie_rel, 'based_bcc'):
                        based_bcc = first_bbie_rel.based_bcc
                    
                    if not based_bcc:
                        assert False, "BBIE relationship does not have based_bcc information."
                    
                    based_bcc_manifest_id = None
                    if isinstance(based_bcc, dict):
                        based_bcc_manifest_id = based_bcc.get('bcc_manifest_id')
                    elif hasattr(based_bcc, 'bcc_manifest_id'):
                        based_bcc_manifest_id = based_bcc.bcc_manifest_id
                    
                    if not based_bcc_manifest_id:
                        assert False, "Could not extract bcc_manifest_id from based_bcc."
                    
                    # Create the BBIE first (as required)
                    create_bbie_result = await client.call_tool("create_bbie", {
                        'from_abie_id': from_abie_id,
                        'based_bcc_manifest_id': based_bcc_manifest_id
                    })
                    
                    created_bbie_id = create_bbie_result.data.bbie_id
                    assert created_bbie_id is not None, "BBIE creation failed - bbie_id is None"
                    
                    # Get the created BBIE to extract xbt_manifest_id from primitiveRestriction
                    bbie_result = await client.call_tool("get_bbie_by_bbie_id", {
                        'bbie_id': created_bbie_id
                    })
                    
                    if (hasattr(bbie_result.data, 'primitiveRestriction') and 
                        bbie_result.data.primitiveRestriction and
                        bbie_result.data.primitiveRestriction.xbtManifestId is not None):
                        return bbie_result.data.primitiveRestriction.xbtManifestId
                    
                    # If xbtManifestId is not set, try to get default from get_bbie_by_based_bcc_manifest_id
                    bbie_info_result = await client.call_tool("get_bbie_by_based_bcc_manifest_id", {
                        'based_bcc_manifest_id': based_bcc_manifest_id
                    })
                    
                    if (hasattr(bbie_info_result.data, 'primitiveRestriction') and 
                        bbie_info_result.data.primitiveRestriction and
                        bbie_info_result.data.primitiveRestriction.xbtManifestId is not None):
                        return bbie_info_result.data.primitiveRestriction.xbtManifestId
                    
                    # Final failure
                    assert False, (
                        f"Created BBIE (bbie_id={created_bbie_id}) but could not find xbt_manifest_id. "
                        f"Neither the created BBIE nor the default BBIE info has xbtManifestId set."
                    )
                finally:
                    # Clean up
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
        
        return asyncio.run(_get_xbt_manifest_id())
    
    @pytest.fixture
    def xbt_manifest_id_from_bbie_sc(self, token, item_master_asccp_manifest_id, sample_business_context_id):
        """Create a BBIE_SC and return its xbt_manifest_id from primitiveRestriction."""
        async def _get_xbt_manifest_id():
            async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
                # Create a top-level ASBIEP
                create_result = await client.call_tool("create_top_level_asbiep", {
                    'asccp_manifest_id': item_master_asccp_manifest_id,
                    'biz_ctx_list': str(sample_business_context_id)
                })
                
                top_level_asbiep_id = create_result.data.top_level_asbiep_id
                assert top_level_asbiep_id is not None, "Top-level ASBIEP should be created"
                
                try:
                    # Get the top-level ASBIEP to find a BBIE relationship
                    get_result = await client.call_tool("get_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                    
                    # Find relationships - they can be ASBIE or BBIE (component_type field)
                    relationships = get_result.data.asbiep.role_of_abie.relationships
                    from_abie_id = get_result.data.asbiep.role_of_abie.abie_id
                    
                    # Find all BBIE relationships (component_type == "BBIE")
                    bbie_relationships = []
                    for rel in relationships:
                        # Check component_type - can be accessed as attribute or dict key
                        component_type = None
                        if isinstance(rel, dict):
                            component_type = rel.get('component_type')
                        elif hasattr(rel, 'component_type'):
                            component_type = rel.component_type
                        
                        if component_type == 'BBIE':
                            bbie_relationships.append(rel)
                    
                    if not bbie_relationships:
                        assert False, (
                            f"No BBIE relationships found in Item Master ASCCP. "
                            f"Found {len(relationships)} total relationships, but none are BBIE type."
                        )
                    
                    # Try each BBIE relationship until we find one with supplementary components that have xbt_manifest_id
                    for bbie_rel in bbie_relationships:
                        # Get bcc_manifest_id from the BBIE relationship
                        based_bcc = None
                        if isinstance(bbie_rel, dict):
                            based_bcc = bbie_rel.get('based_bcc')
                        elif hasattr(bbie_rel, 'based_bcc'):
                            based_bcc = bbie_rel.based_bcc
                        
                        if not based_bcc:
                            continue
                        
                        based_bcc_manifest_id = None
                        if isinstance(based_bcc, dict):
                            based_bcc_manifest_id = based_bcc.get('bcc_manifest_id')
                        elif hasattr(based_bcc, 'bcc_manifest_id'):
                            based_bcc_manifest_id = based_bcc.bcc_manifest_id
                        
                        if not based_bcc_manifest_id:
                            continue
                        
                        # Create the BBIE first (as required)
                        create_bbie_result = await client.call_tool("create_bbie", {
                            'from_abie_id': from_abie_id,
                            'based_bcc_manifest_id': based_bcc_manifest_id
                        })
                        
                        bbie_id = create_bbie_result.data.bbie_id
                        assert bbie_id is not None, "BBIE creation failed - bbie_id is None"
                        
                        # Get the BBIE to find supplementary components
                        bbie_result = await client.call_tool("get_bbie_by_bbie_id", {
                            'bbie_id': bbie_id
                        })
                        
                        # Check if there are supplementary components
                        supp_components = None
                        if (hasattr(bbie_result.data, 'to_bbiep') and 
                            hasattr(bbie_result.data.to_bbiep, 'supplementary_components')):
                            supp_components = bbie_result.data.to_bbiep.supplementary_components
                        
                        # If no supplementary components, try next BBIE relationship
                        if not supp_components or len(supp_components) == 0:
                            continue
                        
                        # First, check if any existing supplementary components have xbt_manifest_id
                        for supp in supp_components:
                            if (hasattr(supp, 'primitiveRestriction') and 
                                supp.primitiveRestriction and
                                supp.primitiveRestriction.xbtManifestId is not None):
                                return supp.primitiveRestriction.xbtManifestId
                        
                        # If no xbt_manifest_id found, create a BBIE_SC from the first supplementary component
                        for supp in supp_components:
                            if hasattr(supp, 'based_dt_sc') and supp.based_dt_sc:
                                based_dt_sc_manifest_id = None
                                if isinstance(supp.based_dt_sc, dict):
                                    based_dt_sc_manifest_id = supp.based_dt_sc.get('dt_sc_manifest_id')
                                elif hasattr(supp.based_dt_sc, 'dt_sc_manifest_id'):
                                    based_dt_sc_manifest_id = supp.based_dt_sc.dt_sc_manifest_id
                                
                                if not based_dt_sc_manifest_id:
                                    continue
                                
                                # Create BBIE_SC
                                create_bbie_sc_result = await client.call_tool("create_bbie_sc", {
                                    'bbie_id': bbie_id,
                                    'based_dt_sc_manifest_id': based_dt_sc_manifest_id
                                })
                                
                                bbie_sc_id = create_bbie_sc_result.data.bbie_sc_id
                                assert bbie_sc_id is not None, "BBIE_SC creation failed - bbie_sc_id is None"
                                
                                # Get the BBIE again to check the created BBIE_SC's primitiveRestriction
                                bbie_result_after = await client.call_tool("get_bbie_by_bbie_id", {
                                    'bbie_id': bbie_id
                                })
                                
                                # Find the created BBIE_SC
                                supp_after = None
                                if (hasattr(bbie_result_after.data, 'to_bbiep') and 
                                    hasattr(bbie_result_after.data.to_bbiep, 'supplementary_components')):
                                    for sc in bbie_result_after.data.to_bbiep.supplementary_components:
                                        sc_id = None
                                        if isinstance(sc, dict):
                                            sc_id = sc.get('bbie_sc_id')
                                        elif hasattr(sc, 'bbie_sc_id'):
                                            sc_id = sc.bbie_sc_id
                                        
                                        if sc_id == bbie_sc_id:
                                            supp_after = sc
                                            break
                                
                                if supp_after:
                                    primitive_restriction = None
                                    if isinstance(supp_after, dict):
                                        primitive_restriction = supp_after.get('primitiveRestriction')
                                    elif hasattr(supp_after, 'primitiveRestriction'):
                                        primitive_restriction = supp_after.primitiveRestriction
                                    
                                    if primitive_restriction:
                                        xbt_manifest_id = None
                                        if isinstance(primitive_restriction, dict):
                                            xbt_manifest_id = primitive_restriction.get('xbtManifestId')
                                        elif hasattr(primitive_restriction, 'xbtManifestId'):
                                            xbt_manifest_id = primitive_restriction.xbtManifestId
                                        
                                        if xbt_manifest_id is not None:
                                            return xbt_manifest_id
                                
                                # Try next supplementary component if this one didn't work
                                break
                    
                    # Final failure - tried all BBIE relationships but none had supplementary components with xbt_manifest_id
                    assert False, (
                        f"Tried {len(bbie_relationships)} BBIE relationships but could not find one with supplementary components "
                        f"that have xbt_manifest_id. Either no supplementary components exist or none have xbtManifestId set."
                    )
                finally:
                    # Clean up
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
        
        return asyncio.run(_get_xbt_manifest_id())
    
    @pytest.mark.asyncio
    async def test_get_xbt_success_from_bbie(self, token, xbt_manifest_id_from_bbie):
        """Test successful XBT retrieval using xbt_manifest_id from BBIE."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_xbt", {
                'xbt_manifest_id': xbt_manifest_id_from_bbie
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'xbt_manifest_id')
            assert result.data.xbt_manifest_id == xbt_manifest_id_from_bbie
            assert hasattr(result.data, 'xbt_id')
            assert hasattr(result.data, 'guid')
            assert hasattr(result.data, 'name')
            assert hasattr(result.data, 'builtIn_type')
            assert hasattr(result.data, 'jbt_draft05_map')
            assert hasattr(result.data, 'openapi30_map')
            assert hasattr(result.data, 'avro_map')
            assert hasattr(result.data, 'subtype_of_xbt')
            assert hasattr(result.data, 'schema_definition')
            assert hasattr(result.data, 'revision_doc')
            assert hasattr(result.data, 'state')
            assert hasattr(result.data, 'is_deprecated')
            assert hasattr(result.data, 'library')
            assert hasattr(result.data, 'release')
            assert hasattr(result.data, 'log')
            assert hasattr(result.data, 'owner')
            assert hasattr(result.data, 'created')
            assert hasattr(result.data, 'last_updated')
            
            # Verify required fields are not None
            assert result.data.xbt_id is not None
            assert result.data.guid is not None
            assert result.data.library is not None
            assert result.data.release is not None
            assert result.data.owner is not None
            assert result.data.created is not None
            assert result.data.last_updated is not None
    
    @pytest.mark.asyncio
    async def test_get_xbt_success_from_bbie_sc(self, token, xbt_manifest_id_from_bbie_sc):
        """Test successful XBT retrieval using xbt_manifest_id from BBIE_SC."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_xbt", {
                'xbt_manifest_id': xbt_manifest_id_from_bbie_sc
            })
            
            # Verify the result structure
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'xbt_manifest_id')
            assert result.data.xbt_manifest_id == xbt_manifest_id_from_bbie_sc
            assert hasattr(result.data, 'xbt_id')
            assert hasattr(result.data, 'guid')
            assert hasattr(result.data, 'name')
            assert hasattr(result.data, 'builtIn_type')
            assert hasattr(result.data, 'library')
            assert hasattr(result.data, 'release')
            assert hasattr(result.data, 'owner')
            assert hasattr(result.data, 'created')
            assert hasattr(result.data, 'last_updated')
            
            # Verify required fields are not None
            assert result.data.xbt_id is not None
            assert result.data.guid is not None
            assert result.data.library is not None
            assert result.data.release is not None
            assert result.data.owner is not None
            assert result.data.created is not None
            assert result.data.last_updated is not None
    
    @pytest.mark.asyncio
    async def test_get_xbt_not_found(self, token):
        """Test XBT retrieval with non-existent ID."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception) as exc_info:
                await client.call_tool("get_xbt", {
                    'xbt_manifest_id': 999999
                })
            
            # Should raise a ToolError
            assert "not found" in str(exc_info.value).lower()
    
    @pytest.mark.asyncio
    async def test_get_xbt_invalid_id(self, token):
        """Test XBT retrieval with invalid ID (zero or negative)."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            with pytest.raises(Exception):
                await client.call_tool("get_xbt", {
                    'xbt_manifest_id': 0
                })
    
    @pytest.mark.asyncio
    async def test_get_xbt_with_subtype(self, token, xbt_manifest_id_from_bbie):
        """Test XBT retrieval when XBT has a subtype relationship."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_xbt", {
                'xbt_manifest_id': xbt_manifest_id_from_bbie
            })
            
            # Verify basic structure first
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'subtype_of_xbt')
            
            # If subtype_of_xbt exists, verify its structure
            if result.data.subtype_of_xbt is not None:
                assert hasattr(result.data.subtype_of_xbt, 'xbt_manifest_id')
                assert hasattr(result.data.subtype_of_xbt, 'xbt_id')
                assert hasattr(result.data.subtype_of_xbt, 'guid')
                assert hasattr(result.data.subtype_of_xbt, 'name')
                assert hasattr(result.data.subtype_of_xbt, 'builtIn_type')
                assert hasattr(result.data.subtype_of_xbt, 'library')
                assert hasattr(result.data.subtype_of_xbt, 'release')
                
                # Verify subtype fields are not None
                assert result.data.subtype_of_xbt.xbt_id is not None
                assert result.data.subtype_of_xbt.guid is not None
                assert result.data.subtype_of_xbt.library is not None
                assert result.data.subtype_of_xbt.release is not None
            # Note: It's valid for an XBT to not have a subtype, so we don't fail the test if subtype_of_xbt is None
    
    @pytest.mark.asyncio
    async def test_get_xbt_response_fields(self, token, xbt_manifest_id_from_bbie):
        """Test that all response fields are properly populated."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_xbt", {
                'xbt_manifest_id': xbt_manifest_id_from_bbie
            })
            
            # Verify library structure
            assert result.data.library.library_id is not None
            assert result.data.library.name is not None
            
            # Verify release structure
            assert result.data.release.release_id is not None
            assert result.data.release.state is not None
            
            # Verify owner structure
            assert result.data.owner.user_id is not None
            assert result.data.owner.login_id is not None
            assert result.data.owner.username is not None
            assert isinstance(result.data.owner.roles, list)
            
            # Verify created structure
            assert result.data.created.who is not None
            assert result.data.created.when is not None
            assert result.data.created.who.user_id is not None
            
            # Verify last_updated structure
            assert result.data.last_updated.who is not None
            assert result.data.last_updated.when is not None
            assert result.data.last_updated.who.user_id is not None
            
            # Verify log structure if present
            if result.data.log is not None:
                assert result.data.log.log_id is not None
                assert result.data.log.revision_num is not None
                assert result.data.log.revision_tracking_num is not None

