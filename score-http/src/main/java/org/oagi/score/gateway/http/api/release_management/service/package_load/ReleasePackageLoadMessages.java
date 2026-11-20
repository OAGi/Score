package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJobId;

/**
 * What is said to whoever loads a release package, in words of what they do and what they can do about it: no names of schemas, tables or
 * columns, and nothing of how the program failed (that is for the log, where the number of the load finds it).
 */
public final class ReleasePackageLoadMessages {

    private ReleasePackageLoadMessages() {
    }

    public static final String ONLY_ADMINISTRATOR = "Only an administrator can use release packages.";

    // -- the package comes -------------------------------------------------------------------------------------------
    public static final String NOT_RECEIVED = "The file could not be received. Please try again.";
    public static final String NO_ROOM = "The server cannot keep the file. Please contact your administrator.";
    public static final String NOT_READ = "The file could not be read. Please try again.";
    public static final String NOT_A_PACKAGE = "This file is not a release package. Choose the .zip file made by 'Generate Migration Script' of a release.";
    public static final String GONE = "This file is no longer on the server (it was used, discarded, or kept too long). Please choose it again.";
    public static final String NOT_READY = "This file is not ready yet. Please wait until it is received.";
    public static final String POOL_NOT_EVICTABLE = "The server cannot renew its database connections, which a load needs once it is swapped in (its connection pool is not the one it supports). Nothing was changed.";
    public static final String BEING_LOADED = "This file is already being processed.";
    public static final String BAD_FILE_NAME = "The name of the file may have only letters, digits, dots, dashes and underscores, and must end with .zip.";

    public static String tooLarge(String size) {
        return "The file is larger than the " + size + " that a release package may be.";
    }

    // -- what is in the way ------------------------------------------------------------------------------------------
    public static final String ANOTHER_LOAD = "Another release package is being processed. Please wait until it is finished.";
    public static final String BUSY_RELEASE = "A release is being drafted or published. Please wait until it is finished, then check again.";
    public static final String SHUTTING_DOWN = "The server is shutting down. Please try again when it is back.";
    public static final String NOT_ALLOWED_TO_CREATE_DATABASES = "The database account of the application is not allowed to create databases. Please contact your administrator.";
    public static final String CANNOT_STOP_WRITING = "Saving by the users could not be paused. Please try again; if it does not work, contact your administrator.";
    public static final String NO_WAY_TO_STOP_WRITING = "This server cannot pause saving by the users, so a release package cannot be applied here. Please contact your administrator.";
    public static final String WRITING_PAUSED = "The data is being updated with a release package. Please try again in a moment.";
    public static final String UNSUPPORTED_NAME = "Release packages cannot be used with this database. Please contact your administrator.";

    public static String libraryIsAhead(String library, String current, String packaged) {
        return "The library '" + library + "' of this database is at " + current + ", which is later than " + packaged
                + " of the package. Choose a package that has " + current + " or later.";
    }

    public static String libraryIsMissing(String library, String current) {
        return "The package has no release of the library '" + library + "', which this database has at " + current
                + ". Choose a package that has " + current + " or later.";
    }

    public static String unknownFormat(String version) {
        return "This package was made by a version of connectCenter that this server cannot read (" + version + "). Make it again with this version.";
    }

    public static String scriptNotSql(String script) {
        return "The package has a file that is not a SQL script: " + script + ". Make the package again.";
    }

    public static String scriptMissing(String script) {
        return "The package is incomplete: " + script + " is not in it. Make the package again.";
    }

    // -- the load ----------------------------------------------------------------------------------------------------
    public static final String LOADING = "Loading the contents of the package";
    public static final String MIGRATING = "Updating the loaded data to the version of this application";
    public static final String COPYING = "Saving is paused for a short while. Copying the work of the users";

    public static String copying(int done, int total) {
        return "Copying the work of the users (" + done + " of " + total + "). Saving is paused for a short while";
    }
    public static final String VERIFYING = "Checking that the loaded data is complete and consistent";
    public static final String SWAPPING = "Putting the loaded data in place";
    public static final String SUCCEEDED = "The release package is applied. The data from before is kept so that this can be undone.";
    public static final String STALE_SCREENS = " Some screens may show old data until the server is restarted.";
    public static final String LEFTOVERS = " Temporary data could not be cleaned up; please tell your administrator.";
    public static final String UNEXPECTED = "The operation stopped because of an unexpected problem. Please try again, and contact your administrator if it happens again.";
    public static final String AFTER_LOAD_PROBLEM = "The release package is applied, but something after it did not finish. Please tell your administrator.";
    public static final String KEPT_ASIDE = " The data from before is kept aside; please contact your administrator.";

    public static String stopped(String reasons) {
        return "It was stopped before anything was changed: " + reasons;
    }

    public static final String STRUCTURE_DIFFERS = "the structure of the loaded data does not match this application.";
    public static final String NOT_ALL_CARRIED_OVER = "some of the work of the users could not be carried over.";
    public static final String CHANGED_WHILE_COPIED = "data was changed by someone while it was being copied. Please try again.";
    /** What is said when someone is asked to pass the job on: its number is shown in the window of the update, and its progress details are there to be copied. */
    public static String passOn(ReleasePackageLoadJobId job) {
        return "Please contact the developers and give them the job number " + job.value() + " (shown at the top of this window) and the text of 'Progress details'.";
    }

    public static String missingUsers(String what) {
        return "after the update, some data made by users would refer to user accounts that are not there (" + what + ").";
    }

    public static String missingRecords(String what) {
        return "after the update, some records would refer to records that are not there (" + what + ").";
    }

    /** The load did not start to copy: the accounts, the namespaces, or both, of this database and of the package do not go together. */
    public static String conflicts(String accounts, String namespaces, ReleasePackageLoadJobId job) {
        StringBuilder message = new StringBuilder("Nothing was changed.");
        if (accounts != null) {
            message.append(" The user accounts of this database and of the package do not go together, so what users made could come to belong to "
                    + "another person, or the update could not be completed: ").append(accounts).append(".");
        }
        if (namespaces != null) {
            message.append(" The update could not be completed, since a namespace can be in a library once: ").append(namespaces).append(".");
        }
        return message.append(" Please contact the developers and give them the job number ").append(job.value())
                .append(" (shown at the top of this window) and what is listed here.").toString();
    }

    // -- the undo ----------------------------------------------------------------------------------------------------
    public static final String ROLLING_BACK = "Putting the earlier data back. Saving is paused for a short while";
    public static final String ROLLED_BACK = "What users saved after the release package was applied was removed with it.";
    public static final String NOT_UNDONE = "The earlier data could not be put back. It is still kept; please contact your administrator.";
    public static final String LOAD_NOT_FOUND = "This job cannot be found.";
    public static final String ONLY_SUCCEEDED = "Only a job that finished successfully can be undone.";
    public static final String ONLY_ENDED = "Only a job that is over can be discarded.";
    public static final String KEEPS_DATA = "This job failed and still keeps data from before. Please contact your administrator; it is not discarded until the data has been looked at.";
    public static final String ONLY_LATEST = "Only the most recent job can be undone, because a later one has replaced what this one brought in.";
}
