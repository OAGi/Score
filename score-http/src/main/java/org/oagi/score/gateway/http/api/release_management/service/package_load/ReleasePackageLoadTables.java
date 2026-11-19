package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.oagi.score.gateway.http.common.lock.DatabaseLock;

import java.util.Set;

/** What of the tables of the application belongs to the load and not to any format of a package. */
public final class ReleasePackageLoadTables {

    /** The record of the loads, which a load is followed by from its start to its end and after it, and the gate of the writers: the swap does not move them. */
    public static final Set<String> STAYS_IN_PLACE = Set.of("release_package_load_job", DatabaseLock.TABLE);

    /**
     * The lock that is the gate of the writers (a row of the table of the named locks): the writers of every process hold it shared until their
     * transaction ends, and a load takes it exclusive. InnoDB makes the exclusive request wait for the writers that are at work, and makes the writers
     * that come after it wait behind it, so it is what lets the whole application, in whatever process, stop writing for a while and go on after it.
     */
    public static final String WRITE_GATE = "write_gate";

    private ReleasePackageLoadTables() {
    }
}
