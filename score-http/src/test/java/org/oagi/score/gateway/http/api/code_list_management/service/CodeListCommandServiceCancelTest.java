package org.oagi.score.gateway.http.api.code_list_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.log_management.model.LogSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListDetailsRecord;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListQueryRepository;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListCommandRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CodeListCommandServiceCancelTest {

    private final CodeListCommandService service = new CodeListCommandService();
    private final CodeListQueryRepository query = mock(CodeListQueryRepository.class);
    private final CodeListCommandRepository command = mock(CodeListCommandRepository.class);
    private final CodeListManifestId manifestId = new CodeListManifestId(BigInteger.ONE);

    @BeforeEach
    void setUp() {
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        when(repositoryFactory.codeListQueryRepository(any())).thenReturn(query);
        when(repositoryFactory.codeListCommandRepository(any())).thenReturn(command);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
    }

    private ScoreUser user(ScoreRole role) {
        return new ScoreUser(new UserId(BigInteger.ONE), "user", "User", null, false, List.of(role));
    }

    private void existing(CcState state, String releaseNum, ScoreRole ownerRole) {
        CodeListDetailsRecord details = mock(CodeListDetailsRecord.class);
        when(details.state()).thenReturn(state);
        when(details.name()).thenReturn("name");
        when(details.release()).thenReturn(new ReleaseSummaryRecord(null, null, releaseNum, null));
        when(details.owner()).thenReturn(new UserSummaryRecord(new UserId(BigInteger.TWO), "o", "Owner", List.of(ownerRole)));
        when(details.log()).thenReturn(new LogSummaryRecord(null, 2, 1));
        when(query.getCodeListDetails(manifestId)).thenReturn(details);
    }

    private void assertRejected(ScoreUser requester) {
        assertThatThrownBy(() -> service.cancel(requester, manifestId, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(command);
    }

    @Test
    void rejectsUnknownManifest() {
        assertRejected(user(ScoreRole.END_USER));
    }

    @Test
    void rejectsReleasedState() {
        existing(CcState.Production, "10.12", ScoreRole.END_USER);
        assertRejected(user(ScoreRole.END_USER));

        existing(CcState.Published, "Working", ScoreRole.DEVELOPER);
        assertRejected(user(ScoreRole.DEVELOPER));
    }

    @Test
    void rejectsWrongBranchForRole() {
        existing(CcState.WIP, "Working", ScoreRole.END_USER);
        assertRejected(user(ScoreRole.END_USER));

        existing(CcState.WIP, "10.12", ScoreRole.DEVELOPER);
        assertRejected(user(ScoreRole.DEVELOPER));
    }

    @Test
    void rejectsOwnerWithDifferentRole() {
        existing(CcState.WIP, "10.12", ScoreRole.DEVELOPER);
        assertRejected(user(ScoreRole.END_USER));
    }
}
