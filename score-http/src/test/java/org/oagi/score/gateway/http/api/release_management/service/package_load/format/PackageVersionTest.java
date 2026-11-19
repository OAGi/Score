package org.oagi.score.gateway.http.api.release_management.service.package_load.format;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PackageVersionTest {

    @Test
    void theFirstTwoNumbersCountAndAThirdIsLeftOut() {
        assertEquals(PackageVersion.of(1, 1), PackageVersion.parse("1.1"));
        assertEquals(PackageVersion.of(1, 2), PackageVersion.parse("1.2.3"));
        assertEquals(PackageVersion.of(10, 0), PackageVersion.parse("10.0"));
        assertEquals("1.2", PackageVersion.parse("1.2.9").toString());
    }

    @Test
    void versionsAreOrderedByNumbersNotByLetters() {
        assertTrue(PackageVersion.parse("1.10").compareTo(PackageVersion.parse("1.9")) > 0);
        assertTrue(PackageVersion.parse("2.0").compareTo(PackageVersion.parse("1.99")) > 0);
        assertEquals(0, PackageVersion.parse("1.2").compareTo(PackageVersion.parse("1.2.7")));
    }

    @Test
    void whatIsNotAVersionIsRefused() {
        for (String text : new String[]{"1", "a.b", "1.", ".1", "1.2.3.4", "", " 1.1", null}) {
            assertThrows(IllegalArgumentException.class, () -> PackageVersion.parse(text), String.valueOf(text));
        }
    }
}
