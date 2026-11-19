package org.oagi.score.gateway.http.api.release_management.service.package_load;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Makes every connection that the pool has now a thing of the past: the ones that are idle are closed, the ones that are in use are
 * closed when they are given back, and the pool makes new ones for what is asked after that. It is what follows a swap, so that no
 * connection that was opened for the schema as it was goes on being used for the one that is served now.
 */
@Component
public class ReleasePackageLoadConnectionPoolRefresher {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DataSource dataSource;

    public ReleasePackageLoadConnectionPoolRefresher(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** @return whether the pool is one that can do it (a pool of Hikari) */
    public boolean refresh() {
        try {
            if (dataSource.isWrapperFor(HikariDataSource.class)) {
                HikariPoolMXBean pool = dataSource.unwrap(HikariDataSource.class).getHikariPoolMXBean();
                if (pool != null) {
                    pool.softEvictConnections();
                    return true;
                }
            }
        } catch (SQLException e) {
            logger.warn("The connections of the pool could not be made new: " + e.getMessage());
        }
        return false;
    }
}
