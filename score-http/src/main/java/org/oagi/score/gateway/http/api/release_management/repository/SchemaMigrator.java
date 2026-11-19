package org.oagi.score.gateway.http.api.release_management.repository;

/**
 * What brings a schema to the shape of a version of the application: an empty schema is made from the baseline, and is
 * migrated from there. How it is done (which tool, from where the migrations are read) is for the implementation, so that it
 * can be another one without anything that loads a release package changing.
 */
public interface SchemaMigrator {

    /**
     * Brings the schema to the version, the schema being one that exists and is empty, or one that was made by this.
     *
     * @param schema        the name of the schema, which is of letters, digits and underscores only
     * @param targetVersion the last version to apply, or {@code null} for the latest
     */
    void migrate(String schema, String targetVersion);
}
