package org.oagi.score.gateway.http.api.cc_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.*;
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
 * A Deleted core component is purged or restored only by a user in the same role as its owner.
 */
class CcCommandServicePurgeRestoreRoleTest {

    private static final AccManifestId ACC = new AccManifestId(BigInteger.ONE);
    private static final AsccpManifestId ASCCP = new AsccpManifestId(BigInteger.TWO);
    private static final BccpManifestId BCCP = new BccpManifestId(BigInteger.TEN);
    private static final DtManifestId DT = new DtManifestId(BigInteger.valueOf(11));

    private final ScoreUser endUser =
            new ScoreUser(new UserId(BigInteger.valueOf(7)), "eu", "End User", null, false, List.of(ScoreRole.END_USER));
    private final UserSummaryRecord developerOwner =
            new UserSummaryRecord(new UserId(BigInteger.valueOf(8)), "dev", "Dev", List.of(ScoreRole.DEVELOPER));

    private AccCommandRepository accCommand;
    private AsccpCommandRepository asccpCommand;
    private BccpCommandRepository bccpCommand;
    private DtCommandRepository dtCommand;
    private CcCommandService service;

    @BeforeEach
    void setUp() {
        service = new CcCommandService();
        RepositoryFactory repositories = mock(RepositoryFactory.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositories);

        AccQueryRepository accQuery = mock(AccQueryRepository.class);
        AsccpQueryRepository asccpQuery = mock(AsccpQueryRepository.class);
        BccpQueryRepository bccpQuery = mock(BccpQueryRepository.class);
        DtQueryRepository dtQuery = mock(DtQueryRepository.class);
        accCommand = mock(AccCommandRepository.class);
        asccpCommand = mock(AsccpCommandRepository.class);
        bccpCommand = mock(BccpCommandRepository.class);
        dtCommand = mock(DtCommandRepository.class);
        when(repositories.accQueryRepository(any())).thenReturn(accQuery);
        when(repositories.asccpQueryRepository(any())).thenReturn(asccpQuery);
        when(repositories.bccpQueryRepository(any())).thenReturn(bccpQuery);
        when(repositories.dtQueryRepository(any())).thenReturn(dtQuery);
        when(repositories.accCommandRepository(any())).thenReturn(accCommand);
        when(repositories.asccpCommandRepository(any())).thenReturn(asccpCommand);
        when(repositories.bccpCommandRepository(any())).thenReturn(bccpCommand);
        when(repositories.dtCommandRepository(any())).thenReturn(dtCommand);

        AccSummaryRecord acc = mock(AccSummaryRecord.class);
        when(acc.state()).thenReturn(CcState.Deleted);
        when(acc.owner()).thenReturn(developerOwner);
        when(accQuery.getAccSummary(ACC)).thenReturn(acc);
        AsccpSummaryRecord asccp = mock(AsccpSummaryRecord.class);
        when(asccp.state()).thenReturn(CcState.Deleted);
        when(asccp.owner()).thenReturn(developerOwner);
        when(asccpQuery.getAsccpSummary(ASCCP)).thenReturn(asccp);
        BccpSummaryRecord bccp = mock(BccpSummaryRecord.class);
        when(bccp.state()).thenReturn(CcState.Deleted);
        when(bccp.owner()).thenReturn(developerOwner);
        when(bccpQuery.getBccpSummary(BCCP)).thenReturn(bccp);
        DtSummaryRecord dt = mock(DtSummaryRecord.class);
        when(dt.state()).thenReturn(CcState.Deleted);
        when(dt.owner()).thenReturn(developerOwner);
        when(dtQuery.getDtSummary(DT)).thenReturn(dt);
    }

    @Test
    void aUserInAnotherRoleCannotPurgeADeletedComponent() {
        assertThrows(IllegalArgumentException.class, () -> service.purge(endUser, ACC));
        assertThrows(IllegalArgumentException.class, () -> service.purge(endUser, ASCCP));
        assertThrows(IllegalArgumentException.class, () -> service.purge(endUser, BCCP));
        assertThrows(IllegalArgumentException.class, () -> service.purge(endUser, DT));
        verify(accCommand, never()).delete(any(AccManifestId.class));
        verify(asccpCommand, never()).delete(any(AsccpManifestId.class));
        verify(bccpCommand, never()).delete(any(BccpManifestId.class));
        verify(dtCommand, never()).delete(any(DtManifestId.class));
    }

    @Test
    void aUserInAnotherRoleCannotRestoreADeletedComponent() {
        assertThrows(IllegalArgumentException.class, () -> service.updateState(endUser, ACC, CcState.WIP));
        assertThrows(IllegalArgumentException.class, () -> service.updateState(endUser, ASCCP, CcState.WIP));
        assertThrows(IllegalArgumentException.class, () -> service.updateState(endUser, BCCP, CcState.WIP));
        assertThrows(IllegalArgumentException.class, () -> service.updateState(endUser, DT, CcState.WIP));
        verify(accCommand, never()).updateState(any(), any());
        verify(asccpCommand, never()).updateState(any(), any());
        verify(bccpCommand, never()).updateState(any(), any());
        verify(dtCommand, never()).updateState(any(), any());
    }
}
