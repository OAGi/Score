package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReleasePackageLoadUnavailableWriteFreezeTest {

    @Test
    void aLoadThatNeedsToStopWritingIsRefusedWhenNothingCan() {
        ReleasePackageLoadRefusedException e = assertThrows(ReleasePackageLoadRefusedException.class, () -> new ReleasePackageLoadUnavailableWriteFreeze().freeze());

        assertTrue(e.getMessage().contains("pause saving"), e.getMessage());
    }

    @Test
    void endingAFreezeThatWasNotStartedDoesNothing() {
        assertDoesNotThrow(() -> new ReleasePackageLoadUnavailableWriteFreeze().release());
    }
}
