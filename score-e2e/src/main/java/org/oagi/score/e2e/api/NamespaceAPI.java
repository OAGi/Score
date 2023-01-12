package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.NamespaceObject;

/**
 * APIs for the namespace management.
 */
public interface NamespaceAPI {

    /**
     * Return the namespace by the given URI.
     *
     * @param uri URI
     * @return namespace object
     */
    NamespaceObject getNamespaceByURI(String uri);

    /**
     * Create a random end-user namespace.
     *
     * @param creator account who creates this namespace
     * @return a created namespace object
     */
    NamespaceObject createRandomEndUserNamespace(AppUserObject creator);

    NamespaceObject createRandomDeveloperNamespace(AppUserObject creator);

}
