package org.oagi.score.gateway.http.api.release_management.repository;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.repository.jooq.JooqReleaseProgressRepository;
import org.springframework.dao.CannotAcquireLockException;

import java.math.BigInteger;
import java.sql.SQLException;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.LIBRARY;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A library whose release rows are held by the work of a release is busy; it is not an error. The application translates the failure
 * of a statement into the exceptions of Spring as it happens, so the lock conflict arrives as one of those.
 */
class JooqReleaseProgressRepositoryLockTest {

    private static final LibraryId LIBRARY_ID = new LibraryId(BigInteger.ONE);

    private static JooqReleaseProgressRepository failingWith(RuntimeException failure) {
        DSLContext dsl = Mockito.mock(DSLContext.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(dsl.select(LIBRARY.LIBRARY_ID).from(LIBRARY).where(Mockito.any(Condition.class)).forShare().noWait().fetch())
                .thenThrow(failure);
        return new JooqReleaseProgressRepository(dsl, null, null);
    }

    @Test
    void aLockConflictAsTheExceptionOfSpringMakesTheLibraryBusy() {
        var failure = new CannotAcquireLockException("Lock wait timeout exceeded", new SQLException("Lock wait timeout", "HY000", 1205));
        assertTrue(failingWith(failure).libraryIsBusy(LIBRARY_ID));
    }

    @Test
    void aFailureThatIsNotALockConflictIsNotHidden() {
        assertThrows(IllegalStateException.class, () -> failingWith(new IllegalStateException("boom")).libraryIsBusy(LIBRARY_ID));
    }
}
