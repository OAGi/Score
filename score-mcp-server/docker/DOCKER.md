# Docker Setup

This project includes Docker support with supervisord and uvicorn.

## Building the Docker Image

```bash
docker build -f docker/Dockerfile -t oagi1docker/score-mcp-server:0.1.0 .
```

## Running the Container

You can run the container in two ways:
1. **Using environment variables** (passed directly to docker run)
2. **Using a .env file** (mounted into the container)

**Note:** Environment variables passed directly take precedence over values in the .env file.

### Method 1: Using Environment Variables

Pass all configuration as environment variables:

```bash
docker run -p 8000:8000 \
  -e DATABASE_URL="mariadb+mariadbconnector://user:password@host:port/database" \
  -e AUTH_PROVIDER="fastmcp.server.auth.oidc_proxy.OIDCProxy" \
  -e AUTH_PROVIDER_PARAMS_CONFIG_URL="https://your-oidc-provider.com/.well-known/openid-configuration" \
  -e AUTH_PROVIDER_PARAMS_CLIENT_ID="your_client_id" \
  -e AUTH_PROVIDER_PARAMS_CLIENT_SECRET="your_client_secret" \
  -e AUTH_PROVIDER_PARAMS_AUDIENCE="https://your-oidc-provider.com/api/v2/" \
  -e AUTH_PROVIDER_PARAMS_BASE_URL="http://localhost:8000" \
  -e HOST=0.0.0.0 \
  -e PORT=8000 \
  -e WORKERS=4 \
  score-mcp-server
```

### Method 2: Using .env File

When you mount a `.env` file, **all properties** from the file are automatically loaded and used by the application. This includes:
- All database configuration (`DATABASE_URL`, etc.)
- All authentication configuration (`AUTH_PROVIDER`, `AUTH_PROVIDER_PARAMS_*`, etc.)
- Uvicorn configuration (`HOST`, `PORT`, `WORKERS`)

1. Create a `.env` file in your project root with all configuration:

```bash
# .env file
DATABASE_URL=mariadb+mariadbconnector://user:password@host:port/database
AUTH_PROVIDER=fastmcp.server.auth.oidc_proxy.OIDCProxy
AUTH_PROVIDER_PARAMS_CONFIG_URL=https://your-oidc-provider.com/.well-known/openid-configuration
AUTH_PROVIDER_PARAMS_CLIENT_ID=your_client_id
AUTH_PROVIDER_PARAMS_CLIENT_SECRET=your_client_secret
AUTH_PROVIDER_PARAMS_AUDIENCE=https://your-oidc-provider.com/api/v2/
AUTH_PROVIDER_PARAMS_BASE_URL=http://localhost:8000
HOST=0.0.0.0
PORT=8000
WORKERS=4
```

2. Mount the .env file when running the container:

```bash
docker run -p 8000:8000 \
  -v $(pwd)/.env:/app/.env:ro \
  oagi1docker/score-mcp-server:0.1.0
```

**Note:** The application automatically loads all properties from `/app/.env` when the container starts. You don't need to specify individual environment variables when using a `.env` file.

### Overriding .env Values with Environment Variables

You can combine both methods - use .env file for most settings and override specific values:

```bash
docker run -p 9000:9000 \
  -v $(pwd)/.env:/app/.env:ro \
  -e PORT=9000 \
  -e WORKERS=8 \
  oagi1docker/score-mcp-server:0.1.0
```

In this example, `PORT` and `WORKERS` from environment variables will override the values in `.env`, while other settings will be loaded from the `.env` file.

### Using Docker Compose

#### Option A: Using environment variables in docker-compose.yml

Create a `docker-compose.yml` file:

```yaml
version: '3.8'

services:
  score-mcp-server:
    image: oagi1docker/score-mcp-server:0.1.0
    ports:
      - "8000:8000"
    environment:
      - HOST=0.0.0.0
      - PORT=8000
      - WORKERS=4
      - DATABASE_URL=mariadb+mariadbconnector://user:password@host:port/database
      - AUTH_PROVIDER=fastmcp.server.auth.oidc_proxy.OIDCProxy
      - AUTH_PROVIDER_PARAMS_CONFIG_URL=https://your-oidc-provider.com/.well-known/openid-configuration
      - AUTH_PROVIDER_PARAMS_CLIENT_ID=your_client_id
      - AUTH_PROVIDER_PARAMS_CLIENT_SECRET=your_client_secret
      - AUTH_PROVIDER_PARAMS_AUDIENCE=https://your-oidc-provider.com/api/v2/
      - AUTH_PROVIDER_PARAMS_BASE_URL=http://localhost:8000
    restart: unless-stopped
```

#### Option B: Using .env file with docker-compose

Create a `docker-compose.yml` file that mounts the .env file:

```yaml
version: '3.8'

services:
  score-mcp-server:
    image: oagi1docker/score-mcp-server:0.1.0
    ports:
      - "8000:8000"
    volumes:
      - ./.env:/app/.env:ro
    restart: unless-stopped
```

Then run:

```bash
docker-compose up -d
```

**Note:** Docker Compose automatically loads variables from a `.env` file in the same directory as `docker-compose.yml` for variable substitution in the compose file itself. However, to make the `.env` file available inside the container (for the application to use), you need to mount it as shown in Option B.

## Environment Variable Precedence

When using both methods, the precedence order is:
1. **Environment variables** passed directly (highest priority)
2. **Values from .env file** (if mounted)
3. **Default values** (lowest priority)

This means environment variables will always override .env file values.

## Environment Variables

### Uvicorn Configuration
- `HOST` - Host to bind to (default: `0.0.0.0`)
- `PORT` - Port to listen on (default: `8000`)
- `WORKERS` - Number of worker processes (default: `4`)

### Application Configuration
See `.env.example` for all available environment variables.

**Note:** The application automatically loads **all properties** from the `.env` file:
- The startup script (`start.sh`) loads the `.env` file and exports all variables to the shell environment
- The Python application (`main.py`) uses `python-dotenv` to load all variables from `/app/.env` into `os.environ`
- This ensures all configuration properties (database, auth, uvicorn settings, etc.) are available throughout the application
- Environment variables passed directly to Docker take precedence over `.env` file values

## Logs

Supervisord logs are available at:
- `/var/log/supervisor/supervisord.log` - Supervisord main log
- `/var/log/supervisor/uvicorn.out.log` - Uvicorn stdout/stderr

To view logs:

```bash
docker exec <container_id> tail -f /var/log/supervisor/uvicorn.out.log
```

## Health Check

The application includes a health check endpoint:

```bash
curl http://localhost:8000/health
```

