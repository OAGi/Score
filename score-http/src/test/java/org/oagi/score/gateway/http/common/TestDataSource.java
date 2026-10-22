package org.oagi.score.gateway.http.common;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;

/**
 * The data source of the application for a test that does not start the application: Spring's own configuration of it
 * ({@code spring.datasource.*} of {@code application.yml} and what overrides it, e.g. {@code DB_HOST}), with nothing else
 * of the application started. Close it when done.
 */
public final class TestDataSource extends DelegatingDataSource implements AutoCloseable {

    private final ConfigurableApplicationContext context;

    private TestDataSource(ConfigurableApplicationContext context) {
        super(context.getBean(DataSource.class));
        this.context = context;
    }

    /** @param maximumPoolSize the most connections the test needs */
    public static TestDataSource open(int maximumPoolSize) {
        return new TestDataSource(new SpringApplicationBuilder(DataSourceAutoConfiguration.class)
                .web(WebApplicationType.NONE)
                .bannerMode(org.springframework.boot.Banner.Mode.OFF)
                .properties("spring.datasource.hikari.maximum-pool-size=" + maximumPoolSize)
                .run());
    }

    @Override
    public void close() {
        context.close();
    }
}
