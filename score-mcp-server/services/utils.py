import re


def validate_guid(guid: str) -> bool:
    """
    Validate that a GUID is a 32-character hexadecimal string (lowercase).
    
    Args:
        guid: The GUID string to validate
        
    Returns:
        bool: True if the GUID is valid, False otherwise
    """
    if not guid:
        return False

    # Check if it's exactly 32 characters
    if len(guid) != 32:
        return False

    # Check if it contains only hexadecimal characters (0-9, a-f)
    if not re.match(r'^[0-9a-f]{32}$', guid):
        return False

    return True


def generate_guid() -> str:
    """
    Generate a valid 32-character hexadecimal GUID (lowercase).
    
    Returns:
        str: A valid GUID string
    """
    import uuid
    return str(uuid.uuid4()).replace('-', '').lower()
