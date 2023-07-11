package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.ReleaseObject;

import java.math.BigInteger;
import java.util.List;

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

    /**
     * Return the releases.
     *
     * @return release objects.
     */
    List<ReleaseObject> getReleases();

    ReleaseObject getTheLatestRelease();

    List<String> getAllReleasesBeforeRelease(ReleaseObject releaseNumber);

    ReleaseObject createDraftRelease(AppUserObject creator, NamespaceObject namespace);
}
