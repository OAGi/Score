package org.oagi.score.gateway.http.api.code_list_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.code_list_management.controller.payload.CreateCodeListRequest;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListCommandRepository;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListQueryRepository;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.log_management.repository.LogCommandRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** A developer makes a Code List in the Working release, and an end user in another release. */
class CodeListCommandServiceCreateTest {

    private final ReleaseId releaseId = new ReleaseId(BigInteger.ONE);
    private final CodeListManifestId manifestId = new CodeListManifestId(BigInteger.TEN);
    private final CreateCodeListRequest request = new CreateCodeListRequest(new LibraryId(BigInteger.ONE), releaseId, null);
    private final ScoreUser developer = new ScoreUser(new UserId(BigInteger.TEN), "dev", "Dev",
            "dev@example.com", true, List.of(ScoreRole.DEVELOPER));
    private final ScoreUser endUser = new ScoreUser(new UserId(BigInteger.valueOf(11)), "user", "User",
            "user@example.com", false, List.of(ScoreRole.END_USER));

    private CodeListCommandService service;
    private CodeListCommandRepository command;
    private ReleaseQueryRepository releaseQuery;

    @BeforeEach
    void setUp() {
        service = new CodeListCommandService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        command = mock(CodeListCommandRepository.class);
        releaseQuery = mock(ReleaseQueryRepository.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        when(repositoryFactory.codeListCommandRepository(any())).thenReturn(command);
        when(repositoryFactory.codeListQueryRepository(any())).thenReturn(mock(CodeListQueryRepository.class));
        when(repositoryFactory.logCommandRepository(any())).thenReturn(mock(LogCommandRepository.class));
        when(repositoryFactory.releaseQueryRepository(any())).thenReturn(releaseQuery);
        when(command.create(any(), any())).thenReturn(manifestId);
        releaseIsWorking(true);
    }

    private void releaseIsWorking(boolean working) {
        ReleaseSummaryRecord release = mock(ReleaseSummaryRecord.class);
        when(release.isWorkingRelease()).thenReturn(working);
        when(releaseQuery.getReleaseSummary(releaseId)).thenReturn(release);
    }

    @Test
    void a_developer_creates_one_in_the_working_release() {
        assertEquals(manifestId, service.create(developer, request));
    }

    @Test
    void a_developer_cannot_create_one_outside_the_working_release() {
        releaseIsWorking(false);

        assertThrows(IllegalArgumentException.class, () -> service.create(developer, request));
        verify(command, never()).create(any(), any());
    }

    @Test
    void an_end_user_creates_one_outside_the_working_release() {
        releaseIsWorking(false);

        assertEquals(manifestId, service.create(endUser, request));
    }

    @Test
    void an_end_user_cannot_create_one_in_the_working_release() {
        assertThrows(IllegalArgumentException.class, () -> service.create(endUser, request));
        verify(command, never()).create(any(), any());
    }
}
