package org.oagi.score.gateway.http.configuration.security;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationQueryService;
import org.oagi.score.gateway.http.api.integration_management.github.config.GitHubIntegrationProperties;
import org.oagi.score.gateway.http.configuration.oauth2.ScoreClientRegistrationRepository;
import org.oagi.score.gateway.http.configuration.oauth2.ScoreOAuth2AuthorizedClientService;
import org.oagi.score.gateway.http.configuration.security.webauthn.ClientAddressResolver;
import org.oagi.score.gateway.http.configuration.security.webauthn.PasskeyConfiguration;
import org.oagi.score.gateway.http.configuration.security.webauthn.PasskeyProperties;
import org.oagi.score.gateway.http.configuration.security.webauthn.PasskeyRateLimiter;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The real {@link SecurityConfiguration#filterChain} (form login, logout, session) with a stub endpoint behind it:
 * what a page on another origin can and cannot do with a signed-in or signed-out browser. The app is served at
 * {@code score.example.org}; {@code localhost:4200} is configured as an extra allowed origin.
 */
class MainFilterChainTest {

    private static final String APP = "https://score.example.org";
    private static final String EVIL = "https://evil.example";

    private static SecurityConfiguration securityConfiguration;

    @Configuration
    @EnableWebSecurity
    @EnableWebMvc
    @Import(PasskeyConfiguration.class)
    static class Config {

        @Bean
        @Order(2)
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return securityConfiguration.filterChain(http);
        }

        @Bean
        AuthenticationProvider authenticationProvider() {
            return securityConfiguration.authenticationProvider();
        }

        @Bean
        org.springframework.security.core.userdetails.UserDetailsService userDetailsService() {
            // A lambda: Spring would try to autowire the DSLContext of a (mock) AppUserDetailsService.
            AppUserDetailsService delegate = (AppUserDetailsService) ReflectionTestUtils.getField(securityConfiguration, "userDetailsService");
            return delegate::loadUserByUsername;
        }

        /** The mocked repositories below are Spring beans too and get their DSLContext field autowired. */
        @Bean
        org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
            return securityConfiguration.passwordEncoder();
        }

        @Bean
        org.jooq.DSLContext dslContext() {
            return mock(org.jooq.DSLContext.class);
        }

        @Bean
        PasskeyProperties passkeyProperties() {
            return (PasskeyProperties) ReflectionTestUtils.getField(securityConfiguration, "passkeyProperties");
        }

        @Bean
        PublicKeyCredentialUserEntityRepository userEntities() {
            return mock(PublicKeyCredentialUserEntityRepository.class);
        }

        @Bean
        UserCredentialRepository userCredentials() {
            return mock(UserCredentialRepository.class);
        }

        @Bean
        ScoreClientRegistrationRepository clientRegistrationRepository() {
            return (ScoreClientRegistrationRepository) ReflectionTestUtils.getField(securityConfiguration, "clientRegistrationRepository");
        }

        @Bean
        StubController stubController() {
            return new StubController();
        }
    }

    @RestController
    static class StubController {
        @PostMapping("/stub/change")
        String change() {
            return "changed";
        }

        @GetMapping("/stub/read")
        String read() {
            return "read";
        }
    }

    private SessionService sessionService;

    private MockMvc mvc() {
        sessionService = mock(SessionService.class);
        AppUserDetailsService userDetailsService = mock(AppUserDetailsService.class);
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(
                new User("alice", new BCryptPasswordEncoder().encode("secret"),
                        List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))));

        securityConfiguration = new SecurityConfiguration();
        ReflectionTestUtils.setField(securityConfiguration, "userDetailsService", userDetailsService);
        ReflectionTestUtils.setField(securityConfiguration, "sessionService", sessionService);
        ReflectionTestUtils.setField(securityConfiguration, "oAuth2AuthorizedClientService", mock(ScoreOAuth2AuthorizedClientService.class));
        ReflectionTestUtils.setField(securityConfiguration, "authenticationSuccessHandler", mock(ScoreAuthenticationSuccessHandler.class));
        ReflectionTestUtils.setField(securityConfiguration, "clientRegistrationRepository", mock(ScoreClientRegistrationRepository.class));
        ReflectionTestUtils.setField(securityConfiguration, "applicationConfigurationService", mock(ApplicationConfigurationQueryService.class));
        ReflectionTestUtils.setField(securityConfiguration, "gitHubIntegrationProperties", new GitHubIntegrationProperties());
        ReflectionTestUtils.setField(securityConfiguration, "passkeyProperties", new PasskeyProperties("", "", ""));
        ReflectionTestUtils.setField(securityConfiguration, "passkeyAuthenticationProvider", mock(
                org.oagi.score.gateway.http.configuration.security.webauthn.PasskeyAuthenticationProvider.class));
        ReflectionTestUtils.setField(securityConfiguration, "passkeyRateLimiter", mock(PasskeyRateLimiter.class));
        ReflectionTestUtils.setField(securityConfiguration, "clientAddressResolver", mock(ClientAddressResolver.class));
        ReflectionTestUtils.setField(securityConfiguration, "corsAllowedOrigins", List.of("http://localhost:4200"));

        GenericWebApplicationContext context = new GenericWebApplicationContext(new MockServletContext());
        AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
        context.registerBean(Config.class);
        context.refresh();
        return webAppContextSetup(context).apply(springSecurity()).build();
    }

    private static MockHttpServletRequestBuilder login(String origin, String password) {
        MockHttpServletRequestBuilder request = post("/login").secure(true).header("Host", "score.example.org")
                .param("username", "alice").param("password", password);
        return origin == null ? request : request.header("Origin", origin);
    }

    private static MockHttpServletRequestBuilder withOrigin(MockHttpServletRequestBuilder request, MockHttpSession session, String origin) {
        request.secure(true).header("Host", "score.example.org");
        if (session != null) {
            request.session(session);
        }
        return origin == null ? request : request.header("Origin", origin);
    }

    /** Signs in from the app and returns the browser's session. */
    private MockHttpSession signedIn(MockMvc mvc) throws Exception {
        MockHttpSession session = (MockHttpSession) mvc.perform(login(APP, "secret"))
                .andReturn().getRequest().getSession();
        mvc.perform(withOrigin(get("/stub/read"), session, null)).andExpect(status().isOk());
        return session;
    }

    // ---- login CSRF ----

    @Test
    void aCrossSiteFormCannotSignTheVictimIn() throws Exception {
        MockMvc mvc = mvc();
        MockHttpSession session = (MockHttpSession) mvc.perform(login(EVIL, "secret"))
                .andExpect(status().isForbidden()).andReturn().getRequest().getSession(false);

        // No session was authenticated: a later request carrying whatever the browser got is still unauthorized.
        mvc.perform(withOrigin(get("/stub/read"), session, null)).andExpect(status().isUnauthorized());
    }

    @Test
    void signingInFromTheAppOrWithoutAnOriginStillWorks() throws Exception {
        for (String origin : new String[]{APP, "http://localhost:4200", null}) {
            MockMvc mvc = mvc();
            MockHttpSession session = (MockHttpSession) mvc.perform(login(origin, "secret"))
                    .andReturn().getRequest().getSession();
            mvc.perform(withOrigin(get("/stub/read"), session, null)).andExpect(status().isOk());
        }
    }

    // ---- state-changing requests with the victim's session ----

    @Test
    void aCrossSitePostWithTheSessionCookieIsRejected() throws Exception {
        MockMvc mvc = mvc();
        MockHttpSession session = signedIn(mvc);

        mvc.perform(withOrigin(post("/stub/change"), session, EVIL)).andExpect(status().isForbidden());
        mvc.perform(withOrigin(post("/stub/change"), session, "null")).andExpect(status().isForbidden());
        mvc.perform(withOrigin(post("/stub/change").header("Referer", EVIL + "/page"), session, null))
                .andExpect(status().isForbidden());
    }

    @Test
    void aPostFromTheAppIsAccepted() throws Exception {
        MockMvc mvc = mvc();
        MockHttpSession session = signedIn(mvc);

        mvc.perform(withOrigin(post("/stub/change"), session, APP)).andExpect(status().isOk());
        mvc.perform(withOrigin(post("/stub/change"), session, "http://localhost:4200")).andExpect(status().isOk());
        mvc.perform(withOrigin(post("/stub/change"), session, null)).andExpect(status().isOk());
    }

    // ---- logout ----

    @Test
    void logoutByLinkOrImageIsNoLongerPossible() throws Exception {
        MockMvc mvc = mvc();
        MockHttpSession session = signedIn(mvc);

        mvc.perform(withOrigin(get("/logout"), session, null));
        mvc.perform(withOrigin(get("/stub/read"), session, null)).andExpect(status().isOk());
    }

    @Test
    void aCrossSiteLogoutPostIsRejectedAndTheAppsLogoutWorks() throws Exception {
        MockMvc mvc = mvc();
        MockHttpSession session = signedIn(mvc);

        mvc.perform(withOrigin(post("/logout"), session, EVIL)).andExpect(status().isForbidden());
        mvc.perform(withOrigin(get("/stub/read"), session, null)).andExpect(status().isOk());

        mvc.perform(withOrigin(post("/logout"), session, APP)).andExpect(status().isOk());
        mvc.perform(withOrigin(get("/stub/read"), session, null)).andExpect(status().isUnauthorized());
    }

    // ---- disabled account ----

    @Test
    void anAccountDisabledAfterSigningInIsRefusedOnEveryRequestButCanSignOut() throws Exception {
        MockMvc mvc = mvc();
        MockHttpSession session = signedIn(mvc);

        // Like the real check, an anonymous request is not a disabled account.
        when(sessionService.isDisabled(org.mockito.ArgumentMatchers.any())).thenAnswer(inv ->
                inv.getArgument(0) instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken);

        mvc.perform(withOrigin(get("/stub/read"), session, null)).andExpect(status().isForbidden());
        mvc.perform(withOrigin(post("/stub/change"), session, APP)).andExpect(status().isForbidden());
        // The public paths stay reachable, so that the sign-in page loads with the stale cookie of a disabled account.
        mvc.perform(withOrigin(get("/info/webpages"), session, null)).andExpect(status().isNotFound());
        // /ai/** is open on the chain but its endpoints work for the signed-in user, so it is not one of them.
        mvc.perform(withOrigin(get("/ai/models"), session, null)).andExpect(status().isForbidden());
        mvc.perform(withOrigin(post("/logout"), session, APP)).andExpect(status().isOk());
        mvc.perform(withOrigin(get("/stub/read"), session, null)).andExpect(status().isUnauthorized());
    }
}
