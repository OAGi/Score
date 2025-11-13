from datetime import datetime, timedelta

from fastmcp.exceptions import ToolError
from tools.models.core_component import ValueConstraint


def parse_date_range(date_range: str) -> tuple[datetime | None, datetime | None]:
    """
    Parse date range format: [before~after] or [~after] or [before~]

    Args:
        date_range: Date range string in format [before~after] or [~after] or [before~]

    Returns:
        tuple: (before_date, after_date) where either can be None

    Raises:
        ValueError: If the format is invalid
    """
    if not date_range.startswith('[') or not date_range.endswith(']'):
        raise ValueError("Date range must be enclosed in square brackets")

    # Remove brackets
    inner = date_range[1:-1]

    if '~' not in inner:
        raise ValueError("Date range must contain '~' separator")

    parts = inner.split('~', 1)
    if len(parts) != 2:
        raise ValueError("Date range must have exactly one '~' separator")

    before_str, after_str = parts

    before_date = None
    after_date = None

    # Parse before date
    if before_str.strip():
        try:
            before_date = datetime.strptime(before_str.strip(), "%Y-%m-%d")
        except ValueError:
            raise ValueError(f"Invalid before date format: {before_str}. Use YYYY-MM-DD format.")

    # Parse after date
    if after_str.strip():
        try:
            after_date = datetime.strptime(after_str.strip(), "%Y-%m-%d")
            # Add one day to make it exclusive (end of day)
            after_date = after_date + timedelta(days=1)
        except ValueError:
            raise ValueError(f"Invalid after date format: {after_str}. Use YYYY-MM-DD format.")

    return before_date, after_date


def str_to_bool(value: bool | str | None) -> bool | None:
    """
    Convert string representation of boolean to bool.
    
    Accepts bool, str, or None. String values are converted as follows:
    - 'True'/'true'/'1' -> True
    - 'False'/'false'/'0' -> False
    
    Args:
        value: The value to convert (bool, str, or None)
    
    Returns:
        bool | None: The converted boolean value, or None if input was None
    
    Raises:
        ToolError: If the string value is not a valid boolean representation
    """
    if value is None:
        return None
    if isinstance(value, bool):
        return value
    if isinstance(value, str):
        value_lower = value.lower().strip()
        if value_lower in ('true', '1'):
            return True
        elif value_lower in ('false', '0'):
            return False
        else:
            raise ToolError(
                f"Invalid boolean string value: '{value}'. "
                f"Accepted values are: 'True'/'true'/'1' for True, 'False'/'false'/'0' for False."
            )
    return value


def str_to_int(value: int | str | None) -> int | None:
    """
    Convert string representation of integer to int.
    
    Accepts int, str, or None. String values are automatically converted to integers.
    
    Args:
        value: The value to convert (int, str, or None)
    
    Returns:
        int | None: The converted integer value, or None if input was None
    
    Raises:
        ToolError: If the string value cannot be converted to an integer
    """
    if value is None:
        return None
    if isinstance(value, int):
        return value
    if isinstance(value, str):
        try:
            return int(value.strip())
        except ValueError:
            raise ToolError(
                f"Invalid integer string value: '{value}'. "
                f"Please provide a valid integer string (e.g., '0', '1', '-1')."
            )
    return value


def validate_and_create_value_constraint(default_value: str | None, fixed_value: str | None) -> ValueConstraint | None:
    """
    Validate and create a ValueConstraint object.
    
    Validation rules:
    - Can return None if both values are None
    - If not None, exactly one of default_value or fixed_value must be set (not both, not neither)
    
    Args:
        default_value: Default value for the component
        fixed_value: Fixed value for the component
        
    Returns:
        ValueConstraint | None: ValueConstraint object if valid, None if both are None
        
    Raises:
        ValueError: If both values are set or validation fails
    """
    if default_value is None and fixed_value is None:
        return None
    
    # Validate: exactly one must be set
    has_default = default_value is not None
    has_fixed = fixed_value is not None
    
    if has_default and has_fixed:
        raise ValueError(
            f"ValueConstraint validation failed: Both default_value and fixed_value cannot be set. "
            f"default_value='{default_value}', fixed_value='{fixed_value}'. "
            f"Exactly one must be set, the other must be None."
        )
    
    if not has_default and not has_fixed:
        raise ValueError(
            "ValueConstraint validation failed: Either default_value or fixed_value must be set. "
            "Both cannot be None."
        )
    
    return ValueConstraint(
        default_value=default_value,
        fixed_value=fixed_value
    )
