package org.oagi.score.gateway.http.api.cc_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.ascc.AsccCreateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.bcc.BccCreateRequest;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.AccCommandRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.AccQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.AsccpQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.BccpQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A deleted ASCCP or BCCP must not be appended to an ACC.
 */
class CcCommandServiceAppendDeletedPropertyTest {

    private static final AccManifestId ACC_ID = new AccManifestId(BigInteger.ONE);
    private static final AsccpManifestId ASCCP_ID = new AsccpManifestId(BigInteger.TWO);
    private static final BccpManifestId BCCP_ID = new BccpManifestId(BigInteger.TEN);

    private final ScoreUser requester = mock(ScoreUser.class);
    private CcCommandService service;
    private AccCommandRepository accCommand;
    private AsccpSummaryRecord asccp;
    private BccpSummaryRecord bccp;

    @BeforeEach
    void setUp() {
        service = new CcCommandService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        AccQueryRepository accQuery = mock(AccQueryRepository.class);
        accCommand = mock(AccCommandRepository.class);
        AsccpQueryRepository asccpQuery = mock(AsccpQueryRepository.class);
        BccpQueryRepository bccpQuery = mock(BccpQueryRepository.class);
        when(repositoryFactory.accQueryRepository(any())).thenReturn(accQuery);
        when(repositoryFactory.accCommandRepository(any())).thenReturn(accCommand);
        when(repositoryFactory.asccpQueryRepository(any())).thenReturn(asccpQuery);
        when(repositoryFactory.bccpQueryRepository(any())).thenReturn(bccpQuery);

        AccSummaryRecord acc = mock(AccSummaryRecord.class);
        when(accQuery.getAccSummary(ACC_ID)).thenReturn(acc);
        asccp = mock(AsccpSummaryRecord.class);
        bccp = mock(BccpSummaryRecord.class);
        when(asccpQuery.getAsccpSummary(ASCCP_ID)).thenReturn(asccp);
        when(bccpQuery.getBccpSummary(BCCP_ID)).thenReturn(bccp);
    }

    @Test
    void deletedAsccpIsRejected() {
        when(asccp.state()).thenReturn(CcState.Deleted);

        assertThrows(IllegalArgumentException.class,
                () -> service.createAscc(requester, AsccCreateRequest.builder(ACC_ID, ASCCP_ID).build()));
        verify(accCommand, never()).createAscc(any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    void deletedBccpIsRejected() {
        when(bccp.state()).thenReturn(CcState.Deleted);

        assertThrows(IllegalArgumentException.class,
                () -> service.createBcc(requester, BccCreateRequest.builder(ACC_ID, BCCP_ID).build()));
        verify(accCommand, never()).createBcc(any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyBoolean());
    }
}
