package org.oagi.score.gateway.http.api.release_management.service.migrationload;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnavailableWriteFreezeTest {

    @Test
    void aLoadThatNeedsToStopWritingIsRefusedWhenNothingCan() {
        MigrationLoadRefusedException e = assertThrows(MigrationLoadRefusedException.class, () -> new UnavailableWriteFreeze().freeze());

        assertTrue(e.getMessage().contains("stop writing"), e.getMessage());
    }

    @Test
    void endingAFreezeThatWasNotStartedDoesNothing() {
        assertDoesNotThrow(() -> new UnavailableWriteFreeze().release());
    }
}
