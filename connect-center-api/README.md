# connectCenter API

## Setup

1. Install dependencies:

```bash
python -m venv .venv
source .venv/bin/activate
pip install -e .
```

This installs the default MariaDB runtime stack, including `asyncmy`.

2. Configure environment:

```bash
cp .env.example .env
```

3. Run:

```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 5555
```

## Vendor Installs (Extras)
```

Install development tools:

```bash
pip install -e ".[dev]"
```

## MCP

The API process now exposes a built-in FastMCP server at `http://127.0.0.1:5555/mcp`.

## Testing

- Run route/service tests for app users:

```bash
./.venv/bin/pytest -q tests/routes/test_app_user_routes.py tests/service/test_app_user_service.py
```

- Run with parallel workers (requires `pytest-xdist`):

```bash
./.venv/bin/pip install -e ".[dev]"
./.venv/bin/pytest -n auto -q tests/routes/test_app_user_routes.py tests/service/test_app_user_service.py
```
```
