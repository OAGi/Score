package org.oagi.score.gateway.http.api.integration_management.github.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Project/repo URL parsing and the fieldOption-sync configuration gate (issue #1533, Feature 2). Board writes
 * use the board token (GitHub App or PAT), so per-user OAuth scope handling is no longer involved.
 */
class GitHubIntegrationPropertiesTest {

    @Test
    void parsesAnOrgProjectUrlIgnoringAnyTrailingPath() {
        GitHubIntegrationProperties p = withProjectUrl("https://github.com/orgs/OAGi/projects/8/views/2");
        assertThat(p.getProjectOwnerType()).isEqualTo("org");
        assertThat(p.getProjectOwner()).isEqualTo("OAGi");
        assertThat(p.getProjectNumber()).isEqualTo(8);
    }

    @Test
    void parsesAUserProjectUrl() {
        GitHubIntegrationProperties p = withProjectUrl("https://github.com/users/jdoe/projects/3");
        assertThat(p.getProjectOwnerType()).isEqualTo("user");
        assertThat(p.getProjectOwner()).isEqualTo("jdoe");
        assertThat(p.getProjectNumber()).isEqualTo(3);
    }

    @Test
    void aBlankOrMalformedProjectUrlYieldsNoProject() {
        GitHubIntegrationProperties blank = withProjectUrl("");
        assertThat(blank.getProjectOwner()).isNull();
        assertThat(blank.getProjectNumber()).isZero();
        assertThat(blank.getProjectOwnerType()).isEqualTo("org");

        GitHubIntegrationProperties bad = withProjectUrl("https://example.com/not/a/project");
        assertThat(bad.getProjectOwner()).isNull();
        assertThat(bad.getProjectNumber()).isZero();
    }

    @Test
    void parsesTheDefaultRepositoryUrl() {
        GitHubIntegrationProperties p = withDefaultRepoUrl("https://github.com/OAGi/oagis");
        assertThat(p.getDefaultRepoOwner()).isEqualTo("OAGi");
        assertThat(p.getDefaultRepoName()).isEqualTo("oagis");
    }

    @Test
    void parsesTheDefaultRepositoryUrlIgnoringTrailingPathAndGitSuffix() {
        assertThat(withDefaultRepoUrl("https://github.com/OAGi/oagis.git").getDefaultRepoName())
                .isEqualTo("oagis");
        GitHubIntegrationProperties deep = withDefaultRepoUrl("https://github.com/OAGi/oagis/issues/123");
        assertThat(deep.getDefaultRepoOwner()).isEqualTo("OAGi");
        assertThat(deep.getDefaultRepoName()).isEqualTo("oagis");
    }

    @Test
    void aBlankOrMalformedDefaultRepoUrlYieldsNoOwnerOrName() {
        GitHubIntegrationProperties blank = withDefaultRepoUrl("");
        assertThat(blank.getDefaultRepoOwner()).isNull();
        assertThat(blank.getDefaultRepoName()).isNull();

        GitHubIntegrationProperties bad = withDefaultRepoUrl("https://example.com/whatever");
        assertThat(bad.getDefaultRepoOwner()).isNull();
        assertThat(bad.getDefaultRepoName()).isNull();
    }

    @Test
    void isProjectConfiguredRequiresBaseConfigEnabledFlagAndAParsableUrl() {
        GitHubIntegrationProperties p = new GitHubIntegrationProperties();
        ReflectionTestUtils.setField(p, "enabled", true);
        ReflectionTestUtils.setField(p, "clientId", "id");
        ReflectionTestUtils.setField(p, "clientSecret", "secret");
        ReflectionTestUtils.setField(p, "projectEnabled", true);
        ReflectionTestUtils.setField(p, "projectUrl", "https://github.com/orgs/OAGi/projects/8");
        // Board writes use the connected user's token, so no separate credential is required.
        assertThat(p.isProjectConfigured()).isTrue();

        ReflectionTestUtils.setField(p, "projectEnabled", false);
        assertThat(p.isProjectConfigured()).isFalse();        // fieldOption sync off

        ReflectionTestUtils.setField(p, "projectEnabled", true);
        ReflectionTestUtils.setField(p, "projectUrl", "garbage");
        assertThat(p.isProjectConfigured()).isFalse();        // URL does not parse

        ReflectionTestUtils.setField(p, "projectUrl", "https://github.com/orgs/OAGi/projects/8");
        ReflectionTestUtils.setField(p, "enabled", false);
        assertThat(p.isProjectConfigured()).isFalse();        // base integration off
    }

    @Test
    void grantsProjectWriteMatchesTheProjectTokenExactly() {
        assertThat(GitHubIntegrationProperties.grantsProjectWrite("project")).isTrue();
        assertThat(GitHubIntegrationProperties.grantsProjectWrite("repo,project")).isTrue();
        assertThat(GitHubIntegrationProperties.grantsProjectWrite("repo, project")).isTrue();
        // read:project contains the substring "project" but cannot write the board — must NOT count.
        assertThat(GitHubIntegrationProperties.grantsProjectWrite("repo,read:project")).isFalse();
        assertThat(GitHubIntegrationProperties.grantsProjectWrite("repo")).isFalse();
        assertThat(GitHubIntegrationProperties.grantsProjectWrite("")).isFalse();
        assertThat(GitHubIntegrationProperties.grantsProjectWrite(null)).isFalse();
    }

    @Test
    void effectiveScopeAppendsProjectAndReadOrgOnlyWhenFieldOptionSyncIsOnAndNotAlreadyGranted() {
        assertThat(effectiveScope("repo", false)).isEqualTo("repo");                              // off
        assertThat(effectiveScope("repo", true)).isEqualTo("repo,project,read:org");              // both appended
        assertThat(effectiveScope("repo,project", true)).isEqualTo("repo,project,read:org");      // project not doubled
        assertThat(effectiveScope("repo,project,read:org", true)).isEqualTo("repo,project,read:org"); // none doubled
        assertThat(effectiveScope("repo,read:org", true)).isEqualTo("repo,read:org,project");     // read:org not doubled
        assertThat(effectiveScope("repo,read:project", true)).isEqualTo("repo,read:project,project,read:org");
        assertThat(effectiveScope("", true)).isEqualTo("project,read:org");
        assertThat(effectiveScope(null, true)).isEqualTo("project,read:org");
    }

    private static String effectiveScope(String scope, boolean projectEnabled) {
        GitHubIntegrationProperties p = new GitHubIntegrationProperties();
        ReflectionTestUtils.setField(p, "scope", scope);
        ReflectionTestUtils.setField(p, "projectEnabled", projectEnabled);
        return p.getEffectiveScope();
    }

    private static GitHubIntegrationProperties withProjectUrl(String projectUrl) {
        GitHubIntegrationProperties properties = new GitHubIntegrationProperties();
        ReflectionTestUtils.setField(properties, "projectUrl", projectUrl);
        return properties;
    }

    private static GitHubIntegrationProperties withDefaultRepoUrl(String defaultRepoUrl) {
        GitHubIntegrationProperties properties = new GitHubIntegrationProperties();
        ReflectionTestUtils.setField(properties, "defaultRepoUrl", defaultRepoUrl);
        return properties;
    }
}
