package org.oagi.score.gateway.http.api.integration_management.github.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.integration_management.github.client.GitHubApiClient;
import org.oagi.score.gateway.http.api.integration_management.github.config.GitHubIntegrationProperties;
import org.oagi.score.gateway.http.api.integration_management.github.model.ProjectFieldOptions;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitHubIntegrationServiceTest {

    private static final ScoreUser USER = new ScoreUser(
            new UserId(BigInteger.ONE), "user", "User", null, false, List.of());

    private GitHubIntegrationService service;

    @Mock
    private GitHubIntegrationProperties properties;

    @Mock
    private ProjectFieldOptions projectFieldOptions;

    @Mock
    private GitHubApiClient gitHubApiClient;

    @BeforeEach
    void setUp() {
        service = spy(new GitHubIntegrationService());
        ReflectionTestUtils.setField(service, "properties", properties);
        ReflectionTestUtils.setField(service, "projectFieldOptions", projectFieldOptions);
        ReflectionTestUtils.setField(service, "gitHubApiClient", gitHubApiClient);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        doReturn("token").when(service).getAccessToken(USER);

        when(properties.getProjectOwnerType()).thenReturn("org");
        when(properties.getProjectOwner()).thenReturn("OAGi");
        when(properties.getProjectNumber()).thenReturn(8);
        when(properties.getProjectStatusFieldName()).thenReturn("Status");
        when(projectFieldOptions.fieldOptionNames()).thenReturn(Set.of("New"));
        when(gitHubApiClient.fetchProject(eq("token"), eq("org"), eq("OAGi"), eq(8)))
                .thenReturn(new GitHubApiClient.ProjectData("project-id", "Project", List.of(
                        new GitHubApiClient.SingleSelectField("status-id", "Status", List.of(
                                new GitHubApiClient.SingleSelectOption("new-id", "New", null))))));
    }

    @Test
    void linkingAnIssueAlreadyOnTheBoardPreservesItsCurrentFieldOption() {
        when(projectFieldOptions.getDefaultFieldOption()).thenReturn("New");
        when(gitHubApiClient.findIssueProjectItem(
                eq("token"), eq("issue-id"), eq("project-id"), eq("Status")))
                .thenReturn(new GitHubApiClient.ProjectItem("item-id", "Implementing"));

        String result = service.addIssueToProjectOnLink(
                USER, "OAGi", "oagis", 1767, "{\"nodeId\":\"issue-id\"}");

        assertThat(result).isEqualTo("Implementing");
        verify(gitHubApiClient, never()).addProjectItem(anyString(), anyString(), anyString());
        verify(gitHubApiClient, never()).setProjectItemFieldOption(
                anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void linkingAnIssueNotOnTheBoardAddsItAndSetsTheInitialFieldOption() {
        when(projectFieldOptions.getDefaultFieldOption()).thenReturn("New");
        when(gitHubApiClient.findIssueProjectItem(
                eq("token"), eq("issue-id"), eq("project-id"), eq("Status")))
                .thenReturn(null);
        when(gitHubApiClient.addProjectItem("token", "project-id", "issue-id"))
                .thenReturn("item-id");
        when(gitHubApiClient.setProjectItemFieldOption(
                "token", "project-id", "item-id", "status-id", "new-id"))
                .thenReturn(true);

        String result = service.addIssueToProjectOnLink(
                USER, "OAGi", "oagis", 1767, "{\"nodeId\":\"issue-id\"}");

        assertThat(result).isEqualTo("New");
        verify(gitHubApiClient).addProjectItem("token", "project-id", "issue-id");
        verify(gitHubApiClient).setProjectItemFieldOption(
                "token", "project-id", "item-id", "status-id", "new-id");
    }

    @Test
    void normalFieldOptionMovesStillUpdateAnExistingBoardCard() {
        when(gitHubApiClient.findIssueProjectItem(
                eq("token"), eq("issue-id"), eq("project-id"), eq("Status")))
                .thenReturn(new GitHubApiClient.ProjectItem("item-id", "Implementing"));
        when(gitHubApiClient.setProjectItemFieldOption(
                "token", "project-id", "item-id", "status-id", "new-id"))
                .thenReturn(true);

        String result = service.moveIssueToFieldOption(
                USER, "OAGi", "oagis", 1767, "{\"nodeId\":\"issue-id\"}", "New");

        assertThat(result).isEqualTo("New");
        verify(gitHubApiClient).setProjectItemFieldOption(
                "token", "project-id", "item-id", "status-id", "new-id");
    }
}
