package org.oagi.score.gateway.http.api.agency_id_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListCommandRepository;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.log_management.repository.LogCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AgencyIdListCommandServicePurgeTest {

    private final AgencyIdListCommandService service = new AgencyIdListCommandService();
    private final AgencyIdListQueryRepository query = mock(AgencyIdListQueryRepository.class);
    private final AgencyIdListCommandRepository command = mock(AgencyIdListCommandRepository.class);
    private final LogCommandRepository logCommand = mock(LogCommandRepository.class);
    private final AgencyIdListManifestId manifestId = new AgencyIdListManifestId(BigInteger.ONE);

    @BeforeEach
    void setUp() {
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        when(repositoryFactory.agencyIdListQueryRepository(any())).thenReturn(query);
        when(repositoryFactory.agencyIdListCommandRepository(any())).thenReturn(command);
        when(repositoryFactory.logCommandRepository(any())).thenReturn(logCommand);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
    }

    private ScoreUser user(ScoreRole role) {
        return new ScoreUser(new UserId(BigInteger.ONE), "user", "User", null, false, List.of(role));
    }

    private void existing(CcState state, ScoreRole ownerRole) {
        AgencyIdListSummaryRecord summary = mock(AgencyIdListSummaryRecord.class);
        when(summary.state()).thenReturn(state);
        when(summary.owner()).thenReturn(new UserSummaryRecord(new UserId(BigInteger.TWO), "o", "Owner", List.of(ownerRole)));
        when(query.getAgencyIdListSummary(manifestId)).thenReturn(summary);
    }

    @Test
    void rejectsPurgeOfANonDeletedList() {
        existing(CcState.WIP, ScoreRole.DEVELOPER);

        assertThatThrownBy(() -> service.purge(user(ScoreRole.DEVELOPER), manifestId))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(command);
    }

    @Test
    void rejectsPurgeByAUserOfAnotherRoleThanTheOwner() {
        existing(CcState.Deleted, ScoreRole.DEVELOPER);
        assertThatThrownBy(() -> service.purge(user(ScoreRole.END_USER), manifestId))
                .isInstanceOf(IllegalArgumentException.class);

        existing(CcState.Deleted, ScoreRole.END_USER);
        assertThatThrownBy(() -> service.purge(user(ScoreRole.DEVELOPER), manifestId))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(command);
    }

    @Test
    void purgesADeletedListOfTheSameRole() {
        existing(CcState.Deleted, ScoreRole.DEVELOPER);
        when(command.delete(manifestId)).thenReturn(true);

        assertThat(service.purge(user(ScoreRole.DEVELOPER), manifestId)).isTrue();
        verify(command).delete(manifestId);
    }
}
