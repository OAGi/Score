package org.oagi.score.gateway.http.api.release_management.repository.flyway;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.Configuration;
import org.oagi.score.gateway.http.api.release_management.repository.SchemaMigrator;

import javax.sql.DataSource;

/**
 * Migrates a schema with Flyway, the way the application's own is: it is given the configuration Spring has made of the
 * {@code spring.flyway.*} properties (where the migrations are, the table of the history, the placeholders, and what a
 * customizer of the application has added), and changes only what makes it a schema of its own: the schema, the data source
 * and the version to stop at.
 *
 * <p>It does not baseline: the application's own may adopt a database that has tables but no history, as of its baseline
 * version, but a schema that is to be loaded into must be empty, and one that is not is refused.</p>
 */
public class FlywaySchemaMigrator implements SchemaMigrator {

    private final DataSource dataSource;

    private final Configuration applicationConfiguration;

    /**
     * @param dataSource               the data source of the database the schema is in
     * @param applicationConfiguration the configuration of the Flyway of the application, or {@code null} when it has none
     *                                 (Flyway is not enabled), in which case there is nothing to migrate by
     */
    public FlywaySchemaMigrator(DataSource dataSource, Configuration applicationConfiguration) {
        this.dataSource = dataSource;
        this.applicationConfiguration = applicationConfiguration;
    }

    @Override
    public void migrate(String schema, String targetVersion) {
        if (applicationConfiguration == null) {
            throw new IllegalStateException("Flyway is not enabled (spring.flyway.enabled), so there are no migrations to bring a schema to a version by.");
        }
        Flyway.configure()
                .configuration(applicationConfiguration)
                .dataSource(dataSource)
                .schemas(schema)
                .defaultSchema(schema)
                .baselineOnMigrate(false)
                .target(targetVersion == null ? "latest" : targetVersion)
                .load()
                .migrate();
    }
}
