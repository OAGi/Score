package org.oagi.score.gateway.http.api.integration_management.github.service;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.integration_management.github.client.GitHubApiClient;
import org.oagi.score.gateway.http.api.integration_management.github.config.GitHubIntegrationProperties;
import org.oagi.score.gateway.http.api.integration_management.github.model.ProjectFieldOptions;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
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

    @Mock
    private RedisTemplate redisTemplate;

    @Mock
    private ValueOperations valueOperations;

    @Mock
    private org.springframework.data.redis.core.SetOperations setOperations;

    @BeforeEach
    void setUp() {
        service = spy(new GitHubIntegrationService());
        ReflectionTestUtils.setField(service, "properties", properties);
        ReflectionTestUtils.setField(service, "projectFieldOptions", projectFieldOptions);
        ReflectionTestUtils.setField(service, "gitHubApiClient", gitHubApiClient);
        ReflectionTestUtils.setField(service, "redisTemplate", redisTemplate);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        lenient().doReturn("token").when(service).getAccessToken(USER);

        lenient().when(properties.getProjectOwnerType()).thenReturn("org");
        lenient().when(properties.getProjectOwner()).thenReturn("OAGi");
        lenient().when(properties.getProjectNumber()).thenReturn(8);
        lenient().when(properties.getProjectStatusFieldName()).thenReturn("Status");
        lenient().when(projectFieldOptions.fieldOptionNames()).thenReturn(Set.of("New"));
        lenient().when(gitHubApiClient.fetchProject(eq("token"), eq("org"), eq("OAGi"), eq(8)))
                .thenReturn(new GitHubApiClient.ProjectData("project-id", "Project", List.of(
                        new GitHubApiClient.SingleSelectField("status-id", "Status", List.of(
                                new GitHubApiClient.SingleSelectOption("new-id", "New", null))))));
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
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

    // --- returnUrl validation (open redirect after the OAuth callback) ---

    @Test
    void beginConnectStoresAnOffSiteReturnUrlAsEmpty() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(properties.getWebBaseUrl()).thenReturn("http://localhost:4200");
        when(properties.getAuthorizationUri()).thenReturn("https://github.com/login/oauth/authorize");
        when(properties.getClientId()).thenReturn("client-id");
        when(properties.getRedirectUri()).thenReturn("http://localhost:4200/api/integration/github/callback");
        when(properties.getEffectiveScope()).thenReturn("repo");

        service.beginConnect(USER, "https://evil.example/login");

        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(valueOperations).set(anyString(), captor.capture(), any(Duration.class));
        assertThat(captor.getValue().get("returnUrl")).isEqualTo("");
    }

    @Test
    void beginConnectStoresASameOriginReturnUrlAsIs() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(properties.getAuthorizationUri()).thenReturn("https://github.com/login/oauth/authorize");
        when(properties.getClientId()).thenReturn("client-id");
        when(properties.getRedirectUri()).thenReturn("http://localhost:4200/api/integration/github/callback");
        when(properties.getEffectiveScope()).thenReturn("repo");

        service.beginConnect(USER, "/bie/express?manifestId=42");

        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(valueOperations).set(anyString(), captor.capture(), any(Duration.class));
        assertThat(captor.getValue().get("returnUrl")).isEqualTo("/bie/express?manifestId=42");
    }

    @Test
    void beginConnectStoresAnAbsoluteSameOriginReturnUrlAsIs() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(properties.getWebBaseUrl()).thenReturn("http://localhost:4200");
        when(properties.getAuthorizationUri()).thenReturn("https://github.com/login/oauth/authorize");
        when(properties.getClientId()).thenReturn("client-id");
        when(properties.getRedirectUri()).thenReturn("http://localhost:4200/api/integration/github/callback");
        when(properties.getEffectiveScope()).thenReturn("repo");

        service.beginConnect(USER, "http://localhost:4200/bie/express?manifestId=42");

        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(valueOperations).set(anyString(), captor.capture(), any(Duration.class));
        assertThat(captor.getValue().get("returnUrl")).isEqualTo("http://localhost:4200/bie/express?manifestId=42");
    }

    @Test
    void completeCallbackFallsBackToEmptyWhenTheStoredReturnUrlIsOffSite() {
        Map<String, String> stateData = new HashMap<>();
        stateData.put("userId", "1");
        stateData.put("returnUrl", "https://evil.example/login");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("score:integration:github:state:abc")).thenReturn(stateData);
        when(properties.getWebBaseUrl()).thenReturn("http://localhost:4200");
        when(gitHubApiClient.exchangeOAuthCode("code", "abc"))
                .thenReturn(new GitHubApiClient.OAuthToken("access-token", "repo"));
        when(gitHubApiClient.fetchLogin("access-token")).thenReturn("octocat");

        String result = service.completeCallback("code", "abc");

        assertThat(result).isEmpty();
    }
}
