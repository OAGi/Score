package org.oagi.score.gateway.http.api.cc_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.common.model.ScoreUser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * A bulk purge purges components in an order that works, and reports what it could not purge.
 */
class CcCommandServiceBulkPurgeTest {

    private static final AccManifestId ACC = new AccManifestId(BigInteger.ONE);
    private static final AsccpManifestId ASCCP = new AsccpManifestId(BigInteger.TWO);

    private final ScoreUser requester = mock(ScoreUser.class);
    private final CcCommandService service = spy(new CcCommandService());
    private final List<String> purged = new ArrayList<>();

    @Test
    void anAccIsPurgedTogetherWithTheAsccpThatUsesIt() {
        doAnswer(invocation -> {
            purged.add("ASCCP");
            return true;
        }).when(service).purge(requester, ASCCP);
        doAnswer(invocation -> {
            if (!purged.contains("ASCCP")) {
                throw new IllegalArgumentException("Please purge related-ASCCPs first before purging the ACC");
            }
            purged.add("ACC");
            return true;
        }).when(service).purge(requester, ACC);

        // the ACC is listed first, as in the request
        service.purge(requester, List.of(ACC), List.of(ASCCP), List.of(), List.of());

        assertEquals(List.of("ASCCP", "ACC"), purged);
    }

    @Test
    void aFailureIsReportedWithItsReason() {
        doAnswer(invocation -> {
            throw new IllegalArgumentException("Please purge derivations first before purging the ACC 'Acc. Details'.");
        }).when(service).purge(any(ScoreUser.class), any(AccManifestId.class));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.purge(requester, List.of(ACC), List.of(), List.of(), List.of()));

        assertTrue(e.getMessage().contains("Please purge derivations first"), e.getMessage());
        assertTrue(e.getMessage().contains("ACC"), e.getMessage());
    }
}
