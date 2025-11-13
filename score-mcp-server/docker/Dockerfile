FROM python:3.13-slim

# Install system dependencies
RUN apt-get update && apt-get install -y \
    supervisor \
    gcc \
    g++ \
    libmariadb-dev \
    pkg-config \
    && rm -rf /var/lib/apt/lists/*

# Install uv for faster dependency management
RUN pip install --no-cache-dir uv

# Set working directory
WORKDIR /app

# Copy dependency files
COPY pyproject.toml uv.lock ./

# Install Python dependencies using uv
RUN uv sync --frozen --no-dev

# Copy application code - only services, tools, and root Python files
COPY services/ ./services/
COPY tools/ ./tools/
COPY *.py ./

# Copy and make start script executable
COPY docker/start.sh /app/start.sh
RUN chmod +x /app/start.sh

# Create directory for supervisord
RUN mkdir -p /var/log/supervisor

# Copy supervisord configuration
COPY docker/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Expose port (default 8000, can be overridden via env)
EXPOSE 8000

# Set default environment variables
ENV HOST=0.0.0.0
ENV PORT=8000
ENV WORKERS=4

# Start supervisord
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]

