package org.oagi.score.gateway.http.api.release_management.service.migrationload;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadJobId;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class MigrationLoadNamesTest {

    private static final MigrationLoadJobId JOB = new MigrationLoadJobId(BigInteger.valueOf(42));

    @Test
    void everySchemaOfALoadStartsWithTheNameOfTheServedOneSoThatOnePatternCoversThem() {
        for (String name : new String[]{MigrationLoadNames.staging("oagi", JOB), MigrationLoadNames.backup("oagi", JOB),
                MigrationLoadNames.discarded("oagi", JOB), MigrationLoadNames.probe("oagi", JOB)}) {
            assertTrue(name.startsWith("oagi"), name);
            assertTrue(name.endsWith("42"), name);
        }
        assertEquals("oagi_load_42", MigrationLoadNames.staging("oagi", JOB));
        assertEquals("oagi_backup_42", MigrationLoadNames.backup("oagi", JOB));
        assertEquals("oagi_discarded_42", MigrationLoadNames.discarded("oagi", JOB));
    }

    @Test
    void theFourNamesOfALoadAreDifferentAndAreOfAnotherLoadWhenTheLoadIsAnother() {
        MigrationLoadJobId other = new MigrationLoadJobId(BigInteger.valueOf(43));

        assertEquals(4, java.util.stream.Stream.of(MigrationLoadNames.staging("oagi", JOB), MigrationLoadNames.backup("oagi", JOB),
                MigrationLoadNames.discarded("oagi", JOB), MigrationLoadNames.probe("oagi", JOB)).distinct().count());
        assertNotEquals(MigrationLoadNames.staging("oagi", JOB), MigrationLoadNames.staging("oagi", other));
    }

    @Test
    void aNameOfTheServedSchemaThatNeedsQuotingIsRefused() {
        assertThrows(MigrationLoadRefusedException.class, () -> MigrationLoadNames.staging("oagi-prod", JOB));
        assertThrows(MigrationLoadRefusedException.class, () -> MigrationLoadNames.staging("oagi`; DROP", JOB));
        assertThrows(MigrationLoadRefusedException.class, () -> MigrationLoadNames.staging(null, JOB));
    }

    @Test
    void aNameLongerThanTheServerAllowsIsRefused() {
        assertThrows(MigrationLoadRefusedException.class, () -> MigrationLoadNames.discarded("a".repeat(60), JOB));
        assertDoesNotThrow(() -> MigrationLoadNames.staging("a".repeat(50), JOB));
    }
}
