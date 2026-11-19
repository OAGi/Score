package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJobId;

/**
 * The names of the schemas a load makes. Each starts with the name of the schema the application serves, so that one
 * privilege on the schemas of that name (the name and a '%') is all the database user of the application is given, whatever
 * the name of the served schema is.
 */
public final class ReleasePackageLoadNames {

    private static final int LONGEST = 64;

    private ReleasePackageLoadNames() {
    }

    public static String staging(String served, ReleasePackageLoadJobId job) {
        return name(served, "_load_", job);
    }

    public static String backup(String served, ReleasePackageLoadJobId job) {
        return name(served, "_backup_", job);
    }

    public static String discarded(String served, ReleasePackageLoadJobId job) {
        return name(served, "_discarded_", job);
    }

    public static String probe(String served, ReleasePackageLoadJobId job) {
        return name(served, "_probe_", job);
    }

    private static String name(String served, String kind, ReleasePackageLoadJobId job) {
        if (served == null || !served.matches("[A-Za-z0-9_]+")) {
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.UNSUPPORTED_NAME);
        }
        String name = served + kind + job;
        if (name.length() > LONGEST) {
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.UNSUPPORTED_NAME);
        }
        return name;
    }
}
