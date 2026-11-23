package org.oagi.score.gateway.http.api.release_management.guard;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.EXCEPTION;

/**
 * When the work for a release fails, or the release is taken back because no one was doing it, the release is given back,
 * what went wrong is kept in the {@code exception} table, once, and the user who asked for it is sent a message that refers
 * to the record.
 */
@EnabledIfSystemProperty(named = "score.releaseGuard", matches = "true")
class ReleaseFailureReportDbTest {

    static GuardHarness harness;

    @BeforeAll
    static void open() {
        harness = GuardHarness.open();
    }

    @AfterAll
    static void close() {
        harness.close();
    }

    private static String tagOf(String kind, ReleaseId release) {
        return kind + "#" + release.value();
    }

    /** The details as an administrator reads them: with the UNCOMPRESS() of the database itself. */
    private static String uncompressed(org.jooq.types.ULong exceptionId) {
        return harness.dsl.resultQuery("select cast(uncompress(stacktrace) as char) from `exception` where exception_id = ?",
                exceptionId).fetchOne(0, String.class);
    }

    @Test
    void aVeryLongMessageIsCutWhereItIsListedAndWholeInTheDetails() {
        String message = "The statement failed: " + "update `bcc` set `state` = 'ReleaseDraft' where id in (1, 2, 3) ".repeat(5_000)
                + "\uD83D\uDE00 the end";
        var id = harness.factory.exceptionCommandRepository(GuardHarness.REQUESTER)
                .create("release.test#1", message, message + "\n\nat x.y.Z", GuardHarness.REQUESTER.userId());
        try {
            var row = harness.dsl.selectFrom(EXCEPTION).where(EXCEPTION.EXCEPTION_ID.eq(org.jooq.types.ULong.valueOf(id.value()))).fetchOne();
            assertTrue(row.getMessage().length() < 1_100, "the message is cut: " + row.getMessage().length());
            assertEquals(message + "\n\nat x.y.Z", uncompressed(row.getExceptionId()));
        } finally {
            harness.dsl.deleteFrom(EXCEPTION).where(EXCEPTION.TAG.eq("release.test#1")).execute();
        }
    }

    private static void forget(ReleaseId release) {
        harness.dsl.deleteFrom(EXCEPTION).where(EXCEPTION.TAG.like("release.%#" + release.value())).execute();
    }

    @Test
    void aFailureOfTheHandlerIsKeptOnceAndToldToTheUserWhoAskedForTheRelease() {
        Arena arena = harness.arrange();
        ReleaseId release = arena.release();
        try {
            Mockito.clearInvocations(harness.messages);
            assertEquals(null, arena.requestDraft());
            FaultInjector.Probe probe = new FaultInjector.Probe().failAfterWrite(3);
            FaultInjector.attach(probe);
            try {
                arena.handleEvents();
            } finally {
                FaultInjector.detach();
            }

            assertEquals("Initialized", arena.observe().releaseState());
            var rows = harness.dsl.selectFrom(EXCEPTION).where(EXCEPTION.TAG.eq(tagOf("release.drafting", release))).fetch();
            assertEquals(1, rows.size());
            assertTrue(rows.get(0).getMessage().contains("could not be made a draft"), rows.get(0).getMessage());
            // all there is to know is in the compressed details, which an administrator reads in SQL
            String details = uncompressed(rows.get(0).getExceptionId());
            assertTrue(details.contains("could not be made a draft"), details);
            assertTrue(details.contains("\tat "), "the stack trace: " + details);
            assertEquals(GuardHarness.REQUESTER.userId().value().longValue(), rows.get(0).getCreatedBy().longValue());

            ArgumentCaptor<Collection<UserId>> recipients = ArgumentCaptor.forClass(Collection.class);
            ArgumentCaptor<String> subject = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
            Mockito.verify(harness.messages, Mockito.times(1)).asyncSendMessage(Mockito.any(), recipients.capture(),
                    subject.capture(), body.capture(), Mockito.eq("text/markdown"));
            assertEquals(List.of(GuardHarness.REQUESTER.userId()), List.copyOf(recipients.getValue()));
            assertTrue(subject.getValue().contains("could not be made a draft"), subject.getValue());
            assertTrue(!body.getValue().contains("Reference"), "a number the user cannot look up is not told: " + body.getValue());
            assertTrue(!body.getValue().contains("Exception"), "the user is not told the detail: " + body.getValue());
        } finally {
            forget(release);
            arena.remove();
        }
    }

    @Test
    void aFailureWhileADraftGoesBackToInitializedIsKeptAndTheDraftIsADraftAgain() {
        Arena arena = harness.arrange().toDraft();
        ReleaseId release = arena.release();
        try {
            Mockito.clearInvocations(harness.messages);
            GuardHarness.Observed before = arena.observe();
            // the request takes hold of the release, and the work is for the handler: nothing is done yet
            assertEquals(null, arena.requestInitialize());
            assertEquals("Initializing", arena.observe().releaseState());
            FaultInjector.Probe probe = new FaultInjector.Probe().failAfterWrite(3);
            FaultInjector.attach(probe);
            try {
                arena.handleEvents();
            } finally {
                FaultInjector.detach();
            }

            assertEquals(before, arena.observe());
            var rows = harness.dsl.selectFrom(EXCEPTION).where(EXCEPTION.TAG.eq(tagOf("release.initializing", release))).fetch();
            assertEquals(1, rows.size());
            ArgumentCaptor<String> subject = ArgumentCaptor.forClass(String.class);
            Mockito.verify(harness.messages, Mockito.times(1)).asyncSendMessage(Mockito.any(), Mockito.any(),
                    subject.capture(), Mockito.contains("Draft"), Mockito.eq("text/markdown"));
            assertTrue(subject.getValue().contains("taken back to Initialized"), subject.getValue());
        } finally {
            forget(release);
            arena.remove();
        }
    }

    @Test
    void aFailureThatRollsBackTheGivingBackIsKeptNowhereAndTellsNoOne() {
        // Nothing is kept or told when the release is no longer in the state it was given back from.
        Arena arena = harness.arrange();
        ReleaseId release = arena.release();
        try {
            Mockito.clearInvocations(harness.messages);
            assertEquals(null, arena.requestDraft());
            // someone else gave the release back already
            assertTrue(harness.progress.moveState(release, ReleaseState.Drafting, ReleaseState.Initialized));
            arena.handleEvents();

            assertEquals(0, harness.dsl.fetchCount(EXCEPTION, EXCEPTION.TAG.like("release.%#" + release.value())));
            Mockito.verify(harness.messages, Mockito.never()).asyncSendMessage(Mockito.any(), Mockito.any(),
                    Mockito.any(), Mockito.any(), Mockito.any());
        } finally {
            forget(release);
            arena.remove();
        }
    }

    @Test
    void aReleaseTakenBackIsKeptAsRecoveredAndToldToTheUserWhoLastUpdatedIt() {
        Arena arena = harness.arrange();
        ReleaseId release = arena.release();
        try {
            Mockito.clearInvocations(harness.messages);
            assertTrue(harness.progress.takeHold(release, ReleaseState.Initialized, ReleaseState.Drafting,
                    GuardHarness.REQUESTER.userId()));
            assertEquals(1, harness.recovery.recoverSince(LocalDateTime.now().plusHours(1)));

            assertEquals("Initialized", arena.observe().releaseState());
            var rows = harness.dsl.selectFrom(EXCEPTION).where(EXCEPTION.TAG.eq(tagOf("release.recovery", release))).fetch();
            assertEquals(1, rows.size());
            assertTrue(uncompressed(rows.get(0).getExceptionId()).contains("no one was doing it"));
            ArgumentCaptor<Collection<UserId>> recipients = ArgumentCaptor.forClass(Collection.class);
            Mockito.verify(harness.messages, Mockito.times(1)).asyncSendMessage(Mockito.any(), recipients.capture(),
                    Mockito.any(), Mockito.contains("no one was working on it"), Mockito.eq("text/markdown"));
            assertEquals(List.of(GuardHarness.REQUESTER.userId()), List.copyOf(recipients.getValue()));
        } finally {
            forget(release);
            arena.remove();
        }
    }
}
