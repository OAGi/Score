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
     * Return releases by the given states.
     *
     * @param states states
     * @return release objects
     */
    List<ReleaseObject> getReleasesByStates(List<String> states);

    /**
     * Return the releases.
     *
     * @return release objects.
     */
    List<ReleaseObject> getReleases();

    ReleaseObject getTheLatestRelease();

    List<String> getAllReleasesBeforeRelease(ReleaseObject releaseNumber);

    /**
     * Create a random release object.
     *
     * @param creator a creator
     * @param namespace a namespace
     * @return a random release object
     */
    ReleaseObject createRandomRelease(AppUserObject creator, NamespaceObject namespace);

}
