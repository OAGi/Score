package org.oagi.score.gateway.http.api.release_management.repository;

import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.release_management.repository.flyway.FlywaySchemaMigrator;
import org.oagi.score.gateway.http.api.release_management.repository.jooq.JooqReleasePackageLoadCommandRepository;
import org.oagi.score.gateway.http.api.release_management.repository.jooq.JooqReleasePackageLoadJobRepository;
import org.oagi.score.gateway.http.api.release_management.repository.jooq.JooqReleasePackageLoadQueryRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * The repositories of the loads of release packages. They are made here, beside the other repositories and not in them, so that what
 * loads a package is added to the application and nothing that was there is changed. None is for a user to ask.
 */
@Component
public class ReleasePackageLoadRepositories {

    private final DSLContext dslContext;
    private final ObjectProvider<SchemaMigrator> schemaMigrator;
    private final ObjectProvider<Flyway> flyway;
    private final ObjectProvider<DataSource> dataSource;

    /**
     * @param schemaMigrator what brings a schema to the shape of a version, when the application is given another than Flyway to do it
     * @param flyway         the Flyway of the application, which the Flyway migrator takes its configuration from; none when it is not enabled
     * @param dataSource     the data source of the application, which the Flyway migrator migrates a schema of the same database through
     */
    public ReleasePackageLoadRepositories(DSLContext dslContext, ObjectProvider<SchemaMigrator> schemaMigrator,
                                     ObjectProvider<Flyway> flyway, ObjectProvider<DataSource> dataSource) {
        this.dslContext = dslContext;
        this.schemaMigrator = schemaMigrator;
        this.flyway = flyway;
        this.dataSource = dataSource;
    }

    /** What is asked of the schemas of the server while a package is loaded. */
    public ReleasePackageLoadQueryRepository query() {
        return new JooqReleasePackageLoadQueryRepository(dslContext, null, null);
    }

    /** What loads a package into a schema of its own and swaps it in. */
    public ReleasePackageLoadCommandRepository command() {
        SchemaMigrator migrator = schemaMigrator.getIfAvailable(() -> {
            Flyway applicationFlyway = flyway.getIfAvailable();
            return new FlywaySchemaMigrator(dataSource.getIfAvailable(), applicationFlyway != null ? applicationFlyway.getConfiguration() : null);
        });
        return new JooqReleasePackageLoadCommandRepository(dslContext, null, null, migrator);
    }

    /** The record of the loads. */
    public ReleasePackageLoadJobRepository jobs() {
        return new JooqReleasePackageLoadJobRepository(dslContext, null, null);
    }
}
