package org.oagi.score.gateway.http.api.integration_management.github.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.integration_management.github.config.GitHubIntegrationProperties;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Exhaustive coverage of {@code GitHubIntegrationService#isSafeReturnUrl} (open redirect after
 * the GitHub OAuth callback). The method is private, so it is exercised reflectively.
 */
@ExtendWith(MockitoExtension.class)
class GitHubIntegrationServiceReturnUrlTest {

    private static final String WEB_BASE = "https://score.example.org";

    private GitHubIntegrationService service;

    @Mock
    private GitHubIntegrationProperties properties;

    @BeforeEach
    void setUp() {
        service = new GitHubIntegrationService();
        ReflectionTestUtils.setField(service, "properties", properties);
        lenient().when(properties.getWebBaseUrl()).thenReturn(WEB_BASE);
    }

    private boolean isSafe(String returnUrl) {
        return Boolean.TRUE.equals(ReflectionTestUtils.invokeMethod(service, "isSafeReturnUrl", returnUrl));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void rejectsNullBlankAndEmpty(String returnUrl) {
        assertThat(isSafe(returnUrl)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/",
            "/bie",
            "/bie/express?manifestId=42",
            "/bie/express?manifestId=42#section",
            "/a/b/../c",
            "/path%20with%20escapes",
            "/%5Cevil.example",   // percent-encoded backslash is not decoded by browsers before parsing
            "/?next=//evil.example",
            "/?next=https://evil.example",
            "/a//b",
            "/%2F/evil.example",
    })
    void acceptsSameOriginRelativePaths(String returnUrl) {
        assertThat(isSafe(returnUrl)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "//evil.example",
            "//evil.example/login",
            "///evil.example",
            "////evil.example",
            "//",
            "/\\evil.example",
            "/\\/evil.example",
            "\\\\evil.example",
            "\\evil.example",
            "/foo\\bar",
    })
    void rejectsProtocolRelativeAndBackslashForms(String returnUrl) {
        assertThat(isSafe(returnUrl)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            WEB_BASE,
            WEB_BASE + "/",
            WEB_BASE + "/bie/express?manifestId=42",
            WEB_BASE + "/path#fragment",
    })
    void acceptsAbsoluteUrlsOnTheConfiguredOrigin(String returnUrl) {
        assertThat(isSafe(returnUrl)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://evil.example",
            "https://evil.example/login",
            "http://score.example.org",                 // scheme downgrade
            "https://score.example.org:8443/",          // different port
            "https://sub.score.example.org/",           // sub-domain
            "https://score.example.org.evil.example/",  // suffix attack
            "https://evil-score.example.org/",
            "https://evil.example/https://score.example.org",
            "https://evil.example?x=https://score.example.org",
            "https://evil.example#https://score.example.org",
            "https://score.example.org@evil.example/",  // userinfo trick: host is evil.example
            "https://score.example.org:pw@evil.example/",
            "https://score.example.org%2F@evil.example/",
            "https://evil.example\\@score.example.org/", // backslash makes java.net.URI throw
            "https://evil.example\\.score.example.org/",
    })
    void rejectsAbsoluteUrlsOnAnotherOrigin(String returnUrl) {
        assertThat(isSafe(returnUrl)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "javascript:alert(1)",
            "JavaScript:alert(1)",
            "javascript://score.example.org/%0Aalert(1)",
            "data:text/html,<script>alert(1)</script>",
            "vbscript:msgbox(1)",
            "file:///etc/passwd",
            "ftp://score.example.org/",
            "mailto:a@score.example.org",
            "https:evil.example",
            "https:/evil.example",
            "https:///evil.example",
    })
    void rejectsNonHttpSchemesAndMalformedAbsoluteForms(String returnUrl) {
        assertThat(isSafe(returnUrl)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "evil.example",
            "evil.example/login",
            "www.evil.example",
            "bie/express",
            "./bie",
            "../bie",
            "?next=/bie",
            "#/bie",
    })
    void rejectsSchemelessNonRootedValues(String returnUrl) {
        assertThat(isSafe(returnUrl)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            " /bie",
            "\t/bie",
            " https://score.example.org/",
            "https://score.example.org/ ",
            "https://score.example.org/a b",
            "/a b",             // raw whitespace is never legitimate in a redirect target
            "http://[::1",
            "https://[",
            "%",
            "https://evil.example/%zz",
    })
    void rejectsMalformedOrWhitespaceWrappedValues(String returnUrl) {
        assertThat(isSafe(returnUrl)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/\t/evil.example",
            "/\n/evil.example",
            "/\r/evil.example",
            "/\r\nLocation: https://evil.example",
            "/bie\r\nSet-Cookie: x=y",
            "/\u0000/evil.example",
    })
    void rejectsControlCharactersThatBrowsersStripBeforeParsing(String returnUrl) {
        // WHATWG URL parsing removes ASCII tab/CR/LF anywhere in the input, so "/\t/evil.example" is "//evil.example".
        assertThat(isSafe(returnUrl)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "HTTPS://score.example.org/bie",
            "https://SCORE.EXAMPLE.ORG/bie",
    })
    void treatsSchemeAndHostComparisonAsCaseSensitiveAndFailsClosed(String returnUrl) {
        // Same origin per RFC 3986, but the implementation compares case-sensitively. This is a safe false-negative
        // (falls back to the default landing page); pinned here so a future change is a conscious decision.
        assertThat(isSafe(returnUrl)).isFalse();
    }

    @Test
    void defaultPortIsNotNormalised() {
        when(properties.getWebBaseUrl()).thenReturn("https://score.example.org");
        assertThat(isSafe("https://score.example.org:443/bie")).isFalse();
    }

    @Test
    void honoursAnExplicitPortOnTheConfiguredOrigin() {
        when(properties.getWebBaseUrl()).thenReturn("http://localhost:4200");
        assertThat(isSafe("http://localhost:4200/bie")).isTrue();
        assertThat(isSafe("http://localhost:4201/bie")).isFalse();
        assertThat(isSafe("http://localhost/bie")).isFalse();
        assertThat(isSafe("https://localhost:4200/bie")).isFalse();
    }

    @Test
    void aWebBaseUrlWithAPathStillMatchesOnOriginOnly() {
        when(properties.getWebBaseUrl()).thenReturn("https://score.example.org/app");
        assertThat(isSafe("https://score.example.org/elsewhere")).isTrue();
        assertThat(isSafe("https://evil.example/app")).isFalse();
    }

    @Test
    void relativePathsAreAcceptedWithoutConsultingTheConfiguredBaseUrl() {
        lenient().when(properties.getWebBaseUrl()).thenReturn(null);
        assertThat(isSafe("/bie")).isTrue();
    }

    @Test
    void absoluteUrlsFailClosedWhenTheConfiguredBaseUrlIsMissing() {
        when(properties.getWebBaseUrl()).thenReturn(null);
        assertThat(isSafe("https://evil.example/")).isFalse();
        assertThat(isSafe("https://score.example.org/")).isFalse();
    }

    @Test
    void absoluteUrlsFailClosedWhenTheConfiguredBaseUrlIsBlankOrMalformed() {
        when(properties.getWebBaseUrl()).thenReturn("");
        assertThat(isSafe("https://evil.example/")).isFalse();
        when(properties.getWebBaseUrl()).thenReturn("not a url");
        assertThat(isSafe("https://evil.example/")).isFalse();
    }

    @Test
    void doesNotAcceptAHostlessConfiguredBaseUrlMatchingAHostlessTarget() {
        // Both URIs have a null host; Objects.equals(null, null) must not make this "same origin".
        when(properties.getWebBaseUrl()).thenReturn("https:///");
        assertThat(isSafe("https:///evil.example")).isFalse();
    }
}
