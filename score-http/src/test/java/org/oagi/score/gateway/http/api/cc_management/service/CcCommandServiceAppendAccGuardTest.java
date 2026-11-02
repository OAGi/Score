package org.oagi.score.gateway.http.api.cc_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.ascc.AsccCreateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.bcc.BccCreateRequest;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.repository.AccCommandRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.AccQueryRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Appending an ASCC or BCC through the endpoint-facing methods needs a WIP ACC owned by the requester.
 */
class CcCommandServiceAppendAccGuardTest {

    private static final AccManifestId ACC_ID = new AccManifestId(BigInteger.ONE);
    private static final AsccpManifestId ASCCP_ID = new AsccpManifestId(BigInteger.TWO);
    private static final BccpManifestId BCCP_ID = new BccpManifestId(BigInteger.TEN);

    private final ScoreUser requester = mock(ScoreUser.class);
    private final AccSummaryRecord acc = mock(AccSummaryRecord.class);
    private CcCommandService service;
    private AccCommandRepository accCommand;

    @BeforeEach
    void setUp() {
        service = new CcCommandService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        AccQueryRepository accQuery = mock(AccQueryRepository.class);
        accCommand = mock(AccCommandRepository.class);
        when(repositoryFactory.accQueryRepository(any())).thenReturn(accQuery);
        when(repositoryFactory.accCommandRepository(any())).thenReturn(accCommand);
        when(accQuery.getAccSummary(ACC_ID)).thenReturn(acc);
        when(requester.userId()).thenReturn(new UserId(BigInteger.ONE));
        when(requester.isAdministrator()).thenReturn(false);
    }

    private void accOwnedBy(CcState state, long ownerId) {
        when(acc.state()).thenReturn(state);
        when(acc.owner()).thenReturn(new UserSummaryRecord(new UserId(BigInteger.valueOf(ownerId)), "o", "Owner", null));
    }

    private void assertNothingAppended() {
        verify(accCommand, never()).createAscc(any(), any(), any(), any(), anyBoolean());
        verify(accCommand, never()).createBcc(any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void rejectsAppendToAnAccOfAnotherUser() {
        accOwnedBy(CcState.WIP, 2);

        assertThrows(IllegalArgumentException.class,
                () -> service.createAscc(requester, AsccCreateRequest.builder(ACC_ID, ASCCP_ID).build()));
        assertThrows(IllegalArgumentException.class,
                () -> service.createBcc(requester, BccCreateRequest.builder(ACC_ID, BCCP_ID).build()));
        assertNothingAppended();
    }

    @Test
    void anEndUserCanCreateTheFirstUserExtensionOfAProductionAccOfADeveloper() {
        accOwnedBy(CcState.Production, 2);
        AccManifestId ueAcc = new AccManifestId(BigInteger.valueOf(30));
        AsccpManifestId ueAsccp = new AsccpManifestId(BigInteger.valueOf(31));
        when(requester.isDeveloper()).thenReturn(false);
        CcCommandService spy = spy(service);
        doReturn(ueAcc).when(spy).createAcc(any(), any());
        doReturn(ueAsccp).when(spy).createAsccp(any(), any());
        doReturn(null).when(spy).createAscc(any(), any(), any(), any(), anyBoolean());
        AccSummaryRecord eAcc = mock(AccSummaryRecord.class);
        when(eAcc.accManifestId()).thenReturn(ACC_ID);
        when(eAcc.objectClassTerm()).thenReturn("All Extension");

        spy.appendUserExtension(requester, eAcc, null, new ReleaseId(BigInteger.ONE));

        verify(spy).createAscc(eq(requester), argThat(r -> ACC_ID.equals(r.accManifestId())),
                any(), any(), eq(false));
    }

    @Test
    void rejectsAppendToAnAccThatIsNotWip() {
        accOwnedBy(CcState.Production, 1);

        assertThrows(IllegalArgumentException.class,
                () -> service.createAscc(requester, AsccCreateRequest.builder(ACC_ID, ASCCP_ID).build()));
        assertThrows(IllegalArgumentException.class,
                () -> service.createBcc(requester, BccCreateRequest.builder(ACC_ID, BCCP_ID).build()));
        assertNothingAppended();
    }
}
