package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.release_management.repository.Namespace;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleasePackageLoadNamespaceCheckTest {

    private static Namespace namespace(long id, long library, String uri) {
        return new Namespace(BigInteger.valueOf(id), BigInteger.valueOf(library), uri);
    }

    private static final BigInteger PACKAGE_MAX = BigInteger.valueOf(3);

    @Test
    void aNamespaceOfAUserThatThePackageHasInTheSameLibraryIsTold() {
        List<String> found = ReleasePackageLoadNamespaceCheck.differences(
                List.of(namespace(100000050, 3, "http://X.example/a")), List.of(namespace(1, 3, "http://x.example/A")), PACKAGE_MAX);

        assertEquals(1, found.size());
        assertTrue(found.get(0).contains("'http://X.example/a'") && found.get(0).contains("library number 3"), found.get(0));
    }

    @Test
    void theSameUriInAnotherLibraryIsNotInTheWay() {
        assertTrue(ReleasePackageLoadNamespaceCheck.differences(
                List.of(namespace(100000050, 4, "http://x.example/a")), List.of(namespace(1, 3, "http://x.example/a")), PACKAGE_MAX).isEmpty());
    }

    @Test
    void aNamespaceOfThePackageRangeIsNotOfAUser() {
        assertTrue(ReleasePackageLoadNamespaceCheck.differences(
                List.of(namespace(1, 3, "http://x.example/a")), List.of(namespace(1, 3, "http://x.example/a")), PACKAGE_MAX).isEmpty());
    }
}
