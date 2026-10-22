package org.oagi.score.gateway.http.api.cc_management.model;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CacheFirstCcDocumentTest {

    private final CcDocument cached = mock(CcDocument.class);
    private final CcDocument loaded = mock(CcDocument.class);
    private final AtomicInteger loads = new AtomicInteger();
    private final CacheFirstCcDocument document = new CacheFirstCcDocument(cached, () -> {
        loads.incrementAndGet();
        return loaded;
    });

    private static AccManifestId accId(long value) {
        return new AccManifestId(BigInteger.valueOf(value));
    }

    @Test
    void lookupsAreAnsweredByTheCachedDocumentAndTheReleasesAreNotLoaded() {
        AccSummaryRecord acc = mock(AccSummaryRecord.class);
        when(cached.getAcc(accId(1))).thenReturn(acc);

        assertSame(acc, document.getAcc(accId(1)));

        assertEquals(0, loads.get());
        verifyNoInteractions(loaded);
    }

    @Test
    void aLookupThatFailsIsAnsweredByTheLoadedDocumentAndSoIsEverythingAfterIt() {
        AccSummaryRecord fromLoaded = mock(AccSummaryRecord.class);
        AccSummaryRecord next = mock(AccSummaryRecord.class);
        when(cached.getAcc(accId(1))).thenThrow(new IllegalStateException("the database is down"));
        when(loaded.getAcc(accId(1))).thenReturn(fromLoaded);
        when(loaded.getAcc(accId(2))).thenReturn(next);

        assertSame(fromLoaded, document.getAcc(accId(1)));
        assertSame(next, document.getAcc(accId(2)));

        assertEquals(1, loads.get());
        verify(cached, never()).getAcc(accId(2));
    }

    @Test
    void whenTheLoadedDocumentCannotAnswerEitherItsErrorIsTheErrorOfTheCall() {
        when(cached.getAcc(accId(1))).thenThrow(new IllegalStateException("cached"));
        when(loaded.getAcc(accId(1))).thenThrow(new IllegalArgumentException("loaded"));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> document.getAcc(accId(1)));
        assertEquals("loaded", thrown.getMessage());
    }

    @Test
    void aScanOfTheReleaseIsAnsweredByTheLoadedDocumentAndSoIsEverythingAfterIt() {
        var blob = mock(org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentSummaryRecord.class);
        var blobId = new org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentManifestId(BigInteger.TEN);
        AsccpSummaryRecord asccp = mock(AsccpSummaryRecord.class);
        when(loaded.getBlobContent(blobId)).thenReturn(blob);
        when(loaded.getAsccp(new AsccpManifestId(BigInteger.ONE))).thenReturn(asccp);

        assertSame(blob, document.getBlobContent(blobId));
        assertSame(asccp, document.getAsccp(new AsccpManifestId(BigInteger.ONE)));

        assertEquals(1, loads.get());
        verify(cached, never()).getAsccp(any());
    }

    @Test
    void theReleasesAreLoadedOnceWhateverTheNumberOfThreads() throws Exception {
        when(loaded.getAsccpList()).thenReturn(List.of());
        Thread[] threads = new Thread[8];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                document.getAsccpList();
                document.getAcc(accId(1));
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        assertEquals(1, loads.get());
    }

    @Test
    void aHintThatFailsIsIgnoredAndDoesNotMakeTheDocumentFallBack() {
        doThrow(new IllegalStateException("boom")).when(cached).prefetchDependencies(CcType.ACC, accId(1));
        AccSummaryRecord acc = mock(AccSummaryRecord.class);
        when(cached.getAcc(accId(1))).thenReturn(acc);

        assertDoesNotThrow(() -> document.prefetchDependencies(CcType.ACC, accId(1)));
        assertSame(acc, document.getAcc(accId(1)));
        assertEquals(0, loads.get());
    }

    @Test
    void hintsAreForwardedWhileTheCachedDocumentAnswers() {
        document.prefetchChildren(CcType.ACC, List.of(accId(1)));
        verify(cached).prefetchChildren(CcType.ACC, List.of(accId(1)));

        document.getAsccpList();
        document.prefetchChildren(CcType.ACC, List.of(accId(2)));
        verify(cached, never()).prefetchChildren(CcType.ACC, List.of(accId(2)));
    }
}
