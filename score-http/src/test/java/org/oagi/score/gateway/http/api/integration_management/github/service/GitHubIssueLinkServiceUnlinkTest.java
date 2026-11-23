package org.oagi.score.gateway.http.api.integration_management.github.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.integration_management.github.model.ComponentOwnerState;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueAccManifestId;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueId;
import org.oagi.score.gateway.http.api.integration_management.github.model.GitHubIssueLinkRecord;
import org.oagi.score.gateway.http.api.integration_management.github.repository.GitHubIssueLinkCommandRepository;
import org.oagi.score.gateway.http.api.integration_management.github.repository.GitHubIssueLinkQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unlinking removes the project board card and the cached ETag only when the issue is no longer linked
 * to any other component (the registry row was garbage-collected).
 */
class GitHubIssueLinkServiceUnlinkTest {

    private static final BigInteger LINK_ID = BigInteger.valueOf(5);

    private GitHubIssueLinkService service;
    private GitHubIssueLinkQueryRepository query;
    private GitHubIssueLinkCommandRepository command;
    private GitHubIntegrationService integrationService;
    private ScoreUser requester;
    private GitHubIssueLinkRecord linked;

    @BeforeEach
    void setUp() {
        service = new GitHubIssueLinkService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        query = mock(GitHubIssueLinkQueryRepository.class);
        command = mock(GitHubIssueLinkCommandRepository.class);
        integrationService = mock(GitHubIntegrationService.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        ReflectionTestUtils.setField(service, "integrationService", integrationService);
        ReflectionTestUtils.setField(service, "self", service);
        requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
                "tester@example.com", true, List.of(ScoreRole.DEVELOPER));
        when(repositoryFactory.gitHubIssueLinkQueryRepository(any())).thenReturn(query);
        when(repositoryFactory.gitHubIssueLinkCommandRepository(any())).thenReturn(command);

        GitHubIssueAccManifestId linkId = new GitHubIssueAccManifestId(LINK_ID);
        linked = new GitHubIssueLinkRecord(null, new GitHubIssueId(BigInteger.ONE),
                "OAGi", "oagis", 1097, null);
        when(query.getComponentOwnerStateByLink(linkId))
                .thenReturn(new ComponentOwnerState(requester.userId(), "WIP"));
        when(query.getLinkedIssue(linkId)).thenReturn(linked);
    }

    @Test
    void keepsTheBoardCardAndEtagWhileAnotherComponentStillLinksTheIssue() {
        when(command.deleteIssueIfOrphaned(linked.issueId())).thenReturn(false);

        assertNull(service.doUnlink(requester, CcType.ACC, LINK_ID));
        service.unlink(requester, CcType.ACC, LINK_ID);

        verify(integrationService, never()).evictIssueEtag(anyString(), anyString(), anyInt());
        verify(integrationService, never()).removeIssueFromProject(any(), anyString(), anyString(), anyInt(), any());
    }

    @Test
    void removesTheBoardCardAndEtagWhenTheLastLinkIsRemoved() {
        when(command.deleteIssueIfOrphaned(linked.issueId())).thenReturn(true);

        assertEquals(linked, service.doUnlink(requester, CcType.ACC, LINK_ID));
        service.unlink(requester, CcType.ACC, LINK_ID);

        verify(integrationService).evictIssueEtag("OAGi", "oagis", 1097);
        verify(integrationService).removeIssueFromProject(requester, "OAGi", "oagis", 1097, null);
    }
}
