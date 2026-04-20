from __future__ import annotations

from functools import lru_cache
from typing import Any, get_args, get_origin, get_type_hints

from dataclasses import fields, is_dataclass


def to_plain_data(value: Any) -> Any:
    if value is None:
        return None
    if is_dataclass(value):
        return {f.name: to_plain_data(getattr(value, f.name)) for f in fields(value)}
    if isinstance(value, list):
        return [to_plain_data(v) for v in value]
    if isinstance(value, tuple):
        return [to_plain_data(v) for v in value]
    if isinstance(value, dict):
        return {k: to_plain_data(v) for k, v in value.items()}
    if hasattr(value, "__dict__"):
        return {k: to_plain_data(v) for k, v in value.__dict__.items() if not k.startswith("_")}
    return value


def to_dataclass(cls: type, value: Any) -> Any:
    if value is None:
        return None
    if isinstance(value, cls):
        return value

    data = to_plain_data(value)
    if not isinstance(data, dict):
        return data

    resolved_types = _resolved_field_types(cls)
    kwargs: dict[str, Any] = {}
    for f in fields(cls):
        if f.name not in data:
            continue
        kwargs[f.name] = _coerce_type(resolved_types.get(f.name, f.type), data[f.name])
    return cls(**kwargs)


@lru_cache(maxsize=None)
def _resolved_field_types(cls: type) -> dict[str, Any]:
    try:
        return get_type_hints(cls, include_extras=True)
    except Exception:
        return {}


def _coerce_type(tp: Any, value: Any) -> Any:
    if value is None:
        return None

    origin = get_origin(tp)
    if origin is None:
        if isinstance(tp, type) and is_dataclass(tp):
            return to_dataclass(tp, value)
        return value

    if str(origin) == "typing.Annotated":
        inner = get_args(tp)[0]
        return _coerce_type(inner, value)

    if origin in (list,):
        inner = get_args(tp)[0] if get_args(tp) else Any
        return [_coerce_type(inner, v) for v in value]

    if origin in (tuple,):
        inner = get_args(tp)[0] if get_args(tp) else Any
        return tuple(_coerce_type(inner, v) for v in value)

    if origin in (dict,):
        args = get_args(tp)
        inner = args[1] if len(args) == 2 else Any
        return {k: _coerce_type(inner, v) for k, v in value.items()}

    args = [a for a in get_args(tp) if a is not type(None)]
    for a in args:
        if isinstance(a, type) and is_dataclass(a):
            try:
                return to_dataclass(a, value)
            except Exception:
                continue
    return value
