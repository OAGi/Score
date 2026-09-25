package org.oagi.score.gateway.http.api.code_list_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListDetailsRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.controller.payload.UpdateCodeListRequest;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListCommandRepository;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListQueryRepository;
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

class CodeListCommandServiceNamespaceTest {

    private final CodeListManifestId manifestId = new CodeListManifestId(BigInteger.ONE);
    private final ScoreUser requester = new ScoreUser(new UserId(BigInteger.TEN), "tester", "Tester",
            "tester@example.com", false, List.of(ScoreRole.END_USER));

    private CodeListCommandService service;
    private RepositoryFactory repositoryFactory;
    private CodeListCommandRepository command;
    private CodeListQueryRepository query;
    private LogCommandRepository logCommand;

    @BeforeEach
    void setUp() {
        service = new CodeListCommandService();
        repositoryFactory = mock(RepositoryFactory.class);
        command = mock(CodeListCommandRepository.class);
        query = mock(CodeListQueryRepository.class);
        logCommand = mock(LogCommandRepository.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        when(repositoryFactory.codeListCommandRepository(any())).thenReturn(command);
        when(repositoryFactory.codeListQueryRepository(any())).thenReturn(query);
        when(repositoryFactory.logCommandRepository(any())).thenReturn(logCommand);
        when(query.getCodeListSummary(manifestId)).thenReturn(summaryWithoutNamespace());
    }

    @Test
    void ordinary_state_change_requires_namespace() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateState(requester, manifestId, CcState.QA));
        verifyNoInteractions(command);
    }

    @Test
    void update_requires_namespace() {
        CodeListDetailsRecord details = mock(CodeListDetailsRecord.class);
        when(details.state()).thenReturn(CcState.WIP);
        when(details.owner()).thenReturn(owner());
        when(query.getCodeListDetails(manifestId)).thenReturn(details);
        UpdateCodeListRequest request = new UpdateCodeListRequest(manifestId,
                "Custom code list", "1", "custom-list", null,
                null, null, null, false, false, List.of());

        assertThrows(IllegalArgumentException.class, () -> service.update(requester, request));
        verifyNoInteractions(command);
    }

    @Test
    void delete_is_allowed_without_namespace() {
        when(query.getAssignedBieSummaryList(manifestId)).thenReturn(List.of());
        service.updateState(requester, manifestId, CcState.Deleted);
        verify(command).updateState(manifestId, CcState.Deleted);
    }

    @Test
    void restore_to_wip_is_allowed_without_namespace() {
        when(query.getCodeListSummary(manifestId)).thenReturn(summaryWithoutNamespace(CcState.Deleted));
        when(query.getAssignedBieSummaryList(manifestId)).thenReturn(List.of());
        service.updateState(requester, manifestId, CcState.WIP);
        verify(command).updateState(manifestId, CcState.WIP);
    }

    private CodeListSummaryRecord summaryWithoutNamespace() {
        return summaryWithoutNamespace(CcState.WIP);
    }

    private CodeListSummaryRecord summaryWithoutNamespace(CcState state) {
        return new CodeListSummaryRecord(manifestId, new CodeListId(BigInteger.ONE),
                new Guid("0123456789abcdef0123456789abcdef"), "enum", null, null,
                "Custom code list", "custom-list", "1", null, null,
                false, state, owner(), null, null, List.of());
    }

    private UserSummaryRecord owner() {
        return new UserSummaryRecord(requester.userId(), "tester", "Tester", List.of(ScoreRole.END_USER));
    }
}
