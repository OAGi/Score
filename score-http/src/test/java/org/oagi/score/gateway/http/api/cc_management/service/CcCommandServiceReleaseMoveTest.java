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
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Candidate -> ReleaseDraft and ReleaseDraft -> Candidate or Published belong to the release process: the state
 * endpoints leave them out, and the role of the requester decides who moves a component to Draft, Candidate,
 * QA or Production.
 */
class CcCommandServiceReleaseMoveTest {

    private static final AccManifestId ACC = new AccManifestId(BigInteger.ONE);

    private final ScoreUser otherDeveloper =
            new ScoreUser(new UserId(BigInteger.valueOf(9)), "dev2", "Dev 2", null, false, List.of(ScoreRole.DEVELOPER));
    private final ScoreUser endUser =
            new ScoreUser(new UserId(BigInteger.valueOf(7)), "eu", "End User", null, false, List.of(ScoreRole.END_USER));
    private final UserSummaryRecord owner =
            new UserSummaryRecord(new UserId(BigInteger.valueOf(8)), "dev", "Dev", List.of(ScoreRole.DEVELOPER));

    private AccQueryRepository accQuery;
    private AccCommandRepository accCommand;
    private AccSummaryRecord acc;
    private CcCommandService service;

    @BeforeEach
    void setUp() {
        service = spy(new CcCommandService());
        RepositoryFactory repositories = mock(RepositoryFactory.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositories);
        accQuery = mock(AccQueryRepository.class);
        accCommand = mock(AccCommandRepository.class);
        when(repositories.accQueryRepository(any())).thenReturn(accQuery);
        when(repositories.accCommandRepository(any())).thenReturn(accCommand);
        acc = mock(AccSummaryRecord.class);
        when(acc.owner()).thenReturn(owner);
        when(accQuery.getAccSummary(ACC)).thenReturn(acc);
    }

    @Test
    void aUserWhoDoesNotOwnTheComponentCannotMoveItInOrOutOfReleaseDraftThroughTheEndpoints() {
        when(acc.state()).thenReturn(CcState.Candidate);
        assertThrows(IllegalArgumentException.class, () -> service.updateState(otherDeveloper, ACC, CcState.ReleaseDraft));

        when(acc.state()).thenReturn(CcState.ReleaseDraft);
        assertThrows(IllegalArgumentException.class, () -> service.updateState(otherDeveloper, ACC, CcState.Published));
        assertThrows(IllegalArgumentException.class, () -> service.updateState(otherDeveloper, ACC, CcState.Candidate));
        verify(accCommand, never()).updateState(any(), any());
    }

    @Test
    void theOwnerCannotSkipTheReleaseProcessEither() {
        ScoreUser ownerUser =
                new ScoreUser(owner.userId(), "dev", "Dev", null, false, List.of(ScoreRole.DEVELOPER));
        when(acc.state()).thenReturn(CcState.ReleaseDraft);

        assertThrows(IllegalArgumentException.class, () -> service.updateState(ownerUser, ACC, CcState.Published));
        verify(accCommand, never()).updateState(any(), any());
    }

    @Test
    void theReleaseProcessMovesTheComponentOfAnotherDeveloper() {
        when(acc.state()).thenReturn(CcState.Candidate);
        when(acc.namespaceId()).thenReturn(new NamespaceId(BigInteger.ONE));
        when(accCommand.updateState(ACC, CcState.ReleaseDraft)).thenReturn(false);

        service.updateState(otherDeveloper, ACC, CcState.ReleaseDraft, null, null, true);

        verify(accCommand).updateState(ACC, CcState.ReleaseDraft);
    }

    @Test
    void anEndUserCannotMoveAComponentToDraftOrCandidateAndADeveloperCannotMoveItToQaOrProduction() {
        when(acc.state()).thenReturn(CcState.WIP);
        assertThrows(IllegalArgumentException.class, () -> service.updateState(endUser, ACC, CcState.Draft));

        ScoreUser ownerUser =
                new ScoreUser(owner.userId(), "dev", "Dev", null, false, List.of(ScoreRole.DEVELOPER));
        assertThrows(IllegalArgumentException.class, () -> service.updateState(ownerUser, ACC, CcState.QA));
        verify(accCommand, never()).updateState(any(), any());
    }
}
