package org.oagi.score.gateway.http.api.code_list_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListCommandRepository;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListQueryRepository;
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

class CodeListCommandServicePurgeTest {

    private final CodeListCommandService service = new CodeListCommandService();
    private final CodeListQueryRepository query = mock(CodeListQueryRepository.class);
    private final CodeListCommandRepository command = mock(CodeListCommandRepository.class);
    private final LogCommandRepository logCommand = mock(LogCommandRepository.class);
    private final CodeListManifestId manifestId = new CodeListManifestId(BigInteger.ONE);

    @BeforeEach
    void setUp() {
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        when(repositoryFactory.codeListQueryRepository(any())).thenReturn(query);
        when(repositoryFactory.codeListCommandRepository(any())).thenReturn(command);
        when(repositoryFactory.logCommandRepository(any())).thenReturn(logCommand);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
    }

    private ScoreUser user(ScoreRole role) {
        return new ScoreUser(new UserId(BigInteger.ONE), "user", "User", null, false, List.of(role));
    }

    private void existing(CcState state, ScoreRole ownerRole) {
        CodeListSummaryRecord summary = mock(CodeListSummaryRecord.class);
        when(summary.state()).thenReturn(state);
        when(summary.owner()).thenReturn(new UserSummaryRecord(new UserId(BigInteger.TWO), "o", "Owner", List.of(ownerRole)));
        when(query.getCodeListSummary(manifestId)).thenReturn(summary);
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
