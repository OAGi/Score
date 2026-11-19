package org.oagi.score.gateway.http.api.release_management.model.package_info;

import org.springframework.util.StringUtils;

import java.util.List;

public class Metadata {

    private List<Release> releases;
    private List<Table> tables;

    public List<Release> getReleases() {
        return releases;
    }

    public void setReleases(List<Release> releases) {
        this.releases = releases;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public boolean hasRelease(long releaseId) {
        return this.releases.stream().filter(e -> e.getId() == releaseId).count() > 0L;
    }

    public Table getTable(String tableName) {
        if (!StringUtils.hasLength(tableName)) {
            throw new IllegalArgumentException("'tableName' parameter must be not null.");
        }
        Table table = this.tables.stream().filter(e -> tableName.equalsIgnoreCase(e.getTableName()))
                .findFirst().orElse(null);
        if (table == null) {
            throw new IllegalArgumentException("No '" + tableName + "' table found.");
        }
        return table;
    }
}
