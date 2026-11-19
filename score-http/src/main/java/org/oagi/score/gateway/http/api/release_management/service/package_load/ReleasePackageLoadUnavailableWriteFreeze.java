package org.oagi.score.gateway.http.api.release_management.service.package_load;

/**
 * What is used when the application has no write freeze: nothing can be frozen, so a load that needs it is refused before
 * it changes anything.
 */
public final class ReleasePackageLoadUnavailableWriteFreeze implements ReleasePackageLoadWriteFreeze {

    @Override
    public void freeze() {
        throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.NO_WAY_TO_STOP_WRITING);
    }

    @Override
    public void release() {
        // Nothing was frozen.
    }
}
