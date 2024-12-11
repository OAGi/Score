package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.LibraryObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.ReleaseObject;

import java.math.BigInteger;
import java.util.List;

/**
 * APIs for managing releases.
 */
public interface ReleaseAPI {

    /**
     * Retrieves a release by its unique ID.
     *
     * @param releaseId The unique identifier of the release.
     * @return The release object associated with the given ID.
     */
    ReleaseObject getReleaseById(BigInteger releaseId);

    /**
     * Retrieves a release by its release number within the specified library.
     *
     * @param library       The library associated with the release.
     * @param releaseNumber The release number.
     * @return The release object with the specified release number.
     */
    ReleaseObject getReleaseByReleaseNumber(LibraryObject library, String releaseNumber);

    /**
     * Retrieves all releases within the specified library that match the given states.
     *
     * @param library The library to search for releases.
     * @param states  A list of states to filter releases.
     * @return A list of release objects matching the given states.
     */
    List<ReleaseObject> getReleasesByStates(LibraryObject library, List<String> states);

    /**
     * Retrieves all releases associated with the specified library.
     *
     * @param library The library to retrieve releases from.
     * @return A list of all release objects in the library.
     */
    List<ReleaseObject> getReleases(LibraryObject library);

    /**
     * Retrieves the latest release within the specified library.
     *
     * @param library The library to retrieve the latest release from.
     * @return The latest release object in the library.
     */
    ReleaseObject getTheLatestRelease(LibraryObject library);

    /**
     * Retrieves all releases before the specified release in the given library.
     *
     * @param library The library containing the releases.
     * @param release The reference release object.
     * @return A list of release numbers for releases created before the specified release.
     */
    List<String> getAllReleasesBeforeRelease(LibraryObject library, ReleaseObject release);

    /**
     * Creates a random release object associated with a given library and namespace.
     *
     * @param creator   The user creating the release.
     * @param library   The library to associate with the release.
     * @param namespace The namespace to associate with the release.
     * @return A newly created random release object.
     */
    ReleaseObject createRandomRelease(AppUserObject creator, LibraryObject library, NamespaceObject namespace);
}
