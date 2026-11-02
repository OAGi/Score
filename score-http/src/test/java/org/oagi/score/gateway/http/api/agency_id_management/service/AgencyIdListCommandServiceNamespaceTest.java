package org.oagi.score.gateway.http.api.agency_id_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListDetailsRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.controller.payload.UpdateAgencyIdListRequest;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListCommandRepository;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.log_management.repository.LogCommandRepository;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AgencyIdListCommandServiceNamespaceTest {

    private final AgencyIdListManifestId manifestId = new AgencyIdListManifestId(BigInteger.ONE);
    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.TEN), "tester", "Tester",
            "tester@example.com", false, List.of(ScoreRole.END_USER));

    private AgencyIdListCommandService service;
    private RepositoryFactory repositoryFactory;
    private AgencyIdListCommandRepository command;
    private AgencyIdListQueryRepository query;
    private LogCommandRepository logCommand;

    @BeforeEach
    void setUp() {
        service = new AgencyIdListCommandService();
        repositoryFactory = mock(RepositoryFactory.class);
        command = mock(AgencyIdListCommandRepository.class);
        query = mock(AgencyIdListQueryRepository.class);
        logCommand = mock(LogCommandRepository.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        when(repositoryFactory.agencyIdListCommandRepository(any())).thenReturn(command);
        when(repositoryFactory.agencyIdListQueryRepository(any())).thenReturn(query);
        when(repositoryFactory.logCommandRepository(any())).thenReturn(logCommand);
        when(query.getAgencyIdListSummary(manifestId)).thenReturn(summaryWithoutNamespace());
    }

    @Test
    void ordinary_state_change_requires_namespace() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateState(requester, manifestId, CcState.QA));
        verifyNoInteractions(command);
    }

    @Test
    void update_requires_namespace() {
        AgencyIdListDetailsRecord details = mock(AgencyIdListDetailsRecord.class);
        when(details.state()).thenReturn(CcState.WIP);
        when(details.owner()).thenReturn(owner());
        when(query.getAgencyIdListDetails(manifestId)).thenReturn(details);
        UpdateAgencyIdListRequest request = new UpdateAgencyIdListRequest(manifestId,
                "Custom agency list", "1", "custom-list", null,
                null, null, null, false, List.of());

        assertThrows(IllegalArgumentException.class, () -> service.update(requester, request));
        verifyNoInteractions(command);
    }

    @Test
    void delete_is_allowed_without_namespace() {
        service.updateState(requester, manifestId, CcState.Deleted);
        verify(command).updateState(manifestId, CcState.Deleted);
    }

    @Test
    void restore_to_wip_is_allowed_without_namespace() {
        when(query.getAgencyIdListSummary(manifestId)).thenReturn(summaryWithoutNamespace(CcState.Deleted));
        service.updateState(requester, manifestId, CcState.WIP);
        verify(command).updateState(manifestId, CcState.WIP);
    }

    private AgencyIdListSummaryRecord summaryWithoutNamespace() {
        return summaryWithoutNamespace(CcState.WIP);
    }

    private AgencyIdListSummaryRecord summaryWithoutNamespace(CcState state) {
        return new AgencyIdListSummaryRecord(manifestId, new AgencyIdListId(BigInteger.ONE),
                new Guid("0123456789abcdef0123456789abcdef"), "enum", null,
                "Custom agency list", "custom-list", "1", null, null,
                null, null, false, state, owner(), null, null, List.of());
    }

    private UserSummaryRecord owner() {
        return new UserSummaryRecord(requester.userId(), "tester", "Tester", List.of(ScoreRole.END_USER));
    }
}
