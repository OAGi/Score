# Score MCP Server

A Model Context Protocol (MCP) server that provides programmatic access to [Score (connectCenter)](https://github.com/OAGi/Score) - a platform for managing business information standards, Core Components, Business Information Entities (BIEs), and related metadata.

> **Note**: This is a sub-project of [Score (connectCenter)](https://github.com/OAGi/Score), an open-source project by the Open Applications Group (OAGi).

## What is This Project?

Score MCP Server exposes Score's functionality through the Model Context Protocol, enabling AI assistants and other MCP-compatible clients to interact with Score's database programmatically. The server provides 59 tools covering:

- **Core Components**: ACCs (Aggregate Core Components), ASCCPs (Association Core Component Properties), BCCPs (Basic Core Component Properties)
- **Data Types**: Data type definitions and supplementary components
- **Code Lists**: Standardized lists of enumerated values
- **Agency ID Lists**: Agency identification lists
- **Business Information Entities (BIEs)**: Top-level ASBIEPs, ASBIEs, BBIEs, and their relationships
- **Business Contexts**: Context categories, context schemes, and business context values
- **Libraries**: Library management
- **Releases**: Version management for business information standards
- **Namespaces**: Namespace management for component scoping
- **Tags**: Component categorization and labeling
- **Users**: User management and authentication

## Prerequisites

- Python 3.13 or higher
- MariaDB/MySQL database with connectCenter schema
- OAuth2/OIDC provider (e.g., Auth0, GitHub) for authentication
- (Optional) Docker and Docker Compose for containerized deployment

## Installation

### Standalone Python Installation

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd score-mcp-server
   ```

2. **Install dependencies using `uv` (recommended):**
   ```bash
   # Install uv if you haven't already
   pip install uv
   
   # Install project dependencies
   uv sync
   ```

   Or using `pip`:
   ```bash
   pip install -e .
   ```

3. **Configure environment variables:**
   Copy `.env.example` to `.env` and update with your configuration:
   ```bash
   cp .env.example .env
   ```
   
   Then edit `.env` with your settings. See the [Configuration](#configuration) section below for details on all available options.

4. **Run the server:**
   ```bash
   # Using uv
   uv run python main.py
   
   # Or activate virtual environment and run
   source .venv/bin/activate  # On Windows: .venv\Scripts\activate
   python main.py
   ```

   The server will start on `http://localhost:8000` by default.

### Docker Installation

#### Option 1: Use Pre-built Image (Recommended)

1. **Pull the pre-built image:**
   ```bash
   docker pull oagi1docker/score-mcp-server:0.1.0
   ```

2. **Create a `.env` file** (copy from `.env.example` and update with your settings)

3. **Run the container:**
   ```bash
   docker run -d \
     --name score-mcp-server \
     -p 8000:8000 \
     --env-file .env \
     oagi1docker/score-mcp-server:0.1.0
   ```

   Or use Docker Compose (create `docker-compose.yml`):
   ```yaml
   version: '3.8'
   services:
     score-mcp-server:
       image: oagi1docker/score-mcp-server:0.1.0
       ports:
         - "8000:8000"
       env_file:
         - .env
       environment:
         - HOST=0.0.0.0
         - PORT=8000
         - WORKERS=4
       restart: unless-stopped
   ```

   Then run:
   ```bash
   docker-compose up -d
   ```

#### Option 2: Build from Source

1. **Build the Docker image:**
   ```bash
   docker build -t oagi1docker/score-mcp-server:0.1.0 -f docker/Dockerfile .
   ```

2. **Create a `.env` file** (copy from `.env.example` and update with your settings)

3. **Run the container:**
   ```bash
   docker run -d \
     --name score-mcp-server \
     -p 8000:8000 \
     --env-file .env \
     oagi1docker/score-mcp-server:0.1.0
   ```

## Using with MCP Hosts

### Claude Desktop

1. **Edit Claude Desktop's MCP configuration file:**
   - macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
   - Windows: `%APPDATA%\Claude\claude_desktop_config.json`

2. **Add the Score MCP Server configuration:**
   ```json
   {
     "mcpServers": {
       "score": {
         "url": "http://localhost:8000/mcp",
         "transport": "http"
       }
     }
   }
   ```

3. **Restart Claude Desktop**

### Cursor

1. **Open Cursor Settings** (Cmd/Ctrl + ,)

2. **Navigate to MCP Settings** or edit the MCP configuration file:
   - macOS: `~/Library/Application Support/Cursor/User/globalStorage/mcp.json`
   - Windows: `%APPDATA%\Cursor\User\globalStorage\mcp.json`

3. **Add the Score MCP Server:**
   For HTTP transport:
   ```json
   {
     "mcpServers": {
       "score": {
         "url": "http://localhost:8000/mcp",
         "transport": "http"
       }
     }
   }
   ```

4. **Restart Cursor**

### Other MCP Hosts

For other MCP-compatible hosts, configure them to connect to the Score MCP Server using either:

- **HTTP transport**: Connect to `http://localhost:8000/mcp` (or your configured host/port)

## Configuration

### Environment Variables

All configuration is done through environment variables. Copy `.env.example` to `.env` and update the values as needed.

#### Database Configuration

**Option 1: Full Connection String (Recommended)**
- `DATABASE_URL` - Full database connection string (e.g., `mariadb+mariadbconnector://user:password@host:port/database`)
  - Format: `mariadb+mariadbconnector://user:password@host:port/database`
  - If this is set, individual `DATABASE_*` variables below are ignored

**Option 2: Individual Components** (used if `DATABASE_URL` is not set)
- `DATABASE_CONNECTOR` - Database connector/driver (default: `mariadb+mariadbconnector`)
- `DATABASE_HOSTNAME` - Database host address (default: `localhost`)
- `DATABASE_PORT` - Database port number (default: `3306` for MariaDB/MySQL)
- `DATABASE_USERNAME` - Database username
- `DATABASE_PASSWORD` - Database password
- `DATABASE_NAME` - Database name

#### Authentication Configuration

- `AUTH_PROVIDER` - Authentication provider class path (e.g., `fastmcp.server.auth.oidc_proxy.OIDCProxy`)
  - If not set, authentication is disabled
- `AUTH_PROVIDER_PARAMS_CONFIG_URL` - OIDC configuration discovery URL (e.g., `.well-known/openid-configuration` endpoint)
- `AUTH_PROVIDER_PARAMS_CLIENT_ID` - OAuth2 client ID from your authentication provider
- `AUTH_PROVIDER_PARAMS_CLIENT_SECRET` - OAuth2 client secret from your authentication provider
- `AUTH_PROVIDER_PARAMS_AUDIENCE` - OAuth2 audience/API identifier
- `AUTH_PROVIDER_PARAMS_BASE_URL` - Base URL of this application (used for redirects and callbacks, default: `http://localhost:8000`)

#### Server Configuration

- `HOST` - Host address to bind the server to (default: `0.0.0.0`)
  - `0.0.0.0` = all interfaces
  - `127.0.0.1` = localhost only
- `PORT` - Port number to listen on (default: `8000`)
- `WORKERS` - Number of uvicorn worker processes (default: `4`)
  - For production, typically 2-4x CPU cores
  - Note: Only used in Docker/startup scripts, not in main.py
- `LOG_LEVEL` - Logging level: `DEBUG`, `INFO`, `WARNING`, `ERROR`, `CRITICAL` (default: `INFO`)
- `STATELESS_HTTP` - Enable stateless HTTP mode (default: `true`)
  - Required for Docker/containerized deployments
  - If CORS is enabled, this is automatically set to `true`

#### CORS Configuration (Optional)

- `CORS_ENABLED` - Enable CORS (Cross-Origin Resource Sharing) middleware (default: `false`)
- `CORS_ALLOW_ORIGINS` - Allowed origins (comma-separated list or `*` for all origins, default: `*`)
- `CORS_ALLOW_METHODS` - Allowed HTTP methods (comma-separated list or `*` for all methods, default: `*`)
- `CORS_ALLOW_HEADERS` - Allowed HTTP headers (comma-separated list or `*` for all headers, default: `*`)
- `CORS_EXPOSE_HEADERS` - Exposed HTTP headers (comma-separated list or `*` for all headers, default: `*`)
- `CORS_ALLOW_CREDENTIALS` - Allow credentials (cookies, authorization headers) in CORS requests (default: `false`)
- `CORS_MAX_AGE` - Maximum age (in seconds) for preflight OPTIONS request caching (default: `600`)

#### Cache Configuration

- `CACHE_TYPE` - Cache type: `memory` (or `inmemory`) for in-memory cache, `redis` for Redis cache (default: `memory`)
  - In-memory cache is faster but limited to a single process/container
  - Redis cache allows sharing cache across multiple processes/containers

**InMemoryCache Settings** (only used when `CACHE_TYPE=memory`)
- `CACHE_MAX_SIZE` - Maximum number of cache entries before LRU (Least Recently Used) eviction (default: `1000`)
  - When limit is reached, oldest entries are removed to make room for new ones
- `CACHE_DEFAULT_TTL` - Default TTL (Time To Live) in seconds for cache entries (default: `60`)
  - After this time, cached entries expire and are removed
  - Individual cache decorators can override this with their own TTL

**RedisCache Settings** (only used when `CACHE_TYPE=redis`)
- `REDIS_CACHE_URL` - Redis Cache connection URL (default: `redis://localhost:6379/0`)
  - Supported URL schemes:
    - Single instance: `redis://[password@]host:port[/database_number]`
    - Sentinel: `redis+sentinel://[password@]host:port[/database_number]?master=master_name`
    - Cluster: `redis-cluster://[password@]host:port`
  - Database number (0-15) is optional, defaults to 0 if not specified
  - Examples:
    - `redis://localhost:6379/0`
    - `redis://:password@redis.example.com:6379/1`
- `REDIS_CACHE_TYPE` - Redis deployment type: `single`, `sentinel`, or `cluster`
  - If not set, will be auto-detected from `REDIS_CACHE_URL` scheme:
    - `redis://` or `rediss://` → single instance
    - `redis+sentinel://` or `rediss+sentinel://` → Sentinel
    - `redis-cluster://` or `rediss-cluster://` → Cluster

**Redis Sentinel Configuration** (only used when `CACHE_TYPE=redis` and `REDIS_CACHE_TYPE=sentinel`)
- `REDIS_CACHE_SENTINEL_MASTER` - Master name for Sentinel mode (default: `mymaster`)
- `REDIS_CACHE_SENTINEL_NODES` - Comma-separated list of Sentinel node addresses
  - Format: `host1:port1,host2:port2,host3:port3`
  - If not set, will try to parse from `REDIS_CACHE_URL` if it uses `redis+sentinel://` scheme
  - Example: `sentinel1.example.com:26379,sentinel2.example.com:26379,sentinel3.example.com:26379`

**Redis Cluster Configuration** (only used when `CACHE_TYPE=redis` and `REDIS_CACHE_TYPE=cluster`)
- `REDIS_CACHE_CLUSTER_NODES` - Comma-separated list of Cluster node addresses
  - Format: `host1:port1,host2:port2,host3:port3`
  - At least one node is required for cluster discovery. The client will discover other nodes automatically.
  - If not set, will try to parse from `REDIS_CACHE_URL` if it uses `redis-cluster://` scheme
  - Example: `cluster1.example.com:6379,cluster2.example.com:6379,cluster3.example.com:6379`

## Health Check

The server provides a health check endpoint:

```bash
curl http://localhost:8000/health
```

Response:
```json
{
  "status": "healthy",
  "service": "score-mcp-server"
}
```

## Troubleshooting

### Database Connection Issues

- Verify database credentials in `.env` file
- Check database server is running and accessible
- Ensure database schema is initialized

### Authentication Issues

- Verify OAuth2/OIDC provider configuration
- Check client ID, secret, and audience are correct
- Ensure token has required scopes

## License

This project is licensed under the MIT License, consistent with the main [Score (connectCenter)](https://github.com/OAGi/Score) project.

See the [LICENSE.txt](https://github.com/OAGi/Score/blob/master/LICENSE.txt) file in the main Score repository for details.

## Contributing

This is a sub-project of [Score (connectCenter)](https://github.com/OAGi/Score). Contributions are welcome!

### How to Contribute

1. **Fork the repository** and create a feature branch
2. **Make your changes** following the existing code style
3. **Add tests** for new functionality (aim for 100% test coverage)
4. **Run the test suite** to ensure all tests pass:
   ```bash
   pytest
   ```
5. **Submit a pull request** with a clear description of your changes

### Contribution Guidelines

- Follow the existing code style and conventions
- Write clear commit messages
- Ensure all tests pass before submitting
- Update documentation as needed
- Reference related issues in your pull request

For more detailed contribution guidelines, please refer to the [Contributing guide](https://github.com/OAGi/Score/blob/master/CONTRIBUTING.md) in the main Score repository.

## Support

### Getting Help

- **Issues**: Report bugs or request features by opening an issue in the main [Score repository](https://github.com/OAGi/Score/issues)
- **Documentation**: Check the [Score documentation](https://github.com/OAGi/Score) for more information about connectCenter
- **Community**: Engage with the OAGi community through the main Score project

### About Score (connectCenter)

Score MCP Server is a sub-project of [Score (connectCenter)](https://github.com/OAGi/Score), an open-source platform for managing business information standards, Core Components, and Business Information Entities. connectCenter was developed through collaboration between the Open Applications Group (OAGi) and the National Institute of Standards and Technology (NIST).

For more information about connectCenter, visit the [main repository](https://github.com/OAGi/Score).

