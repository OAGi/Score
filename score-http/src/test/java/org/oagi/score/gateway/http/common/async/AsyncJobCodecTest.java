package org.oagi.score.gateway.http.common.async;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.event.ReleaseCleanupEvent;
import org.oagi.score.gateway.http.api.release_management.model.event.ReleaseCreateRequestEvent;
import org.oagi.score.gateway.http.api.release_management.model.event.ReleaseInitializeEvent;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AsyncJobCodecTest {

    private final AsyncJobCodec codec = new AsyncJobCodec();

    private static BigInteger n(long v) {
        return BigInteger.valueOf(v);
    }

    @Test
    void writesPlainJsonWithIdsAsBareNumbersAndNoNameOfAClass() {
        String json = codec.encode(new ReleaseCleanupEvent(new UserId(n(7)), new ReleaseId(n(12))));

        assertEquals("{\"userId\":7,\"releaseId\":12}", json);
        assertFalse(json.contains("@class"));
    }

    @Test
    void readsBackEveryEventOfAReleaseAsItWas() {
        ReleaseCleanupEvent cleanup = new ReleaseCleanupEvent(new UserId(n(7)), new ReleaseId(n(12)));
        ReleaseInitializeEvent initialize = new ReleaseInitializeEvent(new UserId(n(7)), new ReleaseId(n(12)));
        ReleaseCreateRequestEvent create = new ReleaseCreateRequestEvent(new UserId(n(7)), new ReleaseId(n(12)),
                List.of(new AccManifestId(n(1)), new AccManifestId(n(2))),
                List.of(new AsccpManifestId(n(3))),
                List.of(new BccpManifestId(n(4))),
                List.of(new DtManifestId(n(5))),
                List.of(new CodeListManifestId(n(6))),
                List.of(new AgencyIdListManifestId(n(8))));

        assertEquals(cleanup, codec.decode(codec.encode(cleanup), ReleaseCleanupEvent.class));
        assertEquals(initialize, codec.decode(codec.encode(initialize), ReleaseInitializeEvent.class));
        assertEquals(create, codec.decode(codec.encode(create), ReleaseCreateRequestEvent.class));
    }

    @Test
    void letsGoOfAPropertyItDoesNotKnowSoThatANewerInstanceCanSendToAnOlderOne() {
        ReleaseCleanupEvent event = codec.decode("{\"userId\":7,\"releaseId\":12,\"somethingNew\":true}",
                ReleaseCleanupEvent.class);

        assertEquals(new ReleaseCleanupEvent(new UserId(n(7)), new ReleaseId(n(12))), event);
    }
}
