package org.oagi.score.gateway.http.api.release_management.service.package_load;

/**
 * What keeps the data of the application from being written while the records of the users are copied and the schema is
 * swapped: reading goes on, writing is refused for everyone, and this is known to every server of the application. A change
 * that is written between the copy and the swap would not be in the schema that is swapped in.
 */
public interface ReleasePackageLoadWriteFreeze {

    /**
     * Starts the freeze, and returns when no write is going on any more.
     *
     * @throws ReleasePackageLoadRefusedException when it cannot be started
     */
    void freeze();

    /** Ends the freeze. Ending one that was not started does nothing. */
    void release();
}
