package org.oagi.score.gateway.http.api.release_management.service.package_load;

import java.util.Set;

/** What of the tables of the application belongs to the load and not to any format of a package. */
public final class ReleasePackageLoadTables {

    /** The record of the loads, which a load is followed by from its start to its end and after it, and the gate of the writers: the swap does not move them. */
    public static final Set<String> STAYS_IN_PLACE = Set.of("release_package_load_job", ReleasePackageLoadWriteGate.TABLE);

    private ReleasePackageLoadTables() {
    }
}
