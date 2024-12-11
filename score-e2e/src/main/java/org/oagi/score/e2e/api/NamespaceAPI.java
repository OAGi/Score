package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.LibraryObject;
import org.oagi.score.e2e.obj.NamespaceObject;

import java.math.BigInteger;
import java.util.List;

/**
 * APIs for the namespace management.
 */
public interface NamespaceAPI {

    /**
     * Return the namespace by the given URI.
     *
     * @param library library
     * @param uri     URI
     * @return namespace object
     */
    NamespaceObject getNamespaceByURI(LibraryObject library, String uri);

    /**
     * Create a random end-user namespace.
     *
     * @param creator account who creates this namespace
     * @param library library
     * @return a created namespace object
     */
    NamespaceObject createRandomEndUserNamespace(AppUserObject creator, LibraryObject library);

    NamespaceObject createRandomDeveloperNamespace(AppUserObject creator, LibraryObject library);

    List<NamespaceObject> getStandardNamespacesURIs(LibraryObject library);

    NamespaceObject getNamespaceById(BigInteger namespaceId);

    List<NamespaceObject> getNonStandardNamespacesURIs(LibraryObject library);

}
