package org.oagi.score.gateway.http.api.cc_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.bccp.BccpUpdateRequest;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.BccpCommandRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.BccpQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * #72 coverage for {@link CcCommandService#updateBccpList}: the request's nillable and deprecated
 * flags must reach {@link BccpCommandRepository#update} in the repository's declared order.
 */
class CcCommandServiceUpdateBccpTest {

    private static final BccpManifestId BCCP_MANIFEST_ID = new BccpManifestId(BigInteger.TEN);

    private CcCommandService service;
    private BccpCommandRepository bccpCommandRepository;
    private ScoreUser requester;

    @BeforeEach
    void setUp() {
        service = new CcCommandService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        BccpQueryRepository bccpQueryRepository = mock(BccpQueryRepository.class);
        bccpCommandRepository = mock(BccpCommandRepository.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        UserId userId = new UserId(BigInteger.ONE);
        requester = new ScoreUser(userId, "eu", "End User", "eu@example.com", true, List.of(ScoreRole.END_USER));

        BccpSummaryRecord bccp = mock(BccpSummaryRecord.class);
        UserSummaryRecord owner = mock(UserSummaryRecord.class);
        when(owner.userId()).thenReturn(userId);
        when(bccp.owner()).thenReturn(owner);
        when(bccp.state()).thenReturn(CcState.WIP);

        when(repositoryFactory.bccpQueryRepository(any())).thenReturn(bccpQueryRepository);
        when(repositoryFactory.bccpCommandRepository(any())).thenReturn(bccpCommandRepository);
        when(bccpQueryRepository.getBccpSummary(BCCP_MANIFEST_ID)).thenReturn(bccp);
    }

    private void updateWith(boolean nillable, boolean deprecated) {
        service.updateBccpList(requester, List.of(new BccpUpdateRequest(
                BCCP_MANIFEST_ID, null, nillable, deprecated, null, null, null, null, null)));
    }

    @Test
    void updateBccp_nillableOnlyIsNotStoredAsDeprecated() {
        updateWith(true, false);

        verify(bccpCommandRepository).update(eq(BCCP_MANIFEST_ID), any(), eq(true), eq(false), any(), any(), any());
    }

    @Test
    void updateBccp_deprecatedOnlyIsNotStoredAsNillable() {
        updateWith(false, true);

        verify(bccpCommandRepository).update(eq(BCCP_MANIFEST_ID), any(), eq(false), eq(true), any(), any(), any());
    }
}
