package org.oagi.score.gateway.http.api.release_management.service.migrationload;

import java.util.Set;

/** What of the tables of the application belongs to the load and not to any format of a package. */
public final class MigrationLoadTables {

    /** The record of the loads, which a load is followed by from its start to its end and after it: the swap does not move it. */
    public static final Set<String> STAYS_IN_PLACE = Set.of("migration_load_job");

    private MigrationLoadTables() {
    }
}
