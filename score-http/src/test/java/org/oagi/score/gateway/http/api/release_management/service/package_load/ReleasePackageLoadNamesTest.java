package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJobId;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class ReleasePackageLoadNamesTest {

    private static final ReleasePackageLoadJobId JOB = new ReleasePackageLoadJobId(BigInteger.valueOf(42));

    @Test
    void everySchemaOfALoadStartsWithTheNameOfTheServedOneSoThatOnePatternCoversThem() {
        for (String name : new String[]{ReleasePackageLoadNames.staging("oagi", JOB), ReleasePackageLoadNames.backup("oagi", JOB),
                ReleasePackageLoadNames.discarded("oagi", JOB), ReleasePackageLoadNames.probe("oagi", JOB)}) {
            assertTrue(name.startsWith("oagi"), name);
            assertTrue(name.endsWith("42"), name);
        }
        assertEquals("oagi_load_42", ReleasePackageLoadNames.staging("oagi", JOB));
        assertEquals("oagi_backup_42", ReleasePackageLoadNames.backup("oagi", JOB));
        assertEquals("oagi_discarded_42", ReleasePackageLoadNames.discarded("oagi", JOB));
    }

    @Test
    void theFourNamesOfALoadAreDifferentAndAreOfAnotherLoadWhenTheLoadIsAnother() {
        ReleasePackageLoadJobId other = new ReleasePackageLoadJobId(BigInteger.valueOf(43));

        assertEquals(4, java.util.stream.Stream.of(ReleasePackageLoadNames.staging("oagi", JOB), ReleasePackageLoadNames.backup("oagi", JOB),
                ReleasePackageLoadNames.discarded("oagi", JOB), ReleasePackageLoadNames.probe("oagi", JOB)).distinct().count());
        assertNotEquals(ReleasePackageLoadNames.staging("oagi", JOB), ReleasePackageLoadNames.staging("oagi", other));
    }

    @Test
    void aNameOfTheServedSchemaThatNeedsQuotingIsRefused() {
        assertThrows(ReleasePackageLoadRefusedException.class, () -> ReleasePackageLoadNames.staging("oagi-prod", JOB));
        assertThrows(ReleasePackageLoadRefusedException.class, () -> ReleasePackageLoadNames.staging("oagi`; DROP", JOB));
        assertThrows(ReleasePackageLoadRefusedException.class, () -> ReleasePackageLoadNames.staging(null, JOB));
    }

    @Test
    void aNameLongerThanTheServerAllowsIsRefused() {
        assertThrows(ReleasePackageLoadRefusedException.class, () -> ReleasePackageLoadNames.discarded("a".repeat(60), JOB));
        assertDoesNotThrow(() -> ReleasePackageLoadNames.staging("a".repeat(50), JOB));
    }
}
