import asyncio
import pytest
from fastmcp import Client
from fastmcp.client import BearerAuth


class TestBusinessInformationEntity:
    """Test cases for Business Information Entity MCP tools using 'Item Master' ASCCP in connectSpec 10.12."""
    
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
    
    @pytest.mark.asyncio
    async def test_get_top_level_asbiep_list_basic(self, token, release_10_12_id):
        """Test basic get_top_level_asbiep_list with default parameters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_top_level_asbiep_list", {
                'offset': 0,
                'limit': 10
            })
            
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            assert result.data.offset == 0
            assert result.data.limit == 10
            assert isinstance(result.data.items, list)
            assert len(result.data.items) <= 10
    
    @pytest.mark.asyncio
    async def test_get_top_level_asbiep_list_with_filters(self, token, release_10_12_id, connectspec_library_id):
        """Test get_top_level_asbiep_list with various filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Test with release_id_list filter (library_id may not be supported in all cases)
            result_release = await client.call_tool("get_top_level_asbiep_list", {
                'release_id_list': str(release_10_12_id),
                'offset': 0,
                'limit': 10
            })
            
            assert result_release.data.total_items >= 0
            
            # Test with DEN filter
            result_den = await client.call_tool("get_top_level_asbiep_list", {
                'den': 'Item',
                'offset': 0,
                'limit': 10
            })
            
            assert result_den.data.total_items >= 0
            # Verify all returned items have "Item" in DEN or display_name (case-insensitive)
            for item in result_den.data.items:
                den_match = 'Item' in item.den or 'item' in item.den.lower() if item.den else False
                display_match = 'Item' in item.display_name or 'item' in item.display_name.lower() if item.display_name else False
                assert den_match or display_match, f"Expected 'Item' in DEN or display_name, got DEN: {item.den}, display_name: {item.display_name}"
            
            # Test with state filter
            result_state = await client.call_tool("get_top_level_asbiep_list", {
                'state': 'WIP',
                'offset': 0,
                'limit': 10
            })
            
            assert result_state.data.total_items >= 0
    
    @pytest.mark.asyncio
    async def test_get_top_level_asbiep_list_with_date_filters(self, token):
        """Test get_top_level_asbiep_list with date range filters."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Test created_on filter
            result_created = await client.call_tool("get_top_level_asbiep_list", {
                'created_on': '[2020-01-01~]',
                'offset': 0,
                'limit': 10
            })
            
            assert result_created.data.total_items >= 0
            
            # Test last_updated_on filter
            result_updated = await client.call_tool("get_top_level_asbiep_list", {
                'last_updated_on': '[2020-01-01~]',
                'offset': 0,
                'limit': 10
            })
            
            assert result_updated.data.total_items >= 0
    
    @pytest.mark.asyncio
    async def test_get_top_level_asbiep_list_with_ordering(self, token, item_master_asccp_manifest_id):
        """Test get_top_level_asbiep_list with order_by parameter."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get or create a business context
            biz_ctx_result = await client.call_tool("get_business_contexts", {
                'offset': 0,
                'limit': 1
            })
            
            created_biz_ctx_id = None
            if biz_ctx_result.data.items and len(biz_ctx_result.data.items) > 0:
                biz_ctx_id = biz_ctx_result.data.items[0].biz_ctx_id
            else:
                # Create a temporary business context for testing
                create_biz_ctx_result = await client.call_tool("create_business_context", {
                    'name': f'Test Business Context for Ordering {token[:8]}'
                })
                biz_ctx_id = create_biz_ctx_result.data.biz_ctx_id
                created_biz_ctx_id = biz_ctx_id
            
            # Create a Top-Level ASBIEP for testing
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Test ordering by DEN
                result_den = await client.call_tool("get_top_level_asbiep_list", {
                    'order_by': 'den',
                    'offset': 0,
                    'limit': 10
                })
                
                assert len(result_den.data.items) > 0
                # Verify all items have DEN values
                for item in result_den.data.items:
                    assert item.den is not None and len(item.den) > 0
                
                # Test ordering by creation_timestamp descending
                result_created_desc = await client.call_tool("get_top_level_asbiep_list", {
                    'order_by': '-creation_timestamp',
                    'offset': 0,
                    'limit': 10
                })
                
                assert len(result_created_desc.data.items) > 0
            finally:
                # Cleanup: delete the created BIE
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    # Log but don't fail the test if cleanup fails
                    print(f"Warning: Failed to cleanup created BIE {top_level_asbiep_id}: {e}")
                
                # Cleanup: delete the created business context if we created one
                if created_biz_ctx_id is not None:
                    try:
                        await client.call_tool("delete_business_context", {
                            'biz_ctx_id': created_biz_ctx_id
                        })
                    except Exception as e:
                        print(f"Warning: Failed to cleanup created business context {created_biz_ctx_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_get_top_level_asbiep_list_pagination(self, token):
        """Test get_top_level_asbiep_list pagination."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get first page
            result_page1 = await client.call_tool("get_top_level_asbiep_list", {
                'offset': 0,
                'limit': 5
            })
            
            assert result_page1.data.offset == 0
            assert result_page1.data.limit == 5
            assert len(result_page1.data.items) <= 5
            
            if result_page1.data.total_items > 5:
                # Get second page
                result_page2 = await client.call_tool("get_top_level_asbiep_list", {
                    'offset': 5,
                    'limit': 5
                })
                
                assert result_page2.data.offset == 5
                assert result_page2.data.limit == 5
                
                # Verify pages don't overlap
                page1_ids = {item.top_level_asbiep_id for item in result_page1.data.items}
                page2_ids = {item.top_level_asbiep_id for item in result_page2.data.items}
                assert len(page1_ids.intersection(page2_ids)) == 0, "Pages should not have overlapping items"
    
    @pytest.mark.asyncio
    async def test_get_top_level_asbiep_list_response_structure(self, token):
        """Test that get_top_level_asbiep_list returns properly structured response."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_top_level_asbiep_list", {
                'offset': 0,
                'limit': 5
            })
            
            # Verify response structure
            assert hasattr(result.data, 'total_items')
            assert hasattr(result.data, 'offset')
            assert hasattr(result.data, 'limit')
            assert hasattr(result.data, 'items')
            
            # Verify pagination values
            assert isinstance(result.data.total_items, int)
            assert result.data.total_items >= 0
            assert isinstance(result.data.offset, int)
            assert result.data.offset >= 0
            assert isinstance(result.data.limit, int)
            assert 1 <= result.data.limit <= 100
            assert isinstance(result.data.items, list)
            
            # Verify item structure if items exist
            if len(result.data.items) > 0:
                item = result.data.items[0]
                assert hasattr(item, 'top_level_asbiep_id')
                assert hasattr(item, 'asbiep_id')
                assert hasattr(item, 'guid')
                assert hasattr(item, 'den')
                assert hasattr(item, 'state')
                assert hasattr(item, 'is_deprecated')
                assert hasattr(item, 'business_contexts')
                assert hasattr(item, 'owner')
                assert hasattr(item, 'created')
                assert hasattr(item, 'last_updated')
                
                # Verify GUID format
                assert len(item.guid) == 32
                assert item.guid.islower()
                
                # Verify business_contexts structure
                assert isinstance(item.business_contexts, list)
                
                # Verify owner structure
                assert hasattr(item.owner, 'user_id')
                assert hasattr(item.owner, 'login_id')
                assert hasattr(item.owner, 'username')
                assert hasattr(item.owner, 'roles')
    
    @pytest.mark.asyncio
    async def test_get_top_level_asbiep_by_id(self, token):
        """Test get_top_level_asbiep by ID - find an existing BIE first."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # First, get a list to find an existing top-level ASBIEP
            list_result = await client.call_tool("get_top_level_asbiep_list", {
                'offset': 0,
                'limit': 1
            })
            
            if list_result.data.items and len(list_result.data.items) > 0:
                top_level_asbiep_id = list_result.data.items[0].top_level_asbiep_id
                
                # Get the full details
                result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                assert hasattr(result, 'data')
                assert hasattr(result.data, 'top_level_asbiep_id')
                assert result.data.top_level_asbiep_id == top_level_asbiep_id
                assert hasattr(result.data, 'asbiep')
                
                # Verify asbiep structure
                asbiep = result.data.asbiep
                assert hasattr(asbiep, 'asbiep_id')
                assert hasattr(asbiep, 'role_of_abie')
                
                # Verify role_of_abie structure (nested under asbiep)
                role_of_abie = asbiep.role_of_abie
                assert hasattr(role_of_abie, 'abie_id')
                assert hasattr(role_of_abie, 'based_acc_manifest')
                assert hasattr(role_of_abie, 'relationships')
                assert isinstance(role_of_abie.relationships, list)
    
    @pytest.mark.asyncio
    async def test_get_top_level_asbiep_relationships_structure(self, token):
        """Test that get_top_level_asbiep returns properly structured relationships."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get a list to find an existing top-level ASBIEP
            list_result = await client.call_tool("get_top_level_asbiep_list", {
                'offset': 0,
                'limit': 1
            })
            
            if list_result.data.items and len(list_result.data.items) > 0:
                top_level_asbiep_id = list_result.data.items[0].top_level_asbiep_id
                
                result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                relationships = result.data.asbiep.role_of_abie.relationships
                
                # Verify relationships structure (relationships are returned as dictionaries)
                for rel in relationships:
                    assert 'is_used' in rel, f"Relationship missing 'is_used' field: {rel}"
                    assert isinstance(rel['is_used'], bool)
                    assert 'path' in rel, f"Relationship missing 'path' field: {rel}"
                    
                    if rel['is_used']:
                        # If is_used=True, should have asbie_id or bbie_id
                        has_asbie = 'asbie_id' in rel and rel['asbie_id'] is not None
                        has_bbie = 'bbie_id' in rel and rel['bbie_id'] is not None
                        assert has_asbie or has_bbie, f"Relationship with is_used=True should have asbie_id or bbie_id, got: {rel}"
                    else:
                        # If is_used=False, should have based_ascc or based_bcc
                        has_based_ascc = 'based_ascc' in rel and rel['based_ascc'] is not None
                        has_based_bcc = 'based_bcc' in rel and rel['based_bcc'] is not None
                        assert has_based_ascc or has_based_bcc, f"Relationship with is_used=False should have based_ascc or based_bcc, got: {rel}"
    
    @pytest.mark.asyncio
    async def test_create_top_level_asbiep(self, token, item_master_asccp_manifest_id, sample_business_context_id):
        """Test creating a new Top-Level ASBIEP from Item Master ASCCP."""
        assert sample_business_context_id is not None, "Business context must be available for testing"
        
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Create a new Top-Level ASBIEP
            result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(sample_business_context_id)
            })
            
            assert hasattr(result, 'data')
            assert hasattr(result.data, 'top_level_asbiep_id')
            assert result.data.top_level_asbiep_id > 0
            
            # Verify the created BIE can be retrieved
            get_result = await client.call_tool("get_top_level_asbiep", {
                'top_level_asbiep_id': result.data.top_level_asbiep_id
            })
            
            assert get_result.data.top_level_asbiep_id == result.data.top_level_asbiep_id
            assert get_result.data.asbiep is not None
            assert get_result.data.asbiep.role_of_abie is not None
            
            # Cleanup: delete the created BIE
            try:
                await client.call_tool("delete_top_level_asbiep", {
                    'top_level_asbiep_id': result.data.top_level_asbiep_id
                })
            except Exception as e:
                # Log but don't fail the test if cleanup fails
                print(f"Warning: Failed to cleanup created BIE {result.data.top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_get_asbie_by_based_ascc_manifest_id(self, token, item_master_asccp_manifest_id):
        """Test get_asbie_by_based_ascc_manifest_id - explore ASBIE structure before profiling."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # First, create a Top-Level ASBIEP to work with
            biz_ctx_result = await client.call_tool("get_business_contexts", {
                'offset': 0,
                'limit': 1
            })
            
            assert biz_ctx_result.data.items and len(biz_ctx_result.data.items) > 0, "Business contexts must be available for testing"
            
            biz_ctx_id = biz_ctx_result.data.items[0].biz_ctx_id
            
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the top-level ASBIEP to find relationships
                top_level_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                # Find an ASBIE relationship (is_used=False means not yet profiled)
                relationships = top_level_result.data.asbiep.role_of_abie.relationships
                ascc_relationships = [r for r in relationships if 'based_ascc' in r and r['based_ascc'] is not None]
                
                if len(ascc_relationships) > 0:
                    first_ascc_rel = ascc_relationships[0]
                    parent_abie_path = first_ascc_rel['path']
                    based_ascc_manifest_id = first_ascc_rel['based_ascc']['ascc_manifest_id']
                    
                    # Get ASBIE by based ASCC manifest ID
                    asbie_result = await client.call_tool("get_asbie_by_based_ascc_manifest_id", {
                        'top_level_asbiep_id': top_level_asbiep_id,
                        'parent_abie_path': parent_abie_path,
                        'based_ascc_manifest_id': based_ascc_manifest_id
                    })
                    
                    assert hasattr(asbie_result, 'data')
                    assert hasattr(asbie_result.data, 'based_ascc')
                    assert asbie_result.data.based_ascc.ascc_manifest_id == based_ascc_manifest_id
                    assert hasattr(asbie_result.data, 'to_asbiep')
                    assert hasattr(asbie_result.data, 'owner_top_level_asbiep')
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_get_bbie_by_based_bcc_manifest_id(self, token, item_master_asccp_manifest_id):
        """Test get_bbie_by_based_bcc_manifest_id - explore BBIE structure before profiling."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # First, create a Top-Level ASBIEP to work with
            biz_ctx_result = await client.call_tool("get_business_contexts", {
                'offset': 0,
                'limit': 1
            })
            
            assert biz_ctx_result.data.items and len(biz_ctx_result.data.items) > 0, "Business contexts must be available for testing"
            
            biz_ctx_id = biz_ctx_result.data.items[0].biz_ctx_id
            
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the top-level ASBIEP to find relationships
                top_level_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                # Find a BBIE relationship (is_used=False means not yet profiled)
                relationships = top_level_result.data.asbiep.role_of_abie.relationships
                bcc_relationships = [r for r in relationships if 'based_bcc' in r and r['based_bcc'] is not None]
                
                if len(bcc_relationships) > 0:
                    first_bcc_rel = bcc_relationships[0]
                    parent_abie_path = first_bcc_rel['path']
                    based_bcc_manifest_id = first_bcc_rel['based_bcc']['bcc_manifest_id']
                    
                    # Get BBIE by based BCC manifest ID
                    bbie_result = await client.call_tool("get_bbie_by_based_bcc_manifest_id", {
                        'top_level_asbiep_id': top_level_asbiep_id,
                        'parent_abie_path': parent_abie_path,
                        'based_bcc_manifest_id': based_bcc_manifest_id
                    })
                    
                    assert hasattr(bbie_result, 'data')
                    assert hasattr(bbie_result.data, 'based_bcc')
                    assert bbie_result.data.based_bcc.bcc_manifest_id == based_bcc_manifest_id
                    assert hasattr(bbie_result.data, 'to_bbiep')
                    assert hasattr(bbie_result.data, 'owner_top_level_asbiep')
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_create_and_get_asbie(self, token, item_master_asccp_manifest_id):
        """Test creating an ASBIE and then retrieving it by asbie_id."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get a business context
            biz_ctx_result = await client.call_tool("get_business_contexts", {
                'offset': 0,
                'limit': 1
            })
            
            assert biz_ctx_result.data.items and len(biz_ctx_result.data.items) > 0, "Business contexts must be available for testing"
            
            biz_ctx_id = biz_ctx_result.data.items[0].biz_ctx_id
            
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the top-level ASBIEP to find an ASCC relationship to enable
                top_level_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                # Find an ASBIE relationship that's not yet used
                relationships = top_level_result.data.asbiep.role_of_abie.relationships
                ascc_relationships = [r for r in relationships if 'based_ascc' in r and r['based_ascc'] is not None and not r.get('is_used', False)]
                
                if len(ascc_relationships) > 0:
                    first_ascc_rel = ascc_relationships[0]
                    from_abie_id = top_level_result.data.asbiep.role_of_abie.abie_id
                    based_ascc_manifest_id = first_ascc_rel['based_ascc']['ascc_manifest_id']
                    
                    # Create (enable) the ASBIE
                    create_asbie_result = await client.call_tool("create_asbie", {
                        'from_abie_id': from_abie_id,
                        'based_ascc_manifest_id': based_ascc_manifest_id
                    })
                    
                    assert hasattr(create_asbie_result, 'data')
                    assert hasattr(create_asbie_result.data, 'asbie_id')
                    assert create_asbie_result.data.asbie_id > 0
                    # Note: 'updates' field was removed from CreateAsbieResponse per design
                    
                    # Get the created ASBIE by ID
                    get_asbie_result = await client.call_tool("get_asbie_by_asbie_id", {
                        'asbie_id': create_asbie_result.data.asbie_id
                    })
                    
                    assert get_asbie_result.data.asbie_id == create_asbie_result.data.asbie_id
                    assert hasattr(get_asbie_result.data, 'to_asbiep')
                    assert hasattr(get_asbie_result.data, 'owner_top_level_asbiep')
                    assert hasattr(get_asbie_result.data, 'based_ascc')
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_create_and_get_bbie(self, token, item_master_asccp_manifest_id):
        """Test creating a BBIE and then retrieving it by bbie_id."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get a business context
            biz_ctx_result = await client.call_tool("get_business_contexts", {
                'offset': 0,
                'limit': 1
            })
            
            assert biz_ctx_result.data.items and len(biz_ctx_result.data.items) > 0, "Business contexts must be available for testing"
            
            biz_ctx_id = biz_ctx_result.data.items[0].biz_ctx_id
            
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the top-level ASBIEP to find a BCC relationship to enable
                top_level_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                # Get base ACC to find BCC relationships
                role_of_abie = top_level_result.data.asbiep.role_of_abie
                asccp_result = await client.call_tool("get_asccp", {
                    'asccp_manifest_id': item_master_asccp_manifest_id
                })
                
                role_of_acc_manifest_id = asccp_result.data.role_of_acc.acc_manifest_id
                
                # Get base ACC
                acc_result = await client.call_tool("get_acc", {
                    'acc_manifest_id': role_of_acc_manifest_id
                })
                
                # Check if ACC has base_acc
                base_acc_manifest_id = role_of_acc_manifest_id
                if hasattr(acc_result.data, 'base_acc') and acc_result.data.base_acc is not None:
                    base_acc_manifest_id = acc_result.data.base_acc.acc_manifest_id
                    base_acc_result = await client.call_tool("get_acc", {
                        'acc_manifest_id': base_acc_manifest_id
                    })
                    relationships = base_acc_result.data.relationships
                else:
                    relationships = acc_result.data.relationships
                
                # Find a BCC relationship
                bcc_relationships = [r for r in relationships if r['component_type'] == 'BCC']
                
                if len(bcc_relationships) > 0:
                    first_bcc = bcc_relationships[0]
                    from_abie_id = role_of_abie.abie_id
                    based_bcc_manifest_id = first_bcc['bcc_manifest_id']
                    
                    # Create (enable) the BBIE
                    create_bbie_result = await client.call_tool("create_bbie", {
                        'from_abie_id': from_abie_id,
                        'based_bcc_manifest_id': based_bcc_manifest_id
                    })
                    
                    assert hasattr(create_bbie_result, 'data')
                    assert hasattr(create_bbie_result.data, 'bbie_id')
                    assert create_bbie_result.data.bbie_id > 0
                    # Note: 'updates' field was removed from CreateBbieResponse per design
                    
                    # Get the created BBIE by ID
                    get_bbie_result = await client.call_tool("get_bbie_by_bbie_id", {
                        'bbie_id': create_bbie_result.data.bbie_id
                    })
                    
                    assert get_bbie_result.data.bbie_id == create_bbie_result.data.bbie_id
                    assert hasattr(get_bbie_result.data, 'to_bbiep')
                    assert hasattr(get_bbie_result.data, 'based_bcc')
                    assert hasattr(get_bbie_result.data, 'owner_top_level_asbiep')
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_update_asbie(self, token, item_master_asccp_manifest_id):
        """Test updating an ASBIE (enable/disable and modify properties)."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get a business context
            biz_ctx_result = await client.call_tool("get_business_contexts", {
                'offset': 0,
                'limit': 1
            })
            
            assert biz_ctx_result.data.items and len(biz_ctx_result.data.items) > 0, "Business contexts must be available for testing"
            
            biz_ctx_id = biz_ctx_result.data.items[0].biz_ctx_id
            
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the top-level ASBIEP to find an ASCC relationship
                top_level_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                relationships = top_level_result.data.asbiep.role_of_abie.relationships
                ascc_relationships = [r for r in relationships if 'based_ascc' in r and r['based_ascc'] is not None and not r.get('is_used', False)]
                
                if len(ascc_relationships) > 0:
                    first_ascc_rel = ascc_relationships[0]
                    from_abie_id = top_level_result.data.asbiep.role_of_abie.abie_id
                    based_ascc_manifest_id = first_ascc_rel['based_ascc']['ascc_manifest_id']
                    
                    # Create the ASBIE first
                    create_asbie_result = await client.call_tool("create_asbie", {
                        'from_abie_id': from_abie_id,
                        'based_ascc_manifest_id': based_ascc_manifest_id
                    })
                    
                    asbie_id = create_asbie_result.data.asbie_id
                    
                    # Update the ASBIE - disable it
                    update_result = await client.call_tool("update_asbie", {
                        'asbie_id': asbie_id,
                        'is_used': False
                    })
                    
                    assert hasattr(update_result, 'data')
                    assert hasattr(update_result.data, 'updates')
                    
                    # Update the ASBIE - enable it again
                    update_result2 = await client.call_tool("update_asbie", {
                        'asbie_id': asbie_id,
                        'is_used': True
                    })
                    
                    assert hasattr(update_result2, 'data')
                    assert hasattr(update_result2.data, 'updates')
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_update_bbie(self, token, item_master_asccp_manifest_id):
        """Test updating a BBIE (enable/disable and modify properties)."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get a business context
            biz_ctx_result = await client.call_tool("get_business_contexts", {
                'offset': 0,
                'limit': 1
            })
            
            assert biz_ctx_result.data.items and len(biz_ctx_result.data.items) > 0, "Business contexts must be available for testing"
            
            biz_ctx_id = biz_ctx_result.data.items[0].biz_ctx_id
            
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the top-level ASBIEP
                top_level_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                # Get base ACC to find BCC relationships
                asccp_result = await client.call_tool("get_asccp", {
                    'asccp_manifest_id': item_master_asccp_manifest_id
                })
                
                role_of_acc_manifest_id = asccp_result.data.role_of_acc.acc_manifest_id
                acc_result = await client.call_tool("get_acc", {
                    'acc_manifest_id': role_of_acc_manifest_id
                })
                
                base_acc_manifest_id = role_of_acc_manifest_id
                if hasattr(acc_result.data, 'base_acc') and acc_result.data.base_acc is not None:
                    base_acc_manifest_id = acc_result.data.base_acc.acc_manifest_id
                    base_acc_result = await client.call_tool("get_acc", {
                        'acc_manifest_id': base_acc_manifest_id
                    })
                    relationships = base_acc_result.data.relationships
                else:
                    relationships = acc_result.data.relationships
                
                bcc_relationships = [r for r in relationships if r['component_type'] == 'BCC']
                
                if len(bcc_relationships) > 0:
                    first_bcc = bcc_relationships[0]
                    from_abie_id = top_level_result.data.asbiep.role_of_abie.abie_id
                    based_bcc_manifest_id = first_bcc['bcc_manifest_id']
                    
                    # Create the BBIE first
                    create_bbie_result = await client.call_tool("create_bbie", {
                        'from_abie_id': from_abie_id,
                        'based_bcc_manifest_id': based_bcc_manifest_id
                    })
                    
                    bbie_id = create_bbie_result.data.bbie_id
                    
                    # Update the BBIE - disable it
                    update_result = await client.call_tool("update_bbie", {
                        'bbie_id': bbie_id,
                        'is_used': False
                    })
                    
                    assert hasattr(update_result, 'data')
                    assert hasattr(update_result.data, 'updates')
                    
                    # Update the BBIE - enable it again
                    update_result2 = await client.call_tool("update_bbie", {
                        'bbie_id': bbie_id,
                        'is_used': True
                    })
                    
                    assert hasattr(update_result2, 'data')
                    assert hasattr(update_result2.data, 'updates')
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_update_top_level_asbiep(self, token, item_master_asccp_manifest_id):
        """Test updating a Top-Level ASBIEP (version, status, deprecation)."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get a business context
            biz_ctx_result = await client.call_tool("get_business_contexts", {
                'offset': 0,
                'limit': 1
            })
            
            assert biz_ctx_result.data.items and len(biz_ctx_result.data.items) > 0, "Business contexts must be available for testing"
            
            biz_ctx_id = biz_ctx_result.data.items[0].biz_ctx_id
            
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Update version
                update_result = await client.call_tool("update_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id,
                    'version': '1.0'
                })
                
                assert hasattr(update_result, 'data')
                assert hasattr(update_result.data, 'top_level_asbiep_id')
                assert update_result.data.top_level_asbiep_id == top_level_asbiep_id
                
                # Update status
                update_result2 = await client.call_tool("update_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id,
                    'status': 'Test'
                })
                
                assert update_result2.data.top_level_asbiep_id == top_level_asbiep_id
                
                # Verify updates
                get_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                assert get_result.data.asbiep.owner_top_level_asbiep.version == '1.0'
                assert get_result.data.asbiep.owner_top_level_asbiep.status == 'Test'
                
                # Test updating ASBIEP properties (display_name, biz_term, remark)
                update_result3 = await client.call_tool("update_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id,
                    'display_name': 'Test Item Master',
                    'biz_term': 'Test Product Master',
                    'remark': 'Test remark for BIE'
                })
                
                assert update_result3.data.top_level_asbiep_id == top_level_asbiep_id
                assert 'display_name' in update_result3.data.updates or 'biz_term' in update_result3.data.updates or 'remark' in update_result3.data.updates
                
                # Verify ASBIEP property updates
                get_result2 = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                assert get_result2.data.asbiep.display_name == 'Test Item Master'
                assert get_result2.data.asbiep.biz_term == 'Test Product Master'
                assert get_result2.data.asbiep.remark == 'Test remark for BIE'
                
                # Test deprecation
                update_result4 = await client.call_tool("update_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id,
                    'is_deprecated': True,
                    'deprecated_reason': 'Test deprecation reason',
                    'deprecated_remark': 'Test deprecation remark'
                })
                
                assert update_result4.data.top_level_asbiep_id == top_level_asbiep_id
                assert 'is_deprecated' in update_result4.data.updates
                
                # Verify deprecation
                get_result3 = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                assert get_result3.data.asbiep.owner_top_level_asbiep.is_deprecated == True
                assert get_result3.data.asbiep.owner_top_level_asbiep.deprecated_reason == 'Test deprecation reason'
                assert get_result3.data.asbiep.owner_top_level_asbiep.deprecated_remark == 'Test deprecation remark'
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_delete_top_level_asbiep(self, token, item_master_asccp_manifest_id):
        """Test deleting a Top-Level ASBIEP and all related records."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get a business context
            biz_ctx_result = await client.call_tool("get_business_contexts", {
                'offset': 0,
                'limit': 1
            })
            
            assert biz_ctx_result.data.items and len(biz_ctx_result.data.items) > 0, "Business contexts must be available for testing"
            
            biz_ctx_id = biz_ctx_result.data.items[0].biz_ctx_id
            
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            # Verify it exists
            get_result = await client.call_tool("get_top_level_asbiep", {
                'top_level_asbiep_id': top_level_asbiep_id
            })
            
            assert get_result.data.top_level_asbiep_id == top_level_asbiep_id
            
            # Delete the Top-Level ASBIEP
            delete_result = await client.call_tool("delete_top_level_asbiep", {
                'top_level_asbiep_id': top_level_asbiep_id
            })
            
            assert hasattr(delete_result, 'data')
            assert hasattr(delete_result.data, 'top_level_asbiep_id')
            assert delete_result.data.top_level_asbiep_id == top_level_asbiep_id
            
            # Verify it's deleted (should raise an error or return None)
            try:
                get_result2 = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                # If we get here, the BIE still exists, which is unexpected
                assert False, f"Top-Level ASBIEP {top_level_asbiep_id} should have been deleted but still exists"
            except Exception as e:
                # Expected: the BIE should not be found after deletion
                assert "not found" in str(e).lower() or "404" in str(e) or "does not exist" in str(e).lower(), f"Expected 'not found' error, got: {e}"
    
    @pytest.mark.asyncio
    async def test_assign_and_unassign_business_context(self, token, item_master_asccp_manifest_id):
        """Test assigning and unassigning business contexts to/from a Top-Level ASBIEP."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get business contexts
            biz_ctx_result = await client.call_tool("get_business_contexts", {
                'offset': 0,
                'limit': 2
            })
            
            # Get first business context (must exist)
            assert biz_ctx_result.data.items and len(biz_ctx_result.data.items) > 0, "At least one business context must be available for testing"
            biz_ctx_id1 = biz_ctx_result.data.items[0].biz_ctx_id
            
            # Create a second business context if needed
            created_biz_ctx_id2 = None
            if len(biz_ctx_result.data.items) >= 2:
                biz_ctx_id2 = biz_ctx_result.data.items[1].biz_ctx_id
            else:
                # Create a temporary business context for testing
                create_biz_ctx_result = await client.call_tool("create_business_context", {
                    'name': f'Test Business Context for Assignment {token[:8]}'
                })
                biz_ctx_id2 = create_biz_ctx_result.data.biz_ctx_id
                created_biz_ctx_id2 = biz_ctx_id2
            
            # Create a Top-Level ASBIEP with first business context
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(biz_ctx_id1)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Assign second business context
                assign_result = await client.call_tool("assign_biz_ctx_to_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id,
                    'biz_ctx_id': biz_ctx_id2
                })
                
                assert hasattr(assign_result, 'data')
                assert hasattr(assign_result.data, 'top_level_asbiep_id')
                assert assign_result.data.top_level_asbiep_id == top_level_asbiep_id
                
                # Verify assignment
                get_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                biz_ctx_ids = [ctx.biz_ctx_id for ctx in get_result.data.business_contexts]
                assert biz_ctx_id1 in biz_ctx_ids
                assert biz_ctx_id2 in biz_ctx_ids
                
                # Unassign second business context
                unassign_result = await client.call_tool("unassign_biz_ctx_from_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id,
                    'biz_ctx_id': biz_ctx_id2
                })
                
                assert hasattr(unassign_result, 'data')
                assert unassign_result.data.top_level_asbiep_id == top_level_asbiep_id
                
                # Verify unassignment
                get_result2 = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                biz_ctx_ids2 = [ctx.biz_ctx_id for ctx in get_result2.data.business_contexts]
                assert biz_ctx_id1 in biz_ctx_ids2
                assert biz_ctx_id2 not in biz_ctx_ids2
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
                
                # Cleanup created business context if we created one
                if created_biz_ctx_id2 is not None:
                    try:
                        await client.call_tool("delete_business_context", {
                            'biz_ctx_id': created_biz_ctx_id2
                        })
                    except Exception as e:
                        print(f"Warning: Failed to cleanup created business context {created_biz_ctx_id2}: {e}")
    
    @pytest.mark.asyncio
    async def test_update_top_level_asbiep_state(self, token, item_master_asccp_manifest_id):
        """Test updating the state of a Top-Level ASBIEP (WIP -> QA -> Production)."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get a business context
            biz_ctx_result = await client.call_tool("get_business_contexts", {
                'offset': 0,
                'limit': 1
            })
            
            assert biz_ctx_result.data.items and len(biz_ctx_result.data.items) > 0, "Business contexts must be available for testing"
            
            biz_ctx_id = biz_ctx_result.data.items[0].biz_ctx_id
            
            # Create a Top-Level ASBIEP (should be in WIP state)
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Verify initial state is WIP
                get_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                initial_state = get_result.data.asbiep.owner_top_level_asbiep.state
                assert initial_state == 'WIP', f"New BIE should be in WIP state, got {initial_state}"
                
                # Update state to QA
                update_result = await client.call_tool("update_top_level_asbiep_state", {
                    'top_level_asbiep_id': top_level_asbiep_id,
                    'new_state': 'QA'
                })
                
                assert hasattr(update_result, 'data')
                assert update_result.data.top_level_asbiep_id == top_level_asbiep_id
                
                # Verify state change
                get_result2 = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                assert get_result2.data.asbiep.owner_top_level_asbiep.state == 'QA'
                
                # Update state back to WIP
                update_result2 = await client.call_tool("update_top_level_asbiep_state", {
                    'top_level_asbiep_id': top_level_asbiep_id,
                    'new_state': 'WIP'
                })
                
                assert update_result2.data.top_level_asbiep_id == top_level_asbiep_id
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_get_top_level_asbiep_complete_structure(self, token, item_master_asccp_manifest_id):
        """Test complete structure traversal of a Top-Level ASBIEP."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get a business context
            biz_ctx_result = await client.call_tool("get_business_contexts", {
                'offset': 0,
                'limit': 1
            })
            
            assert biz_ctx_result.data.items and len(biz_ctx_result.data.items) > 0, "Business contexts must be available for testing"
            
            biz_ctx_id = biz_ctx_result.data.items[0].biz_ctx_id
            
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the complete structure
                result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                # Verify top-level structure
                assert result.data.top_level_asbiep_id == top_level_asbiep_id
                assert result.data.asbiep is not None
                assert result.data.asbiep.role_of_abie is not None
                assert result.data.business_contexts is not None
                assert isinstance(result.data.business_contexts, list)
                assert len(result.data.business_contexts) > 0
                
                # Verify business contexts
                for biz_ctx in result.data.business_contexts:
                    assert hasattr(biz_ctx, 'biz_ctx_id')
                    assert hasattr(biz_ctx, 'guid')
                    assert hasattr(biz_ctx, 'name')
                    assert biz_ctx.biz_ctx_id == biz_ctx_id
                
                # Verify role_of_abie structure (nested under asbiep)
                role_of_abie = result.data.asbiep.role_of_abie
                assert hasattr(role_of_abie, 'abie_id')
                assert hasattr(role_of_abie, 'based_acc_manifest')
                assert hasattr(role_of_abie, 'relationships')
                assert isinstance(role_of_abie.relationships, list)
                
                # Verify relationships have proper structure (relationships are returned as dictionaries)
                for rel in role_of_abie.relationships:
                    assert 'is_used' in rel, f"Relationship missing 'is_used' field: {rel}"
                    assert 'path' in rel, f"Relationship missing 'path' field: {rel}"
                    assert isinstance(rel['is_used'], bool)
                    
                    if rel['is_used']:
                        # Should have either asbie_id or bbie_id
                        has_asbie = 'asbie_id' in rel and rel['asbie_id'] is not None
                        has_bbie = 'bbie_id' in rel and rel['bbie_id'] is not None
                        assert has_asbie or has_bbie, f"Used relationship should have asbie_id or bbie_id, got: {rel}"
                    else:
                        # Should have either based_ascc or based_bcc
                        has_based_ascc = 'based_ascc' in rel and rel['based_ascc'] is not None
                        has_based_bcc = 'based_bcc' in rel and rel['based_bcc'] is not None
                        assert has_based_ascc or has_based_bcc, f"Unused relationship should have based_ascc or based_bcc, got: {rel}"
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_create_bbie_sc(self, token, item_master_asccp_manifest_id):
        """Test creating a BBIE_SC (Basic Business Information Entity Supplementary Component)."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get a business context
            biz_ctx_result = await client.call_tool("get_business_contexts", {
                'offset': 0,
                'limit': 1
            })
            
            assert biz_ctx_result.data.items and len(biz_ctx_result.data.items) > 0, "Business contexts must be available for testing"
            
            biz_ctx_id = biz_ctx_result.data.items[0].biz_ctx_id
            
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the top-level ASBIEP
                top_level_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                # Get base ACC to find BCC relationships
                asccp_result = await client.call_tool("get_asccp", {
                    'asccp_manifest_id': item_master_asccp_manifest_id
                })
                
                role_of_acc_manifest_id = asccp_result.data.role_of_acc.acc_manifest_id
                acc_result = await client.call_tool("get_acc", {
                    'acc_manifest_id': role_of_acc_manifest_id
                })
                
                base_acc_manifest_id = role_of_acc_manifest_id
                if hasattr(acc_result.data, 'base_acc') and acc_result.data.base_acc is not None:
                    base_acc_manifest_id = acc_result.data.base_acc.acc_manifest_id
                    base_acc_result = await client.call_tool("get_acc", {
                        'acc_manifest_id': base_acc_manifest_id
                    })
                    relationships = base_acc_result.data.relationships
                else:
                    relationships = acc_result.data.relationships
                
                bcc_relationships = [r for r in relationships if r['component_type'] == 'BCC']
                
                if len(bcc_relationships) > 0:
                    first_bcc = bcc_relationships[0]
                    from_abie_id = top_level_result.data.asbiep.role_of_abie.abie_id
                    based_bcc_manifest_id = first_bcc['bcc_manifest_id']
                    
                    # Create the BBIE first
                    create_bbie_result = await client.call_tool("create_bbie", {
                        'from_abie_id': from_abie_id,
                        'based_bcc_manifest_id': based_bcc_manifest_id
                    })
                    
                    bbie_id = create_bbie_result.data.bbie_id
                    
                    # Get the BBIE to find supplementary components
                    bbie_result = await client.call_tool("get_bbie_by_bbie_id", {
                        'bbie_id': bbie_id
                    })
                    
                    # Check if BBIEP has supplementary components
                    if hasattr(bbie_result.data, 'to_bbiep') and hasattr(bbie_result.data.to_bbiep, 'supplementary_components'):
                        supp_components = bbie_result.data.to_bbiep.supplementary_components
                        
                        if supp_components and len(supp_components) > 0:
                            # Find an unused supplementary component (supplementary_components are returned as objects)
                            unused_supp = [sc for sc in supp_components if not hasattr(sc, 'bbie_sc_id') or getattr(sc, 'bbie_sc_id', None) is None]
                            
                            if len(unused_supp) > 0:
                                first_supp = unused_supp[0]
                                based_dt_sc_manifest_id = first_supp.based_dt_sc.dt_sc_manifest_id
                                
                                # Create BBIE_SC
                                create_bbie_sc_result = await client.call_tool("create_bbie_sc", {
                                    'bbie_id': bbie_id,
                                    'based_dt_sc_manifest_id': based_dt_sc_manifest_id
                                })
                                
                                assert hasattr(create_bbie_sc_result, 'data')
                                assert hasattr(create_bbie_sc_result.data, 'bbie_sc_id')
                                assert create_bbie_sc_result.data.bbie_sc_id > 0
                                assert hasattr(create_bbie_sc_result.data, 'updates')
                                assert 'is_used' in create_bbie_sc_result.data.updates
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_update_bbie_sc(self, token, item_master_asccp_manifest_id):
        """Test updating a BBIE_SC (enable/disable and modify properties)."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get a business context
            biz_ctx_result = await client.call_tool("get_business_contexts", {
                'offset': 0,
                'limit': 1
            })
            
            assert biz_ctx_result.data.items and len(biz_ctx_result.data.items) > 0, "Business contexts must be available for testing"
            
            biz_ctx_id = biz_ctx_result.data.items[0].biz_ctx_id
            
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the top-level ASBIEP
                top_level_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                # Get base ACC to find BCC relationships
                asccp_result = await client.call_tool("get_asccp", {
                    'asccp_manifest_id': item_master_asccp_manifest_id
                })
                
                role_of_acc_manifest_id = asccp_result.data.role_of_acc.acc_manifest_id
                acc_result = await client.call_tool("get_acc", {
                    'acc_manifest_id': role_of_acc_manifest_id
                })
                
                base_acc_manifest_id = role_of_acc_manifest_id
                if hasattr(acc_result.data, 'base_acc') and acc_result.data.base_acc is not None:
                    base_acc_manifest_id = acc_result.data.base_acc.acc_manifest_id
                    base_acc_result = await client.call_tool("get_acc", {
                        'acc_manifest_id': base_acc_manifest_id
                    })
                    relationships = base_acc_result.data.relationships
                else:
                    relationships = acc_result.data.relationships
                
                bcc_relationships = [r for r in relationships if r['component_type'] == 'BCC']
                
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
                            # Find an unused supplementary component (supplementary_components are returned as objects)
                            unused_supp = [sc for sc in supp_components if not hasattr(sc, 'bbie_sc_id') or getattr(sc, 'bbie_sc_id', None) is None]
                            
                            if len(unused_supp) > 0:
                                first_supp = unused_supp[0]
                                based_dt_sc_manifest_id = first_supp.based_dt_sc.dt_sc_manifest_id
                                
                                # Create BBIE_SC
                                create_bbie_sc_result = await client.call_tool("create_bbie_sc", {
                                    'bbie_id': bbie_id,
                                    'based_dt_sc_manifest_id': based_dt_sc_manifest_id
                                })
                                
                                bbie_sc_id = create_bbie_sc_result.data.bbie_sc_id
                                
                                # Update BBIE_SC - disable it
                                update_result = await client.call_tool("update_bbie_sc", {
                                    'bbie_sc_id': bbie_sc_id,
                                    'is_used': False
                                })
                                
                                assert hasattr(update_result, 'data')
                                assert hasattr(update_result.data, 'updates')
                                
                                # Update BBIE_SC - enable it again
                                update_result2 = await client.call_tool("update_bbie_sc", {
                                    'bbie_sc_id': bbie_sc_id,
                                    'is_used': True
                                })
                                
                                assert hasattr(update_result2, 'data')
                                assert hasattr(update_result2.data, 'updates')
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_get_top_level_asbiep_list_with_all_filters(self, token, release_10_12_id, connectspec_library_id):
        """Test get_top_level_asbiep_list with all filter parameters combined."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            result = await client.call_tool("get_top_level_asbiep_list", {
                'release_id_list': str(release_10_12_id),
                'den': 'Item',
                'state': 'WIP',
                'is_deprecated': False,
                'created_on': '[2020-01-01~]',
                'last_updated_on': '[2020-01-01~]',
                'order_by': '-creation_timestamp',
                'offset': 0,
                'limit': 10
            })
            
            assert result.data.total_items >= 0
            assert len(result.data.items) <= 10
            
            # Verify filters are applied
            for item in result.data.items:
                assert item.is_deprecated == False
                assert item.state == 'WIP'
                den_match = 'Item' in item.den or 'item' in item.den.lower() if item.den else False
                display_match = 'Item' in item.display_name or 'item' in item.display_name.lower() if item.display_name else False
                assert den_match or display_match
    
    @pytest.mark.asyncio
    async def test_get_top_level_asbiep_deep_traversal(self, token, item_master_asccp_manifest_id):
        """Test deep traversal of a Top-Level ASBIEP structure including nested relationships."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get a business context
            biz_ctx_result = await client.call_tool("get_business_contexts", {
                'offset': 0,
                'limit': 1
            })
            
            assert biz_ctx_result.data.items and len(biz_ctx_result.data.items) > 0, "Business contexts must be available for testing"
            
            biz_ctx_id = biz_ctx_result.data.items[0].biz_ctx_id
            
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Get the complete structure
                result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                # Traverse relationships
                relationships = result.data.asbiep.role_of_abie.relationships
                assert len(relationships) > 0, "Item Master ABIE should have relationships"
                
                # Count different types of relationships (relationships are returned as dictionaries)
                ascc_count = sum(1 for r in relationships if 'based_ascc' in r and r['based_ascc'] is not None)
                bcc_count = sum(1 for r in relationships if 'based_bcc' in r and r['based_bcc'] is not None)
                
                assert ascc_count + bcc_count > 0, f"Should have at least one ASCC or BCC relationship, got {len(relationships)} relationships"
                
                # Verify relationship paths
                for rel in relationships:
                    assert 'path' in rel, f"Relationship missing 'path' field: {rel}"
                    assert rel['path'] is not None
                    assert len(rel['path']) > 0
                    
                    # Verify cardinality information
                    if 'cardinality_min' in rel:
                        assert rel['cardinality_min'] >= 0
                    if 'cardinality_max' in rel:
                        assert rel['cardinality_max'] >= -1
            finally:
                # Cleanup
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    print(f"Warning: Failed to cleanup BIE {top_level_asbiep_id}: {e}")
    
    @pytest.mark.asyncio
    async def test_transfer_top_level_asbiep_ownership(self, token, item_master_asccp_manifest_id, temp_end_user_for_transfer):
        """Test transferring ownership of a Top-Level ASBIEP to another user."""
        async with Client("http://localhost:8000/mcp", auth=BearerAuth(token=token)) as client:
            # Get current user
            current_user_result = await client.call_tool("who_am_i", {})
            current_user_id = current_user_result.data.user_id
            
            # Use the temporary End-User created by the fixture
            compatible_user_id = temp_end_user_for_transfer['user_id']
            
            # Get a business context
            biz_ctx_result = await client.call_tool("get_business_contexts", {
                'offset': 0,
                'limit': 1
            })
            
            assert biz_ctx_result.data.items and len(biz_ctx_result.data.items) > 0, "Business contexts must be available for testing"
            
            biz_ctx_id = biz_ctx_result.data.items[0].biz_ctx_id
            
            # Create a Top-Level ASBIEP
            create_result = await client.call_tool("create_top_level_asbiep", {
                'asccp_manifest_id': item_master_asccp_manifest_id,
                'biz_ctx_list': str(biz_ctx_id)
            })
            
            top_level_asbiep_id = create_result.data.top_level_asbiep_id
            
            try:
                # Verify initial ownership
                get_result = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                initial_owner_id = get_result.data.asbiep.owner_top_level_asbiep.owner.user_id
                assert initial_owner_id == current_user_id, "Initial owner should be current user"
                
                # Transfer ownership
                transfer_result = await client.call_tool("transfer_top_level_asbiep_ownership", {
                    'top_level_asbiep_id': top_level_asbiep_id,
                    'new_owner_user_id': compatible_user_id
                })
                
                assert hasattr(transfer_result, 'data')
                assert hasattr(transfer_result.data, 'top_level_asbiep_id')
                assert transfer_result.data.top_level_asbiep_id == top_level_asbiep_id
                assert hasattr(transfer_result.data, 'updates')
                assert 'owner_user_id' in transfer_result.data.updates
                
                # Verify ownership transfer
                get_result2 = await client.call_tool("get_top_level_asbiep", {
                    'top_level_asbiep_id': top_level_asbiep_id
                })
                
                new_owner_id = get_result2.data.asbiep.owner_top_level_asbiep.owner.user_id
                assert new_owner_id == compatible_user_id, f"Owner should be {compatible_user_id}, got {new_owner_id}"
                
                # Note: We cannot transfer ownership back to the original owner because:
                # 1. The current authenticated user is no longer the owner
                # 2. Only the current owner can transfer ownership
                # 3. We would need the compatible_user's authentication token to transfer back
                # This is expected behavior - ownership transfer is a one-way operation from the current owner's perspective
                # The test has successfully verified that ownership transfer works correctly
            finally:
                # Cleanup: Try to delete the BIE
                # Note: If ownership was transferred, the current user may not have permission to delete it
                # This is expected behavior - only the owner can delete their BIE
                try:
                    await client.call_tool("delete_top_level_asbiep", {
                        'top_level_asbiep_id': top_level_asbiep_id
                    })
                except Exception as e:
                    # If deletion fails due to ownership, that's expected after a transfer
                    # The BIE will remain in the database owned by compatible_user
                    # This is acceptable for test cleanup - the test has verified the transfer functionality
                    print(f"Note: Could not cleanup BIE {top_level_asbiep_id} (likely due to ownership transfer): {e}")

