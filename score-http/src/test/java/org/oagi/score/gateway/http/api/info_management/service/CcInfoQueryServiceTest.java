package org.oagi.score.gateway.http.api.info_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.repository.CcQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.service.CcListService;
import org.oagi.score.gateway.http.api.info_management.model.SummaryCcExt;
import org.oagi.score.gateway.http.api.info_management.model.SummaryCcExtInfoRecord;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CcInfoQueryServiceTest {

    private SummaryCcExt extension(long ownerUserId, CcState state) {
        SummaryCcExt extension = new SummaryCcExt();
        extension.setOwnerUserId(new UserId(BigInteger.valueOf(ownerUserId)));
        extension.setOwnerUsername("user" + ownerUserId);
        extension.setState(state);
        return extension;
    }

    @Test
    void countsOnlyTheRequestersUserExtensionsAsMine() {
        ScoreUser requester =
                new ScoreUser(new UserId(BigInteger.ONE), "me", "Me", null, false, List.of(ScoreRole.END_USER));
        CcQueryRepository ccQuery = mock(CcQueryRepository.class);
        when(ccQuery.getSummaryCcExtList(any(), any())).thenReturn(List.of(
                extension(1, CcState.WIP), extension(2, CcState.WIP), extension(3, CcState.WIP),
                extension(2, CcState.Production)));
        RepositoryFactory repositories = mock(RepositoryFactory.class);
        when(repositories.ccQueryRepository(any())).thenReturn(ccQuery);
        CcListService ccListService = mock(CcListService.class);
        when(ccListService.getMyExtensionsUnusedInBIEs(any(), any())).thenReturn(List.of());
        CcInfoQueryService service = new CcInfoQueryService();
        ReflectionTestUtils.setField(service, "repositoryFactory", repositories);
        ReflectionTestUtils.setField(service, "ccListService", ccListService);

        SummaryCcExtInfoRecord info = service.getSummaryCcExtInfo(requester, null, null);

        assertThat(info.numberOfMyCcExtByStates()).isEqualTo(Map.of(CcState.WIP, 1));
        assertThat(info.numberOfTotalCcExtByStates()).isEqualTo(Map.of(CcState.WIP, 3, CcState.Production, 1));
    }
}
