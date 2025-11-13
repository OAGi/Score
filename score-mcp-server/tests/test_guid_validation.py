import pytest
import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from services.utils import validate_guid, generate_guid
from services.models import CtxCategoryBase, CtxSchemeBase, CtxSchemeValueBase, BizCtxBase


class TestGUIDValidation:
    """Test cases for GUID validation functionality."""
    
    def test_validate_guid_valid_cases(self):
        """Test that valid GUIDs pass validation."""
        valid_guids = [
            "a1b2c3d4e5f678901234567890123456",  # 32 chars, lowercase hex
            "0123456789abcdef0123456789abcdef",  # 32 chars, lowercase hex
            "ffffffffffffffffffffffffffffffff",  # All f's
            "00000000000000000000000000000000",  # All 0's
        ]
        
        for guid in valid_guids:
            assert validate_guid(guid) is True, f"GUID {guid} should be valid"
    
    def test_validate_guid_invalid_cases(self):
        """Test that invalid GUIDs fail validation."""
        invalid_guids = [
            "",  # Empty string
            None,  # None value
            "a1b2c3d4e5f67890123456789012345",  # 31 chars (too short)
            "a1b2c3d4e5f6789012345678901234567",  # 33 chars (too long)
            "A1B2C3D4E5F678901234567890123456",  # Uppercase (invalid)
            "a1b2c3d4e5f67890123456789012345g",  # Contains 'g' (invalid hex)
            "a1b2c3d4e5f67890123456789012345-",  # Contains dash
            "a1b2c3d4e5f67890123456789012345 ",  # Contains space
            "a1b2c3d4e5f6789012345678901234567",  # 33 chars
        ]
        
        for guid in invalid_guids:
            if guid is not None:
                assert validate_guid(guid) is False, f"GUID {guid} should be invalid"
    
    def test_generate_guid(self):
        """Test that generated GUIDs are valid."""
        for _ in range(10):  # Test multiple generations
            guid = generate_guid()
            assert validate_guid(guid) is True, f"Generated GUID {guid} should be valid"
            assert len(guid) == 32, f"Generated GUID should be 32 characters long"
            assert guid.islower(), f"Generated GUID should be lowercase"
    
    def test_generate_guid_uniqueness(self):
        """Test that generated GUIDs are unique."""
        guids = set()
        for _ in range(100):  # Generate 100 GUIDs
            guid = generate_guid()
            assert guid not in guids, f"Generated GUID {guid} should be unique"
            guids.add(guid)


class TestModelGUIDValidation:
    """Test cases for GUID validation in Pydantic models."""
    
    def test_ctx_category_guid_validation(self):
        """Test GUID validation in CtxCategoryBase model."""
        # Valid GUID should work
        valid_guid = "a1b2c3d4e5f678901234567890123456"
        category = CtxCategoryBase(
            guid=valid_guid,
            name="Test Category",
            created_by=1,
            last_updated_by=1
        )
        assert category.guid == valid_guid
        
        # Invalid GUID should raise ValueError
        with pytest.raises(ValueError, match="Guid must be a 32-character hexadecimal string \\(lowercase\\)"):
            CtxCategoryBase(
                guid="INVALID_GUID",
                name="Test Category",
                created_by=1,
                last_updated_by=1
            )
    
    def test_ctx_scheme_guid_validation(self):
        """Test GUID validation in CtxSchemeBase model."""
        # Valid GUID should work
        valid_guid = "a1b2c3d4e5f678901234567890123456"
        scheme = CtxSchemeBase(
            guid=valid_guid,
            scheme_id="TEST_SCHEME",
            scheme_agency_id="TEST_AGENCY",
            scheme_version_id="1.0",
            ctx_category_id=1,
            created_by=1,
            last_updated_by=1
        )
        assert scheme.guid == valid_guid
        
        # Invalid GUID should raise ValueError
        with pytest.raises(ValueError, match="Guid must be a 32-character hexadecimal string \\(lowercase\\)"):
            CtxSchemeBase(
                guid="INVALID_GUID",
                scheme_id="TEST_SCHEME",
                scheme_agency_id="TEST_AGENCY",
                scheme_version_id="1.0",
                ctx_category_id=1,
                created_by=1,
                last_updated_by=1
            )
    
    def test_ctx_scheme_value_guid_validation(self):
        """Test GUID validation in CtxSchemeValueBase model."""
        # Valid GUID should work
        valid_guid = "a1b2c3d4e5f678901234567890123456"
        scheme_value = CtxSchemeValueBase(
            guid=valid_guid,
            value="TEST_VALUE",
            owner_ctx_scheme_id=1
        )
        assert scheme_value.guid == valid_guid
        
        # Invalid GUID should raise ValueError
        with pytest.raises(ValueError, match="Guid must be a 32-character hexadecimal string \\(lowercase\\)"):
            CtxSchemeValueBase(
                guid="INVALID_GUID",
                value="TEST_VALUE",
                owner_ctx_scheme_id=1
            )
    
    def test_biz_ctx_guid_validation(self):
        """Test GUID validation in BizCtxBase model."""
        # Valid GUID should work
        valid_guid = "a1b2c3d4e5f678901234567890123456"
        biz_ctx = BizCtxBase(
            guid=valid_guid,
            name="Test Business Context",
            created_by=1,
            last_updated_by=1
        )
        assert biz_ctx.guid == valid_guid
        
        # Invalid GUID should raise ValueError
        with pytest.raises(ValueError, match="Guid must be a 32-character hexadecimal string \\(lowercase\\)"):
            BizCtxBase(
                guid="INVALID_GUID",
                name="Test Business Context",
                created_by=1,
                last_updated_by=1
            )
