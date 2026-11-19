package org.oagi.score.gateway.http.api.release_management.service.migrationload;

/**
 * What is used when the application has no write freeze: nothing can be frozen, so a load that needs it is refused before
 * it changes anything.
 */
public final class UnavailableWriteFreeze implements WriteFreeze {

    @Override
    public void freeze() {
        throw new MigrationLoadRefusedException("The application has no way to stop writing while a release package is loaded, "
                + "so a package is not loaded.");
    }

    @Override
    public void release() {
        // Nothing was frozen.
    }
}
