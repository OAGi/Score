package org.oagi.score.gateway.http.api.release_management.dataload.info;

public class Info {

    private String version;
    private String description;
    private String maintainer;

    /** From format 1.2: the version of the schema the package was written from; {@code null} for a package that does not say. */
    private String schemaVersion;
    private Migration migration;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMaintainer() {
        return maintainer;
    }

    public void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
    }

    public Migration getMigration() {
        return migration;
    }

    public void setMigration(Migration migration) {
        this.migration = migration;
    }


    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }
}
