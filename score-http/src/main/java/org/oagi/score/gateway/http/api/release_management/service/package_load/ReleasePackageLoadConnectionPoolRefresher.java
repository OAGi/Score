package org.oagi.score.gateway.http.api.release_management.service.package_load;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.springframework.boot.jdbc.DataSourceUnwrapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Makes every connection that the pool has now a thing of the past: the ones that are idle are closed, the ones that are in use are
 * closed when they are given back, and the pool makes new ones for what is asked after that. It is what follows a swap, so that no
 * connection that was opened for the schema as it was goes on being used for the one that is served now.
 * <p>
 * Spring has no eviction that every pool shares, so the pool that the application is configured with must be the one this knows (Hikari,
 * the default of Spring Boot). The data source is unwrapped through the proxies Spring puts around it (transaction-aware, lazy, routing,
 * AOP), so a wrapped Hikari pool is found too. Whether the pool can be evicted is asked <em>before</em> anything is changed
 * ({@link #requireEvictable()}), so that a load is never swapped in with connections that cannot be renewed.
 */
@Component
public class ReleasePackageLoadConnectionPoolRefresher {

    private final DataSource dataSource;

    public ReleasePackageLoadConnectionPoolRefresher(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** @throws ReleasePackageLoadRefusedException when the pool cannot be evicted */
    public void requireEvictable() {
        poolOf(dataSource);
    }

    /** Evicts the connections of the pool; fails loudly when that cannot be done, as a swap that is served by old connections is a wrong one. */
    public void refresh() {
        poolOf(dataSource).softEvictConnections();
    }

    private static HikariPoolMXBean poolOf(DataSource dataSource) {
        HikariDataSource hikari = DataSourceUnwrapper.unwrap(dataSource, HikariDataSource.class);
        HikariPoolMXBean pool = hikari == null ? null : hikari.getHikariPoolMXBean();
        if (pool == null) {
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.POOL_NOT_EVICTABLE);
        }
        return pool;
    }
}
