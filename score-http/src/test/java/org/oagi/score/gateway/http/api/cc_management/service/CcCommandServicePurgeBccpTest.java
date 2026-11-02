package org.oagi.score.gateway.http.api.cc_management.service;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.AccQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.BccpCommandRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.BccpQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Purging a BCCP that is still referenced by BCCs must be refused instead of deleting it.
 */
class CcCommandServicePurgeBccpTest {

    private static final BccpManifestId BCCP_MANIFEST_ID = new BccpManifestId(BigInteger.TEN);

    @Test
    void purgeIsRefusedWhileBccsReferenceTheBccp() {
        CcCommandService service = new CcCommandService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        BccpQueryRepository bccpQuery = mock(BccpQueryRepository.class);
        BccpCommandRepository bccpCommand = mock(BccpCommandRepository.class);
        AccQueryRepository accQuery = mock(AccQueryRepository.class);
        BccpSummaryRecord bccp = mock(BccpSummaryRecord.class);
        when(bccp.state()).thenReturn(CcState.Deleted);
        when(bccp.owner()).thenReturn(mock(UserSummaryRecord.class));
        when(bccp.den()).thenReturn("Prop. Type");
        when(bccpQuery.getBccpSummary(BCCP_MANIFEST_ID)).thenReturn(bccp);
        when(accQuery.getBccSummaryList(BCCP_MANIFEST_ID)).thenReturn(List.of(mock(BccSummaryRecord.class)));
        when(repositoryFactory.bccpQueryRepository(any())).thenReturn(bccpQuery);
        when(repositoryFactory.bccpCommandRepository(any())).thenReturn(bccpCommand);
        when(repositoryFactory.accQueryRepository(any())).thenReturn(accQuery);

        assertThrows(IllegalArgumentException.class,
                () -> service.purge(mock(ScoreUser.class), BCCP_MANIFEST_ID));
        verify(bccpCommand, never()).delete(any());
    }
}
