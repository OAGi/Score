package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.oagi.score.gateway.http.api.release_management.repository.Namespace;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A library has a namespace of a URI once. The namespaces that users made in this database are copied beside those of the package,
 * so one of them that has the URI of a namespace of the package, in the same library, could not be copied.
 */
final class ReleasePackageLoadNamespaceCheck {

    private ReleasePackageLoadNamespaceCheck() {
    }

    /**
     * @param served     the namespaces of this database
     * @param packaged   the namespaces of the loaded package
     * @param packageMax the highest number the package gives: what is above it in this database was made by users
     * @return what is told of each namespace of a user that the package has too; empty when none is
     */
    static List<String> differences(List<Namespace> served, List<Namespace> packaged, BigInteger packageMax) {
        Set<String> inPackage = packaged.stream().map(ReleasePackageLoadNamespaceCheck::key).collect(Collectors.toSet());
        return served.stream().filter(namespace -> namespace.id().compareTo(packageMax) > 0 && inPackage.contains(key(namespace)))
                .map(namespace -> "the namespace '" + namespace.uri() + "' that a user made in the library number " + namespace.libraryId()
                        + " is a namespace of the package too").toList();
    }

    private static String key(Namespace namespace) {
        return namespace.libraryId() + " " + namespace.uri().toLowerCase();
    }
}
