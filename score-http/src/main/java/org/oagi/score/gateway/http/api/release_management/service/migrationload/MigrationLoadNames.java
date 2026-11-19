package org.oagi.score.gateway.http.api.release_management.service.migrationload;

import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadJobId;

/**
 * The names of the schemas a load makes. Each starts with the name of the schema the application serves, so that one
 * privilege on the schemas of that name (the name and a '%') is all the database user of the application is given, whatever
 * the name of the served schema is.
 */
public final class MigrationLoadNames {

    private static final int LONGEST = 64;

    private MigrationLoadNames() {
    }

    public static String staging(String served, MigrationLoadJobId job) {
        return name(served, "_load_", job);
    }

    public static String backup(String served, MigrationLoadJobId job) {
        return name(served, "_backup_", job);
    }

    public static String discarded(String served, MigrationLoadJobId job) {
        return name(served, "_discarded_", job);
    }

    public static String probe(String served, MigrationLoadJobId job) {
        return name(served, "_probe_", job);
    }

    private static String name(String served, String kind, MigrationLoadJobId job) {
        if (served == null || !served.matches("[A-Za-z0-9_]+")) {
            throw new MigrationLoadRefusedException("The schema the application serves, '" + served + "', is not of letters, "
                    + "digits and underscores only, so a schema cannot be named after it.");
        }
        String name = served + kind + job;
        if (name.length() > LONGEST) {
            throw new MigrationLoadRefusedException("The name '" + name + "' of a schema of the load is longer than " + LONGEST + ".");
        }
        return name;
    }
}
