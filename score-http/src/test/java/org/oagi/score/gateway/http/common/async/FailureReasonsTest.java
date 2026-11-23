package org.oagi.score.gateway.http.common.async;

import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FailureReasonsTest {

    @Test
    void tellsAnUnexpectedErrorForWhatItDoesNotKnow() {
        assertEquals(FailureReasons.UNEXPECTED, FailureReasons.of(new IllegalStateException("x")));
    }

    @Test
    void tellsOfAConflictOfLocksWhereverItIsInTheChain() {
        assertEquals(FailureReasons.CONCURRENT,
                FailureReasons.of(new RuntimeException("x", new SQLException("deadlock", "40001", 1213))));
        assertEquals(FailureReasons.CONCURRENT,
                FailureReasons.of(new RuntimeException("x", new SQLException("timeout", "HY000", 1205))));
        assertEquals(FailureReasons.CONCURRENT, FailureReasons.of(new CannotAcquireLockException("x")));
    }

    @Test
    void asksWhatIsOfTheWorkBeforeTheKindsThatAreTheSameForAll() {
        FailureReasons.Explainer explainer = t -> t instanceof IllegalArgumentException
                ? Optional.of("of the work") : Optional.empty();

        assertEquals("of the work", FailureReasons.of(
                new IllegalArgumentException("y", new CannotAcquireLockException("x")), t -> Optional.empty(), explainer));
    }

    @Test
    void stopsAtTheLimitWhenTheCausesPointBackToEachOther() {
        RuntimeException a = new RuntimeException("a");
        RuntimeException b = new RuntimeException("b", a);
        a.initCause(b);

        assertEquals(FailureReasons.UNEXPECTED, FailureReasons.of(a));
        assertTrue(FailureReasons.rootMessage(a).contains("RuntimeException"));
    }

    @Test
    void keepsTheStackTraceForTheRecord() {
        String details = FailureReasons.withStackTrace("what", new IllegalStateException("boom"));

        assertTrue(details.startsWith("what\n\n"));
        assertTrue(details.contains("IllegalStateException: boom"));
        assertEquals("what", FailureReasons.withStackTrace("what", null));
    }
}
