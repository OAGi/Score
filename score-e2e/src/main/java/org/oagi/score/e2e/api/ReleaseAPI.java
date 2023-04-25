package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.ReleaseObject;

import java.math.BigInteger;

/**
 * APIs for the release management.
 */
public interface ReleaseAPI {

    /**
     * Return the release by the given ID.
     *
     * @param releaseId release ID
     * @return release object
     */
    ReleaseObject getReleaseById(BigInteger releaseId);

    /**
     * Return the release by the given release number.
     *
     * @param releaseNumber release number
     * @return release object
     */
    ReleaseObject getReleaseByReleaseNumber(String releaseNumber);

    ReleaseObject getTheLatestRelease();

}
