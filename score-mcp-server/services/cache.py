"""
Cache module for Score MCP Server.

This module provides caching functionality with support for:
- InMemoryCache: Thread-safe in-memory caching
- RedisCache: Redis-based distributed caching
- NoneCache: No-op cache that disables caching (useful for testing/debugging)
- Cache decorator with eviction policy support (TTL, LRU, max_size)

Cache type is controlled via CACHE_TYPE environment variable:
- 'memory' or 'inmemory' for InMemoryCache
- 'redis' for RedisCache
- 'none' or 'nocache' for NoneCache (disables caching)
"""

import hashlib
import json
import logging
import os
import pickle
import time
from abc import ABC, abstractmethod
from collections import OrderedDict
from functools import wraps
from threading import Lock
from typing import Any, Callable, Optional, TypeVar, ParamSpec

# Configure logging
logger = logging.getLogger(__name__)

P = ParamSpec('P')
R = TypeVar('R')


class CacheInterface(ABC):
    """Abstract base class for cache implementations."""
    
    @abstractmethod
    def get(self, key: str) -> Optional[Any]:
        """Get a value from cache by key."""
        pass
    
    @abstractmethod
    def set(self, key: str, value: Any, ttl: Optional[int] = None) -> None:
        """Set a value in cache with optional TTL (time to live in seconds)."""
        pass
    
    @abstractmethod
    def delete(self, key: str) -> None:
        """Delete a key from cache."""
        pass
    
    @abstractmethod
    def clear(self) -> None:
        """Clear all entries from cache."""
        pass
    
    @abstractmethod
    def exists(self, key: str) -> bool:
        """Check if a key exists in cache."""
        pass
    
    @abstractmethod
    def evict_by_prefix(self, prefix: str) -> int:
        """Evict all cache entries that start with the given prefix.
        
        Args:
            prefix: The prefix to match against cache keys
            
        Returns:
            Number of keys evicted
        """
        pass


class InMemoryCache(CacheInterface):
    """
    Thread-safe in-memory cache implementation with TTL and LRU eviction.
    
    Features:
    - TTL (Time To Live) support for automatic expiration
    - LRU (Least Recently Used) eviction when max_size is reached
    - Thread-safe operations
    """
    
    def __init__(self, max_size: int = 1000, default_ttl: Optional[int] = None):
        """
        Initialize in-memory cache.
        
        Args:
            max_size: Maximum number of entries (LRU eviction when exceeded)
            default_ttl: Default TTL in seconds for entries (None = no expiration)
        """
        self.max_size = max_size
        self.default_ttl = default_ttl
        self._cache: OrderedDict[str, tuple[Any, Optional[float]]] = OrderedDict()
        self._lock = Lock()
        logger.info(f"Initialized InMemoryCache with max_size={max_size}, default_ttl={default_ttl}")
    
    def _is_expired(self, expiry_time: Optional[float]) -> bool:
        """Check if an entry has expired."""
        if expiry_time is None:
            return False
        return time.time() > expiry_time
    
    def _cleanup_expired(self) -> None:
        """Remove expired entries from cache."""
        current_time = time.time()
        expired_keys = [
            key for key, (_, expiry) in self._cache.items()
            if expiry is not None and current_time > expiry
        ]
        for key in expired_keys:
            del self._cache[key]
    
    def get(self, key: str) -> Optional[Any]:
        """Get a value from cache by key."""
        with self._lock:
            if key not in self._cache:
                return None
            
            value, expiry = self._cache[key]
            
            # Check if expired
            if self._is_expired(expiry):
                del self._cache[key]
                return None
            
            # Move to end (most recently used)
            self._cache.move_to_end(key)
            return value
    
    def set(self, key: str, value: Any, ttl: Optional[int] = None) -> None:
        """Set a value in cache with optional TTL."""
        with self._lock:
            # Cleanup expired entries periodically
            if len(self._cache) > 0 and len(self._cache) % 100 == 0:
                self._cleanup_expired()
            
            # Calculate expiry time
            # Use provided TTL, or default_ttl, or None (no expiration)
            expiry = None
            expire_time = ttl if ttl is not None else self.default_ttl
            if expire_time is not None:
                expiry = time.time() + expire_time
            
            # If key exists, remove it first (will be re-added at end)
            if key in self._cache:
                del self._cache[key]
            
            # If at max size, remove least recently used (first item)
            if len(self._cache) >= self.max_size:
                self._cache.popitem(last=False)  # Remove first (LRU)
            
            # Add new entry at end (most recently used)
            self._cache[key] = (value, expiry)
    
    def delete(self, key: str) -> None:
        """Delete a key from cache."""
        with self._lock:
            if key in self._cache:
                del self._cache[key]
    
    def clear(self) -> None:
        """Clear all entries from cache."""
        with self._lock:
            self._cache.clear()
    
    def exists(self, key: str) -> bool:
        """Check if a key exists in cache (and is not expired)."""
        with self._lock:
            if key not in self._cache:
                return False
            
            _, expiry = self._cache[key]
            if self._is_expired(expiry):
                del self._cache[key]
                return False
            
            return True
    
    def evict_by_prefix(self, prefix: str) -> int:
        """Evict all cache entries that start with the given prefix."""
        with self._lock:
            keys_to_delete = [key for key in self._cache.keys() if key.startswith(prefix)]
            for key in keys_to_delete:
                del self._cache[key]
            return len(keys_to_delete)


class NoneCache(CacheInterface):
    """
    No-op cache implementation that disables caching.
    
    All operations are no-ops, effectively turning off caching.
    Useful for testing, debugging, or when you want to ensure
    fresh data is always fetched from the source.
    """
    
    def __init__(self):
        """Initialize NoneCache (no-op)."""
        logger.info("Initialized NoneCache - caching is disabled")
    
    def get(self, key: str) -> Optional[Any]:
        """Get a value from cache by key (always returns None)."""
        return None
    
    def set(self, key: str, value: Any, ttl: Optional[int] = None) -> None:
        """Set a value in cache (no-op)."""
        pass
    
    def delete(self, key: str) -> None:
        """Delete a key from cache (no-op)."""
        pass
    
    def clear(self) -> None:
        """Clear all entries from cache (no-op)."""
        pass
    
    def exists(self, key: str) -> bool:
        """Check if a key exists in cache (always returns False)."""
        return False
    
    def evict_by_prefix(self, prefix: str) -> int:
        """Evict all cache entries that start with the given prefix (no-op, returns 0)."""
        return 0


class RedisCache(CacheInterface):
    """
    Redis-based cache implementation with TTL support.
    
    Supports three Redis deployment modes:
    - Single instance: Standard Redis server
    - Sentinel: High availability with automatic failover
    - Cluster: Distributed Redis with sharding
    
    Requires redis package and REDIS_CACHE_URL environment variable.
    Redis type can be detected from URL scheme or set explicitly via REDIS_CACHE_TYPE.
    """
    
    def __init__(self, redis_url: Optional[str] = None, redis_type: Optional[str] = None, 
                 default_ttl: Optional[int] = None, sentinel_master: Optional[str] = None,
                 sentinel_nodes: Optional[list] = None, cluster_nodes: Optional[list] = None):
        """
        Initialize Redis cache.
        
        Args:
            redis_url: Redis connection URL (defaults to REDIS_CACHE_URL env var)
                - Single: redis://host:port/db
                - Sentinel: redis+sentinel://host:port/db?master=master_name
                - Cluster: redis-cluster://host:port or redis://host:port (with type=cluster)
            redis_type: Explicit Redis type: 'single', 'sentinel', or 'cluster'
                If None, will be detected from URL scheme
            default_ttl: Default TTL in seconds for entries (None = no expiration)
            sentinel_master: Master name for Sentinel mode (defaults to 'mymaster')
            sentinel_nodes: List of (host, port) tuples for Sentinel nodes
            cluster_nodes: List of (host, port) tuples for Cluster nodes
        """
        try:
            import redis
            from redis.sentinel import Sentinel
            from redis.cluster import RedisCluster
        except ImportError:
            raise ImportError(
                "redis package is required for RedisCache. "
                "Install it with: pip install redis"
            )
        
        self.default_ttl = default_ttl
        redis_url = redis_url or os.getenv("REDIS_CACHE_URL", "redis://localhost:6379/0")
        
        # Determine Redis type: explicit config > URL scheme detection > default to single
        if redis_type is None:
            redis_type = os.getenv("REDIS_CACHE_TYPE", "").lower()
        
        if not redis_type:
            # Detect from URL scheme
            if redis_url.startswith("redis+sentinel://") or redis_url.startswith("rediss+sentinel://"):
                redis_type = "sentinel"
            elif redis_url.startswith("redis-cluster://") or redis_url.startswith("rediss-cluster://"):
                redis_type = "cluster"
            else:
                redis_type = "single"
        
        try:
            self._client = self._create_redis_client(
                redis, Sentinel, RedisCluster, redis_type, redis_url,
                sentinel_master, sentinel_nodes, cluster_nodes
            )
            # Test connection
            self._client.ping()
            logger.info(f"Initialized RedisCache type={redis_type}, url={redis_url}, default_ttl={default_ttl}")
        except Exception as e:
            logger.error(f"Failed to connect to Redis ({redis_type}): {e}")
            raise RuntimeError(f"Failed to initialize Redis cache: {e}")
    
    def _create_redis_client(self, redis, Sentinel, RedisCluster, redis_type: str, 
                             redis_url: str, sentinel_master: Optional[str],
                             sentinel_nodes: Optional[list], cluster_nodes: Optional[list]):
        """
        Create appropriate Redis client based on type.
        
        Args:
            redis: redis module
            Sentinel: redis.sentinel.Sentinel class
            RedisCluster: redis.cluster.RedisCluster class
            redis_type: 'single', 'sentinel', or 'cluster'
            redis_url: Connection URL
            sentinel_master: Master name for Sentinel
            sentinel_nodes: List of Sentinel node tuples
            cluster_nodes: List of Cluster node tuples
            
        Returns:
            Redis client instance
        """
        if redis_type == "sentinel":
            return self._create_sentinel_client(
                redis, Sentinel, redis_url, sentinel_master, sentinel_nodes
            )
        elif redis_type == "cluster":
            return self._create_cluster_client(
                RedisCluster, redis_url, cluster_nodes
            )
        else:  # single
            return redis.from_url(redis_url, decode_responses=False)
    
    def _create_sentinel_client(self, redis, Sentinel, redis_url: str,
                               sentinel_master: Optional[str], sentinel_nodes: Optional[list]):
        """
        Create Redis client connected via Sentinel.
        
        Supports both URL-based and explicit configuration.
        """
        # Parse Sentinel nodes and extract master name from URL if present
        from urllib.parse import urlparse, parse_qs
        
        parsed = urlparse(redis_url)
        query_params = parse_qs(parsed.query)
        
        # Get master name: explicit param > URL query > env var > default
        if not master_name:
            master_name = (
                query_params.get("master", [None])[0] or
                os.getenv("REDIS_CACHE_SENTINEL_MASTER", "mymaster")
            )
        
        # Parse Sentinel nodes
        if sentinel_nodes:
            sentinel_hosts = sentinel_nodes
        else:
            # Try to parse from URL or use env var
            sentinel_hosts_str = os.getenv("REDIS_CACHE_SENTINEL_NODES")
            if sentinel_hosts_str:
                # Format: "host1:port1,host2:port2,host3:port3"
                sentinel_hosts = [
                    tuple(host_port.split(":")) for host_port in sentinel_hosts_str.split(",")
                ]
            elif redis_url.startswith("redis+sentinel://") or redis_url.startswith("rediss+sentinel://"):
                # Parse from URL: redis+sentinel://[password@]host:port[/db]?master=name
                if parsed.hostname and parsed.port:
                    sentinel_hosts = [(parsed.hostname, parsed.port)]
                else:
                    raise ValueError("Invalid Sentinel URL format. Use redis+sentinel://host:port or set REDIS_CACHE_SENTINEL_NODES")
            else:
                # Default to localhost Sentinel
                sentinel_hosts = [("localhost", 26379)]
        
        # Convert ports to ints
        sentinel_hosts = [(host, int(port)) for host, port in sentinel_hosts]
        
        # Extract password from URL if present
        password = None
        if parsed.password:
            password = parsed.password
        
        # Create Sentinel connection
        sentinel = Sentinel(
            sentinel_hosts,
            socket_timeout=5,
            password=password
        )
        
        # Get master connection
        master = sentinel.master_for(master_name, socket_timeout=5, decode_responses=False)
        return master
    
    def _create_cluster_client(self, RedisCluster, redis_url: str, cluster_nodes: Optional[list]):
        """
        Create Redis Cluster client.
        
        Supports both URL-based and explicit configuration.
        """
        if cluster_nodes:
            startup_nodes = [{"host": host, "port": int(port)} for host, port in cluster_nodes]
        else:
            # Try to parse from URL or use env var
            cluster_nodes_str = os.getenv("REDIS_CACHE_CLUSTER_NODES")
            if cluster_nodes_str:
                # Format: "host1:port1,host2:port2,host3:port3"
                startup_nodes = []
                for host_port in cluster_nodes_str.split(","):
                    host, port = host_port.split(":")
                    startup_nodes.append({"host": host, "port": int(port)})
            elif redis_url.startswith("redis-cluster://") or redis_url.startswith("rediss-cluster://"):
                # Parse from URL: redis-cluster://host:port
                from urllib.parse import urlparse
                parsed = urlparse(redis_url)
                if parsed.hostname and parsed.port:
                    startup_nodes = [{"host": parsed.hostname, "port": parsed.port}]
                else:
                    raise ValueError("Invalid Cluster URL format. Use redis-cluster://host:port or set REDIS_CACHE_CLUSTER_NODES")
            else:
                # Try parsing regular redis:// URL as cluster entry point
                from urllib.parse import urlparse
                parsed = urlparse(redis_url)
                if parsed.hostname and parsed.port:
                    startup_nodes = [{"host": parsed.hostname, "port": parsed.port}]
                else:
                    raise ValueError("Invalid Cluster configuration. Provide REDIS_CACHE_CLUSTER_NODES or valid URL")
        
        # Extract password from URL if present
        password = None
        if "@" in redis_url:
            from urllib.parse import urlparse
            parsed = urlparse(redis_url)
            if parsed.password:
                password = parsed.password
        
        # Create Cluster client
        cluster = RedisCluster(
            startup_nodes=startup_nodes,
            decode_responses=False,
            password=password,
            skip_full_coverage_check=True  # Allow partial cluster discovery
        )
        return cluster
    
    def _serialize(self, value: Any) -> bytes:
        """Serialize value to bytes for Redis storage."""
        try:
            return pickle.dumps(value)
        except Exception as e:
            logger.error(f"Failed to serialize value: {e}")
            raise
    
    def _deserialize(self, data: bytes) -> Any:
        """Deserialize bytes from Redis to Python object."""
        try:
            return pickle.loads(data)
        except Exception as e:
            logger.error(f"Failed to deserialize value: {e}")
            raise
    
    def get(self, key: str) -> Optional[Any]:
        """Get a value from cache by key."""
        try:
            data = self._client.get(key)
            if data is None:
                return None
            return self._deserialize(data)
        except Exception as e:
            logger.error(f"Error getting key {key} from Redis: {e}")
            return None
    
    def set(self, key: str, value: Any, ttl: Optional[int] = None) -> None:
        """Set a value in cache with optional TTL."""
        try:
            serialized = self._serialize(value)
            # Use provided TTL, or default_ttl, or None (no expiration)
            expire_time = ttl if ttl is not None else self.default_ttl
            if expire_time is not None:
                self._client.setex(key, expire_time, serialized)
            else:
                self._client.set(key, serialized)
        except Exception as e:
            logger.error(f"Error setting key {key} in Redis: {e}")
            # Don't raise - cache failures shouldn't break the application
    
    def delete(self, key: str) -> None:
        """Delete a key from cache."""
        try:
            self._client.delete(key)
        except Exception as e:
            logger.error(f"Error deleting key {key} from Redis: {e}")
    
    def clear(self) -> None:
        """Clear all entries from cache."""
        try:
            self._client.flushdb()
        except Exception as e:
            logger.error(f"Error clearing Redis cache: {e}")
    
    def exists(self, key: str) -> bool:
        """Check if a key exists in cache."""
        try:
            return bool(self._client.exists(key))
        except Exception as e:
            logger.error(f"Error checking existence of key {key} in Redis: {e}")
            return False
    
    def evict_by_prefix(self, prefix: str) -> int:
        """Evict all cache entries that start with the given prefix."""
        try:
            # Check if this is a Cluster client (has scan_iter method)
            if hasattr(self._client, 'scan_iter'):
                # Redis Cluster uses scan_iter instead of scan
                deleted_count = 0
                for key in self._client.scan_iter(match=f"{prefix}*", count=100):
                    deleted_count += self._client.delete(key)
                return deleted_count
            else:
                # Single instance or Sentinel use scan
                deleted_count = 0
                cursor = 0
                while True:
                    cursor, keys = self._client.scan(cursor, match=f"{prefix}*", count=100)
                    if keys:
                        deleted_count += self._client.delete(*keys)
                    if cursor == 0:
                        break
                return deleted_count
        except Exception as e:
            logger.error(f"Error evicting keys with prefix {prefix} from Redis: {e}")
            return 0


# Global cache instance
_cache_instance: Optional[CacheInterface] = None
_cache_lock = Lock()


def get_cache() -> CacheInterface:
    """
    Get the global cache instance (singleton pattern).
    
    Cache type is determined by CACHE_TYPE environment variable:
    - 'memory' or 'inmemory' for InMemoryCache
    - 'redis' for RedisCache
    - 'none' or 'nocache' for NoneCache (disables caching)
    
    Additional environment variables:
    - CACHE_MAX_SIZE: Max size for InMemoryCache (default: 1000)
    - CACHE_DEFAULT_TTL: Default TTL in seconds (default: 60)
    - REDIS_CACHE_URL: Redis connection URL (default: redis://localhost:6379/0)
    - REDIS_CACHE_TYPE: Redis deployment type: 'single', 'sentinel', or 'cluster'
        If not set, will be detected from URL scheme (redis://, redis+sentinel://, redis-cluster://)
    - REDIS_CACHE_SENTINEL_MASTER: Master name for Sentinel mode (default: 'mymaster')
    - REDIS_CACHE_SENTINEL_NODES: Comma-separated Sentinel nodes (format: host1:port1,host2:port2)
    - REDIS_CACHE_CLUSTER_NODES: Comma-separated Cluster nodes (format: host1:port1,host2:port2)
    """
    global _cache_instance
    
    if _cache_instance is not None:
        return _cache_instance
    
    with _cache_lock:
        # Double-check locking pattern
        if _cache_instance is not None:
            return _cache_instance
        
        cache_type = os.getenv("CACHE_TYPE", "memory").lower()
        default_ttl = os.getenv("CACHE_DEFAULT_TTL")
        default_ttl = int(default_ttl) if default_ttl else 60  # Default to 1 minute
        
        if cache_type in ("memory", "inmemory"):
            max_size = int(os.getenv("CACHE_MAX_SIZE", "1000"))
            _cache_instance = InMemoryCache(max_size=max_size, default_ttl=default_ttl)
        elif cache_type == "redis":
            redis_url = os.getenv("REDIS_CACHE_URL")
            redis_type = os.getenv("REDIS_CACHE_TYPE")
            _cache_instance = RedisCache(redis_url=redis_url, redis_type=redis_type, default_ttl=default_ttl)
        elif cache_type in ("none", "nocache", "disabled"):
            _cache_instance = NoneCache()
        else:
            logger.warning(f"Unknown CACHE_TYPE '{cache_type}', defaulting to InMemoryCache")
            max_size = int(os.getenv("CACHE_MAX_SIZE", "1000"))
            _cache_instance = InMemoryCache(max_size=max_size, default_ttl=default_ttl)
        
        return _cache_instance


def evict_cache(key_prefix: str, *args, **kwargs) -> None:
    """
    Evict cache entries for a given key prefix and optional specific arguments.
    
    This function is used to invalidate cache entries when data is updated or deleted.
    It evicts:
    1. All entries matching the key_prefix (for list queries)
    2. A specific entry if args/kwargs are provided (for get queries)
    
    Args:
        key_prefix: The key prefix used in the cache decorator
        *args: Positional arguments to generate a specific cache key
        **kwargs: Keyword arguments to generate a specific cache key
    
    Example:
        # Evict all cached list queries
        evict_cache("ctx_category.get_ctx_categories")
        
        # Evict a specific cached get query
        evict_cache("ctx_category.get_ctx_category", ctx_category_id=1)
    """
    try:
        cache_instance = get_cache()
        
        # Always evict all entries with the prefix (for list queries)
        count = cache_instance.evict_by_prefix(f"{key_prefix}:")
        if count > 0:
            logger.debug(f"Evicted {count} cache entries with prefix '{key_prefix}:'")
        
        # If args or kwargs are provided, also evict the specific entry
        if args or kwargs:
            # Generate the specific cache key the same way the decorator does
            cache_key_hash = _generate_cache_key(key_prefix, args, kwargs)
            specific_key = f"{key_prefix}:{cache_key_hash}"
            cache_instance.delete(specific_key)
            logger.debug(f"Evicted specific cache entry: {specific_key}")
    except Exception as e:
        logger.warning(f"Failed to evict cache entries for prefix '{key_prefix}': {e}")


def _generate_cache_key(func_name: str, args: tuple, kwargs: dict) -> str:
    """
    Generate a cache key from function name and arguments.
    
    Args:
        func_name: Name of the function
        args: Positional arguments
        kwargs: Keyword arguments
        
    Returns:
        A string cache key
    """
    # Create a hashable representation of arguments
    key_parts = [func_name]
    
    # Add positional arguments
    if args:
        # Skip 'self' if it's a method
        args_to_hash = args[1:] if args and hasattr(args[0], '__class__') else args
        key_parts.append(str(args_to_hash))
    
    # Add keyword arguments (sorted for consistency)
    if kwargs:
        sorted_kwargs = sorted(kwargs.items())
        key_parts.append(str(sorted_kwargs))
    
    # Create a hash of the key parts
    key_string = "|".join(key_parts)
    return hashlib.sha256(key_string.encode()).hexdigest()


def cache(
    ttl: Optional[int] = None,
    key_prefix: Optional[str] = None,
    key_builder: Optional[Callable[[str, tuple, dict], str]] = None
):
    """
    Decorator to cache function results.
    
    Args:
        ttl: Time to live in seconds (overrides default_ttl from cache config)
        key_prefix: Optional string that will be used as func_name in _generate_cache_key.
                   If not provided, uses the actual function name.
        key_builder: Optional callable function that takes (func_name, args, kwargs) and returns a string.
                    If provided, this completely overrides the default key generation.
    
    Usage:
        @cache()  # Uses default key generation
        def get_data(self, id: int):
            return expensive_operation(id)
        
        @cache(key_prefix="custom_prefix")  # Uses "custom_prefix" as func_name in key generation
        def another_function(param: str):
            return result
        
        @cache(key_builder=lambda func_name, args, kwargs: f"custom_{args[0]}")  # Custom key function
        def custom_key_function(self, id: int):
            return result
    """
    def decorator(func: Callable[P, R]) -> Callable[P, R]:
        cache_instance = get_cache()
        func_name = f"{func.__module__}.{func.__qualname__}"
        
        @wraps(func)
        def wrapper(*args: P.args, **kwargs: P.kwargs) -> R:
            # Generate cache key
            if key_builder is not None:
                # Custom key builder takes precedence
                full_key = key_builder(func_name, args, kwargs)
            else:
                # Use key_prefix if provided, otherwise use func_name
                prefix = key_prefix if key_prefix is not None else func_name
                cache_key_hash = _generate_cache_key(prefix, args, kwargs)
                full_key = f"{prefix}:{cache_key_hash}"
            
            # Try to get from cache (fail gracefully if cache is unavailable)
            try:
                cached_value = cache_instance.get(full_key)
                if cached_value is not None:
                    logger.debug(f"Cache hit for {full_key}")
                    return cached_value
            except Exception as e:
                logger.warning(f"Cache get failed for {full_key}: {e}. Proceeding without cache.")
            
            # Cache miss - execute function
            logger.debug(f"Cache miss for {full_key}")
            result = func(*args, **kwargs)
            
            # Store in cache (fail gracefully if cache is unavailable)
            # Use provided TTL, or None to use cache instance's default_ttl
            try:
                cache_instance.set(full_key, result, ttl=ttl)
            except Exception as e:
                logger.warning(f"Cache set failed for {full_key}: {e}. Result returned without caching.")
            
            return result
        
        return wrapper
    return decorator

