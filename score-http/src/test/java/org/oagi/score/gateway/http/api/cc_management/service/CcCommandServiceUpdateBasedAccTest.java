package org.oagi.score.gateway.http.api.cc_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.AccCommandRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.AccQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Setting the base ACC must not close a circle in the base chain.
 */
class CcCommandServiceUpdateBasedAccTest {

    private static final AccManifestId A = new AccManifestId(BigInteger.ONE);
    private static final AccManifestId B = new AccManifestId(BigInteger.TWO);
    private static final AccManifestId C = new AccManifestId(BigInteger.TEN);

    private final ScoreUser requester = mock(ScoreUser.class);
    private CcCommandService service;
    private AccQueryRepository accQuery;
    private AccCommandRepository accCommand;

    @BeforeEach
    void setUp() {
        service = new CcCommandService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        accQuery = mock(AccQueryRepository.class);
        accCommand = mock(AccCommandRepository.class);
        when(repositoryFactory.accQueryRepository(any())).thenReturn(accQuery);
        when(repositoryFactory.accCommandRepository(any())).thenReturn(accCommand);
        when(requester.userId()).thenReturn(new UserId(BigInteger.ONE));
        when(requester.isAdministrator()).thenReturn(false);
    }

    private AccSummaryRecord acc(AccManifestId id, AccManifestId base) {
        AccSummaryRecord acc = mock(AccSummaryRecord.class);
        when(acc.accManifestId()).thenReturn(id);
        when(acc.basedAccManifestId()).thenReturn(base);
        when(acc.state()).thenReturn(CcState.WIP);
        when(acc.den()).thenReturn("Acc " + id + ". Details");
        when(acc.owner()).thenReturn(new UserSummaryRecord(new UserId(BigInteger.ONE), "o", "Owner", null));
        when(accQuery.getAccSummary(id)).thenReturn(acc);
        return acc;
    }

    @Test
    void rejectsTheAccItselfAsItsBase() {
        acc(A, null);

        assertThrows(IllegalArgumentException.class, () -> service.updateBasedAccManifestId(requester, A, A));
        verify(accCommand, never()).updateBasedAccManifestId(any(), any());
    }

    @Test
    void rejectsADirectDescendantAsTheBase() {
        acc(A, null);
        acc(B, A);

        assertThrows(IllegalArgumentException.class, () -> service.updateBasedAccManifestId(requester, A, B));
        verify(accCommand, never()).updateBasedAccManifestId(any(), any());
    }

    @Test
    void rejectsAnIndirectDescendantAsTheBase() {
        acc(A, null);
        acc(B, A);
        acc(C, B);

        assertThrows(IllegalArgumentException.class, () -> service.updateBasedAccManifestId(requester, A, C));
        verify(accCommand, never()).updateBasedAccManifestId(any(), any());
    }
}
