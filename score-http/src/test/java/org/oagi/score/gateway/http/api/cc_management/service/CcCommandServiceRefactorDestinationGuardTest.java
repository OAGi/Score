package org.oagi.score.gateway.http.api.cc_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.AccCommandRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.AccQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * Refactoring an association moves it into the destination ACC, so the destination must be a WIP ACC owned by the
 * requester. An administrator can change the state but not the structure of another user's component.
 */
class CcCommandServiceRefactorDestinationGuardTest {

    private static final AccManifestId DESTINATION_ID = new AccManifestId(BigInteger.ONE);
    private static final AsccManifestId ASCC_ID = new AsccManifestId(BigInteger.TWO);
    private static final BccManifestId BCC_ID = new BccManifestId(BigInteger.TEN);

    private final ScoreUser requester = mock(ScoreUser.class);
    private final AccSummaryRecord destination = mock(AccSummaryRecord.class);
    private CcCommandService service;
    private AccQueryRepository accQuery;
    private AccCommandRepository accCommand;

    @BeforeEach
    void setUp() {
        service = spy(new CcCommandService());
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        accQuery = mock(AccQueryRepository.class);
        accCommand = mock(AccCommandRepository.class);
        when(repositoryFactory.accQueryRepository(any())).thenReturn(accQuery);
        when(repositoryFactory.accCommandRepository(any())).thenReturn(accCommand);
        when(accQuery.getAccSummary(DESTINATION_ID)).thenReturn(destination);
        when(requester.userId()).thenReturn(new UserId(BigInteger.ONE));
        when(requester.isAdministrator()).thenReturn(false);
    }

    private void destinationOwnedBy(CcState state, long ownerId) {
        when(destination.state()).thenReturn(state);
        when(destination.owner()).thenReturn(
                new UserSummaryRecord(new UserId(BigInteger.valueOf(ownerId)), "o", "Owner", null));
    }

    private void assertNothingChanged() {
        verify(accQuery, never()).getRefactorTargetAsccManifestList(any(), any());
        verify(accQuery, never()).getRefactorTargetBccManifestList(any(), any());
        verify(service, never()).discard(any(), any(AsccManifestId.class), anyBoolean(), any(), any());
        verify(service, never()).discard(any(), any(BccManifestId.class), anyBoolean(), any(), any());
        verify(accCommand, never()).createAscc(any(), any(), any(), any(), anyBoolean());
        verify(accCommand, never()).createBcc(any(), any(), any(), any(), anyBoolean());
    }

    private void assertRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.refactorAscc(requester, ASCC_ID, DESTINATION_ID));
        assertThrows(IllegalArgumentException.class, () -> service.refactorBcc(requester, BCC_ID, DESTINATION_ID));
        assertNothingChanged();
    }

    @Test
    void rejectsADestinationOwnedByAnotherUser() {
        destinationOwnedBy(CcState.WIP, 2);

        assertRejected();
    }

    @Test
    void rejectsADestinationThatIsNotWip() {
        destinationOwnedBy(CcState.Production, 1);

        assertRejected();
    }

    @Test
    void rejectsAnAdministratorWhoDoesNotOwnTheDestination() {
        when(requester.isAdministrator()).thenReturn(true);
        destinationOwnedBy(CcState.WIP, 2);

        assertRejected();
    }

    @Test
    void rejectsAMissingDestination() {
        when(accQuery.getAccSummary(DESTINATION_ID)).thenReturn(null);

        assertRejected();
    }

    @Test
    void movesTheAssociationsIntoAWipDestinationOwnedByTheRequester() {
        destinationOwnedBy(CcState.WIP, 1);
        AsccSummaryRecord ascc = mock(AsccSummaryRecord.class);
        BccSummaryRecord bcc = mock(BccSummaryRecord.class);
        when(accQuery.getAsccSummary(ASCC_ID)).thenReturn(ascc);
        when(accQuery.getBccSummary(BCC_ID)).thenReturn(bcc);
        when(accQuery.getRefactorTargetAsccManifestList(ASCC_ID, DESTINATION_ID)).thenReturn(List.of());
        when(accQuery.getRefactorTargetBccManifestList(BCC_ID, DESTINATION_ID)).thenReturn(List.of());
        doReturn(null).when(service).createAscc(any(), any(), any(), any(), anyBoolean());
        doReturn(null).when(service).createBcc(any(), any(), any(), any(), anyBoolean());

        service.refactorAscc(requester, ASCC_ID, DESTINATION_ID);
        service.refactorBcc(requester, BCC_ID, DESTINATION_ID);

        verify(service).createAscc(any(), any(), any(), any(), anyBoolean());
        verify(service).createBcc(any(), any(), any(), any(), anyBoolean());
    }
}
