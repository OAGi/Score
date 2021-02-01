package org.oagi.score.repo.api.impl.jooq;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.Catalog;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.Schema;
import org.jooq.impl.DefaultDSLContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.List;
import java.util.Properties;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractJooqScoreRepositoryTest {

    protected final Log logger = LogFactory.getLog(getClass());

    private Driver driver;
    private DataSource dataSource;
    private DSLContext dslContext;
    private ScoreRepositoryFactory scoreRepositoryFactory;

    @BeforeAll
    void initAll() throws Exception {
        // turn off displaying jooq logo
        System.setProperty("org.jooq.no-logo", "true");

        Properties properties = new Properties();
        try (InputStream inputStream = ClassUtils.getDefaultClassLoader().getResourceAsStream("application.properties")) {
            properties.load(inputStream);
        }

        String jdbcDriverClass = (String) properties.get("jdbc.driver");
        if (!StringUtils.hasLength(jdbcDriverClass)) {
            throw new IllegalStateException("'jdbc.driver' property does not define.");
        }
        String jdbcUrl = (String) properties.get("jdbc.url");
        if (!StringUtils.hasLength(jdbcUrl)) {
            throw new IllegalStateException("'jdbc.url' property does not define.");
        }
        String jdbcUser = (String) properties.get("jdbc.user");
        if (!StringUtils.hasLength(jdbcUser)) {
            throw new IllegalStateException("'jdbc.user' property does not define.");
        }
        String jdbcPassword = (String) properties.get("jdbc.password");
        if (!StringUtils.hasLength(jdbcPassword)) {
            throw new IllegalStateException("'jdbc.password' property does not define.");
        }

        DriverManagerDataSource dataSource = new DriverManagerDataSource(jdbcUrl, jdbcUser, jdbcPassword);
        dataSource.setDriverClassName(jdbcDriverClass);

        Driver driver = (Driver) Class.forName(jdbcDriverClass, true, ClassUtils.getDefaultClassLoader())
                .getConstructor(new Class[0]).newInstance(new Object[0]);
        DriverManager.registerDriver(driver);

        this.driver = driver;
        this.dataSource = dataSource;
        this.dslContext = new DefaultDSLContext(dataSource, SQLDialect.MYSQL);
        this.scoreRepositoryFactory =
                new JooqAccessControlScoreRepositoryFactory(new JooqScoreRepositoryFactory(dslContext));

        List<Catalog> catalogs = this.dslContext.meta().getCatalogs();
        if (catalogs.isEmpty()) {
            throw new IllegalStateException();
        }

        Schema schema = catalogs.get(0).getSchema("oagi");
        if (schema == null) {
            throw new IllegalStateException();
        }
    }

    public final DSLContext dslContext() {
        return dslContext;
    }

    public final ScoreRepositoryFactory scoreRepositoryFactory() {
        return scoreRepositoryFactory;
    }

    @AfterAll
    void tearDownAll() throws Exception {
        if (driver != null) {
            DriverManager.deregisterDriver(driver);
        }
    }
}
